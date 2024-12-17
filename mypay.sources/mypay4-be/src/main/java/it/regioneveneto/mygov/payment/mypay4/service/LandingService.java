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

import it.regioneveneto.mygov.payment.mypay4.controller.LandingController;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LandingService {

  public enum OVERRIDE_REPLICA_CHECK_VALIDATION { VALID, INVALID, EXPIRED }

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;

  @Value("${app.fe.cittadino.absolute-path}")
  private String appFeCittadinoAbsolutePath;

  @Value("${pa.overrideCheckReplicaPayments.validityMinutes:15}")
  private Long overrideCheckReplicaPaymentsValidityMinutes;

  @Autowired
  private MailValidationService mailValidationService;

  public String getUrlInviaDovuti(String id) {
    return getUrlInviaDovuti(id, false);
  }

  public String getUrlInviaDovuti(String id, boolean forceSkipPaymentReplicaCheck) {
    return getUrlImpl(appBeAbsolutePath + LandingController.ANONYMOUS_PATH + "/inviaDovuti", id,
        forceSkipPaymentReplicaCheck?"overrideCheckReplicaPayments":null, null);
  }

  public String getUrlPaymentReplica(String id, String type) {
    return getUrlImpl(appFeCittadinoAbsolutePath + "/landing/paymentReplica", id,
        "skipPaymentReplicaToken", List.of(new BasicNameValuePair("type", type)));
  }

  private String getUrlImpl(String url, String id, String forceSkipPaymentReplicaCheckParam,
                            List<NameValuePair> additionalValues) {
    List<NameValuePair> params = new ArrayList<>();
    if(additionalValues!=null && !additionalValues.isEmpty())
      params.addAll(additionalValues);
    params.add(new BasicNameValuePair("id", id));
    if(StringUtils.isNotBlank(forceSkipPaymentReplicaCheckParam))
      params.add(new BasicNameValuePair(forceSkipPaymentReplicaCheckParam, generateSkipPaymentReplicaToken(id)));

    url = url + "?" + URLEncodedUtils.format(params, StandardCharsets.UTF_8);
    return url;
  }

  private String generateSkipPaymentReplicaToken(String id){
    String toEncrypt = id + "|" + System.currentTimeMillis();
    try {
      return mailValidationService.encrypt(toEncrypt, true);
    }catch(Exception e){
      log.error("error generateSkipPaymentReplicaToken id[{}]", id, e);
      throw new MyPayException(e);
    }
  }

  public OVERRIDE_REPLICA_CHECK_VALIDATION validateParamCheckReplicaPayments(String id, String overrideCheckReplicaPayments){
    try{
      String decrypted = mailValidationService.decrypt(overrideCheckReplicaPayments, true);
      if(!StringUtils.startsWith(decrypted, id+"|")){
        log.warn("invalid overrideCheckReplicaPayments [{}]->[{}] - wrong id[{}]", overrideCheckReplicaPayments, decrypted, id);
        return OVERRIDE_REPLICA_CHECK_VALIDATION.INVALID;
      }
      String creationDate = StringUtils.substring(decrypted, id.length()+1);
      //check if expired
      long minutes = Math.round(Math.floor((System.currentTimeMillis() - Long.parseLong(creationDate))/60000d));
      if(minutes < 0 || minutes > overrideCheckReplicaPaymentsValidityMinutes){
        log.warn("expired overrideCheckReplicaPayments [{}]->[{}] - minutes[{}]", overrideCheckReplicaPayments, decrypted, minutes);
        return OVERRIDE_REPLICA_CHECK_VALIDATION.EXPIRED;
      }
    }catch(Exception e){
      log.error("error validateParamCheckReplicaPayments when decrypting overrideCheckReplicaPayments", e);
      return OVERRIDE_REPLICA_CHECK_VALIDATION.INVALID;
    }
    return OVERRIDE_REPLICA_CHECK_VALIDATION.VALID;
  }

  public String getUrlDownloadAvviso(String dovutoId, String securityToken) {
    return appBeAbsolutePath + LandingController.ANONYMOUS_PATH + "/avviso?id=" + dovutoId + "&securityToken=" + URLEncoder.encode(securityToken, StandardCharsets.UTF_8);
  }

  public String getUrlChiediPosizioniAperte(String id) { return appBeAbsolutePath + LandingController.ANONYMOUS_PATH + "/precaricato?id=" + id; }

  public String getUrlChiediStoricoPagamenti(String id) {
    return appBeAbsolutePath + LandingController.ANONYMOUS_PATH + "/rt?id=" + id;
  }
}
