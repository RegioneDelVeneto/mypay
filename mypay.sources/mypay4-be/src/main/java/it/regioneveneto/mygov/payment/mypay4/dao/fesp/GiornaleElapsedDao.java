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
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.Timestamp;

public interface GiornaleElapsedDao extends BaseDao {

  enum Operation {
    paaVerificaRPT, paaAttivaRPT, paaInviaRT
  }

  @SqlUpdate(
    "insert into mygov_giornale_elapsed (" +
    " id" +
    ",operation " +
    ",id_dominio " +
    ",id_avviso " +
    ",fault " +
    ",start_time " +
    ",elapsed ) values ( " +
    " nextval('mygov_giornale_elapsed_seq') " +
    ",:operation " +
    ",:idDominio " +
    ",:idAvviso " +
    ",:isFault " +
    ",:startTime " +
    ",:elapsedMs )"
  )
  void insertGiornaleElapsed(Operation operation, String idDominio, String idAvviso, boolean isFault, Timestamp startTime, long elapsedMs);
}
