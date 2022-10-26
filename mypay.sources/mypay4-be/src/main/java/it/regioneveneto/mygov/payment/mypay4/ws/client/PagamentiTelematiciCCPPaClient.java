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
package it.regioneveneto.mygov.payment.mypay4.ws.client;

import it.veneto.regione.pagamenti.pa.PaaSILAttivaRP;
import it.veneto.regione.pagamenti.pa.PaaSILAttivaRPRisposta;
import it.veneto.regione.pagamenti.pa.PaaSILVerificaRP;
import it.veneto.regione.pagamenti.pa.PaaSILVerificaRPRisposta;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PagamentiTelematiciCCPPaClient extends BaseClient {

  public PaaSILVerificaRPRisposta paaSILVerificaRP(PaaSILVerificaRP request, IntestazionePPT header, String wsUrl) {
    return (PaaSILVerificaRPRisposta) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request, getMessageCallback(header));
  }

  public PaaSILAttivaRPRisposta paaSILAttivaRP(PaaSILAttivaRP request, IntestazionePPT header, String wsUrl) {
    return (PaaSILAttivaRPRisposta) getWebServiceTemplate().marshalSendAndReceive(wsUrl, request, getMessageCallback(header));
  }
}
