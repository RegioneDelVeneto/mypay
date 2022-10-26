package it.regioneveneto.mygov.payment.nodoregionalefesp.dto;

import java.util.List;

public class EsitoAvvisoDigitaleCompletoDto {

	// HEADER
	private String identificativoDominioHeader;
	private String identificativoIntermediarioPAHeader;
	private String identificativoStazioneIntermediarioPAHeader;

	// FAULT BEAN
	private FaultBeanDto faultBeanDto;

	// ESITO OPERAZIONE
	private String esitoOperazione;

	// ESITO AVVISO
	private String identificativoDominio;
	private String identificativoMessaggioRichiesta;
	private List<EsitoAvvisoDigitaleDto> listaEsitiAvvisiDigitali;

	public String getIdentificativoDominioHeader() {
		return identificativoDominioHeader;
	}

	public void setIdentificativoDominioHeader(String identificativoDominioHeader) {
		this.identificativoDominioHeader = identificativoDominioHeader;
	}

	public String getIdentificativoIntermediarioPAHeader() {
		return identificativoIntermediarioPAHeader;
	}

	public void setIdentificativoIntermediarioPAHeader(String identificativoIntermediarioPAHeader) {
		this.identificativoIntermediarioPAHeader = identificativoIntermediarioPAHeader;
	}

	public String getIdentificativoStazioneIntermediarioPAHeader() {
		return identificativoStazioneIntermediarioPAHeader;
	}

	public void setIdentificativoStazioneIntermediarioPAHeader(String identificativoStazioneIntermediarioPAHeader) {
		this.identificativoStazioneIntermediarioPAHeader = identificativoStazioneIntermediarioPAHeader;
	}

	public FaultBeanDto getFaultBeanDto() {
		return faultBeanDto;
	}

	public void setFaultBeanDto(FaultBeanDto faultBeanDto) {
		this.faultBeanDto = faultBeanDto;
	}

	public String getEsitoOperazione() {
		return esitoOperazione;
	}

	public void setEsitoOperazione(String esitoOperazione) {
		this.esitoOperazione = esitoOperazione;
	}

	public String getIdentificativoDominio() {
		return identificativoDominio;
	}

	public void setIdentificativoDominio(String identificativoDominio) {
		this.identificativoDominio = identificativoDominio;
	}

	public String getIdentificativoMessaggioRichiesta() {
		return identificativoMessaggioRichiesta;
	}

	public void setIdentificativoMessaggioRichiesta(String identificativoMessaggioRichiesta) {
		this.identificativoMessaggioRichiesta = identificativoMessaggioRichiesta;
	}

	public List<EsitoAvvisoDigitaleDto> getListaEsitiAvvisiDigitali() {
		return listaEsitiAvvisiDigitali;
	}

	public void setListaEsitiAvvisiDigitali(List<EsitoAvvisoDigitaleDto> listaEsitiAvvisiDigitali) {
		this.listaEsitiAvvisiDigitali = listaEsitiAvvisiDigitali;
	}

}
