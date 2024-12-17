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

import it.regioneveneto.mygov.payment.mypay4.bo.CarrelloBo;
import it.regioneveneto.mygov.payment.mypay4.controller.fesp.LandingController;
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaPagatore;
import it.regioneveneto.mygov.payment.mypay4.dto.BasketTo;
import it.regioneveneto.mygov.payment.mypay4.dto.CartItem;
import it.regioneveneto.mygov.payment.mypay4.dto.EsitoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.common.Psp;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.Cart;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.CartInfo;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.PaymentInfo;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.PaymentNotice;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.ReturnUrls;
import it.regioneveneto.mygov.payment.mypay4.exception.PaymentOrderException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.CarrelloMultiBeneficiario;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.CheckoutService;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.PaymentNoticeService;
import it.regioneveneto.mygov.payment.mypay4.storage.ContentStorage;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Constants.TRIGGER_PAYMENT;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.pagamenti.pa.PaaTipoDatiPagamentoPA;
import it.veneto.regione.schemas._2012.pagamenti.CtDatiVersamentoRP;
import it.veneto.regione.schemas._2012.pagamenti.EntiSecondariRP;
import it.veneto.regione.schemas._2012.pagamenti.RP;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtDatiVersamentoDovutiEntiSecondari;
import it.veneto.regione.schemas._2012.pagamenti.ente.DovutiEntiSecondari;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.PAA_SYSTEM_ERROR;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PaymentManagerService {

	@Resource
	private PaymentManagerService self;
	@Autowired
	private SpontaneoService spontaneoService;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private JAXBTransformService jaxbTransformService;
	@Autowired
	private DovutoCarrelloService dovutoCarrelloService;
	@Autowired
	private CarrelloService carrelloService;
	@Autowired
	private CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;
	@Autowired
	private DovutoService dovutoService;
	@Autowired
	private EnteTipoDovutoService enteTipoDovutoService;
	@Autowired
	private EnteService enteService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private StorageService storageService;
	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private PaymentNoticeService paymentNoticeService;

	@Value("${pa.codIpaEntePredefinito}")
	private String codIpaEnteDefault;

	@Value("${pa.modelloUnico}")
	private boolean modelloUnico;

	@Value("${app.be.absolute-path}")
	private String appBeAbsolutePath;

	private static final String SPONTANEOUS = "s";
	private static final String DEBITS = "d";

	@Transactional(propagation = Propagation.REQUIRED)
	public EsitoTo checkoutCarrello(UserWithAdditionalInfo user, BasketTo basketTo){

		if (basketTo == null || basketTo.getItems().isEmpty()) {
			log.error("Invalid Carrello - empty cart");
			throw new ValidatorException(messageSource.getMessage("pa.messages.carrelloVuoto", null, Locale.ITALY));
		}
		if (basketTo.getItems().size() > 5) {
			log.error("Invalid Carrello - more than 5 elements");
			throw new ValidatorException(messageSource.getMessage("pa.messages.carrelloContieneTroppiDovuti", null, Locale.ITALY));
		}
		if (!modelloUnico && basketTo.getDovutiEntiSecondari() != null && basketTo.getItems().size() > 1) {
			log.error("Invalid Carrello - if multiBeneficiario is present the cart size must be equals to 1");
			throw new ValidatorException(messageSource.getMessage("pa.messages.carrellomultiBeneficiarioInvalidSize", null, Locale.ITALY));
		}

		//optimization to not repeat multiple times query of dovuto
		List<Dovuto> paymentsWithIuv = new ArrayList<>();
		basketTo.getItems().forEach(item -> {
			if (item.getId()==null) {
				spontaneoService.validateCart(item);
				item.setCodIuv(null);
			} else {
				Dovuto dovuto = this.debitValidation(item);
				if(StringUtils.isNotBlank(dovuto.getCodIuv())) {
					item.setCodIuv(dovuto.getCodIuv());
					paymentsWithIuv.add(dovuto);
				}
			}
		});

		if( Constants.TIPO_CARRELLO_DEFAULT.equals(basketTo.getTipoCarrello()) ||
			Constants.TIPO_CARRELLO_DEFAULT_CITTADINO.equals(basketTo.getTipoCarrello()) ||
			Constants.TIPO_CARRELLO_CITTADINO_ANONYMOUS.equals(basketTo.getTipoCarrello()) ||
			Constants.TIPO_CARRELLO_ESTERNO_ANONIMO.equals(basketTo.getTipoCarrello()) ||
			Constants.TIPO_CARRELLO_AVVISO_ANONIMO_ENTE.equals(basketTo.getTipoCarrello()) )
			dovutoService.validateAndNormalizeAnagraficaPagatore(AnagraficaPagatore.TIPO.Versante, basketTo.getVersante(), true);

		String urlEsito = basketTo.getBackUrlInviaEsito();
		String codIpaEnteCaller = StringUtils.defaultString(basketTo.getEnteCaller(), codIpaEnteDefault);

		if(modelloUnico){
			boolean hasCartIdSession = StringUtils.isNotBlank(basketTo.getIdSession());
			basketTo.setIdSession(Optional.ofNullable(basketTo.getIdSession()).orElseGet(Utilities::getRandomUUIDWithTimestamp));
			Cart pagoPaCart = new Cart();
			pagoPaCart.setAllCCP(false);
			String emailNotice = StringUtils.firstNonBlank(
					Utilities.ifNotNull(basketTo.getVersante(), AnagraficaPagatore::getEmail),
					basketTo.getItems().stream().map(CartItem::getIntestatario).filter(Objects::nonNull).map(AnagraficaPagatore::getEmail).filter(StringUtils::isNotBlank).findFirst().orElse(null) );
			pagoPaCart.setEmailNotice(emailNotice);
			pagoPaCart.setIdCart(String.join(Constants.DASH_SEPARATOR, Constants.ID_CART_PREFIX, basketTo.getIdSession()));
			pagoPaCart.setPaymentNotices(new ArrayList<>());

			List<Long> mygovDovutoIdList = new ArrayList<>();
			List<PaymentInfo> paymentsWithoutIuv = paymentNoticeService.filterPaymentsWithoutIuv(basketTo);
			for (PaymentInfo paymentInfo : paymentsWithoutIuv) {
				log.debug("checkout - loop createPaymentNoticeByPaymentInfo processing started");
				paymentNoticeService.createDebit(paymentInfo);
				paymentInfo.getItems().stream().map(CartItem::getId).forEach(mygovDovutoIdList::add);
				PaymentNotice paymentNotice = paymentNoticeService.createPaymentNoticeByPaymentInfo(paymentInfo);
				pagoPaCart.getPaymentNotices().add(paymentNotice);
				log.debug("checkout - loop createPaymentNoticeByPaymentInfo processing ended");
			}

			for (Dovuto dovuto : paymentsWithIuv) {
				log.debug("checkout - loop createPaymentNoticeByDebit processing started");
				PaymentNotice paymentNotice = paymentNoticeService.createPaymentNoticeByDebit(dovuto);
				pagoPaCart.getPaymentNotices().add(paymentNotice);
				log.debug("checkout - loop createPaymentNoticeByDebit processing ended");
			}

			carrelloMultiBeneficiarioService.insertCarrelloMultiBeneficiario(codIpaEnteCaller, Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO, urlEsito, basketTo.getIdSession(), hasCartIdSession);
			CartInfo cartInfo = CartInfo.builder()
				.mygovDovutoIdIuvVolatiliList(mygovDovutoIdList)
				.paymentNotices(pagoPaCart.getPaymentNotices())
				.idCart(basketTo.getIdSession())
				.codIpaEnteCaller(codIpaEnteCaller)
				.backUrl(urlEsito)
				.build();

			ContentStorage.StorageToken tokenCartInfo = storageService.putObject(StorageService.WS_USER, cartInfo);

			log.info("sending cart to checkout, cartId[{}] tokenId[{}]", cartInfo.getIdCart(), tokenCartInfo.getId());
			pagoPaCart.setReturnUrls(ReturnUrls.builder()
					.returnOkUrl(appBeAbsolutePath + LandingController.PAYMENT_OUTCOME_URL+"/ok?id="+tokenCartInfo.getId())
					.returnErrorUrl(appBeAbsolutePath + LandingController.PAYMENT_OUTCOME_URL+"/ko?id="+tokenCartInfo.getId())
					.returnCancelUrl(appBeAbsolutePath + LandingController.PAYMENT_OUTCOME_URL+"/cn?id="+tokenCartInfo.getId())
					.build());

			return checkoutService.sendCart(pagoPaCart);
		} else {
			Map<String, List<CarrelloBo>> BOMap = self.buildPaymentBO(basketTo);
			List<CarrelloBo> carts = self.placeOrder(codIpaEnteCaller, urlEsito, BOMap, Constants.TRIGGER_PAYMENT.ON_LINE, basketTo.getDovutiEntiSecondari());
			List<RP> rpList = self.buildCtRichiestaPagamento(carts,null);
			NodoSILInviaCarrelloRP nc = self.buildInviaCarrelloRp(rpList, codIpaEnteCaller, basketTo.getDovutiEntiSecondari());
			NodoSILInviaCarrelloRPRisposta ncr = paymentService.inviaCarrelloRP(carts.get(0).getIdCarrelloMulti(), nc);
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

	public Map<String, List<CarrelloBo>> buildPaymentBO(BasketTo basketTo) {
		Map<String, List<CarrelloBo>> resultMap;
		Psp psp = carrelloService.newPspDefaultAgidValue();
		if(basketTo.getVersante()!=null && basketTo.getVersante().getTipoIdentificativoUnivoco()==Character.MIN_VALUE)
			basketTo.getVersante().setTipoIdentificativoUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));
		try {
			Function<CartItem, CarrelloBo> mapBasketToBO = item -> CarrelloBo.builder()
				.codIpaEnte(item.getCodIpaEnte())
				.intestatario(item.getIntestatario())
				.idSession(basketTo.getIdSession())
				.psp(psp)
				.codiceContestoPagamento(enteTipoDovutoService.getByCodTipo(item.getCodTipoDovuto(), item.getCodIpaEnte()).getCodiceContestoPagamento())
				.tipoCarrello(basketTo.getTipoCarrello())
				.dovuti(new ArrayList<>(Collections.singleton(item)))
				.versante(basketTo.getVersante())
				.build();

			resultMap = basketTo.getItems().stream()
				.collect(Collectors.groupingBy(item -> StringUtils.isBlank(item.getCodIuv())? SPONTANEOUS : DEBITS,
					Collectors.mapping(mapBasketToBO, Collectors.toList())));

			List<CarrelloBo> spontanousNoCCP = new ArrayList<>();
			List<CarrelloBo> spontanousWithCCP = new ArrayList<>();
			resultMap.getOrDefault(SPONTANEOUS, Collections.emptyList()).forEach(carrelloBo -> {
				if (StringUtils.isNotEmpty(carrelloBo.getCodiceContestoPagamento()))
					spontanousWithCCP.add(carrelloBo);
				else
					spontanousNoCCP.add(carrelloBo);
			});

			List<CarrelloBo> spontaneousListMapped = spontanousNoCCP.stream()
				.collect(Collectors.groupingBy(CarrelloBo::getCodIpaEnte, Collectors.groupingBy(CarrelloBo::getIntestatario, Collectors.toList())))
				.values()
				.stream()
				.map(Map::values)
				.flatMap(Collection::stream)
				.map(Collection::stream)
				.map(resultStream -> resultStream.reduce((prev, next) -> prev.addOccurrences(next.getDovuti())))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());

			resultMap.replace(SPONTANEOUS, ListUtils.union(spontanousWithCCP, spontaneousListMapped));
		} catch (Exception e) {
			log.error("buildPaymentBO - Error due creating BO", e);
			throw new PaymentOrderException(messageSource.getMessage("pa.errore.internalError", null, Locale.ITALY));
		}
		return resultMap;
	}

	@Transactional(propagation = Propagation.REQUIRED)
  public List<CarrelloBo> placeOrder(String codIpaEnteCaller, String backUrl, Map<String, List<CarrelloBo>> mappedCarts, TRIGGER_PAYMENT triggerPayment, DovutiEntiSecondari dovutiEntiSecondari) {
		List<CarrelloBo> boList = new ArrayList<>();
		CarrelloMultiBeneficiario carrelloMultiBeneficiario =null;
		//id session is fixed when cart is created through paaSILInviaDovuto or paaSILInviaCarrelloDovuti, and it's the same for every dovuto in this case
		Optional<CarrelloBo> firstCarrello = mappedCarts.values().stream().findFirst().flatMap(x -> x.stream().findFirst());
		Optional<String> idSessionFixed = firstCarrello.map(CarrelloBo::getIdSession).filter(StringUtils::isNotBlank);
		idSessionFixed.ifPresent(idSession -> {
			//check if idSession carrello is expired (without grace time)
			if(storageService.isTokenWithTimestampExpired(idSession, 0)){
				log.warn("session expired id[{}] carrello[{}]", idSession, ReflectionToStringBuilder.toString(firstCarrello.get()));
				throw new PaymentOrderException(messageSource.getMessage("pa.carrello.sessioneScadutaError", null, Locale.ITALY));
			} else
				log.debug("idSessionFixed: {}", idSession);
		});
		if (triggerPayment.equals(TRIGGER_PAYMENT.ON_LINE))
			carrelloMultiBeneficiario = carrelloMultiBeneficiarioService.insertCarrelloMultiBeneficiario(codIpaEnteCaller, Constants.STATO_CARRELLO_PREDISPOSTO, backUrl, idSessionFixed.orElse(Utilities.getRandomUUIDWithTimestamp()), idSessionFixed.isPresent());
		List<CarrelloBo> debits = mappedCarts.getOrDefault(DEBITS, Collections.emptyList());
		try {
			if (!debits.isEmpty()) {
				log.debug("placeOrder - loop insertCarrello debits processing start");
				for (CarrelloBo cartBO: debits) {
					cartBO.setTipoCarrello(StringUtils.firstNonBlank(cartBO.getTipoCarrello(), Constants.TIPO_CARRELLO_AVVISO_ANONIMO));
					cartBO.setIdSession(idSessionFixed.orElseGet(Utilities::getRandomicIdSession));
					Optional.ofNullable(carrelloMultiBeneficiario).ifPresent((obj -> cartBO.setIdCarrelloMulti(obj.getMygovCarrelloMultiBeneficiarioId())));
					Ente ente = enteService.getEnteByCodIpa(cartBO.getCodIpaEnte());
					Dovuto dovuto = dovutoService.getById(cartBO.getDovuti().get(0).getId());
					Carrello preparedCart = carrelloService.insertCarrello(dovuto, cartBO, ente, carrelloMultiBeneficiario);
					cartBO.setId(preparedCart.getMygovCarrelloId());
					dovutoService.putInCarrello(preparedCart, dovuto);
					boList.add(cartBO);
				}
				log.debug("placeOrder - loop insertCarrello debits processing end");
			}

			List<CarrelloBo> spontaneousList = mappedCarts.getOrDefault(SPONTANEOUS, Collections.emptyList());
			if (!spontaneousList.isEmpty()){
				log.debug("placeOrder - loop insertDovutoFromSpontaneo processing start");
				for (CarrelloBo cartBO: spontaneousList) {
					cartBO.setTipoCarrello(StringUtils.firstNonBlank(cartBO.getTipoCarrello(), Constants.TIPO_CARRELLO_SPONTANEO_ANONIMO));
					cartBO.setIdSession(idSessionFixed.orElseGet(Utilities::getRandomicIdSession));
					Optional.ofNullable(carrelloMultiBeneficiario).ifPresent((obj -> cartBO.setIdCarrelloMulti(obj.getMygovCarrelloMultiBeneficiarioId())));
					Ente ente = enteService.getEnteByCodIpa(cartBO.getCodIpaEnte());
					for (CartItem item : cartBO.getDovuti()) {
            var newId = dovutoService.insertDovutoFromSpontaneo(item, ente, (null != dovutiEntiSecondari), dovutiEntiSecondari, false);
						log.debug("insert Dovuto base, new Id: " + newId);
						item.setId(newId);
					}
					Dovuto dovutoTemplate = dovutoService.getById(cartBO.getDovuti().get(0).getId());
					Carrello preparedCart = carrelloService.insertCarrello(dovutoTemplate, cartBO, ente, carrelloMultiBeneficiario);
					cartBO.setId(preparedCart.getMygovCarrelloId());
					dovutoService.putInCarrello(preparedCart, cartBO.getDovuti());
					boList.add(cartBO);
				}
				log.debug("placeOrder - loop insertDovutoFromSpontaneo processing end");
			}
		} catch (PaymentOrderException poe) {
			throw poe;
		} catch (Exception e) {
			log.error("placeOrder - Error due processing insertCarrello", e);
			throw new PaymentOrderException(messageSource.getMessage("pa.carrello.salvataggioCarrelloError", null, Locale.ITALY));
		}
		return boList;
	}

	@Transactional(propagation = Propagation.REQUIRED)
  public NodoSILInviaCarrelloRP buildInviaCarrelloRp(List<RP> rpList, String codIpaEnteCaller, DovutiEntiSecondari dovutiEntiSecondari) {
		log.debug("buildInviaCarrelloRp - processing start");
		ListaRP listaRP = new ListaRP();
		try {
			for(RP ctRichiestaPagamento: rpList){
				ElementoRP elementoRP = new ElementoRP();
				CtDatiVersamentoRP ctDatiVersamentoRP = ctRichiestaPagamento.getDatiVersamento();
				elementoRP.setIdentificativoDominio(ctRichiestaPagamento.getDominio().getIdentificativoDominio());
				elementoRP.setIdentificativoUnivocoVersamento(ctDatiVersamentoRP.getIdentificativoUnivocoVersamento());
				elementoRP.setCodiceContestoPagamento(ctDatiVersamentoRP.getCodiceContestoPagamento());
				byte[] encodedRP = jaxbTransformService.marshallingAsBytes(ctRichiestaPagamento, RP.class);
				elementoRP.setRp(encodedRP);
				listaRP.getElementoRPs().add(elementoRP);

				Carrello preparedCart = carrelloService.getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamento(
					elementoRP.getIdentificativoDominio(), elementoRP.getIdentificativoUnivocoVersamento(), elementoRP.getCodiceContestoPagamento());
				CarrelloMultiBeneficiario carrelloMultiBeneficiario = preparedCart.getMygovCarrelloMultiBeneficiarioId();
				carrelloMultiBeneficiarioService.updateCarrelloMultiBeneficiarioStatus(carrelloMultiBeneficiario.getMygovCarrelloMultiBeneficiarioId(), Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO);
				carrelloService.updateCarrelloForRP(preparedCart, ctRichiestaPagamento, Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO);
				List<Dovuto> dovutiInCarrello = dovutoService.getDovutiInCarrello(preparedCart.getMygovCarrelloId());
				dovutoService.updateStatus(dovutiInCarrello, Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO);
				dovutoCarrelloService.insertRP(preparedCart, dovutiInCarrello, ctRichiestaPagamento);
			}
		} catch (Exception e) {
			log.error("buildInviaCarrelloRp - Error due processing RP", e);
			throw new PaymentOrderException(messageSource.getMessage("pa.carrello.creazioneRpError", null, Locale.ITALY));
		}
		String identificativoDominioEnteChiamante = enteService.getEnteByCodIpa(codIpaEnteCaller).getCodiceFiscaleEnte();
		NodoSILInviaCarrelloRP nc = new NodoSILInviaCarrelloRP();
		nc.setListaRP(listaRP);
		nc.setIdentificativoDominioEnteChiamante(identificativoDominioEnteChiamante);

    // Multi-beneficiary IUV management if defined (IUV_MULTI_09)
    if (null != dovutiEntiSecondari) {
      CtDatiVersamentoDovutiEntiSecondari ctDatiVersamentoDovutiEntiSecondari = dovutiEntiSecondari.getDatiVersamentoEntiSecondari();
      EntiSecondariRP entiSecondariRP = new EntiSecondariRP();
      entiSecondariRP.setCodiceFiscaleBeneficiario(ctDatiVersamentoDovutiEntiSecondari.getCodiceFiscaleBeneficiario());
      entiSecondariRP.setDenominazioneBeneficiario(ctDatiVersamentoDovutiEntiSecondari.getDenominazioneBeneficiario());
      entiSecondariRP.setIbanAccreditoBeneficiario(ctDatiVersamentoDovutiEntiSecondari.getIbanAccreditoBeneficiario());
      entiSecondariRP.setIndirizzoBeneficiario(ctDatiVersamentoDovutiEntiSecondari.getIndirizzoBeneficiario());
      entiSecondariRP.setCivicoBeneficiario(ctDatiVersamentoDovutiEntiSecondari.getCivicoBeneficiario());
      entiSecondariRP.setCapBeneficiario(ctDatiVersamentoDovutiEntiSecondari.getCapBeneficiario());
      entiSecondariRP.setLocalitaBeneficiario(ctDatiVersamentoDovutiEntiSecondari.getLocalitaBeneficiario());
      entiSecondariRP.setProvinciaBeneficiario(ctDatiVersamentoDovutiEntiSecondari.getProvinciaBeneficiario());
      entiSecondariRP.setNazioneBeneficiario(ctDatiVersamentoDovutiEntiSecondari.getNazioneBeneficiario());
      entiSecondariRP.setImportoSingoloVersamento(ctDatiVersamentoDovutiEntiSecondari.getImportoSingoloVersamento());

      ListaEntiSecondariRP listaEntiSecondariRP = new ListaEntiSecondariRP();
      ElementoEntiSecondariRP elementoEntiSecondariRP = new ElementoEntiSecondariRP();
      elementoEntiSecondariRP.setEntiSecondariRP(jaxbTransformService.marshallingAsBytes(entiSecondariRP, EntiSecondariRP.class));
      listaEntiSecondariRP.getElementoEntiSecondariRPs().add(elementoEntiSecondariRP);
      nc.setListaEntiSecondariRP(listaEntiSecondariRP);
    }

		log.debug("buildInviaCarrelloRp - processing end");
		return nc;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<RP> buildCtRichiestaPagamento(List<CarrelloBo> carts, PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA) {
		log.debug("buildCtRichiestaPagamento - processing start");
		List<RP> rpList = new ArrayList<>();
		try {
			for (CarrelloBo carrelloBo : carts) {
				Carrello preparedCart = carrelloService.getById(carrelloBo.getId());
				Ente ente = enteService.getEnteByCodIpa(carrelloBo.getCodIpaEnte());
				Date now = new Date();
				XMLGregorianCalendar currentTime = Utilities.toXMLGregorianCalendar(now);
				AnagraficaPagatore anagraficaPagatore = carrelloBo.getIntestatario();
				String iuv = paymentService.generateIUV(preparedCart, ente);
				preparedCart.setCodRpDatiVersIdUnivocoVersamento(iuv);
				String codiceContestoPagamento = paymentService.generateCCP(preparedCart, ente);
				preparedCart.setCodRpDatiVersCodiceContestoPagamento(codiceContestoPagamento);
				log.debug("buildCtRichiestaPagamento - IUV[{}] CCP[{}]", iuv, codiceContestoPagamento);
				preparedCart.setCodRpSilinviarpIdDominio(ente.getCodiceFiscaleEnte());
				preparedCart.setCodRpSilinviarpIdUnivocoVersamento(iuv);
				preparedCart.setCodRpSilinviarpCodiceContestoPagamento(codiceContestoPagamento);
				carrelloService.update(preparedCart);
				RP ctRichiestaPagamento = paymentService.createCtRichiestaPagamento(preparedCart, ente, currentTime, anagraficaPagatore, carrelloBo.getVersante(), paaTipoDatiPagamentoPA);
				log.debug("ctRichiestaPagamento has been created: {}", ReflectionToStringBuilder.toString(ctRichiestaPagamento));
				rpList.add(ctRichiestaPagamento);
			}
		} catch (Exception e) {
			log.error("buildCtRichiestaPagamento - Error due processing RP", e);
			throw new PaymentOrderException(messageSource.getMessage("pa.carrello.creazioneRpError", null, Locale.ITALY)+" ["+e.getMessage()+"]");
		}
		log.debug("buildCtRichiestaPagamento - processing end");
		return rpList;
	}

	private Dovuto debitValidation(final CartItem item) {
		String ente = item.getCodIpaEnte();
		Dovuto dovuto = null;
		try{
			//TODO: after tests, replace with getByIdForUpdate
			//log.debug("locking dovuto id[{}]", item.getId());
			dovuto = dovutoService.getByIdForUpdate(item.getId());
			//log.debug("locked dovuto id[{}]", item.getId());
		}catch(UnableToExecuteStatementException uese){
			if(uese.getCause() instanceof SQLException &&
					StringUtils.equals(((SQLException)uese.getCause()).getSQLState(), PSQLState.QUERY_CANCELED.getState())) {
				log.error("getByIdForUpdate timeout for dovuto id[{}]", item.getId());
				throw new ValidatorException(messageSource.getMessage("pa.sceltaDovuto.dovutoNonPagabile", null, Locale.ITALY));
			} else {
				throw uese;
			}
		}

		if (Objects.isNull(dovuto)) {
			log.error("Invalid Carrello: dovuto non trovato per id: " + item.getId());
			throw new ValidatorException(messageSource.getMessage("pa.messages.dovutoInesistente", null, Locale.ITALY));
		}
		if (!dovuto.getMygovAnagraficaStatoId().getCodStato().equals(Constants.STATO_DOVUTO_DA_PAGARE)) {
			log.error("Invalid Carrello: dovuto non pagabile per IdDovuto: "+ item.getId());
			throw new ValidatorException(messageSource.getMessage("pa.sceltaDovuto.dovutoNonPagabile", null, Locale.ITALY));
		}
		EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), ente, true)
			.orElseThrow(() -> new ValidatorException(messageSource.getMessage("pa.messages.dovutoDisabilitato", null, Locale.ITALY)));
		if (enteTipoDovuto.isFlgScadenzaObbligatoria() &&
				LocalDate.now().isAfter(Utilities.toLocalDate(dovuto.getDtRpDatiVersDataEsecuzionePagamento()))) {
			log.error("Invalid Carrello: dovuto scaduto per IdDovuto: "+ item.getId());
			throw new ValidatorException(messageSource.getMessage("pa.messages.dovutoScaduto", null, Locale.ITALY));
		}
		dovutoService.validateAndNormalizeAnagraficaPagatore(AnagraficaPagatore.TIPO.Pagatore, item.getIntestatario(), enteTipoDovuto.isFlgCfAnonimo());

		return dovuto;
	}
}