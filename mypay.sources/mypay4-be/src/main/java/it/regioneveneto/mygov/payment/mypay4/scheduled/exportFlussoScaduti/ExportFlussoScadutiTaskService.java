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
package it.regioneveneto.mygov.payment.mypay4.scheduled.exportFlussoScaduti;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.FlussoExportScaduti;
import it.regioneveneto.mygov.payment.mypay4.service.AnagraficaStatoService;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteTipoDovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.FlussoExportScadutiService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue= ExportFlussoScadutiTaskApplication.NAME)
public class ExportFlussoScadutiTaskService {

  @Value("${task.exportFlussoScaduti.export.path}")
  private String EXPORT_PATH;
  @Value("${task.exportFlussoScaduti.export.folder.name}")
  private String EXPORT_FOLDER_NAME;
  @Value("${task.exportFlussoScaduti.export.csv.columns}")
  private String EXPORT_CSV_COLUMNS;
  private static final String EXT_ZIP = ".zip";
  private static final String EXT_CSV = ".csv";
  private static final String PAA_ERROR_CREAZIONE_CSV = "PAA_ERROR_CREAZIONE_CSV";
  private static final String PAA_ERROR_NESSUN_DOVUTO_TROVATO = "PAA_ERROR_NESSUN_DOVUTO_TROVATO";
  private static final String PAA_ERROR_ELABORAZIONE = "PAA_ERROR_ELABORAZIONE";

  @Autowired
  private FlussoExportScadutiService flussoExportScadutiService;
  @Autowired
  private AnagraficaStatoService anagraficaStatoService;
  @Autowired
  private DovutoService dovutoService;
  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  private long counter = 0;
  @Transactional(propagation = Propagation.NEVER)
  public void exportFlussoScaduti(){

    log.info("exportFlussoScaduti start [{}]", ++counter);

    //Recupero le richieste di export
    List<FlussoExportScaduti> listaExportRichiesti = flussoExportScadutiService.getListaFlussiRichiesti();
    log.debug("exportFlussoScaduti :: Lista di richieste di export recuperate: {}", listaExportRichiesti);

    //Ciclo le richieste di export
    for (FlussoExportScaduti flussoExportScaduti : listaExportRichiesti)
    {
      try
      {
        //Aggiorno lo stato della richiesta di export ad in elaborazione
        flussoExportScaduti.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_IN_ELAB, Constants.STATO_TIPO_EXPORT));
        flussoExportScadutiService.updateFlusso(flussoExportScaduti);
        log.debug("exportFlussoScaduti :: Richieste di export presa in carico ed aggiornata con id: {}", flussoExportScaduti.getMygovFlussoExportScadutiId());

        //Filtri della richiesta di export
        String codIpaEnte = flussoExportScaduti.getMygovEnteId().getCodIpaEnte();
        List<String> tipoDovutoList = new ArrayList<>(Arrays.asList(flussoExportScaduti.getTipiDovuto().split("\\s*;\\s*"))).stream()
          //discard tipiDovuto where dataScadenza is not compulsory
          .filter(codTipoDovuto -> enteTipoDovutoService.getOptionalByCodTipo(codTipoDovuto, codIpaEnte, true).map(EnteTipoDovuto::isFlgScadenzaObbligatoria).orElse(false))
          .collect(Collectors.toUnmodifiableList());
        final LocalDate dtScadenza = flussoExportScaduti.getDtScadenza().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        log.debug("exportFlussoScaduti :: Filtri per la richiesta tipoDovuto: {} dataScadenza: {} ente: {}", tipoDovutoList, dtScadenza, codIpaEnte);

        //Recupero i dovuti conformi ai filtri
        List<Dovuto> listaPagamentiScaduti = dovutoService.getDovutiScadutiByEnteTipoData(codIpaEnte,tipoDovutoList,dtScadenza);
        log.debug("exportFlussoScaduti :: Totale pagamenti recuperati: {}", listaPagamentiScaduti.size());

        if(!listaPagamentiScaduti.isEmpty()) {

          //Path della folder per l'Ente
          String pathExport = File.separator + codIpaEnte;
          String pathFolderFlussoExportScaduti = EXPORT_PATH + File.separator + codIpaEnte;
          //Creo la folder se non esiste
          File directory = new File(pathFolderFlussoExportScaduti);
          if (!directory.exists() && !directory.mkdir())
            throw new MyPayException("cannot create folder "+pathFolderFlussoExportScaduti);
          //Path della folder per gli export dell'Ente
          pathExport += File.separator + EXPORT_FOLDER_NAME;
          pathFolderFlussoExportScaduti += File.separator + EXPORT_FOLDER_NAME;
          //Creo la folder se non esiste
          directory = new File(pathFolderFlussoExportScaduti);
          if (!directory.exists() && !directory.mkdir())
            throw new MyPayException("cannot create folder "+pathFolderFlussoExportScaduti);

          //Path della folder AAAA_MM per gli export dell'Ente
          String ym = dtScadenza.format(DateTimeFormatter.ofPattern("yyyy-MM"));
          pathExport += File.separator + ym;
          pathFolderFlussoExportScaduti += File.separator + ym;
          directory = new File(pathFolderFlussoExportScaduti);
          if (!directory.exists() && !directory.mkdir())
            throw new MyPayException("cannot create folder "+pathFolderFlussoExportScaduti);

          //Nome del file csv che verrà creato per questi pagamenti per quest'ente, in base allo iuf
          String pathCsvFile = pathFolderFlussoExportScaduti + File.separator + flussoExportScaduti.getIuf() + EXT_CSV;

          //Creo csv e scrivo
          File fileCsvScaduti = null;
          try (FileWriter csvWriterExportScaduti = new FileWriter(pathCsvFile);) {
            log.debug("exportFlussoScaduti :: Scrivo nel csv i valori de pagamenti: {}", listaPagamentiScaduti.size());
            csvWriterExportScaduti.append(EXPORT_CSV_COLUMNS);

            //Per ogni pagamento scrivo una riga
            for (Dovuto dovuto : listaPagamentiScaduti) {
              csvWriterExportScaduti.append("\n");
              csvWriterExportScaduti.append(creaRigaCsv(dovuto));
            }

            // Ho finito di elaborare i dovuti scaduti, ora creo il file CSV
            fileCsvScaduti = new File(pathCsvFile);
          } catch (Exception e) {
            // Si è verificato un errore mentre scrivevo il CSV dei pagamenti
            log.error("exportFlussoScaduti :: Errore nella scrittura del del csv:  :: ERRORE", e);
            //Aggiorno lo stato della richiesta di export ad errore
            flussoExportScaduti.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_ERROR, Constants.STATO_TIPO_EXPORT));
            flussoExportScaduti.setCodErrore(PAA_ERROR_CREAZIONE_CSV);
            flussoExportScadutiService.updateFlusso(flussoExportScaduti);
          }

