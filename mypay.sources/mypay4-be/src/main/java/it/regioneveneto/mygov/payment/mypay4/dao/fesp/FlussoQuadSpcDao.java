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
import it.regioneveneto.mygov.payment.mypay4.model.fesp.FlussoQuadSpc;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.FlussoSpc;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface FlussoQuadSpcDao extends BaseDao {

  @SqlQuery(
      "    select "+FlussoQuadSpc.ALIAS+".mygov_flusso_quad_spc_id as id " +
          ","+FlussoQuadSpc.ALIAS+".version " +
          ","+FlussoQuadSpc.ALIAS+".cod_ipa_ente " +
          ", null as identificativo_psp " +
          ","+FlussoQuadSpc.ALIAS+".cod_identificativo_flusso " +
          ","+FlussoQuadSpc.ALIAS+".dt_data_ora_flusso " +
          ","+FlussoQuadSpc.ALIAS+".de_nome_file_scaricato " +
          ","+FlussoQuadSpc.ALIAS+".num_dimensione_file_scaricato " +
          ","+FlussoQuadSpc.ALIAS+".dt_creazione " +
          ","+FlussoQuadSpc.ALIAS+".dt_ultima_modifica " +
          ","+FlussoQuadSpc.ALIAS+".cod_stato " +
          "  from mygov_flusso_quad_spc " + FlussoQuadSpc.ALIAS +
          "  where "+FlussoQuadSpc.ALIAS+".cod_ipa_ente = :codIpaEnte " +
          "  and "+FlussoQuadSpc.ALIAS+".cod_stato = 'OK' " +
          "  and (" +
          "       (:prodOrDisp = 'P' and (:from <= "+FlussoQuadSpc.ALIAS+".dt_data_ora_flusso and "+FlussoQuadSpc.ALIAS+".dt_data_ora_flusso <= :to))" +
          "    or (:prodOrDisp = 'D' and (:from <= "+FlussoQuadSpc.ALIAS+".dt_creazione and "+FlussoQuadSpc.ALIAS+".dt_creazione <= :to))" +
          "   )" +
          "  order by " +
          "    case when :prodOrDisp = 'P' then "+FlussoQuadSpc.ALIAS+".dt_data_ora_flusso " +
          "         when :prodOrDisp = 'D' then "+FlussoQuadSpc.ALIAS+".dt_creazione " +
          "    end , "+FlussoQuadSpc.ALIAS+".mygov_flusso_quad_spc_id desc")
  @RegisterFieldMapper(FlussoSpc.class)
  List<FlussoSpc> getByCodIpaEnte(String codIpaEnte, LocalDate from, LocalDate to, String prodOrDisp);

  @SqlQuery(
      "    select "+FlussoQuadSpc.ALIAS+".mygov_flusso_quad_spc_id as id " +
          ","+FlussoQuadSpc.ALIAS+".version " +
          ","+FlussoQuadSpc.ALIAS+".cod_ipa_ente " +
          ", null as identificativo_psp " +
          ","+FlussoQuadSpc.ALIAS+".cod_identificativo_flusso " +
          ","+FlussoQuadSpc.ALIAS+".dt_data_ora_flusso " +
          ","+FlussoQuadSpc.ALIAS+".de_nome_file_scaricato " +
          ","+FlussoQuadSpc.ALIAS+".num_dimensione_file_scaricato " +
          ","+FlussoQuadSpc.ALIAS+".dt_creazione " +
          ","+FlussoQuadSpc.ALIAS+".dt_ultima_modifica " +
          ","+FlussoQuadSpc.ALIAS+".cod_stato " +
          "  from mygov_flusso_quad_spc " + FlussoQuadSpc.ALIAS +
          "  where "+FlussoQuadSpc.ALIAS+".cod_ipa_ente = :codIpaEnte" +
          "  and "+FlussoQuadSpc.ALIAS+".cod_identificativo_flusso = :codIdentificativoFlusso" +
          "  and "+FlussoQuadSpc.ALIAS+".dt_data_ora_flusso = :dtDataOraFlusso::timestamp" +
          "  and "+FlussoQuadSpc.ALIAS+".cod_stato <> :codStato"
  )
  @RegisterFieldMapper(FlussoSpc.class)
  List<FlussoSpc> getByEnteIdFlussoExclStato(String codIpaEnte, String codIdentificativoFlusso, Date dtDataOraFlusso, String codStato);
}
