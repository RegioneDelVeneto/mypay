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
public class RpEDettaglioDto {

	private Long mygovRpEDettaglioId;

	private BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento;

	private BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa;

	private String codRpDatiVersDatiSingVersIbanAccredito;

	private String codRpDatiVersDatiSingVersBicAccredito;

	private String codRpDatiVersDatiSingVersIbanAppoggio;

	private String codRpDatiVersDatiSingVersBicAppoggio;

	private String codRpDatiVersDatiSingVersCredenzialiPagatore;

	private String deRpDatiVersDatiSingVersCausaleVersamento;

	private String deRpDatiVersDatiSingVersDatiSpecificiRiscossione;

	private BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato;

	private String deEDatiPagDatiSingPagEsitoSingoloPagamento;

	private Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento;

	private String codEDatiPagDatiSingPagIdUnivocoRiscoss;

	private String deEDatiPagDatiSingPagCausaleVersamento;

	private String deEDatiPagDatiSingPagDatiSpecificiRiscossione;

	private BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp;

	private String codRpDatiVersDatiSingVersDatiMbdTipoBollo;

	private String codRpDatiVersDatiSingVersDatiMbdHashDocumento;

	private String codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza;

	/**
	 * @return the numEDatiPagDatiSingPagCommissioniApplicatePsp
	 */
	public BigDecimal getNumEDatiPagDatiSingPagCommissioniApplicatePsp() {
		return numEDatiPagDatiSingPagCommissioniApplicatePsp;
	}

	/**
	 * @param numEDatiPagDatiSingPagCommissioniApplicatePsp the numEDatiPagDatiSingPagCommissioniApplicatePsp to set
	 */
	public void setNumEDatiPagDatiSingPagCommissioniApplicatePsp(BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp) {
		this.numEDatiPagDatiSingPagCommissioniApplicatePsp = numEDatiPagDatiSingPagCommissioniApplicatePsp;
	}

	/**
	 * @return the codEDatiPagDatiSingPagAllegatoRicevutaTipo
	 */
	public String getCodEDatiPagDatiSingPagAllegatoRicevutaTipo() {
		return codEDatiPagDatiSingPagAllegatoRicevutaTipo;
	}

	/**
	 * @param codEDatiPagDatiSingPagAllegatoRicevutaTipo the codEDatiPagDatiSingPagAllegatoRicevutaTipo to set
	 */
	public void setCodEDatiPagDatiSingPagAllegatoRicevutaTipo(String codEDatiPagDatiSingPagAllegatoRicevutaTipo) {
		this.codEDatiPagDatiSingPagAllegatoRicevutaTipo = codEDatiPagDatiSingPagAllegatoRicevutaTipo;
	}

	/**
	 * @return the blbEDatiPagDatiSingPagAllegatoRicevutaTest
	 */
	public byte[] getBlbEDatiPagDatiSingPagAllegatoRicevutaTest() {
		return blbEDatiPagDatiSingPagAllegatoRicevutaTest;
	}

	/**
	 * @param blbEDatiPagDatiSingPagAllegatoRicevutaTest the blbEDatiPagDatiSingPagAllegatoRicevutaTest to set
	 */
	public void setBlbEDatiPagDatiSingPagAllegatoRicevutaTest(byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest) {
		this.blbEDatiPagDatiSingPagAllegatoRicevutaTest = blbEDatiPagDatiSingPagAllegatoRicevutaTest;
	}

	private String codEDatiPagDatiSingPagAllegatoRicevutaTipo;

	private byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest;

	/**
	 * 
	 */
	public RpEDettaglioDto() {
		super();
	}

	/**
	 * @return the mygovRpEDettaglioId
	 */
	public Long getMygovRpEDettaglioId() {
		return mygovRpEDettaglioId;
	}

