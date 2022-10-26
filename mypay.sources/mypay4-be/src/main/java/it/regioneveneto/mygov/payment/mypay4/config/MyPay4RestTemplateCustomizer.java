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

@Configuration
@Slf4j
public class MyPay4RestTemplateCustomizer {

  @Bean
  public RestTemplateCustomizer customRestTemplateCustomizer(Environment env) {
    return restTemplate -> {
      log.debug("starting MyPay4RestTemplateCustomizer");
      final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
      log.debug("check if proxy support needed");
      factory.setHttpClient(HttpClientBuilderHelper.create().addProxySupport(env).build());
      restTemplate.setRequestFactory(factory);
    };
  }

}
