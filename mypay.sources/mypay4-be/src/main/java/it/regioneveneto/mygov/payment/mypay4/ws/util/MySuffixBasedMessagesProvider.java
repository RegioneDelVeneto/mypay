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

import org.springframework.util.Assert;
import org.springframework.ws.wsdl.wsdl11.provider.SuffixBasedMessagesProvider;
import org.w3c.dom.Element;


public class MySuffixBasedMessagesProvider extends SuffixBasedMessagesProvider {

  protected String requestSuffix = DEFAULT_REQUEST_SUFFIX;

  public String getRequestSuffix() {
    return this.requestSuffix;
  }

  public void setRequestSuffix(String requestSuffix) {
    this.requestSuffix = requestSuffix;
  }

  @Override
  protected boolean isMessageElement(Element element) {
    if (isMessageElement0(element)) {
      String elementName = getElementName(element);
      Assert.hasText(elementName, "Element has no name");
      return elementName.endsWith(getResponseSuffix())
          || (getRequestSuffix().isEmpty() || elementName.endsWith(getRequestSuffix()))
          || elementName.endsWith(getFaultSuffix());
    }
    return false;
  }

  protected boolean isMessageElement0(Element element) {
    return "element".equals(element.getLocalName())
        && "http://www.w3.org/2001/XMLSchema".equals(element.getNamespaceURI());
  }
}
