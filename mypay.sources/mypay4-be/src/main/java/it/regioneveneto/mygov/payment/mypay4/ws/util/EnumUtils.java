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
package it.regioneveneto.mygov.payment.mypay4.ws.util;

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumUtils {

  public enum StCodiceEsitoPagamento {
    X_0("0", 1),
    X_1("1", 2),
    X_2("2", 3),
    X_3("3", 4),
    X_4("4", 5);

    String S;
    int X;
    StCodiceEsitoPagamento(String s, int x) {
      this.S = s;
      this.X = x;
    }

    public static StCodiceEsitoPagamento forString(String s) {
      List<StCodiceEsitoPagamento> esitoPagamenti = Arrays.asList(values()).stream().filter(e -> e.S.equals(s)).collect(Collectors.toList());
      if (esitoPagamenti.size() == 1)
        return esitoPagamenti.get(0);
      throw new MyPayException("Invalido St Codice Esito Pagamento: " + s);
    }

    public static StCodiceEsitoPagamento forInt(int x) {
      List<StCodiceEsitoPagamento> esitoPagamenti = Arrays.asList(values()).stream().filter(e -> e.X == x).collect(Collectors.toList());
      if (esitoPagamenti.size() == 1)
        return esitoPagamenti.get(0);
      throw new MyPayException("Invalido St Codice Esito Pagamento: " + x);
    }

    public String toString() {
      return this.S;
    }
  }

  public enum StTipoBollo {
    X_01("01", 1);
    String S;
    int X;

    StTipoBollo(String s, int x) {
      this.S = s;
      this.X = x;
    }

    public static StTipoBollo forString(String s) {
      List<StTipoBollo> esitoPagamenti = Arrays.asList(values()).stream().filter(e -> e.S.equals(s)).collect(Collectors.toList());
      if (esitoPagamenti.size() == 1)
        return esitoPagamenti.get(0);
      throw new MyPayException("Invalido St Codice Esito Pagamento: " + s);
    }

    public static StTipoBollo forInt(int x) {
      List<StTipoBollo> esitoPagamenti = Arrays.asList(values()).stream().filter(e -> e.X == x).collect(Collectors.toList());
      if (esitoPagamenti.size() == 1)
        return esitoPagamenti.get(0);
      throw new MyPayException("Invalido St Codice Esito Pagamento: " + x);
    }

    public String toString() {
      return this.S;
    }
  }

  public enum StFirmaRicevuta {
    X_0("0", 1),
    X_1("1", 2),
    X_3("3", 3),
    X_4("4", 4);

    String S;
    int X;
    StFirmaRicevuta(String s, int x) {
      this.S = s;
      this.X = x;
    }

    public static StFirmaRicevuta forString(String s) {
      List<StFirmaRicevuta> esitoPagamenti = Arrays.asList(values()).stream().filter(e -> e.S.equals(s)).collect(Collectors.toList());
      if (esitoPagamenti.size() == 1)
        return esitoPagamenti.get(0);
      throw new MyPayException("Invalido St Codice Esito Pagamento: " + s);
    }

    public static StFirmaRicevuta forInt(int x) {
      List<StFirmaRicevuta> esitoPagamenti = Arrays.asList(values()).stream().filter(e -> e.X == x).collect(Collectors.toList());
      if (esitoPagamenti.size() == 1)
        return esitoPagamenti.get(0);
      throw new MyPayException("Invalido St Codice Esito Pagamento: " + x);
    }

    public String toString() {
      return this.S;
    }

  }
}
