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
package it.regioneveneto.mygov.payment.mypay4.service;

import it.regioneveneto.mygov.payment.mypay4.exception.FileStorageException;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.UUID;

@Service
@Slf4j
public class MyBoxService implements Serializable {

  @Value("${mybox.path.root}")
  public String myBoxRootPath;

  @Value("${mypay.path.relative.data}")
  public String relativeDataPath;

  public Pair<String, String> uploadFile(String relativePath, MultipartFile file, String authToken) {
    try {
      authToken = org.apache.commons.lang3.StringUtils.firstNonBlank(authToken, UUID.randomUUID().toString());
      String filename = StringUtils.cleanPath(file.getOriginalFilename());
      //md5 file
      String filenameMd5 = filename;
      if (filenameMd5.contains(".")) {
        filenameMd5 = filenameMd5.substring(0, filenameMd5.lastIndexOf('.'));
      }
      filenameMd5 += ".md5";
      //auth file
      String filenameAuth = filename;
      if (filenameAuth.contains(".")) {
        filenameAuth = filenameAuth.substring(0, filenameAuth.lastIndexOf('.'));
      }
      filenameAuth += ".auth";
      //path of files
      Path fileLocation = Paths.get(myBoxRootPath, relativeDataPath, relativePath, filename);
      Path md5Location = Paths.get(myBoxRootPath, relativeDataPath, relativePath, filenameMd5);
      Path authLocation = Paths.get(myBoxRootPath, relativeDataPath, relativePath, filenameAuth);
      //create missing parent folder, if any
      if(!fileLocation.toAbsolutePath().getParent().toFile().exists())
        Files.createDirectories(fileLocation.toAbsolutePath().getParent());
      //write file to file-system
      MessageDigest md = MessageDigest.getInstance("MD5");
      try(InputStream is = file.getInputStream();  DigestInputStream dis = new DigestInputStream(is, md)){
        Files.copy(dis, fileLocation, StandardCopyOption.REPLACE_EXISTING);
      }
      String md5 = Hex.encodeHexString(md.digest());
      //write md5 file
      Files.writeString(md5Location, md5);
      //write auth file
      Files.writeString(authLocation, authToken);
      String uploadFilename = Paths.get(relativeDataPath, relativePath, filename).toString();
      log.debug("upload file - filename :"+uploadFilename+" - md5: "+md5+" - authToken: "+authToken);
      return Pair.of(uploadFilename, authToken);
    } catch (Exception e) {
      log.error("error uploading file "+file.getOriginalFilename(), e);
      throw new FileStorageException("Errore nel salvataggio del file " + file.getOriginalFilename() + "["+e.getMessage()+"]");
    }
  }

  public Pair<Resource,Long> downloadFile(String relativePath, String filename){
    try {
      File file = Paths.get(myBoxRootPath, relativeDataPath, relativePath, filename).toFile();
      if(!file.exists() || !file.isFile()){
        log.warn("downloadFile - file[{}] not found",file.getAbsolutePath());
        return null;
      }
      long fileLength = 0;
      try{
        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() - start < 20000 && fileLength==0){
          fileLength = file.length();
          if(fileLength==0) {
            log.info("file [{}] has length 0, checking again in 1s", file.getAbsolutePath());
            Thread.sleep(1000);
          }
        }
      } catch(Exception e){
        log.warn("ignoring error checking file length [{}]",file.getAbsolutePath(), e);
      }

      if(fileLength==0)
        throw new MyPayException("File momentaneamente non disponibile, si prega riprovare in seguito");

      return Pair.of(new InputStreamResource(new FileInputStream(file)), fileLength);
    } catch(FileNotFoundException e){
      log.warn("downloadFile - relativePath[{}] filename[{}] not found", relativePath, filename, e);
      return null;
    }
  }

  public File saveFile(String relativePath, String filename, CharSequence content) {
    try {
      var fileLocation = Path.of(myBoxRootPath, relativeDataPath, relativePath, filename);
      if(!fileLocation.toAbsolutePath().getParent().toFile().exists())
        Files.createDirectories(fileLocation.toAbsolutePath().getParent());
      Files.writeString(fileLocation, content, StandardCharsets.UTF_8);
      log.debug("[{}], close writer", filename);
      return fileLocation.toFile();
    } catch (Exception e) {
      log.error("error saving file "+filename, e);
      throw new FileStorageException("Errore nel salvataggio del file " + filename + "["+e.getMessage()+"]");
    }
  }

  public String archiveFileToZip(File fileToArchive, String relativePath, String filename) {
    var fileLocation = Path.of(myBoxRootPath, relativeDataPath, relativePath, filename);
    try {
      if(!fileLocation.toAbsolutePath().getParent().toFile().exists())
        Files.createDirectories(fileLocation.toAbsolutePath().getParent());
      try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fileLocation)) {
        ArchiveEntry entry = zos.createArchiveEntry(fileToArchive, fileToArchive.getName());
        zos.putArchiveEntry(entry);
        zos.write(Files.readAllBytes(fileToArchive.toPath()));
        zos.closeArchiveEntry();
        zos.finish();
      }
      return Paths.get(relativePath, filename).toString();
    } catch (Exception e) {
      log.error("exception on zipfile management", e);
      throw new FileStorageException("Errore nella creazione del file archivio: "+fileToArchive.getName());
    }
  }
}
