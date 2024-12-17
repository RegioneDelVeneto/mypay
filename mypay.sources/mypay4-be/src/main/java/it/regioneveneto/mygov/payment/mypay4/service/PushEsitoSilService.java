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

import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4RestTemplateCustomizer;
import it.regioneveneto.mygov.payment.mypay4.dao.PushEsitoSilDao;
import it.regioneveneto.mygov.payment.mypay4.dto.InvioEsitoPushResponseTo;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.EnteSil;
import it.regioneveneto.mygov.payment.mypay4.model.PushEsitoSil;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.PagamentiTelematiciDovutiPagatiHelper;
import it.veneto.regione.schemas._2012.pagamenti.ente.Pagati;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PushEsitoSilService {

  @Value("${task.sendPaymentNotification.backoff.base:3}")
  private double backOffBase;

  @Value("${task.sendPaymentNotification.backoff.intervalFirstLastAttempSeconds:1209600}")
  private long backOffIntervalFirstLastAttempSeconds;

  @Autowired
  private CarrelloService carrelloService;
  @Autowired
  private PushEsitoSilDao pushEsitoSilDao;
  @Autowired
  private EnteSilService enteSilService;
  @Autowired
  private JAXBTransformService jaxbTransformService;

  private final RestTemplate restTemplate;

  public PushEsitoSilService(RestTemplateBuilder restTemplateBuilder, ConfigurableEnvironment env) {
    int connectTimeout = env.getProperty("task.sendPaymentNotification.connectTimeoutSeconds", Integer.class, 5) * 1000;
    int readTimeout = env.getProperty("task.sendPaymentNotification.connectTimeoutSeconds", Integer.class, 5) * 1000;
    MyPay4RestTemplateCustomizer.setCustomTimeout(connectTimeout, readTimeout);
    this.restTemplate = restTemplateBuilder.build();
  }


  @Transactional(propagation = Propagation.REQUIRED)
  public Optional<Long> insertNewPushEsitoSil(Long mygovDovutoElaboratoId) {
    Optional<PushEsitoSil> existing = pushEsitoSilDao.getByDovutoElaboratoId(mygovDovutoElaboratoId);
    if(existing.isPresent()){
      log.info("insert PushEsitoSil, already exists id[{}], mygovDovutoElaboratoId[{}]",existing.get().getMygovPushEsitoSilId(), mygovDovutoElaboratoId);
      return Optional.empty();
    }
    PushEsitoSil pushEsitoSil = PushEsitoSil.builder()
      .dtCreazione(new Date())
      .flgEsitoInvioPush(false)
      .mygovDovutoElaboratoId(DovutoElaborato.builder().mygovDovutoElaboratoId(mygovDovutoElaboratoId).build())
      .numTentativiEffettuati(0)
      .build();
    Long newId = pushEsitoSilDao.insertNewPushEsitoSil(pushEsitoSil);
    log.info("insert PushEsitoSil, id[{}]",newId);
    return Optional.of(newId);
  }

  public List<Long> findNotificationIdsToSend() {
    return pushEsitoSilDao.findNotificationIdsToSend();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleNotificationToSend(Long idNotificationToSend) {
    pushEsitoSilDao.getToSendByIdLockOrSkip(idNotificationToSend).ifPresentOrElse( pushEsitoSil -> {
      DovutoElaborato dovutoElab = pushEsitoSil.getMygovDovutoElaboratoId();
      try {

        /*
         * Business logic retry: exponential backoff, calculated using fixed interval between first and last attempt
         * (for instance: 14 days)
         *
         * Example in case:
         *     base = 3
         *     num_attempts = 20
         *     interval_first_last (sec) = 1209600 = 14 days
         *
         * Attempt	Wait (sec)	Date/Time attempt
         *  0	        3			    01/06/2023 12.00.03
         *  1	        6			    01/06/2023 12.00.06
         *  2	        11			  01/06/2023 12.00.11
         *  3	        21			  01/06/2023 12.00.21
         *  4	        40			  01/06/2023 12.00.40
         *  5	        76			  01/06/2023 12.01.16
         *  6	        144			  01/06/2023 12.02.24
         *  7	        275			  01/06/2023 12.04.35
         *  8	        524			  01/06/2023 12.08.44
         *  9	        999			  01/06/2023 12.16.39
         *  10		    1905		  01/06/2023 12.31.45
         *  11		    3632		  01/06/2023 13.00.32
         *  12		    6925		  01/06/2023 13.55.25
         *  13		    13204		  01/06/2023 15.40.04
         *  14		    25176		  01/06/2023 18.59.36
         *  15		    48002		  02/06/2023 1.20.02
         *  16		    91525		  02/06/2023 13.25.25
         *  17		    174507		03/06/2023 12.28.27
         *  18		    332728		05/06/2023 8.25.28
         *  19		    634404		08/06/2023 20.13.24
         *  20		    1209600		15/06/2023 12.00.00
         *
         */
        int numTentativiEffettuati = Optional.ofNullable(pushEsitoSil.getNumTentativiEffettuati()).orElse(0);
        Date dtUltimoTentativo = Optional.ofNullable(pushEsitoSil.getDtUltimoTentativo()).orElse(new Date(0));
        long timeSinceLastAttempt = (System.currentTimeMillis() - dtUltimoTentativo.getTime())/1000;
        long waitIntervalThisAttempt = (long) (backOffBase * Math.pow(backOffIntervalFirstLastAttempSeconds/backOffBase,
            (1.0D*numTentativiEffettuati)/pushEsitoSil.getNestedMaxTentativiInoltroEsito()));
        long waitIntervalPreviousAttempt = numTentativiEffettuati==0 ? 0 : (long) (backOffBase * Math.pow(backOffIntervalFirstLastAttempSeconds/backOffBase, (numTentativiEffettuati-1.0D)/pushEsitoSil.getNestedMaxTentativiInoltroEsito()));

        if(timeSinceLastAttempt < (waitIntervalThisAttempt - waitIntervalPreviousAttempt)){
          log.debug("skipping notification id[{}] due to retry policies - lastAttempt[{}] numAttempts[{}] nextAttempsNotBefore[{}]", pushEsitoSil.getMygovPushEsitoSilId(),
              dtUltimoTentativo, numTentativiEffettuati,
              dtUltimoTentativo.toInstant().atZone(ZoneId.of("Europe/Rome")).plus(waitIntervalThisAttempt - waitIntervalPreviousAttempt, ChronoUnit.SECONDS).toLocalDateTime());
          return;
        }

        EnteSil enteSil = enteSilService.getByEnteTipoDovuto(pushEsitoSil.getNestedMygovEnteId(), dovutoElab.getCodTipoDovuto());

        String rtString = Base64.encodeBase64String(dovutoElab.getBlbRtPayload());

        Carrello carrello = carrelloService.getById(pushEsitoSil.getNestedMygovCarrelloId());
        Pagati pagatiDocument = PagamentiTelematiciDovutiPagatiHelper.creaPagatiDocument(carrello, List.of(dovutoElab));
        String pagatiString = jaxbTransformService.marshalling(pagatiDocument, Pagati.class);
        log.debug("PAGATI per dominio [{}] e IUV [{}]: {}", pagatiDocument.getDominio().getIdentificativoDominio(),
            pagatiDocument.getDatiPagamento().getIdentificativoUnivocoVersamento(), pagatiString);
        String esitoString = Base64.encodeBase64String(pagatiString.getBytes(StandardCharsets.UTF_8));

        Map<String, String> payload = Map.of(
            "rt", rtString,
            "esito", esitoString
        );

        String url = enteSil.getDeUrlInoltroEsitoPagamentoPush();
        HttpHeaders headers = new HttpHeaders();
        if (enteSil.isFlgJwtAttivo()) {
          byte[] keyBytes = Decoders.BASE64.decode(enteSil.getCodServiceAccountJwtUscitaSecretKey());
          Key key = Keys.hmacShaKeyFor(keyBytes);
          String jwtToken = Jwts.builder()
              .setHeaderParam(JwsHeader.KEY_ID, enteSil.getCodServiceAccountJwtUscitaSecretKeyId())
              .setSubject(enteSil.getDeServiceAccountJwtUscitaClientMail())
              .setIssuer(enteSil.getCodServiceAccountJwtUscitaClientId())
              .signWith(key, SignatureAlgorithm.HS512).compact();
          headers.setBearerAuth(jwtToken);
        }
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

        long httpCallStart = System.currentTimeMillis();
        String faultCode = null;
        String faultDescription = null;
        boolean success = false;
        try {
          ResponseEntity<InvioEsitoPushResponseTo> response = this.restTemplate.exchange(url, HttpMethod.POST, entity, InvioEsitoPushResponseTo.class);
          if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("returned code[{}] body[{}]", response.getStatusCodeValue(), response.getBody());
            faultCode = Integer.toString(response.getStatusCodeValue());
            if(response.getBody()!=null && StringUtils.isNotBlank(response.getBody().getFaultCode()))
              faultDescription = response.getBody().getFaultCode() + ": "+response.getBody().getFaultDescription();
          } else if(response.getBody()!=null && StringUtils.isNotBlank(response.getBody().getFaultCode())){
            log.warn("returned faultCode[{}] faultDescription[{}]", response.getBody().getFaultCode(), response.getBody().getFaultDescription());
            faultCode = response.getBody().getFaultCode();
            faultDescription = response.getBody().getFaultDescription();
          } else {
            success = true;
          }
        } catch(HttpServerErrorException he){
          int statusCode = he.getRawStatusCode();
          String body = he.getResponseBodyAsString();
          log.warn("HttpServerErrorException returned code[{}] body[{}]",statusCode, body);
          faultCode = Integer.toString(statusCode);
          faultDescription = body;
        } catch(RestClientException e){
          log.error("error sending notification with id[{}]", pushEsitoSil.getMygovPushEsitoSilId(), e);
          faultCode = ExceptionUtils.getMessage(e) + " -- "+ExceptionUtils.getRootCauseMessage(e);
          faultDescription = ExceptionUtils.getStackTrace(e);
        } finally {
          long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
          log.info("elapsed time(ms) for esitoPush call pushEsitoSilId[{}] idDominio[{}] iuv[{}]: {}",
              pushEsitoSil.getMygovPushEsitoSilId(),
              dovutoElab.getCodRpDomIdDominio(),
              dovutoElab.getCodRpSilinviarpIdUnivocoVersamento(),
              elapsed);
        }

        pushEsitoSil.setDtUltimoTentativo(new Date(httpCallStart));
        pushEsitoSil.setNumTentativiEffettuati(numTentativiEffettuati + 1);
        pushEsitoSil.setFlgEsitoInvioPush(success);
        pushEsitoSil.setCodEsitoInvioFaultCode(StringUtils.abbreviate(faultCode,256));
        pushEsitoSil.setDeEsitoInvioFaultDescription(StringUtils.abbreviate(faultDescription,256));
        pushEsitoSilDao.updatePushEsitoSil(pushEsitoSil);

      } catch(Exception e) {
        log.error("error sending notification id[{}]", pushEsitoSil.getMygovPushEsitoSilId(), e);
      }
    }, () -> log.info("handleNotificationToSend, skip because pushEsitoSil missing or locked [{}]", idNotificationToSend) );
  }

  public PushEsitoSil getById(Long idPushEsitoSil) {
    return pushEsitoSilDao.getById(idPushEsitoSil);
  }

}
