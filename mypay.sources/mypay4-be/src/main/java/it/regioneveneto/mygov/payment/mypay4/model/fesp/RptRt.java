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
import org.jdbi.v3.core.mapper.Nested;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovRptRtId")
public class RptRt extends BaseEntity {

  // Special handling for field località(=comune): truncate if > 35 char (i.e. max length on DB)
  //   This special handling is due to località selected by select list nazione/provincia/comune and on list of comuni
  //   there are some values whose length > 35. The choice of 35 chars is due to max length of corresponding field
  //   on PagoPA api.
  public final static int MAX_LENGTH_LOCALITA = 35;

  public final static String ALIAS = "FESP_RptRt";
  public final static String FIELDS = ""+ALIAS+".mygov_rpt_rt_id as FESP_RptRt_mygovRptRtId,"+ALIAS+".version as FESP_RptRt_version"+
      ","+ALIAS+".mygov_carrello_rpt_id as FESP_RptRt_mygovCarrelloRptId,"+ALIAS+".cod_ack_rt as FESP_RptRt_codAckRt"+
      ","+ALIAS+".dt_creazione_rpt as FESP_RptRt_dtCreazioneRpt"+
      ","+ALIAS+".dt_ultima_modifica_rpt as FESP_RptRt_dtUltimaModificaRpt"+
      ","+ALIAS+".dt_creazione_rt as FESP_RptRt_dtCreazioneRt,"+ALIAS+".dt_ultima_modifica_rt as FESP_RptRt_dtUltimaModificaRt"+
      ","+ALIAS+".de_rpt_inviarpt_password as FESP_RptRt_deRptInviarptPassword"+
      ","+ALIAS+".cod_rpt_inviarpt_id_psp as FESP_RptRt_codRptInviarptIdPsp"+
      ","+ALIAS+".cod_rpt_inviarpt_id_intermediario_psp as FESP_RptRt_codRptInviarptIdIntermediarioPsp"+
      ","+ALIAS+".cod_rpt_inviarpt_id_canale as FESP_RptRt_codRptInviarptIdCanale"+
      ","+ALIAS+".de_rpt_inviarpt_tipo_firma as FESP_RptRt_deRptInviarptTipoFirma"+
      ","+ALIAS+".cod_rpt_inviarpt_id_intermediario_pa as FESP_RptRt_codRptInviarptIdIntermediarioPa"+
      ","+ALIAS+".cod_rpt_inviarpt_id_stazione_intermediario_pa as FESP_RptRt_codRptInviarptIdStazioneIntermediarioPa"+
      ","+ALIAS+".cod_rpt_inviarpt_id_dominio as FESP_RptRt_codRptInviarptIdDominio"+
      ","+ALIAS+".cod_rpt_inviarpt_id_univoco_versamento as FESP_RptRt_codRptInviarptIdUnivocoVersamento"+
      ","+ALIAS+".cod_rpt_inviarpt_codice_contesto_pagamento as FESP_RptRt_codRptInviarptCodiceContestoPagamento"+
      ","+ALIAS+".de_rpt_inviarpt_esito as FESP_RptRt_deRptInviarptEsito"+
      ","+ALIAS+".num_rpt_inviarpt_redirect as FESP_RptRt_numRptInviarptRedirect"+
      ","+ALIAS+".cod_rpt_inviarpt_url as FESP_RptRt_codRptInviarptUrl"+
      ","+ALIAS+".cod_rpt_inviarpt_fault_code as FESP_RptRt_codRptInviarptFaultCode"+
      ","+ALIAS+".cod_rpt_inviarpt_fault_string as FESP_RptRt_codRptInviarptFaultString"+
      ","+ALIAS+".cod_rpt_inviarpt_id as FESP_RptRt_codRptInviarptId"+
      ","+ALIAS+".de_rpt_inviarpt_description as FESP_RptRt_deRptInviarptDescription"+
      ","+ALIAS+".num_rpt_inviarpt_serial as FESP_RptRt_numRptInviarptSerial"+
      ","+ALIAS+".de_rpt_versione_oggetto as FESP_RptRt_deRptVersioneOggetto"+
      ","+ALIAS+".cod_rpt_dom_id_dominio as FESP_RptRt_codRptDomIdDominio"+
      ","+ALIAS+".cod_rpt_dom_id_stazione_richiedente as FESP_RptRt_codRptDomIdStazioneRichiedente"+
      ","+ALIAS+".cod_rpt_id_messaggio_richiesta as FESP_RptRt_codRptIdMessaggioRichiesta"+
      ","+ALIAS+".dt_rpt_data_ora_messaggio_richiesta as FESP_RptRt_dtRptDataOraMessaggioRichiesta"+
      ","+ALIAS+".cod_rpt_autenticazione_soggetto as FESP_RptRt_codRptAutenticazioneSoggetto"+
      ","+ALIAS+".cod_rpt_sogg_vers_id_univ_vers_tipo_id_univoco as FESP_RptRt_codRptSoggVersIdUnivVersTipoIdUnivoco"+
      ","+ALIAS+".cod_rpt_sogg_vers_id_univ_vers_codice_id_univoco as FESP_RptRt_codRptSoggVersIdUnivVersCodiceIdUnivoco"+
      ","+ALIAS+".de_rpt_sogg_vers_anagrafica_versante as FESP_RptRt_deRptSoggVersAnagraficaVersante"+
      ","+ALIAS+".de_rpt_sogg_vers_indirizzo_versante as FESP_RptRt_deRptSoggVersIndirizzoVersante"+
      ","+ALIAS+".de_rpt_sogg_vers_civico_versante as FESP_RptRt_deRptSoggVersCivicoVersante"+
      ","+ALIAS+".cod_rpt_sogg_vers_cap_versante as FESP_RptRt_codRptSoggVersCapVersante"+
      ","+ALIAS+".de_rpt_sogg_vers_localita_versante as FESP_RptRt_deRptSoggVersLocalitaVersante"+
      ","+ALIAS+".de_rpt_sogg_vers_provincia_versante as FESP_RptRt_deRptSoggVersProvinciaVersante"+
      ","+ALIAS+".cod_rpt_sogg_vers_nazione_versante as FESP_RptRt_codRptSoggVersNazioneVersante"+
      ","+ALIAS+".de_rpt_sogg_vers_email_versante as FESP_RptRt_deRptSoggVersEmailVersante"+
      ","+ALIAS+".cod_rpt_sogg_pag_id_univ_pag_tipo_id_univoco as FESP_RptRt_codRptSoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_rpt_sogg_pag_id_univ_pag_codice_id_univoco as FESP_RptRt_codRptSoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".de_rpt_sogg_pag_anagrafica_pagatore as FESP_RptRt_deRptSoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_rpt_sogg_pag_indirizzo_pagatore as FESP_RptRt_deRptSoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_rpt_sogg_pag_civico_pagatore as FESP_RptRt_deRptSoggPagCivicoPagatore"+
      ","+ALIAS+".cod_rpt_sogg_pag_cap_pagatore as FESP_RptRt_codRptSoggPagCapPagatore"+
      ","+ALIAS+".de_rpt_sogg_pag_localita_pagatore as FESP_RptRt_deRptSoggPagLocalitaPagatore"+
      ","+ALIAS+".de_rpt_sogg_pag_provincia_pagatore as FESP_RptRt_deRptSoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_rpt_sogg_pag_nazione_pagatore as FESP_RptRt_codRptSoggPagNazionePagatore"+
      ","+ALIAS+".de_rpt_sogg_pag_email_pagatore as FESP_RptRt_deRptSoggPagEmailPagatore"+
      ","+ALIAS+".cod_rpt_ente_benef_id_univ_benef_tipo_id_univoco as FESP_RptRt_codRptEnteBenefIdUnivBenefTipoIdUnivoco"+
      ","+ALIAS+".cod_rpt_ente_benef_id_univ_benef_codice_id_univoco as FESP_RptRt_codRptEnteBenefIdUnivBenefCodiceIdUnivoco"+
      ","+ALIAS+".de_rpt_ente_benef_denominazione_beneficiario as FESP_RptRt_deRptEnteBenefDenominazioneBeneficiario"+
      ","+ALIAS+".cod_rpt_ente_benef_codice_unit_oper_beneficiario as FESP_RptRt_codRptEnteBenefCodiceUnitOperBeneficiario"+
      ","+ALIAS+".de_rpt_ente_benef_denom_unit_oper_beneficiario as FESP_RptRt_deRptEnteBenefDenomUnitOperBeneficiario"+
      ","+ALIAS+".de_rpt_ente_benef_indirizzo_beneficiario as FESP_RptRt_deRptEnteBenefIndirizzoBeneficiario"+
      ","+ALIAS+".de_rpt_ente_benef_civico_beneficiario as FESP_RptRt_deRptEnteBenefCivicoBeneficiario"+
      ","+ALIAS+".cod_rpt_ente_benef_cap_beneficiario as FESP_RptRt_codRptEnteBenefCapBeneficiario"+
      ","+ALIAS+".de_rpt_ente_benef_localita_beneficiario as FESP_RptRt_deRptEnteBenefLocalitaBeneficiario"+
      ","+ALIAS+".de_rpt_ente_benef_provincia_beneficiario as FESP_RptRt_deRptEnteBenefProvinciaBeneficiario"+
      ","+ALIAS+".cod_rpt_ente_benef_nazione_beneficiario as FESP_RptRt_codRptEnteBenefNazioneBeneficiario"+
      ","+ALIAS+".dt_rpt_dati_vers_data_esecuzione_pagamento as FESP_RptRt_dtRptDatiVersDataEsecuzionePagamento"+
      ","+ALIAS+".num_rpt_dati_vers_importo_totale_da_versare as FESP_RptRt_numRptDatiVersImportoTotaleDaVersare"+
      ","+ALIAS+".cod_rpt_dati_vers_tipo_versamento as FESP_RptRt_codRptDatiVersTipoVersamento"+
      ","+ALIAS+".cod_rpt_dati_vers_id_univoco_versamento as FESP_RptRt_codRptDatiVersIdUnivocoVersamento"+
      ","+ALIAS+".cod_rpt_dati_vers_codice_contesto_pagamento as FESP_RptRt_codRptDatiVersCodiceContestoPagamento"+
      ","+ALIAS+".de_rpt_dati_vers_iban_addebito as FESP_RptRt_deRptDatiVersIbanAddebito"+
      ","+ALIAS+".de_rpt_dati_vers_bic_addebito as FESP_RptRt_deRptDatiVersBicAddebito"+
      ","+ALIAS+".cod_rpt_dati_vers_firma_ricevuta as FESP_RptRt_codRptDatiVersFirmaRicevuta"+
      ","+ALIAS+".de_rt_inviart_tipo_firma as FESP_RptRt_deRtInviartTipoFirma"+
      ","+ALIAS+".cod_rt_inviart_id_intermediario_pa as FESP_RptRt_codRtInviartIdIntermediarioPa"+
      ","+ALIAS+".cod_rt_inviart_id_stazione_intermediario_pa as FESP_RptRt_codRtInviartIdStazioneIntermediarioPa"+
      ","+ALIAS+".cod_rt_inviart_id_dominio as FESP_RptRt_codRtInviartIdDominio"+
      ","+ALIAS+".cod_rt_inviart_id_univoco_versamento as FESP_RptRt_codRtInviartIdUnivocoVersamento"+
      ","+ALIAS+".cod_rt_inviart_codice_contesto_pagamento as FESP_RptRt_codRtInviartCodiceContestoPagamento"+
      ","+ALIAS+".cod_rt_inviart_esito as FESP_RptRt_codRtInviartEsito"+
      ","+ALIAS+".cod_rt_inviart_fault_code as FESP_RptRt_codRtInviartFaultCode"+
      ","+ALIAS+".cod_rt_inviart_fault_string as FESP_RptRt_codRtInviartFaultString"+
      ","+ALIAS+".cod_rt_inviart_id as FESP_RptRt_codRtInviartId"+
      ","+ALIAS+".de_rt_inviart_description as FESP_RptRt_deRtInviartDescription"+
      ","+ALIAS+".num_rt_inviart_serial as FESP_RptRt_numRtInviartSerial"+
      ","+ALIAS+".de_rt_versione_oggetto as FESP_RptRt_deRtVersioneOggetto"+
      ","+ALIAS+".cod_rt_dom_id_dominio as FESP_RptRt_codRtDomIdDominio"+
      ","+ALIAS+".cod_rt_dom_id_stazione_richiedente as FESP_RptRt_codRtDomIdStazioneRichiedente"+
      ","+ALIAS+".cod_rt_id_messaggio_ricevuta as FESP_RptRt_codRtIdMessaggioRicevuta"+
      ","+ALIAS+".dt_rt_data_ora_messaggio_ricevuta as FESP_RptRt_dtRtDataOraMessaggioRicevuta"+
      ","+ALIAS+".cod_rt_riferimento_messaggio_richiesta as FESP_RptRt_codRtRiferimentoMessaggioRichiesta"+
      ","+ALIAS+".dt_rt_riferimento_data_richiesta as FESP_RptRt_dtRtRiferimentoDataRichiesta"+
      ","+ALIAS+".cod_rt_istit_attes_id_univ_attes_tipo_id_univoco as FESP_RptRt_codRtIstitAttesIdUnivAttesTipoIdUnivoco"+
      ","+ALIAS+".cod_rt_istit_attes_id_univ_attes_codice_id_univoco as FESP_RptRt_codRtIstitAttesIdUnivAttesCodiceIdUnivoco"+
      ","+ALIAS+".de_rt_istit_attes_denominazione_attestante as FESP_RptRt_deRtIstitAttesDenominazioneAttestante"+
      ","+ALIAS+".cod_rt_istit_attes_codice_unit_oper_attestante as FESP_RptRt_codRtIstitAttesCodiceUnitOperAttestante"+
      ","+ALIAS+".de_rt_istit_attes_denom_unit_oper_attestante as FESP_RptRt_deRtIstitAttesDenomUnitOperAttestante"+
      ","+ALIAS+".de_rt_istit_attes_indirizzo_attestante as FESP_RptRt_deRtIstitAttesIndirizzoAttestante"+
      ","+ALIAS+".de_rt_istit_attes_civico_attestante as FESP_RptRt_deRtIstitAttesCivicoAttestante"+
      ","+ALIAS+".cod_rt_istit_attes_cap_attestante as FESP_RptRt_codRtIstitAttesCapAttestante"+
      ","+ALIAS+".de_rt_istit_attes_localita_attestante as FESP_RptRt_deRtIstitAttesLocalitaAttestante"+
      ","+ALIAS+".de_rt_istit_attes_provincia_attestante as FESP_RptRt_deRtIstitAttesProvinciaAttestante"+
      ","+ALIAS+".cod_rt_istit_attes_nazione_attestante as FESP_RptRt_codRtIstitAttesNazioneAttestante"+
      ","+ALIAS+".cod_rt_ente_benef_id_univ_benef_tipo_id_univoco as FESP_RptRt_codRtEnteBenefIdUnivBenefTipoIdUnivoco"+
      ","+ALIAS+".cod_rt_ente_benef_id_univ_benef_codice_id_univoco as FESP_RptRt_codRtEnteBenefIdUnivBenefCodiceIdUnivoco"+
      ","+ALIAS+".de_rt_ente_benef_denominazione_beneficiario as FESP_RptRt_deRtEnteBenefDenominazioneBeneficiario"+
      ","+ALIAS+".cod_rt_ente_benef_codice_unit_oper_beneficiario as FESP_RptRt_codRtEnteBenefCodiceUnitOperBeneficiario"+
      ","+ALIAS+".de_rt_ente_benef_denom_unit_oper_beneficiario as FESP_RptRt_deRtEnteBenefDenomUnitOperBeneficiario"+
      ","+ALIAS+".de_rt_ente_benef_indirizzo_beneficiario as FESP_RptRt_deRtEnteBenefIndirizzoBeneficiario"+
      ","+ALIAS+".de_rt_ente_benef_civico_beneficiario as FESP_RptRt_deRtEnteBenefCivicoBeneficiario"+
      ","+ALIAS+".cod_rt_ente_benef_cap_beneficiario as FESP_RptRt_codRtEnteBenefCapBeneficiario"+
      ","+ALIAS+".de_rt_ente_benef_localita_beneficiario as FESP_RptRt_deRtEnteBenefLocalitaBeneficiario"+
      ","+ALIAS+".de_rt_ente_benef_provincia_beneficiario as FESP_RptRt_deRtEnteBenefProvinciaBeneficiario"+
      ","+ALIAS+".cod_rt_ente_benef_nazione_beneficiario as FESP_RptRt_codRtEnteBenefNazioneBeneficiario"+
      ","+ALIAS+".cod_rt_sogg_vers_id_univ_vers_tipo_id_univoco as FESP_RptRt_codRtSoggVersIdUnivVersTipoIdUnivoco"+
      ","+ALIAS+".cod_rt_sogg_vers_id_univ_vers_codice_id_univoco as FESP_RptRt_codRtSoggVersIdUnivVersCodiceIdUnivoco"+
      ","+ALIAS+".de_rt_sogg_vers_anagrafica_versante as FESP_RptRt_deRtSoggVersAnagraficaVersante"+
      ","+ALIAS+".de_rt_sogg_vers_indirizzo_versante as FESP_RptRt_deRtSoggVersIndirizzoVersante"+
      ","+ALIAS+".de_rt_sogg_vers_civico_versante as FESP_RptRt_deRtSoggVersCivicoVersante"+
      ","+ALIAS+".cod_rt_sogg_vers_cap_versante as FESP_RptRt_codRtSoggVersCapVersante"+
      ","+ALIAS+".de_rt_sogg_vers_localita_versante as FESP_RptRt_deRtSoggVersLocalitaVersante"+
      ","+ALIAS+".de_rt_sogg_vers_provincia_versante as FESP_RptRt_deRtSoggVersProvinciaVersante"+
      ","+ALIAS+".cod_rt_sogg_vers_nazione_versante as FESP_RptRt_codRtSoggVersNazioneVersante"+
      ","+ALIAS+".de_rt_sogg_vers_email_versante as FESP_RptRt_deRtSoggVersEmailVersante"+
      ","+ALIAS+".cod_rt_sogg_pag_id_univ_pag_tipo_id_univoco as FESP_RptRt_codRtSoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_rt_sogg_pag_id_univ_pag_codice_id_univoco as FESP_RptRt_codRtSoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".de_rt_sogg_pag_anagrafica_pagatore as FESP_RptRt_deRtSoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_rt_sogg_pag_indirizzo_pagatore as FESP_RptRt_deRtSoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_rt_sogg_pag_civico_pagatore as FESP_RptRt_deRtSoggPagCivicoPagatore"+
      ","+ALIAS+".cod_rt_sogg_pag_cap_pagatore as FESP_RptRt_codRtSoggPagCapPagatore"+
      ","+ALIAS+".de_rt_sogg_pag_localita_pagatore as FESP_RptRt_deRtSoggPagLocalitaPagatore"+
      ","+ALIAS+".de_rt_sogg_pag_provincia_pagatore as FESP_RptRt_deRtSoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_rt_sogg_pag_nazione_pagatore as FESP_RptRt_codRtSoggPagNazionePagatore"+
      ","+ALIAS+".de_rt_sogg_pag_email_pagatore as FESP_RptRt_deRtSoggPagEmailPagatore"+
      ","+ALIAS+".cod_rt_dati_pag_codice_esito_pagamento as FESP_RptRt_codRtDatiPagCodiceEsitoPagamento"+
      ","+ALIAS+".num_rt_dati_pag_importo_totale_pagato as FESP_RptRt_numRtDatiPagImportoTotalePagato"+
      ","+ALIAS+".cod_rt_dati_pag_id_univoco_versamento as FESP_RptRt_codRtDatiPagIdUnivocoVersamento"+
      ","+ALIAS+".cod_rt_dati_pag_codice_contesto_pagamento as FESP_RptRt_codRtDatiPagCodiceContestoPagamento"+
      ","+ALIAS+".id_session as FESP_RptRt_idSession,"+ALIAS+".mygov_rp_e_id as FESP_RptRt_mygovRpEId"+
      ","+ALIAS+".blb_rt_payload as FESP_RptRt_blbRtPayload,"+ALIAS+".modello_pagamento as FESP_RptRt_modelloPagamento"+
      ","+ALIAS+".cod_rpt_inviarpt_original_fault_code as FESP_RptRt_codRptInviarptOriginalFaultCode"+
      ","+ALIAS+".de_rpt_inviarpt_original_fault_string as FESP_RptRt_deRptInviarptOriginalFaultString"+
      ","+ALIAS+".de_rpt_inviarpt_original_fault_description as FESP_RptRt_deRptInviarptOriginalFaultDescription"+
      ","+ALIAS+".cod_rt_inviart_original_fault_code as FESP_RptRt_codRtInviartOriginalFaultCode"+
      ","+ALIAS+".de_rt_inviart_original_fault_string as FESP_RptRt_deRtInviartOriginalFaultString"+
      ","+ALIAS+".de_rt_inviart_original_fault_description as FESP_RptRt_deRtInviartOriginalFaultDescription";

