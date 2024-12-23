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
package it.regioneveneto.mygov.payment.mypay4.security;

import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;

@SuppressWarnings("java:S110")
public class AlreadyUsedJwtException extends ClaimJwtException {
  public AlreadyUsedJwtException(Header header, Claims claims, String message) {
    super(header, claims, message);
  }

  /**
   * @param header jwt header
   * @param claims jwt claims (body)
   * @param message exception message
   * @param cause cause
   * @since 0.5
   */
  public AlreadyUsedJwtException(Header header, Claims claims, String message, Throwable cause) {
    super(header, claims, message, cause);
  }
}
