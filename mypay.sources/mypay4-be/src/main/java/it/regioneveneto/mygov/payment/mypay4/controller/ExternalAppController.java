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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.dto.externalapp.CodeDescriptionTo;
import it.regioneveneto.mygov.payment.mypay4.dto.externalapp.DovutoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.externalapp.EnteTo;
import it.regioneveneto.mygov.payment.mypay4.dto.externalapp.*;
import it.regioneveneto.mygov.payment.mypay4.dto.externalapp.EsitoPagamentoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.*;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.storage.ContentStorage;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.ListWithCount;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.pagamenti.ente.StShowMyPay;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.PastOrPresent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@Tag(name = "API Front-end esterno", description = "API esposte per front-end esterni")
@SecurityRequirements
@RestController
@Slf4j
@ConditionalOnWebApplication
public class ExternalAppController {

  private static final String AUTHENTICATED_PATH = "app";
  private static final String PAYMENT_PATH = "pagamento";
  private static final String A2A_PATH = MyPay4AbstractSecurityConfig.PATH_A2A + "/" + AUTHENTICATED_PATH;

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;
  @Value("${app.fe.cittadino.absolute-path}")
  private String appFeCittadinoAbsolutePath;
  @Value("${a2a.externalapp.list}")
  private String externalAppNameList;

  @Autowired
  private EnteService enteService;
  @Autowired
  private it.regioneveneto.mygov.payment.mypay4.service.fesp.EnteService enteFespService;
  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;
  @Autowired
  private DovutoService dovutoService;
  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;
  @Autowired
  private JwtTokenUtil jwtTokenUtil;
  @Autowired
  private AvvisoController avvisoController;
  @Autowired
  private PagatoController pagatoController;
  @Autowired
  private MailValidationService mailValidationService;
  @Autowired
  private CarrelloController carrelloController;
  @Autowired
  private PaymentManagerService paymentManagerService;
  @Autowired
  private StorageService storageService;

  @Autowired
  private AnagraficaSoggettoService anagraficaSoggettoService;

  private Set<String> segregationCodeSet;

  @PostConstruct
  public void init(){
    segregationCodeSet = enteFespService.getAllEnti().stream().map(it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente::getCodCodiceSegregazione).collect(Collectors.toUnmodifiableSet());
  }


  @Operation(summary = "Recupero degli enti con tipo dovuto spontanei",
    description = "Recupero degli enti che hanno almeno un tipo dovuto spontaneo attivo",
    responses = {@ApiResponse(description = "Lista contenente l'elenco degli enti")})
  @GetMapping(A2A_PATH + "/" + SpontaneoController.AUTHENTICATED_PATH + "/ente")
  public List<EnteTo> getEntiForSpontaneousPayment(@AuthenticationPrincipal UserWithAdditionalInfo user) {
    checkExternalAppUserAuthorized(user);

    return enteService.getAllEntiSpontanei().stream().map(ente -> EnteTo.builder()
      .code(ente.getCodIpaEnte())
      .codiceFiscale(ente.getCodiceFiscaleEnte())
      .descrizione(ente.getDeNomeEnte())
      .build()).collect(Collectors.toUnmodifiableList());
  }

  @Operation(summary = "Recupero dei tipo dovuto spontanei per ente",
    description = "Recupero dei tipo dovuto spontanei attivi per uno specifico ente",
    responses = {@ApiResponse(description = "Lista contenente l'elenco dei tipo dovuto")})
  @GetMapping(A2A_PATH + "/" + SpontaneoController.AUTHENTICATED_PATH + "/{codIpaEnte}/tipo")
  public List<TipoDovutoTo> getTipiDovutoEnteForSpontaneousPayment(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @PathVariable @Parameter(description = "Codice ente", example = "R_VENETO", required = true) String codIpaEnte) {
    checkExternalAppUserAuthorized(user);

    Long enteId = Optional.ofNullable(enteService.getEnteByCodIpa(codIpaEnte)).map(Ente::getMygovEnteId).orElseThrow(() -> new NotFoundException("ente not valid"));
    return enteTipoDovutoService.getAttiviByMygovEnteIdAndFlags(enteId, true, null).stream().map(tipoDovuto -> TipoDovutoTo.builder()
      .code(tipoDovuto.getCodTipo())
      .descrizione(tipoDovuto.getDeTipo())
      .codeEnte(codIpaEnte)
      .externalPaymentUrl(tipoDovuto.getDeUrlPagamentoDovuto())
      .build()).collect(Collectors.toUnmodifiableList());
  }

