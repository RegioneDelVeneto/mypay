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

import it.regioneveneto.mygov.payment.mypay4.dto.DovutoMultibeneficiarioElaboratoTo;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoMultibeneficiarioElaborato;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DovutoMultibeneficiarioElaboratoDao extends BaseDao {

  @SqlUpdate(" insert into mygov_dovuto_multibeneficiario_elaborato (" +
    "   mygov_dovuto_multibeneficiario_elaborato_id" +
    "	, cod_iud" +
    " , cod_iuv" +
    " , codice_fiscale_ente" +
    " , de_rp_ente_benef_denominazione_beneficiario" +
    " , cod_rp_dati_vers_dati_sing_vers_iban_accredito" +
    " , de_rp_ente_benef_indirizzo_beneficiario" +
    " , de_rp_ente_benef_civico_beneficiario" +
    " , cod_rp_ente_benef_cap_beneficiario" +
    " , de_rp_ente_benef_localita_beneficiario" +
    " , de_rp_ente_benef_provincia_beneficiario" +
    " , cod_rp_ente_benef_nazione_beneficiario" +
    " , num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento" +
    " , mygov_dovuto_elaborato_id" +
    " , de_rp_dati_vers_dati_sing_vers_causale_versamento" +
    " , de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione" +
    " , dt_creazione" +
    " , dt_ultima_modifica" +
    ") values (" +
    "   nextval('mygov_dovuto_multibenef_mygov_dovuto_multibenef_id_seq')" +
    " , :dME.codIud" +
    " , :dME.codIuv" +
    " , :dME.codiceFiscaleEnte" +
    " , :dME.deRpEnteBenefDenominazioneBeneficiario" +
    " , :dME.codRpDatiVersDatiSingVersIbanAccredito" +
    " , :dME.deRpEnteBenefIndirizzoBeneficiario" +
    " , :dME.deRpEnteBenefCivicoBeneficiario" +
    " , :dME.codRpEnteBenefCapBeneficiario" +
    " , :dME.deRpEnteBenefLocalitaBeneficiario" +
    " , :dME.deRpEnteBenefProvinciaBeneficiario" +
    " , :dME.codRpEnteBenefNazioneBeneficiario" +
    " , :dME.numRpDatiVersDatiSingVersImportoSingoloVersamento" +
    " , :dME.mygovDovutoElaboratoId?.mygovDovutoElaboratoId" +
    " , :dME.deRpDatiVersDatiSingVersCausaleVersamento" +
    " , :dME.deRpDatiVersDatiSingVersDatiSpecificiRiscossione" +
    " , :dME.dtCreazione" +
    " , :dME.dtUltimaModifica)")
  @GetGeneratedKeys("mygov_dovuto_multibeneficiario_elaborato_id")
  long insert(@BindBean("dME") DovutoMultibeneficiarioElaborato dME);

  @SqlQuery("SELECT " +
    DovutoMultibeneficiarioElaborato.ALIAS + ALL_FIELDS +
    "FROM mygov_dovuto_multibeneficiario_elaborato " + DovutoMultibeneficiarioElaborato.ALIAS +
    " WHERE " + DovutoMultibeneficiarioElaborato.ALIAS + ".mygov_dovuto_elaborato_id = :id"
  )
  @RegisterFieldMapper(DovutoMultibeneficiarioElaborato.class)
  Optional<DovutoMultibeneficiarioElaborato> getDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(Long id);

  @SqlQuery("SELECT " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".mygov_dovuto_multibeneficiario_elaborato_id, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_denominazione_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".codice_fiscale_ente, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".cod_rp_dati_vers_dati_sing_vers_iban_accredito, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_indirizzo_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_civico_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".cod_rp_ente_benef_cap_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".cod_rp_ente_benef_nazione_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_provincia_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_localita_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
    "FROM mygov_dovuto_multibeneficiario_elaborato " + DovutoMultibeneficiarioElaborato.ALIAS +
    " WHERE " + DovutoMultibeneficiarioElaborato.ALIAS + ".cod_e_silinviaesito_codice_contesto_pagamento = :CCP"
  )
  @RegisterFieldMapper(DovutoMultibeneficiarioElaborato.class)
  DovutoMultibeneficiarioElaborato getDovutoMultibeneficiarioElaboratoByCCP(String CCP);

  @SqlQuery("SELECT " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".mygov_dovuto_elaborato_id, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".mygov_dovuto_multibeneficiario_elaborato_id, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_denominazione_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".codice_fiscale_ente, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".cod_rp_dati_vers_dati_sing_vers_iban_accredito, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_indirizzo_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_civico_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".cod_rp_ente_benef_cap_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".cod_rp_ente_benef_nazione_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_provincia_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".de_rp_ente_benef_localita_beneficiario, " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
    "FROM mygov_dovuto_multibeneficiario_elaborato " + DovutoMultibeneficiarioElaborato.ALIAS +
    " WHERE " + DovutoMultibeneficiarioElaborato.ALIAS + ".mygov_dovuto_elaborato_id IN (<ids>)"
  )
  List<DovutoMultibeneficiarioElaboratoTo> getListDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(@BindList("ids") List<Long> ids);

  @SqlQuery("SELECT " +
    DovutoMultibeneficiarioElaborato.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
    "FROM mygov_dovuto_multibeneficiario_elaborato " + DovutoMultibeneficiarioElaborato.ALIAS +
    " WHERE " + DovutoMultibeneficiarioElaborato.ALIAS + ".mygov_dovuto_elaborato_id = :idDovutoElaborato"
  )
  BigDecimal getImportoDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(Long idDovutoElaborato);

  @SqlUpdate(" update mygov_dovuto_multibeneficiario_elaborato " +
    " set de_rp_dati_vers_dati_sing_vers_causale_versamento = :deRpDatiVersDatiSingVersCausaleVersamento " +
    "    ,de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione = :deRpDatiVersDatiSingVersDatiSpecificiRiscossione" +
    " where mygov_dovuto_multibeneficiario_elaborato_id = :mygovDovutoMultibeneficiarioElaboratoId")
  int updateFromReceipt(Long mygovDovutoMultibeneficiarioElaboratoId,
                        String deRpDatiVersDatiSingVersCausaleVersamento, String deRpDatiVersDatiSingVersDatiSpecificiRiscossione);

  @SqlUpdate(" update mygov_dovuto_multibeneficiario_elaborato " +
    " set mygov_dovuto_elaborato_id = :dme.mygovDovutoElaboratoId " +
    " where mygov_dovuto_multibeneficiario_elaborato_id = :dme.mygovDovutoMultibeneficiarioElaboratoId")
  int updateDovElaboratoId(@BindBean("dme") DovutoMultibeneficiarioElaborato dme);
}
