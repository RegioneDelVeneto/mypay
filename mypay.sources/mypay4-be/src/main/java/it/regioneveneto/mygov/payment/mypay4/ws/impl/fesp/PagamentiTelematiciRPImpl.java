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
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.RollbackException;
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

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.*;


@Service("PagamentiTelematiciRPImpl")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciRPImpl implements PagamentiTelematiciRP {

  public static final String NODO_RICHIEDI_RT = "NODO_RICHIEDI_RT";
  @Autowired
  private EnteService enteFespService;
  @Autowired
  private FlussoSpcService flussoSpcService;
  @Autowired
  private IuvService iuvService;
  @Autowired
  private JAXBTransformService jaxbTransformService;
  @Autowired
  private NodoPagamentiTelematiciRPTService nodoPagamentiTelematiciRPTservice;
  @Autowired
  private NodoInviaRPTService nodoInviaRPTService;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private PagamentiTelematiciRTImpl pagamentiTelematiciRT;
  @Autowired
  private RptRtService rptRtService;
  @Autowired
  private EnteService enteService;
  @Autowired
  private JwtTokenUtil jwtTokenUtil;
  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;

  @Override
  public ChiediFlussoSPCRisposta chiediFlussoSPC(ChiediFlussoSPC request) {
    ChiediFlussoSPCRisposta response = new ChiediFlussoSPCRisposta();
    try {
      Constants.FLG_TIPO_FLUSSO tipoFlusso = Constants.FLG_TIPO_FLUSSO.fromString(request.getFlgTipoFlusso());
      FlussoSPC flusso = flussoSpcService.getFlussoSpcEsclStato(tipoFlusso,
          request.getIdentificativoDominio(),
          request.getCodIdentificativoFlusso(),
          request.getIdentificativoPSP(),
          Utilities.toDate(request.getDtDataOraFlusso()),
          Constants.STATO_ESECUZIONE_FLUSSO_ERRORE_CARICAMENTO);
      Ente ente = enteService.getEnteByCodFiscale(request.getIdentificativoDominio());
      if (flusso != null) {
        String type = tipoFlusso.equals(Constants.FLG_TIPO_FLUSSO.QUADRATURA) ?
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
    }
    return response;
  }

  @Override
  public ChiediFlussoSPCPageRisposta chiediFlussoSPCPage(ChiediFlussoSPCPage request) {
    ChiediFlussoSPCPageRisposta response = new ChiediFlussoSPCPageRisposta();
    try {
      Constants.FLG_TIPO_FLUSSO tipoFlusso = Constants.FLG_TIPO_FLUSSO.fromString(request.getFlgTipoFlusso());
      List<FlussoSPC> flussi = flussoSpcService.getFlussoSpc(tipoFlusso,
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
    }
    return response;
  }

  @Override
  public ChiediListaFlussiSPCRisposta chiediListaFlussiSPC(ChiediListaFlussiSPC request) {
    ChiediListaFlussiSPCRisposta response = new ChiediListaFlussiSPCRisposta();
    try {
      Constants.FLG_TIPO_FLUSSO tipoFlusso = Constants.FLG_TIPO_FLUSSO.fromString(request.getFlgTipoFlusso());
      List<FlussoSPC> flussi = flussoSpcService.getFlussoSpc(tipoFlusso,
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
    }
    return response;
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
      String msg = String.format("Errore nel recupero dell'esito per identificativo dominio [ %s ], IUV [ %s ] e codice contesto pagamento [ %s ]",
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
    NodoSILInviaRPRisposta rispostaRP;

    try {
      rispostaRP = nodoInviaRPTService.nodoSILInviaRP(bodyrichiesta, header);
    }
    catch (NodoSILInviaRPRispostaException e) {
      log.error("error on nodoSILInviaRP", e);
      rispostaRP = e.getNodoSILInviaRPRisposta();
    }

    log.info("Ended execution operation nodoSILInviaRP");
    return rispostaRP;
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

  @Override
  public NodoSILChiediCCPRisposta nodoSILChiediCCP(NodoSILChiediCCP request) {
    NodoSILChiediCCPRisposta response = new NodoSILChiediCCPRisposta();
    try {
      String ccp = rptRtService.generateCCP(request.getIdDominio(), request.getIuv());
      response.setCcp(ccp);
    } catch (Exception e){
      log.error("nodoSILChiediCCP", e);
      response.setFault(new FaultBean());
      response.getFault().setFaultCode(PAA_SYSTEM_ERROR);
      response.getFault().setDescription(e.getMessage());
    }
    return response;
  }

  public NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP request) {
    NodoSILInviaCarrelloRPRisposta response;
    try {
      response =  nodoPagamentiTelematiciRPTservice.nodoSILInviaCarrelloRP(request);
    } catch (RollbackException re){
      //just rethrow it: we want rollback to occur
      throw re;
    } catch (Exception ex) {
      response = new NodoSILInviaCarrelloRPRisposta();
      response.setEsito(FaultCodeConstants.ESITO_KO);
      FaultBean faultBean = new FaultBean();
      faultBean.setId("NODO_INVIA_CARRELLO");
      faultBean.setFaultCode(PAA_SYSTEM_ERROR);
      faultBean.setDescription(ex.getMessage());
      faultBean.setFaultString(messageSource.getMessage("pa.errore.invioRPT", null, Locale.ITALY));
      response.setFault(faultBean);
      log.error(jaxbTransformService.marshalling(response, NodoSILInviaCarrelloRPRisposta.class), ex.getMessage());
    }
    return response;
  }

  public NodoSILRichiediRTRisposta nodoSILRichiediRT(NodoSILRichiediRT request) {
    NodoSILRichiediRTRisposta response = new NodoSILRichiediRTRisposta();
    try{
      RptRt rpt = rptRtService.getRPTByIdDominioIuvCdContestoPagamento(request.getIdentificativoDominio(), request.getIdentificativoUnivocoVersamento(), request.getCodiceContestoPagamento());
      if(rpt==null) {
        response.setEsito(FaultCodeConstants.ESITO_KO);
        FaultBean faultBean = new FaultBean();
        faultBean.setId(NODO_RICHIEDI_RT);
        faultBean.setFaultCode(FaultCodeConstants.PAA_RPT_NON_PRESENTE);
        faultBean.setDescription("RPT non presente su FESP");
        faultBean.setFaultString(faultBean.getDescription());
        response.setFault(faultBean);
      } else if(StringUtils.isNotBlank(rpt.getCodRtInviartEsito())) {
        response.setEsito(FaultCodeConstants.ESITO_KO);
        FaultBean faultBean = new FaultBean();
        faultBean.setId(NODO_RICHIEDI_RT);
        faultBean.setFaultCode(FaultCodeConstants.PAA_RT_GIA_PRESENTE);
        faultBean.setDescription("RT gia presente su FESP");
        faultBean.setFaultString(faultBean.getDescription());
        response.setFault(faultBean);
      } else {
        Boolean rtReceived = pagamentiTelematiciRT.askRT(rpt);
        if(Objects.equals(rtReceived, Boolean.TRUE))
          response.setEsito(FaultCodeConstants.ESITO_OK);
        else if(Objects.equals(rtReceived, Boolean.FALSE))
          response.setEsito(FaultCodeConstants.ESITO_KO);
        else {
          //if rtReceived is null, it's an unmanaged exception
          throw new MyPayException("error retrieving RT");
        }
      }
    } catch (Exception ex){
      log.error("system error in nodoSILRichiediRT", ex);
      response.setEsito(FaultCodeConstants.ESITO_KO);
      FaultBean faultBean = new FaultBean();
      faultBean.setId(NODO_RICHIEDI_RT);
      faultBean.setFaultCode(PAA_SYSTEM_ERROR);
      faultBean.setDescription(ex.toString());
      faultBean.setFaultString("Errore durante il recupero della RT");
      response.setFault(faultBean);
    }
    return response;
  }
}
