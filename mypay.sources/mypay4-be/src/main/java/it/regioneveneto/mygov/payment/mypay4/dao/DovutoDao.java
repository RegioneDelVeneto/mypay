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
import it.regioneveneto.mygov.payment.mypay4.model.*;
import org.jdbi.v3.core.statement.OutParameters;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.OutParameter;
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
      " set version = :d.version"+
      " , flg_dovuto_attuale = :d.flgDovutoAttuale" +
      " , mygov_flusso_id = :d.mygovFlussoId.mygovFlussoId"+
      " , num_riga_flusso = :d.numRigaFlusso"+
      " , mygov_anagrafica_stato_id = :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId"+
      " , mygov_carrello_id = :d.mygovCarrelloId?.mygovCarrelloId" +
      " , cod_iud = :d.codIud"+
      " , cod_iuv = :d.codIuv"+
      " , dt_creazione = :d.dtCreazione"+
      " , dt_ultima_modifica = :d.dtUltimaModifica"+
      " , cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :d.codRpSoggPagIdUnivPagTipoIdUnivoco"+
      " , cod_rp_sogg_pag_id_univ_pag_codice_id_univoco = :d.codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      " , de_rp_sogg_pag_anagrafica_pagatore = :d.deRpSoggPagAnagraficaPagatore"+
      " , de_rp_sogg_pag_indirizzo_pagatore = :d.deRpSoggPagIndirizzoPagatore"+
      " , de_rp_sogg_pag_civico_pagatore = :d.deRpSoggPagCivicoPagatore"+
      " , cod_rp_sogg_pag_cap_pagatore = :d.codRpSoggPagCapPagatore"+
      " , de_rp_sogg_pag_localita_pagatore = left(:d.deRpSoggPagLocalitaPagatore,"+Dovuto.MAX_LENGTH_LOCALITA+")"+
      " , de_rp_sogg_pag_provincia_pagatore = :d.deRpSoggPagProvinciaPagatore"+
      " , cod_rp_sogg_pag_nazione_pagatore = :d.codRpSoggPagNazionePagatore"+
      " , de_rp_sogg_pag_email_pagatore = :d.deRpSoggPagEmailPagatore"+
      " , dt_rp_dati_vers_data_esecuzione_pagamento = :d.dtRpDatiVersDataEsecuzionePagamento"+
      " , cod_rp_dati_vers_tipo_versamento = :d.codRpDatiVersTipoVersamento"+
      " , num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento = :d.numRpDatiVersDatiSingVersImportoSingoloVersamento"+
      " , num_rp_dati_vers_dati_sing_vers_commissione_carico_pa = :d.numRpDatiVersDatiSingVersCommissioneCaricoPa"+
      " , de_rp_dati_vers_dati_sing_vers_causale_versamento = :d.deRpDatiVersDatiSingVersCausaleVersamento"+
      " , de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione = :d.deRpDatiVersDatiSingVersDatiSpecificiRiscossione"+
      " , cod_tipo_dovuto = :d.codTipoDovuto"+
      " , mygov_avviso_id = :d.mygovAvvisoId?.mygovAvvisoId"+
      " , dt_creazione_cod_iuv = :d.dtCreazioneCodIuv"+
      " , mygov_dati_marca_bollo_digitale_id = :d.mygovDatiMarcaBolloDigitaleId"+
      " , de_causale_visualizzata = :d.deCausaleVisualizzata"+
      " , bilancio = :d.bilancio"+
      " , flg_genera_iuv = :d.flgGeneraIuv"+
      " , id_session = :d.idSession"+
      " where mygov_dovuto_id = :d.mygovDovutoId")
  int update(@BindBean("d") Dovuto d);

  @SqlUpdate(" insert into mygov_dovuto (" +
      "   mygov_dovuto_id"+
      " , version"+
      " , flg_dovuto_attuale" +
      " , mygov_flusso_id"+
      " , num_riga_flusso"+
      " , mygov_anagrafica_stato_id"+
      " , mygov_carrello_id" +
      " , cod_iud"+
      " , cod_iuv"+
      " , dt_creazione"+
      " , dt_ultima_modifica"+
      " , cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco"+
      " , cod_rp_sogg_pag_id_univ_pag_codice_id_univoco"+
      " , de_rp_sogg_pag_anagrafica_pagatore"+
      " , de_rp_sogg_pag_indirizzo_pagatore"+
      " , de_rp_sogg_pag_civico_pagatore"+
      " , cod_rp_sogg_pag_cap_pagatore"+
      " , de_rp_sogg_pag_localita_pagatore"+
      " , de_rp_sogg_pag_provincia_pagatore"+
      " , cod_rp_sogg_pag_nazione_pagatore"+
      " , de_rp_sogg_pag_email_pagatore"+
      " , dt_rp_dati_vers_data_esecuzione_pagamento"+
      " , cod_rp_dati_vers_tipo_versamento"+
      " , num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento"+
      " , num_rp_dati_vers_dati_sing_vers_commissione_carico_pa"+
      " , de_rp_dati_vers_dati_sing_vers_causale_versamento"+
      " , de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione"+
      " , cod_tipo_dovuto"+
      " , mygov_avviso_id"+
      " , dt_creazione_cod_iuv"+
      " , mygov_dati_marca_bollo_digitale_id"+
      " , de_causale_visualizzata"+
      " , bilancio"+
      " , flg_genera_iuv"+
      " , id_session "+
      ") values ("+
      "   nextval('mygov_dovuto_mygov_dovuto_id_seq')"+
      " , :d.version"+
      " , :d.flgDovutoAttuale" +
      " , :d.mygovFlussoId.mygovFlussoId"+
      " , :d.numRigaFlusso"+
      " , :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId"+
      " , :d.mygovCarrelloId?.mygovCarrelloId" +
      " , :d.codIud"+
      " , :d.codIuv"+
      " , :d.dtCreazione"+
      " , :d.dtUltimaModifica"+
      " , :d.codRpSoggPagIdUnivPagTipoIdUnivoco"+
      " , :d.codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      " , :d.deRpSoggPagAnagraficaPagatore"+
      " , :d.deRpSoggPagIndirizzoPagatore"+
      " , :d.deRpSoggPagCivicoPagatore"+
      " , :d.codRpSoggPagCapPagatore"+
      " , left(:d.deRpSoggPagLocalitaPagatore,"+Dovuto.MAX_LENGTH_LOCALITA+")"+
      " , :d.deRpSoggPagProvinciaPagatore"+
      " , :d.codRpSoggPagNazionePagatore"+
      " , :d.deRpSoggPagEmailPagatore"+
      " , :d.dtRpDatiVersDataEsecuzionePagamento"+
      " , :d.codRpDatiVersTipoVersamento"+
      " , :d.numRpDatiVersDatiSingVersImportoSingoloVersamento"+
      " , :d.numRpDatiVersDatiSingVersCommissioneCaricoPa"+
      " , :d.deRpDatiVersDatiSingVersCausaleVersamento"+
      " , :d.deRpDatiVersDatiSingVersDatiSpecificiRiscossione"+
      " , :d.codTipoDovuto"+
      " , :d.mygovAvvisoId?.mygovAvvisoId"+
      " , :d.dtCreazioneCodIuv"+
      " , :d.mygovDatiMarcaBolloDigitaleId"+
      " , :d.deCausaleVisualizzata"+
      " , :d.bilancio"+
      " , :d.flgGeneraIuv"+
      " , :d.idSession)")
  @GetGeneratedKeys("mygov_dovuto_id")
  long insert(@BindBean("d") Dovuto d);

  @SqlUpdate(
      " delete from mygov_dovuto " +
          " where mygov_dovuto_id = :d.mygovDovutoId " +
          " and version = :d.version " +
          " and mygov_flusso_id = :d.mygovFlussoId.mygovFlussoId" +
          " and mygov_anagrafica_stato_id = :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId")
  int delete(@BindBean("d") Dovuto d);

  @SqlQuery(
      " select "+Dovuto.ALIAS+ALL_FIELDS+", "+AnagraficaStato.FIELDS+","+Flusso.FIELDS +","+Ente.FIELDS+","+Avviso.FIELDS+","+Carrello.FIELDS +
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " left join mygov_carrello "+Carrello.ALIAS+" on "+Carrello.ALIAS+".mygov_carrello_id = "+Dovuto.ALIAS+".mygov_carrello_id " +
          " left join mygov_avviso "+Avviso.ALIAS+" on "+Avviso.ALIAS+".cod_iuv = "+Dovuto.ALIAS+".cod_iuv and " +Avviso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Dovuto.ALIAS+".mygov_dovuto_id = :id" )
  @RegisterFieldMapper(Dovuto.class)
  Dovuto getById(Long id);

  @SqlQuery(
      " select count(*)" +
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " where ("+Dovuto.ALIAS+".cod_tipo_dovuto = :codTipoDovuto or :codTipoDovuto is null)" +
          "   and ("+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte or :codIpaEnte is null)")
  long count(String codIpaEnte, String codTipoDovuto);

  @SqlQuery(
      " select "+
          Dovuto.ALIAS+".mygov_dovuto_id "+
          ",coalesce("+Avviso.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, "+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) "+
          " as cod_rp_sogg_pag_id_univ_pag_codice_id_univoco "+
          ","+Dovuto.ALIAS+".cod_iud "+
          ","+Dovuto.ALIAS+".cod_iuv "+
          ","+Dovuto.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento "+
          ","+Dovuto.ALIAS+".de_causale_visualizzata "+
          ","+Dovuto.ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "+
          ","+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento "+
          ","+AnagraficaStato.ALIAS+".cod_stato "+
          ","+AnagraficaStato.ALIAS+".de_stato "+
          ","+Dovuto.ALIAS+".cod_tipo_dovuto "+
          ","+Ente.ALIAS+".cod_ipa_ente "+
          ","+Ente.ALIAS+".de_nome_ente "+
          ","+Ente.ALIAS+".application_code "+
          ","+Dovuto.ALIAS+".cod_rp_dati_vers_tipo_versamento "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_cap_pagatore "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_nazione_pagatore "+
          ",coalesce("+Avviso.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore, "+Dovuto.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore) "+
          " as de_rp_sogg_pag_anagrafica_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_civico_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_email_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_indirizzo_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_localita_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_provincia_pagatore "+
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " left join mygov_carrello carrello on carrello.mygov_carrello_id = "+Dovuto.ALIAS+".mygov_carrello_id " +
          " left join mygov_avviso "+Avviso.ALIAS+" on "+Avviso.ALIAS+".cod_iuv = "+Dovuto.ALIAS+".cod_iuv and " +Avviso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Dovuto.ALIAS+".mygov_dovuto_id = :id" )
  DovutoTo getToById(Long id);

  @SqlQuery(
      " select "+Dovuto.ALIAS+ALL_FIELDS+", "+AnagraficaStato.FIELDS+","+Flusso.FIELDS +","+Ente.FIELDS+","+Avviso.FIELDS+","+Carrello.FIELDS +
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " left join mygov_carrello "+Carrello.ALIAS+" on "+Carrello.ALIAS+".mygov_carrello_id = "+Dovuto.ALIAS+".mygov_carrello_id " +
          " left join mygov_avviso "+Avviso.ALIAS+" on "+Avviso.ALIAS+".cod_iuv = "+Dovuto.ALIAS+".cod_iuv and " +Avviso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte" +
          "   and "+Dovuto.ALIAS+".cod_iuv = :codIuv")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByIuvEnte(String codIuv, String codIpaEnte);

  @SqlQuery(
      " select "+Dovuto.ALIAS+ALL_FIELDS+", "+AnagraficaStato.FIELDS+","+Flusso.FIELDS +","+Ente.FIELDS+","+Avviso.FIELDS+","+Carrello.FIELDS +
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " left join mygov_carrello "+Carrello.ALIAS+" on "+Carrello.ALIAS+".mygov_carrello_id = "+Dovuto.ALIAS+".mygov_carrello_id " +
          " left join mygov_avviso "+Avviso.ALIAS+" on "+Avviso.ALIAS+".cod_iuv = "+Dovuto.ALIAS+".cod_iuv and " +Avviso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte" +
          "   and "+Dovuto.ALIAS+".cod_iud = :codIud")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByIudEnte(String codIud, String codIpaEnte);

  @SqlQuery(
      " select "+Dovuto.ALIAS+ALL_FIELDS+", "+AnagraficaStato.FIELDS+","+Flusso.FIELDS +","+Ente.FIELDS+","+Avviso.FIELDS+","+Carrello.FIELDS +
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " left join mygov_carrello "+Carrello.ALIAS+" on "+Carrello.ALIAS+".mygov_carrello_id = "+Dovuto.ALIAS+".mygov_carrello_id " +
          " left join mygov_avviso "+Avviso.ALIAS+" on "+Avviso.ALIAS+".cod_iuv = "+Dovuto.ALIAS+".cod_iuv and " +Avviso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Dovuto.ALIAS+".mygov_flusso_id = :flussoId" )
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getByFlussoId(Long flussoId);

  @SqlQuery(" select "+Dovuto.ALIAS+ALL_FIELDS+", "+AnagraficaStato.FIELDS+","+Flusso.FIELDS +","+Ente.FIELDS+","+Avviso.FIELDS +
      " from mygov_flusso "+ Flusso.ALIAS+
      "  	   inner join mygov_dovuto "+Dovuto.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
      "      inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
      "      inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id ="+Flusso.ALIAS+".mygov_ente_id " +
      "      inner join mygov_ente_tipo_dovuto "+EnteTipoDovuto.ALIAS+" on "+EnteTipoDovuto.ALIAS+".mygov_ente_id ="+Flusso.ALIAS+".mygov_ente_id and "+EnteTipoDovuto.ALIAS+".cod_tipo = "+Dovuto.ALIAS+".cod_tipo_dovuto " +
      "      inner join mygov_anagrafica_stato anagFlusso on anagFlusso.mygov_anagrafica_stato_id ="+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
      "      left join mygov_avviso "+Avviso.ALIAS+" on "+Avviso.ALIAS+".cod_iuv = "+Dovuto.ALIAS+".cod_iuv and "+Avviso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
      " where "+Flusso.ALIAS+".mygov_anagrafica_stato_id = :idStatoFlussoCaricato " +
      "   and "+Dovuto.ALIAS+".flg_dovuto_attuale = true " +
      "   and "+Dovuto.ALIAS+".mygov_anagrafica_stato_id = :idStatoDovutoDaPagare " +
      " and ("+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte or :codIpaEnte is null) " +
      "	and "+EnteTipoDovuto.ALIAS+".flg_attivo = true " +
      "	and ("+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria = true and "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento >= now() or "+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria = false) " +
      "	and "+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :codRpSoggPagIdUnivPagTipoIdUnivoco " +
      "	and lower("+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:codRpSoggPagIdUnivPagCodiceIdUnivoco) " +
      "	order by "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento asc NULLS LAST , "+Dovuto.ALIAS+".dt_creazione asc NULLS last "
  )
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getUnpaidByEnteIdUnivocoPersona(String codRpSoggPagIdUnivPagTipoIdUnivoco, String codRpSoggPagIdUnivPagCodiceIdUnivoco, String codIpaEnte, Long idStatoDovutoDaPagare, Long idStatoFlussoCaricato);

  String SQL_SEARCH_DOVUTO =
      " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " inner join mygov_ente_tipo_dovuto "+EnteTipoDovuto.ALIAS+" on "+EnteTipoDovuto.ALIAS+".cod_tipo = "+Dovuto.ALIAS+".cod_tipo_dovuto " +
          "            and "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " left join mygov_carrello "+Carrello.ALIAS+" on "+Carrello.ALIAS+".mygov_carrello_id = "+Dovuto.ALIAS+".mygov_carrello_id " +
          " left join mygov_avviso "+Avviso.ALIAS+" on "+Avviso.ALIAS+".cod_iuv = "+Dovuto.ALIAS+".cod_iuv and " +Avviso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Dovuto.ALIAS+".mygov_anagrafica_stato_id <> :idStatoDovutoPredisposto " +
          " and "+Flusso.ALIAS+".mygov_anagrafica_stato_id = :idStatoFlussoCaricato " +
          " and "+Ente.ALIAS+".cd_stato_ente = :idStatoEnteEsercizio " +
          " and ("+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte or :codIpaEnte is null)" +
          " and ("+Dovuto.ALIAS+".cod_tipo_dovuto = :codTipoDovuto or :codTipoDovuto is null)" +
          " and ("+Dovuto.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento ilike '%' || :causale || '%' or :causale is null)" +
          " and  "+EnteTipoDovuto.ALIAS+".flg_attivo = true " +
          " and ( " +
          "          "+Carrello.ALIAS+".mygov_carrello_id is null " +
          "      and lower("+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
          "   or " +
          "          "+Carrello.ALIAS+".mygov_carrello_id is not null and ( " +
          "             lower("+Carrello.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) or " +
          "             lower("+Carrello.ALIAS+".cod_rp_sogg_vers_id_univ_vers_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
          "           )   ) " +
          " and ( " +
          "         "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento is not null " +
          "     and :dataFrom::DATE <= "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento " +
          "     and "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento < :dataTo::DATE " +
          "	    and "+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria = true and "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento >= now() " +
          " or "+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria = false) ";

  @SqlQuery(
      " select "+
          Dovuto.ALIAS+".mygov_dovuto_id "+
          ",coalesce("+Avviso.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, "+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) "+
          " as cod_rp_sogg_pag_id_univ_pag_codice_id_univoco "+
          ","+Dovuto.ALIAS+".cod_iud "+
          ","+Dovuto.ALIAS+".cod_iuv "+
          ","+Dovuto.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento "+
          ","+Dovuto.ALIAS+".de_causale_visualizzata "+
          ","+Dovuto.ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "+
          ","+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento "+
          ","+AnagraficaStato.ALIAS+".cod_stato "+
          ","+AnagraficaStato.ALIAS+".de_stato "+
          ","+Dovuto.ALIAS+".cod_tipo_dovuto "+
          ","+Ente.ALIAS+".cod_ipa_ente "+
          ","+Ente.ALIAS+".de_nome_ente "+
          ","+Ente.ALIAS+".application_code "+
          ","+Dovuto.ALIAS+".cod_rp_dati_vers_tipo_versamento "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_cap_pagatore "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_nazione_pagatore "+
          ",coalesce("+Avviso.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore, "+Dovuto.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore) "+
          " as de_rp_sogg_pag_anagrafica_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_civico_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_email_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_indirizzo_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_localita_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_provincia_pagatore "+
          SQL_SEARCH_DOVUTO +
          " order by " +
          Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento ASC NULLS LAST, "+Dovuto.ALIAS+".dt_creazione " +
          " limit <queryLimit>"
  )
  List<DovutoTo> searchDovuto(String codIpaEnte, Long idStatoEnteEsercizio, String idUnivocoPagatoreVersante,
                              String codTipoDovuto, String causale, LocalDate dataFrom, LocalDate dataTo,
                              Long idStatoDovutoPredisposto, Long idStatoFlussoCaricato, @Define int queryLimit);

  @SqlQuery(
      " select count(1) " +
          SQL_SEARCH_DOVUTO
  )
  int searchDovutoCount(String codIpaEnte, Long idStatoEnteEsercizio, String idUnivocoPagatoreVersante,
                        String codTipoDovuto, String causale, LocalDate dataFrom, LocalDate dataTo,
                        Long idStatoDovutoPredisposto, Long idStatoFlussoCaricato);

  @SqlQuery(
      " select "+
          Dovuto.ALIAS+".mygov_dovuto_id "+
          ",coalesce("+Avviso.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, "+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) "+
          " as cod_rp_sogg_pag_id_univ_pag_codice_id_univoco "+
          ","+Dovuto.ALIAS+".cod_iud "+
          ","+Dovuto.ALIAS+".cod_iuv "+
          ","+Dovuto.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento "+
          ","+Dovuto.ALIAS+".de_causale_visualizzata "+
          ","+Dovuto.ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "+
          ","+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento "+
          ","+AnagraficaStato.ALIAS+".cod_stato "+
          ","+AnagraficaStato.ALIAS+".de_stato "+
          ","+Dovuto.ALIAS+".cod_tipo_dovuto "+
          ","+Ente.ALIAS+".cod_ipa_ente "+
          ","+Ente.ALIAS+".de_nome_ente "+
          ","+Ente.ALIAS+".application_code "+
          ","+Dovuto.ALIAS+".cod_rp_dati_vers_tipo_versamento "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_cap_pagatore "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_nazione_pagatore "+
          ",coalesce("+Avviso.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore, "+Dovuto.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore) "+
          " as de_rp_sogg_pag_anagrafica_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_civico_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_email_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_indirizzo_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_localita_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_provincia_pagatore "+
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " inner join mygov_ente_tipo_dovuto "+EnteTipoDovuto.ALIAS+" on "+EnteTipoDovuto.ALIAS+".cod_tipo = "+Dovuto.ALIAS+".cod_tipo_dovuto " +
          "            and "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " left join mygov_avviso "+Avviso.ALIAS+" on "+Avviso.ALIAS+".cod_iuv = "+Dovuto.ALIAS+".cod_iuv and " +Avviso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Dovuto.ALIAS+".mygov_anagrafica_stato_id <> :idStatoDovutoPredisposto " +
          " and "+Flusso.ALIAS+".mygov_anagrafica_stato_id = :idStatoFlussoCaricato " +
          " and "+Ente.ALIAS+".cd_stato_ente = :idStatoEnteEsercizio " +
          " and  "+EnteTipoDovuto.ALIAS+".flg_attivo = true " +
          " and lower("+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
          " and "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento is not null " +
          "	and ("+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria = true and "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento >= now() " +
          "           or "+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria = false ) "+
          " order by " + Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento ASC, "+Dovuto.ALIAS+".dt_creazione " +
          " limit :num "
  )
  List<DovutoTo> searchLastDovuto(String idUnivocoPagatoreVersante, Long idStatoEnteEsercizio, Long idStatoDovutoPredisposto, Long idStatoFlussoCaricato, Integer num);

  @SqlQuery(
      " select 1 "+
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " where "+AnagraficaStato.ALIAS+".cod_stato='PAGAMENTO_INIZIATO' " +
          "   and "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte " +
          "   and "+Dovuto.ALIAS+".cod_tipo_dovuto = :codTipoDovuto " +
          "   and "+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :tipoIdUnivocoPagatoreVersante " +
          "   and lower("+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:idUnivocoPagatoreVersante) " +
          "   and coalesce("+Dovuto.ALIAS+".dt_ultima_modifica, "+Dovuto.ALIAS+".dt_creazione) between now() - interval '1 day' and now() " + //only last 24 hours
          "   and "+Dovuto.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento = :causale " +
          " limit 1 "
  )
  Optional<Boolean> hasReplicaDovuto(String codIpaEnte, char tipoIdUnivocoPagatoreVersante, String idUnivocoPagatoreVersante,
                                     String causale, String codTipoDovuto);

  @SqlQuery(
      " select "+
          " "+Dovuto.ALIAS+".mygov_dovuto_id "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco "+
          ","+Dovuto.ALIAS+".cod_iud "+
          ","+Dovuto.ALIAS+".cod_iuv "+
          ","+Dovuto.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento "+
          ","+Dovuto.ALIAS+".de_causale_visualizzata "+
          ","+Dovuto.ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "+
          ","+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento "+
          ","+AnagraficaStato.ALIAS+".cod_stato "+
          ","+AnagraficaStato.ALIAS+".de_stato "+
          ","+Dovuto.ALIAS+".cod_tipo_dovuto "+
          ","+Ente.ALIAS+".cod_ipa_ente "+
          ","+Ente.ALIAS+".de_nome_ente "+
          ","+Ente.ALIAS+".application_code "+
          ","+Dovuto.ALIAS+".cod_rp_dati_vers_tipo_versamento "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_cap_pagatore "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_nazione_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_civico_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_email_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_indirizzo_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_localita_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_provincia_pagatore "+
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " inner join mygov_ente_tipo_dovuto "+EnteTipoDovuto.ALIAS+" on "+EnteTipoDovuto.ALIAS+".cod_tipo = "+Dovuto.ALIAS+".cod_tipo_dovuto " +
          "            and "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " left join mygov_carrello carrello on carrello.mygov_carrello_id = "+Dovuto.ALIAS+".mygov_carrello_id " +
          " where ("+Ente.ALIAS+".mygov_ente_id = :mygovEnteId or :mygovEnteId is null) " +
          " and ("+Dovuto.ALIAS+".cod_tipo_dovuto = :codTipoDovuto or :codTipoDovuto is null) " +
          " and "+Dovuto.ALIAS+".cod_iuv = :codIuv " +
          " and lower("+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco) = lower(:codRpSoggPagIdUnivPagCodiceIdUnivoco) " +
          " and (:deRpSoggPagAnagraficaPagatore is null or lower("+Dovuto.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore) = lower(:deRpSoggPagAnagraficaPagatore) ) " +
          " and ( "+EnteTipoDovuto.ALIAS+".flg_attivo = true or :searchOnlyOnTipoAttivo is not true ) ")
  List<DovutoTo> searchDovutoOnTipoAttivo(Long mygovEnteId, String codTipoDovuto, String codIuv,
                                          String codRpSoggPagIdUnivPagCodiceIdUnivoco, String deRpSoggPagAnagraficaPagatore,
                                          Boolean searchOnlyOnTipoAttivo);

  @SqlQuery(
      " select "+Dovuto.ALIAS + ALL_FIELDS+", "+AnagraficaStato.FIELDS+","+Flusso.FIELDS +","+Ente.FIELDS+
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " where "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte " +
          " and "+Dovuto.ALIAS+".cod_iuv = :codIuv ")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> searchDovutoByIuvEnte(String codIuv, String codIpaEnte);


  String SQL_SEARCH_DOVUTO_OPERATORE =
      " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " where ("+Dovuto.ALIAS+".mygov_anagrafica_stato_id = :idStatoDovuto or :idStatoDovuto is null) " +
          " and "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId " +
          " and "+Dovuto.ALIAS+".cod_tipo_dovuto in (<listCodTipoDovuto>) " +
          " and ("+Dovuto.ALIAS+".de_causale_visualizzata ilike '%' || :causale || '%' " +
          "      or "+Dovuto.ALIAS+".de_causale_visualizzata is null and "+Dovuto.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento ilike '%' || :causale || '%' " +
          "      or :causale is null)" +
          " and ("+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco ilike '%' || :codFiscale || '%' or :codFiscale is null)" +
          " and ("+Dovuto.ALIAS+".cod_iud ilike '%' || :iud || '%' or :iud is null)" +
          " and ("+Dovuto.ALIAS+".cod_iuv ilike '%' || :iuv || '%' or :iuv is null)" +
          " and ("+Flusso.ALIAS+".iuf= :nomeFlusso or :nomeFlusso is null)" +
          " and ("+Dovuto.ALIAS+".cod_tipo_dovuto in (<listCodTipoDovutoDataNonObbl>) " +
          "      and :notEnforceDataScadenza " +
          "      or  "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento >= :dataFrom::DATE " +
          "      and "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento < :dataTo::DATE ) " ;

  @SqlQuery(
      " select "+
          " "+Dovuto.ALIAS+".mygov_dovuto_id "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco "+
          ","+Dovuto.ALIAS+".cod_iud "+
          ","+Dovuto.ALIAS+".cod_iuv "+
          ","+Dovuto.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento "+
          ","+Dovuto.ALIAS+".de_causale_visualizzata "+
          ","+Dovuto.ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "+
          ","+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento "+
          ","+AnagraficaStato.ALIAS+".cod_stato "+
          ","+AnagraficaStato.ALIAS+".de_stato "+
          ","+Dovuto.ALIAS+".dt_ultima_modifica "+
          ","+Dovuto.ALIAS+".cod_tipo_dovuto "+
          ","+Ente.ALIAS+".cod_ipa_ente "+
          ", 'debito' as search_type "+
          SQL_SEARCH_DOVUTO_OPERATORE+
          " order by "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento, "+Dovuto.ALIAS+".dt_creazione " +
          " limit <queryLimit>"
  )
  List<DovutoOperatoreTo> searchDovutoForOperatore(Long mygovEnteId, LocalDate dataFrom, LocalDate dataTo,
                                                   Long idStatoDovuto, boolean notEnforceDataScadenza, String nomeFlusso, String causale,
                                                   String codFiscale, String iud, String iuv,
                                                   @BindList(onEmpty=BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovuto,
                                                   @BindList(onEmpty=BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovutoDataNonObbl,
                                                   @Define int queryLimit);

  @SqlQuery(
      " select count(1) " +
          SQL_SEARCH_DOVUTO_OPERATORE
  )
  int searchDovutoForOperatoreCount(Long mygovEnteId, LocalDate dataFrom, LocalDate dataTo,
                                    Long idStatoDovuto, boolean notEnforceDataScadenza, String nomeFlusso, String causale,
                                    String codFiscale, String iud, String iuv,
                                    @BindList(onEmpty=BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovuto,
                                    @BindList(onEmpty=BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovutoDataNonObbl);

  @SqlQuery(
      " select "+
          " "+Dovuto.ALIAS+".mygov_dovuto_id "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco "+
          ","+Dovuto.ALIAS+".cod_iud "+
          ","+Dovuto.ALIAS+".cod_iuv "+
          ","+Dovuto.ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento "+
          ","+Dovuto.ALIAS+".de_causale_visualizzata "+
          ","+Dovuto.ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "+
          ","+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento "+
          ","+AnagraficaStato.ALIAS+".cod_stato "+
          ","+AnagraficaStato.ALIAS+".de_stato "+
          ","+Dovuto.ALIAS+".dt_ultima_modifica "+
          ","+Dovuto.ALIAS+".cod_tipo_dovuto "+
          ","+Ente.ALIAS+".cod_ipa_ente "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_anagrafica_pagatore "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_email_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_indirizzo_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_civico_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_provincia_pagatore "+
          ","+Dovuto.ALIAS+".de_rp_sogg_pag_localita_pagatore "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_nazione_pagatore "+
          ","+Dovuto.ALIAS+".cod_rp_sogg_pag_cap_pagatore "+
          ","+Flusso.ALIAS+".iuf "+
          ",'debito' as search_type"+
          "  from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " where "+Dovuto.ALIAS+".mygov_dovuto_id = :mygovDovutoId " +
          "   and "+Dovuto.ALIAS+".cod_tipo_dovuto in (<listCodTipoDovuto>) "
  )
  DovutoOperatoreTo getDovutoDetailsOperatore(
      @BindList(onEmpty= BindList.EmptyHandling.NULL_STRING) List<String> listCodTipoDovuto,
      Long mygovDovutoId, @Define String details);

  @SqlQuery(
      "select " + Dovuto.ALIAS + ALL_FIELDS +
          " , mygov_flusso_id as Flusso_mygovFlussoId " +
          " , mygov_anagrafica_stato_id as AnagraficaStato_mygovAnagraficaStatoId " +
          " , mygov_carrello_id as Carrello_mygovCarrelloId " +
          "  from mygov_dovuto " + Dovuto.ALIAS +
          " where " + Dovuto.ALIAS + ".mygov_carrello_id = :id "+
          " order by " + Dovuto.ALIAS + ".cod_iud")
  @RegisterFieldMapper(Dovuto.class)
  List<Dovuto> getDovutiInCarrello(Long id);

  @SqlUpdate(
      " update mygov_dovuto " +
          " set mygov_anagrafica_stato_id = :mygovAnagraficaStatoId, "+
          " mygov_carrello_id = null " +
          " where mygov_dovuto_id = :mygovDovutoId")
  int updateStatusAndResetCarrello(Long mygovDovutoId, Long mygovAnagraficaStatoId);

  @SqlUpdate(
      " update mygov_dovuto " +
          " set mygov_anagrafica_stato_id = :statoId"+
          " where mygov_dovuto_id in (<ids>)")
  int updateStatus(@BindList(onEmpty= BindList.EmptyHandling.NULL_STRING) List<Long> ids, Long statoId);

  @SqlUpdate(
      " update mygov_dovuto " +
          " set mygov_carrello_id = :carrelloId"+
          " where mygov_dovuto_id in (<ids>)")
  int putInCarrello(@BindList(onEmpty= BindList.EmptyHandling.NULL_STRING) List<Long> ids, Long carrelloId);

  @SqlCall("{call insert_mygov_dovuto_noinout( "+
      ":n_mygov_ente_id, "+
      ":n_mygov_flusso_id, "+
      "cast(:n_num_riga_flusso as numeric), "+
      ":n_mygov_anagrafica_stato_id, "+
      ":n_mygov_carrello_id, "+
      ":n_cod_iud, "+
      ":n_cod_iuv, "+
      "cast(:n_dt_creazione as timestamp without time zone), "+
      "cast(:n_dt_ultima_modifica as timestamp without time zone), "+
      "cast(:n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as bpchar), "+
      ":n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, "+
      ":n_de_rp_sogg_pag_anagrafica_pagatore, "+
      ":n_de_rp_sogg_pag_indirizzo_pagatore, "+
      ":n_de_rp_sogg_pag_civico_pagatore, "+
      ":n_cod_rp_sogg_pag_cap_pagatore, "+
      "left(:n_de_rp_sogg_pag_localita_pagatore,"+Dovuto.MAX_LENGTH_LOCALITA+"), "+
      ":n_de_rp_sogg_pag_provincia_pagatore, "+
      ":n_cod_rp_sogg_pag_nazione_pagatore, "+
      ":n_de_rp_sogg_pag_email_pagatore, "+
      "cast(:n_dt_rp_dati_vers_data_esecuzione_pagamento as date), "+
      ":n_cod_rp_dati_vers_tipo_versamento, "+
      "cast(:n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as numeric), "+
      "cast(:n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as numeric), "+
      ":n_cod_tipo_dovuto, "+
      ":n_de_rp_dati_vers_dati_sing_vers_causale_versamento, "+
      ":n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, "+
      ":n_mygov_utente_id, "+
      ":n_bilancio, "+
      ":insert_avv_dig, "+
      ":n_flg_genera_iuv, "+
      ":result, "+
      ":result_desc "+
      ")}" )
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
                                   String n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, Long n_mygov_utente_id, String n_bilancio, boolean insert_avv_dig, boolean n_flg_genera_iuv);

  @SqlCall("{call modify_mygov_dovuto_noinout( "+
      ":n_mygov_ente_id, "+
      ":n_mygov_flusso_id, "+
      "cast(:n_num_riga_flusso as numeric), "+
      ":n_mygov_anagrafica_stato_id, "+
      ":n_mygov_carrello_id, "+
      ":n_cod_iud, "+
      ":n_cod_iuv, "+
      "cast(:n_dt_creazione as timestamp without time zone), "+
      "cast(:n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as bpchar), "+
      ":n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, "+
      ":n_de_rp_sogg_pag_anagrafica_pagatore, "+
      ":n_de_rp_sogg_pag_indirizzo_pagatore, "+
      ":n_de_rp_sogg_pag_civico_pagatore, "+
      ":n_cod_rp_sogg_pag_cap_pagatore, "+
      "left(:n_de_rp_sogg_pag_localita_pagatore,"+Dovuto.MAX_LENGTH_LOCALITA+"), "+
      ":n_de_rp_sogg_pag_provincia_pagatore, "+
      ":n_cod_rp_sogg_pag_nazione_pagatore, "+
      ":n_de_rp_sogg_pag_email_pagatore, "+
      "cast(:n_dt_rp_dati_vers_data_esecuzione_pagamento as date), "+
      ":n_cod_rp_dati_vers_tipo_versamento, "+
      "cast(:n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as numeric), "+
      "cast(:n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as numeric), "+
      ":n_cod_tipo_dovuto, "+
      ":n_de_rp_dati_vers_dati_sing_vers_causale_versamento, "+
      ":n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, "+
      ":n_mygov_utente_id, "+
      ":n_bilancio, "+
      ":insert_avv_dig, "+
      ":n_flg_genera_iuv, "+
      ":result, "+
      ":result_desc "+
      ")}" )
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
                                     String n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, Long n_mygov_utente_id, String n_bilancio, boolean insert_avv_dig, boolean n_flg_genera_iuv);

  @SqlQuery(
      " select "+Dovuto.ALIAS+ALL_FIELDS+", "+AnagraficaStato.FIELDS+","+Flusso.FIELDS +","+Ente.FIELDS+","+Avviso.FIELDS+","+Carrello.FIELDS +
          " from mygov_dovuto " + Dovuto.ALIAS +
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_anagrafica_stato asflusso on asflusso.mygov_anagrafica_stato_id = "+Flusso.ALIAS+".mygov_anagrafica_stato_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " left join mygov_carrello "+Carrello.ALIAS+" on "+Carrello.ALIAS+".mygov_carrello_id = "+Dovuto.ALIAS+".mygov_carrello_id " +
          " left join mygov_avviso "+Avviso.ALIAS+" on "+Avviso.ALIAS+".cod_iuv = "+Dovuto.ALIAS+".cod_iuv and " +Avviso.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Dovuto.ALIAS+".id_session = :idSession"
  )
  @RegisterFieldMapper(Dovuto.class)
  Optional<Dovuto> getByIdSession(String idSession);

  @SqlQuery("SELECT EXISTS ( " +
      "SELECT 1 FROM mygov_dovuto " + Dovuto.ALIAS +
      " INNER JOIN mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" ON "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+Dovuto.ALIAS+".mygov_anagrafica_stato_id " +
      " INNER JOIN mygov_flusso "+Flusso.ALIAS+" ON "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
      " INNER JOIN mygov_ente "+Ente.ALIAS+" ON "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
      " WHERE "+AnagraficaStato.ALIAS+".de_tipo_stato = '" + AnagraficaStato.STATO_TIPO_DOVUTO + "'" +
      " AND "+AnagraficaStato.ALIAS+".cod_stato <> '" + AnagraficaStato.STATO_DOVUTO_SCADUTO + "'" +
      " AND "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId " +
      " AND "+Dovuto.ALIAS+".cod_tipo_dovuto = :codTipoDovuto " +
      " AND "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento IS NULL " +
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
      " set id_session = :idSession"+
      " where mygov_dovuto_id = :mygovDovutoId")
  int addIdSession(Long mygovDovutoId, String idSession);
}
