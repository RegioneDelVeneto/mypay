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

import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.massivo.UploadReport;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.massivo.UploadStatus;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.GpdErrDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.GpdSync;
import it.regioneveneto.mygov.payment.mypay4.scheduled.importflusso.pagopa.util.FileUtils;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.GpdMassiveClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.*;

/**
 * This service manages the processing of massive flows of debtor positions.
 */
@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
@Transactional(propagation = Propagation.REQUIRED)
public class AllineaGpdStatusDaPagoPaService {

    private static final String OUTCOME_SUFFIX_IN = "outcome.txt";
    private static final String OUTCOME_SUFFIX_OUT = "outcome.zip";

    @Autowired
    GpdSyncService gpdSyncService;
    @Autowired
    AnagraficaStatoService anagraficaStatoService;
    @Autowired
    private DovutoService dovutoService;
    @Autowired
    private DovutoPreloadService dovutoPreloadService;
    @Autowired
    private EnteService enteService;
    @Autowired
    private GpdErrDovutoService gpdErrDovutoService;
    @Autowired
    private DovutiScartatiService dovutiScartatiService;


    @Value("${pa.gpd.enabled}")
    private boolean gpdEnabled;

    @Value("${pa.gpd.preload}")
    private boolean gpdPreload;

    @Autowired
    private GpdMassiveClientService gpdMassiveClientService;

    @Value("${task.elaboraFlussiMassiviPosDebt.directory_root_elaborazione}")
    private String rootWorkingFolder;

    @Value("${task.elaboraFlussiMassiviPosDebt.max_richieste_esito_elaborazione}")
    private int maxRichiesteEsitoElaborazione;

    /**
     * Processes the massive flows of debtor positions.
     * Retrieves all the sent flows and processes them one by one.
     */
    @Transactional(propagation = Propagation.NEVER)
    public void process() {

        log.info("*** Start of the phase of retrieving the processing outcomes of debtor positions sent to PAGOPA ***");

        List<GpdSync> gpdSyncInviati = gpdSyncService.getByCodStato(STATO_FILE_INVIO_MASSIVO_POSIZ_DEBT.INVIATO.getValue(),
                maxRichiesteEsitoElaborazione);
        log.info("Files stored in DB but still in SENT status: {}", gpdSyncInviati.size());

        gpdSyncInviati.forEach(gpdSyncInviato -> {
            log.info("Retrieving processing outcome of the file {} [{}]",
                    gpdSyncInviato.getFilename(), gpdSyncInviato.getMygovGpdSyncId());
            processGpdSync(gpdSyncInviato);
        });
        log.info("*** End of the retrieval of processing outcomes for the sent files ***");

        if (gpdEnabled) {
            dovutiScartatiService.generaFileDegliScarti();
        }

    }


