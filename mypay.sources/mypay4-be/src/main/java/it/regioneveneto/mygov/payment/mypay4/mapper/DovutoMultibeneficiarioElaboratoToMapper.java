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

import it.regioneveneto.mygov.payment.mypay4.dto.DovutoMultibeneficiarioElaboratoTo;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
@Component
@NoArgsConstructor
public class DovutoMultibeneficiarioElaboratoToMapper implements RowMapper<DovutoMultibeneficiarioElaboratoTo> {

	@Override
	public DovutoMultibeneficiarioElaboratoTo map(ResultSet rs, StatementContext ctx) throws SQLException {

		DovutoMultibeneficiarioElaboratoTo.DovutoMultibeneficiarioElaboratoToBuilder builder = DovutoMultibeneficiarioElaboratoTo.builder()
				.idDovutoMultibeneficiarioElaborato(rs.getLong("mygov_dovuto_multibeneficiario_elaborato_id"))
				.denominazioneBeneficiario(rs.getString("de_rp_ente_benef_denominazione_beneficiario"))
				.codiceIdentificativoUnivoco( rs.getString("codice_fiscale_ente"))
				.ibanAddebito( rs.getString("cod_rp_dati_vers_dati_sing_vers_iban_accredito"))
				.indirizzoBeneficiario( rs.getString("de_rp_ente_benef_indirizzo_beneficiario"))
				.civicoBeneficiario( rs.getString("de_rp_ente_benef_civico_beneficiario"))
				.capBeneficiario(rs.getString("cod_rp_ente_benef_cap_beneficiario"))
				.nazioneBeneficiario(rs.getString("cod_rp_ente_benef_nazione_beneficiario"))
				.provinciaBeneficiario(rs.getString("de_rp_ente_benef_provincia_beneficiario"))
				.localitaBeneficiario(rs.getString("de_rp_ente_benef_localita_beneficiario"))
				.idDovutoElaborato(rs.getLong("mygov_dovuto_elaborato_id"))
				.importoSecondario(Utilities.ifNotNull(
						rs.getBigDecimal("num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento"),
						BigDecimal::toString));

		return builder.build();
	}

}
