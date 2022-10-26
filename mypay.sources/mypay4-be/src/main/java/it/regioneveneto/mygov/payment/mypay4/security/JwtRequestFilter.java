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
package it.regioneveneto.mygov.payment.mypay4.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.SignatureException;
import it.regioneveneto.mygov.payment.mypay4.config.SecurityConfigWhitelist;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotAuthorizedException;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.service.UtenteProfileService;
import it.regioneveneto.mygov.payment.mypay4.storage.JwtTokenUsageStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig.PATH_A2A;
import static it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig.PATH_EMAIL_VALIDATION;

@Component
@Slf4j
@ConditionalOnWebApplication
public class JwtRequestFilter extends OncePerRequestFilter {

  public final static String AUTHORIZATION_HEADER = "Authorization";
  final static String CLAIMS_ATTRIBUTE = "__CLAIMS_"+ UUID.randomUUID();
  final static String FORCE_TOKEN_UPDATE_ATTRIBUTE = "__JWT_UPDATE_"+ UUID.randomUUID();

  @Value("${static.serve.enabled:false}")
  private String staticContentEnabled;
  @Value("${static.serve.paths:/staticContent}")
  private String[] staticContentPaths;
  @Value("${jwt.usage-check.grace-period.milliseconds:3000}")
  private String gracePeriodString;
  private long gracePeriod;
  @Value("${jwt.usage-check.enabled:true}")
  private boolean usageCheckEnabled;
  @Value("${jwt.usage-check.ignorelongcall.milliseconds:0}")
  private long usageCheckIgnoreLongCallMilliseconds;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Autowired
  private JwtTokenUsageStorage jwtTokenUsageService;

  @Autowired(required = false)
  protected SecurityConfigWhitelist securityConfigWhitelist;

  @Autowired
  private UtenteProfileService utenteProfileService;

  private AntPathRequestMatcher[] antPathRequestMatchers;

