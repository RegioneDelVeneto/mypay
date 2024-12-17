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
@Schema(description = "esito del pagamento")
public class EsitoPagamentoTo extends BaseTo implements Serializable {
  @Schema(description = "lo stato del dovuto")
  private String stato;
  @Schema(description = "token per l'interrogazione ciclica dell'esito del pagamento")
  private String pollingToken;
  @Schema(description = "email del versante")
  private String versanteEmail;
  @Schema(description = "importo di centesimi di Euro", example = "100")
  private Long importo;
  @Schema(description = "indirizzo utile al download della ricevuta di pagamento in formato PDF")
  private List<String> urlDownloadRicevuta;
}
