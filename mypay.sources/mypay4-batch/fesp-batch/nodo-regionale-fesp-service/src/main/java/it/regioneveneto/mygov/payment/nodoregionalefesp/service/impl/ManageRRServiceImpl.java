package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RrErDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrErDettaglio;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RrErDettaglioDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService;

/**
 * 
 * @author regione del veneto
 *
 */
public class ManageRRServiceImpl implements ManageRRService{
	
	private static final Log log = LogFactory.getLog(ManageRRServiceImpl.class);
	
	
	private RrErDao rrErDao;
	
	private RrErDettaglioDao rrErDettaglioDao;


	/**
	 * @param rrErDao the rrErDao to set
	 */
	public void setRrErDao(RrErDao rrErDao) {
		this.rrErDao = rrErDao;
	}


	/**
	 * @param rrErDettaglioDao the rrErDettaglioDao to set
	 */
	public void setRrErDettaglioDao(RrErDettaglioDao rrErDettaglioDao) {
		this.rrErDettaglioDao = rrErDettaglioDao;
	}


	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService#insertRRWithRefresh(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String, java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	@Transactional
	public MygovRrEr insertRRWithRefresh(final String codIdUnivocoVersamento, final String codCodiceContestoPagamento,
			final String codRrDomIdDominio, final String codRrDomIdStazioneRichiedente, final String codRrIdMessaggioRevoca,
			final String deRrVersioneOggetto, final Date dtRrDataOraMessaggioRevoca, final String deRrIstitAttDenominazioneMittente,
			final String codRrIstitAttUnitOperMittente, final String deRrIstitAttDenomUnitOperMittente,
			final String deRrIstitAttIndirizzoMittente, final String deRrIstitAttCivicoMittente, final String codRrIstitAttCapMittente,
			final String deRrIstitAttLocalitaMittente, final String deRrIstitAttProvinciaMittente,
			final String codRrIstitAttNazioneMittente, final String codRrIstitAttIdUnivMittTipoIdUnivoco,
			final String codRrIstitAttIdUnivMittCodiceIdUnivoco, final String codRrSoggVersIdUnivVersTipoIdUnivoco,
			final String codRrSoggVersIdUnivVersCodiceIdUnivoco, final String codRrSoggVersAnagraficaVersante,
			final String deRrSoggVersIndirizzoVersante, final String deRrSoggVersCivicoVersante, final String codRrSoggVersCapVersante,
			final String deRrSoggVersLocalitaVersante, final String deRrSoggVersProvinciaVersante,
			final String codRrSoggVersNazioneVersante, final String deRrSoggVersEmailVersante,
			final String codRrSoggPagIdUnivPagTipoIdUnivoco, final String codRrSoggPagIdUnivPagCodiceIdUnivoco,
			final String codRrSoggPagAnagraficaPagatore, final String deRrSoggPagIndirizzoPagatore,
			final String deRrSoggPagCivicoPagatore, final String codRrSoggPagCapPagatore, final String deRrSoggPagLocalitaPagatore,
			final String deRrSoggPagProvinciaPagatore, final String codRrSoggPagNazionePagatore, final String deRrSoggPagEmailPagatore,
			final BigDecimal numRrDatiRevImportoTotaleRevocato, final String codRrDatiRevIdUnivocoVersamento,
			final String codRrDatiRevCodiceContestoPagamento, final String codRrDatiRevTipoRevoca,
			final List<RrErDettaglioDto> rrEDettaglioPagamentis) {
		
		log.debug("Invocato insertRRWithRefresh");
		
		MygovRrEr rr = rrErDao.insertRRWithRefresh(codIdUnivocoVersamento, codCodiceContestoPagamento, 
				codRrDomIdDominio, codRrDomIdStazioneRichiedente, codRrIdMessaggioRevoca, deRrVersioneOggetto, dtRrDataOraMessaggioRevoca, 
				deRrIstitAttDenominazioneMittente, codRrIstitAttUnitOperMittente, deRrIstitAttDenomUnitOperMittente, 
				deRrIstitAttIndirizzoMittente, deRrIstitAttCivicoMittente, codRrIstitAttCapMittente, deRrIstitAttLocalitaMittente, 
				deRrIstitAttProvinciaMittente, codRrIstitAttNazioneMittente, codRrIstitAttIdUnivMittTipoIdUnivoco, 
				codRrIstitAttIdUnivMittCodiceIdUnivoco, codRrSoggVersIdUnivVersTipoIdUnivoco, codRrSoggVersIdUnivVersCodiceIdUnivoco, 
				codRrSoggVersAnagraficaVersante, deRrSoggVersIndirizzoVersante, deRrSoggVersCivicoVersante, 
				codRrSoggVersCapVersante, deRrSoggVersLocalitaVersante, deRrSoggVersProvinciaVersante, codRrSoggVersNazioneVersante, 
				deRrSoggVersEmailVersante, codRrSoggPagIdUnivPagTipoIdUnivoco, codRrSoggPagIdUnivPagCodiceIdUnivoco, 
				codRrSoggPagAnagraficaPagatore, deRrSoggPagIndirizzoPagatore, deRrSoggPagCivicoPagatore, codRrSoggPagCapPagatore, 
				deRrSoggPagLocalitaPagatore, deRrSoggPagProvinciaPagatore, codRrSoggPagNazionePagatore, deRrSoggPagEmailPagatore, 
				numRrDatiRevImportoTotaleRevocato, codRrDatiRevIdUnivocoVersamento, codRrDatiRevCodiceContestoPagamento, 
				codRrDatiRevTipoRevoca);
		
		log.debug("insertRRWithRefresh leggo e carico dettagli");
		
		for(RrErDettaglioDto dettDto : rrEDettaglioPagamentis){
			rrErDettaglioDao.insert(rr, dettDto.getCodRrDatiSingRevIdUnivocoRiscossione(), dettDto.getDeRrDatiSingRevCausaleRevoca(),
					dettDto.getDeRrDatiSingRevDatiAggiuntiviRevoca(), dettDto.getNumRrDatiSingRevSingoloImportoRevocato());
		}
		
		log.debug("Fine insertRRWithRefresh");
		
		return rr;
	}


	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService#updateRispostaRRById(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovRrEr updateRispostaRRById(final Long mygovRrId, final String esito, final String faultCode, final String faultString,
			final String faultId, final String description, final Integer faultSerial, final String originalDescription,
			final String originalFaultCode, final String originalFaultString) {
		
		log.debug("Invocato updateRispostaRRById con: mygovRrId = [" + mygovRrId + "] ");
		
		return rrErDao.updateRispostaRRById(mygovRrId, esito, faultCode, faultString, faultId, description, faultSerial, 
				originalDescription, originalFaultCode, originalFaultString);
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService#getRrByIdMessaggioRevoca(java.lang.String)
	 */
	@Override
	public MygovRrEr checkRrByRifMessaggioRevocaDay(final String idRiferimentoMessaggioRevoca, final Date dtMsgRrStart, final Date dtMsgRrFine) {

		log.debug("Invocato getRrRifMessaggioRevoca con: idRiferimentoMessaggioRevoca = [" + idRiferimentoMessaggioRevoca + "] , "
				+ "dtMsgRrStart = [" + dtMsgRrStart + "] , dtMsgRrFine = [" + dtMsgRrFine + "]");
		
		return rrErDao.checkRrByRifMessaggioRevocaDay(idRiferimentoMessaggioRevoca, dtMsgRrStart, dtMsgRrFine);
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService#updateERById(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovRrEr updateERById(final Long idRevoca, final String identificativoDominio, final String identificativoUnivocoVersamento,
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
			final String codErDatiRevCodiceContestoPagamento, final List<RrErDettaglioDto> esitoRevocaPagamentiList) {

		log.debug("Invocato updateERById con: idRevoca = [" + idRevoca + "]");
		
		MygovRrEr mygovRevoca = rrErDao.updateERById(idRevoca, identificativoDominio, identificativoUnivocoVersamento,
				codiceContestoPagamento, deErVersioneOggetto, codErDomIdDominio,
				codErDomIdStazioneRichiedente, codErIdMessaggioEsito, dtErDataOraMessaggioEsito,
				codErRiferimentoMessaggioRevoca, dtErRiferimentoDataRevoca,
				deErIstitAttDenominazioneMittente, codErIstitAttUnitOperMittente,
				deErIstitAttDenomUnitOperMittente, deErIstitAttIndirizzoMittente,
				deErIstitAttCivicoMittente, codErIstitAttCapMittente, deErIstitAttLocalitaMittente,
				deErIstitAttProvinciaMittente, codErIstitAttNazioneMittente,
				codErIstitAttIdUnivMittTipoIdUnivoco, codErIstitAttIdUnivMittCodiceIdUnivoco,
				codErSoggVersIdUnivVersTipoIdUnivoco, codErSoggVersIdUnivVersCodiceIdUnivoco,
				codErSoggVersAnagraficaVersante, deErSoggVersIndirizzoVersante,
				deErSoggVersCivicoVersante, codErSoggVersCapVersante, deErSoggVersLocalitaVersante,
				deErSoggVersProvinciaVersante, codErSoggVersNazioneVersante, deErSoggVersEmailVersante,
				codErSoggPagIdUnivPagTipoIdUnivoco, codErSoggPagIdUnivPagCodiceIdUnivoco,
				codErSoggPagAnagraficaPagatore, deErSoggPagIndirizzoPagatore,
				deErSoggPagCivicoPagatore, codErSoggPagCapPagatore, deErSoggPagLocalitaPagatore,
				deErSoggPagProvinciaPagatore, codErSoggPagNazionePagatore, deErSoggPagEmailPagatore,
				numErDatiRevImportoTotaleRevocato, codErDatiRevIdUnivocoVersamento,
				codErDatiRevCodiceContestoPagamento);
		
		log.debug("Carico Dettagli");
		
		if(esitoRevocaPagamentiList.size() > 0){
			List<MygovRrErDettaglio> listDett = rrErDettaglioDao.getByRevoca(mygovRevoca);
			Iterator<RrErDettaglioDto> listDettagliDaAggiornare = esitoRevocaPagamentiList.iterator();
			for (MygovRrErDettaglio dett : listDett){
				
				RrErDettaglioDto dettaglioDaAggiornare = listDettagliDaAggiornare.next();
				
				rrErDettaglioDao.updateEsitoDettaglioRevoca(dett.getMygovRrErDettaglioId(), dett.getVersion(), mygovRevoca,
						dettaglioDaAggiornare.getCodErDatiSingRevIdUnivocoRiscossione(),
						dettaglioDaAggiornare.getDeErDatiSingRevCausaleRevoca(),
						dettaglioDaAggiornare.getDeErDatiSingRevDatiAggiuntiviRevoca(),
						dettaglioDaAggiornare.getNumErDatiSingRevSingoloImportoRevocato());
			}
		}
		
		log.debug("Fine updateERById");
		
		return mygovRevoca;
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService#updateRispostaERById(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovRrEr updateRispostaERById(final Long mygovErId, final String esito, final String faultCode, final String faultString, 
			final String faultId, final String description, final Integer faultSerial, final String originalDescription, 
			final String originalFaultCode, final String originalFaultString) {
		
		log.debug("Invocato updateRispostaRRById con: mygovRrId = [" + mygovErId + "] ");
		
		return rrErDao.updateRispostaERById(mygovErId, esito, faultCode, faultString, faultId, description, faultSerial, 
				originalDescription, originalFaultCode, originalFaultString);
		
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService#getRrByRifMessaggioRevoca(java.lang.String, java.util.Date)
	 */
	@Override
	public MygovRrEr getRrByRifMessaggioRevoca(String idRiferimentoMessaggioRevoca,
			Date dataRiferimentoMessaggioRevoca) {

        log.debug("Invocato getRrRifMessaggioRevoca con: idRiferimentoMessaggioRevoca = [" + idRiferimentoMessaggioRevoca + "] , "
                + "dataRiferimentoMessaggioRevoca = [" + dataRiferimentoMessaggioRevoca + "]");
        return rrErDao.getRrByRifMessaggioRevoca(idRiferimentoMessaggioRevoca, dataRiferimentoMessaggioRevoca);

		
	}

	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService#checkRrByDomIuvCont(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovRrEr checkRrByDomIuvCont(String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento) {
		
		log.debug("Invocato checkRrIuv con: identificativoDominio = [" + identificativoDominio + "] , "
                + "identificativoUnivocoVersamento = [" + identificativoUnivocoVersamento + "]CodiceContestoPagamento = [" +codiceContestoPagamento+"]");
		return rrErDao.checkRrByDomIuvCont(identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);
	}

	
	/*
	 * (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService#checkRrByDomIuvCont(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MygovRrEr checkRevoca(String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento) {	
		log.debug("Invocato checkRevoca con: identificativoDominio = [" + identificativoDominio + "] , " + "identificativoUnivocoVersamento = [" + identificativoUnivocoVersamento + "] CodiceContestoPagamento = [" +codiceContestoPagamento+"]");
		return rrErDao.checkRevoca(identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);
	}
}
