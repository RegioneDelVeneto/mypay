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

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtResponse;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.HandleRtEvent;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.PaSendRTDto;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Receipt;
import it.regioneveneto.mygov.payment.mypay4.util.TriFunction;
import it.regioneveneto.mygov.payment.mypay4.ws.client.PagamentiTelematiciCCPPaClient;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciCCPPaImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.pa.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiFunction;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciCCPService {

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String nodoRegionaleFespIdentificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String nodoRegionaleFespIdentificativoStazioneIntermediarioPA;

  @Value("${pa.gpd.stazioneIntermediariaBroadcast:}")
  private String stazioneIntermediariaBroadcastGpd;

  @Autowired
  private EnteService enteFespService;

  @Autowired
  private PagamentiTelematiciCCPPaImpl pagamentiTelematiciCCPPaPaImpl;

  @Autowired
  private PagamentiTelematiciCCPPaClient pagamentiTelematiciCCPPaPaClient;

  @Autowired
  private ReceiptService receiptService;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  public PaVerifyPaymentNoticeRes paVerifyPaymentNoticeHandler(PaVerifyPaymentNoticeReq request) {

    BiFunction<String, String, PaVerifyPaymentNoticeRes> returnFault = (String code, String descr) -> {
      PaVerifyPaymentNoticeRes response = new PaVerifyPaymentNoticeRes();
      response.setFault(new CtFaultBean());
      response.getFault().setFaultCode(code);
      response.getFault().setDescription(descr);
      response.getFault().setFaultString(descr);
      response.getFault().setId(request.getQrCode().getFiscalCode());
      response.getFault().setSerial(0);
      return response;
    };

    // check valid broker
    if (!checkValidBroker(request.getIdBrokerPA(), request.getIdStation())) {
      return returnFault.apply(FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO_CODE, FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO_STRING);
    }

    //retrieve ente
    Ente ente = enteFespService.getEnteByCodFiscale(request.getQrCode().getFiscalCode());
    if(ente==null)
      return returnFault.apply(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_CODE, FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);

    PaVerifyPaymentNotice requestPa = new PaVerifyPaymentNotice();
    requestPa.setIdBrokerPA(request.getIdBrokerPA());
    requestPa.setIdStation(request.getIdStation());
    requestPa.setIdPA(request.getIdPA());
    requestPa.setQrCodeFiscalCode(request.getQrCode().getFiscalCode());
    requestPa.setNoticeNumber(request.getQrCode().getNoticeNumber());
    PaVerifyPaymentNoticeRisposta resPa;
    //WARN: this `if` block is not related to "attivazione esterna" but just to manage case when PA/FESP are not on same location ("remote" install)
    //      "attivazione esterna" is managed inside the corresponding implementation of PA module
    String urlPaRemote = ente.getDeUrlEsterniAttiva();
    if(StringUtils.isNotBlank(urlPaRemote)) {
      // scenario where PA is on a remote location (toward this FESP installation)
      resPa = pagamentiTelematiciCCPPaPaClient.paVerifyPaymentNotice(requestPa, urlPaRemote);
    } else {
      // scenario where PA is on the same local installation of FESP
      resPa = pagamentiTelematiciCCPPaPaImpl.paVerifyPaymentNotice(requestPa);
    }

    PaVerifyPaymentNoticeRes response = new PaVerifyPaymentNoticeRes();
    if(resPa.getFault()!=null){
      response.setOutcome(StOutcome.KO);
      response.setFault(new CtFaultBean());
      response.getFault().setId(request.getQrCode().getFiscalCode());
      response.getFault().setFaultCode(resPa.getFault().getFaultCode());
      response.getFault().setFaultString(resPa.getFault().getFaultString());
      response.getFault().setDescription(resPa.getFault().getDescription());
    } else {
      response.setOutcome(StOutcome.OK);
      response.setFiscalCodePA(resPa.getFiscalCodePA());
      response.setCompanyName(resPa.getCompanyName());
      response.setOfficeName(resPa.getOfficeName());
      response.setPaymentDescription(resPa.getPaymentDescription());
      response.setPaymentList(new CtPaymentOptionsDescriptionListPA());
      response.getPaymentList().setPaymentOptionDescription(new CtPaymentOptionDescriptionPA());
      response.getPaymentList().getPaymentOptionDescription().setAmount(resPa.getAmount());
      response.getPaymentList().getPaymentOptionDescription().setDueDate(resPa.getDueDate());
      response.getPaymentList().getPaymentOptionDescription().setOptions(StAmountOption.EQ);
      response.getPaymentList().getPaymentOptionDescription().setAllCCP(resPa.isAllCCP());
    }
    return response;
  }

  public PaGetPaymentRisposta paGetPaymentHandler(PaGetPayment request) {

    BiFunction<String, String, PaGetPaymentRisposta> returnFault = (String code, String descr) -> {
      var response = new PaGetPaymentRisposta();
      response.setFault(new FaultBean());
      response.getFault().setFaultCode(code);
      response.getFault().setDescription(descr);
      response.getFault().setFaultString(descr);
      response.getFault().setId(request.getQrCodeFiscalCode());
      response.getFault().setSerial(0);
      return response;
    };

    // table mygov_attiva_rpt_e : no more needed because there is no need to track state of previous invocation of "paGetPayment" (aka payment-activation)

    // check valid broker
    if (!checkValidBroker(request.getIdBrokerPA(), request.getIdStation()))
      return returnFault.apply(FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO_CODE, FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO_STRING);

    //retrieve ente
    Ente ente = enteFespService.getEnteByCodFiscale(request.getQrCodeFiscalCode());
    if(ente==null)
      return returnFault.apply(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_CODE, FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);

    //invoke PA-module
    //WARN: this `if` block is not related to "attivazione esterna" but just to manage case when PA/FESP are not on same location ("remote" install)
    //      "attivazione esterna" is managed inside the corresponding implementation of PA module
    String urlPaRemote = ente.getDeUrlEsterniAttiva();
    if(StringUtils.isNotBlank(urlPaRemote)) {
      // scenario where PA is on a remote location (toward this FESP installation)
      return pagamentiTelematiciCCPPaPaClient.paGetPayment(request, urlPaRemote);
    } else {
      // scenario where PA is on the same local installation of FESP
      return pagamentiTelematiciCCPPaPaImpl.paGetPayment(request);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public <T> CtResponse paSendRTHandler(PaSendRTDto paSendRTDto) {
    TriFunction<StOutcome, String, String, CtResponse> manageResponse = (StOutcome outcome, String code, String descr) -> {
      var response = new CtResponse();
      response.setOutcome(outcome);
      if (StOutcome.KO == outcome) {
        response.setFault(new CtFaultBean());
        response.getFault().setFaultCode(code);
        response.getFault().setDescription(descr);
        response.getFault().setFaultString(descr);
        response.getFault().setId(paSendRTDto.getIdPA());
        response.getFault().setSerial(0);
      }
      return response;
    };

    // Retrieving Ente is no longer required due to receiving data in "broadcast" mode
    try {
      if (!checkValidBroker(paSendRTDto.getIdBrokerPA(), paSendRTDto.getIdStation()))
        return manageResponse.apply(StOutcome.KO, FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO_CODE, FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO_STRING);

      Receipt receipt = ReceiptService.mapperModel(paSendRTDto.getReceipt()).standin(paSendRTDto.isStandin()).receiptBytes(paSendRTDto.getReceiptBytes()).build();

      Long mygovReceiptId = receiptService.insertNewReceipt(receipt);

      if(mygovReceiptId == null){
        log.info("ignoring CtReceipt already present for ficalCodeEnte: {} - noticeNumber: {} - receiptId: {}",
          receipt.getFiscalCode(), receipt.getNoticeNumber(), receipt.getReceiptId());
        return manageResponse.apply(StOutcome.OK, null, null);
      }

      log.info("processing CtReceipt for ficalCodeEnte: {} - noticeNumber: {} - receiptId: {} - mygovReceiptId: {}",
        receipt.getFiscalCode(), receipt.getNoticeNumber(), receipt.getReceiptId(), mygovReceiptId);

      applicationEventPublisher.publishEvent(HandleRtEvent.builder()
        .mygovReceiptId(mygovReceiptId)
        .fiscalCode(receipt.getFiscalCode())
        .receiptId(receipt.getReceiptId())
        .noticeNumber(receipt.getNoticeNumber())
        .build());

      return manageResponse.apply(StOutcome.OK, null, null);
    } catch (Exception ex) {
      log.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
      return manageResponse.apply(StOutcome.KO, FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore generico: "+ex.getMessage());
    }
  }

  private boolean checkValidBroker(String idBrokerPA, String idStation){
    boolean ok = StringUtils.equals(nodoRegionaleFespIdentificativoIntermediarioPA, idBrokerPA) &&
        (StringUtils.equals(nodoRegionaleFespIdentificativoStazioneIntermediarioPA, idStation) ||
            (StringUtils.isNotBlank(stazioneIntermediariaBroadcastGpd) && StringUtils.equals(stazioneIntermediariaBroadcastGpd, idStation)));
    if(!ok)
      log.warn("invalid idBrokerPA-idStation[{}-{}], should be [{}-{}]", idBrokerPA, idStation, nodoRegionaleFespIdentificativoIntermediarioPA,
          StringUtils.joinWith(";",nodoRegionaleFespIdentificativoStazioneIntermediarioPA,stazioneIntermediariaBroadcastGpd));
    return ok;
  }
}
