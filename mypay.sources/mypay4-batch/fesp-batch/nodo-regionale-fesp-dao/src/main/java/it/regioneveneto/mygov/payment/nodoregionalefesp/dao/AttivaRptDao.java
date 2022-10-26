/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovAttivaRptE;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.xmlbeans.impl.tool.Extension.Param;

/**
 * @author regione del veneto
 * 
 */
public interface AttivaRptDao {

	/**
	 * @param codAttivarptIdPsp
	 * @param codAttivarptIdentificativoIntermediarioPa
	 * @param codAttivarptIdentificativoStazioneIntermediarioPa
	 * @param codAttivarptIdentificativoDominio
	 * @param codAttivarptIdentificativoUnivocoVersamento
	 * @param codAttivarptCodiceContestoPagamento
	 * @return
	 */
	MygovAttivaRptE getByKey(final String codAttivarptIdentificativoDominio, final String codAttivarptIdentificativoUnivocoVersamento,
			final String codAttivarptCodiceContestoPagamento);

	/**
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
	MygovAttivaRptE insert(final String codAttivarptIdPsp, final String codAttivarptIdIntermediarioPsp, final String codAttivarptIdCanalePsp,
			final String codAttivarptIdentificativoIntermediarioPa, final String codAttivarptIdentificativoStazioneIntermediarioPa,
			final String codAttivarptIdentificativoDominio, final String codAttivarptIdentificativoUnivocoVersamento,
			final String codAttivarptCodiceContestoPagamento, Date dtAttivarpt, final BigDecimal numAttivarptImportoSingoloVersamento,
			final String deAttivarptIbanAppoggio, String deAttivarptBicAppoggio, final String codAttivarptSoggVersIdUnivVersTipoIdUnivoco,
			final String codAttivarptSoggVersIdUnivVersCodiceIdUnivoco, final String deAttivarptSoggVersAnagraficaVersante,
			final String deAttivarptSoggVersIndirizzoVersante, final String deAttivarptSoggVersCivicoVersante, final String codAttivarptSoggVersCapVersante,
			final String deAttivarptSoggVersLocalitaVersante, final String deAttivarptSoggVersProvinciaVersante,
			final String codAttivarptSoggVersNazioneVersante, final String deAttivarptSoggVersEmailVersante, final String deAttivarptIbanAddebito,
			String deAttivarptBicAddebito, final String codAttivarptSoggPagIdUnivPagTipoIdUnivoco, final String codAttivarptSoggPagIdUnivPagCodiceIdUnivoco,
			final String deAttivarptSoggPagAnagraficaPagatore, final String deAttivarptSoggPagIndirizzoPagatore, final String deAttivarptSoggPagCivicoPagatore,
			final String codAttivarptSoggPagCapPagatore, final String deAttivarptSoggPagLocalitaPagatore, final String deAttivarptSoggPagProvinciaPagatore,
			final String codAttivarptSoggPagNazionePagatore, final String deAttivarptSoggPagEmailPagatore);

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
	 * @param codAttivarptOriginalFaultCode
	 * @param deAttivarptOriginalFaultString
	 * @param deAttivarptOriginalFaultDescription
	 * @return
	 */
	MygovAttivaRptE updateByKey(final long mygovAttivaRptEId, final Date dtEAttivarpt, final BigDecimal numEAttivarptImportoSingoloVersamento,
			final String deEAttivarptIbanAccredito, final String deEAttivarptBicAccredito, final String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco,
			final String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco, final String deEAttivarptEnteBenefDenominazioneBeneficiario,
			final String codEAttivarptEnteBenefCodiceUnitOperBeneficiario, final String deEAttivarptEnteBenefDenomUnitOperBeneficiario,
			final String deEAttivarptEnteBenefIndirizzoBeneficiario, final String deEAttivarptEnteBenefCivicoBeneficiario,
			final String codEAttivarptEnteBenefCapBeneficiario, final String deEAttivarptEnteBenefLocalitaBeneficiario,
			final String deEAttivarptEnteBenefProvinciaBeneficiario, final String codEAttivarptEnteBenefNazioneBeneficiario,
			final String deEAttivarptCredenzialiPagatore, final String deEAttivarptCausaleVersamento, final String deAttivarptEsito,
			final String codAttivarptFaultCode, final String deAttivarptFaultString, final String codAttivarptId, final String deAttivarptDescription,
			final Integer codAttivarptSerial, final String codAttivarptOriginalFaultCode, final String deAttivarptOriginalFaultString, 
			final String deAttivarptOriginalFaultDescription);

	/**
	 * @param mygovAttivaRptEId
	 * @param deAttivarptEsito
	 * @return
	 */
	MygovAttivaRptE updateEsitoByKey(final long mygovAttivaRptEId, final String deAttivarptEsito);

}
