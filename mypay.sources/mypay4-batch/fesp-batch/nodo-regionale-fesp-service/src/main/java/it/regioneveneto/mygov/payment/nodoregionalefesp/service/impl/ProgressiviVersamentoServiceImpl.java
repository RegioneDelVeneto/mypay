/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.ProgressiviVersamentoDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ProgressiviVersamentoService;

/**
 * @author regione del veneto
 *
 */
public class ProgressiviVersamentoServiceImpl implements ProgressiviVersamentoService {

	/**
	 * 
	 */
	private ProgressiviVersamentoDao progressiviVersamentoDao;

	/**
	 * 
	 */
	public ProgressiviVersamentoServiceImpl() {
		super();
	}

	/**
	 * @param progressiviVersamentoDao the progressiviVersamentoDao to set
	 */
	public void setProgressiviVersamentoDao(ProgressiviVersamentoDao progressiviVersamentoDao) {
		this.progressiviVersamentoDao = progressiviVersamentoDao;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ProgressiviVersamentoService#getNextProgressivoVersamento(java.lang.String, java.lang.String)
	 */
	@Override
	public long getNextProgressivoVersamento(final String codiceIpaEnte, final String tipoGeneratore,
			final String tipoVersamento) {
		return this.progressiviVersamentoDao
				.getNextProgressivoVersamento(codiceIpaEnte, tipoGeneratore, tipoVersamento);
	}
}
