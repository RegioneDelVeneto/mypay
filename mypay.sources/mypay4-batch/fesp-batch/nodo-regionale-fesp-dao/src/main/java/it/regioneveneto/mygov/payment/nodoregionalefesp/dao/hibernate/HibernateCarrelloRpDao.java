package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

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

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.CarrelloRpDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpE;


public class HibernateCarrelloRpDao extends HibernateDaoSupport implements CarrelloRpDao {
	
	private static final Log log = LogFactory.getLog(HibernateCarrelloRpDao.class);

	@Override
	public MygovCarrelloRp insertWithRefresh(String id_session, String dominioChiamante) {
		
		MygovCarrelloRp mygovCarrelloRp = new MygovCarrelloRp();
		Date now = new Date();
		mygovCarrelloRp.setDtCreazione(now);
		mygovCarrelloRp.setDtUltimaModifica(now);
		mygovCarrelloRp.setIdSessionCarrello(id_session);
		mygovCarrelloRp.setCodiceFiscaleEnte(dominioChiamante);
		//mygovCarrelloRp.setVersion(1);

		getHibernateTemplate().save(mygovCarrelloRp);
		getHibernateTemplate().flush();
		getHibernateTemplate().refresh(mygovCarrelloRp);

		return mygovCarrelloRp;
	}

	@Override
	public MygovCarrelloRp updateRispostaRpByRpId(Long mygovCarrelloRpId, String esito, String url, String faultCode,
			String faultString, String id, Integer serial, String idSession, String originalFaultCode, 
			String originalFaultString, String originalFaultDescription) {
		
		

		MygovCarrelloRp mygovCarrelloRp = doGet(mygovCarrelloRpId);

		mygovCarrelloRp.setDtUltimaModifica(new Date());
		mygovCarrelloRp.setDeRpSilinviacarrellorpEsito(esito);
	
		mygovCarrelloRp.setCodRpSilinviacarrellorpUrl(url);
		mygovCarrelloRp.setCodRpSilinviacarrellorpFaultCode(faultCode);
		mygovCarrelloRp.setDeRpSilinviacarrellorpFaultString(faultString);

		mygovCarrelloRp.setCodRpSilinviacarrellorpId(id);
		
		mygovCarrelloRp.setCodRpSilinviacarrellorpOriginalFaultCode(originalFaultCode);
		mygovCarrelloRp.setDeRpSilinviacarrellorpOriginalFaultString(originalFaultString);
		if (StringUtils.isNotBlank(originalFaultDescription)) {
			if (originalFaultDescription.length() > 1024) 
				mygovCarrelloRp.setDeRpSilinviacarrellorpOriginalFaultDescription(originalFaultDescription.substring(0, 1024));
			else 
				mygovCarrelloRp.setDeRpSilinviacarrellorpOriginalFaultDescription(originalFaultDescription);
		}
		

		mygovCarrelloRp.setCodRpSilinviacarrellorpSerial(serial);
		mygovCarrelloRp.setIdSessionCarrello(idSession);

		

		getHibernateTemplate().update(mygovCarrelloRp);
		
		return mygovCarrelloRp;

	
	}
	
	@Override
	public MygovCarrelloRp getCarrelloRpByIdSession(String nodoRegionaleFespIdSession) {
		//		List<MygovRpE> results = getHibernateTemplate().find("from MygovRpE WHERE idSession = '" + idSession + "'");
		MygovCarrelloRp car = new MygovCarrelloRp();
	
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovCarrelloRp.class);
		criteria.add(Restrictions.eq("idSessionCarrello", nodoRegionaleFespIdSession));
		List<MygovCarrelloRp> results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("Piu di un CarrelloRP ritornata per idSession", 1, results.size());
		return results.size() == 0 ? null : (MygovCarrelloRp) results.get(0);
	}
	
	@SuppressWarnings("unchecked")
	protected MygovCarrelloRp doGet(final long mygovCarrelloRpId) throws DataAccessException {
		List<MygovCarrelloRp> results = getHibernateTemplate().find("from MygovCarrelloRp where mygovCarrelloRpId = ?", mygovCarrelloRpId);
		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("'mygovCarrelloRpId' is not unique", 1, results.size());

		return results.size() == 0 ? null : (MygovCarrelloRp) results.get(0);
	}

	@Override
	public MygovCarrelloRp getCarrelloRpById(Long mygovCarrelloRpId) {
		
		return doGet(mygovCarrelloRpId);
	}
	

}
