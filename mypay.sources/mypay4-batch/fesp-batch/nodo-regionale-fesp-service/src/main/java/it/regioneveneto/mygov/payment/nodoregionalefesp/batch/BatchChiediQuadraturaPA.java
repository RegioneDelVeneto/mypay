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
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoIdQuadratura;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoListaQuadrature;
import it.regioneveneto.mygov.payment.nodoregionalefesp.batch.utils.Costants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.batch.costants.StatiEsecuzione;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovFlussoQuadSpc;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils.FespBean;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.FailedBatchExecutionException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.JumpEnteFlussiQuadraturaException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.EnteService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.FlussoQuadSpcService;
import it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient;
import it.regioneveneto.mygov.payment.utils.Utils;

/**
 * @author regione del veneto
 *
 */
public class BatchChiediQuadraturaPA implements Costants {

	private static final Log log = LogFactory.getLog(BatchChiediQuadraturaPA.class);

	/**
	 * Contesto spring del batch
	 */
	private static ApplicationContext context = null;

	/**
	 * Service per le comunicazioni esterne 
	 */
	private static PagamentiTelematiciRPTServiceClient pagamentiTelematiciRPTServiceClient = null;

	/**
	 * Service per le associazioni dei PSP con gli enti
	 */
	private static EnteService enteService;

	/**
	 * Service per i FlussoSpc
	 */
	private static FlussoQuadSpcService flussoQuadSpcService;

	/**
	 * Bean con le properties dell'intermediario
	 */
	private static FespBean fespBean;

	/**
	 * @param fespBean the fespBean to set
	 */
	public static void setFespBean(FespBean fespBean) {
		BatchChiediQuadraturaPA.fespBean = fespBean;
	}

	public BatchChiediQuadraturaPA() {
	}

	public static void main(String[] args) throws FailedBatchExecutionException {

		Date start = new Date();

		log.info("Inizio esecuzione di BatchChiediQuadratura - [" + start + "]");

		//Caricamento del contesto Spring
		log.info("Caricamento del contesto Spring");
		String[] configLocations = new String[1];

//		configLocations[0] = "it/regioneveneto/mygov/payment/nodoregionalefesp/service/impl/flussi-quadratura-service.xml";
		
		configLocations[0] = "flussi-quadratura-service.xml";

		BatchChiediQuadraturaPA.context = new ClassPathXmlApplicationContext(configLocations);

		//Recupero dal contesto i bean dei Service necessari
		log.info("Recupero dal contesto i bean dei Service necessari");

		BatchChiediQuadraturaPA.pagamentiTelematiciRPTServiceClient = (PagamentiTelematiciRPTServiceClient) BatchChiediQuadraturaPA.context.getBean("pagamentiTelematiciRPTServiceClient");
		BatchChiediQuadraturaPA.enteService = (EnteService) BatchChiediQuadraturaPA.context.getBean("enteService");
//		BatchChiediQuadraturaPA.entePspService = (EntePspService) BatchChiediQuadraturaPA.context.getBean("entePspService");
		BatchChiediQuadraturaPA.flussoQuadSpcService = (FlussoQuadSpcService) BatchChiediQuadraturaPA.context.getBean("flussoQuadSpcService");
		BatchChiediQuadraturaPA.fespBean = (FespBean) BatchChiediQuadraturaPA.context.getBean("fespBean");

		//Inizio del batch
		log.info("Recupero l'elenco degli enti per cui vanno richiesti i flussi di quadratura");
		List<MygovEnte> listEnti = null;

		try {
			listEnti = enteService.findAll();
		} catch (Exception e) {
			log.error("Problemi nel recupero elenco degli enti per cui vanno richiesti i flussi di quadratura.", e);
			throw new FailedBatchExecutionException("Problemi nel recupero elenco degli enti per cui vanno richiesti i flussi di quadratura.", e);
		}

		//Per ogni ente effettuo una chiamata ad SPC per recuperare il catalogo informativo
		for (MygovEnte ente : listEnti) {

			String codiceIpaEnte = null;
			//Recupero codice ente interno ed esterno (mapping)
			codiceIpaEnte = ente.getCodIpaEnte();

			log.info("");
			log.info("*****************************************************************");
			log.info("Elaborazione Flussi quadratura per l'ente: [" + codiceIpaEnte + "]");
			log.info("******************************************************************");

			try {
				recuperaFlussiQuadraturaPerEnte(ente);
			} catch (JumpEnteFlussiQuadraturaException e) {
				log.error("Errore in fase di elaborazione dei flussi di quadratura per l'ente: [" + codiceIpaEnte
						+ "]. L'ente viene saltato. Eccezione: [" + e.getMessage() + "]", e);
			}

			log.info("Fine elaborazione Flussi quadratura per l'ente: [" + codiceIpaEnte + "]");
		}

		Date end = new Date();

		log.info("***********************************************************************");
		log.info("Fine esecuzione di BatchChiediFlussoQuadratura - [" + end + "]");
		log.info("***********************************************************************");
		log.info("");

		long executionTime = end.getTime() - start.getTime();
		log.info("Tempo di esecuzione di BatchChiediFlussoQuadratura in millisecondi = [" + executionTime + "]");
	}

//	private static void recuperaFlussiQuadraturaPerEnte(MygovEnte ente) throws JumpEnteFlussiQuadraturaException {
//
//		String codiceIpaEnte = ente.getCodIpaEnte();
//
//		List<MygovEntepsp> listaPspPerEnte = entePspService.getByCodiceIpa(codiceIpaEnte);
//
//		if (listaPspPerEnte != null && listaPspPerEnte.size() > 0) {
//			log.info("Richiesta dei flussi quadratura per l'ente: [" + codiceIpaEnte + "]. PSP associati = " + listaPspPerEnte.size());
//
//			recuperaFlussiQuadraturaPerEnte(ente, listaPspPerEnte);
//
//		} else {
//			log.warn("Nessun PSP associato per l'ente: [" + codiceIpaEnte + "]");
//		}
//	}

