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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.DebtPosition;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.massivo.DeleteMultipleDebtPositions;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.massivo.MultipleDebtPositions;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.massivo.ZipFileInfo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoMultibeneficiario;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.GpdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.*;

/**
 * Questo servizio gestisce la creazione di file zip per l'esportazione di posizioni debitorie.
 */
@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
@Transactional(propagation = Propagation.REQUIRED)
class ZipFileService {

    @Autowired
    GpdService gpdService;

    @Autowired
    private DovutoService dovutoService;

    @Value("${task.importFlusso.exportFLussoGpd.max_pd_per_file:250}")
    private int MAX_PD_PER_FILE;

    /**
     * Genera il nome del file zip
     *
     * @param ente      ente da esportare
     * @param index     indice del file zip
     * @param gpdStatus gpdStatus del dovuto
     * @return nome del file zip
     */
    private String generateFilename(Ente ente, int index, Character gpdStatus) {

        String action;
        switch (gpdStatus) {
            case STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA:
                action = "INS";
                break;
            case STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA:
                action = "UPD";
                break;
            case STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA:
                action = "DEL";
                break;
            default:
                throw new MyPayException("Invalid GPD status");
        }

        Date now = new Date();
        String dataOrario = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(now);

        return String.format(
                "%s_%s_%04d.%s",
                dataOrario,
                action,
                index,
                "zip"
        );
    }

