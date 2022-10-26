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

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.GiornaleElapsedDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class GiornaleElapsedService {

  @Value("${fesp.giornaleelapsed.enabled:false}")
  private boolean giornaleElapsedEnabled;

  @Value("${async.giornaleelapsed.corePoolSize:5}")
  String corePoolSize;
  @Value("${async.giornaleelapsed.maxPoolSize:10}")
  String maxPoolSize;
  @Value("${async.giornaleelapsed.queueCapacity:500}")
  String queueCapacity;

  @Autowired
  private GiornaleElapsedDao giornaleElapsedDao;

  @Resource
  @Lazy
  private GiornaleElapsedService self;

  @PostConstruct
  public void logEnabled(){
    log.info("giornaleElapsedEnabled: {}", giornaleElapsedEnabled);
  }

  public void insertGiornaleElapsed(GiornaleElapsedDao.Operation operation, String idDominio, String idAvviso, boolean isFault, long startTime){
    if(giornaleElapsedEnabled) {
      try {
        long elapsed = System.currentTimeMillis() - startTime;
        self._insertGiornaleElapsedImpl(operation, idDominio, idAvviso, isFault, startTime, elapsed);
      } catch(Exception e){
        log.warn("error inserting giornaleElapsed - ignoring the error", e);
      }
    }
  }

  @Async("GiornaleElapsedExecutor")
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
  public void _insertGiornaleElapsedImpl(GiornaleElapsedDao.Operation operation, String idDominio, String idAvviso, boolean isFault, long startTime, long elapsed){
    try {
      log.debug("insertGiornaleElapsed op[{}] idDom[{}] idAvv[{}] fault[{}] elapsed[{}]", operation, idDominio, idAvviso, isFault, elapsed);
      Timestamp startTimestamp = new Timestamp(startTime);
      giornaleElapsedDao.insertGiornaleElapsed(operation, idDominio, idAvviso, isFault, startTimestamp, elapsed);
    } catch(Exception e){
      log.warn("error inserting giornaleElapsed(async) - ignoring the error", e);
    }
  }

  @Bean("GiornaleElapsedExecutor")
  public Executor giornaleElapsedExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Integer.parseInt(corePoolSize));
    executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
    executor.setQueueCapacity(Integer.parseInt(queueCapacity));
    executor.setThreadNamePrefix("MyPay4GiornaleElapsed-");
    executor.initialize();
    return executor;
  }

}
