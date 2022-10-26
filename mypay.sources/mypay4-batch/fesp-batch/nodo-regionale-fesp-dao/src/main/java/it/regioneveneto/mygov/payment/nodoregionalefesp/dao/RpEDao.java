/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * @author regione del veneto
 *
 */
public interface RpEDao {

	MygovRpE insertRPWithRefresh(String codRpSilinviarpIdPsp, String codRpSilinviarpIdIntermediarioPsp,
			String codRpSilinviarpIdCanale, String codRpSilinviarpIdDominio, String codRpSilinviarpIdUnivocoVersamento,
			String codRpSilinviarpCodiceContestoPagamento, String deRpVersioneOggetto, String codRpDomIdDominio,
			String codRpDomIdStazioneRichiedente, String codRpIdMessaggioRichiesta, Date dtRpDataOraMessaggioRichiesta,
			String codRpAutenticazioneSoggetto, String codRpSoggVersIdUnivVersTipoIdUnivoco,
			String codRpSoggVersIdUnivVersCodiceIdUnivoco, String deRpSoggVersAnagraficaVersante,
			String deRpSoggVersIndirizzoVersante, String deRpSoggVersCivicoVersante, String codRpSoggVersCapVersante,
			String deRpSoggVersLocalitaVersante, String deRpSoggVersProvinciaVersante,
			String codRpSoggVersNazioneVersante, String deRpSoggVersEmailVersante,
			String codRpSoggPagIdUnivPagTipoIdUnivoco, String codRpSoggPagIdUnivPagCodiceIdUnivoco,
			String deRpSoggPagAnagraficaPagatore, String deRpSoggPagIndirizzoPagatore,
			String deRpSoggPagCivicoPagatore, String codRpSoggPagCapPagatore, String deRpSoggPagLocalitaPagatore,
			String deRpSoggPagProvinciaPagatore, String codRpSoggPagNazionePagatore, String deRpSoggPagEmailPagatore,
			Date dtRpDatiVersDataEsecuzionePagamento, BigDecimal numRpDatiVersImportoTotaleDaVersare,
			String codRpDatiVersTipoVersamento, String codRpDatiVersIdUnivocoVersamento,
			String codRpDatiVersCodiceContestoPagamento, String deRpDatiVersIbanAddebito, String deRpDatiVersBicAddebito, Integer modelloPagamento)
			throws DataAccessException;

	MygovRpE updateRispostaEById(Long mygovRpEId, String codAckE,
			String deESilinviaesitoEsito, String codESilinviaesitoFaultCode, String deESilinviaesitoFaultString,
			String codESilinviaesitoId, String deESilinviaesitoDescription, Integer codESilinviaesitoSerial,
			String codESilinviaesitoOriginalFaultCode, String deESilinviaesitoOriginalFaultString, 
			String deESilinviaesitoOriginalFaultDescription)
			throws DataAccessException;

	MygovRpE updateRispostaRpByRpId(Long mygovRpEId,
			String deRpSilinviarpEsito, Integer codRpSilinviarpRedirect, String codRpSilinviarpUrl,
			String codRpSilinviarpFaultCode, String deRpSilinviarpFaultString, String codRpSilinviarpId,
			String deRpSilinviarpDescription, Integer codRpSilinviarpSerial, String idSession,
			String codRpSilinviarpOriginalFaultCode, String deRpSilinviarpOriginalFaultString,
			String deRpSilinviarpOriginalFaultDescription)
			throws DataAccessException;
	
	List<MygovRpE> updateRispostaRpByRpCarrello(final MygovCarrelloRp mygovCarrelloRp, final String deRpSilinviarpEsito, 
			final String codRpSilinviarpUrl, final String codRpSilinviarpFaultCode, final String deRpSilinviarpFaultString, final String codRpSilinviarpId,
			final String deRpSilinviarpDescription, final Integer codRpSilinviarpSerial, final String codRpSilinviarpOriginalFaultCode, 
			final String deRpSilinviarpOriginalFaultString, final String deRpSilinviarpOriginalFaultDescription) throws DataAccessException;

	MygovRpE updateEById(Long mygovRpEId,
			String codESilinviaesitoIdDominio, String codESilinviaesitoIdUnivocoVersamento,
			String codESilinviaesitoCodiceContestoPagamento, String deEVersioneOggetto, String codEDomIdDominio,
			String codEDomIdStazioneRichiedente, String codEIdMessaggioRicevuta, Date dtEDataOraMessaggioRicevuta,
			String codERiferimentoMessaggioRichiesta, Date dtERiferimentoDataRichiesta,
			String codEIstitAttesIdUnivAttesTipoIdUnivoco, String codEIstitAttesIdUnivAttesCodiceIdUnivoco,
			String deEIstitAttesDenominazioneAttestante, String codEIstitAttesCodiceUnitOperAttestante,
			String deEIstitAttesDenomUnitOperAttestante, String deEIstitAttesIndirizzoAttestante,
			String deEIstitAttesCivicoAttestante, String codEIstitAttesCapAttestante,
			String deEIstitAttesLocalitaAttestante, String deEIstitAttesProvinciaAttestante,
			String codEIstitAttesNazioneAttestante, String codEEnteBenefIdUnivBenefTipoIdUnivoco,
			String codEEnteBenefIdUnivBenefCodiceIdUnivoco, String deEEnteBenefDenominazioneBeneficiario,
			String codEEnteBenefCodiceUnitOperBeneficiario, String deEEnteBenefDenomUnitOperBeneficiario,
			String deEEnteBenefIndirizzoBeneficiario, String deEEnteBenefCivicoBeneficiario,
			String codEEnteBenefCapBeneficiario, String deEEnteBenefLocalitaBeneficiario,
			String deEEnteBenefProvinciaBeneficiario, String codEEnteBenefNazioneBeneficiario,
			String codESoggVersIdUnivVersTipoIdUnivoco, String codESoggVersIdUnivVersCodiceIdUnivoco,
			String deESoggVersAnagraficaVersante, String deESoggVersIndirizzoVersante,
			String deESoggVersCivicoVersante, String codESoggVersCapVersante, String deESoggVersLocalitaVersante,
			String deESoggVersProvinciaVersante, String codESoggVersNazioneVersante, String deESoggVersEmailVersante,
			String codESoggPagIdUnivPagTipoIdUnivoco, String codESoggPagIdUnivPagCodiceIdUnivoco,
			String deESoggPagAnagraficaPagatore, String deESoggPagIndirizzoPagatore, String deESoggPagCivicoPagatore,
			String codESoggPagCapPagatore, String deESoggPagLocalitaPagatore, String deESoggPagProvinciaPagatore,
			String codESoggPagNazionePagatore, String deESoggPagEmailPagatore, String codEDatiPagCodiceEsitoPagamento,
			BigDecimal numEDatiPagImportoTotalePagato, String codEDatiPagIdUnivocoVersamento,
			String codEDatiPagCodiceContestoPagamento) throws DataAccessException;

	MygovRpE updateECodEsitoPagamentoById(long rpEId, String esito) throws DataAccessException;

	/**
	 * @param idSession
	 * @return
	 */
	MygovRpE getRpByIdSession(String idSession);

	void deleteByIdSession(String idSession);
	
	MygovRpE doGet(final long id);

	MygovRpE updateCarrelloRef(Long mygovRpEId, MygovCarrelloRp mygovCarrelloRp);

}
