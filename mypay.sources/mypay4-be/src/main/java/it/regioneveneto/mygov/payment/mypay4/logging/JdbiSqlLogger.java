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

import it.regioneveneto.mygov.payment.mypay4.config.dbreplica.TransactionRoutingDataSource;
import it.regioneveneto.mygov.payment.mypay4.util.LogHelper;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class JdbiSqlLogger implements SqlLogger {

  private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

  private static final String LOG_TEMPLATE = "method={} elapsed_ms={} {} - ro={} - {}{}";

  private boolean isShort = false;
  private int slowQueryTresholdMs = 0;

  public void setBehaviour(String behaviour){
    this.isShort = "short".equalsIgnoreCase(behaviour);
  }

  public void setSlowQueryTresholdMs(int slowQueryTresholdMs) {
    this.slowQueryTresholdMs = slowQueryTresholdMs;
  }

  @Override
  public void logException(StatementContext context, SQLException ex) {
    doLog(context,ex);
  }

  @Override
  public void logAfterExecution(StatementContext context) {
    doLog(context, null);
  }

  private void doLog(StatementContext context, SQLException ex){
    long executionTime = context.getElapsedTime(ChronoUnit.MILLIS);
    Logger sqlLogger;
    Supplier<String> methodLogSupplier;
    if(context.getExtensionMethod()!=null){
      Method sqlMethod = context.getExtensionMethod().getMethod();
      sqlLogger = LoggerFactory.getLogger(sqlMethod.getDeclaringClass());
      methodLogSupplier = () -> ((isShort && ex!=null) ? LogHelper.methodToShortString(sqlMethod) : LogHelper.methodToLongString(sqlMethod));
    } else {
      Optional<StackTraceElement> sqlElement = Arrays.stream(Thread.currentThread().getStackTrace())
          .filter(elem -> elem.getClassName().startsWith("it.regioneveneto.mygov.payment")
              && !elem.getClassName().equals(JdbiSqlLogger.class.getName()))
          .findFirst();
      if(sqlElement.isPresent()){
        final StackTraceElement elem = sqlElement.get();
        sqlLogger = LoggerFactory.getLogger(elem.getClassName());
        methodLogSupplier = () -> elem.getMethodName()+"(row "+elem.getLineNumber()+")";
      } else {
        sqlLogger = LoggerFactory.getLogger(JdbiSqlLogger.class);
        methodLogSupplier = () -> "<unknown>";
      }
    }

    boolean isSlowQuery = slowQueryTresholdMs>0 && executionTime > slowQueryTresholdMs;
    boolean hasToLog = isSlowQuery && sqlLogger.isInfoEnabled(LogMarker.DB_STATEMENT.marker)
      || sqlLogger.isDebugEnabled(LogMarker.DB_STATEMENT.marker);

    if (hasToLog) {
      String slowString;
      if(isSlowQuery)
        slowString = "[SLOW] - started at "+timeFormatter.format(context.getExecutionMoment());
      else
        slowString = "";

      String attributes = "";
      if (!context.getBinding().isEmpty())
        attributes = " - " + context.getBinding().toString();

      String readOnly = ""+TransactionSynchronizationManager.isCurrentTransactionReadOnly();
      if(TransactionRoutingDataSource.isCurrentlyReadonly()!=TransactionSynchronizationManager.isCurrentTransactionReadOnly())
        readOnly += "/"+TransactionRoutingDataSource.isCurrentlyReadonly();

      if(ex!=null)
        sqlLogger.error(LogMarker.DB_STATEMENT.marker, LOG_TEMPLATE
        , methodLogSupplier.get()
        , executionTime
        , slowString
        , readOnly
        , context.getRenderedSql()
        , attributes
        , ex);
      else if(isSlowQuery)
        sqlLogger.warn(LogMarker.DB_STATEMENT.marker, LOG_TEMPLATE
          , methodLogSupplier.get()
          , executionTime
          , slowString
          , readOnly
          , context.getRenderedSql()
          , attributes);
      else
        sqlLogger.debug(LogMarker.DB_STATEMENT.marker, LOG_TEMPLATE
          , methodLogSupplier.get()
          , executionTime
          , slowString
          , readOnly
          , context.getRenderedSql()
          , attributes);
    }
  }

}
