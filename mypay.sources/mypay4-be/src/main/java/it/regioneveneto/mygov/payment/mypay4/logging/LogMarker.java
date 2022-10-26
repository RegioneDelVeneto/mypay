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
package it.regioneveneto.mygov.payment.mypay4.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public enum LogMarker {
  //max 10 characters
  MONITORING("MON_GEN"),
  REST("MON_REST"),
  SOAP_SERVER("MON_WSS"),
  SOAP_CLIENT("MON_WSC"),
  METHOD("MON_METH"),
  DB_STATEMENT("MON_DBS"),
  DB_CONNECTION_POOL("MON_CONN");


  public final Marker marker;

  private LogMarker(String markerName) {
    this.marker = MarkerFactory.getMarker(markerName);
  }
}