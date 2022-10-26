package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoQuadSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface FlussoQuadSpcService {
	
	/**
	 * @param codiceIpaEnte
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @param dtCreazione
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
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
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	void updateByKey(final String codiceIpaEnte, final String codIdentificativoFlusso, Date dtDataOraFlusso,
			final String nomeFileScaricato, final long numDimensioneFileScaricato, final Date dtUltimaModifica, String codStato);

	Page<MygovFlussoQuadSpc> getFlussiQuadSpcPage(final String codIpaEnte, final Date from, final Date to,
			final String prodOrDisp, final int page, final int pageSize);

	List<MygovFlussoQuadSpc> getFlussiQuadSpc(final String codIpaEnte, final Date from, final Date to);
}
