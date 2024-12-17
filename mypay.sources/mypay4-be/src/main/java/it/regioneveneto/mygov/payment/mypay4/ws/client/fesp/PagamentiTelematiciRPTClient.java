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
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.client.BaseClient;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Function;
import java.util.stream.Collectors;

public class PagamentiTelematiciRPTClient extends BaseClient {

  @Value("${pa.pspDefaultIdentificativoPsp}")
  private String identificativoPsp;

  @Autowired
  SystemBlockService systemBlockService;

  private final GiornaleService giornaleCommonService;

  public PagamentiTelematiciRPTClient(GiornaleService giornaleCommonService){
    this.giornaleCommonService = giornaleCommonService;
  }

  public NodoInviaRPTRisposta nodoInviaRPT(NodoInviaRPT request, IntestazionePPT header){
    systemBlockService.blockByOperationName("fesp.client.nodoInviaRPT");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      header.getIdentificativoDominio(),
      header.getIdentificativoUnivocoVersamento(),
      header.getCodiceContestoPagamento(),
      request.getIdentificativoPSP(),
      null,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaRPT.toString(),
      header.getIdentificativoIntermediarioPA(),
      Constants.NODO_DEI_PAGAMENTI_SPC,
      header.getIdentificativoStazioneIntermediarioPA(),
      request.getIdentificativoCanale(),
      () -> (NodoInviaRPTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(header)),
      OutcomeHelper::getOutcome
    );
  }
  public NodoInviaCarrelloRPTRisposta nodoInviaCarrelloRPT(NodoInviaCarrelloRPT request, IntestazioneCarrelloPPT header){
    systemBlockService.blockByOperationName("fesp.client.nodoInviaCarrelloRPT");
    Function<Function<TipoElementoListaRPT, String>, String> fun = x -> request.getListaRPT().getElementoListaRPTs()
      .stream()
      .map(x)
      .map(StringUtils::stripToEmpty)
      .collect(Collectors.joining(GiornaleService.SEPARATOR));

    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      fun.apply(TipoElementoListaRPT::getIdentificativoDominio),
      fun.apply(TipoElementoListaRPT::getIdentificativoUnivocoVersamento),
      fun.apply(TipoElementoListaRPT::getCodiceContestoPagamento),
      request.getIdentificativoPSP(),
      Constants.ALL_PAGAMENTI,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaCarrelloRPT.toString(),
      Constants.NODO_DEI_PAGAMENTI_SPC,
      header.getIdentificativoIntermediarioPA(),
      header.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> (NodoInviaCarrelloRPTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(header)),
      OutcomeHelper::getOutcome
    );
  }

  public NodoInviaRichiestaStornoRisposta nodoInviaRichiestaStorno(NodoInviaRichiestaStorno request){
    throw new UnsupportedOperationException("nodoInviaRichiestaStorno is not supported anymore");
    //return (NodoInviaRichiestaStornoRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediElencoQuadraturePARisposta nodoChiediElencoQuadraturePA(NodoChiediElencoQuadraturePA request){
    throw new UnsupportedOperationException("nodoChiediElencoQuadraturePA is not supported anymore");
    //return (NodoChiediElencoQuadraturePARisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediListaPendentiRPTRisposta nodoChiediListaPendentiRPT(NodoChiediListaPendentiRPT request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediListaPendentiRPT");
    return (NodoChiediListaPendentiRPTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediCopiaRTRisposta nodoChiediCopiaRT(NodoChiediCopiaRT request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediCopiaRT");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      request.getIdentificativoUnivocoVersamento(),
      request.getCodiceContestoPagamento(),
      null,
      null,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediCopiaRT.toString(),
      request.getIdentificativoIntermediarioPA(),
      Constants.NODO_DEI_PAGAMENTI_SPC,
      request.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> (NodoChiediCopiaRTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }
  public NodoChiediElencoFlussiRendicontazioneRisposta nodoChiediElencoFlussiRendicontazione(NodoChiediElencoFlussiRendicontazione request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediElencoFlussiRendicontazione");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      null,
      null,
      request.getIdentificativoPSP(),
      null,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediElencoFlussiRendicontaz.toString(),
      request.getIdentificativoIntermediarioPA(),
      Constants.NODO_DEI_PAGAMENTI_SPC,
      request.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> (NodoChiediElencoFlussiRendicontazioneRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }
  public NodoChiediInformativaPSPRisposta nodoChiediInformativaPSP(NodoChiediInformativaPSP request){
    throw new UnsupportedOperationException("nodoChiediInformativaPSP is not supported anymore");
    //return (NodoChiediInformativaPSPRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoChiediStatoRPTRisposta nodoChiediStatoRPT(NodoChiediStatoRPT request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediStatoRPT");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      request.getIdentificativoUnivocoVersamento(),
      request.getCodiceContestoPagamento(),
      identificativoPsp,
      null,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediStatoRPT.toString(),
      request.getIdentificativoIntermediarioPA(),
      Constants.NODO_DEI_PAGAMENTI_SPC,
      request.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> (NodoChiediStatoRPTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }
  public NodoChiediFlussoRendicontazioneRisposta nodoChiediFlussoRendicontazione(NodoChiediFlussoRendicontazione request){
    systemBlockService.blockByOperationName("fesp.client.nodoChiediFlussoRendicontazione");
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      null,
      null,
      request.getIdentificativoPSP(),
      null,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediFlussoRendicontazione.toString(),
      request.getIdentificativoIntermediarioPA(),
      Constants.NODO_DEI_PAGAMENTI_SPC,
      request.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> (NodoChiediFlussoRendicontazioneRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }
  public NodoChiediSceltaWISPRisposta nodoChiediSceltaWISP(NodoChiediSceltaWISP request){
    throw new UnsupportedOperationException("nodoChiediSceltaWISP is not supported anymore");
    //return (NodoChiediSceltaWISPRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }
  public NodoInviaRispostaRevocaRisposta nodoInviaRispostaRevoca(NodoInviaRispostaRevoca request){
    throw new UnsupportedOperationException("nodoInviaRispostaRevoca is not supported anymore");
    //return (NodoInviaRispostaRevocaRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback());
  }

}
