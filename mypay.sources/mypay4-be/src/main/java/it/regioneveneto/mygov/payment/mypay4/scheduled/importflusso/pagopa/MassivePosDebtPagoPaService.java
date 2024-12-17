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
package it.regioneveneto.mygov.payment.mypay4.scheduled.importflusso.pagopa;

import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.massivo.ZipFileInfo;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.Flusso;
import it.regioneveneto.mygov.payment.mypay4.scheduled.importflusso.pagopa.util.FileUtils;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.GpdService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.*;

/**
 * Servizio per l'esportazione delle posizioni debitorie e l'invio a PagoPA
 */
@Component
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
public class MassivePosDebtPagoPaService {

    private static final int RESULSET_COUNT_LIMIT = 10000;
    private static final int NUM_MAX_TENTATIVI_DI_INVIO_SLOT = 1000;
    @Autowired
    AnagraficaStatoService anagraficaStatoService;
    @Autowired
    private DovutoService dovutoService;
    @Autowired
    private FlussoService flussoService;
    @Autowired
    private EnteService enteService;
    @Autowired
    private PagoPaUploadService pagoPaUploadService;
    @Autowired
    private ZipFileService zipFileService;
    @Autowired
    private GpdService gpdService;

    @Value("${task.importFlusso.exportFLussoGpd.directory_root_elaborazione}")
    private String rootWorkingFolder;

    @Value("${task.elaboraFlussiMassiviPosDebt.recupero_pregresso.flussi_creati.data_creazione.prima_di_almeno_in_minuti}")
    private int olderThanInMinutes;
    @Autowired
    private DovutoPreloadService dovutoPreloadService;


    /**
     * Invia le posizioni debitorie da creare a PagoPA
     *
     * @param flusso flusso
     */

    public void sendPosDebtsToCreateToPagoPAByFlusso(Flusso flusso) {
        sendByStatusToPagoPaByFlusso(flusso, STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA);
    }

    /**
     * Invia le posizioni debitorie da creare a PagoPA
     *
     * @param ente ente
     */
    public void sendPosDebtsToCreateToPagoPAByEnte(Ente ente) {
        sendByStatusToPagoPaByEnte(ente, STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA);
    }


    /**
     * Invia le posizioni debitorie da aggiornare a PagoPA
     *
     * @param ente ente
     */
    public void sendPosDebtsToUpdateToPagoPAByEnte(Ente ente) {
        sendByStatusToPagoPaByEnte(ente, STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA);
    }


    /**
     * Invia le posizioni debitorie da cancellare a PagoPA
     *
     * @param ente ente
     */
    public void sendPosDebtsToDeleteToPagoPAByEnte(Ente ente) {
        sendByStatusToPagoPaByEnte(ente, STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA);
    }


    /**
     * Invia le posizioni debitorie a PagoPA in base allo stato
     *
     * @param ente      id dell'ente
     * @param gpdStatus stato delle posizioni debitorie
     */
    private void sendByStatusToPagoPaByEnte(Ente ente, char gpdStatus) {
        log.info("--- Inizio esportazione dovuti per enteId={}", ente.getMygovEnteId());
        int numTentativiSlot = 0;


        if (gpdStatus != STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA) {
            // Se si tratta di posizioni debitorie da creare

            // Recupera la lista di 'Dovuto' da inviare per il flusso specificato
            List<Dovuto> toSendDovutoList = dovutoService
                    .getByEnteIdAndGpdStatusWithIuvNotNull(ente.getMygovEnteId(), gpdStatus,
                            RESULSET_COUNT_LIMIT);


            while (!toSendDovutoList.isEmpty()) {
                numTentativiSlot++;

                // Crea la cartella di esportazione per l'invio
                Path exportFolder = FileUtils.createFluxExportFolder(
                        rootWorkingFolder,
                        ente.getCodiceFiscaleEnte()
                );

                // Esporta i 'Dovuto' in file zip e invia i file a PagoPA
                Map<String, ZipFileInfo> zipFileInfoMap = zipFileService.exportOnZipFiles(ente, null, toSendDovutoList,
                        exportFolder, gpdStatus);
                pagoPaUploadService.sendZipFilesToPagoPA(ente, zipFileInfoMap, gpdStatus);

                if (numTentativiSlot > NUM_MAX_TENTATIVI_DI_INVIO_SLOT) {
                    log.error("Numero massimo di tentativi di invio slot [{}] raggiunto. Interrompo l'elaborazione.",
                            NUM_MAX_TENTATIVI_DI_INVIO_SLOT);
                    break;
                }

                toSendDovutoList = dovutoService
                        .getByEnteIdAndGpdStatusWithIuvNotNull(ente.getMygovEnteId(), gpdStatus,
                                RESULSET_COUNT_LIMIT);
            }

        } else {
            List<String> toDeleteIupdListWithDups = dovutoPreloadService.getIupdListToDeleteByEnte(ente.getMygovEnteId(), gpdStatus, RESULSET_COUNT_LIMIT);
            List<String> toDeleteIupdList = new ArrayList<String>(new HashSet<String>(toDeleteIupdListWithDups));


            while (!toDeleteIupdList.isEmpty()) {
                // Crea la cartella di esportazione per l'invio
                Path exportFolder = FileUtils.createFluxExportFolder(
                        rootWorkingFolder,
                        ente.getCodiceFiscaleEnte()
                );

                // Esporta i 'Dovuto' in file zip e invia i file a PagoPA
                Map<String, ZipFileInfo> zipFileInfoMap = zipFileService.exportDeleteOnZipFiles(ente, null, toDeleteIupdList,
                        exportFolder, gpdStatus);

                pagoPaUploadService.sendZipFilesToPagoPA(ente, zipFileInfoMap, gpdStatus);

                // Recupera una nuova lista di 'IUPD' da eliminare
                toDeleteIupdListWithDups = dovutoPreloadService.getIupdListToDeleteByEnte(ente.getMygovEnteId(), gpdStatus, RESULSET_COUNT_LIMIT);
                toDeleteIupdList = new ArrayList<String>(new HashSet<String>(toDeleteIupdListWithDups));
            }

        }
        log.info("--- Fine esportazione dovuti per enteId={}", ente.getMygovEnteId());
    }


