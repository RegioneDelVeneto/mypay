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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.LockDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("fespLockService")
public class LockService {

  @Autowired
  private LockDao lockDao;

  public enum Types {
    CHIEDI_FLUSSO_RENDICONTAZIONE("CFR");

    private final String type;
    Types(String type){
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }

  /*
   * Try and acquire lock on entity of type "type" (ex.: "PROCESS_FLUSSO_RENDICONTAZIONE") and key "key" (ex.: "R_VENETO"),
   * where entity is a generic concept that has meaning in the business logic of a particular process.
   *
   * Lock is managed by the table mygov_lock, and the select..for update skip locked; the method returns:
   *  true -> the lock is granted;
   *  false -> the lock is NOT granted (i.e. some other transaction has a lock on it)
   */
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.MANDATORY)
  public boolean lock(Types type, String key, Optional<Integer> onlyIfOlderThanMinutes) {
    //insert in case type/key not present
    int inserted = lockDao.insert(type.getType(), key);
    boolean exists = inserted==1 || onlyIfOlderThanMinutes.isEmpty() ||
        onlyIfOlderThanMinutes.flatMap(m -> lockDao.get(type.getType(), key, m)).isPresent();
    boolean acquired = exists && lockDao.acquireLock(type.getType(), key).isPresent();
    if(acquired)
      lockDao.updateDt(type.getType(), key);
    return acquired;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.MANDATORY)
  public boolean lock(Types type, String key) {
    return lock(type, key, Optional.empty());
  }

}
