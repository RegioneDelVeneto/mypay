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
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciEsitoImpl;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsitoRisposta;
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
public class PagamentiTelematiciEsitoEndpoint extends BaseEndpoint {
  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/pa/";
  public static final String NAME = "PagamentiTelematiciEsito";

  @Autowired
  @Qualifier("PagamentiTelematiciEsitoImpl")
  private PagamentiTelematiciEsitoImpl pagamentiTelematiciEsito;

  @Autowired
  private GiornaleService giornaleCommonService;

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILInviaEsito")
  @ResponsePayload
  public PaaSILInviaEsitoRisposta paaSILInviaEsito(
      @RequestPayload PaaSILInviaEsito request,
      @SoapHeader("{http://www.regione.veneto.it/pagamenti/pa/ppthead}intestazionePPT") SoapHeaderElement header){

    IntestazionePPT intestazionePPT = unmarshallHeader(header, IntestazionePPT.class);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      intestazionePPT.getIdentificativoDominio(),
      intestazionePPT.getIdentificativoUnivocoVersamento(),
      intestazionePPT.getCodiceContestoPagamento(),
      null,
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
      Constants.GIORNALE_TIPO_EVENTO_FESP.paaSILAttivaRP.toString(),
      intestazionePPT.getIdentificativoIntermediarioPA(),
      intestazionePPT.getIdentificativoDominio(),
      intestazionePPT.getIdentificativoStazioneIntermediarioPA(),
      null,
      () -> pagamentiTelematiciEsito.paaSILInviaEsito(request, intestazionePPT),
      OutcomeHelper::getOutcome
    );
  }
}
