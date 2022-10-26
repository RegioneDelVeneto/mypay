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

import it.regioneveneto.mygov.payment.mypay4.model.DatiMarcaBolloDigitale;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface DatiMarcaBolloDigitaleDao extends BaseDao {

  @SqlQuery(
      " select "+DatiMarcaBolloDigitale.ALIAS+ALL_FIELDS +
          " from mygov_dati_marca_bollo_digitale "+DatiMarcaBolloDigitale.ALIAS +
          " where "+DatiMarcaBolloDigitale.ALIAS+".mygov_dati_marca_bollo_digitale_id = :id"
  )
  @RegisterFieldMapper(DatiMarcaBolloDigitale.class)
  DatiMarcaBolloDigitale getById(Long id);

  @SqlUpdate("INSERT INTO mygov_dati_marca_bollo_digitale (" +
      "  mygov_dati_marca_bollo_digitale_id" +
      ", version" +
      ", tipo_bollo" +
      ", hash_documento" +
      ", provincia_residenza" +
      ") VALUES (" +
      "   nextval('mygov_dati_marca_bollo_digitale_id_seq') " +
      ", :b.version" +
      ", :b.tipoBollo" +
      ", :b.hashDocumento" +
      ", :b.provinciaResidenza)")
  @GetGeneratedKeys("mygov_dati_marca_bollo_digitale_id")
  Long insert(@BindBean("b") DatiMarcaBolloDigitale b);

  @SqlUpdate(" delete from mygov_dati_marca_bollo_digitale " +
      " where mygov_dati_marca_bollo_digitale_id = :mygovDatiMarcaBolloDigitaleId")
  int remove(Long mygovDatiMarcaBolloDigitaleId);

}
