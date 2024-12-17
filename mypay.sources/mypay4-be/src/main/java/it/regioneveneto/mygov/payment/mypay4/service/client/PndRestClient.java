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
package it.regioneveneto.mygov.payment.mypay4.service.client;


import it.regioneveneto.mygov.payment.mypay4.config.MyPay4RestTemplateCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PndRestClient {

  private final RestTemplate restTemplate;

  private final RetryTemplate retryTemplate;

  public PndRestClient(RestTemplateBuilder restTemplateBuilder, ConfigurableEnvironment env) {
    int connectTimeout = env.getProperty("pa.amountUpdate.connectTimeoutSeconds", Integer.class, 5) * 1000;
    int readTimeout = env.getProperty("pa.amountUpdate.readTimeoutSeconds", Integer.class, 5) * 1000;
    MyPay4RestTemplateCustomizer.setCustomTimeout(connectTimeout, readTimeout);
    this.restTemplate = restTemplateBuilder.build();
    log.info("created restTemplate for Pnd - amount update - connectTimeoutSeconds[{}] readTimeoutSeconds[{}]", connectTimeout, readTimeout);

    int maxAttempts = env.getProperty("pa.amountUpdate.maxAttempts", Integer.class, 1);
    int backOffPeriod = env.getProperty("pa.amountUpdate.backOffPeriod", Integer.class, 2000);
    this.retryTemplate = new RetryTemplate();
    FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(backOffPeriod);
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(maxAttempts);
    retryTemplate.setRetryPolicy(retryPolicy);
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
    log.info("created retryTemplate for Pnd - amount update - maxAttempts[{}] backOffPeriod[{}]", maxAttempts, backOffPeriod);
  }


  public Map<String, Object> pndAutenticazione(String urlAutenticazionePnd, String userPnd, String pswPnd) {
    try {
      Map<String, String> body = Map.of(
          "username", userPnd,
          "password", pswPnd
      );

      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(List.of(MediaType.APPLICATION_JSON));
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

      Map<String, Object> response = retryTemplate.execute(context -> restTemplate.postForObject(urlAutenticazionePnd, request, Map.class));

      log.debug("response:{}", response);
      return response;

    } catch (Exception e) {
      log.error("error in pndAutenticazione", e);
      return Map.of("errore", "Errore in PND Autenticazione");
    }
  }



  public Map<String, Object> pndAttualizzazione(String urlPndAttualizzazione, String token, String cfEnteCreditore, String numeroAvviso) {
    try {

      Map<String, String> body = Map.of(
          "cfEnteCreditore", cfEnteCreditore,
          "importoPosizione", "S",
          "numeroAvviso", numeroAvviso
      );

      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(List.of(MediaType.APPLICATION_JSON));
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(token);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

      Map<String, Object> response = retryTemplate.execute(context -> restTemplate.postForObject(urlPndAttualizzazione, request, Map.class));

      log.debug("response:{}", response);
      return response;

    } catch (Exception e) {
      log.error("error in pndAttualizzazione", e);
      return Map.of("errore", "Errore in PND Attualizzazione");
    }
  }

}


