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
package it.regioneveneto.mygov.payment.mypay4.ws.server;

import it.regioneveneto.mygov.payment.mypay4.exception.WSFaultResponseWrapperException;
import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciCCPPaImpl;
import it.veneto.regione.pagamenti.pa.PaaSILAttivaRP;
import it.veneto.regione.pagamenti.pa.PaaSILAttivaRPRisposta;
import it.veneto.regione.pagamenti.pa.PaaSILVerificaRP;
import it.veneto.regione.pagamenti.pa.PaaSILVerificaRPRisposta;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
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
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "remote")
@ConditionalOnWebApplication
public class PagamentiTelematiciCCPPaEndpoint extends BaseEndpoint {
  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/pa/";
  public static final String NAME = "PagamentiTelematiciCCPPa";

  @Autowired
  @Qualifier("PagamentiTelematiciCCPPaImpl")
  private PagamentiTelematiciCCPPaImpl pagamentiTelematiciCCPPa;

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILAttivaRP")
  @ResponsePayload
  public PaaSILAttivaRPRisposta paaSILAttivaRP(
          @RequestPayload PaaSILAttivaRP request,
          @SoapHeader("{http://www.regione.veneto.it/pagamenti/pa/ppthead}intestazionePPT") SoapHeaderElement header){
    try {
      return pagamentiTelematiciCCPPa.paaSILAttivaRP(request, unmarshallHeader(header, IntestazionePPT.class));
    } catch(WSFaultResponseWrapperException wsfe){
      return wsfe.getFaultResponse(PaaSILAttivaRPRisposta.class);
    }
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILVerificaRP")
  @ResponsePayload
  public PaaSILVerificaRPRisposta paaSILVerificaRP(
      @RequestPayload PaaSILVerificaRP request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/pa/ppthead}intestazionePPT") SoapHeaderElement header){
    try {
      return pagamentiTelematiciCCPPa.paaSILVerificaRP(request, unmarshallHeader(header, IntestazionePPT.class));
    } catch(WSFaultResponseWrapperException wsfe){
      return wsfe.getFaultResponse(PaaSILVerificaRPRisposta.class);
    }
  }

}
