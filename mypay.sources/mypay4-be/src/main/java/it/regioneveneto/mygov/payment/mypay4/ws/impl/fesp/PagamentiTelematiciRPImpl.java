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
package it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp;

import it.regioneveneto.mygov.payment.mypay4.controller.FlussoController;
import it.regioneveneto.mygov.payment.mypay4.controller.MyBoxController;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.NodoSILInviaRPRispostaException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.bind.JAXBContext;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.*;


@Service("PagamentiTelematiciRPImpl")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciRPImpl implements PagamentiTelematiciRP {

  @Autowired
  EnteService enteFespService;
  @Autowired
  FlussoSpcService flussoSpcService;
  @Autowired
  IuvService iuvService;
  @Autowired
  GiornaleService giornaleFespService;
  @Autowired
  JAXBTransformService jaxbTransformService;
  @Autowired
  NodoPagamentiTelematiciRPTService nodoPagamentiTelematiciRPTservice;
  @Autowired
  NodoInviaRPTService nodoInviaRPTService;
  @Autowired
  MessageSource messageSource;
  @Autowired
  PagamentiTelematiciRTImpl pagamentiTelematiciRT;
  @Autowired
  RptRtService rptRtService;
  @Autowired
  private EnteService enteService;
  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String propIdStazioneIntermediarioPa;
  @Value("${pa.codIpaEntePredefinito}")
  private String codIpaEnteDefault;
  @Value("${pa.pspDefaultIdentificativoCanale}")
  private String identificativoCanale;
  @Value("${pa.pspDefaultIdentificativoPsp}")
  private String identificativoPsp;
  @Value("${pa.identificativoIntermediarioPA}")
  private String identificativoErogatore;
  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;

  @Override
  public ChiediFlussoSPCRisposta chiediFlussoSPC(ChiediFlussoSPC request) {
    ChiediFlussoSPCRisposta response = new ChiediFlussoSPCRisposta();
    try {
      Constants.FLG_TIPO_FLUSSO TIPO_FLUSSO = Constants.FLG_TIPO_FLUSSO.fromString(request.getFlgTipoFlusso());
      FlussoSPC flusso = flussoSpcService.getFlussoSpcEsclStato(TIPO_FLUSSO,
          request.getIdentificativoDominio(),
          request.getCodIdentificativoFlusso(),
          request.getIdentificativoPSP(),
          Utilities.toDate(request.getDtDataOraFlusso()),
          Constants.STATO_ESECUZIONE_FLUSSO_ERRORE_CARICAMENTO);
      Ente ente = enteService.getEnteByCodFiscale(request.getIdentificativoDominio());
      if (flusso != null) {
        String type = TIPO_FLUSSO.equals(Constants.FLG_TIPO_FLUSSO.QUADRATURA) ?
                      FlussoController.FILE_TYPE_FLUSSI_QUADRATURA : FlussoController.FILE_TYPE_FLUSSI_RENDICONTAZIONE;
        String securityToken = jwtTokenUtil.generateSecurityToken(null, type + "|" + ente.getMygovEnteId() + "|" +  flusso.getDeNomeFileScaricato());
        String downloadUrl = UriComponentsBuilder
                .fromUriString(appBeAbsolutePath + MyBoxController.DOWNLOAD_FLUSSO_PATH + File.separator + ente.getMygovEnteId())
                .queryParam("type", type)
                .queryParam("securityToken", URLEncoder.encode(securityToken, StandardCharsets.UTF_8))
                .queryParam("filename", flusso.getDeNomeFileScaricato())
                .encode()
                .toUriString();
        response.setDownloadURL(downloadUrl);
      } else
        response.setDownloadURL("");
      response.setStato(flusso != null ? Constants.ESITO.OK.getValue() : Constants.ESITO.KO.getValue());
    } catch (Exception e) {
      log.error("NODO_REGIONALE_CHIEDI_FLUSSO_SPC: " + e.getMessage(), e);
      response.setFault(VerificationUtils.getFespFaultBean("NODO_REGIONALE_CHIEDI_FLUSSO_SPC",
          PAA_SYSTEM_ERROR, e.getMessage(), e.getMessage(), 1));
    } finally {
      return response;
    }
  }

  @Override
  public ChiediFlussoSPCPageRisposta chiediFlussoSPCPage(ChiediFlussoSPCPage request) {
    ChiediFlussoSPCPageRisposta response = new ChiediFlussoSPCPageRisposta();
    try {
      Constants.FLG_TIPO_FLUSSO TIPO_FLUSSO = Constants.FLG_TIPO_FLUSSO.fromString(request.getFlgTipoFlusso());
      List<FlussoSPC> flussi = flussoSpcService.getFlussoSpc(TIPO_FLUSSO,
          request.getIdentificativoDominio(),
          request.getIdentificativoPSP(),
          request.getDateFrom().toGregorianCalendar().toZonedDateTime().toLocalDate(),
          request.getDateTo().toGregorianCalendar().toZonedDateTime().toLocalDate(),
          request.getFlgProdOrDisp());
      response.getFlussoSPCs().addAll(flussi);
    } catch (Exception e) {
      log.error("NODO_REGIONALE_CHIEDI_FLUSSO_SPC_PAGE: " + e.getMessage(), e);
      response.setFault(VerificationUtils.getFespFaultBean("NODO_REGIONALE_CHIEDI_FLUSSO_SPC_PAGE",
          PAA_SYSTEM_ERROR, e.getMessage(), e.getMessage(), 1));
    } finally {
      return response;
    }
  }

  @Override
  public ChiediListaFlussiSPCRisposta chiediListaFlussiSPC(ChiediListaFlussiSPC request) {
    ChiediListaFlussiSPCRisposta response = new ChiediListaFlussiSPCRisposta();
    try {
      Constants.FLG_TIPO_FLUSSO TIPO_FLUSSO = Constants.FLG_TIPO_FLUSSO.fromString(request.getFlgTipoFlusso());
      List<FlussoSPC> flussi = flussoSpcService.getFlussoSpc(TIPO_FLUSSO,
          request.getIdentificativoDominio(),
          request.getIdentificativoPSP(),
          request.getDateFrom().toGregorianCalendar().toZonedDateTime().toLocalDate(),
          request.getDateTo().toGregorianCalendar().toZonedDateTime().toLocalDate(),
          "D"); // Force to search by dtCreazione.
      response.getFlussoSPCs().addAll(flussi);
      response.setTotalRecords(flussi.size());
    } catch (Exception e) {
      log.error("NODO_REGIONALE_CHIEDI_LISTA_FLUSSO_SPC: " + e.getMessage(), e);
      response.setFault(VerificationUtils.getFespFaultBean("NODO_REGIONALE_CHIEDI_LISTA_FLUSSO_SPC",
          PAA_SYSTEM_ERROR, e.getMessage(), e.getMessage(), 1));
    } finally {
      return response;
    }
  }

  @Override
  public NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(NodoSILChiediCopiaEsito richiesta){
    NodoSILChiediCopiaEsitoRisposta risposta = new NodoSILChiediCopiaEsitoRisposta();
    String identificativoDominio = richiesta.getIdentificativoDominio();
    try {
      Ente ente = enteFespService.getEnteByCodFiscale(identificativoDominio);
      if (ente == null) {
        String msg = "nodoSILChiediCopiaEsito: Nessun Ente per codice fiscale [ " + identificativoDominio + " ]";
        log.error(msg);
        FaultBean fb = VerificationUtils.getFespFaultBean(PAA_SYSTEM_ERROR, PAA_CHIEDI_COPIA_ESITO_ENTE_NON_PRESENTE, msg, msg, 1);
        risposta.setFault(fb);
        return risposta;
      }
    }
    catch (Exception e) {
      String msg = "nodoSILChiediCopiaEsito: Nessun Ente per codice fiscale [ " + identificativoDominio + " ]";
      log.error(msg);
      FaultBean fb = VerificationUtils.getFespFaultBean(PAA_SYSTEM_ERROR, PAA_CHIEDI_COPIA_ESITO_ENTE_NON_PRESENTE, msg, msg, 1);
      risposta.setFault(fb);
      return risposta;
    }

    try {
      risposta = nodoInviaRPTService.nodoSILChiediCopiaEsito(richiesta);
      return risposta;
    }
    catch (Exception e) {
      String msg = String.format("Errore nel recupero dell'esito per identificativo dominio [ {} ], IUV [ {} ] e codice contesto pagamento [ {} ]",
          identificativoDominio, richiesta.getIdentificativoUnivocoVersamento(), richiesta.getCodiceContestoPagamento());
      log.error(msg, e);
      FaultBean fb = VerificationUtils.getFespFaultBean(PAA_SYSTEM_ERROR, PAA_CHIEDI_COPIA_ESITO_ERRORE, msg, msg, 1);
      risposta.setFault(fb);
      return risposta;
    }
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public NodoSILInviaRPRisposta nodoSILInviaRP(NodoSILInviaRP bodyrichiesta, it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT header) {

    log.info("Executing operation nodoSILInviaRP");

    /*
     * LOG GIORNALE DEGLI EVENTI
     */
    Date dataOraEvento;
    String identificativoDominio;
    String identificativoUnivocoVersamento;
    String codiceContestoPagamento;
    String identificativoPrestatoreServiziPagamento;
    String tipoVersamento;
    String componente;
    String categoriaEvento;
    String tipoEvento;
    String sottoTipoEvento;
    String identificativoFruitore;
    String identificativoErogatore;
    String identificativoStazioneIntermediarioPa;
    String canalePagamento;
    String xmlBodyString;
    String xmlHeaderString;
    JAXBContext context;
    String parametriSpecificiInterfaccia;
    String esitoReq;
    try {
      dataOraEvento = new Date();
      identificativoDominio = header.getIdentificativoDominio();
      identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
      codiceContestoPagamento = header.getCodiceContestoPagamento();
      identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
      tipoVersamento = null;
      componente = Constants.COMPONENTE.FESP.toString();
      categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
      tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaRP.toString();
      sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString();

      identificativoFruitore = header.getIdentificativoDominio();
      identificativoErogatore = propIdStazioneIntermediarioPa;
      identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      canalePagamento = bodyrichiesta.getIdentificativoCanale();

      xmlBodyString = jaxbTransformService.marshalling(bodyrichiesta, NodoSILInviaRP.class);
      xmlHeaderString = jaxbTransformService.marshalling(header, it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT.class);

      parametriSpecificiInterfaccia = xmlBodyString + xmlHeaderString;

      esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

      giornaleFespService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
          identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
          identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
    }
    catch (Exception e1) {
      log.warn("nodoSILInviaRP REQUEST impossibile inserire nel giornale degli eventi", e1);
    }

    //BEGIN EXECUTE

    NodoSILInviaRPRisposta _rispostaRP;

    try {
      _rispostaRP = nodoInviaRPTService.nodoSILInviaRP(bodyrichiesta, header);
    }
    catch (NodoSILInviaRPRispostaException e) {
      _rispostaRP = e.getNodoSILInviaRPRisposta();
    }

    //END EXECUTE

    /*
     * LOG GIORNALE DEGLI EVENTI
     */
    try {
      dataOraEvento = new Date();
      identificativoDominio = header.getIdentificativoDominio();
      identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
      codiceContestoPagamento = header.getCodiceContestoPagamento();
      identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
      tipoVersamento = null;
      componente = Constants.COMPONENTE.FESP.toString();
      categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
      tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaRP.toString();
      sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();

      identificativoFruitore = header.getIdentificativoDominio();
      identificativoErogatore = propIdStazioneIntermediarioPa;
      identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      canalePagamento = bodyrichiesta.getIdentificativoCanale();

      xmlBodyString = jaxbTransformService.marshalling(_rispostaRP, NodoSILInviaRPRisposta.class);

      parametriSpecificiInterfaccia = xmlBodyString;

      esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

      giornaleFespService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
          identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
          identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
    }
    catch (Exception e) {
      log.warn("nodoSILInviaRP RESPONSE impossibile inserire nel giornale degli eventi", e);
    }

    log.info("Ended execution operation nodoSILInviaRP");

    return _rispostaRP;
  }


  @Override
  public NodoSILChiediIUVRisposta nodoSILChiediIUV(NodoSILChiediIUV request) {
    NodoSILChiediIUVRisposta response = new NodoSILChiediIUVRisposta();
    try {
      String iuv = iuvService.generateIuv(
          request.getIdentificativoDominio(),
          request.getTipoVersamento(),
          request.getTipoGeneratore(),
          request.getImporto(),
          request.getAuxDigit());
      response.setIdentificativoUnivocoVersamento(iuv);
    } catch (Exception e){
      log.error("nodoSILChiediIUV", e);
      response.setFault(new FaultBean());
      response.getFault().setFaultCode(PAA_SYSTEM_ERROR);
      response.getFault().setDescription(e.getMessage());
    }
    return response;
  }

  public NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP request) {
    NodoSILInviaCarrelloRPRisposta response = new NodoSILInviaCarrelloRPRisposta();
    List<ElementoRP> rpList = request.getListaRP().getElementoRPs();
    try {
      rpList.forEach(elementoRP -> {
        String xmlRequest = new String(elementoRP.getRp(), StandardCharsets.UTF_8);
        log.debug(xmlRequest);
        try {
          giornaleFespService.registraEvento(new Date(), elementoRP.getIdentificativoDominio(),
              elementoRP.getIdentificativoUnivocoVersamento(), elementoRP.getCodiceContestoPagamento(),
              identificativoPsp, Constants.ALL_PAGAMENTI, Constants.COMPONENTE.FESP.toString(),
              Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaCarrelloRP.toString(),
              Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
              codIpaEnteDefault, identificativoErogatore,
              propIdStazioneIntermediarioPa, identificativoCanale,
              xmlRequest, Constants.GIORNALE_ESITO_EVENTO.OK.toString());
        } catch (Exception e) {
          log.warn("nodoSILInviaCarrelloRP [REQUEST] impossible to insert in the event log", e);
        }
      });
      response =  nodoPagamentiTelematiciRPTservice.nodoSILInviaCarrelloRP(request);

    } catch (Exception ex){
      response.setEsito(FaultCodeConstants.ESITO_KO);
      FaultBean faultBean = new FaultBean();
      faultBean.setId("NODO_INVIA_CARRELLO");
      faultBean.setFaultCode(PAA_SYSTEM_ERROR);
      faultBean.setDescription(ex.getMessage());
      faultBean.setFaultString(messageSource.getMessage("pa.errore.invioRPT", null, Locale.ITALY));
      response.setFault(faultBean);
      log.error(jaxbTransformService.marshalling(response, NodoSILInviaCarrelloRPRisposta.class), ex.getMessage());
    } finally {
      String xmlEsito = jaxbTransformService.marshalling(response, NodoSILInviaCarrelloRPRisposta.class);
      final String esito = response.getEsito();
      rpList.forEach(elementoRP -> {
        try {
          giornaleFespService.registraEvento(new Date(), elementoRP.getIdentificativoDominio(),
              elementoRP.getIdentificativoUnivocoVersamento(), elementoRP.getCodiceContestoPagamento(),
              identificativoPsp, Constants.ALL_PAGAMENTI, Constants.COMPONENTE.FESP.toString(),
              Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaCarrelloRP.toString(),
              Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
              codIpaEnteDefault, identificativoErogatore,
              propIdStazioneIntermediarioPa, identificativoCanale,
              xmlEsito, esito);
        } catch (Exception e) {
          log.warn("nodoSILInviaCarrelloRP [RESPONSE] impossible to insert in the event log", e);
        }
      });
    }
    return response;
  }

  public NodoSILRichiediRTRisposta nodoSILRichiediRT(NodoSILRichiediRT request) {
    NodoSILRichiediRTRisposta response = new NodoSILRichiediRTRisposta();
    try{
      try {
        giornaleFespService.registraEvento(new Date(), request.getIdentificativoDominio(),
            request.getIdentificativoUnivocoVersamento(), request.getCodiceContestoPagamento(),
            identificativoPsp, Constants.ALL_PAGAMENTI, Constants.COMPONENTE.FESP.toString(),
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILRichiediRT.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
            codIpaEnteDefault, identificativoErogatore,
            propIdStazioneIntermediarioPa, identificativoCanale,
            null, Constants.GIORNALE_ESITO_EVENTO.OK.toString());
      } catch (Exception e) {
        log.warn("nodoSILRichiediRT [REQUEST] impossible to insert in the event log", e);
      }

      RptRt rpt = rptRtService.getRPTByIdDominioIuvCdContestoPagamento(request.getIdentificativoDominio(), request.getIdentificativoUnivocoVersamento(), request.getCodiceContestoPagamento());
      if(rpt==null) {
        response.setEsito(FaultCodeConstants.ESITO_KO);
        FaultBean faultBean = new FaultBean();
        faultBean.setId("NODO_RICHIEDI_RT");
        faultBean.setFaultCode(FaultCodeConstants.PAA_RPT_NON_PRESENTE);
        faultBean.setDescription("RPT non presente su FESP");
        faultBean.setFaultString(faultBean.getDescription());
        response.setFault(faultBean);
      } else if(StringUtils.isNotBlank(rpt.getCodRtInviartEsito())) {
        response.setEsito(FaultCodeConstants.ESITO_KO);
        FaultBean faultBean = new FaultBean();
        faultBean.setId("NODO_RICHIEDI_RT");
        faultBean.setFaultCode(FaultCodeConstants.PAA_RT_GIA_PRESENTE);
        faultBean.setDescription("RT gia presente su FESP");
        faultBean.setFaultString(faultBean.getDescription());
        response.setFault(faultBean);
      } else {
        Boolean rtReceived = pagamentiTelematiciRT.askRT(rpt);
        response.setEsito(Objects.equals(rtReceived, Boolean.TRUE) ? FaultCodeConstants.ESITO_OK : FaultCodeConstants.ESITO_KO);
      }
    } catch (Exception ex){
      log.error("system error in nodoSILRichiediRT", ex);
      response.setEsito(FaultCodeConstants.ESITO_KO);
      FaultBean faultBean = new FaultBean();
      faultBean.setId("NODO_RICHIEDI_RT");
      faultBean.setFaultCode(PAA_SYSTEM_ERROR);
      faultBean.setDescription(ex.toString());
      faultBean.setFaultString("Errore durante il recupero della RT");
      response.setFault(faultBean);
    } finally {
      final String esito = response.getEsito();
      try {
        giornaleFespService.registraEvento(new Date(), request.getIdentificativoDominio(),
            request.getIdentificativoUnivocoVersamento(), request.getCodiceContestoPagamento(),
            identificativoPsp, Constants.ALL_PAGAMENTI, Constants.COMPONENTE.FESP.toString(),
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILRichiediRT.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
            codIpaEnteDefault, identificativoErogatore,
            propIdStazioneIntermediarioPa, identificativoCanale,
            null, esito);
      } catch (Exception e) {
        log.warn("nodoSILRichiediRT [RESPONSE] impossible to insert in the event log", e);
      }

    }
    return response;
  }
}
