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
package it.regioneveneto.mygov.payment.mypay4.scheduled.importflusso;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Flusso;
import it.regioneveneto.mygov.payment.mypay4.model.ImportDovuti;
import it.regioneveneto.mygov.payment.mypay4.scheduled.importflusso.pagopa.MassivePosDebtPagoPaService;
import it.regioneveneto.mygov.payment.mypay4.service.AnagraficaStatoService;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.FlussoService;
import it.regioneveneto.mygov.payment.mypay4.service.ImportDovutiService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Questa classe è responsabile dell'importazione e dell'elaborazione dei flussi di pagamento.
 * È attiva solo quando la proprietà dell'applicazione 'AbstractApplication.NAME_KEY' è impostata
 * su 'ImportFlussoTaskApplication.NAME'.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = AbstractApplication.NAME_KEY, havingValue = ImportFlussoTaskApplication.NAME)
public class ImportFlussoTask {

    @Value("${pa.gpd.enabled}")
    private boolean gpdEnabled;
    @Autowired
    private ImportFlussoTaskService importFlussoTaskService;
    @Autowired(required = false)
    private MassivePosDebtPagoPaService massivePosDebtPagoPaService;
    @Autowired
    private FlussoService flussoService;
    @Autowired
    AnagraficaStatoService anagraficaStatoService;
    @Autowired
    private DovutoService dovutoService;
    @Autowired
    private ImportDovutiService importDovutiService;

    /**
     * Questo metodo è programmato per essere eseguito a intervalli fissi specificati dalla proprietà
     * dell'applicazione 'task.importFlusso.fixedDelay'.
     * IN sintesi, importa i flussi di pagamento, esporta le posizioni debitorie dei flussi appena importati e li invia
     * a PagoPA. Inoltre, recupera e invia anche le posizioni debitorie dei flussi importati in precedenza che non sono
     * stati inviati a PagoPA per qualche motivo (ad esempio, errore di rete).
     */
    @Scheduled(fixedDelayString = "${task.importFlusso.fixedDelay}", initialDelayString = "${random.long[4000,7000]}")
    public void importFlussoScheduler() {

        log.info("*** Inizio importFlussoScheduler ***");

        // Importa i flussi di pagamento e ottiene i loro ID
        List<Long> idsFlussi = importFlussoTaskService.importaFlusso();

        /*
          Se 'pa.gpd.enabled' è impostata su 'true', allora
          il GPD (Gestione Posizioni Debitorie) è abilitato.
          In tal caso, le posizioni debitorie dei flussi appena importati (ma non solo quelle dal momento
          che vengono prese tutte quelle che hanno GPD_STATUS = 'P', 'U', 'D') vengono esportate e inviate a PagoPA.
         */

        if (gpdEnabled) {

            // Esporta le posizioni debitorie dei flussi appena importati e li invia a PagoPA.
            // Qualora le posizioni debitorie non abbiano lo iupd, lo crea.
            idsFlussi.stream()
                    .filter(idFlusso -> idFlusso > 0)
                    .forEach(idFlusso -> {
                                Integer countDovuti = dovutoService.getCountDovutiToSyncByFlusso(idFlusso);
                                Flusso flusso = flussoService.getById(idFlusso);
                                if (countDovuti > 0) {
                                    massivePosDebtPagoPaService.createMissingIupdByFlusso(flusso);
                                    massivePosDebtPagoPaService.sendPosDebtsToCreateToPagoPAByFlusso(flusso);
                                    massivePosDebtPagoPaService.sendPosDebtsToUpdateToPagoPAByFlusso(flusso);
                                    massivePosDebtPagoPaService.sendPosDebtsToDeleteToPagoPAByFlusso(flusso);
                                } else {
                                    flussoService.updateStatusAsCompleted(flusso);
                                }
                            }
                    );

        }
        log.info("*** Fine importFlussoScheduler ***");

    }

}
