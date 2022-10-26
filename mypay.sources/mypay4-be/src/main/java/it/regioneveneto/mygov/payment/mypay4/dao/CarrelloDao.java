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

import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.CarrelloMultiBeneficiario;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface CarrelloDao extends BaseDao{
  @SqlUpdate("update mygov_carrello" +
      " set version = :c.version"+
      " , mygov_anagrafica_stato_id = :c.mygovAnagraficaStatoId.mygovAnagraficaStatoId"+
      " , cod_ack_rp = :c.codAckRp"+
      " , dt_creazione = :c.dtCreazione"+
      " , dt_ultima_modifica_rp = :c.dtUltimaModificaRp"+
      " , dt_ultima_modifica_e = :c.dtUltimaModificaE"+
      " , cod_rp_silinviarp_id_psp = :c.codRpSilinviarpIdPsp"+
      " , cod_rp_silinviarp_id_intermediario_psp = :c.codRpSilinviarpIdIntermediarioPsp"+
      " , cod_rp_silinviarp_id_canale = :c.codRpSilinviarpIdCanale"+
      " , cod_rp_silinviarp_id_dominio = :c.codRpSilinviarpIdDominio"+
      " , cod_rp_silinviarp_id_univoco_versamento = :c.codRpSilinviarpIdUnivocoVersamento"+
      " , cod_rp_silinviarp_codice_contesto_pagamento = :c.codRpSilinviarpCodiceContestoPagamento"+
      " , de_rp_silinviarp_esito = :c.deRpSilinviarpEsito"+
      " , cod_rp_silinviarp_redirect = :c.codRpSilinviarpRedirect"+
      " , cod_rp_silinviarp_url = :c.codRpSilinviarpUrl"+
      " , cod_rp_silinviarp_fault_code = :c.codRpSilinviarpFaultCode"+
      " , de_rp_silinviarp_fault_string = :c.deRpSilinviarpFaultString"+
      " , cod_rp_silinviarp_id = :c.codRpSilinviarpId"+
      " , de_rp_silinviarp_description = :c.deRpSilinviarpDescription"+
      " , cod_rp_silinviarp_serial = :c.codRpSilinviarpSerial"+
      " , de_rp_versione_oggetto = :c.deRpVersioneOggetto"+
      " , cod_rp_dom_id_dominio = :c.codRpDomIdDominio"+
      " , cod_rp_dom_id_stazione_richiedente = :c.codRpDomIdStazioneRichiedente"+
      " , cod_rp_id_messaggio_richiesta = :c.codRpIdMessaggioRichiesta"+
      " , dt_rp_data_ora_messaggio_richiesta = :c.dtRpDataOraMessaggioRichiesta"+
      " , cod_rp_autenticazione_soggetto = :c.codRpAutenticazioneSoggetto"+
      " , cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco = :c.codRpSoggVersIdUnivVersTipoIdUnivoco"+
      " , cod_rp_sogg_vers_id_univ_vers_codice_id_univoco = :c.codRpSoggVersIdUnivVersCodiceIdUnivoco"+
      " , cod_rp_sogg_vers_anagrafica_versante = :c.codRpSoggVersAnagraficaVersante"+
      " , de_rp_sogg_vers_indirizzo_versante = :c.deRpSoggVersIndirizzoVersante"+
      " , de_rp_sogg_vers_civico_versante = :c.deRpSoggVersCivicoVersante"+
      " , cod_rp_sogg_vers_cap_versante = :c.codRpSoggVersCapVersante"+
      " , de_rp_sogg_vers_localita_versante = left(:c.deRpSoggVersLocalitaVersante,"+Dovuto.MAX_LENGTH_LOCALITA+")"+
      " , de_rp_sogg_vers_provincia_versante = :c.deRpSoggVersProvinciaVersante"+
      " , cod_rp_sogg_vers_nazione_versante = :c.codRpSoggVersNazioneVersante"+
      " , de_rp_sogg_vers_email_versante = :c.deRpSoggVersEmailVersante"+
      " , cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :c.codRpSoggPagIdUnivPagTipoIdUnivoco"+
      " , cod_rp_sogg_pag_id_univ_pag_codice_id_univoco = :c.codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      " , de_rp_sogg_pag_anagrafica_pagatore = :c.deRpSoggPagAnagraficaPagatore"+
      " , de_rp_sogg_pag_indirizzo_pagatore = :c.deRpSoggPagIndirizzoPagatore"+
      " , de_rp_sogg_pag_civico_pagatore = :c.deRpSoggPagCivicoPagatore"+
      " , cod_rp_sogg_pag_cap_pagatore = :c.codRpSoggPagCapPagatore"+
      " , de_rp_sogg_pag_localita_pagatore = left(:c.deRpSoggPagLocalitaPagatore,"+Dovuto.MAX_LENGTH_LOCALITA+")"+
      " , de_rp_sogg_pag_provincia_pagatore = :c.deRpSoggPagProvinciaPagatore"+
      " , cod_rp_sogg_pag_nazione_pagatore = :c.codRpSoggPagNazionePagatore"+
      " , de_rp_sogg_pag_email_pagatore = :c.deRpSoggPagEmailPagatore"+
      " , dt_rp_dati_vers_data_esecuzione_pagamento = :c.dtRpDatiVersDataEsecuzionePagamento"+
      " , num_rp_dati_vers_importo_totale_da_versare = :c.numRpDatiVersImportoTotaleDaVersare"+
      " , cod_rp_dati_vers_tipo_versamento = :c.codRpDatiVersTipoVersamento"+
      " , cod_rp_dati_vers_id_univoco_versamento = :c.codRpDatiVersIdUnivocoVersamento"+
      " , cod_rp_dati_vers_codice_contesto_pagamento = :c.codRpDatiVersCodiceContestoPagamento"+
      " , de_rp_dati_vers_iban_addebito = :c.deRpDatiVersIbanAddebito"+
      " , de_rp_dati_vers_bic_addebito = :c.deRpDatiVersBicAddebito"+
      " , cod_e_silinviaesito_id_dominio = :c.codESilinviaesitoIdDominio"+
      " , cod_e_silinviaesito_id_univoco_versamento = :c.codESilinviaesitoIdUnivocoVersamento"+
      " , cod_e_silinviaesito_codice_contesto_pagamento = :c.codESilinviaesitoCodiceContestoPagamento"+
      " , de_e_silinviaesito_esito = :c.deESilinviaesitoEsito"+
      " , cod_e_silinviaesito_fault_code = :c.codESilinviaesitoFaultCode"+
      " , de_e_silinviaesito_fault_string = :c.deESilinviaesitoFaultString"+
      " , cod_e_silinviaesito_id = :c.codESilinviaesitoId"+
      " , de_e_silinviaesito_description = :c.deESilinviaesitoDescription"+
      " , cod_e_silinviaesito_serial = :c.codESilinviaesitoSerial"+
      " , de_e_versione_oggetto = :c.deEVersioneOggetto"+
      " , cod_e_dom_id_dominio = :c.codEDomIdDominio"+
      " , cod_e_dom_id_stazione_richiedente = :c.codEDomIdStazioneRichiedente"+
      " , cod_e_id_messaggio_ricevuta = :c.codEIdMessaggioRicevuta"+
      " , cod_e_data_ora_messaggio_ricevuta = :c.codEDataOraMessaggioRicevuta"+
      " , cod_e_riferimento_messaggio_richiesta = :c.codERiferimentoMessaggioRichiesta"+
      " , cod_e_riferimento_data_richiesta = :c.codERiferimentoDataRichiesta"+
      " , cod_e_istit_att_id_univ_att_tipo_id_univoco = :c.codEIstitAttIdUnivAttTipoIdUnivoco"+
      " , cod_e_istit_att_id_univ_att_codice_id_univoco = :c.codEIstitAttIdUnivAttCodiceIdUnivoco"+
      " , de_e_istit_att_denominazione_attestante = :c.deEIstitAttDenominazioneAttestante"+
      " , cod_e_istit_att_codice_unit_oper_attestante = :c.codEIstitAttCodiceUnitOperAttestante"+
      " , de_e_istit_att_denom_unit_oper_attestante = :c.deEIstitAttDenomUnitOperAttestante"+
      " , de_e_istit_att_indirizzo_attestante = :c.deEIstitAttIndirizzoAttestante"+
      " , de_e_istit_att_civico_attestante = :c.deEIstitAttCivicoAttestante"+
      " , cod_e_istit_att_cap_attestante = :c.codEIstitAttCapAttestante"+
      " , de_e_istit_att_localita_attestante = :c.deEIstitAttLocalitaAttestante"+
      " , de_e_istit_att_provincia_attestante = :c.deEIstitAttProvinciaAttestante"+
      " , cod_e_istit_att_nazione_attestante = :c.codEIstitAttNazioneAttestante"+
      " , cod_e_ente_benef_id_univ_benef_tipo_id_univoco = :c.codEEnteBenefIdUnivBenefTipoIdUnivoco"+
      " , cod_e_ente_benef_id_univ_benef_codice_id_univoco = :c.codEEnteBenefIdUnivBenefCodiceIdUnivoco"+
      " , de_e_ente_benef_denominazione_beneficiario = :c.deEEnteBenefDenominazioneBeneficiario"+
      " , cod_e_ente_benef_codice_unit_oper_beneficiario = :c.codEEnteBenefCodiceUnitOperBeneficiario"+
      " , de_e_ente_benef_denom_unit_oper_beneficiario = :c.deEEnteBenefDenomUnitOperBeneficiario"+
      " , de_e_ente_benef_indirizzo_beneficiario = :c.deEEnteBenefIndirizzoBeneficiario"+
      " , de_e_ente_benef_civico_beneficiario = :c.deEEnteBenefCivicoBeneficiario"+
      " , cod_e_ente_benef_cap_beneficiario = :c.codEEnteBenefCapBeneficiario"+
      " , de_e_ente_benef_localita_beneficiario = :c.deEEnteBenefLocalitaBeneficiario"+
      " , de_e_ente_benef_provincia_beneficiario = :c.deEEnteBenefProvinciaBeneficiario"+
      " , cod_e_ente_benef_nazione_beneficiario = :c.codEEnteBenefNazioneBeneficiario"+
      " , cod_e_sogg_vers_id_univ_vers_tipo_id_univoco = :c.codESoggVersIdUnivVersTipoIdUnivoco"+
      " , cod_e_sogg_vers_id_univ_vers_codice_id_univoco = :c.codESoggVersIdUnivVersCodiceIdUnivoco"+
      " , cod_e_sogg_vers_anagrafica_versante = :c.codESoggVersAnagraficaVersante"+
      " , de_e_sogg_vers_indirizzo_versante = :c.deESoggVersIndirizzoVersante"+
      " , de_e_sogg_vers_civico_versante = :c.deESoggVersCivicoVersante"+
      " , cod_e_sogg_vers_cap_versante = :c.codESoggVersCapVersante"+
      " , de_e_sogg_vers_localita_versante = :c.deESoggVersLocalitaVersante"+
      " , de_e_sogg_vers_provincia_versante = :c.deESoggVersProvinciaVersante"+
      " , cod_e_sogg_vers_nazione_versante = :c.codESoggVersNazioneVersante"+
      " , de_e_sogg_vers_email_versante = :c.deESoggVersEmailVersante"+
      " , cod_e_sogg_pag_id_univ_pag_tipo_id_univoco = :c.codESoggPagIdUnivPagTipoIdUnivoco"+
      " , cod_e_sogg_pag_id_univ_pag_codice_id_univoco = :c.codESoggPagIdUnivPagCodiceIdUnivoco"+
      " , cod_e_sogg_pag_anagrafica_pagatore = :c.codESoggPagAnagraficaPagatore"+
      " , de_e_sogg_pag_indirizzo_pagatore = :c.deESoggPagIndirizzoPagatore"+
      " , de_e_sogg_pag_civico_pagatore = :c.deESoggPagCivicoPagatore"+
      " , cod_e_sogg_pag_cap_pagatore = :c.codESoggPagCapPagatore"+
      " , de_e_sogg_pag_localita_pagatore = :c.deESoggPagLocalitaPagatore"+
      " , de_e_sogg_pag_provincia_pagatore = :c.deESoggPagProvinciaPagatore"+
      " , cod_e_sogg_pag_nazione_pagatore = :c.codESoggPagNazionePagatore"+
      " , de_e_sogg_pag_email_pagatore = :c.deESoggPagEmailPagatore"+
      " , cod_e_dati_pag_codice_esito_pagamento = :c.codEDatiPagCodiceEsitoPagamento"+
      " , num_e_dati_pag_importo_totale_pagato = :c.numEDatiPagImportoTotalePagato"+
      " , cod_e_dati_pag_id_univoco_versamento = :c.codEDatiPagIdUnivocoVersamento"+
      " , cod_e_dati_pag_codice_contesto_pagamento = :c.codEDatiPagCodiceContestoPagamento"+
      " , id_session = :c.idSession"+
      " , id_session_fesp = :c.idSessionFesp"+
      " , tipo_carrello = :c.tipoCarrello"+
      " , modello_pagamento = :c.modelloPagamento"+
      " , ente_sil_invia_risposta_pagamento_url = :c.enteSilInviaRispostaPagamentoUrl"+
      " , flg_notifica_esito = :c.flgNotificaEsito"+
      " , mygov_carrello_multi_beneficiario_id = :c.mygovCarrelloMultiBeneficiarioId?.mygovCarrelloMultiBeneficiarioId"+
      " , cod_rp_silinviarp_original_fault_code = :c.codRpSilinviarpOriginalFaultCode"+
      " , de_rp_silinviarp_original_fault_string = :c.deRpSilinviarpOriginalFaultString"+
      " , de_rp_silinviarp_original_fault_description = :c.deRpSilinviarpOriginalFaultDescription"+
      " , cod_e_silinviaesito_original_fault_code = :c.codESilinviaesitoOriginalFaultCode"+
      " , de_e_silinviaesito_original_fault_string = :c.deESilinviaesitoOriginalFaultString"+
      " , de_e_silinviaesito_original_fault_description = :c.deESilinviaesitoOriginalFaultDescription" +
      " where mygov_carrello_id = :c.mygovCarrelloId"
  )
  int update(@BindBean("c") Carrello c);

  @SqlUpdate(" insert into mygov_carrello (" +
      "   mygov_carrello_id"+
      " , version"+
      " , mygov_anagrafica_stato_id"+
      " , cod_ack_rp"+
      " , dt_creazione"+
      " , dt_ultima_modifica_rp"+
      " , dt_ultima_modifica_e"+
      " , cod_rp_silinviarp_id_psp"+
      " , cod_rp_silinviarp_id_intermediario_psp"+
      " , cod_rp_silinviarp_id_canale"+
      " , cod_rp_silinviarp_id_dominio"+
      " , cod_rp_silinviarp_id_univoco_versamento"+
      " , cod_rp_silinviarp_codice_contesto_pagamento"+
      " , de_rp_silinviarp_esito"+
      " , cod_rp_silinviarp_redirect"+
      " , cod_rp_silinviarp_url"+
      " , cod_rp_silinviarp_fault_code"+
      " , de_rp_silinviarp_fault_string"+
      " , cod_rp_silinviarp_id"+
      " , de_rp_silinviarp_description"+
      " , cod_rp_silinviarp_serial"+
      " , de_rp_versione_oggetto"+
      " , cod_rp_dom_id_dominio"+
      " , cod_rp_dom_id_stazione_richiedente"+
      " , cod_rp_id_messaggio_richiesta"+
      " , dt_rp_data_ora_messaggio_richiesta"+
      " , cod_rp_autenticazione_soggetto"+
      " , cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco"+
      " , cod_rp_sogg_vers_id_univ_vers_codice_id_univoco"+
      " , cod_rp_sogg_vers_anagrafica_versante"+
      " , de_rp_sogg_vers_indirizzo_versante"+
      " , de_rp_sogg_vers_civico_versante"+
      " , cod_rp_sogg_vers_cap_versante"+
      " , de_rp_sogg_vers_localita_versante"+
      " , de_rp_sogg_vers_provincia_versante"+
      " , cod_rp_sogg_vers_nazione_versante"+
      " , de_rp_sogg_vers_email_versante"+
      " , cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco"+
      " , cod_rp_sogg_pag_id_univ_pag_codice_id_univoco"+
      " , de_rp_sogg_pag_anagrafica_pagatore"+
      " , de_rp_sogg_pag_indirizzo_pagatore"+
      " , de_rp_sogg_pag_civico_pagatore"+
      " , cod_rp_sogg_pag_cap_pagatore"+
      " , de_rp_sogg_pag_localita_pagatore"+
      " , de_rp_sogg_pag_provincia_pagatore"+
      " , cod_rp_sogg_pag_nazione_pagatore"+
      " , de_rp_sogg_pag_email_pagatore"+
      " , dt_rp_dati_vers_data_esecuzione_pagamento"+
      " , num_rp_dati_vers_importo_totale_da_versare"+
      " , cod_rp_dati_vers_tipo_versamento"+
      " , cod_rp_dati_vers_id_univoco_versamento"+
      " , cod_rp_dati_vers_codice_contesto_pagamento"+
      " , de_rp_dati_vers_iban_addebito"+
      " , de_rp_dati_vers_bic_addebito"+
      " , cod_e_silinviaesito_id_dominio"+
      " , cod_e_silinviaesito_id_univoco_versamento"+
      " , cod_e_silinviaesito_codice_contesto_pagamento"+
      " , de_e_silinviaesito_esito"+
      " , cod_e_silinviaesito_fault_code"+
      " , de_e_silinviaesito_fault_string"+
      " , cod_e_silinviaesito_id"+
      " , de_e_silinviaesito_description"+
      " , cod_e_silinviaesito_serial"+
      " , de_e_versione_oggetto"+
      " , cod_e_dom_id_dominio"+
      " , cod_e_dom_id_stazione_richiedente"+
      " , cod_e_id_messaggio_ricevuta"+
      " , cod_e_data_ora_messaggio_ricevuta"+
      " , cod_e_riferimento_messaggio_richiesta"+
      " , cod_e_riferimento_data_richiesta"+
      " , cod_e_istit_att_id_univ_att_tipo_id_univoco"+
      " , cod_e_istit_att_id_univ_att_codice_id_univoco"+
      " , de_e_istit_att_denominazione_attestante"+
      " , cod_e_istit_att_codice_unit_oper_attestante"+
      " , de_e_istit_att_denom_unit_oper_attestante"+
      " , de_e_istit_att_indirizzo_attestante"+
      " , de_e_istit_att_civico_attestante"+
      " , cod_e_istit_att_cap_attestante"+
      " , de_e_istit_att_localita_attestante"+
      " , de_e_istit_att_provincia_attestante"+
      " , cod_e_istit_att_nazione_attestante"+
      " , cod_e_ente_benef_id_univ_benef_tipo_id_univoco"+
      " , cod_e_ente_benef_id_univ_benef_codice_id_univoco"+
      " , de_e_ente_benef_denominazione_beneficiario"+
      " , cod_e_ente_benef_codice_unit_oper_beneficiario"+
      " , de_e_ente_benef_denom_unit_oper_beneficiario"+
      " , de_e_ente_benef_indirizzo_beneficiario"+
      " , de_e_ente_benef_civico_beneficiario"+
      " , cod_e_ente_benef_cap_beneficiario"+
      " , de_e_ente_benef_localita_beneficiario"+
      " , de_e_ente_benef_provincia_beneficiario"+
      " , cod_e_ente_benef_nazione_beneficiario"+
      " , cod_e_sogg_vers_id_univ_vers_tipo_id_univoco"+
      " , cod_e_sogg_vers_id_univ_vers_codice_id_univoco"+
      " , cod_e_sogg_vers_anagrafica_versante"+
      " , de_e_sogg_vers_indirizzo_versante"+
      " , de_e_sogg_vers_civico_versante"+
      " , cod_e_sogg_vers_cap_versante"+
      " , de_e_sogg_vers_localita_versante"+
      " , de_e_sogg_vers_provincia_versante"+
      " , cod_e_sogg_vers_nazione_versante"+
      " , de_e_sogg_vers_email_versante"+
      " , cod_e_sogg_pag_id_univ_pag_tipo_id_univoco"+
      " , cod_e_sogg_pag_id_univ_pag_codice_id_univoco"+
      " , cod_e_sogg_pag_anagrafica_pagatore"+
      " , de_e_sogg_pag_indirizzo_pagatore"+
      " , de_e_sogg_pag_civico_pagatore"+
      " , cod_e_sogg_pag_cap_pagatore"+
      " , de_e_sogg_pag_localita_pagatore"+
      " , de_e_sogg_pag_provincia_pagatore"+
      " , cod_e_sogg_pag_nazione_pagatore"+
      " , de_e_sogg_pag_email_pagatore"+
      " , cod_e_dati_pag_codice_esito_pagamento"+
      " , num_e_dati_pag_importo_totale_pagato"+
      " , cod_e_dati_pag_id_univoco_versamento"+
      " , cod_e_dati_pag_codice_contesto_pagamento"+
      " , id_session"+
      " , id_session_fesp"+
      " , tipo_carrello"+
      " , modello_pagamento"+
      " , ente_sil_invia_risposta_pagamento_url"+
      " , flg_notifica_esito"+
      " , mygov_carrello_multi_beneficiario_id"+
      " , cod_rp_silinviarp_original_fault_code"+
      " , de_rp_silinviarp_original_fault_string"+
      " , de_rp_silinviarp_original_fault_description"+
      " , cod_e_silinviaesito_original_fault_code"+
      " , de_e_silinviaesito_original_fault_string"+
      " , de_e_silinviaesito_original_fault_description"+
      ") values ("+
      "   nextval('mygov_carrello_mygov_carrello_id_seq')"+
      " , :c.version"+
      " , :c.mygovAnagraficaStatoId.mygovAnagraficaStatoId"+
      " , :c.codAckRp"+
      " , :c.dtCreazione"+
      " , :c.dtUltimaModificaRp"+
      " , :c.dtUltimaModificaE"+
      " , :c.codRpSilinviarpIdPsp"+
      " , :c.codRpSilinviarpIdIntermediarioPsp"+
      " , :c.codRpSilinviarpIdCanale"+
      " , :c.codRpSilinviarpIdDominio"+
      " , :c.codRpSilinviarpIdUnivocoVersamento"+
      " , :c.codRpSilinviarpCodiceContestoPagamento"+
      " , :c.deRpSilinviarpEsito"+
      " , :c.codRpSilinviarpRedirect"+
      " , :c.codRpSilinviarpUrl"+
      " , :c.codRpSilinviarpFaultCode"+
      " , :c.deRpSilinviarpFaultString"+
      " , :c.codRpSilinviarpId"+
      " , :c.deRpSilinviarpDescription"+
      " , :c.codRpSilinviarpSerial"+
      " , :c.deRpVersioneOggetto"+
      " , :c.codRpDomIdDominio"+
      " , :c.codRpDomIdStazioneRichiedente"+
      " , :c.codRpIdMessaggioRichiesta"+
      " , :c.dtRpDataOraMessaggioRichiesta"+
      " , :c.codRpAutenticazioneSoggetto"+
      " , :c.codRpSoggVersIdUnivVersTipoIdUnivoco"+
      " , :c.codRpSoggVersIdUnivVersCodiceIdUnivoco"+
      " , :c.codRpSoggVersAnagraficaVersante"+
      " , :c.deRpSoggVersIndirizzoVersante"+
      " , :c.deRpSoggVersCivicoVersante"+
      " , :c.codRpSoggVersCapVersante"+
      " , left(:c.deRpSoggVersLocalitaVersante,"+Dovuto.MAX_LENGTH_LOCALITA+")"+
      " , :c.deRpSoggVersProvinciaVersante"+
      " , :c.codRpSoggVersNazioneVersante"+
      " , :c.deRpSoggVersEmailVersante"+
      " , :c.codRpSoggPagIdUnivPagTipoIdUnivoco"+
      " , :c.codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      " , :c.deRpSoggPagAnagraficaPagatore"+
      " , :c.deRpSoggPagIndirizzoPagatore"+
      " , :c.deRpSoggPagCivicoPagatore"+
      " , :c.codRpSoggPagCapPagatore"+
      " , left(:c.deRpSoggPagLocalitaPagatore,"+Dovuto.MAX_LENGTH_LOCALITA+")"+
      " , :c.deRpSoggPagProvinciaPagatore"+
      " , :c.codRpSoggPagNazionePagatore"+
      " , :c.deRpSoggPagEmailPagatore"+
      " , :c.dtRpDatiVersDataEsecuzionePagamento"+
      " , :c.numRpDatiVersImportoTotaleDaVersare"+
      " , :c.codRpDatiVersTipoVersamento"+
      " , :c.codRpDatiVersIdUnivocoVersamento"+
      " , :c.codRpDatiVersCodiceContestoPagamento"+
      " , :c.deRpDatiVersIbanAddebito"+
      " , :c.deRpDatiVersBicAddebito"+
      " , :c.codESilinviaesitoIdDominio"+
      " , :c.codESilinviaesitoIdUnivocoVersamento"+
      " , :c.codESilinviaesitoCodiceContestoPagamento"+
      " , :c.deESilinviaesitoEsito"+
      " , :c.codESilinviaesitoFaultCode"+
      " , :c.deESilinviaesitoFaultString"+
      " , :c.codESilinviaesitoId"+
      " , :c.deESilinviaesitoDescription"+
      " , :c.codESilinviaesitoSerial"+
      " , :c.deEVersioneOggetto"+
      " , :c.codEDomIdDominio"+
      " , :c.codEDomIdStazioneRichiedente"+
      " , :c.codEIdMessaggioRicevuta"+
      " , :c.codEDataOraMessaggioRicevuta"+
      " , :c.codERiferimentoMessaggioRichiesta"+
      " , :c.codERiferimentoDataRichiesta"+
      " , :c.codEIstitAttIdUnivAttTipoIdUnivoco"+
      " , :c.codEIstitAttIdUnivAttCodiceIdUnivoco"+
      " , :c.deEIstitAttDenominazioneAttestante"+
      " , :c.codEIstitAttCodiceUnitOperAttestante"+
      " , :c.deEIstitAttDenomUnitOperAttestante"+
      " , :c.deEIstitAttIndirizzoAttestante"+
      " , :c.deEIstitAttCivicoAttestante"+
      " , :c.codEIstitAttCapAttestante"+
      " , :c.deEIstitAttLocalitaAttestante"+
      " , :c.deEIstitAttProvinciaAttestante"+
      " , :c.codEIstitAttNazioneAttestante"+
      " , :c.codEEnteBenefIdUnivBenefTipoIdUnivoco"+
      " , :c.codEEnteBenefIdUnivBenefCodiceIdUnivoco"+
      " , :c.deEEnteBenefDenominazioneBeneficiario"+
      " , :c.codEEnteBenefCodiceUnitOperBeneficiario"+
      " , :c.deEEnteBenefDenomUnitOperBeneficiario"+
      " , :c.deEEnteBenefIndirizzoBeneficiario"+
      " , :c.deEEnteBenefCivicoBeneficiario"+
      " , :c.codEEnteBenefCapBeneficiario"+
      " , :c.deEEnteBenefLocalitaBeneficiario"+
      " , :c.deEEnteBenefProvinciaBeneficiario"+
      " , :c.codEEnteBenefNazioneBeneficiario"+
      " , :c.codESoggVersIdUnivVersTipoIdUnivoco"+
      " , :c.codESoggVersIdUnivVersCodiceIdUnivoco"+
      " , :c.codESoggVersAnagraficaVersante"+
      " , :c.deESoggVersIndirizzoVersante"+
      " , :c.deESoggVersCivicoVersante"+
      " , :c.codESoggVersCapVersante"+
      " , :c.deESoggVersLocalitaVersante"+
      " , :c.deESoggVersProvinciaVersante"+
      " , :c.codESoggVersNazioneVersante"+
      " , :c.deESoggVersEmailVersante"+
      " , :c.codESoggPagIdUnivPagTipoIdUnivoco"+
      " , :c.codESoggPagIdUnivPagCodiceIdUnivoco"+
      " , :c.codESoggPagAnagraficaPagatore"+
      " , :c.deESoggPagIndirizzoPagatore"+
      " , :c.deESoggPagCivicoPagatore"+
      " , :c.codESoggPagCapPagatore"+
      " , :c.deESoggPagLocalitaPagatore"+
      " , :c.deESoggPagProvinciaPagatore"+
      " , :c.codESoggPagNazionePagatore"+
      " , :c.deESoggPagEmailPagatore"+
      " , :c.codEDatiPagCodiceEsitoPagamento"+
      " , :c.numEDatiPagImportoTotalePagato"+
      " , :c.codEDatiPagIdUnivocoVersamento"+
      " , :c.codEDatiPagCodiceContestoPagamento"+
      " , :c.idSession"+
      " , :c.idSessionFesp"+
      " , :c.tipoCarrello"+
      " , :c.modelloPagamento"+
      " , :c.enteSilInviaRispostaPagamentoUrl"+
      " , :c.flgNotificaEsito"+
      " , :c.mygovCarrelloMultiBeneficiarioId?.mygovCarrelloMultiBeneficiarioId"+
      " , :c.codRpSilinviarpOriginalFaultCode"+
      " , :c.deRpSilinviarpOriginalFaultString"+
      " , :c.deRpSilinviarpOriginalFaultDescription"+
      " , :c.codESilinviaesitoOriginalFaultCode"+
      " , :c.deESilinviaesitoOriginalFaultString"+
      " , :c.deESilinviaesitoOriginalFaultDescription)"
  )
  @GetGeneratedKeys("mygov_carrello_id")
  Long insert(@BindBean("c") Carrello c);

  @SqlUpdate("update mygov_carrello"+
    " set flg_notifica_esito = :flgNotificaEsito" +
    " where cod_rp_id_messaggio_richiesta = :idMessaggioRichiesta")
  int updateFlgNotificaEsitoByIdMessaggioRichiesta(String idMessaggioRichiesta, Boolean flgNotificaEsito);

  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +", "+AnagraficaStato.FIELDS+","+CarrelloMultiBeneficiario.FIELDS+
      " from mygov_carrello " + Carrello.ALIAS +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Carrello.ALIAS+".mygov_anagrafica_stato_id " +
      "  left join mygov_carrello_multi_beneficiario "+CarrelloMultiBeneficiario.ALIAS+" on "+CarrelloMultiBeneficiario.ALIAS+".mygov_carrello_multi_beneficiario_id = "+Carrello.ALIAS+".mygov_carrello_multi_beneficiario_id " +
      " where "+Carrello.ALIAS + ".mygov_carrello_id = :id"
  )
  @RegisterFieldMapper(Carrello.class)
  Carrello getById(Long id);

  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +
    " from mygov_carrello " + Carrello.ALIAS +
    " where "+Carrello.ALIAS + ".mygov_carrello_id = :id " +
    " for update skip locked "
  )
  @RegisterFieldMapper(Carrello.class)
  Optional<Carrello> getByIdLockOrSkip(Long id);

  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +", "+AnagraficaStato.FIELDS+","+CarrelloMultiBeneficiario.FIELDS+
      " from mygov_carrello " + Carrello.ALIAS +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Carrello.ALIAS+".mygov_anagrafica_stato_id " +
      "  left join mygov_carrello_multi_beneficiario "+CarrelloMultiBeneficiario.ALIAS+" on "+CarrelloMultiBeneficiario.ALIAS+".mygov_carrello_multi_beneficiario_id = "+Carrello.ALIAS+".mygov_carrello_multi_beneficiario_id " +
      " where "+Carrello.ALIAS + ".cod_rp_id_messaggio_richiesta = :idMessaggioRichiesta"
  )
  @RegisterFieldMapper(Carrello.class)
  List<Carrello> getByIdMessaggioRichiesta(String idMessaggioRichiesta);

  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +", "+AnagraficaStato.FIELDS+","+CarrelloMultiBeneficiario.FIELDS+
      " from mygov_carrello " + Carrello.ALIAS +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Carrello.ALIAS+".mygov_anagrafica_stato_id " +
      "  left join mygov_carrello_multi_beneficiario "+CarrelloMultiBeneficiario.ALIAS+" on "+CarrelloMultiBeneficiario.ALIAS+".mygov_carrello_multi_beneficiario_id = "+Carrello.ALIAS+".mygov_carrello_multi_beneficiario_id " +
      " where "+Carrello.ALIAS + ".id_session = :idSession"
  )
  @RegisterFieldMapper(Carrello.class)
  List<Carrello> getByIdSession (String idSession);

  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +", "+AnagraficaStato.FIELDS+","+CarrelloMultiBeneficiario.FIELDS+
      " from mygov_carrello " + Carrello.ALIAS +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Carrello.ALIAS+".mygov_anagrafica_stato_id " +
      " inner join mygov_carrello_multi_beneficiario "+CarrelloMultiBeneficiario.ALIAS+" on "+CarrelloMultiBeneficiario.ALIAS+".mygov_carrello_multi_beneficiario_id = "+Carrello.ALIAS+".mygov_carrello_multi_beneficiario_id " +
      " where "+Carrello.ALIAS + ".mygov_carrello_multi_beneficiario_id = :mygovCarrelloMultiBeneficiarioId"
  )
  @RegisterFieldMapper(Carrello.class)
  List<Carrello> getByMultiBeneficarioId (Long mygovCarrelloMultiBeneficiarioId);


  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +", "+AnagraficaStato.FIELDS+","+CarrelloMultiBeneficiario.FIELDS+
      " from mygov_carrello " + Carrello.ALIAS +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Carrello.ALIAS+".mygov_anagrafica_stato_id " +
      "  left join mygov_carrello_multi_beneficiario "+CarrelloMultiBeneficiario.ALIAS+" on "+CarrelloMultiBeneficiario.ALIAS+".mygov_carrello_multi_beneficiario_id = "+Carrello.ALIAS+".mygov_carrello_multi_beneficiario_id " +
      " where "+Carrello.ALIAS + ".cod_rp_silinviarp_id_dominio = :idDominio "+
      "   and "+Carrello.ALIAS + ".cod_rp_silinviarp_id_univoco_versamento = :iuv "
  )
  @RegisterFieldMapper(Carrello.class)
  List<Carrello> getByIdDominioIUV(String idDominio, String iuv);


  @SqlUpdate("delete from mygov_carrello "+ Carrello.ALIAS +
      " where "+ Carrello.ALIAS +".mygov_carrello_id = :mygovCarrelloId"
  )
  int delete(Long mygovCarrelloId);

  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +", "+AnagraficaStato.FIELDS+","+CarrelloMultiBeneficiario.FIELDS+
      " from mygov_carrello " + Carrello.ALIAS +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Carrello.ALIAS+".mygov_anagrafica_stato_id " +
      "  left join mygov_carrello_multi_beneficiario "+CarrelloMultiBeneficiario.ALIAS+" on "+CarrelloMultiBeneficiario.ALIAS+".mygov_carrello_multi_beneficiario_id = "+Carrello.ALIAS+".mygov_carrello_multi_beneficiario_id " +
      " where "+Carrello.ALIAS + ".cod_rp_silinviarp_id_dominio = :idDominio "+
      "   and "+Carrello.ALIAS + ".cod_rp_silinviarp_id_univoco_versamento = :iuv "+
      "   and "+Carrello.ALIAS + ".cod_rp_silinviarp_codice_contesto_pagamento = :codiceContestoPagamento "
  )
  @RegisterFieldMapper(Carrello.class)
  List<Carrello> getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamento(String idDominio, String iuv, String codiceContestoPagamento);

  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +
    " from mygov_carrello " + Carrello.ALIAS +
    " where "+Carrello.ALIAS + ".cod_rp_silinviarp_id_dominio = :idDominio "+
    "   and "+Carrello.ALIAS + ".cod_rp_silinviarp_id_univoco_versamento = :iuv "+
    "   and "+Carrello.ALIAS + ".cod_rp_silinviarp_codice_contesto_pagamento = :codiceContestoPagamento "
  )
  @RegisterFieldMapper(Carrello.class)
  List<Carrello> getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamentoNoJoin(String idDominio, String iuv, String codiceContestoPagamento);


  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +
      " from mygov_carrello " + Carrello.ALIAS +
      " where " + Carrello.ALIAS + ".mygov_anagrafica_stato_id = <statoCarrelloPagamentoInCorso> " +
      "   and " + Carrello.ALIAS + ".modello_pagamento = :modelloPagamento " +
      "   and " + Carrello.ALIAS + ".dt_ultima_modifica_rp < now() - make_interval(0,0,0,0,0,:deltaMinutes,0)" +
      " order by " + Carrello.ALIAS + ".dt_ultima_modifica_rp DESC "
  )
  @RegisterFieldMapper(Carrello.class)
  List<Carrello> getByStatePagamentoInCorso(int modelloPagamento, int deltaMinutes, @Define long statoCarrelloPagamentoInCorso);


  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +
      " from mygov_carrello " + Carrello.ALIAS +
      " where "+Carrello.ALIAS + ".flg_notifica_esito is not true " +
      "   and "+Carrello.ALIAS + ".cod_e_dati_pag_codice_esito_pagamento is not null " +
      "   and ( "+Carrello.ALIAS + ".de_e_sogg_pag_email_pagatore is not null " +
      "      or "+Carrello.ALIAS + ".de_e_sogg_vers_email_versante is not null " +
      "      or "+Carrello.ALIAS + ".tipo_carrello in ('"+Constants.TIPO_CARRELLO_AVVISO_ANONIMO+"','"+Constants.TIPO_CARRELLO_SPONTANEO_ANONIMO+"') )" +
      "   and "+Carrello.ALIAS + ".cod_e_id_messaggio_ricevuta not like '###%' " +
      " limit <queryLimit> " +
      " for update skip locked"
  )
  @RegisterFieldMapper(Carrello.class)
  List<Carrello> getWithEsitoAndNotMailSent(@Define int queryLimit);

  @SqlQuery(" select " + Carrello.ALIAS + ALL_FIELDS +
      " from mygov_carrello " + Carrello.ALIAS +
      " where "+Carrello.ALIAS + ".mygov_anagrafica_stato_id = :mygovAnagraficaStatoId " +
      "   and "+Carrello.ALIAS + ".tipo_carrello = :type " +
      "   and "+Carrello.ALIAS + ".dt_creazione < now() - make_interval(0,0,0,0,0,:deltaMinutes,0) " +
      " limit <queryLimit> " +
      " for update skip locked"
  )
  @RegisterFieldMapper(Carrello.class)
  List<Carrello> getOlderByTypeAndState(int deltaMinutes, String type, Long mygovAnagraficaStatoId, @Define int queryLimit);


  @SqlUpdate("update mygov_carrello" +
      " set mygov_anagrafica_stato_id = :mygovAnagraficaStatoId"+
      " where mygov_carrello_id = :mygovCarrelloId"
  )
  int updateStato(Long mygovCarrelloId, Long mygovAnagraficaStatoId);

}
