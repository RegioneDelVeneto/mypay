/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils;

/**
 * @author regione del veneto
 *
 */
public class ChiediStatoRTPBean {

	private int intervalloMinutiModelloImmediato;
	private int intervalloMinutiModelloDifferito;
	private int intervalloMinutiModelloAttivatoPressoPsp;
	private boolean forzaGenerazioneRtNegativa;

	public int getIntervalloMinutiModelloImmediato() {
		return intervalloMinutiModelloImmediato;
	}

	public void setIntervalloMinutiModelloImmediato(int intervalloMinutiModelloImmediato) {
		this.intervalloMinutiModelloImmediato = intervalloMinutiModelloImmediato;
	}

	public int getIntervalloMinutiModelloDifferito() {
		return intervalloMinutiModelloDifferito;
	}

	public void setIntervalloMinutiModelloDifferito(int intervalloMinutiModelloDifferito) {
		this.intervalloMinutiModelloDifferito = intervalloMinutiModelloDifferito;
	}

	public int getIntervalloMinutiModelloAttivatoPressoPsp() {
		return intervalloMinutiModelloAttivatoPressoPsp;
	}

	public void setIntervalloMinutiModelloAttivatoPressoPsp(int intervalloMinutiModelloAttivatoPressoPsp) {
		this.intervalloMinutiModelloAttivatoPressoPsp = intervalloMinutiModelloAttivatoPressoPsp;
	}

	public boolean isForzaGenerazioneRtNegativa() {
		return forzaGenerazioneRtNegativa;
	}

	public void setForzaGenerazioneRtNegativa(boolean forzaGenerazioneRtNegativa) {
		this.forzaGenerazioneRtNegativa = forzaGenerazioneRtNegativa;
	}

}