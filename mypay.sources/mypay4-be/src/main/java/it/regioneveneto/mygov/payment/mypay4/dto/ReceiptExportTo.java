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
package it.regioneveneto.mygov.payment.mypay4.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceiptExportTo extends BaseTo implements Serializable {

	private Long mygovReceiptId;

	private String receiptId;
	private String noticeNumber;
	private String fiscalCode;
	private String outcome;
	private String creditorReferenceId;
	private BigDecimal paymentAmount;
	private String description;
	private String companyName;
	private String officeName;
	private String uniqueIdentifierTypeDebtor;
	private String uniqueIdentifierValueDebtor;
	private String fullNameDebtor;
	private String streetNameDebtor;
	private String civicNumberDebtor;
	private String postalCodeDebtor;
	private String cityDebtor;
	private String stateProvinceRegionDebtor;
	private String countryDebtor;
	private String eMailDebtor;

	private String idPSP;
	private String pspFiscalCode;
	private String pspPartitaIVA;
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
	private String eMailPayer;

	private String paymentMethod;
	private BigDecimal fee;
	private String paymentDateTime;
	private String applicationDate;
	private String transferDate;

	private List<ReceiptTransferExportTo> receiptTransferExportToList;

	private boolean flgExported;
	private String codTipoDovuto;
	private String deTipoDovuto;
	private String codTassonomico; // of primary transfer
	private String deNomeEnte;
}