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

import org.springframework.util.Assert;

public class WSFaultResponseWrapperException extends MyPayException {

  private Object faultResponse;

  public WSFaultResponseWrapperException(Object faultResponse, Throwable cause) {
    super(cause);
    Assert.notNull(faultResponse, "faultResponse cannot be null");
    Assert.notNull(faultResponse, "cause cannot be null");
    this.faultResponse = faultResponse;
  }

  public <T> T getFaultResponse(Class<T> clazz){
    if(faultResponse.getClass().isAssignableFrom(clazz)){
      return (T)faultResponse;
    } else {
      throw new MyPayException(String.format("invalid FaultResponse: [%s] instead of [%s]", faultResponse.getClass().getName(), clazz.getName()));
    }
  }

  public Object getRawFaultResponse(){
    return faultResponse;
  }

}
