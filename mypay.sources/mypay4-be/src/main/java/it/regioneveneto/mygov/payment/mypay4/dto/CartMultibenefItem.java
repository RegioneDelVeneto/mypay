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

import it.regioneveneto.mygov.payment.mypay4.model.DatiMarcaBolloDigitale;
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
public class CartMultibenefItem extends BaseTo implements Serializable {

  private String causale;
  private String causaleVisualizzata;
  private String bilancio;
  private BigDecimal importo;
  private String codIpaEnte;
  private String codTipoDovuto;
  private String codStato;
  private AnagraficaPagatore intestatario;
  private String messaggioAvviso;
  private String codIuv;
  private boolean flagSpontaneo;
  private boolean avviso;

  //to download avviso
  private String securityTokenAvviso;
  private String versanteEmail;

  //only for WS paaSILInviaDovuti
  private String iud;
  private String tipoVersamento;
  private String datiSpecificiRiscossione;
  private String identificativoUnivocoFlusso;
  private DatiMarcaBolloDigitale bolloDigitale;
}
