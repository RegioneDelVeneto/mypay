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
import it.regioneveneto.mygov.payment.mypay4.dto.EnteRolesTo;
import it.regioneveneto.mygov.payment.mypay4.dto.OperatoreTo;
import it.regioneveneto.mygov.payment.mypay4.dto.RegistroOperazioneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.UtenteTo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotAuthorizedException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Tag(name = "Operatori", description = "Backoffice - gestione degli operatori")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class BackofficeUtenteController {

  private static final String AUTHENTICATED_PATH ="admin/utenti";
  private static final String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  @Value("${pa.adminEnte.editUser.enabled:false}")
  private boolean adminEnteEditUserEnabled;

  @Autowired
  private UtenteService utenteService;

  @Autowired
  private OperatoreService operatoreService;

  @Autowired
  private OperatoreEnteTipoDovutoService operatoreEnteTipoDovutoService;

  @Autowired
  private UtenteProfileService utenteProfileService;

  @Autowired
  private RegistroOperazioneService registroOperazioneService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private EnteService enteService;

  @GetMapping(OPERATORE_PATH+"/search")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<UtenteTo> searchUtenti(
      @RequestParam(required = false) String username, @RequestParam(required = false) String cognome,
      @RequestParam(required = false) String nome, @RequestParam(required = false, defaultValue = "true") boolean onlyOper){
    return utenteService.searchUtenti(username, cognome, nome, onlyOper);
  }

  @GetMapping(OPERATORE_PATH+"/search/ente/{mygovEnteId}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN}, value="mygovEnteId")
  public List<UtenteTo> searchUtentiForEnte(
      @PathVariable Long mygovEnteId, @RequestParam(required = false) String username,
      @RequestParam(required = false) String cognome, @RequestParam(required = false) String nome){
    return utenteService.searchOperatoriForEnte(mygovEnteId, username, cognome, nome);
  }

  @GetMapping(OPERATORE_PATH+"/search/tipoDovuto/{mygovEnteId}/{mygovEnteTipoDovutoId}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN}, value="mygovEnteId")
  public List<UtenteTo> searchUtentiForEnteTipoDovuto(
      @PathVariable Long mygovEnteId, @PathVariable Long mygovEnteTipoDovutoId, @RequestParam(required = false) String username,
      @RequestParam(required = false) String cognome, @RequestParam(required = false) String nome, @RequestParam(required = false) Boolean flgAssociato){
    return utenteService.searchUtentiForEnteTipoDovuto(mygovEnteId, mygovEnteTipoDovutoId, username, cognome, nome, flgAssociato);
  }

  @GetMapping(OPERATORE_PATH+"/{id}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public OperatoreTo getDetailUtente(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long id){
    //force refresh user profile data from profiling system
    Utente utente = utenteService.getById(id).orElseThrow(NotFoundException::new);
    utenteProfileService.getAndUpdateUserTenantsAndRoles(utente.getCodFedUserId());
    //retrieve operatore details
    Optional<OperatoreTo> operatoreTo = operatoreService.getOperatoreDetails(id);
    if(!user.isSysAdmin()) {
      Predicate<EnteRolesTo> filterNoMatch = e -> !enteService.getCodIpaEntiForAdminEnte(user).contains(e.getCodIpaEnte());
      operatoreTo.map(OperatoreTo::getFullEntiRoles).stream().forEach(list -> list.removeIf(filterNoMatch));
    }
    return operatoreTo.orElseThrow(NotFoundException::new);
  }

  @PostMapping(OPERATORE_PATH+"/{id}/anagrafica")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public void updateAnagraficaUtente(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long id, @RequestBody UtenteTo utente){
    if(!user.isSysAdmin() && !adminEnteEditUserEnabled){
      throw new NotAuthorizedException("this user cannot execute this operation");
    }
    //use userId from path
    utente.setUserId(id);
    //check fields
    if(StringUtils.isBlank(utente.getNome()) || StringUtils.isBlank(utente.getCognome()) ||
        StringUtils.isBlank(utente.getEmail()) || !Utilities.isValidEmail(utente.getEmail()) ||
        StringUtils.isBlank(utente.getCodiceFiscale()) || !Utilities.isValidCFpf(utente.getCodiceFiscale()) )
      throw new BadRequestException();
    //update user
    utenteService.updateAnagraficaUtente(utente);
  }

  @PostMapping(OPERATORE_PATH+"/anagrafica")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public Long insertAnagraficaUtente(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestBody UtenteTo utenteTo){
    if(!user.isSysAdmin() && !adminEnteEditUserEnabled){
      throw new NotAuthorizedException("this user cannot execute this operation");
    }
    //check fields
    if(StringUtils.isBlank(utenteTo.getNome()) || StringUtils.isBlank(utenteTo.getCognome()) ||
        StringUtils.isBlank(utenteTo.getUsername()) ||
        StringUtils.isBlank(utenteTo.getCodiceFiscale()) || !Utilities.isValidCFpf(utenteTo.getCodiceFiscale()) ||
        StringUtils.isBlank(utenteTo.getEmail()) || !Utilities.isValidEmail(utenteTo.getEmail()))
      throw new BadRequestException();
    //check that user doesn't exist
    if(utenteService.getByCodFedUserId(utenteTo.getUsername()).isPresent())
      throw new BadRequestException("an user with same username already exists");

    //insert user
    Utente utente = Utente.builder()
        .codFedUserId(utenteTo.getUsername())
        .deLastname(utenteTo.getCognome())
        .deFirstname(utenteTo.getNome())
        .codCodiceFiscaleUtente(utenteTo.getCodiceFiscale())
        .deEmailAddress(utenteTo.getEmail())
        .deEmailAddressNew(null)
        .emailSourceType(Utente.EMAIL_SOURCE_TYPES.BACKOFFICE.asChar())
        .deFedLegalEntity("fisica")
        .build();
    //return userid
    return utenteService.upsertUtente(utente, false).getMygovUtenteId();
  }

  @GetMapping(OPERATORE_PATH+"/username/{username}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public boolean checkExistingUsername(@PathVariable String username){
    return utenteService.getByCodFedUserId(username).isPresent();
  }

  @PostMapping(OPERATORE_PATH+"/{username}/coupleEnte/{idEnte}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public EnteRolesTo addAssociationOperatoreEnte(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable String username,
                                                 @PathVariable Long idEnte, @RequestParam String couplingMode, @RequestParam String emailAddress){
    OperatoreService.COUPLING_MODE couplingModeValue = OperatoreService.COUPLING_MODE.valueOf(couplingMode);
    if(!OperatoreService.COUPLING_MODE.DOVUTI.equals(couplingModeValue))
      utenteProfileService.checkDisabledEditWhenExternalProfile();

    if(StringUtils.isBlank(emailAddress) || !Utilities.isValidEmail(emailAddress)){
      throw new BadRequestException("invalid email address");
    }
    //update operatore/ente association
    return operatoreService.addAssociationOperatoreEnte(user, username, idEnte, couplingModeValue, emailAddress);
  }

  @DeleteMapping(OPERATORE_PATH+"/{username}/coupleEnte/{idEnte}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public void deleteAssociationOperatoreEnte(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable String username,
                                             @PathVariable Long idEnte, @RequestParam boolean decoupleOnlyTipiDovuto){
    if(!decoupleOnlyTipiDovuto)
      utenteProfileService.checkDisabledEditWhenExternalProfile();
    //update operatore/ente association
    operatoreService.deleteAssociationOperatoreEnte(user, username, idEnte, decoupleOnlyTipiDovuto);
  }

  @PostMapping(OPERATORE_PATH+"/{idOperatore}/coupleTipo/{idTipo}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public EnteRolesTo addAssociationOperatoreEnteTipoDovuto(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                                           @PathVariable Long idOperatore, @PathVariable Long idTipo){
    return operatoreService.addAssociationOperatoreEnteTipoDovuto(user, idOperatore, idTipo);
  }

  @DeleteMapping(OPERATORE_PATH+"/{idOperatore}/coupleTipo/{idTipo}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public void deleteAssociationOperatoreEnteTipoDovuto(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                                       @PathVariable Long idOperatore, @PathVariable Long idTipo){
    operatoreService.deleteAssociationOperatoreEnteTipoDovuto(user, idOperatore, idTipo);
  }

  @PostMapping(OPERATORE_PATH+"/{idOperatore}/changeRuolo/{idEnte}")
  @Operatore(appAdmin = true)
  public void changeRuolo(@PathVariable Long idOperatore, @PathVariable Long idEnte, @RequestParam boolean toAdmin){
    utenteProfileService.checkDisabledEditWhenExternalProfile();
    operatoreService.changeRuolo(idOperatore, idEnte, toAdmin);
  }

  @PostMapping(OPERATORE_PATH+"/{idOperatore}/changeEmail/{idEnte}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public void changeEmailAddress(@PathVariable Long idOperatore, @PathVariable Long idEnte, @RequestParam String emailAddress){
    if(StringUtils.isBlank(emailAddress) || !Utilities.isValidEmail(emailAddress)){
      throw new BadRequestException("invalid email address");
    }

    operatoreService.changeEmailAddress(idOperatore, idEnte, emailAddress);
  }

  @GetMapping(OPERATORE_PATH+"/{idOperatore}/registro/{idTipo}")
  @Operatore(appAdmin = true)
  public List<RegistroOperazioneTo> registroTipoDovutoOperatore(@PathVariable Long idOperatore, @PathVariable Long idTipo){
    OperatoreTo operatoreTo = operatoreService.getOperatoreDetails(idOperatore).orElseThrow(()-> new NotFoundException("Operatore not found"));
    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getById(idTipo);
    List<RegistroOperazione> registro = registroOperazioneService.getByTipoAndOggetto(RegistroOperazione.TipoOperazione.OPER_TIP_DOV,
        operatoreTo.getUsername() + '|' + enteTipoDovuto.getMygovEnteId().getCodIpaEnte()+'|'+enteTipoDovuto.getCodTipo());
    return registro.stream().map(registroOperazioneService::mapRegistroOperazioneToDto).collect(Collectors.toList());
  }
}
