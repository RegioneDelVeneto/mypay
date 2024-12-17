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

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.ws.server.*;
import it.regioneveneto.mygov.payment.mypay4.ws.server.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.ws.util.MyEndpointInterceptor;
import it.regioneveneto.mygov.payment.mypay4.ws.util.MyWsdl11Definition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.support.WebUtils;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.transport.http.WsdlDefinitionHandlerAdapter;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EnableWs
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class SoapWebServiceConfig extends WsConfigurerAdapter {

  @Value("${ws.wsdl}")
  private String dynamicwsdl;

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private MyEndpointInterceptor myEndpointInterceptor;

  public static final String WS_PATH_PA = MyPay4AbstractSecurityConfig.PATH_WS+"/pa/";
  public static final String WS_PATH_FESP = MyPay4AbstractSecurityConfig.PATH_WS+"/fesp/";

  public static final String XSD_PagInf_Dovuti_Pagati_6_2_0 = "PagInf_Dovuti_Pagati_6_2_0";
  public static final String XSD_PagInf_RP_Esito_6_2_0 = "PagInf_RP_Esito_6_2_0";
  public static final String XSD_PagInf_RPT_RT_6_2_0 = "PagInf_RPT_RT_6_2_0";
  public static final String XSD_PaForNode = "paForNode";
  public static final String XSD_SacCommonTypes = "sac-common-types-1.0";
  public static final String XSD_MarcaDaBollo = "MarcaDaBollo";
  public static final String XSD_XmldsigCoreSchema = "xmldsig-core-schema";



  public static Set<String> WS_PATH_NAME_SET = new HashSet<>();


  public static final Map<String, String> XSD_NAME_PATH_MAP = Map.of(
      XSD_PagInf_Dovuti_Pagati_6_2_0, WS_PATH_PA,
      XSD_PagInf_RP_Esito_6_2_0, WS_PATH_PA,
      XSD_PagInf_RPT_RT_6_2_0,WS_PATH_FESP,
      XSD_PaForNode,WS_PATH_FESP,
      XSD_SacCommonTypes,WS_PATH_FESP,
      XSD_MarcaDaBollo, WS_PATH_FESP,
      XSD_XmldsigCoreSchema, WS_PATH_FESP
  );

  @Override
  public void addInterceptors(List<EndpointInterceptor> interceptors) {
    interceptors.add(myEndpointInterceptor);
  }

  private void registerWsdlDefinition(String path){
    String contextRoot;
    try{
      contextRoot = new URI(appBeAbsolutePath).getPath().replaceAll("/$", "");
    } catch(Exception e){
      throw new MyPayException("invalid app.be.absolute-path ["+appBeAbsolutePath+"]", e);
    }
    log.debug("register ws soap: {}",contextRoot + path);
    WS_PATH_NAME_SET.add(contextRoot + path);
    log.trace("WS_PATH_NAME_SET contains now: {}",WS_PATH_NAME_SET);
  }

  private static String extractPathFromUrlPath(String urlPath) {
    int end = urlPath.indexOf('?');
    if (end == -1) {
      end = urlPath.indexOf('#');
      if (end == -1) {
        end = urlPath.length();
      }
    }
    int begin = urlPath.lastIndexOf('/', end) + 1;
    //int paramIndex = urlPath.indexOf(';', begin);
    return urlPath.substring(0, begin);
  }

  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet(){
      @Override
      protected WsdlDefinition getWsdlDefinition(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String name = WebUtils.extractFilenameFromUrlPath(uri);
        String path = extractPathFromUrlPath(uri);
        log.trace("getWsdlDefinition uri:{} path:{} name:{} found:{}", uri, name, path, WS_PATH_NAME_SET.contains(path+name));
        if(WS_PATH_NAME_SET.contains(path+name))
          return super.getWsdlDefinition(request);
        else
          return null;
      }

      @Override
      protected XsdSchema getXsdSchema(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String name = WebUtils.extractFilenameFromUrlPath(uri);
        String path = extractPathFromUrlPath(uri);
        String xsdPath = request.getContextPath()+XSD_NAME_PATH_MAP.getOrDefault(name,"__NOT_FOUND__");
        if(xsdPath.equals(path))
          return super.getXsdSchema(request);
        else
          return null;
      }
    };
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, MyPay4AbstractSecurityConfig.PATH_WS + "/*");
  }

  @Bean("wsdlDefinitionHandlerAdapter")
  public WsdlDefinitionHandlerAdapter getWsdlDefinitionHandlerAdapter(){
    return new WsdlDefinitionHandlerAdapter(){
      private WsdlDefinitionHandlerAdapter instance = this;
      @Override
      protected String transformLocation(String location, HttpServletRequest request) {
        //do not take url from request, because it may be changed by proxy / ingress. Use application property
        StringBuilder url = new StringBuilder(appBeAbsolutePath);
        if (location.startsWith("/")) {
          url.append(location);
          return url.toString();
        } else {
          log.error("wsdl url in location must start with / : [{}]", request.getRequestURL());
          return super.transformLocation(location, request);
        }
      }
    };
  }

  @Bean
  public XsdSchemaCollection getXsdSchemaCollection() {
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      final String XSD_PATH = "/xsd/";
      CommonsXsdSchemaCollection xsds = new CommonsXsdSchemaCollection(
          new ClassPathResource(XSD_PATH + "digitpa.xsd"),
          new ClassPathResource(XSD_PATH + "pagam.xsd"),
          new ClassPathResource(XSD_PATH + "pagam_head.xsd"),
          new ClassPathResource(XSD_PATH + "ven_fesp.xsd"),
          new ClassPathResource(XSD_PATH + "ven_fesp_head.xsd")
      );
      xsds.setInline(true);
      return xsds;
    } else {
      return null;
    }
  }

  @Bean(name = PagamentiTelematiciRPEndpoint.NAME)
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
  public Wsdl11Definition pagamentiTelematiciRPEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_FESP+PagamentiTelematiciRPEndpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(PagamentiTelematiciRPEndpoint.NAME + "Port");
      def.setServiceName(PagamentiTelematiciRPEndpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_FESP + PagamentiTelematiciRPEndpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(PagamentiTelematiciRPEndpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/fesp/nodo-regionale-per-pa.wsdl"));
    }
  }

  @Bean(name = PagamentiTelematiciAvvisiDigitaliEndpoint.NAME)
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
  public Wsdl11Definition pagamentiTelematiciAvvisiDigitaliEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_FESP+PagamentiTelematiciAvvisiDigitaliEndpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(PagamentiTelematiciAvvisiDigitaliEndpoint.NAME + "Port");
      def.setServiceName(PagamentiTelematiciAvvisiDigitaliEndpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_FESP + PagamentiTelematiciAvvisiDigitaliEndpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(PagamentiTelematiciAvvisiDigitaliEndpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/fesp/nodo-regionale-per-pa-avvisi-digitali.wsdl"));
    }
  }

  @Bean(name = PagamentiTelematiciCCP25Endpoint.NAME)
  @ConditionalOnExpression("'${fesp.mode}'.equals('local') && !${pa.modelloUnico}")
  public Wsdl11Definition pagamentiTelematiciCCP25Endpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_FESP+PagamentiTelematiciCCP25Endpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(PagamentiTelematiciCCP25Endpoint.NAME + "Port");
      def.setServiceName(PagamentiTelematiciCCP25Endpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_FESP + PagamentiTelematiciCCP25Endpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(PagamentiTelematiciCCP25Endpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/fesp/paForNode.wsdl"));
    }
  }

  @Bean(name = PagamentiTelematiciRTEndpoint.NAME)
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
  public Wsdl11Definition pagamentiTelematiciRTEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_FESP+PagamentiTelematiciRTEndpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(PagamentiTelematiciRTEndpoint.NAME + "Port");
      def.setServiceName(PagamentiTelematiciRTEndpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_FESP + PagamentiTelematiciRTEndpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(PagamentiTelematiciRTEndpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/fesp/nodo-regionale-per-nodo-spc.wsdl"));
    }
  }

  @Bean(name = PagamentiTelematiciCCPPaEndpoint.NAME)
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "remote")
  public Wsdl11Definition pagamentiTelematiciCCPPaEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_PA+PagamentiTelematiciCCPPaEndpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(PagamentiTelematiciCCPPaEndpoint.NAME + "Port");
      def.setServiceName(PagamentiTelematiciCCPPaEndpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_PA + PagamentiTelematiciCCPPaEndpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(PagamentiTelematiciCCPPaEndpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/pa/pa-per-nodo-regionale-pagamento-presso-psp.wsdl"));
    }
  }

  @Bean(name = PagamentiTelematiciEsitoEndpoint.NAME)
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "remote")
  public Wsdl11Definition pagamentiTelematiciEsitoEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_PA+PagamentiTelematiciEsitoEndpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(PagamentiTelematiciEsitoEndpoint.NAME + "Port");
      def.setServiceName(PagamentiTelematiciEsitoEndpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_PA + PagamentiTelematiciEsitoEndpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(PagamentiTelematiciEsitoEndpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/pa/pa-per-nodo-regionale.wsdl"));
    }
  }

  @Bean(name = PagamentiTelematiciDovutiPagatiEndpoint.NAME)
  public Wsdl11Definition pagamentiTelematiciDovutiPagatiEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_PA+PagamentiTelematiciDovutiPagatiEndpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(PagamentiTelematiciDovutiPagatiEndpoint.NAME + "Port");
      def.setServiceName(PagamentiTelematiciDovutiPagatiEndpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_PA + PagamentiTelematiciDovutiPagatiEndpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(PagamentiTelematiciDovutiPagatiEndpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/pa/pa-per-ente.wsdl"));
    }
  }

  @Bean(name = AllineamentoMyCSEndpoint.NAME)
  public Wsdl11Definition allineamentoMyCSEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_PA+AllineamentoMyCSEndpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(AllineamentoMyCSEndpoint.NAME + "Port");
      def.setServiceName(AllineamentoMyCSEndpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_PA + AllineamentoMyCSEndpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(AllineamentoMyCSEndpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/pa/pa-per-mycs.wsdl"));
    }
  }

  @Bean(name = PagamentiTelematiciFlussiSPCEndpoint.NAME)
  public Wsdl11Definition pagamentiTelematiciFlussiSPCEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_PA+PagamentiTelematiciFlussiSPCEndpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(PagamentiTelematiciFlussiSPCEndpoint.NAME + "Port");
      def.setServiceName(PagamentiTelematiciFlussiSPCEndpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_PA + PagamentiTelematiciFlussiSPCEndpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(PagamentiTelematiciFlussiSPCEndpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/pa/mypivot-pa-per-ente.wsdl"));
    }
  }

  @Bean(name = PagamentiTelematiciCCP36Endpoint.NAME)
  @ConditionalOnExpression("'${fesp.mode}'.equals('local') && ${pa.modelloUnico}")
  public Wsdl11Definition pagamentiTelematiciCCP36Endpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_FESP+PagamentiTelematiciCCP36Endpoint.NAME);
    if("dynamic".equalsIgnoreCase(dynamicwsdl)) {
      MyWsdl11Definition def = new MyWsdl11Definition();
      def.setPortTypeName(PagamentiTelematiciCCP36Endpoint.NAME + "Port");
      def.setServiceName(PagamentiTelematiciCCP36Endpoint.NAME + "Service");
      def.setLocationUri(WS_PATH_FESP + PagamentiTelematiciCCP36Endpoint.NAME);
      def.setRequestSuffix("");
      def.setResponseSuffix("Risposta");
      def.setTargetNamespace(PagamentiTelematiciCCP36Endpoint.NAMESPACE_URI);
      def.setSchemaCollection(xsdSchemaCollection);
      return def;
    } else {
      return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/fesp/paForNode.wsdl"));
    }
  }

  @Bean(name = XSD_PagInf_Dovuti_Pagati_6_2_0)
  public XsdSchema getPagInf_Dovuti_Pagati_6_2_0Xsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/pa/"+XSD_PagInf_Dovuti_Pagati_6_2_0+".xsd"));
  }

  @Bean(name = XSD_PagInf_RP_Esito_6_2_0)
  public XsdSchema getPagInf_RP_Esito_6_2_0Xsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/pa/"+XSD_PagInf_RP_Esito_6_2_0+".xsd"));
  }

  @Bean(name = XSD_PagInf_RPT_RT_6_2_0)
  public XsdSchema getPagInf_RPT_RT_6_2_0Xsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_PagInf_RPT_RT_6_2_0+".xsd"));
  }

  @Bean(name = XSD_PaForNode)
  public XsdSchema getPaForNodeXsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_PaForNode+".xsd"));
  }

  @Bean(name = XSD_SacCommonTypes)
  public XsdSchema getSacCommonTypesXsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_SacCommonTypes+".xsd"));
  }

  @Bean(name = XSD_MarcaDaBollo)
  public XsdSchema getMarcaDaBolloXsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_MarcaDaBollo+".xsd"));
  }

  @Bean(name = XSD_XmldsigCoreSchema)
  public XsdSchema getXmldsigCoreSchemaXsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_XmldsigCoreSchema+".xsd"));
  }
}
