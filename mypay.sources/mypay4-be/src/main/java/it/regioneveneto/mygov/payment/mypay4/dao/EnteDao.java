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

import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static it.regioneveneto.mygov.payment.mypay4.util.Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA;
import static it.regioneveneto.mygov.payment.mypay4.util.Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA;
import static it.regioneveneto.mygov.payment.mypay4.util.Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA;

public interface EnteDao extends BaseDao {

  @SqlUpdate(
      "update mygov_ente set" +
          "   cod_ipa_ente = :d.codIpaEnte" +
          " , codice_fiscale_ente = :d.codiceFiscaleEnte" +
          " , de_nome_ente = :d.deNomeEnte" +
          " , email_amministratore = :d.emailAmministratore" +
          //" , dt_creazione = :d.dtCreazione" +
          //" , dt_ultima_modifica = :d.dtUltimaModifica" +
          " , dt_ultima_modifica = now()" +
          " , cod_rp_dati_vers_tipo_versamento = :d.codRpDatiVersTipoVersamento" +
          " , num_rp_dati_vers_dati_sing_vers_commissione_carico_pa = :d.numRpDatiVersDatiSingVersCommissioneCaricoPa" +
          " , cod_rp_dati_vers_dati_sing_vers_iban_accredito = :d.codRpDatiVersDatiSingVersIbanAccredito" +
          " , cod_rp_dati_vers_dati_sing_vers_bic_accredito = :d.codRpDatiVersDatiSingVersBicAccredito" +
          " , cod_rp_dati_vers_dati_sing_vers_iban_appoggio = :d.codRpDatiVersDatiSingVersIbanAppoggio" +
          " , cod_rp_dati_vers_dati_sing_vers_bic_appoggio = :d.codRpDatiVersDatiSingVersBicAppoggio" +
          " , mybox_client_key = :d.myboxClientKey" +
          " , mybox_client_secret = :d.myboxClientSecret" +
          " , ente_sil_invia_risposta_pagamento_url = :d.enteSilInviaRispostaPagamentoUrl" +
          " , cod_global_location_number = :d.codGlobalLocationNumber" +
          " , de_password = :d.dePassword" +
          " , cod_rp_dati_vers_dati_sing_vers_bic_accredito_seller = :d.codRpDatiVersDatiSingVersBicAccreditoSeller" +
          " , de_rp_ente_benef_denominazione_beneficiario = :d.deRpEnteBenefDenominazioneBeneficiario" +
          " , de_rp_ente_benef_indirizzo_beneficiario = :d.deRpEnteBenefIndirizzoBeneficiario" +
          " , de_rp_ente_benef_civico_beneficiario = :d.deRpEnteBenefCivicoBeneficiario" +
          " , cod_rp_ente_benef_cap_beneficiario = :d.codRpEnteBenefCapBeneficiario" +
          " , de_rp_ente_benef_localita_beneficiario = :d.deRpEnteBenefLocalitaBeneficiario" +
          " , de_rp_ente_benef_provincia_beneficiario = :d.deRpEnteBenefProvinciaBeneficiario" +
          " , cod_rp_ente_benef_nazione_beneficiario = :d.codRpEnteBenefNazioneBeneficiario" +
          " , de_rp_ente_benef_telefono_beneficiario = :d.deRpEnteBenefTelefonoBeneficiario" +
          " , de_rp_ente_benef_sito_web_beneficiario = :d.deRpEnteBenefSitoWebBeneficiario" +
          " , de_rp_ente_benef_email_beneficiario = :d.deRpEnteBenefEmailBeneficiario" +
          " , application_code = :d.applicationCode" +
          " , cod_codice_interbancario_cbill = :d.codCodiceInterbancarioCbill" +
          " , de_informazioni_ente = :d.deInformazioniEnte" +
          " , de_logo_ente = :d.deLogoEnte" +
          " , de_autorizzazione = :d.deAutorizzazione" +
          " , cd_stato_ente = :d.cdStatoEnte.mygovAnagraficaStatoId" +
          " , de_url_esterni_attiva = :d.deUrlEsterniAttiva" +
          " , lingua_aggiuntiva = :d.linguaAggiuntiva" +
          " , cod_tipo_ente = :d.codTipoEnte" +
          " , dt_avvio = :d.dtAvvio" +
          " where mygov_ente_id = :d.mygovEnteId"
  )
  int update(@BindBean("d") Ente d);

