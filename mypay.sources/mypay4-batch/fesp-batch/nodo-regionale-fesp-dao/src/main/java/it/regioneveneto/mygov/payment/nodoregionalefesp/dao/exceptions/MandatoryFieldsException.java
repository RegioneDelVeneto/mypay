/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.exceptions;

import org.springframework.dao.DataAccessException;

/**
 * @author regione del veneto
 *
 */
public class MandatoryFieldsException extends DataAccessException {

	/**
	 * @param msg
	 */
	public MandatoryFieldsException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param msg
	 * @param cause
	 */
	public MandatoryFieldsException(String msg, Throwable cause) {
		super(msg, cause);
		// TODO Auto-generated constructor stub
	}

}
