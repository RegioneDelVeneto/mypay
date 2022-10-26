/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.gov.digitpa.schemas.x2011.pagamenti.CtRicevutaTelematica;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRpt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRtDettaglio;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RptRtDettaglioDto;

/**
 * @author regione del veneto
 *
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface ManageRPTRTService {

	/**
	 * @param mygovRptRtId
	 * @param deRtInviartTipoFirma
	 * @param codRtInviartIdIntermediarioPa
	 * @param codRtInviartIdStazioneIntermediarioPa
	 * @param codRtInviartIdDominio
	 * @param codRtInviartIdUnivocoVersamento
	 * @param codRtInviartCodiceContestoPagamento
	 * @param deRtVersioneOggetto
	 * @param codRtDomIdDominio
	 * @param codRtDomIdStazioneRichiedente
	 * @param codRtIdMessaggioRicevuta
	 * @param dtRtDataOraMessaggioRicevuta
	 * @param codRtRiferimentoMessaggioRichiesta
	 * @param dtRtRiferimentoDataRichiesta
	 * @param codRtIstitAttesIdUnivAttesTipoIdUnivoco
	 * @param codRtIstitAttesIdUnivAttesCodiceIdUnivoco
	 * @param deRtIstitAttesDenominazioneAttestante
	 * @param codRtIstitAttesCodiceUnitOperAttestante
	 * @param deRtIstitAttesDenomUnitOperAttestante
	 * @param deRtIstitAttesIndirizzoAttestante
	 * @param deRtIstitAttesCivicoAttestante
	 * @param codRtIstitAttesCapAttestante
	 * @param deRtIstitAttesLocalitaAttestante
	 * @param deRtIstitAttesProvinciaAttestante
	 * @param codRtIstitAttesNazioneAttestante
	 * @param codRtEnteBenefIdUnivBenefTipoIdUnivoco
	 * @param codRtEnteBenefIdUnivBenefCodiceIdUnivoco
	 * @param deRtEnteBenefDenominazioneBeneficiario
	 * @param codRtEnteBenefCodiceUnitOperBeneficiario
	 * @param deRtEnteBenefDenomUnitOperBeneficiario
	 * @param deRtEnteBenefIndirizzoBeneficiario
	 * @param deRtEnteBenefCivicoBeneficiario
	 * @param codRtEnteBenefCapBeneficiario
	 * @param deRtEnteBenefLocalitaBeneficiario
	 * @param deRtEnteBenefProvinciaBeneficiario
	 * @param codRtEnteBenefNazioneBeneficiario
	 * @param codRtSoggVersIdUnivVersTipoIdUnivoco
	 * @param codRtSoggVersIdUnivVersCodiceIdUnivoco
	 * @param deRtSoggVersAnagraficaVersante
	 * @param deRtSoggVersIndirizzoVersante
	 * @param deRtSoggVersCivicoVersante
	 * @param codRtSoggVersCapVersante
	 * @param deRtSoggVersLocalitaVersante
	 * @param deRtSoggVersProvinciaVersante
	 * @param codRtSoggVersNazioneVersante
	 * @param deRtSoggVersEmailVersante
	 * @param codRtSoggPagIdUnivPagTipoIdUnivoco
	 * @param codRtSoggPagIdUnivPagCodiceIdUnivoco
	 * @param deRtSoggPagAnagraficaPagatore
	 * @param deRtSoggPagIndirizzoPagatore
	 * @param deRtSoggPagCivicoPagatore
	 * @param codRtSoggPagCapPagatore
	 * @param deRtSoggPagLocalitaPagatore
	 * @param deRtSoggPagProvinciaPagatore
	 * @param codRtSoggPagNazionePagatore
	 * @param deRtSoggPagEmailPagatore
	 * @param codRtDatiPagCodiceEsitoPagamento
	 * @param numRtDatiPagImportoTotalePagato
	 * @param codRtDatiPagIdUnivocoVersamento
	 * @param codRtDatiPagCodiceContestoPagamento
	 * @param rptRtDettaglio
	 * @param blbRtPayload
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	void updateRtById(final Long mygovRptRtId, final String deRtInviartTipoFirma, final String codRtInviartIdIntermediarioPa,
			final String codRtInviartIdStazioneIntermediarioPa, final String codRtInviartIdDominio, final String codRtInviartIdUnivocoVersamento,
			final String codRtInviartCodiceContestoPagamento, final String deRtVersioneOggetto, final String codRtDomIdDominio,
			final String codRtDomIdStazioneRichiedente, final String codRtIdMessaggioRicevuta, final Date dtRtDataOraMessaggioRicevuta,
			final String codRtRiferimentoMessaggioRichiesta, final Date dtRtRiferimentoDataRichiesta, final String codRtIstitAttesIdUnivAttesTipoIdUnivoco,
			final String codRtIstitAttesIdUnivAttesCodiceIdUnivoco, final String deRtIstitAttesDenominazioneAttestante,
			final String codRtIstitAttesCodiceUnitOperAttestante, final String deRtIstitAttesDenomUnitOperAttestante,
			final String deRtIstitAttesIndirizzoAttestante, final String deRtIstitAttesCivicoAttestante, final String codRtIstitAttesCapAttestante,
			final String deRtIstitAttesLocalitaAttestante, final String deRtIstitAttesProvinciaAttestante, final String codRtIstitAttesNazioneAttestante,
			final String codRtEnteBenefIdUnivBenefTipoIdUnivoco, final String codRtEnteBenefIdUnivBenefCodiceIdUnivoco,
			final String deRtEnteBenefDenominazioneBeneficiario, final String codRtEnteBenefCodiceUnitOperBeneficiario,
			final String deRtEnteBenefDenomUnitOperBeneficiario, final String deRtEnteBenefIndirizzoBeneficiario, final String deRtEnteBenefCivicoBeneficiario,
			final String codRtEnteBenefCapBeneficiario, final String deRtEnteBenefLocalitaBeneficiario, final String deRtEnteBenefProvinciaBeneficiario,
			final String codRtEnteBenefNazioneBeneficiario, final String codRtSoggVersIdUnivVersTipoIdUnivoco,
			final String codRtSoggVersIdUnivVersCodiceIdUnivoco, final String deRtSoggVersAnagraficaVersante, final String deRtSoggVersIndirizzoVersante,
			final String deRtSoggVersCivicoVersante, final String codRtSoggVersCapVersante, final String deRtSoggVersLocalitaVersante,
			final String deRtSoggVersProvinciaVersante, final String codRtSoggVersNazioneVersante, final String deRtSoggVersEmailVersante,
			final String codRtSoggPagIdUnivPagTipoIdUnivoco, final String codRtSoggPagIdUnivPagCodiceIdUnivoco, final String deRtSoggPagAnagraficaPagatore,
			final String deRtSoggPagIndirizzoPagatore, final String deRtSoggPagCivicoPagatore, final String codRtSoggPagCapPagatore,
			final String deRtSoggPagLocalitaPagatore, final String deRtSoggPagProvinciaPagatore, final String codRtSoggPagNazionePagatore,
			final String deRtSoggPagEmailPagatore, final String codRtDatiPagCodiceEsitoPagamento, final BigDecimal numRtDatiPagImportoTotalePagato,
			final String codRtDatiPagIdUnivocoVersamento, final String codRtDatiPagCodiceContestoPagamento, final List<RptRtDettaglioDto> rptRtDettaglio,
			byte[] blbRtPayload);
	
	/**
	 * @param mygovCarrelloRpt
	 * @param deRptInviarptEsito
	 * @param codRptInviarptUrl
	 * @param faultBean
	 * @return
	 * @throws DataAccessException
	 */
	List<MygovRptRt> updateRispostaRptByCarrello(final MygovCarrelloRpt mygovCarrelloRpt, final String deRptInviarptEsito, final String codRptInviarptUrl,
			final FaultBean faultBean) throws UnsupportedEncodingException, MalformedURLException, DataAccessException;

	/**
	 * @param deRptInviarptPassword
	 * @param codRptInviarptIdPsp
	 * @param codRptInviarptIdIntermediarioPsp
	 * @param codRptInviarptIdCanale
	 * @param deRptInviarptTipoFirma
	 * @param codRptInviarptIdIntermediarioPa
	 * @param codRptInviarptIdStazioneIntermediarioPa
	 * @param codRptInviarptIdDominio
	 * @param codRptInviarptIdUnivocoVersamento
	 * @param codRptInviarptCodiceContestoPagamento
	 * @param deRptVersioneOggetto
	 * @param codRptDomIdDominio
	 * @param codRptDomIdStazioneRichiedente
	 * @param codRptIdMessaggioRichiesta
	 * @param dtRptDataOraMessaggioRichiesta
	 * @param codRptAutenticazioneSoggetto
	 * @param codRptSoggVersIdUnivVersTipoIdUnivoco
	 * @param codRptSoggVersIdUnivVersCodiceIdUnivoco
	 * @param deRptSoggVersAnagraficaVersante
	 * @param deRptSoggVersIndirizzoVersante
	 * @param deRptSoggVersCivicoVersante
	 * @param codRptSoggVersCapVersante
	 * @param deRptSoggVersLocalitaVersante
	 * @param deRptSoggVersProvinciaVersante
	 * @param codRptSoggVersNazioneVersante
	 * @param deRptSoggVersEmailVersante
	 * @param codRptSoggPagIdUnivPagTipoIdUnivoco
	 * @param codRptSoggPagIdUnivPagCodiceIdUnivoco
	 * @param deRptSoggPagAnagraficaPagatore
	 * @param deRptSoggPagIndirizzoPagatore
	 * @param deRptSoggPagCivicoPagatore
	 * @param codRptSoggPagCapPagatore
	 * @param deRptSoggPagLocalitaPagatore
	 * @param deRptSoggPagProvinciaPagatore
	 * @param codRptSoggPagNazionePagatore
	 * @param deRptSoggPagEmailPagatore
	 * @param codRptEnteBenefIdUnivBenefTipoIdUnivoco
	 * @param codRptEnteBenefIdUnivBenefCodiceIdUnivoco
	 * @param deRptEnteBenefDenominazioneBeneficiario
	 * @param codRptEnteBenefCodiceUnitOperBeneficiario
	 * @param deRptEnteBenefDenomUnitOperBeneficiario
	 * @param deRptEnteBenefIndirizzoBeneficiario
	 * @param deRptEnteBenefCivicoBeneficiario
	 * @param codRptEnteBenefCapBeneficiario
	 * @param deRptEnteBenefLocalitaBeneficiario
	 * @param deRptEnteBenefProvinciaBeneficiario
	 * @param codRptEnteBenefNazioneBeneficiario
	 * @param dtRptDatiVersDataEsecuzionePagamento
	 * @param numRptDatiVersImportoTotaleDaVersare
	 * @param codRptDatiVersTipoVersamento
	 * @param codRptDatiVersIdUnivocoVersamento
	 * @param codRptDatiVersCodiceContestoPagamento
	 * @param deRptDatiVersIbanAddebito
	 * @param deRptDatiVersBicAddebito
	 * @param codRptDatiVersFirmaRicevuta
	 * @param versamenti
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRptRt insertRpt(final String deRptInviarptPassword, final String codRptInviarptIdPsp, final String codRptInviarptIdIntermediarioPsp,
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
			final String codRptDatiVersFirmaRicevuta, final List<RptRtDettaglioDto> versamenti, final Integer modelloPagamento, final Long mygovRpEId);

	/**
	 * @param codRtDatiPagIdUnivocoVersamento
	 * @param codAckRt
	 * @param codRtInviartEsito
	 * @param codRtInviartFaultCode
	 * @param codRtInviartFaultString
	 * @param codRtInviartId
	 * @param deRtInviartDescription
	 * @param numRtInviartSerial
	 * @param codRtInviartOriginaltFaultCode
	 * @param deRtInviartOriginaltFaultString
	 * @param deRtInviartOriginaltFaultDescription
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRptRt updateRispostaRtById(final Long mygovRptRtId, final String codAckRt, final String codRtInviartEsito, final String codRtInviartFaultCode,
			final String codRtInviartFaultString, final String codRtInviartId, final String deRtInviartDescription, final Integer numRtInviartSerial,
			final String codRtInviartOriginaltFaultCode, final String deRtInviartOriginaltFaultString, final String deRtInviartOriginaltFaultDescription);

	/**
	 * @param codRptDatiVersIdUnivocoVersamento
	 * @param deRptInviarptEsito
	 * @param numRptInviarptRedirect
	 * @param codRptInviarptUrl
	 * @param codRptInviarptFaultCode
	 * @param codRptInviarptFaultString
	 * @param codRptInviarptId
	 * @param deRptInviarptDescription
	 * @param numRptInviarptSerial
	 * @param codRptInviarptOriginaltFaultCode
	 * @param deRptInviarptOriginaltFaultString
	 * @param deRptInviarptOriginaltFaultDescription
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRptRt updateRispostaRptById(final Long mygovRptRtId, final String deRptInviarptEsito, final Integer numRptInviarptRedirect,
			final String codRptInviarptUrl, final String codRptInviarptFaultCode, final String codRptInviarptFaultString, final String codRptInviarptId,
			final String deRptInviarptDescription, final Integer numRptInviarptSerial, final String codRptInviarptOriginaltFaultCode,
			final String deRptInviarptOriginaltFaultString, final String deRptInviarptOriginaltFaultDescription) throws UnsupportedEncodingException, MalformedURLException;

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRptRt updateCodRtDatiPagCodiceEsitoPagamentoByIdSession(final String idSession, final String stato);

	MygovRptRt getRptByRpEId(final Long mygovRpEId);

	MygovRptRt getRptByIdSession(final String nodoSPCFespIdSession);

	MygovRptRt getRptByCodRptIdMessaggioRichiesta(final String riferimentoMessaggioRichiesta);

	List<MygovRptRtDettaglio> getByRptRt(MygovRptRt mygovRptRt) throws DataAccessException;

	/**
	 * @param rt
	 * @return
	 */
	boolean validaUguaglianzaCampiRptRt(CtRicevutaTelematica rt);

	/**
	 * @param mygovRptRtId
	 * @param codStato
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRptRt updateStatoRtById(Long mygovRptRtId, String codStato);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRptRt clearInviarptEsitoById(Long mygovRptRtId);

	/**
	 * @param intervalMinute
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	List<MygovRptRt> findAllRptPendenti(final int modelloPagamento, final int intervalMinute);

	/**
	 * USATO SOLO DA DUMMY
	 * 
	 * @param identificativoDominio
	 * @param identificativoUnivocoVersamento
	 * @param codiceContestoPagamento
	 * @return
	 */
	MygovRptRt getRptByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(String identificativoDominio, String identificativoUnivocoVersamento,
			String codiceContestoPagamento);

	MygovRptRt getRtByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(String identificativoDominio, String identificativoUnivocoVersamento,
			String codiceContestoPagamento);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRptRt updateCarrelloRef(Long mygovRptRtId, MygovCarrelloRpt mygovCarrelloRpt);

}
