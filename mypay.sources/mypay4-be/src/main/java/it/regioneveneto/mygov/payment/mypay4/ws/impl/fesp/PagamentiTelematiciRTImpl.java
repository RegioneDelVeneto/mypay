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

import gov.telematici.pagamenti.ws.nodoregionaleperspc.EsitoPaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionaleperspc.FaultBean;
import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.IntestazionePPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediCopiaRT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediCopiaRTRisposta;
import it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloPagamentoRT;
import it.gov.digitpa.schemas._2011.pagamenti.RT;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.client.PagamentiTelematiciEsitoClient;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciRPTClient;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRT;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciEsitoImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.regioneveneto.mygov.payment.mypay4.ws.util.ManageWsFault;
import it.veneto.regione.pagamenti.pa.EsitoPaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsitoRisposta;
import it.veneto.regione.schemas._2012.pagamenti.CtDatiSingoloPagamentoEsito;
import it.veneto.regione.schemas._2012.pagamenti.Esito;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Service("PagamentiTelematiciRTFespImpl")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciRTImpl implements PagamentiTelematiciRT {

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  @Value("${nodoRegionaleFesp.password}")
  private String password;

  @Autowired
  private EsitoBuilderService esitoBuilderService;
  @Autowired
  private RptRtService rptRtService;
  @Autowired
  private JAXBTransformService jaxbTransformService;
  @Autowired
  private RptRtDettaglioService rptRtDettaglioService;
  @Autowired
  private RpEService rpEService;
  @Autowired
  private RpEDettaglioService rpEDettaglioService;
  @Autowired
  private PagamentiTelematiciRPTClient pagamentiTelematiciRPTClient;
  @Autowired
  private PagamentiTelematiciEsitoImpl pagamentiTelematiciEsitoImpl;
  @Autowired
  private PagamentiTelematiciEsitoClient pagamentiTelematiciEsitoClient;
  @Autowired
  private RPTConservazioneService rptConservazioneService;
  @Autowired
  private EnteService enteFespService;
  @Autowired
  private SystemBlockService systemBlockService;

  public PaaInviaRTRisposta paaInviaRT(PaaInviaRT bodyrichiesta, IntestazionePPT header) {
    log.info("Executing operation paaInviaRT");
    return processRT(bodyrichiesta, header);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public Boolean askRT(RptRt rpt) {
    try {
      NodoChiediCopiaRT body = new NodoChiediCopiaRT();
      body.setIdentificativoIntermediarioPA(identificativoIntermediarioPA);
      body.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPA);
      body.setPassword(password);
      body.setIdentificativoDominio(rpt.getCodRptInviarptIdDominio());
      body.setCodiceContestoPagamento(rpt.getCodRptInviarptCodiceContestoPagamento());
      body.setIdentificativoUnivocoVersamento(rpt.getCodRptInviarptIdUnivocoVersamento());

      NodoChiediCopiaRTRisposta nodoChiediCopiaRTRisposta = pagamentiTelematiciRPTClient.nodoChiediCopiaRT(body);
      if (nodoChiediCopiaRTRisposta.getFault() != null) {
        gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean fault = nodoChiediCopiaRTRisposta.getFault();
        log.error("fault returned on nodoChiediCopiaRT rptId[{}]: {}", rpt.getMygovRptRtId(), ToStringBuilder.reflectionToString(fault));
        return Boolean.FALSE;
      } else {
        PaaInviaRT paaInviaRT = new PaaInviaRT();
        paaInviaRT.setRt(IOUtils.toByteArray(nodoChiediCopiaRTRisposta.getRt().getInputStream()));
        paaInviaRT.setTipoFirma(Constants.EMPTY);
        IntestazionePPT intestazionePPT = new IntestazionePPT();
        intestazionePPT.setIdentificativoDominio(rpt.getCodRptInviarptIdDominio());
        intestazionePPT.setIdentificativoIntermediarioPA(rpt.getCodRptInviarptIdIntermediarioPa());
        intestazionePPT.setIdentificativoStazioneIntermediarioPA(rpt.getCodRptInviarptIdStazioneIntermediarioPa());
        intestazionePPT.setIdentificativoUnivocoVersamento(rpt.getCodRptInviarptIdUnivocoVersamento());
        intestazionePPT.setCodiceContestoPagamento(rpt.getCodRptInviarptCodiceContestoPagamento());
      PaaInviaRTRisposta paaInviaRTRisposta = this.processRT(paaInviaRT, intestazionePPT);
        if (log.isInfoEnabled())
          log.info("RT received rptId[{}]: {}", rpt.getMygovRptRtId(), ToStringBuilder.reflectionToString(paaInviaRTRisposta));
        return Boolean.TRUE;
      }
    } catch(Exception e){
      log.error("exception when processing askRt with id: {} - idDominio: {} - IUV: {} - ccp: {}",
        rpt.getMygovRptRtId(),
        rpt.getCodRptInviarptIdDominio(),
        rpt.getCodRptInviarptIdUnivocoVersamento(),
        rpt.getCodRptInviarptCodiceContestoPagamento(),
        e);
      return null;
    }
  }

  private static final ThreadLocal<Esito> fakeCtEsito = new ThreadLocal<>();
  public static Esito getFakeCtEsito(){
    return fakeCtEsito.get();
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public PaaInviaRTRisposta forwardFakeRT(RT rt, IntestazionePPT intestazionePPT) {
    PaaInviaRT paaInviaRT = new PaaInviaRT();
    paaInviaRT.setTipoFirma(Constants.EMPTY);
    paaInviaRT.setRt(jaxbTransformService.marshallingAsBytes(rt, RT.class));
    try{
      fakeCtEsito.remove();
      Esito ctEsito = esitoBuilderService.buildEsito(rt);
      fakeCtEsito.set(ctEsito);
    }catch(Exception e){
      log.warn("cannot set ctEsito for dominio[{}] iuv[{}] ccp[{}]",
        intestazionePPT.getIdentificativoDominio(),
        intestazionePPT.getIdentificativoUnivocoVersamento(),
        intestazionePPT.getCodiceContestoPagamento(), e);
    }
    try {
      return this.processRT(paaInviaRT, intestazionePPT);
    } finally {
      fakeCtEsito.remove();
    }
  }

  private PaaInviaRTRisposta processRT(PaaInviaRT bodyrichiesta, IntestazionePPT header) {
    Esito ctEsito;
    it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT paaSILInviaEsitoHeader;
    RT rt;
    // tipoFirma deprecated by PagoPA, see here: https://www.agid.gov.it/sites/default/files/repository_files/modifichesanp_2_1.pdf
    byte[] rtPayloadInChiaro = bodyrichiesta.getRt();
    String tipoFirma = Constants.EMPTY;
    String identificativoDominio = header.getIdentificativoDominio();
    String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
    String codiceContestoPagamento = header.getCodiceContestoPagamento();

    ManageWsFault<PaaInviaRTRisposta> manageFault = (codeFault, faultString, faultDescr, error) -> {
      log.error(faultString, error);
      PaaInviaRTRisposta paaInviaRTRisposta = new PaaInviaRTRisposta();
      EsitoPaaInviaRT esitoPaaInviaRT = new EsitoPaaInviaRT();
      FaultBean faultBean = VerificationUtils.getNodoFaultBean(identificativoDominio,
          codeFault, faultString, faultDescr,1);
      esitoPaaInviaRT.setEsito(Constants.STATO_ESITO_KO);
      esitoPaaInviaRT.setFault(faultBean);
      paaInviaRTRisposta.setPaaInviaRTRisposta(esitoPaaInviaRT);
      return paaInviaRTRisposta;
    };
    BiFunction<String, String, PaaInviaRTRisposta> manageFaultBiFunction = manageFault::apply;

    BiFunction<PaaInviaRTRisposta, Long, PaaInviaRTRisposta> manageFaultWithUpdate = (response, idTable) -> {
      log.debug("manageFaultWithUpdate - saving rptRt with id: %d for response {}", jaxbTransformService.marshalling(response, PaaInviaRTRisposta.class));
      Optional.ofNullable(idTable).ifPresent(aLong -> rptRtService.updateRispostaRtById(response, aLong));
      return response;
    };

    PaaInviaRTRisposta response = new PaaInviaRTRisposta();
    log.trace("unmarshallingBase64 payload [{}]", rtPayloadInChiaro);
    try {
      rt = jaxbTransformService.unmarshalling(rtPayloadInChiaro, RT.class);
    } catch (MyPayException e) {
      String faultString = String.format("%s: Failed to parse Ricevuta Telematica ::: Exception :::", e.getClass().getName());
      String faultDescr = String.format("|%s|%s|", identificativoUnivocoVersamento, codiceContestoPagamento);
      return manageFault.apply(FaultCodeConstants.CODE_PAA_SYSTEM_ERROR, faultString, faultDescr, e);
    }

    //check blacklist/whitelist codice fiscale
    if(rt.getSoggettoPagatore()!=null && rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore()!=null &&
      systemBlockService.checkBlockByPayerCf(rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco())){
      //normally whitelist/blacklist is only needed during planned maintenance slot for allowing specific tests,
      // therefore the error message returned is quite generic
      String faultString = systemBlockService.getErrorMessage();
      return manageFault.apply(FaultCodeConstants.CODE_PAA_SYSTEM_ERROR, faultString);
    }

    RptRt rptRt = rptRtService.getRPTByCodRptIdMessaggioRichiesta(rt.getRiferimentoMessaggioRichiesta());
    if (rptRt == null) {
      String faultString = String.format("RPT non trovata per Identificativo Messaggio Richiesta [%s]" , rt.getRiferimentoMessaggioRichiesta());
      return manageFault.apply(FaultCodeConstants.PAA_RPT_SCONOSCIUTA, faultString);
    }

    // Se il CCP e' relativo ad una rpt_rt gia' presente ma l'identificativo dominio della rt e' differente da quello presente nella rpt_rt
    // stiamo parlando di una rt arrivata per un ente secondario intermediato da MyPay
    boolean isRtForSecondaryEnte = false;
    if (rptRt.getCodRptInviarptCodiceContestoPagamento().equals(rt.getDatiPagamento().getCodiceContestoPagamento()) &&
            !rptRt.getCodRptDomIdDominio().equals(rt.getDominio().getIdentificativoDominio())) {
      isRtForSecondaryEnte = true;
      log.debug("La RT per IUV [{}] e CCP [{}] e' relativa all'Ente secondario dominio [{}] e non all'Ente primario dominio [{}]",
              rt.getDatiPagamento().getIdentificativoUnivocoVersamento(),
              rt.getDatiPagamento().getCodiceContestoPagamento(),
              rt.getDominio().getIdentificativoDominio(), rptRt.getCodRptInviarptIdDominio());
    }
    if (isRtForSecondaryEnte) {
      log.debug("Non persisto RT per dominio [{}] e IUV [{}] perche' relativo ad Ente Secondario", identificativoDominio, identificativoUnivocoVersamento);
      //saveRt(rptRt.getMygovRptRtId(), header, bodyrichiesta, rt, rtPayloadInChiaro);
      EsitoPaaInviaRT esitoPaaInviaRT = new EsitoPaaInviaRT();
      esitoPaaInviaRT.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_OK);
      response.setPaaInviaRTRisposta(esitoPaaInviaRT);
    } else {
      String esitoRT = rt.getDatiPagamento().getCodiceEsitoPagamento();
      Optional<String> optionalEsitoInDB = Optional.ofNullable(rptRt.getCodRtDatiPagCodiceEsitoPagamento());
      // se in precedenza ho ricevuto una RT ed era ABORTITA
      boolean precedenteAbortita = optionalEsitoInDB.isPresent() && optionalEsitoInDB.get().equals("9");
      // Nel caso di una RT ricevuta a seguito di un invio RPT modello
      // "immediato" con esito risposta invio RPT "KO", memorizzare la RT
      // sul fesp e non propagarla a pa.
      boolean immediatoKO = rptRt.getDeRptInviarptEsito().equals("KO") && List.of(0, 1).contains(rptRt.getModelloPagamento());
      boolean modelloTreOK = esitoRT.equals("0") && rptRt.getModelloPagamento() == 4;

      if (precedenteAbortita && !esitoRT.equals("1")) {
        // pagato o parzialmente pagato rt con esito non possibile
        // (RT precedente ABORTITA e ora mi arriva un esito pagato o parzialmente pagato!!)
        String faultString = String.format("Pagamento con IUV: [%s] abortito", identificativoUnivocoVersamento);
        return manageFault.apply(FaultCodeConstants.PAA_SYSTEM_ERROR, faultString);
      } else if (rptRt.getCodRtInviartEsito() != null
              && (rptRt.getCodRtInviartEsito().equals(Constants.NODO_REGIONALE_FESP_ESITO_OK)
              || (rptRt.getCodRtInviartEsito().equals(Constants.NODO_REGIONALE_FESP_ESITO_KO)
              && rptRt.getCodRtInviartFaultCode().equals(FaultCodeConstants.PAA_RT_DUPLICATA)))) {
        // rt gia ricevuta, errore
        String faultString = String.format("RT gia ricevuta per IUV: %s", identificativoUnivocoVersamento);
        return manageFaultBiFunction
                .andThen(paaInviaRTRisposta -> manageFaultWithUpdate.apply(paaInviaRTRisposta, rptRt.getMygovRptRtId()))
                .apply(FaultCodeConstants.PAA_SYSTEM_ERROR, faultString);
      }
      log.debug("persisto RT per dominio [{}] e IUV [{}]", identificativoDominio, identificativoUnivocoVersamento);
      try {
        saveRt(rptRt.getMygovRptRtId(), header, bodyrichiesta, rt, rtPayloadInChiaro);
        // Se esito 0 o 2 deve essere presente la busta DatiSingoloPagamento
        if (List.of("0", "2").contains(esitoRT) && rt.getDatiPagamento().getDatiSingoloPagamentos().isEmpty()) {
          // rt con esito 0 o 2 ma con busta DatiSingoloPagamento non presente
          String faultString = String.format("RT con esito: %s non contiene DatiSingoloPagamento", esitoRT);
          return manageFaultBiFunction
                  .andThen(paaInviaRTRisposta -> manageFaultWithUpdate.apply(paaInviaRTRisposta, rptRt.getMygovRptRtId()))
                  .apply(FaultCodeConstants.PAA_SYSTEM_ERROR, faultString);
        }
        // Controlla che il numero di pagamenti sia coerente con rpt solo se busta esiste
        if (!rt.getDatiPagamento().getDatiSingoloPagamentos().isEmpty()) {
          List<CtDatiSingoloPagamentoRT> dovutiInRT = rt.getDatiPagamento().getDatiSingoloPagamentos();
          List<RptRtDettaglio> dovutiInRPT = rptRtDettaglioService.getByRptRtId(rptRt);
          if (dovutiInRT.size() != dovutiInRPT.size()) {
            String faultString = String.format("RT ricevuta per IUV: %s e Riferimento Messaggio Richiesta: %s " +
                            "contiene (%d) singoli pagamenti mentre l'RPT associata ne contiene (%d)"
                    , identificativoUnivocoVersamento, rt.getRiferimentoMessaggioRichiesta(), dovutiInRPT.size(), dovutiInRPT.size());
            return manageFaultBiFunction
                    .andThen(paaInviaRTRisposta -> manageFaultWithUpdate.apply(paaInviaRTRisposta, rptRt.getMygovRptRtId()))
                    .apply(FaultCodeConstants.PAA_SYSTEM_ERROR, faultString);
          }
        }
        if (!rptRtService.validaUguaglianzaCampiRptRt(rptRt, rt)) {
          String faultString = String.format("RT ricevuta per IUV: %s contiene dati non corrispondenti all RPT", identificativoUnivocoVersamento);
          return manageFault.apply(FaultCodeConstants.PAA_SYSTEM_ERROR, faultString);
        }
        log.debug("costruisco esito per dominio [{}] e IUV [{}]", identificativoDominio, identificativoUnivocoVersamento);
        ctEsito = esitoBuilderService.buildEsito(rt);
        paaSILInviaEsitoHeader = esitoBuilderService.buildHeaderEsito(header);
        log.debug("persisto esito per dominio [{}] e IUV [{}]", identificativoDominio, identificativoUnivocoVersamento);
        saveE(paaSILInviaEsitoHeader, ctEsito, rptRt.getMygovRpEId());
      } catch (Exception ex) {
        log.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
        String faultString = "error while saving Ricevuta Telematica ::: Exception :::";
        Long idTable = Optional.of(rptRt).map(RptRt::getMygovRptRtId).orElse(null);
        return manageFaultBiFunction
                .andThen(paaInviaRTRisposta -> manageFaultWithUpdate.apply(paaInviaRTRisposta, idTable))
                .apply(FaultCodeConstants.PAA_SYSTEM_ERROR, faultString);
      }
      PaaSILInviaEsito paaSILInviaEsitoBody = esitoBuilderService.buildBodyEsito(ctEsito, tipoFirma, rtPayloadInChiaro);

      // blocchiamo su FESP le RT del modello tre in stato positivo per velocizzare il modello 1
      // e risponder piu velocemente al nodo 3/9/19
      // le RT che blocchiamo verranno recuperate del batch chiediCopiaEsito di PA
      // chiamo PA
      PaaSILInviaEsitoRisposta paaSILInviaEsitoRisposta = getPaaSILInviaEsitoRisposta(paaSILInviaEsitoHeader, paaSILInviaEsitoBody, esitoRT, precedenteAbortita, immediatoKO, modelloTreOK);
      String paaSILInviaEsitoXmlString = jaxbTransformService.marshalling(paaSILInviaEsitoRisposta, PaaSILInviaEsitoRisposta.class);
      log.debug("paaSILInviaEsito Response: {}", paaSILInviaEsitoXmlString);

      try {
        rpEService.updateRispostaEById(paaSILInviaEsitoRisposta, rptRt.getMygovRpEId());
        EsitoPaaInviaRT esitoPaaInviaRT = new EsitoPaaInviaRT();
        esitoPaaInviaRT.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_OK);
        response.setPaaInviaRTRisposta(esitoPaaInviaRT);
        rptRtService.updateRispostaRtById(response, rptRt.getMygovRptRtId());
      } catch (Exception ex) {
        log.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
        String faultString = "error while saving Esito ::: Exception :::";
        Long idTable = Optional.of(rptRt).map(RptRt::getMygovRptRtId).orElse(null);
        return manageFaultBiFunction
                .andThen(paaInviaRTRisposta -> manageFaultWithUpdate.apply(paaInviaRTRisposta, idTable))
                .apply(FaultCodeConstants.PAA_SYSTEM_ERROR, faultString);
      }
    }

    String codRtDomIdDominio = rt.getDominio().getIdentificativoDominio();
    String codRtIdMessaggioRicevuta = rt.getIdentificativoMessaggioRicevuta();
    Date dtRtDataOraMessaggioRicevuta = rt.getDataOraMessaggioRicevuta().toGregorianCalendar().getTime();
    String codRtEnteBenefIdUnivBenefCodiceIdUnivoco = rt.getEnteBeneficiario()
            .getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco();
    String codRtSoggVersIdUnivVersCodiceIdUnivocoString = null;
    String deRtSoggVersAnagraficaVersante = null;

    if (rt.getSoggettoVersante() != null) {
      codRtSoggVersIdUnivVersCodiceIdUnivocoString = rt.getSoggettoVersante().getIdentificativoUnivocoVersante()
              .getCodiceIdentificativoUnivoco();
      deRtSoggVersAnagraficaVersante = rt.getSoggettoVersante().getAnagraficaVersante();
    }
    String codRtSoggPagIdUnivPagTipoIdUnivoco = rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
            .getTipoIdentificativoUnivoco().toString();
    String codRtSoggPagIdUnivPagCodiceIdUnivoco = rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
            .getCodiceIdentificativoUnivoco();
    String deRtSoggPagAnagraficaPagatore = rt.getSoggettoPagatore().getAnagraficaPagatore();
    String deRtSoggPagEmailPagatore = rt.getSoggettoPagatore().getEMailPagatore();
    String codRtDatiPagCodiceEsitoPagamento = rt.getDatiPagamento().getCodiceEsitoPagamento();
    String codRtDatiPagIdUnivocoVersamento = rt.getDatiPagamento().getIdentificativoUnivocoVersamento();
    String codRtDatiPagCodiceContestoPagamento = rt.getDatiPagamento().getCodiceContestoPagamento();

    String idAggregazione = null;
    String causaleVersamento = null;
    if(!rt.getDatiPagamento().getDatiSingoloPagamentos().isEmpty()) {
      CtDatiSingoloPagamentoRT ctDatiSingoloPagamentoRT = rt.getDatiPagamento().getDatiSingoloPagamentos().get(0);
      idAggregazione = codRtDomIdDominio + "-" + ctDatiSingoloPagamentoRT.getDatiSpecificiRiscossione();
      causaleVersamento = ctDatiSingoloPagamentoRT.getCausaleVersamento();
    }

    Long mygovRptRtId = rptRt.getMygovRptRtId();

    RT_Conservazione rtConservazione =rptConservazioneService.insertRtConservazione(mygovRptRtId, codRtDomIdDominio, codRtDatiPagIdUnivocoVersamento,
            codRtDatiPagCodiceContestoPagamento, codRtIdMessaggioRicevuta, rtPayloadInChiaro, dtRtDataOraMessaggioRicevuta,
            causaleVersamento, codRtSoggPagIdUnivPagTipoIdUnivoco,
            deRtSoggPagAnagraficaPagatore, codRtSoggPagIdUnivPagCodiceIdUnivoco, deRtSoggPagEmailPagatore,
            codRtEnteBenefIdUnivBenefCodiceIdUnivoco, idAggregazione, codRtSoggVersIdUnivVersCodiceIdUnivocoString,
            deRtSoggVersAnagraficaVersante, codRtDatiPagCodiceEsitoPagamento,
            null);
    log.debug("CONSERVAZIONE: "+rtConservazione);

    return response;
  }


  private PaaSILInviaEsitoRisposta getPaaSILInviaEsitoRisposta(it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT header, PaaSILInviaEsito paaSILInviaEsitoBody, String esitoRT, boolean precedenteAbortita, boolean immediatoKO, boolean modelloTreOK) {
    PaaSILInviaEsitoRisposta paaSILInviaEsitoRisposta = new PaaSILInviaEsitoRisposta();
    EsitoPaaSILInviaEsito esitoPaaSILInviaEsito = new EsitoPaaSILInviaEsito();
    if (!precedenteAbortita && !immediatoKO && !modelloTreOK) {
      log.debug("invoco paaSILInviaEsito per dominio [{}] e IUV [{}]", header.getIdentificativoDominio(), header.getIdentificativoUnivocoVersamento());
      // INVIA ESITO --->> PA
      try{
        Ente ente = enteFespService.getEnteByCodFiscale(header.getIdentificativoDominio());
        String urlPaRemote = ente.getDeUrlEsterniAttiva(); //TODO: add a relevant field for remote PA url
        if(StringUtils.isNotBlank(urlPaRemote)) {
          // scenario where PA is on a remote location (toward this FESP installation)
          paaSILInviaEsitoRisposta = pagamentiTelematiciEsitoClient.paaSILInviaEsito(paaSILInviaEsitoBody, header, urlPaRemote);
        } else {
          // scenario where PA is on the same local installation of FESP
          paaSILInviaEsitoRisposta = pagamentiTelematiciEsitoImpl.paaSILInviaEsito(paaSILInviaEsitoBody, header);
        }
      } catch (Exception ex) {
        log.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
        // se la chiamata a PA solleva eccezione torno OK al nodo in modo tale che il nodo
        // non reinoltri piu'. L' esito sara' poi recuperato da BatchChiediCopiaEsito
        esitoPaaSILInviaEsito.setEsito(Constants.GIORNALE_ESITO_EVENTO.KO.toString());
        it.veneto.regione.pagamenti.pa.FaultBean faultBean = VerificationUtils.getPaFaultBean(header.getIdentificativoDominio(),
            FaultCodeConstants.PAA_SYSTEM_ERROR, "eccezione in chiamata paaSILInviaEsito", ex.getMessage(), 1);
        esitoPaaSILInviaEsito.setFault(faultBean);
        paaSILInviaEsitoRisposta.setPaaSILInviaEsitoRisposta(esitoPaaSILInviaEsito);
      }
    } else {
      log.debug("non invoco paaSILInviaEsito per dominio [{}] e IUV [{}]", header.getIdentificativoDominio(), header.getIdentificativoUnivocoVersamento());
      if (List.of("0","2").contains(esitoRT)) {
        String msg = String.format("Ricevuto esito pagamento positivo con precedenteAbortita [%s], " +
            "immediatoKO [%s], modelloTreOK [%s], esitoRT [%s]", precedenteAbortita, immediatoKO, modelloTreOK, esitoRT);
        if(modelloTreOK)
          log.debug(msg);
        else
          log.error(msg);
      }
      it.veneto.regione.pagamenti.pa.FaultBean faultBean = VerificationUtils.getPaFaultBean(header.getIdentificativoDominio(),
          FaultCodeConstants.PAA_NOT_INVOKED, "PA non invocato", String.format("RT precedenteAbortita [%s], " +
              "immediatoKO [%s], modelloTreOK [%s], esitoRT [%s]", precedenteAbortita, immediatoKO, modelloTreOK, esitoRT), 1);
      esitoPaaSILInviaEsito.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
      esitoPaaSILInviaEsito.setFault(faultBean);
      paaSILInviaEsitoRisposta.setPaaSILInviaEsitoRisposta(esitoPaaSILInviaEsito);
    }
    return paaSILInviaEsitoRisposta;
  }

  private void saveRt(Long mygovRptRtId, IntestazionePPT header, PaaInviaRT bodyrichiesta, RT rt, byte[] rtPayload) {

    RptRt rptRt = rptRtService.updateRtById(mygovRptRtId, header, bodyrichiesta, rt, rtPayload);
    log.debug("updateRtByRptIuv lettura caricamento singoli pagamenti");
    List<CtDatiSingoloPagamentoRT> list = rt.getDatiPagamento().getDatiSingoloPagamentos();
    if (!list.isEmpty()) {
      rptRtDettaglioService.updateDateRt(rptRt, list);
    }
    log.debug("fine updateRtByRptIuv ");
  }

  private void saveE(it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT header, Esito esito, Long mygovRpEId) {
    RpE rpe = rpEService.updateEById(mygovRpEId, esito, header);
    log.debug("updateE lettura caricamento singoli pagamenti");
    List<CtDatiSingoloPagamentoEsito> list = esito.getDatiPagamento().getDatiSingoloPagamentos();
    if (!list.isEmpty()) {
      rpEDettaglioService.updateDateE(rpe, list);
    }
    log.debug("fine updateE");
  }
}
