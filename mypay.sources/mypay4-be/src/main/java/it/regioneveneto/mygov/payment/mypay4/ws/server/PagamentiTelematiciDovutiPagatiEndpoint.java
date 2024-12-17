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
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciDovutiPagatiImpl;
import it.veneto.regione.pagamenti.ente.*;
import it.veneto.regione.pagamenti.ente.ppthead.IntestazionePPT;
import it.veneto.regione.schemas._2012.pagamenti.ente.Versamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;
import java.util.Optional;

@Endpoint
@ConditionalOnWebApplication
public class PagamentiTelematiciDovutiPagatiEndpoint extends BaseEndpoint {
  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/ente/";
  public static final String NAME = "PagamentiTelematiciDovutiPagati";

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  @Autowired
  @Qualifier("PagamentiTelematiciDovutiPagatiImpl")
  private PagamentiTelematiciDovutiPagatiImpl pagamentiTelematiciDovutiPagati;

  @Autowired
  private SystemBlockService systemBlockService;

  @Autowired
  private GiornaleService giornaleCommonService;

  @Autowired
  private EnteService enteService;

  @Autowired
  private JAXBTransformService jaxbTransformService;

  @Autowired
  private DovutoService dovutoService;

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILAutorizzaImportFlusso")
  @ResponsePayload
  public PaaSILAutorizzaImportFlussoRisposta paaSILAutorizzaImportFlusso(
      @RequestPayload PaaSILAutorizzaImportFlusso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILAutorizzaImportFlusso");

    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    String idDominio = Optional.ofNullable(intestazionePPT.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILAutorizzaImportFlusso.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILAutorizzaImportFlusso(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediEsitoCarrelloDovuti")
  @ResponsePayload
  public PaaSILChiediEsitoCarrelloDovutiRisposta paaSILChiediEsitoCarrelloDovuti(
      @RequestPayload PaaSILChiediEsitoCarrelloDovuti request){
    systemBlockService.blockByOperationName("pa.paaSILChiediEsitoCarrelloDovuti");

    String idDominio = Optional.ofNullable(request.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediEsitoCarrelloDovuti.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> {
        PaaSILChiediEsitoCarrelloDovutiRisposta risposta = new PaaSILChiediEsitoCarrelloDovutiRisposta();
        Holder<FaultBean> fault = new Holder<>();
        Holder<ListaCarrelli> listaCarrelli = new Holder<>();
        pagamentiTelematiciDovutiPagati.paaSILChiediEsitoCarrelloDovuti(request.getCodIpaEnte(), request.getPassword(),
          request.getIdSessionCarrello(), fault, listaCarrelli);
        risposta.setFault(fault.value);
        risposta.setListaCarrelli(listaCarrelli.value);
        return risposta;
      },
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediPagati")
  @ResponsePayload
  public PaaSILChiediPagatiRisposta paaSILChiediPagati(
      @RequestPayload PaaSILChiediPagati request){
    systemBlockService.blockByOperationName("pa.paaSILChiediPagati");
    String idDominio = Optional.ofNullable(request.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagati.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> {
        PaaSILChiediPagatiRisposta r = new PaaSILChiediPagatiRisposta();
        Holder<FaultBean> fault = new Holder<>();
        Holder<DataHandler> pagati = new Holder<>();
        pagamentiTelematiciDovutiPagati.paaSILChiediPagati(request.getCodIpaEnte(), request.getPassword(), request.getIdSession(), fault, pagati);
        r.setFault(fault.value);
        r.setPagati(pagati.value);
        return r;
      },
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediPagatiConRicevuta")
  @ResponsePayload
  public PaaSILChiediPagatiConRicevutaRisposta paaSILChiediPagatiConRicevuta(
      @RequestPayload PaaSILChiediPagatiConRicevuta request){
    systemBlockService.blockByOperationName("pa.paaSILChiediPagatiConRicevuta");

    String idDominio = Optional.ofNullable(request.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      request.getIdentificativoUnivocoVersamento(),
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> {
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
      },
      OutcomeHelper::getOutcome
    );
  }


  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediPosizioniAperte")
  @ResponsePayload
  public PaaSILChiediPosizioniAperteRisposta paaSILChiediPosizioniAperte(
      @RequestPayload PaaSILChiediPosizioniAperte request){
    systemBlockService.blockByOperationName("pa.paaSILChiediPosizioniAperte");
    String idDominio = Optional.ofNullable(request.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPosizioniAperte.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILChiediPosizioniAperte(request),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediStatoExportFlusso")
  @ResponsePayload
  public PaaSILChiediStatoExportFlussoRisposta paaSILChiediStatoExportFlusso(
      @RequestPayload PaaSILChiediStatoExportFlusso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILChiediStatoExportFlusso");

    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    String idDominio = Optional.ofNullable(intestazionePPT.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediStatoExportFlusso.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILChiediStatoExportFlusso(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediStatoExportFlussoScaduti")
  @ResponsePayload
  public PaaSILChiediStatoExportFlussoScadutiRisposta paaSILChiediStatoExportFlussoScaduti(
          @RequestPayload PaaSILChiediStatoExportFlussoScaduti request,
          @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILChiediStatoExportFlussoScaduti");
    return giornaleCommonService.wrapRecordSoapServerEvent(
            Constants.GIORNALE_MODULO.PA,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            () -> pagamentiTelematiciDovutiPagati.paaSILChiediStatoExportFlussoScaduti(request, unmarshallHeader(header, IntestazionePPT.class)),
            OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediStatoImportFlusso")
  @ResponsePayload
  public PaaSILChiediStatoImportFlussoRisposta paaSILChiediStatoImportFlusso(
      @RequestPayload PaaSILChiediStatoImportFlusso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILChiediStatoImportFlusso");
    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    String idDominio = Optional.ofNullable(intestazionePPT.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediStatoImportFlusso.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILChiediStatoImportFlusso(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediStoricoPagamenti")
  @ResponsePayload
  public PaaSILChiediStoricoPagamentiRisposta paaSILChiediStoricoPagamenti(
      @RequestPayload PaaSILChiediStoricoPagamenti request){
    systemBlockService.blockByOperationName("pa.paaSILChiediStoricoPagamenti");
    String idDominio = Optional.ofNullable(request.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediStoricoPagamenti.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILChiediStoricoPagamenti(request),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILImportaDovuto")
  @ResponsePayload
  public PaaSILImportaDovutoRisposta paaSILImportaDovuto(
      @RequestPayload PaaSILImportaDovuto request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILImportaDovuto");

    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    String idDominio = Optional.ofNullable(intestazionePPT.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);

    //workaround to log the iuv in giornale
    String iuv = null;
    try {
      Versamento versamento = jaxbTransformService.unmarshalling(request.getDovuto(), Versamento.class,"/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
      iuv = versamento.getDatiVersamento().getIdentificativoUnivocoVersamento();
    } catch (Exception e) {
      //ignore the exception
    }


    PaaSILImportaDovutoRisposta response = giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      iuv,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILImportaDovuto.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILImportaDovuto(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );

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

    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    String idDominio = Optional.ofNullable(intestazionePPT.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILInviaCarrelloDovuti.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILInviaCarrelloDovuti(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILInviaDovuti")
  @ResponsePayload
  public PaaSILInviaDovutiRisposta paaSILInviaDovuti(
      @RequestPayload PaaSILInviaDovuti request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILInviaDovuti");

    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    String idDominio = Optional.ofNullable(intestazionePPT.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILInviaDovuti.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILInviaDovuti(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILPrenotaExportFlusso")
  @ResponsePayload
  public PaaSILPrenotaExportFlussoRisposta paaSILPrenotaExportFlusso(
      @RequestPayload PaaSILPrenotaExportFlusso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILPrenotaExportFlusso");
    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    String idDominio = Optional.ofNullable(intestazionePPT.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILPrenotaExportFlusso.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILPrenotaExportFlusso(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILPrenotaExportFlussoScaduti")
  @ResponsePayload
  public PaaSILPrenotaExportFlussoScadutiRisposta paaSILPrenotaExportFlussoScaduti(
          @RequestPayload PaaSILPrenotaExportFlussoScaduti request,
          @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILPrenotaExportFlussoScaduti");
    return giornaleCommonService.wrapRecordSoapServerEvent(
            Constants.GIORNALE_MODULO.PA,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            () -> pagamentiTelematiciDovutiPagati.paaSILPrenotaExportFlussoScaduti(request, unmarshallHeader(header, IntestazionePPT.class)),
            OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILPrenotaExportFlussoIncrementaleConRicevuta")
  @ResponsePayload
  public PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta paaSILPrenotaExportFlussoIncrementaleConRicevuta(
      @RequestPayload PaaSILPrenotaExportFlussoIncrementaleConRicevuta request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILPrenotaExportFlussoIncrementaleConRicevuta");
    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    String idDominio = Optional.ofNullable(intestazionePPT.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILPrenotaExportFlussoIncrRicev.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILPrenotaExportFlussoIncrementaleConRicevuta(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILRegistraPagamento")
  @ResponsePayload
  public PaaSILRegistraPagamentoRisposta paaSILRegistraPagamento(
      @RequestPayload PaaSILRegistraPagamento request){
    systemBlockService.blockByOperationName("pa.paaSILRegistraPagamento");

    String idDominio = Optional.ofNullable(request.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      request.getIdentificativoUnivocoVersamento(),
      request.getCodiceContestoPagamento(),
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILRegistraPagamento.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> {
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
      },
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILVerificaAvviso")
  @ResponsePayload
  public PaaSILVerificaAvvisoRisposta paaSILVerificaAvviso(
      @RequestPayload PaaSILVerificaAvviso request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/ente/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("pa.paaSILVerificaAvviso");
    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    String idDominio = Optional.ofNullable(intestazionePPT.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      request.getIdentificativoUnivocoVersamento(),
      null,
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILVerificaAvviso.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciDovutiPagati.paaSILVerificaAvviso(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

}
