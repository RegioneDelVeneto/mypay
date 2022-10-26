package it.regioneveneto.mygov.payment.nodoregionalefesp.domain;
// Generated Jul 14, 2017 12:18:12 PM by Hibernate Tools 3.5.0.Final

import java.util.Date;

/**
 * MygovIuv generated by hbm2java
 */
public class MygovIuv implements java.io.Serializable {

	private Long id;

	private int version;

	private String iuv;

	private String tipoVersamento;

	private String codIpaEnte;

	private Date dtCreazione;

	public MygovIuv() {
	}

	public MygovIuv(String codIpaEnte, Date dtCreazione) {
		this.codIpaEnte = codIpaEnte;
		this.dtCreazione = dtCreazione;
	}

	public MygovIuv(String iuv, String tipoVersamento, String codIpaEnte, Date dtCreazione) {
		this.iuv = iuv;
		this.tipoVersamento = tipoVersamento;
		this.codIpaEnte = codIpaEnte;
		this.dtCreazione = dtCreazione;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getIuv() {
		return this.iuv;
	}

	public void setIuv(String iuv) {
		this.iuv = iuv;
	}

	public String getTipoVersamento() {
		return this.tipoVersamento;
	}

	public void setTipoVersamento(String tipoVersamento) {
		this.tipoVersamento = tipoVersamento;
	}

	public String getCodIpaEnte() {
		return this.codIpaEnte;
	}

	public void setCodIpaEnte(String codIpaEnte) {
		this.codIpaEnte = codIpaEnte;
	}

	public Date getDtCreazione() {
		return this.dtCreazione;
	}

	public void setDtCreazione(Date dtCreazione) {
		this.dtCreazione = dtCreazione;
	}

}