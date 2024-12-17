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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Component
@Slf4j
@ConditionalOnWebApplication
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

  public static final String TOKEN_ERROR_CODE_ATTRIB = "_TOKEN_ERROR_CODE";
  public static final String TOKEN_ERROR_CODE_EXPIRED = "TOKEN_EXPIRED";
  public static final String TOKEN_ERROR_CODE_USED = "TOKEN_ALREADY_USED";
  public static final String TOKEN_ERROR_CODE_INVALID = "TOKEN_INVALID";
  public static final String TOKEN_ERROR_CODE_INSUFFICIENT = "TOKEN_LEVEL_INSUFFICIENT";
  public static final String TOKEN_ERROR_CODE_MISSING = "TOKEN_MISSING";
  public static final String TOKEN_LOGGED_OUT = "_TOKEN_LOGGED_OUT";

  public static final List<String> NOT_REMOVE_AUTH = List.of(TOKEN_ERROR_CODE_MISSING, TOKEN_ERROR_CODE_USED);

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException {
    //log.warn(request.getRequestURI(), authException.toString());
    String error = (String)request.getAttribute(TOKEN_ERROR_CODE_ATTRIB);
    String message = error != null ? error : "Unauthorized";
    if(!TOKEN_ERROR_CODE_MISSING.equals(error) || !request.getRequestURL().toString().endsWith(JwtAuthenticationController.CHECK_LOGIN_COOKIE_PATH))
      log.warn("request: [{}] - authentication error: {}", request.getRequestURL(), message, authException);
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
  }
}
