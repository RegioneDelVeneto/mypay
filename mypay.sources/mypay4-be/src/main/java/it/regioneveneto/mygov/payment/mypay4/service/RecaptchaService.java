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
import it.regioneveneto.mygov.payment.mypay4.exception.RecaptchaFallbackException;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

  public static final String V_2 = "V2|";
  public static final String V_2_FORCE = "V2Force|";
  private final RestTemplate restTemplate;

  public RecaptchaService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  @Value("${google.recaptcha.enabled}")
  private String enabled;
  @Value("${google.recaptcha.secret.key}")
  private String recaptchaSecret;
  @Value("${google.recaptcha.v2.enabled:false}")
  private String v2Enabled;
  @Value("${google.recaptcha.v2.secret.key:}")
  private String recaptchaSecretV2;
  @Value("${google.recaptcha.verify.url}")
  private String recaptchaVerifyUrl;
  @Value("${google.recaptcha.score.threshold:0.5}")
  private double scoreThreshold;

  public boolean verify(String response, String action) {
    if(!Boolean.parseBoolean(enabled))
      return true;

    boolean isV2Enabled = Boolean.parseBoolean(v2Enabled) && StringUtils.isNotBlank(recaptchaSecretV2);

    boolean isV2 = StringUtils.startsWith(response, V_2);
    boolean isV2Force = StringUtils.startsWith(response, V_2_FORCE);

    if(isV2) {
      if(!isV2Enabled) {
        log.warn("trying recaptcha V2 validation but V2 is not enabled or key missing");
        return false;
      }
      log.debug("performing fallback recaptcha validation V2");
      response = response.substring(V_2.length());
    }
    if(isV2Force){
      response = response.substring(V_2_FORCE.length());
    }

    MultiValueMap<String, String> param= new LinkedMultiValueMap<>();
    param.add("secret", isV2 ? recaptchaSecretV2 : recaptchaSecret);
    param.add("response", response);
    try {
      RecaptchaResponseTo recaptchaResponse = this.restTemplate.postForObject(recaptchaVerifyUrl, param, RecaptchaResponseTo.class);

      boolean successWithoutScoreCheck = recaptchaResponse!=null && recaptchaResponse.isSuccess() && ( isV2 || action.equals(recaptchaResponse.getAction()) );
      boolean scoreV3Check = !isV2 && recaptchaResponse!=null && recaptchaResponse.getScore() > scoreThreshold;

      //just for test: force use of recaptcha v2
      if(isV2Force) {
        log.warn("recaptcha validation V3, force use V2; originalScore[{}]", Utilities.ifNotNull(recaptchaResponse, RecaptchaResponseTo::getScore));
        scoreV3Check = false;
      }

      boolean success;
      if(isV2)
        success = successWithoutScoreCheck;
      else if(isV2Enabled && successWithoutScoreCheck && !scoreV3Check)
        throw new RecaptchaFallbackException("recaptcha low score: ["+recaptchaResponse.getScore()+"]", recaptchaResponse.getScore());
      else
        success = successWithoutScoreCheck && scoreV3Check;

      if(success)
        log.debug("recaptcha V2[{}] valid for action [{}]: [{}]", isV2, action, recaptchaResponse);
      else
        log.warn("recaptcha V2[{}] NOT valid for action[{}]: [{}]", isV2, action, recaptchaResponse);

      return success;
    }catch(RestClientException e){
      log.error("error verifying recaptcha for response "+response, e);
      return false;
    }
  }

}

