package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.Assert;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RptRtDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RptRtDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRpt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;

/**
 * @author regione del veneto
 * @author regione del veneto
 *
 */
public class HibernateRptRtDao extends HibernateDaoSupport implements RptRtDao {

	private static final Log log = LogFactory.getLog(HibernateRptRtDao.class);

	private RptRtDettaglioDao rptRtDettaglioDao;

	public void setRptRtDettaglioDao(RptRtDettaglioDao rptRtDettaglioDao) {
		this.rptRtDettaglioDao = rptRtDettaglioDao;
	}

	@Override
	public MygovRptRt insertRptWithRefresh(final String deRptInviarptPassword, final String codRptInviarptIdPsp, final String codRptInviarptIdIntermediarioPsp,
			final String codRptInviarptIdCanale, final String deRptInviarptTipoFirma, final String codRptInviarptIdIntermediarioPa,
			final String codRptInviarptIdStazioneIntermediarioPa, final String codRptInviarptIdDominio, final String codRptInviarptIdUnivocoVersamento,
			final String codRptInviarptCodiceContestoPagamento, final String deRptVersioneOggetto, final String codRptDomIdDominio,
			final String codRptDomIdStazioneRichiedente, final String codRptIdMessaggioRichiesta, final Date dtRptDataOraMessaggioRichiesta,
			final String codRptAutenticazioneSoggetto, final String codRptSoggVersIdUnivVersTipoIdUnivoco, final String codRptSoggVersIdUnivVersCodiceIdUnivoco,
			final String deRptSoggVersAnagraficaVersante, final String deRptSoggVersIndirizzoVersante, final String deRptSoggVersCivicoVersante,
			final String codRptSoggVersCapVersante, final String deRptSoggVersLocalitaVersante, final String deRptSoggVersProvinciaVersante,
			final String codRptSoggVersNazioneVersante, final String deRptSoggVersEmailVersante, final String codRptSoggPagIdUnivPagTipoIdUnivoco,
			final String codRptSoggPagIdUnivPagCodiceIdUnivoco, final String deRptSoggPagAnagraficaPagatore, final String deRptSoggPagIndirizzoPagatore,
			final String deRptSoggPagCivicoPagatore, final String codRptSoggPagCapPagatore, final String deRptSoggPagLocalitaPagatore,
			final String deRptSoggPagProvinciaPagatore, final String codRptSoggPagNazionePagatore, final String deRptSoggPagEmailPagatore,
			final String codRptEnteBenefIdUnivBenefTipoIdUnivoco, final String codRptEnteBenefIdUnivBenefCodiceIdUnivoco,
			final String deRptEnteBenefDenominazioneBeneficiario, final String codRptEnteBenefCodiceUnitOperBeneficiario,
			final String deRptEnteBenefDenomUnitOperBeneficiario, final String deRptEnteBenefIndirizzoBeneficiario,
			final String deRptEnteBenefCivicoBeneficiario, final String codRptEnteBenefCapBeneficiario, final String deRptEnteBenefLocalitaBeneficiario,
			final String deRptEnteBenefProvinciaBeneficiario, final String codRptEnteBenefNazioneBeneficiario, final Date dtRptDatiVersDataEsecuzionePagamento,
			final BigDecimal numRptDatiVersImportoTotaleDaVersare, final String codRptDatiVersTipoVersamento, final String codRptDatiVersIdUnivocoVersamento,
			final String codRptDatiVersCodiceContestoPagamento, final String deRptDatiVersIbanAddebito, final String deRptDatiVersBicAddebito,
			final String codRptDatiVersFirmaRicevuta, Integer modelloPagamento, final Long mygovRptEId) throws DataAccessException {

		MygovRptRt mygovRptRt = doInsertRpt(deRptInviarptPassword, codRptInviarptIdPsp, codRptInviarptIdIntermediarioPsp, codRptInviarptIdCanale,
				deRptInviarptTipoFirma, codRptInviarptIdIntermediarioPa, codRptInviarptIdStazioneIntermediarioPa, codRptInviarptIdDominio,
				codRptInviarptIdUnivocoVersamento, codRptInviarptCodiceContestoPagamento, deRptVersioneOggetto, codRptDomIdDominio,
				codRptDomIdStazioneRichiedente, codRptIdMessaggioRichiesta, dtRptDataOraMessaggioRichiesta, codRptAutenticazioneSoggetto,
				codRptSoggVersIdUnivVersTipoIdUnivoco, codRptSoggVersIdUnivVersCodiceIdUnivoco, deRptSoggVersAnagraficaVersante, deRptSoggVersIndirizzoVersante,
				deRptSoggVersCivicoVersante, codRptSoggVersCapVersante, deRptSoggVersLocalitaVersante, deRptSoggVersProvinciaVersante,
				codRptSoggVersNazioneVersante, deRptSoggVersEmailVersante, codRptSoggPagIdUnivPagTipoIdUnivoco, codRptSoggPagIdUnivPagCodiceIdUnivoco,
				deRptSoggPagAnagraficaPagatore, deRptSoggPagIndirizzoPagatore, deRptSoggPagCivicoPagatore, codRptSoggPagCapPagatore,
				deRptSoggPagLocalitaPagatore, deRptSoggPagProvinciaPagatore, codRptSoggPagNazionePagatore, deRptSoggPagEmailPagatore,
				codRptEnteBenefIdUnivBenefTipoIdUnivoco, codRptEnteBenefIdUnivBenefCodiceIdUnivoco, deRptEnteBenefDenominazioneBeneficiario,
				codRptEnteBenefCodiceUnitOperBeneficiario, deRptEnteBenefDenomUnitOperBeneficiario, deRptEnteBenefIndirizzoBeneficiario,
				deRptEnteBenefCivicoBeneficiario, codRptEnteBenefCapBeneficiario, deRptEnteBenefLocalitaBeneficiario, deRptEnteBenefProvinciaBeneficiario,
				codRptEnteBenefNazioneBeneficiario, dtRptDatiVersDataEsecuzionePagamento, numRptDatiVersImportoTotaleDaVersare, codRptDatiVersTipoVersamento,
				codRptDatiVersIdUnivocoVersamento, codRptDatiVersCodiceContestoPagamento, deRptDatiVersIbanAddebito, deRptDatiVersBicAddebito,
				codRptDatiVersFirmaRicevuta, modelloPagamento, mygovRptEId);

		getHibernateTemplate().flush();
		getHibernateTemplate().refresh(mygovRptRt);

		return mygovRptRt;
	}