  @Override
  protected void initFilterBean() throws ServletException {
    super.initFilterBean();
    String[] authWhitelist = securityConfigWhitelist != null ? ArrayUtils.addAll(securityConfigWhitelist.getAuthWithelist(), securityConfigWhitelist.getSecurityWhitelist()) : new String[0];
    if ("true".equalsIgnoreCase(staticContentEnabled)) {
      String[] pathsToWhitelist = new String[0];
      for(String aPath: staticContentPaths)
        pathsToWhitelist = ArrayUtils.addAll(pathsToWhitelist, aPath, aPath+"/**");
      authWhitelist = ArrayUtils.addAll(authWhitelist, pathsToWhitelist);
    }
    antPathRequestMatchers = Arrays.stream(authWhitelist).map(AntPathRequestMatcher::new).toArray(AntPathRequestMatcher[]::new);

    gracePeriod = Long.parseLong(gracePeriodString);
    log.debug("setting JWT usage check grace time: " + gracePeriod + " ms");
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    return Arrays.stream(antPathRequestMatchers).anyMatch(antPathRequestMatcher -> antPathRequestMatcher.matches(request));
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
      throws ServletException, IOException {

    String jwtToken = null;
    Claims claims;
    String jti = null;
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    boolean tokenCheckOk = false;
    boolean isA2ACall = isApplication2ApplicationCall(request);
    boolean isEmailValidationCall = isEmailValidationCall(request);

    if(isA2ACall || !jwtTokenUtil.isTokenInCookie()) {
      // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
      final String requestTokenHeader = request.getHeader(AUTHORIZATION_HEADER);
      if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer "))
        jwtToken = requestTokenHeader.substring(7);
    } else {
      // JWT Token is in an http-only token
      jwtToken = jwtTokenUtil.extractTokenFromCookies(request.getCookies());
    }

    if (jwtToken != null) {
      try {
        Jws<Claims> jws;
        String a2aSystem;
        //parse JWT token
        if(isA2ACall){
          Pair<String, Jws<Claims>> jwsAndSystem = jwtTokenUtil.parseA2AAuthorizationToken(jwtToken);
          a2aSystem = jwsAndSystem.getLeft();
          jws = jwsAndSystem.getRight();
          jws.getBody().setSubject(a2aSystem);
        } else {
          jws = jwtTokenUtil.parseToken(jwtToken);
        }
        claims = jws.getBody();
        jti = claims.getId();
        //subject cannot be empty
        String subject = claims.getSubject();
        if( StringUtils.isBlank(subject) ){
          throw new InvalidJwtException(jws.getHeader(), claims, "Invalid subject");
        }
        //add logged user info into log
        MDC.put("user",subject);
        //check if token was not already used before
// TODO remove this commented code after test
//        Long rolledAt = jwtTokenUsageService.wasTokenRolled(jti);
//        log.debug("wasTokenAlreadyRolled: " + jti + " : " + rolledAt);
//        if (rolledAt == null)
        if (usageCheckEnabled && wasTokenAlreadyUsed(jti)) {
          throw new AlreadyUsedJwtException(jws.getHeader(), claims, "Token was already used");
        } else if (!isA2ACall && !jwtTokenUtil.isAuthToken(jws)){
          throw new InvalidJwtException(jws.getHeader(), claims, "Invalid token type");
        } else if(!isA2ACall && !isEmailValidationCall && isEmailValidationNeeded(claims)){
          throw new InvalidJwtException(jws.getHeader(), claims, "Invalid token type (email validation needed)");
        }
        //retrieve user info from token
        UserWithAdditionalInfo user;
        if(isA2ACall)
          user = UserWithAdditionalInfo.builder()
            .username("A2A-"+subject)
            .build();
        else {
          Map<String, Set<String>> userTenantsAndRoles = utenteProfileService.getUserTenantsAndRoles(subject);
          user = UserWithAdditionalInfo.builder()
              .username(subject)
              .codiceFiscale(claims.get(JwtTokenUtil.JWT_CLAIM_CODICE_FISCALE, String.class))
              .familyName(claims.get(JwtTokenUtil.JWT_CLAIM_COGNOME, String.class))
              .firstName(claims.get(JwtTokenUtil.JWT_CLAIM_NOME, String.class))
              .email(claims.get(JwtTokenUtil.JWT_CLAIM_EMAIL, String.class))
              .emailNew(claims.get(JwtTokenUtil.JWT_CLAIM_EMAIL_NEW, String.class))
              .emailSourceType(Optional.ofNullable(claims.get(JwtTokenUtil.JWT_CLAIM_EMAIL_SOURCE_TYPE, String.class))
                  .map(x -> x.charAt(0)).filter(Utente.EMAIL_SOURCE_TYPES::isValid)
                  .orElseThrow(() -> new MyPayException("invalid emailSourceType")))
              //retrieve enti/roles from profile service (may be cached for performance reasons)
              .entiRoles(userTenantsAndRoles)
              .sysAdmin(utenteProfileService.isSystemAdministrator(userTenantsAndRoles))
              .authenticatingAuthority(claims.get(JwtTokenUtil.JWT_CLAIM_AUTH_AUTHORITY, String.class))
              .authenticationMethod(claims.get(JwtTokenUtil.JWT_CLAIM_AUTH_METHOD, String.class))
              .build();
        }
        //set the current user (with details) from JWT Token into Spring Security configuration
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
        usernamePasswordAuthenticationToken
            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        request.setAttribute(CLAIMS_ATTRIBUTE, claims);
        tokenCheckOk = true;
      } catch (IllegalArgumentException | SignatureException | InvalidJwtException e) {
        log.warn("Unable to get or invalid JWT Token", e);
        request.setAttribute(JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_ATTRIB, JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_INVALID);
      } catch (ExpiredJwtException e) {
        log.warn("JWT Token has expired");
        request.setAttribute(JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_ATTRIB, JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_EXPIRED);
      } catch (AlreadyUsedJwtException e) {
        log.warn("JWT Token was already used");
        request.setAttribute(JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_ATTRIB, JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_USED);
      }
    } else {
      request.setAttribute(JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_ATTRIB, JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_MISSING);
    }
    try {
      chain.doFilter(request, response);
      //mark token as used
      stopWatch.stop();
      if(usageCheckEnabled && tokenCheckOk) {
        //do not set the token as "used" when operation took more than X seconds,
        // to decrease the risk that user ignored the operation front-end side and tried to navigate
        if(usageCheckIgnoreLongCallMilliseconds > 0 && stopWatch.getTime() > usageCheckIgnoreLongCallMilliseconds){
          log.debug("token tot set as used because response time longer than threshold [{}/{}]ms", stopWatch.getTime(), usageCheckIgnoreLongCallMilliseconds);
        } else {
          log.debug("operation completed, elapsed time ms[{}]", stopWatch.getTime());
          jwtTokenUsageService.markTokenUsed(jti);
        }
      }
    } catch(Exception e){
      Throwable notAuthExc = e;
      while(notAuthExc.getCause()!=null && notAuthExc.getCause()!=notAuthExc){
        if(notAuthExc instanceof NotAuthorizedException)
          break;
        notAuthExc = notAuthExc.getCause();
      }
      if(notAuthExc instanceof NotAuthorizedException)
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, notAuthExc.getMessage());
      else
        throw e;
    } //finally {
      //clear logged user info from log
      //remark: MDC is cleared just at start of next request in order to keep
      // user information in case of exception logging by Spring DispatcherServlet
      //MDC.clear();
    //}
  }

