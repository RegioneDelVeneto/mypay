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
package it.regioneveneto.mygov.payment.mypay4.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.Nested;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovOperatoreEnteTipoDovutoId")
public class OperatoreEnteTipoDovuto extends BaseEntity {

  public static final String ALIAS = "OperatoreEnteTipoDovuto";
  public static final String FIELDS = ""+ALIAS+".mygov_operatore_ente_tipo_dovuto_id as OperatoreEnteTipoDovuto_mygovOperatoreEnteTipoDovutoId"+
      ","+ALIAS+".mygov_ente_tipo_dovuto_id as OperatoreEnteTipoDovuto_mygovEnteTipoDovutoId"+
      ","+ALIAS+".flg_attivo as OperatoreEnteTipoDovuto_flgAttivo"+
      ","+ALIAS+".mygov_operatore_id as OperatoreEnteTipoDovuto_mygovOperatoreId";

  private Long mygovOperatoreEnteTipoDovutoId;
  @Nested(EnteTipoDovuto.ALIAS)
  private EnteTipoDovuto mygovEnteTipoDovutoId;
  private boolean flgAttivo;
  @Nested(Operatore.ALIAS)
  private Operatore mygovOperatoreId;
}
