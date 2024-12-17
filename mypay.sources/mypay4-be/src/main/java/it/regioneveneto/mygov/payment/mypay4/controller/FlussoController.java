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
import it.regioneveneto.mygov.payment.mypay4.dto.FileTo;
import it.regioneveneto.mygov.payment.mypay4.dto.FlussoImportTo;
import it.regioneveneto.mygov.payment.mypay4.dto.FlussoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.ExportConservazione;
import it.regioneveneto.mygov.payment.mypay4.model.Flusso;
import it.regioneveneto.mygov.payment.mypay4.queue.QueueProducer;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.FlussoConservazioneService;
import it.regioneveneto.mygov.payment.mypay4.service.FlussoService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Flussi", description = "Gestione dei flussi di pagamento e di rendicontazione")
@RestController
@RequestMapping(MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/flussi")
@Slf4j
@ConditionalOnWebApplication
public class FlussoController {

  public static final String FILE_TYPE_FLUSSI_IMPORT = "FLUSSI_IMPORT";
  public static final String FILE_TYPE_FLUSSI_EXPORT = "FLUSSI_EXPORT";
  public final static String FILE_TYPE_FLUSSI_EXPORT_SCADUTI = "FLUSSI_EXPORT_SCADUTI";
  public static final String FILE_TYPE_FLUSSI_RENDICONTAZIONE = "FLUSSI_RENDIC";
  public static final String FILE_TYPE_FLUSSI_QUADRATURA = "FLUSSI_QUADR";

  public static final String FILE_TYPE_FLUSSI_CONSERVAZIONE = "FLUSSI_CONSERVAZIONE";

  @Autowired
  FlussoService flussoService;

  @Autowired
  EnteService enteService;

  @Autowired
  MyBoxController myBoxController;

  @Autowired
  private FlussoConservazioneService flussoConservazioneService;

  @Autowired
  QueueProducer queueProducer;

  @GetMapping("byEnteId/{mygovEnteId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public List<FlussoTo> getByEnte(@PathVariable Long mygovEnteId){
    List<Flusso> listFlussi = flussoService.getByEnte(mygovEnteId, false);
    return listFlussi.stream().map(this::mapFlussoToDto).collect(Collectors.toList());
  }

  private FlussoTo mapFlussoToDto(Flusso flusso) {
    return flusso == null ? null : FlussoTo.builder()
        .id(flusso.getMygovFlussoId())
        .nome(flusso.getIuf())
        .build();
  }

  @GetMapping("import/{mygovEnteId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public List<FlussoImportTo> flussiImport(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                           @PathVariable Long mygovEnteId,
                                           @RequestParam(required = false) String nomeFlusso,
                                           @RequestParam LocalDate from, @RequestParam LocalDate to){
    // check if user is app-admin..
    boolean isAdmin = user.isSysAdmin() ||
      // .. or ente-admin
      user.getEntiRoles().getOrDefault(enteService.getEnteById(mygovEnteId).getCodIpaEnte(), Set.of())
        .contains(Operatore.Role.ROLE_ADMIN.name());

    //if app/ente admin, then remove constraint to only find your own import request
    List<FlussoImportTo> flussi = flussoService.getByEnteIufCreateDt(mygovEnteId, isAdmin ? null : user.getUsername(), nomeFlusso, from, to);

    //generate security token (to allow download)
    flussi.forEach(flussoImportTo -> {
          // only user who requested the import or is admin app/ente can download it
          if(BooleanUtils.isTrue(flussoImportTo.getShowDownload()))
            flussoImportTo.setSecurityToken(
                myBoxController.generateSecurityToken(FILE_TYPE_FLUSSI_IMPORT, flussoImportTo.getPath(), user, mygovEnteId));
        });

    return flussi;
  }

  @GetMapping("import/{mygovEnteId}/log")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public ResponseEntity<Resource> downloadLog(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                              @RequestParam String fileName) {

    Ente ente = enteService.getEnteById(mygovEnteId);
    try {
      byte[] fileContent = flussoService.downloadLog(ente.getCodIpaEnte(), fileName);
      Resource resource = new ByteArrayResource(fileContent);
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

      return ResponseEntity.ok()
          .headers(headers)
          .contentLength(fileContent.length)
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(resource);
    } catch (Exception e) {
      throw new NotFoundException(e);
    }
  }

  @GetMapping("remove/{mygovEnteId}/{mygovFlussoId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public int flussiRemove(@AuthenticationPrincipal UserWithAdditionalInfo user,
                          @PathVariable Long mygovEnteId, @PathVariable Long mygovFlussoId) {
    return flussoService.removeFlusso(user.getUsername(), mygovEnteId, mygovFlussoId);
  }

  @GetMapping("export/{mygovEnteId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public List<FileTo> flussiExport(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                        @RequestParam(required = false) String nomeFlusso, @RequestParam LocalDate from, @RequestParam LocalDate to){
    // check if user is app-admin..
    boolean isAdmin = user.isSysAdmin() ||
      // .. or ente-admin
      user.getEntiRoles().getOrDefault(enteService.getEnteById(mygovEnteId).getCodIpaEnte(), Set.of())
        .contains(Operatore.Role.ROLE_ADMIN.name());

    //if app/ente admin, then remove constraint to only find your own export request
    List<FileTo> files = flussoService.flussiExport(mygovEnteId, isAdmin ? null : user.getUsername(), nomeFlusso, from, to);

    //generate security token (to allow download)
    files.stream()
        .filter(fileTo -> StringUtils.isNotBlank(fileTo.getPath()))
        .forEach(fileTo -> {
          if(BooleanUtils.isTrue(fileTo.getShowDownload()))
            fileTo.setSecurityToken(
              myBoxController.generateSecurityToken(FILE_TYPE_FLUSSI_EXPORT, fileTo.getPath(), user, mygovEnteId));
        });

    return files;
  }

  @GetMapping("export/insert/{mygovEnteId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public long flussiExportInsert(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                              @RequestParam(required = false)  String tipoDovuto, @RequestParam String versioneTracciato,
                                              @RequestParam LocalDate from, @RequestParam LocalDate to){
    return flussoService.flussiExportInsert(mygovEnteId, user.getCodiceFiscale(), tipoDovuto, versioneTracciato, from, to);
  }

  @GetMapping("spc/R/{mygovEnteId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public List<FileTo> flussiRendicontazione(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                                 @RequestParam String flgProdOrDisp, @RequestParam LocalDate from, @RequestParam LocalDate to) {
    return this.flussiSPC(user, Constants.FLG_TIPO_FLUSSO.RENDICONTAZIONE, mygovEnteId, flgProdOrDisp, from, to);
  }

  @GetMapping("spc/Q/{mygovEnteId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public List<FileTo> flussiQuadratura(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                            @RequestParam String flgProdOrDisp, @RequestParam LocalDate from, @RequestParam LocalDate to) {
    return this.flussiSPC(user, Constants.FLG_TIPO_FLUSSO.QUADRATURA, mygovEnteId, flgProdOrDisp, from, to);
  }

  private List<FileTo> flussiSPC(UserWithAdditionalInfo user, Constants.FLG_TIPO_FLUSSO flgTipoFlusso, Long mygovEnteId,
                                      String flgProdOrDisp, LocalDate from, LocalDate to) {
    List<FileTo> files = flussoService.flussiSPC(flgTipoFlusso, mygovEnteId, flgProdOrDisp, from, to);

    //generate security token (to allow download)
    files.forEach(fileTo -> fileTo.setSecurityToken(myBoxController.generateSecurityToken(
        flgTipoFlusso==Constants.FLG_TIPO_FLUSSO.RENDICONTAZIONE?FILE_TYPE_FLUSSI_RENDICONTAZIONE:FILE_TYPE_FLUSSI_QUADRATURA,
        fileTo.getPath(), user, mygovEnteId)));

    return files;
  }

  @GetMapping("export/conservazione/insert/{mygovEnteId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public long flussiExportConservazioneInsert(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                              @RequestParam String tipoTracciato, @RequestParam LocalDate from,
                                              @RequestParam LocalDate to){
    long insertedRec = flussoService.flussiExportConservazioneInsert(mygovEnteId, user.getCodiceFiscale(), tipoTracciato, from, to);
    return insertedRec;
  }

  @GetMapping("export/conservazione/{mygovEnteId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public List<FileTo> flussiExportConservazione(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                                @RequestParam(required = false) String nomeFlusso, @RequestParam LocalDate from, @RequestParam LocalDate to){
    List<FileTo> files = flussoService.flussiExportConservazione(mygovEnteId, user.getCodiceFiscale(), nomeFlusso, from, to);

    //generate security token (to allow download)
    files.stream().filter(fileTo -> StringUtils.isNotBlank(fileTo.getPath())).forEach(fileTo -> {
      fileTo.setSecurityToken(myBoxController.generateSecurityToken(FILE_TYPE_FLUSSI_CONSERVAZIONE, fileTo.getPath(), user, mygovEnteId));
    });

    return files;
  }

  @GetMapping("conservazione/reload/{mygovEnteId}/{id}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public void reload(@AuthenticationPrincipal UserWithAdditionalInfo user,
                     @PathVariable Long mygovEnteId, @PathVariable Long id) {

    ExportConservazione exportConservazione = flussoConservazioneService.getFlussoConservazioneByID(id);
    flussoConservazioneService.updateExportConservazione(exportConservazione, null, Constants.STATO_EXPORT_LOAD,exportConservazione.getNumDimensioneFileGenerato());
  }
}
