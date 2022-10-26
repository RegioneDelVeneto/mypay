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

import com.fasterxml.jackson.databind.ObjectMapper;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ContentStorage {

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${cache.cacheExpirations."+ CacheService.CACHE_NAME_UPLOAD +":${cache.timeoutSeconds}}")
  private long cacheExpirations;

  @Cacheable(value= CacheService.CACHE_NAME_UPLOAD, key="{'file',#storageToken.username,#storageToken.id}", unless="#result==null")
  public byte[] getFileContent(StorageToken storageToken){
    return null;
  }

  @CachePut(value= CacheService.CACHE_NAME_UPLOAD, key="{'file',#uploadToken.username,#uploadToken.id}")
  public byte[] putFileContent(StorageToken storageToken, byte[] fileContent){
    storageToken.uploadTimestamp = System.currentTimeMillis();
    storageToken.expiryTimestamp = storageToken.uploadTimestamp + cacheExpirations * 1000;
    return fileContent;
  }

  @Cacheable(value= CacheService.CACHE_NAME_UPLOAD, key="{'object',#storageToken.username,#storageToken.id}", unless="#result==null")
  public String getObjectAsString(StorageToken storageToken){
    return null;
  }

  public <T> T deserializeString(String serializedObject, Class<T> clazz){
    try{
      return objectMapper.readValue(serializedObject, clazz);
    }catch(Exception e){
      throw new MyPayException("deserializeString", e);
    }
  }

  @CachePut(value= CacheService.CACHE_NAME_UPLOAD, key="{'object',#storageToken.username,#storageToken.id}")
  public String putObject(StorageToken storageToken, Object object){
    String storedObject;
    try{
      if(object instanceof String)
        storedObject = (String) object;
      else
        storedObject = objectMapper.writeValueAsString(object);
    } catch(Exception e){
      throw new MyPayException("putObject", e);
    }
    storageToken.uploadTimestamp = System.currentTimeMillis();
    storageToken.expiryTimestamp = storageToken.uploadTimestamp + cacheExpirations * 1000;
    return storedObject;
  }

  @CacheEvict(value= CacheService.CACHE_NAME_UPLOAD, key="{'file',#storageToken.username,#storageToken.id}")
  public void deleteStorage(StorageToken storageToken){
  }

  public StorageToken newUploadToken(String username){
    return new StorageToken(username);
  }

  public StorageToken getUploadToken(String username, String tokenId){
    return new StorageToken(username, tokenId);
  }

  public static class StorageToken {
    private final String id;
    private final String username;
    private long expiryTimestamp;
    private long uploadTimestamp;

    private StorageToken(String username){
      this(username, Utilities.getRandomUUIDWithTimestamp());
    }

    private StorageToken(String username, String tokenId){
      this.id = tokenId;
      this.username = username;
    }

    public String getId(){
      return this.id;
    }

    public long getExpiryTimestamp() {
      return expiryTimestamp;
    }

    public long getUploadTimestamp() {
      return uploadTimestamp;
    }

    public String getUsername() {
      return username;
    }
  }
}
