/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.FlussoRendSpcDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.batch.costants.StatiEsecuzione;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoRendSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.FlussoRendSpcService;

/**
 * @author regione del veneto
 */
public class FlussoRendSpcServiceImpl implements FlussoRendSpcService {
	private static final Log log = LogFactory.getLog(FlussoRendSpcServiceImpl.class);

	private FlussoRendSpcDao flussoRendSpcDao;

	public void setFlussoRendSpcDao(FlussoRendSpcDao flussoRendSpcDao) {
		this.flussoRendSpcDao = flussoRendSpcDao;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.FlussoSpcService#insert(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)
	 */
	@Override
	public void insert(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso, final Date dtDataOraFlusso,
			final Date dtCreazione) {
		flussoRendSpcDao.insert(codiceIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso, dtCreazione);
	}

	@Override
	public MygovFlussoRendSpc getByKeyInsertable(String codiceIpaEnte, String identificativoPsp, String codIdentificativoFlusso, Date dtDataOraFlusso) {

		return flussoRendSpcDao.getByKeyInsertable(codiceIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso);
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.FlussoSpcService#updateByKey(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String, long, java.util.Date)
	 */
	@Override
	public void updateByKey(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso, Date dtDataOraFlusso,
			final String nomeFileScaricato, final long numDimensioneFileScaricato, final Date dtUltimaModifica, final String codStato) {
		flussoRendSpcDao.updateByKey(codiceIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso, nomeFileScaricato, numDimensioneFileScaricato,
				dtUltimaModifica, codStato);
	}

	@Override
	public Page<MygovFlussoRendSpc> getFlussiRendSpcPage(final String codIpaEnte, final String identificativoPsp, final Date from, final Date to,
			final String prodOrDisp, int page, int pageSize) {
		return flussoRendSpcDao.getFlussiRendSpcPage(codIpaEnte, identificativoPsp, from, to, prodOrDisp, page, pageSize);
	}

	@Override
	public List<MygovFlussoRendSpc> getFlussiRendSpc(final String codIpaEnte, final String identificativoPSP, final Date from, final Date to) {
		return flussoRendSpcDao.getFlussiRendSpc(codIpaEnte, identificativoPSP, from, to);
	}
	
	@Override
	public int resetFlussiInCaricamento() {
		List<MygovFlussoRendSpc> rendicontazioniDaRipristinare = flussoRendSpcDao.getFlussiRendSpcByState(StatiEsecuzione.IN_CARICAMENTO.getValue());
		if (rendicontazioniDaRipristinare != null && !rendicontazioniDaRipristinare.isEmpty()){
			for (MygovFlussoRendSpc flussoRend : rendicontazioniDaRipristinare){
				flussoRendSpcDao.updateToKoState(flussoRend);
			}
			return rendicontazioniDaRipristinare.size();
		}
		else
			log.info("Nessun flusso da aggiornare");
			return 0;
	}

}
