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
package it.regioneveneto.mygov.payment.mypay4.model.fesp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import it.regioneveneto.mygov.payment.mypay4.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovReceiptId")
public class Receipt extends BaseEntity {

	public static final String ALIAS = "FESP_Receipt";
	public static final String FIELDS = ""+ALIAS+".mygov_receipt_id as FESP_Receipt_mygovReceiptId,"+ALIAS+".dt_creazione as FESP_Receipt_dtCreazione"+
		","+ALIAS+".receipt_id as FESP_Receipt_receiptId,"+ALIAS+".notice_number as FESP_Receipt_noticeNumber"+
		","+ALIAS+".fiscal_code as FESP_Receipt_fiscalCode,"+ALIAS+".outcome as FESP_Receipt_outcome"+
		","+ALIAS+".creditor_reference_id as FESP_Receipt_creditorReferenceId"+
		","+ALIAS+".payment_amount as FESP_Receipt_paymentAmount,"+ALIAS+".description as FESP_Receipt_description"+
		","+ALIAS+".company_name as FESP_Receipt_companyName,"+ALIAS+".office_name as FESP_Receipt_officeName"+
		","+ALIAS+".unique_identifier_type_debtor as FESP_Receipt_uniqueIdentifierTypeDebtor"+
		","+ALIAS+".unique_identifier_value_debtor as FESP_Receipt_uniqueIdentifierValueDebtor"+
		","+ALIAS+".full_name_debtor as FESP_Receipt_fullNameDebtor"+
		","+ALIAS+".street_name_debtor as FESP_Receipt_streetNameDebtor"+
		","+ALIAS+".civic_number_debtor as FESP_Receipt_civicNumberDebtor"+
		","+ALIAS+".postal_code_debtor as FESP_Receipt_postalCodeDebtor,"+ALIAS+".city_debtor as FESP_Receipt_cityDebtor"+
		","+ALIAS+".state_province_region_debtor as FESP_Receipt_stateProvinceRegionDebtor"+
		","+ALIAS+".country_debtor as FESP_Receipt_countryDebtor,"+ALIAS+".email_debtor as FESP_Receipt_eMailDebtor"+
		","+ALIAS+".id_psp as FESP_Receipt_idPSP,"+ALIAS+".psp_fiscal_code as FESP_Receipt_pspFiscalCode"+
		","+ALIAS+".psp_partita_iva as FESP_Receipt_pspPartitaIVA,"+ALIAS+".psp_company_name as FESP_Receipt_pspCompanyName"+
		","+ALIAS+".id_channel as FESP_Receipt_idChannel,"+ALIAS+".channel_description as FESP_Receipt_channelDescription"+
		","+ALIAS+".unique_identifier_type_payer as FESP_Receipt_uniqueIdentifierTypePayer"+
		","+ALIAS+".unique_identifier_value_payer as FESP_Receipt_uniqueIdentifierValuePayer"+
		","+ALIAS+".full_name_payer as FESP_Receipt_fullNamePayer,"+ALIAS+".street_name_payer as FESP_Receipt_streetNamePayer"+
		","+ALIAS+".civic_number_payer as FESP_Receipt_civicNumberPayer"+
		","+ALIAS+".postal_code_payer as FESP_Receipt_postalCodePayer,"+ALIAS+".city_payer as FESP_Receipt_cityPayer"+
		","+ALIAS+".state_province_region_payer as FESP_Receipt_stateProvinceRegionPayer"+
		","+ALIAS+".country_payer as FESP_Receipt_countryPayer,"+ALIAS+".email_payer as FESP_Receipt_eMailPayer"+
		","+ALIAS+".payment_method as FESP_Receipt_paymentMethod,"+ALIAS+".fee as FESP_Receipt_fee"+
		","+ALIAS+".payment_date_time as FESP_Receipt_paymentDateTime,"+ALIAS+".application_date as FESP_Receipt_applicationDate"+
		","+ALIAS+".transfer_date as FESP_Receipt_transferDate,"+ALIAS+".transfer_amount_1 as FESP_Receipt_transferAmount1"+
		","+ALIAS+".fiscal_code_pa_1 as FESP_Receipt_fiscalCodePA1,"+ALIAS+".iban_1 as FESP_Receipt_iban1"+
		","+ALIAS+".remittance_information_1 as FESP_Receipt_remittanceInformation1"+
		","+ALIAS+".transfer_category_1 as FESP_Receipt_transferCategory1"+
		","+ALIAS+".transfer_amount_2 as FESP_Receipt_transferAmount2,"+ALIAS+".fiscal_code_pa_2 as FESP_Receipt_fiscalCodePA2"+
		","+ALIAS+".iban_2 as FESP_Receipt_iban2,"+ALIAS+".remittance_information_2 as FESP_Receipt_remittanceInformation2"+
		","+ALIAS+".transfer_category_2 as FESP_Receipt_transferCategory2,"+ALIAS+".status as FESP_Receipt_status"+
		","+ALIAS+".standin as FESP_Receipt_standin,"+ALIAS+".receipt_bytes as FESP_Receipt_receiptBytes"+
		","+ALIAS+".dt_processing as FESP_Receipt_dtProcessing"+
		","+ALIAS+".num_tries_processing as FESP_Receipt_numTriesProcessing";

	private Long mygovReceiptId;
	private Date dtCreazione;
	private String receiptId;
	private String noticeNumber;
	private String fiscalCode;
	private String outcome;
	private String creditorReferenceId;
	private BigDecimal paymentAmount;
	private String description;
	private String companyName;
	private String officeName;
	//*from CtSubject debtor
	private String uniqueIdentifierTypeDebtor;
	private String uniqueIdentifierValueDebtor;
	private String fullNameDebtor;
	private String streetNameDebtor;
	private String civicNumberDebtor;
	private String postalCodeDebtor;
	private String cityDebtor;
	private String stateProvinceRegionDebtor;
	private String countryDebtor;
	private String emailDebtor;

	private String idPsp;
	private String pspFiscalCode;
	private String pspPartitaIva;
	private String pspCompanyName;
	private String idChannel;
	private String channelDescription;
	//*from CtSubject payer
	private String uniqueIdentifierTypePayer;
	private String uniqueIdentifierValuePayer;
	private String fullNamePayer;
	private String streetNamePayer;
	private String civicNumberPayer;
	private String postalCodePayer;
	private String cityPayer;
	private String stateProvinceRegionPayer;
	private String countryPayer;
	private String emailPayer;

	private String paymentMethod;
	private BigDecimal fee;
	private Date paymentDateTime;
	private Date applicationDate;
	private Date transferDate;
	//from CtTransferListPA list ( two items)
	private BigDecimal transferAmount1;
	private String fiscalCodePa1;
	private String iban1;
	private String remittanceInformation1;
	private String transferCategory1;
	private BigDecimal transferAmount2;
	private String fiscalCodePa2;
	private String iban2;
	private String remittanceInformation2;
	private String transferCategory2;
	private String status;
	private Date dtProcessing;
	private Integer numTriesProcessing;

	private boolean standin;
	private byte[] receiptBytes;
}