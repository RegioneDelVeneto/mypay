/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovGiornale;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * @author regione del veneto
 * 
 */
public interface GiornaleDao {

	Page<MygovGiornale> getGiornalePage(final String iuv, final String ente, final String te, final String ce, final String psp, final String esito,
			final Date from, final Date to, final int pageNumber, final int pageNumOfRecords, final String orderingField, final String sortingOrder)
			throws DataAccessException;

	MygovGiornale getGiornale(final String iuv) throws DataAccessException;

	MygovGiornale getGiornale(final long idGiornale) throws DataAccessException;

	List<String> getAllPspDistinct();

	void insertGiornale(final Date dataOraEvento, final String identificativoDominio, final String identificativoUnivocoVersamento,
			final String codiceContestoPagamento, final String identificativoPrestatoreServiziPagamento, final String tipoVersamento, final String componente,
			final String categoriaEvento, final String tipoEvento, final String sottoTipoEvento, final String identificativoFruitore,
			final String identificativoErogatore, final String identificativoStazioneIntermediarioPa, final String canalePagamento,
			final String parametriSpecificiInterfaccia, final String esito);
}
