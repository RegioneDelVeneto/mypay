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
package it.regioneveneto.mygov.payment.mypay4.config;

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(CacheConfigurationProperties.class)
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {

  @Value("${cache.clienttimeout.milliseconds:5000}")
  private long clientTimeout;

  @SuppressWarnings("java:S1872")
  private RedisCacheConfiguration createCacheConfiguration(CacheConfigurationProperties properties, String cacheName) {
    long timeoutInSeconds;
    if(cacheName==null){
      timeoutInSeconds = properties.getTimeoutSeconds();
    } else {
      Assert.notNull(cacheName, "cache name cannot be null");
      timeoutInSeconds = properties.getCacheExpirations().get(cacheName);
      Assert.notNull(properties.getCacheExpirations().get(cacheName), "cache timeout cannot be null");
    }
    RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofSeconds(timeoutInSeconds))
        .prefixCacheNameWith(properties.getCachePrefix()+":");

    // to fix issue Redis serialization with Spring Devtools https://github.com/spring-projects/spring-boot/issues/9444
    if("org.springframework.boot.devtools.restart.classloader.RestartClassLoader".equals(getClass().getClassLoader().getClass().getName())) {
      if(cacheName==null)
        log.warn("Spring Boot devtools enabled -> using Redis JdkSerializationRedisSerializer");
      RedisSerializer<Object> redisSerializer = new JdkSerializationRedisSerializer(getClass().getClassLoader());
      redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));
    }
    return redisCacheConfiguration;
  }

  @Bean
  public RedisCacheConfiguration cacheConfiguration(CacheConfigurationProperties properties) {
    return createCacheConfiguration(properties, null);
  }

  @Bean
  public LettuceConnectionFactory redisConnectionFactory(CacheConfigurationProperties properties) {
    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
      .commandTimeout(Duration.ofMillis(clientTimeout))
      .build();
    switch (properties.getType()) {
      case sentinel:
        log.info("Redis (/Lettuce) sentinel configuration enabled. With cache timeout {} seconds", properties.getTimeoutSeconds());
        RedisSentinelConfiguration redisSentinelConfig = new RedisSentinelConfiguration(properties.getSentinelMaster(),
            new HashSet<>(properties.getSentinelNodes()));
        redisSentinelConfig.setPassword(properties.getPassword());
        return new LettuceConnectionFactory(redisSentinelConfig,clientConfig);
      case cluster:
        log.info("Redis (/Lettuce) cluster configuration enabled. With cache timeout {} seconds", properties.getTimeoutSeconds());
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(properties.getClusterNodes());
        redisClusterConfiguration.setMaxRedirects(properties.getClusterMaxRedirects());
        if(StringUtils.isNotBlank(properties.getPassword()))
          redisClusterConfiguration.setPassword(properties.getPassword());
        return new LettuceConnectionFactory(redisClusterConfiguration,LettuceClientConfiguration.defaultConfiguration());
      case standalone:
        log.info("Redis (/Lettuce) standalone configuration enabled. With cache timeout {} seconds", properties.getTimeoutSeconds());
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(properties.getStandaloneHost());
        redisStandaloneConfiguration.setPort(properties.getStandalonePort());
        if(StringUtils.isNotBlank(properties.getPassword()))
          redisStandaloneConfiguration.setPassword(properties.getPassword());

        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
      default:
        throw new MyPayException("invalid redis configuration type: "+properties.getType());
    }
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(cf);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    return redisTemplate;
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, CacheConfigurationProperties properties) {
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

    for (Map.Entry<String, Long> cacheNameAndTimeout : properties.getCacheExpirations().entrySet()) {
      cacheConfigurations.put(cacheNameAndTimeout.getKey(), createCacheConfiguration(properties, cacheNameAndTimeout.getKey()));
      log.info("create custom cache configuration name: "+cacheNameAndTimeout.getKey()+" - timeout: "+cacheNameAndTimeout.getValue()+" seconds");
    }

    return RedisCacheManager
        .builder(redisConnectionFactory)
        .cacheDefaults(cacheConfiguration(properties))
        .withInitialCacheConfigurations(cacheConfigurations).build();
  }

}

@ConfigurationProperties(prefix = "cache")
@Data
class CacheConfigurationProperties {

  @SuppressWarnings("java:S115")
  public enum TYPE { standalone, cluster, sentinel }

  private TYPE type = TYPE.standalone;

  //sentinel properties
  private String sentinelMaster;
  private List<String> sentinelNodes;

  //cluster properties
  private List<String> clusterNodes;
  private int clusterMaxRedirects = 3;

  //standalone properties
  private boolean useEmbeddedRedis = false;
  private String standaloneHost = "localhost";
  private int standalonePort = 6379;

  //common properties
  private String password;
  private long timeoutSeconds = 60;
  private String cachePrefix = "MyPay4";
  // Mapping of cacheNames to expire-after-write timeout in seconds
  private Map<String, Long> cacheExpirations = new HashMap<>();
}