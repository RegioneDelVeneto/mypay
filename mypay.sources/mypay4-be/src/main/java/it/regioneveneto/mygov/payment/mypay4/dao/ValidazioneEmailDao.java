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

import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.model.ValidazioneEmail;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface ValidazioneEmailDao extends BaseDao {

  @SqlUpdate(
      "update mygov_validazione_email set" +
          "   codice = :d.codice " +
          " , dt_primo_invio = :d.dtPrimoInvio " +
          " , dt_ultimo_invio = :d.dtUltimoInvio " +
          " , num_invii = :d.numInvii " +
          " , num_tentativi = :d.numTentativi " +
          " , dt_ultimo_tentativo = :d.dtUltimoTentativo " +
          " where mygov_utente_id = :d.mygovUtenteId.mygovUtenteId"
  )
  int update(@BindBean("d") ValidazioneEmail d);

  @SqlUpdate(
      "insert into mygov_validazione_email (" +
          "   mygov_utente_id" +
          " , codice" +
          " , dt_primo_invio" +
          " , dt_ultimo_invio" +
          " , num_invii" +
          " , num_tentativi" +
          " , dt_ultimo_tentativo" +
          ") values (" +
          "   :d.mygovUtenteId.mygovUtenteId" +
          " , :d.codice" +
          " , now()" +
          " , now()" +
          " , 1" +
          " , 0" +
          " , null" +
          ")"
  )
  int insert(@BindBean("d") ValidazioneEmail d);

  @SqlUpdate(
      "delete from mygov_validazione_email " +
          " where mygov_utente_id = :mygovUtenteId"
  )
  int delete(Long mygovUtenteId);

  @SqlQuery(
      "    select " + ValidazioneEmail.ALIAS + ALL_FIELDS + ", " + Utente.FIELDS +
          "  from mygov_validazione_email " + ValidazioneEmail.ALIAS +
          "  join mygov_utente " + Utente.ALIAS +
          "    on "+Utente.ALIAS+".mygov_utente_id = "+ValidazioneEmail.ALIAS+".mygov_utente_id " +
          " where "+ValidazioneEmail.ALIAS+".mygov_utente_id = :mygovUtenteId")
  @RegisterFieldMapper(ValidazioneEmail.class)
  Optional<ValidazioneEmail> get(Long mygovUtenteId);

  @SqlQuery(
      "    select " + ValidazioneEmail.ALIAS + ALL_FIELDS + ", " + Utente.FIELDS +
          "  from mygov_validazione_email " + ValidazioneEmail.ALIAS +
          "  join mygov_utente " + Utente.ALIAS +
          "    on "+Utente.ALIAS+".mygov_utente_id = "+ValidazioneEmail.ALIAS+".mygov_utente_id " +
          " where "+Utente.ALIAS+".cod_fed_user_id = :username")
  @RegisterFieldMapper(ValidazioneEmail.class)
  Optional<ValidazioneEmail> getByUsername(String username);

}
