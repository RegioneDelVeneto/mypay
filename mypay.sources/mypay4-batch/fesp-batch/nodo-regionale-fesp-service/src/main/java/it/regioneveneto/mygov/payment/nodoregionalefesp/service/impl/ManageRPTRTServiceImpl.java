/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import it.gov.digitpa.schemas.x2011.pagamenti.CtRicevutaTelematica;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RptRtDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RptRtDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRpt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRtDettaglio;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RptRtDettaglioDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPTRTService;
import it.regioneveneto.mygov.payment.utils.Utils;

/**
 * @author regione del veneto
 * @author regione del veneto
 *
 */
public class ManageRPTRTServiceImpl implements ManageRPTRTService {

	private static final Log log = LogFactory.getLog(ManageRPTRTServiceImpl.class);

	private RptRtDao rptRtDao;

	private RptRtDettaglioDao rptRtDettaglioDao;

	/**
	 * @param rptRtDao
	 */
	public void setRptRtDao(RptRtDao rptRtDao) {
		this.rptRtDao = rptRtDao;
	}

	/**
	 * @param rptRtDettaglioDao the rptRtDettaglioDao to set
	 */
	public void setRptRtDettaglioDao(RptRtDettaglioDao rptRtDettaglioDao) {
		this.rptRtDettaglioDao = rptRtDettaglioDao;
	}

