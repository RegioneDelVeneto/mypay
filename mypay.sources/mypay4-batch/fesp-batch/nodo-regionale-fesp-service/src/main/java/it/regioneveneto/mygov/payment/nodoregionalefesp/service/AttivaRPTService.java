package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovAttivaRptE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author regione del veneto
 */

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface AttivaRPTService {

	/**
	 * Metodo per l'inserimento della request di Attiva al momento della
	 * richiesta da parte del nodo SPC
	 * @param codAttivarptIdPsp
	 * @param codAttivarptIdentificativoIntermediarioPa
	 * @param codAttivarptIdentificativoStazioneIntermediarioPa
	 * @param codAttivarptIdentificativoDominio
	 * @param codAttivarptIdentificativoUnivocoVersamento
	 * @param codAttivarptCodiceContestoPagamento
	 * @param dtAttivarpt
	 * @param numAttivarptImportoSingoloVersamento
	 * @param deAttivarptIbanAppoggio
	 * @param deAttivarptBicAppoggio
	 * @param codAttivarptSoggVersIdUnivVersTipoIdUnivoco
	 * @param codAttivarptSoggVersIdUnivVersCodiceIdUnivoco
	 * @param deAttivarptSoggVersAnagraficaVersante
	 * @param deAttivarptSoggVersIndirizzoVersante
	 * @param deAttivarptSoggVersCivicoVersante
	 * @param codAttivarptSoggVersCapVersante
	 * @param deAttivarptSoggVersLocalitaVersante
	 * @param deAttivarptSoggVersProvinciaVersante
	 * @param codAttivarptSoggVersNazioneVersante
	 * @param deAttivarptSoggVersEmailVersante
	 * @param deAttivarptIbanAddebito
	 * @param deAttivarptBicAddebito
	 * @param codAttivarptSoggPagIdUnivPagTipoIdUnivoco
	 * @param codAttivarptSoggPagIdUnivPagCodiceIdUnivoco
	 * @param deAttivarptSoggPagAnagraficaPagatore
	 * @param deAttivarptSoggPagIndirizzoPagatore
	 * @param deAttivarptSoggPagCivicoPagatore
	 * @param codAttivarptSoggPagCapPagatore
	 * @param deAttivarptSoggPagLocalitaPagatore
	 * @param deAttivarptSoggPagProvinciaPagatore
	 * @param codAttivarptSoggPagNazionePagatore
	 * @param deAttivarptSoggPagEmailPagatore
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovAttivaRptE insert(final String codAttivarptIdPsp,
			final String codAttivarptIdIntermediarioPsp,
			final String  codAttivarptIdCanalePsp,
			final String codAttivarptIdentificativoIntermediarioPa,
			final String codAttivarptIdentificativoStazioneIntermediarioPa,
			final String codAttivarptIdentificativoDominio,
			final String codAttivarptIdentificativoUnivocoVersamento,
			final String codAttivarptCodiceContestoPagamento,
			final Date dtAttivarpt,
			final BigDecimal numAttivarptImportoSingoloVersamento,
			final String deAttivarptIbanAppoggio,
			final String deAttivarptBicAppoggio,
			final String codAttivarptSoggVersIdUnivVersTipoIdUnivoco,
			final String codAttivarptSoggVersIdUnivVersCodiceIdUnivoco,
			final String deAttivarptSoggVersAnagraficaVersante,
			final String deAttivarptSoggVersIndirizzoVersante,
			final String deAttivarptSoggVersCivicoVersante,
			final String codAttivarptSoggVersCapVersante,
			final String deAttivarptSoggVersLocalitaVersante,
			final String deAttivarptSoggVersProvinciaVersante,
			final String codAttivarptSoggVersNazioneVersante,
			final String deAttivarptSoggVersEmailVersante,
			final String deAttivarptIbanAddebito,
			final String deAttivarptBicAddebito,
			final String codAttivarptSoggPagIdUnivPagTipoIdUnivoco,
			final String codAttivarptSoggPagIdUnivPagCodiceIdUnivoco,
			final String deAttivarptSoggPagAnagraficaPagatore,
			final String deAttivarptSoggPagIndirizzoPagatore,
			final String deAttivarptSoggPagCivicoPagatore,
			final String codAttivarptSoggPagCapPagatore,
			final String deAttivarptSoggPagLocalitaPagatore,
			final String deAttivarptSoggPagProvinciaPagatore,
			final String codAttivarptSoggPagNazionePagatore,
			final String deAttivarptSoggPagEmailPagatore);



	/**
	 * @param codAttivarptIdPsp
	 * @param codAttivarptIdentificativoIntermediarioPa
	 * @param codAttivarptIdentificativoStazioneIntermediarioPa
	 * @param codAttivarptIdentificativoDominio
	 * @param codAttivarptIdentificativoUnivocoVersamento
	 * @param codAttivarptCodiceContestoPagamento
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	MygovAttivaRptE getByKey(final String codAttivarptIdentificativoDominio, 
			final String codAttivarptIdentificativoUnivocoVersamento, final String codAttivarptCodiceContestoPagamento);

	/**
	 * @param codAttivarptIdPsp
	 * @param codAttivarptIdentificativoIntermediarioPa
	 * @param codAttivarptIdentificativoStazioneIntermediarioPa
	 * @param codAttivarptIdentificativoDominio
	 * @param codAttivarptIdentificativoUnivocoVersamento
	 * @param codAttivarptCodiceContestoPagamento
	 * @param dtEAttivarpt
	 * @param numEAttivarptImportoSingoloVersamento
	 * @param deEAttivarptIbanAccredito
	 * @param deEAttivarptBicAccredito
	 * @param codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco
	 * @param codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco
	 * @param deEAttivarptEnteBenefDenominazioneBeneficiario
	 * @param codEAttivarptEnteBenefCodiceUnitOperBeneficiario
	 * @param deEAttivarptEnteBenefDenomUnitOperBeneficiario
	 * @param deEAttivarptEnteBenefIndirizzoBeneficiario
	 * @param deEAttivarptEnteBenefCivicoBeneficiario
	 * @param codEAttivarptEnteBenefCapBeneficiario
	 * @param deEAttivarptEnteBenefLocalitaBeneficiario
	 * @param deEAttivarptEnteBenefProvinciaBeneficiario
	 * @param codEAttivarptEnteBenefNazioneBeneficiario
	 * @param deEAttivarptCredenzialiPagatore
	 * @param deEAttivarptCausaleVersamento
	 * @param deAttivarptEsito
	 * @param codAttivarptFaultCode
	 * @param deAttivarptFaultString
	 * @param codAttivarptId
	 * @param deAttivarptDescription
	 * @param codAttivarptSerial
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovAttivaRptE updateByKey(final long mygovAttivaRptEId,
			final Date dtEAttivarpt,
			final BigDecimal numEAttivarptImportoSingoloVersamento,
			final String deEAttivarptIbanAccredito,
			final String deEAttivarptBicAccredito,
			final String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco,
			final String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco,
			final String deEAttivarptEnteBenefDenominazioneBeneficiario,
			final String codEAttivarptEnteBenefCodiceUnitOperBeneficiario,
			final String deEAttivarptEnteBenefDenomUnitOperBeneficiario,
			final String deEAttivarptEnteBenefIndirizzoBeneficiario,
			final String deEAttivarptEnteBenefCivicoBeneficiario,
			final String codEAttivarptEnteBenefCapBeneficiario,
			final String deEAttivarptEnteBenefLocalitaBeneficiario,
			final String deEAttivarptEnteBenefProvinciaBeneficiario,
			final String codEAttivarptEnteBenefNazioneBeneficiario,
			final String deEAttivarptCredenzialiPagatore,
			final String deEAttivarptCausaleVersamento,	
			final String deAttivarptEsito,
			final String codAttivarptFaultCode,
			final String deAttivarptFaultString,
			final String codAttivarptId,
			final String deAttivarptDescription,
			final Integer codAttivarptSerial,
			final String codAttivarptOriginalFaultCode, 
			final String deAttivarptOriginalFaultString, 
			final String deAttivarptOriginalFaultDescription);


	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	List<MygovRptRt> elaboraRPTAttivate();		

}
