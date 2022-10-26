package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataAccessException;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrErDettaglio;

/**
 * 
 * @author regione del veneto
 *
 */
public interface RrErDettaglioDao {
	
	MygovRrErDettaglio insert(MygovRrEr rr, String codRrDatiSingRevIdUnivocoRiscossione, String deRrDatiSingRevCausaleRevoca, 
			String deRrDatiSingRevDatiAggiuntiviRevoca, BigDecimal numRrDatiSingRevSingoloImportoRevocato) throws DataAccessException;

	List<MygovRrErDettaglio> getByRevoca(MygovRrEr mygovRevoca) throws DataAccessException;

	MygovRrErDettaglio updateEsitoDettaglioRevoca(Long mygovRevocaDettaglioPagamentiId, int version, MygovRrEr mygovRevoca,
			String codErDatiSingRevIdUnivocoRiscossione, String deErDatiSingRevCausaleRevoca,
			String deErDatiSingRevDatiAggiuntiviRevoca, BigDecimal numErDatiSingRevSingoloImportoRevocato) throws DataAccessException;

}



