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

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.TipiVersamentoDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.TipiVersamento;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TipiVersamentoService {

  @Autowired
  private TipiVersamentoDao tipiVersamentoDao;

  @Cacheable(value=CacheService.CACHE_NAME_FESP_TIPI_VERSAMENTO, key="{'tipoVersamento',#tipoVersamento}", unless="#result==null")
  public TipiVersamento getByTipoVersamento(final String tipoVersamento) {
    return tipiVersamentoDao.getByTipoVersamento(tipoVersamento);
  }
}
