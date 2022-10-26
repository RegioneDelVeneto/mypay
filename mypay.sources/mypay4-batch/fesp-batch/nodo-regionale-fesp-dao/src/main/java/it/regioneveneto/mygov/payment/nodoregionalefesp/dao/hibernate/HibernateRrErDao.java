package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument.Restriction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.exceptions.MandatoryFieldsException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr;

public class HibernateRrErDao extends HibernateDaoSupport implements RrErDao{
	
	private static final Log log = LogFactory.getLog(HibernateRrErDao.class);
	
	private RrErDettaglioDao rrErDettaglioDao;

	/**
	 * @param rrErDettaglioDao the rrErDettaglioDao to set
	 */
	public void setRrErDettaglioDao(RrErDettaglioDao rrErDettaglioDao) {
		this.rrErDettaglioDao = rrErDettaglioDao;
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao#insertRRWithRefresh(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovRrEr insertRRWithRefresh(String codIdUnivocoVersamento, String codCodiceContestoPagamento,
			String codRrDomIdDominio, String codRrDomIdStazioneRichiedente, String codRrIdMessaggioRevoca,
			String deRrVesioneOggetto, Date dtRrDataOraMessaggioRevoca, String deRrIstitAttDenominazioneMittente,
			String codRrIstitAttUnitOperMittente, String deRrIstitAttDenomUnitOperMittente,
			String deRrIstitAttIndirizzoMittente, String deRrIstitAttCivicoMittente, String codRrIstitAttCapMittente,
			String deRrIstitAttLocalitaMittente, String deRrIstitAttProvinciaMittente,
			String codRrIstitAttNazioneMittente, String codRrIstitAttIdUnivMittTipoIdUnivoco,
			String codRrIstitAttIdUnivMittCodiceIdUnivoco, String codRrSoggVersIdUnivVersTipoIdUnivoco,
			String codRrSoggVersIdUnivVersCodiceIdUnivoco, String codRrSoggVersAnagraficaVersante,
			String deRrSoggVersIndirizzoVersante, String deRrSoggVersCivicoVersante, String codRrSoggVersCapVersante,
			String deRrSoggVersLocalitaVersante, String deRrSoggVersProvinciaVersante,
			String codRrSoggVersNazioneVersante, String deRrSoggVersEmailVersante,
			String codRrSoggPagIdUnivPagTipoIdUnivoco, String codRrSoggPagIdUnivPagCodiceIdUnivoco,
			String codRrSoggPagAnagraficaPagatore, String deRrSoggPagIndirizzoPagatore,
			String deRrSoggPagCivicoPagatore, String codRrSoggPagCapPagatore, String deRrSoggPagLocalitaPagatore,
			String deRrSoggPagProvinciaPagatore, String codRrSoggPagNazionePagatore, String deRrSoggPagEmailPagatore,
			BigDecimal numRrDatiRevImportoTotaleRevocato, String codRrDatiRevIdUnivocoVersamento,
			String codRrDatiRevCodiceContestoPagamento, String codRrDatiRevTipoRevoca) throws DataAccessException {
		
		log.debug("Invocato metodo insertRRWithRefresh con PARAMETRI:::" + 
				" codIdUnivocoVersamento: " + codIdUnivocoVersamento +  
				" codCodiceContestoPagamento: " + codCodiceContestoPagamento +
				" codRrDomIdDominio: " + codRrDomIdDominio +
				" codRrDomIdStazioneRichiedente: " + codRrDomIdStazioneRichiedente +
				" codRrIdMessaggioRevoca: " + codCodiceContestoPagamento +
				" deRrVesioneOggetto: " + deRrVesioneOggetto +
				" dtRrDataOraMessaggioRevoca: " + dtRrDataOraMessaggioRevoca +
				" deRrIstitAttDenominazioneMittente: " + deRrIstitAttDenominazioneMittente +
				" codRrIstitAttUnitOperMittente: " + codRrIstitAttUnitOperMittente +
				" deRrIstitAttDenomUnitOperMittente: " + deRrIstitAttDenomUnitOperMittente +
				" deRrIstitAttIndirizzoMittente: " + deRrIstitAttIndirizzoMittente +
				" deRrIstitAttCivicoMittente: " + deRrIstitAttCivicoMittente +
				" codRrIstitAttCapMittente: " + codRrIstitAttCapMittente +
				" deRrIstitAttLocalitaMittente: " + deRrIstitAttLocalitaMittente +
				" deRrIstitAttProvinciaMittente: " + deRrIstitAttProvinciaMittente +
				" codRrIstitAttNazioneMittente: " + codRrIstitAttNazioneMittente +
				" codRrIstitAttIdUnivMittTipoIdUnivoco: " + codRrIstitAttIdUnivMittTipoIdUnivoco +
				" codRrIstitAttIdUnivMittCodiceIdUnivoco: " + codRrIstitAttIdUnivMittCodiceIdUnivoco +
				" codRrSoggVersIdUnivVersTipoIdUnivoco: " + codRrSoggVersIdUnivVersTipoIdUnivoco +
				" codRrSoggVersIdUnivVersCodiceIdUnivoco: " + codRrSoggVersIdUnivVersCodiceIdUnivoco +
				" codRrSoggVersAnagraficaVersante: " + codRrSoggVersAnagraficaVersante +
				" deRrSoggVersIndirizzoVersante: " + deRrSoggVersIndirizzoVersante +
				" deRrSoggVersCivicoVersante: " + deRrSoggVersCivicoVersante +
				" codRrSoggVersCapVersante: " + codRrSoggVersCapVersante +
				" deRrSoggVersLocalitaVersante: " + deRrSoggVersLocalitaVersante +
				" deRrSoggVersProvinciaVersante: " + deRrSoggVersProvinciaVersante +
				" codRrSoggVersNazioneVersante: " + codRrSoggVersNazioneVersante +
				" deRrSoggVersEmailVersante: " + deRrSoggVersEmailVersante +
				" codRrSoggPagIdUnivPagTipoIdUnivoco: " + codRrSoggPagIdUnivPagTipoIdUnivoco +
				" codRrSoggPagIdUnivPagCodiceIdUnivoco: " + codRrSoggPagIdUnivPagCodiceIdUnivoco +
				" codRrSoggPagAnagraficaPagatore: " + codRrSoggPagAnagraficaPagatore +
				" deRrSoggPagIndirizzoPagatore: " + deRrSoggPagIndirizzoPagatore +
				" deRrSoggPagCivicoPagatore: " + deRrSoggPagCivicoPagatore +
				" codRrSoggPagCapPagatore: " + codRrSoggPagCapPagatore +
				" deRrSoggPagLocalitaPagatore: " + deRrSoggPagLocalitaPagatore +
				" deRrSoggPagProvinciaPagatore: " + deRrSoggPagProvinciaPagatore +
				" codRrSoggPagNazionePagatore: " + codRrSoggPagNazionePagatore +
				" deRrSoggPagEmailPagatore: " + deRrSoggPagEmailPagatore +
				" numRrDatiRevImportoTotaleRevocato: " + numRrDatiRevImportoTotaleRevocato +
				" codRrDatiRevIdUnivocoVersamento: " + codRrDatiRevIdUnivocoVersamento +
				" codRrDatiRevCodiceContestoPagamento: " + codRrDatiRevCodiceContestoPagamento +
				" codRrDatiRevTipoRevoca: " + codRrDatiRevTipoRevoca );
				
		MygovRrEr mygovRevoca = doInsert(codIdUnivocoVersamento, codCodiceContestoPagamento, codRrDomIdDominio,  
				codRrDomIdStazioneRichiedente, codRrIdMessaggioRevoca, deRrVesioneOggetto,dtRrDataOraMessaggioRevoca, deRrIstitAttDenominazioneMittente,
				codRrIstitAttUnitOperMittente, deRrIstitAttDenomUnitOperMittente, deRrIstitAttIndirizzoMittente, deRrIstitAttCivicoMittente,  codRrIstitAttCapMittente,
				deRrIstitAttLocalitaMittente, deRrIstitAttProvinciaMittente, codRrIstitAttNazioneMittente, codRrIstitAttIdUnivMittTipoIdUnivoco,
				codRrIstitAttIdUnivMittCodiceIdUnivoco, codRrSoggVersIdUnivVersTipoIdUnivoco, codRrSoggVersIdUnivVersCodiceIdUnivoco, codRrSoggVersAnagraficaVersante,
				deRrSoggVersIndirizzoVersante, deRrSoggVersCivicoVersante, codRrSoggVersCapVersante, deRrSoggVersLocalitaVersante, deRrSoggVersProvinciaVersante,
				codRrSoggVersNazioneVersante, deRrSoggVersEmailVersante, codRrSoggPagIdUnivPagTipoIdUnivoco, codRrSoggPagIdUnivPagCodiceIdUnivoco,
				codRrSoggPagAnagraficaPagatore, deRrSoggPagIndirizzoPagatore, deRrSoggPagCivicoPagatore, codRrSoggPagCapPagatore, deRrSoggPagLocalitaPagatore,
				deRrSoggPagProvinciaPagatore, codRrSoggPagNazionePagatore, deRrSoggPagEmailPagatore, numRrDatiRevImportoTotaleRevocato, codRrDatiRevIdUnivocoVersamento,
				codRrDatiRevCodiceContestoPagamento, codRrDatiRevTipoRevoca);
		
		getHibernateTemplate().flush();
		getHibernateTemplate().refresh(mygovRevoca);
		
		return mygovRevoca;
	}

