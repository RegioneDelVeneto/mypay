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

import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Slf4j
@ConditionalOnWebApplication
public class SpontaneoController {

  static final String AUTHENTICATED_PATH ="spontaneo";
  private static final String ANONYMOUS_PATH= MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;
  @Autowired
  private EnteService enteService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private EnteFunzionalitaService enteFunzionalitaService;

  @Autowired
  private SpontaneoService spontaneoService;

  @Autowired
  private DovutoService dovutoService;

  @Autowired
  private MailValidationService mailValidationService;

  @Autowired
  private RecaptchaService recaptchaService;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @GetMapping(value= {ANONYMOUS_PATH+"/url" , ANONYMOUS_PATH+"/url/{codIpaEnte}" , ANONYMOUS_PATH+"/url/{codIpaEnte}/{codTipoDovuto}"})
  public ResponseEntity<?> getUrlSpontaneo(@PathVariable Optional<String> codIpaEnte, @PathVariable Optional<String> codTipoDovuto){
    Map<String, String> response = Map.of("url", spontaneoService.getFrontendUrlForSpontaneo(codIpaEnte, codTipoDovuto) );
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping(ANONYMOUS_PATH+"/config/{codIpaEnte}/{codTipoDovuto}")
  public ResponseEntity<?> getConfigSpontaneo(@PathVariable String codIpaEnte, @PathVariable String codTipoDovuto){
    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getSpontaneo(codIpaEnte, codTipoDovuto).orElseThrow(NotFoundException::new);
    Map<String, Object> response = Map.of(
            "codTipo", enteTipoDovuto.getCodTipo(),
            "deTipo", enteTipoDovuto.getDeTipo(),
            "flgCfAnonimo", enteTipoDovuto.isFlgCfAnonimo(),
            "url", spontaneoService.getFrontendUrlForSpontaneo(Optional.of(codIpaEnte), Optional.of(codTipoDovuto)) );
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping(ANONYMOUS_PATH)
  public List<EnteTo> getAllEntiSpontanei(@RequestParam(required = false) String logoMode){
    List<Ente> listEnti = enteService.getAllEntiSpontanei();
    Function<Ente, EnteTo> mapper = "hash".equals(logoMode) ? enteService::mapEnteToDtoWithThumbnailHash : enteService::mapEnteToDtoWithThumbnail;
    return listEnti.stream().map(mapper).collect(Collectors.toList());
  }

  @GetMapping(ANONYMOUS_PATH+"/ente/{codIpaEnte}")
  public List<EnteTo> getEnteSpontaneoByCodIpa(@PathVariable String codIpaEnte, @RequestParam(required = false) String logoMode){
    return Optional.ofNullable(enteService.getEnteByCodIpa(codIpaEnte))
      .filter(ente -> ente.getCdStatoEnte().getCodStato().equals(AnagraficaStato.STATO_ENTE_ESERCIZIO))
      .filter(ente ->
        enteFunzionalitaService.getAllByCodIpaEnte(ente.getCodIpaEnte(), Boolean.TRUE).stream().filter(
          enteFunzionalita -> enteFunzionalita.getCodFunzionalita().equals(Constants.FUNZIONALITA_ENTE_PUBBLICO) ||
            enteFunzionalita.getCodFunzionalita().equals(Constants.FUNZIONALITA_PAGAMENTO_SPONTANEO)).count() == 2)
      .map("hash".equals(logoMode) ? enteService::mapEnteToDtoWithThumbnailHash : enteService::mapEnteToDtoWithThumbnail)
      .map(List::of) //returns a list (of 1 element) instead of Optional for better compatibility with getAllEntiSpontanei()
      .orElseThrow(NotFoundException::new);
  }

  @GetMapping(ANONYMOUS_PATH+"/{id}/tipiDovuto")
  public List<EnteTipoDovutoTo> getTipiDovutoByEnteId(@PathVariable Long id){
    List<EnteTipoDovuto> listEnteTipiDovuto = enteTipoDovutoService.getAttiviByMygovEnteIdAndFlags(id, true, null);
    return listEnteTipiDovuto.stream().map(enteTipoDovutoService::mapEnteTipoDovutoToDto).collect(Collectors.toList());
  }

  @GetMapping(ANONYMOUS_PATH+"/{codIpaEnte}/tipiDovutoByCodIpa")
  public List<EnteTipoDovutoTo> getTipiDovutoByCodIpaEnte(@PathVariable String codIpaEnte){
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
    if(ente==null)
      return Collections.emptyList();
    return this.getTipiDovutoByEnteId(ente.getMygovEnteId());
  }

  @GetMapping(AUTHENTICATED_PATH +"/initialize/{codIpaEnte}/{codTipo}")
  public SpontaneoFormTo initializeForm(@PathVariable String codIpaEnte, @PathVariable String codTipo) {
    return spontaneoService.initializeForm(codIpaEnte, codTipo);
  }

  @PostMapping(AUTHENTICATED_PATH +"/validate/{codIpaEnte}/{codTipo}")
  public SpontaneoTo validateForm(@PathVariable String codIpaEnte, @PathVariable String codTipo,
                                   @RequestBody SpontaneoFormTo spontaneoForm) {
    try {
      return spontaneoService.validate(codIpaEnte, codTipo, spontaneoForm, false);
    } catch(Exception ex) {
      if(ex instanceof ValidatorException && StringUtils.isNotBlank(ex.getMessage())){
        log.warn("Error validating spontaneo",ex);
        return SpontaneoTo.builder().errorMsg(ex.getMessage()).build();
      } else {
        throw (ex instanceof MyPayException) ? (MyPayException)ex : new MyPayException(ex);
      }
    }
  }

  @GetMapping(ANONYMOUS_PATH+"/initialize/{codIpaEnte}/{codTipo}")
  public ResponseEntity<?> initializeFormAnonymous(@PathVariable String codIpaEnte, @PathVariable String codTipo,
                                                   @RequestParam String recaptcha) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"initializeForm");
    if(!captchaVerified){
      log.info("error verifying recaptcha operation[initializeForm] codTipoDovuto[{}] ipa[{}]", codTipo, codIpaEnte);
      throw new BadRequestException("Errore verifica recaptcha");
    }
    return ResponseEntity.ok(spontaneoService.initializeForm(codIpaEnte, codTipo));
  }

