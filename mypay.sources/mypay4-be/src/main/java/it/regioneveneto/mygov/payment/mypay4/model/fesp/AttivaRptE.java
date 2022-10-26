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
package it.regioneveneto.mygov.payment.mypay4.model.fesp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import it.regioneveneto.mygov.payment.mypay4.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovAttivaRptEId")
public class AttivaRptE extends BaseEntity {

  public final static String ALIAS = "FESP_AttivaRptE";
  public final static String FIELDS = "" + ALIAS + ".mygov_attiva_rpt_e_id as FESP_AttivaRptE_mygovAttivaRptEId," + ALIAS + ".version as FESP_AttivaRptE_version" +
      "," + ALIAS + ".cod_attivarpt_id_psp as FESP_AttivaRptE_codAttivarptIdPsp" +
      "," + ALIAS + ".cod_attivarpt_identificativo_intermediario_pa as FESP_AttivaRptE_codAttivarptIdentificativoIntermediarioPa" +
      "," + ALIAS + ".cod_attivarpt_identificativo_stazione_intermediario_pa as FESP_AttivaRptE_codAttivarptIdentificativoStazioneIntermediarioPa" +
      "," + ALIAS + ".cod_attivarpt_identificativo_dominio as FESP_AttivaRptE_codAttivarptIdentificativoDominio" +
      "," + ALIAS + ".cod_attivarpt_identificativo_univoco_versamento as FESP_AttivaRptE_codAttivarptIdentificativoUnivocoVersamento" +
      "," + ALIAS + ".cod_attivarpt_codice_contesto_pagamento as FESP_AttivaRptE_codAttivarptCodiceContestoPagamento" +
      "," + ALIAS + ".dt_attivarpt as FESP_AttivaRptE_dtAttivarpt" +
      "," + ALIAS + ".num_attivarpt_importo_singolo_versamento as FESP_AttivaRptE_numAttivarptImportoSingoloVersamento" +
      "," + ALIAS + ".de_attivarpt_iban_appoggio as FESP_AttivaRptE_deAttivarptIbanAppoggio" +
      "," + ALIAS + ".de_attivarpt_bic_appoggio as FESP_AttivaRptE_deAttivarptBicAppoggio" +
      "," + ALIAS + ".cod_attivarpt_sogg_vers_id_univ_vers_tipo_id_univoco as FESP_AttivaRptE_codAttivarptSoggVersIdUnivVersTipoIdUnivoco" +
      "," + ALIAS + ".cod_attivarpt_sogg_vers_id_univ_vers_codice_id_univoco as FESP_AttivaRptE_codAttivarptSoggVersIdUnivVersCodiceIdUnivoco" +
      "," + ALIAS + ".de_attivarpt_sogg_vers_anagrafica_versante as FESP_AttivaRptE_deAttivarptSoggVersAnagraficaVersante" +
      "," + ALIAS + ".de_attivarpt_sogg_vers_indirizzo_versante as FESP_AttivaRptE_deAttivarptSoggVersIndirizzoVersante" +
      "," + ALIAS + ".de_attivarpt_sogg_vers_civico_versante as FESP_AttivaRptE_deAttivarptSoggVersCivicoVersante" +
      "," + ALIAS + ".cod_attivarpt_sogg_vers_cap_versante as FESP_AttivaRptE_codAttivarptSoggVersCapVersante" +
      "," + ALIAS + ".de_attivarpt_sogg_vers_localita_versante as FESP_AttivaRptE_deAttivarptSoggVersLocalitaVersante" +
      "," + ALIAS + ".de_attivarpt_sogg_vers_provincia_versante as FESP_AttivaRptE_deAttivarptSoggVersProvinciaVersante" +
      "," + ALIAS + ".cod_attivarpt_sogg_vers_nazione_versante as FESP_AttivaRptE_codAttivarptSoggVersNazioneVersante" +
      "," + ALIAS + ".de_attivarpt_sogg_vers_email_versante as FESP_AttivaRptE_deAttivarptSoggVersEmailVersante" +
      "," + ALIAS + ".de_attivarpt_iban_addebito as FESP_AttivaRptE_deAttivarptIbanAddebito" +
      "," + ALIAS + ".de_attivarpt_bic_addebito as FESP_AttivaRptE_deAttivarptBicAddebito" +
      "," + ALIAS + ".cod_attivarpt_sogg_pag_id_univ_pag_tipo_id_univoco as FESP_AttivaRptE_codAttivarptSoggPagIdUnivPagTipoIdUnivoco" +
      "," + ALIAS + ".cod_attivarpt_sogg_pag_id_univ_pag_codice_id_univoco as FESP_AttivaRptE_codAttivarptSoggPagIdUnivPagCodiceIdUnivoco" +
      "," + ALIAS + ".de_attivarpt_sogg_pag_anagrafica_pagatore as FESP_AttivaRptE_deAttivarptSoggPagAnagraficaPagatore" +
      "," + ALIAS + ".de_attivarpt_sogg_pag_indirizzo_pagatore as FESP_AttivaRptE_deAttivarptSoggPagIndirizzoPagatore" +
      "," + ALIAS + ".de_attivarpt_sogg_pag_civico_pagatore as FESP_AttivaRptE_deAttivarptSoggPagCivicoPagatore" +
      "," + ALIAS + ".cod_attivarpt_sogg_pag_cap_pagatore as FESP_AttivaRptE_codAttivarptSoggPagCapPagatore" +
      "," + ALIAS + ".de_attivarpt_sogg_pag_localita_pagatore as FESP_AttivaRptE_deAttivarptSoggPagLocalitaPagatore" +
      "," + ALIAS + ".de_attivarpt_sogg_pag_provincia_pagatore as FESP_AttivaRptE_deAttivarptSoggPagProvinciaPagatore" +
      "," + ALIAS + ".cod_attivarpt_sogg_pag_nazione_pagatore as FESP_AttivaRptE_codAttivarptSoggPagNazionePagatore" +
      "," + ALIAS + ".de_attivarpt_sogg_pag_email_pagatore as FESP_AttivaRptE_deAttivarptSoggPagEmailPagatore" +
      "," + ALIAS + ".dt_e_attivarpt as FESP_AttivaRptE_dtEAttivarpt" +
      "," + ALIAS + ".num_e_attivarpt_importo_singolo_versamento as FESP_AttivaRptE_numEAttivarptImportoSingoloVersamento" +
      "," + ALIAS + ".de_e_attivarpt_iban_accredito as FESP_AttivaRptE_deEAttivarptIbanAccredito" +
      "," + ALIAS + ".de_e_attivarpt_bic_accredito as FESP_AttivaRptE_deEAttivarptBicAccredito" +
      "," + ALIAS + ".cod_e_attivarpt_ente_benef_id_univ_benef_tipo_id_univoco as FESP_AttivaRptE_codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco" +
      "," + ALIAS + ".cod_e_attivarpt_ente_benef_id_univ_benef_codice_id_univoco as FESP_AttivaRptE_codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco" +
      "," + ALIAS + ".de_e_attivarpt_ente_benef_denominazione_beneficiario as FESP_AttivaRptE_deEAttivarptEnteBenefDenominazioneBeneficiario" +
      "," + ALIAS + ".cod_e_attivarpt_ente_benef_codice_unit_oper_beneficiario as FESP_AttivaRptE_codEAttivarptEnteBenefCodiceUnitOperBeneficiario" +
      "," + ALIAS + ".de_e_attivarpt_ente_benef_denom_unit_oper_beneficiario as FESP_AttivaRptE_deEAttivarptEnteBenefDenomUnitOperBeneficiario" +
      "," + ALIAS + ".de_e_attivarpt_ente_benef_indirizzo_beneficiario as FESP_AttivaRptE_deEAttivarptEnteBenefIndirizzoBeneficiario" +
      "," + ALIAS + ".de_e_attivarpt_ente_benef_civico_beneficiario as FESP_AttivaRptE_deEAttivarptEnteBenefCivicoBeneficiario" +
      "," + ALIAS + ".cod_e_attivarpt_ente_benef_cap_beneficiario as FESP_AttivaRptE_codEAttivarptEnteBenefCapBeneficiario" +
      "," + ALIAS + ".de_e_attivarpt_ente_benef_localita_beneficiario as FESP_AttivaRptE_deEAttivarptEnteBenefLocalitaBeneficiario" +
      "," + ALIAS + ".de_e_attivarpt_ente_benef_provincia_beneficiario as FESP_AttivaRptE_deEAttivarptEnteBenefProvinciaBeneficiario" +
      "," + ALIAS + ".cod_e_attivarpt_ente_benef_nazione_beneficiario as FESP_AttivaRptE_codEAttivarptEnteBenefNazioneBeneficiario" +
      "," + ALIAS + ".de_e_attivarpt_credenziali_pagatore as FESP_AttivaRptE_deEAttivarptCredenzialiPagatore" +
      "," + ALIAS + ".de_e_attivarpt_causale_versamento as FESP_AttivaRptE_deEAttivarptCausaleVersamento" +
      "," + ALIAS + ".de_attivarpt_esito as FESP_AttivaRptE_deAttivarptEsito" +
      "," + ALIAS + ".cod_attivarpt_fault_code as FESP_AttivaRptE_codAttivarptFaultCode" +
      "," + ALIAS + ".de_attivarpt_fault_string as FESP_AttivaRptE_deAttivarptFaultString" +
      "," + ALIAS + ".cod_attivarpt_id as FESP_AttivaRptE_codAttivarptId" +
      "," + ALIAS + ".de_attivarpt_description as FESP_AttivaRptE_deAttivarptDescription" +
      "," + ALIAS + ".cod_attivarpt_serial as FESP_AttivaRptE_codAttivarptSerial" +
      "," + ALIAS + ".cod_attivarpt_id_intermediario_psp as FESP_AttivaRptE_codAttivarptIdIntermediarioPsp" +
      "," + ALIAS + ".cod_attivarpt_id_canale_psp as FESP_AttivaRptE_codAttivarptIdCanalePsp" +
      "," + ALIAS + ".cod_attivarpt_original_fault_code as FESP_AttivaRptE_codAttivarptOriginalFaultCode" +
      "," + ALIAS + ".de_attivarpt_original_fault_string as FESP_AttivaRptE_deAttivarptOriginalFaultString" +
      "," + ALIAS + ".de_attivarpt_original_fault_description as FESP_AttivaRptE_deAttivarptOriginalFaultDescription";

