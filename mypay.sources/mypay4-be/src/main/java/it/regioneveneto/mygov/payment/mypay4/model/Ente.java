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
import org.jdbi.v3.core.mapper.Nested;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovEnteId")
public class Ente extends BaseEntity {

  public final static String TABLE = "pa.mygov_ente";
  public final static String ALIAS = "Ente";
  public final static String FIELDS_WITHOUT_LOGO = ""+ALIAS+".mygov_ente_id as Ente_mygovEnteId,"+ALIAS+".cod_ipa_ente as Ente_codIpaEnte"+
    ","+ALIAS+".codice_fiscale_ente as Ente_codiceFiscaleEnte,"+ALIAS+".de_nome_ente as Ente_deNomeEnte"+
    ","+ALIAS+".email_amministratore as Ente_emailAmministratore,"+ALIAS+".dt_creazione as Ente_dtCreazione"+
    ","+ALIAS+".dt_ultima_modifica as Ente_dtUltimaModifica"+
    ","+ALIAS+".cod_rp_dati_vers_tipo_versamento as Ente_codRpDatiVersTipoVersamento"+
    ","+ALIAS+".num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as Ente_numRpDatiVersDatiSingVersCommissioneCaricoPa"+
    ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_iban_accredito as Ente_codRpDatiVersDatiSingVersIbanAccredito"+
    ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_bic_accredito as Ente_codRpDatiVersDatiSingVersBicAccredito"+
    ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_iban_appoggio as Ente_codRpDatiVersDatiSingVersIbanAppoggio"+
    ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_bic_appoggio as Ente_codRpDatiVersDatiSingVersBicAppoggio"+
    ","+ALIAS+".mybox_client_key as Ente_myboxClientKey,"+ALIAS+".mybox_client_secret as Ente_myboxClientSecret"+
    ","+ALIAS+".ente_sil_invia_risposta_pagamento_url as Ente_enteSilInviaRispostaPagamentoUrl"+
    ","+ALIAS+".cod_global_location_number as Ente_codGlobalLocationNumber,"+ALIAS+".de_password as Ente_dePassword"+
    ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_bic_accredito_seller as Ente_codRpDatiVersDatiSingVersBicAccreditoSeller"+
    ","+ALIAS+".de_rp_ente_benef_denominazione_beneficiario as Ente_deRpEnteBenefDenominazioneBeneficiario"+
    ","+ALIAS+".de_rp_ente_benef_indirizzo_beneficiario as Ente_deRpEnteBenefIndirizzoBeneficiario"+
    ","+ALIAS+".de_rp_ente_benef_civico_beneficiario as Ente_deRpEnteBenefCivicoBeneficiario"+
    ","+ALIAS+".cod_rp_ente_benef_cap_beneficiario as Ente_codRpEnteBenefCapBeneficiario"+
    ","+ALIAS+".de_rp_ente_benef_localita_beneficiario as Ente_deRpEnteBenefLocalitaBeneficiario"+
    ","+ALIAS+".de_rp_ente_benef_provincia_beneficiario as Ente_deRpEnteBenefProvinciaBeneficiario"+
    ","+ALIAS+".cod_rp_ente_benef_nazione_beneficiario as Ente_codRpEnteBenefNazioneBeneficiario"+
    ","+ALIAS+".de_rp_ente_benef_telefono_beneficiario as Ente_deRpEnteBenefTelefonoBeneficiario"+
    ","+ALIAS+".de_rp_ente_benef_sito_web_beneficiario as Ente_deRpEnteBenefSitoWebBeneficiario"+
    ","+ALIAS+".de_rp_ente_benef_email_beneficiario as Ente_deRpEnteBenefEmailBeneficiario"+
    ","+ALIAS+".application_code as Ente_applicationCode"+
    ","+ALIAS+".cod_codice_interbancario_cbill as Ente_codCodiceInterbancarioCbill"+
    ","+ALIAS+".de_informazioni_ente as Ente_deInformazioniEnte"+
    ","+ALIAS+".de_autorizzazione as Ente_deAutorizzazione,"+ALIAS+".cd_stato_ente as Ente_cdStatoEnte"+
    ","+ALIAS+".de_url_esterni_attiva as Ente_deUrlEsterniAttiva,"+ALIAS+".lingua_aggiuntiva as Ente_linguaAggiuntiva"+
    ","+ALIAS+".cod_tipo_ente as Ente_codTipoEnte"+
    ","+ALIAS+".dt_avvio as Ente_dtAvvio";
  public final static String FIELDS = FIELDS_WITHOUT_LOGO+
    ","+ALIAS+".de_logo_ente as Ente_deLogoEnte";

  private Long mygovEnteId;
  private String codIpaEnte;
  private String codiceFiscaleEnte;
  private String deNomeEnte;
  private String emailAmministratore;
  private Timestamp dtCreazione;
  private Timestamp dtUltimaModifica;
  private String codRpDatiVersTipoVersamento;
  private BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa;
  private String codRpDatiVersDatiSingVersIbanAccredito;
  private String codRpDatiVersDatiSingVersBicAccredito;
  private String codRpDatiVersDatiSingVersIbanAppoggio;
  private String codRpDatiVersDatiSingVersBicAppoggio;
  private String myboxClientKey;
  private String myboxClientSecret;
  private String enteSilInviaRispostaPagamentoUrl;
  private String codGlobalLocationNumber;
  private String dePassword;
  private Boolean codRpDatiVersDatiSingVersBicAccreditoSeller;
  private String deRpEnteBenefDenominazioneBeneficiario;
  private String deRpEnteBenefIndirizzoBeneficiario;
  private String deRpEnteBenefCivicoBeneficiario;
  private String codRpEnteBenefCapBeneficiario;
  private String deRpEnteBenefLocalitaBeneficiario;
  private String deRpEnteBenefProvinciaBeneficiario;
  private String codRpEnteBenefNazioneBeneficiario;
  private String deRpEnteBenefTelefonoBeneficiario;
  private String deRpEnteBenefSitoWebBeneficiario;
  private String deRpEnteBenefEmailBeneficiario;
  private String applicationCode;
  private String codCodiceInterbancarioCbill;
  private String deInformazioniEnte;
  private String deLogoEnte;
  private String deAutorizzazione;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato cdStatoEnte;
  private String deUrlEsterniAttiva;
  private String linguaAggiuntiva;
  private String codTipoEnte;
  private LocalDate dtAvvio;
}
