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
package it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.mock;

import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.IntestazionePPT;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRT;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service("PagamentiTelematiciRTMock")
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "none")
public class PagamentiTelematiciRTMock implements PagamentiTelematiciRT {
  @Override
  public PaaInviaRTRisposta paaInviaRT(PaaInviaRT request, IntestazionePPT header) {
    throw new MyPayException("FESP is disabled");
  }
}
