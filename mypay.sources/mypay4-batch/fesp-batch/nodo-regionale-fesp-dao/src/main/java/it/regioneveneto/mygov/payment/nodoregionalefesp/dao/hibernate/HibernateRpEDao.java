package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RpEDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RpEDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.exceptions.MandatoryFieldsException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpEDettaglio;

/**
 * @author regione del veneto
 *
 */
public class HibernateRpEDao extends HibernateDaoSupport implements RpEDao {

	private static final Log log = LogFactory.getLog(HibernateRpEDao.class);

	private RpEDettaglioDao rpEDettaglioDao;

	public void setRpEDettaglioDao(RpEDettaglioDao rpEDettaglioDao) {
		this.rpEDettaglioDao = rpEDettaglioDao;
	}

	@Override
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
			final Integer modelloPagamento) throws DataAccessException {

		log.debug("Invocato metodo insertRPWithRefresh PARAMETRI ::: " + "codRpSilinviarpIdPsp = [" + codRpSilinviarpIdPsp
				+ "] ::: codRpSilinviarpIdIntermediarioPsp = [" + codRpSilinviarpIdIntermediarioPsp + "] ::: codRpSilinviarpIdCanale= ["
				+ codRpSilinviarpIdCanale + "] ::: codRpSilinviarpIdDominio = [" + codRpSilinviarpIdDominio + "] ::: codRpSilinviarpIdUnivocoVersamento= ["
				+ codRpSilinviarpIdUnivocoVersamento + "] ::: codRpSilinviarpCodiceContestoPagamento = [" + codRpSilinviarpCodiceContestoPagamento
				+ "] ::: deRpVersioneOggetto= [" + deRpVersioneOggetto + "] ::: codRpDomIdDominio = [" + codRpDomIdDominio
				+ "] ::: codRpDomIdStazioneRichiedente= [" + codRpDomIdStazioneRichiedente + "] ::: codRpIdMessaggioRichiesta = [" + codRpIdMessaggioRichiesta
				+ "] ::: dtRpDataOraMessaggioRichiesta= [" + dtRpDataOraMessaggioRichiesta + "] ::: codRpAutenticazioneSoggetto = ["
				+ codRpAutenticazioneSoggetto + "] ::: codRpSoggVersIdUnivVersTipoIdUnivoco= [" + codRpSoggVersIdUnivVersTipoIdUnivoco
				+ "] ::: codRpSoggVersIdUnivVersCodiceIdUnivoco = [" + codRpSoggVersIdUnivVersCodiceIdUnivoco + "] ::: deRpSoggVersAnagraficaVersante= ["
				+ deRpSoggVersAnagraficaVersante + "] ::: deRpSoggVersIndirizzoVersante = [" + deRpSoggVersIndirizzoVersante
				+ "] ::: deRpSoggVersCivicoVersante= [" + deRpSoggVersCivicoVersante + "] ::: codRpSoggVersCapVersante = [" + codRpSoggVersCapVersante
				+ "] ::: deRpSoggVersLocalitaVersante= [" + deRpSoggVersLocalitaVersante + "] ::: deRpSoggVersProvinciaVersante = ["
				+ deRpSoggVersProvinciaVersante + "] ::: codRpSoggVersNazioneVersante= [" + codRpSoggVersNazioneVersante + "] ::: deRpSoggVersEmailVersante = ["
				+ deRpSoggVersEmailVersante + "] ::: codRpSoggPagIdUnivPagTipoIdUnivoco= [" + codRpSoggPagIdUnivPagTipoIdUnivoco
				+ "] ::: codRpSoggPagIdUnivPagCodiceIdUnivoco = [" + codRpSoggPagIdUnivPagCodiceIdUnivoco + "] ::: deRpSoggPagAnagraficaPagatore= ["
				+ deRpSoggPagAnagraficaPagatore + "] ::: deRpSoggPagIndirizzoPagatore = [" + deRpSoggPagIndirizzoPagatore + "] ::: deRpSoggPagCivicoPagatore= ["
				+ deRpSoggPagCivicoPagatore + "] ::: codRpSoggPagCapPagatore = [" + codRpSoggPagCapPagatore + "] ::: deRpSoggPagLocalitaPagatore= ["
				+ deRpSoggPagLocalitaPagatore + "] ::: deRpSoggPagProvinciaPagatore = [" + deRpSoggPagProvinciaPagatore + "] ::: codRpSoggPagNazionePagatore= ["
				+ codRpSoggPagNazionePagatore + "] ::: deRpSoggPagEmailPagatore = [" + deRpSoggPagEmailPagatore + "] ::: dtRpDatiVersDataEsecuzionePagamento= ["
				+ dtRpDatiVersDataEsecuzionePagamento + "] ::: numRpDatiVersImportoTotaleDaVersare = [" + numRpDatiVersImportoTotaleDaVersare
				+ "] ::: codRpDatiVersTipoVersamento= [" + codRpDatiVersTipoVersamento + "] ::: codRpDatiVersIdUnivocoVersamento = ["
				+ codRpDatiVersIdUnivocoVersamento + "] ::: codRpDatiVersCodiceContestoPagamento= [" + codRpDatiVersCodiceContestoPagamento
				+ "] ::: deRpDatiVersIbanAddebito = [" + deRpDatiVersIbanAddebito + "] ::: deRpDatiVersBicAddebito= [" + "] ::: modelloPagamento= ["
				+ modelloPagamento + "] ::: deRpDatiVersBicAddebito= [" + deRpDatiVersBicAddebito);

		MygovRpE mygovRpE = doInsertRP(codRpSilinviarpIdPsp, codRpSilinviarpIdIntermediarioPsp, codRpSilinviarpIdCanale, codRpSilinviarpIdDominio,
				codRpSilinviarpIdUnivocoVersamento, codRpSilinviarpCodiceContestoPagamento, deRpVersioneOggetto, codRpDomIdDominio,
				codRpDomIdStazioneRichiedente, codRpIdMessaggioRichiesta, dtRpDataOraMessaggioRichiesta, codRpAutenticazioneSoggetto,
				codRpSoggVersIdUnivVersTipoIdUnivoco, codRpSoggVersIdUnivVersCodiceIdUnivoco, deRpSoggVersAnagraficaVersante, deRpSoggVersIndirizzoVersante,
				deRpSoggVersCivicoVersante, codRpSoggVersCapVersante, deRpSoggVersLocalitaVersante, deRpSoggVersProvinciaVersante, codRpSoggVersNazioneVersante,
				deRpSoggVersEmailVersante, codRpSoggPagIdUnivPagTipoIdUnivoco, codRpSoggPagIdUnivPagCodiceIdUnivoco, deRpSoggPagAnagraficaPagatore,
				deRpSoggPagIndirizzoPagatore, deRpSoggPagCivicoPagatore, codRpSoggPagCapPagatore, deRpSoggPagLocalitaPagatore, deRpSoggPagProvinciaPagatore,
				codRpSoggPagNazionePagatore, deRpSoggPagEmailPagatore, dtRpDatiVersDataEsecuzionePagamento, numRpDatiVersImportoTotaleDaVersare,
				codRpDatiVersTipoVersamento, codRpDatiVersIdUnivocoVersamento, codRpDatiVersCodiceContestoPagamento, deRpDatiVersIbanAddebito,
				deRpDatiVersBicAddebito, modelloPagamento);

		getHibernateTemplate().flush();
		getHibernateTemplate().refresh(mygovRpE);

		return mygovRpE;
	}

	@Override
	public MygovRpE updateEById(Long mygovRpEId, String codESilinviaesitoIdDominio, String codESilinviaesitoIdUnivocoVersamento,
			String codESilinviaesitoCodiceContestoPagamento, String deEVersioneOggetto, String codEDomIdDominio, String codEDomIdStazioneRichiedente,
			String codEIdMessaggioRicevuta, Date dtEDataOraMessaggioRicevuta, String codERiferimentoMessaggioRichiesta, Date dtERiferimentoDataRichiesta,
			String codEIstitAttesIdUnivAttesTipoIdUnivoco, String codEIstitAttesIdUnivAttesCodiceIdUnivoco, String deEIstitAttesDenominazioneAttestante,
			String codEIstitAttesCodiceUnitOperAttestante, String deEIstitAttesDenomUnitOperAttestante, String deEIstitAttesIndirizzoAttestante,
			String deEIstitAttesCivicoAttestante, String codEIstitAttesCapAttestante, String deEIstitAttesLocalitaAttestante,
			String deEIstitAttesProvinciaAttestante, String codEIstitAttesNazioneAttestante, String codEEnteBenefIdUnivBenefTipoIdUnivoco,
			String codEEnteBenefIdUnivBenefCodiceIdUnivoco, String deEEnteBenefDenominazioneBeneficiario, String codEEnteBenefCodiceUnitOperBeneficiario,
			String deEEnteBenefDenomUnitOperBeneficiario, String deEEnteBenefIndirizzoBeneficiario, String deEEnteBenefCivicoBeneficiario,
			String codEEnteBenefCapBeneficiario, String deEEnteBenefLocalitaBeneficiario, String deEEnteBenefProvinciaBeneficiario,
			String codEEnteBenefNazioneBeneficiario, String codESoggVersIdUnivVersTipoIdUnivoco, String codESoggVersIdUnivVersCodiceIdUnivoco,
			String deESoggVersAnagraficaVersante, String deESoggVersIndirizzoVersante, String deESoggVersCivicoVersante, String codESoggVersCapVersante,
			String deESoggVersLocalitaVersante, String deESoggVersProvinciaVersante, String codESoggVersNazioneVersante, String deESoggVersEmailVersante,
			String codESoggPagIdUnivPagTipoIdUnivoco, String codESoggPagIdUnivPagCodiceIdUnivoco, String deESoggPagAnagraficaPagatore,
			String deESoggPagIndirizzoPagatore, String deESoggPagCivicoPagatore, String codESoggPagCapPagatore, String deESoggPagLocalitaPagatore,
			String deESoggPagProvinciaPagatore, String codESoggPagNazionePagatore, String deESoggPagEmailPagatore, String codEDatiPagCodiceEsitoPagamento,
			BigDecimal numEDatiPagImportoTotalePagato, String codEDatiPagIdUnivocoVersamento, String codEDatiPagCodiceContestoPagamento)
			throws DataAccessException {

		log.debug("Invocato metodo updateEById PARAMETRI ::: mygovRpEId = [" + mygovRpEId + "] ::: codESilinviaesitoIdDominio = [" + codESilinviaesitoIdDominio
				+ "] ::: codESilinviaesitoIdUnivocoVersamento = [" + codESilinviaesitoIdUnivocoVersamento + "] ::: codESilinviaesitoCodiceContestoPagamento = ["
				+ codESilinviaesitoCodiceContestoPagamento + "] ::: deEVersioneOggetto = [" + deEVersioneOggetto + "] ::: codEDomIdDominio = ["
				+ codEDomIdDominio + "] ::: codEDomIdStazioneRichiedente = [" + codEDomIdStazioneRichiedente + "] ::: codEIdMessaggioRicevuta = ["
				+ codEIdMessaggioRicevuta + "] ::: dtEDataOraMessaggioRicevuta = [" + dtEDataOraMessaggioRicevuta
				+ "] ::: codERiferimentoMessaggioRichiesta = [" + codERiferimentoMessaggioRichiesta + "] ::: dtERiferimentoDataRichiesta = ["
				+ dtERiferimentoDataRichiesta + "] ::: codEIstitAttesIdUnivAttesTipoIdUnivoco = [" + codEIstitAttesIdUnivAttesTipoIdUnivoco
				+ "] ::: codEIstitAttesIdUnivAttesCodiceIdUnivoco = [" + codEIstitAttesIdUnivAttesCodiceIdUnivoco
				+ "] ::: deEIstitAttesDenominazioneAttestante = [" + deEIstitAttesDenominazioneAttestante + "] ::: codEIstitAttesCodiceUnitOperAttestante = ["
				+ codEIstitAttesCodiceUnitOperAttestante + "] ::: deEIstitAttesDenomUnitOperAttestante = [" + deEIstitAttesDenomUnitOperAttestante
				+ "] ::: deEIstitAttesIndirizzoAttestante = [" + deEIstitAttesIndirizzoAttestante + "] ::: deEIstitAttesCivicoAttestante = ["
				+ deEIstitAttesCivicoAttestante + "] ::: codEIstitAttesCapAttestante = [" + codEIstitAttesCapAttestante
				+ "] ::: deEIstitAttesLocalitaAttestante = [" + deEIstitAttesLocalitaAttestante + "] ::: deEIstitAttesProvinciaAttestante = ["
				+ deEIstitAttesProvinciaAttestante + "] ::: codEIstitAttesNazioneAttestante = [" + codEIstitAttesNazioneAttestante
				+ "] ::: codEEnteBenefIdUnivBenefTipoIdUnivoco = [" + codEEnteBenefIdUnivBenefTipoIdUnivoco
				+ "] ::: codEEnteBenefIdUnivBenefCodiceIdUnivoco = [" + codEEnteBenefIdUnivBenefCodiceIdUnivoco
				+ "] ::: deEEnteBenefDenominazioneBeneficiario = [" + deEEnteBenefDenominazioneBeneficiario
				+ "] ::: codEEnteBenefCodiceUnitOperBeneficiario = [" + codEEnteBenefCodiceUnitOperBeneficiario
				+ "] ::: deEEnteBenefDenomUnitOperBeneficiario = [" + deEEnteBenefDenomUnitOperBeneficiario + "] ::: deEEnteBenefIndirizzoBeneficiario = ["
				+ deEEnteBenefIndirizzoBeneficiario + "] ::: deEEnteBenefCivicoBeneficiario = [" + deEEnteBenefCivicoBeneficiario
				+ "] ::: codEEnteBenefCapBeneficiario = [" + codEEnteBenefCapBeneficiario + "] ::: deEEnteBenefLocalitaBeneficiario = ["
				+ deEEnteBenefLocalitaBeneficiario + "] ::: deEEnteBenefProvinciaBeneficiario = [" + deEEnteBenefProvinciaBeneficiario
				+ "] ::: codEEnteBenefNazioneBeneficiario = [" + codEEnteBenefNazioneBeneficiario + "] ::: codESoggVersIdUnivVersTipoIdUnivoco = ["
				+ codESoggVersIdUnivVersTipoIdUnivoco + "] ::: codESoggVersIdUnivVersCodiceIdUnivoco = [" + codESoggVersIdUnivVersCodiceIdUnivoco
				+ "] ::: deESoggVersAnagraficaVersante = [" + deESoggVersAnagraficaVersante + "] ::: deESoggVersIndirizzoVersante = ["
				+ deESoggVersIndirizzoVersante + "] ::: deESoggVersCivicoVersante = [" + deESoggVersCivicoVersante + "] ::: codESoggVersCapVersante = ["
				+ codESoggVersCapVersante + "] ::: deESoggVersLocalitaVersante = [" + deESoggVersLocalitaVersante + "] ::: deESoggVersProvinciaVersante = ["
				+ deESoggVersProvinciaVersante + "] ::: codESoggVersNazioneVersante = [" + codESoggVersNazioneVersante + "] ::: deESoggVersEmailVersante = ["
				+ deESoggVersEmailVersante + "] ::: codESoggPagIdUnivPagTipoIdUnivoco = [" + codESoggPagIdUnivPagTipoIdUnivoco
				+ "] ::: codESoggPagIdUnivPagCodiceIdUnivoco = [" + codESoggPagIdUnivPagCodiceIdUnivoco + "] ::: deESoggPagAnagraficaPagatore = ["
				+ deESoggPagAnagraficaPagatore + "] ::: deESoggPagIndirizzoPagatore = [" + deESoggPagIndirizzoPagatore + "] ::: deESoggPagCivicoPagatore = ["
				+ deESoggPagCivicoPagatore + "] ::: codESoggPagCapPagatore = [" + codESoggPagCapPagatore + "] ::: deESoggPagLocalitaPagatore = ["
				+ deESoggPagLocalitaPagatore + "] ::: deESoggPagProvinciaPagatore = [" + deESoggPagProvinciaPagatore + "] ::: codESoggPagNazionePagatore = ["
				+ codESoggPagNazionePagatore + "] ::: deESoggPagEmailPagatore = [" + deESoggPagEmailPagatore + "] ::: codEDatiPagCodiceEsitoPagamento = ["
				+ codEDatiPagCodiceEsitoPagamento + "] ::: numEDatiPagImportoTotalePagato = [" + numEDatiPagImportoTotalePagato
				+ "] ::: codEDatiPagIdUnivocoVersamento = [" + codEDatiPagIdUnivocoVersamento + "] ::: codEDatiPagCodiceContestoPagamento = ["
				+ codEDatiPagCodiceContestoPagamento + "]");

		MygovRpE mygovRpE = doGet(mygovRpEId);
		//		LANCIO L'ECCEZIONE SUL METODO getByRpIuv
		//		if (mygovRpE == null)
		//			throw new DataRetrievalFailureException("'codRpDatiVersIdUnivocoVersamento' is not valid");

		if (StringUtils.isEmpty(deEVersioneOggetto) || StringUtils.isBlank(deEVersioneOggetto)) {
			log.error("Errore nell'inserimento esito: 'deEVersioneOggetto' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'deEVersioneOggetto' obbligatorio");
		}

		if (StringUtils.isEmpty(codEDomIdDominio) || StringUtils.isBlank(codEDomIdDominio)) {
			log.error("Errore nell'inserimento esito: 'codEDomIdDominio' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codEDomIdDominio' obbligatorio");
		}

		if (StringUtils.isEmpty(codEIdMessaggioRicevuta) || StringUtils.isBlank(codEIdMessaggioRicevuta)) {
			log.error("Errore nell'inserimento esito: 'codEIdMessaggioRicevuta' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codEIdMessaggioRicevuta' obbligatorio");
		}

		if (dtEDataOraMessaggioRicevuta == null) {
			log.error("Errore nell'inserimento esito: 'dtEDataOraMessaggioRicevuta' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'dtEDataOraMessaggioRicevuta' obbligatorio");
		}

		if (StringUtils.isEmpty(codERiferimentoMessaggioRichiesta) || StringUtils.isBlank(codERiferimentoMessaggioRichiesta)) {
			log.error("Errore nell'inserimento esito: 'codERiferimentoMessaggioRichiesta' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codERiferimentoMessaggioRichiesta' obbligatorio");
		}

		if (dtERiferimentoDataRichiesta == null) {
			log.error("Errore nell'inserimento esito: 'dtERiferimentoDataRichiesta' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'dtERiferimentoDataRichiesta' obbligatorio");
		}

		if (StringUtils.isEmpty(codEIstitAttesIdUnivAttesTipoIdUnivoco) || StringUtils.isBlank(codEIstitAttesIdUnivAttesTipoIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codEIstitAttesIdUnivAttesTipoIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codEIstitAttesIdUnivAttesTipoIdUnivoco' obbligatorio");
		}

		if (StringUtils.isEmpty(codEIstitAttesIdUnivAttesCodiceIdUnivoco) || StringUtils.isBlank(codEIstitAttesIdUnivAttesCodiceIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codEIstitAttesIdUnivAttesCodiceIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codEIstitAttesIdUnivAttesCodiceIdUnivoco' obbligatorio");
		}

		if (StringUtils.isEmpty(deEIstitAttesDenominazioneAttestante) || StringUtils.isBlank(deEIstitAttesDenominazioneAttestante)) {
			log.error("Errore nell'inserimento esito: 'deEIstitAttesDenominazioneAttestante' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'deEIstitAttesDenominazioneAttestante' obbligatorio");
		}

		if (StringUtils.isEmpty(codEEnteBenefIdUnivBenefTipoIdUnivoco) || StringUtils.isBlank(codEEnteBenefIdUnivBenefTipoIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codEEnteBenefIdUnivBenefTipoIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codEEnteBenefIdUnivBenefTipoIdUnivoco' obbligatorio");
		}

		if (StringUtils.isEmpty(codEEnteBenefIdUnivBenefCodiceIdUnivoco) || StringUtils.isBlank(codEEnteBenefIdUnivBenefCodiceIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codEEnteBenefIdUnivBenefCodiceIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codEEnteBenefIdUnivBenefCodiceIdUnivoco' obbligatorio");
		}

		if (StringUtils.isEmpty(deEEnteBenefDenominazioneBeneficiario) || StringUtils.isBlank(deEEnteBenefDenominazioneBeneficiario)) {
			log.error("Errore nell'inserimento esito: 'deEEnteBenefDenominazioneBeneficiario' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'deEEnteBenefDenominazioneBeneficiario' obbligatorio");
		}

		if (StringUtils.isEmpty(codESoggPagIdUnivPagTipoIdUnivoco) || StringUtils.isBlank(codESoggPagIdUnivPagTipoIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codESoggPagIdUnivPagTipoIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codESoggPagIdUnivPagTipoIdUnivoco' obbligatorio");
		}

		if (StringUtils.isEmpty(codESoggPagIdUnivPagCodiceIdUnivoco) || StringUtils.isBlank(codESoggPagIdUnivPagCodiceIdUnivoco)) {
			log.error("Errore nell'inserimento esito: 'codESoggPagIdUnivPagCodiceIdUnivoco' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codESoggPagIdUnivPagCodiceIdUnivoco' obbligatorio");
		}

		if (StringUtils.isEmpty(deESoggPagAnagraficaPagatore) || StringUtils.isBlank(deESoggPagAnagraficaPagatore)) {
			log.error("Errore nell'inserimento esito: 'deESoggPagAnagraficaPagatore' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'deESoggPagAnagraficaPagatore' obbligatorio");
		}

		if (StringUtils.isEmpty(codEDatiPagCodiceEsitoPagamento) || StringUtils.isBlank(codEDatiPagCodiceEsitoPagamento)) {
			log.error("Errore nell'inserimento esito: 'codEDatiPagCodiceEsitoPagamento' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codEDatiPagCodiceEsitoPagamento' obbligatorio");
		}

		if (numEDatiPagImportoTotalePagato == null) {
			log.error("Errore nell'inserimento esito: 'numEDatiPagImportoTotalePagato' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'numEDatiPagImportoTotalePagato' obbligatorio");
		}

		if (StringUtils.isEmpty(codEDatiPagIdUnivocoVersamento) || StringUtils.isBlank(codEDatiPagIdUnivocoVersamento)) {
			log.error("Errore nell'inserimento esito: 'codEDatiPagIdUnivocoVersamento' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codEDatiPagIdUnivocoVersamento' obbligatorio");
		}

		if (StringUtils.isEmpty(codEDatiPagCodiceContestoPagamento) || StringUtils.isBlank(codEDatiPagCodiceContestoPagamento)) {
			log.error("Errore nell'inserimento esito: 'codEDatiPagCodiceContestoPagamento' obbligatorio");
			throw new MandatoryFieldsException("Errore nell'inserimento esito: 'codEDatiPagCodiceContestoPagamento' obbligatorio");
		}

		mygovRpE.setDtCreazioneE(new Date()); // Metto data come se fosse creazione
		mygovRpE.setDtUltimaModificaE(new Date());
		mygovRpE.setCodESilinviaesitoIdDominio(codESilinviaesitoIdDominio);
		mygovRpE.setCodESilinviaesitoIdUnivocoVersamento(codESilinviaesitoIdUnivocoVersamento);
		mygovRpE.setCodESilinviaesitoCodiceContestoPagamento(codESilinviaesitoCodiceContestoPagamento);
		mygovRpE.setDeEVersioneOggetto(deEVersioneOggetto);
		mygovRpE.setCodEDomIdDominio(codEDomIdDominio);
		mygovRpE.setCodEDomIdStazioneRichiedente(codEDomIdStazioneRichiedente);
		mygovRpE.setCodEIdMessaggioRicevuta(codEIdMessaggioRicevuta);
		mygovRpE.setDtEDataOraMessaggioRicevuta(dtEDataOraMessaggioRicevuta);
		mygovRpE.setCodERiferimentoMessaggioRichiesta(codERiferimentoMessaggioRichiesta);
		mygovRpE.setDtERiferimentoDataRichiesta(dtERiferimentoDataRichiesta);
		mygovRpE.setCodEIstitAttesIdUnivAttesTipoIdUnivoco(codEIstitAttesIdUnivAttesTipoIdUnivoco);
		mygovRpE.setCodEIstitAttesIdUnivAttesCodiceIdUnivoco(codEIstitAttesIdUnivAttesCodiceIdUnivoco);
		mygovRpE.setDeEIstitAttesDenominazioneAttestante(deEIstitAttesDenominazioneAttestante);
		mygovRpE.setCodEIstitAttesCodiceUnitOperAttestante(codEIstitAttesCodiceUnitOperAttestante);
		mygovRpE.setDeEIstitAttesDenomUnitOperAttestante(deEIstitAttesDenomUnitOperAttestante);
		mygovRpE.setDeEIstitAttesIndirizzoAttestante(deEIstitAttesIndirizzoAttestante);
		mygovRpE.setDeEIstitAttesCivicoAttestante(deEIstitAttesCivicoAttestante);
		mygovRpE.setCodEIstitAttesCapAttestante(codEIstitAttesCapAttestante);
		mygovRpE.setDeEIstitAttesLocalitaAttestante(deEIstitAttesLocalitaAttestante);
		mygovRpE.setDeEIstitAttesProvinciaAttestante(deEIstitAttesProvinciaAttestante);
		mygovRpE.setCodEIstitAttesNazioneAttestante(codEIstitAttesNazioneAttestante);
		mygovRpE.setCodEEnteBenefIdUnivBenefTipoIdUnivoco(codEEnteBenefIdUnivBenefTipoIdUnivoco);
		mygovRpE.setCodEEnteBenefIdUnivBenefCodiceIdUnivoco(codEEnteBenefIdUnivBenefCodiceIdUnivoco);
		mygovRpE.setDeEEnteBenefDenominazioneBeneficiario(deEEnteBenefDenominazioneBeneficiario);
		mygovRpE.setCodEEnteBenefCodiceUnitOperBeneficiario(codEEnteBenefCodiceUnitOperBeneficiario);
		mygovRpE.setDeEEnteBenefDenomUnitOperBeneficiario(deEEnteBenefDenomUnitOperBeneficiario);
		mygovRpE.setDeEEnteBenefIndirizzoBeneficiario(deEEnteBenefIndirizzoBeneficiario);
		mygovRpE.setDeEEnteBenefCivicoBeneficiario(deEEnteBenefCivicoBeneficiario);
		mygovRpE.setCodEEnteBenefCapBeneficiario(codEEnteBenefCapBeneficiario);
		mygovRpE.setDeEEnteBenefLocalitaBeneficiario(deEEnteBenefLocalitaBeneficiario);
		mygovRpE.setDeEEnteBenefProvinciaBeneficiario(deEEnteBenefProvinciaBeneficiario);
		mygovRpE.setCodEEnteBenefNazioneBeneficiario(codEEnteBenefNazioneBeneficiario);
		mygovRpE.setCodESoggVersIdUnivVersTipoIdUnivoco(codESoggVersIdUnivVersTipoIdUnivoco);
		mygovRpE.setCodESoggVersIdUnivVersCodiceIdUnivoco(codESoggVersIdUnivVersCodiceIdUnivoco);
		mygovRpE.setDeESoggVersAnagraficaVersante(deESoggVersAnagraficaVersante);
		mygovRpE.setDeESoggVersIndirizzoVersante(deESoggVersIndirizzoVersante);
		mygovRpE.setDeESoggVersCivicoVersante(deESoggVersCivicoVersante);
		mygovRpE.setCodESoggVersCapVersante(codESoggVersCapVersante);
		mygovRpE.setDeESoggVersLocalitaVersante(deESoggVersLocalitaVersante);
		mygovRpE.setDeESoggVersProvinciaVersante(deESoggVersProvinciaVersante);
		mygovRpE.setCodESoggVersNazioneVersante(codESoggVersNazioneVersante);
		mygovRpE.setDeESoggVersEmailVersante(deESoggVersEmailVersante);
		mygovRpE.setCodESoggPagIdUnivPagTipoIdUnivoco(codESoggPagIdUnivPagTipoIdUnivoco);
		mygovRpE.setCodESoggPagIdUnivPagCodiceIdUnivoco(codESoggPagIdUnivPagCodiceIdUnivoco);
		mygovRpE.setDeESoggPagAnagraficaPagatore(deESoggPagAnagraficaPagatore);
		mygovRpE.setDeESoggPagIndirizzoPagatore(deESoggPagIndirizzoPagatore);
		mygovRpE.setDeESoggPagCivicoPagatore(deESoggPagCivicoPagatore);
		mygovRpE.setCodESoggPagCapPagatore(codESoggPagCapPagatore);
		mygovRpE.setDeESoggPagLocalitaPagatore(deESoggPagLocalitaPagatore);
		mygovRpE.setDeESoggPagProvinciaPagatore(deESoggPagProvinciaPagatore);
		mygovRpE.setCodESoggPagNazionePagatore(codESoggPagNazionePagatore);
		mygovRpE.setDeESoggPagEmailPagatore(deESoggPagEmailPagatore);
		mygovRpE.setCodEDatiPagCodiceEsitoPagamento(codEDatiPagCodiceEsitoPagamento);
		mygovRpE.setNumEDatiPagImportoTotalePagato(numEDatiPagImportoTotalePagato);
		mygovRpE.setCodEDatiPagIdUnivocoVersamento(codEDatiPagIdUnivocoVersamento);
		mygovRpE.setCodEDatiPagCodiceContestoPagamento(codEDatiPagCodiceContestoPagamento);

		getHibernateTemplate().update(mygovRpE);

		return mygovRpE;
	}

	@Override
	public MygovRpE updateRispostaEById(Long mygovRpEId, final String codAckE, final String deESilinviaesitoEsito, final String codESilinviaesitoFaultCode,
			final String deESilinviaesitoFaultString, final String codESilinviaesitoId, final String deESilinviaesitoDescription,
			final Integer codESilinviaesitoSerial, final String codESilinviaesitoOriginalFaultCode, final String deESilinviaesitoOriginalFaultString, 
			final String deESilinviaesitoOriginalFaultDescription) throws DataAccessException {

		log.debug("Invocato metodo updateRispostaEById PARAMETRI ::: " + "mygovRpEId = [" + mygovRpEId + "] ::: codAckE = [" + codAckE
				+ "] ::: deESilinviaesitoEsito = [" + deESilinviaesitoEsito + "] ::: codESilinviaesitoFaultCode = [" + codESilinviaesitoFaultCode
				+ "] ::: deESilinviaesitoFaultString = [" + deESilinviaesitoFaultString + "] ::: codESilinviaesitoId = [" + codESilinviaesitoId
				+ "] ::: deESilinviaesitoDescription = [" + deESilinviaesitoDescription + "] ::: codESilinviaesitoSerial = [" + codESilinviaesitoSerial
				+ "] ::: codESilinviaesitoOriginalFaultCode = [" + codESilinviaesitoOriginalFaultCode + "] ::: deESilinviaesitoOriginalFaultString = [" + deESilinviaesitoOriginalFaultString
				+ "] ::: deESilinviaesitoOriginalFaultDescription = [" + deESilinviaesitoOriginalFaultDescription + "]");

		MygovRpE mygovRpE = doGet(mygovRpEId);

		mygovRpE.setDtUltimaModificaE(new Date());
		mygovRpE.setCodAckE(codAckE);
		mygovRpE.setDeESilinviaesitoEsito(deESilinviaesitoEsito);
		mygovRpE.setCodESilinviaesitoFaultCode(codESilinviaesitoFaultCode);
		mygovRpE.setDeESilinviaesitoFaultString(deESilinviaesitoFaultString);
		mygovRpE.setCodESilinviaesitoId(codESilinviaesitoId);

		// Tronco deESilinviaesitoDescription se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(deESilinviaesitoDescription)) {
			if (deESilinviaesitoDescription.length() > 1024) {
				mygovRpE.setDeESilinviaesitoDescription(deESilinviaesitoDescription.substring(0, 1024));
			}
			else {
				mygovRpE.setDeESilinviaesitoDescription(deESilinviaesitoDescription);
			}
		}
		else {
			mygovRpE.setDeESilinviaesitoDescription(deESilinviaesitoDescription);
		}
		
		mygovRpE.setCodESilinviaesitoOriginalFaultCode(codESilinviaesitoOriginalFaultCode);
		mygovRpE.setDeESilinviaesitoOriginalFaultString(deESilinviaesitoOriginalFaultString);
		if (StringUtils.isNotBlank(deESilinviaesitoOriginalFaultDescription)) {
			if (deESilinviaesitoOriginalFaultDescription.length() > 1024) 
				mygovRpE.setDeESilinviaesitoOriginalFaultDescription(deESilinviaesitoOriginalFaultDescription.substring(0, 1024));
			else 
				mygovRpE.setDeESilinviaesitoOriginalFaultDescription(deESilinviaesitoOriginalFaultDescription);
		}
		

		mygovRpE.setCodESilinviaesitoSerial(codESilinviaesitoSerial);

		getHibernateTemplate().update(mygovRpE);

		return mygovRpE;

	}

	@Override
	public MygovRpE updateRispostaRpByRpId(final Long mygovRpEId, final String deRpSilinviarpEsito, final Integer codRpSilinviarpRedirect,
			final String codRpSilinviarpUrl, final String codRpSilinviarpFaultCode, final String deRpSilinviarpFaultString, final String codRpSilinviarpId,
			final String deRpSilinviarpDescription, final Integer codRpSilinviarpSerial, final String idSession, final String codRpSilinviarpOriginalFaultCode, 
			final String deRpSilinviarpOriginalFaultString, final String deRpSilinviarpOriginalFaultDescription) throws DataAccessException {

		log.debug("Invocato metodo updateRispostaRpById PARAMETRI ::: " + "mygovRpEId = [" + mygovRpEId + "] ::: deRpSilinviarpEsito = [" + deRpSilinviarpEsito
				+ "] ::: codRpSilinviarpRedirect = [" + codRpSilinviarpRedirect + "] ::: codRpSilinviarpUrl = [" + codRpSilinviarpUrl
				+ "] ::: codRpSilinviarpFaultCode = [" + codRpSilinviarpFaultCode + "] ::: deRpSilinviarpFaultString = [" + deRpSilinviarpFaultString
				+ "] ::: codRpSilinviarpId = [" + codRpSilinviarpId + "] ::: deRpSilinviarpDescription = [" + deRpSilinviarpDescription
				+ "] ::: codRpSilinviarpSerial = [" + codRpSilinviarpSerial + "] ::: idSession = [" + idSession + "] ::: "
				+ "codRpSilinviarpOriginalFaultCode = [" + codRpSilinviarpOriginalFaultCode + "] ::: "
				+ "deRpSilinviarpOriginalFaultString = [" + deRpSilinviarpOriginalFaultString + "] ::: "
				+ "deRpSilinviarpOriginalFaultDescription = [" + deRpSilinviarpOriginalFaultDescription + "] ::: ");

		MygovRpE mygovRpE = doGet(mygovRpEId);

		mygovRpE.setDtUltimaModificaRp(new Date());
		mygovRpE.setDeRpSilinviarpEsito(deRpSilinviarpEsito);
		mygovRpE.setCodRpSilinviarpRedirect(codRpSilinviarpRedirect);
		mygovRpE.setCodRpSilinviarpUrl(codRpSilinviarpUrl);
		mygovRpE.setCodRpSilinviarpFaultCode(codRpSilinviarpFaultCode);
		mygovRpE.setDeRpSilinviarpFaultString(deRpSilinviarpFaultString);

		mygovRpE.setCodRpSilinviarpId(codRpSilinviarpId);

		// Tronco deRpSilinviarpDescription se non è null e la lunghezza è maggiore di 1024
		if (StringUtils.isNotBlank(deRpSilinviarpDescription)) {
			if (deRpSilinviarpDescription.length() > 1024) {
				mygovRpE.setDeRpSilinviarpDescription(deRpSilinviarpDescription.substring(0, 1024));
			}
			else {
				mygovRpE.setDeRpSilinviarpDescription(deRpSilinviarpDescription);
			}
		}
		else {
			mygovRpE.setDeRpSilinviarpDescription(deRpSilinviarpDescription);
		}
		
		mygovRpE.setCodRpSilinviarpOriginalFaultCode(codRpSilinviarpOriginalFaultCode);
		mygovRpE.setDeRpSilinviarpOriginalFaultString(deRpSilinviarpOriginalFaultString);
		if (StringUtils.isNotBlank(deRpSilinviarpOriginalFaultDescription)) {
			if (deRpSilinviarpOriginalFaultDescription.length() > 1024) 
				mygovRpE.setDeRpSilinviarpOriginalFaultDescription(deRpSilinviarpOriginalFaultDescription.substring(0, 1024));
			else 
				mygovRpE.setDeRpSilinviarpOriginalFaultDescription(deRpSilinviarpOriginalFaultDescription);
		}

		mygovRpE.setCodRpSilinviarpSerial(codRpSilinviarpSerial);
		mygovRpE.setIdSession(idSession);

		log.debug("deRpSilinviarpEsito :" + deRpSilinviarpEsito);
		log.debug("codRpSilinviarpRedirect :" + codRpSilinviarpRedirect);
		log.debug("codRpSilinviarpUrl :" + codRpSilinviarpUrl);
		log.debug("codRpSilinviarpFaultCode :" + codRpSilinviarpFaultCode);
		log.debug("deRpSilinviarpFaultString :" + deRpSilinviarpFaultString);
		log.debug("codRpSilinviarpId :" + codRpSilinviarpId);
		log.debug("deRpSilinviarpDescription :" + deRpSilinviarpDescription);
		log.debug("codRpSilinviarpSerial :" + codRpSilinviarpSerial);
		log.debug("idSession :" + idSession);
		log.debug("codRpSilinviarpOriginalFaultCode :" + codRpSilinviarpOriginalFaultCode);
		log.debug("deRpSilinviarpOriginalFaultString :" + deRpSilinviarpOriginalFaultString);
		log.debug("deRpSilinviarpOriginalFaultDescription :" + deRpSilinviarpOriginalFaultDescription);
		
		getHibernateTemplate().update(mygovRpE);

		return mygovRpE;
	}
	


	@Override
	public List<MygovRpE> updateRispostaRpByRpCarrello(final MygovCarrelloRp mygovCarrelloRp, final String deRpSilinviarpEsito, 
			final String codRpSilinviarpUrl, final String codRpSilinviarpFaultCode, final String deRpSilinviarpFaultString, final String codRpSilinviarpId,
			final String deRpSilinviarpDescription, final Integer codRpSilinviarpSerial, final String codRpSilinviarpOriginalFaultCode, 
			final String deRpSilinviarpOriginalFaultString, final String deRpSilinviarpOriginalFaultDescription) throws DataAccessException {

		log.debug("Invocato metodo updateRispostaRpByCarrello PARAMETRI ::: " + "MygovCarrelloRpId = [" + mygovCarrelloRp.getMygovCarrelloRpId() + "] ::: deRpSilinviarpEsito = [" + deRpSilinviarpEsito
				+ "] ::: codRpSilinviarpUrl = [" + codRpSilinviarpUrl
				+ "] ::: codRpSilinviarpFaultCode = [" + codRpSilinviarpFaultCode + "] ::: deRpSilinviarpFaultString = [" + deRpSilinviarpFaultString
				+ "] ::: codRpSilinviarpId = [" + codRpSilinviarpId + "] ::: deRpSilinviarpDescription = [" + deRpSilinviarpDescription
				+ "] ::: codRpSilinviarpSerial = [" + codRpSilinviarpSerial + "] ::: " 
				+ "codRpSilinviarpOriginalFaultCode = [" + codRpSilinviarpOriginalFaultCode + "] ::: "
				+ "deRpSilinviarpOriginalFaultString = [" + deRpSilinviarpOriginalFaultString + "] ::: "
				+ "deRpSilinviarpOriginalFaultDescription = [" + deRpSilinviarpOriginalFaultDescription + "] ::: ");

		List<MygovRpE> mygovRpEList = doGetCarrello(mygovCarrelloRp);
		for (MygovRpE mygovRpE : mygovRpEList) {


			mygovRpE.setDtUltimaModificaRp(new Date());
			mygovRpE.setDeRpSilinviarpEsito(deRpSilinviarpEsito);
			mygovRpE.setCodRpSilinviarpUrl(codRpSilinviarpUrl);
			mygovRpE.setCodRpSilinviarpFaultCode(codRpSilinviarpFaultCode);
			mygovRpE.setDeRpSilinviarpFaultString(deRpSilinviarpFaultString);

			mygovRpE.setCodRpSilinviarpId(codRpSilinviarpId);

			// Tronco deRpSilinviarpDescription se non e null e la lunghezza e maggiore di 1024
			if (StringUtils.isNotBlank(deRpSilinviarpDescription)) {
				if (deRpSilinviarpDescription.length() > 1024) {
					mygovRpE.setDeRpSilinviarpDescription(deRpSilinviarpDescription.substring(0, 1024));
				}
				else {
					mygovRpE.setDeRpSilinviarpDescription(deRpSilinviarpDescription);
				}
			}
			else {
				mygovRpE.setDeRpSilinviarpDescription(deRpSilinviarpDescription);
			}
			
			mygovRpE.setCodRpSilinviarpOriginalFaultCode(codRpSilinviarpOriginalFaultCode);
			mygovRpE.setDeRpSilinviarpOriginalFaultString(deRpSilinviarpOriginalFaultString);
			if (StringUtils.isNotBlank(deRpSilinviarpOriginalFaultDescription)) {
				if (deRpSilinviarpOriginalFaultDescription.length() > 1024) 
					mygovRpE.setDeRpSilinviarpOriginalFaultDescription(deRpSilinviarpOriginalFaultDescription.substring(0, 1024));
				else 
					mygovRpE.setDeRpSilinviarpOriginalFaultDescription(deRpSilinviarpOriginalFaultDescription);
			}

			mygovRpE.setCodRpSilinviarpSerial(codRpSilinviarpSerial);

			log.debug("deRpSilinviarpEsito :" + deRpSilinviarpEsito);
			log.debug("codRpSilinviarpUrl :" + codRpSilinviarpUrl);
			log.debug("codRpSilinviarpFaultCode :" + codRpSilinviarpFaultCode);
			log.debug("deRpSilinviarpFaultString :" + deRpSilinviarpFaultString);
			log.debug("codRpSilinviarpId :" + codRpSilinviarpId);
			log.debug("deRpSilinviarpDescription :" + deRpSilinviarpDescription);
			log.debug("codRpSilinviarpSerial :" + codRpSilinviarpSerial);
			log.debug("codRpSilinviarpOriginalFaultCode :" + codRpSilinviarpOriginalFaultCode);
			log.debug("deRpSilinviarpOriginalFaultString :" + deRpSilinviarpOriginalFaultString);
			log.debug("deRpSilinviarpOriginalFaultDescription :" + deRpSilinviarpOriginalFaultDescription);

			getHibernateTemplate().update(mygovRpE);
		}

		return mygovRpEList;
	}

	private MygovRpE doInsertRP(final String codRpSilinviarpIdPsp, final String codRpSilinviarpIdIntermediarioPsp, final String codRpSilinviarpIdCanale,
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
			final Integer modelloPagamento) throws DataAccessException {

		MygovRpE mygovRpE = new MygovRpE();

		mygovRpE.setDtCreazioneRp(new Date());
		mygovRpE.setDtUltimaModificaRp(new Date());
		mygovRpE.setCodRpSilinviarpIdPsp(codRpSilinviarpIdPsp);
		mygovRpE.setCodRpSilinviarpIdIntermediarioPsp(codRpSilinviarpIdIntermediarioPsp);
		mygovRpE.setCodRpSilinviarpIdCanale(codRpSilinviarpIdCanale);
		mygovRpE.setCodRpSilinviarpIdDominio(codRpSilinviarpIdDominio);
		mygovRpE.setCodRpSilinviarpIdUnivocoVersamento(codRpSilinviarpIdUnivocoVersamento);
		mygovRpE.setCodRpSilinviarpCodiceContestoPagamento(codRpSilinviarpCodiceContestoPagamento);
		mygovRpE.setDeRpVersioneOggetto(deRpVersioneOggetto);
		mygovRpE.setCodRpDomIdDominio(codRpDomIdDominio);
		mygovRpE.setCodRpDomIdStazioneRichiedente(codRpDomIdStazioneRichiedente);
		mygovRpE.setCodRpIdMessaggioRichiesta(codRpIdMessaggioRichiesta);
		mygovRpE.setDtRpDataOraMessaggioRichiesta(dtRpDataOraMessaggioRichiesta);
		mygovRpE.setCodRpAutenticazioneSoggetto(codRpAutenticazioneSoggetto);
		mygovRpE.setCodRpSoggVersIdUnivVersTipoIdUnivoco(codRpSoggVersIdUnivVersTipoIdUnivoco);
		mygovRpE.setCodRpSoggVersIdUnivVersCodiceIdUnivoco(codRpSoggVersIdUnivVersCodiceIdUnivoco);
		mygovRpE.setDeRpSoggVersAnagraficaVersante(deRpSoggVersAnagraficaVersante);
		mygovRpE.setDeRpSoggVersIndirizzoVersante(deRpSoggVersIndirizzoVersante);
		mygovRpE.setDeRpSoggVersCivicoVersante(deRpSoggVersCivicoVersante);
		mygovRpE.setCodRpSoggVersCapVersante(codRpSoggVersCapVersante);
		mygovRpE.setDeRpSoggVersLocalitaVersante(deRpSoggVersLocalitaVersante);
		mygovRpE.setDeRpSoggVersProvinciaVersante(deRpSoggVersProvinciaVersante);
		mygovRpE.setCodRpSoggVersNazioneVersante(codRpSoggVersNazioneVersante);
		mygovRpE.setDeRpSoggVersEmailVersante(deRpSoggVersEmailVersante);
		mygovRpE.setCodRpSoggPagIdUnivPagTipoIdUnivoco(codRpSoggPagIdUnivPagTipoIdUnivoco);
		mygovRpE.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(codRpSoggPagIdUnivPagCodiceIdUnivoco);
		mygovRpE.setDeRpSoggPagAnagraficaPagatore(deRpSoggPagAnagraficaPagatore);
		mygovRpE.setDeRpSoggPagIndirizzoPagatore(deRpSoggPagIndirizzoPagatore);
		mygovRpE.setDeRpSoggPagCivicoPagatore(deRpSoggPagCivicoPagatore);
		mygovRpE.setCodRpSoggPagCapPagatore(codRpSoggPagCapPagatore);
		mygovRpE.setDeRpSoggPagLocalitaPagatore(deRpSoggPagLocalitaPagatore);
		mygovRpE.setDeRpSoggPagProvinciaPagatore(deRpSoggPagProvinciaPagatore);
		mygovRpE.setCodRpSoggPagNazionePagatore(codRpSoggPagNazionePagatore);
		mygovRpE.setDeRpSoggPagEmailPagatore(deRpSoggPagEmailPagatore);
		mygovRpE.setDtRpDatiVersDataEsecuzionePagamento(dtRpDatiVersDataEsecuzionePagamento);
		mygovRpE.setNumRpDatiVersImportoTotaleDaVersare(numRpDatiVersImportoTotaleDaVersare);
		mygovRpE.setCodRpDatiVersTipoVersamento(codRpDatiVersTipoVersamento);
		mygovRpE.setCodRpDatiVersIdUnivocoVersamento(codRpDatiVersIdUnivocoVersamento);
		mygovRpE.setCodRpDatiVersCodiceContestoPagamento(codRpDatiVersCodiceContestoPagamento);
		mygovRpE.setDeRpDatiVersIbanAddebito(deRpDatiVersIbanAddebito);
		mygovRpE.setDeRpDatiVersBicAddebito(deRpDatiVersBicAddebito);
		mygovRpE.setModelloPagamento(modelloPagamento);

		getHibernateTemplate().save(mygovRpE);
		return mygovRpE;
	}

	public MygovRpE doGet(final long mygovRpEId) throws DataAccessException {

		log.debug("Invocato metodo doGet PARAMETRI ::: " + "mygovRpEId = [" + mygovRpEId);

		return getHibernateTemplate().get(MygovRpE.class, mygovRpEId);
	}
	
	@SuppressWarnings("unchecked")
	public List<MygovRpE> doGetCarrello(final MygovCarrelloRp mygovCarrelloRp) throws DataAccessException {

		List<MygovRpE> results = getHibernateTemplate().find("from MygovRpE mygovRpE where mygovRpE.mygov_carrello_rp = ?", mygovCarrelloRp);
		if (results.size() == 0)
			throw new IncorrectResultSizeDataAccessException("non esistono rp_e associate al carrello", 1, results.size());

		return results;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RpEDao#getRpByIdSession(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MygovRpE getRpByIdSession(final String idSession) throws DataAccessException {

		//		List<MygovRpE> results = getHibernateTemplate().find("from MygovRpE WHERE idSession = '" + idSession + "'");

		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRpE.class);
		criteria.add(Restrictions.eq("idSession", idSession));
		List<MygovRpE> results = getHibernateTemplate().findByCriteria(criteria);

		if (results.size() > 1)
			throw new IncorrectResultSizeDataAccessException("Piu di una RPE ritornata per idSession", 1, results.size());
		return results.size() == 0 ? null : (MygovRpE) results.get(0);
	}

	@Override
	public MygovRpE updateECodEsitoPagamentoById(long rpEId, String esito) throws DataAccessException {

		MygovRpE mygovRpE = doGet(rpEId);

		mygovRpE.setCodEDatiPagCodiceEsitoPagamento(esito);

		getHibernateTemplate().update(mygovRpE);

		return mygovRpE;
	}

	@Override
	public void deleteByIdSession(String idSession) {

		MygovRpE mygovRpE = getRpByIdSession(idSession);

		List<MygovRpEDettaglio> dettaglios = rpEDettaglioDao.getByRpE(mygovRpE);
		for (MygovRpEDettaglio mygovRpEDettaglio : dettaglios) {
			getHibernateTemplate().delete(mygovRpEDettaglio);
		}

		getHibernateTemplate().delete(mygovRpE);

	}

	@Override
	public MygovRpE updateCarrelloRef(Long mygovRpEId, MygovCarrelloRp mygovCarrelloRp) {
		MygovRpE mygovRpE = doGet(mygovRpEId);

		mygovRpE.setMygov_carrello_rp(mygovCarrelloRp);

		getHibernateTemplate().update(mygovRpE);

		return mygovRpE;
	}

}
