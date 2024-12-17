/**
 *     MyPay - Payment portal of Regione Veneto.
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.mygov.payment.mypay4.scheduled.exportconservazione;

import it.regioneveneto.mygov.payment.mypay4.model.ExportConservazione;
import it.regioneveneto.mygov.payment.mypay4.service.FlussoConservazioneService;
import it.regioneveneto.mygov.payment.mypay4.service.MailService;
import it.regioneveneto.mygov.payment.mypay4.service.NodoFespService;
import it.regioneveneto.mygov.payment.mypay4.service.OperatoreService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class BatchConservazioneExport {

	@Autowired
	private FlussoConservazioneService flussoConservazioneService;
	@Autowired
	private NodoFespService nodoFespService;
	@Value("${mybox.path.root}")
	private String rootPathFile;

	@Value("${mypay.path.relative.data}")
	private String relativePathFile;

	private String from;
	private String sender;

	@Autowired
	private OperatoreService operatoreService;

	@Autowired
	private MailService asyncMailService;

	public void execute() {

		log.info("Caricamento del contesto Spring");

		/*
		 * 1) List tutti gli export da effettuare:
		 */
		try {
			List<ExportConservazione> exportConservazioneList = flussoConservazioneService
					.getExportConservazioneDaEffettuare();
			log.debug("Export conservazioni da effettuare: "
					+ (exportConservazioneList != null ? exportConservazioneList.size() : 0));
			if (exportConservazioneList != null) {
				for (ExportConservazione exportConservazione : exportConservazioneList) {
					File filezippato = null;
					log.debug("Export Conservazione: " + exportConservazione);
					try {
						/// Prendo i dati da conservare in rt o rpt conservzione sul db del fesp;
						List<String> listData = null;
						if (exportConservazione.getTipoTracciato()
								.equals(Constants.TIPO_TRACCIATO_CONSERVAZIONE.RPT.toString())) {
							log.debug("Export conservazione RPT per ente: "
									+ exportConservazione.getMygovEnteId().getCodiceFiscaleEnte());
							log.debug("Data Inizio: " + exportConservazione.getDtInizioEstrazione());
							log.debug("Data Fine: " + exportConservazione.getDtFineEstrazione());
							listData = nodoFespService.getListaRPT(
									exportConservazione.getMygovEnteId().getCodiceFiscaleEnte(),
									exportConservazione.getDtInizioEstrazione(),
									exportConservazione.getDtFineEstrazione());

						} else { // caso RT
							log.debug("Export conservazione RT per ente: "
									+ exportConservazione.getMygovEnteId().getCodiceFiscaleEnte());
							log.debug("Data Inizio: " + exportConservazione.getDtInizioEstrazione());
							log.debug("Data Fine: " + exportConservazione.getDtFineEstrazione());
							listData = nodoFespService.getListaRT(
									exportConservazione.getMygovEnteId().getCodiceFiscaleEnte(),
									exportConservazione.getDtInizioEstrazione(),
									exportConservazione.getDtFineEstrazione());

						}
						for (String row : listData) {
							log.debug("ROW: " + row);
						}

						/// Costruisco il path in vui nseririre il file
						String pathFile = getPathFile(exportConservazione);
						int indexSlash = pathFile.lastIndexOf("/");
						File myFile = new File(pathFile.substring(0, indexSlash));
						boolean dirCreated = myFile.mkdirs();
						Charset utf8 = StandardCharsets.UTF_8;
						Files.write(Paths.get(pathFile), listData, utf8, StandardOpenOption.CREATE,
								StandardOpenOption.APPEND);

						/// ZIPPO
						File file = new File(pathFile);
						int i = file.getName().lastIndexOf('.');
						String name = file.getName().substring(0, i);
						filezippato = new File(file.getParent(), name + ".zip");

						FileOutputStream fos = new FileOutputStream(filezippato.getAbsolutePath());
						ZipOutputStream zos = new ZipOutputStream(fos);

						zos.putNextEntry(new ZipEntry(file.getName()));

						byte[] bytes = Files.readAllBytes(Paths.get(pathFile));
						zos.write(bytes, 0, bytes.length);
						zos.closeEntry();
						zos.close();
						// File filezippato = new File(zipFileName);
						// Cancello il file csv
						try {
							file.delete();
						} catch (Exception e) {
							log.error("Errore nella cancellazione del file", e);

						}
						long fileSize = filezippato.length();
						log.debug("Filezippato): " + filezippato.getAbsolutePath());
						int indexOfPath = filezippato.getAbsolutePath().indexOf("/EXPORT");
						String pathRelativoInDB = filezippato.getAbsolutePath().substring(indexOfPath);
						log.debug("size del file: " + fileSize);
						flussoConservazioneService.updateExportConservazione(exportConservazione, pathRelativoInDB,
								Constants.STATO_EXPORT_ESEGUITO, fileSize);
						try {
							// inviaEmail(exportConservazione);
						} catch (Exception e) {
							log.error("Errore nell'invio mail di conservazione" + e, e);
							log.error("Impossibile inviare email di conferma");

						}

					} catch (Exception e) {
						log.error("Errore nell'export di conservazione" + e, e);
						String path = filezippato != null ? filezippato.getAbsolutePath() : null;
						flussoConservazioneService.updateExportConservazione(exportConservazione, path,
								Constants.STATO_EXPORT_ERROR, 0);
						try {
							// inviaEmailError(exportConservazione);
						} catch (Exception ex) {
							log.error("Errore nell'invio mail di conservazione" + ex, ex);
							log.error("Impossibile inviare email di conferma");
						}
					}

				}
			}
		} catch (Exception e) {
			log.error("Errore nell'export di conservazione" + e, e);
		} finally {
			deleteOldFiles();
		}

	}

	private void deleteOldFiles() {

		try {
			List<ExportConservazione> listaExport = flussoConservazioneService.getExportDaCancellare();
			log.debug("Export da cancellare: " + listaExport);
			for (ExportConservazione exp : listaExport) {
				// trovo il path.
				String path = getPathDaCancellare(exp);
				log.debug("Cancello file: " + path);
				File file = new File(path);
				file.delete();
				flussoConservazioneService.updateExportConservazione(exp, null, Constants.STATO_EXPORT_CANCELLATO, 0);
			}
		} catch (Exception e) {
			log.error("Errore nella cancellazione dei file Export: " + e, e);
		}

	}

