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
package it.regioneveneto.mygov.payment.mypay4.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaPagatore;
import it.regioneveneto.mygov.payment.mypay4.dto.ComuneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.NazioneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.ProvinciaTo;
import it.regioneveneto.mygov.payment.mypay4.exception.PaymentOrderException;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.schemas._2012.pagamenti.CtIdentificativoUnivocoPersonaFG;
import it.veneto.regione.schemas._2012.pagamenti.StTipoIdentificativoUnivocoPersFG;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtSoggettoPagatore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@Service
@Slf4j
public class AnagraficaSoggettoService {

  @Autowired
  LocationService locationService;
  @Autowired
  MessageSource messageSource;

  public AnagraficaPagatore getAnagraficaPagatore(Dovuto dovuto) {

    AnagraficaPagatore subject = AnagraficaPagatore.builder()
        .codiceIdentificativoUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco())
        .tipoIdentificativoUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco())
        .anagrafica(dovuto.getDeRpSoggPagAnagraficaPagatore())
        .email(dovuto.getDeRpSoggPagEmailPagatore())
        .indirizzo(dovuto.getDeRpSoggPagIndirizzoPagatore())
        .civico(dovuto.getDeRpSoggPagCivicoPagatore())
        .cap(dovuto.getCodRpSoggPagCapPagatore())
        .build();

    Optional<String> nomeNazione = Optional.ofNullable(dovuto.getCodRpSoggPagNazionePagatore()).filter(StringUtils::isNotBlank).map(String::trim);
    Optional<NazioneTo> nazione = nomeNazione.map(locationService::getNazioneByCodIso);
    Optional<String> nomeProvincia;
    Optional<ProvinciaTo> provincia;
    Optional<ComuneTo> comune = Optional.empty();
    if(nazione.map(NazioneTo::hasProvince).orElse(false)){
      nomeProvincia = Optional.ofNullable(dovuto.getDeRpSoggPagProvinciaPagatore()).filter(StringUtils::isNotBlank).map(String::trim);
      provincia = nomeProvincia.map(locationService::getProvinciaBySigla);
      if(provincia.isPresent())
        comune = Optional.ofNullable(dovuto.getDeRpSoggPagLocalitaPagatore()).filter(StringUtils::isNotBlank).map(String::trim)
            .map(nomeComune -> ComuneTo.builder().comune(nomeComune).build())
            .map(c -> locationService.getComuneByNameAndSiglaProvincia(c.getComune(), provincia.get().getSigla()).orElse(c));
    } else {
      nomeProvincia = Optional.empty();
      provincia = Optional.empty();
    }
    subject.setNazioneId(nazione.map(NazioneTo::getNazioneId).orElse(null));
    subject.setNazione(nazione.map(NazioneTo::getNomeNazione).or(()->nomeNazione).orElse(null));
    subject.setProvinciaId(provincia.map(ProvinciaTo::getProvinciaId).orElse(null));
    subject.setProvincia(provincia.map(ProvinciaTo::getSigla).or(()->nomeProvincia).orElse(null));
    subject.setLocalitaId(comune.map(ComuneTo::getComuneId).orElse(null));
    subject.setLocalita(comune.map(ComuneTo::getComune).orElse(null));

    return subject;
  }

  public <T> Optional<T> mapAnagraficaSoggetto(AnagraficaPagatore anag, Class<T> clazz) {
    String refToConvert;
    if (ObjectUtils.isEmpty(anag))
      return Optional.empty();
    if (clazz.equals(it.veneto.regione.schemas._2012.pagamenti.CtSoggettoPagatore.class))
      refToConvert = "Pagatore";
    else if(clazz.equals(it.veneto.regione.schemas._2012.pagamenti.CtSoggettoVersante.class))
      refToConvert = "Versante";
    else
      throw new PaymentOrderException(messageSource.getMessage("pa.carrello.creazioneRpError", null, Locale.ITALY));

    return Optional.ofNullable(anag)
        .map(e -> this.getMapOf(e, refToConvert))
        .map(map -> new ObjectMapper().convertValue(map, clazz));
  }

  public Map getMapOf(AnagraficaPagatore anag, String soggetto){
    Map map = new HashMap();
    BiConsumer <String, String> putIn = (k, v) -> map.put(k + soggetto, StringUtils.defaultString(v).trim());

    CtIdentificativoUnivocoPersonaFG ctIdentificativoUnivocoPersonaFG = new CtIdentificativoUnivocoPersonaFG();
    ctIdentificativoUnivocoPersonaFG.setTipoIdentificativoUnivoco(
        StTipoIdentificativoUnivocoPersFG.fromValue(Character.toString(anag.getTipoIdentificativoUnivoco())));
    ctIdentificativoUnivocoPersonaFG.setCodiceIdentificativoUnivoco(anag.getCodiceIdentificativoUnivoco().trim());
    map.put("identificativoUnivoco"+soggetto, ctIdentificativoUnivocoPersonaFG);

    String anagrafica = Utilities.getTruncatedAt(70).apply(anag.getAnagrafica());
    putIn.accept("anagrafica", anagrafica);

    String civico = Optional.ofNullable(anag.getCivico()).filter(StringUtils::isNotBlank).map(String::trim).orElse(null);
    putIn.accept("civico", civico);
    String indirizzo = Utilities.getTruncatedAt(70).apply(anag.getIndirizzo());
    putIn.accept("indirizzo", indirizzo);
    putIn.accept("email", anag.getEmail());


    Optional<NazioneTo> nazione = Optional.ofNullable(anag.getNazione())
      .filter(StringUtils::isNotBlank).map(String::trim)
      .map(n -> ObjectUtils.firstNonNull(locationService.getNazioneByName(n), locationService.getNazioneByCodIso(n)));
    Optional<ProvinciaTo> provincia;
    Optional<ComuneTo> comune = Optional.empty();
    if(nazione.map(NazioneTo::hasProvince).orElse(false)){
      provincia = Optional.ofNullable(anag.getProvincia()).filter(StringUtils::isNotBlank).map(String::trim).map(locationService::getProvinciaBySigla);
      if(provincia.isPresent())
        comune = Optional.ofNullable(anag.getLocalita()).filter(StringUtils::isNotBlank).map(String::trim)
          .map(nomeComune -> ComuneTo.builder().comune(nomeComune).build())
          .map(c -> locationService.getComuneByNameAndSiglaProvincia(c.getComune(), provincia.get().getSigla()).orElse(c));
    } else {
      provincia = Optional.empty();
    }
    String codNazione = nazione.map(NazioneTo::getCodiceIsoAlpha2).orElse(null);
    String cap = Optional.ofNullable(anag.getCap())
      .filter(s -> Utilities.isValidCAP(s, codNazione))
      .orElse(null);
    putIn.accept("cap", cap);

    putIn.accept("localita", comune.map(ComuneTo::getComune).orElse(null));
    putIn.accept("provincia", provincia.map(ProvinciaTo::getSigla).orElse(null));
    putIn.accept("nazione", codNazione);

    map.values().removeIf(ObjectUtils::isEmpty);

    return map;
  }

  public AnagraficaPagatore fromSoggettoPagatore(CtSoggettoPagatore soggettoPagatore){

    Optional<NazioneTo> nazioneTo = Optional.ofNullable(soggettoPagatore.getNazionePagatore())
      .filter(s -> s.length()==2)
      .map(locationService::getNazioneByCodIso);
    Optional<ProvinciaTo> provinciaTo = nazioneTo.filter(NazioneTo::hasProvince)
      .map(x -> soggettoPagatore.getProvinciaPagatore())
      .filter(s -> s.length()==2)
      .map(locationService::getProvinciaBySigla);
    Optional<ComuneTo> comuneTo = provinciaTo
      .filter(p -> StringUtils.isNotBlank(soggettoPagatore.getLocalitaPagatore()))
      .map(x -> Pair.of(x.getSigla(), soggettoPagatore.getLocalitaPagatore()))
      .flatMap(x -> locationService.getComuneByNameAndSiglaProvincia(x.getRight(), x.getLeft()));

    return AnagraficaPagatore.builder()
      .anagrafica(soggettoPagatore.getAnagraficaPagatore())
      .cap(soggettoPagatore.getCapPagatore())
      .civico(soggettoPagatore.getCivicoPagatore())
      .email(soggettoPagatore.getEMailPagatore())
      .codiceIdentificativoUnivoco(soggettoPagatore.getIdentificativoUnivocoPagatore()!=null ?
          soggettoPagatore.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco() : null)
      .tipoIdentificativoUnivoco(soggettoPagatore.getIdentificativoUnivocoPagatore()!=null ?
          soggettoPagatore.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().value().charAt(0) : null)
      .indirizzo(soggettoPagatore.getIndirizzoPagatore())
      .nazione(soggettoPagatore.getNazionePagatore())
      .nazioneId(nazioneTo.map(NazioneTo::getNazioneId).orElse(null))
      .provincia(soggettoPagatore.getProvinciaPagatore())
      .provinciaId(provinciaTo.map(ProvinciaTo::getProvinciaId).orElse(null))
      .localita(soggettoPagatore.getLocalitaPagatore())
      .localitaId(comuneTo.map(ComuneTo::getComuneId).orElse(null))
      .build();
  }
}
