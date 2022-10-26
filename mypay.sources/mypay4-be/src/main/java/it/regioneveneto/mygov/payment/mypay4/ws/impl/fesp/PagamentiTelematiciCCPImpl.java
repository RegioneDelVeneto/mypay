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

import gov.telematici.pagamenti.ws.FaultBean;
import gov.telematici.pagamenti.ws.ObjectFactory;
import gov.telematici.pagamenti.ws.PaaTipoDatiPagamentoPSP;
import gov.telematici.pagamenti.ws.*;
import gov.telematici.pagamenti.ws.ppthead.IntestazionePPT;
import it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario;
import it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaG;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersG;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.InviaRptEvent;
import it.regioneveneto.mygov.payment.mypay4.exception.WSFaultResponseWrapperException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.AttivaRptE;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.common.AppErrorService;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.AttivaRptService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.client.PagamentiTelematiciCCPPaClient;
import it.regioneveneto.mygov.payment.mypay4.ws.client.PagamentiTelematiciEsterniCCPClient;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciCCP;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciCCPPaImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.pa.PaaTipoDatiPagamentoPA;
import it.veneto.regione.pagamenti.pa.*;
import it.veneto.regione.schemas._2012.pagamenti.CtSoggettoPagatore;
import it.veneto.regione.schemas._2012.pagamenti.CtSoggettoVersante;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Service("PagamentiTelematiciCCPFespImpl")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciCCPImpl implements PagamentiTelematiciCCP {

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String nodoRegionaleFespIdentificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String nodoRegionaleFespIdentificativoStazioneIntermediarioPA;

  @Autowired
  GiornaleService giornaleFespService;

  @Autowired
  EnteService enteFespService;

  @Autowired
  JAXBTransformService jaxbTransformService;

  @Autowired
  AttivaRptService attivaRptService;

  @Autowired
  PagamentiTelematiciCCPPaClient pagamentiTelematiciCCPPaPaClient;

  @Autowired
  PagamentiTelematiciCCPPaImpl pagamentiTelematiciCCPPaPaImpl;

  @Autowired
  PagamentiTelematiciEsterniCCPClient pagamentiTelematiciEsterniCCPClient;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  private AppErrorService appErrorService;

  @Override
  public PaaVerificaRPTRisposta paaVerificaRPT(PaaVerificaRPT bodyrichiesta, gov.telematici.pagamenti.ws.ppthead.IntestazionePPT header) {
    try {
      String xmlBodyString = jaxbTransformService.marshalling(bodyrichiesta, PaaVerificaRPT.class);
      String xmlHeaderString = jaxbTransformService.marshalling(header, IntestazionePPT.class);
      giornaleFespService.registraEvento(
          null,
          header.getIdentificativoDominio(),
          header.getIdentificativoUnivocoVersamento(),
          header.getCodiceContestoPagamento(),
          bodyrichiesta.getIdentificativoPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.paaVerificaRPT.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
          Constants.NODO_DEI_PAGAMENTI_SPC,
          header.getIdentificativoStazioneIntermediarioPA(),
          header.getIdentificativoStazioneIntermediarioPA(),
          null,
          xmlHeaderString + xmlBodyString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("paaVerificaRPT [REQUEST] impossible to insert in the event log", e);
    }

    PaaVerificaRPTRisposta paaVerificaRPTRisposta = new PaaVerificaRPTRisposta();
    paaVerificaRPTRisposta.setPaaVerificaRPTRisposta(new EsitoVerificaRPT());

    EsitoVerificaRPT esitoVerifica = paaVerificaRPTRisposta.getPaaVerificaRPTRisposta();

    if (!nodoRegionaleFespIdentificativoIntermediarioPA.equalsIgnoreCase(header.getIdentificativoIntermediarioPA())
      || !nodoRegionaleFespIdentificativoStazioneIntermediarioPA.equalsIgnoreCase(header.getIdentificativoStazioneIntermediarioPA())) {

      log.warn(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [Identificativo Intermediario PA diverso da regione veneto]");
      esitoVerifica.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
      FaultBean faultBean = new FaultBean();
      faultBean.setFaultCode(FaultCodeConstants.PAA_VERIFICA_RPT_INTERMEDIARIO_SCONOSCIUTO);
      faultBean.setFaultString("Errore PAA_VERIFICA_RPT: Intermediario sconosciuto");
      faultBean.setDescription("Errore PAA_VERIFICA_RPT: Intermediario diverso da regione veneto");
      faultBean.setId(header.getIdentificativoDominio());
      faultBean.setSerial(0);
      esitoVerifica.setFault(faultBean);

      try {
        String xmlString = jaxbTransformService.marshalling(paaVerificaRPTRisposta, PaaVerificaRPTRisposta.class);
        giornaleFespService.registraEvento(
          null,
          header.getIdentificativoDominio(),
          header.getIdentificativoUnivocoVersamento(),
          header.getCodiceContestoPagamento(),
          bodyrichiesta.getIdentificativoPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.paaVerificaRPT.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
          Constants.NODO_DEI_PAGAMENTI_SPC,
          header.getIdentificativoStazioneIntermediarioPA(),
          header.getIdentificativoStazioneIntermediarioPA(),
          null,
          xmlString,
          esitoVerifica.getEsito()
        );
      } catch (Exception e) {
        log.warn("paaSILVerificaRPT RESPONSE impossible to insert in the event log", e);
      }

      return paaVerificaRPTRisposta;
    }

    try{
      // PREPARAZIONE DELLA RICHIESTA VERIFICA VERSO PA
      var headerVerificaRPTToCallPa = buildHeaderRPTToCallPa(header);
      PaaSILVerificaRP bodyVerificaRPTToCallPa = new PaaSILVerificaRP();
      bodyVerificaRPTToCallPa.setIdentificativoPSP(bodyrichiesta.getIdentificativoPSP());

      try {
        String xmlBodyString = jaxbTransformService.marshalling(bodyVerificaRPTToCallPa, PaaSILVerificaRP.class);
        String xmlHeaderString = jaxbTransformService.marshalling(headerVerificaRPTToCallPa, it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT.class);
        giornaleFespService.registraEvento(
            null,
            header.getIdentificativoDominio(),
            header.getIdentificativoUnivocoVersamento(),
            header.getCodiceContestoPagamento(),
            bodyrichiesta.getIdentificativoPSP(),
            Constants.PAY_PRESSO_PSP,
            Constants.COMPONENTE_FESP,
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
            Constants.GIORNALE_TIPO_EVENTO_FESP.paaSILVerificaRP.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
            header.getIdentificativoStazioneIntermediarioPA(),
            header.getIdentificativoDominio(),
            header.getIdentificativoStazioneIntermediarioPA(),
            null,
            xmlHeaderString + xmlBodyString,
            Constants.GIORNALE_ESITO_EVENTO.OK.toString()
        );
      } catch (Exception e) {
        log.warn("paaVerificaRPT REQUEST impossible to insert in the event log", e);
      }

      PaaSILVerificaRPRisposta paaSILVerificaRPRisposta;
      Ente ente = enteFespService.getEnteByCodFiscale(header.getIdentificativoDominio());
      if (StringUtils.isNotBlank(ente.getDeUrlEsterniAttiva())) { //dipende da ente
        paaSILVerificaRPRisposta = new PaaSILVerificaRPRisposta();
        PaaSILVerificaEsterna bodyPaaSILVerificaEsterna = new PaaSILVerificaEsterna();
        bodyPaaSILVerificaEsterna.setIdentificativoPSP(bodyrichiesta.getIdentificativoPSP());

        PaaSILVerificaEsternaRisposta paaSILVerificaEsternaRisposta = pagamentiTelematiciEsterniCCPClient.paaSILVerificaEsterna(bodyPaaSILVerificaEsterna, headerVerificaRPTToCallPa, ente.getDeUrlEsterniAttiva());
        EsitoSILVerificaRP esitoSILVerificaRP = new EsitoSILVerificaRP();
        if (paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta() != null && paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getEsito() != null) {
          esitoSILVerificaRP.setEsito(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getEsito());
        }
        PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA = new PaaTipoDatiPagamentoPA();
        if (paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta() != null && paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA() != null) {
          paaTipoDatiPagamentoPA.setBicAccredito(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getBicAccredito());
          paaTipoDatiPagamentoPA.setCausaleVersamento(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getCausaleVersamento());
          paaTipoDatiPagamentoPA.setCredenzialiPagatore(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getCredenzialiPagatore());
          paaTipoDatiPagamentoPA.setEnteBeneficiario(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getEnteBeneficiario());
          paaTipoDatiPagamentoPA.setIbanAccredito(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getIbanAccredito());
          paaTipoDatiPagamentoPA.setImportoSingoloVersamento(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getImportoSingoloVersamento());
        }
        esitoSILVerificaRP.setDatiPagamentoPA(paaTipoDatiPagamentoPA);
        it.veneto.regione.pagamenti.pa.FaultBean faultBean = new it.veneto.regione.pagamenti.pa.FaultBean();
        if (paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta() != null && paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault() != null) {
          faultBean.setDescription(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getDescription());
          faultBean.setFaultCode(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getFaultCode());
          faultBean.setFaultString(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getFaultString());
          faultBean.setId(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getId());
          faultBean.setSerial(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getSerial());
        }
        esitoSILVerificaRP.setFault(faultBean);

        paaSILVerificaRPRisposta.setPaaSILVerificaRPRisposta(esitoSILVerificaRP);

      } else {
        String urlPaRemote = ente.getDeUrlEsterniAttiva(); //TODO: add a relevant field for remote PA url
        if(StringUtils.isNotBlank(urlPaRemote)) {
          // scenario where PA is on a remote location (toward this FESP installation)
          paaSILVerificaRPRisposta = pagamentiTelematiciCCPPaPaClient.paaSILVerificaRP(bodyVerificaRPTToCallPa, headerVerificaRPTToCallPa, urlPaRemote);
        } else {
          // scenario where PA is on the same local installation of FESP
          paaSILVerificaRPRisposta = pagamentiTelematiciCCPPaPaImpl.paaSILVerificaRP(bodyVerificaRPTToCallPa, headerVerificaRPTToCallPa);
        }
      }

      try {
        String xmlString = jaxbTransformService.marshalling(paaSILVerificaRPRisposta, PaaSILVerificaRPRisposta.class);
        giornaleFespService.registraEvento(
            null,
            header.getIdentificativoDominio(),
            header.getIdentificativoUnivocoVersamento(),
            header.getCodiceContestoPagamento(),
            bodyrichiesta.getIdentificativoPSP(),
            Constants.PAY_PRESSO_PSP,
            Constants.COMPONENTE_FESP,
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
            Constants.GIORNALE_TIPO_EVENTO_FESP.paaSILVerificaRP.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
            header.getIdentificativoStazioneIntermediarioPA(),
            header.getIdentificativoDominio(),
            header.getIdentificativoStazioneIntermediarioPA(),
            null,
            xmlString,
            paaSILVerificaRPRisposta.getPaaSILVerificaRPRisposta().getEsito()
        );
      } catch (Exception e) {
        log.warn("paaSILVerificaRP RESPONSE impossible to insert in the event log", e);
      }

      EsitoSILVerificaRP esitoSILVerificaRP = paaSILVerificaRPRisposta.getPaaSILVerificaRPRisposta();

      PaaTipoDatiPagamentoPA datiPagamentoPA = esitoSILVerificaRP.getDatiPagamentoPA();
      it.veneto.regione.pagamenti.pa.FaultBean faultBeanPA = esitoSILVerificaRP.getFault();

      // Imposto ESITO PA
      esitoVerifica.setEsito(esitoSILVerificaRP.getEsito());

      // Imposto DATI PAGAMENTO PA
      if (Constants.NODO_REGIONALE_FESP_ESITO_OK.equalsIgnoreCase(esitoSILVerificaRP.getEsito())) {

        log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
            + bodyrichiesta.getIdentificativoPSP() + "]: ente [" + ente + "]");
        log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
            + bodyrichiesta.getIdentificativoPSP() + "]: BicAccredito [" + datiPagamentoPA.getBicAccredito() + "]");
        log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
            + bodyrichiesta.getIdentificativoPSP() + "]: CausaleVersamento [" + datiPagamentoPA.getCausaleVersamento() + "]");
        log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
            + bodyrichiesta.getIdentificativoPSP() + "]: CredenzialiPagatore [" + datiPagamentoPA.getCredenzialiPagatore() + "]");
        log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
            + bodyrichiesta.getIdentificativoPSP() + "]: IbanAccredito [" + datiPagamentoPA.getIbanAccredito() + "]");
        log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
            + bodyrichiesta.getIdentificativoPSP() + "]: ImportoSingoloVersamento [" + datiPagamentoPA.getImportoSingoloVersamento() + "]");

        esitoVerifica.setDatiPagamentoPA(new gov.telematici.pagamenti.ws.PaaTipoDatiPagamentoPA());
        gov.telematici.pagamenti.ws.PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA = esitoVerifica.getDatiPagamentoPA();

        paaTipoDatiPagamentoPA.setBicAccredito(datiPagamentoPA.getBicAccredito());
        paaTipoDatiPagamentoPA.setCausaleVersamento(datiPagamentoPA.getCausaleVersamento().length() > 140 ? datiPagamentoPA.getCausaleVersamento().substring(0, 140) : datiPagamentoPA.getCausaleVersamento());
        paaTipoDatiPagamentoPA.setCredenzialiPagatore(datiPagamentoPA.getCredenzialiPagatore());
        paaTipoDatiPagamentoPA.setIbanAccredito(datiPagamentoPA.getIbanAccredito());
        paaTipoDatiPagamentoPA.setImportoSingoloVersamento(datiPagamentoPA.getImportoSingoloVersamento());
        paaTipoDatiPagamentoPA.setEnteBeneficiario(buildEnteBeneficiario(ente));
      } else if (faultBeanPA != null) {
        //Imposto FAULT PA
        FaultBean faultBean = new FaultBean();
        faultBean.setFaultCode(faultBeanPA.getFaultCode());
        faultBean.setFaultString(faultBeanPA.getFaultString());
        faultBean.setDescription(faultBeanPA.getDescription());

        if(StringUtils.isNotBlank(faultBeanPA.getOriginalFaultCode()))
          faultBean.setOriginalFaultCode(faultBeanPA.getOriginalFaultCode());
        if(StringUtils.isNotBlank(faultBeanPA.getOriginalFaultString()))
          faultBean.setOriginalFaultString(faultBeanPA.getOriginalFaultString());
        if(StringUtils.isNotBlank(faultBeanPA.getOriginalDescription()))
          faultBean.setOriginalDescription(faultBeanPA.getOriginalDescription());

        faultBean.setId(faultBeanPA.getId());
        faultBean.setSerial(faultBeanPA.getSerial());
        esitoVerifica.setFault(faultBean);
      } else {
        FaultBean faultBean = new FaultBean();
        faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
        faultBean.setFaultString("Errore generico PAA_ATTIVA_RPT: esito KO ma faultBean null");
        faultBean.setDescription("Errore generico PAA_ATTIVA_RPT: esito KO ma faultBean null");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoVerifica.setFault(faultBean);
      }

    } catch (Exception ex) {
      Pair<String, String> errorUid = appErrorService.generateNowStringAndErrorUid();
      log.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [{}] errorUid [{}]", ex.getMessage(), errorUid.getRight(), ex);
      FaultBean faultBean = new FaultBean();
      faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
      faultBean.setFaultString("Errore generico PAA_VERIFICA_RPT: " + errorUid.getRight());
      faultBean.setDescription("Errore generico PAA_VERIFICA_RPT: " + errorUid.getRight());
      faultBean.setId(header.getIdentificativoDominio());
      faultBean.setSerial(0);
      esitoVerifica.setFault(faultBean);
      esitoVerifica.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
      throw new WSFaultResponseWrapperException(paaVerificaRPTRisposta, ex);
    } finally {
      try {
        String xmlString = jaxbTransformService.marshalling(paaVerificaRPTRisposta, PaaVerificaRPTRisposta.class);
        giornaleFespService.registraEvento(
          null,
          header.getIdentificativoDominio(),
          header.getIdentificativoUnivocoVersamento(),
          header.getCodiceContestoPagamento(),
          bodyrichiesta.getIdentificativoPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.paaVerificaRPT.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
          Constants.NODO_DEI_PAGAMENTI_SPC,
          header.getIdentificativoStazioneIntermediarioPA(),
          header.getIdentificativoStazioneIntermediarioPA(),
          null,
          xmlString,
          esitoVerifica.getEsito()
        );
      } catch (Exception e) {
        log.warn("paaSILVerificaRPT RESPONSE impossible to insert in the event log", e);
      }
    }

    return paaVerificaRPTRisposta;
  }

  @Override
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public PaaAttivaRPTRisposta paaAttivaRPT(PaaAttivaRPT bodyrichiesta, gov.telematici.pagamenti.ws.ppthead.IntestazionePPT header) {

    try {
      String xmlBodyString = jaxbTransformService.marshalling(bodyrichiesta, PaaAttivaRPT.class);
      String xmlHeaderString = jaxbTransformService.marshalling(header, IntestazionePPT.class);
      giornaleFespService.registraEvento(
          null,
          header.getIdentificativoDominio(),
          header.getIdentificativoUnivocoVersamento(),
          header.getCodiceContestoPagamento(),
          bodyrichiesta.getIdentificativoPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.paaAttivaRPT.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
          Constants.NODO_DEI_PAGAMENTI_SPC,
          header.getIdentificativoStazioneIntermediarioPA(),
          header.getIdentificativoStazioneIntermediarioPA(),
          null,
          xmlHeaderString + xmlBodyString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("paaAttivaRPT [REQUEST] impossible to insert in the event log", e);
    }

    var govTelematiciPagamentiWsObjectFactory = new ObjectFactory();
    PaaAttivaRPTRisposta paaAttivaRPTRisposta = govTelematiciPagamentiWsObjectFactory.createPaaAttivaRPTRisposta();
    paaAttivaRPTRisposta.setPaaAttivaRPTRisposta(govTelematiciPagamentiWsObjectFactory.createEsitoAttivaRPT());
    EsitoAttivaRPT esitoAttivaRPT = paaAttivaRPTRisposta.getPaaAttivaRPTRisposta();

    Optional<AttivaRptE> attivaRptEWrapper = attivaRptService.getByKey(header.getIdentificativoDominio(), header.getIdentificativoUnivocoVersamento(),
        header.getCodiceContestoPagamento());

    if (attivaRptEWrapper.isPresent() && attivaRptEWrapper.get().getDeAttivarptEsito().equals("OK")) {
      log.warn(FaultCodeConstants.PAA_ATTIVA_RPT_DUPLICATA + ": [ATTIVA DUPLICATA]");
      esitoAttivaRPT.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
      FaultBean faultBean = govTelematiciPagamentiWsObjectFactory.createFaultBean();
      faultBean.setFaultCode(FaultCodeConstants.PAA_ATTIVA_RPT_DUPLICATA);
      faultBean.setFaultString("Errore PAA_ATTIVA_RPT_DUPLICATA: Attiva duplicata");
      faultBean.setDescription("Errore PAA_ATTIVA_RPT_DUPLICATA: Attiva duplicata per identificativo dominio [" + header.getIdentificativoDominio()
          + "], IUV [" + header.getIdentificativoUnivocoVersamento() + "], codice contesto pagamento [" + header.getCodiceContestoPagamento() + "]");
      faultBean.setId(header.getIdentificativoDominio());
      faultBean.setSerial(0);
      esitoAttivaRPT.setFault(faultBean);

      try {
        String xmlString = jaxbTransformService.marshalling(paaAttivaRPTRisposta, PaaAttivaRPTRisposta.class);
        giornaleFespService.registraEvento(
            null,
            header.getIdentificativoDominio(),
            header.getIdentificativoUnivocoVersamento(),
            header.getCodiceContestoPagamento(),
            bodyrichiesta.getIdentificativoPSP(),
            Constants.PAY_PRESSO_PSP,
            Constants.COMPONENTE_FESP,
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
            Constants.GIORNALE_TIPO_EVENTO_FESP.paaAttivaRPT.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
            Constants.NODO_DEI_PAGAMENTI_SPC,
            header.getIdentificativoStazioneIntermediarioPA(),
            header.getIdentificativoStazioneIntermediarioPA(),
            null,
            xmlString,
            esitoAttivaRPT.getEsito()
        );
      } catch (Exception e) {
        log.warn("paaAttivaRPT RESPONSE impossible to insert in the event log", e);
      }

      return paaAttivaRPTRisposta;
    }

    String codAttivarptIdentificativoIntermediarioPa;
    String codAttivarptIdentificativoStazioneIntermediarioPa;
    String codAttivarptIdentificativoDominio = null;

    try {
      // RECUPERO I CAMPI PASSATI
      // Recuperati dall'header
      codAttivarptIdentificativoIntermediarioPa = header.getIdentificativoIntermediarioPA();
      codAttivarptIdentificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
      codAttivarptIdentificativoDominio = header.getIdentificativoDominio();

      if (!nodoRegionaleFespIdentificativoIntermediarioPA.equalsIgnoreCase(codAttivarptIdentificativoIntermediarioPa)
          || !nodoRegionaleFespIdentificativoStazioneIntermediarioPA.equalsIgnoreCase(codAttivarptIdentificativoStazioneIntermediarioPa)) {

        log.warn(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [Identificativo Intermediario PA diverso da regione veneto]");
        esitoAttivaRPT.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
        FaultBean faultBean = govTelematiciPagamentiWsObjectFactory.createFaultBean();
        faultBean.setFaultCode(FaultCodeConstants.PAA_ATTIVA_RPT_INTERMEDIARIO_SCONOSCIUTO);
        faultBean.setFaultString("Errore PAA_ATTIVA_RPT: Intermediario sconosciuto");
        faultBean.setDescription("Errore PAA_ATTIVA_RPT: Intermediario diverso da regione veneto");
        faultBean.setId(codAttivarptIdentificativoDominio);
        faultBean.setSerial(0);
        esitoAttivaRPT.setFault(faultBean);

        try {
          String xmlString = jaxbTransformService.marshalling(paaAttivaRPTRisposta, PaaAttivaRPTRisposta.class);
          giornaleFespService.registraEvento(
              null,
              header.getIdentificativoDominio(),
              header.getIdentificativoUnivocoVersamento(),
              header.getCodiceContestoPagamento(),
              bodyrichiesta.getIdentificativoPSP(),
              Constants.PAY_PRESSO_PSP,
              Constants.COMPONENTE_FESP,
              Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
              Constants.GIORNALE_TIPO_EVENTO_FESP.paaAttivaRPT.toString(),
              Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
              Constants.NODO_DEI_PAGAMENTI_SPC,
              header.getIdentificativoStazioneIntermediarioPA(),
              header.getIdentificativoStazioneIntermediarioPA(),
              null,
              xmlString,
              esitoAttivaRPT.getEsito()
          );
        } catch (Exception e) {
          log.warn("paaAttivaRPT RESPONSE impossible to insert in the event log", e);
        }

        return paaAttivaRPTRisposta;
      }

      Ente ente = enteFespService.getEnteByCodFiscale(codAttivarptIdentificativoDominio);

      // Persiste la request della richiesta attivazione
      AttivaRptE attivaRptE = persistiRequestAttivazione(header, bodyrichiesta);

      log.debug("persistita richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
          + bodyrichiesta.getIdentificativoPSP() + "] e codice contesto pagamento [" + header.getCodiceContestoPagamento() + "]");

      // CHIAMATA A PA
      var headerAttivaRPTToCallPa = buildHeaderRPTToCallPa(header);
      PaaSILAttivaRP bodyRichiestaAttivaRPTToCallPa = buildBodyRichiestaAttivaRPTToCallPa(bodyrichiesta);
      
      

      try {
        String xmlBodyString = jaxbTransformService.marshalling(bodyRichiestaAttivaRPTToCallPa, PaaSILAttivaRP.class);
        String xmlHeaderString = jaxbTransformService.marshalling(headerAttivaRPTToCallPa, it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT.class);
        giornaleFespService.registraEvento(
            null,
            header.getIdentificativoDominio(),
            header.getIdentificativoUnivocoVersamento(),
            header.getCodiceContestoPagamento(),
            bodyrichiesta.getIdentificativoPSP(),
            Constants.PAY_PRESSO_PSP,
            Constants.COMPONENTE_FESP,
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
            Constants.GIORNALE_TIPO_EVENTO_FESP.paaSILAttivaRP.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
            header.getIdentificativoStazioneIntermediarioPA(),
            header.getIdentificativoDominio(),
            header.getIdentificativoStazioneIntermediarioPA(),
            null,
            xmlHeaderString + xmlBodyString,
            Constants.GIORNALE_ESITO_EVENTO.OK.toString()
        );
      } catch (Exception e) {
        log.warn("paaSILAttivaRP REQUEST impossible to insert in the event log", e);
      }

      // Effettua la chiamata a PA dell'attiva RP
      PaaSILAttivaRPRisposta paaSILAttivaRPRispostaDaPa;
      String urlPaRemote = ente.getDeUrlEsterniAttiva(); //TODO: add a relevant field for remote PA url
      //WARN: this if block is not related to "attivazione esterna" but just to manage case when PA/FESP are not on same location ("remote" install)
      //      "attivazione esterna" is managed inside PA.paaSILAttivaRP() implementation
      if(StringUtils.isNotBlank(urlPaRemote)) {
        // scenario where PA is on a remote location (toward this FESP installation)
        paaSILAttivaRPRispostaDaPa = pagamentiTelematiciCCPPaPaClient.paaSILAttivaRP(bodyRichiestaAttivaRPTToCallPa, headerAttivaRPTToCallPa, urlPaRemote);
      } else {
        // scenario where PA is on the same local installation of FESP
        paaSILAttivaRPRispostaDaPa = pagamentiTelematiciCCPPaPaImpl.paaSILAttivaRP(bodyRichiestaAttivaRPTToCallPa, headerAttivaRPTToCallPa);
      }
        log.debug("risposta da PA per richiesta di attivazione per IUV [{}] e PSP [{}] esito [{}]",
            header.getIdentificativoUnivocoVersamento(),
            bodyrichiesta.getIdentificativoPSP(),
            paaSILAttivaRPRispostaDaPa.getPaaSILAttivaRPRisposta().getEsito());
      try {
        String xmlString = jaxbTransformService.marshalling(paaSILAttivaRPRispostaDaPa, PaaSILAttivaRPRisposta.class);
        giornaleFespService.registraEvento(
            null,
            header.getIdentificativoDominio(),
            header.getIdentificativoUnivocoVersamento(),
            header.getCodiceContestoPagamento(),
            bodyrichiesta.getIdentificativoPSP(),
            Constants.PAY_PRESSO_PSP,
            Constants.COMPONENTE_FESP,
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
            Constants.GIORNALE_TIPO_EVENTO_FESP.paaSILAttivaRP.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
            header.getIdentificativoStazioneIntermediarioPA(),
            header.getIdentificativoDominio(),
            header.getIdentificativoStazioneIntermediarioPA(),
            null,
            xmlString,
            paaSILAttivaRPRispostaDaPa.getPaaSILAttivaRPRisposta().getEsito()
        );
      } catch (Exception e) {
        log.warn("paaSILAttivaRP REQUEST impossible to insert in the event log", e);
      }

      // Persiste la response della richiesta attivazione
      persistiResponseAttivazione(header, bodyrichiesta, paaSILAttivaRPRispostaDaPa, ente, attivaRptE.getMygovAttivaRptEId());
      log.debug("persistita risposta pa per richiesta di attivazione per IUV [{}] e PSP [{}]",
          header.getIdentificativoUnivocoVersamento(), bodyrichiesta.getIdentificativoPSP());
      var esitoSILAttivaRP = paaSILAttivaRPRispostaDaPa.getPaaSILAttivaRPRisposta();

      if (esitoSILAttivaRP != null) {
        var datiPagamentoPA = esitoSILAttivaRP.getDatiPagamentoPA();
        var faultBeanPA = esitoSILAttivaRP.getFault();
        var faultBean = new FaultBean();
        esitoAttivaRPT.setEsito(esitoSILAttivaRP.getEsito());

        if (esitoSILAttivaRP.getEsito().equals(Constants.NODO_REGIONALE_FESP_ESITO_OK)) {
          if (datiPagamentoPA != null) {
            esitoAttivaRPT.setDatiPagamentoPA(govTelematiciPagamentiWsObjectFactory.createPaaTipoDatiPagamentoPA());
            var paaTipoDatiPagamentoPA = esitoAttivaRPT.getDatiPagamentoPA();
            paaTipoDatiPagamentoPA.setBicAccredito(datiPagamentoPA.getBicAccredito());
            paaTipoDatiPagamentoPA.setCausaleVersamento(datiPagamentoPA.getCausaleVersamento());
            paaTipoDatiPagamentoPA.setCredenzialiPagatore(datiPagamentoPA.getCredenzialiPagatore());
            paaTipoDatiPagamentoPA.setIbanAccredito(datiPagamentoPA.getIbanAccredito());
            paaTipoDatiPagamentoPA.setImportoSingoloVersamento(datiPagamentoPA.getImportoSingoloVersamento());
            // ENTE BENEFICIARIO DA FESP QUELLO DI PA E' INCOMPLETO
            paaTipoDatiPagamentoPA.setEnteBeneficiario(buildEnteBeneficiario(ente));
            esitoAttivaRPT.setDatiPagamentoPA(paaTipoDatiPagamentoPA);

            // Launch async call to InviaRPTAttivata to improve performances (in any case a batch jobs will do it later, if necessary)
            log.info("publishing event for inviaRPT for ente: {} - iuv: {} - ccp: {}", header.getIdentificativoDominio(), header.getIdentificativoUnivocoVersamento(),
              header.getCodiceContestoPagamento());
            applicationEventPublisher.publishEvent(InviaRptEvent.builder()
              .codAttivarptIdentificativoDominio(header.getIdentificativoDominio())
              .codAttivarptIdentificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
              .codAttivarptCodiceContestoPagamento(header.getCodiceContestoPagamento())
              .build());
          } else {
            faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
            faultBean.setFaultString("Errore generico PAA_ATTIVA_RPT: esito OK ma paaTipoDatiPagamentoPA null");
            faultBean.setDescription("Errore generico PAA_ATTIVA_RPT: esito OK ma paaTipoDatiPagamentoPA null");
            faultBean.setId(codAttivarptIdentificativoDominio);
            faultBean.setSerial(0);
            esitoAttivaRPT.setFault(faultBean);
          }
        } else if (esitoSILAttivaRP.getEsito().equals(Constants.NODO_REGIONALE_FESP_ESITO_KO)) {
          if (faultBeanPA != null) {
            faultBean.setFaultCode(faultBeanPA.getFaultCode());
            faultBean.setFaultString(faultBeanPA.getFaultString());
            faultBean.setId(faultBeanPA.getId());
            faultBean.setDescription(faultBeanPA.getDescription());
            Utilities.setIfNotBlank(faultBeanPA.getOriginalFaultCode(), faultBean::setOriginalFaultCode);
            Utilities.setIfNotBlank(faultBeanPA.getOriginalFaultString(), faultBean::setOriginalFaultString);
            Utilities.setIfNotBlank(faultBeanPA.getOriginalDescription(), faultBean::setOriginalDescription);
            faultBean.setSerial(faultBeanPA.getSerial());
            esitoAttivaRPT.setFault(faultBean);
          } else {
            faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
            faultBean.setFaultString("Errore generico PAA_ATTIVA_RPT: esito KO ma faultBean null");
            faultBean.setDescription("Errore generico PAA_ATTIVA_RPT: esito KO ma faultBean null");
            faultBean.setId(codAttivarptIdentificativoDominio);
            faultBean.setSerial(0);
            esitoAttivaRPT.setFault(faultBean);
          }
        } else {
          faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
          faultBean.setFaultString("Errore generico PAA_ATTIVA_RPT: esito sconosciuto "+esitoSILAttivaRP.getEsito());
          faultBean.setDescription("Errore generico PAA_ATTIVA_RPT: esito sconosciuto "+esitoSILAttivaRP.getEsito());
          faultBean.setId(codAttivarptIdentificativoDominio);
          faultBean.setSerial(0);
          esitoAttivaRPT.setFault(faultBean);
        }
      } else throw new Exception("esitoSILAttivaRP is NULL");
    } catch (Exception ex) {
      Pair<String, String> errorUid = appErrorService.generateNowStringAndErrorUid();
      log.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [{}] errorUid [{}]", ex.getMessage(), errorUid.getRight(), ex);
      esitoAttivaRPT.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
      FaultBean faultBean = govTelematiciPagamentiWsObjectFactory.createFaultBean();
      faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
      faultBean.setFaultString("Errore generico PAA_ATTIVA_RPT: " + errorUid.getRight());
      faultBean.setDescription("Errore generico PAA_ATTIVA_RPT: " + errorUid.getRight());
      faultBean.setId(codAttivarptIdentificativoDominio);
      faultBean.setSerial(0);
      esitoAttivaRPT.setFault(faultBean);
      throw new WSFaultResponseWrapperException(paaAttivaRPTRisposta, ex);
    } finally {
      try {
        String xmlString = jaxbTransformService.marshalling(paaAttivaRPTRisposta, PaaAttivaRPTRisposta.class);
        giornaleFespService.registraEvento(
          null,
          header.getIdentificativoDominio(),
          header.getIdentificativoUnivocoVersamento(),
          header.getCodiceContestoPagamento(),
          bodyrichiesta.getIdentificativoPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.paaAttivaRPT.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
          Constants.NODO_DEI_PAGAMENTI_SPC,
          header.getIdentificativoStazioneIntermediarioPA(),
          header.getIdentificativoStazioneIntermediarioPA(),
          null,
          xmlString,
          esitoAttivaRPT.getEsito()
        );
      } catch (Exception e) {
        log.warn("paaAttivaRPTRisposta RESPONSE impossible to insert in the event log", e);
      }
    }

    return paaAttivaRPTRisposta;
  }

  private AttivaRptE persistiRequestAttivazione(IntestazionePPT header, PaaAttivaRPT bodyrichiesta) {
    PaaTipoDatiPagamentoPSP datiPagamentoPSP = bodyrichiesta.getDatiPagamentoPSP();
    
    AttivaRptE.AttivaRptEBuilder builder = AttivaRptE.builder()
      .codAttivarptIdentificativoIntermediarioPa(header.getIdentificativoIntermediarioPA())
      .codAttivarptIdentificativoStazioneIntermediarioPa(header.getIdentificativoStazioneIntermediarioPA())
      .codAttivarptIdentificativoDominio(header.getIdentificativoDominio())
      .codAttivarptIdentificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
      .codAttivarptCodiceContestoPagamento(header.getCodiceContestoPagamento())
      .dtAttivarpt(new Date())
      .codAttivarptIdPsp(bodyrichiesta.getIdentificativoPSP())
      .codAttivarptIdIntermediarioPsp(bodyrichiesta.getIdentificativoIntermediarioPSP())
      .codAttivarptIdCanalePsp(bodyrichiesta.getIdentificativoCanalePSP())
      .numAttivarptImportoSingoloVersamento(datiPagamentoPSP.getImportoSingoloVersamento())
      .deAttivarptIbanAddebito(datiPagamentoPSP.getIbanAddebito())
      .deAttivarptBicAddebito(datiPagamentoPSP.getBicAddebito());
    
    if (StringUtils.isNotBlank(datiPagamentoPSP.getIbanAppoggio()))
      builder.deAttivarptIbanAppoggio(datiPagamentoPSP.getIbanAppoggio());
    if (StringUtils.isNotBlank(datiPagamentoPSP.getBicAppoggio()))
      builder.deAttivarptBicAppoggio(datiPagamentoPSP.getBicAppoggio());

    if (datiPagamentoPSP.getSoggettoVersante() != null) {
      if (datiPagamentoPSP.getSoggettoVersante().getIdentificativoUnivocoVersante() != null
          && datiPagamentoPSP.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() != null) {
        builder.codAttivarptSoggVersIdUnivVersTipoIdUnivoco(datiPagamentoPSP.getSoggettoVersante().getIdentificativoUnivocoVersante()
            .getTipoIdentificativoUnivoco().value());
        builder.codAttivarptSoggVersIdUnivVersCodiceIdUnivoco(datiPagamentoPSP.getSoggettoVersante().getIdentificativoUnivocoVersante()
            .getCodiceIdentificativoUnivoco());
      }
      builder.deAttivarptSoggVersAnagraficaVersante(datiPagamentoPSP.getSoggettoVersante().getAnagraficaVersante());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getIndirizzoVersante()))
        builder.deAttivarptSoggVersIndirizzoVersante(datiPagamentoPSP.getSoggettoVersante().getIndirizzoVersante());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getCivicoVersante()))
        builder.deAttivarptSoggVersCivicoVersante(datiPagamentoPSP.getSoggettoVersante().getCivicoVersante());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getCapVersante()))
        builder.codAttivarptSoggVersCapVersante(datiPagamentoPSP.getSoggettoVersante().getCapVersante());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getLocalitaVersante()))
        builder.deAttivarptSoggVersLocalitaVersante(datiPagamentoPSP.getSoggettoVersante().getLocalitaVersante());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getProvinciaVersante()))
        builder.deAttivarptSoggVersProvinciaVersante(datiPagamentoPSP.getSoggettoVersante().getProvinciaVersante());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getNazioneVersante()))
        builder.codAttivarptSoggVersNazioneVersante(datiPagamentoPSP.getSoggettoVersante().getNazioneVersante());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getEMailVersante()))
        builder.deAttivarptSoggVersEmailVersante(datiPagamentoPSP.getSoggettoVersante().getEMailVersante());
    }

    if (datiPagamentoPSP.getSoggettoPagatore() != null) {
      if (datiPagamentoPSP.getSoggettoPagatore().getIdentificativoUnivocoPagatore() != null
          && datiPagamentoPSP.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco() != null) {
        builder.codAttivarptSoggPagIdUnivPagTipoIdUnivoco(datiPagamentoPSP.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
            .getTipoIdentificativoUnivoco().value());
        builder.codAttivarptSoggPagIdUnivPagCodiceIdUnivoco(datiPagamentoPSP.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
            .getCodiceIdentificativoUnivoco());
      }
      builder.deAttivarptSoggPagAnagraficaPagatore(datiPagamentoPSP.getSoggettoPagatore().getAnagraficaPagatore());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getIndirizzoPagatore()))
        builder.deAttivarptSoggPagIndirizzoPagatore(datiPagamentoPSP.getSoggettoPagatore().getIndirizzoPagatore());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getCivicoPagatore()))
        builder.deAttivarptSoggPagCivicoPagatore(datiPagamentoPSP.getSoggettoPagatore().getCivicoPagatore());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getCapPagatore()))
        builder.codAttivarptSoggPagCapPagatore(datiPagamentoPSP.getSoggettoPagatore().getCapPagatore());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getLocalitaPagatore()))
        builder.deAttivarptSoggPagLocalitaPagatore(datiPagamentoPSP.getSoggettoPagatore().getLocalitaPagatore());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getProvinciaPagatore()))
        builder.deAttivarptSoggPagProvinciaPagatore(datiPagamentoPSP.getSoggettoPagatore().getProvinciaPagatore());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getNazionePagatore()))
        builder.codAttivarptSoggPagNazionePagatore(datiPagamentoPSP.getSoggettoPagatore().getNazionePagatore());
      if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getEMailPagatore()))
        builder.deAttivarptSoggPagEmailPagatore(datiPagamentoPSP.getSoggettoPagatore().getEMailPagatore());
    }

    AttivaRptE attivaRptE = builder.build();
    // persist request "attivazione"
    long attivaRptEId = attivaRptService.insert(attivaRptE);
    return attivaRptService.getById(attivaRptEId).get();
  }

  private void persistiResponseAttivazione(IntestazionePPT header, PaaAttivaRPT bodyrichiesta, PaaSILAttivaRPRisposta paaSILAttivaRPRispostaDaPa,
                                           Ente enteProp, long mygovAttivaRptEId) {

    // Recuperati dai Dati Pagamento
    BigDecimal numEAttivarptImportoSingoloVersamento = null;
    String deEAttivarptIbanAccredito = null;
    String deEAttivarptBicAccredito = null;
    String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco = null;
    String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco = null;
    String deEAttivarptEnteBenefDenominazioneBeneficiario = null;
    String codEAttivarptEnteBenefCodiceUnitOperBeneficiario = null;
    String deEAttivarptEnteBenefDenomUnitOperBeneficiario = null;
    String deEAttivarptEnteBenefIndirizzoBeneficiario = null;
    String deEAttivarptEnteBenefCivicoBeneficiario = null;
    String codEAttivarptEnteBenefCapBeneficiario = null;
    String deEAttivarptEnteBenefLocalitaBeneficiario = null;
    String deEAttivarptEnteBenefProvinciaBeneficiario = null;
    String codEAttivarptEnteBenefNazioneBeneficiario = null;
    String deEAttivarptCredenzialiPagatore = null;
    String deEAttivarptCausaleVersamento = null;
    String deAttivarptEsito = null;
    String codAttivarptFaultCode = null;
    String deAttivarptFaultString = null;
    String codAttivarptId = null;
    String deAttivarptDescription = null;
    Integer codAttivarptSerial = null;
    String codAttivarptOriginalFaultCode = null;
    String deAttivarptOriginalFaultString = null;
    String deAttivarptOriginalFaultDescription = null;

    EsitoSILAttivaRP esitoSILAttivaRP = paaSILAttivaRPRispostaDaPa.getPaaSILAttivaRPRisposta();
    if (esitoSILAttivaRP != null) {
      it.veneto.regione.pagamenti.pa.PaaTipoDatiPagamentoPA datiPagamentoDaPa = esitoSILAttivaRP.getDatiPagamentoPA();

      if (datiPagamentoDaPa != null) {
        deEAttivarptCausaleVersamento = datiPagamentoDaPa.getCausaleVersamento();
        numEAttivarptImportoSingoloVersamento = datiPagamentoDaPa.getImportoSingoloVersamento();
        deEAttivarptIbanAccredito = datiPagamentoDaPa.getIbanAccredito();
        deEAttivarptBicAccredito = datiPagamentoDaPa.getBicAccredito();
        deEAttivarptCredenzialiPagatore = datiPagamentoDaPa.getCredenzialiPagatore();

        it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario enteBeneficiario = buildEnteBeneficiario(enteProp);
        if (enteBeneficiario != null) {
          codEAttivarptEnteBenefCapBeneficiario = enteBeneficiario.getCapBeneficiario();
          deEAttivarptEnteBenefCivicoBeneficiario = enteBeneficiario.getCivicoBeneficiario();
          codEAttivarptEnteBenefCodiceUnitOperBeneficiario = enteBeneficiario.getCodiceUnitOperBeneficiario();
          deEAttivarptEnteBenefDenominazioneBeneficiario = enteBeneficiario.getDenominazioneBeneficiario();
          deEAttivarptEnteBenefDenomUnitOperBeneficiario = enteBeneficiario.getDenomUnitOperBeneficiario();
          deEAttivarptEnteBenefIndirizzoBeneficiario = enteBeneficiario.getIndirizzoBeneficiario();
          deEAttivarptEnteBenefLocalitaBeneficiario = enteBeneficiario.getLocalitaBeneficiario();
          codEAttivarptEnteBenefNazioneBeneficiario = enteBeneficiario.getNazioneBeneficiario();
          deEAttivarptEnteBenefProvinciaBeneficiario = enteBeneficiario.getProvinciaBeneficiario();

          if (enteBeneficiario.getIdentificativoUnivocoBeneficiario() != null
              && enteBeneficiario.getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco() != null) {
            codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco = enteBeneficiario.getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco()
                .value();
            codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco = enteBeneficiario.getIdentificativoUnivocoBeneficiario()
                .getCodiceIdentificativoUnivoco();
          }
        }
      }

      // Esito
      deAttivarptEsito = esitoSILAttivaRP.getEsito();
      // Codici errore
      it.veneto.regione.pagamenti.pa.FaultBean faultBean = esitoSILAttivaRP.getFault();
      if (faultBean != null) {
        codAttivarptFaultCode = faultBean.getFaultCode();
        deAttivarptFaultString = faultBean.getFaultString();
        codAttivarptId = faultBean.getId();
        deAttivarptDescription = faultBean.getDescription();
        codAttivarptSerial = faultBean.getSerial();
        if(StringUtils.isNotBlank(faultBean.getOriginalFaultCode()))
          codAttivarptOriginalFaultCode = faultBean.getOriginalFaultCode();
        if(StringUtils.isNotBlank(faultBean.getOriginalFaultString()))
          deAttivarptOriginalFaultString = faultBean.getOriginalFaultString();
        if(StringUtils.isNotBlank(faultBean.getOriginalDescription()))
          deAttivarptOriginalFaultDescription = faultBean.getOriginalDescription();
      }

      // PERSISTE LA RISPOSTA ALLA RICHIESTA DI ATTIVAZIONE
      attivaRptService.updateByKey(mygovAttivaRptEId, new Date(), numEAttivarptImportoSingoloVersamento, deEAttivarptIbanAccredito,
          deEAttivarptBicAccredito, codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco, codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco,
          deEAttivarptEnteBenefDenominazioneBeneficiario, codEAttivarptEnteBenefCodiceUnitOperBeneficiario,
          deEAttivarptEnteBenefDenomUnitOperBeneficiario, deEAttivarptEnteBenefIndirizzoBeneficiario, deEAttivarptEnteBenefCivicoBeneficiario,
          codEAttivarptEnteBenefCapBeneficiario, deEAttivarptEnteBenefLocalitaBeneficiario, deEAttivarptEnteBenefProvinciaBeneficiario,
          codEAttivarptEnteBenefNazioneBeneficiario, deEAttivarptCredenzialiPagatore, deEAttivarptCausaleVersamento, deAttivarptEsito,
          codAttivarptFaultCode, deAttivarptFaultString, codAttivarptId, deAttivarptDescription, codAttivarptSerial,
          codAttivarptOriginalFaultCode, deAttivarptOriginalFaultString, deAttivarptOriginalFaultDescription);
    }
  }

  private it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT buildHeaderRPTToCallPa(IntestazionePPT header) {
    var headerAttivaRPTToCallPa = new it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT();
    headerAttivaRPTToCallPa.setCodiceContestoPagamento(header.getCodiceContestoPagamento());
    headerAttivaRPTToCallPa.setIdentificativoDominio(header.getIdentificativoDominio());
    headerAttivaRPTToCallPa.setIdentificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento());
    headerAttivaRPTToCallPa.setIdentificativoIntermediarioPA(header.getIdentificativoIntermediarioPA());
    headerAttivaRPTToCallPa.setIdentificativoStazioneIntermediarioPA(header.getIdentificativoStazioneIntermediarioPA());
    return headerAttivaRPTToCallPa;
  }

  private PaaSILAttivaRP buildBodyRichiestaAttivaRPTToCallPa(PaaAttivaRPT bodyrichiesta) {
    PaaSILAttivaRP bodyRichiestaAttivaRPTToCallPa = new PaaSILAttivaRP();
    bodyRichiestaAttivaRPTToCallPa.setIdentificativoPSP(bodyrichiesta.getIdentificativoPSP());
    Optional.ofNullable(bodyrichiesta.getIdentificativoIntermediarioPSP()).ifPresent(bodyRichiestaAttivaRPTToCallPa::setIdentificativoIntermediarioPSP);
    Optional.ofNullable(bodyrichiesta.getIdentificativoCanalePSP()).ifPresent(bodyRichiestaAttivaRPTToCallPa::setIdentificativoCanalePSP);
    bodyRichiestaAttivaRPTToCallPa.setDatiPagamentoPSP(buildPaaTipoDatiPagamentoToCallPa(bodyrichiesta.getDatiPagamentoPSP()));
    return bodyRichiestaAttivaRPTToCallPa;
  }

  private it.veneto.regione.pagamenti.pa.PaaTipoDatiPagamentoPSP buildPaaTipoDatiPagamentoToCallPa(PaaTipoDatiPagamentoPSP datiPagamentoPSP) {
    var paaTipoDatiPagamentoToCallPa = new it.veneto.regione.pagamenti.pa.PaaTipoDatiPagamentoPSP();
    Optional.ofNullable(datiPagamentoPSP.getIbanAppoggio()).ifPresent(paaTipoDatiPagamentoToCallPa::setIbanAppoggio);
    Optional.ofNullable(datiPagamentoPSP.getBicAppoggio()).ifPresent(paaTipoDatiPagamentoToCallPa::setBicAppoggio);
    Optional.ofNullable(datiPagamentoPSP.getIbanAddebito()).ifPresent(paaTipoDatiPagamentoToCallPa::setIbanAddebito);
    Optional.ofNullable(datiPagamentoPSP.getBicAddebito()).ifPresent(paaTipoDatiPagamentoToCallPa::setBicAddebito);
    Optional.ofNullable(datiPagamentoPSP.getImportoSingoloVersamento()).ifPresent(paaTipoDatiPagamentoToCallPa::setImportoSingoloVersamento);

    paaTipoDatiPagamentoToCallPa.setSoggettoPagatore(buildSoggettoPagatoreToCallPa(datiPagamentoPSP.getSoggettoPagatore()));
    paaTipoDatiPagamentoToCallPa.setSoggettoVersante(buildSoggettoVersanteToCallPa(datiPagamentoPSP.getSoggettoVersante()));
    return paaTipoDatiPagamentoToCallPa;
  }

  private CtSoggettoVersante buildSoggettoVersanteToCallPa(it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoVersante soggettoVersante) {
    CtSoggettoVersante ctSoggettoVersanteToCallPa = new CtSoggettoVersante();
    var identificativoUnivocoVersante = new it.veneto.regione.schemas._2012.pagamenti.CtIdentificativoUnivocoPersonaFG();
    if (soggettoVersante != null && soggettoVersante.getIdentificativoUnivocoVersante() != null
        && soggettoVersante.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() != null) {

      var tipoIdentificativoUnivocoPersona = it.veneto.regione.schemas._2012.pagamenti.StTipoIdentificativoUnivocoPersFG
          .valueOf(soggettoVersante.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString());

      identificativoUnivocoVersante.setTipoIdentificativoUnivoco(tipoIdentificativoUnivocoPersona);
      identificativoUnivocoVersante.setCodiceIdentificativoUnivoco(soggettoVersante.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());

      /**
       *  RIMOSSO IN DATA 01-12-2017
       *  L'anagrafica pagatore viene valorizzata con quella dell'avviso o del dovuto, l'anagrafica versante è stata ignorata perchè possono arrivare dati scorretti
       */
      //ctSoggettoVersanteToCallPa.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);

      ctSoggettoVersanteToCallPa.setAnagraficaVersante(soggettoVersante.getAnagraficaVersante());
      Optional.ofNullable(soggettoVersante.getIndirizzoVersante()).ifPresent(ctSoggettoVersanteToCallPa::setIndirizzoVersante);
      Optional.ofNullable(soggettoVersante.getCivicoVersante()).ifPresent(ctSoggettoVersanteToCallPa::setCivicoVersante);
      Optional.ofNullable(soggettoVersante.getCapVersante()).ifPresent(ctSoggettoVersanteToCallPa::setCapVersante);
      Optional.ofNullable(soggettoVersante.getLocalitaVersante()).ifPresent(ctSoggettoVersanteToCallPa::setLocalitaVersante);
      Optional.ofNullable(soggettoVersante.getProvinciaVersante()).ifPresent(ctSoggettoVersanteToCallPa::setProvinciaVersante);
      Optional.ofNullable(soggettoVersante.getNazioneVersante()).ifPresent(ctSoggettoVersanteToCallPa::setNazioneVersante);
      Optional.ofNullable(soggettoVersante.getEMailVersante()).ifPresent(ctSoggettoVersanteToCallPa::setEMailVersante);
    }
    return ctSoggettoVersanteToCallPa;
  }

  private CtSoggettoPagatore buildSoggettoPagatoreToCallPa(it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoPagatore soggettoPagatore) {
    CtSoggettoPagatore soggettoPagatoreToCallPa = new CtSoggettoPagatore();
    var identificativoUnivocoPagatore = new it.veneto.regione.schemas._2012.pagamenti.CtIdentificativoUnivocoPersonaFG();

    if (soggettoPagatore != null && soggettoPagatore.getIdentificativoUnivocoPagatore() != null
        && soggettoPagatore.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco() != null) {

      var tipoIdentificativoUnivocoPersona = it.veneto.regione.schemas._2012.pagamenti.StTipoIdentificativoUnivocoPersFG
          .valueOf(soggettoPagatore.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString());

      identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(tipoIdentificativoUnivocoPersona);
      identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(soggettoPagatore.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());

      /**
       *  RIMOSSO IN DATA 01-12-2017
       *  L'anagrafica pagatore viene valorizzata con quella dell'avviso o del dovuto, l'anagrafica versante è stata ignorata perchè possono arrivare dati scorretti
       */
      //soggettoPagatoreToCallPa.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);

      soggettoPagatoreToCallPa.setAnagraficaPagatore(soggettoPagatore.getAnagraficaPagatore());
      Optional.ofNullable(soggettoPagatore.getIndirizzoPagatore()).ifPresent(soggettoPagatoreToCallPa::setIndirizzoPagatore);
      Optional.ofNullable(soggettoPagatore.getCivicoPagatore()).ifPresent(soggettoPagatoreToCallPa::setCivicoPagatore);
      Optional.ofNullable(soggettoPagatore.getCapPagatore()).ifPresent(soggettoPagatoreToCallPa::setCapPagatore);
      Optional.ofNullable(soggettoPagatore.getLocalitaPagatore()).ifPresent(soggettoPagatoreToCallPa::setLocalitaPagatore);
      Optional.ofNullable(soggettoPagatore.getProvinciaPagatore()).ifPresent(soggettoPagatoreToCallPa::setProvinciaPagatore);
      Optional.ofNullable(soggettoPagatore.getNazionePagatore()).ifPresent(soggettoPagatoreToCallPa::setNazionePagatore);
      Optional.ofNullable(soggettoPagatore.getEMailPagatore()).ifPresent(soggettoPagatoreToCallPa::setEMailPagatore);
    }
    return soggettoPagatoreToCallPa;
  }

  private CtEnteBeneficiario buildEnteBeneficiario(Ente enteProp) {
    // l'ente sull'rp nn c'e' (prendere da tabella ente)
    CtEnteBeneficiario enteBeneficiario = new CtEnteBeneficiario();

    CtIdentificativoUnivocoPersonaG idUnivocoPersonaG = new CtIdentificativoUnivocoPersonaG();
    idUnivocoPersonaG.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.fromValue(enteProp
        .getCodRpEnteBenefIdUnivBenefTipoIdUnivoco()));
    idUnivocoPersonaG.setCodiceIdentificativoUnivoco(enteProp.getCodRpEnteBenefIdUnivBenefCodiceIdUnivoco());
    enteBeneficiario.setIdentificativoUnivocoBeneficiario(idUnivocoPersonaG);

    enteBeneficiario.setDenominazioneBeneficiario(enteProp.getDeRpEnteBenefDenominazioneBeneficiario());
    Utilities.setIfNotBlank(enteProp.getDeRpEnteBenefDenomUnitOperBeneficiario(), enteBeneficiario::setDenomUnitOperBeneficiario);
    Utilities.setIfNotBlank(enteProp.getCodRpEnteBenefCodiceUnitOperBeneficiario(), enteBeneficiario::setCodiceUnitOperBeneficiario);
    Utilities.setIfNotBlank(enteProp.getDeRpEnteBenefLocalitaBeneficiario(), enteBeneficiario::setLocalitaBeneficiario);
    Utilities.setIfNotBlank(enteProp.getDeRpEnteBenefProvinciaBeneficiario(), enteBeneficiario::setProvinciaBeneficiario);
    Utilities.setIfNotBlank(enteProp.getDeRpEnteBenefIndirizzoBeneficiario(), enteBeneficiario::setIndirizzoBeneficiario);
    Utilities.setIfNotBlank(enteProp.getDeRpEnteBenefCivicoBeneficiario(), enteBeneficiario::setCivicoBeneficiario);
    Utilities.setIfNotBlank(enteProp.getCodRpEnteBenefCapBeneficiario(), enteBeneficiario::setCapBeneficiario);
    Utilities.setIfNotBlank(enteProp.getCodRpEnteBenefNazioneBeneficiario(), enteBeneficiario::setNazioneBeneficiario);

    return enteBeneficiario;
  }
}
