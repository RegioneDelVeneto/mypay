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
package it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.*;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.PaSendRTDto;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RPT_Conservazione;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RT_Conservazione;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.PagamentiTelematiciCCPService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.RPTConservazioneService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciCCP36;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.pa.PaGetPayment;
import it.veneto.regione.pagamenti.pa.PaGetPaymentRisposta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.function.BiFunction;

@Service("PagamentiTelematiciCCP36FespImpl")
@Slf4j
@ConditionalOnExpression("'${fesp.mode}'.equals('local') && ${pa.modelloUnico}")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)

public class PagamentiTelematiciCCP36Impl implements PagamentiTelematiciCCP36 {

  @Autowired
  private SystemBlockService systemBlockService;

  @Autowired
  private PagamentiTelematiciCCPService pagamentiTelematiciCCPService;

  @Autowired
  private JAXBTransformService jaxbTransformService;

  @Autowired
  private Jackson2ObjectMapperBuilder mapperBuilder;

  @Autowired
  private RPTConservazioneService rptConservazioneService;

  @Override
  public PaDemandPaymentNoticeResponse paDemandPaymentNotice(PaDemandPaymentNoticeRequest request) {
    BiFunction<String, String, PaDemandPaymentNoticeResponse> returnFault = (String code, String descr) -> {
      PaDemandPaymentNoticeResponse response = new PaDemandPaymentNoticeResponse();
      response.setFault(new CtFaultBean());
      response.getFault().setFaultCode(code);
      response.getFault().setDescription(descr);
      response.getFault().setFaultString(descr);
      response.getFault().setId(request.getIdPA());
      response.getFault().setSerial(0);
      return response;
    };
    // returning default KO response
    // Action not supported by Regione Veneto, look up to  https://docs.pagopa.it/sanp/casi-duso/pagamento-spontaneo-presso-psp
    return returnFault.apply(FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO, FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO_STRING);
  }

  @Override
  public PaVerifyPaymentNoticeRes paVerifyPaymentNotice(PaVerifyPaymentNoticeReq request) {
    return pagamentiTelematiciCCPService.paVerifyPaymentNoticeHandler(request);
  }

