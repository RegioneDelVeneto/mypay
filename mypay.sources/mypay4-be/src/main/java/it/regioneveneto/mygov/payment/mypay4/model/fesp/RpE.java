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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovRpEId")
public class RpE extends BaseEntity {

  public static final String ALIAS = "FESP_RpE";
  public static final String FIELDS = ""+ALIAS+".mygov_rp_e_id as FESP_RpE_mygovRpEId,"+ALIAS+".version as FESP_RpE_version"+
      ","+ALIAS+".mygov_carrello_rp_id as FESP_RpE_mygovCarrelloRpId,"+ALIAS+".cod_ack_e as FESP_RpE_codAckE"+
      ","+ALIAS+".dt_creazione_rp as FESP_RpE_dtCreazioneRp,"+ALIAS+".dt_ultima_modifica_rp as FESP_RpE_dtUltimaModificaRp"+
      ","+ALIAS+".dt_creazione_e as FESP_RpE_dtCreazioneE,"+ALIAS+".dt_ultima_modifica_e as FESP_RpE_dtUltimaModificaE"+
      ","+ALIAS+".cod_rp_silinviarp_id_psp as FESP_RpE_codRpSilinviarpIdPsp"+
      ","+ALIAS+".cod_rp_silinviarp_id_intermediario_psp as FESP_RpE_codRpSilinviarpIdIntermediarioPsp"+
      ","+ALIAS+".cod_rp_silinviarp_id_canale as FESP_RpE_codRpSilinviarpIdCanale"+
      ","+ALIAS+".cod_rp_silinviarp_id_dominio as FESP_RpE_codRpSilinviarpIdDominio"+
      ","+ALIAS+".cod_rp_silinviarp_id_univoco_versamento as FESP_RpE_codRpSilinviarpIdUnivocoVersamento"+
      ","+ALIAS+".cod_rp_silinviarp_codice_contesto_pagamento as FESP_RpE_codRpSilinviarpCodiceContestoPagamento"+
      ","+ALIAS+".de_rp_silinviarp_esito as FESP_RpE_deRpSilinviarpEsito"+
      ","+ALIAS+".cod_rp_silinviarp_redirect as FESP_RpE_codRpSilinviarpRedirect"+
      ","+ALIAS+".cod_rp_silinviarp_url as FESP_RpE_codRpSilinviarpUrl"+
      ","+ALIAS+".cod_rp_silinviarp_fault_code as FESP_RpE_codRpSilinviarpFaultCode"+
      ","+ALIAS+".de_rp_silinviarp_fault_string as FESP_RpE_deRpSilinviarpFaultString"+
      ","+ALIAS+".cod_rp_silinviarp_id as FESP_RpE_codRpSilinviarpId"+
      ","+ALIAS+".de_rp_silinviarp_description as FESP_RpE_deRpSilinviarpDescription"+
      ","+ALIAS+".cod_rp_silinviarp_serial as FESP_RpE_codRpSilinviarpSerial"+
      ","+ALIAS+".de_rp_versione_oggetto as FESP_RpE_deRpVersioneOggetto"+
      ","+ALIAS+".cod_rp_dom_id_dominio as FESP_RpE_codRpDomIdDominio"+
      ","+ALIAS+".cod_rp_dom_id_stazione_richiedente as FESP_RpE_codRpDomIdStazioneRichiedente"+
      ","+ALIAS+".cod_rp_id_messaggio_richiesta as FESP_RpE_codRpIdMessaggioRichiesta"+
      ","+ALIAS+".dt_rp_data_ora_messaggio_richiesta as FESP_RpE_dtRpDataOraMessaggioRichiesta"+
      ","+ALIAS+".cod_rp_autenticazione_soggetto as FESP_RpE_codRpAutenticazioneSoggetto"+
      ","+ALIAS+".cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco as FESP_RpE_codRpSoggVersIdUnivVersTipoIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_vers_id_univ_vers_codice_id_univoco as FESP_RpE_codRpSoggVersIdUnivVersCodiceIdUnivoco"+
      ","+ALIAS+".de_rp_sogg_vers_anagrafica_versante as FESP_RpE_deRpSoggVersAnagraficaVersante"+
      ","+ALIAS+".de_rp_sogg_vers_indirizzo_versante as FESP_RpE_deRpSoggVersIndirizzoVersante"+
      ","+ALIAS+".de_rp_sogg_vers_civico_versante as FESP_RpE_deRpSoggVersCivicoVersante"+
      ","+ALIAS+".cod_rp_sogg_vers_cap_versante as FESP_RpE_codRpSoggVersCapVersante"+
      ","+ALIAS+".de_rp_sogg_vers_localita_versante as FESP_RpE_deRpSoggVersLocalitaVersante"+
      ","+ALIAS+".de_rp_sogg_vers_provincia_versante as FESP_RpE_deRpSoggVersProvinciaVersante"+
      ","+ALIAS+".cod_rp_sogg_vers_nazione_versante as FESP_RpE_codRpSoggVersNazioneVersante"+
      ","+ALIAS+".de_rp_sogg_vers_email_versante as FESP_RpE_deRpSoggVersEmailVersante"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as FESP_RpE_codRpSoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco as FESP_RpE_codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".de_rp_sogg_pag_anagrafica_pagatore as FESP_RpE_deRpSoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_indirizzo_pagatore as FESP_RpE_deRpSoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_civico_pagatore as FESP_RpE_deRpSoggPagCivicoPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_cap_pagatore as FESP_RpE_codRpSoggPagCapPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_localita_pagatore as FESP_RpE_deRpSoggPagLocalitaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_provincia_pagatore as FESP_RpE_deRpSoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_nazione_pagatore as FESP_RpE_codRpSoggPagNazionePagatore"+
      ","+ALIAS+".de_rp_sogg_pag_email_pagatore as FESP_RpE_deRpSoggPagEmailPagatore"+
      ","+ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento as FESP_RpE_dtRpDatiVersDataEsecuzionePagamento"+
      ","+ALIAS+".num_rp_dati_vers_importo_totale_da_versare as FESP_RpE_numRpDatiVersImportoTotaleDaVersare"+
      ","+ALIAS+".cod_rp_dati_vers_tipo_versamento as FESP_RpE_codRpDatiVersTipoVersamento"+
      ","+ALIAS+".cod_rp_dati_vers_id_univoco_versamento as FESP_RpE_codRpDatiVersIdUnivocoVersamento"+
      ","+ALIAS+".cod_rp_dati_vers_codice_contesto_pagamento as FESP_RpE_codRpDatiVersCodiceContestoPagamento"+
      ","+ALIAS+".de_rp_dati_vers_iban_addebito as FESP_RpE_deRpDatiVersIbanAddebito"+
      ","+ALIAS+".de_rp_dati_vers_bic_addebito as FESP_RpE_deRpDatiVersBicAddebito"+
      ","+ALIAS+".cod_e_silinviaesito_id_dominio as FESP_RpE_codESilinviaesitoIdDominio"+
      ","+ALIAS+".cod_e_silinviaesito_id_univoco_versamento as FESP_RpE_codESilinviaesitoIdUnivocoVersamento"+
      ","+ALIAS+".cod_e_silinviaesito_codice_contesto_pagamento as FESP_RpE_codESilinviaesitoCodiceContestoPagamento"+
      ","+ALIAS+".de_e_silinviaesito_esito as FESP_RpE_deESilinviaesitoEsito"+
      ","+ALIAS+".cod_e_silinviaesito_fault_code as FESP_RpE_codESilinviaesitoFaultCode"+
      ","+ALIAS+".de_e_silinviaesito_fault_string as FESP_RpE_deESilinviaesitoFaultString"+
      ","+ALIAS+".cod_e_silinviaesito_id as FESP_RpE_codESilinviaesitoId"+
      ","+ALIAS+".de_e_silinviaesito_description as FESP_RpE_deESilinviaesitoDescription"+
      ","+ALIAS+".cod_e_silinviaesito_serial as FESP_RpE_codESilinviaesitoSerial"+
      ","+ALIAS+".de_e_versione_oggetto as FESP_RpE_deEVersioneOggetto"+
      ","+ALIAS+".cod_e_dom_id_dominio as FESP_RpE_codEDomIdDominio"+
      ","+ALIAS+".cod_e_dom_id_stazione_richiedente as FESP_RpE_codEDomIdStazioneRichiedente"+
      ","+ALIAS+".cod_e_id_messaggio_ricevuta as FESP_RpE_codEIdMessaggioRicevuta"+
      ","+ALIAS+".dt_e_data_ora_messaggio_ricevuta as FESP_RpE_dtEDataOraMessaggioRicevuta"+
      ","+ALIAS+".cod_e_riferimento_messaggio_richiesta as FESP_RpE_codERiferimentoMessaggioRichiesta"+
      ","+ALIAS+".dt_e_riferimento_data_richiesta as FESP_RpE_dtERiferimentoDataRichiesta"+
      ","+ALIAS+".cod_e_istit_attes_id_univ_attes_tipo_id_univoco as FESP_RpE_codEIstitAttesIdUnivAttesTipoIdUnivoco"+
      ","+ALIAS+".cod_e_istit_attes_id_univ_attes_codice_id_univoco as FESP_RpE_codEIstitAttesIdUnivAttesCodiceIdUnivoco"+
      ","+ALIAS+".de_e_istit_attes_denominazione_attestante as FESP_RpE_deEIstitAttesDenominazioneAttestante"+
      ","+ALIAS+".cod_e_istit_attes_codice_unit_oper_attestante as FESP_RpE_codEIstitAttesCodiceUnitOperAttestante"+
      ","+ALIAS+".de_e_istit_attes_denom_unit_oper_attestante as FESP_RpE_deEIstitAttesDenomUnitOperAttestante"+
      ","+ALIAS+".de_e_istit_attes_indirizzo_attestante as FESP_RpE_deEIstitAttesIndirizzoAttestante"+
      ","+ALIAS+".de_e_istit_attes_civico_attestante as FESP_RpE_deEIstitAttesCivicoAttestante"+
      ","+ALIAS+".cod_e_istit_attes_cap_attestante as FESP_RpE_codEIstitAttesCapAttestante"+
      ","+ALIAS+".de_e_istit_attes_localita_attestante as FESP_RpE_deEIstitAttesLocalitaAttestante"+
      ","+ALIAS+".de_e_istit_attes_provincia_attestante as FESP_RpE_deEIstitAttesProvinciaAttestante"+
      ","+ALIAS+".cod_e_istit_attes_nazione_attestante as FESP_RpE_codEIstitAttesNazioneAttestante"+
      ","+ALIAS+".cod_e_ente_benef_id_univ_benef_tipo_id_univoco as FESP_RpE_codEEnteBenefIdUnivBenefTipoIdUnivoco"+
      ","+ALIAS+".cod_e_ente_benef_id_univ_benef_codice_id_univoco as FESP_RpE_codEEnteBenefIdUnivBenefCodiceIdUnivoco"+
      ","+ALIAS+".de_e_ente_benef_denominazione_beneficiario as FESP_RpE_deEEnteBenefDenominazioneBeneficiario"+
      ","+ALIAS+".cod_e_ente_benef_codice_unit_oper_beneficiario as FESP_RpE_codEEnteBenefCodiceUnitOperBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_denom_unit_oper_beneficiario as FESP_RpE_deEEnteBenefDenomUnitOperBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_indirizzo_beneficiario as FESP_RpE_deEEnteBenefIndirizzoBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_civico_beneficiario as FESP_RpE_deEEnteBenefCivicoBeneficiario"+
      ","+ALIAS+".cod_e_ente_benef_cap_beneficiario as FESP_RpE_codEEnteBenefCapBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_localita_beneficiario as FESP_RpE_deEEnteBenefLocalitaBeneficiario"+
      ","+ALIAS+".de_e_ente_benef_provincia_beneficiario as FESP_RpE_deEEnteBenefProvinciaBeneficiario"+
      ","+ALIAS+".cod_e_ente_benef_nazione_beneficiario as FESP_RpE_codEEnteBenefNazioneBeneficiario"+
      ","+ALIAS+".cod_e_sogg_vers_id_univ_vers_tipo_id_univoco as FESP_RpE_codESoggVersIdUnivVersTipoIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_vers_id_univ_vers_codice_id_univoco as FESP_RpE_codESoggVersIdUnivVersCodiceIdUnivoco"+
      ","+ALIAS+".de_e_sogg_vers_anagrafica_versante as FESP_RpE_deESoggVersAnagraficaVersante"+
      ","+ALIAS+".de_e_sogg_vers_indirizzo_versante as FESP_RpE_deESoggVersIndirizzoVersante"+
      ","+ALIAS+".de_e_sogg_vers_civico_versante as FESP_RpE_deESoggVersCivicoVersante"+
      ","+ALIAS+".cod_e_sogg_vers_cap_versante as FESP_RpE_codESoggVersCapVersante"+
      ","+ALIAS+".de_e_sogg_vers_localita_versante as FESP_RpE_deESoggVersLocalitaVersante"+
      ","+ALIAS+".de_e_sogg_vers_provincia_versante as FESP_RpE_deESoggVersProvinciaVersante"+
      ","+ALIAS+".cod_e_sogg_vers_nazione_versante as FESP_RpE_codESoggVersNazioneVersante"+
      ","+ALIAS+".de_e_sogg_vers_email_versante as FESP_RpE_deESoggVersEmailVersante"+
      ","+ALIAS+".cod_e_sogg_pag_id_univ_pag_tipo_id_univoco as FESP_RpE_codESoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_e_sogg_pag_id_univ_pag_codice_id_univoco as FESP_RpE_codESoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".de_e_sogg_pag_anagrafica_pagatore as FESP_RpE_deESoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_e_sogg_pag_indirizzo_pagatore as FESP_RpE_deESoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_e_sogg_pag_civico_pagatore as FESP_RpE_deESoggPagCivicoPagatore"+
      ","+ALIAS+".cod_e_sogg_pag_cap_pagatore as FESP_RpE_codESoggPagCapPagatore"+
      ","+ALIAS+".de_e_sogg_pag_localita_pagatore as FESP_RpE_deESoggPagLocalitaPagatore"+
      ","+ALIAS+".de_e_sogg_pag_provincia_pagatore as FESP_RpE_deESoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_e_sogg_pag_nazione_pagatore as FESP_RpE_codESoggPagNazionePagatore"+
      ","+ALIAS+".de_e_sogg_pag_email_pagatore as FESP_RpE_deESoggPagEmailPagatore"+
      ","+ALIAS+".cod_e_dati_pag_codice_esito_pagamento as FESP_RpE_codEDatiPagCodiceEsitoPagamento"+
      ","+ALIAS+".num_e_dati_pag_importo_totale_pagato as FESP_RpE_numEDatiPagImportoTotalePagato"+
      ","+ALIAS+".cod_e_dati_pag_id_univoco_versamento as FESP_RpE_codEDatiPagIdUnivocoVersamento"+
      ","+ALIAS+".cod_e_dati_pag_codice_contesto_pagamento as FESP_RpE_codEDatiPagCodiceContestoPagamento"+
      ","+ALIAS+".id_session as FESP_RpE_idSession,"+ALIAS+".modello_pagamento as FESP_RpE_modelloPagamento"+
      ","+ALIAS+".cod_e_silinviaesito_original_fault_code as FESP_RpE_codESilinviaesitoOriginalFaultCode"+
      ","+ALIAS+".de_e_silinviaesito_original_fault_string as FESP_RpE_deESilinviaesitoOriginalFaultString"+
      ","+ALIAS+".de_e_silinviaesito_original_fault_description as FESP_RpE_deESilinviaesitoOriginalFaultDescription"+
      ","+ALIAS+".cod_rp_silinviarp_original_fault_code as FESP_RpE_codRpSilinviarpOriginalFaultCode"+
      ","+ALIAS+".de_rp_silinviarp_original_fault_string as FESP_RpE_deRpSilinviarpOriginalFaultString"+
      ","+ALIAS+".de_rp_silinviarp_original_fault_description as FESP_RpE_deRpSilinviarpOriginalFaultDescription";

