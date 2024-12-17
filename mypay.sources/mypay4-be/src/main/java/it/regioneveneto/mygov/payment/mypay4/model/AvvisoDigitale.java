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
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovAvvisoDigitaleId")
public class AvvisoDigitale {

  public static final String ALIAS = "AvvisoDigitale";
  public static final String FIELDS = ""+ALIAS+".mygov_avviso_digitale_id as AvvisoDigitale_mygovAvvisoDigitaleId,"+ALIAS+".version as AvvisoDigitale_version"+
      ","+ALIAS+".cod_ad_id_dominio as AvvisoDigitale_codAdIdDominio"+
      ","+ALIAS+".de_ad_anag_beneficiario as AvvisoDigitale_deAdAnagBeneficiario"+
      ","+ALIAS+".cod_ad_id_messaggio_richiesta as AvvisoDigitale_codAdIdMessaggioRichiesta"+
      ","+ALIAS+".cod_ad_cod_avviso as AvvisoDigitale_codAdCodAvviso"+
      ","+ALIAS+".de_ad_sog_pag_anag_pagatore as AvvisoDigitale_deAdSogPagAnagPagatore"+
      ","+ALIAS+".cod_ad_sog_pag_id_univ_pag_tipo_id_univ as AvvisoDigitale_codAdSogPagIdUnivPagTipoIdUniv"+
      ","+ALIAS+".cod_ad_sog_pag_id_univ_pag_cod_id_univ as AvvisoDigitale_codAdSogPagIdUnivPagCodIdUniv"+
      ","+ALIAS+".dt_ad_data_scadenza_pagamento as AvvisoDigitale_dtAdDataScadenzaPagamento"+
      ","+ALIAS+".dt_ad_data_scadenza_avviso as AvvisoDigitale_dtAdDataScadenzaAvviso"+
      ","+ALIAS+".num_ad_importo_avviso as AvvisoDigitale_numAdImportoAvviso"+
      ","+ALIAS+".de_ad_email_soggetto as AvvisoDigitale_deAdEmailSoggetto"+
      ","+ALIAS+".de_ad_cellulare_soggetto as AvvisoDigitale_deAdCellulareSoggetto"+
      ","+ALIAS+".de_ad_desc_pagamento as AvvisoDigitale_deAdDescPagamento"+
      ","+ALIAS+".de_ad_url_avviso as AvvisoDigitale_deAdUrlAvviso,"+ALIAS+".cod_id_flusso_av as AvvisoDigitale_codIdFlussoAv"+
      ","+ALIAS+".cod_e_ad_id_dominio as AvvisoDigitale_codEAdIdDominio"+
      ","+ALIAS+".cod_e_ad_id_messaggio_richiesta as AvvisoDigitale_codEAdIdMessaggioRichiesta"+
      ","+ALIAS+".cod_id_flusso_e as AvvisoDigitale_codIdFlussoE"+
      ","+ALIAS+".mygov_anagrafica_stato_id as AvvisoDigitale_mygovAnagraficaStatoId"+
      ","+ALIAS+".num_ad_tentativi_invio as AvvisoDigitale_numAdTentativiInvio"+
      ","+ALIAS+".dt_creazione as AvvisoDigitale_dtCreazione,"+ALIAS+".dt_ultima_modifica as AvvisoDigitale_dtUltimaModifica"+
      ","+ALIAS+".cod_tassonomia_avviso as AvvisoDigitale_codTassonomiaAvviso"+
      ","+ALIAS+".dati_sing_vers_iban_accredito as AvvisoDigitale_datiSingVersIbanAccredito"+
      ","+ALIAS+".dati_sing_vers_iban_appoggio as AvvisoDigitale_datiSingVersIbanAppoggio"+
      ","+ALIAS+".tipo_pagamento as AvvisoDigitale_tipoPagamento,"+ALIAS+".tipo_operazione as AvvisoDigitale_tipoOperazione";

  private Long mygovAvvisoDigitaleId;
  private int version;
  private String codAdIdDominio;
  private String deAdAnagBeneficiario;
  private String codAdIdMessaggioRichiesta;
  private String codAdCodAvviso;
  private String deAdSogPagAnagPagatore;
  private String codAdSogPagIdUnivPagTipoIdUniv;
  private String codAdSogPagIdUnivPagCodIdUniv;
  private Date dtAdDataScadenzaPagamento;
  private Date dtAdDataScadenzaAvviso;
  private BigDecimal numAdImportoAvviso;
  private String deAdEmailSoggetto;
  private String deAdCellulareSoggetto;
  private String deAdDescPagamento;
  private String deAdUrlAvviso;
  private String codIdFlussoAv;
  private String codEAdIdDominio;
  private String codEAdIdMessaggioRichiesta;
  private String codIdFlussoE;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
  private Integer numAdTentativiInvio;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private String codTassonomiaAvviso;
  private String datiSingVersIbanAccredito;
  private String datiSingVersIbanAppoggio;
  private Integer tipoPagamento;
  private String tipoOperazione;

}