	@Override
	public MygovRptRt updateRtById(Long mygovRptRtId, String deRtInviartTipoFirma, String codRtInviartIdIntermediarioPa,
			String codRtInviartIdStazioneIntermediarioPa, String codRtInviartIdDominio, String codRtInviartIdUnivocoVersamento,
			String codRtInviartCodiceContestoPagamento, String deRtVersioneOggetto, String codRtDomIdDominio, String codRtDomIdStazioneRichiedente,
			String codRtIdMessaggioRicevuta, Date dtRtDataOraMessaggioRicevuta, String codRtRiferimentoMessaggioRichiesta, Date dtRtRiferimentoDataRichiesta,
			String codRtIstitAttesIdUnivAttesTipoIdUnivoco, String codRtIstitAttesIdUnivAttesCodiceIdUnivoco, String deRtIstitAttesDenominazioneAttestante,
			String codRtIstitAttesCodiceUnitOperAttestante, String deRtIstitAttesDenomUnitOperAttestante, String deRtIstitAttesIndirizzoAttestante,
			String deRtIstitAttesCivicoAttestante, String codRtIstitAttesCapAttestante, String deRtIstitAttesLocalitaAttestante,
			String deRtIstitAttesProvinciaAttestante, String codRtIstitAttesNazioneAttestante, String codRtEnteBenefIdUnivBenefTipoIdUnivoco,
			String codRtEnteBenefIdUnivBenefCodiceIdUnivoco, String deRtEnteBenefDenominazioneBeneficiario, String codRtEnteBenefCodiceUnitOperBeneficiario,
			String deRtEnteBenefDenomUnitOperBeneficiario, String deRtEnteBenefIndirizzoBeneficiario, String deRtEnteBenefCivicoBeneficiario,
			String codRtEnteBenefCapBeneficiario, String deRtEnteBenefLocalitaBeneficiario, String deRtEnteBenefProvinciaBeneficiario,
			String codRtEnteBenefNazioneBeneficiario, String codRtSoggVersIdUnivVersTipoIdUnivoco, String codRtSoggVersIdUnivVersCodiceIdUnivoco,
			String deRtSoggVersAnagraficaVersante, String deRtSoggVersIndirizzoVersante, String deRtSoggVersCivicoVersante, String codRtSoggVersCapVersante,
			String deRtSoggVersLocalitaVersante, String deRtSoggVersProvinciaVersante, String codRtSoggVersNazioneVersante, String deRtSoggVersEmailVersante,
			String codRtSoggPagIdUnivPagTipoIdUnivoco, String codRtSoggPagIdUnivPagCodiceIdUnivoco, String deRtSoggPagAnagraficaPagatore,
			String deRtSoggPagIndirizzoPagatore, String deRtSoggPagCivicoPagatore, String codRtSoggPagCapPagatore, String deRtSoggPagLocalitaPagatore,
			String deRtSoggPagProvinciaPagatore, String codRtSoggPagNazionePagatore, String deRtSoggPagEmailPagatore, String codRtDatiPagCodiceEsitoPagamento,
			BigDecimal numRtDatiPagImportoTotalePagato, String codRtDatiPagIdUnivocoVersamento, String codRtDatiPagCodiceContestoPagamento, byte[] blbRtPayload)
			throws DataAccessException {

		MygovRptRt mygovRptRt = doGet(mygovRptRtId);
		if (mygovRptRt == null)
			throw new DataRetrievalFailureException("'id' is not valid");

		mygovRptRt.setDtCreazioneRt(new Date());
		mygovRptRt.setDtUltimaModificaRt(new Date());
		mygovRptRt.setDeRtInviartTipoFirma(deRtInviartTipoFirma);
		mygovRptRt.setCodRtInviartIdIntermediarioPa(codRtInviartIdIntermediarioPa);
		mygovRptRt.setCodRtInviartIdStazioneIntermediarioPa(codRtInviartIdStazioneIntermediarioPa);
		mygovRptRt.setCodRtInviartIdDominio(codRtInviartIdDominio);
		mygovRptRt.setCodRtInviartIdUnivocoVersamento(codRtInviartIdUnivocoVersamento);
		mygovRptRt.setCodRtInviartCodiceContestoPagamento(codRtInviartCodiceContestoPagamento);
		mygovRptRt.setDeRtVersioneOggetto(deRtVersioneOggetto);
		mygovRptRt.setCodRtDomIdDominio(codRtDomIdDominio);
		mygovRptRt.setCodRtDomIdStazioneRichiedente(codRtDomIdStazioneRichiedente);
		mygovRptRt.setCodRtIdMessaggioRicevuta(codRtIdMessaggioRicevuta);
		mygovRptRt.setDtRtDataOraMessaggioRicevuta(dtRtDataOraMessaggioRicevuta);
		mygovRptRt.setCodRtRiferimentoMessaggioRichiesta(codRtRiferimentoMessaggioRichiesta);
		mygovRptRt.setDtRtRiferimentoDataRichiesta(dtRtRiferimentoDataRichiesta);
		mygovRptRt.setCodRtIstitAttesIdUnivAttesTipoIdUnivoco(codRtIstitAttesIdUnivAttesTipoIdUnivoco);
		mygovRptRt.setCodRtIstitAttesIdUnivAttesCodiceIdUnivoco(codRtIstitAttesIdUnivAttesCodiceIdUnivoco);
		mygovRptRt.setDeRtIstitAttesDenominazioneAttestante(deRtIstitAttesDenominazioneAttestante);
		mygovRptRt.setCodRtIstitAttesCodiceUnitOperAttestante(codRtIstitAttesCodiceUnitOperAttestante);
		mygovRptRt.setDeRtIstitAttesDenomUnitOperAttestante(deRtIstitAttesDenomUnitOperAttestante);
		mygovRptRt.setDeRtIstitAttesIndirizzoAttestante(deRtIstitAttesIndirizzoAttestante);
		mygovRptRt.setDeRtIstitAttesCivicoAttestante(deRtIstitAttesCivicoAttestante);
		mygovRptRt.setCodRtIstitAttesCapAttestante(codRtIstitAttesCapAttestante);
		mygovRptRt.setDeRtIstitAttesLocalitaAttestante(deRtIstitAttesLocalitaAttestante);
		mygovRptRt.setDeRtIstitAttesProvinciaAttestante(deRtIstitAttesProvinciaAttestante);
		mygovRptRt.setCodRtIstitAttesNazioneAttestante(codRtIstitAttesNazioneAttestante);
		mygovRptRt.setCodRtEnteBenefIdUnivBenefTipoIdUnivoco(codRtEnteBenefIdUnivBenefTipoIdUnivoco);
		mygovRptRt.setCodRtEnteBenefIdUnivBenefCodiceIdUnivoco(codRtEnteBenefIdUnivBenefCodiceIdUnivoco);
		mygovRptRt.setDeRtEnteBenefDenominazioneBeneficiario(deRtEnteBenefDenominazioneBeneficiario);
		mygovRptRt.setCodRtEnteBenefCodiceUnitOperBeneficiario(codRtEnteBenefCodiceUnitOperBeneficiario);
		mygovRptRt.setDeRtEnteBenefDenomUnitOperBeneficiario(deRtEnteBenefDenomUnitOperBeneficiario);
		mygovRptRt.setDeRtEnteBenefIndirizzoBeneficiario(deRtEnteBenefIndirizzoBeneficiario);
		mygovRptRt.setDeRtEnteBenefCivicoBeneficiario(deRtEnteBenefCivicoBeneficiario);
		mygovRptRt.setCodRtEnteBenefCapBeneficiario(codRtEnteBenefCapBeneficiario);
		mygovRptRt.setDeRtEnteBenefLocalitaBeneficiario(deRtEnteBenefLocalitaBeneficiario);
		mygovRptRt.setDeRtEnteBenefProvinciaBeneficiario(deRtEnteBenefProvinciaBeneficiario);
		mygovRptRt.setCodRtEnteBenefNazioneBeneficiario(codRtEnteBenefNazioneBeneficiario);
		mygovRptRt.setCodRtSoggVersIdUnivVersTipoIdUnivoco(codRtSoggVersIdUnivVersTipoIdUnivoco);
		mygovRptRt.setCodRtSoggVersIdUnivVersCodiceIdUnivoco(codRtSoggVersIdUnivVersCodiceIdUnivoco);
		mygovRptRt.setDeRtSoggVersAnagraficaVersante(deRtSoggVersAnagraficaVersante);
		mygovRptRt.setDeRtSoggVersIndirizzoVersante(deRtSoggVersIndirizzoVersante);
		mygovRptRt.setDeRtSoggVersCivicoVersante(deRtSoggVersCivicoVersante);
		mygovRptRt.setCodRtSoggVersCapVersante(codRtSoggVersCapVersante);
		mygovRptRt.setDeRtSoggVersLocalitaVersante(deRtSoggVersLocalitaVersante);
		mygovRptRt.setDeRtSoggVersProvinciaVersante(deRtSoggVersProvinciaVersante);
		mygovRptRt.setCodRtSoggVersNazioneVersante(codRtSoggVersNazioneVersante);
		mygovRptRt.setDeRtSoggVersEmailVersante(deRtSoggVersEmailVersante);
		mygovRptRt.setCodRtSoggPagIdUnivPagTipoIdUnivoco(codRtSoggPagIdUnivPagTipoIdUnivoco);
		mygovRptRt.setCodRtSoggPagIdUnivPagCodiceIdUnivoco(codRtSoggPagIdUnivPagCodiceIdUnivoco);
		mygovRptRt.setDeRtSoggPagAnagraficaPagatore(deRtSoggPagAnagraficaPagatore);
		mygovRptRt.setDeRtSoggPagIndirizzoPagatore(deRtSoggPagIndirizzoPagatore);
		mygovRptRt.setDeRtSoggPagCivicoPagatore(deRtSoggPagCivicoPagatore);
		mygovRptRt.setCodRtSoggPagCapPagatore(codRtSoggPagCapPagatore);
		mygovRptRt.setDeRtSoggPagLocalitaPagatore(deRtSoggPagLocalitaPagatore);
		mygovRptRt.setDeRtSoggPagProvinciaPagatore(deRtSoggPagProvinciaPagatore);
		mygovRptRt.setCodRtSoggPagNazionePagatore(codRtSoggPagNazionePagatore);
		mygovRptRt.setDeRtSoggPagEmailPagatore(deRtSoggPagEmailPagatore);
		mygovRptRt.setCodRtDatiPagCodiceEsitoPagamento(codRtDatiPagCodiceEsitoPagamento);
		mygovRptRt.setNumRtDatiPagImportoTotalePagato(numRtDatiPagImportoTotalePagato);
		mygovRptRt.setCodRtDatiPagIdUnivocoVersamento(codRtDatiPagIdUnivocoVersamento);
		mygovRptRt.setCodRtDatiPagCodiceContestoPagamento(codRtDatiPagCodiceContestoPagamento);
		mygovRptRt.setBlbRtPayload(blbRtPayload);
		getHibernateTemplate().update(mygovRptRt);

		return mygovRptRt;
	}

