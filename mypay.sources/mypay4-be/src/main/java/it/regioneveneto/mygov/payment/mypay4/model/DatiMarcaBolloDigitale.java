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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovDatiMarcaBolloDigitaleId")
public class DatiMarcaBolloDigitale {

  public final static String ALIAS = "DatiMarcaBolloDigitale";
  public final static String FIELDS = ""+ALIAS+".mygov_dati_marca_bollo_digitale_id as DatiMarcaBolloDigitale_mygovDatiMarcaBolloDigitaleId"+
      ","+ALIAS+".version as DatiMarcaBolloDigitale_version,"+ALIAS+".tipo_bollo as DatiMarcaBolloDigitale_tipoBollo"+
      ","+ALIAS+".hash_documento as DatiMarcaBolloDigitale_hashDocumento"+
      ","+ALIAS+".provincia_residenza as DatiMarcaBolloDigitale_provinciaResidenza";

  private Long mygovDatiMarcaBolloDigitaleId;
  private int version;
  private String tipoBollo;
  private String hashDocumento;
  private String provinciaResidenza;
}
