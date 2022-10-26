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

import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.Flusso;
import it.regioneveneto.mygov.payment.mypay4.model.IdentificativoUnivoco;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface IdentificativoUnivocoDao extends BaseDao {

  @SqlQuery(
      " select "+ IdentificativoUnivoco.ALIAS + ALL_FIELDS+", "+ Flusso.FIELDS +","+ Ente.FIELDS+
          " from mygov_identificativo_univoco " + IdentificativoUnivoco.ALIAS +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+IdentificativoUnivoco.ALIAS+".mygov_flusso_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " where "+IdentificativoUnivoco.ALIAS+".mygov_ente_id = :enteId" +
          "   and "+IdentificativoUnivoco.ALIAS+".cod_tipo_identificativo = :codTipoIdentificativo" +
          "   and "+IdentificativoUnivoco.ALIAS+".identificativo = :identificativo")
  @RegisterFieldMapper(IdentificativoUnivoco.class)
  List<IdentificativoUnivoco> getByEnteAndCodTipoIdAndId(Long enteId, String codTipoIdentificativo, String identificativo);

  @SqlUpdate("INSERT INTO mygov_identificativo_univoco (" +
      "  mygov_identificativo_univoco_id" +
      ", version" +
      ", mygov_ente_id" +
      ", mygov_flusso_id" +
      ", cod_tipo_identificativo" +
      ", identificativo" +
      ", dt_inserimento" +
      ") VALUES (" +
      "nextval('mygov_identificativo_univoco_id_seq') " +
      ", :i.version" +
      ", :i.mygovEnteId" +
      ", :i.mygovFlussoId" +
      ", :i.codTipoIdentificativo" +
      ", :i.identificativo" +
      ", :i.dtInserimento)")
  @GetGeneratedKeys("mygov_identificativo_univoco_id")
  Long insert(@BindBean("i") IdentificativoUnivoco i);
}
