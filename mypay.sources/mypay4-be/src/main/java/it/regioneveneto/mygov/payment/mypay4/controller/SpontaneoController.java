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
import it.regioneveneto.mygov.payment.mypay4.dto.CartItem;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteTipoDovutoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteTo;
import it.regioneveneto.mygov.payment.mypay4.dto.SpontaneoFormTo;
import it.regioneveneto.mygov.payment.mypay4.dto.SpontaneoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Slf4j
@ConditionalOnWebApplication
public class SpontaneoController {

  private final static String AUTHENTICATED_PATH ="spontaneo";
  private final static String ANONYMOUS_PATH= MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;

  @Autowired
  EnteService enteService;

  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  EnteFunzionalitaService enteFunzionalitaService;

  @Autowired
  SpontaneoService spontaneoService;

  @Autowired
  MessageSource messageSource;

  @Autowired
  private RecaptchaService recaptchaService;

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
      log.warn("Error validating spontaneo",ex);
      String errorMsg = StringUtils.isBlank(ex.getMessage()) ?
          messageSource.getMessage("pa.errore.internalError", null, Locale.ITALY) : ex.getMessage();
      return SpontaneoTo.builder().errorMsg(errorMsg).build();
    }
  }

  @GetMapping(ANONYMOUS_PATH+"/initialize/{codIpaEnte}/{codTipo}")
  public ResponseEntity<?> initializeFormAnonymous(@PathVariable String codIpaEnte, @PathVariable String codTipo,
                                                   @RequestParam String recaptcha) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"initializeForm");
    if(!captchaVerified){
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("errorCode", "Errore verifica recaptcha");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
    }
    return ResponseEntity.ok(spontaneoService.initializeForm(codIpaEnte, codTipo));
  }

  @PostMapping(ANONYMOUS_PATH+"/validate/{codIpaEnte}/{codTipo}")
  public SpontaneoTo validateFormAnonymous(@PathVariable String codIpaEnte, @PathVariable String codTipo,
                                       @RequestBody SpontaneoFormTo spontaneoForm,
                                       @RequestParam String recaptcha) {
    try {
      boolean captchaVerified = recaptchaService.verify(recaptcha,"validateForm");
      if(!captchaVerified){
        return SpontaneoTo.builder().errorMsg("Errore verifica recaptcha").build();
      }
      return spontaneoService.validate(codIpaEnte, codTipo, spontaneoForm, true);
    } catch(Exception ex) {
      String errorMsg = StringUtils.isBlank(ex.getMessage()) ?
          messageSource.getMessage("pa.errore.internalError", null, Locale.ITALY) : ex.getMessage();
      return SpontaneoTo.builder().errorMsg(errorMsg).build();
    }
  }

  @PostMapping(AUTHENTICATED_PATH + "/prepareAvviso")
  public DovutoTo prepareAvviso(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                @RequestBody CartItem spontaneo) {
    return spontaneoService.prepareAvviso(user, spontaneo);
  }

  @PostMapping(ANONYMOUS_PATH + "/prepareAvviso")
  public DovutoTo prepareAvvisoAnonymous(@RequestBody CartItem spontaneo,@RequestParam String recaptcha) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"prepareAvviso");
    if(!captchaVerified){
      throw new MyPayException("Errore verifica recaptcha");
    }
    return spontaneoService.prepareAvviso(null, spontaneo);
  }


}
