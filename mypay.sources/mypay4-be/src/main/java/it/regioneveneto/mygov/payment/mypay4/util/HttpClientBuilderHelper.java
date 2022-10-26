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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Scanner;
import java.util.function.Function;

@Slf4j
public class HttpClientBuilderHelper {

  private final HttpClientBuilder _instance;

  private HttpClientBuilderHelper(){
    this._instance = HttpClientBuilder.create();
  }

  public static HttpClientBuilderHelper create() {
    return new HttpClientBuilderHelper();
  }

  public HttpClient build(){
    return _instance.build();
  }

  public HttpClientBuilderHelper addProxySupport(Environment env) {
    this.addProxySupport(env, false);
    return this;
  }

  public HttpClientBuilderHelper addProxySupport(Environment env, boolean withSoapHeadersFix) {
    Function<String, String> getProp = key -> env.getProperty(key, System.getProperty(key));
    if (StringUtils.isNotBlank(getProp.apply("http.proxyHost"))) {
      String proxyHost = getProp.apply("http.proxyHost");
      int proxyPort = Integer.parseInt(getProp.apply("http.proxyPort"));
      _instance.useSystemProperties();
      HttpHost proxy = new HttpHost(proxyHost, proxyPort);
      _instance.setProxy(proxy);
      log.debug("setting HttpClient proxy: {}:{}", proxyHost,proxyPort);

      if(StringUtils.isNotBlank(getProp.apply("http.nonProxyHosts"))){
        String[] nonProxyList = StringUtils.split(getProp.apply("http.nonProxyHosts"), '|');
        log.debug("setting HttpClient proxy exclusion for: ", StringUtils.joinWith(";",(Object[])nonProxyList));
        _instance.setRoutePlanner(new DefaultProxyRoutePlanner(proxy) {
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


      if (StringUtils.isNotBlank(getProp.apply("http.proxyUser"))) {
        String proxyUser = getProp.apply("http.proxyUser");
        String proxyPassword = getProp.apply("http.proxyPassword");
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPassword));
        _instance.setDefaultCredentialsProvider(credsProvider);
        _instance.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        log.debug("setting HttpClient proxyUser: {} - password: ***", proxyUser);
      }
    } else {
      log.debug("no HttpClient proxy properties detected");
    }
    if(withSoapHeadersFix){
      _instance.addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor());
    }
    return this;
  }

  public HttpClientBuilderHelper addHttpsClientCertSupport(Environment env, String propPrefix) {
    Function<String, String> getProp = key -> env.getProperty(key, System.getProperty(key));
    try {

      InputStream keyStoreStream;
      if (StringUtils.isNotBlank(getProp.apply(propPrefix+".keystore_content"))) {
        keyStoreStream = new ByteArrayInputStream(Base64.decodeBase64(getProp.apply(propPrefix+".keystore_content")));
        log.debug("set custom KeyStore (by base64 string)");
      } else if (StringUtils.isNotBlank(getProp.apply(propPrefix+".keystore_path"))) {
        keyStoreStream = new FileInputStream(getProp.apply(propPrefix+".keystore_path"));
        log.debug("set custom KeyStore (by file)");
      } else {
        log.debug("no HTTPS client certificate properties detected");
        return this;
      }
      KeyStore keyStore = KeyStore.getInstance("pkcs12");
      String keyStorePassword = getProp.apply(propPrefix+".keystore_password");
      Assert.notNull(keyStorePassword, "configuration property '"+propPrefix+".keystore_password' could not be null");
      String keyPassword = getProp.apply(propPrefix+".key_password");
      keyStore.load(keyStoreStream, keyStorePassword.toCharArray());

      InputStream trustStoreStream = null;
      if (StringUtils.isNotBlank(getProp.apply(propPrefix+".truststore_content"))) {
        trustStoreStream = new ByteArrayInputStream(Base64.decodeBase64(getProp.apply(propPrefix+".truststore_content")));
        log.debug("set custom TrustStore (by base64 string)");
      } else if (StringUtils.isNotBlank(getProp.apply(propPrefix+".truststore_path"))) {
        trustStoreStream = new FileInputStream(getProp.apply(propPrefix+".truststore_path"));
        log.debug("set custom TrustStore (by file)");
      }
      KeyStore trustStore = null;
      if(trustStoreStream != null) {
        trustStore = KeyStore.getInstance("pkcs12");
        String trustStorePassword = getProp.apply(propPrefix+".truststore_password");
        Assert.notNull(trustStorePassword, "configuration property '"+propPrefix+".truststore_password' could not be null");
        trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
      }

      SSLContext sslContext = SSLContexts.custom()
          .loadKeyMaterial(keyStore,  keyPassword!=null ? keyPassword.toCharArray() : null)
          .loadTrustMaterial(trustStore, null)
          .build();
      _instance.setSSLContext(sslContext);
      log.debug("setting HTTPS client certificate");
    }catch(Exception e){
      log.error("cannot set HTTPS client certificate", e);
      throw new MyPayException("cannot set HTTPS client certificate", e);
    }
    return this;
  }

  public static void main(String[] args) {
    try{

      String P12_BASE64 =
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
      String P12_PWD = "sviluppoks";
      String P12_KEY_PWD = "sviluppo";

      String TRUST_CONTENT =
          "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" +
          "AmKhAgIEAA==";



      HttpClientBuilderHelper builder = HttpClientBuilderHelper.create();
      System.setProperty("testP12.keystore_content",P12_BASE64);
      //System.setProperty("testP12.keystore_path","/tmp/keystore.p12");
      System.setProperty("testP12.keystore_password",P12_PWD);
      System.setProperty("testP12.key_password",P12_KEY_PWD);
      System.setProperty("testP12.truststore_content",TRUST_CONTENT);
      //System.setProperty("testP12.truststore_path","/tmp/truststore.p12");
      System.setProperty("testP12.truststore_password","pwd");
      Environment env = new StandardEnvironment();
      builder.addProxySupport(new StandardEnvironment());
      builder.addHttpsClientCertSupport(env, "testP12");
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
