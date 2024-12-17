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
package it.regioneveneto.mygov.payment.mypay4.extra;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.invoke.MethodHandles;

@Slf4j
public class AbstractStandaloneApplication extends AbstractApplication {

  protected static void execute(String name, String[] args) {
    log.debug("starting main class {}", name);
    SpringApplication application = new SpringApplication(MethodHandles.lookup().lookupClass());
    // disable webapp nature (so a web container is not started)
    application.setWebApplicationType(WebApplicationType.NONE);
    application.setHeadless(true);
    System.setProperty(NAME_KEY, name);
    try(ConfigurableApplicationContext ctx = application.run(args)) {
      log.info("startup of application {} completed, shutting it down", name);
    }
  }
}
