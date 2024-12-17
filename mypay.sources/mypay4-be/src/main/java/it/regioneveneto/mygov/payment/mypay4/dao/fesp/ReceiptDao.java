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
package it.regioneveneto.mygov.payment.mypay4.dao.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.BaseDao;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.CtReceiptWithExtras;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Receipt;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

import java.util.List;
import java.util.Optional;

public interface ReceiptDao extends BaseDao {

	@SqlQuery("select " + Receipt.ALIAS + ".mygov_receipt_id" +
		"  from mygov_receipt " + Receipt.ALIAS +
		" where " + Receipt.ALIAS + ".status in ( '" + Constants.RECEIPT_STATUS_READY + "' ,'" + Constants.RECEIPT_STATUS_FORCE + "' ) " +
		" order by mygov_receipt_id asc"
	)
	List<Long> findIdByReadyOrForceStatus();

	@SqlQuery("select " + Receipt.ALIAS + ".mygov_receipt_id" +
		" , " + Receipt.ALIAS + ".status" +
		" , " + Receipt.ALIAS + ".dt_processing" +
		" , coalesce(" + Receipt.ALIAS + ".num_tries_processing, 1) as num_tries_processing" +
		"  from mygov_receipt " + Receipt.ALIAS +
		" where " + Receipt.ALIAS + ".status = '" + Constants.RECEIPT_STATUS_ERROR + "'" +
		" order by mygov_receipt_id asc"
	)
	@RegisterFieldMapper(Receipt.class)
	List<Receipt> findReceiptsToRecovery();

	@SqlQuery("    select " + Receipt.ALIAS + ALL_FIELDS +
		"  from mygov_receipt " + Receipt.ALIAS +
		" where " + Receipt.ALIAS + ".mygov_receipt_id = :id ")
	@RegisterFieldMapper(Receipt.class)
	Optional<Receipt> getById(Long id);

	@SqlQuery(
		"select " + Receipt.ALIAS + ALL_FIELDS +
			" from mygov_receipt " + Receipt.ALIAS +
			" where " + Receipt.ALIAS + ".mygov_receipt_id = :mygovReceiptId " +
			"   and " + Receipt.ALIAS + ".status in ( '" + Constants.RECEIPT_STATUS_READY + "' ,'" + Constants.RECEIPT_STATUS_FORCE + "' ) " +
			" for update skip locked"
	)
	Optional<CtReceiptWithExtras> getReadyByIdLockOrSkip(Long mygovReceiptId);

	@SqlUpdate("update mygov_receipt" +
		" set status = :status" +
		" , dt_processing = <if(incrementTries)> now() <elseif(resetTries)> null <else> dt_processing <endif>" +
		" , num_tries_processing = <if(resetTries)> 0 <elseif(incrementTries)> coalesce(num_tries_processing, 0) + 1 <else> coalesce(num_tries_processing, 0) <endif>" +
		" where mygov_receipt_id = :mygov_receipt_id "
	)
	@UseStringTemplateEngine
	int updateStatusById(Long mygov_receipt_id, String status, @Define boolean resetTries, @Define boolean incrementTries);

	@SqlUpdate("update mygov_receipt" +
		" set dt_creazione = :r.dtCreazione" +
		", receipt_id = :r.receiptId" +
		", notice_number = :r.noticeNumber" +
		", fiscal_code = :r.fiscalCode" +
		", outcome = :r.outcome" +
		", creditor_reference_id = :r.creditorReferenceId" +
		", payment_amount = :r.paymentAmount" +
		", description = :r.description" +
		", company_name = :r.companyName" +
		", office_name = :r.officeName" +
		", unique_identifier_type_debtor = :r.uniqueIdentifierTypeDebtor" +
		", unique_identifier_value_debtor = :r.uniqueIdentifierValueDebtor" +
		", full_name_debtor = :r.fullNameDebtor" +
		", street_name_debtor = :r.streetNameDebtor" +
		", civic_number_debtor = :r.civicNumberDebtor" +
		", postal_code_debtor = :r.postalCodeDebtor" +
		", city_debtor = :r.cityDebtor" +
		", state_province_region_debtor = :r.stateProvinceRegionDebtor" +
		", country_debtor = :r.countryDebtor" +
		", email_debtor = :r.emailDebtor" +
		", id_psp = :r.idPsp" +
		", psp_fiscal_code = :r.pspFiscalCode" +
		", psp_partita_iva = :r.pspPartitaIva" +
		", psp_company_name = :r.pspCompanyName" +
		", id_channel = :r.idChannel" +
		", channel_description = :r.channelDescription" +
		", unique_identifier_type_payer = :r.uniqueIdentifierTypePayer" +
		", unique_identifier_value_payer = :r.uniqueIdentifierValuePayer" +
		", full_name_payer = :r.fullNamePayer" +
		", street_name_payer = :r.streetNamePayer" +
		", civic_number_payer = :r.civicNumberPayer" +
		", postal_code_payer = :r.postalCodePayer" +
		", city_payer = :r.cityPayer" +
		", state_province_region_payer = :r.stateProvinceRegionPayer" +
		", country_payer = :r.countryPayer" +
		", email_payer = :r.emailPayer" +
		", payment_method = :r.paymentMethod" +
		", fee = :r.fee" +
		", payment_date_time = :r.paymentDateTime" +
		", application_date = :r.applicationDate" +
		", transfer_date = :r.transferDate" +
		", transfer_amount_1 = :r.transferAmount1" +
		", fiscal_code_pa_1 = :r.fiscalCodePa1" +
		", iban_1 = :r.iban1" +
		", remittance_information_1 = :r.remittanceInformation1" +
		", transfer_category_1 = :r.transferCategory1" +
		", transfer_amount_2 = :r.transferAmount2" +
		", fiscal_code_pa_2 = :r.fiscalCodePa2" +
		", iban_2 = :r.iban2" +
		", remittance_information_2 = :r.remittanceInformation2" +
		", transfer_category_2 = :r.transferCategory2" +
		", status = :r.status " +
		", receipt_bytes = :r.receiptBytes " +
		", standin = :r.standin " +
		", dt_processing = :r.dtProcessing "+
		", num_tries_processing = :r.num_tries_processing "+
		" where mygov_receipt_id = :r.mygovReceiptId "
	)
	int updateRecord(@BindBean("r") Receipt r);

