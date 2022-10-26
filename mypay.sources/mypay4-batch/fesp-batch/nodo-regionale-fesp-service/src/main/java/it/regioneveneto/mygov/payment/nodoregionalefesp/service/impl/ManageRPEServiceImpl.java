/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RpEDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RpEDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpEDettaglio;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RpEDettaglioDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPEService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author regione del veneto
 *
 */
public class ManageRPEServiceImpl implements ManageRPEService {

	private static final Log log = LogFactory.getLog(ManageRPEServiceImpl.class);

	private RpEDettaglioDao rpEDettaglioDao;

	private RpEDao rpEDao;

	/**
	 * @param rpEDettaglioDao the rpEDettaglioDao to set
	 */
	public void setRpEDettaglioDao(RpEDettaglioDao rpEDettaglioDao) {
		this.rpEDettaglioDao = rpEDettaglioDao;
	}

	/**
	 * @param rpEDao the rpEDao to set
	 */
	public void setRpEDao(RpEDao rpEDao) {
		this.rpEDao = rpEDao;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPEService#insertRP(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.math.BigDecimal, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	@Transactional
	public MygovRpE insertRPWithRefresh(final String codRpSilinviarpIdPsp, final String codRpSilinviarpIdIntermediarioPsp, final String codRpSilinviarpIdCanale,
			final String codRpSilinviarpIdDominio, final String codRpSilinviarpIdUnivocoVersamento, final String codRpSilinviarpCodiceContestoPagamento,
			final String deRpVersioneOggetto, final String codRpDomIdDominio, final String codRpDomIdStazioneRichiedente,
			final String codRpIdMessaggioRichiesta, final Date dtRpDataOraMessaggioRichiesta, final String codRpAutenticazioneSoggetto,
			final String codRpSoggVersIdUnivVersTipoIdUnivoco, final String codRpSoggVersIdUnivVersCodiceIdUnivoco, final String deRpSoggVersAnagraficaVersante,
			final String deRpSoggVersIndirizzoVersante, final String deRpSoggVersCivicoVersante, final String codRpSoggVersCapVersante,
			final String deRpSoggVersLocalitaVersante, final String deRpSoggVersProvinciaVersante, final String codRpSoggVersNazioneVersante,
			final String deRpSoggVersEmailVersante, final String codRpSoggPagIdUnivPagTipoIdUnivoco, final String codRpSoggPagIdUnivPagCodiceIdUnivoco,
			final String deRpSoggPagAnagraficaPagatore, final String deRpSoggPagIndirizzoPagatore, final String deRpSoggPagCivicoPagatore,
			final String codRpSoggPagCapPagatore, final String deRpSoggPagLocalitaPagatore, final String deRpSoggPagProvinciaPagatore,
			final String codRpSoggPagNazionePagatore, final String deRpSoggPagEmailPagatore, final Date dtRpDatiVersDataEsecuzionePagamento,
			final BigDecimal numRpDatiVersImportoTotaleDaVersare, final String codRpDatiVersTipoVersamento, final String codRpDatiVersIdUnivocoVersamento,
			final String codRpDatiVersCodiceContestoPagamento, final String deRpDatiVersIbanAddebito, final String deRpDatiVersBicAddebito,
			final Integer modelloPagamento, final List<RpEDettaglioDto> rpEDettaglios) {

		log.debug("Invocato insertRP con: codRpSilinviarpIdPsp = [" + codRpSilinviarpIdPsp + "] ");

		MygovRpE mygovRpE = rpEDao.insertRPWithRefresh(codRpSilinviarpIdPsp, codRpSilinviarpIdIntermediarioPsp, codRpSilinviarpIdCanale,
				codRpSilinviarpIdDominio, codRpSilinviarpIdUnivocoVersamento, codRpSilinviarpCodiceContestoPagamento, deRpVersioneOggetto, codRpDomIdDominio,
				codRpDomIdStazioneRichiedente, codRpIdMessaggioRichiesta, dtRpDataOraMessaggioRichiesta, codRpAutenticazioneSoggetto,
				codRpSoggVersIdUnivVersTipoIdUnivoco, codRpSoggVersIdUnivVersCodiceIdUnivoco, deRpSoggVersAnagraficaVersante, deRpSoggVersIndirizzoVersante,
				deRpSoggVersCivicoVersante, codRpSoggVersCapVersante, deRpSoggVersLocalitaVersante, deRpSoggVersProvinciaVersante, codRpSoggVersNazioneVersante,
				deRpSoggVersEmailVersante, codRpSoggPagIdUnivPagTipoIdUnivoco, codRpSoggPagIdUnivPagCodiceIdUnivoco, deRpSoggPagAnagraficaPagatore,
				deRpSoggPagIndirizzoPagatore, deRpSoggPagCivicoPagatore, codRpSoggPagCapPagatore, deRpSoggPagLocalitaPagatore, deRpSoggPagProvinciaPagatore,
				codRpSoggPagNazionePagatore, deRpSoggPagEmailPagatore, dtRpDatiVersDataEsecuzionePagamento, numRpDatiVersImportoTotaleDaVersare,
				codRpDatiVersTipoVersamento, codRpDatiVersIdUnivocoVersamento, codRpDatiVersCodiceContestoPagamento, deRpDatiVersIbanAddebito,
				deRpDatiVersBicAddebito, modelloPagamento);

		log.debug("insertRP leggo e carico dettagli");

		for (RpEDettaglioDto dett : rpEDettaglios) {
			rpEDettaglioDao.insert(mygovRpE, dett.getNumRpDatiVersDatiSingVersImportoSingoloVersamento(),
					dett.getNumRpDatiVersDatiSingVersCommissioneCaricoPa(), dett.getCodRpDatiVersDatiSingVersIbanAccredito(),
					dett.getCodRpDatiVersDatiSingVersBicAccredito(), dett.getCodRpDatiVersDatiSingVersIbanAppoggio(),
					dett.getCodRpDatiVersDatiSingVersBicAppoggio(), dett.getCodRpDatiVersDatiSingVersCredenzialiPagatore(),
					dett.getDeRpDatiVersDatiSingVersCausaleVersamento(), dett.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(),
					dett.getNumEDatiPagDatiSingPagSingoloImportoPagato(), dett.getDeEDatiPagDatiSingPagEsitoSingoloPagamento(),
					dett.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(), dett.getCodEDatiPagDatiSingPagIdUnivocoRiscoss(),
					dett.getDeEDatiPagDatiSingPagCausaleVersamento(), dett.getDeEDatiPagDatiSingPagDatiSpecificiRiscossione(),
					dett.getNumEDatiPagDatiSingPagCommissioniApplicatePsp(), dett.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo(),
					dett.getBlbEDatiPagDatiSingPagAllegatoRicevutaTest(),
					StringUtils.isNotBlank(dett.getCodRpDatiVersDatiSingVersDatiMbdTipoBollo()) ? dett.getCodRpDatiVersDatiSingVersDatiMbdTipoBollo() : null,
					StringUtils.isNotBlank(dett.getCodRpDatiVersDatiSingVersDatiMbdHashDocumento()) ? dett.getCodRpDatiVersDatiSingVersDatiMbdHashDocumento()
							: null,
					StringUtils.isNotBlank(dett.getCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza())
							? dett.getCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza() : null);
		}

		log.debug("fine insertRP");

		return mygovRpE;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPEService#updateEByRpIuv(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String, java.util.List)
	 */
	@Override
	@Transactional
	public void updateEById(final Long mygovRpEId, final String codESilinviaesitoIdDominio, final String codESilinviaesitoIdUnivocoVersamento,
			final String codESilinviaesitoCodiceContestoPagamento, final String deEVersioneOggetto, final String codEDomIdDominio,
			final String codEDomIdStazioneRichiedente, final String codEIdMessaggioRicevuta, final Date dtEDataOraMessaggioRicevuta,
			final String codERiferimentoMessaggioRichiesta, final Date dtERiferimentoDataRichiesta, final String codEIstitAttesIdUnivAttesTipoIdUnivoco,
			final String codEIstitAttesIdUnivAttesCodiceIdUnivoco, final String deEIstitAttesDenominazioneAttestante,
			final String codEIstitAttesCodiceUnitOperAttestante, final String deEIstitAttesDenomUnitOperAttestante,
			final String deEIstitAttesIndirizzoAttestante, final String deEIstitAttesCivicoAttestante, final String codEIstitAttesCapAttestante,
			final String deEIstitAttesLocalitaAttestante, final String deEIstitAttesProvinciaAttestante, final String codEIstitAttesNazioneAttestante,
			final String codEEnteBenefIdUnivBenefTipoIdUnivoco, final String codEEnteBenefIdUnivBenefCodiceIdUnivoco,
			final String deEEnteBenefDenominazioneBeneficiario, final String codEEnteBenefCodiceUnitOperBeneficiario,
			final String deEEnteBenefDenomUnitOperBeneficiario, final String deEEnteBenefIndirizzoBeneficiario, final String deEEnteBenefCivicoBeneficiario,
			final String codEEnteBenefCapBeneficiario, final String deEEnteBenefLocalitaBeneficiario, final String deEEnteBenefProvinciaBeneficiario,
			final String codEEnteBenefNazioneBeneficiario, final String codESoggVersIdUnivVersTipoIdUnivoco, final String codESoggVersIdUnivVersCodiceIdUnivoco,
			final String deESoggVersAnagraficaVersante, final String deESoggVersIndirizzoVersante, final String deESoggVersCivicoVersante,
			final String codESoggVersCapVersante, final String deESoggVersLocalitaVersante, final String deESoggVersProvinciaVersante,
			final String codESoggVersNazioneVersante, final String deESoggVersEmailVersante, final String codESoggPagIdUnivPagTipoIdUnivoco,
			final String codESoggPagIdUnivPagCodiceIdUnivoco, final String deESoggPagAnagraficaPagatore, final String deESoggPagIndirizzoPagatore,
			final String deESoggPagCivicoPagatore, final String codESoggPagCapPagatore, final String deESoggPagLocalitaPagatore,
			final String deESoggPagProvinciaPagatore, final String codESoggPagNazionePagatore, final String deESoggPagEmailPagatore,
			final String codEDatiPagCodiceEsitoPagamento, final BigDecimal numEDatiPagImportoTotalePagato, final String codEDatiPagIdUnivocoVersamento,
			final String codEDatiPagCodiceContestoPagamento, final List<RpEDettaglioDto> rpEDettaglios) {

		log.debug("Invocato updateEById con: mygovRpEId = [" + mygovRpEId + "]");

		MygovRpE mygovRpE = rpEDao.updateEById(mygovRpEId, codESilinviaesitoIdDominio, codESilinviaesitoIdUnivocoVersamento,
				codESilinviaesitoCodiceContestoPagamento, deEVersioneOggetto, codEDomIdDominio, codEDomIdStazioneRichiedente, codEIdMessaggioRicevuta,
				dtEDataOraMessaggioRicevuta, codERiferimentoMessaggioRichiesta, dtERiferimentoDataRichiesta, codEIstitAttesIdUnivAttesTipoIdUnivoco,
				codEIstitAttesIdUnivAttesCodiceIdUnivoco, deEIstitAttesDenominazioneAttestante, codEIstitAttesCodiceUnitOperAttestante,
				deEIstitAttesDenomUnitOperAttestante, deEIstitAttesIndirizzoAttestante, deEIstitAttesCivicoAttestante, codEIstitAttesCapAttestante,
				deEIstitAttesLocalitaAttestante, deEIstitAttesProvinciaAttestante, codEIstitAttesNazioneAttestante, codEEnteBenefIdUnivBenefTipoIdUnivoco,
				codEEnteBenefIdUnivBenefCodiceIdUnivoco, deEEnteBenefDenominazioneBeneficiario, codEEnteBenefCodiceUnitOperBeneficiario,
				deEEnteBenefDenomUnitOperBeneficiario, deEEnteBenefIndirizzoBeneficiario, deEEnteBenefCivicoBeneficiario, codEEnteBenefCapBeneficiario,
				deEEnteBenefLocalitaBeneficiario, deEEnteBenefProvinciaBeneficiario, codEEnteBenefNazioneBeneficiario, codESoggVersIdUnivVersTipoIdUnivoco,
				codESoggVersIdUnivVersCodiceIdUnivoco, deESoggVersAnagraficaVersante, deESoggVersIndirizzoVersante, deESoggVersCivicoVersante,
				codESoggVersCapVersante, deESoggVersLocalitaVersante, deESoggVersProvinciaVersante, codESoggVersNazioneVersante, deESoggVersEmailVersante,
				codESoggPagIdUnivPagTipoIdUnivoco, codESoggPagIdUnivPagCodiceIdUnivoco, deESoggPagAnagraficaPagatore, deESoggPagIndirizzoPagatore,
				deESoggPagCivicoPagatore, codESoggPagCapPagatore, deESoggPagLocalitaPagatore, deESoggPagProvinciaPagatore, codESoggPagNazionePagatore,
				deESoggPagEmailPagatore, codEDatiPagCodiceEsitoPagamento, numEDatiPagImportoTotalePagato, codEDatiPagIdUnivocoVersamento,
				codEDatiPagCodiceContestoPagamento);

		log.debug("Carico Dettagli pagamenti ");

		if (rpEDettaglios.size() > 0) {
			List<MygovRpEDettaglio> listDett = rpEDettaglioDao.getByRpE(mygovRpE);
			Iterator<RpEDettaglioDto> listDettagliDaAggiornare = rpEDettaglios.iterator();
			for (MygovRpEDettaglio dett : listDett) {

				RpEDettaglioDto dettaglioDaAggiornare = listDettagliDaAggiornare.next();

				rpEDettaglioDao.updateDateE(dett.getMygovRpEDettaglioId(), dett.getVersion(), mygovRpE,
						dettaglioDaAggiornare.getNumEDatiPagDatiSingPagSingoloImportoPagato(),
						dettaglioDaAggiornare.getDeEDatiPagDatiSingPagEsitoSingoloPagamento(),
						dettaglioDaAggiornare.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(),
						dettaglioDaAggiornare.getCodEDatiPagDatiSingPagIdUnivocoRiscoss(), dettaglioDaAggiornare.getDeEDatiPagDatiSingPagCausaleVersamento(),
						dettaglioDaAggiornare.getDeEDatiPagDatiSingPagDatiSpecificiRiscossione(),
						dettaglioDaAggiornare.getNumEDatiPagDatiSingPagCommissioniApplicatePsp(),
						dettaglioDaAggiornare.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo(),
						dettaglioDaAggiornare.getBlbEDatiPagDatiSingPagAllegatoRicevutaTest());

			}
		}

		log.debug("Fine updateEByRpIuv");

	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPEService#updateRispostaEByEIuv(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public MygovRpE updateRispostaEById(final Long mygovRpEId, final String codAckE, final String deESilinviaesitoEsito,
			final String codESilinviaesitoFaultCode, final String deESilinviaesitoFaultString, final String codESilinviaesitoId,
			final String deESilinviaesitoDescription, final Integer codESilinviaesitoSerial, final String codESilinviaesitoOriginalFaultCode, 
			final String deESilinviaesitoOriginalFaultString, final String deESilinviaesitoOriginalFaultDescription) {

		log.debug("Invocato updateRispostaEById con: mygovRpEId = [" + mygovRpEId + "] ");

		return rpEDao.updateRispostaEById(mygovRpEId, codAckE, deESilinviaesitoEsito, codESilinviaesitoFaultCode, deESilinviaesitoFaultString,
				codESilinviaesitoId, deESilinviaesitoDescription, codESilinviaesitoSerial, codESilinviaesitoOriginalFaultCode, 
				deESilinviaesitoOriginalFaultString, deESilinviaesitoOriginalFaultDescription);
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPEService#updateRispostaRpByRpIuv(java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public MygovRpE updateRispostaRpById(final Long mygovRpEId, final String deRpSilinviarpEsito, final Integer codRpSilinviarpRedirect,
			final String codRpSilinviarpUrl, final String codRpSilinviarpFaultCode, final String deRpSilinviarpFaultString, final String codRpSilinviarpId,
			final String deRpSilinviarpDescription, final Integer codRpSilinviarpSerial, final String idSession, final String codRpSilinviarpOriginalFaultCode, 
			final String deRpSilinviarpOriginalFaultString, final String deRpSilinviarpOriginalFaultDescription) {

		log.debug("Invocato updateRispostaRpById con: mygovRpEId = [" + mygovRpEId + "] ");

		return rpEDao.updateRispostaRpByRpId(mygovRpEId, deRpSilinviarpEsito, codRpSilinviarpRedirect, codRpSilinviarpUrl, codRpSilinviarpFaultCode,
				deRpSilinviarpFaultString, codRpSilinviarpId, deRpSilinviarpDescription, codRpSilinviarpSerial, idSession, codRpSilinviarpOriginalFaultCode,
				deRpSilinviarpOriginalFaultString, deRpSilinviarpOriginalFaultDescription);
	}
	
	@Override
	public List<MygovRpE> updateRispostaRpByCarrello(final MygovCarrelloRp mygovCarrelloRp, final String deRpSilinviarpEsito, 
			final String codRpSilinviarpUrl, final String codRpSilinviarpFaultCode, final String deRpSilinviarpFaultString, final String codRpSilinviarpId,
			final String deRpSilinviarpDescription, final Integer codRpSilinviarpSerial, final String codRpSilinviarpOriginalFaultCode, 
			final String deRpSilinviarpOriginalFaultString, final String deRpSilinviarpOriginalFaultDescription) {

		log.debug("Invocato updateRispostaRpByCarrello con: mygovCarrelloRpId = [" + mygovCarrelloRp.getMygovCarrelloRpId() + "] ");

		return rpEDao.updateRispostaRpByRpCarrello(mygovCarrelloRp, deRpSilinviarpEsito, codRpSilinviarpUrl, codRpSilinviarpFaultCode, 
				deRpSilinviarpFaultString, codRpSilinviarpId, deRpSilinviarpDescription, codRpSilinviarpSerial,codRpSilinviarpOriginalFaultCode,
				deRpSilinviarpOriginalFaultString, deRpSilinviarpOriginalFaultDescription);
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPEService#getRpByIdSession(java.lang.String)
	 */
	@Override
	public MygovRpE getRpByIdSession(final String idSession) {
		return rpEDao.getRpByIdSession(idSession);
	}

	@Override
	public MygovRpE updateECodEsitoByRpEId(final long rpEId, String esito) {
		return rpEDao.updateECodEsitoPagamentoById(rpEId, esito);

	}

	@Override
	public void deleteByIdSession(String idSession) {
		rpEDao.deleteByIdSession(idSession);
	}

	@Override
	public MygovRpE getRpByRpEId(long mygovRpEId) {
		return rpEDao.doGet(mygovRpEId);
	}

	@Override
	public MygovRpE updateCarrelloRef(Long mygovRpEId, MygovCarrelloRp mygovCarrelloRp) {
		return rpEDao.updateCarrelloRef(mygovRpEId, mygovCarrelloRp);
		
	}

}
