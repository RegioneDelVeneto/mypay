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

import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsitoRisposta;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class PagamentiTelematiciEsitoClient extends BaseClient {

  @Autowired
  SystemBlockService systemBlockService;

  private final GiornaleService giornaleCommonService;

  public PagamentiTelematiciEsitoClient(GiornaleService giornaleCommonService){
    this.giornaleCommonService = giornaleCommonService;
  }

  public PaaSILInviaEsitoRisposta paaSILInviaEsito(PaaSILInviaEsito request, IntestazionePPT header, String wsUrl) {
    systemBlockService.blockByOperationName("pa.client.paaSILInviaEsito");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      header.getIdentificativoDominio(),
      header.getIdentificativoUnivocoVersamento(),
      header.getCodiceContestoPagamento(),
      Constants.EMPTY,
      Constants.EMPTY,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.paaSILInviaEsito.toString(),
      header.getIdentificativoIntermediarioPA(),
      header.getIdentificativoDominio(),
      header.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> (PaaSILInviaEsitoRisposta) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request, getMessageCallback(header)),
      OutcomeHelper::getOutcome
    );
  }
}
