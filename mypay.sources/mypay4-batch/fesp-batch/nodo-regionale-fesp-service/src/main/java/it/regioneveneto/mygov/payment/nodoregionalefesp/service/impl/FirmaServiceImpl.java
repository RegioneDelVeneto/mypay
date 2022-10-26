/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import it.regioneveneto.mygov.payment.constants.FaultCodeConstants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.FirmaNotValidException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.FirmaService;
import it.regioneveneto.mygov.payment.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.axis.soap.MessageFactoryImpl;
import org.apache.axis.soap.SOAPConnectionFactoryImpl;
import org.apache.axis.soap.SOAPConnectionImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author regione del veneto
 * @author regione del veneto
 */
public class FirmaServiceImpl implements FirmaService {

	private static final Log log = LogFactory.getLog(FirmaServiceImpl.class);

	private static final String VERIFICA_FILE_SERVICE = "VerificaFileService";
	private static final String DEFAULT_TIMEOUT = "60000";
	private static final int ID_RICHIESTA_LENGHT = 24;
	public static final String INTESTAZIONE_ALLEGATO_NAME = "XML_MESSAGE";
	public static final String INFORMAZIONI_VERIFICHE = "informazioniVerifiche.xml";
	public static final String ESTENSIONE_DOCUMENTO = ".odt";
	public static final String ESTENSIONE_DOCUMENTO_FIRMATO = ".p7m";
	public static final String ESITO_VERIFICA_OK = "S";
	public static final String INTESTAZIONE_DIRV = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + "<Intestazione>"
			+ "<IdRichiesta>PLACEHOLDER_ID_RICHIESTA</IdRichiesta>"
			+ "<TmstInvio>PLACEHOLDER_TIMESTAMP_INVIO</TmstInvio>" + "<Mittente>NODOREGIONALEFESP</Mittente>"
			+ "<Destinatario>DIRV</Destinatario>" + "<UserId>mrossi</UserId>" + "<Password>mrossi</Password>"
			+ "<Servizio>PLACEHOLDER_SERVIZIO</Servizio>" + "<RichiestaApplicativa>" + "PLACEHOLDER_ALLEGATI"
			+ "</RichiestaApplicativa>" + "</Intestazione>";

	// ex. VerificaFileService
	public static final String PLACEHOLDER_SERVIZIO = "PLACEHOLDER_SERVIZIO";
	public static final String PLACEHOLDER_ALLEGATI = "PLACEHOLDER_ALLEGATI";
	// ex. 049773094022390575875829
	public static final String PLACEHOLDER_ID_RICHIESTA = "PLACEHOLDER_ID_RICHIESTA";
	// ex. 01/04/14_9.00.00
	public static final String PLACEHOLDER_TIMESTAMP_INVIO = "PLACEHOLDER_TIMESTAMP_INVIO";
	public static final SimpleDateFormat FORMAT_TIMESTAMP_INVIO = new SimpleDateFormat("dd/MM/yy_H.mm.ss");
	public static final SimpleDateFormat FORMAT_TIMESTAMP_FILENAME = new SimpleDateFormat("SSSssmmHHddMMyyyy");

	/**
	 * Url per la DIRV del servizio Verifica Firma
	 */
	private String nodoDirvVerificaFirmaUrl;

	/**
	 * 
	 */
	public FirmaServiceImpl() {
		super();
	}

