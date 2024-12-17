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


import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class MyEndpointInterceptor implements EndpointInterceptor, ClientInterceptor {

  @Autowired
  @Lazy
  GiornaleService giornaleCommonService;

  @SneakyThrows(IOException.class)
  private String getMessageContent(WebServiceMessage request) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    request.writeTo(baos);
    return baos.toString(StandardCharsets.UTF_8);
  }

  @Override
  public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
    return this.handleRequestImpl(messageContext, false);
  }

  @Override
  public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
    return this.handleResponseImpl(messageContext, false);
  }

  @Override
  public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
    return this.handleResponse(messageContext, endpoint);
  }

  @Override
  public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {

  }

  @Override
  public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
    return this.handleRequestImpl(messageContext, true);
  }

  @Override
  public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
    return this.handleResponseImpl(messageContext, true);
  }

  @Override
  public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
    return this.handleResponse(messageContext);
  }

  @Override
  public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {

  }

  private boolean handleRequestImpl(MessageContext messageContext, boolean isClient){
    if(isClient){
      giornaleCommonService.recordSoapEventLast(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ, getMessageContent(messageContext.getRequest()));
    } else {
      giornaleCommonService.recordSoapEventFirst(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ, getMessageContent(messageContext.getRequest()));
    }
    return true;
  }

  private boolean handleResponseImpl(MessageContext messageContext, boolean isClient){
    if(isClient){
      giornaleCommonService.recordSoapEventFirst(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES, getMessageContent(messageContext.getResponse()));
    } else {
      giornaleCommonService.recordSoapEventLast(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES, getMessageContent(messageContext.getResponse()));
    }
    return true;
  }
}
