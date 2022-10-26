/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions;

/**
 * @author regione del veneto
 *
 */
public class IuvGeneratorNotFound extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Costruttore senza argomenti
	 */
	public IuvGeneratorNotFound() {
		super();
	}

	/**
	 * @param message
	 */
	public IuvGeneratorNotFound(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IuvGeneratorNotFound(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IuvGeneratorNotFound(String message, Throwable cause) {
		super(message, cause);
	}

}
