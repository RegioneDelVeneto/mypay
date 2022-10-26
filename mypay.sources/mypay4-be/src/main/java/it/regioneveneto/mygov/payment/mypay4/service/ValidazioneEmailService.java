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

import it.regioneveneto.mygov.payment.mypay4.dao.ValidazioneEmailDao;
import it.regioneveneto.mygov.payment.mypay4.dto.ValidazioneEmailTo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.model.ValidazioneEmail;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class ValidazioneEmailService {

  @Value("${mailValidation.expireHours:24}")
  private Integer expireHours;

  @Value("${mailValidation.sendCode.maxTries:5}")
  private Integer maxTries;

  @Value("${mailValidation.sendCode.resendWaitMinutes:15}")
  private Integer resendWaitMinutes;


  @Resource
  private ValidazioneEmailService self;

  @Autowired
  private ValidazioneEmailDao validazioneEmailDao;

  @Autowired
  private MailService mailService;

  @Autowired
  private UtenteService utenteService;

  public Optional<ValidazioneEmail> get(Long mygovUtenteId) {
    return validazioneEmailDao.get(mygovUtenteId);
  }

  public Optional<ValidazioneEmail> getByUsername(String username) {
    return validazioneEmailDao.getByUsername(username);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Optional<ValidazioneEmailTo> checkEmailValidationStatus(String username){
    Utente utente = utenteService.getByCodFedUserId(username).orElseThrow(NotFoundException::new);
    return self.get(utente.getMygovUtenteId()).map(v -> {
      ValidazioneEmailTo dto = this.model2Dto(v);
      if(self.checkExpired(v))
        dto.setOutcome(ValidazioneEmailTo.OUTCOME.EXPIRED);
      return dto;
    });
  }

  private ValidazioneEmailTo model2Dto(ValidazioneEmail model){
    long waitSeconds = Math.max(0, resendWaitMinutes*60 - (Instant.now().getEpochSecond() - model.getDtUltimoInvio().toInstant().getEpochSecond()));
    return ValidazioneEmailTo.builder()
        .emailAddress(model.getMygovUtenteId().getDeEmailAddressNew())
        .expireDt(model.getDtPrimoInvio().toLocalDateTime().plus(expireHours, ChronoUnit.HOURS))
        .cantRequestNewMailBeforeSeconds((int) Math.min(resendWaitMinutes*60, waitSeconds))
        .remainingTries(maxTries - model.getNumTentativi())
        .build();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void initiateEmailValidationProcess(String username, String emailAddress){
    if(!Utilities.isValidEmail(emailAddress)){
      log.error("invalid email [{}], user [{}]", emailAddress, username);
      throw new BadRequestException("invalid email");
    }

    Utente utente = utenteService.getByCodFedUserId(username).orElseThrow(NotFoundException::new);

    // check if another validation not expired
    self.get(utente.getMygovUtenteId()).ifPresent(v -> {
      if(!self.checkExpired(v)){
        log.info("validation procedure already existing for email [{}] and not expired, cannot initiate a new one", ToStringBuilder.reflectionToString(v));
        throw new BadRequestException();
      }
    });

    //update user
    utenteService.updateEmailAddressNew(utente.getMygovUtenteId(), emailAddress);

    //insert validazioneEmail
    ValidazioneEmail v = ValidazioneEmail.builder()
        .mygovUtenteId(utente)
        .codice(RandomStringUtils.random(6, false, true))
        .build();
    validazioneEmailDao.insert(v);

    //send mail
    mailService.sendUserMailValidationMail(emailAddress, v.getCodice());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void resetEmailValidationProcess(String username){
    Utente utente = utenteService.getByCodFedUserId(username).orElseThrow(NotFoundException::new);

    //update user
    utenteService.updateEmailAddressNew(utente.getMygovUtenteId(), null);
    //delete validazioneEmail
    validazioneEmailDao.delete(utente.getMygovUtenteId());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public boolean checkExpired(ValidazioneEmail v){
    if(v.getDtPrimoInvio().toInstant().plus(expireHours, ChronoUnit.HOURS).isBefore(Instant.now())) {
      utenteService.updateEmailAddressNew(v.getMygovUtenteId().getMygovUtenteId(), null);
      validazioneEmailDao.delete(v.getMygovUtenteId().getMygovUtenteId());
      log.warn("validation expired for user: {} - email: {} - {}", v.getMygovUtenteId().getMygovUtenteId(),
          v.getMygovUtenteId().getDeEmailAddressNew(), v.getDtPrimoInvio().toInstant().plus(expireHours, ChronoUnit.HOURS));
      return true;
    }
    return false;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public ValidazioneEmailTo verifyEmailValidationCode(String username, String code){
    final Instant now = Instant.now();
    //verify validation exists
    ValidazioneEmail v = self.getByUsername(username).orElseThrow(NotFoundException::new);
    //verify logged user has an ongoing validation of this email address
    if(!v.getMygovUtenteId().isEmailValidationOngoing()){
      log.warn("user {} has not an ongoing validation process", username);
      throw new BadRequestException();
    }

    ValidazioneEmailTo dto;
    ValidazioneEmailTo.OUTCOME outcome=null;
    if(self.checkExpired(v)) {
      //reload after updating expired status
      outcome = ValidazioneEmailTo.OUTCOME.EXPIRED;
    } else if(!StringUtils.equals(v.getCodice(), code)) {
      v.setNumTentativi(v.getNumTentativi()+1);
      v.setDtUltimoTentativo(Timestamp.from(now));
      if(v.getNumTentativi() >= maxTries){
        log.warn("wrong code and max tries reached by user: {} for validation of email: {} - {}",
            v.getMygovUtenteId().getCodFedUserId(), v.getMygovUtenteId().getDeEmailAddressNew(), code);
        utenteService.updateEmailAddressNew(v.getMygovUtenteId().getMygovUtenteId(), null);
        validazioneEmailDao.delete(v.getMygovUtenteId().getMygovUtenteId());
        outcome = ValidazioneEmailTo.OUTCOME.MAX_TRIES;
      } else {
        log.warn("wrong code by user: {} for validation of email: {} try: {} - {}",
            v.getMygovUtenteId().getCodFedUserId(), v.getMygovUtenteId().getDeEmailAddressNew(), v.getNumTentativi(), code);
        outcome = ValidazioneEmailTo.OUTCOME.WRONG_CODE;
        validazioneEmailDao.update(v);
      }
    } else {
      //validation ok
      log.info("email validated {} for user {}", v.getMygovUtenteId().getDeEmailAddressNew(), username);
      utenteService.markNewEmailUserValidated(v.getMygovUtenteId().getMygovUtenteId());
      validazioneEmailDao.delete(v.getMygovUtenteId().getMygovUtenteId());
      outcome = ValidazioneEmailTo.OUTCOME.OK;
    }
    dto = this.model2Dto(v);
    dto.setOutcome(outcome);
    return dto;
  }


//  @Transactional(propagation = Propagation.REQUIRED)
//  public void insert(String emailAddress) {
//    validazioneEmailDao.insert(validazioneEmail);
//    self.clearCacheByEmail(validazioneEmail.getDeEmailAddress());
//  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void update(ValidazioneEmail validazioneEmail) {
    validazioneEmailDao.update(validazioneEmail);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateSourceTypeFromBackofficeToUserConfirmed(String username) {
    utenteService.getByCodFedUserId(username)
        .filter(u -> !u.isEmailValidationOngoing() && u.getEmailSourceType()==Utente.EMAIL_SOURCE_TYPES.BACKOFFICE.asChar())
        .map(Utente::getMygovUtenteId)
        .ifPresentOrElse(utenteService::markEmailUserConfirmed,()->{throw new NotFoundException();});
  }

}
