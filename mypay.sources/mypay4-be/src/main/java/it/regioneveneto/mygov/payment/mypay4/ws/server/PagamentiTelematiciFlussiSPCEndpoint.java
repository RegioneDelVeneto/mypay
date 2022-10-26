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

import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciFlussiSPCImpl;
import it.veneto.regione.pagamenti.pa.PaaSILChiediElencoFlussiSPC;
import it.veneto.regione.pagamenti.pa.PaaSILChiediElencoFlussiSPCRisposta;
import it.veneto.regione.pagamenti.pa.PaaSILChiediFlussoSPC;
import it.veneto.regione.pagamenti.pa.PaaSILChiediFlussoSPCRisposta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@ConditionalOnWebApplication
public class PagamentiTelematiciFlussiSPCEndpoint extends BaseEndpoint {

  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/pa/";
  public static final String NAME = "PagamentiTelematiciFlussiSPC";

  @Autowired
  @Qualifier("PagamentiTelematiciFlussiSPCImpl")
  private PagamentiTelematiciFlussiSPCImpl pagamentiTelematiciFlussiSPC;

  @Autowired
  private SystemBlockService systemBlockService;

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediFlussoSPC")
  @ResponsePayload
  public PaaSILChiediFlussoSPCRisposta paaSILChiediFlussoSPC(
      @RequestPayload PaaSILChiediFlussoSPC request){
    systemBlockService.blockByOperationName("pa.paaSILChiediFlussoSPC");
    return pagamentiTelematiciFlussiSPC.paaSILChiediFlussoSPC(request);
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediElencoFlussiSPC")
  @ResponsePayload
  public PaaSILChiediElencoFlussiSPCRisposta paaSILChiediElencoFlussiSPC(
      @RequestPayload PaaSILChiediElencoFlussiSPC request){
    systemBlockService.blockByOperationName("pa.paaSILChiediElencoFlussiSPC");
    return pagamentiTelematiciFlussiSPC.paaSILChiediElencoFlussiSPC(request);
  }
}
