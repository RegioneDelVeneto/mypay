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
package it.regioneveneto.mygov.payment.mypay4.scheduled.scadenzacarrello;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
@ConditionalOnProperty(name=AbstractApplication.NAME_KEY, havingValue=ScadenzaCarrelloTaskApplication.NAME)
public class ScadenzaCarrelloTaskService {

  @Value("${pa.modelloUnico}")
  private boolean modelloUnico;

  @Value("${task.scadenzaCarrello.batchRowLimit:${task.common.batchRowLimit}}")
  private int scadenzaCarrelloBatchRowLimit;

  @Value("${task.scadenzaCarrello.minutiScadenzaCarrello}")
  private int scadenzaCarrelloMinutiScadenza;

  @Value("${task.scadenzaCarrello.minutiElaborazioneCarrello}")
  private int scadenzaCarrelloMinutiElaborazione;

  @Value("${task.scadenzaCarrello.oreScadenzaCarrelloModelloUnico}")
  private int oreScadenzaCarrelloModelloUnico;

  @Value("${task.scadenzaCarrello.oreCancellazioneCarrelloModelloUnico}")
  private int oreCancellazioneCarrelloModelloUnico;

  @Resource
  private ScadenzaCarrelloTaskService self;

  @Autowired
  private CarrelloService carrelloService;

  @Autowired
  private CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;

  @Autowired
  private DovutoService dovutoService;

  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;

  @Autowired
  private DovutoCarrelloService dovutoCarrelloService;

  @Autowired
  private DatiMarcaBolloDigitaleService datiMarcaBolloDigitaleService;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  private Long statoCarrelloPredispostoId;
  private Long statoCarrelloScadutoId;
  private Long statoCarrelloScadutoElaboratoId;
  private Long statoDovutoScadutoId;
  private Long statoCarrelloMultiBeneficiarioPredispostoId;
  private Long statoCarrelloMultiBeneficiarioPagamentoInCorsoId;
  private Long statoCarrelloMultiBeneficiarioScadutoId;
  private Long statoCarrelloMultiBeneficiarioScadutoElaboratoId;
  private Long statoDovutoPagabileId;