  @Override
  public PaGetPaymentV2Response paGetPaymentV2(PaGetPaymentV2Request request) {
    PaGetPayment requestPa = new PaGetPayment();
    requestPa.setIdBrokerPA(request.getIdBrokerPA());
    requestPa.setIdStation(request.getIdStation());
    requestPa.setIdPA(request.getIdPA());
    requestPa.setIdPA(request.getIdPA());
    requestPa.setQrCodeFiscalCode(request.getQrCode().getFiscalCode());
    requestPa.setNoticeNumber(request.getQrCode().getNoticeNumber());
    requestPa.setPaymentNote(request.getPaymentNote());
    requestPa.setTransferType(request.getTransferType()!=null ? it.veneto.regione.schemas._2012.pagamenti.StTransferType.fromValue(request.getTransferType().value()) : null);

    var resPa = pagamentiTelematiciCCPService.paGetPaymentHandler(requestPa);

    //convert response
    PaGetPaymentV2Response response = new PaGetPaymentV2Response();
    if(resPa.getFault()!=null){
      response.setOutcome(StOutcome.KO);
      response.setFault(new CtFaultBean());
      response.getFault().setId(request.getQrCode().getFiscalCode());
      response.getFault().setFaultCode(resPa.getFault().getFaultCode());
      response.getFault().setFaultString(resPa.getFault().getFaultString());
      response.getFault().setDescription(resPa.getFault().getDescription());
    } else {
      response.setOutcome(StOutcome.OK);
      response.setData(new CtPaymentPAV2());
      response.getData().setDescription(resPa.getDescription());
      response.getData().setCompanyName(resPa.getCompanyName());
      response.getData().setDueDate(resPa.getDueDate());
      response.getData().setPaymentAmount(resPa.getPaymentAmount());
      response.getData().setCreditorReferenceId(resPa.getCreditorReferenceId());
      response.getData().setOfficeName(resPa.getOfficeName());
      response.getData().setRetentionDate(resPa.getRetentionDate());
      response.getData().setDebtor(new CtSubject());
      response.getData().getDebtor().setUniqueIdentifier(new CtEntityUniqueIdentifier());
      response.getData().getDebtor().getUniqueIdentifier().setEntityUniqueIdentifierType(StEntityUniqueIdentifierType.fromValue(resPa.getDebtor().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().value()));
      response.getData().getDebtor().getUniqueIdentifier().setEntityUniqueIdentifierValue(resPa.getDebtor().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());
      response.getData().getDebtor().setFullName(resPa.getDebtor().getAnagraficaPagatore());
      response.getData().getDebtor().setCountry(resPa.getDebtor().getNazionePagatore());
      response.getData().getDebtor().setStateProvinceRegion(resPa.getDebtor().getProvinciaPagatore());
      response.getData().getDebtor().setCity(resPa.getDebtor().getLocalitaPagatore());
      response.getData().getDebtor().setPostalCode(resPa.getDebtor().getCapPagatore());
      response.getData().getDebtor().setStreetName(resPa.getDebtor().getIndirizzoPagatore());
      response.getData().getDebtor().setCivicNumber(resPa.getDebtor().getCivicoPagatore());
      response.getData().getDebtor().setEMail(resPa.getDebtor().getEMailPagatore());
      response.getData().setTransferList(new CtTransferListPAV2());
      resPa.getTransferLists().forEach(elem -> {
        var elemNode = new CtTransferPAV2();
        elemNode.setFiscalCodePA(elem.getFiscalCodePA());
        if (elem.getRichiestaMarcaDaBollo() != null) {
          elemNode.setRichiestaMarcaDaBollo(new CtRichiestaMarcaDaBollo());
          elemNode.getRichiestaMarcaDaBollo().setTipoBollo(elem.getRichiestaMarcaDaBollo().getTipoBollo());
          elemNode.getRichiestaMarcaDaBollo().setHashDocumento(elem.getRichiestaMarcaDaBollo().getHashDocumento());
          elemNode.getRichiestaMarcaDaBollo().setProvinciaResidenza(elem.getRichiestaMarcaDaBollo().getProvinciaResidenza());
        } else {
          elemNode.setIBAN(elem.getIBAN());
        }
        elemNode.setIdTransfer(elem.getIdTransfer());
        elemNode.setTransferAmount(elem.getTransferAmount());
        elemNode.setRemittanceInformation(elem.getRemittanceInformation());
        elemNode.setTransferCategory(elem.getTransferCategory());
        if (elem.getMetadata() != null) {
          elemNode.setMetadata(new CtMetadata());
          elem.getMetadata().getMapEntries().forEach(entry -> {
            var mapEntry = new CtMapEntry();
            mapEntry.setKey(entry.getKey());
            mapEntry.setValue(entry.getValue());
            elemNode.getMetadata().getMapEntries().add(mapEntry);
          });
        }
        response.getData().getTransferList().getTransfers().add(elemNode);
      });
    }

    //Parte conservazione
    try {
      byte[] rptFake = jaxbTransformService.marshallingAsBytes(resPa, PaGetPaymentRisposta.class);
      RPT_Conservazione rptConservazione = RPT_Conservazione.builder()
        .rptRtEstrazioneId(0L)
        .mygovGiornaleId(0L)
        .mygovRptRtId(0L)
        .identificativoDominio(resPa.getTransferLists().get(0).getFiscalCodePA())
        .identificativoUnivocoVersamento(resPa.getCreditorReferenceId())
        .codiceContestoPagamento("N/A")
        .identificativo(resPa.getTransferLists().get(0).getFiscalCodePA()+
          "-"+
          resPa.getCreditorReferenceId()+
          "-"+
          resPa.getRetentionDate())
        .rptXML(new String(rptFake))
        .dataRegistrazione(resPa.getRetentionDate().toGregorianCalendar().getTime())
        //.oggetto(ctRPT.getDatiVersamento().getDatiSingoloVersamentoArray(0).getCausaleVersamento())
        .oggetto(resPa.getDescription())
        .tipoSoggettoPagatore("P" + resPa.getDebtor().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().value())
        .nominativoPagatore(resPa.getDebtor().getAnagraficaPagatore())
        .identificativoPagatore(resPa.getDebtor().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco())
        .indirizzoRiferimentoPagatore(resPa.getDebtor().getIndirizzoPagatore())
        .tipoSoggettoBeneficiario("PG")
        .nominativoBeneficiario(resPa.getCompanyName())
        .identificativoBeneficiario(resPa.getTransferLists().get(0).getFiscalCodePA())
        .indirizzoRiferimentoBeneficiario("N/A")
        .idAggregazione(resPa.getTransferLists().get(0).getFiscalCodePA() + "-" +
          //ctRPT.getDatiVersamento().getDatiSingoloVersamentoArray(0).getDatiSpecificiRiscossione())
          resPa.getTransferLists().get(0).getRemittanceInformation())
        .identificativoVersante(null)
        .nominativoVersante(null)
        .build();
      rptConservazioneService.insertRptConservazione(rptConservazione , "paGetPayment");
    } catch (Exception e) {
      log.error("Errore in conservazione getPayment: "+e , e);
    }

    return response;
  }