	/**
	 * 
	 * @param codIdUnivocoVersamento
	 * @param codCodiceContestoPagamento
	 * @param codRrDomIdDominio
	 * @param codRrDomIdStazioneRichiedente
	 * @param codRrIdMessaggioRevoca
	 * @param deRrVesioneOggetto 
	 * @param dtRrDataOraMessaggioRevoca
	 * @param deRrIstitAttDenominazioneMittente
	 * @param codRrIstitAttUnitOperMittente
	 * @param deRrIstitAttDenomUnitOperMittente
	 * @param deRrIstitAttIndirizzoMittente
	 * @param deRrIstitAttCivicoMittente
	 * @param codRrIstitAttCapMittente
	 * @param deRrIstitAttLocalitaMittente
	 * @param deRrIstitAttProvinciaMittente
	 * @param codRrIstitAttNazioneMittente
	 * @param codRrIstitAttIdUnivMittTipoIdUnivoco
	 * @param codRrIstitAttIdUnivMittCodiceIdUnivoco
	 * @param codRrSoggVersIdUnivVersTipoIdUnivoco
	 * @param codRrSoggVersIdUnivVersCodiceIdUnivoco
	 * @param codRrSoggVersAnagraficaVersante
	 * @param deRrSoggVersIndirizzoVersante
	 * @param deRrSoggVersCivicoVersante
	 * @param codRrSoggVersCapVersante
	 * @param deRrSoggVersLocalitaVersante
	 * @param deRrSoggVersProvinciaVersante
	 * @param codRrSoggVersNazioneVersante
	 * @param deRrSoggVersEmailVersante
	 * @param codRrSoggPagIdUnivPagTipoIdUnivoco
	 * @param codRrSoggPagIdUnivPagCodiceIdUnivoco
	 * @param codRrSoggPagAnagraficaPagatore
	 * @param deRrSoggPagIndirizzoPagatore
	 * @param deRrSoggPagCivicoPagatore
	 * @param codRrSoggPagCapPagatore
	 * @param deRrSoggPagLocalitaPagatore
	 * @param deRrSoggPagProvinciaPagatore
	 * @param codRrSoggPagNazionePagatore
	 * @param deRrSoggPagEmailPagatore
	 * @param numRrDatiRevImportoTotaleRevocato
	 * @param codRrDatiRevIdUnivocoVersamento
	 * @param codRrDatiRevCodiceContestoPagamento
	 * @param codRrDatiRevTipoRevoca
	 * @return
	 * @throws DataAccessException
	 */
	private MygovRrEr doInsert(String codIdUnivocoVersamento, String codCodiceContestoPagamento,
			String codRrDomIdDominio, String codRrDomIdStazioneRichiedente, String codRrIdMessaggioRevoca,
			String deRrVersioneOggetto, Date dtRrDataOraMessaggioRevoca, String deRrIstitAttDenominazioneMittente,
			String codRrIstitAttUnitOperMittente, String deRrIstitAttDenomUnitOperMittente,
			String deRrIstitAttIndirizzoMittente, String deRrIstitAttCivicoMittente, String codRrIstitAttCapMittente,
			String deRrIstitAttLocalitaMittente, String deRrIstitAttProvinciaMittente,
			String codRrIstitAttNazioneMittente, String codRrIstitAttIdUnivMittTipoIdUnivoco,
			String codRrIstitAttIdUnivMittCodiceIdUnivoco, String codRrSoggVersIdUnivVersTipoIdUnivoco,
			String codRrSoggVersIdUnivVersCodiceIdUnivoco, String codRrSoggVersAnagraficaVersante,
			String deRrSoggVersIndirizzoVersante, String deRrSoggVersCivicoVersante, String codRrSoggVersCapVersante,
			String deRrSoggVersLocalitaVersante, String deRrSoggVersProvinciaVersante,
			String codRrSoggVersNazioneVersante, String deRrSoggVersEmailVersante,
			String codRrSoggPagIdUnivPagTipoIdUnivoco, String codRrSoggPagIdUnivPagCodiceIdUnivoco,
			String codRrSoggPagAnagraficaPagatore, String deRrSoggPagIndirizzoPagatore,
			String deRrSoggPagCivicoPagatore, String codRrSoggPagCapPagatore, String deRrSoggPagLocalitaPagatore,
			String deRrSoggPagProvinciaPagatore, String codRrSoggPagNazionePagatore, String deRrSoggPagEmailPagatore,
			BigDecimal numRrDatiRevImportoTotaleRevocato, String codRrDatiRevIdUnivocoVersamento,
			String codRrDatiRevCodiceContestoPagamento, String codRrDatiRevTipoRevoca){
		
		MygovRrEr mygovRevoca = new MygovRrEr();
		
		mygovRevoca.setDtCreazione(new Date());
		mygovRevoca.setDtUltimaModifica(new Date());
		mygovRevoca.setCodIdUnivocoVersamento(codIdUnivocoVersamento);
		mygovRevoca.setCodCodiceContestoPagamento(codCodiceContestoPagamento);
		mygovRevoca.setCodRrDomIdDominio(codRrDomIdDominio); 
		mygovRevoca.setCodRrDomIdStazioneRichiedente(codRrDomIdStazioneRichiedente); 
		mygovRevoca.setCodRrIdMessaggioRevoca(codRrIdMessaggioRevoca);
		mygovRevoca.setDeRrVersioneOggetto(deRrVersioneOggetto);
		mygovRevoca.setDtRrDataOraMessaggioRevoca(dtRrDataOraMessaggioRevoca); 
		mygovRevoca.setDeRrIstitAttDenominazioneMittente(deRrIstitAttDenominazioneMittente);
		mygovRevoca.setCodRrIstitAttUnitOperMittente(codRrIstitAttUnitOperMittente); 
		mygovRevoca.setDeRrIstitAttDenomUnitOperMittente(deRrIstitAttDenomUnitOperMittente);
		mygovRevoca.setDeRrIstitAttIndirizzoMittente(deRrIstitAttIndirizzoMittente); 
		mygovRevoca.setDeRrIstitAttCivicoMittente(deRrIstitAttCivicoMittente); 
		mygovRevoca.setCodRrIstitAttCapMittente(codRrIstitAttCapMittente);
		mygovRevoca.setDeRrIstitAttLocalitaMittente(deRrIstitAttLocalitaMittente); 
		mygovRevoca.setDeRrIstitAttProvinciaMittente(deRrIstitAttProvinciaMittente);
		mygovRevoca.setCodRrIstitAttNazioneMittente(codRrIstitAttNazioneMittente); 
		mygovRevoca.setCodRrIstitAttIdUnivMittTipoIdUnivoco(codRrIstitAttIdUnivMittTipoIdUnivoco);
		mygovRevoca.setCodRrIstitAttIdUnivMittCodiceIdUnivoco(codRrIstitAttIdUnivMittCodiceIdUnivoco); 
		mygovRevoca.setCodRrSoggVersIdUnivVersTipoIdUnivoco(codRrSoggVersIdUnivVersTipoIdUnivoco);
		mygovRevoca.setCodRrSoggVersIdUnivVersCodiceIdUnivoco(codRrSoggVersIdUnivVersCodiceIdUnivoco); 
		mygovRevoca.setCodRrSoggVersAnagraficaVersante(codRrSoggVersAnagraficaVersante);
		mygovRevoca.setDeRrSoggVersIndirizzoVersante(deRrSoggVersIndirizzoVersante); 
		mygovRevoca.setDeRrSoggVersCivicoVersante(deRrSoggVersCivicoVersante); 
		mygovRevoca.setCodRrSoggVersCapVersante(codRrSoggVersCapVersante);
		mygovRevoca.setDeRrSoggVersLocalitaVersante(deRrSoggVersLocalitaVersante); 
		mygovRevoca.setDeRrSoggVersProvinciaVersante(deRrSoggVersProvinciaVersante);
		mygovRevoca.setCodRrSoggVersNazioneVersante(codRrSoggVersNazioneVersante); 
		mygovRevoca.setDeRrSoggVersEmailVersante(deRrSoggVersEmailVersante);
		mygovRevoca.setCodRrSoggPagIdUnivPagTipoIdUnivoco(codRrSoggPagIdUnivPagTipoIdUnivoco); 
		mygovRevoca.setCodRrSoggPagIdUnivPagCodiceIdUnivoco(codRrSoggPagIdUnivPagCodiceIdUnivoco);
		mygovRevoca.setCodRrSoggPagAnagraficaPagatore(codRrSoggPagAnagraficaPagatore); 
		mygovRevoca.setDeRrSoggPagIndirizzoPagatore(deRrSoggPagIndirizzoPagatore);
		mygovRevoca.setDeRrSoggPagCivicoPagatore(deRrSoggPagCivicoPagatore);
		mygovRevoca.setCodRrSoggPagCapPagatore(codRrSoggPagCapPagatore); 
		mygovRevoca.setDeRrSoggPagLocalitaPagatore(deRrSoggPagLocalitaPagatore);
		mygovRevoca.setDeRrSoggPagProvinciaPagatore(deRrSoggPagProvinciaPagatore); 
		mygovRevoca.setCodRrSoggPagNazionePagatore(codRrSoggPagNazionePagatore); 
		mygovRevoca.setDeRrSoggPagEmailPagatore(deRrSoggPagEmailPagatore);
		mygovRevoca.setNumRrDatiRevImportoTotaleRevocato(numRrDatiRevImportoTotaleRevocato); 
		mygovRevoca.setCodRrDatiRevIdUnivocoVersamento(codRrDatiRevIdUnivocoVersamento);
		mygovRevoca.setCodRrDatiRevCodiceContestoPagamento(codRrDatiRevCodiceContestoPagamento);
		mygovRevoca.setCodRrDatiRevTipoRevoca(codRrDatiRevTipoRevoca);
		
		getHibernateTemplate().save(mygovRevoca);
		
		return mygovRevoca;
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao#updateRispostaRRById(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovRrEr updateRispostaRRById(Long mygovRrId, String esito, String faultCode, String faultString,
			String faultId, String description, Integer faultSerial, String originalDescription,
			String originalFaultCode, String originalFaultString) throws DataAccessException {
		
		log.debug("Invocato metodo updateRispostaRRById PARAMETRI ::: mygovRrId = [" + mygovRrId + "] ::: esito = [" + esito
				+ "] ::: faultCode = [" + faultCode + "] ::: faultString = [" + faultString
				+ "] ::: faultId = [" + faultId + "] ::: description = [" + description
				+ "] ::: faultSerial = [" + faultSerial + "] ::: originalDescription = [" + originalDescription
				+ "] ::: originalFaultCode = [" + originalFaultCode + "] ::: originalFaultString = [" + originalFaultString +"]"
				);
		
		MygovRrEr mygovRevoca = doGet(mygovRrId);

		mygovRevoca.setDtUltimaModifica(new Date());
		mygovRevoca.setCodRrPaEsito(esito);
		mygovRevoca.setCodRrPaFaultCode(faultCode);
		mygovRevoca.setCodRrPaFaultString(faultString);
		mygovRevoca.setCodRrPaFaultId(faultId);
		// Tronco description se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(description)) {
			if (description.length() > 1024) {
				mygovRevoca.setCodRrPaFaultDescription(description.substring(0, 1024));
			}
			else {
				mygovRevoca.setCodRrPaFaultDescription(description);
			}
		}
		else {
			mygovRevoca.setCodRrPaFaultDescription(description);
		}
		mygovRevoca.setCodRrPaFaultSerial(faultSerial);
		mygovRevoca.setCodRrPaOriginalFaultCode(originalFaultCode);
		mygovRevoca.setCodRrPaOriginalFaultString(originalFaultString);
		// Tronco originalDescription se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(originalDescription)) {
			if (originalDescription.length() > 1024) {
				mygovRevoca.setCodRrPaOriginalFaultDescription(originalDescription.substring(0, 1024));
			}
			else {
				mygovRevoca.setCodRrPaOriginalFaultDescription(originalDescription);
			}
		}
		else {
			mygovRevoca.setCodRrPaOriginalFaultDescription(originalDescription);
		}
		
		getHibernateTemplate().update(mygovRevoca);
		
		return mygovRevoca;
	}
	
	/**
	 * 
	 * @param mygovRrId
	 * @return
	 * @throws DataAccessException
	 */
	public MygovRrEr doGet(final long mygovRrId) throws DataAccessException {

		log.debug("Invocato metodo doGet PARAMETRI ::: mygovRrId = [" + mygovRrId + "]");

		return getHibernateTemplate().get(MygovRrEr.class, mygovRrId);
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao#getRrByRifMessaggioRevoca(java.lang.String, java.util.Date)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MygovRrEr checkRrByRifMessaggioRevocaDay(String idRiferimentoMessaggioRevoca, Date dtMsgRrStart, Date dtMsgRrFine) 
			throws DataAccessException{

		log.debug("Invocato metodo getRrByRifMessaggioRevoca ::: idRiferimentoMessaggioRevoca = [" + idRiferimentoMessaggioRevoca + "] , "
				+ "dtMsgRrStart = [" + dtMsgRrStart + "] , dtMsgRrFine = [" + dtMsgRrFine + "]");
		
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRrEr.class);
		criteria.add(Restrictions.eq("codRrIdMessaggioRevoca", idRiferimentoMessaggioRevoca));
		criteria.add(Restrictions.and(Restrictions.ge("dtRrDataOraMessaggioRevoca", dtMsgRrStart), Restrictions.lt("dtRrDataOraMessaggioRevoca", dtMsgRrFine)));

		List<MygovRrEr> results = getHibernateTemplate().findByCriteria(criteria);
		
		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException(
					"'MygovRevoca' is not unique for codRrIdMessaggioRevoca [" + idRiferimentoMessaggioRevoca + "] , dtMsgRrStart = [" + dtMsgRrStart + "] , dtMsgRrFine = [" + dtMsgRrFine + "]", 1, results.size());

		return results.size() == 0 ? null : (MygovRrEr) results.get(0);
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao#updateERById(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovRrEr updateERById(Long idRevoca, String identificativoDominio, String identificativoUnivocoVersamento,
			String codiceContestoPagamento, String deErVersioneOggetto, String codErDomIdDominio,
			String codErDomIdStazioneRichiedente, String codErIdMessaggioEsito, Date dtErDataOraMessaggioEsito,
			String codErRiferimentoMessaggioRevoca, Date dtErRiferimentoDataRevoca,
			String deErIstitAttDenominazioneMittente, String codErIstitAttUnitOperMittente,
			String deErIstitAttDenomUnitOperMittente, String deErIstitAttIndirizzoMittente,
			String deErIstitAttCivicoMittente, String codErIstitAttCapMittente, String deErIstitAttLocalitaMittente,
			String deErIstitAttProvinciaMittente, String codErIstitAttNazioneMittente,
			String codErIstitAttIdUnivMittTipoIdUnivoco, String codErIstitAttIdUnivMittCodiceIdUnivoco,
			String codErSoggVersIdUnivVersTipoIdUnivoco, String codErSoggVersIdUnivVersCodiceIdUnivoco,
			String codErSoggVersAnagraficaVersante, String deErSoggVersIndirizzoVersante,
			String deErSoggVersCivicoVersante, String codErSoggVersCapVersante, String deErSoggVersLocalitaVersante,
			String deErSoggVersProvinciaVersante, String codErSoggVersNazioneVersante, String deErSoggVersEmailVersante,
			String codErSoggPagIdUnivPagTipoIdUnivoco, String codErSoggPagIdUnivPagCodiceIdUnivoco,
			String codErSoggPagAnagraficaPagatore, String deErSoggPagIndirizzoPagatore,
			String deErSoggPagCivicoPagatore, String codErSoggPagCapPagatore, String deErSoggPagLocalitaPagatore,
			String deErSoggPagProvinciaPagatore, String codErSoggPagNazionePagatore, String deErSoggPagEmailPagatore,
			BigDecimal numErDatiRevImportoTotaleRevocato, String codErDatiRevIdUnivocoVersamento,
			String codErDatiRevCodiceContestoPagamento) throws DataAccessException{

		log.debug("Invocato metodo updateEById PARAMETRI ::: idRevoca = [" + idRevoca + "] ::: identificativoDominio = [" + identificativoDominio + "] "
				+ "::: identificativoUnivocoVersamento = [" + identificativoUnivocoVersamento + "] ::: codiceContestoPagamento = [" + codiceContestoPagamento + "] " 
				+ "::: deErVersioneOggetto = [" + deErVersioneOggetto + "] ::: codErDomIdDominio = [" + codErDomIdDominio + "] ::: codErDomIdStazioneRichiedente = [" + codErDomIdStazioneRichiedente + "] "
				+ "::: codErIdMessaggioEsito = [" + codErIdMessaggioEsito + "] ::: codErRiferimentoMessaggioRevoca = [" + codErRiferimentoMessaggioRevoca + "] " 
				+ "::: dtErRiferimentoDataRevoca = [" + dtErRiferimentoDataRevoca + "] ::: deErIstitAttDenominazioneMittente = [" + deErIstitAttDenominazioneMittente + "] " 
				+ "::: codErIstitAttUnitOperMittente = [" + codErIstitAttUnitOperMittente + "] ::: deErIstitAttDenomUnitOperMittente = [" + deErIstitAttDenomUnitOperMittente + "] " 
				+ "::: deErIstitAttIndirizzoMittente = [" + deErIstitAttIndirizzoMittente + "] ::: deErIstitAttCivicoMittente = [" + deErIstitAttCivicoMittente + "] " 
				+ "::: codErIstitAttCapMittente = [" + codErIstitAttCapMittente + "] ::: deErIstitAttLocalitaMittente = [" + deErIstitAttLocalitaMittente + "] " 
				+ "::: deErIstitAttProvinciaMittente = [" + deErIstitAttProvinciaMittente + "] ::: codErIstitAttNazioneMittente = [" + codErIstitAttNazioneMittente + "] "
				+ "::: codErIstitAttIdUnivMittTipoIdUnivoco = [" + codErIstitAttIdUnivMittTipoIdUnivoco + "] ::: codErIstitAttIdUnivMittCodiceIdUnivoco = [" + codErIstitAttIdUnivMittCodiceIdUnivoco + "] "
				+ "::: codErSoggVersIdUnivVersTipoIdUnivoco = [" + codErSoggVersIdUnivVersTipoIdUnivoco + "] ::: codErSoggVersIdUnivVersCodiceIdUnivoco = [" + codErSoggVersIdUnivVersCodiceIdUnivoco + "] "
				+ "::: codErSoggVersAnagraficaVersante = [" + codErSoggVersAnagraficaVersante + "] ::: deErSoggVersIndirizzoVersante = [" + deErSoggVersIndirizzoVersante + "] "
				+ "::: deErSoggVersCivicoVersante = [" + deErSoggVersCivicoVersante + "] ::: codErSoggVersCapVersante = [" + codErSoggVersCapVersante + "] "
				+ "::: deErSoggVersLocalitaVersante = [" + deErSoggVersLocalitaVersante + "] ::: deErSoggVersProvinciaVersante = [" + deErSoggVersProvinciaVersante + "] "
				+ "::: codErSoggVersNazioneVersante = [" + codErSoggVersNazioneVersante + "] ::: deErSoggVersEmailVersante = [" + deErSoggVersEmailVersante + "] "
				+ "::: codErSoggPagIdUnivPagTipoIdUnivoco = [" + codErSoggPagIdUnivPagTipoIdUnivoco + "] ::: codErSoggPagIdUnivPagCodiceIdUnivoco = [" + codErSoggPagIdUnivPagCodiceIdUnivoco + "] "
				+ "::: codErSoggPagAnagraficaPagatore = [" + codErSoggPagAnagraficaPagatore + "] ::: deErSoggPagIndirizzoPagatore = [" + deErSoggPagIndirizzoPagatore + "] "
				+ "::: deErSoggPagCivicoPagatore = [" + deErSoggPagCivicoPagatore + "] ::: codErSoggPagCapPagatore = [" + codErSoggPagCapPagatore + "] "
				+ "::: deErSoggPagLocalitaPagatore = [" + deErSoggPagLocalitaPagatore + "] ::: deErSoggPagProvinciaPagatore = [" + deErSoggPagProvinciaPagatore + "] "
				+ "::: codErSoggPagNazionePagatore = [" + codErSoggPagNazionePagatore + "] ::: deErSoggPagEmailPagatore = [" + deErSoggPagEmailPagatore + "] "
				+ "::: numErDatiRevImportoTotaleRevocato = [" + numErDatiRevImportoTotaleRevocato + "] ::: codErDatiRevIdUnivocoVersamento = [" + codErDatiRevIdUnivocoVersamento + "] "
				+ "::: codErDatiRevCodiceContestoPagamento = [" + codErDatiRevCodiceContestoPagamento + "] ");
				
		MygovRrEr mygovRevoca = doGet(idRevoca);
		
		//Controllo valori NOT NULL
		if (StringUtils.isBlank(deErVersioneOggetto)) {
			log.error("Errore nell'inserimento esito: 'deErVersioneOggetto' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'deErVersioneOggetto' obbligatorio");
		}
		
		if (StringUtils.isBlank(codErDomIdDominio)) {
			log.error("Errore nell'inserimento esito: 'codErDomIdDominio' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErDomIdDominio' obbligatorio");
		}
		
		if (StringUtils.isBlank(codErIdMessaggioEsito)) {
			log.error("Errore nell'inserimento esito: 'codErIdMessaggioEsito' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErIdMessaggioEsito' obbligatorio");
		}
		
		if (null == dtErDataOraMessaggioEsito) {
			log.error("Errore nell'inserimento esito: 'dtErDataOraMessaggioEsito' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'dtErDataOraMessaggioEsito' obbligatorio");
		}

		if (StringUtils.isBlank(codErRiferimentoMessaggioRevoca)) {
			log.error("Errore nell'inserimento esito: 'codErRiferimentoMessaggioRevoca' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErRiferimentoMessaggioRevoca' obbligatorio");
		}

		if (null == dtErRiferimentoDataRevoca) {
			log.error("Errore nell'inserimento esito: 'dtErRiferimentoDataRevoca' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'dtErRiferimentoDataRevoca' obbligatorio");
		}

		if (StringUtils.isBlank(deErIstitAttDenominazioneMittente)) {
			log.error("Errore nell'inserimento esito: 'deErIstitAttDenominazioneMittente' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'deErIstitAttDenominazioneMittente' obbligatorio");
		}

		if (StringUtils.isBlank(codErIstitAttIdUnivMittTipoIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codErIstitAttIdUnivMittTipoIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErIstitAttIdUnivMittTipoIdUnivoco' obbligatorio");
		}

		if (StringUtils.isBlank(codErIstitAttIdUnivMittCodiceIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codErIstitAttIdUnivMittCodiceIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErIstitAttIdUnivMittCodiceIdUnivoco' obbligatorio");
		}

		if (StringUtils.isBlank(codErSoggPagIdUnivPagTipoIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codErSoggPagIdUnivPagTipoIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErSoggPagIdUnivPagTipoIdUnivoco' obbligatorio");
		}

		if (StringUtils.isBlank(codErSoggPagIdUnivPagCodiceIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codErSoggPagIdUnivPagCodiceIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErSoggPagIdUnivPagCodiceIdUnivoco' obbligatorio");
		}

		if (StringUtils.isBlank(codErSoggPagAnagraficaPagatore)) {
			log.error("Errore nell'inserimento esito: 'codErSoggPagAnagraficaPagatore' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErSoggPagAnagraficaPagatore' obbligatorio");
		}

		if (null == numErDatiRevImportoTotaleRevocato) {
			log.error("Errore nell'inserimento esito: 'numErDatiRevImportoTotaleRevocato' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'numErDatiRevImportoTotaleRevocato' obbligatorio");
		}

		if (StringUtils.isBlank(codErDatiRevIdUnivocoVersamento)) {
			log.error("Errore nell'inserimento esito: 'codErDatiRevIdUnivocoVersamento' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErDatiRevIdUnivocoVersamento' obbligatorio");
		}

		if (StringUtils.isEmpty(codErDatiRevCodiceContestoPagamento) || StringUtils.isBlank(codErDatiRevCodiceContestoPagamento)) {
			log.error("Errore nell'inserimento esito: 'codErDatiRevCodiceContestoPagamento' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codErDatiRevCodiceContestoPagamento' obbligatorio");
		}

		mygovRevoca.setDtUltimaModifica(new Date());
		
		mygovRevoca.setDeErVersioneOggetto(deErVersioneOggetto);
		mygovRevoca.setCodErDomIdDominio(codErDomIdDominio);
		mygovRevoca.setCodErDomIdStazioneRichiedente(codErDomIdStazioneRichiedente);
		mygovRevoca.setCodErIdMessaggioEsito(codErIdMessaggioEsito);
		mygovRevoca.setDtErDataOraMessaggioEsito(dtErDataOraMessaggioEsito);
		mygovRevoca.setCodErRiferimentoMessaggioRevoca(codErRiferimentoMessaggioRevoca);
		mygovRevoca.setDtErRiferimentoDataRevoca(dtErRiferimentoDataRevoca);
		mygovRevoca.setDeErIstitAttDenominazioneMittente(deErIstitAttDenominazioneMittente);
		mygovRevoca.setCodErIstitAttUnitOperMittente(codErIstitAttUnitOperMittente);
		mygovRevoca.setDeErIstitAttDenomUnitOperMittente(deErIstitAttDenomUnitOperMittente);
		mygovRevoca.setDeErIstitAttIndirizzoMittente(deErIstitAttIndirizzoMittente);
		mygovRevoca.setDeErIstitAttCivicoMittente(deErIstitAttCivicoMittente);
		mygovRevoca.setCodErIstitAttCapMittente(codErIstitAttCapMittente);
		mygovRevoca.setDeErIstitAttLocalitaMittente(deErIstitAttLocalitaMittente);
		mygovRevoca.setDeErIstitAttProvinciaMittente(deErIstitAttProvinciaMittente);
		mygovRevoca.setCodErIstitAttNazioneMittente(codErIstitAttNazioneMittente);
		mygovRevoca.setCodErIstitAttIdUnivMittTipoIdUnivoco(codErIstitAttIdUnivMittTipoIdUnivoco);
		mygovRevoca.setCodErIstitAttIdUnivMittCodiceIdUnivoco(codErIstitAttIdUnivMittCodiceIdUnivoco);
		mygovRevoca.setCodErSoggVersIdUnivVersTipoIdUnivoco(codErSoggVersIdUnivVersTipoIdUnivoco);
		mygovRevoca.setCodErSoggVersIdUnivVersCodiceIdUnivoco(codErSoggVersIdUnivVersCodiceIdUnivoco);
		mygovRevoca.setCodErSoggVersAnagraficaVersante(codErSoggVersAnagraficaVersante);
		mygovRevoca.setDeErSoggVersIndirizzoVersante(deErSoggVersIndirizzoVersante);
		mygovRevoca.setDeErSoggVersCivicoVersante(deErSoggVersCivicoVersante);
		mygovRevoca.setCodErSoggVersCapVersante(codErSoggVersCapVersante);
		mygovRevoca.setDeErSoggVersLocalitaVersante(deErSoggVersLocalitaVersante);
		mygovRevoca.setDeErSoggVersProvinciaVersante(deErSoggVersProvinciaVersante);
		mygovRevoca.setCodErSoggVersNazioneVersante(codErSoggVersNazioneVersante);
		mygovRevoca.setDeErSoggVersEmailVersante(deErSoggVersEmailVersante);
		mygovRevoca.setCodErSoggPagIdUnivPagTipoIdUnivoco(codErSoggPagIdUnivPagTipoIdUnivoco);
		mygovRevoca.setCodErSoggPagIdUnivPagCodiceIdUnivoco(codErSoggPagIdUnivPagCodiceIdUnivoco);
		mygovRevoca.setCodErSoggPagAnagraficaPagatore(codErSoggPagAnagraficaPagatore);
		mygovRevoca.setDeErSoggPagIndirizzoPagatore(deErSoggPagIndirizzoPagatore);
		mygovRevoca.setDeErSoggPagCivicoPagatore(deErSoggPagCivicoPagatore);
		mygovRevoca.setCodErSoggPagCapPagatore(codErSoggPagCapPagatore);
		mygovRevoca.setDeErSoggPagLocalitaPagatore(deErSoggPagLocalitaPagatore);
		mygovRevoca.setDeErSoggPagProvinciaPagatore(deErSoggPagProvinciaPagatore);
		mygovRevoca.setCodErSoggPagNazionePagatore(codErSoggPagNazionePagatore);
		mygovRevoca.setDeErSoggPagEmailPagatore(deErSoggPagEmailPagatore);
		mygovRevoca.setNumErDatiRevImportoTotaleRevocato(numErDatiRevImportoTotaleRevocato);
		mygovRevoca.setCodErDatiRevIdUnivocoVersamento(codErDatiRevIdUnivocoVersamento);
		mygovRevoca.setCodErDatiRevCodiceContestoPagamento(codErDatiRevCodiceContestoPagamento);
		
		getHibernateTemplate().update(mygovRevoca);
		
		return mygovRevoca;
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao#updateRispostaERById(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovRrEr updateRispostaERById(Long mygovErId, String esito, String faultCode, String faultString,
			String faultId, String description, Integer faultSerial, String originalDescription,
			String originalFaultCode, String originalFaultString) {
		
		log.debug("Invocato metodo updateRispostaERById PARAMETRI ::: mygovErId = [" + mygovErId + "] ::: esito = [" + esito
				+ "] ::: faultCode = [" + faultCode + "] ::: faultString = [" + faultString
				+ "] ::: faultId = [" + faultId + "] ::: description = [" + description
				+ "] ::: faultSerial = [" + faultSerial + "] ::: originalDescription = [" + originalDescription
				+ "] ::: originalFaultCode = [" + originalFaultCode + "] ::: originalFaultString = [" + originalFaultString +"]"
				);
		
		MygovRrEr mygovRevoca = doGet(mygovErId);

		mygovRevoca.setDtUltimaModifica(new Date());
		mygovRevoca.setCodErNodoEsito(esito);
		mygovRevoca.setCodErNodoFaultCode(faultCode);
		mygovRevoca.setCodErNodoFaultString(faultString);
		mygovRevoca.setCodErNodoFaultId(faultId);
		// Tronco description se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(description)) {
			if (description.length() > 1024) {
				mygovRevoca.setCodErNodoFaultDescription(description.substring(0, 1024));
			}
			else {
				mygovRevoca.setCodErNodoFaultDescription(description);
			}
		}

		mygovRevoca.setCodErNodoFaultSerial(faultSerial);
		mygovRevoca.setCodErNodoOriginalFaultCode(originalFaultCode);
		mygovRevoca.setCodErNodoOriginalFaultString(originalFaultString);
		// Tronco originalDescription se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(originalDescription)) {
			if (originalDescription.length() > 1024) {
				mygovRevoca.setCodErNodoOriginalFaultDescription(originalDescription.substring(0, 1024));
			}
			else {
				mygovRevoca.setCodErNodoOriginalFaultDescription(originalDescription);
			}
		}
		
		getHibernateTemplate().update(mygovRevoca);
		
		return mygovRevoca;
		
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao#getRrByRifMessaggioRevoca(java.lang.String, java.util.Date)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MygovRrEr getRrByRifMessaggioRevoca(String idRiferimentoMessaggioRevoca, Date dataRiferimentoMessaggioRevoca)
			throws DataAccessException {
		
        log.debug("Invocato metodo getRrByRifMessaggioRevoca ::: idRiferimentoMessaggioRevoca = [" + idRiferimentoMessaggioRevoca + "] , "
                + "dataRiferimentoMessaggioRevoca = [" + dataRiferimentoMessaggioRevoca + "]");
        
        DetachedCriteria criteria = DetachedCriteria.forClass(MygovRrEr.class);
        criteria.add(Restrictions.eq("codRrIdMessaggioRevoca", idRiferimentoMessaggioRevoca));
        criteria.add(Restrictions.eq("dtRrDataOraMessaggioRevoca", dataRiferimentoMessaggioRevoca));
        List<MygovRrEr> results = getHibernateTemplate().findByCriteria(criteria);
        
        if (results.size() > 1)
            throw new IncorrectResultSizeDataAccessException(
                    "'MygovRevoca' is not unique for codRrIdMessaggioRevoca [" + idRiferimentoMessaggioRevoca + "] , dtRrDataOraMessaggioRevoca = [" + dataRiferimentoMessaggioRevoca + "]", 1, results.size());

        return results.size() == 0 ? null : (MygovRrEr) results.get(0);

	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao#checkRrByDomIuvCont(java.lang.String, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MygovRrEr checkRrByDomIuvCont(String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento) {
		
		log.debug("Invocato metodo checkRrByDomIuv ::: identificativoDominio = [" + identificativoDominio + "] , "
				+ "identificativoUnivocoVersamento = [" + identificativoUnivocoVersamento + "] codiceContestoPagamento = [" + codiceContestoPagamento + "]");
		
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRrEr.class);
		criteria.add(Restrictions.eq("codRrDomIdDominio", identificativoDominio));
		criteria.add(Restrictions.eq("codIdUnivocoVersamento", identificativoUnivocoVersamento));
		criteria.add(Restrictions.eq("codCodiceContestoPagamento", codiceContestoPagamento));

		List<MygovRrEr> results = getHibernateTemplate().findByCriteria(criteria);
		
		return results.size() == 0 ? null : (MygovRrEr) results.get(0);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao#checkRrByDomIuvCont(java.lang.String, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MygovRrEr checkRevoca(String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento) {
		
		log.debug("Invocato metodo checkRevoca ::: identificativoDominio = [" + identificativoDominio + "] , " + "identificativoUnivocoVersamento = [" + identificativoUnivocoVersamento 
				+ "] codiceContestoPagamento = [" + codiceContestoPagamento + "]");
		
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRrEr.class);
		
		Criterion importoERpositivo = Restrictions.gt("numErDatiRevImportoTotaleRevocato", BigDecimal.ZERO);
		Criterion importoRRpositivo = Restrictions.gt("numRrDatiRevImportoTotaleRevocato", BigDecimal.ZERO);
		LogicalExpression importoRRpositivoANDimportoERpositivo = Restrictions.and(importoRRpositivo, importoERpositivo);
		Criterion importoRRZero = Restrictions.eq("numRrDatiRevImportoTotaleRevocato", BigDecimal.ZERO);
		Criterion importoERZero = Restrictions.eq("numErDatiRevImportoTotaleRevocato", BigDecimal.ZERO);
		LogicalExpression importoRRZeroANDimportoERZero = Restrictions.and(importoRRZero, importoERZero);
		criteria.add(Restrictions.or(importoRRpositivoANDimportoERpositivo,importoRRZeroANDimportoERZero));
		criteria.add(Restrictions.eq("codRrDomIdDominio", identificativoDominio));
		criteria.add(Restrictions.eq("codIdUnivocoVersamento", identificativoUnivocoVersamento));
		criteria.add(Restrictions.eq("codCodiceContestoPagamento", codiceContestoPagamento));
		criteria.add(Restrictions.eq("codRrDatiRevTipoRevoca", new String ("1")));
		List<MygovRrEr> results = getHibernateTemplate().findByCriteria(criteria);
		
		return results.size() == 0 ? null : (MygovRrEr) results.get(0);
		
	}

	

}
