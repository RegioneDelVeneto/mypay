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
package it.regioneveneto.mygov.payment.mypay4.service.pagopa;

import it.regioneveneto.mygov.payment.mypay4.dto.BasketTo;
import it.regioneveneto.mygov.payment.mypay4.dto.CartItem;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.PaymentInfo;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.PaymentNotice;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoMultibeneficiario;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PaymentNoticeService {


	@Value("${pa.gpd.enabled}")
	private boolean gpdEnabled;
	@Value("${pa.gpd.preload}")
	private boolean gpdPreload;

	@Autowired
	private DovutoService dovutoService;

	@Autowired
	private EnteService enteService;

	@Autowired(required = false)
	private GpdService gpdService;

	private final static String CAUSALI_MULTIPLE = "causali multiple";

	private final static BigDecimal BIG_DECIMAL_100 = new BigDecimal(100L);

	public List<PaymentInfo> filterPaymentsWithoutIuv(BasketTo basket) {
		log.debug("filterPaymentsWithoutIuv - starts");
		Function<CartItem, PaymentInfo> mapper = item -> PaymentInfo.builder()
			.codIpaEnte(item.getCodIpaEnte())
			.intestatario(item.getIntestatario())
			.items(new ArrayList<>(Collections.singleton(item)))
			.dovutiEntiSecondari(Optional.ofNullable(basket.getDovutiEntiSecondari()))
			.build();

		var result = basket.getItems().stream()
			.filter(item -> StringUtils.isBlank(item.getCodIuv()))
			.map(mapper)
			.collect(Collectors.groupingBy(PaymentInfo::getCodIpaEnte, Collectors.groupingBy(PaymentInfo::getIntestatario, Collectors.toList())))
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.map(Collection::stream)
			.map(resultStream -> resultStream.reduce((prev, next) -> prev.addOccurrences(next.getItems())))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void createDebit(PaymentInfo paymentInfo) {
		log.debug("createDebit - starts");
		var ente = enteService.getEnteByCodIpa(paymentInfo.getCodIpaEnte());
		if (paymentInfo.getDovutiEntiSecondari().isEmpty()) {
			log.debug("its mono-beneficiary");
			String iuv = dovutoService.generateIUV(ente, paymentInfo.getTotalAmount().toString(), Constants.IUV_GENERATOR_17, Constants.PAY_PRESSO_PSP);
			for (CartItem item : paymentInfo.getItems()) {
				var newId = dovutoService.insertDovutoFromSpontaneo(item, ente, false, null, true);
				item.setId(newId);
			}
			String iupd = gpdEnabled ? gpdService.generateRandomIupd(ente.getCodiceFiscaleEnte()) : null;
			String assignedIuv = dovutoService.assignIuvAndIupd(paymentInfo.getItems(), iuv, iupd);
			paymentInfo.setIuv(assignedIuv);
		}else {
			log.debug("its multi-beneficiary");
			var newId = dovutoService.insertDovutoFromSpontaneo(paymentInfo.getItems().get(0), ente, true, paymentInfo.getDovutiEntiSecondari().get(), true);
			paymentInfo.getItems().get(0).setId(newId);
		}

		if (gpdEnabled) {
			List<Dovuto> dovutoList = paymentInfo.getItems().stream().map(CartItem::getId).map(dovutoService::getById).collect(Collectors.toUnmodifiableList());
			Optional<DovutoMultibeneficiario> multibeneficiario = dovutoService.getOptionalDovMultibenefByIdDovuto(dovutoList.get(0).getMygovDovutoId());
			gpdService.newDebtPosition(dovutoList, multibeneficiario);
		}
		log.debug("createDebit - ended");
	}

	public PaymentNotice createPaymentNoticeByPaymentInfo(PaymentInfo paymentInfo) {
		log.debug("createPaymentNoticeByPaymentInfo - starts");
		String causale = paymentInfo.getItems().size() > 1 ? CAUSALI_MULTIPLE : StringUtils.firstNonBlank(paymentInfo.getItems().get(0).getCausaleVisualizzata(), paymentInfo.getItems().get(0).getCausale());
		Optional<DovutoMultibeneficiario> dovutoMultibeneficiario = Optional.empty();
		if (paymentInfo.getDovutiEntiSecondari().isPresent())
				dovutoMultibeneficiario = dovutoService.getOptionalDovMultibenefByIdDovuto(paymentInfo.getItems().get(0).getId());
		var ente = enteService.getEnteByCodIpa(paymentInfo.getCodIpaEnte());
		var paymentNotice = mappingPaymentNotice(dovutoMultibeneficiario, paymentInfo.getTotalAmount(), ente, paymentInfo.getIuv(), causale);
		log.debug("createPaymentNoticeByPaymentInfo - ended");
		return paymentNotice;
	}

	public PaymentNotice createPaymentNoticeByDebit(Dovuto dovuto) {
		log.debug("createPaymentNoticeByDebit - starts");
		var dovutoMultibeneficiario = dovutoService.getOptionalDovMultibenefByIdDovuto(dovuto.getMygovDovutoId());
		var causale = StringUtils.firstNonBlank(dovuto.getDeCausaleVisualizzata(), dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento());
		var result = mappingPaymentNotice(dovutoMultibeneficiario, dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento(), dovuto.getNestedEnte(), dovuto.getCodIuv(), causale);
		log.debug("createPaymentNoticeByDebit - ended");
		return result;
	}


	private PaymentNotice mappingPaymentNotice(Optional<DovutoMultibeneficiario> dovutoMultibeneficiario, BigDecimal amount, Ente ente, String iuv, String causale) {
		log.debug("mappingPaymentNotice - starts");
		if(dovutoMultibeneficiario.isPresent())
			amount = amount.add(dovutoMultibeneficiario.get().getNumRpDatiVersDatiSingVersImportoSingoloVersamento());
		long amountAsCents = amount.multiply(BIG_DECIMAL_100).longValue();
		var result = PaymentNotice.builder()
			.noticeNumber(Utilities.iuvToNumeroAvviso(iuv, ente.getApplicationCode(), false))
			.amount(amountAsCents)
			.fiscalCode(ente.getCodiceFiscaleEnte())
			.companyName(StringUtils.abbreviate(ente.getDeNomeEnte(), 140))
			.description(StringUtils.abbreviate(causale, 140))
			.build();
		log.debug("mappingPaymentNotice - ended");
		return result;
	}
}
