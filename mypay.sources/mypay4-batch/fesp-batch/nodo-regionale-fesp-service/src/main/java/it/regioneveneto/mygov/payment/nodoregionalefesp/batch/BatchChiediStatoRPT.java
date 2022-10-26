package it.regioneveneto.mygov.payment.nodoregionalefesp.batch;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.ws.Holder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.telematici.pagamenti.ws.nodoregionalepernodospc.EsitoPaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.PaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.PaaInviaRTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.EsitoChiediStatoRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import gov.telematici.pagamenti.ws.ppthead.IntestazionePPT;
import it.gov.digitpa.schemas.x2011.pagamenti.CtDatiVersamentoRT;
import it.gov.digitpa.schemas.x2011.pagamenti.CtDominio;
import it.gov.digitpa.schemas.x2011.pagamenti.CtEnteBeneficiario;
import it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivoco;
import it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG;
import it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaG;
import it.gov.digitpa.schemas.x2011.pagamenti.CtIstitutoAttestante;
import it.gov.digitpa.schemas.x2011.pagamenti.CtRicevutaTelematica;
import it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoPagatore;
import it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoVersante;
import it.gov.digitpa.schemas.x2011.pagamenti.RTDocument;
import it.gov.digitpa.schemas.x2011.pagamenti.StCodiceEsitoPagamento;
import it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivoco;
import it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersFG;
import it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersG;
import it.regioneveneto.mygov.payment.nodoregionalefesp.client.PagamentiTelematiciRTServiceClient;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils.ChiediStatoRTPBean;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils.FespBean;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.FailedBatchExecutionException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPTRTService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.utils.FaultCodeChiediStatoRPT;
import it.regioneveneto.mygov.payment.nodoregionalefesp.utils.FaultCodeInvioRPT;
import it.regioneveneto.mygov.payment.nodoregionalefesp.utils.StatiRPT;
import it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient;

/**
 * @author regione del veneto
 * 
 */
public class BatchChiediStatoRPT {

	/**
	 * 
	 */
	private static final Log log = LogFactory.getLog(BatchChiediStatoRPT.class);

	/**
	 * 
	 */
	private static final String FORZA_GENERAZIONE_RT_NEGATIVA = "FORZA_GENERAZIONE_RT_NEGATIVA";

	/**
	 * Contesto spring del batch
	 */
	private static ApplicationContext context = null;

	/**
	 * Service per le comunicazioni esterne
	 */
	private static PagamentiTelematiciRPTServiceClient pagamentiTelematiciRPTServiceClient;

	/**
	 * Service per le comunicazioni con FESP SERVER
	 */
	private static PagamentiTelematiciRTServiceClient pagamentiTelematiciRTServiceClient;

	/**
	 * Service per manageRTP
	 */
	private static ManageRPTRTService manageRPTRTService;

	/**
	 * Bean con le properties dell'intermediario
	 */
	private static ChiediStatoRTPBean chiediStatoRTPBean;

	/**
	 * 
	 */
	private static FespBean fespProperties;

	/**
	 * 
	 */
	public BatchChiediStatoRPT() {
		super();
	}

	/**
	 * @param args
	 * @throws FailedBatchExecutionException
	 */
	public static void main(String[] args) throws FailedBatchExecutionException {

		Date start = new Date();

		log.info("Inizio esecuzione di BatchChiediStatoRPT - [" + start + "]");

		// Caricamento del contesto Spring
		log.info("Caricamento del contesto Spring");

		String[] configLocations = new String[1];
		configLocations[0] = "stato-rpt-service.xml";

		BatchChiediStatoRPT.context = new ClassPathXmlApplicationContext(configLocations);

		// Recupero dal contesto i bean dei Service necessari
		log.info("Recupero dal contesto i bean dei Service necessari");

		BatchChiediStatoRPT.pagamentiTelematiciRPTServiceClient = (PagamentiTelematiciRPTServiceClient) BatchChiediStatoRPT.context.getBean("pagamentiTelematiciRPTServiceClient");
		BatchChiediStatoRPT.pagamentiTelematiciRTServiceClient = (PagamentiTelematiciRTServiceClient) BatchChiediStatoRPT.context.getBean("pagamentiTelematiciRTServiceClient");
		BatchChiediStatoRPT.manageRPTRTService = (ManageRPTRTService) BatchChiediStatoRPT.context.getBean("manageRPTRTService");
		BatchChiediStatoRPT.chiediStatoRTPBean = (ChiediStatoRTPBean) BatchChiediStatoRPT.context.getBean("chiediStatoRTPBean");
		BatchChiediStatoRPT.fespProperties = (FespBean) BatchChiediStatoRPT.context.getBean("fespBean");

		int intervalloMinutiModelloImmediato = BatchChiediStatoRPT.chiediStatoRTPBean.getIntervalloMinutiModelloImmediato();
		int intervalloMinutiModelloDifferito = BatchChiediStatoRPT.chiediStatoRTPBean.getIntervalloMinutiModelloDifferito();
		int intervalloMinutiModelloAttivatoPressoPsp = BatchChiediStatoRPT.chiediStatoRTPBean.getIntervalloMinutiModelloAttivatoPressoPsp();

		//****
		// ELABORAZIONE RPT MODELLO 0: IMMEDIATO
		//****

		log.info("Inizio elaborazione RPT pendenti - MODELLO IMMEDIATO");

		try {
			elaboraRptPendenti(0, intervalloMinutiModelloImmediato);
		}
		catch (Exception ex) {
			log.error("Errore durante elaborazione RTP pendenti - MODELLO IMMEDIATO", ex);

			throw new FailedBatchExecutionException("Errore durante elaborazione RTP pendenti - MODELLO IMMEDIATO ", ex);
		}

		//****
		// ELABORAZIONE RPT MODELLO 1: IMMEDIATO MULTICARRELLO
		//****

		log.info("Inizio elaborazione RPT pendenti - MODELLO IMMEDIATO MULTICARRELLO");

		try {
			elaboraRptPendenti(1, intervalloMinutiModelloImmediato);
		}
		catch (Exception ex) {
			log.error("Errore durante elaborazione RTP pendenti - MODELLO IMMEDIATO MULTICARRELLO", ex);

			throw new FailedBatchExecutionException("Errore durante elaborazione RTP pendenti - MODELLO IMMEDIATO MULTICARRELLO", ex);
		}

		//****
		// ELABORAZIONE RPT MODELLO 2: DIFFERITO
		//****

		log.info("Inizio elaborazione RPT pendenti - MODELLO DIFFERITO");

		try {
			elaboraRptPendenti(2, intervalloMinutiModelloDifferito);
		}
		catch (Exception ex) {
			log.error("Errore durante elaborazione RTP pendenti - MODELLO DIFFERITO", ex);

			throw new FailedBatchExecutionException("Errore durante elaborazione RTP pendenti - MODELLO DIFFERITO ", ex);
		}

		//****
		// ELABORAZIONE RPT MODELLO 4: ATTIVATO PRESSO PSP
		//****

		log.info("Inizio elaborazione RPT pendenti - MODELLO ATTIVATO PRESSO PSP");

		try {
			elaboraRptPendenti(4, intervalloMinutiModelloAttivatoPressoPsp);
		}
		catch (Exception ex) {
			log.error("Errore durante elaborazione RTP pendenti - MODELLO ATTIVATO PRESSO PSP", ex);

			throw new FailedBatchExecutionException("Errore durante elaborazione RTP pendenti - MODELLO ATTIVATO PRESSO PSP ", ex);
		}

		Date end = new Date();

		log.info("Fine esecuzione di BatchChiediStatoRPT - [" + end + "]");
	}