	@Override
	public MygovRptRt updateRispostaRtById(Long mygovRptRtId, String codAckRt, String codRtInviartEsito, String codRtInviartFaultCode,
			String codRtInviartFaultString, String codRtInviartId, String deRtInviartDescription, Integer numRtInviartSerial,
			String codRtInviartOriginaltFaultCode, String deRtInviartOriginaltFaultString, String deRtInviartOriginaltFaultDescription) throws DataAccessException {

		MygovRptRt mygovRptRt = doGet(mygovRptRtId);

		mygovRptRt.setDtUltimaModificaRt(new Date());
		mygovRptRt.setCodAckRt(codAckRt);
		mygovRptRt.setCodRtInviartEsito(codRtInviartEsito);
		mygovRptRt.setCodRtInviartFaultCode(codRtInviartFaultCode);
		mygovRptRt.setCodRtInviartFaultString(codRtInviartFaultString);
		mygovRptRt.setCodRtInviartId(codRtInviartId);

		// Tronco deRtInviartDescription se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(deRtInviartDescription)) {
			if (deRtInviartDescription.length() > 1024) {
				mygovRptRt.setDeRtInviartDescription(deRtInviartDescription.substring(0, 1024));
			}
			else {
				mygovRptRt.setDeRtInviartDescription(deRtInviartDescription);
			}
		}
		else {
			mygovRptRt.setDeRtInviartDescription(deRtInviartDescription);
		}
		
		
		mygovRptRt.setCodRtInviartOriginaltFaultCode(codRtInviartOriginaltFaultCode);
		mygovRptRt.setDeRtInviartOriginaltFaultString(deRtInviartOriginaltFaultString);
		if (StringUtils.isNotBlank(deRtInviartOriginaltFaultDescription)) {
			if (deRtInviartOriginaltFaultDescription.length() > 1024) 
				mygovRptRt.setDeRtInviartOriginaltFaultDescription(deRtInviartOriginaltFaultDescription.substring(0, 1024));
			else 
				mygovRptRt.setDeRtInviartOriginaltFaultDescription(deRtInviartOriginaltFaultDescription);
		}

