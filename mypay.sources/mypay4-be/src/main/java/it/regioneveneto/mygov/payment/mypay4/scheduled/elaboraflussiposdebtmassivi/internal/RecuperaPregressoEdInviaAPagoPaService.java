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

import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.Flusso;
import it.regioneveneto.mygov.payment.mypay4.scheduled.importflusso.pagopa.MassivePosDebtPagoPaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
@Transactional(propagation = Propagation.NEVER)
public class RecuperaPregressoEdInviaAPagoPaService {

    @Autowired
    private MassivePosDebtPagoPaService massivePosDebtPagoPaService;
    @Value("${pa.gpd.preload}")
    private boolean gpdPreload;
    @Value("${pa.gpd.enabled}")
    private boolean gpdEnabled;

    public void process() {
        if (gpdEnabled) {
            inviaFlussiDaRecuperare();
        }

        if (gpdPreload) {
            eseguiCaricamentoIniziale();
        }
    }

    private void inviaFlussiDaRecuperare() {
        log.info("*** Inizio del recupero dei flussi da inviare a PAGOPA ***");

        List<Flusso> flussiDaRecuperare = massivePosDebtPagoPaService.getFlussiDaRecuperare();
        log.info("Flussi con dovuti da recuperare: {}", flussiDaRecuperare.size());

        List<Ente> entiDaRecuperare = flussiDaRecuperare.stream()
                .map(Flusso::getMygovEnteId)
                .collect(Collectors.toList());
        log.info("Enti dei flussi con dovuti da recuperare: {}", entiDaRecuperare.size());

        entiDaRecuperare.forEach(ente -> {
            log.info("Recupero flussi per l'ente: {}", ente.getCodIpaEnte());

            try {
                flussiDaRecuperare.stream()
                        .filter(flusso -> flusso.getMygovEnteId().equals(ente))
                        .forEach(flusso -> {
                            massivePosDebtPagoPaService.createMissingIupdByFlusso(flusso);
                            massivePosDebtPagoPaService.sendPosDebtsToCreateToPagoPAByFlusso(flusso);
                            massivePosDebtPagoPaService.sendPosDebtsToUpdateToPagoPAByFlusso(flusso);
                            massivePosDebtPagoPaService.sendPosDebtsToDeleteToPagoPAByFlusso(flusso);
                        });
            } catch (Exception e) {
                log.error("Errore nell'elaborazione del recupero dei flussi per l'ente: {}", ente.getCodIpaEnte());
            }
        });

        log.info("*** Fine del recupero dei flussi ***");
    }

    private void eseguiCaricamentoIniziale() {
        log.info("*** Inizio del recupero del pregresso dei dovuti da inviare a PAGOPA ***");
        List<Ente> entiDaRecuperare = massivePosDebtPagoPaService.getEntiConDovutiDaSyncConPagoPA();

        log.info("Enti con dovuti da recuperare: {}", entiDaRecuperare.size());

        entiDaRecuperare.forEach(ente -> {
            try {
                massivePosDebtPagoPaService.createMissingIupdByEnte(ente);
                massivePosDebtPagoPaService.sendPosDebtsToCreateToPagoPAByEnte(ente);
                massivePosDebtPagoPaService.sendPosDebtsToUpdateToPagoPAByEnte(ente);
                massivePosDebtPagoPaService.sendPosDebtsToDeleteToPagoPAByEnte(ente);
            } catch (Exception e) {
                log.error("Errore nell'elaborazione del recupero per l'ente: {}", ente.getCodIpaEnte());
                log.error(e.getMessage(), e);
            }
        });

        log.info("*** Fine del recupero del pregresso ***");
    }


}
