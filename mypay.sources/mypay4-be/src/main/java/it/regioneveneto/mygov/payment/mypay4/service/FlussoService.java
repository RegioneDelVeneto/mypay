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
package it.regioneveneto.mygov.payment.mypay4.service;

import it.regioneveneto.mygov.payment.mypay4.controller.FlussoController;
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.FlussoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.FileTo;
import it.regioneveneto.mygov.payment.mypay4.dto.FlussoImportTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.queue.QueueProducer;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.MaxResultsHelper;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediFlussoSPCPage;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediFlussoSPCPageRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FlussoSPC;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jdbi.v3.core.mapper.JoinRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class FlussoService {

  @Value("${jdbc.limit.default:1000}")
  private int defaultQueryLimit;

  @Resource
  private FlussoService self;

  @Autowired
  private FlussoDao flussoDao;

  @Autowired
  private DovutoDao dovutoDao;

  @Autowired
  private EnteService enteService;

  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;

  @Autowired
  private EnteFunzionalitaService enteFunzionalitaService;

  @Autowired
  private AvvisoDigitaleService avvisoDigitaleService;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  private ExportDovutiService exportDovutiService;

  @Autowired
  ImportDovutiService importDovutiService;

  @Autowired
  private PagamentiTelematiciRP pagamentiTelematiciRPClient;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private QueueProducer queueProducer;

  @Autowired
  private MaxResultsHelper maxResultsHelper;

  @Value("${mypay.path.manage.log}")
  private String flussiLogRootDir;
  private final String MESSAGE_PROPERTY = "pa.batch.import.";

  private static Function<String, List<String>> mapFlussiDefault = s -> List.of(
      "_" + s + "_SPONTANEO",
      "_" + s + "_ESTERNO-ANONIMO",
      "_" + s + "_IMPORT-DOVUTO"
  );

  @CacheEvict(value=CacheService.CACHE_NAME_FLUSSO,key="{'id',#id}")
  public void clearCacheById(Long id){}

  @Cacheable(value=CacheService.CACHE_NAME_FLUSSO, key="{'id',#id}", unless="#result==null")
  public Flusso getById(Long id) {
    return flussoDao.getById(id);
  }

  @Cacheable(value=CacheService.CACHE_NAME_FLUSSO)
  public List<Flusso> getByEnte(Long mygovEnteId, boolean spontaneo) {
    return flussoDao.getByEnte(mygovEnteId, spontaneo);
  }

  @Transactional(propagation =  Propagation.SUPPORTS)
  public List<FlussoImportTo> getByEnteIufCreateDt(Long mygovEnteId, String iuf, LocalDate dateFrom, LocalDate dateTo) throws ValidatorException {
    if (dateFrom == null && dateTo == null) {
      dateTo = LocalDate.now();
      dateFrom = dateTo.plusMonths(-1);
    } else if (dateTo == null) {
      dateTo = dateFrom.plusMonths(1);
    } else if (dateFrom == null) {
      dateFrom = LocalDate.now().isBefore(dateTo.plusMonths(-1)) ? LocalDate.now() : dateTo.plusMonths(-1);
    } else if (dateTo.isBefore(dateFrom)) {
      throw new ValidatorException(messageSource.getMessage("pa.messages.invalidDataIntervallo", null, Locale.ITALY));
    }
    final LocalDate dateFromFinal = dateFrom;
    final LocalDate dateToFinal = dateTo.plusDays(1);

    return maxResultsHelper.manageMaxResults(
        maxResults -> flussoDao.getByEnteIufCreateDt(mygovEnteId, iuf, dateFromFinal, dateToFinal, maxResults)
            .stream().map(this::mapEntityToDto).collect(Collectors.toList()),
        () -> flussoDao.getByEnteIufCreateDtCount(mygovEnteId, iuf, dateFromFinal, dateToFinal) );
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public Optional<Flusso> getByIuf(String iuf) {
    return flussoDao.getByIuf(iuf);
  }

  public byte[] downloadLog(String pa, String fileName) throws Exception {

    File file = new File(flussiLogRootDir);
    if (!file.exists()) {
      file.mkdir();
    }
    String subDir = flussiLogRootDir + pa + "/";
    file = new File(subDir);

    if (!file.exists()) {
      file.mkdir();
    }

    String fileNameFull = subDir + fileName + ".log";
    file = new File(fileNameFull);

    File checkFile = new File(subDir);
    File parent = file.getParentFile();

    if (file.exists() && parent.getAbsolutePath().equals(checkFile.getAbsolutePath())) {
      return Files.readAllBytes(file.toPath());
    } else {
      throw new ValidatorException(messageSource.getMessage("pa.flussi.errorDownload", null, Locale.ITALY));
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int removeFlusso(String username, Long mygovEnteId, Long mygovFlussoId) {
    Flusso flusso = flussoDao.getById(mygovFlussoId);

    if ((flusso == null) || !flusso.getMygovAnagraficaStatoId().getCodStato().equals(Constants.STATO_FLUSSO_CARICATO))
      throw new MyPayException(messageSource.getMessage("pa.flusso.optimisticLockErrorAnnullamento", null, Locale.ITALY));

    AnagraficaStato anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_FLUSSO_ANNULLATO, Constants.STATO_TIPO_FLUSSO);
    flusso.setMygovAnagraficaStatoId(anagraficaStato);
    flusso.setDtUltimaModifica(new Date());
    int updatedRec = flussoDao.update(flusso);
    if (updatedRec != 1)
      throw new MyPayException("Errore interno aggiornamento flusso");
    this.clearCacheById(flusso.getMygovFlussoId());

    List<Dovuto> dovuti = dovutoDao.getByFlussoId(mygovFlussoId);

    for (Dovuto dovuto: dovuti) {
      dovutoElaboratoService.elaborateDovuto(dovuto, Constants.STATO_DOVUTO_DA_PAGARE, Constants.STATO_DOVUTO_ANNULLATO);
    }

    try {
      Ente ente = enteService.getEnteById(mygovEnteId);
      List<EnteFunzionalita> listaFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(ente.getCodIpaEnte(), true);
      if (listaFunzionalita.stream().anyMatch(f -> Constants.FUNZIONALITA_AVVISATURA_DIGITALE.equals(f.getCodFunzionalita()))) {
        avvisoDigitaleService.changeStateToAnnullato(dovuti, ente);
      }
    } catch (Exception e) {
      log.warn("Errore cambio di stato nell'anagrafica digitale", e);
      throw e;
    }
    return updatedRec;
  }

  @Transactional(propagation =  Propagation.SUPPORTS)
  public List<FileTo> flussiExport(Long mygovEnteId, String codFedUserId, String nomeFile, LocalDate dateFrom,
                                         LocalDate dateTo) throws ValidatorException {
    boolean invalidDates = false;
    LocalDate now = LocalDate.now();
    if (dateFrom == null && dateTo == null) {
      dateTo = LocalDate.now();
      dateFrom = dateTo.minusMonths(1);
    } else if (dateTo == null) {
      invalidDates = dateFrom.isAfter(LocalDate.now());
      dateTo = dateFrom.plusMonths(1).isBefore(now) ? dateFrom.plusMonths(1) : now;
    } else if (dateFrom == null) {
      invalidDates = dateTo.isAfter(LocalDate.now());
      dateFrom = dateTo.minusMonths(1);
    } else if (dateTo.isBefore(dateFrom)) {
      invalidDates = true;
    }
    if(invalidDates)
      throw new ValidatorException(messageSource.getMessage("pa.messages.invalidDataIntervallo", null, Locale.ITALY));

    final LocalDate dateFromFinal = dateFrom;
    final LocalDate dateToFinal = dateTo.plusDays(1);

    return maxResultsHelper.manageMaxResults(
        maxResults -> exportDovutiService.getByEnteNomefileDtmodifica
            (mygovEnteId, codFedUserId, nomeFile, dateFromFinal, dateToFinal, maxResults)
            .stream().map(this::mapEntityToDto).collect(Collectors.toList()),
        () -> exportDovutiService.getByEnteNomefileDtmodificaCount(mygovEnteId, codFedUserId, nomeFile, dateFromFinal, dateToFinal) );
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long flussiExportInsert(Long mygovEnteId, String codFedUserId, String tipoDovuto, String versioneTracciato, LocalDate dateFrom, LocalDate dateTo) throws ValidatorException {
    if (dateFrom == null && dateTo == null) {
      dateTo = LocalDate.now();
      dateFrom = dateTo.plusMonths(-1);
    } else if (dateTo == null) {
      dateTo = dateFrom.plusMonths(1);
    } else if (dateFrom == null) {
      dateFrom = LocalDate.now().isBefore(dateTo.plusMonths(-1)) ? LocalDate.now() : dateTo.plusMonths(-1);
    } else if (dateTo.isBefore(dateFrom)) {
      throw new ValidatorException(messageSource.getMessage("pa.messages.invalidDataIntervallo", null, Locale.ITALY));
    }

    String codRequestToken = UUID.randomUUID().toString();

    boolean correctVesioneTracciato = Arrays.stream(Constants.VERSIONE_TRACCIATO_EXPORT.values())
        .anyMatch(v -> v.getValue().equals(StringUtils.firstNonBlank(versioneTracciato, Constants.DEFAULT_VERSIONE_TRACCIATO)));
    if (!correctVesioneTracciato)
      throw new ValidatorException(messageSource.getMessage("pa.messages.notOperatorAccess", null, Locale.ITALY));

    boolean flgRicevuta = !Constants.DEFAULT_VERSIONE_TRACCIATO.equals(versioneTracciato);

    if (tipoDovuto != null) {
      var codIpa = enteService.getEnteById(mygovEnteId).getCodIpaEnte();
      enteTipoDovutoService.getOptionalByCodTipo(tipoDovuto, codIpa, true).orElseThrow(()-> new NotFoundException("Tipo Dovuto not found"));
    }

    Long exportDovutiId = exportDovutiService.insert(mygovEnteId, codFedUserId, dateFrom, dateTo, tipoDovuto, codRequestToken,
        flgRicevuta, false, versioneTracciato);

    //add message to EXPORT DOVUTI queue
    queueProducer.enqueueExportDovuti(exportDovutiId);

    return exportDovutiId;
  }

  @Transactional(propagation =  Propagation.SUPPORTS)
  public List<FileTo> flussiSPC( Constants.FLG_TIPO_FLUSSO tipoFlusso, Long mygovEnteId, String codFedUserId, String flgProdOrDisp, LocalDate dateFrom,
                                   LocalDate dateTo) throws ValidatorException {
    if (dateFrom == null && dateTo == null) {
      dateTo = LocalDate.now();
      dateFrom = dateTo.plusMonths(-1);
    } else if (dateTo == null) {
      dateTo = dateFrom.plusMonths(1);
    } else if (dateFrom == null) {
      dateFrom = LocalDate.now().isBefore(dateTo.plusMonths(-1)) ? LocalDate.now() : dateTo.plusMonths(-1);
    } else if (dateTo.isBefore(dateFrom)) {
      throw new ValidatorException(messageSource.getMessage("pa.messages.invalidDataIntervallo", null, Locale.ITALY));
    }

    ChiediFlussoSPCPage request = new ChiediFlussoSPCPage();
    request.setDateFrom(Utilities.toXMLGregorianCalendar(dateFrom));
    request.setDateTo(Utilities.toXMLGregorianCalendar(dateTo));
    request.setFlgTipoFlusso(tipoFlusso.getValue());
    //request.setIdentificativoPSP(identificativoPSP); // Not used as of 30.11.2020
    request.setFlgProdOrDisp(flgProdOrDisp);
    request.setPage(1);
    request.setPageSize(defaultQueryLimit);
    Ente ente = enteService.getEnteById(mygovEnteId);
    request.setIdentificativoDominio(ente.getCodiceFiscaleEnte());
    ChiediFlussoSPCPageRisposta response = pagamentiTelematiciRPClient.chiediFlussoSPCPage(request);

    return this.mapSpcResponseToDtos(response);
  }

  private FlussoImportTo mapEntityToDto(JoinRow joinRowFlussoUtente) {
    FlussoImportTo flussoTo = this.mapEntityToDto(joinRowFlussoUtente.get(Flusso.class));
    Optional.ofNullable(joinRowFlussoUtente.get(Utente.class)).map(Utente::getCodFedUserId).ifPresent(flussoTo::setCodFedUserId);
    return flussoTo;
  }

  private FlussoImportTo mapEntityToDto(Flusso flusso) {
    FlussoImportTo flussoTo = new FlussoImportTo();

    flussoTo.setId(flusso.getMygovFlussoId());
    flussoTo.setNomeFlusso(flusso.getIuf());
    flussoTo.setDataCaricamento(new java.sql.Date(flusso.getDtCreazione().getTime()).toLocalDate());
    flussoTo.setOperatore(flusso.getDeNomeOperatore());
    flussoTo.setCodStato(flusso.getMygovAnagraficaStatoId().getCodStato());
    flussoTo.setDeStato(flusso.getMygovAnagraficaStatoId().getDeStato());
    flussoTo.setNumRigheTotali(flusso.getNumRigheTotali());
    flussoTo.setNumRigheImportateCorrettamente(flusso.getNumRigheImportateCorrettamente());

    if(flusso.getPdfGenerati() != null) {
      flussoTo.setPdfGenerati(flusso.getPdfGenerati());
      if(Constants.STATO_FLUSSO_ERRORE_CARICAMENTO.equals(flusso.getMygovAnagraficaStatoId().getCodStato())) {
        if(StringUtils.isNotBlank(flusso.getCodErrore())) {
          String erroreComposto;
          String errore = flusso.getCodErrore();
          if(errore.contains("(")) {
            int a=0;
            int b=0;

            for(int i = 0; i < errore.length(); i++) {
              if(errore.charAt(i) == '(') {
                a = i;
              }
              if(errore.charAt(i) == ')') {
                b = i;
              }
            }
            String codice = errore.substring(a+1, b);
            String[] temp = codice.split(",");
            Map<String,String> map = new HashMap<>();
            for(int i = 0; i<temp.length; i++)
            {
              map.put(String.valueOf(i), temp[i]);
            }
            String tempErrore =  errore.substring(0,a);
            erroreComposto = StringSubstitutor.replace(messageSource.getMessage(MESSAGE_PROPERTY+tempErrore, null, Locale.ITALY), map,"{","}");
          }
          else {
            erroreComposto = messageSource.getMessage(MESSAGE_PROPERTY+errore, null, Locale.ITALY);
          }
          flussoTo.setCodErrore(erroreComposto);
        }
      }
      if(Constants.STATO_FLUSSO_CARICATO.equals(flusso.getMygovAnagraficaStatoId().getCodStato())) {
        flussoTo.setShowDownload(Boolean.TRUE);
        if(StringUtils.isNotBlank(flusso.getDeNomeFile()) && (flusso.getDeNomeFile().length() > 4) ) {
          String nomeFlusso = flusso.getDeNomeFile();
          flussoTo.setPath(flusso.getDePercorsoFile()+ File.separator+nomeFlusso.substring(0, nomeFlusso.length()-4));
        }
      }
    }
    else
      flussoTo.setShowDownload(Boolean.FALSE);

    File file = new File(flussiLogRootDir+File.separator+flusso.getMygovEnteId().getCodIpaEnte(), flusso.getIuf()+".log");
    flussoTo.setLog(file.exists()?flusso.getIuf():null);

    return flussoTo;
  }

  private FileTo mapEntityToDto(ExportDovuti exportDovuto) {
    String fileName = FilenameUtils.getName(exportDovuto.getDeNomeFileGenerato());
    if(StringUtils.isBlank(fileName))
      fileName = messageSource.getMessage("pa.batch.export.no.data", null, Locale.ITALY);
    FileTo.FileToBuilder builder = FileTo.builder()
        .name(fileName)
        .path(exportDovuto.getDeNomeFileGenerato())
        .dataCreazione(Utilities.toLocalDateTime(exportDovuto.getDtUltimaModifica()))
        .dimensione(exportDovuto.getNumDimensioneFileGenerato());
    return builder.build();
  }

  private List<FileTo> mapSpcResponseToDtos(ChiediFlussoSPCPageRisposta response) {
    List<FileTo> files = new ArrayList<>();
    for (FlussoSPC flusso: response.getFlussoSPCs()) {
      int idx = flusso.getDeNomeFileScaricato().lastIndexOf(File.separator);
      String fileName = flusso.getDeNomeFileScaricato().substring(idx + 1);
      FileTo.FileToBuilder builder = FileTo.builder()
          .identificativo(flusso.getCodIdentificativoFlusso())
          .name(fileName)
          .path(flusso.getDeNomeFileScaricato())
          .dataProduzione(Utilities.toLocalDateTime(flusso.getDtDataOraFlusso().toGregorianCalendar().getTime()))
          .dataCreazione(Utilities.toLocalDateTime(flusso.getDtCreazione().toGregorianCalendar().getTime())) // Data Disponibilità
          .dimensione(flusso.getNumDimensioneFileScaricato());
      files.add(builder.build());
    }
    return files;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void onUploadFile(String codFedUserId, Ente ente, String flussoType, String filePath, String authToken, MultipartFile file){

    this.validateFile(ente, file);

    switch (flussoType){
      case FlussoController.FILE_TYPE_FLUSSI_IMPORT:
        importDovutiService.insert(ente, codFedUserId, authToken);
        queueProducer.enqueueImportDovuti(filePath);
        break;
      default: throw new ValidatorException("invalid upload file type");
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void onUploadFile(String codIpa, String filePath, String authToken){
    importDovutiService.insert(codIpa, authToken);
    queueProducer.enqueueImportDovuti(filePath);
  }

  @Transactional(propagation =  Propagation.SUPPORTS)
  public Flusso getRend9Flusso(Ente ente) {

    Flusso flusso;
    String iuf = "_" + ente.getCodIpaEnte() + "_REND9";

    flusso = flussoDao.getByIuf(iuf).orElse(null);

    if (null == flusso) {
      AnagraficaStato anagraficaFlussoCaricato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_FLUSSO_CARICATO, Constants.STATO_TIPO_FLUSSO);
      flusso = new Flusso();
      flusso.setVersion(0);
      flusso.setMygovEnteId(ente);
      flusso.setMygovAnagraficaStatoId(anagraficaFlussoCaricato);
      flusso.setIuf(iuf);
      flusso.setNumRigheTotali(Long.parseLong("0"));
      flusso.setNumRigheImportateCorrettamente(Long.parseLong("0"));
      Date now = new Date();
      flusso.setDtCreazione(now);
      flusso.setDtUltimaModifica(now);
      flusso.setFlgAttivo(true);
      flusso.setDeNomeOperatore(null);
      flusso.setFlgSpontaneo(false);
      flusso.setDePercorsoFile(null);
      flusso.setDeNomeFile(null);
      long mygovFlussoId = flussoDao.insert(flusso);
      flusso.setMygovFlussoId(mygovFlussoId);
    }

    return flusso;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public void insertFlussiDefault(Ente ente) {
    AnagraficaStato asFlussoCaricato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_FLUSSO_CARICATO, Constants.STATO_TIPO_FLUSSO);

    var flussiDefaultList = mapFlussiDefault.apply(ente.getCodIpaEnte());

    if (Predicate.isEqual(0).negate().test(self.getByIuf(flussiDefaultList).size()))
      throw new MyPayException("Flusso presente nel database");
    flussiDefaultList.forEach(iuf -> self.insertDefault(ente, iuf, asFlussoCaricato));
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<Flusso> getByIuf(List<String> iufs) {
    return flussoDao.getByIuf(iufs);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Long insertDefault(Ente ente, String iuf, AnagraficaStato anagraficaStato) {
    Flusso flusso = Flusso.builder()
        .iuf(iuf)
        .flgSpontaneo(true)
        .version(0)
        .mygovEnteId(ente)
        .mygovAnagraficaStatoId(anagraficaStato)
        .numRigheTotali(0L)
        .numRigheImportateCorrettamente(0L)
        .flgAttivo(true)
        .build();
    return flussoDao.insert(flusso);
  }


  private void validateFile(Ente ente, MultipartFile file) {
    if (!StringUtils.endsWithIgnoreCase(file.getOriginalFilename(),".zip")) {
      throw new ValidatorException("Il formato del file deve essere lo zip.");
    } else
      try {
        String fileNameNoExtension = StringUtils.removeEndIgnoreCase(file.getOriginalFilename(),".zip");
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(file.getBytes()));
        ZipEntry entry;
        while((entry = zis.getNextEntry()) != null) {
          if(!StringUtils.startsWith(entry.getName(), fileNameNoExtension + "."))
            throw new ValidatorException("Il nome del file contenuto deve essere uguale al nome dello zip.");
        }
      } catch (IOException ex) {
        throw new ValidatorException("Verificato un errore durante la lettura dello zip caricato.");
      }

      if (!StringUtils.startsWith(file.getOriginalFilename(), ente.getCodIpaEnte()) )
        throw new ValidatorException("Il nome del file deve iniziare col codice dell'ente.");


    if (flussoDao.countDuplicateFileName(file.getOriginalFilename())>0)
      throw new ValidatorException("Lo stesso nome di file esiste gia'.");
  }

}
