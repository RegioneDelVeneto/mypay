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

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.ReceiptDao;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.CtReceiptWithExtras;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Receipt;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.veneto.regione.schemas._2012.pagamenti.CtReceipt;
import it.veneto.regione.schemas._2012.pagamenti.CtSubject;
import it.veneto.regione.schemas._2012.pagamenti.CtTransferPAReceipt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service("ReceiptServiceFESP")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class ReceiptService {

	@Autowired
	ReceiptDao receiptDao;

	@Autowired
	EnteService enteService;

	public Optional<CtReceiptWithExtras> getReadyByIdLockOrSkip(Long mygovReceiptId) {
		return receiptDao.getReadyByIdLockOrSkip(mygovReceiptId);
	}

	public List<Long> findIdByReadyOrForceStatus() {
		return receiptDao.findIdByReadyOrForceStatus();
	}

	public List<Receipt> findReceiptDataToRecovery() {
		return receiptDao.findReceiptsToRecovery();
	}

	@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
	public Long insertNewReceipt(Receipt receipt) { return receiptDao.insertNew(receipt);	}

	@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
	public int updateStatus(Long mygovReceiptId, String newStatus, boolean resetTries, boolean incrementTries) {
		return receiptDao.updateStatusById(mygovReceiptId, newStatus, resetTries, incrementTries);
	}

	public static Receipt.ReceiptBuilder mapperModel(CtReceipt receipt) {
		Optional<CtSubject> optionalPayer = Optional.ofNullable(receipt.getPayer());
		CtTransferPAReceipt primaryPayment = receipt.getTransferList().getTransfers().get(0);
		// secondary payment is only considered in MyPay for payment "like" TARI / TEFA
		// i.e.: only 2 transfers, to different beneficiaries
		Optional<CtTransferPAReceipt> secondaryPayment = receipt.getTransferList().getTransfers().stream()
				.filter(payment -> payment.getIdTransfer() == 2 && !StringUtils.equals(primaryPayment.getFiscalCodePA(),payment.getFiscalCodePA()))
				.findFirst();
		Receipt.ReceiptBuilder builder = Receipt.builder()
			.dtCreazione(new Date())
			.receiptId(receipt.getReceiptId())
			.noticeNumber(receipt.getNoticeNumber())
			.fiscalCode(receipt.getFiscalCode())
			.outcome(receipt.getOutcome().value())
			.creditorReferenceId(receipt.getCreditorReferenceId())
			.paymentAmount(receipt.getPaymentAmount())
			.description(receipt.getDescription())
			.companyName(receipt.getCompanyName())
			.idPsp(receipt.getIdPSP())
			.pspCompanyName(receipt.getPSPCompanyName())
			.idChannel(receipt.getIdChannel())
			.channelDescription(receipt.getChannelDescription())
			.uniqueIdentifierTypeDebtor(receipt.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierType().value())
			.uniqueIdentifierValueDebtor(receipt.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue())
			.fullNameDebtor(receipt.getDebtor().getFullName())
			.streetNameDebtor(receipt.getDebtor().getStreetName())
			.civicNumberDebtor(receipt.getDebtor().getCivicNumber())
			.postalCodeDebtor(receipt.getDebtor().getPostalCode())
			.cityDebtor(receipt.getDebtor().getCity())
			.stateProvinceRegionDebtor(receipt.getDebtor().getStateProvinceRegion())
			.countryDebtor(receipt.getDebtor().getCountry())
			.emailDebtor(receipt.getDebtor().getEMail())
			.transferAmount1(primaryPayment.getTransferAmount())
			.fiscalCodePa1(primaryPayment.getFiscalCodePA())
			.iban1(primaryPayment.getIBAN())
			.remittanceInformation1(primaryPayment.getRemittanceInformation())
			.transferCategory1(primaryPayment.getTransferCategory());
		secondaryPayment.ifPresent(payment -> builder
				.transferAmount2(payment.getTransferAmount())
				.fiscalCodePa2(payment.getFiscalCodePA())
				.iban2(payment.getIBAN())
				.remittanceInformation2(payment.getRemittanceInformation())
				.transferCategory2(payment.getTransferCategory()) );
		builder.status(Constants.RECEIPT_STATUS.READY.toString());

		Optional.ofNullable(receipt.getOfficeName()).ifPresent(builder::officeName);
		Optional.ofNullable(receipt.getPspFiscalCode()).ifPresent(builder::pspFiscalCode);
		Optional.ofNullable(receipt.getPspPartitaIVA()).ifPresent(builder::pspPartitaIva);
		Optional.ofNullable(receipt.getPaymentMethod()).ifPresent(builder::paymentMethod);
		Optional.ofNullable(receipt.getFee()).ifPresent(builder::fee);
		Optional.ofNullable(receipt.getPaymentDateTime()).map(d -> d.toGregorianCalendar().getTime()).ifPresent(builder::paymentDateTime);
		Optional.ofNullable(receipt.getApplicationDate()).map(d -> d.toGregorianCalendar().getTime()).ifPresent(builder::applicationDate);
		Optional.ofNullable(receipt.getTransferDate()).map(d -> d.toGregorianCalendar().getTime()).ifPresent(builder::transferDate);

		optionalPayer.ifPresent( payer -> builder
			.uniqueIdentifierTypePayer(payer.getUniqueIdentifier().getEntityUniqueIdentifierType().value())
			.uniqueIdentifierValuePayer(payer.getUniqueIdentifier().getEntityUniqueIdentifierValue())
			.fullNamePayer(payer.getFullName())
			.streetNamePayer(payer.getStreetName())
			.civicNumberPayer(payer.getCivicNumber())
			.postalCodePayer(payer.getPostalCode())
			.cityPayer(payer.getCity())
			.stateProvinceRegionPayer(payer.getStateProvinceRegion())
			.countryPayer(payer.getCountry())
			.emailPayer(payer.getEMail()) );

		return builder;
	}

	public boolean isReceiptManagedByMyPay(String fiscalCode, String noticeNumber, Set<String> secondaryFiscalCodes) {
		// a receipt is managed by MyPay/MyPivot if:
		// - its primary ente / auxDigit is managed by MyPay
		// - any secondary transfer ente is managed by MyPay
		Ente primaryEnte = enteService.getEnteByCodFiscale(fiscalCode);
		String auxDigitNotice = StringUtils.substring(noticeNumber, 1, 3);
		if(primaryEnte!=null){
			if(StringUtils.equals(primaryEnte.getCodCodiceSegregazione(), auxDigitNotice))
				return true;
			else
				log.debug("receipt [{}/{}]: mismatch segregation code [{}/{}]", fiscalCode, noticeNumber,
					primaryEnte.getCodCodiceSegregazione(), auxDigitNotice);
		}

		boolean isSecondaryManaged = secondaryFiscalCodes.stream()
			.filter(fc -> !StringUtils.equals(fc, fiscalCode))
			.map(enteService::getEnteByCodFiscale).anyMatch(Objects::nonNull);

		if(!isSecondaryManaged)
			log.debug("receipt [{}/{}] not managed by MyPay: unknown ente", fiscalCode, noticeNumber);

		return isSecondaryManaged;
	}
}
