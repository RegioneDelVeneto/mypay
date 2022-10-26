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
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import it.regioneveneto.mygov.payment.mypay4.dto.WsImportTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.util.Possibly;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class JwtTokenUtil {

  private final static String TOKEN_TYPE = "type";
  private final static String TOKEN_TYPE_AUTH = "auth";
  private final static String TOKEN_TYPE_LOGIN = "login";
  private final static String TOKEN_TYPE_SECURITY = "sec";
  private final static String TOKEN_TYPE_WSAUTH = "ws";
  private final static String TOKEN_TYPE_A2A = "a2a";

  public final static String JWT_CLAIM_COGNOME = "cognome";
  public final static String JWT_CLAIM_NOME = "nome";
  public final static String JWT_CLAIM_CODICE_FISCALE = "cf";
  public final static String JWT_CLAIM_EMAIL = "email";
  public final static String JWT_CLAIM_EMAIL_NEW = "emailNew";
  public final static String JWT_CLAIM_USERID = "userid";
  public final static String JWT_CLAIM_ENTE = "ente";
  public final static String JWT_CLAIM_UPLOAD_TYPE = "uploadType";
  public final static String JWT_CLAIM_IMPORT_PATH = "importPath";
  public final static String JWT_CLAIM_OID = "oid";
  public final static String JWT_CLAIM_TOKEN_ID = "jti";
  public final static String JWT_CLAIM_INITIAL_TOKEN_ID = "ijti";
  public final static String JWT_CLAIM_EMAIL_SOURCE_TYPE = "emailSrc";
  public final static String JWT_CLAIM_NAZIONE_NASCITA = "nazNas";
  public final static String JWT_CLAIM_PROV_NASCITA = "prNas";
  public final static String JWT_CLAIM_COMUNE_NASCITA = "comNas";
  public final static String JWT_CLAIM_DATA_NASCITA = "dtNas";
  public final static String JWT_CLAIM_AUTH_AUTHORITY = "authAuth";
  public final static String JWT_CLAIM_AUTH_METHOD = "authMeth";
  public final static String JWT_CLAIM_NAZIONE_RESIDENZA = "nazRes";
  public final static String JWT_CLAIM_PROV_RESIDENZA = "prRes";
  public final static String JWT_CLAIM_COMUNE_RESIDENZA = "comRes";
  public final static String JWT_CLAIM_CAP_RESIDENZA = "capRes";
  public final static String JWT_CLAIM_INDIRIZZO_RESIDENZA = "indRes";
  public final static String JWT_CLAIM_CIVICO_RESIDENZA = "civRes";
  public final static String JWT_CLAIM_LEGAL_ENTITY = "legEnt";
  public final static String JWT_CLAIM_FED_AUTH = "fedAuth";



  @Value("${cors.enabled:false}")
  private String corsEnabled;

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;
  private String appBePath;

  @Value("${jwt.secret}")
  private String secret;
  @Value("${jwt.validity.seconds:36000}") //default: 10 hours
  private int jwtTokenValidity;

  private final Map<String, PublicKey> clientApplicationsPublicKey;

  public JwtTokenUtil(@Autowired ConfigurableEnvironment env){
    this.clientApplicationsPublicKey = StreamSupport.stream(env.getPropertySources().spliterator(), false)
        .filter(ps -> ps instanceof EnumerablePropertySource)
        .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
        .flatMap(Arrays::stream)
        .filter(name -> name.startsWith("a2a.public."))
        .distinct()
        .map(name -> Pair.of(name.substring("a2a.public.".length()), this.getPublicKeyFromString(env.getProperty(name))))
        //.map(pair -> {log.debug(pair.getKey()+":"+pair.getValue()); return pair;})
        .collect(Collectors.toUnmodifiableMap(Pair::getKey, Pair::getValue));
    log.debug("A2A client applications: "+clientApplicationsPublicKey.keySet());
  }


  private String getAppBePath(){
    if(appBePath == null){
      try{
        appBePath = StringUtils.firstNonBlank(new URL(this.appBeAbsolutePath).getPath(),"/");
        log.info("absolute BE app url: {} - BE app path: {}", appBeAbsolutePath, appBePath);
      } catch(MalformedURLException mue){
        throw new MyPayException(mue);
      }
    }
    return appBePath;
  }

  //retrieve expiration date from jwt token
  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = parseToken(token).getBody();
    return claimsResolver.apply(claims);
  }

  //for retrieveing any information from token we will need the secret key
  public Jws<Claims> parseToken(String token) {
    return parseJwsTokenImpl(token);
  }

  private String generateTokenCookie(String jwtToken) {
    return tokenToCookie(jwtToken, -1);
  }

  private String removeTokenCookie() {
    return tokenToCookie(null, 0);
  }

  private String tokenToCookie(String jwtToken, int maxAge){
    return ResponseCookie
        .from("jwtToken", jwtToken)
        .path(getAppBePath())
        .httpOnly(true)
        //.sameSite("None")
        .secure(true)
        .maxAge(maxAge)
        .build().toString();
  }

  public AbstractMap.SimpleImmutableEntry<String, String> generateCookieRemovalHeader(){
    String authHeaderName,authHeaderValue;
    if("true".equalsIgnoreCase(corsEnabled)) {
      return null;
    } else {
      authHeaderName = HttpHeaders.SET_COOKIE;
      authHeaderValue = removeTokenCookie();
      return new AbstractMap.SimpleImmutableEntry<>(authHeaderName, authHeaderValue);
    }
  }

  public AbstractMap.SimpleImmutableEntry<String, String> generateAuthorizationHeader(String jwtToken){
    String authHeaderName,authHeaderValue;
    if("true".equalsIgnoreCase(corsEnabled)) {
      authHeaderName = JwtRequestFilter.AUTHORIZATION_HEADER;
      authHeaderValue = "Bearer "+jwtToken;
    } else {
      authHeaderName = HttpHeaders.SET_COOKIE;
      authHeaderValue = generateTokenCookie(jwtToken);
    }
    return new AbstractMap.SimpleImmutableEntry<>(authHeaderName, authHeaderValue);
  }

  public boolean isTokenInCookie(){
    return !"true".equalsIgnoreCase(corsEnabled);
  }

  public String extractTokenFromCookies(Cookie[] cookies){
    if(cookies!=null)
      for(Cookie cookie:cookies)
        if("jwtToken".equals(cookie.getName()))
          return cookie.getValue();
    return null;
  }

  public boolean isAuthToken(Jws<Claims> token){
    return TOKEN_TYPE_AUTH.equals(token.getBody().getOrDefault(TOKEN_TYPE, null));
  }
  public boolean isLoginToken(Jws<Claims> token){
    return TOKEN_TYPE_LOGIN.equals(token.getBody().getOrDefault(TOKEN_TYPE, null));
  }
  public boolean isSecurityToken(Jws<Claims> token){
    return TOKEN_TYPE_SECURITY.equals(token.getBody().getOrDefault(TOKEN_TYPE, null));
  }
  public boolean isWsAuthToken(Jws<Claims> token){
    return TOKEN_TYPE_WSAUTH.equals(token.getBody().getOrDefault(TOKEN_TYPE, null));
  }
  public boolean isA2AAuthToken(Jws<Claims> token){
    return TOKEN_TYPE_A2A.equals(token.getBody().getOrDefault(TOKEN_TYPE, null));
  }

  public String generateToken(String username, UserWithAdditionalInfo user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(JWT_CLAIM_COGNOME,user.getFamilyName());
    claims.put(JWT_CLAIM_NOME,user.getFirstName());
    claims.put(JWT_CLAIM_CODICE_FISCALE,user.getCodiceFiscale());
    claims.put(JWT_CLAIM_EMAIL,user.getEmail());
    claims.put(JWT_CLAIM_EMAIL_NEW,user.getEmailNew());
    claims.put(JWT_CLAIM_EMAIL_SOURCE_TYPE,String.valueOf(user.getEmailSourceType()));
    claims.put(JWT_CLAIM_AUTH_AUTHORITY,user.getAuthenticatingAuthority());
    claims.put(JWT_CLAIM_AUTH_METHOD,user.getAuthenticationMethod());
    claims.put(Claims.SUBJECT, username);
    claims.put(JWT_CLAIM_USERID, user.getUserId());
    claims.put(TOKEN_TYPE,TOKEN_TYPE_AUTH);
    return doGenerateToken(claims, username, new Date(System.currentTimeMillis() + jwtTokenValidity * 1000L));
  }

  //generate token for user
  public String generateToken(String username, Map<String, Object> claims) {
    claims.put(TOKEN_TYPE,TOKEN_TYPE_AUTH);
    return doGenerateToken(claims, username, new Date(System.currentTimeMillis() + jwtTokenValidity * 1000L));
  }

  //generate login token for user
  public String generateLoginToken(String username, Map<String, Object> claims) {
    claims.put(TOKEN_TYPE,TOKEN_TYPE_LOGIN);
    return doGenerateToken(claims, username, new Date(System.currentTimeMillis() + 15 * 1000)); //15 seconds
  }

  //generate authorization token for file import
  public String generateWsAuthorizationToken(String codIpaEnte, String importPath, String requestToken, String uploadType) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(Claims.SUBJECT, requestToken);
    claims.put(JWT_CLAIM_ENTE,codIpaEnte);
    claims.put(JWT_CLAIM_UPLOAD_TYPE,uploadType);
    claims.put(JWT_CLAIM_IMPORT_PATH,importPath);
    claims.put(TOKEN_TYPE,TOKEN_TYPE_WSAUTH);
    return doGenerateToken(claims, requestToken, new Date(System.currentTimeMillis() + 1 * 3600 * 1000)); //1 hour
  }

  public WsImportTo parseWsAuthorizationToken(String token) {
    Jws<Claims> jws = parseJwsTokenImpl(token);
    if (!isWsAuthToken(jws)){
      throw new InvalidJwtException(jws.getHeader(), jws.getBody(), "Invalid token type");
    }
    WsImportTo wsImportTo = new WsImportTo();
    wsImportTo.setRequestToken(jws.getBody().getSubject());
    wsImportTo.setCodIpaEnte(jws.getBody().get(JWT_CLAIM_ENTE,String.class));
    wsImportTo.setUploadType(jws.getBody().get(JWT_CLAIM_UPLOAD_TYPE,String.class));
    wsImportTo.setImportPath(jws.getBody().get(JWT_CLAIM_IMPORT_PATH,String.class));
    return wsImportTo;
  }

  public String generateSecurityToken(UserWithAdditionalInfo user, String objectId) {
    return generateSecurityToken(user, objectId, 2 * 3600); //2 hours
  }

  public String generateSecurityToken(UserWithAdditionalInfo user, String objectId, long expirationInSeconds) {
    String username = user!=null ? user.getUsername() : "<ANONIMO>";
    Map<String, Object> claims = new HashMap<>();
    claims.put(Claims.SUBJECT, username);
    claims.put(JWT_CLAIM_OID,objectId);
    claims.put(TOKEN_TYPE,TOKEN_TYPE_SECURITY);
    return doGenerateToken(claims, username, new Date(System.currentTimeMillis() + expirationInSeconds * 1000));
  }

  public String parseSecurityToken(UserWithAdditionalInfo user, String token) {
    String username = user!=null ? user.getUsername() : "<ANONIMO>";
    Jws<Claims> jws = parseJwsTokenImpl(token);
    if (!isSecurityToken(jws)){
      throw new InvalidJwtException(jws.getHeader(), jws.getBody(), "Invalid token type");
    }
    if(!username.equals(jws.getBody().getSubject())){
      throw new InvalidJwtException(jws.getHeader(), jws.getBody(), "Username mismatch");
    }
    return (String)jws.getBody().getOrDefault(JWT_CLAIM_OID,null);
  }

  private Jws<Claims> parseJwsTokenImpl(String token){
    Jws<Claims> jws;
    try{
      jws = Jwts.parserBuilder().setSigningKey(this.secret).build().parseClaimsJws(token);
    } catch(Exception e){
      log.error("error parsing jwt token [{}]: {}", token, e.toString());
      throw e;
    }
    return jws;
  }

  private Jws<Claims> parseJwsTokenImpl(String token, Key secretKey){
    Jws<Claims> jws;
    try{
      jws = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
    } catch(Exception e){
      log.error("error parsing jwt token [{}]: {}", token, e.toString());
      throw e;
    }
    return jws;
  }

  //while creating the token -