	@Override
	@Transactional
	public void updateRtById(Long mygovRptRtId, String deRtInviartTipoFirma, String codRtInviartIdIntermediarioPa, String codRtInviartIdStazioneIntermediarioPa,
			String codRtInviartIdDominio, String codRtInviartIdUnivocoVersamento, String codRtInviartCodiceContestoPagamento, String deRtVersioneOggetto,
			String codRtDomIdDominio, String codRtDomIdStazioneRichiedente, String codRtIdMessaggioRicevuta, Date dtRtDataOraMessaggioRicevuta,
			String codRtRiferimentoMessaggioRichiesta, Date dtRtRiferimentoDataRichiesta, String codRtIstitAttesIdUnivAttesTipoIdUnivoco,
			String codRtIstitAttesIdUnivAttesCodiceIdUnivoco, String deRtIstitAttesDenominazioneAttestante, String codRtIstitAttesCodiceUnitOperAttestante,
			String deRtIstitAttesDenomUnitOperAttestante, String deRtIstitAttesIndirizzoAttestante, String deRtIstitAttesCivicoAttestante,
			String codRtIstitAttesCapAttestante, String deRtIstitAttesLocalitaAttestante, String deRtIstitAttesProvinciaAttestante,
			String codRtIstitAttesNazioneAttestante, String codRtEnteBenefIdUnivBenefTipoIdUnivoco, String codRtEnteBenefIdUnivBenefCodiceIdUnivoco,
			String deRtEnteBenefDenominazioneBeneficiario, String codRtEnteBenefCodiceUnitOperBeneficiario, String deRtEnteBenefDenomUnitOperBeneficiario,
			String deRtEnteBenefIndirizzoBeneficiario, String deRtEnteBenefCivicoBeneficiario, String codRtEnteBenefCapBeneficiario,
			String deRtEnteBenefLocalitaBeneficiario, String deRtEnteBenefProvinciaBeneficiario, String codRtEnteBenefNazioneBeneficiario,
			String codRtSoggVersIdUnivVersTipoIdUnivoco, String codRtSoggVersIdUnivVersCodiceIdUnivoco, String deRtSoggVersAnagraficaVersante,
			String deRtSoggVersIndirizzoVersante, String deRtSoggVersCivicoVersante, String codRtSoggVersCapVersante, String deRtSoggVersLocalitaVersante,
			String deRtSoggVersProvinciaVersante, String codRtSoggVersNazioneVersante, String deRtSoggVersEmailVersante,
			String codRtSoggPagIdUnivPagTipoIdUnivoco, String codRtSoggPagIdUnivPagCodiceIdUnivoco, String deRtSoggPagAnagraficaPagatore,
			String deRtSoggPagIndirizzoPagatore, String deRtSoggPagCivicoPagatore, String codRtSoggPagCapPagatore, String deRtSoggPagLocalitaPagatore,
			String deRtSoggPagProvinciaPagatore, String codRtSoggPagNazionePagatore, String deRtSoggPagEmailPagatore, String codRtDatiPagCodiceEsitoPagamento,
			BigDecimal numRtDatiPagImportoTotalePagato, String codRtDatiPagIdUnivocoVersamento, String codRtDatiPagCodiceContestoPagamento,
			List<RptRtDettaglioDto> rptRtDettaglios, byte[] blbRtPayload) {

		log.debug("Invocato updateRtById con: id = [" + mygovRptRtId + "] ");

		MygovRptRt mygovRptRt = rptRtDao.updateRtById(mygovRptRtId, deRtInviartTipoFirma, codRtInviartIdIntermediarioPa, codRtInviartIdStazioneIntermediarioPa,
				codRtInviartIdDominio, codRtInviartIdUnivocoVersamento, codRtInviartCodiceContestoPagamento, deRtVersioneOggetto, codRtDomIdDominio,
				codRtDomIdStazioneRichiedente, codRtIdMessaggioRicevuta, dtRtDataOraMessaggioRicevuta, codRtRiferimentoMessaggioRichiesta,
				dtRtRiferimentoDataRichiesta, codRtIstitAttesIdUnivAttesTipoIdUnivoco, codRtIstitAttesIdUnivAttesCodiceIdUnivoco,
				deRtIstitAttesDenominazioneAttestante, codRtIstitAttesCodiceUnitOperAttestante, deRtIstitAttesDenomUnitOperAttestante,
				deRtIstitAttesIndirizzoAttestante, deRtIstitAttesCivicoAttestante, codRtIstitAttesCapAttestante, deRtIstitAttesLocalitaAttestante,
				deRtIstitAttesProvinciaAttestante, codRtIstitAttesNazioneAttestante, codRtEnteBenefIdUnivBenefTipoIdUnivoco,
				codRtEnteBenefIdUnivBenefCodiceIdUnivoco, deRtEnteBenefDenominazioneBeneficiario, codRtEnteBenefCodiceUnitOperBeneficiario,
				deRtEnteBenefDenomUnitOperBeneficiario, deRtEnteBenefIndirizzoBeneficiario, deRtEnteBenefCivicoBeneficiario, codRtEnteBenefCapBeneficiario,
				deRtEnteBenefLocalitaBeneficiario, deRtEnteBenefProvinciaBeneficiario, codRtEnteBenefNazioneBeneficiario, codRtSoggVersIdUnivVersTipoIdUnivoco,
				codRtSoggVersIdUnivVersCodiceIdUnivoco, deRtSoggVersAnagraficaVersante, deRtSoggVersIndirizzoVersante, deRtSoggVersCivicoVersante,
				codRtSoggVersCapVersante, deRtSoggVersLocalitaVersante, deRtSoggVersProvinciaVersante, codRtSoggVersNazioneVersante, deRtSoggVersEmailVersante,
				codRtSoggPagIdUnivPagTipoIdUnivoco, codRtSoggPagIdUnivPagCodiceIdUnivoco, deRtSoggPagAnagraficaPagatore, deRtSoggPagIndirizzoPagatore,
				deRtSoggPagCivicoPagatore, codRtSoggPagCapPagatore, deRtSoggPagLocalitaPagatore, deRtSoggPagProvinciaPagatore, codRtSoggPagNazionePagatore,
				deRtSoggPagEmailPagatore, codRtDatiPagCodiceEsitoPagamento, numRtDatiPagImportoTotalePagato, codRtDatiPagIdUnivocoVersamento,
				codRtDatiPagCodiceContestoPagamento, blbRtPayload);

		log.debug("updateRtByRptIuv lettura caricamento singoli pagamenti");

		if (rptRtDettaglios.size() > 0) {
			Iterator<RptRtDettaglioDto> listDettagliDaAggiornare = rptRtDettaglios.iterator();
			List<MygovRptRtDettaglio> listDett = rptRtDettaglioDao.getByRptRt(mygovRptRt);
			for (MygovRptRtDettaglio dett : listDett) {

				RptRtDettaglioDto dettaglioDaAggiornare = listDettagliDaAggiornare.next();

				rptRtDettaglioDao.updateDateRt(dett.getMygovRptRtDettaglioId(), dett.getVersion(),
						dettaglioDaAggiornare.getNumRtDatiPagDatiSingPagSingoloImportoPagato(),
						dettaglioDaAggiornare.getDeRtDatiPagDatiSingPagEsitoSingoloPagamento(),
						dettaglioDaAggiornare.getDtRtDatiPagDatiSingPagDataEsitoSingoloPagamento(),
						dettaglioDaAggiornare.getCodRtDatiPagDatiSingPagIdUnivocoRiscossione(),
						dettaglioDaAggiornare.getDeRtDatiPagDatiSingPagCausaleVersamento(),
						dettaglioDaAggiornare.getDeRtDatiPagDatiSingPagDatiSpecificiRiscossione(),
						dettaglioDaAggiornare.getNumRtDatiPagDatiSingPagCommissioniApplicatePsp(),
						dettaglioDaAggiornare.getCodRtDatiPagDatiSingPagAllegatoRicevutaTipo(),
						dettaglioDaAggiornare.getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest());
			}
		}

		log.debug("fine updateRtByRptIuv ");

	}

