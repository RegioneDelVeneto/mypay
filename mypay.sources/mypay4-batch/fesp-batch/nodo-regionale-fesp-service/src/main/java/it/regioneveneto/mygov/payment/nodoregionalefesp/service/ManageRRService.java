package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RrErDettaglioDto;

/**
 * 
 * @author regione del veneto
 *
 */

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface ManageRRService {
	
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
	 * @param rrEDettaglioPagamentis
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRrEr insertRRWithRefresh(final String codIdUnivocoVersamento, final String codCodiceContestoPagamento, final String codRrDomIdDominio,
		     final String codRrDomIdStazioneRichiedente, final String codRrIdMessaggioRevoca, final String deRrVersioneOggetto, final Date dtRrDataOraMessaggioRevoca,
		     final String deRrIstitAttDenominazioneMittente, final String codRrIstitAttUnitOperMittente, final String deRrIstitAttDenomUnitOperMittente,
		     final String deRrIstitAttIndirizzoMittente, final String deRrIstitAttCivicoMittente, final String codRrIstitAttCapMittente,
		     final String deRrIstitAttLocalitaMittente, final String deRrIstitAttProvinciaMittente, final String codRrIstitAttNazioneMittente,
		     final String codRrIstitAttIdUnivMittTipoIdUnivoco, final String codRrIstitAttIdUnivMittCodiceIdUnivoco, final String codRrSoggVersIdUnivVersTipoIdUnivoco,
		     final String codRrSoggVersIdUnivVersCodiceIdUnivoco, final String codRrSoggVersAnagraficaVersante, final String deRrSoggVersIndirizzoVersante,
		     final String deRrSoggVersCivicoVersante, final String codRrSoggVersCapVersante, final String deRrSoggVersLocalitaVersante,
		     final String deRrSoggVersProvinciaVersante, final String codRrSoggVersNazioneVersante, final String deRrSoggVersEmailVersante,
		     final String codRrSoggPagIdUnivPagTipoIdUnivoco, final String codRrSoggPagIdUnivPagCodiceIdUnivoco, final String codRrSoggPagAnagraficaPagatore,
		     final String deRrSoggPagIndirizzoPagatore, final String deRrSoggPagCivicoPagatore, final String codRrSoggPagCapPagatore,
		     final String deRrSoggPagLocalitaPagatore, final String deRrSoggPagProvinciaPagatore, final String codRrSoggPagNazionePagatore,
		     final String deRrSoggPagEmailPagatore, final BigDecimal numRrDatiRevImportoTotaleRevocato, final String codRrDatiRevIdUnivocoVersamento,
		     final String codRrDatiRevCodiceContestoPagamento, final String codRrDatiRevTipoRevoca, final List<RrErDettaglioDto> rrEDettaglioPagamentis
			);
	
	/**
	 * 
	 * @param mygovRrId
	 * @param esito
	 * @param faultCode
	 * @param faultString
	 * @param faultId
	 * @param description
	 * @param faultSerial
	 * @param originalDescription
	 * @param originalFaultCode
	 * @param originalFaultString
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRrEr updateRispostaRRById(final Long mygovRrId, final String esito, final String faultCode, final String faultString, 
			final String faultId, final String description, final Integer faultSerial, final String originalDescription, 
			final String originalFaultCode, final String originalFaultString);

	/**
	 * 
	 * @param idRiferimentoMessaggioRevoca
	 * @param dtMsgRrStart
	 * @param dtMsgRrFine 
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	MygovRrEr checkRrByRifMessaggioRevocaDay(final String idRiferimentoMessaggioRevoca, final Date dtMsgRrStart, final Date dtMsgRrFine);

	/**
	 * 
	 * @param idRevoca
	 * @param identificativoDominio
	 * @param identificativoUnivocoVersamento
	 * @param codiceContestoPagamento
	 * @param deErVersioneOggetto
	 * @param codErDomIdDominio
	 * @param codErDomIdStazioneRichiedente
	 * @param codErIdMessaggioEsito
	 * @param dtErDataOraMessaggioEsito
	 * @param codErRiferimentoMessaggioRevoca
	 * @param dtErRiferimentoDataRevoca
	 * @param deErIstitAttDenominazioneMittente
	 * @param codErIstitAttUnitOperMittente
	 * @param deErIstitAttDenomUnitOperMittente
	 * @param deErIstitAttIndirizzoMittente
	 * @param deErIstitAttCivicoMittente
	 * @param codErIstitAttCapMittente
	 * @param deErIstitAttLocalitaMittente
	 * @param deErIstitAttProvinciaMittente
	 * @param codErIstitAttNazioneMittente
	 * @param codErIstitAttIdUnivMittTipoIdUnivoco
	 * @param codErIstitAttIdUnivMittCodiceIdUnivoco
	 * @param codErSoggVersIdUnivVersTipoIdUnivoco
	 * @param codErSoggVersIdUnivVersCodiceIdUnivoco
	 * @param codErSoggVersAnagraficaVersante
	 * @param deErSoggVersIndirizzoVersante
	 * @param deErSoggVersCivicoVersante
	 * @param codErSoggVersCapVersante
	 * @param deErSoggVersLocalitaVersante
	 * @param deErSoggVersProvinciaVersante
	 * @param codErSoggVersNazioneVersante
	 * @param deErSoggVersEmailVersante
	 * @param codErSoggPagIdUnivPagTipoIdUnivoco
	 * @param codErSoggPagIdUnivPagCodiceIdUnivoco
	 * @param codErSoggPagAnagraficaPagatore
	 * @param deErSoggPagIndirizzoPagatore
	 * @param deErSoggPagCivicoPagatore
	 * @param codErSoggPagCapPagatore
	 * @param deErSoggPagLocalitaPagatore
	 * @param deErSoggPagProvinciaPagatore
	 * @param codErSoggPagNazionePagatore
	 * @param deErSoggPagEmailPagatore
	 * @param numErDatiRevImportoTotaleRevocato
	 * @param codErDatiRevIdUnivocoVersamento
	 * @param codErDatiRevCodiceContestoPagamento
	 * @param esitoRevocaPagamentiList
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRrEr updateERById(final Long idRevoca, final String identificativoDominio, final String identificativoUnivocoVersamento,
			final String codiceContestoPagamento, final String deErVersioneOggetto, final String codErDomIdDominio,
			final String codErDomIdStazioneRichiedente, final String codErIdMessaggioEsito, final Date dtErDataOraMessaggioEsito,
			final String codErRiferimentoMessaggioRevoca, final Date dtErRiferimentoDataRevoca,
			final String deErIstitAttDenominazioneMittente, final String codErIstitAttUnitOperMittente,
			final String deErIstitAttDenomUnitOperMittente, final String deErIstitAttIndirizzoMittente,
			final String deErIstitAttCivicoMittente, final String codErIstitAttCapMittente, final String deErIstitAttLocalitaMittente,
			final String deErIstitAttProvinciaMittente, final String codErIstitAttNazioneMittente,
			final String codErIstitAttIdUnivMittTipoIdUnivoco, final String codErIstitAttIdUnivMittCodiceIdUnivoco,
			final String codErSoggVersIdUnivVersTipoIdUnivoco, final String codErSoggVersIdUnivVersCodiceIdUnivoco,
			final String codErSoggVersAnagraficaVersante, final String deErSoggVersIndirizzoVersante,
			final String deErSoggVersCivicoVersante, final String codErSoggVersCapVersante, final String deErSoggVersLocalitaVersante,
			final String deErSoggVersProvinciaVersante, final String codErSoggVersNazioneVersante, final String deErSoggVersEmailVersante,
			final String codErSoggPagIdUnivPagTipoIdUnivoco, final String codErSoggPagIdUnivPagCodiceIdUnivoco,
			final String codErSoggPagAnagraficaPagatore, final String deErSoggPagIndirizzoPagatore,
			final String deErSoggPagCivicoPagatore, final String codErSoggPagCapPagatore, final String deErSoggPagLocalitaPagatore,
			final String deErSoggPagProvinciaPagatore, final String codErSoggPagNazionePagatore, final String deErSoggPagEmailPagatore,
			final BigDecimal numErDatiRevImportoTotaleRevocato, final String codErDatiRevIdUnivocoVersamento,
			final String codErDatiRevCodiceContestoPagamento, final List<RrErDettaglioDto> esitoRevocaPagamentiList);

	/**
	 * 
	 * @param mygovErId
	 * @param esito
	 * @param faultCode
	 * @param faultString
	 * @param faultId
	 * @param description
	 * @param faultSerial
	 * @param originalDescription
	 * @param originalFaultCode
	 * @param originalFaultString
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovRrEr updateRispostaERById(final Long mygovErId, final String esito, final String faultCode, final String faultString, 
			final String faultId, final String description, final Integer faultSerial, final String originalDescription, 
			final String originalFaultCode, final String originalFaultString);

	/**
	 * 
	 * @param idRiferimentoMessaggioRevoca
	 * @param dataRiferimentoMessaggioRevoca
	 * @return
	 */
	MygovRrEr getRrByRifMessaggioRevoca(String idRiferimentoMessaggioRevoca, Date dataRiferimentoMessaggioRevoca);

	/**
	 * 
	 * @param identificativoDominio
	 * @param identificativoUnivocoVersamento
	 * @param codiceContestoPagamento
	 * @return
	 */
	MygovRrEr checkRrByDomIuvCont(String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento);

	
	MygovRrEr checkRevoca(String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento);

}
