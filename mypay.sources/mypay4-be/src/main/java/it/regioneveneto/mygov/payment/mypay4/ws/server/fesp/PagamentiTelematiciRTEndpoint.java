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

import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRTRisposta;
import gov.telematici.pagamenti.ws.ppthead.IntestazionePPT;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.GiornaleElapsedDao;
import it.regioneveneto.mygov.payment.mypay4.exception.WSFaultResponseWrapperException;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.GiornaleElapsedService;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciRTImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.server.BaseEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PagamentiTelematiciRTEndpoint extends BaseEndpoint {
    public static final String NAMESPACE_URI = "http://ws.pagamenti.telematici.gov/";
    public static final String NAME = "PagamentiTelematiciRT";

  @Autowired
  private PagamentiTelematiciRTImpl pagamentiTelematiciRT;

  @Autowired
  private SystemBlockService systemBlockService;

  @Autowired
  private GiornaleElapsedService giornaleElapsedService;


  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaInviaRT")
  @ResponsePayload
  public PaaInviaRTRisposta paaInviaRT(
      @RequestPayload PaaInviaRT request,
      @SoapHeader("{http://ws.pagamenti.telematici.gov/ppthead}intestazionePPT") SoapHeaderElement header){
    systemBlockService.blockByOperationName("fesp.paaInviaRT");

    long startTime = System.currentTimeMillis();
    IntestazionePPT intestazionePPT = null;
    PaaInviaRTRisposta response = null;
    try{
      intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
      response = pagamentiTelematiciRT.paaInviaRT(request,intestazionePPT);
      return response;
    } catch(WSFaultResponseWrapperException wsfe){
      return wsfe.getFaultResponse(PaaInviaRTRisposta.class);
    } finally {
      if(intestazionePPT!=null
        && intestazionePPT.getIdentificativoDominio()!=null
        && intestazionePPT.getIdentificativoUnivocoVersamento()!=null ){
        giornaleElapsedService.insertGiornaleElapsed(GiornaleElapsedDao.Operation.paaInviaRT,
          intestazionePPT.getIdentificativoDominio(), intestazionePPT.getIdentificativoUnivocoVersamento(),
          response == null || response.getPaaInviaRTRisposta() == null || response.getPaaInviaRTRisposta().getFault() != null,
          startTime );
      }
    }

  }
}
