package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaCarrelloRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaCarrelloRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRispostaRevoca;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRispostaRevocaRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoElementoListaRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoListaRPT;
import gov.telematici.pagamenti.ws.ppthead.IntestazioneCarrelloPPT;
import it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico;
import it.gov.digitpa.schemas.x2011.pagamenti.RPTDocument;
import it.regioneveneto.mygov.payment.constants.Constants;
import it.regioneveneto.mygov.payment.constants.FaultCodeConstants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RptRtDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRpt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRtDettaglio;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils.FespBean;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.NodoSILInviaRPRispostaException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.EnteService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.GiornaleService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageCarrelliRP_RPTService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPEService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPTRTService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.NodoInviaRPTService;
import it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient;
import it.regioneveneto.mygov.payment.utils.PropertiesUtil;
import it.regioneveneto.mygov.payment.utils.Utils;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ElementoRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediCopiaEsito;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediCopiaEsitoRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediSceltaWISP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediSceltaWISPRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRPRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRPRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT;
import it.veneto.regione.schemas.x2012.pagamenti.EsitoDocument;
import it.veneto.regione.schemas.x2012.pagamenti.RPDocument;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.Holder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.beans.factory.annotation.Autowired;

public class NodoInviaRPTServiceImpl implements NodoInviaRPTService {

	private static final Log log = LogFactory.getLog(NodoInviaRPTServiceImpl.class);

	private ManageRPTRTService manageRPTRTService;

	private RptRtDettaglioDao rptRtDettaglioDao;

	private PagamentiTelematiciRPTServiceClient pagamentiTelematiciRPTServiceClient;

	private FespBean fespProperties;

	private EnteService enteService;

	private ManageRPEService manageRPEService;

	private GiornaleService giornaleService;

	private ManageCarrelliRP_RPTService manageCarrelloRPService;
	
	@Autowired
	private PropertiesUtil propertiesUtil;

	

	public void setManageCarrelloRPService(ManageCarrelliRP_RPTService manageCarrelloRPService) {
		this.manageCarrelloRPService = manageCarrelloRPService;
	}

	public NodoInviaRPTServiceImpl() {
		super();
	}

	/**
	 * @param manageRPTRTService the manageRPTRTService to set
	 */
	public void setManageRPTRTService(ManageRPTRTService manageRPTRTService) {
		this.manageRPTRTService = manageRPTRTService;
	}

	public void setRptRtDettaglioDao(RptRtDettaglioDao rptRtDettaglioDao) {
		this.rptRtDettaglioDao = rptRtDettaglioDao;
	}

	/**
	 * @param pagamentiTelematiciRPTServiceClient the pagamentiTelematiciRPTServiceClient to set
	 */
	public void setPagamentiTelematiciRPTServiceClient(
			PagamentiTelematiciRPTServiceClient pagamentiTelematiciRPTServiceClient) {
		this.pagamentiTelematiciRPTServiceClient = pagamentiTelematiciRPTServiceClient;
	}

	public void setFespProperties(FespBean fespProperties) {
		this.fespProperties = fespProperties;
	}

	public void setEnteService(EnteService enteService) {
		this.enteService = enteService;
	}

	public void setManageRPEService(ManageRPEService manageRPEService) {
		this.manageRPEService = manageRPEService;
	}

	public void setGiornaleService(GiornaleService giornaleService) {
		this.giornaleService = giornaleService;
	}

	public NodoSILInviaRPRisposta nodoSILInviaRP(NodoSILInviaRP bodyrichiesta,
			it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT header)
			throws NodoSILInviaRPRispostaException {

		NodoInviaRPTRisposta _rispostaRPT = null;
		NodoSILInviaRPRisposta _rispostaRP = null;

		//generare l'informazione idSession come UUID
		UUID id_session = UUID.randomUUID();

		MygovRptRt mygovRptRt = null;
		MygovRpE mygovRpE = null;
		try {
			//			SALVA DB RP
			it.veneto.regione.schemas.x2012.pagamenti.CtRichiestaPagamento rp = decodificaRp(bodyrichiesta);

			mygovRpE = saveRp(header, bodyrichiesta, rp);

			//        	COSTRUISCI RPT
			//Prendo dati mancanti da tabella ente
			String idDominio = rp.getDominio().getIdentificativoDominio();
			MygovEnte enteProp = enteService.getByCodiceFiscale(idDominio);

			it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico CtRPT = this.buildRPT(rp, enteProp);
			gov.telematici.pagamenti.ws.ppthead.IntestazionePPT _paaSILInviaRPT_header = this.buildHeaderRPT(header, enteProp);
			NodoInviaRPT _paaSILInviaRPT_body = this.buildBodyRPT(CtRPT, bodyrichiesta);

			//'TipoFirma' e stata tolta da RP prendi da tabella ente

			/*
			if (StringUtils.isBlank(enteProp.getDeRpInviarpTipoFirma())) {
			        _paaSILInviaRPT_body.setTipoFirma("");
			}else {
			        _paaSILInviaRPT_body.setTipoFirma(enteProp.getDeRpInviarpTipoFirma());
			}
			*/

			//Tipo Firma RPT (dichiarazione di tipo firma dell RPT inviato dal nodo regionale)
			//_paaSILInviaRPT_body.setTipoFirma(StFirmaRicevuta.Enum.forInt(StFirmaRicevuta.INT_X_0).toString());

			//per poste questo campo e' obbligatorio e vuoto
			_paaSILInviaRPT_body.setTipoFirma("");

			//        	SALVA RPT
			mygovRptRt = saveRPT(_paaSILInviaRPT_header, _paaSILInviaRPT_body, CtRPT, mygovRpE.getMygovRpEId(),
					bodyrichiesta.getModelloPagamento());

			// se modello 2 o 3 prendo in carico l'invio RPT e torno subito OK
			if (bodyrichiesta.getModelloPagamento() == 4
					|| StringUtils.isNotBlank(mygovRpE.getDeRpDatiVersIbanAddebito())) {

				log.debug("workaraund attiva per idSession: [" + id_session + "]");

				_rispostaRP = new NodoSILInviaRPRisposta();
				_rispostaRP.setEsito("OK");
				_rispostaRP.setRedirect(1);

				it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean err = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
				_rispostaRP.setFault(err);

			} else {
				//altrimenti invio adesso RPT

				_rispostaRPT = nodoInviaRPT(mygovRptRt.getMygovRptRtId(), _paaSILInviaRPT_header, _paaSILInviaRPT_body);

				//        	COSTRUISCI RISPOSTA RP
				_rispostaRP = builtRispostaRP(_rispostaRPT, id_session);
			}

		} catch (java.lang.Exception ex) {
			log.error(FaultCodeConstants.PPT_ESITO_SCONOSCIUTO + ": [" + ex.getMessage() + "]", ex);

			//non e' modello  2 ne modello 3 (in quei casi non c'e' risposta da salvare e non la costruisco)
			if (mygovRpE != null && bodyrichiesta.getModelloPagamento() != 4
					&& StringUtils.isBlank(mygovRpE.getDeRpDatiVersIbanAddebito())) {
				//RISPOSTA RPT
				_rispostaRPT = new NodoInviaRPTRisposta();
				_rispostaRPT.setEsito("KO");

				gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean faultRPT = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean();
				faultRPT.setFaultCode(FaultCodeConstants.PPT_ESITO_SCONOSCIUTO);
				faultRPT.setDescription(ex.getMessage());
				_rispostaRPT.setFault(faultRPT);
			}

			//RISPOSTA RP
			_rispostaRP = new NodoSILInviaRPRisposta();
			_rispostaRP.setEsito("KO");

			it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean faultRP = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
			faultRP.setFaultCode(FaultCodeConstants.PPT_ESITO_SCONOSCIUTO);
			faultRP.setDescription(ex.getMessage());
			_rispostaRP.setFault(faultRP);
		}

		finally {

			try {
				if (mygovRpE != null) {

					//	        	SALVA DB RISPOSTA RPT
					//non e' modello  2 ne modello 3 (in quei casi non c'e' risposta da salvare)
					if (bodyrichiesta.getModelloPagamento() != 4 && StringUtils.isBlank(mygovRpE.getDeRpDatiVersIbanAddebito())
							&& mygovRptRt != null) {
						saveRPTRisposta(_rispostaRPT, mygovRptRt.getMygovRptRtId());
					}

					//        	SALVA DB RISPOSTA RP  
					saveRpRisposta(mygovRpE.getMygovRpEId(), _rispostaRP, id_session);
				}
			} catch (Exception e) {
				log.error("Error saving RP risposta: [" + e.getMessage() + "]", e);
			} finally {
				//se errore nel salvataggio (invio preso in carico) RP o RPT e (modello 2 o 3) ROLLBACK per permettere risottomissione
				if (_rispostaRP.getEsito().equals("KO") && (bodyrichiesta.getModelloPagamento() == 4
						|| StringUtils.isNotBlank(mygovRpE.getDeRpDatiVersIbanAddebito()))) {
					// rollback
					log.error("Rollback invia RPT con IUV [" + header.getIdentificativoUnivocoVersamento()
							+ "] esito KO e CCP [" + header.getCodiceContestoPagamento() + "]");
					NodoSILInviaRPRispostaException nodoSILInviaRPRispostaException = new NodoSILInviaRPRispostaException(
							"Rollback invia RPT con IUV [" + header.getIdentificativoUnivocoVersamento()
									+ "] esito KO e CCP [" + header.getCodiceContestoPagamento() + "]");

					nodoSILInviaRPRispostaException.setNodoSILInviaRPRisposta(_rispostaRP);
					throw nodoSILInviaRPRispostaException;
				}
			}
		}

		// commit
		return _rispostaRP;
	}

	@Override
	public gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPTRisposta nodoInviaRPT(Long mygovRptRtId, 
			gov.telematici.pagamenti.ws.ppthead.IntestazionePPT _paaSILInviaRPT_header,
			gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPT _paaSILInviaRPT_body) throws UnsupportedEncodingException, MalformedURLException {

		/*
		 * LOG GIORNALE DEGLI EVENTI
		 */
		Date dataOraEvento;
		String identificativoDominio;
		String identificativoUnivocoVersamento;
		String codiceContestoPagamento;
		String identificativoPrestatoreServiziPagamento;
		String tipoVersamento;
		String componente;
		String categoriaEvento;
		String tipoEvento;
		String sottoTipoEvento;
		String identificativoFruitore;
		String identificativoErogatore;
		String identificativoStazioneIntermediarioPa;
		String canalePagamento;
		String xmlString;
		JAXBContext context;
		String parametriSpecificiInterfaccia;
		String esitoReq;
		try {
			dataOraEvento = new Date();
			identificativoDominio = _paaSILInviaRPT_header.getIdentificativoDominio();
			identificativoUnivocoVersamento = _paaSILInviaRPT_header.getIdentificativoUnivocoVersamento();
			codiceContestoPagamento = _paaSILInviaRPT_header.getCodiceContestoPagamento();
			identificativoPrestatoreServiziPagamento = _paaSILInviaRPT_body.getIdentificativoPSP();
			tipoVersamento = null;
			componente = Constants.COMPONENTE.FESP.toString();
			categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoInviaRPT.toString();
			sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();

			identificativoFruitore = fespProperties.getIdentificativoStazioneIntermediarioPa();
			identificativoErogatore = Constants.NODO_DEI_PAGAMENTI_SPC;
			identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
			canalePagamento = _paaSILInviaRPT_body.getIdentificativoCanale();

			xmlString = "";
			try {
				gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory();
				context = JAXBContext.newInstance(NodoInviaRPT.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createNodoInviaRPT(_paaSILInviaRPT_body), sw);
				xmlString = sw.toString();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage());
			}

			parametriSpecificiInterfaccia = xmlString;

			esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
		} catch (Exception e1) {
			log.warn("nodoInviaRPT REQUEST impossibile inserire nel giornale degli eventi", e1);
		}

		//        	INVIA RPT --->> FESP			
		NodoInviaRPTRisposta _rispostaRPT = pagamentiTelematiciRPTServiceClient.nodoInviaRPT(_paaSILInviaRPT_header,
				_paaSILInviaRPT_body);

		/*
		 * LOG GIORNALE DEGLI EVENTI
		 */
		try {
			dataOraEvento = new Date();
			identificativoDominio = _paaSILInviaRPT_header.getIdentificativoDominio();
			identificativoUnivocoVersamento = _paaSILInviaRPT_header.getIdentificativoUnivocoVersamento();
			codiceContestoPagamento = _paaSILInviaRPT_header.getCodiceContestoPagamento();
			identificativoPrestatoreServiziPagamento = _paaSILInviaRPT_body.getIdentificativoPSP();
			tipoVersamento = null;
			componente = Constants.COMPONENTE.FESP.toString();
			categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoInviaRPT.toString();
			sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();

			identificativoFruitore = fespProperties.getIdentificativoStazioneIntermediarioPa();
			identificativoErogatore = Constants.NODO_DEI_PAGAMENTI_SPC;
			identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
			canalePagamento = _paaSILInviaRPT_body.getIdentificativoCanale();

			xmlString = "";
			try {
				gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory();
				context = JAXBContext.newInstance(NodoInviaRPTRisposta.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createNodoInviaRPTRisposta(_rispostaRPT), sw);
				xmlString = sw.toString();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage());
			}

			parametriSpecificiInterfaccia = xmlString;

			esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
		} catch (Exception e) {
			log.warn("nodoInviaRPT RESPONSE impossibile inserire nel giornale degli eventi", e);
		}

		return _rispostaRPT;
	}

	public void saveRPTRisposta(NodoInviaRPTRisposta _rispostaRPT, Long mygovRptRtId)
			throws UnsupportedEncodingException, MalformedURLException {
		manageRPTRTService.updateRispostaRptById(mygovRptRtId, _rispostaRPT.getEsito(), _rispostaRPT.getRedirect(),
				_rispostaRPT.getUrl(),
				((_rispostaRPT.getFault() != null) ? _rispostaRPT.getFault().getFaultCode() : null),
				((_rispostaRPT.getFault() != null) ? _rispostaRPT.getFault().getFaultString() : null),
				((_rispostaRPT.getFault() != null) ? _rispostaRPT.getFault().getId() : null),
				((_rispostaRPT.getFault() != null) ? _rispostaRPT.getFault().getDescription() : null),
				((_rispostaRPT.getFault() != null) ? _rispostaRPT.getFault().getSerial() : null),
				((_rispostaRPT.getFault() != null) ? _rispostaRPT.getFault().getOriginalFaultCode() : null),
				((_rispostaRPT.getFault() != null) ? _rispostaRPT.getFault().getOriginalFaultString() : null),
				((_rispostaRPT.getFault() != null) ? _rispostaRPT.getFault().getOriginalDescription() : null)
				);
	}

	public gov.telematici.pagamenti.ws.ppthead.IntestazionePPT buildHeaderRPT(it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT header,
			MygovEnte enteProp) {
		gov.telematici.pagamenti.ws.ppthead.IntestazionePPT result = new gov.telematici.pagamenti.ws.ppthead.IntestazionePPT();

		result.setCodiceContestoPagamento(header.getCodiceContestoPagamento());
		result.setIdentificativoDominio(header.getIdentificativoDominio());
		//Questi dati sono stati tolti da RP.. prendere da tabella ente
		result.setIdentificativoIntermediarioPA(fespProperties.getIdentificativoIntermediarioPa());
		result.setIdentificativoStazioneIntermediarioPA(fespProperties.getIdentificativoStazioneIntermediarioPa());
		result.setIdentificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento());

		return result;
	}

	public gov.telematici.pagamenti.ws.ppthead.IntestazionePPT buildHeaderRPT(String codRptIdMessaggioRichiesta) {
		MygovRptRt mygovRptRt = manageRPTRTService.getRptByCodRptIdMessaggioRichiesta(codRptIdMessaggioRichiesta);

		gov.telematici.pagamenti.ws.ppthead.IntestazionePPT result = new gov.telematici.pagamenti.ws.ppthead.IntestazionePPT();

		result.setCodiceContestoPagamento(mygovRptRt.getCodRptInviarptCodiceContestoPagamento());
		result.setIdentificativoDominio(mygovRptRt.getCodRptInviarptIdDominio());
		result.setIdentificativoIntermediarioPA(mygovRptRt.getCodRptInviarptIdIntermediarioPa());
		result.setIdentificativoStazioneIntermediarioPA(mygovRptRt.getCodRptInviarptIdStazioneIntermediarioPa());
		result.setIdentificativoUnivocoVersamento(mygovRptRt.getCodRptInviarptIdUnivocoVersamento());

		return result;
	}

	public it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico buildRPT(
			it.veneto.regione.schemas.x2012.pagamenti.CtRichiestaPagamento rp, MygovEnte enteProp) {
		it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico ctRPT = it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico.Factory
				.newInstance();
		ctRPT.setVersioneOggetto(rp.getVersioneOggetto());

		it.gov.digitpa.schemas.x2011.pagamenti.CtDominio dominio = it.gov.digitpa.schemas.x2011.pagamenti.CtDominio.Factory
				.newInstance();
		dominio.setIdentificativoDominio(rp.getDominio().getIdentificativoDominio());

		if (rp.getDominio().getIdentificativoStazioneRichiedente() != null) {
			dominio.setIdentificativoStazioneRichiedente(rp.getDominio().getIdentificativoStazioneRichiedente());
		}

		ctRPT.setDominio(dominio);

		ctRPT.setIdentificativoMessaggioRichiesta(rp.getIdentificativoMessaggioRichiesta());
		ctRPT.setDataOraMessaggioRichiesta(rp.getDataOraMessaggioRichiesta());
		ctRPT.setAutenticazioneSoggetto(it.gov.digitpa.schemas.x2011.pagamenti.StAutenticazioneSoggetto.Enum
				.forString(rp.getAutenticazioneSoggetto().toString()));

		it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoVersante soggettoVersante = it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoVersante.Factory
				.newInstance();
		it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante = it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG.Factory
				.newInstance();

		if (rp.getSoggettoVersante() != null) {
			identificativoUnivocoVersante.setTipoIdentificativoUnivoco(
					it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersFG.Enum
							.forString(rp.getSoggettoVersante().getIdentificativoUnivocoVersante()
									.getTipoIdentificativoUnivoco().toString()));
			identificativoUnivocoVersante.setCodiceIdentificativoUnivoco(
					rp.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());
			soggettoVersante.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);
			soggettoVersante.setAnagraficaVersante(rp.getSoggettoVersante().getAnagraficaVersante());

			if (rp.getSoggettoVersante().getIndirizzoVersante() != null) {
				soggettoVersante.setIndirizzoVersante(rp.getSoggettoVersante().getIndirizzoVersante());
			}

			if (rp.getSoggettoVersante().getCivicoVersante() != null) {
				soggettoVersante.setCivicoVersante(rp.getSoggettoVersante().getCivicoVersante());
			}

			if (rp.getSoggettoVersante().getCapVersante() != null) {
				soggettoVersante.setCapVersante(rp.getSoggettoVersante().getCapVersante());
			}

			if (rp.getSoggettoVersante().getLocalitaVersante() != null) {
				soggettoVersante.setLocalitaVersante(rp.getSoggettoVersante().getLocalitaVersante());
			}

			if (rp.getSoggettoVersante().getProvinciaVersante() != null) {
				soggettoVersante.setProvinciaVersante(rp.getSoggettoVersante().getProvinciaVersante());
			}

			if (rp.getSoggettoVersante().getNazioneVersante() != null) {
				soggettoVersante.setNazioneVersante(rp.getSoggettoVersante().getNazioneVersante());
			}

			if (rp.getSoggettoVersante().getEMailVersante() != null) {
				soggettoVersante.setEMailVersante(rp.getSoggettoVersante().getEMailVersante());
			}

			ctRPT.setSoggettoVersante(soggettoVersante);
		}

		it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoPagatore soggettoPagatore = it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoPagatore.Factory
				.newInstance();
		it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore = it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG.Factory
				.newInstance();
		identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(
				it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersFG.Enum
						.forString(rp.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
								.getTipoIdentificativoUnivoco().toString()));
		identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(
				rp.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());
		soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
		soggettoPagatore.setAnagraficaPagatore(rp.getSoggettoPagatore().getAnagraficaPagatore());

		if (rp.getSoggettoPagatore().getIndirizzoPagatore() != null) {
			soggettoPagatore.setIndirizzoPagatore(rp.getSoggettoPagatore().getIndirizzoPagatore());
		}

		if (rp.getSoggettoPagatore().getCivicoPagatore() != null) {
			soggettoPagatore.setCivicoPagatore(rp.getSoggettoPagatore().getCivicoPagatore());
		}

		if (rp.getSoggettoPagatore().getCapPagatore() != null) {
			soggettoPagatore.setCapPagatore(rp.getSoggettoPagatore().getCapPagatore());
		}

		if (rp.getSoggettoPagatore().getLocalitaPagatore() != null) {
			soggettoPagatore.setLocalitaPagatore(rp.getSoggettoPagatore().getLocalitaPagatore());
		}

		if (rp.getSoggettoPagatore().getProvinciaPagatore() != null) {
			soggettoPagatore.setProvinciaPagatore(rp.getSoggettoPagatore().getProvinciaPagatore());
		}

		if (rp.getSoggettoPagatore().getNazionePagatore() != null) {
			soggettoPagatore.setNazionePagatore(rp.getSoggettoPagatore().getNazionePagatore());
		}

		if (rp.getSoggettoPagatore().getEMailPagatore() != null) {
			soggettoPagatore.setEMailPagatore(rp.getSoggettoPagatore().getEMailPagatore());
		}

		ctRPT.setSoggettoPagatore(soggettoPagatore);

		//l'ente sull'rp nn c'e' (prendere da tabella ente)
		it.gov.digitpa.schemas.x2011.pagamenti.CtEnteBeneficiario enteBeneficiario = it.gov.digitpa.schemas.x2011.pagamenti.CtEnteBeneficiario.Factory
				.newInstance();
		it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaG idUnivocoPersonaG = it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaG.Factory
				.newInstance();
		idUnivocoPersonaG.setTipoIdentificativoUnivoco(
				it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersG.Enum
						.forString(enteProp.getCodRpEnteBenefIdUnivBenefTipoIdUnivoco()));
		idUnivocoPersonaG.setCodiceIdentificativoUnivoco(enteProp.getCodRpEnteBenefIdUnivBenefCodiceIdUnivoco());
		enteBeneficiario.setIdentificativoUnivocoBeneficiario(idUnivocoPersonaG);
		enteBeneficiario.setDenominazioneBeneficiario(enteProp.getDeRpEnteBenefDenominazioneBeneficiario());

		if (enteProp.getDeRpEnteBenefDenomUnitOperBeneficiario() != null) {
			enteBeneficiario.setDenomUnitOperBeneficiario(enteProp.getDeRpEnteBenefDenomUnitOperBeneficiario());
		}

		if (enteProp.getCodRpEnteBenefCodiceUnitOperBeneficiario() != null) {
			enteBeneficiario.setCodiceUnitOperBeneficiario(enteProp.getCodRpEnteBenefCodiceUnitOperBeneficiario());
		}

		if (enteProp.getDeRpEnteBenefLocalitaBeneficiario() != null) {
			enteBeneficiario.setLocalitaBeneficiario(enteProp.getDeRpEnteBenefLocalitaBeneficiario());
		}

		if (enteProp.getDeRpEnteBenefProvinciaBeneficiario() != null) {
			enteBeneficiario.setProvinciaBeneficiario(enteProp.getDeRpEnteBenefProvinciaBeneficiario());
		}

		if (enteProp.getDeRpEnteBenefIndirizzoBeneficiario() != null) {
			enteBeneficiario.setIndirizzoBeneficiario(enteProp.getDeRpEnteBenefIndirizzoBeneficiario());
		}

		if (enteProp.getDeRpEnteBenefCivicoBeneficiario() != null) {
			enteBeneficiario.setCivicoBeneficiario(enteProp.getDeRpEnteBenefCivicoBeneficiario());
		}

		if (enteProp.getCodRpEnteBenefCapBeneficiario() != null) {
			enteBeneficiario.setCapBeneficiario(enteProp.getCodRpEnteBenefCapBeneficiario());
		}

		if (enteProp.getCodRpEnteBenefNazioneBeneficiario() != null) {
			enteBeneficiario.setNazioneBeneficiario(enteProp.getCodRpEnteBenefNazioneBeneficiario());
		}

		ctRPT.setEnteBeneficiario(enteBeneficiario);

		it.gov.digitpa.schemas.x2011.pagamenti.CtDatiVersamentoRPT datiVersamento = it.gov.digitpa.schemas.x2011.pagamenti.CtDatiVersamentoRPT.Factory
				.newInstance();
		datiVersamento.setDataEsecuzionePagamento(rp.getDatiVersamento().getDataEsecuzionePagamento());
		datiVersamento.setImportoTotaleDaVersare(rp.getDatiVersamento().getImportoTotaleDaVersare());
		datiVersamento.setTipoVersamento(it.gov.digitpa.schemas.x2011.pagamenti.StTipoVersamento.Enum
				.forString(rp.getDatiVersamento().getTipoVersamento().toString()));
		datiVersamento.setIdentificativoUnivocoVersamento(rp.getDatiVersamento().getIdentificativoUnivocoVersamento());
		datiVersamento.setCodiceContestoPagamento(rp.getDatiVersamento().getCodiceContestoPagamento());

		if (rp.getDatiVersamento().getIbanAddebito() != null) {
			datiVersamento.setIbanAddebito(rp.getDatiVersamento().getIbanAddebito());
		}

		if (rp.getDatiVersamento().getBicAddebito() != null) {
			datiVersamento.setBicAddebito(rp.getDatiVersamento().getBicAddebito());
		}

		//Tipo Firma richiesta per la RT (preso dalla tabella ente)
		datiVersamento.setFirmaRicevuta(it.gov.digitpa.schemas.x2011.pagamenti.StFirmaRicevuta.Enum
				.forString(enteProp.getCodRpDatiVersFirmaRicevuta()));

		//SINGOLI VERSAMENTI		
		it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT[] datiSingoloVersamentoArray = new it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT[rp
				.getDatiVersamento().getDatiSingoloVersamentoArray().length];

		for (int i = 0; i < rp.getDatiVersamento().getDatiSingoloVersamentoArray().length; i++) {
			it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloVersamentoRP tmpVer = rp.getDatiVersamento()
					.getDatiSingoloVersamentoArray(i);
			it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT datiSingoloVersamento = it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT.Factory
					.newInstance();
			datiSingoloVersamento.setImportoSingoloVersamento(tmpVer.getImportoSingoloVersamento());

			if (tmpVer.getCommissioneCaricoPA() != null) {
				datiSingoloVersamento.setCommissioneCaricoPA(tmpVer.getCommissioneCaricoPA());
			}

			if (tmpVer.getIbanAccredito() != null) {
				datiSingoloVersamento.setIbanAccredito(tmpVer.getIbanAccredito());
			}

			if (tmpVer.getBicAccredito() != null) {
				datiSingoloVersamento.setBicAccredito(tmpVer.getBicAccredito());
			}

			if (tmpVer.getIbanAppoggio() != null) {
				datiSingoloVersamento.setIbanAppoggio(tmpVer.getIbanAppoggio());
			}

			if (tmpVer.getBicAppoggio() != null) {
				datiSingoloVersamento.setBicAppoggio(tmpVer.getBicAppoggio());
			}

			if (tmpVer.getCredenzialiPagatore() != null) {
				datiSingoloVersamento.setCredenzialiPagatore(tmpVer.getCredenzialiPagatore());
			}

			if (tmpVer.isSetDatiMarcaBolloDigitale()) {
				it.gov.digitpa.schemas.x2011.pagamenti.CtDatiMarcaBolloDigitale newMarcaBolloDigitale = it.gov.digitpa.schemas.x2011.pagamenti.CtDatiMarcaBolloDigitale.Factory
						.newInstance();

				newMarcaBolloDigitale.setTipoBollo(it.gov.digitpa.schemas.x2011.pagamenti.StTipoBollo.Enum
						.forString(tmpVer.getDatiMarcaBolloDigitale().getTipoBollo().toString()));
				newMarcaBolloDigitale.setHashDocumento(tmpVer.getDatiMarcaBolloDigitale().getHashDocumento());
				newMarcaBolloDigitale.setProvinciaResidenza(tmpVer.getDatiMarcaBolloDigitale().getProvinciaResidenza());

				datiSingoloVersamento.setDatiMarcaBolloDigitale(newMarcaBolloDigitale);
			}

			datiSingoloVersamento.setCausaleVersamento(tmpVer.getCausaleVersamento());
			datiSingoloVersamento.setDatiSpecificiRiscossione(tmpVer.getDatiSpecificiRiscossione());
			datiSingoloVersamentoArray[i] = datiSingoloVersamento;
		}

		datiVersamento.setDatiSingoloVersamentoArray(datiSingoloVersamentoArray);
		ctRPT.setDatiVersamento(datiVersamento);

		return ctRPT;
	}

	public CtRichiestaPagamentoTelematico buildRPT(String codRptIdMessaggioRichiesta) {
		MygovRptRt mygovRptRt = manageRPTRTService.getRptByCodRptIdMessaggioRichiesta(codRptIdMessaggioRichiesta);
		List<MygovRptRtDettaglio> mygovRptRtDettaglios = rptRtDettaglioDao.getByRptRt(mygovRptRt);

		it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico ctRPT = it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico.Factory
				.newInstance();

		ctRPT.setVersioneOggetto(mygovRptRt.getDeRptVersioneOggetto());

		it.gov.digitpa.schemas.x2011.pagamenti.CtDominio dominio = it.gov.digitpa.schemas.x2011.pagamenti.CtDominio.Factory
				.newInstance();
		dominio.setIdentificativoDominio(mygovRptRt.getCodRptDomIdDominio());

		if (mygovRptRt.getCodRptDomIdStazioneRichiedente() != null) {
			dominio.setIdentificativoStazioneRichiedente(mygovRptRt.getCodRptDomIdStazioneRichiedente());
		}

		ctRPT.setDominio(dominio);

		ctRPT.setIdentificativoMessaggioRichiesta(mygovRptRt.getCodRptIdMessaggioRichiesta());

		GDateBuilder builder = new GDateBuilder(mygovRptRt.getDtRptDataOraMessaggioRichiesta());
		builder.clearTimeZone();
		Calendar dtRptDataOraMessaggioRichiesta = builder.getCalendar();

		ctRPT.setDataOraMessaggioRichiesta(dtRptDataOraMessaggioRichiesta);

		ctRPT.setAutenticazioneSoggetto(it.gov.digitpa.schemas.x2011.pagamenti.StAutenticazioneSoggetto.Enum
				.forString(mygovRptRt.getCodRptAutenticazioneSoggetto()));

		it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoVersante soggettoVersante = it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoVersante.Factory
				.newInstance();
		it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante = it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG.Factory
				.newInstance();

		if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco())) {

			identificativoUnivocoVersante.setTipoIdentificativoUnivoco(
					it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersFG.Enum
							.forString(mygovRptRt.getCodRptSoggVersIdUnivVersTipoIdUnivoco()));
			identificativoUnivocoVersante
					.setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco());

			soggettoVersante.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);

			soggettoVersante.setAnagraficaVersante(mygovRptRt.getDeRptSoggVersAnagraficaVersante());

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersIndirizzoVersante())) {
				soggettoVersante.setIndirizzoVersante(mygovRptRt.getDeRptSoggVersIndirizzoVersante());
			}

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersCivicoVersante())) {
				soggettoVersante.setCivicoVersante(mygovRptRt.getDeRptSoggVersCivicoVersante());
			}

			if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggVersCapVersante())) {
				soggettoVersante.setCapVersante(mygovRptRt.getCodRptSoggVersCapVersante());
			}

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersLocalitaVersante())) {
				soggettoVersante.setLocalitaVersante(mygovRptRt.getDeRptSoggVersLocalitaVersante());
			}

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersProvinciaVersante())) {
				soggettoVersante.setProvinciaVersante(mygovRptRt.getDeRptSoggVersProvinciaVersante());
			}

			if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggVersNazioneVersante())) {
				soggettoVersante.setNazioneVersante(mygovRptRt.getCodRptSoggVersNazioneVersante());
			}

			if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggVersEmailVersante())) {
				soggettoVersante.setEMailVersante(mygovRptRt.getDeRptSoggVersEmailVersante());
			}

			ctRPT.setSoggettoVersante(soggettoVersante);
		}

		it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoPagatore soggettoPagatore = it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoPagatore.Factory
				.newInstance();
		it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore = it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG.Factory
				.newInstance();

		identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(
				it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersFG.Enum
						.forString(mygovRptRt.getCodRptSoggPagIdUnivPagTipoIdUnivoco()));
		identificativoUnivocoPagatore
				.setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco());
		soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
		soggettoPagatore.setAnagraficaPagatore(mygovRptRt.getDeRptSoggPagAnagraficaPagatore());

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagIndirizzoPagatore())) {
			soggettoPagatore.setIndirizzoPagatore(mygovRptRt.getDeRptSoggPagIndirizzoPagatore());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagCivicoPagatore())) {
			soggettoPagatore.setCivicoPagatore(mygovRptRt.getDeRptSoggPagCivicoPagatore());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggPagCapPagatore())) {
			soggettoPagatore.setCapPagatore(mygovRptRt.getCodRptSoggPagCapPagatore());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagLocalitaPagatore())) {
			soggettoPagatore.setLocalitaPagatore(mygovRptRt.getDeRptSoggPagLocalitaPagatore());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagProvinciaPagatore())) {
			soggettoPagatore.setProvinciaPagatore(mygovRptRt.getDeRptSoggPagProvinciaPagatore());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggPagNazionePagatore())) {
			soggettoPagatore.setNazionePagatore(mygovRptRt.getCodRptSoggPagNazionePagatore());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptSoggPagEmailPagatore())) {
			soggettoPagatore.setEMailPagatore(mygovRptRt.getDeRptSoggPagEmailPagatore());
		}

		ctRPT.setSoggettoPagatore(soggettoPagatore);

		it.gov.digitpa.schemas.x2011.pagamenti.CtEnteBeneficiario enteBeneficiario = it.gov.digitpa.schemas.x2011.pagamenti.CtEnteBeneficiario.Factory
				.newInstance();

		it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaG idUnivocoPersonaG = it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaG.Factory
				.newInstance();

		idUnivocoPersonaG.setTipoIdentificativoUnivoco(
				it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersG.Enum
						.forString(mygovRptRt.getCodRptEnteBenefIdUnivBenefTipoIdUnivoco()));
		idUnivocoPersonaG.setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptEnteBenefIdUnivBenefCodiceIdUnivoco());
		enteBeneficiario.setIdentificativoUnivocoBeneficiario(idUnivocoPersonaG);

		enteBeneficiario.setDenominazioneBeneficiario(mygovRptRt.getDeRptEnteBenefDenominazioneBeneficiario());

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptEnteBenefDenomUnitOperBeneficiario())) {
			enteBeneficiario.setDenomUnitOperBeneficiario(mygovRptRt.getDeRptEnteBenefDenomUnitOperBeneficiario());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getCodRptEnteBenefCodiceUnitOperBeneficiario())) {
			enteBeneficiario.setCodiceUnitOperBeneficiario(mygovRptRt.getCodRptEnteBenefCodiceUnitOperBeneficiario());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptEnteBenefLocalitaBeneficiario())) {
			enteBeneficiario.setLocalitaBeneficiario(mygovRptRt.getDeRptEnteBenefLocalitaBeneficiario());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptEnteBenefProvinciaBeneficiario())) {
			enteBeneficiario.setProvinciaBeneficiario(mygovRptRt.getDeRptEnteBenefProvinciaBeneficiario());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptEnteBenefIndirizzoBeneficiario())) {
			enteBeneficiario.setIndirizzoBeneficiario(mygovRptRt.getDeRptEnteBenefIndirizzoBeneficiario());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptEnteBenefCivicoBeneficiario())) {
			enteBeneficiario.setCivicoBeneficiario(mygovRptRt.getDeRptEnteBenefCivicoBeneficiario());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getCodRptEnteBenefCapBeneficiario())) {
			enteBeneficiario.setCapBeneficiario(mygovRptRt.getCodRptEnteBenefCapBeneficiario());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getCodRptEnteBenefNazioneBeneficiario())) {
			enteBeneficiario.setNazioneBeneficiario(mygovRptRt.getCodRptEnteBenefNazioneBeneficiario());
		}

		ctRPT.setEnteBeneficiario(enteBeneficiario);

		it.gov.digitpa.schemas.x2011.pagamenti.CtDatiVersamentoRPT datiVersamento = it.gov.digitpa.schemas.x2011.pagamenti.CtDatiVersamentoRPT.Factory
				.newInstance();

		builder = new GDateBuilder(mygovRptRt.getDtRptDatiVersDataEsecuzionePagamento());
		builder.clearTimeZone();
		Calendar dtRptDatiVersDataEsecuzionePagamento = builder.getCalendar();

		datiVersamento.setDataEsecuzionePagamento(dtRptDatiVersDataEsecuzionePagamento);

		datiVersamento.setImportoTotaleDaVersare(mygovRptRt.getNumRptDatiVersImportoTotaleDaVersare());
		datiVersamento.setTipoVersamento(it.gov.digitpa.schemas.x2011.pagamenti.StTipoVersamento.Enum
				.forString(mygovRptRt.getCodRptDatiVersTipoVersamento()));
		datiVersamento.setIdentificativoUnivocoVersamento(mygovRptRt.getCodRptDatiVersIdUnivocoVersamento());
		datiVersamento.setCodiceContestoPagamento(mygovRptRt.getCodRptDatiVersCodiceContestoPagamento());

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptDatiVersIbanAddebito())) {
			datiVersamento.setIbanAddebito(mygovRptRt.getDeRptDatiVersIbanAddebito());
		}

		if (StringUtils.isNotBlank(mygovRptRt.getDeRptDatiVersBicAddebito())) {
			datiVersamento.setBicAddebito(mygovRptRt.getDeRptDatiVersBicAddebito());
		}
		//Tipo Firma richiesta per la RT (preso dalla tabella ente)
		datiVersamento.setFirmaRicevuta(it.gov.digitpa.schemas.x2011.pagamenti.StFirmaRicevuta.Enum
				.forString(mygovRptRt.getCodRptDatiVersFirmaRicevuta()));

		//SINGOLI VERSAMENTI		
		it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT[] datiSingoloVersamentoArray = new it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT[mygovRptRtDettaglios
				.size()];

		for (MygovRptRtDettaglio mygovRptRtDettaglio : mygovRptRtDettaglios) {

			it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT datiSingoloVersamento = it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT.Factory
					.newInstance();
			datiSingoloVersamento.setImportoSingoloVersamento(
					mygovRptRtDettaglio.getNumRptDatiVersDatiSingVersImportoSingoloVersamento());

			if (mygovRptRtDettaglio.getNumRptDatiVersDatiSingVersCommissioneCaricoPa() != null) {
				datiSingoloVersamento
						.setCommissioneCaricoPA(mygovRptRtDettaglio.getNumRptDatiVersDatiSingVersCommissioneCaricoPa());
			}

			if (StringUtils.isNotBlank(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersIbanAccredito())) {
				datiSingoloVersamento.setIbanAccredito(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersIbanAccredito());
			}

			if (StringUtils.isNotBlank(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersBicAccredito())) {
				datiSingoloVersamento.setBicAccredito(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersBicAccredito());
			}

			if (StringUtils.isNotBlank(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersIbanAppoggio())) {
				datiSingoloVersamento.setIbanAppoggio(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersIbanAppoggio());
			}

			if (StringUtils.isNotBlank(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersBicAppoggio())) {
				datiSingoloVersamento.setBicAppoggio(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersBicAppoggio());
			}

			if (StringUtils.isNotBlank(mygovRptRtDettaglio.getCodRptDatiVersDatiSingVersCredenzialiPagatore())) {
				datiSingoloVersamento
						.setCredenzialiPagatore(mygovRptRtDettaglio.getCodRptDatiVersDatiSingVersCredenzialiPagatore());
			}

			datiSingoloVersamento
					.setCausaleVersamento(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersCausaleVersamento());
			datiSingoloVersamento.setDatiSpecificiRiscossione(
					mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersDatiSpecificiRiscossione());
			datiSingoloVersamentoArray[mygovRptRtDettaglios.indexOf(mygovRptRtDettaglio)] = datiSingoloVersamento;
		}

		datiVersamento.setDatiSingoloVersamentoArray(datiSingoloVersamentoArray);
		ctRPT.setDatiVersamento(datiVersamento);

		return ctRPT;
	}

	public NodoInviaRPT buildBodyRPT(it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico CtRPT,
			NodoSILInviaRP bodyrichiesta) {
		byte[] byteRPT = encodeRPT(CtRPT);

		NodoInviaRPT result = new NodoInviaRPT();
		if (bodyrichiesta.getIdentificativoCanale() != null) {
			result.setIdentificativoCanale(bodyrichiesta.getIdentificativoCanale());
		}
		if (bodyrichiesta.getIdentificativoIntermediarioPSP() != null) {
			result.setIdentificativoIntermediarioPSP(bodyrichiesta.getIdentificativoIntermediarioPSP());
		}
		result.setIdentificativoPSP(bodyrichiesta.getIdentificativoPSP());
		result.setPassword(fespProperties.getPassword());
		result.setRpt(byteRPT);

		return result;
	}

	public NodoInviaRPT buildBodyRPT(it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico CtRPT,
			String codRptIdMessaggioRichiesta) {
		byte[] byteRPT = encodeRPT(CtRPT);

		MygovRptRt mygovRptRt = manageRPTRTService.getRptByCodRptIdMessaggioRichiesta(codRptIdMessaggioRichiesta);

		NodoInviaRPT result = new NodoInviaRPT();
		if (mygovRptRt.getCodRptInviarptIdCanale() != null) {
			result.setIdentificativoCanale(mygovRptRt.getCodRptInviarptIdCanale());
		}
		if (mygovRptRt.getCodRptInviarptIdIntermediarioPsp() != null) {
			result.setIdentificativoIntermediarioPSP(mygovRptRt.getCodRptInviarptIdIntermediarioPsp());
		}
		result.setIdentificativoPSP(mygovRptRt.getCodRptInviarptIdPsp());
		result.setPassword(fespProperties.getPassword());
		result.setRpt(byteRPT);

		return result;
	}

	private byte[] encodeRPT(it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico CtRPT) {
		byte[] byteRPT = null;

		RPTDocument rptDoc = RPTDocument.Factory.newInstance();
		rptDoc.setRPT(CtRPT);

		try {
			//byteRPT = Base64.encodeBase64(rptDoc.toString().getBytes("UTF-8"));
			byteRPT = (rptDoc.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException(
					"Failed to parse Ricevuta Pagamento Telematica ::: UnsupportedEncodingException :::", uee);
		}

		return byteRPT;
	}

	/**
	 * @param mygovRpEId
	 * @param _rispostaRP
	 * @param id_session
	 */
	private void saveRpRisposta(Long mygovRpEId, NodoSILInviaRPRisposta _rispostaRP, UUID id_session) {

		it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean esitoFault = _rispostaRP.getFault();

		manageRPEService.updateRispostaRpById(mygovRpEId, _rispostaRP.getEsito(), _rispostaRP.getRedirect(),
				_rispostaRP.getUrl(), esitoFault.getFaultCode(), esitoFault.getFaultString(), esitoFault.getId(),
				esitoFault.getDescription(), esitoFault.getSerial(), id_session.toString(), esitoFault.getOriginalFaultCode(),
				esitoFault.getOriginalFaultString(), esitoFault.getOriginalDescription());
	}

	private MygovRpE saveRp(it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT header,
			NodoSILInviaRP bodyrichiesta, it.veneto.regione.schemas.x2012.pagamenti.CtRichiestaPagamento rp) {
		List<it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RpEDettaglioDto> rpVersamenti = new ArrayList<it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RpEDettaglioDto>();

		it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloVersamentoRP[] versamenti = rp.getDatiVersamento()
				.getDatiSingoloVersamentoArray();

		for (int i = 0; i < versamenti.length; i++) {
			it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloVersamentoRP singoloVersamento = versamenti[i];
			it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RpEDettaglioDto rpVersamento = new it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RpEDettaglioDto();

			rpVersamento.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(
					singoloVersamento.getImportoSingoloVersamento());
			rpVersamento.setNumRpDatiVersDatiSingVersCommissioneCaricoPa(singoloVersamento.getCommissioneCaricoPA());
			rpVersamento.setCodRpDatiVersDatiSingVersIbanAccredito(singoloVersamento.getIbanAccredito());
			rpVersamento.setCodRpDatiVersDatiSingVersBicAccredito(singoloVersamento.getBicAccredito());
			rpVersamento.setCodRpDatiVersDatiSingVersIbanAppoggio(singoloVersamento.getIbanAppoggio());
			rpVersamento.setCodRpDatiVersDatiSingVersBicAppoggio(singoloVersamento.getBicAppoggio());
			rpVersamento.setCodRpDatiVersDatiSingVersCredenzialiPagatore(singoloVersamento.getCredenzialiPagatore());
			rpVersamento.setDeRpDatiVersDatiSingVersCausaleVersamento(singoloVersamento.getCausaleVersamento());
			rpVersamento.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(
					singoloVersamento.getDatiSpecificiRiscossione());

			rpVersamento.setNumEDatiPagDatiSingPagCommissioniApplicatePsp(null);
			rpVersamento.setCodEDatiPagDatiSingPagAllegatoRicevutaTipo(null);
			rpVersamento.setBlbEDatiPagDatiSingPagAllegatoRicevutaTest(null);

			if (singoloVersamento.isSetDatiMarcaBolloDigitale()) {
				rpVersamento.setCodRpDatiVersDatiSingVersDatiMbdTipoBollo(
						singoloVersamento.getDatiMarcaBolloDigitale().getTipoBollo().toString());
				rpVersamento.setCodRpDatiVersDatiSingVersDatiMbdHashDocumento(
						singoloVersamento.getDatiMarcaBolloDigitale().getHashDocumento());
				rpVersamento.setCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza(
						singoloVersamento.getDatiMarcaBolloDigitale().getProvinciaResidenza());
			}

			rpVersamenti.add(rpVersamento);
		}

		return manageRPEService.insertRPWithRefresh(bodyrichiesta.getIdentificativoPSP(),
				bodyrichiesta.getIdentificativoIntermediarioPSP(), bodyrichiesta.getIdentificativoCanale(),
				header.getIdentificativoDominio(), header.getIdentificativoUnivocoVersamento(),
				header.getCodiceContestoPagamento(), rp.getVersioneOggetto(),
				rp.getDominio().getIdentificativoDominio(), rp.getDominio().getIdentificativoStazioneRichiedente(),
				rp.getIdentificativoMessaggioRichiesta(), rp.getDataOraMessaggioRichiesta().getTime(),
				rp.getAutenticazioneSoggetto().toString(),
				((rp.getSoggettoVersante() != null) ? rp.getSoggettoVersante().getIdentificativoUnivocoVersante()
						.getTipoIdentificativoUnivoco().toString() : null),
				((rp.getSoggettoVersante() != null)
						? rp.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco()
						: null),
				((rp.getSoggettoVersante() != null) ? rp.getSoggettoVersante().getAnagraficaVersante() : null),
				((rp.getSoggettoVersante() != null) ? rp.getSoggettoVersante().getIndirizzoVersante() : null),
				((rp.getSoggettoVersante() != null) ? rp.getSoggettoVersante().getCivicoVersante() : null),
				((rp.getSoggettoVersante() != null) ? rp.getSoggettoVersante().getCapVersante() : null),
				((rp.getSoggettoVersante() != null) ? rp.getSoggettoVersante().getLocalitaVersante() : null),
				((rp.getSoggettoVersante() != null) ? rp.getSoggettoVersante().getProvinciaVersante() : null),
				((rp.getSoggettoVersante() != null) ? rp.getSoggettoVersante().getNazioneVersante() : null),
				((rp.getSoggettoVersante() != null) ? rp.getSoggettoVersante().getEMailVersante() : null),
				((rp.getSoggettoPagatore() != null) ? rp.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
						.getTipoIdentificativoUnivoco().toString() : null),
				((rp.getSoggettoPagatore() != null)
						? rp.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco()
						: null),
				((rp.getSoggettoPagatore() != null) ? rp.getSoggettoPagatore().getAnagraficaPagatore() : null),
				((rp.getSoggettoPagatore() != null) ? rp.getSoggettoPagatore().getIndirizzoPagatore() : null),
				((rp.getSoggettoPagatore() != null) ? rp.getSoggettoPagatore().getCivicoPagatore() : null),
				((rp.getSoggettoPagatore() != null) ? rp.getSoggettoPagatore().getCapPagatore() : null),
				((rp.getSoggettoPagatore() != null) ? rp.getSoggettoPagatore().getLocalitaPagatore() : null),
				((rp.getSoggettoPagatore() != null) ? rp.getSoggettoPagatore().getProvinciaPagatore() : null),
				((rp.getSoggettoPagatore() != null) ? rp.getSoggettoPagatore().getNazionePagatore() : null),
				((rp.getSoggettoPagatore() != null) ? rp.getSoggettoPagatore().getEMailPagatore() : null),
				((rp.getDatiVersamento() != null) ? rp.getDatiVersamento().getDataEsecuzionePagamento().getTime()
						: null),
				((rp.getDatiVersamento() != null) ? rp.getDatiVersamento().getImportoTotaleDaVersare() : null),
				((rp.getDatiVersamento() != null) ? rp.getDatiVersamento().getTipoVersamento().toString() : null),
				((rp.getDatiVersamento() != null) ? rp.getDatiVersamento().getIdentificativoUnivocoVersamento() : null),
				((rp.getDatiVersamento() != null) ? rp.getDatiVersamento().getCodiceContestoPagamento() : null),
				((rp.getDatiVersamento() != null) ? rp.getDatiVersamento().getIbanAddebito() : null),
				((rp.getDatiVersamento() != null) ? rp.getDatiVersamento().getBicAddebito() : null),
				bodyrichiesta.getModelloPagamento(), rpVersamenti);
	}

	private it.veneto.regione.schemas.x2012.pagamenti.CtRichiestaPagamento decodificaRp(NodoSILInviaRP bodyrichiesta) {
		byte[] byteRp = bodyrichiesta.getRp();

		RPDocument decRp;

		try {
			String rpString = Base64.isBase64(byteRp) ? new String(Base64.decodeBase64(byteRp), "UTF-8")
					: new String(byteRp, "UTF-8");

			log.debug("INVIO RP: " + rpString);

			/*
			XmlOptions options = new XmlOptions();
			List<String> errors = new ArrayList<String>();
			boolean validXml = false;
			options.setErrorListener(errors);
			*/
			decRp = RPDocument.Factory.parse(rpString);

			//validXml = decRp.validate(options);
			it.veneto.regione.schemas.x2012.pagamenti.CtRichiestaPagamento rp = decRp.getRP();

			return rp;
		} catch (XmlException xmle) {
			throw new RuntimeException("Failed to parse Ricevuta Pagamento ::: XmlException :::", xmle);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Failed to parse Ricevuta Pagamento ::: UnsupportedEncodingException :::", uee);
		}
	}

	private MygovRptRt saveRPT(gov.telematici.pagamenti.ws.ppthead.IntestazionePPT header,
			NodoInviaRPT _paaSILInviaRPT_body,
			it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico rpt, Long mygovRpEId,
			Integer modelloPagamento) {
		List<it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RptRtDettaglioDto> richiestaVersamenti = new ArrayList<it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RptRtDettaglioDto>();

		it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT[] versamenti = rpt.getDatiVersamento()
				.getDatiSingoloVersamentoArray();

		for (int i = 0; i < versamenti.length; i++) {
			it.gov.digitpa.schemas.x2011.pagamenti.CtDatiSingoloVersamentoRPT tmpVer = versamenti[i];
			it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RptRtDettaglioDto tmpRPTVer = new it.regioneveneto.mygov.payment.nodoregionalefesp.dto.RptRtDettaglioDto();

			tmpRPTVer.setNumRptDatiVersDatiSingVersImportoSingoloVersamento(tmpVer.getImportoSingoloVersamento());
			tmpRPTVer.setNumRptDatiVersDatiSingVersCommissioneCaricoPa(tmpVer.getCommissioneCaricoPA());
			tmpRPTVer.setDeRptDatiVersDatiSingVersIbanAccredito(tmpVer.getIbanAccredito());
			tmpRPTVer.setDeRptDatiVersDatiSingVersBicAccredito(tmpVer.getBicAccredito());
			tmpRPTVer.setDeRptDatiVersDatiSingVersIbanAppoggio(tmpVer.getIbanAppoggio());
			tmpRPTVer.setDeRptDatiVersDatiSingVersBicAppoggio(tmpVer.getBicAppoggio());
			tmpRPTVer.setCodRptDatiVersDatiSingVersCredenzialiPagatore(tmpVer.getCredenzialiPagatore());
			tmpRPTVer.setDeRptDatiVersDatiSingVersCausaleVersamento(tmpVer.getCausaleVersamento());
			tmpRPTVer.setDeRptDatiVersDatiSingVersDatiSpecificiRiscossione(tmpVer.getDatiSpecificiRiscossione());

			tmpRPTVer.setNumRtDatiPagDatiSingPagCommissioniApplicatePsp(null);
			tmpRPTVer.setCodRtDatiPagDatiSingPagAllegatoRicevutaTipo(null);
			tmpRPTVer.setBlbRtDatiPagDatiSingPagAllegatoRicevutaTest(null);

			if (tmpVer.isSetDatiMarcaBolloDigitale()) {
				tmpRPTVer.setCodRptDatiVersDatiSingVersDatiMbdTipoBollo(
						tmpVer.getDatiMarcaBolloDigitale().getTipoBollo().toString());
				tmpRPTVer.setCodRptDatiVersDatiSingVersDatiMbdHashDocumento(
						tmpVer.getDatiMarcaBolloDigitale().getHashDocumento());
				tmpRPTVer.setCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza(
						tmpVer.getDatiMarcaBolloDigitale().getProvinciaResidenza());
			}

			richiestaVersamenti.add(tmpRPTVer);
		}

		String soggVersanteTipoIdentificativoUnivocoVersante = null;
		String soggVersanteCodiceIdentificativoUnivoco = null;

		if ((rpt.getSoggettoVersante() != null)
				&& (rpt.getSoggettoVersante().getIdentificativoUnivocoVersante() != null)) {
			if (rpt.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() != null) {
				soggVersanteTipoIdentificativoUnivocoVersante = rpt.getSoggettoVersante()
						.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString();
			}

			soggVersanteCodiceIdentificativoUnivoco = rpt.getSoggettoVersante().getIdentificativoUnivocoVersante()
					.getCodiceIdentificativoUnivoco();
		}

		return manageRPTRTService.insertRpt(_paaSILInviaRPT_body.getPassword(),
				_paaSILInviaRPT_body.getIdentificativoPSP(), _paaSILInviaRPT_body.getIdentificativoIntermediarioPSP(),
				_paaSILInviaRPT_body.getIdentificativoCanale(), _paaSILInviaRPT_body.getTipoFirma(),
				header.getIdentificativoIntermediarioPA(), header.getIdentificativoStazioneIntermediarioPA(),
				header.getIdentificativoDominio(), header.getIdentificativoUnivocoVersamento(),
				header.getCodiceContestoPagamento(), rpt.getVersioneOggetto(),
				rpt.getDominio().getIdentificativoDominio(), rpt.getDominio().getIdentificativoStazioneRichiedente(),
				rpt.getIdentificativoMessaggioRichiesta(), rpt.getDataOraMessaggioRichiesta().getTime(),
				rpt.getAutenticazioneSoggetto().toString(), soggVersanteTipoIdentificativoUnivocoVersante,
				soggVersanteCodiceIdentificativoUnivoco,
				((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getAnagraficaVersante() : null),
				((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getIndirizzoVersante() : null),
				((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getCivicoVersante() : null),
				((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getCapVersante() : null),
				((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getLocalitaVersante() : null),
				((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getProvinciaVersante() : null),
				((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getNazioneVersante() : null),
				((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getEMailVersante() : null),
				rpt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString(),
				rpt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco(),
				rpt.getSoggettoPagatore().getAnagraficaPagatore(), rpt.getSoggettoPagatore().getIndirizzoPagatore(),
				rpt.getSoggettoPagatore().getCivicoPagatore(), rpt.getSoggettoPagatore().getCapPagatore(),
				rpt.getSoggettoPagatore().getLocalitaPagatore(), rpt.getSoggettoPagatore().getProvinciaPagatore(),
				rpt.getSoggettoPagatore().getNazionePagatore(), rpt.getSoggettoPagatore().getEMailPagatore(),
				rpt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco()
						.toString(),
				rpt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco(),
				rpt.getEnteBeneficiario().getDenominazioneBeneficiario(),
				rpt.getEnteBeneficiario().getCodiceUnitOperBeneficiario(),
				rpt.getEnteBeneficiario().getDenomUnitOperBeneficiario(),
				rpt.getEnteBeneficiario().getIndirizzoBeneficiario(), rpt.getEnteBeneficiario().getCivicoBeneficiario(),
				rpt.getEnteBeneficiario().getCapBeneficiario(), rpt.getEnteBeneficiario().getLocalitaBeneficiario(),
				rpt.getEnteBeneficiario().getProvinciaBeneficiario(),
				rpt.getEnteBeneficiario().getNazioneBeneficiario(),
				rpt.getDatiVersamento().getDataEsecuzionePagamento().getTime(),
				rpt.getDatiVersamento().getImportoTotaleDaVersare(),
				rpt.getDatiVersamento().getTipoVersamento().toString(),
				rpt.getDatiVersamento().getIdentificativoUnivocoVersamento(),
				rpt.getDatiVersamento().getCodiceContestoPagamento(), rpt.getDatiVersamento().getIbanAddebito(),
				rpt.getDatiVersamento().getBicAddebito(), rpt.getDatiVersamento().getFirmaRicevuta().toString(),
				richiestaVersamenti, modelloPagamento, mygovRpEId);

	}

	private NodoSILInviaRPRisposta builtRispostaRP(NodoInviaRPTRisposta _rispostaRPT, UUID id_session) {
		NodoSILInviaRPRisposta _rispostaRP = new NodoSILInviaRPRisposta();

		_rispostaRP.setEsito(_rispostaRPT.getEsito());
		_rispostaRP.setRedirect(_rispostaRPT.getRedirect());

		if ((_rispostaRPT.getRedirect() != null) && (_rispostaRPT.getRedirect() == 1)) {
			//prendi da properties :: NODO REGIONALE FESP BASE URL
			_rispostaRP.setUrl(fespProperties.getBaseUrl() + "/nodoSILInviaRichiestaPagamento.html?idSession="
					+ id_session.toString());
		} else {
			_rispostaRP.setUrl(_rispostaRPT.getUrl());
		}

		it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean err = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();

		if (_rispostaRPT.getFault() != null) {
			err.setFaultCode(_rispostaRPT.getFault().getFaultCode());
			err.setFaultString(_rispostaRPT.getFault().getFaultString());
			err.setId(_rispostaRPT.getFault().getId());
			err.setDescription(_rispostaRPT.getFault().getDescription());
			err.setOriginalFaultCode(_rispostaRPT.getFault().getOriginalFaultCode());
			err.setOriginalFaultString(_rispostaRPT.getFault().getOriginalFaultString());
			err.setOriginalDescription(_rispostaRPT.getFault().getOriginalDescription());
			err.setSerial(_rispostaRPT.getFault().getSerial());
		}

		_rispostaRP.setFault(err);

		return _rispostaRP;
	}

	public NodoSILChiediSceltaWISPRisposta nodoChiediSceltaWISP(NodoSILChiediSceltaWISP nodoChiediSceltaWisp) {
		Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean> fault = new Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean>();
		Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.StEffettuazioneScelta> effettuazioneScelta = new Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.StEffettuazioneScelta>();
		Holder<String> identificativoPSP = new Holder<String>();
		Holder<String> identificativoIntermediarioPSP = new Holder<String>();
		Holder<String> identificativoCanale = new Holder<String>();
		Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.StTipoVersamento> tipoVersamento = new Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.StTipoVersamento>();

		Date dataOraEvento;
		String idDominio;
		String identificativoUnivocoVersamento;
		String codiceContestoPagamento;
		String identificativoPrestatoreServiziPagamento;
		String tipoVers;
		String componente;
		String categoriaEvento;
		String tipoEvento;
		String sottoTipoEvento;
		String identificativoFruitore;
		String identificativoErogatore;
		String identificativoStazioneIntermediarioPa;
		String canalePagamento;
		String parametriSpecificiInterfaccia;
		String esitoReq;

		try {
			dataOraEvento = new Date();
			idDominio = nodoChiediSceltaWisp.getIdentificativoDominio();
			identificativoUnivocoVersamento = "";
			codiceContestoPagamento = "n/a";
			identificativoPrestatoreServiziPagamento = "";
			tipoVers = null;
			componente = Constants.COMPONENTE.NODO_SPC.toString();
			categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoChiediSceltaWISP.toString();
			sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();

			identificativoFruitore = fespProperties.getIdentificativoIntermediarioPa();
			identificativoErogatore = Constants.NODO_DEI_PAGAMENTI_SPC;
			identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
			canalePagamento = "";

			parametriSpecificiInterfaccia = "Parametri di richiesta verso il Nodo SPC: Identificativo dominio [ "
					+ nodoChiediSceltaWisp.getIdentificativoDominio() + " ], KeyPA [ " + nodoChiediSceltaWisp.getKeyPA()
					+ " ], KeyWISP [ " + nodoChiediSceltaWisp.getKeyWISP() + " ]";

			esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

			giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
		} catch (Exception e1) {
			log.warn("nodoChiediSceltaWISP REQUEST impossibile inserire nel giornale degli eventi", e1);
		}

		pagamentiTelematiciRPTServiceClient.nodoChiediSceltaWISP(fespProperties.getIdentificativoIntermediarioPa(),
				fespProperties.getIdentificativoStazioneIntermediarioPa(), fespProperties.getPassword(),
				nodoChiediSceltaWisp.getIdentificativoDominio(), nodoChiediSceltaWisp.getKeyPA(),
				nodoChiediSceltaWisp.getKeyWISP(), fault, effettuazioneScelta, identificativoPSP,
				identificativoIntermediarioPSP, identificativoCanale, tipoVersamento);

		NodoSILChiediSceltaWISPRisposta risposta = new NodoSILChiediSceltaWISPRisposta();

		if (fault.value != null) {

			try {
				dataOraEvento = new Date();
				idDominio = nodoChiediSceltaWisp.getIdentificativoDominio();
				identificativoUnivocoVersamento = "";
				codiceContestoPagamento = "n/a";
				identificativoPrestatoreServiziPagamento = "";
				tipoVers = null;
				componente = Constants.COMPONENTE.NODO_SPC.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoChiediSceltaWISP.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();

				identificativoFruitore = fespProperties.getIdentificativoIntermediarioPa();
				identificativoErogatore = Constants.NODO_DEI_PAGAMENTI_SPC;
				identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
				canalePagamento = "";

				parametriSpecificiInterfaccia = "Fault Bean: Id [ " + fault.value.getId() + " ], Fault Code [ "
						+ fault.value.getFaultCode() + " ], Fault String [ " + fault.value.getFaultString()
						+ " ], Fault Description [ " + fault.value.getDescription() 
						+ " ], Original Fault Code [ " + (StringUtils.isNotBlank(fault.value.getOriginalFaultCode()) ? fault.value.getOriginalFaultCode() : "")
						+ " ], Original Fault String [ " + (StringUtils.isNotBlank(fault.value.getOriginalFaultString()) ? fault.value.getOriginalFaultString() : "") 
						+ " ], Original Fault Description [ " + (StringUtils.isNotBlank(fault.value.getOriginalDescription()) ? fault.value.getOriginalDescription() : "") 
						+ " ]";

				esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

				giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
			} catch (Exception e1) {
				log.warn("nodoChiediSceltaWISP RESPONSE impossibile inserire nel giornale degli eventi", e1);
			}

			it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean fb = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
			fb.setId(FaultCodeConstants.PAA_CHIEDI_SCELTA_WISP);
			fb.setFaultCode(fault.value.getFaultCode());
			fb.setFaultString(fault.value.getFaultString());
			fb.setDescription(fault.value.getDescription());
			fb.setOriginalFaultCode(fault.value.getOriginalFaultCode());
			fb.setOriginalFaultString(fault.value.getOriginalFaultString());
			fb.setOriginalDescription(fault.value.getOriginalDescription());
			fb.setSerial(1);
			risposta.setFault(fb);
		} else {
			try {
				dataOraEvento = new Date();
				idDominio = nodoChiediSceltaWisp.getIdentificativoDominio();
				identificativoUnivocoVersamento = "";
				codiceContestoPagamento = "n/a";
				identificativoPrestatoreServiziPagamento = identificativoPSP.value;
				tipoVers = tipoVersamento.value.toString();
				componente = Constants.COMPONENTE.NODO_SPC.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoChiediSceltaWISP.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();

				identificativoFruitore = fespProperties.getIdentificativoIntermediarioPa();
				identificativoErogatore = Constants.NODO_DEI_PAGAMENTI_SPC;
				identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
				canalePagamento = identificativoCanale.value;

				parametriSpecificiInterfaccia = "Parametri di risposta dal Nodo SPC: Effettuazione Scelta [ "
						+ effettuazioneScelta.value.toString() + " ], Identificativo PSP [ " + identificativoPSP.value
						+ " ], Identificativo Intermediario PSP [ " + identificativoIntermediarioPSP.value
						+ " ], Identificativo Canale [ " + identificativoCanale.value + " ], Tipo Versamento [ "
						+ tipoVersamento.value.toString() + " ]";

				esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

				giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
			} catch (Exception e1) {
				log.warn("nodoChiediSceltaWISP RESPONSE impossibile inserire nel giornale degli eventi", e1);
			}


			if (effettuazioneScelta.value != null) {
				it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.StEffettuazioneScelta effScelta = it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.StEffettuazioneScelta
						.fromValue(effettuazioneScelta.value.toString());
				risposta.setEffettuazioneScelta(effScelta);
			}

			if (identificativoPSP.value != null) {
				risposta.setIdentificativoPSP(identificativoPSP.value);
			}
			if (identificativoIntermediarioPSP.value != null) {
				risposta.setIdentificativoIntermediarioPSP(identificativoIntermediarioPSP.value);
			}
			if (identificativoCanale.value != null) {
				risposta.setIdentificativoCanale(identificativoCanale.value);
			}

			if (tipoVersamento.value != null) {
				it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.StTipoVersamento tVers = it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.StTipoVersamento
						.fromValue(tipoVersamento.value.toString());
				risposta.setTipoVersamento(tVers);
			}

//			MygovEnte ente = enteService.getByCodiceFiscale(nodoChiediSceltaWisp.getIdentificativoDominio());
			boolean selezionatoPSPfittizio = false;
			String pspFittizioIdentificativoPsp = 
					propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoPsp");
			String pspFittizioIdentificativoIntermediarioPsp =
					propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoIntermediarioPsp");
			String pspFittizioIdentificativoCanale =
					propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoCanale");
			if (risposta.getIdentificativoPSP() != null
					&& risposta.getIdentificativoPSP().equals(pspFittizioIdentificativoPsp)
					&& risposta.getIdentificativoIntermediarioPSP() != null
					&& risposta.getIdentificativoIntermediarioPSP().equals(pspFittizioIdentificativoIntermediarioPsp)
					&& risposta.getIdentificativoCanale() != null
					&& risposta.getIdentificativoCanale().equals(pspFittizioIdentificativoCanale)
					){
				selezionatoPSPfittizio = true;
			}
			if (selezionatoPSPfittizio){
				risposta.setDisponibilitaServizio("Disponibilita Servizio test");
				risposta.setDescrizioneServizio("Descrizione Servizio");
				risposta.setUrlInformazioniCanale("Url Informazioni Canale test");
				risposta.setUrlInformazioniPsp("UrlInformazioniPsp test");
				risposta.setRagioneSociale("RagioneSociale test");
				// potrei portare in configurazione anche il modello di pagamento
				risposta.setModelloPagamento(
						Integer.parseInt(propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioModelloPagamento"))
						);
				risposta.setStornoPagamento(0);
				risposta.setListaFasceCostoServizio("99999999.99#0#2.5");
				byte[] pspFittiziologoPsp = new byte[0];
				risposta.setLogoPsp(pspFittiziologoPsp);
				byte[] pspFittiziologoServizio = new byte[0];
				risposta.setLogoServizio(pspFittiziologoServizio);
			}

		}
		return risposta;
	}

	@Override
	public NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(NodoSILChiediCopiaEsito nodoSilChiediCopiaEsito) {
		NodoSILChiediCopiaEsitoRisposta risposta = new NodoSILChiediCopiaEsitoRisposta();
		//controllo le celle relative a RPT
		MygovRptRt rptRt = manageRPTRTService.getRptByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(
				nodoSilChiediCopiaEsito.getIdentificativoDominio(),
				nodoSilChiediCopiaEsito.getIdentificativoUnivocoVersamento(),
				nodoSilChiediCopiaEsito.getCodiceContestoPagamento());
		
		if (rptRt == null) {
			String msg = "Nessuna RPT per identificativo dominio [ "
					+ nodoSilChiediCopiaEsito.getIdentificativoDominio() + " ], IUV [ "
					+ nodoSilChiediCopiaEsito.getIdentificativoUnivocoVersamento() + " ] e codice contesto pagamento [ "
					+ nodoSilChiediCopiaEsito.getCodiceContestoPagamento() + " ]";
			log.error(msg);
			it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean fb = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
			fb.setSerial(1);
			fb.setDescription(msg);
			fb.setFaultString(msg);
			fb.setFaultCode(FaultCodeConstants.PAA_RPT_NON_PRESENTE);
			fb.setId(FaultCodeConstants.PAA_RPT_NON_PRESENTE);
			risposta.setFault(fb);
			return risposta;
		} else {
			//controllo le celle relative a RT
			rptRt = manageRPTRTService.getRtByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(
					nodoSilChiediCopiaEsito.getIdentificativoDominio(),
					nodoSilChiediCopiaEsito.getIdentificativoUnivocoVersamento(),
					nodoSilChiediCopiaEsito.getCodiceContestoPagamento());
			if (rptRt == null) {
				String msg = "Nessuna RT per identificativo dominio [ "
						+ nodoSilChiediCopiaEsito.getIdentificativoDominio() + " ], IUV [ "
						+ nodoSilChiediCopiaEsito.getIdentificativoUnivocoVersamento()
						+ " ] e codice contesto pagamento [ " + nodoSilChiediCopiaEsito.getCodiceContestoPagamento()
						+ " ]";
				log.info(msg);
				it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean fb = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
				fb.setSerial(1);
				fb.setDescription(msg);
				fb.setFaultString(msg);
				fb.setFaultCode(FaultCodeConstants.PAA_RT_NON_PRESENTE);
				fb.setId(FaultCodeConstants.PAA_RT_NON_PRESENTE);
				risposta.setFault(fb);
				return risposta;
			}
//
//			// Se risposta di fesp a nodo a paaInviaRt non valorizzata
//			// oppure valorizzata a KO ma senza fault code, rispondo che esito non presente
//			if (rptRt.getCodRtInviartEsito() == null ||
//				(rptRt.getCodRtInviartEsito().equals(Constants.NODO_REGIONALE_FESP_ESITO_KO) &&
//						StringUtils.isBlank(rptRt.getCodRtInviartFaultCode())) ){
//				String msg = "Esito o faultCode di paaInviaRt non valorizzato [ "
//						+ nodoSilChiediCopiaEsito.getIdentificativoDominio() + " ], IUV [ "
//						+ nodoSilChiediCopiaEsito.getIdentificativoUnivocoVersamento() + " ] e codice contesto pagamento [ "
//						+ nodoSilChiediCopiaEsito.getCodiceContestoPagamento() + " ]";
//				log.error(msg);
//				it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean fb = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
//				fb.setSerial(1);
//				fb.setDescription(msg);
//				fb.setFaultString(msg);
//				fb.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
//				fb.setId(FaultCodeConstants.PAA_SYSTEM_ERROR);
//				risposta.setFault(fb);
//				return risposta;
//			}
//			
			if (StringUtils.isNotBlank(rptRt.getCodRtDatiPagCodiceEsitoPagamento())
					
//				&&(rptRt.getCodRtInviartEsito().equals(Constants.NODO_REGIONALE_FESP_ESITO_OK)
//					|| (rptRt.getCodRtInviartEsito().equals(Constants.NODO_REGIONALE_FESP_ESITO_KO) &&
//						rptRt.getCodRtInviartFaultCode().equals(FaultCodeConstants.PAA_RT_DUPLICATA)))
				) {
				risposta.setTipoFirma(rptRt.getDeRptInviarptTipoFirma());

				risposta.setRt(rptRt.getBlbRtPayload());

				it.veneto.regione.schemas.x2012.pagamenti.CtEsito esito = buildEsito(rptRt);
				risposta.setEsito(encodeEsito(esito));
			} else {
				String msg = "Codice esito pagamento non valorizzato RT con identificativo dominio [ "
						+ nodoSilChiediCopiaEsito.getIdentificativoDominio() + " ], IUV [ "
						+ nodoSilChiediCopiaEsito.getIdentificativoUnivocoVersamento()
						+ " ] e codice contesto pagamento [ " + nodoSilChiediCopiaEsito.getCodiceContestoPagamento()
						+ " ]";
				log.error(msg);
				it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean fb = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
				fb.setSerial(1);
				fb.setDescription(msg);
				fb.setFaultString(msg);
				fb.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
				fb.setId(FaultCodeConstants.PAA_SYSTEM_ERROR);
				risposta.setFault(fb);
			}

			return risposta;
		}
	}

	private it.veneto.regione.schemas.x2012.pagamenti.CtEsito buildEsito(MygovRptRt rptRt) {

		/**
		 * CtEsito
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtEsito ctEsito = it.veneto.regione.schemas.x2012.pagamenti.CtEsito.Factory
				.newInstance();
		ctEsito.setVersioneOggetto(rptRt.getDeRtVersioneOggetto());

		/**
		 * CtDominio
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtDominio dominio = it.veneto.regione.schemas.x2012.pagamenti.CtDominio.Factory
				.newInstance();
		dominio.setIdentificativoDominio(rptRt.getCodRtDomIdDominio());
		if (rptRt.getCodRtDomIdStazioneRichiedente() != null) {
			dominio.setIdentificativoStazioneRichiedente(rptRt.getCodRtDomIdStazioneRichiedente());
		}
		ctEsito.setDominio(dominio);

		ctEsito.setIdentificativoMessaggioRicevuta(rptRt.getCodRtIdMessaggioRicevuta());
		//if (rptRt.getDtRtDataOraMessaggioRicevuta() != null)
		ctEsito.setDataOraMessaggioRicevuta(dateToCalendar(rptRt.getDtRtDataOraMessaggioRicevuta()));
		ctEsito.setRiferimentoMessaggioRichiesta(rptRt.getCodRptIdMessaggioRichiesta());
		//if (rptRt.getDtRptDataOraMessaggioRichiesta() != null)
		ctEsito.setRiferimentoDataRichiesta(dateToCalendar(rptRt.getDtRptDataOraMessaggioRichiesta()));

		/**
		 * CtIstitutoAttestante
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtIstitutoAttestante istitutoAttestante = it.veneto.regione.schemas.x2012.pagamenti.CtIstitutoAttestante.Factory
				.newInstance();
		it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivoco identificativoUnivocoAttestante = it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivoco.Factory
				.newInstance();
		identificativoUnivocoAttestante
				.setTipoIdentificativoUnivoco(it.veneto.regione.schemas.x2012.pagamenti.StTipoIdentificativoUnivoco.Enum
						.forString(rptRt.getCodRtIstitAttesIdUnivAttesTipoIdUnivoco()));
		identificativoUnivocoAttestante
				.setCodiceIdentificativoUnivoco(rptRt.getCodRtIstitAttesIdUnivAttesCodiceIdUnivoco());
		istitutoAttestante.setIdentificativoUnivocoAttestante(identificativoUnivocoAttestante);

		if (rptRt.getDeRtIstitAttesDenominazioneAttestante() != null) {
			istitutoAttestante.setDenominazioneAttestante(rptRt.getDeRtIstitAttesDenominazioneAttestante());
		}
		if (rptRt.getCodRtIstitAttesCodiceUnitOperAttestante() != null) {
			istitutoAttestante.setCodiceUnitOperAttestante(rptRt.getCodRtIstitAttesCodiceUnitOperAttestante());
		}
		if (rptRt.getDeRtIstitAttesDenomUnitOperAttestante() != null) {
			istitutoAttestante.setDenomUnitOperAttestante(rptRt.getDeRtIstitAttesDenomUnitOperAttestante());
		}
		if (rptRt.getDeRtIstitAttesIndirizzoAttestante() != null) {
			istitutoAttestante.setIndirizzoAttestante(rptRt.getDeRtIstitAttesIndirizzoAttestante());
		}
		if (rptRt.getDeRtIstitAttesCivicoAttestante() != null) {
			istitutoAttestante.setCivicoAttestante(rptRt.getDeRtIstitAttesCivicoAttestante());
		}
		if (rptRt.getCodRtIstitAttesCapAttestante() != null) {
			istitutoAttestante.setCapAttestante(rptRt.getCodRtIstitAttesCapAttestante());
		}
		if (rptRt.getDeRtIstitAttesLocalitaAttestante() != null) {
			istitutoAttestante.setLocalitaAttestante(rptRt.getDeRtIstitAttesLocalitaAttestante());
		}
		if (rptRt.getDeRtIstitAttesProvinciaAttestante() != null) {
			istitutoAttestante.setProvinciaAttestante(rptRt.getDeRtIstitAttesProvinciaAttestante());
		}
		if (rptRt.getCodRtIstitAttesNazioneAttestante() != null) {
			istitutoAttestante.setNazioneAttestante(rptRt.getCodRtIstitAttesNazioneAttestante());
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
						.forString(rptRt.getCodRtEnteBenefIdUnivBenefTipoIdUnivoco()));
		identificativoUnivocoBeneficiario
				.setCodiceIdentificativoUnivoco(rptRt.getCodRtEnteBenefIdUnivBenefCodiceIdUnivoco());
		enteBeneficiario.setIdentificativoUnivocoBeneficiario(identificativoUnivocoBeneficiario);
		enteBeneficiario.setDenominazioneBeneficiario(rptRt.getDeRtEnteBenefDenominazioneBeneficiario());

		if (rptRt.getCodRtEnteBenefCodiceUnitOperBeneficiario() != null) {
			enteBeneficiario.setCodiceUnitOperBeneficiario(rptRt.getCodRtEnteBenefCodiceUnitOperBeneficiario());
		}
		if (rptRt.getDeRtEnteBenefDenomUnitOperBeneficiario() != null) {
			enteBeneficiario.setDenomUnitOperBeneficiario(rptRt.getDeRtEnteBenefDenomUnitOperBeneficiario());
		}
		if (rptRt.getDeRtEnteBenefIndirizzoBeneficiario() != null) {
			enteBeneficiario.setIndirizzoBeneficiario(rptRt.getDeRtEnteBenefIndirizzoBeneficiario());
		}
		if (rptRt.getDeRtEnteBenefCivicoBeneficiario() != null) {
			enteBeneficiario.setCivicoBeneficiario(rptRt.getDeRtEnteBenefCivicoBeneficiario());
		}
		if (rptRt.getCodRtEnteBenefCapBeneficiario() != null) {
			enteBeneficiario.setCapBeneficiario(rptRt.getCodRtEnteBenefCapBeneficiario());
		}
		if (rptRt.getDeRtEnteBenefLocalitaBeneficiario() != null) {
			enteBeneficiario.setLocalitaBeneficiario(rptRt.getDeRtEnteBenefLocalitaBeneficiario());
		}
		if (rptRt.getDeRtEnteBenefProvinciaBeneficiario() != null) {
			enteBeneficiario.setProvinciaBeneficiario(rptRt.getDeRtEnteBenefProvinciaBeneficiario());
		}
		if (rptRt.getCodRtEnteBenefNazioneBeneficiario() != null) {
			enteBeneficiario.setNazioneBeneficiario(rptRt.getCodRtEnteBenefNazioneBeneficiario());
		}
		ctEsito.setEnteBeneficiario(enteBeneficiario);

		/**
		 * CtSoggettoVersante
		 */

		if (rptRt.getDeRtSoggVersAnagraficaVersante() != null) {
			it.veneto.regione.schemas.x2012.pagamenti.CtSoggettoVersante soggettoVersante = it.veneto.regione.schemas.x2012.pagamenti.CtSoggettoVersante.Factory
					.newInstance();
			it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante = it.veneto.regione.schemas.x2012.pagamenti.CtIdentificativoUnivocoPersonaFG.Factory
					.newInstance();
			identificativoUnivocoVersante.setTipoIdentificativoUnivoco(
					it.veneto.regione.schemas.x2012.pagamenti.StTipoIdentificativoUnivocoPersFG.Enum
							.forString(rptRt.getCodRtSoggVersIdUnivVersTipoIdUnivoco()));
			identificativoUnivocoVersante
					.setCodiceIdentificativoUnivoco(rptRt.getCodRtSoggVersIdUnivVersCodiceIdUnivoco());
			soggettoVersante.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);
			soggettoVersante.setAnagraficaVersante(rptRt.getDeRtSoggVersAnagraficaVersante());

			if (rptRt.getDeRtSoggVersIndirizzoVersante() != null) {
				soggettoVersante.setIndirizzoVersante(rptRt.getDeRtSoggVersIndirizzoVersante());
			}
			if (rptRt.getDeRtSoggVersCivicoVersante() != null) {
				soggettoVersante.setCivicoVersante(rptRt.getDeRtSoggVersCivicoVersante());
			}
			if (rptRt.getCodRtSoggVersCapVersante() != null) {
				soggettoVersante.setCapVersante(rptRt.getCodRtSoggVersCapVersante());
			}
			if (rptRt.getDeRtSoggVersLocalitaVersante() != null) {
				soggettoVersante.setLocalitaVersante(rptRt.getDeRtSoggVersLocalitaVersante());
			}
			if (rptRt.getDeRtSoggVersProvinciaVersante() != null) {
				soggettoVersante.setProvinciaVersante(rptRt.getDeRtSoggVersProvinciaVersante());
			}
			if (rptRt.getCodRtSoggVersNazioneVersante() != null) {
				soggettoVersante.setNazioneVersante(rptRt.getCodRtSoggVersNazioneVersante());
			}
			if (rptRt.getDeRtSoggVersEmailVersante() != null) {
				soggettoVersante.setEMailVersante(rptRt.getDeRtSoggVersEmailVersante());
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
						.forString(rptRt.getCodRtSoggPagIdUnivPagTipoIdUnivoco()));
		identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(rptRt.getCodRtSoggPagIdUnivPagCodiceIdUnivoco());
		soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
		soggettoPagatore.setAnagraficaPagatore(rptRt.getDeRtSoggPagAnagraficaPagatore());

		if (rptRt.getDeRtSoggPagIndirizzoPagatore() != null) {
			soggettoPagatore.setIndirizzoPagatore(rptRt.getDeRtSoggPagIndirizzoPagatore());
		}
		if (rptRt.getDeRtSoggPagCivicoPagatore() != null) {
			soggettoPagatore.setCivicoPagatore(rptRt.getDeRtSoggPagCivicoPagatore());
		}
		if (rptRt.getCodRtSoggPagCapPagatore() != null) {
			soggettoPagatore.setCapPagatore(rptRt.getCodRtSoggPagCapPagatore());
		}
		if (rptRt.getDeRtSoggPagLocalitaPagatore() != null) {
			soggettoPagatore.setLocalitaPagatore(rptRt.getDeRtSoggPagLocalitaPagatore());
		}
		if (rptRt.getDeRtSoggPagProvinciaPagatore() != null) {
			soggettoPagatore.setProvinciaPagatore(rptRt.getDeRtSoggPagProvinciaPagatore());
		}
		if (rptRt.getCodRtSoggPagNazionePagatore() != null) {
			soggettoPagatore.setNazionePagatore(rptRt.getCodRtSoggPagNazionePagatore());
		}
		if (rptRt.getDeRtSoggPagEmailPagatore() != null) {
			soggettoPagatore.setEMailPagatore(rptRt.getDeRtSoggPagEmailPagatore());
		}
		ctEsito.setSoggettoPagatore(soggettoPagatore);

		/**
		 * CtDatiVersamentoEsito
		 */

		it.veneto.regione.schemas.x2012.pagamenti.CtDatiVersamentoEsito datiPagamento = it.veneto.regione.schemas.x2012.pagamenti.CtDatiVersamentoEsito.Factory
				.newInstance();
		datiPagamento.setCodiceEsitoPagamento(it.veneto.regione.schemas.x2012.pagamenti.StCodiceEsitoPagamento.Enum
				.forString(rptRt.getCodRtDatiPagCodiceEsitoPagamento()));
		datiPagamento.setImportoTotalePagato(rptRt.getNumRtDatiPagImportoTotalePagato());
		datiPagamento.setIdentificativoUnivocoVersamento(rptRt.getCodRtDatiPagIdUnivocoVersamento());
		datiPagamento.setCodiceContestoPagamento(rptRt.getCodRtDatiPagCodiceContestoPagamento());

		/**
		 * CtDatiSingoloPagamentoEsito
		 */

		List<MygovRptRtDettaglio> listaPagamenti = rptRtDettaglioDao.getByRptRt(rptRt);

		if (listaPagamenti != null) {

			it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito[] pagamentiSingoli = new it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito[listaPagamenti
					.size()];

			int count = 0;

			for (int i = 0; i < listaPagamenti.size(); i++) {
				MygovRptRtDettaglio dettaglioPagamento = listaPagamenti.get(i);

				if (dettaglioPagamento.getNumRtDatiPagDatiSingPagSingoloImportoPagato() == null) {
					continue;
				}

				count++;

				it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito singoloPagamento = it.veneto.regione.schemas.x2012.pagamenti.CtDatiSingoloPagamentoEsito.Factory
						.newInstance();
				singoloPagamento
						.setSingoloImportoPagato(dettaglioPagamento.getNumRtDatiPagDatiSingPagSingoloImportoPagato());
				singoloPagamento.setCausaleVersamento(dettaglioPagamento.getDeRtDatiPagDatiSingPagCausaleVersamento());

				singoloPagamento.setDataEsitoSingoloPagamento(
						dateToCalendar(dettaglioPagamento.getDtRtDatiPagDatiSingPagDataEsitoSingoloPagamento()));

				singoloPagamento.setDatiSpecificiRiscossione(
						dettaglioPagamento.getDeRtDatiPagDatiSingPagDatiSpecificiRiscossione());

				if (dettaglioPagamento.getDeRtDatiPagDatiSingPagEsitoSingoloPagamento() != null) {
					singoloPagamento.setEsitoSingoloPagamento(
							dettaglioPagamento.getDeRtDatiPagDatiSingPagEsitoSingoloPagamento());
				}
				singoloPagamento.setIdentificativoUnivocoRiscossione(
						dettaglioPagamento.getCodRtDatiPagDatiSingPagIdUnivocoRiscossione());

				if (dettaglioPagamento.getNumRtDatiPagDatiSingPagCommissioniApplicatePsp() != null) {
					singoloPagamento.setCommissioniApplicatePSP(
							dettaglioPagamento.getNumRtDatiPagDatiSingPagCommissioniApplicatePsp());
				}
				if (dettaglioPagamento.getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest() != null) {
					it.veneto.regione.schemas.x2012.pagamenti.CtAllegatoRicevuta ctAllegatoRicevuta = it.veneto.regione.schemas.x2012.pagamenti.CtAllegatoRicevuta.Factory
							.newInstance();
					ctAllegatoRicevuta.setTipoAllegatoRicevuta(
							it.veneto.regione.schemas.x2012.pagamenti.StTipoAllegatoRicevuta.Enum
									.forString(dettaglioPagamento.getCodRtDatiPagDatiSingPagAllegatoRicevutaTipo()));
					ctAllegatoRicevuta
							.setTestoAllegato(dettaglioPagamento.getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest());
					singoloPagamento.setAllegatoRicevuta(ctAllegatoRicevuta);
				}
				pagamentiSingoli[i] = singoloPagamento;
			}

			if (count == listaPagamenti.size())
				datiPagamento.setDatiSingoloPagamentoArray(pagamentiSingoli);
		}

		ctEsito.setDatiPagamento(datiPagamento);

		return ctEsito;
	}

	private Calendar dateToCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	private byte[] encodeEsito(it.veneto.regione.schemas.x2012.pagamenti.CtEsito esito) {
		try {
			byte[] byteEsito;

			EsitoDocument encEsito = EsitoDocument.Factory.newInstance();
			encEsito.setEsito(esito);

			byteEsito = Base64.encodeBase64(encEsito.toString().getBytes("UTF-8"));

			return byteEsito;
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Failed to encode CtEsito ::: UnsupportedEncodingException :::", uee);
		}
	}

	@Override
	public NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP nc) {
		NodoSILInviaCarrelloRPRisposta _rispostaCarrello = null;
	    NodoInviaCarrelloRPTRisposta _rispostaNodo= null;
		
		MygovCarrelloRp mygovCarrelloRp = null;
		MygovCarrelloRpt mygovCarrelloRpt = null;
		String id_session = Utils.getRandomUIDsenzaCarattereMeno();
		try {
			// salva myGovCarrelloRP
			
		
			mygovCarrelloRp = saveCarrelloRP(id_session, nc.getIdentificativoDominioEnteChiamante())  ;
			mygovCarrelloRpt = saveCarrelloRPT(mygovCarrelloRp);
			TipoListaRPT listaRPT = new TipoListaRPT();
			List<ElementoRP> listRP = nc.getListaRP().getElementoRP();
			for (ElementoRP elementoRP: listRP) {
				///Utilizzo lo stesso metodo di saveRP 
				it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT intestazionePPT = new IntestazionePPT();
				intestazionePPT.setCodiceContestoPagamento(elementoRP.getCodiceContestoPagamento());
				intestazionePPT.setIdentificativoDominio(elementoRP.getIdentificativoDominio());
				intestazionePPT.setIdentificativoUnivocoVersamento(elementoRP.getIdentificativoUnivocoVersamento());
//				intestazionePPT.setIdentificativoIntermediarioPA
//				(this.propertiesUtil.getProperty("nodoRegionaleFesp.identificativoIntermediarioPA"));
//				intestazionePPT.setIdentificativoStazioneIntermediarioPA
//				(this.propertiesUtil.getProperty("nodoRegionaleFesp.identificativoStazioneIntermediarioPA"));
				
				NodoSILInviaRP nsirp = new NodoSILInviaRP();
				int modelloPagamento = Integer.parseInt(this.propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioModelloPagamento"));
				nsirp.setModelloPagamento(modelloPagamento);
				nsirp.setIdentificativoCanale
				(this.propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoCanale"));
				nsirp.setIdentificativoIntermediarioPSP
				(this.propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoIntermediarioPsp"));
				nsirp.setIdentificativoPSP
				(this.propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoPsp"));
				nsirp.setRp(elementoRP.getRp());
				
				it.veneto.regione.schemas.x2012.pagamenti.CtRichiestaPagamento rp 
				= decodificaRp(nsirp);
				MygovRpE rpe = saveRp(intestazionePPT, nsirp, rp);
				rpe = manageRPEService.updateCarrelloRef(rpe.getMygovRpEId(), mygovCarrelloRp);
				
				//Passo alle RPT
				String idDominio = rp.getDominio().getIdentificativoDominio();
				MygovEnte enteProp = enteService.getByCodiceFiscale(idDominio);

				it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico CtRPT = this.buildRPT(rp, enteProp);
				gov.telematici.pagamenti.ws.ppthead.IntestazionePPT _paaSILInviaRPT_header = this.buildHeaderRPT(intestazionePPT, enteProp);
				NodoInviaRPT _paaSILInviaRPT_body = this.buildBodyRPT(CtRPT, nsirp);
				_paaSILInviaRPT_body.setTipoFirma("");

				MygovRptRt mygovRptRt = saveRPT(_paaSILInviaRPT_header, _paaSILInviaRPT_body, CtRPT, rpe.getMygovRpEId(),
						1);
				mygovRptRt = manageRPTRTService.updateCarrelloRef(mygovRptRt.getMygovRptRtId(), mygovCarrelloRpt);
				TipoElementoListaRPT elementoRPT = new TipoElementoListaRPT();
				elementoRPT.setCodiceContestoPagamento(elementoRP.getCodiceContestoPagamento());
				elementoRPT.setIdentificativoDominio(elementoRP.getIdentificativoDominio());
				elementoRPT.setIdentificativoUnivocoVersamento(elementoRP.getIdentificativoUnivocoVersamento());
				elementoRPT.setTipoFirma("");
				elementoRPT.setRpt(this.encodeRPT(CtRPT));
				listaRPT.getElementoListaRPT().add(elementoRPT);
			}
			
			
			IntestazioneCarrelloPPT intestazioneCarrelloPPT =
					new IntestazioneCarrelloPPT();
			NodoInviaCarrelloRPT nodoInviaCarrelloRPT = new NodoInviaCarrelloRPT();
			intestazioneCarrelloPPT.setIdentificativoCarrello(mygovCarrelloRpt.getCodRptInviacarrellorptIdCarrello());
			intestazioneCarrelloPPT.setIdentificativoIntermediarioPA(mygovCarrelloRpt.getCodRptInviacarrellorptIdIntermediarioPa());
			intestazioneCarrelloPPT.setIdentificativoStazioneIntermediarioPA(mygovCarrelloRpt.getCodRptInviacarrellorptIdStazioneIntermediarioPa());
			nodoInviaCarrelloRPT.setPassword(mygovCarrelloRpt.getDeRptInviacarrellorptPassword());
			nodoInviaCarrelloRPT.setIdentificativoCanale(mygovCarrelloRpt.getCodRptInviacarrellorptIdCanale());
			nodoInviaCarrelloRPT.setIdentificativoIntermediarioPSP(mygovCarrelloRpt.getCodRptInviacarrellorptIdIntermediarioPsp());
			nodoInviaCarrelloRPT.setIdentificativoPSP(mygovCarrelloRpt.getCodRptInviacarrellorptIdPsp());
			nodoInviaCarrelloRPT.setListaRPT(listaRPT);
			
			_rispostaNodo = nodoInviaCarrelloRPT(intestazioneCarrelloPPT, nodoInviaCarrelloRPT);
			log.info("AAAAAAAAAAAAAAAAAAAAAAAA" + _rispostaNodo.getEsitoComplessivoOperazione() + "BBB");
			_rispostaCarrello = buildRispostaCarrelloRP(id_session, _rispostaNodo);
			
			
		}
		catch (Exception e) {
			log.error(FaultCodeConstants.PPT_ESITO_SCONOSCIUTO + ": [" + e.getMessage() + "]", e);

		
			
			_rispostaNodo = new NodoInviaCarrelloRPTRisposta();
			_rispostaNodo.setEsitoComplessivoOperazione("KO");

				gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean faultRPT = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean();
				faultRPT.setFaultCode(FaultCodeConstants.PPT_ESITO_SCONOSCIUTO);
				faultRPT.setDescription(e.getMessage());
				_rispostaNodo.setFault(faultRPT);
			

			//RISPOSTA RP
			_rispostaCarrello = new NodoSILInviaCarrelloRPRisposta();
			_rispostaCarrello.setEsito("KO");

			it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean faultRP = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
			faultRP.setFaultCode(FaultCodeConstants.PPT_ESITO_SCONOSCIUTO);
			faultRP.setDescription(e.getMessage());
			_rispostaCarrello.setFault(faultRP);
			
		}
		finally {

			try {
				if (mygovCarrelloRp != null) {

					
					if  (mygovCarrelloRpt != null) {
						saveCarrelloRPTRisposta(_rispostaNodo, mygovCarrelloRpt);
					}

					//        	SALVA DB RISPOSTA RP  
					saveCarrelloRpRisposta(mygovCarrelloRp, 
							_rispostaCarrello, id_session);
				}
			} catch (Exception e) {
				log.error("Error saving RP risposta: [" + e.getMessage() + "]", e);
			} finally {
				//se errore nel salvataggio (invio preso in carico) RP o RPT e (modello 2 o 3) ROLLBACK per permettere risottomissione
		
			}
		}
		
		return _rispostaCarrello;
			
		
			
	}



	private void saveCarrelloRpRisposta(MygovCarrelloRp mygovCarrelloRp, NodoSILInviaCarrelloRPRisposta _rispostaCarrello,
			String id_session) {
		it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean esitoFault = _rispostaCarrello.getFault();

		manageCarrelloRPService.updateRispostaRpById(mygovCarrelloRp.getMygovCarrelloRpId(), _rispostaCarrello.getEsito(), 
				_rispostaCarrello.getUrl(), esitoFault.getFaultCode(), esitoFault.getFaultString(), esitoFault.getId(),
				esitoFault.getDescription(), esitoFault.getSerial(), id_session.toString(), esitoFault.getOriginalFaultCode(),
				esitoFault.getOriginalFaultString(), esitoFault.getOriginalDescription());
		
		manageRPEService.updateRispostaRpByCarrello(mygovCarrelloRp, _rispostaCarrello.getEsito(), _rispostaCarrello.getUrl(), esitoFault.getFaultCode(),
				esitoFault.getFaultString(), esitoFault.getId(), esitoFault.getDescription(), esitoFault.getSerial(), esitoFault.getOriginalFaultCode(),
				esitoFault.getOriginalFaultString(), esitoFault.getOriginalDescription())
		;
	}

	private void saveCarrelloRPTRisposta(NodoInviaCarrelloRPTRisposta _rispostaNodo, MygovCarrelloRpt mygovCarrelloRpt) throws UnsupportedEncodingException, MalformedURLException {
		FaultBean faultBean = new FaultBean();
		
		if( !_rispostaNodo.getEsitoComplessivoOperazione().equals("OK")) {
			
			if(_rispostaNodo.getFault() != null)
				faultBean = _rispostaNodo.getFault();
			else if(_rispostaNodo.getListaErroriRPT().getFault().get(0) != null)
				faultBean = _rispostaNodo.getListaErroriRPT().getFault().get(0);
		
		}
		
		manageCarrelloRPService.updateRispostaRptById(mygovCarrelloRpt.getMygovCarrelloRptId(), _rispostaNodo.getEsitoComplessivoOperazione(), _rispostaNodo.getUrl(), faultBean);
		manageRPTRTService.updateRispostaRptByCarrello(mygovCarrelloRpt, _rispostaNodo.getEsitoComplessivoOperazione(), _rispostaNodo.getUrl(), faultBean);
	}

	private NodoSILInviaCarrelloRPRisposta buildRispostaCarrelloRP(String id_session,
			NodoInviaCarrelloRPTRisposta _rispostaNodo) {
		
		NodoSILInviaCarrelloRPRisposta _rispostaCarrelloRP = new NodoSILInviaCarrelloRPRisposta();

		_rispostaCarrelloRP.setEsito(_rispostaNodo.getEsitoComplessivoOperazione());
		

		_rispostaCarrelloRP.setUrl(fespProperties.getBaseUrl() + "/nodoSILInviaRichiestaPagamentoCarrello.html?idSession="
					+ id_session.toString());
		
		it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean err = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
		
		if( !_rispostaNodo.getEsitoComplessivoOperazione().equals("OK")) { 
		
			FaultBean faultBean = new FaultBean();
			if(_rispostaNodo.getFault() != null)
				faultBean = _rispostaNodo.getFault();
			else if(_rispostaNodo.getListaErroriRPT().getFault().get(0) != null)
				faultBean = _rispostaNodo.getListaErroriRPT().getFault().get(0);
			
			err.setFaultCode(faultBean.getFaultCode());
			err.setFaultString(faultBean.getFaultString());
			err.setId(faultBean.getId());
			err.setDescription(faultBean.getDescription());
			err.setOriginalFaultCode(faultBean.getOriginalFaultCode());
			err.setOriginalFaultString(faultBean.getOriginalFaultString());
			err.setOriginalDescription(faultBean.getOriginalDescription());
			err.setSerial(faultBean.getSerial());
			
		}

		_rispostaCarrelloRP.setFault(err);

		return _rispostaCarrelloRP;
	}

	private NodoInviaCarrelloRPTRisposta nodoInviaCarrelloRPT(IntestazioneCarrelloPPT intestazioneCarrelloPPT,
			NodoInviaCarrelloRPT nodoInviaCarrelloRPT) {
		//INSERIRE GIORNALE
		Date dataOraEvento;
		String identificativoDominio;
		String identificativoUnivocoVersamento;
		String codiceContestoPagamento;
		String identificativoPrestatoreServiziPagamento;
		String tipoVersamento;
		String componente;
		String categoriaEvento;
		String tipoEvento;
		String sottoTipoEvento;
		String identificativoFruitore;
		String identificativoErogatore;
		String identificativoStazioneIntermediarioPa;
		String canalePagamento;
		String xmlString;
		JAXBContext context;
		String parametriSpecificiInterfaccia;
		String esitoReq;
		List<TipoElementoListaRPT> lista = nodoInviaCarrelloRPT.getListaRPT().getElementoListaRPT();
		for (TipoElementoListaRPT singolaRPT: lista) {
			try {
				dataOraEvento = new Date();
				identificativoDominio = singolaRPT.getIdentificativoDominio();
				identificativoUnivocoVersamento = singolaRPT.getIdentificativoUnivocoVersamento();
				codiceContestoPagamento = singolaRPT.getCodiceContestoPagamento();
				identificativoPrestatoreServiziPagamento =  propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoPsp");
				tipoVersamento = null;
				componente = Constants.COMPONENTE.FESP.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoInviaCarrelloRPT.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
	
				identificativoFruitore = fespProperties.getIdentificativoStazioneIntermediarioPa();
				identificativoErogatore = Constants.NODO_DEI_PAGAMENTI_SPC;
				identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
				canalePagamento = propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoCanale");
				
				parametriSpecificiInterfaccia = decodeRPT(singolaRPT.getRpt());
	
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
	
				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
			} catch (Exception e1) {
				log.warn("nodoInviaRPT REQUEST impossibile inserire nel giornale degli eventi", e1);
			}
		}
		log.debug("PRIMA DI CHIAMATA AL WS ESTERNO");
		NodoInviaCarrelloRPTRisposta _rispostaRPT = pagamentiTelematiciRPTServiceClient.nodoInviaCarrelloRPT(nodoInviaCarrelloRPT, intestazioneCarrelloPPT);
		for (TipoElementoListaRPT singolaRPT: lista) {
			try {
				dataOraEvento = new Date();
				identificativoDominio = singolaRPT.getIdentificativoDominio();
				identificativoUnivocoVersamento = singolaRPT.getIdentificativoUnivocoVersamento();
				codiceContestoPagamento = singolaRPT.getCodiceContestoPagamento();
				identificativoPrestatoreServiziPagamento =  propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoPsp");
				tipoVersamento = null;
				componente = Constants.COMPONENTE.FESP.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoInviaCarrelloRPT.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
	
				identificativoFruitore = fespProperties.getIdentificativoStazioneIntermediarioPa();
				identificativoErogatore = Constants.NODO_DEI_PAGAMENTI_SPC;
				identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
				canalePagamento = propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoCanale");
	       
	
				xmlString = "";
				try {
					gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory();
					context = JAXBContext.newInstance(NodoInviaCarrelloRPTRisposta.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createNodoInviaCarrelloRPTRisposta(_rispostaRPT), sw);
					xmlString = sw.toString();
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					log.error(e.getMessage());
				}
	
				parametriSpecificiInterfaccia = xmlString;
	
				esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
	
				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
			} catch (Exception e) {
				log.warn("nodoInviaCarrelloRPT RESPONSE impossibile inserire nel giornale degli eventi", e);
			}
		}
		
		return _rispostaRPT;
		
	}

	private String decodeRPT(byte[] rpt) {
		String decodedString = null;
		byte[] decodedRPT = rpt;
		//decodedRPT = Base64.decodeBase64(rpt); // fix 5.8.x

		CtRichiestaPagamentoTelematico ctRPT  = null;
		XmlOptions options = new XmlOptions();
		List<XmlError> errors = new ArrayList<XmlError>();
		boolean validXml = false;
		options.setErrorListener(errors);

		RPTDocument rptDocument  = null;
		try {
			rptDocument = RPTDocument.Factory.parse(new String(decodedRPT, "UTF-8"));
			validXml = rptDocument.validate(options);

			if (validXml) {

				ctRPT =	rptDocument.getRPT();
				decodedString = ctRPT.toString();

			}
		} catch (Exception e) {
			log.warn("Errore nel decoding della RPT", e);
		}

		return decodedString;
	}

	private MygovCarrelloRpt saveCarrelloRPT(MygovCarrelloRp mygovCarrelloRp) {

		MygovCarrelloRpt carrelloRPT = this.manageCarrelloRPService.insertCarrelloRPTWithRefresh(
				mygovCarrelloRp);
		return carrelloRPT;
	}

	private MygovCarrelloRp saveCarrelloRP( String id_session, String dominioChiamante) {
		//generare l'informazione idSession come UUID
		
		MygovCarrelloRp carrelloRP = this.manageCarrelloRPService.insertCarrelloRPWithRefresh(
				id_session,dominioChiamante );
		return carrelloRP;
	}

	/**
	 * 
	 * Componente del fesp che chiama il client 
	 */
	@Override
	public NodoInviaRispostaRevocaRisposta nodoInviaRispostaRevoca(NodoInviaRispostaRevoca bodyrichiesta) {
		Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean> fault = new Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean>();
		Holder<java.lang.String> esito = new Holder<java.lang.String>();
		
		log.info("Richiamato metodo nodoInviaRispostaRevoca");
		
		/*
		 * LOG GIORNALE DEGLI EVENTI
		 */
		Date dataOraEvento;
		String identificativoDominio = bodyrichiesta.getIdentificativoDominio();
		String identificativoUnivocoVersamento = bodyrichiesta.getIdentificativoUnivocoVersamento();
		String codiceContestoPagamento =  bodyrichiesta.getCodiceContestoPagamento();
		String identificativoPrestatoreServiziPagamento = "";
		String tipoVersamento = "";
		String componente = Constants.COMPONENTE.FESP.toString();
		String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
		String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoInviaRispostaRevoca.toString();
		String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
		String identificativoFruitore = fespProperties.getIdentificativoStazioneIntermediarioPa();
		String identificativoErogatore = "";
		String identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
		String canalePagamento = "";
		String xmlString;
		JAXBContext context;
		String parametriSpecificiInterfaccia;
		String esitoReq;
		
		try{
			
			dataOraEvento = new Date();
			xmlString = "";
			
			try {
				gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory();
				context = JAXBContext.newInstance(NodoInviaRPT.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createNodoInviaRispostaRevoca(bodyrichiesta), sw);
				xmlString = sw.toString();
			} catch (JAXBException e) {
				log.error(e.getMessage());
			}
			
			parametriSpecificiInterfaccia = xmlString;

			esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
			
		}catch (Exception e) {
			log.warn("nodoInviaRispostaRevoca REQUEST impossibile inserire nel giornale degli eventi: ", e);
		}
		
		//INVIA RISPOSTA 
		NodoInviaRispostaRevocaRisposta rispostaNIRRR = pagamentiTelematiciRPTServiceClient.nodoInviaRispostaRevoca(
				bodyrichiesta.getIdentificativoIntermediarioPA(), 
				bodyrichiesta.getIdentificativoStazioneIntermediarioPA(), 
				bodyrichiesta.getPassword(), 
				bodyrichiesta.getIdentificativoDominio(), 
				bodyrichiesta.getIdentificativoUnivocoVersamento(), 
				bodyrichiesta.getCodiceContestoPagamento(), 
				bodyrichiesta.getEr(), fault, esito);
		
		//Log risposta nel giornale degli eventi
		try{
			
			dataOraEvento = new Date();
			xmlString = "";
			
			try {
				gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.ObjectFactory();
				context = JAXBContext.newInstance(NodoInviaRispostaRevocaRisposta.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createNodoInviaRispostaRevocaRisposta(rispostaNIRRR), sw);
				xmlString = sw.toString();
			} catch (JAXBException e) {
				log.error(e.getMessage());
			}
	
			parametriSpecificiInterfaccia = xmlString;
	
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
	
			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
			
		} catch (Exception e) {
			log.warn("nodoInviaRispostaRevoca RESPONSE impossibile inserire nel giornale degli eventi", e);
		}
		
		return rispostaNIRRR;
	}
	
	private String decodificaRPT(byte[] rpt) {
		String decodedString = null;
		RPTDocument rpDocument;

		try {
			String rpString = Base64.isBase64(rpt) ? new String(Base64.decodeBase64(rpt), "UTF-8") : new String(rpt, "UTF-8");
			log.debug("decodificaRPT: " + rpString);
			rpDocument = RPTDocument.Factory.parse(rpString);
			it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico ctRichiestaPagamento = rpDocument.getRPT();
			decodedString = ctRichiestaPagamento.toString();
		} catch (XmlException xmle) {
			throw new RuntimeException("Failed to parse RPT ::: XmlException :::", xmle);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Failed to parse RPT ::: UnsupportedEncodingException :::", uee);
		}
		return decodedString;
	}

}