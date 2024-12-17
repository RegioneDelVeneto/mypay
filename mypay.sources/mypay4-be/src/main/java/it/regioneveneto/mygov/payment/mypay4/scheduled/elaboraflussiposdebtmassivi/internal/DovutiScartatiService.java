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
package it.regioneveneto.mygov.payment.mypay4.scheduled.elaboraflussiposdebtmassivi.internal;

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.STATO_FILE_INVIO_MASSIVO_POSIZ_DEBT;
import static it.regioneveneto.mygov.payment.mypay4.util.Constants.STATO_FLUSSO_CARICATO;

/**
 * Questo servizio gestisce l'elaborazione dei flussi massivi di posizioni debitorie.
 */
@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
@Transactional(propagation = Propagation.REQUIRED)
public class DovutiScartatiService {

    private static final String HEADER_CSV = "IUD;codIuv;tipoIdentificativoUnivoco;codiceIdentificativoUnivoco;" +
            "anagraficaPagatore;indirizzoPagatore;civicoPagatore;capPagatore;" +
            "localitaPagatore;provinciaPagatore;nazionePagatore;mailPagatore;dataEsecuzionePagamento;" +
            "importoDovutoPagato;commissioneCaricoPa;tipoDovuto;tipoVersamento;causaleVersamento;" +
            "datiSpecificiRiscossione;bilancio;azione;cod_rifiuto;de_rifiuto\n";
    private static final String GPD_ERROR_LABEL = "GPD_ERROR";


    @Autowired
    GpdSyncService gpdSyncService;

    @Autowired
    AnagraficaStatoService anagraficaStatoService;

    @Autowired
    private DovutoService dovutoService;

    @Autowired
    private GpdErrDovutoService gpdErrDovutoService;

    @Autowired
    private FlussoService flussoService;

    @Autowired
    private ImportDovutiService importDovutiService;

    @Autowired
    private MailWrapperService mailWrapperService;

    @Value("${task.importFlusso.context.directory_root_enti}")
    private String dataPathString;


