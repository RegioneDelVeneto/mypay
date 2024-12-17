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

import it.regioneveneto.mygov.payment.mypay4.dto.TassonomiaCodDescTo;
import it.regioneveneto.mygov.payment.mypay4.dto.TassonomiaTo;
import it.regioneveneto.mygov.payment.mypay4.model.Tassonomia;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TassonomiaDao extends BaseDao {

  String SELECT_STAR_FROM_TABLE = "SELECT " + Tassonomia.ALIAS + ALL_FIELDS + " FROM mygov_tassonomia " + Tassonomia.ALIAS;

  @SqlQuery(
      "select " +
      "   tipo_ente as code" +
      " , descrizione_tipo_ente as description" +
      " from mygov_tassonomia" +
      " group by tipo_ente, descrizione_tipo_ente"

  )
  List<TassonomiaCodDescTo> getTipoEnteForSelect();

  @SqlQuery(
      "select " +
          "   prog_macro_area as code" +
          " , prog_macro_area||'.'||nome_macro_area as description" +
          " from mygov_tassonomia" +
          " where tipo_ente = :tipoEnte" +
          " group by prog_macro_area, nome_macro_area" +
          " order by prog_macro_area"
  )
  List<TassonomiaCodDescTo> getMacroAreaForSelect(String tipoEnte);

  @SqlQuery(
      "select " +
          "   cod_tipo_servizio as code" +
          " , cod_tipo_servizio||'.'||tipo_servizio as description" +
          " from mygov_tassonomia" +
          " where tipo_ente = :tipoEnte" +
          "   and prog_macro_area = :macroArea" +
          " group by cod_tipo_servizio, tipo_servizio" +
          " order by cod_tipo_servizio"
  )
  List<TassonomiaCodDescTo> getTipoServizioForSelect(String tipoEnte, String macroArea);

  @SqlQuery(
      "select " +
          "   motivo_riscossione as code" +
          " , motivo_riscossione as description" +
          " from mygov_tassonomia" +
          " where tipo_ente = :tipoEnte" +
          "   and prog_macro_area = :macroArea" +
          "   and cod_tipo_servizio = :codTipoServizio" +
          " group by motivo_riscossione"
  )
  List<TassonomiaCodDescTo> getMotivoRiscossioneforSelect(String tipoEnte, String macroArea, String codTipoServizio);

  @SqlQuery(
      "select " +
          "   codice_tassonomico as code" +
          " , codice_tassonomico as description" +
          " from mygov_tassonomia" +
          " where tipo_ente = :tipoEnte" +
          "   and prog_macro_area = :macroArea" +
          "   and cod_tipo_servizio = :codTipoServizio" +
          "   and motivo_riscossione = :motivoRiscossione" +
          " group by codice_tassonomico "
  )
  List<TassonomiaCodDescTo> getCodTassFromSelect(String tipoEnte, String macroArea, String codTipoServizio, String motivoRiscossione);

  @SqlQuery(
          " select true" +
          " where exists" +
          " (" +
          "   select 1 from mygov_tassonomia" +
          "   where codice_tassonomico = :codiceTassonomico" +
          " )"
  )
  Boolean ifExitsCodiceTassonomico(String codiceTassonomico);

  @SqlQuery(SELECT_STAR_FROM_TABLE + " where tipo_ente = :tipoEnte")
  @RegisterFieldMapper(Tassonomia.class)
  Collection<Tassonomia> getByTipoEnte(String tipoEnte);

  @SqlUpdate(" insert into mygov_tassonomia ( "+
    " mygov_tassonomia_id  "+
    //", version_my_pay" + //workaround due to previous mistake -- when insert, default(=0) will be used
    ", tipo_ente  "+
    ", descrizione_tipo_ente  "+
    ", prog_macro_area  "+
    ", nome_macro_area  "+
    ", desc_macro_area  "+
    ", cod_tipo_servizio  "+
    ", tipo_servizio  "+
    ", descrizione_tipo_servizio  "+
    ", motivo_riscossione  "+
    ", dt_inizio_validita  "+
    ", dt_fine_validita  "+
    ", codice_tassonomico  "+
    ", dt_creazione  "+
    ", dt_ultima_modifica  "+
    ", version "+
    ") values ( "+
    " nextval('mygov_tassonomia_mygov_tassonomia_id_seq') "+
    ", :t.tipoEnte"+
    ", :t.descrizioneTipoEnte"+
    ", :t.progMacroArea"+
    ", :t.nomeMacroArea"+
    ", :t.descMacroArea"+
    ", :t.codTipoServizio"+
    ", :t.tipoServizio"+
    ", :t.descrizioneTipoServizio"+
    ", :t.motivoRiscossione"+
    ", :t.dtInizioValidita"+
    ", :t.dtFineValidita"+
    ", :t.codiceTassonomico"+
    ", coalesce(:t.dtCreazione, now())" +
    ", coalesce(:t.dtUltimaModifica, now())" +
    ", :t.version" +
    ")")
  @GetGeneratedKeys("mygov_tassonomia_id")
	Long insert(@BindBean("t") Tassonomia tassonomia);
  @SqlUpdate("UPDATE mygov_tassonomia SET "+
    " version = :t.version" +
    ", tipo_ente = :t.tipoEnte" +
    ", descrizione_tipo_ente = :t.descrizioneTipoEnte" +
    ", prog_macro_area = :t.progMacroArea" +
    ", nome_macro_area = :t.nomeMacroArea" +
    ", desc_macro_area = :t.descMacroArea" +
    ", cod_tipo_servizio = :t.codTipoServizio" +
    ", tipo_servizio = :t.tipoServizio" +
    ", descrizione_tipo_servizio = :t.descrizioneTipoServizio" +
    ", motivo_riscossione = :t.motivoRiscossione" +
    ", dt_inizio_validita = :t.dtInizioValidita" +
    ", dt_fine_validita = :t.dtFineValidita" +
    ", codice_tassonomico = :t.codiceTassonomico" +
    ", dt_ultima_modifica = now()"+
    " WHERE mygov_tassonomia_id = :t.mygovTassonomiaId")
  int update(@BindBean("t") Tassonomia tassonomia);

  @SqlQuery(SELECT_STAR_FROM_TABLE +
    " WHERE dt_fine_validita BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '1 month'"
  )
  @RegisterFieldMapper(Tassonomia.class)
	Set<Tassonomia> getAllExpiring();

  @SqlQuery(SELECT_STAR_FROM_TABLE)
  Set<TassonomiaTo> getAll();
}
