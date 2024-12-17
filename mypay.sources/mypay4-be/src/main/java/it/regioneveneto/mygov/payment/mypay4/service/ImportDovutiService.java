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
import it.regioneveneto.mygov.payment.mypay4.dto.ResultImportFlussoTo;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.jdbi.v3.core.statement.OutParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  @Autowired
  private MailService mailService;

  @Transactional(propagation = Propagation.REQUIRED)
  public long insert(Ente ente, String codFedUserId, String codRequestToken) {

    AnagraficaStato anagraficaStato = anagraficaStatoDao.getByCodStatoAndTipoStato(Constants.STATO_IMPORT_LOAD, Constants.STATO_TIPO_IMPORT);

    ImportDovuti importDovuti = new ImportDovuti();
    importDovuti.setMygovEnteId(ente);
    importDovuti.setMygovAnagraficaStatoId(anagraficaStato);
    importDovuti.setCodRequestToken(codRequestToken);
    importDovuti.setMygovUtenteId(utenteDao.getByCodFedUserId(codFedUserId).orElseThrow());

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
    importDovuti.setMygovUtenteId(utenteDao.getByCodFedUserId(codIpa + "-"+Constants.WS_USER).orElseThrow());

    importDovuti.setDtCreazione(new Date());
    importDovuti.setDtUltimaModifica(new Date());
    return importDovutiDao.insert(importDovuti);
  }

  public ImportDovuti getFlussoImport(String requestToken) {
    List<ImportDovuti> listImportDovuti = importDovutiDao.getImportByRequestToken(requestToken);
    return listImportDovuti.size()>0 ? listImportDovuti.get(0): null;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long updateImportFlusso(ImportDovuti importDovuto, String codStato) {
    AnagraficaStato anagraficaStato = anagraficaStatoDao.getByCodStatoAndTipoStato(codStato, Constants.STATO_TIPO_IMPORT);
    importDovuto.setMygovAnagraficaStatoId(anagraficaStato);
    importDovuto.setVersion(importDovuto.getVersion()+1);
    importDovuto.setDtUltimaModifica(new Date());
    return importDovutiDao.updateImportFlusso(importDovuto);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public ImportDovutiOut callUpdateInsertFlussoFunction(Long mygovEnteId, String iuf, Date dataCreazione, Date dataUltimaModifica) {
    OutParameters out = importDovutiDao.callUpdateInsertFlussoFunction(mygovEnteId,iuf,dataCreazione,dataUltimaModifica);
    return out==null?null: ImportDovutiOut.builder()
            .sequenceValue(new BigInteger(out.getString("sequence_value")))
            .eccezione(out.getString("eccezione"))
            .build();
  }

  public void sendMailImportFlussoOk(Ente ente, ResultImportFlussoTo result, String emailOperatoreRichiedente) {
    Map<String, String> params = new HashMap<>();
    params.put("nomeFile", result.getNomeFile());
    DateFormat parser = new SimpleDateFormat("EEE, MMM dd yyyy, hh:mm:ss");
    String dataAttuale = parser.format(new Date());
    params.put("dataAttuale",dataAttuale);
    String testoMail = "Il caricamento del file " + result.getNomeFile() + " è andato a buon fine, tutti i " + result.getNumRigheTotali() + " dovuti presenti sono stati caricati correttamente.";
    params.put("testoMail",testoMail);
    String emailAmministratoreEnte = ente.getEmailAmministratore();
    String mailTo = "";
    String mailCC = "";
    if(StringUtils.hasText(emailOperatoreRichiedente)){
      mailTo = emailOperatoreRichiedente;
      if(!emailOperatoreRichiedente.equalsIgnoreCase(emailAmministratoreEnte)){
        mailCC = emailAmministratoreEnte;
      }
    }
    else{
      mailTo = emailAmministratoreEnte;
    }
    mailService.sendMailImportFlussoOk(new String[]{mailTo},(StringUtils.hasText(mailCC) ? new String[]{mailCC} : null),params);
  }

  public void sendMailImportFlussoScarti(Ente ente, ResultImportFlussoTo result, String emailOperatoreRichiedente) {
    Map<String, String> params = new HashMap<>();
    params.put("nomeFile", result.getNomeFile());
    DateFormat parser = new SimpleDateFormat("EEE, MMM dd yyyy, hh:mm:ss");
    String dataAttuale = parser.format(new Date());
    params.put("dataAttuale",dataAttuale);
    String testoMail = "Il caricamento del file " + result.getNomeFile() + " è andato a buon fine; non tutti i dovuti pero' sono stati caricati:" + "\r\n" + " - dovuti totali: " + result.getNumRigheTotali() + "\r\n" + " - dovuti caricati: " + result.getNumRigheImportate() + "\r\n" + "Accedere all'area operatore del portale per ottenere la lista dei dovuti che hanno generato errori e che non sono stati caricati.";
    params.put("testoMail",testoMail);
    String emailAmministratoreEnte = ente.getEmailAmministratore();
    String mailTo = "";
    String mailCC = "";
    if(StringUtils.hasText(emailOperatoreRichiedente)){
      mailTo = emailOperatoreRichiedente;
      if(!emailOperatoreRichiedente.equalsIgnoreCase(emailAmministratoreEnte)){
        mailCC = emailAmministratoreEnte;
      }
    }
    else{
      mailTo = emailAmministratoreEnte;
    }
    mailService.sendMailImportFlussoScarti(new String[]{mailTo},(StringUtils.hasText(mailCC) ? new String[]{mailCC} : null),params);
  }

  public void sendMailError(Ente ente, String nomeFile, String codErrore, String deErrore, String emailOperatoreRichiedente) {
    Map<String, String> params = new HashMap<>();
    params.put("nomeFile", nomeFile);
    DateFormat parser = new SimpleDateFormat("EEE, MMM dd yyyy, hh:mm:ss");
    String dataAttuale = parser.format(new Date());
    params.put("dataAttuale",dataAttuale);
    String testoMail = "\r\nCODICE ERRORE:\r\n" + codErrore + "\r\nDESCRIZIONE:\r\n" + deErrore;
    params.put("testoMail",testoMail);
    String emailAmministratoreEnte = ente.getEmailAmministratore();
    String mailTo = "";
    String mailCC = "";
    if(StringUtils.hasText(emailOperatoreRichiedente)){
      mailTo = emailOperatoreRichiedente;
      if(!emailOperatoreRichiedente.equalsIgnoreCase(emailAmministratoreEnte)){
        mailCC = emailAmministratoreEnte;
      }
    }
    else{
      mailTo = emailAmministratoreEnte;
    }
    mailService.sendMailImportFlussoError(new String[]{mailTo},(StringUtils.hasText(mailCC) ? new String[]{mailCC} : null),params);
  }
}
