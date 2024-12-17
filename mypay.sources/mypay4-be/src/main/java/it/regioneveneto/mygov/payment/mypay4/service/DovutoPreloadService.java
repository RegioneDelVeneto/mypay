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
package it.regioneveneto.mygov.payment.mypay4.service;

import it.regioneveneto.mygov.payment.mypay4.dao.DovutoPreloadDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class DovutoPreloadService {

    @Autowired
    private DovutoPreloadDao dovutoPreloadDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGpdStatusByIupds(Long enteId, List<String> iupds, char gpdStatus, boolean resetNuovoStatusGpd) {

        if (resetNuovoStatusGpd) {
            int updatedRec = dovutoPreloadDao.updateGpdStatusWithResetNuovoStatusGpdByIupds(enteId, iupds, gpdStatus);
            if (log.isDebugEnabled()) {
                log.debug("Aggiornati {} record con gpdStatus {}", updatedRec, gpdStatus);
            }
        } else {
            int updatedRec = dovutoPreloadDao.updateGpdStatusByIupds(enteId, iupds, gpdStatus);
            if (log.isDebugEnabled()) {
                log.debug("Aggiornati {} record con gpdStatus {}", updatedRec, gpdStatus);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteGpdStatusByIupds(Long enteId, List<String> iupds, boolean slashNuovoStatoGpd) {

        if (slashNuovoStatoGpd) {
            int updatedRec = dovutoPreloadDao.deleteGpdStatusByIupdsAck(enteId, iupds);
            if (log.isDebugEnabled()) {
                log.debug("Aggiornati {} record con gpdStatus", updatedRec);
            }
            return updatedRec;
        }else {
            int updatedRec = dovutoPreloadDao.deleteGpdStatusByIupds(enteId, iupds);
            if (log.isDebugEnabled()) {
                log.debug("Aggiornati {} record con gpdStatus", updatedRec);
            }
            return updatedRec;
        }
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGpdStatus(List<Long> dovutiIds, char gpdStatus) {
        int updatedRec = dovutoPreloadDao.updateGpdStatus(dovutiIds, gpdStatus);
        if (log.isDebugEnabled()) {
            log.debug("Aggiornati {} record con gpdStatus {}", updatedRec, gpdStatus);
        }
    }


    public List<String> getIupdListToDeleteByEnte(Long enteId, char gpdStatus, int maxResults) {
        return dovutoPreloadDao.getIupdListToDeleteByEnte(enteId, gpdStatus, maxResults);
    }

}
