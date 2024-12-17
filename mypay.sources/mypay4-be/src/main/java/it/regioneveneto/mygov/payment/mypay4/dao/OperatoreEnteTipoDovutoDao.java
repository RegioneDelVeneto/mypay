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

import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Operatore;
import it.regioneveneto.mygov.payment.mypay4.model.OperatoreEnteTipoDovuto;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface OperatoreEnteTipoDovutoDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_operatore_ente_tipo_dovuto (" +
          "   mygov_operatore_ente_tipo_dovuto_id" +
          " , mygov_ente_tipo_dovuto_id" +
          " , flg_attivo" +
          " , mygov_operatore_id" +
          " ) values (" +
          "   nextval('mygov_operatore_mygov_operatore_ente_tipo_dovuto_id_seq')" +
          " , :d.mygovEnteTipoDovutoId?.mygovEnteTipoDovutoId" +
          " , :d.flgAttivo" +
          " , :d.mygovOperatoreId.mygovOperatoreId)"
  )
  @GetGeneratedKeys("mygov_operatore_ente_tipo_dovuto_id")
  long insert(@BindBean("d") OperatoreEnteTipoDovuto d);

  @SqlUpdate(
      "update mygov_operatore_ente_tipo_dovuto set" +
          "   mygov_ente_tipo_dovuto_id = :d.mygovEnteTipoDovutoId?.mygovEnteTipoDovutoId" +
          " , mygov_operatore_id = :d.mygovOperatoreId.mygovOperatoreId" +
          " , flg_attivo = :d.flgAttivo" +
          " where mygov_operatore_ente_tipo_dovuto_id = :d.mygovOperatoreEnteTipoDovutoId"
  )
  int update(@BindBean("d") OperatoreEnteTipoDovuto d);

  @SqlUpdate(
      "delete from mygov_operatore_ente_tipo_dovuto where mygov_ente_tipo_dovuto_id = :mygovEnteTipoDovutoId"
  )
  int deleteAllByEnteTipoDovuto(Long mygovEnteTipoDovutoId);

  @SqlUpdate(
      "delete from mygov_operatore_ente_tipo_dovuto " +
          " where mygov_operatore_id in ( " +
          " select mygov_operatore_id from mygov_operatore where cod_fed_user_id = :codFedUserId ) " +
          "   and mygov_ente_tipo_dovuto_id in ( " +
          " select mygov_ente_tipo_dovuto_id from mygov_ente_tipo_dovuto where mygov_ente_id = :mygovEnteId )"
  )
  int deleteAllTipiDovutoByEnteForOperatore(String codFedUserId, Long mygovEnteId);

  @SqlUpdate(
      "delete from mygov_operatore_ente_tipo_dovuto " +
          " where mygov_operatore_id in ( " +
          " select mygov_operatore_id from mygov_operatore where cod_fed_user_id = :codFedUserId ) "
  )
  int deleteAllTipiDovutoForOperatore(String codFedUserId);

  @SqlUpdate(
      "insert into mygov_operatore_ente_tipo_dovuto (" +
          "   mygov_operatore_ente_tipo_dovuto_id" +
          " , mygov_ente_tipo_dovuto_id" +
          " , flg_attivo" +
          " , mygov_operatore_id" +
          " ) ( select " +
          "  nextval('mygov_operatore_mygov_operatore_ente_tipo_dovuto_id_seq')" +
          ", mygov_ente_tipo_dovuto_id " +
          ", true " +
          ", :mygovOperatoreId " +
          " from mygov_ente_tipo_dovuto " +
          " where mygov_ente_id = :mygovEnteId ) "
  )
  int insertAllTipiDovutoByEnteForOperatore(Long mygovOperatoreId, Long mygovEnteId);

  @SqlQuery(
      "    select "+OperatoreEnteTipoDovuto.ALIAS+ALL_FIELDS+", "+EnteTipoDovuto.FIELDS+", "+Operatore.FIELDS +
          "  from mygov_operatore_ente_tipo_dovuto " + OperatoreEnteTipoDovuto.ALIAS +
          "  join mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id " +
          "  join mygov_ente "+Ente.ALIAS +
          "    on "+Ente.ALIAS+".mygov_ente_id = "+EnteTipoDovuto.ALIAS+".mygov_ente_id " +
          "  join mygov_operatore "+Operatore.ALIAS +
          "    on "+Operatore.ALIAS+".mygov_operatore_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_operatore_id " +
          " where mygov_operatore_ente_tipo_dovuto_id = :mygovOperatoreEnteTipoDovutoId"
  )
  @RegisterFieldMapper(OperatoreEnteTipoDovuto.class)
  Optional<OperatoreEnteTipoDovuto> getById(Long mygovOperatoreEnteTipoDovutoId);

  @SqlQuery(
      "    select "+OperatoreEnteTipoDovuto.ALIAS+ALL_FIELDS+", "+EnteTipoDovuto.FIELDS+", "+Operatore.FIELDS +
          "  from mygov_operatore_ente_tipo_dovuto " + OperatoreEnteTipoDovuto.ALIAS +
          "  join mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id " +
          "  join mygov_ente "+Ente.ALIAS +
          "    on "+Ente.ALIAS+".mygov_ente_id = "+EnteTipoDovuto.ALIAS+".mygov_ente_id " +
          "  join mygov_operatore "+Operatore.ALIAS +
          "    on "+Operatore.ALIAS+".mygov_operatore_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_operatore_id " +
          " where ("+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte or :codIpaEnte is null) " +
          "   and ("+EnteTipoDovuto.ALIAS+".cod_tipo = :codTipo or :codTipo is null)" +
          "   and ("+Operatore.ALIAS+".cod_fed_user_id = :codFedUserId or :codFedUserId is null) " +
          " order by mygov_operatore_ente_tipo_dovuto_id "
  )
  @RegisterFieldMapper(OperatoreEnteTipoDovuto.class)
  List<OperatoreEnteTipoDovuto> getByCodIpaCodTipoCodFed(String codIpaEnte, String codTipo, String codFedUserId);

  @SqlQuery(
      "    select "+EnteTipoDovuto.ALIAS+".cod_tipo " +
          "  from mygov_operatore_ente_tipo_dovuto " + OperatoreEnteTipoDovuto.ALIAS +
          "  join mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id " +
          "  join mygov_ente " + Ente.ALIAS +
          "    on "+Ente.ALIAS+".mygov_ente_id = "+EnteTipoDovuto.ALIAS+".mygov_ente_id " +
          "  join mygov_operatore " + Operatore.ALIAS +
          "    on "+Operatore.ALIAS+".mygov_operatore_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_operatore_id " +
          " where ("+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte or :codIpaEnte is null) " +
          "   and ("+Operatore.ALIAS+".cod_fed_user_id = :codFedUserId or :codFedUserId is null) "
  )
  List<String> getTipoByCodIpaCodFedUser(String codIpaEnte, String codFedUserId);

  @SqlQuery(
      "    select "+OperatoreEnteTipoDovuto.ALIAS+ALL_FIELDS+", "+EnteTipoDovuto.FIELDS+", "+Operatore.FIELDS +
          "  from mygov_operatore_ente_tipo_dovuto " + OperatoreEnteTipoDovuto.ALIAS +
          "  join mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id " +
          "  join mygov_operatore "+Operatore.ALIAS +
          "    on "+Operatore.ALIAS+".mygov_operatore_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_operatore_id " +
          " where "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = :mygovEnteTipoDovuto" +
          " order by mygov_operatore_ente_tipo_dovuto_id "
  )
  @RegisterFieldMapper(OperatoreEnteTipoDovuto.class)
  List<OperatoreEnteTipoDovuto> getByEnteTipoDovuto(Long mygovEnteTipoDovuto);

  @SqlQuery(
          "    select "+OperatoreEnteTipoDovuto.ALIAS+ALL_FIELDS+", "+EnteTipoDovuto.FIELDS+", "+Operatore.FIELDS +
                  "  from mygov_operatore_ente_tipo_dovuto " + OperatoreEnteTipoDovuto.ALIAS +
                  "  join mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS +
                  "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id " +
                  "  join mygov_operatore "+Operatore.ALIAS +
                  "    on "+Operatore.ALIAS+".mygov_operatore_id = "+OperatoreEnteTipoDovuto.ALIAS+".mygov_operatore_id " +
                  " order by mygov_operatore_ente_tipo_dovuto_id "
  )
  @RegisterFieldMapper(OperatoreEnteTipoDovuto.class)
  List<OperatoreEnteTipoDovuto> getAll();

  @SqlQuery(
      "    select count(1) > 0 " +
          "  from mygov_operatore_ente_tipo_dovuto " + OperatoreEnteTipoDovuto.ALIAS +
          " where "+OperatoreEnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = :d.mygovEnteTipoDovutoId.mygovEnteTipoDovutoId " +
          "   and "+OperatoreEnteTipoDovuto.ALIAS+".mygov_operatore_id = :d.mygovOperatoreId.mygovOperatoreId " +
          " limit 1 "
  )
  boolean existsByEnteTipoDovutoAndOperatore(@BindBean("d") OperatoreEnteTipoDovuto d);

}
