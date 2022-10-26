package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.Assert;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.FlussoQuadSpcDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.batch.costants.StatiEsecuzione;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.exceptions.MandatoryFieldsException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate.pagination.HibernatePage;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoQuadSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

/**
 * @author regione del veneto
 * 
 */
public class HibernateFlussoQuadSpcDao extends HibernateDaoSupport implements FlussoQuadSpcDao {

	private static final Log log = LogFactory.getLog(HibernateFlussoQuadSpcDao.class);

	@Override
	public void insert(String codiceIpaEnte, String codIdentificativoFlusso, Date dtDataOraFlusso, Date dtCreazione) {

		if (StringUtils.isEmpty(codiceIpaEnte) || StringUtils.isBlank(codiceIpaEnte)) {
			log.error("Errore nell'inserimento del tracking del flusso quad Spc: 'codiceIpaEnte' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento del tracking del flusso quad Spc: 'codiceIpaEnte' obbligatorio");
		}

		if (StringUtils.isEmpty(codIdentificativoFlusso) || StringUtils.isBlank(codIdentificativoFlusso)) {
			log.error("Errore nell'inserimento del tracking del flusso quad Spc: 'codIdentificativoFlusso' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento del tracking del flusso quad Spc: 'codIdentificativoFlusso' obbligatorio");
		}

		if (dtDataOraFlusso == null) {
			log.error("Errore nell'inserimento del tracking del flusso quad Spc: 'dtDataOraFlusso' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento del tracking del flusso quad Spc: 'dtDataOraFlusso' obbligatorio");
		}

		// Inserisco la chiave. La data di modifica inizialmente uguale alla
		// data di creazione
		MygovFlussoQuadSpc myGovFlussoSpc = new MygovFlussoQuadSpc(codiceIpaEnte, codIdentificativoFlusso, dtDataOraFlusso, dtCreazione, dtCreazione,
				StatiEsecuzione.IN_CARICAMENTO.getValue());
		getHibernateTemplate().save(myGovFlussoSpc);
		log.debug("Inserimento tracking flusso quad spc effettuato");
	}

	@Override
	public MygovFlussoQuadSpc getByKeyInsertable(String codiceIpaEnte, String codIdentificativoFlusso, Date dtDataOraFlusso) {
		return getByKeyAndExcludedState(codiceIpaEnte, codIdentificativoFlusso, dtDataOraFlusso, StatiEsecuzione.ERRORE_CARICAMENTO);
	}

	@Override
	public void updateByKey(String codiceIpaEnte, String codIdentificativoFlusso, Date dtDataOraFlusso, String nomeFileScaricato,
			long numDimensioneFileScaricato, Date dtUltimaModifica, String codStato) {
		MygovFlussoQuadSpc myGovFlussoSpc = getByKeyUpdatable(codiceIpaEnte, codIdentificativoFlusso, dtDataOraFlusso);

		if (myGovFlussoSpc == null) {
			log.error("Errore nel update del tracking del flusso Spc: entita' da aggiornare non esistente.");
		}
		else {
			myGovFlussoSpc.setDeNomeFileScaricato(nomeFileScaricato);
			myGovFlussoSpc.setNumDimensioneFileScaricato(numDimensioneFileScaricato);
			myGovFlussoSpc.setDtUltimaModifica(dtUltimaModifica);
			myGovFlussoSpc.setCodStato(codStato);
			getHibernateTemplate().update(myGovFlussoSpc);
			log.debug("Aggiornamento tracking flusso spc effettuato");
		}
	}

	private MygovFlussoQuadSpc getByKeyUpdatable(final String codiceIpaEnte, final String codIdentificativoFlusso, final Date dtDataOraFlusso)
			throws DataAccessException {
		return getByKeyAndExcludedState(codiceIpaEnte, codIdentificativoFlusso, dtDataOraFlusso, StatiEsecuzione.IN_CARICAMENTO);
	}

	private MygovFlussoQuadSpc getByKeyAndExcludedState(final String codiceIpaEnte, final String codIdentificativoFlusso, final Date dtDataOraFlusso,
			final StatiEsecuzione statoEsecuzioneEscluso) throws DataAccessException {

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovFlussoQuadSpc.class);
		criteria.add(Restrictions.eq("codIpaEnte", codiceIpaEnte));
		criteria.add(Restrictions.eq("codIdentificativoFlusso", codIdentificativoFlusso));
		criteria.add(Restrictions.eq("dtDataOraFlusso", dtDataOraFlusso));
		criteria.add(Restrictions.ne("codStato", statoEsecuzioneEscluso.getValue()));

		@SuppressWarnings("unchecked")
		List<MygovFlussoQuadSpc> results = getHibernateTemplate().findByCriteria(criteria);

