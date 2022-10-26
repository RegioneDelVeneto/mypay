package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import org.springframework.dao.DataAccessException;

/**
 * @author regione del veneto
 *
 */
public interface IuvDao {

	/**
	 * @param codiceIpaEnte
	 * @param tipoVersamento
	 * @param iuv
	 * @throws DataAccessException
	 */
	void insert(final String codiceIpaEnte, final String tipoVersamento, final String iuv) throws DataAccessException;

}
