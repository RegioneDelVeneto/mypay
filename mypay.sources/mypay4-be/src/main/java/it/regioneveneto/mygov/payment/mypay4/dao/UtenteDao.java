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

import it.regioneveneto.mygov.payment.mypay4.dto.UtenteTo;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface UtenteDao extends BaseDao {

  @SqlQuery(
      "select "+Utente.ALIAS + ALL_FIELDS +" from mygov_utente "+ Utente.ALIAS +
          " where mygov_utente_id = :id" )
  @RegisterFieldMapper(Utente.class)
  Optional<Utente> getById(Long id);

  @SqlQuery(
      "select "+Utente.ALIAS + ALL_FIELDS +" from mygov_utente "+ Utente.ALIAS +
          " where cod_fed_user_id = :codFedUserId" )
  @RegisterFieldMapper(Utente.class)
  Optional<Utente> getByCodFedUserId(String codFedUserId);

  @SqlUpdate(" insert into mygov_utente ( "+
      " mygov_utente_id "+
      //",version "+  //when insert, default(=0) will be used
      ",cod_fed_user_id "+
      ",cod_codice_fiscale_utente "+
      ",flg_fed_authorized "+
      ",de_email_address "+
      ",de_email_address_new "+
      ",email_source_type "+
      ",de_firstname "+
      ",de_lastname "+
      ",de_fed_legal_entity "+
      ",dt_ultimo_login "+
      ",indirizzo "+
      ",civico "+
      ",cap "+
      ",comune_id "+
      ",provincia_id "+
      ",nazione_id "+
      ",dt_set_address "+
      ") values ( "+
      " nextval('mygov_utente_mygov_utente_id_seq') "+
      //",:u.version "+
      ",:u.codFedUserId "+
      ",:u.codCodiceFiscaleUtente "+
      ",:u.flgFedAuthorized "+
      ",:u.deEmailAddress "+
      ",:u.deEmailAddressNew "+
      ",:u.emailSourceType "+
      ",:u.deFirstname "+
      ",:u.deLastname "+
      ",:u.deFedLegalEntity "+
      ",:u.dtUltimoLogin "+
      ",:u.indirizzo "+
      ",:u.civico "+
      ",:u.cap "+
      ",:u.comuneId "+
      ",:u.provinciaId "+
      ",:u.nazioneId "+
      ",:u.dtSetAddress "+
      ")" )
  @GetGeneratedKeys("mygov_utente_id")
  long insert(@BindBean("u") Utente utente);

  @SqlUpdate("update mygov_utente set "+
      " version = :u.version "+
      ",cod_fed_user_id = :u.codFedUserId "+
      ",cod_codice_fiscale_utente = :u.codCodiceFiscaleUtente "+
      ",flg_fed_authorized = :u.flgFedAuthorized "+
      ",de_email_address = :u.deEmailAddress "+
      ",de_email_address_new = :u.deEmailAddressNew "+
      ",email_source_type = :u.emailSourceType "+
      ",de_firstname = :u.deFirstname "+
      ",de_lastname = :u.deLastname "+
      ",de_fed_legal_entity = :u.deFedLegalEntity "+
      ",dt_ultimo_login = :u.dtUltimoLogin "+
      ",indirizzo = :u.indirizzo "+
      ",civico = :u.civico "+
      ",cap = :u.cap "+
      ",comune_id = :u.comuneId "+
      ",provincia_id = :u.provinciaId "+
      ",nazione_id = :u.nazioneId "+
      ",dt_set_address = :u.dtSetAddress "+
      " where mygov_utente_id = :u.mygovUtenteId " )
  int update(@BindBean("u") Utente utente);

  String QUERY_SEARCH_UTENTI_OPERATORI =
      " from mygov_utente "+ Utente.ALIAS +
      " where (:codFedUserId is null or "+Utente.ALIAS+".cod_fed_user_id ilike '%'||:codFedUserId||'%') " +
      "   and (:cognome is null or "+Utente.ALIAS+".de_lastname ilike '%'||:cognome||'%') " +
      "   and (:nome is null or "+Utente.ALIAS+".de_firstname ilike '%'||:nome||'%') " +
      "   and (:onlyOperatori = false or exists (select 1 from mygov_operatore "+ Operatore.ALIAS +
      "                where "+Utente.ALIAS+".cod_fed_user_id = "+Operatore.ALIAS+".cod_fed_user_id) ) ";

  @SqlQuery(
      "select "+ Utente.ALIAS+ALL_FIELDS +
          QUERY_SEARCH_UTENTI_OPERATORI +
          " order by "+Utente.ALIAS+".cod_fed_user_id ASC "+
          " limit :queryLimit")
  @RegisterFieldMapper(Utente.class)
  List<Utente> searchUtenti(String codFedUserId, String cognome, String nome, boolean onlyOperatori, int queryLimit);

  @SqlQuery(
      "select count(1)" + QUERY_SEARCH_UTENTI_OPERATORI)
  int searchUtentiCount(String codFedUserId, String cognome, String nome, boolean onlyOperatori);

  @SqlQuery(
      "select " +
          "  "+Utente.ALIAS+".mygov_utente_id" +
          " ,"+Utente.ALIAS+".cod_fed_user_id" +
          " ,"+Utente.ALIAS+".cod_codice_fiscale_utente" +
          " ,"+Utente.ALIAS+".de_firstname" +
          " ,"+Utente.ALIAS+".de_lastname" +
          " ,"+Operatore.ALIAS+".de_email_address" +
          " ,'"+Utente.EMAIL_SOURCE_TYPE_BACKOFFICE+"' as email_source_type" +
          " ,null as de_email_address_new" +
          " ,"+Utente.ALIAS+".dt_ultimo_login" +
          " ,"+Operatore.ALIAS+".ruolo," +
          " (case when :codTipo is not null then (select max("+ RegistroOperazione.ALIAS+".dt_operazione)" +
          "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
          "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'OPER_TIP_DOV'"+
          "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+Operatore.ALIAS+".cod_fed_user_id || '|' || "+Operatore.ALIAS+".cod_ipa_ente || '|' || :codTipo"+
          "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = true) else null end) as dt_ultima_abilitazione,"+
          " (case when :codTipo is not null then (select max("+ RegistroOperazione.ALIAS+".dt_operazione)" +
          "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
          "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'OPER_TIP_DOV'"+
          "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+Operatore.ALIAS+".cod_fed_user_id || '|' || "+Operatore.ALIAS+".cod_ipa_ente || '|' || :codTipo"+
          "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = false) else null end) as dt_ultima_disabilitazione"+
          " from mygov_utente "+Utente.ALIAS +
          " inner join mygov_operatore "+Operatore.ALIAS +
          "   on "+Operatore.ALIAS+".cod_fed_user_id = "+Utente.ALIAS+".cod_fed_user_id" +
          " inner join mygov_ente "+ Ente.ALIAS +
          "   on "+Ente.ALIAS+".cod_ipa_ente = "+Operatore.ALIAS+".cod_ipa_ente" +
          " where "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId" +
          "   and (:codFedUserId is null or "+Utente.ALIAS+".cod_fed_user_id ilike '%'||:codFedUserId||'%') " +
          "   and (:cognome is null or "+Utente.ALIAS+".de_lastname ilike '%'||:cognome||'%') " +
          "   and (:nome is null or "+Utente.ALIAS+".de_firstname ilike '%'||:nome||'%') " +
          " order by "+Utente.ALIAS+".cod_fed_user_id ASC "
  )
  List<UtenteTo> searchOperatoriForEnte(Long mygovEnteId, String codFedUserId, String cognome, String nome, String codTipo);

  @SqlQuery(
      "select "+Utente.ALIAS+ALL_FIELDS +
          " from mygov_operatore_ente_tipo_dovuto "+ OperatoreEnteTipoDovuto.ALIAS +
          " inner join mygov_ente_tipo_dovuto "+ EnteTipoDovuto.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id" +
          " inner join mygov_operatore "+Operatore.ALIAS +
          "   on "+Operatore.ALIAS+".mygov_operatore_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_operatore_id" +
          " inner join mygov_utente "+Utente.ALIAS +
          "   on "+Utente.ALIAS+".cod_fed_user_id = "+Operatore.ALIAS+".cod_fed_user_id" +
          " where "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = :mygovEnteTipoDovutoId" +
          "   and "+OperatoreEnteTipoDovuto.ALIAS+".flg_attivo = true " +
          "   and (:codFedUserId is null or "+Utente.ALIAS+".cod_fed_user_id ilike '%'||:codFedUserId||'%') " +
          "   and (:cognome is null or "+Utente.ALIAS+".de_lastname ilike '%'||:cognome||'%') " +
          "   and (:nome is null or "+Utente.ALIAS+".de_firstname ilike '%'||:nome||'%') "
  )
  @RegisterFieldMapper(Utente.class)
  List<Utente> searchUtentiForEnteTipoDovuto(Long mygovEnteTipoDovutoId, String codFedUserId, String cognome, String nome);
}