	/**
	 * @param modelloPagamento
	 * @param intervalloMinuti
	 * @throws FailedBatchExecutionException
	 */
	private static void elaboraRptPendenti(int modelloPagamento, int intervalloMinuti) throws FailedBatchExecutionException {

		List<MygovRptRt> mygovRptRtList = new ArrayList<MygovRptRt>();

		try {
			mygovRptRtList = BatchChiediStatoRPT.manageRPTRTService.findAllRptPendenti(modelloPagamento, intervalloMinuti);
		}
		catch (Exception ex) {
			log.error("Errore durante recupero elenco RTP - MODELLO " + modelloPagamento, ex);

			throw new FailedBatchExecutionException("Problemi nel recupero RPT - MODELLO " + modelloPagamento + " ", ex);
		}

		for (MygovRptRt mygovRptRt : mygovRptRtList) {

			try {

				log.info("");
				log.info("*****************************************************************");
				log.info("Elaborazione singola RPT (MODELLO " + modelloPagamento + ") con IUV : [" + mygovRptRt.getCodRptDatiVersIdUnivocoVersamento() + "]");
				log.info("******************************************************************");

				Boolean chiediStatoRPT = false;
				//				Boolean chiediCopiaRT = false;
				Boolean generaRTNegativa = false;
				Boolean riattivaRPT = false;
				//				Boolean riabilitaAttivaRPT = false;

				switch (modelloPagamento) {

				case 0: //MODELLO 1 - IMMEDIATO

					if (StringUtils.isNotBlank(mygovRptRt.getCodRptInviarptFaultCode())) {
						//invio RPT KO

						if (chiediStatoRTPBean.isForzaGenerazioneRtNegativa() && mygovRptRt.getCodRptInviarptFaultCode() != null
								&& mygovRptRt.getCodRptInviarptFaultCode().equals(FORZA_GENERAZIONE_RT_NEGATIVA)) {
							generaRTNegativa = true;
							break;
						}

						FaultCodeInvioRPT faultCodeInvioRPT = FaultCodeInvioRPT.valueOf(mygovRptRt.getCodRptInviarptFaultCode());

						switch (faultCodeInvioRPT) {

						case PPT_RPT_DUPLICATA:
							continue;
						case PPT_SINTASSI_XSD:
							generaRTNegativa = true;
							break;
						case PPT_SINTASSI_EXTRAXSD:
							generaRTNegativa = true;
							break;
						case PPT_AUTENTICAZIONE:
							generaRTNegativa = true;
							break;
						case PPT_AUTORIZZAZIONE:
							generaRTNegativa = true;
							break;
						case PPT_SEMANTICA:
							generaRTNegativa = true;
							break;
						case PPT_DOMINIO_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_DOMINIO_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_STAZIONE_INT_PA_SCONOSCIUTA:
							generaRTNegativa = true;
							break;
						case PPT_STAZIONE_INT_PA_DISABILITATA:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PA_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PA_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_IRRAGGIUNGIBILE:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_SERVIZIO_NONATTIVO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_NONRISOLVIBILE:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_INDISPONIBILE:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_ERR_PARAM_PAG_IMM:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PSP_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PSP_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_PSP_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_PSP_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_SUPERAMENTOSOGLIA:
							generaRTNegativa = true;
							break;
						case PPT_TIPOFIRMA_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_ERRORE_FORMATO_BUSTA_FIRMATA:
							generaRTNegativa = true;
							break;
						case PPT_FIRMA_INDISPONIBILE:
							generaRTNegativa = true;
							break;
						case PPT_SYSTEM_ERROR:
							generaRTNegativa = true;
							break;
						case PPT_IBAN_NON_CENSITO:
							generaRTNegativa = true;
							break;
						default:
							chiediStatoRPT = true;
							break;
						}
					}
					else {
						//invio RPT OK

						//							chiediStatoRPT = true;
						continue;
					}

					break;

				case 1: //MODELLO 1 - IMMEDIATO MULTICARRELLO (NON IMPLEMENTATO)

					if (StringUtils.isNotBlank(mygovRptRt.getCodRptInviarptFaultCode())) {
						//invio RPT KO

						if (chiediStatoRTPBean.isForzaGenerazioneRtNegativa() && mygovRptRt.getCodRptInviarptFaultCode() != null
								&& mygovRptRt.getCodRptInviarptFaultCode().equals(FORZA_GENERAZIONE_RT_NEGATIVA)) {
							generaRTNegativa = true;
							break;
						}

						FaultCodeInvioRPT faultCodeInvioRPT = FaultCodeInvioRPT.valueOf(mygovRptRt.getCodRptInviarptFaultCode());

						switch (faultCodeInvioRPT) {

						case PPT_RPT_DUPLICATA:
							continue;
						case PPT_SINTASSI_XSD:
							generaRTNegativa = true;
							break;
						case PPT_SINTASSI_EXTRAXSD:
							generaRTNegativa = true;
							break;
						case PPT_AUTENTICAZIONE:
							generaRTNegativa = true;
							break;
						case PPT_AUTORIZZAZIONE:
							generaRTNegativa = true;
							break;
						case PPT_SEMANTICA:
							generaRTNegativa = true;
							break;
						case PPT_DOMINIO_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_DOMINIO_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_STAZIONE_INT_PA_SCONOSCIUTA:
							generaRTNegativa = true;
							break;
						case PPT_STAZIONE_INT_PA_DISABILITATA:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PA_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PA_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_IRRAGGIUNGIBILE:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_SERVIZIO_NONATTIVO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_NONRISOLVIBILE:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_INDISPONIBILE:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_ERR_PARAM_PAG_IMM:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PSP_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PSP_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_PSP_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_PSP_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_SUPERAMENTOSOGLIA:
							generaRTNegativa = true;
							break;
						case PPT_TIPOFIRMA_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_ERRORE_FORMATO_BUSTA_FIRMATA:
							generaRTNegativa = true;
							break;
						case PPT_FIRMA_INDISPONIBILE:
							generaRTNegativa = true;
							break;
						case PPT_SYSTEM_ERROR:
							generaRTNegativa = true;
							break;
						case PPT_IBAN_NON_CENSITO:
							generaRTNegativa = true;
							break;
						default:
							chiediStatoRPT = true;
							break;
						}
					}
					else {
						//invio RPT OK

						//							chiediStatoRPT = true;
						continue;
					}

					break;

				case 2: //MODELLO 2 - DIFFERITO

					if (StringUtils.isNotBlank(mygovRptRt.getCodRptInviarptFaultCode())) {
						//invio RPT KO

						if (chiediStatoRTPBean.isForzaGenerazioneRtNegativa() && mygovRptRt.getCodRptInviarptFaultCode() != null
								&& mygovRptRt.getCodRptInviarptFaultCode().equals(FORZA_GENERAZIONE_RT_NEGATIVA)) {
							generaRTNegativa = true;
							break;
						}

						FaultCodeInvioRPT faultCodeInvioRPT = FaultCodeInvioRPT.valueOf(mygovRptRt.getCodRptInviarptFaultCode());

						switch (faultCodeInvioRPT) {

						case PPT_RPT_DUPLICATA:
							continue;
						case PPT_SINTASSI_XSD:
							generaRTNegativa = true;
							break;
						case PPT_SINTASSI_EXTRAXSD:
							generaRTNegativa = true;
							break;
						case PPT_AUTENTICAZIONE:
							generaRTNegativa = true;
							break;
						case PPT_AUTORIZZAZIONE:
							generaRTNegativa = true;
							break;
						case PPT_SEMANTICA:
							generaRTNegativa = true;
							break;
						case PPT_DOMINIO_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_DOMINIO_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_STAZIONE_INT_PA_SCONOSCIUTA:
							generaRTNegativa = true;
							break;
						case PPT_STAZIONE_INT_PA_DISABILITATA:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PA_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PA_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_IRRAGGIUNGIBILE:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_SERVIZIO_NONATTIVO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_NONRISOLVIBILE:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_INDISPONIBILE:
							generaRTNegativa = true;
							break;
						case PPT_CANALE_ERR_PARAM_PAG_IMM:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PSP_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_INTERMEDIARIO_PSP_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_PSP_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_PSP_DISABILITATO:
							generaRTNegativa = true;
							break;
						case PPT_SUPERAMENTOSOGLIA:
							generaRTNegativa = true;
							break;
						case PPT_TIPOFIRMA_SCONOSCIUTO:
							generaRTNegativa = true;
							break;
						case PPT_ERRORE_FORMATO_BUSTA_FIRMATA:
							generaRTNegativa = true;
							break;
						case PPT_FIRMA_INDISPONIBILE:
							generaRTNegativa = true;
							break;
						case PPT_SYSTEM_ERROR:
							chiediStatoRPT = true;
							break;
						default:
							chiediStatoRPT = true;
							break;
						}
					}
					else {
						//invio RPT OK

						//							chiediStatoRPT = true;
						continue;
					}

					break;

				case 4: //MODELLO 3 - ATTIVATO PRESSO PSP

					if (chiediStatoRTPBean.isForzaGenerazioneRtNegativa() && mygovRptRt.getCodRptInviarptFaultCode() != null
							&& mygovRptRt.getCodRptInviarptFaultCode().equals(FORZA_GENERAZIONE_RT_NEGATIVA)) {
						generaRTNegativa = true;
						break;
					}

					if ((FaultCodeInvioRPT.PPT_CANALE_TIMEOUT.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
							|| FaultCodeInvioRPT.PPT_CANALE_ERRORE_RESPONSE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
							|| FaultCodeInvioRPT.PPT_CANALE_ERRORE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
							|| FaultCodeInvioRPT.PPT_ESITO_SCONOSCIUTO.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
							|| FaultCodeInvioRPT.PPT_SYSTEM_ERROR.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())))
						chiediStatoRPT = true;

					break;

				default:
					break;
				}

				String identificativoDominio = mygovRptRt.getCodRptInviarptIdDominio();
				String identificativoUnivocoVersamento = mygovRptRt.getCodRptInviarptIdUnivocoVersamento();
				String codiceContestoPagamento = mygovRptRt.getCodRptInviarptCodiceContestoPagamento();

				//******************
				//CHIEDI STATO RPT
				//******************

				if (chiediStatoRPT) {

					String identificativoIntermediarioPA = BatchChiediStatoRPT.fespProperties.getIdentificativoIntermediarioPa();
					String identificativoStazioneIntermediarioPA = BatchChiediStatoRPT.fespProperties.getIdentificativoStazioneIntermediarioPa();
					String password = BatchChiediStatoRPT.fespProperties.getPassword();

					StatiRPT statoRPT = null;
					FaultCodeChiediStatoRPT faultCodeChiediStatoRPT = null;

					Holder<FaultBean> fault = new Holder<FaultBean>();
					Holder<EsitoChiediStatoRPT> esito = new Holder<EsitoChiediStatoRPT>();

					//Sleep 2 secondi prima di ogni chiamata
					Thread.sleep(2000);

					log.debug("Chiamo nodoChiediStatoRPT con i parametri: identificativoIntermediarioPa [" + identificativoIntermediarioPA
							+ "], identificativoStazioneIntermediarioPA [" + identificativoStazioneIntermediarioPA + "], password [" + password
							+ "], identificativoDominio [" + identificativoDominio + "], identificativoUnivocoVersamento [" + identificativoUnivocoVersamento
							+ "], codiceContestoPagamento [" + codiceContestoPagamento + "]");

					BatchChiediStatoRPT.pagamentiTelematiciRPTServiceClient.nodoChiediStatoRPT(identificativoIntermediarioPA,
							identificativoStazioneIntermediarioPA, password, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
							fault, esito);

					if (fault.value != null) {

						log.debug("ERROR in nodoChiediStatoRPT per IUV [" + identificativoUnivocoVersamento + "] faultCode [" + fault.value.getFaultCode()
								+ "] faultString [" + fault.value.getFaultString() + "] description [" + fault.value.getDescription() + "]");

						faultCodeChiediStatoRPT = FaultCodeChiediStatoRPT.valueOf(fault.value.getFaultCode());
						faultCodeChiediStatoRPT.setFaultString(fault.value.getFaultString());
						faultCodeChiediStatoRPT.setDescription(fault.value.getDescription());
					}
					else {

						EsitoChiediStatoRPT esitoChiediStatoRPT = esito.value;
						String stato = esitoChiediStatoRPT.getStato();

						log.debug("Ricevuta risposta nodoChiediStatoRPT per IUV [" + identificativoUnivocoVersamento + "] stato ricevuto [" + stato + "]");

						statoRPT = StatiRPT.valueOf(stato);
					}

					//VALUTAZIONE AZIONI IN BASE A RISPOSTA CHIEDI STATO

					switch (modelloPagamento) {

					case 0: //MODELLO 1 - IMMEDIATO

						//							if ((FaultCodeInvioRPT.PPT_CANALE_TIMEOUT.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_CANALE_ERRORE_RESPONSE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_CANALE_ERRORE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_ESITO_SCONOSCIUTO.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode()))) {
						if ((faultCodeChiediStatoRPT != null
								&& FaultCodeChiediStatoRPT.PPT_RPT_SCONOSCIUTA.getFaultCode().equals(faultCodeChiediStatoRPT.getFaultCode()))
								|| (statoRPT != null && (StatiRPT.RPT_RIFIUTATA_NODO.equals(statoRPT) || StatiRPT.RPT_RIFIUTATA_PSP.equals(statoRPT)
										|| StatiRPT.RPT_ERRORE_INVIO_A_PSP.equals(statoRPT))))
							generaRTNegativa = true;
						//							}

						break;

					case 1: //MODELLO 1 - IMMEDIATO MULTICARRELLO (NON IMPLEMENTATO)

						//						if ((FaultCodeInvioRPT.PPT_CANALE_TIMEOUT.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_CANALE_ERRORE_RESPONSE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_CANALE_ERRORE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_ESITO_SCONOSCIUTO.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode()))) {
						if ((faultCodeChiediStatoRPT != null
								&& FaultCodeChiediStatoRPT.PPT_RPT_SCONOSCIUTA.getFaultCode().equals(faultCodeChiediStatoRPT.getFaultCode()))
								|| (statoRPT != null && (StatiRPT.RPT_RIFIUTATA_NODO.equals(statoRPT) || StatiRPT.RPT_RIFIUTATA_PSP.equals(statoRPT)
										|| StatiRPT.RPT_ERRORE_INVIO_A_PSP.equals(statoRPT))))
							generaRTNegativa = true;
						//							}

						break;

					case 2: //MODELLO 2 - DIFFERITO

						//							if ((FaultCodeInvioRPT.PPT_CANALE_TIMEOUT.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_CANALE_ERRORE_RESPONSE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_CANALE_ERRORE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_ESITO_SCONOSCIUTO.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode()))) {
						if (faultCodeChiediStatoRPT != null
								&& FaultCodeChiediStatoRPT.PPT_RPT_SCONOSCIUTA.getFaultCode().equals(faultCodeChiediStatoRPT.getFaultCode()))
							riattivaRPT = true;
						else if ((statoRPT != null && (StatiRPT.RPT_RIFIUTATA_NODO.equals(statoRPT) || StatiRPT.RPT_RIFIUTATA_PSP.equals(statoRPT)
								|| StatiRPT.RPT_ERRORE_INVIO_A_PSP.equals(statoRPT))))
							generaRTNegativa = true;
						//							}

						break;

					case 4: //MODELLO 3 - ATTIVATO PRESSO PSP

						//							if ((FaultCodeInvioRPT.PPT_CANALE_TIMEOUT.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_CANALE_ERRORE_RESPONSE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_CANALE_ERRORE.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode())
						//									|| FaultCodeInvioRPT.PPT_ESITO_SCONOSCIUTO.getFaultCode().equals(mygovRptRt.getCodRptInviarptFaultCode()))) {
						if (faultCodeChiediStatoRPT != null
								&& FaultCodeChiediStatoRPT.PPT_RPT_SCONOSCIUTA.getFaultCode().equals(faultCodeChiediStatoRPT.getFaultCode()))
							riattivaRPT = true;
						//							}

						break;

					default:
						break;
					}
				}

				//				Assert.isTrue(!(chiediCopiaRT && generaRTNegativa), "Azioni chiediCopiaRT e generaRTNegativa non compatibili.");

				HashMap<String, Object> paramsHashMap = null;

				//				if (chiediCopiaRT)
				//					paramsHashMap = chiediCopiaRT(mygovRptRt);

				if (generaRTNegativa)
					paramsHashMap = creaRTNegativa(mygovRptRt);

				Boolean inviaRT = paramsHashMap != null;

				//******************
				//AZIONI DISPOSITIVE
				//******************

				if (inviaRT) {

					PaaInviaRT bodyrichiesta = (PaaInviaRT) paramsHashMap.get("bodyrichiesta");
					IntestazionePPT intestazionePPT = (IntestazionePPT) paramsHashMap.get("intestazionePPT");

					//Sleep 2 secondi prima di ogni chiamata
					Thread.sleep(2000);

					PaaInviaRTRisposta paaInviaRTRisposta;

					try {
						paaInviaRTRisposta = BatchChiediStatoRPT.pagamentiTelematiciRTServiceClient.paaInviaRT(bodyrichiesta, intestazionePPT);
					}
					catch (Exception ex) {

						log.error("Errore nella richiesta paaInviaRT con parametri:  identificativoDominio [" + identificativoDominio
								+ "], identificativoUnivocoVersamento [" + identificativoUnivocoVersamento + "], codiceContestoPagamento ["
								+ codiceContestoPagamento + "]", ex);

						return;
					}

					EsitoPaaInviaRT esitoPaaInviaRT = paaInviaRTRisposta.getPaaInviaRTRisposta();
					if (esitoPaaInviaRT.getFault() != null) {

						log.debug("ERROR in paaInviaRT per IUV [" + intestazionePPT.getIdentificativoUnivocoVersamento() + "] faultCode ["
								+ esitoPaaInviaRT.getFault().getFaultCode() + "] faultString [" + esitoPaaInviaRT.getFault().getFaultString()
								+ "] description [" + esitoPaaInviaRT.getFault().getDescription() + "]");

						return;
					}

					log.debug("Ricevuta risposta paaInviaRTRisposta per IUV [" + intestazionePPT.getIdentificativoUnivocoVersamento() + "] esito ricevuto ["
							+ esitoPaaInviaRT.getEsito().toString() + "]");
				}

				if (riattivaRPT) {

					BatchChiediStatoRPT.manageRPTRTService.clearInviarptEsitoById(mygovRptRt.getMygovRptRtId());

					log.debug("Abilitato Reinvio RPT per identificativoDominio [" + identificativoDominio + "], identificativoUnivocoVersamento ["
							+ identificativoUnivocoVersamento + "], codiceContestoPagamento [" + codiceContestoPagamento + "]");
				}

				//				if (riabilitaAttivaRPT) {
				//
				//					MygovAttivaRptE attivaRptE = BatchChiediStatoRPT.attivaRptDao.getByKey(identificativoDominio, identificativoUnivocoVersamento,
				//							codiceContestoPagamento);
				//					if (attivaRptE == null) {
				//
				//						log.error("Nessuna MygovAttivaRptE trovata per riattivazione :  identificativoDominio [" + identificativoDominio
				//								+ "], identificativoUnivocoVersamento [" + identificativoUnivocoVersamento + "], codiceContestoPagamento ["
				//								+ codiceContestoPagamento + "]");
				//
				//						return;
				//					}
				//
				//					BatchChiediStatoRPT.attivaRptDao.updateEsitoByKey(attivaRptE.getMygovAttivaRptEId(), "KO");
				//
				//					log.debug("Riabilitata attiva per identificativoDominio [" + identificativoDominio + "], identificativoUnivocoVersamento ["
				//							+ identificativoUnivocoVersamento + "], codiceContestoPagamento [" + codiceContestoPagamento + "]");
				//				}
			}
			catch (Exception ex) {
				log.error("Errore durante elaborazione RPT - MODELLO " + modelloPagamento + " - IUV " + mygovRptRt.getCodRptDatiVersIdUnivocoVersamento(), ex);
			}
		}
	}

	/**
	 * @param mygovRptRt
	 * @return
	 */
	private static HashMap<String, Object> creaRTNegativa(MygovRptRt mygovRptRt) {

		String identificativoIntermediarioPA = BatchChiediStatoRPT.fespProperties.getIdentificativoIntermediarioPa();
		String identificativoStazioneIntermediarioPA = BatchChiediStatoRPT.fespProperties.getIdentificativoStazioneIntermediarioPa();
		String identificativoDominio = mygovRptRt.getCodRptInviarptIdDominio();
		String identificativoUnivocoVersamento = mygovRptRt.getCodRptInviarptIdUnivocoVersamento();
		String codiceContestoPagamento = mygovRptRt.getCodRptInviarptCodiceContestoPagamento();

		GregorianCalendar calendar = new GregorianCalendar();

		CtRicevutaTelematica rt = CtRicevutaTelematica.Factory.newInstance();
		rt.setVersioneOggetto(mygovRptRt.getDeRptVersioneOggetto());

		CtDominio ctDominio = CtDominio.Factory.newInstance();
		ctDominio.setIdentificativoDominio(mygovRptRt.getCodRptDomIdDominio());
		ctDominio.setIdentificativoStazioneRichiedente(mygovRptRt.getCodRptDomIdStazioneRichiedente());
		rt.setDominio(ctDominio);

		rt.setIdentificativoMessaggioRicevuta(BatchChiediStatoRPT.getUUID());

		calendar.setTime(new Date());
		rt.setDataOraMessaggioRicevuta(calendar);

		rt.setRiferimentoMessaggioRichiesta(mygovRptRt.getCodRptIdMessaggioRichiesta());

		calendar.setTime(mygovRptRt.getDtRptDataOraMessaggioRichiesta());
		rt.setRiferimentoDataRichiesta(calendar);

		CtIstitutoAttestante ctIstitutoAttestante = CtIstitutoAttestante.Factory.newInstance();
		CtIdentificativoUnivoco ctIdentificativoUnivoco = CtIdentificativoUnivoco.Factory.newInstance();
		ctIdentificativoUnivoco.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivoco.G);
		ctIdentificativoUnivoco.setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptInviarptIdPsp());
		ctIstitutoAttestante.setIdentificativoUnivocoAttestante(ctIdentificativoUnivoco);

		List<String> tipiVersamento = new ArrayList<String>();
		tipiVersamento.add(mygovRptRt.getCodRptDatiVersTipoVersamento());

		ctIstitutoAttestante.setDenominazioneAttestante("[" + mygovRptRt.getCodRptInviarptIdPsp() + "]");

		rt.setIstitutoAttestante(ctIstitutoAttestante);

		CtEnteBeneficiario ctEnteBeneficiario = CtEnteBeneficiario.Factory.newInstance();

		CtIdentificativoUnivocoPersonaG ctIdentificativoUnivocoPersonaG = CtIdentificativoUnivocoPersonaG.Factory.newInstance();
		ctIdentificativoUnivocoPersonaG
				.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.Enum.forString(mygovRptRt.getCodRptEnteBenefIdUnivBenefTipoIdUnivoco()));
		ctIdentificativoUnivocoPersonaG.setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptEnteBenefIdUnivBenefCodiceIdUnivoco());

		ctEnteBeneficiario.setIdentificativoUnivocoBeneficiario(ctIdentificativoUnivocoPersonaG);

		ctEnteBeneficiario.setDenominazioneBeneficiario(mygovRptRt.getDeRptEnteBenefDenominazioneBeneficiario());
		ctEnteBeneficiario.setCodiceUnitOperBeneficiario(mygovRptRt.getCodRptEnteBenefCodiceUnitOperBeneficiario());
		ctEnteBeneficiario.setDenomUnitOperBeneficiario(mygovRptRt.getDeRptEnteBenefDenomUnitOperBeneficiario());
		ctEnteBeneficiario.setIndirizzoBeneficiario(mygovRptRt.getDeRptEnteBenefIndirizzoBeneficiario());
		ctEnteBeneficiario.setCivicoBeneficiario(mygovRptRt.getDeRptEnteBenefCivicoBeneficiario());
		ctEnteBeneficiario.setCapBeneficiario(mygovRptRt.getCodRptEnteBenefCapBeneficiario());
		ctEnteBeneficiario.setLocalitaBeneficiario(mygovRptRt.getDeRptEnteBenefLocalitaBeneficiario());
		ctEnteBeneficiario.setProvinciaBeneficiario(mygovRptRt.getDeRptEnteBenefProvinciaBeneficiario());
		ctEnteBeneficiario.setNazioneBeneficiario(mygovRptRt.getCodRptEnteBenefNazioneBeneficiario());

		rt.setEnteBeneficiario(ctEnteBeneficiario);

		//SOGGETTO VERSANTE
		if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco())) {

			CtSoggettoVersante ctSoggettoVersante = CtSoggettoVersante.Factory.newInstance();

			CtIdentificativoUnivocoPersonaFG ctIdentificativoUnivocoPersonaFG = CtIdentificativoUnivocoPersonaFG.Factory.newInstance();
			ctIdentificativoUnivocoPersonaFG.setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco());
			ctIdentificativoUnivocoPersonaFG
					.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.Enum.forString(mygovRptRt.getCodRptSoggVersIdUnivVersTipoIdUnivoco()));

			ctSoggettoVersante.setIdentificativoUnivocoVersante(ctIdentificativoUnivocoPersonaFG);

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersAnagraficaVersante()))
				ctSoggettoVersante.setAnagraficaVersante(mygovRptRt.getDeRptSoggVersAnagraficaVersante());

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersIndirizzoVersante()))
				ctSoggettoVersante.setIndirizzoVersante(mygovRptRt.getDeRptSoggVersIndirizzoVersante());

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersCivicoVersante()))
				ctSoggettoVersante.setCivicoVersante(mygovRptRt.getDeRptSoggVersCivicoVersante());

			if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggVersCapVersante()))
				ctSoggettoVersante.setCapVersante(mygovRptRt.getCodRptSoggVersCapVersante());

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersLocalitaVersante()))
				ctSoggettoVersante.setLocalitaVersante(mygovRptRt.getDeRptSoggVersLocalitaVersante());

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersProvinciaVersante()))
				ctSoggettoVersante.setProvinciaVersante(mygovRptRt.getDeRptSoggVersProvinciaVersante());

			if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggVersNazioneVersante()))
				ctSoggettoVersante.setNazioneVersante(mygovRptRt.getCodRptSoggVersNazioneVersante());

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersEmailVersante()))
				ctSoggettoVersante.setEMailVersante(mygovRptRt.getDeRptSoggVersEmailVersante());

			rt.setSoggettoVersante(ctSoggettoVersante);
		}

		//SOGGETTO PAGATORE

		CtSoggettoPagatore ctSoggettoPagatore = CtSoggettoPagatore.Factory.newInstance();

		CtIdentificativoUnivocoPersonaFG ctIdentificativoUnivocoPersonaFG = CtIdentificativoUnivocoPersonaFG.Factory.newInstance();
		ctIdentificativoUnivocoPersonaFG.setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco());
		ctIdentificativoUnivocoPersonaFG
				.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.Enum.forString(mygovRptRt.getCodRptSoggPagIdUnivPagTipoIdUnivoco()));

		ctSoggettoPagatore.setIdentificativoUnivocoPagatore(ctIdentificativoUnivocoPersonaFG);

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagAnagraficaPagatore()))
			ctSoggettoPagatore.setAnagraficaPagatore(mygovRptRt.getDeRptSoggPagAnagraficaPagatore());

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagIndirizzoPagatore()))
			ctSoggettoPagatore.setIndirizzoPagatore(mygovRptRt.getDeRptSoggPagIndirizzoPagatore());

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagCivicoPagatore()))
			ctSoggettoPagatore.setCivicoPagatore(mygovRptRt.getDeRptSoggPagCivicoPagatore());

		if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggPagCapPagatore()))
			ctSoggettoPagatore.setCapPagatore(mygovRptRt.getCodRptSoggPagCapPagatore());

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagLocalitaPagatore()))
			ctSoggettoPagatore.setLocalitaPagatore(mygovRptRt.getDeRptSoggPagLocalitaPagatore());

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagProvinciaPagatore()))
			ctSoggettoPagatore.setProvinciaPagatore(mygovRptRt.getDeRptSoggPagProvinciaPagatore());

		if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggPagNazionePagatore()))
			ctSoggettoPagatore.setNazionePagatore(mygovRptRt.getCodRptSoggPagNazionePagatore());

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagEmailPagatore()))
			ctSoggettoPagatore.setEMailPagatore(mygovRptRt.getDeRptSoggPagEmailPagatore());

		rt.setSoggettoPagatore(ctSoggettoPagatore);

		CtDatiVersamentoRT ctDatiVersamentoRT = CtDatiVersamentoRT.Factory.newInstance();
		ctDatiVersamentoRT.setCodiceEsitoPagamento(StCodiceEsitoPagamento.X_1);
		ctDatiVersamentoRT.setImportoTotalePagato(BigDecimal.ZERO);
		ctDatiVersamentoRT.setIdentificativoUnivocoVersamento(mygovRptRt.getCodRptDatiVersIdUnivocoVersamento());
		ctDatiVersamentoRT.setCodiceContestoPagamento(mygovRptRt.getCodRptDatiVersCodiceContestoPagamento());

		rt.setDatiPagamento(ctDatiVersamentoRT);

		PaaInviaRT bodyrichiesta;

		try {
			bodyrichiesta = buildBodyPaaInviaRT(rt);
		}
		catch (Exception ex) {
			log.error("Failed to build negative RT", ex);

			return null;
		}

		IntestazionePPT intestazionePPT = new IntestazionePPT();

		intestazionePPT.setIdentificativoDominio(identificativoDominio);
		intestazionePPT.setIdentificativoUnivocoVersamento(identificativoUnivocoVersamento);
		intestazionePPT.setCodiceContestoPagamento(codiceContestoPagamento);
		intestazionePPT.setIdentificativoIntermediarioPA(identificativoIntermediarioPA);
		intestazionePPT.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPA);

		HashMap<String, Object> paramsHashMap = new HashMap<String, Object>();
		paramsHashMap.put("bodyrichiesta", bodyrichiesta);
		paramsHashMap.put("intestazionePPT", intestazionePPT);

		return paramsHashMap;
	}

	/**
	 * @param ctRicevutaTelematica
	 * @return
	 */
	private static PaaInviaRT buildBodyPaaInviaRT(CtRicevutaTelematica ctRicevutaTelematica) {

		RTDocument rtDoc = RTDocument.Factory.newInstance();
		rtDoc.setRT(ctRicevutaTelematica);

		byte[] byteRT = null;

		try {
			byteRT = rtDoc.toString().getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Failed to parse RT ::: UnsupportedEncodingException :::", uee);
		}

		PaaInviaRT result = new PaaInviaRT();
		result.setTipoFirma("0");
		result.setRt(byteRT);

		return result;
	}

