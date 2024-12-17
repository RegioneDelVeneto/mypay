/**
 *     MyPay - Payment portal of Regione Veneto.
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.mygov.payment.mypay4.ws.util;

import org.springframework.ws.wsdl.wsdl11.provider.SuffixBasedPortTypesProvider;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.util.*;

public class MySuffixBasedPortTypesProvider extends SuffixBasedPortTypesProvider {

  private String requestSuffix = DEFAULT_REQUEST_SUFFIX;

  @Override
  public String getRequestSuffix() {
    return requestSuffix;
  }

  @Override
  public void setRequestSuffix(String requestSuffix) {
    this.requestSuffix = requestSuffix;
  }

  @Override
  protected String getOperationName(Message message) {
    String messageName = getMessageName(message);
    String result = null;
    if (messageName != null) {
      if (messageName.endsWith(getResponseSuffix())) {
        result = messageName.substring(0, messageName.length() - getResponseSuffix().length());
      } else if (messageName.endsWith(getFaultSuffix())) {
        result = messageName.substring(0, messageName.length() - getFaultSuffix().length());
      } else if (messageName.endsWith(getRequestSuffix())) {
        result = messageName.substring(0, messageName.length() - getRequestSuffix().length());
      }
    }
    return result;
  }

  @Override
  protected boolean isInputMessage(Message message) {
    String messageName = getMessageName(message);

    return messageName != null && !messageName.endsWith(getResponseSuffix());
  }

  @SuppressWarnings("java:S2177")
  private String getMessageName(Message message) {
    return message.getQName().getLocalPart();
  }

  @Override
  public void addPortTypes(Definition definition) throws WSDLException {
    super.addPortTypes(definition);
    Map<QName, List<Operation>> toRemove = new HashMap<>();
    for(Object key : definition.getPortTypes().keySet()){
      PortType portType = definition.getPortType((QName) key);
      toRemove.put(portType.getQName(),new ArrayList<>());
      for(Object operObj : portType.getOperations()){
        Operation operation = (Operation) operObj;
        if(operation.getOutput()==null)
          toRemove.get(portType.getQName()).add(operation);
      }
    }
    for(Map.Entry<QName, List<Operation>> entry: toRemove.entrySet())
      for(Operation operation : entry.getValue())
        definition.getPortType(entry.getKey()).removeOperation(operation.getName(), operation.getInput().getName(), null);
  }
}