	@SqlUpdate("INSERT INTO mygov_receipt(" +
		"  mygov_receipt_id " +
		", dt_creazione " +
		", receipt_id " +
		", notice_number " +
		", fiscal_code " +
		", outcome " +
		", creditor_reference_id " +
		", payment_amount " +
		", description " +
		", company_name " +
		", office_name " +
		", unique_identifier_type_debtor " +
		", unique_identifier_value_debtor " +
		", full_name_debtor " +
		", street_name_debtor " +
		", civic_number_debtor " +
		", postal_code_debtor " +
		", city_debtor " +
		", state_province_region_debtor " +
		", country_debtor " +
		", email_debtor " +
		", id_psp " +
		", psp_fiscal_code " +
		", psp_partita_iva " +
		", psp_company_name " +
		", id_channel " +
		", channel_description " +
		", unique_identifier_type_payer " +
		", unique_identifier_value_payer " +
		", full_name_payer " +
		", street_name_payer " +
		", civic_number_payer " +
		", postal_code_payer " +
		", city_payer " +
		", state_province_region_payer " +
		", country_payer " +
		", email_payer " +
		", payment_method " +
		", fee " +
		", payment_date_time " +
		", application_date " +
		", transfer_date " +
		", transfer_amount_1 " +
		", fiscal_code_pa_1 " +
		", iban_1 " +
		", remittance_information_1 " +
		", transfer_category_1 " +
		", transfer_amount_2 " +
		", fiscal_code_pa_2 " +
		", iban_2 " +
		", remittance_information_2 " +
		", transfer_category_2 " +
		", status " +
		", receipt_bytes " +
		", standin " +
		", dt_processing " +
		", num_tries_processing " +
		") values (" +
		"   nextval('mygov_receipt_mygov_receipt_id_seq')" +
		", :r.dtCreazione" +
		", :r.receiptId" +
		", :r.noticeNumber" +
		", :r.fiscalCode" +
		", :r.outcome" +
		", :r.creditorReferenceId" +
		", :r.paymentAmount" +
		", :r.description" +
		", :r.companyName" +
		", :r.officeName" +
		", :r.uniqueIdentifierTypeDebtor" +
		", :r.uniqueIdentifierValueDebtor" +
		", :r.fullNameDebtor" +
		", :r.streetNameDebtor" +
		", :r.civicNumberDebtor" +
		", :r.postalCodeDebtor" +
		", :r.cityDebtor" +
		", :r.stateProvinceRegionDebtor" +
		", :r.countryDebtor" +
		", :r.emailDebtor" +
		", :r.idPsp" +
		", :r.pspFiscalCode" +
		", :r.pspPartitaIva" +
		", :r.pspCompanyName" +
		", :r.idChannel" +
		", :r.channelDescription" +
		", :r.uniqueIdentifierTypePayer" +
		", :r.uniqueIdentifierValuePayer" +
		", :r.fullNamePayer" +
		", :r.streetNamePayer" +
		", :r.civicNumberPayer" +
		", :r.postalCodePayer" +
		", :r.cityPayer" +
		", :r.stateProvinceRegionPayer" +
		", :r.countryPayer" +
		", :r.emailPayer" +
		", :r.paymentMethod" +
		", :r.fee" +
		", :r.paymentDateTime" +
		", :r.applicationDate" +
		", :r.transferDate" +
		", :r.transferAmount1" +
		", :r.fiscalCodePa1" +
		", :r.iban1" +
		", :r.remittanceInformation1" +
		", :r.transferCategory1" +
		", :r.transferAmount2" +
		", :r.fiscalCodePa2" +
		", :r.iban2" +
		", :r.remittanceInformation2" +
		", :r.transferCategory2" +
		", :r.status " +
		", :r.receiptBytes " +
		", :r.standin " +
		", coalesce( CAST(:r.dtProcessing as TIMESTAMP), CAST(now() as TIMESTAMP)) " +
		", coalesce( CAST(:r.numTriesProcessing as INT2), 0) " +
	  ")" +
		" ON CONFLICT DO NOTHING"
	)
	@GetGeneratedKeys("mygov_receipt_id")
	Long insertNew(@BindBean("r") Receipt Receipt);

}
