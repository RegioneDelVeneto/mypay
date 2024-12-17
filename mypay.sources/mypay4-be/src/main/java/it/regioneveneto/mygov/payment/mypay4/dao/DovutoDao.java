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
package it.regioneveneto.mygov.payment.mypay4.dao;

import it.regioneveneto.mygov.payment.mypay4.dto.DovutoOperatoreTo;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.DovutoEntePrimarioTo;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.jdbi.v3.core.statement.OutParameters;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.*;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlCall;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.Types;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DovutoDao extends BaseDao {


  @SqlUpdate(" update mygov_dovuto " +
    " set version = :d.version" +
    " , flg_dovuto_attuale = :d.flgDovutoAttuale" +
    " , mygov_flusso_id = :d.mygovFlussoId.mygovFlussoId" +
    " , num_riga_flusso = :d.numRigaFlusso" +
    " , mygov_anagrafica_stato_id = :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
    " , mygov_carrello_id = :d.mygovCarrelloId?.mygovCarrelloId" +
    " , cod_iud = :d.codIud" +
    " , cod_iuv = :d.codIuv" +
    " , dt_creazione = :d.dtCreazione" +
    " , dt_ultima_modifica = :d.dtUltimaModifica" +
    " , cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :d.codRpSoggPagIdUnivPagTipoIdUnivoco" +
    " , cod_rp_sogg_pag_id_univ_pag_codice_id_univoco = :d.codRpSoggPagIdUnivPagCodiceIdUnivoco" +
    " , de_rp_sogg_pag_anagrafica_pagatore = :d.deRpSoggPagAnagraficaPagatore" +
    " , de_rp_sogg_pag_indirizzo_pagatore = :d.deRpSoggPagIndirizzoPagatore" +
    " , de_rp_sogg_pag_civico_pagatore = :d.deRpSoggPagCivicoPagatore" +
    " , cod_rp_sogg_pag_cap_pagatore = :d.codRpSoggPagCapPagatore" +
    " , de_rp_sogg_pag_localita_pagatore = left(:d.deRpSoggPagLocalitaPagatore," + Dovuto.MAX_LENGTH_LOCALITA + ")" +
    " , de_rp_sogg_pag_provincia_pagatore = :d.deRpSoggPagProvinciaPagatore" +
    " , cod_rp_sogg_pag_nazione_pagatore = :d.codRpSoggPagNazionePagatore" +
    " , de_rp_sogg_pag_email_pagatore = :d.deRpSoggPagEmailPagatore" +
    " , dt_rp_dati_vers_data_esecuzione_pagamento = :d.dtRpDatiVersDataEsecuzionePagamento" +
    " , cod_rp_dati_vers_tipo_versamento = :d.codRpDatiVersTipoVersamento" +
    " , num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento = :d.numRpDatiVersDatiSingVersImportoSingoloVersamento" +
    " , num_rp_dati_vers_dati_sing_vers_commissione_carico_pa = :d.numRpDatiVersDatiSingVersCommissioneCaricoPa" +
    " , de_rp_dati_vers_dati_sing_vers_causale_versamento = :d.deRpDatiVersDatiSingVersCausaleVersamento" +
    " , de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione = :d.deRpDatiVersDatiSingVersDatiSpecificiRiscossione" +
    " , cod_tipo_dovuto = :d.codTipoDovuto" +
    " , mygov_avviso_id = :d.mygovAvvisoId?.mygovAvvisoId" +
    " , dt_creazione_cod_iuv = :d.dtCreazioneCodIuv" +
    " , mygov_dati_marca_bollo_digitale_id = :d.mygovDatiMarcaBolloDigitaleId" +
    " , de_causale_visualizzata = :d.deCausaleVisualizzata" +
    " , bilancio = :d.bilancio" +
    " , flg_genera_iuv = :d.flgGeneraIuv" +
    " , id_session = :d.idSession" +
    " , flg_iuv_volatile = :d.flgIuvVolatile" +
    " , gpd_iupd = :d.gpdIupd" +
    " , gpd_status = :d.gpdStatus" +
    " where mygov_dovuto_id = :d.mygovDovutoId")
  int update(@BindBean("d") Dovuto d);

  @SqlUpdate(" insert into mygov_dovuto (" +
    "   mygov_dovuto_id" +
    " , version" +
    " , flg_dovuto_attuale" +
    " , mygov_flusso_id" +
    " , num_riga_flusso" +
    " , mygov_anagrafica_stato_id" +
    " , mygov_carrello_id" +
    " , cod_iud" +
    " , cod_iuv" +
    " , dt_creazione" +
    " , dt_ultima_modifica" +
    " , cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco" +
    " , cod_rp_sogg_pag_id_univ_pag_codice_id_univoco" +
    " , de_rp_sogg_pag_anagrafica_pagatore" +
    " , de_rp_sogg_pag_indirizzo_pagatore" +
    " , de_rp_sogg_pag_civico_pagatore" +
    " , cod_rp_sogg_pag_cap_pagatore" +
    " , de_rp_sogg_pag_localita_pagatore" +
    " , de_rp_sogg_pag_provincia_pagatore" +
    " , cod_rp_sogg_pag_nazione_pagatore" +
    " , de_rp_sogg_pag_email_pagatore" +
    " , dt_rp_dati_vers_data_esecuzione_pagamento" +
    " , cod_rp_dati_vers_tipo_versamento" +
    " , num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento" +
    " , num_rp_dati_vers_dati_sing_vers_commissione_carico_pa" +
    " , de_rp_dati_vers_dati_sing_vers_causale_versamento" +
    " , de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione" +
    " , cod_tipo_dovuto" +
    " , mygov_avviso_id" +
    " , dt_creazione_cod_iuv" +
    " , mygov_dati_marca_bollo_digitale_id" +
    " , de_causale_visualizzata" +
    " , bilancio" +
    " , flg_genera_iuv" +
    " , id_session " +
    " , flg_iuv_volatile " +
    " , gpd_iupd " +
    " , gpd_status " +
    ") values (" +
    "   nextval('mygov_dovuto_mygov_dovuto_id_seq')" +
    " , :d.version" +
    " , :d.flgDovutoAttuale" +
    " , :d.mygovFlussoId.mygovFlussoId" +
    " , :d.numRigaFlusso" +
    " , :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
    " , :d.mygovCarrelloId?.mygovCarrelloId" +
    " , :d.codIud" +
    " , :d.codIuv" +
    " , :d.dtCreazione" +
    " , :d.dtUltimaModifica" +
    " , :d.codRpSoggPagIdUnivPagTipoIdUnivoco" +
    " , :d.codRpSoggPagIdUnivPagCodiceIdUnivoco" +
    " , :d.deRpSoggPagAnagraficaPagatore" +
    " , :d.deRpSoggPagIndirizzoPagatore" +
    " , :d.deRpSoggPagCivicoPagatore" +
    " , :d.codRpSoggPagCapPagatore" +
    " , left(:d.deRpSoggPagLocalitaPagatore," + Dovuto.MAX_LENGTH_LOCALITA + ")" +
    " , :d.deRpSoggPagProvinciaPagatore" +
    " , :d.codRpSoggPagNazionePagatore" +
    " , :d.deRpSoggPagEmailPagatore" +
    " , :d.dtRpDatiVersDataEsecuzionePagamento" +
    " , :d.codRpDatiVersTipoVersamento" +
    " , :d.numRpDatiVersDatiSingVersImportoSingoloVersamento" +
    " , :d.numRpDatiVersDatiSingVersCommissioneCaricoPa" +
    " , :d.deRpDatiVersDatiSingVersCausaleVersamento" +
    " , :d.deRpDatiVersDatiSingVersDatiSpecificiRiscossione" +
    " , :d.codTipoDovuto" +
    " , :d.mygovAvvisoId?.mygovAvvisoId" +
    " , :d.dtCreazioneCodIuv" +
    " , :d.mygovDatiMarcaBolloDigitaleId" +
    " , :d.deCausaleVisualizzata" +
    " , :d.bilancio" +
    " , :d.flgGeneraIuv" +
    " , :d.idSession" +
    " , :d.flgIuvVolatile" +
    " , :d.gpdIupd" +
    " , :d.gpdStatus)")
  @GetGeneratedKeys("mygov_dovuto_id")
  long insert(@BindBean("d") Dovuto d);

  @SqlUpdate(
    " delete from mygov_dovuto " +
      " where mygov_dovuto_id = :d.mygovDovutoId " +
      " and version = :d.version " +
      " and mygov_flusso_id = :d.mygovFlussoId.mygovFlussoId" +
      " and mygov_anagrafica_stato_id = :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId")
  int delete(@BindBean("d") Dovuto d);

  @SqlUpdate(
    " delete from mygov_dovuto " +
      " where mygov_dovuto_id in (<ids>)")
  int delete(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> ids);

  @SqlUpdate(
    " delete from mygov_dovuto " +
      " where mygov_dovuto_id = :id ")
  int deleteById(Long id);

  String GET_BY_ID_SQL =
    " select " + Dovuto.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + "," + Flusso.FIELDS + "," + Ente.FIELDS + "," + Avviso.FIELDS + "," + Carrello.FIELDS +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = " + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " left join mygov_carrello " + Carrello.ALIAS + " on " + Carrello.ALIAS + ".mygov_carrello_id = " + Dovuto.ALIAS + ".mygov_carrello_id " +
      " left join mygov_avviso " + Avviso.ALIAS + " on " + Avviso.ALIAS + ".cod_iuv = " + Dovuto.ALIAS + ".cod_iuv and " + Avviso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " where " + Dovuto.ALIAS + ".mygov_dovuto_id = :id";

  @SqlQuery(GET_BY_ID_SQL)
  @RegisterFieldMapper(Dovuto.class)
  Dovuto getById(Long id);

  @SqlQuery(GET_BY_ID_SQL + " for update of Dovuto")
  @RegisterFieldMapper(Dovuto.class)
  @QueryTimeOut(20)
  Dovuto getByIdForUpdate(Long id);

  @SqlQuery(GET_BY_ID_SQL + " for update of Dovuto skip locked")
  @RegisterFieldMapper(Dovuto.class)
  Optional<Dovuto> getByIdLockOrSkip(Long id);

  @SqlQuery(
    " select count(*)" +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " where (" + Dovuto.ALIAS + ".cod_tipo_dovuto = :codTipoDovuto or :codTipoDovuto is null)" +
      "   and (" + Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte or :codIpaEnte is null)")
  long count(String codIpaEnte, String codTipoDovuto);

  @SqlQuery(
    " select " +
      Dovuto.ALIAS + ".mygov_dovuto_id " +
      ",coalesce(" + Avviso.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, " + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) " +
      " as cod_rp_sogg_pag_id_univ_pag_codice_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_iud " +
      "," + Dovuto.ALIAS + ".cod_iuv " +
      "," + Dovuto.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento " +
      "," + Dovuto.ALIAS + ".de_causale_visualizzata " +
      "," + Dovuto.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
      "," + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento " +
      "," + AnagraficaStato.ALIAS + ".cod_stato " +
      "," + AnagraficaStato.ALIAS + ".de_stato " +
      "," + Dovuto.ALIAS + ".cod_tipo_dovuto " +
      "," + Ente.ALIAS + ".cod_ipa_ente " +
      "," + Ente.ALIAS + ".de_nome_ente " +
      "," + Ente.ALIAS + ".codice_fiscale_ente " +
      "," + Ente.ALIAS + ".application_code " +
      "," + Dovuto.ALIAS + ".cod_rp_dati_vers_tipo_versamento " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_cap_pagatore " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_nazione_pagatore " +
      ",coalesce(" + Avviso.ALIAS + ".de_rp_sogg_pag_anagrafica_pagatore, " + Dovuto.ALIAS + ".de_rp_sogg_pag_anagrafica_pagatore) " +
      " as de_rp_sogg_pag_anagrafica_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_civico_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_email_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_indirizzo_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_localita_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_provincia_pagatore " +
      "," + Flusso.ALIAS + ".flg_spontaneo " +
      "," + Dovuto.ALIAS + ".flg_iuv_volatile " +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = " + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " left join mygov_carrello carrello on carrello.mygov_carrello_id = " + Dovuto.ALIAS + ".mygov_carrello_id " +
      " left join mygov_avviso " + Avviso.ALIAS + " on " + Avviso.ALIAS + ".cod_iuv = " + Dovuto.ALIAS + ".cod_iuv and " + Avviso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " where " + Dovuto.ALIAS + ".mygov_dovuto_id = :id")
  DovutoTo getToById(Long id);

  @SqlQuery(
    " select " + Dovuto.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + "," + Flusso.FIELDS + "," + Ente.FIELDS + "," + Avviso.FIELDS + "," + Carrello.FIELDS +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = " + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " left join mygov_carrello " + Carrello.ALIAS + " on " + Carrello.ALIAS + ".mygov_carrello_id = " + Dovuto.ALIAS + ".mygov_carrello_id " +
      " left join mygov_avviso " + Avviso.ALIAS + " on " + Avviso.ALIAS + ".cod_iuv = " + Dovuto.ALIAS + ".cod_iuv and " + Avviso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " where " + Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte" +
      "   and " + Dovuto.ALIAS + ".cod_iuv = :codIuv")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByIuvEnte(String codIuv, String codIpaEnte);

  @SqlQuery(
    " select " + Dovuto.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + "," + Flusso.FIELDS + "," + Ente.FIELDS + "," + Avviso.FIELDS + "," + Carrello.FIELDS +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = " + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " left join mygov_carrello " + Carrello.ALIAS + " on " + Carrello.ALIAS + ".mygov_carrello_id = " + Dovuto.ALIAS + ".mygov_carrello_id " +
      " left join mygov_avviso " + Avviso.ALIAS + " on " + Avviso.ALIAS + ".cod_iuv = " + Dovuto.ALIAS + ".cod_iuv and " + Avviso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " where " + Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte" +
      "   and " + Dovuto.ALIAS + ".cod_iud = :codIud")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByIudEnte(String codIud, String codIpaEnte);

  @SqlQuery(
          " select " + Dovuto.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + "," + Flusso.FIELDS + "," + Ente.FIELDS + "," + Avviso.FIELDS + "," + Carrello.FIELDS +
                  " from mygov_dovuto " + Dovuto.ALIAS +
                  " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
                  " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
                  " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = " + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
                  " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
                  " left join mygov_carrello " + Carrello.ALIAS + " on " + Carrello.ALIAS + ".mygov_carrello_id = " + Dovuto.ALIAS + ".mygov_carrello_id " +
                  " left join mygov_avviso " + Avviso.ALIAS + " on " + Avviso.ALIAS + ".cod_iuv = " + Dovuto.ALIAS + ".cod_iuv and " + Avviso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
                  " where " + Dovuto.ALIAS + ".mygov_flusso_id = :flussoId")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByFlussoId(Long flussoId);

    // GPD MASSIVA -- INIZIO

    String GET_BY_BASE_GPD_MASSIVA_SQL= " select " + Dovuto.ALIAS + ALL_FIELDS + "," + Ente.FIELDS +
            " from mygov_dovuto " + Dovuto.ALIAS +
            " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
            " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id ";

    String GET_BY_FLUSSO_ID_GPD_MASSIVA_SQL =
            GET_BY_BASE_GPD_MASSIVA_SQL +
                    " where " + Dovuto.ALIAS + ".mygov_flusso_id = :flussoId";

    String GET_BY_ENTE_ID_SQL =
            GET_BY_BASE_GPD_MASSIVA_SQL +
                    " inner join mygov_dovuto_preload  " + DovutoPreload.ALIAS + " on " + DovutoPreload.ALIAS + ".mygov_dovuto_id = " + Dovuto.ALIAS + ".mygov_dovuto_id " +
                    " where " + Ente.ALIAS + ".mygov_ente_id = :enteId";

    // per flussoId

  @SqlQuery(
          GET_BY_FLUSSO_ID_GPD_MASSIVA_SQL
                  + " and " + Dovuto.ALIAS + ".gpd_status = :gpdStatus"
      + " and " + Dovuto.ALIAS + ".cod_iuv is not null"
      + " order by " + Dovuto.ALIAS + ".mygov_dovuto_id asc "
      + " limit :maxResults ")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByFlussoIdAndGpdStatusWithIuvNotNull(Long flussoId, char gpdStatus, int maxResults);

  @SqlQuery(
          GET_BY_FLUSSO_ID_GPD_MASSIVA_SQL
                  + " and "
                  + Dovuto.ALIAS + ".gpd_status = :gpdStatus "
                  + " and " + Dovuto.ALIAS + ".gpd_iupd is null"
                  + " and " + Dovuto.ALIAS + ".cod_iuv is not null"
                  + " order by " + Dovuto.ALIAS + ".mygov_dovuto_id asc "
                  + " limit :maxResults "
  )
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByFlussoIdAndGpdStatusWithIuvNotNullAndIupdNull(Long flussoId, char gpdStatus, int maxResults);

  // per enteId
  @SqlQuery(
          GET_BY_ENTE_ID_SQL
                  + " and " + DovutoPreload.ALIAS + ".nuovo_gpd_status = :gpdStatus"
                  + " and " + Dovuto.ALIAS + ".cod_iuv is not null"
                  + " order by " + Dovuto.ALIAS + ".mygov_dovuto_id asc "
                  + " limit :maxResults ")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByEnteIdAndGpdStatusWithIuvNotNull(Long enteId, char gpdStatus, int maxResults);

  @SqlQuery(
          GET_BY_ENTE_ID_SQL
                  + " and " + DovutoPreload.ALIAS + ".nuovo_gpd_status = :gpdStatus"
                  + " and " + Dovuto.ALIAS + ".gpd_iupd is null"
                  + " and " + Dovuto.ALIAS + ".cod_iuv is not null"
                  + " order by " + Dovuto.ALIAS + ".mygov_dovuto_id asc "
                  + " limit :maxResults "
  )
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByEnteIdAndGpdStatusWithIuvNotNullAndIupdNull(Long enteId, char gpdStatus, int maxResults);

  @SqlQuery(
          GET_BY_BASE_GPD_MASSIVA_SQL +
                  " inner join mygov_gpd_err_dovuto  " + GpdErrDovuto.ALIAS + " on " + GpdErrDovuto.ALIAS + ".gpd_iupd = " + Dovuto.ALIAS + ".gpd_iupd " +
                  " where " + Dovuto.ALIAS + ".mygov_flusso_id = :flussoId"
  )
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getErrDovutiByIdFlusso(Long flussoId);

  @SqlQuery(
          "select count(1) from mygov_dovuto " + Dovuto.ALIAS +
                  " where " + Dovuto.ALIAS + ".mygov_flusso_id = :flussoId " +
                  " and " + Dovuto.ALIAS + ".cod_iuv is null "
  )
  Integer getCountDovutiWithIuvNullByFlusso(Long flussoId);

  @SqlQuery(
          "select count(1) from mygov_dovuto " + Dovuto.ALIAS +
                  " where " + Dovuto.ALIAS + ".mygov_flusso_id = :flussoId " +
                  " and " + Dovuto.ALIAS + ".cod_iuv is not null" +
                  " and " + Dovuto.ALIAS + ".gpd_status IN ('" +
                  Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA + "','" +
                  Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA + "','" +
                  Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA + "')"
  )
  Integer getCountDovutiToSyncByFlusso(Long flussoId);

  @SqlQuery(" select " + Dovuto.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + "," + Flusso.FIELDS + "," + Ente.FIELDS + "," + Avviso.FIELDS +
    " from mygov_flusso " + Flusso.ALIAS +
    "  	   inner join mygov_dovuto " + Dovuto.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
    "      inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
    "      inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id =" + Flusso.ALIAS + ".mygov_ente_id " +
    "      inner join mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS + " on " + EnteTipoDovuto.ALIAS + ".mygov_ente_id =" + Flusso.ALIAS + ".mygov_ente_id and " + EnteTipoDovuto.ALIAS + ".cod_tipo = " + Dovuto.ALIAS + ".cod_tipo_dovuto " +
    "      inner join mygov_anagrafica_stato anagFlusso on anagFlusso.mygov_anagrafica_stato_id =" + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
    "      left join mygov_avviso " + Avviso.ALIAS + " on " + Avviso.ALIAS + ".cod_iuv = " + Dovuto.ALIAS + ".cod_iuv and " + Avviso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
    " where " + Flusso.ALIAS + ".mygov_anagrafica_stato_id = :idStatoFlussoCaricato " +
    "   and " + Dovuto.ALIAS + ".flg_dovuto_attuale = true " +
    "   and " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id = :idStatoDovutoDaPagare " +
    " and (" + Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte or :codIpaEnte is null) " +
    "	and " + EnteTipoDovuto.ALIAS + ".flg_attivo = true " +
    "	and (" + EnteTipoDovuto.ALIAS + ".flg_scadenza_obbligatoria = true and " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento >= now() or " + EnteTipoDovuto.ALIAS + ".flg_scadenza_obbligatoria = false) " +
    "	and " + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :codRpSoggPagIdUnivPagTipoIdUnivoco " +
    "	and lower(" + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:codRpSoggPagIdUnivPagCodiceIdUnivoco) " +
    " and coalesce(" + Dovuto.ALIAS + ".flg_iuv_volatile,false) = false " +
    "	order by " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento asc NULLS LAST , " + Dovuto.ALIAS + ".dt_creazione asc NULLS last "
  )
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getUnpaidByEnteIdUnivocoPersona(String codRpSoggPagIdUnivPagTipoIdUnivoco, String codRpSoggPagIdUnivPagCodiceIdUnivoco, String codIpaEnte, Long idStatoDovutoDaPagare, Long idStatoFlussoCaricato);

  String SQL_SEARCH_DOVUTO =
    " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " inner join mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS + " on " + EnteTipoDovuto.ALIAS + ".cod_tipo = " + Dovuto.ALIAS + ".cod_tipo_dovuto " +
      "            and " + EnteTipoDovuto.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " left join mygov_carrello " + Carrello.ALIAS + " on " + Carrello.ALIAS + ".mygov_carrello_id = " + Dovuto.ALIAS + ".mygov_carrello_id " +
      " left join mygov_avviso " + Avviso.ALIAS + " on " + Avviso.ALIAS + ".cod_iuv = " + Dovuto.ALIAS + ".cod_iuv and " + Avviso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " where " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id NOT IN  ( <listStatiDovuto> )" +
      " and " + Flusso.ALIAS + ".mygov_anagrafica_stato_id = :idStatoFlussoCaricato " +
      " and " + Ente.ALIAS + ".cd_stato_ente = :idStatoEnteEsercizio " +
      " and ( :noticeOnly = false or length(" + Dovuto.ALIAS + ".cod_iuv) in (15, 17) )" +
      " and ( :payableOnly = false or " + AnagraficaStato.ALIAS + ".cod_stato='" + AnagraficaStato.STATO_DOVUTO_DA_PAGARE + "' " +
      "   and ("+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria is not true or "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento >= CURRENT_DATE) )" +
      " and (" + Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte or :codIpaEnte is null)" +
      " and (" + Dovuto.ALIAS + ".cod_tipo_dovuto = :codTipoDovuto or :codTipoDovuto is null)" +
      " and (" + Dovuto.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento ilike '%' || :causale || '%' or :causale is null)" +
      " and  " + EnteTipoDovuto.ALIAS + ".flg_attivo = true " +
      " and ( " +
      "          " + Carrello.ALIAS + ".mygov_carrello_id is null " +
      "      and lower(" + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
      "   or " +
      "          " + Carrello.ALIAS + ".mygov_carrello_id is not null and ( " +
      "             lower(" + Carrello.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) or " +
      "             lower(" + Carrello.ALIAS + ".cod_rp_sogg_vers_id_univ_vers_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
      "           )   ) " +
      " and ( " +
      "         " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento is not null " +
      "     and :dataFrom::DATE <= " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento " +
      "     and " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento < :dataTo::DATE " +
      "	    and " + EnteTipoDovuto.ALIAS + ".flg_scadenza_obbligatoria = true " +
      " or " + EnteTipoDovuto.ALIAS + ".flg_scadenza_obbligatoria = false) ";

  @SqlQuery(
    " select " +
      Dovuto.ALIAS + ".mygov_dovuto_id " +
      ",coalesce(" + Avviso.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, " + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) " +
      " as cod_rp_sogg_pag_id_univ_pag_codice_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_iud " +
      "," + Dovuto.ALIAS + ".cod_iuv " +
      "," + Dovuto.ALIAS + ".flg_iuv_volatile " +
      "," + Dovuto.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento " +
      "," + Dovuto.ALIAS + ".de_causale_visualizzata " +
      "," + Dovuto.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
      "," + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento " +
      "," + AnagraficaStato.ALIAS + ".cod_stato " +
      "," + AnagraficaStato.ALIAS + ".de_stato " +
      "," + Dovuto.ALIAS + ".cod_tipo_dovuto " +
      "," + Ente.ALIAS + ".cod_ipa_ente " +
      "," + Ente.ALIAS + ".codice_fiscale_ente " +
      "," + Ente.ALIAS + ".de_nome_ente " +
      "," + Ente.ALIAS + ".application_code " +
      "," + Dovuto.ALIAS + ".cod_rp_dati_vers_tipo_versamento " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_cap_pagatore " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_nazione_pagatore " +
      ",coalesce(" + Avviso.ALIAS + ".de_rp_sogg_pag_anagrafica_pagatore, " + Dovuto.ALIAS + ".de_rp_sogg_pag_anagrafica_pagatore) " +
      " as de_rp_sogg_pag_anagrafica_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_civico_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_email_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_indirizzo_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_localita_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_provincia_pagatore " +
      "," + Flusso.ALIAS + ".flg_spontaneo " +
      ", case when "+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria is not true then null else "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento end as dt_scadenza_order" +
      SQL_SEARCH_DOVUTO +
      " order by dt_scadenza_order ASC NULLS LAST, " + Dovuto.ALIAS + ".dt_creazione " +
      " limit <queryLimit>"
  )
  List<DovutoTo> searchDovuto(String codIpaEnte, Long idStatoEnteEsercizio, String idUnivocoPagatoreVersante,
                              String codTipoDovuto, String causale, LocalDate dataFrom, LocalDate dataTo,
                              @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> listStatiDovuto,
                              Long idStatoFlussoCaricato, boolean noticeOnly, boolean payableOnly, @Define int queryLimit);

  @SqlQuery(
    " select count(1) " +
      SQL_SEARCH_DOVUTO
  )
  int searchDovutoCount(String codIpaEnte, Long idStatoEnteEsercizio, String idUnivocoPagatoreVersante,
                        String codTipoDovuto, String causale, LocalDate dataFrom, LocalDate dataTo,
                        @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> listStatiDovuto,
                        Long idStatoFlussoCaricato, boolean noticeOnly, boolean payableOnly);

  @SqlQuery(
    " select " +
      Dovuto.ALIAS + ".mygov_dovuto_id " +
      ",coalesce(" + Avviso.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, " + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) " +
      " as cod_rp_sogg_pag_id_univ_pag_codice_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_iud " +
      "," + Dovuto.ALIAS + ".cod_iuv " +
      "," + Dovuto.ALIAS + ".flg_iuv_volatile " +
      "," + Dovuto.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento " +
      "," + Dovuto.ALIAS + ".de_causale_visualizzata " +
      "," + Dovuto.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
      "," + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento " +
      "," + AnagraficaStato.ALIAS + ".cod_stato " +
      "," + AnagraficaStato.ALIAS + ".de_stato " +
      "," + Dovuto.ALIAS + ".cod_tipo_dovuto " +
      "," + Ente.ALIAS + ".cod_ipa_ente " +
      "," + Ente.ALIAS + ".codice_fiscale_ente " +
      "," + Ente.ALIAS + ".de_nome_ente " +
      "," + Ente.ALIAS + ".application_code " +
      "," + Dovuto.ALIAS + ".cod_rp_dati_vers_tipo_versamento " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_cap_pagatore " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_nazione_pagatore " +
      ",coalesce(" + Avviso.ALIAS + ".de_rp_sogg_pag_anagrafica_pagatore, " + Dovuto.ALIAS + ".de_rp_sogg_pag_anagrafica_pagatore) " +
      " as de_rp_sogg_pag_anagrafica_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_civico_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_email_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_indirizzo_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_localita_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_provincia_pagatore " +
      "," + Flusso.ALIAS + ".flg_spontaneo " +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " inner join mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS + " on " + EnteTipoDovuto.ALIAS + ".cod_tipo = " + Dovuto.ALIAS + ".cod_tipo_dovuto " +
      "            and " + EnteTipoDovuto.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " left join mygov_avviso " + Avviso.ALIAS + " on " + Avviso.ALIAS + ".cod_iuv = " + Dovuto.ALIAS + ".cod_iuv and " + Avviso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " where " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id NOT IN ( <listStatiDovuto> ) " +
      " and " + Flusso.ALIAS + ".mygov_anagrafica_stato_id = :idStatoFlussoCaricato " +
      " and " + Ente.ALIAS + ".cd_stato_ente = :idStatoEnteEsercizio " +
      " and  " + EnteTipoDovuto.ALIAS + ".flg_attivo = true " +
      " and lower(" + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
      " and " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento is not null " +
      "	and (" + EnteTipoDovuto.ALIAS + ".flg_scadenza_obbligatoria = true and " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento >= now() " +
      "           or " + EnteTipoDovuto.ALIAS + ".flg_scadenza_obbligatoria = false ) " +
      " order by " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento ASC, " + Dovuto.ALIAS + ".dt_creazione " +
      " limit :num "
  )
  List<DovutoTo> searchLastDovuto(String idUnivocoPagatoreVersante, Long idStatoEnteEsercizio,
                                  @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> listStatiDovuto,
                                  Long idStatoFlussoCaricato, Integer num);

  @SqlQuery(
    " select 1 " +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " where " + AnagraficaStato.ALIAS + ".cod_stato='PAGAMENTO_INIZIATO' " +
      "   and " + Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte " +
      "   and " + Dovuto.ALIAS + ".cod_tipo_dovuto = :codTipoDovuto " +
      "   and " + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :tipoIdUnivocoPagatoreVersante " +
      "   and lower(" + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
      "   and coalesce(" + Dovuto.ALIAS + ".dt_ultima_modifica, " + Dovuto.ALIAS + ".dt_creazione) between now() - interval '1 day' and now() " + //only last 24 hours
      "   and " + Dovuto.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento = :causale " +
      " limit 1 "
  )
  Optional<Boolean> hasReplicaDovuto(String codIpaEnte, char tipoIdUnivocoPagatoreVersante, String idUnivocoPagatoreVersante,
                                     String causale, String codTipoDovuto);

  @SqlQuery(
    " select " +
      " " + Dovuto.ALIAS + ".mygov_dovuto_id " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_iud " +
      "," + Dovuto.ALIAS + ".cod_iuv " +
      "," + Dovuto.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento " +
      "," + Dovuto.ALIAS + ".de_causale_visualizzata " +
      "," + Dovuto.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
      "," + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento " +
      "," + AnagraficaStato.ALIAS + ".cod_stato " +
      "," + AnagraficaStato.ALIAS + ".de_stato " +
      "," + Dovuto.ALIAS + ".cod_tipo_dovuto " +
      "," + Ente.ALIAS + ".cod_ipa_ente " +
      "," + Ente.ALIAS + ".codice_fiscale_ente " +
      "," + Ente.ALIAS + ".de_nome_ente " +
      "," + Ente.ALIAS + ".application_code " +
      "," + Dovuto.ALIAS + ".cod_rp_dati_vers_tipo_versamento " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_cap_pagatore " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_nazione_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_anagrafica_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_civico_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_email_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_indirizzo_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_localita_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_provincia_pagatore " +
      "," + Flusso.ALIAS + ".flg_spontaneo " +
      "," + Dovuto.ALIAS + ".flg_iuv_volatile " +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = " + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " inner join mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS + " on " + EnteTipoDovuto.ALIAS + ".cod_tipo = " + Dovuto.ALIAS + ".cod_tipo_dovuto " +
      "            and " + EnteTipoDovuto.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " left join mygov_carrello carrello on carrello.mygov_carrello_id = " + Dovuto.ALIAS + ".mygov_carrello_id " +
      " where (" + Ente.ALIAS + ".mygov_ente_id = :mygovEnteId or :mygovEnteId is null) " +
      " and (" + Dovuto.ALIAS + ".cod_tipo_dovuto = :codTipoDovuto or :codTipoDovuto is null) " +
      " and " + Dovuto.ALIAS + ".cod_iuv = :codIuv " +
      " and lower(" + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:codRpSoggPagIdUnivPagCodiceIdUnivoco) " +
      " and (:deRpSoggPagAnagraficaPagatore is null or lower(" + Dovuto.ALIAS + ".de_rp_sogg_pag_anagrafica_pagatore) = lower(:deRpSoggPagAnagraficaPagatore) ) " +
      " and ( " + EnteTipoDovuto.ALIAS + ".flg_attivo = true or :searchOnlyOnTipoAttivo is not true ) ")
  List<DovutoTo> searchDovutoOnTipoAttivo(Long mygovEnteId, String codTipoDovuto, String codIuv,
                                          String codRpSoggPagIdUnivPagCodiceIdUnivoco, String deRpSoggPagAnagraficaPagatore,
                                          Boolean searchOnlyOnTipoAttivo);

  @SqlQuery(
    " select " + Dovuto.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + "," + Flusso.FIELDS + "," + Ente.FIELDS +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = " + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " where " + Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte " +
      " and " + Dovuto.ALIAS + ".cod_iuv = :codIuv " +
      " order by 1")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> searchDovutoByIuvEnte(String codIuv, String codIpaEnte);


  String SQL_SEARCH_DOVUTO_OPERATORE =
    " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " where (" + Dovuto.ALIAS + ".mygov_anagrafica_stato_id = :idStatoDovuto " +
      "        <flgIuvVolatileOperator> coalesce(" + Dovuto.ALIAS + ".flg_iuv_volatile,false) = :flgIuvVolatile " +
      "        or :idStatoDovuto is null) " +
      " and " + Ente.ALIAS + ".mygov_ente_id = :mygovEnteId " +
      " and " + Dovuto.ALIAS + ".cod_tipo_dovuto in (<listCodTipoDovuto>) " +
      " and (" + Dovuto.ALIAS + ".de_causale_visualizzata ilike '%' || :causale || '%' " +
      "      or " + Dovuto.ALIAS + ".de_causale_visualizzata is null and " + Dovuto.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento ilike '%' || :causale || '%' " +
      "      or :causale is null)" +
      " and (" + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco ilike '%' || :codFiscale || '%' or :codFiscale is null)" +
      " and (" + Dovuto.ALIAS + ".cod_iud ilike '%' || :iud || '%' or :iud is null)" +
      " and (" + Dovuto.ALIAS + ".cod_iuv ilike '%' || :iuv || '%' or :iuv is null)" +
      " and (" + Flusso.ALIAS + ".iuf= :nomeFlusso or :nomeFlusso is null)" +
      " and (" + Dovuto.ALIAS + ".cod_tipo_dovuto in (<listCodTipoDovutoDataNonObbl>) " +
      "      and :notEnforceDataScadenza " +
      "      or  " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento >= :dataFrom::DATE " +
      "      and " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento < :dataTo::DATE ) ";

  @SqlQuery(
    " select " +
      " " + Dovuto.ALIAS + ".mygov_dovuto_id " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_iud " +
      "," + Dovuto.ALIAS + ".cod_iuv " +
      "," + Dovuto.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento " +
      "," + Dovuto.ALIAS + ".de_causale_visualizzata " +
      "," + Dovuto.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
      "," + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento " +
      "," + AnagraficaStato.ALIAS + ".cod_stato " +
      "," + AnagraficaStato.ALIAS + ".de_stato " +
      "," + Dovuto.ALIAS + ".dt_ultima_modifica " +
      "," + Dovuto.ALIAS + ".cod_tipo_dovuto " +
      "," + Ente.ALIAS + ".cod_ipa_ente " +
      "," + Dovuto.ALIAS + ".flg_iuv_volatile " +
      ", 'debito' as search_type " +
      SQL_SEARCH_DOVUTO_OPERATORE +
      " order by " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento, " + Dovuto.ALIAS + ".dt_creazione " +
      " limit <queryLimit>"
  )
  List<DovutoOperatoreTo> searchDovutoForOperatore(Long mygovEnteId, LocalDate dataFrom, LocalDate dataTo, Long idStatoDovuto,
                                                   boolean flgIuvVolatile, @Define String flgIuvVolatileOperator,
                                                   boolean notEnforceDataScadenza, String nomeFlusso, String causale,
                                                   String codFiscale, String iud, String iuv,
                                                   @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovuto,
                                                   @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovutoDataNonObbl,
                                                   @Define int queryLimit);

  @SqlQuery(
    " select count(1) " +
      SQL_SEARCH_DOVUTO_OPERATORE
  )
  int searchDovutoForOperatoreCount(Long mygovEnteId, LocalDate dataFrom, LocalDate dataTo, Long idStatoDovuto,
                                    boolean flgIuvVolatile, @Define String flgIuvVolatileOperator,
                                    boolean notEnforceDataScadenza, String nomeFlusso, String causale,
                                    String codFiscale, String iud, String iuv,
                                    @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovuto,
                                    @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovutoDataNonObbl);

  @SqlQuery(
    " select " +
      " " + Dovuto.ALIAS + ".mygov_dovuto_id " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco " +
      "," + Dovuto.ALIAS + ".cod_iud " +
      "," + Dovuto.ALIAS + ".cod_iuv " +
      "," + Dovuto.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento " +
      "," + Dovuto.ALIAS + ".de_causale_visualizzata " +
      "," + Dovuto.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
      "," + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento " +
      "," + AnagraficaStato.ALIAS + ".cod_stato " +
      "," + AnagraficaStato.ALIAS + ".de_stato " +
      "," + Dovuto.ALIAS + ".dt_ultima_modifica " +
      "," + Dovuto.ALIAS + ".cod_tipo_dovuto " +
      "," + Ente.ALIAS + ".cod_ipa_ente " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_anagrafica_pagatore " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_email_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_indirizzo_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_civico_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_provincia_pagatore " +
      "," + Dovuto.ALIAS + ".de_rp_sogg_pag_localita_pagatore " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_nazione_pagatore " +
      "," + Dovuto.ALIAS + ".cod_rp_sogg_pag_cap_pagatore " +
      "," + Flusso.ALIAS + ".iuf " +
      "," + Dovuto.ALIAS + ".flg_iuv_volatile " +
      ",'debito' as search_type" +
      "  from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " where " + Dovuto.ALIAS + ".mygov_dovuto_id = :mygovDovutoId " +
      "   and " + Dovuto.ALIAS + ".cod_tipo_dovuto in (<listCodTipoDovuto>) "
  )
  DovutoOperatoreTo getDovutoDetailsOperatore(
    @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovuto,
    Long mygovDovutoId, @Define String details);

  @SqlQuery(
    " select " + Dovuto.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + "," + Flusso.FIELDS + "," + Ente.FIELDS + "," + Carrello.FIELDS +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = " + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " left join mygov_carrello " + Carrello.ALIAS + " on " + Carrello.ALIAS + ".mygov_carrello_id = " + Dovuto.ALIAS + ".mygov_carrello_id " +
      " where " + Dovuto.ALIAS + ".mygov_carrello_id = :id " +
      " order by " + Dovuto.ALIAS + ".cod_iud")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getDovutiInCarrello(Long id);

  @SqlUpdate(
    " update mygov_dovuto " +
      " set mygov_anagrafica_stato_id = :mygovAnagraficaStatoId, " +
      " mygov_carrello_id = null " +
      " where mygov_dovuto_id = :mygovDovutoId")
  int updateStatusAndResetCarrello(Long mygovDovutoId, Long mygovAnagraficaStatoId);

  @SqlUpdate(
    " update mygov_dovuto " +
      " set mygov_anagrafica_stato_id = :statoId" +
      " where mygov_dovuto_id in (<ids>)")
  int updateStatus(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> ids, Long statoId);

  @SqlUpdate(
    " update mygov_dovuto " +
      " set mygov_anagrafica_stato_id = :statoId," +
      " gpd_status                    = :gpdStatus " +
      " where gpd_iupd in (<gpdIupdList>)")
  int updateStatusAndGpdStatusByIupdList(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> gpdIupdList,
                                         Long statoId,
                                         char gpdStatus);

  @SqlUpdate(
          " update mygov_dovuto " +
                  " set gpd_status = :gpdStatus " +
                  " where gpd_iupd in (<gpdIupdList>)")
  int updateGpdStatusByIupdList(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> gpdIupdList,
                                         char gpdStatus);

  @SqlUpdate(
    " update mygov_dovuto " +
      " set gpd_status = :gpdStatus" +
      " where mygov_dovuto_id in (<ids>)")
  int updateGpdStatus(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> ids, char gpdStatus);

  @SqlUpdate(
    " update mygov_dovuto " +
      " set gpd_iupd = :gpdIupd" +
      " where mygov_dovuto_id = :mygovDovutoId")
  int updateGpdIupd(Long mygovDovutoId, String gpdIupd);

  @SqlUpdate(
    " update mygov_dovuto " +
      " set gpd_status = :gpdStatus" +
      " where mygov_dovuto_id = :mygovDovutoId")
  int updateGpd(Long mygovDovutoId, char gpdStatus);

  @SqlUpdate(
    " update mygov_dovuto " +
      " set mygov_carrello_id = :carrelloId" +
      " where mygov_dovuto_id in (<ids>)")
  int putInCarrello(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> ids, Long carrelloId);

  @SqlCall("{call insert_mygov_dovuto_noinout( " +
    ":n_mygov_ente_id, " +
    ":n_mygov_flusso_id, " +
    "cast(:n_num_riga_flusso as numeric), " +
    ":n_mygov_anagrafica_stato_id, " +
    ":n_mygov_carrello_id, " +
    ":n_cod_iud, " +
    ":n_cod_iuv, " +
    "cast(:n_dt_creazione as timestamp without time zone), " +
    "cast(:n_dt_ultima_modifica as timestamp without time zone), " +
    "cast(:n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as bpchar), " +
    ":n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, " +
    ":n_de_rp_sogg_pag_anagrafica_pagatore, " +
    ":n_de_rp_sogg_pag_indirizzo_pagatore, " +
    ":n_de_rp_sogg_pag_civico_pagatore, " +
    ":n_cod_rp_sogg_pag_cap_pagatore, " +
    "left(:n_de_rp_sogg_pag_localita_pagatore," + Dovuto.MAX_LENGTH_LOCALITA + "), " +
    ":n_de_rp_sogg_pag_provincia_pagatore, " +
    ":n_cod_rp_sogg_pag_nazione_pagatore, " +
    ":n_de_rp_sogg_pag_email_pagatore, " +
    "cast(:n_dt_rp_dati_vers_data_esecuzione_pagamento as date), " +
    ":n_cod_rp_dati_vers_tipo_versamento, " +
    "cast(:n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as numeric), " +
    "cast(:n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as numeric), " +
    ":n_cod_tipo_dovuto, " +
    ":n_de_rp_dati_vers_dati_sing_vers_causale_versamento, " +
    ":n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, " +
    ":n_mygov_utente_id, " +
    ":n_bilancio, " +
    ":insert_avv_dig, " +
    ":n_flg_genera_iuv, " +
    ":n_cod_iupd, " +
    ":n_gpd_status, " +
    ":result, " +
    ":result_desc " +
    ")}")
  @OutParameter(name = "result", sqlType = Types.VARCHAR)
  @OutParameter(name = "result_desc", sqlType = Types.VARCHAR)
  OutParameters callInsertFunction(Long n_mygov_ente_id, Long n_mygov_flusso_id, Integer n_num_riga_flusso, Long n_mygov_anagrafica_stato_id,
                                   Long n_mygov_carrello_id, String n_cod_iud, String n_cod_iuv, Date n_dt_creazione, Date n_dt_ultima_modifica,
                                   String n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, String n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
                                   String n_de_rp_sogg_pag_anagrafica_pagatore, String n_de_rp_sogg_pag_indirizzo_pagatore, String n_de_rp_sogg_pag_civico_pagatore,
                                   String n_cod_rp_sogg_pag_cap_pagatore, String n_de_rp_sogg_pag_localita_pagatore, String n_de_rp_sogg_pag_provincia_pagatore,
                                   String n_cod_rp_sogg_pag_nazione_pagatore, String n_de_rp_sogg_pag_email_pagatore, Date n_dt_rp_dati_vers_data_esecuzione_pagamento,
                                   String n_cod_rp_dati_vers_tipo_versamento, Double n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
                                   Double n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, String n_cod_tipo_dovuto, String n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
                                   String n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, Long n_mygov_utente_id, String n_bilancio,
                                   boolean insert_avv_dig, boolean n_flg_genera_iuv, String n_cod_iupd, Character n_gpd_status);

  @SqlCall("{call modify_mygov_dovuto_noinout( " +
    ":n_mygov_ente_id, " +
    ":n_mygov_flusso_id, " +
    "cast(:n_num_riga_flusso as numeric), " +
    ":n_mygov_anagrafica_stato_id, " +
    ":n_mygov_carrello_id, " +
    ":n_cod_iud, " +
    ":n_cod_iuv, " +
    "cast(:n_dt_creazione as timestamp without time zone), " +
    "cast(:n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as bpchar), " +
    ":n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, " +
    ":n_de_rp_sogg_pag_anagrafica_pagatore, " +
    ":n_de_rp_sogg_pag_indirizzo_pagatore, " +
    ":n_de_rp_sogg_pag_civico_pagatore, " +
    ":n_cod_rp_sogg_pag_cap_pagatore, " +
    "left(:n_de_rp_sogg_pag_localita_pagatore," + Dovuto.MAX_LENGTH_LOCALITA + "), " +
    ":n_de_rp_sogg_pag_provincia_pagatore, " +
    ":n_cod_rp_sogg_pag_nazione_pagatore, " +
    ":n_de_rp_sogg_pag_email_pagatore, " +
    "cast(:n_dt_rp_dati_vers_data_esecuzione_pagamento as date), " +
    ":n_cod_rp_dati_vers_tipo_versamento, " +
    "cast(:n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as numeric), " +
    "cast(:n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as numeric), " +
    ":n_cod_tipo_dovuto, " +
    ":n_de_rp_dati_vers_dati_sing_vers_causale_versamento, " +
    ":n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, " +
    ":n_mygov_utente_id, " +
    ":n_bilancio, " +
    ":insert_avv_dig, " +
    ":n_flg_genera_iuv, " +
    ":n_cod_iupd, " +
    ":n_gpd_status, " +
    ":result, " +
    ":result_desc " +
    ")}")
  @OutParameter(name = "result", sqlType = Types.VARCHAR)
  @OutParameter(name = "result_desc", sqlType = Types.VARCHAR)
  OutParameters callModificaFunction(Long n_mygov_ente_id, Long n_mygov_flusso_id, Integer n_num_riga_flusso, Long n_mygov_anagrafica_stato_id,
                                     Long n_mygov_carrello_id, String n_cod_iud, String n_cod_iuv, Date n_dt_creazione, String n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
                                     String n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, String n_de_rp_sogg_pag_anagrafica_pagatore, String n_de_rp_sogg_pag_indirizzo_pagatore,
                                     String n_de_rp_sogg_pag_civico_pagatore, String n_cod_rp_sogg_pag_cap_pagatore, String n_de_rp_sogg_pag_localita_pagatore,
                                     String n_de_rp_sogg_pag_provincia_pagatore, String n_cod_rp_sogg_pag_nazione_pagatore, String n_de_rp_sogg_pag_email_pagatore,
                                     Date n_dt_rp_dati_vers_data_esecuzione_pagamento, String n_cod_rp_dati_vers_tipo_versamento,
                                     Double n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, Double n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa,
                                     String n_cod_tipo_dovuto, String n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
                                     String n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, Long n_mygov_utente_id, String n_bilancio, boolean insert_avv_dig, boolean n_flg_genera_iuv,
                                     String n_cod_iupd, Character n_gpd_status);

  @SqlQuery(
    " select " + Dovuto.ALIAS + ALL_FIELDS + ", " + AnagraficaStato.FIELDS + "," + Flusso.FIELDS + "," + Ente.FIELDS + "," + Avviso.FIELDS + "," + Carrello.FIELDS +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = " + Flusso.ALIAS + ".mygov_anagrafica_stato_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " left join mygov_carrello " + Carrello.ALIAS + " on " + Carrello.ALIAS + ".mygov_carrello_id = " + Dovuto.ALIAS + ".mygov_carrello_id " +
      " left join mygov_avviso " + Avviso.ALIAS + " on " + Avviso.ALIAS + ".cod_iuv = " + Dovuto.ALIAS + ".cod_iuv and " + Avviso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
      " where " + Dovuto.ALIAS + ".id_session = :idSession"
  )
  @RegisterFieldMapper(Dovuto.class)
  Optional<Dovuto> getByIdSession(String idSession);

  @SqlQuery("SELECT EXISTS ( " +
    "SELECT 1 FROM mygov_dovuto " + Dovuto.ALIAS +
    " INNER JOIN mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " ON " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id " +
    " INNER JOIN mygov_flusso " + Flusso.ALIAS + " ON " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
    " INNER JOIN mygov_ente " + Ente.ALIAS + " ON " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
    " WHERE " + AnagraficaStato.ALIAS + ".de_tipo_stato = '" + AnagraficaStato.STATO_TIPO_DOVUTO + "'" +
    " AND " + AnagraficaStato.ALIAS + ".cod_stato <> '" + AnagraficaStato.STATO_DOVUTO_SCADUTO + "'" +
    " AND " + Ente.ALIAS + ".mygov_ente_id = :mygovEnteId " +
    " AND " + Dovuto.ALIAS + ".cod_tipo_dovuto = :codTipoDovuto " +
    " AND " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento IS NULL " +
    ")"
  )
  boolean hasDovutoNoScadenza(Long mygovEnteId, String codTipoDovuto);


  @SqlQuery("select cod_rp_sogg_pag_id_univ_pag_codice_id_univoco " +
    " from mygov_dovuto " +
    " tablesample bernoulli(1000.0 / (SELECT reltuples AS estimate FROM pg_class where relname = 'mygov_dovuto')) " +
    " where (dt_rp_dati_vers_data_esecuzione_pagamento > current_date or dt_rp_dati_vers_data_esecuzione_pagamento is null) " +
    " limit 1")
  Optional<String> getRandomCodiceFiscaleOnDovutoTable();

  @SqlUpdate(
    " update mygov_dovuto " +
      " set id_session = :idSession" +
      " where mygov_dovuto_id = :mygovDovutoId")
  int addIdSession(Long mygovDovutoId, String idSession);

  @SqlQuery(
    " select " + Dovuto.ALIAS + ALL_FIELDS +
      "," + Flusso.FIELDS +
      "," + Ente.FIELDS +
      " from mygov_dovuto " + Dovuto.ALIAS +
      " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
      " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id " +
      " where " + Ente.ALIAS + ".cod_ipa_ente = :codIpaEnte " +
      " and " + Dovuto.ALIAS + ".cod_Tipo_Dovuto in ( <tipoDovutoList> ) " +
      " and " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento = :dtScadenza::DATE " +
      " and " + Dovuto.ALIAS + ".dt_rp_dati_vers_data_esecuzione_pagamento is not null "
  )
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getDovutiScadutiByEnteTipoData(String codIpaEnte, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> tipoDovutoList, LocalDate dtScadenza);

  @SqlQuery("SELECT " +
    Dovuto.ALIAS + ".mygov_dovuto_id, " +
    Ente.ALIAS + ".de_nome_ente, " +
    Ente.ALIAS + ".codice_fiscale_ente, " +
    Ente.ALIAS + ".cod_rp_dati_vers_dati_sing_vers_iban_accredito, " +
    Ente.ALIAS + ".de_rp_ente_benef_indirizzo_beneficiario, " +
    Ente.ALIAS + ".de_rp_ente_benef_civico_beneficiario, " +
    Ente.ALIAS + ".cod_rp_ente_benef_cap_beneficiario, " +
    Ente.ALIAS + ".cod_rp_ente_benef_nazione_beneficiario, " +
    Ente.ALIAS + ".de_rp_ente_benef_provincia_beneficiario, " +
    Ente.ALIAS + ".de_rp_ente_benef_localita_beneficiario, " +
    Dovuto.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
    "FROM mygov_dovuto " + Dovuto.ALIAS +
    " JOIN mygov_flusso " + Flusso.ALIAS + " ON " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id" +
    " JOIN mygov_ente " + Ente.ALIAS + " ON " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id" +
    " WHERE " + Dovuto.ALIAS + ".mygov_dovuto_id = :idDovuto"

  )
  DovutoEntePrimarioTo getInfoEntePrimarioByIdDovuto(Long idDovuto);

  @SqlQuery("SELECT " +
    Dovuto.ALIAS + ".mygov_dovuto_id, " +
    Ente.ALIAS + ".de_nome_ente, " +
    Ente.ALIAS + ".codice_fiscale_ente, " +
    Ente.ALIAS + ".cod_rp_dati_vers_dati_sing_vers_iban_accredito, " +
    Ente.ALIAS + ".de_rp_ente_benef_indirizzo_beneficiario, " +
    Ente.ALIAS + ".de_rp_ente_benef_civico_beneficiario, " +
    Ente.ALIAS + ".cod_rp_ente_benef_cap_beneficiario, " +
    Ente.ALIAS + ".cod_rp_ente_benef_nazione_beneficiario, " +
    Ente.ALIAS + ".de_rp_ente_benef_provincia_beneficiario, " +
    Ente.ALIAS + ".de_rp_ente_benef_localita_beneficiario, " +
    Dovuto.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento " +
    "FROM mygov_dovuto " + Dovuto.ALIAS +
    " JOIN mygov_flusso " + Flusso.ALIAS + " ON " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id" +
    " JOIN mygov_ente " + Ente.ALIAS + " ON " + Ente.ALIAS + ".mygov_ente_id = " + Flusso.ALIAS + ".mygov_ente_id" +
    " WHERE " + Dovuto.ALIAS + ".mygov_dovuto_id IN (<ids>)"
  )
  List<DovutoEntePrimarioTo> getListInfoEntePrimarioByIdDovuto(@BindList("ids") List<Long> ids);

  @SqlUpdate(
    " update mygov_dovuto " +
      " set cod_iuv = :iuv" +
      ", gpd_iupd = :iupd" +
      " where mygov_dovuto_id in (<ids>)")
  int assignIuvAndIupd(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> ids, String iuv, String iupd);

  @SqlQuery(" select " + Dovuto.ALIAS + ALL_FIELDS +
    " ,"+Dovuto.ALIAS+".mygov_flusso_id as "+Flusso.ALIAS+"_mygov_flusso_id "+
    " from mygov_dovuto " + Dovuto.ALIAS +
    " where " + Dovuto.ALIAS + ".mygov_anagrafica_stato_id = :mygovAnagraficaStatoId " +
    "   and " + Dovuto.ALIAS + ".flg_iuv_volatile = TRUE " +
    "   and " + Dovuto.ALIAS + ".dt_creazione < now() - make_interval(0,0,0,0,0,:deltaMinutes,0) " +
    " limit <queryLimit> " +
    " for update skip locked"
  )
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getOlderByIuvVolatileAndState(int deltaMinutes, Long mygovAnagraficaStatoId, @Define int queryLimit);

  @SqlQuery(
          " select " + Dovuto.ALIAS +  ".mygov_dovuto_id " +
                  " from mygov_dovuto " + Dovuto.ALIAS +
                  " inner join mygov_flusso " + Flusso.ALIAS + " on " + Flusso.ALIAS + ".mygov_flusso_id = " + Dovuto.ALIAS + ".mygov_flusso_id " +
                  " where " + Flusso.ALIAS + ".mygov_ente_id=:enteId" +
                  "  and " + Dovuto.ALIAS + ".gpd_iupd in (<iupds>) " +
                  "  and " + Dovuto.ALIAS + ".gpd_iupd is not null " )
  List<Long> getGetByIupdAndEnteId(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> iupds, Long enteId);

}