//1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
//2. Sign the JWT using the HS512 algorithm and secret key.
//3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
//   compaction of the JWT to a URL-safe string
  private String doGenerateToken(Map<String, Object> claims, String subject, Date expiration) {

    if( StringUtils.isBlank(subject) || (
        TOKEN_TYPE_AUTH.equals(claims.getOrDefault(TOKEN_TYPE,null)) &&
        !StringUtils.equals(subject, Objects.toString(claims.get(Claims.SUBJECT))) ) ) {
      log.warn("jwtTokenUtil, invalid subject [{}] [{}]",subject, claims.get(Claims.SUBJECT));
      throw new MyPayException("invalid token subject");
    }

    byte[] keyBytes = Decoders.BASE64.decode(secret);
    Key key = Keys.hmacShaKeyFor(keyBytes);
    if(!TOKEN_TYPE_SECURITY.equals(claims.getOrDefault(TOKEN_TYPE,null))) {
      //compute id (it's always a new random UUID)
      claims.put(JWT_CLAIM_TOKEN_ID, UUID.randomUUID().toString());
      //this is the original token id, it's always the same in case of refresh
      claims.computeIfAbsent(JWT_CLAIM_INITIAL_TOKEN_ID, k -> claims.get(JWT_CLAIM_TOKEN_ID));
    }
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(expiration)
        .signWith(key, SignatureAlgorithm.HS512).compact();
  }

  private PublicKey getPublicKeyFromString(String encodedKey) {
    try {
      X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(Decoders.BASE64.decode(encodedKey));
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return kf.generatePublic(X509publicKey);
    } catch (Exception e){
      throw new MyPayException("invalid public key", e);
    }
  }

  public Pair<String, Jws<Claims>> parseA2AAuthorizationToken(String token) {
    ImmutablePair<String, Jws<Claims>> pairAppJws = this.clientApplicationsPublicKey.keySet().stream()
        .map(Possibly.of((String app) -> new ImmutablePair<>(app,
            parseJwsTokenImpl(token, this.clientApplicationsPublicKey.get(app)))))
        .filter(Possibly::is)
        .map(Possibly::orNull)
        .findFirst()
        .orElseThrow(() -> new InvalidJwtException(null, null, "Invalid token for A2A call"));

    if (!isA2AAuthToken(pairAppJws.getRight())){
      throw new InvalidJwtException(pairAppJws.getRight().getHeader(), pairAppJws.getRight().getBody(), "Invalid token type");
    }

    return pairAppJws;
  }

  public static void main(String[] args) {
    //this is used to generate a random key secret
    System.out.println("HS512:\n"+Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded()));
    KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
    System.out.println("RS512, public:\n"+Encoders.BASE64.encode(keyPair.getPublic().getEncoded()));
    System.out.println("RS512, private:\n"+Encoders.BASE64.encode(keyPair.getPrivate().getEncoded()));
  }
}
