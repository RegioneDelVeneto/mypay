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
package it.regioneveneto.mygov.payment.mypay4.service.extra;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaSendRTReq;
import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.dao.extra.BonificaPaSendRtFespDao;
import it.regioneveneto.mygov.payment.mypay4.extra.bonificapasendrt.BonificaPaSendRtStandaloneApplication;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Giornale;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue= BonificaPaSendRtStandaloneApplication.NAME)
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class BonificaPaSendRtFespService {

  @Autowired
  private BonificaPaSendRtFespDao bonificaPaSendRtFespDao;

  @Autowired
  private JAXBTransformService jaxbTransformService;

  public void bonificaPaSendRtNotProcessed(){

    String SQL = "INSERT INTO mygov_receipt(" +
      "  mygov_receipt_id " +
      ", dt_creazione " +
      ", receipt_id " +
      ", notice_number " +
      ", fiscal_code " +
      ", outcome " +
      ", creditor_reference_id " +
      ", payment_amount " +
      ", description " +
      ", company_name " +
      ", office_name " +
      ", unique_identifier_type_debtor " +
      ", unique_identifier_value_debtor " +
      ", full_name_debtor " +
      ", street_name_debtor " +
      ", civic_number_debtor " +
      ", postal_code_debtor " +
      ", city_debtor " +
      ", state_province_region_debtor " +
      ", country_debtor " +
      ", email_debtor " +
      ", id_psp " +
      ", psp_fiscal_code " +
      ", psp_partita_iva " +
      ", psp_company_name " +
      ", id_channel " +
      ", channel_description " +
      ", unique_identifier_type_payer " +
      ", unique_identifier_value_payer " +
      ", full_name_payer " +
      ", street_name_payer " +
      ", civic_number_payer " +
      ", postal_code_payer " +
      ", city_payer " +
      ", state_province_region_payer " +
      ", country_payer " +
      ", email_payer " +
      ", payment_method " +
      ", fee " +
      ", payment_date_time " +
      ", application_date " +
      ", transfer_date " +
      ", transfer_amount_1 " +
      ", fiscal_code_pa_1 " +
      ", iban_1 " +
      ", remittance_information_1 " +
      ", transfer_category_1 " +
      ", transfer_amount_2 " +
      ", fiscal_code_pa_2 " +
      ", iban_2 " +
      ", remittance_information_2 " +
      ", transfer_category_2 " +
      ", status " +
      ") values (" +
      "   nextval('mygov_receipt_mygov_receipt_id_seq')" +
      ", :r.dtCreazione" +
      ", :r.receiptId" +
      ", :r.noticeNumber" +
      ", :r.fiscalCode" +
      ", :r.outcome" +
      ", :r.creditorReferenceId" +
      ", :r.paymentAmount" +
      ", :r.description" +
      ", :r.companyName" +
      ", :r.officeName" +
      ", :r.uniqueIdentifierTypeDebtor" +
      ", :r.uniqueIdentifierValueDebtor" +
      ", :r.fullNameDebtor" +
      ", :r.streetNameDebtor" +
      ", :r.civicNumberDebtor" +
      ", :r.postalCodeDebtor" +
      ", :r.cityDebtor" +
      ", :r.stateProvinceRegionDebtor" +
      ", :r.countryDebtor" +
      ", :r.emailDebtor" +
      ", :r.idPsp" +
      ", :r.pspFiscalCode" +
      ", :r.pspPartitaIva" +
      ", :r.pspCompanyName" +
      ", :r.idChannel" +
      ", :r.channelDescription" +
      ", :r.uniqueIdentifierTypePayer" +
      ", :r.uniqueIdentifierValuePayer" +
      ", :r.fullNamePayer" +
      ", :r.streetNamePayer" +
      ", :r.civicNumberPayer" +
      ", :r.postalCodePayer" +
      ", :r.cityPayer" +
      ", :r.stateProvinceRegionPayer" +
      ", :r.countryPayer" +
      ", :r.emailPayer" +
      ", :r.paymentMethod" +
      ", :r.fee" +
      ", :r.paymentDateTime" +
      ", :r.applicationDate" +
      ", :r.transferDate" +
      ", :r.transferAmount1" +
      ", :r.fiscalCodePa1" +
      ", :r.iban1" +
      ", :r.remittanceInformation1" +
      ", :r.transferCategory1" +
      ", :r.transferAmount2" +
      ", :r.fiscalCodePa2" +
      ", :r.iban2" +
      ", :r.remittanceInformation2" +
      ", :r.transferCategory2" +
      ", :r.status )" +
      " ON CONFLICT DO NOTHING;";

    try{
      AtomicInteger counter = new AtomicInteger(0);
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
      List<Giornale> idDaBonificare = bonificaPaSendRtFespDao.getGiornaleDaBonificare();
      log.info("id da bonificare: {}", idDaBonificare.size());
      List<String> daBonificare = idDaBonificare.stream().map(giornale -> {
        int processed = counter.getAndIncrement();
        if(processed % 100 == 0)
          log.debug("processed: {}", processed);
        return bonificaPaSendRtFespDao.getReceiptFromGiornale(giornale.getIdentificativoDominio(), giornale.getCodiceContestoPagamento());
      }).collect(Collectors.toList());
      //List<String> daBonificare = receiptDao.getDaBonificare("ALL");
      log.info("da bonificare: {}", daBonificare.size());
      List<String> statement = daBonificare.stream()
        //.peek(xml -> log.info("XML [{}]", xml))
        .map(String::getBytes)
        .map(ByteArrayInputStream::new)
        .map(xmlStream -> {
          try{
            SOAPMessage message = MessageFactory.newInstance().createMessage(null, xmlStream);
            return message.getSOAPBody().extractContentAsDocument();
          }catch(Exception e){
            log.error("error unmarshalling", e);
            throw new RuntimeException(e);
          }
        })
        .map(body -> jaxbTransformService.unmarshalling(body, PaSendRTReq.class))
        //.peek(r->log.info("PaSendRTReq: {}", ReflectionToStringBuilder.toString(r)))
        .map(PaSendRTReq::getReceipt)
        //.peek(r->log.info("Receipt: {}", ReflectionToStringBuilder.toString(r)))
        .map(r -> {
          try {
            return BeanUtils.describe(r).keySet().stream().reduce(SQL, (s, k) -> {
              String valueString;
              try {
                Object value = PropertyUtils.getProperty(r, k);
                if(value==null)
                  valueString = null;
                else if(value instanceof Date)
                  valueString = StringUtils.wrap(sdf.format(value),"'");
                else if(value instanceof String)
                  valueString = StringUtils.wrap(StringUtils.replace(StringUtils.stripToNull((String)value),"'","''"),"'");
                else
                  valueString = value.toString();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              return RegExUtils.replaceAll(s, Pattern.compile("(\\:r\\."+k+")([^a-zA-Z0-9])"),valueString+"$2");
            });
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        //.peek(s -> log.info("sql: {}", s))
        .distinct()
        .collect(Collectors.toList());
      log.info("processed: {}, bonificati: {}", counter.get(), statement.size());
      try(FileWriter writer = new FileWriter("/tmp/bonfica_receipt.sql", false)){
        for(String str: statement)
          writer.write(str + System.lineSeparator());
      }


      log.info("stat:\n{}",daBonificare.stream()
        //.peek(xml -> log.info("XML [{}]", xml))
        .map(String::getBytes)
        .map(ByteArrayInputStream::new)
        .map(xmlStream -> {
          try{
            SOAPMessage message = MessageFactory.newInstance().createMessage(null, xmlStream);
            return message.getSOAPBody().extractContentAsDocument();
          }catch(Exception e){
            log.error("error unmarshalling", e);
            throw new RuntimeException(e);
          }
        })
        .map(body -> jaxbTransformService.unmarshalling(body, PaSendRTReq.class))
        //.peek(r->log.info("PaSendRTReq: {}", ReflectionToStringBuilder.toString(r)))
        .map(PaSendRTReq::getReceipt)
        .map(r -> r.getReceiptId()+"|"+r.getCompanyName()+"|"+r.getNoticeNumber())
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));

    }catch(Exception e){
      log.error("errore bonifica", e);
    }
  }
}
