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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovDovutoId")
public class Dovuto extends BaseEntity {

  // Special handling for field località(=comune): truncate if > 35 char (i.e. max length on DB)
  //   This special handling is due to località selected by select list nazione/provincia/comune and on list of comuni
  //   there are some values whose length > 35. The choice of 35 chars is due to max length of corresponding field
  //   on PagoPA api.
  public final static int MAX_LENGTH_LOCALITA = 35;

  public final static String ALIAS = "Dovuto";
  public final static String FIELDS = ""+ALIAS+".mygov_dovuto_id as Dovuto_mygovDovutoId,"+ALIAS+".version as Dovuto_version"+
      ","+ALIAS+".flg_dovuto_attuale as Dovuto_flgDovutoAttuale,"+ALIAS+".mygov_flusso_id as Dovuto_mygovFlussoId"+
      ","+ALIAS+".num_riga_flusso as Dovuto_numRigaFlusso,"+ALIAS+".mygov_anagrafica_stato_id as Dovuto_mygovAnagraficaStatoId"+
      ","+ALIAS+".mygov_carrello_id as Dovuto_mygovCarrelloId,"+ALIAS+".cod_iud as Dovuto_codIud"+
      ","+ALIAS+".cod_iuv as Dovuto_codIuv,"+ALIAS+".dt_creazione as Dovuto_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as Dovuto_dtUltimaModifica"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as Dovuto_codRpSoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco as Dovuto_codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".de_rp_sogg_pag_anagrafica_pagatore as Dovuto_deRpSoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_indirizzo_pagatore as Dovuto_deRpSoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_civico_pagatore as Dovuto_deRpSoggPagCivicoPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_cap_pagatore as Dovuto_codRpSoggPagCapPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_localita_pagatore as Dovuto_deRpSoggPagLocalitaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_provincia_pagatore as Dovuto_deRpSoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_nazione_pagatore as Dovuto_codRpSoggPagNazionePagatore"+
      ","+ALIAS+".de_rp_sogg_pag_email_pagatore as Dovuto_deRpSoggPagEmailPagatore"+
      ","+ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento as Dovuto_dtRpDatiVersDataEsecuzionePagamento"+
      ","+ALIAS+".cod_rp_dati_vers_tipo_versamento as Dovuto_codRpDatiVersTipoVersamento"+
      ","+ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as Dovuto_numRpDatiVersDatiSingVersImportoSingoloVersamento"+
      ","+ALIAS+".num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as Dovuto_numRpDatiVersDatiSingVersCommissioneCaricoPa"+
      ","+ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento as Dovuto_deRpDatiVersDatiSingVersCausaleVersamento"+
      ","+ALIAS+".de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione as Dovuto_deRpDatiVersDatiSingVersDatiSpecificiRiscossione"+
      ","+ALIAS+".cod_tipo_dovuto as Dovuto_codTipoDovuto,"+ALIAS+".mygov_avviso_id as Dovuto_mygovAvvisoId"+
      ","+ALIAS+".dt_creazione_cod_iuv as Dovuto_dtCreazioneCodIuv"+
      ","+ALIAS+".mygov_dati_marca_bollo_digitale_id as Dovuto_mygovDatiMarcaBolloDigitaleId"+
      ","+ALIAS+".de_causale_visualizzata as Dovuto_deCausaleVisualizzata,"+ALIAS+".bilancio as Dovuto_bilancio"+
      ","+ALIAS+".flg_genera_iuv as Dovuto_flgGeneraIuv,"+ALIAS+".id_session as Dovuto_idSession";


  private Long mygovDovutoId;
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
  private Date dtCreazione;
  private Date dtUltimaModifica;
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
  private String codRpDatiVersTipoVersamento;
  private BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento;
  private BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa;
  private String deRpDatiVersDatiSingVersCausaleVersamento;
  private String deRpDatiVersDatiSingVersDatiSpecificiRiscossione;
  private String codTipoDovuto;
  @Nested(Avviso.ALIAS)
  private Avviso mygovAvvisoId;
  private Date dtCreazioneCodIuv;
  private Long mygovDatiMarcaBolloDigitaleId;
  private String deCausaleVisualizzata;
  private String bilancio;
  private boolean flgGeneraIuv;
  private String idSession;

  @Nested(Ente.ALIAS)
  private Ente nestedEnte; // Ente joined by mygov_flusso.
  @Nested(Avviso.ALIAS)
  private Avviso nestedAvviso;
}
