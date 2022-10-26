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
package it.regioneveneto.mygov.payment.mypay4.scheduled.chiedistatorpt;

import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediStatoRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediStatoRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory;
import gov.telematici.pagamenti.ws.ppthead.IntestazionePPT;
import it.gov.digitpa.schemas._2011.pagamenti.RT;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivoco;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersFG;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersG;
import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.NodoInviaRPTService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.RptRtService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciRPTImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciRTImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeChiediStatoRPT;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeInvioRPT;
import it.regioneveneto.mygov.payment.mypay4.ws.util.StatiRPT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue=ChiediStatoRPTTaskApplication.NAME)
public class ChiediStatoRPTTaskService {

  private static final String FORZA_GENERAZIONE_RT_NEGATIVA = "FORZA_GENERAZIONE_RT_NEGATIVA";

  public enum ACTIONS {
    NO_ACTION, FORZA_RT_NEGATIVA, CHIEDI_STATO_RPT, RIATTIVA_RPT, CAMBIA_STATO_RPT_OK, CHIEDI_COPIA_RT, POSSIBILE_RT_NEGATIVA, POSSIBILE_RT_NEGATIVA_CON_TENTATIVI
  }

  @Value("${task.chiediStatoRPT.forzaRTNegativa:false}")
  private boolean forceRTNegativaParam;

  @Autowired
  private NodoInviaRPTService nodoInviaRPTService;

  @Resource
  private ChiediStatoRPTTaskService self;

  @Autowired
  private RptRtService rptRtService;

  @Autowired
  private PagamentiTelematiciRPTImpl pagamentiTelematiciRPT;

  @Autowired
  private PagamentiTelematiciRTImpl pagamentiTelematiciRT;

  @Autowired
  private JAXBTransformService jaxbTransformService;

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  @Value("${nodoRegionaleFesp.password}")
  private String password;

  @Value("${task.chiediStatoRPT.maxDaysBeforeRtNegativa}")
  private int maxDaysBeforeRtNegativa;

  @Value("${task.chiediStatoRPT.maxDaysBeforeAutoRtNegativa}")
  private int maxDaysBeforeAutoRtNegativa;



  @Value("${task.chiediStatoRPT.minTriesBeforeRtNegativa}")
  private int minTriesBeforeRtNegativa;

  @Value("${task.chiediStatoRPT.skipCloseRtNegativa: false}")
  private boolean skipCloseRtNegativa;

  private final static long MS_TO_MINUTES = 1000*60;
  private final static long MS_TO_HOURS = MS_TO_MINUTES*60;
  private final static long MS_TO_DAYS = MS_TO_HOURS*24;

  private static final String[] ERROR_CODES_FORCE_RT_NEGATIVA = {
      FaultCodeInvioRPT.PPT_SINTASSI_XSD.getFaultCode(),
      FaultCodeInvioRPT.PPT_SINTASSI_EXTRAXSD.getFaultCode(),
      FaultCodeInvioRPT.PPT_AUTENTICAZIONE.getFaultCode(),
      FaultCodeInvioRPT.PPT_AUTORIZZAZIONE.getFaultCode(),
      FaultCodeInvioRPT.PPT_SEMANTICA.getFaultCode(),
      FaultCodeInvioRPT.PPT_DOMINIO_SCONOSCIUTO.getFaultCode(),
      FaultCodeInvioRPT.PPT_DOMINIO_DISABILITATO.getFaultCode(),
      FaultCodeInvioRPT.PPT_STAZIONE_INT_PA_SCONOSCIUTA.getFaultCode(),
      FaultCodeInvioRPT.PPT_STAZIONE_INT_PA_DISABILITATA.getFaultCode(),
      FaultCodeInvioRPT.PPT_INTERMEDIARIO_PA_SCONOSCIUTO.getFaultCode(),
      FaultCodeInvioRPT.PPT_INTERMEDIARIO_PA_DISABILITATO.getFaultCode(),
      FaultCodeInvioRPT.PPT_CANALE_SCONOSCIUTO.getFaultCode(),
      FaultCodeInvioRPT.PPT_CANALE_IRRAGGIUNGIBILE.getFaultCode(),
      FaultCodeInvioRPT.PPT_CANALE_SERVIZIO_NONATTIVO.getFaultCode(),
      FaultCodeInvioRPT.PPT_CANALE_DISABILITATO.getFaultCode(),
      FaultCodeInvioRPT.PPT_CANALE_NONRISOLVIBILE.getFaultCode(),
      FaultCodeInvioRPT.PPT_CANALE_INDISPONIBILE.getFaultCode(),
      FaultCodeInvioRPT.PPT_CANALE_ERR_PARAM_PAG_IMM.getFaultCode(),
      FaultCodeInvioRPT.PPT_INTERMEDIARIO_PSP_SCONOSCIUTO.getFaultCode(),
      FaultCodeInvioRPT.PPT_INTERMEDIARIO_PSP_DISABILITATO.getFaultCode(),
      FaultCodeInvioRPT.PPT_PSP_SCONOSCIUTO.getFaultCode(),
      FaultCodeInvioRPT.PPT_PSP_DISABILITATO.getFaultCode(),
      FaultCodeInvioRPT.PPT_SUPERAMENTOSOGLIA.getFaultCode(),
      FaultCodeInvioRPT.PPT_TIPOFIRMA_SCONOSCIUTO.getFaultCode(),
      FaultCodeInvioRPT.PPT_ERRORE_FORMATO_BUSTA_FIRMATA.getFaultCode(),
      FaultCodeInvioRPT.PPT_FIRMA_INDISPONIBILE.getFaultCode(),
      FaultCodeInvioRPT.PPT_SYSTEM_ERROR.getFaultCode(),
      FaultCodeInvioRPT.PPT_IBAN_NON_CENSITO.getFaultCode()
  };

