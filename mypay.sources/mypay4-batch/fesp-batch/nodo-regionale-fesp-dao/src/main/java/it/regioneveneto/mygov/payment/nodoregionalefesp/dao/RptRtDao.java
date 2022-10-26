/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRpt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;

/**
 * @author regione del veneto
 *
 */
public interface RptRtDao {

	MygovRptRt insertRptWithRefresh(String deRptInviarptPassword, String codRptInviarptIdPsp, String codRptInviarptIdIntermediarioPsp,
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
			Integer modelloPagamento, Long mygovRptEId) throws DataAccessException;

	MygovRptRt updateRispostaRtById(Long mygovRptRtId, String codAckRt, String codRtInviartEsito, String codRtInviartFaultCode, String codRtInviartFaultString,
			String codRtInviartId, String deRtInviartDescription, Integer numRtInviartSerial, String codRtInviartOriginaltFaultCode,
			String deRtInviartOriginaltFaultString, String deRtInviartOriginaltFaultDescription) throws DataAccessException;
	
	List<MygovRptRt> updateRispostaRptByCarrello(MygovCarrelloRpt mygovCarrelloRpt, String deRptInviarptEsito, String codRptInviarptUrl,
			String codRptInviarptFaultCode, String codRptInviarptFaultString, String codRptInviarptId, String deRptInviarptDescription,
			Integer numRptInviarptSerial, String codRptInviarptOriginaltFaultCode, String deRptInviarptOriginaltFaultString,
     		String deRptInviarptOriginaltFaultDescription) throws DataAccessException;

	MygovRptRt updateRispostaRptById(Long mygovRptRtId, String deRptInviarptEsito, Integer numRptInviarptRedirect, String codRptInviarptUrl,
			String codRptInviarptFaultCode, String codRptInviarptFaultString, String codRptInviarptId, String deRptInviarptDescription,
			Integer numRptInviarptSerial, String idSessionSPC , String codRptInviarptOriginaltFaultCode, String deRptInviarptOriginaltFaultString,
     		String deRptInviarptOriginaltFaultDescription) throws DataAccessException;

	MygovRptRt updateRtById(Long mygovRptRtId, String deRtInviartTipoFirma, String codRtInviartIdIntermediarioPa, String codRtInviartIdStazioneIntermediarioPa,
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
			BigDecimal numRtDatiPagImportoTotalePagato, String codRtDatiPagIdUnivocoVersamento, String codRtDatiPagCodiceContestoPagamento, byte[] blbRtPayload)
			throws DataAccessException;

	MygovRptRt updateCodRtDatiPagCodiceEsitoPagamentoByIdSession(final String idSession, final String esito);

	MygovRptRt getRptByMygovRpEId(final Long mygovRpEId);

	MygovRptRt getRptByIdSession(final String nodoSPCFespIdSession);

	MygovRptRt getRptByCodRptIdMessaggioRichiesta(final String riferimentoMessaggioRichiesta);

	MygovRptRt getRptByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(final String idDominio, final String idUnivocoVersamento,
			final String codiceContestoPagamento);

	MygovRptRt getRtByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(final String idDominio, final String idUnivocoVersamento,
			final String codiceContestoPagamento);

	/**
	 * @param mygovRptRtId
	 * @param codStato
	 * @return
	 */
	MygovRptRt updateStatoRtById(Long mygovRptRtId, String codStato);

	MygovRptRt clearInviarptEsitoById(Long mygovRptRtId);

	/**
	 * @param intervalMinute
	 * @return
	 */
	List<MygovRptRt> findAllRptPendenti(final int modelloPagamento, final int intervalMinute) throws DataAccessException;

	/**
	 * @return
	 * @throws DataAccessException
	 */
	List<MygovRptRt> findAllRptAttivate() throws DataAccessException;

	MygovRptRt updateCarrelloRef(Long mygovRptRtId, MygovCarrelloRpt mygovCarrelloRpt);

}