  @PostMapping(A2A_PATH + "/" + SpontaneoController.AUTHENTICATED_PATH + "/" + ExternalAppController.PAYMENT_PATH)
  public String startSpontaneousPayment(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
	  @Valid
	  @RequestBody
	  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "l'oggetto del pagamento spontaneo", required = true,
		  content = @Content(
	      schema = @Schema(implementation = PagamentoSpontaneoTo.class))) PagamentoSpontaneoTo pagamentoSpontaneoTo)  {
		checkExternalAppUserAuthorized(user);

    if(StringUtils.isNotBlank(pagamentoSpontaneoTo.getVersanteCf()) && !Utilities.isValidCFOrPIVA(pagamentoSpontaneoTo.getVersanteCf()))
		  throw new ValidatorException("Codice fiscale del debitore invalido");
	  if(enteService.getOptionalEnteByCodIpa(pagamentoSpontaneoTo.getCodiceEnte()).isEmpty())
		  throw new ValidatorException("Codice Ente invalido");
	  if(enteTipoDovutoService.getOptionalByCodTipo(pagamentoSpontaneoTo.getCodiceTipoDovuto(), pagamentoSpontaneoTo.getCodiceEnte(), true).isEmpty())
		  throw new ValidatorException("Codice tipo dovuto invalido");

    ContentStorage.StorageToken storageToken = storageService.putObject(StorageService.WS_USER, pagamentoSpontaneoTo);

    //noinspection VulnerableCodeUsages
    String redirectUrl = UriComponentsBuilder
      .fromUriString(appFeCittadinoAbsolutePath + "/landing/extAppSpontaneo")
      .queryParam("extAppToken", storageToken.getId())
      .build()
      .toUriString();

    log.debug("generated startSpontaneousPayment url[{}]", redirectUrl);
    return redirectUrl;
  }

  @Operation(summary = "Ricerca dei dovuti per codice fiscale debitore",
    description = "Ricerca dei dovuti per codice fiscale debitore e altri criteri di ricerca (range temporale data scadenza, Codice IPA ente creditore, solo dovuti pagabili)",
    responses = {@ApiResponse(description = "Lista contenente l'elenco dei dovuti")})
  @GetMapping(A2A_PATH + "/" + DovutoController.AUTHENTICATED_PATH + "/ricerca/{codiceFiscaleDebitore}")
  public ListWithCountTo<DovutoTo> searchDovuti(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @PathVariable @Parameter(description = "Codice fiscale del debitore", example = "TSTTNT80A01H501O", required = true) String codiceFiscaleDebitore,
    @RequestParam @Parameter(description = "Data di scadenza DA", example = "2023/07/23") @PastOrPresent LocalDate da,
    @RequestParam @Parameter(description = "Data di scadenza A", example = "2023/08/23") @FutureOrPresent LocalDate a,
    @RequestParam(required = false) @Parameter(description = "Codice ente IPA creditore", example = "R_VENETO") String codiceEnte,
    @RequestParam(required = false) @Parameter(description = "Codice tipo dovuto", example = "ALTRO") String tipoDovuto,
    @RequestParam(required = false) @Parameter(description = "Solo pagabili", example = "true") Boolean soloPagabili) {
    checkExternalAppUserAuthorized(user);

	  if(!Utilities.isValidCFOrPIVA(codiceFiscaleDebitore))
		  throw new ValidatorException("Codice fiscale del debitore invalido");

    ListWithCount<it.regioneveneto.mygov.payment.mypay4.dto.DovutoTo> data = dovutoService.searchDovuto(
      codiceEnte, codiceFiscaleDebitore, tipoDovuto, null, da, a, false, BooleanUtils.isTrue(soloPagabili));
    List<DovutoTo> resultData = data.stream().map(d -> toMapper(d, user)).collect(Collectors.toUnmodifiableList());
    return ListWithCountTo.<DovutoTo>builder()
      .count(data.getCount())
      .incomplete(data.isIncomplete())
      .data(resultData)
      .build();
  }

  @Operation(summary = "Ricerca dei pagati per codice fiscale debitore",
    description = "Ricerca dei pagati per codice fiscale debitore e altri criteri di ricerca (range temporale data scadenza, Codice IPA ente creditore, solo completati posistivamente)",
    responses = {@ApiResponse(description = "Lista contenente l'elenco dei pagati")})
  @GetMapping(A2A_PATH + "/" + PagatoController.AUTHENTICATED_PATH + "/ricerca/{codiceFiscaleDebitore}")
  public ListWithCountTo<PagatoTo> searchPagati(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @PathVariable @Parameter(description = "Codice fiscale del debitore", example = "TSTTNT80A01H501O", required = true) String codiceFiscaleDebitore,
    @RequestParam @Parameter(description = "Data di scadenza DA", example = "2023/07/23") @PastOrPresent LocalDate da,
    @RequestParam @Parameter(description = "Data di scadenza A", example = "2023/08/23") @FutureOrPresent LocalDate a,
    @RequestParam(required = false) @Parameter(description = "Codice ente creditore", example = "R_VENETO") String codiceEnte,
    @RequestParam(required = false) @Parameter(description = "Codice tipo dovuto", example = "ALTRO") String tipoDovuto,
    @RequestParam(required = false) @Parameter(description = "Solo completati positivamente", example = "true") Boolean soloPagati) {
    checkExternalAppUserAuthorized(user);

		if(!Utilities.isValidCFOrPIVA(codiceFiscaleDebitore))
			throw new ValidatorException("Codice fiscale del debitore invalido");

    ListWithCount<it.regioneveneto.mygov.payment.mypay4.dto.DovutoElaboratoTo> data = dovutoElaboratoService.searchDovutoElaborato(
	    codiceEnte, codiceFiscaleDebitore, da, a, null, tipoDovuto, BooleanUtils.isTrue(soloPagati) ? "pagato" : null);
    List<PagatoTo> resultData = data.stream().map(d -> toMapper(d, user)).collect(Collectors.toUnmodifiableList());
    return ListWithCountTo.<PagatoTo>builder()
      .count(data.getCount())
      .incomplete(data.isIncomplete())
      .data(resultData)
      .build();
  }

  @Operation(summary = "Verifica se un avviso di pagamento è gestito da MyPay o meno",
    description = "Verifica se un avviso di pagamento è gestito da MyPay o meno, fornendo numero avviso e codice fiscale ente creditore",
    responses = {@ApiResponse(description = "Messaggio di errore che indica se l'avviso non è gestito da MyPay (NOT_MANAGED); nessuna risposta se l'avviso è stato trovato")})
  @GetMapping(A2A_PATH + "/" + DovutoController.AUTHENTICATED_PATH + "/avviso/verifica/{numeroAvviso}/{codiceFiscaleEnteCreditore}")
  public CodeDescriptionTo verifyAvviso(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @PathVariable @Parameter(description = "Codice fiscale dell'ente creditore", example = "80007580279", required = true)  String codiceFiscaleEnteCreditore,
    @PathVariable @Parameter(description = "Numero avviso", example = "3010000000001234", required = true) String numeroAvviso
  ){
    checkExternalAppUserAuthorized(user);

    try {
      //check if numeroAvviso is formally correct
      if(!numeroAvviso.matches(Constants.NUMERO_AVVISO_PATTERN))
        throw new ValidatorException("numero avviso invalido");

      //check if segregation code is amongst the ones managed by MyPay
      var ente = enteService.getOptionalEnteByCodFiscale(codiceFiscaleEnteCreditore);

      if(ente.map(Ente::getApplicationCode)
        .filter(numeroAvviso.substring(1, 3)::equals)
        .isEmpty()){
        return CodeDescriptionTo.builder().code("NOT_MANAGED").descr("Questo numero avviso non è gestito da Mypay").build();
      }

      //formal validation numeroAvviso
      Utilities.numeroAvvisoToIuvValidator(numeroAvviso);

      return null;
    } catch(ValidatorException ve){
      return CodeDescriptionTo.builder().code("INVALID").descr(ve.getMessage()).build();
    } catch(Exception e){
      return CodeDescriptionTo.builder().code("SYSTEM_ERROR").descr(e.getMessage()).build();
    }
  }

  @Operation(summary = "Ricerca degli avvisi per codice fiscale debitore",
    description = "Ricerca degli avvisi da pagaere per codice fiscale debitore e numero avviso o IUV",
    responses = {@ApiResponse(description = "Lista contenente l'elenco degli avvisi")})
  @GetMapping(A2A_PATH + "/" + DovutoController.AUTHENTICATED_PATH + "/avviso/{numeroAvviso}")
  public List<AvvisoDovutoOrPagatoTo> searchAvviso(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @PathVariable @Parameter(description = "Numero avviso", example = "3010000000001234", required = true) String numeroAvviso,
    @RequestParam(required = false) @Parameter(description = "Codice fiscale del debitore", example = "TSTTNT80A01H501O") String codiceFiscaleDebitore,
    @RequestParam(required = false) @Parameter(description = "Anagrafica del debitore", example = "Mario Rossi") String anagraficaDebitore
  ){
    checkExternalAppUserAuthorized(user);

    // exclusive or: exactly one of codiceFiscaleDebitore and anagraficaDebitore must be empty
    if(StringUtils.isBlank(codiceFiscaleDebitore) == StringUtils.isBlank(anagraficaDebitore))
      throw new BadRequestException("exactly one of codiceFiscaleDebitore and anagraficaDebitore must be empty");
	  if(!StringUtils.isBlank(codiceFiscaleDebitore) && !Utilities.isValidCFOrPIVA(codiceFiscaleDebitore))
		  throw new ValidatorException("Codice fiscale del debitore invalido");
    //check if IUV/noticeNumber is formally correct
    String iuv = Utilities.numeroAvvisoToIuvValidator(numeroAvviso);
    //check if segregation code is amongst the ones managed by MyPay
    String segregationCode = null;
    if(numeroAvviso.length()==17)
      segregationCode = numeroAvviso.substring(0, 2);
    else if(numeroAvviso.length()==18)
      segregationCode = numeroAvviso.substring(1, 3);

    if(segregationCode!=null && !segregationCodeSet.contains(segregationCode))
      throw new ValidatorException("Invalid notice number: this notice number is not managed by MyPay");

    return getAvvisoDovutoOrPagatoTos(user, codiceFiscaleDebitore, anagraficaDebitore, iuv);
  }

  @SuppressWarnings("unchecked")
  private List<AvvisoDovutoOrPagatoTo> getAvvisoDovutoOrPagatoTos(UserWithAdditionalInfo user, String debtorFiscalCode, String debtorName, String iuv) {
    Map<String, Object> responseAvviso = avvisoController.searchAvvisoImpl(null, false, iuv,
      StringUtils.stripToNull(debtorFiscalCode), StringUtils.stripToNull(debtorName), null, null).getBody();
    if(responseAvviso==null || responseAvviso.containsKey("errorCode")){
      throw new BadRequestException(responseAvviso==null ? "response null" : (String)responseAvviso.get("errorCode"));
    }
    List<AvvisoDovutoOrPagatoTo> response = new ArrayList<>();
    if(responseAvviso.containsKey("debiti"))
      ((List<it.regioneveneto.mygov.payment.mypay4.dto.DovutoTo>) responseAvviso.get("debiti")).forEach(d -> response.add(AvvisoDovutoOrPagatoTo.builder().dovutoTo(toMapper(d, user)).build()));
    if(responseAvviso.containsKey("pagati"))
      ((List<DovutoElaboratoTo>) responseAvviso.get("pagati")).forEach(d -> response.add(AvvisoDovutoOrPagatoTo.builder().pagatoTo(toMapper(d, user)).build()));

    return response;
  }

  @Operation(summary = "Verifica la presenza di pagamenti duplicati",
    description = "verifica la presenza di possibili pagamenti duplicati (nel senso: pagamenti differenti, ma possibilmente riferiti ad uno stesso debito, avendo in comune elementi come: causale, importo, etc..)",
    responses = {@ApiResponse(description = "Informazioni sulla presenza di pagamenti duplicati")})
  @GetMapping(A2A_PATH + "/" + ExternalAppController.PAYMENT_PATH + "/duplicati/{idPagamento}")
  public ReplicaPaymentTo checkReplicaPayments(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @PathVariable @Parameter(description = "Id del dovuto da pagare restiruito dalla API di ricerca delle posizioni aperte", example = "1234", required = true) String idPagamento) {
    checkExternalAppUserAuthorized(user);

    Long mygovDovutoId = mailValidationService.parsePaymentId(idPagamento, null);
    Dovuto dovuto = dovutoService.getById(mygovDovutoId);
    if (dovuto == null)
      throw new NotFoundException("dovuto not found [" + mygovDovutoId + "]");
    else if (!StringUtils.equals(dovuto.getMygovAnagraficaStatoId().getCodStato(), Constants.STATO_DOVUTO_DA_PAGARE))
      throw new MyPayException("dovuto non pagabile [" + mygovDovutoId + "]");

    BasketTo basketTo = BasketTo.builder().items(List.of(CartItem.builder()
      .codIpaEnte(dovuto.getNestedEnte().getCodIpaEnte())
      .intestatario(anagraficaSoggettoService.getAnagraficaPagatore(dovuto))
      .causale(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento())
      .codTipoDovuto(dovuto.getCodTipoDovuto())
      .build())).build();

    Optional<String> replicaPaymentType = carrelloController.checkReplicaPayment(basketTo);

    ReplicaPaymentTo replicaPaymentTo = new ReplicaPaymentTo();
    if (replicaPaymentType.isPresent()) {
      if (StringUtils.equals(replicaPaymentType.get(), "REPLICA_DOVUTO"))
        replicaPaymentTo.setHasOngoingPaymentReplica(true);
      else
        replicaPaymentTo.setHasPaymentCompletedReplica(true);
      log.debug("checkReplicaPayment, paymentId[{}] hasReplica[{}]", idPagamento, replicaPaymentType.get());
    }
    replicaPaymentTo.setReplicaCheckPaymentId(mailValidationService.generatePaymentId(mygovDovutoId));

    return replicaPaymentTo;
  }

  @Operation(summary = "Innesco procedura di pagamento",
    description = "Inoltra la richiesta della procedura di pagamento",
    responses = {@ApiResponse(description = "Esito del pagamento")})
  @PostMapping(A2A_PATH + "/" + ExternalAppController.PAYMENT_PATH + "/start")
  public String startPayment(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @Valid
    @RequestBody
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "l'oggetto del pagamento", required = true,
      content = @Content(
        schema = @Schema(implementation = PagamentoTo.class))) PagamentoTo pagamentoTo)  {
    checkExternalAppUserAuthorized(user);

    Long mygovDovutoId = mailValidationService.parsePaymentId(pagamentoTo.getIdPagamento(), null);
    Dovuto dovuto = dovutoService.getById(mygovDovutoId);
    if (dovuto == null)
      throw new NotFoundException("dovuto not found [" + mygovDovutoId + "]");
	  if(StringUtils.isNotBlank(pagamentoTo.getVersanteCf()) && !Utilities.isValidCFOrPIVA(pagamentoTo.getVersanteCf()))
		  throw new ValidatorException("Codice fiscale del debitore invalido");
    //check replicaCheckPaymentId
    Long replicaCheckMygovDovutoId = mailValidationService.parsePaymentId(pagamentoTo.getReplicaCheckPaymentId(), 1000L * 60 * 5); //5 minutes
    if (!Objects.equals(replicaCheckMygovDovutoId, mygovDovutoId))
      throw new MyPayException("invalid replica check paymentId [" + mygovDovutoId + "/" + replicaCheckMygovDovutoId + "]");

    BasketTo basketTo = BasketTo.builder()
      .enteCaller(dovuto.getNestedEnte().getCodIpaEnte())
      .idSession(Utilities.getRandomUUIDWithTimestamp())
      .showMyPay(StShowMyPay.NONE.value())
      .tipoCarrello(Constants.TIPO_CARRELLO_AVVISO_ANONIMO_ENTE)
      .items(List.of(CartItem.builder()
        .id(dovuto.getMygovDovutoId())
        .codIuv(dovuto.getCodIuv())
        .codIpaEnte(dovuto.getNestedEnte().getCodIpaEnte())
        .codTipoDovuto(dovuto.getCodTipoDovuto())
        .intestatario(anagraficaSoggettoService.getAnagraficaPagatore(dovuto))
        .causale(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento())
        .importo(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento())
        .build()))
      .versante(AnagraficaPagatore.builder()
        .anagrafica(pagamentoTo.getVersanteAnagrafica())
        .codiceIdentificativoUnivoco(pagamentoTo.getVersanteCf())
        .tipoIdentificativoUnivoco(StringUtils.length(pagamentoTo.getVersanteCf())==11 ? 'G' : 'F')
        .email(pagamentoTo.getVersanteEmail())
        .build())
      .backUrlInviaEsito(pagamentoTo.getCallbackUrl())
      //workaround to skip mail validation when a2a interaction
      .mailValidationToken(mailValidationService.getWildcartMailValidationToken())
      .build();

    log.debug("checkoutCarrello: {}", ReflectionToStringBuilder.toString(basketTo));
    EsitoTo esitoTo = paymentManagerService.checkoutCarrello(null, basketTo);
    log.debug("EsitoTo checkoutCarrello: {}", reflectionToString(esitoTo, new RecursiveToStringStyle()));
    if(esitoTo.getFaultBean()!=null && StringUtils.isNotBlank(esitoTo.getFaultBean().getFaultCode())){
      log.error("error on checkoutCarrello: {}", reflectionToString(esitoTo.getFaultBean(), new RecursiveToStringStyle()));
      throw new MyPayException("error on checkout ["+esitoTo.getFaultBean().getFaultCode()+"]");
    }
    return esitoTo.getUrl();
  }

  @Operation(summary = "Restituisce lo stato del pagamento",
    description = "Restituisce lo stato del pagamento e, se eseguito, ulteriori informazioni dettagliate tramite token di sicurezza",
    responses = {@ApiResponse(description = "restituisce informazioni relative allo stato del pagamento")})
  @GetMapping(A2A_PATH + "/" + ExternalAppController.PAYMENT_PATH + "/esito")
  public EsitoPagamentoTo getOutcome(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @RequestParam @Parameter(description = "token alfanumerico associato al pagamento appena concluso", required = true) String pollingToken) {
    checkExternalAppUserAuthorized(user);

    PaymentOutcomeTo outcome = carrelloController.getOutcomeLoggedIn(null, pollingToken);
    return EsitoPagamentoTo.builder()
      .stato(outcome.getStatus())
      .importo(Utilities.amountAsEuroCents(outcome.getPaidAmount()))
      .versanteEmail(outcome.getEmailVersante())
      .pollingToken(outcome.getPollingToken())
      .urlDownloadRicevuta(Utilities.ifNotNull(outcome.getRt(), rtList -> rtList.stream()
        .map(rt -> getUrlDownloadReceipt(rt.getId(), rt.getSecurityToken()))
        .collect(Collectors.toUnmodifiableList())))
      .build();
  }

  @Operation(summary = "Restituisce l'avviso richiesto in formato PDF",
    description = "Restituisce l'avviso richiesto in formato PDF tramite ID e token di sicurezza",
    responses = {@ApiResponse(description = "Avviso in formato PDF")})
  @GetMapping(A2A_PATH + "/" + DovutoController.AUTHENTICATED_PATH + "/download/{id}")
  public ResponseEntity<Resource> downloadAvviso(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @PathVariable @Parameter(description = "Id della risorsa", required = true) Long id,
    @RequestParam @Parameter(description = "token alfanumerico di sicurezza associato alla richiesta di download", required = true) String securityToken) {
    checkExternalAppUserAuthorized(user);

    return avvisoController.downloadAvviso(user, id, securityToken);
  }

  @Operation(summary = "Restituisce la ricevuta telematica richiesta in formato PDF",
    description = "Restituisce la ricevuta telematica richiesta in formato PDF tramite ID e token di sicurezza",
    responses = {@ApiResponse(description = "ricevuta telematica in formato PDF")})
  @GetMapping(A2A_PATH + "/" + PagatoController.AUTHENTICATED_PATH + "/download/{id}")
  public ResponseEntity<Resource> downloadRt(
    @AuthenticationPrincipal UserWithAdditionalInfo user,
    @PathVariable @Parameter(description = "Id della risorsa", required = true) Long id,
    @RequestParam @Parameter(description = "token alfanumerico di sicurezza associato alla richiesta di download", required = true) String securityToken) {
    checkExternalAppUserAuthorized(user);

    return pagatoController.downloadImpl(id, user, securityToken);
  }

  private void checkExternalAppUserAuthorized(UserWithAdditionalInfo user) {
    if(!StringUtils.startsWith(user.getUsername(), "A2A-") || !externalAppNameList.contains(user.getUsername().substring(4))){
      log.error("invalid username[{}] trying to invoke A2A reserved method", user.getUsername());
      throw new NotAuthorizedException("Not authorized");
    }
  }

  private String getUrlDownloadPaymentNotice(it.regioneveneto.mygov.payment.mypay4.dto.DovutoTo dovuto, String securityToken) {
    if(StringUtils.isBlank(dovuto.getCodIuv()) || dovuto.isFlgIuvVolatile())
      return null;
    else
      return appBeAbsolutePath + A2A_PATH + "/" + DovutoController.AUTHENTICATED_PATH + "/download/" + dovuto.getId()
        + "?securityToken=" + URLEncoder.encode(securityToken, StandardCharsets.UTF_8);
  }

  private String getUrlDownloadReceipt(Long dovutoElaboratoId, String securityToken) {
    return appBeAbsolutePath + A2A_PATH + "/" + PagatoController.AUTHENTICATED_PATH + "/download/" + dovutoElaboratoId
      + "?securityToken=" + URLEncoder.encode(securityToken, StandardCharsets.UTF_8);
  }

  private DovutoTo toMapper(it.regioneveneto.mygov.payment.mypay4.dto.DovutoTo d, UserWithAdditionalInfo user){
    return DovutoTo.builder()
      .stato(CodeDescriptionTo.builder().code(d.getCodStato()).descr(d.getDeStato()).build())
      .numeroAvviso(StringUtils.remove(d.getNumeroAvviso(), ' ')) //remove spaces
      .iuv(d.getCodIuv())
      .ente(EnteTo.builder().code(d.getCodIpaEnte()).codiceFiscale(d.getCodiceFiscaleEnte()).descrizione(d.getDeEnte()).build())
      .tipoDovuto(TipoDovutoTo.builder().code(d.getCodTipoDovuto()).descrizione(d.getDeTipoDovuto()).build())
      .causale(StringUtils.firstNonEmpty(d.getCausaleVisualizzata(), d.getCausale()))
      .codiceFicaleIntestatario(d.getIntestatario().getCodiceIdentificativoUnivoco())
      .anagaraficaIntestatario(d.getIntestatario().getAnagrafica())
      .email(d.getIntestatario().getEmail())
      .dataScadenza(d.getDataScadenza())
      .importo(d.getImportoAsCent())
      .urlDownloadAvviso(getUrlDownloadPaymentNotice(
        d,
        jwtTokenUtil.generateSecurityToken(user, Long.toString(d.getId()), 3600L * 24) //expire in 24 hours
      ))
      .idPagamento(d.isPayable()?mailValidationService.generatePaymentId(d.getId()):null)
      .build();
  }

  private PagatoTo toMapper(DovutoElaboratoTo d, UserWithAdditionalInfo user) {
    var intestatario = d.getIntestatario().split(" - ");
    return PagatoTo.builder()
      .stato(CodeDescriptionTo.builder().code(d.getCodStato()).descr(d.getStato()).build())
      .numeroAvviso(StringUtils.remove(d.getNumeroAvviso(), ' ')) //remove spaces
      .iuv(d.getCodIuv())
      .ente(EnteTo.builder().code(d.getCodIpaEnte()).codiceFiscale(d.getCodFiscaleEnte()).descrizione(d.getEnteDeNome()).build())
      .tipoDovuto(TipoDovutoTo.builder().code(d.getCodTipoDovuto()).descrizione(d.getDeTipoDovuto()).build())
      .causale(d.getCausale())
      .anagaraficaIntestatario(intestatario[0])
      .codiceFicaleIntestatario(intestatario[1])
      .email(d.getEmail())
      .dataPagamento(d.getDataPagamento())
      .importo(d.getImportoAsCent())
      .urlDownloadRicevuta(getUrlDownloadReceipt(
        d.getId(),
        jwtTokenUtil.generateSecurityToken(user, Long.toString(d.getId()), 3600L * 24) //expire in 24 hours
      ))
      .build();
  }

}