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
package it.regioneveneto.mygov.payment.mypay4.ws.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.regioneveneto.mygov.payment.mypay4.bo.CarrelloBo;
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaPagatore;
import it.regioneveneto.mygov.payment.mypay4.dto.CartItem;
import it.regioneveneto.mygov.payment.mypay4.dto.common.Psp;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.PaymentOrderException;
import it.regioneveneto.mygov.payment.mypay4.exception.WSFaultResponseWrapperException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.client.PagamentiTelematiciEsterniCCPClient;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.PagamentiTelematiciCCPPa;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.ente.PaaSILImportaDovutoRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRPRisposta;
import it.veneto.regione.pagamenti.pa.*;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
import it.veneto.regione.schemas._2012.pagamenti.*;
import it.veneto.regione.schemas._2012.pagamenti.ente.Versamento;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

@Service("PagamentiTelematiciCCPPaImpl")
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciCCPPaImpl implements PagamentiTelematiciCCPPa {

  @Autowired
  JAXBTransformService jaxbTransformService;
  @Autowired
  GiornaleService giornalePaService;
  @Autowired
  EnteService enteService;
  @Autowired
  DovutoService dovutoService;
  @Autowired
  DovutoElaboratoService dovutoElaboratoService;
  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;
  @Autowired
  FlussoService flussoService;
  @Autowired
  AnagraficaStatoService anagraficaStatoService;
  @Autowired
  UtenteService utenteService;
  @Autowired
  AvvisoService avvisoService;
  @Autowired
  PagamentiTelematiciEsterniCCPClient pagamentiTelematiciEsterniCCPClient;
  @Autowired
  AnagraficaSoggettoService anagraficaSoggettoService;
  @Autowired
  PaymentManagerService paymentManagerService;
  @Autowired
  PaymentService paymentService;

  @Autowired
  SystemBlockService systemBlockService;

  private final static String DEBITS = "d";

  public PaaSILVerificaRPRisposta paaSILVerificaRP(PaaSILVerificaRP bodyrichiesta, IntestazionePPT header) {
    try {
      String xmlbodyString = jaxbTransformService.marshalling(bodyrichiesta, PaaSILVerificaRP.class);
      String xmlHeaderString = jaxbTransformService.marshalling(header, IntestazionePPT.class);
      giornalePaService.registraEvento(
          null,
          header.getIdentificativoDominio(),
          header.getIdentificativoUnivocoVersamento(),
          header.getCodiceContestoPagamento(),
          bodyrichiesta.getIdentificativoPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_PA.paaSILVerificaRP.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
          header.getIdentificativoStazioneIntermediarioPA(),
          header.getIdentificativoDominio(),
          header.getIdentificativoStazioneIntermediarioPA(),
          Constants.EMPTY,
          xmlHeaderString + xmlbodyString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("paaSILVerificaRP [REQUEST] impossible to insert in the event log", e);
    }

    PaaSILVerificaRPRisposta paaSILVerificaRPRisposta = new PaaSILVerificaRPRisposta();
    paaSILVerificaRPRisposta.setPaaSILVerificaRPRisposta(new EsitoSILVerificaRP());

    EsitoSILVerificaRP esitoSILVerificaRP = paaSILVerificaRPRisposta.getPaaSILVerificaRPRisposta();
    esitoSILVerificaRP.setFault(new FaultBean());
    esitoSILVerificaRP.setDatiPagamentoPA(new PaaTipoDatiPagamentoPA());

    FaultBean faultBean = esitoSILVerificaRP.getFault();
    PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA = esitoSILVerificaRP.getDatiPagamentoPA();

    String iuv = header.getIdentificativoUnivocoVersamento();

    Consumer<FaultBean> registraGiornaleEventoError = aFaultBean -> {
      try {
        giornalePaService.registraEvento(
            null,
            header.getIdentificativoDominio(),
            header.getIdentificativoUnivocoVersamento(),
            header.getCodiceContestoPagamento(),
            bodyrichiesta.getIdentificativoPSP(),
            Constants.PAY_PRESSO_PSP,
            Constants.COMPONENTE_FESP,
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
            Constants.GIORNALE_TIPO_EVENTO_PA.paaSILVerificaRP.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
            header.getIdentificativoStazioneIntermediarioPA(),
            header.getIdentificativoDominio(),
            header.getIdentificativoStazioneIntermediarioPA(),
            Constants.EMPTY,
            aFaultBean.getDescription(),
            Constants.GIORNALE_ESITO_EVENTO.KO.toString()
        );
      } catch (Exception e) {
        log.warn("paaSILVerificaRP [RESPONSE] impossible to insert in the event log", e);
      }
    };

    try {
      Ente ente = enteService.getEnteByCodFiscale(header.getIdentificativoDominio());

      // Se l'ente non viene trovato ritorna errore
      if (ente == null) {
        faultBean.setFaultCode(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_CODE);
        faultBean.setDescription(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);
        faultBean.setFaultString(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;
      }
      boolean isStatoInserito = Utilities.checkIfStatoInserito(ente);
      if (isStatoInserito) {
        faultBean.setFaultCode(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_CODE);
        faultBean.setDescription(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);
        faultBean.setFaultString(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;

      }

      List<Dovuto> dovuti = dovutoService.searchDovutoByIuvEnte(iuv, ente.getCodIpaEnte());

      // Se non presente nella tabella DOVUTO
      if (dovuti == null || dovuti.size() == 0) {

        // Lo cerco nella tabella DOVUTO_ELABORATO
        List<DovutoElaborato> dovutiElaborati = dovutoElaboratoService.searchDovutoElaboratoByIuvEnte(iuv,
            ente.getCodIpaEnte());

        if ((dovutiElaborati == null) || (dovutiElaborati.size() == 0)) {
          faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_CODE);
          faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);
          faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);
          faultBean.setId(header.getIdentificativoDominio());
          faultBean.setSerial(0);
          esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
          registraGiornaleEventoError.accept(faultBean);
          return paaSILVerificaRPRisposta;
        }

        for (DovutoElaborato dovutoElaborato : dovutiElaborati) {
          // CONTROLLO DOVUTO ANNULLATO
          if (Constants.STATO_DOVUTO_ANNULLATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato())) {
            faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_CODE);
            faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_STRING);
            faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_STRING);
            faultBean.setId(header.getIdentificativoDominio());
            faultBean.setSerial(0);

            esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);

            registraGiornaleEventoError.accept(faultBean);

            return paaSILVerificaRPRisposta;
          }

          if (Constants.STATO_DOVUTO_COMPLETATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato())
              && dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento() != null
              && !dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento().equals(Constants.CODICE_ESITO_PAGAMENTO_KO)) {

            faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_CODE);
            faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_STRING);
            faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_STRING);
            faultBean.setId(header.getIdentificativoDominio());
            faultBean.setSerial(0);

            esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);

            registraGiornaleEventoError.accept(faultBean);

            return paaSILVerificaRPRisposta;
          }

        }

        faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
        faultBean.setDescription("Il pagamento richiesto ha stato non atteso");
        faultBean.setFaultString("Il pagamento richiesto ha stato non atteso");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;
      }

      // Se esiste piu di un dovuto nella tabella DOVUTO
      if (dovuti.size() > 1) {
        faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
        faultBean.setDescription("Ci sono piu' dovuti che corrispondono allo stesso iuv");
        faultBean.setFaultString("Errore pagamento multiplo");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;
      }

      Dovuto dovuto = dovuti.get(0);

      //check blacklist/whitelist codice fiscale
      systemBlockService.blockByPayerCf(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());

      Optional<EnteTipoDovuto> enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), ente.getCodIpaEnte(), false);

      // CHECK SE TIPO DOVUTO CENSITO PER ENTE
      if (enteTipoDovutoOptional.isEmpty()) {
        faultBean.setFaultCode(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_VALIDO_PER_ENTE);
        faultBean.setDescription("Tipo dovuto [" + dovuto.getCodTipoDovuto() + "]" + "non trovato per ente ["
            + ente.getCodIpaEnte() + "]");
        faultBean.setFaultString("Tipo dovuto non trovato per ente");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;
      }
      EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOptional.get();
      if (!enteTipoDovuto.isFlgAttivo()) {
        faultBean.setFaultCode(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_ABILITATO_PER_ENTE);
        faultBean.setDescription("Tipo dovuto [" + dovuto.getCodTipoDovuto() + "]" + "non abilitato per ente ["
            + ente.getCodIpaEnte() + " (flgAttivo: false)]");
        faultBean.setFaultString("Tipo dovuto non abilitato per ente");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;
      }

      if (enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_VALIDO_PER_ENTE);
        faultBean.setDescription("Pagamento tipo dovuto [" + dovuto.getCodTipoDovuto() + "]"
            + "non supportato per modello 3 per ente [" + ente.getCodIpaEnte() + "]");
        faultBean.setFaultString("Pagamento marca da bollo digitale non supportato per modello 3 per ente");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;
      }

      boolean isValidIdPSPAndTipoDovuto = isValidIdPSPAndTipoDovuto(enteTipoDovuto, bodyrichiesta.getIdentificativoPSP());
      if (!isValidIdPSPAndTipoDovuto) {
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_SEMANTICA_CODE);
        faultBean.setDescription("Nessun IBAN postale associato al tipo dovuto [ " + enteTipoDovuto.getDeTipo()
            + " ] per lo IUV [ " + header.getIdentificativoUnivocoVersamento() + " ]");
        faultBean.setFaultString(FaultCodeConstants.PAA_SEMANTICA_STRING);
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;
      }


      // EFFETTUA I CONTROLLI

      boolean isDovutoPagabile = checkDovutoPagabile(dovuto, enteTipoDovuto.isFlgScadenzaObbligatoria(), faultBean, header.getIdentificativoDominio());

      // Se non supera i controlli viene ritornato la risposta contentente
      // l'errore
      if (!isDovutoPagabile) {
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;
      }

      if ((!dovuto.getCodRpDatiVersTipoVersamento().equals(Constants.ALL_PAGAMENTI))
          && (!dovuto.getCodRpDatiVersTipoVersamento().contains(Constants.PAY_PRESSO_PSP))) {
        faultBean.setFaultCode(FaultCodeConstants.PAA_TIPO_VERSAMENTO_ERRATO);
        faultBean.setDescription("Il dovuto non contiene tipo versamento PO");
        faultBean.setFaultString("Tipo versamento non consentito");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILVerificaRPRisposta;

      }

      // Prepara risposta per FESP

      esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_OK);

      paaTipoDatiPagamentoPA
          .setImportoSingoloVersamento(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento());

      // iban di accredito in base al PSP e al tipo dovuto
      paaTipoDatiPagamentoPA.setIbanAccredito(
          getIbanAccredito(ente, enteTipoDovuto, bodyrichiesta.getIdentificativoPSP(), Constants.PAY_PRESSO_PSP));
      paaTipoDatiPagamentoPA.setBicAccredito(
          getBicAccredito(ente, enteTipoDovuto, bodyrichiesta.getIdentificativoPSP(), Constants.PAY_PRESSO_PSP));

      // lasciare CredenzialiPagatore a null perche' info non nota
      paaTipoDatiPagamentoPA.setCredenzialiPagatore(null);

      // ritorna, se presente la causale visual troncata a 140 caratteri,
      // oppure la causale del dovuto troncata a 140 caratteri.
      paaTipoDatiPagamentoPA.setCausaleVersamento(calcolaCausaleRispostaCCP(dovuto));

    } catch (java.lang.Exception ex) {
      log.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
      faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
      faultBean.setDescription("Errore generico: " + ex.getMessage());
      faultBean.setFaultString("Errore generico: " + ex.getCause());
      faultBean.setId(header.getIdentificativoDominio());
      esitoSILVerificaRP.setEsito(FaultCodeConstants.ESITO_KO);
      registraGiornaleEventoError.accept(faultBean);
      return paaSILVerificaRPRisposta;
    }

    try {
      String xmlString = jaxbTransformService.marshalling(paaSILVerificaRPRisposta, PaaSILVerificaRPRisposta.class);
      giornalePaService.registraEvento(
          null,
          header.getIdentificativoDominio(),
          header.getIdentificativoUnivocoVersamento(),
          header.getCodiceContestoPagamento(),
          bodyrichiesta.getIdentificativoPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_PA.paaSILVerificaRP.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
          header.getIdentificativoStazioneIntermediarioPA(),
          header.getIdentificativoDominio(),
          header.getIdentificativoStazioneIntermediarioPA(),
          Constants.EMPTY,
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("paaSILVerificaRP [RESPONSE] impossible to insert in the event log", e);
    }

    return paaSILVerificaRPRisposta;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public PaaSILAttivaRPRisposta paaSILAttivaRP(PaaSILAttivaRP bodyrichiesta, IntestazionePPT header) {
    
    try {
      String xmlBodyString = jaxbTransformService.marshalling(bodyrichiesta, PaaSILAttivaRP.class);
      String xmlHeaderString = jaxbTransformService.marshalling(header, IntestazionePPT.class);
      giornalePaService.registraEvento(
          null,
          header.getIdentificativoDominio(),
          header.getIdentificativoUnivocoVersamento(),
          header.getCodiceContestoPagamento(),
          bodyrichiesta.getIdentificativoPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_PA.paaSILAttivaRP.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
          header.getIdentificativoStazioneIntermediarioPA(),
          header.getIdentificativoDominio(),
          header.getIdentificativoStazioneIntermediarioPA(),
          Constants.EMPTY,
          xmlHeaderString + xmlBodyString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("paaSILAttivaRP [REQUEST] impossible to insert in the event log", e);
    }

    Consumer<FaultBean> registraGiornaleEventoError = aFaultBean -> {
      try {
        giornalePaService.registraEvento(
            null,
            header.getIdentificativoDominio(),
            header.getIdentificativoUnivocoVersamento(),
            header.getCodiceContestoPagamento(),
            bodyrichiesta.getIdentificativoPSP(),
            Constants.PAY_PRESSO_PSP,
            Constants.COMPONENTE_FESP,
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
            Constants.GIORNALE_TIPO_EVENTO_PA.paaSILAttivaRP.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
            header.getIdentificativoStazioneIntermediarioPA(),
            header.getIdentificativoDominio(),
            header.getIdentificativoStazioneIntermediarioPA(),
            Constants.EMPTY,
            aFaultBean.getDescription(),
            Constants.GIORNALE_ESITO_EVENTO.KO.toString()
        );
      } catch (Exception e) {
        log.warn("paaSILVerificaRP [RESPONSE] impossible to insert in the event log", e);
      }
    };

    PaaSILAttivaRPRisposta paaSILAttivaRPRisposta = new PaaSILAttivaRPRisposta();
    paaSILAttivaRPRisposta.setPaaSILAttivaRPRisposta(new EsitoSILAttivaRP());
    EsitoSILAttivaRP esitoSILAttivaRP = paaSILAttivaRPRisposta.getPaaSILAttivaRPRisposta();
    esitoSILAttivaRP.setDatiPagamentoPA(new PaaTipoDatiPagamentoPA());
    esitoSILAttivaRP.setFault(new FaultBean());

    FaultBean faultBean = esitoSILAttivaRP.getFault();
    PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA = esitoSILAttivaRP.getDatiPagamentoPA();


    Ente ente = enteService.getEnteByCodFiscale(header.getIdentificativoDominio());
    PaaSILAttivaEsternaRisposta paaSILAttivaEsternaRisposta = null;
    Versamento ctVersamento = null;
    if (ente!= null && StringUtils.isNotBlank(ente.getDeUrlEsterniAttiva())) { //ente
      PaaSILAttivaEsterna bodyEsterna = new PaaSILAttivaEsterna();
      bodyEsterna.setIdentificativoPSP(bodyrichiesta.getIdentificativoPSP());
      bodyEsterna.setIdentificativoIntermediarioPSP(bodyrichiesta.getIdentificativoIntermediarioPSP());
      bodyEsterna.setIdentificativoCanalePSP(bodyrichiesta.getIdentificativoCanalePSP());

      PaaTipoDatiPagamentoPSP paaTipoDatiPagamentoPSP = new PaaTipoDatiPagamentoPSP();
      if (bodyrichiesta.getDatiPagamentoPSP() != null) {
        paaTipoDatiPagamentoPSP.setBicAddebito(bodyrichiesta.getDatiPagamentoPSP().getBicAddebito());
        paaTipoDatiPagamentoPSP.setBicAppoggio(bodyrichiesta.getDatiPagamentoPSP().getBicAppoggio());
        paaTipoDatiPagamentoPSP.setIbanAddebito(bodyrichiesta.getDatiPagamentoPSP().getIbanAddebito());
        paaTipoDatiPagamentoPSP.setIbanAppoggio(bodyrichiesta.getDatiPagamentoPSP().getIbanAppoggio());
        paaTipoDatiPagamentoPSP.setImportoSingoloVersamento(bodyrichiesta.getDatiPagamentoPSP().getImportoSingoloVersamento());
        paaTipoDatiPagamentoPSP.setSoggettoPagatore(bodyrichiesta.getDatiPagamentoPSP().getSoggettoPagatore());
        paaTipoDatiPagamentoPSP.setSoggettoVersante(bodyrichiesta.getDatiPagamentoPSP().getSoggettoVersante());
      }
      bodyEsterna.setDatiPagamentoPSP(paaTipoDatiPagamentoPSP);

      paaSILAttivaEsternaRisposta = pagamentiTelematiciEsterniCCPClient.paaSILAttivaEsterna(bodyEsterna, header, ente.getDeUrlEsterniAttiva());
      if (paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta() != null &&
          paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta().getFault() != null &&
          StringUtils.isNotBlank(paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta().getFault().getFaultCode())) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        FaultBean faultBeanEsterna = paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta().getFault();
        faultBean.setFaultCode(faultBeanEsterna.getFaultCode());
        faultBean.setDescription(faultBeanEsterna.getDescription());
        faultBean.setFaultString(faultBeanEsterna.getFaultString());
        faultBean.setId(faultBeanEsterna.getId());
        faultBean.setSerial(faultBeanEsterna.getSerial());
        paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta().setFault(faultBeanEsterna);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILAttivaRPRisposta;
      }
      if (paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta() != null &&
          ("KO".equalsIgnoreCase(paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta().getEsito()) ||
              paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta().getDovuto() == null)) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_CODE);
        faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);
        faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILAttivaRPRisposta;
      }
      //mettere in unica transazione se va in errore nn ho niente su db
      PaaSILImportaDovutoRisposta paaSILImportaDovutoRisposta = dovutoService.importDovuto(Constants.GIORNALE_TIPO_EVENTO_PA.paaSILAttivaRP.toString(), false, ente.getCodIpaEnte(), ente.getDePassword(), paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta().getDovuto());
      //se va in errore sotto devo annullare dovuto inserito qua
      if ("KO".equalsIgnoreCase(paaSILImportaDovutoRisposta.getEsito())) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_CODE);
        faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);
        faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILAttivaRPRisposta;
      }
      try {
        ctVersamento = jaxbTransformService.unmarshalling(paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta().getDovuto(), Versamento.class);
      } catch (MyPayException e) {
        log.error("paaSILAttivaRP error unmarshalling: [" + e.getMessage() + "]", e);
        throw new RuntimeException("paaSILAttivaRP error unmarshalling: [" + e.getMessage() + "]", e);
      }
      //COSA DEVO SETTARE SU HEADER E BODY PROVENIENTI DA ESTERNI????
      //header.setIdentificativoUnivocoVersamento(ctVersamento.getDatiVersamento().getIdentificativoUnivocoVersamento());
    }

    String iuv = header.getIdentificativoUnivocoVersamento();

    try {
      // Se l'ente non viene trovato ritorna errore
      if (ente == null) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_CODE);
        faultBean.setDescription(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);
        faultBean.setFaultString(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        return paaSILAttivaRPRisposta;
      }
      boolean isStatoInserito = Utilities.checkIfStatoInserito(ente) ;
      if (isStatoInserito) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_CODE);
        faultBean.setDescription(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);
        faultBean.setFaultString(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        return paaSILAttivaRPRisposta;

      }

      List<Dovuto> dovuti = dovutoService.searchDovutoByIuvEnte(iuv, ente.getCodIpaEnte());
      log.debug("post estrazione Dovuto: {}", new ObjectMapper().writeValueAsString(dovuti));
      // Se non presente nella tabella DOVUTO
      if (dovuti == null || dovuti.size() == 0) {

        // Lo cerco nella tabella DOVUTO_ELABORATO
        List<DovutoElaborato> dovutiElaborati = dovutoElaboratoService.searchDovutoElaboratoByIuvEnte(iuv,
            ente.getCodIpaEnte());
        log.debug("post estrazione DovutoElaborato: {}", new ObjectMapper().writeValueAsString(dovutiElaborati));
        if ((dovutiElaborati == null) || (dovutiElaborati.size() == 0)) {
          faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_CODE);
          faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);
          faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);
          faultBean.setId(header.getIdentificativoDominio());
          faultBean.setSerial(0);
          esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
          registraGiornaleEventoError.accept(faultBean);
          if (paaSILAttivaEsternaRisposta != null)
            annullaDovuto(ctVersamento, ente);
          return paaSILAttivaRPRisposta;
        }

        for (DovutoElaborato dovutoElaborato : dovutiElaborati) {
          // CONTROLLO DOVUTO ANNULLATO
          if (Constants.STATO_DOVUTO_ANNULLATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato())) {
            faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_CODE);
            faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_STRING);
            faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_STRING);
            faultBean.setId(header.getIdentificativoDominio());
            faultBean.setSerial(0);
            esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
            registraGiornaleEventoError.accept(faultBean);
            if (paaSILAttivaEsternaRisposta != null)
              annullaDovuto(ctVersamento, ente);
            return paaSILAttivaRPRisposta;
          }

          if (Constants.STATO_DOVUTO_COMPLETATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato())
              && dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento() != null
              && !dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento().equals(Constants.CODICE_ESITO_PAGAMENTO_KO)) {
            faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_CODE);
            faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_STRING);
            faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_STRING);
            faultBean.setId(header.getIdentificativoDominio());
            faultBean.setSerial(0);
            esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
            registraGiornaleEventoError.accept(faultBean);
            if (paaSILAttivaEsternaRisposta != null)
              annullaDovuto(ctVersamento, ente);
            return paaSILAttivaRPRisposta;
          }
        }

        faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
        faultBean.setDescription("Il pagamento richiesto ha stato non atteso");
        faultBean.setFaultString("Il pagamento richiesto ha stato non atteso");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        return paaSILAttivaRPRisposta;
      }

      // Se esiste piu di un dovuto nella tabella DOVUTO
      if (dovuti.size() > 1) {
        faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
        faultBean.setDescription("Ci sono piu' dovuti che corrispondono allo stesso iuv");
        faultBean.setFaultString("Errore pagamento multiplo");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        return paaSILAttivaRPRisposta;
      }

      Dovuto dovuto = dovuti.get(0);

      //check blacklist/whitelist codice fiscale
      systemBlockService.blockByPayerCf(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());

      Optional<EnteTipoDovuto> enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), ente.getCodIpaEnte(), false);
      // CHECK SE TIPO DOVUTO CENSITO PER ENTE
      if (enteTipoDovutoOptional.isEmpty()) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_VALIDO_PER_ENTE);
        String description = StringUtils.left("Tipo dovuto [" + dovuto.getCodTipoDovuto() + "]" + "non trovato per ente ["
            + ente.getCodIpaEnte() + "]", 1024);
        faultBean.setDescription(description);
        faultBean.setFaultString("Tipo dovuto non trovato per ente");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        return paaSILAttivaRPRisposta;
      }
      EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOptional.get();
      if (!enteTipoDovuto.isFlgAttivo()) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_ABILITATO_PER_ENTE);
        String description = StringUtils.left("Tipo dovuto [" + dovuto.getCodTipoDovuto() + "]" + "non abilitato per ente ["
            + ente.getCodIpaEnte() + "(flgAttivo: false)]", 1024);
        faultBean.setDescription(description);
        faultBean.setFaultString("Tipo dovuto non abilitato per ente");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        return paaSILAttivaRPRisposta;
      }

      // CHECK SE TIPO DOVUTO CENSITO PER ENTE = MARCA DA BOLLO DIGITALE (NON POSSIBILE PAGAMENTO PRESSO PSP)
      if (enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_VALIDO_PER_ENTE);
        String description = StringUtils.left("Pagamento tipo dovuto [" + dovuto.getCodTipoDovuto() + "]"
            + "non supportato per modello 3 per ente [" + ente.getCodIpaEnte() + "]", 1024);
        faultBean.setDescription(description);
        faultBean.setFaultString("Pagamento marca da bollo digitale non supportato per modello 3 per ente");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        return paaSILAttivaRPRisposta;
      }

      boolean isValidIdPSPAndTipoDovuto = isValidIdPSPAndTipoDovuto(enteTipoDovuto,
          bodyrichiesta.getIdentificativoPSP());
      if (!isValidIdPSPAndTipoDovuto) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_SEMANTICA_CODE);
        String description = StringUtils.left("Nessun IBAN postale associato al tipo dovuto [ " + enteTipoDovuto.getDeTipo()
            + " ] per lo IUV [ " + header.getIdentificativoUnivocoVersamento() + " ]", 1024);
        faultBean.setDescription(description);
        faultBean.setFaultString(FaultCodeConstants.PAA_SEMANTICA_STRING);
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        return paaSILAttivaRPRisposta;
      }

      boolean isDovutoPagabile = checkDovutoPagabile(dovuto, enteTipoDovuto.isFlgScadenzaObbligatoria(), faultBean, header.getIdentificativoDominio());

      // Se non supera i controlli viene ritornato la risposta contentente l'errore
      if (!isDovutoPagabile) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        return paaSILAttivaRPRisposta;
      }

      if ((!dovuto.getCodRpDatiVersTipoVersamento().equals(Constants.ALL_PAGAMENTI))
          && (!dovuto.getCodRpDatiVersTipoVersamento().contains(Constants.PAY_PRESSO_PSP))) {
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        faultBean.setFaultCode(FaultCodeConstants.PAA_TIPO_VERSAMENTO_ERRATO);
        faultBean.setDescription("Il dovuto non contiene tipo versamento PO");
        faultBean.setFaultString("Tipo versamento non consentito");
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        return paaSILAttivaRPRisposta;
      }

      Psp pspScelto = new Psp();
      pspScelto.setIdentificativoPSP(bodyrichiesta.getIdentificativoPSP());
      pspScelto.setIdentificativoIntermediarioPSP(bodyrichiesta.getIdentificativoIntermediarioPSP());
      pspScelto.setIdentificativoCanale(bodyrichiesta.getIdentificativoCanalePSP());
      pspScelto.setModelloPagamento(4);
      pspScelto.setTipoVersamento(Constants.PAY_PRESSO_PSP);
      // SE IL DOVUTO E' PAGABILE GENERO IL CARRELLO PER ATTIVARE LA PREPARAZIONE DELLA RP
      final AnagraficaPagatore anagraficaPagatore = anagraficaSoggettoService.getAnagraficaPagatore(dovuto);
      CarrelloBo carrello = CarrelloBo.builder()
          .tipoCarrello(Constants.TIPO_CARRELLO_PAGAMENTO_ATTIVATO_PRESSO_PSP)
          .codIpaEnte(ente.getCodIpaEnte())
          .psp(pspScelto)
          .codiceContestoPagamento(header.getCodiceContestoPagamento())
          .dovuti(List.of(Optional.of(dovuto).map(d ->
              CartItem.builder()
                  .id(d.getMygovDovutoId())
                  .codIuv(d.getCodIuv())
                  .codIpaEnte(ente.getCodIpaEnte())
                  .codTipoDovuto(d.getCodTipoDovuto())
                  .importo(d.getNumRpDatiVersDatiSingVersImportoSingoloVersamento())
                  .build()).get()))
          .intestatario(anagraficaPagatore)
          .build();
      log.debug("CarrelloBo.builder: {}", new ObjectMapper().writeValueAsString(carrello));

      try {
        // SETTA ANAG PAGATORE SOLO SE NON E' VALORIZZATA QUELLA CHE MI ARRIVA
        Optional<Avviso> avviso = avvisoService.getByIuvEnte(iuv, ente.getCodIpaEnte());
        if(avviso.isPresent())
          carrello.setIntestatario(estraiAnagrafica(avviso.get().getCodRpSoggPagIdUnivPagTipoIdUnivoco().charAt(0),
              avviso.get().getCodRpSoggPagIdUnivPagCodiceIdUnivoco(), avviso.get().getDeRpSoggPagAnagraficaPagatore(),
              avviso.get().getDeRpSoggPagEmailPagatore(), avviso.get().getDeRpSoggPagIndirizzoPagatore(),
              avviso.get().getDeRpSoggPagCivicoPagatore(), avviso.get().getCodRpSoggPagCapPagatore(),
              avviso.get().getDeRpSoggPagLocalitaPagatore(), avviso.get().getDeRpSoggPagProvinciaPagatore(),
              avviso.get().getCodRpSoggPagNazionePagatore()));
        else
          carrello.setIntestatario(anagraficaPagatore);

        CtSoggettoVersante soggettoVersanteReq = bodyrichiesta.getDatiPagamentoPSP().getSoggettoVersante();
        if (soggettoVersanteReq != null) {
          CtIdentificativoUnivocoPersonaFG ctIdentificativoUnivocoPersonaFG = soggettoVersanteReq.getIdentificativoUnivocoVersante();
          if (ctIdentificativoUnivocoPersonaFG != null &&
              StringUtils.isNotBlank(ctIdentificativoUnivocoPersonaFG.getCodiceIdentificativoUnivoco())) {
            AnagraficaPagatore anagraficaVersante = new AnagraficaPagatore();
            if (ctIdentificativoUnivocoPersonaFG.getTipoIdentificativoUnivoco() != null) {
              anagraficaVersante.setTipoIdentificativoUnivoco(ctIdentificativoUnivocoPersonaFG.getTipoIdentificativoUnivoco().value().charAt(0));
            }
            anagraficaVersante.setCodiceIdentificativoUnivoco(ctIdentificativoUnivocoPersonaFG.getCodiceIdentificativoUnivoco());
            anagraficaVersante.setAnagrafica(soggettoVersanteReq.getAnagraficaVersante());
            anagraficaVersante.setIndirizzo(soggettoVersanteReq.getIndirizzoVersante());
            anagraficaVersante.setCivico(soggettoVersanteReq.getCivicoVersante());
            anagraficaVersante.setCap(soggettoVersanteReq.getCapVersante());
            anagraficaVersante.setLocalita(soggettoVersanteReq.getLocalitaVersante());
            anagraficaVersante.setProvincia(soggettoVersanteReq.getProvinciaVersante());
            anagraficaVersante.setNazione(soggettoVersanteReq.getNazioneVersante());
            anagraficaVersante.setEmail(soggettoVersanteReq.getEMailVersante());
            carrello.setVersante(anagraficaVersante);
          }
        }

        BigDecimal importoAttiva = bodyrichiesta.getDatiPagamentoPSP().getImportoSingoloVersamento();
        if (importoAttiva == null) {
          log.error(FaultCodeConstants.PAA_ATTIVA_RPT_IMPORTO_NON_VALIDO);
          faultBean.setFaultCode(FaultCodeConstants.PAA_ATTIVA_RPT_IMPORTO_NON_VALIDO);
          faultBean.setFaultString("Importo non specificato");
          faultBean.setDescription("Importo nullo");
          faultBean.setId(header.getIdentificativoDominio());
          esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
          registraGiornaleEventoError.accept(faultBean);
          if (paaSILAttivaEsternaRisposta != null)
            annullaDovuto(ctVersamento, ente);
          return paaSILAttivaRPRisposta;
        }

        if (importoAttiva.compareTo(BigDecimal.ZERO) == 0) {
          log.error(FaultCodeConstants.PAA_ATTIVA_RPT_IMPORTO_NON_VALIDO);
          faultBean.setFaultCode(FaultCodeConstants.PAA_ATTIVA_RPT_IMPORTO_NON_VALIDO);
          faultBean.setFaultString("Importo non valido");
          faultBean.setDescription("Importo zero non valido");
          faultBean.setId(header.getIdentificativoDominio());
          esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
          registraGiornaleEventoError.accept(faultBean);
          if (paaSILAttivaEsternaRisposta != null)
            annullaDovuto(ctVersamento, ente);
          return paaSILAttivaRPRisposta;
        }

        BigDecimal importoCarrello = carrello.getTotalAmount();
        if (importoAttiva.compareTo(importoCarrello) != 0) {
          log.error(FaultCodeConstants.PAA_ATTIVA_RPT_IMPORTO_NON_VALIDO);
          faultBean.setFaultCode(FaultCodeConstants.PAA_ATTIVA_RPT_IMPORTO_NON_VALIDO);
          faultBean.setFaultString("Importo non valido");
          faultBean.setDescription("Importo inviato non corrisponde con importo dovuto");
          faultBean.setId(header.getIdentificativoDominio());
          esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
          registraGiornaleEventoError.accept(faultBean);
          if (paaSILAttivaEsternaRisposta != null)
            annullaDovuto(ctVersamento, ente);
          return paaSILAttivaRPRisposta;
        }
      } catch (Exception e) {
        log.error("Errore in fase di preparazione anagrafiche",e);
        faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
        faultBean.setDescription("Errore in fase di preparazione anagrafiche");
        String faultString = StringUtils.left("Errore in fase di preparazione anagrafiche" + e.getMessage(), 1024);
        faultBean.setFaultString(faultString);
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        throw new WSFaultResponseWrapperException(paaSILAttivaRPRisposta, e);
      }

      // SE IL PAGAMENTO E' PAGABILE VIENE INOLTRATO L'INVIO RP
      log.debug("Il pagamento risulta essere pagabile, viene preparata l' invioRP.");

      NodoSILInviaRPRisposta nodoSILInviaRPRisposta;
      RP ctRichiestaPagamento;
      boolean isAttivaEsterna = Objects.nonNull(paaSILAttivaEsternaRisposta) && Objects.nonNull(paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta());
      PaaTipoDatiPagamentoPA datiPagamentoPA = isAttivaEsterna? paaSILAttivaEsternaRisposta.getPaaSILAttivaEsternaRisposta().getDatiPagamentoPA() : null;

      try {//portare iban da richiesta ws se esterna
        Map<String, List<CarrelloBo>> mappedCarts = Collections.singletonMap(DEBITS, Collections.singletonList(carrello));
        List<CarrelloBo> carts = paymentManagerService.placeOrder(ente.getCodIpaEnte(), null, mappedCarts, Constants.TRIGGER_PAYMENT.FROM_PSP);
        Long idCarrello = carts.get(0).getId();
        ctRichiestaPagamento = paymentManagerService.buildCtRichiestaPagamento(carts, datiPagamentoPA).get(0);
        Map<String, Object> returnHashMap = paymentManagerService.buildInviaRp(ctRichiestaPagamento, pspScelto, ente.getCodIpaEnte(), idCarrello);
        returnHashMap.put("ctRichiestaPagamento", ctRichiestaPagamento);

        nodoSILInviaRPRisposta = paymentService.inviaRP(carts.get(0).getId(), returnHashMap);

      } catch (PaymentOrderException e) {
        faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
        faultBean.setDescription("Errore in fase di preparazione della richiesta pagamento");
        faultBean.setFaultString(StringUtils.left(e.getMessage(), 1024));
        faultBean.setId(header.getIdentificativoDominio());
        faultBean.setSerial(0);
        esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
        registraGiornaleEventoError.accept(faultBean);
        if (paaSILAttivaEsternaRisposta != null)
          annullaDovuto(ctVersamento, ente);
        throw new WSFaultResponseWrapperException(paaSILAttivaRPRisposta, e);
      }

      esitoSILAttivaRP.setEsito(nodoSILInviaRPRisposta.getEsito());

      // Dove trovo valori DATI_PAGAMENTO da info su db (no
      // codiceContestoPagamento)
      CtDatiVersamentoRP ctDatiVersamentoRP = ctRichiestaPagamento.getDatiVersamento();
      if (ctDatiVersamentoRP != null && ctDatiVersamentoRP.getDatiSingoloVersamentos() != null &&
          ctDatiVersamentoRP.getDatiSingoloVersamentos().size() == 1) {
        CtDatiSingoloVersamentoRP ctDatiSingoloVersamentoRP = ctDatiVersamentoRP
            .getDatiSingoloVersamentos().get(0);
        // ritorna, se presente la causale visual troncata a 140 caratteri,
        // oppure la causale del dovuto troncata a 140 caratteri.
        paaTipoDatiPagamentoPA.setCausaleVersamento(calcolaCausaleRispostaCCP(dovuto));
        paaTipoDatiPagamentoPA.setImportoSingoloVersamento(ctDatiSingoloVersamentoRP.getImportoSingoloVersamento());
        paaTipoDatiPagamentoPA.setIbanAccredito(ctDatiSingoloVersamentoRP.getIbanAccredito());
        paaTipoDatiPagamentoPA.setBicAccredito(ctDatiSingoloVersamentoRP.getBicAccredito());
        paaTipoDatiPagamentoPA.setCredenzialiPagatore(ctDatiSingoloVersamentoRP.getCredenzialiPagatore());
        paaTipoDatiPagamentoPA.setEnteBeneficiario(buildEnteBeneficiario(ente));
      }
    } catch (java.lang.Exception ex) {
      log.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
      faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
      String faultString = StringUtils.left("Errore generico: " + ex.getCause(), 1024);
      String description = StringUtils.left("Errore generico: " + ex.getMessage(), 1024);
      faultBean.setFaultString(faultString);
      faultBean.setDescription(description);
      faultBean.setId(header.getIdentificativoDominio());
      esitoSILAttivaRP.setEsito(FaultCodeConstants.ESITO_KO);
      registraGiornaleEventoError.accept(faultBean);
      //TODO??? aggiunto SDM
      if (paaSILAttivaEsternaRisposta != null)
        annullaDovuto(ctVersamento, ente);
      throw new WSFaultResponseWrapperException(paaSILAttivaRPRisposta, ex);
    }

    try {
      String xmlString = jaxbTransformService.marshalling(paaSILAttivaRPRisposta, PaaSILAttivaRPRisposta.class);
      giornalePaService.registraEvento(
          null,
          header.getIdentificativoDominio(),
          header.getIdentificativoUnivocoVersamento(),
          header.getCodiceContestoPagamento(),
          bodyrichiesta.getIdentificativoPSP(),
          Constants.PAY_PRESSO_PSP,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_PA.paaSILAttivaRP.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
          header.getIdentificativoStazioneIntermediarioPA(),
          header.getIdentificativoDominio(),
          header.getIdentificativoStazioneIntermediarioPA(),
          Constants.EMPTY,
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("paaSILAttivaRP [RESPONSE] impossible to insert in the event log", e);
    }

    return paaSILAttivaRPRisposta;
  }


  private boolean isValidIdPSPAndTipoDovuto(EnteTipoDovuto enteTipoDovuto, String identificativoPSP) {
    if (!enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
      if (Constants.IDENTIFICATIVO_PSP_POSTE.equals(identificativoPSP)) {
        String ibanPostale = enteTipoDovuto.getIbanAccreditoPi();
        return !StringUtils.isBlank(ibanPostale);
      }
    }
    return true;
  }

  private String calcolaCausaleRispostaCCP(Dovuto dovuto) {
    String causale = StringUtils.isNotBlank(dovuto.getDeCausaleVisualizzata())
        ? dovuto.getDeCausaleVisualizzata() : dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento();
    return StringUtils.left(causale, 140);
  }

  private boolean checkDovutoPagabile(Dovuto dovuto, boolean flagScadenzaObbligatoria,
                                      FaultBean faultBean, String identificativoDominio) {

    // CONTROLLO DOVUTO GIA' INIZIATO
    if (Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO.equals(dovuto.getMygovAnagraficaStatoId().getCodStato())) {
      faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_IN_CORSO_CODE);
      faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_IN_CORSO_STRING);
      faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_IN_CORSO_STRING);
      faultBean.setId(identificativoDominio);
      faultBean.setSerial(0);
      return false;
    }

    // CONTROLLO DOVUTO SCADUTO
    boolean isDovutoScaduto = dovutoService.isDovutoScaduto(dovuto, flagScadenzaObbligatoria);
    if (isDovutoScaduto) {
      faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_SCADUTO_CODE);
      faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_SCADUTO_STRING);
      faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_SCADUTO_STRING);
      faultBean.setId(identificativoDominio);
      faultBean.setSerial(0);
      return false;
    }

    // STATO DIVERSO DA PAGABILE
    if (!Constants.STATO_DOVUTO_DA_PAGARE.equals(dovuto.getMygovAnagraficaStatoId().getCodStato())) {
      faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
      faultBean.setDescription("Il pagamento richiesto risulta in uno stato non pagabile");
      faultBean.setFaultString("Il pagamento richiesto risulta in uno stato non pagabile");
      faultBean.setId(identificativoDominio);
      faultBean.setSerial(0);
      return false;
    }

    return true;
  }

  private String getIbanAccredito(Ente ente, EnteTipoDovuto enteTipoDovuto, String identificativoPSP,
                                  String tipoVersamento) {
    if (!enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
      if (Constants.IDENTIFICATIVO_PSP_POSTE.equals(identificativoPSP)) {
        String ibanAccreditoTipoDovuto = enteTipoDovuto.getIbanAccreditoPi();

        // ACCREDITO
        if (StringUtils.isNotBlank(ibanAccreditoTipoDovuto)) {
          return ibanAccreditoTipoDovuto;
        }
        throw new RuntimeException(
            "Nessun IBAN postale associato al tipo dovuto [ " + enteTipoDovuto.getDeTipo() + " ]");
      } else if (Constants.PAY_MYBANK.equals(tipoVersamento)) {
        String ibanAccreditoPSPTipoDovuto = enteTipoDovuto.getIbanAccreditoPsp();
        String bicAccreditoPSPTipoDovuto = enteTipoDovuto.getBicAccreditoPsp();

        String ibanAccreditoPiTipoDovuto = enteTipoDovuto.getIbanAccreditoPi();
        String bicAccreditoPiTipoDovuto = enteTipoDovuto.getBicAccreditoPi();

        // ACCREDITO
        if (enteTipoDovuto.isBicAccreditoPspSeller() && (StringUtils.isNotBlank(ibanAccreditoPSPTipoDovuto)
            || StringUtils.isNotBlank(bicAccreditoPSPTipoDovuto))) {
          if (StringUtils.isNotBlank(ibanAccreditoPSPTipoDovuto)) {
            return ibanAccreditoPSPTipoDovuto;
          }
        } else if (enteTipoDovuto.isBicAccreditoPiSeller() && (StringUtils.isNotBlank(ibanAccreditoPiTipoDovuto)
            || StringUtils.isNotBlank(bicAccreditoPiTipoDovuto))) {
          if (StringUtils.isNotBlank(ibanAccreditoPiTipoDovuto)) {
            return ibanAccreditoPiTipoDovuto;
          }
        } else {
          if (ente.getCodRpDatiVersDatiSingVersBicAccreditoSeller()) {
            // Recupera dall'ente
            if (StringUtils.isNotBlank(ente.getCodRpDatiVersDatiSingVersIbanAccredito())) {
              return ente.getCodRpDatiVersDatiSingVersIbanAccredito();
            }
          } else {
            throw new RuntimeException("Errore nel recupero del iban di accredito per PSP MyBank");
          }
        }
      } else {
        String ibanAccreditoTipoDovuto = enteTipoDovuto.getIbanAccreditoPsp();
        String bicAccreditoTipoDovuto = enteTipoDovuto.getBicAccreditoPsp();

        // ACCREDITO
        if (StringUtils.isNotBlank(ibanAccreditoTipoDovuto) || StringUtils.isNotBlank(bicAccreditoTipoDovuto)) {
          if (StringUtils.isNotBlank(ibanAccreditoTipoDovuto)) {
            return ibanAccreditoTipoDovuto;
          }
        } else {
          // Recupera dall'ente
          if (StringUtils.isNotBlank(ente.getCodRpDatiVersDatiSingVersIbanAccredito())) {
            return ente.getCodRpDatiVersDatiSingVersIbanAccredito();
          }
        }
      }
    }
    return null;
  }

  private String getBicAccredito(Ente ente, EnteTipoDovuto enteTipoDovuto, String identificativoPSP,
                                 String tipoVersamento) {
    if (!enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
      if (Constants.IDENTIFICATIVO_PSP_POSTE.equals(identificativoPSP)) {
        String bicAccreditoTipoDovuto = enteTipoDovuto.getBicAccreditoPi();

        if (StringUtils.isNotBlank(bicAccreditoTipoDovuto)) {
          return bicAccreditoTipoDovuto;
        }
        throw new RuntimeException(
            "Nessun BIC postale associato al tipo dovuto [ " + enteTipoDovuto.getDeTipo() + " ]");
      } else if (Constants.PAY_MYBANK.equals(tipoVersamento)) {
        String ibanAccreditoPSPTipoDovuto = enteTipoDovuto.getIbanAccreditoPsp();
        String bicAccreditoPSPTipoDovuto = enteTipoDovuto.getBicAccreditoPsp();

        String ibanAccreditoPiTipoDovuto = enteTipoDovuto.getIbanAccreditoPi();
        String bicAccreditoPiTipoDovuto = enteTipoDovuto.getBicAccreditoPi();

        // ACCREDITO
        if (enteTipoDovuto.isBicAccreditoPspSeller() && (StringUtils.isNotBlank(ibanAccreditoPSPTipoDovuto)
            || StringUtils.isNotBlank(bicAccreditoPSPTipoDovuto))) {

          if (StringUtils.isNotBlank(bicAccreditoPSPTipoDovuto)) {
            return bicAccreditoPSPTipoDovuto;
          }
        } else if (enteTipoDovuto.isBicAccreditoPiSeller() && (StringUtils.isNotBlank(ibanAccreditoPiTipoDovuto)
            || StringUtils.isNotBlank(bicAccreditoPiTipoDovuto))) {

          if (StringUtils.isNotBlank(bicAccreditoPiTipoDovuto)) {
            return bicAccreditoPiTipoDovuto;
          }
        } else {
          if (ente.getCodRpDatiVersDatiSingVersBicAccreditoSeller()) {
            // Recupera dall'ente
            if (StringUtils.isNotBlank(ente.getCodRpDatiVersDatiSingVersBicAccredito())) {
              return ente.getCodRpDatiVersDatiSingVersBicAccredito();
            }
          } else {
            throw new RuntimeException("Errore nel recupero del bic di accredito per PSP MyBank");
          }
        }
      } else {
        String ibanAccreditoTipoDovuto = enteTipoDovuto.getIbanAccreditoPsp();
        String bicAccreditoTipoDovuto = enteTipoDovuto.getBicAccreditoPsp();

        // ACCREDITO
        if (StringUtils.isNotBlank(ibanAccreditoTipoDovuto) || StringUtils.isNotBlank(bicAccreditoTipoDovuto)) {
          if (StringUtils.isNotBlank(bicAccreditoTipoDovuto)) {
            return bicAccreditoTipoDovuto;
          }
        } else {
          // Recupera dall'ente
          if (StringUtils.isNotBlank(ente.getCodRpDatiVersDatiSingVersBicAccredito())) {
            return ente.getCodRpDatiVersDatiSingVersBicAccredito();
          }
        }
      }
    }
    return null;
  }

  private void annullaDovuto(Versamento ctDatiVersamento, Ente ente) {
    String nomeFlusso = "_" + ente.getCodIpaEnte() + "_IMPORT-DOVUTO";
    Flusso flusso = flussoService.getByIuf(nomeFlusso).get();
    AnagraficaStato anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_ANNULLATO,
        Constants.STATO_TIPO_DOVUTO);

    BigDecimal commissioneCaricoPA = null;
    if (ctDatiVersamento.getDatiVersamento().getCommissioneCaricoPA() == null) {
      commissioneCaricoPA = BigDecimal.ZERO;
    } else {
      commissioneCaricoPA = ctDatiVersamento.getDatiVersamento().getCommissioneCaricoPA();
    }

    dovutoElaboratoService.callAnnullaFunction(ente.getMygovEnteId(), flusso.getMygovFlussoId(), 0, anagraficaStato.getMygovAnagraficaStatoId(),
        null, ctDatiVersamento.getDatiVersamento().getIdentificativoUnivocoDovuto(), ctDatiVersamento.getDatiVersamento().getIdentificativoUnivocoVersamento(),
        new Date(), "-", ctDatiVersamento.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString(),
        ctDatiVersamento.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco(),
        ctDatiVersamento.getSoggettoPagatore().getAnagraficaPagatore(),
        Utilities.bonificaIndirizzoAnagrafica(ctDatiVersamento.getSoggettoPagatore().getIndirizzoPagatore()),
        Utilities.bonificaCivicoAnagrafica(ctDatiVersamento.getSoggettoPagatore().getCivicoPagatore()),
        ctDatiVersamento.getSoggettoPagatore().getCapPagatore(), ctDatiVersamento.getSoggettoPagatore().getLocalitaPagatore(),
        ctDatiVersamento.getSoggettoPagatore().getProvinciaPagatore(), ctDatiVersamento.getSoggettoPagatore().getNazionePagatore(),
        ctDatiVersamento.getSoggettoPagatore().getEMailPagatore(),
        ctDatiVersamento.getDatiVersamento().getDataEsecuzionePagamento() != null ? ctDatiVersamento.getDatiVersamento().getDataEsecuzionePagamento().toGregorianCalendar().getTime() : null,
        ctDatiVersamento.getDatiVersamento().getTipoVersamento(), ctDatiVersamento.getDatiVersamento().getImportoSingoloVersamento().doubleValue(),
        commissioneCaricoPA.doubleValue(), ctDatiVersamento.getDatiVersamento().getIdentificativoTipoDovuto(),
        ctDatiVersamento.getDatiVersamento().getCausaleVersamento(),
        ctDatiVersamento.getDatiVersamento().getDatiSpecificiRiscossione(), utenteService.getByCodFedUserId(ente.getCodIpaEnte() + "-" + Constants.WS_USER).get().getMygovUtenteId(),
        true);
  }

  private CtEnteBeneficiario buildEnteBeneficiario(Ente ente) {

    // l'ente sull'rp nn c'e' (prendere da tabella ente)
    CtEnteBeneficiario enteBeneficiario = new CtEnteBeneficiario();

    CtIdentificativoUnivocoPersonaG idUnivocoPersonaG = new CtIdentificativoUnivocoPersonaG();
    idUnivocoPersonaG.setCodiceIdentificativoUnivoco(ente.getCodiceFiscaleEnte());
    enteBeneficiario.setIdentificativoUnivocoBeneficiario(idUnivocoPersonaG);

    enteBeneficiario.setDenominazioneBeneficiario(ente.getDeNomeEnte());
    return enteBeneficiario;
  }

  private AnagraficaPagatore estraiAnagrafica(char tipoIdentificativoUnivoco, String codiceIdentificativoUnivoco,
                                              String anagraficaPagatoreString, String eMailPagatore, String indirizzoPagatore, String civicoPagatore,
                                              String capPagatore, String localitaPagatore, String provinciaPagatore, String nazionePagatore) {

    return AnagraficaPagatore.builder()
      .tipoIdentificativoUnivoco(tipoIdentificativoUnivoco)
      .codiceIdentificativoUnivoco(codiceIdentificativoUnivoco)
      .anagrafica(anagraficaPagatoreString)
      .email(eMailPagatore)
      .indirizzo(indirizzoPagatore)
      .civico(civicoPagatore)
      .cap(capPagatore)
      .nazione(nazionePagatore)
      .provincia(provinciaPagatore)
      .localita(localitaPagatore)
      .build();
  }

}
