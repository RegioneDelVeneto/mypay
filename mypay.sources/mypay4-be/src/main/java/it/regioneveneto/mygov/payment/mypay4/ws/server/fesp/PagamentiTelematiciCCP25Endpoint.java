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
package it.regioneveneto.mygov.payment.mypay4.ws.server.fesp;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciCCP25Impl;
import it.regioneveneto.mygov.payment.mypay4.ws.server.BaseEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@ConditionalOnWebApplication
public class PagamentiTelematiciCCP25Endpoint extends BaseEndpoint {
  public static final String NAMESPACE_URI = "http://ws.pagamenti.telematici.gov/";
  public static final String NAME = "PagamentiTelematiciCCP25";

  @Autowired
  private SystemBlockService systemBlockService;

  @Autowired
  @Qualifier("PagamentiTelematiciCCP25FespImpl")
  private PagamentiTelematiciCCP25Impl pagamentiTelematiciCCP25;

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paVerifyPaymentNotice")
  @ResponsePayload
  public PaVerifyPaymentNoticeRes paVerifyPaymentNotice(
      @RequestPayload PaVerifyPaymentNoticeReq request){
    systemBlockService.blockByOperationName("fesp.paVerifyPaymentNotice");
    return pagamentiTelematiciCCP25.paVerifyPaymentNotice(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paGetPayment")
  @ResponsePayload
  public PaGetPaymentRes paGetPayment(
      @RequestPayload PaGetPaymentReq request){
    systemBlockService.blockByOperationName("fesp.paGetPayment");
    return pagamentiTelematiciCCP25.paGetPayment(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paSendRT")
  @ResponsePayload
  public PaSendRTRes paSendRT(
      @RequestPayload PaSendRTReq request){
    systemBlockService.blockByOperationName("fesp.paSendRT");
    return pagamentiTelematiciCCP25.paSendRT(request);
  }
}
