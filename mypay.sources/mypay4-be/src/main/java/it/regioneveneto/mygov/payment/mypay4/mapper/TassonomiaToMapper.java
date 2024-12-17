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

import it.regioneveneto.mygov.payment.mypay4.dto.TassonomiaTo;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@NoArgsConstructor
public class TassonomiaToMapper implements RowMapper<TassonomiaTo> {

	public TassonomiaTo map(ResultSet rs, StatementContext ctx) throws SQLException {
		return TassonomiaTo.builder()
			.id(rs.getLong("mygov_tassonomia_id"))
			.codiceTipoEnte(rs.getString("tipo_ente"))
			.descrizioneTipoEnte(rs.getString("descrizione_tipo_ente"))
			.progressivoMacroArea(rs.getString("prog_macro_area"))
			.nomeMacroArea(rs.getString("nome_macro_area"))
			.descricioneMacroArea(rs.getString("desc_macro_area"))
			.codiceTipoServizio(rs.getString("cod_tipo_servizio"))
			.tipoServizio(rs.getString("tipo_servizio"))
			.motivoRiscossione(rs.getString("motivo_riscossione"))
			.descrizioneTipoServizio(rs.getString("descrizione_tipo_servizio"))
			.versioneTassonomia(rs.getString("version"))
			.datiSpecificiIncasso(rs.getString("codice_tassonomico"))
			.dataInizioValidita(rs.getTimestamp("dt_inizio_validita"))
			.dataFineValidita(rs.getTimestamp("dt_fine_validita"))
			.build();
	}
}
