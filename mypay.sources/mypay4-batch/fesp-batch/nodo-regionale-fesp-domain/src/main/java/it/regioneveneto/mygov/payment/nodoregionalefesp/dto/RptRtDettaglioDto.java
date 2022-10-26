/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author regione del veneto
 *
 */
public class RptRtDettaglioDto {

	private Long mygovRptRtDettaglioId;

	private BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento;

	private BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa;

	private String deRptDatiVersDatiSingVersIbanAccredito;

	private String deRptDatiVersDatiSingVersBicAccredito;

	private String deRptDatiVersDatiSingVersIbanAppoggio;

	private String deRptDatiVersDatiSingVersBicAppoggio;

	private String codRptDatiVersDatiSingVersCredenzialiPagatore;

	private String deRptDatiVersDatiSingVersCausaleVersamento;

	private String deRptDatiVersDatiSingVersDatiSpecificiRiscossione;

	private BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato;

	private String deRtDatiPagDatiSingPagEsitoSingoloPagamento;

	private Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento;

	private String codRtDatiPagDatiSingPagIdUnivocoRiscossione;

	private String deRtDatiPagDatiSingPagCausaleVersamento;

	private String deRtDatiPagDatiSingPagDatiSpecificiRiscossione;

	private BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp;

	private String codRtDatiPagDatiSingPagAllegatoRicevutaTipo;

	private byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest;

	private String codRptDatiVersDatiSingVersDatiMbdTipoBollo;

	private String codRptDatiVersDatiSingVersDatiMbdHashDocumento;

	private String codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza;

	/**
	 * 
	 */
	public RptRtDettaglioDto() {
		super();
	}

	/**
	 * @return the mygovRptRtDettaglioId
	 */
	public Long getMygovRptRtDettaglioId() {
		return mygovRptRtDettaglioId;
	}

	/**
	 * @param mygovRptRtDettaglioId the mygovRptRtDettaglioId to set
	 */
	public void setMygovRptRtDettaglioId(Long mygovRptRtDettaglioId) {
		this.mygovRptRtDettaglioId = mygovRptRtDettaglioId;
	}

	/**
	 * @return the numRptDatiVersDatiSingVersImportoSingoloVersamento
	 */
	public BigDecimal getNumRptDatiVersDatiSingVersImportoSingoloVersamento() {
		return numRptDatiVersDatiSingVersImportoSingoloVersamento;
	}

	/**
	 * @param numRptDatiVersDatiSingVersImportoSingoloVersamento the numRptDatiVersDatiSingVersImportoSingoloVersamento to set
	 */
	public void setNumRptDatiVersDatiSingVersImportoSingoloVersamento(BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento) {
		this.numRptDatiVersDatiSingVersImportoSingoloVersamento = numRptDatiVersDatiSingVersImportoSingoloVersamento;
	}

	/**
	 * @return the numRptDatiVersDatiSingVersCommissioneCaricoPa
	 */
	public BigDecimal getNumRptDatiVersDatiSingVersCommissioneCaricoPa() {
		return numRptDatiVersDatiSingVersCommissioneCaricoPa;
	}

	/**
	 * @param numRptDatiVersDatiSingVersCommissioneCaricoPa the numRptDatiVersDatiSingVersCommissioneCaricoPa to set
	 */
	public void setNumRptDatiVersDatiSingVersCommissioneCaricoPa(BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa) {
		this.numRptDatiVersDatiSingVersCommissioneCaricoPa = numRptDatiVersDatiSingVersCommissioneCaricoPa;
	}

	/**
	 * @return the deRptDatiVersDatiSingVersIbanAccredito
	 */
	public String getDeRptDatiVersDatiSingVersIbanAccredito() {
		return deRptDatiVersDatiSingVersIbanAccredito;
	}

