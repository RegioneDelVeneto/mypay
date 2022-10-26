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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.CarrelloRpDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.CarrelloRp;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRPRisposta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class CarrelloRpService {

  @Autowired
  CarrelloRpDao carrelloRpDao;

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public CarrelloRp insert(String idSession, String dominioChiamante){
    Date now = new Date();
    CarrelloRp carrelloRp = CarrelloRp.builder()
        .dtCreazione(now)
        .dtUltimaModifica(now)
        .idSessionCarrello(idSession)
        .codiceFiscaleEnte(dominioChiamante).build();

    long newId = carrelloRpDao.insert(carrelloRp);
    return carrelloRp.toBuilder().mygovCarrelloRpId(newId).build();
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateRispostaRpById(CarrelloRp carrelloRp, NodoSILInviaCarrelloRPRisposta response, String idSession) {
    carrelloRp.toBuilder()
        .dtUltimaModifica(new Date())
        .deRpSilinviacarrellorpEsito(response.getEsito())
        .codRpSilinviacarrellorpUrl(response.getUrl())
        .idSessionCarrello(idSession)
        .build();
    var fb = response.getFault();
    if (fb != null) carrelloRp.toBuilder()
        .codRpSilinviacarrellorpFaultCode(fb.getFaultCode())
        .deRpSilinviacarrellorpFaultString(fb.getFaultString())
        .codRpSilinviacarrellorpId(fb.getId())
        .codRpSilinviacarrellorpOriginalFaultCode(fb.getOriginalFaultCode())
        .deRpSilinviacarrellorpOriginalFaultString(fb.getOriginalFaultString())
        .deRpSilinviacarrellorpOriginalFaultDescription(Utilities.getTruncatedAt(1024).apply(fb.getOriginalDescription()))
        .codRpSilinviacarrellorpSerial(fb.getSerial())
        .build();
    int updated = carrelloRpDao.update(carrelloRp);
    if(updated!=1)
      throw new MyPayException("invalid number of rows updated:"+updated+" for mygovCarrelloRpId:"+carrelloRp.getMygovCarrelloRpId());
  }

  public Optional<CarrelloRp> getByIdSession(String idSession){
    return carrelloRpDao.getByIdSession(idSession);
  }

  public Optional<CarrelloRp> getById(Long id){
    return carrelloRpDao.getById(id);
  }
}
