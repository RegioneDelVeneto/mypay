package it.regioneveneto.mygov.payment.nodoregionalefesp.domain;
// Generated Jul 17, 2017 3:10:37 PM by Hibernate Tools 3.5.0.Final

import java.util.Date;

/**
 * MygovFlussoRendSpc generated by hbm2java
 */
public class MygovFlussoRendSpc implements java.io.Serializable {

	private Long mygovFlussoRendSpcId;

	private int version;

	private String codIpaEnte;

	private String identificativoPsp;

	private String codIdentificativoFlusso;

	private Date dtDataOraFlusso;

	private String deNomeFileScaricato;

	private Long numDimensioneFileScaricato;

	private Date dtCreazione;

	private Date dtUltimaModifica;

	private String codStato;

	public MygovFlussoRendSpc() {
	}

	public MygovFlussoRendSpc(String codIpaEnte, String identificativoPsp, String codIdentificativoFlusso, Date dtDataOraFlusso, Date dtCreazione,
			Date dtUltimaModifica, String codStato) {
		this.codIpaEnte = codIpaEnte;
		this.identificativoPsp = identificativoPsp;
		this.codIdentificativoFlusso = codIdentificativoFlusso;
		this.dtDataOraFlusso = dtDataOraFlusso;
		this.dtCreazione = dtCreazione;
		this.dtUltimaModifica = dtUltimaModifica;
		this.codStato = codStato;
	}

	public MygovFlussoRendSpc(String codIpaEnte, String identificativoPsp, String codIdentificativoFlusso, Date dtDataOraFlusso, String deNomeFileScaricato,
			Long numDimensioneFileScaricato, Date dtCreazione, Date dtUltimaModifica, String codStato) {
		this.codIpaEnte = codIpaEnte;
		this.identificativoPsp = identificativoPsp;
		this.codIdentificativoFlusso = codIdentificativoFlusso;
		this.dtDataOraFlusso = dtDataOraFlusso;
		this.deNomeFileScaricato = deNomeFileScaricato;
		this.numDimensioneFileScaricato = numDimensioneFileScaricato;
		this.dtCreazione = dtCreazione;
		this.dtUltimaModifica = dtUltimaModifica;
		this.codStato = codStato;
	}

	public Long getMygovFlussoRendSpcId() {
		return this.mygovFlussoRendSpcId;
	}

	public void setMygovFlussoRendSpcId(Long mygovFlussoRendSpcId) {
		this.mygovFlussoRendSpcId = mygovFlussoRendSpcId;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getCodIpaEnte() {
		return this.codIpaEnte;
	}

	public void setCodIpaEnte(String codIpaEnte) {
		this.codIpaEnte = codIpaEnte;
	}

	public String getIdentificativoPsp() {
		return this.identificativoPsp;
	}

	public void setIdentificativoPsp(String identificativoPsp) {
		this.identificativoPsp = identificativoPsp;
	}

	public String getCodIdentificativoFlusso() {
		return this.codIdentificativoFlusso;
	}

	public void setCodIdentificativoFlusso(String codIdentificativoFlusso) {
		this.codIdentificativoFlusso = codIdentificativoFlusso;
	}

	public Date getDtDataOraFlusso() {
		return this.dtDataOraFlusso;
	}

	public void setDtDataOraFlusso(Date dtDataOraFlusso) {
		this.dtDataOraFlusso = dtDataOraFlusso;
	}

	public String getDeNomeFileScaricato() {
		return this.deNomeFileScaricato;
	}

	public void setDeNomeFileScaricato(String deNomeFileScaricato) {
		this.deNomeFileScaricato = deNomeFileScaricato;
	}

	public Long getNumDimensioneFileScaricato() {
		return this.numDimensioneFileScaricato;
	}

	public void setNumDimensioneFileScaricato(Long numDimensioneFileScaricato) {
		this.numDimensioneFileScaricato = numDimensioneFileScaricato;
	}

	public Date getDtCreazione() {
		return this.dtCreazione;
	}

	public void setDtCreazione(Date dtCreazione) {
		this.dtCreazione = dtCreazione;
	}

	public Date getDtUltimaModifica() {
		return this.dtUltimaModifica;
	}

	public void setDtUltimaModifica(Date dtUltimaModifica) {
		this.dtUltimaModifica = dtUltimaModifica;
	}

	public String getCodStato() {
		return this.codStato;
	}

	public void setCodStato(String codStato) {
		this.codStato = codStato;
	}

}
