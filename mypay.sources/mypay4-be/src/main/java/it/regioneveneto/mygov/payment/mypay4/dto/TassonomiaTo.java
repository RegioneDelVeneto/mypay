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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(asEnum = true)
@JsonIgnoreProperties("id")
public class TassonomiaTo extends BaseTo implements Serializable {

	@FieldNameConstants.Exclude
	private Long id;
	@JsonProperty("CODICE TIPO ENTE CREDITORE")
	private String codiceTipoEnte;
	@JsonProperty("TIPO ENTE CREDITORE")
	private String descrizioneTipoEnte;
	@JsonProperty("PROGRESSIVO MACRO AREA PER ENTE CREDITORE")
	private String progressivoMacroArea;
	@JsonProperty("NOME MACRO AREA")
	private String nomeMacroArea;
	@JsonProperty("DESCRIZIONE MACRO AREA")
	private String descricioneMacroArea;
	@JsonProperty("CODICE TIPOLOGIA SERVIZIO")
	private String codiceTipoServizio;
	@JsonProperty("TIPO SERVIZIO")
	private String tipoServizio;
	@JsonProperty("MOTIVO GIURIDICO DELLA RISCOSSIONE")
	private String motivoRiscossione;
	@JsonProperty("DESCRIZIONE TIPO SERVIZIO")
	private String descrizioneTipoServizio;
	@JsonProperty("VERSIONE TASSONOMIA")
	private String versioneTassonomia;
	@JsonProperty("DATI SPECIFICI INCASSO")
	private String datiSpecificiIncasso;
	@JsonProperty("DATA INIZIO VALIDITA")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
	private Date dataInizioValidita;
	@JsonProperty("DATA FINE VALIDITA")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
	private Date dataFineValidita;

	public enum Fields {
		 ;// This is necessary!
		public String fieldName() {
			return  name();
		}
	}
}
