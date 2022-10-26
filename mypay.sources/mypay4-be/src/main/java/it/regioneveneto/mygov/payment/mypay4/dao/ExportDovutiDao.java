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
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.ExportDovuti;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDate;
import java.util.List;

public interface ExportDovutiDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_export_dovuti (" +
          "  mygov_export_dovuti_id"+
          ", version"+
          ", mygov_ente_id"+
          ", mygov_anagrafica_stato_id"+
          ", de_nome_file_generato"+
          ", num_dimensione_file_generato"+
          ", dt_inizio_estrazione"+
          ", dt_fine_estrazione"+
          ", dt_creazione"+
          ", dt_ultima_modifica"+
          ", cod_tipo_dovuto"+
          ", cod_request_token"+
          ", mygov_utente_id"+
          ", flg_ricevuta"+
          ", flg_incrementale"+
          ", versione_tracciato"+
          ") values ("+
          "  nextval('mygov_export_dovuti_mygov_export_dovuti_id_seq')"+
          ", :d.version"+
          ", :d.mygovEnteId.mygovEnteId"+
          ", :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId"+
          ", :d.deNomeFileGenerato"+
          ", :d.numDimensioneFileGenerato"+
          ", :d.dtInizioEstrazione"+
          ", :d.dtFineEstrazione"+
          ", :d.dtCreazione"+
          ", :d.dtUltimaModifica"+
          ", :d.codTipoDovuto"+
          ", :d.codRequestToken"+
          ", :d.mygovUtenteId.mygovUtenteId"+
          ", :d.flgRicevuta"+
          ", :d.flgIncrementale"+
          ", :d.versioneTracciato"+
          ")"
  )
  @GetGeneratedKeys("mygov_export_dovuti_id")
  long insert(@BindBean("d") ExportDovuti d);

  String SQL_SEARCH_EXPORT =
      " from mygov_export_dovuti " + ExportDovuti.ALIAS +
      " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+ExportDovuti.ALIAS+".mygov_ente_id " +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ExportDovuti.ALIAS+".mygov_anagrafica_stato_id " +
      " inner join mygov_utente "+Utente.ALIAS+" on "+Utente.ALIAS+".mygov_utente_id = "+ExportDovuti.ALIAS+".mygov_utente_id " +
      " where "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId" +
      " and "+AnagraficaStato.ALIAS+".cod_stato = 'EXPORT_ESEGUITO'" +
      " and "+AnagraficaStato.ALIAS+".de_tipo_stato = 'export'" +
      //" and "+ExportDovuti.ALIAS+".de_nome_file_generato is not null " +
      " and ( :nomeFile is null or "+ExportDovuti.ALIAS+".de_nome_file_generato ilike '%' || :nomeFile || '%') " +
      " and "+Utente.ALIAS+".cod_fed_user_id = :codFedUserId" +
      " and ( :dateFrom::DATE <= "+ExportDovuti.ALIAS+".dt_ultima_modifica and "+ExportDovuti.ALIAS+".dt_ultima_modifica < :dateTo::DATE ) ";

  @SqlQuery(
      " select "+ExportDovuti.ALIAS+ALL_FIELDS+", "+Ente.FIELDS+", "+AnagraficaStato.FIELDS+", "+Utente.FIELDS +
          SQL_SEARCH_EXPORT +
          " order by "+ExportDovuti.ALIAS+".dt_creazione DESC " +
          " limit <queryLimit>"
  )
  @RegisterFieldMapper(ExportDovuti.class)
  List<ExportDovuti> getByEnteNomefileDtmodifica(Long mygovEnteId, String codFedUserId, String nomeFile,
                                                 LocalDate dateFrom, LocalDate dateTo, @Define int queryLimit);

  @SqlQuery(
      " select count(1) " +
          SQL_SEARCH_EXPORT
  )
  @RegisterFieldMapper(ExportDovuti.class)
  int getByEnteNomefileDtmodificaCount(Long mygovEnteId, String codFedUserId, String nomeFile,
                                                 LocalDate dateFrom, LocalDate dateTo);

  @SqlQuery(
          " select "+ExportDovuti.ALIAS+ALL_FIELDS+", "+Ente.FIELDS+", "+AnagraficaStato.FIELDS+", "+Utente.FIELDS +
                  " from mygov_export_dovuti " + ExportDovuti.ALIAS +
                  " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+ExportDovuti.ALIAS+".mygov_ente_id " +
                  " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ExportDovuti.ALIAS+".mygov_anagrafica_stato_id " +
                  " inner join mygov_utente "+Utente.ALIAS+" on "+Utente.ALIAS+".mygov_utente_id = "+ExportDovuti.ALIAS+".mygov_utente_id " +
                  " where "+ExportDovuti.ALIAS+".cod_request_token = :codRequestToken "
  )
  @RegisterFieldMapper(ExportDovuti.class)
  List<ExportDovuti> getExportByRequestToken(String codRequestToken);


}
