package it.regioneveneto.mygov.payment.nodoregionalefesp.dto;

public class EnteDto {

	private String codIpa;
	private String codFiscale;
	private String nomeEnte;

	public EnteDto() {
		super();
	}

	public String getCodIpa() {
		return codIpa;
	}

	public void setCodIpa(String codIpa) {
		this.codIpa = codIpa;
	}

	public String getNomeEnte() {
		return nomeEnte;
	}

	public void setNomeEnte(String nomeEnte) {
		this.nomeEnte = nomeEnte;
	}

	public String getCodFiscale() {
		return codFiscale;
	}

	public void setCodFiscale(String codFiscale) {
		this.codFiscale = codFiscale;
	}

}
