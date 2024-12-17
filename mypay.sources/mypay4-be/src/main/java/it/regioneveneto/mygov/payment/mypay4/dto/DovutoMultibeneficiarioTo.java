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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DovutoMultibeneficiarioTo extends BaseTo implements Serializable {

	private String denominazioneBeneficiario;
	private String codiceIdentificativoUnivoco;
	private String ibanAddebito;
	private String indirizzoBeneficiario;
	private String civicoBeneficiario;
	private String capBeneficiario;
	private String nazioneBeneficiario;
	private String provinciaBeneficiario;
	private String localitaBeneficiario;
	private String importoSecondario;
	private String causaleMB;
	private String datiSpecificiRiscossione;

	@JsonIgnore
	private Long idDovuto;
}