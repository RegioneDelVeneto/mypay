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
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciFlussiSPCImpl;
import it.veneto.regione.pagamenti.pa.PaaSILChiediElencoFlussiSPC;
import it.veneto.regione.pagamenti.pa.PaaSILChiediElencoFlussiSPCRisposta;
import it.veneto.regione.pagamenti.pa.PaaSILChiediFlussoSPC;
import it.veneto.regione.pagamenti.pa.PaaSILChiediFlussoSPCRisposta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.Optional;

@Endpoint
@ConditionalOnWebApplication
public class PagamentiTelematiciFlussiSPCEndpoint extends BaseEndpoint {

  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/pa/";
  public static final String NAME = "PagamentiTelematiciFlussiSPC";

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  @Autowired
  private GiornaleService giornaleCommonService;

  @Autowired
  @Qualifier("PagamentiTelematiciFlussiSPCImpl")
  private PagamentiTelematiciFlussiSPCImpl pagamentiTelematiciFlussiSPC;

  @Autowired
  private SystemBlockService systemBlockService;

  @Autowired
  private EnteService enteService;

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediFlussoSPC")
  @ResponsePayload
  public PaaSILChiediFlussoSPCRisposta paaSILChiediFlussoSPC(
      @RequestPayload PaaSILChiediFlussoSPC request){
    systemBlockService.blockByOperationName("pa.paaSILChiediFlussoSPC");
    String idDominio = Optional.ofNullable(request.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      request.getIdentificativoPsp(),
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediFlussoSPC.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciFlussiSPC.paaSILChiediFlussoSPC(request),
      OutcomeHelper::getOutcome
    );
  }

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaSILChiediElencoFlussiSPC")
  @ResponsePayload
  public PaaSILChiediElencoFlussiSPCRisposta paaSILChiediElencoFlussiSPC(
      @RequestPayload PaaSILChiediElencoFlussiSPC request){
    systemBlockService.blockByOperationName("pa.paaSILChiediElencoFlussiSPC");
    String idDominio = Optional.ofNullable(request.getCodIpaEnte()).map(enteService::getEnteByCodIpa).map(Ente::getCodiceFiscaleEnte).orElse(null);
    return giornaleCommonService.wrapRecordSoapServerEvent(
      Constants.GIORNALE_MODULO.PA,
      idDominio,
      null,
      null,
      request.getIdentificativoPsp(),
      null,
      Constants.COMPONENTE_PA,
      Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediElencoFlussiSPC.toString(),
      idDominio,
      identificativoIntermediarioPA,
      identificativoStazioneIntermediarioPA,
      null,
      () -> pagamentiTelematiciFlussiSPC.paaSILChiediElencoFlussiSPC(request),
      OutcomeHelper::getOutcome
    );
  }
}