    /**
     * Crea il file zip con la lista di posizioni debitorie
     *
     * @param mapper                oggetto ObjectMapper per la serializzazione in JSON
     * @param multipleDebtPositions lista di posizioni debitorie
     * @param percorsoPacchetto     percorso del file zip da creare
     */
    private void createZipFile(ObjectMapper mapper, Object multipleDebtPositions, Path percorsoPacchetto) throws IOException {
        String json = mapper.writeValueAsString(multipleDebtPositions);
        String filename = percorsoPacchetto.getFileName().toString();
        String jsonFilename = filename.substring(0, filename.lastIndexOf('.')).concat(".json");

        // remove non utf-8 characters from json
        // json = json.replaceAll("[^\\x00-\\x7F]", "");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(percorsoPacchetto.toFile()))) {
            zos.putNextEntry(new ZipEntry(jsonFilename));
            zos.write(json.getBytes());
        } catch (IOException e) {
            throw new MyPayException("Error creating zip file", e);
        }
    }

    /**
     * Crea la lista di posizioni debitorie a partire dalla lista di dovuti
     *
     * @param sottoLista lista di dovuti
     * @return lista di posizioni debitorie
     */
    private MultipleDebtPositions createMultDebtPosUsingSublist(List<Dovuto> sottoLista) throws IOException {

        List<DebtPosition> pdList = sottoLista.stream()
                .filter(dovuto -> dovuto.getGpdIupd() != null && !dovuto.getGpdIupd().isEmpty())
                .map(dovuto -> {
                    Optional<DovutoMultibeneficiario> multibeneficiario =
                            dovutoService.getOptionalDovMultibenefByIdDovuto(dovuto.getMygovDovutoId());
                    DebtPosition debtPosition = gpdService.mapDebtPosition(List.of(dovuto), multibeneficiario);
                    return debtPosition;
                })
                .collect(Collectors.toList());

        return MultipleDebtPositions.builder()
                .paymentPositions(pdList)
                .build();

    }


    private DeleteMultipleDebtPositions createDeleteMultDebtPosUsingSublist(List<Dovuto> sottoLista) throws IOException {

        List<String> iupdList = sottoLista.stream()
                .filter(dovuto -> dovuto.getGpdIupd() != null && !dovuto.getGpdIupd().isEmpty())
                .map(Dovuto::getGpdIupd)
                .collect(Collectors.toList());

        return DeleteMultipleDebtPositions.builder().paymentPositionIUPDs(iupdList).build();

    }


    private DeleteMultipleDebtPositions createDeleteMultDebtPosUsingSubStringlist(List<String> iupdLIst) throws IOException {
        return DeleteMultipleDebtPositions.builder().paymentPositionIUPDs(iupdLIst).build();
    }

    /**
     * Crea i file zip con le sottoliste di massimo MAX_PD_PER_FILE elementi
     *
     * @param ente               flusso da esportare
     * @param dovutoList         lista dei dovuti da esportare
     * @param enteFlussoPathname percorso della cartella in cui creare i file zip
     * @return mappa dei file zip creati
     */
    public Map<String, ZipFileInfo> exportOnZipFiles(Ente ente, Long flussoId, List<Dovuto> dovutoList, Path enteFlussoPathname, Character gpdStatus) {

        // Creazione delle sottoliste di massimo MAX_PD_PER_FILE elementi

        List<List<Dovuto>> sottoListe = partitionList(dovutoList);

        // Creazione dell'oggetto ObjectMapper per la serializzazione in JSON

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Creazione dei file zip usando le sottoliste di massimo MAX_PD_PER_FILE elementi

        Map<String, ZipFileInfo> zipFileInfoMap = new TreeMap<>();
        int index = 1;
        for (List<Dovuto> sottoLista : sottoListe) {
            String zipFilename = generateFilename(ente, index++, gpdStatus);
            Path zipPathname = enteFlussoPathname.resolve(zipFilename);

            try {
                if (Files.notExists(zipPathname)) {
                    Object jsonObject;

                    if (gpdStatus != STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA) {
                        jsonObject = createMultDebtPosUsingSublist(sottoLista);

                    } else {
                        jsonObject = createDeleteMultDebtPosUsingSublist(sottoLista);
                    }
                    createZipFile(mapper, jsonObject, zipPathname);

                    List<Long> dovutiIdSubList = sottoLista.stream()
                            .map(Dovuto::getMygovDovutoId).collect(Collectors.toList());

                    ZipFileInfo zipFileInfo = ZipFileInfo.builder()
                            .dovutiList(dovutiIdSubList)
                            .zipPathName(zipPathname)
                            .fileId(null)
                            .flussiId(flussoId)
                            .enteId(ente.getMygovEnteId())
                            .build();

                    String zipFileName = zipPathname.getFileName().toString();
                    zipFileInfoMap.put(zipFileName, zipFileInfo);

                } else {
                    String msgErr = String.format("File %s already exists: can't continue", zipFilename);
                    throw new MyPayException(msgErr);
                }
            } catch (IOException e) {
                String msgErr = String.format("Error creating zip file %s for exporting pos. debts", zipFilename);
                throw new MyPayException(msgErr, e);
            }
        }

        return zipFileInfoMap;
    }


    public Map<String, ZipFileInfo> exportDeleteOnZipFiles(Ente ente, Long flussoId, List<String> iupdList, Path enteFlussoPathname, Character gpdStatus) {

        // Creazione dell'oggetto ObjectMapper per la serializzazione in JSON

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Creazione del file zip

        Map<String, ZipFileInfo> zipFileInfoMap = new TreeMap<>();
        int index = 1;
        String zipFilename = generateFilename(ente, index++, gpdStatus);
        Path zipPathname = enteFlussoPathname.resolve(zipFilename);

        try {
            if (Files.notExists(zipPathname)) {
                Object jsonObject = createDeleteMultDebtPosUsingSubStringlist(iupdList);
                createZipFile(mapper, jsonObject, zipPathname);

                ZipFileInfo zipFileInfo = ZipFileInfo.builder()
                        .iupdList(iupdList)
                        .zipPathName(zipPathname)
                        .fileId(null)
                        .flussiId(flussoId)
                        .enteId(ente.getMygovEnteId())
                        .build();

                String zipFileName = zipPathname.getFileName().toString();
                zipFileInfoMap.put(zipFileName, zipFileInfo);

            } else {
                String msgErr = String.format("File %s already exists: can't continue", zipFilename);
                throw new MyPayException(msgErr);
            }
        } catch (IOException e) {
            String msgErr = String.format("Error creating zip file %s for exporting pos. debts", zipFilename);
            throw new MyPayException(msgErr, e);
        }

        return zipFileInfoMap;
    }


    /**
     * Crea una lista di sottoliste a partire dalla lista di dovuti
     *
     * @param dovutoList lista dei dovuti
     * @return lista di sottoliste
     */
    private List<List<Dovuto>> partitionList(List<Dovuto> dovutoList) {
        List<List<Dovuto>> sottoListe = new ArrayList<>();
        int bound = (dovutoList.size() + MAX_PD_PER_FILE - 1) / MAX_PD_PER_FILE;
        for (int i = 0; i < bound; i++) {
            List<Dovuto> sottolistaDovuti = dovutoList.subList(
                    i * MAX_PD_PER_FILE, Math.min(MAX_PD_PER_FILE * (i + 1), dovutoList.size()));
            sottoListe.add(sottolistaDovuti);
        }
        return sottoListe;
    }
}
