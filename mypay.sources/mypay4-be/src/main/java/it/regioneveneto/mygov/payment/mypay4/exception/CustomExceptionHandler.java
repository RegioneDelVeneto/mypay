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
package it.regioneveneto.mygov.payment.mypay4.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

  private final static String ERROR_MESSAGE_TAG = "_ERROR_MESSAGE_";
  private final static String HTML_TEMPLATE = String.join("\n"
      , "<html>"
      , "  <head>"
      , "    <style>"
      , "      .center { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); }"
      , "    </style>"
      , "  </head>"
      , "  <body>"
      , "    <div class='center'><p>"
      , ERROR_MESSAGE_TAG
      , "    </p></div>"
      , "  </body>"
      , "</html>");

  @ExceptionHandler(value = { InsufficientAuthenticationException.class })
  public ResponseEntity handleInsufficientAuthenticationException(InsufficientAuthenticationException ex, final HttpServletRequest httpServletRequest) {
    String errorMessage = StringUtils.firstNonBlank(ex.getMessage(),
        "L'utente usato per accedere non ha i diritti per accedere a questa applicazione.");

    log.error("handleException, show error page with message: {}", errorMessage, ex);

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.TEXT_HTML)
        .body(StringUtils.replace(HTML_TEMPLATE, ERROR_MESSAGE_TAG, errorMessage));
  }

  @ExceptionHandler(value = { RecaptchaFallbackException.class })
  public ResponseEntity<?> handleRecaptchaFallbackException(RecaptchaFallbackException ex, final HttpServletRequest httpServletRequest) {
  log.error("handleException RecaptchaFallbackException score[{}]", ex.getScore(), ex);
    return ResponseEntity.status(471)
        .contentType(MediaType.TEXT_HTML)
        .body("recaptcha_low_score");
  }

}
