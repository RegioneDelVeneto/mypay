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

import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaPagatore;
import it.regioneveneto.mygov.payment.mypay4.dto.BasketTo;
import it.regioneveneto.mygov.payment.mypay4.dto.CartItem;
import it.regioneveneto.mygov.payment.mypay4.dto.EsitoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.CartInfo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.storage.ContentStorage;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.pagamenti.ente.StShowMyPay;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@Slf4j
@ConditionalOnWebApplication
public class LandingController {

  public static final String ANONYMOUS_PATH = MyPay4AbstractSecurityConfig.PATH_PUBLIC + "/landing";
  public static final String LEGACY = "/legacy";

  public static final String PAYMENT_OUTCOME_URL = ANONYMOUS_PATH + "/outcome";

  private enum REPLICA_TYPE { DOVUTO, DOVUTO_ELAB, NO_REPLICA }

  @Value("${app.fe.cittadino.absolute-path}")
  private String appFeCittadinoAbsolutePath;

  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPa;

  @Value("${pa.dataScaricaAvvisoAnagrafica:31/12/2021}")
  private String dataScaricaAvvisoAnagraficaString;

  @Autowired
  private StorageService storageService;
  @Lazy
  @Autowired
  private DovutoService dovutoService;
  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;
  @Autowired
  private AvvisoController avvisoController;
  @Autowired
  private JwtTokenUtil jwtTokenUtil;
  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;
  @Autowired
  EnteService enteService;
  @Autowired
  CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;
  @Autowired
  CarrelloService carrelloService;
  @Autowired
  GiornaleService giornaleService;
  @Autowired
  MessageSource messageSource;
  @Autowired
  JasperService jasperService;
  @Autowired
  PaymentManagerService paymentManagerService;
  @Autowired
  private ExportDovutiService exportDovutiService;
  @Autowired
  private MyBoxService myBoxService;
  @Autowired
  private LandingService landingService;

  private final SimpleDateFormat dateFormatDdMmYyyy = new SimpleDateFormat("dd/MM/yyyy");

  @GetMapping(ANONYMOUS_PATH + "/inviaDovutiReplicaCheck")
  public ResponseEntity<?> landingInviaDovutiReplicaCheck(
      @RequestParam String idSession) {
    try {
      Map<String, String> returnMap = new HashMap<>();
      String[] idTokenContainer = new String[1];
      Optional<BasketTo> basketTo = storageService.getString(StorageService.WS_USER, idSession)
          .flatMap(idToken -> {
            idTokenContainer[0] = idToken;
            return storageService.getObject(StorageService.WS_USER, idToken, BasketTo.class);
          });
      if (basketTo.isEmpty())
        return ResponseEntity.badRequest().body("idSession not valid: " + idSession);
      REPLICA_TYPE replicaType = manageReplicaDovutoImpl(basketTo.get());
      returnMap.put("replica", replicaType.name());
      String url = landingService.getUrlInviaDovuti(idTokenContainer[0], true);
      returnMap.put("urlToForceStartPayment", url);
      return ResponseEntity.ok(returnMap);
    } catch (Exception e) {
      log.error("landingInviaDovutiReplicaCheck", e);
      throw new MyPayException("system error", e);
    }
  }

