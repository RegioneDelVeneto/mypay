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

import gov.telematici.pagamenti.ws.PaaAttivaRPT;
import gov.telematici.pagamenti.ws.PaaAttivaRPTRisposta;
import gov.telematici.pagamenti.ws.PaaVerificaRPT;
import gov.telematici.pagamenti.ws.PaaVerificaRPTRisposta;
import gov.telematici.pagamenti.ws.ppthead.IntestazionePPT;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciCCP;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service("PagamentiTelematiciCCPMock")
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "none")
public class PagamentiTelematiciCCPMock implements PagamentiTelematiciCCP {
  @Override
  public PaaVerificaRPTRisposta paaVerificaRPT(PaaVerificaRPT request, IntestazionePPT header) {
    throw new MyPayException("FESP is disabled");
  }

  @Override
  public PaaAttivaRPTRisposta paaAttivaRPT(PaaAttivaRPT request, IntestazionePPT header) {
    throw new MyPayException("FESP is disabled");
  }
}
