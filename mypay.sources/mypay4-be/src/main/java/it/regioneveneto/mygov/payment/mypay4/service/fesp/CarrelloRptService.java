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

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.ListaErroriRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaCarrelloRPTRisposta;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.CarrelloRptDao;
import it.regioneveneto.mygov.payment.mypay4.dto.common.Psp;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.CarrelloRp;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.CarrelloRpt;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class CarrelloRptService {

  @Autowired
  CarrelloRptDao carrelloRptDao;

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public CarrelloRpt insert(CarrelloRp carrelloRp, Psp psp){
    Date now = new Date();
    CarrelloRpt carrelloRpt = CarrelloRpt.builder()
        .dtCreazione(now)
        .dtUltimaModifica(now)
        .mygovCarrelloRpId(carrelloRp)
        .codRptInviacarrellorptIdCarrello(carrelloRp.getIdSessionCarrello())
        .codRptInviacarrellorptIdIntermediarioPa(psp.getIdentificativoIntermediarioPA())
        .codRptInviacarrellorptIdStazioneIntermediarioPa(psp.getIdentificativoStazioneIntermediarioPA())
        .deRptInviacarrellorptPassword(psp.getPassword())
        .codRptInviacarrellorptIdCanale(psp.getIdentificativoCanale())
        .codRptInviacarrellorptIdIntermediarioPsp(psp.getIdentificativoIntermediarioPSP())
        .codRptInviacarrellorptIdPsp(psp.getIdentificativoPSP())
        .build();

    long newId = carrelloRptDao.insert(carrelloRpt);
    return carrelloRpt.toBuilder().mygovCarrelloRptId(newId).build();
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateByKey(NodoInviaCarrelloRPTRisposta responseNodo, CarrelloRpt carrelloRpt)
      throws UnsupportedEncodingException, MalformedURLException {
    String idSessionSPC = null;
    if (StringUtils.isNotBlank(responseNodo.getUrl())) {
      Map<String, String> parametersMap = Utilities.splitQuery(new URL(responseNodo.getUrl()));
      idSessionSPC = parametersMap.get("idSession");
    }
    carrelloRpt.setDtUltimaModifica(new Date());
    carrelloRpt.setDeRptInviacarrellorptEsitoComplessivoOperazione(responseNodo.getEsitoComplessivoOperazione());
    carrelloRpt.setCodRptInviacarrellorptUrl(responseNodo.getUrl());
    ListaErroriRPT listaErroriRPT = responseNodo.getListaErroriRPT();
    if (listaErroriRPT != null && listaErroriRPT.getFaults() != null) {
      FaultBean f = listaErroriRPT.getFaults().get(0);
      carrelloRpt = carrelloRpt.toBuilder()
          .codRptInviacarrellorptFaultCode(f.getFaultCode())
          .codRptInviacarrellorptFaultString(f.getFaultString())
          .codRptInviacarrellorptId(f.getId())
          .numRptInviacarrellorptSerial(f.getSerial())
          .codRptSilinviacarrellorptOriginalFaultCode(f.getOriginalFaultCode())
          .deRptSilinviacarrellorptOriginalFaultString(f.getOriginalFaultString())
          .deRptSilinviacarrellorptOriginalFaultDescription(Utilities.getTruncatedAt(1024).apply(f.getOriginalDescription()))
          .deRptInviacarrellorptDescription(Utilities.getTruncatedAt(1024).apply(f.getDescription()))
          .build();

    }
    Utilities.setIfNotBlank(idSessionSPC, carrelloRpt::setCodRptInviacarrellorptIdCarrello);

    int updated = carrelloRptDao.update(carrelloRpt);
    if(updated!=1)
      throw new MyPayException("invalid number of rows updated:"+updated+" for mygovCarrelloRptId:"+carrelloRpt.getMygovCarrelloRptId());
  }

  public Optional<CarrelloRpt> getByIdSession(String idSession){ return carrelloRptDao.getByIdSession(idSession); }

  public Optional<CarrelloRpt> getById(Long id){
    return carrelloRptDao.getById(id);
  }

  public Optional<CarrelloRpt> getByCarrelloRpId(Long id){ return carrelloRptDao.getByCarrelloRpId(id); }

}
