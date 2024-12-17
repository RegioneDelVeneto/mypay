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

import it.regioneveneto.mygov.payment.mypay4.model.Operatore;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface OperatoreDao extends BaseDao {

  @SqlQuery(
      "select "+ Operatore.ALIAS+ALL_FIELDS +
          " from mygov_operatore "+Operatore.ALIAS +
          " where "+Operatore.ALIAS+".cod_ipa_ente = :codIpaEnte"
  )
  @RegisterFieldMapper(Operatore.class)
  List<Operatore> getAllOperatoriByCodIpaEnte(String codIpaEnte);

  @SqlQuery(
          "select "+ Operatore.ALIAS+ALL_FIELDS +
                  " from mygov_operatore "+Operatore.ALIAS
  )
  @RegisterFieldMapper(Operatore.class)
  List<Operatore> getAll();

  @SqlQuery(
      "select "+ Operatore.ALIAS+ALL_FIELDS +
          " from mygov_operatore "+Operatore.ALIAS +
          " where "+Operatore.ALIAS+".cod_ipa_ente = :codIpaEnte" +
          "   and "+Operatore.ALIAS+".cod_fed_user_id = :codFedUserId"
  )
  @RegisterFieldMapper(Operatore.class)
  Operatore getByCodFedUserIdEnte(String codFedUserId, String codIpaEnte);

  @SqlQuery(
      "select "+ Operatore.ALIAS+ALL_FIELDS +
          " from mygov_operatore "+Operatore.ALIAS +
          " where "+Operatore.ALIAS+".cod_fed_user_id = :codFedUserId"
  )
  @RegisterFieldMapper(Operatore.class)
  List<Operatore> getByCodFedUserId(String codFedUserId);

  @SqlUpdate(
      "update mygov_operatore set " +
          "  cod_fed_user_id = :d.codFedUserId" +
          " ,cod_ipa_ente = :d.codIpaEnte" +
          " ,ruolo = :d.ruolo" +
          " ,de_email_address = :d.deEmailAddress" +
          "  where mygov_operatore_id = :d.mygovOperatoreId"
  )
  void update(@BindBean("d") Operatore d);

  @SqlUpdate(
      "insert into mygov_operatore ( " +
          "cod_fed_user_id, " +
          "cod_ipa_ente, " +
          "mygov_operatore_id, " +
          "ruolo, " +
          "de_email_address) values ( " +
          ":codFedUserId, " +
          ":codIpaEnte, " +
          "nextval('mygov_operatore_mygov_operatore_id_seq'), " +
          ":ruolo, " +
          ":deEmailAddress) " +
          "returning mygov_operatore_id"
  )
  @GetGeneratedKeys
  Long insert(String codFedUserId, String codIpaEnte, String ruolo, String deEmailAddress);

  @SqlUpdate(
      "delete from mygov_operatore " +
          "where cod_fed_user_id = :codFedUserId " +
          "and cod_ipa_ente = :codIpaEnte"
  )
  int delete(String codFedUserId, String codIpaEnte);

}
