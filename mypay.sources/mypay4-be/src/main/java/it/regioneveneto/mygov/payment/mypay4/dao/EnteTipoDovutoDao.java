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

import it.regioneveneto.mygov.payment.mypay4.dto.EnteTipoDovutoTo;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

import java.util.List;
import java.util.Optional;

public interface EnteTipoDovutoDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_ente_tipo_dovuto (" +
          "   mygov_ente_tipo_dovuto_id" +
          " , mygov_ente_id" +
          " , cod_tipo" +
          " , de_tipo" +
          " , iban_accredito_pi" +
          " , bic_accredito_pi" +
          " , iban_appoggio_pi" +
          " , bic_appoggio_pi" +
          " , iban_accredito_psp" +
          " , bic_accredito_psp" +
          " , iban_appoggio_psp" +
          " , bic_appoggio_psp" +
          " , cod_conto_corrente_postale" +
          " , cod_xsd_causale" +
          " , bic_accredito_pi_seller" +
          " , bic_accredito_psp_seller" +
          " , spontaneo" +
          " , importo" +
          " , de_url_pagamento_dovuto" +
          " , de_bilancio_default" +
          " , flg_cf_anonimo" +
          " , flg_scadenza_obbligatoria" +
          " , flg_stampa_data_scadenza" +
          " , de_intestatario_cc_postale" +
          " , de_settore_ente" +
          " , flg_notifica_io" +
          " , flg_notifica_esito_push" +
          " , max_tentativi_inoltro_esito" +
          " , mygov_ente_sil_id" +
          " , flg_attivo" +
          " , codice_contesto_pagamento" +
          " , flg_disabilita_stampa_avviso" +
          " , macro_area" +
          " , tipo_servizio" +
          " , motivo_riscossione" +
          " , cod_tassonomico" +
          " ) values (" +
          "   nextval('mygov_ente_tipo_dovuto_mygov_ente_tipo_dovuto_id_seq')" +
          " , :d.mygovEnteId.mygovEnteId" +
          " , :d.codTipo" +
          " , :d.deTipo" +
          " , :d.ibanAccreditoPi" +
          " , :d.bicAccreditoPi" +
          " , :d.ibanAppoggioPi" +
          " , :d.bicAppoggioPi" +
          " , :d.ibanAccreditoPsp" +
          " , :d.bicAccreditoPsp" +
          " , :d.ibanAppoggioPsp" +
          " , :d.bicAppoggioPsp" +
          " , :d.codContoCorrentePostale" +
          " , :d.codXsdCausale" +
          " , :d.bicAccreditoPiSeller" +
          " , :d.bicAccreditoPspSeller" +
          " , :d.spontaneo" +
          " , :d.importo" +
          " , :d.deUrlPagamentoDovuto" +
          " , :d.deBilancioDefault" +
          " , :d.flgCfAnonimo" +
          " , :d.flgScadenzaObbligatoria" +
          " , :d.flgStampaDataScadenza" +
          " , :d.deIntestatarioCcPostale" +
          " , :d.deSettoreEnte" +
          " , :d.flgNotificaIo" +
          " , :d.flgNotificaEsitoPush" +
          " , :d.maxTentativiInoltroEsito" +
          " , :d.mygovEnteSilId" +
          " , :d.flgAttivo" +
          " , :d.codiceContestoPagamento" +
          " , :d.flgDisabilitaStampaAvviso" +
          " , :d.macroArea" +
          " , :d.tipoServizio" +
          " , :d.motivoRiscossione" +
          " , :d.codTassonomico)")
  @GetGeneratedKeys("mygov_ente_tipo_dovuto_id")
  long insert(@BindBean("d") EnteTipoDovuto d);

  @SqlUpdate(
      "update mygov_ente_tipo_dovuto set " +
          "   mygov_ente_id = :d.mygovEnteId.mygovEnteId" +
          " , cod_tipo = :d.codTipo" +
          " , de_tipo = :d.deTipo" +
          " , iban_accredito_pi = :d.ibanAccreditoPi" +
          " , bic_accredito_pi = :d.bicAccreditoPi" +
          " , iban_appoggio_pi = :d.ibanAppoggioPi" +
          " , bic_appoggio_pi = :d.bicAppoggioPi" +
          " , iban_accredito_psp = :d.ibanAccreditoPsp" +
          " , bic_accredito_psp = :d.bicAccreditoPsp" +
          " , iban_appoggio_psp = :d.ibanAppoggioPsp" +
          " , bic_appoggio_psp = :d.bicAppoggioPsp" +
          " , cod_conto_corrente_postale = :d.codContoCorrentePostale" +
          " , cod_xsd_causale = :d.codXsdCausale" +
          " , bic_accredito_pi_seller = :d.bicAccreditoPiSeller" +
          " , bic_accredito_psp_seller = :d.bicAccreditoPspSeller" +
          " , spontaneo = :d.spontaneo" +
          " , importo = :d.importo" +
          " , de_url_pagamento_dovuto = :d.deUrlPagamentoDovuto" +
          " , de_bilancio_default = :d.deBilancioDefault" +
          " , flg_cf_anonimo = :d.flgCfAnonimo" +
          " , flg_scadenza_obbligatoria = :d.flgScadenzaObbligatoria" +
          " , flg_stampa_data_scadenza = :d.flgStampaDataScadenza" +
          " , de_intestatario_cc_postale = :d.deIntestatarioCcPostale" +
          " , de_settore_ente = :d.deSettoreEnte" +
          " , flg_notifica_io = :d.flgNotificaIo" +
          " , flg_notifica_esito_push = :d.flgNotificaEsitoPush" +
          " , max_tentativi_inoltro_esito = :d.maxTentativiInoltroEsito" +
          " , mygov_ente_sil_id = :d.mygovEnteSilId" +
          " , flg_attivo = :d.flgAttivo" +
          " , codice_contesto_pagamento = :d.codiceContestoPagamento" +
          " , flg_disabilita_stampa_avviso = :d.flgDisabilitaStampaAvviso" +
          " , macro_area = :d.macroArea" +
          " , tipo_servizio = :d.tipoServizio" +
          " , motivo_riscossione = :d.motivoRiscossione" +
          " , cod_tassonomico = :d.codTassonomico" +
          " where mygov_ente_tipo_dovuto_id = :d.mygovEnteTipoDovutoId")
  int update(@BindBean("d") EnteTipoDovuto d);

  @SqlUpdate(
      "delete from mygov_ente_tipo_dovuto where mygov_ente_tipo_dovuto_id = :mygovEnteTipoDovutoId")
  int delete(Long mygovEnteTipoDovutoId);

  String SQL_SEARCH_BY_TIPO_FLG_ATTIVO =
      "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
      "  join mygov_ente " + Ente.ALIAS +
      "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
      " where ("+ EnteTipoDovuto.ALIAS+".cod_tipo ilike '%' || :codTipo || '%' or :codTipo is null) " +
      "   and ("+ EnteTipoDovuto.ALIAS+".de_tipo ilike '%' || :deTipo  || '%' or :deTipo is null) " +
      "   and ("+ EnteTipoDovuto.ALIAS+".flg_attivo = :flgAttivo "+" or :flgAttivo is null) " +
      " group by "+EnteTipoDovuto.ALIAS + ".cod_tipo, "+EnteTipoDovuto.ALIAS + ".de_tipo ";

  @SqlQuery(
      "    select " +
      "  null as mygov_ente_tipo_dovuto_id" +
      " ,null as mygov_ente_id" +
      " ,null as cod_ipa_ente" +
      " ,null as de_nome_ente" +
      " ,null as de_logo_ente" +
      " ,"+EnteTipoDovuto.ALIAS+".cod_tipo" +
      " ,"+EnteTipoDovuto.ALIAS+".de_tipo" +
      " ,null as de_url_pagamento_dovuto" +
      " ,null as flg_cf_anonimo" +
      " ,null as flg_scadenza_obbligatoria" +
      " ,null as flg_attivo" +
      " ,null as importo" +
      " ,null as dt_ultima_abilitazione" +
      " ,null as dt_ultima_disabilitazione" +
      SQL_SEARCH_BY_TIPO_FLG_ATTIVO +
      " order by "+EnteTipoDovuto.ALIAS + ".cod_tipo, "+EnteTipoDovuto.ALIAS + ".de_tipo " +
      " limit <queryLimit>")
  List<EnteTipoDovutoTo> searchByTipoFlgAttivo(String codTipo, String deTipo, Boolean flgAttivo, @Define int queryLimit);

  @SqlQuery(
      "    select " +
      "  count(1) " +
      SQL_SEARCH_BY_TIPO_FLG_ATTIVO)
  int searchByTipoFlgAttivoCount(String codTipo, String deTipo, Boolean flgAttivo);

  @SqlQuery(
      "    select " +
          "  "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id" +
          " ,"+Ente.ALIAS+".mygov_ente_id" +
          " ,"+Ente.ALIAS+".cod_ipa_ente" +
          " ,"+Ente.ALIAS+".de_nome_ente" +
          " ,"+Ente.ALIAS+".de_logo_ente" +
          " ,"+EnteTipoDovuto.ALIAS+".cod_tipo" +
          " ,"+EnteTipoDovuto.ALIAS+".de_tipo" +
          " ,"+EnteTipoDovuto.ALIAS+".de_url_pagamento_dovuto" +
          " ,"+EnteTipoDovuto.ALIAS+".flg_cf_anonimo" +
          " ,"+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria" +
          " ,"+EnteTipoDovuto.ALIAS+".flg_attivo" +
          " ,"+EnteTipoDovuto.ALIAS+".importo" +
          " ,null as dt_ultima_abilitazione" +
          " ,null as dt_ultima_disabilitazione" +
          "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+ EnteTipoDovuto.ALIAS+".cod_tipo = :codTipo" +
          "   and ("+ Ente.ALIAS+".cod_ipa_ente ilike '%' || :codIpaEnte || '%' or :codIpaEnte is null) " +
          "   and ("+ Ente.ALIAS+".de_nome_ente ilike '%' || :deNomeEnte || '%' or :deNomeEnte is null) " +
          "   and ("+ EnteTipoDovuto.ALIAS+".flg_attivo = :flgAttivo "+" or :flgAttivo is null) " +
          " order by "+Ente.ALIAS+".cod_ipa_ente, "+Ente.ALIAS+".de_nome_ente")
  List<EnteTipoDovutoTo> searchByTipoEnteFlgAttivo(String codTipo, String codIpaEnte, String deNomeEnte, Boolean flgAttivo);

  @SqlQuery(
      "    select " +
          "  "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id" +
          " ,"+Ente.ALIAS+".mygov_ente_id" +
          " ,"+Ente.ALIAS+".cod_ipa_ente" +
          " ,"+Ente.ALIAS+".de_nome_ente" +
          " ,(case when :withLogo then "+Ente.ALIAS+".de_logo_ente else null end) as de_logo_ente" +
          " ,"+EnteTipoDovuto.ALIAS+".cod_tipo" +
          " ,"+EnteTipoDovuto.ALIAS+".de_tipo" +
          " ,"+EnteTipoDovuto.ALIAS+".de_url_pagamento_dovuto" +
          " ,"+EnteTipoDovuto.ALIAS+".flg_cf_anonimo" +
          " ,"+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria" +
          " ,"+EnteTipoDovuto.ALIAS+".flg_attivo" +
          " ,"+EnteTipoDovuto.ALIAS+".importo" +
          " ,(case when :withActivationInfo then (select max("+ RegistroOperazione.ALIAS+".dt_operazione)" +
          "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
          "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'ENTE_TIP_DOV'"+
          "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+Ente.ALIAS+".cod_ipa_ente || '|' || "+EnteTipoDovuto.ALIAS+".cod_tipo"+
          "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = true) else null end) as dt_ultima_abilitazione"+
          " ,(case when :withActivationInfo then (select max("+ RegistroOperazione.ALIAS+".dt_operazione)" +
          "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
          "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'ENTE_TIP_DOV'"+
          "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+Ente.ALIAS+".cod_ipa_ente || '|' || "+EnteTipoDovuto.ALIAS+".cod_tipo"+
          "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = false) else null end) as dt_ultima_disabilitazione"+
          "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId " +
          "   and ("+ EnteTipoDovuto.ALIAS+".cod_tipo ilike '%' || :codTipo || '%' or :codTipo is null) " +
          "   and ("+ EnteTipoDovuto.ALIAS+".de_tipo ilike '%' || :deTipo  || '%' or :deTipo is null) " +
          "   and ("+ EnteTipoDovuto.ALIAS+".flg_attivo = :flgAttivo "+" or :flgAttivo is null) " +
          " order by "+EnteTipoDovuto.ALIAS+".cod_tipo, "+EnteTipoDovuto.ALIAS+".de_tipo")
  List<EnteTipoDovutoTo> searchByEnteTipoFlgAttivo(Long mygovEnteId, String codTipo, String deTipo, Boolean flgAttivo, Boolean withActivationInfo, Boolean withLogo);

  @SqlQuery(
    "    select " +
      "  "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id" +
      " ,"+Ente.ALIAS+".mygov_ente_id" +
      " ,"+Ente.ALIAS+".cod_ipa_ente" +
      " ,"+Ente.ALIAS+".de_nome_ente" +
      " ,(case when :withLogo then "+Ente.ALIAS+".de_logo_ente else null end) as de_logo_ente" +
      " ,"+EnteTipoDovuto.ALIAS+".cod_tipo" +
      " ,"+EnteTipoDovuto.ALIAS+".de_tipo" +
      " ,"+EnteTipoDovuto.ALIAS+".de_url_pagamento_dovuto" +
      " ,"+EnteTipoDovuto.ALIAS+".flg_cf_anonimo" +
      " ,"+EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria" +
      " ,"+EnteTipoDovuto.ALIAS+".flg_attivo" +
      " ,"+EnteTipoDovuto.ALIAS+".importo" +
      " ,(case when :withActivationInfo then (select max("+ RegistroOperazione.ALIAS+".dt_operazione)" +
      "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
      "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'ENTE_TIP_DOV'"+
      "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+Ente.ALIAS+".cod_ipa_ente || '|' || "+EnteTipoDovuto.ALIAS+".cod_tipo"+
      "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = true) else null end) as dt_ultima_abilitazione"+
      " ,(case when :withActivationInfo then (select max("+ RegistroOperazione.ALIAS+".dt_operazione)" +
      "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
      "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'ENTE_TIP_DOV'"+
      "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+Ente.ALIAS+".cod_ipa_ente || '|' || "+EnteTipoDovuto.ALIAS+".cod_tipo"+
      "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = false) else null end) as dt_ultima_disabilitazione"+
      "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
      "  join mygov_ente " + Ente.ALIAS +
      "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
      " where "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId " +
      "   and "+ EnteTipoDovuto.ALIAS+".cod_tipo = :codTipo " +
      "   and ("+ EnteTipoDovuto.ALIAS+".flg_attivo = :flgAttivo "+" or :flgAttivo is null) " +
      " order by "+EnteTipoDovuto.ALIAS+".cod_tipo, "+EnteTipoDovuto.ALIAS+".de_tipo")
  Optional<EnteTipoDovutoTo> getByEnteTipoFlgAttivo(Long mygovEnteId, String codTipo, Boolean flgAttivo, Boolean withActivationInfo, Boolean withLogo);


  @SqlQuery(
      "    select " + EnteTipoDovuto.ALIAS + ALL_FIELDS +", " + Ente.FIELDS_WITHOUT_LOGO +
          "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId " +
          "   and ("+ EnteTipoDovuto.ALIAS+".spontaneo = :spontaneo "+" or :spontaneo is null) " +
          "   and ("+ EnteTipoDovuto.ALIAS+".flg_scadenza_obbligatoria = :flgScadenzaObbligatoria "+" or :flgScadenzaObbligatoria  is null) " +
          "   and "+EnteTipoDovuto.ALIAS+".flg_attivo = true " +
          " order by " + EnteTipoDovuto.ALIAS + ".de_tipo")
  @RegisterFieldMapper(EnteTipoDovuto.class)
  List<EnteTipoDovuto> getAttiviByMygovEnteIdAndFlags(Long mygovEnteId, Boolean spontaneo, Boolean flgScadenzaObbligatoria);

  @SqlQuery(
      "    select " + EnteTipoDovuto.ALIAS + ALL_FIELDS +", " + Ente.FIELDS_WITHOUT_LOGO +
          "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = :id")
  @RegisterFieldMapper(EnteTipoDovuto.class)
  @Caching(
      put = {
        @CachePut(value = CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'id',#result.mygovEnteTipoDovutoId}", condition="#result!=null"),
        @CachePut(value = CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'codTipo',#result.codTipo, #result.mygovEnteId.codIpaEnte}", condition="#result!=null"),
        @CachePut(value = CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'codTipoIdEnte',#result.codTipo, #result.mygovEnteId.mygovEnteId}", condition="#result!=null")
      }
  )
  EnteTipoDovuto getById(Long id);

  @SqlQuery(
    "  select " + EnteTipoDovuto.ALIAS + ALL_FIELDS +", " + Ente.FIELDS_WITHOUT_LOGO +
      "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
      "  join mygov_ente " + Ente.ALIAS +
      "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
      " where "+ EnteTipoDovuto.ALIAS+".cod_tipo = :codTipo " +
      "   and "+ EnteTipoDovuto.ALIAS+".mygov_ente_id = :mygovEnteId " )
  @RegisterFieldMapper(EnteTipoDovuto.class)
  @Caching(
    put = {
      @CachePut(value = CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'id',#result.mygovEnteTipoDovutoId}", condition="#result!=null"),
      @CachePut(value = CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'codTipo',#result.codTipo, #result.mygovEnteId.codIpaEnte}", condition="#result!=null"),
      @CachePut(value = CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'codTipoIdEnte',#result.codTipo, #result.mygovEnteId.mygovEnteId}", condition="#result!=null")
    }
  )
  EnteTipoDovuto getByCodTipoIdEnte(String codTipo, Long mygovEnteId);

  @SqlQuery(
      "    select " + EnteTipoDovuto.ALIAS + ALL_FIELDS +", " + Ente.FIELDS_WITHOUT_LOGO +
          "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+ EnteTipoDovuto.ALIAS+".cod_tipo = :codTipo " +
          "   and "+ Ente.ALIAS+".cod_ipa_ente = :codIpaEnte " )
  @RegisterFieldMapper(EnteTipoDovuto.class)
  @Caching(
      put = {
        @CachePut(value = CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'id',#result.mygovEnteTipoDovutoId}", condition="#result!=null"),
        @CachePut(value = CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'codTipo',#result.codTipo, #result.mygovEnteId.codIpaEnte}", condition="#result!=null"),
        @CachePut(value = CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'codTipoIdEnte',#result.codTipo, #result.mygovEnteId.mygovEnteId}", condition="#result!=null")
      }
  )
  EnteTipoDovuto getByCodTipo(String codTipo, String codIpaEnte);

  @SqlQuery(
      "    select " + EnteTipoDovuto.ALIAS + ALL_FIELDS +", " + Ente.FIELDS_WITHOUT_LOGO +
          "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+ EnteTipoDovuto.ALIAS+".mygov_ente_id = :mygovEnteId" +
          " order by " + EnteTipoDovuto.ALIAS + ".mygov_ente_tipo_dovuto_id")
  @RegisterFieldMapper(EnteTipoDovuto.class)
  List<EnteTipoDovuto> getAllByEnte(long mygovEnteId);

  @SqlQuery(
      "    select " + EnteTipoDovuto.ALIAS + ALL_FIELDS +", " + Ente.FIELDS_WITHOUT_LOGO +
          "  from mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "  join mygov_operatore "+ Operatore.ALIAS +
          "    on "+Operatore.ALIAS+".cod_ipa_ente = "+Ente.ALIAS+".cod_ipa_ente" +
          "  join mygov_utente " + Utente.ALIAS +
          "    on "+Utente.ALIAS+".cod_fed_user_id  = "+Operatore.ALIAS+".cod_fed_user_id " +
          "  join mygov_operatore_ente_tipo_dovuto " + OperatoreEnteTipoDovuto.ALIAS +
          "    on "+OperatoreEnteTipoDovuto.ALIAS+".mygov_operatore_id = "+Operatore.ALIAS+".mygov_operatore_id " +
          "   and "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id " +
          " where Utente.cod_fed_user_id = :operatoreUsername " +
          "   and "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId " +
          "   and "+EnteTipoDovuto.ALIAS+".flg_attivo = true " +
          "   and "+OperatoreEnteTipoDovuto.ALIAS+".flg_attivo = true " +
          " order by " + EnteTipoDovuto.ALIAS + ".de_tipo")
  @RegisterFieldMapper(EnteTipoDovuto.class)
  List<EnteTipoDovuto> getByMygovEnteIdAndOperatoreUsername(Long mygovEnteId, String operatoreUsername);

  @SqlQuery(
      "select count("+Dovuto.ALIAS+".*) = 0" +
          " from mygov_dovuto "+Dovuto.ALIAS +
          " inner join mygov_flusso "+Flusso.ALIAS+" on "+Flusso.ALIAS+".mygov_flusso_id = "+Dovuto.ALIAS+".mygov_flusso_id " +
          " inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Flusso.ALIAS+".mygov_ente_id " +
          " where "+Dovuto.ALIAS+".cod_tipo_dovuto = :codTipo" +
          "   and "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte" +
          "   and "+Dovuto.ALIAS+".dt_rp_dati_vers_data_esecuzione_pagamento is null"
  )
  boolean dataScadenzaObbligatoriaEnable(String codIpaEnte, String codTipo);
}
