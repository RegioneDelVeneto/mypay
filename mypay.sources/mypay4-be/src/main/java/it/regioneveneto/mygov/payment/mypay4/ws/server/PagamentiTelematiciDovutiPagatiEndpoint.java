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
package it.regioneveneto.mygov.payment.mypay4.ws.server;

import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.logging.LogExecutionAspect;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciDovutiPagatiImpl;
import it.veneto.regione.pagamenti.ente.*;
import it.veneto.regione.pagamenti.ente.ppthead.IntestazionePPT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

@Endpoint
@ConditionalOnWebApplication
public class PagamentiTelematiciDovutiPagatiEndpoint extends BaseEndpoint {
  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/ente/";
  public static final String NAME = "PagamentiTelematiciDovutiPagati";

  @Autowired
  @Qualifier("PagamentiTelematiciDovutiPagatiImpl")
  private PagamentiTelematiciDovutiPagatiImpl pagamentiTelematiciDovutiPagati;

  @Autowired
  private SystemBlockService systemBlockService;

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILAutorizzaImportFlusso")
  @ResponsePayload
  public PaaSILAutorizzaImportFlussoRisposta pivotSILAutorizzaImportFlusso(
      @RequestPayload PaaSILAutorizzaImportFlusso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILAutorizzaImportFlusso");
    return pagamentiTelematiciDovutiPagati.paaSILAutorizzaImportFlusso(request, unmarshallHeader(header, IntestazionePPT.class));
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediEsitoCarrelloDovuti")
  @ResponsePayload
  public PaaSILChiediEsitoCarrelloDovutiRisposta paaSILChiediEsitoCarrelloDovuti(
      @RequestPayload PaaSILChiediEsitoCarrelloDovuti request){
    systemBlockService.blockByOperationName("pa.paaSILChiediEsitoCarrelloDovuti");
    PaaSILChiediEsitoCarrelloDovutiRisposta risposta = new PaaSILChiediEsitoCarrelloDovutiRisposta();
    Holder<FaultBean> fault = new Holder<>();
    Holder<ListaCarrelli> listaCarrelli = new Holder<>();
    pagamentiTelematiciDovutiPagati.paaSILChiediEsitoCarrelloDovuti(request.getCodIpaEnte(), request.getPassword(),
        request.getIdSessionCarrello(), fault, listaCarrelli);
    risposta.setFault(fault.value);
    risposta.setListaCarrelli(listaCarrelli.value);
    return  risposta;
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediPagati")
  @ResponsePayload
  public PaaSILChiediPagatiRisposta paaSILChiediPagati(
      @RequestPayload PaaSILChiediPagati request){
    systemBlockService.blockByOperationName("pa.paaSILChiediPagati");
    PaaSILChiediPagatiRisposta risposta = new PaaSILChiediPagatiRisposta();
    Holder<FaultBean> fault = new Holder<>();
    Holder<DataHandler> pagati = new Holder<>();
    pagamentiTelematiciDovutiPagati.paaSILChiediPagati(request.getCodIpaEnte(), request.getPassword(), request.getIdSession(), fault, pagati);
    risposta.setFault(fault.value);
    risposta.setPagati(pagati.value);
    return risposta;
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediPagatiConRicevuta")
  @ResponsePayload
  public PaaSILChiediPagatiConRicevutaRisposta paaSILChiediPagatiConRicevuta(
      @RequestPayload PaaSILChiediPagatiConRicevuta request){
    systemBlockService.blockByOperationName("pa.paaSILChiediPagatiConRicevuta");
    PaaSILChiediPagatiConRicevutaRisposta risposta = new PaaSILChiediPagatiConRicevutaRisposta();
    Holder<FaultBean> fault = new Holder<>();
    Holder<DataHandler> pagati = new Holder<>();
    Holder<String> tipoFirma = new Holder<>();
    Holder<DataHandler> rt = new Holder<>();
    pagamentiTelematiciDovutiPagati.paaSILChiediPagatiConRicevuta(request.getCodIpaEnte(), request.getPassword(), request.getIdSession(),
        request.getIdentificativoUnivocoVersamento(), request.getIdentificativoUnivocoDovuto(), fault, pagati, tipoFirma, rt);
    risposta.setFault(fault.value);
    risposta.setPagati(pagati.value);
    risposta.setTipoFirma(tipoFirma.value);
    risposta.setRt(rt.value);
    return risposta;
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediPosizioniAperte")
  @ResponsePayload
  public PaaSILChiediPosizioniAperteRisposta paaSILChiediPosizioniAperte(
      @RequestPayload PaaSILChiediPosizioniAperte request){
    systemBlockService.blockByOperationName("pa.paaSILChiediPosizioniAperte");
    return pagamentiTelematiciDovutiPagati.paaSILChiediPosizioniAperte(request);
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediStatoExportFlusso")
  @ResponsePayload
  public PaaSILChiediStatoExportFlussoRisposta paaSILChiediStatoExportFlusso(
      @RequestPayload PaaSILChiediStatoExportFlusso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILChiediStatoExportFlusso");
    return pagamentiTelematiciDovutiPagati.paaSILChiediStatoExportFlusso(request, unmarshallHeader(header, IntestazionePPT.class));
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediStatoImportFlusso")
  @ResponsePayload
  public PaaSILChiediStatoImportFlussoRisposta paaSILChiediStatoImportFlusso(
      @RequestPayload PaaSILChiediStatoImportFlusso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILChiediStatoImportFlusso");
    return pagamentiTelematiciDovutiPagati.paaSILChiediStatoImportFlusso(request, unmarshallHeader(header, IntestazionePPT.class));
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediStoricoPagamenti")
  @ResponsePayload
  public PaaSILChiediStoricoPagamentiRisposta paaSILChiediStoricoPagamenti(
      @RequestPayload PaaSILChiediStoricoPagamenti request){
    systemBlockService.blockByOperationName("pa.paaSILChiediStoricoPagamenti");
    return pagamentiTelematiciDovutiPagati.paaSILChiediStoricoPagamenti(request);
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILImportaDovuto")
  @ResponsePayload
  public PaaSILImportaDovutoRisposta paaSILImportaDovuto(
      @RequestPayload PaaSILImportaDovuto request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILImportaDovuto");
    PaaSILImportaDovutoRisposta response = pagamentiTelematiciDovutiPagati.paaSILImportaDovuto(request, unmarshallHeader(header, IntestazionePPT.class));
    LogExecutionAspect.setCustomMessage(String.format("iuv[%s] esito[%s]",
      response!=null?response.getIdentificativoUnivocoVersamento():"-",
      response!=null?response.getEsito():"-"));
    return response;
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILInviaCarrelloDovuti")
  @ResponsePayload
  public PaaSILInviaCarrelloDovutiRisposta paaSILInviaCarrelloDovuti(
      @RequestPayload PaaSILInviaCarrelloDovuti request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILInviaCarrelloDovuti");
    return pagamentiTelematiciDovutiPagati.paaSILInviaCarrelloDovuti(request, unmarshallHeader(header, IntestazionePPT.class));
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILInviaDovuti")
  @ResponsePayload
  public PaaSILInviaDovutiRisposta paaSILInviaDovuti(
      @RequestPayload PaaSILInviaDovuti request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILInviaDovuti");
    return pagamentiTelematiciDovutiPagati.paaSILInviaDovuti(request, unmarshallHeader(header, IntestazionePPT.class));
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILPrenotaExportFlusso")
  @ResponsePayload
  public PaaSILPrenotaExportFlussoRisposta paaSILPrenotaExportFlusso(
      @RequestPayload PaaSILPrenotaExportFlusso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILPrenotaExportFlusso");
    return pagamentiTelematiciDovutiPagati.paaSILPrenotaExportFlusso(request, unmarshallHeader(header, IntestazionePPT.class));
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILPrenotaExportFlussoIncrementaleConRicevuta")
  @ResponsePayload
  public PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta paaSILPrenotaExportFlussoIncrementaleConRicevuta(
      @RequestPayload PaaSILPrenotaExportFlussoIncrementaleConRicevuta request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILPrenotaExportFlussoIncrementaleConRicevuta");
    return pagamentiTelematiciDovutiPagati.paaSILPrenotaExportFlussoIncrementaleConRicevuta(request, unmarshallHeader(header, IntestazionePPT.class));
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILRegistraPagamento")
  @ResponsePayload
  public PaaSILRegistraPagamentoRisposta paaSILRegistraPagamento(
      @RequestPayload PaaSILRegistraPagamento request){
    systemBlockService.blockByOperationName("pa.paaSILRegistraPagamento");
    PaaSILRegistraPagamentoRisposta risposta = new PaaSILRegistraPagamentoRisposta();
    Holder<FaultBean> fault = new Holder<>();
    Holder<String> esito = new Holder<>();
    pagamentiTelematiciDovutiPagati.paaSILRegistraPagamento(request.getCodIpaEnte(), request.getPassword(), request.getIdentificativoUnivocoVersamento(),
        request.getCodiceContestoPagamento(), request.getSingoloImportoPagato(), request.getDataEsitoSingoloPagamento(), request.getIndiceDatiSingoloPagamento(),
        request.getIdentificativoUnivocoRiscossione(), request.getTipoIstitutoAttestante(), request.getCodiceIstitutiAttestante(), request.getDenominazioneAttestante(),
        fault, esito);
    risposta.setFault(fault.value);
    risposta.setEsito(esito.value);
    return risposta;
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILVerificaAvviso")
  @ResponsePayload
  public PaaSILVerificaAvvisoRisposta paaSILVerificaAvviso(
      @RequestPayload PaaSILVerificaAvviso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILVerificaAvviso");
    return pagamentiTelematiciDovutiPagati.paaSILVerificaAvviso(request, unmarshallHeader(header, IntestazionePPT.class));
  }

}