//	/**
//	 * @param mygovRptRt
//	 * @return
//	 */
//	private static HashMap<String, Object> chiediCopiaRT(MygovRptRt mygovRptRt) {
//
//		try {
//
//			String identificativoIntermediarioPA = BatchChiediStatoRPT.fespProperties.getIdentificativoIntermediarioPa();
//			String identificativoStazioneIntermediarioPA = BatchChiediStatoRPT.fespProperties.getIdentificativoStazioneIntermediarioPa();
//			String password = BatchChiediStatoRPT.fespProperties.getPassword();
//			String identificativoDominio = mygovRptRt.getCodRptInviarptIdDominio();
//			String identificativoUnivocoVersamento = mygovRptRt.getCodRptInviarptIdUnivocoVersamento();
//			String codiceContestoPagamento = mygovRptRt.getCodRptInviarptCodiceContestoPagamento();
//
//			Holder<FaultBean> fault = new Holder<FaultBean>();
//			Holder<String> tipoFirma = new Holder<String>();
//			Holder<DataHandler> rtRicevuta = new Holder<DataHandler>();
//
//			log.debug("Chiamo nodoChiediCopiaRT con i parametri: identificativoIntermediarioPa [" + identificativoIntermediarioPA
//					+ "], identificativoStazioneIntermediarioPA [" + identificativoStazioneIntermediarioPA + "], password [" + password
//					+ "], identificativoDominio [" + identificativoDominio + "], identificativoUnivocoVersamento [" + identificativoUnivocoVersamento
//					+ "], codiceContestoPagamento [" + codiceContestoPagamento + "]");
//
//			//Sleep 2 secondi prima di ogni chiamata
//			Thread.sleep(2000);
//
//			BatchChiediStatoRPT.pagamentiTelematiciRPTServiceClient.nodoChiediCopiaRT(identificativoIntermediarioPA, identificativoStazioneIntermediarioPA,
//					password, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento, fault, tipoFirma, rtRicevuta);
//
//			if (fault.value != null) {
//				log.debug("ERROR in nodoChiediCopiaRT per IUV [" + identificativoUnivocoVersamento + "] faultCode [" + fault.value.getFaultCode()
//						+ "] faultString [" + fault.value.getFaultString() + "] description [" + fault.value.getDescription() + "]");
//
//				return null;
//			}
//
//			InputStream rtPayload = rtRicevuta.value.getInputStream();
//			byte[] rtPayloadInviatoOriginale = IOUtils.toByteArray(rtPayload);
//
//			PaaInviaRT bodyrichiesta = new PaaInviaRT();
//			bodyrichiesta.setRt(rtPayloadInviatoOriginale);
//			bodyrichiesta.setTipoFirma(tipoFirma.value);
//
//			IntestazionePPT intestazionePPT = new IntestazionePPT();
//
//			intestazionePPT.setIdentificativoDominio(identificativoDominio);
//			intestazionePPT.setIdentificativoUnivocoVersamento(identificativoUnivocoVersamento);
//			intestazionePPT.setCodiceContestoPagamento(codiceContestoPagamento);
//			intestazionePPT.setIdentificativoIntermediarioPA(identificativoIntermediarioPA);
//			intestazionePPT.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPA);
//
//			HashMap<String, Object> paramsHashMap = new HashMap<String, Object>();
//			paramsHashMap.put("bodyrichiesta", bodyrichiesta);
//			paramsHashMap.put("intestazionePPT", intestazionePPT);
//
//			log.debug("Ricevuta risposta nodoChiediCopiaRT con i parametri: identificativoIntermediarioPa [" + identificativoIntermediarioPA
//					+ "], identificativoStazioneIntermediarioPA [" + identificativoStazioneIntermediarioPA + "], password [" + password
//					+ "], identificativoDominio [" + identificativoDominio + "], identificativoUnivocoVersamento [" + identificativoUnivocoVersamento
//					+ "], codiceContestoPagamento [" + codiceContestoPagamento + "]");
//
//			return paramsHashMap;
//		}
//		catch (Exception ex) {
//			log.error("Errore nella richiesta nodoChiediCopiaRT per IUV [" + mygovRptRt.getCodRptDatiVersIdUnivocoVersamento() + "]", ex);
//
//			return null;
//		}
//	}

	/**
	 * @return
	 */
	private static String getUUID() {
		return "###" + UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * @param pagamentiTelematiciRPTServiceClient the pagamentiTelematiciRPTServiceClient to set
	 */
	public static void setPagamentiTelematiciRPTServiceClient(PagamentiTelematiciRPTServiceClient pagamentiTelematiciRPTServiceClient) {
		BatchChiediStatoRPT.pagamentiTelematiciRPTServiceClient = pagamentiTelematiciRPTServiceClient;
	}

	/**
	 * @param manageRPTRTService the manageRPTRTService to set
	 */
	public static void setManageRPTRTService(ManageRPTRTService manageRPTRTService) {
		BatchChiediStatoRPT.manageRPTRTService = manageRPTRTService;
	}

	/**
	 * @param chiediStatoRTPBean the chiediStatoRTPBean to set
	 */
	public static void setChiediStatoRTPBean(ChiediStatoRTPBean chiediStatoRTPBean) {
		BatchChiediStatoRPT.chiediStatoRTPBean = chiediStatoRTPBean;
	}

	/**
	 * @param pagamentiTelematiciRTServiceClient
	 */
	public static void setPagamentiTelematiciRTServiceClient(PagamentiTelematiciRTServiceClient pagamentiTelematiciRTServiceClient) {
		BatchChiediStatoRPT.pagamentiTelematiciRTServiceClient = pagamentiTelematiciRTServiceClient;
	}

	/**
	 * @param fespProperties
	 */
	public void setFespProperties(FespBean fespProperties) {
		BatchChiediStatoRPT.fespProperties = fespProperties;
	}

//	/**
//	 * @param attivaRptDao
//	 */
//	public static void setAttivaRptDao(AttivaRptDao attivaRptDao) {
//		BatchChiediStatoRPT.attivaRptDao = attivaRptDao;
//	}
}
