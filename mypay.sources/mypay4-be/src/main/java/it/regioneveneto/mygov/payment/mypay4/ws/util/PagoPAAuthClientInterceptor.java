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

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

import java.io.IOException;

@Component
@Slf4j
public class PagoPAAuthClientInterceptor extends ClientInterceptorAdapter {

  public final static String SUBSCRIPTION_KEY_KEY = "Ocp-Apim-Subscription-Key";

  @Value("${ws.pagamentiTelematiciRPT.remoteurl:}")
  private String pagoPaWsUrl;
  @Value("${ws.pagamentiTelematiciRPT.api.subscription.key:}")
  private String pagoPaWsApiKey;

  @Override
  public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {

    if(StringUtils.isNotBlank(pagoPaWsApiKey)) {
      try {
        TransportContext context = TransportContextHolder.getTransportContext();
        WebServiceConnection connection = context.getConnection();
        if (StringUtils.startsWithIgnoreCase(connection.getUri().toString(), pagoPaWsUrl)) {
          ((HeadersAwareSenderWebServiceConnection) connection).addRequestHeader(SUBSCRIPTION_KEY_KEY, pagoPaWsApiKey);
        }
      } catch (IOException ioe) {
        throw new WebServiceIOException("error adding PagoPA auth header", ioe);
      } catch (Exception e) {
        throw new MyPayException("error adding PagoPA auth header", e);
      }
    }

    return super.handleRequest(messageContext);
  }
}