          if (fileCsvScaduti!=null) {

            // Creo lo zip dal csv
            zipSingleFile(pathCsvFile, fileCsvScaduti.getName(), fileCsvScaduti.getParent());

            // Sposto lo zip e cancello il csv
            String[] nameParts = fileCsvScaduti.getName().split("\\.");
            String newZipFile = nameParts[0] + EXT_ZIP;
            pathExport += File.separator + newZipFile;

            //Aggiorno lo stato della richiesta di export ad  elaborato
            flussoExportScaduti.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_ESEGUITO, Constants.STATO_TIPO_EXPORT));
            flussoExportScaduti.setDeNomeFile(newZipFile);
            log.debug("exportFlussoScaduti :: pathExport:{}", pathExport);
            String pathEnte = File.separator + codIpaEnte;
            pathExport = pathExport.replace(pathEnte, "");
            flussoExportScaduti.setDePercorsoFile(pathExport);
            flussoExportScaduti.setNumPagamentiScaduti(listaPagamentiScaduti.size());
            flussoExportScadutiService.updateFlusso(flussoExportScaduti);
          } else {

            // Si è verificato nell'elaborazione della richiesta del flusso
            log.error("exportFlussoScaduti :: Errore nell'elaborazione della richiesta di export con id: {} :: ERRORE ", flussoExportScaduti.getMygovFlussoExportScadutiId());
            //Aggiorno lo stato della richiesta di export ad errore
            flussoExportScaduti.setCodErrore(PAA_ERROR_ELABORAZIONE);
            flussoExportScaduti.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_ERROR, Constants.STATO_TIPO_EXPORT));
            flussoExportScadutiService.updateFlusso(flussoExportScaduti);
          }
        }
        else
        {
          //Non ci sono pagamenti in scadenza per questa data scadenza, ente e tipi dovuto
          log.debug("exportFlussoScaduti :: Non ci sono pagamenti con dataScadenza: {} ente: {} e tipiDovuto: {}", dtScadenza, codIpaEnte, tipoDovutoList);
          //Aggiorno lo stato della richiesta di export ad elaborato senza risultati
          flussoExportScaduti.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_ESEGUITO, Constants.STATO_TIPO_EXPORT));
          flussoExportScaduti.setCodErrore(PAA_ERROR_NESSUN_DOVUTO_TROVATO);
          flussoExportScadutiService.updateFlusso(flussoExportScaduti);
        }
      }
      catch(Exception e)
      {
        // Si è verificato nell'elaborazione della richiesta del flusso
        log.error("exportFlussoScaduti :: Errore nell'elaborazione della richiesta di export con id: {} :: ERRORE", flussoExportScaduti.getMygovFlussoExportScadutiId(), e);
        //Aggiorno lo stato della richiesta di export ad errore
        flussoExportScaduti.setCodErrore(PAA_ERROR_ELABORAZIONE);
        flussoExportScaduti.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_ERROR, Constants.STATO_TIPO_EXPORT));
        flussoExportScadutiService.updateFlusso(flussoExportScaduti);
      }
    }
    log.info("exportFlussoScaduti end [{}]", counter);
  }

  private static String creaRigaCsv(Dovuto dovuto) throws Exception {
    String rigaCsv = "";

    //IUD
    if(StringUtils.isNotBlank(dovuto.getCodIud()))
      rigaCsv = rigaCsv+dovuto.getCodIud()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: IUD non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //IUV
    if(StringUtils.isNotBlank(dovuto.getCodIuv()))
      rigaCsv = rigaCsv+dovuto.getCodIuv()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: IUV non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //DATA CREAZIONE
    if(dovuto.getDtCreazione() != null)
      rigaCsv = rigaCsv+new SimpleDateFormat("yyyy-MM-dd").format(dovuto.getDtCreazione())+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: DATA CREAZIONE non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //TIPO ID UNIVOCO
    if(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco() != ' ')
      rigaCsv = rigaCsv+dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: TIPO ID UNIVOCO non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //CODICE FISCALE PAGATORE
    if(StringUtils.isNotBlank(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco()))
      rigaCsv = rigaCsv+dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: CODICE FISCALE PAGATORE non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //ANAGRAFICA PAGATORE
    if(StringUtils.isNotBlank(dovuto.getDeRpSoggPagAnagraficaPagatore()))
      rigaCsv = rigaCsv+dovuto.getDeRpSoggPagAnagraficaPagatore()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: ANAGRAFICA PAGATORE non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //INDIRIZZO PAGATORE
    if(StringUtils.isNotBlank(dovuto.getDeRpSoggPagIndirizzoPagatore()))
      rigaCsv = rigaCsv+dovuto.getDeRpSoggPagIndirizzoPagatore()+";";
    else
      rigaCsv = rigaCsv+";";

    //CIVICO PAGATORE
    if(StringUtils.isNotBlank(dovuto.getDeRpSoggPagCivicoPagatore()))
      rigaCsv = rigaCsv+dovuto.getDeRpSoggPagCivicoPagatore()+";";
    else
      rigaCsv = rigaCsv+";";

    //CAP PAGATORE
    if(StringUtils.isNotBlank(dovuto.getCodRpSoggPagCapPagatore()))
      rigaCsv = rigaCsv+dovuto.getCodRpSoggPagCapPagatore()+";";
    else
      rigaCsv = rigaCsv+";";

    //LOCALITA PAGATORE
    if(StringUtils.isNotBlank(dovuto.getDeRpSoggPagLocalitaPagatore()))
      rigaCsv = rigaCsv+dovuto.getDeRpSoggPagLocalitaPagatore()+";";
    else
      rigaCsv = rigaCsv+";";

    //PROVINCIA PAGATORE
    if(StringUtils.isNotBlank(dovuto.getDeRpSoggPagProvinciaPagatore()))
      rigaCsv = rigaCsv+dovuto.getDeRpSoggPagProvinciaPagatore()+";";
    else
      rigaCsv = rigaCsv+";";

    //NAZIONE PAGATORE
    if(StringUtils.isNotBlank(dovuto.getCodRpSoggPagNazionePagatore()))
      rigaCsv = rigaCsv+dovuto.getCodRpSoggPagNazionePagatore()+";";
    else
      rigaCsv = rigaCsv+";";

    //EMAIL PAGATORE
    if(StringUtils.isNotBlank(dovuto.getDeRpSoggPagEmailPagatore()))
      rigaCsv = rigaCsv+dovuto.getDeRpSoggPagEmailPagatore()+";";
    else
      rigaCsv = rigaCsv+";";

    //DATA SCADENZA
    if(dovuto.getDtRpDatiVersDataEsecuzionePagamento() != null)
      rigaCsv = rigaCsv+new SimpleDateFormat("yyyy-MM-dd").format(dovuto.getDtRpDatiVersDataEsecuzionePagamento())+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: DATA SCADENZA non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //IMPORTO SINGOLO PAGAMENTO
    if(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento() != null &&
            dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento().intValue() != 0)
      rigaCsv = rigaCsv+dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: IMPORTO SINGOLO VERSAMENTO non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //COMMISSIONE CARICO PA
    if(dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa() != null &&
            dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa().intValue() != 0)
      rigaCsv = rigaCsv+dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa()+";";
    else
      rigaCsv = rigaCsv+";";

    //COD TIPO DOVUTO
    if(StringUtils.isNotBlank(dovuto.getCodTipoDovuto()))
      rigaCsv = rigaCsv+dovuto.getCodTipoDovuto()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: TIPO DOVUTO non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //TIPO VERSAMENTO
    if(StringUtils.isNotBlank(dovuto.getCodRpDatiVersTipoVersamento()))
      rigaCsv = rigaCsv+dovuto.getCodRpDatiVersTipoVersamento()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: TIPO VERSAMENTO non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //CAUSALE VERSAMENTO
    if(StringUtils.isNotBlank(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento()))
      rigaCsv = rigaCsv+dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: CAUSALE non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //DATI SPECIFICI RISCOSSIONE
    if(StringUtils.isNotBlank(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione()))
      rigaCsv = rigaCsv+dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione()+";";
    else
      throw new Exception("BatchExportFlussoScaduti :: DATI SPECIFICI RISCOSSIOEN non presente per dovuto con id: "+dovuto.getMygovDovutoId());

    //BILANCIO
    if(StringUtils.isNotBlank(dovuto.getBilancio()))
      rigaCsv = rigaCsv+dovuto.getBilancio()+";";
    else
      rigaCsv = rigaCsv+";";

    //FLAG GENERA IUV
    rigaCsv = rigaCsv+dovuto.isFlgGeneraIuv()+";";

    return rigaCsv;
  }

  /**
   *
   * @param FILE           da zippare
   * @param SOURCE_FOLDER, dove sarà posto lo zip
   *
   */
  public static void zipSingleFile(String FILE, String PATH_FILE, String SOURCE_FOLDER) {

    /* 1. ZIP DEL FILE PASSATO COME INPUT **/
    byte[] buffer = new byte[1024];
    FileOutputStream fos = null;
    ZipOutputStream zos = null;
    String[] nameParts = PATH_FILE.split("\\.");
    String newZipFile = SOURCE_FOLDER + File.separator + nameParts[0] + EXT_ZIP;
    File fileToZip = new File(FILE);
    try {
      fos = new FileOutputStream(newZipFile);
      zos = new ZipOutputStream(fos);
      FileInputStream in = null;
      ZipEntry ze = new ZipEntry(PATH_FILE);
      zos.putNextEntry(ze);
      try {
        in = new FileInputStream(fileToZip);
        int len;
        while ((len = in.read(buffer)) > 0) {
          zos.write(buffer, 0, len);
        }
      } finally {
        if(in!=null)
          in.close();
      }
      zos.closeEntry();

      /* 2. RIMUOVO IL FILE ZIPPATO **/
      if (fileToZip.exists()) {
        Files.delete(fileToZip.toPath());
      }
    }catch(IOException e){
      log.debug("error processing zipSingleFile with FILE: {} - PATH_FILE: {} - SOURCE_FOLDER: {}",
              FILE, PATH_FILE, SOURCE_FOLDER, e);
    }
    finally {
      try {
        if(zos!=null)
          zos.close();
      }catch(IOException e){
        log.debug("error closing zipSingleFile with FILE: {} - PATH_FILE: {} - SOURCE_FOLDER: {}",
                FILE, PATH_FILE, SOURCE_FOLDER, e);
      }
    }
    
  }

}
