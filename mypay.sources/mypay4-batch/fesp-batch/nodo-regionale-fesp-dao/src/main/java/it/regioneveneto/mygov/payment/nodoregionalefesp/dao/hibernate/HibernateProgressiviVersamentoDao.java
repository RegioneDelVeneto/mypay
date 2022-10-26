package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.ProgressiviVersamentoDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovProgressiviversamento;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author regione del veneto
 *
 */
public class HibernateProgressiviVersamentoDao extends HibernateDaoSupport implements ProgressiviVersamentoDao {

	private static final Log log = LogFactory.getLog(HibernateProgressiviVersamentoDao.class);

	/**
	 * 
	 */
	public HibernateProgressiviVersamentoDao() {
		super();
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.ProgressiviVersamentoDao#getNextProgressivoVersamento(java.lang.String, java.lang.String)
	 */
	@Override
	public long getNextProgressivoVersamento(final String codiceIpaEnte, final String tipoGeneratore, final String tipoVersamento) throws DataAccessException {

		MygovProgressiviversamento myGovProgressiviVersamento = this.getByKey(codiceIpaEnte, tipoGeneratore, tipoVersamento);

		if (myGovProgressiviVersamento == null) {
			myGovProgressiviVersamento = new MygovProgressiviversamento();

			myGovProgressiviVersamento.setCodIpaEnte(codiceIpaEnte);
			myGovProgressiviVersamento.setTipoGeneratore(tipoGeneratore);
			myGovProgressiviVersamento.setTipoVersamento(tipoVersamento);
			myGovProgressiviVersamento.setProgressivoVersamento(1);
		}
		else {
			getHibernateTemplate().evict(myGovProgressiviVersamento);

			myGovProgressiviVersamento = (MygovProgressiviversamento) getHibernateTemplate().load(MygovProgressiviversamento.class,
					myGovProgressiviVersamento.getId(), LockMode.PESSIMISTIC_WRITE);

			myGovProgressiviVersamento.setProgressivoVersamento(myGovProgressiviVersamento.getProgressivoVersamento() + 1);
		}

		getHibernateTemplate().saveOrUpdate(myGovProgressiviVersamento);
		getHibernateTemplate().flush();

		return myGovProgressiviVersamento.getProgressivoVersamento();
	}

	/**
	 * @param codiceIpaEnte
	 * @param tipoGeneratore
	 * @param tipoVersamento
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	private MygovProgressiviversamento getByKey(final String codiceIpaEnte, final String tipoGeneratore, final String tipoVersamento)
			throws DataAccessException {

		List<MygovProgressiviversamento> results = getHibernateTemplate().find(
				"FROM MygovProgressiviversamento WHERE codIpaEnte = ? AND tipoGeneratore = ? AND tipoVersamento = ?", codiceIpaEnte, tipoGeneratore,
				tipoVersamento);
		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException(
					"'(codIpaEnte [" + codiceIpaEnte + "], tipoGeneratore [" + tipoGeneratore + "], tipoVersamento [" + tipoVersamento + "])' is not unique", 1,
					results.size());

		return (results.size() == 0) ? null : results.get(0);
	}
}
