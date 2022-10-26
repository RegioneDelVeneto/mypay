package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoRendSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

/**
 * @author regione del veneto
 *
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface FlussoRendSpcService {

	/**
	 * @param codiceIpaEnte
	 * @param identificativoPsp
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @param dtCreazione
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	void insert(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso, final Date dtDataOraFlusso,
			final Date dtCreazione);

	/**
	 * @param flgTipoFlusso
	 * @param codiceIpaEnte
	 * @param identificativoPsp
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @return
	 */
	MygovFlussoRendSpc getByKeyInsertable(String codiceIpaEnte, String identificativoPsp, String codIdentificativoFlusso, Date dtDataOraFlusso);

	/**
	 * @param codiceIpaEnte
	 * @param identificativoPsp
	 * @param codIdentificativoFlusso
	 * @param dtDataOraFlusso
	 * @param nomeFileScaricato
	 * @param numDimensioneFileScaricato
	 * @param dtUltimaModifica
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	void updateByKey(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso, Date dtDataOraFlusso,
			final String nomeFileScaricato, final long numDimensioneFileScaricato, final Date dtUltimaModifica, String codStato);

	/**
	 * @param flgTipoFlusso
	 * @param codIpaEnte
	 * @param identificativoPsp
	 * @param from
	 * @param to
	 * @param prodOrDisp
	 * @param page
	 * @param pageSize
	 * @return
	 */
	Page<MygovFlussoRendSpc> getFlussiRendSpcPage(final String codIpaEnte, final String identificativoPsp, final Date from, final Date to,
			final String prodOrDisp, final int page, final int pageSize);

	/**
	 * @param flgTipoFlusso
	 * @param codIpaEnte
	 * @param identificativoPSP
	 * @param time
	 * @param time2
	 * @param flgProdOrDisp
	 * @return
	 */
	List<MygovFlussoRendSpc> getFlussiRendSpc(final String codIpaEnte, final String identificativoPSP, final Date from, final Date to);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	int resetFlussiInCaricamento();

}
