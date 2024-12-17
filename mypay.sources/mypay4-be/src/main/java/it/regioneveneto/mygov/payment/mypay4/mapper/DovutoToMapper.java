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
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.service.AnagraficaStatoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteTipoDovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.LocationService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@NoArgsConstructor
public class DovutoToMapper implements RowMapper<DovutoTo> {

  @Autowired
  @Lazy
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  @Lazy
  private LocationService locationService;

  @Autowired
  @Lazy
  private AnagraficaStatoService anagraficaStatoService;

  private static String deTipoDovutoScaduto;
  private static String deTipoDovutoPagamentoInCorso;

  @Override
  public DovutoTo map(ResultSet rs, StatementContext ctx) throws SQLException {

    DovutoTo dovutoTo = new DovutoTo();

    //fill nested object (using cache for better performance, only ID is retrieved in the main query)
    //dovutoTo.setNestedEnte(enteService.getEnteById(dovuto.getNestedEnte().getMygovEnteId()));
    //dovutoTo.setMygovAnagraficaStatoId(anagraficaStatoService.getById(dovuto.getMygovAnagraficaStatoId().getMygovAnagraficaStatoId()));

    dovutoTo.setId(rs.getLong("mygov_dovuto_id"));
    dovutoTo.setCodIud(rs.getString("cod_iud"));
    dovutoTo.setCausale(rs.getString("de_rp_dati_vers_dati_sing_vers_causale_versamento"));
    dovutoTo.setCausaleVisualizzata(rs.getString("de_causale_visualizzata"));
    BigDecimal amount = rs.getBigDecimal("num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento");
    dovutoTo.setImporto(amount.toString());
    dovutoTo.setImportoAsCent(Utilities.amountAsEuroCents(amount));
    dovutoTo.setImporto(rs.getBigDecimal("num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento").toString());
    dovutoTo.setValuta("EUR");
    dovutoTo.setCodStato(rs.getString("cod_stato"));
    dovutoTo.setDeEnte(rs.getString("de_nome_ente"));
    dovutoTo.setCodIpaEnte(rs.getString("cod_ipa_ente"));
    dovutoTo.setCodiceFiscaleEnte(rs.getString("codice_fiscale_ente"));

    LocalDate dataScadenza = Utilities.toLocalDate(rs.getDate("dt_rp_dati_vers_data_esecuzione_pagamento"));

    dovutoTo.setCodTipoDovuto(rs.getString("cod_tipo_dovuto"));
    EnteTipoDovuto etd = enteTipoDovutoService.getOptionalByCodTipo(dovutoTo.getCodTipoDovuto(), dovutoTo.getCodIpaEnte(), false)
        .orElseThrow(()-> new NotFoundException("tipoDovuto not found"));
    dovutoTo.setDeTipoDovuto(etd.getDeTipo());
    dovutoTo.setDataScadenza(etd.isFlgStampaDataScadenza() ? dataScadenza : null);

    if (etd.getUrlNotificaPnd() != null && etd.getUrlNotificaAttualizzazionePnd() != null) {
      dovutoTo.setUrlNotificaPnd(true);
    }

    if (dovutoTo.getDataScadenza()!=null && dataScadenza.isBefore(LocalDate.now()) && etd.isFlgScadenzaObbligatoria()) {
      dovutoTo.setCodStato(Constants.STATO_DOVUTO_SCADUTO);
    }

    if(StringUtils.equals(dovutoTo.getCodStato(), Constants.STATO_DOVUTO_SCADUTO)) {
      if(deTipoDovutoScaduto == null)
        deTipoDovutoScaduto = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_SCADUTO, Constants.STATO_TIPO_DOVUTO).getDeStato();
      dovutoTo.setDeStato(deTipoDovutoScaduto);
    } else
      dovutoTo.setDeStato(rs.getString("de_stato"));

