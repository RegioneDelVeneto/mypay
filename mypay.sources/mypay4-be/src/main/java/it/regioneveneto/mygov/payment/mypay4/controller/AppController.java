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
package it.regioneveneto.mygov.payment.mypay4.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.ApplicationStartupService;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.exception.NotAuthorizedException;
import it.regioneveneto.mygov.payment.mypay4.queue.QueueProducer;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.service.common.DbToolsService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@OpenAPIDefinition(
    info = @Info(
        title = "MyPay4",
        version = "0.0",
        description = "Sistema di gestione dei pagamenti"
    ),
    security = { @SecurityRequirement(name="myPay4Security") }
)
@SecurityScheme(name = "myPay4Security",
    type = SecuritySchemeType.APIKEY,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.COOKIE,
    paramName = "jwtToken")
@Tag(name = "Applicazione", description = "Gestione generale dell'applicazione")
@SecurityRequirements
@RestController
@Slf4j
@ConditionalOnWebApplication
public class AppController {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  @Value("${google.recaptcha.enabled}")
  private String recaptchaEnabled;

  @Value("${google.recaptcha.site.key}")
  private String recaptchaSiteKey;

  @Value("${auth.fake.enabled:false}")
  private String fakeAuthEnabled;

  @Value("${cors.enabled:false}")
  private String corsEnabled;

  @Value("${external-profile.enabled:false}")
  private String externalProfileEnabled;

  @Value("${pa.codIpaEntePredefinito}")
  private String adminEnte;

  @Value("${pa.adminEnte.editUser.enabled:false}")
  private boolean adminEnteEditUserEnabled;

  @Value("${pa.matomo.1.trackerUrl:}")
  private String matomo1TrackerUrl;
  @Value("${pa.matomo.1.siteId:}")
  private String matomo1SiteId;
  @Value("${pa.matomo.1.trackerUrlSuffix:matomo.php}")
  private String matomo1Suffix;

  @Value("${pa.matomo.2.trackerUrl:}")
  private String matomo2TrackerUrl;
  @Value("${pa.matomo.2.siteId:}")
  private String matomo2SiteId;
  @Value("${pa.matomo.2.trackerUrlSuffix:matomo.php}")
  private String matomo2Suffix;

  @Value("${mybox.path.root}")
  private String myBoxRootPath;

  @Value("${pa.operations.key:}")
  private String paOperationsKey;

  @Autowired
  private BuildProperties buildProperties;

  @Autowired
  private ApplicationStartupService applicationStartupService;

  @Autowired
  @Lazy
  private CacheService cacheService;

  @Autowired
  private QueueProducer queueProducer;

  @Autowired
  private DovutoService dovutoService;

  @Autowired
  private DbToolsService dbToolsService;

