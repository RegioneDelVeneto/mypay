package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import org.springframework.dao.DataAccessException;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRichiestaAvvisi;

import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtDatiPagamentoPA;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtFaultBean;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtNumeroAvviso;
import it.veneto.regione.schemas._2012.pagamenti.CtEnteBeneficiario;

public interface RichiestaAvvisiDao {

	MygovRichiestaAvvisi insertWithRefresh(String identificativoIntermediarioPa, String identificativoStazioneIntermediarioPA, String identificativoDominio, String identificativoPSP, String idServizio, String datiSpecificiServizio);
	
	void updateEsito(Long richiestaAvvisiId, String codEsito);
	
	void updateDatiPagamentoPa(Long richiestaAvvisiId, CtDatiPagamentoPA datiPagamentoPa);
	
	void updateFaultBean(Long richiestaAvvisiId, CtFaultBean faultBean);
	
	void updateNumeroAvviso(Long richiestaAvvisiId, CtNumeroAvviso numeroAvviso);
	
	void updateEnteBeneficiario(Long richiestaAvvisiId, CtEnteBeneficiario enteBeneficiario);
	
	
	MygovRichiestaAvvisi getById(Long richiestaAvvisiId) throws DataAccessException;
}
