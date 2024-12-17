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
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.GpdMassiveClientService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Questo servizio gestisce l'invio di file zip a PagoPA.
 */
@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
class PagoPaUploadService {

    @Autowired
    DbHelperService dbHelperService;
    @Autowired
    private GpdMassiveClientService gpdMassiveClientService;

    /**
     * Invia i file zip a PagoPA.
     *
     * @param ente           Ente
     * @param zipFileInfoMap mappa dei file zip
     * @param gpdStatus      stato del file
     */
    public void sendZipFilesToPagoPA(Ente ente, Map<String, ZipFileInfo> zipFileInfoMap, Character gpdStatus) {
        zipFileInfoMap.forEach((zipFilename, zipfileInfo) -> {
            processFile(zipfileInfo, ente.getCodiceFiscaleEnte(), gpdStatus);
        });
    }

    /**
     * Invia un file zip a PagoPA.
     *
     * @param zipfileInfo       informazioni sul file zip
     * @param codiceFiscaleEnte codice fiscale dell'ente
     * @param gpdStatus         stato del file
     */
    private void processFile(ZipFileInfo zipfileInfo, String codiceFiscaleEnte, Character gpdStatus) {
        try {
            String fileId = gpdMassiveClientService.uploadZipFile(codiceFiscaleEnte, zipfileInfo.getZipPathName(), gpdStatus);
            if (!fileId.isEmpty()) {
                log.info("Il file {} e' stato caricato con successo su PagoPA. [fileId: {}]", zipfileInfo.getZipPathName().getFileName(), fileId);
                zipfileInfo.setCodStato(Constants.STATO_FILE_INVIO_MASSIVO_POSIZ_DEBT.INVIATO.getValue());
                zipfileInfo.setFileId(fileId);
                boolean deleteOnDbLocaleDovutiInviati = gpdStatus == Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA;
                dbHelperService.registerSingleSentFile(zipfileInfo,
                        Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_INVIATO_A_PAGOPA, deleteOnDbLocaleDovutiInviati);
            } else {
                log.info("Errore durante il caricamento del {} su PagoPA. [fileId restituito vuoto]", zipfileInfo.getZipPathName().getFileName());
                removeFile(zipfileInfo.getZipPathName());
            }

        } catch (Exception e) {
            // removeFile(zipfileInfo.getZipPathName());
            log.error("Errore generico su uploadZipFile", e);
            throw e;
        }
    }

    /**
     * Rimuove un file dal percorso specificato.
     *
     * @param filePath percorso del file da rimuovere
     */
    private void removeFile(Path filePath) {
        log.info("Rimozione del file {}", filePath);

        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                log.warn("Errore su removeFile", e);
            }
        }
    }
}
