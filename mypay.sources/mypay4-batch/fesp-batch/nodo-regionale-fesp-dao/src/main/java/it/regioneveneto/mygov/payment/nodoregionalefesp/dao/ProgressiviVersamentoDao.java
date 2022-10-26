/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import org.springframework.dao.DataAccessException;

/**
 * @author regione del veneto
 *
 */
public interface ProgressiviVersamentoDao {

	/**
	 * @param codiceIpaEnte
	 * @param tipoGeneratore
	 * @param tipoVersamento
	 * @return
	 * @throws DataAccessException
	 */
	long getNextProgressivoVersamento(final String codiceIpaEnte, final String tipoGeneratore,
			final String tipoVersamento) throws DataAccessException;
}
