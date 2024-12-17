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
package it.regioneveneto.mygov.payment.mypay4.scheduled.importflusso;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.dto.ImportFlussoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.NazioneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.ProvinciaTo;
import it.regioneveneto.mygov.payment.mypay4.dto.ResultImportFlussoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.NameSpaceFilterBilancio;
import it.regioneveneto.mygov.payment.mypay4.util.SupportedFileVersion;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediIUV;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediIUVRisposta;
import it.veneto.regione.schemas._2012.pagamenti.ente.Bilancio;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSConstants;
import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.jms.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Component
@Slf4j
@ConditionalOnProperty(name = AbstractApplication.NAME_KEY, havingValue = ImportFlussoTaskApplication.NAME)
public class ImportFlussoTaskService {

    private final static String separator = FileSystems.getDefault().getSeparator();

    @Autowired
    private DovutoService dovutoService;

    @Autowired
    private FlussoService flussoService;

    @Autowired
    private JasperService jasperService;

    @Autowired
    private EnteTipoDovutoService enteTipoDovutoService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private EnteService enteService;

    @Autowired
    private ImportDovutiService importDovutiService;

    @Autowired
    private PagamentiTelematiciRP pagamentiTelematiciRPClient;

    @Autowired
    private TassonomiaService tassonomiaService;

    @Value("${task.importFlusso.context.directory_root_elaborazione}")
    private String managePathString;

    @Value("${task.importFlusso.context.directory_root_enti}")
    private String dataPathString;

    @Value("${mypay.path.import.dovuti}")
    private String dovutiImportPath;

    @Value("${spring.artemis.host}")
    private String mqHost;

    @Value("${spring.artemis.port}")
    private String mqPort;

    @Value("${spring.artemis.user}")
    private String mqUsername;

    @Value("${spring.artemis.password}")
    private String mqPassword;

    @Value("${queue.import-dovuti}")
    private String mqQueue;

    @Value("${pa.gpd.enabled}")
    private boolean gpdEnabled;

    private static String cleanDoubleQuote(String s) {
        String str = "";
        if (StringUtils.isNotBlank(s)) {
            str = s.trim();
            if (str.charAt(0) == '\"') {
                if (str.charAt(str.length() - 1) == '\"') {
                    str = str.substring(1, str.length() - 1);
                }
            }
        }
        return StringUtils.replace(str, "\\\"", "\"");
    }

    public List<Long> importaFlusso() {
        //BatchImportV1_4
        StopWatch swBatchImportFlusso = new StopWatch("Batch ImportFlussoTaskService :: importaFlusso");
        swBatchImportFlusso.start();
        log.debug("ImportFlussoTaskService :: importaFlusso :: Start batch ");

        //CREATE_FOLDER_IMPORT - TALEND
        log.debug("ImportFlussoTaskService :: importaFlusso :: Start createFolderImport ");
        createFolderImport();
        log.debug("ImportFlussoTaskService :: importaFlusso :: End createFolderImport ");

        //LEGGI_ENTI - TALEND
        log.debug("ImportFlussoTaskService :: importaFlusso :: Start leggiEnti ");
        //BatchImport - VERSIONE SENZA QUEUE
        //leggiEnti();
        //BatchImportQueue - VERSIONE CON QUEUE
        var idsFlusso = leggiMessageQueue();
        swBatchImportFlusso.stop();
        log.debug("ImportFlussoTaskService :: importaFlusso :: End leggiEnti :: {} secondi", swBatchImportFlusso.getTotalTimeSeconds());
        return idsFlusso;
    }

    private void createFolderImport() {
        try {
            //CREATE_FOLDER_IMPORT - TALEND
            Path managePath = Paths.get(managePathString);

            //Crea il path del Manage se non esiste, se esiste gia non lancia eccezione
            Files.createDirectories(managePath);

            //Creo il path del failed e creo la folder se non esiste, se esiste gia non lancia eccezione
            Path manageFailedPath = managePath.resolve("failed");
            Files.createDirectories(manageFailedPath);

            //Creo il path del log e creo la folder se non esiste, se esiste gia non lancia eccezione
            Path manageLogPath = managePath.resolve("log");
            Files.createDirectories(manageLogPath);

            //Creo il path del store e creo la folder se non esiste, se esiste gia non lancia eccezione
            Path manageStorePath = managePath.resolve("store");
            Files.createDirectories(manageStorePath);

            //Creo il path del tmp_import e creo la folder se non esiste, se esiste gia non lancia eccezione
            Path manageTmpImportPath = managePath.resolve("tmp_import");
            Files.createDirectories(manageTmpImportPath);

            //Creo il path del work e creo la folder se non esiste, se esiste gia non lancia eccezione
            Path manageWorkPath = managePath.resolve("work");
            Files.createDirectories(manageWorkPath);
        } catch (Exception e) {
            log.error("ImportFlussoTaskService :: createFolderImport :: ERROR :: {} :: {}", e.getMessage(), e.getStackTrace());
            manageLog(e, null, null);
        }
    }

    private void manageLog(Exception e, String c, String d) {
        log.error("ImportFlussoTaskService :: manageLog :: ERROR :: {} :: codErrore :: {} :: deErrore :: {}", e, c, d);
    }

