package it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions;

import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRPRisposta;

public class NodoSILInviaRPRispostaException extends RuntimeException {

	private NodoSILInviaRPRisposta nodoSILInviaRPRisposta;

	public NodoSILInviaRPRisposta getNodoSILInviaRPRisposta() {
		return nodoSILInviaRPRisposta;
	}

	public void setNodoSILInviaRPRisposta(NodoSILInviaRPRisposta nodoSILInviaRPRisposta) {
		this.nodoSILInviaRPRisposta = nodoSILInviaRPRisposta;
	}

	private static final long serialVersionUID = 1L;

	public NodoSILInviaRPRispostaException() {
	}

	public NodoSILInviaRPRispostaException(String message) {
		super(message);
	}

	public NodoSILInviaRPRispostaException(Throwable cause) {
		super(cause);
	}

	public NodoSILInviaRPRispostaException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodoSILInviaRPRispostaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
