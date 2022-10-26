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
package it.regioneveneto.mygov.payment.mypay4.scheduled.chiediflussorendicontazione;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediElencoFlussiRendicontazioneRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediFlussoRendicontazione;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediFlussoRendicontazioneRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoIdRendicontazione;
import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.WSFaultException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.FlussoSpcService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.LockService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciRPTClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
@Slf4j
@ConditionalOnProperty(name=AbstractApplication.NAME_KEY, havingValue= ChiediFlussoRendicontazioneTaskApplication.NAME)
public class ChiediFlussoRendicontazioneTaskService {

  @Value("${task.chiediFlussoRendicontazione.fixedDelay}")
  private int fixedDelayTaskMilliseconds;

  @Value("${task.chiediFlussoRendicontazione.filterEnti:}")
  private List<String> filterEnti;

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  @Value("${nodoRegionaleFesp.password}")
  private String password;

  @Value("${mybox.path.root}")
  private String fsRootPath;

  @Value("${mypay.path.relative.data}")
  private String relativeDataPath;

  @Autowired
  private ChiediFlussoRendicontazioneTaskService self;

  @Autowired
  private EnteService enteService;

  @Autowired
  private LockService lockService;

  @Autowired
  private FlussoSpcService flussoSpcService;

  @Autowired
  private PagamentiTelematiciRPTClient pagamentiTelematiciRPTClient;

  private final ObjectFactory objectFactoryNodoSpc = new ObjectFactory();
  private final SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy_MM");
  private final SimpleDateFormat untilSecondsFormat = new SimpleDateFormat("yyyyMMddHHmmss");


