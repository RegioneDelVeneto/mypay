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

import it.regioneveneto.mygov.payment.mypay4.dto.EnteTipoDovutoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.service.common.ThumbnailService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
@NoArgsConstructor
public class EnteTipoDovutoToMapper implements RowMapper<EnteTipoDovutoTo> {

  @Autowired
  @Lazy
  private ThumbnailService thumbnailService;

  @Override
  public EnteTipoDovutoTo map(ResultSet rs, StatementContext ctx) throws SQLException {

    EnteTipoDovutoTo tipoDovuto = new EnteTipoDovutoTo();
    tipoDovuto.setMygovEnteTipoDovutoId(rs.getLong("mygov_ente_tipo_dovuto_id"));
    tipoDovuto.setMygovEnteId(rs.getLong("mygov_ente_id"));
    tipoDovuto.setCodIpaEnte(rs.getString("cod_ipa_ente"));
    tipoDovuto.setDeNomeEnte(rs.getString("de_nome_ente"));
    try {
      String logoString = rs.getString("de_logo_ente");
      thumbnailService.generateThumbnail(logoString).ifPresent( thumbLogoEnte -> {
        tipoDovuto.setThumbLogoEnte(thumbLogoEnte.getContent());
        tipoDovuto.setHashThumbLogoEnte(thumbLogoEnte.getHash());
      });
    } catch(Exception e){
      throw new MyPayException("invalid logo for ente: "+tipoDovuto.getCodIpaEnte(), e);
    }
    tipoDovuto.setCodTipo(rs.getString("cod_tipo"));
    tipoDovuto.setDeTipo(rs.getString("de_tipo"));
    tipoDovuto.setDeUrlPagamentoDovuto(rs.getString("de_url_pagamento_dovuto"));
    tipoDovuto.setFlgCfAnonimo(rs.getBoolean("flg_cf_anonimo"));
    tipoDovuto.setFlgScadenzaObbligatoria(rs.getBoolean("flg_scadenza_obbligatoria"));
    tipoDovuto.setFlgAttivo(rs.getBoolean("flg_attivo"));
    BigDecimal importo = rs.getBigDecimal("importo");
    tipoDovuto.setImporto(importo != null ? importo.toString() : null);
    Timestamp dtUltimaAbilitazione = rs.getTimestamp("dt_ultima_abilitazione");
    tipoDovuto.setDtUltimaAbilitazione(Utilities.toLocalDateTime(dtUltimaAbilitazione));
    Timestamp dtUltimaDisabilitazione = rs.getTimestamp("dt_ultima_disabilitazione");
    tipoDovuto.setDtUltimaDisabilitazione(Utilities.toLocalDateTime(dtUltimaDisabilitazione));
    return tipoDovuto;
  }
}