  @Operation(summary = "Configurazione applicativa",
      description = "Ritorna i parametri della configurazione applicativa",
      responses = { @ApiResponse(description = "I parametri della configurazione applicativa come mappa chiave/valore")})
  @PostMapping(MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/info/config")
  public ResponseEntity<?> configInfo() {
    Map<String, Object> configInfo = new HashMap<>();
    configInfo.put("fakeAuth", Boolean.valueOf(fakeAuthEnabled));
    if(Boolean.parseBoolean(recaptchaEnabled))
      configInfo.put("recaptchaSiteKey", recaptchaSiteKey);
    configInfo.put("useAuthCookie", !Boolean.parseBoolean(corsEnabled));
    configInfo.put("adminEnte", adminEnte);
    configInfo.put("adminEnteEditUserEnabled", adminEnteEditUserEnabled);
    configInfo.put("externalProfileEnabled", Boolean.valueOf(externalProfileEnabled));
    if(StringUtils.isNotBlank(matomo1TrackerUrl)) {
      configInfo.put("matomo1TrackerUrl", StringUtils.stripToNull(matomo1TrackerUrl));
      configInfo.put("matomo1SiteId", StringUtils.stripToNull(matomo1SiteId));
      configInfo.put("matomo1Suffix", StringUtils.stripToNull(matomo1Suffix));
    }
    if(StringUtils.isNotBlank(matomo2TrackerUrl)) {
      configInfo.put("matomo2TrackerUrl", StringUtils.stripToNull(matomo2TrackerUrl));
      configInfo.put("matomo2SiteId", StringUtils.stripToNull(matomo2SiteId));
      configInfo.put("matomo2Suffix", StringUtils.stripToNull(matomo2Suffix));
    }
    return ResponseEntity.status(HttpStatus.OK).body(configInfo);
  }

  @Operation(summary = "Informazioni applicazione",
      description = "Ritorna le informazioni dell'applicazione (es. versione, build time, etc..)",
      responses = { @ApiResponse(description = "Le informazioni dell'applicazione come mappa chiave/valore")})
  @PostMapping(MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/info/app")
  public ResponseEntity<?> appInfo() {
    Map<String, String> appInfo = new HashMap<>();
    appInfo.put("gitHash", buildProperties.get("gitHash"));
    appInfo.put("branchName", buildProperties.get("branchName"));
    appInfo.put("lastTag", buildProperties.get("lastTag"));
    appInfo.put("version", buildProperties.get("version"));
    appInfo.put("buildTime", buildProperties.get("time"));
    appInfo.put("startTime", Long.toString(applicationStartupService.getApplicationReadyTimestamp()));
    return ResponseEntity.status(HttpStatus.OK).body(appInfo);
  }

  @GetMapping(MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/info/check")
  public ResponseEntity<?> checkAppStatus(@RequestParam(name="key", required = false) String paOperationsKey) {
    if(StringUtils.isBlank(this.paOperationsKey) || !StringUtils.equals(this.paOperationsKey, paOperationsKey))
      throw new NotAuthorizedException("non autorizzato");

    Map<String, String> appStatus = new HashMap<>();
    appStatus.put("dovuto", Boolean.toString(dovutoService.checkAppStatusDovuto()));
    return ResponseEntity.status(HttpStatus.OK).body(appStatus);
  }


  @Operation(summary = "Ping",
      description = "Metodo per verificare che il server stia rispondendo correttamente",
      responses = { @ApiResponse(description = "La data corrente in millisecondi dal 01/Gen/1970")})
  @GetMapping(MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/info/ping")
  public ResponseEntity<?> ping() {
    return ResponseEntity.status(HttpStatus.OK).body(""+System.currentTimeMillis());
  }

  @Operation(hidden = true)
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  @GetMapping(MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/test/queue")
  public ResponseEntity<?> testQueue(@RequestParam(required = false) String msg) {
    msg = "["+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +"] "+(msg!=null?msg:"");
    String msgId = queueProducer.enqueueTest(msg);
    return ResponseEntity.status(HttpStatus.OK).body("enqueued msg: '"+msg+"' - msgId: "+msgId);
  }

  @Operation(summary = "CacheFlush",
    description = "Metodo per svuotare la cache applicativa",
    responses = { @ApiResponse(description = "L'elenco dei nomi delle cache svuotate")})
  @GetMapping(MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/cache/flush")
  @Operatore(appAdmin = true)
  public ResponseEntity<?> cacheFlush() {
    return ResponseEntity.ok(cacheService.cacheFlush());
  }

  @Operation(summary = "CacheFlush specific cache",
    description = "Metodo per svuotare la cache applicativa passando il nome cache",
    responses = { @ApiResponse(description = "OK o msg di errore")})
  @GetMapping(MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/cache/flush/{cacheName}")
  @Operatore(appAdmin = true)
  public ResponseEntity<?> cacheFlush(@PathVariable String cacheName) {
    return ResponseEntity.ok(cacheService.cacheFlush(cacheName));
  }

  @Operation(summary = "CacheFlush specific key on cache",
    description = "Metodo per svuotare la cache applicativa passando il nome cache e la chiave",
    responses = { @ApiResponse(description = "OK o msg di errore")})
  @GetMapping(MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/cache/flush/{cacheName}/{cacheKey}")
  @Operatore(appAdmin = true)
  public ResponseEntity<?> cacheFlush(@PathVariable String cacheName, @PathVariable String cacheKey) {
    return ResponseEntity.ok(cacheService.cacheFlush(cacheName, cacheKey));
  }

  @Operation(summary = "CacheGet all cache",
    description = "Metodo per ottenere la cache applicativa",
    responses = { @ApiResponse(description = "OK o msg di errore")})
  @GetMapping(MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/cache/get")
  @Operatore(appAdmin = true)
  public ResponseEntity<?> cacheGet() {
    return ResponseEntity.ok(cacheService.cacheGet());
  }

  @Operation(summary = "CacheGet specific cache",
    description = "Metodo per ottenere la cache applicativa passando il nome cache",
    responses = { @ApiResponse(description = "OK o msg di errore")})
  @GetMapping(MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/cache/get/{cacheName}")
  @Operatore(appAdmin = true)
  public ResponseEntity<?> cacheGet(@PathVariable String cacheName) {
    return ResponseEntity.ok(cacheService.cacheGet(cacheName));
  }

  @Operation(summary = "CacheGet specific key on cache",
    description = "Metodo per ottenere la cache applicativa passando il nome cache e la chiave",
    responses = { @ApiResponse(description = "OK o msg di errore")})
  @GetMapping(MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/cache/get/{cacheName}/{cacheKey}")
  @Operatore(appAdmin = true)
  public ResponseEntity<?> cacheGet(@PathVariable String cacheName, @PathVariable String cacheKey) {
    return ResponseEntity.ok(cacheService.cacheGet(cacheName, cacheKey));
  }

  @GetMapping(value = MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/fs", produces = {MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @Operatore(appAdmin = true)
  public ResponseEntity<?> fsGet(@RequestParam(defaultValue = ".") String path) {
    String responseMsg;
    try {
      Path file = Paths.get(myBoxRootPath, path);
      if (!StringUtils.startsWith(
        file.toAbsolutePath().normalize().toString(),
        Paths.get(myBoxRootPath).toAbsolutePath().normalize().toString())) {
        responseMsg = "not authorized";
      } else if (Files.exists(file)) {
        String normalizedRelativePath = StringUtils.substringAfter(
          file.toAbsolutePath().normalize().toString(),
          Paths.get(myBoxRootPath).toAbsolutePath().normalize().toString());
        if (Files.isDirectory(file)) {
          responseMsg = Files.list(file).map(aFile -> {
            String permissions;
            String owner;
            String group;
            String fileTime;
            String size;
            try {
              owner = Files.getOwner(aFile).getName();
              permissions = PosixFilePermissions.toString(Files.getPosixFilePermissions(aFile));
              PosixFileAttributes attr = Files.readAttributes(aFile, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
              group = attr.group().getName();
              size = Utilities.humanReadableByteCountSI(Files.size(aFile));
              fileTime = formatter.format(attr.creationTime().toInstant());
            } catch (Exception e) {
              log.warn("error on fs get", e);
              permissions = "?";
              owner = "?";
              group = "?";
              size = "?";
              fileTime = "?";
            }
            String folder = Files.isDirectory(aFile) ? "d" : "-";
            String name = aFile.getFileName().toString();
            String nameLink;
            nameLink = "<a href='?path="+ URLEncoder.encode(normalizedRelativePath+"/"+name, StandardCharsets.UTF_8)+"'>"+StringEscapeUtils.escapeHtml4(name)+"</a>";
            return String.format("%s %10s %10s %7s %s %s", folder+permissions, owner, group, size, fileTime, nameLink);
          }).collect(Collectors.joining("\n"))+"\n";
          String currentFolder = "<b>"+StringUtils.firstNonBlank(normalizedRelativePath, "[Root folder]")+"</b>\n\n";
          String backUpFolderString = "";
          if(StringUtils.isNotBlank(normalizedRelativePath))
            backUpFolderString = "<a href='?path="+URLEncoder.encode(normalizedRelativePath+"/..", StandardCharsets.UTF_8)+"'>.. [up]</a>\n";
          responseMsg = "<html><head></head><body><pre>\n"+currentFolder+backUpFolderString+responseMsg+backUpFolderString+"</pre></body></html>";
        } else if (Files.isRegularFile(file)) {
          try {
            String realFileName = FilenameUtils.getName(file.getFileName().toString());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + realFileName);
            return ResponseEntity.ok()
              .headers(headers)
              .contentLength(file.toFile().length())
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              .body(new InputStreamResource(new FileInputStream(file.toFile())));
          } catch (Exception e) {
            log.error("error downloading file [{}]", file, e);
            responseMsg = "error downloading file " + file + ": " + e;
          }
        } else {
          responseMsg = "unknown type";
        }
      } else {
        responseMsg = "not found";
      }
    } catch (Exception e){
      log.error("system error on fsGet for path[{}]", path, e);
      responseMsg = "system error: "+e;
    }
    return ResponseEntity.ok(responseMsg);
  }

  @GetMapping(MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/db/{dbName}/locks")
  @Operatore(appAdmin = true)
  public ResponseEntity<?> getDbLocks(@PathVariable String dbName) {
    List<Map<String, Object>> locks;
    if(StringUtils.equalsIgnoreCase(dbName, "pa"))
      locks = dbToolsService.getPaDbLocks();
    else if(StringUtils.equalsIgnoreCase(dbName, "fesp"))
      locks = dbToolsService.getFespDbLocks();
    else
      return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("unknown DB");

    return locks==null || locks.size()==0 ? ResponseEntity.status(HttpStatus.NO_CONTENT).build() : ResponseEntity.ok(locks);
  }

}