    /**
     * Invia le posizioni debitorie da aggiornare a PagoPA
     *
     * @param flusso flusso
     */
    public void sendPosDebtsToUpdateToPagoPAByFlusso(Flusso flusso) {
        sendByStatusToPagoPaByFlusso(flusso, STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA);
    }


    /**
     * Invia le posizioni debitorie da eliminare a PagoPA
     *
     * @param flusso flusso
     */
    public void sendPosDebtsToDeleteToPagoPAByFlusso(Flusso flusso) {
        sendByStatusToPagoPaByFlusso(flusso, STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA);
    }


    /**
     * Invia le posizioni debitorie a PagoPA in base allo stato
     *
     * @param flusso    id del flusso
     * @param gpdStatus stato delle posizioni debitorie
     */

    private void sendByStatusToPagoPaByFlusso(Flusso flusso, Character gpdStatus) {
        log.info("--- Inizio esportazione flusso pos. debiti con idFlusso={}", flusso);
        int numTentativiSlot = 0;

        // Recupera la lista di 'Dovuto' da inviare per il flusso specificato
        List<Dovuto> toSendDovutoList = dovutoService
                .getByFlussoIdAndGpdStatusWithIuvNotNull(flusso.getMygovFlussoId(), gpdStatus,
                        RESULSET_COUNT_LIMIT);

        while (!toSendDovutoList.isEmpty()) {
            numTentativiSlot++;
            // Crea la cartella di esportazione per il flusso
            Path exportFolder = FileUtils.createFluxExportFolder(rootWorkingFolder,
                    flusso.getMygovEnteId().getCodiceFiscaleEnte()
            );

            // Esporta i 'Dovuto' in file zip e invia i file a PagoPA
            Map<String, ZipFileInfo> zipFileInfoMap = zipFileService.exportOnZipFiles(flusso.getMygovEnteId(), flusso.getMygovFlussoId(), toSendDovutoList,
                    exportFolder, gpdStatus);
            pagoPaUploadService.sendZipFilesToPagoPA(flusso.getMygovEnteId(), zipFileInfoMap, gpdStatus);

            if (numTentativiSlot > NUM_MAX_TENTATIVI_DI_INVIO_SLOT) {
                log.error("Numero massimo di tentativi di invio slot [{}] raggiunto. Interrompo l'elaborazione.",
                        NUM_MAX_TENTATIVI_DI_INVIO_SLOT);
                break;
            }
            toSendDovutoList = dovutoService
                    .getByFlussoIdAndGpdStatusWithIuvNotNull(flusso.getMygovFlussoId(), gpdStatus,
                            RESULSET_COUNT_LIMIT);
        }

        log.info("--- Fine esportazione flusso con idFlusso={}", flusso);
    }

    /**
     * Per le posizioni debitorie da sincronizzare con PagPA
     * crea i relativi iupd qualora manchino
     *
     * @param flusso flusso per il quale creare gli iupd mancanti
     */

