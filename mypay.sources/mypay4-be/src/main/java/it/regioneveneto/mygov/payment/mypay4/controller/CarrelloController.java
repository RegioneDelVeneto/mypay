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
import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.dto.externalapp.PagamentoSpontaneoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.PaymentOrderException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.AppErrorService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@ConditionalOnWebApplication
public class CarrelloController {

  private static final String AUTHENTICATED_PATH ="carrello";
  private static final String ANONYMOUS_PATH= MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;
  private static final String A2A_PATH= MyPay4AbstractSecurityConfig.PATH_A2A+"/"+ AUTHENTICATED_PATH;

  @Value("${pa.modelloUnico}")
  private boolean modelloUnico;

  @Autowired
  private PaymentManagerService paymentManagerService;
  @Autowired
  private DovutoService dovutoService;
  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;
  @Autowired
  private CarrelloService carrelloService;
  @Autowired
  private CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;
  @Autowired
  private JwtTokenUtil jwtTokenUtil;
  @Autowired
  private RecaptchaService recaptchaService;
  @Autowired
  private EnteService enteService;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private AppErrorService appErrorService;
  @Autowired
  private MailValidationService mailValidationService;
  @Autowired
  private StorageService storageService;
  @Autowired
  private LandingService landingService;
  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private AttualizzazioneImportoService attualizzazioneImportoService;

  @Value("${pa.gpd.enabled}")
  private boolean gpdEnabled;

  @GetMapping(AUTHENTICATED_PATH + "/update")
  public AttualizzazioneTo updateBasketPnd(
          @AuthenticationPrincipal UserWithAdditionalInfo user,
          @RequestParam String codIpaEnte, @RequestParam String codTipoDovuto,
          @RequestParam String codIuv) {
    return updateImportPnd(codIpaEnte, codTipoDovuto, codIuv);
  }

  @GetMapping(ANONYMOUS_PATH + "/update")
  public AttualizzazioneTo updateBasketPndAnonymous(
          @AuthenticationPrincipal UserWithAdditionalInfo user,
          @RequestParam String codIpaEnte, @RequestParam String codTipoDovuto,
          @RequestParam String codIuv, @RequestParam String recaptcha) {
    boolean captchaVerified = recaptchaService.verify(recaptcha, "updateImporto");
    if (!captchaVerified) {
      throw new MyPayException("Errore verifica recaptcha");
    }
    return updateImportPnd(codIpaEnte, codTipoDovuto, codIuv);
  }

  private AttualizzazioneTo updateImportPnd(String codIpaEnte, String codTipoDovuto, String codIuv) {

    ///Ricavo user & password  PND
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
    Optional<EnteTipoDovuto> enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(codTipoDovuto, codIpaEnte, false);
    if (enteTipoDovutoOptional.isEmpty())
      throw new MyPayException("Tipo dovuto non trovato per ente");
    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOptional.get();
    BigDecimal importoPosizione = getAttualeImportoDovuto(codIuv, ente);
    AttualizzazioneTo attualizzazione = new AttualizzazioneTo();
    attualizzazione.setImportoPosizione(importoPosizione);
    if(!gpdEnabled) {
      try {
        attualizzazione = attualizzazioneImportoService.attualizzaImporto(codIuv, enteTipoDovuto, ente);
      } catch (AttualizzaImportoException ae) {
        if (ae.isBlocking())
          throw ae;
        else
          log.warn("Eccezione gestita nell'attualizzazione dell'importo, ignoring it", ae);
      } catch (Exception e) {
        log.warn("Eccezione nell'attualizzazione dell'importo, ignoring it", e);
      }
    }
    return attualizzazione;
  }

