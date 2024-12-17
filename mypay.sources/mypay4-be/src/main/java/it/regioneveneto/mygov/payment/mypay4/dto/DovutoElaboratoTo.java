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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DovutoElaboratoTo extends BaseTo implements Serializable {

  private long id;
  private String causale;
  private String importo;
  private long importoAsCent;
  private String valuta;
  private LocalDate dataScadenza;
  private LocalDate dataPagamento;

  private String codStato;
  private String stato;
  private String statoComplessivo;

  private String modPagamento;

  //Campi valorizzati solo in caso di dovuto in stato TRANSAZIONE_CONCLUSA
  private LocalDateTime dataInizioTransazione;
  private String identificativoTransazione;
  private String intestatario;
  private String pspScelto;

  //Campi valorizzati solo in caso di dovuto in stato PAGATO
  private String commissioniApplicatePsp;
  private String allegatoRicevutaCodiceTipo;
  private String allegatoRicevutaTipo;
  private String allegatoRicevutaTest;

  private String email;

  private boolean showStampaRicevutaButton;

  private Long enteId;
  private String codIpaEnte;
  private String codFiscaleEnte;
  private String enteDeNome;
  private String codTipoDovuto;
  private String deTipoDovuto;

  private String codIuv;
  private String numeroAvviso;
  private String securityTokenRt;

  private DovutoElaboratoEntePrimarioTo entePrimarioElaboratoDetail;

  private DovutoMultibeneficiarioElaboratoTo dovutoMultibeneficiario;
  private boolean isMultibeneficiario;
}
