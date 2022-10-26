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
package it.regioneveneto.mygov.payment.mypay4.dto.fesp;

import it.regioneveneto.mygov.payment.mypay4.exception.fesp.FespException;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRPRisposta;

public class NodoSILInviaRPRispostaException extends FespException {
  private NodoSILInviaRPRisposta nodoSILInviaRPRisposta;

  public NodoSILInviaRPRisposta getNodoSILInviaRPRisposta() {
    return nodoSILInviaRPRisposta;
  }

  public void setNodoSILInviaRPRisposta(NodoSILInviaRPRisposta nodoSILInviaRPRisposta) {
    this.nodoSILInviaRPRisposta = nodoSILInviaRPRisposta;
  }

  private static final long serialVersionUID = 1L;

  public NodoSILInviaRPRispostaException(String message) {
    super(message);
  }

  public NodoSILInviaRPRispostaException(Throwable cause) {
    super(cause);
  }

  public NodoSILInviaRPRispostaException(String message, Throwable cause) {
    super(message, cause);
  }

  public NodoSILInviaRPRispostaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