	/**
	 * @param mygovRpEDettaglioId the mygovRpEDettaglioId to set
	 */
	public void setMygovRpEDettaglioId(Long mygovRpEDettaglioId) {
		this.mygovRpEDettaglioId = mygovRpEDettaglioId;
	}

	/**
	 * @return the numRpDatiVersDatiSingVersImportoSingoloVersamento
	 */
	public BigDecimal getNumRpDatiVersDatiSingVersImportoSingoloVersamento() {
		return numRpDatiVersDatiSingVersImportoSingoloVersamento;
	}

	/**
	 * @param numRpDatiVersDatiSingVersImportoSingoloVersamento the numRpDatiVersDatiSingVersImportoSingoloVersamento to set
	 */
	public void setNumRpDatiVersDatiSingVersImportoSingoloVersamento(BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento) {
		this.numRpDatiVersDatiSingVersImportoSingoloVersamento = numRpDatiVersDatiSingVersImportoSingoloVersamento;
	}

	/**
	 * @return the numRpDatiVersDatiSingVersCommissioneCaricoPa
	 */
	public BigDecimal getNumRpDatiVersDatiSingVersCommissioneCaricoPa() {
		return numRpDatiVersDatiSingVersCommissioneCaricoPa;
	}

	/**
	 * @param numRpDatiVersDatiSingVersCommissioneCaricoPa the numRpDatiVersDatiSingVersCommissioneCaricoPa to set
	 */
	public void setNumRpDatiVersDatiSingVersCommissioneCaricoPa(BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa) {
		this.numRpDatiVersDatiSingVersCommissioneCaricoPa = numRpDatiVersDatiSingVersCommissioneCaricoPa;
	}

	/**
	 * @return the codRpDatiVersDatiSingVersIbanAccredito
	 */
	public String getCodRpDatiVersDatiSingVersIbanAccredito() {
		return codRpDatiVersDatiSingVersIbanAccredito;
	}

	/**
	 * @param codRpDatiVersDatiSingVersIbanAccredito the codRpDatiVersDatiSingVersIbanAccredito to set
	 */
	public void setCodRpDatiVersDatiSingVersIbanAccredito(String codRpDatiVersDatiSingVersIbanAccredito) {
		this.codRpDatiVersDatiSingVersIbanAccredito = codRpDatiVersDatiSingVersIbanAccredito;
	}

	/**
	 * @return the codRpDatiVersDatiSingVersBicAccredito
	 */
	public String getCodRpDatiVersDatiSingVersBicAccredito() {
		return codRpDatiVersDatiSingVersBicAccredito;
	}

	/**
	 * @param codRpDatiVersDatiSingVersBicAccredito the codRpDatiVersDatiSingVersBicAccredito to set
	 */
	public void setCodRpDatiVersDatiSingVersBicAccredito(String codRpDatiVersDatiSingVersBicAccredito) {
		this.codRpDatiVersDatiSingVersBicAccredito = codRpDatiVersDatiSingVersBicAccredito;
	}

	/**
	 * @return the codRpDatiVersDatiSingVersIbanAppoggio
	 */
	public String getCodRpDatiVersDatiSingVersIbanAppoggio() {
		return codRpDatiVersDatiSingVersIbanAppoggio;
	}

	/**
	 * @param codRpDatiVersDatiSingVersIbanAppoggio the codRpDatiVersDatiSingVersIbanAppoggio to set
	 */
	public void setCodRpDatiVersDatiSingVersIbanAppoggio(String codRpDatiVersDatiSingVersIbanAppoggio) {
		this.codRpDatiVersDatiSingVersIbanAppoggio = codRpDatiVersDatiSingVersIbanAppoggio;
	}

	/**
	 * @return the codRpDatiVersDatiSingVersBicAppoggio
	 */
	public String getCodRpDatiVersDatiSingVersBicAppoggio() {
		return codRpDatiVersDatiSingVersBicAppoggio;
	}

