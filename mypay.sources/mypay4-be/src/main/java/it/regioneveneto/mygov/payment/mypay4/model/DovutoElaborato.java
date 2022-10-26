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
package it.regioneveneto.mygov.payment.mypay4.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.mapper.Nested;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovDovutoElaboratoId")
public class DovutoElaborato extends BaseEntity {

  public final static char TIPO_SOGGETTO_PERSONA_FISICA = 'F';

  public final static String ALIAS = "DovutoElaborato";
  public final static String FIELDS = ""+ALIAS+".mygov_dovuto_elaborato_id as DovutoElaborato_mygovDovutoElaboratoId"+
      ","+ALIAS+".version as DovutoElaborato_version,"+ALIAS+".flg_dovuto_attuale as DovutoElaborato_flgDovutoAttuale"+
      ","+ALIAS+".mygov_flusso_id as DovutoElaborato_mygovFlussoId,"+ALIAS+".num_riga_flusso as DovutoElaborato_numRigaFlusso"+
      ","+ALIAS+".mygov_anagrafica_stato_id as DovutoElaborato_mygovAnagraficaStatoId"+
      ","+ALIAS+".mygov_carrello_id as DovutoElaborato_mygovCarrelloId,"+ALIAS+".cod_iud as DovutoElaborato_codIud"+
      ","+ALIAS+".cod_iuv as DovutoElaborato_codIuv,"+ALIAS+".cod_ack_rp as DovutoElaborato_codAckRp"+
      ","+ALIAS+".dt_creazione as DovutoElaborato_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica_rp as DovutoElaborato_dtUltimaModificaRp"+
      ","+ALIAS+".dt_ultima_modifica_e as DovutoElaborato_dtUltimaModificaE"+
      ","+ALIAS+".cod_rp_silinviarp_id_psp as DovutoElaborato_codRpSilinviarpIdPsp"+
      ","+ALIAS+".cod_rp_silinviarp_id_intermediario_psp as DovutoElaborato_codRpSilinviarpIdIntermediarioPsp"+
      ","+ALIAS+".cod_rp_silinviarp_id_canale as DovutoElaborato_codRpSilinviarpIdCanale"+
      ","+ALIAS+".cod_rp_silinviarp_id_dominio as DovutoElaborato_codRpSilinviarpIdDominio"+
      ","+ALIAS+".cod_rp_silinviarp_id_univoco_versamento as DovutoElaborato_codRpSilinviarpIdUnivocoVersamento"+
      ","+ALIAS+".cod_rp_silinviarp_codice_contesto_pagamento as DovutoElaborato_codRpSilinviarpCodiceContestoPagamento"+
      ","+ALIAS+".de_rp_silinviarp_esito as DovutoElaborato_deRpSilinviarpEsito"+
      ","+ALIAS+".cod_rp_silinviarp_redirect as DovutoElaborato_codRpSilinviarpRedirect"+
      ","+ALIAS+".cod_rp_silinviarp_url as DovutoElaborato_codRpSilinviarpUrl"+
      ","+ALIAS+".cod_rp_silinviarp_fault_code as DovutoElaborato_codRpSilinviarpFaultCode"+
      ","+ALIAS+".de_rp_silinviarp_fault_string as DovutoElaborato_deRpSilinviarpFaultString"+
      ","+ALIAS+".cod_rp_silinviarp_id as DovutoElaborato_codRpSilinviarpId"+
      ","+ALIAS+".de_rp_silinviarp_description as DovutoElaborato_deRpSilinviarpDescription"+
      ","+ALIAS+".cod_rp_silinviarp_serial as DovutoElaborato_codRpSilinviarpSerial"+
      ","+ALIAS+".de_rp_versione_oggetto as DovutoElaborato_deRpVersioneOggetto"+
      ","+ALIAS+".cod_rp_dom_id_dominio as DovutoElaborato_codRpDomIdDominio"+
      ","+ALIAS+".cod_rp_dom_id_stazione_richiedente as DovutoElaborato_codRpDomIdStazioneRichiedente"+
      ","+ALIAS+".cod_rp_id_messaggio_richiesta as DovutoElaborato_codRpIdMessaggioRichiesta"+
      ","+ALIAS+".dt_rp_data_ora_messaggio_richiesta as DovutoElaborato_dtRpDataOraMessaggioRichiesta"+
      ","+ALIAS+".cod_rp_autenticazione_soggetto as DovutoElaborato_codRpAutenticazioneSoggetto"+
      ","+ALIAS+".cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco as DovutoElaborato_codRpSoggVersIdUnivVersTipoIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_vers_id_univ_vers_codice_id_univoco as DovutoElaborato_codRpSoggVersIdUnivVersCodiceIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_vers_anagrafica_versante as DovutoElaborato_codRpSoggVersAnagraficaVersante"+
      ","+ALIAS+".de_rp_sogg_vers_indirizzo_versante as DovutoElaborato_deRpSoggVersIndirizzoVersante"+
      ","+ALIAS+".de_rp_sogg_vers_civico_versante as DovutoElaborato_deRpSoggVersCivicoVersante"+
      ","+ALIAS+".cod_rp_sogg_vers_cap_versante as DovutoElaborato_codRpSoggVersCapVersante"+
      ","+ALIAS+".de_rp_sogg_vers_localita_versante as DovutoElaborato_deRpSoggVersLocalitaVersante"+
      ","+ALIAS+".de_rp_sogg_vers_provincia_versante as DovutoElaborato_deRpSoggVersProvinciaVersante"+
      ","+ALIAS+".cod_rp_sogg_vers_nazione_versante as DovutoElaborato_codRpSoggVersNazioneVersante"+
      ","+ALIAS+".de_rp_sogg_vers_email_versante as DovutoElaborato_deRpSoggVersEmailVersante"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as DovutoElaborato_codRpSoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco as DovutoElaborato_codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".de_rp_sogg_pag_anagrafica_pagatore as DovutoElaborato_deRpSoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_indirizzo_pagatore as DovutoElaborato_deRpSoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_civico_pagatore as DovutoElaborato_deRpSoggPagCivicoPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_cap_pagatore as DovutoElaborato_codRpSoggPagCapPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_localita_pagatore as DovutoElaborato_deRpSoggPagLocalitaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_provincia_pagatore as DovutoElaborato_deRpSoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_nazione_pagatore as DovutoElaborato_codRpSoggPagNazionePagatore"+
      ","+ALIAS+".de_rp_sogg_pag_email_pagatore as DovutoElaborato_deRpSoggPagEmailPagatore"+
      ","+ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento as DovutoElaborato_dtRpDatiVersDataEsecuzionePagamento"+
      ","+ALIAS+".num_rp_dati_vers_importo_totale_da_versare as DovutoElaborato_numRpDatiVersImportoTotaleDaVersare"+
      ","+ALIAS+".cod_rp_dati_vers_tipo_versamento as DovutoElaborato_codRpDatiVersTipoVersamento"+
      ","+ALIAS+".cod_rp_dati_vers_id_univoco_versamento as DovutoElaborato_codRpDatiVersIdUnivocoVersamento"+
      ","+ALIAS+".cod_rp_dati_vers_codice_contesto_pagamento as DovutoElaborato_codRpDatiVersCodiceContestoPagamento"+
      ","+ALIAS+".de_rp_dati_vers_iban_addebito as DovutoElaborato_deRpDatiVersIbanAddebito"+
      ","+ALIAS+".de_rp_dati_vers_bic_addebito as DovutoElaborato_deRpDatiVersBicAddebito"+
      ","+ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as DovutoElaborato_numRpDatiVersDatiSingVersImportoSingoloVersamento"+
      ","+ALIAS+".num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as DovutoElaborato_numRpDatiVersDatiSingVersCommissioneCaricoPa"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_iban_accredito as DovutoElaborato_codRpDatiVersDatiSingVersIbanAccredito"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_bic_accredito as DovutoElaborato_codRpDatiVersDatiSingVersBicAccredito"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_iban_appoggio as DovutoElaborato_codRpDatiVersDatiSingVersIbanAppoggio"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_bic_appoggio as DovutoElaborato_codRpDatiVersDatiSingVersBicAppoggio"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore as DovutoElaborato_codRpDatiVersDatiSingVersCredenzialiPagatore"+
      ","+ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento as DovutoElaborato_deRpDatiVersDatiSingVersCausaleVersamento"+
      ","+ALIAS+".de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione as DovutoElaborato_deRpDatiVersDatiSingVersDatiSpecificiRiscossione"+
      ","+ALIAS+".cod_e_silinviaesito_id_dominio as DovutoElaborato_codESilinviaesitoIdDominio"+
      ","+ALIAS+".cod_e_silinviaesito_id_univoco_versamento as DovutoElaborato_codESilinviaesitoIdUnivocoVersamento"+
      ","+ALIAS+".cod_e_silinviaesito_codice_contesto_pagamento as DovutoElaborato_codESilinviaesitoCodiceContestoPagamento"+
      ","+ALIAS+".de_e_silinviaesito_esito as DovutoElaborato_deESilinviaesitoEsito"+
      ","+ALIAS+".cod_e_silinviaesito_fault_code as DovutoElaborato_codESilinviaesitoFaultCode"+
      ","+ALIAS+".de_e_silinviaesito_fault_string as DovutoElaborato_deESilinviaesitoFaultString"+
      ","+ALIAS+".cod_e_silinviaesito_id as DovutoElaborato_codESilinviaesitoId"+
      ","+ALIAS+".de_e_silinviaesito_description as DovutoElaborato_deESilinviaesitoDescription"+
      ","+ALIAS+".cod_e_silinviaesito_serial as DovutoElaborato_codESilinviaesitoSerial"+
      ","+ALIAS+".de_e_versione_oggetto as DovutoElaborato_deEVersioneOggetto"+
      ","+ALIAS+".cod_e_dom_id_dominio as DovutoElaborato_codEDomIdDominio"+
      ","+ALIAS+".cod_e_dom_id_stazione_richiedente as DovutoElaborato_codEDomIdStazioneRichiedente"+
      ","+ALIAS+".cod_e_id_messaggio_ricevuta as DovutoElaborato_codEIdMessaggioRicevuta"+
      ","+ALIAS+".cod_e_data_ora_messaggio_ricevuta as DovutoElaborato_codEDataOraMessaggioRicevuta"+
      ","+ALIAS+".cod_e_riferimento_messaggio_richiesta as DovutoElaborato_codERiferimentoMessaggioRichiesta"+
      ","+ALIAS+".cod_e_riferimento_data_richiesta as DovutoElaborato_codERiferimentoDataRichiesta"+
      ","+ALIAS+".cod_e_istit_att_id_univ_att_tipo_id_univoco as DovutoElaborato_codEIstitAttIdUnivAttTipoIdUnivoco"+
      ","+ALIAS+".cod_e_istit_att_id_univ_att_codice_id_univoco as DovutoElaborato_codEIstitAttIdUnivAttCodiceIdUnivoco"+
      ","+ALIAS+".de_e_istit_att_denominazione_attestante as DovutoElaborato_deEIstitAttDenominazioneAttestante"+
      ","+ALIAS+".cod_e_istit_att_codice_unit_oper_attestante as DovutoElaborato_codEIstitAttCodiceUnitOperAttestante"+
      ","+ALIAS+".de_e_istit_att_denom_unit_oper_attestante as DovutoElaborato_deEIstitAttDenomUnitOperAttestante"+
      ","+ALIAS+".de_e_istit_att_indirizzo_attestante as DovutoElaborato_deEIstitAttIndirizzoAttestante"+
      ","+ALIAS+".de_e_istit_att_civico_attestante as DovutoElaborato_deEIstitAttCivicoAttestante"+
      ","+ALIAS+".cod_e_istit_att_cap_attestante as DovutoElaborato_codEIstitAttCapAttestante"+
      ","+ALIAS+".de_e_istit_att_localita_attestante as DovutoElaborato_deEIstitAttLocalitaAttestante"+
      ","+ALIAS+".de_e_istit_att_provincia_attestante as DovutoElaborato_deEIstitAttProvinciaAttestante"+
      ","+ALIAS+".cod_e_istit_att_nazione_attestante as DovutoElaborato_codEIstitAttNazioneAttestante"+
      ","+ALIAS+".cod_e_ente_benef_id_univ_benef_tipo_id_univoco as DovutoElaborato_codEEnteBenefIdUnivBenefTipoIdUnivoco"+
      ","+ALIAS+".cod_e_ente_benef_id_univ_benef_codice_id_univoco as DovutoElaborato_codEEnteBenefIdUnivBenefCodiceIdUnivoco"+
      ","+ALIAS+".de_e_ente_benef_denominazione_beneficiario as DovutoElaborato_deEEnteBenefDenominazioneBeneficiario"+
      ","+ALIAS+".cod_e_ente_benef_codice_unit_oper_beneficiario as DovutoElaborato_codEEnteBenefCodiceUnitOperBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_denom_unit_oper_beneficiario as DovutoElaborato_deEEnteBenefDenomUnitOperBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_indirizzo_beneficiario as DovutoElaborato_deEEnteBenefIndirizzoBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_civico_beneficiario as DovutoElaborato_deEEnteBenefCivicoBeneficiario"+
      ","+ALIAS+".cod_e_ente_benef_cap_beneficiario as DovutoElaborato_codEEnteBenefCapBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_localita_beneficiario as DovutoElaborato_deEEnteBenefLocalitaBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_provincia_beneficiario as DovutoElaborato_deEEnteBenefProvinciaBeneficiario"+
      ","+ALIAS+".cod_e_ente_benef_nazione_beneficiario as DovutoElaborato_codEEnteBenefNazioneBeneficiario"+
      ","+ALIAS+".cod_e_sogg_vers_id_univ_vers_tipo_id_univoco as DovutoElaborato_codESoggVersIdUnivVersTipoIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_vers_id_univ_vers_codice_id_univoco as DovutoElaborato_codESoggVersIdUnivVersCodiceIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_vers_anagrafica_versante as DovutoElaborato_codESoggVersAnagraficaVersante"+
      ","+ALIAS+".de_e_sogg_vers_indirizzo_versante as DovutoElaborato_deESoggVersIndirizzoVersante"+
      ","+ALIAS+".de_e_sogg_vers_civico_versante as DovutoElaborato_deESoggVersCivicoVersante"+
      ","+ALIAS+".cod_e_sogg_vers_cap_versante as DovutoElaborato_codESoggVersCapVersante"+
      ","+ALIAS+".de_e_sogg_vers_localita_versante as DovutoElaborato_deESoggVersLocalitaVersante"+
      ","+ALIAS+".de_e_sogg_vers_provincia_versante as DovutoElaborato_deESoggVersProvinciaVersante"+
      ","+ALIAS+".cod_e_sogg_vers_nazione_versante as DovutoElaborato_codESoggVersNazioneVersante"+
      ","+ALIAS+".de_e_sogg_vers_email_versante as DovutoElaborato_deESoggVersEmailVersante"+
      ","+ALIAS+".cod_e_sogg_pag_id_univ_pag_tipo_id_univoco as DovutoElaborato_codESoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_pag_id_univ_pag_codice_id_univoco as DovutoElaborato_codESoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_pag_anagrafica_pagatore as DovutoElaborato_codESoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_e_sogg_pag_indirizzo_pagatore as DovutoElaborato_deESoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_e_sogg_pag_civico_pagatore as DovutoElaborato_deESoggPagCivicoPagatore"+
      ","+ALIAS+".cod_e_sogg_pag_cap_pagatore as DovutoElaborato_codESoggPagCapPagatore"+
      ","+ALIAS+".de_e_sogg_pag_localita_pagatore as DovutoElaborato_deESoggPagLocalitaPagatore"+
      ","+ALIAS+".de_e_sogg_pag_provincia_pagatore as DovutoElaborato_deESoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_e_sogg_pag_nazione_pagatore as DovutoElaborato_codESoggPagNazionePagatore"+
      ","+ALIAS+".de_e_sogg_pag_email_pagatore as DovutoElaborato_deESoggPagEmailPagatore"+
      ","+ALIAS+".cod_e_dati_pag_codice_esito_pagamento as DovutoElaborato_codEDatiPagCodiceEsitoPagamento"+
      ","+ALIAS+".num_e_dati_pag_importo_totale_pagato as DovutoElaborato_numEDatiPagImportoTotalePagato"+
      ","+ALIAS+".cod_e_dati_pag_id_univoco_versamento as DovutoElaborato_codEDatiPagIdUnivocoVersamento"+
      ","+ALIAS+".cod_e_dati_pag_codice_contesto_pagamento as DovutoElaborato_codEDatiPagCodiceContestoPagamento"+
      ","+ALIAS+".num_e_dati_pag_dati_sing_pag_singolo_importo_pagato as DovutoElaborato_numEDatiPagDatiSingPagSingoloImportoPagato"+
      ","+ALIAS+".de_e_dati_pag_dati_sing_pag_esito_singolo_pagamento as DovutoElaborato_deEDatiPagDatiSingPagEsitoSingoloPagamento"+
      ","+ALIAS+".dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento as DovutoElaborato_dtEDatiPagDatiSingPagDataEsitoSingoloPagamento"+
      ","+ALIAS+".cod_e_dati_pag_dati_sing_pag_id_univoco_riscoss as DovutoElaborato_codEDatiPagDatiSingPagIdUnivocoRiscoss"+
      ","+ALIAS+".de_e_dati_pag_dati_sing_pag_causale_versamento as DovutoElaborato_deEDatiPagDatiSingPagCausaleVersamento"+
      ","+ALIAS+".de_e_dati_pag_dati_sing_pag_dati_specifici_riscossione as DovutoElaborato_deEDatiPagDatiSingPagDatiSpecificiRiscossione"+
      ","+ALIAS+".cod_tipo_dovuto as DovutoElaborato_codTipoDovuto"+
      ","+ALIAS+".dt_ultimo_cambio_stato as DovutoElaborato_dtUltimoCambioStato"+
      ","+ALIAS+".modello_pagamento as DovutoElaborato_modelloPagamento"+
      ","+ALIAS+".ente_sil_invia_risposta_pagamento_url as DovutoElaborato_enteSilInviaRispostaPagamentoUrl"+
      ","+ALIAS+".de_rt_inviart_tipo_firma as DovutoElaborato_deRtInviartTipoFirma"+
      ","+ALIAS+".blb_rt_payload as DovutoElaborato_blbRtPayload"+
      ","+ALIAS+".indice_dati_singolo_pagamento as DovutoElaborato_indiceDatiSingoloPagamento"+
      ","+ALIAS+".num_e_dati_pag_dati_sing_pag_commissioni_applicate_psp as DovutoElaborato_numEDatiPagDatiSingPagCommissioniApplicatePsp"+
      ","+ALIAS+".cod_e_dati_pag_dati_sing_pag_allegato_ricevuta_tipo as DovutoElaborato_codEDatiPagDatiSingPagAllegatoRicevutaTipo"+
      ","+ALIAS+".blb_e_dati_pag_dati_sing_pag_allegato_ricevuta_test as DovutoElaborato_blbEDatiPagDatiSingPagAllegatoRicevutaTest"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo as DovutoElaborato_codRpDatiVersDatiSingVersDatiMbdTipoBollo"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_dati_mbd_hash_documento as DovutoElaborato_codRpDatiVersDatiSingVersDatiMbdHashDocumento"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza as DovutoElaborato_codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza"+
      ","+ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento_agid as DovutoElaborato_deRpDatiVersDatiSingVersCausaleVersamentoAgid"+
      ","+ALIAS+".bilancio as DovutoElaborato_bilancio"+
      ","+ALIAS+".cod_rp_silinviarp_original_fault_code as DovutoElaborato_codRpSilinviarpOriginalFaultCode"+
      ","+ALIAS+".de_rp_silinviarp_original_fault_string as DovutoElaborato_deRpSilinviarpOriginalFaultString"+
      ","+ALIAS+".de_rp_silinviarp_original_fault_description as DovutoElaborato_deRpSilinviarpOriginalFaultDescription"+
      ","+ALIAS+".cod_e_silinviaesito_original_fault_code as DovutoElaborato_codESilinviaesitoOriginalFaultCode"+
      ","+ALIAS+".de_e_silinviaesito_original_fault_string as DovutoElaborato_deESilinviaesitoOriginalFaultString"+
      ","+ALIAS+".de_e_silinviaesito_original_fault_description as DovutoElaborato_deESilinviaesitoOriginalFaultDescription";


