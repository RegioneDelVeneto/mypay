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
package it.regioneveneto.mygov.payment.mypay4.service;

import it.regioneveneto.mygov.payment.mypay4.dao.CarrelloMultiBeneficiarioDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.CarrelloMultiBeneficiario;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRPRisposta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class CarrelloMultiBeneficiarioService {

  @Value("${task.scadenzaCarrello.batchRowLimit:${task.common.batchRowLimit}}")
  private int scadenzaCarrelloBatchRowLimit;

  @Autowired
  AnagraficaStatoService anagraficaStatoService;
  @Autowired
  CarrelloMultiBeneficiarioDao carrelloMultiBeneficiarioDao;
  @Autowired
  MessageSource messageSource;

  public CarrelloMultiBeneficiario getById(Long id) {
    return carrelloMultiBeneficiarioDao.getById(id);
  }

  public CarrelloMultiBeneficiario getByIdSession(String idSession) {
    List<CarrelloMultiBeneficiario> items = carrelloMultiBeneficiarioDao.getByIdSession(idSession);
    if (CollectionUtils.isEmpty(items)) {
      return null;
    } else if (items.size() > 1) {
      throw new MyPayException(messageSource.getMessage("pa.carrello.carrelloDuplicato", null, Locale.ITALY));
    }
    return items.get(0);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public CarrelloMultiBeneficiario insertCarrelloMultiBeneficiario(String codIpaEnte, String status, String backUrl, Optional<String> idSession){
    CarrelloMultiBeneficiario basket = new CarrelloMultiBeneficiario();
    basket.setCodIpaEnte(codIpaEnte);
    AnagraficaStato anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(status, Constants.STATO_TIPO_MULTI_CARRELLO);
    basket.setMygovAnagraficaStatoId(anagraficaStato);
    Date now = new Date();
    basket.setDtCreazione(now);
    basket.setDtUltimaModifica(now);
    basket.setIdSessionCarrello(idSession.orElseGet(Utilities::getRandomicIdSession));
    basket.setRispostaPagamentoUrl(backUrl);
    Long newId = carrelloMultiBeneficiarioDao.insert(basket);
    log.info("insert CarrelloMultiBeneficiario, new newId: "+newId);
    basket = basket.toBuilder().mygovCarrelloMultiBeneficiarioId(newId).build();
    return basket;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateCarrelloMultiBeneficiarioStatus(Long idCarrello, String status){

    CarrelloMultiBeneficiario basket = carrelloMultiBeneficiarioDao.getById(idCarrello);
    String error = "Attempt to change status Carrello ["+basket.getMygovCarrelloMultiBeneficiarioId()
        +"] from ["+basket.getMygovAnagraficaStatoId().getCodStato()+"] to ["+status+"]";
    switch (status) {
      case Constants.STATO_CARRELLO_ABORT:
      case Constants.STATO_CARRELLO_SCADUTO:
        if (!basket.getMygovAnagraficaStatoId().getCodStato().equals(Constants.STATO_CARRELLO_PREDISPOSTO)) {
          throw new DataRetrievalFailureException(error);
        }
        break;
      case Constants.STATO_CARRELLO_SCADUTO_ELABORATO:
        if (!basket.getMygovAnagraficaStatoId().getCodStato().equals(Constants.STATO_CARRELLO_SCADUTO)) {
          throw new DataRetrievalFailureException(error);
        }
        break;
      case Constants.STATO_CARRELLO_IMPOSSIBILE_INVIARE_RP:
      case Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE:
      case Constants.STATO_CARRELLO_DECORRENZA_TERMINI:
      case Constants.STATO_CARRELLO_NON_PAGATO:
      case Constants.STATO_CARRELLO_PARZIALMENTE_PAGATO:
      case Constants.STATO_CARRELLO_PAGATO:
        if (!basket.getMygovAnagraficaStatoId().getCodStato().equals(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO)) {
          throw new DataRetrievalFailureException(error);
        }
        break;
      default:
        break;
    }
    basket.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(status, Constants.STATO_TIPO_MULTI_CARRELLO));

    Date now = new Date();
    basket.setDtUltimaModifica(now);

    int updatedRec = carrelloMultiBeneficiarioDao.update(basket);
    if (updatedRec != 1) {
      throw new MyPayException("CarrelloMultiBeneficiario update internal error");
    }
    log.info("CarrelloMultiBeneficiario ["+basket.getMygovCarrelloMultiBeneficiarioId()+"] is up to date");
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateResultCarrelloRp(final CarrelloMultiBeneficiario basket,
                                     final NodoSILInviaCarrelloRPRisposta ncr)
      throws DataAccessException, UnsupportedEncodingException, MalformedURLException {

    basket.setDeRpSilinviacarrellorpEsito(ncr.getEsito());
    if (!"OK".equals(ncr.getEsito()) && ncr.getFault() != null) {
      FaultBean fb = ncr.getFault();
      basket.setCodRpSilinviacarrellorpFaultCode(fb.getFaultCode());
      basket.setDeRpSilinviacarrellorpFaultString(fb.getFaultString());
      basket.setCodRpSilinviacarrellorpId(fb.getId());
      basket.setCodRpSilinviacarrellorpSerial(fb.getSerial());

      String description = Utilities.getTruncatedAt(1024).apply(fb.getDescription());
      Optional.ofNullable(description).ifPresent(basket::setDeRpSilinviacarrellorpOriginalFaultDescription);
      basket.setCodRpSilinviacarrellorpOriginalFaultCode(fb.getOriginalFaultCode());
      basket.setDeRpSilinviacarrellorpOriginalFaultString(fb.getOriginalFaultString());
      String originalDescription = Utilities.getTruncatedAt(1024).apply(fb.getOriginalDescription());
      Optional.ofNullable(originalDescription).ifPresent(basket::setDeRpSilinviacarrellorpOriginalFaultDescription);

      AnagraficaStato newStatus = anagraficaStatoService.getByCodStatoAndTipoStato(
          Constants.STATO_CARRELLO_IMPOSSIBILE_INVIARE_RP, Constants.STATO_TIPO_MULTI_CARRELLO);

      if (!basket.getMygovAnagraficaStatoId().getCodStato()
          .equals(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO)) {
        throw new DataRetrievalFailureException("Attempt to change status Carrello ["
            + basket.getIdSessionCarrello() + "] from ["
            + basket.getMygovAnagraficaStatoId().getCodStato() + "] to [" + newStatus.getCodStato() + "]");
      }
      basket.setMygovAnagraficaStatoId(newStatus);
    }
    basket.setCodRpSilinviacarrellorpUrl(ncr.getUrl());

    // estrarre idSession e persistere in idSessionFesp
    String idSessionFESP = null;
    if (StringUtils.isNotBlank(ncr.getUrl())) {
      Map<String, String> parametersMap = Utilities.splitQuery(new URL(ncr.getUrl()));
      idSessionFESP = parametersMap.get("idSession");
    }
    basket.setIdSessionCarrellofesp(idSessionFESP);
    basket.setDtUltimaModifica(new Date());

    int updatedRec = carrelloMultiBeneficiarioDao.update(basket);
    if (updatedRec != 1) {
      throw new MyPayException("CarrelloMultiBeneficiario update internal error");
    }
    log.info("CarrelloMultiBeneficiario with: "+basket.getMygovCarrelloMultiBeneficiarioId()+" is up to date");
  }

  public Optional<CarrelloMultiBeneficiario> getByIdSessionFesp(String idSession) {
    return carrelloMultiBeneficiarioDao.getByIdSessionFesp(idSession);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public List<CarrelloMultiBeneficiario> getOlderByState(int minutes, Long mygovAnagraficaStatoId){
    return carrelloMultiBeneficiarioDao.getOlderByState(minutes, mygovAnagraficaStatoId, scadenzaCarrelloBatchRowLimit);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int updateStato(Long mygovCarrelloMultiBeneficiarioId, Long mygovAnagraficaStatoId){
    return carrelloMultiBeneficiarioDao.updateStato(mygovCarrelloMultiBeneficiarioId, mygovAnagraficaStatoId);
  }
}
