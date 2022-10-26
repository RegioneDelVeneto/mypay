/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.TipoVersamentoDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovTipiversamento;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.TipoVersamentoService;

import java.util.List;

/**
 * @author regione del veneto
 *
 */
public class TipoVersamentoServiceImpl implements TipoVersamentoService {

	private TipoVersamentoDao tipoVersamentoDao;

	/**
	 * @param tipoVersamentoDao
	 */
	public void setTipoVersamentoDao(TipoVersamentoDao tipoVersamentoDao) {
		this.tipoVersamentoDao = tipoVersamentoDao;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.TipoVersamentoService#getByKey(long)
	 */
	@Override
	public MygovTipiversamento getByKey(final long id) {
		return tipoVersamentoDao.getByKey(id);
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.TipoVersamentoService#getByTipoVersamento(java.lang.String)
	 */
	@Override
	public MygovTipiversamento getByTipoVersamento(final String tipoVersamento) {
		return tipoVersamentoDao.getByTipoVersamento(tipoVersamento);
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.TipoVersamentoService#findAll()
	 */
	@Override
	public List<MygovTipiversamento> findAll() {
		return tipoVersamentoDao.findAll();
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.TipoVersamentoService#getIuvCodicePerTipoVersamento(java.lang.String)
	 */
	@Override
	public String getIuvCodicePerTipoVersamento(final String tipoVersamento) {
		return tipoVersamentoDao.getIuvCodicePerTipoVersamento(tipoVersamento);
	}
}
