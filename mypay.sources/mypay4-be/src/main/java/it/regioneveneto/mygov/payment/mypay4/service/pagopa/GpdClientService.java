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
package it.regioneveneto.mygov.payment.mypay4.service.pagopa;

import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.DebtPosition;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.DebtPositionDetail;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.PaymentOption;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.util.PagoPAAuthClientInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
public class GpdClientService {

  @Value("${pa.pagopa.gpd.baseUrl}")
  private String pagoPaGpdBaseUrl;

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  @Value("${pa.pagopa.gpd.apikey}")
  private String pagoPaGpdApiKey;
  @Value("${pa.pagopa.gpd.prefixes:}")
  private List<String> pagoPaGpdApiPrefix;

  @Autowired
  private GiornaleService giornaleService;

  private Set<String> pagoPaGpdUrlSet;

  @PostConstruct
  private void init() {
    this.pagoPaGpdUrlSet = this.pagoPaGpdApiPrefix.stream().map(prefix -> pagoPaGpdBaseUrl + prefix).collect(Collectors.toUnmodifiableSet());
  }

  private final RestTemplate restTemplate;

  public GpdClientService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.additionalInterceptors(
      (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
        String url = request.getURI().toString();
        log.debug("GPD url[{}] method[{}], req body[{}]", url, request.getMethodValue(), StringUtils.abbreviate(new String(body, StandardCharsets.UTF_8), 2000));
        String apiKey, apiKeyType;
        if (pagoPaGpdUrlSet.stream().anyMatch(x -> StringUtils.startsWithIgnoreCase(url, x))) {
          //GPD API
          apiKey = StringUtils.firstNonBlank(pagoPaGpdApiKey);
          apiKeyType = "gpd";
//          } else if(...) {
//            //ADD HERE OTHER API CALL (receipt, reporting API
        } else {
          throw new MyPayException("invalid url setting GPD apiKey [" + url + "]");
        }
        log.debug("GPD url[{}], adding api key type[{}]", url, apiKeyType);
        request.getHeaders().set(PagoPAAuthClientInterceptor.SUBSCRIPTION_KEY_KEY, apiKey);
        return execution.execute(request, body);
      }).build();
  }

  public DebtPositionDetail getDebtPosition(String organizationFiscalCode, String iupd) {
    long httpCallStart = System.currentTimeMillis();
    try {
      ResponseEntity<DebtPositionDetail> response = this.restTemplate.getForEntity(pagoPaGpdBaseUrl + "/organizations/{organizationFiscalCode}/debtpositions/{iupd}"
        , DebtPositionDetail.class, Map.of("organizationFiscalCode", organizationFiscalCode, "iupd", iupd));
      return response.getBody();
    } catch (RestClientResponseException he) {
      int statusCode = he.getRawStatusCode();
      String body = he.getResponseBodyAsString();
      if (statusCode == HttpStatus.NOT_FOUND.value()) {
        log.error("not found on getDebtPosition[{}/{}] body[{}]", organizationFiscalCode, iupd, body);
        return null;
      } else {
        log.warn("RestClientResponseException on getDebtPosition - returned code[{}] body[{}]", statusCode, body);
        throw he;
      }
    } catch (RestClientException e) {
      log.error("error on getDebtPosition[{}/{}]", organizationFiscalCode, iupd, e);
      throw e;
    } finally {
      long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
      log.info("elapsed time(ms) for getDebtPosition[{}/{}]: {}", organizationFiscalCode, iupd, elapsed);
    }
  }

  public DebtPosition createDebtPosition(String organizationFiscalCode, boolean toPublish, DebtPosition debtPosition) {
    long httpCallStart = System.currentTimeMillis();
    try {
      log.info("calling debtPosition POST for pa[{}] and debtPosition[{}]", organizationFiscalCode, debtPosition);

      String url = pagoPaGpdBaseUrl + "/organizations/"+organizationFiscalCode+"/debtpositions?toPublish="+toPublish;
      ResponseEntity<DebtPosition> response = giornaleService.wrapRecordRestClientEvent(
              Constants.GIORNALE_MODULO.FESP,
              organizationFiscalCode,
              debtPosition.getPaymentOption().stream().map(PaymentOption::getIuv).collect(Collectors.joining(GiornaleService.SEPARATOR)),
              debtPosition.getIupd(),
              null,
              null,
              Constants.COMPONENTE_FESP,
              Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
              Constants.GIORNALE_TIPO_EVENTO_FESP.gpd.toString(),
              identificativoIntermediarioPA,
              identificativoIntermediarioPA,
              identificativoStazioneIntermediarioPA,
              null,
              restTemplate,
              HttpMethod.POST,
              url,
              debtPosition,
              DebtPosition.class,
              r -> r.getStatusCode().is2xxSuccessful() ?
                      Constants.GIORNALE_ESITO_EVENTO.OK.toString() :
                      Constants.GIORNALE_ESITO_EVENTO.KO.toString()
      );

      DebtPosition responseDebtPosition = response.getBody();
      log.info("created debtPosition[{}]", responseDebtPosition);
      return responseDebtPosition;
    } catch (HttpServerErrorException he) {
      int statusCode = he.getRawStatusCode();
      String body = he.getResponseBodyAsString();
      log.warn("HttpServerErrorException on createDebtPosition - returned code[{}] body[{}]", statusCode, body);
      throw he;
    } catch (RestClientException e) {
      log.error("error on createDebtPosition[{}]", debtPosition, e);
      throw e;
    } finally {
      long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
      log.info("elapsed time(ms) for createDebtPosition[{}]: {}", debtPosition, elapsed);
    }
  }


  public DebtPosition updateDebtPosition(String organizationFiscalCode, String iupd, boolean toPublish, DebtPosition debtPosition) {
    long httpCallStart = System.currentTimeMillis();
    try {
      log.info("calling debtPosition PUT for fc[{}] and iupd[{}]", organizationFiscalCode, iupd);

      String url = pagoPaGpdBaseUrl + "/organizations/"+organizationFiscalCode+"/debtpositions/"+iupd+"?toPublish="+toPublish;
      ResponseEntity<DebtPosition> response = giornaleService.wrapRecordRestClientEvent(
              Constants.GIORNALE_MODULO.FESP,
              organizationFiscalCode,
              debtPosition.getPaymentOption().stream().map(PaymentOption::getIuv).collect(Collectors.joining(GiornaleService.SEPARATOR)),
              debtPosition.getIupd(),
              null,
              null,
              Constants.COMPONENTE_FESP,
              Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
              Constants.GIORNALE_TIPO_EVENTO_FESP.gpd.toString(),
              identificativoIntermediarioPA,
              identificativoIntermediarioPA,
              identificativoStazioneIntermediarioPA,
              null,
              restTemplate,
              HttpMethod.PUT,
              url,
              debtPosition,
              DebtPosition.class,
              r -> r.getStatusCode().is2xxSuccessful() ?
                      Constants.GIORNALE_ESITO_EVENTO.OK.toString() :
                      Constants.GIORNALE_ESITO_EVENTO.KO.toString()
      );

      DebtPosition updatedDebtPosition = response.getBody();
      log.info("response debtPosition PUT code[{}] body[{}]", response.getStatusCodeValue(), updatedDebtPosition);
      return updatedDebtPosition;
    } catch (HttpServerErrorException he) {
      int statusCode = he.getRawStatusCode();
      String body = he.getResponseBodyAsString();
      log.warn("HttpServerErrorException on updateDebtPosition - returned code[{}] body[{}]", statusCode, body);
      throw he;
    } catch (RestClientException e) {
      log.error("error on updateDebtPosition[{}]", debtPosition, e);
      throw e;
    } finally {
      long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
      log.info("elapsed time(ms) for updateDebtPosition[{}]: {}", debtPosition, elapsed);
    }
  }


  public void deleteDebtPosition(String organizationFiscalCode, String iupd, String iuv) {
    long httpCallStart = System.currentTimeMillis();
    try {
      log.info("calling debtPosition DELETE for fc[{}] and iupd[{}]", organizationFiscalCode, iupd);

      String url = pagoPaGpdBaseUrl + "/organizations/"+organizationFiscalCode+"/debtpositions/"+iupd;
      ResponseEntity<String> response = giornaleService.wrapRecordRestClientEvent(
              Constants.GIORNALE_MODULO.FESP,
              organizationFiscalCode,
              iuv,
              iupd,
              null,
              null,
              Constants.COMPONENTE_FESP,
              Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
              Constants.GIORNALE_TIPO_EVENTO_FESP.gpd.toString(),
              identificativoIntermediarioPA,
              identificativoIntermediarioPA,
              identificativoStazioneIntermediarioPA,
              null,
              restTemplate,
              HttpMethod.DELETE,
              url,
              null,
              String.class,
              r -> r.getStatusCode().is2xxSuccessful() ?
                      Constants.GIORNALE_ESITO_EVENTO.OK.toString() :
                      Constants.GIORNALE_ESITO_EVENTO.KO.toString()
      );

      log.info("response debtPosition DELETE code[{}] body[{}]", response.getStatusCodeValue(), response.getBody());
    } catch (HttpServerErrorException he) {
      int statusCode = he.getRawStatusCode();
      String body = he.getResponseBodyAsString();
      log.warn("HttpServerErrorException on deleteDebtPosition - returned code[{}] body[{}]", statusCode, body);
      throw he;
    } catch (RestClientException e) {
      log.error("error on deleteDebtPosition iupd[{}]", iupd, e);
      throw e;
    } finally {
      long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
      log.info("elapsed time(ms) for deleteDebtPosition iupd[{}]: {}", iupd, elapsed);
    }
  }


}
