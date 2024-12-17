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

import it.regioneveneto.mygov.payment.mypay4.util.HttpClientBuilderHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.Objects;

@Configuration
@Slf4j
public class MyPay4RestTemplateCustomizer {

  private static final ThreadLocal<Boolean> disableRedirectHandling = ThreadLocal.withInitial(()->Boolean.FALSE);
  private static final ThreadLocal<String> customSSLSupport = ThreadLocal.withInitial(()->null);

  private static final ThreadLocal<int[]> customTimeout = ThreadLocal.withInitial(()->null);

  public static void setDisableRedirectHandling(){
    log.info("setDisableRedirectHandling to TRUE");
    disableRedirectHandling.set(Boolean.TRUE);
  }

  public static void setCustomSSLSupport(String prefix){
    log.info("setCustomSSLSupport to [{}]", prefix);
    customSSLSupport.set(prefix);
  }

  public static void setCustomTimeout(int connectTimeout, int readTimeout){
    int[] customTimeoutValue = { connectTimeout, readTimeout };
    log.info("setCustomTimeout to connect[{}] read[{}]", connectTimeout, readTimeout);
    customTimeout.set(customTimeoutValue);
  }

  @Bean
  public RestTemplateCustomizer customRestTemplateCustomizer(Environment env) {
    return restTemplate -> {
      log.debug("starting MyPay4RestTemplateCustomizer");
      log.debug("check if proxy support needed");
      HttpClientBuilderHelper httpClientBuilderHelper = HttpClientBuilderHelper.create(env).addProxySupport();
      if(Objects.equals(disableRedirectHandling.get(), Boolean.TRUE)) {
        log.info("creating HttpComponentsClientHttpRequestFactory with disableRedirectHandling=TRUE");
        httpClientBuilderHelper.disableRedirectHandling();
        disableRedirectHandling.set(Boolean.FALSE);
      }
      if(customSSLSupport.get()!=null) {
        log.info("creating HttpComponentsClientHttpRequestFactory with customSSLSupport[{}]", customSSLSupport.get());
        httpClientBuilderHelper.addCustomSSLSupport(customSSLSupport.get());
        customSSLSupport.remove();
      }

      final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
      factory.setHttpClient(httpClientBuilderHelper.build());
      if(customTimeout.get()!=null) {
        int connectTimeout = customTimeout.get()[0];
        int readTimeout = customTimeout.get()[1];
        log.info("creating HttpComponentsClientHttpRequestFactory with customTimeout - connect[{}] read[{}]", connectTimeout, readTimeout);
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        customTimeout.remove();
      }
      restTemplate.setRequestFactory(factory);
    };
  }

}
