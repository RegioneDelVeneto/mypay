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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.Assert;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.FlussoRendSpcDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.batch.costants.StatiEsecuzione;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.exceptions.MandatoryFieldsException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate.pagination.HibernatePage;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoRendSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

/**
 * @author regione del veneto
 * 
 */
public class HibernateFlussoRendSpcDao extends HibernateDaoSupport implements FlussoRendSpcDao {

	private static final Log log = LogFactory.getLog(HibernateFlussoRendSpcDao.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.regioneveneto.mygov.payment.nodoregionalefesp.dao.FlussoSpcDao#insert
	 * (java.lang.String, java.lang.String, java.lang.String, java.util.Date,
	 * java.util.Date)
	 */
	@Override
	public void insert(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso, final Date dtDataOraFlusso,
			final Date dtCreazione) throws DataAccessException, DataIntegrityViolationException {

		if (StringUtils.isEmpty(codiceIpaEnte) || StringUtils.isBlank(codiceIpaEnte)) {
			log.error("Errore nell'inserimento del tracking del flusso Spc: 'codiceIpaEnte' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento del tracking del flusso Spc: 'codiceIpaEnte' obbligatorio");
		}

		if (StringUtils.isEmpty(identificativoPsp) || StringUtils.isBlank(identificativoPsp)) {
			log.error("Errore nell'inserimento del tracking del flusso Spc: 'identificativoPsp' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento del tracking del flusso Spc: 'identificativoPsp' obbligatorio");
		}

		if (StringUtils.isEmpty(codIdentificativoFlusso) || StringUtils.isBlank(codIdentificativoFlusso)) {
			log.error("Errore nell'inserimento del tracking del flusso Spc: 'codIdentificativoFlusso' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento del tracking del flusso Spc: 'codIdentificativoFlusso' obbligatorio");
		}

		if (dtDataOraFlusso == null) {
			log.error("Errore nell'inserimento del tracking del flusso Spc: 'dtDataOraFlusso' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento del tracking del flusso Spc: 'dtDataOraFlusso' obbligatorio");
		}

		// Inserisco la chiave. La data di modifica inizialmente uguale alla
		// data di creazione
		MygovFlussoRendSpc myGovFlussoSpc = new MygovFlussoRendSpc(codiceIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso, dtCreazione,
				dtCreazione, StatiEsecuzione.IN_CARICAMENTO.getValue());
		getHibernateTemplate().save(myGovFlussoSpc);
		log.debug("Inserimento tracking flusso spc effettuato");

	}

	@Override
	public MygovFlussoRendSpc getByKeyInsertable(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso,
			final Date dtDataOraFlusso) throws DataAccessException {
		//		List<MygovFlussoRendSpc> results = getHibernateTemplate()
		//				.find("FROM MygovFlussoSpc WHERE flgTipoFlusso = ? AND codIpaEnte = ? AND identificativoPsp = ? AND codIdentificativoFlusso = ? AND dtDataOraFlusso = ? AND codStato != ?",
		//						flgTipoFlusso, codiceIpaEnte, identificativoPsp,
		//						codIdentificativoFlusso, dtDataOraFlusso,
		//						StatiEsecuzione.ERRORE_CARICAMENTO.getValue());

		return getByKeyAndExcludedState(codiceIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso, StatiEsecuzione.ERRORE_CARICAMENTO);
	}

	private MygovFlussoRendSpc getByKeyUpdatable(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso,
			final Date dtDataOraFlusso) throws DataAccessException {
		return getByKeyAndIncludedState(codiceIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso, StatiEsecuzione.IN_CARICAMENTO);
	}

	private MygovFlussoRendSpc getByKeyAndExcludedState(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso,
			final Date dtDataOraFlusso, final StatiEsecuzione statoEsecuzioneEscluso) throws DataAccessException {
		//		List<MygovFlussoRendSpc> results = getHibernateTemplate()
		//				.find("FROM MygovFlussoSpc WHERE flgTipoFlusso = ? AND codIpaEnte = ? AND identificativoPsp = ? AND codIdentificativoFlusso = ? AND dtDataOraFlusso = ? AND codStato = ?",
		//						flgTipoFlusso, codiceIpaEnte, identificativoPsp,
		//						codIdentificativoFlusso, dtDataOraFlusso,
		//						StatiEsecuzione.IN_CARICAMENTO.getValue());

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovFlussoRendSpc.class);
		criteria.add(Restrictions.eq("codIpaEnte", codiceIpaEnte));
		criteria.add(Restrictions.eq("identificativoPsp", identificativoPsp));
		criteria.add(Restrictions.eq("codIdentificativoFlusso", codIdentificativoFlusso));
		criteria.add(Restrictions.eq("dtDataOraFlusso", dtDataOraFlusso));
		criteria.add(Restrictions.ne("codStato", statoEsecuzioneEscluso.getValue()));

		@SuppressWarnings("unchecked")
		List<MygovFlussoRendSpc> results = getHibernateTemplate().findByCriteria(criteria);

		if (CollectionUtils.isEmpty(results)) {
			return null;
		}
		else if (results.size() == 1) {
			return results.get(0);
		}
		else
			throw new IncorrectResultSizeDataAccessException("'(codiceIpaEnte [" + codiceIpaEnte + "], identificativoPsp [" + identificativoPsp
					+ "], codIdentificativoFlusso [" + codIdentificativoFlusso + "] , dtDataOraFlusso [" + dtDataOraFlusso + "])' is not unique", 1,
					results.size());

	}
	