	/**
	 * @param signed
	 * @param trackingInfo
	 * @return
	 * @throws FirmaNotValidException
	 */
	@Override
	public byte[] verify(final byte[] signed, final String trackingInfo) throws FirmaNotValidException {

		byte[] unsigned = null;

		Date currentTimestamp = new Date();
		String nomeAllegato = null;
		
		
		//Se viene passata una qualche informazione sulla modalita di tracking, il nome dell'allegato viene valorizzato con essa
		//Altrimenti viene utilizzato un timestamp
		if(StringUtils.isNotBlank(trackingInfo))
			nomeAllegato = trackingInfo;
		else
			nomeAllegato = FORMAT_TIMESTAMP_FILENAME.format(currentTimestamp);			
		
		String intestazione = elaborateIntestazione(currentTimestamp, nomeAllegato);
		
		DataHandler intestazioneDh = Utils.getDataHandlerFromBytes(intestazione.getBytes(), "application/xml");
		DataHandler rtFirmataDh = Utils.getDataHandlerFromBytes(signed, "application/xml");

		Map<String, DataHandler> mapResults = null;

		SOAPConnectionFactoryImpl soapConnectionFactory = new SOAPConnectionFactoryImpl();
		try {
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			MessageFactoryImpl messageFactory = new MessageFactoryImpl();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			SOAPEnvelope requestEnvelope = soapPart.getEnvelope();
			SOAPBody body = requestEnvelope.getBody();
			SOAPBodyElement bodyElement = null;
			bodyElement = body.addBodyElement(requestEnvelope.createName("payload", "spagic", "urn:eng:spagic"));

			// INTESTAZIONE
			SOAPElement elementIntestazione = bodyElement.addChildElement(requestEnvelope.createName("attachments"));
			AttachmentPart attachmentIntestazione = soapMessage.createAttachmentPart(intestazioneDh);
			attachmentIntestazione.setContentId(INTESTAZIONE_ALLEGATO_NAME);
			soapMessage.addAttachmentPart(attachmentIntestazione);
			elementIntestazione.addAttribute(requestEnvelope.createName("href"), "attachmentName:"
					+ attachmentIntestazione.getContentId());

			// RT FIRMATA
			SOAPElement elementRtFirmata = bodyElement.addChildElement(requestEnvelope.createName("attachments"));
			AttachmentPart attachmentRtFirmata = soapMessage.createAttachmentPart(rtFirmataDh);

			attachmentRtFirmata.setContentId(nomeAllegato + ESTENSIONE_DOCUMENTO + ESTENSIONE_DOCUMENTO_FIRMATO);
			soapMessage.addAttachmentPart(attachmentRtFirmata);
			elementRtFirmata.addAttribute(requestEnvelope.createName("href"),
					"attachmentName:" + attachmentRtFirmata.getContentId());

			SOAPConnectionImpl sOAPConnectionImpl = ((SOAPConnectionImpl) soapConnection);
			sOAPConnectionImpl.setTimeout(new Integer(DEFAULT_TIMEOUT));

			log.debug("Chiamata al servizio di Verifica Firma...[" + nodoDirvVerificaFirmaUrl + "]");
			javax.xml.soap.SOAPMessage returnedSOAPMessage = sOAPConnectionImpl.call(soapMessage,
					this.nodoDirvVerificaFirmaUrl);
			// Gestione allegati di risposta
			mapResults = getAttachmentHashmap(returnedSOAPMessage);
			int countResponseAttachments = mapResults.size();
			log.debug("Chiamata alla DIRV [VerificaFileService] effettuata: ottenuti [" + countResponseAttachments
					+ "] allegati.");

			DataHandler xmlMessage = (DataHandler) mapResults.get(INTESTAZIONE_ALLEGATO_NAME);
			DataHandler xmlInformazioniVerifiche = (DataHandler) mapResults.get(INFORMAZIONI_VERIFICHE);
			String xmlMessageString = Utils.getStringFromDataHandler(xmlMessage);

			if (xmlInformazioniVerifiche == null) {
				try {
					SAXReader reader = new SAXReader();
					Element rootNodeXmlMessage = null;
					Document documentXmlMessage = reader.read(xmlMessage.getInputStream());
					rootNodeXmlMessage = documentXmlMessage.getRootElement();
					String descrizioneRispostaApplicativa = rootNodeXmlMessage
							.selectSingleNode("//RispostaApplicativa").getText();
					throw new FirmaNotValidException(FaultCodeConstants.PAA_FIRMA_ERRATA,
							descrizioneRispostaApplicativa);
				} catch (DocumentException | IOException e) {
					throw new FirmaNotValidException(
							FaultCodeConstants.PAA_FIRMA_ERRATA,
							"Errore nel recuperare le informazioni della descrizione della Risposta Applicativa nell' xmlMessage",
							e);
				}
			}

			log.debug("Intestazione ritornata dal servizio di Verifica Firma: " + xmlMessageString);
			String xmlInformazioniVerificheString = Utils.getStringFromDataHandler(xmlInformazioniVerifiche);
			log.debug("Informazioni sulla verifica ritornate dal servizio di Verifica Firma: "
					+ xmlInformazioniVerificheString);

			SAXReader reader = new SAXReader();
			Document document = null;
			Element rootNode = null;
			String esito = null;
			try {
				document = reader.read(xmlInformazioniVerifiche.getInputStream());
				rootNode = document.getRootElement();
				esito = rootNode.selectSingleNode("//EsitoVerifica").getText();
			} catch (DocumentException | IOException e) {
				throw new FirmaNotValidException(FaultCodeConstants.PAA_FIRMA_ERRATA,
						"Errore nel recuperare le informazioni dell'esito della verifica firma", e);
			}
			if (ESITO_VERIFICA_OK.equalsIgnoreCase(esito)) {
				DataHandler documentoNonFirmato = (DataHandler) mapResults.get(nomeAllegato + ESTENSIONE_DOCUMENTO);
				InputStream is;
				try {
					is = documentoNonFirmato.getInputStream();
					// It handles large files by copying the bytes in blocks of
					// 4MB.
					unsigned = IOUtils.toByteArray(is);
				} catch (IOException e) {
					throw new FirmaNotValidException(FaultCodeConstants.PAA_FIRMA_ERRATA,
							"Errore generato nel gestire il binary del documento ritornato dalla verifica firma", e);
				}
			} else {
				String descrizioneEsito = rootNode.selectSingleNode("//DescrEsito").getText();
				throw new FirmaNotValidException(FaultCodeConstants.PAA_FIRMA_ERRATA, descrizioneEsito);
			}
		} catch (SOAPException e) {
			log.error("Errore nella chiamata alla DIRV per il servizio Verifica Firma.", e);
			throw new FirmaNotValidException(FaultCodeConstants.PAA_FIRMA_ERRATA,
					"Errore nella chiamata alla DIRV per il servizio Verifica Firma.", e);
		}

		return unsigned;
	}

