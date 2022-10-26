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

import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface RegistroOperazioneDao extends BaseDao {

  @SqlQuery(
      "select "+ RegistroOperazione.ALIAS+ALL_FIELDS +
          "  from mygov_registro_operazione "+RegistroOperazione.ALIAS +
          " where "+RegistroOperazione.ALIAS+".cod_tipo_operazione = :tipoOperazione" +
          "   and "+RegistroOperazione.ALIAS+".de_oggetto_operazione = :oggettoOperazione" +
          " order by "+RegistroOperazione.ALIAS+".dt_operazione desc"
  )
  @RegisterFieldMapper(RegistroOperazione.class)
  List<RegistroOperazione> getByTipoAndOggetto(RegistroOperazione.TipoOperazione tipoOperazione, String oggettoOperazione);

  @SqlUpdate(
      "insert into mygov_registro_operazione ( " +
          "mygov_registro_operazione_id, " +
          "cod_fed_user_id_operatore, " +
          "cod_tipo_operazione, " +
          "de_oggetto_operazione, " +
          "cod_stato_bool ) values ( " +
          "nextval('mygov_registro_operazione_id_seq'), " +
          ":r.codFedUserIdOperatore, " +
          ":r.codTipoOperazione, " +
          ":r.deOggettoOperazione, " +
          ":r.codStatoBool ) "
  )
  void insert(@BindBean("r") RegistroOperazione r);

}