  @Override
  public PaSendRTV2Response paSendRtV2(PaSendRTV2Request request) {
    //check blacklist/whitelist codice fiscale
    systemBlockService.blockByPayerCf(Optional.ofNullable(request.getReceipt().getDebtor())
      .map(CtSubject::getUniqueIdentifier).map(CtEntityUniqueIdentifier::getEntityUniqueIdentifierValue).orElse(Constants.CODICE_FISCALE_ANONIMO));

    CtReceiptV2 receipt = request.getReceipt();
    byte[] receiptBytes = jaxbTransformService.marshallingAsBytes(receipt, CtReceiptV2.class, "receipt");
    try {
      RT_Conservazione rtConservazione = rptConservazioneService.insertRtConservazione(0L,
        receipt.getFiscalCode(), receipt.getCreditorReferenceId(),
        "N/A",
        receipt.getReceiptId(),
        receiptBytes,
        receipt.getPaymentDateTime() != null
          ? receipt.getPaymentDateTime().toGregorianCalendar().getTime()
          : new Date(),
        receipt.getDescription(),
        receipt.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierType().value(),
        receipt.getDebtor().getFullName(),
        receipt.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue(),
        receipt.getDebtor().getEMail(),
        receipt.getFiscalCode(),
        receipt.getFiscalCode() + "-" + receipt.getTransferList().getTransfers().get(0).getRemittanceInformation(),
        receipt.getPayer() != null ? receipt.getPayer().getUniqueIdentifier().getEntityUniqueIdentifierType().value() : null,
        receipt.getPayer() != null ? receipt.getPayer().getUniqueIdentifier().getEntityUniqueIdentifierValue() : null,
        "0", "paSendRT"
      );
      log.debug("CONSERVAZIONE: " + rtConservazione);
    } catch (Exception ex) {
      log.error("Errore in conservazione receipt: "+ex , ex);
    }

    PaSendRTDto paSendRTDto = PaSendRTDto.builder()
      .idPA(request.getIdPA())
      .idBrokerPA(request.getIdBrokerPA())
      .idStation(request.getIdStation())
      .receiptBytes(receiptBytes)
      .receipt(mapperBuilder.build().convertValue(receipt, it.veneto.regione.schemas._2012.pagamenti.CtReceipt.class))
      .standin(BooleanUtils.toBoolean(request.isStandin()))
      .build();

    CtResponse ctResponseTo = pagamentiTelematiciCCPService.paSendRTHandler(paSendRTDto);
    PaSendRTV2Response response = new PaSendRTV2Response();
    response.setFault(ctResponseTo.getFault());
    response.setOutcome(ctResponseTo.getOutcome());
    return response;
  }
}