    public void createMissingIupdByFlusso(Flusso flusso) {
        Long idFlusso = flusso.getMygovFlussoId();
        log.info("--- Inizio generazione IUPD mancanti per le pos. debt. del flusso con idFlusso={}", idFlusso);


        // Recupera la lista dei 'Dovuti' da sincronizzare che non hanno lo iupd
        List<Dovuto> listaDvutiDaSincrSenzaIupd = dovutoService
                .getByFlussoIdAndGpdStatusWithIuvNotNullAndIupdNull(idFlusso, STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA,
                        RESULSET_COUNT_LIMIT);

        while (!listaDvutiDaSincrSenzaIupd.isEmpty()) {

            listaDvutiDaSincrSenzaIupd.forEach(dovuto -> {
                if (dovuto.getGpdIupd() == null || dovuto.getGpdIupd().isEmpty()) {
                    dovutoService.updateGpdIupd(
                            dovuto.getMygovDovutoId(),
                            gpdService.generateRandomIupd(dovuto.getNestedEnte().getCodiceFiscaleEnte())
                    );
                }
            });
            listaDvutiDaSincrSenzaIupd = dovutoService
                    .getByFlussoIdAndGpdStatusWithIuvNotNullAndIupdNull(idFlusso, STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA,
                            RESULSET_COUNT_LIMIT);

        }
        log.info("--- Fine generazione IUPD mancanti per le pos. debt. del flusso con idFlusso={}", idFlusso);
    }


    /**
     * Questo metodo genera IUPD (Identificativo Unico Posizione Debito) mancanti per l'Ente dato.
     * Recupera una lista di elementi 'Dovuto' che necessitano di essere sincronizzati e non hanno un IUPD.
     * Per ogni elemento 'Dovuto' nella lista, se l'IUPD è null o vuoto, genera un nuovo IUPD e aggiorna l'elemento 'Dovuto' con il nuovo IUPD.
     * Questo processo continua fino a quando non ci sono più elementi 'Dovuto' senza un IUPD.
     *
     * @param ente L'Ente per il quale generare IUPD mancanti.
     */
    public void createMissingIupdByEnte(Ente ente) {
        // Log dell'inizio della generazione di IUPD per l'Ente dato
        log.info("--- Inizio generazione IUPD mancanti per le pos. debt. del flusso con enteId={}", ente.getMygovEnteId());

        // Recupera la lista dei 'Dovuti' da sincronizzare che non hanno lo iupd
        List<Dovuto> listaDovutiDaSincrSenzaIupd = dovutoService
                .getByEnteIdAndGpdStatusWithIuvNotNullAndIupdNull(ente.getMygovEnteId(), STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA,
                        RESULSET_COUNT_LIMIT);

        // Continua il processo fino a quando non ci sono più 'Dovuti' senza un IUPD
        while (!listaDovutiDaSincrSenzaIupd.isEmpty()) {

            listaDovutiDaSincrSenzaIupd.forEach(dovuto -> {
                // Se l'IUPD è null o vuoto
                if (dovuto.getGpdIupd() == null || dovuto.getGpdIupd().isEmpty()) {

                    // Genera un nuovo IUPD
                    String generatedIupd = gpdService.generateRandomIupd(dovuto.getNestedEnte().getCodiceFiscaleEnte());
                    // Aggiorna l'elemento 'Dovuto' con il nuovo IUPD
                    dovutoService.updateGpdIupd(
                            dovuto.getMygovDovutoId(),
                            generatedIupd
                    );
                }
            });
            // Recupera una nuova lista di 'Dovuti' da sincronizzare che non hanno un IUPD
            listaDovutiDaSincrSenzaIupd = dovutoService
                    .getByEnteIdAndGpdStatusWithIuvNotNullAndIupdNull(ente.getMygovEnteId(), STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA,
                            RESULSET_COUNT_LIMIT);

        }
        log.info("--- Fine generazione IUPD mancanti per le pos. debt. del flusso con enteId={}", ente.getMygovEnteId());

    }

    /**
     * Recupera gli enti che contengono ancora dei 'Dovuti' (posizioni debitorie)
     * da esportare e inviare a PagoPA
     *
     * @return lista degli enti da recuperare
     */
    public List<Ente> getEntiConDovutiDaSyncConPagoPA() {
        return enteService.getEntiConDovutiDaSyncConPagoPA();
    }


    /**
     * Questo metodo recupera una lista di flussi che contengono 'Dovuti' (posizioni debitorie) non ancora esportati e inviati a PagoPA.
     * Prima, recupera lo stato 'In Caricamento' da 'AnagraficaStatoService' utilizzando il codice dello stato e il tipo dello stato.
     * Poi, utilizza 'FlussoService' per recuperare i flussi che sono più vecchi di un certo numero di minuti e che hanno lo stato 'In Caricamento'.
     *
     * @return Una lista di flussi che devono essere recuperati.
     */
    public List<Flusso> getFlussiDaRecuperare() {
        // Restituisce la lista di flussi che contengono 'Dovuti' non ancora esportati e inviati a PagoPA
        AnagraficaStato statoIncCaricamento = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_FLUSSO_IN_CARICAMENTO, Constants.STATO_TIPO_FLUSSO);
        return flussoService.getFlussiDaRecuperare(olderThanInMinutes, statoIncCaricamento.getMygovAnagraficaStatoId());
    }
}