  @SqlUpdate(
    "update mygov_ente set" +
      " de_logo_ente = :logo" +
      " where mygov_ente_id = :mygovEnteId"
  )
  int updateLogoEnte(Long mygovEnteId, String logo);

  @SqlUpdate(
      "insert into mygov_ente (" +
          "   mygov_ente_id" +
          " , cod_ipa_ente" +
          " , codice_fiscale_ente" +
          " , de_nome_ente" +
          " , email_amministratore" +
          " , dt_creazione" +
          " , dt_ultima_modifica" +
          " , cod_rp_dati_vers_tipo_versamento" +
          " , num_rp_dati_vers_dati_sing_vers_commissione_carico_pa" +
          " , cod_rp_dati_vers_dati_sing_vers_iban_accredito" +
          " , cod_rp_dati_vers_dati_sing_vers_bic_accredito" +
          " , cod_rp_dati_vers_dati_sing_vers_iban_appoggio" +
          " , cod_rp_dati_vers_dati_sing_vers_bic_appoggio" +
          " , mybox_client_key" +
          " , mybox_client_secret" +
          " , ente_sil_invia_risposta_pagamento_url" +
          " , cod_global_location_number" +
          " , de_password" +
          " , cod_rp_dati_vers_dati_sing_vers_bic_accredito_seller" +
          " , de_rp_ente_benef_denominazione_beneficiario" +
          " , de_rp_ente_benef_indirizzo_beneficiario" +
          " , de_rp_ente_benef_civico_beneficiario" +
          " , cod_rp_ente_benef_cap_beneficiario" +
          " , de_rp_ente_benef_localita_beneficiario" +
          " , de_rp_ente_benef_provincia_beneficiario" +
          " , cod_rp_ente_benef_nazione_beneficiario" +
          " , de_rp_ente_benef_telefono_beneficiario" +
          " , de_rp_ente_benef_sito_web_beneficiario" +
          " , de_rp_ente_benef_email_beneficiario" +
          " , application_code" +
          " , cod_codice_interbancario_cbill" +
          " , de_informazioni_ente" +
          " , de_logo_ente" +
          " , de_autorizzazione" +
          " , cd_stato_ente" +
          " , de_url_esterni_attiva" +
          " , lingua_aggiuntiva" +
          " , cod_tipo_ente" +
          " , dt_avvio" +
          ") values (" +
          "   nextval('mygov_ente_mygov_ente_id_seq')" +
          " , :d.codIpaEnte" +
          " , :d.codiceFiscaleEnte" +
          " , :d.deNomeEnte" +
          " , :d.emailAmministratore" +
          " , coalesce(:d.dtCreazione, now())" +
          " , coalesce(:d.dtUltimaModifica, now())" +
          " , :d.codRpDatiVersTipoVersamento" +
          " , :d.numRpDatiVersDatiSingVersCommissioneCaricoPa" +
          " , :d.codRpDatiVersDatiSingVersIbanAccredito" +
          " , :d.codRpDatiVersDatiSingVersBicAccredito" +
          " , :d.codRpDatiVersDatiSingVersIbanAppoggio" +
          " , :d.codRpDatiVersDatiSingVersBicAppoggio" +
          " , :d.myboxClientKey" +
          " , :d.myboxClientSecret" +
          " , :d.enteSilInviaRispostaPagamentoUrl" +
          " , :d.codGlobalLocationNumber" +
          " , :d.dePassword" +
          " , :d.codRpDatiVersDatiSingVersBicAccreditoSeller" +
          " , :d.deRpEnteBenefDenominazioneBeneficiario" +
          " , :d.deRpEnteBenefIndirizzoBeneficiario" +
          " , :d.deRpEnteBenefCivicoBeneficiario" +
          " , :d.codRpEnteBenefCapBeneficiario" +
          " , :d.deRpEnteBenefLocalitaBeneficiario" +
          " , :d.deRpEnteBenefProvinciaBeneficiario" +
          " , :d.codRpEnteBenefNazioneBeneficiario" +
          " , :d.deRpEnteBenefTelefonoBeneficiario" +
          " , :d.deRpEnteBenefSitoWebBeneficiario" +
          " , :d.deRpEnteBenefEmailBeneficiario" +
          " , :d.applicationCode" +
          " , :d.codCodiceInterbancarioCbill" +
          " , :d.deInformazioniEnte" +
          " , :d.deLogoEnte" +
          " , :d.deAutorizzazione" +
          " , :d.cdStatoEnte.mygovAnagraficaStatoId" +
          " , :d.deUrlEsterniAttiva" +
          " , :d.linguaAggiuntiva" +
          " , :d.codTipoEnte" +
          " , :d.dtAvvio)"
  )
  @GetGeneratedKeys("mygov_ente_id")
  long insert(@BindBean("d") Ente d);

