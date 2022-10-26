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

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovEnteId")
public class Ente extends BaseEntity {

  public final static String ALIAS = "FESP_Ente";
  public final static String FIELDS = ""+ALIAS+".mygov_ente_id as FESP_Ente_mygovEnteId,"+ALIAS+".cod_ipa_ente as FESP_Ente_codIpaEnte"+
      ","+ALIAS+".codice_fiscale_ente as FESP_Ente_codiceFiscaleEnte,"+ALIAS+".de_nome_ente as FESP_Ente_deNomeEnte"+
      ","+ALIAS+".email_amministratore as FESP_Ente_emailAmministratore,"+ALIAS+".dt_creazione as FESP_Ente_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as FESP_Ente_dtUltimaModifica"+
      ","+ALIAS+".de_rp_inviarp_tipo_firma as FESP_Ente_deRpInviarpTipoFirma"+
      ","+ALIAS+".cod_rp_ente_benef_id_univ_benef_tipo_id_univoco as FESP_Ente_codRpEnteBenefIdUnivBenefTipoIdUnivoco"+
      ","+ALIAS+".cod_rp_ente_benef_id_univ_benef_codice_id_univoco as FESP_Ente_codRpEnteBenefIdUnivBenefCodiceIdUnivoco"+
      ","+ALIAS+".de_rp_ente_benef_denominazione_beneficiario as FESP_Ente_deRpEnteBenefDenominazioneBeneficiario"+
      ","+ALIAS+".cod_rp_ente_benef_codice_unit_oper_beneficiario as FESP_Ente_codRpEnteBenefCodiceUnitOperBeneficiario"+
      ","+ALIAS+".de_rp_ente_benef_denom_unit_oper_beneficiario as FESP_Ente_deRpEnteBenefDenomUnitOperBeneficiario"+
      ","+ALIAS+".de_rp_ente_benef_indirizzo_beneficiario as FESP_Ente_deRpEnteBenefIndirizzoBeneficiario"+
      ","+ALIAS+".de_rp_ente_benef_civico_beneficiario as FESP_Ente_deRpEnteBenefCivicoBeneficiario"+
      ","+ALIAS+".cod_rp_ente_benef_cap_beneficiario as FESP_Ente_codRpEnteBenefCapBeneficiario"+
      ","+ALIAS+".de_rp_ente_benef_localita_beneficiario as FESP_Ente_deRpEnteBenefLocalitaBeneficiario"+
      ","+ALIAS+".de_rp_ente_benef_provincia_beneficiario as FESP_Ente_deRpEnteBenefProvinciaBeneficiario"+
      ","+ALIAS+".cod_rp_ente_benef_nazione_beneficiario as FESP_Ente_codRpEnteBenefNazioneBeneficiario"+
      ","+ALIAS+".cod_rp_dati_vers_firma_ricevuta as FESP_Ente_codRpDatiVersFirmaRicevuta"+
      ","+ALIAS+".paa_sil_invia_risposta_pagamento_url as FESP_Ente_paaSilInviaRispostaPagamentoUrl"+
      ","+ALIAS+".de_wisp_url_back as FESP_Ente_deWispUrlBack,"+ALIAS+".de_wisp_url_return as FESP_Ente_deWispUrlReturn"+
      ","+ALIAS+".cod_codice_segregazione as FESP_Ente_codCodiceSegregazione"+
      ","+ALIAS+".de_url_esterni_attiva as FESP_Ente_deUrlEsterniAttiva";

  private Long mygovEnteId;
  private String codIpaEnte;
  private String codiceFiscaleEnte;
  private String deNomeEnte;
  private String emailAmministratore;
  private Timestamp dtCreazione;
  private Timestamp dtUltimaModifica;
  private String deRpInviarpTipoFirma;
  private String codRpEnteBenefIdUnivBenefTipoIdUnivoco;
  private String codRpEnteBenefIdUnivBenefCodiceIdUnivoco;
  private String deRpEnteBenefDenominazioneBeneficiario;
  private String codRpEnteBenefCodiceUnitOperBeneficiario;
  private String deRpEnteBenefDenomUnitOperBeneficiario;
  private String deRpEnteBenefIndirizzoBeneficiario;
  private String deRpEnteBenefCivicoBeneficiario;
  private String codRpEnteBenefCapBeneficiario;
  private String deRpEnteBenefLocalitaBeneficiario;
  private String deRpEnteBenefProvinciaBeneficiario;
  private String codRpEnteBenefNazioneBeneficiario;
  private String codRpDatiVersFirmaRicevuta;
  private String paaSilInviaRispostaPagamentoUrl;
  private String deWispUrlBack;
  private String deWispUrlReturn;
  private String codCodiceSegregazione;
  private String deUrlEsterniAttiva;
}
