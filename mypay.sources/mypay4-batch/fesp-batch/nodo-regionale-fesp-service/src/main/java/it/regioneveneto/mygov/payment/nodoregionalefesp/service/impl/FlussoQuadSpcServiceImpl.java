/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.FlussoQuadSpcDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoQuadSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.FlussoQuadSpcService;

/**
 * @author regione del veneto
 */
public class FlussoQuadSpcServiceImpl implements FlussoQuadSpcService {
	private static final Log log = LogFactory.getLog(FlussoQuadSpcServiceImpl.class);

	public void setFlussoQuadSpcDao(FlussoQuadSpcDao flussoQuadSpcDao) {
		this.flussoQuadSpcDao = flussoQuadSpcDao;
	}

	private FlussoQuadSpcDao flussoQuadSpcDao;

	@Override
	public void insert(String codiceIpaEnte, String codIdentificativoFlusso, Date dtDataOraFlusso, Date dtCreazione) {
		flussoQuadSpcDao.insert(codiceIpaEnte, codIdentificativoFlusso, dtDataOraFlusso, dtCreazione);
	}

	@Override
	public MygovFlussoQuadSpc getByKeyInsertable(String codiceIpaEnte, String codIdentificativoFlusso, Date dtDataOraFlusso) {
		return flussoQuadSpcDao.getByKeyInsertable(codiceIpaEnte, codIdentificativoFlusso, dtDataOraFlusso);
	}

	@Override
	public void updateByKey(String codiceIpaEnte, String codIdentificativoFlusso, Date dtDataOraFlusso, String nomeFileScaricato,
			long numDimensioneFileScaricato, Date dtUltimaModifica, String codStato) {
		flussoQuadSpcDao.updateByKey(codiceIpaEnte, codIdentificativoFlusso, dtDataOraFlusso, nomeFileScaricato, numDimensioneFileScaricato, dtUltimaModifica,
				codStato);
	}
	
	@Override
	public Page<MygovFlussoQuadSpc> getFlussiQuadSpcPage(final String codIpaEnte, final Date from, final Date to,
			final String prodOrDisp, final int page, final int pageSize){
		return flussoQuadSpcDao.getFlussiQuadSpcPage(codIpaEnte, from, to, prodOrDisp, page, pageSize);
	}

	@Override
	public List<MygovFlussoQuadSpc> getFlussiQuadSpc(final String codIpaEnte, final Date from, final Date to) {
		return flussoQuadSpcDao.getFlussiQuadSpc(codIpaEnte, from, to);
	}
}