	/**
	 * @param codRpDatiVersDatiSingVersBicAppoggio the codRpDatiVersDatiSingVersBicAppoggio to set
	 */
	public void setCodRpDatiVersDatiSingVersBicAppoggio(String codRpDatiVersDatiSingVersBicAppoggio) {
		this.codRpDatiVersDatiSingVersBicAppoggio = codRpDatiVersDatiSingVersBicAppoggio;
	}

	/**
	 * @return the codRpDatiVersDatiSingVersCredenzialiPagatore
	 */
	public String getCodRpDatiVersDatiSingVersCredenzialiPagatore() {
		return codRpDatiVersDatiSingVersCredenzialiPagatore;
	}

	/**
	 * @param codRpDatiVersDatiSingVersCredenzialiPagatore the codRpDatiVersDatiSingVersCredenzialiPagatore to set
	 */
	public void setCodRpDatiVersDatiSingVersCredenzialiPagatore(String codRpDatiVersDatiSingVersCredenzialiPagatore) {
		this.codRpDatiVersDatiSingVersCredenzialiPagatore = codRpDatiVersDatiSingVersCredenzialiPagatore;
	}

	/**
	 * @return the deRpDatiVersDatiSingVersCausaleVersamento
	 */
	public String getDeRpDatiVersDatiSingVersCausaleVersamento() {
		return deRpDatiVersDatiSingVersCausaleVersamento;
	}

	/**
	 * @param deRpDatiVersDatiSingVersCausaleVersamento the deRpDatiVersDatiSingVersCausaleVersamento to set
	 */
	public void setDeRpDatiVersDatiSingVersCausaleVersamento(String deRpDatiVersDatiSingVersCausaleVersamento) {
		this.deRpDatiVersDatiSingVersCausaleVersamento = deRpDatiVersDatiSingVersCausaleVersamento;
	}

	/**
	 * @return the deRpDatiVersDatiSingVersDatiSpecificiRiscossione
	 */
	public String getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione() {
		return deRpDatiVersDatiSingVersDatiSpecificiRiscossione;
	}

	/**
	 * @param deRpDatiVersDatiSingVersDatiSpecificiRiscossione the deRpDatiVersDatiSingVersDatiSpecificiRiscossione to set
	 */
	public void setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(String deRpDatiVersDatiSingVersDatiSpecificiRiscossione) {
		this.deRpDatiVersDatiSingVersDatiSpecificiRiscossione = deRpDatiVersDatiSingVersDatiSpecificiRiscossione;
	}

	/**
	 * @return the numEDatiPagDatiSingPagSingoloImportoPagato
	 */
	public BigDecimal getNumEDatiPagDatiSingPagSingoloImportoPagato() {
		return numEDatiPagDatiSingPagSingoloImportoPagato;
	}

	/**
	 * @param numEDatiPagDatiSingPagSingoloImportoPagato the numEDatiPagDatiSingPagSingoloImportoPagato to set
	 */
	public void setNumEDatiPagDatiSingPagSingoloImportoPagato(BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato) {
		this.numEDatiPagDatiSingPagSingoloImportoPagato = numEDatiPagDatiSingPagSingoloImportoPagato;
	}

	/**
	 * @return the deEDatiPagDatiSingPagEsitoSingoloPagamento
	 */
	public String getDeEDatiPagDatiSingPagEsitoSingoloPagamento() {
		return deEDatiPagDatiSingPagEsitoSingoloPagamento;
	}

	/**
	 * @param deEDatiPagDatiSingPagEsitoSingoloPagamento the deEDatiPagDatiSingPagEsitoSingoloPagamento to set
	 */
	public void setDeEDatiPagDatiSingPagEsitoSingoloPagamento(String deEDatiPagDatiSingPagEsitoSingoloPagamento) {
		this.deEDatiPagDatiSingPagEsitoSingoloPagamento = deEDatiPagDatiSingPagEsitoSingoloPagamento;
	}

