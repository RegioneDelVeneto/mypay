package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.AttivaRptDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovAttivaRptE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author regione del veneto
 * 
 */
public class HibernateAttivaRptDao extends HibernateDaoSupport implements AttivaRptDao {

	public HibernateAttivaRptDao() {
		super();
	}

	private static final Log log = LogFactory.getLog(HibernateAttivaRptDao.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.regioneveneto.mygov.payment.nodoregionalefesp.dao.AttivaRptDao#getByKey
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MygovAttivaRptE getByKey(final String codAttivarptIdentificativoDominio, final String codAttivarptIdentificativoUnivocoVersamento,
			final String codAttivarptCodiceContestoPagamento) {

		log.debug("Recupero della riga di richiesta di attivazione");

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovAttivaRptE.class);
		criteria.add(Restrictions.eq("codAttivarptCodiceContestoPagamento", codAttivarptCodiceContestoPagamento));
		criteria.add(Restrictions.eq("codAttivarptIdentificativoDominio", codAttivarptIdentificativoDominio));
		criteria.add(Restrictions.eq("codAttivarptIdentificativoUnivocoVersamento", codAttivarptIdentificativoUnivocoVersamento));
		criteria.add(Restrictions.eq("deAttivarptEsito", "OK"));

		List<MygovAttivaRptE> results = getHibernateTemplate().findByCriteria(criteria);
		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("'MygovAttivaRptE' is not unique", 1, results.size());

		return results.size() == 0 ? null : (MygovAttivaRptE) results.get(0);
	}

	public MygovAttivaRptE doGet(final long mygovAttivaRptE) throws DataAccessException {

		log.debug("Invocato metodo doGet PARAMETRI ::: " + "mygovAttivaRptE = [" + mygovAttivaRptE);

		return getHibernateTemplate().get(MygovAttivaRptE.class, mygovAttivaRptE);
	}

