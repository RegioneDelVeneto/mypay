/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.GiornaleDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate.pagination.HibernatePage;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovGiornale;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.Assert;

/**
 * @author regione del veneto
 * 
 */
@SuppressWarnings("deprecation")
public class HibernateGiornaleDao extends HibernateDaoSupport implements GiornaleDao {

	/**
	 * 
	 */
	public HibernateGiornaleDao() {
		super();
	}

	@Override
	public Page<MygovGiornale> getGiornalePage(final String iuv, final String ente, final String te, final String ce, final String psp, final String esito,
			final Date dateFrom, final Date dateTo, final int pageNumber, final int pageNumOfRecords, final String orderingField, final String sortingOrder)
			throws DataAccessException {
		Assert.isTrue((pageNumber >= 0), "page number parameter must not be negative");
		Assert.isTrue((pageNumOfRecords >= 0), "page size parameter must not be negative");

		Page<MygovGiornale> page = getHibernateTemplate().execute(new HibernateCallback<Page<MygovGiornale>>() {
			public Page<MygovGiornale> doInHibernate(Session session) throws HibernateException {
				Criteria criteria = session.createCriteria(MygovGiornale.class);
				Criteria criteriaCount = session.createCriteria(MygovGiornale.class);

				criteriaCount.setProjection(Projections.rowCount());

				if (StringUtils.isNotBlank(iuv)) {
					criteria.add(Restrictions.eq("identificativoUnivocoVersamento", iuv).ignoreCase());
					criteriaCount.add(Restrictions.eq("identificativoUnivocoVersamento", iuv).ignoreCase());
				}

				if (StringUtils.isNotBlank(ente) && !ente.equals("tutti")) {
					criteria.add(Restrictions.eq("identificativoDominio", ente).ignoreCase());
					criteriaCount.add(Restrictions.eq("identificativoDominio", ente).ignoreCase());
				}

				if (StringUtils.isNotBlank(te) && !te.equals("tutti")) {
					criteria.add(Restrictions.eq("tipoEvento", te).ignoreCase());
					criteriaCount.add(Restrictions.eq("tipoEvento", te).ignoreCase());
				}
				if (StringUtils.isNotBlank(ce) && !ce.equals("tutti")) {
					criteria.add(Restrictions.eq("categoriaEvento", ce).ignoreCase());
					criteriaCount.add(Restrictions.eq("categoriaEvento", ce).ignoreCase());
				}

				if (StringUtils.isNotBlank(psp) && !psp.equals("tutti")) {
					criteria.add(Restrictions.eq("identificativoPrestatoreServiziPagamento", psp).ignoreCase());
					criteriaCount.add(Restrictions.eq("identificativoPrestatoreServiziPagamento", psp).ignoreCase());
				}

				if (StringUtils.isNotBlank(esito) && !esito.equals("tutti")) {
					criteria.add(Restrictions.eq("esito", esito).ignoreCase());
					criteriaCount.add(Restrictions.eq("esito", esito).ignoreCase());
				}

				if (dateFrom != null && dateTo != null) {

					Date fromMinusOne = new Date(dateFrom.getTime());
					fromMinusOne = DateUtils.addDays(fromMinusOne, -1);
					fromMinusOne = DateUtils.setHours(fromMinusOne, 23);
					fromMinusOne = DateUtils.setMinutes(fromMinusOne, 59);
					fromMinusOne = DateUtils.setSeconds(fromMinusOne, 59);
					fromMinusOne = DateUtils.setMilliseconds(fromMinusOne, 999);

					Date toPlusOne = new Date(dateTo.getTime());
					toPlusOne = DateUtils.setHours(toPlusOne, 23);
					toPlusOne = DateUtils.setMinutes(toPlusOne, 59);
					toPlusOne = DateUtils.setSeconds(toPlusOne, 59);
					toPlusOne = DateUtils.setMilliseconds(toPlusOne, 999);

					criteria.add(Restrictions.between("dataOraEvento", fromMinusOne, toPlusOne));
					criteriaCount.add(Restrictions.between("dataOraEvento", fromMinusOne, toPlusOne));
				}

				if (StringUtils.isNotBlank(orderingField) && StringUtils.isNotBlank(sortingOrder)) {
					if (sortingOrder.equals("asc")) {
						criteria.addOrder(Order.asc(orderingField));
					} else {
						criteria.addOrder(Order.desc(orderingField));
					}
				}

				return new HibernatePage<MygovGiornale>(criteria, pageNumber, pageNumOfRecords, criteriaCount);
			}
		});

		return page;
	}

	@Override
	public MygovGiornale getGiornale(String iuv) throws DataAccessException {
		return null;
	}

	@Override
	public MygovGiornale getGiornale(long idGiornale) throws DataAccessException {
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovGiornale.class);
		criteria.add(Restrictions.eq("id", idGiornale));
		List<MygovGiornale> results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1) {
			throw new DataIntegrityViolationException("pa.giornale.giornaleDuplicato");
		}
		return results.get(0);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllPspDistinct() {

		List<String> results = getHibernateTemplate().find("select distinct g.identificativoPrestatoreServiziPagamento from MygovGiornale g order by g.identificativoPrestatoreServiziPagamento");

		return results;
	}

	@Override
	public void insertGiornale(final Date dataOraEvento, final String identificativoDominio, final String identificativoUnivocoVersamento,
			final String codiceContestoPagamento, final String identificativoPrestatoreServiziPagamento, final String tipoVersamento, final String componente,
			final String categoriaEvento, final String tipoEvento, final String sottoTipoEvento, final String identificativoFruitore,
			final String identificativoErogatore, final String identificativoStazioneIntermediarioPa, final String canalePagamento,
			final String parametriSpecificiInterfaccia, final String esito) {

		MygovGiornale giornale = new MygovGiornale(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
				identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
				identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);

		getHibernateTemplate().save(giornale);

		getHibernateTemplate().flush();
	}

}
