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
package it.regioneveneto.mygov.payment.mypay4.config;

import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.HttpClientBuilderHelper;
import it.regioneveneto.mygov.payment.mypay4.ws.client.PagamentiTelematiciCCPPaClient;
import it.regioneveneto.mygov.payment.mypay4.ws.client.PagamentiTelematiciEsitoClient;
import it.regioneveneto.mygov.payment.mypay4.ws.client.PagamentiTelematiciEsterniCCPClient;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciAvvisiDigitaliClient;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciAvvisiDigitaliServiceClient;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciRPClient;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciRPTClient;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.mock.PagamentiTelematiciRPTMockClient;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciAvvisiDigitali;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.regioneveneto.mygov.payment.mypay4.ws.server.fesp.PagamentiTelematiciAvvisiDigitaliEndpoint;
import it.regioneveneto.mygov.payment.mypay4.ws.server.fesp.PagamentiTelematiciRPEndpoint;
import it.regioneveneto.mygov.payment.mypay4.ws.util.MyEndpointInterceptor;
import it.regioneveneto.mygov.payment.mypay4.ws.util.PagoPAAuthClientInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
@Slf4j
public class SoapWebServiceClientConfig {

  @Value("${fesp.remoteurl}")
  String fespRemoteUrl;

  @Value("${fesp.mockPagoPa:false}")
  boolean mockPagoPa;

  @Value("${ws.pagamentiTelematiciAvvisiDigitali.remoteurl}")
  String pagamentiTelematiciAvvisiDigitaliRemoteUrl;

  @Value("${ws.pagamentiTelematiciRPT.remoteurl}")
  String pagamentiTelematiciRPTRemoteUrl;

  @Value("${pa.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  @Autowired
  Environment env;

  @Autowired
  MyEndpointInterceptor myEndpointInterceptor;

  @Autowired
  GiornaleService giornaleCommonService;

  @Autowired
  private PagoPAAuthClientInterceptor pagoPAAuthClientInterceptor;

  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "remote")
  public PagamentiTelematiciRP getPagamentiTelematiciRPClientRemote() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <jaxbJavaGenXXXX> task specified in build.gradle.kts
    // (i.e. in the XXX.xjb file corresponding to the WSDL)
    marshaller.setContextPath("it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa");
    PagamentiTelematiciRPClient client = new PagamentiTelematiciRPClient(giornaleCommonService,
      identificativoIntermediarioPA, identificativoStazioneIntermediarioPA);
    client.setInterceptors(new ClientInterceptor[]{myEndpointInterceptor});
    client.setDefaultUri(fespRemoteUrl+ PagamentiTelematiciRPEndpoint.NAME);
    client.setMarshaller(marshaller);
    client.setUnmarshaller(marshaller);
    return client;
  }

  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "remote")
  public PagamentiTelematiciAvvisiDigitali getPagamentiTelematiciAvvisiDigitaliClientRemote() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <jaxbJavaGenXXXX> task specified in build.gradle.kts
    // (i.e. in the XXX.xjb file corresponding to the WSDL)
    marshaller.setContextPath("it.veneto.regione.pagamenti.nodoregionalefesp");
    PagamentiTelematiciAvvisiDigitaliClient client = new PagamentiTelematiciAvvisiDigitaliClient(giornaleCommonService);
    client.setDefaultUri(fespRemoteUrl+ PagamentiTelematiciAvvisiDigitaliEndpoint.NAME);
    client.setMarshaller(marshaller);
    client.setUnmarshaller(marshaller);
    return client;
  }

  @Bean
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
  public PagamentiTelematiciCCPPaClient getPagamentiTelematiciCCPPaClientRemote() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <jaxbJavaGenXXXX> task specified in build.gradle.kts
    // (i.e. in the XXX.xjb file corresponding to the WSDL)
    marshaller.setPackagesToScan("it.veneto.regione.pagamenti.pa");
    PagamentiTelematiciCCPPaClient client = new PagamentiTelematiciCCPPaClient(giornaleCommonService,
      identificativoIntermediarioPA, identificativoStazioneIntermediarioPA);
    client.setInterceptors(new ClientInterceptor[]{myEndpointInterceptor});
    //for this WS the default Uri is not set, as it depends on ente
    client.setMarshaller(marshaller);
    client.setUnmarshaller(marshaller);
    return client;
  }

  @Bean
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
  public PagamentiTelematiciEsitoClient getPagamentiTelematiciEsitoClientRemote() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <jaxbJavaGenXXXX> task specified in build.gradle.kts
    // (i.e. in the XXX.xjb file corresponding to the WSDL)
    marshaller.setContextPath("it.veneto.regione.pagamenti.nodoregionalefesp");
    PagamentiTelematiciEsitoClient client = new PagamentiTelematiciEsitoClient(giornaleCommonService);
    //for this WS the default Uri is not set, as it depends on ente
    client.setMarshaller(marshaller);
    client.setUnmarshaller(marshaller);
    return client;
  }

  @Bean
  public PagamentiTelematiciEsterniCCPClient getPagamentiTelematiciEsterniCCPClientRemote() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <jaxbJavaGenXXXX> task specified in build.gradle.kts
    // (i.e. in the XXX.xjb file corresponding to the WSDL)
    marshaller.setContextPath("it.veneto.regione.pagamenti.nodoregionalefesp");
    PagamentiTelematiciEsterniCCPClient client = new PagamentiTelematiciEsterniCCPClient(giornaleCommonService,
      identificativoIntermediarioPA, identificativoStazioneIntermediarioPA);
    //for this WS the default Uri is not set, as it depends on ente
    client.setMarshaller(marshaller);
    client.setUnmarshaller(marshaller);
    return client;
  }

  @Bean
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
  public PagamentiTelematiciAvvisiDigitaliServiceClient getPagamentiTelematiciAvvisiDigitaliServiceClientRemote() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <jaxbJavaGenXXXX> task specified in build.gradle.kts
    // (i.e. in the XXX.xjb file corresponding to the WSDL)
    marshaller.setContextPath("it.veneto.regione.pagamenti.nodoregionalefesp");
    PagamentiTelematiciAvvisiDigitaliServiceClient client = new PagamentiTelematiciAvvisiDigitaliServiceClient(giornaleCommonService);
    client.setDefaultUri(pagamentiTelematiciAvvisiDigitaliRemoteUrl);
    client.setMarshaller(marshaller);
    client.setUnmarshaller(marshaller);
    return client;
  }

  @Bean
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
  public PagamentiTelematiciRPTClient getPagamentiTelematiciRPTClientRemote() {
    if(mockPagoPa)
      return new PagamentiTelematiciRPTMockClient();

    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <jaxbJavaGenXXXX> task specified in build.gradle.kts
    // (i.e. in the XXX.xjb file corresponding to the WSDL)
    marshaller.setContextPath("gov.telematici.pagamenti.ws.nodospcpernodoregionale");
    PagamentiTelematiciRPTClient client = new PagamentiTelematiciRPTClient(giornaleCommonService);
    client.setInterceptors(new ClientInterceptor[]{myEndpointInterceptor, pagoPAAuthClientInterceptor});
    client.setDefaultUri(pagamentiTelematiciRPTRemoteUrl);
    client.setMarshaller(marshaller);
    client.setUnmarshaller(marshaller);
    //add proxy and SSL Client Cert support
    client.setMessageSender(new HttpComponentsMessageSender(HttpClientBuilderHelper.create(env)
        .addProxySupport(true)
        .addCustomSSLSupport("ws.pagamentiTelematiciRPT.clientAuth")
        .build()));
    return client;
  }

}
