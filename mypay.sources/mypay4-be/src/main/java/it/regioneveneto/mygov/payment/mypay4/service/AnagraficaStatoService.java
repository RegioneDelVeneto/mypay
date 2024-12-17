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

import it.regioneveneto.mygov.payment.mypay4.dao.AnagraficaStatoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaStatoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AnagraficaStatoService {

  private static final String TIPO_STATO_ENTE = "ente";

  @Resource
  private AnagraficaStatoService self;

  @Autowired
  private AnagraficaStatoDao anagraficaStatoDao;

  private Map<String, Long> mapIdByTipoAndCode = new HashMap<>();

  public List<AnagraficaStato> getStatoEnteForSelect() {
    return self.getByTipoStato(TIPO_STATO_ENTE);
  }

  // since these are reference data that never change, it's possible to assume they can be stored locally to improve performance
  public Long getIdByTipoAndCode(String deTipoStato, String codStato) {
    return Optional.ofNullable(mapIdByTipoAndCode.computeIfAbsent(deTipoStato+"|"+codStato, key ->
        self.getByCodStatoAndTipoStato(codStato, deTipoStato).getMygovAnagraficaStatoId() ) ).orElseThrow(NotFoundException::new);
  }

  @Cacheable(value=CacheService.CACHE_NAME_ANAGRAFICA_STATO, key="{'codStato+deTipoStato',#codStato,#deTipoStato}", unless="#result==null")
  public AnagraficaStato getByCodStatoAndTipoStato(String codStato, String deTipoStato) {
    return anagraficaStatoDao.getByCodStatoAndTipoStato(codStato, deTipoStato);
  }

  @Cacheable(value=CacheService.CACHE_NAME_ANAGRAFICA_STATO, key="{'deTipoStato',#deTipoStato}", unless="#result==null")
  public List<AnagraficaStato> getByTipoStato(String deTipoStato) {
    return anagraficaStatoDao.getByTipoStato(deTipoStato);
  }

  @Cacheable(value=CacheService.CACHE_NAME_ANAGRAFICA_STATO, key="{'id',#mygovAnagraficaStatoId}", unless="#result==null")
  public AnagraficaStato getById(Long mygovAnagraficaStatoId) {
    return anagraficaStatoDao.getById(mygovAnagraficaStatoId);
  }

  public AnagraficaStato getByCodiceEsitoPagamento(int code, String deTipoStato) {
    Map<Integer,String> map = Map.of(
        0, Constants.STATO_CARRELLO_PAGATO,
        1, Constants.STATO_CARRELLO_NON_PAGATO,
        2, Constants.STATO_CARRELLO_PARZIALMENTE_PAGATO,
        3, Constants.STATO_CARRELLO_DECORRENZA_TERMINI,
        4, Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE
    );
    String codStato = map.get(code);
    return anagraficaStatoDao.getByCodStatoAndTipoStato(codStato, deTipoStato);
  }

  public static AnagraficaStatoTo mapToDto(AnagraficaStato anagraficaStato){
    return AnagraficaStatoTo.builder()
        .mygovAnagraficaStatoId(anagraficaStato.getMygovAnagraficaStatoId())
        .codStato(anagraficaStato.getCodStato())
        .deStato(anagraficaStato.getDeStato())
        .build();
  }
}