  @PostMapping(AUTHENTICATED_PATH + "/checkout")
  public EsitoTo checkoutLoggedIn(
      @AuthenticationPrincipal UserWithAdditionalInfo user,
      @RequestBody BasketTo basketTo,
      @RequestParam(name = "overrideCheckReplicaPayments", required = false) String overrideCheckReplicaPayments) {
    if (Objects.isNull(basketTo.getTipoCarrello()))
      basketTo.setTipoCarrello(Constants.TIPO_CARRELLO_DEFAULT_CITTADINO);
    //if logged in, versante field must be the same of authenticated user
    //if versante is null, later on a more detailed error will be returned
    if(basketTo.getVersante()!=null && !basketTo.getVersante().checkSameCoreFields(user))
      return EsitoTo.builder().esito("KO").returnMsg("Errore dati versante").build();
    //mail should be either logged-in email address or validated mail with token
    if(StringUtils.isBlank(basketTo.getMailValidationToken())){
      if(basketTo.getVersante()!=null && !StringUtils.equalsIgnoreCase(user.getEmail(), basketTo.getVersante().getEmail()))
        return EsitoTo.builder().esito("KO").returnMsg("Errore dati versante - mail").build();
    } else {
      if(basketTo.getVersante()!=null && !mailValidationService.verifyMailValidationToken(basketTo.getMailValidationToken(), basketTo.getVersante().getEmail()))
        return EsitoTo.builder().esito("KO").returnMsg("Errore verifica indirizzo email").build();
    }

    if(!modelloUnico)
      aggiornaImportoCarrello(basketTo);

    return checkoutCarrello(user, basketTo, overrideCheckReplicaPayments);
  }

