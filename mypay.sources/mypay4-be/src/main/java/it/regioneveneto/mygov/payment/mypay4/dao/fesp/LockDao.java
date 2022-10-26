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
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface LockDao extends BaseDao {

  @SqlQuery(
      "    select 1 " +
          "  from mygov_lock " +
          " where type = :type and key = :key " +
          "   and (:ifOlderThanMinutes is null or dt_modified < now() - make_interval(0,0,0,0,0,:ifOlderThanMinutes,0)) "
  )
  Optional<Integer> get(String type, String key, Integer ifOlderThanMinutes);

  @SqlQuery(
      "    select 1 " +
          "  from mygov_lock " +
          " where type = :type and key = :key " +
          "   for update skip locked"
  )
  Optional<Integer> acquireLock(String type, String key);

  @SqlUpdate(
      "insert into mygov_lock(type, key) values (:type, :key) " +
          " on conflict do nothing")
  int insert(String type, String key);

  @SqlUpdate(
      "update mygov_lock set dt_modified = current_timestamp " +
          " where type = :type and key = :key")
  int updateDt(String type, String key);
}
