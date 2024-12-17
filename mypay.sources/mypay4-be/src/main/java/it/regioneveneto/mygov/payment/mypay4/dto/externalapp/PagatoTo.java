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
import java.time.LocalDate;

@Data
@SuperBuilder(toBuilder = true)
@Schema(description = "informazioni del pagamento effettuato")
public class PagatoTo extends BaseTo implements Serializable {
  @Schema(description = "lo stato del dovuto")
  private CodeDescriptionTo stato;
  @Schema(description = "Numero avviso", example = "3010000000001234")
  private String numeroAvviso;
  @Schema(description = "identificativo univoco versamento", example = "010000000001234")
  private String iuv;
  private EnteTo ente;
  private TipoDovutoTo tipoDovuto;
  @Schema(description = "causale del versamento")
  private String causale;
  @Schema(description = "Codice fiscale del debitore", example = "TSTTNT80A01H501O")
  private String codiceFicaleIntestatario;
  @Schema(description = "anagrafica del debitore", example = "Mario Rossi")
  private String anagaraficaIntestatario;
  @Schema(description = "email del versante")
  private String email;
  @Schema(description = "Data del pagamento", example = "2023/08/23")
  private LocalDate dataPagamento;
  @Schema(description = "importo di centesimi di Euro", example = "100")
  private Long importo;
  @Schema(description = "indirizzo utile al download della ricevuta telematica in formato PDF")
  private String urlDownloadRicevuta;
}
