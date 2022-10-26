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
package it.regioneveneto.mygov.payment.mypay4.ws.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public abstract class BaseClient extends WebServiceGatewaySupport {

  protected WebServiceMessageCallback getMessageCallback(){
    return getMessageCallback(null);
  }

  protected WebServiceMessageCallback getMessageCallback(Object header){
    return message -> {
      try {
        //fix SoapAction header not included automatically by Spring WS
        Arrays.stream(Thread.currentThread().getStackTrace())
          .filter(i -> {
            try {
              return !i.getClassName().equals(BaseClient.class.getName()) && BaseClient.class.isAssignableFrom(Class.forName(i.getClassName()));
            } catch (ClassNotFoundException e) {
              return false;
            } })
          .findFirst()
          .ifPresent(ste -> {
            String methodName = ste.getMethodName();
            methodName = StringUtils.firstNonBlank(StringUtils.substringAfter(methodName, "lambda$"), methodName);
            methodName = StringUtils.substringBefore(methodName, "$");
            log.debug("ste: {} - method: {} [{}]",ste, methodName, ste.getMethodName());
            ((SoapMessage)message).setSoapAction(methodName);
          });


        if(header==null)
          return;
        SoapHeader soapHeader = ((SoapMessage) message).getSoapHeader();
        Class objFactoryClass = Class.forName(header.getClass().getPackageName()+".ObjectFactory");
        Method createMethod = objFactoryClass.getMethod("create"+ StringUtils.capitalize(header.getClass().getSimpleName()));
        Object headerObj = createMethod.invoke(objFactoryClass.getConstructor().newInstance());
        BeanUtils.copyProperties(headerObj, header);
        JAXBContext context = JAXBContext.newInstance(header.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(headerObj, soapHeader.getResult());
      } catch(Exception e){
        log.error("error during marshalling of the SOAP headers", e);
        throw new RuntimeException("error during marshalling of the SOAP headers", e);
      }
    };
  }

}
