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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.ws.wsdl.wsdl11.ProviderBasedWsdl4jDefinition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.provider.InliningXsdSchemaTypesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.SoapProvider;
import org.springframework.ws.wsdl.wsdl11.provider.SuffixBasedMessagesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.SuffixBasedPortTypesProvider;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

import javax.xml.transform.Source;
import java.util.Properties;

public class MyWsdl11Definition implements Wsdl11Definition, InitializingBean {

  private final InliningXsdSchemaTypesProvider typesProvider = new InliningXsdSchemaTypesProvider();

  private final SuffixBasedMessagesProvider messagesProvider = new MySuffixBasedMessagesProvider();

  private final SuffixBasedPortTypesProvider portTypesProvider = new MySuffixBasedPortTypesProvider();

  private final SoapProvider soapProvider = new SoapProvider();

  private final ProviderBasedWsdl4jDefinition delegate = new ProviderBasedWsdl4jDefinition();

  private String serviceName;

  public MyWsdl11Definition() {
    delegate.setTypesProvider(typesProvider);
    delegate.setMessagesProvider(messagesProvider);
    delegate.setPortTypesProvider(portTypesProvider);
    delegate.setBindingsProvider(soapProvider);
    delegate.setServicesProvider(soapProvider);
  }

  public void setTargetNamespace(String targetNamespace) {
    delegate.setTargetNamespace(targetNamespace);
  }

  public void setSchema(XsdSchema schema) {
    typesProvider.setSchema(schema);
  }

  public void setSchemaCollection(XsdSchemaCollection schemaCollection) {
    typesProvider.setSchemaCollection(schemaCollection);
  }

  public void setPortTypeName(String portTypeName) {
    portTypesProvider.setPortTypeName(portTypeName);
  }

  public void setRequestSuffix(String requestSuffix) {
    portTypesProvider.setRequestSuffix(requestSuffix);
    messagesProvider.setRequestSuffix(requestSuffix);
  }

  public void setResponseSuffix(String responseSuffix) {
    portTypesProvider.setResponseSuffix(responseSuffix);
    messagesProvider.setResponseSuffix(responseSuffix);
  }

  public void setFaultSuffix(String faultSuffix) {
    portTypesProvider.setFaultSuffix(faultSuffix);
    messagesProvider.setFaultSuffix(faultSuffix);
  }

  public void setCreateSoap11Binding(boolean createSoap11Binding) {
    soapProvider.setCreateSoap11Binding(createSoap11Binding);
  }

  public void setCreateSoap12Binding(boolean createSoap12Binding) {
    soapProvider.setCreateSoap12Binding(createSoap12Binding);
  }

  public void setSoapActions(Properties soapActions) {
    soapProvider.setSoapActions(soapActions);
  }

  public void setTransportUri(String transportUri) {
    soapProvider.setTransportUri(transportUri);
  }

  public void setLocationUri(String locationUri) {
    soapProvider.setLocationUri(locationUri);
  }

  public void setServiceName(String serviceName) {
    soapProvider.setServiceName(serviceName);
    this.serviceName = serviceName;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (!StringUtils.hasText(delegate.getTargetNamespace()) && typesProvider.getSchemaCollection() != null &&
        typesProvider.getSchemaCollection().getXsdSchemas().length > 0) {
      XsdSchema schema = typesProvider.getSchemaCollection().getXsdSchemas()[0];
      setTargetNamespace(schema.getTargetNamespace());
    }
    if (!StringUtils.hasText(serviceName) && StringUtils.hasText(portTypesProvider.getPortTypeName())) {
      soapProvider.setServiceName(portTypesProvider.getPortTypeName() + "Service");
    }
    delegate.afterPropertiesSet();
  }

  @Override
  public Source getSource() {
    return delegate.getSource();
  }

}