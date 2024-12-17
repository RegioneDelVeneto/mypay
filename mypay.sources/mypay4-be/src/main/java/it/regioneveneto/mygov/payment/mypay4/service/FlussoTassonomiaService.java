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
import it.regioneveneto.mygov.payment.mypay4.dto.FlussoTasMasTo;
import it.regioneveneto.mygov.payment.mypay4.dto.PagoPaApiTaxonomyTo;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.FlussoTassonomia;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FlussoTassonomiaService {

	private static String MESSAGE_PROPERTY = "pa.batch.import.";
	public static final String STATO_FLUSSO_ERRORE = "ERRORE_CARICAMENTO";
	public static final String STATO_FLUSSO_ERRORE_ELABORAZIONE= "ERRORE_ELABORAZIONE";
	private static String STATO_CARICATO = "CARICATO";

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private AnagraficaStatoService anagraficaStatoService;

	@Autowired
	private FlussoTassonomiaDao flussoTassonomiaDao;

	public FlussoTassonomia getLast() {	return flussoTassonomiaDao.getLast();	}

	public List<FlussoTasMasTo> searchTassonomie(String nomeTassonomia, LocalDate dateFrom, LocalDate dateTo) {
		List<FlussoTassonomia> flussoTassonomie = flussoTassonomiaDao.searchTassonomie(nomeTassonomia, dateFrom, dateTo.plusDays(1));
		return flussoTassonomie.stream().map(this::mapTassonomiaToDto).collect(Collectors.toList());
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

	@Transactional(propagation = Propagation.REQUIRED)
	public FlussoTassonomia upsert(FlussoTassonomia flussoTassonomia) {
		if (flussoTassonomia.getMygovFlussoTassonomiaId() == null || getById(flussoTassonomia.getMygovFlussoTassonomiaId()) == null) {
			long aLong = flussoTassonomiaDao.insert(flussoTassonomia);
			flussoTassonomia.setMygovFlussoTassonomiaId(aLong);
		} else {
			flussoTassonomiaDao.update(flussoTassonomia);
		}
		return flussoTassonomia;
	}

	public FlussoTassonomia getById(Long mygovFlussoTassonomiaId) { return flussoTassonomiaDao.getById(mygovFlussoTassonomiaId); }

	@Transactional(propagation = Propagation.REQUIRED)
	public void onUploadFile(String user, String filePathString, int rowSize, PagoPaApiTaxonomyTo pagoPaApiTaxonomyTo) {

		AnagraficaStato anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(STATO_CARICATO, Constants.STATO_TIPO_FLUSSO);
		Path filePath = Paths.get(filePathString);
		String fileName = filePath.getFileName().toString();
		var flussoTassonomia = FlussoTassonomia.builder()
			.mygovAnagraficaStatoId(anagraficaStato)
			.iuft(fileName.substring(0, fileName.length() - 4))
			.numRigheTotali(Long.valueOf(rowSize))
			.numRigheElaborateCorrettamente(Long.valueOf(rowSize))
			.deNomeOperatore(user)
			.dePercorsoFile(Paths.get(filePathString).getParent().toString())
			.deNomeFile(fileName)
			.codRequestToken(pagoPaApiTaxonomyTo.getUuid())
			.hash(pagoPaApiTaxonomyTo.getHash())
			.build();
		flussoTassonomiaDao.insert(flussoTassonomia);
	}
}