	/**
	 * @param deRptDatiVersDatiSingVersIbanAccredito the deRptDatiVersDatiSingVersIbanAccredito to set
	 */
	public void setDeRptDatiVersDatiSingVersIbanAccredito(String deRptDatiVersDatiSingVersIbanAccredito) {
		this.deRptDatiVersDatiSingVersIbanAccredito = deRptDatiVersDatiSingVersIbanAccredito;
	}

	/**
	 * @return the deRptDatiVersDatiSingVersBicAccredito
	 */
	public String getDeRptDatiVersDatiSingVersBicAccredito() {
		return deRptDatiVersDatiSingVersBicAccredito;
	}

	/**
	 * @param deRptDatiVersDatiSingVersBicAccredito the deRptDatiVersDatiSingVersBicAccredito to set
	 */
	public void setDeRptDatiVersDatiSingVersBicAccredito(String deRptDatiVersDatiSingVersBicAccredito) {
		this.deRptDatiVersDatiSingVersBicAccredito = deRptDatiVersDatiSingVersBicAccredito;
	}

	/**
	 * @return the deRptDatiVersDatiSingVersIbanAppoggio
	 */
	public String getDeRptDatiVersDatiSingVersIbanAppoggio() {
		return deRptDatiVersDatiSingVersIbanAppoggio;
	}

	/**
	 * @param deRptDatiVersDatiSingVersIbanAppoggio the deRptDatiVersDatiSingVersIbanAppoggio to set
	 */
	public void setDeRptDatiVersDatiSingVersIbanAppoggio(String deRptDatiVersDatiSingVersIbanAppoggio) {
		this.deRptDatiVersDatiSingVersIbanAppoggio = deRptDatiVersDatiSingVersIbanAppoggio;
	}

	/**
	 * @return the deRptDatiVersDatiSingVersBicAppoggio
	 */
	public String getDeRptDatiVersDatiSingVersBicAppoggio() {
		return deRptDatiVersDatiSingVersBicAppoggio;
	}

	/**
	 * @param deRptDatiVersDatiSingVersBicAppoggio the deRptDatiVersDatiSingVersBicAppoggio to set
	 */
	public void setDeRptDatiVersDatiSingVersBicAppoggio(String deRptDatiVersDatiSingVersBicAppoggio) {
		this.deRptDatiVersDatiSingVersBicAppoggio = deRptDatiVersDatiSingVersBicAppoggio;
	}

	/**
	 * @return the codRptDatiVersDatiSingVersCredenzialiPagatore
	 */
	public String getCodRptDatiVersDatiSingVersCredenzialiPagatore() {
		return codRptDatiVersDatiSingVersCredenzialiPagatore;
	}

	/**
	 * @param codRptDatiVersDatiSingVersCredenzialiPagatore the codRptDatiVersDatiSingVersCredenzialiPagatore to set
	 */
	public void setCodRptDatiVersDatiSingVersCredenzialiPagatore(String codRptDatiVersDatiSingVersCredenzialiPagatore) {
		this.codRptDatiVersDatiSingVersCredenzialiPagatore = codRptDatiVersDatiSingVersCredenzialiPagatore;
	}

	/**
	 * @return the deRptDatiVersDatiSingVersCausaleVersamento
	 */
	public String getDeRptDatiVersDatiSingVersCausaleVersamento() {
		return deRptDatiVersDatiSingVersCausaleVersamento;
	}

	/**
	 * @param deRptDatiVersDatiSingVersCausaleVersamento the deRptDatiVersDatiSingVersCausaleVersamento to set
	 */
	public void setDeRptDatiVersDatiSingVersCausaleVersamento(String deRptDatiVersDatiSingVersCausaleVersamento) {
		this.deRptDatiVersDatiSingVersCausaleVersamento = deRptDatiVersDatiSingVersCausaleVersamento;
	}

	/**
	 * @return the deRptDatiVersDatiSingVersDatiSpecificiRiscossione
	 */
	public String getDeRptDatiVersDatiSingVersDatiSpecificiRiscossione() {
		return deRptDatiVersDatiSingVersDatiSpecificiRiscossione;
	}

