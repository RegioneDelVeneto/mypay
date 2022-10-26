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

import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediFlussoSPC;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediFlussoSPCPage;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediFlussoSPCPageRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediFlussoSPCRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediListaFlussiSPC;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediListaFlussiSPCRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediCopiaEsito;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediCopiaEsitoRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediIUV;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediIUVRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRPRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRPRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILRichiediRT;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILRichiediRTRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT;

public interface PagamentiTelematiciRP {
  ChiediFlussoSPCRisposta chiediFlussoSPC(ChiediFlussoSPC request);
  ChiediFlussoSPCPageRisposta chiediFlussoSPCPage(ChiediFlussoSPCPage request);
  ChiediListaFlussiSPCRisposta chiediListaFlussiSPC(ChiediListaFlussiSPC request);
  NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(NodoSILChiediCopiaEsito request);
  NodoSILInviaRPRisposta nodoSILInviaRP(NodoSILInviaRP request, IntestazionePPT header);
  NodoSILChiediIUVRisposta nodoSILChiediIUV(NodoSILChiediIUV request);
  NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP request);
  NodoSILRichiediRTRisposta nodoSILRichiediRT(NodoSILRichiediRT request);
}
