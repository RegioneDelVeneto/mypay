package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoQuadSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

public interface FlussoQuadSpcDao {
	

	
	/**
	 * @param codiceIpaEnte
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @param dtCreazione
	 */
	void insert(final String codiceIpaEnte, final String codIdentificativoFlusso, final Date dtDataOraFlusso,
			final Date dtCreazione);

	/**
	 * @param flgTipoFlusso
	 * @param codiceIpaEnte
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @return
	 */
	MygovFlussoQuadSpc getByKeyInsertable(String codiceIpaEnte, String codIdentificativoFlusso, Date dtDataOraFlusso);

	/**
	 * @param codiceIpaEnte
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @param nomeFileScaricato
	 * @param numDimensioneFileScaricato
	 * @param dtUltimaModifica
	 */
	void updateByKey(final String codiceIpaEnte, final String codIdentificativoFlusso, Date dtDataOraFlusso,
			final String nomeFileScaricato, final long numDimensioneFileScaricato, final Date dtUltimaModifica, String codStato);

	
	Page<MygovFlussoQuadSpc> getFlussiQuadSpcPage(final String codIpaEnte, final Date from, final Date to,
			final String prodOrDisp, final int pageNumber, final int pageSize) throws DataAccessException;
	
	List<MygovFlussoQuadSpc> getFlussiQuadSpc(final String codIpaEnte, final Date from, final Date to) throws DataAccessException;
}
