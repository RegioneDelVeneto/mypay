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
package it.regioneveneto.mygov.payment.mypay4.model.common;

import it.regioneveneto.mygov.payment.mypay4.dto.BaseTo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder=true)
public abstract class Giornale extends BaseTo {

  private Long mygovGiornaleId;
  private int version;
  private Date dataOraEvento;
  private String identificativoDominio;
  private String identificativoUnivocoVersamento;
  private String codiceContestoPagamento;
  private String identificativoPrestatoreServiziPagamento;
  private String tipoVersamento;
  private String componente;
  private String categoriaEvento;
  private String tipoEvento;
  private String sottoTipoEvento;
  private String identificativoFruitore;
  private String identificativoErogatore;
  private String identificativoStazioneIntermediarioPa;
  private String canalePagamento;
  private String parametriSpecificiInterfaccia;
  private String esito;
}
