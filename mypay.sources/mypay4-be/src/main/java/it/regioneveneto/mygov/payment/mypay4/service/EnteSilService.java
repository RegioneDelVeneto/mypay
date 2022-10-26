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

import it.regioneveneto.mygov.payment.mypay4.dao.EnteSilDao;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.EnteSil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class EnteSilService {

  @Autowired
  private EnteSilDao enteSilDao;
  @Autowired
  private MessageSource messageSource;

  @Transactional(propagation = Propagation.REQUIRED)
  public EnteSil upsert(EnteSil enteSil) {
    if (enteSil.getMygovEnteSilId() != null && enteSilDao.getById(enteSil.getMygovEnteSilId()) != null) {
      enteSilDao.update(enteSil);
    } else {
      long mygovEnteSilId = enteSilDao.insert(enteSil);
      enteSil.setMygovEnteSilId(mygovEnteSilId);
    }
    return getById(enteSil.getMygovEnteSilId());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int delete(Long mygovEnteSilId) {
    return enteSilDao.delete(mygovEnteSilId);
  }

  public EnteSil getById(Long mygovEnteSilId) {
    return enteSilDao.getById(mygovEnteSilId);
  }

  public void validate(EnteSil enteSil, boolean flgNotificaEsitoPush) {
    if (flgNotificaEsitoPush && (StringUtils.isBlank(enteSil.getNomeApplicativo()) ||
        StringUtils.isBlank(enteSil.getDeUrlInoltroEsitoPagamentoPush())))
      throw new ValidatorException(messageSource.getMessage("pa.manager.error.esito.push.campi.obbligatori", null, Locale.ITALY));
    if (enteSil.isFlgJwtAttivo() && (StringUtils.isBlank(enteSil.getCodServiceAccountJwtUscitaClientId()) ||
        StringUtils.isBlank(enteSil.getDeServiceAccountJwtUscitaClientMail()) || StringUtils.isBlank(enteSil.getCodServiceAccountJwtUscitaSecretKeyId()) ||
        StringUtils.isBlank(enteSil.getCodServiceAccountJwtUscitaSecretKey())))
      throw new ValidatorException(messageSource.getMessage("pa.manager.error.esito.push.campi.obbligatori", null, Locale.ITALY));
  }
}
