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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovRpEDettaglioId")
public class RpEDettaglio extends BaseEntity {

  public static final String ALIAS = "FESP_RpEDettaglio";
  public static final String FIELDS = ""+ALIAS+".mygov_rp_e_dettaglio_id as FESP_RpEDettaglio_mygovRpEDettaglioId,"+ALIAS+".version as FESP_RpEDettaglio_version"+
      ","+ALIAS+".dt_creazione as FESP_RpEDettaglio_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as FESP_RpEDettaglio_dtUltimaModifica"+
      ","+ALIAS+".mygov_rp_e_id as FESP_RpEDettaglio_mygovRpEId"+
      ","+ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as FESP_RpEDettaglio_numRpDatiVersDatiSingVersImportoSingoloVersamento"+
      ","+ALIAS+".num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as FESP_RpEDettaglio_numRpDatiVersDatiSingVersCommissioneCaricoPa"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_iban_accredito as FESP_RpEDettaglio_codRpDatiVersDatiSingVersIbanAccredito"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_bic_accredito as FESP_RpEDettaglio_codRpDatiVersDatiSingVersBicAccredito"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_iban_appoggio as FESP_RpEDettaglio_codRpDatiVersDatiSingVersIbanAppoggio"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_bic_appoggio as FESP_RpEDettaglio_codRpDatiVersDatiSingVersBicAppoggio"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore as FESP_RpEDettaglio_codRpDatiVersDatiSingVersCredenzialiPagatore"+
      ","+ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento as FESP_RpEDettaglio_deRpDatiVersDatiSingVersCausaleVersamento"+
      ","+ALIAS+".de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione as FESP_RpEDettaglio_deRpDatiVersDatiSingVersDatiSpecificiRiscossione"+
      ","+ALIAS+".num_e_dati_pag_dati_sing_pag_singolo_importo_pagato as FESP_RpEDettaglio_numEDatiPagDatiSingPagSingoloImportoPagato"+
      ","+ALIAS+".de_e_dati_pag_dati_sing_pag_esito_singolo_pagamento as FESP_RpEDettaglio_deEDatiPagDatiSingPagEsitoSingoloPagamento"+
      ","+ALIAS+".dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento as FESP_RpEDettaglio_dtEDatiPagDatiSingPagDataEsitoSingoloPagamento"+
      ","+ALIAS+".cod_e_dati_pag_dati_sing_pag_id_univoco_riscoss as FESP_RpEDettaglio_codEDatiPagDatiSingPagIdUnivocoRiscoss"+
      ","+ALIAS+".de_e_dati_pag_dati_sing_pag_causale_versamento as FESP_RpEDettaglio_deEDatiPagDatiSingPagCausaleVersamento"+
      ","+ALIAS+".de_e_dati_pag_dati_sing_pag_dati_specifici_riscossione as FESP_RpEDettaglio_deEDatiPagDatiSingPagDatiSpecificiRiscossione"+
      ","+ALIAS+".num_e_dati_pag_dati_sing_pag_commissioni_applicate_psp as FESP_RpEDettaglio_numEDatiPagDatiSingPagCommissioniApplicatePsp"+
      ","+ALIAS+".cod_e_dati_pag_dati_sing_pag_allegato_ricevuta_tipo as FESP_RpEDettaglio_codEDatiPagDatiSingPagAllegatoRicevutaTipo"+
      ","+ALIAS+".blb_e_dati_pag_dati_sing_pag_allegato_ricevuta_test as FESP_RpEDettaglio_blbEDatiPagDatiSingPagAllegatoRicevutaTest"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo as FESP_RpEDettaglio_codRpDatiVersDatiSingVersDatiMbdTipoBollo"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_dati_mbd_hash_documento as FESP_RpEDettaglio_codRpDatiVersDatiSingVersDatiMbdHashDocumento"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza as FESP_RpEDettaglio_codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza";

  private Long mygovRpEDettaglioId;
  private int version;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  @Nested(RpE.ALIAS)
  private RpE mygovRpEId;
  private BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento;
  private BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa;
  private String codRpDatiVersDatiSingVersIbanAccredito;
  private String codRpDatiVersDatiSingVersBicAccredito;
  private String codRpDatiVersDatiSingVersIbanAppoggio;
  private String codRpDatiVersDatiSingVersBicAppoggio;
  private String codRpDatiVersDatiSingVersCredenzialiPagatore;
  private String deRpDatiVersDatiSingVersCausaleVersamento;
  private String deRpDatiVersDatiSingVersDatiSpecificiRiscossione;
  private BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato;
  private String deEDatiPagDatiSingPagEsitoSingoloPagamento;
  private Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento;
  private String codEDatiPagDatiSingPagIdUnivocoRiscoss;
  private String deEDatiPagDatiSingPagCausaleVersamento;
  private String deEDatiPagDatiSingPagDatiSpecificiRiscossione;
  private BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp;
  private String codEDatiPagDatiSingPagAllegatoRicevutaTipo;
  private byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest;
  private String codRpDatiVersDatiSingVersDatiMbdTipoBollo;
  private String codRpDatiVersDatiSingVersDatiMbdHashDocumento;
  private String codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza;
}
