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

import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.client.BaseClient;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class PagamentiTelematiciRPClient extends BaseClient implements PagamentiTelematiciRP {

  private final String identificativoIntermediarioPA;

  private final String identificativoStazioneIntermediarioPA;

  @Value("${pa.pspDefaultIdentificativoPsp}")
  private String identificativoPsp;

  private final GiornaleService giornaleCommonService;

  public PagamentiTelematiciRPClient(GiornaleService giornaleCommonService,
                                     String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA){
    this.giornaleCommonService = giornaleCommonService;
    this.identificativoIntermediarioPA = identificativoIntermediarioPA;
    this.identificativoStazioneIntermediarioPA = identificativoStazioneIntermediarioPA;
  }

  @Override
  public ChiediFlussoSPCRisposta chiediFlussoSPC(ChiediFlussoSPC request) {
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      request.getIdentificativoDominio(),
      null,
      null,
      request.getIdentificativoPSP(),
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.chiediFlussoSPC.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (ChiediFlussoSPCRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }

  @Override
  public ChiediFlussoSPCPageRisposta chiediFlussoSPCPage(ChiediFlussoSPCPage request){
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      request.getIdentificativoDominio(),
      null,
      null,
      request.getIdentificativoPSP(),
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.chiediFlussoSPCPage.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (ChiediFlussoSPCPageRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }

  @Override
  public ChiediListaFlussiSPCRisposta chiediListaFlussiSPC(ChiediListaFlussiSPC request) {
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      request.getIdentificativoDominio(),
      null,
      null,
      request.getIdentificativoPSP(),
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.chiediListaFlussiSPC.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (ChiediListaFlussiSPCRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }

  @Override
  public NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(NodoSILChiediCopiaEsito request){
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      request.getIdentificativoDominio(),
      request.getIdentificativoUnivocoVersamento(),
      request.getCodiceContestoPagamento(),
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILChiediCopiaEsito.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (NodoSILChiediCopiaEsitoRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }

  @Override
  public NodoSILInviaRPRisposta nodoSILInviaRP(NodoSILInviaRP request, IntestazionePPT header) {
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      header.getIdentificativoDominio(),
      header.getIdentificativoUnivocoVersamento(),
      header.getCodiceContestoPagamento(),
      request.getIdentificativoPSP(),
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaRP.toString(),
      header.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (NodoSILInviaRPRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(header)),
      OutcomeHelper::getOutcome
    );
  }

  @Override
  @LogExecution
  public NodoSILChiediIUVRisposta nodoSILChiediIUV(NodoSILChiediIUV request) {
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      request.getIdentificativoDominio(),
      null,
      null,
      null,
      request.getTipoVersamento(),
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILChiediIUV.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (NodoSILChiediIUVRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }

  @Override
  @LogExecution
  public NodoSILChiediCCPRisposta nodoSILChiediCCP(NodoSILChiediCCP request) {
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      request.getIdDominio(),
      request.getIuv(),
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILChiediCCP.toString(),
      request.getIdDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (NodoSILChiediCCPRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }

  @Override
  @LogExecution
  public NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP request) {
    Function<Function<ElementoRP, String>, String> fun = x -> request.getListaRP().getElementoRPs()
      .stream()
      .map(x)
      .map(StringUtils::stripToEmpty)
      .collect(Collectors.joining(GiornaleService.SEPARATOR));

    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      fun.apply(ElementoRP::getIdentificativoDominio),
      fun.apply(ElementoRP::getIdentificativoUnivocoVersamento),
      fun.apply(ElementoRP::getCodiceContestoPagamento),
      identificativoPsp,
      Constants.PAY_PRESSO_PSP,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaRP.toString(),
      request.getIdentificativoDominioEnteChiamante(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (NodoSILInviaCarrelloRPRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }

  @Override
  @LogExecution
  public NodoSILRichiediRTRisposta nodoSILRichiediRT(NodoSILRichiediRT request) {
    return giornaleCommonService.wrapRecordSoapClientEvent(
      Constants.GIORNALE_MODULO.PA,
      request.getIdentificativoDominio(),
      request.getIdentificativoUnivocoVersamento(),
      request.getCodiceContestoPagamento(),
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILRichiediRT.toString(),
      request.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> (NodoSILRichiediRTRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback()),
      OutcomeHelper::getOutcome
    );
  }
}
