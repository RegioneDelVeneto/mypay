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

import it.regioneveneto.mygov.payment.mypay4.dto.DovutoOperatoreTo;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.jdbi.v3.core.statement.OutParameters;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.OutParameter;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlCall;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.Types;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DovutoElaboratoDao extends BaseDao {

  @SqlUpdate(
      " update mygov_dovuto_elaborato " +
          "set   version = :d.version" +
          ",  flg_dovuto_attuale = :d.flgDovutoAttuale" +
          ",  mygov_flusso_id = :d.mygovFlussoId.mygovFlussoId" +
          ",  num_riga_flusso = :d.numRigaFlusso" +
          ",  mygov_anagrafica_stato_id = :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          ",  mygov_carrello_id = :d.mygovCarrelloId?.mygovCarrelloId" +
          ",  cod_iud = :d.codIud" +
          ",  cod_iuv = :d.codIuv" +
          ",  cod_ack_rp = :d.codAckRp" +
          ",  dt_creazione = :d.dtCreazione" +
          ",  dt_ultima_modifica_rp = :d.dtUltimaModificaRp" +
          ",  dt_ultima_modifica_e = :d.dtUltimaModificaE" +
          ",  cod_rp_silinviarp_id_psp = :d.codRpSilinviarpIdPsp" +
          ",  cod_rp_silinviarp_id_intermediario_psp = :d.codRpSilinviarpIdIntermediarioPsp" +
          ",  cod_rp_silinviarp_id_canale = :d.codRpSilinviarpIdCanale" +
          ",  cod_rp_silinviarp_id_dominio = :d.codRpSilinviarpIdDominio" +
          ",  cod_rp_silinviarp_id_univoco_versamento = :d.codRpSilinviarpIdUnivocoVersamento" +
          ",  cod_rp_silinviarp_codice_contesto_pagamento = :d.codRpSilinviarpCodiceContestoPagamento" +
          ",  de_rp_silinviarp_esito = :d.deRpSilinviarpEsito" +
          ",  cod_rp_silinviarp_redirect = :d.codRpSilinviarpRedirect" +
          ",  cod_rp_silinviarp_url = :d.codRpSilinviarpUrl" +
          ",  cod_rp_silinviarp_fault_code = :d.codRpSilinviarpFaultCode" +
          ",  de_rp_silinviarp_fault_string = :d.deRpSilinviarpFaultString" +
          ",  cod_rp_silinviarp_id = :d.codRpSilinviarpId" +
          ",  de_rp_silinviarp_description = :d.deRpSilinviarpDescription" +
          ",  cod_rp_silinviarp_serial = :d.codRpSilinviarpSerial" +
          ",  de_rp_versione_oggetto = :d.deRpVersioneOggetto" +
          ",  cod_rp_dom_id_dominio = :d.codRpDomIdDominio" +
          ",  cod_rp_dom_id_stazione_richiedente = :d.codRpDomIdStazioneRichiedente" +
          ",  cod_rp_id_messaggio_richiesta = :d.codRpIdMessaggioRichiesta" +
          ",  dt_rp_data_ora_messaggio_richiesta = :d.dtRpDataOraMessaggioRichiesta" +
          ",  cod_rp_autenticazione_soggetto = :d.codRpAutenticazioneSoggetto" +
          ",  cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco = :d.codRpSoggVersIdUnivVersTipoIdUnivoco" +
          ",  cod_rp_sogg_vers_id_univ_vers_codice_id_univoco = :d.codRpSoggVersIdUnivVersCodiceIdUnivoco" +
          ",  cod_rp_sogg_vers_anagrafica_versante = :d.codRpSoggVersAnagraficaVersante" +
          ",  de_rp_sogg_vers_indirizzo_versante = :d.deRpSoggVersIndirizzoVersante" +
          ",  de_rp_sogg_vers_civico_versante = :d.deRpSoggVersCivicoVersante" +
          ",  cod_rp_sogg_vers_cap_versante = :d.codRpSoggVersCapVersante" +
          ",  de_rp_sogg_vers_localita_versante = left(:d.deRpSoggVersLocalitaVersante,"+Dovuto.MAX_LENGTH_LOCALITA+")" +
          ",  de_rp_sogg_vers_provincia_versante = :d.deRpSoggVersProvinciaVersante" +
          ",  cod_rp_sogg_vers_nazione_versante = :d.codRpSoggVersNazioneVersante" +
          ",  de_rp_sogg_vers_email_versante = :d.deRpSoggVersEmailVersante" +
          ",  cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :d.codRpSoggPagIdUnivPagTipoIdUnivoco" +
          ",  cod_rp_sogg_pag_id_univ_pag_codice_id_univoco = :d.codRpSoggPagIdUnivPagCodiceIdUnivoco" +
          ",  de_rp_sogg_pag_anagrafica_pagatore = :d.deRpSoggPagAnagraficaPagatore" +
          ",  de_rp_sogg_pag_indirizzo_pagatore = :d.deRpSoggPagIndirizzoPagatore" +
          ",  de_rp_sogg_pag_civico_pagatore = :d.deRpSoggPagCivicoPagatore" +
          ",  cod_rp_sogg_pag_cap_pagatore = :d.codRpSoggPagCapPagatore" +
          ",  de_rp_sogg_pag_localita_pagatore = left(:d.deRpSoggPagLocalitaPagatore,"+Dovuto.MAX_LENGTH_LOCALITA+")" +
          ",  de_rp_sogg_pag_provincia_pagatore = :d.deRpSoggPagProvinciaPagatore" +
          ",  cod_rp_sogg_pag_nazione_pagatore = :d.codRpSoggPagNazionePagatore" +
          ",  de_rp_sogg_pag_email_pagatore = :d.deRpSoggPagEmailPagatore" +
          ",  dt_rp_dati_vers_data_esecuzione_pagamento = :d.dtRpDatiVersDataEsecuzionePagamento" +
          ",  num_rp_dati_vers_importo_totale_da_versare = :d.numRpDatiVersImportoTotaleDaVersare" +
          ",  cod_rp_dati_vers_tipo_versamento = :d.codRpDatiVersTipoVersamento" +
          ",  cod_rp_dati_vers_id_univoco_versamento = :d.codRpDatiVersIdUnivocoVersamento" +
          ",  cod_rp_dati_vers_codice_contesto_pagamento = :d.codRpDatiVersCodiceContestoPagamento" +
          ",  de_rp_dati_vers_iban_addebito = :d.deRpDatiVersIbanAddebito" +
          ",  de_rp_dati_vers_bic_addebito = :d.deRpDatiVersBicAddebito" +
          ",  num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento = :d.numRpDatiVersDatiSingVersImportoSingoloVersamento" +
          ",  num_rp_dati_vers_dati_sing_vers_commissione_carico_pa = :d.numRpDatiVersDatiSingVersCommissioneCaricoPa" +
          ",  cod_rp_dati_vers_dati_sing_vers_iban_accredito = :d.codRpDatiVersDatiSingVersIbanAccredito" +
          ",  cod_rp_dati_vers_dati_sing_vers_bic_accredito = :d.codRpDatiVersDatiSingVersBicAccredito" +
          ",  cod_rp_dati_vers_dati_sing_vers_iban_appoggio = :d.codRpDatiVersDatiSingVersIbanAppoggio" +
          ",  cod_rp_dati_vers_dati_sing_vers_bic_appoggio = :d.codRpDatiVersDatiSingVersBicAppoggio" +
          ",  cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore = :d.codRpDatiVersDatiSingVersCredenzialiPagatore" +
          ",  de_rp_dati_vers_dati_sing_vers_causale_versamento = :d.deRpDatiVersDatiSingVersCausaleVersamento" +
          ",  de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione = :d.deRpDatiVersDatiSingVersDatiSpecificiRiscossione" +
          ",  cod_e_silinviaesito_id_dominio = :d.codESilinviaesitoIdDominio" +
          ",  cod_e_silinviaesito_id_univoco_versamento = :d.codESilinviaesitoIdUnivocoVersamento" +
          ",  cod_e_silinviaesito_codice_contesto_pagamento = :d.codESilinviaesitoCodiceContestoPagamento" +
          ",  de_e_silinviaesito_esito = :d.deESilinviaesitoEsito" +
          ",  cod_e_silinviaesito_fault_code = :d.codESilinviaesitoFaultCode" +
          ",  de_e_silinviaesito_fault_string = :d.deESilinviaesitoFaultString" +
          ",  cod_e_silinviaesito_id = :d.codESilinviaesitoId" +
          ",  de_e_silinviaesito_description = :d.deESilinviaesitoDescription" +
          ",  cod_e_silinviaesito_serial = :d.codESilinviaesitoSerial" +
          ",  de_e_versione_oggetto = :d.deEVersioneOggetto" +
          ",  cod_e_dom_id_dominio = :d.codEDomIdDominio" +
          ",  cod_e_dom_id_stazione_richiedente = :d.codEDomIdStazioneRichiedente" +
          ",  cod_e_id_messaggio_ricevuta = :d.codEIdMessaggioRicevuta" +
          ",  cod_e_data_ora_messaggio_ricevuta = :d.codEDataOraMessaggioRicevuta" +
          ",  cod_e_riferimento_messaggio_richiesta = :d.codERiferimentoMessaggioRichiesta" +
          ",  cod_e_riferimento_data_richiesta = :d.codERiferimentoDataRichiesta" +
          ",  cod_e_istit_att_id_univ_att_tipo_id_univoco = :d.codEIstitAttIdUnivAttTipoIdUnivoco" +
          ",  cod_e_istit_att_id_univ_att_codice_id_univoco = :d.codEIstitAttIdUnivAttCodiceIdUnivoco" +
          ",  de_e_istit_att_denominazione_attestante = :d.deEIstitAttDenominazioneAttestante" +
          ",  cod_e_istit_att_codice_unit_oper_attestante = :d.codEIstitAttCodiceUnitOperAttestante" +
          ",  de_e_istit_att_denom_unit_oper_attestante = :d.deEIstitAttDenomUnitOperAttestante" +
          ",  de_e_istit_att_indirizzo_attestante = :d.deEIstitAttIndirizzoAttestante" +
          ",  de_e_istit_att_civico_attestante = :d.deEIstitAttCivicoAttestante" +
          ",  cod_e_istit_att_cap_attestante = :d.codEIstitAttCapAttestante" +
          ",  de_e_istit_att_localita_attestante = :d.deEIstitAttLocalitaAttestante" +
          ",  de_e_istit_att_provincia_attestante = :d.deEIstitAttProvinciaAttestante" +
          ",  cod_e_istit_att_nazione_attestante = :d.codEIstitAttNazioneAttestante" +
          ",  cod_e_ente_benef_id_univ_benef_tipo_id_univoco = :d.codEEnteBenefIdUnivBenefTipoIdUnivoco" +
          ",  cod_e_ente_benef_id_univ_benef_codice_id_univoco = :d.codEEnteBenefIdUnivBenefCodiceIdUnivoco" +
          ",  de_e_ente_benef_denominazione_beneficiario = :d.deEEnteBenefDenominazioneBeneficiario" +
          ",  cod_e_ente_benef_codice_unit_oper_beneficiario = :d.codEEnteBenefCodiceUnitOperBeneficiario" +
          ",  de_e_ente_benef_denom_unit_oper_beneficiario = :d.deEEnteBenefDenomUnitOperBeneficiario" +
          ",  de_e_ente_benef_indirizzo_beneficiario = :d.deEEnteBenefIndirizzoBeneficiario" +
          ",  de_e_ente_benef_civico_beneficiario = :d.deEEnteBenefCivicoBeneficiario" +
          ",  cod_e_ente_benef_cap_beneficiario = :d.codEEnteBenefCapBeneficiario" +
          ",  de_e_ente_benef_localita_beneficiario = :d.deEEnteBenefLocalitaBeneficiario" +
          ",  de_e_ente_benef_provincia_beneficiario = :d.deEEnteBenefProvinciaBeneficiario" +
          ",  cod_e_ente_benef_nazione_beneficiario = :d.codEEnteBenefNazioneBeneficiario" +
          ",  cod_e_sogg_vers_id_univ_vers_tipo_id_univoco = :d.codESoggVersIdUnivVersTipoIdUnivoco" +
          ",  cod_e_sogg_vers_id_univ_vers_codice_id_univoco = :d.codESoggVersIdUnivVersCodiceIdUnivoco" +
          ",  cod_e_sogg_vers_anagrafica_versante = :d.codESoggVersAnagraficaVersante" +
          ",  de_e_sogg_vers_indirizzo_versante = :d.deESoggVersIndirizzoVersante" +
          ",  de_e_sogg_vers_civico_versante = :d.deESoggVersCivicoVersante" +
          ",  cod_e_sogg_vers_cap_versante = :d.codESoggVersCapVersante" +
          ",  de_e_sogg_vers_localita_versante = :d.deESoggVersLocalitaVersante" +
          ",  de_e_sogg_vers_provincia_versante = :d.deESoggVersProvinciaVersante" +
          ",  cod_e_sogg_vers_nazione_versante = :d.codESoggVersNazioneVersante" +
          ",  de_e_sogg_vers_email_versante = :d.deESoggVersEmailVersante" +
          ",  cod_e_sogg_pag_id_univ_pag_tipo_id_univoco = :d.codESoggPagIdUnivPagTipoIdUnivoco" +
          ",  cod_e_sogg_pag_id_univ_pag_codice_id_univoco = :d.codESoggPagIdUnivPagCodiceIdUnivoco" +
          ",  cod_e_sogg_pag_anagrafica_pagatore = :d.codESoggPagAnagraficaPagatore" +
          ",  de_e_sogg_pag_indirizzo_pagatore = :d.deESoggPagIndirizzoPagatore" +
          ",  de_e_sogg_pag_civico_pagatore = :d.deESoggPagCivicoPagatore" +
          ",  cod_e_sogg_pag_cap_pagatore = :d.codESoggPagCapPagatore" +
          ",  de_e_sogg_pag_localita_pagatore = :d.deESoggPagLocalitaPagatore" +
          ",  de_e_sogg_pag_provincia_pagatore = :d.deESoggPagProvinciaPagatore" +
          ",  cod_e_sogg_pag_nazione_pagatore = :d.codESoggPagNazionePagatore" +
          ",  de_e_sogg_pag_email_pagatore = :d.deESoggPagEmailPagatore" +
          ",  cod_e_dati_pag_codice_esito_pagamento = :d.codEDatiPagCodiceEsitoPagamento" +
          ",  num_e_dati_pag_importo_totale_pagato = :d.numEDatiPagImportoTotalePagato" +
          ",  cod_e_dati_pag_id_univoco_versamento = :d.codEDatiPagIdUnivocoVersamento" +
          ",  cod_e_dati_pag_codice_contesto_pagamento = :d.codEDatiPagCodiceContestoPagamento" +
          ",  num_e_dati_pag_dati_sing_pag_singolo_importo_pagato = :d.numEDatiPagDatiSingPagSingoloImportoPagato" +
          ",  de_e_dati_pag_dati_sing_pag_esito_singolo_pagamento = :d.deEDatiPagDatiSingPagEsitoSingoloPagamento" +
          ",  dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento = :d.dtEDatiPagDatiSingPagDataEsitoSingoloPagamento" +
          ",  cod_e_dati_pag_dati_sing_pag_id_univoco_riscoss = :d.codEDatiPagDatiSingPagIdUnivocoRiscoss" +
          ",  de_e_dati_pag_dati_sing_pag_causale_versamento = :d.deEDatiPagDatiSingPagCausaleVersamento" +
          ",  de_e_dati_pag_dati_sing_pag_dati_specifici_riscossione = :d.deEDatiPagDatiSingPagDatiSpecificiRiscossione" +
          ",  cod_tipo_dovuto = :d.codTipoDovuto" +
          ",  dt_ultimo_cambio_stato = :d.dtUltimoCambioStato" +
          ",  modello_pagamento = :d.modelloPagamento" +
          ",  ente_sil_invia_risposta_pagamento_url = :d.enteSilInviaRispostaPagamentoUrl" +
          ",  de_rt_inviart_tipo_firma = :d.deRtInviartTipoFirma" +
          ",  blb_rt_payload = :d.blbRtPayload" +
          ",  indice_dati_singolo_pagamento = :d.indiceDatiSingoloPagamento" +
          ",  num_e_dati_pag_dati_sing_pag_commissioni_applicate_psp = :d.numEDatiPagDatiSingPagCommissioniApplicatePsp" +
          ",  cod_e_dati_pag_dati_sing_pag_allegato_ricevuta_tipo = :d.codEDatiPagDatiSingPagAllegatoRicevutaTipo" +
          ",  blb_e_dati_pag_dati_sing_pag_allegato_ricevuta_test = :d.blbEDatiPagDatiSingPagAllegatoRicevutaTest" +
          ",  cod_rp_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo = :d.codRpDatiVersDatiSingVersDatiMbdTipoBollo" +
          ",  cod_rp_dati_vers_dati_sing_vers_dati_mbd_hash_documento = :d.codRpDatiVersDatiSingVersDatiMbdHashDocumento" +
          ",  cod_rp_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza = :d.codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza" +
          ",  de_rp_dati_vers_dati_sing_vers_causale_versamento_agid = :d.deRpDatiVersDatiSingVersCausaleVersamentoAgid" +
          ",  bilancio = :d.bilancio" +
          ",  cod_rp_silinviarp_original_fault_code = :d.codRpSilinviarpOriginalFaultCode" +
          ",  de_rp_silinviarp_original_fault_string = :d.deRpSilinviarpOriginalFaultString" +
          ",  de_rp_silinviarp_original_fault_description = :d.deRpSilinviarpOriginalFaultDescription" +
          ",  cod_e_silinviaesito_original_fault_code = :d.codESilinviaesitoOriginalFaultCode" +
          ",  de_e_silinviaesito_original_fault_string = :d.deESilinviaesitoOriginalFaultString" +
          ",  de_e_silinviaesito_original_fault_description = :d.deESilinviaesitoOriginalFaultDescription" +
          " where  mygov_dovuto_elaborato_id = :d.mygovDovutoElaboratoId"
  )
  int update(@BindBean("d") DovutoElaborato d);

  @SqlUpdate(
      " insert into mygov_dovuto_elaborato (" +
          "   mygov_dovuto_elaborato_id" +
          " , version" +
          " , flg_dovuto_attuale" +
          " , mygov_flusso_id" +
          " , num_riga_flusso" +
          " , mygov_anagrafica_stato_id" +
          " , mygov_carrello_id" +
          " , cod_iud" +
          " , cod_iuv" +
          " , cod_ack_rp" +
          " , dt_creazione" +
          " , dt_ultima_modifica_rp" +
          " , dt_ultima_modifica_e" +
          " , cod_rp_silinviarp_id_psp" +
          " , cod_rp_silinviarp_id_intermediario_psp" +
          " , cod_rp_silinviarp_id_canale" +
          " , cod_rp_silinviarp_id_dominio" +
          " , cod_rp_silinviarp_id_univoco_versamento" +
          " , cod_rp_silinviarp_codice_contesto_pagamento" +
          " , de_rp_silinviarp_esito" +
          " , cod_rp_silinviarp_redirect" +
          " , cod_rp_silinviarp_url" +
          " , cod_rp_silinviarp_fault_code" +
          " , de_rp_silinviarp_fault_string" +
          " , cod_rp_silinviarp_id" +
          " , de_rp_silinviarp_description" +
          " , cod_rp_silinviarp_serial" +
          " , de_rp_versione_oggetto" +
          " , cod_rp_dom_id_dominio" +
          " , cod_rp_dom_id_stazione_richiedente" +
          " , cod_rp_id_messaggio_richiesta" +
          " , dt_rp_data_ora_messaggio_richiesta" +
          " , cod_rp_autenticazione_soggetto" +
          " , cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco" +
          " , cod_rp_sogg_vers_id_univ_vers_codice_id_univoco" +
          " , cod_rp_sogg_vers_anagrafica_versante" +
          " , de_rp_sogg_vers_indirizzo_versante" +
          " , de_rp_sogg_vers_civico_versante" +
          " , cod_rp_sogg_vers_cap_versante" +
          " , de_rp_sogg_vers_localita_versante" +
          " , de_rp_sogg_vers_provincia_versante" +
          " , cod_rp_sogg_vers_nazione_versante" +
          " , de_rp_sogg_vers_email_versante" +
          " , cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco" +
          " , cod_rp_sogg_pag_id_univ_pag_codice_id_univoco" +
          " , de_rp_sogg_pag_anagrafica_pagatore" +
          " , de_rp_sogg_pag_indirizzo_pagatore" +
          " , de_rp_sogg_pag_civico_pagatore" +
          " , cod_rp_sogg_pag_cap_pagatore" +
          " , de_rp_sogg_pag_localita_pagatore" +
          " , de_rp_sogg_pag_provincia_pagatore" +
          " , cod_rp_sogg_pag_nazione_pagatore" +
          " , de_rp_sogg_pag_email_pagatore" +
          " , dt_rp_dati_vers_data_esecuzione_pagamento" +
          " , num_rp_dati_vers_importo_totale_da_versare" +
          " , cod_rp_dati_vers_tipo_versamento" +
          " , cod_rp_dati_vers_id_univoco_versamento" +
          " , cod_rp_dati_vers_codice_contesto_pagamento" +
          " , de_rp_dati_vers_iban_addebito" +
          " , de_rp_dati_vers_bic_addebito" +
          " , num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento" +
          " , num_rp_dati_vers_dati_sing_vers_commissione_carico_pa" +
          " , cod_rp_dati_vers_dati_sing_vers_iban_accredito" +
          " , cod_rp_dati_vers_dati_sing_vers_bic_accredito" +
          " , cod_rp_dati_vers_dati_sing_vers_iban_appoggio" +
          " , cod_rp_dati_vers_dati_sing_vers_bic_appoggio" +
          " , cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore" +
          " , de_rp_dati_vers_dati_sing_vers_causale_versamento" +
          " , de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione" +
          " , cod_e_silinviaesito_id_dominio" +
          " , cod_e_silinviaesito_id_univoco_versamento" +
          " , cod_e_silinviaesito_codice_contesto_pagamento" +
          " , de_e_silinviaesito_esito" +
          " , cod_e_silinviaesito_fault_code" +
          " , de_e_silinviaesito_fault_string" +
          " , cod_e_silinviaesito_id" +
          " , de_e_silinviaesito_description" +
          " , cod_e_silinviaesito_serial" +
          " , de_e_versione_oggetto" +
          " , cod_e_dom_id_dominio" +
          " , cod_e_dom_id_stazione_richiedente" +
          " , cod_e_id_messaggio_ricevuta" +
          " , cod_e_data_ora_messaggio_ricevuta" +
          " , cod_e_riferimento_messaggio_richiesta" +
          " , cod_e_riferimento_data_richiesta" +
          " , cod_e_istit_att_id_univ_att_tipo_id_univoco" +
          " , cod_e_istit_att_id_univ_att_codice_id_univoco" +
          " , de_e_istit_att_denominazione_attestante" +
          " , cod_e_istit_att_codice_unit_oper_attestante" +
          " , de_e_istit_att_denom_unit_oper_attestante" +
          " , de_e_istit_att_indirizzo_attestante" +
          " , de_e_istit_att_civico_attestante" +
          " , cod_e_istit_att_cap_attestante" +
          " , de_e_istit_att_localita_attestante" +
          " , de_e_istit_att_provincia_attestante" +
          " , cod_e_istit_att_nazione_attestante" +
          " , cod_e_ente_benef_id_univ_benef_tipo_id_univoco" +
          " , cod_e_ente_benef_id_univ_benef_codice_id_univoco" +
          " , de_e_ente_benef_denominazione_beneficiario" +
          " , cod_e_ente_benef_codice_unit_oper_beneficiario" +
          " , de_e_ente_benef_denom_unit_oper_beneficiario" +
          " , de_e_ente_benef_indirizzo_beneficiario" +
          " , de_e_ente_benef_civico_beneficiario" +
          " , cod_e_ente_benef_cap_beneficiario" +
          " , de_e_ente_benef_localita_beneficiario" +
          " , de_e_ente_benef_provincia_beneficiario" +
          " , cod_e_ente_benef_nazione_beneficiario" +
          " , cod_e_sogg_vers_id_univ_vers_tipo_id_univoco" +
          " , cod_e_sogg_vers_id_univ_vers_codice_id_univoco" +
          " , cod_e_sogg_vers_anagrafica_versante" +
          " , de_e_sogg_vers_indirizzo_versante" +
          " , de_e_sogg_vers_civico_versante" +
          " , cod_e_sogg_vers_cap_versante" +
          " , de_e_sogg_vers_localita_versante" +
          " , de_e_sogg_vers_provincia_versante" +
          " , cod_e_sogg_vers_nazione_versante" +
          " , de_e_sogg_vers_email_versante" +
          " , cod_e_sogg_pag_id_univ_pag_tipo_id_univoco" +
          " , cod_e_sogg_pag_id_univ_pag_codice_id_univoco" +
          " , cod_e_sogg_pag_anagrafica_pagatore" +
          " , de_e_sogg_pag_indirizzo_pagatore" +
          " , de_e_sogg_pag_civico_pagatore" +
          " , cod_e_sogg_pag_cap_pagatore" +
          " , de_e_sogg_pag_localita_pagatore" +
          " , de_e_sogg_pag_provincia_pagatore" +
          " , cod_e_sogg_pag_nazione_pagatore" +
          " , de_e_sogg_pag_email_pagatore" +
          " , cod_e_dati_pag_codice_esito_pagamento" +
          " , num_e_dati_pag_importo_totale_pagato" +
          " , cod_e_dati_pag_id_univoco_versamento" +
          " , cod_e_dati_pag_codice_contesto_pagamento" +
          " , num_e_dati_pag_dati_sing_pag_singolo_importo_pagato" +
          " , de_e_dati_pag_dati_sing_pag_esito_singolo_pagamento" +
          " , dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento" +
          " , cod_e_dati_pag_dati_sing_pag_id_univoco_riscoss" +
          " , de_e_dati_pag_dati_sing_pag_causale_versamento" +
          " , de_e_dati_pag_dati_sing_pag_dati_specifici_riscossione" +
          " , cod_tipo_dovuto" +
          " , dt_ultimo_cambio_stato" +
          " , modello_pagamento" +
          " , ente_sil_invia_risposta_pagamento_url" +
          " , de_rt_inviart_tipo_firma" +
          " , blb_rt_payload" +
          " , indice_dati_singolo_pagamento" +
          " , num_e_dati_pag_dati_sing_pag_commissioni_applicate_psp" +
          " , cod_e_dati_pag_dati_sing_pag_allegato_ricevuta_tipo" +
          " , blb_e_dati_pag_dati_sing_pag_allegato_ricevuta_test" +
          " , cod_rp_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo" +
          " , cod_rp_dati_vers_dati_sing_vers_dati_mbd_hash_documento" +
          " , cod_rp_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza" +
          " , de_rp_dati_vers_dati_sing_vers_causale_versamento_agid" +
          " , bilancio" +
          " , cod_rp_silinviarp_original_fault_code" +
          " , de_rp_silinviarp_original_fault_string" +
          " , de_rp_silinviarp_original_fault_description" +
          " , cod_e_silinviaesito_original_fault_code" +
          " , de_e_silinviaesito_original_fault_string" +
          " , de_e_silinviaesito_original_fault_description" +
          " ) values ( " +
          "   nextval('mygov_dovuto_elaborato_mygov_dovuto_elaborato_id_seq')"+
          " , :d.version" +
          " , :d.flgDovutoAttuale" +
          " , :d.mygovFlussoId.mygovFlussoId" +
          " , :d.numRigaFlusso" +
          " , :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          " , :d.mygovCarrelloId?.mygovCarrelloId" +
          " , :d.codIud" +
          " , :d.codIuv" +
          " , :d.codAckRp" +
          " , :d.dtCreazione" +
          " , :d.dtUltimaModificaRp" +
          " , :d.dtUltimaModificaE" +
          " , :d.codRpSilinviarpIdPsp" +
          " , :d.codRpSilinviarpIdIntermediarioPsp" +
          " , :d.codRpSilinviarpIdCanale" +
          " , :d.codRpSilinviarpIdDominio" +
          " , :d.codRpSilinviarpIdUnivocoVersamento" +
          " , :d.codRpSilinviarpCodiceContestoPagamento" +
          " , :d.deRpSilinviarpEsito" +
          " , :d.codRpSilinviarpRedirect" +
          " , :d.codRpSilinviarpUrl" +
          " , :d.codRpSilinviarpFaultCode" +
          " , :d.deRpSilinviarpFaultString" +
          " , :d.codRpSilinviarpId" +
          " , :d.deRpSilinviarpDescription" +
          " , :d.codRpSilinviarpSerial" +
          " , :d.deRpVersioneOggetto" +
          " , :d.codRpDomIdDominio" +
          " , :d.codRpDomIdStazioneRichiedente" +
          " , :d.codRpIdMessaggioRichiesta" +
          " , :d.dtRpDataOraMessaggioRichiesta" +
          " , :d.codRpAutenticazioneSoggetto" +
          " , :d.codRpSoggVersIdUnivVersTipoIdUnivoco" +
          " , :d.codRpSoggVersIdUnivVersCodiceIdUnivoco" +
          " , :d.codRpSoggVersAnagraficaVersante" +
          " , :d.deRpSoggVersIndirizzoVersante" +
          " , :d.deRpSoggVersCivicoVersante" +
          " , :d.codRpSoggVersCapVersante" +
          " , left(:d.deRpSoggVersLocalitaVersante,"+Dovuto.MAX_LENGTH_LOCALITA+")" +
          " , :d.deRpSoggVersProvinciaVersante" +
          " , :d.codRpSoggVersNazioneVersante" +
          " , :d.deRpSoggVersEmailVersante" +
          " , :d.codRpSoggPagIdUnivPagTipoIdUnivoco" +
          " , :d.codRpSoggPagIdUnivPagCodiceIdUnivoco" +
          " , :d.deRpSoggPagAnagraficaPagatore" +
          " , :d.deRpSoggPagIndirizzoPagatore" +
          " , :d.deRpSoggPagCivicoPagatore" +
          " , :d.codRpSoggPagCapPagatore" +
          " , left(:d.deRpSoggPagLocalitaPagatore,"+Dovuto.MAX_LENGTH_LOCALITA+")" +
          " , :d.deRpSoggPagProvinciaPagatore" +
          " , :d.codRpSoggPagNazionePagatore" +
          " , :d.deRpSoggPagEmailPagatore" +
          " , :d.dtRpDatiVersDataEsecuzionePagamento" +
          " , :d.numRpDatiVersImportoTotaleDaVersare" +
          " , :d.codRpDatiVersTipoVersamento" +
          " , :d.codRpDatiVersIdUnivocoVersamento" +
          " , :d.codRpDatiVersCodiceContestoPagamento" +
          " , :d.deRpDatiVersIbanAddebito" +
          " , :d.deRpDatiVersBicAddebito" +
          " , :d.numRpDatiVersDatiSingVersImportoSingoloVersamento" +
          " , :d.numRpDatiVersDatiSingVersCommissioneCaricoPa" +
          " , :d.codRpDatiVersDatiSingVersIbanAccredito" +
          " , :d.codRpDatiVersDatiSingVersBicAccredito" +
          " , :d.codRpDatiVersDatiSingVersIbanAppoggio" +
          " , :d.codRpDatiVersDatiSingVersBicAppoggio" +
          " , :d.codRpDatiVersDatiSingVersCredenzialiPagatore" +
          " , :d.deRpDatiVersDatiSingVersCausaleVersamento" +
          " , :d.deRpDatiVersDatiSingVersDatiSpecificiRiscossione" +
          " , :d.codESilinviaesitoIdDominio" +
          " , :d.codESilinviaesitoIdUnivocoVersamento" +
          " , :d.codESilinviaesitoCodiceContestoPagamento" +
          " , :d.deESilinviaesitoEsito" +
          " , :d.codESilinviaesitoFaultCode" +
          " , :d.deESilinviaesitoFaultString" +
          " , :d.codESilinviaesitoId" +
          " , :d.deESilinviaesitoDescription" +
          " , :d.codESilinviaesitoSerial" +
          " , :d.deEVersioneOggetto" +
          " , :d.codEDomIdDominio" +
          " , :d.codEDomIdStazioneRichiedente" +
          " , :d.codEIdMessaggioRicevuta" +
          " , :d.codEDataOraMessaggioRicevuta" +
          " , :d.codERiferimentoMessaggioRichiesta" +
          " , :d.codERiferimentoDataRichiesta" +
          " , :d.codEIstitAttIdUnivAttTipoIdUnivoco" +
          " , :d.codEIstitAttIdUnivAttCodiceIdUnivoco" +
          " , :d.deEIstitAttDenominazioneAttestante" +
          " , :d.codEIstitAttCodiceUnitOperAttestante" +
          " , :d.deEIstitAttDenomUnitOperAttestante" +
          " , :d.deEIstitAttIndirizzoAttestante" +
          " , :d.deEIstitAttCivicoAttestante" +
          " , :d.codEIstitAttCapAttestante" +
          " , :d.deEIstitAttLocalitaAttestante" +
          " , :d.deEIstitAttProvinciaAttestante" +
          " , :d.codEIstitAttNazioneAttestante" +
          " , :d.codEEnteBenefIdUnivBenefTipoIdUnivoco" +
          " , :d.codEEnteBenefIdUnivBenefCodiceIdUnivoco" +
          " , :d.deEEnteBenefDenominazioneBeneficiario" +
          " , :d.codEEnteBenefCodiceUnitOperBeneficiario" +
          " , :d.deEEnteBenefDenomUnitOperBeneficiario" +
          " , :d.deEEnteBenefIndirizzoBeneficiario" +
          " , :d.deEEnteBenefCivicoBeneficiario" +
          " , :d.codEEnteBenefCapBeneficiario" +
          " , :d.deEEnteBenefLocalitaBeneficiario" +
          " , :d.deEEnteBenefProvinciaBeneficiario" +
          " , :d.codEEnteBenefNazioneBeneficiario" +
          " , :d.codESoggVersIdUnivVersTipoIdUnivoco" +
          " , :d.codESoggVersIdUnivVersCodiceIdUnivoco" +
          " , :d.codESoggVersAnagraficaVersante" +
          " , :d.deESoggVersIndirizzoVersante" +
          " , :d.deESoggVersCivicoVersante" +
          " , :d.codESoggVersCapVersante" +
          " , :d.deESoggVersLocalitaVersante" +
          " , :d.deESoggVersProvinciaVersante" +
          " , :d.codESoggVersNazioneVersante" +
          " , :d.deESoggVersEmailVersante" +
          " , :d.codESoggPagIdUnivPagTipoIdUnivoco" +
          " , :d.codESoggPagIdUnivPagCodiceIdUnivoco" +
          " , :d.codESoggPagAnagraficaPagatore" +
          " , :d.deESoggPagIndirizzoPagatore" +
          " , :d.deESoggPagCivicoPagatore" +
          " , :d.codESoggPagCapPagatore" +
          " , :d.deESoggPagLocalitaPagatore" +
          " , :d.deESoggPagProvinciaPagatore" +
          " , :d.codESoggPagNazionePagatore" +
          " , :d.deESoggPagEmailPagatore" +
          " , :d.codEDatiPagCodiceEsitoPagamento" +
          " , :d.numEDatiPagImportoTotalePagato" +
          " , :d.codEDatiPagIdUnivocoVersamento" +
          " , :d.codEDatiPagCodiceContestoPagamento" +
          " , :d.numEDatiPagDatiSingPagSingoloImportoPagato" +
          " , :d.deEDatiPagDatiSingPagEsitoSingoloPagamento" +
          " , :d.dtEDatiPagDatiSingPagDataEsitoSingoloPagamento" +
          " , :d.codEDatiPagDatiSingPagIdUnivocoRiscoss" +
          " , :d.deEDatiPagDatiSingPagCausaleVersamento" +
          " , :d.deEDatiPagDatiSingPagDatiSpecificiRiscossione" +
          " , :d.codTipoDovuto" +
          " , :d.dtUltimoCambioStato" +
          " , :d.modelloPagamento" +
          " , :d.enteSilInviaRispostaPagamentoUrl" +
          " , :d.deRtInviartTipoFirma" +
          " , :d.blbRtPayload" +
          " , :d.indiceDatiSingoloPagamento" +
          " , :d.numEDatiPagDatiSingPagCommissioniApplicatePsp" +
          " , :d.codEDatiPagDatiSingPagAllegatoRicevutaTipo" +
          " , :d.blbEDatiPagDatiSingPagAllegatoRicevutaTest" +
          " , :d.codRpDatiVersDatiSingVersDatiMbdTipoBollo" +
          " , :d.codRpDatiVersDatiSingVersDatiMbdHashDocumento" +
          " , :d.codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza" +
          " , :d.deRpDatiVersDatiSingVersCausaleVersamentoAgid" +
          " , :d.bilancio" +
          " , :d.codRpSilinviarpOriginalFaultCode" +
          " , :d.deRpSilinviarpOriginalFaultString" +
          " , :d.deRpSilinviarpOriginalFaultDescription" +
          " , :d.codESilinviaesitoOriginalFaultCode" +
          " , :d.deESilinviaesitoOriginalFaultString" +
          " , :d.deESilinviaesitoOriginalFaultDescription" +
          " )"
  )
  @GetGeneratedKeys("mygov_dovuto_elaborato_id")
  long insert(@BindBean("d") DovutoElaborato d);

  // The "100X" numbers at head of select list on following queries are just used to easily
  // identify SQL statements when debugging locks using PostgreSQL tables pg_locks / pg_stat_activity
  // where the "query" field is cut after first N characters
  @SqlQuery(
      "    select 1001,"+DovutoElaborato.ALIAS+ALL_FIELDS+", "+Flusso.FIELDS+", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS+", "+Carrello.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  left join mygov_carrello " + Carrello.ALIAS +
          "    on "+Carrello.ALIAS+".mygov_carrello_id = "+DovutoElaborato.ALIAS+".mygov_carrello_id " +
          "  where "+DovutoElaborato.ALIAS + ".mygov_dovuto_elaborato_id = :id"
  )
  @RegisterFieldMapper(DovutoElaborato.class)
  DovutoElaborato getById(Long id);

  @SqlQuery(
      "    select count(*)" +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where ("+ DovutoElaborato.ALIAS+".cod_tipo_dovuto = :codTipoDovuto or :codTipoDovuto is null)" +
          "   and ("+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte or :codIpaEnte is null)"
  )
  long count(String codIpaEnte, String codTipoDovuto);

  @SqlQuery(
      "    select 1002,"+DovutoElaborato.ALIAS+ALL_FIELDS+", "+Flusso.FIELDS+", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS+", "+Carrello.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  left join mygov_carrello " + Carrello.ALIAS +
          "    on "+Carrello.ALIAS+".mygov_carrello_id = "+DovutoElaborato.ALIAS+".mygov_carrello_id " +
          "  where "+DovutoElaborato.ALIAS+".mygov_carrello_id = :carrelloId"
  )
  @RegisterFieldMapper(DovutoElaborato.class)
  List<DovutoElaborato> getByCarrelloId(Long carrelloId);

  @SqlQuery(
      "    select 1003,"+DovutoElaborato.ALIAS+ALL_FIELDS+", "+Flusso.FIELDS+", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS+", "+Carrello.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  left join mygov_carrello " + Carrello.ALIAS +
          "    on "+Carrello.ALIAS+".mygov_carrello_id = "+DovutoElaborato.ALIAS+".mygov_carrello_id " +
          "  where "+Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte " +
          "    and "+DovutoElaborato.ALIAS+".cod_iud = :codIud "
  )
  @RegisterFieldMapper(DovutoElaborato.class)
  List<DovutoElaborato> getByIudEnte(String codIud, String codIpaEnte);

  @SqlQuery(
      "    select 1004,"+DovutoElaborato.ALIAS+ALL_FIELDS+", "+Flusso.FIELDS+", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS+", "+Carrello.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  left join mygov_carrello " + Carrello.ALIAS +
          "    on "+Carrello.ALIAS+".mygov_carrello_id = "+DovutoElaborato.ALIAS+".mygov_carrello_id " +
          "  where "+Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte " +
          "    and "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id != :idStatoDovutoAnnullato " +
          "    and "+DovutoElaborato.ALIAS+".cod_rp_silinviarp_id_univoco_versamento = :codIuv "
  )
  @RegisterFieldMapper(DovutoElaborato.class)
  List<DovutoElaborato> getByIuvEnte(String codIuv, String codIpaEnte, Long idStatoDovutoAnnullato);

  @SqlQuery(
      "    select 1005,"+DovutoElaborato.ALIAS+ALL_FIELDS+", "+Flusso.FIELDS+", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS+", "+Carrello.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  left join mygov_carrello " + Carrello.ALIAS +
          "    on "+Carrello.ALIAS+".mygov_carrello_id = "+DovutoElaborato.ALIAS+".mygov_carrello_id " +
          "  where "+Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte " +
          "    and "+DovutoElaborato.ALIAS+".cod_iuv = :iuv " +
          "    and (("+AnagraficaStato.ALIAS+".cod_stato = :codStato  and :inStato = true) " +
          "        or ("+AnagraficaStato.ALIAS+".cod_stato <> :codStato  and :inStato = false)) " +
          "    and "+AnagraficaStato.ALIAS+".de_tipo_stato = 'dovuto'" +
          "    and ("+DovutoElaborato.ALIAS+".mygov_carrello_id = :carrelloId or :carrelloId is null)"
  )
  @RegisterFieldMapper(DovutoElaborato.class)
  List<DovutoElaborato> getByIuvEnteStato(String iuv, String codIpaEnte, String codStato, boolean inStato, Long carrelloId);

  @SqlQuery(
      "    select 1006,"+DovutoElaborato.ALIAS+ALL_FIELDS+", "+Flusso.FIELDS+", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS+", "+Carrello.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  left join mygov_carrello " + Carrello.ALIAS +
          "    on "+Carrello.ALIAS+".mygov_carrello_id = "+DovutoElaborato.ALIAS+".mygov_carrello_id " +
          "  where ("+Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte or coalesce(:codIpaEnte, '') = '')" +
          "    and "+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :tipoIdUnivoco" +
          "    and "+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco = :idUnivoco" +
          "    and "+DovutoElaborato.ALIAS+".cod_e_dati_pag_codice_esito_pagamento = '"+Constants.CODICE_ESITO_PAGAMENTO_ESEGUITO+"'" +
          "    and "+AnagraficaStato.ALIAS+".cod_stato = '"+Constants.STATO_DOVUTO_COMPLETATO+"'" +
          "    and "+AnagraficaStato.ALIAS+".de_tipo_stato = '"+Constants.STATO_TIPO_DOVUTO+"'" +
          "    and ("+DovutoElaborato.ALIAS+".dt_ultima_modifica_e > :from and "+DovutoElaborato.ALIAS+".dt_ultima_modifica_e <= :to)" +
          " order by "+DovutoElaborato.ALIAS+".dt_ultimo_cambio_stato desc"+
          "        , "+DovutoElaborato.ALIAS+".mygov_dovuto_elaborato_id desc"
  )
  @RegisterFieldMapper(DovutoElaborato.class)
  List<DovutoElaborato> getByIdUnivocoPersonaEnteIntervalloData(String tipoIdUnivoco, String idUnivoco, String codIpaEnte, Date from, Date to);

  String SQL_SEARCH_DOVUTO_ELABORATO =
      "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where (:codIpaEnte is null or "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte ) " +
          "   and (lower("+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
          "     or lower("+DovutoElaborato.ALIAS+".cod_rp_sogg_vers_id_univ_vers_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) ) " +
          "   and coalesce("+DovutoElaborato.ALIAS+".dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento, " +
          "                "+DovutoElaborato.ALIAS+".dt_ultimo_cambio_stato) between :from and :to " +
          "   and (:codTipoDovuto is null or " +
          "        "+DovutoElaborato.ALIAS+".cod_tipo_dovuto = :codTipoDovuto ) " +
          "   and (:causale is null or " +
          "        coalesce("+DovutoElaborato.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento_agid, " +
          "                 "+DovutoElaborato.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento) ilike '%' || :causale || '%' ) ";

  @SqlQuery(
      "    select 1007," + DovutoElaborato.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + ", " + Ente.FIELDS +
          SQL_SEARCH_DOVUTO_ELABORATO +
          " order by "+DovutoElaborato.ALIAS+".dt_ultimo_cambio_stato desc " +
          " limit <queryLimit>"
  )
  @RegisterFieldMapper(DovutoElaborato.class)
  List<DovutoElaborato> searchDovutoElaborato(String codIpaEnte, String idUnivocoPagatoreVersante,
                                              LocalDate from, LocalDate to, String causale, String codTipoDovuto, @Define int queryLimit);

  @SqlQuery(
      "    select count(1)" +
          SQL_SEARCH_DOVUTO_ELABORATO)
  @RegisterFieldMapper(DovutoElaborato.class)
  int searchDovutoElaboratoCount(String codIpaEnte, String idUnivocoPagatoreVersante,
                                 LocalDate from, LocalDate to, String causale, String codTipoDovuto);

  @SqlQuery(
      "    select 1 " +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte " +
          "   and "+AnagraficaStato.ALIAS+".cod_stato='COMPLETATO' " +
          "   and "+DovutoElaborato.ALIAS+".cod_e_dati_pag_codice_esito_pagamento <> '"+Constants.CODICE_ESITO_PAGAMENTO_KO+"' " +  // only consider successful payments
          "   and coalesce("+DovutoElaborato.ALIAS+".dt_ultima_modifica_e, " +
          "                "+DovutoElaborato.ALIAS+".dt_creazione) between now() - interval '1 day' and now() " + //only last 24 hours
          "   and "+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :tipoIdUnivocoPagatoreVersante " +
          "   and lower("+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
          "   and "+DovutoElaborato.ALIAS+".cod_tipo_dovuto = :codTipoDovuto " +
          "   and "+DovutoElaborato.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento = :causale " +
          " limit 1")
  Optional<Boolean> hasReplicaDovutoElaborato(String codIpaEnte, char tipoIdUnivocoPagatoreVersante, String idUnivocoPagatoreVersante,
                                              String causale, String codTipoDovuto);

  @SqlQuery(
      "    select 1008," + DovutoElaborato.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + ", " + Ente.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where ( lower("+DovutoElaborato.ALIAS+".cod_rp_silinviarp_id_univoco_versamento)=lower(:iuv)" +
          "         or lower("+DovutoElaborato.ALIAS+".cod_iuv)=lower(:iuv) )" +
          "   and lower("+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatore) " +
          "   and (:anagraficaPagatore is null or lower("+DovutoElaborato.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore) = lower(:anagraficaPagatore) ) " +
          " order by "+DovutoElaborato.ALIAS+".mygov_dovuto_elaborato_id desc")
  @RegisterFieldMapper(DovutoElaborato.class)
  List<DovutoElaborato> searchDovutoElaboratoByIuvIdPagatore(String iuv, String idUnivocoPagatore, String anagraficaPagatore);

  @SqlQuery(
      "    select 1009," + DovutoElaborato.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + ", " + Ente.FIELDS +
          "     , " + DovutoElaborato.ALIAS + ".mygov_carrello_id as " + Carrello.ALIAS + "_mygov_carrello_id " +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte " +
          "   and ( lower("+DovutoElaborato.ALIAS+".cod_rp_silinviarp_id_univoco_versamento)=lower(:iuv)" +
          "         or lower("+DovutoElaborato.ALIAS+".cod_iuv)=lower(:iuv) )")
  @RegisterFieldMapper(DovutoElaborato.class)
  List<DovutoElaborato> searchDovutoElaboratoByIuvEnte(String iuv, String codIpaEnte);

  String QUERY_SEARCH_DOVUTO_ELABORATO_ARCHIVIO =
      " from mygov_dovuto_elaborato "+DovutoElaborato.ALIAS+"" +
          " inner join mygov_anagrafica_stato "+AnagraficaStato.ALIAS +
          "   on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id="+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id" +
          " inner join mygov_flusso "+Flusso.ALIAS +
          "   on "+DovutoElaborato.ALIAS+".mygov_flusso_id="+Flusso.ALIAS+".mygov_flusso_id" +
          " inner join mygov_ente "+Ente.ALIAS +
          "   on "+Flusso.ALIAS+".mygov_ente_id="+Ente.ALIAS+".mygov_ente_id" +
          " where "+Ente.ALIAS+".mygov_ente_id =:mygovEnteId" +
          " and (:codStato is null or "+AnagraficaStato.ALIAS+".cod_stato= :codStato)" +
          " and (:codStato is null or "+AnagraficaStato.ALIAS+".de_tipo_stato='dovuto')" +
          " and (" +
          "       :importoCod is null"+
          "   or (:importoCod = 'eqZero' and num_e_dati_pag_dati_sing_pag_singolo_importo_pagato = 0)"+
          "   or (:importoCod = 'gtZero' and num_e_dati_pag_dati_sing_pag_singolo_importo_pagato > 0)"+
          "   )"+
          " and ("+DovutoElaborato.ALIAS+".cod_tipo_dovuto in (<listCodTipoDovuto>))"+
          " and (:nomeFlusso is null or "+Flusso.ALIAS+".iuf = :nomeFlusso)" +
          " and ( :from::DATE <= "+DovutoElaborato.ALIAS+".dt_ultimo_cambio_stato and "+DovutoElaborato.ALIAS+".dt_ultimo_cambio_stato < :to::DATE)" +
          " and (:causale is null" +
          "   or (" +
          "        "+DovutoElaborato.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento_agid ilike '%' || :causale || '%' " +
          "        or (" +
          "                "+DovutoElaborato.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento_agid is null" +
          "          and "+DovutoElaborato.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento ilike '%' || :causale || '%' " +
          "      )" +
          "    )" +
          "  )" +
          "  and (:codiceFiscale is null or "+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco ilike '%' || :codiceFiscale || '%')" +
          "  and (:codIud is null or "+DovutoElaborato.ALIAS+".cod_iud ilike '%' || :codIud || '%')" +
          "  and (:codIuv is null or" +
          "    (" +
          "      lower("+DovutoElaborato.ALIAS+".cod_rp_silinviarp_id_univoco_versamento)=lower(:codIuv)" +
          "      or lower("+DovutoElaborato.ALIAS+".cod_iuv)=lower(:codIuv)" +
          "    )" +
          "  )";

  @SqlQuery(
      " select "+ DovutoElaborato.ALIAS+".mygov_dovuto_elaborato_id"+
          ","+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco "+
          ","+DovutoElaborato.ALIAS+".cod_iud "+
          ","+DovutoElaborato.ALIAS+".cod_iuv "+
          ","+DovutoElaborato.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento "+
          //","+DovutoElaborato.ALIAS+".de_causale_visualizzata "+
          ","+DovutoElaborato.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento_agid  as de_causale_visualizzata "+
          ","+DovutoElaborato.ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "+
          ","+DovutoElaborato.ALIAS+".num_e_dati_pag_dati_sing_pag_singolo_importo_pagato "+
          ","+DovutoElaborato.ALIAS+".dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento "+
          ","+DovutoElaborato.ALIAS+".dt_ultimo_cambio_stato "+
          ","+DovutoElaborato.ALIAS+".cod_e_id_messaggio_ricevuta" +
          ","+DovutoElaborato.ALIAS+".cod_tipo_dovuto "+
          ","+DovutoElaborato.ALIAS+".dt_ultima_modifica_rp "+
          ","+DovutoElaborato.ALIAS+".cod_rp_silinviarp_id_univoco_versamento "+
          ","+DovutoElaborato.ALIAS+".cod_rp_silinviarp_codice_contesto_pagamento "+
          ","+DovutoElaborato.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore "+
          ","+DovutoElaborato.ALIAS+".de_e_istit_att_denominazione_attestante "+
          ","+DovutoElaborato.ALIAS+".cod_e_istit_att_id_univ_att_codice_id_univoco "+
          ","+AnagraficaStato.ALIAS+".cod_stato "+
          ","+AnagraficaStato.ALIAS+".de_stato "+
          ","+Ente.ALIAS+".cod_ipa_ente "+
          ","+Flusso.ALIAS+".iuf "+
          ", 'pagato' as search_type "+
          QUERY_SEARCH_DOVUTO_ELABORATO_ARCHIVIO +
          " order by" +
          " "+DovutoElaborato.ALIAS+".dt_ultimo_cambio_stato desc," +
          " "+DovutoElaborato.ALIAS+".mygov_dovuto_elaborato_id desc " +
          " limit <queryLimit>"
  )
  List<DovutoOperatoreTo> searchDovutoElaboratoNellArchivio(Long mygovEnteId, String codStato, String importoCod,
                                                            @BindList(onEmpty= BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovuto,
                                                            String nomeFlusso, LocalDate from, LocalDate to, String codiceFiscale,
                                                            String causale, String codIud, String codIuv, @Define int queryLimit);

  @SqlQuery(
      " select count(1) "+
          QUERY_SEARCH_DOVUTO_ELABORATO_ARCHIVIO
  )
  int searchDovutoElaboratoNellArchivioCount(Long mygovEnteId, String codStato, String importoCod,
                                             @BindList(onEmpty= BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovuto,
                                             String nomeFlusso, LocalDate from, LocalDate to, String codiceFiscale,
                                             String causale, String codIud, String codIuv);

  @SqlQuery(
      "    select 1010," + DovutoElaborato.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + ", " + Ente.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where (lower("+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
          "         or lower("+DovutoElaborato.ALIAS+".cod_rp_sogg_vers_id_univ_vers_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) ) " +
          "   and "+AnagraficaStato.ALIAS+".cod_stato = '" + AnagraficaStato.STATO_DOVUTO_COMPLETATO + "'" +
          " order by "+DovutoElaborato.ALIAS+".dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento desc" +
          " limit :num " )
  @RegisterFieldMapper(DovutoElaborato.class)
  List<DovutoElaborato> searchLastDovutoElaborato(String idUnivocoPagatoreVersante, Integer num);

  @SqlQuery(
      "    select 1011,"+DovutoElaborato.ALIAS+ALL_FIELDS+", "+Flusso.FIELDS+", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS+", "+Carrello.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  left join mygov_carrello " + Carrello.ALIAS +
          "    on "+Carrello.ALIAS+".mygov_carrello_id = "+DovutoElaborato.ALIAS+".mygov_carrello_id " +
          " where "+DovutoElaborato.ALIAS+".mygov_dovuto_elaborato_id = :id " +
          "   and ("+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :tipoIdUnivocoPagatoreVersante " +
          "            and "+DovutoElaborato.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco = :idUnivocoPagatoreVersante " +
          "         or "+DovutoElaborato.ALIAS+".cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco = :tipoIdUnivocoPagatoreVersante " +
          "            and "+DovutoElaborato.ALIAS+".cod_rp_sogg_vers_id_univ_vers_codice_id_univoco = :idUnivocoPagatoreVersante ) " )
  @RegisterFieldMapper(DovutoElaborato.class)
  DovutoElaborato getByIdAndUser(Long id, char tipoIdUnivocoPagatoreVersante, String idUnivocoPagatoreVersante);

  @SqlCall("{call insert_mygov_dovuto_elaborato_noinout( "+
      ":n_mygov_ente_id, "+
      ":n_mygov_flusso_id, "+
      "cast(:n_num_riga_flusso as numeric), "+
      ":n_mygov_anagrafica_stato_id, "+
      ":n_mygov_carrello_id, "+
      ":n_cod_iud, "+
      ":n_cod_iuv, "+
      "cast(:n_dt_creazione as timestamp without time zone), "+
      ":n_de_rp_versione_oggetto, "+
      "cast(:n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as bpchar), "+
      ":n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, "+
      ":n_de_rp_sogg_pag_anagrafica_pagatore, "+
      ":n_de_rp_sogg_pag_indirizzo_pagatore, "+
      ":n_de_rp_sogg_pag_civico_pagatore, "+
      ":n_cod_rp_sogg_pag_cap_pagatore, "+
      "left(:n_de_rp_sogg_pag_localita_pagatore,"+Dovuto.MAX_LENGTH_LOCALITA+"), "+
      ":n_de_rp_sogg_pag_provincia_pagatore, "+
      ":n_cod_rp_sogg_pag_nazione_pagatore, "+
      ":n_de_rp_sogg_pag_email_pagatore, "+
      "cast(:n_dt_rp_dati_vers_data_esecuzione_pagamento as date), "+
      ":n_cod_rp_dati_vers_tipo_versamento, "+
      "cast(:n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as numeric), "+
      "cast(:n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as numeric), "+
      ":n_cod_tipo_dovuto, "+
      ":n_de_rp_dati_vers_dati_sing_vers_causale_versamento, "+
      ":n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, "+
      ":n_mygov_utente_id, "+
      ":insert_avv_dig, "+
      ":result, "+
      ":result_desc "+
      ")}" )
  @OutParameter(name = "result", sqlType = Types.VARCHAR)
  @OutParameter(name = "result_desc", sqlType = Types.VARCHAR)
  OutParameters callAnnullaFunction(Long n_mygov_ente_id, Long n_mygov_flusso_id, Integer n_num_riga_flusso, Long n_mygov_anagrafica_stato_id,
                                    Long n_mygov_carrello_id, String n_cod_iud, String n_cod_iuv, Date n_dt_creazione, String n_de_rp_versione_oggetto,
                                    String n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, String n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
                                    String n_de_rp_sogg_pag_anagrafica_pagatore, String n_de_rp_sogg_pag_indirizzo_pagatore, String n_de_rp_sogg_pag_civico_pagatore,
                                    String n_cod_rp_sogg_pag_cap_pagatore, String n_de_rp_sogg_pag_localita_pagatore, String n_de_rp_sogg_pag_provincia_pagatore,
                                    String n_cod_rp_sogg_pag_nazione_pagatore, String n_de_rp_sogg_pag_email_pagatore,
                                    Date n_dt_rp_dati_vers_data_esecuzione_pagamento, String n_cod_rp_dati_vers_tipo_versamento,
                                    Double n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, Double n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa,
                                    String n_cod_tipo_dovuto, String n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
                                    String n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, Long n_mygov_utente_id, boolean insert_avv_dig);

  @SqlQuery(
      "    select 1012,"+DovutoElaborato.ALIAS+ALL_FIELDS+", "+Flusso.FIELDS+", "+AnagraficaStato.FIELDS+", "+Ente.FIELDS+", "+Carrello.FIELDS +
          "  from mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_flusso " + Flusso.ALIAS +
          "    on "+DovutoElaborato.ALIAS+".mygov_flusso_id = "+Flusso.ALIAS+".mygov_flusso_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Flusso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  left join mygov_carrello " + Carrello.ALIAS +
          "    on "+Carrello.ALIAS+".mygov_carrello_id = "+DovutoElaborato.ALIAS+".mygov_carrello_id " +
          "  where "+DovutoElaborato.ALIAS + ".cod_rp_id_messaggio_richiesta = :idMessaggioRichiesta"
  )
  @RegisterFieldMapper(DovutoElaborato.class)
  Optional<DovutoElaborato> getByCodRpIdMessaggioRichiesta(String idMessaggioRichiesta);
}
