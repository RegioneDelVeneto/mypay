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
package it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp;

import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT;

public interface PagamentiTelematiciRP {
  ChiediFlussoSPCRisposta chiediFlussoSPC(ChiediFlussoSPC request);
  ChiediFlussoSPCPageRisposta chiediFlussoSPCPage(ChiediFlussoSPCPage request);
  ChiediListaFlussiSPCRisposta chiediListaFlussiSPC(ChiediListaFlussiSPC request);
  NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(NodoSILChiediCopiaEsito request);
  NodoSILInviaRPRisposta nodoSILInviaRP(NodoSILInviaRP request, IntestazionePPT header);
  NodoSILChiediIUVRisposta nodoSILChiediIUV(NodoSILChiediIUV request);
  NodoSILChiediCCPRisposta nodoSILChiediCCP(NodoSILChiediCCP request);
  NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP request);
  NodoSILRichiediRTRisposta nodoSILRichiediRT(NodoSILRichiediRT request);


}