	@Override
	public MygovAttivaRptE insert(final String codAttivarptIdPsp, final String codAttivarptIdIntermediarioPsp, final String codAttivarptIdCanalePsp,
			final String codAttivarptIdentificativoIntermediarioPa, final String codAttivarptIdentificativoStazioneIntermediarioPa,
			final String codAttivarptIdentificativoDominio, final String codAttivarptIdentificativoUnivocoVersamento,
			final String codAttivarptCodiceContestoPagamento, final Date dtAttivarpt, final BigDecimal numAttivarptImportoSingoloVersamento,
			final String deAttivarptIbanAppoggio, final String deAttivarptBicAppoggio, final String codAttivarptSoggVersIdUnivVersTipoIdUnivoco,
			final String codAttivarptSoggVersIdUnivVersCodiceIdUnivoco, final String deAttivarptSoggVersAnagraficaVersante,
			final String deAttivarptSoggVersIndirizzoVersante, final String deAttivarptSoggVersCivicoVersante, final String codAttivarptSoggVersCapVersante,
			final String deAttivarptSoggVersLocalitaVersante, final String deAttivarptSoggVersProvinciaVersante,
			final String codAttivarptSoggVersNazioneVersante, final String deAttivarptSoggVersEmailVersante, final String deAttivarptIbanAddebito,
			final String deAttivarptBicAddebito, final String codAttivarptSoggPagIdUnivPagTipoIdUnivoco,
			final String codAttivarptSoggPagIdUnivPagCodiceIdUnivoco, final String deAttivarptSoggPagAnagraficaPagatore,
			final String deAttivarptSoggPagIndirizzoPagatore, final String deAttivarptSoggPagCivicoPagatore, final String codAttivarptSoggPagCapPagatore,
			final String deAttivarptSoggPagLocalitaPagatore, final String deAttivarptSoggPagProvinciaPagatore, final String codAttivarptSoggPagNazionePagatore,
			final String deAttivarptSoggPagEmailPagatore) {

		log.debug("Inserimento della riga di richiesta di attivazione");

		MygovAttivaRptE mygovAttivaRptE = new MygovAttivaRptE();

		mygovAttivaRptE.setCodAttivarptIdPsp(codAttivarptIdPsp);
		mygovAttivaRptE.setCodAttivarptIdIntermediarioPsp(codAttivarptIdIntermediarioPsp);
		mygovAttivaRptE.setCodAttivarptIdCanalePsp(codAttivarptIdCanalePsp);

		mygovAttivaRptE.setCodAttivarptIdentificativoIntermediarioPa(codAttivarptIdentificativoIntermediarioPa);
		mygovAttivaRptE.setCodAttivarptIdentificativoStazioneIntermediarioPa(codAttivarptIdentificativoStazioneIntermediarioPa);
		mygovAttivaRptE.setCodAttivarptIdentificativoDominio(codAttivarptIdentificativoDominio);
		mygovAttivaRptE.setCodAttivarptIdentificativoUnivocoVersamento(codAttivarptIdentificativoUnivocoVersamento);
		mygovAttivaRptE.setCodAttivarptCodiceContestoPagamento(codAttivarptCodiceContestoPagamento);
		mygovAttivaRptE.setDtAttivarpt(dtAttivarpt);

		mygovAttivaRptE.setNumAttivarptImportoSingoloVersamento(numAttivarptImportoSingoloVersamento);
		mygovAttivaRptE.setDeAttivarptIbanAppoggio(deAttivarptIbanAppoggio);
		mygovAttivaRptE.setDeAttivarptBicAppoggio(deAttivarptBicAppoggio);
		mygovAttivaRptE.setCodAttivarptSoggVersIdUnivVersTipoIdUnivoco(codAttivarptSoggVersIdUnivVersTipoIdUnivoco);
		mygovAttivaRptE.setCodAttivarptSoggVersIdUnivVersCodiceIdUnivoco(codAttivarptSoggVersIdUnivVersCodiceIdUnivoco);
		mygovAttivaRptE.setDeAttivarptSoggVersAnagraficaVersante(deAttivarptSoggVersAnagraficaVersante);
		mygovAttivaRptE.setDeAttivarptSoggVersIndirizzoVersante(deAttivarptSoggVersIndirizzoVersante);
		mygovAttivaRptE.setDeAttivarptSoggVersCivicoVersante(deAttivarptSoggVersCivicoVersante);
		mygovAttivaRptE.setCodAttivarptSoggVersCapVersante(codAttivarptSoggVersCapVersante);
		mygovAttivaRptE.setDeAttivarptSoggVersLocalitaVersante(deAttivarptSoggVersLocalitaVersante);
		mygovAttivaRptE.setDeAttivarptSoggVersProvinciaVersante(deAttivarptSoggVersProvinciaVersante);
		mygovAttivaRptE.setCodAttivarptSoggVersNazioneVersante(codAttivarptSoggVersNazioneVersante);
		mygovAttivaRptE.setDeAttivarptSoggVersEmailVersante(deAttivarptSoggVersEmailVersante);
		mygovAttivaRptE.setDeAttivarptIbanAddebito(deAttivarptIbanAddebito);
		mygovAttivaRptE.setDeAttivarptBicAddebito(deAttivarptBicAddebito);
		mygovAttivaRptE.setCodAttivarptSoggPagIdUnivPagTipoIdUnivoco(codAttivarptSoggPagIdUnivPagTipoIdUnivoco);
		mygovAttivaRptE.setCodAttivarptSoggPagIdUnivPagCodiceIdUnivoco(codAttivarptSoggPagIdUnivPagCodiceIdUnivoco);
		mygovAttivaRptE.setDeAttivarptSoggPagAnagraficaPagatore(deAttivarptSoggPagAnagraficaPagatore);
		mygovAttivaRptE.setDeAttivarptSoggPagIndirizzoPagatore(deAttivarptSoggPagIndirizzoPagatore);
		mygovAttivaRptE.setDeAttivarptSoggPagCivicoPagatore(deAttivarptSoggPagCivicoPagatore);
		mygovAttivaRptE.setCodAttivarptSoggPagCapPagatore(codAttivarptSoggPagCapPagatore);
		mygovAttivaRptE.setDeAttivarptSoggPagLocalitaPagatore(deAttivarptSoggPagLocalitaPagatore);
		mygovAttivaRptE.setDeAttivarptSoggPagProvinciaPagatore(deAttivarptSoggPagProvinciaPagatore);
		mygovAttivaRptE.setCodAttivarptSoggPagNazionePagatore(codAttivarptSoggPagNazionePagatore);
		mygovAttivaRptE.setDeAttivarptSoggPagEmailPagatore(deAttivarptSoggPagEmailPagatore);

		getHibernateTemplate().save(mygovAttivaRptE);
		getHibernateTemplate().flush();
		getHibernateTemplate().refresh(mygovAttivaRptE);

		log.debug("Inserimento della riga di richiesta di attivazione andato a buon fine");
		return mygovAttivaRptE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.regioneveneto.mygov.payment.nodoregionalefesp.dao.AttivaRptDao#updateByKey
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.util.Date, java.math.BigDecimal,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovAttivaRptE updateByKey(final long mygovAttivaRptEId, final Date dtEAttivarpt, final BigDecimal numEAttivarptImportoSingoloVersamento,
			final String deEAttivarptIbanAccredito, final String deEAttivarptBicAccredito, final String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco,
			final String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco, final String deEAttivarptEnteBenefDenominazioneBeneficiario,
			final String codEAttivarptEnteBenefCodiceUnitOperBeneficiario, final String deEAttivarptEnteBenefDenomUnitOperBeneficiario,
			final String deEAttivarptEnteBenefIndirizzoBeneficiario, final String deEAttivarptEnteBenefCivicoBeneficiario,
			final String codEAttivarptEnteBenefCapBeneficiario, final String deEAttivarptEnteBenefLocalitaBeneficiario,
			final String deEAttivarptEnteBenefProvinciaBeneficiario, final String codEAttivarptEnteBenefNazioneBeneficiario,
			final String deEAttivarptCredenzialiPagatore, final String deEAttivarptCausaleVersamento, final String deAttivarptEsito,
			final String codAttivarptFaultCode, final String deAttivarptFaultString, final String codAttivarptId, final String deAttivarptDescription,
			final Integer codAttivarptSerial, final String codAttivarptOriginalFaultCode, final String deAttivarptOriginalFaultString, 
			final String deAttivarptOriginalFaultDescription) {

		MygovAttivaRptE mygovAttivaRptE = doGet(mygovAttivaRptEId);

		mygovAttivaRptE.setDtEAttivarpt(dtEAttivarpt);
		mygovAttivaRptE.setNumEAttivarptImportoSingoloVersamento(numEAttivarptImportoSingoloVersamento);
		mygovAttivaRptE.setDeEAttivarptIbanAccredito(deEAttivarptIbanAccredito);
		mygovAttivaRptE.setDeEAttivarptBicAccredito(deEAttivarptBicAccredito);
		mygovAttivaRptE.setCodEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco(codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco);
		mygovAttivaRptE.setCodEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco(codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco);
		mygovAttivaRptE.setDeEAttivarptEnteBenefDenominazioneBeneficiario(deEAttivarptEnteBenefDenominazioneBeneficiario);
		mygovAttivaRptE.setCodEAttivarptEnteBenefCodiceUnitOperBeneficiario(codEAttivarptEnteBenefCodiceUnitOperBeneficiario);
		mygovAttivaRptE.setDeEAttivarptEnteBenefDenomUnitOperBeneficiario(deEAttivarptEnteBenefDenomUnitOperBeneficiario);
		mygovAttivaRptE.setDeEAttivarptEnteBenefIndirizzoBeneficiario(deEAttivarptEnteBenefIndirizzoBeneficiario);
		mygovAttivaRptE.setDeEAttivarptEnteBenefCivicoBeneficiario(deEAttivarptEnteBenefCivicoBeneficiario);
		mygovAttivaRptE.setCodEAttivarptEnteBenefCapBeneficiario(codEAttivarptEnteBenefCapBeneficiario);
		mygovAttivaRptE.setDeEAttivarptEnteBenefLocalitaBeneficiario(deEAttivarptEnteBenefLocalitaBeneficiario);
		mygovAttivaRptE.setDeEAttivarptEnteBenefProvinciaBeneficiario(deEAttivarptEnteBenefProvinciaBeneficiario);
		mygovAttivaRptE.setCodEAttivarptEnteBenefNazioneBeneficiario(codEAttivarptEnteBenefNazioneBeneficiario);
		mygovAttivaRptE.setDeEAttivarptCredenzialiPagatore(deEAttivarptCredenzialiPagatore);
		mygovAttivaRptE.setDeEAttivarptCausaleVersamento(deEAttivarptCausaleVersamento);

		// Campi esito errore
		mygovAttivaRptE.setDeAttivarptEsito(deAttivarptEsito);
		mygovAttivaRptE.setCodAttivarptFaultCode(codAttivarptFaultCode);
		mygovAttivaRptE.setDeAttivarptFaultString(deAttivarptFaultString);
		mygovAttivaRptE.setCodAttivarptId(codAttivarptId);
		
		// Tronco deAttivarptDescription se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(deAttivarptDescription)) {
			if (deAttivarptDescription.length() > 1024)
				mygovAttivaRptE.setDeAttivarptDescription(deAttivarptDescription.substring(0, 1024));
			else
				mygovAttivaRptE.setDeAttivarptDescription(deAttivarptDescription);
		}
		
		mygovAttivaRptE.setCodAttivarptOriginalFaultCode(codAttivarptOriginalFaultCode); 
		mygovAttivaRptE.setDeAttivarptOriginalFaultString(deAttivarptOriginalFaultString);
		// Tronco deAttivarptDescription se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(deAttivarptOriginalFaultDescription)) {
			if (deAttivarptOriginalFaultDescription.length() > 1024)
				mygovAttivaRptE.setDeAttivarptOriginalFaultDescription(deAttivarptOriginalFaultDescription.substring(0, 1024));
			else
				mygovAttivaRptE.setDeAttivarptOriginalFaultDescription(deAttivarptOriginalFaultDescription);
		}
		
		mygovAttivaRptE.setCodAttivarptSerial(codAttivarptSerial);

		getHibernateTemplate().saveOrUpdate(mygovAttivaRptE);

		return mygovAttivaRptE;
	}

	@Override
	public MygovAttivaRptE updateEsitoByKey(long mygovAttivaRptEId, String deAttivarptEsito) {

		MygovAttivaRptE mygovAttivaRptE = doGet(mygovAttivaRptEId);

		mygovAttivaRptE.setDeAttivarptEsito(deAttivarptEsito);

		getHibernateTemplate().saveOrUpdate(mygovAttivaRptE);

		return mygovAttivaRptE;
	}

}