	@Override
	public List<MygovRptRtDettaglio> getByRptRt(MygovRptRt mygovRptRt) throws DataAccessException {

		return rptRtDettaglioDao.getByRptRt(mygovRptRt);
	}

	@Override
	@Transactional
	public MygovRptRt insertRpt(String deRptInviarptPassword, String codRptInviarptIdPsp, String codRptInviarptIdIntermediarioPsp,
			String codRptInviarptIdCanale, String deRptInviarptTipoFirma, String codRptInviarptIdIntermediarioPa,
			String codRptInviarptIdStazioneIntermediarioPa, String codRptInviarptIdDominio, String codRptInviarptIdUnivocoVersamento,
			String codRptInviarptCodiceContestoPagamento, String deRptVersioneOggetto, String codRptDomIdDominio, String codRptDomIdStazioneRichiedente,
			String codRptIdMessaggioRichiesta, Date dtRptDataOraMessaggioRichiesta, String codRptAutenticazioneSoggetto,
			String codRptSoggVersIdUnivVersTipoIdUnivoco, String codRptSoggVersIdUnivVersCodiceIdUnivoco, String deRptSoggVersAnagraficaVersante,
			String deRptSoggVersIndirizzoVersante, String deRptSoggVersCivicoVersante, String codRptSoggVersCapVersante, String deRptSoggVersLocalitaVersante,
			String deRptSoggVersProvinciaVersante, String codRptSoggVersNazioneVersante, String deRptSoggVersEmailVersante,
			String codRptSoggPagIdUnivPagTipoIdUnivoco, String codRptSoggPagIdUnivPagCodiceIdUnivoco, String deRptSoggPagAnagraficaPagatore,
			String deRptSoggPagIndirizzoPagatore, String deRptSoggPagCivicoPagatore, String codRptSoggPagCapPagatore, String deRptSoggPagLocalitaPagatore,
			String deRptSoggPagProvinciaPagatore, String codRptSoggPagNazionePagatore, String deRptSoggPagEmailPagatore,
			String codRptEnteBenefIdUnivBenefTipoIdUnivoco, String codRptEnteBenefIdUnivBenefCodiceIdUnivoco, String deRptEnteBenefDenominazioneBeneficiario,
			String codRptEnteBenefCodiceUnitOperBeneficiario, String deRptEnteBenefDenomUnitOperBeneficiario, String deRptEnteBenefIndirizzoBeneficiario,
			String deRptEnteBenefCivicoBeneficiario, String codRptEnteBenefCapBeneficiario, String deRptEnteBenefLocalitaBeneficiario,
			String deRptEnteBenefProvinciaBeneficiario, String codRptEnteBenefNazioneBeneficiario, Date dtRptDatiVersDataEsecuzionePagamento,
			BigDecimal numRptDatiVersImportoTotaleDaVersare, String codRptDatiVersTipoVersamento, String codRptDatiVersIdUnivocoVersamento,
			String codRptDatiVersCodiceContestoPagamento, String deRptDatiVersIbanAddebito, String deRptDatiVersBicAddebito, String codRptDatiVersFirmaRicevuta,
			List<RptRtDettaglioDto> versamenti, final Integer modelloPagamento, Long mygovRpEId) {

		log.debug("Invocato insertRpt con: codRptDatiVersIdUnivocoVersamento = [" + codRptDatiVersIdUnivocoVersamento + "] ");

		MygovRptRt mygovRptRt = rptRtDao.insertRptWithRefresh(deRptInviarptPassword, codRptInviarptIdPsp, codRptInviarptIdIntermediarioPsp,
				codRptInviarptIdCanale, deRptInviarptTipoFirma, codRptInviarptIdIntermediarioPa, codRptInviarptIdStazioneIntermediarioPa,
				codRptInviarptIdDominio, codRptInviarptIdUnivocoVersamento, codRptInviarptCodiceContestoPagamento, deRptVersioneOggetto, codRptDomIdDominio,
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
				codRptDatiVersFirmaRicevuta, modelloPagamento, mygovRpEId);

		log.debug("insertRpt lettura caricamento singoli pagamenti");

		for (RptRtDettaglioDto dett : versamenti) {

			rptRtDettaglioDao.insert(mygovRptRt, dett.getNumRptDatiVersDatiSingVersImportoSingoloVersamento(),
					dett.getNumRptDatiVersDatiSingVersCommissioneCaricoPa(), dett.getDeRptDatiVersDatiSingVersIbanAccredito(),
					dett.getDeRptDatiVersDatiSingVersBicAccredito(), dett.getDeRptDatiVersDatiSingVersIbanAppoggio(),
					dett.getDeRptDatiVersDatiSingVersBicAppoggio(), dett.getCodRptDatiVersDatiSingVersCredenzialiPagatore(),
					dett.getDeRptDatiVersDatiSingVersCausaleVersamento(), dett.getDeRptDatiVersDatiSingVersDatiSpecificiRiscossione(),
					dett.getNumRtDatiPagDatiSingPagSingoloImportoPagato(), dett.getDeRtDatiPagDatiSingPagEsitoSingoloPagamento(),
					dett.getDtRtDatiPagDatiSingPagDataEsitoSingoloPagamento(), dett.getCodRtDatiPagDatiSingPagIdUnivocoRiscossione(),
					dett.getDeRtDatiPagDatiSingPagCausaleVersamento(), dett.getDeRtDatiPagDatiSingPagDatiSpecificiRiscossione(),
					dett.getNumRtDatiPagDatiSingPagCommissioniApplicatePsp(), dett.getCodRtDatiPagDatiSingPagAllegatoRicevutaTipo(),
					dett.getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest(),
					StringUtils.isNotBlank(dett.getCodRptDatiVersDatiSingVersDatiMbdTipoBollo()) ? dett.getCodRptDatiVersDatiSingVersDatiMbdTipoBollo() : null,
					StringUtils.isNotBlank(dett.getCodRptDatiVersDatiSingVersDatiMbdHashDocumento()) ? dett.getCodRptDatiVersDatiSingVersDatiMbdHashDocumento()
							: null,
					StringUtils.isNotBlank(dett.getCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza())
							? dett.getCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza() : null);
		}

		log.debug("fine insertRpt ");

		return mygovRptRt;
	}

