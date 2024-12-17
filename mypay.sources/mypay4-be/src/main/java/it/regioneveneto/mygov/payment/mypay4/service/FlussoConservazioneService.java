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
import it.regioneveneto.mygov.payment.mypay4.dao.ExportConservazioneDao;
import it.regioneveneto.mygov.payment.mypay4.dao.UtenteDao;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.ExportConservazione;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Transactional(propagation =  Propagation.SUPPORTS)
public class FlussoConservazioneService {
	
	@Autowired
	private ExportConservazioneDao exportConservazioneDao;
	
	@Autowired
	private AnagraficaStatoService anagraficaStatoService;

	@Autowired
	private AnagraficaStatoDao anagraficaStatoDao;

	@Autowired
	private EnteDao enteDao;

	@Autowired
	private UtenteDao utenteDao;

	@Value("${task.conservazione.orePerCancellazione}")
	private int orePerCancellazione;
	
	
	
	public List<ExportConservazione> getExportConservazioneDaEffettuare() {
		return exportConservazioneDao.getExportConservazioneDaEffettuare();
	}

	@Transactional(propagation =  Propagation.REQUIRED)
	public void updateExportConservazione(ExportConservazione exportConservazione, String absolutePath,
			String statoExportEseguito, long fileSize) {
		AnagraficaStato as = anagraficaStatoService.getByCodStatoAndTipoStato(statoExportEseguito, Constants.STATO_TIPO_EXPORT);
		exportConservazione.setDeNomeFileGenerato(absolutePath);
		exportConservazione.setNumDimensioneFileGenerato(fileSize);
		
		exportConservazioneDao.update(exportConservazione,  as.getMygovAnagraficaStatoId());
		
	}

	public List<ExportConservazione> getExportDaCancellare() {
		
		return exportConservazioneDao.getExportDaCancellare(orePerCancellazione);
	}

	///

	@Transactional(propagation = Propagation.REQUIRED)
	public long insert(Long mygovEnteId, String codFedUserId, LocalDate extractionFrom, LocalDate extractionTo,
					   String codRequestToken, String tipoTracciato) {

		AnagraficaStato anagraficaStato = anagraficaStatoDao.getByCodStatoAndTipoStato(it.regioneveneto.mygov.payment.mypay4.util.Constants.STATO_EXPORT_LOAD, it.regioneveneto.mygov.payment.mypay4.util.Constants.STATO_TIPO_EXPORT);
		ExportConservazione exportConservazione = new ExportConservazione();
		exportConservazione.setMygovEnteId(enteDao.getEnteById(mygovEnteId));
		exportConservazione.setMygovUtenteId(utenteDao.getByCodFedUserId(codFedUserId).get());
		exportConservazione.setDtInizioEstrazione(java.sql.Date.valueOf(extractionFrom));
		exportConservazione.setDtFineEstrazione(java.sql.Date.valueOf(extractionTo));
		exportConservazione.setMygovAnagraficaStatoId(anagraficaStato);
		exportConservazione.setCodRequestToken(codRequestToken);
		exportConservazione.setTipoTracciato(tipoTracciato);
		exportConservazione.setDtCreazione(new Date());
		exportConservazione.setDtUltimaModifica(new Date());
		return exportConservazioneDao.insert(exportConservazione);
	}

	public List<ExportConservazione> getByEnteNomefileDtmodifica(Long mygovEnteId, String codFedUserId, String nomeFile,
																 LocalDate dateFrom, LocalDate dateTo, int queryLimit) {
		return exportConservazioneDao.getByEnteNomefileDtmodifica(mygovEnteId, codFedUserId, nomeFile, dateFrom, dateTo, queryLimit);
	}

	public int getByEnteNomefileDtmodificaCount(Long mygovEnteId, String codFedUserId, String nomeFile,
												LocalDate dateFrom, LocalDate dateTo) {
		return exportConservazioneDao.getByEnteNomefileDtmodificaCount(mygovEnteId, codFedUserId, nomeFile, dateFrom, dateTo);
	}

	public ExportConservazione getFlussoConservazioneByID(Long idReload) {
		List<ExportConservazione> exportConservaziones = exportConservazioneDao.getExportConservazioneByID(idReload);
		if (exportConservaziones.size() > 1) {
			throw new DataIntegrityViolationException("pa.flusso.exportDovutiDuplicato");
		}
		if (exportConservaziones.size() == 0) {
			return null;
		}
		return exportConservaziones.get(0);
	}

	/*@Transactional(propagation = Propagation.REQUIRED)
	public void update(ExportConservazione exportConservazione, String pathFile,
					   String statoExportEseguito, long fileSize) {

		AnagraficaStato anagraficaStato = anagraficaStatoDao.getByCodStatoAndTipoStato(statoExportEseguito, it.regioneveneto.mygov.payment.mypay4.util.Constants.STATO_TIPO_EXPORT);
		exportConservazione.setMygovAnagraficaStatoId(anagraficaStato);

		if (pathFile != null) {
			int index = pathFile.indexOf("/EXPORT");
			String pathInDb = pathFile.substring(index);
			exportConservazione.setDeNomeFileGenerato(pathInDb);
		}
		exportConservazione.setNumDimensioneFileGenerato(fileSize);
		exportConservazione.setDtUltimaModifica(new Date());
		exportConservazioneDao.update(exportConservazione);
	}*/

}
