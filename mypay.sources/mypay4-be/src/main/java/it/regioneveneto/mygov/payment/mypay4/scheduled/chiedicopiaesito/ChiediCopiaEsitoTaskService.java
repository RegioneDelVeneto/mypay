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
package it.regioneveneto.mygov.payment.mypay4.scheduled.chiedicopiaesito;

import com.google.common.collect.Streams;
import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.service.CarrelloService;
import it.regioneveneto.mygov.payment.mypay4.service.EsitoService;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.RptRtService;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediCopiaEsito;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediCopiaEsitoRisposta;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
import it.veneto.regione.pagamenti.pa.ppthead.ObjectFactory;
import it.veneto.regione.schemas._2012.pagamenti.Esito;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue=ChiediCopiaEsitoTaskApplication.NAME)
public class ChiediCopiaEsitoTaskService {

  @Autowired
  private ChiediCopiaEsitoTaskService self;

  @Autowired
  private CarrelloService carrelloService;

  @Autowired
  private RptRtService rptRtService;

  @Autowired
  private ApplicationArguments arguments;

  @Autowired
  private PagamentiTelematiciRP pagamentiTelematiciRPClient;

  @Autowired
  private EsitoService esitoService;

  @Autowired
  private JAXBTransformService jaxbTransformService;

  @Value("${task.chiediCopiaEsito.forzaChiusuraCarrelli}")
  private boolean forzaChiusuraCarrelli;

  private final ObjectFactory objectFactoryPaPpthead = new ObjectFactory();

  private long counter = 0;

//  private Instant lastFullExecutionEnd = Instant.EPOCH;

  private final static long MS_TO_MINUTES = 1000*60;
  private final static long MS_TO_HOURS = MS_TO_MINUTES*60;
  private final static long MS_TO_DAYS = MS_TO_HOURS*24;

  @Transactional(propagation = Propagation.NEVER)
  public void chiediCopiaEsito(){
    int[] okOutcomeCount = {0};
    int carrelloListCount = 0;
    int carrelloListFilteredCount = 0;
    try {
      log.info("chiediCopiaEsito start [{}]", ++counter);

      long idCarrello = 0;
      if (ArrayUtils.isNotEmpty(arguments.getSourceArgs())) {
        try {
          idCarrello = Long.parseLong(arguments.getSourceArgs()[0]);
          log.info("ChiediCopiaEsito invoked with idCarrello: {}", idCarrello);
        } catch (Exception e) {
          throw new MyPayException("invalid command line argument for idCarrello: " + arguments.getSourceArgs()[0], e);
        }
      }

      List<Carrello> carrelloList;
      List<Integer> toProcessByType = new ArrayList<>();
      List<Integer> toProcessFilteredByType = new ArrayList<>();
      if (idCarrello == 0) {
        carrelloList = Carrello.VALID_MODELLOPAGAMENTO.stream()
          .map(modelloPagamento -> carrelloService.getByStatePagamentoInCorso(modelloPagamento))
          .peek(list -> toProcessByType.add(list.size()))
          .map(listByModello -> listByModello.stream().filter(mygovCarrelloId -> this.isCarrelloToProcess(mygovCarrelloId).getLeft()).collect(Collectors.toList()))
          .peek(list -> toProcessFilteredByType.add(list.size()))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
      } else {
        carrelloList = Optional.ofNullable(carrelloService.getById(idCarrello))
            .map(List::of)
            .orElse(Collections.emptyList());
      }

      carrelloListCount = toProcessByType.stream().reduce(Integer::sum).orElse(0);
      carrelloListFilteredCount = carrelloList.size();
      String toProcessByTypeString = Streams
          .zip(Carrello.VALID_MODELLOPAGAMENTO.stream(), toProcessByType.stream(), (type, size) -> type + ": " + size)
          .collect(Collectors.joining(" - "));
      String toProcessFilteredByTypeString = Streams
        .zip(Carrello.VALID_MODELLOPAGAMENTO.stream(), toProcessFilteredByType.stream(), (type, size) -> type + ": " + size)
        .collect(Collectors.joining(" - "));
      log.info("number of carrello to process: {} [{}]", carrelloListCount, toProcessByTypeString);
      log.info("number of filtered carrello to process: {} [{}]",carrelloListFilteredCount , toProcessFilteredByTypeString);

      carrelloList.forEach( carrello -> {
        try{
          log.debug("Start processing carrello with id: {} - idDominio: {} - IUV: {} - ccp: {}",
            carrello.getMygovCarrelloId(),
            carrello.getCodRpSilinviarpIdDominio(),
            carrello.getCodRpDatiVersIdUnivocoVersamento(),
            carrello.getCodRpDatiVersCodiceContestoPagamento());
          //process carrelloList (in a separate transaction)
          boolean outcome = self.elaboraEsito(carrello.getMygovCarrelloId());
          if(outcome)
            okOutcomeCount[0]++;
          log.debug("End processing carrello with id: {} - idDominio: {} - IUV: {} - ccp: {}",
            carrello.getMygovCarrelloId(),
            carrello.getCodRpSilinviarpIdDominio(),
            carrello.getCodRpDatiVersIdUnivocoVersamento(),
            carrello.getCodRpDatiVersCodiceContestoPagamento());
        }catch(Exception e){
          log.error("Error processing carrello with id: {} - idDominio: {} - IUV: {} - ccp: {}", carrello.getMygovCarrelloId(),
            carrello.getCodRpSilinviarpIdDominio(), carrello.getCodRpDatiVersIdUnivocoVersamento(),
            carrello.getCodRpDatiVersCodiceContestoPagamento(), e);
        }
      });

      log.info("chiediCopiaEsito end [{}] okOutcomeCount[{}/{}/{}]", counter, okOutcomeCount[0], carrelloListFilteredCount, carrelloListCount);
    }catch(Exception e){
      log.error("chiediCopiaEsito error [{}] okOutcomeCount[{}/{}/{}]", counter, okOutcomeCount[0], carrelloListFilteredCount, carrelloListCount, e);
    }
  }

