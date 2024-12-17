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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import it.regioneveneto.mygov.payment.mypay4.dto.ReceiptExportTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class MyPivotService {

  private final String baseUrl;

  private final PrivateKey mypivotPrivateKey;

  private final RestTemplate restTemplate;

  public MyPivotService(RestTemplateBuilder restTemplateBuilder, ConfigurableEnvironment env) {
    this.baseUrl = env.getProperty("a2a.mypivot.baseUrl");
    //generate private key
    try{
      String mypayJwtPrivateKey = env.getProperty("a2a.mypivot.private");
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Decoders.BASE64.decode(mypayJwtPrivateKey));
      KeyFactory kf = KeyFactory.getInstance("RSA");
      this.mypivotPrivateKey = kf.generatePrivate(keySpec);
    } catch(Exception e){
      log.error("error while generating JWT Token key for MyPivot", e);
      throw new MyPayException("error while generating JWT Token key for MyPivot", e);
    }
    this.restTemplate = restTemplateBuilder.build();
  }

  public boolean exportReceipt(ReceiptExportTo receipt){
    String url = baseUrl + String.format("a2a/ricevute-telematiche/import/2/%s", receipt.getMygovReceiptId());
    HttpHeaders headers = new HttpHeaders();
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", "a2a");
    claims.put("jti", UUID.randomUUID().toString());
    String jwtToken = Jwts.builder()
        .setClaims(claims)
        .setSubject("mypivot")
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis()+60*1000)) // 1 minute validity
        .signWith(mypivotPrivateKey).compact();
    headers.setBearerAuth(jwtToken);
    HttpEntity<ReceiptExportTo> entity = new HttpEntity<>(receipt, headers);
    log.info("invoking mypivot api to export receipt[{}]",receipt.getMygovReceiptId());
    try {
      ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
      if (response.getStatusCode().is2xxSuccessful())
        return true;
      else {
        log.warn("returned code[{}] body[{}]", response.getStatusCodeValue(), StringUtils.abbreviate(response.getBody(), 500));
        return false;
      }
    } catch(RestClientException e){
      log.error("error invoking mypivot", e);
      return false;
    }
  }

}
