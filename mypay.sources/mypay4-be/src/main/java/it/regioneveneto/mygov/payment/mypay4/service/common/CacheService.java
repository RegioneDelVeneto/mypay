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
package it.regioneveneto.mygov.payment.mypay4.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CacheService {

  public final static String CACHE_NAME_ALL_OBJECTS = "allObjectCache";
  public final static String CACHE_NAME_ANAGRAFICA_STATO = "anagraficaStatoCache";
  public final static String CACHE_NAME_CHIEDI_COPIA_ESITO = "processingInfoChiediCopiaEsitoCache";
  public final static String CACHE_NAME_CHIEDI_STATO_RPT = "processingInfoChiediStatoRptCache";
  public final static String CACHE_NAME_COMUNE_TO = "comuneToCache";
  public final static String CACHE_NAME_ENTE = "enteCache";
  public final static String CACHE_NAME_ENTE_TIPO_DOVUTO = "enteTipoDovutoCache";
  public final static String CACHE_NAME_FESP_ENTE = "fespEnteCache";
  public final static String CACHE_NAME_FESP_TIPI_VERSAMENTO = "fespTipiVersamentoCache";
  public final static String CACHE_NAME_FLUSSO = "flussoCache";
  public final static String CACHE_NAME_MY_DICTIONARY_XSD = "mydictionaryXsdCache";
  public final static String CACHE_NAME_NAZIONE_TO = "nazioneToCache";
  public final static String CACHE_NAME_PROVINCIA_TO = "provinciaToCache";
  public final static String CACHE_NAME_TASSONOMIA = "tassonomiaCache";
  public final static String CACHE_NAME_THUMBNAIL = "thumbnailCache";
  public final static String CACHE_NAME_THUMBNAIL_HASH = "thumbnailHashCache";
  public final static String CACHE_NAME_TOKEN_USAGE = "jwtTokenUsageCache";
  public final static String CACHE_NAME_UPLOAD = "uploadCache";
  public final static String CACHE_NAME_UTENTE = "utenteCache";
  public final static String CACHE_NAME_UTENTE_PROFILE = "UtenteProfileCache";

  public final static Set<String> CACHE_NAMES = Set.of(
    CACHE_NAME_ALL_OBJECTS,
    CACHE_NAME_ANAGRAFICA_STATO,
    CACHE_NAME_CHIEDI_COPIA_ESITO,
    CACHE_NAME_CHIEDI_STATO_RPT,
    CACHE_NAME_COMUNE_TO,
    CACHE_NAME_ENTE,
    CACHE_NAME_ENTE_TIPO_DOVUTO,
    CACHE_NAME_FESP_ENTE,
    CACHE_NAME_FESP_TIPI_VERSAMENTO,
    CACHE_NAME_FLUSSO,
    CACHE_NAME_MY_DICTIONARY_XSD,
    CACHE_NAME_NAZIONE_TO,
    CACHE_NAME_PROVINCIA_TO,
    CACHE_NAME_TASSONOMIA,
    CACHE_NAME_THUMBNAIL,
    CACHE_NAME_THUMBNAIL_HASH,
    CACHE_NAME_TOKEN_USAGE,
    CACHE_NAME_UPLOAD,
    CACHE_NAME_UTENTE,
    CACHE_NAME_UTENTE_PROFILE
  );
  public final List<String> CACHE_TO_PRESERVE = List.of(
    CACHE_NAME_UPLOAD,
    CACHE_NAME_CHIEDI_COPIA_ESITO,
    CACHE_NAME_CHIEDI_STATO_RPT
  );

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RedisTemplate<String, ?> redisTemplate;

  @Value("${cache.cachePrefix:}")
  private String cachePrefix;

  public String cacheFlush(){
    try {
      String msg = CACHE_NAMES
        .stream()
        .filter( cacheName -> ! CACHE_TO_PRESERVE.contains(cacheName) )
        .peek(c -> cacheManager.getCache(c).clear())
        .collect(Collectors.joining("; ", "flushed caches: ", ""));
      log.info(msg);
      return msg;
    } catch(Exception e){
      log.warn("error flushing cache", e);
      return "cache not flushed: "+e;
    }
  }

  public String cacheFlush(String cacheName){
    try {
      String responseMsg;
      if(StringUtils.stripToNull(cacheName)==null){
        responseMsg = "cache name empty";
      } else {
        Cache cache = null;
        if(CACHE_NAMES.contains(cacheName))
          cache = cacheManager.getCache(cacheName);
        if (cache != null) {
          cache.clear();
          log.info("flushed cache {}", cacheName);
          responseMsg = "OK";
        } else {
          log.info("not flushed (not found) cache {}", cacheName);
          responseMsg = "not found";
        }
      }

      return responseMsg;
    } catch(Exception e){
      log.warn("error flushing cache {}", cacheName, e);
      return "error: "+e;
    }
  }

  public String cacheFlush(String cacheName, String cacheKey){
    try {
      String responseMsg;
      if(StringUtils.stripToNull(cacheName)==null){
        responseMsg = "cache name empty";
      } else {
        Cache cache = null;
        if(CACHE_NAMES.contains(cacheName))
          cache = cacheManager.getCache(cacheName);
        if (cache != null) {
          boolean present = Optional.ofNullable(cache.get(cacheKey)).map(Cache.ValueWrapper::get).isPresent();
          cache.evict(cacheKey);
          log.info("flushed cache [{}] - key [{}] - present [{}]", cacheName, cacheKey, present);
          responseMsg = present ? "OK" : "key not found";
        } else {
          log.info("not flushed (cache not found) cache [{}] - key [{}]", cacheName, cacheKey);
          responseMsg = "cache not found";
        }
      }

      return responseMsg;
    } catch(Exception e){
      log.warn("error flushing cache [{}] - key [{}]", cacheName, cacheKey, e);
      return "error: "+e;
    }
  }

  public String cacheGet(){
    try {
      String responseMsg = CACHE_NAMES.stream().collect(Collectors.joining("<br>\n"));
      return responseMsg;
    } catch(Exception e){
      log.warn("error getting cache names", e);
      return "error: "+e;
    }
  }

  public String cacheGet(String cacheName){
    try {
      String responseMsg;
      if(StringUtils.stripToNull(cacheName)==null){
        responseMsg = "cache name empty";
      } else {
        responseMsg = Optional.of(cacheName)
          .filter(CACHE_NAMES::contains)
          .map(cacheManager::getCache)
          .map(cache -> redisTemplate.keys(StringUtils.isBlank(cachePrefix)?"":(cachePrefix+":") + cacheName + "::*"))
          .map(cacheKeys -> cacheKeys.size() + " elements found" + (cacheKeys.size() > 0 ? cacheKeys.stream()
            .map(x -> StringUtils.substringAfter(x, "::"))
            .collect(Collectors.joining("<br>\n", ":<br>\n", "")) : "") )
          .orElse("cache not found");
      }
      return responseMsg;
    } catch(Exception e){
      log.warn("error getting cache [{}]", cacheName, e);
      return "error: "+e;
    }
  }

  public String cacheGet(String cacheName, String cacheKey){
    try {
      String responseMsg;
      if(StringUtils.stripToNull(cacheName)==null){
        responseMsg = "cache name empty";
      } else {
        Cache cache = null;
        if(CACHE_NAMES.contains(cacheName))
          cache = cacheManager.getCache(cacheName);
        if (cache != null) {
          responseMsg = Optional.ofNullable(cache.get(cacheKey)).map(Cache.ValueWrapper::get)
            .map(value -> {
              try{
                return objectMapper.writeValueAsString(value);
              }catch(JsonProcessingException e){
                log.warn("error getting cache [{}] - key [{}]", cacheName, cacheKey, e);
                return "error: "+e;
              }
            })
            .orElse("value not found");
        } else {
          responseMsg = "cache not found";
        }
      }
      return responseMsg;
    } catch(Exception e){
      log.warn("error getting cache [{}] - key [{}]", cacheName, cacheKey, e);
      return "error: "+e;
    }
  }
}