  private Pair<Boolean,Integer> isCarrelloToProcess(Carrello carrello){
    long now = System.currentTimeMillis();
    return Optional.ofNullable(carrelloService.getProcessingInfoChiediCopiaEsito(carrello.getMygovCarrelloId()))
      .map(processingInfo -> {
        int numChecks = Integer.parseInt(StringUtils.substringBefore(processingInfo, "|"));
        long lastCheck = Long.parseLong(StringUtils.substringAfter(processingInfo, "|"));
        long carrelloDt = ObjectUtils.firstNonNull(carrello.getDtUltimaModificaRp(), carrello.getDtCreazione()).getTime();
        /*
          Frequency |  Num tries |  Total tries | Elapsed time | Total elapsed time
          -------------------------------------------------------------------------
                1m  |        10  |           10 |          10m |              10m
                5m  |        10  |           20 |          50m |               1h
               10m  |         6  |           26 |          60m |               2h
                1h  |        22  |           48 |          22h |               1d
                6h  |        24  |           72 |           6d |               7d
                1d  |         *  |            * |            * |              >7d
         */
        long totalElapsed = now - carrelloDt;
        boolean toProcess;
        String frequency;
        if (totalElapsed < 10 * MS_TO_MINUTES) {
          toProcess = (now - lastCheck) > 1 * MS_TO_MINUTES;
          frequency = "1m";
        } else if (totalElapsed < 1 * MS_TO_HOURS) {
          toProcess = (now - lastCheck) > 5 * MS_TO_MINUTES;
          frequency = "5m";
        } else if (totalElapsed < 2 * MS_TO_HOURS) {
          toProcess = (now - lastCheck) > 10 * MS_TO_MINUTES;
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
        log.debug("carrelloId [{}] toProcess[{}] numChecks[{}] Dcarrello[{}] DlastCheck[{}] frequency[{}]",
          carrello.getMygovCarrelloId(), toProcess, numChecks, DurationFormatUtils.formatDuration(totalElapsed, "dd-HH:mm:ss"),
          DurationFormatUtils.formatDuration(now - lastCheck, "dd-HH:mm:ss"), frequency);
        return Pair.of(toProcess, numChecks);
      })
      .orElseGet(() -> {
        log.debug("carrelloId [{}] Dcarrello[{}] not found in processing info cache", carrello.getMygovCarrelloId(),
          DurationFormatUtils.formatDuration(now - ObjectUtils.firstNonNull(carrello.getDtUltimaModificaRp(), carrello.getDtCreazione()).getTime(),"dd-HH:mm:ss"));
        return Pair.of(true, 0);
      });
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean elaboraEsito(Long mygovCarrelloId){
    boolean[] outcome = {false};
    carrelloService.getByIdLockOrSkip(mygovCarrelloId).ifPresentOrElse(carrello -> {
      //check again if is to process
      Pair<Boolean, Integer> isToProcess = this.isCarrelloToProcess(carrello);

      if(!isToProcess.getLeft()){
        return;
      }

      //update processing info for carrello
      carrelloService.updateProcessingInfoChiediCopiaEsito(carrello.getMygovCarrelloId(), isToProcess.getRight()+1);

      NodoSILChiediCopiaEsito request = new NodoSILChiediCopiaEsito();
      request.setIdentificativoDominio(carrello.getCodRpSilinviarpIdDominio());
      request.setIdentificativoUnivocoVersamento(carrello.getCodRpDatiVersIdUnivocoVersamento());
      request.setCodiceContestoPagamento(carrello.getCodRpDatiVersCodiceContestoPagamento());
      NodoSILChiediCopiaEsitoRisposta response = pagamentiTelematiciRPClient.nodoSILChiediCopiaEsito(request);

      FaultBean faultBean = response.getFault();
      if (faultBean != null && StringUtils.isNotBlank(faultBean.getFaultCode())) {
        log.error("Fault - id:{} - code:{} - descr:{}", faultBean.getId(), faultBean.getFaultCode(), faultBean.getDescription());
        if (forzaChiusuraCarrelli && faultBean.getFaultCode().equals(FaultCodeConstants.PAA_RPT_NON_PRESENTE)) {
          esitoService.elaboraDovutiNonPagatiERimuoviCarrello(carrello.getCodRpSilinviarpIdDominio(),
            carrello.getCodRpSilinviarpIdUnivocoVersamento(), carrello.getCodRpSilinviarpCodiceContestoPagamento());
          outcome[0] = true;
        }
      } else if (response.getEsito() != null) {
        Esito ctEsito;
        try {
          //decode esito
          ctEsito = jaxbTransformService.unmarshalling(response.getEsito(), Esito.class, "/wsdl/pa/PagInf_RP_Esito_6_2_0.xsd");
        } catch (MyPayException e) {
          log.error("cannot unmarshall esito, try to recover without using schema validation", e);
          ctEsito = jaxbTransformService.unmarshalling(response.getEsito(), Esito.class);
        }

        //process esito
        IntestazionePPT header = objectFactoryPaPpthead.createIntestazionePPT();
        header.setIdentificativoDominio(carrello.getCodRpSilinviarpIdDominio());
        header.setIdentificativoUnivocoVersamento(carrello.getCodRpDatiVersIdUnivocoVersamento());
        header.setCodiceContestoPagamento(carrello.getCodRpDatiVersCodiceContestoPagamento());
        esitoService.elaboraEsito(ctEsito, response.getTipoFirma(), response.getRt(), header);

        //manage avviso digitale
        esitoService.manageAvvisoDigitale(carrello.getCodRpSilinviarpIdDominio(), ctEsito, carrello);

        //in case of positive outcome, remove chiediEsitoProcessingInfo from redis cache
        if(response.getFault()==null) {
          log.info("delete processingInfoChiediCopiaEsito for carrelloId [{}]", mygovCarrelloId);
          carrelloService.deleteProcessingInfoChiediCopiaEsito(mygovCarrelloId);
          outcome[0] = true;
        }

      } else {
        log.info("Esito null - response:{}", ReflectionToStringBuilder.toString(response));
      }
    }, () -> log.info("elaboraEsito, skip because carrello missing or locked [{}]", mygovCarrelloId));

    return outcome[0];
  }
}