  @PostConstruct
  void init(){
    statoCarrelloPredispostoId = anagraficaStatoService
        .getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PREDISPOSTO, Constants.STATO_TIPO_CARRELLO).getMygovAnagraficaStatoId();
    statoCarrelloScadutoId = anagraficaStatoService
        .getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_SCADUTO, Constants.STATO_TIPO_CARRELLO).getMygovAnagraficaStatoId();
    statoCarrelloScadutoElaboratoId = anagraficaStatoService
        .getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_SCADUTO_ELABORATO, Constants.STATO_TIPO_CARRELLO).getMygovAnagraficaStatoId();
    statoDovutoScadutoId = anagraficaStatoService
        .getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_SCADUTO, Constants.STATO_TIPO_DOVUTO).getMygovAnagraficaStatoId();
    statoCarrelloMultiBeneficiarioPredispostoId = anagraficaStatoService
        .getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PREDISPOSTO, Constants.STATO_TIPO_MULTI_CARRELLO).getMygovAnagraficaStatoId();
    statoCarrelloMultiBeneficiarioPagamentoInCorsoId = anagraficaStatoService
        .getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO, Constants.STATO_TIPO_MULTI_CARRELLO).getMygovAnagraficaStatoId();
    statoCarrelloMultiBeneficiarioScadutoId = anagraficaStatoService
        .getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_SCADUTO, Constants.STATO_TIPO_MULTI_CARRELLO).getMygovAnagraficaStatoId();
    statoCarrelloMultiBeneficiarioScadutoElaboratoId = anagraficaStatoService
        .getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_SCADUTO_ELABORATO, Constants.STATO_TIPO_MULTI_CARRELLO).getMygovAnagraficaStatoId();
    statoDovutoPagabileId = anagraficaStatoService
      .getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_DA_PAGARE, Constants.STATO_TIPO_DOVUTO).getMygovAnagraficaStatoId();
  }

  private long counter = 0;
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void scadenzaCarrello(){
    log.info("scadenzaCarrello start [{}]", ++counter);

    if (modelloUnico) {
      self.eraseCheckoutCartsExpired(scadenzaCarrelloMinutiScadenza * 60, statoDovutoPagabileId);
    }
    self.markCarrelliExpired(Constants.TIPO_CARRELLO_ESTERNO_ANONIMO);
    self.processCarrelliExpired(Constants.TIPO_CARRELLO_ESTERNO_ANONIMO);

    self.markCarrelliMultiBeneficiarioExpired(scadenzaCarrelloMinutiScadenza,
      statoCarrelloMultiBeneficiarioPredispostoId, statoCarrelloMultiBeneficiarioScadutoId);
    self.markCarrelliExpired(Constants.TIPO_CARRELLO_ESTERNO_ANONIMO_MULTIENTE);

    self.markCarrelliMultiBeneficiarioExpired(scadenzaCarrelloMinutiElaborazione,
      statoCarrelloMultiBeneficiarioScadutoId, statoCarrelloMultiBeneficiarioScadutoElaboratoId);
    self.processCarrelliExpired(Constants.TIPO_CARRELLO_ESTERNO_ANONIMO_MULTIENTE);

    if(modelloUnico){
      self.markCarrelliMultiBeneficiarioExpired(oreScadenzaCarrelloModelloUnico * 60,
          statoCarrelloMultiBeneficiarioPagamentoInCorsoId, statoCarrelloMultiBeneficiarioScadutoId);
      self.deleteExpiredCarrelliMultiBeneficiario(oreCancellazioneCarrelloModelloUnico * 60,
          statoCarrelloMultiBeneficiarioScadutoId);
    }

    log.info("scadenzaCarrello end [{}]", counter);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void markCarrelliExpired(String carrelloType){
    List<Carrello> carrelloList = carrelloService.getOlderByTypeAndState(
        scadenzaCarrelloMinutiScadenza, carrelloType, statoCarrelloPredispostoId);

    if(!carrelloList.isEmpty()){
      log.info("carrello of type {} to mark expired: {}", carrelloType, carrelloList.size());
      carrelloList.forEach(carrello -> {
        try{
          self.markCarrelloExpired(carrello);
        }catch(Exception e){
          log.debug("error marking expired carrello with idSession: {}", carrello.getIdSession(), e);
        }
      });
    }
  }

  @Transactional(propagation = Propagation.NESTED)
  public void markCarrelloExpired(Carrello carrello){
    log.debug("marking expired carrello with idSession: {}", carrello.getIdSession());
    carrelloService.updateStato(carrello.getMygovCarrelloId(), statoCarrelloScadutoId);
    List<Long> idDovutiInCarrello = dovutoService.getDovutiInCarrello(carrello.getMygovCarrelloId())
        .stream().map(Dovuto::getMygovDovutoId).collect(Collectors.toUnmodifiableList());
    dovutoService.updateStato(idDovutiInCarrello, statoDovutoScadutoId);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void markCarrelliMultiBeneficiarioExpired(int minutes, Long currentMygovAnagraficaStatoId, Long newMygovAnagraficaStatoId){
    List<CarrelloMultiBeneficiario> carrelloList = carrelloMultiBeneficiarioService.getOlderByState(
        minutes, currentMygovAnagraficaStatoId, false);

    if(!carrelloList.isEmpty()){
      log.info("carrelloMultiBeneficiario to mark expired status[{}->{}]: {}", currentMygovAnagraficaStatoId, newMygovAnagraficaStatoId, carrelloList.size());
      carrelloList.forEach(carrello -> {
        try{
          self.markCarrelloMultiBeneficiarioExpired(carrello, newMygovAnagraficaStatoId);
        }catch(Exception e){
          log.debug("error marking expired carrello with id: {}", carrello.getMygovCarrelloMultiBeneficiarioId(), e);
        }
      });
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteExpiredCarrelliMultiBeneficiario(int minutes, Long currentMygovAnagraficaStatoId){
    List<CarrelloMultiBeneficiario> carrelloList = carrelloMultiBeneficiarioService.getOlderByState(
        minutes, currentMygovAnagraficaStatoId, true);

    if(!carrelloList.isEmpty()){
      log.info("carrelloMultiBeneficiario to delete: {}", carrelloList.size());
      carrelloList.forEach(carrello -> {
        try{
          log.info("deleting expired carrello with id: {}", carrello.getMygovCarrelloMultiBeneficiarioId());
          carrelloMultiBeneficiarioService.delete(carrello.getMygovCarrelloMultiBeneficiarioId(), currentMygovAnagraficaStatoId);
        }catch(Exception e){
          log.info("error deleting expired carrello with id: {}", carrello.getMygovCarrelloMultiBeneficiarioId(), e);
        }
      });
    }
  }

  @Transactional(propagation = Propagation.NESTED)
  public void markCarrelloMultiBeneficiarioExpired(CarrelloMultiBeneficiario carrello, Long newStato){
    log.debug("marking expired carrelloMultiBeneficiario with id: {}", carrello.getMygovCarrelloMultiBeneficiarioId());
    carrelloMultiBeneficiarioService.updateStato(carrello.getMygovCarrelloMultiBeneficiarioId(), newStato);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void processCarrelliExpired(String carrelloType){
    List<Carrello> carrelloList = carrelloService.getOlderByTypeAndState(
        scadenzaCarrelloMinutiElaborazione, carrelloType, statoCarrelloScadutoId);

    if(!carrelloList.isEmpty()){
      log.info("carrello of type {} to process: {}", carrelloType, carrelloList.size());
      carrelloList.forEach(carrello -> {
        try{
          self.processCarrelloExpired(carrello);
        }catch(Exception e){
          log.debug("error processing expired carrello with idSession: {}", carrello.getIdSession(), e);
        }
      });
    }
  }

  @Transactional(propagation = Propagation.NESTED)
  public void processCarrelloExpired(Carrello carrello){
    log.debug("process expired carrello with idSession: {}", carrello.getIdSession());
    carrelloService.updateStato(carrello.getMygovCarrelloId(), statoCarrelloScadutoElaboratoId);


    AtomicInteger indiceDatiSingoloPagamento = new AtomicInteger();
    dovutoService.getDovutiInCarrello(carrello.getMygovCarrelloId()).forEach(dovuto -> {
      DovutoElaborato dovutoElaborato = dovutoElaboratoService.insertByEsito(dovuto, null, carrello, null, null, null,
          Constants.STATO_DOVUTO_SCADUTO_ELABORATO, Constants.STATO_TIPO_DOVUTO, indiceDatiSingoloPagamento.incrementAndGet());
      dovutoCarrelloService.deleteDovutoCarrelloByIdDovuto(dovuto.getMygovDovutoId());

      /*IUV_MULTI_14 Multi-beneficiary IUV management if defined
       * Insertion of the dovuto multi-beneficiary elaborate and delete of the dovuto multi-beneficiary
       */
      DovutoMultibeneficiario dovutoMultibeneficiario = dovutoService.getDovMultibenefByIdDovuto(dovuto.getMygovDovutoId());
      if(dovutoMultibeneficiario!=null) {
        dovutoElaboratoService.insertDovutoMultibenefElaborato(dovuto, dovutoMultibeneficiario, dovutoElaborato.getMygovDovutoElaboratoId());
        dovutoService.deleteDovMultibenef(dovutoMultibeneficiario);
      }
      dovutoService.removeDovuto(dovuto);
      if(dovuto.getMygovDatiMarcaBolloDigitaleId()!=null)
        datiMarcaBolloDigitaleService.remove(dovuto.getMygovDatiMarcaBolloDigitaleId());
    });
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void eraseCheckoutCartsExpired(int minutes, Long currentMygovAnagraficaStatoId){
    List<Dovuto> list = dovutoService.getOlderByIuvVolatileAndState(minutes, currentMygovAnagraficaStatoId, scadenzaCarrelloBatchRowLimit);
    if(!list.isEmpty()){
      log.info("list of Checkout Carts with flgIuvVolatile to erase: {}", list.size());
      list.forEach(item -> {
        try{
          dovutoService.eraseCheckoutCartsExpired(item);
        }catch(Exception e){
          log.debug("error wiping out dovuto with IUD: {}", item.getCodIud(), e);
        }
      });
    }
  }

  @Transactional(propagation = Propagation.NESTED)
  public void eraseCheckoutCartsExpired(Dovuto dovuto){
    log.debug("process expired Iuv Volatile with IUD: {}", dovuto.getCodIud());
    DovutoMultibeneficiario dovutoMultibeneficiario = dovutoService.getDovMultibenefByIdDovuto(dovuto.getMygovDovutoId());
    if(dovutoMultibeneficiario!=null) {
      dovutoService.deleteDovMultibenef(dovutoMultibeneficiario);
    }
    dovutoService.removeDovuto(dovuto);
    if(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE.equals(dovuto.getCodTipoDovuto()))
      datiMarcaBolloDigitaleService.remove(dovuto.getMygovDatiMarcaBolloDigitaleId());
  }
}
