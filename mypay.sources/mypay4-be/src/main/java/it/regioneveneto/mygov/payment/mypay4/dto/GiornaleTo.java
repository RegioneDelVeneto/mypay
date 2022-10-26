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
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GiornaleTo extends BaseTo implements Serializable {
  private Long mygovGiornaleId;
  private LocalDateTime dataOraEvento;
  private String idDominio;
  private String iuv;
  private String evento;
  private String idPsp;

  private String tipo;
  private String sottotipo;
  private String categoria;
  private String esito;
  private String contestoPagamento;
  private String tipoVersamento;
  private String componente;
  private String idFruitore;
  private String idErogatore;
  private String idStazione;
  private String canalePagamento;
  private String parametriInterfaccia;
}