	private MygovFlussoRendSpc getByKeyAndIncludedState(final String codiceIpaEnte, final String identificativoPsp, final String codIdentificativoFlusso,
			final Date dtDataOraFlusso, final StatiEsecuzione statoEsecuzioneEscluso) throws DataAccessException {
		
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovFlussoRendSpc.class);
		criteria.add(Restrictions.eq("codIpaEnte", codiceIpaEnte));
		criteria.add(Restrictions.eq("identificativoPsp", identificativoPsp));
		criteria.add(Restrictions.eq("codIdentificativoFlusso", codIdentificativoFlusso));
		criteria.add(Restrictions.eq("dtDataOraFlusso", dtDataOraFlusso));
		criteria.add(Restrictions.eq("codStato", statoEsecuzioneEscluso.getValue()));

		@SuppressWarnings("unchecked")
		List<MygovFlussoRendSpc> results = getHibernateTemplate().findByCriteria(criteria);

		if (CollectionUtils.isEmpty(results)) {
			return null;
		}
		else if (results.size() == 1) {
			return results.get(0);
		}
		else
			throw new IncorrectResultSizeDataAccessException("'(codiceIpaEnte [" + codiceIpaEnte + "], identificativoPsp [" + identificativoPsp
					+ "], codIdentificativoFlusso [" + codIdentificativoFlusso + "] , dtDataOraFlusso [" + dtDataOraFlusso + "])' is not unique", 1,
					results.size());

	}

	@Override
	public void updateByKey(String codiceIpaEnte, String identificativoPsp, String codIdentificativoFlusso, Date dtDataOraFlusso, String nomeFileScaricato,
			long numDimensioneFileScaricato, Date dtUltimaModifica, String codStato) throws DataAccessException {

		MygovFlussoRendSpc myGovFlussoSpc = getByKeyUpdatable(codiceIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.FlussoSpcDao#
	 * getFlussiSpcPage(java.lang.String, java.lang.String, java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.Boolean, int, int)
	 */
	@Override
	public Page<MygovFlussoRendSpc> getFlussiRendSpcPage(final String codIpaEnte, final String identificativoPsp, final Date from, final Date to,
			final String prodOrDisp, final int pageNumber, final int pageSize) throws DataAccessException {

		Assert.isTrue((from != null), "date from parameter must not be null");
		Assert.isTrue((to != null), "date to parameter must not be null");
		Assert.isTrue(!to.before(from), "date to parameter must not be before date from parameter");

		Assert.isTrue((pageNumber >= 0), "pageNumber parameter must not be negative");
		Assert.isTrue((pageSize >= 0), "page size parameter must not be negative");
		Assert.isTrue((prodOrDisp != null), "prodOrDisp parameter must not be null");

		Page<MygovFlussoRendSpc> page = getHibernateTemplate().execute(new HibernateCallback<Page<MygovFlussoRendSpc>>() {

			public Page<MygovFlussoRendSpc> doInHibernate(Session session) throws HibernateException {

				Criteria criteria = session.createCriteria(MygovFlussoRendSpc.class);
				Criteria criteriaCount = session.createCriteria(MygovFlussoRendSpc.class);

				criteriaCount.setProjection(Projections.rowCount());

//				criteria.add(Restrictions.eq("flgTipoFlusso", flgTipoFlusso));
//				criteriaCount.add(Restrictions.eq("flgTipoFlusso", flgTipoFlusso));

				criteria.add(Restrictions.eq("codIpaEnte", codIpaEnte));
				criteriaCount.add(Restrictions.eq("codIpaEnte", codIpaEnte));

				criteria.add(Restrictions.eq("codStato", StatiEsecuzione.CARICATO.getValue()));
				criteriaCount.add(Restrictions.eq("codStato", StatiEsecuzione.CARICATO.getValue()));

				if (StringUtils.isNotBlank(identificativoPsp)) {
					criteria.add(Restrictions.eq("identificativoPsp", identificativoPsp));
					criteriaCount.add(Restrictions.eq("identificativoPsp", identificativoPsp));
				}

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
				criteria.addOrder(Order.desc("mygovFlussoRendSpcId")); // per sicurezza aggiungo sempre un'ordinamento per id in modo da non avere errori in paginazione
				return new HibernatePage<MygovFlussoRendSpc>(criteria, pageNumber, pageSize, criteriaCount);
			}
		});

		return page;
	}

	@Override
	public List<MygovFlussoRendSpc> getFlussiRendSpc(final String codIpaEnte, final String identificativoPSP, final Date from, final Date to) {

		Assert.isTrue((from != null), "date from parameter must not be null");
		Assert.isTrue((to != null), "date to parameter must not be null");
		Assert.isTrue(!to.before(from), "date to parameter must not be before date from parameter");
		Assert.isTrue((codIpaEnte != null), "codIpaEnte parameter must not be null");

		List<MygovFlussoRendSpc> listaFlussi = getHibernateTemplate().execute(new HibernateCallback<List<MygovFlussoRendSpc>>() {

			@SuppressWarnings("unchecked")
			@Override
			public List<MygovFlussoRendSpc> doInHibernate(Session session) throws HibernateException {

				Criteria criteria = session.createCriteria(MygovFlussoRendSpc.class);

//				criteria.add(Restrictions.eq("flgTipoFlusso", flgTipoFlusso));
				criteria.add(Restrictions.eq("codIpaEnte", codIpaEnte));
				if (StringUtils.isNotBlank(identificativoPSP)) {
					criteria.add(Restrictions.eq("identificativoPsp", identificativoPSP));
				}
				criteria.add(Restrictions.eq("codStato", StatiEsecuzione.CARICATO.getValue()));

				criteria.add(Restrictions.and(Restrictions.ge("dtCreazione", from), Restrictions.lt("dtCreazione", DateUtils.addDays(to, 1))));

				return criteria.list();
			}

		});

		return listaFlussi;
	}
	
	@Override
	public List<MygovFlussoRendSpc> getFlussiRendSpcByState(final String stato){
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovFlussoRendSpc.class);
		criteria.add(Restrictions.eq("codStato", stato));

		@SuppressWarnings("unchecked")
		List<MygovFlussoRendSpc> myGovFlussoSpc = getHibernateTemplate().findByCriteria(criteria);
		
		return myGovFlussoSpc;
	}
	
	@Override
	public void updateToKoState(MygovFlussoRendSpc flussoRendDaAggiornare) {
		flussoRendDaAggiornare.setCodStato(StatiEsecuzione.ERRORE_CARICAMENTO.getValue());
		getHibernateTemplate().update(flussoRendDaAggiornare);
		log.debug("Ripristinata rendicontazione  ["
				+ flussoRendDaAggiornare.getCodIdentificativoFlusso() + "]");
	}
}