    /**
     * Processes a single sent zip file, identified by a gpd_sync record.
     * Retrieves the status of the file and, if all debtor positions have been processed,
     * requests the processing report.
     *
     * @param gpdSyncInviato massive flow of debtor positions
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void processGpdSync(GpdSync gpdSyncInviato) {

        Ente ente = enteService.getEnteById(gpdSyncInviato.getMygovEnteId());
        UploadStatus uploadStatus = null;
        try {
            uploadStatus = gpdMassiveClientService.getUploadStatus(
                    ente.getCodiceFiscaleEnte(), gpdSyncInviato.getFileId()
            );
        } catch (Exception e) {

            log.error("Error retrieving upload status for file ID {}: {}", gpdSyncInviato.getFileId(), e.getMessage());
            updateElaborazioni(gpdSyncInviato, 0);

            if (gpdSyncInviato.getNumRichElaborazione() >= this.maxRichiesteEsitoElaborazione) {
                log.error("Maximum number of processing outcome requests reached for the file {}", gpdSyncInviato.getFileId());
                gpdSyncService.updateStatus(gpdSyncInviato.getMygovGpdSyncId(),
                        STATO_FILE_INVIO_MASSIVO_POSIZ_DEBT.ERR_MAX_RICH_ESITO_ELAB.getValue(),
                        gpdSyncInviato.getNumDovutiElaborati());
            }
            return;
        }

        if (uploadStatus != null) {
            log.debug("Status of fileId {}: {}", gpdSyncInviato.getFileId(), uploadStatus);
            updateElaborazioni(gpdSyncInviato, uploadStatus.getProcessedItem());

            boolean isProcessingComplete = uploadStatus.getProcessedItem() > 0
                    && uploadStatus.getSubmittedItem().equals(uploadStatus.getProcessedItem());

            String processingStatus = isProcessingComplete ? "TERMIMATA" : "ANCORA IN CORSO";

            log.info("Processing {} for the file {} [{}]", processingStatus, gpdSyncInviato.getFilename(), gpdSyncInviato.getFileId());
            log.info("Processed {} / {}", uploadStatus.getProcessedItem(), uploadStatus.getSubmittedItem());
            if (isProcessingComplete) {
                processReport(ente, gpdSyncInviato);
            } else if (gpdSyncInviato.getNumRichElaborazione() >= this.maxRichiesteEsitoElaborazione) {
                log.error("Maximum number of processing outcome requests reached for the file {}", gpdSyncInviato.getFileId());
                gpdSyncService.updateStatus(gpdSyncInviato.getMygovGpdSyncId(),
                        STATO_FILE_INVIO_MASSIVO_POSIZ_DEBT.ERR_MAX_RICH_ESITO_ELAB.getValue(),
                        uploadStatus.getProcessedItem());
            }
        }
    }

    /**
     * Updates the number of processed debtor positions in a gpd_sync record.
     *
     * @param gpdSyncInviato     gpd_sync record
     * @param numDovutiElaborati number of processed debtor positions
     */
    private void updateElaborazioni(GpdSync gpdSyncInviato, int numDovutiElaborati) {
        gpdSyncService.updateElaborazioni(gpdSyncInviato.getMygovGpdSyncId(), numDovutiElaborati);
        gpdSyncInviato.setNumRichElaborazione(gpdSyncInviato.getNumRichElaborazione() + 1);
    }

