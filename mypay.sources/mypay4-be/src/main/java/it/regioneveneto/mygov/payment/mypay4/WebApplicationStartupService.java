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

import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
@Slf4j
@ConditionalOnWebApplication
public class WebApplicationStartupService {

  @Autowired
  private EnteService enteService;

  @Autowired
  private ConfigurableEnvironment env;

  @EventListener
  @Order(1)
  public void onApplicationEventWebapp(ApplicationReadyEvent event) {

    if(BooleanUtils.toBoolean(env.getProperty("pa.startuptask.skip","false"))){
      log.warn("SKIPPING onApplicationReadyEvent for WebApplication as per configuration property pa.startuptask.skip");
      return;
    }

    log.info("execute onApplicationReadyEvent for WebApplication");

    //print REST API mapping
    nonBlockingOperation( () -> {
      StringBuilder apiList = new StringBuilder();
      ApplicationContext applicationContext = event.getApplicationContext();
      applicationContext.getBean(RequestMappingHandlerMapping.class).getHandlerMethods()
        .forEach((key, value) -> apiList.append("\n").append(key.toString()).append(" : ").append(value));
      log.info("List of published REST API resources mapping: {}", apiList);
    });

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

}
