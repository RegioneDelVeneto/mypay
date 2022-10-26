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

import it.regioneveneto.mygov.payment.mypay4.dao.AnagraficaStatoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.EnteDao;
import it.regioneveneto.mygov.payment.mypay4.dao.ExportDovutiDao;
import it.regioneveneto.mygov.payment.mypay4.dao.UtenteDao;
import it.regioneveneto.mygov.payment.mypay4.dto.WsExportFlussoIncomeTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.ExportDovuti;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class ExportDovutiService {

  @Autowired
  private EnteDao enteDao;
  @Autowired
  private UtenteDao utenteDao;
  @Autowired
  private ExportDovutiDao exportDovutiDao;
  @Autowired
  private AnagraficaStatoDao anagraficaStatoDao;
  @Autowired
  private MessageSource messageSource;

  @Transactional(propagation = Propagation.REQUIRED)
  public long insert(Long mygovEnteId, String codFedUserId, LocalDate extractionFrom, LocalDate extractionTo, String codTipoDovuto,
                    String codRequestToken, boolean flgRicevuta, boolean flgIncrementale, String versioneTracciato) {

    AnagraficaStato anagraficaStato = anagraficaStatoDao.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_LOAD, Constants.STATO_TIPO_EXPORT);
    ExportDovuti exportDovuti = new ExportDovuti();
    exportDovuti.setMygovEnteId(enteDao.getEnteById(mygovEnteId));
    exportDovuti.setMygovUtenteId(utenteDao.getByCodFedUserId(codFedUserId).get());
    exportDovuti.setDtInizioEstrazione(java.sql.Date.valueOf(extractionFrom));
    exportDovuti.setDtFineEstrazione(java.sql.Date.valueOf(extractionTo));
    exportDovuti.setMygovAnagraficaStatoId(anagraficaStato);
    exportDovuti.setCodTipoDovuto(codTipoDovuto);
    exportDovuti.setCodRequestToken(codRequestToken);
    exportDovuti.setFlgRicevuta(flgRicevuta);
    exportDovuti.setFlgIncrementale(flgIncrementale);
    exportDovuti.setVersioneTracciato(versioneTracciato);

    exportDovuti.setDtCreazione(new Date());
    exportDovuti.setDtUltimaModifica(new Date());
    return exportDovutiDao.insert(exportDovuti);
  }

  public List<ExportDovuti> getByEnteNomefileDtmodifica(Long mygovEnteId, String codFedUserId, String nomeFile,
                                                 LocalDate dateFrom, LocalDate dateTo, int queryLimit) {
    return exportDovutiDao.getByEnteNomefileDtmodifica(mygovEnteId, codFedUserId, nomeFile, dateFrom, dateTo, queryLimit);
  }

  public int getByEnteNomefileDtmodificaCount(Long mygovEnteId, String codFedUserId, String nomeFile,
                                                        LocalDate dateFrom, LocalDate dateTo) {
    return exportDovutiDao.getByEnteNomefileDtmodificaCount(mygovEnteId, codFedUserId, nomeFile, dateFrom, dateTo);
  }

  public ExportDovuti getFlussoExport(String requestToken) {
    List<ExportDovuti> listExportDovuti = exportDovutiDao.getExportByRequestToken(requestToken);
    if (CollectionUtils.isEmpty(listExportDovuti)) {
      return null;
    } else if (listExportDovuti.size() > 1) {
      throw new MyPayException(messageSource.getMessage("pa.flusso.exportDovutiDuplicato", null, Locale.ITALY));
    }
    return listExportDovuti.get(0);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public ExportDovuti insertFlussoExport(WsExportFlussoIncomeTo to, String requestToken) {
    Date now = new Date();
    ExportDovuti exportDovuti = ExportDovuti.builder()
        .mygovEnteId(enteDao.getEnteByCodIpa(to.getCodIpaEnte()))
        .mygovAnagraficaStatoId(anagraficaStatoDao.getByCodStatoAndTipoStato(Constants.STATO_EXPORT_LOAD, Constants.STATO_TIPO_EXPORT))
        .mygovUtenteId(utenteDao.getByCodFedUserId(to.getCodIpaEnte()+ "-" + Constants.WS_USER).get())
        .dtInizioEstrazione(to.getDateFrom())
        .dtFineEstrazione(to.getDateTo())
        .codTipoDovuto(to.getIdentificativoTipoDovuto())
        .codRequestToken(requestToken)
        .flgRicevuta(to.isRicevuta())
        .flgIncrementale(to.isIncrementale())
        .versioneTracciato(to.getVersioneTracciato())
        .dtCreazione(now)
        .dtUltimaModifica(now)
        .build();
    long newId = exportDovutiDao.insert(exportDovuti);
    exportDovuti.setMygovExportDovutiId(newId);
    return exportDovuti;
  }
}
