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

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovEnteTipoDovutoId")
public class EnteTipoDovuto extends BaseEntity {

  public static final String ALIAS = "EnteTipoDovuto";
  public static final String FIELDS = ""+ALIAS+".mygov_ente_tipo_dovuto_id as EnteTipoDovuto_mygovEnteTipoDovutoId"+
      ","+ALIAS+".mygov_ente_id as EnteTipoDovuto_mygovEnteId,"+ALIAS+".cod_tipo as EnteTipoDovuto_codTipo"+
      ","+ALIAS+".de_tipo as EnteTipoDovuto_deTipo,"+ALIAS+".iban_accredito_pi as EnteTipoDovuto_ibanAccreditoPi"+
      ","+ALIAS+".bic_accredito_pi as EnteTipoDovuto_bicAccreditoPi"+
      ","+ALIAS+".iban_appoggio_pi as EnteTipoDovuto_ibanAppoggioPi,"+ALIAS+".bic_appoggio_pi as EnteTipoDovuto_bicAppoggioPi"+
      ","+ALIAS+".iban_accredito_psp as EnteTipoDovuto_ibanAccreditoPsp"+
      ","+ALIAS+".bic_accredito_psp as EnteTipoDovuto_bicAccreditoPsp"+
      ","+ALIAS+".iban_appoggio_psp as EnteTipoDovuto_ibanAppoggioPsp"+
      ","+ALIAS+".bic_appoggio_psp as EnteTipoDovuto_bicAppoggioPsp"+
      ","+ALIAS+".cod_conto_corrente_postale as EnteTipoDovuto_codContoCorrentePostale"+
      ","+ALIAS+".cod_xsd_causale as EnteTipoDovuto_codXsdCausale"+
      ","+ALIAS+".bic_accredito_pi_seller as EnteTipoDovuto_bicAccreditoPiSeller"+
      ","+ALIAS+".bic_accredito_psp_seller as EnteTipoDovuto_bicAccreditoPspSeller"+
      ","+ALIAS+".spontaneo as EnteTipoDovuto_spontaneo,"+ALIAS+".importo as EnteTipoDovuto_importo"+
      ","+ALIAS+".de_url_pagamento_dovuto as EnteTipoDovuto_deUrlPagamentoDovuto"+
      ","+ALIAS+".de_bilancio_default as EnteTipoDovuto_deBilancioDefault"+
      ","+ALIAS+".flg_cf_anonimo as EnteTipoDovuto_flgCfAnonimo"+
      ","+ALIAS+".flg_scadenza_obbligatoria as EnteTipoDovuto_flgScadenzaObbligatoria"+
      ","+ALIAS+".flg_stampa_data_scadenza as EnteTipoDovuto_flgStampaDataScadenza"+
      ","+ALIAS+".de_intestatario_cc_postale as EnteTipoDovuto_deIntestatarioCcPostale"+
      ","+ALIAS+".de_settore_ente as EnteTipoDovuto_deSettoreEnte,"+ALIAS+".flg_notifica_io as EnteTipoDovuto_flgNotificaIo"+
      ","+ALIAS+".flg_notifica_esito_push as EnteTipoDovuto_flgNotificaEsitoPush"+
      ","+ALIAS+".max_tentativi_inoltro_esito as EnteTipoDovuto_maxTentativiInoltroEsito"+
      ","+ALIAS+".mygov_ente_sil_id as EnteTipoDovuto_mygovEnteSilId,"+ALIAS+".flg_attivo as EnteTipoDovuto_flgAttivo"+
      ","+ALIAS+".codice_contesto_pagamento as EnteTipoDovuto_codiceContestoPagamento"+
      ","+ALIAS+".flg_disabilita_stampa_avviso as EnteTipoDovuto_flgDisabilitaStampaAvviso"+
      ","+ALIAS+".macro_area as EnteTipoDovuto_macroArea,"+ALIAS+".tipo_servizio as EnteTipoDovuto_tipoServizio"+
      ","+ALIAS+".motivo_riscossione as EnteTipoDovuto_motivoRiscossione"+
      ","+ALIAS+".cod_tassonomico as EnteTipoDovuto_codTassonomico" +
             ","+ALIAS+".url_notifica_pnd as EnteTipoDovuto_urlNotificaPnd"+
          ","+ALIAS+".user_pnd as EnteTipoDovuto_userPnd"+
          ","+ALIAS+".psw_pnd as EnteTipoDovuto_pswPnd"+
          ","+ALIAS+".url_notifica_attualizzazione_pnd as EnteTipoDovuto_urlNotificaAttualizzazionePnd";


  private Long mygovEnteTipoDovutoId;
  @Nested(Ente.ALIAS)
  private Ente mygovEnteId;
  private String codTipo;
  private String deTipo;
  private String ibanAccreditoPi;
  private String bicAccreditoPi;
  private String ibanAppoggioPi;
  private String bicAppoggioPi;
  private String ibanAccreditoPsp;
  private String bicAccreditoPsp;
  private String ibanAppoggioPsp;
  private String bicAppoggioPsp;
  private String codContoCorrentePostale;
  private String codXsdCausale;
  private boolean bicAccreditoPiSeller;
  private boolean bicAccreditoPspSeller;
  private boolean spontaneo;
  private BigDecimal importo;
  private String deUrlPagamentoDovuto;
  private String deBilancioDefault;
  private boolean flgCfAnonimo;
  private boolean flgScadenzaObbligatoria;
  private boolean flgStampaDataScadenza;
  private String deIntestatarioCcPostale;
  private String deSettoreEnte;
  private boolean flgNotificaIo;
  private boolean flgNotificaEsitoPush;
  private Integer maxTentativiInoltroEsito;
  private Long mygovEnteSilId;
  private boolean flgAttivo;
  private String codiceContestoPagamento;
  private boolean flgDisabilitaStampaAvviso;
  private String macroArea;
  private String tipoServizio;
  private String motivoRiscossione;
  private String codTassonomico;
  private String urlNotificaPnd;
  private String userPnd;
  private String pswPnd;
  private String urlNotificaAttualizzazionePnd;
}
