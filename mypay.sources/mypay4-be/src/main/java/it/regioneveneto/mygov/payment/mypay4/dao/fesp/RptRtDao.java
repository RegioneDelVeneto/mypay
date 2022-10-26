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
import it.regioneveneto.mygov.payment.mypay4.model.fesp.CarrelloRpt;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface RptRtDao extends BaseDao {

  public final static String REMOVE_FROM_SCOPE_CHIEDI_STATO_RPT = "SKIP_CHIEDI_STATO_RTP";

  @SqlUpdate(" insert into mygov_rpt_rt (" +
      "  mygov_rpt_rt_id , version , mygov_carrello_rpt_id , cod_ack_rt , dt_creazione_rpt , dt_ultima_modifica_rpt "+
      ", dt_creazione_rt , dt_ultima_modifica_rt , de_rpt_inviarpt_password , cod_rpt_inviarpt_id_psp "+
      ", cod_rpt_inviarpt_id_intermediario_psp , cod_rpt_inviarpt_id_canale , de_rpt_inviarpt_tipo_firma "+
      ", cod_rpt_inviarpt_id_intermediario_pa , cod_rpt_inviarpt_id_stazione_intermediario_pa "+
      ", cod_rpt_inviarpt_id_dominio , cod_rpt_inviarpt_id_univoco_versamento "+
      ", cod_rpt_inviarpt_codice_contesto_pagamento , de_rpt_inviarpt_esito , num_rpt_inviarpt_redirect "+
      ", cod_rpt_inviarpt_url , cod_rpt_inviarpt_fault_code , cod_rpt_inviarpt_fault_string , cod_rpt_inviarpt_id "+
      ", de_rpt_inviarpt_description , num_rpt_inviarpt_serial , de_rpt_versione_oggetto , cod_rpt_dom_id_dominio "+
      ", cod_rpt_dom_id_stazione_richiedente , cod_rpt_id_messaggio_richiesta , dt_rpt_data_ora_messaggio_richiesta "+
      ", cod_rpt_autenticazione_soggetto , cod_rpt_sogg_vers_id_univ_vers_tipo_id_univoco "+
      ", cod_rpt_sogg_vers_id_univ_vers_codice_id_univoco , de_rpt_sogg_vers_anagrafica_versante "+
      ", de_rpt_sogg_vers_indirizzo_versante , de_rpt_sogg_vers_civico_versante , cod_rpt_sogg_vers_cap_versante "+
      ", de_rpt_sogg_vers_localita_versante , de_rpt_sogg_vers_provincia_versante , cod_rpt_sogg_vers_nazione_versante "+
      ", de_rpt_sogg_vers_email_versante , cod_rpt_sogg_pag_id_univ_pag_tipo_id_univoco "+
      ", cod_rpt_sogg_pag_id_univ_pag_codice_id_univoco , de_rpt_sogg_pag_anagrafica_pagatore "+
      ", de_rpt_sogg_pag_indirizzo_pagatore , de_rpt_sogg_pag_civico_pagatore , cod_rpt_sogg_pag_cap_pagatore "+
      ", de_rpt_sogg_pag_localita_pagatore , de_rpt_sogg_pag_provincia_pagatore , cod_rpt_sogg_pag_nazione_pagatore "+
      ", de_rpt_sogg_pag_email_pagatore , cod_rpt_ente_benef_id_univ_benef_tipo_id_univoco "+
      ", cod_rpt_ente_benef_id_univ_benef_codice_id_univoco , de_rpt_ente_benef_denominazione_beneficiario "+
      ", cod_rpt_ente_benef_codice_unit_oper_beneficiario , de_rpt_ente_benef_denom_unit_oper_beneficiario "+
      ", de_rpt_ente_benef_indirizzo_beneficiario , de_rpt_ente_benef_civico_beneficiario "+
      ", cod_rpt_ente_benef_cap_beneficiario , de_rpt_ente_benef_localita_beneficiario "+
      ", de_rpt_ente_benef_provincia_beneficiario , cod_rpt_ente_benef_nazione_beneficiario "+
      ", dt_rpt_dati_vers_data_esecuzione_pagamento , num_rpt_dati_vers_importo_totale_da_versare "+
      ", cod_rpt_dati_vers_tipo_versamento , cod_rpt_dati_vers_id_univoco_versamento "+
      ", cod_rpt_dati_vers_codice_contesto_pagamento , de_rpt_dati_vers_iban_addebito , de_rpt_dati_vers_bic_addebito "+
      ", cod_rpt_dati_vers_firma_ricevuta , de_rt_inviart_tipo_firma , cod_rt_inviart_id_intermediario_pa "+
      ", cod_rt_inviart_id_stazione_intermediario_pa , cod_rt_inviart_id_dominio , cod_rt_inviart_id_univoco_versamento "+
      ", cod_rt_inviart_codice_contesto_pagamento , cod_rt_inviart_esito , cod_rt_inviart_fault_code "+
      ", cod_rt_inviart_fault_string , cod_rt_inviart_id , de_rt_inviart_description , num_rt_inviart_serial "+
      ", de_rt_versione_oggetto , cod_rt_dom_id_dominio , cod_rt_dom_id_stazione_richiedente "+
      ", cod_rt_id_messaggio_ricevuta , dt_rt_data_ora_messaggio_ricevuta , cod_rt_riferimento_messaggio_richiesta "+
      ", dt_rt_riferimento_data_richiesta , cod_rt_istit_attes_id_univ_attes_tipo_id_univoco "+
      ", cod_rt_istit_attes_id_univ_attes_codice_id_univoco , de_rt_istit_attes_denominazione_attestante "+
      ", cod_rt_istit_attes_codice_unit_oper_attestante , de_rt_istit_attes_denom_unit_oper_attestante "+
      ", de_rt_istit_attes_indirizzo_attestante , de_rt_istit_attes_civico_attestante "+
      ", cod_rt_istit_attes_cap_attestante , de_rt_istit_attes_localita_attestante "+
      ", de_rt_istit_attes_provincia_attestante , cod_rt_istit_attes_nazione_attestante "+
      ", cod_rt_ente_benef_id_univ_benef_tipo_id_univoco , cod_rt_ente_benef_id_univ_benef_codice_id_univoco "+
      ", de_rt_ente_benef_denominazione_beneficiario , cod_rt_ente_benef_codice_unit_oper_beneficiario "+
      ", de_rt_ente_benef_denom_unit_oper_beneficiario , de_rt_ente_benef_indirizzo_beneficiario "+
      ", de_rt_ente_benef_civico_beneficiario , cod_rt_ente_benef_cap_beneficiario "+
      ", de_rt_ente_benef_localita_beneficiario , de_rt_ente_benef_provincia_beneficiario "+
      ", cod_rt_ente_benef_nazione_beneficiario , cod_rt_sogg_vers_id_univ_vers_tipo_id_univoco "+
      ", cod_rt_sogg_vers_id_univ_vers_codice_id_univoco , de_rt_sogg_vers_anagrafica_versante "+
      ", de_rt_sogg_vers_indirizzo_versante , de_rt_sogg_vers_civico_versante , cod_rt_sogg_vers_cap_versante "+
      ", de_rt_sogg_vers_localita_versante , de_rt_sogg_vers_provincia_versante , cod_rt_sogg_vers_nazione_versante "+
      ", de_rt_sogg_vers_email_versante , cod_rt_sogg_pag_id_univ_pag_tipo_id_univoco "+
      ", cod_rt_sogg_pag_id_univ_pag_codice_id_univoco , de_rt_sogg_pag_anagrafica_pagatore "+
      ", de_rt_sogg_pag_indirizzo_pagatore , de_rt_sogg_pag_civico_pagatore , cod_rt_sogg_pag_cap_pagatore "+
      ", de_rt_sogg_pag_localita_pagatore , de_rt_sogg_pag_provincia_pagatore , cod_rt_sogg_pag_nazione_pagatore "+
      ", de_rt_sogg_pag_email_pagatore , cod_rt_dati_pag_codice_esito_pagamento , num_rt_dati_pag_importo_totale_pagato "+
      ", cod_rt_dati_pag_id_univoco_versamento , cod_rt_dati_pag_codice_contesto_pagamento , id_session "+
      ", mygov_rp_e_id , blb_rt_payload , modello_pagamento , cod_rpt_inviarpt_original_fault_code "+
      ", de_rpt_inviarpt_original_fault_string , de_rpt_inviarpt_original_fault_description "+
      ", cod_rt_inviart_original_fault_code , de_rt_inviart_original_fault_string "+
      ", de_rt_inviart_original_fault_description ) values ( " +
      " nextval('mygov_rpt_rt_mygov_rpt_rt_id_seq'), "+
      ":r.version, "+
      ":r.mygovCarrelloRptId?.mygovCarrelloRptId, "+
      ":r.codAckRt, "+
      "coalesce(:r.dtCreazioneRpt, now()), "+
      "coalesce(:r.dtUltimaModificaRpt, now()), "+
      ":r.dtCreazioneRt, "+
      ":r.dtUltimaModificaRt, "+
      ":r.deRptInviarptPassword, "+
      ":r.codRptInviarptIdPsp, "+
      ":r.codRptInviarptIdIntermediarioPsp, "+
      ":r.codRptInviarptIdCanale, "+
      ":r.deRptInviarptTipoFirma, "+
      ":r.codRptInviarptIdIntermediarioPa, "+
      ":r.codRptInviarptIdStazioneIntermediarioPa, "+
      ":r.codRptInviarptIdDominio, "+
      ":r.codRptInviarptIdUnivocoVersamento, "+
      ":r.codRptInviarptCodiceContestoPagamento, "+
      ":r.deRptInviarptEsito, "+
      ":r.numRptInviarptRedirect, "+
      ":r.codRptInviarptUrl, "+
      ":r.codRptInviarptFaultCode, "+
      ":r.codRptInviarptFaultString, "+
      ":r.codRptInviarptId, "+
      ":r.deRptInviarptDescription, "+
      ":r.numRptInviarptSerial, "+
      ":r.deRptVersioneOggetto, "+
      ":r.codRptDomIdDominio, "+
      ":r.codRptDomIdStazioneRichiedente, "+
      ":r.codRptIdMessaggioRichiesta, "+
      ":r.dtRptDataOraMessaggioRichiesta, "+
      ":r.codRptAutenticazioneSoggetto, "+
      ":r.codRptSoggVersIdUnivVersTipoIdUnivoco, "+
      ":r.codRptSoggVersIdUnivVersCodiceIdUnivoco, "+
      ":r.deRptSoggVersAnagraficaVersante, "+
      ":r.deRptSoggVersIndirizzoVersante, "+
      ":r.deRptSoggVersCivicoVersante, "+
      ":r.codRptSoggVersCapVersante, "+
      "left(:r.deRptSoggVersLocalitaVersante,"+RptRt.MAX_LENGTH_LOCALITA+"), "+
      ":r.deRptSoggVersProvinciaVersante, "+
      ":r.codRptSoggVersNazioneVersante, "+
      ":r.deRptSoggVersEmailVersante, "+
      ":r.codRptSoggPagIdUnivPagTipoIdUnivoco, "+
      ":r.codRptSoggPagIdUnivPagCodiceIdUnivoco, "+
      ":r.deRptSoggPagAnagraficaPagatore, "+
      ":r.deRptSoggPagIndirizzoPagatore, "+
      ":r.deRptSoggPagCivicoPagatore, "+
      ":r.codRptSoggPagCapPagatore, "+
      "left(:r.deRptSoggPagLocalitaPagatore,"+RptRt.MAX_LENGTH_LOCALITA+"), "+
      ":r.deRptSoggPagProvinciaPagatore, "+
      ":r.codRptSoggPagNazionePagatore, "+
      ":r.deRptSoggPagEmailPagatore, "+
      ":r.codRptEnteBenefIdUnivBenefTipoIdUnivoco, "+
      ":r.codRptEnteBenefIdUnivBenefCodiceIdUnivoco, "+
      ":r.deRptEnteBenefDenominazioneBeneficiario, "+
      ":r.codRptEnteBenefCodiceUnitOperBeneficiario, "+
      ":r.deRptEnteBenefDenomUnitOperBeneficiario, "+
      ":r.deRptEnteBenefIndirizzoBeneficiario, "+
      ":r.deRptEnteBenefCivicoBeneficiario, "+
      ":r.codRptEnteBenefCapBeneficiario, "+
      ":r.deRptEnteBenefLocalitaBeneficiario, "+
      ":r.deRptEnteBenefProvinciaBeneficiario, "+
      ":r.codRptEnteBenefNazioneBeneficiario, "+
      ":r.dtRptDatiVersDataEsecuzionePagamento, "+
      ":r.numRptDatiVersImportoTotaleDaVersare, "+
      ":r.codRptDatiVersTipoVersamento, "+
      ":r.codRptDatiVersIdUnivocoVersamento, "+
      ":r.codRptDatiVersCodiceContestoPagamento, "+
      ":r.deRptDatiVersIbanAddebito, "+
      ":r.deRptDatiVersBicAddebito, "+
      ":r.codRptDatiVersFirmaRicevuta, "+
      ":r.deRtInviartTipoFirma, "+
      ":r.codRtInviartIdIntermediarioPa, "+
      ":r.codRtInviartIdStazioneIntermediarioPa, "+
      ":r.codRtInviartIdDominio, "+
      ":r.codRtInviartIdUnivocoVersamento, "+
      ":r.codRtInviartCodiceContestoPagamento, "+
      ":r.codRtInviartEsito, "+
      ":r.codRtInviartFaultCode, "+
      ":r.codRtInviartFaultString, "+
      ":r.codRtInviartId, "+
      ":r.deRtInviartDescription, "+
      ":r.numRtInviartSerial, "+
      ":r.deRtVersioneOggetto, "+
      ":r.codRtDomIdDominio, "+
      ":r.codRtDomIdStazioneRichiedente, "+
      ":r.codRtIdMessaggioRicevuta, "+
      ":r.dtRtDataOraMessaggioRicevuta, "+
      ":r.codRtRiferimentoMessaggioRichiesta, "+
      ":r.dtRtRiferimentoDataRichiesta, "+
      ":r.codRtIstitAttesIdUnivAttesTipoIdUnivoco, "+
      ":r.codRtIstitAttesIdUnivAttesCodiceIdUnivoco, "+
      ":r.deRtIstitAttesDenominazioneAttestante, "+
      ":r.codRtIstitAttesCodiceUnitOperAttestante, "+
      ":r.deRtIstitAttesDenomUnitOperAttestante, "+
      ":r.deRtIstitAttesIndirizzoAttestante, "+
      ":r.deRtIstitAttesCivicoAttestante, "+
      ":r.codRtIstitAttesCapAttestante, "+
      ":r.deRtIstitAttesLocalitaAttestante, "+
      ":r.deRtIstitAttesProvinciaAttestante, "+
      ":r.codRtIstitAttesNazioneAttestante, "+
      ":r.codRtEnteBenefIdUnivBenefTipoIdUnivoco, "+
      ":r.codRtEnteBenefIdUnivBenefCodiceIdUnivoco, "+
      ":r.deRtEnteBenefDenominazioneBeneficiario, "+
      ":r.codRtEnteBenefCodiceUnitOperBeneficiario, "+
      ":r.deRtEnteBenefDenomUnitOperBeneficiario, "+
      ":r.deRtEnteBenefIndirizzoBeneficiario, "+
      ":r.deRtEnteBenefCivicoBeneficiario, "+
      ":r.codRtEnteBenefCapBeneficiario, "+
      ":r.deRtEnteBenefLocalitaBeneficiario, "+
      ":r.deRtEnteBenefProvinciaBeneficiario, "+
      ":r.codRtEnteBenefNazioneBeneficiario, "+
      ":r.codRtSoggVersIdUnivVersTipoIdUnivoco, "+
      ":r.codRtSoggVersIdUnivVersCodiceIdUnivoco, "+
      ":r.deRtSoggVersAnagraficaVersante, "+
      ":r.deRtSoggVersIndirizzoVersante, "+
      ":r.deRtSoggVersCivicoVersante, "+
      ":r.codRtSoggVersCapVersante, "+
      ":r.deRtSoggVersLocalitaVersante, "+
      ":r.deRtSoggVersProvinciaVersante, "+
      ":r.codRtSoggVersNazioneVersante, "+
      ":r.deRtSoggVersEmailVersante, "+
      ":r.codRtSoggPagIdUnivPagTipoIdUnivoco, "+
      ":r.codRtSoggPagIdUnivPagCodiceIdUnivoco, "+
      ":r.deRtSoggPagAnagraficaPagatore, "+
      ":r.deRtSoggPagIndirizzoPagatore, "+
      ":r.deRtSoggPagCivicoPagatore, "+
      ":r.codRtSoggPagCapPagatore, "+
      ":r.deRtSoggPagLocalitaPagatore, "+
      ":r.deRtSoggPagProvinciaPagatore, "+
      ":r.codRtSoggPagNazionePagatore, "+
      ":r.deRtSoggPagEmailPagatore, "+
      ":r.codRtDatiPagCodiceEsitoPagamento, "+
      ":r.numRtDatiPagImportoTotalePagato, "+
      ":r.codRtDatiPagIdUnivocoVersamento, "+
      ":r.codRtDatiPagCodiceContestoPagamento, "+
      ":r.idSession, "+
      ":r.mygovRpEId, "+
      ":r.blbRtPayload, "+
      ":r.modelloPagamento, "+
      ":r.codRptInviarptOriginalFaultCode, "+
      ":r.deRptInviarptOriginalFaultString, "+
      ":r.deRptInviarptOriginalFaultDescription, "+
      ":r.codRtInviartOriginalFaultCode, "+
      ":r.deRtInviartOriginalFaultString, "+
      ":r.deRtInviartOriginalFaultDescription );"
  )
  @GetGeneratedKeys("mygov_rpt_rt_id")
  Long insert(@BindBean("r") RptRt r);

  @SqlUpdate(
      "update mygov_rpt_rt " +
          "set version = :r.version" +
          ", mygov_carrello_rpt_id = :r.mygovCarrelloRptId?.mygovCarrelloRptId" +
          ", cod_ack_rt = :r.codAckRt" +
          ", dt_creazione_rpt = :r.dtCreazioneRpt" +
          ", dt_ultima_modifica_rpt = :r.dtUltimaModificaRpt" +
          ", dt_creazione_rt = :r.dtCreazioneRt" +
          ", dt_ultima_modifica_rt = :r.dtUltimaModificaRt" +
          ", de_rpt_inviarpt_password = :r.deRptInviarptPassword" +
          ", cod_rpt_inviarpt_id_psp = :r.codRptInviarptIdPsp" +
          ", cod_rpt_inviarpt_id_intermediario_psp = :r.codRptInviarptIdIntermediarioPsp" +
          ", cod_rpt_inviarpt_id_canale = :r.codRptInviarptIdCanale" +
          ", de_rpt_inviarpt_tipo_firma = :r.deRptInviarptTipoFirma" +
          ", cod_rpt_inviarpt_id_intermediario_pa = :r.codRptInviarptIdIntermediarioPa" +
          ", cod_rpt_inviarpt_id_stazione_intermediario_pa = :r.codRptInviarptIdStazioneIntermediarioPa" +
          ", cod_rpt_inviarpt_id_dominio = :r.codRptInviarptIdDominio" +
          ", cod_rpt_inviarpt_id_univoco_versamento = :r.codRptInviarptIdUnivocoVersamento" +
          ", cod_rpt_inviarpt_codice_contesto_pagamento = :r.codRptInviarptCodiceContestoPagamento" +
          ", de_rpt_inviarpt_esito = :r.deRptInviarptEsito" +
          ", num_rpt_inviarpt_redirect = :r.numRptInviarptRedirect" +
          ", cod_rpt_inviarpt_url = :r.codRptInviarptUrl" +
          ", cod_rpt_inviarpt_fault_code = :r.codRptInviarptFaultCode" +
          ", cod_rpt_inviarpt_fault_string = :r.codRptInviarptFaultString" +
          ", cod_rpt_inviarpt_id = :r.codRptInviarptId" +
          ", de_rpt_inviarpt_description = :r.deRptInviarptDescription" +
          ", num_rpt_inviarpt_serial = :r.numRptInviarptSerial" +
          ", de_rpt_versione_oggetto = :r.deRptVersioneOggetto" +
          ", cod_rpt_dom_id_dominio = :r.codRptDomIdDominio" +
          ", cod_rpt_dom_id_stazione_richiedente = :r.codRptDomIdStazioneRichiedente" +
          ", cod_rpt_id_messaggio_richiesta = :r.codRptIdMessaggioRichiesta" +
          ", dt_rpt_data_ora_messaggio_richiesta = :r.dtRptDataOraMessaggioRichiesta" +
          ", cod_rpt_autenticazione_soggetto = :r.codRptAutenticazioneSoggetto" +
          ", cod_rpt_sogg_vers_id_univ_vers_tipo_id_univoco = :r.codRptSoggVersIdUnivVersTipoIdUnivoco" +
          ", cod_rpt_sogg_vers_id_univ_vers_codice_id_univoco = :r.codRptSoggVersIdUnivVersCodiceIdUnivoco" +
          ", de_rpt_sogg_vers_anagrafica_versante = :r.deRptSoggVersAnagraficaVersante" +
          ", de_rpt_sogg_vers_indirizzo_versante = :r.deRptSoggVersIndirizzoVersante" +
          ", de_rpt_sogg_vers_civico_versante = :r.deRptSoggVersCivicoVersante" +
          ", cod_rpt_sogg_vers_cap_versante = :r.codRptSoggVersCapVersante" +
          ", de_rpt_sogg_vers_localita_versante = left(:r.deRptSoggVersLocalitaVersante,"+RptRt.MAX_LENGTH_LOCALITA+")" +
          ", de_rpt_sogg_vers_provincia_versante = :r.deRptSoggVersProvinciaVersante" +
          ", cod_rpt_sogg_vers_nazione_versante = :r.codRptSoggVersNazioneVersante" +
          ", de_rpt_sogg_vers_email_versante = :r.deRptSoggVersEmailVersante" +
          ", cod_rpt_sogg_pag_id_univ_pag_tipo_id_univoco = :r.codRptSoggPagIdUnivPagTipoIdUnivoco" +
          ", cod_rpt_sogg_pag_id_univ_pag_codice_id_univoco = :r.codRptSoggPagIdUnivPagCodiceIdUnivoco" +
          ", de_rpt_sogg_pag_anagrafica_pagatore = :r.deRptSoggPagAnagraficaPagatore" +
          ", de_rpt_sogg_pag_indirizzo_pagatore = :r.deRptSoggPagIndirizzoPagatore" +
          ", de_rpt_sogg_pag_civico_pagatore = :r.deRptSoggPagCivicoPagatore" +
          ", cod_rpt_sogg_pag_cap_pagatore = :r.codRptSoggPagCapPagatore" +
          ", de_rpt_sogg_pag_localita_pagatore = left(:r.deRptSoggPagLocalitaPagatore,"+RptRt.MAX_LENGTH_LOCALITA+")" +
          ", de_rpt_sogg_pag_provincia_pagatore = :r.deRptSoggPagProvinciaPagatore" +
          ", cod_rpt_sogg_pag_nazione_pagatore = :r.codRptSoggPagNazionePagatore" +
          ", de_rpt_sogg_pag_email_pagatore = :r.deRptSoggPagEmailPagatore" +
          ", cod_rpt_ente_benef_id_univ_benef_tipo_id_univoco = :r.codRptEnteBenefIdUnivBenefTipoIdUnivoco" +
          ", cod_rpt_ente_benef_id_univ_benef_codice_id_univoco = :r.codRptEnteBenefIdUnivBenefCodiceIdUnivoco" +
          ", de_rpt_ente_benef_denominazione_beneficiario = :r.deRptEnteBenefDenominazioneBeneficiario" +
          ", cod_rpt_ente_benef_codice_unit_oper_beneficiario = :r.codRptEnteBenefCodiceUnitOperBeneficiario" +
          ", de_rpt_ente_benef_denom_unit_oper_beneficiario = :r.deRptEnteBenefDenomUnitOperBeneficiario" +
          ", de_rpt_ente_benef_indirizzo_beneficiario = :r.deRptEnteBenefIndirizzoBeneficiario" +
          ", de_rpt_ente_benef_civico_beneficiario = :r.deRptEnteBenefCivicoBeneficiario" +
          ", cod_rpt_ente_benef_cap_beneficiario = :r.codRptEnteBenefCapBeneficiario" +
          ", de_rpt_ente_benef_localita_beneficiario = :r.deRptEnteBenefLocalitaBeneficiario" +
          ", de_rpt_ente_benef_provincia_beneficiario = :r.deRptEnteBenefProvinciaBeneficiario" +
          ", cod_rpt_ente_benef_nazione_beneficiario = :r.codRptEnteBenefNazioneBeneficiario" +
          ", dt_rpt_dati_vers_data_esecuzione_pagamento = :r.dtRptDatiVersDataEsecuzionePagamento" +
          ", num_rpt_dati_vers_importo_totale_da_versare = :r.numRptDatiVersImportoTotaleDaVersare" +
          ", cod_rpt_dati_vers_tipo_versamento = :r.codRptDatiVersTipoVersamento" +
          ", cod_rpt_dati_vers_id_univoco_versamento = :r.codRptDatiVersIdUnivocoVersamento" +
          ", cod_rpt_dati_vers_codice_contesto_pagamento = :r.codRptDatiVersCodiceContestoPagamento" +
          ", de_rpt_dati_vers_iban_addebito = :r.deRptDatiVersIbanAddebito" +
          ", de_rpt_dati_vers_bic_addebito = :r.deRptDatiVersBicAddebito" +
          ", cod_rpt_dati_vers_firma_ricevuta = :r.codRptDatiVersFirmaRicevuta" +
          ", de_rt_inviart_tipo_firma = :r.deRtInviartTipoFirma" +
          ", cod_rt_inviart_id_intermediario_pa = :r.codRtInviartIdIntermediarioPa" +
          ", cod_rt_inviart_id_stazione_intermediario_pa = :r.codRtInviartIdStazioneIntermediarioPa" +
          ", cod_rt_inviart_id_dominio = :r.codRtInviartIdDominio" +
          ", cod_rt_inviart_id_univoco_versamento = :r.codRtInviartIdUnivocoVersamento" +
          ", cod_rt_inviart_codice_contesto_pagamento = :r.codRtInviartCodiceContestoPagamento" +
          ", cod_rt_inviart_esito = :r.codRtInviartEsito" +
          ", cod_rt_inviart_fault_code = :r.codRtInviartFaultCode" +
          ", cod_rt_inviart_fault_string = :r.codRtInviartFaultString" +
          ", cod_rt_inviart_id = :r.codRtInviartId" +
          ", de_rt_inviart_description = :r.deRtInviartDescription" +
          ", num_rt_inviart_serial = :r.numRtInviartSerial" +
          ", de_rt_versione_oggetto = :r.deRtVersioneOggetto" +
          ", cod_rt_dom_id_dominio = :r.codRtDomIdDominio" +
          ", cod_rt_dom_id_stazione_richiedente = :r.codRtDomIdStazioneRichiedente" +
          ", cod_rt_id_messaggio_ricevuta = :r.codRtIdMessaggioRicevuta" +
          ", dt_rt_data_ora_messaggio_ricevuta = :r.dtRtDataOraMessaggioRicevuta" +
          ", cod_rt_riferimento_messaggio_richiesta = :r.codRtRiferimentoMessaggioRichiesta" +
          ", dt_rt_riferimento_data_richiesta = :r.dtRtRiferimentoDataRichiesta" +
          ", cod_rt_istit_attes_id_univ_attes_tipo_id_univoco = :r.codRtIstitAttesIdUnivAttesTipoIdUnivoco" +
          ", cod_rt_istit_attes_id_univ_attes_codice_id_univoco = :r.codRtIstitAttesIdUnivAttesCodiceIdUnivoco" +
          ", de_rt_istit_attes_denominazione_attestante = :r.deRtIstitAttesDenominazioneAttestante" +
          ", cod_rt_istit_attes_codice_unit_oper_attestante = :r.codRtIstitAttesCodiceUnitOperAttestante" +
          ", de_rt_istit_attes_denom_unit_oper_attestante = :r.deRtIstitAttesDenomUnitOperAttestante" +
          ", de_rt_istit_attes_indirizzo_attestante = :r.deRtIstitAttesIndirizzoAttestante" +
          ", de_rt_istit_attes_civico_attestante = :r.deRtIstitAttesCivicoAttestante" +
          ", cod_rt_istit_attes_cap_attestante = :r.codRtIstitAttesCapAttestante" +
          ", de_rt_istit_attes_localita_attestante = :r.deRtIstitAttesLocalitaAttestante" +
          ", de_rt_istit_attes_provincia_attestante = :r.deRtIstitAttesProvinciaAttestante" +
          ", cod_rt_istit_attes_nazione_attestante = :r.codRtIstitAttesNazioneAttestante" +
          ", cod_rt_ente_benef_id_univ_benef_tipo_id_univoco = :r.codRtEnteBenefIdUnivBenefTipoIdUnivoco" +
          ", cod_rt_ente_benef_id_univ_benef_codice_id_univoco = :r.codRtEnteBenefIdUnivBenefCodiceIdUnivoco" +
          ", de_rt_ente_benef_denominazione_beneficiario = :r.deRtEnteBenefDenominazioneBeneficiario" +
          ", cod_rt_ente_benef_codice_unit_oper_beneficiario = :r.codRtEnteBenefCodiceUnitOperBeneficiario" +
          ", de_rt_ente_benef_denom_unit_oper_beneficiario = :r.deRtEnteBenefDenomUnitOperBeneficiario" +
          ", de_rt_ente_benef_indirizzo_beneficiario = :r.deRtEnteBenefIndirizzoBeneficiario" +
          ", de_rt_ente_benef_civico_beneficiario = :r.deRtEnteBenefCivicoBeneficiario" +
          ", cod_rt_ente_benef_cap_beneficiario = :r.codRtEnteBenefCapBeneficiario" +
          ", de_rt_ente_benef_localita_beneficiario = :r.deRtEnteBenefLocalitaBeneficiario" +
          ", de_rt_ente_benef_provincia_beneficiario = :r.deRtEnteBenefProvinciaBeneficiario" +
          ", cod_rt_ente_benef_nazione_beneficiario = :r.codRtEnteBenefNazioneBeneficiario" +
          ", cod_rt_sogg_vers_id_univ_vers_tipo_id_univoco = :r.codRtSoggVersIdUnivVersTipoIdUnivoco" +
          ", cod_rt_sogg_vers_id_univ_vers_codice_id_univoco = :r.codRtSoggVersIdUnivVersCodiceIdUnivoco" +
          ", de_rt_sogg_vers_anagrafica_versante = :r.deRtSoggVersAnagraficaVersante" +
          ", de_rt_sogg_vers_indirizzo_versante = :r.deRtSoggVersIndirizzoVersante" +
          ", de_rt_sogg_vers_civico_versante = :r.deRtSoggVersCivicoVersante" +
          ", cod_rt_sogg_vers_cap_versante = :r.codRtSoggVersCapVersante" +
          ", de_rt_sogg_vers_localita_versante = :r.deRtSoggVersLocalitaVersante" +
          ", de_rt_sogg_vers_provincia_versante = :r.deRtSoggVersProvinciaVersante" +
          ", cod_rt_sogg_vers_nazione_versante = :r.codRtSoggVersNazioneVersante" +
          ", de_rt_sogg_vers_email_versante = :r.deRtSoggVersEmailVersante" +
          ", cod_rt_sogg_pag_id_univ_pag_tipo_id_univoco = :r.codRtSoggPagIdUnivPagTipoIdUnivoco" +
          ", cod_rt_sogg_pag_id_univ_pag_codice_id_univoco = :r.codRtSoggPagIdUnivPagCodiceIdUnivoco" +
          ", de_rt_sogg_pag_anagrafica_pagatore = :r.deRtSoggPagAnagraficaPagatore" +
          ", de_rt_sogg_pag_indirizzo_pagatore = :r.deRtSoggPagIndirizzoPagatore" +
          ", de_rt_sogg_pag_civico_pagatore = :r.deRtSoggPagCivicoPagatore" +
          ", cod_rt_sogg_pag_cap_pagatore = :r.codRtSoggPagCapPagatore" +
          ", de_rt_sogg_pag_localita_pagatore = :r.deRtSoggPagLocalitaPagatore" +
          ", de_rt_sogg_pag_provincia_pagatore = :r.deRtSoggPagProvinciaPagatore" +
          ", cod_rt_sogg_pag_nazione_pagatore = :r.codRtSoggPagNazionePagatore" +
          ", de_rt_sogg_pag_email_pagatore = :r.deRtSoggPagEmailPagatore" +
          ", cod_rt_dati_pag_codice_esito_pagamento = :r.codRtDatiPagCodiceEsitoPagamento" +
          ", num_rt_dati_pag_importo_totale_pagato = :r.numRtDatiPagImportoTotalePagato" +
          ", cod_rt_dati_pag_id_univoco_versamento = :r.codRtDatiPagIdUnivocoVersamento" +
          ", cod_rt_dati_pag_codice_contesto_pagamento = :r.codRtDatiPagCodiceContestoPagamento" +
          ", id_session = :r.idSession" +
          ", mygov_rp_e_id = :r.mygovRpEId" +
          ", blb_rt_payload = :r.blbRtPayload" +
          ", modello_pagamento = :r.modelloPagamento" +
          ", cod_rpt_inviarpt_original_fault_code = :r.codRptInviarptOriginalFaultCode" +
          ", de_rpt_inviarpt_original_fault_string = :r.deRptInviarptOriginalFaultString" +
          ", de_rpt_inviarpt_original_fault_description = :r.deRptInviarptOriginalFaultDescription" +
          ", cod_rt_inviart_original_fault_code = :r.codRtInviartOriginalFaultCode" +
          ", de_rt_inviart_original_fault_string = :r.deRtInviartOriginalFaultString" +
          ", de_rt_inviart_original_fault_description = :r.deRtInviartOriginalFaultDescription" +
          " where mygov_rpt_rt_id = :r.mygovRptRtId"
  )
  int update(@BindBean("r") RptRt r);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS+","+ CarrelloRpt.FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " left join mygov_carrello_rpt "+CarrelloRpt.ALIAS+ " on "+CarrelloRpt.ALIAS+".mygov_carrello_rpt_id = "+RptRt.ALIAS+".mygov_carrello_rpt_id" +
          " where "+RptRt.ALIAS+".mygov_rpt_rt_id = :mygovRptRtId"
  )
  @RegisterFieldMapper(RptRt.class)
  RptRt getById(Long mygovRptRtId);

  @SqlQuery(
    "select "+RptRt.ALIAS+ALL_FIELDS +
      " from mygov_rpt_rt "+RptRt.ALIAS +
      " where "+RptRt.ALIAS+".mygov_rpt_rt_id = :mygovRptRtId " +
      " for update skip locked"
  )
  @RegisterFieldMapper(RptRt.class)
  Optional<RptRt> getByIdLockOrSkip(Long mygovRptRtId);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS+","+ CarrelloRpt.FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " left join mygov_carrello_rpt "+CarrelloRpt.ALIAS+ " on "+CarrelloRpt.ALIAS+".mygov_carrello_rpt_id = "+RptRt.ALIAS+".mygov_carrello_rpt_id" +
          " where "+RptRt.ALIAS+".cod_rpt_inviarpt_id_dominio = :idDominio" +
          "   and "+RptRt.ALIAS+".cod_rpt_inviarpt_id_univoco_versamento = :idUnivocoVersamento" +
          "   and "+RptRt.ALIAS+".cod_rpt_inviarpt_codice_contesto_pagamento = :codiceContestoPagamento"
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getRPTByIdDominioIuvCdContestoPagamento(String idDominio, String idUnivocoVersamento, String codiceContestoPagamento);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " where "+RptRt.ALIAS+".de_rpt_inviarpt_esito is null" +
          "   and "+RptRt.ALIAS+".modello_pagamento in (2, 4)" +
          "   and "+RptRt.ALIAS+".dt_ultima_modifica_rpt < now() - interval '5 seconds' " +
          " order by "+RptRt.ALIAS+".mygov_rpt_rt_id ASC " +
          " limit <queryLimit> " +
          " for update skip locked"
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getRPTAttivateForInviaRPT(@Define int queryLimit);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " where "+RptRt.ALIAS+".de_rpt_inviarpt_esito is null" +
          "   and "+RptRt.ALIAS+".modello_pagamento in (2, 4)" +
          "   and "+RptRt.ALIAS+".dt_ultima_modifica_rpt < now() - interval '5 seconds' " +
          " order by "+RptRt.ALIAS+".mygov_rpt_rt_id ASC " +
          " limit <queryLimit> "
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getRPTAttivateForInviaRPTNoLock(@Define int queryLimit);

  @SqlQuery(
      "select mygov_rpt_rt_id, dt_rpt_data_ora_messaggio_richiesta, dt_creazione_rpt " +
          " , cod_rpt_inviarpt_id_dominio, cod_rpt_inviarpt_id_univoco_versamento, cod_rpt_inviarpt_codice_contesto_pagamento " +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " where "+RptRt.ALIAS+".modello_pagamento = :modelloPagamento " +
          "   and "+RptRt.ALIAS+".cod_rt_inviart_esito is null " +
          "   and "+RptRt.ALIAS+".cod_rt_dati_pag_codice_esito_pagamento is null " +
          "   and ("+RptRt.ALIAS+".de_rpt_inviarpt_esito = 'KO' " +
          "    or  "+RptRt.ALIAS+".de_rpt_inviarpt_esito = 'OK' " +
          "   and  "+RptRt.ALIAS+".dt_rpt_data_ora_messaggio_richiesta < now() - make_interval(0,0,0,0,0,:deltaMinutes,0) ) " +
          "   and ("+RptRt.ALIAS+".cod_rpt_inviarpt_original_fault_code != '"+REMOVE_FROM_SCOPE_CHIEDI_STATO_RPT+"' " +
          "     or "+RptRt.ALIAS+".cod_rpt_inviarpt_original_fault_code is null ) " +
          " order by "+RptRt.ALIAS+".dt_rpt_data_ora_messaggio_richiesta DESC "
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getRPTPendentiForChiediStatoRPT(int modelloPagamento, int deltaMinutes);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " where "+RptRt.ALIAS+".cod_rpt_inviarpt_id_dominio = :idDominio" +
          "   and "+RptRt.ALIAS+".cod_rpt_inviarpt_id_univoco_versamento = :idUnivocoVersamento" +
          "   and "+RptRt.ALIAS+".cod_rpt_inviarpt_codice_contesto_pagamento = :codiceContestoPagamento" +
          "   and "+RptRt.ALIAS+".de_rpt_inviarpt_esito is null" +
          "   and "+RptRt.ALIAS+".modello_pagamento in (2, 4)" +
          " for update skip locked"
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getRPTByIdDominioIuvCdContestoPagamentoForInviaRPT(String idDominio, String idUnivocoVersamento, String codiceContestoPagamento);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " where "+RptRt.ALIAS+".cod_rpt_inviarpt_id_dominio = :idDominio" +
          "   and "+RptRt.ALIAS+".cod_rpt_inviarpt_id_univoco_versamento = :idUnivocoVersamento" +
          "   and "+RptRt.ALIAS+".cod_rpt_inviarpt_codice_contesto_pagamento = :codiceContestoPagamento" +
          "   and "+RptRt.ALIAS+".de_rpt_inviarpt_esito is null" +
          "   and "+RptRt.ALIAS+".modello_pagamento in (2, 4)"
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getRPTByIdDominioIuvCdContestoPagamentoForInviaRPTNoLock(String idDominio, String idUnivocoVersamento, String codiceContestoPagamento);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS+","+ CarrelloRpt.FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " left join mygov_carrello_rpt "+CarrelloRpt.ALIAS+ " on "+CarrelloRpt.ALIAS+".mygov_carrello_rpt_id = "+RptRt.ALIAS+".mygov_carrello_rpt_id" +
          " where "+RptRt.ALIAS+".cod_rt_inviart_id_dominio = :idDominio" +
          "   and "+RptRt.ALIAS+".cod_rt_inviart_id_univoco_versamento = :idUnivocoVersamento" +
          "   and "+RptRt.ALIAS+".cod_rt_inviart_codice_contesto_pagamento = :codiceContestoPagamento"
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getRTByIdDominioIuvCdContestoPagamento(String idDominio, String idUnivocoVersamento, String codiceContestoPagamento);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS+","+ CarrelloRpt.FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " left join mygov_carrello_rpt "+CarrelloRpt.ALIAS+ " on "+CarrelloRpt.ALIAS+".mygov_carrello_rpt_id = "+RptRt.ALIAS+".mygov_carrello_rpt_id" +
          " where "+RptRt.ALIAS+".cod_rpt_id_messaggio_richiesta = :riferimentoMessaggioRichiesta"
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getRPTByCodRptIdMessaggioRichiesta(String riferimentoMessaggioRichiesta);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS+","+ CarrelloRpt.FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " left join mygov_carrello_rpt "+CarrelloRpt.ALIAS+ " on "+CarrelloRpt.ALIAS+".mygov_carrello_rpt_id = "+RptRt.ALIAS+".mygov_carrello_rpt_id" +
          " where "+CarrelloRpt.ALIAS+".mygov_carrello_rpt_id = :mygovCarrelloRptId"
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getByCart(Long mygovCarrelloRptId);

  @SqlQuery(
      "select "+RptRt.ALIAS+ALL_FIELDS+","+ CarrelloRpt.FIELDS +
          " from mygov_rpt_rt "+RptRt.ALIAS +
          " left join mygov_carrello_rpt "+CarrelloRpt.ALIAS+ " on "+CarrelloRpt.ALIAS+".mygov_carrello_rpt_id = "+RptRt.ALIAS+".mygov_carrello_rpt_id" +
          " where "+RptRt.ALIAS+".id_session = :idSession"
  )
  @RegisterFieldMapper(RptRt.class)
  List<RptRt> getByIdSession(String idSession);
}
