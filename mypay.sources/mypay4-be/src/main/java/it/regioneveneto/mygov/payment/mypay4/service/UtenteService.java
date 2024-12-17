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

import io.jsonwebtoken.Claims;
import it.regioneveneto.mygov.payment.mypay4.dao.UtenteDao;
import it.regioneveneto.mygov.payment.mypay4.dto.ComuneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.NazioneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.ProvinciaTo;
import it.regioneveneto.mygov.payment.mypay4.dto.UtenteTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.MaxResultsHelper;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.veneto.regione.pagamenti.ente.FaultBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class UtenteService {

  @Resource
  private UtenteService self;

  @Autowired
  private UtenteDao utenteDao;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private LocationService locationService;

  @Autowired
  private MaxResultsHelper maxResultsHelper;

  @Cacheable(value=CacheService.CACHE_NAME_UTENTE, key="{'codFedUserId',#codFedUserId}", unless="#result==null")
  public Optional<Utente> getByCodFedUserId(String codFedUserId) {
    return utenteDao.getByCodFedUserId(codFedUserId);
  }

  public List<Utente> getAll() {
    return utenteDao.getAll();
  }

  @CacheEvict(value=CacheService.CACHE_NAME_UTENTE,key="{'codFedUserId',#codFedUserId}")
  public void clearCacheByCodFedUserId(String codFedUserId){}

  // since this is only used for user administration (backoffice), it is better not to use cache
  //@Cacheable(value=CacheService.CACHE_NAME_UTENTE, key="{'id',#id}", unless="#result==null")
  public Optional<Utente> getById(Long id) {
    return utenteDao.getById(id);
  }


  public static UtenteTo mapUtenteToDto(Utente utente){
    return mapUtenteToDtoMainFields(utente).toBuilder()
        .indirizzo(utente.getIndirizzo())
        .civico(utente.getCivico())
        .cap(utente.getCap())
        .nazioneId(utente.getNazioneId())
        .provinciaId(utente.getProvinciaId())
        .comuneId(utente.getComuneId())
        .build();
  }

  public static UtenteTo mapUtenteToDtoMainFields(Utente utente){
    return completeMapUtenteToDtoMainFields(utente, UtenteTo::new);
  }

  public static <T extends UtenteTo> T completeMapUtenteToDtoMainFields(Utente utente, Supplier<T> supplier){
    return (T) supplier.get().toBuilder()
        .userId(utente.getMygovUtenteId())
        .username(utente.getCodFedUserId())
        .codiceFiscale(utente.getCodCodiceFiscaleUtente())
        .nome(utente.getDeFirstname())
        .cognome(utente.getDeLastname())
        .email(utente.getDeEmailAddress())
        .emailNew(utente.getDeEmailAddressNew())
        .emailSourceType(utente.getEmailSourceType())
        .lastLogin(Utilities.toLocalDateTime(utente.getDtUltimoLogin()))
        .build();
  }

  public Utente mapLoginClaimsToUtente(Claims claims){
    Optional<String> nomeNazione = Optional.ofNullable(claims.get(JwtTokenUtil.JWT_CLAIM_NAZIONE_RESIDENZA, String.class)).filter(StringUtils::isNotBlank).map(String::trim);
    Optional<NazioneTo> nazione = nomeNazione.map(locationService::getNazioneByCodIso);
    Optional<ProvinciaTo> provincia;
    Optional<ComuneTo> comune = Optional.empty();
    if(nazione.map(NazioneTo::hasProvince).orElse(false)){
      provincia = Optional.ofNullable(claims.get(JwtTokenUtil.JWT_CLAIM_PROV_RESIDENZA, String.class))
          .filter(StringUtils::isNotBlank).map(String::trim).map(locationService::getProvinciaBySigla);
      if(provincia.isPresent())
        comune = Optional.ofNullable(claims.get(JwtTokenUtil.JWT_CLAIM_COMUNE_RESIDENZA, String.class)).filter(StringUtils::isNotBlank).map(String::trim)
            .map(nomeComune -> ComuneTo.builder().comune(nomeComune).build())
            .map(c -> locationService.getComuneByNameAndSiglaProvincia(c.getComune(), provincia.get().getSigla()).orElse(c));
    } else
      provincia = Optional.empty();

    return Utente.builder()
        .codFedUserId(claims.getSubject())
        .deEmailAddress(claims.get(JwtTokenUtil.JWT_CLAIM_EMAIL, String.class))
        .deEmailAddressNew(claims.get(JwtTokenUtil.JWT_CLAIM_EMAIL_NEW, String.class))
        .emailSourceType(Utente.EMAIL_SOURCE_TYPES.AUTH_SYSTEM.asChar())
        .deFirstname(claims.get(JwtTokenUtil.JWT_CLAIM_NOME, String.class))
        .deLastname(claims.get(JwtTokenUtil.JWT_CLAIM_COGNOME, String.class))
        .codCodiceFiscaleUtente(claims.get(JwtTokenUtil.JWT_CLAIM_CODICE_FISCALE, String.class))
        .deFedLegalEntity(StringUtils.firstNonBlank(claims.get(JwtTokenUtil.JWT_CLAIM_LEGAL_ENTITY, String.class),"fisica"))
        .flgFedAuthorized(ObjectUtils.firstNonNull(claims.get(JwtTokenUtil.JWT_CLAIM_FED_AUTH, Boolean.class), Boolean.TRUE))
        .nazioneId(nazione.map(NazioneTo::getNazioneId).orElse(null))
        .provinciaId(provincia.map(ProvinciaTo::getProvinciaId).orElse(null))
        .comuneId(comune.map(ComuneTo::getComuneId).orElse(null))
        .cap(claims.get(JwtTokenUtil.JWT_CLAIM_CAP_RESIDENZA, String.class))
        .indirizzo(claims.get(JwtTokenUtil.JWT_CLAIM_INDIRIZZO_RESIDENZA, String.class))
        .civico(claims.get(JwtTokenUtil.JWT_CLAIM_CIVICO_RESIDENZA, String.class))
        .build();
  }

/*  public Utente mapUserWithAdditionalInfoToUtente(UserWithAdditionalInfo user){
    String nomeNazione = user.getNazione();
    Nazione nazione=null;
    if(StringUtils.isNotBlank(nomeNazione))
      nazione = locationService.getNazioneByName(nomeNazione.trim());
    String siglaProvincia = user.getProvincia();
    Provincia provincia=null;
    if(StringUtils.isNotBlank(siglaProvincia))
      provincia = locationService.getProvinciaBySigla(siglaProvincia.trim());
    String nomeComune = user.getComune();
    ComuneTo comune=null;
    if(StringUtils.isNotBlank(nomeNazione))
      comune = locationService.getComuneByName(nomeComune.trim());
    return Utente.builder()
        .codFedUserId(user.getUsername())
        .deEmailAddress(user.getEmail())
        .deEmailAddressNew(user.getEmailNew())
        .emailSourceType(Utente.EMAIL_SOURCE_AUTH_SYSTEM)
        .deFirstname(user.getFirstName())
        .deLastname(user.getFamilyName())
        .codCodiceFiscaleUtente(user.getCodiceFiscale())
        .deFedLegalEntity(user.getLegalEntity())
        .flgFedAuthorized(user.isFederaAuthorized())
        .nazioneId(nazione!=null?nazione.getNazioneId():null)
        .provinciaId(provincia!=null?provincia.getProvinciaId():null)
        .comuneId(comune!=null?comune.getComuneId():null)
        .cap(user.getCap())
        .indirizzo(user.getIndirizzo())
        .civico(user.getCivico())
        .build();
  }*/

  public Map<String, Object> mapUserToLoginClaims(UserWithAdditionalInfo userDetails){
    Map<String, Object> claims = new HashMap<>();
    claims.put(JwtTokenUtil.JWT_CLAIM_COGNOME,userDetails.getFamilyName());
    claims.put(JwtTokenUtil.JWT_CLAIM_NOME,userDetails.getFirstName());
    claims.put(JwtTokenUtil.JWT_CLAIM_CODICE_FISCALE,userDetails.getCodiceFiscale());
    claims.put(JwtTokenUtil.JWT_CLAIM_EMAIL,userDetails.getEmail());
    claims.put(JwtTokenUtil.JWT_CLAIM_EMAIL_NEW,userDetails.getEmailNew());
    claims.put(JwtTokenUtil.JWT_CLAIM_EMAIL_SOURCE_TYPE,String.valueOf(userDetails.getEmailSourceType()));
    claims.put(JwtTokenUtil.JWT_CLAIM_LEGAL_ENTITY,userDetails.getLegalEntity());
    claims.put(JwtTokenUtil.JWT_CLAIM_FED_AUTH,userDetails.isFederaAuthorized());
    claims.put(JwtTokenUtil.JWT_CLAIM_NAZIONE_RESIDENZA, userDetails.getNazione());
    claims.put(JwtTokenUtil.JWT_CLAIM_PROV_RESIDENZA, userDetails.getProvincia());
    claims.put(JwtTokenUtil.JWT_CLAIM_COMUNE_RESIDENZA, userDetails.getComune());
    claims.put(JwtTokenUtil.JWT_CLAIM_CAP_RESIDENZA, userDetails.getCap());
    claims.put(JwtTokenUtil.JWT_CLAIM_INDIRIZZO_RESIDENZA, userDetails.getIndirizzo());
    claims.put(JwtTokenUtil.JWT_CLAIM_CIVICO_RESIDENZA, userDetails.getCivico());

    claims.put(JwtTokenUtil.JWT_CLAIM_NAZIONE_NASCITA, userDetails.getStatoNascita());
    claims.put(JwtTokenUtil.JWT_CLAIM_PROV_NASCITA, userDetails.getProvinciaNascita());
    claims.put(JwtTokenUtil.JWT_CLAIM_COMUNE_NASCITA, userDetails.getComuneNascita());
    claims.put(JwtTokenUtil.JWT_CLAIM_DATA_NASCITA, userDetails.getDataNascita());
    claims.put(JwtTokenUtil.JWT_CLAIM_AUTH_AUTHORITY, userDetails.getAuthenticatingAuthority());
    claims.put(JwtTokenUtil.JWT_CLAIM_AUTH_METHOD, userDetails.getAuthenticationMethod());
    return claims;
  }

  public void markNewEmailUserValidated(Long mygovUtenteId){
    Utente utenteOnDb = utenteDao.getById(mygovUtenteId).orElseThrow(NotFoundException::new);
    if(StringUtils.isBlank(utenteOnDb.getDeEmailAddressNew()))
      throw new MyPayException("no new mail to mark as validated for user "+mygovUtenteId);
    utenteOnDb.setEmailSourceType(Utente.EMAIL_SOURCE_TYPES.USER_VALIDATED.asChar());
    utenteOnDb.setDeEmailAddress(utenteOnDb.getDeEmailAddressNew());
    utenteOnDb.setDeEmailAddressNew(null);
    utenteDao.update(utenteOnDb);
    //clear invalid cache
    self.clearCacheByCodFedUserId(utenteOnDb.getCodFedUserId());
  }

  public void markEmailUserConfirmed(Long mygovUtenteId){
    Utente utenteOnDb = utenteDao.getById(mygovUtenteId).orElseThrow(NotFoundException::new);
    utenteOnDb.setEmailSourceType(Utente.EMAIL_SOURCE_TYPES.BACKOFFICE_CONFIRMED.asChar());
    utenteDao.update(utenteOnDb);
    //clear invalid cache
    self.clearCacheByCodFedUserId(utenteOnDb.getCodFedUserId());
  }

  public void updateEmailAddressNew(Long mygovUtenteId, String emailAddressNew){
    Utente utenteOnDb = utenteDao.getById(mygovUtenteId).orElseThrow(NotFoundException::new);
    utenteOnDb.setDeEmailAddressNew(emailAddressNew);
    utenteDao.update(utenteOnDb);
    //clear invalid cache
    self.clearCacheByCodFedUserId(utenteOnDb.getCodFedUserId());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Utente upsertUtente(Utente utente, boolean skipUserModifiedData){
    Optional<Utente> utenteOnDb = utenteDao.getByCodFedUserId(utente.getCodFedUserId());
    if(utenteOnDb.isEmpty()){
      log.info("adding new utente to DB: "+utente.getCodFedUserId()+" ("+utente.getDeFirstname()+" - "+utente.getDeLastname()+")");
      long newUtenteId = utenteDao.insert(utente);
      return utenteDao.getById(newUtenteId).orElseThrow(() -> new MyPayException("cant find utente with id "+newUtenteId));
    } else {
      Utente existingUtente = utenteOnDb.get();
      //if user has a validated email associated, it should never be replaced by the system
      boolean skipEmailUpdate = existingUtente.isChosenByUserEmail() && utente.getEmailSourceType()==Utente.EMAIL_SOURCE_TYPES.AUTH_SYSTEM.asChar()
          || StringUtils.isBlank(utente.getDeEmailAddress());
      boolean equals = Objects.equals(existingUtente.getCodCodiceFiscaleUtente(), utente.getCodCodiceFiscaleUtente())
          && (skipEmailUpdate || Objects.equals(existingUtente.getDeEmailAddress(), utente.getDeEmailAddress()))
          && Objects.equals(existingUtente.getDeFirstname(), utente.getDeFirstname())
          && Objects.equals(existingUtente.getDeLastname(), utente.getDeLastname())
          && existingUtente.isFlgFedAuthorized()==utente.isFlgFedAuthorized()
          && StringUtils.equals(existingUtente.getDeFedLegalEntity(), utente.getDeFedLegalEntity());

      boolean skipUserDataFields = skipUserModifiedData && existingUtente.getDtSetAddress()!=null;

      equals = equals && (skipUserDataFields ||
          Objects.equals(existingUtente.getCap(), utente.getCap())
              && Objects.equals(existingUtente.getCivico(), utente.getCivico())
              && Objects.equals(existingUtente.getIndirizzo(), utente.getIndirizzo())
              && Objects.equals(existingUtente.getComuneId(), utente.getComuneId())
              && Objects.equals(existingUtente.getProvinciaId(), utente.getProvinciaId())
              && Objects.equals(existingUtente.getNazioneId(), utente.getNazioneId()) );

      existingUtente.setCodCodiceFiscaleUtente(utente.getCodCodiceFiscaleUtente());
      if(!skipEmailUpdate) {
        existingUtente.setDeEmailAddress(utente.getDeEmailAddress());
        existingUtente.setEmailSourceType(utente.getEmailSourceType());
      }
      existingUtente.setDeFirstname(utente.getDeFirstname());
      existingUtente.setDeLastname(utente.getDeLastname());
      existingUtente.setDtUltimoLogin(utente.getDtUltimoLogin());
      existingUtente.setFlgFedAuthorized(utente.isFlgFedAuthorized());
      existingUtente.setDeFedLegalEntity(utente.getDeFedLegalEntity());
      if(!skipUserDataFields){
        existingUtente.setCap(utente.getCap());
        existingUtente.setCivico(utente.getCivico());
        existingUtente.setIndirizzo(utente.getIndirizzo());
        existingUtente.setComuneId(utente.getComuneId());
        existingUtente.setProvinciaId(utente.getProvinciaId());
        existingUtente.setNazioneId(utente.getNazioneId());
      }
      //increase version by 1 if at least a field is not equal (except last login time)
      // version passed by input object is ignored
      existingUtente.setVersion(existingUtente.getVersion()+(equals?0:1));
      //update utente
      utenteDao.update(existingUtente);

      //clear invalid cache
      self.clearCacheByCodFedUserId(existingUtente.getCodFedUserId());

      return existingUtente;
    }
  }

  public Optional<FaultBean> verificaWsUser(String codIpaEnte) {
    Optional<Utente> utente = utenteDao.getByCodFedUserId(codIpaEnte+ "-" + Constants.WS_USER);
    if (utente.isEmpty()) {
      log.error("Utente non presente: " + codIpaEnte + "-" + Constants.WS_USER);
      return Optional.of(VerificationUtils.getFaultBean(codIpaEnte, Constants.CODE_PAA_ENTE_NON_VALIDO,  "Utente WS_USER non censito per ente [" + codIpaEnte + "]", null));
    }
    return Optional.empty();
  }

  //
  // Backoffice functionalities
  //

  public List<UtenteTo> searchUtenti(String codFedUserId, String cognome, String nome, boolean onlyOperatori){
    return maxResultsHelper.manageMaxResults(
        maxResults -> utenteDao.searchUtenti(codFedUserId, cognome, nome, onlyOperatori, maxResults),
        UtenteService::mapUtenteToDtoMainFields,
        () -> utenteDao.searchUtentiCount(codFedUserId, cognome, nome, onlyOperatori) );
  }

  public List<UtenteTo> searchOperatoriForEnte(Long mygovEnteId, String codFedUserId, String cognome, String nome) {
    return utenteDao.searchOperatoriForEnte(mygovEnteId, codFedUserId, cognome, nome, null);
  }


  public List<UtenteTo> searchUtentiForEnteTipoDovuto(Long mygovEnteId, Long mygovEnteTipoDovutoId, String codFedUserId, String cognome, String nome, Boolean flgAssociato) {
    String codTipoDovuto = enteTipoDovutoService.getById(mygovEnteTipoDovutoId).getCodTipo();
    List<UtenteTo> allOperatoriForEnte = utenteDao.searchOperatoriForEnte(mygovEnteId, codFedUserId, cognome, nome, codTipoDovuto);
    List<Long> allOperatoriForTipo = utenteDao.searchUtentiForEnteTipoDovuto(mygovEnteTipoDovutoId, codFedUserId, cognome, nome)
        .stream().map(Utente::getMygovUtenteId).collect(Collectors.toList());
    return allOperatoriForEnte.stream()
        .peek(o-> o.setFlgAssociato(allOperatoriForTipo.contains(o.getUserId())))
        .filter(o -> flgAssociato == null || flgAssociato.equals(o.isFlgAssociato()))
        .collect(Collectors.toList());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateAnagraficaUtente(UtenteTo utenteTo){
    //find user on db
    Utente utente = utenteDao.getById(utenteTo.getUserId()).orElseThrow(NotFoundException::new);

    //update fields
    log.info("updating user: {} with values: {}", utente, utenteTo);
    utente.setDeFirstname(utenteTo.getNome());
    utente.setDeLastname(utenteTo.getCognome());
    utente.setDeEmailAddress(utenteTo.getEmail());
    utente.setEmailSourceType(Utente.EMAIL_SOURCE_TYPES.BACKOFFICE.asChar());
    utente.setCodCodiceFiscaleUtente(utenteTo.getCodiceFiscale());
    utenteDao.update(utente);

    //clear invalid cache
    self.clearCacheByCodFedUserId(utente.getCodFedUserId());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateIndirizzo(UtenteTo utenteTo){
    //find user on db
    Utente utente = utenteDao.getById(utenteTo.getUserId()).orElseThrow(NotFoundException::new);

    boolean equals = Objects.equals(utenteTo.getCap(), utente.getCap())
        && Objects.equals(utenteTo.getCivico(), utente.getCivico())
        && Objects.equals(utenteTo.getIndirizzo(), utente.getIndirizzo())
        && Objects.equals(utenteTo.getComuneId(), utente.getComuneId())
        && Objects.equals(utenteTo.getProvinciaId(), utente.getProvinciaId())
        && Objects.equals(utenteTo.getNazioneId(), utente.getNazioneId());

    if(equals)
      return;

    //update fields
    log.info("updating user: {} with values: {}", utente, utenteTo);
    utente.setNazioneId(utenteTo.getNazioneId());
    utente.setProvinciaId(utenteTo.getProvinciaId());
    utente.setComuneId(utenteTo.getComuneId());
    utente.setCap(utenteTo.getCap());
    utente.setIndirizzo(utenteTo.getIndirizzo());
    utente.setCivico(utenteTo.getCivico());
    utente.setDtSetAddress(new Date());
    utente.setVersion(utente.getVersion()+1);
    utenteDao.update(utente);

    //clear invalid cache
    self.clearCacheByCodFedUserId(utente.getCodFedUserId());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Optional<Long> insertDefaultWsUser(String codWsUser, String email) {
    Utente utente = Utente.builder()
        .codFedUserId(codWsUser)
        .codCodiceFiscaleUtente("-")
        .flgFedAuthorized(false)
        .deFirstname("Sistema").deLastname("Informativo").deFedLegalEntity("fisica")
        .deEmailAddress(email)
        .emailSourceType(Utente.EMAIL_SOURCE_TYPES.BACKOFFICE.asChar())
        .dtUltimoLogin(new Date())
        .build();
    Long newId = utenteDao.insert(utente);
    return Optional.ofNullable(newId);
  }
}