	/**
	 * Effettua la chiamata al nodo nazionale per recuperare i flussi di quadratura per ente(codiceFiscale).
	 * @param ente
	 * @return
	 */
	private static void recuperaFlussiQuadraturaPerEnte(MygovEnte ente) {

		//Recupero codice ente interno ed esterno (mapping)
		String identificativoIntermediarioPA = fespBean.getIdentificativoIntermediarioPa();
		String identificativoStazioneIntermediarioPA = fespBean.getIdentificativoStazioneIntermediarioPa();
		String password = fespBean.getPassword();
		String identificativoDominio = ente.getCodiceFiscaleEnte();
		String codiceIpaEnte = ente.getCodIpaEnte();
		String rootQuadratura = fespBean.getRoot() + "/" + codiceIpaEnte + PATH_QUADRATURA;

//		String identificativoPSP = null;
//		for (MygovEntepsp entePsp : listaPspPerEnte) {
//			identificativoPSP = entePsp.getIdentificativoPsp();

			Holder<FaultBean> fault = new Holder<FaultBean>();
			Holder<TipoListaQuadrature> listaQuadrature = new Holder<TipoListaQuadrature>();

			try {
				log.debug("Chiamato nodoChiediElencoFlussiQuadratura con i parametri: identificativoIntermediarioPa ["
						+ identificativoIntermediarioPA
						+ "], identificativoStazioneIntermediarioPA ["
						+ identificativoStazioneIntermediarioPA
						+ "], password ["
						+ password
						+ "], identificativoDominio ["
						+ identificativoDominio
//						+ "], identificativoPSP ["
//						+ identificativoPSP 
						+ "]");

				pagamentiTelematiciRPTServiceClient.nodoChiediElencoQuadraturePA(
						identificativoIntermediarioPA, 
						identificativoStazioneIntermediarioPA, 
						password, 
						identificativoDominio, 
						fault, 
						listaQuadrature);
						

				if (listaQuadrature.value != null) {
					List<TipoIdQuadratura> elencoFlussiQuadraturaList = listaQuadrature.value.getIdQuadratura();
					if (elencoFlussiQuadraturaList != null && elencoFlussiQuadraturaList.size() > 0) {
						log.info("Flussi di quadratura ottenuti = [" + elencoFlussiQuadraturaList.size() + "]");

						//Elaborazione lista flussi del PSP
						for (TipoIdQuadratura tipoIdQuadratura : elencoFlussiQuadraturaList) {

							String identificativoFlusso = tipoIdQuadratura.getIdentificativoFlusso();
							XMLGregorianCalendar dataOraFlusso = tipoIdQuadratura.getDataOraFlusso();

							Calendar dataOraFlussoCalendar = dataOraFlusso.toGregorianCalendar();
							dataOraFlussoCalendar.clear(Calendar.MILLISECOND);
							
							String dataOraFlussoString = Utils.PLAIN_TIMESTAMP_UNTIL_SECONDS.format(dataOraFlussoCalendar.getTime());

							//Verifica se il flusso e' gia' presente
							Date dataOraRiferimentoFlusso = new Date(dataOraFlussoCalendar.getTimeInMillis());
							Date currentDate = new Date();

							//Controlla se il flusso e' gia' stato scaricato
							MygovFlussoQuadSpc myGovflussoSpc = flussoQuadSpcService.getByKeyInsertable(codiceIpaEnte,
									identificativoFlusso, dataOraRiferimentoFlusso);

							//Se e' gia' presente lo salto e proseguo con quelli successivi
							if (myGovflussoSpc != null) {
								log.info("Verificato che il Flusso per codiceIpaEnte [" + codiceIpaEnte 
//										+ "], identificativoPSP [" + identificativoPSP
										+ "], identificativoFlusso [" + identificativoFlusso + "], dataOraFlusso [" + dataOraFlussoString
										+ "] e' gia' stato processato");
							}
							//Altrimenti inserisco la traccia del flusso e poi do il via al download
							else {
								//Inserimento della chiave del Flusso
								try {
									flussoQuadSpcService.insert( codiceIpaEnte, identificativoFlusso,
											dataOraRiferimentoFlusso, currentDate);
								} catch (DataIntegrityViolationException dive) {
									log.warn("Inserimento non possibile in quanto il Flusso per codiceIpaEnte [" + codiceIpaEnte 
//											+ "], identificativoPSP [" + identificativoPSP 
											+ "], identificativoFlusso [" + identificativoFlusso + "], dataOraFlusso ["
											+ dataOraFlussoString + "] e' gia' stato processato. Il flusso viene saltato.");
									//Salta il flusso e passa al successivo
									continue;
								} catch (DataAccessException dae) {
									log.error("Errore in fase di inserimento del Flusso per codiceIpaEnte [" + codiceIpaEnte 
//											+ "], identificativoPSP ["+ identificativoPSP 
											+ "], identificativoFlusso [" + identificativoFlusso + "], dataOraFlusso ["
											+ dataOraFlussoString + "]  nel DB");
									//Salta il flusso e passa al successivo
									continue;
								}

								//Scarica il flusso dal nodo spc
								Holder<DataHandler> xmlQuadratura = new Holder<DataHandler>();

								pagamentiTelematiciRPTServiceClient.nodoChiediQuadraturaPA(identificativoIntermediarioPA,
										identificativoStazioneIntermediarioPA, password, identificativoDominio, identificativoFlusso, fault, xmlQuadratura);

								//Upload del file su File System
								try {
									scriviFlusso(xmlQuadratura, rootQuadratura, codiceIpaEnte, identificativoFlusso, dataOraFlussoString,
											dataOraRiferimentoFlusso);
								} catch (Exception e) {
									log.error("Errore nella fase di scaricamento del flusso su FileSystem per codiceIpaEnte [" 
								+ codiceIpaEnte
//											+ "], identificativoPSP [" + identificativoPSP 
											+ "], identificativoFlusso [" + identificativoFlusso
											+ "], dataOraFlusso [" + dataOraFlussoString + "]", e);
									flussoQuadSpcService.updateByKey(codiceIpaEnte, identificativoFlusso,
											dataOraRiferimentoFlusso, null, 0L, new Date(), StatiEsecuzione.ERRORE_CARICAMENTO.getValue());
									try {
										if (xmlQuadratura.value != null && xmlQuadratura.value.getInputStream() != null)
											xmlQuadratura.value.getInputStream().close();
									} catch (Exception ex) {
										//Nothing to do
									}
								}
							}
						}
					} else {
						log.warn("------------------------------------");
						log.warn("Nessun flusso di quadratura ottenuto");
						log.warn("------------------------------------");
					}
				}else{
					log.warn("------------------------------------");
					log.warn("Nessun flusso di quadratura ottenuto");
					log.warn("------------------------------------");
				}

			} catch (Exception e) {
				log.error("Errore nella richiesta elenco flussi quadratura per l'ente [" + codiceIpaEnte
//						+ "] per il PSP [" + identificativoPSP
						+ "]. L'elenco flussi per il singolo PSP non viene recuperato. Errore: [" + e.getMessage()
						+ "]", e);
			}
//		}
	}

