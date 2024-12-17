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
import org.jdbi.v3.core.statement.OutParameters;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.OutParameter;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlCall;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.Types;
import java.util.Date;
import java.util.List;

public interface ImportDovutiDao extends BaseDao {


  @SqlUpdate(
      "insert into mygov_import_dovuti (" +
          "  mygov_import_dovuti_id"+
          ", version"+
          ", mygov_ente_id"+
          ", mygov_utente_id"+
          ", mygov_anagrafica_stato_id"+
          ", cod_request_token"+
          ", dt_creazione"+
          ", dt_ultima_modifica"+
          ", de_nome_file_scarti"+
          ", cod_errore"+
          ") values ("+
          "  nextval('mygov_import_dovuti_id_seq')"+
          ", :d.version"+
          ", :d.mygovEnteId.mygovEnteId"+
          ", :d.mygovUtenteId.mygovUtenteId"+
          ", :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId"+
          ", :d.codRequestToken"+
          ", :d.dtCreazione"+
          ", :d.dtUltimaModifica"+
          ", :d.deNomeFileScarti"+
          ", :d.codErrore)"
  )
  @GetGeneratedKeys("mygov_import_dovuti_id")
  long insert(@BindBean("d")ImportDovuti d);

  @SqlQuery(
          " select "+ ImportDovuti.ALIAS+ALL_FIELDS+", "+ Ente.FIELDS+", "+ AnagraficaStato.FIELDS+", "+ Utente.FIELDS +
                  " from mygov_import_dovuti " + ImportDovuti.ALIAS +
                  " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+ImportDovuti.ALIAS+".mygov_ente_id " +
                  " left join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ImportDovuti.ALIAS+".mygov_anagrafica_stato_id " +
                  " inner join mygov_utente "+Utente.ALIAS+" on "+Utente.ALIAS+".mygov_utente_id = "+ImportDovuti.ALIAS+".mygov_utente_id " +
                  " where "+ImportDovuti.ALIAS+".cod_request_token = :codRequestToken "
  )
  @RegisterFieldMapper(ImportDovuti.class)
  List<ImportDovuti> getImportByRequestToken(String codRequestToken);

  @SqlUpdate(
          " update mygov_import_dovuti " +
                  " set mygov_anagrafica_stato_id = :i.mygovAnagraficaStatoId.mygovAnagraficaStatoId ," +
                  " version = :i.version ," +
                  " de_nome_file_scarti = :i.deNomeFileScarti ," +
                  " cod_errore = :i.codErrore ," +
                  " dt_ultima_modifica = :i.dtUltimaModifica "+
                  " where cod_request_token = :i.codRequestToken and mygov_ente_id = :i.mygovEnteId.mygovEnteId")
  int updateImportFlusso(@BindBean("i") ImportDovuti i);

  @SqlCall("{call update_insert_flusso( "+
          ":n_mygov_ente_id, "+
          ":n_iuf, "+
          ":n_dt_creazione, "+
          ":n_dt_ultima_modifica, "+
          ":sequence_value, "+
          ":eccezione "+
          ")}" )
  @OutParameter(name = "sequence_value", sqlType = Types.BIGINT)
  @OutParameter(name = "eccezione", sqlType = Types.VARCHAR)
  OutParameters callUpdateInsertFlussoFunction(Long n_mygov_ente_id, String n_iuf, Date n_dt_creazione, Date n_dt_ultima_modifica);
}
