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
package it.regioneveneto.mygov.payment.mypay4.scheduled.inviarptattivate;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.NodoInviaRPTService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.RptRtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue=InviaRPTAttivateTaskApplication.NAME)
class InviaRPTAttivateTaskService {

  @Autowired
  private NodoInviaRPTService nodoInviaRPTService;

  @Autowired
  private RptRtService rptRtService;

  private long counter = 0;
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
  public void inviaRPTAttivate(){
    log.info("inviaRPTAttivate start [{}]", ++counter);
    List<RptRt> rptAttivate = rptRtService.getRPTAttivateForInviaRPT();
    if(!rptAttivate.isEmpty()) {
      log.info("RptRt to send: {}", rptAttivate.size());
      rptAttivate.forEach(nodoInviaRPTService::inviaRPTForModello3ForTask);
    }
    log.info("inviaRPTAttivate stop [{}]", counter);
  }
}