//	private void inviaEmail(ExportConservazione flusso) throws Exception {
//		Locale locale = Locale.ITALY;
//		ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", locale);
//		
//	
//		String subject = java.text.MessageFormat.format(
//								resourceBundle.getString("pa.funzionalita.massiva.esito.mailSubject"), flusso.getDeNomeFileGenerato()); 
////						 resourceBundle.getString("pa.funzionalita.massiva.esito.mailSubject");
//		String content = java.text.MessageFormat.format(
//				resourceBundle.getString("pa.funzionalita.massiva.esito.mailContent.start"), flusso.getDeNomeFileGenerato());
//		String content2 = java.text.MessageFormat.format(
//				resourceBundle.getString("pa.funzionalita.massiva.esito.mailContent_success"), flusso.getDeNomeFileGenerato());
//		content = content.concat(content2);
////		String firma = String.valueOf(new Date());
//		try {
//			// -->recupero email amministratori poi invio
//			List<String> listaEmailDestinatari = operatoreService.getAllEmailAndSend();
//			if (listaEmailDestinatari != null && listaEmailDestinatari.size() > 0) {
//				for (String email : listaEmailDestinatari) {
//					
//					try {
//						
//						asyncMailService.sendMail_esitoCaricamento(from, sender, subject, content, email);
//					} catch (Exception ex) {
//						
//						log.error("Errore durante l'invio mail ad indirizzo [" + email + "] " + ex.getMessage());
//						throw new Exception(ex);
//					}	
//					
////					// INVIA MAIL A SOGGETTO VERSANTE SE MAIL ESISTE ED E' DIVERSA DA MAIL SOGGETTO
////					// PAGATORE
////					try {
////						batchConfigEmail.sendMailFromBatch(email, subject, content, firma, mailSender, velocityEngine);
////						log.info("E-mail inviata al seguente indirizzo [" + email + "] ");
////					} catch (Exception ex) {
////						log.error("Errore durante l'invio mail ad indirizzo [" + email + "] " + ex.getMessage());
////						throw new Exception(ex);
////					}
//				}
//			}
//		} catch (Exception e) {
//			log.error(e.getMessage());
//			throw new Exception(e);
//		}
//		
//	}
//	
//	private void inviaEmailError(ExportConservazione flusso) throws Exception {
//		Locale locale = Locale.ITALY;
//		ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", locale);
//		
//	
//		
//		String subject = java.text.MessageFormat.format(
//							resourceBundle.getString("pa.funzionalita.massiva.esito.mailSubject"), flusso.getDeNomeFileGenerato()); 
////		 				resourceBundle.getString("pa.funzionalita.massiva.esito.mailSubject");
//		String content = java.text.MessageFormat.format(
//				resourceBundle.getString("pa.funzionalita.massiva.esito.mailContent.start"), flusso.getDeNomeFileGenerato());
//		String content2 = java.text.MessageFormat.format(
//				resourceBundle.getString("pa.funzionalita.massiva.esito.mailContent_error"), flusso.getDeNomeFileGenerato());
//		content = content.concat(content2);
////		String firma = String.valueOf(new Date());
//		try {
//			// -->recupero email amministratori poi invio
//			List<String> listaEmailDestinatari = operatoreService.getAllEmailAndSend();
//			if (listaEmailDestinatari != null && listaEmailDestinatari.size() > 0) {
//				for (String email : listaEmailDestinatari) {
//					
//					try {
//						
//						asyncMailService.sendMail_esitoCaricamento(from, sender, subject, content, email);
//					} catch (Exception ex) {
//						
//						log.error("Errore durante l'invio mail ad indirizzo [" + email + "] " + ex.getMessage());
//						throw new Exception(ex);
//					}	
//					
////					// INVIA MAIL A SOGGETTO VERSANTE SE MAIL ESISTE ED E' DIVERSA DA MAIL SOGGETTO
////					// PAGATORE
////					try {
////						batchConfigEmail.sendMailFromBatch(email, subject, content, firma, mailSender, velocityEngine);
////						log.info("E-mail inviata al seguente indirizzo [" + email + "] ");
////					} catch (Exception ex) {
////						log.error("Errore durante l'invio mail ad indirizzo [" + email + "] " + ex.getMessage());
////						throw new Exception(ex);
////					}
//				}
//			}
//		} catch (Exception e) {
//			log.error(e.getMessage());
//			throw new Exception(e);
//		}
//	}
//

	private String getPathFile(ExportConservazione exportConservazione) {
		String codIpaEnte = exportConservazione.getMygovEnteId().getCodIpaEnte();
		LocalDateTime currentdate = LocalDateTime.now();
		int currentYear = currentdate.getYear();
		int currentMonth = currentdate.getMonth().getValue();
		String monthStr = currentMonth > 9 ? Integer.toString(currentMonth) : "0" + Integer.toString(currentMonth);
		String yearMonthStr = currentYear + "_" + monthStr;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
		String formattedString = currentdate.format(formatter);
		String rootPathNormalized = new String(rootPathFile);
		if (!rootPathNormalized.trim().endsWith("/")) {
			rootPathNormalized = rootPathNormalized + "/";
		}
		rootPathNormalized = rootPathNormalized + "/" + relativePathFile + "/";
		String tipoEstr = exportConservazione.getTipoTracciato();
		String pathFile = rootPathNormalized + codIpaEnte + "/EXPORT/" + yearMonthStr + "/" + codIpaEnte + "-"
				+ exportConservazione.getMygovExportConservazioneId() + "_ESTRAZIONE_CONS_" + formattedString + "_"
				+ tipoEstr + ".csv";
		return pathFile;
	}

	private String getPathDaCancellare(ExportConservazione exp) {
		String rootPathNormalized = new String(rootPathFile);
		if (!rootPathNormalized.trim().endsWith("/")) {
			rootPathNormalized = rootPathNormalized + "/";
		}
		String path = rootPathNormalized + exp.getMygovEnteId().getCodIpaEnte() + exp.getDeNomeFileGenerato();
		return path;
	}

}
