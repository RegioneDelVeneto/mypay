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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "l'oggetto del pagamento")
public class PagamentoTo extends BaseTo implements Serializable {
  @NotBlank(message = "idPagamento è obbligatorio")
  private String idPagamento;
  @NotBlank(message = "replicaCheckPaymentId è obbligatorio")
  private String replicaCheckPaymentId;
  @NotBlank(message = "versanteAnagrafica è obbligatorio")
  private String versanteAnagrafica;
  private String versanteCf;
  @NotBlank(message = "versanteEmail è obbligatorio")
  @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
    message = "The email address is invalid.", flags = { Pattern.Flag.CASE_INSENSITIVE })
  private String versanteEmail;
  @NotBlank(message = "callbackUrl è obbligatorio")
  private String callbackUrl;
}
