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
package it.regioneveneto.mygov.payment.mypay4.logging;


import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
@Slf4j
@ConditionalOnProperty(name="datasource-pool-logging.enabled", havingValue="true")
public class LogConnectionPoolAspect {

  @Autowired
  private HikariDataSource ds;


  @Before("execution(* it.regioneveneto.mygov.payment.*.dao.*.*(..))")
  public void logBeforeConnection(JoinPoint jp) {
    log.info(LogMarker.DB_CONNECTION_POOL.marker, jp.getSignature().toShortString()+" BEFORE "+getDsInfo(this.ds));
  }


  @After("execution(* it.regioneveneto.mygov.payment.*.dao.*.*(..)) ")
  public void logAfterConnection(JoinPoint jp) {
    log.info(LogMarker.DB_CONNECTION_POOL.marker, jp.getSignature().toShortString()+" AFTER "+getDsInfo(this.ds));
  }

  private String getDsInfo(HikariDataSource ds){
    HikariPoolMXBean mxb = ds.getHikariPoolMXBean();
    if(mxb==null)
      return "warning: connection pool MXBean null";
    String jdbcUrl = ds.getJdbcUrl();
    if(jdbcUrl.contains("/"))
      jdbcUrl = jdbcUrl.substring(jdbcUrl.lastIndexOf("/")+1);
    return "[" + jdbcUrl + "]" +
        " active: " + mxb.getActiveConnections() +
        " idle: " + mxb.getIdleConnections() +
        " total: " + mxb.getTotalConnections() +
        " thread-awaiting: " + mxb.getThreadsAwaitingConnection();
  }
}
