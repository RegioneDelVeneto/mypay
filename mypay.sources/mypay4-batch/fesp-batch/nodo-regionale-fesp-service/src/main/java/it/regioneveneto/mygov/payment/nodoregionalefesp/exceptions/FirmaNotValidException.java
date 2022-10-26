/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions;

/**
 * @author regione del veneto
 * @author regione del veneto
 */
public class FirmaNotValidException extends RuntimeException {

	private String code;
	private String description;
	
	/**
	 * 
	 */
	public FirmaNotValidException() {
		super();
	}

	/**
	 * @param code
	 * @param description
	 */
	public FirmaNotValidException(String code, String description) {
		super(description);
		this.code = code;
	}
	
	/**
	 * @param message
	 */
	public FirmaNotValidException(String description) {
		super(description);
	}

	/**
	 * @param cause
	 */
	public FirmaNotValidException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FirmaNotValidException(String message, Throwable cause) {
		super(message, cause);
	}


	public FirmaNotValidException(String code, String description, Throwable cause) {
		super(description, cause);
		this.code = code;
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public FirmaNotValidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}


	/**
	 * @param code
	 * @param description
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public FirmaNotValidException(String code, String description, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(description, cause, enableSuppression, writableStackTrace);
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
