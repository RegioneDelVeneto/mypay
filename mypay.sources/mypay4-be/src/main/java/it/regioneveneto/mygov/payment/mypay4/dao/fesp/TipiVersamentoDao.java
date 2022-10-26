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
import it.regioneveneto.mygov.payment.mypay4.model.fesp.TipiVersamento;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface TipiVersamentoDao extends BaseDao {

  @SqlQuery(
      "    select " + TipiVersamento.ALIAS + ALL_FIELDS +
          "  from mygov_tipiversamento " + TipiVersamento.ALIAS +
          " where "+TipiVersamento.ALIAS+".tipo_versamento  = :tipoVersamento " )
  @RegisterFieldMapper(TipiVersamento.class)
  TipiVersamento getByTipoVersamento(final String tipoVersamento);
}
