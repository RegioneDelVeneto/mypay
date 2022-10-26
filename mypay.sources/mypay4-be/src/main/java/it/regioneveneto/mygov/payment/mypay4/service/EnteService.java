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

import it.regioneveneto.mygov.payment.mypay4.dao.EnteDao;
import it.regioneveneto.mygov.payment.mypay4.dao.EnteTipoDovutoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaEnteTo;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.service.common.ThumbnailService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.veneto.regione.pagamenti.ente.FaultBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class EnteService {

  @Resource
  private EnteService self;

  @Autowired
  private EnteDao enteDao;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private EnteFunzionalitaService enteFunzionalitaService;

  @Autowired
  private RegistroOperazioneService registroOperazioneService;

  @Autowired
  private EnteTipoDovutoDao enteTipoDovutoDao;

  @Autowired
  private TassonomiaService tassonomiaService;

  @Autowired
  private LocationService locationService;

  @Autowired
  private FlussoService flussoService;

  @Autowired
  private UtenteService utenteService;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private BackOfficeService backOfficeService;

  @Autowired
  ThumbnailService thumbnailService;

  @Value("${pa.logoDefault:}")
  private String paLogoDefault;

  @Value("${pa.identificativoIntermediarioPAPassword}")
  private String paIdentificativoIntermediarioPAPassword;

  @CacheEvict(value=CacheService.CACHE_NAME_ENTE,key="{'id',#id}")
  public void clearCacheById(Long id){}

  public EnteTo mapEnteToDtoWithoutLogo(Ente ente) {
    return ente == null ? null : EnteTo.builder()
        .mygovEnteId(ente.getMygovEnteId())
        .codIpaEnte(ente.getCodIpaEnte())
        .codiceFiscaleEnte(ente.getCodiceFiscaleEnte())
        .deNomeEnte(ente.getDeNomeEnte())
        .build();
  }

  public EnteTo mapEnteToDtoWithThumbnail(Ente ente) {
    if(ente==null)
      return null;
    EnteTo enteTo = mapEnteToDtoWithoutLogo(ente);
    try {
      thumbnailService.generateThumbnail(ente.getDeLogoEnte()).ifPresent( thumbLogoEnte -> {
        enteTo.setThumbLogoEnte(thumbLogoEnte.getContent());
        enteTo.setHashThumbLogoEnte(thumbLogoEnte.getHash());
      });
    } catch(Exception e){
      throw new MyPayException("invalid logo for ente: "+ente.getCodIpaEnte(), e);
    }
    return enteTo;
  }

  public EnteTo mapEnteToDtoWithThumbnailHash(Ente ente) {
    if(ente==null)
      return null;
    EnteTo enteTo = mapEnteToDtoWithoutLogo(ente);
    try {
      thumbnailService.getThumbnailHash(ente.getDeLogoEnte()).ifPresent(enteTo::setHashThumbLogoEnte);
    } catch(Exception e){
      throw new MyPayException("invalid logo for ente: "+ente.getCodIpaEnte(), e);
    }
    return enteTo;
  }

  public EnteTo mapEnteToDtoWithBigLogo(Ente ente) {
    if(ente==null)
      return null;
    EnteTo enteTo = mapEnteToDtoWithThumbnail(ente);
    enteTo.setDeLogoEnte(ente.getDeLogoEnte());
    enteTo.setDeInformazioniEnte(ente.getDeInformazioniEnte());
    return enteTo;
  }

  public AnagraficaEnteTo mapAnagraficaEnteToDtoWithThumbnail(Ente ente) {
    if(ente==null)
      return null;
    AnagraficaEnteTo enteTo = mapAnagraficaEnteToDtoWithoutLogo(ente);
    try {
      thumbnailService.generateThumbnail(ente.getDeLogoEnte()).ifPresent( thumbLogoEnte -> {
        enteTo.setThumbLogoEnte(thumbLogoEnte.getContent());
        enteTo.setHashThumbLogoEnte(thumbLogoEnte.getHash());
      });
    } catch(Exception e){
      throw new MyPayException("invalid logo for ente: "+ente.getCodIpaEnte(), e);
    }
    return enteTo;
  }

  public AnagraficaEnteTo mapAnagraficaEnteToDtoWithThumbnailHash(Ente ente) {
    if(ente==null)
      return null;
    AnagraficaEnteTo enteTo = mapAnagraficaEnteToDtoWithoutLogo(ente);
    try {
      thumbnailService.getThumbnailHash(ente.getDeLogoEnte()).ifPresent(enteTo::setHashThumbLogoEnte);
    } catch(Exception e){
      throw new MyPayException("invalid logo for ente: "+ente.getCodIpaEnte(), e);
    }
    return enteTo;
  }

  public AnagraficaEnteTo mapAnagraficaEnteToDtoWithoutLogo(Ente ente) {
    return ente == null ? null : AnagraficaEnteTo.builder()
        .mygovEnteId(ente.getMygovEnteId())
        .codIpaEnte(ente.getCodIpaEnte())
        .codiceFiscaleEnte(ente.getCodiceFiscaleEnte())
        .deNomeEnte(ente.getDeNomeEnte())
        .cdStatoEnte(Optional.of(ente.getCdStatoEnte())
            .map(stato -> AnagraficaStatoService.mapToDto(stato))
            .orElse(null))
        .dtAvvio(ente.getDtAvvio())
        .build();
  }

  @LogExecution(enabled = LogExecution.ParamMode.OFF)
  @Cacheable(value=CacheService.CACHE_NAME_ENTE, key="{'all'}")
  public List<Ente> getAllEnti() {
    return enteDao.getAllEnti();
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE, key="{'all-spontanei'}")
  public List<Ente> getAllEntiSpontanei() {
    return enteDao.getAllEntiSpontanei();
  }

  @Caching(
      evict = {
          @CacheEvict(value=CacheService.CACHE_NAME_ENTE,key="{'all'}"),
          @CacheEvict(value=CacheService.CACHE_NAME_ENTE,key="{'all-spontanei'}")
      }
  )
  public void clearCacheAllEnti(){}

  public void verifyEnteIsPublicAndActive(Long mygovEnteId){
    self.verifyEnteIsPublicAndActive(self.getEnteById(mygovEnteId));
  }
  public void verifyEnteIsPublicAndActive(String codIpaEnte){
    self.verifyEnteIsPublicAndActive(self.getEnteByCodIpa(codIpaEnte));
  }

  public void verifyEnteIsPublicAndActive(Ente ente){
    if( ente==null || !ente.getCdStatoEnte().getCodStato().equals(Constants.STATO_ENTE_ESERCIZIO) ||
        !enteFunzionalitaService.isActiveByFunzionalitaAndCodIpaEnte(ente.getCodIpaEnte(), Constants.FUNZIONALITA_ENTE_PUBBLICO) ) {
      log.warn("trying to operate on ente that is not existing, not public, or not in state esercizio");
      throw new MyPayException("invalid ente");
    }
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE, key="{'id',#id}", unless="#result==null")
  @LogExecution(params = LogExecution.ParamMode.ON, returns = LogExecution.ParamMode.ON)  //TODO to remove: added just as a configuration example
  public Ente getEnteById(Long id) {
    return enteDao.getEnteById(id);
  }

  public AnagraficaEnteTo getAnagraficaTo(Ente ente, boolean isSysAdmin) {
    AnagraficaEnteTo anagrafica = new AnagraficaEnteTo(ente);
    if(isSysAdmin){
      anagrafica.setPaaSILInviaCarrelloDovutiHash(DigestUtils.sha256Hex(
          String.format("paaSILInviaCarrelloDovuti-%s-%s", paIdentificativoIntermediarioPAPassword, ente.getCodiceFiscaleEnte())));
    } else {
      anagrafica.setPaaSILInviaCarrelloDovutiHash(null);
      anagrafica.setDePassword(null);
    }
    return anagrafica;
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE, key="{'codIpa',#codIpa}", unless="#result==null")
  public Ente getEnteByCodIpa(String codIpa) {
    return enteDao.getEnteByCodIpa(codIpa);
  }
  @Cacheable(value=CacheService.CACHE_NAME_ENTE, key="{'codFiscale',#codFiscale}", unless="#result==null")
  public Ente getEnteByCodFiscale(String codFiscale) {
    return enteDao.getEnteByCodFiscale(codFiscale);
  }

  public List<Ente> getEntiByOperatoreUsername(String operatoreUsername) {
    return enteDao.getEntiByOperatoreUsername(operatoreUsername);
  }

  public List<Ente> searchEnti(String codIpaEnte, String deNome,String codFiscale,Long idStato,LocalDate dtAvvioFrom,LocalDate dtAvvioTo) {
    return enteDao.searchEnti(codIpaEnte, deNome, codFiscale, idStato, dtAvvioFrom, dtAvvioTo);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AnagraficaEnteTo insertEnte(UserWithAdditionalInfo user, AnagraficaEnteTo anagraficaEnteTo) {
    if (self.getEnteByCodIpa(anagraficaEnteTo.getCodIpaEnte()) != null)
      throw new ValidatorException(messageSource.getMessage("pa.manager.error.ente.presente", null, Locale.ITALY));
    validatePayload(anagraficaEnteTo, user);
    Ente enteIncomePayload = this.dtoToModel(anagraficaEnteTo, null, user);
    AnagraficaStato statoEnte = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_ENTE_INSERITO, Constants.STATO_TIPO_ENTE);
    enteIncomePayload.setCdStatoEnte(statoEnte);
    String myboxClientKey = UUID.randomUUID().toString();
    String myboxClientSecret = UUID.randomUUID().toString();
    String dePassword = RandomStringUtils.randomAlphanumeric(12).toUpperCase();
    enteIncomePayload.setMyboxClientKey(myboxClientKey);
    enteIncomePayload.setMyboxClientSecret(myboxClientSecret);
    enteIncomePayload.setDePassword(dePassword);
    enteIncomePayload.setCodRpDatiVersTipoVersamento(Constants.TIPO_VERSAMENTO.TUTTI.getValue());
    enteIncomePayload.setCodIpaEnte(enteIncomePayload.getCodIpaEnte().toUpperCase());
    long mygovEnteId = enteDao.insert(enteIncomePayload);
    Ente enteInserito = self.getEnteById(mygovEnteId);

    for(String funzionalita: Constants.ENTI_ALL_FUNZIONALITA){
      boolean stato = funzionalita.equals(Constants.FUNZIONALITA_PAGAMENTO_SPONTANEO);
      Optional<Long> optionalLong = enteFunzionalitaService.insert(funzionalita, enteIncomePayload.getCodIpaEnte(), stato);
      if (optionalLong.isPresent())
        registroOperazioneService.insert(user, RegistroOperazione.TipoOperazione.ENTE_FUNZ, enteIncomePayload.getCodIpaEnte()+'|'+funzionalita, stato);
    }

    flussoService.insertFlussiDefault(enteInserito);

    String codWsUser = enteInserito.getCodIpaEnte() + "-WS_USER";
    if(utenteService.getByCodFedUserId(codWsUser).isPresent())
      throw new MyPayException("Flusso already present in database");
    utenteService.insertDefaultWsUser(codWsUser, enteIncomePayload.getEmailAmministratore());

    enteTipoDovutoService.insertDefaultSet(enteInserito, anagraficaEnteTo.isFlagInsertDefaultSet());
    self.clearCacheAllEnti();
    return getAnagraficaTo(enteInserito, user.isSysAdmin());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AnagraficaEnteTo updateEnte(UserWithAdditionalInfo user, AnagraficaEnteTo anagraficaEnteTo) {
    Ente enteOnDb = Optional.ofNullable(self.getEnteById(anagraficaEnteTo.getMygovEnteId())).orElseThrow(NotFoundException::new);
    validatePayload(anagraficaEnteTo, user);
    Ente enteIncomePayload = this.dtoToModel(anagraficaEnteTo, enteOnDb, user);
    String oldCodTipoEnte = enteOnDb.getCodTipoEnte();
    if (!anagraficaEnteTo.getCodTipoEnte().equals(oldCodTipoEnte)) {
      List<EnteTipoDovuto> enteTipoDovutos = enteTipoDovutoDao.getAllByEnte(enteIncomePayload.getMygovEnteId());
      enteTipoDovutos.stream().forEach( enteTipoDovuto -> {
        enteTipoDovuto.setMacroArea(null);
        enteTipoDovuto.setTipoServizio(null);
        enteTipoDovuto.setMotivoRiscossione(null);
        enteTipoDovuto.setCodTassonomico(null);
        enteTipoDovutoDao.update(enteTipoDovuto);
      });
      enteTipoDovutoService.clearCache();
      log.info(enteTipoDovutos.size() + " recs of EnteTipoDovuto updated.");
    }
    Optional.ofNullable(enteIncomePayload.getCodRpDatiVersTipoVersamento())
        .ifPresentOrElse(s -> {}, () -> enteIncomePayload.setCodRpDatiVersTipoVersamento(Constants.TIPO_VERSAMENTO.TUTTI.getValue()));
    enteDao.update(enteIncomePayload);
    self.clearCacheById(enteIncomePayload.getMygovEnteId());
    self.clearCacheAllEnti();
    Ente updatedEnte = self.getEnteById(enteIncomePayload.getMygovEnteId());
    backOfficeService.sendReportToSystemMaintainer(user, enteOnDb.getDeNomeEnte(), "Ente", self.getAnagraficaTo(enteOnDb, true), anagraficaEnteTo);
    return getAnagraficaTo(updatedEnte, user.isSysAdmin());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public EnteTo updateEnteLogo(Long mygovEnteId,  MultipartFile file) {
    try {
      Ente ente = enteDao.getEnteById(mygovEnteId);
      BufferedImage image = ImageIO.read(file.getInputStream());
      if (!Utilities.isValidImageDimensions(image, Constants.MAX_WIDTH_LOGO_ENTE, Constants.MAX_HEIGHT_LOGO_ENTE))
        throw new ValidatorException(messageSource.getMessage("pa.manager.error.validation.dimensioniLogo", null, Locale.ITALY));

      String imageString = Utilities.getBase64StringFromImage(image);
      if (StringUtils.isBlank(imageString))
        throw new ValidatorException(messageSource.getMessage("pa.manager.error.validation.generic", null, Locale.ITALY));

      ente.setDeLogoEnte(imageString);
      enteDao.updateLogoEnte(ente.getMygovEnteId(), ente.getDeLogoEnte());
      self.clearCacheById(ente.getMygovEnteId());
      self.clearCacheAllEnti();
      return this.mapEnteToDtoWithBigLogo(ente);
    } catch (ValidatorException e){
      throw e;
    } catch (Exception e){
      log.error(e.getMessage(), e);
      throw new MyPayException(messageSource.getMessage("pa.manager.error.validation.generic", null, Locale.ITALY));
    }
  }

  public Optional<FaultBean> verificaEnte(String codIpaEnte, String password) {
    return verificaEnte(codIpaEnte, password, true);
  }

  public Optional<FaultBean> verificaEnte(String codIpaEnte, String password, boolean verificaStato) {
    FaultBean faultBean = null;
    Ente ente = self.getEnteByCodIpa(codIpaEnte);
    if (ente == null) {
      String faultString = String.format("Codice IPA Ente [%s] non valido o password errata", codIpaEnte);
      log.error(faultString);
      faultBean = VerificationUtils.getFaultBean(codIpaEnte, Constants.CODE_PAA_ENTE_NON_VALIDO, faultString,null);
    }

    if (verificaStato) {
      boolean isStatoInserito = Utilities.checkIfStatoInserito(ente) ;
      if (isStatoInserito) {
        String faultString = String.format("Stato Ente non valido: %s", codIpaEnte);
        log.error(faultString);
        faultBean = VerificationUtils.getFaultBean(codIpaEnte, Constants.CODE_PAA_ENTE_NON_VALIDO, faultString,null);
      }
    }

    Boolean passwordValidaPerEnte = verificaPassword(codIpaEnte, password);
    if (!passwordValidaPerEnte) {
      String faultString = String.format("Password non valida per ente [%s]", codIpaEnte);
      log.error(faultString);
      faultBean = VerificationUtils.getFaultBean(codIpaEnte, Constants.CODE_PAA_ENTE_NON_VALIDO, faultString, null);
    }
    return Optional.ofNullable(faultBean);
  }


  public boolean verificaPassword(final String codIpaEnte, final String password) {
    List<Ente> entes = enteDao.findByCodIpaEnteAndNullPassword(codIpaEnte);
    if (entes.size() > 1) {
      throw new DataIntegrityViolationException("mypivot.ente.enteDuplicato");
    }

    //se la password in database e' NULL autorizzo
    if (entes.size() == 1)
      return true;

    if (password == null) {
      return false;
    }

    //altrimenti controllo che password in input corrisponda con password sul database
    entes = enteDao.findByCodIpaEnteAndPassword(codIpaEnte, password);

    if (entes.size() > 1) {
      throw new DataIntegrityViolationException("mypivot.ente.enteDuplicato");
    }
    return !entes.isEmpty();
  }

  public Long callGetEnteTipoProgressivoFunction(String codIpaEnte, String tipoGeneratore, Date data) {
    return enteDao.callGetEnteTipoProgressivoFunction(codIpaEnte, tipoGeneratore, data);
  }

  public String calculateInviaCarrelloDovutiHash(Ente ente) {
    String codiceFiscaleEnte = ente.getCodiceFiscaleEnte();
    StringBuilder sb = new StringBuilder();
    sb.append("paaSILInviaCarrelloDovuti-");
    sb.append(paIdentificativoIntermediarioPAPassword);
    sb.append("-");
    sb.append(codiceFiscaleEnte);
    String originalString = new String(sb);
    String sha256hex = DigestUtils.sha256Hex(originalString);
    return sha256hex;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public String checkInvalidLogo(boolean deleteLogo){
    return enteDao.getFullTable().stream().map(ente -> {
      try{
        if(StringUtils.isNotBlank(ente.getDeLogoEnte()))
          thumbnailService.generateThumbnail(ente.getDeLogoEnte());
        return null;
      }catch(Exception e){
        log.error("ente with id[{}] cod_ipa[{}] has invalid logo", ente.getMygovEnteId(), ente.getCodIpaEnte(), e);
        if(deleteLogo){
          log.warn("deleting logo for ente with id[{}] cod_ipa[{}]", ente.getMygovEnteId(), ente.getCodIpaEnte());
          enteDao.updateLogoEnte(ente.getMygovEnteId(), null);
          self.clearCacheById(ente.getMygovEnteId());
          self.clearCacheAllEnti();
        }
        return ente.getCodIpaEnte();
      }
    }).filter(StringUtils::isNotBlank)
      .collect(Collectors.joining(", ", "Clean logo for ente: ", ""));
  }

  private Ente dtoToModel(AnagraficaEnteTo anagrafica, Ente fromEnte, UserWithAdditionalInfo user) {
    Ente.EnteBuilder enteBuilder = Optional.ofNullable(fromEnte).map(e -> e.toBuilder()).orElse(Ente.builder());
    if(user.isSysAdmin()) {
      enteBuilder
        .codIpaEnte(anagrafica.getCodIpaEnte())
        .deNomeEnte(anagrafica.getDeNomeEnte())
        .cdStatoEnte(anagraficaStatoService.getById(anagrafica.getCdStatoEnte().getMygovAnagraficaStatoId()))
        .dtAvvio(anagrafica.getDtAvvio())
        .codTipoEnte(anagrafica.getCodTipoEnte())
        .codCodiceInterbancarioCbill(anagrafica.getCodCodiceInterbancarioCbill())
        .applicationCode(anagrafica.getApplicationCode())
        .codiceFiscaleEnte(anagrafica.getCodiceFiscaleEnte())
        .enteSilInviaRispostaPagamentoUrl(anagrafica.getEnteSilInviaRispostaPagamentoUrl())
        .deAutorizzazione(anagrafica.getDeAutorizzazione());
    }
    return enteBuilder
        .emailAmministratore(anagrafica.getEmailAmministratore())
        .codRpDatiVersDatiSingVersIbanAccredito(anagrafica.getCodRpDatiVersDatiSingVersIbanAccredito())
        .codRpDatiVersDatiSingVersBicAccredito(anagrafica.getCodRpDatiVersDatiSingVersIbanAccredito()
            .startsWith("07601", 5)? Constants.IDENTIFICATIVO_PSP_POSTE : null)
        .codRpDatiVersDatiSingVersBicAccreditoSeller(anagrafica.getCodRpDatiVersDatiSingVersBicAccreditoSeller())
        .deRpEnteBenefDenominazioneBeneficiario(anagrafica.getDeRpEnteBenefDenominazioneBeneficiario())
        .deRpEnteBenefIndirizzoBeneficiario(anagrafica.getDeRpEnteBenefIndirizzoBeneficiario())
        .deRpEnteBenefCivicoBeneficiario(anagrafica.getDeRpEnteBenefCivicoBeneficiario())
        .codRpEnteBenefCapBeneficiario(anagrafica.getCodRpEnteBenefCapBeneficiario())
        .deRpEnteBenefLocalitaBeneficiario(anagrafica.getDeRpEnteBenefLocalitaBeneficiario())
        .deRpEnteBenefProvinciaBeneficiario(anagrafica.getDeRpEnteBenefProvinciaBeneficiario())
        .codRpEnteBenefNazioneBeneficiario(anagrafica.getCodRpEnteBenefNazioneBeneficiario())
        .deRpEnteBenefTelefonoBeneficiario(anagrafica.getDeRpEnteBenefTelefonoBeneficiario())
        .deRpEnteBenefSitoWebBeneficiario(anagrafica.getDeRpEnteBenefSitoWebBeneficiario())
        .deRpEnteBenefEmailBeneficiario(anagrafica.getDeRpEnteBenefEmailBeneficiario())
        .deInformazioniEnte(anagrafica.getDeInformazioniEnte())
        .linguaAggiuntiva(anagrafica.getLinguaAggiuntiva())
        .build();
  }

  public Set<String> getCodIpaEntiForAdminEnte(UserWithAdditionalInfo user) {
    return user.getEntiRoles()
        .entrySet()
        .stream()
        .filter(e -> e.getValue().contains(Operatore.Role.ROLE_ADMIN.name()))
        .map(Map.Entry::getKey)
        .collect(toSet());
  }

  private void validatePayload(AnagraficaEnteTo entePayload, UserWithAdditionalInfo user) {
    if (StringUtils.isNotBlank(entePayload.getCodRpEnteBenefCapBeneficiario()) && !StringUtils.isNumeric(entePayload.getCodRpEnteBenefCapBeneficiario()))
      throw new ValidatorException("Cap Beneficiario è invalido");
    if(StringUtils.isNotBlank(entePayload.getCodRpEnteBenefNazioneBeneficiario()) && Optional.ofNullable(locationService.getNazioneByCodIso(entePayload.getCodRpEnteBenefNazioneBeneficiario())).isEmpty())
      throw new ValidatorException("Nazione Beneficiario è invalido");
    if(StringUtils.isNotBlank(entePayload.getDeRpEnteBenefTelefonoBeneficiario()) && !StringUtils.isNumeric(entePayload.getDeRpEnteBenefTelefonoBeneficiario()))
      throw new ValidatorException("Telefono Beneficiario è invalido");
    if(StringUtils.isNotBlank(entePayload.getDeRpEnteBenefSitoWebBeneficiario()) && !Utilities.isValidUrl(entePayload.getDeRpEnteBenefSitoWebBeneficiario()))
      throw new ValidatorException("SitoWeb Beneficiario è invalido");
    if (StringUtils.isNotBlank(entePayload.getCodRpDatiVersDatiSingVersIbanAccredito()) && !Utilities.isValidIban(entePayload.getCodRpDatiVersDatiSingVersIbanAccredito()))
      throw new ValidatorException("Iban Accredito è invalido");
    if (StringUtils.isBlank(entePayload.getEmailAmministratore()) || !Utilities.isValidEmail(entePayload.getEmailAmministratore()))
      throw new ValidatorException("E-mail Amministratore è invalido");
    if(user.isSysAdmin()) {
      if (StringUtils.isBlank(entePayload.getCodIpaEnte()))
        throw new ValidatorException("Codice IPA è campo obbligatorio");
      if (StringUtils.isBlank(entePayload.getDeNomeEnte()))
        throw new ValidatorException("Denominazione Ente è campo obbligatorio");
      if (StringUtils.isBlank(entePayload.getCodiceFiscaleEnte()) || !Utilities.isValidPIVA(entePayload.getCodiceFiscaleEnte()))
        throw new ValidatorException("Codice Fiscale Ente è invalido");
      if (entePayload.getCodRpDatiVersDatiSingVersBicAccreditoSeller() == null)
        throw new ValidatorException("Bic Accredito Seller è campo obbligatorio");
      if (StringUtils.isBlank(entePayload.getEmailAmministratore()) || !Utilities.isValidEmail(entePayload.getEmailAmministratore()))
        throw new ValidatorException("E-mail Amministratore è invalido");
      if (StringUtils.isBlank(entePayload.getApplicationCode()) || entePayload.getApplicationCode().length() != 2)
        throw new ValidatorException("Segregation Code è invalido");
      if (StringUtils.isNotBlank(entePayload.getDeAutorizzazione()) && !entePayload.getDeAutorizzazione().matches(Constants.REGEX_AUTORIZZAZIONE_POSTE))
        throw new ValidatorException("Autorizzazione poste è invalido");
      if (StringUtils.isBlank(entePayload.getCodTipoEnte()))
        throw new ValidatorException("CodTipoEnte è campo obbligatorio");
      if(!anagraficaStatoService.getStatoEnteForSelect().stream().anyMatch(statoEnte -> statoEnte.getMygovAnagraficaStatoId().equals(entePayload.getCdStatoEnte().getMygovAnagraficaStatoId())))
        throw new ValidatorException("CodStatoEnte è invalido");
      if(!tassonomiaService.getTipoEnteForSelect().stream().anyMatch(tipoEnte -> tipoEnte.getCode().equals(entePayload.getCodTipoEnte())))
        throw new ValidatorException("CodTipoEnte è invalido");
    }
  }
}
