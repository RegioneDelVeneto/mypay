package it.regioneveneto.mygov.payment.nodoregionalefesp.to;

import java.util.Date;

public class UtenteTO implements java.io.Serializable {

	private Date dtUltimoLogin;
	private int version;
	private String deLastname;
	private String deEmailAddress;
	private String codFedUserId;
	private String deFirstname;
	private Long id;
	private String codCodiceFiscaleUtente;

	public Date getDtUltimoLogin() {
		return dtUltimoLogin;
	}

	public void setDtUltimoLogin(Date dtUltimoLogin) {
		this.dtUltimoLogin = dtUltimoLogin;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getDeLastname() {
		return deLastname;
	}

	public void setDeLastname(String deLastname) {
		this.deLastname = deLastname;
	}

	public String getDeEmailAddress() {
		return deEmailAddress;
	}

	public void setDeEmailAddress(String deEmailAddress) {
		this.deEmailAddress = deEmailAddress;
	}

	public String getCodFedUserId() {
		return codFedUserId;
	}

	public void setCodFedUserId(String codFedUserId) {
		this.codFedUserId = codFedUserId;
	}

	public String getDeFirstname() {
		return deFirstname;
	}

	public void setDeFirstname(String deFirstname) {
		this.deFirstname = deFirstname;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodCodiceFiscaleUtente() {
		return codCodiceFiscaleUtente;
	}

	public void setCodCodiceFiscaleUtente(String codCodiceFiscaleUtente) {
		this.codCodiceFiscaleUtente = codCodiceFiscaleUtente;
	}

}