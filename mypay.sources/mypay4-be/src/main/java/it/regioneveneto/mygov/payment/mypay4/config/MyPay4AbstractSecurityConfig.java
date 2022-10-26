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

import io.jsonwebtoken.lang.Assert;
import it.regioneveneto.mygov.payment.mypay4.security.*;
import it.regioneveneto.mygov.payment.mypay4.service.UtenteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class MyPay4AbstractSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

  public final static String PATH_PUBLIC = "/public";
  public final static String PATH_APP_ADMIN = "/appadmin";
  public final static String PATH_WS = "/ws";
  public final static String PATH_A2A = "/a2a";
  public final static String PATH_EMAIL_VALIDATION = "/email";
  public final static String PATH_OPERATORE = "/operatore";

  @Value("${cors.enabled:false}")
  private String corsEnabled;
  @Value("${auth.fake.enabled:false}")
  protected String fakeAuthEnabled;

  @Value("${static.serve.enabled:false}")
  private String staticContentEnabled;
  @Value("${static.serve.paths:/staticContent}")
  private String[] staticContentPaths;
  @Value("${static.serve.locations:/staticLocation}")
  private String[] staticContentLocations;

  @Value("${server.error.path:/error}")
  private String errorPath;

  @Autowired
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  @Autowired
  private FakeUserDetailsService fakeUserDetailsService;
  @Autowired
  private JwtRequestFilter jwtRequestFilter;
  @Autowired
  private AuthorizationHeaderWriter authorizationHeaderWriter;
  @Autowired
  private JwtTokenUtil jwtTokenUtil;
  @Autowired
  private UtenteService utenteService;

  //TODO: this is a temporary workaround to fix the behaviour introduced with SpringBoot 2.6
  // of error page not shown when user is not authenticated or a NotAuthenticated error is thrown
  // see here for further details:
  // - https://github.com/spring-projects/spring-boot/issues/26356
  // - https://github.com/spring-projects/spring-boot/issues/28759
  // - https://github.com/spring-projects/spring-boot/issues/28953
  // - https://github.com/spring-projects/spring-boot/issues/29299
  @Bean
  static BeanFactoryPostProcessor removeErrorSecurityFilter() {
    return (beanFactory) ->
        ((DefaultListableBeanFactory)beanFactory).removeBeanDefinition("errorPageSecurityInterceptor");
  }

  @Bean
  public SecurityConfigWhitelist getSecurityWhitelist(){
    log.debug("MyPay4AbstractSecurityConfig.getSecurityWhitelist()");
    return new SecurityConfigWhitelist(new String[]{ "/favicon.ico" }, new String[]{PATH_PUBLIC+"/**", PATH_WS+"/**"});
  }

  @Bean
  public MethodInvokingFactoryBean methodInvokingFactoryBean() {
    MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
    methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
    methodInvokingFactoryBean.setTargetMethod("setStrategyName");
    methodInvokingFactoryBean.setArguments(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    return methodInvokingFactoryBean;
  }

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  @Bean
  public PasswordEncoder passwordEncoder() {
    return this.passwordEncoder;
  }

  @Bean
  public DaoAuthenticationProvider usernamePasswordAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(fakeUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  public void configure(WebSecurity web) {
    SecurityConfigWhitelist securityConfigWhitelist = getSecurityWhitelist();
    String[] securityWhitelist = ArrayUtils.addAll(securityConfigWhitelist.getSecurityWhitelist(), errorPath);
    if("true".equalsIgnoreCase(staticContentEnabled)) {
      log.warn("serving static content at path: " + ArrayUtils.toString(staticContentPaths));
      String[] pathsToWhitelist = new String[0];
      for(String aPath: staticContentPaths)
        pathsToWhitelist = ArrayUtils.addAll(pathsToWhitelist, aPath, aPath+"/**");
      securityWhitelist = ArrayUtils.addAll(securityWhitelist, pathsToWhitelist);
    }
    web.ignoring().antMatchers(securityWhitelist);
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    SecurityConfigWhitelist securityConfigWhitelist = getSecurityWhitelist();
    //HttpSecurity httpSecurityRef = httpSecurity;
    if("true".equalsIgnoreCase(corsEnabled)) {
      log.warn("enabling CORS (security)");
      httpSecurity = httpSecurity.cors().and();
    }
    httpSecurity.csrf().disable()
        .authorizeRequests()
        .antMatchers(ArrayUtils.addAll(securityConfigWhitelist.getAuthWithelist())).permitAll()
        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
        .anyRequest().authenticated().and()
        // add a filter to validate the tokens with every request
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
        // this is necessary to correctly manage the rolling authorization token
        .headers().addHeaderWriter(authorizationHeaderWriter).and()
        // define custom error handling
        .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
        // make sure we use stateless session; session won't be used to store user's state.
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    if("true".equalsIgnoreCase(corsEnabled)) {
      log.warn("enabling CORS");
      registry.addMapping("/**")
          .exposedHeaders(JwtRequestFilter.AUTHORIZATION_HEADER, HttpHeaders.CONTENT_DISPOSITION)
          .allowedMethods("*");
      //.allowCredentials(true).exposedHeaders("Set-Cookie");
    }
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    if("true".equalsIgnoreCase(staticContentEnabled)) {
      Assert.isTrue(staticContentPaths.length==staticContentLocations.length,
          "static content path and location not having same length: " +
              staticContentPaths.length + "/" + staticContentLocations.length);
      for(int i=0;i<staticContentPaths.length;i++){
        registry
            .addResourceHandler(staticContentPaths[i],staticContentPaths[i]+"/**")
            .addResourceLocations("file:"+staticContentLocations[i])
            .resourceChain(false)
            .addResolver(new PathResourceResolver() {
              @Override
              protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
                Resource resource = super.resolveResourceInternal(request, requestPath, locations, chain);
                if (resource == null)
                  resource = super.resolveResourceInternal(request, "index.html", locations, chain);
                return resource;
              }
            });
      }
    }
  }

  protected String generateRedirectLoginUrl(String callbackUrl, UserWithAdditionalInfo userDetails) {
    //update user info into mypay db from authentication system
    log.info("principal: "+userDetails);
    Map<String, Object> claims = utenteService.mapUserToLoginClaims(userDetails);
    final String token = jwtTokenUtil.generateLoginToken(userDetails.getUsername(), claims);
    String targetUrl = callbackUrl + "?login_token="+ URLEncoder.encode(token, StandardCharsets.UTF_8);
    log.debug("success authentication url: "+targetUrl);
    return targetUrl;
  }

}

