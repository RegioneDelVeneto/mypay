package it.regioneveneto.mygov.payment.nodoregionalefesp.dto;


public class GiornaleDto {

	private Long id;
	private String dataOraEvento;
	private String identificativoUnivocoVersamento;
	private String tipoEvento;

	private String identificativoDominio;
	private String identificativoPrestatoreServiziPagamento;
	private String categoriaEvento;
	private String esito;

	private String codiceContestoPagamento;
	private String tipoVersamento;
	private String componente;
	private String sottoTipoEvento;
	private String identificativoFruitore;
	private String identificativoErogatore;
	private String identificativoStazioneIntermediarioPa;
	private String canalePagamento;
	private String parametriSpecificiInterfaccia;

	public GiornaleDto() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDataOraEvento() {
		return dataOraEvento;
	}

	public void setDataOraEvento(String dataOraEvento) {
		this.dataOraEvento = dataOraEvento;
	}

	public String getIdentificativoUnivocoVersamento() {
		return identificativoUnivocoVersamento;
	}

	public void setIdentificativoUnivocoVersamento(String identificativoUnivocoVersamento) {
		this.identificativoUnivocoVersamento = identificativoUnivocoVersamento;
	}

	public String getTipoEvento() {
		return tipoEvento;
	}

	public void setTipoEvento(String tipoEvento) {
		this.tipoEvento = tipoEvento;
	}

	public String getIdentificativoDominio() {
		return identificativoDominio;
	}

	public void setIdentificativoDominio(String identificativoDominio) {
		this.identificativoDominio = identificativoDominio;
	}

	public String getIdentificativoPrestatoreServiziPagamento() {
		return identificativoPrestatoreServiziPagamento;
	}

	public void setIdentificativoPrestatoreServiziPagamento(String identificativoPrestatoreServiziPagamento) {
		this.identificativoPrestatoreServiziPagamento = identificativoPrestatoreServiziPagamento;
	}

	public String getCategoriaEvento() {
		return categoriaEvento;
	}

	public void setCategoriaEvento(String categoriaEvento) {
		this.categoriaEvento = categoriaEvento;
	}

	public String getEsito() {
		return esito;
	}

	public void setEsito(String esito) {
		this.esito = esito;
	}

	public String getCodiceContestoPagamento() {
		return codiceContestoPagamento;
	}

	public void setCodiceContestoPagamento(String codiceContestoPagamento) {
		this.codiceContestoPagamento = codiceContestoPagamento;
	}

	public String getTipoVersamento() {
		return tipoVersamento;
	}

	public void setTipoVersamento(String tipoVersamento) {
		this.tipoVersamento = tipoVersamento;
	}

	public String getComponente() {
		return componente;
	}

	public void setComponente(String componente) {
		this.componente = componente;
	}

	public String getSottoTipoEvento() {
		return sottoTipoEvento;
	}

	public void setSottoTipoEvento(String sottoTipoEvento) {
		this.sottoTipoEvento = sottoTipoEvento;
	}

	public String getIdentificativoFruitore() {
		return identificativoFruitore;
	}

	public void setIdentificativoFruitore(String identificativoFruitore) {
		this.identificativoFruitore = identificativoFruitore;
	}

	public String getIdentificativoErogatore() {
		return identificativoErogatore;
	}

	public void setIdentificativoErogatore(String identificativoErogatore) {
		this.identificativoErogatore = identificativoErogatore;
	}

	public String getIdentificativoStazioneIntermediarioPa() {
		return identificativoStazioneIntermediarioPa;
	}

	public void setIdentificativoStazioneIntermediarioPa(String identificativoStazioneIntermediarioPa) {
		this.identificativoStazioneIntermediarioPa = identificativoStazioneIntermediarioPa;
	}

	public String getCanalePagamento() {
		return canalePagamento;
	}

	public void setCanalePagamento(String canalePagamento) {
		this.canalePagamento = canalePagamento;
	}

	public String getParametriSpecificiInterfaccia() {
		return parametriSpecificiInterfaccia;
	}

	public void setParametriSpecificiInterfaccia(String parametriSpecificiInterfaccia) {
		this.parametriSpecificiInterfaccia = parametriSpecificiInterfaccia;
	}

}
