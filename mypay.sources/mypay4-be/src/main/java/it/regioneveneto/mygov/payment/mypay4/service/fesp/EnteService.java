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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.EnteDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("fespEnteService")
public class EnteService {

  @Autowired
  private EnteDao enteDao;

  @Cacheable(value=CacheService.CACHE_NAME_FESP_ENTE, key="{'mygovEnteId',#mygovEnteId}", unless="#result==null")
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
  public Ente getEnteById(Long mygovEnteId) {
    return enteDao.getEnteById(mygovEnteId);
  }

  @Cacheable(value=CacheService.CACHE_NAME_FESP_ENTE, key="{'codIpa',#codIpa}", unless="#result==null")
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
  public Ente getEnteByCodIpa(String codIpa) {
    return enteDao.getEnteByCodIpa(codIpa);
  }

  @Cacheable(value=CacheService.CACHE_NAME_FESP_ENTE, key="{'codFiscale',#codFiscale}", unless="#result==null")
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
  public Ente getEnteByCodFiscale(String codFiscale) {
   return enteDao.getEnteByCodFiscale(codFiscale);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
  public List<Ente> getAllEnti() {
    return enteDao.getAllEnti();
  }

}
