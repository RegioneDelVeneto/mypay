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
package it.regioneveneto.mygov.payment.mypay4.mapper;

import it.regioneveneto.mygov.payment.mypay4.dto.UtenteTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Component
@NoArgsConstructor
public class UtenteToMapper implements RowMapper<UtenteTo> {

  @Override
  public UtenteTo map(ResultSet rs, StatementContext ctx) throws SQLException {
    Timestamp lastLogin = rs.getTimestamp("dt_ultimo_login");
    Timestamp dtUltimaAbilitazione = rs.getTimestamp("dt_ultima_abilitazione");
    Timestamp dtUltimaDisabilitazione = rs.getTimestamp("dt_ultima_disabilitazione");
    Character emailSourceType = Optional.ofNullable(rs.getString("email_source_type"))
        .map(x -> x.charAt(0))
        .filter(Utente.EMAIL_SOURCE_TYPES::isValid)
        .orElseThrow(()->new MyPayException("invalid emailSourceType"));

    return UtenteTo.builder().userId(rs.getLong("mygov_utente_id"))
        .username(rs.getString("cod_fed_user_id"))
        .codiceFiscale(rs.getString("cod_codice_fiscale_utente"))
        .nome(rs.getString("de_firstname"))
        .cognome(rs.getString("de_lastname"))
        .email(rs.getString("de_email_address"))
        .emailNew(rs.getString("de_email_address_new"))
        .emailSourceType(emailSourceType)
        .ruolo(rs.getString("ruolo"))
        .lastLogin(Utilities.toLocalDateTime(lastLogin))
        .dtUltimaAbilitazione(Utilities.toLocalDateTime(dtUltimaAbilitazione))
        .dtUltimaDisabilitazione(Utilities.toLocalDateTime(dtUltimaDisabilitazione))
        .build();
  }
}
