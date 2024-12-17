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

import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.veneto.regione.pagamenti.pa.*;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class PagamentiTelematiciEsterniCCPClient extends BaseClient {

  @Autowired
  SystemBlockService systemBlockService;

  private final String identificativoIntermediarioPA;

  private final String identificativoStazioneIntermediarioPA;

  private final GiornaleService giornaleCommonService;

  public PagamentiTelematiciEsterniCCPClient(GiornaleService giornaleCommonService,
                                             String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA){
    this.giornaleCommonService = giornaleCommonService;
    this.identificativoIntermediarioPA = identificativoIntermediarioPA;
    this.identificativoStazioneIntermediarioPA = identificativoStazioneIntermediarioPA;
  }

  public PaaSILVerificaEsternaRisposta paaSILVerificaEsterna(PaaSILVerificaEsterna request, IntestazionePPT header, String wsUrl) {
    systemBlockService.blockByOperationName("pa.client.paaSILVerificaEsterna");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      header.getIdentificativoDominio(),
      header.getIdentificativoUnivocoVersamento(),
      header.getCodiceContestoPagamento(),
      request.getIdentificativoPSP(),
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILVerificaEsterna.toString(),
      header.getIdentificativoIntermediarioPA(),
      header.getIdentificativoDominio(),
      header.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> (PaaSILVerificaEsternaRisposta) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request, getMessageCallback(header)),
      OutcomeHelper::getOutcome
    );
  }

  public PaaSILAttivaEsternaRisposta paaSILAttivaEsterna(PaaSILAttivaEsterna request, IntestazionePPT header, String wsUrl) {
    systemBlockService.blockByOperationName("pa.client.paaSILAttivaEsterna");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      header.getIdentificativoDominio(),
      header.getIdentificativoUnivocoVersamento(),
      header.getCodiceContestoPagamento(),
      request.getIdentificativoPSP(),
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILAttivaEsterna.toString(),
      header.getIdentificativoIntermediarioPA(),
      header.getIdentificativoDominio(),
      header.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> (PaaSILAttivaEsternaRisposta) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request, getMessageCallback(header)),
      OutcomeHelper::getOutcome
    );
  }

  public PaExternalVerifyPaymentNoticeRes paExternalVerifyPaymentNotice(PaExternalVerifyPaymentNoticeReq request, String wsUrl) {
    systemBlockService.blockByOperationName("pa.client.paExternalVerifyPaymentNotice");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      request.getIdPA(),
      Utilities.numeroAvvisoToIuvValidator(request.getNoticeNumber()),
      null,
      null,
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paExternalVerifyPaymentNotice.toString(),
      request.getIdBrokerPA(),
      request.getIdPA(),
      request.getIdStation(),
      null,
      () -> (PaExternalVerifyPaymentNoticeRes) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request),
      OutcomeHelper::getOutcome
    );
  }

  public PaExternalGetPaymentRes paExternalGetPayment(PaExternalGetPaymentReq request, String wsUrl) {
    systemBlockService.blockByOperationName("pa.client.paExternalGetPayment");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      request.getIdPA(),
      Utilities.numeroAvvisoToIuvValidator(request.getNoticeNumber()),
      null,
      null,
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paExternalGetPayment.toString(),
      request.getIdBrokerPA(),
      request.getIdPA(),
      request.getIdStation(),
      null,
      () -> (PaExternalGetPaymentRes) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request),
      OutcomeHelper::getOutcome
    );
  }
}
