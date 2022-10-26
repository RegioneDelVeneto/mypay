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
package it.regioneveneto.mygov.payment.mypay4.scheduled.chiedicopiaesito;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name=AbstractApplication.NAME_KEY, havingValue=ChiediCopiaEsitoTaskApplication.NAME)
public class ChiediCopiaEsitoTask {
  @Autowired
  private ChiediCopiaEsitoTaskService chiediCopiaEsitoTaskService;

  @Scheduled(fixedDelayString = "${task.chiediCopiaEsito.fixedDelay}", initialDelayString = "${random.long[4000,7000]}")
  public void chiediCopiaEsitoScheduler(){
    chiediCopiaEsitoTaskService.chiediCopiaEsito();
  }
}
