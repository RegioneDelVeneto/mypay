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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
@EnableConfigurationProperties(CacheConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cache", name = "useEmbeddedRedis", havingValue = "true")
@Slf4j
public class EmbeddedRedisConfig {
  private RedisServer redisServer;

  public EmbeddedRedisConfig(CacheConfigurationProperties properties) {
    if(properties.getType()==CacheConfigurationProperties.TYPE.standalone && properties.isUseEmbeddedRedis()) {
      log.warn("creating embedded redis server, port: " + properties.getStandalonePort());
      try {
        this.redisServer = new RedisServer(properties.getStandalonePort());
      } catch (Exception e){
        log.warn("Error creating embedded redis server, ignoring the error", e);
      }
    }
  }

  @PostConstruct
  public void postConstruct() {
    if(redisServer!=null) {
      log.warn("starting embedded redis server");
      try {
        redisServer.start();
      } catch (Exception e){
        log.warn("Error starting embedded redis server, ignoring the error", e);
      }
    }
  }

  @PreDestroy
  public void preDestroy() {
    if(redisServer!=null) {
      log.warn("stopping embedded redis server");
      redisServer.stop();
    }
  }
}
