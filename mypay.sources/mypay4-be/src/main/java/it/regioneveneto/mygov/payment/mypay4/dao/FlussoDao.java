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

import it.regioneveneto.mygov.payment.mypay4.model.*;
import org.jdbi.v3.core.mapper.JoinRow;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.config.RegisterJoinRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlussoDao extends BaseDao {

  @SqlUpdate(
      "    update mygov_flusso set " +
          "   version = :d.version" +
          " , mygov_ente_id = :d.mygovEnteId.mygovEnteId" +
          " , mygov_anagrafica_stato_id = :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          " , iuf = :d.iuf" +
          " , num_righe_totali = :d.numRigheTotali" +
          " , num_righe_importate_correttamente = :d.numRigheImportateCorrettamente" +
          " , dt_creazione = :d.dtCreazione" +
          " , dt_ultima_modifica = :d.dtUltimaModifica" +
          " , flg_attivo = :d.flgAttivo" +
          " , de_nome_operatore = :d.deNomeOperatore" +
          " , flg_spontaneo = :d.flgSpontaneo" +
          " , de_percorso_file = :d.dePercorsoFile" +
          " , de_nome_file = :d.deNomeFile" +
          " , pdf_generati = :d.pdfGenerati" +
          " , cod_request_token = :d.codRequestToken" +
          " , cod_errore = :d.codErrore" +
          " where mygov_flusso_id = :d.mygovFlussoId"
  )
  int update(@BindBean("d") Flusso d);

  @SqlUpdate(
      " insert into mygov_flusso (" +
          "   mygov_flusso_id" +
          " , version" +
          " , mygov_ente_id" +
          " , mygov_anagrafica_stato_id" +
          " , iuf" +
          " , num_righe_totali" +
          " , num_righe_importate_correttamente" +
          " , dt_creazione" +
          " , dt_ultima_modifica" +
          " , flg_attivo" +
          " , de_nome_operatore" +
          " , flg_spontaneo" +
          " , de_percorso_file" +
          " , de_nome_file" +
          " , pdf_generati" +
          " , cod_request_token" +
          " , cod_errore" +
          " ) values (" +
          "   nextval('mygov_flusso_mygov_flusso_id_seq')" +
          " , :d.version" +
          " , :d.mygovEnteId.mygovEnteId" +
          " , :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          " , :d.iuf" +
          " , :d.numRigheTotali" +
          " , :d.numRigheImportateCorrettamente" +
          " , coalesce(:d.dtCreazione, now())" +
          " , coalesce(:d.dtUltimaModifica, now())" +
          " , :d.flgAttivo" +
          " , :d.deNomeOperatore" +
          " , :d.flgSpontaneo" +
          " , :d.dePercorsoFile" +
          " , :d.deNomeFile" +
          " , :d.pdfGenerati" +
          " , :d.codRequestToken" +
          " , :d.codErrore)"
  )
  @GetGeneratedKeys("mygov_flusso_id")
  long insert(@BindBean("d") Flusso d);

  @SqlQuery(
      "    select "+Flusso.ALIAS+ALL_FIELDS +", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS +
          "  from mygov_flusso " + Flusso.ALIAS +
          "  join mygov_ente " + Ente.ALIAS + "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+
          "    on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " where "+Flusso.ALIAS+".mygov_flusso_id = :mygovFlussoId ")
  @RegisterFieldMapper(Flusso.class)
  Flusso getById(Long mygovFlussoId);

  @SqlQuery(
      "    select " + Flusso.ALIAS + ALL_FIELDS +
          "  from mygov_flusso " + Flusso.ALIAS +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Flusso.ALIAS+".mygov_ente_id = :mygovEnteId " +
          "   and (:spontaneo = true AND "+Flusso.ALIAS+".flg_spontaneo = true OR " +
          "        :spontaneo = false AND not "+Flusso.ALIAS+".iuf like '_'||"+Ente.ALIAS+".cod_ipa_ente||'_%') " +
          " order by " + Flusso.ALIAS + ".iuf")
  @RegisterFieldMapper(Flusso.class)
  List<Flusso> getByEnte(Long mygovEnteId, boolean spontaneo);

  String SQL_SEARCH =
      "  from mygov_flusso " + Flusso.ALIAS +
          "  join mygov_ente "+Ente.ALIAS+" on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+
          "    on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          "  left join mygov_import_dovuti "+ImportDovuti.ALIAS+" on "+Flusso.ALIAS+".cod_request_token = "+ImportDovuti.ALIAS+".cod_request_token " +
          "  left join mygov_utente "+ Utente.ALIAS+" on "+Utente.ALIAS+".mygov_utente_id = "+ImportDovuti.ALIAS+".mygov_utente_id " +
          " where "+Flusso.ALIAS+".mygov_ente_id = :mygovEnteId " +
          "   and "+Flusso.ALIAS+".flg_spontaneo <> true " +
          "   and "+Flusso.ALIAS+".iuf not like '\\_%' " +
          "   and ("+Flusso.ALIAS+".dt_creazione >= :dateFrom::DATE and "+Flusso.ALIAS+".dt_creazione < :dateTo::DATE) " +
          "   and (:iuf is null or "+Flusso.ALIAS+".iuf ilike '%' || :iuf || '%')";

  @SqlQuery(
      "    select "+Flusso.ALIAS+ALL_FIELDS +", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS+", "+Utente.FIELDS +
          SQL_SEARCH +
          " order by " + Flusso.ALIAS + ".dt_creazione desc" +
          " limit <queryLimit>")
  @RegisterFieldMapper(value = Flusso.class)
  @RegisterFieldMapper(value = Utente.class, prefix = Utente.ALIAS)
  @RegisterJoinRowMapper({Flusso.class, Utente.class})
  List<JoinRow> getByEnteIufCreateDt(Long mygovEnteId, String iuf, LocalDate dateFrom, LocalDate dateTo, @Define int queryLimit);

  @SqlQuery(
      "    select count(1)" +
          SQL_SEARCH)
  int getByEnteIufCreateDtCount(Long mygovEnteId, String iuf, LocalDate dateFrom, LocalDate dateTo);

  @SqlQuery(
      "    select "+Flusso.ALIAS+ALL_FIELDS +", "+AnagraficaStato.FIELDS +
          "  from mygov_flusso " + Flusso.ALIAS +
          "  join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+
          "    on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " where "+Flusso.ALIAS+".iuf = :iuf ")
  @RegisterFieldMapper(Flusso.class)
  Optional<Flusso> getByIuf(String iuf);

  @SqlQuery(
      "    select "+Flusso.ALIAS+ALL_FIELDS +
          " from mygov_flusso " + Flusso.ALIAS +
          " where "+Flusso.ALIAS+".cod_request_token = :codRequestToken ")
  @RegisterFieldMapper(Flusso.class)
  Flusso getByCodRequestToken(String codRequestToken);

  @SqlQuery(
      "    select "+Flusso.ALIAS+ALL_FIELDS +", "+AnagraficaStato.FIELDS +
          "  from mygov_flusso " + Flusso.ALIAS +
          "  join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+
          "    on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " where "+Flusso.ALIAS+".iuf in (<iufs>) ")
  @RegisterFieldMapper(Flusso.class)
  List<Flusso> getByIuf(@BindList(onEmpty= BindList.EmptyHandling.NULL_STRING) List<String> iufs);

  @SqlQuery(
          "select count(*) from mygov_flusso where de_nome_file = :deNomeFile"
  )
  int countDuplicateFileName(String deNomeFile);

}
