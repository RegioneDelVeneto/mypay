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

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.ValidazioneEmailTo;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.security.JwtRequestFilter;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.ValidazioneEmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Tag(name = "Validazione email", description = "Gestione della validazione dell'email utente")
@SecurityRequirements
@RestController
@RequestMapping(MyPay4AbstractSecurityConfig.PATH_EMAIL_VALIDATION)
@Slf4j
@ConditionalOnWebApplication
public class ValidazioneEmailController {

  @Autowired
  ValidazioneEmailService validazioneEmailService;

  @GetMapping()
  public ValidazioneEmailTo checkEmailValidationStatus(@AuthenticationPrincipal UserWithAdditionalInfo user){
    return validazioneEmailService.checkEmailValidationStatus(user.getUsername())
        .orElse(null);
  }

  @PostMapping("init")
  public ValidazioneEmailTo initValidation(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                       @RequestParam String emailAddress, @RequestParam(required = false) String codIpaEnte){
    validazioneEmailService.initiateEmailValidationProcess(user.getUsername(), emailAddress, codIpaEnte);
    return this.checkEmailValidationStatus(user);
  }

  @PostMapping("validate")
  public ValidazioneEmailTo sendCode(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                     HttpServletRequest request,
                                     @RequestParam String code){
    ValidazioneEmailTo dto = validazioneEmailService.verifyEmailValidationCode(user.getUsername(), code);
    if(dto!=null && dto.getOutcome()==ValidazioneEmailTo.OUTCOME.OK){
      //must regenerate token since now user validated email
      JwtRequestFilter.updateMailOnToken(request, dto.getEmailAddress(), Utente.EMAIL_SOURCE_TYPES.USER_VALIDATED.asChar());
    }
    return dto;
  }

  @PostMapping("reset")
  public void resetValidation(@AuthenticationPrincipal UserWithAdditionalInfo user){
    validazioneEmailService.resetEmailValidationProcess(user.getUsername());
  }

  @PostMapping("confirmBackoffice")
  public void updateSourceTypeFromBackofficeToUserConfirmed(@AuthenticationPrincipal UserWithAdditionalInfo user){
    validazioneEmailService.updateSourceTypeFromBackofficeToUserConfirmed(user.getUsername());
  }
}
