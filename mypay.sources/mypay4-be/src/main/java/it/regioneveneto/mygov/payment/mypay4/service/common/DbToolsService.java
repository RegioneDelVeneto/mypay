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
package it.regioneveneto.mygov.payment.mypay4.service.common;

import it.regioneveneto.mygov.payment.mypay4.dao.common.DbToolsDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DbToolsService {

  @Autowired
  @Qualifier("paDbToolsDao")
  private DbToolsDao paDbToolsDao;

  @Autowired
  @Qualifier("fespDbToolsDao")
  private DbToolsDao fespDbToolsDao;

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<Map<String, Object>> getPaDbLocks(){
    return paDbToolsDao.getLocksDb();
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
  public List<Map<String, Object>> getFespDbLocks(){
    return fespDbToolsDao.getLocksDb();
  }
}
