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
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.OutcomeHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciCCPPaImpl;
import it.veneto.regione.pagamenti.pa.*;
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

	@Autowired
	private GiornaleService giornaleCommonService;

	@LogExecution
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "paVerifyPaymentNotice")
	@ResponsePayload
	public PaVerifyPaymentNoticeRisposta paVerifyPaymentNotice(
		@RequestPayload PaVerifyPaymentNotice request){
		return giornaleCommonService.wrapRecordSoapServerEvent(
			Constants.GIORNALE_MODULO.PA,
			request.getQrCodeFiscalCode(),
			Utilities.numeroAvvisoToIuvValidator(request.getNoticeNumber()),
			null,
			null,
			Constants.PAY_PRESSO_PSP,
			Constants.COMPONENTE_PA,
			Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
			Constants.GIORNALE_TIPO_EVENTO_FESP.paVerifyPaymentNotice.toString(),
			request.getIdBrokerPA(),
			request.getIdPA(),
			request.getIdStation(),
			null,
			() -> pagamentiTelematiciCCPPa.paVerifyPaymentNotice(request),
			OutcomeHelper::getOutcome
		);
	}

	@LogExecution
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "paGetPayment")
	@ResponsePayload
	public PaGetPaymentRisposta paGetPayment(
		@RequestPayload PaGetPayment request){
		return giornaleCommonService.wrapRecordSoapServerEvent(
			Constants.GIORNALE_MODULO.PA,
			request.getQrCodeFiscalCode(),
			Utilities.numeroAvvisoToIuvValidator(request.getNoticeNumber()),
			null,
			null,
			Constants.PAY_PRESSO_PSP,
			Constants.COMPONENTE_PA,
			Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString(),
			Constants.GIORNALE_TIPO_EVENTO_FESP.paGetPayment.toString(),
			request.getIdBrokerPA(),
			request.getIdPA(),
			request.getIdStation(),
			null,
			() -> pagamentiTelematiciCCPPa.paGetPayment(request),
			OutcomeHelper::getOutcome
		);
	}

}
