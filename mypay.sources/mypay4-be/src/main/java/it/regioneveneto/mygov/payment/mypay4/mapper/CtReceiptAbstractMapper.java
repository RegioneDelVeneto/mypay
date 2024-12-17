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
package it.regioneveneto.mygov.payment.mypay4.mapper;

import it.veneto.regione.schemas._2012.pagamenti.*;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
public abstract class CtReceiptAbstractMapper {

  @SneakyThrows
  protected <T extends CtReceipt> T baseCtReceiptMap(T receipt, ResultSet rs, StatementContext ctx) {

    receipt.setReceiptId(rs.getString("receipt_id"));
    receipt.setNoticeNumber(rs.getString("notice_number"));
    receipt.setFiscalCode(rs.getString("fiscal_code"));
    receipt.setOutcome(StOutcome.valueOf(rs.getString("outcome")));
    receipt.setCreditorReferenceId(rs.getString("creditor_reference_id"));
    receipt.setPaymentAmount(rs.getBigDecimal("payment_amount"));
    receipt.setDescription(rs.getString("description"));
    receipt.setCompanyName(rs.getString("company_name"));
    receipt.setOfficeName(rs.getString("office_name"));
    receipt.setIdPSP(rs.getString("id_psp"));
    Optional.ofNullable(rs.getString("psp_fiscal_code")).ifPresent(receipt::setPspFiscalCode);
    Optional.ofNullable(rs.getString("psp_partita_iva")).ifPresent(receipt::setPspPartitaIVA);
    receipt.setPSPCompanyName(rs.getString("psp_company_name"));
    receipt.setIdChannel(rs.getString("id_channel"));
    receipt.setChannelDescription(rs.getString("channel_description"));
    Optional.ofNullable(rs.getString("payment_method")).ifPresent(receipt::setPaymentMethod);
    Optional.ofNullable(rs.getBigDecimal("fee")).ifPresent(receipt::setFee);

    Optional.ofNullable(rs.getTimestamp("payment_date_time")).map(Utilities::toXMLGregorianCalendar).ifPresent(receipt::setPaymentDateTime);
    Optional.ofNullable(rs.getTimestamp("application_date")).map(Utilities::toXMLGregorianCalendar).ifPresent(receipt::setApplicationDate);
    Optional.ofNullable(rs.getTimestamp("transfer_date")).map(Utilities::toXMLGregorianCalendar).ifPresent(receipt::setTransferDate);

    CtSubject debtor = new CtSubject();
    CtEntityUniqueIdentifier uniqueIdentifierDebtor = new CtEntityUniqueIdentifier();
    var identifierTypeDebtor = rs.getString("unique_identifier_type_debtor");
    var identifierValueDebtor = rs.getString("unique_identifier_value_debtor");
    uniqueIdentifierDebtor.setEntityUniqueIdentifierType(StEntityUniqueIdentifierType.valueOf(identifierTypeDebtor));
    uniqueIdentifierDebtor.setEntityUniqueIdentifierValue(identifierValueDebtor);
    debtor.setUniqueIdentifier(uniqueIdentifierDebtor);
    Optional.ofNullable(rs.getString("full_name_debtor")).ifPresent(debtor::setFullName);
    Optional.ofNullable(rs.getString("street_name_debtor")).ifPresent(debtor::setStreetName);
    Optional.ofNullable(rs.getString("civic_number_debtor")).ifPresent(debtor::setCivicNumber);
    Optional.ofNullable(rs.getString("postal_code_debtor")).ifPresent(debtor::setPostalCode);
    Optional.ofNullable(rs.getString("city_debtor")).ifPresent(debtor::setCity);
    Optional.ofNullable(rs.getString("state_province_region_debtor")).ifPresent(debtor::setStateProvinceRegion);
    Optional.ofNullable(rs.getString("country_debtor")).ifPresent(debtor::setCountry);
    Optional.ofNullable(rs.getString("email_debtor")).ifPresent(debtor::setEMail);
    receipt.setDebtor(debtor);

    if(StringUtils.isNotBlank(rs.getString("full_name_payer")) || StringUtils.isNotBlank(rs.getString("unique_identifier_value_payer"))) {
      CtSubject payer = new CtSubject();
      CtEntityUniqueIdentifier uniqueIdentifierPayer = new CtEntityUniqueIdentifier();
      Optional.ofNullable(rs.getString("unique_identifier_type_payer"))
        .map(StEntityUniqueIdentifierType::valueOf).ifPresent(uniqueIdentifierPayer::setEntityUniqueIdentifierType);
      Optional.ofNullable(rs.getString("unique_identifier_value_payer")).ifPresent(uniqueIdentifierPayer::setEntityUniqueIdentifierValue);
      payer.setUniqueIdentifier(uniqueIdentifierPayer);
      Optional.ofNullable(rs.getString("full_name_payer")).ifPresent(payer::setFullName);
      Optional.ofNullable(rs.getString("street_name_payer")).ifPresent(payer::setStreetName);
      Optional.ofNullable(rs.getString("civic_number_payer")).ifPresent(payer::setCivicNumber);
      Optional.ofNullable(rs.getString("postal_code_payer")).ifPresent(payer::setPostalCode);
      Optional.ofNullable(rs.getString("city_payer")).ifPresent(payer::setCity);
      Optional.ofNullable(rs.getString("state_province_region_payer")).ifPresent(payer::setStateProvinceRegion);
      Optional.ofNullable(rs.getString("country_payer")).ifPresent(payer::setCountry);
      Optional.ofNullable(rs.getString("email_payer")).ifPresent(payer::setEMail);
      receipt.setPayer(payer);
    }

    var first = new CtTransferPAReceipt();
    first.setIdTransfer(1);
    first.setTransferAmount(rs.getBigDecimal("transfer_amount_1"));
    first.setFiscalCodePA(rs.getString("fiscal_code_pa_1"));
    first.setIBAN(rs.getString("iban_1"));
    first.setRemittanceInformation(rs.getString("remittance_information_1"));
    first.setTransferCategory(rs.getString("transfer_category_1"));

    var second = new CtTransferPAReceipt();
    Optional.ofNullable(rs.getBigDecimal("transfer_amount_2")).ifPresent(second::setTransferAmount);
    Optional.ofNullable(rs.getString("fiscal_code_pa_2")).ifPresent(second::setFiscalCodePA);
    Optional.ofNullable(rs.getString("iban_2")).ifPresent(second::setIBAN);
    Optional.ofNullable(rs.getString("remittance_information_2")).ifPresent(second::setRemittanceInformation);
    Optional.ofNullable(rs.getString("transfer_category_2")).ifPresent(second::setTransferCategory);

    var transferListPA = new CtTransferListPAReceipt();
    var list = new ArrayList<>(List.of(first));
    if (BeanUtils.describe(second).values().stream().allMatch(Objects::nonNull)) {
      second.setIdTransfer(2);
      list.add(second);
    }
    transferListPA.getTransfers().addAll(list);
    receipt.setTransferList(transferListPA);

    return receipt;
  }
}
