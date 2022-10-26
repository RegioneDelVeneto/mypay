/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions;

/**
 * @author regione del veneto
 *
 */
public class IuvGenerationException extends RuntimeException {

	/**
	 * Costruttore senza argomenti
	 */
	public IuvGenerationException() {
	}

	/**
	 * @param message
	 */
	public IuvGenerationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IuvGenerationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IuvGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

}
