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

import gov.telematici.pagamenti.ws.NodoInviaAvvisoDigitale;
import gov.telematici.pagamenti.ws.NodoInviaAvvisoDigitaleRisposta;
import gov.telematici.pagamenti.ws.sachead.IntestazionePPT;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.ws.client.BaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class PagamentiTelematiciAvvisiDigitaliServiceClient extends BaseClient {

  @Autowired
  SystemBlockService systemBlockService;

  public NodoInviaAvvisoDigitaleRisposta nodoInviaAvvisoDigitale(NodoInviaAvvisoDigitale request, IntestazionePPT header) {
    systemBlockService.blockByOperationName("fesp.client.nodoInviaAvvisoDigitale");
    return (NodoInviaAvvisoDigitaleRisposta) getWebServiceTemplate().marshalSendAndReceive(request, getMessageCallback(header));
  }



}
