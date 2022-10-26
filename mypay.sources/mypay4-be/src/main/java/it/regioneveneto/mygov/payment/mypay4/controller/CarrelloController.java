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
package it.regioneveneto.mygov.payment.mypay4.controller;

import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.BasketTo;
import it.regioneveneto.mygov.payment.mypay4.dto.CartItem;
import it.regioneveneto.mygov.payment.mypay4.dto.EsitoTo;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoElaboratoService;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.PaymentManagerService;
import it.regioneveneto.mygov.payment.mypay4.service.RecaptchaService;
import it.regioneveneto.mygov.payment.mypay4.service.common.AppErrorService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRPRisposta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.PAA_SYSTEM_ERROR;

@RestController
@Slf4j
@ConditionalOnWebApplication
public class CarrelloController {

  private final static String AUTHENTICATED_PATH ="carrello";
  private final static String ANONYMOUS_PATH= MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;

  @Autowired
  PaymentManagerService paymentManagerService;
  @Autowired
  DovutoService dovutoService;
  @Autowired
  DovutoElaboratoService dovutoElaboratoService;
  @Autowired
  RecaptchaService recaptchaService;
  @Autowired
  MessageSource messageSource;

  @Autowired
  AppErrorService appErrorService;

  @RequestMapping(value = AUTHENTICATED_PATH + "/checkout", method = RequestMethod.POST)
  public EsitoTo checkoutLoggedIn(
      @RequestBody BasketTo basketTo,
      @RequestParam(name = "overrideCheckReplicaPayments", required = false) String overrideCheckReplicaPayments) {
    if (Objects.isNull(basketTo.getTipoCarrello()))
      basketTo.setTipoCarrello(Constants.TIPO_CARRELLO_DEFAULT_CITTADINO);
    return checkoutCarrello(basketTo, overrideCheckReplicaPayments);
  }

  @RequestMapping(value = ANONYMOUS_PATH + "/checkout", method = RequestMethod.POST)
  public EsitoTo checkoutAnonymous(
      @RequestBody BasketTo basketTo,
      @RequestParam String recaptcha,
      @RequestParam(name = "overrideCheckReplicaPayments", required = false) String overrideCheckReplicaPayments) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"checkoutCarrello");
    if(!captchaVerified){
      return EsitoTo.builder().esito("KO").returnMsg("Errore verifica recaptcha").build();
    }
    if (Objects.isNull(basketTo.getTipoCarrello()))
      basketTo.setTipoCarrello(Constants.TIPO_CARRELLO_CITTADINO_ANONYMOUS);
    return checkoutCarrello(basketTo, overrideCheckReplicaPayments);
  }

  private EsitoTo checkoutCarrello(BasketTo basketTo, String overrideCheckReplicaPayments) {
    //check Replica Payments (SPAC #261)
    if (!"ok".equals(overrideCheckReplicaPayments)) {
      Optional<CartItem> replicaDovuto = basketTo.getItems().stream().filter(item ->
          dovutoService.hasReplicaDovuto(
              item.getCodIpaEnte(),
              item.getIntestatario().getTipoIdentificativoUnivoco(),
              item.getIntestatario().getCodiceIdentificativoUnivoco(),
              item.getCausale(),
              item.getCodTipoDovuto()).orElse(Boolean.FALSE)
      ).findFirst();
      if (replicaDovuto.isPresent()) {
        log.debug("found DOVUTO payment replica for cart item :" + replicaDovuto.get().toString());
        return EsitoTo.builder().esito("KO_REPLICA").returnMsg("REPLICA_DOVUTO").build();
      }
      Optional<CartItem> replicaDovutoElaborato = basketTo.getItems().stream().filter(item ->
          dovutoElaboratoService.hasReplicaDovutoElaborato(
              item.getCodIpaEnte(),
              item.getIntestatario().getTipoIdentificativoUnivoco(),
              item.getIntestatario().getCodiceIdentificativoUnivoco(),
              item.getCausale(),
              item.getCodTipoDovuto()).orElse(Boolean.FALSE)
      ).findFirst();
      if (replicaDovutoElaborato.isPresent()) {
        log.debug("found DOVUTO_ELABORATO payment replica for cart item :" + replicaDovutoElaborato.get().toString());
        return EsitoTo.builder().esito("KO_REPLICA").returnMsg("REPLICA_DOVUTO_ELABORATO").build();
      }
    } else {
      log.debug("skipping replica payment check as per override request parameter");
    }

    EsitoTo esitoTo;
    try {
      NodoSILInviaCarrelloRPRisposta ncr = paymentManagerService.checkoutCarrello(basketTo);
      esitoTo = this.mapResponseNodoToEsitoTo(ncr);
    } catch(Exception ex) {
      log.error("checkoutCarrello", ex);
      String errorMsg = StringUtils.isBlank(ex.getMessage()) ?
          messageSource.getMessage("pa.errore.internalError", null, Locale.ITALY)
          : ex.getMessage();
      esitoTo = EsitoTo.builder().esito("KO").returnMsg(errorMsg).build();
    }
    if(!StringUtils.equalsIgnoreCase(esitoTo.getEsito(), "OK")){
      Pair<String, String> nowStringAndErrorUid = appErrorService.generateNowStringAndErrorUid();
      esitoTo.setErrorUid(nowStringAndErrorUid.getRight());
      log.error("errorUID[{}] now[{}] faultBean[{}]", nowStringAndErrorUid.getRight(), nowStringAndErrorUid.getLeft(),
        esitoTo.getFaultBean()!=null ? ReflectionToStringBuilder.toString(esitoTo.getFaultBean()) : null);
    }

    return esitoTo;
  }

  private EsitoTo mapResponseNodoToEsitoTo(NodoSILInviaCarrelloRPRisposta ncr) {
    UnaryOperator<FaultBean> fault = obj -> Optional.ofNullable(obj)
      .orElse(VerificationUtils.getFespFaultBean("NODO_INVIA_CARRELLO", PAA_SYSTEM_ERROR,
        messageSource.getMessage("pa.errore.internalError", null, Locale.ITALY))
      );

    return Optional.ofNullable(ncr).map(obj ->
      EsitoTo.builder()
        .esito(obj.getEsito())
        .url(obj.getUrl())
        .faultBean(fault.apply(ncr.getFault()))
        .returnMsg(fault.apply(ncr.getFault()).getFaultString())
        .build())
      .orElse(null);
  }
}
