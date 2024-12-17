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

import it.regioneveneto.mygov.payment.mypay4.model.EnteFunzionalita;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface EnteFunzionalitaDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_ente_funzionalita (" +
          "  mygov_ente_funzionalita_id" +
          " ,cod_ipa_ente" +
          " ,cod_funzionalita" +
          " ,flg_attivo" +
          ") values (" +
          "  nextval('mygov_ente_funzionalita_mygov_ente_funzionalita_id_seq')" +
          " ,:d.codIpaEnte" +
          " ,:d.codFunzionalita" +
          " ,:d.flgAttivo)"
  )
  @GetGeneratedKeys("mygov_ente_funzionalita_id")
  long insert(@BindBean("d") EnteFunzionalita d);

  @SqlUpdate(
      "update mygov_ente_funzionalita set" +
          "  cod_ipa_ente = :d.codIpaEnte" +
          " ,cod_funzionalita = :d.codFunzionalita" +
          " ,flg_attivo = :d.flgAttivo" +
          " where mygov_ente_funzionalita_id = :d.mygovEnteFunzionalitaId"
  )
  int update(@BindBean("d") EnteFunzionalita d);

  @SqlQuery(
      "    select "+ EnteFunzionalita.ALIAS+ALL_FIELDS+
          "  from mygov_ente_funzionalita "+EnteFunzionalita.ALIAS+
          "  where "+EnteFunzionalita.ALIAS+".mygov_ente_funzionalita_id = :mygovEnteFunzionalitaId")
  @RegisterFieldMapper(EnteFunzionalita.class)
  Optional<EnteFunzionalita> getById(Long mygovEnteFunzionalitaId);

  @SqlQuery(
      "    select "+EnteFunzionalita.ALIAS+ALL_FIELDS+","+
          " (select max("+ RegistroOperazione.ALIAS+".dt_operazione) as dt_ultima_abilitazione" +
          "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
          "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'ENTE_FUNZ'"+
          "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+EnteFunzionalita.ALIAS+".cod_ipa_ente || '|' || "+EnteFunzionalita.ALIAS+".cod_funzionalita"+
          "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = true),"+
          " (select max("+ RegistroOperazione.ALIAS+".dt_operazione) as dt_ultima_disabilitazione" +
          "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
          "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'ENTE_FUNZ'"+
          "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+EnteFunzionalita.ALIAS+".cod_ipa_ente || '|' || "+EnteFunzionalita.ALIAS+".cod_funzionalita"+
          "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = false)"+
          "  from mygov_ente_funzionalita "+EnteFunzionalita.ALIAS+
          " where "+EnteFunzionalita.ALIAS+".cod_ipa_ente = :codIpaEnte " +
          "   and ("+EnteFunzionalita.ALIAS+".flg_attivo = :flgAttivo or :flgAttivo is null)" +
          " order by "+EnteFunzionalita.ALIAS+".cod_funzionalita")
  @RegisterFieldMapper(EnteFunzionalita.class)
  List<EnteFunzionalita> getAllByCodIpaEnte(String codIpaEnte, Boolean flgAttivo);

  @SqlQuery(
          "    select "+EnteFunzionalita.ALIAS+ALL_FIELDS+","+
                  " (select max("+ RegistroOperazione.ALIAS+".dt_operazione) as dt_ultima_abilitazione" +
                  "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
                  "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'ENTE_FUNZ'"+
                  "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+EnteFunzionalita.ALIAS+".cod_ipa_ente || '|' || "+EnteFunzionalita.ALIAS+".cod_funzionalita"+
                  "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = true),"+
                  " (select max("+ RegistroOperazione.ALIAS+".dt_operazione) as dt_ultima_disabilitazione" +
                  "    from mygov_registro_operazione "+RegistroOperazione.ALIAS+
                  "   where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = 'ENTE_FUNZ'"+
                  "     and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = "+EnteFunzionalita.ALIAS+".cod_ipa_ente || '|' || "+EnteFunzionalita.ALIAS+".cod_funzionalita"+
                  "     and "+RegistroOperazione.ALIAS+".cod_stato_bool = false)"+
                  "  from mygov_ente_funzionalita "+EnteFunzionalita.ALIAS+
                  " order by "+EnteFunzionalita.ALIAS+".cod_funzionalita")
  @RegisterFieldMapper(EnteFunzionalita.class)
  List<EnteFunzionalita> getAll();

  @SqlQuery(
      "    select count(1) > 0 " +
          "  from mygov_ente_funzionalita "+EnteFunzionalita.ALIAS +
          " where "+EnteFunzionalita.ALIAS+".cod_ipa_ente = :codIpaEnte " +
          "   and "+EnteFunzionalita.ALIAS+".cod_funzionalita = :codFunzionalita " +
          "   and "+EnteFunzionalita.ALIAS+".flg_attivo = true ")
  boolean isActiveByFunzionalitaAndCodIpaEnte(String codIpaEnte, String codFunzionalita);
}
