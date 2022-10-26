/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.AttivaRptDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RptRtDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovAttivaRptE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.AttivaRPTService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author regione del veneto
 * 
 */
public class AttivaRPTServiceImpl implements AttivaRPTService {

	private static final Log log = LogFactory.getLog(AttivaRPTServiceImpl.class);

	private AttivaRptDao attivaRptDao;
	
	private RptRtDao rptRtDao;
	
	public AttivaRPTServiceImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.regioneveneto.mygov.payment.nodoregionalefesp.service.AttivaRPTService
	 * #getByKey(java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovAttivaRptE getByKey(final String codAttivarptIdentificativoDominio, 
			final String codAttivarptIdentificativoUnivocoVersamento, final String codAttivarptCodiceContestoPagamento) {

		return attivaRptDao.getByKey(codAttivarptIdentificativoDominio, codAttivarptIdentificativoUnivocoVersamento, codAttivarptCodiceContestoPagamento);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.regioneveneto.mygov.payment.nodoregionalefesp.service.AttivaRPTService
	 * #insert(java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.util.Date,
	 * java.math.BigDecimal, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public MygovAttivaRptE insert(final String codAttivarptIdPsp,
			final String codAttivarptIdIntermediarioPsp,
			final String  codAttivarptIdCanalePsp,
			final String codAttivarptIdentificativoIntermediarioPa,
			final String codAttivarptIdentificativoStazioneIntermediarioPa,
			final String codAttivarptIdentificativoDominio,
			final String codAttivarptIdentificativoUnivocoVersamento,
			final String codAttivarptCodiceContestoPagamento,
			final Date dtAttivarpt,
			final BigDecimal numAttivarptImportoSingoloVersamento,
			final String deAttivarptIbanAppoggio,
			final String deAttivarptBicAppoggio,
			final String codAttivarptSoggVersIdUnivVersTipoIdUnivoco,
			final String codAttivarptSoggVersIdUnivVersCodiceIdUnivoco,
			final String deAttivarptSoggVersAnagraficaVersante,
			final String deAttivarptSoggVersIndirizzoVersante,
			final String deAttivarptSoggVersCivicoVersante,
			final String codAttivarptSoggVersCapVersante,
			final String deAttivarptSoggVersLocalitaVersante,
			final String deAttivarptSoggVersProvinciaVersante,
			final String codAttivarptSoggVersNazioneVersante,
			final String deAttivarptSoggVersEmailVersante,
			final String deAttivarptIbanAddebito,
			final String deAttivarptBicAddebito,
			final String codAttivarptSoggPagIdUnivPagTipoIdUnivoco,
			final String codAttivarptSoggPagIdUnivPagCodiceIdUnivoco,
			final String deAttivarptSoggPagAnagraficaPagatore,
			final String deAttivarptSoggPagIndirizzoPagatore,
			final String deAttivarptSoggPagCivicoPagatore,
			final String codAttivarptSoggPagCapPagatore,
			final String deAttivarptSoggPagLocalitaPagatore,
			final String deAttivarptSoggPagProvinciaPagatore,
			final String codAttivarptSoggPagNazionePagatore,
			final String deAttivarptSoggPagEmailPagatore) {

		return attivaRptDao.insert(codAttivarptIdPsp,
				codAttivarptIdIntermediarioPsp,
				codAttivarptIdCanalePsp,
				codAttivarptIdentificativoIntermediarioPa,
				codAttivarptIdentificativoStazioneIntermediarioPa,
				codAttivarptIdentificativoDominio,
				codAttivarptIdentificativoUnivocoVersamento,
				codAttivarptCodiceContestoPagamento, dtAttivarpt,
				numAttivarptImportoSingoloVersamento, deAttivarptIbanAppoggio,
				deAttivarptBicAppoggio,
				codAttivarptSoggVersIdUnivVersTipoIdUnivoco,
				codAttivarptSoggVersIdUnivVersCodiceIdUnivoco,
				deAttivarptSoggVersAnagraficaVersante,
				deAttivarptSoggVersIndirizzoVersante,
				deAttivarptSoggVersCivicoVersante,
				codAttivarptSoggVersCapVersante,
				deAttivarptSoggVersLocalitaVersante,
				deAttivarptSoggVersProvinciaVersante,
				codAttivarptSoggVersNazioneVersante,
				deAttivarptSoggVersEmailVersante, deAttivarptIbanAddebito,
				deAttivarptBicAddebito,
				codAttivarptSoggPagIdUnivPagTipoIdUnivoco,
				codAttivarptSoggPagIdUnivPagCodiceIdUnivoco,
				deAttivarptSoggPagAnagraficaPagatore,
				deAttivarptSoggPagIndirizzoPagatore,
				deAttivarptSoggPagCivicoPagatore,
				codAttivarptSoggPagCapPagatore,
				deAttivarptSoggPagLocalitaPagatore,
				deAttivarptSoggPagProvinciaPagatore,
				codAttivarptSoggPagNazionePagatore,
				deAttivarptSoggPagEmailPagatore);

	}



	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.AttivaRPTService#updateByKey(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.math.BigDecimal, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public MygovAttivaRptE updateByKey(final long mygovAttivaRptEId,
			final Date dtEAttivarpt,
			final BigDecimal numEAttivarptImportoSingoloVersamento,
			final String deEAttivarptIbanAccredito,
			final String deEAttivarptBicAccredito,
			final String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco,
			final String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco,
			final String deEAttivarptEnteBenefDenominazioneBeneficiario,
			final String codEAttivarptEnteBenefCodiceUnitOperBeneficiario,
			final String deEAttivarptEnteBenefDenomUnitOperBeneficiario,
			final String deEAttivarptEnteBenefIndirizzoBeneficiario,
			final String deEAttivarptEnteBenefCivicoBeneficiario,
			final String codEAttivarptEnteBenefCapBeneficiario,
			final String deEAttivarptEnteBenefLocalitaBeneficiario,
			final String deEAttivarptEnteBenefProvinciaBeneficiario,
			final String codEAttivarptEnteBenefNazioneBeneficiario,
			final String deEAttivarptCredenzialiPagatore,
			final String deEAttivarptCausaleVersamento,
			final String deAttivarptEsito,
			final String codAttivarptFaultCode,
			final String deAttivarptFaultString,
			final String codAttivarptId,
			final String deAttivarptDescription,
			final Integer codAttivarptSerial,
			final String codAttivarptOriginalFaultCode, 
			final String deAttivarptOriginalFaultString, 
			final String deAttivarptOriginalFaultDescription) {

		return attivaRptDao.updateByKey(mygovAttivaRptEId, dtEAttivarpt,
				numEAttivarptImportoSingoloVersamento,
				deEAttivarptIbanAccredito, deEAttivarptBicAccredito,
				codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco,
				codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco,
				deEAttivarptEnteBenefDenominazioneBeneficiario,
				codEAttivarptEnteBenefCodiceUnitOperBeneficiario,
				deEAttivarptEnteBenefDenomUnitOperBeneficiario,
				deEAttivarptEnteBenefIndirizzoBeneficiario,
				deEAttivarptEnteBenefCivicoBeneficiario,
				codEAttivarptEnteBenefCapBeneficiario,
				deEAttivarptEnteBenefLocalitaBeneficiario,
				deEAttivarptEnteBenefProvinciaBeneficiario,
				codEAttivarptEnteBenefNazioneBeneficiario,
				deEAttivarptCredenzialiPagatore, deEAttivarptCausaleVersamento, deAttivarptEsito,
				codAttivarptFaultCode,
				deAttivarptFaultString,
				codAttivarptId,
				deAttivarptDescription,
				codAttivarptSerial,
				codAttivarptOriginalFaultCode,
				deAttivarptOriginalFaultString,
				deAttivarptOriginalFaultDescription);
	}

	
	@Override
	public List<MygovRptRt> elaboraRPTAttivate() {
		List<MygovRptRt> mygovRptRts = rptRtDao.findAllRptAttivate();
		
		log.info("Trovate ["+mygovRptRts.size()+"] RPT attivate ancora da inviare");
		
		return mygovRptRts;
	}

	public AttivaRptDao getAttivaRptDao() {
		return attivaRptDao;
	}

	public void setAttivaRptDao(AttivaRptDao attivaRptDao) {
		this.attivaRptDao = attivaRptDao;
	}
	
	public RptRtDao getRptRtDao() {
		return rptRtDao;
	}

	public void setRptRtDao(RptRtDao rptRtDao) {
		this.rptRtDao = rptRtDao;
	}


}
