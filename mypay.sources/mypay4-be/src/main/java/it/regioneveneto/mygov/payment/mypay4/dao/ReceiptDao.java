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
package it.regioneveneto.mygov.payment.mypay4.dao;

import it.gov.pagopa.pagopa_api.pa.pafornode.CtReceipt;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.Receipt;
import it.regioneveneto.mygov.payment.mypay4.model.ReceiptWithAdditionalInfo;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ReceiptDao extends BaseDao {

  @SqlQuery("select " + Receipt.ALIAS + ALL_FIELDS +
    "  from mygov_receipt " + Receipt.ALIAS +
    " where " + Receipt.ALIAS + ".fiscal_code = :fiscalCode ")
  @RegisterFieldMapper(Receipt.class)
  List<Receipt> findByFiscalCode(String fiscalCode);

  @SqlQuery("    select " + Receipt.ALIAS + ALL_FIELDS +
    "  from mygov_receipt " + Receipt.ALIAS +
    " where " + Receipt.ALIAS + ".mygov_receipt_id = :id ")
  @RegisterFieldMapper(Receipt.class)
  Optional<Receipt> getById(Long id);

  @SqlQuery("    select " + Receipt.ALIAS + ALL_FIELDS +
    "  from mygov_receipt " + Receipt.ALIAS +
    " where " + Receipt.ALIAS + ".mygov_dovuto_elaborato_id = :idDovutoElaborato ")
  @RegisterFieldMapper(Receipt.class)
  Optional<Receipt> getByDovutoElaboratoId(Long idDovutoElaborato);

  @SqlQuery("    select " + Receipt.ALIAS + ALL_FIELDS +
    "  from mygov_receipt " + Receipt.ALIAS +
    " where " + Receipt.ALIAS + ".receipt_id = :receiptId " +
    " limit 1")
  @RegisterFieldMapper(Receipt.class)
  Optional<Receipt> getByReceiptId(String receiptId);

  @SqlQuery("    select " + Receipt.ALIAS + ALL_FIELDS +
    "  from mygov_receipt " + Receipt.ALIAS +
    " where " + Receipt.ALIAS + ".fiscal_code = :fiscalCode " +
    " and " + Receipt.ALIAS + ".notice_number = :noticeNumber " +
    " and " + Receipt.ALIAS + ".receipt_id = :receiptId"
  )
  @RegisterFieldMapper(Receipt.class)
  Optional<Receipt> getByIdentifierSectionData(String receiptId, String noticeNumber, String fiscalCode);

  @SqlQuery("select " + Receipt.ALIAS + ".mygov_receipt_id" +
    ", "+Receipt.ALIAS + ".status_export" +
    ", "+Receipt.ALIAS + ".num_try_export" +
    ", "+Receipt.ALIAS + ".dt_last_export" +
    "  from mygov_receipt " + Receipt.ALIAS +
    " where " + Receipt.ALIAS + ".status_export in ('"+Receipt.STATUS_N+"', '"+Receipt.STATUS_S+"')" +
    "   and (" + Receipt.ALIAS + ".fiscal_code_pa_2 is not null " +
    "     or " + Receipt.ALIAS + ".fiscal_code_pa_1 != " + Receipt.ALIAS + ".fiscal_code )" +
    " order by mygov_receipt_id asc"
  )
  @RegisterFieldMapper(Receipt.class)
  List<Receipt> findByMultiBeneficiaryNotExported();

  @SqlQuery(
    "select " + Receipt.ALIAS + ALL_FIELDS +
      ", "+DovutoElaborato.ALIAS+".mygov_dovuto_elaborato_id as " + DovutoElaborato.ALIAS +"_mygovDovutoElaboratoId" +
      ", "+DovutoElaborato.ALIAS+".cod_tipo_dovuto as " + DovutoElaborato.ALIAS +"_codTipoDovuto" +
      "  from mygov_receipt " + Receipt.ALIAS +
      "  left join mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
      "    on " + DovutoElaborato.ALIAS + ".mygov_dovuto_elaborato_id = " + Receipt.ALIAS + ".mygov_dovuto_elaborato_id" +
      " where " + Receipt.ALIAS + ".mygov_receipt_id = :mygovReceiptId " +
      "   and " + Receipt.ALIAS + ".status_export in ('"+Receipt.STATUS_N+"', '"+Receipt.STATUS_S+"')" +
      " for update of " + Receipt.ALIAS + " skip locked"
  )
  @RegisterFieldMapper(Receipt.class)
  Optional<Receipt> getNotExportedByIdLockOrSkip(Long mygovReceiptId);

  @SqlUpdate("update mygov_receipt " +
    "   set dt_last_export = now()" +
    "      ,num_try_export = num_try_export + 1" +
    "      ,status_export = '"+Receipt.STATUS_S+"'" +
    " where mygov_receipt_id = :mygovReceiptId" +
    "   and status_export in ('"+Receipt.STATUS_N+"', '"+Receipt.STATUS_S+"')"
  )
  int setExportStatusSent(Long mygovReceiptId);

  @SqlUpdate("update mygov_receipt " +
    "   set dt_last_export = now()" +
    "      ,num_try_export = num_try_export + 1" +
    "      ,status_export = '"+Receipt.STATUS_R+"'" +
    " where mygov_receipt_id = :mygovReceiptId" +
    "   and status_export in ('"+Receipt.STATUS_N+"', '"+Receipt.STATUS_S+"')"
  )
  int setExportStatusSentAndReceived(Long mygovReceiptId);

  @SqlUpdate("update mygov_receipt " +
    "   set status_export = '"+Receipt.STATUS_R+"'" +
    " where mygov_receipt_id = :mygovReceiptId" +
    "   and status_export in ('"+Receipt.STATUS_S+"', '"+Receipt.STATUS_E+"')"
  )
  int setExportStatusReceived(Long mygovReceiptId);

  @SqlUpdate("update mygov_receipt " +
    "   set status_export = '"+Receipt.STATUS_E+"'" +
    " where mygov_receipt_id = :mygovReceiptId"
  )
  int setExportStatusError(Long mygovReceiptId);

  @SqlUpdate("update mygov_receipt " +
    "   set dt_last_export = null" +
    "      ,num_try_export = 0" +
    "      ,status_export = '"+Receipt.STATUS_N+"'" +
    " where mygov_receipt_id = :mygovReceiptId"
  )
  int resetExportStatusNew(Long mygovReceiptId);

  @SqlUpdate("INSERT INTO mygov_receipt(" +
    "  mygov_receipt_id " +
    ", dt_creazione " +
    ", mygov_dovuto_elaborato_id" +
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
    ", receipt_bytes " +
    ") values (" +
    "   nextval('mygov_receipt_mygov_receipt_id_seq')" +
    ", :r.dtCreazione" +
    ", :r.mygovDovutoElaboratoId?.mygovDovutoElaboratoId" +
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
    ", :r.receiptBytes)"
  )
  @GetGeneratedKeys("mygov_receipt_id")
  Long insertNew(@BindBean("r") Receipt Receipt);

  @SqlQuery(
    "select " + Receipt.ALIAS + ALL_FIELDS +
      "  from mygov_receipt " + Receipt.ALIAS +
      " where " + Receipt.ALIAS + ".mygov_receipt_id = :mygovReceiptId " +
      "   and " + Receipt.ALIAS + ".receipt_bytes is null "
  )
	Optional<CtReceipt> getByIdAndNullBytesAsCtReceipt(Long mygovReceiptId);

  @SqlQuery(
    "select " + Receipt.ALIAS + ".mygov_receipt_id" +
      "  from mygov_receipt " + Receipt.ALIAS +
      " where " + Receipt.ALIAS + ".receipt_bytes is null " +
      " order by 1"
  )
  List<Long> getWithoutReceiptBytes();

  @SqlUpdate("update mygov_receipt " +
    "   set receipt_bytes = :receiptBytes" +
    " where mygov_receipt_id = :mygovReceiptId"
  )
  int setReceiptBytes(Long mygovReceiptId, byte[] receiptBytes);

  @SqlQuery(
          "select " + Receipt.ALIAS + ALL_FIELDS + "," +
                  "COALESCE(" + Receipt.ALIAS + ".REMITTANCE_INFORMATION_2, " + Receipt.ALIAS + ".DESCRIPTION) AS deRpDatiVersDatiSingVersCausaleVersamento, " +
                  "CASE WHEN SUBSTRING(" + Receipt.ALIAS + ".TRANSFER_CATEGORY_2,1,2) = '9/' THEN " + Receipt.ALIAS + ".TRANSFER_CATEGORY_2 ELSE '9/' || "
                  + Receipt.ALIAS + ".TRANSFER_CATEGORY_2 || '/' END AS deEDatiPagDatiSingPagDatiSpecificiRiscossione, " +
                  Ente.FIELDS_WITHOUT_LOGO +
                  " FROM mygov_receipt " + Receipt.ALIAS +
                  ",mygov_ente " + Ente.ALIAS +
                  " WHERE " + Receipt.ALIAS + ".FISCAL_CODE_PA_2 = " + Ente.ALIAS + ".CODICE_FISCALE_ENTE " +
                  "and " + Ente.ALIAS + ".mygov_ente_id = :idEnte " +
                  "and case " +
                  "when :flgIncrementale = true then " + Receipt.ALIAS + ".dt_creazione >= :dataOraInizioEstrazione " +
                  "and  " + Receipt.ALIAS + ".dt_creazione < :dataOraFineEstrazione " +
                  "when :flgIncrementale = false then " + Receipt.ALIAS + ".payment_date_time >= :dataOraInizioEstrazione " +
                  "and " + Receipt.ALIAS + ".payment_date_time <= :dataOraFineEstrazione end " +
                  "and coalesce (:codTipoDovuto,'') ='EXPORT_ENTE_SECONDARIO'"
  )
  @RegisterFieldMapper(ReceiptWithAdditionalInfo.class)
  List<ReceiptWithAdditionalInfo> getRowForExportDovutoFromReceipt(Long idEnte, boolean flgIncrementale, String codTipoDovuto, Date dataOraInizioEstrazione, Date dataOraFineEstrazione);

  @SqlQuery(
          "select " + Receipt.ALIAS + ALL_FIELDS +
                  ", "+DovutoElaborato.ALIAS+".mygov_dovuto_elaborato_id as " + DovutoElaborato.ALIAS +"_mygovDovutoElaboratoId" +
                  "  from mygov_receipt " + Receipt.ALIAS +
                  "  left join mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
                  "    on " + DovutoElaborato.ALIAS + ".mygov_dovuto_elaborato_id = " + Receipt.ALIAS + ".mygov_dovuto_elaborato_id" +
                  " where " + Receipt.ALIAS + ".payment_date_time >= :dataInizioRecuperoConservazione "
  )
  @RegisterFieldMapper(Receipt.class)
  List<Receipt> getByDataInizio(LocalDate dataInizioRecuperoConservazione);

}
