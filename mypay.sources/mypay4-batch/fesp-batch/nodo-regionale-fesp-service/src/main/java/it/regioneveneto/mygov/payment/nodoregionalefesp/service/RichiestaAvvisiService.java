package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRichiestaAvvisi;

import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtDatiPagamentoPA;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtFaultBean;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtNumeroAvviso;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface RichiestaAvvisiService {

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRichiestaAvvisi creaRichiestaAvvisi(String identificativoIntermediarioPa, String identificativoStazioneIntermediarioPA, String identificativoDominio, String identificativoPSP, String idServizio, String datiSpecificiServizio);
	
	@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = false)
	void updateRispostaPA(Long richiestaAvvisiId, String codEsito, CtDatiPagamentoPA datiPagamentoPa, CtNumeroAvviso numeroAvviso, CtFaultBean faultBean);
}
