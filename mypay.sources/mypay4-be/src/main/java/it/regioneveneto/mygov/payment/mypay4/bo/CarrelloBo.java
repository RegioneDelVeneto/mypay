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
package it.regioneveneto.mygov.payment.mypay4.bo;

import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaPagatore;
import it.regioneveneto.mygov.payment.mypay4.dto.BaseTo;
import it.regioneveneto.mygov.payment.mypay4.dto.CartItem;
import it.regioneveneto.mygov.payment.mypay4.dto.common.Psp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrelloBo extends BaseTo implements Serializable {

  private long id;
  private String codIpaEnte;
  private List<CartItem> dovuti;
  private String iuv;
  private String codiceContestoPagamento;
  private String tipoCarrello;
  private String idSession;

  private CarrelloMultiBeneficiarioBo carrelloMultiBeneficiarioBo;
  private AnagraficaPagatore intestatario;
  private AnagraficaPagatore versante;
  private Psp psp;
  private BigDecimal totalAmount;
  private Long idCarrelloMulti;
  //items for single
  private String backUrl;

  public BigDecimal getTotalAmount(){
    return Stream.ofNullable(dovuti)
        .flatMap(Collection::stream)
        .map(CartItem::getImporto)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public CarrelloBo addOccurrences(List<CartItem> occurrences) {
    this.dovuti.addAll(occurrences);
    return this;
  }
}

