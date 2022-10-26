package it.regioneveneto.mygov.payment.nodoregionalefesp.dto;

import java.math.BigDecimal;




/**
 * 
 * @author regione del veneto
 *
 */
public class RrErDettaglioDto {
	
	private Long mygovRevocaDettaglioPagamentiId;
	
    private BigDecimal numRrDatiSingRevSingoloImportoRevocato;
    
    private String codRrDatiSingRevIdUnivocoRiscossione;
    
    private String deRrDatiSingRevCausaleRevoca;
    
    private String deRrDatiSingRevDatiAggiuntiviRevoca;
    
    private BigDecimal numErDatiSingRevSingoloImportoRevocato;
    
    private String codErDatiSingRevIdUnivocoRiscossione;
    
    private String deErDatiSingRevCausaleRevoca;
    
    private String deErDatiSingRevDatiAggiuntiviRevoca;
    
	/**
	 * 
	 */
	public RrErDettaglioDto() {
		super();
	}

	/**
	 * @return the mygovRevocaDettaglioPagamentiId
	 */
	public Long getMygovRevocaDettaglioPagamentiId() {
		return mygovRevocaDettaglioPagamentiId;
	}

	/**
	 * @param mygovRevocaDettaglioPagamentiId the mygovRevocaDettaglioPagamentiId to set
	 */
	public void setMygovRevocaDettaglioPagamentiId(Long mygovRevocaDettaglioPagamentiId) {
		this.mygovRevocaDettaglioPagamentiId = mygovRevocaDettaglioPagamentiId;
	}

	/**
	 * @return the numRrDatiSingRevSingoloImportoRevocato
	 */
	public BigDecimal getNumRrDatiSingRevSingoloImportoRevocato() {
		return numRrDatiSingRevSingoloImportoRevocato;
	}

	/**
	 * @param numRrDatiSingRevSingoloImportoRevocato the numRrDatiSingRevSingoloImportoRevocato to set
	 */
	public void setNumRrDatiSingRevSingoloImportoRevocato(BigDecimal numRrDatiSingRevSingoloImportoRevocato) {
		this.numRrDatiSingRevSingoloImportoRevocato = numRrDatiSingRevSingoloImportoRevocato;
	}

	/**
	 * @return the codRrDatiSingRevIdUnivocoRiscossione
	 */
	public String getCodRrDatiSingRevIdUnivocoRiscossione() {
		return codRrDatiSingRevIdUnivocoRiscossione;
	}

	/**
	 * @param codRrDatiSingRevIdUnivocoRiscossione the codRrDatiSingRevIdUnivocoRiscossione to set
	 */
	public void setCodRrDatiSingRevIdUnivocoRiscossione(String codRrDatiSingRevIdUnivocoRiscossione) {
		this.codRrDatiSingRevIdUnivocoRiscossione = codRrDatiSingRevIdUnivocoRiscossione;
	}

	/**
	 * @return the deRrDatiSingRevCausaleRevoca
	 */
	public String getDeRrDatiSingRevCausaleRevoca() {
		return deRrDatiSingRevCausaleRevoca;
	}

	/**
	 * @param deRrDatiSingRevCausaleRevoca the deRrDatiSingRevCausaleRevoca to set
	 */
	public void setDeRrDatiSingRevCausaleRevoca(String deRrDatiSingRevCausaleRevoca) {
		this.deRrDatiSingRevCausaleRevoca = deRrDatiSingRevCausaleRevoca;
	}

	/**
	 * @return the deRrDatiSingRevDatiAggiuntiviRevoca
	 */
	public String getDeRrDatiSingRevDatiAggiuntiviRevoca() {
		return deRrDatiSingRevDatiAggiuntiviRevoca;
	}

	/**
	 * @param deRrDatiSingRevDatiAggiuntiviRevoca the deRrDatiSingRevDatiAggiuntiviRevoca to set
	 */
	public void setDeRrDatiSingRevDatiAggiuntiviRevoca(String deRrDatiSingRevDatiAggiuntiviRevoca) {
		this.deRrDatiSingRevDatiAggiuntiviRevoca = deRrDatiSingRevDatiAggiuntiviRevoca;
	}

	/**
	 * @return the numErDatiSingRevSingoloImportoRevocato
	 */
	public BigDecimal getNumErDatiSingRevSingoloImportoRevocato() {
		return numErDatiSingRevSingoloImportoRevocato;
	}

	/**
	 * @return the codErDatiSingRevIdUnivocoRiscossione
	 */
	public String getCodErDatiSingRevIdUnivocoRiscossione() {
		return codErDatiSingRevIdUnivocoRiscossione;
	}

	/**
	 * @return the deErDatiSingRevCausaleRevoca
	 */
	public String getDeErDatiSingRevCausaleRevoca() {
		return deErDatiSingRevCausaleRevoca;
	}

	/**
	 * @return the deErDatiSingRevDatiAggiuntiviRevoca
	 */
	public String getDeErDatiSingRevDatiAggiuntiviRevoca() {
		return deErDatiSingRevDatiAggiuntiviRevoca;
	}

	/**
	 * @param numErDatiSingRevSingoloImportoRevocato the numErDatiSingRevSingoloImportoRevocato to set
	 */
	public void setNumErDatiSingRevSingoloImportoRevocato(BigDecimal numErDatiSingRevSingoloImportoRevocato) {
		this.numErDatiSingRevSingoloImportoRevocato = numErDatiSingRevSingoloImportoRevocato;
	}

	/**
	 * @param codErDatiSingRevIdUnivocoRiscossione the codErDatiSingRevIdUnivocoRiscossione to set
	 */
	public void setCodErDatiSingRevIdUnivocoRiscossione(String codErDatiSingRevIdUnivocoRiscossione) {
		this.codErDatiSingRevIdUnivocoRiscossione = codErDatiSingRevIdUnivocoRiscossione;
	}

	/**
	 * @param deErDatiSingRevCausaleRevoca the deErDatiSingRevCausaleRevoca to set
	 */
	public void setDeErDatiSingRevCausaleRevoca(String deErDatiSingRevCausaleRevoca) {
		this.deErDatiSingRevCausaleRevoca = deErDatiSingRevCausaleRevoca;
	}

	/**
	 * @param deErDatiSingRevDatiAggiuntiviRevoca the deErDatiSingRevDatiAggiuntiviRevoca to set
	 */
	public void setDeErDatiSingRevDatiAggiuntiviRevoca(String deErDatiSingRevDatiAggiuntiviRevoca) {
		this.deErDatiSingRevDatiAggiuntiviRevoca = deErDatiSingRevDatiAggiuntiviRevoca;
	}

	
}
