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
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RpE;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RpEDettaglio;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface RpEDettaglioDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_rp_e_dettaglio (" +
          "  mygov_rp_e_dettaglio_id" +
          ", version" +
          ", dt_creazione" +
          ", dt_ultima_modifica" +
          ", mygov_rp_e_id" +
          ", num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento" +
          ", num_rp_dati_vers_dati_sing_vers_commissione_carico_pa" +
          ", cod_rp_dati_vers_dati_sing_vers_iban_accredito" +
          ", cod_rp_dati_vers_dati_sing_vers_bic_accredito" +
          ", cod_rp_dati_vers_dati_sing_vers_iban_appoggio" +
          ", cod_rp_dati_vers_dati_sing_vers_bic_appoggio" +
          ", cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore" +
          ", de_rp_dati_vers_dati_sing_vers_causale_versamento" +
          ", de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione" +
          ", num_e_dati_pag_dati_sing_pag_singolo_importo_pagato" +
          ", de_e_dati_pag_dati_sing_pag_esito_singolo_pagamento" +
          ", dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento" +
          ", cod_e_dati_pag_dati_sing_pag_id_univoco_riscoss" +
          ", de_e_dati_pag_dati_sing_pag_causale_versamento" +
          ", de_e_dati_pag_dati_sing_pag_dati_specifici_riscossione" +
          ", num_e_dati_pag_dati_sing_pag_commissioni_applicate_psp" +
          ", cod_e_dati_pag_dati_sing_pag_allegato_ricevuta_tipo" +
          ", blb_e_dati_pag_dati_sing_pag_allegato_ricevuta_test" +
          ", cod_rp_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo" +
          ", cod_rp_dati_vers_dati_sing_vers_dati_mbd_hash_documento" +
          ", cod_rp_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza" +
          ") values (" +
          "  nextval('mygov_rp_e_dettaglio_mygov_rp_e_dettaglio_id_seq')" +
          ", :r.version" +
          ", coalesce(:r.dtCreazione, now())" +
          ", coalesce(:r.dtUltimaModifica, now())" +
          ", :r.mygovRpEId.mygovRpEId" +
          ", :r.numRpDatiVersDatiSingVersImportoSingoloVersamento" +
          ", :r.numRpDatiVersDatiSingVersCommissioneCaricoPa" +
          ", :r.codRpDatiVersDatiSingVersIbanAccredito" +
          ", :r.codRpDatiVersDatiSingVersBicAccredito" +
          ", :r.codRpDatiVersDatiSingVersIbanAppoggio" +
          ", :r.codRpDatiVersDatiSingVersBicAppoggio" +
          ", :r.codRpDatiVersDatiSingVersCredenzialiPagatore" +
          ", :r.deRpDatiVersDatiSingVersCausaleVersamento" +
          ", :r.deRpDatiVersDatiSingVersDatiSpecificiRiscossione" +
          ", :r.numEDatiPagDatiSingPagSingoloImportoPagato" +
          ", :r.deEDatiPagDatiSingPagEsitoSingoloPagamento" +
          ", :r.dtEDatiPagDatiSingPagDataEsitoSingoloPagamento" +
          ", :r.codEDatiPagDatiSingPagIdUnivocoRiscoss" +
          ", :r.deEDatiPagDatiSingPagCausaleVersamento" +
          ", :r.deEDatiPagDatiSingPagDatiSpecificiRiscossione" +
          ", :r.numEDatiPagDatiSingPagCommissioniApplicatePsp" +
          ", :r.codEDatiPagDatiSingPagAllegatoRicevutaTipo" +
          ", :r.blbEDatiPagDatiSingPagAllegatoRicevutaTest" +
          ", :r.codRpDatiVersDatiSingVersDatiMbdTipoBollo" +
          ", :r.codRpDatiVersDatiSingVersDatiMbdHashDocumento" +
          ", :r.codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza)"
  )
  @GetGeneratedKeys("mygov_rp_e_dettaglio_id")
  long insert(@BindBean("r") RpEDettaglio r);

  @SqlQuery("select "+ RpEDettaglio.ALIAS+ALL_FIELDS+", "+ RpE.FIELDS +
          " from mygov_rp_e_dettaglio "+RpEDettaglio.ALIAS +
          " join mygov_rp_e "+ RpE.ALIAS+" on "+RpE.ALIAS+".mygov_rp_e_id = "+RpEDettaglio.ALIAS+".mygov_rp_e_id" +
          " where "+RpEDettaglio.ALIAS+".mygov_rp_e_dettaglio_id = :mygovRpEId" +
          " order by "+RpEDettaglio.ALIAS+".mygov_rp_e_dettaglio_id"
  )
  @RegisterFieldMapper(RpEDettaglio.class)
  List<RpEDettaglio> getByRpE(Long mygovRpEId);

  @SqlUpdate("update mygov_rp_e_dettaglio " +
      "  set version = :r.version " +
      ", dt_ultima_modifica = coalesce(:r.dtUltimaModifica, now()) " +
      ", mygov_rp_e_id = :r.mygovRpEId.mygovRpEId " +
      ", num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento = :r.numRpDatiVersDatiSingVersImportoSingoloVersamento " +
      ", num_rp_dati_vers_dati_sing_vers_commissione_carico_pa = :r.numRpDatiVersDatiSingVersCommissioneCaricoPa " +
      ", cod_rp_dati_vers_dati_sing_vers_iban_accredito = :r.codRpDatiVersDatiSingVersIbanAccredito " +
      ", cod_rp_dati_vers_dati_sing_vers_bic_accredito = :r.codRpDatiVersDatiSingVersBicAccredito " +
      ", cod_rp_dati_vers_dati_sing_vers_iban_appoggio = :r.codRpDatiVersDatiSingVersIbanAppoggio " +
      ", cod_rp_dati_vers_dati_sing_vers_bic_appoggio = :r.codRpDatiVersDatiSingVersBicAppoggio " +
      ", cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore = :r.codRpDatiVersDatiSingVersCredenzialiPagatore " +
      ", de_rp_dati_vers_dati_sing_vers_causale_versamento = :r.deRpDatiVersDatiSingVersCausaleVersamento " +
      ", de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione = :r.deRpDatiVersDatiSingVersDatiSpecificiRiscossione " +
      ", num_e_dati_pag_dati_sing_pag_singolo_importo_pagato = :r.numEDatiPagDatiSingPagSingoloImportoPagato " +
      ", de_e_dati_pag_dati_sing_pag_esito_singolo_pagamento = :r.deEDatiPagDatiSingPagEsitoSingoloPagamento " +
      ", dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento = :r.dtEDatiPagDatiSingPagDataEsitoSingoloPagamento " +
      ", cod_e_dati_pag_dati_sing_pag_id_univoco_riscoss = :r.codEDatiPagDatiSingPagIdUnivocoRiscoss " +
      ", de_e_dati_pag_dati_sing_pag_causale_versamento = :r.deEDatiPagDatiSingPagCausaleVersamento " +
      ", de_e_dati_pag_dati_sing_pag_dati_specifici_riscossione = :r.deEDatiPagDatiSingPagDatiSpecificiRiscossione " +
      ", num_e_dati_pag_dati_sing_pag_commissioni_applicate_psp = :r.numEDatiPagDatiSingPagCommissioniApplicatePsp " +
      ", cod_e_dati_pag_dati_sing_pag_allegato_ricevuta_tipo = :r.codEDatiPagDatiSingPagAllegatoRicevutaTipo " +
      ", blb_e_dati_pag_dati_sing_pag_allegato_ricevuta_test = :r.blbEDatiPagDatiSingPagAllegatoRicevutaTest " +
      ", cod_rp_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo = :r.codRpDatiVersDatiSingVersDatiMbdTipoBollo " +
      ", cod_rp_dati_vers_dati_sing_vers_dati_mbd_hash_documento = :r.codRpDatiVersDatiSingVersDatiMbdHashDocumento " +
      ", cod_rp_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza = :r.codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza " +
      "  where mygov_rp_e_dettaglio_id = :r.mygovRpEDettaglioId"
  )
  int updateDateE(@BindBean("r") RpEDettaglio r);
}
