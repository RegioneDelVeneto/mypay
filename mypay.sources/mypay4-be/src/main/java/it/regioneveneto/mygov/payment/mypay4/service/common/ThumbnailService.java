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

import it.regioneveneto.mygov.payment.mypay4.dto.StringWithHashTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
public class ThumbnailService {

  @Resource
  ThumbnailService self;

  @Bean("thumbnailCacheKeyGenerator")
  public KeyGenerator getThumbnailCacheKeyGenerator(){
    return new ThumbnailCacheKeyGenerator();
  }

  @Cacheable(value=CacheService.CACHE_NAME_THUMBNAIL, keyGenerator = "thumbnailCacheKeyGenerator", unless="#result==null")
  public Optional<StringWithHashTo> generateThumbnail(String imgData){
    if(StringUtils.isBlank(imgData))
      return Optional.empty();
    try {
      log.debug("generating thumbnail ofr base64 img; image size: "+imgData.length()+" char");
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(imgData)));
      BufferedImage thumb = Scalr.resize(image, 48);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(thumb, "png", Base64.getEncoder().wrap(os));
      String thumbnail = os.toString(StandardCharsets.ISO_8859_1);
      String hash = ThumbnailCacheKeyGenerator.currentKeyTL.get();
      log.debug("thumbnail size: {} chars - hash: {}",thumbnail.length(),hash);
      self.putHashCache(imgData, hash);
      return Optional.of(new StringWithHashTo(thumbnail, hash));
    } catch (final IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }

  @Cacheable(value=CacheService.CACHE_NAME_THUMBNAIL_HASH, keyGenerator = "thumbnailCacheKeyGenerator", unless="#result==null")
  public Optional<String> getThumbnailHash(String imgData){
    if(StringUtils.isBlank(imgData))
      return Optional.empty();
    Optional<StringWithHashTo> stringWithHashTo = self.generateThumbnail(imgData);
    return stringWithHashTo.map(StringWithHashTo::getHash);
  }

  @CachePut(value = CacheService.CACHE_NAME_THUMBNAIL_HASH, key = "#imgData")
  public String putHashCache(String imgData, String hash){
    return hash;
  }
}

class ThumbnailCacheKeyGenerator implements KeyGenerator{

  static ThreadLocal<String> currentKeyTL = new InheritableThreadLocal<>();

  @Override
  public Object generate(Object target, Method method, Object... params) {
    String hash;
    if(params.length == 0 || params[0] == null)
      hash = "";
    else
      hash = Integer.toString(MurmurHash.hash(((String)params[0]).getBytes(), 1));
    ThumbnailCacheKeyGenerator.currentKeyTL.set(hash);
    return hash;
  }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This is a very fast, non-cryptographic hash suitable for general hash-based
 * lookup.  See http://murmurhash.googlepages.com/ for more details.
 *
 * <p>The C version of MurmurHash 2.0 found at that site was ported
 * to Java by Andrzej Bialecki (ab at getopt org).</p>
 */
class MurmurHash {
  public static int hash(byte[] data, int seed) {
    int m = 0x5bd1e995;
    int r = 24;

    int h = seed ^ data.length;

    int len = data.length;
    int len_4 = len >> 2;

    for (int i = 0; i < len_4; i++) {
      int i_4 = i << 2;
      int k = data[i_4 + 3];
      k = k << 8;
      k = k | (data[i_4 + 2] & 0xff);
      k = k << 8;
      k = k | (data[i_4 + 1] & 0xff);
      k = k << 8;
      k = k | (data[i_4 + 0] & 0xff);
      k *= m;
      k ^= k >>> r;
      k *= m;
      h *= m;
      h ^= k;
    }

    int len_m = len_4 << 2;
    int left = len - len_m;

    if (left != 0) {
      if (left >= 3) {
        h ^= (int) data[len - 3] << 16;
      }
      if (left >= 2) {
        h ^= (int) data[len - 2] << 8;
      }
      if (left >= 1) {
        h ^= (int) data[len - 1];
      }

      h *= m;
    }

    h ^= h >>> 13;
    h *= m;
    h ^= h >>> 15;

    return h;
  }

  /* Testing ...
  static int NUM = 1000;

  public static void main(String[] args) {
    byte[] bytes = new byte[4];
    for (int i = 0; i < NUM; i++) {
      bytes[0] = (byte)(i & 0xff);
      bytes[1] = (byte)((i & 0xff00) >> 8);
      bytes[2] = (byte)((i & 0xff0000) >> 16);
      bytes[3] = (byte)((i & 0xff000000) >> 24);
      System.out.println(Integer.toHexString(i) + " " + Integer.toHexString(hash(bytes, 1)));
    }
  } */
}
