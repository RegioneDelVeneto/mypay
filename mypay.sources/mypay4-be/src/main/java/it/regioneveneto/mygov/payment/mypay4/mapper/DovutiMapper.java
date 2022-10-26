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

import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.veneto.regione.schemas._2012.pagamenti.ente.*;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Component
@NoArgsConstructor
public class DovutiMapper implements RowMapper<Dovuti> {

  @Autowired
  JAXBTransformService jaxbTransformService;

  @Value("${pa.deRpVersioneOggetto}")
  private String deRpVersioneOggetto;

  @Override
  public Dovuti map(ResultSet rs, StatementContext ctx) throws SQLException {
    Dovuti ctDovuti = new Dovuti();

    //	VERSIONE OGGETTO
    ctDovuti.setVersioneOggetto(deRpVersioneOggetto);

    //  SOGGETTO PAGATORE
    CtSoggettoPagatore soggettoPagatore = new CtSoggettoPagatore();

    CtIdentificativoUnivocoPersonaFG ctIdentificativoUnivocoPersonaFG = new CtIdentificativoUnivocoPersonaFG();
    ctIdentificativoUnivocoPersonaFG.setCodiceIdentificativoUnivoco(rs.getString("cod_rp_sogg_pag_id_univ_pag_codice_id_univoco"));
    String codRpSoggPagIdUnivPagTipoIdUnivoco = String.valueOf(rs.getString("cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco"));
    ctIdentificativoUnivocoPersonaFG.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.valueOf(codRpSoggPagIdUnivPagTipoIdUnivoco));

    soggettoPagatore.setIdentificativoUnivocoPagatore(ctIdentificativoUnivocoPersonaFG);
    soggettoPagatore.setAnagraficaPagatore(rs.getString("de_rp_sogg_pag_anagrafica_pagatore"));
    Optional.ofNullable(rs.getString("de_rp_sogg_pag_email_pagatore")).ifPresent(soggettoPagatore::setIndirizzoPagatore);
    Optional.ofNullable(rs.getString("de_rp_sogg_pag_civico_pagatore")).ifPresent(soggettoPagatore::setCivicoPagatore);
    Optional.ofNullable(rs.getString("cod_rp_sogg_pag_cap_pagatore")).ifPresent(soggettoPagatore::setCapPagatore);
    Optional.ofNullable(rs.getString("de_rp_sogg_pag_localita_pagatore")).ifPresent(soggettoPagatore::setLocalitaPagatore);
    Optional.ofNullable(rs.getString("de_rp_sogg_pag_provincia_pagatore")).ifPresent(soggettoPagatore::setProvinciaPagatore);
    Optional.ofNullable(rs.getString("cod_rp_sogg_pag_nazione_pagatore")).ifPresent(soggettoPagatore::setNazionePagatore);
    Optional.ofNullable(rs.getString("de_rp_sogg_pag_email_pagatore")).ifPresent(soggettoPagatore::setEMailPagatore);
    ctDovuti.setSoggettoPagatore(soggettoPagatore);

    CtDatiVersamentoDovuti datiVersamentoDovuti = new CtDatiVersamentoDovuti();

    datiVersamentoDovuti.setTipoVersamento(rs.getString("cod_rp_dati_vers_tipo_versamento"));
    Optional.ofNullable(rs.getString("cod_iuv")).ifPresent(datiVersamentoDovuti::setIdentificativoUnivocoVersamento);

    ctDovuti.setDatiVersamento(datiVersamentoDovuti);
    CtDatiSingoloVersamentoDovuti datiSingoloVersamento = new CtDatiSingoloVersamentoDovuti();
    datiVersamentoDovuti.getDatiSingoloVersamentos().add(datiSingoloVersamento);

    datiSingoloVersamento.setIdentificativoUnivocoDovuto(rs.getString("cod_iud"));
    datiSingoloVersamento.setImportoSingoloVersamento(rs.getBigDecimal("num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento"));
    Optional.ofNullable(rs.getBigDecimal("num_rp_dati_vers_dati_sing_vers_commissione_carico_pa")).ifPresent(datiSingoloVersamento::setCommissioneCaricoPA);

    datiSingoloVersamento.setIdentificativoTipoDovuto(rs.getString("cod_tipo_dovuto"));
    datiSingoloVersamento.setCausaleVersamento(rs.getString("de_rp_dati_vers_dati_sing_vers_causale_versamento"));
    datiSingoloVersamento.setDatiSpecificiRiscossione(rs.getString("de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione"));

    Optional.ofNullable(rs.getString("bilancio"))
        .map(e -> jaxbTransformService.unmarshalling(e.getBytes(), Bilancio.class))
        .ifPresent(datiSingoloVersamento::setBilancio);
    return ctDovuti;
  }
}