  private boolean wasTokenAlreadyUsed(String jti) {
    Long lastUsed = jwtTokenUsageService.getTokenUsageTime(jti);
    if (lastUsed == null) {
      log.debug("wasTokenAlreadyUsed: " + jti + " : null");
      return false;
    } else {
      long now = System.currentTimeMillis();
      boolean used = now - lastUsed > gracePeriod;
      log.debug("wasTokenAlreadyUsed (lastUsed): " + jti + " :" + used);
      if(used) {
        Long rolledAt = jwtTokenUsageService.wasTokenRolled(jti);
        log.debug("wasTokenAlreadyRolled: " + jti + " : " + rolledAt);
        used = rolledAt==null || now - rolledAt > gracePeriod;
        log.debug("wasTokenAlreadyUsed (alreadyRolled): " + jti + " :" + used);
      }
      return used;
    }
  }

  private String a2aPath = null;
  private boolean isApplication2ApplicationCall(HttpServletRequest request) {
    if(this.a2aPath == null){
      this.a2aPath = "...";
      try {
        String a2aPath = request.getContextPath();
        if (a2aPath.endsWith("/"))
          a2aPath = a2aPath.substring(0, a2aPath.length() - 1);
        a2aPath = a2aPath + PATH_A2A + "/";
        log.info("A2A path: " + a2aPath);
        this.a2aPath = a2aPath;
      } catch(Exception e){
        log.error("error initializing A2A path", e);
        this.a2aPath = null;
      }
    }
    boolean isA2a = request.getRequestURI().startsWith(this.a2aPath);
    log.trace("isA2a [{}]: {}",request.getRequestURI(),isA2a);
    return isA2a;
  }

  private String emailValidationPath = null;
  private boolean isEmailValidationCall(HttpServletRequest request) {
    if(this.emailValidationPath == null){
      this.emailValidationPath = "...";
      try {
        String emailValidationPath = request.getContextPath();
        if (emailValidationPath.endsWith("/"))
          emailValidationPath = emailValidationPath.substring(0, emailValidationPath.length() - 1);
        emailValidationPath = emailValidationPath + PATH_EMAIL_VALIDATION;
        log.info("EmailValidation path: " + emailValidationPath);
        this.emailValidationPath = emailValidationPath;
      }catch(Exception e){
        log.error("error initializing EmailValidation path", e);
        this.emailValidationPath = null;
      }
    }
    boolean isEmailValidation = request.getRequestURI().equals(this.emailValidationPath) || request.getRequestURI().startsWith(this.emailValidationPath+"/");
    log.trace("isEmailValidation [{}]: {}",request.getRequestURI(),isEmailValidation);
    return isEmailValidation;
  }

  private boolean isEmailValidationNeeded(Claims claims){
    return JwtRequestFilter.isEmailValidationNeeded(claims.get(JwtTokenUtil.JWT_CLAIM_EMAIL, String.class));
  }

  public static boolean isEmailValidationNeeded(String email){
    return StringUtils.isBlank(email);
  }

  public static void updateMailOnToken(HttpServletRequest request, String validatedEmail, char emailSourceType){
    Claims claims = (Claims) request.getAttribute(JwtRequestFilter.CLAIMS_ATTRIBUTE);
    if(claims!=null){
      claims.put(JwtTokenUtil.JWT_CLAIM_EMAIL, validatedEmail);
      claims.put(JwtTokenUtil.JWT_CLAIM_EMAIL_SOURCE_TYPE, emailSourceType);
      request.setAttribute(FORCE_TOKEN_UPDATE_ATTRIBUTE, "true");
    }
  }

}