	private String elaborateIntestazione(Date currentTimestamp, String nomeAllegato) {
		String intestazione = INTESTAZIONE_DIRV;
		String timestamp = FORMAT_TIMESTAMP_INVIO.format(currentTimestamp);

		intestazione = intestazione.replaceAll(PLACEHOLDER_SERVIZIO, VERIFICA_FILE_SERVICE);
		intestazione = intestazione.replaceAll(PLACEHOLDER_TIMESTAMP_INVIO, timestamp);

		SecureRandom random = new SecureRandom();
		String idRichiesta = new BigInteger(110, random).toString(ID_RICHIESTA_LENGHT);
		intestazione = intestazione.replaceAll(PLACEHOLDER_ID_RICHIESTA, idRichiesta);

		StringBuffer allegatiXml = new StringBuffer();
		allegatiXml.append("<Allegati>");
		allegatiXml.append("<Allegato file_name=");
		allegatiXml.append("\"" + nomeAllegato + ESTENSIONE_DOCUMENTO + ESTENSIONE_DOCUMENTO_FIRMATO + "\"");
		allegatiXml.append(" mime_type=");
		allegatiXml.append("\"application/timestamp-reply\">");
		allegatiXml.append(nomeAllegato + ESTENSIONE_DOCUMENTO + ESTENSIONE_DOCUMENTO_FIRMATO);
		allegatiXml.append("</Allegato>");
		allegatiXml.append("</Allegati>");
		intestazione = intestazione.replaceAll(PLACEHOLDER_ALLEGATI, allegatiXml.toString());

		return intestazione;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, DataHandler> getAttachmentHashmap(SOAPMessage returnedSOAPMessage) throws SOAPException {
		Map<String, DataHandler> attachmentHashMap = new HashMap<String, DataHandler>();
		for (Iterator<AttachmentPart> iterator = returnedSOAPMessage.getAttachments(); iterator.hasNext();) {
			AttachmentPart attachment = (AttachmentPart) iterator.next();
			String attachmentName = attachment.getContentId();
			attachmentHashMap.put(attachmentName, attachment.getDataHandler());
		}
		return attachmentHashMap;
	}

	public String getNodoDirvVerificaFirmaUrl() {
		return nodoDirvVerificaFirmaUrl;
	}

	public void setNodoDirvVerificaFirmaUrl(String nodoDirvVerificaFirmaUrl) {
		this.nodoDirvVerificaFirmaUrl = nodoDirvVerificaFirmaUrl;
	}
}
