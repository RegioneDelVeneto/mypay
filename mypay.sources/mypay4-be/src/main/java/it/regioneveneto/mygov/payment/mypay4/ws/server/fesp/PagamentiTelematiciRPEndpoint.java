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
package it.regioneveneto.mygov.payment.mypay4.ws.server.fesp;

import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciRPImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.server.BaseEndpoint;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;

@Endpoint
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@ConditionalOnWebApplication
public class PagamentiTelematiciRPEndpoint extends BaseEndpoint {
  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/nodoregionalefesp/";
  public static final String NAME = "PagamentiTelematiciRP";

  @Autowired
  @Qualifier("PagamentiTelematiciRPImpl")
  private PagamentiTelematiciRPImpl pagamentiTelematiciRP;

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "chiediFlussoSPC")
  @ResponsePayload
  public ChiediFlussoSPCRisposta chiediFlussoSPC(@RequestPayload ChiediFlussoSPC request){
    return pagamentiTelematiciRP.chiediFlussoSPC(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "chiediFlussoSPCPage")
  @ResponsePayload
  public ChiediFlussoSPCPageRisposta chiediFlussoSPCPage(@RequestPayload ChiediFlussoSPCPage request){
    return pagamentiTelematiciRP.chiediFlussoSPCPage(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "chiediListaFlussiSPC")
  @ResponsePayload
  public ChiediListaFlussiSPCRisposta chiediListaFlussiSPC(@RequestPayload ChiediListaFlussiSPC request){
    return pagamentiTelematiciRP.chiediListaFlussiSPC(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILChiediCopiaEsito")
  @ResponsePayload
  public NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(@RequestPayload NodoSILChiediCopiaEsito request){
    return pagamentiTelematiciRP.nodoSILChiediCopiaEsito(request);
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILInviaRP")
  @ResponsePayload
  public NodoSILInviaRPRisposta nodoSILInviaRP(
      @RequestPayload NodoSILInviaRP request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/nodoregionalefesp/ppthead}intestazionePPT") SoapHeaderElement header){
    return pagamentiTelematiciRP.nodoSILInviaRP(request, unmarshallHeader(header, IntestazionePPT.class));
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILChiediIUV")
  @ResponsePayload
  public NodoSILChiediIUVRisposta nodoSILChiediIUV(@RequestPayload NodoSILChiediIUV request){
    return pagamentiTelematiciRP.nodoSILChiediIUV(request);
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILInviaCarrelloRP")
  @ResponsePayload
  public NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(@RequestPayload NodoSILInviaCarrelloRP request) {
    return pagamentiTelematiciRP.nodoSILInviaCarrelloRP(request);
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILRichiediRT")
  @ResponsePayload
  public NodoSILRichiediRTRisposta nodoSILRichiediRT(@RequestPayload NodoSILRichiediRT request) {
    return pagamentiTelematiciRP.nodoSILRichiediRT(request);
  }
}
