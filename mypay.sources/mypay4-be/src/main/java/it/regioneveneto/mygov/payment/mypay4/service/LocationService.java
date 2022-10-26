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

import it.regioneveneto.mygov.payment.mypay4.dao.LocationDao;
import it.regioneveneto.mygov.payment.mypay4.dto.ComuneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.NazioneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.ProvinciaTo;
import it.regioneveneto.mygov.payment.mypay4.model.Comune;
import it.regioneveneto.mygov.payment.mypay4.model.Nazione;
import it.regioneveneto.mygov.payment.mypay4.model.Provincia;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocationService {

  @Autowired
  private LocationDao locationDao;

  @Cacheable(value=CacheService.CACHE_NAME_NAZIONE_TO, key="{'id',#id}", unless="#result==null")
  public NazioneTo getNazione(Long id) {
    return mapToDto(locationDao.getNazione(id));
  }

  @Cacheable(value=CacheService.CACHE_NAME_ALL_OBJECTS, key="{#root.method}")
  public List<NazioneTo> getNazioni() {
    final List<Nazione> nazioni = locationDao.getNazioni();
    //place Italia entry at first place
    nazioni.stream().filter(nazione -> StringUtils.equalsIgnoreCase("IT",nazione.getCodiceIsoAlpha2())).findFirst().ifPresent( italy -> {
      nazioni.remove(italy);
      nazioni.add(0, italy);
    });
    return nazioni.stream().map(this::mapToDto).collect(Collectors.toList());
  }

  @Cacheable(value=CacheService.CACHE_NAME_NAZIONE_TO, key="{'codIso',#codIso.toUpperCase()}", unless="#result==null")
  public NazioneTo getNazioneByCodIso(final String codIso) {
    return mapToDto(locationDao.getNazioneByCodIso(codIso.toUpperCase()));
  }
  @Cacheable(value=CacheService.CACHE_NAME_NAZIONE_TO, key="{'name',#name.toUpperCase()}", unless="#result==null")
  public NazioneTo getNazioneByName(final String name) {
    return mapToDto(locationDao.getNazioneByName(name.toUpperCase()));
  }

  @Cacheable(value=CacheService.CACHE_NAME_PROVINCIA_TO, key="{'id',#id}", unless="#result==null")
  public ProvinciaTo getProvincia(Long id) {
    return mapToDto(locationDao.getProvincia(id));
  }

  @Cacheable(value=CacheService.CACHE_NAME_ALL_OBJECTS, key="{#root.method}")
  public List<ProvinciaTo> getProvince() {
    return locationDao.getProvince().stream().map(this::mapToDto).collect(Collectors.toList());
  }

  @Cacheable(value=CacheService.CACHE_NAME_PROVINCIA_TO, key="{'sigla',#sigla.toUpperCase()}", unless="#result==null")
  public ProvinciaTo getProvinciaBySigla(final String sigla) {
    return mapToDto(locationDao.getProvinciaBySigla(sigla.toUpperCase()));
  }

  @Cacheable(value=CacheService.CACHE_NAME_COMUNE_TO, key="{'id',#id}", unless="#result==null")
  public ComuneTo getComune(Long id) {
    return mapToDto(locationDao.getComune(id));
  }

  @Cacheable(value=CacheService.CACHE_NAME_COMUNE_TO, key="{'provinciaId',#provinciaId}", unless="#result==null")
  public List<ComuneTo> getComuniByProvincia(Long provinciaId) {
    return locationDao.getComuniByProvincia(provinciaId).stream().map(this::mapToDto).collect(Collectors.toList());
  }

  @Cacheable(value=CacheService.CACHE_NAME_COMUNE_TO, key="{'comune',#comune.toUpperCase()}", unless="#result==null")
  public List<ComuneTo> getComuneByName(String comune) {
    return locationDao.getComuneByName(comune.toUpperCase()).stream().map(this::mapToDto).collect(Collectors.toList());
  }

  @Cacheable(value=CacheService.CACHE_NAME_COMUNE_TO, key="{'comune+sigla',#comune.toUpperCase(),#sigla.toUpperCase()}", unless="#result==null")
  public Optional<ComuneTo> getComuneByNameAndSiglaProvincia(String comune, String sigla) {
    return locationDao.getComuneByNameAndSiglaProvincia(comune.toUpperCase(), sigla.toUpperCase()).map(this::mapToDto);
  }

  public NazioneTo mapToDto(Nazione nazione){
    return nazione==null ? null : NazioneTo.builder()
      .nazioneId(nazione.getNazioneId())
      .nomeNazione(nazione.getNomeNazione())
      .codiceIsoAlpha2(nazione.getCodiceIsoAlpha2())
      .build();
  }
  public ProvinciaTo mapToDto(Provincia provincia){
    return provincia==null ? null : ProvinciaTo.builder()
      .provinciaId(provincia.getProvinciaId())
      .provincia(provincia.getProvincia())
      .sigla(provincia.getSigla())
      .build();
  }
  public ComuneTo mapToDto(Comune comune){
    return comune==null ? null : ComuneTo.builder()
      .comuneId(comune.getComuneId())
      .comune(comune.getComune())
      .provinciaId(comune.getProvinciaId())
      .build();
  }
}
