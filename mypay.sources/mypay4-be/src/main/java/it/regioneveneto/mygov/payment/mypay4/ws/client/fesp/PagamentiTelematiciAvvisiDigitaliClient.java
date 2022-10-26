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
package it.regioneveneto.mygov.payment.mypay4.ws.client.fesp;

import it.regioneveneto.mygov.payment.mypay4.ws.client.BaseClient;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciAvvisiDigitali;
import it.veneto.regione.pagamenti.nodoregionalefesp.IntestazionePPT;
import it.veneto.regione.pagamenti.nodoregionalefesp.NodoSILInviaAvvisoDigitale;
import it.veneto.regione.pagamenti.nodoregionalefesp.NodoSILInviaAvvisoDigitaleRisposta;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PagamentiTelematiciAvvisiDigitaliClient extends BaseClient implements PagamentiTelematiciAvvisiDigitali {

  @Override
  public NodoSILInviaAvvisoDigitaleRisposta nodoSILInviaAvvisoDigitale(NodoSILInviaAvvisoDigitale bodyrichiesta, IntestazionePPT header) {
    return (NodoSILInviaAvvisoDigitaleRisposta) getWebServiceTemplate().marshalSendAndReceive(bodyrichiesta, getMessageCallback(header));
  }
}
