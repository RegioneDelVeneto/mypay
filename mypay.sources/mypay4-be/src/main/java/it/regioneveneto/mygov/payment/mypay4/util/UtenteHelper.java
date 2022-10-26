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

import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class UtenteHelper {

  private final static Logger log = LoggerFactory.getLogger(UtenteHelper.class);
  private final static Set<String> validRoles = Arrays.asList(Operatore.Role.values()).stream()
      .filter(role -> role!= Operatore.Role.ANY)
      .map(Operatore.Role::name)
      .collect(Collectors.toSet());

  public static boolean isValidRole(String role){
    //log.info("isValidRole {}: {}",role,validRoles.contains(role));
    return validRoles.contains(role);
  }
  public static boolean isInvalidRole(String role){
    return !validRoles.contains(role);
  }
}
