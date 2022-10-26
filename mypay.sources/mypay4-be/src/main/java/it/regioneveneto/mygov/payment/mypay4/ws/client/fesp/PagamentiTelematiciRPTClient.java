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

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.ws.client.BaseClient;
import org.springframework.beans.factory.annotation.Autowired;

public class PagamentiTelematiciRPTClient extends BaseClient {

  @Autowired
  SystemBlockService systemBlockService;

  public NodoInviaRPTRisposta nodoInviaRPT(NodoInviaRPT request, IntestazionePPT header){
    systemBlockService.blockByOperationName("fesp.client.nodoInviaRPT");
    return (NodoInviaRPTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(header));
  }
  public NodoInviaCarrelloRPTRisposta nodoInviaCarrelloRPT(NodoInviaCarrelloRPT request, IntestazioneCarrelloPPT header){
    systemBlockService.blockByOperationName("fesp.client.nodoInviaCarrelloRPT");
    return (NodoInviaCarrelloRPTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(header));
  }
  public NodoChiediQuadraturaPARisposta nodoChiediQuadraturaPA(NodoChiediQuadraturaPA request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediQuadraturaPA");
    return (NodoChiediQuadraturaPARisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoInviaRichiestaStornoRisposta nodoInviaRichiestaStorno(NodoInviaRichiestaStorno request){
    systemBlockService.blockByOperationName("fesp.client.nodoInviaRichiestaStorno");
    return (NodoInviaRichiestaStornoRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediElencoQuadraturePARisposta nodoChiediElencoQuadraturePA(NodoChiediElencoQuadraturePA request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediElencoQuadraturePA");
    return (NodoChiediElencoQuadraturePARisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediListaPendentiRPTRisposta nodoChiediListaPendentiRPT(NodoChiediListaPendentiRPT request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediListaPendentiRPT");
    return (NodoChiediListaPendentiRPTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediCopiaRTRisposta nodoChiediCopiaRT(NodoChiediCopiaRT request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediCopiaRT");
    return (NodoChiediCopiaRTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediElencoFlussiRendicontazioneRisposta nodoChiediElencoFlussiRendicontazione(NodoChiediElencoFlussiRendicontazione request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediElencoFlussiRendicontazione");
    return (NodoChiediElencoFlussiRendicontazioneRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediInformativaPSPRisposta nodoChiediInformativaPSP(NodoChiediInformativaPSP request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediInformativaPSP");
    return (NodoChiediInformativaPSPRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediStatoRPTRisposta nodoChiediStatoRPT(NodoChiediStatoRPT request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediStatoRPT");
    return (NodoChiediStatoRPTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediFlussoRendicontazioneRisposta nodoChiediFlussoRendicontazione(NodoChiediFlussoRendicontazione request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediFlussoRendicontazione");
    return (NodoChiediFlussoRendicontazioneRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediSceltaWISPRisposta nodoChiediSceltaWISP(NodoChiediSceltaWISP request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediSceltaWISP");
    return (NodoChiediSceltaWISPRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoInviaRispostaRevocaRisposta nodoInviaRispostaRevoca(NodoInviaRispostaRevoca request){
    systemBlockService.blockByOperationName("fesp.client.nodoInviaRispostaRevoca");
    return (NodoInviaRispostaRevocaRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }

}
