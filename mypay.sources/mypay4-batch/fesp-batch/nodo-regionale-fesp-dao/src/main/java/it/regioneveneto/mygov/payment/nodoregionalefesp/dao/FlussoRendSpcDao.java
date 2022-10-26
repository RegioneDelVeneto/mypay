package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoRendSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

/**
 * @author regione del veneto
 *
 */
public interface FlussoRendSpcDao {

	/**
	 * @param codiceIpaEnte
	 * @param identificativoPsp
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @param dtCreazione
	 * @throws DataAccessException
	 */
	void insert(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso,
			final Date dtDataOraFlusso, final Date dtCreazione) throws DataAccessException,
			DataIntegrityViolationException;

	/**
	 * @param codiceIpaEnte
	 * @param identificativoPsp
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @return
	 * @throws DataAccessException
	 */
	MygovFlussoRendSpc getByKeyInsertable(final String codiceIpaEnte, final String identificativoPsp,
			final String codIdentificativoFlusso, final Date dtDataOraFlusso) throws DataAccessException;	

	/**
	 * @param codiceIpaEnte
	 * @param identificativoPsp
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @param nomeFileScaricato
	 * @param numDimensioneFileScaricato
	 * @param dtUltimaModifica
	 * @throws DataAccessException
	 */
	void updateByKey(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso,
			Date dtDataOraFlusso, final String nomeFileScaricato, final long numDimensioneFileScaricato,
			final Date dtUltimaModifica, final String codStato) throws DataAccessException;

	
	
	/**
	 * @param codIpaEnte
	 * @param identificativoPsp
	 * @param from
	 * @param to
	 * @param prodOrDisp
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws DataAccessException
	 */
	Page<MygovFlussoRendSpc> getFlussiRendSpcPage(final String codIpaEnte, final String identificativoPsp,
			final Date from, final Date to, final String prodOrDisp, final int page, final int pageSize) throws DataAccessException;
	
	/**
	 * @param codIpaEnte
	 * @param identificativoPSP
	 * @param from
	 * @param to
	 * @param flgProdOrDisp
	 * @return
	 */
	List<MygovFlussoRendSpc> getFlussiRendSpc(final String codIpaEnte,
			final String identificativoPSP, final Date from, final Date to);

	/**
	 * @param stato
	 * @return
	 */
	List<MygovFlussoRendSpc> getFlussiRendSpcByState(String stato);

	/**
	 * @param flussoRendDaAggiornare
	 * @return
	 */
	void updateToKoState(MygovFlussoRendSpc flussoRendDaAggiornare);	
}
