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
import it.regioneveneto.mygov.payment.mypay4.dto.TassonomiaTo;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.FlussoTassonomia;
import it.regioneveneto.mygov.payment.mypay4.model.Tassonomia;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class TassonomiaService {

  private static String MESSAGE_PROPERTY = "pa.batch.import.";
  public static final String STATO_FLUSSO_ERRORE = "ERRORE_CARICAMENTO";
  public static final String STATO_FLUSSO_ERRORE_ELABORAZIONE= "ERRORE_ELABORAZIONE";
  private static String STATO_CARICATO = "CARICATO";

  @Autowired
  private TassonomiaDao tassonomiaDao;

  @Autowired
  private FlussoTassonomiaDao flussoTassonomiaDao;


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

  public String getMyPayTransferCategoryPatternFormat(Dovuto dovuto, String codiceTassonomicoTipoDovuto) {
    var out = new AtomicReference<>(codiceTassonomicoTipoDovuto.concat(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione()));
    Matcher matcher = Pattern.compile("^(9/.*?/).*$").matcher(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione());
    if(matcher.find())
      tassonomiaDao.getByTipoEnte(dovuto.getNestedEnte().getCodTipoEnte()).stream()
        .map(Tassonomia::getCodiceTassonomico)
        .filter(matcher.group(1)::contains)
        .findAny().ifPresent(s -> out.set(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione()));
    return out.get();
  }

  @Cacheable(value=CacheService.CACHE_NAME_TASSONOMIA, key="{'ifExitsCodiceTassonomico',#codiceTassonomico}")
  public boolean ifExitsCodiceTassonomico(String codiceTassonomico) {
    boolean result = false;
    if (StringUtils.isNotBlank(codiceTassonomico)) {
      Boolean existsCodiceTassonomico = tassonomiaDao.ifExitsCodiceTassonomico(codiceTassonomico);
      result = (existsCodiceTassonomico != null && existsCodiceTassonomico.compareTo(Boolean.TRUE) == 0 );
    }
    return result;
  }

  public String getCleanTransferCategory(Dovuto dovuto, EnteTipoDovuto enteTipoDovuto) {
    var transferCategory = getMyPayTransferCategoryPatternFormat(dovuto, enteTipoDovuto.getCodTassonomico());
    Matcher matcher = Pattern.compile("^9/(.*?)/.*$").matcher(transferCategory);
    if(matcher.find())
      return matcher.group(1);
    return String.format("%s%s%s%s", dovuto.getNestedEnte().getCodTipoEnte(),
      enteTipoDovuto.getMacroArea(), enteTipoDovuto.getTipoServizio(), enteTipoDovuto.getMotivoRiscossione());
  }

  public Set<Tassonomia> getAllExpiring() { return tassonomiaDao.getAllExpiring();  }

  public Set<TassonomiaTo> getAll() { return tassonomiaDao.getAll();  }

  public Map<String, TassonomiaTo> getAllMapped() {
    return tassonomiaDao.getAll().stream().collect(Collectors.toMap(TassonomiaTo::getDatiSpecificiIncasso, Function.identity()));
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Tassonomia upsert(TassonomiaTo tassonomiaTo) {
    var tassonomia = Tassonomia.builder()
        .mygovTassonomiaId(tassonomiaTo.getId())
        .version(Integer.parseInt(tassonomiaTo.getVersioneTassonomia()))
        .tipoEnte(tassonomiaTo.getCodiceTipoEnte())
        .descrizioneTipoEnte(tassonomiaTo.getDescrizioneTipoEnte())
        .progMacroArea(tassonomiaTo.getProgressivoMacroArea())
        .nomeMacroArea(tassonomiaTo.getNomeMacroArea())
        .descMacroArea(tassonomiaTo.getDescricioneMacroArea())
        .codTipoServizio(tassonomiaTo.getCodiceTipoServizio())
        .tipoServizio(tassonomiaTo.getTipoServizio())
        .descrizioneTipoServizio(tassonomiaTo.getDescrizioneTipoEnte())
        .motivoRiscossione(tassonomiaTo.getMotivoRiscossione())
        .dtInizioValidita(tassonomiaTo.getDataInizioValidita())
        .dtFineValidita(tassonomiaTo.getDataFineValidita())
        .codiceTassonomico(tassonomiaTo.getDatiSpecificiIncasso())
        .build();
    if (tassonomia.getMygovTassonomiaId() == null) {
      long aLong = tassonomiaDao.insert(tassonomia);
      tassonomia.setMygovTassonomiaId(aLong);
    } else {
      tassonomiaDao.update(tassonomia);
    }
    return tassonomia;
  }
}