  @GetMapping(ANONYMOUS_PATH + "/inviaDovuti")
  public ResponseEntity<?> landingInviaDovuti(
      @RequestParam String id,
      @RequestParam(name = "overrideCheckReplicaPayments", required = false) String overrideCheckReplicaPayments) {
    try {
      Optional<BasketTo> basketTo = storageService.getObject(StorageService.WS_USER, id, BasketTo.class);

      if (basketTo.isEmpty())
        return ResponseEntity.badRequest().body("Id not valid: " + id);
      else {
        if (StShowMyPay.NONE.value().equals(basketTo.get().getShowMyPay())) {
          //showMyPay NONE -> skip UI and go directly to WISP

          //check Replica Payments (SPAC #261)
          if ("ko".equals(overrideCheckReplicaPayments)) {
            //case user don't want to proceed with replica payment
            // go back to back url provided in the basket and notify error
            String backUrl = UriComponentsBuilder
                .fromUriString(basketTo.get().getBackUrlInviaEsito())
                .queryParam("idSession", basketTo.get().getIdSession())
                .queryParam("esito", "ERROR")
                .encode()
                .toUriString();
            return Utilities.redirectTo(backUrl);
          } else {
            boolean skipOverrideCheckReplicaPayments = false;
            if(StringUtils.isNotBlank(overrideCheckReplicaPayments))
              switch(landingService.validateParamCheckReplicaPayments(id, overrideCheckReplicaPayments)){
                case INVALID:
                  throw new MyPayException("invalid overrideCheckReplicaPayments");
                case EXPIRED:
                  log.info("expired overrideCheckReplicaPayments [{}], ignoring it", overrideCheckReplicaPayments);
                  break;
                case VALID:
                  skipOverrideCheckReplicaPayments = true;
                  break;
              }

            if (!skipOverrideCheckReplicaPayments) {
              //case we have to make replica payment check
              switch (manageReplicaDovutoImpl(basketTo.get())) {
                case DOVUTO:
                  return Utilities.redirectTo(landingService.getUrlPaymentReplica(id, "dovuto"));
                case DOVUTO_ELAB:
                  return Utilities.redirectTo(landingService.getUrlPaymentReplica(id, "elab"));
                case NO_REPLICA: //nothing to do
              }
            }
          }

          //we don't have payment replica, or we have it but used decided to proceed with payment
          EsitoTo esitoTo = paymentManagerService.checkoutCarrello(null, basketTo.get());
          if(!StringUtils.equals(esitoTo.getEsito(),Constants.STATO_ESITO_OK)){
            log.error("EsitoTo checkoutCarrello: {}", ReflectionToStringBuilder.toString(esitoTo));
            throw new MyPayException("error during checkout: "+Utilities.ifNotNull(esitoTo.getFaultBean(), FaultBean::getFaultCode));
          } else {
            log.debug("EsitoTo checkoutCarrello: {}", ReflectionToStringBuilder.toString(esitoTo));
          }
          return Utilities.redirectTo(esitoTo.getUrl());
        } else {
          //showMyPay !NONE -> go to UI

          //generate security token to download avviso
          basketTo.get().getItems().stream()
              .filter(cartItem -> StringUtils.isNotBlank(cartItem.getCodIuv()) && cartItem.getId()!=null)
              .forEach(cartItem -> cartItem.setSecurityTokenAvviso(
                  jwtTokenUtil.generateSecurityToken(null, ""+cartItem.getId())));

          ContentStorage.StorageToken tokenFe = storageService.putObject(StorageService.WS_USER, basketTo);

          //redirect to UI
          return Utilities.redirectTo(appFeCittadinoAbsolutePath + "/landing/inviaDovuti?id=" + tokenFe.getId());
        }

      }
    } catch (Exception e) {
      log.error("landingInviaDovuti", e);
      throw new MyPayException("system error", e);
    }
  }