  @PostMapping(ANONYMOUS_PATH+"/validate/{codIpaEnte}/{codTipo}")
  public SpontaneoTo validateFormAnonymous(@PathVariable String codIpaEnte, @PathVariable String codTipo,
                                       @RequestBody SpontaneoFormTo spontaneoForm,
                                       @RequestParam String recaptcha) {
    try {
      boolean captchaVerified = recaptchaService.verify(recaptcha, "validateForm");
      if (!captchaVerified) {
        return SpontaneoTo.builder().errorMsg("Errore verifica recaptcha").build();
      }
      return spontaneoService.validate(codIpaEnte, codTipo, spontaneoForm, true);
    } catch(Exception ex) {
      if(ex instanceof ValidatorException && StringUtils.isNotBlank(ex.getMessage())){
        log.warn("Error validating spontaneo",ex);
        return SpontaneoTo.builder().errorMsg(ex.getMessage()).build();
      } else {
        throw (ex instanceof MyPayException) ? (MyPayException)ex : new MyPayException(ex);
      }
    }

  }

  @PostMapping(AUTHENTICATED_PATH + "/prepareAvviso")
  public DovutoTo prepareAvviso(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                @RequestBody CartItem spontaneo) {
    Dovuto dovuto = spontaneoService.prepareAvviso(user, spontaneo);
    DovutoTo dovutoTo = dovutoService.getToById(dovuto.getMygovDovutoId());
    if(dovutoTo!=null)
      dovutoTo.setSecurityTokenAvviso(jwtTokenUtil.generateSecurityToken(user, ""+dovutoTo.getId()));
    return dovutoTo;
  }

  @PostMapping(ANONYMOUS_PATH + "/prepareAvviso")
  public DovutoTo prepareAvvisoAnonymous(@RequestBody CartItem spontaneo,@RequestParam String recaptcha) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"prepareAvviso");
    if(!captchaVerified){
      throw new MyPayException("Errore verifica recaptcha");
    }
    //verify mail has been correctly validated
    String emailToCheck = StringUtils.firstNonBlank(Utilities.ifNotNull(spontaneo.getIntestatario(), AnagraficaPagatore::getEmail), spontaneo.getVersanteEmail());
    if(StringUtils.isBlank(emailToCheck) || !mailValidationService.verifyMailValidationToken(spontaneo.getMailValidationToken(), emailToCheck)){
      if(StringUtils.isBlank(emailToCheck))
        log.info("empty emailToValidate, versanteEmail[{}] intestatarioAnagrafica[{}]", spontaneo.getVersanteEmail(), Utilities.ifNotNull(spontaneo.getIntestatario(), AnagraficaPagatore::getAnagrafica));
      throw new ValidatorException("Errore verifica indirizzo email");
    }

    Dovuto dovuto = spontaneoService.prepareAvviso(null, spontaneo);
    DovutoTo dovutoTo = dovutoService.getToById(dovuto.getMygovDovutoId());
    if(dovutoTo!=null)
      dovutoTo.setSecurityTokenAvviso(jwtTokenUtil.generateSecurityToken(null, ""+dovutoTo.getId()));
    return dovutoTo;
  }


}
