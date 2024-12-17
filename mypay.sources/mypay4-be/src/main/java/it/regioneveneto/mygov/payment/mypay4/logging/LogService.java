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
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogService {

  public static final String LOG_APPENDER_PRINT_ENV = "printEnvAppender";

  @Value("${mypay4.logging.printEnvAppender.file:}")
  private String printEnvAppenderFile;

  @Autowired
  private ConfigurableEnvironment env;

  @Autowired
  private BuildProperties buildProperties;

  private String[] privateProperties;

  public void printApplicationProperties(){
    printApplicationProperties(null);
  }

  private boolean equalLevel(Level level1, Level level2){
    return StringUtils.equals(
        Optional.ofNullable(level1).map(Level::toString).orElse("null"),
        Optional.ofNullable(level2).map(Level::toString).orElse("null") );
  }

  public String getLogDetails(String loggerName){
    StringBuilder resp = new StringBuilder();
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger logger = lc.getLogger(loggerName);
    resp.append("logger: ").append(loggerName).append("<br>\n");
    resp.append("level: ").append(logger.getLevel()).append("<br>\n");
    resp.append("effectiveLevel: ").append(logger.getEffectiveLevel()).append("<br>\n");
    resp.append("additive: ").append(logger.isAdditive()).append("<br>\n");
    for (Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders(); it.hasNext(); ) {
      Appender<?> appender = it.next();
      if (FileAppender.class.isAssignableFrom(appender.getClass()))
        resp.append("appender: "+appender.getName() + "-" + appender.getClass().getName() + "-" + ((FileAppender<?>) appender).getFile() + "<br>\n");
      else
        resp.append("appender: "+appender.getName() + "-" + appender.getClass().getName() + "<br>\n");
    }
    return resp.toString();
  }

  public String setLevel(String loggerName, String level){
    StringBuilder resp = new StringBuilder();
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger logger = lc.getLogger(loggerName);
    resp.append("logger: ").append(loggerName).append("<br>\n");
    resp.append("BEFORE: level: ").append(logger.getLevel()).append(" - effectiveLevel: ").append(logger.getEffectiveLevel()).append("<br>\n");
    logger.setLevel(Level.toLevel(level));
    resp.append("AFTER: level: ").append(logger.getLevel()).append(" - effectiveLevel: ").append(logger.getEffectiveLevel()).append("<br>\n");
    return resp.toString();
  }

  public void printApplicationProperties(List<String> loggersForDetails){

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger printEnvLogger = lc.getLogger("printEnvironment");

    if(printEnvLogger.getAppender(LOG_APPENDER_PRINT_ENV)==null && StringUtils.isNotBlank(printEnvAppenderFile)) {
      log.info("creating appender [{}] file[{}]", LOG_APPENDER_PRINT_ENV, printEnvAppenderFile);
      PatternLayoutEncoder ple = new PatternLayoutEncoder();
      ple.setPattern("[%d{yyyy-MM-dd HH:mm:ss.SSS}][%X{user:--}][%p] %msg%n");
      ple.setContext(lc);
      ple.start();
      FileAppender<ILoggingEvent> printEnvAppender = new FileAppender<>();
      printEnvAppender.setName(LOG_APPENDER_PRINT_ENV);
      printEnvAppender.setFile(printEnvAppenderFile);
      printEnvAppender.setEncoder(ple);
      printEnvAppender.setContext(lc);
      printEnvAppender.start();
      printEnvLogger.setLevel(Level.DEBUG);
      printEnvLogger.addAppender(printEnvAppender);
    }

    if(printEnvLogger.isErrorEnabled()){
      printEnvLogger.error("Build properties");
      printEnvLogger.error("gitHash: {}", buildProperties.get("gitHash"));
      printEnvLogger.error("lastTag: {}", buildProperties.get("lastTag"));
      printEnvLogger.error("version: {}", buildProperties.get("version"));
      printEnvLogger.error("buildTime: {}", buildProperties.get("buildTime"));
    }

    if(printEnvLogger.isErrorEnabled())
      try {
        loggersForDetails = ObjectUtils.firstNonNull(loggersForDetails, Collections.emptyList());
        boolean showNullLevelLoggers = loggersForDetails.contains("ROOT");
        printEnvLogger.error("configured loggers: \n{}", lc.getLoggerList().stream()
            .filter(logger -> showNullLevelLoggers || logger.getLevel() != null)
            .map(logger -> logger.getName() + " -> " + logger.getLevel() + (equalLevel(logger.getLevel(), logger.getEffectiveLevel())  ? "" : (" (" + logger.getEffectiveLevel()+")")) )
            .collect(Collectors.joining("\n")));
        Set<String> appenders = new HashSet<>();
        lc.getLoggerList().forEach(logger -> {
          for (Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders(); it.hasNext(); ) {
            Appender<?> appender = it.next();
            if (FileAppender.class.isAssignableFrom(appender.getClass()))
              appenders.add(appender.getName() + "-" + appender.getClass().getName() + "-" + ((FileAppender<?>) appender).getFile());
            else
              appenders.add(appender.getName() + "-" + appender.getClass().getName());
          }
        });
        printEnvLogger.error("force log methods: {}", env.getProperty("mypay.logging.forcelog.methods",""));
        printEnvLogger.error("skip log methods: {}", env.getProperty("mypay.logging.skiplog.methods",""));
        printEnvLogger.error("configured appenders: \n{}", String.join("\n", appenders));
        loggersForDetails.forEach(loggerName -> {
          Logger logger = lc.getLogger(loggerName);
          StringBuilder appenderString = new StringBuilder();
          logger.iteratorForAppenders().forEachRemaining(appender -> appenderString.append(appender.getName()).append(";"));
          printEnvLogger.error("logger[{}] level[{}] effectiveLevel[{}] additive[{}] trace[{}] debug[{}] info[{}], warn[{}] error[{}] appenders[{}]",
              loggerName, logger.getLevel(), logger.getEffectiveLevel(), logger.isAdditive(),
              logger.isTraceEnabled(), logger.isDebugEnabled(), logger.isInfoEnabled(), logger.isWarnEnabled(), logger.isErrorEnabled(),
              appenderString);
        });
      }catch(Exception e){
        printEnvLogger.error("error printing configured loggers", e);
      }

    if(printEnvLogger.isErrorEnabled())
      try {
        String forceLogPropertiesString = env.getProperty("properties.force-log", "");
        List<String> forceLogProperties = Arrays.asList(forceLogPropertiesString.split(","));
        String privatePropertiesString = env.getProperty("properties.hidden", "password,pwd,secret,prv,apikey,api_key");
        this.privateProperties = privatePropertiesString.toLowerCase().split(",");
        printEnvLogger.error("*** start listing application properties by source");
        for (PropertySource<?> propertySource : env.getPropertySources()) {
          if (propertySource instanceof EnumerablePropertySource) {
            String[] propertyNames = ((EnumerablePropertySource<?>) propertySource).getPropertyNames();
            Arrays.stream(propertyNames).sorted().forEach(propertyName -> this.printProperty(printEnvLogger, "", env, propertySource, propertyName));
          } else {
            printEnvLogger.error("[{}] not enumerable: {}", propertySource.getName(), propertySource.getSource().getClass());
          }
          forceLogProperties.forEach(propertyName ->{
            if(propertySource.containsProperty(propertyName))
              this.printProperty(printEnvLogger, "FORCED", env, propertySource, propertyName);
          });
        }
        printEnvLogger.error("*** end listing application properties by source");
      } catch (Exception e) {
        printEnvLogger.error("error printing application properties", e);
      }

  }

  private void printProperty(Logger logger, String prefix, ConfigurableEnvironment env, PropertySource<?> propertySource, String propertyName){
    try {
      String resolvedProperty = env.getProperty(propertyName);
      String sourceProperty = String.valueOf(propertySource.getProperty(propertyName));
      if (StringUtils.containsAnyIgnoreCase(propertyName, this.privateProperties)) {
        resolvedProperty = StringUtils.equals(sourceProperty, resolvedProperty) ? "***hidden***" : "***overriden hidden***";
        sourceProperty = "***hidden***";
      }
      if (StringUtils.equals(sourceProperty, resolvedProperty)) {
        logger.info("{} [{}] {}={}", prefix, propertySource.getName(), propertyName, resolvedProperty);
      } else {
        logger.info("{} [{}] {}={} OVERRIDDEN to {}", prefix, propertySource.getName(), propertyName, sourceProperty, resolvedProperty);
      }
    } catch (Exception e){
      logger.warn("error printing application property {} [{}] {}: {}", prefix, propertySource.getName(), propertyName, e.getMessage());
    }
  }
}
