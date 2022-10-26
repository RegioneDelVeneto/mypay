package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.dao.DataAccessException;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr;

/**
 * 
 * @author regione del veneto
 *
 */
public interface RrErDao {
	
	public MygovRrEr insertRRWithRefresh(String codIdUnivocoVersamento, String codCodiceContestoPagamento,
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
			String codRrDatiRevCodiceContestoPagamento, String codRrDatiRevTipoRevoca)
					throws DataAccessException;
	
	public MygovRrEr updateRispostaRRById(Long mygovRrId, String esito, String faultCode, String faultString,
			String faultId, String description, Integer faultSerial, String originalDescription,
			String originalFaultCode, String originalFaultString) throws DataAccessException;

	public MygovRrEr checkRrByRifMessaggioRevocaDay(String idRiferimentoMessaggioRevoca, Date dtMsgRrStart, Date dtMsgRrFine) throws DataAccessException;
	
	public MygovRrEr getRrByRifMessaggioRevoca(String idRiferimentoMessaggioRevoca, Date dataRiferimentoMessaggioRevoca) throws DataAccessException;

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
			String codErDatiRevCodiceContestoPagamento) throws DataAccessException;

	public MygovRrEr updateRispostaERById(Long mygovErId, String esito, String faultCode, String faultString,
			String faultId, String description, Integer faultSerial, String originalDescription,
			String originalFaultCode, String originalFaultString);

	public MygovRrEr checkRrByDomIuvCont(String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento);
	
	public MygovRrEr checkRevoca(String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento);

}
