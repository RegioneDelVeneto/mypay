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

import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteTo;
import it.regioneveneto.mygov.payment.mypay4.dto.UploadTo;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.StorageService;
import it.regioneveneto.mygov.payment.mypay4.storage.ContentStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Tag(name = "Storage temporaneo", description = "Gestione dello storage temporaneo di file sul server")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class StorageController {

  private static final String AUTHENTICATED_PATH ="storage";
  private static final String ANONYMOUS_PATH= MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;

  @Autowired
  StorageService storageService;

  @GetMapping(value = ANONYMOUS_PATH+"/object/{tokenId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Object getObject(@PathVariable String tokenId) {
    return storageService.getString(StorageService.WS_USER, tokenId).orElseThrow(NotFoundException::new);
  }

  @PostMapping(value = ANONYMOUS_PATH+"/object/test")
  public ResponseEntity<Object> testAddObject() {
    ContentStorage.StorageToken storageToken = storageService.putObject(StorageService.WS_USER,
        EnteTo.builder().codIpaEnte(UUID.randomUUID().toString()).deNomeEnte(new Date().toString()).build());
    UploadTo uploadTo = UploadTo.builder()
        .uploadId(storageToken.getId())
        .uploadTimestamp(storageToken.getUploadTimestamp())
        .expiryTimestamp(storageToken.getExpiryTimestamp())
        .build();
    return ResponseEntity.ok(uploadTo);
  }

  @PostMapping(AUTHENTICATED_PATH+"/file")
  public ResponseEntity<UploadTo> uploadFile(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                             @RequestParam("file") MultipartFile file) throws IOException {

    String error = null;

    //check max filesize
    if(file.getSize()>storageService.getMaxSizeBytes()){
      error = "Dimensione massima del file superata";
    } else if(file.getSize()==0) {
      error = "File vuoto";
    }

    if(error == null){
      ContentStorage.StorageToken storageToken = storageService.putFile(user.getUsername(), file.getBytes());
      UploadTo uploadTo = UploadTo.builder()
          .uploadId(storageToken.getId())
          .filename(file.getOriginalFilename())
          .size(file.getSize())
          .uploadTimestamp(storageToken.getUploadTimestamp())
          .expiryTimestamp(storageToken.getExpiryTimestamp())
          .build();
      return ResponseEntity.ok(uploadTo);
    } else {
      log.error("uploadFile error: "+error);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(UploadTo.builder().error(error).build());
    }
  }

  @GetMapping(AUTHENTICATED_PATH+"/file/{tokenId}/{filename}")
  public ResponseEntity<Resource> downloadFile(
      @AuthenticationPrincipal UserWithAdditionalInfo user,
      @PathVariable String tokenId,
      @PathVariable String filename) {

    byte[] fileContent = storageService.getFile(user.getUsername(), tokenId);
    if(fileContent==null){
      log.warn("no file found with tokenId: "+tokenId+" - filename: "+filename);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    storageService.deleteStorage(user.getUsername(), tokenId);
    Resource resource = new ByteArrayResource(fileContent);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+filename);

    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(fileContent.length)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
  }

}
