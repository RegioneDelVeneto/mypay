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
package it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

  @AllArgsConstructor
  public enum ID_TRANSFER {
    ID_1(1), ID_2(2), ID_3(3), ID_4(4), ID_5(5);

    @JsonValue
    private final int index;

    @JsonCreator
    public static ID_TRANSFER get(int index){
      return get(Integer.toString(index));
    }

    public static ID_TRANSFER get(String index){
      return ID_TRANSFER.valueOf("ID_"+index);
    }

    @Override
    public String toString() {
      return Integer.toString(this.index);
    }
  }

  private ID_TRANSFER idTransfer;
  private Long amount;
  private String organizationFiscalCode;
  private String remittanceInformation;
  private String category;
  private String iban;
  private String postalIban;
  private Stamp stamp;
}
