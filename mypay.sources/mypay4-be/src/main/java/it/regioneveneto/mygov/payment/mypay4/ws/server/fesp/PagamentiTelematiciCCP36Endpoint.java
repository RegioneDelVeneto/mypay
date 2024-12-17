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
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.GiornaleElapsedDao;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.GiornaleElapsedService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciCCP36Impl;
import it.regioneveneto.mygov.payment.mypay4.ws.server.BaseEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@ConditionalOnExpression("'${fesp.mode}'.equals('local') && ${pa.modelloUnico}")
@ConditionalOnWebApplication
public class PagamentiTelematiciCCP36Endpoint extends BaseEndpoint {
  public static final String NAMESPACE_URI = "http://pagopa-api.pagopa.gov.it/pa/paForNode.xsd";
  public static final String NAME = "PagamentiTelematiciCCP36";

  @Autowired
  GiornaleService giornaleCommonService;

  @Autowired
  private GiornaleElapsedService giornaleElapsedService;

  @Autowired
  private SystemBlockService systemBlockService;

  @Autowired
  @Qualifier("PagamentiTelematiciCCP36FespImpl")
  private PagamentiTelematiciCCP36Impl pagamentiTelematiciCCP36;

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PaDemandPaymentNoticeRequest")
  @ResponsePayload
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public PaDemandPaymentNoticeResponse paDemandPaymentNotice(@RequestPayload PaDemandPaymentNoticeRequest request) {
    systemBlockService.blockByOperationName("fesp.paDemandPaymentNotice");

    long startTime = System.currentTimeMillis();
    PaDemandPaymentNoticeResponse response = null;
    try {
      response = giornaleCommonService.wrapRecordSoapServerEvent(
          Constants.GIORNALE_MODULO.FESP,
          request.getIdPA(),
          null,
          null,
          null,
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.paDemandPaymentNotice.toString(),
          Constants.NODO_DEI_PAGAMENTI_SPC,
          request.getIdBrokerPA(),
          request.getIdStation(),
          null,
          () -> pagamentiTelematiciCCP36.paDemandPaymentNotice(request),
          OutcomeHelper::getOutcome
      );
      return response;
    } finally {
      if(request!=null)
        giornaleElapsedService.insertGiornaleElapsed(GiornaleElapsedDao.Operation.paDemandPaymentNotic,
            request.getIdPA(),
            null,
            response == null || response.getFault() != null,
            startTime );
    }
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paVerifyPaymentNoticeReq")
  @ResponsePayload
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public PaVerifyPaymentNoticeRes paVerifyPaymentNotice(@RequestPayload PaVerifyPaymentNoticeReq request){
    systemBlockService.blockByOperationName("fesp.paVerifyPaymentNotice");

    long startTime = System.currentTimeMillis();
    PaVerifyPaymentNoticeRes response = null;
    try {
      response = giornaleCommonService.wrapRecordSoapServerEvent(
        Constants.GIORNALE_MODULO.FESP,
        request.getIdPA(),
        Utilities.numeroAvvisoToIuvValidator(request.getQrCode().getNoticeNumber()),
        null,
        null,
        Constants.PAY_PRESSO_PSP,
        Constants.COMPONENTE_FESP,
        Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
        Constants.GIORNALE_TIPO_EVENTO_FESP.paVerifyPaymentNotice.toString(),
        Constants.NODO_DEI_PAGAMENTI_SPC,
        request.getIdBrokerPA(),
        request.getIdStation(),
        null,
        () -> pagamentiTelematiciCCP36.paVerifyPaymentNotice(request),
        OutcomeHelper::getOutcome
      );
      return response;
    }finally{
      if(request!=null && request.getQrCode()!=null)
        giornaleElapsedService.insertGiornaleElapsed(GiornaleElapsedDao.Operation.paVerifyPaymentNotic,
          request.getQrCode().getFiscalCode(),
          Utilities.tryOrInputValueWrapper(Utilities::numeroAvvisoToIuvValidator).apply(request.getQrCode().getNoticeNumber()),
          response == null || response.getFault() != null,
          startTime );
    }
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paGetPaymentV2Request")
  @ResponsePayload
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public PaGetPaymentV2Response paGetPaymentV2(@RequestPayload PaGetPaymentV2Request request) {
    systemBlockService.blockByOperationName("fesp.paGetPaymentV2");

    long startTime = System.currentTimeMillis();
    PaGetPaymentV2Response response = null;
    try {
      response = giornaleCommonService.wrapRecordSoapServerEvent(
          Constants.GIORNALE_MODULO.FESP,
          request.getIdPA(),
          Utilities.numeroAvvisoToIuvValidator(request.getQrCode().getNoticeNumber()),
          null,
          null,
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.paGetPaymentV2.toString(),
          Constants.NODO_DEI_PAGAMENTI_SPC,
          request.getIdBrokerPA(),
          request.getIdStation(),
          null,
          () -> pagamentiTelematiciCCP36.paGetPaymentV2(request),
          OutcomeHelper::getOutcome
      );
      return response;
    }finally{
      if(request!=null && request.getQrCode()!=null)
        giornaleElapsedService.insertGiornaleElapsed(GiornaleElapsedDao.Operation.paGetPaymentV2,
            request.getQrCode().getFiscalCode(),
            Utilities.tryOrInputValueWrapper(Utilities::numeroAvvisoToIuvValidator).apply(request.getQrCode().getNoticeNumber()),
            response == null || response.getFault() != null,
            startTime );
    }
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paSendRTV2Request")
  @ResponsePayload
  public PaSendRTV2Response paSendRTV2(@RequestPayload PaSendRTV2Request request){
    systemBlockService.blockByOperationName("fesp.paSendRTV2");

    long startTime = System.currentTimeMillis();
    PaSendRTV2Response response = null;
    try {
      response = giornaleCommonService.wrapRecordSoapServerEvent(
          Constants.GIORNALE_MODULO.FESP,
          request.getIdPA(),
          Utilities.numeroAvvisoToIuvValidator(request.getReceipt().getNoticeNumber()),
          request.getReceipt().getReceiptId(),
          request.getReceipt().getIdPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.paSendRTV2.toString(),
          Constants.NODO_DEI_PAGAMENTI_SPC,
          request.getIdBrokerPA(),
          request.getIdStation(),
          request.getReceipt().getIdChannel(),
          () -> pagamentiTelematiciCCP36.paSendRtV2(request),
          OutcomeHelper::getOutcome
      );
      return response;
    }finally{
      if(request!=null && request.getReceipt()!=null)
        giornaleElapsedService.insertGiornaleElapsed(GiornaleElapsedDao.Operation.paSendRTV2,
            request.getIdPA(), // idPa is used instead of receipt.companyName because we want to know to which ente PagoPa sent the RT (not the ente creating the notice)
            request.getReceipt().getCreditorReferenceId(),
            response == null || response.getFault() != null,
            startTime );
    }
  }
}
