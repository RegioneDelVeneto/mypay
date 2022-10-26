package it.regioneveneto.mygov.payment.nodoregionalefesp.dto;

import javax.xml.datatype.XMLGregorianCalendar;

public class EsitoAvvisoDigitaleDto {

	private String tipoCanaleEsito;
	private String identificativoCanale;
	private XMLGregorianCalendar dataEsito;
	private Integer codiceEsito;
	private String descrizioneEsito;

	public String getTipoCanaleEsito() {
		return tipoCanaleEsito;
	}

	public void setTipoCanaleEsito(String tipoCanaleEsito) {
		this.tipoCanaleEsito = tipoCanaleEsito;
	}

	public String getIdentificativoCanale() {
		return identificativoCanale;
	}

	public void setIdentificativoCanale(String identificativoCanale) {
		this.identificativoCanale = identificativoCanale;
	}

	public XMLGregorianCalendar getDataEsito() {
		return dataEsito;
	}

	public void setDataEsito(XMLGregorianCalendar dataEsito) {
		this.dataEsito = dataEsito;
	}

	public Integer getCodiceEsito() {
		return codiceEsito;
	}

	public void setCodiceEsito(Integer codiceEsito) {
		this.codiceEsito = codiceEsito;
	}

	public String getDescrizioneEsito() {
		return descrizioneEsito;
	}

	public void setDescrizioneEsito(String descrizioneEsito) {
		this.descrizioneEsito = descrizioneEsito;
	}

}
