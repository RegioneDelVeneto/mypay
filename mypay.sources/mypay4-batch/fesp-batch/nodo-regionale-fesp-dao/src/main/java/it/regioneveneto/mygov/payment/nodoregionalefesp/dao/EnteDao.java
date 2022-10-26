package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;

import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * @author regione del veneto
 * @author regione del veneto
 *
 */

public interface EnteDao {

	/**
	 * @param cfEnte
	 * @return
	 * @throws DataAccessException
	 */
	MygovEnte getByCodiceFiscale(final String codiceFiscaleEnte) throws DataAccessException;

	/**
	 * @param codiceIpa
	 * @return
	 * @throws DataAccessException
	 */
	MygovEnte getByCodiceIpa(final String codiceIpa) throws DataAccessException;

	/**
	 * @return
	 * @throws DataAccessException
	 */
	public List<MygovEnte> findAll() throws DataAccessException;
}
