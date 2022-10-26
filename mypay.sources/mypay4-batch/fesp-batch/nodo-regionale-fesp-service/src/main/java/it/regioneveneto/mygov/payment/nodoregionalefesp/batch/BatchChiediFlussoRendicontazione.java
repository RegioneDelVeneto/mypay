package it.regioneveneto.mygov.payment.nodoregionalefesp.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoIdRendicontazione;
import it.regioneveneto.mygov.payment.nodoregionalefesp.batch.utils.Costants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.batch.costants.StatiEsecuzione;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoRendSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils.FespBean;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.FailedBatchExecutionException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.JumpEnteFlussiRendicontazioneException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.EnteService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.FlussoRendSpcService;
import it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient;
import it.regioneveneto.mygov.payment.utils.Utils;

/**
 * @author regione del veneto
 * @author regione del veneto
 *
 */
public class BatchChiediFlussoRendicontazione implements Costants {

	private static final Log log = LogFactory.getLog(BatchChiediFlussoRendicontazione.class);

	/**
	 * Contesto spring del batch
	 */
	private static ApplicationContext context = null;

	/**
	 * Service per le comunicazioni esterne relative al catalogo informativo 
	 */
	private static PagamentiTelematiciRPTServiceClient pagamentiTelematiciRPTServiceClient = null;

	/**
	 * Service per le associazioni dei PSP con gli enti
	 */
	private static EnteService enteService;

//	/**
//	 * Service per le associazioni dei PSP con gli enti
//	 */
//	private static EntePspService entePspService;

	/**
	 * Service per i FlussoRendSpc
	 */
	private static FlussoRendSpcService flussoRendSpcService;

	/**
	 * Bean con le properties dell'intermediario
	 */
	private static FespBean fespBean;

	/**
	 * @param fespBean the fespBean to set
	 */
	public static void setFespBean(FespBean fespBean) {
		BatchChiediFlussoRendicontazione.fespBean = fespBean;
	}

//	/**
//	 * @param entePspService the entePspService to set
//	 */
//	public void setEntePspService(EntePspService entePspService) {
//		BatchChiediFlussoRendicontazione_v2.entePspService = entePspService;
//	}

	public BatchChiediFlussoRendicontazione() {
	}

	public static void main(String[] args) throws FailedBatchExecutionException {

		Date start = new Date();

		log.info("Inizio esecuzione di BatchChiediFlussoRendicontazione - [" + start + "]");

		//Caricamento del contesto Spring
		log.info("Caricamento del contesto Spring");
		String[] configLocations = new String[1];

		// it/regioneveneto/mygov/payment/nodoregionalefesp/service/impl/flussi-rendicontazione-service.xml
		configLocations[0] = "flussi-rendicontazione-service.xml";

		BatchChiediFlussoRendicontazione.context = new ClassPathXmlApplicationContext(configLocations);

		//Recupero dal contesto i bean dei Service necessari
		log.info("Recupero dal contesto i bean dei Service necessari");

		BatchChiediFlussoRendicontazione.pagamentiTelematiciRPTServiceClient = (PagamentiTelematiciRPTServiceClient) BatchChiediFlussoRendicontazione.context
				.getBean("pagamentiTelematiciRPTServiceClient");
		BatchChiediFlussoRendicontazione.enteService = (EnteService) BatchChiediFlussoRendicontazione.context.getBean("enteService");
//		BatchChiediFlussoRendicontazione_v2.entePspService = (EntePspService) BatchChiediFlussoRendicontazione_v2.context.getBean("entePspService");
		BatchChiediFlussoRendicontazione.flussoRendSpcService = (FlussoRendSpcService) BatchChiediFlussoRendicontazione.context.getBean("flussoRendSpcService");
		BatchChiediFlussoRendicontazione.fespBean = (FespBean) BatchChiediFlussoRendicontazione.context.getBean("fespBean");

		//Inizio del batch
		log.info("Recupero l'elenco degli enti per cui vanno richiesti i flussi di rendicontazione");
		List<MygovEnte> listEnti = null;

		try {
			listEnti = enteService.findAll();
		}
		catch (Exception e) {
			log.error("Problemi nel recupero elenco degli enti per cui vanno richiesti i flussi di rendicontazione.", e);
			throw new FailedBatchExecutionException("Problemi nel recupero elenco degli enti per cui vanno richiesti i flussi di rendicontazione.", e);
		}

		//Per ogni ente effettuo una chiamata ad SPC per recuperare il catalogo informativo
		for (MygovEnte ente : listEnti) {

			String codiceIpaEnte = null;
			//Recupero codice ente interno ed esterno (mapping)
			codiceIpaEnte = ente.getCodIpaEnte();

			log.info("");
			log.info("***********************************************************************");
			log.info("Elaborazione Flussi Rendicontazione per l'ente: [" + codiceIpaEnte + "]");
			log.info("***********************************************************************");

			try {
				recuperaFlussiRendicontazionePerEnte(ente);
			}
			catch (JumpEnteFlussiRendicontazioneException e) {
				log.error("Errore in fase di elaborazione dei flussi di rendicontazione per l'ente: [" + codiceIpaEnte + "]. L'ente viene saltato. Eccezione: ["
						+ e.getMessage() + "]", e);
			}

			log.info("Fine elaborazione Flussi Rendicontazione per l'ente: [" + codiceIpaEnte + "]");
		}
		
		try {
			ripristinaFlussiNonScaricatiCorrettamente();
		} catch (Exception exp) {
			log.error("Impossibile ripristinare i flussi non correttamente scaricati: " 
					+ exp.getMessage());
		}
		
		Date end = new Date();

		log.info("***********************************************************************");
		log.info("Fine esecuzione di BatchChiediFlussoRendicontazione - [" + end + "]");
		log.info("***********************************************************************");
		log.info("");

		long executionTime = end.getTime() - start.getTime();
		log.info("Tempo di esecuzione di BatchChiediFlussoRendicontazione in millisecondi = [" + executionTime + "]");
	}