  private long counter = 0;
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
  public void chiediFlussoRendicontazione(){
    log.info("chiediFlussoRendicontazione start [{}]", ++counter);
    log.info("filterEnti: {}", filterEnti);
    enteService.getAllEnti()
        .stream().filter(ente -> filterEnti.isEmpty() || filterEnti.contains(ente.getCodIpaEnte()))
        .forEach(self::manageFlussiRendicontazioneForEnte);

    log.info("chiediFlussoRendicontazione end [{}]", counter);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
  public void manageFlussiRendicontazioneForEnte(Ente ente){
    try {
      log.debug("Start processing ente: {} [{}]", ente.getCodIpaEnte(), ente.getDeNomeEnte());

      //lock ente (only if older than a given amount of minutes; otherwise skip)
      Optional<Integer> onlyIfOlderThanMinutes = Optional.of(fixedDelayTaskMilliseconds/(1000*60));
      boolean lockAcquired = lockService.lock(LockService.Types.CHIEDI_FLUSSO_RENDICONTAZIONE, ente.getCodIpaEnte(), onlyIfOlderThanMinutes);
      if(!lockAcquired){
        log.info("cannot acquire lock (or last lock not older than {} minutes) for ente {}, skipping", onlyIfOlderThanMinutes.get(), ente.getCodIpaEnte());
        return;
      }

      NodoChiediElencoFlussiRendicontazione request = objectFactoryNodoSpc.createNodoChiediElencoFlussiRendicontazione();
      request.setIdentificativoIntermediarioPA(identificativoIntermediarioPA);
      request.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPA);
      request.setPassword(password);
      request.setIdentificativoDominio(ente.getCodiceFiscaleEnte());
      request.setIdentificativoPSP(null);
      NodoChiediElencoFlussiRendicontazioneRisposta response = pagamentiTelematiciRPTClient.nodoChiediElencoFlussiRendicontazione(request);
      if(response.getFault()!=null){
        log.error("WsFault nodoChiediElencoFlussiRendicontazione: {}", response.getFault());
        throw new WSFaultException(response.getFault().getFaultCode(), response.getFault().getDescription());
      }
      if(response.getElencoFlussiRendicontazione()==null ||
          CollectionUtils.isEmpty(response.getElencoFlussiRendicontazione().getIdRendicontaziones())){
        log.info("no flussi rendicontazione found for ente: {}", ente.getCodIpaEnte());
        return;
      }

      List<TipoIdRendicontazione> idRendicontazioneList = response.getElencoFlussiRendicontazione().getIdRendicontaziones();
      log.info("{} flussi di rendicontazione found for ente: {}", idRendicontazioneList.size(), ente.getCodIpaEnte());

      idRendicontazioneList.forEach(tipoIdRendicontazione -> {
        try {
          self.manageFlussoRendicontazione(ente, tipoIdRendicontazione);
        }catch(Exception e) {
          log.error("Error processing flusso rendicontazione for ente:{} with flusso-id:{} time:{}",ente.getCodIpaEnte(),
              tipoIdRendicontazione.getIdentificativoFlusso(), tipoIdRendicontazione.getDataOraFlusso(), e);
        }
      });

      //recover any flusso in case it is stuck in state IN_CARICAMENTO
      //TODO check if necessary, this should never be possible

    }catch(Exception e){
      log.error("Error processing ente: {}", ente.getCodIpaEnte(), e);
    }

  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.NESTED)
  public void manageFlussoRendicontazione(Ente ente, TipoIdRendicontazione tipoIdRendicontazione){
    String idFlusso = tipoIdRendicontazione.getIdentificativoFlusso();
    String idPSP;
    try(Scanner scanner = new Scanner(idFlusso.substring(10))){
      scanner.useDelimiter("-");
      idPSP = scanner.next();
    }

    Calendar dataOraFlussoCalendar = tipoIdRendicontazione.getDataOraFlusso().toGregorianCalendar();
    dataOraFlussoCalendar.clear(Calendar.MILLISECOND);
    Timestamp dataOraRiferimentoFlusso = new Timestamp(dataOraFlussoCalendar.getTimeInMillis());
    String currentYearMonthString = yearMonthFormat.format(new Date());
    String dataOraRiferimentoFlussoString = untilSecondsFormat.format(dataOraRiferimentoFlusso);
    log.info("managing flusso with codIpaEnte: {}, idPSP: {}, idFlusso: {}, dataOraFlusso: {}", ente.getCodIpaEnte(), idPSP, idFlusso, dataOraRiferimentoFlusso);
    flussoSpcService.getRendicontazioneByKeyInsertable(ente.getCodIpaEnte(), idFlusso, idPSP, dataOraRiferimentoFlusso).ifPresentOrElse(
        flussoSpc -> log.info("flusso rendicontazione already present for ente:{} with flusso-id: {} [{}]",ente.getCodIpaEnte(),
            flussoSpc.getCodIdentificativoFlusso(), flussoSpc.getIdentificativoPsp()),
        () -> {
          Integer idMygovFlussoRendSpc = flussoSpcService.insertRendicontazione(ente.getCodIpaEnte(), idFlusso, idPSP, dataOraRiferimentoFlusso);
          try {
            NodoChiediFlussoRendicontazione request = objectFactoryNodoSpc.createNodoChiediFlussoRendicontazione();
            request.setIdentificativoIntermediarioPA(identificativoIntermediarioPA);
            request.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPA);
            request.setPassword(password);
            request.setIdentificativoDominio(ente.getCodiceFiscaleEnte());
            request.setIdentificativoFlusso(idFlusso);
            request.setIdentificativoPSP(idPSP);
            NodoChiediFlussoRendicontazioneRisposta response = pagamentiTelematiciRPTClient.nodoChiediFlussoRendicontazione(request);
            if (response.getFault() != null) {
              log.error("WsFault nodoChiediFlussoRendicontazione: {}", response.getFault());
              throw new WSFaultException(response.getFault().getFaultCode(), response.getFault().getDescription());
            }
            try (InputStream inputStream = response.getXmlRendicontazione().getInputStream()) {
              File basePath = Path.of(fsRootPath, relativeDataPath, ente.getCodIpaEnte(), Constants.PATH_RENDICONTAZIONE, currentYearMonthString).toFile();
              String fileRendicontazioneBaseName = String.format("%s_%s_%s", idPSP, idFlusso, dataOraRiferimentoFlussoString);
              String fileRendicontazioneName = String.format("%s.%s", fileRendicontazioneBaseName, Constants.FILE_EXTENSION_FLUSSO);
              String fileTemporaryName = String.format("%s_%s.%s", fileRendicontazioneBaseName, untilSecondsFormat.format(new Date()), Constants.FILE_EXTENSION_TEMPORARY);
              File fileRendicontazione = new File(basePath, fileRendicontazioneName);
              File fileTemporary = new File(basePath, fileTemporaryName);
              FileUtils.copyToFile(inputStream, fileTemporary);
              if (!fileTemporary.renameTo(fileRendicontazione)) {
                log.error("error renaming temporary file [{} -> {}] for PSP:{} Ente:{}",
                    fileTemporary.getAbsolutePath(), fileRendicontazione.getAbsolutePath(), idPSP, ente.getCodIpaEnte());
                throw new MyPayException("error renaming file [" + fileTemporary.getAbsolutePath() + "] to [" + fileRendicontazione + "]");
              }
              log.info("correctly uploaded file [{}] for PSP:{} Ente:{}", fileRendicontazione.getAbsolutePath(), idPSP, ente.getCodIpaEnte());
              String relativePath = File.separator + Path.of(Constants.PATH_RENDICONTAZIONE, currentYearMonthString, fileRendicontazioneName);
              flussoSpcService.updateRendicontazione(idMygovFlussoRendSpc, relativePath, fileRendicontazione.length(), Constants.STATO_ESECUZIONE_FLUSSO_CARICATO);
            }
          } catch (Exception e) {
            log.error("Error downloading/saving flusso rendicontazione with id:{} for ente:{} with flusso-id:{} time:{}",
                idMygovFlussoRendSpc, ente.getCodIpaEnte(), idFlusso, dataOraRiferimentoFlusso, e);
            flussoSpcService.updateRendicontazione(idMygovFlussoRendSpc, null, 0, Constants.STATO_ESECUZIONE_FLUSSO_ERRORE_CARICAMENTO);
          }
        }
    );
  }

}