  private static final String[] ERROR_CODES_NO_ACTION = {
      FaultCodeInvioRPT.PPT_RPT_DUPLICATA.getFaultCode()
  };

  private static final String[] ERROR_CODES_CHIEDI_STATO_RPT_MODELLO_4 = {
      FaultCodeInvioRPT.PPT_CANALE_TIMEOUT.getFaultCode(),
      FaultCodeInvioRPT.PPT_CANALE_ERRORE_RESPONSE.getFaultCode(),
      FaultCodeInvioRPT.PPT_CANALE_ERRORE.getFaultCode(),
      FaultCodeInvioRPT.PPT_ESITO_SCONOSCIUTO.getFaultCode(),
      FaultCodeInvioRPT.PPT_SYSTEM_ERROR.getFaultCode()
  };

  private static final String[] STATES_CHIEDI_STATO_RPT_FORCE_RT_NEGATIVA = {
      StatiRPT.RPT_RIFIUTATA_NODO.name(),
      StatiRPT.RPT_RIFIUTATA_PSP.name(),
      StatiRPT.RPT_ERRORE_INVIO_A_PSP.name()
  };


  private final ObjectFactory objectFactoryNodoSpc = new ObjectFactory();
  private final it.gov.digitpa.schemas._2011.pagamenti.ObjectFactory objectFactoryPagamenti = new it.gov.digitpa.schemas._2011.pagamenti.ObjectFactory();

