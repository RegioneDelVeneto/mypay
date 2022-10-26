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
package it.regioneveneto.mygov.payment.mypay4.util;

import it.regioneveneto.mygov.payment.mypay4.model.IdentificativoUnivoco;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.AttivaRptE;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EntityHelper {
  private static final Map<String, String> prefixFieldList = new HashMap();

  public static String getFieldListWithPrefix(Class theClass, String alias, boolean hardcodeAlias) {
    return prefixFieldList.computeIfAbsent(alias + "_", key ->
        "public final static String ALIAS = \"" + alias + "\";\n" +
            "public final static String FIELDS = \"" +
            Arrays.stream(theClass.getDeclaredFields())
                .filter(field -> Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()))
                .map(field -> hardcodeAlias ? (alias + ".") : "\"+ALIAS+\"." + camelToSnake(field.getName()) + " as " + key + field.getName())
                .reduce((s, e) -> {
                  return s + ((s.substring(Math.max(0, s.lastIndexOf("\n"))).length() + e.length() > 120) ? "\"+\n\"" : "") + "," + e;
                }).get() + "\";"
    );
  }

  public static String getFieldListWithPrefix(Class theClass) {
    String alias = theClass.getSimpleName();
    if(theClass.getPackageName().endsWith(".fesp"))
      alias = "FESP_"+alias;
    return getFieldListWithPrefix(theClass, alias, false);
  }

  // Function to covert camel case
  // string to snake case string
  public static String camelToSnake(String str) {

    // Empty String
    String result = "";

    // Append first character(in lower case)
    // to result string
    char c = str.charAt(0);
    result = result + Character.toLowerCase(c);

    // Tarverse the string from
    // ist index to last index
    for (int i = 1; i < str.length(); i++) {

      char ch = str.charAt(i);

      // Check if the character is upper case
      // then append '_' and such character
      // (in lower case) to result string
      if (Character.isUpperCase(ch)) {
        result = result + '_';
        result
            = result
            + Character.toLowerCase(ch);
      }

      // If the character is lower case then
      // add such character into result string
      else {
        result = result + ch;
      }
    }

    // return the result
    return result;
  }

  public static void main(String[] args) {
    System.out.println(getFieldListWithPrefix(RegistroOperazione.class));
  }

}
