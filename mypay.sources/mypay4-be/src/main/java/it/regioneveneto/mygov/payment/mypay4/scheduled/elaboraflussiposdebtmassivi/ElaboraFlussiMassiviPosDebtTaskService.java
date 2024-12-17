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
package it.regioneveneto.mygov.payment.mypay4.scheduled.elaboraflussiposdebtmassivi;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.scheduled.elaboraflussiposdebtmassivi.internal.AllineaGpdStatusDaPagoPaService;
import it.regioneveneto.mygov.payment.mypay4.scheduled.elaboraflussiposdebtmassivi.internal.RecuperaPregressoEdInviaAPagoPaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Slf4j
@ConditionalOnProperty(name = AbstractApplication.NAME_KEY, havingValue = ElaboraFlussiMassiviPosDebtTaskApplication.NAME)
public class ElaboraFlussiMassiviPosDebtTaskService {

    @Autowired
    private AllineaGpdStatusDaPagoPaService allineaGpdStatusDaPagoPaService;

    @Autowired
    RecuperaPregressoEdInviaAPagoPaService recuperaPregressoEdInviaAPagoPaService;

    public void allineaGpdStatusDaPagoPa() {

        StopWatch swElaboraFlussiMassivi = new StopWatch("Batch elaboraFlussiMassiviPosDebtService::allineaGpdStatusDaPagoPa");
        swElaboraFlussiMassivi.start();

        // seleziona i flussi massivi da elaborare
        // per ognuno di essi ottiene l'esito dell'elaborazione richiamando il servizio di status del flusso
        // se lo status indica che tutti le posizioni debitorie sono state elaborate (con successo o con errore) allora
        // chiedi il report ed elabora il report
        // altrimenti non fare nulla

        allineaGpdStatusDaPagoPaService.process();

        swElaboraFlussiMassivi.stop();
        log.debug("Batch elaboraFlussiMassiviPosDebtService::allineaGpdStatusDaPagoPa:: End :: {} secondi", swElaboraFlussiMassivi.getTotalTimeSeconds());
    }


    public void recuperaPregressoEdInviaAPagoPA() {
        StopWatch swElaboraFlussiMassivi = new StopWatch("Batch elaboraFlussiMassiviPosDebtService::recuperaPregressoEdInviaAPagoPA");
        swElaboraFlussiMassivi.start();

        recuperaPregressoEdInviaAPagoPaService.process();

        swElaboraFlussiMassivi.stop();
        log.debug("Batch elaboraFlussiMassiviPosDebtService::recuperaPregressoEdInviaAPagoPA:: End :: {} secondi", swElaboraFlussiMassivi.getTotalTimeSeconds());

    }
}
