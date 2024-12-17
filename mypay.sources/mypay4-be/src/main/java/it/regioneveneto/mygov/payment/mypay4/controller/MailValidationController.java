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

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.MailValidationRequest;
import it.regioneveneto.mygov.payment.mypay4.dto.MailValidationResponse;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.MailValidationService;
import it.regioneveneto.mygov.payment.mypay4.service.RecaptchaService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Tag(name = "Validazione mail", description = "Gestione della validazione dell'indirizzo email (utenti non autenticati)")
@SecurityRequirements
@RestController
@Slf4j
@ConditionalOnWebApplication
public class MailValidationController {

  private static final String AUTHENTICATED_PATH ="mailvalidation";
  private static final String ANONYMOUS_PATH= MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;

  @Autowired
  private RecaptchaService recaptchaService;

  @Autowired
  private MailValidationService mailValidationService;

  @Autowired
  private EnteService enteService;

  @PostMapping(ANONYMOUS_PATH + "/request")
  public ResponseEntity<MailValidationRequest> requestMailValidationAnonymous(@RequestParam String emailAddress, @RequestParam String recaptcha,
                                                                              @RequestParam(required = false) String codIpaEnte) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"requestMailValidation");
    if(!captchaVerified){
      log.info("error verifying recaptcha operation[requestMailValidation] email[{}]", emailAddress);
      throw new BadRequestException("Errore verifica recaptcha");
    }
    return this.requestMailValidationLoggedIn(emailAddress, codIpaEnte);
  }

  @PostMapping(AUTHENTICATED_PATH + "/request")
  public ResponseEntity<MailValidationRequest> requestMailValidationLoggedIn(@RequestParam String emailAddress,
                                                                             @RequestParam(required = false) String codIpaEnte) {
    if(emailAddress!=null && !Utilities.isValidEmail(emailAddress)){
      throw new BadRequestException("invalid email address");
    }
    String nomeEnte = null;
    if(StringUtils.isNotBlank(codIpaEnte))
      nomeEnte = Optional.ofNullable(enteService.getEnteByCodIpa(codIpaEnte))
          .map(Ente::getDeNomeEnte)
          .orElseThrow(NotFoundException::new);

    MailValidationRequest mailValidationRequest = mailValidationService.requestMailValidation(emailAddress, nomeEnte);
    return ResponseEntity.ok(mailValidationRequest);
  }

  @PostMapping(ANONYMOUS_PATH + "/verify")
  public ResponseEntity<MailValidationResponse> verifyMailValidationAnonymous(@RequestBody MailValidationRequest mailValidationRequest,
                                                @RequestParam String recaptcha) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"verifyMailValidation");
    if(!captchaVerified){
      log.info("error verifying recaptcha operation[verifyMailValidation] email[{}]", Utilities.ifNotNull(mailValidationRequest, MailValidationRequest::getEmail));
      throw new BadRequestException("Errore verifica recaptcha");
    }
    return this.verifyMailValidationLoggedIn(mailValidationRequest);
  }

  @PostMapping(AUTHENTICATED_PATH + "/verify")
  public ResponseEntity<MailValidationResponse> verifyMailValidationLoggedIn(@RequestBody MailValidationRequest mailValidationRequest) {
    MailValidationResponse mailValidationResponse = mailValidationService.verifyMailValidation(mailValidationRequest);
    return ResponseEntity.ok(mailValidationResponse);
  }
}
