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
import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
public class ListWithCountTo<T> extends BaseTo implements Serializable {
  @Schema(description = "Numero di elementi ritornati", example = "27")
  private int count;
  @Schema(description = "Indica se sono stati ritornati o meno tutti gli elementi corrispondenti alla ricerca, nel caso si sia superato il limite massimo degli elementi restituibili", example = "false")
  private boolean incomplete;
  @Schema(description = "dati ritornati")
  private List<T> data;
}
