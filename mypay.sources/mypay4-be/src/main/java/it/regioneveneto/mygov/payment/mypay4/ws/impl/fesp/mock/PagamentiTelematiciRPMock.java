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

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service("PagamentiTelematiciRPMock")
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "none")
public class PagamentiTelematiciRPMock implements PagamentiTelematiciRP {
  @Override
  public ChiediFlussoSPCRisposta chiediFlussoSPC(ChiediFlussoSPC request) {
    throw new MyPayException("FESP is disabled");
  }

  @Override
  public ChiediFlussoSPCPageRisposta chiediFlussoSPCPage(ChiediFlussoSPCPage request) {
    throw new MyPayException("FESP is disabled");
  }

  @Override
  public ChiediListaFlussiSPCRisposta chiediListaFlussiSPC(ChiediListaFlussiSPC request) {
    throw new MyPayException("FESP is disabled");
  }

  @Override
  public NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(NodoSILChiediCopiaEsito request) {
    throw new MyPayException("FESP is disabled");
  }

  @Override
  public NodoSILInviaRPRisposta nodoSILInviaRP(NodoSILInviaRP request, IntestazionePPT header) {
    throw new MyPayException("FESP is disabled");
  }

  @Override
  public NodoSILChiediIUVRisposta nodoSILChiediIUV(NodoSILChiediIUV request) {
    throw new MyPayException("FESP is disabled");
  }

  @Override
  public NodoSILChiediCCPRisposta nodoSILChiediCCP(NodoSILChiediCCP request) {
    throw new MyPayException("FESP is disabled");
  }

  @Override
  public NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP request) {
    throw new MyPayException("FESP is disabled");
  }

  @Override
  public NodoSILRichiediRTRisposta nodoSILRichiediRT(NodoSILRichiediRT request) {
    throw new MyPayException("FESP is disabled");
  }
}
