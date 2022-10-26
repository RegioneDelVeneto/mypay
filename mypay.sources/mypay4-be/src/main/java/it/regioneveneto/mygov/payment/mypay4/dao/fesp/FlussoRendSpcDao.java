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
import it.regioneveneto.mygov.payment.mypay4.model.fesp.FlussoRendSpc;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.FlussoSpc;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface FlussoRendSpcDao extends BaseDao {

  @SqlQuery(
      "    select "+ FlussoRendSpc.ALIAS+".mygov_flusso_rend_spc_id as id " +
          ","+FlussoRendSpc.ALIAS+".version " +
          ","+FlussoRendSpc.ALIAS+".cod_ipa_ente " +
          ","+FlussoRendSpc.ALIAS+".identificativo_psp " +
          ","+FlussoRendSpc.ALIAS+".cod_identificativo_flusso " +
          ","+FlussoRendSpc.ALIAS+".dt_data_ora_flusso " +
          ","+FlussoRendSpc.ALIAS+".de_nome_file_scaricato " +
          ","+FlussoRendSpc.ALIAS+".num_dimensione_file_scaricato " +
          ","+FlussoRendSpc.ALIAS+".dt_creazione " +
          ","+FlussoRendSpc.ALIAS+".dt_ultima_modifica " +
          ","+FlussoRendSpc.ALIAS+".cod_stato " +
          "  from mygov_flusso_rend_spc " + FlussoRendSpc.ALIAS +
          "  where "+FlussoRendSpc.ALIAS+".cod_ipa_ente = :codIpaEnte " +
          "  and (coalesce(:identificativoPsp, '') = '' or "+FlussoRendSpc.ALIAS+".identificativo_psp = :identificativoPsp) " +
          "  and "+FlussoRendSpc.ALIAS+".cod_stato = 'OK' " +
          "  and (" +
          "       (:prodOrDisp = 'P' and (:from <= "+FlussoRendSpc.ALIAS+".dt_data_ora_flusso and "+FlussoRendSpc.ALIAS+".dt_data_ora_flusso <= :to))" +
          "    or (:prodOrDisp = 'D' and (:from <= "+FlussoRendSpc.ALIAS+".dt_creazione and "+FlussoRendSpc.ALIAS+".dt_creazione <= :to))" +
          "   )" +
          "  order by " +
          "    case when :prodOrDisp = 'P' then "+FlussoRendSpc.ALIAS+".dt_data_ora_flusso " +
          "         when :prodOrDisp = 'D' then "+FlussoRendSpc.ALIAS+".dt_creazione " +
          "    end , "+FlussoRendSpc.ALIAS+".mygov_flusso_rend_spc_id desc")
  @RegisterFieldMapper(FlussoSpc.class)
  List<FlussoSpc> getByCodIpaEnteIdentificativoPsp(String codIpaEnte, String identificativoPsp, LocalDate from, LocalDate to, String prodOrDisp);

  @SqlQuery(
      "    select "+ FlussoRendSpc.ALIAS+".mygov_flusso_rend_spc_id as id " +
          ","+FlussoRendSpc.ALIAS+".version " +
          ","+FlussoRendSpc.ALIAS+".cod_ipa_ente " +
          ","+FlussoRendSpc.ALIAS+".identificativo_psp " +
          ","+FlussoRendSpc.ALIAS+".cod_identificativo_flusso " +
          ","+FlussoRendSpc.ALIAS+".dt_data_ora_flusso " +
          ","+FlussoRendSpc.ALIAS+".de_nome_file_scaricato " +
          ","+FlussoRendSpc.ALIAS+".num_dimensione_file_scaricato " +
          ","+FlussoRendSpc.ALIAS+".dt_creazione " +
          ","+FlussoRendSpc.ALIAS+".dt_ultima_modifica " +
          ","+FlussoRendSpc.ALIAS+".cod_stato " +
          "  from mygov_flusso_rend_spc " + FlussoRendSpc.ALIAS +
          "  where "+FlussoRendSpc.ALIAS+".cod_ipa_ente = :codIpaEnte" +
          "    and "+FlussoRendSpc.ALIAS+".identificativo_psp = :identificativoPsp" +
          "    and "+FlussoRendSpc.ALIAS+".cod_identificativo_flusso = :codIdentificativoFlusso" +
          "    and "+FlussoRendSpc.ALIAS+".dt_data_ora_flusso = :dtDataOraFlusso::timestamp" +
          "    and "+FlussoRendSpc.ALIAS+".cod_stato <> :codStato"
  )
  @RegisterFieldMapper(FlussoSpc.class)
  List<FlussoSpc> getByEnteIdFlussoExclStato(String codIpaEnte, String identificativoPsp, String codIdentificativoFlusso, Date dtDataOraFlusso, String codStato);

  @SqlUpdate("INSERT INTO mygov_flusso_rend_spc (" +
      " mygov_flusso_rend_spc_id, \"version\", cod_ipa_ente, identificativo_psp, " +
      " cod_identificativo_flusso, dt_data_ora_flusso, de_nome_file_scaricato, num_dimensione_file_scaricato, " +
      " dt_creazione, dt_ultima_modifica, cod_stato " +
      " ) VALUES ( " +
      " nextval('mygov_flusso_rend_spc_mygov_flusso_rend_spc_id_seq'), 0, :codIpaEnte, :identificativoPsp, " +
      " :codIdentificativoFlusso, :dtDataOraFlusso, null, 0, " +
      " current_timestamp, current_timestamp, :stato) " +
      " returning mygov_flusso_rend_spc_id ")
  @GetGeneratedKeys("mygov_flusso_rend_spc_id")
  Integer insert(String codIpaEnte, String identificativoPsp, String codIdentificativoFlusso, Timestamp dtDataOraFlusso, String stato);

  @SqlUpdate("UPDATE mygov_flusso_rend_spc SET " +
      " version = version + 1, " +
      " de_nome_file_scaricato = :nomeFileScaricato, " +
      " num_dimensione_file_scaricato = :numDimensioneFileScaricato, " +
      " dt_ultima_modifica = CURRENT_TIMESTAMP, " +
      " cod_stato = :codStato " +
      " WHERE mygov_flusso_rend_spc_id = :idMygovFlussoRendSpc")
  int update(Integer idMygovFlussoRendSpc, String nomeFileScaricato, long numDimensioneFileScaricato, final String codStato);
}
