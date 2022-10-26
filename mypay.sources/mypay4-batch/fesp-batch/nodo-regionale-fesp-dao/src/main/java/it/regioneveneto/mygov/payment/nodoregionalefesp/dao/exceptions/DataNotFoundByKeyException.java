/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.exceptions;

import org.springframework.dao.DataAccessException;

/**
 * @author regione del veneto
 *
 */
public class DataNotFoundByKeyException extends DataAccessException {

	/**
	 * @param msg
	 */
	public DataNotFoundByKeyException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param msg
	 * @param cause
	 */
	public DataNotFoundByKeyException(String msg, Throwable cause) {
		super(msg, cause);
		// TODO Auto-generated constructor stub
	}

}
