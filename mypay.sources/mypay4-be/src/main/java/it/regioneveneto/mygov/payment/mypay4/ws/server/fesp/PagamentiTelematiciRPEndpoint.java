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

import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciRPImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.server.BaseEndpoint;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;

import java.util.function.Function;
import java.util.stream.Collectors;

@Endpoint
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@ConditionalOnWebApplication
public class PagamentiTelematiciRPEndpoint extends BaseEndpoint {
  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/nodoregionalefesp/";
  public static final String NAME = "PagamentiTelematiciRP";

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  @Value("${pa.pspDefaultIdentificativoPsp}")
  private String identificativoPsp;

  @Value("${pa.pspDefaultIdentificativoCanale}")
  private String identificativoCanale;

  @Autowired
  @Qualifier("PagamentiTelematiciRPImpl")
  private PagamentiTelematiciRPImpl pagamentiTelematiciRP;

  @Autowired
  GiornaleService giornaleCommonService;

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "chiediFlussoSPC")
  @ResponsePayload
  public ChiediFlussoSPCRisposta chiediFlussoSPC(@RequestPayload ChiediFlussoSPC request){
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      null,
      null,
      request.getIdentificativoPSP(),
      Constants.ALL_PAGAMENTI,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.chiediFlussoSPC.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      identificativoCanale,
      () -> pagamentiTelematiciRP.chiediFlussoSPC(request),
      OutcomeHelper::getOutcome
    );
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "chiediFlussoSPCPage")
  @ResponsePayload
  public ChiediFlussoSPCPageRisposta chiediFlussoSPCPage(@RequestPayload ChiediFlussoSPCPage request){
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      null,
      null,
      request.getIdentificativoPSP(),
      Constants.ALL_PAGAMENTI,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.chiediFlussoSPCPage.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      identificativoCanale,
      () -> pagamentiTelematiciRP.chiediFlussoSPCPage(request),
      OutcomeHelper::getOutcome
    );
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "chiediListaFlussiSPC")
  @ResponsePayload
  public ChiediListaFlussiSPCRisposta chiediListaFlussiSPC(@RequestPayload ChiediListaFlussiSPC request){
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      null,
      null,
      request.getIdentificativoPSP(),
      Constants.ALL_PAGAMENTI,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.chiediListaFlussiSPC.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      identificativoCanale,
      () -> pagamentiTelematiciRP.chiediListaFlussiSPC(request),
      OutcomeHelper::getOutcome
    );
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILChiediCopiaEsito")
  @ResponsePayload
  public NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(@RequestPayload NodoSILChiediCopiaEsito request){
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      request.getIdentificativoUnivocoVersamento(),
      request.getCodiceContestoPagamento(),
      identificativoPsp,
      Constants.ALL_PAGAMENTI,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILRichiediRT.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      identificativoCanale,
      () -> pagamentiTelematiciRP.nodoSILChiediCopiaEsito(request),
      OutcomeHelper::getOutcome
    );
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILInviaRP")
  @ResponsePayload
  public NodoSILInviaRPRisposta nodoSILInviaRP(
      @RequestPayload NodoSILInviaRP request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/nodoregionalefesp/ppthead}intestazionePPT") SoapHeaderElement header){
    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.FESP,
      intestazionePPT.getIdentificativoDominio(),
      intestazionePPT.getIdentificativoUnivocoVersamento(),
      intestazionePPT.getCodiceContestoPagamento(),
      request.getIdentificativoPSP(),
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaRP.toString(),
      intestazionePPT.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      request.getIdentificativoCanale(),
      () -> pagamentiTelematiciRP.nodoSILInviaRP(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILChiediIUV")
  @ResponsePayload
  public NodoSILChiediIUVRisposta nodoSILChiediIUV(@RequestPayload NodoSILChiediIUV request){
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      null,
      null,
      identificativoPsp,
      Constants.ALL_PAGAMENTI,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILChiediIUV.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      identificativoCanale,
      () -> pagamentiTelematiciRP.nodoSILChiediIUV(request),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILInviaCarrelloRP")
  @ResponsePayload
  public NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(@RequestPayload NodoSILInviaCarrelloRP request) {
    Function<Function<ElementoRP, String>, String> fun = x -> request.getListaRP().getElementoRPs()
      .stream()
      .map(x)
      .map(StringUtils::stripToEmpty)
      .collect(Collectors.joining(GiornaleService.SEPARATOR));

    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.FESP,
      fun.apply(ElementoRP::getIdentificativoDominio),
      fun.apply(ElementoRP::getIdentificativoUnivocoVersamento),
      fun.apply(ElementoRP::getCodiceContestoPagamento),
      identificativoPsp,
      Constants.ALL_PAGAMENTI,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaCarrelloRP.toString(),
      request.getIdentificativoDominioEnteChiamante(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      identificativoCanale,
      () -> pagamentiTelematiciRP.nodoSILInviaCarrelloRP(request),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILRichiediRT")
  @ResponsePayload
  public NodoSILRichiediRTRisposta nodoSILRichiediRT(@RequestPayload NodoSILRichiediRT request) {
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.FESP,
      request.getIdentificativoDominio(),
      request.getIdentificativoUnivocoVersamento(),
      request.getCodiceContestoPagamento(),
      identificativoPsp,
      Constants.ALL_PAGAMENTI,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILRichiediRT.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      identificativoCanale,
      () -> pagamentiTelematiciRP.nodoSILRichiediRT(request),
      OutcomeHelper::getOutcome
    );
  }
}
