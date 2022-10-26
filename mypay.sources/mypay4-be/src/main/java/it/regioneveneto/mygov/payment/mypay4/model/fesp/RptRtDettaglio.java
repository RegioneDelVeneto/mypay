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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovRptRtDettaglioId")
public class RptRtDettaglio {

  public final static String ALIAS = "FESP_RptRtDettaglio";
  public final static String FIELDS = ""+ALIAS+".mygov_rpt_rt_dettaglio_id as FESP_RptRtDettaglio_mygovRptRtDettaglioId"+
      ","+ALIAS+".version as FESP_RptRtDettaglio_version,"+ALIAS+".dt_creazione as FESP_RptRtDettaglio_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as FESP_RptRtDettaglio_dtUltimaModifica"+
      ","+ALIAS+".mygov_rpt_rt_id as FESP_RptRtDettaglio_mygovRptRtId"+
      ","+ALIAS+".num_rpt_dati_vers_dati_sing_vers_importo_singolo_versamento as FESP_RptRtDettaglio_numRptDatiVersDatiSingVersImportoSingoloVersamento"+
      ","+ALIAS+".num_rpt_dati_vers_dati_sing_vers_commissione_carico_pa as FESP_RptRtDettaglio_numRptDatiVersDatiSingVersCommissioneCaricoPa"+
      ","+ALIAS+".de_rpt_dati_vers_dati_sing_vers_iban_accredito as FESP_RptRtDettaglio_deRptDatiVersDatiSingVersIbanAccredito"+
      ","+ALIAS+".de_rpt_dati_vers_dati_sing_vers_bic_accredito as FESP_RptRtDettaglio_deRptDatiVersDatiSingVersBicAccredito"+
      ","+ALIAS+".de_rpt_dati_vers_dati_sing_vers_iban_appoggio as FESP_RptRtDettaglio_deRptDatiVersDatiSingVersIbanAppoggio"+
      ","+ALIAS+".de_rpt_dati_vers_dati_sing_vers_bic_appoggio as FESP_RptRtDettaglio_deRptDatiVersDatiSingVersBicAppoggio"+
      ","+ALIAS+".cod_rpt_dati_vers_dati_sing_vers_credenziali_pagatore as FESP_RptRtDettaglio_codRptDatiVersDatiSingVersCredenzialiPagatore"+
      ","+ALIAS+".de_rpt_dati_vers_dati_sing_vers_causale_versamento as FESP_RptRtDettaglio_deRptDatiVersDatiSingVersCausaleVersamento"+
      ","+ALIAS+".de_rpt_dati_vers_dati_sing_vers_dati_specifici_riscossione as FESP_RptRtDettaglio_deRptDatiVersDatiSingVersDatiSpecificiRiscossione"+
      ","+ALIAS+".num_rt_dati_pag_dati_sing_pag_singolo_importo_pagato as FESP_RptRtDettaglio_numRtDatiPagDatiSingPagSingoloImportoPagato"+
      ","+ALIAS+".de_rt_dati_pag_dati_sing_pag_esito_singolo_pagamento as FESP_RptRtDettaglio_deRtDatiPagDatiSingPagEsitoSingoloPagamento"+
      ","+ALIAS+".dt_rt_dati_pag_dati_sing_pag_data_esito_singolo_pagamento as FESP_RptRtDettaglio_dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento"+
      ","+ALIAS+".cod_rt_dati_pag_dati_sing_pag_id_univoco_riscossione as FESP_RptRtDettaglio_codRtDatiPagDatiSingPagIdUnivocoRiscossione"+
      ","+ALIAS+".de_rt_dati_pag_dati_sing_pag_causale_versamento as FESP_RptRtDettaglio_deRtDatiPagDatiSingPagCausaleVersamento"+
      ","+ALIAS+".de_rt_dati_pag_dati_sing_pag_dati_specifici_riscossione as FESP_RptRtDettaglio_deRtDatiPagDatiSingPagDatiSpecificiRiscossione"+
      ","+ALIAS+".num_rt_dati_pag_dati_sing_pag_commissioni_applicate_psp as FESP_RptRtDettaglio_numRtDatiPagDatiSingPagCommissioniApplicatePsp"+
      ","+ALIAS+".cod_rt_dati_pag_dati_sing_pag_allegato_ricevuta_tipo as FESP_RptRtDettaglio_codRtDatiPagDatiSingPagAllegatoRicevutaTipo"+
      ","+ALIAS+".blb_rt_dati_pag_dati_sing_pag_allegato_ricevuta_test as FESP_RptRtDettaglio_blbRtDatiPagDatiSingPagAllegatoRicevutaTest"+
      ","+ALIAS+".cod_rpt_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo as FESP_RptRtDettaglio_codRptDatiVersDatiSingVersDatiMbdTipoBollo"+
      ","+ALIAS+".cod_rpt_dati_vers_dati_sing_vers_dati_mbd_hash_documento as FESP_RptRtDettaglio_codRptDatiVersDatiSingVersDatiMbdHashDocumento"+
      ","+ALIAS+".cod_rpt_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza as FESP_RptRtDettaglio_codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza";

  private Long mygovRptRtDettaglioId;
  private int version;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  @Nested(RptRt.ALIAS)
  private RptRt mygovRptRtId;
  private BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento;
  private BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa;
  private String deRptDatiVersDatiSingVersIbanAccredito;
  private String deRptDatiVersDatiSingVersBicAccredito;
  private String deRptDatiVersDatiSingVersIbanAppoggio;
  private String deRptDatiVersDatiSingVersBicAppoggio;
  private String codRptDatiVersDatiSingVersCredenzialiPagatore;
  private String deRptDatiVersDatiSingVersCausaleVersamento;
  private String deRptDatiVersDatiSingVersDatiSpecificiRiscossione;
  private BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato;
  private String deRtDatiPagDatiSingPagEsitoSingoloPagamento;
  private Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento;
  private String codRtDatiPagDatiSingPagIdUnivocoRiscossione;
  private String deRtDatiPagDatiSingPagCausaleVersamento;
  private String deRtDatiPagDatiSingPagDatiSpecificiRiscossione;
  private BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp;
  private String codRtDatiPagDatiSingPagAllegatoRicevutaTipo;
  private byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest;
  private String codRptDatiVersDatiSingVersDatiMbdTipoBollo;
  private String codRptDatiVersDatiSingVersDatiMbdHashDocumento;
  private String codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza;
}