    private List<Long> leggiMessageQueue() {
        log.debug("ImportFlussoTaskService :: leggiMessageQueue :: Start leggiMessageQueue");
        Connection connection = null;
        Session session;
        List<String> messageList = new ArrayList<>();
        Map<String, Message> map = new HashMap<>();
        List<Long> idsFlusso = new ArrayList<>();
        try {
            ConnectionFactory cf = ActiveMQJMSClient.createConnectionFactory("tcp://" + mqHost + ":" + mqPort + "?consumerWindowSize=0", "mypay4");
            connection = cf.createConnection(mqUsername, mqPassword);
            log.debug("ImportFlussoTaskService :: leggiMessageQueue :: Connection creata");
            // Step 1. Create a JMS Session
            //session = connection.createSession(Session.CLIENT_ACKNOWLEDGE);

            //session = connection.createSession(Session.AUTO_ACKNOWLEDGE);
            session = connection.createSession(false, ActiveMQJMSConstants.INDIVIDUAL_ACKNOWLEDGE);

            log.debug("ImportFlussoTaskService :: leggiMessageQueue :: Session creata");
            // Step 2. Create a JMS Message Producer
            Destination destination = session.createQueue(mqQueue);
            // Step 3. Create a JMS Message Consumer
            MessageConsumer messageConsumer = session.createConsumer(destination);
            // Step 4. Start the Connection
            connection.start();
            // Step 5. Receive the message
            Message message = messageConsumer.receiveNoWait();
            TextMessage textMessage;

            int i = 1;
            final int numPrefetchLimit = 3;

            if (message == null)
                log.debug("ImportFlussoTaskService :: leggiMessageQueue :: Nessun messaggio recuperato dalla coda");

            //Verifico il message
            while (message != null && i <= numPrefetchLimit) {
                textMessage = (ActiveMQTextMessage) message;
                log.debug("ImportFlussoTaskService :: leggiMessageQueue :: Received message :: {}", textMessage.getText());
                if (((ActiveMQTextMessage) message).getText().trim().equals("NO_REMOVE")) {
                    message.acknowledge();
                } else {
                    //Aggiungo message nella lista e nella mappa
                    messageList.add(textMessage.getText());
                    map.put(textMessage.getText(), message);
                    i++;
                }
                //Se posso ancora prenderne allora continuo
                if (i <= numPrefetchLimit) {
                    message = messageConsumer.receiveNoWait();
                }
            }


            //Ciclo la lista di messaggio
            if (!messageList.isEmpty()) {
                String messaggioJms;
                for (i = 0; i < messageList.size(); i++) {
                    messaggioJms = messageList.get(i);
                    try {
                        log.debug("ImportFlussoTaskService :: leggiMessageQueue :: Elaboro il messaggio: {}", messaggioJms);

                        //Recupero ente grazie al messaggio JMS
                        String codIpaEnte = messaggioJms.substring(messaggioJms.indexOf(separator, 1) + 1, messaggioJms.indexOf(separator, messaggioJms.indexOf(separator) + 1));
                        Ente ente = enteService.getEnteByCodIpa(codIpaEnte);

                        //Recupero i path esattamente come nel batch d'import normale
                        String nomeFile = messaggioJms.substring(messaggioJms.lastIndexOf(separator) + 1);
                        String relativePath = ente.getCodIpaEnte() + separator + dovutiImportPath;
                        Path fileLocation = Paths.get(dataPathString, relativePath, nomeFile);
                        String pathNomeFile = fileLocation.toString();
                        nomeFile = nomeFile.replace(".zip", "").replace(".ZIP", "");
                        String pathFile = fileLocation.getParent().toString();

                        //Proseguo con l'elaborazione del file
                        long idFlusso = validaFlusso(pathNomeFile, pathFile, nomeFile, ente);
                        idsFlusso.add(idFlusso);

                    } catch (Exception e) {
                        log.error("ImportFlussoTaskService :: leggiMessageQueue :: ERROR :: {}", e.getMessage());
                        manageLog(e, null, null);
                    }

                    //Acko il messaggio jms letto
                    Message messageToAck = null;
                    try {
                        messageToAck = map.get(messaggioJms);
                        messageToAck.acknowledge();
                        log.debug("ImportFlussoTaskService :: leggiMessageQueue :: Eseguito caricamento file:" + messaggioJms);
                        log.debug("ImportFlussoTaskService :: leggiMessageQueue :: Eseguito ack:" + messageToAck);
                    } catch (Exception e) {
                        log.error("ImportFlussoTaskService :: leggiMessageQueue :: ERROR :: Couldn't ack message :: {} :: ERROR :: {}", messageToAck, e.getMessage());
                        manageLog(e, null, null);
                    }
                }
            }
        } catch (Exception e) {
            log.error("ImportFlussoTaskService :: leggiMessageQueue :: ERROR ::  {}", e.getMessage());
            manageLog(e, null, null);
        } finally {
            // Step 6. Be sure to close our JMS resources!
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ex) {
                    log.error("ImportFlussoTaskService :: leggiMessageQueue :: ERROR :: Couldn't close JMSConnection :: {}", ex.getMessage());
                }
            }
        }
        log.debug("ImportFlussoTaskService :: leggiMessageQueue :: End leggiMessageQueue");

        return idsFlusso;

    }

    private long validaFlusso(String pathNomeFile, String pathFile, String nomeFile, Ente ente) {
        String codErrore = "";
        String deErrore = "";
        try {
            log.debug("ImportFlussoTaskService :: validaFlusso :: Start validaFlusso ");
            //Remember, pathNomeFile è il path a tutto il file, pathFile è il path della folder del file e nomeFile non ha l'estensione
            log.debug("ImportFlussoTaskService :: validaFlusso :: Start validaFlusso :: pathNomeFile :: {} :: " +
                            "pathFile :: {} ::nomeFile :: {} ::codIpaEnte :: {} ::codiceFiscaleEnte :: {} ::", pathNomeFile, pathFile, nomeFile,
                    ente.getCodIpaEnte(), ente.getCodiceFiscaleEnte());

            //Variabili talend
            boolean statoElaborazione = true;
            boolean doNothing = false;
            boolean isWs = false;
            String valueAUT = null;
            String valueMD5 = null;
            String iuf = null;
            String deNomeOperatoreRichiedente = null;
            String eMailOperatoreRichiedente = null;
            String calculatedMD5 = null;
            Path fileCsv = null;
            Utente utente = null;
            ImportDovuti importDovuto = null;
            ImportDovutiOut out = null;
            ResultImportFlussoTo resultElaboraTo = new ResultImportFlussoTo();
            String percorsoFileAUT = pathNomeFile.replace(".zip", ".auth");
            String percorsoFileMD5 = pathNomeFile.replace(".zip", ".md5");
            String percorsoFile = pathNomeFile.replace(nomeFile + ".zip", "");
            GregorianCalendar now = new GregorianCalendar();
            String cartellaAnnoMese = now.get(GregorianCalendar.YEAR) + "_" +
                    ((now.get(GregorianCalendar.MONTH) + 1) > 9 ? (now.get(GregorianCalendar.MONTH) + 1)
                            : "0" + (now.get(GregorianCalendar.MONTH) + 1));

            //Creo la cartellaToPath
            Path dataPath = Paths.get(dataPathString);
            Path cartellaToPath = dataPath.resolve(ente.getCodIpaEnte()).resolve("IMPORT_ELABORATI").resolve(cartellaAnnoMese).resolve(nomeFile);
            Files.createDirectories(cartellaToPath);

            //Recupero il valore del file Auth
            try {
                valueAUT = Files.readString(Paths.get(percorsoFileAUT));
            } catch (Exception e) {
                codErrore = "PAA_IMPORT_NO_FILE_AUTH";
                deErrore = "Impossibile recuperare il valore dal file .auth per il caricamento del file: " + nomeFile;
                statoElaborazione = false;
                manageLog(e, codErrore, deErrore);
                resultElaboraTo.setCodErrori(codErrore);
                resultElaboraTo.setDeErrori(deErrore);
            }

            //Recupero il valore del file MD5 solo se ho recuperato bene auth
            if (statoElaborazione) {
                try {
                    valueMD5 = Files.readString(Paths.get(percorsoFileMD5));
                } catch (Exception e) {
                    codErrore = "PAA_IMPORT_NO_FILE_MD5";
                    deErrore = "Impossibile recuperare il valore dal file .md5 per il caricamento del file: " + nomeFile;
                    statoElaborazione = false;
                    manageLog(e, codErrore, deErrore);
                    resultElaboraTo.setCodErrori(codErrore);
                    resultElaboraTo.setDeErrori(deErrore);
                }

                //Calcolo l'MD5 solo se ho recuperato bene l'MD5
                if (statoElaborazione) {
                    //Calcolo MD5
                    try {
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        InputStream is = Files.newInputStream(Paths.get(pathNomeFile));
                        DigestInputStream dis = new DigestInputStream(is, md);
                        IOUtils.toByteArray(dis);
                        calculatedMD5 = Hex.encodeHexString(md.digest());
                    } catch (Exception e) {
                        codErrore = "PAA_IMPORT_NO_FILE_MD5";
                        deErrore = "Impossibile calcolare il valore .md5 dal file caricato: " + nomeFile;
                        statoElaborazione = false;
                        manageLog(e, codErrore, deErrore);
                        resultElaboraTo.setCodErrori(codErrore);
                        resultElaboraTo.setDeErrori(deErrore);
                    }
                } else {
                    //caso di errore - talend
                    //ERROR_MD5
                    log.error("ImportFlussoTaskService :: validaFlusso :: {} :: {}", codErrore, deErrore);
                }
            } else {
                //caso di errore - talend
                //ERROR_AUTH
                log.error("ImportFlussoTaskService :: validaFlusso :: {} :: {}", codErrore, deErrore);
            }

            //Solo se è andato tutto bene fin'ora recupero il record import dovuti e provo update
            if (statoElaborazione) {
                //TALEND - SELECT IMPORT
                try {
                    importDovuto = importDovutiService.getFlussoImport(valueAUT);
                    String codStato = "IMPORT_IN_ELAB";
                    long hasUpdated;

                    //Proseguo a select utente o update solo se l'ho trovato
                    if (importDovuto != null) {
                        //TALEND - SELECT_PROP_ENTE_UTENTE
                        utente = importDovuto.getMygovUtenteId();
                        isWs = utente.getCodFedUserId().contains("WS_USER") ||
                                utente.getCodCodiceFiscaleUtente().equalsIgnoreCase("USER_JAVA");
                        if (StringUtils.isNotBlank(utente.getDeFirstname()) &&
                                StringUtils.isNotBlank(utente.getDeLastname())) {
                            deNomeOperatoreRichiedente = utente.getDeFirstname() + " " + utente.getDeLastname();
                            eMailOperatoreRichiedente = utente.getDeEmailAddress();
                        }

                        //TALEND - UPDATE_IMPORT
                        hasUpdated = importDovutiService.updateImportFlusso(importDovuto, codStato);

                        //Se update non ha restituito niente doNothing sarà true perché l'ho comunque trovato
                        if (hasUpdated == 0)
                            doNothing = true;
                    } else {
                        codErrore = "PAA_IMPORT_NO_MATCH_AUTH";
                        deErrore = "Non e' stata trovata corrispondenza tra il codice presente nel file .auth e nessuna riga di prenotazione presente nel database";
                        statoElaborazione = false;
                        manageLog(new Exception(codErrore + ":" + deErrore), codErrore, deErrore);
                        resultElaboraTo.setCodErrori(codErrore);
                        resultElaboraTo.setDeErrori(deErrore);
                    }
                } catch (Exception e) {
                    //Caso di eccezione in cui comunque non ha trovato il record
                    if (importDovuto == null) {
                        codErrore = "PAA_IMPORT_NO_MATCH_AUTH";
                        deErrore = "Non e' stata trovata corrispondenza tra il codice presente nel file .auth e nessuna riga di prenotazione presente nel database";
                        statoElaborazione = false;
                        manageLog(e, codErrore, deErrore);
                        resultElaboraTo.setCodErrori(codErrore);
                        resultElaboraTo.setDeErrori(deErrore);
                    } else //Caso di eccezione in cui ha trovato il record ma non è riesco ad aggiornare
                    {
                        doNothing = true;
                        codErrore = "PAA_IMPORT_NO_MATCH_AUTH";
                        deErrore = "Non e' stato possibile aggiornare la riga di prenotazione presente nel database con request token: " + valueAUT;
                        manageLog(e, codErrore, deErrore);
                        resultElaboraTo.setCodErrori(codErrore);
                        resultElaboraTo.setDeErrori(deErrore);
                    }
                }
            } else {
                //caso di errore - talend
                //CALCOLO_MD5
                log.error("ImportFlussoTaskService :: validaFlusso :: {} :: {}", codErrore, deErrore);
            }

            //Proseguo alla verifica del file anche se stato elaborazione è false
            if (!doNothing) {
                //TALEND - INIT_LOAD
                statoElaborazione = verificaNomeVersioneFile(nomeFile, ente, valueMD5, calculatedMD5, resultElaboraTo);
                iuf = nomeFile.split("-")[1]; //todo alternativa
            }

            //Copia, unzip e verifica si fanno solo se lo stato di elaborazione è ok e doNothing è false
            if (statoElaborazione && !doNothing) {
                //TALEND - COPIA_TO_WORK
                statoElaborazione = copiaToWork(pathFile, managePathString + separator + "work", ".zip", nomeFile, "copia", resultElaboraTo);
                codErrore = resultElaboraTo.getCodErrori();
                deErrore = resultElaboraTo.getDeErrori();

                if (statoElaborazione) {
                    //TALEND - UNZIP_IN_WORK SOLO SE COPIA_TO_WORK è andato bene
                    statoElaborazione = copiaToWork(managePathString + separator + "work", managePathString + separator + "work", ".zip", nomeFile, "unzip", resultElaboraTo);
                    codErrore = resultElaboraTo.getCodErrori();
                    deErrore = resultElaboraTo.getDeErrori();

                    //TALEND - VERIFICA_FILE (lo fa a prescindere dall'un zip)
                    fileCsv = Paths.get(managePathString).resolve("work").resolve(nomeFile + ".csv");
                    if (!Files.exists(fileCsv)) {
                        //caso di errore - talend
                        codErrore = "PAA_IMPORT_NO_CSV";
                        deErrore = "Il processo di caricamento dopo l'unzip del pacchetto caricato non trova il file .csv";
                        statoElaborazione = false;
                        manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
                        resultElaboraTo.setCodErrori(codErrore);
                        resultElaboraTo.setDeErrori(deErrore);
                    }
                } else {
                    //caso di errore - talend
                    //Errore nel copia to work
                    log.error("ImportFlussoTaskService :: validaFlusso :: {} :: {}", codErrore, deErrore);
                    manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
                }
            } else {
                //Caso di errore talend per la versione del file e la sua verifica
                log.error("ImportFlussoTaskService :: validaFlusso :: {} :: {}", codErrore, deErrore);
                statoElaborazione = false;
            }

            //Fare la call alla function anche se si è in errore, importante è do nothing ora
            if (!doNothing) {
                //TALEND - MOVE_ORIGINAL_TO_TMP
                statoElaborazione = copiaToWork(managePathString + separator + "work", managePathString + separator + "tmp_import", ".zip", nomeFile, "sposta", null);
                statoElaborazione = statoElaborazione && resultElaboraTo.getStatoElaborazione();
                codErrore = resultElaboraTo.getCodErrori();
                deErrore = resultElaboraTo.getDeErrori();

                //TALEND - INSERISCI_VALIDI - FUNCTION
                out = importDovutiService.callUpdateInsertFlussoFunction(ente.getMygovEnteId(), iuf, new Date(), new Date());

                //TALEND - STORE ID FLUSSO
                if (out.getEccezione() != null) {
                    if (out.getEccezione().equalsIgnoreCase("InCaricamentoFlussoException")) {
                        codErrore = "PAA_IMPORT_FLUSSO_IN_CARICAMENTO";
                        deErrore = "Impossibile inserire il flusso nella tabella, esiste un flusso in caricamento (non completato) da meno di 24 ore per stesso ente e stesso nome.";
                    } else if (out.getEccezione().equalsIgnoreCase("duplicatoFlussoException")) {
                        codErrore = "PAA_IMPORT_FLUSSO_DUPLICATO";
                        deErrore = "Impossibile inserire il flusso nella tabella, esiste un flusso valido caricato per stesso ente e stesso nome.";
                    } else {
                        codErrore = "PAA_IMPORT_FLUSSO_ERRORE_GENERICO";
                        deErrore = "Errore inserimento tabella flussi.";
                    }
                    statoElaborazione = false;
                    manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
                    resultElaboraTo.setCodErrori(codErrore);
                    resultElaboraTo.setDeErrori(deErrore);
                }
            }

            //Gli elabora si fanno soltanto se stato elaborazione è true
            if (statoElaborazione) {
                //TALEND - ELABORA
                String versione = nomeFile.split("-")[2]; //todo alternativa
                if (SupportedFileVersion.VERSIONE_1_0.getVersione_file().equalsIgnoreCase(versione)) {
                    resultElaboraTo = elaboraCsv(fileCsv, nomeFile, ente, utente, iuf, out.getSequenceValue(), versione);
                } else if (SupportedFileVersion.VERSIONE_1_1.getVersione_file().equalsIgnoreCase(versione)) {
                    resultElaboraTo = elaboraCsv(fileCsv, nomeFile, ente, utente, iuf, out.getSequenceValue(), versione);
                } else if (SupportedFileVersion.VERSIONE_1_2.getVersione_file().equalsIgnoreCase(versione)) {
                    resultElaboraTo = elaboraCsv(fileCsv, nomeFile, ente, utente, iuf, out.getSequenceValue(), versione);
                } else if (SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione)) {
                    resultElaboraTo = elaboraCsv(fileCsv, nomeFile, ente, utente, iuf, out.getSequenceValue(), versione);
                } else if (SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) {
                    resultElaboraTo = elaboraCsv(fileCsv, nomeFile, ente, utente, iuf, out.getSequenceValue(), versione);
                } else {
                    codErrore = "PAA_IMPORT_FILE_VERSIONE_ERR(" + versione + ")";
                    deErrore = "La versione tracciato del file '" + versione + "' non è supportata. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente' .";
                    statoElaborazione = false;
                    manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
                    resultElaboraTo.setCodErrori(codErrore);
                    resultElaboraTo.setDeErrori(deErrore);
                }
            } else {
                //Caso di errore che può esserci stato in copia, unzip, move o la call alla function
                log.error("ImportFlussoTaskService :: validaFlusso :: codErrore {} :: deErrore {}", codErrore, deErrore);
            }
            resultElaboraTo.setStatoElaborazione(statoElaborazione);

            //Prendo e aggiorno il flusso e mando le mail anche se stato elaborazione è falso
            if (!doNothing) {
                //TALEND - UPDATE FLUSSO
                try {
                    if (out.getSequenceValue() != null) {
                        Flusso flusso = flussoService.getById(out.getSequenceValue().longValue());
                        flusso.setFlgAttivo(true);
                        if (!resultElaboraTo.getStatoElaborazione())
                            flusso.setCodErrore((StringUtils.isNotBlank(resultElaboraTo.getCodErrori())) ? resultElaboraTo.getCodErrori() : "PAA_IMPORT_ERRORE_GENERICO");
                        if (resultElaboraTo.getNumRigheTotali() != null)
                            flusso.setNumRigheTotali(resultElaboraTo.getNumRigheTotali().longValue());
                        if (resultElaboraTo.getNumRigheImportate() != null)
                            flusso.setNumRigheImportateCorrettamente(resultElaboraTo.getNumRigheImportate().longValue());
                        flusso.setDeNomeOperatore(deNomeOperatoreRichiedente);
                        flusso.setDePercorsoFile(separator + "IMPORT_ELABORATI" + separator + cartellaAnnoMese + separator + nomeFile);
                        flusso.setDeNomeFile(nomeFile + ".zip");
                        flusso.setMygovEnteId(ente);
                        flusso.setCodRequestToken(valueAUT);
                        if (resultElaboraTo.getNumAvvisiGenerati() != null)
                            flusso.setPdfGenerati(resultElaboraTo.getNumAvvisiGenerati().longValue());
                        flussoService.updateFlussoImport(flusso);
                    } else {
                        codErrore = "PAA_IMPORT_ERROR_UPDATE_FLUSSO)";
                        deErrore = "Impossibile trovare il record del flusso con iuf: " + iuf;
                        manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
                        resultElaboraTo.setStatoElaborazione(false);
                        resultElaboraTo.setCodErrori(codErrore);
                        resultElaboraTo.setDeErrori(deErrore);
                    }
                } catch (Exception e) {
                    codErrore = "PAA_IMPORT_ERROR_UPDATE_FLUSSO)";
                    deErrore = "Impossibile aggiornare il record del flusso con iuf: " + iuf;
                    manageLog(e, codErrore, deErrore);
                    resultElaboraTo.setStatoElaborazione(false);
                    resultElaboraTo.setCodErrori(codErrore);
                    resultElaboraTo.setDeErrori(deErrore);
                }

                if (!gpdEnabled) {
                    //TALEND - MAIL OK
                    if (resultElaboraTo.getStatoElaborazione() && !isWs &&
                            resultElaboraTo.getNumRigheTotali().equals(resultElaboraTo.getNumRigheImportate())) {
                        try {
                            importDovutiService.sendMailImportFlussoOk(ente, resultElaboraTo, eMailOperatoreRichiedente);
                        } catch (Exception e) {
                            log.warn("ImportFlussoTaskService :: validaFlusso :: MailOk :: Error sending mail for iuf :: {} :: ERROR", iuf, e);
                        }
                    }

                    //TALEND - MAIL SCARTI
                    if (resultElaboraTo.getStatoElaborazione() &&
                            (resultElaboraTo.getNumRigheTotali() == null ||
                                    (!resultElaboraTo.getNumRigheTotali().equals(resultElaboraTo.getNumRigheImportate())))) {
                        try {
                            importDovutiService.sendMailImportFlussoScarti(ente, resultElaboraTo, eMailOperatoreRichiedente);
                        } catch (Exception e) {
                            log.error("ImportFlussoTaskService :: validaFlusso :: MailScarti :: Error sending mail for iuf :: {} :: ERROR", iuf, e);
                        }
                    }
                } else {
                    // se gpd abilitato
                    // si invia la mail solo se non si importato nulla

                    if (resultElaboraTo.getStatoElaborazione() && !isWs &&
                            resultElaboraTo.getNumRigheImportate() == 0) {
                        try {
                            importDovutiService.sendMailImportFlussoScarti(ente, resultElaboraTo, eMailOperatoreRichiedente);
                        } catch (Exception e) {
                            log.error("ImportFlussoTaskService :: validaFlusso :: MailScarti :: Error sending mail for iuf :: {} :: ERROR", iuf, e);
                        }
                    }

                }


                //TALEND - UPDATE IMPORT DOVUTI
                try {
                    String codStato = "IMPORT_ESEGUITO";
                    if (gpdEnabled) {
                        codStato = "IMPORT_IN_ELAB";
                    }
                    if (!resultElaboraTo.getStatoElaborazione())
                        codStato = "IMPORT_ABORTITO";
                    if (importDovuto != null) {
                        importDovuto.setDeNomeFileScarti(resultElaboraTo.getDeNomeFileScarti());
                        importDovuto.setCodErrore(resultElaboraTo.getCodErrori());
                        importDovutiService.updateImportFlusso(importDovuto, codStato);
                    } else {
                        codErrore = "PAA_IMPORT_ERROR_UPDATE_IMPORT_DOVUTI)";
                        deErrore = "Impossibile aggiornare il record di import dovuti in quanto è null: " + iuf;
                        resultElaboraTo.setStatoElaborazione(false);
                        manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
                    }
                } catch (Exception e) {
                    codErrore = "PAA_IMPORT_ERROR_UPDATE_IMPORT_DOVUTI)";
                    deErrore = "Impossibile aggiornare il record di import dovuti con iuf: " + iuf;
                    resultElaboraTo.setStatoElaborazione(false);
                    manageLog(e, codErrore, deErrore);
                }

                //TALEND - ARCHIVIA
                archivia(percorsoFile, cartellaToPath.toString(), nomeFile, resultElaboraTo);

                return out.getSequenceValue().longValue();
            }

            if (!resultElaboraTo.getStatoElaborazione()) {
                //TALEND - MAIL UTENTE - INVIA MAIL
                //Se la select ha trovato il record ma c'è un errore o se non è stato restituito niente
                try {
                    importDovutiService.sendMailError(ente, nomeFile, codErrore, deErrore, eMailOperatoreRichiedente);
                } catch (Exception e) {
                    log.error("ImportFlussoTaskService :: validaFlusso :: MAIL UTENTE - INVIA MAIL :: Error sending mail for request token :: {} :: ERROR", valueAUT, e);
                }

                return -1;
            }
            log.debug("ImportFlussoTaskService :: validaFlusso :: End validaFlusso ");
        } catch (Exception e) {
            log.error("ImportFlussoTaskService :: validaFlusso :: ERROR :: {}", e.getMessage());
            manageLog(e, codErrore, deErrore);
            return -2;
        }
        return -3;
    }

    private void archivia(String cartellaFrom, String cartellaTo, String nomeFile, ResultImportFlussoTo resultElaboraTo) {
        try {
            log.debug("ImportFlussoTaskService :: archivia :: Start archivia");
            //TALEND - ARCHIVIA MD5
            Boolean esito = resultElaboraTo.getStatoElaborazione();
            copiaToWork(cartellaFrom, cartellaTo, ".md5", nomeFile, esito ? "delete" : "sposta_rinomina", resultElaboraTo);
            //TALEND - DELETE_CSV_FROM_WORK
            copiaToWork(managePathString + separator + "work", "", ".csv", nomeFile, "delete", resultElaboraTo);
            //TALEND - ARCHIVIA_AUT
            copiaToWork(cartellaFrom, cartellaTo, ".auth", nomeFile, esito ? "delete" : "sposta_rinomina", resultElaboraTo);
            //TALEND - MOVE_ORIGINAL_TO_STORE
            copiaToWork(managePathString + separator + "tmp_import", cartellaTo, ".zip", nomeFile, esito ? "sposta" : "delete", resultElaboraTo);
            //TALEND - ARCHIVIA_ZIP
            copiaToWork(cartellaFrom, cartellaTo, ".zip", nomeFile, esito ? "delete" : "sposta_rinomina", resultElaboraTo);
            log.debug("ImportFlussoTaskService :: archivia :: End archivia");
        } catch (Exception e) {
            log.error("ImportFlussoTaskService :: archivia :: ERROR :: {}", e.getMessage());
            manageLog(e, resultElaboraTo.getCodErrori(), resultElaboraTo.getDeErrori());
        }
    }

    private ResultImportFlussoTo elaboraCsv(Path pathFileCsv, String nomeFile, Ente ente, Utente utente, String iuf, BigInteger idFlusso, String versione) {
        ResultImportFlussoTo result = new ResultImportFlussoTo();
        result.setNomeFile(nomeFile);
        result.setCodErrori("");
        result.setDeErrori("");
        String rigaHeader = "";
        String riga;
        boolean statoElaborazione = true;
        int pdfGenerati = 0;
        int numCaricatiCorretti = 0;
        int numNonCaricati = 0;
        int numRiga = 1;
        try {
            log.debug("ImportFlussoTaskService :: elaboraCsv :: Start elabora versione {}", versione);
            //Imposto il path della cartellaTo come talend
            GregorianCalendar now = new GregorianCalendar();
            String cartellaAnnoMese = now.get(GregorianCalendar.YEAR) + "_" +
                    ((now.get(GregorianCalendar.MONTH) + 1) > 9 ? (now.get(GregorianCalendar.MONTH) + 1)
                            : "0" + (now.get(GregorianCalendar.MONTH) + 1));
            Path dataPath = Paths.get(dataPathString);
            Path cartellaToPath = dataPath.resolve(ente.getCodIpaEnte()).resolve("IMPORT_ELABORATI").resolve(cartellaAnnoMese).resolve(nomeFile);

            //ISTANZIO MAPPA PER LO IUD E GENERO PATH DI FILE ERRORI
            HashMap<String, ImportFlussoTo> mappaCaricati = new HashMap<>();
            HashMap<String, ImportFlussoTo> mappaNonCaricati = new HashMap<>();
            HashMap<String, String> mappaRigaNonCaricati = new HashMap<>();
            List<String> iudList = new ArrayList<>();
            String pathFileErrori = managePathString + separator + "work" + separator + nomeFile + ".log";

            // Mi collego al csv recuperato dallo zip per leggerlo e mi creo il BufferedReader
            // a partire dal InputStreamReader affinchè si possa forzare la lettura del file con la codifica
            // di caratteri UTF-8, come da impostazione Talend della versione MyPay 3, altrimenti si genera
            // l'eccezione java.nio.charset.MalformedInputException.
            // Inoltre mi creo gia il writer del file di errori
            try (
                    FileInputStream fis = new FileInputStream(pathFileCsv.toString());
                    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(isr);
                    FileWriter fileErrori = new FileWriter(pathFileErrori, true)
            ) {
                //HEADER CSV
                rigaHeader = reader.readLine();

                //HEADER FILE ERRORI
                fileErrori.append("num_riga_flusso;cod_rifiuto;de_rifiuto");
                fileErrori.append("\n");

                //LEGGIAMO TUTTE LE RIGHE DEL CSV
                while ((riga = reader.readLine()) != null) {

                    if (StringUtils.isNotBlank(riga)) {

                        //TALEND - CONTROLLA_CONGRUENZA
                        ImportFlussoTo importFlusso = leggiVerificaRiga(riga, versione);
                        importFlusso.setIuf(iuf);
                        importFlusso.setIdFlusso(idFlusso);
                        importFlusso.setNumRigaFlusso("" + numRiga);
                        result.setEmailPagatore(importFlusso.getEmailPagatore());
                        result.setAzione(importFlusso.getAzione());

                        //TALEND - GENERA FILE ERRORI
                        if (StringUtils.isNotBlank(importFlusso.getDeErrore())) {
                            importFlusso.setCodErrore("PAA_IMPORT_SINTASSI_CSV");
                            String rigaErrori = importFlusso.getNumRigaFlusso() + ";" + importFlusso.getCodErrore() + ";" + importFlusso.getDeErrore();
                            fileErrori.append(rigaErrori);
                            statoElaborazione = false;
                        }

                        //Se non ho trovato errori nella lettura della riga proseguo
                        if (statoElaborazione) {
                            //TALEND - IUD_UNICO
                            if (mappaCaricati.containsKey(importFlusso.getIUD())) {
                                importFlusso.setCodErrore("PAA_IMPORT_IUD_DUPLICATO");
                                importFlusso.setDeErrore("Errore IUD duplicato all''interno dello stesso flusso");
                                String erroreIud = importFlusso.getNumRigaFlusso() + ";" + importFlusso.getCodErrore() + ";" + importFlusso.getDeErrore();
                                fileErrori.append(erroreIud);
                                statoElaborazione = false;
                            }

                            //Se non ho trovato altri iud dello stesso valore gia proseguo
                            if (statoElaborazione) {
                                //TALEND - DIVIDI_RIGHE
                                switch (importFlusso.getAzione()) {
                                    case "I":

                                    case "M":

                                    case "A":
                                        processaDovuto(importFlusso, ente, utente, fileErrori, versione);
                                        break;
                                    default:
                                        importFlusso.setCodErrore("PAA_IMPORT_SINTASSI_CSV_AZIONE");
                                        importFlusso.setDeErrore("Errore sintassi CSV campo azione. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'");
                                        String rigaErroreDefault = importFlusso.getNumRigaFlusso() + ";PAA_IMPORT_SINTASSI_CSV_AZIONE;Errore sintassi CSV campo azione. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                                        fileErrori.append(rigaErroreDefault);
                                        break;
                                }
                                if (!StringUtils.isNotBlank(importFlusso.getCodErrore())) {
                                    //Infine inserisco l'import flusso nella mappa per tenere traccia
                                    mappaCaricati.put(importFlusso.getIUD(), importFlusso);
                                    iudList.add(importFlusso.getIUD());
                                    numCaricatiCorretti++;
                                } else {
                                    //E' fallita inserisciDovuto, modificaDovuto o annullaDovuto
                                    mappaNonCaricati.put(importFlusso.getNumRigaFlusso(), importFlusso);
                                    mappaRigaNonCaricati.put(importFlusso.getNumRigaFlusso(), riga);
                                    numNonCaricati++;
                                }
                            } else {
                                //E' fallita perché stato trovato uno IUD duplicato
                                mappaNonCaricati.put(importFlusso.getNumRigaFlusso(), importFlusso);
                                mappaRigaNonCaricati.put(importFlusso.getNumRigaFlusso(), riga);
                                numNonCaricati++;
                            }
                        } else {
                            //E' fallita leggiVerificaRiga
                            mappaNonCaricati.put(importFlusso.getNumRigaFlusso(), importFlusso);
                            mappaRigaNonCaricati.put(importFlusso.getNumRigaFlusso(), riga);
                            numNonCaricati++;
                        }
                        //Aumento il conteggio delle righe, compreso header
                        statoElaborazione = true;
                        numRiga++;

                    }
                }

                //TALEND - FILE_ORI_SCARTI - a prescindere dallo stato di elaborazione
                if (numNonCaricati > 0) {
                    //File degli scarti
                    Path csvScartiPath = cartellaToPath.resolve(nomeFile + "_SCARTI.csv");
                    int rigaAttuale = 1;
                    boolean scartiElaborazione = true;
                    //Ci scrivo sopra
                    try (FileWriter fileScarti = new FileWriter(csvScartiPath.toString(), true)) {
                        //Itero la mappa creata per creare il file di scarti
                        String rigaHeaderScarti = rigaHeader + ";cod_rifiuto;de_rifiuto";
                        fileScarti.append(rigaHeaderScarti);
                        fileScarti.append("\n");
                        for (Map.Entry<String, String> scarto : mappaRigaNonCaricati.entrySet()) {
                            //Creo la riga e l'appendo
                            ImportFlussoTo importFlusso = mappaNonCaricati.get(scarto.getKey());
                            String rigaScarti = creaRigaFileScarti(scarto.getValue(), importFlusso);
                            fileScarti.append(rigaScarti);
                            fileScarti.append("\n");
                            rigaAttuale++;
                        }
                        String dePathFileScarti = separator + "IMPORT_ELABORATI" + separator + cartellaAnnoMese + separator + nomeFile + "_SCARTI.csv";
                        result.setDeNomeFileScarti(dePathFileScarti);
                    } catch (Exception e) {
                        scartiElaborazione = false;
                        result.setCodErrori("PAA_IMPORT_ERROR");
                        result.setDeErrori(result.getDeErrori() + "|Errore durante la generazione del file di scarti per il flusso con IUF: " + iuf);
                        String rigaScarti = rigaAttuale + ";PAA_IMPORT_ERROR;Errore sintassi nella scrittura del file di scarti.";
                        fileErrori.append(rigaScarti);
                        manageLog(e, result.getCodErrori(), result.getDeErrori());
                    }
                    //TALEND - ZIP SCARTI
                    if (scartiElaborazione)
                        copiaToWork(cartellaToPath.toString(), cartellaToPath.toString(), ".csv", nomeFile + "_SCARTI", "zip", result);
                    else
                        //Cancello il csv che si è tentato di creare
                        copiaToWork(cartellaToPath.toString(), "", ".csv", nomeFile + "_SCARTI", "delete", result);
                }
            } catch (Exception e) {
                result.setCodErrori("PAA_IMPORT_ERROR");
                result.setDeErrori(result.getDeErrori() + "|Errore generico durante la lettura del csv per il flusso con IUF: " + iuf);
                manageLog(e, result.getCodErrori(), result.getDeErrori());
            }

            //TALEND - LOAD_CORRETTI
            int numScartati = numRiga - 1 - numCaricatiCorretti;
            result.setNumRigheTotali(numRiga - 1);
            result.setNumRigheImportate(numCaricatiCorretti);
            //TODO IN TALEND FANNO UNA QUERY PER VEDERE SE HA CARICATO
            log.info("ImportFlussoTaskService :: elaboraCsv :: versione " + versione + " :: leggiCsv :: Elaborati per il file: " + nomeFile + " numero dovuti: " + numCaricatiCorretti + " mentre le righe scartate sono " + numScartati);

            //TALEND - GENERA FILE_ORI_IUV
            //File degli iuv generati
            if (result.getNumRigheImportate() != 0) {
                Path oriIuvPath = cartellaToPath.resolve(nomeFile + "_IUV.csv");
                int rigaAttuale = 1;
                boolean iuvElaborazione = true;
                //Ci scrivo sopra
                try (FileWriter fileOriIuv = new FileWriter(oriIuvPath.toString(), true)) {
                    //Itero la mappa creata per creare il file di scarti
                    fileOriIuv.append(rigaHeader);
                    fileOriIuv.append("\n");
                    for (String iud : iudList) {
                        //Creo la riga e l'appendo
                        ImportFlussoTo importFlusso = mappaCaricati.get(iud);
                        if (!StringUtils.isNotBlank(result.getCodErrori())) {
                            String rigaOriIuv = creaRigaFileScartiOriIuv(importFlusso, versione);
                            fileOriIuv.append(rigaOriIuv);
                            fileOriIuv.append("\n");
                            rigaAttuale++;
                        }
                    }
                } catch (Exception e) {
                    iuvElaborazione = false;
                    result.setCodErrori("PAA_IMPORT_ERROR");
                    result.setDeErrori(result.getDeErrori() + "|Errore durante la generazione del file di IUV per il flusso con IUF: " + iuf + " alla riga: " + rigaAttuale);
                    manageLog(e, result.getCodErrori(), result.getDeErrori());
                }

                //TALEND - ZIP IUV
                if (iuvElaborazione)
                    copiaToWork(cartellaToPath.toString(), cartellaToPath.toString(), ".csv", nomeFile + "_IUV", "zip", result);
                else
                    //Cancello il csv che si è tentato di creare
                    copiaToWork(cartellaToPath.toString(), "", ".csv", nomeFile + "_IUV", "delete", result);

                //TALEND - GENERA AVVISI PDF
                if ((SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione)
                        || SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione))
                        && result.getNumRigheImportate() != 0) {
                    rigaAttuale = 1;
                    try {
                        //Itero la mappa creata per riprendere ogni dovuto
                        Path pathAvvisi = cartellaToPath.resolve("AVVISI");
                        Files.createDirectories(pathAvvisi);
                        for (String iud : iudList) {
                            //Creo la riga e l'appendo
                            ImportFlussoTo importFlusso = mappaCaricati.get(iud);
                            if (Boolean.parseBoolean(importFlusso.getFlagGeneraIuv()) &&
                                    !importFlusso.getAzione().equalsIgnoreCase("A") &&
                                    !StringUtils.isNotBlank(importFlusso.getCodErrore())) {
                                //Genero avviso pdf come fa mypay4 e riprendo talend
                                Dovuto dovuto = dovutoService.getByIudEnte(importFlusso.getIUD(), ente.getCodIpaEnte());
                                // Retrieve "importo singolo versamento" for dovuto multibeneficiario
                                BigDecimal importoDovutoMB = dovutoService.getImportoSingoloVerdamentoMBByIdDovuto(dovuto.getMygovDovutoId());
                                // if 'importoDovutoMB' is not null, i add in the amount of importo singolo versamento dovuto
                                if (importoDovutoMB != null) {
                                    dovuto.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento().add(importoDovutoMB));
                                }

                                ByteArrayOutputStream byteArrayOutputStream = jasperService.generateAvviso(dovuto);
                                try (OutputStream outputStream = new FileOutputStream(pathAvvisi.resolve("avviso_" + dovuto.getCodIuv() + ".pdf").toString())) {
                                    byteArrayOutputStream.writeTo(outputStream);
                                }
                                pdfGenerati++;
                            }
                            rigaAttuale++;
                        }
                        //TALEND - ZIP PDF
                        if (pdfGenerati > 0) {
                            boolean statoZipAvvisi = copiaToWork(pathAvvisi.toString(), pathAvvisi.toString(), ".pdf", nomeFile + "_AVVISI_PDF", "zip_all", result);
                            //Se lo zip degli avvisi è andato bene allora lo sposto
                            if (statoZipAvvisi) {
                                boolean statoMoveZipAvvisi = copiaToWork(pathAvvisi.toString(), cartellaToPath.toString(), ".zip", nomeFile + "_AVVISI_PDF", "sposta", result);
                                //Se ho spostato lo zip cancello la cartella avvisi
                                if (statoMoveZipAvvisi)
                                    copiaToWork(pathAvvisi.toString(), "", "", "", "delete", null);
                            }
                        } else {
                            //Cancello tutto dentro la folder avvisi
                            copiaToWork(pathAvvisi.toString(), pathAvvisi.toString(), ".pdf", "", "delete_all", null);
                        }
                    } catch (Exception e) {
                        result.setCodErrori("PAA_IMPORT_ERROR");
                        result.setDeErrori(result.getDeErrori() + "|Errore durante la generazione degli avvisi PDF per il flusso con IUF: " + iuf + " alla riga: " + rigaAttuale);
                        manageLog(e, result.getCodErrori(), result.getDeErrori());
                    }
                }

                //Cancello il file di log creato secondo direzioni talend
                copiaToWork(managePathString + separator + "work", "", ".log", nomeFile, "delete", null);
            }
        } catch (Exception e) {
            log.error("ImportFlussoTaskService :: elaboraCsv :: versione {} :: ERROR :: {}", versione, e.getMessage());
            result.setCodErrori("PAA_IMPORT_ERROR");
            result.setDeErrori(result.getDeErrori() + "|Errore generico durante l'elaborazione del csv per IUF: " + iuf);
            manageLog(e, result.getCodErrori(), result.getDeErrori());
        }
        result.setNumAvvisiGenerati(pdfGenerati);
        log.debug("ImportFlussoTaskService :: elaboraCsv :: versione {} ::END", versione);
        return result;
    }

    private String creaRigaFileScartiOriIuv(ImportFlussoTo importFlusso, String versione) {
        log.debug("ImportFlussoTaskService :: creaRigaFileScartiOriIuv :: Start creaRigaFileScartiOriIuv");
        String riga = "";
        try {
            riga = riga + importFlusso.getIUD() + ";";
            riga = riga + (StringUtils.isNotBlank(importFlusso.getCodIuv()) ? importFlusso.getCodIuv() : "") + ";";
            riga = riga + importFlusso.getTipoIdentificativoUnivoco() + ";";
            riga = riga + importFlusso.getCodiceIdentificativoUnivoco() + ";";
            riga = riga + importFlusso.getAnagraficaPagatore() + ";";
            riga = riga + (StringUtils.isNotBlank(importFlusso.getIndirizzoPagatore()) ? importFlusso.getIndirizzoPagatore() : "") + ";";
            riga = riga + (StringUtils.isNotBlank(importFlusso.getCivicoPagatore()) ? importFlusso.getCivicoPagatore() : "") + ";";
            riga = riga + (StringUtils.isNotBlank(importFlusso.getCapPagatore()) ? importFlusso.getCapPagatore() : "") + ";";
            riga = riga + (StringUtils.isNotBlank(importFlusso.getLocalitaPagatore()) ? importFlusso.getLocalitaPagatore() : "") + ";";
            riga = riga + (StringUtils.isNotBlank(importFlusso.getProvinciaPagatore()) ? importFlusso.getProvinciaPagatore() : "") + ";";
            riga = riga + (StringUtils.isNotBlank(importFlusso.getNazionePagatore()) ? importFlusso.getNazionePagatore() : "") + ";";
            riga = riga + (StringUtils.isNotBlank(importFlusso.getEmailPagatore()) ? importFlusso.getEmailPagatore() : "") + ";";
            riga = riga + (importFlusso.getDataScadenzaPagamento() != null ? new SimpleDateFormat("yyyy-MM-dd").format(importFlusso.getDataScadenzaPagamento()) : "") + ";";
            riga = riga + (new DecimalFormat("0.00").format(importFlusso.getImportoDovuto())) + ";";
            riga = riga + (importFlusso.getCommissioneCaricoPa() != null ? new DecimalFormat("0.00").format(importFlusso.getCommissioneCaricoPa()) : "") + ";";
            riga = riga + importFlusso.getTipoDovuto() + ";";
            riga = riga + importFlusso.getTipoVersamento() + ";";
            riga = riga + importFlusso.getCausaleVersamento() + ";";
            riga = riga + importFlusso.getDatiSpecificiRiscossione() + ";";
            if (SupportedFileVersion.VERSIONE_1_2.getVersione_file().equalsIgnoreCase(versione)
                    || SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione)
                    || SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) {
                riga = riga + importFlusso.getBilancio() + ";";
                if (SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione)
                        || SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) {
                    riga = riga + importFlusso.getFlagGeneraIuv() + ";";
                    if (SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione) &&
                            Boolean.parseBoolean(importFlusso.getFlagMultiBeneficiario())) {
                        riga = riga + importFlusso.getFlagMultiBeneficiario() + ";";
                        riga = riga + importFlusso.getCodiceFiscaleEnteSecondario() + ";";
                        riga = riga + importFlusso.getDenominazioneEnteSecondario() + ";";
                        riga = riga + importFlusso.getIbanAccreditoEnteSecondario() + ";";
                        riga = riga + (StringUtils.isNotBlank(importFlusso.getIndirizzoEnteSecondario()) ? importFlusso.getIndirizzoEnteSecondario() : "") + ";";
                        riga = riga + (StringUtils.isNotBlank(importFlusso.getCivicoEnteSecondario()) ? importFlusso.getCivicoEnteSecondario() : "") + ";";
                        riga = riga + (StringUtils.isNotBlank(importFlusso.getCapEnteSecondario()) ? importFlusso.getCapEnteSecondario() : "") + ";";
                        riga = riga + (StringUtils.isNotBlank(importFlusso.getLocalitaEnteSecondario()) ? importFlusso.getLocalitaEnteSecondario() : "") + ";";
                        riga = riga + (StringUtils.isNotBlank(importFlusso.getProvinciaEnteSecondario()) ? importFlusso.getProvinciaEnteSecondario() : "") + ";";
                        riga = riga + (StringUtils.isNotBlank(importFlusso.getNazioneEnteSecondario()) ? importFlusso.getNazioneEnteSecondario() : "") + ";";
                        riga = riga + (StringUtils.isNotBlank(importFlusso.getDatiSpecificiRiscossioneEnteSecondario()) ? importFlusso.getDatiSpecificiRiscossioneEnteSecondario() : "") + ";";
                        riga = riga + (StringUtils.isNotBlank(importFlusso.getCausaleVersamentoEnteSecondario()) ? importFlusso.getCausaleVersamentoEnteSecondario() : "") + ";";
                        riga = riga + (new DecimalFormat("0.00").format(importFlusso.getImportoVersamentoEnteSecondario())) + ";";
                    }
                }
            }
            riga = riga + importFlusso.getAzione();
            if (StringUtils.isNotBlank(importFlusso.getCodErrore())) {
                riga = riga + ";" + importFlusso.getCodErrore() + ";";
                riga = riga + importFlusso.getDeErrore();
            }
        } catch (Exception e) {
            log.error("ImportFlussoTaskService :: creaRigaFileScartiOriIuv :: ERROR ", e);
            throw e;
        }
        log.debug("ImportFlussoTaskService :: creaRigaFileScartiOriIuv :: End creaRigaFileScartiOriIuv");
        return riga;
    }

    private String creaRigaFileScarti(String rigaOri, ImportFlussoTo importFlusso) {
        log.debug("ImportFlussoTaskService :: creaRigaFileScarti :: Start creaRigaFileScarti");
        String riga = rigaOri;
        try {
            if (StringUtils.isNotBlank(importFlusso.getCodErrore())) {
                riga = riga + ";" + importFlusso.getCodErrore() + ";";
                riga = riga + importFlusso.getDeErrore();
            }
        } catch (Exception e) {
            log.error("ImportFlussoTaskService :: creaRigaFileScarti :: ERROR ", e);
            throw e;
        }
        log.debug("ImportFlussoTaskService :: creaRigaFileScarti :: End creaRigaFileScarti");
        return riga;
    }

    private void processaDovuto(ImportFlussoTo importFlusso, Ente ente, Utente utente, FileWriter fileErrori, String versione) {
        log.debug("ImportFlussoTaskService :: processaDovuto :: Start processaDovuto");
        try {
            //TALEND - VALIDA
            boolean valid = validaElabora(importFlusso, ente, fileErrori, versione);
            if (valid)
                dovutoService.importDovutoImportFlusso(importFlusso, ente, utente, fileErrori, versione);
        } catch (Exception e) {
            log.error("ImportFlussoTaskService :: processaDovuto :: ERROR", e);
            manageLog(e, importFlusso.getCodErrore(), importFlusso.getDeErrore());
        }
        log.debug("ImportFlussoTaskService :: processaDovuto :: End processaDovuto");
        }


    private boolean validaElabora(ImportFlussoTo importFlusso, Ente ente, FileWriter fileErrori, String versione) {
        boolean validazioneFlusso = true;
        String codErrori = "";
        String deErrori = "";
        try {
            log.debug("ImportFlussoTaskService :: validaElabora :: Start validaElabora");

            // IUD
            try {
                String IUD = importFlusso.getIUD();
                if (!Utilities.validaIUD(IUD)) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IUD";
                    deErrori = deErrori + "|Errore sintassi CSV campo IUD. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IUD";
                deErrori = deErrori + "|Errore sintassi CSV campo IUD. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // IUV
            try {
                if (!Utilities.validaIUV(importFlusso.getCodIuv(), Boolean.parseBoolean(importFlusso.getFlagGeneraIuv()), ente.getApplicationCode(), StringUtils.equals(importFlusso.getAzione(),"I"))) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IUV";
                    deErrori = deErrori + "|Errore sintassi CSV campo codIuv. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IUV";
                deErrori = deErrori + "|Errore sintassi CSV campo codIuv. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // TIPO DOVUTO
            Optional<EnteTipoDovuto> enteTipoDovutoOptional = Optional.empty();
            try {
                String codTipo = importFlusso.getTipoDovuto();
                enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(codTipo, ente.getCodIpaEnte(), true);
                if (enteTipoDovutoOptional.isEmpty()) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_TIPO_DOVUTO_INESISTENTE";
                    deErrori = deErrori + "|Errore sintassi CSV campo tipoDovuto non presente in archivio";
                    validazioneFlusso = false;
                } else if (!enteTipoDovutoOptional.get().isFlgAttivo()) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_TIPO_DOVUTO_NON_ATTIVO_PER_ENTE";
                    deErrori = deErrori + "|Errore sintassi CSV campo tipoDovuto non attivo per ente";
                    validazioneFlusso = false;
                } else if (codTipo.equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
                    //marca bollo digitale not supported on import flusso
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_TIPO_DOVUTO_NON_VALIDO_MARCA_DA_BOLLO";
                    deErrori = deErrori + "|Errore sintassi CSV campo tipoDovuto, marca da bollo non supportato per import";
                    validazioneFlusso = false;
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_TIPO_DOVUTO";
                deErrori = deErrori + "|Errore sintassi CSV campo tipoDovuto. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            //MODIFICA LOGICA PER CODICE UTILIZZO FISCALE ANONIMO
            try {
                String tipoIdentificativoUnivoco = importFlusso.getTipoIdentificativoUnivoco();
                boolean isFlagCfAnonimo = enteTipoDovutoOptional.map(EnteTipoDovuto::isFlgCfAnonimo).orElse(false);
                log.debug("flag CF{} -> {}", Constants.CODICE_FISCALE_ANONIMO, isFlagCfAnonimo);
                if (!Utilities.isValidCodIdUnivocoConAnonimo(isFlagCfAnonimo, tipoIdentificativoUnivoco, importFlusso.getCodiceIdentificativoUnivoco())) {
                    if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO_F)) {
                        codErrori = codErrori + "PAA_IMPORT_SINTASSI_CSV_COD_FISC";
                        deErrori = deErrori + "|Errore sintassi CSV Codice fiscale non valido. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    } else if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO_G)) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_P_IVA";
                        deErrori = deErrori + "|Errore sintassi CSV campo P.IVA non valida. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    } else {

                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_ID_UNIV_FG";
                        deErrori = deErrori + "|Errore sintassi CSV campo tipoIdentificativoUnivoco. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_COD_FISC_P_IVA";
                deErrori = deErrori + "|Errore sintassi CSV campo Codice fiscale - P.IVA non valida. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // ANAGRAFICA PAGATORE
            // Nessun ulteriore controllo previsto. Lunghezza già verificata.

            // INDIRIZZO PAGATORE
            try {
                String indirizzoPagatore = importFlusso.getIndirizzoPagatore();
                if (StringUtils.isNotBlank(indirizzoPagatore)) {
                    if (!Utilities.validaIndirizzoAnagrafica(importFlusso.getIndirizzoPagatore(), false)) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_INDIRIZZO";
                        deErrori = deErrori + "|Errore sintassi CSV campo indirizzoPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_INDIRIZZO";
                deErrori = deErrori + "|Errore sintassi CSV campo indirizzoPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // CIVICO PAGATORE
            try {
                String civicoPagatore = importFlusso.getCivicoPagatore();
                if (StringUtils.isNotBlank(civicoPagatore)) {
                    if (!Utilities.validaCivicoAnagrafica(importFlusso.getCivicoPagatore(), false)) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_CIVICO";
                        deErrori = deErrori + "|Errore sintassi CSV campo civicoPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_CIVICO";
                deErrori = deErrori + "|Errore sintassi CSV campo civicoPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // NAZIONE PAGATORE
            try {
                String nazionePagatore = importFlusso.getNazionePagatore();
                if (StringUtils.isNotBlank(nazionePagatore)) {
                    NazioneTo nazione = locationService.getNazioneByCodIso(nazionePagatore);
                    if (nazione == null) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_NAZ";
                        deErrori = deErrori + "|Errore sintassi CSV campo nazionePagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_NAZ";
                deErrori = deErrori + "|Errore sintassi CSV campo nazionePagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // PROVINCIA PAGATORE
            try {
                String siglaProvincia = importFlusso.getProvinciaPagatore();
                if (StringUtils.isNotBlank(siglaProvincia)
                        && StringUtils.isNotBlank(importFlusso.getNazionePagatore())
                        && Constants.CODICE_NAZIONE_ITALIA.equalsIgnoreCase(importFlusso.getNazionePagatore())) {
                    ProvinciaTo provincia = locationService.getProvinciaBySigla(siglaProvincia);
                    if (provincia == null) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_PROV";
                        deErrori = deErrori + "|Errore sintassi CSV campo provinciaPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_PROV";
                deErrori = deErrori + "|Errore sintassi CSV campo provinciaPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // CAP PAGATORE
            try {
                if (StringUtils.isNotBlank(importFlusso.getCapPagatore())) {
                    if (!Utilities.isValidCAP(importFlusso.getCapPagatore())) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_CAP";
                        deErrori = deErrori + "|Errore sintassi CSV campo capPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_NAZ_PROV_CAP";
                deErrori = deErrori + "|Errore sintassi CSV campo capPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // LOCALITA PAGATORE
            // Nessun ulteriore controllo previsto. Lunghezza già verificata.

            // E-MAIL PAGATORE
            try {
                if (StringUtils.isNotBlank(importFlusso.getEmailPagatore())) {
                    if (!Utilities.isValidEmail(importFlusso.getEmailPagatore())) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_EMAIL";
                        deErrori = deErrori + "|Errore sintassi CSV campo emailPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_EMAIL";
                deErrori = deErrori + "|Errore sintassi CSV campo emailPagatore. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }


            // DATA_SCADENZA_PAGAMENTO
            try {
                if (SupportedFileVersion.VERSIONE_1_2.getVersione_file().equalsIgnoreCase(versione) ||
                        SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione) ||
                        SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) {
                    if (enteTipoDovutoOptional.isPresent()) {
                        if (importFlusso.getDataScadenzaPagamento() == null
                                && enteTipoDovutoOptional.get().isFlgScadenzaObbligatoria()) {
                            codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_DATA_SCADENZA_PAGAMENTO_NON_PRESENTE";
                            deErrori = deErrori + "|Errore sintassi CSV campo dataScadenzaPagamento, non presente. Deve essere valorizzata per questo tipo dovuto";
                            validazioneFlusso = false;
                        }
                    }
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_DATA_SCADENZA_PAGAMENTO";
                deErrori = deErrori + "|Errore sintassi CSV campo dataScadenzaPagamento. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // IMPORTO SINGOLO VERSAMENTO
            try {
                BigDecimal importoSingoloVersamento = BigDecimal.valueOf(importFlusso.getImportoDovuto());
                if (importoSingoloVersamento.compareTo(BigDecimal.ZERO) == 0) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IMPORTO";
                    deErrori = deErrori + " Errore sintassi CSV campo importoDovuto. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                }
                if (((StringUtils.isNotBlank(importFlusso.getCodIuv()) &&
                        (importFlusso.getCodIuv().length() == 15 || importFlusso.getCodIuv().length() == 17)) ||
                        (Boolean.parseBoolean(importFlusso.getFlagGeneraIuv()))) &&
                        importoSingoloVersamento.compareTo(Constants.MAX_AMOUNT) > 0
                ) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IMPORTO";
                    deErrori = deErrori + " Errore sintassi CSV campo importoDovuto. ImportoSingoloVersamento non valido. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                } else if (importoSingoloVersamento.compareTo(Constants.MAX_AMOUNT) > 0) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IMPORTO";
                    deErrori = deErrori + " Errore sintassi CSV campo importoDovuto. ImportoSingoloVersamento non valido. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IMPORTO";
                deErrori = deErrori + "|Errore sintassi CSV campo importoDovuto. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // COMMISSIONE CARICO PA
            try {
                BigDecimal commissioneCaricoPA = Optional.ofNullable(importFlusso.getCommissioneCaricoPa()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
                if (commissioneCaricoPA.compareTo(Constants.MAX_AMOUNT) > 0) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_COMMISSIONI_PA";
                    deErrori = deErrori + " Errore sintassi CSV campo commissioneCaricoPa. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_COMMISSIONI_PA";
                deErrori = deErrori + "|Errore sintassi CSV campo commissioneCaricoPa. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // TIPO VERSAMENTO
            try {
                String tipoVersamento = importFlusso.getTipoVersamento();
                if (!Utilities.validaTipoVersamento(importFlusso.getTipoVersamento())) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_TIPO_VERS";
                    deErrori = deErrori + "|Errore sintassi CSV campo tipoVersamento. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                }
                if (!StringUtils.isNotBlank(tipoVersamento)) {
                    tipoVersamento = Constants.ALL_PAGAMENTI;
                    importFlusso.setTipoVersamento(tipoVersamento);
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_TIPO_VERS";
                deErrori = deErrori + "|Errore sintassi CSV campo tipoVersamento. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // CAUSALE VERSAMENTO
            // Nessun ulteriore controllo previsto. Lunghezza già verificata.

            // DATI SPECIFICI RISCOSSIONE
            try {
                String datiSpecificiRiscossione = importFlusso.getDatiSpecificiRiscossione();
                if (!Utilities.validaDatiSpecificiRiscossione(datiSpecificiRiscossione)) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_DATI_RISCOSS";
                    deErrori = deErrori + "|Errore sintassi CSV campo datiSpecificiRiscossione. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                }
            } catch (Exception e) {
                codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_DATI_RISCOSS";
                deErrori = deErrori + "|Errore sintassi CSV campo datiSpecificiRiscossione. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                validazioneFlusso = false;
                manageLog(e, codErrori, deErrori);
            }

            // BILANCIO
            if ((SupportedFileVersion.VERSIONE_1_2.getVersione_file().equalsIgnoreCase(versione) ||
                    SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione) ||
                    SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) &&
                    importFlusso.getBilancio() != null
            ) {
                try {
                    //Prepare JAXB objects
                    JAXBContext jc = JAXBContext.newInstance(Bilancio.class);
                    Unmarshaller u = jc.createUnmarshaller();

                    //Create an XMLReader to use with our filter
                    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
                    SAXParser parser = parserFactory.newSAXParser();
                    XMLReader reader = parser.getXMLReader();

                    //Create the filter (to add namespace) and set the xmlReader as its parent.
                    NameSpaceFilterBilancio inFilter = new NameSpaceFilterBilancio("http://www.regione.veneto.it/schemas/2012/Pagamenti/Ente/", true);
                    inFilter.setParent(reader);

                    //Prepare the input, in this case a java.io.File (output)
                    StringReader readerString = new StringReader(importFlusso.getBilancio());
                    InputSource is = new InputSource(readerString);

                    //Create a SAXSource specifying the filter
                    SAXSource source = new SAXSource(inFilter, is);

                    //Do unmarshalling
                    Bilancio bilancio = (Bilancio) u.unmarshal(source);
                    if (bilancio != null) {
                        if (!Utilities.verificaImportoBilancio(bilancio, BigDecimal.valueOf(importFlusso.getImportoDovuto()))) {
                            codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_BILANCIO";
                            deErrori = deErrori + "|Errore sintassi CSV campo bilancio. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                            log.error("ImportFlussoTaskService :: validaElabora :: ERROR :: BILANCIO :: {}", deErrori);
                            validazioneFlusso = false;
                        }
                        log.debug("ImportFlussoTaskService :: validaElabora :: BILANCIO CORRECTLY UNMARSHALLED:: {}", importFlusso.getBilancio());
                    }
                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_BILANCIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo bilancio. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }
            }

            // IUV_MULTI_7 MULTI BENEFICIARIO
            if (SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione) &&
                    Boolean.parseBoolean(importFlusso.getFlagMultiBeneficiario())) {
                // DENOMINAZIONE ENTE SECONDARIO
                // Nessun ulteriore controllo previsto. Lunghezza già verificata.

                // CODICE FISCALE ENTE SECONDARIO
                try {
                    if (!Utilities.isValidCFOrPIVA(importFlusso.getCodiceFiscaleEnteSecondario())) {
                        codErrori = codErrori + "PAA_IMPORT_SINTASSI_CSV_COD_FISC_ENTE_SECONDARIO";
                        deErrori = deErrori + "|Errore sintassi CSV campo codiceFiscaleEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_COD_FISC_ENTE_SECONDARIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo codiceFiscaleEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }

                // IBAN ACCREDITO ENTE SECONDARIO
                try {
                    if (!Utilities.isValidIban(importFlusso.getIbanAccreditoEnteSecondario())) {
                        codErrori = codErrori + "PAA_IMPORT_SINTASSI_CSV_IBAN_ACCREDITO_ENTE_SECONDARIO";
                        deErrori = deErrori + "|Errore sintassi CSV campo ibanAccreditoEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IBAN_ACCREDITO_ENTE_SECONDARIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo ibanAccreditoEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }

                // INDIRIZZO ENTE SECONDARIO
                try {
                    String indirizzoEnteSecondario = importFlusso.getIndirizzoEnteSecondario();
                    if (StringUtils.isNotBlank(indirizzoEnteSecondario)) {
                        if (!Utilities.validaIndirizzoAnagrafica(importFlusso.getIndirizzoEnteSecondario(), false)) {
                            codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_INDIRIZZO_ENTE_SECONDARIO";
                            deErrori = deErrori + "|Errore sintassi CSV campo indirizzoEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                            validazioneFlusso = false;
                        }
                    }
                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_INDIRIZZO_ENTE_SECONDARIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo indirizzoEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }

                // CIVICO ENTE SECONDARIO
                try {
                    String civicoEnteSecondario = importFlusso.getCivicoEnteSecondario();
                    if (StringUtils.isNotBlank(civicoEnteSecondario)) {
                        if (!Utilities.validaCivicoAnagrafica(importFlusso.getCivicoEnteSecondario(), false)) {
                            codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_CIVICO_ENTE_SECONDARIO";
                            deErrori = deErrori + "|Errore sintassi CSV campo civicoEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                            validazioneFlusso = false;
                        }
                    }
                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_CIVICO_ENTE_SECONDARIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo civicoEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }

                // NAZIONE PAGATORE ENTE SECONDARIO
                try {
                    String nazioneEnteSecondario = importFlusso.getNazioneEnteSecondario();
                    if (StringUtils.isNotBlank(nazioneEnteSecondario)) {
                        NazioneTo nazione = locationService.getNazioneByCodIso(nazioneEnteSecondario);
                        if (nazione == null) {
                            codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_NAZ_ENTE_SECONDARIO";
                            deErrori = deErrori + "|Errore sintassi CSV campo nazioneEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                            validazioneFlusso = false;
                        }
                    }
                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_NAZ_ENTE_SECONDARIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo nazioneEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }

                // PROVINCIA ENTE SECONDARIO
                try {
                    String siglaProvincia = importFlusso.getProvinciaEnteSecondario();
                    if (StringUtils.isNotBlank(siglaProvincia)
                            && StringUtils.isNotBlank(importFlusso.getNazioneEnteSecondario())
                            && Constants.CODICE_NAZIONE_ITALIA.equals(importFlusso.getNazioneEnteSecondario())) {
                        ProvinciaTo provincia = locationService.getProvinciaBySigla(siglaProvincia);
                        if (provincia == null) {
                            codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_PROV_ENTE_SECONDARIO";
                            deErrori = deErrori + "|Errore sintassi CSV campo provinciaEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                            validazioneFlusso = false;
                        }
                    }
                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_PROV_ENTE_SECONDARIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo provinciaEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }

                // CAP ENTE SECONDARIO
                try {
                    if (StringUtils.isNotBlank(importFlusso.getCapEnteSecondario())) {
                        if (!Utilities.isValidCAP(importFlusso.getCapEnteSecondario())) {
                            codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_CAP_ENTE_SECONDARIO";
                            deErrori = deErrori + "|Errore sintassi CSV campo capEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                            validazioneFlusso = false;
                        }
                    }
                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_CAP_ENTE_SECONDARIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo capEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }

                // DATI SPECIFICI RISCOSSIONE ENTE SECONDARIO
                try {
                    String datiSpecificiRiscossioneEnteSecondario = importFlusso.getDatiSpecificiRiscossioneEnteSecondario();
                    boolean validazioneDatiSpecRiscEnteSec = StringUtils.isNotBlank(datiSpecificiRiscossioneEnteSecondario)
                            && Utilities.validaDatiSpecificiRiscossioneEnteSecondario(datiSpecificiRiscossioneEnteSecondario);
                    if (validazioneDatiSpecRiscEnteSec) {
                        String codiceTassonomicoEnteSec = StringUtils.substringBeforeLast(datiSpecificiRiscossioneEnteSecondario, "/") + "/";
                        validazioneDatiSpecRiscEnteSec = tassonomiaService.ifExitsCodiceTassonomico(codiceTassonomicoEnteSec);
                    }

                    if (!validazioneDatiSpecRiscEnteSec) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_DATI_RISCOSS_ENTE_SECONDARIO";
                        deErrori = deErrori + "|Errore sintassi CSV campo datiSpecificiRiscossioneEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }

                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_DATI_RISCOSS_ENTE_SECONDARIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo datiSpecificiRiscossioneEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }

                // CAUSALE VERSAMENTO ENTE SECONDARIO
                // Nessun ulteriore controllo previsto. Lunghezza già verificata.

                // IMPORTO VERSAMENTO ENTE SECONDARIO
                try {
                    BigDecimal importoVersamentoEnteSecondario = BigDecimal.valueOf(importFlusso.getImportoVersamentoEnteSecondario());
                    if (importoVersamentoEnteSecondario.compareTo(BigDecimal.ZERO) == 0) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IMPORTO_VERS_ENTE_SECONDARIO";
                        deErrori = deErrori + " Errore sintassi CSV campo importoVersamentoEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                    if (importoVersamentoEnteSecondario.compareTo(Constants.MAX_AMOUNT) > 0) {
                        codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IMPORTO_VERS_ENTE_SECONDARIO";
                        deErrori = deErrori + " Errore sintassi CSV campo importoVersamentoEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                        validazioneFlusso = false;
                    }
                } catch (Exception e) {
                    codErrori = codErrori + "|PAA_IMPORT_SINTASSI_CSV_IMPORTO_VERS_ENTE_SECONDARIO";
                    deErrori = deErrori + "|Errore sintassi CSV campo importoVersamentoEnteSecondario. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'";
                    validazioneFlusso = false;
                    manageLog(e, codErrori, deErrori);
                }
            }

            // FLAG GENERA IUV
            if (SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione) ||
                    SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) {
                if (importFlusso.getAzione().equalsIgnoreCase("I")) {
                    if (validazioneFlusso && !StringUtils.isNotBlank(importFlusso.getCodIuv()) &&
                            "true".equalsIgnoreCase(importFlusso.getFlagGeneraIuv())) {
                        NodoSILChiediIUVRisposta nodoSILChiediIUVRisposta;
                        try {
                            NodoSILChiediIUV nodoSILChiediIUV = new NodoSILChiediIUV();
                            nodoSILChiediIUV.setIdentificativoDominio(ente.getCodiceFiscaleEnte());
                            nodoSILChiediIUV.setTipoGeneratore(Constants.IUV_GENERATOR_17);
                            nodoSILChiediIUV.setTipoVersamento(Constants.PAY_PRESSO_PSP);
                            nodoSILChiediIUV.setImporto(importFlusso.getImportoDovuto().toString());
                            nodoSILChiediIUV.setAuxDigit(Constants.SMALL_IUV_AUX_DIGIT);
                            nodoSILChiediIUVRisposta = pagamentiTelematiciRPClient.nodoSILChiediIUV(nodoSILChiediIUV);

                            if (nodoSILChiediIUVRisposta.getFault() != null && nodoSILChiediIUVRisposta.getFault().getFaultCode() != null) {
                                validazioneFlusso = false;
                                codErrori = codErrori + "|PAA_IMPORT_ERROR";
                                deErrori = deErrori + "|CODICE " + nodoSILChiediIUVRisposta.getFault().getFaultCode() + " DESCRIZIONE " + nodoSILChiediIUVRisposta.getFault().getDescription();
                            } else {
                                importFlusso.setCodIuv(nodoSILChiediIUVRisposta.getIdentificativoUnivocoVersamento());
                            }
                        } catch (Exception e) {
                            validazioneFlusso = false;
                            codErrori = codErrori + "|PAA_IMPORT_ERROR";
                            deErrori = deErrori + "|CODICE " + e.getMessage();
                            manageLog(e, codErrori, deErrori);
                        }
                    }
                }
            }
        } catch (Exception e) {
            codErrori = "PAA_IMPORT_ERROR";
            deErrori = deErrori + "|Errore generico nella validazione della posizione debitoria con IUD: " + importFlusso.getIUD();
            validazioneFlusso = false;
            manageLog(e, codErrori, deErrori);
        }

        //Scrivo nel file ERRORI di log
        try {
            if (!validazioneFlusso) {
                importFlusso.setCodErrore(codErrori);
                importFlusso.setDeErrore(deErrori);
                String rigaErrore = importFlusso.getNumRigaFlusso() + ";" + codErrori + ";" + deErrori;
                fileErrori.append(rigaErrore);
            }
        } catch (Exception e) {
            codErrori = codErrori + "|PAA_IMPORT_ERROR";
            deErrori = deErrori + "|Errore durante la scrittura nel file degli errori per la posizione debitoria con IUD: " + importFlusso.getIUD();
            manageLog(e, codErrori, deErrori);
        }
        return validazioneFlusso;
    }

    private ImportFlussoTo leggiVerificaRiga(String riga, String versione) {
        ImportFlussoTo importFlusso = new ImportFlussoTo();
        try {
            log.debug("ImportFlussoTaskService :: leggiVerificaRiga :: Start leggiVerificaRiga");
            // Recupero tutti i campi del csv tenendo conto del loro formato e lunghezza e nullability
            String[] splitted = riga.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (riga.endsWith(";")) {
                String[] newSplitted = new String[splitted.length + 1];
                System.arraycopy(splitted, 0, newSplitted, 0, splitted.length);
                newSplitted[splitted.length] = "";
                splitted = newSplitted;
            }
            var data = Arrays.stream(splitted)
                    .map(ImportFlussoTaskService::cleanDoubleQuote)
                    .toArray(String[]::new);
            importFlusso.setDeErrore("");

            // Controllo sul numero massimo di campi previsti per la riga
            int maxDataLength = 0;
            if ((SupportedFileVersion.VERSIONE_1_0.getVersione_file().equalsIgnoreCase(versione)
                    || SupportedFileVersion.VERSIONE_1_1.getVersione_file().equalsIgnoreCase(versione))) {
                maxDataLength = 20;
            } else if (SupportedFileVersion.VERSIONE_1_2.getVersione_file().equalsIgnoreCase(versione)) {
                maxDataLength = 21;
            } else if (SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione)) {
                maxDataLength = 22;
            } else if (SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) {
                maxDataLength = 35;
            }

            if (data.length > maxDataLength) {
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "Too many columns");
            }

            // COD_IUD
            if (StringUtils.isNotBlank(data[0])) {
                if (data[0].length() > 35) {
                    //IUD TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "COD_IUD TROPPO GRANDE");
                }
                importFlusso.setIUD(data[0]);
            } else {
                //IUD NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "IUD NON PRESENTE");
            }

            // COD_IUV
            if (StringUtils.isNotBlank(data[1])) {
                if (data[1].length() > 35) {
                    //IUV TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "COD_IUV TROPPO GRANDE");
                }
                importFlusso.setCodIuv(data[1]);
            }

            // TIPO_IDENTIFICATIVO_UNIVOCO
            if (StringUtils.isNotBlank(data[2])) {
                if (data[2].length() != 1) {
                    //TIPO_IDENTIFICATIVO_UNIVOCO TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "TIPO_IDENTIFICATIVO_UNIVOCO TROPPO GRANDE");
                }
                importFlusso.setTipoIdentificativoUnivoco(data[2].toUpperCase());
            } else {
                //TIPO_IDENTIFICATIVO_UNIVOCO NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "TIPO_IDENTIFICATIVO_UNIVOCO NON PRESENTE");
            }

            // CODICE_IDENTIFICATIVO_UNIVOCO
            if (StringUtils.isNotBlank(data[3])) {
                if (data[3].length() > 35) {
                    //CODICE_IDENTIFICATIVO_UNIVOCO TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CODICE_IDENTIFICATIVO_UNIVOCO TROPPO GRANDE");
                }
                importFlusso.setCodiceIdentificativoUnivoco(data[3].toUpperCase());
            } else {
                //CODICE_IDENTIFICATIVO_UNIVOCO NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CODICE_IDENTIFICATIVO_UNIVOCO NON PRESENTE");
            }

            // ANAGRAFICA_PAGATORE
            if (StringUtils.isNotBlank(data[4])) {
                if (data[4].length() > 70) {
                    //ANAGRAFICA_PAGATORE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "ANAGRAFICA_PAGATORE TROPPO GRANDE");
                }
                importFlusso.setAnagraficaPagatore(data[4]);
            } else {
                //ANAGRAFICA_PAGATORE NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "ANAGRAFICA_PAGATORE NON PRESENTE");
            }

            // INDIRIZZO_PAGATORE
            if (StringUtils.isNotBlank(data[5])) {
                if (data[5].length() > 70) {
                    //INDIRIZZO_PAGATORE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "INDIRIZZO_PAGATORE TROPPO GRANDE");
                }
                importFlusso.setIndirizzoPagatore(data[5]);
            }

            // CIVICO_PAGATORE
            if (StringUtils.isNotBlank(data[6])) {
                if (data[6].length() > 16) {
                    //CIVICO_PAGATORE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CIVICO_PAGATORE TROPPO GRANDE");
                }
                importFlusso.setCivicoPagatore(data[6]);
            }

            // CAP_PAGATORE
            if (StringUtils.isNotBlank(data[7])) {
                if (data[7].length() > 16) {
                    //CAP_PAGATORE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CAP_PAGATORE TROPPO GRANDE");
                }
                importFlusso.setCapPagatore(data[7]);
            }

            // LOCALITA_PAGATORE
            if (StringUtils.isNotBlank(data[8])) {
                if (data[8].length() > 35) {
                    //LOCALITA_PAGATORE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "LOCALITA_PAGATORE TROPPO GRANDE");
                }
                importFlusso.setLocalitaPagatore(data[8]);
            }

            // PROVINCIA_PAGATORE
            if (StringUtils.isNotBlank(data[9])) {
                if (data[9].length() > 2) {
                    //PROVINCIA_PAGATORE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "PROVINCIA_PAGATORE TROPPO GRANDE");
                }
                importFlusso.setProvinciaPagatore(data[9]);
            }

            // NAZIONE_PAGATORE
            if (StringUtils.isNotBlank(data[10])) {
                if (data[10].length() > 2) {
                    //NAZIONE_PAGATORE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "NAZIONE_PAGATORE TROPPO GRANDE");
                }
                importFlusso.setNazionePagatore(data[10]);
            }

            // EMAIL_PAGATORE
            if (StringUtils.isNotBlank(data[11])) {
                if (data[11].length() > 256) {
                    //EMAIL_PAGATORE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "EMAIL_PAGATORE TROPPO GRANDE");
                }
                importFlusso.setEmailPagatore(data[11]);
            }

            // DATA_SCADENZA_PAGAMENTO
            if (StringUtils.isNotBlank(data[12])) {
                if (data[12].length() == 10) {
                    try {
                        importFlusso.setDataScadenzaPagamento(new SimpleDateFormat("yyyy-MM-dd").parse(data[12]));
                    } catch (Exception e) {
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "dataScadenzaPagamento:wrong DATE pattern or wrong DATE data");
                    }
                } else {
                    //DATA_SCADENZA_PAGAMENTO NON CONFORME A yyyy-MM-dd
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "dataScadenzaPagamento:wrong DATE pattern or wrong DATE data");
                }
            } else if (SupportedFileVersion.VERSIONE_1_0.getVersione_file().equalsIgnoreCase(versione) ||
                    SupportedFileVersion.VERSIONE_1_1.getVersione_file().equalsIgnoreCase(versione)) {
                //DATA_SCADENZA_PAGAMENTO NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "DATA_SCADENZA_PAGAMENTO NON PRESENTE");
            }

            // IMPORTO_DOVUTO
            if (StringUtils.isNotBlank(data[13])) {
                if (data[13].length() > 15) {
                    //IMPORTO_DOVUTO TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "IMPORTO_DOVUTO TROPPO GRANDE");
                }
                try {
                    importFlusso.setImportoDovuto(Double.valueOf(data[13]));
                } catch (Exception e) {
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "IMPORTO_DOVUTO NON CONFROME: " + data[13]);
                }
            } else {
                //IMPORTO_DOVUTO NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "IMPORTO_DOVUTO NON PRESENTE");
            }

            // COMMISSIONE_CARICO_PA
            if (StringUtils.isNotBlank(data[14])) {
                if (data[14].length() > 15) {
                    //COMMISSIONE_CARICO_PA TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "COMMISSIONE_CARICO_PA TROPPO GRANDE");
                }
                try {
                    importFlusso.setCommissioneCaricoPa(Double.valueOf(data[14]));
                } catch (Exception e) {
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "COMMISSIONE_CARICO_PA NON CONFROME: " + data[14]);
                }
            }

            // TIPO_DOVUTO
            if (StringUtils.isNotBlank(data[15])) {
                if (data[15].length() > 64) {
                    //TIPO_DOVUTO TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "TIPO_DOVUTO TROPPO GRANDE");
                }
                importFlusso.setTipoDovuto(data[15]);
            } else {
                //TIPO_DOVUTO NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "TIPO_DOVUTO NON PRESENTE");
            }

            // TIPO_VERSAMENTO
            if (StringUtils.isNotBlank(data[16])) {
                if (data[16].length() > 32) {
                    //TIPO_VERSAMENTO TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "TIPO_VERSAMENTO TROPPO GRANDE");
                }
                importFlusso.setTipoVersamento(data[16]);
            }

            // CAUSALE_VERSAMENTO
            if (StringUtils.isNotBlank(data[17])) {
                if (SupportedFileVersion.VERSIONE_1_0.getVersione_file().equalsIgnoreCase(versione)) {
                    if (data[17].length() > 140) {
                        //CAUSALE_VERSAMENTO TROPPO GRANDE
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CAUSALE_VERSAMENTO TROPPO GRANDE");
                    }
                } else {
                    if (data[17].length() > 1024) {
                        //CAUSALE_VERSAMENTO TROPPO GRANDE
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CAUSALE_VERSAMENTO TROPPO GRANDE");
                    }
                }
                importFlusso.setCausaleVersamento(data[17]);
            } else {
                //CAUSALE_VERSAMENTO NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CAUSALE_VERSAMENTO NON PRESENTE");
            }

            // DATI_SPECIFICI_RISCOSSIONE
            if (StringUtils.isNotBlank(data[18])) {
                if (data[18].length() > 140) {
                    //DATI_SPECIFICI_RISCOSSIONE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "DATI_SPECIFICI_RISCOSSIONE TROPPO GRANDE");
                }
                importFlusso.setDatiSpecificiRiscossione(data[18]);
            } else {
                //DATI_SPECIFICI_RISCOSSIONE NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "DATI_SPECIFICI_RISCOSSIONE NON PRESENTE");
            }

            // BILANCIO
            if (SupportedFileVersion.VERSIONE_1_2.getVersione_file().equalsIgnoreCase(versione) ||
                    SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione) ||
                    SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) {
                if (StringUtils.isNotBlank(data[19])) {
                    if (data[19].length() > 4096) {
                        //BILANCIO TROPPO GRANDE
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "BILANCIO TROPPO GRANDE");
                    }
                    importFlusso.setBilancio(data[19]);
                }
            }

            // FLAG_GENERA_IUV
            if (SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione) ||
                    SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) {
                if (StringUtils.isNotBlank(data[20])) {
                    if (data[20].length() <= 5) {
                        if (!data[20].equalsIgnoreCase("true") &&
                                !data[20].equalsIgnoreCase("false")) {
                            //FLAG_GENERA_IUV NON CONFORME
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "FLAG_GENERA_IUV NON CONFORME");
                        }
                    } else {
                        //FLAG_GENERA_IUV TROPPO GRANDE
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "FLAG_GENERA_IUV TROPPO GRANDE");
                    }
                    importFlusso.setFlagGeneraIuv(data[20]);
                }
            }

            //IUV_MULTI_7 MULTI_BENEFICIARIO
            if (SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione)) {
                // FLAG_MULTI_BENEFICIARIO
                if (StringUtils.isNotBlank(data[21])) {
                    if (data[21].length() <= 5) {
                        if (!data[21].equalsIgnoreCase("true") &&
                                !data[21].equalsIgnoreCase("false")) {
                            //FLAG_MULTI_BENEFICIARIO NON CONFORME
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "FLAG_MULTI_BENEFICIARIO NON CONFORME");
                        }
                    } else {
                        //FLAG_MULTI_BENEFICIARIO TROPPO GRANDE
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "FLAG_MULTI_BENEFICIARIO TROPPO GRANDE");
                    }
                    importFlusso.setFlagMultiBeneficiario(data[21]);
                }

                //Controllo se il flag è true, se è null o false allora tratto il dovuto come versione 1_3
                if (StringUtils.isNotBlank(importFlusso.getFlagMultiBeneficiario()) &&
                        importFlusso.getFlagMultiBeneficiario().equalsIgnoreCase("true")) {
                    // CODICE_FISCALE_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[22])) {
                        if (data[22].length() > 11) {
                            //CODICE_FISCALE_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CODICE_FISCALE_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setCodiceFiscaleEnteSecondario(data[22].toUpperCase());
                    } else {
                        //CODICE_FISCALE_ENTE_SECONDARIO NULL
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CODICE_FISCALE_ENTE_SECONDARIO NON PRESENTE");
                    }

                    // DENOMINAZIONE_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[23])) {
                        if (data[23].length() > 70) {
                            //DENOMINAZIONE_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "DENOMINAZIONE_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setDenominazioneEnteSecondario(data[23]);
                    } else {
                        //DENOMINAZIONE_ENTE_SECONDARIO NULL
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "DENOMINAZIONE_ENTE_SECONDARIO NON PRESENTE");
                    }

                    // IBAN_ACCREDITO_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[24])) {
                        if (data[24].length() > 35) {
                            //IBAN_ACCREDITO_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "IBAN_ACCREDITO_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setIbanAccreditoEnteSecondario(data[24]);
                    } else {
                        //IBAN_ACCREDITO_ENTE_SECONDARIO NULL
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "IBAN_ACCREDITO_ENTE_SECONDARIO NON PRESENTE");
                    }

                    // INDIRIZZO_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[25])) {
                        if (data[25].length() > 70) {
                            //INDIRIZZO_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "INDIRIZZO_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setIndirizzoEnteSecondario(data[25]);
                    }

                    // CIVICO_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[26])) {
                        if (data[26].length() > 16) {
                            //CIVICO_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CIVICO_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setCivicoEnteSecondario(data[26]);
                    }

                    // CAP_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[27])) {
                        if (data[27].length() > 16) {
                            //CAP_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CAP_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setCapEnteSecondario(data[27]);
                    }

                    // LOCALITA_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[28])) {
                        if (data[28].length() > 35) {
                            //LOCALITA_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "LOCALITA_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setLocalitaEnteSecondario(data[28]);
                    }

                    // PROVINCIA_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[29])) {
                        if (data[29].length() > 35) {
                            //PROVINCIA_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "PROVINCIA_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setProvinciaEnteSecondario(data[29]);
                    }

                    // NAZIONE_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[30])) {
                        if (data[30].length() > 2) {
                            //NAZIONE_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "NAZIONE_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setNazioneEnteSecondario(data[30]);
                    }

                    // DATI_SPECIFICI_RISCOSSIONE_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[31])) {
                        if (data[31].length() > 140) {
                            //DATI_SPECIFICI_RISCOSSIONE_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "DATI_SPECIFICI_RISCOSSIONE_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setDatiSpecificiRiscossioneEnteSecondario(data[31]);
                    }

                    // CAUSALE_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[32])) {
                        if (data[32].length() > 1024) {
                            //CAUSALE_ENTE_SECONDARIO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "CAUSALE_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        importFlusso.setCausaleVersamentoEnteSecondario(data[32]);
                    }

                    // IMPORTO_VERSAMENTO_ENTE_SECONDARIO
                    if (StringUtils.isNotBlank(data[33])) {
                        if (data[33].length() > 15) {
                            //IMPORTO_DOVUTO TROPPO GRANDE
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "IMPORTO_VERSAMENTO_ENTE_SECONDARIO TROPPO GRANDE");
                        }
                        try {
                            importFlusso.setImportoVersamentoEnteSecondario(Double.valueOf(data[33]));
                        } catch (Exception e) {
                            importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "IMPORTO_VERSAMENTO_ENTE_SECONDARIO NON CONFROME: " + data[33]);
                        }
                    } else {
                        //IMPORTO_DOVUTO NULL
                        importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "IMPORTO_VERSAMENTO_ENTE_SECONDARIO NON PRESENTE");
                    }
                }
            }

            // AZIONE
            int azione = 19;
            if (SupportedFileVersion.VERSIONE_1_2.getVersione_file().equalsIgnoreCase(versione))
                azione = 20;
            else if (SupportedFileVersion.VERSIONE_1_3.getVersione_file().equalsIgnoreCase(versione))
                azione = 21;
            else if (SupportedFileVersion.VERSIONE_1_4.getVersione_file().equalsIgnoreCase(versione))
                azione = 34;

            if (StringUtils.isNotBlank(data[azione])) {
                if (data[azione].length() != 1) {
                    //AZIONE TROPPO GRANDE
                    importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "AZIONE TROPPO GRANDE");
                }
                importFlusso.setAzione(data[azione]);
            } else {
                //AZIONE NULL
                importFlusso.setDeErrore(importFlusso.getDeErrore() + "|" + "AZIONE NON PRESENTE");
            }
        } catch (Exception e) {
            importFlusso.setDeErrore("ERRORE GENERICO NELLA LETTURA DELLA RIGA DEL CSV");
            log.error("ImportFlussoTaskService :: leggiVerificaRiga :: ERROR :: {}", e.getMessage());
            manageLog(e, "PAA_IMPORT_SINTASSI_CSV", importFlusso.getDeErrore());
        }
        log.debug("ImportFlussoTaskService :: leggiVerificaRiga :: End leggiVerificaRiga");
        return importFlusso;
    }

    private boolean copiaToWork(String cartellaFrom, String cartellaTo, String estensioneFile, String nomeFile, String operazione, ResultImportFlussoTo resultElaboraTo) {
        boolean verifica = true;
        String codErrore;
        String deErrore;
        try {
            log.debug("ImportFlussoTaskService :: copiaToWork :: Start copiaToWork :: cartellaFrom={} :: " +
                            "cartellaTo={} ::estensioneFile={} ::nomeFile={} ::operazione={} ::", cartellaFrom, cartellaTo, estensioneFile,
                    nomeFile, operazione);

            if (operazione.equalsIgnoreCase("sposta")) {
                Path fileToMovePath = Paths.get(cartellaFrom + separator + nomeFile + estensioneFile);
                Path targetPath = Paths.get(cartellaTo);
                Files.move(fileToMovePath, targetPath.resolve(fileToMovePath.getFileName()));
            } else if (operazione.equalsIgnoreCase("zip")) {
                byte[] buffer = new byte[1024];
                String newZipFile = cartellaTo + separator + nomeFile + ".zip";
                File fileToZip = new File(cartellaFrom + separator + nomeFile + estensioneFile);
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(newZipFile))) {
                    ZipEntry ze = new ZipEntry(nomeFile + estensioneFile);
                    zos.putNextEntry(ze);
                    try (FileInputStream in = new FileInputStream(fileToZip)) {
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }
                    zos.closeEntry();
                }
                if (fileToZip.exists()) {
                    if (!fileToZip.delete())
                        throw new MyPayException("cannot delete file " + fileToZip);
                }
            } else if (operazione.equalsIgnoreCase("zip_all")) {
                String newZipFile = cartellaTo + separator + nomeFile + ".zip";
                byte[] buffer = new byte[1024];
                List<String> filesName = new ArrayList<>();
                File folder = new File(cartellaFrom);
                File[] listOfFiles = folder.listFiles();
                for (int i = 0; i < (listOfFiles != null ? listOfFiles.length : 0); i++) {
                    if (listOfFiles[i].isFile()) {
                        filesName.add(listOfFiles[i].getName());
                    }
                }

                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(newZipFile))) {
                    for (String fileName : filesName) {
                        File fileToZip = new File(cartellaFrom + separator + fileName);
                        ZipEntry ze = new ZipEntry(fileName);
                        zos.putNextEntry(ze);
                        try (FileInputStream in = new FileInputStream(fileToZip)) {
                            int len;
                            while ((len = in.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        }
                        zos.closeEntry();
                    }
                }
                for (String fileName : filesName) {
                    File fileAddedToZip = new File(cartellaFrom + separator + fileName);
                    if (fileAddedToZip.exists()) {
                        if (!fileAddedToZip.delete())
                            throw new MyPayException("cannot delete file " + fileAddedToZip);
                    }
                }
            } else if (operazione.equalsIgnoreCase("unzip")) {
                File dir = new File(cartellaTo);
                if (!dir.exists()&& !
                    dir.mkdirs())
          throw new MyPayException("error creating dir: "+dir);
                FileInputStream fis;
                byte[] buffer = new byte[1024];
                try {
                    fis = new FileInputStream(cartellaFrom + separator + nomeFile + estensioneFile);
                    ZipInputStream zis = new ZipInputStream(fis);
                    ZipEntry ze = zis.getNextEntry();
                    while (ze != null) {

                        String fileName = ze.getName();
                        File newFile = new File(cartellaTo + separator + fileName);
                        File newFileParent = newFile.getParentFile();
            if(newFileParent==null || !newFileParent.exists() && !newFileParent.mkdirs())
              throw new MyPayException("error creating parent dir: "+newFileParent);
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {

                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                        zis.closeEntry();
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                    zis.close();
                    fis.close();
                } catch (IOException e) {
                    log.error("ImportFlussoTaskService :: copiaToWork :: unzip :: Errore nell'unzipping del file  :: ", e);
                    throw e;
                }
            } else if (operazione.equalsIgnoreCase("delete")) {
                if (StringUtils.isNotBlank(nomeFile))
                    Files.delete(Paths.get(cartellaFrom + separator + nomeFile + estensioneFile));
                else
                    Files.delete(Paths.get(cartellaFrom));
            } else if (operazione.equalsIgnoreCase("delete_all")) {
                File folder = new File(cartellaFrom);
                File[] listOfFiles = folder.listFiles();
                for (int i = 0; i < (listOfFiles != null ? listOfFiles.length : 0); i++) {
                    if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".pdf")) {
                        if (Files.exists(Paths.get(cartellaFrom + separator + listOfFiles[i].getName())))
                            Files.delete(Paths.get(cartellaFrom + separator + listOfFiles[i].getName()));
                    }
                }
                Files.delete(Paths.get(cartellaFrom));
            } else if (operazione.equalsIgnoreCase("sposta_rinomina")) {
                DateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
                String convertedDate = parser.format(new Date());
                Path fileToMovePath = Paths.get(cartellaFrom + separator + nomeFile + estensioneFile);
                Path destinationFilePath = Paths.get(cartellaFrom + separator + nomeFile + "_" + convertedDate + estensioneFile);
                Path targetPath = Paths.get(cartellaTo);
                Files.move(fileToMovePath, targetPath.resolve(destinationFilePath.getFileName()), REPLACE_EXISTING);
            } else if (operazione.equalsIgnoreCase("copia")) {
                Path fileCopy = Paths.get(cartellaFrom).resolve(Paths.get(nomeFile + estensioneFile));
                Files.copy(fileCopy, Paths.get(cartellaTo).resolve(fileCopy.getFileName()), REPLACE_EXISTING);
            } else {
                verifica = false;
                log.error("ImportFlussoTaskService :: copiaToWork :: ERROR :: NESSUN AZIONE SCELTA :: cartellaFrom :: {} :: cartellaTo :: {} " +
                        ":: estensioneFile :: {} :: nomeFile :: {} :: operazione :: {}", cartellaFrom, cartellaTo, estensioneFile, nomeFile, operazione);
                codErrore = "PAA_IMPORT_ERROR";
                deErrore = "Errore durante la gestione dei file presenti sul server, nessuna operazione valida scelta per il file: " + nomeFile;
                manageLog(new Exception("Nessuna azione sui file scelta"), codErrore, deErrore);
                if (resultElaboraTo != null) {
                    resultElaboraTo.setCodErrori(codErrore);
                    resultElaboraTo.setDeErrori(deErrore);
                }
            }
        } catch (Exception e) {
            verifica = false;
            log.error("ImportFlussoTaskService :: copiaToWork :: ERROR :: cartellaFrom :: {} :: cartellaTo :: {} " +
                    ":: estensioneFile :: {} :: nomeFile :: {} :: operazione :: {}", cartellaFrom, cartellaTo, estensioneFile, nomeFile, operazione);
            log.error("ImportFlussoTaskService :: copiaToWork :: ERROR", e);
            codErrore = "PAA_IMPORT_ERROR";
            deErrore = "Errore durante la gestione dei file presenti sul server, operazione: " + operazione + "per il file: " + nomeFile;
            if (resultElaboraTo != null) {
                resultElaboraTo.setCodErrori(codErrore);
                resultElaboraTo.setDeErrori(deErrore);
            }
            manageLog(e, codErrore, deErrore);
        }
        return verifica;
    }

    private boolean verificaNomeVersioneFile(String nomeFile, Ente ente, String valueMD5, String calculatedMD5, ResultImportFlussoTo resultElaboraTo) {
        boolean verifica = true;
        String codErrore = "";
        String deErrore = "";
        try {
            log.debug("ImportFlussoTaskService :: verificaNomeVersioneFile :: Start verificaNomeVersioneFile");
            String[] temp;
            String delimiter = "-";
            String codIpaEnteFile;
            temp = nomeFile.split(delimiter);
            codIpaEnteFile = temp[0];
            String versione = temp[2];
            if (temp.length != 3) {
                codErrore = "PAA_IMPORT_NOME_FILE_ERR";
                deErrore = "Il nome del file '" + nomeFile + "' non rispetta la convenzione '<codice IPA>-<identificativo univoco flusso>-<versione tracciato>.zip'. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente' .";
                verifica = false;
                manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
            } else if (!codIpaEnteFile.equalsIgnoreCase(ente.getCodIpaEnte())) {
                codErrore = "PAA_IMPORT_ENTE_ERR(" + codIpaEnteFile + "," + ente.getCodIpaEnte() + ")";
                deErrore = "Codice IPA nel nome file '" + codIpaEnteFile + "' diverso da quello dell'ente per cui si carica il file da Backoffice/WS '" + ente.getCodIpaEnte() + "'";
                verifica = false;
                manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
            } else {
                log.debug("ImportFlussoTaskService :: verificaNomeVersioneFile :: Recuperato valore 'codIpaEnteFile' da file : " + codIpaEnteFile);
                SupportedFileVersion supportedFileVersion = SupportedFileVersion.GET_VERSIONE_FILE(versione);
                if (supportedFileVersion == null) {
                    codErrore = "PAA_IMPORT_FILE_VERSIONE_ERR(" + versione + ")";
                    deErrore = "La versione tracciato del file '" + versione + "' non è supportata. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente' .";
                    verifica = false;
                    manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
                } else {
                    if (!valueMD5.equalsIgnoreCase(calculatedMD5)) {
                        codErrore = "PAA_IMPORT_NO_MATCH_MD5";
                        deErrore = "Il confronto tra i due codici MD5 calcolato e inserito non coincide, probabile errore ricezione flusso dati";
                        verifica = false;
                        manageLog(new Exception(codErrore + deErrore), codErrore, deErrore);
                    }
                }
            }
        } catch (Exception e) {
            verifica = false;
            if (!StringUtils.isNotBlank(codErrore))
                codErrore = "PAA_IMPORT_NOME_FILE_ERR(" + nomeFile + ")";
            if (!StringUtils.isNotBlank(deErrore))
                deErrore = "Il nome del file '" + nomeFile + "' non rispetta la convenzione '<codice IPA>-<identificativo univoco flusso>-<versione tracciato>.zip'. Per maggiori informazioni fare riferimento al manuale 'Integrazione Ente'.";
            log.error("ImportFlussoTaskService :: verificaNomeVersioneFile :: {} :: {}", codErrore, deErrore);
            manageLog(e, codErrore, deErrore);
        }
        log.debug("ImportFlussoTaskService :: verificaNomeVersioneFile :: End verificaNomeVersioneFile");
        resultElaboraTo.setCodErrori(codErrore);
        resultElaboraTo.setDeErrori(deErrore);
        resultElaboraTo.setStatoElaborazione(verifica);
        return verifica;
    }

}
