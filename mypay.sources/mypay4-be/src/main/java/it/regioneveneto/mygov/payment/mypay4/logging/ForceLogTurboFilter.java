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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.thymeleaf.util.StringUtils;

import java.util.function.Predicate;

public class ForceLogTurboFilter extends TurboFilter {

  private static final String FORCE_LOG_MDC_KEY = "_forceLog";
  private static final String FORCE_LOG_MDC_VALUE = "force";
  private static final String SKIP_LOG_MDC_VALUE = "skip";

  private final Predicate<String> forceLogPredicate;

  private static boolean isEnabled = false;

  public static boolean isEnabled(){
    return isEnabled;
  }

  public static void resetForceLog(){
    MDC.remove(SKIP_LOG_MDC_VALUE);
  }

  public static boolean setForceLog(){
    return setForceOrSkipLog(FORCE_LOG_MDC_VALUE);
  }

  public static boolean setSkipLog(){
    return setForceOrSkipLog(SKIP_LOG_MDC_VALUE);
  }

  private static boolean setForceOrSkipLog(String targetValue){
    String currentValue = MDC.get(FORCE_LOG_MDC_KEY);
    if(currentValue == null){
      MDC.put(FORCE_LOG_MDC_KEY, targetValue);
      return true;
    } else {
      return currentValue.equals(targetValue);
    }
  }

  public ForceLogTurboFilter(Predicate<String> forceLogPredicate){
    isEnabled = true;
    this.forceLogPredicate = forceLogPredicate;
  }

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
    String mdcValue = MDC.get(FORCE_LOG_MDC_KEY);
    if(StringUtils.equals(mdcValue, FORCE_LOG_MDC_VALUE)
      && level.isGreaterOrEqual(Level.DEBUG)
      && forceLogPredicate.test(logger.getName())
    )
      return FilterReply.ACCEPT;
    else if(StringUtils.equals(mdcValue, SKIP_LOG_MDC_VALUE)
      && !level.isGreaterOrEqual(Level.WARN)
      && forceLogPredicate.test(logger.getName())
    )
      return FilterReply.DENY;
    else
      return FilterReply.NEUTRAL;
  }
}
