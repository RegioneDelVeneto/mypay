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
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.EnteSil;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
  @CacheEvict(value=CacheService.CACHE_NAME_ENTE_SIL, allEntries = true)
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
  @CacheEvict(value=CacheService.CACHE_NAME_ENTE_SIL, allEntries = true)
  public int delete(Long mygovEnteSilId) {
    return enteSilDao.delete(mygovEnteSilId);
  }

  @Cacheable(value= CacheService.CACHE_NAME_ENTE_SIL, key = "{'enteTipoDovuto',#mygovEnteId, #codTipoDovuto}", unless="#result==null")
  public EnteSil getByEnteTipoDovuto(Long mygovEnteId, String codTipoDovuto) {
    List<EnteSil> result = enteSilDao.getByEnteTipoDovuto(mygovEnteId, codTipoDovuto);
    if(result.size()==2 &&
        result.get(0).getMygovEnteTipoDovutoId()!=null && result.get(0).getMygovEnteTipoDovutoId().getMygovEnteTipoDovutoId()!=null &&
        (result.get(1).getMygovEnteTipoDovutoId()==null || result.get(1).getMygovEnteTipoDovutoId().getMygovEnteTipoDovutoId()==null)){
      //case when there is a configuration for tipoDovuto and one for ente: take just the one of tipoDovuto
    } else if(result.size()>1) {
      log.error("found multiple EnteSil ({}) for mygovEnteId[{}] and codTipoDovuto[{}]", result.size(), mygovEnteId, codTipoDovuto);
      throw new MyPayException("multiple EnteSil for ente[" + mygovEnteId + "] and codTipoDovuto[" + codTipoDovuto + "]");
    } else if(result.isEmpty()){
      log.error("no EnteSil found for mygovEnteId[{}] and codTipoDovuto[{}]", mygovEnteId, codTipoDovuto);
      throw new MyPayException("no EnteSil found for ente[" + mygovEnteId + "] and codTipoDovuto[" + codTipoDovuto + "]");
    }
    return result.get(0);
  }

  @Cacheable(value= CacheService.CACHE_NAME_ENTE_SIL, key = "{'id',#mygovEnteSilId}", unless="#result==null")
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
