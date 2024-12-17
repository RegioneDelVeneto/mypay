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
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.AllineamentoMyCSImpl;
import it.veneto.regione.pagamenti.ente.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.ws.Holder;

@Endpoint
@ConditionalOnWebApplication
public class AllineamentoMyCSEndpoint extends BaseEndpoint {
  public static final String NAMESPACE_URI = "http://www.regione.veneto.it/pagamenti/ente/";
  public static final String NAME = "AllineamentoMyCS";


  @Autowired
  @Qualifier("AllineamentoMyCSImpl")
  private AllineamentoMyCSImpl allineamentoMyCS;


  @Autowired
  private SystemBlockService systemBlockService;

  @Autowired
  private GiornaleService giornaleCommonService;

  @LogExecution
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "paaMCSAllineamento")
  @ResponsePayload
  public PaaMCSAllineamentoRisposta paaMCSAllineamento(
          @RequestPayload PaaMCSAllineamento request){
    systemBlockService.blockByOperationName("pa.paaMCSAllineamento");
    PaaMCSAllineamentoRisposta risposta = new PaaMCSAllineamentoRisposta();

    Holder<FaultBean> fault = new Holder<>();
    Holder<String> jsonAllineamento = new Holder<>();
    allineamentoMyCS.paaMCSAllineamento(request.getPasswordAllineamento(), null,
            fault, jsonAllineamento);
    risposta.setFault(fault.value);
    risposta.setJsonAllineamento(jsonAllineamento.value);

    return giornaleCommonService.wrapRecordSoapServerEvent(
            Constants.GIORNALE_MODULO.PA,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            () -> risposta,
            OutcomeHelper::getOutcome
            );
  }
}
