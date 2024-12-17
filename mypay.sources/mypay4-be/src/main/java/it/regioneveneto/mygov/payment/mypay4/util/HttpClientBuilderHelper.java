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
package it.regioneveneto.mygov.payment.mypay4.util;

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Scanner;

@Slf4j
public class HttpClientBuilderHelper {

  private final HttpClientBuilder instance;
  private final Environment env;

  private HttpClientBuilderHelper(Environment env){
    this.instance = HttpClientBuilder.create();
    this.env = env;
  }

  public static HttpClientBuilderHelper create(Environment env) {
    return new HttpClientBuilderHelper(env);
  }

  public HttpClient build(){
    int timeoutSeconds = env.getProperty("mypay.httpClient.connectTimeoutSeconds", Integer.class, 0);
    this.setConnectionTimeout(timeoutSeconds);
    return instance.build();
  }

  private String getEnvProperty(String key){
    return env.getProperty(key, System.getProperty(key));
  }

  private void setConnectionTimeout(int timeoutSeconds) {
    log.info("set connect/connectionRequest/socket Timeout to: {} secondes", timeoutSeconds);
    if (timeoutSeconds>0) {
      RequestConfig config = RequestConfig.custom()
          .setConnectTimeout(timeoutSeconds * 1000)
          .setConnectionRequestTimeout(timeoutSeconds * 1000)
          .setSocketTimeout(timeoutSeconds * 1000)
          .build();
      instance.setDefaultRequestConfig(config);
    }
  }

  public HttpClientBuilderHelper addProxySupport() {
    this.addProxySupport(false);
    return this;
  }

