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
package it.regioneveneto.mygov.payment.mypay4.dao.catalog;

import it.regioneveneto.mygov.payment.mypay4.dto.catalog.ColumnInfo;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface CatalogDao {

  @SqlQuery(
      "select" +
          "    table_schema," +
          "    table_name," +
          "    column_name," +
          "    data_type," +
          "    character_maximum_length" +
          "  from information_schema.columns" +
          " where data_type in ('character varying', 'character') " +
          "   and table_schema != 'information_schema'" +
          "   and character_maximum_length is not null"
  )
  @RegisterFieldMapper(ColumnInfo.class)
  public List<ColumnInfo> loadMaxLengthData();
}
