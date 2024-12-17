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
import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteFunzionalita;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.ThumbnailService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Tag(name = "BackofficeEnte", description = "Backoffice Ente")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class BackofficeEnteController {

  private static final String AUTHENTICATED_PATH ="admin/enti";
  private static final String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  @Autowired
  private EnteService enteService;

  @Autowired
  private EnteFunzionalitaService enteFunzionalitaService;

  @Autowired
  private RegistroOperazioneService registroOperazioneService;

  @Autowired
  private TassonomiaService tassonomiaService;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  ThumbnailService thumbnailService;

  @Autowired
  LingueAggiuntivaAvvisoService lingueAggiuntivaAvvisoService;

  @GetMapping(OPERATORE_PATH+"/lingueAggiuntive")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<LingueAggiuntiveAvvisoCodDescTo> getLingueAggiuntive(@AuthenticationPrincipal UserWithAdditionalInfo user) {
    return lingueAggiuntivaAvvisoService.getLingueAggiuntive();
  }

  @GetMapping(OPERATORE_PATH+"/tipiEnte")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<TassonomiaCodDescTo> getTipiEnte(@AuthenticationPrincipal UserWithAdditionalInfo user) {
    return tassonomiaService.getTipoEnteForSelect();
  }

  @GetMapping(OPERATORE_PATH+"/anagraficaStati")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<AnagraficaStatoTo> getAnagraficaStati(@AuthenticationPrincipal UserWithAdditionalInfo user) {
    return anagraficaStatoService.getByTipoStato(Constants.STATO_TIPO_ENTE).stream()
        .map(AnagraficaStatoService::mapToDto).collect(toList());
  }

  @GetMapping(OPERATORE_PATH)
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<AnagraficaEnteTo> searchEnti(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                           @RequestParam(required = false) String codIpaEnte,
                                           @RequestParam(required = false) String deNome,
                                           @RequestParam(required = false) String codFiscale,
                                           @RequestParam(required = false) Long idStato,
                                           @RequestParam(required = false) LocalDate dtAvvioFrom,
                                           @RequestParam(required = false) LocalDate dtAvvioTo,
                                           @RequestParam(required = false) String logoMode){
    Function<Ente, AnagraficaEnteTo> mapper = "hash".equals(logoMode) ? enteService::mapAnagraficaEnteToDtoWithThumbnailHash : enteService::mapAnagraficaEnteToDtoWithThumbnail;
    List<Ente> enti = enteService.searchEnti(codIpaEnte, deNome, codFiscale, idStato, dtAvvioFrom, dtAvvioTo);
    if(!user.isSysAdmin()) {
      Predicate<Ente> filterNoMatch = e -> !enteService.getCodIpaEntiForAdminEnte(user).contains(e.getCodIpaEnte());
      enti.removeIf(filterNoMatch);
    }
    return enti.stream().map(mapper).collect(toList());
  }

  @GetMapping(OPERATORE_PATH+"/{mygovEnteId}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN}, value="mygovEnteId")
  public AnagraficaEnteTo getEnteById(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId){
    Ente ente = enteService.getEnteById(mygovEnteId);
    return enteService.getAnagraficaTo(ente, user.isSysAdmin());
  }

  @PostMapping(OPERATORE_PATH+"/ente/insert")
  @Operatore(appAdmin = true)
  public AnagraficaEnteTo insertEnte(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestBody AnagraficaEnteTo ente){
    return enteService.insertEnte(user, ente);
  }

  @PostMapping(OPERATORE_PATH+"/{mygovEnteId}/update")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN}, value="mygovEnteId")
  public AnagraficaEnteTo updateEnte(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                     @PathVariable Long mygovEnteId, @RequestBody AnagraficaEnteTo ente){
    if(!Objects.equals(ente.getMygovEnteId(), mygovEnteId))
      throw new BadRequestException("invalid mygovEnteId");
    return enteService.updateEnte(user, ente);
  }

  @GetMapping(OPERATORE_PATH+"/funzionalita/{mygovEnteId}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN}, value="mygovEnteId")
  public List<EnteFunzionalita> searchFunzionalita(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId){
    Ente ente = enteService.getEnteById(mygovEnteId);
    return enteFunzionalitaService.getAllByCodIpaEnte(ente.getCodIpaEnte());
  }

  @GetMapping(OPERATORE_PATH+"/funzionalita/{mygovEnteId}/{funzionalita}")
  @Operatore(appAdmin = true)
  public List<RegistroOperazioneTo> registroFunzionalita(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                                         @PathVariable Long mygovEnteId, @PathVariable String funzionalita){
    Ente ente = enteService.getEnteById(mygovEnteId);
    List<RegistroOperazione> registro = registroOperazioneService.getByTipoAndOggetto(RegistroOperazione.TipoOperazione.ENTE_FUNZ, ente.getCodIpaEnte()+'|'+funzionalita);
    return registro.stream().map(registroOperazioneService::mapRegistroOperazioneToDto).collect(toList());
  }

  @GetMapping(OPERATORE_PATH+"/funzionalita/activate/{mygovEnteFunzionalitaId}")
  @Operatore(appAdmin = true)
  public void activateFunzionalita(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteFunzionalitaId){
    enteFunzionalitaService.switchActivation(mygovEnteFunzionalitaId, true, user);
  }

  @GetMapping(OPERATORE_PATH+"/funzionalita/deactivate/{mygovEnteFunzionalitaId}")
  @Operatore(appAdmin = true)
  public void deactivateFunzionalita(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteFunzionalitaId){
    enteFunzionalitaService.switchActivation(mygovEnteFunzionalitaId, false, user);
  }

  @PostMapping(OPERATORE_PATH+"/{mygovEnteId}/saveLogo")
  @Operatore(appAdmin = true)
  public EnteTo saveLogo(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                 @RequestParam("file") MultipartFile file) {
    return enteService.updateEnteLogo(mygovEnteId, file);
  }
}