  public HttpClientBuilderHelper addProxySupport(boolean withSoapHeadersFix) {
    if (StringUtils.isNotBlank(getEnvProperty("http.proxyHost"))) {
      String proxyHost = getEnvProperty("http.proxyHost");
      int proxyPort = Integer.parseInt(getEnvProperty("http.proxyPort"));
      instance.useSystemProperties();
      HttpHost proxy = new HttpHost(proxyHost, proxyPort);
      instance.setProxy(proxy);
      log.debug("setting HttpClient proxy: {}:{}", proxyHost,proxyPort);

      if(StringUtils.isNotBlank(getEnvProperty("http.nonProxyHosts"))){
        String[] nonProxyList = StringUtils.split(getEnvProperty("http.nonProxyHosts"), '|');
        log.debug("setting HttpClient proxy exclusion for: {}", StringUtils.joinWith(";",(Object[])nonProxyList));
        instance.setRoutePlanner(new DefaultProxyRoutePlanner(proxy) {
          @Override
          public HttpRoute determineRoute(
              final HttpHost host,
              final HttpRequest request,
              final HttpContext context) throws HttpException {
            String hostname = host.getHostName();
            if(StringUtils.containsAnyIgnoreCase(hostname, nonProxyList)) {
              // Return direct route
              return new HttpRoute(host);
            }
            return super.determineRoute(host, request, context);
          }
        });
      }


      if (StringUtils.isNotBlank(getEnvProperty("http.proxyUser"))) {
        String proxyUser = getEnvProperty("http.proxyUser");
        String proxyPassword = getEnvProperty("http.proxyPassword");
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPassword));
        instance.setDefaultCredentialsProvider(credsProvider);
        instance.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        log.debug("setting HttpClient proxyUser: {} - password: ***", proxyUser);
      }
    } else {
      log.debug("no HttpClient proxy properties detected");
    }
    if(withSoapHeadersFix){
      instance.addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor());
    }
    return this;
  }

  public HttpClientBuilderHelper addCustomSSLSupport(String propPrefix) {
    try {
      KeyStore keyStore = null;
      KeyStore trustStore = null;

      final boolean disableCertCheck = BooleanUtils.toBoolean(getEnvProperty(propPrefix + ".disable_cert_check"));
      final boolean dontAddDefaultCertsToTruststore = BooleanUtils.toBoolean(getEnvProperty(propPrefix + ".dont_add_default_certs_to_truststore"));
      TrustStrategy trustStrategy = (x509Certificates, s) -> {
        for (int i = 0; i < x509Certificates.length; i++)
          log.debug("checking certificate chain[{}] subject[{}] issuer[{}] exp[{}]", i, x509Certificates[i].getSubjectDN(), x509Certificates[i].getIssuerDN(), x509Certificates[i].getNotAfter().toInstant());
        return disableCertCheck;
      };
      if(disableCertCheck){
        log.warn("Disabled certificate check, using NoopHostnameVerifier()!");
        instance.setSSLHostnameVerifier(new NoopHostnameVerifier());
      }

      InputStream keyStoreStream = null;
      if (StringUtils.isNotBlank(getEnvProperty(propPrefix+".keystore_content"))) {
        keyStoreStream = new ByteArrayInputStream(Base64.decodeBase64(getEnvProperty(propPrefix+".keystore_content")));
        log.debug("set custom KeyStore (by base64 string)");
      } else if (StringUtils.isNotBlank(getEnvProperty(propPrefix+".keystore_path"))) {
        keyStoreStream = new FileInputStream(getEnvProperty(propPrefix+".keystore_path"));
        log.debug("set custom KeyStore (by file)");
      } else {
        log.debug("no custom KeyStore detected");
      }
      if(keyStoreStream!=null) {
        keyStore = KeyStore.getInstance("pkcs12");
        String keyStorePassword = getEnvProperty(propPrefix + ".keystore_password");
        Assert.notNull(keyStorePassword, "configuration property '" + propPrefix + ".keystore_password' could not be null");
        keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
        logKeyStoreEntries("key", keyStore);
      }

      InputStream trustStoreStream = null;
      if (StringUtils.isNotBlank(getEnvProperty(propPrefix+".truststore_content"))) {
        trustStoreStream = new ByteArrayInputStream(Base64.decodeBase64(getEnvProperty(propPrefix+".truststore_content")));
        log.debug("set custom TrustStore (by base64 string)");
      } else if (StringUtils.isNotBlank(getEnvProperty(propPrefix+".truststore_path"))) {
        trustStoreStream = new FileInputStream(getEnvProperty(propPrefix+".truststore_path"));
        log.debug("set custom TrustStore (by file)");
      } else {
        log.debug("no custom TrustStore detected");
      }
      if(trustStoreStream != null) {
        trustStore = KeyStore.getInstance("pkcs12");
        String trustStorePassword = getEnvProperty(propPrefix+".truststore_password");
        Assert.notNull(trustStorePassword, "configuration property '"+propPrefix+".truststore_password' could not be null");
        trustStore.load(trustStoreStream, trustStorePassword.toCharArray());

        if(!dontAddDefaultCertsToTruststore){
          log.info("adding default certs to custom truststore");
          try{
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore)null);
            int certCounter = 0;
            for(TrustManager tm : tmf.getTrustManagers()){
              if(tm instanceof X509TrustManager){
                X509TrustManager tmx = (X509TrustManager) tm;
                for(X509Certificate cert : tmx.getAcceptedIssuers()){
                  try {
                    trustStore.setCertificateEntry("default_cert_" + certCounter++, cert);
                  }catch(Exception e){
                    log.warn("cannot add to custom truststore certificate serial[{}] subject[{}]", cert.getSerialNumber(), cert.getSubjectDN());
                  }
                }
              }
            }
          }catch(Exception e){
            log.warn("error adding default truststore certs to custom truststore", e);
          }
        } else {
          log.info("skip adding default certs to custom truststore");
        }

        logKeyStoreEntries("trust", trustStore);
      }

      String keyPassword = getEnvProperty(propPrefix + ".key_password");
      SSLContext sslContext = SSLContexts.custom()
          .loadKeyMaterial(keyStore,  keyPassword!=null ? keyPassword.toCharArray() : null)
          .loadTrustMaterial(trustStore, trustStrategy)
          .build();
      instance.setSSLContext(sslContext);
      listAllTmIssuers(trustStore);
      log.debug("setting custom SSL properties");
    }catch(Exception e){
      log.error("cannot set custom SSL properties", e);
      throw new MyPayException("cannot set custom SSL properties", e);
    }
    return this;
  }

  private void listAllTmIssuers(KeyStore trustStore){
    try{
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(trustStore);

      for(TrustManager tm : tmf.getTrustManagers()){
        log.info("trust manager [{}]", tm.toString());
        if(tm instanceof X509TrustManager){
          X509TrustManager tmx = (X509TrustManager) tm;
          for(X509Certificate cert : tmx.getAcceptedIssuers()){
            log.info("accepted certificate, subject[{}] issuer[{}] exp[{}] serial[{}]", cert.getSubjectDN(), cert.getIssuerDN(), cert.getNotAfter().toInstant(), cert.getSerialNumber());
          }
        }
      }
    }catch(Exception e){
      log.warn("error listing all accepted issuers of truststore", e);
    }
  }

  private void logKeyStoreEntries(String name, KeyStore keyStore){
    try {
      Enumeration<String> aliases = keyStore.aliases();
      while (aliases.hasMoreElements()) {
        String alias = aliases.nextElement();
        Certificate[] certChain = Optional.ofNullable(keyStore.getCertificateChain(alias)).orElse(new Certificate[0]);
        for(int i=0; i< certChain.length; i++) {
          if(certChain[i] instanceof X509Certificate){
            X509Certificate cert = (X509Certificate) certChain[i];
            log.info("keyStore[{}] alias[{}]({}): subject[{}] issuer[{}] exp[{}] serial[{}]", name, alias, i, cert.getSubjectDN(), cert.getIssuerDN(), cert.getNotAfter().toInstant(), cert.getSerialNumber());
          } else
            log.debug("keyStore[{}] alias[{}], certificate({})[{}]", name, alias, i, certChain[i]);
        }
      }
    }catch(Exception e){
      log.warn("error logging entries for keystore", e);
    }
  }

  public HttpClientBuilderHelper disableRedirectHandling() {
    instance.disableRedirectHandling();
    return this;
  }

  @SuppressWarnings({"java:S2068"})
  public static void main(String[] args) {
    try{

      String p12Base64 =
          "MIIKIAIBAzCCCdoGCSqGSIb3DQEHAaCCCcsEggnHMIIJwzCCBYAGCSqGSIb3DQEHAaCCBXEEggVt" +
              "MIIFaTCCBWUGCyqGSIb3DQEMCgECoIIE+jCCBPYwKAYKKoZIhvcNAQwBAzAaBBR/vN51SZn0cqGy" +
              "EnXBkG93sFwdqQICBAAEggTImDKB4InpY3zGT1+K9kZBNT8MdX6sQHwSivUsujUowUuosOBu27+J" +
              "sjwkw7T1EE4imhy5RUID/y5vlpC+Zecegq052Cl9hiYj9F8AW8n4zzkuRk4LSw3nmVJDsO3ZvoG4" +
              "7cvSPDXqgunq/tOiX5Ec/FUR0cWPuns3ushrTOZP0pR66gBJ9AoDOyTosUtxVRNVJTsRLIRopbqK" +
              "+A+NZTJ78450zDVoZPr0HCa34xbHTrY+mC3GgVfvPyjWCxdnxIM2AmSNgJjJFcbI9excBEIcNDDT" +
              "Z7TPLZw3rTOH6R9sDr8g+IMY1vie9yjC/mlL6VUtTczpPe5QkP1aL38Cznr7ZxkNAb7RUrtQZhxl" +
              "FeoiuZ7c7iSLVnG4Z85BL+zIZWXWvg9jhjcPaurExKglXEIh1wf/zbuTND5p2RCUY5Xhc00z1g2U" +
              "b1WMLl86HiFKeiGAJIsbV0rP7ZeRWYJyIkGp/6EjFEfjiSuE7g0QlDcS7usyriiLsqdCfqmxnCdQ" +
              "YI9DZeL2pkIrYqWFIbg7hDpr81AA1g47SLNXv3KbznL22pE8vNidF+wG6Qh8u4m2Acc6snNzwMyd" +
              "G0yHvW8t2sLQ6Xo8QMOW1kZLDvbeS8kk6wB+4DcTgWgDB5L/8NuW0l25DN7IOOzUg8MKwcF+C6lr" +
              "fwFwhjBIb9bKzQ7WZCopgi74bBq93RYLG4LpSk3h6oeBVHt/qGIaQM8DRNXrCrHJUVxC3wT7SHc/" +
              "r5c72I6pOiQ4HryBo8kEMJgsdCOKAOdc+Rs1OPpBiKgw3/vDZrvAbMmf7Msh2L50YN1xkCFdSZNx" +
              "Q37STrlsDnSSGuCl5XoS+ZG67mB2hSFVnPv6x1YM0/Z1DKvNCJh1rSracdREMKHteDibcUBymUoh" +
              "TgvaFEYV/y6m12/ihQZmQe+ss97c9UVYR7iB7dxfNtUQ4hehZc9bdPVxbwPMPoWEFfym9+NWpzez" +
              "HRLU86d7EN9ytsvevWJCGrB68+W2cDCMrZdK829eYsw070zjodIhgTw3Xmn54PbUn00fK0PAwY5x" +
              "Esbi0BRsc0F56QFnBFEc9uWNL5Sl3VshZrRloGnWTQu9iqrjvQTx+Ros4CzNREmT3l/729jpIR2m" +
              "+qLvbN4CVWYrF88xKQ7raFKaE3bDYdjbuTqV/ks+d/aP4Cdg0M69IgcG2p1NIwfVgy35a04Ezate" +
              "f2wnWv1TWbBfATPzr16RDtlSeOw2Dmpi6s3HQtpJCoLH38CpO/P67isn5dp6xPDX+B2ebYKlpRqf" +
              "2Bs/lPUiIByUGrRDLYQJE5GXpzpciqd9bUu2cr/JADXLWvD96hTKfx6DoAyZ2457eClsz9F0CERX" +
              "5V6LsKQtUo+x/B4qVDAKrIuEBQtMpEw+sw2KGjcNkPnp/+r8+N+64bapFbFTBvCixBRmzLBr5seQ" +
              "lr4300/7jyM42JW6+l04b6T83IyeYFAIjwdp5mRVj211MrtN2cLgsheTySMdgwg9zQrq2bi3sj1O" +
              "X274567IrGOzubiX7dNBhweWzaHgV5WYgO4xRczoHbIgAvPQJYWKvdz4xAJ5tZfF6LIh2RYhrEe5" +
              "b0o6pKoEVXNrQD7KgVkk3jNHLfiD04kwVW2PQQ1LKZD4COnz9XQ7Ady3DsfuMVgwMwYJKoZIhvcN" +
              "AQkUMSYeJABzAHYAaQBsAHUAcABwAG8ALgBtAHkAcABhAHkANAAuAGkAdDAhBgkqhkiG9w0BCRUx" +
              "FAQSVGltZSAxNjM1MTY3Njg2NTU1MIIEOwYJKoZIhvcNAQcGoIIELDCCBCgCAQAwggQhBgkqhkiG" +
              "9w0BBwEwKAYKKoZIhvcNAQwBBjAaBBRTF4IU6LVVSEF5nRNJdC+WDSICFAICBACAggPoW31mPhA3" +
              "BhZeiCJrdorZPav//JGLR+zzYnQ5o8FrNBsY4zkl6VQsl0J6ilCXxJd/ysgr3f9osvZMrL4x5Sge" +
              "8QjH7LI1bSHFkSMvLiNDJ9BUb56sjXSIZ2D4A2mF5JFu68jSS5X6JJyW4RFgoTyAEaMBx8rMX2Kt" +
              "FceJzY6FSRfAzrX4TXo8UC27DYrxk7Fz2E9ysuyEOeycj802r5BidpYMObK/6/S6chR3eojkUrJ2" +
              "kZKwq4AuWA3E3/Bmsqc1lQNAwYUHmKXvcvYln6z/Xgt6Y8MytEOU0HAtVYOdGgyIuR+TERBTndou" +
              "jMOQCZrCb1E5Id778Z9WjQl0V2+ciealT8zY5a6D9mpR5rSQlnpUvOKmaNQHi4XcIVL8uOz+6RhW" +
              "PWMFoDi0x4ulQse5Mn/uso570ZCS6UJ/y35YhWYxiYJV0M/oykegbVthnV6mV5YiLfQzkR/wiLBK" +
              "ymRzw7/7ZQSEWNOABEY/QZ21fsms8yOqh4SIXMkPxNABP6h7RM1BSAOFR+xZxUuEQ+qDoOq7Uu6/" +
              "qXhdfk2dvF1p/V7A8vwjuqc9meI57gYUtQJh6B9dPgAcUN7h7jyBLE5s20xn7ZleEtz1quZTq3a8" +
              "2BH+6Uh+i6yHz4uR6SFGUmJeWveJ9owaAWq3YAKGt2u58o/GvaWgyd71M1DJAiIgyFghOrOKUjkA" +
              "JUsh38krxaZ9OgYkp4K+P1kufSqARt8oIHJy7dUqn8TJ9TJY1V33lxBAH8mJofeMPsk34Sj30VY0" +
              "s2ttkpf+pEhh1JUaSrt1/1FxRCXdKL/vA2RjmRV21jyoKr0EJMvVhA8LXKIia+Z05f3TcGIC+bEl" +
              "hKb1R1INCUDUC1liIC7LOGjUjR2r59R9qU0IkmBtv2jlKltJbuwMN1bJXAa5jR2rqUwInnejW/2p" +
              "cYyHMlcP50qWhoXNOi5KfFMGdQW6dk27fywjlFykcvP9ii8IvkR7kl3D1fZufBXZSXyd1XtefDed" +
              "8S5vK1MeLceceiV9nQJBEdveNkNJkNElwEUaBr8NmnVDe7jleGhXbZmdH9KCvXZVGG0Om3xMGzI/" +
              "DUsV0prc03V8/uUAs2aIlFKiE31qlDiHvjrQ6BdquNkXISl8oIn7ukKXqFIxrAoxqk0IerJoOj51" +
              "AyWeNV8q+pM72kvVU7QbvpeSaontlFq9NQPBv7RzA1GsHxJZAksc5EOjFwZzMJEYqRmD6g3wAant" +
              "V9pMm3gLLBqXup4wvG1YYTFH35P8f7DBK37mY7CBjBU7Q8BbkWaUfn9eUVHm6aeWXt8TuUfklg+1" +
              "ZsgIdmsOukPsGORgPxSTx9Pz8ycHJXrCwTA9MCEwCQYFKw4DAhoFAAQU4mRz4BFbaAsyk9xkcmpn" +
              "y31ydiQEFLq1REWAQ8LWSDePVl3U+yGcLn22AgIEAA==";
      String p12Pwd = "sviluppoks";
      String p12KeyPwd = "sviluppo";

      String trustContent =
          "MIIHXAIBAzCCBxYGCSqGSIb3DQEHAaCCBwcEggcDMIIG/zCCBvsGCSqGSIb3DQEHBqCCBuwwggbo" +
              "AgEAMIIG4QYJKoZIhvcNAQcBMCgGCiqGSIb3DQEMAQYwGgQUaMfhStsV4WPuEj7BmPutihzYRpIC" +
              "AgQAgIIGqPzGGYA78x/xgPT/qOyN1MpuTc6sxtU0mADpUBskZQkvU0YsXZ3y6u7w3Oze90kgK3Qf" +
              "RlYstCG0pieT4V90jSIfG98INBocdoEuHzFAr8TkTbK37lH52UW6+5BdBTCWFRrYhOwLjwofuUeG" +
              "KFuoD2uneak8s/GdkSfySKik4ITz6YXZZpDxftLb4matxT8OPvCr5gy+fPQaJVW4fEybK2YJHnur" +
              "KuBehhQoeDwZnCw+XgiZy40r6wZ7qn4olsuQKyuDnOzCDYpaqkFw2hzGh77O1Yij+p8+Gbp5lSpo" +
              "4Uzox4yed8ePincaOHNaPs6PCiI8rYTxTNnlZwq+Z/dccWKcUuAaJgcHCjGiEHjcsg5GjJVmLf4Q" +
              "wYK6XmeSspD7vjRu9BpInm0gqbFd8uqH5bz1G7rdbvMWT7TqXcVCa8M+8t7t+KAmh2pZ4/bEG+3z" +
              "KMBM3hKMC1OSuFNQtQzwFrSszBmtkwvtsORB3wvnrArMJY6DR27PwpulPOBYx07bWDEZFTcXOtUg" +
              "EOOQIE6l3CghE9UthFvsLJ9KzZ7hKoukSyhgU81TYBeflPU6MgX6Mw30nGkKJXrnmYe+5nbxZeTg" +
              "7XfGHdApk9XcwENtb1TzWYRalP58sZ2qhoFYdWmzXaozDRyVEXCJasqJEmXgslcfRqM8TBVMIYPA" +
              "d0GfoejmlJjbLHAWEjoKnjyG9qbGEhuJWn29mq06TSfDNNpZFZsd9NWf1rZ+NEE2Rnw4oLQ1M/EW" +
              "LAsk7+5nqx4RlnqIjt3A1X+EoUqKklUoBhLLps8T0F5I4K0UA+91KFvckipNk/E1tyGRbQ1iGeMI" +
              "lfTIvRzsN6/SdZlkuR2k5/HgwZ6GEW6MPIJrl+AecbcYM9UUkLHfIuFA8QxM+L7ec0gmcylcOARu" +
              "Skyv4EaXYasfDNG96M+50N/iyqIHbuKEy0b7ls78o7UP3MB2AKh5G0JQj48R/u4jS9/+Kb6apTnx" +
              "SoBF5aOplpE8sH4ahim9KxFjCt8LVjPlWe5JcVnLy8GzGJroqYBBaeKxscDSVvdm4kWJfaEleWtl" +
              "bAz7z4VyvK+Tc0rc4v7xnO5ojraiEq/KFSbztU3AofKswOSvlDmAkG5q1GrDKn2nffx9qNTVbxSR" +
              "2MnmYcV+ItKQmEXbb6NU4R18TJAZBo7DGdR914dwB7Wq+xoqOrZt2XbSpMkcupwF0SlIR/0LMJb8" +
              "dKsYciIhgukThpSQ38R3MKq6lY9W55gXBhP862f8zsruWF6db3NY+zTW3nXwgqUmnLYjoHz3beqy" +
              "YKyQOO9KeljJY+srWr4Q0tZZzFtCqoTLrZJHfLOPg6UjCZDOGAPGJ2a8uOD/IOee3YjGOOQ4ApLv" +
              "YKnnXihDLtT4nZBQN0/2LXyiDgY1ZK08VJc29XXRGw6FihDDP4JJ7Q4YW1Gf422UH0YR/X3zh2km" +
              "bzN00tQOPZOcWvjna3hPzOR3BDuZlnShZfEDoxG8/jTYwXRlE6MNV5KaHP9oezK4/3l5b+J7bvcQ" +
              "qXqK2W4PsFE1avRiymf0Z0V0DOQDcj4cJP5lAgsZwFI/IWAj0Loe//+StVmns11oBf6EQBbvuejn" +
              "nZO8o8CbzoEDyfvRsqZK+9dZgIRsSuHGaup7dtkaL7aI3FRXw0g8UgL4/SoNmCRX/sXEMmHRKMSn" +
              "6Ur0zvZLTweloh++4hnDTau3G/yVy3qm9EEudc2yuq3LJZe3HMt7CZT4ZyIl41U9vNgxBQ/cWFP/" +
              "wfcCYF6is/Rb6fQX4s7+fdYiuwbugThbKXIGcoadt09DxpWgwGRqxSCGaNntq/e6Dn9DJLTl/kVB" +
              "KldDzNeg44QD/HGxr9pxCTMNOPp94fqXNF4u1PwQo5D5ZSeNxyZDpEv6xQvMPjyrdlJ8bTLoxRBM" +
              "xNMJCAdEfUizd0LkW4eTd7ZKPCbnpEIDOb5Q3xb2Sns4mhwrVaMHqDae3Cf+RCgJn7ILzIOAFK0X" +
              "wmDe7XNucBcMrycawwzp5g2w6gESf5jhLskNWz76Sv5sXcIkAONf8swMWnQS7/TiIBqmxt6Xcq45" +
              "4Dn5GE8ThzMnFDLkJ4ADHKJMwNUy70elmPgX5nXCBDddif6ruCZvfMLpCy9LoJTsf7qXb/E0gsEy" +
              "RSjLXc0U5bvH0oDiVfQMkOg9LKUTQ74zueRzbAK6JRSdVrHOytyAG2cJwm58MvBONtldga29Fe0L" +
              "yn0XamqKmwilRxC7DaBdCXLZwOxhPFvB4WnM+rX8/CB8w6aJ6Y3A2T46uv1IDnVnM8YwhJKl1cd3" +
              "IzA9MCEwCQYFKw4DAhoFAAQUiBVK8PX2HK9pOVjIZHJQkEK+6sYEFEe7VZKuiP/KmfGYDLVCIOkH" +
              "AmKhAgIEAA==";



      HttpClientBuilderHelper builder = HttpClientBuilderHelper.create(new StandardEnvironment());
      System.setProperty("testP12.keystore_content",p12Base64);
      System.setProperty("testP12.keystore_password",p12Pwd);
      System.setProperty("testP12.key_password",p12KeyPwd);
      System.setProperty("testP12.truststore_content",trustContent);
      System.setProperty("testP12.truststore_password","pwd");
      builder.addProxySupport();
      builder.addCustomSSLSupport("testP12");
      final HttpClient httpClient = builder.build();

      HttpGet httpGet = new HttpGet("https://prod.idrix.eu/secure/");
      HttpResponse httpResponse = httpClient.execute(httpGet);
      Scanner sc = new Scanner(httpResponse.getEntity().getContent());
      //Printing the status line
      System.out.println("status:"+httpResponse.getStatusLine());
      while(sc.hasNext()) {
        System.out.println(sc.nextLine());
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

}
