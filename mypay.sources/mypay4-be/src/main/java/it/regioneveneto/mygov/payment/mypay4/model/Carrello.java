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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovCarrelloId")
public class Carrello extends BaseEntity {

  public static final String ALIAS = "Carrello";
  public static final String FIELDS = ""+ALIAS+".mygov_carrello_id as Carrello_mygovCarrelloId,"+ALIAS+".version as Carrello_version"+
      ","+ALIAS+".mygov_anagrafica_stato_id as Carrello_mygovAnagraficaStatoId,"+ALIAS+".cod_ack_rp as Carrello_codAckRp"+
      ","+ALIAS+".dt_creazione as Carrello_dtCreazione,"+ALIAS+".dt_ultima_modifica_rp as Carrello_dtUltimaModificaRp"+
      ","+ALIAS+".dt_ultima_modifica_e as Carrello_dtUltimaModificaE"+
      ","+ALIAS+".cod_rp_silinviarp_id_psp as Carrello_codRpSilinviarpIdPsp"+
      ","+ALIAS+".cod_rp_silinviarp_id_intermediario_psp as Carrello_codRpSilinviarpIdIntermediarioPsp"+
      ","+ALIAS+".cod_rp_silinviarp_id_canale as Carrello_codRpSilinviarpIdCanale"+
      ","+ALIAS+".cod_rp_silinviarp_id_dominio as Carrello_codRpSilinviarpIdDominio"+
      ","+ALIAS+".cod_rp_silinviarp_id_univoco_versamento as Carrello_codRpSilinviarpIdUnivocoVersamento"+
      ","+ALIAS+".cod_rp_silinviarp_codice_contesto_pagamento as Carrello_codRpSilinviarpCodiceContestoPagamento"+
      ","+ALIAS+".de_rp_silinviarp_esito as Carrello_deRpSilinviarpEsito"+
      ","+ALIAS+".cod_rp_silinviarp_redirect as Carrello_codRpSilinviarpRedirect"+
      ","+ALIAS+".cod_rp_silinviarp_url as Carrello_codRpSilinviarpUrl"+
      ","+ALIAS+".cod_rp_silinviarp_fault_code as Carrello_codRpSilinviarpFaultCode"+
      ","+ALIAS+".de_rp_silinviarp_fault_string as Carrello_deRpSilinviarpFaultString"+
      ","+ALIAS+".cod_rp_silinviarp_id as Carrello_codRpSilinviarpId"+
      ","+ALIAS+".de_rp_silinviarp_description as Carrello_deRpSilinviarpDescription"+
      ","+ALIAS+".cod_rp_silinviarp_serial as Carrello_codRpSilinviarpSerial"+
      ","+ALIAS+".de_rp_versione_oggetto as Carrello_deRpVersioneOggetto"+
      ","+ALIAS+".cod_rp_dom_id_dominio as Carrello_codRpDomIdDominio"+
      ","+ALIAS+".cod_rp_dom_id_stazione_richiedente as Carrello_codRpDomIdStazioneRichiedente"+
      ","+ALIAS+".cod_rp_id_messaggio_richiesta as Carrello_codRpIdMessaggioRichiesta"+
      ","+ALIAS+".dt_rp_data_ora_messaggio_richiesta as Carrello_dtRpDataOraMessaggioRichiesta"+
      ","+ALIAS+".cod_rp_autenticazione_soggetto as Carrello_codRpAutenticazioneSoggetto"+
      ","+ALIAS+".cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco as Carrello_codRpSoggVersIdUnivVersTipoIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_vers_id_univ_vers_codice_id_univoco as Carrello_codRpSoggVersIdUnivVersCodiceIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_vers_anagrafica_versante as Carrello_codRpSoggVersAnagraficaVersante"+
      ","+ALIAS+".de_rp_sogg_vers_indirizzo_versante as Carrello_deRpSoggVersIndirizzoVersante"+
      ","+ALIAS+".de_rp_sogg_vers_civico_versante as Carrello_deRpSoggVersCivicoVersante"+
      ","+ALIAS+".cod_rp_sogg_vers_cap_versante as Carrello_codRpSoggVersCapVersante"+
      ","+ALIAS+".de_rp_sogg_vers_localita_versante as Carrello_deRpSoggVersLocalitaVersante"+
      ","+ALIAS+".de_rp_sogg_vers_provincia_versante as Carrello_deRpSoggVersProvinciaVersante"+
      ","+ALIAS+".cod_rp_sogg_vers_nazione_versante as Carrello_codRpSoggVersNazioneVersante"+
      ","+ALIAS+".de_rp_sogg_vers_email_versante as Carrello_deRpSoggVersEmailVersante"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as Carrello_codRpSoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco as Carrello_codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".de_rp_sogg_pag_anagrafica_pagatore as Carrello_deRpSoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_indirizzo_pagatore as Carrello_deRpSoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_civico_pagatore as Carrello_deRpSoggPagCivicoPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_cap_pagatore as Carrello_codRpSoggPagCapPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_localita_pagatore as Carrello_deRpSoggPagLocalitaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_provincia_pagatore as Carrello_deRpSoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_nazione_pagatore as Carrello_codRpSoggPagNazionePagatore"+
      ","+ALIAS+".de_rp_sogg_pag_email_pagatore as Carrello_deRpSoggPagEmailPagatore"+
      ","+ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento as Carrello_dtRpDatiVersDataEsecuzionePagamento"+
      ","+ALIAS+".num_rp_dati_vers_importo_totale_da_versare as Carrello_numRpDatiVersImportoTotaleDaVersare"+
      ","+ALIAS+".cod_rp_dati_vers_tipo_versamento as Carrello_codRpDatiVersTipoVersamento"+
      ","+ALIAS+".cod_rp_dati_vers_id_univoco_versamento as Carrello_codRpDatiVersIdUnivocoVersamento"+
      ","+ALIAS+".cod_rp_dati_vers_codice_contesto_pagamento as Carrello_codRpDatiVersCodiceContestoPagamento"+
      ","+ALIAS+".de_rp_dati_vers_iban_addebito as Carrello_deRpDatiVersIbanAddebito"+
      ","+ALIAS+".de_rp_dati_vers_bic_addebito as Carrello_deRpDatiVersBicAddebito"+
      ","+ALIAS+".cod_e_silinviaesito_id_dominio as Carrello_codESilinviaesitoIdDominio"+
      ","+ALIAS+".cod_e_silinviaesito_id_univoco_versamento as Carrello_codESilinviaesitoIdUnivocoVersamento"+
      ","+ALIAS+".cod_e_silinviaesito_codice_contesto_pagamento as Carrello_codESilinviaesitoCodiceContestoPagamento"+
      ","+ALIAS+".de_e_silinviaesito_esito as Carrello_deESilinviaesitoEsito"+
      ","+ALIAS+".cod_e_silinviaesito_fault_code as Carrello_codESilinviaesitoFaultCode"+
      ","+ALIAS+".de_e_silinviaesito_fault_string as Carrello_deESilinviaesitoFaultString"+
      ","+ALIAS+".cod_e_silinviaesito_id as Carrello_codESilinviaesitoId"+
      ","+ALIAS+".de_e_silinviaesito_description as Carrello_deESilinviaesitoDescription"+
      ","+ALIAS+".cod_e_silinviaesito_serial as Carrello_codESilinviaesitoSerial"+
      ","+ALIAS+".de_e_versione_oggetto as Carrello_deEVersioneOggetto"+
      ","+ALIAS+".cod_e_dom_id_dominio as Carrello_codEDomIdDominio"+
      ","+ALIAS+".cod_e_dom_id_stazione_richiedente as Carrello_codEDomIdStazioneRichiedente"+
      ","+ALIAS+".cod_e_id_messaggio_ricevuta as Carrello_codEIdMessaggioRicevuta"+
      ","+ALIAS+".cod_e_data_ora_messaggio_ricevuta as Carrello_codEDataOraMessaggioRicevuta"+
      ","+ALIAS+".cod_e_riferimento_messaggio_richiesta as Carrello_codERiferimentoMessaggioRichiesta"+
      ","+ALIAS+".cod_e_riferimento_data_richiesta as Carrello_codERiferimentoDataRichiesta"+
      ","+ALIAS+".cod_e_istit_att_id_univ_att_tipo_id_univoco as Carrello_codEIstitAttIdUnivAttTipoIdUnivoco"+
      ","+ALIAS+".cod_e_istit_att_id_univ_att_codice_id_univoco as Carrello_codEIstitAttIdUnivAttCodiceIdUnivoco"+
      ","+ALIAS+".de_e_istit_att_denominazione_attestante as Carrello_deEIstitAttDenominazioneAttestante"+
      ","+ALIAS+".cod_e_istit_att_codice_unit_oper_attestante as Carrello_codEIstitAttCodiceUnitOperAttestante"+
      ","+ALIAS+".de_e_istit_att_denom_unit_oper_attestante as Carrello_deEIstitAttDenomUnitOperAttestante"+
      ","+ALIAS+".de_e_istit_att_indirizzo_attestante as Carrello_deEIstitAttIndirizzoAttestante"+
      ","+ALIAS+".de_e_istit_att_civico_attestante as Carrello_deEIstitAttCivicoAttestante"+
      ","+ALIAS+".cod_e_istit_att_cap_attestante as Carrello_codEIstitAttCapAttestante"+
      ","+ALIAS+".de_e_istit_att_localita_attestante as Carrello_deEIstitAttLocalitaAttestante"+
      ","+ALIAS+".de_e_istit_att_provincia_attestante as Carrello_deEIstitAttProvinciaAttestante"+
      ","+ALIAS+".cod_e_istit_att_nazione_attestante as Carrello_codEIstitAttNazioneAttestante"+
      ","+ALIAS+".cod_e_ente_benef_id_univ_benef_tipo_id_univoco as Carrello_codEEnteBenefIdUnivBenefTipoIdUnivoco"+
      ","+ALIAS+".cod_e_ente_benef_id_univ_benef_codice_id_univoco as Carrello_codEEnteBenefIdUnivBenefCodiceIdUnivoco"+
      ","+ALIAS+".de_e_ente_benef_denominazione_beneficiario as Carrello_deEEnteBenefDenominazioneBeneficiario"+
      ","+ALIAS+".cod_e_ente_benef_codice_unit_oper_beneficiario as Carrello_codEEnteBenefCodiceUnitOperBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_denom_unit_oper_beneficiario as Carrello_deEEnteBenefDenomUnitOperBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_indirizzo_beneficiario as Carrello_deEEnteBenefIndirizzoBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_civico_beneficiario as Carrello_deEEnteBenefCivicoBeneficiario"+
      ","+ALIAS+".cod_e_ente_benef_cap_beneficiario as Carrello_codEEnteBenefCapBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_localita_beneficiario as Carrello_deEEnteBenefLocalitaBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_provincia_beneficiario as Carrello_deEEnteBenefProvinciaBeneficiario"+
      ","+ALIAS+".cod_e_ente_benef_nazione_beneficiario as Carrello_codEEnteBenefNazioneBeneficiario"+
      ","+ALIAS+".cod_e_sogg_vers_id_univ_vers_tipo_id_univoco as Carrello_codESoggVersIdUnivVersTipoIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_vers_id_univ_vers_codice_id_univoco as Carrello_codESoggVersIdUnivVersCodiceIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_vers_anagrafica_versante as Carrello_codESoggVersAnagraficaVersante"+
      ","+ALIAS+".de_e_sogg_vers_indirizzo_versante as Carrello_deESoggVersIndirizzoVersante"+
      ","+ALIAS+".de_e_sogg_vers_civico_versante as Carrello_deESoggVersCivicoVersante"+
      ","+ALIAS+".cod_e_sogg_vers_cap_versante as Carrello_codESoggVersCapVersante"+
      ","+ALIAS+".de_e_sogg_vers_localita_versante as Carrello_deESoggVersLocalitaVersante"+
      ","+ALIAS+".de_e_sogg_vers_provincia_versante as Carrello_deESoggVersProvinciaVersante"+
      ","+ALIAS+".cod_e_sogg_vers_nazione_versante as Carrello_codESoggVersNazioneVersante"+
      ","+ALIAS+".de_e_sogg_vers_email_versante as Carrello_deESoggVersEmailVersante"+
      ","+ALIAS+".cod_e_sogg_pag_id_univ_pag_tipo_id_univoco as Carrello_codESoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_pag_id_univ_pag_codice_id_univoco as Carrello_codESoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_pag_anagrafica_pagatore as Carrello_codESoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_e_sogg_pag_indirizzo_pagatore as Carrello_deESoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_e_sogg_pag_civico_pagatore as Carrello_deESoggPagCivicoPagatore"+
      ","+ALIAS+".cod_e_sogg_pag_cap_pagatore as Carrello_codESoggPagCapPagatore"+
      ","+ALIAS+".de_e_sogg_pag_localita_pagatore as Carrello_deESoggPagLocalitaPagatore"+
      ","+ALIAS+".de_e_sogg_pag_provincia_pagatore as Carrello_deESoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_e_sogg_pag_nazione_pagatore as Carrello_codESoggPagNazionePagatore"+
      ","+ALIAS+".de_e_sogg_pag_email_pagatore as Carrello_deESoggPagEmailPagatore"+
      ","+ALIAS+".cod_e_dati_pag_codice_esito_pagamento as Carrello_codEDatiPagCodiceEsitoPagamento"+
      ","+ALIAS+".num_e_dati_pag_importo_totale_pagato as Carrello_numEDatiPagImportoTotalePagato"+
      ","+ALIAS+".cod_e_dati_pag_id_univoco_versamento as Carrello_codEDatiPagIdUnivocoVersamento"+
      ","+ALIAS+".cod_e_dati_pag_codice_contesto_pagamento as Carrello_codEDatiPagCodiceContestoPagamento"+
      ","+ALIAS+".id_session as Carrello_idSession,"+ALIAS+".id_session_fesp as Carrello_idSessionFesp"+
      ","+ALIAS+".tipo_carrello as Carrello_tipoCarrello,"+ALIAS+".modello_pagamento as Carrello_modelloPagamento"+
      ","+ALIAS+".ente_sil_invia_risposta_pagamento_url as Carrello_enteSilInviaRispostaPagamentoUrl"+
      ","+ALIAS+".flg_notifica_esito as Carrello_flgNotificaEsito"+
      ","+ALIAS+".mygov_carrello_multi_beneficiario_id as Carrello_mygovCarrelloMultiBeneficiarioId"+
      ","+ALIAS+".cod_rp_silinviarp_original_fault_code as Carrello_codRpSilinviarpOriginalFaultCode"+
      ","+ALIAS+".de_rp_silinviarp_original_fault_string as Carrello_deRpSilinviarpOriginalFaultString"+
      ","+ALIAS+".de_rp_silinviarp_original_fault_description as Carrello_deRpSilinviarpOriginalFaultDescription"+
      ","+ALIAS+".cod_e_silinviaesito_original_fault_code as Carrello_codESilinviaesitoOriginalFaultCode"+
      ","+ALIAS+".de_e_silinviaesito_original_fault_string as Carrello_deESilinviaesitoOriginalFaultString"+
      ","+ALIAS+".de_e_silinviaesito_original_fault_description as Carrello_deESilinviaesitoOriginalFaultDescription";

  private Long mygovCarrelloId;
  private int version;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
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
  private Character codRpSoggPagIdUnivPagTipoIdUnivoco;
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
  private String idSession;
  private String idSessionFesp;
  private String tipoCarrello;
  private Integer modelloPagamento;
  private String enteSilInviaRispostaPagamentoUrl;
  private Boolean flgNotificaEsito;
  @Nested(CarrelloMultiBeneficiario.ALIAS)
  private CarrelloMultiBeneficiario mygovCarrelloMultiBeneficiarioId;
  private String codRpSilinviarpOriginalFaultCode;
  private String deRpSilinviarpOriginalFaultString;
  private String deRpSilinviarpOriginalFaultDescription;
  private String codESilinviaesitoOriginalFaultCode;
  private String deESilinviaesitoOriginalFaultString;
  private String deESilinviaesitoOriginalFaultDescription;


  public String getValidIuv(){
    return StringUtils.firstNonBlank(codEDatiPagIdUnivocoVersamento, codRpDatiVersIdUnivocoVersamento, codRpSilinviarpIdUnivocoVersamento);
  }
}
