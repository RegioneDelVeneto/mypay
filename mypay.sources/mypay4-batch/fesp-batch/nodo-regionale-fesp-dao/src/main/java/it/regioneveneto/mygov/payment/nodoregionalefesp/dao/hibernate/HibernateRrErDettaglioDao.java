package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrErDettaglio;

public class HibernateRrErDettaglioDao extends HibernateDaoSupport implements RrErDettaglioDao{
	
	private static final Log log = LogFactory.getLog(HibernateRrErDettaglioDao.class);

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDettaglioDao#insert(it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal)
	 */
	@Override
	public MygovRrErDettaglio insert(MygovRrEr rr, String codRrDatiSingRevIdUnivocoRiscossione,
			String deRrDatiSingRevCausaleRevoca, String deRrDatiSingRevDatiAggiuntiviRevoca,
			BigDecimal numRrDatiSingRevSingoloImportoRevocato) throws DataAccessException {

		return doInsert(rr, codRrDatiSingRevIdUnivocoRiscossione, deRrDatiSingRevCausaleRevoca, deRrDatiSingRevDatiAggiuntiviRevoca,
				numRrDatiSingRevSingoloImportoRevocato);
	}

	/**
	 * 
	 * @param rr
	 * @param codRrDatiSingRevIdUnivocoRiscossione
	 * @param deRrDatiSingRevCausaleRevoca
	 * @param deRrDatiSingRevDatiAggiuntiviRevoca
	 * @param numRrDatiSingRevSingoloImportoRevocato
	 * @return
	 */
	private MygovRrErDettaglio doInsert(MygovRrEr rr, String codRrDatiSingRevIdUnivocoRiscossione, String deRrDatiSingRevCausaleRevoca, 
			String deRrDatiSingRevDatiAggiuntiviRevoca, BigDecimal numRrDatiSingRevSingoloImportoRevocato) {
		
		MygovRrErDettaglio mygovRevocaDettaglioPagamenti = new MygovRrErDettaglio();
		
		mygovRevocaDettaglioPagamenti.setMygov_rr_er(rr);
		mygovRevocaDettaglioPagamenti.setDtCreazione(new Date());
		mygovRevocaDettaglioPagamenti.setDtUltimaModifica(new Date());
		mygovRevocaDettaglioPagamenti.setCodRrDatiSingRevIdUnivocoRiscossione(codRrDatiSingRevIdUnivocoRiscossione);
		mygovRevocaDettaglioPagamenti.setDeRrDatiSingRevCausaleRevoca(deRrDatiSingRevCausaleRevoca);
		mygovRevocaDettaglioPagamenti.setDeRrDatiSingRevDatiAggiuntiviRevoca(deRrDatiSingRevDatiAggiuntiviRevoca);
		mygovRevocaDettaglioPagamenti.setNumRrDatiSingRevSingoloImportoRevocato(numRrDatiSingRevSingoloImportoRevocato);
		
		getHibernateTemplate().save(mygovRevocaDettaglioPagamenti);
		return mygovRevocaDettaglioPagamenti;
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDettaglioDao#getByRevoca(it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<MygovRrErDettaglio> getByRevoca(MygovRrEr mygovRevoca) throws DataAccessException{
		
		log.debug("Invocato metodo getByRevoca per ID = [" + mygovRevoca.getMygovRrErId() + "]");
		
		List<MygovRrErDettaglio> results = getHibernateTemplate().find(
				"from MygovRrErDettaglio mygovRrErDettaglio where mygovRrErDettaglio.mygov_rr_er = ? order by mygovRrErDettaglio.mygovRrErDettaglioId", mygovRevoca);
		
		return results;
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDettaglioDao#updateEsitoDettaglioRevoca(java.lang.Long, int, it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal)
	 */
	@Override
	public MygovRrErDettaglio updateEsitoDettaglioRevoca(Long mygovRevocaDettaglioPagamentiId, int version, MygovRrEr mygovRevoca,
			String codErDatiSingRevIdUnivocoRiscossione, String deErDatiSingRevCausaleRevoca,
			String deErDatiSingRevDatiAggiuntiviRevoca, BigDecimal numErDatiSingRevSingoloImportoRevocato) 
					throws DataAccessException{
		
		
		MygovRrErDettaglio mygovRevocaDettaglioPagamenti = doGet(mygovRevocaDettaglioPagamentiId);
		
		if(null == mygovRevocaDettaglioPagamenti)
			throw new DataRetrievalFailureException("'mygovRevocaDettaglioPagamentiId' is not valid");
		
		mygovRevocaDettaglioPagamenti.setDtUltimaModifica(new Date());
		
		mygovRevocaDettaglioPagamenti.setCodErDatiSingRevIdUnivocoRiscossione(codErDatiSingRevIdUnivocoRiscossione);
		mygovRevocaDettaglioPagamenti.setDeErDatiSingRevCausaleRevoca(deErDatiSingRevCausaleRevoca);
		mygovRevocaDettaglioPagamenti.setDeErDatiSingRevDatiAggiuntiviRevoca(deErDatiSingRevDatiAggiuntiviRevoca);
		mygovRevocaDettaglioPagamenti.setNumErDatiSingRevSingoloImportoRevocato(numErDatiSingRevSingoloImportoRevocato);
		
		if(version != mygovRevocaDettaglioPagamenti.getVersion())
			throw new ObjectOptimisticLockingFailureException(MygovRrErDettaglio.class, mygovRevocaDettaglioPagamentiId);
		
		getHibernateTemplate().update(mygovRevocaDettaglioPagamenti);
		
		return mygovRevocaDettaglioPagamenti;
		
	}

	/**
	 * 
	 * @param mygovRevocaDettaglioPagamentiId
	 * @return
	 */
	private MygovRrErDettaglio doGet(Long mygovRevocaDettaglioPagamentiId) {

		log.debug("Invocato metodo doGet PARAMETRI ::: " + "mygovRevocaDettaglioPagamentiId = [" + mygovRevocaDettaglioPagamentiId + "]");
		
		return getHibernateTemplate().get(MygovRrErDettaglio.class, mygovRevocaDettaglioPagamentiId);
	}

}
