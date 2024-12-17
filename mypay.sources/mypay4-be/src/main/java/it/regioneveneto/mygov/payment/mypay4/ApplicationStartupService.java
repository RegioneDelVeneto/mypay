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
package it.regioneveneto.mygov.payment.mypay4;

import ch.qos.logback.classic.LoggerContext;
import it.regioneveneto.mygov.payment.mypay4.logging.ForceLogTurboFilter;
import it.regioneveneto.mygov.payment.mypay4.logging.LogService;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ApplicationStartupService {

  @Value("${mypay.logging.forcelog.methods:}")
  private List<String> forceLogMethods;

  @Value("${mypay.logging.skiplog.methods:}")
  private List<String> skipLogMethods;

  @Value("${mypay.logging.forcelog.contexts:}")
  private List<String> forceLogContexts;

  @Autowired
  private LogService logService;

  @Autowired
  private CacheService cacheService;

  @Getter
  private long applicationReadyTimestamp;

  @EventListener
  @Order(0)
  public void onApplicationEvent(ApplicationReadyEvent event) {
    log.info("execute onApplicationReadyEvent");

    this.applicationReadyTimestamp = System.currentTimeMillis();

    //init ForceLog filter
    log.info("forceLog methodsForce[{}] methodsSkip[{}] contexts[{}]",
        String.join(", ", forceLogMethods),
        String.join(", ", skipLogMethods),
        String.join(", ", forceLogContexts));
    if((!forceLogMethods.isEmpty() || !skipLogMethods.isEmpty()) && !forceLogContexts.isEmpty()) {
      log.info("enabling ForceLogTurboFilter");
      final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      lc.addTurboFilter(new ForceLogTurboFilter(loggerName -> {
        for (String context : forceLogContexts)
          if (StringUtils.startsWith(loggerName, context))
            return true;
        return false;
      }));
    }

    //print application properties
    nonBlockingOperation( logService::printApplicationProperties );

    //cache flush
    nonBlockingOperation( cacheService::cacheFlush );

  }

  private void nonBlockingOperation(Runnable r){
    try{
      r.run();
    }catch(Exception e){
      log.error("error executing non-blocking startup operation, ignoring it", e);
    }
  }

}