    /**
     * Processes the report of a sent zip file.
     * Creates an outcome file with information on the processed debtor positions.
     * Updates the status of the debtor positions and the synchronization status with PagoPA
     * based on the processing outcome.
     *
     * @param ente           entity of debtor positions
     * @param gpdSyncInviato gpd_sync record related to the zip file
     */
    private void processReport(Ente ente, GpdSync gpdSyncInviato) {

        UploadReport uploadReport = gpdMassiveClientService.getUploadReport(
                ente.getCodiceFiscaleEnte(), gpdSyncInviato.getFileId()
        );

        Path exportFolder = FileUtils.createFluxExportFolder(
                rootWorkingFolder,
                ente.getCodiceFiscaleEnte());

        String zipFilename = exportFolder.toString() + File.separatorChar + gpdSyncInviato.getFilename();
        String outcomeFilename = zipFilename.replaceFirst("\\.zip$", "." + OUTCOME_SUFFIX_IN);
        String azione = recuperaAzioneByFilename(gpdSyncInviato.getFilename());

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outcomeFilename))) {

            uploadReport.getResponses().forEach(responseEntry -> {
                log.info("Processing the report for the file {}", gpdSyncInviato.getFileId());
                log.debug("ResponseEntry: {}", responseEntry);
                // The requestIDs are essentially the IUPD of the debtor positions
                List<String> requestIDs = responseEntry.getRequestIDs();

                try {

                    switch (responseEntry.getStatusCode()) {

                        case 201:
                            // CREATION OF DEBTOR POSITIONS IN PAGOPA
                            // Updating the status of debtor positions
                            // and the synchronization status with PagoPA
                            // in case of a positive outcome
                            // (all debtor positions have been successfully processed)
                            // STATUS="INSERTION_DOVUTO"
                            // STATUS_GPD="S" (synchronized)

                            if (gpdEnabled) {
                                dovutoService.updateStatusAndGpdStatusByIupdList(
                                        requestIDs,
                                        STATO_DOVUTO_DA_PAGARE,
                                        STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA);
                            } else if (gpdPreload) {
                                dovutoService.updateGpdStatusByIupdList(requestIDs,
                                        STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA);
                                dovutoPreloadService.updateGpdStatusByIupds(ente.getMygovEnteId(),
                                        requestIDs, STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA, false);
                            }

                            writer.write(String.format(
                                            "POSIZIONI DEBITORIE DA CREARE ELABORATE CON SUCCESSO (CODICE %d): %d",
                                            responseEntry.getStatusCode(),
                                            requestIDs.size()
                                    )
                            );

                            gpdErrDovutoService.delete(ente.getMygovEnteId(), requestIDs);
                            break;

                        case 200:
                            // // UPDATE OF DEBTOR POSITIONS IN PAGOPA
                            // Updating the status of debtor positions
                            // and the synchronization status with PagoPA
                            // in case of a positive outcome
                            // (all debtor positions have been successfully processed)
                            // STATUS="INSERTION_DOVUTO"
                            // STATUS_GPD="S" (synchronized)

                            if (gpdEnabled) {

                                if (azione.equals("M")) {

                                    dovutoService.updateStatusAndGpdStatusByIupdList(
                                            requestIDs,
                                            STATO_DOVUTO_DA_PAGARE,
                                            STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA);

                                } else if (azione.equals("A")) {
                                    dovutoService.deleteByIupd(ente.getMygovEnteId(), requestIDs);
                                }
                                gpdErrDovutoService.delete(ente.getMygovEnteId(), requestIDs);
                            } else if (gpdPreload) {

                                ///////////////// PRELOAD //////////////////////////////////////////

                                dovutoService.updateGpdStatusByIupdList(
                                        requestIDs, STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA);

                                if (azione.equals("M")) {
                                    dovutoPreloadService.updateGpdStatusByIupds(ente.getMygovEnteId(),
                                            requestIDs, STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA, false);
                                } else if (azione.equals("A")) {
                                    // Nel caso di azione di cancellazione,
                                    // lo stato del dovuto è D sulla preload
                                    dovutoPreloadService.deleteGpdStatusByIupds(ente.getMygovEnteId(), requestIDs, true);
                                }

                                gpdErrDovutoService.delete(ente.getMygovEnteId(), requestIDs);
                                /////////////////////////// FINE PRELOAD ///////////////////////
                            }

                            writer.write(String.format(
                                            "POSIZIONI DEBITORIE AGGIORNATE ELABORATE CON SUCCESSO (CODICE %d): %d",
                                            responseEntry.getStatusCode(),
                                            requestIDs.size()
                                    )
                            );

                            break;

                        case 404:
                            // Debtor positions not found in PagoPA
                            // These are the ones for which deletion from PagoPA was requested
                            // In MyPay, they were deleted as soon as they were sent to PagoPA, so there is no need to proceed with
                            // deletion in MyPay

                            gpdErrDovutoService.delete(ente.getMygovEnteId(), requestIDs);

                            log.warn("Number of debtor positions not found in PagoPA: {}, error description: {}",
                                    requestIDs.size(),
                                    responseEntry.getStatusCode() + " " + responseEntry.getStatusMessage());

                            requestIDs.forEach(iupd -> {
                                gpdErrDovutoService.insert(GpdErrDovuto.builder()
                                        .gpdIupd(iupd)
                                        .codErroreHttp(responseEntry.getStatusCode() + "")
                                        .descrizione(responseEntry.getStatusMessage())
                                        .azione(azione)
                                        .dtUltimaModifica(new Date())
                                        .mygovEnteId(ente.getMygovEnteId())
                                        .build());

                            });

                            writer.write(String.format(
                                            "POSIZIONI DEBITORIE ELABORATE MA NON TROVATE in PAGOPA (%s): %d",
                                            responseEntry.getStatusCode() + " - " + responseEntry.getStatusMessage(),
                                            requestIDs.size()
                                    )
                            );

                            if (gpdEnabled && azione.equals("M")) {
                                dovutoService.updateStatusAndGpdStatusByIupdList(
                                        requestIDs,
                                        STATO_DOVUTO_ERRORE,
                                        STATO_POS_DEBT_SINCRONIZZAZIONE_ERRORE_CON_PAGOPA
                                );
                            }

                            break;

                        default:
                            gpdErrDovutoService.delete(ente.getMygovEnteId(), requestIDs);

                            if (gpdEnabled) {
                                dovutoService.updateStatusAndGpdStatusByIupdList(
                                        requestIDs,
                                        STATO_DOVUTO_ERRORE,
                                        STATO_POS_DEBT_SINCRONIZZAZIONE_ERRORE_CON_PAGOPA
                                );
                            } else if (gpdPreload) {
                                dovutoService.updateGpdStatusByIupdList(
                                        requestIDs,
                                        STATO_POS_DEBT_SINCRONIZZAZIONE_ERRORE_CON_PAGOPA
                                );
                            }


                            log.error("Number of debtor positions processed with errors: {}, error description: {}",
                                    requestIDs.size(),
                                    responseEntry.getStatusCode() + " " + responseEntry.getStatusMessage());

                            writer.write(String.format(
                                            "POSIZIONI DEBITORIE ELABORATE CON ERRORI (%s): %d",
                                            responseEntry.getStatusCode() + " - " + responseEntry.getStatusMessage(),
                                            requestIDs.size()
                                    )
                            );

                            requestIDs.forEach(iupd -> {
                                //log.error("Debtor position with error: {}", iupd);
                                gpdErrDovutoService.insert(GpdErrDovuto.builder()
                                        .gpdIupd(iupd)
                                        .codErroreHttp(responseEntry.getStatusCode() + "")
                                        .descrizione(responseEntry.getStatusMessage())
                                        .azione(azione)
                                        .mygovEnteId(ente.getMygovEnteId())
                                        .dtUltimaModifica(new Date())
                                        .build());

                            });

                            if (gpdPreload) {
                                dovutoPreloadService.updateGpdStatusByIupds(ente.getMygovEnteId(),
                                        requestIDs, STATO_POS_DEBT_SINCRONIZZAZIONE_ERRORE_CON_PAGOPA, false);
                            }

                            break;
                    }
                    // log in the outcome file of all the details of the processed debtor positions
                    writeDovutiList(requestIDs, writer);

                } catch (IOException e) {
                    throw new MyPayException("Unable to write to the discard file", e);
                }
            });

            // Processing completed
            int numFile = gpdSyncService.updateStatus(gpdSyncInviato.getMygovGpdSyncId(),
                    STATO_FILE_INVIO_MASSIVO_POSIZ_DEBT.ELABORATO.getValue(),
                    uploadReport.getProcessedItem());
            if (numFile > 0) {
                log.info("The file {} has been processed and the related status updated in the DB", gpdSyncInviato.getFileId());
            }
        } catch (Exception e) {
            throw new MyPayException("Error during the processing of the report", e);
        }

        // finally, create the zip file of the outcomes by removing the text file
        zipAndRemoveOutcomeFile(outcomeFilename);

    }

    private String recuperaAzioneByFilename(String filename) {
        String azione = null;

        if (filename.matches(".*_INS_\\d+\\.zip$")) {
            azione = "I";
        }
        if (filename.matches(".*_UPD_\\d+\\.zip$")) {
            azione = "M";
        }
        if (filename.matches(".*_DEL_\\d+\\.zip$")) {
            azione = "A";
        }
        return azione;
    }

    /**
     * Writes the list of debtor positions to the outcome file.
     *
     * @param dovutiIupdList list of IUPD of the debtor positions
     * @param writer         writer of the outcome file
     * @throws IOException if an error occurs while writing to the file
     */
    private void writeDovutiList(List<String> dovutiIupdList, BufferedWriter writer) throws IOException {
        writer.write("\n");
        for (String iupd : dovutiIupdList) {
            writer.write("iupd: " + iupd + "\n");
        }
    }

    /**
     * Creates a zip file with the outcome file and removes the original outcome file.
     *
     * @param outcomeFilename name of the outcome file
     */
    private void zipAndRemoveOutcomeFile(String outcomeFilename) {
        String zipFilename = outcomeFilename.replaceFirst("\\." + OUTCOME_SUFFIX_IN + "$", "." + OUTCOME_SUFFIX_OUT);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilename))) {
            Path outcomePath = Paths.get(outcomeFilename);
            zos.putNextEntry(new ZipEntry(outcomePath.getFileName().toString()));
            Files.copy(outcomePath, zos);
            Files.delete(outcomePath);
        } catch (IOException e) {
            throw new MyPayException("Error during the zipping or deletion of the outcome file", e);
        }
    }


}
