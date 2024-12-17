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

import it.regioneveneto.mygov.payment.mypay4.controller.FlussoController;
import it.regioneveneto.mygov.payment.mypay4.dao.AnagraficaStatoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.FlussoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.FlussoExportScadutiDao;
import it.regioneveneto.mygov.payment.mypay4.dto.FileTo;
import it.regioneveneto.mygov.payment.mypay4.dto.FlussoImportTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.queue.QueueProducer;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.MaxResultsHelper;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediFlussoSPCPage;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ChiediFlussoSPCPageRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FlussoSPC;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jdbi.v3.core.mapper.JoinRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class FlussoExportScadutiService {

  @Resource
  private FlussoExportScadutiService self;
  @Autowired
  private FlussoExportScadutiDao flussoExportScadutiDao;
  @Autowired
  private AnagraficaStatoDao anagraficaStatoDao;

  public List<FlussoExportScaduti> getByRequestToken(final String requestToken) {return flussoExportScadutiDao.getByRequestToken(requestToken);}

  public List<FlussoExportScaduti> getListaFlussiRichiesti() {return flussoExportScadutiDao.getListaFlussiRichiesti();}

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateFlusso(FlussoExportScaduti FlussoExportScaduti) {flussoExportScadutiDao.updateFlusso(FlussoExportScaduti);}

  @Transactional(propagation = Propagation.REQUIRED)
  public FlussoExportScaduti insertWithRefresh(String nomeFlusso, Ente ente, String tipiDovuto, Date dataScadenza, String anagraficaStato, String requestToken) {
    FlussoExportScaduti flussoExportScaduto = new FlussoExportScaduti();
    List<FlussoExportScaduti> flussoExportScaduti = flussoExportScadutiDao.getByRequestToken(requestToken);
    if (flussoExportScaduti.size() > 1) {

      throw new DataIntegrityViolationException("pa.flusso.flussoDuplicato");
    }
    if (flussoExportScaduti.size() == 0) {

      AnagraficaStato anagStato = anagraficaStatoDao.getByCodStatoAndTipoStato(anagraficaStato, Constants.STATO_TIPO_EXPORT);

      flussoExportScaduto.setMygovEnteId(ente);
      flussoExportScaduto.setDtCreazione(new Date());
      flussoExportScaduto.setDtUltimaModifica(new Date());
      flussoExportScaduto.setMygovAnagraficaStatoId(anagStato);
      flussoExportScaduto.setIuf(nomeFlusso);
      flussoExportScaduto.setVersion(0);
      flussoExportScaduto.setNumPagamentiScaduti(null);
      flussoExportScaduto.setCodRequestToken(requestToken);
      flussoExportScaduto.setTipiDovuto(tipiDovuto);
      flussoExportScaduto.setDtScadenza(dataScadenza);
      flussoExportScaduto.setDeNomeFile(null);
      flussoExportScaduto.setDePercorsoFile(null);
      flussoExportScaduto.setCodErrore(null);
      flussoExportScadutiDao.insert(flussoExportScaduto);
      //Long idFlussoExportScaduto =
     //flussoExportScaduto.setMygovFlussoExportScadutiId(idFlussoExportScaduto);
    }

    return flussoExportScaduto;
  }
}