	@Override
	public MygovRptRt updateRispostaRtById(Long mygovRptRtId, String codAckRt, String codRtInviartEsito, String codRtInviartFaultCode,
			String codRtInviartFaultString, String codRtInviartId, String deRtInviartDescription, Integer numRtInviartSerial,
			String codRtInviartOriginaltFaultCode, String deRtInviartOriginaltFaultString, String deRtInviartOriginaltFaultDescription) {

		log.debug("Invocato updateRispostaRtById con: mygovRptRtId = [" + mygovRptRtId + "] ");

		return rptRtDao.updateRispostaRtById(mygovRptRtId, codAckRt, codRtInviartEsito, codRtInviartFaultCode, codRtInviartFaultString, codRtInviartId,
				deRtInviartDescription, numRtInviartSerial, codRtInviartOriginaltFaultCode, deRtInviartOriginaltFaultString, deRtInviartOriginaltFaultDescription);
	}

	@Override
	public MygovRptRt updateRispostaRptById(Long mygovRptRtId, String deRptInviarptEsito, Integer numRptInviarptRedirect, String codRptInviarptUrl,
			String codRptInviarptFaultCode, String codRptInviarptFaultString, String codRptInviarptId, String deRptInviarptDescription,
			Integer numRptInviarptSerial,  String codRptInviarptOriginaltFaultCode, String deRptInviarptOriginaltFaultString,  
			 String deRptInviarptOriginaltFaultDescription) throws UnsupportedEncodingException, MalformedURLException {

		log.debug("Invocato updateRispostaRptById con: id = [" + mygovRptRtId + "] ");

		String idSessionSPC = null;
		if (StringUtils.isNotBlank(codRptInviarptUrl)) {
			//estrarre idSessioSPC
			Map<String, String> parametersMap = Utils.splitQuery(new URL(codRptInviarptUrl));
			idSessionSPC = parametersMap.get("idSession");
		}

		return rptRtDao.updateRispostaRptById(mygovRptRtId, deRptInviarptEsito, numRptInviarptRedirect, codRptInviarptUrl, codRptInviarptFaultCode,
				codRptInviarptFaultString, codRptInviarptId, deRptInviarptDescription, numRptInviarptSerial, idSessionSPC, codRptInviarptOriginaltFaultCode,
				deRptInviarptOriginaltFaultString, deRptInviarptOriginaltFaultDescription);
	}

