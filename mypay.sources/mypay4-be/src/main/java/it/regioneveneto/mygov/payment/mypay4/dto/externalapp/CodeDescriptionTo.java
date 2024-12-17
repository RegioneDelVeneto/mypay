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
package it.regioneveneto.mygov.payment.mypay4.dto.externalapp;

import io.swagger.v3.oas.annotations.media.Schema;
import it.regioneveneto.mygov.payment.mypay4.dto.BaseTo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder(toBuilder = true)
@Schema( description = "Generico oggetto chiave (code) valore (descr)")
public class CodeDescriptionTo extends BaseTo implements Serializable {
  @Schema(description = "Codice / chiave", example = "cod")
  private String code;
  @Schema(description = "Descrizione / Valore", example = "descrizione")
  private String descr;
}