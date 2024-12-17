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
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciAvvisiDigitaliImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.server.BaseEndpoint;
import it.veneto.regione.pagamenti.nodoregionalefesp.CtAvvisoDigitale;
import it.veneto.regione.pagamenti.nodoregionalefesp.IntestazionePPT;
import it.veneto.regione.pagamenti.nodoregionalefesp.NodoSILInviaAvvisoDigitale;
import it.veneto.regione.pagamenti.nodoregionalefesp.NodoSILInviaAvvisoDigitaleRisposta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
public class PagamentiTelematiciAvvisiDigitaliEndpoint extends BaseEndpoint {

  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/nodoregionalefesp/";
  public static final String NAME = "PagamentiTelematiciAvvisiDigitali";

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  @Autowired
  @Qualifier("PagamentiTelematiciAvvisiDigitaliImpl")
  private PagamentiTelematiciAvvisiDigitaliImpl pagamentiTelematiciAvvisiDigitali;

  @Autowired
  GiornaleService giornaleCommonService;

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "nodoSILInviaAvvisoDigitale")
  @ResponsePayload
  public NodoSILInviaAvvisoDigitaleRisposta nodoSILInviaAvvisoDigitale(
      @RequestPayload NodoSILInviaAvvisoDigitale request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/nodoregionalefesp/ppthead}intestazionePPT") SoapHeaderElement header){
    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.FESP,
      intestazionePPT.getIdentificativoDominio(),
      Utilities.ifNotNull(request.getAvvisoDigitaleWS(), CtAvvisoDigitale::getCodiceAvviso),
      null,
      null,
      null,
      Constants.COMPONENTE_FESP,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaAvvisoDigitale.toString(),
      intestazionePPT.getIdentificativoDominio(),
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciAvvisiDigitali.nodoSILInviaAvvisoDigitale(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }

}
