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

import it.regioneveneto.mygov.payment.mypay4.model.StandardTipoDovuto;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Set;

import static it.regioneveneto.mygov.payment.mypay4.dao.BaseDao.ALL_FIELDS;

public interface StandardTipoDovutoDao {

  @SqlQuery("SELECT " + StandardTipoDovuto.ALIAS + ALL_FIELDS +
      " FROM mygov_standard_tipo_dovuto " + StandardTipoDovuto.ALIAS)
  @RegisterFieldMapper(StandardTipoDovuto.class)
  Set<StandardTipoDovuto> getAll();
}
