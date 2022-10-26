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

import it.regioneveneto.mygov.payment.mypay4.util.LogHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.client.BaseClient;
import it.regioneveneto.mygov.payment.mypay4.ws.server.BaseEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This aspect will log calls to method, displaying:
 * - method signature;
 * - execution time;
 * - (optional) input parameters value;
 * - (optional) return value.
 * By default is configured to log all RestController methods.
 * It's possible to enable the log adding the annotation {@link LogExecution} to a method.
 * The logging will occur at INFO log level; the logger will have the class name containing the logged method.
 * The logging of input params and return value will only occur if the relevant logger has DEBUG log level enabled (even
 * though the logg will occur at INFO log level).
 * It's possible to customize the behaviour of logging using the parameters of {@link LogExecution} annotation.
 */
@Aspect
@Component
@ConditionalOnProperty(name="method-execution-logging.enabled", havingValue="")
public class LogExecutionAspect {

  private static final ThreadLocal<String> customMessageHolder = new ThreadLocal<>();

  @Value("${method-execution-logging.enabled}")
  private String enabledValue;

  @Value("${mypay.logging.forcelog.methods:}")
  private List<String> forceLogMethods;

  private Boolean methodOnly = null;
  private boolean isMethodOnly(){
    if(methodOnly == null)
      methodOnly = "method".equalsIgnoreCase(enabledValue);
    return methodOnly;
  }

  final private static LogExecution.ParamMode LogExecution_enabled, LogExecution_params, LogExecution_returns;
  static {
    try {
      LogExecution_enabled =(LogExecution.ParamMode)LogExecution.class.getMethod("enabled").getDefaultValue();
      LogExecution_params = (LogExecution.ParamMode)LogExecution.class.getMethod("params").getDefaultValue();
      LogExecution_returns = (LogExecution.ParamMode)LogExecution.class.getMethod("returns").getDefaultValue();
    } catch(Exception e){ throw new RuntimeException(e); }
  }

  //by dafult log all Rest controller are included in the scope of this log
  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void restControllerPointcut() {
    // Method is empty as this is just a Pointcut, the implementations are in the advices.
  }


  //by dafult log all Soap WS endpoint are included in the scope of this log
  @Pointcut("within(it.regioneveneto.mygov.payment.*.ws.server.BaseEndpoint+)")
  public void soapEndpointPointcut() {
    // Method is empty as this is just a Pointcut, the implementations are in the advices.
  }

  //by dafult log all Soap WS client are included in the scope of this log
  @Pointcut("within(it.regioneveneto.mygov.payment.*.ws.client.BaseClient+)")
  public void soapClientPointcut() {
    // Method is empty as this is just a Pointcut, the implementations are in the advices.
  }

  public static void setCustomMessage(String customMessage){
    if(StringUtils.isNotBlank(customMessage))
      customMessageHolder.set(customMessage);
    else
      customMessageHolder.remove();
  }


  @Around("restControllerPointcut() || soapEndpointPointcut() || soapClientPointcut() ||@annotation(LogExecution)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
    Method loggedMethod = null;
    boolean hasSetForceLog = false;
    if(joinPoint.getSignature() instanceof MethodSignature) {
      loggedMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
      if(ForceLogTurboFilter.isEnabled()) {
        String methodName = LogHelper.methodToFullNameString(loggedMethod);
        if (forceLogMethods.contains(methodName)) {
          if(MDC.get(ForceLogTurboFilter.FORCE_LOG_MDC_KEY)==null) {
            MDC.put(ForceLogTurboFilter.FORCE_LOG_MDC_KEY, ForceLogTurboFilter.FORCE_LOG_MDC_VALUE);
            hasSetForceLog = true;
          }
        }
      }
    }

    long start = System.currentTimeMillis();
    Object proceed;
    try{
      proceed = joinPoint.proceed();
    } finally {
      if(ForceLogTurboFilter.isEnabled() && hasSetForceLog) {
        MDC.remove(ForceLogTurboFilter.FORCE_LOG_MDC_KEY);
      }
    }

    long executionTime = System.currentTimeMillis() - start;

    Marker marker = LogMarker.METHOD.marker;
    String customMessage = customMessageHolder.get();
    customMessageHolder.remove();
    if(logger.isInfoEnabled()) {
      LogExecution.ParamMode
          enabled = LogExecution_enabled,
          params = isMethodOnly() ? LogExecution.ParamMode.OFF : LogExecution_params,
          returns = isMethodOnly() ? LogExecution.ParamMode.OFF : LogExecution_returns;
      if(joinPoint.getSignature() instanceof MethodSignature){
        LogExecution annot = loggedMethod.getAnnotation(LogExecution.class);
        if(annot!=null) {
          enabled = annot.enabled();
          params = isMethodOnly() ? LogExecution.ParamMode.OFF : annot.params();
          returns = isMethodOnly() ? LogExecution.ParamMode.OFF : annot.returns();
        }
        if(!enabled.equals(LogExecution.ParamMode.OFF)){
          if(loggedMethod.getDeclaringClass().getAnnotation(RestController.class)!=null)
            marker= LogMarker.REST.marker;
          else if(BaseEndpoint.class.isAssignableFrom(loggedMethod.getDeclaringClass()))
            marker= LogMarker.SOAP_SERVER.marker;
          else if(BaseClient.class.isAssignableFrom(loggedMethod.getDeclaringClass()))
            marker= LogMarker.SOAP_CLIENT.marker;
        }
      }
      if(enabled.equals(LogExecution.ParamMode.OFF))
        return proceed;
      String paramString = "";
      if(!params.equals(LogExecution.ParamMode.OFF)
          && (logger.isDebugEnabled(marker) || params.equals(LogExecution.ParamMode.ON))
          && joinPoint.getArgs().length > 0)
        paramString = " - params: " + Arrays.stream(joinPoint.getArgs()).map(e -> e!=null?e.toString():null).collect(Collectors.joining(" ; "));
      String returnsString = "";
      if(!returns.equals(LogExecution.ParamMode.OFF)
          && (logger.isDebugEnabled(marker) || returns.equals(LogExecution.ParamMode.ON)))
        returnsString = " - return value: " + proceed;
      if(StringUtils.isNotBlank(customMessage))
        customMessage = " - custom message: " + customMessage;
      else
        customMessage = "";
      String loggedMethodString = loggedMethod!=null ? LogHelper.methodToString(loggedMethod) : joinPoint.getSignature().toString();
      logger.info(marker, "method=" + loggedMethodString + " - elapsed_ms=" + executionTime + paramString + returnsString + customMessage);
    }
    return proceed;
  }
}
