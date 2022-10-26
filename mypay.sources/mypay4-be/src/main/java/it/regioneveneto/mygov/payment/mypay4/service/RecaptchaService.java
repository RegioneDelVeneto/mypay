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

import it.regioneveneto.mygov.payment.mypay4.dto.RecaptchaResponseTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RecaptchaService {

  private final RestTemplate restTemplate;

  public RecaptchaService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  @Value("${google.recaptcha.enabled}")
  private String enabled;
  @Value("${google.recaptcha.secret.key}")
  private String recaptchaSecret;
  @Value("${google.recaptcha.verify.url}")
  private String recaptchaVerifyUrl;
  @Value("${google.recaptcha.score.threshold:0.5}")
  private double scoreThreshold;

  public boolean verify(String response, String action) {
    if(!Boolean.parseBoolean(enabled))
      return true;

    MultiValueMap<String, String> param= new LinkedMultiValueMap<>();
    param.add("secret", recaptchaSecret);
    param.add("response", response);
    try {
      RecaptchaResponseTo recaptchaResponse = this.restTemplate.postForObject(recaptchaVerifyUrl, param, RecaptchaResponseTo.class);
      boolean success = recaptchaResponse.isSuccess() &&
              recaptchaResponse.getScore() > scoreThreshold &&
              (action==null || action.equals(recaptchaResponse.getAction()));
      log.debug("recaptcha valid for action {}: {} [{}]", action, success, recaptchaResponse);
      return success;
    }catch(RestClientException e){
      log.error("error verifying recaptcha for response "+response, e);
      return false;
    }
  }
}

