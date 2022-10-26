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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DovutoTo extends BaseTo implements Serializable {

  private long id;
  private String codIud;
  private String causale;
  private String causaleVisualizzata;
  private String importo;
  private String valuta;
  private String codStato;
  private String deEnte;
  private String codIpaEnte;
  private String codTipoDovuto;
  private String deTipoDovuto;
  private LocalDate dataScadenza;
  private String deStato;
  private List<String> modPagamento;
  private boolean avviso;
  private String codIuv;
  private boolean isMultiIntestatario;
  private String numeroAvviso;
  private String intestatarioAvviso;

  private AnagraficaPagatore intestatario;

  private String securityTokenAvviso;
}
