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
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.veneto.regione.pagamenti.pa.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PagamentiTelematiciCCPPaClient extends BaseClient {

  private final String identificativoIntermediarioPA;

  private final String identificativoStazioneIntermediarioPA;

  private final GiornaleService giornaleCommonService;

  public PagamentiTelematiciCCPPaClient(GiornaleService giornaleCommonService,
                                        String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA){
    this.giornaleCommonService = giornaleCommonService;
    this.identificativoIntermediarioPA = identificativoIntermediarioPA;
    this.identificativoStazioneIntermediarioPA = identificativoStazioneIntermediarioPA;
  }

  public PaVerifyPaymentNoticeRisposta paVerifyPaymentNotice(PaVerifyPaymentNotice request, String wsUrl) {
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getQrCodeFiscalCode(),
      Utilities.numeroAvvisoToIuvValidator(request.getNoticeNumber()),
      null,
      null,
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.paVerifyPaymentNotice.toString(),
      request.getIdBrokerPA(),
      request.getIdPA(),
      request.getIdStation(),
      null,
      () -> (PaVerifyPaymentNoticeRisposta) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request),
      OutcomeHelper::getOutcome
    );
  }

  public PaGetPaymentRisposta paGetPayment(PaGetPayment request, String wsUrl) {
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getQrCodeFiscalCode(),
      Utilities.numeroAvvisoToIuvValidator(request.getNoticeNumber()),
      null,
      null,
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.paGetPayment.toString(),
      request.getIdBrokerPA(),
      request.getIdPA(),
      request.getIdStation(),
      null,
      () -> (PaGetPaymentRisposta) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request),
      OutcomeHelper::getOutcome
    );
  }

  public PaSendRTRisposta paSendRT(PaSendRT request, String wsUrl) {
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getReceipt().getFiscalCode(),
      Utilities.numeroAvvisoToIuvValidator(request.getReceipt().getNoticeNumber()),
      request.getReceipt().getReceiptId(),
      request.getReceipt().getIdPSP(),
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.paGetPayment.toString(),
      request.getReceipt().getFiscalCode(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (PaSendRTRisposta) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request),
      OutcomeHelper::getOutcome
    );
  }
}
