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

import it.regioneveneto.mygov.payment.mypay4.storage.ContentStorage;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StorageService {

  public static final String ANONYMOUS = "__ANONYMOUS__";
  public static final String WS_USER = "__WS_USER__";
  public static final String ADMIN_USER = "__ADMIN_USER__";

  @Value("${upload.max-size-MiB:1}")
  private int maxUploadSizeMib;

  @Value("${cache.cacheExpirations.uploadCache:${cache.timeoutSeconds}}")
  private long cacheExpirationSeconds;

  @Autowired
  private ContentStorage contentStorage;

  private long maxSizeBytes;
  public long getMaxSizeBytes(){
    if(maxSizeBytes ==0)
      maxSizeBytes = maxUploadSizeMib*1024*1024l;
    return maxSizeBytes;
  }

  public ContentStorage.StorageToken putFile(String username, byte[] content){
    if(content==null || content.length==0)
      throw new IllegalArgumentException("content is "+(content==null?"null":"empty"));
    else if(content.length > getMaxSizeBytes())
      throw new IllegalArgumentException("content size ["+content.length+"] exceeds allowed limit ["+getMaxSizeBytes()+"]");
    ContentStorage.StorageToken storageToken = contentStorage.newUploadToken(username);
    contentStorage.putFileContent(storageToken, content);
    return storageToken;
  }

  public byte[] getFile(String username, String tokenId){
    ContentStorage.StorageToken storageToken = contentStorage.getUploadToken(username, tokenId);
    return contentStorage.getFileContent(storageToken);
  }

  public void deleteStorage(String username, String tokenId){
    ContentStorage.StorageToken storageToken = contentStorage.getUploadToken(username, tokenId);
    contentStorage.deleteStorage(storageToken);
  }

  public ContentStorage.StorageToken putObject(String username, Object content){
    if(content==null)
      throw new IllegalArgumentException("content is null");
    ContentStorage.StorageToken storageToken = contentStorage.newUploadToken(username);
    contentStorage.putObject(storageToken, content);
    return storageToken;
  }

  public ContentStorage.StorageToken putObject(String username, String tokenId, Object content){
    if(content==null)
      throw new IllegalArgumentException("content is null");
    ContentStorage.StorageToken storageToken = contentStorage.getUploadToken(username, tokenId);
    contentStorage.putObject(storageToken, content);
    return storageToken;
  }

  public <T> Optional<T> getObject(String username, String tokenId, Class<T> clazz){
    return getString(username, tokenId).map(objectAsString -> contentStorage.deserializeString(objectAsString, clazz));
  }

  public Optional<String> getString(String username, String tokenId){
    ContentStorage.StorageToken storageToken = contentStorage.getUploadToken(username, tokenId);
    String objectAsString = contentStorage.getObjectAsString(storageToken);
    return Optional.ofNullable(objectAsString);
  }

  public boolean isTokenWithTimestampExpired(String tokenId, long graceTimeSeconds){
    return Utilities.parseDateFromUUIDWithTimestamp(tokenId).map(uuidDate -> System.currentTimeMillis() - uuidDate.getTime() > (cacheExpirationSeconds + graceTimeSeconds)*1000).orElse(false);
  }

  public boolean isTokenWithTimestampExpired(String tokenId){
    //use default grace time of 5 minutes (i.e. time after cache expired but not considering item expired)
    return isTokenWithTimestampExpired(tokenId, 300);
  }

}
