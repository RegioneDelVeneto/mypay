package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.CarrelloRptDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRpt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;

public class HibernateCarrelloRptDao extends HibernateDaoSupport implements CarrelloRptDao {

	@Override
	public MygovCarrelloRpt insertWithRefresh(MygovCarrelloRp carrelloRp, String identificativoIntermediarioPa, 
			String identificativoStazioneIntermediarioPa, String password, String identificativoCanale, String identificativoIntermediarioPsp, String identificativoPsp) {
		
		MygovCarrelloRpt mygovCarrelloRpt = new MygovCarrelloRpt();
		Date now = new Date();
		mygovCarrelloRpt.setDtCreazione(now);
		mygovCarrelloRpt.setDtUltimaModifica(now);
		mygovCarrelloRpt.setMygovCarrelloRpId(carrelloRp.getMygovCarrelloRpId());
		
		mygovCarrelloRpt.setCodRptInviacarrellorptIdCarrello(carrelloRp.getIdSessionCarrello());
		mygovCarrelloRpt.setCodRptInviacarrellorptIdIntermediarioPa(identificativoIntermediarioPa);
		mygovCarrelloRpt.setCodRptInviacarrellorptIdStazioneIntermediarioPa(identificativoStazioneIntermediarioPa);
		mygovCarrelloRpt.setDeRptInviacarrellorptPassword(password);
		
		mygovCarrelloRpt.setCodRptInviacarrellorptIdCanale(identificativoCanale);
		mygovCarrelloRpt.setCodRptInviacarrellorptIdIntermediarioPsp(identificativoIntermediarioPsp);
		mygovCarrelloRpt.setCodRptInviacarrellorptIdPsp(identificativoPsp);

		getHibernateTemplate().save(mygovCarrelloRpt);
		getHibernateTemplate().flush();
		getHibernateTemplate().refresh(mygovCarrelloRpt);

		return mygovCarrelloRpt;
	}


		
		
	

	@Override
	public void updateRispostaRptById(Long mygovCarrelloRptId, String esitoComplessivoOperazione, String url,
			String faultCode, String faultString, String faultId, String faultDescription, Integer faultSerial,
			String idSessionSPC, String originalFaultCode, String originalFaultString, String originalFaultDescription) {
		
		MygovCarrelloRpt myGovCarrelloRpt = doGet(mygovCarrelloRptId);

		myGovCarrelloRpt.setDtUltimaModifica(new Date());
		myGovCarrelloRpt.setDeRptInviacarrellorptEsitoComplessivoOperazione(esitoComplessivoOperazione);
	    myGovCarrelloRpt.setCodRptInviacarrellorptUrl(url);
		myGovCarrelloRpt.setCodRptInviacarrellorptFaultCode(faultCode);
		myGovCarrelloRpt.setCodRptInviacarrellorptFaultString(faultString);
		myGovCarrelloRpt.setCodRptInviacarrellorptId(faultId);

		// Tronco deRptInviarptDescription se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(faultDescription)) {
			if (faultDescription.length() > 1024) {
				myGovCarrelloRpt.setDeRptInviacarrellorptDescription(faultDescription.substring(0, 1024));
			}
			else {
				myGovCarrelloRpt.setDeRptInviacarrellorptDescription(faultDescription);
			}
		}
		else {
			myGovCarrelloRpt.setDeRptInviacarrellorptDescription(faultDescription);
		}
		
		
		myGovCarrelloRpt.setCodRptSilinviacarrellorptOriginalFaultCode(originalFaultCode);
		myGovCarrelloRpt.setDeRptSilinviacarrellorptOriginalFaultString(originalFaultString);
		if (StringUtils.isNotBlank(originalFaultDescription)) {
			if (originalFaultDescription.length() > 1024) {
				myGovCarrelloRpt.setDeRptSilinviacarrellorptOriginalFaultDescription(originalFaultDescription.substring(0, 1024));
			}
			else {
				myGovCarrelloRpt.setDeRptSilinviacarrellorptOriginalFaultDescription(originalFaultDescription);
			}
		}

		myGovCarrelloRpt.setNumRptInviacarrellorptSerial(faultSerial);
		
		// sovrascrivo campo solo se riesco ad estrarre idSessionSPC da url
		// in risposta
		if (StringUtils.isNotBlank(idSessionSPC))
			myGovCarrelloRpt.setCodRptInviacarrellorptIdCarrello(idSessionSPC);
		

		getHibernateTemplate().update(myGovCarrelloRpt);

		
	}
	
	@SuppressWarnings("unchecked")
	protected MygovCarrelloRpt doGet(final long mygovCarrelloRptId) throws DataAccessException {
		List<MygovCarrelloRpt> results = getHibernateTemplate().find("from MygovCarrelloRpt where mygovCarrelloRptId = ?", mygovCarrelloRptId);
		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("'mygovCarrelloRptId' is not unique", 1, results.size());

		return results.size() == 0 ? null : (MygovCarrelloRpt) results.get(0);
	}






	@Override
	public MygovCarrelloRpt getCarrelloRptByRpEId(Long mygovCarrelloRpId) {
		MygovCarrelloRpt car = new MygovCarrelloRpt();
	
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovCarrelloRpt.class);
		criteria.add(Restrictions.eq("mygovCarrelloRpId", mygovCarrelloRpId));
		List<MygovCarrelloRpt> results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("Piu di un CarrelloRPT ritornata per idSession", 1, results.size());
		return results.size() == 0 ? null : (MygovCarrelloRpt) results.get(0);
	}






	@Override
	public MygovCarrelloRpt getCarrelloRptByIdSession(String nodoSPCFespIdSession) {
		MygovCarrelloRpt car = new MygovCarrelloRpt();
		
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovCarrelloRpt.class);
		criteria.add(Restrictions.eq("codRptInviacarrellorptIdCarrello", nodoSPCFespIdSession));
		List<MygovCarrelloRpt> results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("Piu di un CarrelloRPT ritornata per idSession", 1, results.size());
		return results.size() == 0 ? null : (MygovCarrelloRpt) results.get(0);
	}
	
	

}
