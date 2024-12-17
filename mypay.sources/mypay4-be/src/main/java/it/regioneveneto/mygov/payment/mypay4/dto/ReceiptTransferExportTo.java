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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceiptTransferExportTo extends BaseTo implements Serializable {

	private int idTransfer;
	private BigDecimal transferAmount;
	private String fiscalCodePA;
	private String iban;
	private String remittanceInformation; //deRpDatiVersDatiSingVersCausaleVersamento
	private String transferCategory; //deRpDatiVersDatiSingVersDatiSpecificiRiscossione

	private String deRpEnteBenefIndirizzoBeneficiario;
	private String deRpEnteBenefCivicoBeneficiario;
	private String codRpEnteBenefCapBeneficiario;
	private String deRpEnteBenefLocalitaBeneficiario;
	private String deRpEnteBenefProvinciaBeneficiario;
}