  private long counter = 0;
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.NEVER)
  public void chiediStatoRPT(){
    log.info("chiediStatoRPT start [{}]", ++counter);
    int[] totalOkOutcomeCount = {0};
    int[] totalFilteredCount = {0};
    int[] totalCount = {0};
    Carrello.VALID_MODELLOPAGAMENTO.forEach(modelloPagamento -> {
      List<RptRt> rptPendenti = rptRtService.getRPTPendentiForChiediStatoRPT(modelloPagamento);
      if(!rptPendenti.isEmpty()) {
        int[] filteredCount = {0};
        int[] okOutcomeCount = {0};
        log.info("RptRt pendenti for modello pagamento {} : {}", modelloPagamento, rptPendenti.size());
        rptPendenti.forEach(rptRt -> {
          Pair<Boolean,Integer> processingInfo = isRptToProcess(rptRt);
          if(processingInfo.getLeft()) {
            filteredCount[0]++;
            try {
              log.debug("Start processing rptRt with id: {} - idDominio: {} - IUV: {} - ccp: {}",
                rptRt.getMygovRptRtId(),
                rptRt.getCodRptInviarptIdDominio(),
                rptRt.getCodRptInviarptIdUnivocoVersamento(),
                rptRt.getCodRptInviarptCodiceContestoPagamento());
              // process RPT
              boolean outcome = self.processRpt(rptRt.getMygovRptRtId());
              if(outcome)
                okOutcomeCount[0]++;
              log.debug("End processing rptRt with id: {} - idDominio: {} - IUV: {} - ccp: {}",
                rptRt.getMygovRptRtId(),
                rptRt.getCodRptInviarptIdDominio(),
                rptRt.getCodRptInviarptIdUnivocoVersamento(),
                rptRt.getCodRptInviarptCodiceContestoPagamento());
            } catch (Exception e) {
              log.debug("Error processing rptRt with id: {} - idDominio: {} - IUV: {} - ccp: {}",
                rptRt.getMygovRptRtId(),
                rptRt.getCodRptInviarptIdDominio(),
                rptRt.getCodRptInviarptIdUnivocoVersamento(),
                rptRt.getCodRptInviarptCodiceContestoPagamento(),
                e);
            }
          }
        });
        totalOkOutcomeCount[0] += okOutcomeCount[0];
        totalFilteredCount[0] += filteredCount[0];
        totalCount[0] += rptPendenti.size();
        log.info("RptRt processed for modello pagamento {} : [{}/{}] okOutcomeCount[{}/{}]", modelloPagamento, filteredCount[0], rptPendenti.size(), okOutcomeCount[0], filteredCount[0]);
      }
    });
    log.info("chiediStatoRPT stop [{}] okOutcomeCount[{}/{}/{}]", counter, totalOkOutcomeCount[0], totalFilteredCount[0], totalCount[0]);
  }

  private Pair<Boolean,Integer> isRptToProcess(RptRt rptRt){
    long now = System.currentTimeMillis();
    return Optional.ofNullable(rptRtService.getProcessingInfoChiediStatoRpt(rptRt.getMygovRptRtId()))
      .map(processingInfo -> {
        int numChecks = Integer.parseInt(StringUtils.substringBefore(processingInfo, "|"));
        long lastCheck = Long.parseLong(StringUtils.substringAfter(processingInfo, "|"));
        long rptDt = ObjectUtils.firstNonNull(rptRt.getDtRptDataOraMessaggioRichiesta(), rptRt.getDtCreazioneRpt()).getTime();
        /*
          Frequency |  Num tries |  Total tries | Elapsed time | Total elapsed time
          -------------------------------------------------------------------------
                5m  |        12  |           12 |           1h |               1h
               15m  |         8  |           20 |           2h |               3h
                1h  |        21  |           41 |          21h |               1d
                6h  |        24  |           65 |           6d |               7d
                1d  |         *  |            * |            * |              >7d
         */
        long totalElapsed = now - rptDt;
        boolean toProcess;
        String frequency;
        if (totalElapsed < 1 * MS_TO_HOURS) {
          toProcess = (now - lastCheck) > 5 * MS_TO_MINUTES;
          frequency = "5m";
        } else if (totalElapsed < 3 * MS_TO_HOURS) {
          toProcess = (now - lastCheck) > 15 * MS_TO_MINUTES;
          frequency = "10m";
        } else if (totalElapsed < 1 * MS_TO_DAYS) {
          toProcess = (now - lastCheck) > 1 * MS_TO_HOURS;
          frequency = "1h";
        } else if (totalElapsed < 7 * MS_TO_DAYS) {
          toProcess = (now - lastCheck) > 6 * MS_TO_HOURS;
          frequency = "6h";
        } else {
          toProcess = (now - lastCheck) > 1 * MS_TO_DAYS;
          frequency = "1d";
        }
        log.debug("rptId [{}] toProcess[{}] numChecks[{}] Drpt[{}] DlastCheck[{}] frequency[{}]",
          rptRt.getMygovRptRtId(), toProcess, numChecks, DurationFormatUtils.formatDuration(totalElapsed, "dd-HH:mm:ss"),
          DurationFormatUtils.formatDuration(now - lastCheck, "dd-HH:mm:ss"), frequency);
        return Pair.of(toProcess, numChecks);
      })
      .orElseGet(() -> {
        log.debug("rptId [{}] Drpt[{}] not found in processing info cache", rptRt.getMygovRptRtId(),
          DurationFormatUtils.formatDuration(now - ObjectUtils.firstNonNull(rptRt.getDtRptDataOraMessaggioRichiesta(), rptRt.getDtCreazioneRpt()).getTime(),"dd-HH:mm:ss"));
        return Pair.of(true, 0);
      });
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
  public boolean processRpt(Long rptRtId) {
    boolean[] outcome = {false};

    rptRtService.getByIdLockOrSkip(rptRtId).ifPresentOrElse(rpt -> {

      //check again if is to process
      Pair<Boolean, Integer> isToProcess = this.isRptToProcess(rpt);

      if(!isToProcess.getLeft()){
        return;
      }

      //update processing info for carrello
      rptRtService.updateProcessingInfoChiediStatoRpt(rpt.getMygovRptRtId(), isToProcess.getRight()+1);

      ACTIONS action;
      if("OK".equals(rpt.getDeRptInviarptEsito())){
        //chiedi copia RT
        action = ACTIONS.CHIEDI_COPIA_RT;
      } else {
        if (rpt.getModelloPagamento() == 0 || rpt.getModelloPagamento() == 1 || rpt.getModelloPagamento() == 2) {
          if (StringUtils.isNotBlank(rpt.getCodRptInviarptFaultCode())) {
            if (forceRTNegativaParam && rpt.getCodRptInviarptFaultCode().equals(FORZA_GENERAZIONE_RT_NEGATIVA) ||
              ArrayUtils.contains(ERROR_CODES_FORCE_RT_NEGATIVA, rpt.getCodRptInviarptFaultCode()))
              action = ACTIONS.FORZA_RT_NEGATIVA;
            else if (ArrayUtils.contains(ERROR_CODES_NO_ACTION, rpt.getCodRptInviarptFaultCode()))
              action = ACTIONS.POSSIBILE_RT_NEGATIVA_CON_TENTATIVI;
            else
              action = ACTIONS.CHIEDI_STATO_RPT;
          } else
            action = ACTIONS.POSSIBILE_RT_NEGATIVA;
        } else if (rpt.getModelloPagamento() == 4) {
          if (StringUtils.isNotBlank(rpt.getCodRptInviarptFaultCode())) {
            if (ArrayUtils.contains(ERROR_CODES_CHIEDI_STATO_RPT_MODELLO_4, rpt.getCodRptInviarptFaultCode()))
              action = ACTIONS.CHIEDI_STATO_RPT;
            else if (forceRTNegativaParam && rpt.getCodRptInviarptFaultCode().equals(FORZA_GENERAZIONE_RT_NEGATIVA) ||
              ArrayUtils.contains(ERROR_CODES_FORCE_RT_NEGATIVA, rpt.getCodRptInviarptFaultCode()))
              action = ACTIONS.FORZA_RT_NEGATIVA;
            else
              action = ACTIONS.POSSIBILE_RT_NEGATIVA_CON_TENTATIVI;
          } else
            action = ACTIONS.POSSIBILE_RT_NEGATIVA;
        } else {
          throw new MyPayException(String.format("invalid modelloPagamento [%d] for rpt id:%d", rpt.getModelloPagamento(), rpt.getMygovRptRtId()));
        }
      }
      log.debug("processing rpt id:{}, action:{}", rpt.getMygovRptRtId(), action);

      if(action.equals(ACTIONS.CHIEDI_COPIA_RT)){
        Boolean rtReceived = pagamentiTelematiciRT.askRT(rpt);
        if(Objects.equals(rtReceived, Boolean.TRUE))
          outcome[0] = true;
        else if(Objects.equals(rtReceived, Boolean.FALSE))
          action = ACTIONS.POSSIBILE_RT_NEGATIVA;
        else {
          //if rtReceived is null, it's an unmanaged exception: retry at least N times
          action = ACTIONS.POSSIBILE_RT_NEGATIVA_CON_TENTATIVI;
        }
      }

      if(action.equals(ACTIONS.CHIEDI_STATO_RPT)){

        NodoChiediStatoRPT body = objectFactoryNodoSpc.createNodoChiediStatoRPT();
        body.setIdentificativoIntermediarioPA(identificativoIntermediarioPA);
        body.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPA);
        body.setPassword(password);
        body.setIdentificativoDominio(rpt.getCodRptInviarptIdDominio());
        body.setCodiceContestoPagamento(rpt.getCodRptInviarptCodiceContestoPagamento());
        body.setIdentificativoUnivocoVersamento(rpt.getCodRptInviarptIdUnivocoVersamento());

        NodoChiediStatoRPTRisposta response = pagamentiTelematiciRPT.nodoChiediStatoRPT(body);

        action = ACTIONS.POSSIBILE_RT_NEGATIVA;
        if(response.getFault()==null && response.getEsito()!=null && StringUtils.equals(response.getEsito().getStato(), StatiRPT.RPT_RICEVUTA_NODO.name())) {
          action = ACTIONS.CAMBIA_STATO_RPT_OK;
        } else if(rpt.getModelloPagamento()==0 || rpt.getModelloPagamento()==1) {
          if( response.getFault()!=null && FaultCodeChiediStatoRPT.PPT_RPT_SCONOSCIUTA.getFaultCode().equals(response.getFault().getFaultCode())
            || response.getEsito() != null && StringUtils.isNotBlank(response.getEsito().getStato()) && ArrayUtils.contains(STATES_CHIEDI_STATO_RPT_FORCE_RT_NEGATIVA,response.getEsito().getStato()) )
            action = ACTIONS.FORZA_RT_NEGATIVA;
        } else if(rpt.getModelloPagamento()==2) {
          if( response.getFault()!=null && FaultCodeChiediStatoRPT.PPT_RPT_SCONOSCIUTA.getFaultCode().equals(response.getFault().getFaultCode()) )
            action = ACTIONS.RIATTIVA_RPT;
          else if( response.getEsito() != null && StringUtils.isNotBlank(response.getEsito().getStato()) && ArrayUtils.contains(STATES_CHIEDI_STATO_RPT_FORCE_RT_NEGATIVA,response.getEsito().getStato()) )
            action = ACTIONS.FORZA_RT_NEGATIVA;
        } else if(rpt.getModelloPagamento()==4) {
          if (response.getFault() != null && FaultCodeChiediStatoRPT.PPT_RPT_SCONOSCIUTA.getFaultCode().equals(response.getFault().getFaultCode()))
            action = ACTIONS.RIATTIVA_RPT;
        }

      }

      if(action.equals(ACTIONS.CAMBIA_STATO_RPT_OK)){
        rptRtService.setInviarptEsitoOkById(rpt.getMygovRptRtId());
        //TODO check if correct
        outcome[0] = true;
        log.debug("setInviarptEsitoOkById rptId:{}", rpt.getMygovRptRtId());
      }

      if(action.equals(ACTIONS.RIATTIVA_RPT)){
        rptRtService.clearInviarptEsitoById(rpt.getMygovRptRtId());
        //TODO check if correct
        outcome[0] = true;
        log.debug("clearInviarptEsitoById rptId:{}", rpt.getMygovRptRtId());
      }

      //if older than N days, force RT negativa
      if(action.equals(ACTIONS.POSSIBILE_RT_NEGATIVA) || action.equals(ACTIONS.POSSIBILE_RT_NEGATIVA_CON_TENTATIVI)) {
        long rptAge = System.currentTimeMillis() - ObjectUtils.firstNonNull(rpt.getDtRptDataOraMessaggioRichiesta(), rpt.getDtCreazioneRpt()).getTime();
        if (rptAge > maxDaysBeforeRtNegativa * MS_TO_DAYS) {
          int numTries = isToProcess.getRight() + 1;
          boolean autoRtNegativa = rptAge > maxDaysBeforeAutoRtNegativa * MS_TO_DAYS;
          String skipMsg = "set";
          if(skipCloseRtNegativa)
            skipMsg = "NOT set (SKIPPED by param)";
          else if(numTries < minTriesBeforeRtNegativa && !autoRtNegativa)
            skipMsg = "NOT set (SKIPPED by numTries)";
          else {
            if(numTries < minTriesBeforeRtNegativa && autoRtNegativa)
              skipMsg = "set (by autoRtNegativa)";
            action = ACTIONS.FORZA_RT_NEGATIVA;
          }
          log.info("{} to FORZA_RT_NEGATIVA because RPT is older than {} days and {} tries - id[{}] age[{}] tries[{}]",
            skipMsg, maxDaysBeforeRtNegativa, minTriesBeforeRtNegativa, rptRtId,
            DurationFormatUtils.formatDuration(rptAge, "dd-HH:mm:ss"), numTries);
        }
      }

      if (action.equals(ACTIONS.FORZA_RT_NEGATIVA)) {

        IntestazionePPT header = new IntestazionePPT();
        header.setIdentificativoDominio(rpt.getCodRptInviarptIdDominio());
        header.setIdentificativoUnivocoVersamento(rpt.getCodRptInviarptIdUnivocoVersamento());
        header.setCodiceContestoPagamento(rpt.getCodRptInviarptCodiceContestoPagamento());
        header.setIdentificativoIntermediarioPA(identificativoIntermediarioPA);
        header.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPA);

        RT rt = objectFactoryPagamenti.createRT();
        rt.setVersioneOggetto(rpt.getDeRptVersioneOggetto());
        rt.setDominio(objectFactoryPagamenti.createCtDominio());
        rt.getDominio().setIdentificativoDominio(rpt.getCodRptDomIdDominio());
        rt.getDominio().setIdentificativoStazioneRichiedente(rpt.getCodRptDomIdStazioneRichiedente());
        rt.setIdentificativoMessaggioRicevuta("###" + Utilities.getRandomicUUID());
        rt.setDataOraMessaggioRicevuta(Utilities.toXMLGregorianCalendar(new Date()));
        rt.setRiferimentoMessaggioRichiesta(rpt.getCodRptIdMessaggioRichiesta());
        rt.setRiferimentoDataRichiesta(Utilities.toXMLGregorianCalendar(rpt.getDtRptDataOraMessaggioRichiesta()));

        rt.setIstitutoAttestante(objectFactoryPagamenti.createCtIstitutoAttestante());
        rt.getIstitutoAttestante().setIdentificativoUnivocoAttestante(objectFactoryPagamenti.createCtIdentificativoUnivoco());
        rt.getIstitutoAttestante().getIdentificativoUnivocoAttestante().setCodiceIdentificativoUnivoco(rpt.getCodRptInviarptIdPsp());
        rt.getIstitutoAttestante().getIdentificativoUnivocoAttestante().setTipoIdentificativoUnivoco(StTipoIdentificativoUnivoco.G);
        rt.getIstitutoAttestante().setDenominazioneAttestante("[" + rpt.getCodRptInviarptIdPsp() + "]");

        rt.setEnteBeneficiario(objectFactoryPagamenti.createCtEnteBeneficiario());
        rt.getEnteBeneficiario().setIdentificativoUnivocoBeneficiario(objectFactoryPagamenti.createCtIdentificativoUnivocoPersonaG());
        rt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().setCodiceIdentificativoUnivoco(rpt.getCodRptEnteBenefIdUnivBenefCodiceIdUnivoco());
        rt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.G);
        rt.getEnteBeneficiario().setDenominazioneBeneficiario(rpt.getDeRptEnteBenefDenominazioneBeneficiario());
        rt.getEnteBeneficiario().setCodiceUnitOperBeneficiario(rpt.getCodRptEnteBenefCodiceUnitOperBeneficiario());
        rt.getEnteBeneficiario().setDenomUnitOperBeneficiario(rpt.getDeRptEnteBenefDenomUnitOperBeneficiario());
        rt.getEnteBeneficiario().setIndirizzoBeneficiario(rpt.getDeRptEnteBenefIndirizzoBeneficiario());
        rt.getEnteBeneficiario().setCivicoBeneficiario(rpt.getDeRptEnteBenefCivicoBeneficiario());
        rt.getEnteBeneficiario().setCapBeneficiario(rpt.getCodRptEnteBenefCapBeneficiario());
        rt.getEnteBeneficiario().setLocalitaBeneficiario(rpt.getDeRptEnteBenefLocalitaBeneficiario());
        rt.getEnteBeneficiario().setProvinciaBeneficiario(rpt.getDeRptEnteBenefProvinciaBeneficiario());
        rt.getEnteBeneficiario().setNazioneBeneficiario(rpt.getCodRptEnteBenefNazioneBeneficiario());

        if (StringUtils.isNotBlank(rpt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco())) {
          rt.setSoggettoVersante(objectFactoryPagamenti.createCtSoggettoVersante());
          rt.getSoggettoVersante().setIdentificativoUnivocoVersante(objectFactoryPagamenti.createCtIdentificativoUnivocoPersonaFG());
          rt.getSoggettoVersante().getIdentificativoUnivocoVersante().setCodiceIdentificativoUnivoco(rpt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco());
          rt.getSoggettoVersante().getIdentificativoUnivocoVersante().setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.valueOf(rpt.getCodRptSoggVersIdUnivVersTipoIdUnivoco()));
          rt.getSoggettoVersante().setAnagraficaVersante(StringUtils.stripToNull(rpt.getDeRptSoggVersAnagraficaVersante()));
          rt.getSoggettoVersante().setIndirizzoVersante(StringUtils.stripToNull(rpt.getDeRptSoggVersIndirizzoVersante()));
          rt.getSoggettoVersante().setCivicoVersante(StringUtils.stripToNull(rpt.getDeRptSoggVersCivicoVersante()));
          rt.getSoggettoVersante().setCapVersante(StringUtils.stripToNull(rpt.getCodRptSoggVersCapVersante()));
          rt.getSoggettoVersante().setLocalitaVersante(StringUtils.stripToNull(rpt.getDeRptSoggVersLocalitaVersante()));
          rt.getSoggettoVersante().setProvinciaVersante(StringUtils.stripToNull(rpt.getDeRptSoggVersProvinciaVersante()));
          rt.getSoggettoVersante().setNazioneVersante(StringUtils.stripToNull(rpt.getCodRptSoggVersNazioneVersante()));
          rt.getSoggettoVersante().setEMailVersante(StringUtils.stripToNull(rpt.getDeRptSoggVersEmailVersante()));
        }

        rt.setSoggettoPagatore(objectFactoryPagamenti.createCtSoggettoPagatore());
        rt.getSoggettoPagatore().setIdentificativoUnivocoPagatore(objectFactoryPagamenti.createCtIdentificativoUnivocoPersonaFG());
        rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().setCodiceIdentificativoUnivoco(rpt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco());
        rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.valueOf(rpt.getCodRptSoggPagIdUnivPagTipoIdUnivoco()));
        rt.getSoggettoPagatore().setAnagraficaPagatore(StringUtils.stripToNull(rpt.getDeRptSoggPagAnagraficaPagatore()));
        rt.getSoggettoPagatore().setIndirizzoPagatore(StringUtils.stripToNull(rpt.getDeRptSoggPagIndirizzoPagatore()));
        rt.getSoggettoPagatore().setCivicoPagatore(StringUtils.stripToNull(rpt.getDeRptSoggPagCivicoPagatore()));
        rt.getSoggettoPagatore().setCapPagatore(StringUtils.stripToNull(rpt.getCodRptSoggPagCapPagatore()));
        rt.getSoggettoPagatore().setLocalitaPagatore(StringUtils.stripToNull(rpt.getDeRptSoggPagLocalitaPagatore()));
        rt.getSoggettoPagatore().setProvinciaPagatore(StringUtils.stripToNull(rpt.getDeRptSoggPagProvinciaPagatore()));
        rt.getSoggettoPagatore().setNazionePagatore(StringUtils.stripToNull(rpt.getCodRptSoggPagNazionePagatore()));
        rt.getSoggettoPagatore().setEMailPagatore(StringUtils.stripToNull(rpt.getDeRptSoggPagEmailPagatore()));

        rt.setDatiPagamento(objectFactoryPagamenti.createCtDatiVersamentoRT());
        rt.getDatiPagamento().setCodiceEsitoPagamento("1");
        rt.getDatiPagamento().setImportoTotalePagato(BigDecimal.ZERO);
        rt.getDatiPagamento().setIdentificativoUnivocoVersamento(rpt.getCodRptDatiVersIdUnivocoVersamento());
        rt.getDatiPagamento().setCodiceContestoPagamento(rpt.getCodRptDatiVersCodiceContestoPagamento());

        PaaInviaRTRisposta paaInviaRTRisposta = pagamentiTelematiciRT.forwardFakeRT(rt, header);
        if(paaInviaRTRisposta!=null && paaInviaRTRisposta.getPaaInviaRTRisposta()!=null &&
           paaInviaRTRisposta.getPaaInviaRTRisposta().getFault() == null){
          outcome[0] = true;
        }

        if (log.isInfoEnabled())
          log.info("fake RT received rptId[{}]: {}", rpt.getMygovRptRtId(), ToStringBuilder.reflectionToString(paaInviaRTRisposta, new MultilineRecursiveToStringStyle()));
      }

      if(outcome[0]){
        log.info("delete processingInfoChiediCopiaEsito for rptId [{}]", rptRtId);
        rptRtService.deleteProcessingInfoChiediStatoRpt(rptRtId);
      }

    }, () -> log.info("processRpt, skip because rpt missing or locked [{}]", rptRtId));

    return outcome[0];
  }
}
