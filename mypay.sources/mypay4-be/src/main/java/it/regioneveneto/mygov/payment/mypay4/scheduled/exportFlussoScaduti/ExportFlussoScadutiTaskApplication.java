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
package it.regioneveneto.mygov.payment.mypay4.scheduled.exportFlussoScaduti;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.scheduled.AbstractTaskApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.invoke.MethodHandles;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
@ComponentScan(basePackages = "it.regioneveneto.mygov.payment.mypay4")
@EnableCaching
@EnableTransactionManagement
@EnableAsync
@EnableRetry
@EnableScheduling
@Slf4j
@ConditionalOnProperty(name=AbstractApplication.NAME_KEY, havingValue= ExportFlussoScadutiTaskApplication.NAME)
public class ExportFlussoScadutiTaskApplication extends AbstractTaskApplication {
  final static public String NAME = "ExportFlussoScadutiTaskApplication";
  public static void main(String[] args) {
    log.debug("starting main class {}", NAME);
    SpringApplication application = new SpringApplication(MethodHandles.lookup().lookupClass());
    // disable webapp nature (so a web container is not started)
    application.setWebApplicationType(WebApplicationType.NONE);
    application.setHeadless(true);
    System.setProperty(NAME_KEY, NAME);
    application.run(args);
    log.info("started {}", NAME);
  }
}