	/**
	 * Effettua la chiamata al nodo nazionale per recuperare i flussi di rendicontazione per ente(codiceFiscale) e PSP.
	 * @param ente
	 * @param listaPspPerEnte
	 * @return
	 */
	private static TipoElencoFlussiRendicontazione recuperaFlussiRendicontazionePerEnte(MygovEnte ente) {

		//Recupero codice ente interno ed esterno (mapping)
		String identificativoIntermediarioPa = fespBean.getIdentificativoIntermediarioPa();
		String identificativoStazioneIntermediarioPA = fespBean.getIdentificativoStazioneIntermediarioPa();
		String password = fespBean.getPassword();
		String identificativoDominio = ente.getCodiceFiscaleEnte();
		String codiceIpaEnte = ente.getCodIpaEnte();
		String rootRendicontazione = fespBean.getRoot() + "/" + codiceIpaEnte + PATH_RENDICONTAZIONE;

		log.info("Richiesta dei flussi di rendicontazione per l'ente: [" + codiceIpaEnte + "].");

		String identificativoPSP = null;

		Holder<FaultBean> fault = new Holder<FaultBean>();
		Holder<TipoElencoFlussiRendicontazione> elencoFlussiRendicontazione = new Holder<TipoElencoFlussiRendicontazione>();

		try {
			log.debug("Chiamato nodoChiediElencoFlussiRendicontazione con i parametri: identificativoIntermediarioPa [" + identificativoIntermediarioPa
					+ "], identificativoStazioneIntermediarioPA [" + identificativoStazioneIntermediarioPA + "], password [" + password
					+ "], identificativoDominio [" + identificativoDominio + "], identificativoPSP [" + identificativoPSP + "]");

			pagamentiTelematiciRPTServiceClient.nodoChiediElencoFlussiRendicontazione(identificativoIntermediarioPa, identificativoStazioneIntermediarioPA,
					password, identificativoDominio, null, fault, elencoFlussiRendicontazione);

			if (elencoFlussiRendicontazione.value != null) {
				List<TipoIdRendicontazione> elencoFlussiRendicontazioneList = elencoFlussiRendicontazione.value.getIdRendicontazione();
				if (elencoFlussiRendicontazioneList != null && elencoFlussiRendicontazioneList.size() > 0) {
					log.info("Flussi di Rendicontazione ottenuti = [" + elencoFlussiRendicontazioneList.size() + "]");

					//Elaborazione lista flussi del PSP
					for (TipoIdRendicontazione tipoIdRendicontazione : elencoFlussiRendicontazioneList) {

						String identificativoFlusso = tipoIdRendicontazione.getIdentificativoFlusso();

						try {
							identificativoPSP = parseIdentificativoPSPFromIdentificativoFlusso(identificativoFlusso);
							log.debug("Identificativo PSP [ " + identificativoPSP + " ] per ente [ " + codiceIpaEnte + " ]");
						}
						catch (Exception e) {
							log.error("Errore nel parsing dell'identificativo PSP per l'ente [ " + codiceIpaEnte + " ]");
							continue;
						}

						XMLGregorianCalendar dataOraFlusso = tipoIdRendicontazione.getDataOraFlusso();

						Calendar dataOraFlussoCalendar = dataOraFlusso.toGregorianCalendar();
						dataOraFlussoCalendar.clear(Calendar.MILLISECOND);

						String dataOraFlussoString = Utils.PLAIN_TIMESTAMP_UNTIL_SECONDS.format(dataOraFlussoCalendar.getTime());

						//Verifica se il flusso e' gia' presente
						Date dataOraRiferimentoFlusso = new Date(dataOraFlussoCalendar.getTimeInMillis());
						Date currentDate = new Date();

						//Controlla se il flusso e' gia' stato scaricato
						MygovFlussoRendSpc flussoRendSpc = flussoRendSpcService.getByKeyInsertable(codiceIpaEnte, identificativoPSP,
								identificativoFlusso, dataOraRiferimentoFlusso);

						//Se e' gia' presente lo salto e proseguo con quelli successivi
						if (flussoRendSpc != null) {
							log.info("Verificato che il Flusso per codiceIpaEnte [" + codiceIpaEnte + "], identificativoPSP [" + identificativoPSP
									+ "], identificativoFlusso [" + identificativoFlusso + "], dataOraFlusso [" + dataOraFlussoString
									+ "] e' gia' stato processato");
						}
						//Altrimenti inserisco la traccia del flusso e poi do il via al download
						else {
							//Inserimento della chiave del Flusso
							try {
								flussoRendSpcService.insert(codiceIpaEnte, identificativoPSP, identificativoFlusso,
										dataOraRiferimentoFlusso, currentDate);
							}
							catch (DataIntegrityViolationException dive) {
								log.warn("Inserimento non possibile in quanto il Flusso per codiceIpaEnte [" + codiceIpaEnte + "], identificativoPSP ["
										+ identificativoPSP + "], identificativoFlusso [" + identificativoFlusso + "], dataOraFlusso [" + dataOraFlussoString
										+ "] e' gia' stato processato. Il flusso viene saltato.");
								//Salta il flusso e passa al successivo
								continue;
							}
							catch (DataAccessException dae) {
								log.error("Errore in fase di inserimento del Flusso per codiceIpaEnte [" + codiceIpaEnte + "], identificativoPSP ["
										+ identificativoPSP + "], identificativoFlusso [" + identificativoFlusso + "], dataOraFlusso [" + dataOraFlussoString
										+ "]  nel DB");
								//Salta il flusso e passa al successivo
								continue;
							}

							//Sleep 2 secondi prima di ogni chiamata
							Thread.sleep(2000);

							//Scarica il flusso dal nodo spc
							Holder<DataHandler> xmlRendicontazione = new Holder<DataHandler>();

							pagamentiTelematiciRPTServiceClient.nodoChiediFlussoRendicontazione(identificativoIntermediarioPa,
									identificativoStazioneIntermediarioPA, password, identificativoDominio, identificativoPSP, identificativoFlusso, fault,
									xmlRendicontazione);

							//Upload del file su File System
							try {
								scriviFlusso(xmlRendicontazione, rootRendicontazione, codiceIpaEnte, identificativoPSP, identificativoFlusso,
										dataOraFlussoString, dataOraRiferimentoFlusso);
							}
							catch (Exception e) {
								log.error("Errore nella fase di scaricamento del flusso su FileSystem per codiceIpaEnte [" + codiceIpaEnte
										+ "], identificativoPSP [" + identificativoPSP + "], identificativoFlusso [" + identificativoFlusso
										+ "], dataOraFlusso [" + dataOraFlussoString + "]", e);

								flussoRendSpcService.updateByKey(codiceIpaEnte, identificativoPSP, identificativoFlusso,
										dataOraRiferimentoFlusso, null, 0L, new Date(), StatiEsecuzione.ERRORE_CARICAMENTO.getValue());
								try {
									if (xmlRendicontazione.value != null && xmlRendicontazione.value.getInputStream() != null)
										xmlRendicontazione.value.getInputStream().close();
								}
								catch (Exception ex) {
									//Nothing to do
								}
							}
						}
					}
				}
				else {
					log.warn("-----------------------------------------");
					log.warn("Nessun flusso di Rendicontazione ottenuto");
					log.warn("-----------------------------------------");
				}
			}
			else {
				log.warn("-----------------------------------------");
				log.warn("Nessun flusso di Rendicontazione ottenuto");
				log.warn("-----------------------------------------");
			}

		}
		catch (Exception e) {
			log.error("Errore nella richiesta elenco flussi rendicontazione per l'ente [" + codiceIpaEnte + "] per il PSP [" + identificativoPSP
					+ "]. L'elenco flussi per il singolo PSP non viene recuperato. Errore: [" + e.getMessage() + "]", e);
		}
		return null;
	}