		if (CollectionUtils.isEmpty(results)) {
			return null;
		}
		else if (results.size() == 1) {
			return results.get(0);
		}
		else
			throw new IncorrectResultSizeDataAccessException("'(codiceIpaEnte [" + codiceIpaEnte + "], codIdentificativoFlusso [" + codIdentificativoFlusso
					+ "] , dtDataOraFlusso [" + dtDataOraFlusso + "])' is not unique", 1, results.size());
	}
	
	@Override
	public Page<MygovFlussoQuadSpc> getFlussiQuadSpcPage(final String codIpaEnte, final Date from, final Date to,
			final String prodOrDisp, final int pageNumber, final int pageSize) throws DataAccessException {

		Assert.isTrue((from != null), "date from parameter must not be null");
		Assert.isTrue((to != null), "date to parameter must not be null");
		Assert.isTrue(!to.before(from), "date to parameter must not be before date from parameter");

		Assert.isTrue((pageNumber >= 0), "pageNumber parameter must not be negative");
		Assert.isTrue((pageSize >= 0), "page size parameter must not be negative");
		Assert.isTrue((prodOrDisp != null), "prodOrDisp parameter must not be null");

		Page<MygovFlussoQuadSpc> page = getHibernateTemplate().execute(new HibernateCallback<Page<MygovFlussoQuadSpc>>() {

			public Page<MygovFlussoQuadSpc> doInHibernate(Session session) throws HibernateException {

				Criteria criteria = session.createCriteria(MygovFlussoQuadSpc.class);
				Criteria criteriaCount = session.createCriteria(MygovFlussoQuadSpc.class);

				criteriaCount.setProjection(Projections.rowCount());

//				criteria.add(Restrictions.eq("flgTipoFlusso", flgTipoFlusso));
//				criteriaCount.add(Restrictions.eq("flgTipoFlusso", flgTipoFlusso));

				criteria.add(Restrictions.eq("codIpaEnte", codIpaEnte));
				criteriaCount.add(Restrictions.eq("codIpaEnte", codIpaEnte));

				criteria.add(Restrictions.eq("codStato", StatiEsecuzione.CARICATO.getValue()));
				criteriaCount.add(Restrictions.eq("codStato", StatiEsecuzione.CARICATO.getValue()));

				// P = filtro su data produzione //D = filtro su data
				// creazione
				if (prodOrDisp.equals("P")) {
					criteria.add(Restrictions.and(Restrictions.ge("dtDataOraFlusso", from), Restrictions.lt("dtDataOraFlusso", DateUtils.addDays(to, 1))));
					criteriaCount.add(Restrictions.and(Restrictions.ge("dtDataOraFlusso", from), Restrictions.lt("dtDataOraFlusso", DateUtils.addDays(to, 1))));

					criteria.addOrder(Order.desc("dtDataOraFlusso"));

				}
				else {
					criteria.add(Restrictions.and(Restrictions.ge("dtCreazione", from), Restrictions.lt("dtCreazione", DateUtils.addDays(to, 1))));
					criteriaCount.add(Restrictions.and(Restrictions.ge("dtCreazione", from), Restrictions.lt("dtCreazione", DateUtils.addDays(to, 1))));

					criteria.addOrder(Order.desc("dtCreazione"));
				}
				criteria.addOrder(Order.desc("mygovFlussoQuadSpcId")); // per sicurezza aggiungo sempre un'ordinamento per id in modo da non avere errori in paginazione
				return new HibernatePage<MygovFlussoQuadSpc>(criteria, pageNumber, pageSize, criteriaCount);
			}
		});

		return page;
	}
	

	@Override
	public List<MygovFlussoQuadSpc> getFlussiQuadSpc(final String codIpaEnte, final Date from, final Date to) {

		Assert.isTrue((from != null), "date from parameter must not be null");
		Assert.isTrue((to != null), "date to parameter must not be null");
		Assert.isTrue(!to.before(from), "date to parameter must not be before date from parameter");
		Assert.isTrue((codIpaEnte != null), "codIpaEnte parameter must not be null");

		List<MygovFlussoQuadSpc> listaFlussi = getHibernateTemplate().execute(new HibernateCallback<List<MygovFlussoQuadSpc>>() {

			@SuppressWarnings("unchecked")
			@Override
			public List<MygovFlussoQuadSpc> doInHibernate(Session session) throws HibernateException {
				Criteria criteria = session.createCriteria(MygovFlussoQuadSpc.class);
				criteria.add(Restrictions.eq("codIpaEnte", codIpaEnte));
				criteria.add(Restrictions.eq("codStato", StatiEsecuzione.CARICATO.getValue()));

				criteria.add(Restrictions.and(Restrictions.ge("dtCreazione", from), Restrictions.lt("dtCreazione", DateUtils.addDays(to, 1))));

				return criteria.list();
			}
		});

		return listaFlussi;
	}
}
