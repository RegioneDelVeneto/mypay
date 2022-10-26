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
package it.regioneveneto.mygov.payment.mypay4.scheduled.invioemailesito;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.service.CarrelloService;
import it.regioneveneto.mygov.payment.mypay4.service.EsitoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name=AbstractApplication.NAME_KEY, havingValue=InvioEmailEsitoTaskApplication.NAME)
public class InvioEmailEsitoTaskService {

  @Autowired
  InvioEmailEsitoTaskService self;

  @Autowired
  CarrelloService carrelloService;

  @Autowired
  EsitoService esitoService;

  private long counter = 0;
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void invioEmailEsito(){
    log.info("invioEmailEsito start [{}]", ++counter);

    List<Carrello> carrelloList = carrelloService.getWithEsitoAndNotMailSent();

    if(!carrelloList.isEmpty()){
      log.info("carrello to process: {}", carrelloList.size());
      carrelloList.forEach(carrello -> {
        try{
          self.invioMail(carrello);
        }catch(Exception e){
          log.debug("error processing carrello with idDominio: {} - IUV: {} - ccp: {}",
              carrello.getCodRpSilinviarpIdDominio(),
              carrello.getCodRpDatiVersIdUnivocoVersamento(),
              carrello.getCodRpDatiVersCodiceContestoPagamento(),
              e);
        }
      });
    }

    log.info("chiediCopiaEsito end [{}]", counter);
  }

  @Transactional(propagation = Propagation.NESTED)
  public void invioMail(Carrello carrello){
    log.debug("Start processing carrello with idDominio: {} - IUV: {} - ccp: {}",
        carrello.getCodRpSilinviarpIdDominio(),
        carrello.getCodRpDatiVersIdUnivocoVersamento(),
        carrello.getCodRpDatiVersCodiceContestoPagamento());
    esitoService.sendEmailEsito(carrello);
  }

}