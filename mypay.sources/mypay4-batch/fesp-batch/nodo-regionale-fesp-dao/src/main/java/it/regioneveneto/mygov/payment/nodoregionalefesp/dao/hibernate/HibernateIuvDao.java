package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.IuvDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovIuv;

import java.sql.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author regione del veneto
 *
 */
public class HibernateIuvDao extends HibernateDaoSupport implements IuvDao {

	private static final Log log = LogFactory.getLog(HibernateIuvDao.class);

	@Override
	public void insert(final String codiceIpaEnte, final String tipoVersamento, final String iuv)
			throws DataAccessException {

		log.debug("Invocato metodo insert(" + codiceIpaEnte + ", " + tipoVersamento + ", " + iuv);

		Date dataCreazione = new Date(System.currentTimeMillis());

		MygovIuv myGovIuv = new MygovIuv();

		myGovIuv.setCodIpaEnte(codiceIpaEnte);
		myGovIuv.setIuv(iuv);
		myGovIuv.setTipoVersamento(tipoVersamento);
		myGovIuv.setDtCreazione(dataCreazione);

		getHibernateTemplate().saveOrUpdate(myGovIuv);

	}

}