  @PostMapping(ANONYMOUS_PATH + "/checkout")
  public EsitoTo checkoutAnonymous(
      @RequestBody BasketTo basketTo,
      @RequestParam String recaptcha,
      @RequestParam(name = "overrideCheckReplicaPayments", required = false) String overrideCheckReplicaPayments) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"checkoutCarrello");
    if(!captchaVerified){
      return EsitoTo.builder().esito("KO").returnMsg("Errore verifica recaptcha").build();
    }
    //if versante is null, later on a more detailed error will be returned
    if(basketTo.getVersante()!=null && !mailValidationService.verifyMailValidationToken(basketTo.getMailValidationToken(), basketTo.getVersante().getEmail())){
      return EsitoTo.builder().esito("KO").returnMsg("Errore verifica indirizzo email").build();
    }
    if (Objects.isNull(basketTo.getTipoCarrello()))
      basketTo.setTipoCarrello(Constants.TIPO_CARRELLO_CITTADINO_ANONYMOUS);

    aggiornaImportoCarrello(basketTo);

    return checkoutCarrello(null, basketTo, overrideCheckReplicaPayments);
  }

  @PostMapping(ANONYMOUS_PATH + "/checkoutExtApp/{token}")
  public EsitoTo checkoutAnonymousExternalApp(
    @PathVariable String token,
    @RequestBody SpontaneoTo spontaneoTo,
    @RequestParam String recaptcha,
    @RequestParam(name = "overrideCheckReplicaPayments", required = false) String overrideCheckReplicaPayments) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"checkoutExtApp");
    if(!captchaVerified){
      return EsitoTo.builder().esito("KO").returnMsg("Errore verifica recaptcha").build();
    }

    //retrieve data from storage
    PagamentoSpontaneoTo spontaneousPaymentTo = storageService.getObject(StorageService.WS_USER, token, PagamentoSpontaneoTo.class)
      .orElse(null);
    if(spontaneousPaymentTo==null){
      return EsitoTo.builder().esito("KO").returnMsg("Token non valido").build();
    }

    BasketTo basketTo = BasketTo.builder()
      .versante(AnagraficaPagatore.builder()
        .codiceIdentificativoUnivoco(spontaneousPaymentTo.getVersanteCf())
        .tipoIdentificativoUnivoco((StringUtils.length(spontaneousPaymentTo.getVersanteCf())==11 ? Constants.TIPOIDENTIFICATIVOUNIVOCO_G : Constants.TIPOIDENTIFICATIVOUNIVOCO_F).charAt(0))
        .anagrafica(spontaneousPaymentTo.getVersanteAnagrafica())
        .email(spontaneousPaymentTo.getVersanteEmail())
        .build())
      .items(List.of(CartItem.builder()
          .id(spontaneoTo.getId())
          .causale(spontaneoTo.getCausale())
          .causaleVisualizzata(spontaneoTo.getCausaleVisualizzata())
          .importo(spontaneoTo.getImporto())
          .codIpaEnte(spontaneoTo.getCodIpaEnte())
          .codTipoDovuto(spontaneoTo.getCodTipoDovuto())
          .intestatario(spontaneoTo.getIntestatario())
        .build()))
      .backUrlInviaEsito(spontaneousPaymentTo.getCallbackUrl())
      .build();

    return checkoutCarrello(null, basketTo, overrideCheckReplicaPayments);
  }

  private EsitoTo checkoutCarrello(UserWithAdditionalInfo user, BasketTo basketTo, String overrideCheckReplicaPayments) {
    //check Replica Payments (SPAC #261)
    if (!"ok".equals(overrideCheckReplicaPayments)) {
      Optional<String> replicaPaymentType = checkReplicaPayment(basketTo);
      if(replicaPaymentType.isPresent())
        return EsitoTo.builder().esito("KO_REPLICA").returnMsg(replicaPaymentType.get()).build();
    } else {
      log.debug("skipping replica payment check as per override request parameter");
    }

    EsitoTo esitoTo;
    try {
      esitoTo = paymentManagerService.checkoutCarrello(user, basketTo);
    } catch(PaymentOrderException poe) {
      return EsitoTo.builder().esito("KO_MANAGED").returnMsg(poe.getMessage()).build();
    } catch(Exception ex) {
      log.error("checkoutCarrello", ex);
      String errorMsg = StringUtils.isBlank(ex.getMessage()) ?
          messageSource.getMessage("pa.errore.internalError", null, Locale.ITALY)
          : ex.getMessage();
      esitoTo = EsitoTo.builder().esito(Constants.STATO_ESITO_KO).returnMsg(errorMsg).build();
    }
    if(esitoTo!=null && !StringUtils.equalsIgnoreCase(esitoTo.getEsito(), Constants.STATO_ESITO_OK)){
      Pair<String, String> nowStringAndErrorUid = appErrorService.generateNowStringAndErrorUid();
      esitoTo.setErrorUid(nowStringAndErrorUid.getRight());
      log.error("errorUID[{}] now[{}] faultBean[{}]", nowStringAndErrorUid.getRight(), nowStringAndErrorUid.getLeft(),
          esitoTo.getFaultBean()!=null ? ReflectionToStringBuilder.toString(esitoTo.getFaultBean()) : null);
    }

    return esitoTo;
  }

  public Optional<String> checkReplicaPayment(BasketTo basketTo){
    Optional<CartItem> replicaDovuto = basketTo.getItems().stream().filter(item ->
            dovutoService.hasReplicaDovuto(
                    item.getCodIpaEnte(),
                    item.getIntestatario().getTipoIdentificativoUnivoco(),
                    item.getIntestatario().getCodiceIdentificativoUnivoco(),
                    item.getCausale(),
                    item.getCodTipoDovuto()).orElse(Boolean.FALSE)
    ).findFirst();
    if (replicaDovuto.isPresent()) {
      log.debug("found DOVUTO payment replica for cart item : {}", replicaDovuto.get());
      return Optional.of("REPLICA_DOVUTO");
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
      log.debug("found DOVUTO_ELABORATO payment replica for cart item : {}", replicaDovutoElaborato.get());
      return Optional.of("REPLICA_DOVUTO_ELABORATO");
    }
    return Optional.empty();
  }

  @PostMapping(A2A_PATH + "/checkReplicaPayments/{idSession}")
  public PaymentCheckOutcomeTo checkReplicaPayments(
          @PathVariable String idSession) {
    String idToken = storageService.getString(StorageService.WS_USER, idSession).orElseThrow(NotFoundException::new);
    log.debug("checkReplicaPayment, idSession[{}] idToken[{}]", idSession, idToken);
    BasketTo basketTo = storageService.getObject(StorageService.WS_USER, idToken, BasketTo.class).orElseThrow(NotFoundException::new);

    PaymentCheckOutcomeTo paymentCheckOutcomeTo = new PaymentCheckOutcomeTo();
    Optional<String> replicaPaymentType = checkReplicaPayment(basketTo);

    if (replicaPaymentType.isPresent()) {
      if (StringUtils.equals(replicaPaymentType.get(), "REPLICA_DOVUTO"))
        paymentCheckOutcomeTo.setHasOngoingPaymentReplica(true);
      else
        paymentCheckOutcomeTo.setHasPaymentCompletedReplica(true);
      log.debug("checkReplicaPayment, idSession[{}] hasReplica[{}]", idSession, replicaPaymentType.get());
    }
    paymentCheckOutcomeTo.setForcePaymentUrl(landingService.getUrlInviaDovuti(idToken, true));

    return paymentCheckOutcomeTo;
  }

  @GetMapping(AUTHENTICATED_PATH + "/outcome")
  public PaymentOutcomeTo getOutcomeLoggedIn(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                             @RequestParam String pollingToken) {
    return getOutcomeImpl(user, pollingToken);
  }

  @GetMapping(ANONYMOUS_PATH + "/outcome")
  public PaymentOutcomeTo getOutcomeAnonymous(@RequestParam String pollingToken,
                                              @RequestParam String recaptcha) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"outcome");
    if(!captchaVerified){
      throw new MyPayException("Errore verifica recaptcha");
    }
    return getOutcomeImpl(null, pollingToken);
  }

  private PaymentOutcomeTo getOutcomeImpl(UserWithAdditionalInfo user, String pollingToken){

    try {
      String pollingTokenPayload;
      if(pollingToken.startsWith("!!") && user!=null && user.isSysAdmin()){
        pollingTokenPayload = pollingToken.substring(2);
      } else {
        pollingTokenPayload = jwtTokenUtil.parsePollingToken(null, pollingToken);
      }

      String outcome;
      Long carrelloMultibeneficiarioId;
      AnagraficaStato stato = null;

      if(modelloUnico){
        Optional<CarrelloMultiBeneficiario> optionalCarrelloMb = this.carrelloMultiBeneficiarioService.getByIdSession(pollingTokenPayload);
        if(optionalCarrelloMb.isEmpty())
          return PaymentOutcomeTo.builder().status(PaymentOutcomeTo.PAYMENT_OUTCOME_NOT_FOUND).build();

        stato = optionalCarrelloMb.get().getMygovAnagraficaStatoId();
        carrelloMultibeneficiarioId = optionalCarrelloMb.get().getMygovCarrelloMultiBeneficiarioId();
      } else {
        carrelloMultibeneficiarioId = Long.parseLong(pollingTokenPayload);
      }
      Optional<Carrello> optionalCarrello = Optional.ofNullable(carrelloMultibeneficiarioId).flatMap(this.carrelloService::getByMultiBeneficarioId);
      if(optionalCarrello.isPresent()){
        stato = optionalCarrello.get().getMygovAnagraficaStatoId();
      } else if (!modelloUnico) {
        return PaymentOutcomeTo.builder().status(PaymentOutcomeTo.PAYMENT_OUTCOME_NOT_FOUND).build();
      }

      String newPollingToken = null;
      switch (stato.getCodStato()) {
        case Constants.STATO_CARRELLO_PAGATO:
          outcome = PaymentOutcomeTo.PAYMENT_OUTCOME_PAID_OK;
          break;
        case Constants.STATO_CARRELLO_NUOVO:
          outcome = PaymentOutcomeTo.PAYMENT_OUTCOME_TO_PAY;
          break;
        case Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO:
          outcome = PaymentOutcomeTo.PAYMENT_OUTCOME_PAYMENT_INITIATED;
          newPollingToken = jwtTokenUtil.generatePollingToken(null, String.valueOf(carrelloMultibeneficiarioId), 20);
          break;
        default:
          outcome = PaymentOutcomeTo.PAYMENT_OUTCOME_PAID_KO;
      }


      BigDecimal paidAmount = null;
      String emailVersante = null;

      PaymentOutcomeTo response = PaymentOutcomeTo.builder()
          .status(outcome)
          .pollingToken(newPollingToken)
          .build();

      if(optionalCarrello.isPresent()) {
        Carrello carrello = optionalCarrello.get();
        emailVersante = StringUtils.firstNonBlank(carrello.getDeESoggVersEmailVersante(), carrello.getDeRpSoggVersEmailVersante());
        BigDecimal paidAmountCarrello = carrello.getNumEDatiPagImportoTotalePagato();
        String statoCarrello = carrello.getMygovAnagraficaStatoId().getCodStato();
        if (statoCarrello.equals(Constants.STATO_CARRELLO_PAGATO)
            || statoCarrello.equals(Constants.STATO_CARRELLO_NON_PAGATO)
            || statoCarrello.equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
            || statoCarrello.equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE)) {
          List<DovutoElaborato> pagatiList = dovutoElaboratoService.getByCarrello(carrello);
          if (pagatiList.size() == 1 && paidAmountCarrello.compareTo(BigDecimal.ZERO) > 0) {
            DovutoMultibeneficiarioElaborato dovutoMultibeneficiarioElaborato = dovutoElaboratoService.getDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(
                pagatiList.get(0).getMygovDovutoElaboratoId()).orElse(null);
            if (dovutoMultibeneficiarioElaborato != null)
              paidAmountCarrello = pagatiList.get(0).getNumEDatiPagDatiSingPagSingoloImportoPagato().add(dovutoMultibeneficiarioElaborato.getNumRpDatiVersDatiSingVersImportoSingoloVersamento());
          }
          if (!pagatiList.isEmpty()) {
            if (response.getRt() == null)
              response.setRt(new ArrayList<>());
            Ente ente = pagatiList.get(0).getNestedEnte();
            response.getRt().add(PaymentOutcomeRtTo.builder()
                .id(pagatiList.get(0).getMygovDovutoElaboratoId())
                .noticeNumber(Utilities.iuvToNumeroAvviso(pagatiList.get(0).getValidIuv(), ente.getApplicationCode(), false))
                .codIpaEnte(ente.getCodIpaEnte())
                .deEnte(ente.getDeNomeEnte())
                .securityToken(jwtTokenUtil.generateSecurityToken(user, String.valueOf(pagatiList.get(0).getMygovDovutoElaboratoId())))
                .build()
            );
          }
        }
        if (paidAmountCarrello != null)
          paidAmount = paidAmountCarrello;
      }

      response.setEmailVersante(emailVersante);
      response.setPaidAmount(paidAmount);
      return response;
    } catch(Exception e){
      log.warn("error parsing pollingToken", e);
      throw new MyPayException("token non valido");
    }

  }

  private BasketTo aggiornaImportoCarrello(BasketTo basketTo) {
    List<CartItem> listItemAtt = basketTo.getItems().stream()
            .filter(CartItem::isImportoAtt) // Utilizza il risultato di item.isImportoAtt() come condizione di filtro
            .collect(Collectors.toList()); // Raccogli gli elementi filtrati in una lista
    List<CartItem> listItemOrig = basketTo.getItems();
    log.debug("item Attualizzati: "+listItemAtt);
    for ( CartItem item: listItemAtt) {
      log.debug("Cart ITem da Aggiornare: "+item);
      BigDecimal importoAttualizzato = item.getImporto();
      Dovuto dovuto = dovutoService.getById(item.getId());
      dovuto.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(importoAttualizzato);
      String bilancio = item.getBilancio();
      if(bilancio != null) {
        dovuto.setBilancio(bilancio);
      }
      dovutoService.upsert(dovuto);
    }
    for (int i = 0; i < listItemOrig.size(); i++) {
      CartItem originalItem = listItemOrig.get(i);
      String codIuvOrig = originalItem.getCodIuv(); // Supponiamo che ci sia un metodo per ottenere il codice IUV

      // Cerca l'elemento corrispondente in listItemAtt
      Optional<CartItem> matchingItem = listItemAtt.stream()
              .filter(item -> codIuvOrig.equals(item.getCodIuv())) // Filtra gli elementi con lo stesso codice IUV
              .findFirst(); // Prendi il primo elemento corrispondente, se esiste

      // Sostituisci l'elemento se ne hai trovato uno corrispondente
      int finalI = i;
      matchingItem.ifPresent(item -> listItemOrig.set(finalI, item));
    }
    basketTo.setItems(listItemOrig);
    return basketTo;
  }

  private BigDecimal getAttualeImportoDovuto(String iuv, Ente ente) {
    BigDecimal ret = BigDecimal.ZERO;
    List<Dovuto> dovutoList =  dovutoService.getByIuvEnte(iuv, ente.getCodIpaEnte());
    if (dovutoList != null && !dovutoList.isEmpty()) {
      ret = dovutoList.get(0).getNumRpDatiVersDatiSingVersImportoSingoloVersamento();
    }
    return ret;
  }



}
