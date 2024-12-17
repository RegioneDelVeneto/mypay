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

import java.math.BigDecimal;

public class SumUtilis {

    public static String sumAmount(String importo, String importoMb) {
        return new BigDecimal(importo).add(new BigDecimal(importoMb)).toString();
    }

    public static String sumAmountPagati(String importo, String importoMb) {
        //remove thousands separator and use dot as decimal separator
        importo = importo.replace(".", "").replace(",",".");
        return sumAmount(importo, importoMb);
    }
}
