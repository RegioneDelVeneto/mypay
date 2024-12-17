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
package it.regioneveneto.mygov.payment.mypay4.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.Nested;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovReceiptId")
public class Receipt extends BaseEntity {

	public static final char STATUS_R = 'R';  //received: record exported and received
	public static final char STATUS_S = 'S';  //sent: record exported but not yet received
	public static final char STATUS_N = 'N';	//new: record not yet exported
	public static final char STATUS_E = 'E';	//error: record not exported and in non-recoverable error-state

	public enum Status {
		N, S, R, E
	}

	public static final String ALIAS = "Receipt";
	public static final String FIELDS = ""+ALIAS+".mygov_receipt_id as Receipt_mygovReceiptId"+
		","+ALIAS+".dt_creazione as Receipt_dtCreazione"+
		","+ALIAS+".mygov_dovuto_elaborato_id as Receipt_mygovDovutoElaboratoId"+
		","+ALIAS+".receipt_id as Receipt_receiptId,"+ALIAS+".notice_number as Receipt_noticeNumber"+
		","+ALIAS+".fiscal_code as Receipt_fiscalCode,"+ALIAS+".outcome as Receipt_outcome"+
		","+ALIAS+".creditor_reference_id as Receipt_creditorReferenceId,"+ALIAS+".payment_amount as Receipt_paymentAmount"+
		","+ALIAS+".description as Receipt_description,"+ALIAS+".company_name as Receipt_companyName"+
		","+ALIAS+".office_name as Receipt_officeName"+
		","+ALIAS+".unique_identifier_type_debtor as Receipt_uniqueIdentifierTypeDebtor"+
		","+ALIAS+".unique_identifier_value_debtor as Receipt_uniqueIdentifierValueDebtor"+
		","+ALIAS+".full_name_debtor as Receipt_fullNameDebtor,"+ALIAS+".street_name_debtor as Receipt_streetNameDebtor"+
		","+ALIAS+".civic_number_debtor as Receipt_civicNumberDebtor,"+ALIAS+".postal_code_debtor as Receipt_postalCodeDebtor"+
		","+ALIAS+".city_debtor as Receipt_cityDebtor"+
		","+ALIAS+".state_province_region_debtor as Receipt_stateProvinceRegionDebtor"+
		","+ALIAS+".country_debtor as Receipt_countryDebtor,"+ALIAS+".email_debtor as Receipt_eMailDebtor"+
		","+ALIAS+".id_psp as Receipt_idPSP,"+ALIAS+".psp_fiscal_code as Receipt_pspFiscalCode"+
		","+ALIAS+".psp_partita_iva as Receipt_pspPartitaIVA,"+ALIAS+".psp_company_name as Receipt_pspCompanyName"+
		","+ALIAS+".id_channel as Receipt_idChannel,"+ALIAS+".channel_description as Receipt_channelDescription"+
		","+ALIAS+".unique_identifier_type_payer as Receipt_uniqueIdentifierTypePayer"+
		","+ALIAS+".unique_identifier_value_payer as Receipt_uniqueIdentifierValuePayer"+
		","+ALIAS+".full_name_payer as Receipt_fullNamePayer,"+ALIAS+".street_name_payer as Receipt_streetNamePayer"+
		","+ALIAS+".civic_number_payer as Receipt_civicNumberPayer,"+ALIAS+".postal_code_payer as Receipt_postalCodePayer"+
		","+ALIAS+".city_payer as Receipt_cityPayer,"+ALIAS+".state_province_region_payer as Receipt_stateProvinceRegionPayer"+
		","+ALIAS+".country_payer as Receipt_countryPayer,"+ALIAS+".email_payer as Receipt_eMailPayer"+
		","+ALIAS+".payment_method as Receipt_paymentMethod,"+ALIAS+".fee as Receipt_fee"+
		","+ALIAS+".payment_date_time as Receipt_paymentDateTime,"+ALIAS+".application_date as Receipt_applicationDate"+
		","+ALIAS+".transfer_date as Receipt_transferDate,"+ALIAS+".transfer_amount_1 as Receipt_transferAmount1"+
		","+ALIAS+".fiscal_code_pa_1 as Receipt_fiscalCodePA1,"+ALIAS+".iban_1 as Receipt_iban1"+
		","+ALIAS+".remittance_information_1 as Receipt_remittanceInformation1"+
		","+ALIAS+".transfer_category_1 as Receipt_transferCategory1,"+ALIAS+".transfer_amount_2 as Receipt_transferAmount2"+
		","+ALIAS+".fiscal_code_pa_2 as Receipt_fiscalCodePA2,"+ALIAS+".iban_2 as Receipt_iban2"+
		","+ALIAS+".remittance_information_2 as Receipt_remittanceInformation2"+
		","+ALIAS+".transfer_category_2 as Receipt_transferCategory2"+
		","+ALIAS+".dt_last_export as Receipt_dtLastExport"+
		","+ALIAS+".num_try_export as Receipt_numTryExport"+
		","+ALIAS+".status_export as Receipt_statusExport"+
		","+ALIAS+".receipt_bytes as Receipt_receiptBytes";

	private Long mygovReceiptId;

	private Date dtCreazione;
	@Nested(DovutoElaborato.ALIAS)
	private DovutoElaborato mygovDovutoElaboratoId;
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

	private Date dtLastExport;
	private int numTryExport;
	private char statusExport;
	private byte[] receiptBytes;
}