		mygovRptRt.setNumRtInviartSerial(numRtInviartSerial);

		getHibernateTemplate().update(mygovRptRt);

		return mygovRptRt;
	}

	@Override
	public MygovRptRt updateRispostaRptById(Long mygovRptRtId, String deRptInviarptEsito, Integer numRptInviarptRedirect, String codRptInviarptUrl,
			String codRptInviarptFaultCode, String codRptInviarptFaultString, String codRptInviarptId, String deRptInviarptDescription,
			Integer numRptInviarptSerial, String idSessionSPC, String codRptInviarptOriginaltFaultCode, String deRptInviarptOriginaltFaultString,
     		String deRptInviarptOriginaltFaultDescription) throws DataAccessException {

		MygovRptRt mygovRptRt = doGet(mygovRptRtId);

		mygovRptRt.setDtUltimaModificaRpt(new Date());
		mygovRptRt.setDeRptInviarptEsito(deRptInviarptEsito);
		mygovRptRt.setNumRptInviarptRedirect(numRptInviarptRedirect);
		mygovRptRt.setCodRptInviarptUrl(codRptInviarptUrl);
		mygovRptRt.setCodRptInviarptFaultCode(codRptInviarptFaultCode);
		mygovRptRt.setCodRptInviarptFaultString(codRptInviarptFaultString);
		mygovRptRt.setCodRptInviarptId(codRptInviarptId);

		// Tronco deRptInviarptDescription se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(deRptInviarptDescription)) {
			if (deRptInviarptDescription.length() > 1024) {
				mygovRptRt.setDeRptInviarptDescription(deRptInviarptDescription.substring(0, 1024));
			}
			else {
				mygovRptRt.setDeRptInviarptDescription(deRptInviarptDescription);
			}
		}
		else {
			mygovRptRt.setDeRptInviarptDescription(deRptInviarptDescription);
		}
		
		mygovRptRt.setCodRptInviarptOriginaltFaultCode(codRptInviarptOriginaltFaultCode);
		mygovRptRt.setDeRptInviarptOriginaltFaultString(deRptInviarptOriginaltFaultString);
		if (StringUtils.isNotBlank(deRptInviarptOriginaltFaultDescription)) {
			if (deRptInviarptOriginaltFaultDescription.length() > 1024) 
				mygovRptRt.setDeRptInviarptOriginaltFaultDescription(deRptInviarptOriginaltFaultDescription.substring(0, 1024));
			else 
				mygovRptRt.setDeRptInviarptOriginaltFaultDescription(deRptInviarptOriginaltFaultDescription);
		}

		mygovRptRt.setNumRptInviarptSerial(numRptInviarptSerial);
		mygovRptRt.setIdSession(idSessionSPC);

		log.debug("deRptInviarptEsito :" + deRptInviarptEsito);
		log.debug("numRptInviarptRedirect :" + numRptInviarptRedirect);
		log.debug("codRptInviarptUrl :" + codRptInviarptUrl);
		log.debug("codRptInviarptFaultCode :" + codRptInviarptFaultCode);
		log.debug("codRptInviarptFaultString :" + codRptInviarptFaultString);
		log.debug("codRptInviarptId :" + codRptInviarptId);
		log.debug("deRptInviarptDescription :" + deRptInviarptDescription);
		log.debug("numRptInviarptSerial :" + numRptInviarptSerial);
		log.debug("idSessionSPC :" + idSessionSPC);
		log.debug("codRptInviarptOriginaltFaultCode :" + codRptInviarptOriginaltFaultCode);
		log.debug("deRptInviarptOriginaltFaultString :" + deRptInviarptOriginaltFaultString);
		log.debug("deRptInviarptOriginaltFaultDescription :" + deRptInviarptOriginaltFaultDescription);

		getHibernateTemplate().update(mygovRptRt);

		return mygovRptRt;
	}
	


	@Override
	public List<MygovRptRt> updateRispostaRptByCarrello(MygovCarrelloRpt mygovCarrelloRpt, String deRptInviarptEsito, String codRptInviarptUrl,
			String codRptInviarptFaultCode, String codRptInviarptFaultString, String codRptInviarptId, String deRptInviarptDescription,
			Integer numRptInviarptSerial, String codRptInviarptOriginaltFaultCode, String deRptInviarptOriginaltFaultString,
     		String deRptInviarptOriginaltFaultDescription) throws DataAccessException {

		List<MygovRptRt> mygovRptRtList = doGetFromCarrello(mygovCarrelloRpt);
		
		for (MygovRptRt mygovRptRt : mygovRptRtList) {
			
			mygovRptRt.setDtUltimaModificaRpt(new Date());
			mygovRptRt.setDeRptInviarptEsito(deRptInviarptEsito);
			//mygovRptRt.setNumRptInviarptRedirect(numRptInviarptRedirect);
			mygovRptRt.setCodRptInviarptUrl(codRptInviarptUrl);
			mygovRptRt.setCodRptInviarptFaultCode(codRptInviarptFaultCode);
			mygovRptRt.setCodRptInviarptFaultString(codRptInviarptFaultString);
			mygovRptRt.setCodRptInviarptId(codRptInviarptId);

			// Tronco deRptInviarptDescription se non e null e la lunghezza e maggiore di 1024
			if (StringUtils.isNotBlank(deRptInviarptDescription)) {
				if (deRptInviarptDescription.length() > 1024) {
					mygovRptRt.setDeRptInviarptDescription(deRptInviarptDescription.substring(0, 1024));
				}
				else {
					mygovRptRt.setDeRptInviarptDescription(deRptInviarptDescription);
				}
			}
			else {
				mygovRptRt.setDeRptInviarptDescription(deRptInviarptDescription);
			}
			
			
			mygovRptRt.setCodRptInviarptOriginaltFaultCode(codRptInviarptOriginaltFaultCode);
			mygovRptRt.setDeRptInviarptOriginaltFaultString(deRptInviarptOriginaltFaultString);
			if (StringUtils.isNotBlank(deRptInviarptOriginaltFaultDescription)) {
				if (deRptInviarptOriginaltFaultDescription.length() > 1024) 
					mygovRptRt.setDeRptInviarptOriginaltFaultDescription(deRptInviarptOriginaltFaultDescription.substring(0, 1024));
				else 
					mygovRptRt.setDeRptInviarptOriginaltFaultDescription(deRptInviarptOriginaltFaultDescription);
			}

			mygovRptRt.setNumRptInviarptSerial(numRptInviarptSerial);

			log.debug("deRptInviarptEsito :" + deRptInviarptEsito);
			log.debug("codRptInviarptUrl :" + codRptInviarptUrl);
			log.debug("codRptInviarptFaultCode :" + codRptInviarptFaultCode);
			log.debug("codRptInviarptFaultString :" + codRptInviarptFaultString);
			log.debug("codRptInviarptId :" + codRptInviarptId);
			log.debug("deRptInviarptDescription :" + deRptInviarptDescription);
			log.debug("numRptInviarptSerial :" + numRptInviarptSerial);
			log.debug("codRptInviarptOriginaltFaultCode :" + codRptInviarptOriginaltFaultCode);
			log.debug("deRptInviarptOriginaltFaultString :" + deRptInviarptOriginaltFaultString);
			log.debug("deRptInviarptOriginaltFaultDescription :" + deRptInviarptOriginaltFaultDescription);

			getHibernateTemplate().update(mygovRptRt);
		}

		

		return mygovRptRtList;
	}

	private MygovRptRt doInsertRpt(final String deRptInviarptPassword, final String codRptInviarptIdPsp, final String codRptInviarptIdIntermediarioPsp,
			final String codRptInviarptIdCanale, final String deRptInviarptTipoFirma, final String codRptInviarptIdIntermediarioPa,
			final String codRptInviarptIdStazioneIntermediarioPa, final String codRptInviarptIdDominio, final String codRptInviarptIdUnivocoVersamento,
			final String codRptInviarptCodiceContestoPagamento, final String deRptVersioneOggetto, final String codRptDomIdDominio,
			final String codRptDomIdStazioneRichiedente, final String codRptIdMessaggioRichiesta, final Date dtRptDataOraMessaggioRichiesta,
			final String codRptAutenticazioneSoggetto, final String codRptSoggVersIdUnivVersTipoIdUnivoco, final String codRptSoggVersIdUnivVersCodiceIdUnivoco,
			final String deRptSoggVersAnagraficaVersante, final String deRptSoggVersIndirizzoVersante, final String deRptSoggVersCivicoVersante,
			final String codRptSoggVersCapVersante, final String deRptSoggVersLocalitaVersante, final String deRptSoggVersProvinciaVersante,
			final String codRptSoggVersNazioneVersante, final String deRptSoggVersEmailVersante, final String codRptSoggPagIdUnivPagTipoIdUnivoco,
			final String codRptSoggPagIdUnivPagCodiceIdUnivoco, final String deRptSoggPagAnagraficaPagatore, final String deRptSoggPagIndirizzoPagatore,
			final String deRptSoggPagCivicoPagatore, final String codRptSoggPagCapPagatore, final String deRptSoggPagLocalitaPagatore,
			final String deRptSoggPagProvinciaPagatore, final String codRptSoggPagNazionePagatore, final String deRptSoggPagEmailPagatore,
			final String codRptEnteBenefIdUnivBenefTipoIdUnivoco, final String codRptEnteBenefIdUnivBenefCodiceIdUnivoco,
			final String deRptEnteBenefDenominazioneBeneficiario, final String codRptEnteBenefCodiceUnitOperBeneficiario,
			final String deRptEnteBenefDenomUnitOperBeneficiario, final String deRptEnteBenefIndirizzoBeneficiario,
			final String deRptEnteBenefCivicoBeneficiario, final String codRptEnteBenefCapBeneficiario, final String deRptEnteBenefLocalitaBeneficiario,
			final String deRptEnteBenefProvinciaBeneficiario, final String codRptEnteBenefNazioneBeneficiario, final Date dtRptDatiVersDataEsecuzionePagamento,
			final BigDecimal numRptDatiVersImportoTotaleDaVersare, final String codRptDatiVersTipoVersamento, final String codRptDatiVersIdUnivocoVersamento,
			final String codRptDatiVersCodiceContestoPagamento, final String deRptDatiVersIbanAddebito, final String deRptDatiVersBicAddebito,
			final String codRptDatiVersFirmaRicevuta, Integer modelloPagamento, final Long mygovRptEId) throws DataAccessException {

		MygovRptRt mygovRptRt = new MygovRptRt(new Date(), new Date(), deRptVersioneOggetto, codRptDomIdDominio, codRptIdMessaggioRichiesta,
				dtRptDataOraMessaggioRichiesta, codRptAutenticazioneSoggetto, codRptSoggPagIdUnivPagTipoIdUnivoco, codRptSoggPagIdUnivPagCodiceIdUnivoco,
				deRptSoggPagAnagraficaPagatore, codRptEnteBenefIdUnivBenefTipoIdUnivoco, codRptEnteBenefIdUnivBenefCodiceIdUnivoco,
				deRptEnteBenefDenominazioneBeneficiario, dtRptDatiVersDataEsecuzionePagamento, numRptDatiVersImportoTotaleDaVersare,
				codRptDatiVersTipoVersamento, codRptDatiVersIdUnivocoVersamento, codRptDatiVersCodiceContestoPagamento, codRptDatiVersFirmaRicevuta,
				mygovRptEId);

		mygovRptRt.setDeRptDatiVersIbanAddebito(deRptDatiVersIbanAddebito);
		mygovRptRt.setDeRptDatiVersBicAddebito(deRptDatiVersBicAddebito);
		mygovRptRt.setCodRptEnteBenefCodiceUnitOperBeneficiario(codRptEnteBenefCodiceUnitOperBeneficiario);
		mygovRptRt.setDeRptEnteBenefDenomUnitOperBeneficiario(deRptEnteBenefDenomUnitOperBeneficiario);
		mygovRptRt.setDeRptEnteBenefIndirizzoBeneficiario(deRptEnteBenefIndirizzoBeneficiario);
		mygovRptRt.setDeRptEnteBenefCivicoBeneficiario(deRptEnteBenefCivicoBeneficiario);
		mygovRptRt.setCodRptEnteBenefCapBeneficiario(codRptEnteBenefCapBeneficiario);
		mygovRptRt.setDeRptEnteBenefLocalitaBeneficiario(deRptEnteBenefLocalitaBeneficiario);
		mygovRptRt.setDeRptEnteBenefProvinciaBeneficiario(deRptEnteBenefProvinciaBeneficiario);
		mygovRptRt.setCodRptEnteBenefNazioneBeneficiario(codRptEnteBenefNazioneBeneficiario);
		mygovRptRt.setDeRptSoggPagIndirizzoPagatore(deRptSoggPagIndirizzoPagatore);
		mygovRptRt.setDeRptSoggPagCivicoPagatore(deRptSoggPagCivicoPagatore);
		mygovRptRt.setCodRptSoggPagCapPagatore(codRptSoggPagCapPagatore);
		mygovRptRt.setDeRptSoggPagLocalitaPagatore(deRptSoggPagLocalitaPagatore);
		mygovRptRt.setDeRptSoggPagProvinciaPagatore(deRptSoggPagProvinciaPagatore);
		mygovRptRt.setCodRptSoggPagNazionePagatore(codRptSoggPagNazionePagatore);
		mygovRptRt.setDeRptSoggPagEmailPagatore(deRptSoggPagEmailPagatore);
		mygovRptRt.setCodRptSoggVersIdUnivVersTipoIdUnivoco(codRptSoggVersIdUnivVersTipoIdUnivoco);
		mygovRptRt.setCodRptSoggVersIdUnivVersCodiceIdUnivoco(codRptSoggVersIdUnivVersCodiceIdUnivoco);
		mygovRptRt.setDeRptSoggVersAnagraficaVersante(deRptSoggVersAnagraficaVersante);
		mygovRptRt.setDeRptSoggVersIndirizzoVersante(deRptSoggVersIndirizzoVersante);
		mygovRptRt.setDeRptSoggVersCivicoVersante(deRptSoggVersCivicoVersante);
		mygovRptRt.setCodRptSoggVersCapVersante(codRptSoggVersCapVersante);
		mygovRptRt.setDeRptSoggVersLocalitaVersante(deRptSoggVersLocalitaVersante);
		mygovRptRt.setDeRptSoggVersProvinciaVersante(deRptSoggVersProvinciaVersante);
		mygovRptRt.setCodRptSoggVersNazioneVersante(codRptSoggVersNazioneVersante);
		mygovRptRt.setDeRptSoggVersEmailVersante(deRptSoggVersEmailVersante);
		mygovRptRt.setDeRptInviarptPassword(deRptInviarptPassword);
		mygovRptRt.setCodRptInviarptIdPsp(codRptInviarptIdPsp);
		mygovRptRt.setCodRptInviarptIdIntermediarioPsp(codRptInviarptIdIntermediarioPsp);
		mygovRptRt.setCodRptInviarptIdCanale(codRptInviarptIdCanale);
		mygovRptRt.setDeRptInviarptTipoFirma(deRptInviarptTipoFirma);
		mygovRptRt.setCodRptInviarptIdIntermediarioPa(codRptInviarptIdIntermediarioPa);
		mygovRptRt.setCodRptInviarptIdStazioneIntermediarioPa(codRptInviarptIdStazioneIntermediarioPa);
		mygovRptRt.setCodRptInviarptIdDominio(codRptInviarptIdDominio);
		mygovRptRt.setCodRptInviarptIdUnivocoVersamento(codRptInviarptIdUnivocoVersamento);
		mygovRptRt.setCodRptInviarptCodiceContestoPagamento(codRptInviarptCodiceContestoPagamento);

		mygovRptRt.setModelloPagamento(modelloPagamento);

		getHibernateTemplate().save(mygovRptRt);
		return mygovRptRt;
	}

	@SuppressWarnings("unchecked")
	protected MygovRptRt doGet(final long mygovRptRtId) throws DataAccessException {
		List<MygovRptRt> results = getHibernateTemplate().find("from MygovRptRt where mygovRptRtId = ?", mygovRptRtId);
		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("'mygovRptRtId' is not unique", 1, results.size());

		return results.size() == 0 ? null : (MygovRptRt) results.get(0);
	}
	
	@SuppressWarnings("unchecked")
	protected List<MygovRptRt> doGetFromCarrello(final MygovCarrelloRpt mygovCarrelloRpt) throws DataAccessException {
		List<MygovRptRt> results = getHibernateTemplate().find("from MygovRptRt mygovRptRt where mygovRptRt.mygov_carrello_rpt = ?", mygovCarrelloRpt);
		if (results.size() == 0)
			throw new IncorrectResultSizeDataAccessException("non esistono rpt_rt associate al carrello", 1, results.size());

		return results;
	}

	@Override
	public MygovRptRt updateCodRtDatiPagCodiceEsitoPagamentoByIdSession(String idSession, String esito) throws DataAccessException {

		MygovRptRt mygovRptRt = getRptByIdSession(idSession);
		mygovRptRt.setCodRtDatiPagCodiceEsitoPagamento(esito);
		getHibernateTemplate().update(mygovRptRt);

		return mygovRptRt;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MygovRptRt getRptByMygovRpEId(final Long mygovRpEId) {

		//		List<MygovRptRt> results = getHibernateTemplate().find(
		//				"from MygovRptRt WHERE mygovRpEId = '" + mygovRpEId + "'");

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRptRt.class);
		criteria.add(Restrictions.eq("mygovRpEId", mygovRpEId));
		List<MygovRptRt> results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("'mygovRptRt' is not unique", 1, results.size());

		return results.size() == 0 ? null : (MygovRptRt) results.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MygovRptRt getRptByIdSession(String nodoSPCFespIdSession) {

		//		List<MygovRptRt> results = getHibernateTemplate().find(
		//				"from MygovRptRt WHERE idSession = '" + nodoSPCFespIdSession + "'");

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRptRt.class);
		criteria.add(Restrictions.eq("idSession", nodoSPCFespIdSession));
		List<MygovRptRt> results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("'mygovRptRt' is not unique", 1, results.size());

		return results.size() == 0 ? null : (MygovRptRt) results.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public MygovRptRt getRptByCodRptIdMessaggioRichiesta(final String riferimentoMessaggioRichiesta) {

		//		List<MygovRptRt> results = getHibernateTemplate().find(
		//				"from MygovRptRt WHERE codRptIdMessaggioRichiesta = '" + riferimentoMessaggioRichiesta + "'");

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRptRt.class);
		criteria.add(Restrictions.eq("codRptIdMessaggioRichiesta", riferimentoMessaggioRichiesta));
		List<MygovRptRt> results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException(
					"'mygovRptRt' is not unique for codRptIdMessaggioRichiesta [" + riferimentoMessaggioRichiesta + "]", 1, results.size());

		return results.size() == 0 ? null : (MygovRptRt) results.get(0);
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RptRtDao#updateStatoRtById(java.lang.Long, java.lang.String)
	 */
	@Override
	public MygovRptRt updateStatoRtById(Long mygovRptRtId, String codStato) {
		MygovRptRt mygovRptRt = doGet(mygovRptRtId);

		mygovRptRt.setDtUltimaModificaRt(new Date());
		getHibernateTemplate().update(mygovRptRt);
		return mygovRptRt;
	}

	@Override
	public MygovRptRt clearInviarptEsitoById(Long mygovRptRtId) {
		MygovRptRt mygovRptRt = doGet(mygovRptRtId);

		mygovRptRt.setDeRptInviarptEsito(null);
		mygovRptRt.setCodRptInviarptFaultCode(null);
		mygovRptRt.setCodRptInviarptFaultString(null);
		mygovRptRt.setCodRptInviarptId(null);
		mygovRptRt.setDeRptInviarptDescription(null);

		getHibernateTemplate().update(mygovRptRt);
		return mygovRptRt;
	}

	@SuppressWarnings("unchecked")
	public List<MygovRptRt> findAllRptPendenti(final int modelloPagamento, final int intervalMinute) throws DataAccessException {

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRptRt.class);

		//		switch (modelloPagamento) {
		//		case 0: //MODELLO 1 - IMMEDIATO
		//			Criterion codiceContestoPagamentoNA = Restrictions.eq("codRptInviarptCodiceContestoPagamento", "n/a");
		//			Criterion ibanAddebitoNull = Restrictions.isNull("deRptDatiVersIbanAddebito");
		//
		//			LogicalExpression codiceContestoPagamentoNA_AND_ibanAddebitoNull = Restrictions.and(codiceContestoPagamentoNA, ibanAddebitoNull);
		//			criteria.add(codiceContestoPagamentoNA_AND_ibanAddebitoNull);
		//
		//			break;
		//		case 1:
		//			break;
		//		case 2: //MODELLO 2 - DIFFERITO
		//			codiceContestoPagamentoNA = Restrictions.eq("codRptInviarptCodiceContestoPagamento", "n/a");
		//			Criterion ibanAddebitoNotNull = Restrictions.isNotNull("deRptDatiVersIbanAddebito");
		//
		//			LogicalExpression codiceContestoPagamentoNA_AND_ibanAddebitoNotNull = Restrictions.and(codiceContestoPagamentoNA, ibanAddebitoNotNull);
		//			criteria.add(codiceContestoPagamentoNA_AND_ibanAddebitoNotNull);
		//
		//			break;
		//		case 4: //MODELLO 3 - ATTIVATO PRESSO PSP
		//			Criterion codiceContestoPagamentoNotNA = Restrictions.ne("codRptInviarptCodiceContestoPagamento", "n/a");
		//
		//			criteria.add(codiceContestoPagamentoNotNA);
		//			break;
		//		default:
		//			break;
		//		}
		Criterion modelloPagamentoCriterion = Restrictions.eq("modelloPagamento", modelloPagamento);
		criteria.add(modelloPagamentoCriterion);

		Criterion rtEsitoNonPresente = Restrictions.isNull("codRtInviartEsito");
		Criterion esitoPagamentoNonPresente = Restrictions.isNull("codRtDatiPagCodiceEsitoPagamento");
		Criterion ricevutaNonPresente = Restrictions.and(rtEsitoNonPresente, esitoPagamentoNonPresente);
		Criterion esitoOK = Restrictions.eq("deRptInviarptEsito", "OK");
		Criterion esitoKO = Restrictions.eq("deRptInviarptEsito", "KO");
		Criterion piuVecchiaDi = Restrictions.le("dtRptDataOraMessaggioRichiesta", DateUtils.addMinutes(new Date(), -intervalMinute));

		LogicalExpression esitoOK_AND_piuVecchiaDi = Restrictions.and(esitoOK, piuVecchiaDi);
		LogicalExpression esitoOKAndPiuVecchiaDi_OR_esitoKO = Restrictions.or(esitoOK_AND_piuVecchiaDi, esitoKO);
		LogicalExpression esitoNonPresente_AND_esitoOKAndPiuVecchiaDi_OR_esitoKO = Restrictions.and(ricevutaNonPresente, esitoOKAndPiuVecchiaDi_OR_esitoKO);

		criteria.add(esitoNonPresente_AND_esitoOKAndPiuVecchiaDi_OR_esitoKO);
		criteria.addOrder(Order.asc("dtRptDataOraMessaggioRichiesta"));

		return getHibernateTemplate().findByCriteria(criteria);

	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RptRtDao#getRptByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(java.lang.String, java.lang.String, java.lang.String)
	 *
	 * USATO SOLO DA DUMMY
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MygovRptRt getRptByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(String idDominio, String idUnivocoVersamento,
			String codiceContestoPagamento) {
		List<MygovRptRt> results = null;

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRptRt.class);

		criteria.add(Restrictions.eq("codRptInviarptIdDominio", idDominio));
		criteria.add(Restrictions.eq("codRptInviarptIdUnivocoVersamento", idUnivocoVersamento));
		criteria.add(Restrictions.eq("codRptInviarptCodiceContestoPagamento", codiceContestoPagamento));

		results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("'mygovRptRt' is not unique", 1, results.size());

		return results.size() == 0 ? null : (MygovRptRt) results.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MygovRptRt getRtByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(final String idDominio, final String idUnivocoVersamento,
			final String codiceContestoPagamento) {
		List<MygovRptRt> results = null;

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRptRt.class);

		criteria.add(Restrictions.eq("codRtInviartIdDominio", idDominio));
		criteria.add(Restrictions.eq("codRtInviartIdUnivocoVersamento", idUnivocoVersamento));
		criteria.add(Restrictions.eq("codRtInviartCodiceContestoPagamento", codiceContestoPagamento));

		results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("'mygovRptRt' is not unique", 1, results.size());

		return results.size() == 0 ? null : (MygovRptRt) results.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<MygovRptRt> findAllRptAttivate() throws DataAccessException {
		List<MygovRptRt> results = null;

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRptRt.class);

		Criterion esitoInvioRPTNull = Restrictions.isNull("deRptInviarptEsito");

		LogicalExpression modelloPagamento2o4 = Restrictions.or(Restrictions.eq("modelloPagamento", 2), Restrictions.eq("modelloPagamento", 4));

		LogicalExpression modelloPagamento2o4_and_esitoInvioRPTNull = Restrictions.and(modelloPagamento2o4, esitoInvioRPTNull);

		criteria.add(modelloPagamento2o4_and_esitoInvioRPTNull);

		results = getHibernateTemplate().findByCriteria(criteria);

		return results;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public MygovRptRt updateCarrelloRef(Long mygovRptId, MygovCarrelloRpt mygovCarrelloRpt) {
		MygovRptRt mygovRpt = doGet(mygovRptId);

		mygovRpt.setMygov_carrello_rpt(mygovCarrelloRpt);

		getHibernateTemplate().update(mygovRpt);

		return mygovRpt;
	}

	
}
