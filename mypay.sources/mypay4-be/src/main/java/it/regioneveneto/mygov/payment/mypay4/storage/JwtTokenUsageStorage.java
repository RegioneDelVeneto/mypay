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
package it.regioneveneto.mygov.payment.mypay4.storage;

import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtTokenUsageStorage {

  @Cacheable(value= CacheService.CACHE_NAME_TOKEN_USAGE, key="{'usage',#jti}", unless="#result==null")
  public Long getTokenUsageTime(String jti){
    log.debug("getTokenUsageTime: "+jti);
    return null;
  }

  @CachePut(value= CacheService.CACHE_NAME_TOKEN_USAGE, key="{'usage',#jti}")
  public Long markTokenUsed(String jti){
    log.debug("markTokenUsed: "+jti);
    return System.currentTimeMillis();
  }

  @Cacheable(value= CacheService.CACHE_NAME_TOKEN_USAGE, key="{'reqUid',#jti}", unless="#result==null")
  public String getTokenUsageReqUid(String jti){
    log.debug("getTokenUsageReqUid: "+jti);
    return null;
  }

  @CachePut(value= CacheService.CACHE_NAME_TOKEN_USAGE, key="{'reqUid',#jti}")
  public String markTokenUsedReqUid(String jti, String reqUid){
    log.debug("markTokenUsedReqUid: "+jti);
    return reqUid;
  }

  @Cacheable(value= CacheService.CACHE_NAME_TOKEN_USAGE, key="{'rolling',#jti}", unless="#result==null")
  public Long wasTokenRolled(String jti){
    log.debug("getRollingToken: "+jti);
    return null;
  }

  @CachePut(value= CacheService.CACHE_NAME_TOKEN_USAGE, key="{'rolling',#jti}")
  public Long markTokenRolled(String jti){
    log.debug("setRollingToken: "+jti);
    return System.currentTimeMillis();
  }
}
