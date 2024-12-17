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
package it.regioneveneto.mygov.payment.mypay4.dao.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.BaseDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRtDettaglio;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface RptRtDettaglioDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_rpt_rt_dettaglio (" +
         "  mygov_rpt_rt_dettaglio_id" +
          ", version" +
          ", dt_creazione" +
          ", dt_ultima_modifica" +
          ", mygov_rpt_rt_id" +
          ", mygov_carrello_rpt_pagopa_id" +
          ", num_rpt_dati_vers_dati_sing_vers_importo_singolo_versamento" +
          ", num_rpt_dati_vers_dati_sing_vers_commissione_carico_pa" +
          ", de_rpt_dati_vers_dati_sing_vers_iban_accredito" +
          ", de_rpt_dati_vers_dati_sing_vers_bic_accredito" +
          ", de_rpt_dati_vers_dati_sing_vers_iban_appoggio" +
          ", de_rpt_dati_vers_dati_sing_vers_bic_appoggio" +
          ", cod_rpt_dati_vers_dati_sing_vers_credenziali_pagatore" +
          ", de_rpt_dati_vers_dati_sing_vers_causale_versamento" +
          ", de_rpt_dati_vers_dati_sing_vers_dati_specifici_riscossione" +
          ", num_rt_dati_pag_dati_sing_pag_singolo_importo_pagato" +
          ", de_rt_dati_pag_dati_sing_pag_esito_singolo_pagamento" +
          ", dt_rt_dati_pag_dati_sing_pag_data_esito_singolo_pagamento" +
          ", cod_rt_dati_pag_dati_sing_pag_id_univoco_riscossione" +
          ", de_rt_dati_pag_dati_sing_pag_causale_versamento" +
          ", de_rt_dati_pag_dati_sing_pag_dati_specifici_riscossione" +
          ", num_rt_dati_pag_dati_sing_pag_commissioni_applicate_psp" +
          ", cod_rt_dati_pag_dati_sing_pag_allegato_ricevuta_tipo" +
          ", blb_rt_dati_pag_dati_sing_pag_allegato_ricevuta_test" +
          ", cod_rpt_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo" +
          ", cod_rpt_dati_vers_dati_sing_vers_dati_mbd_hash_documento" +
          ", cod_rpt_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza" +
          ") values (" +
         "  nextval('mygov_rpt_rt_dettaglio_mygov_rpt_rt_dettaglio_id_seq')" +
          ", :d.version" +
          ", coalesce(:d.dtCreazione, now())" +
          ", coalesce(:d.dtUltimaModifica, now())" +
          ", :d.mygovRptRtId.mygovRptRtId" +
          ", :d.mygovCarrelloRptPagopaId"+
          ", :d.numRptDatiVersDatiSingVersImportoSingoloVersamento" +
          ", :d.numRptDatiVersDatiSingVersCommissioneCaricoPa" +
          ", :d.deRptDatiVersDatiSingVersIbanAccredito" +
          ", :d.deRptDatiVersDatiSingVersBicAccredito" +
          ", :d.deRptDatiVersDatiSingVersIbanAppoggio" +
          ", :d.deRptDatiVersDatiSingVersBicAppoggio" +
          ", :d.codRptDatiVersDatiSingVersCredenzialiPagatore" +
          ", :d.deRptDatiVersDatiSingVersCausaleVersamento" +
          ", :d.deRptDatiVersDatiSingVersDatiSpecificiRiscossione" +
          ", :d.numRtDatiPagDatiSingPagSingoloImportoPagato" +
          ", :d.deRtDatiPagDatiSingPagEsitoSingoloPagamento" +
          ", :d.dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento" +
          ", :d.codRtDatiPagDatiSingPagIdUnivocoRiscossione" +
          ", :d.deRtDatiPagDatiSingPagCausaleVersamento" +
          ", :d.deRtDatiPagDatiSingPagDatiSpecificiRiscossione" +
          ", :d.numRtDatiPagDatiSingPagCommissioniApplicatePsp" +
          ", :d.codRtDatiPagDatiSingPagAllegatoRicevutaTipo" +
          ", :d.blbRtDatiPagDatiSingPagAllegatoRicevutaTest" +
          ", :d.codRptDatiVersDatiSingVersDatiMbdTipoBollo" +
          ", :d.codRptDatiVersDatiSingVersDatiMbdHashDocumento" +
          ", :d.codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza)"
  )
  @GetGeneratedKeys("mygov_rpt_rt_dettaglio_id")
  Long insert(@BindBean("d") RptRtDettaglio d);

  @SqlQuery(
      "select "+RptRtDettaglio.ALIAS+ALL_FIELDS+", "+ RptRt.FIELDS +
          " from mygov_rpt_rt_dettaglio "+RptRtDettaglio.ALIAS +
          " join mygov_rpt_rt "+RptRt.ALIAS+" on "+RptRt.ALIAS+".mygov_rpt_rt_id = "+RptRtDettaglio.ALIAS+".mygov_rpt_rt_id" +
          " where "+RptRtDettaglio.ALIAS+".mygov_rpt_rt_id = :mygovRptRtId" +
          " order by "+RptRtDettaglio.ALIAS+".mygov_rpt_rt_dettaglio_id"
  )
  @RegisterFieldMapper(RptRtDettaglio.class)
  List<RptRtDettaglio> getByRptRtId(Long mygovRptRtId);

  @SqlUpdate("update mygov_rpt_rt_dettaglio " +
      "set version = :d.version" +
      ", dt_creazione = coalesce(:d.dtCreazione, now())" +
      ", dt_ultima_modifica = coalesce(:d.dtUltimaModifica, now())" +
      ", mygov_rpt_rt_id = :d.mygovRptRtId?.mygovRptRtId" +
      ", num_rpt_dati_vers_dati_sing_vers_importo_singolo_versamento = :d.numRptDatiVersDatiSingVersImportoSingoloVersamento" +
      ", num_rpt_dati_vers_dati_sing_vers_commissione_carico_pa = :d.numRptDatiVersDatiSingVersCommissioneCaricoPa" +
      ", de_rpt_dati_vers_dati_sing_vers_iban_accredito = :d.deRptDatiVersDatiSingVersIbanAccredito" +
      ", de_rpt_dati_vers_dati_sing_vers_bic_accredito = :d.deRptDatiVersDatiSingVersBicAccredito" +
      ", de_rpt_dati_vers_dati_sing_vers_iban_appoggio = :d.deRptDatiVersDatiSingVersIbanAppoggio" +
      ", de_rpt_dati_vers_dati_sing_vers_bic_appoggio = :d.deRptDatiVersDatiSingVersBicAppoggio" +
      ", cod_rpt_dati_vers_dati_sing_vers_credenziali_pagatore = :d.codRptDatiVersDatiSingVersCredenzialiPagatore" +
      ", de_rpt_dati_vers_dati_sing_vers_causale_versamento = :d.deRptDatiVersDatiSingVersCausaleVersamento" +
      ", de_rpt_dati_vers_dati_sing_vers_dati_specifici_riscossione = :d.deRptDatiVersDatiSingVersDatiSpecificiRiscossione" +
      ", num_rt_dati_pag_dati_sing_pag_singolo_importo_pagato = :d.numRtDatiPagDatiSingPagSingoloImportoPagato" +
      ", de_rt_dati_pag_dati_sing_pag_esito_singolo_pagamento = :d.deRtDatiPagDatiSingPagEsitoSingoloPagamento" +
      ", dt_rt_dati_pag_dati_sing_pag_data_esito_singolo_pagamento = :d.dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento" +
      ", cod_rt_dati_pag_dati_sing_pag_id_univoco_riscossione = :d.codRtDatiPagDatiSingPagIdUnivocoRiscossione" +
      ", de_rt_dati_pag_dati_sing_pag_causale_versamento = :d.deRtDatiPagDatiSingPagCausaleVersamento" +
      ", de_rt_dati_pag_dati_sing_pag_dati_specifici_riscossione = :d.deRtDatiPagDatiSingPagDatiSpecificiRiscossione" +
      ", num_rt_dati_pag_dati_sing_pag_commissioni_applicate_psp = :d.numRtDatiPagDatiSingPagCommissioniApplicatePsp" +
      ", cod_rt_dati_pag_dati_sing_pag_allegato_ricevuta_tipo = :d.codRtDatiPagDatiSingPagAllegatoRicevutaTipo" +
      ", blb_rt_dati_pag_dati_sing_pag_allegato_ricevuta_test = :d.blbRtDatiPagDatiSingPagAllegatoRicevutaTest" +
      ", cod_rpt_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo = :d.codRptDatiVersDatiSingVersDatiMbdTipoBollo" +
      ", cod_rpt_dati_vers_dati_sing_vers_dati_mbd_hash_documento = :d.codRptDatiVersDatiSingVersDatiMbdHashDocumento" +
      ", cod_rpt_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza = :d.codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza" +
      "  where mygov_rpt_rt_dettaglio_id = :d.mygovRptRtDettaglioId"
  )
  int updateDateRt(@BindBean("d") RptRtDettaglio d);
}
