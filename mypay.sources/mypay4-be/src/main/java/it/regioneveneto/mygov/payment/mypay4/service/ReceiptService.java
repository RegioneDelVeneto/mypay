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

import it.regioneveneto.mygov.payment.mypay4.dao.ReceiptDao;
import it.regioneveneto.mygov.payment.mypay4.dto.ReceiptExportTo;
import it.regioneveneto.mygov.payment.mypay4.dto.ReceiptTransferExportTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.Receipt;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.veneto.regione.schemas._2012.pagamenti.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service("ReceiptServicePA")
@Slf4j
public class ReceiptService {

	private final ThreadLocal<SimpleDateFormat> dateFormatReceipt = ThreadLocal.withInitial(()->new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));

	@Resource
	private ReceiptService self;

	@Autowired
	private ReceiptDao receiptDao;

	@Autowired
	private EnteService enteService;

	@Autowired
	private MyPivotService myPivotService;

	@Autowired
	JAXBTransformService jaxbTransformService;

	@Transactional(propagation = Propagation.SUPPORTS)
	public List<Receipt> findByMultiBeneficiaryNotExported() {
		return receiptDao.findByMultiBeneficiaryNotExported();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int setExportStatusSent(Long mygovReceiptId) {
		return receiptDao.setExportStatusSent(mygovReceiptId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int setExportStatusSentAndReceived(Long mygovReceiptId) {
		return receiptDao.setExportStatusSentAndReceived(mygovReceiptId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int setExportStatusReceived(Long mygovReceiptId) {
		return receiptDao.setExportStatusReceived(mygovReceiptId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int setExportStatusError(Long mygovReceiptId) {
		return receiptDao.setExportStatusError(mygovReceiptId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int resetExportStatusNew(Long mygovReceiptId) {
		return receiptDao.resetExportStatusNew(mygovReceiptId);
	}

	public Optional<Receipt> getById(Long id) { return receiptDao.getById(id);	}

	public Optional<Receipt> getByDovutoElaboratoId(Long dovutoElaboratoId) { return receiptDao.getByDovutoElaboratoId(dovutoElaboratoId);	}

	public Optional<Receipt> getByReceiptId(String receiptId) { return receiptDao.getByReceiptId(receiptId);	}

	public Optional<Receipt> getByIdentifierSectionData(String receiptId, String noticeNumber, String fiscalCode) {
		return receiptDao.getByIdentifierSectionData(receiptId, noticeNumber, fiscalCode);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public Long insertNewReceipt(CtReceipt receipt, byte[] receiptBytes, Optional<DovutoElaborato> dovutoElaborato) {
		Optional<Receipt> existingReceipt = self.getByReceiptId(receipt.getReceiptId());
		if(existingReceipt.isPresent()){
			log.error("already existing receipt with id[{}] receiptId[{}]", existingReceipt.get().getMygovReceiptId(), existingReceipt.get().getReceiptId());
			throw new MyPayException("already existing receipt to insert");
		}
		Receipt.ReceiptBuilder builder = mapperModel(receipt);
		builder.receiptBytes(receiptBytes);
		dovutoElaborato.ifPresent(builder::mygovDovutoElaboratoId);
		Long newId = receiptDao.insertNew(builder.build());
		return newId;
	}

	private Receipt.ReceiptBuilder mapperModel(CtReceipt receipt) {
		var optionalPayer = Optional.ofNullable(receipt.getPayer());
		var primaryPayment = receipt.getTransferList().getTransfers().get(0);
		var secondaryPayment = receipt.getTransferList().getTransfers().stream().skip(1).findFirst().orElse(new CtTransferPAReceipt());
		var builder = Receipt.builder()
			.dtCreazione(new Date())
			.receiptId(receipt.getReceiptId())
			.noticeNumber(receipt.getNoticeNumber())
			.fiscalCode(receipt.getFiscalCode())
			.outcome(receipt.getOutcome().value())
			.creditorReferenceId(receipt.getCreditorReferenceId())
			.paymentAmount(receipt.getPaymentAmount())
			.description(receipt.getDescription())
			.companyName(receipt.getCompanyName())
			.officeName(receipt.getOfficeName())
			.idPsp(receipt.getIdPSP())
			.pspFiscalCode(receipt.getPspFiscalCode())
			.pspPartitaIva(receipt.getPspPartitaIVA())
			.pspCompanyName(receipt.getPSPCompanyName())
			.idChannel(receipt.getIdChannel())
			.channelDescription(receipt.getChannelDescription())
			.paymentMethod(receipt.getPaymentMethod())
			.fee(receipt.getFee())
			.paymentDateTime(Optional.ofNullable(receipt.getPaymentDateTime()).map(x->x.toGregorianCalendar().getTime()).orElse(null))
			.applicationDate(Optional.ofNullable(receipt.getApplicationDate()).map(x->x.toGregorianCalendar().getTime()).orElse(null))
			.transferDate(Optional.ofNullable(receipt.getTransferDate()).map(x->x.toGregorianCalendar().getTime()).orElse(null))
			.uniqueIdentifierTypeDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getUniqueIdentifier)
				.map(CtEntityUniqueIdentifier::getEntityUniqueIdentifierType).map(StEntityUniqueIdentifierType::value).orElse(null))
			.uniqueIdentifierValueDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getUniqueIdentifier)
				.map(CtEntityUniqueIdentifier::getEntityUniqueIdentifierValue).orElse(null))
			.fullNameDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getFullName).orElse(null))
			.streetNameDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getStreetName).orElse(null))
			.civicNumberDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getCivicNumber).orElse(null))
			.postalCodeDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getPostalCode).orElse(null))
			.cityDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getCity).orElse(null))
			.stateProvinceRegionDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getStateProvinceRegion).orElse(null))
			.countryDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getCountry).orElse(null))
			.emailDebtor(Optional.ofNullable(receipt.getDebtor()).map(CtSubject::getEMail).orElse(null))
			.transferAmount1(primaryPayment.getTransferAmount())
			.fiscalCodePa1(primaryPayment.getFiscalCodePA())
			.iban1(primaryPayment.getIBAN())
			.remittanceInformation1(primaryPayment.getRemittanceInformation())
			.transferCategory1(primaryPayment.getTransferCategory());
		if (!StringUtils.equals(primaryPayment.getFiscalCodePA(), secondaryPayment.getFiscalCodePA())) {
			builder.transferAmount2(secondaryPayment.getTransferAmount())
				.fiscalCodePa2(secondaryPayment.getFiscalCodePA())
				.iban2(secondaryPayment.getIBAN())
				.remittanceInformation2(secondaryPayment.getRemittanceInformation())
				.transferCategory2(secondaryPayment.getTransferCategory());
		}

		if (optionalPayer.isPresent()) {
			var payer = optionalPayer.get();
			builder
				.uniqueIdentifierTypePayer(payer.getUniqueIdentifier().getEntityUniqueIdentifierType().value())
				.uniqueIdentifierValuePayer(payer.getUniqueIdentifier().getEntityUniqueIdentifierValue())
				.fullNamePayer(payer.getFullName())
				.streetNamePayer(payer.getStreetName())
				.civicNumberPayer(payer.getCivicNumber())
				.postalCodePayer(payer.getPostalCode())
				.cityPayer(payer.getCity())
				.stateProvinceRegionPayer(payer.getStateProvinceRegion())
				.countryPayer(payer.getCountry())
				.emailPayer(payer.getEMail());
		}
		return builder;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public Optional<Receipt> getNotExportedByIdLockOrSkip(Long mygovReceiptId) {
		return receiptDao.getNotExportedByIdLockOrSkip(mygovReceiptId);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void exportReceipt(Long mygovReceiptId){
		self.getNotExportedByIdLockOrSkip(mygovReceiptId).ifPresentOrElse(receipt -> {
			try {
				ReceiptExportTo receiptExportTo = receiptRowMapper(receipt);

				//set tipo dovuto (only for payments handled by MyPay)
				Optional.ofNullable(receipt.getMygovDovutoElaboratoId())
						.map(DovutoElaborato::getCodTipoDovuto)
						.map(StringUtils::stripToNull)
						.ifPresent(receiptExportTo::setCodTipoDovuto);

				boolean received = myPivotService.exportReceipt(receiptExportTo);
				log.info("receipt sent to MyPivot for domain[{}] - iuv[{}] - receiptId[{}] - mygovReceiptId[{}] - received status[{}]",
						receipt.getFiscalCodePa2(), receipt.getCreditorReferenceId(), receipt.getReceiptId(), receipt.getMygovReceiptId(), received);
				if(received)
					self.setExportStatusSentAndReceived(mygovReceiptId);
				else
					self.setExportStatusSent(mygovReceiptId);
			} catch (Exception e) {
				log.error("Error on exportReceipt id[{}]", mygovReceiptId, e);
				self.setExportStatusSent(mygovReceiptId);
			}
		}, () -> log.info("exportReceipt, skip because receipt missing or locked [{}]", mygovReceiptId) );
	}

	private ReceiptExportTo receiptRowMapper(Receipt receipt) throws InvocationTargetException, IllegalAccessException {
		//use the original XML of receipt to extract needed information
		it.gov.pagopa.pagopa_api.pa.pafornode.CtReceipt ctReceipt = jaxbTransformService.unmarshalling(receipt.getReceiptBytes(),it.gov.pagopa.pagopa_api.pa.pafornode.CtReceipt.class);

		ReceiptExportTo receiptExportTo = new ReceiptExportTo();

		//copy all properties with same name from PagoPa receipt
		BeanUtils.copyProperties(receiptExportTo, receipt);
		receiptExportTo.setPaymentDateTime( Optional.ofNullable(receipt.getPaymentDateTime()).map(dateFormatReceipt.get()::format).orElse(""));    //getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento
		receiptExportTo.setTransferDate( Optional.ofNullable(receipt.getTransferDate()).map(dateFormatReceipt.get()::format).orElse(""));

		//for primary transfer, there is a specific handling of codiceTassonomico needed by MyPivot
		receiptExportTo.setCodTassonomico( ctReceipt.getTransferList().getTransfers().stream()
			.filter(ctTransferPA -> ctTransferPA.getIdTransfer()==1 && StringUtils.equals(ctTransferPA.getFiscalCodePA(), ctReceipt.getFiscalCode()))
			.findFirst()
			.map(ctTransferPA -> {
				if (StringUtils.isNotBlank(ctTransferPA.getTransferCategory())) {
					Matcher m = Constants.DATI_SPECIFICI_RISCOSSIONE_REGEX.matcher(ctTransferPA.getTransferCategory());
					if (m.find())
						return m.group(1);
					else if (Constants.CODICE_TASSONOMICO_REGEX.matcher(ctTransferPA.getTransferCategory()).matches())
						return ctTransferPA.getTransferCategory();
				}
				return null;
			}).orElse(null));

		receiptExportTo.setDeNomeEnte(Optional.ofNullable(enteService.getEnteByCodFiscale(receipt.getFiscalCode())).map(Ente::getDeNomeEnte).orElse(null));

		//fill transfer list
		receiptExportTo.setReceiptTransferExportToList(
			ctReceipt.getTransferList().getTransfers().stream()
					.map(ctTransfer -> {
						Optional<Ente> ente = Optional.ofNullable(enteService.getEnteByCodFiscale(ctTransfer.getFiscalCodePA()));
						return ReceiptTransferExportTo.builder()
								.idTransfer(ctTransfer.getIdTransfer())
								.fiscalCodePA(ctTransfer.getFiscalCodePA())
								.transferAmount(ctTransfer.getTransferAmount())
								.iban(ctTransfer.getIBAN())
								.remittanceInformation(ctTransfer.getRemittanceInformation()) //deRpDatiVersDatiSingVersCausaleVersamento
								.transferCategory(ctTransfer.getTransferCategory()) //deRpDatiVersDatiSingVersDatiSpecificiRiscossione
								.deRpEnteBenefCivicoBeneficiario(ente.map(Ente::getDeRpEnteBenefCivicoBeneficiario).orElse(null))
								.deRpEnteBenefIndirizzoBeneficiario(ente.map(Ente::getDeRpEnteBenefIndirizzoBeneficiario).orElse(null))
								.deRpEnteBenefLocalitaBeneficiario(ente.map(Ente::getDeRpEnteBenefLocalitaBeneficiario).orElse(null))
								.deRpEnteBenefProvinciaBeneficiario(ente.map(Ente::getDeRpEnteBenefProvinciaBeneficiario).orElse(null))
								.codRpEnteBenefCapBeneficiario(ente.map(Ente::getCodRpEnteBenefCapBeneficiario).orElse(null))
								.build();
					})
					.collect(Collectors.toList())
		);

		return receiptExportTo;
	}

	@Transactional(propagation = Propagation.NEVER)
	public List<Long> getWithoutReceiptBytes(){
		return receiptDao.getWithoutReceiptBytes();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public boolean forceGenerateBytes(Long mygovReceiptId){
		return receiptDao.getByIdAndNullBytesAsCtReceipt(mygovReceiptId).map(ctReceipt -> {
			byte[] xmlReceiptBytes = jaxbTransformService.marshallingAsBytes(ctReceipt, it.gov.pagopa.pagopa_api.pa.pafornode.CtReceipt.class, "receipt");
			receiptDao.setReceiptBytes(mygovReceiptId, xmlReceiptBytes);
			return true;
		}).orElse(false);
	}

}
