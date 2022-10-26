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
import it.regioneveneto.mygov.payment.mypay4.model.fesp.ProgressiviVersamento;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

public interface ProgressiviVersamentoDao extends BaseDao {

  @SqlQuery(
      "    select " + ProgressiviVersamento.ALIAS + ALL_FIELDS +
          "  from mygov_progressiviversamento " + ProgressiviVersamento.ALIAS +
          " where "+ProgressiviVersamento.ALIAS+".cod_ipa_ente = :codIpaEnte " +
          "   and "+ProgressiviVersamento.ALIAS+".tipo_generatore = :tipoGeneratore " +
          "   and "+ProgressiviVersamento.ALIAS+".tipo_versamento  = :tipoVersamento " +
          " <if(forUpdate)> for update <endif>" )
  @RegisterFieldMapper(ProgressiviVersamento.class)
  @UseStringTemplateEngine
  ProgressiviVersamento getByKey(final String codIpaEnte, final String tipoGeneratore, final String tipoVersamento, @Define boolean forUpdate);

  @SqlUpdate("insert into mygov_progressiviversamento " +
      " (id, version, cod_ipa_ente, tipo_generatore, tipo_versamento, progressivo_versamento) " +
      " values (nextval('mygov_progressiviversamento_id_seq'), :version, :codIpaEnte, :tipoGeneratore, :tipoVersamento, :progressivoVersamento)")
  void insert(@BindBean ProgressiviVersamento progressiviVersamento);

  @SqlUpdate("update mygov_progressiviversamento " +
      " set progressivo_versamento = :progressivoVersamento " +
      " where id = :id")
  void updateProgressivoVersamento(Long id, Long progressivoVersamento);
}
