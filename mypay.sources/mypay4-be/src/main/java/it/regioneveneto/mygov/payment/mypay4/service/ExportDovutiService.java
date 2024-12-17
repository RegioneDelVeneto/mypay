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

import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import it.regioneveneto.mygov.payment.mypay4.dao.*;
import it.regioneveneto.mygov.payment.mypay4.dto.WsExportFlussoIncomeTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class ExportDovutiService {

  @Autowired
  private EnteDao enteDao;
  @Autowired
  private UtenteDao utenteDao;
  @Autowired
  private ExportDovutiDao exportDovutiDao;
  @Autowired
  private ReceiptDao receiptDao;
  @Autowired
  private AnagraficaStatoDao anagraficaStatoDao;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private MailService mailService;

  @Value("${mypay.path.export.basePath}")
  private String dovutiExportPath;

  @Value("${mypay.path.export.directoryRootEnti}")
  private String directoryRootEnti;

  @Value("${mypay.path.export.url}")
  private String exportUrl;

  @Value("${mypay.path.export.email.ambiente}")
  private String emailAmbiente;


  @Transactional(propagation = Propagation.REQUIRED)
  public long insert(Long mygovEnteId, String codFedUserId, LocalDate extractionFrom, LocalDate extractionTo, String codTipoDovuto,
                     String codRequestToken, boolean flgRicevuta, boolean flgIncrementale, String versioneTracciato) {

    AnagraficaStato anagraficaStato = anagraficaStatoDao.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_LOAD, Constants.STATO_TIPO_EXPORT);
    ExportDovuti exportDovuti = new ExportDovuti();
    exportDovuti.setMygovEnteId(enteDao.getEnteById(mygovEnteId));
    exportDovuti.setMygovUtenteId(utenteDao.getByCodFedUserId(codFedUserId).orElseThrow(NotFoundException::new));
    exportDovuti.setDtInizioEstrazione(java.sql.Date.valueOf(extractionFrom));
    exportDovuti.setDtFineEstrazione(java.sql.Date.valueOf(extractionTo));
    exportDovuti.setMygovAnagraficaStatoId(anagraficaStato);
    exportDovuti.setCodTipoDovuto(codTipoDovuto);
    exportDovuti.setCodRequestToken(codRequestToken);
    exportDovuti.setFlgRicevuta(flgRicevuta);
    exportDovuti.setFlgIncrementale(flgIncrementale);
    exportDovuti.setVersioneTracciato(versioneTracciato);

    exportDovuti.setDtCreazione(new Date());
    exportDovuti.setDtUltimaModifica(new Date());
    return exportDovutiDao.insert(exportDovuti);
  }

  public List<ExportDovuti> getByEnteNomefileDtmodifica(Long mygovEnteId, String username, String nomeFile,
                                                        LocalDate dateFrom, LocalDate dateTo, int queryLimit) {
    return exportDovutiDao.getByEnteNomefileDtmodifica(mygovEnteId, username, nomeFile, dateFrom, dateTo, queryLimit);
  }

  public int getByEnteNomefileDtmodificaCount(Long mygovEnteId, String username, String nomeFile,
                                              LocalDate dateFrom, LocalDate dateTo) {
    return exportDovutiDao.getByEnteNomefileDtmodificaCount(mygovEnteId, username, nomeFile, dateFrom, dateTo);
  }

  public ExportDovuti getFlussoExport(String requestToken) {
    List<ExportDovuti> listExportDovuti = exportDovutiDao.getExportByRequestToken(requestToken);
    if (CollectionUtils.isEmpty(listExportDovuti)) {
      return null;
    } else if (listExportDovuti.size() > 1) {
      throw new MyPayException(messageSource.getMessage("pa.flusso.exportDovutiDuplicato", null, Locale.ITALY));
    }
    return listExportDovuti.get(0);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public ExportDovuti insertFlussoExport(WsExportFlussoIncomeTo to, String requestToken) {
    Date now = new Date();
    ExportDovuti exportDovuti = ExportDovuti.builder()
        .mygovEnteId(enteDao.getEnteByCodIpa(to.getCodIpaEnte()))
        .mygovAnagraficaStatoId(anagraficaStatoDao.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_LOAD, Constants.STATO_TIPO_EXPORT))
        .mygovUtenteId(utenteDao.getByCodFedUserId(to.getCodIpaEnte() + "-" + Constants.WS_USER).orElseThrow(NotFoundException::new))
        .dtInizioEstrazione(to.getDateFrom())
        .dtFineEstrazione(to.getDateTo())
        .codTipoDovuto(to.getIdentificativoTipoDovuto())
        .codRequestToken(requestToken)
        .flgRicevuta(to.isRicevuta())
        .flgIncrementale(to.isIncrementale())
        .versioneTracciato(to.getVersioneTracciato())
        .dtCreazione(now)
        .dtUltimaModifica(now)
        .flgMypivot(!StringUtils.isEmpty(to.getPasswordMypivot()))
        .build();
    long newId = exportDovutiDao.insert(exportDovuti);
    exportDovuti.setMygovExportDovutiId(newId);
    return exportDovuti;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void exportDovuti(Long mygovExportDovutiId) {
    log.debug("exportDovuti id[{}], update obsolete rows query", mygovExportDovutiId);

    Long idStatoExportError = anagraficaStatoDao.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_ERROR, Constants.STATO_TIPO_EXPORT).getMygovAnagraficaStatoId();
    Long idStatoExportElab = anagraficaStatoDao.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_IN_ELAB, Constants.STATO_TIPO_EXPORT).getMygovAnagraficaStatoId();
    exportDovutiDao.updateObsoleteRowsExportDovuti(idStatoExportError, idStatoExportElab);

    log.debug("exportDovuti id[{}], execute update_mygov_export_dovuti function", mygovExportDovutiId);
    int updateExport = exportDovutiDao.updateExportDovuti(mygovExportDovutiId, new Date());

    Map<String, Object> map = new HashMap<>();

    log.debug("exportDovuti id[{}], load export dovuti by id", mygovExportDovutiId);
    ExportDovuti exportDovuti = exportDovutiDao.getExportDovutoById(mygovExportDovutiId);

    if (updateExport != 1) {
      log.debug("exportDovuti id[{}], export terminated, nothing to export", mygovExportDovutiId);
      return;
    }

    Long idUtenteToLoad = exportDovuti.getMygovUtenteId().getMygovUtenteId();
    Long idEnteToLoad = exportDovuti.getMygovEnteId().getMygovEnteId();
    log.debug("exportDovuti id[{}], load utente by id[{}]", mygovExportDovutiId, idUtenteToLoad);
    Utente utente = utenteDao.serachUtenteOperatoreByIdAndIdEnte(idUtenteToLoad, idEnteToLoad);
    log.debug("exportDovuti id[{}], load ente by id[{}]", mygovExportDovutiId, idEnteToLoad);
    Ente ente = enteDao.getEnteById(idEnteToLoad);

    Long idEnte = exportDovuti.getMygovEnteId().getMygovEnteId();
    Date dtTimeInizioEstrazione = exportDovuti.getDtInizioEstrazione();
    Date dtInizioEstrazione = DateUtils.truncate(dtTimeInizioEstrazione, java.util.Calendar.DAY_OF_MONTH);
    Date dtTimeFineEstrazione = exportDovuti.getDtFineEstrazione();
    Date dtFineEstrazione = DateUtils.truncate(dtTimeFineEstrazione, java.util.Calendar.DAY_OF_MONTH);
    String codIpaEnte = ente.getCodIpaEnte();
    String codTipoDovuto = exportDovuti.getCodTipoDovuto();
    String codFedUserId = utente.getCodFedUserId();
    boolean flgIncrementale = exportDovuti.isFlgIncrementale();
    map.put("email_amministratore_ente", ente.getEmailAmministratore());
    map.put("operatore_export", utente.getDeEmailAddress());
    map.put("ente", ente.getDeNomeEnte());
    map.put("codRequestToken", exportDovuti.getCodRequestToken());

    boolean isWs = (utente.getCodFedUserId().endsWith("WS_USER") || utente.getCodCodiceFiscaleUtente().equals("USER_JAVA"));

    GregorianCalendar now = new GregorianCalendar();
    String cartella_anno_mese = now.get(GregorianCalendar.YEAR) + "_" + ((now.get(GregorianCalendar.MONTH) + 1) > 9 ? (now.get(GregorianCalendar.MONTH) + 1) : "0" + (now.get(GregorianCalendar.MONTH) + 1));

    String cartellaDestinazione = directoryRootEnti + "/" + ente.getCodIpaEnte() + "/EXPORT/" + cartella_anno_mese;
    String cartellaPathRelativo = "/EXPORT/" + cartella_anno_mese;
    String nomeFileExport = "";
    if (exportDovuti.getVersioneTracciato().equals("1.0")) {
      String dateTimeFormat = "yyyy-MM-dd_HH_mm_ss";
      SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(dateTimeFormat);
      nomeFileExport = exportDovuti.getMygovExportDovutiId() + "_ESTRAZIONE_DOVUTI_" + dateTimeFormatter.format(new Date());
    } else {
      String dateTimeFormat = "yyyyMMddHHmmss";
      SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(dateTimeFormat);
      nomeFileExport = ente.getCodIpaEnte() + "-" + exportDovuti.getMygovExportDovutiId() + "_ESTRAZIONE_DOVUTI_" + dateTimeFormatter.format(new Date()) + "-" + exportDovuti.getVersioneTracciato().replace(".", "_");
    }

    String cartellaTemporanea = dovutiExportPath + "/tmp_export";
    String fileTemporaneo = cartellaTemporanea + "/" + nomeFileExport + ".csv";

    map.put("nome_file_export", nomeFileExport);

    List<DovutoElaboratoWithAdditionalInfo> resDovutoElaborato = new ArrayList<>();
    log.debug("exportDovuti id[{}], execute export query on dovuto elaborato", mygovExportDovutiId);
    resDovutoElaborato = exportDovutiDao.getRowForExportDovuto(idEnte, flgIncrementale, codTipoDovuto, codFedUserId, dtTimeInizioEstrazione, dtTimeFineEstrazione, dtInizioEstrazione, dtFineEstrazione, codIpaEnte);
    log.debug("exportDovuti id[{}], export dovuto elaborato terminated. list size[{}]", mygovExportDovutiId, resDovutoElaborato.size());

    List<ReceiptWithAdditionalInfo> resReceipt = new ArrayList<>();
    if (exportDovuti.getVersioneTracciato().equals("1.3")) {
      log.debug("exportDovuti id[{}], execute export query on receipt", mygovExportDovutiId);
      resReceipt = receiptDao.getRowForExportDovutoFromReceipt(idEnte, flgIncrementale, codTipoDovuto, dtTimeInizioEstrazione, dtTimeFineEstrazione);
      log.debug("exportDovuti id[{}], export receipt terminated. list size[{}]", mygovExportDovutiId, resReceipt.size());
    }

    try {
      Files.createDirectories(Paths.get(cartellaDestinazione));
      log.debug("exportDovuti id[{}], creazione cartella [{}]", mygovExportDovutiId, cartellaDestinazione);
      Files.createDirectories(Paths.get(cartellaTemporanea));
      log.debug("exportDovuti id[{}], creazione cartella temporanea [{}]", mygovExportDovutiId, cartellaTemporanea);

      Writer writer = new FileWriter(fileTemporaneo);
      File outputZipFile = new File(cartellaDestinazione + "/" + nomeFileExport + ".zip");
      ZipArchiveOutputStream zos = new ZipArchiveOutputStream(outputZipFile);

      CSVWriter csvWriter = (CSVWriter) new CSVWriterBuilder(writer)
          .withSeparator(';')
          .build();

      File aFileInArchive = new File(fileTemporaneo);

      List<String[]> header = getHeaderExport(exportDovuti);
      List<String[]> data = addListDovutoElaboratotoStringArray(resDovutoElaborato, header, exportDovuti);
      data = addListReceiptToStringArray(resReceipt, data, exportDovuti);
      csvWriter.writeAll(data);
      csvWriter.close();

      log.debug("exportDovuti id[{}], chiusura csv writer", mygovExportDovutiId);

      ArchiveEntry entry = zos.createArchiveEntry(aFileInArchive, aFileInArchive.getName());
      zos.putArchiveEntry(entry);
      zos.write(Files.readAllBytes(aFileInArchive.toPath()));
      zos.closeArchiveEntry();
      zos.finish();

      log.debug("exportDovuti id[{}], creo zip file [{}]", mygovExportDovutiId, aFileInArchive.getAbsolutePath());

      Long fileSize = aFileInArchive.length();

      if (aFileInArchive.delete())
        log.debug("exportDovuti id[{}], deleted temp file [{}]", mygovExportDovutiId, aFileInArchive.getAbsolutePath());


      boolean mailAttachment = !resDovutoElaborato.isEmpty() || !resReceipt.isEmpty();
      if (!isWs) {
        sendMailExportDovutiOk(map, mailAttachment);
      }


      AnagraficaStato anagraficaStato = anagraficaStatoDao.getByCodStatoAndTipoStato("EXPORT_ESEGUITO", "export");

      exportDovuti.setVersion(exportDovuti.getVersion() + 1);
      if (mailAttachment) {
        exportDovuti.setDeNomeFileGenerato(cartellaPathRelativo + "/" + nomeFileExport + ".zip");
        exportDovuti.setNumDimensioneFileGenerato(fileSize);
      }
      exportDovuti.setDtUltimaModifica(new Date());
      exportDovuti.setMygovAnagraficaStatoId(anagraficaStato);

      log.debug("exportDovuti id[{}], update export dovuti attributes with stato [{}]", mygovExportDovutiId, anagraficaStato.getDeStato());
      exportDovutiDao.update(exportDovuti);


    } catch (IOException e) {
      log.error("exception on file managment [{}]", e.getMessage());
      throw new RuntimeException(e);
    }

  }

  private static String charToString(Character c) {
    if (c != null)
      return c.toString();
    else
      return "";

  }

  private List<String[]> getHeaderExport(ExportDovuti ed) {
    List<String[]> records = new ArrayList<String[]>();

    String[] initialBaseHeader = new String[]{"iuf", "numRigaFlusso", "codIud", "codIuv", "versioneOggetto", "identificativoDominio",
        "identificativoStazioneRichiedente", "identificativoMessaggioRicevuta", "dataOraMessaggioRicevuta", "riferimentoMessaggioRichiesta",
        "riferimentoDataRichiesta", "tipoIdentificativoUnivoco", "codiceIdentificativoUnivoco", "denominazioneAttestante", "codiceUnitOperAttestante",
        "denomUnitOperAttestante", "indirizzoAttestante", "civicoAttestante", "capAttestante", "localitaAttestante", "provinciaAttestante",
        "nazioneAttestante", "enteBenefTipoIdentificativoUnivoco", "enteBenefCodiceIdentificativoUnivoco", "denominazioneBeneficiario",
        "codiceUnitOperBeneficiario", "denomUnitOperBeneficiario", "indirizzoBeneficiario", "civicoBeneficiario", "capBeneficiario", "localitaBeneficiario",
        "provinciaBeneficiario", "nazioneBeneficiario", "soggVersTipoIdentificativoUnivoco", "soggVersCodiceIdentificativoUnivoco", "anagraficaVersante",
        "indirizzoVersante", "civicoVersante", "capVersante", "localitaVersante", "provinciaVersante", "nazioneVersante", "emailVersante", "soggPagTipoIdentificativoUnivoco",
        "soggPagCodiceIdentificativoUnivoco", "anagraficaPagatore", "indirizzoPagatore", "civicoPagatore", "capPagatore", "localitaPagatore", "provinciaPagatore",
        "nazionePagatore", "emailPagatore", "codiceEsitoPagamento", "importoTotalePagato", "identificativoUnivocoVersamento", "codiceContestoPagamento",
        "singoloImportoPagato", "esitoSingoloPagamento", "dataEsitoSingoloPagamento", "identificativoUnivocoRiscoss", "causaleVersamento",
        "datiSpecificiRiscossione", "tipoDovuto"};

    String[] headerWithFlagRicevuta = new String[]{};
    if (ed.isFlgRicevuta()) {
      headerWithFlagRicevuta = new String[]{"tipoFirma", "rt", "indiceDatiSingoloPagamento", "numRtDatiPagDatiSingPagCommissioniApplicatePsp",
          "codRtDatiPagDatiSingPagAllegatoRicevutaTipo", "blbRtDatiPagDatiSingPagAllegatoRicevutaTest"};
    }
    String[] headerVersion = new String[]{};
    switch (ed.getVersioneTracciato()) {
      case "1.2":
        headerVersion = new String[]{"bilancio"};
        break;
      case "1.3":
        headerVersion = new String[]{"bilancio", "cod_fiscale_pa1", "de_nome_pa1", "cod_tassonomico_dovuto_pa1"};
        break;
    }

    String[] finalBaseHeader = (String[]) ArrayUtils.addAll(headerWithFlagRicevuta, headerVersion);

    String[] headerArrayString = ArrayUtils.addAll(initialBaseHeader, finalBaseHeader);

    records.add(headerArrayString);

    return records;
  }


  private static List<String[]> addListDovutoElaboratotoStringArray(List<DovutoElaboratoWithAdditionalInfo> dovutoElaboratoList, List<String[]> records, ExportDovuti ed) {

    String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(dateTimeFormat);
    String dateFormat = "yyyy-MM-dd";
    SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);

    for (DovutoElaborato dE : (Iterable<DovutoElaboratoWithAdditionalInfo>) dovutoElaboratoList) {


      String numEDatiPagImportoTotalePagatoToExport = "";
      if (dE.getNumEDatiPagImportoTotalePagato() != null)
        numEDatiPagImportoTotalePagatoToExport = Utilities.parseImportoStringWithDotSeparator(dE.getNumEDatiPagImportoTotalePagato());

      String numEDatiPagDatiSingPagSingoloImportoPagatoToExport = "";
      if (dE.getNumEDatiPagDatiSingPagSingoloImportoPagato() != null)
        numEDatiPagDatiSingPagSingoloImportoPagatoToExport = Utilities.parseImportoStringWithDotSeparator(dE.getNumEDatiPagDatiSingPagSingoloImportoPagato());

      String codEDataOraMessaggioRicevutaFormatted = "";
      if (dE.getCodEDataOraMessaggioRicevuta() != null) {
        codEDataOraMessaggioRicevutaFormatted = dateTimeFormatter.format(dE.getCodEDataOraMessaggioRicevuta()).replace(" ", "T");
      }

      String[] initialRow = new String[]{dE.getMygovFlussoId().getIuf(), "" + dE.getNumRigaFlusso(), dE.getCodIud(),
          StringUtils.stripToEmpty(dE.getCodRpSilinviarpIdUnivocoVersamento()), StringUtils.stripToEmpty(dE.getDeEVersioneOggetto()),
          StringUtils.stripToEmpty(dE.getCodEDomIdDominio()), StringUtils.stripToEmpty(dE.getCodEDomIdStazioneRichiedente()),
          StringUtils.stripToEmpty(dE.getCodEIdMessaggioRicevuta()), codEDataOraMessaggioRicevutaFormatted,
          StringUtils.stripToEmpty(dE.getCodERiferimentoMessaggioRichiesta()), dateFormatter.format(dE.getCodERiferimentoDataRichiesta()),
          charToString(dE.getCodEIstitAttIdUnivAttTipoIdUnivoco()), StringUtils.stripToEmpty(dE.getCodEIstitAttIdUnivAttCodiceIdUnivoco()),
          StringUtils.stripToEmpty(dE.getDeEIstitAttDenominazioneAttestante()), StringUtils.stripToEmpty(dE.getCodEIstitAttCodiceUnitOperAttestante()),
          StringUtils.stripToEmpty(dE.getDeEIstitAttDenomUnitOperAttestante()), StringUtils.stripToEmpty(dE.getDeEIstitAttIndirizzoAttestante()),
          StringUtils.stripToEmpty(dE.getDeEIstitAttCivicoAttestante()), StringUtils.stripToEmpty(dE.getCodEIstitAttCapAttestante()),
          StringUtils.stripToEmpty(dE.getDeEIstitAttLocalitaAttestante()), StringUtils.stripToEmpty(dE.getDeEIstitAttProvinciaAttestante()),
          StringUtils.stripToEmpty(dE.getCodEIstitAttNazioneAttestante()), charToString(dE.getCodEEnteBenefIdUnivBenefTipoIdUnivoco()),
          StringUtils.stripToEmpty(dE.getCodEEnteBenefIdUnivBenefCodiceIdUnivoco()), StringUtils.stripToEmpty(dE.getDeEEnteBenefDenominazioneBeneficiario()),
          StringUtils.stripToEmpty(dE.getCodEEnteBenefCodiceUnitOperBeneficiario()), StringUtils.stripToEmpty(dE.getDeEEnteBenefDenomUnitOperBeneficiario()),
          StringUtils.stripToEmpty(dE.getDeEEnteBenefIndirizzoBeneficiario()), StringUtils.stripToEmpty(dE.getDeEEnteBenefCivicoBeneficiario()),
          StringUtils.stripToEmpty(dE.getCodEEnteBenefCapBeneficiario()), StringUtils.stripToEmpty(dE.getDeEEnteBenefLocalitaBeneficiario()),
          StringUtils.stripToEmpty(dE.getDeEEnteBenefProvinciaBeneficiario()), StringUtils.stripToEmpty(dE.getCodEEnteBenefNazioneBeneficiario()),
          charToString(dE.getCodESoggVersIdUnivVersTipoIdUnivoco()), StringUtils.stripToEmpty(dE.getCodESoggVersIdUnivVersCodiceIdUnivoco()),
          StringUtils.stripToEmpty(dE.getCodESoggVersAnagraficaVersante()), StringUtils.stripToEmpty(dE.getDeESoggVersIndirizzoVersante()),
          StringUtils.stripToEmpty(dE.getDeESoggVersCivicoVersante()), StringUtils.stripToEmpty(dE.getCodESoggVersCapVersante()),
          StringUtils.stripToEmpty(dE.getDeESoggVersLocalitaVersante()), StringUtils.stripToEmpty(dE.getDeESoggVersProvinciaVersante()),
          StringUtils.stripToEmpty(dE.getCodESoggVersNazioneVersante()), StringUtils.stripToEmpty(dE.getDeESoggVersEmailVersante()),
          charToString(dE.getCodESoggPagIdUnivPagTipoIdUnivoco()), StringUtils.stripToEmpty(dE.getCodESoggPagIdUnivPagCodiceIdUnivoco()),
          StringUtils.stripToEmpty(dE.getCodESoggPagAnagraficaPagatore()), StringUtils.stripToEmpty(dE.getDeESoggPagIndirizzoPagatore()),
          StringUtils.stripToEmpty(dE.getDeESoggPagCivicoPagatore()), StringUtils.stripToEmpty(dE.getCodESoggPagCapPagatore()),
          StringUtils.stripToEmpty(dE.getDeESoggPagLocalitaPagatore()), StringUtils.stripToEmpty(dE.getDeESoggPagProvinciaPagatore()),
          StringUtils.stripToEmpty(dE.getCodESoggPagNazionePagatore()), StringUtils.stripToEmpty(dE.getDeESoggPagEmailPagatore()),
          charToString(dE.getCodEDatiPagCodiceEsitoPagamento()), numEDatiPagImportoTotalePagatoToExport, StringUtils.stripToEmpty(dE.getCodEDatiPagIdUnivocoVersamento()),
          StringUtils.stripToEmpty(dE.getCodEDatiPagCodiceContestoPagamento()), numEDatiPagDatiSingPagSingoloImportoPagatoToExport,
          StringUtils.stripToEmpty(dE.getDeEDatiPagDatiSingPagEsitoSingoloPagamento()),
          dE.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento() != null ? dateFormatter.format(dE.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento()) : "",
          StringUtils.stripToEmpty(dE.getCodEDatiPagDatiSingPagIdUnivocoRiscoss()), Utilities.shortenString(dE.getDeRpDatiVersDatiSingVersCausaleVersamento(), 1024),
          StringUtils.stripToEmpty(dE.getDeEDatiPagDatiSingPagDatiSpecificiRiscossione()), StringUtils.stripToEmpty(dE.getCodTipoDovuto())};


      String[] rowWithRicevuta = new String[]{};
      if (ed.isFlgRicevuta()) {

        String blolRtToExport = "";
        if (dE.getBlbRtPayload() != null)
          blolRtToExport = Base64.getEncoder().encodeToString(dE.getBlbRtPayload());

        String numEDatiPagDatiSingPagCommissioniApplicatePspToExport = "";
        if (dE.getNumEDatiPagDatiSingPagCommissioniApplicatePsp() != null)
          numEDatiPagDatiSingPagCommissioniApplicatePspToExport = Utilities.parseImportoStringWithDotSeparator(dE.getNumEDatiPagDatiSingPagCommissioniApplicatePsp());

        String blobToExport = "";
        if (dE.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo() != null) {
          if (dE.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo().equals("BD")) {
            blobToExport = Base64.getEncoder().encodeToString(dE.getBlbEDatiPagDatiSingPagAllegatoRicevutaTest());
          } else if (dE.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo().equals("ES")) {
            blobToExport = new String(dE.getBlbEDatiPagDatiSingPagAllegatoRicevutaTest(), StandardCharsets.UTF_8);
          }
        }

        rowWithRicevuta = new String[]{StringUtils.stripToEmpty(dE.getDeRtInviartTipoFirma()), blolRtToExport, "" + dE.getIndiceDatiSingoloPagamento(),
            numEDatiPagDatiSingPagCommissioniApplicatePspToExport, StringUtils.stripToEmpty(dE.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo()), blobToExport};
      }

      String[] rowVersion = new String[]{};
      switch (ed.getVersioneTracciato()) {
        case "1.2":
          rowVersion = new String[]{StringUtils.stripToEmpty(dE.getBilancio())};
          break;
        case "1.3":
          rowVersion = new String[]{StringUtils.stripToEmpty(dE.getBilancio()), "", "", ""};
          break;
      }

      String[] finalRow = (String[]) ArrayUtils.addAll(rowWithRicevuta, rowVersion);

      String[] row = (String[]) ArrayUtils.addAll(initialRow, finalRow);

      records.add(row);

    }
    return records;
  }

    private static List<String[]> addListReceiptToStringArray(List<ReceiptWithAdditionalInfo> receiptList, List<String[]> records, ExportDovuti ed) {


        String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(dateTimeFormat);
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);

        for (ReceiptWithAdditionalInfo receipt : (Iterable<ReceiptWithAdditionalInfo>) receiptList) {

            String transferAmount2ToExport = "";
            if (receipt.getTransferAmount2() != null)
                transferAmount2ToExport = Utilities.parseImportoStringWithDotSeparator(receipt.getTransferAmount2());


            String[] initialRow = new String[]{"", "", "", receipt.getCreditorReferenceId(), "", receipt.getFiscalCodePa2(), "0007580279_03", receipt.getReceiptId(),
                    dateTimeFormatter.format(receipt.getPaymentDateTime()).replace(" ", "T"),
                    StringUtils.stripToEmpty(receipt.getReceiptId()), dateFormatter.format(receipt.getPaymentDateTime()), "G",
                    StringUtils.stripToEmpty(receipt.getIdPsp()), StringUtils.stripToEmpty(receipt.getPspCompanyName()), "", "", "", "", "", "", "", "", "G",
                    StringUtils.stripToEmpty(receipt.getFiscalCodePa2()), StringUtils.stripToEmpty(receipt.getMygovEnteId().getDeNomeEnte()), "", "",
                    StringUtils.stripToEmpty(receipt.getMygovEnteId().getDeRpEnteBenefIndirizzoBeneficiario()),
                    StringUtils.stripToEmpty(receipt.getMygovEnteId().getDeRpEnteBenefCivicoBeneficiario()),
                    StringUtils.stripToEmpty(receipt.getMygovEnteId().getCodRpEnteBenefCapBeneficiario()),
                    StringUtils.stripToEmpty(receipt.getMygovEnteId().getDeRpEnteBenefLocalitaBeneficiario()),
                    StringUtils.stripToEmpty(receipt.getMygovEnteId().getDeRpEnteBenefProvinciaBeneficiario()),
                    StringUtils.stripToEmpty(receipt.getMygovEnteId().getCodRpEnteBenefNazioneBeneficiario()),
                    StringUtils.stripToEmpty(receipt.getUniqueIdentifierTypePayer()), StringUtils.stripToEmpty(receipt.getUniqueIdentifierValuePayer()),
                    StringUtils.stripToEmpty(receipt.getFullNamePayer()), StringUtils.stripToEmpty(receipt.getStreetNamePayer()),
                    StringUtils.stripToEmpty(receipt.getCivicNumberPayer()), StringUtils.stripToEmpty(receipt.getPostalCodePayer()),
                    StringUtils.stripToEmpty(receipt.getCityPayer()), StringUtils.stripToEmpty(receipt.getStateProvinceRegionPayer()),
                    StringUtils.stripToEmpty(receipt.getCountryPayer()), StringUtils.stripToEmpty(receipt.getEmailPayer()),
                    StringUtils.stripToEmpty(receipt.getUniqueIdentifierTypeDebtor()), StringUtils.stripToEmpty(receipt.getUniqueIdentifierValueDebtor()),
                    StringUtils.stripToEmpty(receipt.getFullNameDebtor()), StringUtils.stripToEmpty(receipt.getStreetNameDebtor()), StringUtils.stripToEmpty(receipt.getCivicNumberDebtor()),
                    StringUtils.stripToEmpty(receipt.getPostalCodeDebtor()), StringUtils.stripToEmpty(receipt.getCityDebtor()), StringUtils.stripToEmpty(receipt.getStateProvinceRegionDebtor()),
                    StringUtils.stripToEmpty(receipt.getCountryDebtor()), StringUtils.stripToEmpty(receipt.getEmailDebtor()), "0",
                    transferAmount2ToExport, StringUtils.stripToEmpty(receipt.getCreditorReferenceId()), StringUtils.stripToEmpty(receipt.getReceiptId()), transferAmount2ToExport, "0",
                    dateFormatter.format(receipt.getPaymentDateTime()), StringUtils.stripToEmpty(receipt.getReceiptId()),
                    StringUtils.stripToEmpty(receipt.getDeRpDatiVersDatiSingVersCausaleVersamento()),
                    StringUtils.stripToEmpty(receipt.getDeEDatiPagDatiSingPagDatiSpecificiRiscossione()), "EXPORT_ENTE_SECONDARIO"};


            String[] rowWithRicevuta = new String[]{};
            if (ed.isFlgRicevuta()) {

                String feeToExport = "";
                if (receipt.getFee() != null)
                    feeToExport = Utilities.parseImportoStringWithDotSeparator(receipt.getFee());

                String blobToExport = "";
                if (receipt.getReceiptBytes() != null)
                    blobToExport = Base64.getEncoder().encodeToString(receipt.getReceiptBytes());

                rowWithRicevuta = new String[]{"", blobToExport, "2", feeToExport, "", ""};
            }

            String[] rowVersion = new String[]{};
            switch (ed.getVersioneTracciato()) {
                case "1.2":
                    rowVersion = new String[]{""};
                    break;
                case "1.3":
                    rowVersion = new String[]{"", StringUtils.stripToEmpty(receipt.getFiscalCode()), StringUtils.stripToEmpty(receipt.getCompanyName()),
                            StringUtils.stripToEmpty(receipt.getTransferCategory1())};
                    break;
            }

            String[] finalRow = (String[]) ArrayUtils.addAll(rowWithRicevuta, rowVersion);

            String[] row = (String[]) ArrayUtils.addAll(initialRow, finalRow);

            records.add(row);
        }
        return records;
    }


    public void sendMailExportDovutiOk(Map<String, Object> inputParams, boolean withAttachment) {
    Map<String, String> params = new HashMap<>();
    String mailTo = "";
    String mailCC = "";
    params.put("ente", (String) inputParams.get("ente"));
    params.put("ambiente", emailAmbiente);

    String indirizzoOperatoreRichiedente = (String) inputParams.get("operatore_export");
    String emailAmministratoreEnte = (String) inputParams.get("email_amministratore_ente");

    if (StringUtils.isNotBlank(indirizzoOperatoreRichiedente)) {
      mailTo = indirizzoOperatoreRichiedente;
      if (!indirizzoOperatoreRichiedente.equals(emailAmministratoreEnte)) {
        mailCC = emailAmministratoreEnte;
      }
    } else {
      mailTo = emailAmministratoreEnte;
    }

    DateFormat parser = new SimpleDateFormat("EEE, MMM dd yyyy, hh:mm:ss");
    String dataAttuale = parser.format(new Date());
    params.put("dataAttuale", dataAttuale);

    String testoMail = "";
    if (withAttachment)
      testoMail = "Estrazione terminata con successo." + "\r\n" + "Clicca sul link qui sotto per scaricare il file con i dovuti estratti: " + "\r\n" + exportUrl + "?codRequestToken=" + (String) inputParams.get("codRequestToken") + " \r\n" + "Il nome del file creato e': " + (String) inputParams.get("nome_file_export") + ".zip";
    else
      testoMail = "Nessun file generato. Non e' stato trovato alcun dovuto pagato nell'intervallo di date inserito.";

    params.put("testoMail", testoMail);

    mailService.sendMailExportDovutiOk(new String[]{mailTo}, (org.springframework.util.StringUtils.hasText(mailCC) ? new String[]{mailCC} : null), params);
  }

}
