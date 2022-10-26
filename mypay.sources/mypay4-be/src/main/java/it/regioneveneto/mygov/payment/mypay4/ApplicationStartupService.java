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
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ApplicationStartupService {

  @Value("${mypay.logging.forcelog.methods:}")
  private List<String> forceLogMethods;

  @Value("${mypay.logging.forcelog.contexts:}")
  private List<String> forceLogContexts;

  @Autowired
  private ConfigurableEnvironment env;

  @Autowired
  private BuildProperties buildProperties;

  @Autowired
  private EnteService enteService;

  @Autowired
  private CacheService cacheService;

  private String[] privateProperties;

  private long applicationReadyTimestamp;

  public long getApplicationReadyTimestamp(){
    return this.applicationReadyTimestamp;
  }

  @EventListener
  @Order(0)
  public void onApplicationEvent(ApplicationReadyEvent event) {
    log.info("execute onApplicationReadyEvent");

    this.applicationReadyTimestamp = System.currentTimeMillis();

    //init ForceLog filter
    log.info("forceLog methods[{}] contexts[{}]",
      forceLogMethods.stream().collect(Collectors.joining(", ")),
      forceLogContexts.stream().collect(Collectors.joining(", ")) );
    if(!forceLogMethods.isEmpty() && !forceLogContexts.isEmpty()) {
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
    nonBlockingOperation( () -> printApplicationProperties(env, buildProperties) );

    //cache flush
    nonBlockingOperation( cacheService::cacheFlush );

  }

  @EventListener
  @Order(1)
  @ConditionalOnWebApplication
  public void onApplicationEventWebapp(ApplicationReadyEvent event) {
    log.info("execute onApplicationReadyEvent for WebApplication");

    //check if any ente has invalid logo (invalid image): in case, delete the invalid logo
    nonBlockingOperation( () -> enteService.checkInvalidLogo(true) );

    //initialize ente cache (to improve first user requests)
    nonBlockingOperation( () -> enteService.getAllEnti() );
    nonBlockingOperation( () -> enteService.getAllEntiSpontanei() );
  }

  private void nonBlockingOperation(Runnable r){
    try{
      r.run();
    }catch(Exception e){
      log.error("error executing non-blocking startup operation, ignoring it", e);
    }
  }


  private void printApplicationProperties(final ConfigurableEnvironment env, final BuildProperties buildProperties){

    if(log.isErrorEnabled()){
      log.error("Build properties");
      log.error("gitHash: {}", buildProperties.get("gitHash"));
      log.error("lastTag: {}", buildProperties.get("lastTag"));
      log.error("version: {}", buildProperties.get("version"));
      log.error("buildTime: {}", buildProperties.get("buildTime"));
    }

    if(log.isErrorEnabled())
      try {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        log.error("configured loggers: \n{}", lc.getLoggerList().stream()
          .filter(logger -> logger.getLevel() != null)
          .map(logger -> logger.getName() + " -> " + logger.getLevel())
          .collect(Collectors.joining("\n")));
      }catch(Exception e){
        log.error("error printing configured loggers", e);
      }

    if(log.isInfoEnabled())
      try {
        String forceLogPropertiesString = env.getProperty("properties.force-log", "");
        List<String> forceLogProperties = Arrays.asList(forceLogPropertiesString.split(","));
        String privatePropertiesString = env.getProperty("properties.hidden", "password,pwd,secret,prv");
        this.privateProperties = privatePropertiesString.toLowerCase().split(",");
        log.info("*** start listing application properties by source");
        for (PropertySource<?> propertySource : env.getPropertySources()) {
          if (propertySource instanceof EnumerablePropertySource) {
            String[] propertyNames = ((EnumerablePropertySource<?>) propertySource).getPropertyNames();
            Arrays.stream(propertyNames).sorted().forEach(propertyName -> this.printProperty("", env, propertySource, propertyName));
          } else {
            log.info("[{}] not enumerable: {}", propertySource.getName(), propertySource.getSource().getClass());
          }
          forceLogProperties.forEach(propertyName ->{
            if(propertySource.containsProperty(propertyName))
              this.printProperty("FORCED", env, propertySource, propertyName);
          });
        }
        log.info("*** end listing application properties by source");
      } catch (Exception e) {
        log.warn("error printing application properties", e);
      }

  }

  private void printProperty(String prefix, ConfigurableEnvironment env, PropertySource<?> propertySource, String propertyName){
    try {
      String resolvedProperty = env.getProperty(propertyName);
      String sourceProperty = String.valueOf(propertySource.getProperty(propertyName));
      if (StringUtils.containsAnyIgnoreCase(propertyName, this.privateProperties)) {
        resolvedProperty = StringUtils.equals(sourceProperty, resolvedProperty) ? "***hidden***" : "***overriden hidden***";
        sourceProperty = "***hidden***";
      }
      if (StringUtils.equals(sourceProperty, resolvedProperty)) {
        log.info("{} [{}] {}={}", prefix, propertySource.getName(), propertyName, resolvedProperty);
      } else {
        log.info("{} [{}] {}={} OVERRIDDEN to {}", prefix, propertySource.getName(), propertyName, sourceProperty, resolvedProperty);
      }
    } catch (Exception e){
      log.warn("error printing application property {} [{}] {}: {}", prefix, propertySource.getName(), propertyName, e.getMessage());
    }
  }

}
