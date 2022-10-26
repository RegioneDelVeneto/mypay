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
import it.regioneveneto.mygov.payment.mypay4.dao.ImportDovutiDao;
import it.regioneveneto.mygov.payment.mypay4.dao.UtenteDao;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.ImportDovuti;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class ImportDovutiService {

  @Autowired
  private EnteDao enteDao;
  @Autowired
  private UtenteDao utenteDao;
  @Autowired
  private ImportDovutiDao importDovutiDao;
  @Autowired
  private AnagraficaStatoDao anagraficaStatoDao;

  @Transactional(propagation = Propagation.REQUIRED)
  public long insert(Ente ente, String codFedUserId, String codRequestToken) {

    AnagraficaStato anagraficaStato = anagraficaStatoDao.getByCodStatoAndTipoStato(Constants.STATO_IMPORT_LOAD, Constants.STATO_TIPO_IMPORT);

    ImportDovuti importDovuti = new ImportDovuti();
    importDovuti.setMygovEnteId(ente);
    importDovuti.setMygovAnagraficaStatoId(anagraficaStato);
    importDovuti.setCodRequestToken(codRequestToken);
    importDovuti.setMygovUtenteId(utenteDao.getByCodFedUserId(codFedUserId).get());

    importDovuti.setDtCreazione(new Date());
    importDovuti.setDtUltimaModifica(new Date());
    return importDovutiDao.insert(importDovuti);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long insert(String codIpa, String codRequestToken) {

    AnagraficaStato anagraficaStato = anagraficaStatoDao.getByCodStatoAndTipoStato(Constants.STATO_IMPORT_LOAD, Constants.STATO_TIPO_IMPORT);

    ImportDovuti importDovuti = new ImportDovuti();
    importDovuti.setMygovEnteId(enteDao.getEnteByCodIpa(codIpa));
    importDovuti.setMygovAnagraficaStatoId(anagraficaStato);
    importDovuti.setCodRequestToken(codRequestToken);
    importDovuti.setMygovUtenteId(utenteDao.getByCodFedUserId(codIpa + "-"+Constants.WS_USER).get());

    importDovuti.setDtCreazione(new Date());
    importDovuti.setDtUltimaModifica(new Date());
    return importDovutiDao.insert(importDovuti);
  }

  public ImportDovuti getFlussoImport(String requestToken) {
    List<ImportDovuti> listImportDovuti = importDovutiDao.getImportByRequestToken(requestToken);
    return listImportDovuti.size()>0 ? listImportDovuti.get(0): null;
  }

}
