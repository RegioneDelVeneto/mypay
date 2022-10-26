package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.exceptions;

@SuppressWarnings("serial")
public class PspNonTrovatoException extends Exception {

	public PspNonTrovatoException() {
		super();
	}

	public PspNonTrovatoException(String message) {
		super(message);
	}

	public PspNonTrovatoException(Exception e) {
		super(e);
	}

	public PspNonTrovatoException(String message, Exception e) {
		super(message, e);
	}

}
