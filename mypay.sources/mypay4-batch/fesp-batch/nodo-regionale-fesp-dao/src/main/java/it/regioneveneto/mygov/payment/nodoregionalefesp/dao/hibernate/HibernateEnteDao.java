package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.EnteDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author regione del veneto
 *
 */
public class HibernateEnteDao extends HibernateDaoSupport implements EnteDao {

	private static final Log log = LogFactory.getLog(HibernateEnteDao.class);

	@SuppressWarnings("unchecked")
	@Override
	public MygovEnte getByCodiceFiscale(final String codiceFiscaleEnte) throws DataAccessException {

		//		List<MygovEnte> results = getHibernateTemplate().find("from MygovEnte where  codiceFiscaleEnte = ?",
//				codiceFiscaleEnte);

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovEnte.class);
		criteria.add(Restrictions.eq("codiceFiscaleEnte", codiceFiscaleEnte));
		List<MygovEnte> results = getHibernateTemplate().findByCriteria(criteria);
		
		
		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("'codiceFiscaleEnte' is not unique. Per valore ::: : "
					+ codiceFiscaleEnte + "'", 1, results.size());

		if (results.size() == 0)
			throw new IncorrectResultSizeDataAccessException("'codiceFiscaleEnte' non esiste in db. Per valore ::: '"
					+ codiceFiscaleEnte + "'", 1, results.size());

		return results.size() == 0 ? null : (MygovEnte) results.get(0);

	}

	@SuppressWarnings("unchecked")
	@Override
	public MygovEnte getByCodiceIpa(final String codiceIpa) throws DataAccessException {
		
//		List<MygovEnte> results = getHibernateTemplate().find("from MygovEnte where  codIpaEnte = ?", codiceIpa);

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovEnte.class);
		criteria.add(Restrictions.eq("codIpaEnte", codiceIpa));
		List<MygovEnte> results = getHibernateTemplate().findByCriteria(criteria);
		
		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("' codIpaEnte' is not unique. Per valore ::: : "
					+ codiceIpa + "'", 1, results.size());

		if (results.size() == 0)
			throw new IncorrectResultSizeDataAccessException("' codIpaEnte' non esiste in DB. Per valore ::: : "
					+ codiceIpa + "'", 1, results.size());

		return results.size() == 0 ? null : (MygovEnte) results.get(0);

	}

	@SuppressWarnings("unchecked")
	public List<MygovEnte> findAll() {
		log.debug("Invocato metodo findAll()");
		
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovEnte.class);
		criteria.addOrder(Order.asc("deNomeEnte"));
		List<MygovEnte> list = getHibernateTemplate().findByCriteria(criteria);
		return list;
	}
}