	private static void scriviFlusso(Holder<DataHandler> xmlFlusso, String rootQuadratura,
			String codiceIpaEnte//, String identificativoPSP
			, String identificativoFlusso, String dataOraFlussoString,
			Date dataOraRiferimentoFlusso) throws Exception {

		if (xmlFlusso.value == null || xmlFlusso.value.getInputStream() == null) {
			throw new Exception("Stream vuoto per l'ente [" + codiceIpaEnte 
//					+ "] per il PSP [" + identificativoPSP
					+ "] del flusso [" + identificativoFlusso + " - " + dataOraFlussoString + "]");
		}
		//LO STREAM DEL FLUSSO DI QUADRATURA
		InputStream xmlInputStream = xmlFlusso.value.getInputStream();
		
		GregorianCalendar now = new GregorianCalendar();
		String cartella_anno_mese = now.get(GregorianCalendar.YEAR) + "_" + 
				((now.get(GregorianCalendar.MONTH) + 1) > 9 ? (now.get(GregorianCalendar.MONTH) + 1) : "0" + (now.get(GregorianCalendar.MONTH) + 1));

		//CREAZIONE DELLE CARTELLE SE NON ESISTONO
		StringBuffer subDirectory = new StringBuffer();
		subDirectory.append(rootQuadratura);
		subDirectory.append(cartella_anno_mese + "/");

		File file = new File(subDirectory.toString());
		if (!file.exists()) {
			file.mkdirs();
		}

		//Base del nome del file scaricato
		StringBuffer nomeFileScaricatoBaseName = new StringBuffer();
//		nomeFileScaricatoBaseName.append(identificativoPSP);
//		nomeFileScaricatoBaseName.append("_");
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

		StringBuffer fileName = new StringBuffer();
		fileName.append(subDirectory);
		fileName.append(nomeFileScaricato);

		//File per la gestione dell'upload su FileSystem
		File fileTemporary = new File(fileTemporaryName.toString());

		/*
		 * ************************************************
		 * SCRITTURA DEL FILE TEMPORANEO DI QUADRATURA 
		 * ************************************************ 
		 */
		OutputStream outStream = null;
		log.debug("Scrittura del file temporaneo di quadratura [" + fileTemporaryName 
//				+ "]  per il PSP [" + identificativoPSP 
				+ "] per l'ente [" + codiceIpaEnte + "].");
		try {
//			if (true) throw new FileNotFoundException();				
			outStream = new FileOutputStream(fileTemporary);

			byte[] buffer = new byte[CHUNK_DIMENSION];
			int bytesRead;
			//read from is to buffer
			while ((bytesRead = xmlInputStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			log.error("Errore nella scrittura del file temporaneo di quadratura [" + fileTemporaryName
//					+ "]  per il PSP [" + identificativoPSP 
					+ "] per l'ente [" + codiceIpaEnte + "].", e);
			throw (e);
		} finally {
			try {
				xmlInputStream.close();
			} catch (IOException ex) {
				//Nothing to do
			}
			if (outStream != null) {
				try {
					outStream.flush();
				} catch (IOException ex) {
					//Nothing to do
				}
				try {
					outStream.close();
				} catch (IOException ex) {
					//Nothing to do
				}
			}
		}

		log.debug("Il file [" + fileTemporaryName + "] temporaneo per l'ente ["
				+ codiceIpaEnte + "] e' stato correttamente uploadato.");

		/*
		 * ***************************************************************
		 * RINOMINO FILE TEMPORANEO NEL FILE DI QUADRATURA EFFETTIVO
		 * *************************************************************** 
		 */
		File fileQuadratura = new File(fileName.toString());
		boolean renamingEsito = fileTemporary.renameTo(fileQuadratura);
		if (renamingEsito) {
			log.info("Il file [" + fileName 
//					+ "] per il PSP [" + identificativoPSP 
					+ "] per l'ente ["
					+ codiceIpaEnte + "] e' stato correttamente uploadato.");
		} else {
			log.error("Errore nel rinominare il file temporaneo nel file [" + fileName
//					+ "] per il PSP [" + identificativoPSP 
					+ "] per l'ente [" + codiceIpaEnte + "].");
			throw new Exception("Errore nel rinominare il file temporaneo nel file [" + fileName
//					+ "] per il PSP [" + identificativoPSP 
					+ "] per l'ente [" + codiceIpaEnte + "].");
		}

		//Se il processo di scrittura su FileSystem e' andato a buon fine, effettua l'aggiornamento su DB con i dati del file salvato sul FileSystem
		long numDimensioneFileScaricato = fileQuadratura.length();

		flussoQuadSpcService.updateByKey(codiceIpaEnte, identificativoFlusso, dataOraRiferimentoFlusso,
				PATH_QUADRATURA + cartella_anno_mese + "/" + nomeFileScaricato.toString(), numDimensioneFileScaricato, new Date(), StatiEsecuzione.CARICATO.getValue());

	}
}
