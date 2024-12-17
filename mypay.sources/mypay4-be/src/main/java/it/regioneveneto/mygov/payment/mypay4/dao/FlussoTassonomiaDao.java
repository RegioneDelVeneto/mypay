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
import it.regioneveneto.mygov.payment.mypay4.model.FlussoTassonomia;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDate;
import java.util.List;

public interface FlussoTassonomiaDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_flusso_tassonomia (" +
          "  mygov_flusso_tassonomia_id" +
          " ,version" +
          " ,mygov_anagrafica_stato_id" +
          " ,iuft" +
          " ,num_righe_totali" +
          " ,num_righe_elaborate_correttamente" +
          " ,dt_creazione" +
          " ,dt_ultima_modifica" +
          " ,de_nome_operatore" +
          " ,de_percorso_file" +
          " ,de_nome_file" +
          " ,cod_request_token" +
          " ,cod_errore" +
          " ,hash" +
          ") values (" +
          " nextval('mygov_flusso_tassonomia_mygov_flusso_tassonomia_id_seq')" +
          " ,:ft.version" +
          " ,:ft.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          " ,:ft.iuft" +
          " ,:ft.numRigheTotali" +
          " ,:ft.numRigheElaborateCorrettamente" +
          " ,coalesce(:ft.dtCreazione, now())" +
          " ,coalesce(:ft.dtUltimaModifica, now())" +
          " ,:ft.deNomeOperatore" +
          " ,:ft.dePercorsoFile" +
          " ,:ft.deNomeFile" +
          " ,:ft.codRequestToken" +
          " ,:ft.codErrore"+
          " ,:ft.hash)"
  )
  @GetGeneratedKeys("mygov_flusso_tassonomia_id")
  Long insert(@BindBean("ft") FlussoTassonomia ft);

  @SqlQuery(
      "    select "+FlussoTassonomia.ALIAS+ALL_FIELDS+", "+ AnagraficaStato.FIELDS +
          "  from mygov_flusso_tassonomia " + FlussoTassonomia.ALIAS +
          "  join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+
          "    on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+FlussoTassonomia.ALIAS+".mygov_anagrafica_stato_id " +
          " where "+FlussoTassonomia.ALIAS+".iuft not like '\\_%'" +
          "   and (:nomeTassonomia is null or "+FlussoTassonomia.ALIAS+".iuft ilike '%' || :nomeTassonomia || '%')" +
          "   and ("+FlussoTassonomia.ALIAS+".dt_creazione >= :dateFrom::DATE and "+FlussoTassonomia.ALIAS+".dt_creazione < :dateTo::DATE) " +
          " order by " + FlussoTassonomia.ALIAS + ".dt_creazione desc")
  @RegisterFieldMapper(FlussoTassonomia.class)
  List<FlussoTassonomia> searchTassonomie(String nomeTassonomia, LocalDate dateFrom, LocalDate dateTo);

  @SqlQuery(
    "    select "+FlussoTassonomia.ALIAS+ALL_FIELDS+", "+ AnagraficaStato.FIELDS +
        "  from mygov_flusso_tassonomia " + FlussoTassonomia.ALIAS +
        "  join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+
        "    on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+FlussoTassonomia.ALIAS+".mygov_anagrafica_stato_id " +
        " order by " + FlussoTassonomia.ALIAS + ".mygov_flusso_tassonomia_id desc" +
        " limit 1"
  )
  @RegisterFieldMapper(FlussoTassonomia.class)
  FlussoTassonomia getLast();

  @SqlUpdate(
    "    update mygov_flusso_tassonomia set " +
    " ,version = :ft.version" +
    " ,mygov_anagrafica_stato_id = :ft.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
    " ,iuft = :ft.iuft" +
    " ,num_righe_totali = :ft.numRigheTotali" +
    " ,num_righe_elaborate_correttamente = :ft.numRigheElaborateCorrettamente" +
    " ,dt_creazione = :ft.dtCreazione" +
    " ,dt_ultima_modifica = :ft.dtUltimaModifica" +
    " ,de_nome_operatore = :ft.deNomeOperatore" +
    " ,de_percorso_file = :ft.dePercorsoFile" +
    " ,de_nome_file = :ft.deNomeFile" +
    " ,cod_request_token = :ft.codRequestToken" +
    " ,cod_errore = :ft.codErrore" +
    " where mygov_flusso_tassonomia_id = :ft.mygov_flusso_tassonomia_id"
  )
  int update(@BindBean("ft") FlussoTassonomia ft);

  @SqlQuery(
    "    select "+FlussoTassonomia.ALIAS+ALL_FIELDS+", "+ AnagraficaStato.FIELDS +
      "  from mygov_flusso_tassonomia " + FlussoTassonomia.ALIAS +
      "  join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+
      "    on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+FlussoTassonomia.ALIAS+".mygov_anagrafica_stato_id " +
      " where "+FlussoTassonomia.ALIAS+".mygov_flusso_tassonomia_id = :mygovFlussoTassonomiaId"
  )
  @RegisterFieldMapper(FlussoTassonomia.class)
  FlussoTassonomia getById(Long mygovFlussoTassonomiaId);
}
