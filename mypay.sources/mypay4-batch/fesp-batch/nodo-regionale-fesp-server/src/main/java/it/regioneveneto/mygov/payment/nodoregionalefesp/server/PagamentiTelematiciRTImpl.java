package it.regioneveneto.mygov.payment.nodoregionalefesp.server;

import gov.telematici.pagamenti.ws.nodoregionalepernodospc.EsitoPaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.FaultBean;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.PaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.PaaInviaRTRisposta;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.TipoInviaEsitoStornoRisposta;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.TipoInviaRichiestaRevocaRisposta;
import gov.telematici.pagamenti.ws.ppthead.IntestazionePPT;
import it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloPagamentoRT;
import it.gov.digitpa.schemas.x2011.pagamenti.CtRicevutaTelematica;
import it.gov.digitpa.schemas.x2011.pagamenti.RTDocument;
import it.gov.digitpa.schemas.x2011.pagamenti.StFirmaRicevuta;
import it.gov.digitpa.schemas.x2011.pagamenti.revoche.CtDatiRevoca;
import it.gov.digitpa.schemas.x2011.pagamenti.revoche.CtDatiSingolaRevoca;
import it.gov.digitpa.schemas.x2011.pagamenti.revoche.CtIstitutoAttestante;
import it.gov.digitpa.schemas.x2011.pagamenti.revoche.CtRichiestaRevoca;
import it.gov.digitpa.schemas.x2011.pagamenti.revoche.CtSoggettoPagatore;
import it.gov.digitpa.schemas.x2011.pagamenti.revoche.CtSoggettoVersante;
import it.gov.digitpa.schemas.x2011.pagamenti.revoche.RRDocument;
import it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematicirt.PagamentiTelematiciRT;
import it.regioneveneto.mygov.payment.constants.Constants;
import it.regioneveneto.mygov.payment.constants.FaultCodeConstants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRrEr;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRtDettaglio;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils.FespBean;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RpEDettaglioDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RptRtDettaglioDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RrErDettaglioDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.FirmaNotValidException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.EnteService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.FirmaService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.GiornaleService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPEService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPTRTService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRRService;
import it.regioneveneto.mygov.payment.pa.client.PagamentiTelematiciEsitoServiceClient;
import it.regioneveneto.mygov.payment.utils.Utils;
import it.regioneveneto.mygov.payment.pa.client.InviaRichiestaRevocaServiceClient;
import it.veneto.regione.pagamenti.pa.papernodoregionale.EsitoPaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.papernodoregionale.PaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.papernodoregionale.PaaSILInviaEsitoRisposta;
import it.veneto.regione.pagamenti.pa.papernodoregionale.inviarichiestarevoca.PaaSILInviaRichiestaRevoca;
import it.veneto.regione.pagamenti.pa.papernodoregionale.inviarichiestarevoca.PaaSILInviaRichiestaRevocaRisposta;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.beans.factory.annotation.Autowired;

@javax.jws.WebService(serviceName = "PagamentiTelematiciRTservice", portName = "PPTPort", targetNamespace = "http://NodoPagamentiSPC.spcoop.gov.it/servizi/PagamentiTelematiciRT", wsdlLocation = "classpath:it/regioneveneto/mygov/payment/nodoregionalefesp/server/nodo-regionale-per-nodo-spc.wsdl", endpointInterface = "it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematicirt.PagamentiTelematiciRT")
public class PagamentiTelematiciRTImpl implements PagamentiTelematiciRT {

	/**
	 * 
	 */
	private static final Log LOG = LogFactory.getLog(PagamentiTelematiciRTImpl.class.getName());

	/**
	 * 
	 */
	private ManageRPTRTService manageRPTRTService;

	/**
	 * 
	 */
	private ManageRPEService manageRPEService;

	/**
	 * 
	 */
	private PagamentiTelematiciEsitoServiceClient pagamentiTelematiciEsitoServiceClient;
	
	/**
	 * 
	 */
	private InviaRichiestaRevocaServiceClient inviaRichiestaRevocaServiceClient;

	/**
	 * 
	 */
	private FirmaService firmaService;

	/**
	 * 
	 */
	@Autowired
	private GiornaleService giornaleService;
	
	/**
	 * 
	 */
	private FespBean fespProperties;
	
	/**
	 * 
	 */
	private EnteService enteService;
	
	/**
	 * 
	 */
	private ManageRRService manageRRService;

	/**
	 * 
	 */
	public PagamentiTelematiciRTImpl() {
		super();
	}

	/**
	 * @param manageRPTRTService
	 *            the manageRPTRTService to set
	 */
	public void setManageRPTRTService(ManageRPTRTService manageRPTRTService) {
		this.manageRPTRTService = manageRPTRTService;
	}

	/**
	 * @param manageRRService
	 *            the manageRRService to set
	 */
	public void setManageRRService(ManageRRService manageRRService) {
		this.manageRRService = manageRRService;
	}
	
	
	/**
	 * @param manageRPEService
	 *            the manageRPEService to set
	 */
	public void setManageRPEService(ManageRPEService manageRPEService) {
		this.manageRPEService = manageRPEService;
	}

	/**
	 * @param pagamentiTelematiciEsitoServiceClient
	 *            the pagamentiTelematiciEsitoServiceClient to set
	 */
	public void setPagamentiTelematiciEsitoServiceClient(
			PagamentiTelematiciEsitoServiceClient pagamentiTelematiciEsitoServiceClient) {
		this.pagamentiTelematiciEsitoServiceClient = pagamentiTelematiciEsitoServiceClient;
	}

	 
	/**
	 * 
	 * @param inviaRichiestaRevocaServiceClient
	 */
	public void setInviaRichiestaRevocaServiceClient(
			InviaRichiestaRevocaServiceClient inviaRichiestaRevocaServiceClient) {
		this.inviaRichiestaRevocaServiceClient = inviaRichiestaRevocaServiceClient;
	}

	/**
	 * @param firmaService
	 *            the firmaService to set
	 */
	public void setFirmaService(FirmaService firmaService) {
		this.firmaService = firmaService;
	}
	
	/**
	 * @param fespProperties the fespProperties to set
	 */
	public void setFespProperties(FespBean fespProperties) {
		this.fespProperties = fespProperties;
	}
	
	/**
	 * @param enteService the enteService to set
	 */
	public void setEnteService(EnteService enteService) {
		this.enteService = enteService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematicirt.
	 * PagamentiTelematiciRT#paaInviaEsitoStorno(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * byte[])
	 */
	@Override
	public TipoInviaEsitoStornoRisposta paaInviaEsitoStorno(String identificativoIntermediarioPA,
			String identificativoStazioneIntermediarioPA, String identificativoDominio,
			String identificativoUnivocoVersamento, String codiceContestoPagamento, byte[] er) {
		throw new RuntimeException("method not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematicirt.
	 * PagamentiTelematiciRT#paaInviaRT(gov.telematici.pagamenti.ws.PaaInviaRT
	 * bodyrichiesta ,)gov.telematici.pagamenti.ws.ppthead.IntestazionePPT
	 * header )*
	 */
	public PaaInviaRTRisposta paaInviaRT(PaaInviaRT bodyrichiesta, IntestazionePPT header) {

		LOG.info("Executing operation paaInviaRT");

		PaaInviaRTRisposta _rispostaRT = new PaaInviaRTRisposta();

		PaaSILInviaEsitoRisposta _rispostaEsito = null;
		it.veneto.regione.schemas.x2012.pagamenti.CtEsito ctEsito = null;
		CtRicevutaTelematica rt = null;

		MygovRptRt mygovRptRt = null;

		// Il binario dell'RT inviata dal nodo nazionale
		byte[] rtPayloadInviatoOriginale = bodyrichiesta.getRt();
		byte[] rtPayloadInChiaro = null;

		boolean precedenteAbortita;
		boolean immediatoKO;
		boolean modelloTreOK;
		boolean esisteRevoca;

		String tipoFirmaRicevuto = bodyrichiesta.getTipoFirma();
		
		
		try {

			LOG.debug("decodifico RT per dominio [" + header.getIdentificativoDominio() + "] e IUV ["
					+ header.getIdentificativoUnivocoVersamento() + "]");

			// RECUPERA E DECODIFICA L'RT GESTENDO L'EVENTUALE FIRMA
			try {

				rtPayloadInChiaro = decodificaRt(rtPayloadInviatoOriginale, tipoFirmaRicevuto,
						header.getIdentificativoUnivocoVersamento());

				RTDocument decRt = null;
				try {
					String rtString = new String(rtPayloadInChiaro, "UTF-8");

					LOG.debug("RICEVUTA RT:" + rtString);

					decRt = RTDocument.Factory.parse(rtString);
				} catch (Exception e) {
					LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR, e);

					fillAndSaveRispostaRT_Error(_rispostaRT, FaultCodeConstants.PAA_SYSTEM_ERROR,
							e.getClass().getName() +  ": Failed to parse Ricevuta Telematica ::: Exception :::",
							null);

					/*
					 * LOG GIORNALE DEGLI EVENTI
					 */
					try {
						Date dataOraEvento = new Date();
						String identificativoDominio = header.getIdentificativoDominio();
						String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
						String codiceContestoPagamento = header.getCodiceContestoPagamento();
						String identificativoPrestatoreServiziPagamento = null;
						String tipoVersamento = null;
						String componente = Constants.COMPONENTE.FESP.toString();
						String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
						String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
						String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
						String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

						String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
						String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
						String canalePagamento = "";

						String xmlString = "";
						try {
							gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory();
							JAXBContext context = JAXBContext.newInstance(PaaInviaRTRisposta.class);
							Marshaller marshaller = context.createMarshaller();
							StringWriter sw = new StringWriter();
							marshaller.marshal(objectFactory.createPaaInviaRTRisposta(_rispostaRT), sw);
							xmlString = sw.toString();
						} catch (JAXBException jaxbex) {
							LOG.error("Errore deserializzazione RTRisposta");
						}

						String parametriSpecificiInterfaccia = xmlString;

						String esitoReq = _rispostaRT.getPaaInviaRTRisposta().getEsito();

						giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
								codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
								categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
								identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
								esitoReq);
					} catch (Exception ex1) {
						LOG.warn("paaInviaRT RESPONSE impossibile inserire nel giornale degli eventi", ex1);
					}

					return _rispostaRT;
				}

				rt = decRt.getRT();
			} catch (FirmaNotValidException e) {

				String codeError = e.getCode();
				String messageError = e.getDescription();

				LOG.error(codeError + ": " + messageError);

				EsitoPaaInviaRT rt_esitoPaaInviaRT = new EsitoPaaInviaRT();
				rt_esitoPaaInviaRT.setEsito("KO");
				FaultBean rt_rispostaEsito_FaultBean = new FaultBean();
				rt_rispostaEsito_FaultBean.setFaultCode(codeError);
				rt_rispostaEsito_FaultBean.setDescription(messageError);
				rt_esitoPaaInviaRT.setFault(rt_rispostaEsito_FaultBean);
				_rispostaRT.setPaaInviaRTRisposta(rt_esitoPaaInviaRT);

				return _rispostaRT;
			}

			/*
			 * LOG GIORNALE DEGLI EVENTI
			 */
			try {

				Date dataOraEvento = new Date();
				String identificativoDominio = header.getIdentificativoDominio();
				String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
				String codiceContestoPagamento = header.getCodiceContestoPagamento();
				String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
						.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
				String tipoVersamento = null;
				String componente = Constants.COMPONENTE.FESP.toString();
				String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
				String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
				String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

				String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
				String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
				String canalePagamento = "";

				String xmlString = "";
				try {
					gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory();
					JAXBContext context = JAXBContext.newInstance(PaaInviaRT.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createPaaInviaRT(bodyrichiesta), sw);
					xmlString = sw.toString();
				} catch (JAXBException jaxbex) {
					LOG.error("Errore deserializzazione RT");
				}

				String parametriSpecificiInterfaccia = xmlString;

				String esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
			} catch (Exception ex) {
				LOG.warn("paaInviaRT REQUEST impossibile inserire nel giornale degli eventi", ex);
			}

			mygovRptRt = this.manageRPTRTService.getRptByCodRptIdMessaggioRichiesta(rt.getRiferimentoMessaggioRichiesta());
			if (mygovRptRt == null) {

				String messageError = "[RPT non trovata per Identificativo Messaggio Richiesta ["
						+ rt.getRiferimentoMessaggioRichiesta() + "]";

				LOG.error(FaultCodeConstants.PAA_RPT_SCONOSCIUTA + ": " + messageError);

				EsitoPaaInviaRT rt_esitoPaaInviaRT = new EsitoPaaInviaRT();
				rt_esitoPaaInviaRT.setEsito("KO");
				FaultBean rt_rispostaEsito_FaultBean = new FaultBean();
				rt_rispostaEsito_FaultBean.setFaultCode(FaultCodeConstants.PAA_RPT_SCONOSCIUTA);
				rt_rispostaEsito_FaultBean.setDescription(messageError);
				rt_esitoPaaInviaRT.setFault(rt_rispostaEsito_FaultBean);
				_rispostaRT.setPaaInviaRTRisposta(rt_esitoPaaInviaRT);

				/*
				 * LOG GIORNALE DEGLI EVENTI
				 */
				try {
					Date dataOraEvento = new Date();
					String identificativoDominio = header.getIdentificativoDominio();
					String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
					String codiceContestoPagamento = header.getCodiceContestoPagamento();
					String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
							.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
					String tipoVersamento = null;
					String componente = Constants.COMPONENTE.FESP.toString();
					String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
					String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
					String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
					String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

					String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
					String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
					String canalePagamento = "";

					String xmlString = "";
					try {
						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory();
						JAXBContext context = JAXBContext.newInstance(PaaInviaRTRisposta.class);
						Marshaller marshaller = context.createMarshaller();
						StringWriter sw = new StringWriter();
						marshaller.marshal(objectFactory.createPaaInviaRTRisposta(_rispostaRT), sw);
						xmlString = sw.toString();
					} catch (JAXBException jaxbex) {
						LOG.error("Errore deserializzazione RTRisposta");
					}

					String parametriSpecificiInterfaccia = xmlString;

					String esitoReq = _rispostaRT.getPaaInviaRTRisposta().getEsito();

					giornaleService.registraEvento(dataOraEvento, identificativoDominio,
							identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
							tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
							esitoReq);
				} catch (Exception ex1) {
					LOG.warn("paaInviaRT RESPONSE impossibile inserire nel giornale degli eventi", ex1);
				}

				return _rispostaRT;
			}

			String esitoRT = rt.getDatiPagamento().getCodiceEsitoPagamento().toString();

			// se in precedenza ho ricevuto una RT ed era ABORTITA
			precedenteAbortita = mygovRptRt.getCodRtDatiPagCodiceEsitoPagamento() != null
					&& mygovRptRt.getCodRtDatiPagCodiceEsitoPagamento().equals("9");

			// Nel caso di una RT ricevuta a seguito di un invio RPT modello
			// "immediato" con esito risposta invio RPT "KO", memorizzare la RT
			// sul fesp e non propagarla a pa.
			immediatoKO = mygovRptRt.getDeRptInviarptEsito().equals("KO")
					&& (mygovRptRt.getModelloPagamento() == 0 || mygovRptRt.getModelloPagamento() == 1);
			
			modelloTreOK = esitoRT.equals("0") && mygovRptRt.getModelloPagamento() == 4;
			
			esisteRevoca = esisteRevoca(header.getIdentificativoDominio(),header.getIdentificativoUnivocoVersamento(),header.getCodiceContestoPagamento());
			LOG.error("esisteRevoca" + ": " + esisteRevoca);
			
			if (precedenteAbortita) {
				if (!esitoRT.equals("1")) { // pagato o parzialmente pagato
					// rt con esito non possibile (RT precedente ABORTITA e ora
					// mi arriva un esito pagato o parzialmente pagato!!)

					String messageError = "[Pagamento con IUV: " + header.getIdentificativoUnivocoVersamento()
							+ " abortito]";

					LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": " + messageError);

					fillAndSaveRispostaRT_Error(_rispostaRT, FaultCodeConstants.PAA_SYSTEM_ERROR, messageError,
							mygovRptRt.getMygovRptRtId());

					/*
					 * LOG GIORNALE DEGLI EVENTI
					 */
					try {
						Date dataOraEvento = new Date();
						String identificativoDominio = header.getIdentificativoDominio();
						String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
						String codiceContestoPagamento = header.getCodiceContestoPagamento();
						String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
								.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
						String tipoVersamento = null;
						String componente = Constants.COMPONENTE.FESP.toString();
						String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
						String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
						String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
						String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

						String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
						String identificativoStazioneIntermediarioPa = header
								.getIdentificativoStazioneIntermediarioPA();
						String canalePagamento = "";

						String xmlString = "";
						try {
							gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory();
							JAXBContext context = JAXBContext.newInstance(PaaInviaRTRisposta.class);
							Marshaller marshaller = context.createMarshaller();
							StringWriter sw = new StringWriter();
							marshaller.marshal(objectFactory.createPaaInviaRTRisposta(_rispostaRT), sw);
							xmlString = sw.toString();
						} catch (JAXBException jaxbex) {
							LOG.error("Errore deserializzazione RTRisposta");
						}

						String parametriSpecificiInterfaccia = xmlString;

						String esitoReq = _rispostaRT.getPaaInviaRTRisposta().getEsito();

						giornaleService.registraEvento(dataOraEvento, identificativoDominio,
								identificativoUnivocoVersamento, codiceContestoPagamento,
								identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
								tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
								identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
								esitoReq);
					} catch (Exception ex1) {
						LOG.warn("paaInviaRT RESPONSE impossibile inserire nel giornale degli eventi", ex1);
					}

					return _rispostaRT;
				}
			} else if (mygovRptRt.getCodRtInviartEsito() != null && (mygovRptRt.getCodRtInviartEsito()
					.equals(Constants.NODO_REGIONALE_FESP_ESITO_OK)
					|| (mygovRptRt.getCodRtInviartEsito().equals(Constants.NODO_REGIONALE_FESP_ESITO_KO)
							&& mygovRptRt.getCodRtInviartFaultCode().equals(FaultCodeConstants.PAA_RT_DUPLICATA)))
					&&
					! esisteRevoca) {
				// rt gia ricevuta, errore

				String messageError = "[RT gia ricevuta per IUV: " + header.getIdentificativoUnivocoVersamento() + "]";

				LOG.error(FaultCodeConstants.PAA_RT_DUPLICATA + ": " + messageError);

				fillAndSaveRispostaRT_Error(_rispostaRT, FaultCodeConstants.PAA_RT_DUPLICATA, messageError,
						mygovRptRt.getMygovRptRtId());

				/*
				 * LOG GIORNALE DEGLI EVENTI
				 */
				try {
					Date dataOraEvento = new Date();
					String identificativoDominio = header.getIdentificativoDominio();
					String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
					String codiceContestoPagamento = header.getCodiceContestoPagamento();
					String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
							.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
					String tipoVersamento = null;
					String componente = Constants.COMPONENTE.FESP.toString();
					String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
					String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
					String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
					String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

					String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
					String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
					String canalePagamento = "";

					String xmlString = "";
					try {
						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory();
						JAXBContext context = JAXBContext.newInstance(PaaInviaRTRisposta.class);
						Marshaller marshaller = context.createMarshaller();
						StringWriter sw = new StringWriter();
						marshaller.marshal(objectFactory.createPaaInviaRTRisposta(_rispostaRT), sw);
						xmlString = sw.toString();
					} catch (JAXBException jaxbex) {
						LOG.error("Errore deserializzazione RTRisposta");
					}

					String parametriSpecificiInterfaccia = xmlString;

					String esitoReq = _rispostaRT.getPaaInviaRTRisposta().getEsito();

					giornaleService.registraEvento(dataOraEvento, identificativoDominio,
							identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
							tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
							esitoReq);
				} catch (Exception ex1) {
					LOG.warn("paaInviaRT RESPONSE impossibile inserire nel giornale degli eventi", ex1);
				}

				return _rispostaRT;
			}

