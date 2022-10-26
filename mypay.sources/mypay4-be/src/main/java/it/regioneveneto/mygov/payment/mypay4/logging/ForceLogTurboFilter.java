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

  public final static String FORCE_LOG_MDC_KEY = "_forceLog";
  public final static String FORCE_LOG_MDC_VALUE = "true";

  private Predicate<String> forceLogPredicate;

  private static boolean isEnabled = false;

  public final static boolean isEnabled(){
    return isEnabled;
  }

  public ForceLogTurboFilter(Predicate<String> forceLogPredicate){
    isEnabled = true;
    this.forceLogPredicate = forceLogPredicate;
  }

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
    boolean forceLog = StringUtils.equalsIgnoreCase(MDC.get(FORCE_LOG_MDC_KEY), FORCE_LOG_MDC_VALUE);
    if(forceLog
      && level.isGreaterOrEqual(Level.DEBUG)
      && forceLogPredicate.test(logger.getName())
    )
      return FilterReply.ACCEPT;
    else
      return FilterReply.NEUTRAL;
  }
}