  private Long mygovDovutoElaboratoId;
  private int version;
  private boolean flgDovutoAttuale;
  @Nested(Flusso.ALIAS)
  private Flusso mygovFlussoId;
  private long numRigaFlusso;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
  @Nested(Carrello.ALIAS)
  private Carrello mygovCarrelloId;
  private String codIud;
  private String codIuv;
  private String codAckRp;
  private Date dtCreazione;
  private Date dtUltimaModificaRp;
  private Date dtUltimaModificaE;
  private String codRpSilinviarpIdPsp;
  private String codRpSilinviarpIdIntermediarioPsp;
  private String codRpSilinviarpIdCanale;
  private String codRpSilinviarpIdDominio;
  private String codRpSilinviarpIdUnivocoVersamento;
  private String codRpSilinviarpCodiceContestoPagamento;
  private String deRpSilinviarpEsito;
  private Integer codRpSilinviarpRedirect;
  private String codRpSilinviarpUrl;
  private String codRpSilinviarpFaultCode;
  private String deRpSilinviarpFaultString;
  private String codRpSilinviarpId;
  private String deRpSilinviarpDescription;
  private Integer codRpSilinviarpSerial;
  private String deRpVersioneOggetto;
  private String codRpDomIdDominio;
  private String codRpDomIdStazioneRichiedente;
  private String codRpIdMessaggioRichiesta;
  private Date dtRpDataOraMessaggioRichiesta;
  private String codRpAutenticazioneSoggetto;
  private Character codRpSoggVersIdUnivVersTipoIdUnivoco;
  private String codRpSoggVersIdUnivVersCodiceIdUnivoco;
  private String codRpSoggVersAnagraficaVersante;
  private String deRpSoggVersIndirizzoVersante;
  private String deRpSoggVersCivicoVersante;
  private String codRpSoggVersCapVersante;
  private String deRpSoggVersLocalitaVersante;
  private String deRpSoggVersProvinciaVersante;
  private String codRpSoggVersNazioneVersante;
  private String deRpSoggVersEmailVersante;
  private char codRpSoggPagIdUnivPagTipoIdUnivoco;
  private String codRpSoggPagIdUnivPagCodiceIdUnivoco;
  private String deRpSoggPagAnagraficaPagatore;
  private String deRpSoggPagIndirizzoPagatore;
  private String deRpSoggPagCivicoPagatore;
  private String codRpSoggPagCapPagatore;
  private String deRpSoggPagLocalitaPagatore;
  private String deRpSoggPagProvinciaPagatore;
  private String codRpSoggPagNazionePagatore;
  private String deRpSoggPagEmailPagatore;
  private Date dtRpDatiVersDataEsecuzionePagamento;
  private BigDecimal numRpDatiVersImportoTotaleDaVersare;
  private String codRpDatiVersTipoVersamento;
  private String codRpDatiVersIdUnivocoVersamento;
  private String codRpDatiVersCodiceContestoPagamento;
  private String deRpDatiVersIbanAddebito;
  private String deRpDatiVersBicAddebito;
  private BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento;
  private BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa;
  private String codRpDatiVersDatiSingVersIbanAccredito;
  private String codRpDatiVersDatiSingVersBicAccredito;
  private String codRpDatiVersDatiSingVersIbanAppoggio;
  private String codRpDatiVersDatiSingVersBicAppoggio;
  private String codRpDatiVersDatiSingVersCredenzialiPagatore;
  private String deRpDatiVersDatiSingVersCausaleVersamento;
  private String deRpDatiVersDatiSingVersDatiSpecificiRiscossione;
  private String codESilinviaesitoIdDominio;
  private String codESilinviaesitoIdUnivocoVersamento;
  private String codESilinviaesitoCodiceContestoPagamento;
  private String deESilinviaesitoEsito;
  private String codESilinviaesitoFaultCode;
  private String deESilinviaesitoFaultString;
  private String codESilinviaesitoId;
  private String deESilinviaesitoDescription;
  private Integer codESilinviaesitoSerial;
  private String deEVersioneOggetto;
  private String codEDomIdDominio;
  private String codEDomIdStazioneRichiedente;
  private String codEIdMessaggioRicevuta;
  private Date codEDataOraMessaggioRicevuta;
  private String codERiferimentoMessaggioRichiesta;
  private Date codERiferimentoDataRichiesta;
  private Character codEIstitAttIdUnivAttTipoIdUnivoco;
  private String codEIstitAttIdUnivAttCodiceIdUnivoco;
  private String deEIstitAttDenominazioneAttestante;
  private String codEIstitAttCodiceUnitOperAttestante;
  private String deEIstitAttDenomUnitOperAttestante;
  private String deEIstitAttIndirizzoAttestante;
  private String deEIstitAttCivicoAttestante;
  private String codEIstitAttCapAttestante;
  private String deEIstitAttLocalitaAttestante;
  private String deEIstitAttProvinciaAttestante;
  private String codEIstitAttNazioneAttestante;
  private Character codEEnteBenefIdUnivBenefTipoIdUnivoco;
  private String codEEnteBenefIdUnivBenefCodiceIdUnivoco;
  private String deEEnteBenefDenominazioneBeneficiario;
  private String codEEnteBenefCodiceUnitOperBeneficiario;
  private String deEEnteBenefDenomUnitOperBeneficiario;
  private String deEEnteBenefIndirizzoBeneficiario;
  private String deEEnteBenefCivicoBeneficiario;
  private String codEEnteBenefCapBeneficiario;
  private String deEEnteBenefLocalitaBeneficiario;
  private String deEEnteBenefProvinciaBeneficiario;
  private String codEEnteBenefNazioneBeneficiario;
  private Character codESoggVersIdUnivVersTipoIdUnivoco;
  private String codESoggVersIdUnivVersCodiceIdUnivoco;
  private String codESoggVersAnagraficaVersante;
  private String deESoggVersIndirizzoVersante;
  private String deESoggVersCivicoVersante;
  private String codESoggVersCapVersante;
  private String deESoggVersLocalitaVersante;
  private String deESoggVersProvinciaVersante;
  private String codESoggVersNazioneVersante;
  private String deESoggVersEmailVersante;
  private Character codESoggPagIdUnivPagTipoIdUnivoco;
  private String codESoggPagIdUnivPagCodiceIdUnivoco;
  private String codESoggPagAnagraficaPagatore;
  private String deESoggPagIndirizzoPagatore;
  private String deESoggPagCivicoPagatore;
  private String codESoggPagCapPagatore;
  private String deESoggPagLocalitaPagatore;
  private String deESoggPagProvinciaPagatore;
  private String codESoggPagNazionePagatore;
  private String deESoggPagEmailPagatore;
  private Character codEDatiPagCodiceEsitoPagamento;
  private BigDecimal numEDatiPagImportoTotalePagato;
  private String codEDatiPagIdUnivocoVersamento;
  private String codEDatiPagCodiceContestoPagamento;
  private BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato;
  private String deEDatiPagDatiSingPagEsitoSingoloPagamento;
  private Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento;
  private String codEDatiPagDatiSingPagIdUnivocoRiscoss;
  private String deEDatiPagDatiSingPagCausaleVersamento;
  private String deEDatiPagDatiSingPagDatiSpecificiRiscossione;
  private String codTipoDovuto;
  private Date dtUltimoCambioStato;
  private Integer modelloPagamento;
  private String enteSilInviaRispostaPagamentoUrl;
  private String deRtInviartTipoFirma;
  private byte[] blbRtPayload;
  private Integer indiceDatiSingoloPagamento;
  private BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp;
  private String codEDatiPagDatiSingPagAllegatoRicevutaTipo;
  private byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest;
  private String codRpDatiVersDatiSingVersDatiMbdTipoBollo;
  private String codRpDatiVersDatiSingVersDatiMbdHashDocumento;
  private String codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza;
  private String deRpDatiVersDatiSingVersCausaleVersamentoAgid;
  private String bilancio;
  private String codRpSilinviarpOriginalFaultCode;
  private String deRpSilinviarpOriginalFaultString;
  private String deRpSilinviarpOriginalFaultDescription;
  private String codESilinviaesitoOriginalFaultCode;
  private String deESilinviaesitoOriginalFaultString;
  private String deESilinviaesitoOriginalFaultDescription;

  @Nested(Ente.ALIAS)
  private Ente nestedEnte;

  public String getValidIuv(){
    return StringUtils.firstNonBlank(codEDatiPagIdUnivocoVersamento, codRpDatiVersIdUnivocoVersamento, codRpSilinviarpIdUnivocoVersamento, codIuv);
  }
}