	@Override
	public List<MygovRptRt> updateRispostaRptByCarrello(MygovCarrelloRpt mygovCarrelloRpt, String deRptInviarptEsito, String codRptInviarptUrl,
		FaultBean faultBean) throws UnsupportedEncodingException, MalformedURLException, DataAccessException {

		String codRptInviarptFaultCode = faultBean.getFaultCode();
		String codRptInviarptFaultString = faultBean.getFaultString();
		String codRptInviarptId = faultBean.getId();
		String deRptInviarptDescription = faultBean.getDescription();
		Integer numRptInviarptSerial = faultBean.getSerial();
		String codRptInviarptOriginaltFaultCode = faultBean.getOriginalFaultCode();
		String deRptInviarptOriginaltFaultString = faultBean.getOriginalFaultString();
		String deRptInviarptOriginaltFaultDescription = faultBean.getOriginalDescription();

		log.debug("Invocato updateRispostaRptByCarrello con: idCarrello = [" + mygovCarrelloRpt.getMygovCarrelloRpId() + "] ");

		return rptRtDao.updateRispostaRptByCarrello(mygovCarrelloRpt, deRptInviarptEsito, codRptInviarptUrl,
				codRptInviarptFaultCode, codRptInviarptFaultString, codRptInviarptId, deRptInviarptDescription,
				numRptInviarptSerial, codRptInviarptOriginaltFaultCode, deRptInviarptOriginaltFaultString,
				deRptInviarptOriginaltFaultDescription);
	}

	@Override
	public MygovRptRt updateCodRtDatiPagCodiceEsitoPagamentoByIdSession(final String idSession, final String esito) {
		return rptRtDao.updateCodRtDatiPagCodiceEsitoPagamentoByIdSession(idSession, esito);
	}

	@Override
	public MygovRptRt getRptByRpEId(final Long mygovRpEId) {
		return rptRtDao.getRptByMygovRpEId(mygovRpEId);
	}

	@Override
	public MygovRptRt getRptByIdSession(final String nodoSPCFespIdSession) {
		return rptRtDao.getRptByIdSession(nodoSPCFespIdSession);
	}

	@Override
	public MygovRptRt getRptByCodRptIdMessaggioRichiesta(final String riferimentoMessaggioRichiesta) {
		return rptRtDao.getRptByCodRptIdMessaggioRichiesta(riferimentoMessaggioRichiesta);
	}

