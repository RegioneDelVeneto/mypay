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

import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.massivo.UploadReport;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.massivo.UploadStatus;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.*;

@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
public class GpdMassiveClientService {
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final int MAX_RETRIES = 5;

    private final RestTemplate restTemplate;
    private final String BROKER_PATH =
            "/brokers/%s/organizations/%s/debtpositions";
    @Value("${pa.identificativoIntermediarioPA}")
    private String brokerCode;
    @Value("${pa.pagopa.gpdMassivo.baseUrl}")
    private String pagoPaGpdMassivoBaseUrl;
    @Value("${pa.pagopa.gpd.apikey}")
    private String pagoPaGpdMassivoApiKey;
    @Value("${pa.pagopa.gpdMassivo.prefixes:}")
    private List<String> pagoPaGpdMassivoApiPrefix;
    private Set<String> pagoPaGpdMassivoUrlSet;

    @Autowired
    private GiornaleService giornaleService;

    public GpdMassiveClientService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.additionalInterceptors(
                (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
                    String url = request.getURI().toString();
                    log.debug("Massive GPD url[{}], req body[{}]", url, StringUtils.abbreviate(new String(body, StandardCharsets.UTF_8), 2000));
                    String apiKey, apiKeyType;
                    if (pagoPaGpdMassivoUrlSet.stream().anyMatch(x -> StringUtils.startsWithIgnoreCase(url, x))) {
                        //GPD API
                        apiKey = StringUtils.firstNonBlank(pagoPaGpdMassivoApiKey);
                        apiKeyType = "gpd";
                    } else {
                        throw new MyPayException("invalid url setting MASSIVE GPD apiKey [" + url + "]");
                    }
                    log.debug("Massive GPD url[{}], adding api key type[{}]", url, apiKeyType);
                    request.getHeaders().set(PagoPAAuthClientInterceptor.SUBSCRIPTION_KEY_KEY, apiKey);
                    return execution.execute(request, body);
                }).build();
    }


    @PostConstruct
    private void init() {
        this.pagoPaGpdMassivoUrlSet = this.pagoPaGpdMassivoApiPrefix.stream()
                .map(prefix -> pagoPaGpdMassivoBaseUrl + prefix).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Upload a zip file to PagoPA
     *
     * @param organizationFiscalCode the organization fiscal code
     * @param zipFilePath            the zip file path
     * @param gpdStatus              the GPD status
     * @return the file id
     */
    public String uploadZipFile(String organizationFiscalCode, Path zipFilePath, Character gpdStatus) {

        log.info("Uploading ZipFile[{}]", zipFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(zipFilePath));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = String.format(pagoPaGpdMassivoBaseUrl + BROKER_PATH + "/file",
                brokerCode, organizationFiscalCode);

        HttpMethod method = getHttpMethod(gpdStatus);
        return executeUploadWithRetries(organizationFiscalCode, method, url, requestEntity);

    }

    /**
     * Get the upload status for a file
     *
     * @param organizationFiscalCode the organization fiscal code
     * @param fileId                 the file id
     * @return the upload status
     */
    public UploadStatus getUploadStatus(String organizationFiscalCode, String fileId) {
        String url = String.format(pagoPaGpdMassivoBaseUrl + BROKER_PATH + "/file/%s/status",
                brokerCode, organizationFiscalCode, fileId);

        return executeWithRetries(organizationFiscalCode, HttpMethod.GET, url, null, UploadStatus.class);
    }


    /**
     * Get the upload report for a file
     *
     * @param organizationFiscalCode the organization fiscal code
     * @param fileId                 the file id
     * @return the upload report
     */
    public UploadReport getUploadReport(String organizationFiscalCode, String fileId) {
        String url = String.format(pagoPaGpdMassivoBaseUrl + BROKER_PATH + "/file/%s/report", brokerCode, organizationFiscalCode, fileId);

        return executeWithRetries(organizationFiscalCode, HttpMethod.GET, url, null, UploadReport.class);
    }


    /**
     * Execute a request with retries
     *
     * @param organizationFiscalCode the organization fiscal code
     * @param method                 the HTTP method
     * @param url                    the URL
     * @param requestEntity          the request entity
     * @param responseType           the response type
     * @param <T>                    the response type
     * @return the response
     */
    private <T> T executeWithRetries(String organizationFiscalCode, HttpMethod method, String url, HttpEntity<?> requestEntity, Class<T> responseType) {
        int retries = 0;
        do {
            long httpCallStart = System.currentTimeMillis();
            try {
                ResponseEntity<T> response = giornaleService.wrapRecordRestClientEvent(
                        Constants.GIORNALE_MODULO.FESP,
                        organizationFiscalCode,
                        null,
                        null,
                        null,
                        null,
                        Constants.COMPONENTE_FESP,
                        Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
                        Constants.GIORNALE_TIPO_EVENTO_FESP.gpdMassivo.toString(),
                        brokerCode,
                        brokerCode,
                        brokerCode,
                        null,
                        restTemplate,
                        method,
                        url,
                        requestEntity,
                        responseType,
                        r -> r.getStatusCode().is2xxSuccessful() ?
                                Constants.GIORNALE_ESITO_EVENTO.OK.toString() :
                                Constants.GIORNALE_ESITO_EVENTO.KO.toString()
                );

                return response.getBody();
            } catch (HttpClientErrorException hce) {
                if (hce.getRawStatusCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                    sleep(getSecondsToSleep(hce));
                } else {
                    throw hce;
                }
            } finally {
                long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
                log.info("elapsed time(ms) for {} {}: {}", method.name(), url, elapsed);
            }

        } while (retries++ < MAX_RETRIES);

        throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Too many retries on " + method.name() + " " + url);
    }


    /**
     * Execute an upload request with retries
     *
     * @param organizationFiscalCode the organization fiscal code
     * @param method                 the HTTP method
     * @param url                    the URL
     * @param requestEntity          the request entity
     * @return the file id
     */

    private String executeUploadWithRetries(String organizationFiscalCode, HttpMethod method, String url, HttpEntity<?> requestEntity) {
        int retries = 0;
        do {
            long httpCallStart = System.currentTimeMillis();
            try {
                ResponseEntity<String> response = giornaleService.wrapRecordRestClientEvent(
                        Constants.GIORNALE_MODULO.FESP,
                        organizationFiscalCode,
                        null,
                        null,
                        null,
                        null,
                        Constants.COMPONENTE_FESP,
                        Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
                        Constants.GIORNALE_TIPO_EVENTO_FESP.gpdMassivo.toString(),
                        brokerCode,
                        brokerCode,
                        brokerCode,
                        null,
                        restTemplate,
                        method,
                        url,
                        requestEntity,
                        String.class,
                        r -> r.getStatusCode().is2xxSuccessful() ?
                                Constants.GIORNALE_ESITO_EVENTO.OK.toString() :
                                Constants.GIORNALE_ESITO_EVENTO.KO.toString(),
                        true
                );

                return extractFileIdFromLocationHeader(response);
            } catch (HttpClientErrorException hce) {
                if (hce.getRawStatusCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                    sleep(getSecondsToSleep(hce));
                } else {
                    throw hce;
                }
            } finally {
                long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
                log.info("elapsed time(ms) for {} {}: {}", method.name(), url, elapsed);
            }

        } while (retries++ < MAX_RETRIES);

        throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Too many retries on " + method.name() + " " + url);
    }


    /**
     * Extract the file id from the Location header
     *
     * @param response the response
     * @return the file id
     */
    private String extractFileIdFromLocationHeader(ResponseEntity<String> response) {
        log.info("Location header: {}", response.getHeaders().getLocation());

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            log.error("Location header is null");
            return "";
        }

        String path = location.getPath();
        if (Objects.isNull(path)) {
            log.error("Invalid location header: {} ", location);
            return "";
        }

        String[] parts = path.split("/", -1);

        if (parts.length < 2) {
            log.error("Invalid location header: {} , missing fileId", path);
            return "";
        }
        return parts[parts.length - 2];
    }

    /**
     * Extract the number of seconds to sleep from the Retry-After header
     *
     * @param hce the HttpClientErrorException
     * @return the number of seconds to sleep
     */

    private int getSecondsToSleep(HttpClientErrorException hce) {

        String first = hce.getResponseHeaders().getFirst(RETRY_AFTER_HEADER);
        log.warn("Errore 429: troppe richieste! il server chiede di fermarsi per almeno {} secondi", first);
        int retryAfter = first != null ? Integer.parseInt(first) : 60;
        if (retryAfter > 60) {
            retryAfter = 60;
        }
        log.warn("Errore 429: ci si ferma per {} secondi", first);
        return retryAfter;
    }


    /**
     * Get the HTTP method for the GPD status
     *
     * @param gpdStatus the GPD status
     * @return the HTTP method
     */
    private HttpMethod getHttpMethod(Character gpdStatus) {
        HttpMethod method;

        switch (gpdStatus) {
            case STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA:
                method = HttpMethod.POST;
                break;
            case STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA:
                method = HttpMethod.PUT;
                break;
            case STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA:
                method = HttpMethod.DELETE;
                break;
            default:
                log.error("Invalid gpdStatus[{}]", gpdStatus);
                throw new MyPayException("Invalid gpdStatus[" + gpdStatus + "]");

        }
        return method;
    }


    /**
     * Sleep for a number of seconds
     *
     * @param sleepSeconds number of seconds to sleep
     */
    private void sleep(int sleepSeconds) {
        log.info("Sleeping for {} seconds", sleepSeconds);
        try {
            Thread.sleep(sleepSeconds * 1_000L);
        } catch (InterruptedException ignored) {
        }
        log.info("Slept for {} seconds", sleepSeconds);
    }

}