    /**
     * Genera un file unico degli scarti per tutti i flussi massivi di posizioni debitorie.
     * Per ogni flusso, recupera le posizioni debitorie con errore e le scrive su un file CSV.
     * Infine, crea un file zip contenente tutti i file CSV degli scarti.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void generaFileDegliScarti() {
        log.info("Inizio generazione file unico degli scarti");

        List<GpdSync> gpdSyncElaborati = gpdSyncService.getByCodStato(
                STATO_FILE_INVIO_MASSIVO_POSIZ_DEBT.ELABORATO.getValue(), Integer.MAX_VALUE);

        Set<Long> idsFlussiElaborati = gpdSyncElaborati.stream()
                .filter(gpdSync -> gpdSync.getMygovFlussoId() != null)
                .map(gpdSync -> gpdSync.getMygovFlussoId())
                .collect(Collectors.toSet());

        idsFlussiElaborati.forEach(idFlusso -> {
            Flusso flusso = flussoService.getById(idFlusso);
            Long numRigheImportateCorrettamente = flusso.getNumRigheImportateCorrettamente();

            Long numRigheElaborate = gpdSyncElaborati.stream()
                    .filter(gpdSync -> gpdSync.getMygovFlussoId() != null && gpdSync.getMygovFlussoId().equals(idFlusso))
                    .mapToLong(gpdSync -> gpdSync.getNumDovutiElaborati())
                    .sum();

            Integer numDovutiConIuvNULL = dovutoService.getCountDovutiWithIuvNullByFlusso(flusso.getMygovFlussoId());

            if (numRigheElaborate.equals(numRigheImportateCorrettamente - numDovutiConIuvNULL)) {
                log.info("Tutte le {} posizioni debitorie del flusso {} sono state elaborate correttamente dalla GPD",
                        numRigheElaborate, idFlusso);
                log.info("Si procede ora alla creazione del file degli scarti per il flusso {}", idFlusso);

                List<Dovuto> errDovutiByIdFlusso = dovutoService.getErrDovutiByIdFlusso(flusso.getMygovFlussoId());

                if (!errDovutiByIdFlusso.isEmpty()) {

                    String scartiZipFile = getZipScarti(flusso);
                    Path scartiZipFilePath = Paths.get(scartiZipFile);
                    Path csvFile = Paths.get(scartiZipFile.replace("_SCARTI.zip", "_SCARTI.csv"));

                    creaSeNecessarioFileCsvScarti(scartiZipFilePath, csvFile);

                    try (BufferedWriter writer = Files.newBufferedWriter(csvFile, StandardOpenOption.APPEND)) {
                        errDovutiByIdFlusso.forEach(dovuto -> {
                            GpdErrDovuto gpdErrDovuto = gpdErrDovutoService.getByIupd(dovuto.getGpdIupd());
                            try {
                                writer.write(
                                        dovuto.getCodIud() + ";"
                                                + dovuto.getCodIuv() + ";"
                                                + dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco() + ";"
                                                + StringUtils.defaultString(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco()) + ";"
                                                + StringUtils.defaultString(dovuto.getDeRpSoggPagAnagraficaPagatore()) + ";"
                                                + StringUtils.defaultString(dovuto.getDeRpSoggPagIndirizzoPagatore()) + ";"
                                                + StringUtils.defaultString(dovuto.getDeRpSoggPagCivicoPagatore()) + ";"
                                                + StringUtils.defaultString(dovuto.getCodRpSoggPagCapPagatore()) + ";"
                                                + StringUtils.defaultString(dovuto.getDeRpSoggPagLocalitaPagatore()) + ";"
                                                + StringUtils.defaultString(dovuto.getDeRpSoggPagProvinciaPagatore()) + ";"
                                                + StringUtils.defaultString(dovuto.getCodRpSoggPagNazionePagatore()) + ";"
                                                + StringUtils.defaultString(dovuto.getDeRpSoggPagEmailPagatore()) + ";"
                                                + (dovuto.getDtRpDatiVersDataEsecuzionePagamento() != null ? dovuto.getDtRpDatiVersDataEsecuzionePagamento() : "") + ";"
                                                + dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento() + ";"
                                                + (dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa() != null ? dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa().toString() : "") + ";"
                                                + StringUtils.defaultString(dovuto.getCodTipoDovuto()) + ";"
                                                + StringUtils.defaultString(dovuto.getCodRpDatiVersTipoVersamento()) + ";"
                                                + StringUtils.defaultString(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento()) + ";"
                                                + StringUtils.defaultString(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione()) + ";"
                                                + StringUtils.defaultString(dovuto.getBilancio()) + ";"

                                                + StringUtils.defaultString(gpdErrDovuto.getAzione()) + ";"
                                                + GPD_ERROR_LABEL + ";"
                                                + gpdErrDovuto.getCodErroreHttp() + "-" + gpdErrDovuto.getDescrizione() + "\n");

                            } catch (IOException e) {
                                mailWrapperService.inviaMailImportFlussoError(idFlusso, "ERR_SCRIT",
                                        "Impossibile scrivere sul file degli scarti");
                                throw new MyPayException("Impossibile scrivere sul file degli scarti", e);
                            }
                        });

                    } catch (IOException e) {
                        mailWrapperService.inviaMailImportFlussoError(idFlusso, "ERR_SCRIT",
                                "Impossibile scrivere sul file degli scarti");
                        throw new MyPayException("Impossibile scrivere sul file degli scarti", e);
                    }


                    if (Files.exists(scartiZipFilePath)) {
                        try {
                            Files.delete(scartiZipFilePath);
                        } catch (IOException e) {
                            throw new MyPayException("Impossibile eliminare il file degli scarti", e);
                        }
                    }

                    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(scartiZipFile))) {
                        zos.putNextEntry(new ZipEntry(csvFile.getFileName().toString()));
                        Files.copy(csvFile, zos);
                        Files.delete(csvFile);
                    } catch (IOException e) {
                        throw new MyPayException("Error during the zipping or deletion of the scarti file", e);
                    }

                    log.info("Il file degli scarti {} è stato creato", scartiZipFile);
                    mailWrapperService.inviaMailImportFlussoKO(idFlusso);
                } else {
                    log.info("Il flusso {} non ha posizioni debitorie con errori", idFlusso);
                    mailWrapperService.inviaMailImportFlussoOk(idFlusso);
                }
                gpdSyncService.updateStatusByFlussoId(STATO_FILE_INVIO_MASSIVO_POSIZ_DEBT.COMPLETATO.getValue(), idFlusso);

                Long numRigheImportateCorrettamenteUpdate =
                        flusso.getNumRigheImportateCorrettamente() - errDovutiByIdFlusso.size();

                AnagraficaStato statoCaricato = anagraficaStatoService.getByCodStatoAndTipoStato(
                        STATO_FLUSSO_CARICATO, Constants.STATO_TIPO_FLUSSO);

                flussoService.updateStatoNumDovutiImportati(flusso.getMygovFlussoId(),
                        statoCaricato.getMygovAnagraficaStatoId(), numRigheImportateCorrettamenteUpdate);

                ImportDovuti flussoImport = importDovutiService.getFlussoImport(flusso.getCodRequestToken());
                if (flussoImport != null) {
                    importDovutiService.updateImportFlusso(flussoImport, "IMPORT_ESEGUITO");
                }

            } else {
                log.info("Il flusso {} ha {} posizioni debitorie importate correttamente ma solo {} posizioni debitorie elaborate." +
                                " NON è ancora possibile procedere alla generazione del file degli scarti.",
                        idFlusso, numRigheImportateCorrettamente, numRigheElaborate);
            }

        });


        log.info("Fine generazione file unico degli scarti");
    }

    private void creaSeNecessarioFileCsvScarti(Path scartiZipPath, Path csvFile) {
        if (Files.exists(scartiZipPath)) {
            log.info("Il file degli scarti {} esiste già", scartiZipPath.toString());
            unzipFile(scartiZipPath.toString(), scartiZipPath.getParent().toString());

        } else {
            log.info("Il file degli scarti {}  non esiste: si crea vuoto", scartiZipPath.toString());
            try {
                Files.createFile(csvFile);
                try (BufferedWriter writer = Files.newBufferedWriter(csvFile)) {
                    writer.write(HEADER_CSV);
                }
            } catch (IOException e) {
                throw new MyPayException("Impossibile creare il file degli scarti", e);
            }
        }
    }


    /**
     * Restituisce il percorso del file zip degli scarti.
     *
     * @param flusso flusso massivo di posizioni debitorie
     * @return percorso del file zip degli scarti
     */
    private String getZipScarti(Flusso flusso) {
        String zipfile =
                dataPathString + File.separatorChar +
                        flusso.getMygovEnteId().getCodIpaEnte() + File.separatorChar +
                        flusso.getDePercorsoFile() + File.separatorChar
                        + flusso.getDeNomeFile();

        return zipfile.replaceFirst("\\.zip$", "_SCARTI.zip");
    }

    /**
     * Scompatta un file zip.
     *
     * @param zipFilePath percorso del file zip
     * @param destDir     directory di destinazione
     */
    public void unzipFile(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                log.info("Unzipping to " + newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            String errorMessage = "Errore nella scompattazione del file degli scarti";
            log.error(errorMessage, e);
            throw new MyPayException(errorMessage, e);
        }
    }


}