  @SqlQuery(
    "    select " + Ente.ALIAS + ALL_FIELDS +
      "  from mygov_ente " + Ente.ALIAS +
      " order by " + Ente.ALIAS + ".cod_ipa_ente")
  @RegisterFieldMapper(Ente.class)
  List<Ente> getFullTable();

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +", " + AnagraficaStato.FIELDS +
          "  from mygov_ente " + Ente.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+Ente.ALIAS+".cd_stato_ente = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_ente_funzionalita "+ EnteFunzionalita.ALIAS +
          "    on "+Ente.ALIAS+".cod_ipa_ente = "+EnteFunzionalita.ALIAS+".cod_ipa_ente" +
          "   and "+EnteFunzionalita.ALIAS+".cod_funzionalita = '"+Constants.FUNZIONALITA_ENTE_PUBBLICO+"'" +
          " where "+AnagraficaStato.ALIAS+".cod_stato = '" + AnagraficaStato.STATO_ENTE_ESERCIZIO + "' " +
          "   and "+EnteFunzionalita.ALIAS+".flg_attivo = true" +
          " order by " + Ente.ALIAS + ".de_nome_ente")
  @RegisterFieldMapper(Ente.class)
  List<Ente> getAllEnti();

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +", " + AnagraficaStato.FIELDS +
          "  from mygov_ente " + Ente.ALIAS +
          "  join mygov_anagrafica_stato "+AnagraficaStato.ALIAS +
          "    on "+Ente.ALIAS+".cd_stato_ente = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_ente_funzionalita "+ EnteFunzionalita.ALIAS +
          "    on "+Ente.ALIAS+".cod_ipa_ente = "+EnteFunzionalita.ALIAS+".cod_ipa_ente" +
          "   and "+EnteFunzionalita.ALIAS+".cod_funzionalita = '"+Constants.FUNZIONALITA_ENTE_PUBBLICO+"'" +
          "  join mygov_ente_funzionalita "+ EnteFunzionalita.ALIAS+"_2" +
          "    on "+Ente.ALIAS+".cod_ipa_ente = "+EnteFunzionalita.ALIAS+"_2.cod_ipa_ente" +
          "   and "+EnteFunzionalita.ALIAS+"_2.cod_funzionalita = '"+Constants.FUNZIONALITA_PAGAMENTO_SPONTANEO+"'" +
          " where "+AnagraficaStato.ALIAS+".cod_stato = '" + AnagraficaStato.STATO_ENTE_ESERCIZIO + "' " +
          "   and "+EnteFunzionalita.ALIAS+".flg_attivo = true" +
          "   and "+EnteFunzionalita.ALIAS+"_2.flg_attivo = true" +
          "   and exists ( select 1 from mygov_ente_tipo_dovuto "+EnteTipoDovuto.ALIAS+" where "+EnteTipoDovuto.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          "       and "+EnteTipoDovuto.ALIAS+".spontaneo = true  and "+EnteTipoDovuto.ALIAS+".flg_attivo = true )" +
          " order by " + Ente.ALIAS + ".de_nome_ente")
  @RegisterFieldMapper(Ente.class)
  List<Ente> getAllEntiSpontanei();

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +", " + AnagraficaStato.FIELDS +
          "  from mygov_ente " + Ente.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+Ente.ALIAS+".cd_stato_ente = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          " where "+Ente.ALIAS+".mygov_ente_id = :id ")
  @RegisterFieldMapper(Ente.class)
  @Caching(
      put = {
          @CachePut(value = CacheService.CACHE_NAME_ENTE, key = "{'id',#result.mygovEnteId}", condition="#result!=null"),
          @CachePut(value = CacheService.CACHE_NAME_ENTE, key = "{'codIpa',#result.codIpaEnte}", condition="#result!=null"),
          @CachePut(value = CacheService.CACHE_NAME_ENTE, key = "{'codFiscale',#result.codiceFiscaleEnte}", condition="#result!=null")
      }
  )
  Ente getEnteById(Long id);

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +", " + AnagraficaStato.FIELDS +
          "  from mygov_ente " + Ente.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+Ente.ALIAS+".cd_stato_ente = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          " where "+Ente.ALIAS+".cod_ipa_ente = :codIpa ")
  @RegisterFieldMapper(Ente.class)
  @Caching(
      put = {
          @CachePut(value = CacheService.CACHE_NAME_ENTE, key = "{'id',#result.mygovEnteId}", condition="#result!=null"),
          @CachePut(value = CacheService.CACHE_NAME_ENTE, key = "{'codIpa',#result.codIpaEnte}", condition="#result!=null"),
          @CachePut(value = CacheService.CACHE_NAME_ENTE, key = "{'codFiscale',#result.codiceFiscaleEnte}", condition="#result!=null")
      }
  )
  Ente getEnteByCodIpa(String codIpa);

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +", " + AnagraficaStato.FIELDS +
          "  from mygov_ente " + Ente.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+Ente.ALIAS+".cd_stato_ente = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          " where "+Ente.ALIAS+".codice_fiscale_ente = :codFiscale ")
  @RegisterFieldMapper(Ente.class)
  @Caching(
      put = {
          @CachePut(value = CacheService.CACHE_NAME_ENTE, key = "{'id',#result.mygovEnteId}", condition="#result!=null"),
          @CachePut(value = CacheService.CACHE_NAME_ENTE, key = "{'codIpa',#result.codIpaEnte}", condition="#result!=null"),
          @CachePut(value = CacheService.CACHE_NAME_ENTE, key = "{'codFiscale',#result.codiceFiscaleEnte}", condition="#result!=null")
      }
  )
  Ente getEnteByCodFiscale(String codFiscale);

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +", " + AnagraficaStato.FIELDS +
        "  from mygov_ente " + Ente.ALIAS +
        "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
        "    on "+Ente.ALIAS+".cd_stato_ente = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
        "  join mygov_operatore Operatore " +
        "    on Operatore.cod_ipa_ente = "+Ente.ALIAS+".cod_ipa_ente" +
        "  join mygov_utente Utente " +
        "    on Utente.cod_fed_user_id  = Operatore.cod_fed_user_id " +
        " where Utente.cod_fed_user_id = :operatoreUsername " +
        "   and "+AnagraficaStato.ALIAS+".cod_stato in ('" + AnagraficaStato.STATO_ENTE_ESERCIZIO + "','" + AnagraficaStato.STATO_ENTE_PRE_ESERCIZIO +"') " +
        " order by " + Ente.ALIAS + ".de_nome_ente")
  @RegisterFieldMapper(Ente.class)
  List<Ente> getEntiByOperatoreUsername(String operatoreUsername);

  @SqlQuery(
      "    select " + Ente.ALIAS + ALL_FIELDS +", " + AnagraficaStato.FIELDS +
          "  from mygov_ente " + Ente.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+Ente.ALIAS+".cd_stato_ente = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          " where ("+Ente.ALIAS+".cod_ipa_ente ilike '%' || :codIpaEnte || '%' or coalesce(:codIpaEnte, '') = '')" +
          "   and ("+Ente.ALIAS+".de_nome_ente ilike '%' || :deNome || '%' or coalesce(:deNome, '') = '')" +
          "   and ("+Ente.ALIAS+".codice_fiscale_ente ilike '%' || :codFiscale || '%' or coalesce(:codFiscale, '') = '')" +
          "   and ("+Ente.ALIAS+".cd_stato_ente = :idStato or :idStato is null)" +
          "   and ("+Ente.ALIAS+".dt_avvio between coalesce(:dtAvvioFrom,cast('1970-01-01' as DATE)) and coalesce(:dtAvvioTo,cast('2099-12-31' as DATE))" +
          "         or cast(:dtAvvioFrom as DATE) is null and cast(:dtAvvioTo as DATE) is null)" +
          " order by " + Ente.ALIAS + ".de_nome_ente asc")
  @RegisterFieldMapper(Ente.class)
  List<Ente> searchEnti(String codIpaEnte, String deNome,String codFiscale,Long idStato,LocalDate dtAvvioFrom,LocalDate dtAvvioTo);

  @SqlQuery(
      "    select 1 " +
          "  from mygov_ente " + Ente.ALIAS +
          "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          "    on "+Ente.ALIAS+".cd_stato_ente = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
          "  join mygov_operatore Operatore " +
          "    on Operatore.cod_ipa_ente = "+Ente.ALIAS+".cod_ipa_ente" +
          "  join mygov_utente Utente " +
          "    on Utente.cod_fed_user_id  = Operatore.cod_fed_user_id " +
          " where Utente.cod_fed_user_id = :operatoreUsername " +
          "   and "+AnagraficaStato.ALIAS+".cod_stato in ('" + AnagraficaStato.STATO_ENTE_ESERCIZIO + "','" + AnagraficaStato.STATO_ENTE_PRE_ESERCIZIO +"') " +
          "   and (:mygovEnteId is null or "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId)" +
          "  limit 1")
  Optional<Boolean> isOperatoreForEnte(String operatoreUsername, Long mygovEnteId);

  @SqlQuery(
          "   select " + Ente.ALIAS + ALL_FIELDS +
                  " from mygov_ente " + Ente.ALIAS +
                  " where "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte " +
                  " and de_password IS NULL")
  @RegisterFieldMapper(Ente.class)
  List<Ente> findByCodIpaEnteAndNullPassword(String codIpaEnte);

  @SqlQuery(
          "   select " + Ente.ALIAS + ALL_FIELDS +
                  " from mygov_ente " + Ente.ALIAS +
                  " where "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte " +
                  " and "+Ente.ALIAS+".de_password = :password ")
  @RegisterFieldMapper(Ente.class)
  List<Ente> findByCodIpaEnteAndPassword(String codIpaEnte, String password);

  @SqlQuery(
      "   select coalesce(" + Ente.ALIAS + ".de_password, '')" +
          " from mygov_ente " + Ente.ALIAS +
          " where "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte ")
  @SingleValue()
  Optional<String> getPasswordByCodIpaEnte(String codIpaEnte);

  @SqlQuery(
      "select get_ente_tipo_progressivo(:codIpaEnte, :tipoGeneratore, cast(:data as date))"
  )
  Long callGetEnteTipoProgressivoFunction(String codIpaEnte, String tipoGeneratore, Date data);

  //SANP25-IMPORTFLUSSO
  @SqlQuery(
          "    select " + Ente.ALIAS + ALL_FIELDS +
                  "  from mygov_ente " + Ente.ALIAS +
                  "  join mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
                  "    on "+Ente.ALIAS+".cd_stato_ente = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
                  " where "+AnagraficaStato.ALIAS+".cod_stato != '" + Constants.STATO_ENTE_INSERITO + "' ")
  @RegisterFieldMapper(Ente.class)
  List<Ente> getAllEntiImportFlusso();


  // IMPORT MASSIVO FLUSSI GPD
  @SqlQuery(
          "select " +
                  " distinct " + Ente.ALIAS + ALL_FIELDS +
                  " from mygov_ente " + Ente.ALIAS +
                  " join mygov_dovuto_preload " + DovutoPreload.ALIAS +
                  " on " +
                  Ente.ALIAS + ".mygov_ente_id = " + DovutoPreload.ALIAS + ".mygov_ente_id " +

                  " where " +
                  DovutoPreload.ALIAS + ".nuovo_gpd_status in (" +
                  "'" + STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA +
                  "', '" + STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA +
                  "', '" + STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA +
                  "')"

  )
  @RegisterFieldMapper(Ente.class)
  List<Ente> getEntiConDovutiDaSyncConPagoPA();

}
