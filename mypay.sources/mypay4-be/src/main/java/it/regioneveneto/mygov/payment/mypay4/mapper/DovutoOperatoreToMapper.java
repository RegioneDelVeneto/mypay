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

import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.service.AnagraficaStatoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteTipoDovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.LocationService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@NoArgsConstructor
public class DovutoOperatoreToMapper implements RowMapper<DovutoOperatoreTo> {

  @Autowired
  @Lazy
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  @Lazy
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  @Lazy
  private LocationService locationService;

  private boolean loadDetails = false;

  private static String deTipoDovutoScaduto;
  private static String deTipoDovutoPagamentoInCorso;

  @Override
  public DovutoOperatoreTo map(ResultSet rs, StatementContext ctx) throws SQLException {
    String dovutoType = rs.getString("search_type");
    String deStato, codStato;

    DovutoOperatoreTo.DovutoOperatoreToBuilder builder = DovutoOperatoreTo.builder();
    EnteTipoDovuto etd = enteTipoDovutoService.getOptionalByCodTipo(
        rs.getString("cod_tipo_dovuto"),rs.getString("cod_ipa_ente"), false)
        .orElseThrow(()-> new NotFoundException("tipoDovuto not found"));

    if (dovutoType.equals("debito")) {
      LocalDateTime dataStato = Utilities.toLocalDateTime(rs.getTimestamp("dt_ultima_modifica"));
      LocalDate dataScadenzaRaw = Utilities.toLocalDate(rs.getTimestamp("dt_rp_dati_vers_data_esecuzione_pagamento"));
      LocalDate dataScadenza = etd.isFlgStampaDataScadenza() ? dataScadenzaRaw : null;

      codStato = rs.getString("cod_stato");
      if(dataScadenza!=null && dataScadenza.isBefore(LocalDate.now())
          && !codStato.equals(AnagraficaStato.STATO_DOVUTO_PAGAMENTO_INIZIATO)
          && etd.isFlgScadenzaObbligatoria()){
        codStato = AnagraficaStato.STATO_DOVUTO_SCADUTO;
        if(deTipoDovutoScaduto == null)
          deTipoDovutoScaduto = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_SCADUTO, Constants.STATO_TIPO_DOVUTO).getDeStato();
        deStato = deTipoDovutoScaduto;
      } else {
        deStato = rs.getString("de_stato");
      }

      String iuv = rs.getString("cod_iuv");
      boolean flgIuvVolatile = rs.getBoolean("flg_iuv_volatile");
      if(flgIuvVolatile){
        //iuv volatile: user should not se IUV; it should appear with state "pagamento in corso"
        iuv = null;
        codStato = Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO;
        if(deTipoDovutoPagamentoInCorso == null)
          deTipoDovutoPagamentoInCorso = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO, Constants.STATO_TIPO_DOVUTO).getDeStato();
        deStato = deTipoDovutoPagamentoInCorso;
      }

      builder
          .dovutoType("debito")
          .id(rs.getLong("mygov_dovuto_id"))
          .codFiscale(rs.getString("cod_rp_sogg_pag_id_univ_pag_codice_id_univoco"))
          .iud(rs.getString("cod_iud"))
          .iuv(iuv)
          .flgIuvVolatile(flgIuvVolatile)
          .causale(rs.getString("de_rp_dati_vers_dati_sing_vers_causale_versamento"))
          .causaleVisualizzata(rs.getString("de_causale_visualizzata"))
          .importo(Utilities.ifNotNull(rs.getBigDecimal("num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento"), BigDecimal::toString))
          .dataScadenza(dataScadenza)
          .stato(deStato)
          .codStato(codStato)
          .dataStato(dataStato)
          .hasAvviso(iuv!=null && (iuv.length()==15 || iuv.length()==17));

