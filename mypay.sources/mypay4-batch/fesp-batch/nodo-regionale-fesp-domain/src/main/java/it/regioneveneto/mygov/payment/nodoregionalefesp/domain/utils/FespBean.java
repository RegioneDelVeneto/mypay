/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils;

/**
 * @author regione del veneto
 *
 */
public class FespBean {

	private String password;

	private String identificativoIntermediarioPa;

	private String identificativoStazioneIntermediarioPa;

	private String root;
	
	private String baseUrl;
	
	private Boolean informativaPosteSingola;

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the identificativoIntermediarioPa
	 */
	public String getIdentificativoIntermediarioPa() {
		return identificativoIntermediarioPa;
	}

	/**
	 * @param identificativoIntermediarioPa the identificativoIntermediarioPa to set
	 */
	public void setIdentificativoIntermediarioPa(
			String identificativoIntermediarioPa) {
		this.identificativoIntermediarioPa = identificativoIntermediarioPa;
	}

	/**
	 * @return the identificativoStazioneIntermediarioPa
	 */
	public String getIdentificativoStazioneIntermediarioPa() {
		return identificativoStazioneIntermediarioPa;
	}

	/**
	 * @param identificativoStazioneIntermediarioPa the identificativoStazioneIntermediarioPa to set
	 */
	public void setIdentificativoStazioneIntermediarioPa(
			String identificativoStazioneIntermediarioPa) {
		this.identificativoStazioneIntermediarioPa = identificativoStazioneIntermediarioPa;
	}

	/**
	 * @return the root
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(String root) {
		this.root = root;
	}

	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * @param baseUrl the baseUrl to set
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * @return the informativaPosteSingola
	 */
	public Boolean getInformativaPosteSingola() {
		return informativaPosteSingola;
	}

	/**
	 * @param informativaPosteSingola the informativaPosteSingola to set
	 */
	public void setInformativaPosteSingola(Boolean informativaPosteSingola) {
		this.informativaPosteSingola = informativaPosteSingola;
	}
}