			LOG.debug("persisto RT per dominio [" + header.getIdentificativoDominio() + "] e IUV ["
					+ header.getIdentificativoUnivocoVersamento() + "]");

			saveRt(mygovRptRt.getMygovRptRtId(), header, bodyrichiesta, rt, rtPayloadInviatoOriginale);
			
			// Se esito 0 o 2 deve essere presente la busta DatiSingoloPagamento
			if (("0".equals(esitoRT) || "2".equals(esitoRT))
					&& (rt.getDatiPagamento().getDatiSingoloPagamentoArray().length == 0)) {
				// rt con esito 0 o 2 ma con busta DatiSingoloPagamento non
				// presente

				String messageError = "[RT con esito [" + esitoRT + "] non contiene DatiSingoloPagamento";

				LOG.error(FaultCodeConstants.PAA_RT_NON_VALIDA + ": " + messageError);

				fillAndSaveRispostaRT_Error(_rispostaRT, FaultCodeConstants.PAA_RT_NON_VALIDA, messageError,
						mygovRptRt.getMygovRptRtId());

				/*
				 * LOG GIORNALE DEGLI EVENTI
				 */
				try {
					Date dataOraEvento = new Date();
					String identificativoDominio = header.getIdentificativoDominio();
					String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
					String codiceContestoPagamento = header.getCodiceContestoPagamento();
					String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
							.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
					String tipoVersamento = null;
					String componente = Constants.COMPONENTE.FESP.toString();
					String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
					String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
					String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
					String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

					String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
					String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
					String canalePagamento = "";

					String xmlString = "";
					try {
						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory();
						JAXBContext context = JAXBContext.newInstance(PaaInviaRTRisposta.class);
						Marshaller marshaller = context.createMarshaller();
						StringWriter sw = new StringWriter();
						marshaller.marshal(objectFactory.createPaaInviaRTRisposta(_rispostaRT), sw);
						xmlString = sw.toString();
					} catch (JAXBException jaxbex) {
						LOG.error("Errore deserializzazione RTRisposta");
					}

					String parametriSpecificiInterfaccia = xmlString;

					String esitoReq = _rispostaRT.getPaaInviaRTRisposta().getEsito();

					giornaleService.registraEvento(dataOraEvento, identificativoDominio,
							identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
							tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
							esitoReq);
				} catch (Exception ex1) {
					LOG.warn("paaInviaRT RESPONSE impossibile inserire nel giornale degli eventi", ex1);
				}

				throw new Exception(messageError);
			}

