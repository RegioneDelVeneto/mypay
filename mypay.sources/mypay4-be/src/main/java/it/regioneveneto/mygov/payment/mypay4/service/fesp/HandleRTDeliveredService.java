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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import it.gov.pagopa.pagopa_api.pa.pafornode.CtReceiptV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferPAReceiptV2;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciCCPPaImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.pa.FaultBean;
import it.veneto.regione.pagamenti.pa.PaSendRT;
import it.veneto.regione.pagamenti.pa.PaSendRTRisposta;
import it.veneto.regione.schemas._2012.pagamenti.CtReceipt;
import it.veneto.regione.schemas._2012.pagamenti.CtTransferPAReceipt;
import it.veneto.regione.schemas._2012.pagamenti.StOutcome;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
public class HandleRTDeliveredService {

	@Autowired
	ReceiptService receiptService;
	@Autowired
	Jackson2ObjectMapperBuilder mapperBuilder;
	@Autowired
	PagamentiTelematiciCCPPaImpl pagamentiTelematiciCCPPaImpl;

	@Autowired
	JAXBTransformService jaxbTransformService;

	@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
	public void handleRTDelivered(Long mygovReceiptId) {

		receiptService.getReadyByIdLockOrSkip(mygovReceiptId).ifPresentOrElse(dataModelReceipt -> {
			PaSendRTRisposta response = new PaSendRTRisposta();
			try {
				PaSendRT request = new PaSendRT();
				//set the full original object (payload from bytes representation) instead of mapped DB Data Model
				CtReceiptV2 ctReceiptV2 = jaxbTransformService.unmarshalling(dataModelReceipt.getReceiptBytes(), CtReceiptV2.class);
				boolean isReceiptManagedByMyPay;
				if(ctReceiptV2!=null){
					request.setReceipt(mapperBuilder.build().convertValue(ctReceiptV2, CtReceipt.class));
					isReceiptManagedByMyPay = receiptService.isReceiptManagedByMyPay(ctReceiptV2.getFiscalCode(), ctReceiptV2.getNoticeNumber(),
						ctReceiptV2.getTransferList().getTransfers().stream().map(CtTransferPAReceiptV2::getFiscalCodePA).collect(Collectors.toUnmodifiableSet()));
				} else {
					//fallback
					request.setReceipt(dataModelReceipt);
					isReceiptManagedByMyPay = receiptService.isReceiptManagedByMyPay(dataModelReceipt.getFiscalCode(), dataModelReceipt.getNoticeNumber(),
						dataModelReceipt.getTransferList().getTransfers().stream().map(CtTransferPAReceipt::getFiscalCodePA).collect(Collectors.toUnmodifiableSet()));
				}

				if(isReceiptManagedByMyPay) {
					request.setReceiptBytes(dataModelReceipt.getReceiptBytes());
					request.setForce(Constants.RECEIPT_STATUS_FORCE.equals(dataModelReceipt.getStatus()));
					request.setStandin(dataModelReceipt.isStandin());
					response = pagamentiTelematiciCCPPaImpl.paSendRT(request);
				} else {
					response = new PaSendRTRisposta();
					response.setOutcome(StOutcome.OK);
					response.setFault(new FaultBean());
					response.getFault().setFaultCode(FaultCodeConstants.PAA_UNMANAGED);
				}
			} catch (Exception e) {
				log.error("Error due processing handleRTDeliveredImpl", e);
				response.setOutcome(StOutcome.KO);
				var fault = new FaultBean();
				fault.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
				fault.setFaultString(FaultCodeConstants.DESC_PAA_SYSTEM_ERROR);
				response.setFault(fault);
			} finally {
				PaSendRTRisposta finalResponse = response;
				String msg = Optional.ofNullable(finalResponse.getFault())
					.map(f -> String.join(" - ", finalResponse.getOutcome().value(), f.getFaultCode(), f.getFaultString()))
					.orElse(finalResponse.getOutcome().value());
				log.info("RESPONSE SendRT for paymentToken: {} - esito/fault: {}", dataModelReceipt.getReceiptId(), msg);
				Constants.RECEIPT_STATUS status;
				if(finalResponse.getOutcome().equals(StOutcome.OK))
					status = response.getFault()!=null && FaultCodeConstants.PAA_UNMANAGED.equals(response.getFault().getFaultCode()) ? Constants.RECEIPT_STATUS.UNMNGD : Constants.RECEIPT_STATUS.SENT;
				else
					status = Constants.RECEIPT_STATUS.ERROR;
				if(response.getFault()!=null && StringUtils.equals(response.getFault().getFaultCode(), FaultCodeConstants.PAA_WAIT)){
					log.info("postpone processing of receipt {}", mygovReceiptId);
				} else {
					receiptService.updateStatus(mygovReceiptId, status.toString(), false, true);
				}
			}
		}, () -> log.info("handleRTDeliveredImpl, skip because receipt missing or locked [{}]", mygovReceiptId) );
	}
}
