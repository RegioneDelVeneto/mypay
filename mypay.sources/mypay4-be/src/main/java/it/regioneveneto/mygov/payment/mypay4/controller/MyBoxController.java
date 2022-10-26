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

import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.WsImportTo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.AppErrorService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "MyBox", description = "Gestione di upload e download dei file")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class MyBoxController {

  public final static String AUTHENTICATED_PATH = "mybox";
  public final static String ANONYMOUS_PATH = MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;
  public final static String UPLOAD_FLUSSO_PATH = ANONYMOUS_PATH+"/uploadFlusso";
  public final static String DOWNLOAD_FLUSSO_PATH = ANONYMOUS_PATH+"/download";

  @Value("${mypay.path.import.dovuti}")
  private String dovutiImportPath;

  @Value("${mypay.path.import.tassonomia}")
  private String tassonomiaImportPath;

  @Autowired
  MyBoxService myBoxService;

  @Autowired
  EnteService enteService;

  @Autowired
  it.regioneveneto.mygov.payment.mypay4.service.fesp.EnteService enteFespService;

  @Autowired
  TassonomiaService tassonomiaService;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Autowired
  private FlussoService flussoService;

  @Autowired
  private ImportDovutiService importDovutiService;

  @Autowired
  private AppErrorService appErrorService;

  @PostMapping(path=UPLOAD_FLUSSO_PATH, consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<?> uploadByWS(@RequestParam("authorizationToken") String authorizationToken, MultipartHttpServletRequest request) {
    Map<String, String> responseMap = new HashMap<>();
    try {
      MultipartFile file = getMultipartFileFromRequest(request);

      WsImportTo uploadWsTo = jwtTokenUtil.parseWsAuthorizationToken(authorizationToken);
      Pair<String, String> uploaded;
      switch (uploadWsTo.getUploadType()) {
        case FlussoController.FILE_TYPE_FLUSSI_IMPORT:
          String codIpa = uploadWsTo.getCodIpaEnte();
          Ente ente = enteService.getEnteByCodIpa(codIpa);
          if (ente == null)
            throw new ValidatorException("invalid ente");
          if (importDovutiService.getFlussoImport(uploadWsTo.getRequestToken()) != null) {
            log.warn("uploadByWS with duplicate request token {}", uploadWsTo.getRequestToken());
            throw new BadRequestException("Request token già usato");
          }
          String relativePath = StringUtils.firstNonBlank(uploadWsTo.getImportPath(), ente.getCodIpaEnte() + File.separator + dovutiImportPath);
          uploaded = myBoxService.uploadFile(relativePath, file, uploadWsTo.getRequestToken());
          flussoService.onUploadFile(codIpa + "-" + Constants.WS_USER, ente, uploadWsTo.getUploadType(), uploaded.getLeft(), uploaded.getRight(), file);
          break;
        default:
          throw new ValidatorException("invalid upload file type");
      }
      responseMap.put("fileName",file.getOriginalFilename());
      responseMap.put("fileSize",file.getSize()+"");
      responseMap.put("fileType",file.getContentType());
    } catch(Exception e) {
      String code = (e instanceof ValidatorException) ? "400" : "500";
      Pair<String, String> errorUid = appErrorService.generateNowStringAndErrorUid();
      log.error("uploadByWS exception now[{}] errorUid[{}]", errorUid.getLeft(), errorUid.getRight(), e);
      responseMap.put("codice",code);
      responseMap.put("descrizione","["+errorUid.getRight()+"] "+e.getMessage());
    }
    // responseMap has been put in a list because of legacy response, it was a JSONArray
    return ResponseEntity.status(HttpStatus.OK).body(List.of(responseMap));
  }

  @PostMapping(path=AUTHENTICATED_PATH+"/upload/{mygovEnteId}", consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<?> uploadByWebapp(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                          @RequestParam String type, MultipartHttpServletRequest request) {

    MultipartFile file = getMultipartFileFromRequest(request);

    Pair<String, String> uploaded;
    switch (type){
      case FlussoController.FILE_TYPE_FLUSSI_IMPORT:
        Ente ente = enteService.getEnteById(mygovEnteId);
        if (ente == null)
          throw new ValidatorException("invalid ente");
        uploaded = myBoxService.uploadFile(ente.getCodIpaEnte() + File.separator + dovutiImportPath, file, null);
        flussoService.onUploadFile(user.getUsername(), ente, type, uploaded.getLeft(), uploaded.getRight(), file);
        break;
      case BackofficeFlussoController.FILE_TYPE_TASSONOMIA_IMPORT:
        uploaded = myBoxService.uploadFile(tassonomiaImportPath, file, null);
        tassonomiaService.onUploadFile(user, type, uploaded.getLeft(), uploaded.getRight());
        break;
      default:
        throw new ValidatorException("invalid upload file type");
    }
    return ResponseEntity.ok().build();
  }

  @LogExecution(enabled = LogExecution.ParamMode.OFF)
  String generateSecurityToken(String type, String filePath, UserWithAdditionalInfo user, Long mygovEnteId){
    return jwtTokenUtil.generateSecurityToken(user, type+"|"+mygovEnteId+"|"+filePath);
  }

  @GetMapping(ANONYMOUS_PATH+"/download/{mygovEnteId}")
  public ResponseEntity<?> downloadPublic(@PathVariable Long mygovEnteId, @RequestParam String type,
                                          @RequestParam String securityToken, @RequestParam String filename) {
    return this.download(null, mygovEnteId, type, filename, securityToken);
  }

  @GetMapping(AUTHENTICATED_PATH+"/download/{mygovEnteId}")
  public ResponseEntity<?> download(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                    @RequestParam String type, @RequestParam String filename,
                                    @RequestParam String securityToken) {

    //check that is downloading something attached to a security token
    String oid;
    try {
      oid = jwtTokenUtil.parseSecurityToken(user, securityToken);
    } catch(ExpiredJwtException e){
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token download scaduto, si prega di ricaricare la pagina e ripetere l'operazione.");
    }
    log.trace("{}|{}|{} <--> {}",type,mygovEnteId,filename,oid);

    String path = "";
    switch (type){
      case FlussoController.FILE_TYPE_FLUSSI_EXPORT:
        Ente ente = enteService.getEnteById(mygovEnteId);
        if(ente==null)
          throw new ValidatorException("invalid ente");
        path = ente.getCodIpaEnte();
        if (!(type+"|"+mygovEnteId+"|"+filename).equals(oid)) {
          throw new MyPayException("codice sicurezza non valido");
        }
        break;
      case FlussoController.FILE_TYPE_FLUSSI_RENDICONTAZIONE:
      case FlussoController.FILE_TYPE_FLUSSI_QUADRATURA:
        if (user==null) {
          it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente enteFesp = enteFespService.getEnteById(mygovEnteId);
          if(enteFesp==null)
            throw new ValidatorException("invalid ente");
          path = enteFesp.getCodIpaEnte();
          if (!(type+"|"+enteFesp.getMygovEnteId()+"|"+filename).equals(oid)) {
            throw new MyPayException("codice sicurezza non valido");
          }
        }
        else {
          ente = enteService.getEnteById(mygovEnteId);
          if(ente==null)
            throw new ValidatorException("invalid ente");
          path = ente.getCodIpaEnte();
          if (!(type+"|"+mygovEnteId+"|"+filename).equals(oid)) {
            throw new MyPayException("codice sicurezza non valido");
          }
        }
        break;
      case FlussoController.FILE_TYPE_FLUSSI_IMPORT:
        ente = enteService.getEnteById(mygovEnteId);
        if(ente==null)
          throw new ValidatorException("invalid ente");
        path = ente.getCodIpaEnte();
        break;
      case BackofficeFlussoController.FILE_TYPE_TASSONOMIA_IMPORT:
        if (!(type+"|"+mygovEnteId+"|"+filename).startsWith(oid)) {
          throw new MyPayException("codice sicurezza non valido");
        }
        break;
      default:
        throw new MyPayException("invalid type: "+type);
    }

    Pair<Resource, Long> file = myBoxService.downloadFile(path, filename);
    if(file==null)
      throw new NotFoundException("File \""+filename+"\" non disponibile");

    String realFileName = FilenameUtils.getName(filename);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+realFileName);
    log.debug("downloading file with oid[{}], path[{}], filename[{}] - length[{} bytes]", oid, path, filename, file.getRight());
    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(file.getRight())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(file.getLeft());
  }

  //retrieve the file this way and not with @RequestParam because we don't know the param name used in the request and may allow any value
  private MultipartFile getMultipartFileFromRequest(MultipartHttpServletRequest request){
    MultipartFile file;
    Map<String, MultipartFile> multipartFileMap = request.getMultiFileMap().toSingleValueMap();
    List<Map.Entry<String, MultipartFile>> listEntryStreamNotEmpty = multipartFileMap.entrySet().stream().filter(entry -> entry.getValue().getSize()>0).collect(Collectors.toList());
    if(listEntryStreamNotEmpty.size()==1){
      String key = listEntryStreamNotEmpty.get(0).getKey();
      file = listEntryStreamNotEmpty.get(0).getValue();
      log.info("retrieving MultipartFile with key [{}] name [{}] size [{}]", key, file.getOriginalFilename(), file.getSize());
    } else if(listEntryStreamNotEmpty.size()>1){
      log.error("invalid MultipartFile size {}", listEntryStreamNotEmpty.size());
      throw new BadRequestException("request multipart non valida, file presenti: "+listEntryStreamNotEmpty.size());
    } else if(multipartFileMap.size() > 0){
      log.error("invalid MultipartFile size, empty file");
      throw new BadRequestException("request multipart non valida, file vuoto");
    } else {
      log.error("invalid MultipartFile size, missing file");
      throw new BadRequestException("request multipart non valida, file non presente");
    }
    return file;
  }

}
