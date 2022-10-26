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
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.ComuneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.NazioneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.ProvinciaTo;
import it.regioneveneto.mygov.payment.mypay4.dto.UtenteTo;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.service.LocationService;
import it.regioneveneto.mygov.payment.mypay4.service.UtenteProfileService;
import it.regioneveneto.mygov.payment.mypay4.service.UtenteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "Autenticazione", description = "Gestione dell'autenticazione")
@RestController
@RequestMapping("/")
@CrossOrigin
@Slf4j
@ConditionalOnWebApplication
public class JwtAuthenticationController {
  static final String CHECK_LOGIN_COOKIE_PATH = "/checkLoginCookie";


  @Value("${auth.fake.enabled:false}")
  private String fakeAuthEnabled;

  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private JwtTokenUtil jwtTokenUtil;
  @Autowired
  private UtenteService utenteService;
  @Autowired
  private UtenteProfileService utenteProfileService;

  @Operation(summary ="Finalizza autenticazione",
      description = "Finalizza la procedura di autenticazione trasformando un login-token in un jwt-token sotto forma di http-only cookie",
      responses = {@ApiResponse(description = "nel body l'oggetto contenente i dati dell'utente loggato; viene settato un cookie http-only con il token necessario per le chiamate protette",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UtenteTo.class) )},
            headers = {@Header(name="Set-Cookie",schema = @Schema(type = "string", example = "jwtToken=eyJhbGciOiJIUzU...; Path=/; Secure; HttpOnly"))}),
          @ApiResponse(responseCode = "401", description = "ritorna la descrizione dell'errore nel caso non sia stato possibile completare la procedura di autenticazione con successo",
            content = { @Content(mediaType = "text/plain", schema = @Schema(type = "string"))})
      })
  @SecurityRequirements
  @PostMapping(MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/authtoken")
  public ResponseEntity<?> authWithToken(@RequestBody String loginToken, HttpServletResponse response) {
    String error;
    try {
      //parse JWT token
      Jws<Claims> jws = jwtTokenUtil.parseToken(loginToken);
      if(jwtTokenUtil.isLoginToken(jws)){
        Claims claims = jws.getBody();
        //extract logged user details from authentication system
        Utente utente = utenteService.mapLoginClaimsToUtente(claims);
        //set last login time
        utente.setDtUltimoLogin(new Date());
        // update user details of logged user into DB (or insert into DB when not existing)
        utente = utenteService.upsertUtente(utente, true);
        //generate auth token
        claims.put(JwtTokenUtil.JWT_CLAIM_EMAIL_SOURCE_TYPE,utente.getEmailSourceType());
        claims.put(JwtTokenUtil.JWT_CLAIM_EMAIL,utente.getDeEmailAddress());

        String token = jwtTokenUtil.generateToken(claims.getSubject(), claims);
        AbstractMap.SimpleImmutableEntry<String, String> authHeader = jwtTokenUtil.generateAuthorizationHeader(token);
        UtenteTo utenteTo = UtenteService.mapUtenteToDto(utente).toBuilder()
            .statoNascita(claims.get(JwtTokenUtil.JWT_CLAIM_NAZIONE_NASCITA, String.class))
            .provinciaNascita(claims.get(JwtTokenUtil.JWT_CLAIM_PROV_NASCITA, String.class))
            .comuneNascita(claims.get(JwtTokenUtil.JWT_CLAIM_COMUNE_NASCITA, String.class))
            .dataNascita(claims.get(JwtTokenUtil.JWT_CLAIM_DATA_NASCITA, String.class))
            .loginType(StringUtils.joinWith(" - ",
                claims.get(JwtTokenUtil.JWT_CLAIM_AUTH_AUTHORITY, String.class),claims.get(JwtTokenUtil.JWT_CLAIM_AUTH_METHOD, String.class)))
            .emailValidationNeeded(StringUtils.isBlank(utente.getDeEmailAddress()))
            .build();
        //add roles into utente
        utenteTo.setEntiRoles(utenteProfileService.getAndUpdateUserTenantsAndRoles(utente.getCodFedUserId()));
        return ResponseEntity.ok()
            .header(authHeader.getKey(), authHeader.getValue())
            .body(utenteTo);
      } else {
        error = "JWT Token is not a login token";
      }
    } catch (IllegalArgumentException | SignatureException e) {
      error = "Unable to get JWT Token";
      log.warn(error, e);
    } catch (ExpiredJwtException e) {
      error = "JWT Token has expired";
      log.warn(error, e);
    } catch (AlreadyUsedJwtException e) {
      error = "JWT Token was already used";
      log.warn(error, e);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @Operation(summary ="Effettua login utilizzando il cookie http-only", description = "Verifica che esista il cookie http-only di autenticazione e, in caso, effettua il login con tale cookie")
  @PostMapping(CHECK_LOGIN_COOKIE_PATH)
  public ResponseEntity<?> checkLoginCookie(@AuthenticationPrincipal UserWithAdditionalInfo user){
    //if I'm here this means that http-only access cookie is valid. Just return user data
    Utente utente = utenteService.getByCodFedUserId(user.getUsername()).orElseThrow(NotFoundException::new);
    //Utente utente = utenteService.mapUserWithAdditionalInfoToUtente(user);
    UtenteTo utenteTo = UtenteService.mapUtenteToDto(utente).toBuilder()
        .statoNascita(user.getStatoNascita())
        .provinciaNascita(user.getProvinciaNascita())
        .comuneNascita(user.getComuneNascita())
        .dataNascita(user.getDataNascita())
        .loginType(StringUtils.joinWith(" - ",
            user.getAuthenticatingAuthority(),user.getAuthenticationMethod()))
        .emailValidationNeeded(StringUtils.isBlank(utente.getDeEmailAddress()))
        .build();
    //add roles into utente
    utenteTo.setEntiRoles(utenteProfileService.getAndUpdateUserTenantsAndRoles(utente.getCodFedUserId()));
    return ResponseEntity.ok().body(utenteTo);
  }

  @Autowired
  private LocationService locationService;

  private final Random random = new Random(System.currentTimeMillis());
  private List<NazioneTo> nazioni;
  private List<ProvinciaTo> province;
  private final Map<ProvinciaTo, List<ComuneTo>> comuni = new HashMap<>();

  @Hidden
  @PostMapping(MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/authpassword")
  public ResponseEntity<?> authWithUsernamePassword(@RequestBody JwtRequest authenticationRequest, HttpServletResponse response) {
    if(!"true".equalsIgnoreCase(fakeAuthEnabled))
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

    try{
      String username = authenticationRequest.getUsername();
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, authenticationRequest.getPassword()));
      //fake authentication
      Utente utente = utenteService.getByCodFedUserId(username).orElseThrow(()->new BadCredentialsException("not existing user"));

      //TODO: remove after test
      utente.setCap("00100");
      utente.setIndirizzo("via roma");
      utente.setCivico(null);
      utente.setNazioneId(1L);
      utente.setProvinciaId(null);
      utente.setComuneId(null);
      //set last login time
      utente.setDtUltimoLogin(new Date());
      // update user details of logged user into DB (or insert into DB when not existing)
      utente = utenteService.upsertUtente(utente, true);

      if(nazioni==null)
        nazioni = locationService.getNazioni();
      String statoNascita = random.nextDouble() < 0.75 ? "ITALIA" : nazioni.get(random.nextInt(nazioni.size())).getNomeNazione();
      if(province==null)
        province = locationService.getProvince();
      ProvinciaTo provinciaNascita = null;
      String comune = null;
      if(statoNascita.equals("ITALIA")) {
        while(comune==null) {
          provinciaNascita = province.get(random.nextInt(province.size()));
          if (provinciaNascita != null) {
            List<ComuneTo> comuniProvincia = comuni.computeIfAbsent(provinciaNascita, p -> locationService.getComuniByProvincia(p.getProvinciaId()));
            if (!comuniProvincia.isEmpty())
              comune = comuniProvincia.get(random.nextInt(comuniProvincia.size())).getComune();
          }
        }
      }

      UtenteTo utenteTo = UtenteService.mapUtenteToDto(utente).toBuilder()
          .loginType("Sviluppo - password")
          .statoNascita(statoNascita)
          .provinciaNascita(provinciaNascita!=null?provinciaNascita.getSigla():null)
          .comuneNascita(comune)
          .dataNascita(LocalDate.now().minusDays(360*30+new Random().nextInt(360*30)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
          .emailValidationNeeded(StringUtils.isBlank(utente.getDeEmailAddress()))
          .build();
      final UserWithAdditionalInfo userDetails = UserWithAdditionalInfo.builder()
          .username(utenteTo.getUsername())
          .userId(""+utenteTo.getUserId())
          .firstName(utenteTo.getNome())
          .familyName(utenteTo.getCognome())
          .codiceFiscale(utenteTo.getCodiceFiscale())
          .email(utenteTo.getEmail())
          .emailNew(utenteTo.getEmailNew())
          .emailSourceType(utente.getEmailSourceType())
          .authenticatingAuthority("Sviluppo")
          .authenticationMethod("password")
          .build();
      final String jwtToken = jwtTokenUtil.generateToken(username, userDetails);
      AbstractMap.SimpleImmutableEntry<String, String> authHeader = jwtTokenUtil.generateAuthorizationHeader(jwtToken);
      //add roles into utente
      utenteTo.setEntiRoles(utenteProfileService.getAndUpdateUserTenantsAndRoles(utente.getCodFedUserId()));
      return ResponseEntity.ok()
          .header(authHeader.getKey(), authHeader.getValue())
          .body(utenteTo);
    } catch (DisabledException | BadCredentialsException e){
      log.error("bad credentials or disabledUser", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AbstractMap.SimpleEntry<>("errorCode","BAD_CREDENTIALS"));
    }
  }

  @Operation(summary ="Finalizza logout", description = "Finalizza la procedura di logout cancellando il cookie")
  @GetMapping(MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/doLogout")
  public ResponseEntity<Void> logout(HttpServletRequest request){
    request.setAttribute(JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_ATTRIB, JwtAuthenticationEntryPoint.TOKEN_LOGGED_OUT);
    return ResponseEntity.ok().build();
  }
}
