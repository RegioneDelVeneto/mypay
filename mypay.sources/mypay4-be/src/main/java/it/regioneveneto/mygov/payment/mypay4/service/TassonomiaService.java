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


import it.regioneveneto.mygov.payment.mypay4.dao.FlussoTassonomiaDao;
import it.regioneveneto.mygov.payment.mypay4.dao.TassonomiaDao;
import it.regioneveneto.mygov.payment.mypay4.dto.FlussoTasMasTo;
import it.regioneveneto.mygov.payment.mypay4.dto.TassonomiaCodDescTo;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.FlussoTassonomia;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static it.regioneveneto.mygov.payment.mypay4.controller.BackofficeFlussoController.FILE_TYPE_TASSONOMIA_IMPORT;

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class TassonomiaService {

  private static String MESSAGE_PROPERTY = "pa.batch.import.";
  public final static String STATO_FLUSSO_ERRORE = "ERRORE_CARICAMENTO";
  public final static String STATO_FLUSSO_ERRORE_ELABORAZIONE= "ERRORE_ELABORAZIONE";
  private static String STATO_CARICATO = "CARICATO";

  @Autowired
  private TassonomiaDao tassonomiaDao;

  @Autowired
  private FlussoTassonomiaDao flussoTassonomiaDao;

  //@Autowired
  //private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  private MessageSource messageSource;

  public List<FlussoTasMasTo> searchTassonomie(String nomeTassonomia, LocalDate dateFrom, LocalDate dateTo) {
    List<FlussoTassonomia> flussoTassonomie = flussoTassonomiaDao.searchTassonomie(nomeTassonomia, dateFrom, dateTo.plusDays(1));
    return flussoTassonomie.stream().map(this::mapTassonomiaToDto).collect(Collectors.toList());
  }

  @Cacheable(value=CacheService.CACHE_NAME_TASSONOMIA, key="{'tipoEnte',#root.method}", unless="#result==null")
  public List<TassonomiaCodDescTo> getTipoEnteForSelect() {
    return tassonomiaDao.getTipoEnteForSelect();
  }

  @Cacheable(value=CacheService.CACHE_NAME_TASSONOMIA, key="{'macroArea',#tipoEnte}", unless="#result==null")
  public List<TassonomiaCodDescTo> getMacroAreaForSelect(String tipoEnte) {
    return tassonomiaDao.getMacroAreaForSelect(tipoEnte);
  }

  @Cacheable(value=CacheService.CACHE_NAME_TASSONOMIA, key="{'tipoServizio',#tipoEnte, #macroArea}", unless="#result==null")
  public List<TassonomiaCodDescTo> getTipoServizioForSelect(String tipoEnte, String macroArea) {
    return tassonomiaDao.getTipoServizioForSelect(tipoEnte, macroArea);
  }

  @Cacheable(value=CacheService.CACHE_NAME_TASSONOMIA, key="{'motivoRiscossione',#tipoEnte, #macroArea, #codTipoServizio}", unless="#result==null")
  public List<TassonomiaCodDescTo> getMotivoRiscossioneforSelect(String tipoEnte, String macroArea, String codTipoServizio) {
    return tassonomiaDao.getMotivoRiscossioneforSelect(tipoEnte, macroArea, codTipoServizio);
  }

  @Cacheable(value=CacheService.CACHE_NAME_TASSONOMIA, key="{'codTassonomico',#tipoEnte, #macroArea, #codTipoServizio, #motivoRiscossione}", unless="#result==null")
  public List<TassonomiaCodDescTo> getCodTassFromSelect(String tipoEnte, String macroArea, String codTipoServizio, String motivoRiscossione) {
    return tassonomiaDao.getCodTassFromSelect(tipoEnte, macroArea, codTipoServizio, motivoRiscossione);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void onUploadFile(UserWithAdditionalInfo user, String flussoType, String filePathString, String authToken) {
    if (!flussoType.equals(FILE_TYPE_TASSONOMIA_IMPORT))
      throw new ValidatorException("invalid upload file type");

    /*
    AnagraficaStato anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(STATO_CARICATO, Constants.STATO_TIPO_FLUSSO);
    Path filePath = Paths.get(filePathString);
    String fileName = filePath.getFileName().toString();
    FlussoTassonomia flussoTassonmia = FlussoTassonomia.builder()
        .mygovAnagraficaStatoId(anagraficaStato)
        .iuft(fileName.substring(0, fileName.length() - 4))
        .numRigheTotali(0L)
        .numRigheElaborateCorrettamente(0L)
        .deNomeOperatore(user.getCodiceFiscale())
        .dePercorsoFile(filePath.getParent().toString())
        .deNomeFile(fileName)
        .codRequestToken(authToken).build();
    long mygovFlussoTassonomiaId = flussoTassonomiaDao.insert(flussoTassonmia);
     */
  }

  private FlussoTasMasTo mapTassonomiaToDto(FlussoTassonomia flussoTassonomia) {
    FlussoTasMasTo tassonomiaTo = new FlussoTasMasTo();
    tassonomiaTo.setId(flussoTassonomia.getMygovFlussoTassonomiaId());
    tassonomiaTo.setCodStato(flussoTassonomia.getMygovAnagraficaStatoId().getCodStato());
    tassonomiaTo.setDeStato(flussoTassonomia.getMygovAnagraficaStatoId().getDeStato());
    tassonomiaTo.setIuf(flussoTassonomia.getIuft());
    tassonomiaTo.setNumRigheTotali(flussoTassonomia.getNumRigheTotali());
    tassonomiaTo.setNumRigheElaborateCorrettamente(flussoTassonomia.getNumRigheElaborateCorrettamente());
    tassonomiaTo.setDtCreazione(flussoTassonomia.getDtCreazione());
    tassonomiaTo.setDeNomeOperatore(flussoTassonomia.getDeNomeOperatore());
    tassonomiaTo.setShowDownload(false);
    if(STATO_FLUSSO_ERRORE.equals(tassonomiaTo.getCodStato())) {
      String errSrc = MESSAGE_PROPERTY + "PAA_IMPORT_NO_XLS";
      tassonomiaTo.setCodErrore(messageSource.getMessage(errSrc, null, Locale.ITALY));
    } else if(STATO_FLUSSO_ERRORE_ELABORAZIONE.equals(tassonomiaTo.getCodStato())) {
      String errSrc = MESSAGE_PROPERTY + "PAA_IMPORT_FLUSSO_DUPLICATO_NO_ENTE";
      tassonomiaTo.setCodErrore(messageSource.getMessage(errSrc, null, Locale.ITALY));
    } else if(STATO_CARICATO.equals(tassonomiaTo.getCodStato())) {
      tassonomiaTo.setShowDownload(true);
      if(StringUtils.isNotBlank(flussoTassonomia.getDeNomeFile()) && (flussoTassonomia.getDeNomeFile().length() > 4) ) {
        String nomeFlusso = flussoTassonomia.getDeNomeFile();
        tassonomiaTo.setPath(flussoTassonomia.getDePercorsoFile()+"/"+nomeFlusso.substring(0, nomeFlusso.length()-4));
      }
    }
    return tassonomiaTo;
  }

  public String getRightTaxonomicCode(String datiSpecificiRiscossione, String codiceTassonomicoTipoDovuto) {
    Predicate<String> matching = s -> s.matches(Constants.TAXONOMIC_CODE_PATTERN) && s.startsWith(codiceTassonomicoTipoDovuto);
    return matching.test(datiSpecificiRiscossione) ?
        datiSpecificiRiscossione : codiceTassonomicoTipoDovuto.concat(datiSpecificiRiscossione);
  }
}