			// Controlla che il numero di pagamenti sia coerente con rpt solo se
			// busta esiste
			if (rt.getDatiPagamento().getDatiSingoloPagamentoArray().length != 0) {

				List<CtDatiSingoloPagamentoRT> dovutiInRT = Arrays
						.asList(rt.getDatiPagamento().getDatiSingoloPagamentoArray());
				List<MygovRptRtDettaglio> dovutiInRPT = this.manageRPTRTService.getByRptRt(mygovRptRt);
				if (dovutiInRT.size() != dovutiInRPT.size()) {

					String messageError = "[RT ricevuta per IUV: " + header.getIdentificativoUnivocoVersamento()
							+ " Riferimento Messaggio Richiesta: " + rt.getRiferimentoMessaggioRichiesta()
							+ " contiene (" + dovutiInRT.size()
							+ ") singoli pagamenti mentre l'RPT associata ne contiene (" + dovutiInRPT.size() + ")]";

					LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": " + messageError);

					fillAndSaveRispostaRT_Error(_rispostaRT, FaultCodeConstants.PAA_SYSTEM_ERROR, messageError,
							mygovRptRt.getMygovRptRtId());

					/*
					 * LOG GIORNALE DEGLI EVENTI
					 */
					try {
						Date dataOraEvento = new Date();
						String identificativoDominio = header.getIdentificativoDominio();
						String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
						String codiceContestoPagamento = header.getCodiceContestoPagamento();
						String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
								.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
						String tipoVersamento = null;
						String componente = Constants.COMPONENTE.FESP.toString();
						String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
						String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
						String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
						String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

						String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
						String identificativoStazioneIntermediarioPa = header
								.getIdentificativoStazioneIntermediarioPA();
						String canalePagamento = "";

						String xmlString = "";
						try {
							gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory();
							JAXBContext context = JAXBContext.newInstance(PaaInviaRTRisposta.class);
							Marshaller marshaller = context.createMarshaller();
							StringWriter sw = new StringWriter();
							marshaller.marshal(objectFactory.createPaaInviaRTRisposta(_rispostaRT), sw);
							xmlString = sw.toString();
						} catch (JAXBException jaxbex) {
							LOG.error("Errore deserializzazione RTRisposta");
						}

						String parametriSpecificiInterfaccia = xmlString;

						String esitoReq = _rispostaRT.getPaaInviaRTRisposta().getEsito();

						giornaleService.registraEvento(dataOraEvento, identificativoDominio,
								identificativoUnivocoVersamento, codiceContestoPagamento,
								identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
								tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
								identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
								esitoReq);
					} catch (Exception ex1) {
						LOG.warn("paaInviaRT RESPONSE impossibile inserire nel giornale degli eventi", ex1);
					}

					throw new Exception(messageError);
				}
			}			
		} catch (Exception ex) {
			LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
			/*
			 * LOG GIORNALE DEGLI EVENTI
			 */
			try {
				Date dataOraEvento = new Date();
				String identificativoDominio = header.getIdentificativoDominio();
				String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
				String codiceContestoPagamento = header.getCodiceContestoPagamento();
				String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
						.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
				String tipoVersamento = null;
				String componente = Constants.COMPONENTE.FESP.toString();
				String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
				String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
				String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

				String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
				String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
				String canalePagamento = "";

				String xmlString = "";
				
				String parametriSpecificiInterfaccia = xmlString;

				String esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
			} catch (Exception ex1) {
				LOG.warn("paaInviaRT RESPONSE impossibile inserire nel giornale degli eventi", ex1);
			}
			
			// Alzo eccezione al fine di causare un re-inoltro
			throw new RuntimeException(ex);
		}
		try {
			String esitoRT = rt.getDatiPagamento().getCodiceEsitoPagamento().toString();
			// se errato, loggo controllo uguaglianza tra i campi di rt con quelli nviati nella rp
			boolean rptRtValida = this.manageRPTRTService.validaUguaglianzaCampiRptRt(rt);
			if (!rptRtValida) {
				String messageError = "[RT ricevuta per IUV: " + header.getIdentificativoUnivocoVersamento()
						+ " contiene dati non corrispondenti all RPT]";
				LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": " + messageError);
			}

			LOG.debug("costruisco esito per dominio [" + header.getIdentificativoDominio() + "] e IUV ["
					+ header.getIdentificativoUnivocoVersamento() + "]");

			// COSTRUISCI ESITO
			ctEsito = buildEsito(rt);
			it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT _paaSILInviaEsito_header = buildHeaderEsito(header);
			PaaSILInviaEsito _paaSILInviaEsito_bodyrichiesta = buildBodyEsito(ctEsito, tipoFirmaRicevuto,
					rtPayloadInviatoOriginale);

			LOG.debug("persisto esito per dominio [" + header.getIdentificativoDominio() + "] e IUV ["
					+ header.getIdentificativoUnivocoVersamento() + "]");

			// SALVA DB ESITO (2)
			saveE(_paaSILInviaEsito_header, ctEsito, mygovRptRt.getMygovRpEId());
			
			// chiamo PA
			// blocchiamo su FESP le RT del modello tre in stato positivo per velocizzare il modello 1
			// e risponder piu velocemente al nodo 3/9/19
			// le RT che blocchiamo verranno recuperate del batch chiediCopiaEsito di PA
			if ((!precedenteAbortita && !immediatoKO && !modelloTreOK) ||  esisteRevoca) {

				LOG.debug("invoco paaSILInviaEsito per dominio [" + header.getIdentificativoDominio() + "] e IUV ["
						+ header.getIdentificativoUnivocoVersamento() + "]");

				/*
				 * LOG GIORNALE DEGLI EVENTI
				 */
				try {
					Date dataOraEvento = new Date();
					String identificativoDominio = header.getIdentificativoDominio();
					String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
					String codiceContestoPagamento = header.getCodiceContestoPagamento();
					String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
							.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
					String tipoVersamento = null;
					String componente = Constants.COMPONENTE.FESP.toString();
					String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
					String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaSILInviaEsito.toString();
					String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
					String identificativoFruitore = header.getIdentificativoStazioneIntermediarioPA();
					String identificativoErogatore = header.getIdentificativoDominio();
					String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
					String canalePagamento = "";

					String xmlString = "";
					try {
						it.veneto.regione.pagamenti.pa.papernodoregionale.ObjectFactory objectFactory = new it.veneto.regione.pagamenti.pa.papernodoregionale.ObjectFactory();
						JAXBContext context = JAXBContext.newInstance(PaaSILInviaEsito.class);
						Marshaller marshaller = context.createMarshaller();
						StringWriter sw = new StringWriter();
						marshaller.marshal(objectFactory.createPaaSILInviaEsito(_paaSILInviaEsito_bodyrichiesta), sw);
						xmlString = sw.toString();
					} catch (JAXBException jaxbex) {
						LOG.error("Errore deserializzazione Esito");
					}

					String parametriSpecificiInterfaccia = xmlString;

					String esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

					giornaleService.registraEvento(dataOraEvento, identificativoDominio,
							identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
							tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
							esitoReq);
				} catch (Exception ex) {
					LOG.warn("paaSILInviaEsito REQUEST impossibile inserire nel giornale degli eventi", ex);
				}

				// INVIA ESITO --->> PA
				try{
					_rispostaEsito = this.pagamentiTelematiciEsitoServiceClient
							.paaSILInviaEsito(_paaSILInviaEsito_bodyrichiesta, _paaSILInviaEsito_header);
				} catch (Exception ex) {
					// se la chiamata a PA solleva eccezione torno OK al nodo in modo tale che il nodo
					// non reinoltri piu'. L' esito sara' poi recuperato da BatchChiediCopiaEsito

					_rispostaEsito = new PaaSILInviaEsitoRisposta();
					EsitoPaaSILInviaEsito esitoPaaSILInviaEsito = new EsitoPaaSILInviaEsito();
					esitoPaaSILInviaEsito.setEsito("KO");
					it.veneto.regione.pagamenti.pa.papernodoregionale.FaultBean faultBean = new it.veneto.regione.pagamenti.pa.papernodoregionale.FaultBean();
					faultBean.setFaultCode(FaultCodeConstants.PAA_NOT_INVOKED);
					faultBean.setFaultString("PA non invocato");
					faultBean.setDescription("eccezione in chiamata paaSILInviaEsito]");
					esitoPaaSILInviaEsito.setFault(faultBean);
					_rispostaEsito.setPaaSILInviaEsitoRisposta(esitoPaaSILInviaEsito);
				}
				/*
				 * LOG GIORNALE DEGLI EVENTI RISPOSTA DI PA
				 */
				try {
					Date dataOraEvento = new Date();
					String identificativoDominio = header.getIdentificativoDominio();
					String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
					String codiceContestoPagamento = header.getCodiceContestoPagamento();
					String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
							.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
					String tipoVersamento = null;
					String componente = Constants.COMPONENTE.FESP.toString();
					String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
					String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaSILInviaEsito.toString();
					String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
					String identificativoFruitore = header.getIdentificativoStazioneIntermediarioPA();
					String identificativoErogatore = header.getIdentificativoDominio();
					String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
					String canalePagamento = "";

					String xmlString = "";
					try {
						it.veneto.regione.pagamenti.pa.papernodoregionale.ObjectFactory objectFactory = new it.veneto.regione.pagamenti.pa.papernodoregionale.ObjectFactory();
						JAXBContext context = JAXBContext.newInstance(PaaSILInviaEsitoRisposta.class);
						Marshaller marshaller = context.createMarshaller();
						StringWriter sw = new StringWriter();
						marshaller.marshal(objectFactory.createPaaSILInviaEsitoRisposta(_rispostaEsito), sw);
						xmlString = sw.toString();
					} catch (JAXBException jaxbex) {
						LOG.error("Errore deserializzazione EsitoRisposta");
					}

					String parametriSpecificiInterfaccia = xmlString;

					String esitoReq = _rispostaEsito.getPaaSILInviaEsitoRisposta().getEsito();

					giornaleService.registraEvento(dataOraEvento, identificativoDominio,
							identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
							tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
							esitoReq);
				} catch (Exception ex) {
					LOG.warn("paaSILInviaEsito RESPONSE impossibile inserire nel giornale degli eventi", ex);
				}

				if (!_rispostaEsito.getPaaSILInviaEsitoRisposta().getEsito().equals("OK"))
					LOG.debug("Ricevuto errore in rispostaInvioEsito:"
							+ _rispostaEsito.getPaaSILInviaEsitoRisposta().getFault().getDescription());
			}
			// NON chiamo PA
			else {
				if ("0".equals(esitoRT) || "2".equals(esitoRT)) {
					String error = "Ricevuto esito pagamento positivo con precedenteAbortita [" + precedenteAbortita
					+ "], immediatoKO [" + immediatoKO + "], modelloTreOK [" + modelloTreOK + "], esitoRT [" + esitoRT + "]";
					if(modelloTreOK)
						LOG.debug(error);
					else
						LOG.error(error);					
				}
				LOG.debug("non invoco paaSILInviaEsito per dominio [" + header.getIdentificativoDominio() + "] e IUV ["
						+ header.getIdentificativoUnivocoVersamento() + "]");

				_rispostaEsito = new PaaSILInviaEsitoRisposta();
				EsitoPaaSILInviaEsito esitoPaaSILInviaEsito = new EsitoPaaSILInviaEsito();
				esitoPaaSILInviaEsito.setEsito("KO");
				it.veneto.regione.pagamenti.pa.papernodoregionale.FaultBean faultBean = new it.veneto.regione.pagamenti.pa.papernodoregionale.FaultBean();
				faultBean.setFaultCode(FaultCodeConstants.PAA_NOT_INVOKED);
				faultBean.setFaultString("PA non invocato");
				faultBean.setDescription("RT precedenteAbortita [" + precedenteAbortita + "], immediatoKO ["
						+ immediatoKO + "], modelloTreOK [" + modelloTreOK + "], esitoRT [" + esitoRT + "]");
				esitoPaaSILInviaEsito.setFault(faultBean);
				_rispostaEsito.setPaaSILInviaEsitoRisposta(esitoPaaSILInviaEsito);
			}
		} catch (java.lang.Exception ex) {
			LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);

			fillAndSaveRispostaRT_Error(_rispostaRT, FaultCodeConstants.PAA_SYSTEM_ERROR,
					ex.getClass().getName() + (ex.getMessage() != null ? ": " + ex.getMessage() : ""),
					(mygovRptRt != null ? mygovRptRt.getMygovRptRtId(): null));

			/*
			 * LOG GIORNALE DEGLI EVENTI
			 */
			try {
				Date dataOraEvento = new Date();
				String identificativoDominio = header.getIdentificativoDominio();
				String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
				String codiceContestoPagamento = header.getCodiceContestoPagamento();
				String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
						.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
				String tipoVersamento = null;
				String componente = Constants.COMPONENTE.FESP.toString();
				String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
				String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
				String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

				String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
				String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
				String canalePagamento = "";

				String xmlString = "";
				try {
					gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory();
					JAXBContext context = JAXBContext.newInstance(PaaInviaRTRisposta.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createPaaInviaRTRisposta(_rispostaRT), sw);
					xmlString = sw.toString();
				} catch (JAXBException jaxbex) {
					LOG.error("Errore deserializzazione RTRisposta");
				}

				String parametriSpecificiInterfaccia = xmlString;

				String esitoReq = _rispostaRT.getPaaInviaRTRisposta().getEsito();

				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
			} catch (Exception ex1) {
				LOG.warn("paaInviaRT RESPONSE impossibile inserire nel giornale degli eventi", ex1);
			}

			return _rispostaRT;
		}

		try {

			saveEsitoRisposta(_rispostaEsito, mygovRptRt.getMygovRpEId());

			// COSTRUISCI RISPOSTA RT SEMPRE OK
			EsitoPaaInviaRT rt_esitoPaaInviaRT = new EsitoPaaInviaRT();
			rt_esitoPaaInviaRT.setEsito("OK");
			_rispostaRT.setPaaInviaRTRisposta(rt_esitoPaaInviaRT);

			// SALVA DB RISPOSTA RT PSP NAZIONALE rt_ack (4)

			saveRtRisposta(_rispostaRT, mygovRptRt.getMygovRptRtId());
		} catch (Exception e) {
			LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [" + e.getMessage() + "]", e);

			fillAndSaveRispostaRT_Error(_rispostaRT, FaultCodeConstants.PAA_SYSTEM_ERROR,
					e.getClass().getName() + (e.getMessage() != null ? ": " + e.getMessage() : ""),
					mygovRptRt.getMygovRptRtId());
		}

		/*
		 * LOG GIORNALE DEGLI EVENTI
		 */
		try {
			Date dataOraEvento = new Date();
			String identificativoDominio = header.getIdentificativoDominio();
			String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
			String codiceContestoPagamento = header.getCodiceContestoPagamento();
			String identificativoPrestatoreServiziPagamento = rt.getIstitutoAttestante()
					.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
			String tipoVersamento = null;
			String componente = Constants.COMPONENTE.FESP.toString();
			String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRT.toString();
			String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
			String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

			String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
			String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
			String canalePagamento = "";

			String xmlString = "";
			try {
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ObjectFactory();
				JAXBContext context = JAXBContext.newInstance(PaaInviaRTRisposta.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createPaaInviaRTRisposta(_rispostaRT), sw);
				xmlString = sw.toString();
			} catch (JAXBException jaxbex) {
				LOG.error("Errore deserializzazione RTRisposta");
			}

			String parametriSpecificiInterfaccia = xmlString;

			String esitoReq = _rispostaRT.getPaaInviaRTRisposta().getEsito();

			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
		} catch (Exception ex) {
			LOG.warn("paaInviaRT RESPONSE impossibile inserire nel giornale degli eventi", ex);
		}

		return _rispostaRT;
	}

	/**
	 * @param rtPayloadInviatoOriginale
	 * @param tipoFirmaRicevuto
	 * @param trackingInfo
	 * @return
	 * @throws FirmaNotValidException
	 */
	private byte[] decodificaRt(byte[] rtPayloadInviatoOriginale, String tipoFirmaRicevuto, String trackingInfo)
			throws FirmaNotValidException {

		byte[] byteRtRicevutoDecodificato = null;

		// Se non e' vuoto e non e' "0" per noi significa che e' firmato
		boolean isRtFirmata = StringUtils.isNotBlank(tipoFirmaRicevuto)
				&& !StFirmaRicevuta.Enum.forInt(StFirmaRicevuta.INT_X_0).toString().equalsIgnoreCase(tipoFirmaRicevuto);

		// INNESTO PER VERIFICA FIRMA
		byte[] byteRtInChiaro = null;

		// byteRtRicevutoDecodificato =
		// Base64.decodeBase64(rtPayloadInviatoOriginale);
		byteRtRicevutoDecodificato = rtPayloadInviatoOriginale;

		if (isRtFirmata) {
			byteRtInChiaro = this.firmaService.verify(byteRtRicevutoDecodificato, trackingInfo);
		} else {
			byteRtInChiaro = byteRtRicevutoDecodificato;
		}

		// try {
		// LOG.debug("rtPayloadInviatoOriginale: " + new
		// String(rtPayloadInviatoOriginale, "UTF-8"));
		// LOG.debug("byteRtRicevutoDecodificato: " + new
		// String(byteRtRicevutoDecodificato, "UTF-8"));
		// LOG.debug("byteRtInChiaro: " + new String(byteRtInChiaro, "UTF-8"));
		// } catch (Exception ex) {
		// }

		return byteRtInChiaro;
	}

	/**
	 * @param mygovRptRtId
	 * @param header
	 * @param bodyrichiesta
	 * @param rt
	 * @param rtPayload
	 */
	private void saveRt(Long mygovRptRtId, IntestazionePPT header, PaaInviaRT bodyrichiesta, CtRicevutaTelematica rt,
			byte[] rtPayload) {

		List<RptRtDettaglioDto> rtPagamenti = new ArrayList<RptRtDettaglioDto>();

		if (rt.getDatiPagamento().getDatiSingoloPagamentoArray().length != 0) {

			CtDatiSingoloPagamentoRT[] pagamenti = rt.getDatiPagamento().getDatiSingoloPagamentoArray();

			for (int i = 0; i < pagamenti.length; i++) {
				CtDatiSingoloPagamentoRT singoloPagamento = pagamenti[i];
				RptRtDettaglioDto rtPagamento = new RptRtDettaglioDto();

				rtPagamento.setNumRtDatiPagDatiSingPagSingoloImportoPagato(singoloPagamento.getSingoloImportoPagato());
				rtPagamento.setDeRtDatiPagDatiSingPagEsitoSingoloPagamento(singoloPagamento.getEsitoSingoloPagamento());
				rtPagamento.setDtRtDatiPagDatiSingPagDataEsitoSingoloPagamento(
						singoloPagamento.getDataEsitoSingoloPagamento().getTime());
				rtPagamento.setCodRtDatiPagDatiSingPagIdUnivocoRiscossione(
						singoloPagamento.getIdentificativoUnivocoRiscossione());
				rtPagamento.setDeRtDatiPagDatiSingPagCausaleVersamento(singoloPagamento.getCausaleVersamento());
				rtPagamento.setDeRtDatiPagDatiSingPagDatiSpecificiRiscossione(
						singoloPagamento.getDatiSpecificiRiscossione());

				rtPagamento.setNumRtDatiPagDatiSingPagCommissioniApplicatePsp(
						singoloPagamento.getCommissioniApplicatePSP());
				if (singoloPagamento.getAllegatoRicevuta() != null) {
					rtPagamento.setCodRtDatiPagDatiSingPagAllegatoRicevutaTipo(
							singoloPagamento.getAllegatoRicevuta().getTipoAllegatoRicevuta().toString());
					rtPagamento.setBlbRtDatiPagDatiSingPagAllegatoRicevutaTest(
							singoloPagamento.getAllegatoRicevuta().getTestoAllegato());
				}
				rtPagamenti.add(rtPagamento);
			}
		}

		String deRtInviartTipoFirma = bodyrichiesta.getTipoFirma();
		String codRtInviartIdIntermediarioPa = header.getIdentificativoIntermediarioPA();
		String codRtInviartIdStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
		String codRtInviartIdDominio = header.getIdentificativoDominio();
		String codRtInviartIdUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
		String codRtInviartCodiceContestoPagamento = header.getCodiceContestoPagamento();
		String deRtVersioneOggetto = rt.getVersioneOggetto();
		String codRtDomIdDominio = rt.getDominio().getIdentificativoDominio();
		String codRtDomIdStazioneRichiedente = rt.getDominio().getIdentificativoStazioneRichiedente();
		String codRtIdMessaggioRicevuta = rt.getIdentificativoMessaggioRicevuta();
		Date dtRtDataOraMessaggioRicevuta = rt.getDataOraMessaggioRicevuta().getTime();
		String codRtRiferimentoMessaggioRichiesta = rt.getRiferimentoMessaggioRichiesta();
		Date dtRtRiferimentoDataRichiesta = rt.getRiferimentoDataRichiesta().getTime();
		String codRtIstitAttesIdUnivAttesTipoIdUnivoco = rt.getIstitutoAttestante().getIdentificativoUnivocoAttestante()
				.getTipoIdentificativoUnivoco().toString();
		String codRtIstitAttesIdUnivAttesCodiceIdUnivoco = rt.getIstitutoAttestante()
				.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
		String deRtIstitAttesDenominazioneAttestante = rt.getIstitutoAttestante().getDenominazioneAttestante();
		String codRtIstitAttesCodiceUnitOperAttestante = rt.getIstitutoAttestante().getCodiceUnitOperAttestante();
		String deRtIstitAttesDenomUnitOperAttestante = rt.getIstitutoAttestante().getDenomUnitOperAttestante();
		String deRtIstitAttesIndirizzoAttestante = rt.getIstitutoAttestante().getIndirizzoAttestante();
		String deRtIstitAttesCivicoAttestante = rt.getIstitutoAttestante().getCivicoAttestante();
		String codRtIstitAttesCapAttestante = rt.getIstitutoAttestante().getCapAttestante();
		String deRtIstitAttesLocalitaAttestante = rt.getIstitutoAttestante().getLocalitaAttestante();
		String deRtIstitAttesProvinciaAttestante = rt.getIstitutoAttestante().getProvinciaAttestante();
		String codRtIstitAttesNazioneAttestante = rt.getIstitutoAttestante().getNazioneAttestante();
		String codRtEnteBenefIdUnivBenefTipoIdUnivoco = rt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario()
				.getTipoIdentificativoUnivoco().toString();
		String codRtEnteBenefIdUnivBenefCodiceIdUnivoco = rt.getEnteBeneficiario()
				.getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco();
		String deRtEnteBenefDenominazioneBeneficiario = rt.getEnteBeneficiario().getDenominazioneBeneficiario();
		String codRtEnteBenefCodiceUnitOperBeneficiario = rt.getEnteBeneficiario().getCodiceUnitOperBeneficiario();
		String deRtEnteBenefDenomUnitOperBeneficiario = rt.getEnteBeneficiario().getDenomUnitOperBeneficiario();
		String deRtEnteBenefIndirizzoBeneficiario = rt.getEnteBeneficiario().getIndirizzoBeneficiario();
		String deRtEnteBenefCivicoBeneficiario = rt.getEnteBeneficiario().getCivicoBeneficiario();
		String codRtEnteBenefCapBeneficiario = rt.getEnteBeneficiario().getCapBeneficiario();
		String deRtEnteBenefLocalitaBeneficiario = rt.getEnteBeneficiario().getLocalitaBeneficiario();
		String deRtEnteBenefProvinciaBeneficiario = rt.getEnteBeneficiario().getProvinciaBeneficiario();
		String codRtEnteBenefNazioneBeneficiario = rt.getEnteBeneficiario().getNazioneBeneficiario();

		String codRtSoggVersIdUnivVersTipoIdUnivoco = null;
		String codRtSoggVersIdUnivVersCodiceIdUnivocoString = null;
		String deRtSoggVersAnagraficaVersante = null;
		String deRtSoggVersIndirizzoVersante = null;
		String deRtSoggVersCivicoVersante = null;
		String codRtSoggVersCapVersante = null;
		String deRtSoggVersLocalitaVersante = null;
		String deRtSoggVersProvinciaVersante = null;
		String codRtSoggVersNazioneVersante = null;
		String deRtSoggVersEmailVersante = null;

		if (rt.getSoggettoVersante() != null) {
			codRtSoggVersIdUnivVersTipoIdUnivoco = rt.getSoggettoVersante().getIdentificativoUnivocoVersante()
					.getTipoIdentificativoUnivoco().toString();
			codRtSoggVersIdUnivVersCodiceIdUnivocoString = rt.getSoggettoVersante().getIdentificativoUnivocoVersante()
					.getCodiceIdentificativoUnivoco();
			deRtSoggVersAnagraficaVersante = rt.getSoggettoVersante().getAnagraficaVersante();
			deRtSoggVersIndirizzoVersante = rt.getSoggettoVersante().getIndirizzoVersante();
			deRtSoggVersCivicoVersante = rt.getSoggettoVersante().getCivicoVersante();
			codRtSoggVersCapVersante = rt.getSoggettoVersante().getCapVersante();
			deRtSoggVersLocalitaVersante = rt.getSoggettoVersante().getLocalitaVersante();
			deRtSoggVersProvinciaVersante = rt.getSoggettoVersante().getProvinciaVersante();
			codRtSoggVersNazioneVersante = rt.getSoggettoVersante().getNazioneVersante();
			deRtSoggVersEmailVersante = rt.getSoggettoVersante().getEMailVersante();
		}

		String codRtSoggPagIdUnivPagTipoIdUnivoco = rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
				.getTipoIdentificativoUnivoco().toString();
		String codRtSoggPagIdUnivPagCodiceIdUnivoco = rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
				.getCodiceIdentificativoUnivoco();
		String deRtSoggPagAnagraficaPagatore = rt.getSoggettoPagatore().getAnagraficaPagatore();
		String deRtSoggPagIndirizzoPagatore = rt.getSoggettoPagatore().getIndirizzoPagatore();
		String deRtSoggPagCivicoPagatore = rt.getSoggettoPagatore().getCivicoPagatore();
		String codRtSoggPagCapPagatore = rt.getSoggettoPagatore().getCapPagatore();
		String deRtSoggPagLocalitaPagatore = rt.getSoggettoPagatore().getLocalitaPagatore();
		String deRtSoggPagProvinciaPagatore = rt.getSoggettoPagatore().getProvinciaPagatore();
		String codRtSoggPagNazionePagatore = rt.getSoggettoPagatore().getNazionePagatore();
		String deRtSoggPagEmailPagatore = rt.getSoggettoPagatore().getEMailPagatore();

		String codRtDatiPagCodiceEsitoPagamento = rt.getDatiPagamento().getCodiceEsitoPagamento().toString();

		BigDecimal numRtDatiPagImportoTotalePagato = rt.getDatiPagamento().getImportoTotalePagato();
		String codRtDatiPagIdUnivocoVersamento = rt.getDatiPagamento().getIdentificativoUnivocoVersamento();
		String codRtDatiPagCodiceContestoPagamento = rt.getDatiPagamento().getCodiceContestoPagamento();

		/* dell'rpt non ho l'ente.. uso quello dell'rt */
		this.manageRPTRTService.updateRtById(

				mygovRptRtId, deRtInviartTipoFirma, codRtInviartIdIntermediarioPa,
				codRtInviartIdStazioneIntermediarioPa, codRtInviartIdDominio, codRtInviartIdUnivocoVersamento,
				codRtInviartCodiceContestoPagamento, deRtVersioneOggetto,

				codRtDomIdDominio, codRtDomIdStazioneRichiedente,

				codRtIdMessaggioRicevuta, dtRtDataOraMessaggioRicevuta, codRtRiferimentoMessaggioRichiesta,
				dtRtRiferimentoDataRichiesta,

				codRtIstitAttesIdUnivAttesTipoIdUnivoco, codRtIstitAttesIdUnivAttesCodiceIdUnivoco,
				deRtIstitAttesDenominazioneAttestante, codRtIstitAttesCodiceUnitOperAttestante,
				deRtIstitAttesDenomUnitOperAttestante, deRtIstitAttesIndirizzoAttestante,
				deRtIstitAttesCivicoAttestante, codRtIstitAttesCapAttestante, deRtIstitAttesLocalitaAttestante,
				deRtIstitAttesProvinciaAttestante, codRtIstitAttesNazioneAttestante,
				codRtEnteBenefIdUnivBenefTipoIdUnivoco, codRtEnteBenefIdUnivBenefCodiceIdUnivoco,
				deRtEnteBenefDenominazioneBeneficiario, codRtEnteBenefCodiceUnitOperBeneficiario,
				deRtEnteBenefDenomUnitOperBeneficiario, deRtEnteBenefIndirizzoBeneficiario,
				deRtEnteBenefCivicoBeneficiario, codRtEnteBenefCapBeneficiario, deRtEnteBenefLocalitaBeneficiario,
				deRtEnteBenefProvinciaBeneficiario, codRtEnteBenefNazioneBeneficiario,
				codRtSoggVersIdUnivVersTipoIdUnivoco, codRtSoggVersIdUnivVersCodiceIdUnivocoString,
				deRtSoggVersAnagraficaVersante, deRtSoggVersIndirizzoVersante, deRtSoggVersCivicoVersante,
				codRtSoggVersCapVersante, deRtSoggVersLocalitaVersante, deRtSoggVersProvinciaVersante,
				codRtSoggVersNazioneVersante, deRtSoggVersEmailVersante,

				codRtSoggPagIdUnivPagTipoIdUnivoco, codRtSoggPagIdUnivPagCodiceIdUnivoco, deRtSoggPagAnagraficaPagatore,
				deRtSoggPagIndirizzoPagatore, deRtSoggPagCivicoPagatore, codRtSoggPagCapPagatore,
				deRtSoggPagLocalitaPagatore, deRtSoggPagProvinciaPagatore, codRtSoggPagNazionePagatore,
				deRtSoggPagEmailPagatore, codRtDatiPagCodiceEsitoPagamento, numRtDatiPagImportoTotalePagato,
				codRtDatiPagIdUnivocoVersamento, codRtDatiPagCodiceContestoPagamento, rtPagamenti, rtPayload);
	}

	/**
	 * @param _rispostaEsito
	 * @param mygovRpEId
	 */
	private void saveEsitoRisposta(PaaSILInviaEsitoRisposta _rispostaEsito, Long mygovRpEId) {

		EsitoPaaSILInviaEsito esitoRisposta = _rispostaEsito.getPaaSILInviaEsitoRisposta();
		it.veneto.regione.pagamenti.pa.papernodoregionale.FaultBean esitoFault = esitoRisposta.getFault();

		this.manageRPEService.updateRispostaEById(mygovRpEId, Constants.ACK_STRING, esitoRisposta.getEsito(),
				(esitoFault != null ? esitoFault.getFaultCode() : null),
				(esitoFault != null ? esitoFault.getFaultString() : null),
				(esitoFault != null ? esitoFault.getId() : null),
				(esitoFault != null ? esitoFault.getDescription() : null),
				(esitoFault != null ? esitoFault.getSerial() : null),
				(esitoFault != null ? esitoFault.getOriginalFaultCode() : null),
				(esitoFault != null ? esitoFault.getOriginalFaultString() : null),
				(esitoFault != null ? esitoFault.getOriginalDescription() : null)
				);
	}

	/**
	 * @param _rispostaRT
	 * @param mygovRptRtId
	 */
	private void saveRtRisposta(PaaInviaRTRisposta _rispostaRT, Long mygovRptRtId) {

		EsitoPaaInviaRT rtRisposta = _rispostaRT.getPaaInviaRTRisposta();
		gov.telematici.pagamenti.ws.nodoregionalepernodospc.FaultBean esitoFault = rtRisposta.getFault();

		String description = null;
		String faultCode = null;
		String faultString = null;
		String faultId = null;
		Integer faultSerial = null;
		String originalFaultCode = null;
		String originalFaultString = null;
		String originalFaultdescription = null;

		if (esitoFault != null) {
			faultCode = esitoFault.getFaultCode();
			faultString = esitoFault.getFaultString();
			faultId = esitoFault.getId();
			description = esitoFault.getDescription();
			faultSerial = esitoFault.getSerial();
			originalFaultCode = esitoFault.getOriginalFaultCode();
			originalFaultString = esitoFault.getOriginalFaultString();
			originalFaultdescription = esitoFault.getOriginalDescription();
		}

		this.manageRPTRTService.updateRispostaRtById(mygovRptRtId, Constants.ACK_STRING, rtRisposta.getEsito(),
				faultCode, faultString, faultId, description, faultSerial, originalFaultCode, 
				originalFaultString, originalFaultdescription);
	}

	/**
	 * @param header
	 * @return
	 */
	private it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT buildHeaderEsito(IntestazionePPT header) {

		it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT result = new it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT();

		result.setCodiceContestoPagamento(header.getCodiceContestoPagamento());
		result.setIdentificativoDominio(header.getIdentificativoDominio());
		// result.setIdentificativoIntermediarioPA(header.getIdentificativoIntermediarioPA());
		// result.setIdentificativoStazioneIntermediarioPA(header.getIdentificativoStazioneIntermediarioPA());
		result.setIdentificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento());

		return result;
	}

	/**
	 * @param ctEsito
	 * @return
	 */
	private PaaSILInviaEsito buildBodyEsito(it.veneto.regione.schemas.x2012.pagamenti.CtEsito ctEsito, String tipoFirma,
			byte[] rt) {

		it.veneto.regione.schemas.x2012.pagamenti.EsitoDocument esitoDoc = it.veneto.regione.schemas.x2012.pagamenti.EsitoDocument.Factory
				.newInstance();
		esitoDoc.setEsito(ctEsito);

		byte[] byteEsito = null;

		try {
			byteEsito = Base64.encodeBase64(esitoDoc.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Failed to parse Esito ::: UnsupportedEncodingException :::", uee);
		}

		PaaSILInviaEsito result = new PaaSILInviaEsito();
		result.setEsito(byteEsito);

		result.setTipoFirma(tipoFirma);
		result.setRt(rt);

		return result;
	}

	/**
	 * @param rt
	 * @return
	 */
	private it.veneto.regione.schemas.x2012.pagamenti.CtEsito buildEsito(CtRicevutaTelematica rt) {

		/**
		 * CtEsito
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtEsito ctEsito = it.veneto.regione.schemas.x2012.pagamenti.CtEsito.Factory
				.newInstance();
		ctEsito.setVersioneOggetto(rt.getVersioneOggetto());

		/**
		 * CtDominio
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtDominio dominio = it.veneto.regione.schemas.x2012.pagamenti.CtDominio.Factory
				.newInstance();
		dominio.setIdentificativoDominio(rt.getDominio().getIdentificativoDominio());
		if (rt.getDominio().getIdentificativoStazioneRichiedente() != null) {
			dominio.setIdentificativoStazioneRichiedente(rt.getDominio().getIdentificativoStazioneRichiedente());
		}
		ctEsito.setDominio(dominio);

		ctEsito.setIdentificativoMessaggioRicevuta(rt.getIdentificativoMessaggioRicevuta());
		ctEsito.setDataOraMessaggioRicevuta(rt.getDataOraMessaggioRicevuta());
		ctEsito.setRiferimentoMessaggioRichiesta(rt.getRiferimentoMessaggioRichiesta());
		ctEsito.setRiferimentoDataRichiesta(rt.getRiferimentoDataRichiesta());

		/**
		 * CtIstitutoAttestante
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtIstitutoAttestante istitutoAttestante = it.veneto.regione.schemas.x2012.pagamenti.CtIstitutoAttestante.Factory
				.newInstance();
		it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivoco identificativoUnivocoAttestante = it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivoco.Factory
				.newInstance();
		identificativoUnivocoAttestante
				.setTipoIdentificativoUnivoco(it.veneto.regione.schemas.x2012.pagamenti.StTipoIdentificativoUnivoco.Enum
						.forString(rt.getIstitutoAttestante().getIdentificativoUnivocoAttestante()
								.getTipoIdentificativoUnivoco().toString()));
		identificativoUnivocoAttestante.setCodiceIdentificativoUnivoco(
				rt.getIstitutoAttestante().getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco());
		istitutoAttestante.setIdentificativoUnivocoAttestante(identificativoUnivocoAttestante);

		if (rt.getIstitutoAttestante().getDenominazioneAttestante() != null) {
			istitutoAttestante.setDenominazioneAttestante(rt.getIstitutoAttestante().getDenominazioneAttestante());
		}
		if (rt.getIstitutoAttestante().getCodiceUnitOperAttestante() != null) {
			istitutoAttestante.setCodiceUnitOperAttestante(rt.getIstitutoAttestante().getCodiceUnitOperAttestante());
		}
		if (rt.getIstitutoAttestante().getDenomUnitOperAttestante() != null) {
			istitutoAttestante.setDenomUnitOperAttestante(rt.getIstitutoAttestante().getDenomUnitOperAttestante());
		}
		if (rt.getIstitutoAttestante().getIndirizzoAttestante() != null) {
			istitutoAttestante.setIndirizzoAttestante(rt.getIstitutoAttestante().getIndirizzoAttestante());
		}
		if (rt.getIstitutoAttestante().getCivicoAttestante() != null) {
			istitutoAttestante.setCivicoAttestante(rt.getIstitutoAttestante().getCivicoAttestante());
		}
		if (rt.getIstitutoAttestante().getCapAttestante() != null) {
			istitutoAttestante.setCapAttestante(rt.getIstitutoAttestante().getCapAttestante());
		}
		if (rt.getIstitutoAttestante().getLocalitaAttestante() != null) {
			istitutoAttestante.setLocalitaAttestante(rt.getIstitutoAttestante().getLocalitaAttestante());
		}
		if (rt.getIstitutoAttestante().getProvinciaAttestante() != null) {
			istitutoAttestante.setProvinciaAttestante(rt.getIstitutoAttestante().getProvinciaAttestante());
		}
		if (rt.getIstitutoAttestante().getNazioneAttestante() != null) {
			istitutoAttestante.setNazioneAttestante(rt.getIstitutoAttestante().getNazioneAttestante());
		}
		ctEsito.setIstitutoAttestante(istitutoAttestante);

		/**
		 * CtEnteBeneficiario
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtEnteBeneficiario enteBeneficiario = it.veneto.regione.schemas.x2012.pagamenti.CtEnteBeneficiario.Factory
				.newInstance();
		it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivocoPersonaG identificativoUnivocoBeneficiario = it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivocoPersonaG.Factory
				.newInstance();
		identificativoUnivocoBeneficiario.setTipoIdentificativoUnivoco(
				it.veneto.regione.schemas.x2012.pagamenti.StTipoIdentificativoUnivocoPersG.Enum
						.forString(rt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario()
								.getTipoIdentificativoUnivoco().toString()));
		identificativoUnivocoBeneficiario.setCodiceIdentificativoUnivoco(
				rt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco());
		enteBeneficiario.setIdentificativoUnivocoBeneficiario(identificativoUnivocoBeneficiario);
		enteBeneficiario.setDenominazioneBeneficiario(rt.getEnteBeneficiario().getDenominazioneBeneficiario());

		if (rt.getEnteBeneficiario().getCodiceUnitOperBeneficiario() != null) {
			enteBeneficiario.setCodiceUnitOperBeneficiario(rt.getEnteBeneficiario().getCodiceUnitOperBeneficiario());
		}
		if (rt.getEnteBeneficiario().getDenomUnitOperBeneficiario() != null) {
			enteBeneficiario.setDenomUnitOperBeneficiario(rt.getEnteBeneficiario().getDenomUnitOperBeneficiario());
		}
		if (rt.getEnteBeneficiario().getIndirizzoBeneficiario() != null) {
			enteBeneficiario.setIndirizzoBeneficiario(rt.getEnteBeneficiario().getIndirizzoBeneficiario());
		}
		if (rt.getEnteBeneficiario().getCivicoBeneficiario() != null) {
			enteBeneficiario.setCivicoBeneficiario(rt.getEnteBeneficiario().getCivicoBeneficiario());
		}
		if (rt.getEnteBeneficiario().getCapBeneficiario() != null) {
			enteBeneficiario.setCapBeneficiario(rt.getEnteBeneficiario().getCapBeneficiario());
		}
		if (rt.getEnteBeneficiario().getLocalitaBeneficiario() != null) {
			enteBeneficiario.setLocalitaBeneficiario(rt.getEnteBeneficiario().getLocalitaBeneficiario());
		}
		if (rt.getEnteBeneficiario().getProvinciaBeneficiario() != null) {
			enteBeneficiario.setProvinciaBeneficiario(rt.getEnteBeneficiario().getProvinciaBeneficiario());
		}
		if (rt.getEnteBeneficiario().getNazioneBeneficiario() != null) {
			enteBeneficiario.setNazioneBeneficiario(rt.getEnteBeneficiario().getNazioneBeneficiario());
		}
		ctEsito.setEnteBeneficiario(enteBeneficiario);

		/**
		 * CtSoggettoVersante
		 */

		if (rt.getSoggettoVersante() != null) {
			it.veneto.regione.schemas.x2012.pagamenti.CtSoggettoVersante soggettoVersante = it.veneto.regione.schemas.x2012.pagamenti.CtSoggettoVersante.Factory
					.newInstance();
			it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante = it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivocoPersonaFG.Factory
					.newInstance();
			identificativoUnivocoVersante.setTipoIdentificativoUnivoco(
					it.veneto.regione.schemas.x2012.pagamenti.StTipoIdentificativoUnivocoPersFG.Enum
							.forString(rt.getSoggettoVersante().getIdentificativoUnivocoVersante()
									.getTipoIdentificativoUnivoco().toString()));
			identificativoUnivocoVersante.setCodiceIdentificativoUnivoco(
					rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());
			soggettoVersante.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);
			soggettoVersante.setAnagraficaVersante(rt.getSoggettoVersante().getAnagraficaVersante());

			if (rt.getSoggettoVersante().getIndirizzoVersante() != null) {
				soggettoVersante.setIndirizzoVersante(rt.getSoggettoVersante().getIndirizzoVersante());
			}
			if (rt.getSoggettoVersante().getCivicoVersante() != null) {
				soggettoVersante.setCivicoVersante(rt.getSoggettoVersante().getCivicoVersante());
			}
			if (rt.getSoggettoVersante().getCapVersante() != null) {
				soggettoVersante.setCapVersante(rt.getSoggettoVersante().getCapVersante());
			}
			if (rt.getSoggettoVersante().getLocalitaVersante() != null) {
				soggettoVersante.setLocalitaVersante(rt.getSoggettoVersante().getLocalitaVersante());
			}
			if (rt.getSoggettoVersante().getProvinciaVersante() != null) {
				soggettoVersante.setProvinciaVersante(rt.getSoggettoVersante().getProvinciaVersante());
			}
			if (rt.getSoggettoVersante().getNazioneVersante() != null) {
				soggettoVersante.setNazioneVersante(rt.getSoggettoVersante().getNazioneVersante());
			}
			if (rt.getSoggettoVersante().getEMailVersante() != null) {
				soggettoVersante.setEMailVersante(rt.getSoggettoVersante().getEMailVersante());
			}
			ctEsito.setSoggettoVersante(soggettoVersante);
		}

		/**
		 * CtSoggettoPagatore
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtSoggettoPagatore soggettoPagatore = it.veneto.regione.schemas.x2012.pagamenti.CtSoggettoPagatore.Factory
				.newInstance();
		it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore = it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivocoPersonaFG.Factory
				.newInstance();
		identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(
				it.veneto.regione.schemas.x2012.pagamenti.StTipoIdentificativoUnivocoPersFG.Enum
						.forString(rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
								.getTipoIdentificativoUnivoco().toString()));
		identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(
				rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());
		soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
		soggettoPagatore.setAnagraficaPagatore(rt.getSoggettoPagatore().getAnagraficaPagatore());

		if (rt.getSoggettoPagatore().getIndirizzoPagatore() != null) {
			soggettoPagatore.setIndirizzoPagatore(rt.getSoggettoPagatore().getIndirizzoPagatore());
		}
		if (rt.getSoggettoPagatore().getCivicoPagatore() != null) {
			soggettoPagatore.setCivicoPagatore(rt.getSoggettoPagatore().getCivicoPagatore());
		}
		if (rt.getSoggettoPagatore().getCapPagatore() != null) {
			soggettoPagatore.setCapPagatore(rt.getSoggettoPagatore().getCapPagatore());
		}
		if (rt.getSoggettoPagatore().getLocalitaPagatore() != null) {
			soggettoPagatore.setLocalitaPagatore(rt.getSoggettoPagatore().getLocalitaPagatore());
		}
		if (rt.getSoggettoPagatore().getProvinciaPagatore() != null) {
			soggettoPagatore.setProvinciaPagatore(rt.getSoggettoPagatore().getProvinciaPagatore());
		}
		if (rt.getSoggettoPagatore().getNazionePagatore() != null) {
			soggettoPagatore.setNazionePagatore(rt.getSoggettoPagatore().getNazionePagatore());
		}
		if (rt.getSoggettoPagatore().getEMailPagatore() != null) {
			soggettoPagatore.setEMailPagatore(rt.getSoggettoPagatore().getEMailPagatore());
		}
		ctEsito.setSoggettoPagatore(soggettoPagatore);

		/**
		 * CtDatiVersamentoEsito
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtDatiVersamentoEsito datiPagamento = it.veneto.regione.schemas.x2012.pagamenti.CtDatiVersamentoEsito.Factory
				.newInstance();
		datiPagamento.setCodiceEsitoPagamento(it.veneto.regione.schemas.x2012.pagamenti.StCodiceEsitoPagamento.Enum
				.forString(rt.getDatiPagamento().getCodiceEsitoPagamento().toString()));
		datiPagamento.setImportoTotalePagato(rt.getDatiPagamento().getImportoTotalePagato());
		datiPagamento.setIdentificativoUnivocoVersamento(rt.getDatiPagamento().getIdentificativoUnivocoVersamento());
		datiPagamento.setCodiceContestoPagamento(rt.getDatiPagamento().getCodiceContestoPagamento());

		/**
		 * CtDatiSingoloPagamentoEsito
		 */

		if (rt.getDatiPagamento().getDatiSingoloPagamentoArray() != null) {

			it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito[] pagamentiSingoli = new it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito[rt
					.getDatiPagamento().getDatiSingoloPagamentoArray().length];

			for (int i = 0; i < rt.getDatiPagamento().getDatiSingoloPagamentoArray().length; i++) {

				CtDatiSingoloPagamentoRT tmpPag = rt.getDatiPagamento().getDatiSingoloPagamentoArray(i);
				it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito singoloPagamento = it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito.Factory
						.newInstance();
				singoloPagamento.setSingoloImportoPagato(tmpPag.getSingoloImportoPagato());
				singoloPagamento.setCausaleVersamento(tmpPag.getCausaleVersamento());
				singoloPagamento.setDataEsitoSingoloPagamento(tmpPag.getDataEsitoSingoloPagamento());
				singoloPagamento.setDatiSpecificiRiscossione(tmpPag.getDatiSpecificiRiscossione());

				if (tmpPag.getEsitoSingoloPagamento() != null) {
					singoloPagamento.setEsitoSingoloPagamento(tmpPag.getEsitoSingoloPagamento());
				}
				singoloPagamento.setIdentificativoUnivocoRiscossione(tmpPag.getIdentificativoUnivocoRiscossione());

				if (tmpPag.getCommissioniApplicatePSP() != null) {
					singoloPagamento.setCommissioniApplicatePSP(tmpPag.getCommissioniApplicatePSP());
				}
				if (tmpPag.getAllegatoRicevuta() != null) {
					it.veneto.regione.schemas.x2012.pagamenti.CtAllegatoRicevuta ctAllegatoRicevuta = it.veneto.regione.schemas.x2012.pagamenti.CtAllegatoRicevuta.Factory
							.newInstance();
					ctAllegatoRicevuta.setTipoAllegatoRicevuta(
							it.veneto.regione.schemas.x2012.pagamenti.StTipoAllegatoRicevuta.Enum
									.forString(tmpPag.getAllegatoRicevuta().getTipoAllegatoRicevuta().toString()));
					ctAllegatoRicevuta.setTestoAllegato(tmpPag.getAllegatoRicevuta().getTestoAllegato());
					singoloPagamento.setAllegatoRicevuta(ctAllegatoRicevuta);
				}
				pagamentiSingoli[i] = singoloPagamento;
			}

			datiPagamento.setDatiSingoloPagamentoArray(pagamentiSingoli);
		}

		ctEsito.setDatiPagamento(datiPagamento);

		return ctEsito;
	}

	/**
	 * @param header
	 * @param esito
	 * @param mygovRpEId
	 */
	private void saveE(it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT header,
			it.veneto.regione.schemas.x2012.pagamenti.CtEsito esito, Long mygovRpEId) {

		List<RpEDettaglioDto> esitoPagamenti = new ArrayList<RpEDettaglioDto>();

		if (esito.getDatiPagamento().getDatiSingoloPagamentoArray() != null) {

			it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito[] pagamenti = esito.getDatiPagamento()
					.getDatiSingoloPagamentoArray();

			for (int i = 0; i < pagamenti.length; i++) {

				it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito singoloPagamento = pagamenti[i];
				RpEDettaglioDto esitoPagamento = new RpEDettaglioDto();

				esitoPagamento
						.setNumEDatiPagDatiSingPagSingoloImportoPagato(singoloPagamento.getSingoloImportoPagato());
				esitoPagamento
						.setDeEDatiPagDatiSingPagEsitoSingoloPagamento(singoloPagamento.getEsitoSingoloPagamento());
				esitoPagamento.setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(
						singoloPagamento.getDataEsitoSingoloPagamento().getTime());
				esitoPagamento.setCodEDatiPagDatiSingPagIdUnivocoRiscoss(
						singoloPagamento.getIdentificativoUnivocoRiscossione());
				esitoPagamento.setDeEDatiPagDatiSingPagCausaleVersamento(singoloPagamento.getCausaleVersamento());
				esitoPagamento.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(
						singoloPagamento.getDatiSpecificiRiscossione());

				esitoPagamento.setNumEDatiPagDatiSingPagCommissioniApplicatePsp(
						singoloPagamento.getCommissioniApplicatePSP());
				if (singoloPagamento.getAllegatoRicevuta() != null) {
					esitoPagamento.setCodEDatiPagDatiSingPagAllegatoRicevutaTipo(
							singoloPagamento.getAllegatoRicevuta().getTipoAllegatoRicevuta().toString());
					esitoPagamento.setBlbEDatiPagDatiSingPagAllegatoRicevutaTest(
							singoloPagamento.getAllegatoRicevuta().getTestoAllegato());
				}

				esitoPagamenti.add(esitoPagamento);
			}
		}

		String codESilinviaesitoIdDominio = header.getIdentificativoDominio();
		String codESilinviaesitoIdUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
		String codESilinviaesitoCodiceContestoPagamento = header.getCodiceContestoPagamento();

		String deEVersioneOggetto = esito.getVersioneOggetto();
		String codEDomIdDominio = esito.getDominio().getIdentificativoDominio();
		String codEDomIdStazioneRichiedente = esito.getDominio().getIdentificativoStazioneRichiedente();
		String codEIdMessaggioRicevuta = esito.getIdentificativoMessaggioRicevuta();
		Date dtEDataOraMessaggioRicevuta = esito.getDataOraMessaggioRicevuta().getTime();
		String codERiferimentoMessaggioRichiesta = esito.getRiferimentoMessaggioRichiesta();
		Date dtERiferimentoDataRichiesta = esito.getRiferimentoDataRichiesta().getTime();

		String codEIstitAttesIdUnivAttesTipoIdUnivoco = esito.getIstitutoAttestante()
				.getIdentificativoUnivocoAttestante().getTipoIdentificativoUnivoco().toString();
		String codEIstitAttesIdUnivAttesCodiceIdUnivoco = esito.getIstitutoAttestante()
				.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco();
		String deEIstitAttesDenominazioneAttestante = esito.getIstitutoAttestante().getDenominazioneAttestante();
		String codEIstitAttesCodiceUnitOperAttestante = esito.getIstitutoAttestante().getCodiceUnitOperAttestante();
		String deEIstitAttesDenomUnitOperAttestante = esito.getIstitutoAttestante().getDenomUnitOperAttestante();
		String deEIstitAttesIndirizzoAttestante = esito.getIstitutoAttestante().getIndirizzoAttestante();
		String deEIstitAttesCivicoAttestante = esito.getIstitutoAttestante().getCivicoAttestante();
		String codEIstitAttesCapAttestante = esito.getIstitutoAttestante().getCapAttestante();
		String deEIstitAttesLocalitaAttestante = esito.getIstitutoAttestante().getLocalitaAttestante();
		String deEIstitAttesProvinciaAttestante = esito.getIstitutoAttestante().getProvinciaAttestante();
		String codEIstitAttesNazioneAttestante = esito.getIstitutoAttestante().getNazioneAttestante();

		String codEEnteBenefIdUnivBenefTipoIdUnivoco = esito.getEnteBeneficiario()
				.getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco().toString();
		String codEEnteBenefIdUnivBenefCodiceIdUnivoco = esito.getEnteBeneficiario()
				.getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco();
		String deEEnteBenefDenominazioneBeneficiario = esito.getEnteBeneficiario().getDenominazioneBeneficiario();
		String codEEnteBenefCodiceUnitOperBeneficiario = esito.getEnteBeneficiario().getCodiceUnitOperBeneficiario();
		String deEEnteBenefDenomUnitOperBeneficiario = esito.getEnteBeneficiario().getDenomUnitOperBeneficiario();
		String deEEnteBenefIndirizzoBeneficiario = esito.getEnteBeneficiario().getIndirizzoBeneficiario();
		String deEEnteBenefCivicoBeneficiario = esito.getEnteBeneficiario().getCivicoBeneficiario();
		String codEEnteBenefCapBeneficiario = esito.getEnteBeneficiario().getCapBeneficiario();
		String deEEnteBenefLocalitaBeneficiario = esito.getEnteBeneficiario().getLocalitaBeneficiario();
		String deEEnteBenefProvinciaBeneficiario = esito.getEnteBeneficiario().getProvinciaBeneficiario();
		String codEEnteBenefNazioneBeneficiario = esito.getEnteBeneficiario().getNazioneBeneficiario();

		String codESoggVersIdUnivVersTipoIdUnivoco = null;
		String codESoggVersIdUnivVersCodiceIdUnivoco = null;
		String deESoggVersAnagraficaVersante = null;
		String deESoggVersIndirizzoVersante = null;
		String deESoggVersCivicoVersante = null;
		String codESoggVersCapVersante = null;
		String deESoggVersLocalitaVersante = null;
		String deESoggVersProvinciaVersante = null;
		String codESoggVersNazioneVersante = null;
		String deESoggVersEmailVersante = null;

		if (esito.getSoggettoVersante() != null) {
			codESoggVersIdUnivVersTipoIdUnivoco = esito.getSoggettoVersante().getIdentificativoUnivocoVersante()
					.getTipoIdentificativoUnivoco().toString();
			codESoggVersIdUnivVersCodiceIdUnivoco = esito.getSoggettoVersante().getIdentificativoUnivocoVersante()
					.getCodiceIdentificativoUnivoco();
			deESoggVersAnagraficaVersante = esito.getSoggettoVersante().getAnagraficaVersante();
			deESoggVersIndirizzoVersante = esito.getSoggettoVersante().getIndirizzoVersante();
			deESoggVersCivicoVersante = esito.getSoggettoVersante().getCivicoVersante();
			codESoggVersCapVersante = esito.getSoggettoVersante().getCapVersante();
			deESoggVersLocalitaVersante = esito.getSoggettoVersante().getLocalitaVersante();
			deESoggVersProvinciaVersante = esito.getSoggettoVersante().getProvinciaVersante();
			codESoggVersNazioneVersante = esito.getSoggettoVersante().getNazioneVersante();
			deESoggVersEmailVersante = esito.getSoggettoVersante().getEMailVersante();
		}

		String codESoggPagIdUnivPagTipoIdUnivoco = esito.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
				.getTipoIdentificativoUnivoco().toString();
		String codESoggPagIdUnivPagCodiceIdUnivoco = esito.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
				.getCodiceIdentificativoUnivoco();
		String deESoggPagAnagraficaPagatore = esito.getSoggettoPagatore().getAnagraficaPagatore();
		String deESoggPagIndirizzoPagatore = esito.getSoggettoPagatore().getIndirizzoPagatore();
		String deESoggPagCivicoPagatore = esito.getSoggettoPagatore().getCivicoPagatore();
		String codESoggPagCapPagatore = esito.getSoggettoPagatore().getCapPagatore();
		String deESoggPagLocalitaPagatore = esito.getSoggettoPagatore().getLocalitaPagatore();
		String deESoggPagProvinciaPagatore = esito.getSoggettoPagatore().getProvinciaPagatore();
		String codESoggPagNazionePagatore = esito.getSoggettoPagatore().getNazionePagatore();
		String deESoggPagEmailPagatore = esito.getSoggettoPagatore().getEMailPagatore();

		String codEDatiPagCodiceEsitoPagamento = esito.getDatiPagamento().getCodiceEsitoPagamento().toString();
		BigDecimal numEDatiPagImportoTotalePagato = esito.getDatiPagamento().getImportoTotalePagato();
		String codEDatiPagIdUnivocoVersamento = esito.getDatiPagamento().getIdentificativoUnivocoVersamento();
		String codEDatiPagCodiceContestoPagamento = esito.getDatiPagamento().getCodiceContestoPagamento();

		this.manageRPEService.updateEById(mygovRpEId, codESilinviaesitoIdDominio, codESilinviaesitoIdUnivocoVersamento,
				codESilinviaesitoCodiceContestoPagamento, deEVersioneOggetto, codEDomIdDominio,
				codEDomIdStazioneRichiedente, codEIdMessaggioRicevuta, dtEDataOraMessaggioRicevuta,
				codERiferimentoMessaggioRichiesta, dtERiferimentoDataRichiesta, codEIstitAttesIdUnivAttesTipoIdUnivoco,
				codEIstitAttesIdUnivAttesCodiceIdUnivoco, deEIstitAttesDenominazioneAttestante,
				codEIstitAttesCodiceUnitOperAttestante, deEIstitAttesDenomUnitOperAttestante,
				deEIstitAttesIndirizzoAttestante, deEIstitAttesCivicoAttestante, codEIstitAttesCapAttestante,
				deEIstitAttesLocalitaAttestante, deEIstitAttesProvinciaAttestante, codEIstitAttesNazioneAttestante,
				codEEnteBenefIdUnivBenefTipoIdUnivoco, codEEnteBenefIdUnivBenefCodiceIdUnivoco,
				deEEnteBenefDenominazioneBeneficiario, codEEnteBenefCodiceUnitOperBeneficiario,
				deEEnteBenefDenomUnitOperBeneficiario, deEEnteBenefIndirizzoBeneficiario,
				deEEnteBenefCivicoBeneficiario, codEEnteBenefCapBeneficiario, deEEnteBenefLocalitaBeneficiario,
				deEEnteBenefProvinciaBeneficiario, codEEnteBenefNazioneBeneficiario,
				codESoggVersIdUnivVersTipoIdUnivoco, codESoggVersIdUnivVersCodiceIdUnivoco,
				deESoggVersAnagraficaVersante, deESoggVersIndirizzoVersante, deESoggVersCivicoVersante,
				codESoggVersCapVersante, deESoggVersLocalitaVersante, deESoggVersProvinciaVersante,
				codESoggVersNazioneVersante, deESoggVersEmailVersante, codESoggPagIdUnivPagTipoIdUnivoco,
				codESoggPagIdUnivPagCodiceIdUnivoco, deESoggPagAnagraficaPagatore, deESoggPagIndirizzoPagatore,
				deESoggPagCivicoPagatore, codESoggPagCapPagatore, deESoggPagLocalitaPagatore,
				deESoggPagProvinciaPagatore, codESoggPagNazionePagatore, deESoggPagEmailPagatore,
				codEDatiPagCodiceEsitoPagamento, numEDatiPagImportoTotalePagato, codEDatiPagIdUnivocoVersamento,
				codEDatiPagCodiceContestoPagamento, esitoPagamenti);
	}

	// /**
	// * @param _rispostaRT
	// * @param _rispostaEsito
	// * @return
	// */
	// private PaaInviaRTRisposta fillRispostaRT(PaaInviaRTRisposta _rispostaRT,
	// PaaSILInviaEsitoRisposta _rispostaEsito) {
	//
	// EsitoPaaInviaRT rt_esitoPaaInviaRT = new EsitoPaaInviaRT();
	// rt_esitoPaaInviaRT.setEsito(_rispostaEsito.getPaaSILInviaEsitoRisposta().getEsito());
	//
	// gov.telematici.pagamenti.ws.nodoregionalepernodospc.FaultBean
	// rt_rispostaEsito_FaultBean = new
	// gov.telematici.pagamenti.ws.nodoregionalepernodospc.FaultBean();
	//
	// String description = null;
	// String faultCode = null;
	// String faultString = null;
	// String faultId = null;
	// Integer faultSerial = null;
	//
	// it.veneto.regione.pagamenti.pa.papernodoregionale.FaultBean faultBean =
	// _rispostaEsito.getPaaSILInviaEsitoRisposta().getFault();
	// if (faultBean != null) {
	// description = faultBean.getDescription();
	// faultCode = faultBean.getFaultCode();
	// faultString = faultBean.getDescription();
	// faultId = faultBean.getId();
	// faultSerial = faultBean.getSerial();
	//
	// rt_rispostaEsito_FaultBean.setDescription(description);
	// rt_rispostaEsito_FaultBean.setFaultCode(faultCode);
	// rt_rispostaEsito_FaultBean.setFaultString(faultString);
	// rt_rispostaEsito_FaultBean.setId(faultId);
	// rt_rispostaEsito_FaultBean.setSerial(faultSerial);
	// rt_esitoPaaInviaRT.setFault(rt_rispostaEsito_FaultBean);
	// }
	//
	// _rispostaRT.setPaaInviaRTRisposta(rt_esitoPaaInviaRT);
	//
	// return _rispostaRT;
	// }

	/**
	 * @param _rispostaRT
	 * @param _rispostaEsito
	 * @param faultCode
	 * @param faultDescription
	 * @param mygovRptRtId
	 * @return
	 */
	private PaaInviaRTRisposta fillAndSaveRispostaRT_Error(PaaInviaRTRisposta _rispostaRT, String faultCode,
			String faultDescription, Long mygovRptRtId) {

		EsitoPaaInviaRT rt_esitoPaaInviaRT = new EsitoPaaInviaRT();
		rt_esitoPaaInviaRT.setEsito("KO");

		gov.telematici.pagamenti.ws.nodoregionalepernodospc.FaultBean rt_rispostaEsito_FaultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.FaultBean();

		rt_rispostaEsito_FaultBean.setFaultCode(faultCode);
		rt_rispostaEsito_FaultBean.setDescription(faultDescription);

		rt_esitoPaaInviaRT.setFault(rt_rispostaEsito_FaultBean);

		_rispostaRT.setPaaInviaRTRisposta(rt_esitoPaaInviaRT);

		if (mygovRptRtId != null)
			saveRtRisposta(_rispostaRT, mygovRptRtId);

		return _rispostaRT;
	}


	/**
	 * 
	 * ws che fesp espone verso (per) il nodo SPC
	 * il NodoSPC chiama la componente di Back-end del FESP per mezzo della primitiva paaInviaRichiestaRevoca
	 * e sottomette una richiesta di revoca
	 * 
	 * PAA_SYSTEM_ERROR: errore generico di sistema
	 * PAA_SINTASSI_EXTRAXSD: in caso di errori nella SOAP request
	 * PAA_SINTASSI_XSD: in caso di errori nel documento XML RR
	 * PAA_SEMANTICA: nel caso di errori semantici
	 * PAA_RT_SCONOSCIUTA: incaso la sottomissione della richiesta di revoca non è associata a nessuna RT
	 * 
	 * 
	 * 
	 * @param identificativoDominio
	 * @param identificativoUnivocoVersamento
	 * @param codiceContestoPagamento
	 * @param rr
	 */
	@SuppressWarnings("static-access")
	@Override
	public TipoInviaRichiestaRevocaRisposta paaInviaRichiestaRevoca(
			String identificativoDominio,
			String identificativoUnivocoVersamento,
			String codiceContestoPagamento, byte[] rr) {
		
		LOG.info("paaInviaRichiestaRevoca - Executing operation paaInviaRichiestaRevoca");
		
		TipoInviaRichiestaRevocaRisposta rispostaRR = new TipoInviaRichiestaRevocaRisposta();
		
		Date dataOraEvento = new Date();
		String parametriSpecificiInterfaccia = "";
		String esitoReq = "";
		String identificativoPrestatoreServiziPagamento = "";
		String tipoVersamento = "";
		String componente = Constants.COMPONENTE.FESP.toString();
		String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
		String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaInviaRichiestaRevoca.toString();
		String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
		String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;
		String identificativoErogatore = "";
		String identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
		String canalePagamento = "";
		String rrString = "";
		String tipoIdentificativoUnivoco = "";
		String codiceIdentificativoUnivoco = "";
		String tipoRevoca = "";
		BigDecimal sumImpSingRev = BigDecimal.ZERO;
		List<CtDatiSingolaRevoca> listaSingolaRevoca = new ArrayList<CtDatiSingolaRevoca>();
		CtSoggettoVersante sgVersante = null;
		
		
		//Log nel giornale degli eventi - ricezione richiesta revoca
		parametriSpecificiInterfaccia = "Identificativo dominio: " + identificativoDominio + " - IUV: " + identificativoUnivocoVersamento + 
				" - Codice contesto pagamento: " + codiceContestoPagamento;
		LOG.info("paaInviaRichiestaRevoca - Richiesta: " + parametriSpecificiInterfaccia);
		esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
		giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
				codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
				categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
				identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
				esitoReq);
		
		//Recupero ente dato identificativoDominio (Codice Fiscale dell'ente)
		MygovEnte ente = enteService.getByCodiceFiscale(identificativoDominio); 
		
		//Verifico presenza ente
		if (ente == null) {
			LOG.info("paaInviaRichiestaRevoca - Ente non valido: " + identificativoDominio);
			
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: Ente non valido: " + identificativoDominio;
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SINTASSI_EXTRAXSD, "Errore di sintassi extra XSD", "Codice fiscale Ente [" + identificativoDominio + "] indicato nella request, non valido");		
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}
		
		
		//Verifico se la richiesta di revoca sia per uan RT esistente
		MygovRptRt rptRt = manageRPTRTService.getRtByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);
		if (rptRt == null) {
			String msg = "Nessuna RT per identificativo dominio [ " + identificativoDominio + " ], IUV [ " + identificativoUnivocoVersamento
					+ " ] e codice contesto pagamento [ " + codiceContestoPagamento + " ]";
			LOG.info(msg);
			
			parametriSpecificiInterfaccia = msg;
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio,
					identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
					tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_RT_SCONOSCIUTA, "RT sconosciuta", msg);
			
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}
		
		
		//Controllo che la richiesta non sia di una RT di cui in precedenza è stata già effettuata
		// una richiesta di revoca dato identificativoDominio e identificativoUnivocoVersamento
		MygovRrEr rifRevoca = manageRRService.checkRrByDomIuvCont(identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);
		if(null != rifRevoca){
			LOG.info("paaInviaRichiestaRevoca - Richiesta revoca duplicata per i parametri: identificativoDominio [" + identificativoDominio + "] e identificativoUnivocoVersamento ["
					+ identificativoUnivocoVersamento +"]");
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca - Richiesta revoca duplicata per i parametri: identificativoDominio [" + identificativoDominio + "] e identificativoUnivocoVersamento ["
					+ identificativoUnivocoVersamento +"]";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
		
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico", 
					"paaInviaRichiestaRevoca - Richiesta revoca duplicata per i parametri: identificativoDominio [" + identificativoDominio + "] e identificativoUnivocoVersamento ["
							+ identificativoUnivocoVersamento +"]");
			
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}		
		
		try{
			
			//Log nel giornale degli eventi - ricezione richiesta revoca con richiesta 
			rrString = Base64.isBase64(rr) ? new String(Base64.decodeBase64(rr), "UTF-8") : new String(rr, "UTF-8");
			parametriSpecificiInterfaccia = "Identificativo dominio: " + identificativoDominio + " - IUV: " + identificativoUnivocoVersamento + 
					" - Codice contesto pagamento: " + codiceContestoPagamento + " - RR: " + rrString;
			LOG.info("paaInviaRichiestaRevoca - Richiesta: " + parametriSpecificiInterfaccia);
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
		}catch (UnsupportedEncodingException e) {
			
			//Log nel giornale degli eventi - Errore nell'encoding in UTF-8 della Richiesta di Revoca
			String msg = "paaInviaRichiestaRevoca - Errore nell'encoding in UTF-8 della Richiesta di Revoca";
			LOG.error(msg);
			parametriSpecificiInterfaccia = msg;
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore generico", "Errore nell'encoding in UTF-8 della Richiesta di Revoca");			
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
			
		}		

		//Controllo sulla validità dell'xml
		RRDocument rrDocument = null;
		boolean validXml = false;
		List<XmlError> errors = new ArrayList<XmlError>();
		XmlOptions options = new XmlOptions();
		
		try{
			
			rrDocument = RRDocument.Factory.parse(rrString);
			options.setErrorListener(errors);
			validXml = rrDocument.validate(options);
			
		}catch (XmlException e) {
			
			LOG.error("paaInviaRichiestaRevoca - Errore struttura xml: " + e.getMessage());
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca errore struttura xml: [" + e.getMessage() + "]";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			//Log nel giornale degli eventi - struttura xml non conforme
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SINTASSI_XSD, "Errore di sintassi XSD", "paaInviaRichiestaRevoca errore struttura xml: [" + e.getMessage() + "]");
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
			
		}
		
		if (!validXml) {
			LOG.info("paaInviaRichiestaRevoca - XML ricevuto per paaInviaRichiestaRevoca non conforme");
			StringBuffer buffer = new StringBuffer();
			buffer.append("XML ricevuto per paaInviaRichiestaRevoca non conforme");
			for (int i = 0; i < errors.size(); i++) {
				XmlError error = (XmlError) errors.get(i);
				buffer.append("Message: " + error.getMessage() + "\n");
				buffer.append("Location of invalid XML: " + error.getCursorLocation().xmlText() + "\n");
			}
			parametriSpecificiInterfaccia = buffer.toString();
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SINTASSI_XSD, "Errore di sintassi XSD", "XML ricevuto per paaInviaRichiestaRevoca non conforme");
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}
		
		
		//Estraggo richiesta
		CtRichiestaRevoca richiesta = rrDocument.getRR();
		
		// Controllo esistenza di una richiesta di revoca
		// Identificativo legato alla trasmissione della Richiesta di Revoca.
		// Deve essere univoco nell'ambito della stessa data riferita all'elemento dataMessaggioRevoca.
		Calendar dtMsgRrStart = richiesta.getDataOraMessaggioRevoca();
		dtMsgRrStart.set(dtMsgRrStart.get(dtMsgRrStart.YEAR), dtMsgRrStart.get(dtMsgRrStart.MONTH), dtMsgRrStart.get(dtMsgRrStart.DATE), 00, 00, 00);
		Calendar dtMsgRrFine = richiesta.getDataOraMessaggioRevoca();
		dtMsgRrFine.set(dtMsgRrFine.get(dtMsgRrFine.YEAR), dtMsgRrFine.get(dtMsgRrFine.MONTH), dtMsgRrFine.get(dtMsgRrFine.DATE), 23, 59, 59);

		rifRevoca = manageRRService.checkRrByRifMessaggioRevocaDay(richiesta.getIdentificativoMessaggioRevoca(), dtMsgRrStart.getTime(), dtMsgRrFine.getTime());
		if(null != rifRevoca){
			LOG.info("paaInviaRichiestaRevoca - Richiesta revoca duplicata per i parametri: IdentificativoMessaggioRevoca [" + richiesta.getIdentificativoMessaggioRevoca() + "] e DataOraMessaggioRevoca ["
					+ richiesta.getDataOraMessaggioRevoca().getTime().toString() +"]");
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca - Richiesta revoca duplicata per i parametri: IdentificativoMessaggioRevoca [" + richiesta.getIdentificativoMessaggioRevoca() + "] e DataOraMessaggioRevoca ["
						+ richiesta.getDataOraMessaggioRevoca().getTime().toString() +"]";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
		
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico", 
					"paaInviaRichiestaRevoca - Richiesta revoca duplicata per i parametri: IdentificativoMessaggioRevoca [" + richiesta.getIdentificativoMessaggioRevoca() + "] e DataOraMessaggioRevoca ["
					+ richiesta.getDataOraMessaggioRevoca().getTime().toString() +"]");
			
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}

		CtDatiRevoca datiRevoca = richiesta.getDatiRevoca();
		
		
		
		///Controllo  che dentificativoUnivocoVersamento presente nei dati in ingresso non differisca da quello presente in datiRevoca
		if(!identificativoUnivocoVersamento.equals(datiRevoca.getIdentificativoUnivocoVersamento())){
			LOG.info("paaInviaRichiestaRevoca - IdentificativoUnivocoVersamento presente nella richiesta " + identificativoUnivocoVersamento + " differisce da quello presente in datiRevoca "+ datiRevoca.getIdentificativoUnivocoVersamento());
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: IdentificativoUnivocoVersamento presente nella richiesta ( " + identificativoUnivocoVersamento
					+ " differisce da quello presente in datiRevoca( "+ datiRevoca.getIdentificativoUnivocoVersamento() +" )";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);

			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico", 
					"paaInviaRichiestaRevoca: IdentificativoUnivocoVersamento presente nella richiesta ( " + identificativoUnivocoVersamento
					+ " differisce da quello presente in datiRevoca( "+ datiRevoca.getIdentificativoUnivocoVersamento() +" )");

			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}	
		
		
		
		
		//Controlli su istituto attestante
		CtIstitutoAttestante istAttestante = richiesta.getIstitutoAttestante();
		tipoIdentificativoUnivoco = istAttestante.getIdentificativoUnivocoMittente().getTipoIdentificativoUnivoco().toString();
		codiceIdentificativoUnivoco = istAttestante.getIdentificativoUnivocoMittente().getCodiceIdentificativoUnivoco();
		
		/*
		 * CONTROLLO NON NECESSARIO
		//Controllo su tipoIdentificativoUnivoco e codiceIdentificativoUnivoco presente nella richiesta
		if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO.G.toString())) { //Persona giuridica (PIVA)
			if (!Utils.isValidPIVA(codiceIdentificativoUnivoco)) {
				LOG.info("paaInviaRichiestaRevoca - P.IVA istituto attestante non valida: " + codiceIdentificativoUnivoco);

				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  P.IVA istituto attestante non valida: "
						+ codiceIdentificativoUnivoco;
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);

				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						" P.IVA istituto attestante non valida [" + codiceIdentificativoUnivoco + "]");

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
		} else if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO.A.toString())) { //Codice ABI (Associazione Bancaria Italiana) è un numero composto da cinque cifre e rappresenta l'istituto di credito
			if (!Utils.isValidABI(codiceIdentificativoUnivoco)) {
				LOG.info("paaInviaRichiestaRevoca - codice ABI istituto attestante non valido: " + codiceIdentificativoUnivoco);

				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  codice ABI istituto attestante non valido: "
						+ codiceIdentificativoUnivoco;
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);

				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						"Codice ABI istituto attestante non valido [" + codiceIdentificativoUnivoco + "]");

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
		} else if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO.B.toString())) { //Codice BIC (standard ISO 9362)
			if (!Utils.isValidBIC(codiceIdentificativoUnivoco)) {
				LOG.info("paaInviaRichiestaRevoca - codice BIC istituto attestante non valido: " + codiceIdentificativoUnivoco);

				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  codice BIC istituto attestante non valido: "
						+ codiceIdentificativoUnivoco;
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);

				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						"Codice BIC istituto attestante non valido [" + codiceIdentificativoUnivoco + "]");

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
		} else {
			LOG.info("paaInviaRichiestaRevoca - tipoIdentificativoUnivoco istituto attestante non valido: " + tipoIdentificativoUnivoco);
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  tipoIdentificativoUnivoco istituto attestante non valido: " + tipoIdentificativoUnivoco;
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio,
					identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
					tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
					"tipoIdentificativoUnivoco istituto attestante non valido: " + tipoIdentificativoUnivoco);

			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
			
		}
		
		
		//Controllo validità CAP Attestante
		if(StringUtils.isNotBlank(istAttestante.getCapMittente()) && StringUtils.isNotBlank(istAttestante.getNazioneMittente())){
			if(!Utils.isValidCAP(istAttestante.getCapMittente(), istAttestante.getNazioneMittente())){
				LOG.info("paaInviaRichiestaRevoca - CAP attestante non valido: " + istAttestante.getCapMittente());

				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  CAP attestante non valido: "
						+ istAttestante.getCapMittente();
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);

				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						" CAP attestante non valido [" + istAttestante.getCapMittente() + "]");

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
		}
		
		//Controlli soggetto versante
		if(null != richiesta.getSoggettoVersante()){ // Il soggettoVersante può anche non essere presente
			
			sgVersante = richiesta.getSoggettoVersante();
			tipoIdentificativoUnivoco = sgVersante.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString();
			codiceIdentificativoUnivoco = sgVersante.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco();
			
			if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO.G.toString())) { //Persona giuridica (PIVA)
				if (!Utils.isValidPIVA(codiceIdentificativoUnivoco)) {
					LOG.info("paaInviaRichiestaRevoca: P.IVA soggetto versante non valida: " + codiceIdentificativoUnivoco);

					parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  P.IVA soggetto versante non valida: "
							+ codiceIdentificativoUnivoco;
					esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
					giornaleService.registraEvento(dataOraEvento, identificativoDominio,
							identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
							tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
							esitoReq);

					FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
							" P.IVA soggetto versante non valida [" + codiceIdentificativoUnivoco + "]");

					rispostaRR.setEsito(esitoReq);
					rispostaRR.setFault(faultBean);
					return rispostaRR;
				}
			} else if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO.F.toString())) { //Persona fisica (CF)
				if (!Utils.isValidCF(codiceIdentificativoUnivoco)) {
					LOG.info("paaInviaRichiestaRevoca: Codice fiscale soggetto versante non valido: " + codiceIdentificativoUnivoco);

					parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: Codice fiscale soggetto versante non valido: "
							+ codiceIdentificativoUnivoco;
					esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
					giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
							codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
							categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);

					FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
							"Codice fiscale soggetto versante non valido [" + codiceIdentificativoUnivoco + "]");

					rispostaRR.setEsito(esitoReq);
					rispostaRR.setFault(faultBean);
					return rispostaRR;
				}
			} else if(!tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO.ANONIMO.toString())){
				LOG.info("paaInviaRichiestaRevoca - tipoIdentificativoUnivoco soggetto versante non valido: " + tipoIdentificativoUnivoco);
				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  tipoIdentificativoUnivoco soggetto versante non valido: " + tipoIdentificativoUnivoco;
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
				
				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						"tipoIdentificativoUnivoco soggetto versante non valido: " + tipoIdentificativoUnivoco);

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
			
			
			if(StringUtils.isNotBlank(sgVersante.getCapVersante()) && StringUtils.isNotBlank(sgVersante.getNazioneVersante())){
				if(!Utils.isValidCAP(sgVersante.getCapVersante(), sgVersante.getNazioneVersante())){
					LOG.info("paaInviaRichiestaRevoca: CAP versante non valido: " + sgVersante.getCapVersante());

					parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  CAP versante non valido: "
							+ sgVersante.getCapVersante();
					esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
					giornaleService.registraEvento(dataOraEvento, identificativoDominio,
							identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
							tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
							esitoReq);

					FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
							" CAP versante non valido [" + sgVersante.getCapVersante() + "]");

					rispostaRR.setEsito(esitoReq);
					rispostaRR.setFault(faultBean);
					return rispostaRR;
				}
			}
			
			
			if(StringUtils.isNotBlank(sgVersante.getEMailVersante())){
				if(!Utils.isValidEmail(sgVersante.getEMailVersante())){
					LOG.info("paaInviaRichiestaRevoca: Email versante non valido: " + sgVersante.getEMailVersante());

					parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  Email versante non valido: "
							+ sgVersante.getEMailVersante();
					esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
					giornaleService.registraEvento(dataOraEvento, identificativoDominio,
							identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
							tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
							esitoReq);

					FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
							" Email versante non valido [" + sgVersante.getEMailVersante() + "]");

					rispostaRR.setEsito(esitoReq);
					rispostaRR.setFault(faultBean);
					return rispostaRR;
				}
			}
			
		}
		*/
		//Controllo soggetto pagatore
		CtSoggettoPagatore sgPagatore = richiesta.getSoggettoPagatore();
		
		/*
		tipoIdentificativoUnivoco = sgPagatore.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString();
		codiceIdentificativoUnivoco = sgPagatore.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco();
		
		if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO.G.toString())) { //Persona giuridica (PIVA)
			if (!Utils.isValidPIVA(codiceIdentificativoUnivoco)) {
				LOG.info("paaInviaRichiestaRevoca: P.IVA pagatore non valida: " + codiceIdentificativoUnivoco);

				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  P.IVA pagatore non valida: "
						+ codiceIdentificativoUnivoco;
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);

				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						" P.IVApagatore non valida [" + codiceIdentificativoUnivoco + "]");

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
		} else if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO.F.toString())) { //Persona fisica (CF)
			if (!Utils.isValidCF(codiceIdentificativoUnivoco)) {
				LOG.info("paaInviaRichiestaRevoca: Codice fiscale pagatore non valido: " + codiceIdentificativoUnivoco);

				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: Codice fiscale pagatore non valido: "
						+ codiceIdentificativoUnivoco;
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);

				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						"Codice fiscale pagatore non valido [" + codiceIdentificativoUnivoco + "]");

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
		}  else if(!tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO.ANONIMO.toString())){
			LOG.info("paaInviaRichiestaRevoca - tipoIdentificativoUnivoco pagatore non valido: " + tipoIdentificativoUnivoco);
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  tipoIdentificativoUnivoco pagatore non valido: " + tipoIdentificativoUnivoco;
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio,
					identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
					tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
					"tipoIdentificativoUnivoco pagatore non valido: " + tipoIdentificativoUnivoco);

			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}
		
		if(StringUtils.isNotBlank(sgPagatore.getCapPagatore()) && StringUtils.isNotBlank(sgPagatore.getNazionePagatore())){
			if(!Utils.isValidCAP(sgPagatore.getCapPagatore(), sgPagatore.getNazionePagatore())){
				LOG.info("paaInviaRichiestaRevoca: CAP pagatore non valido: " + sgPagatore.getCapPagatore());

				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  CAP pagatore non valido: "
						+ sgPagatore.getCapPagatore();
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);

				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						" CAP pagatore non valido [" + sgPagatore.getCapPagatore() + "]");

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
		}
		
		if(StringUtils.isNotBlank(sgPagatore.getEMailPagatore())){
			if(!Utils.isValidEmail(sgPagatore.getEMailPagatore())){
				LOG.error("paaInviaRichiestaRevoca: Email pagatore non valido: " + sgPagatore.getEMailPagatore());

				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca:  Email pagatore non valido: "
						+ sgPagatore.getEMailPagatore();
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);

				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						"Email pagatore non valido [" + sgPagatore.getEMailPagatore() + "]");

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
		}
		*/
		
		//Controlli datiRevoca
		
		//Se tipoRevoca != null può assumere valori: 0 1 2 
		if(null != datiRevoca.getTipoRevoca()){ 
			tipoRevoca = datiRevoca.getTipoRevoca().toString();
			if(!Utils.isValidTipoRevoca(tipoRevoca)){
				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: TipoRevoca non valido: " + tipoRevoca;
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);

				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						"TipoRevoca non valido" + tipoRevoca);

				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
			
			
			//Controllo validità del tipo revoca rispetto all'esito della RT, sul valore CodiceEsitoPagamento (0 = Pagamento eseguito)
			if (tipoRevoca.equals(Constants.TIPOREVOCA.S_0.getValue()) || tipoRevoca.equals(Constants.TIPOREVOCA.S_2.getValue())){
				if(null == rptRt.getCodRtDatiPagCodiceEsitoPagamento() || !rptRt.getCodRtDatiPagCodiceEsitoPagamento().equals("0")){
					parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: TipoRevoca non conforme alla RT: " + tipoRevoca;
					esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
					giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
							codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
							categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
							identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);

					FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
							"TipoRevoca non conforme alla RT" + tipoRevoca);

					rispostaRR.setEsito(esitoReq);
					rispostaRR.setFault(faultBean);
					return rispostaRR;
				}
			}
			
			
		}
		
		//Controlli singola revoca
		CtDatiSingolaRevoca[] arrSingolaRevoca = datiRevoca.getDatiSingolaRevocaArray();
		
		for (CtDatiSingolaRevoca singolaRevoca : arrSingolaRevoca){
			//sommo gli importi delle singole revoche per confrontarli successivamente con l'importo totale revocato
			sumImpSingRev = sumImpSingRev.add(singolaRevoca.getSingoloImportoRevocato());
			
			//creo lista delle singole revoche
			listaSingolaRevoca.add(singolaRevoca);
		}
		
		if (!sumImpSingRev.equals(datiRevoca.getImportoTotaleRevocato())) {
			LOG.info("paaInviaRichiestaRevoca: somma importoSingolaRevoca non coincide con importoTotaleRevocato");

			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: somma importoSingolaRevoca non coincide con importoTotaleRevocato";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio,
					identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
					tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);

			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
					"paaInviaRichiestaRevoca: somma importoSingolaRevoca non coincide con importoTotaleRevocato");

			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}
		
		//Controllo che gli l'importo totale della RR corrisponda con l'importo totale nella RT  
		if(!datiRevoca.getImportoTotaleRevocato().equals(rptRt.getNumRtDatiPagImportoTotalePagato())){
			LOG.info("paaInviaRichiestaRevoca: l'importo della revoca non coincide con l'importo della RT");

			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: l'importo della revoca non coincide con l'importo della RT";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio,
					identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
					tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);

			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
					"paaInviaRichiestaRevoca: l'importo della revoca non coincide con l'importo della RT");

			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}
		
		//Recupero lista dettaglio RT
		List<MygovRptRtDettaglio> listDettRt = manageRPTRTService.getByRptRt(rptRt);
		
		//Controllo che il numero del dettaglio pagamenti nella RR coincida con quelli presenti nel dettaglio della RT
		if (listaSingolaRevoca.size() != listDettRt.size()) {
			LOG.info("paaInviaRichiestaRevoca: il numero degli elementi presenti nel dettaglio della RR non coincide con il"
					+ "numero degli elementi presenti nel dettaglio della RT");
			
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: il numero degli elementi presenti nel dettaglio della RR non coincide con il"
					+ "numero degli elementi presenti nel dettaglio della RT";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			giornaleService.registraEvento(dataOraEvento, identificativoDominio,
					identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
					tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
					"paaInviaRichiestaRevoca: il numero degli elementi presenti nel dettaglio della RR non coincide con il"
					+ "numero degli elementi presenti nel dettaglio della RT");
			
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
		}
		
		
		Iterator<MygovRptRtDettaglio> iterDettRt =  listDettRt.iterator();
		for (CtDatiSingolaRevoca sngRR : listaSingolaRevoca){
			
			MygovRptRtDettaglio dettRt = iterDettRt.next();
			if(!dettRt.getNumRtDatiPagDatiSingPagSingoloImportoPagato().equals(sngRR.getSingoloImportoRevocato())){
				
				LOG.info("paaInviaRichiestaRevoca: importo dei singoli pagamenti della RT non coincide con i singoli importi revocati della RR");
				
				parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca: importo dei singoli pagamenti della RT non coincide con i singoli importi revocati della RR";
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
				
				FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SEMANTICA, "Errore semantico",
						"importo dei singoli pagamenti della RT non coincide con i singoli importi revocati della RR");
				
				rispostaRR.setEsito(esitoReq);
				rispostaRR.setFault(faultBean);
				return rispostaRR;
			}
		}
		
		
		//Se supero tutti i controlli inserisco in tabella 
		MygovRrEr myGovRevoca = null;
		try{
			
			//Costruisco il dto della lista dei dati singola revoca 
			List<RrErDettaglioDto> lstRrDettPagDto = this.buildRrErDettaglioDto(listaSingolaRevoca);
			
			myGovRevoca = manageRRService.insertRRWithRefresh(
					identificativoUnivocoVersamento, 
					codiceContestoPagamento, 
					identificativoDominio, 
					richiesta.getDominio().getIdentificativoStazioneRichiedente(), 
					richiesta.getIdentificativoMessaggioRevoca(), 
					richiesta.getVersioneOggetto(),
					richiesta.getDataOraMessaggioRevoca().getTime(), 
					istAttestante.getDenominazioneMittente(), 
					istAttestante.getCodiceUnitOperMittente(), 
					istAttestante.getDenomUnitOperMittente(), 
					istAttestante.getIndirizzoMittente(), 
					istAttestante.getCivicoMittente(), 
					istAttestante.getCapMittente(),
					istAttestante.getLocalitaMittente(), 
					istAttestante.getProvinciaMittente(), 
					istAttestante.getNazioneMittente(),
					istAttestante.getIdentificativoUnivocoMittente().getTipoIdentificativoUnivoco().toString(),
					istAttestante.getIdentificativoUnivocoMittente().getCodiceIdentificativoUnivoco(),
					null != sgVersante ? sgVersante.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString() : null,
					null != sgVersante ? sgVersante.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco() : null,
					null != sgVersante ? sgVersante.getAnagraficaVersante() : null,
					null != sgVersante ? sgVersante.getIndirizzoVersante() : null, 
					null != sgVersante ? sgVersante.getCivicoVersante() : null, 
					null != sgVersante ? sgVersante.getCapVersante() : null, 
					null != sgVersante ? sgVersante.getLocalitaVersante() : null, 
					null != sgVersante ? sgVersante.getProvinciaVersante() : null, 
					null != sgVersante ? sgVersante.getNazioneVersante() : null, 
					null != sgVersante ? sgVersante.getEMailVersante() : null, 
					sgPagatore.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString(),
					sgPagatore.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco(),
					sgPagatore.getAnagraficaPagatore(),
					sgPagatore.getIndirizzoPagatore(), 
					sgPagatore.getCivicoPagatore(), 
					sgPagatore.getCapPagatore(), 
					sgPagatore.getLocalitaPagatore(), 
					sgPagatore.getProvinciaPagatore(), 
					sgPagatore.getNazionePagatore(), 
					sgPagatore.getEMailPagatore(), 
					datiRevoca.getImportoTotaleRevocato(),
					datiRevoca.getIdentificativoUnivocoVersamento(),
					datiRevoca.getCodiceContestoPagamento(), 
					datiRevoca.getTipoRevoca().toString(), 
					lstRrDettPagDto);
		
		}catch (Exception e) {

			LOG.error("paaInviaRichiestaRevoca - Errore inserimento su tabella mygov_revoca: " + e.getMessage());
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca errore inserimento su tabella mygov_revoca: [" + e.getMessage() + "]";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			//Log nel giornale degli eventi - errore inserimento su tabella mygov_revoca
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore generico", "paaInviaRichiestaRevoca errore inserimento su tabella mygov_revoca");
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
			
		}
		
		// INVIA richiesta --->> PA
		PaaSILInviaRichiestaRevocaRisposta paaRisposta = null;
		//Imposto sottotipo risposta
		sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
		try{
			/**
			 * La componente di Back-end del FESP inoltra a sua volta la richiesta di revoca a PA
			 */
			PaaSILInviaRichiestaRevoca bodyrichiesta = this.buildBodyRichiesta(identificativoDominio, identificativoUnivocoVersamento, 
					codiceContestoPagamento, richiesta);
			
			paaRisposta = this.inviaRichiestaRevocaServiceClient.paaSILInviaRichiestaRevoca(bodyrichiesta);
			
			LOG.info("paaInviaRichiestaRevoca - Effettuata chiamata a paaSILInviaRichiestaRevoca");
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
			//Log nel giornale degli eventi - esito da paaSILInviaRichiestaRevoca
			
			String xmlString = "";
			try {
				it.veneto.regione.pagamenti.pa.papernodoregionale.inviarichiestarevoca.ObjectFactory objectFactory = new it.veneto.regione.pagamenti.pa.papernodoregionale.inviarichiestarevoca.ObjectFactory();
				JAXBContext context = JAXBContext.newInstance(PaaSILInviaRichiestaRevocaRisposta.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createPaaSILInviaRichiestaRevocaRisposta(paaRisposta), sw);
				xmlString = sw.toString();
			} catch (JAXBException jaxbex) {
				LOG.error("Errore deserializzazione Esito PaaSILInviaRichiestaRevocaRisposta");
			}

			parametriSpecificiInterfaccia = xmlString;
			
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			rispostaRR.setEsito(paaRisposta.getPaaSILInviaRichiestaRevocaRisposta().getEsito());
			
			if (null != paaRisposta.getPaaSILInviaRichiestaRevocaRisposta().getFault())
				rispostaRR.setFault(getFaultBean(paaRisposta.getPaaSILInviaRichiestaRevocaRisposta().getFault().getId(),
						paaRisposta.getPaaSILInviaRichiestaRevocaRisposta().getFault().getFaultCode(),
						paaRisposta.getPaaSILInviaRichiestaRevocaRisposta().getFault().getFaultString(),
						paaRisposta.getPaaSILInviaRichiestaRevocaRisposta().getFault().getDescription()));
			else
				rispostaRR.setFault(null);
			
			 
		} catch (Exception e) {
			
			LOG.error("paaInviaRichiestaRevoca - Errore chiamata paaSILInviaRichiestaRevoca");
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca errore chiamata paaSILInviaRichiestaRevoca: [" + e.getMessage() + "]";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			//Log nel giornale degli eventi - Errore chiamata paaSILInviaRichiestaRevoca
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
			
			FaultBean faultBean = this.getFaultBean(identificativoDominio, FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore generico", "paaInviaRichiestaRevoca errore chiamata paaSILInviaRichiestaRevoca: [" + e.getMessage() + "]");
			rispostaRR.setEsito(esitoReq);
			rispostaRR.setFault(faultBean);
			return rispostaRR;
 
		}
		
		//Aggiornamento risposta ricevuta da pa
		try{
			this.saveRRRisposta(paaRisposta, myGovRevoca.getMygovRrErId());
		}catch (Exception e) {
			
			LOG.error("paaInviaRichiestaRevoca - Errore update risposta su tabella mygov_revoca: " + e.getMessage());
			parametriSpecificiInterfaccia = "paaInviaRichiestaRevoca errore update risposta su tabella mygov_revoca: [" + e.getMessage() + "]";
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
			//Log nel giornale degli eventi - errore update risposta su tabella mygov_revoca
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
					esitoReq);
		}
		
		return rispostaRR;
	}
	
	/**
	 * 
	 * @param faultID
	 * @param faultCode
	 * @param faultString
	 * @param description
	 * @return
	 */
	private FaultBean getFaultBean(String faultID, String faultCode, String faultString, String description) {
			
		LOG.info("getFaultBean - faultCode: " + faultCode + " faultString: " + faultString + " description: " + description);
		FaultBean faultBean = new FaultBean();
		faultBean.setId(faultID);
		faultBean.setFaultCode(faultCode);
		faultBean.setFaultString(faultString);
		faultBean.setDescription(description);

		return faultBean;
	}
	
	/**
	 * 
	 * @param singola
	 * @return
	 */
	private List<RrErDettaglioDto> buildRrErDettaglioDto(List<CtDatiSingolaRevoca> singola){
		
		List<RrErDettaglioDto> listDto = new ArrayList<RrErDettaglioDto>();
		RrErDettaglioDto dto;
		
		for (CtDatiSingolaRevoca sng : singola){
			dto = new RrErDettaglioDto();
			
			dto.setCodRrDatiSingRevIdUnivocoRiscossione(sng.getIdentificativoUnivocoRiscossione());
			dto.setDeRrDatiSingRevCausaleRevoca(sng.getCausaleRevoca());
			dto.setDeRrDatiSingRevDatiAggiuntiviRevoca(sng.getDatiAggiuntiviRevoca());
			dto.setNumRrDatiSingRevSingoloImportoRevocato(sng.getSingoloImportoRevocato());
			
			listDto.add(dto);
		}
		
		return listDto;
	}
	
	/**
	 * 
	 * @param identificativoDominio
	 * @param identificativoUnivocoVersamento
	 * @param codiceContestoPagamento
	 * @param richiesta
	 * @return
	 */
	private PaaSILInviaRichiestaRevoca buildBodyRichiesta(String identificativoDominio, String identificativoUnivocoVersamento,
			String codiceContestoPagamento, CtRichiestaRevoca richiesta){
				
		PaaSILInviaRichiestaRevoca bodyRichiesta = new PaaSILInviaRichiestaRevoca();
		
		bodyRichiesta.setIdentificativoDominio(identificativoDominio);
		bodyRichiesta.setIdentificativoUnivocoVersamento(identificativoUnivocoVersamento);
		bodyRichiesta.setCodiceContestoPagamento(codiceContestoPagamento);
		
		//Costruisco byte[] arr della richiesta aggiornata
		byte[] arr = this.encodeRichiesta(richiesta);
		bodyRichiesta.setRr(arr); 
		
		return bodyRichiesta;
	}
	
	/**
	 * 
	 * @param tipoInviaRichiestaRevocaRisposta
	 * @param mygovRrId
	 */
	private void saveRRRisposta(PaaSILInviaRichiestaRevocaRisposta paaRisposta, Long mygovRrId) {

		it.veneto.regione.pagamenti.pa.papernodoregionale.inviarichiestarevoca.TipoInviaRichiestaRevocaRisposta rrRisposta = paaRisposta.getPaaSILInviaRichiestaRevocaRisposta();
		it.veneto.regione.pagamenti.pa.papernodoregionale.inviarichiestarevoca.FaultBean esitoFault = rrRisposta.getFault();
		String esito = rrRisposta.getEsito();

		String description = null;
		String faultCode = null;
		String faultString = null;
		String originalDescription = null;
		String originalFaultCode = null;
		String originalFaultString = null;
		String faultId = null;
		Integer faultSerial = null;

		if (null != esitoFault) {
			faultCode = esitoFault.getFaultCode();
			faultString = esitoFault.getFaultString();
			faultId = esitoFault.getId();
			description = esitoFault.getDescription();
			faultSerial = esitoFault.getSerial();
			originalDescription = esitoFault.getOriginalDescription();
			originalFaultCode = esitoFault.getOriginalFaultCode();
			originalFaultString = esitoFault.getOriginalFaultString();
		}

		this.manageRRService.updateRispostaRRById(mygovRrId, esito, faultCode, faultString, faultId, description, 
				faultSerial, originalDescription, originalFaultCode, originalFaultString);
	}
	
	/**
	 * 
	 * @param richiestaRevoca
	 * @return
	 */
	private byte[] encodeRichiesta(CtRichiestaRevoca richiestaRevoca) {
		try {
			
			byte[] byteRR;
			RRDocument encRichiesta = RRDocument.Factory.newInstance();
			encRichiesta.setRR(richiestaRevoca);
			byteRR = Base64.encodeBase64(encRichiesta.toString().getBytes("UTF-8"));

			return byteRR;
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Failed to encode CtRichiestaRevoca ::: UnsupportedEncodingException :::", uee);
		}
	}
	
	/**
	 * Se torna un risultato diverso da null, esiste una riga di revoca con le codizioni passate
	 * 
	 * @param identificativoDominio
	 * @param identificativoUnivocoVersamento
	 * @param codiceContestoPagamento
	 * @return
	 */
	private boolean esisteRevoca(String identificativoDominio,String identificativoUnivocoVersamento, String codiceContestoPagamento) {
		MygovRrEr revoca = manageRRService.checkRevoca(identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);
		if(revoca != null)
			return true;
		else
			return false;
	}
	
}