  private REPLICA_TYPE manageReplicaDovutoImpl(BasketTo basketTo){
    //case we have to make replica payment check
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
      return REPLICA_TYPE.DOVUTO;
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
      log.debug("found DOVUTO_ELABORATO payment replica for cart item : {}" , replicaDovutoElaborato.get());
      return REPLICA_TYPE.DOVUTO_ELAB;
    }
    return REPLICA_TYPE.NO_REPLICA;
  }

  @GetMapping(ANONYMOUS_PATH + "/esitoPagamento")
  public ResponseEntity<?> landingPaymentResponse(@RequestParam String idSession, @RequestParam String esito) {
    if (StringUtils.isBlank(idSession)) {
      log.error("idSession parameter is mandatory");
      throw new BadRequestException("idSession parameter is mandatory");
    }
    try {
      CarrelloMultiBeneficiario multiCart = carrelloMultiBeneficiarioService.getByIdSessionFesp(idSession).orElseThrow(NotFoundException::new);
      Ente enteCaller = Optional.ofNullable(enteService.getEnteByCodIpa(multiCart.getCodIpaEnte())).orElseThrow(NotFoundException::new);
      var cart = carrelloService.getByMultiBeneficarioId(multiCart.getMygovCarrelloMultiBeneficiarioId()).orElseThrow(NotFoundException::new);
      try {
        giornaleService.registraEvento(new Date(), cart.getCodRpSilinviarpIdDominio(),
          cart.getCodRpDatiVersIdUnivocoVersamento(), cart.getCodRpSilinviarpCodiceContestoPagamento(),
          cart.getCodRpSilinviarpIdPsp(), cart.getCodRpDatiVersTipoVersamento(), Constants.COMPONENTE_WFESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_PA.paaSILInviaRispostaPagamentoCarrel.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
          Constants.NODO_REGIONALE_FESP, cart.getCodRpSilinviarpIdDominio(),
          identificativoStazioneIntermediarioPa, cart.getCodRpSilinviarpIdCanale(),
          idSession+" - "+esito,
          StringUtils.equalsIgnoreCase(esito,"OK") ? Constants.GIORNALE_ESITO_EVENTO.OK.toString() : Constants.GIORNALE_ESITO_EVENTO.KO.toString());
      } catch (Exception e) {
        log.warn("paaSILInviaRispostaPagamentoCarrel [RESPONSE] impossible to insert in the event log", e);
      }

      String redirectUrl = null;
      String idSessionCart = multiCart.getIdSessionCarrello();
      String esitoParam = Utilities.ifEqualsOrElse("OK", "ERROR").apply(esito);
      String backUrl = StringUtils.firstNonBlank(multiCart.getRispostaPagamentoUrl(), enteCaller.getEnteSilInviaRispostaPagamentoUrl());
      if(StringUtils.isNotBlank(backUrl)){
        String pollingToken = jwtTokenUtil.generatePollingToken(null, multiCart.getMygovCarrelloMultiBeneficiarioId()+"");
        try {
          redirectUrl = UriComponentsBuilder
            .fromUriString(backUrl)
            .queryParam("idSession", idSessionCart)
            .queryParam("esito", esitoParam)
            .queryParam("pollingToken", pollingToken)
            .encode()
            .toUriString();
        } catch(Exception e){
          log.warn("error when setting the redirectUrl to url[{}], ignoring the redirect", backUrl, e);
        }
      }
      if(StringUtils.isBlank(redirectUrl)) {
        String msgCode = null;
        switch (multiCart.getMygovAnagraficaStatoId().getCodStato()){
          case Constants.STATO_CARRELLO_PAGATO: msgCode = "pa.esito.ok"; break;
          case Constants.STATO_CARRELLO_PARZIALMENTE_PAGATO: msgCode = "pa.esito.okko"; break;
          case Constants.STATO_CARRELLO_NON_PAGATO:
          case Constants.STATO_CARRELLO_PREDISPOSTO:
            msgCode = "pa.esito.ko"; break;
          case Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO:
            if ("OK".equals(esito))
              msgCode = "pa.esito.naok";
            else if (List.of(Constants.STATO_EVTIPO_ERROR_NODO_FESP, Constants.STATO_EVTIPO_ERROR_NODO_SPC).contains(esito))
              msgCode = "pa.esito.nako";
        }
        String msgEsito = msgCode!=null? messageSource.getMessage(msgCode, null, Locale.ITALY) : null;
        EsitoTo esitoTo = EsitoTo.builder()
            .esito(esitoParam)
            .returnMsg(msgEsito)
            .build();
        ContentStorage.StorageToken tokenFe = storageService.putObject(StorageService.WS_USER, esitoTo);
        redirectUrl = appFeCittadinoAbsolutePath + "/landing/esitoPagamento?id=" + tokenFe.getId();
      }
      return Utilities.redirectTo(redirectUrl);
    } catch (Exception e) {
      log.error("landingPaymentResponse", e);
      throw new MyPayException("system error", e);
    }
  }

  @GetMapping(PAYMENT_OUTCOME_URL+"/{outcome}")
  public ResponseEntity<?> landingPaymentResponseFromCheckout(@PathVariable String outcome, @RequestParam String id) {
    if (StringUtils.isBlank(id)) {
      log.error("idSession parameter is mandatory");
      throw new BadRequestException("idSession parameter is mandatory");
    }
    Optional<CartInfo> optionalCartInfo = storageService.getObject(StorageService.WS_USER, id, CartInfo.class);
    try{
      String esitoParam;
      switch(outcome.toUpperCase()){
        case "OK": //successful payment
          esitoParam = outcome.toUpperCase();
          break;
        case "KO": //error
        case "CN": //canceled by user
          esitoParam = "ERROR";
          optionalCartInfo.ifPresent(cartInfo -> cartInfo.getMygovDovutoIdIuvVolatiliList()
            .stream()
            .map(dovutoService::getById)
            .forEach(dovutoService::eraseCheckoutCartsExpired));
          break;
        default:
          throw new BadRequestException(String.format("landingPaymentResponseFromCheckout, invalid outcome[%s]", outcome));
      }
      String redirectUrl = null;
      if (optionalCartInfo.isPresent()) {
        CartInfo cartInfo = optionalCartInfo.get();
        Ente enteCaller = Optional.ofNullable(enteService.getEnteByCodIpa(cartInfo.getCodIpaEnteCaller())).orElseThrow(NotFoundException::new);
        String idCart = cartInfo.getIdCart();
        String backUrl = StringUtils.firstNonBlank(cartInfo.getBackUrl(), enteCaller.getEnteSilInviaRispostaPagamentoUrl());

        if(StringUtils.isNotBlank(backUrl)){
          String pollingToken = jwtTokenUtil.generatePollingToken(null, String.valueOf(cartInfo.getIdCart()));
          try {
            redirectUrl = UriComponentsBuilder
                .fromUriString(backUrl)
                .queryParam("idSession", idCart)
                .queryParam("esito", esitoParam)
                .queryParam("pollingToken", pollingToken)
                .encode()
                .toUriString();
          } catch(Exception e){
            log.warn("error when setting the redirectUrl to url[{}], ignoring the redirect", backUrl, e);
          }
        }
      }
      if(StringUtils.isBlank(redirectUrl)) {
        String msgEsito = messageSource.getMessage(outcome.equalsIgnoreCase(Constants.STATO_ESITO_OK) ? "pa.esito.naok" : "pa.esito.nako", null, Locale.ITALY);
        EsitoTo esitoTo = EsitoTo.builder()
            .esito(esitoParam)
            .returnMsg(msgEsito)
            .build();
        ContentStorage.StorageToken tokenFe = storageService.putObject(StorageService.WS_USER, esitoTo);
        redirectUrl = appFeCittadinoAbsolutePath + "/landing/esitoPagamento?id=" + tokenFe.getId();
      }
      return Utilities.redirectTo(redirectUrl);
    } catch (Exception e) {
      log.error("landingPaymentResponse", e);
      throw new MyPayException("system error", e);
    }
  }

  @GetMapping(ANONYMOUS_PATH + "/precaricato")
  public ResponseEntity<?> landingPrecaricatoAnonimoEnte(
      @RequestParam String id,
      @RequestParam String backUrl,
      @RequestParam(required = false) String email) {
    try {
      if(StringUtils.isBlank(id))
        throw new BadRequestException("id parameter is mandatory");
      if(StringUtils.isBlank(backUrl))
        throw new BadRequestException("backUrl parameter is mandatory");

      CartItem item = dovutoService.getByIdSession(id)
          .map(dovuto -> CartItem.builder()
              .id(dovuto.getMygovDovutoId())
              .codStato(dovuto.getMygovAnagraficaStatoId().getCodStato())
              .codIpaEnte(dovuto.getNestedEnte().getCodIpaEnte())
              .codTipoDovuto(dovuto.getCodTipoDovuto())
              .causale(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento())
              .causaleVisualizzata(dovuto.getDeCausaleVisualizzata())
              .importo(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento())
              .datiSpecificiRiscossione(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione())
              .iud(dovuto.getCodIud())
              .codIuv(dovuto.getCodIuv())
              .intestatario(AnagraficaPagatore.builder()
                  .anagrafica(dovuto.getDeRpSoggPagAnagraficaPagatore())
                  .tipoIdentificativoUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco())
                  .codiceIdentificativoUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco())
                  .email(Utilities.getDefaultString().apply(dovuto.getDeRpSoggPagEmailPagatore(), email))
                  .nazione(dovuto.getCodRpSoggPagNazionePagatore())
                  .provincia(dovuto.getDeRpSoggPagProvinciaPagatore())
                  .localita(dovuto.getDeRpSoggPagLocalitaPagatore())
                  .cap(dovuto.getCodRpSoggPagCapPagatore())
                  .indirizzo(dovuto.getDeRpSoggPagIndirizzoPagatore())
                  .civico(dovuto.getDeRpSoggPagCivicoPagatore())
                  .build())
              .build())
          .orElseThrow(() -> new BadRequestException(String.format("dovuto non trovato per idSession = %s", id)));
      BasketTo basketTo = BasketTo.builder()
          .idSession(id)
          .backUrlInviaEsito(backUrl)
          .showMyPay(StShowMyPay.NONE.value())
          .tipoCarrello(Constants.TIPO_CARRELLO_PRECARICATO_ANONIMO_ENTE)
          .items(Collections.singletonList(item))
          .build();
      EsitoTo esitoTo = paymentManagerService.checkoutCarrello(null, basketTo);
      log.debug("EsitoTo checkoutCarrello: {}", ReflectionToStringBuilder.toString(esitoTo));
      return Utilities.redirectTo(esitoTo.getUrl());
    } catch (Exception e) {
      log.error("landingPrecaricatoAnonimoEnte", e);
      throw new MyPayException(messageSource.getMessage("pa.errore.internalError", null, Locale.ITALY));
    }
  }

  @GetMapping(ANONYMOUS_PATH + "/avviso")
  public ResponseEntity<Resource> downloadAvviso(
      @RequestParam Long id,
      @RequestParam String securityToken) throws Exception {
    return avvisoController.downloadAvviso(null, id, securityToken);
  }

  // replicate behaviour of MyPay3 "scaricaAvviso.html"
  @GetMapping(ANONYMOUS_PATH + LEGACY + "/avviso")
  public ResponseEntity<Resource> downloadAvvisoLegacy(
      @RequestParam(name = "iuv") String numeroAvviso,
      @RequestParam(name = "ente") String codIpaEnte,
      @RequestParam(name = "anagrafica", required = false) String anagrafica) throws ParseException {

    numeroAvviso = StringUtils.strip(numeroAvviso);
    anagrafica = StringUtils.strip(anagrafica);

    //check if anagrafica may be missing (depending on configured threshold date)
    Date tresholdDate = DateUtils.addDays(dateFormatDdMmYyyy.parse(dataScaricaAvvisoAnagraficaString), 1);
    if (StringUtils.isBlank(anagrafica) && tresholdDate.before(new Date()))
      throw new ValidatorException("Anagrafica obbligatoria.");

      // check that ente exists
    Ente ente = Optional.of(enteService.getEnteByCodIpa(codIpaEnte)).orElseThrow(NotFoundException::new);
    // convert numeroAvviso to IUV
    String iuv = Utilities.numeroAvvisoToIuvValidator(numeroAvviso);
    // check if a dovuto exists with ente/IUV
    List<Dovuto> dovutoList = dovutoService.getByIuvEnte(iuv, codIpaEnte);
    if(!dovutoList.isEmpty()) {
      // take the first (in theory it's not possible to have more than one)
      Dovuto dovuto = dovutoList.get(0);

      //check that anagrafica passed in request is the same on DB
      if(!StringUtils.isBlank(anagrafica) && !StringUtils.equalsIgnoreCase(dovuto.getDeRpSoggPagAnagraficaPagatore(),anagrafica))
        throw new NotFoundException("Avviso non trovato");

      switch(dovuto.getMygovAnagraficaStatoId().getCodStato()){
        case Constants.STATO_DOVUTO_DA_PAGARE:
          // check if expired
          Optional<EnteTipoDovuto> enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), codIpaEnte, true);
          if ( enteTipoDovuto.isEmpty() ){
            return ResponseEntity.badRequest().body(
                new ByteArrayResource(messageSource.getMessage("pa.dovuto.nonpagabile", null, Locale.ITALY).getBytes()));
          } else if ( enteTipoDovuto.get().isFlgScadenzaObbligatoria() &&
            dovuto.getDtRpDatiVersDataEsecuzionePagamento() != null &&
            Utilities.toLocalDate(dovuto.getDtRpDatiVersDataEsecuzionePagamento()).isBefore(Utilities.toLocalDate(new Date())) ) {
            return ResponseEntity.badRequest().body(new ByteArrayResource(
                messageSource.getMessage("pa.dovuto.scaduto", null, Locale.ITALY).getBytes()));
          } else {
            return avvisoController.downloadAvvisoImpl(dovuto);
          }
        case Constants.STATO_DOVUTO_SCADUTO:
          return ResponseEntity.badRequest().body(new ByteArrayResource(
              messageSource.getMessage("pa.dovuto.scaduto", null, Locale.ITALY).getBytes()));
        case Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO:
          return ResponseEntity.badRequest().body(new ByteArrayResource(
              messageSource.getMessage("pa.dovuto.inCorsoPagamento", null, Locale.ITALY).getBytes()));
        default:
          return throwInternalError("invalid dovuto state: "+dovuto.getMygovAnagraficaStatoId().getCodStato());
      }
    } else {
      List<DovutoElaborato> dovutoElaboratoAllList = dovutoElaboratoService.searchDovutoElaboratoByIuvEnte(iuv, ente.getCodIpaEnte());

      List<DovutoElaborato> dovutoElaboratoList = new ArrayList<>();
      if(!dovutoElaboratoAllList.isEmpty()) {
        List<DovutoElaborato> listDovutoKO = new ArrayList<>();
        for(DovutoElaborato dovutoElaborato : dovutoElaboratoAllList) {
          Boolean carrelloOK = Utilities.verifyCarrelloloOK(carrelloService.getById(dovutoElaborato.getMygovCarrelloId().getMygovCarrelloId()));
          if (carrelloOK==Boolean.TRUE){
            dovutoElaboratoList.add(dovutoElaborato);
          }
          else if (carrelloOK==Boolean.FALSE){
            listDovutoKO.add(dovutoElaborato);
          }
        }
        if (dovutoElaboratoList.isEmpty() && !listDovutoKO.isEmpty()) {
            dovutoElaboratoList.addAll(listDovutoKO);
        }
      }

      if(!dovutoElaboratoList.isEmpty()) {
        //check all dovutoElaborato in same carrello
        Carrello carrello = null;
        for(DovutoElaborato dovutoElaborato : dovutoElaboratoList) {
          Carrello otherCarrello = carrelloService.getById(dovutoElaborato.getMygovCarrelloId().getMygovCarrelloId());
          if (otherCarrello != null && carrello != null && !carrello.getMygovCarrelloId().equals(otherCarrello.getMygovCarrelloId()))
            return throwInternalError("multiple id carrello");
          else if (carrello == null)
            carrello = otherCarrello;
        }
        //check state of carrello
        if(carrello!=null && (Constants.STATO_CARRELLO_PAGATO.equals(carrello.getMygovAnagraficaStatoId().getCodStato()) ||
            Constants.STATO_CARRELLO_NON_PAGATO.equals(carrello.getMygovAnagraficaStatoId().getCodStato()) ||
            Constants.STATO_CARRELLO_DECORRENZA_TERMINI.equals(carrello.getMygovAnagraficaStatoId().getCodStato()) ||
            Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE.equals(carrello.getMygovAnagraficaStatoId().getCodStato()) ) ) {

          //check that anagrafica passed in request is the same on DB
          if(!StringUtils.isBlank(anagrafica) && !StringUtils.equalsIgnoreCase(dovutoElaboratoList.get(0).getDeRpSoggPagAnagraficaPagatore(),anagrafica))
            return ResponseEntity.badRequest().body(new ByteArrayResource("Avviso non trovato".getBytes()));

          return this.generateReceipt(carrello);
        } else {
          return throwInternalError("invalid carrello state");
        }
      } else {
        return ResponseEntity.badRequest().body(new ByteArrayResource("Avviso non trovato".getBytes()));
      }
    }
  }

  @GetMapping(ANONYMOUS_PATH + "/rt")
  public ResponseEntity<Resource> downloadReceipt(@RequestParam String id) {
    Carrello carrello = Optional.ofNullable(carrelloService.getByIdMessaggioRichiesta(id)).orElseThrow(NotFoundException::new);
    return this.generateReceipt(carrello);
  }

  @GetMapping(ANONYMOUS_PATH + "/flussi-export")
  public ResponseEntity<?> downloadFlussiExport(@RequestParam String codRequestToken) {
    ExportDovuti exportDovuti = Optional.ofNullable(exportDovutiService.getFlussoExport(codRequestToken))
      .filter(ed -> ed.getMygovAnagraficaStatoId().getCodStato().equals(Constants.STATO_EXPORT_ESEGUITO))
      .orElseThrow(NotFoundException::new);

    String path = exportDovuti.getMygovEnteId().getCodIpaEnte();
    String filename = exportDovuti.getDeNomeFileGenerato();

    Pair<Resource, Long> file = myBoxService.downloadFile(path, filename);
    if(file==null)
      throw new NotFoundException("File \""+filename+"\" non disponibile");

    String realFileName = FilenameUtils.getName(filename);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+realFileName);
    log.debug("downloading file with path[{}], filename[{}] - length[{} bytes]", path, filename, file.getRight());
    return ResponseEntity.ok()
      .headers(headers)
      .contentLength(file.getRight())
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(file.getLeft());
  }

  private ResponseEntity<Resource> throwInternalError(String hint) {
    long errorId = System.currentTimeMillis();
    log.warn("throw internal error ("+hint+") id="+errorId+", error caused at: ", new Exception());
    String msg = "Errore di sistema ["+errorId+"]";
    return ResponseEntity.badRequest().body(new ByteArrayResource(msg.getBytes()));
  }

  private ResponseEntity<Resource> generateReceipt(Carrello carrello) {
    try(ByteArrayOutputStream outputStream = jasperService.generateRicevuta(carrello)) {

      String filename = Utilities.getFilenameRt(carrello.getCodRpDomIdDominio(), carrello.getValidIuv());

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
      headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");

      InputStream inputStreamReport = new ByteArrayInputStream(outputStream.toByteArray());

      return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.APPLICATION_PDF)
        .body(new InputStreamResource(inputStreamReport));

    } catch (Exception e) {
      String error = String.format("Si e verificato un errore nella generazione della ricevuta telematica per il carrello con id: %s", carrello.getMygovCarrelloId());
      log.error(error, e);
      throw new MyPayException(error, e);
    }
  }


}