	@Override
	public boolean validaUguaglianzaCampiRptRt(CtRicevutaTelematica rt) {

		MygovRptRt mygovRptRt = this.getRptByCodRptIdMessaggioRichiesta(rt.getRiferimentoMessaggioRichiesta());

		// per ogni campo facciamo sto roba
		// if !((NULL && NULL) || (!NULL && value==value)) return false

	//	if (!mygovRptRt.getDeRptVersioneOggetto().equals(rt.getVersioneOggetto())) {
	//		log.debug("validaUguaglianzaCampiRptRt: campo [DeRptVersioneOggetto] non coerente");
//			return false;
	//	}

		if (!mygovRptRt.getCodRptDomIdDominio().equals(rt.getDominio().getIdentificativoDominio())) {
			log.debug("validaUguaglianzaCampiRptRt: campo [IdentificativoDominio] non coerente");
			return false;
		}

		if (!((mygovRptRt.getCodRptDomIdStazioneRichiedente() == null && rt.getDominio().getIdentificativoStazioneRichiedente() == null)
				|| (mygovRptRt.getCodRptDomIdStazioneRichiedente() != null
						&& mygovRptRt.getCodRptDomIdStazioneRichiedente().equals(rt.getDominio().getIdentificativoStazioneRichiedente())))) {
			log.debug("validaUguaglianzaCampiRptRt: campo [IdentificativoStazioneRichiedente] non coerente");
			return false;
		}

		if (!mygovRptRt.getCodRptDomIdDominio().equals(rt.getDominio().getIdentificativoDominio())) {
			log.debug("validaUguaglianzaCampiRptRt: campo [IdentificativoDominio] non coerente");
			return false;
		}

		//soggetto versante
		if (rt.getSoggettoVersante() != null && mygovRptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco() != null) {

			if (!((rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco() == null
					&& mygovRptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco() == null)
					|| (rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco() != null
							&& rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco()
									.equalsIgnoreCase(mygovRptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco())))) {
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.IdentificativoUnivocoVersante] non coerente");
				return false;
			}

			if (!((rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() == null
					&& mygovRptRt.getCodRptSoggVersIdUnivVersTipoIdUnivoco() == null)
					|| (rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() != null
							&& rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString()
									.equalsIgnoreCase(mygovRptRt.getCodRptSoggVersIdUnivVersTipoIdUnivoco())))) {
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.TipoIdentificativoUnivoco] non coerente");
				return false;
			}

