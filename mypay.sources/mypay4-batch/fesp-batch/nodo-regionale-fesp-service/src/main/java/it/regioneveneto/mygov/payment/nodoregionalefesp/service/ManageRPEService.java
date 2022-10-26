/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RpEDettaglioDto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author regione del veneto
 *
 */

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface ManageRPEService {

	/**
	 * @param codRpSilinviarpIdPsp
	 * @param codRpSilinviarpIdIntermediarioPsp
	 * @param codRpSilinviarpIdCanale
	 * @param codRpSilinviarpIdDominio
	 * @param codRpSilinviarpIdUnivocoVersamento
	 * @param codRpSilinviarpCodiceContestoPagamento
	 * @param deRpVersioneOggetto
	 * @param codRpDomIdDominio
	 * @param codRpDomIdStazioneRichiedente
	 * @param codRpIdMessaggioRichiesta
	 * @param dtRpDataOraMessaggioRichiesta
	 * @param codRpAutenticazioneSoggetto
	 * @param codRpSoggVersIdUnivVersTipoIdUnivoco
	 * @param codRpSoggVersIdUnivVersCodiceIdUnivoco
	 * @param deRpSoggVersAnagraficaVersante
	 * @param deRpSoggVersIndirizzoVersante
	 * @param deRpSoggVersCivicoVersante
	 * @param codRpSoggVersCapVersante
	 * @param deRpSoggVersLocalitaVersante
	 * @param deRpSoggVersProvinciaVersante
	 * @param codRpSoggVersNazioneVersante
	 * @param deRpSoggVersEmailVersante
	 * @param codRpSoggPagIdUnivPagTipoIdUnivoco
	 * @param codRpSoggPagIdUnivPagCodiceIdUnivoco
	 * @param deRpSoggPagAnagraficaPagatore
	 * @param deRpSoggPagIndirizzoPagatore
	 * @param deRpSoggPagCivicoPagatore
	 * @param codRpSoggPagCapPagatore
	 * @param deRpSoggPagLocalitaPagatore
	 * @param deRpSoggPagProvinciaPagatore
	 * @param codRpSoggPagNazionePagatore
	 * @param deRpSoggPagEmailPagatore
	 * @param dtRpDatiVersDataEsecuzionePagamento
	 * @param numRpDatiVersImportoTotaleDaVersare
	 * @param codRpDatiVersTipoVersamento
	 * @param codRpDatiVersIdUnivocoVersamento
	 * @param codRpDatiVersCodiceContestoPagamento
	 * @param deRpDatiVersIbanAddebito
	 * @param deRpDatiVersBicAddebito
	 * @param rpEDettaglios
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRpE insertRPWithRefresh(final String codRpSilinviarpIdPsp, final String codRpSilinviarpIdIntermediarioPsp,
			final String codRpSilinviarpIdCanale, final String codRpSilinviarpIdDominio,
			final String codRpSilinviarpIdUnivocoVersamento, final String codRpSilinviarpCodiceContestoPagamento,
			final String deRpVersioneOggetto, final String codRpDomIdDominio,
			final String codRpDomIdStazioneRichiedente, final String codRpIdMessaggioRichiesta,
			final Date dtRpDataOraMessaggioRichiesta, final String codRpAutenticazioneSoggetto,
			final String codRpSoggVersIdUnivVersTipoIdUnivoco, final String codRpSoggVersIdUnivVersCodiceIdUnivoco,
			final String deRpSoggVersAnagraficaVersante, final String deRpSoggVersIndirizzoVersante,
			final String deRpSoggVersCivicoVersante, final String codRpSoggVersCapVersante,
			final String deRpSoggVersLocalitaVersante, final String deRpSoggVersProvinciaVersante,
			final String codRpSoggVersNazioneVersante, final String deRpSoggVersEmailVersante,
			final String codRpSoggPagIdUnivPagTipoIdUnivoco, final String codRpSoggPagIdUnivPagCodiceIdUnivoco,
			final String deRpSoggPagAnagraficaPagatore, final String deRpSoggPagIndirizzoPagatore,
			final String deRpSoggPagCivicoPagatore, final String codRpSoggPagCapPagatore,
			final String deRpSoggPagLocalitaPagatore, final String deRpSoggPagProvinciaPagatore,
			final String codRpSoggPagNazionePagatore, final String deRpSoggPagEmailPagatore,
			final Date dtRpDatiVersDataEsecuzionePagamento, final BigDecimal numRpDatiVersImportoTotaleDaVersare,
			final String codRpDatiVersTipoVersamento, final String codRpDatiVersIdUnivocoVersamento,
			final String codRpDatiVersCodiceContestoPagamento, final String deRpDatiVersIbanAddebito,
			final String deRpDatiVersBicAddebito, final Integer modelloPagamento, final List<RpEDettaglioDto> rpEDettaglios);

	/**
	 * @param codRpDatiVersIdUnivocoVersamento
	 * @param codESilinviaesitoIdDominio
	 * @param codESilinviaesitoIdUnivocoVersamento
	 * @param codESilinviaesitoCodiceContestoPagamento
	 * @param deEVersioneOggetto
	 * @param codEDomIdDominio
	 * @param codEDomIdStazioneRichiedente
	 * @param codEIdMessaggioRicevuta
	 * @param dtEDataOraMessaggioRicevuta
	 * @param codERiferimentoMessaggioRichiesta
	 * @param dtERiferimentoDataRichiesta
	 * @param codEIstitAttesIdUnivAttesTipoIdUnivoco
	 * @param codEIstitAttesIdUnivAttesCodiceIdUnivoco
	 * @param deEIstitAttesDenominazioneAttestante
	 * @param codEIstitAttesCodiceUnitOperAttestante
	 * @param deEIstitAttesDenomUnitOperAttestante
	 * @param deEIstitAttesIndirizzoAttestante
	 * @param deEIstitAttesCivicoAttestante
	 * @param codEIstitAttesCapAttestante
	 * @param deEIstitAttesLocalitaAttestante
	 * @param deEIstitAttesProvinciaAttestante
	 * @param codEIstitAttesNazioneAttestante
	 * @param codEEnteBenefIdUnivBenefTipoIdUnivoco
	 * @param codEEnteBenefIdUnivBenefCodiceIdUnivoco
	 * @param deEEnteBenefDenominazioneBeneficiario
	 * @param codEEnteBenefCodiceUnitOperBeneficiario
	 * @param deEEnteBenefDenomUnitOperBeneficiario
	 * @param deEEnteBenefIndirizzoBeneficiario
	 * @param deEEnteBenefCivicoBeneficiario
	 * @param codEEnteBenefCapBeneficiario
	 * @param deEEnteBenefLocalitaBeneficiario
	 * @param deEEnteBenefProvinciaBeneficiario
	 * @param codEEnteBenefNazioneBeneficiario
	 * @param codESoggVersIdUnivVersTipoIdUnivoco
	 * @param codESoggVersIdUnivVersCodiceIdUnivoco
	 * @param deESoggVersAnagraficaVersante
	 * @param deESoggVersIndirizzoVersante
	 * @param deESoggVersCivicoVersante
	 * @param codESoggVersCapVersante
	 * @param deESoggVersLocalitaVersante
	 * @param deESoggVersProvinciaVersante
	 * @param codESoggVersNazioneVersante
	 * @param deESoggVersEmailVersante
	 * @param codESoggPagIdUnivPagTipoIdUnivoco
	 * @param codESoggPagIdUnivPagCodiceIdUnivoco
	 * @param deESoggPagAnagraficaPagatore
	 * @param deESoggPagIndirizzoPagatore
	 * @param deESoggPagCivicoPagatore
	 * @param codESoggPagCapPagatore
	 * @param deESoggPagLocalitaPagatore
	 * @param deESoggPagProvinciaPagatore
	 * @param codESoggPagNazionePagatore
	 * @param deESoggPagEmailPagatore
	 * @param codEDatiPagCodiceEsitoPagamento
	 * @param numEDatiPagImportoTotalePagato
	 * @param codEDatiPagIdUnivocoVersamento
	 * @param codEDatiPagCodiceContestoPagamento
	 * @param rpEDettaglios
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	void updateEById(Long mygovRpEId,
			final String codESilinviaesitoIdDominio, final String codESilinviaesitoIdUnivocoVersamento,
			final String codESilinviaesitoCodiceContestoPagamento, final String deEVersioneOggetto,
			final String codEDomIdDominio, final String codEDomIdStazioneRichiedente,
			final String codEIdMessaggioRicevuta, final Date dtEDataOraMessaggioRicevuta,
			final String codERiferimentoMessaggioRichiesta, final Date dtERiferimentoDataRichiesta,
			final String codEIstitAttesIdUnivAttesTipoIdUnivoco, final String codEIstitAttesIdUnivAttesCodiceIdUnivoco,
			final String deEIstitAttesDenominazioneAttestante, final String codEIstitAttesCodiceUnitOperAttestante,
			final String deEIstitAttesDenomUnitOperAttestante, final String deEIstitAttesIndirizzoAttestante,
			final String deEIstitAttesCivicoAttestante, final String codEIstitAttesCapAttestante,
			final String deEIstitAttesLocalitaAttestante, final String deEIstitAttesProvinciaAttestante,
			final String codEIstitAttesNazioneAttestante, final String codEEnteBenefIdUnivBenefTipoIdUnivoco,
			final String codEEnteBenefIdUnivBenefCodiceIdUnivoco, final String deEEnteBenefDenominazioneBeneficiario,
			final String codEEnteBenefCodiceUnitOperBeneficiario, final String deEEnteBenefDenomUnitOperBeneficiario,
			final String deEEnteBenefIndirizzoBeneficiario, final String deEEnteBenefCivicoBeneficiario,
			final String codEEnteBenefCapBeneficiario, final String deEEnteBenefLocalitaBeneficiario,
			final String deEEnteBenefProvinciaBeneficiario, final String codEEnteBenefNazioneBeneficiario,
			final String codESoggVersIdUnivVersTipoIdUnivoco, final String codESoggVersIdUnivVersCodiceIdUnivoco,
			final String deESoggVersAnagraficaVersante, final String deESoggVersIndirizzoVersante,
			final String deESoggVersCivicoVersante, final String codESoggVersCapVersante,
			final String deESoggVersLocalitaVersante, final String deESoggVersProvinciaVersante,
			final String codESoggVersNazioneVersante, final String deESoggVersEmailVersante,
			final String codESoggPagIdUnivPagTipoIdUnivoco, final String codESoggPagIdUnivPagCodiceIdUnivoco,
			final String deESoggPagAnagraficaPagatore, final String deESoggPagIndirizzoPagatore,
			final String deESoggPagCivicoPagatore, final String codESoggPagCapPagatore,
			final String deESoggPagLocalitaPagatore, final String deESoggPagProvinciaPagatore,
			final String codESoggPagNazionePagatore, final String deESoggPagEmailPagatore,
			final String codEDatiPagCodiceEsitoPagamento, final BigDecimal numEDatiPagImportoTotalePagato,
			final String codEDatiPagIdUnivocoVersamento, final String codEDatiPagCodiceContestoPagamento,
			final List<RpEDettaglioDto> rpEDettaglios);

	/**
	 * @param codEDatiPagIdUnivocoVersamento
	 * @param codAckE
	 * @param deESilinviaesitoEsito
	 * @param codESilinviaesitoFaultCode
	 * @param deESilinviaesitoFaultString
	 * @param codESilinviaesitoId
	 * @param deESilinviaesitoDescription
	 * @param codESilinviaesitoSerial
	 * @param codESilinviaesitoOriginalFaultCode
	 * @param deESilinviaesitoOriginalFaultString
	 * @param deESilinviaesitoOriginalFaultDescription
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRpE updateRispostaEById(final Long mygovRpEId,
			final String codAckE, final String deESilinviaesitoEsito, final String codESilinviaesitoFaultCode,
			final String deESilinviaesitoFaultString, final String codESilinviaesitoId,
			final String deESilinviaesitoDescription, final Integer codESilinviaesitoSerial,
			final String codESilinviaesitoOriginalFaultCode, final String deESilinviaesitoOriginalFaultString, 
			final String deESilinviaesitoOriginalFaultDescription);
	
	/**
	 * @param mygovRpEId
	 * @param deRpSilinviarpEsito
	 * @param codRpSilinviarpRedirect
	 * @param codRpSilinviarpUrl
	 * @param codRpSilinviarpFaultCode
	 * @param deRpSilinviarpFaultString
	 * @param codRpSilinviarpId
	 * @param deRpSilinviarpDescription
	 * @param codRpSilinviarpSerial
	 * @param idSession
	 * @param codRpSilinviarpOriginalFaultCode
	 * @param deRpSilinviarpOriginalFaultString
	 * @param deRpSilinviarpOriginalFaultDescription
	 * @return
	 */
	List<MygovRpE> updateRispostaRpByCarrello(final MygovCarrelloRp mygovCarrelloRp, final String deRpSilinviarpEsito,
			final String codRpSilinviarpUrl, final String codRpSilinviarpFaultCode, final String deRpSilinviarpFaultString, final String codRpSilinviarpId,
			final String deRpSilinviarpDescription, final Integer codRpSilinviarpSerial, final String codRpSilinviarpOriginalFaultCode, 
			final String deRpSilinviarpOriginalFaultString, final String deRpSilinviarpOriginalFaultDescription);

	/**
	 * @param codRpDatiVersIdUnivocoVersamento
	 * @param deRpSilinviarpEsito
	 * @param codRpSilinviarpRedirect
	 * @param codRpSilinviarpUrl
	 * @param codRpSilinviarpFaultCode
	 * @param deRpSilinviarpFaultString
	 * @param codRpSilinviarpId
	 * @param deRpSilinviarpDescription
	 * @param codRpSilinviarpSerial
	 * @param codRpSilinviarpOriginalFaultCode
	 * @param deRpSilinviarpOriginalFaultString
	 * @param deRpSilinviarpOriginalFaultDescription
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRpE updateRispostaRpById(final Long mygovRpEId,
			final String deRpSilinviarpEsito, final Integer codRpSilinviarpRedirect, final String codRpSilinviarpUrl,
			final String codRpSilinviarpFaultCode, final String deRpSilinviarpFaultString,
			final String codRpSilinviarpId, final String deRpSilinviarpDescription,
			final Integer codRpSilinviarpSerial, final String idSession, final String codRpSilinviarpOriginalFaultCode, 
			final String deRpSilinviarpOriginalFaultString, final String deRpSilinviarpOriginalFaultDescription);

	/**
	 * @param idSession
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRpE getRpByIdSession(final String idSession);
	
	
	/**
	 * @param codRpDomIdDominio
	 * @param codRpDatiVersIdUnivocoVersamento
	 * @param esito
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRpE updateECodEsitoByRpEId(final long rpEId, final String esito);

	/**
	 * @param nodoRegionaleFespIdSession
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	void deleteByIdSession(String nodoRegionaleFespIdSession);

	/**
	 * @param mygovRpEId
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	MygovRpE getRpByRpEId(final long mygovRpEId);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRpE updateCarrelloRef(Long mygovRpEId, MygovCarrelloRp mygovCarrelloRp);

}