  private Long mygovAttivaRptEId;
  private int version;
  private String codAttivarptIdPsp;
  private String codAttivarptIdentificativoIntermediarioPa;
  private String codAttivarptIdentificativoStazioneIntermediarioPa;
  private String codAttivarptIdentificativoDominio;
  private String codAttivarptIdentificativoUnivocoVersamento;
  private String codAttivarptCodiceContestoPagamento;
  private Date dtAttivarpt;
  private BigDecimal numAttivarptImportoSingoloVersamento;
  private String deAttivarptIbanAppoggio;
  private String deAttivarptBicAppoggio;
  private String codAttivarptSoggVersIdUnivVersTipoIdUnivoco;
  private String codAttivarptSoggVersIdUnivVersCodiceIdUnivoco;
  private String deAttivarptSoggVersAnagraficaVersante;
  private String deAttivarptSoggVersIndirizzoVersante;
  private String deAttivarptSoggVersCivicoVersante;
  private String codAttivarptSoggVersCapVersante;
  private String deAttivarptSoggVersLocalitaVersante;
  private String deAttivarptSoggVersProvinciaVersante;
  private String codAttivarptSoggVersNazioneVersante;
  private String deAttivarptSoggVersEmailVersante;
  private String deAttivarptIbanAddebito;
  private String deAttivarptBicAddebito;
  private String codAttivarptSoggPagIdUnivPagTipoIdUnivoco;
  private String codAttivarptSoggPagIdUnivPagCodiceIdUnivoco;
  private String deAttivarptSoggPagAnagraficaPagatore;
  private String deAttivarptSoggPagIndirizzoPagatore;
  private String deAttivarptSoggPagCivicoPagatore;
  private String codAttivarptSoggPagCapPagatore;
  private String deAttivarptSoggPagLocalitaPagatore;
  private String deAttivarptSoggPagProvinciaPagatore;
  private String codAttivarptSoggPagNazionePagatore;
  private String deAttivarptSoggPagEmailPagatore;
  private Date dtEAttivarpt;
  private BigDecimal numEAttivarptImportoSingoloVersamento;
  private String deEAttivarptIbanAccredito;
  private String deEAttivarptBicAccredito;
  private String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco;
  private String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco;
  private String deEAttivarptEnteBenefDenominazioneBeneficiario;
  private String codEAttivarptEnteBenefCodiceUnitOperBeneficiario;
  private String deEAttivarptEnteBenefDenomUnitOperBeneficiario;
  private String deEAttivarptEnteBenefIndirizzoBeneficiario;
  private String deEAttivarptEnteBenefCivicoBeneficiario;
  private String codEAttivarptEnteBenefCapBeneficiario;
  private String deEAttivarptEnteBenefLocalitaBeneficiario;
  private String deEAttivarptEnteBenefProvinciaBeneficiario;
  private String codEAttivarptEnteBenefNazioneBeneficiario;
  private String deEAttivarptCredenzialiPagatore;
  private String deEAttivarptCausaleVersamento;
  private String deAttivarptEsito;
  private String codAttivarptFaultCode;
  private String deAttivarptFaultString;
  private String codAttivarptId;
  private String deAttivarptDescription;
  private Integer codAttivarptSerial;
  private String codAttivarptIdIntermediarioPsp;
  private String codAttivarptIdCanalePsp;
  private String codAttivarptOriginalFaultCode;
  private String deAttivarptOriginalFaultString;
  private String deAttivarptOriginalFaultDescription;
}
