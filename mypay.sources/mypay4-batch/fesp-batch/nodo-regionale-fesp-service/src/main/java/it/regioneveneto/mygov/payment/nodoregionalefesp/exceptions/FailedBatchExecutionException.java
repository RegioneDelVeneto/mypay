/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions;

/**
 * @author regione del veneto
 *
 */
public class FailedBatchExecutionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public FailedBatchExecutionException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public FailedBatchExecutionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public FailedBatchExecutionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FailedBatchExecutionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
