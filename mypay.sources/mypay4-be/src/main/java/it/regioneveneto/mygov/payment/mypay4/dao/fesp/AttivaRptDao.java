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
package it.regioneveneto.mygov.payment.mypay4.dao.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.BaseDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.AttivaRptE;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface AttivaRptDao extends BaseDao {

  @SqlQuery(
      "    select " + AttivaRptE.ALIAS + ALL_FIELDS +
          "  from mygov_attiva_rpt_e " + AttivaRptE.ALIAS +
          " where "+ AttivaRptE.ALIAS+".mygov_attiva_rpt_e_id = :id " )
  @RegisterFieldMapper(AttivaRptE.class)
  Optional<AttivaRptE> getById(Long id);

  @SqlQuery(
      "    select " + AttivaRptE.ALIAS + ALL_FIELDS +
          "  from mygov_attiva_rpt_e " + AttivaRptE.ALIAS +
          " where "+ AttivaRptE.ALIAS+".cod_attivarpt_codice_contesto_pagamento = :codAttivarptCodiceContestoPagamento " +
          "   and "+ AttivaRptE.ALIAS+".cod_attivarpt_identificativo_dominio = :codAttivarptIdentificativoDominio " +
          "   and "+ AttivaRptE.ALIAS+".cod_attivarpt_identificativo_univoco_versamento = :codAttivarptIdentificativoUnivocoVersamento " +
          "   and "+ AttivaRptE.ALIAS+".de_attivarpt_esito = 'OK'")
  @RegisterFieldMapper(AttivaRptE.class)
  Optional<AttivaRptE> getByKey(String codAttivarptIdentificativoDominio,
                                String codAttivarptIdentificativoUnivocoVersamento,
                                String codAttivarptCodiceContestoPagamento);

  @SqlUpdate(
      "insert into mygov_attiva_rpt_e ( " +
          "mygov_attiva_rpt_e_id, "+
          "version, " +
          "cod_attivarpt_id_psp, "+
          "cod_attivarpt_id_intermediario_psp, "+
          "cod_attivarpt_id_canale_psp, "+
          "cod_attivarpt_identificativo_intermediario_pa, "+
          "cod_attivarpt_identificativo_stazione_intermediario_pa, "+
          "cod_attivarpt_identificativo_dominio, "+
          "cod_attivarpt_identificativo_univoco_versamento, "+
          "cod_attivarpt_codice_contesto_pagamento, "+
          "dt_attivarpt, "+
          "num_attivarpt_importo_singolo_versamento, "+
          "de_attivarpt_iban_appoggio, "+
          "de_attivarpt_bic_appoggio, "+
          "cod_attivarpt_sogg_vers_id_univ_vers_tipo_id_univoco, "+
          "cod_attivarpt_sogg_vers_id_univ_vers_codice_id_univoco, "+
          "de_attivarpt_sogg_vers_anagrafica_versante, "+
          "de_attivarpt_sogg_vers_indirizzo_versante, "+
          "de_attivarpt_sogg_vers_civico_versante, "+
          "cod_attivarpt_sogg_vers_cap_versante, "+
          "de_attivarpt_sogg_vers_localita_versante, "+
          "de_attivarpt_sogg_vers_provincia_versante, "+
          "cod_attivarpt_sogg_vers_nazione_versante, "+
          "de_attivarpt_sogg_vers_email_versante, "+
          "de_attivarpt_iban_addebito, "+
          "de_attivarpt_bic_addebito, "+
          "cod_attivarpt_sogg_pag_id_univ_pag_tipo_id_univoco, "+
          "cod_attivarpt_sogg_pag_id_univ_pag_codice_id_univoco, "+
          "de_attivarpt_sogg_pag_anagrafica_pagatore, "+
          "de_attivarpt_sogg_pag_indirizzo_pagatore, "+
          "de_attivarpt_sogg_pag_civico_pagatore, "+
          "cod_attivarpt_sogg_pag_cap_pagatore, "+
          "de_attivarpt_sogg_pag_localita_pagatore, "+
          "de_attivarpt_sogg_pag_provincia_pagatore, "+
          "cod_attivarpt_sogg_pag_nazione_pagatore, "+
          "de_attivarpt_sogg_pag_email_pagatore ) values ( "+
          "nextval('mygov_attiva_rpt_e_id_seq'), "+
          ":r.version, "+
          ":r.codAttivarptIdPsp, "+
          ":r.codAttivarptIdIntermediarioPsp, "+
          ":r.codAttivarptIdCanalePsp, "+
          ":r.codAttivarptIdentificativoIntermediarioPa, "+
          ":r.codAttivarptIdentificativoStazioneIntermediarioPa, "+
          ":r.codAttivarptIdentificativoDominio, "+
          ":r.codAttivarptIdentificativoUnivocoVersamento, "+
          ":r.codAttivarptCodiceContestoPagamento, "+
          ":r.dtAttivarpt, "+
          ":r.numAttivarptImportoSingoloVersamento, "+
          ":r.deAttivarptIbanAppoggio, "+
          ":r.deAttivarptBicAppoggio, "+
          ":r.codAttivarptSoggVersIdUnivVersTipoIdUnivoco, "+
          ":r.codAttivarptSoggVersIdUnivVersCodiceIdUnivoco, "+
          ":r.deAttivarptSoggVersAnagraficaVersante, "+
          ":r.deAttivarptSoggVersIndirizzoVersante, "+
          ":r.deAttivarptSoggVersCivicoVersante, "+
          ":r.codAttivarptSoggVersCapVersante, "+
          "left(:r.deAttivarptSoggVersLocalitaVersante,"+RptRt.MAX_LENGTH_LOCALITA+"), "+
          ":r.deAttivarptSoggVersProvinciaVersante, "+
          ":r.codAttivarptSoggVersNazioneVersante, "+
          ":r.deAttivarptSoggVersEmailVersante, "+
          ":r.deAttivarptIbanAddebito, "+
          ":r.deAttivarptBicAddebito, "+
          ":r.codAttivarptSoggPagIdUnivPagTipoIdUnivoco, "+
          ":r.codAttivarptSoggPagIdUnivPagCodiceIdUnivoco, "+
          ":r.deAttivarptSoggPagAnagraficaPagatore, "+
          ":r.deAttivarptSoggPagIndirizzoPagatore, "+
          ":r.deAttivarptSoggPagCivicoPagatore, "+
          ":r.codAttivarptSoggPagCapPagatore, "+
          "left(:r.deAttivarptSoggPagLocalitaPagatore,"+RptRt.MAX_LENGTH_LOCALITA+"), "+
          ":r.deAttivarptSoggPagProvinciaPagatore, "+
          ":r.codAttivarptSoggPagNazionePagatore, "+
          ":r.deAttivarptSoggPagEmailPagatore )")
  @GetGeneratedKeys
  AttivaRptE insertForAttivaRPT(@BindBean("r") AttivaRptE attivaRptE);

  @SqlUpdate(
      "update mygov_attiva_rpt_e set "+
        " dt_e_attivarpt = :r.dtEAttivarpt, "+
        " num_e_attivarpt_importo_singolo_versamento = :r.numEAttivarptImportoSingoloVersamento, "+
        " de_e_attivarpt_iban_accredito = :r.deEAttivarptIbanAccredito, "+
        " de_e_attivarpt_bic_accredito = :r.deEAttivarptBicAccredito, "+
        " cod_e_attivarpt_ente_benef_id_univ_benef_tipo_id_univoco = :r.codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco, "+
        " cod_e_attivarpt_ente_benef_id_univ_benef_codice_id_univoco = :r.codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco, "+
        " de_e_attivarpt_ente_benef_denominazione_beneficiario = :r.deEAttivarptEnteBenefDenominazioneBeneficiario, "+
        " cod_e_attivarpt_ente_benef_codice_unit_oper_beneficiario = :r.codEAttivarptEnteBenefCodiceUnitOperBeneficiario, "+
        " de_e_attivarpt_ente_benef_denom_unit_oper_beneficiario = :r.deEAttivarptEnteBenefDenomUnitOperBeneficiario, "+
        " de_e_attivarpt_ente_benef_indirizzo_beneficiario = :r.deEAttivarptEnteBenefIndirizzoBeneficiario, "+
        " de_e_attivarpt_ente_benef_civico_beneficiario = :r.deEAttivarptEnteBenefCivicoBeneficiario, "+
        " cod_e_attivarpt_ente_benef_cap_beneficiario = :r.codEAttivarptEnteBenefCapBeneficiario, "+
        " de_e_attivarpt_ente_benef_localita_beneficiario = :r.deEAttivarptEnteBenefLocalitaBeneficiario, "+
        " de_e_attivarpt_ente_benef_provincia_beneficiario = :r.deEAttivarptEnteBenefProvinciaBeneficiario, "+
        " cod_e_attivarpt_ente_benef_nazione_beneficiario = :r.codEAttivarptEnteBenefNazioneBeneficiario, "+
        " de_e_attivarpt_credenziali_pagatore = :r.deEAttivarptCredenzialiPagatore, "+
        " de_e_attivarpt_causale_versamento = :r.deEAttivarptCausaleVersamento, "+
        " de_attivarpt_esito = :r.deAttivarptEsito, "+
        " cod_attivarpt_fault_code = :r.codAttivarptFaultCode, "+
        " de_attivarpt_fault_string = :r.deAttivarptFaultString, "+
        " cod_attivarpt_id = :r.codAttivarptId, "+
        " de_attivarpt_description = :r.deAttivarptDescription, "+
        " cod_attivarpt_original_fault_code = :r.codAttivarptOriginalFaultCode, "+
        " de_attivarpt_original_fault_string = :r.deAttivarptOriginalFaultString, "+
        " de_attivarpt_original_fault_description = :r.deAttivarptOriginalFaultDescription, "+
        " cod_attivarpt_serial = :r.codAttivarptSerial "+
        " where mygov_attiva_rpt_e_id = :mygovAttivaRptEId ")
  int updateForAttivaRPT(Long mygovAttivaRptEId, @BindBean("r") AttivaRptE attivaRptE);
}
