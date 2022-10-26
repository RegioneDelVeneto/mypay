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
package it.regioneveneto.mygov.payment.mypay4.dao.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.BaseDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

import java.util.List;

public interface EnteDao extends BaseDao {

  @SqlQuery(
    "    select " + Ente.ALIAS + ALL_FIELDS +
      "  from mygov_ente " + Ente.ALIAS +
      " where "+ Ente.ALIAS+".mygov_ente_id = :mygovEnteId ")
  @RegisterFieldMapper(Ente.class)
  @Caching(
    put = {
      @CachePut(value = CacheService.CACHE_NAME_FESP_ENTE, key = "{'mygovEnteId',#result.mygovEnteId}", condition="#result!=null"),
      @CachePut(value = CacheService.CACHE_NAME_FESP_ENTE, key = "{'codIpa',#result.codIpaEnte}", condition="#result!=null"),
      @CachePut(value = CacheService.CACHE_NAME_FESP_ENTE, key = "{'codFiscale',#result.codiceFiscaleEnte}", condition="#result!=null")
    }
  )
  Ente getEnteById(Long mygovEnteId);

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +
          "  from mygov_ente " + Ente.ALIAS +
          " where "+ Ente.ALIAS+".cod_ipa_ente = :codIpa ")
  @RegisterFieldMapper(Ente.class)
  @Caching(
      put = {
        @CachePut(value = CacheService.CACHE_NAME_FESP_ENTE, key = "{'mygovEnteId',#result.mygovEnteId}", condition="#result!=null"),
        @CachePut(value = CacheService.CACHE_NAME_FESP_ENTE, key = "{'codIpa',#result.codIpaEnte}", condition="#result!=null"),
        @CachePut(value = CacheService.CACHE_NAME_FESP_ENTE, key = "{'codFiscale',#result.codiceFiscaleEnte}", condition="#result!=null")
      }
  )
  Ente getEnteByCodIpa(String codIpa);

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +
          "  from mygov_ente " + Ente.ALIAS +
          " where "+Ente.ALIAS+".codice_fiscale_ente = :codFiscale ")
  @RegisterFieldMapper(Ente.class)
  @Caching(
      put = {
        @CachePut(value = CacheService.CACHE_NAME_FESP_ENTE, key = "{'mygovEnteId',#result.mygovEnteId}", condition="#result!=null"),
        @CachePut(value = CacheService.CACHE_NAME_FESP_ENTE, key = "{'codIpa',#result.codIpaEnte}", condition="#result!=null"),
        @CachePut(value = CacheService.CACHE_NAME_FESP_ENTE, key = "{'codFiscale',#result.codiceFiscaleEnte}", condition="#result!=null")
      }
  )
  Ente getEnteByCodFiscale(String codFiscale);

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +
          "  from mygov_ente " + Ente.ALIAS +
          " order by " + Ente.ALIAS + ".mygov_ente_id")
  @RegisterFieldMapper(Ente.class)
  List<Ente> getAllEnti();
}
