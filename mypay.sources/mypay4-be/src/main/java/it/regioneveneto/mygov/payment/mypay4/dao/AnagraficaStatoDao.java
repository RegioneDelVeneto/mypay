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
package it.regioneveneto.mygov.payment.mypay4.dao;

import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

import java.util.List;

public interface AnagraficaStatoDao extends BaseDao {
  @SqlQuery(
      "    select "+AnagraficaStato.ALIAS+ALL_FIELDS+
          "  from mygov_anagrafica_stato "+AnagraficaStato.ALIAS+
          " where "+AnagraficaStato.ALIAS+".cod_stato = :codStato " +
          "   and "+AnagraficaStato.ALIAS+".de_tipo_stato = :deTipoStato ")
  @RegisterFieldMapper(AnagraficaStato.class)
  @Caching(
      put = {
          @CachePut(value = CacheService.CACHE_NAME_ANAGRAFICA_STATO, key = "{'id',#result.mygovAnagraficaStatoId}", condition="#result!=null"),
          @CachePut(value = CacheService.CACHE_NAME_ANAGRAFICA_STATO, key = "{'codStato+deTipoStato',#result.codStato,#result.deTipoStato}", condition="#result!=null")
      }
  )
  AnagraficaStato getByCodStatoAndTipoStato(String codStato, String deTipoStato);

  @SqlQuery(
      "    select "+AnagraficaStato.ALIAS+ALL_FIELDS+
          "  from mygov_anagrafica_stato "+AnagraficaStato.ALIAS+
          " where "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = :mygovAnagraficaStatoId ")
  @RegisterFieldMapper(AnagraficaStato.class)
  @Caching(
      put = {
          @CachePut(value = CacheService.CACHE_NAME_ANAGRAFICA_STATO, key = "{'id',#result.mygovAnagraficaStatoId}", condition="#result!=null"),
          @CachePut(value = CacheService.CACHE_NAME_ANAGRAFICA_STATO, key = "{'codStato+deTipoStato',#result.codStato,#result.deTipoStato}", condition="#result!=null")
      }
  )
  AnagraficaStato getById(Long mygovAnagraficaStatoId);

  @SqlQuery(
      "    select "+AnagraficaStato.ALIAS+ALL_FIELDS+
          "  from mygov_anagrafica_stato "+AnagraficaStato.ALIAS+
          "  where "+AnagraficaStato.ALIAS+".de_tipo_stato = :deTipoStato ")
  @RegisterFieldMapper(AnagraficaStato.class)
  List<AnagraficaStato> getByTipoStato(String deTipoStato);
}
