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
package it.regioneveneto.mygov.payment.mypay4.config;

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.security.JwtAuthenticationEntryPoint;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.RequestDispatcher;
import java.util.Map;

@Component
@Slf4j
public class ErrorAttributes extends DefaultErrorAttributes {

  @Override
  public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
    // customize behaviour (message shown to users) based on exception type
    String customMessage = null;
    Throwable error = getError(webRequest);
    //HttpStatus status = getStatus(webRequest);

    //custom behaviour for PSQLExceptions
    if(error instanceof UnableToExecuteStatementException){
      if(error.getCause() instanceof PSQLException){
        PSQLException psqlException = (PSQLException) error.getCause();
        ServerErrorMessage sem = psqlException.getServerErrorMessage();
        //case of "value too long for field..:"
        if(sem!=null && PSQLState.STRING_DATA_RIGHT_TRUNCATION.getState().equals(sem.getSQLState())){
          //value too long for string field exception: return bed request
          customMessage = "È stato inserito un valore troppo lungo in un campo";
        }
        log.debug("ServerErrorMessage: {}",sem);
      }
    } else if(error instanceof MyPayException){
      customMessage = error.getMessage();
    }


    final Map<String, Object> errorAttributes =  super.getErrorAttributes(webRequest, options);

    String tokenError = (String) webRequest.getAttribute(JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_ATTRIB, WebRequest.SCOPE_REQUEST);
    if(tokenError!=null) {
      errorAttributes.put("message", tokenError);
    }
    if(customMessage!=null) {
      errorAttributes.put("message", customMessage);
    }

    return errorAttributes;
  }

  protected HttpStatus getStatus(WebRequest request) {
    Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE, WebRequest.SCOPE_REQUEST);
    if (statusCode == null) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    try {
      return HttpStatus.valueOf(statusCode);
    }
    catch (Exception ex) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}