  private Long mygovRptRtId;
  private int version;
  @Nested(CarrelloRpt.ALIAS)
  private CarrelloRpt mygovCarrelloRptId;
  private String codAckRt;
  private Date dtCreazioneRpt;
  private Date dtUltimaModificaRpt;
  private Date dtCreazioneRt;
  private Date dtUltimaModificaRt;
  private String deRptInviarptPassword;
  private String codRptInviarptIdPsp;
  private String codRptInviarptIdIntermediarioPsp;
  private String codRptInviarptIdCanale;
  private String deRptInviarptTipoFirma;
  private String codRptInviarptIdIntermediarioPa;
  private String codRptInviarptIdStazioneIntermediarioPa;
  private String codRptInviarptIdDominio;
  private String codRptInviarptIdUnivocoVersamento;
  private String codRptInviarptCodiceContestoPagamento;
  private String deRptInviarptEsito;
  private Integer numRptInviarptRedirect;
  private String codRptInviarptUrl;
  private String codRptInviarptFaultCode;
  private String codRptInviarptFaultString;
  private String codRptInviarptId;
  private String deRptInviarptDescription;
  private Integer numRptInviarptSerial;
  private String deRptVersioneOggetto;
  private String codRptDomIdDominio;
  private String codRptDomIdStazioneRichiedente;
  private String codRptIdMessaggioRichiesta;
  private Date dtRptDataOraMessaggioRichiesta;
  private String codRptAutenticazioneSoggetto;
  private String codRptSoggVersIdUnivVersTipoIdUnivoco;
  private String codRptSoggVersIdUnivVersCodiceIdUnivoco;
  private String deRptSoggVersAnagraficaVersante;
  private String deRptSoggVersIndirizzoVersante;
  private String deRptSoggVersCivicoVersante;
  private String codRptSoggVersCapVersante;
  private String deRptSoggVersLocalitaVersante;
  private String deRptSoggVersProvinciaVersante;
  private String codRptSoggVersNazioneVersante;
  private String deRptSoggVersEmailVersante;
  private String codRptSoggPagIdUnivPagTipoIdUnivoco;
  private String codRptSoggPagIdUnivPagCodiceIdUnivoco;
  private String deRptSoggPagAnagraficaPagatore;
  private String deRptSoggPagIndirizzoPagatore;
  private String deRptSoggPagCivicoPagatore;
  private String codRptSoggPagCapPagatore;
  private String deRptSoggPagLocalitaPagatore;
  private String deRptSoggPagProvinciaPagatore;
  private String codRptSoggPagNazionePagatore;
  private String deRptSoggPagEmailPagatore;
  private String codRptEnteBenefIdUnivBenefTipoIdUnivoco;
  private String codRptEnteBenefIdUnivBenefCodiceIdUnivoco;
  private String deRptEnteBenefDenominazioneBeneficiario;
  private String codRptEnteBenefCodiceUnitOperBeneficiario;
  private String deRptEnteBenefDenomUnitOperBeneficiario;
  private String deRptEnteBenefIndirizzoBeneficiario;
  private String deRptEnteBenefCivicoBeneficiario;
  private String codRptEnteBenefCapBeneficiario;
  private String deRptEnteBenefLocalitaBeneficiario;
  private String deRptEnteBenefProvinciaBeneficiario;
  private String codRptEnteBenefNazioneBeneficiario;
  private Date dtRptDatiVersDataEsecuzionePagamento;
  private BigDecimal numRptDatiVersImportoTotaleDaVersare;
  private String codRptDatiVersTipoVersamento;
  private String codRptDatiVersIdUnivocoVersamento;
  private String codRptDatiVersCodiceContestoPagamento;
  private String deRptDatiVersIbanAddebito;
  private String deRptDatiVersBicAddebito;
  private String codRptDatiVersFirmaRicevuta;
  private String deRtInviartTipoFirma;
  private String codRtInviartIdIntermediarioPa;
  private String codRtInviartIdStazioneIntermediarioPa;
  private String codRtInviartIdDominio;
  private String codRtInviartIdUnivocoVersamento;
  private String codRtInviartCodiceContestoPagamento;
  private String codRtInviartEsito;
  private String codRtInviartFaultCode;
  private String codRtInviartFaultString;
  private String codRtInviartId;
  private String deRtInviartDescription;
  private Integer numRtInviartSerial;
  private String deRtVersioneOggetto;
  private String codRtDomIdDominio;
  private String codRtDomIdStazioneRichiedente;
  private String codRtIdMessaggioRicevuta;
  private Date dtRtDataOraMessaggioRicevuta;
  private String codRtRiferimentoMessaggioRichiesta;
  private Date dtRtRiferimentoDataRichiesta;
  private String codRtIstitAttesIdUnivAttesTipoIdUnivoco;
  private String codRtIstitAttesIdUnivAttesCodiceIdUnivoco;
  private String deRtIstitAttesDenominazioneAttestante;
  private String codRtIstitAttesCodiceUnitOperAttestante;
  private String deRtIstitAttesDenomUnitOperAttestante;
  private String deRtIstitAttesIndirizzoAttestante;
  private String deRtIstitAttesCivicoAttestante;
  private String codRtIstitAttesCapAttestante;
  private String deRtIstitAttesLocalitaAttestante;
  private String deRtIstitAttesProvinciaAttestante;
  private String codRtIstitAttesNazioneAttestante;
  private String codRtEnteBenefIdUnivBenefTipoIdUnivoco;
  private String codRtEnteBenefIdUnivBenefCodiceIdUnivoco;
  private String deRtEnteBenefDenominazioneBeneficiario;
  private String codRtEnteBenefCodiceUnitOperBeneficiario;
  private String deRtEnteBenefDenomUnitOperBeneficiario;
  private String deRtEnteBenefIndirizzoBeneficiario;
  private String deRtEnteBenefCivicoBeneficiario;
  private String codRtEnteBenefCapBeneficiario;
  private String deRtEnteBenefLocalitaBeneficiario;
  private String deRtEnteBenefProvinciaBeneficiario;
  private String codRtEnteBenefNazioneBeneficiario;
  private String codRtSoggVersIdUnivVersTipoIdUnivoco;
  private String codRtSoggVersIdUnivVersCodiceIdUnivoco;
  private String deRtSoggVersAnagraficaVersante;
  private String deRtSoggVersIndirizzoVersante;
  private String deRtSoggVersCivicoVersante;
  private String codRtSoggVersCapVersante;
  private String deRtSoggVersLocalitaVersante;
  private String deRtSoggVersProvinciaVersante;
  private String codRtSoggVersNazioneVersante;
  private String deRtSoggVersEmailVersante;
  private String codRtSoggPagIdUnivPagTipoIdUnivoco;
  private String codRtSoggPagIdUnivPagCodiceIdUnivoco;
  private String deRtSoggPagAnagraficaPagatore;
  private String deRtSoggPagIndirizzoPagatore;
  private String deRtSoggPagCivicoPagatore;
  private String codRtSoggPagCapPagatore;
  private String deRtSoggPagLocalitaPagatore;
  private String deRtSoggPagProvinciaPagatore;
  private String codRtSoggPagNazionePagatore;
  private String deRtSoggPagEmailPagatore;
  private String codRtDatiPagCodiceEsitoPagamento;
  private BigDecimal numRtDatiPagImportoTotalePagato;
  private String codRtDatiPagIdUnivocoVersamento;
  private String codRtDatiPagCodiceContestoPagamento;
  private String idSession;
  private long mygovRpEId;
  private byte[] blbRtPayload;
  private Integer modelloPagamento;
  private String codRptInviarptOriginalFaultCode;
  private String deRptInviarptOriginalFaultString;
  private String deRptInviarptOriginalFaultDescription;
  private String codRtInviartOriginalFaultCode;
  private String deRtInviartOriginalFaultString;
  private String deRtInviartOriginalFaultDescription;
}