	/**
	 * @return the dtEDatiPagDatiSingPagDataEsitoSingoloPagamento
	 */
	public Date getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento() {
		return dtEDatiPagDatiSingPagDataEsitoSingoloPagamento;
	}

	/**
	 * @param dtEDatiPagDatiSingPagDataEsitoSingoloPagamento the dtEDatiPagDatiSingPagDataEsitoSingoloPagamento to set
	 */
	public void setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento) {
		this.dtEDatiPagDatiSingPagDataEsitoSingoloPagamento = dtEDatiPagDatiSingPagDataEsitoSingoloPagamento;
	}

	/**
	 * @return the codEDatiPagDatiSingPagIdUnivocoRiscoss
	 */
	public String getCodEDatiPagDatiSingPagIdUnivocoRiscoss() {
		return codEDatiPagDatiSingPagIdUnivocoRiscoss;
	}

	/**
	 * @param codEDatiPagDatiSingPagIdUnivocoRiscoss the codEDatiPagDatiSingPagIdUnivocoRiscoss to set
	 */
	public void setCodEDatiPagDatiSingPagIdUnivocoRiscoss(String codEDatiPagDatiSingPagIdUnivocoRiscoss) {
		this.codEDatiPagDatiSingPagIdUnivocoRiscoss = codEDatiPagDatiSingPagIdUnivocoRiscoss;
	}

	/**
	 * @return the deEDatiPagDatiSingPagCausaleVersamento
	 */
	public String getDeEDatiPagDatiSingPagCausaleVersamento() {
		return deEDatiPagDatiSingPagCausaleVersamento;
	}

	/**
	 * @param deEDatiPagDatiSingPagCausaleVersamento the deEDatiPagDatiSingPagCausaleVersamento to set
	 */
	public void setDeEDatiPagDatiSingPagCausaleVersamento(String deEDatiPagDatiSingPagCausaleVersamento) {
		this.deEDatiPagDatiSingPagCausaleVersamento = deEDatiPagDatiSingPagCausaleVersamento;
	}

	/**
	 * @return the deEDatiPagDatiSingPagDatiSpecificiRiscossione
	 */
	public String getDeEDatiPagDatiSingPagDatiSpecificiRiscossione() {
		return deEDatiPagDatiSingPagDatiSpecificiRiscossione;
	}

	/**
	 * @param deEDatiPagDatiSingPagDatiSpecificiRiscossione the deEDatiPagDatiSingPagDatiSpecificiRiscossione to set
	 */
	public void setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(String deEDatiPagDatiSingPagDatiSpecificiRiscossione) {
		this.deEDatiPagDatiSingPagDatiSpecificiRiscossione = deEDatiPagDatiSingPagDatiSpecificiRiscossione;
	}

	public String getCodRpDatiVersDatiSingVersDatiMbdTipoBollo() {
		return codRpDatiVersDatiSingVersDatiMbdTipoBollo;
	}

	public void setCodRpDatiVersDatiSingVersDatiMbdTipoBollo(String codRpDatiVersDatiSingVersDatiMbdTipoBollo) {
		this.codRpDatiVersDatiSingVersDatiMbdTipoBollo = codRpDatiVersDatiSingVersDatiMbdTipoBollo;
	}

	public String getCodRpDatiVersDatiSingVersDatiMbdHashDocumento() {
		return codRpDatiVersDatiSingVersDatiMbdHashDocumento;
	}

	public void setCodRpDatiVersDatiSingVersDatiMbdHashDocumento(String codRpDatiVersDatiSingVersDatiMbdHashDocumento) {
		this.codRpDatiVersDatiSingVersDatiMbdHashDocumento = codRpDatiVersDatiSingVersDatiMbdHashDocumento;
	}

	public String getCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza() {
		return codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza;
	}

	public void setCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza(String codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza) {
		this.codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza = codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza;
	}

}
