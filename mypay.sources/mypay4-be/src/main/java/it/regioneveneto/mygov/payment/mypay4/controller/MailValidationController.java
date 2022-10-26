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
import it.regioneveneto.mygov.payment.mypay4.service.MailValidationService;
import it.regioneveneto.mygov.payment.mypay4.service.RecaptchaService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Validazione mail", description = "Gestione della validazione dell'indirizzo email (utenti non autenticati)")
@SecurityRequirements
@RestController
@RequestMapping(MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/mailvalidation")
@Slf4j
@ConditionalOnWebApplication
public class MailValidationController {

  @Autowired
  private RecaptchaService recaptchaService;

  @Autowired
  private MailValidationService mailValidationService;

  @PostMapping("request")
  public ResponseEntity<MailValidationRequest> requestMailValidation(@RequestParam String emailAddress, @RequestParam String recaptcha) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"requestMailValidation");
    if(!captchaVerified){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
          MailValidationRequest.builder().error("Errore verifica recaptcha").build());
    }
    if(emailAddress!=null && !Utilities.isValidEmail(emailAddress)){
      throw new BadRequestException("invalid email address");
    }
    MailValidationRequest mailValidationRequest = mailValidationService.requestMailValidation(emailAddress);
    return ResponseEntity.ok(mailValidationRequest);
  }

  @PostMapping("verify")
  public ResponseEntity<MailValidationResponse> verifyMailValidation(@RequestBody MailValidationRequest mailValidationRequest,
                                                @RequestParam String recaptcha) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"verifyMailValidation");
    if(!captchaVerified){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
          MailValidationResponse.builder().validationStatus(MailValidationService.MAIL_VALIDATION_STATUS_CAPTCHA).build());
    }
    MailValidationResponse mailValidationResponse = mailValidationService.verifyMailValidation(mailValidationRequest);
    return ResponseEntity.ok(mailValidationResponse);
  }
}
