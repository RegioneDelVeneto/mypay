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


import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import org.apache.commons.lang3.StringUtils;

public class WSFaultException extends MyPayException {
  private final String faultCode;
  private final String faultDescription;

  public String getFaultCode() {
    return faultCode;
  }

  public String getFaultDescription() {
    return faultDescription;
  }

  public WSFaultException(String faultCode, String faultDescription, Throwable cause){
    super("faultCode: "+faultCode+(StringUtils.isNotBlank(faultDescription)?(" - faultDescription: "+faultDescription):""), cause);
    this.faultCode = faultCode;
    this.faultDescription = faultDescription;
  }

  public WSFaultException(String faultCode, String faultDescription){
    this(faultCode, faultDescription, null);
  }

  public WSFaultException(Throwable cause){
    this(FaultCodeConstants.OTHER_ERROR, null, cause);
  }
}
