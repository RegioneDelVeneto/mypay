package it.regioneveneto.mygov.payment.nodoregionalefesp.batch;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPTRisposta;
import it.regioneveneto.mygov.payment.constants.FaultCodeConstants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.FailedBatchExecutionException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.AttivaRPTService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.NodoInviaRPTService;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author regione del veneto
 * 
 */
public class BatchInviaRPTAttivate {

	private static final Log log = LogFactory.getLog(BatchInviaRPTAttivate.class);

	/**
	 * Contesto spring del batch
	 */
	private static ApplicationContext context = null;

	private static AttivaRPTService attivaRPTService;

	private static NodoInviaRPTService nodoInviaRPTService;

	public BatchInviaRPTAttivate() {
		super();
	}

	public static void main(String[] args) throws FailedBatchExecutionException {

		Date start = new Date();

		log.info("INIZIO esecuzione di BatchInviaRPTAttivate - [" + start + "]");

		// Caricamento del contesto Spring
		log.info("Caricamento del contesto Spring");
		String[] configLocations = new String[1];

		configLocations[0] = "invia-rpt-attivate-service.xml";

		BatchInviaRPTAttivate.context = new ClassPathXmlApplicationContext(configLocations);

		// Recupero dal contesto i bean dei Service necessari
		log.info("Recupero dal contesto i bean dei Service necessari");

		BatchInviaRPTAttivate.attivaRPTService = (AttivaRPTService) BatchInviaRPTAttivate.context.getBean("attivaRPTService");
		BatchInviaRPTAttivate.nodoInviaRPTService = (NodoInviaRPTService) BatchInviaRPTAttivate.context.getBean("nodoInviaRPTService");

		List<MygovRptRt> mygovRptRts = attivaRPTService.elaboraRPTAttivate();

		for (MygovRptRt mygovRptRt : mygovRptRts) {
			log.info("invio RPT [" + mygovRptRt.getCodRptDatiVersIdUnivocoVersamento() + "]");

			gov.telematici.pagamenti.ws.ppthead.IntestazionePPT _paaSILInviaRPT_header = nodoInviaRPTService.buildHeaderRPT(mygovRptRt
					.getCodRptIdMessaggioRichiesta());
			it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico CtRPT = nodoInviaRPTService.buildRPT(mygovRptRt
					.getCodRptIdMessaggioRichiesta());

			log.info("BATCH INVIO RPT: " + CtRPT.toString());

			NodoInviaRPT _paaSILInviaRPT_body = nodoInviaRPTService.buildBodyRPT(CtRPT, mygovRptRt.getCodRptIdMessaggioRichiesta());
			_paaSILInviaRPT_body.setTipoFirma("");

			NodoInviaRPTRisposta _rispostaRPT = null;
			try {
				_rispostaRPT = nodoInviaRPTService.nodoInviaRPT(mygovRptRt.getMygovRptRtId(), _paaSILInviaRPT_header, _paaSILInviaRPT_body);
				log.info("inviata RPT [" + mygovRptRt.getCodRptDatiVersIdUnivocoVersamento() + "]");
			} catch (Exception e) {
				log.error("Errore durante l'invio dell'RPT [" + mygovRptRt.getCodRptDatiVersIdUnivocoVersamento() + "]", e);
				
				//RISPOSTA RPT
				_rispostaRPT = new NodoInviaRPTRisposta();
				_rispostaRPT.setEsito("KO");

				FaultBean faultRPT = new FaultBean();
				faultRPT.setFaultCode(FaultCodeConstants.PPT_ESITO_SCONOSCIUTO);
				faultRPT.setDescription(e.getMessage());
				_rispostaRPT.setFault(faultRPT);
			} finally {
				try {
					nodoInviaRPTService.saveRPTRisposta(_rispostaRPT, mygovRptRt.getMygovRptRtId());
				} catch (Exception e) {
					log.error("Error saving RP risposta: [" + e.getMessage() + "]", e);
				}
			}
		}

		log.info("FINE esecuzione di BatchInviaRPTAttivate");
	}

	public static void setAttivaRPTService(AttivaRPTService attivaRPTService) {
		BatchInviaRPTAttivate.attivaRPTService = attivaRPTService;
	}

	public static void setNodoInviaRPTService(NodoInviaRPTService nodoInviaRPTService) {
		BatchInviaRPTAttivate.nodoInviaRPTService = nodoInviaRPTService;
	}

}