    String codIuv = rs.getString("cod_iuv");
    boolean flgIuvVolatile = rs.getBoolean("flg_iuv_volatile");
    if(flgIuvVolatile){
      dovutoTo.setFlgIuvVolatile(true);
      //iuv volatile: user should not se IUV; it should appear with state "pagamento in corso"
      codIuv = null;
      dovutoTo.setCodStato(Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO);
      if (deTipoDovutoPagamentoInCorso == null)
        deTipoDovutoPagamentoInCorso = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO, Constants.STATO_TIPO_DOVUTO).getDeStato();
      dovutoTo.setDeStato(deTipoDovutoPagamentoInCorso);
    }

    List<String> list;
    String codRpDatiVersTipoVersamento = rs.getString("cod_rp_dati_vers_tipo_versamento"); // NOT NULL
    if (codRpDatiVersTipoVersamento.contains(Constants.ALL_PAGAMENTI)) {
      list = new ArrayList<>(Utilities.tipiVersamento);
    }
    else {
      list = new ArrayList<>(Arrays.asList(StringUtils.split(codRpDatiVersTipoVersamento, "|")));
    }
    list.retainAll(Utilities.tipiVersamento);
    dovutoTo.setModPagamento(list);

    // ICONA AVVISO e suo ALT
    if (StringUtils.isNotBlank(codIuv)) {
      dovutoTo.setCodIuv(codIuv);
      if(codIuv.length() == Constants.IUV_GENERATOR_17_LENGTH)
        dovutoTo.setNumeroAvviso(Utilities.formatCodAvviso(Constants.SMALL_IUV_AUX_DIGIT, "", codIuv));
      else
        dovutoTo.setNumeroAvviso(Utilities.formatCodAvviso(Constants.OLD_IUV_AUX_DIGIT, rs.getString("application_code"), codIuv));
      dovutoTo.setIntestatarioAvviso(rs.getString("de_rp_sogg_pag_anagrafica_pagatore") + " - " + rs.getString("cod_rp_sogg_pag_id_univ_pag_codice_id_univoco"));
      dovutoTo.setAvviso(Utilities.isAvviso(codIuv));
    }
    else {
      dovutoTo.setAvviso(false);
    }

    /*
    // ICONA UTENTE o MULTIINTESTATARIO e SUO ALT
    String cfDovuto = dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco();
    String cfUtente = cfDovuto;
    if (SecurityContext.getUtente() != null) {
      cfUtente = SecurityContext.getUtente().getCodCodiceFiscaleUtente();
    }

    if (cfDovuto.equals(cfUtente)) {
      dovutoTo.setMultiIntestatario(false);
    }
    else {
      dovutoTo.setMultiIntestatario(true);

      // TODO lista codici fiscali altri instestatari
    }
     */

    AnagraficaPagatore intestatario = new AnagraficaPagatore();
    intestatario.setCodiceIdentificativoUnivoco(rs.getString("cod_rp_sogg_pag_id_univ_pag_codice_id_univoco"));
    intestatario.setTipoIdentificativoUnivoco(rs.getString("cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco").charAt(0)); // NOT NULL
    intestatario.setAnagrafica(rs.getString("de_rp_sogg_pag_anagrafica_pagatore"));
    intestatario.setEmail(rs.getString("de_rp_sogg_pag_email_pagatore"));
    intestatario.setIndirizzo(rs.getString("de_rp_sogg_pag_indirizzo_pagatore"));
    intestatario.setCivico(rs.getString("de_rp_sogg_pag_civico_pagatore"));
    intestatario.setCap(rs.getString("cod_rp_sogg_pag_cap_pagatore"));

    Optional<String> nomeNazione = Optional.ofNullable(rs.getString("cod_rp_sogg_pag_nazione_pagatore")).filter(StringUtils::isNotBlank).map(String::trim);
    Optional<NazioneTo> nazione = nomeNazione.map(locationService::getNazioneByCodIso);
    Optional<String> nomeProvincia;
    Optional<ProvinciaTo> provincia;
    Optional<ComuneTo> comune = Optional.empty();
    if(nazione.map(NazioneTo::hasProvince).orElse(false)){
      nomeProvincia = Optional.ofNullable(rs.getString("de_rp_sogg_pag_provincia_pagatore")).filter(StringUtils::isNotBlank).map(String::trim);
      provincia = nomeProvincia.map(locationService::getProvinciaBySigla);
      if(provincia.isPresent())
        comune = Optional.ofNullable(rs.getString("de_rp_sogg_pag_localita_pagatore")).filter(StringUtils::isNotBlank).map(String::trim)
            .map(nomeComune -> ComuneTo.builder().comune(nomeComune).build())
            .map(c -> locationService.getComuneByNameAndSiglaProvincia(c.getComune(), provincia.get().getSigla()).orElse(c));
    } else {
      nomeProvincia = Optional.empty();
      provincia = Optional.empty();
    }
    intestatario.setNazioneId(nazione.map(NazioneTo::getNazioneId).orElse(null));
    intestatario.setNazione(nazione.map(NazioneTo::getNomeNazione).or(()->nomeNazione).orElse(null));
    intestatario.setProvinciaId(provincia.map(ProvinciaTo::getProvinciaId).orElse(null));
    intestatario.setProvincia(provincia.map(ProvinciaTo::getSigla).or(()->nomeProvincia).orElse(null));
    intestatario.setLocalitaId(comune.map(ComuneTo::getComuneId).orElse(null));
    intestatario.setLocalita(comune.map(ComuneTo::getComune).orElse(null));

    dovutoTo.setIntestatario(intestatario);

    //annullabile for cittadino if isAvviso and spontaneo and stato DA_PAGARE
    dovutoTo.setAnnullabile(
      (dovutoTo.getCodStato().equals(Constants.STATO_DOVUTO_DA_PAGARE) || dovutoTo.getCodStato().equals(Constants.STATO_DOVUTO_SCADUTO)) &&
      rs.getBoolean("flg_spontaneo") &&
      dovutoTo.isAvviso()
    );

    return dovutoTo;
  }
}