	/**
	 * @param deRptDatiVersDatiSingVersDatiSpecificiRiscossione the deRptDatiVersDatiSingVersDatiSpecificiRiscossione to set
	 */
	public void setDeRptDatiVersDatiSingVersDatiSpecificiRiscossione(String deRptDatiVersDatiSingVersDatiSpecificiRiscossione) {
		this.deRptDatiVersDatiSingVersDatiSpecificiRiscossione = deRptDatiVersDatiSingVersDatiSpecificiRiscossione;
	}

	/**
	 * @return the numRtDatiPagDatiSingPagSingoloImportoPagato
	 */
	public BigDecimal getNumRtDatiPagDatiSingPagSingoloImportoPagato() {
		return numRtDatiPagDatiSingPagSingoloImportoPagato;
	}

	/**
	 * @param numRtDatiPagDatiSingPagSingoloImportoPagato the numRtDatiPagDatiSingPagSingoloImportoPagato to set
	 */
	public void setNumRtDatiPagDatiSingPagSingoloImportoPagato(BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato) {
		this.numRtDatiPagDatiSingPagSingoloImportoPagato = numRtDatiPagDatiSingPagSingoloImportoPagato;
	}

	/**
	 * @return the deRtDatiPagDatiSingPagEsitoSingoloPagamento
	 */
	public String getDeRtDatiPagDatiSingPagEsitoSingoloPagamento() {
		return deRtDatiPagDatiSingPagEsitoSingoloPagamento;
	}

	/**
	 * @param deRtDatiPagDatiSingPagEsitoSingoloPagamento the deRtDatiPagDatiSingPagEsitoSingoloPagamento to set
	 */
	public void setDeRtDatiPagDatiSingPagEsitoSingoloPagamento(String deRtDatiPagDatiSingPagEsitoSingoloPagamento) {
		this.deRtDatiPagDatiSingPagEsitoSingoloPagamento = deRtDatiPagDatiSingPagEsitoSingoloPagamento;
	}

	/**
	 * @return the dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento
	 */
	public Date getDtRtDatiPagDatiSingPagDataEsitoSingoloPagamento() {
		return dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento;
	}

	/**
	 * @param dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento the dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento to set
	 */
	public void setDtRtDatiPagDatiSingPagDataEsitoSingoloPagamento(Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento) {
		this.dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento = dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento;
	}

	/**
	 * @return the codRtDatiPagDatiSingPagIdUnivocoRiscossione
	 */
	public String getCodRtDatiPagDatiSingPagIdUnivocoRiscossione() {
		return codRtDatiPagDatiSingPagIdUnivocoRiscossione;
	}

	/**
	 * @param codRtDatiPagDatiSingPagIdUnivocoRiscossione the codRtDatiPagDatiSingPagIdUnivocoRiscossione to set
	 */
	public void setCodRtDatiPagDatiSingPagIdUnivocoRiscossione(String codRtDatiPagDatiSingPagIdUnivocoRiscossione) {
		this.codRtDatiPagDatiSingPagIdUnivocoRiscossione = codRtDatiPagDatiSingPagIdUnivocoRiscossione;
	}

	/**
	 * @return the deRtDatiPagDatiSingPagCausaleVersamento
	 */
	public String getDeRtDatiPagDatiSingPagCausaleVersamento() {
		return deRtDatiPagDatiSingPagCausaleVersamento;
	}

	/**
	 * @param deRtDatiPagDatiSingPagCausaleVersamento the deRtDatiPagDatiSingPagCausaleVersamento to set
	 */
	public void setDeRtDatiPagDatiSingPagCausaleVersamento(String deRtDatiPagDatiSingPagCausaleVersamento) {
		this.deRtDatiPagDatiSingPagCausaleVersamento = deRtDatiPagDatiSingPagCausaleVersamento;
	}

	/**
	 * @return the deRtDatiPagDatiSingPagDatiSpecificiRiscossione
	 */
	public String getDeRtDatiPagDatiSingPagDatiSpecificiRiscossione() {
		return deRtDatiPagDatiSingPagDatiSpecificiRiscossione;
	}

