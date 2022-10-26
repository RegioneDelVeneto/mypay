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
package it.regioneveneto.mygov.payment.mypay4.dao.common;

import it.regioneveneto.mygov.payment.mypay4.dao.BaseDao;
import org.jdbi.v3.core.mapper.MapMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;
import java.util.Map;

public interface DbToolsDao extends BaseDao {

  @SqlQuery(
    "SELECT bl.pid     AS blocked_pid, " +
      "     a.usename  AS blocked_user, " +
      "     kl.pid     AS blocking_pid, " +
      "     ka.usename AS blocking_user, " +
      "     a.query    AS blocked_statement, " +
      "     ka.query as blocking_statement, " +
      "     host(a.client_addr) as blocked_address, " +
      "     host(ka.client_addr) as blocking_address, " +
      "     a.wait_event_type as blocked_wait_event_type, " +
      "     ka.wait_event_type as blocking_wait_event_type, " +
      "     a.backend_start as blocked_backend_start, " +
      "     ka.backend_start as blocking_backend_start, " +
      "     a.query_start as blocked_querty_start, " +
      "     ka.query_start as blocking_querty_start, " +
      "     a.datname as dbname " +
    "FROM  pg_catalog.pg_locks         bl " +
    "JOIN pg_catalog.pg_stat_activity a  ON a.pid = bl.pid " +
    "JOIN pg_catalog.pg_locks         kl ON kl.transactionid = bl.transactionid AND kl.pid != bl.pid " +
    "JOIN pg_catalog.pg_stat_activity ka ON ka.pid = kl.pid " +
    "WHERE NOT bl.granted")
  @RegisterRowMapper(MapMapper.class)
  List<Map<String, Object>> getLocksDb();
}