      if(loadDetails) {
        Optional<NazioneTo> nazione = Optional.ofNullable(rs.getString("cod_rp_sogg_pag_nazione_pagatore"))
          .filter(StringUtils::isNotBlank)
          .map(String::trim)
          .map(locationService::getNazioneByCodIso);
        Optional<ProvinciaTo> provincia;
        Optional<ComuneTo> comune = Optional.empty();
        if(nazione.map(NazioneTo::hasProvince).orElse(false)){
          provincia = Optional.ofNullable(rs.getString("de_rp_sogg_pag_provincia_pagatore"))
            .filter(StringUtils::isNotBlank).map(String::trim).
            map(locationService::getProvinciaBySigla);
          if(provincia.isPresent())
            comune = Optional.ofNullable(rs.getString("de_rp_sogg_pag_localita_pagatore"))
                .filter(StringUtils::isNotBlank).map(String::trim)
                .map(nomeComune -> ComuneTo.builder().comune(nomeComune).build())
                .map(c -> locationService.getComuneByNameAndSiglaProvincia(c.getComune(), provincia.get().getSigla()).orElse(c));
        } else
          provincia = Optional.empty();
        builder
            // 2021/dic/06: come deciso durante il collaudo, la dataScadenza, nel caso di dovuti con
            //  flgStampaDataScadenza a false, viene mostrata solo sulla pagina di dettaglio dell'operatore,
            //  mentre è nascosta in tutti gli altri posti della webapp (sia per il cittadino che per l'operatore)
            .dataScadenza(dataScadenzaRaw)
            .tipoDovuto(EnteTipoDovutoTo.builder()
                .mygovEnteTipoDovutoId(etd.getMygovEnteTipoDovutoId())
                .codTipo(etd.getCodTipo())
                .deTipo(etd.getDeTipo())
                .build())
            .iuf(rs.getString("iuf"))
            .anagrafica(rs.getString("de_rp_sogg_pag_anagrafica_pagatore"))
            .tipoSoggetto(rs.getString("cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco"))
            .hasCodFiscale(true)
            .email(rs.getString("de_rp_sogg_pag_email_pagatore"))
            .indirizzo(rs.getString("de_rp_sogg_pag_indirizzo_pagatore"))
            .numCiv(rs.getString("de_rp_sogg_pag_civico_pagatore"))
            .cap(rs.getString("cod_rp_sogg_pag_cap_pagatore"))
            .nazione(nazione.orElse(null))
            .prov(provincia.orElse(null))
            .comune(comune.orElse(null));
      }
    } else {
      LocalDateTime dataStato = Utilities.toLocalDateTime(rs.getTimestamp("dt_ultimo_cambio_stato"));
      LocalDate dataScadenza = Utilities.toLocalDate(rs.getTimestamp("dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento"));
      if (dataScadenza == null) {
        dataScadenza = Utilities.toLocalDate(rs.getTimestamp( "dt_ultimo_cambio_stato"));
      }
      codStato = rs.getString("cod_stato");
      if (!Constants.STATO_DOVUTO_COMPLETATO.equals(codStato)) {
        deStato = rs.getString("de_stato");
      } else {
        if (rs.getBigDecimal("num_e_dati_pag_dati_sing_pag_singolo_importo_pagato").compareTo(BigDecimal.ZERO) == 0) {
          codStato = Constants.STATO_CARRELLO_NON_PAGATO;
          deStato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_NON_PAGATO, Constants.STATO_TIPO_CARRELLO).getDeStato();
        } else {
          codStato = Constants.STATO_CARRELLO_PAGATO;
          deStato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PAGATO, Constants.STATO_TIPO_CARRELLO).getDeStato();
        }
      }
      BigDecimal importo = deStato.equals(Constants.STATO_CARRELLO_PAGATO) ?
          rs.getBigDecimal("num_e_dati_pag_dati_sing_pag_singolo_importo_pagato") : rs.getBigDecimal("num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento");

      String messaggioRicevuta = rs.getString("cod_e_id_messaggio_ricevuta");

      builder
          .dovutoType("pagato")
          .id(rs.getLong("mygov_dovuto_elaborato_id"))
          .codFiscale(rs.getString("cod_rp_sogg_pag_id_univ_pag_codice_id_univoco"))
          .iud(rs.getString("cod_iud"))
          .iuv(StringUtils.firstNonBlank(rs.getString("cod_iuv"), rs.getString("cod_rp_silinviarp_id_univoco_versamento")))
          .causale(rs.getString("de_rp_dati_vers_dati_sing_vers_causale_versamento"))
          .causaleVisualizzata(rs.getString("de_causale_visualizzata"))
          .importo(importo.toString())
          .dataScadenza(dataScadenza)
          .stato(deStato)
          .codStato(codStato)
          .dataStato(dataStato)
          .tipoDovuto(EnteTipoDovutoTo.builder()
              .mygovEnteTipoDovutoId(etd.getMygovEnteTipoDovutoId())
              .codTipo(etd.getCodTipo())
              .deTipo(etd.getDeTipo())
              .build())
          .iuf(rs.getString("iuf"))
          .hasRicevuta(Constants.STATO_DOVUTO_COMPLETATO.equals(rs.getString("cod_stato"))
              && StringUtils.isNotBlank(messaggioRicevuta) && !messaggioRicevuta.startsWith("###"));

      //0000106: Aggiunte informazioni transazioni chiuse nel dettaglio pagato
      builder
          .dataInizioTransazione(Utilities.ifNotNull(rs.getTimestamp("dt_ultima_modifica_rp"), Timestamp::toLocalDateTime))
          .identificativoTransazione(StringUtils.join(ArrayUtils.removeAllOccurrences(new String[]{
              rs.getString("cod_rp_silinviarp_id_univoco_versamento"),
              rs.getString("cod_rp_silinviarp_codice_contesto_pagamento")},null)," - "))
          .intestatario(StringUtils.join(ArrayUtils.removeAllOccurrences(new String[]{
              rs.getString("de_rp_sogg_pag_anagrafica_pagatore"),
              rs.getString("cod_rp_sogg_pag_id_univ_pag_codice_id_univoco")},null)," - "))
            .pspScelto(StringUtils.join(ArrayUtils.removeAllOccurrences(new String[]{
            rs.getString("de_e_istit_att_denominazione_attestante"),
            rs.getString("cod_e_istit_att_id_univ_att_codice_id_univoco")},null)," - "));
    }

    return builder.build();
  }

  @Override
  public RowMapper<DovutoOperatoreTo> specialize(ResultSet rs, StatementContext ctx) {
    this.loadDetails = "true".equals(ctx.getAttribute("details"));
    return this;
  }
}
