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
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenUtil {

  private static final String TOKEN_TYPE = "type";
  private static final String TOKEN_TYPE_AUTH = "auth";
  private static final String TOKEN_TYPE_LOGIN = "login";
  private static final String TOKEN_TYPE_SECURITY = "sec";
  private static final String TOKEN_TYPE_POLLING = "poll";
  private static final String TOKEN_TYPE_WSAUTH = "ws";
  private static final String TOKEN_TYPE_A2A = "a2a";
  private static final String TOKEN_TYPE_FEDERATED_LOGIN = "feder";

  public static final String JWT_CLAIM_COGNOME = "cognome";
  public static final String JWT_CLAIM_NOME = "nome";
  public static final String JWT_CLAIM_CODICE_FISCALE = "cf";
  public static final String JWT_CLAIM_EMAIL = "email";
  public static final String JWT_CLAIM_EMAIL_NEW = "emailNew";
  public static final String JWT_CLAIM_USERID = "userid";
  public static final String JWT_CLAIM_ENTE = "ente";
  public static final String JWT_CLAIM_UPLOAD_TYPE = "uploadType";
  public static final String JWT_CLAIM_IMPORT_PATH = "importPath";
  public static final String JWT_CLAIM_OID = "oid";
  public static final String JWT_CLAIM_TOKEN_ID = "jti";
  public static final String JWT_CLAIM_INITIAL_TOKEN_ID = "ijti";
  public static final String JWT_CLAIM_EMAIL_SOURCE_TYPE = "emailSrc";
  public static final String JWT_CLAIM_NAZIONE_NASCITA = "nazNas";
  public static final String JWT_CLAIM_PROV_NASCITA = "prNas";
  public static final String JWT_CLAIM_COMUNE_NASCITA = "comNas";
  public static final String JWT_CLAIM_DATA_NASCITA = "dtNas";
  public static final String JWT_CLAIM_AUTH_AUTHORITY = "authAuth";
  public static final String JWT_CLAIM_AUTH_METHOD = "authMeth";
  public static final String JWT_CLAIM_NAZIONE_RESIDENZA = "nazRes";
  public static final String JWT_CLAIM_PROV_RESIDENZA = "prRes";
  public static final String JWT_CLAIM_COMUNE_RESIDENZA = "comRes";
  public static final String JWT_CLAIM_CAP_RESIDENZA = "capRes";
  public static final String JWT_CLAIM_INDIRIZZO_RESIDENZA = "indRes";
  public static final String JWT_CLAIM_CIVICO_RESIDENZA = "civRes";
  public static final String JWT_CLAIM_LEGAL_ENTITY = "legEnt";
  public static final String JWT_CLAIM_FED_AUTH = "fedAuth";
  public static final String JWT_CLAIM_APP = "app";



  @Value("${cors.enabled:false}")
  private boolean corsEnabled;

  @Value("${jwt.use-header-auth.enabled:false}")
  private boolean useHeaderAuth;

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;
  private String appBePath;

  @Value("${jwt.secret}")
  private String secret;
  @Value("${jwt.validity.seconds:36000}") //default: 10 hours
  private int jwtTokenValidity;

  private final Map<String, PublicKey> clientApplicationsPublicKey;

  private final Map<String, PublicKey> federatedLoginPublicKey;

  public JwtTokenUtil(@Autowired ConfigurableEnvironment env){
    this.clientApplicationsPublicKey = Utilities.envToMap(env, "a2a.public.", JwtTokenUtil::getPublicKeyFromString);
    log.debug("A2A client applications: "+clientApplicationsPublicKey.keySet());

    this.federatedLoginPublicKey = Utilities.envToMap(env, "federated-login.public.", JwtTokenUtil::getPublicKeyFromString);
    log.debug("Federated login entries: "+federatedLoginPublicKey.keySet());
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
        .sameSite(this.corsEnabled?"None":null)
        .secure(true)
        .maxAge(maxAge)
        .build().toString();
  }

  public AbstractMap.SimpleImmutableEntry<String, String> generateCookieRemovalHeader(){
    String authHeaderName,authHeaderValue;
    if(this.useHeaderAuth) {
      return null;
    } else {
      authHeaderName = HttpHeaders.SET_COOKIE;
      authHeaderValue = removeTokenCookie();
      return new AbstractMap.SimpleImmutableEntry<>(authHeaderName, authHeaderValue);
    }
  }

  public AbstractMap.SimpleImmutableEntry<String, String> generateAuthorizationHeader(String jwtToken){
    String authHeaderName,authHeaderValue;
    if(this.useHeaderAuth) {
      authHeaderName = JwtRequestFilter.AUTHORIZATION_HEADER;
      authHeaderValue = "Bearer "+jwtToken;
    } else {
      authHeaderName = HttpHeaders.SET_COOKIE;
      authHeaderValue = generateTokenCookie(jwtToken);
    }
    return new AbstractMap.SimpleImmutableEntry<>(authHeaderName, authHeaderValue);
  }

  public boolean isTokenInCookie(){
    return !this.useHeaderAuth;
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
  public boolean isPollingToken(Jws<Claims> token){
    return TOKEN_TYPE_POLLING.equals(token.getBody().getOrDefault(TOKEN_TYPE, null));
  }
  public boolean isWsAuthToken(Jws<Claims> token){
    return TOKEN_TYPE_WSAUTH.equals(token.getBody().getOrDefault(TOKEN_TYPE, null));
  }
  public boolean isA2AAuthToken(Jws<Claims> token){
    return TOKEN_TYPE_A2A.equals(token.getBody().getOrDefault(TOKEN_TYPE, null));
  }

  public boolean isFederatedLoginAuthToken(Jws<Claims> token){
    return TOKEN_TYPE_FEDERATED_LOGIN.equals(token.getBody().getOrDefault(TOKEN_TYPE, null));
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
    return generateSecurityToken(user, objectId, 2l * 3600); //2 hours
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

  public String generatePollingToken(UserWithAdditionalInfo user, String objectId) {
    return generatePollingToken(user, objectId, 1L * 60); //1 minute
  }

  public String generatePollingToken(UserWithAdditionalInfo user, String objectId, long expirationInSeconds) {
    String username = user!=null ? user.getUsername() : "<ANONIMO>";
    Map<String, Object> claims = new HashMap<>();
    claims.put(Claims.SUBJECT, username);
    claims.put(JWT_CLAIM_OID,objectId);
    claims.put(TOKEN_TYPE,TOKEN_TYPE_POLLING);
    return doGenerateToken(claims, username, new Date(System.currentTimeMillis() + expirationInSeconds * 1000));
  }

  public String parsePollingToken(UserWithAdditionalInfo user, String token) {
    String username = user!=null ? user.getUsername() : "<ANONIMO>";
    Jws<Claims> jws = parseJwsTokenImpl(token);
    if (!isPollingToken(jws)){
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

  private static PublicKey getPublicKeyFromString(String encodedKey) {
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

  public Pair<String, Jws<Claims>> parseFederatedLoginToken(String token) {
    ImmutablePair<String, Jws<Claims>> pairAppJws = this.federatedLoginPublicKey.keySet().stream()
      .map(Possibly.of((String app) -> new ImmutablePair<>(app,
        parseJwsTokenImpl(token, this.federatedLoginPublicKey.get(app)))))
      .filter(Possibly::is)
      .map(Possibly::orNull)
      .findFirst()
      .orElseThrow(() -> new InvalidJwtException(null, null, "Invalid token for federated-login call"));

    if (!isFederatedLoginAuthToken(pairAppJws.getRight())){
      throw new InvalidJwtException(pairAppJws.getRight().getHeader(), pairAppJws.getRight().getBody(), "Invalid token type");
    }

    return pairAppJws;
  }

  public static void main(String[] args) {
    //this is used to generate a random key secret
    System.out.println("HS512:\n"+Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded()));
    KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
    String publicKeyEncoded = Encoders.BASE64.encode(keyPair.getPublic().getEncoded());
    String privateKeyEncoded = Encoders.BASE64.encode(keyPair.getPrivate().getEncoded());
    System.out.println("RS512, public:\n"+publicKeyEncoded);
    System.out.println("RS512, private:\n"+privateKeyEncoded);

    JwtTokenUtil.checkKeyPairMatch(publicKeyEncoded, privateKeyEncoded);
  }

  private static void checkKeyPairMatch(String publicKeyEncoded, String privateKeyEncoded) {
    try {
      PublicKey publicKey = getPublicKeyFromString(publicKeyEncoded);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Decoders.BASE64.decode(privateKeyEncoded));
      KeyFactory kf = KeyFactory.getInstance("RSA");
      PrivateKey privateKey = kf.generatePrivate(keySpec);

      // create a challenge
      byte[] challenge = new byte[10000];
      ThreadLocalRandom.current().nextBytes(challenge);
      // sign using the private key
      Signature sig = Signature.getInstance("SHA256withRSA");
      sig.initSign(privateKey);
      sig.update(challenge);
      byte[] signature = sig.sign();
      // verify signature using the public key
      sig.initVerify(publicKey);
      sig.update(challenge);
      boolean keyPairMatches = sig.verify(signature);
      System.out.println("KyPair match: "+keyPairMatches);

    }catch(Exception e){
      e.printStackTrace();
    }
  }
}
