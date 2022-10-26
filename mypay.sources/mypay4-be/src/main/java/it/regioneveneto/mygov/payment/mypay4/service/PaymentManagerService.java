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
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaPagatore;
import it.regioneveneto.mygov.payment.mypay4.dto.BasketTo;
import it.regioneveneto.mygov.payment.mypay4.dto.CartItem;
import it.regioneveneto.mygov.payment.mypay4.dto.common.Psp;
import it.regioneveneto.mygov.payment.mypay4.exception.PaymentOrderException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Constants.TRIGGER_PAYMENT;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT;
import it.veneto.regione.pagamenti.pa.PaaTipoDatiPagamentoPA;
import it.veneto.regione.schemas._2012.pagamenti.CtDatiVersamentoRP;
import it.veneto.regione.schemas._2012.pagamenti.RP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PaymentManagerService {

	@Resource
	PaymentManagerService self;
	@Autowired
	SpontaneoService spontaneoService;
	@Autowired
	PaymentService paymentService;
	@Autowired
	JAXBTransformService jaxbTransformService;
	@Autowired
	DovutoCarrelloService dovutoCarrelloService;
	@Autowired
	CarrelloService carrelloService;
	@Autowired
	CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;
	@Autowired
	DovutoService dovutoService;
	@Autowired
	EnteTipoDovutoService enteTipoDovutoService;
	@Autowired
	EnteService enteService;
	@Autowired
	MessageSource messageSource;

	@Value("${pa.codIpaEntePredefinito}")
	private String codIpaEnteDefault;

	private final static String SPONTANEOUS = "s";
	private final static String DEBITS = "d";

	@Transactional(propagation = Propagation.REQUIRED)
	public NodoSILInviaCarrelloRPRisposta checkoutCarrello(BasketTo basketTo){

		if (basketTo == null || basketTo.getItems().isEmpty()) {
			log.error("Invalid Carrello - empty cart");
			throw new ValidatorException(messageSource.getMessage("pa.messages.carrelloVuoto", null, Locale.ITALY));
		}
		basketTo.getItems().forEach(item -> {
			if (StringUtils.isBlank(item.getCodIuv())) {
				spontaneoService.validateCart(item);
			} else {
				this.debitValidation(item);
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
		Map<String, List<CarrelloBo>> BOMap = self.buildPaymentBO(basketTo);
		List<CarrelloBo> carts = self.placeOrder(codIpaEnteCaller, urlEsito, BOMap, Constants.TRIGGER_PAYMENT.ON_LINE);
		List<RP> rpList = self.buildCtRichiestaPagamento(carts,null);
		NodoSILInviaCarrelloRP nc = self.buildInviaCarrelloRp(rpList, codIpaEnteCaller);
		NodoSILInviaCarrelloRPRisposta ncr = paymentService.inviaCarrelloRP(carts.get(0).getIdCarrelloMulti(), nc);
		return ncr;
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
	public List<CarrelloBo> placeOrder(String codIpaEnteCaller, String backUrl, Map<String, List<CarrelloBo>> mappedCarts, TRIGGER_PAYMENT triggerPayment) {
		List<CarrelloBo> boList = new ArrayList<>();
		CarrelloMultiBeneficiario carrelloMultiBeneficiario =null;
		//id session is fixed when cart is created through paaSILInviaDovuto or paaSILInviaCarrelloDovuti, and it's the same for every dovuto in this case
		Optional<String> idSessionFixed = mappedCarts.values().stream().findFirst().flatMap(x -> x.stream().findFirst())
			.map(CarrelloBo::getIdSession).filter(StringUtils::isNotBlank);
		idSessionFixed.ifPresent(id -> log.debug("idSessionFixed: {}", id));
		if (triggerPayment.equals(TRIGGER_PAYMENT.ON_LINE))
			carrelloMultiBeneficiario = carrelloMultiBeneficiarioService.insertCarrelloMultiBeneficiario(codIpaEnteCaller, Constants.STATO_CARRELLO_PREDISPOSTO, backUrl, idSessionFixed);
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
						long newId = dovutoService.insertDovutoFromSpontaneo(item, ente, false);
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
		} catch (Exception e) {
			log.error("placeOrder - Error due processing insertCarrello", e);
			throw new PaymentOrderException(messageSource.getMessage("pa.carrello.salvataggioCarrelloError", null, Locale.ITALY));
		}
		return boList;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public NodoSILInviaCarrelloRP buildInviaCarrelloRp(List<RP> rpList, String codIpaEnteCaller) {
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
				String codiceContestoPagamento = paymentService.generateCCP(preparedCart);
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

	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> buildInviaRp(RP ctRichiestaPagamento, Psp psp, String codIpaEnteCaller, Long idCarrello) {
		log.debug("buildInviaRp - processing start");
		String idDominio = ctRichiestaPagamento.getDominio().getIdentificativoDominio();
		String iuv = ctRichiestaPagamento.getDatiVersamento().getIdentificativoUnivocoVersamento();
		String ccp = ctRichiestaPagamento.getDatiVersamento().getCodiceContestoPagamento();
		HashMap<String, Object> returnHashMap = new HashMap<>();
		log.debug("INVIO RPT: ente [{}] IUV [{}]", codIpaEnteCaller, iuv );
		log.debug("INVIO RPT: psp scelto [{}] modello pagamento [{}] tipo versamento [{}]",
			psp.getIdentificativoPSP(), psp.getModelloPagamento(), psp.getTipoVersamento());

		NodoSILInviaRP nodoSILInviaRP = new NodoSILInviaRP();
		IntestazionePPT intestazionePPT = new IntestazionePPT();
		try {
			//Carrello preparedCart = carrelloService.getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamento(idDominio, iuv, ccp, true);
			Carrello preparedCart = carrelloService.getById(idCarrello);
			nodoSILInviaRP.setIdentificativoPSP(psp.getIdentificativoPSP());
			nodoSILInviaRP.setModelloPagamento(psp.getModelloPagamento());
			Optional.ofNullable(psp.getIdentificativoIntermediarioPSP()).ifPresent(nodoSILInviaRP::setIdentificativoIntermediarioPSP);
			Optional.ofNullable(psp.getIdentificativoCanale()).ifPresent(nodoSILInviaRP::setIdentificativoCanale);
			byte[] encodedRP = jaxbTransformService.marshallingAsBytes(ctRichiestaPagamento, RP.class);
			nodoSILInviaRP.setRp(encodedRP);
			intestazionePPT.setIdentificativoDominio(idDominio);
			intestazionePPT.setIdentificativoUnivocoVersamento(iuv);
			intestazionePPT.setCodiceContestoPagamento(ccp);

			carrelloService.updateCarrelloForRP(preparedCart, ctRichiestaPagamento, Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO);
			List<Dovuto> dovutiInCarrello = dovutoService.getDovutiInCarrello(preparedCart.getMygovCarrelloId());
			dovutoService.updateStatus(dovutiInCarrello, Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO);
			dovutoCarrelloService.insertRP(preparedCart, dovutiInCarrello, ctRichiestaPagamento);
		} catch (Exception e) {
			log.error("Error due processing nodoSILInviaRP", e);
			throw new PaymentOrderException(messageSource.getMessage("pa.errore.invioRPT", null, Locale.ITALY));
		}
		returnHashMap.put("nodoSILInviaRP", nodoSILInviaRP);
		returnHashMap.put("intestazionePPT", intestazionePPT);
		log.debug("buildInviaRp - processing end");
		return returnHashMap;
	}

	private void debitValidation(final CartItem item) {
		String ente = item.getCodIpaEnte();
		Dovuto dovuto = dovutoService.getById(item.getId());

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
	}
}
