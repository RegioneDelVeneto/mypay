/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovTipiversamento;

import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * @author regione del veneto
 *
 */
public interface TipoVersamentoDao {

	/**
	 * @param id
	 * @return
	 */
	MygovTipiversamento getByKey(final long id) throws DataAccessException;

	/**
	 * @param tipoVersamento
	 * @return
	 */
	MygovTipiversamento getByTipoVersamento(final String tipoVersamento) throws DataAccessException;

	/**
	 * @return
	 */
	List<MygovTipiversamento> findAll() throws DataAccessException;

	/**
	 * @param tipoVersamento
	 * @return
	 */
	String getIuvCodicePerTipoVersamento(final String tipoVersamento) throws DataAccessException;

}
