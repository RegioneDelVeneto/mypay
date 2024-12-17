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
package it.regioneveneto.mygov.payment.mypay4.ws.client.fesp;

import gov.telematici.pagamenti.ws.CtAvvisoDigitale;
import gov.telematici.pagamenti.ws.NodoInviaAvvisoDigitale;
import gov.telematici.pagamenti.ws.NodoInviaAvvisoDigitaleRisposta;
import gov.telematici.pagamenti.ws.sachead.IntestazionePPT;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.client.BaseClient;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class PagamentiTelematiciAvvisiDigitaliServiceClient extends BaseClient {

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;
  @Autowired
  SystemBlockService systemBlockService;

  private final GiornaleService giornaleCommonService;
  
  public PagamentiTelematiciAvvisiDigitaliServiceClient(GiornaleService giornaleCommonService){
    this.giornaleCommonService = giornaleCommonService;
  }

  public NodoInviaAvvisoDigitaleRisposta nodoInviaAvvisoDigitale(NodoInviaAvvisoDigitale request, IntestazionePPT header) {
    systemBlockService.blockByOperationName("fesp.client.nodoInviaAvvisoDigitale");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      header.getIdentificativoDominio(),
      Utilities.ifNotNull(request.getAvvisoDigitaleWS(), CtAvvisoDigitale::getCodiceAvviso),
      null,
      null,
      null,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaAvvisoDigitale.toString(),
      header.getIdentificativoDominio(),
      Constants.NODO_DEI_PAGAMENTI_SPC,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (NodoInviaAvvisoDigitaleRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(header)),
      OutcomeHelper::getOutcome
    );
  }



}