	private static void scriviFlusso(Holder<DataHandler> xmlRendicontazione, String rootRendicontazione, String codiceIpaEnte, String identificativoPSP,
			String identificativoFlusso, String dataOraFlussoString, Date dataOraRiferimentoFlusso) throws Exception {

		if (xmlRendicontazione.value == null || xmlRendicontazione.value.getInputStream() == null) {
			throw new Exception("Stream vuoto per l'ente [" + codiceIpaEnte + "] per il PSP [" + identificativoPSP + "] del flusso [" + identificativoFlusso
					+ " - " + dataOraFlussoString + "]");
		}
		//LO STREAM DEL FLUSSO DI RENDICONTAZIONE
		InputStream xmlRendInputStream = xmlRendicontazione.value.getInputStream();

		GregorianCalendar now = new GregorianCalendar();
		String cartella_anno_mese = now.get(GregorianCalendar.YEAR) + "_"
				+ ((now.get(GregorianCalendar.MONTH) + 1) > 9 ? (now.get(GregorianCalendar.MONTH) + 1) : "0" + (now.get(GregorianCalendar.MONTH) + 1));

		//CREAZIONE DELLE CARTELLE SE NON ESISTONO
		StringBuffer subDirectory = new StringBuffer();
		subDirectory.append(rootRendicontazione);
		subDirectory.append(cartella_anno_mese + "/");

		File file = new File(subDirectory.toString());
		if (!file.exists()) {
			file.mkdirs();
		}

		//Base del nome del file scaricato
		StringBuffer nomeFileScaricatoBaseName = new StringBuffer();
		nomeFileScaricatoBaseName.append(identificativoPSP);
		nomeFileScaricatoBaseName.append("_");
		nomeFileScaricatoBaseName.append(identificativoFlusso);
		nomeFileScaricatoBaseName.append("_");
		nomeFileScaricatoBaseName.append(dataOraFlussoString);

		//Nome del file scaricato
		StringBuffer nomeFileScaricato = new StringBuffer();
		nomeFileScaricato.append(nomeFileScaricatoBaseName);
		nomeFileScaricato.append(FLUSSO_FILE_EXTENSION);

		//TIMESTAMP PER GARANTIRE L'UNIVOCITA' DEL FILE
		String timestampString = Utils.PLAIN_TIMESTAMP_UNTIL_SECONDS.format(new Date());

		//File temporaneo
		StringBuffer fileTemporaryName = new StringBuffer();
		fileTemporaryName.append(subDirectory);
		fileTemporaryName.append(nomeFileScaricatoBaseName);
		fileTemporaryName.append("_");
		fileTemporaryName.append(timestampString);
		fileTemporaryName.append(TEMPORARY_FILE_EXTENSION);

		StringBuffer fileRendicontazioneName = new StringBuffer();
		fileRendicontazioneName.append(subDirectory);
		fileRendicontazioneName.append(nomeFileScaricato);

		//File per la gestione dell'upload su FileSystem
		File fileTemporaryRendicontazione = new File(fileTemporaryName.toString());

		/*
		 * ************************************************
		 * SCRITTURA DEL FILE TEMPORANEO DI RENDICONTAZIONE 
		 * ************************************************ 
		 */
		OutputStream outStream = null;
		log.debug("Scrittura del file temporaneo di rendicontazione [" + fileTemporaryName + "]  per il PSP [" + identificativoPSP + "] per l'ente ["
				+ codiceIpaEnte + "].");
		try {
			//			if (true) throw new FileNotFoundException();			
			outStream = new FileOutputStream(fileTemporaryRendicontazione);

			byte[] buffer = new byte[CHUNK_DIMENSION];
			int bytesRead;
			//read from is to buffer
			while ((bytesRead = xmlRendInputStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
		}
		catch (IOException e) {
			log.error("Errore nella scrittura del file temporaneo di rendicontazione [" + fileTemporaryName + "]  per il PSP [" + identificativoPSP
					+ "] per l'ente [" + codiceIpaEnte + "].", e);
			throw (e);
		}
		finally {
			try {
				xmlRendInputStream.close();
			}
			catch (IOException ex) {
				//Nothing to do
			}
			if (outStream != null) {
				try {
					outStream.flush();
				}
				catch (IOException ex) {
					//Nothing to do
				}
				try {
					outStream.close();
				}
				catch (IOException ex) {
					//Nothing to do
				}
			}
		}

		log.debug("Il file [" + fileTemporaryName + "] temporaneo per il PSP [" + identificativoPSP + "] per l'ente [" + codiceIpaEnte
				+ "] e' stato correttamente uploadato.");

		/*
		 * ***************************************************************
		 * RINOMINAZIONE DEL FILE TEMPORANEO NEL FILE DI RENDICONTAZIONE EFFETTIVO
		 * *************************************************************** 
		 */
		File fileRendicontazione = new File(fileRendicontazioneName.toString());
		boolean renamingEsito = fileTemporaryRendicontazione.renameTo(fileRendicontazione);
		if (renamingEsito) {
			log.info("Il file [" + fileRendicontazioneName + "] per il PSP [" + identificativoPSP + "] per l'ente [" + codiceIpaEnte
					+ "] e' stato correttamente uploadato.");
		}
		else {
			log.error("Errore nel rinominare il file temporaneo nel file [" + fileRendicontazioneName + "] per il PSP [" + identificativoPSP + "] per l'ente ["
					+ codiceIpaEnte + "].");
			throw new Exception("Errore nel rinominare il file temporaneo nel file [" + fileRendicontazioneName + "] per il PSP [" + identificativoPSP
					+ "] per l'ente [" + codiceIpaEnte + "].");
		}

		//Se il processo di scrittura su FileSystem e' andato a buon fine, effettua l'aggiornamento su DB con i dati del file salvato sul FileSystem
		long numDimensioneFileScaricato = fileRendicontazione.length();

		flussoRendSpcService.updateByKey(codiceIpaEnte, identificativoPSP, identificativoFlusso, dataOraRiferimentoFlusso,
				PATH_RENDICONTAZIONE + cartella_anno_mese + "/" + nomeFileScaricato.toString(), numDimensioneFileScaricato, new Date(),
				StatiEsecuzione.CARICATO.getValue());

	}

	private static String parseIdentificativoPSPFromIdentificativoFlusso(String identificativoFlusso) {
		String identificativoPSP = null;
		String temp = identificativoFlusso.substring(10);
		Scanner scanner = new Scanner(temp);
		scanner.useDelimiter("-");
		identificativoPSP = scanner.next();
		scanner.close();
		return identificativoPSP;
	}

	/**
	 * Aggiorna i flussi con stato IN_CARICAMENTO settando lo stato KO.
	 * In questo modo i flussi pendenti possono essere scaricati nuovamente
	 * alla prossima esecuzione del batch
	 */
	private static void ripristinaFlussiNonScaricatiCorrettamente() {
		log.info("Inizio ripristino flussi non scaricati correttamente");
		int numFlussiAggiornati = flussoRendSpcService.resetFlussiInCaricamento();
		log.info("Ripristinati " + numFlussiAggiornati + " flussi di rendicontazione");
	}

}
