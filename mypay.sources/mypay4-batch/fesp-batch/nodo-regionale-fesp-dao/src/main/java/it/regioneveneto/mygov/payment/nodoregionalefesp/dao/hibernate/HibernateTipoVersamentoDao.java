package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.TipoVersamentoDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovTipiversamento;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.Assert;

/**
 * @author regione del veneto
 *
 */
public class HibernateTipoVersamentoDao extends HibernateDaoSupport implements TipoVersamentoDao {

	private static final Log log = LogFactory.getLog(HibernateTipoVersamentoDao.class);

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.TipoVersamentoDao#getByKey(long)
	 */
	@Override
	public MygovTipiversamento getByKey(final long id) throws DataAccessException {
		log.debug("Invocato metodo getByKey(" + id + ")");

		MygovTipiversamento tipoVersamento = null;
		tipoVersamento = getHibernateTemplate().get(MygovTipiversamento.class, id);

		return tipoVersamento;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.TipoVersamentoDao#getByTipoVersamento(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MygovTipiversamento getByTipoVersamento(final String tipoVersamento) throws DataAccessException {

		log.debug("Invocato metodo getByTipoVersamento(" + tipoVersamento + ")");

		MygovTipiversamento myGovTipoVersamento = null;

		Assert.notNull(tipoVersamento, "tipoVersamento deve essere valorizzato");

//		List<MygovTipiversamento> list = (List<MygovTipiversamento>) getHibernateTemplate().find(
//				"FROM MygovTipiversamento WHERE tipoVersamento ='" + tipoVersamento + "' ");
		
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovTipiversamento.class);
		criteria.add(Restrictions.eq("tipoVersamento", tipoVersamento));
		List<MygovTipiversamento> list = getHibernateTemplate().findByCriteria(criteria);
		
		
		if (list != null && list.size() > 0) {
			myGovTipoVersamento = (MygovTipiversamento) list.get(0);
		}

		return myGovTipoVersamento;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.TipoVersamentoDao#findAll()
	 */
	@Override
	public List<MygovTipiversamento> findAll() throws DataAccessException {
		log.debug("Invocato metodo findAll()");
		
//		List<MygovTipiversamento> list = (List<MygovTipiversamento>) getSession().createQuery(
//				"from MygovTipiversamento").list();
		
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovTipiversamento.class);
		List<MygovTipiversamento> list = getHibernateTemplate().findByCriteria(criteria);
		
		return list;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.TipoVersamentoDao#getIuvCodicePerTipoVersamento(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getIuvCodicePerTipoVersamento(final String tipoVersamento) throws DataAccessException {
		log.debug("Invocato metodo getIuvCodicePerTipoVersamento(" + tipoVersamento + ")");

		String iuvCodiceTipoVersamento = null;

//		String sql = "SELECT iuvCodiceTipoVersamentoId FROM MygovTipiversamento WHERE tipoVersamento = '"
//				+ tipoVersamento + "'";
//		List listResult = getHibernateTemplate().find(sql);

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovTipiversamento.class);
		criteria.add(Restrictions.eq("tipoVersamento", tipoVersamento));
		List<MygovTipiversamento> list = getHibernateTemplate().findByCriteria(criteria);
		
		

		if (list != null && list.size() > 0) {

			//Recupera il codice tipo versamento
			if (list.get(0) != null) {
				iuvCodiceTipoVersamento = list.get(0).getIuvCodiceTipoVersamentoId();
			}
		}

		return iuvCodiceTipoVersamento;
	}
}
