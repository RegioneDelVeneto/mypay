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

import it.regioneveneto.mygov.payment.mypay4.dao.PushEsitoSilDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.PushEsitoSil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;


@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PushEsitoSilService {

  @Autowired
  DovutoElaboratoService dovutoElaboratoService;
  @Autowired
  PushEsitoSilDao pushEsitoSilDao;

  @Transactional(propagation = Propagation.REQUIRED)
  public void insertNewPushEsitoSil(Long idDovutoElaborato) {
    DovutoElaborato elaborato = dovutoElaboratoService.getById(idDovutoElaborato);
    PushEsitoSil pushEsitoSil = PushEsitoSil.builder()
      .dtCreazione(new Date())
      .flgEsitoInvioPush(false)
      .mygovDovutoElaboratoId(elaborato)
      .numTentativiEffettuati(0)
      .build();
    Long newId = pushEsitoSilDao.insertNewPushEsitoSil(pushEsitoSil);
    log.info("insert PushEsitoSil, new Id: "+newId);
  }

  public List<PushEsitoSil> getEsitiToSend() {
    List<PushEsitoSil> list = pushEsitoSilDao.getEsitiToSend();
    return Collections.unmodifiableList(list);
  }

  public PushEsitoSil getById(Long idPushEsitoSil) {
    return pushEsitoSilDao.getById(idPushEsitoSil);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updatePushEsitoSil(PushEsitoSil pushEsitoSil) {
    int updatedRec = pushEsitoSilDao.updatePushEsitoSil(pushEsitoSil);
    if (updatedRec != 1) {
      throw new MyPayException("PushEsitoSil update internal error");
    }
    log.info("PushEsitoSil whit: "+pushEsitoSil.getMygovPushEsitoSilId()+" is up to date");
  }

}