  private Long mygovRpEId;
  private int version;
  @Nested(CarrelloRp.ALIAS)
  private CarrelloRp mygovCarrelloRpId;
  private String codAckE;
  private Date dtCreazioneRp;
  private Date dtUltimaModificaRp;
  private Date dtCreazioneE;
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
  private String codRpSoggVersIdUnivVersTipoIdUnivoco;
  private String codRpSoggVersIdUnivVersCodiceIdUnivoco;
  private String deRpSoggVersAnagraficaVersante;
  private String deRpSoggVersIndirizzoVersante;
  private String deRpSoggVersCivicoVersante;
  private String codRpSoggVersCapVersante;
  private String deRpSoggVersLocalitaVersante;
  private String deRpSoggVersProvinciaVersante;
  private String codRpSoggVersNazioneVersante;
  private String deRpSoggVersEmailVersante;
  private String codRpSoggPagIdUnivPagTipoIdUnivoco;
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
  private Date dtEDataOraMessaggioRicevuta;
  private String codERiferimentoMessaggioRichiesta;
  private Date dtERiferimentoDataRichiesta;
  private String codEIstitAttesIdUnivAttesTipoIdUnivoco;
  private String codEIstitAttesIdUnivAttesCodiceIdUnivoco;
  private String deEIstitAttesDenominazioneAttestante;
  private String codEIstitAttesCodiceUnitOperAttestante;
  private String deEIstitAttesDenomUnitOperAttestante;
  private String deEIstitAttesIndirizzoAttestante;
  private String deEIstitAttesCivicoAttestante;
  private String codEIstitAttesCapAttestante;
  private String deEIstitAttesLocalitaAttestante;
  private String deEIstitAttesProvinciaAttestante;
  private String codEIstitAttesNazioneAttestante;
  private String codEEnteBenefIdUnivBenefTipoIdUnivoco;
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
  private String codESoggVersIdUnivVersTipoIdUnivoco;
  private String codESoggVersIdUnivVersCodiceIdUnivoco;
  private String deESoggVersAnagraficaVersante;
  private String deESoggVersIndirizzoVersante;
  private String deESoggVersCivicoVersante;
  private String codESoggVersCapVersante;
  private String deESoggVersLocalitaVersante;
  private String deESoggVersProvinciaVersante;
  private String codESoggVersNazioneVersante;
  private String deESoggVersEmailVersante;
  private String codESoggPagIdUnivPagTipoIdUnivoco;
  private String codESoggPagIdUnivPagCodiceIdUnivoco;
  private String deESoggPagAnagraficaPagatore;
  private String deESoggPagIndirizzoPagatore;
  private String deESoggPagCivicoPagatore;
  private String codESoggPagCapPagatore;
  private String deESoggPagLocalitaPagatore;
  private String deESoggPagProvinciaPagatore;
  private String codESoggPagNazionePagatore;
  private String deESoggPagEmailPagatore;
  private String codEDatiPagCodiceEsitoPagamento;
  private BigDecimal numEDatiPagImportoTotalePagato;
  private String codEDatiPagIdUnivocoVersamento;
  private String codEDatiPagCodiceContestoPagamento;
  private String idSession;
  private Integer modelloPagamento;
  private String codESilinviaesitoOriginalFaultCode;
  private String deESilinviaesitoOriginalFaultString;
  private String deESilinviaesitoOriginalFaultDescription;
  private String codRpSilinviarpOriginalFaultCode;
  private String deRpSilinviarpOriginalFaultString;
  private String deRpSilinviarpOriginalFaultDescription;
}