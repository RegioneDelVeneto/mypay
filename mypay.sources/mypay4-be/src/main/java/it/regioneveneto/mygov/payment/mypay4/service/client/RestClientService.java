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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RestClientService {

    @Value("${proxy.host:}")
    private String proxyHost;
    @Value("${proxy.port:0}")
    private int proxyPort;
    @Value("${proxy.enable:false}")
    private boolean useProxy;
    @Value("${proxy.user:}")
    private String proxyUser;
    @Value("${proxy.password:}")
    private String proxyPwd;

    @Value("${proxy.nonProxyHosts:}")
    private String proxyNonProxyHosts;

    public RestTemplate getRestTemplate(String url,   List<ClientHttpRequestInterceptor> interceptorsClient ) {

        RestTemplate restTemplate;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (useProxy) {
            useProxy = shouldUseProxy( url);

        }
        if (useProxy) {
            log.debug("useProxy");
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

            if (!StringUtils.isBlank(proxyUser)) {
                Authenticator authenticator = new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(proxyUser,
                                proxyPwd.toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
            }


            requestFactory.setProxy(proxy);

            restTemplate = new RestTemplate(requestFactory);

        } else {
            restTemplate = new RestTemplate();
        }



        //    HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        //  HttpClient httpClient = clientBuilder.build();

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        restTemplate.getMessageConverters().add(new ResourceHttpMessageConverter());
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        restTemplate.setInterceptors(interceptorsClient);

        BufferingClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(requestFactory);

        restTemplate.setRequestFactory(factory);

        return restTemplate;

    }

    private  boolean shouldUseProxy(String destinationHost) {

        String regexString = "/" +proxyNonProxyHosts+"/";

        // Sostituisci il carattere "*" con l'espressione regolare ".*"
        regexString = regexString.replace(".", "\\.");
        regexString = regexString.replace("*", ".*");


        log.debug("regexString: "+regexString);
        // Dividi i pattern usando il carattere "|"
        String[] patterns = regexString.split("\\|");

        // Escapa i punti nelle espressioni regolari
        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = patterns[i].replaceAll("\\.", "\\\\.");
        }

        // Costruisci l'espressione regolare completa
        //  String regex = "(" + String.join("|", patterns) + ")";
        //  log.debug("regex: "+regex);
        // Crea un oggetto Pattern
        Pattern pattern = Pattern.compile(regexString);
        URI uri = null;
        String domain = destinationHost;
        try {
            uri = new URI(destinationHost);
            domain = uri.getHost();
        } catch (URISyntaxException e) {
            log.error("errore nell'URL: "+e, e);
        }

        log.debug("domain: "+domain);
        // Esegui il confronto dell'host di destinazione con l'espressione regolare
        Matcher matcher = pattern.matcher(domain);
        boolean trovato = matcher.find();
        log.debug("trovato: "+trovato);
        // Restituisci true se non c'è una corrispondenza (non deve passare da proxy), altrimenti false
        return !trovato;
    }

    public RetryTemplate getRetryTemplate(long time, int attempts) {

        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(time);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(attempts);

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        return retryTemplate;
    }
}
