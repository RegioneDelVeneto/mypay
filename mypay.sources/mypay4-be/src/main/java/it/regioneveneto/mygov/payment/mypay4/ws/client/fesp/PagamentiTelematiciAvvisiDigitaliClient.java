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

import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.client.BaseClient;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciAvvisiDigitali;
import it.veneto.regione.pagamenti.nodoregionalefesp.IntestazionePPT;
import it.veneto.regione.pagamenti.nodoregionalefesp.NodoSILInviaAvvisoDigitale;
import it.veneto.regione.pagamenti.nodoregionalefesp.NodoSILInviaAvvisoDigitaleRisposta;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PagamentiTelematiciAvvisiDigitaliClient extends BaseClient implements PagamentiTelematiciAvvisiDigitali {
  private final GiornaleService giornaleCommonService;

  public PagamentiTelematiciAvvisiDigitaliClient(GiornaleService giornaleCommonService){
    this.giornaleCommonService = giornaleCommonService;
  }

  @Override
  public NodoSILInviaAvvisoDigitaleRisposta nodoSILInviaAvvisoDigitale(NodoSILInviaAvvisoDigitale request, IntestazionePPT header) {
    String iuv = null;
    if(request!=null && request.getAvvisoDigitaleWS()!=null)
      try{
        iuv = Utilities.numeroAvvisoToIuvValidator(request.getAvvisoDigitaleWS().getCodiceAvviso());
      }catch(Exception e){
        iuv =  request.getAvvisoDigitaleWS().getCodiceAvviso();
      }
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      header.getIdentificativoDominio(),
      iuv,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaAvvisoDigitale.toString(),
      header.getIdentificativoDominio(),
      header.getIdentificativoIntermediarioPA(),
      header.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> (NodoSILInviaAvvisoDigitaleRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(header)),
      OutcomeHelper::getOutcome
    );
  }
}
