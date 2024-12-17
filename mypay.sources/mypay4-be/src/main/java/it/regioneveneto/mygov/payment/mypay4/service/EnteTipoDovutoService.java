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

import it.regioneveneto.mygov.payment.mypay4.dao.DovutoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoElaboratoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.EnteTipoDovutoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.OperatoreEnteTipoDovutoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaTipoDovutoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.CodeDescriptionTo;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteTipoDovutoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotAuthorizedException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteSil;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.OperatoreEnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.MaxResultsHelper;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.veneto.regione.pagamenti.ente.FaultBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class EnteTipoDovutoService {

  @Resource
  private EnteTipoDovutoService self;

  @Autowired
  private EnteService enteService;

  @Autowired
  private EnteTipoDovutoDao enteTipoDovutoDao;

  @Autowired
  private EnteSilService enteSilService;

  @Autowired
  private OperatoreEnteTipoDovutoDao operatoreEnteTipoDovutoDao;

  @Autowired
  private RegistroOperazioneService registroOperazioneService;

  @Autowired
  private DovutoDao dovutoDao;

  @Autowired
  private DovutoElaboratoDao dovutoElaboratoDao;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private MaxResultsHelper maxResultsHelper;

  @Autowired
  private StandardTipoDovutoService standardTipoDovutoService;

  @Autowired
  private BackOfficeService backOfficeService;

  @Transactional(propagation = Propagation.REQUIRED)
  public EnteTipoDovuto insert(EnteTipoDovuto enteTipoDovuto) {
    //check that tipoDovuto for same ente and codTipo does not exist
    Optional.ofNullable(
      self.getByCodTipoIdEnte(enteTipoDovuto.getCodTipo(),enteTipoDovuto.getMygovEnteId().getMygovEnteId()))
        .ifPresent(etd -> {
          throw new ValidatorException(messageSource.getMessage("pa.manager.error.tipodovuto.presente",
              new String[]{etd.getMygovEnteId().getCodIpaEnte()+"-"+etd.getCodTipo()}, Locale.ITALY)); });
    //by default ente tipo dovuto is inserted in disabled state
    enteTipoDovuto.setFlgAttivo(false);
    long mygovEnteTipoDovutoId = enteTipoDovutoDao.insert(enteTipoDovuto);
    self.clearCache();
    enteTipoDovuto.setMygovEnteTipoDovutoId(mygovEnteTipoDovutoId);
    return enteTipoDovuto;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public EnteTipoDovuto update(EnteTipoDovuto enteTipoDovuto) {
    //cannot change codTipo or ente
    EnteTipoDovuto actualData = self.getById(enteTipoDovuto.getMygovEnteTipoDovutoId());
    if( !StringUtils.equals(actualData.getMygovEnteId().getCodIpaEnte(), enteTipoDovuto.getMygovEnteId().getCodIpaEnte()) ||
        !StringUtils.equals(actualData.getCodTipo(), enteTipoDovuto.getCodTipo()) ){
      throw new BadRequestException(String.format("trying to change ente/codTipo from[%s-%s] to[%s-%s]",
          actualData.getMygovEnteId().getCodIpaEnte(), actualData.getCodTipo(),
          enteTipoDovuto.getMygovEnteId().getCodIpaEnte(), enteTipoDovuto.getCodTipo() ));
    }

    enteTipoDovutoDao.update(enteTipoDovuto);
    self.clearCache();
    return self.getById(enteTipoDovuto.getMygovEnteTipoDovutoId());
  }

  @CacheEvict(value=CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, allEntries = true)
  public void clearCache(){}

  @Transactional(propagation = Propagation.REQUIRED)
  public void switchActivation(UserWithAdditionalInfo user, Long mygovEnteTipoDovutoId, boolean flgAttivo) {
    EnteTipoDovuto enteTipoDovuto = self.getById(mygovEnteTipoDovutoId);
    String codEnteTipoDovuto = enteTipoDovuto.getMygovEnteId().getCodIpaEnte() + '|' + enteTipoDovuto.getCodTipo();
    if(flgAttivo != enteTipoDovuto.isFlgAttivo()) {
      //if activating, check that tassonomia fields are not null
      if(flgAttivo && (StringUtils.isBlank(enteTipoDovuto.getMacroArea()) ||
            StringUtils.isBlank(enteTipoDovuto.getTipoServizio()) ||
            StringUtils.isBlank(enteTipoDovuto.getMotivoRiscossione()) ||
            StringUtils.isBlank(enteTipoDovuto.getCodTassonomico()) ) ) {
          throw new BadRequestException(messageSource.getMessage("pa.manager.error.tipodovuto.tassonomiaMissing", null, Locale.ITALY));
      }
      enteTipoDovuto.setFlgAttivo(flgAttivo);
      enteTipoDovutoDao.update(enteTipoDovuto);
      registroOperazioneService.insert(user, RegistroOperazione.TipoOperazione.ENTE_TIP_DOV, codEnteTipoDovuto, flgAttivo);
      self.clearCache();
      if (!flgAttivo) {
        List<OperatoreEnteTipoDovuto> oetds = operatoreEnteTipoDovutoDao.getByEnteTipoDovuto(mygovEnteTipoDovutoId);
        oetds.stream().filter(OperatoreEnteTipoDovuto::isFlgAttivo).forEach(oetd -> {
          oetd.setFlgAttivo(false);
          operatoreEnteTipoDovutoDao.update(oetd);
          registroOperazioneService.insert(user, RegistroOperazione.TipoOperazione.OPER_TIP_DOV, oetd.getMygovOperatoreId().getCodFedUserId() + '|' + codEnteTipoDovuto, false);
        });
        log.info(oetds.size() + " records of OperatoreEnteTipoDovuto disactivated.");
      }
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int deleteTipoDovuto(Long mygovEnteTipoDovutoId) {
    EnteTipoDovuto enteTipoDovuto = self.getById(mygovEnteTipoDovutoId);
    long numDebiti = dovutoDao.count(enteTipoDovuto.getMygovEnteId().getCodIpaEnte(), enteTipoDovuto.getCodTipo());
    if (numDebiti > 0)
      throw new ValidatorException(messageSource.getMessage("pa.errore.esistedovutopagato", null, Locale.ITALY));
    long numPagati = dovutoElaboratoDao.count(enteTipoDovuto.getMygovEnteId().getCodIpaEnte(), enteTipoDovuto.getCodTipo());
    if (numPagati > 0)
      throw new ValidatorException(messageSource.getMessage("pa.errore.esistedovutopagato", null, Locale.ITALY));
    int deletedRec = operatoreEnteTipoDovutoDao.deleteAllByEnteTipoDovuto(mygovEnteTipoDovutoId);
    log.info(deletedRec  + " records of Operatore Ente Tipo Dovuto deleted.");

    // Cascade
    if (enteTipoDovuto.getMygovEnteSilId() != null) {
      long mygovEnteSilId = enteTipoDovuto.getMygovEnteSilId();
      enteTipoDovuto.setMygovEnteSilId(null);
      enteTipoDovutoDao.update(enteTipoDovuto);
      enteSilService.delete(mygovEnteSilId);
    }
    deletedRec = enteTipoDovutoDao.delete(mygovEnteTipoDovutoId);
    self.clearCache();
    return deletedRec;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AnagraficaTipoDovutoTo insertTipoDovuto(UserWithAdditionalInfo user, AnagraficaTipoDovutoTo anagraficaTipoDovutoTo) {
    if(enteService.getEnteByCodIpa(anagraficaTipoDovutoTo.getCodIpaEnte())==null)
      throw new NotFoundException();
    validateEnteTipoDovuto(anagraficaTipoDovutoTo);
    EnteTipoDovuto enteTipoDovuto = this.dtoToModel(anagraficaTipoDovutoTo, null, user);

    Optional.ofNullable(enteTipoDovuto.getIbanAccreditoPi())
        .ifPresent(s -> {
          enteTipoDovuto.setIbanAccreditoPi(s.toUpperCase());
          enteTipoDovuto.setCodContoCorrentePostale(s.toUpperCase().substring(15));
          enteTipoDovuto.setBicAccreditoPi(Constants.IDENTIFICATIVO_PSP_POSTE);
        });
    if (StringUtils.isBlank(enteTipoDovuto.getCodXsdCausale()))
      enteTipoDovuto.setCodXsdCausale(Constants.COD_XSD_DEFAULT);
    enteTipoDovuto.setBicAccreditoPiSeller(true);
    enteTipoDovuto.setBicAccreditoPspSeller(false);
    enteTipoDovuto.setSpontaneo(false);
    enteTipoDovuto.setFlgScadenzaObbligatoria(true);
    enteTipoDovuto.setFlgStampaDataScadenza(true);
    enteTipoDovuto.setFlgCfAnonimo(false);
    EnteTipoDovuto newEnteTipoDovuto = self.insert(enteTipoDovuto);
    self.clearCache();
    return self.getAnagrafica(newEnteTipoDovuto);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AnagraficaTipoDovutoTo updateTipoDovuto(UserWithAdditionalInfo user, AnagraficaTipoDovutoTo anagraficaTipoDovutoTo) {
    var enteById = Optional.ofNullable(enteService.getEnteById(anagraficaTipoDovutoTo.getMygovEnteId())).orElseThrow(NotFoundException::new);
    var enteByIpa = Optional.ofNullable(enteService.getEnteByCodIpa(anagraficaTipoDovutoTo.getCodIpaEnte())).orElseThrow(NotFoundException::new);
    if(!Objects.equals(enteById, enteByIpa))
      throw new ValidatorException(messageSource.getMessage("pa.manager.error.nessunEnte", null, Locale.ITALY));
    EnteTipoDovuto etdFromDb = self.getOptionalById(anagraficaTipoDovutoTo.getMygovEnteTipoDovutoId()).orElseThrow(NotFoundException::new);
    if (!user.isSysAdmin() && etdFromDb.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE))
      throw new NotAuthorizedException("this user cannot execute this operation");
    validateEnteTipoDovuto(anagraficaTipoDovutoTo);
    EnteTipoDovuto enteTipoDovuto = this.dtoToModel(anagraficaTipoDovutoTo, etdFromDb, user);

    Optional.ofNullable(enteTipoDovuto.getIbanAccreditoPi())
        .ifPresentOrElse(s -> {
          enteTipoDovuto.setIbanAccreditoPi(s.toUpperCase());
          enteTipoDovuto.setCodContoCorrentePostale(s.toUpperCase().substring(15));
          enteTipoDovuto.setBicAccreditoPi(Constants.IDENTIFICATIVO_PSP_POSTE);
        }, () -> {
          enteTipoDovuto.setCodContoCorrentePostale(null);
          enteTipoDovuto.setBicAccreditoPi(null);
        });
    if (enteTipoDovuto.isFlgNotificaEsitoPush()) {
      var optionalEnteSil = Optional.ofNullable(enteTipoDovuto.getMygovEnteSilId());
      EnteSil.EnteSilBuilder builder = optionalEnteSil
          .map(enteSilService::getById)
          .map(EnteSil::toBuilder)
          .orElse(EnteSil.builder());


        builder.mygovEnteId(enteTipoDovuto.getMygovEnteId()).mygovEnteTipoDovutoId(enteTipoDovuto);

      EnteSil enteSil = builder
          .nomeApplicativo(anagraficaTipoDovutoTo.getNomeApplicativo())
          .deUrlInoltroEsitoPagamentoPush(anagraficaTipoDovutoTo.getDeUrlInoltroEsitoPagamentoPush())
          .flgJwtAttivo(anagraficaTipoDovutoTo.isFlgJwtAttivo())
          .codServiceAccountJwtUscitaClientId(anagraficaTipoDovutoTo.getCodServiceAccountJwtUscitaClientId())
          .deServiceAccountJwtUscitaClientMail(anagraficaTipoDovutoTo.getDeServiceAccountJwtUscitaClientMail())
          .codServiceAccountJwtUscitaSecretKeyId(anagraficaTipoDovutoTo.getCodServiceAccountJwtUscitaSecretKeyId())
          .codServiceAccountJwtUscitaSecretKey(anagraficaTipoDovutoTo.getCodServiceAccountJwtUscitaSecretKey())
          .build();
      enteSilService.validate(enteSil, enteTipoDovuto.isFlgNotificaEsitoPush());
      long mygovEnteSilId = enteSilService.upsert(enteSil).getMygovEnteSilId();
      enteTipoDovuto.setMygovEnteSilId(mygovEnteSilId);
    }

    enteTipoDovutoDao.update(enteTipoDovuto);
    self.clearCache();
    EnteTipoDovuto updatedEnteTipoDovuto = self.getById(enteTipoDovuto.getMygovEnteTipoDovutoId());
    backOfficeService.sendReportToSystemMaintainer(user, enteById.getDeNomeEnte(), "Tipo Dovuto per " + updatedEnteTipoDovuto.getDeTipo(), self.getAnagrafica(etdFromDb), anagraficaTipoDovutoTo);
    return self.getAnagrafica(updatedEnteTipoDovuto);
  }

  public List<EnteTipoDovutoTo> searchByTipoFlgAttivo(String codTipo, String deTipo, Boolean flgAttivo) {
    return maxResultsHelper.manageMaxResults(
        maxResults -> enteTipoDovutoDao.searchByTipoFlgAttivo(codTipo, deTipo, flgAttivo, maxResults),
        () -> enteTipoDovutoDao.searchByTipoFlgAttivoCount(codTipo, deTipo, flgAttivo) );
  }

  public List<EnteTipoDovutoTo> searchByTipoEnteFlgAttivo(String codTipo, String codIpaEnte, String deNomeEnte, Boolean flgAttivo) {
    return enteTipoDovutoDao.searchByTipoEnteFlgAttivo(codTipo, codIpaEnte, deNomeEnte, flgAttivo);
  }

  public List<EnteTipoDovutoTo> searchByEnteTipoFlgAttivo(Long mygovEnteId, String codTipo, String deTipo, Boolean flgAttivo, Boolean withActivationInfo) {
    return enteTipoDovutoDao.searchByEnteTipoFlgAttivo(mygovEnteId, codTipo, deTipo, flgAttivo, withActivationInfo, false);
  }

  public EnteTipoDovutoTo getTipoDovutoByEnteAndCodTipo(Long mygovEnteId, String codTipo) {
    return enteTipoDovutoDao.getByEnteTipoFlgAttivo(mygovEnteId, codTipo, null, true, true).orElse(null);
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'mygovEnteId+operatoreUsername',#mygovEnteId,#operatoreUsername}", unless="#result==null")
  public List<EnteTipoDovuto> getByMygovEnteIdAndOperatoreUsername(Long mygovEnteId, String operatoreUsername) {
    return enteTipoDovutoDao.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, operatoreUsername);
  }

  public List<EnteTipoDovuto> addTipoDovutoExportEnteSecondario(Long mygovEnteId, List<EnteTipoDovuto> list) {
    String codIpaEnte = null;
    if(list.size()>0) {
      EnteTipoDovuto anETD = list.get(0);
      if(anETD.getMygovEnteId()!=null)
        codIpaEnte = anETD.getMygovEnteId().getCodIpaEnte();
    }
    EnteTipoDovuto exportEnteSecondario = EnteTipoDovuto.builder()
      .mygovEnteTipoDovutoId(-1l)
      .codTipo(Constants.COD_TIPO_DOVUTO_EXPORT_ENTE_SECONDARIO)
      .deTipo(Constants.DE_TIPO_DOVUTO_EXPORT_ENTE_SECONDARIO)
      .mygovEnteId(Ente.builder().mygovEnteId(mygovEnteId).codIpaEnte(codIpaEnte).build())
      .build();
    list.add(exportEnteSecondario);
    return list;
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'mygovEnteId+flags',#mygovEnteId, #spontaneo, #flgScadenzaObbligtoria}", unless="#result==null")
  public List<EnteTipoDovuto> getAttiviByMygovEnteIdAndFlags(Long mygovEnteId, Boolean spontaneo, Boolean flgScadenzaObbligatoria) {
    return  enteTipoDovutoDao.getAttiviByMygovEnteIdAndFlags(mygovEnteId, spontaneo, flgScadenzaObbligatoria);
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'spontaneo',#codIpaEnte, #codTipoDovuto}", unless="#result==null")
  public Optional<EnteTipoDovuto> getSpontaneo(String codIpaEnte, String codTipoDovuto) {
    return  enteTipoDovutoDao.getSpontaneo(codIpaEnte, codTipoDovuto);
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key="{'id',#id}", unless="#result==null")
  public EnteTipoDovuto getById(Long id) {
    return enteTipoDovutoDao.getById(id);
  }

  public Optional<EnteTipoDovuto> getOptionalById(Long id) {
    var etd = self.getById(id);
    return Optional.of(etd);
  }

  public AnagraficaTipoDovutoTo getAnagrafica(EnteTipoDovuto enteTipoDovuto) {
    AnagraficaTipoDovutoTo.AnagraficaTipoDovutoToBuilder builder = new AnagraficaTipoDovutoTo(enteTipoDovuto).toBuilder();

    if (enteTipoDovuto.getMygovEnteSilId() != null) {
      EnteSil enteSil = enteSilService.getById(enteTipoDovuto.getMygovEnteSilId());
      builder
        .nomeApplicativo(enteSil.getNomeApplicativo())
        .deUrlInoltroEsitoPagamentoPush(enteSil.getDeUrlInoltroEsitoPagamentoPush())
        .flgJwtAttivo(enteSil.isFlgJwtAttivo())
        .codServiceAccountJwtUscitaClientId(enteSil.getCodServiceAccountJwtUscitaClientId())
        .deServiceAccountJwtUscitaClientMail(enteSil.getDeServiceAccountJwtUscitaClientMail())
        .codServiceAccountJwtUscitaSecretKeyId(enteSil.getCodServiceAccountJwtUscitaSecretKeyId())
        .codServiceAccountJwtUscitaSecretKey(enteSil.getCodServiceAccountJwtUscitaSecretKey());
    }
    return builder.build();
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key="{'codTipoIdEnte',#codTipo,#mygovEnteId}", unless="#result==null")
  public EnteTipoDovuto getByCodTipoIdEnte(String codTipo, Long mygovEnteId) {
    return enteTipoDovutoDao.getByCodTipoIdEnte(codTipo, mygovEnteId);
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key="{'codTipo',#codTipo,#codIpaEnte}", unless="#result==null")
  public EnteTipoDovuto getByCodTipo(String codTipo, String codIpaEnte) {
    return enteTipoDovutoDao.getByCodTipo(codTipo, codIpaEnte);
  }

  public Optional<EnteTipoDovuto> getOptionalByCodTipo(String codTipo, String codIpaEnte, boolean onlyActive) {
    return Optional.ofNullable(self.getByCodTipo(codTipo, codIpaEnte)).filter(etd -> !onlyActive || etd.isFlgAttivo());
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key="{'mygovEnteId',#mygovEnteId}", unless="#result==null")
  public List<EnteTipoDovuto> getAllByEnte(long mygovEnteId) {
    return enteTipoDovutoDao.getAllByEnte(mygovEnteId);
  }

  public List<EnteTipoDovuto> getAll() {
  	return enteTipoDovutoDao.getAll();
  }

  public Optional<FaultBean> verificaTipoDovuto(String codIpaEnte, String identificativoTipoDovuto) {
    //EXPORT_ENTE_SECONDARIO is a "special marker" to select only the multi-beneficiary (SANP 2.5) payments where the ente is the "secondary ente"
    // since this tipo dovuto does not exist on DB as a regular tipo-dovuto, the check on DB must be skipped
    if (StringUtils.isNotBlank(identificativoTipoDovuto) &&
      !StringUtils.equals(Constants.COD_TIPO_DOVUTO_EXPORT_ENTE_SECONDARIO, identificativoTipoDovuto)
    ) {
      if(self.getOptionalByCodTipo(identificativoTipoDovuto, codIpaEnte, false).isEmpty()) {
        log.error("Identificativo dovuto non valido: " + identificativoTipoDovuto);
        return Optional.of(VerificationUtils.getFaultBean(codIpaEnte, Constants.CODE_PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO,  "identificativoTipoDovuto [" + identificativoTipoDovuto + "] non valido", null));
      }
    }
    return Optional.empty();
  }

  public static EnteTipoDovuto getEnteTipoDovutoDefault() {

    EnteTipoDovuto enteTipoDovuto = new EnteTipoDovuto();
    enteTipoDovuto.setCodTipo(Constants.COD_TIPO_DOVUTO_DEFAULT);
    enteTipoDovuto.setDeTipo(Constants.COD_TIPO_DOVUTO_DEFAULT);
    enteTipoDovuto.setCodXsdCausale(Constants.COD_XSD_DEFAULT);
    enteTipoDovuto.setBicAccreditoPiSeller(false);
    enteTipoDovuto.setBicAccreditoPspSeller(false);
    enteTipoDovuto.setSpontaneo(false);
    enteTipoDovuto.setFlgScadenzaObbligatoria(false);
    enteTipoDovuto.setFlgStampaDataScadenza(false);
    enteTipoDovuto.setFlgCfAnonimo(false);

    return enteTipoDovuto;
  }

  public void validateEnteTipoDovuto(AnagraficaTipoDovutoTo enteTipoDovuto) {
    if (StringUtils.isBlank(enteTipoDovuto.getCodTipo()) || enteTipoDovuto.getCodTipo().contains(" "))
      throw new ValidatorException("Codice Tipo Dovuto è invalido");
    if (StringUtils.isBlank(enteTipoDovuto.getDeTipo()))
      throw new ValidatorException("Descrizione Tipo Dovuto è campo obbligatorio");
    if (StringUtils.isNotBlank(enteTipoDovuto.getIbanAccreditoPi()) && !Utilities.isValidIban(enteTipoDovuto.getIbanAccreditoPi()))
      throw new ValidatorException("Iban Accredito Poste Italiane è invalido");
    if (StringUtils.isNotBlank(enteTipoDovuto.getIbanAccreditoPsp()) && !Utilities.isValidIban(enteTipoDovuto.getIbanAccreditoPsp()))
      throw new ValidatorException("Iban Accredito PSP è invalido");
    if(enteTipoDovuto.getImporto()!=null){
      String errorImporto = Utilities.verificaImporto(enteTipoDovuto.getImporto());
      if(errorImporto!=null)
        throw new ValidatorException(errorImporto);
    }
  }

  private EnteTipoDovuto dtoToModel(AnagraficaTipoDovutoTo anagrafica, EnteTipoDovuto etdFromDb, UserWithAdditionalInfo user) {
    EnteTipoDovuto.EnteTipoDovutoBuilder builder = Optional.ofNullable(etdFromDb)
      .map(EnteTipoDovuto::toBuilder)
      .orElse(EnteTipoDovuto.builder()
        .mygovEnteId(enteService.getEnteById(anagrafica.getMygovEnteId()))
      );
    if (user.isSysAdmin()) {
      builder
        .codTipo(anagrafica.getCodTipo())
        .deTipo(anagrafica.getDeTipo())
        .codXsdCausale(anagrafica.getCodXsdCausale())
        .deUrlPagamentoDovuto(anagrafica.getDeUrlPagamentoDovuto())
        .deSettoreEnte(anagrafica.getDeSettoreEnte())
        .deIntestatarioCcPostale(anagrafica.getDeIntestatarioCcPostale())
        .codiceContestoPagamento(anagrafica.getCodiceContestoPagamento())
        .flgNotificaEsitoPush(anagrafica.isFlgNotificaEsitoPush())
        .maxTentativiInoltroEsito(anagrafica.getMaxTentativiInoltroEsito())
        .macroArea(anagrafica.getMacroArea())
        .tipoServizio(anagrafica.getTipoServizio())
        .motivoRiscossione(anagrafica.getMotivoRiscossione())
        .flgCfAnonimo(anagrafica.isFlgCfAnonimo())
        .flgDisabilitaStampaAvviso(anagrafica.isFlgDisabilitaStampaAvviso())
        .codTassonomico(anagrafica.getCodTassonomico())
              .urlNotificaPnd(anagrafica.getUrlNotificaPnd())
              .userPnd(anagrafica.getUserPnd())
              .pswPnd(anagrafica.getPswPnd())
              .urlNotificaAttualizzazionePnd(anagrafica.getUrlNotificaAttualizzazionePnd());
    }
    return builder
        .ibanAccreditoPi(anagrafica.getIbanAccreditoPi())
        .ibanAccreditoPsp(anagrafica.getIbanAccreditoPsp())
        .importo(anagrafica.getImporto())
        .spontaneo(anagrafica.isSpontaneo())
        .flgScadenzaObbligatoria(anagrafica.isFlgScadenzaObbligatoria())
        .flgStampaDataScadenza(anagrafica.isFlgStampaDataScadenza())
        .deBilancioDefault(anagrafica.getDeBilancioDefault())
        .flgNotificaIo(anagrafica.isFlgNotificaIo())
        .build();
  }

  public CodeDescriptionTo mapEnteTipoDovutoToCodeDescriptionDto(EnteTipoDovuto enteTipoDovuto) {
    return enteTipoDovuto == null ? null : CodeDescriptionTo.builder()
        .code(enteTipoDovuto.getCodTipo())
        .descr(enteTipoDovuto.getDeTipo())
        .build();
  }

  public EnteTipoDovutoTo mapEnteTipoDovutoToDto(EnteTipoDovuto enteTipoDovuto) {
    return enteTipoDovuto == null ? null : EnteTipoDovutoTo.builder()
        .mygovEnteTipoDovutoId(enteTipoDovuto.getMygovEnteTipoDovutoId())
        .codIpaEnte(enteTipoDovuto.getMygovEnteId()!=null?enteTipoDovuto.getMygovEnteId().getCodIpaEnte():null)
        .codTipo(enteTipoDovuto.getCodTipo())
        .deUrlPagamentoDovuto(enteTipoDovuto.getDeUrlPagamentoDovuto())
        .deTipo(enteTipoDovuto.getDeTipo())
        .flgCfAnonimo(enteTipoDovuto.isFlgCfAnonimo())
        .flgScadenzaObbligatoria(enteTipoDovuto.isFlgScadenzaObbligatoria())
        .flgAttivo(enteTipoDovuto.isFlgAttivo())
        .importo(enteTipoDovuto.getImporto() != null ? enteTipoDovuto.getImporto().toString() : null)
        .build();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void insertDefaultSet(Ente ente, boolean flagInsertDefaultSet) {
    EnteTipoDovuto marcaBolloDigitale = EnteTipoDovuto.builder()
        .mygovEnteId(ente)
        .codTipo(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)
        .deTipo("Marca da bollo digitale")
        .codXsdCausale(Constants.COD_XSD_DEFAULT)
        .bicAccreditoPiSeller(true)
        .bicAccreditoPspSeller(false)
        .spontaneo(false)
        .build();
    Set<EnteTipoDovuto> setToInsert = new HashSet<>(Collections.singleton(marcaBolloDigitale));
    if (flagInsertDefaultSet) {
      //TODO get list from config table and add to #setToInsert
      Set<EnteTipoDovuto> setToAdd = standardTipoDovutoService.getAllStandardDefault(ente);
      setToInsert.addAll(setToAdd);
    }
    setToInsert.forEach(item -> self.insert(item));
  }

  @Cacheable(value=CacheService.CACHE_NAME_ENTE_TIPO_DOVUTO, key = "{'flagSpontaneo', #mygovEnteId}", unless="#result==null")
  public boolean existsAnyFlagSpontaneo(Long mygovEnteId) { return  enteTipoDovutoDao.existsAnyFlagSpontaneo(mygovEnteId);  }

}
