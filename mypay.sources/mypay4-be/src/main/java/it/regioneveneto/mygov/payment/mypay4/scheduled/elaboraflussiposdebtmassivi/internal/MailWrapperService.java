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

import it.regioneveneto.mygov.payment.mypay4.dto.ResultImportFlussoTo;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.Flusso;
import it.regioneveneto.mygov.payment.mypay4.model.ImportDovuti;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.FlussoService;
import it.regioneveneto.mygov.payment.mypay4.service.ImportDovutiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
public class MailWrapperService {
    @Autowired
    private ImportDovutiService importDovutiService;

    @Autowired
    private FlussoService flussoService;

    @Autowired
    private EnteService enteService;

    /**
     * Invia mail di notifica di successo per l'importazione del flusso
     *
     * @param flussoId
     */
    public void inviaMailImportFlussoOk(Long flussoId) {
        sendMail(flussoId, (ente, resultElaboraTo, email) -> importDovutiService.sendMailImportFlussoOk(ente, resultElaboraTo, email));
    }

    /**
     * Invia mail di notifica di successo ma con scarti per l'importazione del flusso
     *
     * @param flussoId
     */
    public void inviaMailImportFlussoKO(Long flussoId) {
        sendMail(flussoId, (ente, resultElaboraTo, email) -> importDovutiService.sendMailImportFlussoScarti(ente, resultElaboraTo, email));
    }


    /**
     * Invia mail di notifica di errore per l'importazione del flusso
     *
     * @param flussoId
     * @param codErrore
     * @param deErrore
     */
    public void inviaMailImportFlussoError(Long flussoId, String codErrore, String deErrore) {
        Flusso flusso = flussoService.getById(flussoId);
        Ente ente = flusso.getMygovEnteId();
        ImportDovuti importDovuto = importDovutiService.getFlussoImport(flusso.getCodRequestToken());
        String email = getEmail(importDovuto);

        if (email != null) {
            try {
                importDovutiService.sendMailError(ente, flusso.getDeNomeFile(), codErrore, deErrore, email);
            } catch (Exception e) {
                log.warn("Error sending mail for iuf :: {} :: ERROR", flusso.getIuf(), e);
            }
        }
    }

    private void sendMail(Long flussoId, MailSender mailSender) {
        Flusso flusso = flussoService.getById(flussoId);
        Ente ente = flusso.getMygovEnteId();
        ImportDovuti importDovuto = importDovutiService.getFlussoImport(flusso.getCodRequestToken());
        String email = getEmail(importDovuto);

        if (email != null) {
            try {
                ResultImportFlussoTo resultElaboraTo = new ResultImportFlussoTo();
                resultElaboraTo.setNomeFile(flusso.getDeNomeFile());
                mailSender.send(ente, resultElaboraTo, email);
            } catch (Exception e) {
                log.warn("Error sending mail for iuf :: {} :: ERROR", flusso.getIuf(), e);
            }
        }
    }

    private String getEmail(ImportDovuti importDovuto) {
        if (importDovuto != null) {
            Utente utente = importDovuto.getMygovUtenteId();
            if (StringUtils.isNotBlank(utente.getDeFirstname()) && StringUtils.isNotBlank(utente.getDeLastname())) {
                return utente.getDeEmailAddress();
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface MailSender {
        void send(Ente ente, ResultImportFlussoTo resultElaboraTo, String email);
    }
}
