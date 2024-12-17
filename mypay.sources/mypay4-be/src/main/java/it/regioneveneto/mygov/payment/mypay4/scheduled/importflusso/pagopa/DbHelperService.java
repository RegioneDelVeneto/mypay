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
import it.regioneveneto.mygov.payment.mypay4.model.GpdSync;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoPreloadService;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.GpdSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA;

/**
 * Questo servizio fornisce metodi per interagire con il database.
 */
@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
@Transactional(propagation = Propagation.REQUIRED)
public class DbHelperService {
    public static final int VERSION = 0;
    @Autowired
    private GpdSyncService gpdSyncService;
    @Autowired
    private DovutoService dovutoService;
    @Autowired
    private DovutoPreloadService dovutoPreloadService;

    @Value("${pa.gpd.preload}")
    private boolean gpdPreload;

    @Value("${pa.gpd.enabled}")
    private boolean gpdEnabled;


    /**
     * Registra un singolo file inviato nel database.
     *
     * @param zipfileInfo informazioni sul file zip inviato
     * @param gpdStatus   stato del flusso di pagamento
     */
    public void registerSingleSentFile(ZipFileInfo zipfileInfo, char gpdStatus, boolean deleteOnDbLocaleDovutiInviati) {

        String zipFilename = zipfileInfo.getZipPathName().getFileName().toString();
        log.info("Registrazione file inviato {} ", zipFilename);

        Date now = new Date();

        int recordInviati;
        if (gpdPreload && deleteOnDbLocaleDovutiInviati) {
            recordInviati = zipfileInfo.getIupdList().size();
        } else {
            recordInviati = zipfileInfo.getDovutiList().size();
        }
        Long idGpdSync = gpdSyncService.insert(GpdSync.builder()
                .mygovFlussoId(zipfileInfo.getFlussiId())
                .mygovEnteId(zipfileInfo.getEnteId())
                .filename(zipFilename)
                .fileId(zipfileInfo.getFileId())
                .numDovutiInviati(recordInviati)
                .numDovutiElaborati(0)
                .codStato(zipfileInfo.getCodStato())
                .dtCreazione(now)
                .dtUltimaModifica(now)
                .version(VERSION)
                .build()
        );

        log.info("Inserito idGpdSync {}", idGpdSync);

        if (gpdPreload) {

            // //////////////////////
            // PRELOAD
            // /////////////////////
            if (deleteOnDbLocaleDovutiInviati) {
                // Si mettono a "D" nel preload tuti i dovuti cancellati
                // Dobbiamo aggiornare a D per IUPD nella tabnella preload annullando il nuovo_status_gpd
                dovutoPreloadService.deleteGpdStatusByIupds(zipfileInfo.getEnteId(), zipfileInfo.getIupdList(), false);

            } else {
                // si mettono a "I"
                // sia sul Preload che sul Dovuto
                // Il nuovo_status_gpd si resetta

                dovutoService.updateGpdStatus(zipfileInfo.getDovutiList(), gpdStatus);
                dovutoPreloadService.updateGpdStatus(zipfileInfo.getDovutiList(), gpdStatus);
            }
        }

        if (gpdEnabled) {
            // //////////////////////
            // GDP ENABLED
            // /////////////////////

            dovutoService.updateGpdStatus(zipfileInfo.getDovutiList(), gpdStatus);

        }
    }
}