	/**
	 * @param deRtDatiPagDatiSingPagDatiSpecificiRiscossione the deRtDatiPagDatiSingPagDatiSpecificiRiscossione to set
	 */
	public void setDeRtDatiPagDatiSingPagDatiSpecificiRiscossione(String deRtDatiPagDatiSingPagDatiSpecificiRiscossione) {
		this.deRtDatiPagDatiSingPagDatiSpecificiRiscossione = deRtDatiPagDatiSingPagDatiSpecificiRiscossione;
	}

	/**
	 * @return the numRtDatiPagDatiSingPagCommissioniApplicatePsp
	 */
	public BigDecimal getNumRtDatiPagDatiSingPagCommissioniApplicatePsp() {
		return numRtDatiPagDatiSingPagCommissioniApplicatePsp;
	}

	/**
	 * @param numRtDatiPagDatiSingPagCommissioniApplicatePsp the numRtDatiPagDatiSingPagCommissioniApplicatePsp to set
	 */
	public void setNumRtDatiPagDatiSingPagCommissioniApplicatePsp(BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp) {
		this.numRtDatiPagDatiSingPagCommissioniApplicatePsp = numRtDatiPagDatiSingPagCommissioniApplicatePsp;
	}

	/**
	 * @return the codRtDatiPagDatiSingPagAllegatoRicevutaTipo
	 */
	public String getCodRtDatiPagDatiSingPagAllegatoRicevutaTipo() {
		return codRtDatiPagDatiSingPagAllegatoRicevutaTipo;
	}

	/**
	 * @param codRtDatiPagDatiSingPagAllegatoRicevutaTipo the codRtDatiPagDatiSingPagAllegatoRicevutaTipo to set
	 */
	public void setCodRtDatiPagDatiSingPagAllegatoRicevutaTipo(String codRtDatiPagDatiSingPagAllegatoRicevutaTipo) {
		this.codRtDatiPagDatiSingPagAllegatoRicevutaTipo = codRtDatiPagDatiSingPagAllegatoRicevutaTipo;
	}

	/**
	 * @return the blbRtDatiPagDatiSingPagAllegatoRicevutaTest
	 */
	public byte[] getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest() {
		return blbRtDatiPagDatiSingPagAllegatoRicevutaTest;
	}

	/**
	 * @param blbRtDatiPagDatiSingPagAllegatoRicevutaTest the blbRtDatiPagDatiSingPagAllegatoRicevutaTest to set
	 */
	public void setBlbRtDatiPagDatiSingPagAllegatoRicevutaTest(byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest) {
		this.blbRtDatiPagDatiSingPagAllegatoRicevutaTest = blbRtDatiPagDatiSingPagAllegatoRicevutaTest;
	}

	public String getCodRptDatiVersDatiSingVersDatiMbdTipoBollo() {
		return codRptDatiVersDatiSingVersDatiMbdTipoBollo;
	}

	public void setCodRptDatiVersDatiSingVersDatiMbdTipoBollo(String codRptDatiVersDatiSingVersDatiMbdTipoBollo) {
		this.codRptDatiVersDatiSingVersDatiMbdTipoBollo = codRptDatiVersDatiSingVersDatiMbdTipoBollo;
	}

	public String getCodRptDatiVersDatiSingVersDatiMbdHashDocumento() {
		return codRptDatiVersDatiSingVersDatiMbdHashDocumento;
	}

	public void setCodRptDatiVersDatiSingVersDatiMbdHashDocumento(String codRptDatiVersDatiSingVersDatiMbdHashDocumento) {
		this.codRptDatiVersDatiSingVersDatiMbdHashDocumento = codRptDatiVersDatiSingVersDatiMbdHashDocumento;
	}

	public String getCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza() {
		return codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza;
	}

	public void setCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza(String codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza) {
		this.codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza = codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza;
	}

}