			/*
			
			if (!((rt.getSoggettoVersante().getAnagraficaVersante() == null && mygovRptRt
					.getDeRptSoggVersAnagraficaVersante() == null) || (rt.getSoggettoVersante().getAnagraficaVersante() != null && rt
					.getSoggettoVersante().getAnagraficaVersante().equals(mygovRptRt.getDeRptSoggVersAnagraficaVersante())))){
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.AnagraficaVersante] non coerente");	
				return false;
			}
			
			if (!((rt.getSoggettoVersante().getIndirizzoVersante() == null && mygovRptRt
					.getDeRptSoggVersIndirizzoVersante() == null) || (rt.getSoggettoVersante().getIndirizzoVersante() != null && rt
					.getSoggettoVersante().getIndirizzoVersante().equals(mygovRptRt.getDeRptSoggVersIndirizzoVersante())))){
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.IndirizzoVersante] non coerente");	
				return false;
			}
			
			if (!((rt.getSoggettoVersante().getCivicoVersante() == null && mygovRptRt.getDeRptSoggVersCivicoVersante() == null) || (rt
					.getSoggettoVersante().getCivicoVersante() != null && rt.getSoggettoVersante().getCivicoVersante()
					.equals(mygovRptRt.getDeRptSoggVersCivicoVersante())))){
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.CivicoVersante] non coerente");
				return false;
			}
			
			if (!((rt.getSoggettoVersante().getCapVersante() == null && mygovRptRt.getCodRptSoggVersCapVersante() == null) || (rt
					.getSoggettoVersante().getCapVersante() != null && rt.getSoggettoVersante().getCapVersante()
					.equals(mygovRptRt.getCodRptSoggVersCapVersante())))){
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.CapVersante] non coerente");
				return false;
			}
			
			if (!((rt.getSoggettoVersante().getLocalitaVersante() == null && mygovRptRt.getDeRptSoggVersLocalitaVersante() == null) || (rt
					.getSoggettoVersante().getLocalitaVersante() != null && rt.getSoggettoVersante().getLocalitaVersante()
					.equals(mygovRptRt.getDeRptSoggVersLocalitaVersante())))){
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.LocalitaVersante] non coerente");
				return false;
			}
			
			
			
			if (!((rt.getSoggettoVersante().getProvinciaVersante() == null && mygovRptRt.getDeRptSoggVersProvinciaVersante() == null)
					|| (rt.getSoggettoVersante().getProvinciaVersante() != null
							&& rt.getSoggettoVersante().getProvinciaVersante().equalsIgnoreCase(mygovRptRt.getDeRptSoggVersProvinciaVersante())))) {
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.ProvinciaVersante] non coerente");
				return false;
			}
			
			if (!((rt.getSoggettoVersante().getNazioneVersante() == null && mygovRptRt.getCodRptSoggVersNazioneVersante() == null)
					|| (rt.getSoggettoVersante().getNazioneVersante() != null
							&& rt.getSoggettoVersante().getNazioneVersante().equalsIgnoreCase(mygovRptRt.getCodRptSoggVersNazioneVersante())))) {
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.NazioneVersante] non coerente");
				return false;
			}
			
			if (!((rt.getSoggettoVersante().getEMailVersante() == null && mygovRptRt.getDeRptSoggVersEmailVersante() == null)
					|| (rt.getSoggettoVersante().getEMailVersante() != null
							&& rt.getSoggettoVersante().getEMailVersante().equalsIgnoreCase(mygovRptRt.getDeRptSoggVersEmailVersante())))) {
				log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.EMailVersante] non coerente");
				return false;
			}
			*/
		}

		//soggetto pagatore
		if ((rt.getSoggettoPagatore() == null && mygovRptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco() != null)
				|| (rt.getSoggettoPagatore() != null && mygovRptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco() == null)) {
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore] non coerente");
			return false;
		}

		if (!((rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco() == null
				&& mygovRptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco() == null)
				|| (rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco() != null
						&& rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco()
								.equalsIgnoreCase(mygovRptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco())))) {
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.CodiceIdentificativoUnivoco] non coerente");
			return false;
		}

		if (!((rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco() == null
				&& mygovRptRt.getCodRptSoggPagIdUnivPagTipoIdUnivoco() == null)
				|| (rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco() != null
						&& rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString()
								.equalsIgnoreCase(mygovRptRt.getCodRptSoggPagIdUnivPagTipoIdUnivoco())))) {
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.TipoIdentificativoUnivoco] non coerente");
			return false;
		}

		/*
		
		if (!((rt.getSoggettoPagatore().getAnagraficaPagatore() == null && mygovRptRt
				.getDeRptSoggPagAnagraficaPagatore() == null) || (rt.getSoggettoPagatore().getAnagraficaPagatore() != null && rt
				.getSoggettoPagatore().getAnagraficaPagatore().equals(mygovRptRt.getDeRptSoggPagAnagraficaPagatore())))){
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.AnagraficaPagatore] non coerente");
			return false;
		}
		
		if (!((rt.getSoggettoPagatore().getIndirizzoPagatore() == null && mygovRptRt
				.getDeRptSoggPagIndirizzoPagatore() == null) || (rt.getSoggettoPagatore().getIndirizzoPagatore() != null && rt
				.getSoggettoPagatore().getIndirizzoPagatore().equals(mygovRptRt.getDeRptSoggPagIndirizzoPagatore())))){
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.IndirizzoPagatore] non coerente");
			return false;
		}
		
		if (!((rt.getSoggettoPagatore().getCivicoPagatore() == null && mygovRptRt.getDeRptSoggPagCivicoPagatore() == null) || (rt
				.getSoggettoPagatore().getCivicoPagatore() != null && rt.getSoggettoPagatore().getCivicoPagatore()
				.equals(mygovRptRt.getDeRptSoggPagCivicoPagatore())))){
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.CivicoPagatore] non coerente");
			return false;
		}
		
		if (!((rt.getSoggettoPagatore().getCapPagatore() == null && mygovRptRt.getCodRptSoggPagCapPagatore() == null) || (rt
				.getSoggettoPagatore().getCapPagatore() != null && rt.getSoggettoPagatore().getCapPagatore()
				.equals(mygovRptRt.getCodRptSoggPagCapPagatore())))){
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.CapPagatore] non coerente");
			return false;
		}
		
		if (!((rt.getSoggettoPagatore().getLocalitaPagatore() == null && mygovRptRt.getDeRptSoggPagLocalitaPagatore() == null) || (rt
				.getSoggettoPagatore().getLocalitaPagatore() != null && rt.getSoggettoPagatore().getLocalitaPagatore()
				.equals(mygovRptRt.getDeRptSoggPagLocalitaPagatore())))){
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.LocalitaPagatore] non coerente");
			return false;
		}
		
		
		
		if (!((rt.getSoggettoPagatore().getProvinciaPagatore() == null && mygovRptRt.getDeRptSoggPagProvinciaPagatore() == null)
				|| (rt.getSoggettoPagatore().getProvinciaPagatore() != null
						&& rt.getSoggettoPagatore().getProvinciaPagatore().equalsIgnoreCase(mygovRptRt.getDeRptSoggPagProvinciaPagatore())))) {
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.ProvinciaPagatore] non coerente");
			return false;
		}
		
		if (!((rt.getSoggettoPagatore().getNazionePagatore() == null && mygovRptRt.getCodRptSoggPagNazionePagatore() == null)
				|| (rt.getSoggettoPagatore().getNazionePagatore() != null
						&& rt.getSoggettoPagatore().getNazionePagatore().equalsIgnoreCase(mygovRptRt.getCodRptSoggPagNazionePagatore())))) {
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.NazionePagatore] non coerente");
			return false;
		}
		
		if (!((rt.getSoggettoPagatore().getEMailPagatore() == null && mygovRptRt.getDeRptSoggPagEmailPagatore() == null)
				|| (rt.getSoggettoPagatore().getEMailPagatore() != null
						&& rt.getSoggettoPagatore().getEMailPagatore().equalsIgnoreCase(mygovRptRt.getDeRptSoggPagEmailPagatore())))) {
			log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.EMailPagatore] non coerente");
			return false;
		}
		
		*/

		return true;
	}

	@Override
	public MygovRptRt updateStatoRtById(final Long mygovRptRtId, final String codStato) {
		return rptRtDao.updateStatoRtById(mygovRptRtId, codStato);
	}

	public MygovRptRt clearInviarptEsitoById(Long mygovRptRtId) {
		return rptRtDao.clearInviarptEsitoById(mygovRptRtId);
	}

	@Override
	public List<MygovRptRt> findAllRptPendenti(final int modelloPagamento, final int intervalMinute) {
		return rptRtDao.findAllRptPendenti(modelloPagamento, intervalMinute);
	}

	@Override
	public MygovRptRt getRptByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(String identificativoDominio, String identificativoUnivocoVersamento,
			String codiceContestoPagamento) {
		return rptRtDao.getRptByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(identificativoDominio, identificativoUnivocoVersamento,
				codiceContestoPagamento);
	}

	@Override
	public MygovRptRt getRtByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(String identificativoDominio, String identificativoUnivocoVersamento,
			String codiceContestoPagamento) {
		return rptRtDao.getRtByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(identificativoDominio, identificativoUnivocoVersamento,
				codiceContestoPagamento);
	}

	@Override
	public MygovRptRt updateCarrelloRef(Long mygovRptRtId, MygovCarrelloRpt mygovCarrelloRpt) {
		
		return rptRtDao.updateCarrelloRef(mygovRptRtId, mygovCarrelloRpt);
	}
	
}
