package it.regioneveneto.mygov.payment.nodoregionalefesp.server;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.EsitoAttivaRPT;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.EsitoVerificaRPT;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaAttivaRPT;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaAttivaRPTRisposta;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaVerificaRPT;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaVerificaRPTRisposta;
import gov.telematici.pagamenti.ws.ppthead.IntestazionePPT;
import it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematiciccp.PagamentiTelematiciCCP;
import it.regioneveneto.mygov.payment.constants.Constants;
import it.regioneveneto.mygov.payment.constants.FaultCodeConstants;
import it.regioneveneto.mygov.payment.esterni.client.PagamentiTelematiciEsterniCCPServiceClient;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovAttivaRptE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.AttivaRPTService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.EnteService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.GiornaleService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPTRTService;
import it.regioneveneto.mygov.payment.pa.client.PagamentiTelematiciCCPServiceClient;
import it.veneto.regione.pagamenti.nodoregionalefesp.esterni.esterniperpa.ccp.PaaSILVerificaEsternaRisposta;
import it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.EsitoSILAttivaRP;
import it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.EsitoSILVerificaRP;
import it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.PaaSILAttivaRP;
import it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.PaaSILAttivaRPRisposta;
import it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.PaaSILVerificaRP;
import it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.PaaSILVerificaRPRisposta;
import it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.PaaTipoDatiPagamentoPA;
import it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.PaaTipoDatiPagamentoPSP;
import it.veneto.regione.schemas._2012.pagamenti.CtSoggettoPagatore;
import it.veneto.regione.schemas._2012.pagamenti.CtSoggettoVersante;

/**
 * @author regione del veneto
 *
 */
@javax.jws.WebService(serviceName = "PagamentiTelematiciCCPservice", portName = "PPTPort", targetNamespace = "http://NodoPagamentiSPC.spcoop.gov.it/servizi/PagamentiTelematiciCCP", wsdlLocation = "classpath:it/regioneveneto/mygov/payment/nodoregionalefesp/server/nodo-regionale-per-nodo-spc-pagamento-presso-psp.wsdl", endpointInterface = "it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematiciccp.PagamentiTelematiciCCP")
public class PagamentiTelematiciCCPImpl implements PagamentiTelematiciCCP {

	private static final Log log = LogFactory.getLog(PagamentiTelematiciCCPImpl.class.getName());

	// Bean per la chiamata al WS di pa
	private PagamentiTelematiciCCPServiceClient pagamentiTelematiciCCPServiceClient;
	
	// Bean per la chiamata al WS esterni
	private PagamentiTelematiciEsterniCCPServiceClient pagamentiTelematiciEsterniCCPServiceClient;

	private AttivaRPTService attivaRPTService;

	private ManageRPTRTService manageRPTRTService;

	private EnteService enteService;

	@Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
	private String nodoRegionaleFespIdentificativoIntermediarioPA;

	@Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
	private String nodoRegionaleFespIdentificativoStazioneIntermediarioPA;

	@Autowired
	private GiornaleService giornaleService;

	/**
	 * 
	 */
	public PagamentiTelematiciCCPImpl() {
		super();
	}

	/**
	 * @param pagamentiTelematiciCCPServiceClient the pagamentiTelematiciCCPServiceClient to set
	 */
	public void setPagamentiTelematiciCCPServiceClient(PagamentiTelematiciCCPServiceClient pagamentiTelematiciCCPServiceClient) {
		this.pagamentiTelematiciCCPServiceClient = pagamentiTelematiciCCPServiceClient;
	}

	public void setPagamentiTelematiciEsterniCCPServiceClient(
			PagamentiTelematiciEsterniCCPServiceClient pagamentiTelematiciEsterniCCPServiceClient) {
		this.pagamentiTelematiciEsterniCCPServiceClient = pagamentiTelematiciEsterniCCPServiceClient;
	}

	/**
	 * @param attivaRPTService the attivaRPTService to set
	 */
	public void setAttivaRPTService(AttivaRPTService attivaRPTService) {
		this.attivaRPTService = attivaRPTService;
	}

	/**
	 * @param manageRPTRTService the manageRPTRTService to set
	 */
	public void setManageRPTRTService(ManageRPTRTService manageRPTRTService) {
		this.manageRPTRTService = manageRPTRTService;
	}

	/**
	 * @param enteService the enteService to set
	 */
	public void setEnteService(EnteService enteService) {
		this.enteService = enteService;
	}

	/**
	 * @param nodoRegionaleFespIdentificativoIntermediarioPA the nodoRegionaleFespIdentificativoIntermediarioPA to set
	 */
	public void setNodoRegionaleFespIdentificativoIntermediarioPA(String nodoRegionaleFespIdentificativoIntermediarioPA) {
		this.nodoRegionaleFespIdentificativoIntermediarioPA = nodoRegionaleFespIdentificativoIntermediarioPA;
	}

	/**
	 * @param nodoRegionaleFespIdentificativoStazioneIntermediarioPA the nodoRegionaleFespIdentificativoStazioneIntermediarioPA to set
	 */
	public void setNodoRegionaleFespIdentificativoStazioneIntermediarioPA(String nodoRegionaleFespIdentificativoStazioneIntermediarioPA) {
		this.nodoRegionaleFespIdentificativoStazioneIntermediarioPA = nodoRegionaleFespIdentificativoStazioneIntermediarioPA;
	}

	/* (non-Javadoc)
	 * @see it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematiciccp.PagamentiTelematiciCCP#paaAttivaRPT(gov.telematici.pagamenti.ws.PaaAttivaRPT, gov.telematici.pagamenti.ws.ppthead.IntestazionePPT)
	 */
	public PaaAttivaRPTRisposta paaAttivaRPT(PaaAttivaRPT bodyrichiesta, gov.telematici.pagamenti.ws.ppthead.IntestazionePPT header) {

		log.debug("Executing operation paaAttivaRPT");
		log.debug("bodyrichiesta.IdentificativoPSP [" + bodyrichiesta.getIdentificativoPSP() + "]");
		
		log.debug("bodyrichiesta.IdentificativoIntermediarioPSP [" + bodyrichiesta.getIdentificativoIntermediarioPSP() + "]");
		log.debug("bodyrichiesta.IdentificativoCanalePSP [" + bodyrichiesta.getIdentificativoCanalePSP() + "]");
		
		log.debug("bodyrichiesta.DatiPagamentoPSP.ImportoSingoloVersamento [" + bodyrichiesta.getDatiPagamentoPSP().getImportoSingoloVersamento() + "]");
		log.debug("bodyrichiesta.DatiPagamentoPSP.IbanAppoggio [" + bodyrichiesta.getDatiPagamentoPSP().getIbanAppoggio() + "]");
		log.debug("bodyrichiesta.DatiPagamentoPSP.BicAppoggio [" + bodyrichiesta.getDatiPagamentoPSP().getBicAppoggio() + "]");
		log.debug("bodyrichiesta.DatiPagamentoPSP.IbanAddebito [" + bodyrichiesta.getDatiPagamentoPSP().getIbanAddebito() + "]");
		log.debug("bodyrichiesta.DatiPagamentoPSP.BicAddebito [" + bodyrichiesta.getDatiPagamentoPSP().getBicAddebito() + "]");
		if (bodyrichiesta.getDatiPagamentoPSP().getSoggettoPagatore() != null) {
			log.debug("bodyrichiesta.DatiPagamentoPSP.SoggettoPagatore.AnagraficaPagatore ["
					+ bodyrichiesta.getDatiPagamentoPSP().getSoggettoPagatore().getAnagraficaPagatore() + "]");
			log.debug("bodyrichiesta.DatiPagamentoPSP.SoggettoPagatore.CodiceIdentificativoUnivoco ["
					+ bodyrichiesta.getDatiPagamentoPSP().getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco() + "]");
			log.debug("bodyrichiesta.DatiPagamentoPSP.SoggettoPagatore.TipoIdentificativoUnivoco ["
					+ bodyrichiesta.getDatiPagamentoPSP().getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco() + "]");
		}
		if (bodyrichiesta.getDatiPagamentoPSP().getSoggettoVersante() != null) {
			log.debug("bodyrichiesta.DatiPagamentoPSP.SoggettoVersante.AnagraficaVersante ["
					+ bodyrichiesta.getDatiPagamentoPSP().getSoggettoVersante().getAnagraficaVersante() + "]");
			log.debug("bodyrichiesta.DatiPagamentoPSP.SoggettoVersante.CodiceIdentificativoUnivoco ["
					+ bodyrichiesta.getDatiPagamentoPSP().getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco() + "]");
			log.debug("bodyrichiesta.DatiPagamentoPSP.SoggettoVersante.TipoIdentificativoUnivoco ["
					+ bodyrichiesta.getDatiPagamentoPSP().getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() + "]");
		}

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
		String esito;
		try {
			dataOraEvento = new Date();
			identificativoDominio = header.getIdentificativoDominio();
			identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
			codiceContestoPagamento = header.getCodiceContestoPagamento();
			identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
			tipoVersamento = Constants.PAY_PRESSO_PSP;
			componente = Constants.COMPONENTE.FESP.toString();
			categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaAttivaRPT.toString();
			sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
			identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

			identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
			identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
			canalePagamento = "";

			xmlString = "";
			try {
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory();
				context = JAXBContext.newInstance(PaaAttivaRPT.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createPaaAttivaRPT(bodyrichiesta), sw);
				xmlString = sw.toString();
			} catch (JAXBException e) {
				log.error("paaAttivaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
			}

			parametriSpecificiInterfaccia = xmlString;

			esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
					identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
		} catch (Exception e1) {
			log.warn("paaAttivaRPT REQUEST impossibile inserire nel giornale degli eventi", e1);
		}

		PaaAttivaRPTRisposta paaAttivaRPTRisposta = new PaaAttivaRPTRisposta();
		paaAttivaRPTRisposta.setPaaAttivaRPTRisposta(new EsitoAttivaRPT());
		EsitoAttivaRPT esitoAttivaRPT = paaAttivaRPTRisposta.getPaaAttivaRPTRisposta();

		MygovAttivaRptE mygovAttivaRptE = attivaRPTService.getByKey(header.getIdentificativoDominio(), header.getIdentificativoUnivocoVersamento(),
				header.getCodiceContestoPagamento());

		if (mygovAttivaRptE != null && mygovAttivaRptE.getDeAttivarptEsito().equals("OK")) {
			log.warn(FaultCodeConstants.PAA_ATTIVA_RPT_DUPLICATA + ": [ATTIVA DUPLICATA]");
			esitoAttivaRPT.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
			gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
			faultBean.setFaultCode(FaultCodeConstants.PAA_ATTIVA_RPT_DUPLICATA);
			faultBean.setFaultString("Errore PAA_ATTIVA_RPT_DUPLICATA: Attiva duplicata");
			faultBean.setDescription("Errore PAA_ATTIVA_RPT_DUPLICATA: Attiva duplicata per identificativo dominio [" + header.getIdentificativoDominio()
					+ "], IUV [" + header.getIdentificativoUnivocoVersamento() + "], codice contesto pagamento [" + header.getCodiceContestoPagamento() + "]");
			faultBean.setId(header.getIdentificativoDominio());
			faultBean.setSerial(0);
			esitoAttivaRPT.setFault(faultBean);
			
			/*
			 * LOG GIORNALE DEGLI EVENTI
			 */
			try {
				dataOraEvento = new Date();
				identificativoDominio = header.getIdentificativoDominio();
				identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
				codiceContestoPagamento = header.getCodiceContestoPagamento();
				identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
				tipoVersamento = Constants.PAY_PRESSO_PSP;
				componente = Constants.COMPONENTE.FESP.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaAttivaRPT.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
				identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

				identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
				identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
				canalePagamento = "";

				xmlString = "";
				try {
					gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory();
					context = JAXBContext.newInstance(PaaAttivaRPTRisposta.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createPaaAttivaRPTRisposta(paaAttivaRPTRisposta), sw);
					xmlString = sw.toString();
				} catch (JAXBException e) {
					log.error("paaAttivaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
				}

				parametriSpecificiInterfaccia = xmlString;

				esito = esitoAttivaRPT.getEsito();

				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
						identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
			} catch (Exception e) {
				log.warn("paaAttivaRPT RESPONSE impossibile inserire nel giornale degli eventi", e);
			}
			
			return paaAttivaRPTRisposta;
		}

		String codAttivarptIdentificativoIntermediarioPa = null;
		String codAttivarptIdentificativoStazioneIntermediarioPa = null;
		String codAttivarptIdentificativoDominio = null;

		try {
			// RECUPERO I CAMPI PASSATI
			// Recuperati dall'header
			codAttivarptIdentificativoIntermediarioPa = header.getIdentificativoIntermediarioPA();
			codAttivarptIdentificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
			codAttivarptIdentificativoDominio = header.getIdentificativoDominio();

			if (!nodoRegionaleFespIdentificativoIntermediarioPA.equalsIgnoreCase(codAttivarptIdentificativoIntermediarioPa)
					|| !nodoRegionaleFespIdentificativoStazioneIntermediarioPA.equalsIgnoreCase(codAttivarptIdentificativoStazioneIntermediarioPa)) {

				log.warn(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [Identificativo Intermediario PA diverso da regione veneto]");
				esitoAttivaRPT.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
				faultBean.setFaultCode(FaultCodeConstants.PAA_ATTIVA_RPT_INTERMEDIARIO_SCONOSCIUTO);
				faultBean.setFaultString("Errore PAA_ATTIVA_RPT: Intermediario sconosciuto");
				faultBean.setDescription("Errore PAA_ATTIVA_RPT: Intermediario diverso da regione veneto");
				faultBean.setId(codAttivarptIdentificativoDominio);
				faultBean.setSerial(0);
				esitoAttivaRPT.setFault(faultBean);
				
				/*
				 * LOG GIORNALE DEGLI EVENTI
				 */
				try {
					dataOraEvento = new Date();
					identificativoDominio = header.getIdentificativoDominio();
					identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
					codiceContestoPagamento = header.getCodiceContestoPagamento();
					identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
					tipoVersamento = Constants.PAY_PRESSO_PSP;
					componente = Constants.COMPONENTE.FESP.toString();
					categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
					tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaAttivaRPT.toString();
					sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
					identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

					identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
					identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
					canalePagamento = "";

					xmlString = "";
					try {
						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory();
						context = JAXBContext.newInstance(PaaAttivaRPTRisposta.class);
						Marshaller marshaller = context.createMarshaller();
						StringWriter sw = new StringWriter();
						marshaller.marshal(objectFactory.createPaaAttivaRPTRisposta(paaAttivaRPTRisposta), sw);
						xmlString = sw.toString();
					} catch (JAXBException e) {
						log.error("paaAttivaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
					}

					parametriSpecificiInterfaccia = xmlString;

					esito = esitoAttivaRPT.getEsito();

					giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
							identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
				} catch (Exception e) {
					log.warn("paaAttivaRPT RESPONSE impossibile inserire nel giornale degli eventi", e);
				}
				
				return paaAttivaRPTRisposta;
			}

			MygovEnte enteProp = enteService.getByCodiceFiscale(codAttivarptIdentificativoDominio);
//			List<MygovEntepsp> mygovEntepspList = entePspService.getByCodiceIpa(enteProp.getCodIpaEnte());
//			boolean trovato = false;
//			for (MygovEntepsp mygovEntepsp : mygovEntepspList) {
//				if (mygovEntepsp.getIdentificativoPsp().equalsIgnoreCase(codAttivarptIdPsp)) {
//					trovato = true;
//					break;
//				}
//			}
//			if (!trovato) {

			/**
			 *  RIMOSSO IN DATA 06-02-2019
			 *  Non vengono più effettuati controlli su tabella mygov_catalogoinformativo
			 */
//			MygovCatalogopsp mygovCatalogopsp = catalogoPspService.getAssociazioneCatalogoPsp(codAttivarptIdPsp);
//			if(mygovCatalogopsp == null) {
//				log.warn(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [Identificativo PSP non presente per ente]");
//				esitoAttivaRPT.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
//				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
//				faultBean.setFaultCode(FaultCodeConstants.PAA_ATTIVA_RPT_PSP_SCONOSCIUTO);
//				faultBean.setFaultString("Errore PAA_ATTIVA_RPT: psp sconosciuto");
//				faultBean.setDescription("Errore PAA_ATTIVA_RPT: psp sconosciuto per l'ente");
//				faultBean.setId(codAttivarptIdentificativoDominio);
//				faultBean.setSerial(0);
//				esitoAttivaRPT.setFault(faultBean);
//				
//				/*
//				 * LOG GIORNALE DEGLI EVENTI
//				 */
//				try {
//					dataOraEvento = new Date();
//					identificativoDominio = header.getIdentificativoDominio();
//					identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
//					codiceContestoPagamento = header.getCodiceContestoPagamento();
//					identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
//					tipoVersamento = Constants.PAY_PRESSO_PSP;
//					componente = Constants.COMPONENTE.FESP.toString();
//					categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
//					tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaAttivaRPT.toString();
//					sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
//					identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;
//
//					identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
//					identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
//					canalePagamento = "";
//
//					xmlString = "";
//					try {
//						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory();
//						context = JAXBContext.newInstance(PaaAttivaRPTRisposta.class);
//						Marshaller marshaller = context.createMarshaller();
//						StringWriter sw = new StringWriter();
//						marshaller.marshal(objectFactory.createPaaAttivaRPTRisposta(paaAttivaRPTRisposta), sw);
//						xmlString = sw.toString();
//					} catch (JAXBException e) {
//						log.error("paaAttivaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
//					}
//
//					parametriSpecificiInterfaccia = xmlString;
//
//					esito = esitoAttivaRPT.getEsito();
//
//					giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
//							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
//							identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
//				} catch (Exception e) {
//					log.warn("paaAttivaRPT RESPONSE impossibile inserire nel giornale degli eventi", e);
//				}
//				
//				return paaAttivaRPTRisposta;
//			}

			// Persiste la request della richiesta attivazione
			mygovAttivaRptE = persistiRequestAttivazione(header, bodyrichiesta);

			log.debug("persistita richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
					+ bodyrichiesta.getIdentificativoPSP() + "] e codice contesto pagamento [" + header.getCodiceContestoPagamento() + "]");

			// CHIAMATA A PA
			it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT headerAttivaRPTToCallPa = new it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT();
			PaaSILAttivaRP bodyRichiestaAttivaRPTToCallPa = new PaaSILAttivaRP();
			buildHeaderRPTToCallPa(header, headerAttivaRPTToCallPa);
			buildBodyRichiestaAttivaRPTToCallPa(bodyrichiesta, bodyRichiestaAttivaRPTToCallPa);

			/*
			 * LOG GIORNALE DEGLI EVENTI
			 */
			try {
				dataOraEvento = new Date();
				identificativoDominio = header.getIdentificativoDominio();
				identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
				codiceContestoPagamento = header.getCodiceContestoPagamento();
				identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
				tipoVersamento = Constants.PAY_PRESSO_PSP;
				componente = Constants.COMPONENTE.FESP.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaSILAttivaRP.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
				identificativoFruitore = header.getIdentificativoStazioneIntermediarioPA();
				identificativoErogatore = header.getIdentificativoDominio();
				identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
				canalePagamento = "";

				xmlString = "";
				try {
					it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.ObjectFactory objectFactory = new it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.ObjectFactory();
					context = JAXBContext.newInstance(PaaSILAttivaRP.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createPaaSILAttivaRP(bodyRichiestaAttivaRPTToCallPa), sw);
					xmlString = sw.toString();
				} catch (JAXBException e) {
					log.error("paaAttivaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
				}

				parametriSpecificiInterfaccia = xmlString;

				esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
						identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
			} catch (Exception e1) {
				log.warn("paaSILAttivaRP REQUEST impossibile inserire nel giornale degli eventi", e1);
			}

			// Effettua la chiamata a PA dell'attiva RP
			PaaSILAttivaRPRisposta paaSILAttivaRPRispostaDaPa = pagamentiTelematiciCCPServiceClient.paaSILAttivaRP(bodyRichiestaAttivaRPTToCallPa,
					headerAttivaRPTToCallPa);

			/*
			 * LOG GIORNALE DEGLI EVENTI RISPOSTA DI PA
			 */
			try {
				dataOraEvento = new Date();
				identificativoDominio = header.getIdentificativoDominio();
				identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
				codiceContestoPagamento = header.getCodiceContestoPagamento();
				identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
				tipoVersamento = Constants.PAY_PRESSO_PSP;
				componente = Constants.COMPONENTE.FESP.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaSILAttivaRP.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
				identificativoFruitore =header.getIdentificativoStazioneIntermediarioPA();
				identificativoErogatore = header.getIdentificativoDominio();
				identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
				canalePagamento = "";

				xmlString = "";
				try {
					it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.ObjectFactory objectFactory = new it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.ObjectFactory();
					context = JAXBContext.newInstance(PaaSILAttivaRPRisposta.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createPaaSILAttivaRPRisposta(paaSILAttivaRPRispostaDaPa), sw);
					xmlString = sw.toString();
				} catch (JAXBException e) {
					log.error("paaAttivaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
				}

				parametriSpecificiInterfaccia = xmlString;

				esito = paaSILAttivaRPRispostaDaPa.getPaaSILAttivaRPRisposta().getEsito();

				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
						identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
			} catch (Exception e) {
				log.warn("paaSILAttivaRP RESPONSE impossibile inserire nel giornale degli eventi", e);
			}

			// Persiste la response della richiesta attivazione
			persistiResponseAttivazione(header, bodyrichiesta, paaSILAttivaRPRispostaDaPa, enteProp, mygovAttivaRptE.getMygovAttivaRptEId());

			log.debug("persistita risposta pa per richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
					+ bodyrichiesta.getIdentificativoPSP() + "]");

			// Prepara la risposta per SPC

			EsitoSILAttivaRP esitoSILAttivaRP = paaSILAttivaRPRispostaDaPa.getPaaSILAttivaRPRisposta();

			if (esitoSILAttivaRP != null) {
				PaaTipoDatiPagamentoPA datiPagamentoPA = esitoSILAttivaRP.getDatiPagamentoPA();
				it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.FaultBean faultBeanPA = esitoSILAttivaRP.getFault();

				esitoAttivaRPT.setEsito(esitoSILAttivaRP.getEsito());

				log.debug("risposta da PA per richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
						+ bodyrichiesta.getIdentificativoPSP() + "] esito [" + esitoSILAttivaRP.getEsito() + "]");
				if (esitoSILAttivaRP.getEsito().equals(Constants.NODO_REGIONALE_FESP_ESITO_OK)) {

					if (datiPagamentoPA != null) {

						log.debug("risposta per SPC per richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
								+ bodyrichiesta.getIdentificativoPSP() + "]: ente [" + enteProp + "]");
						log.debug("risposta per SPC per richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
								+ bodyrichiesta.getIdentificativoPSP() + "]: BicAccredito [" + datiPagamentoPA.getBicAccredito() + "]");
						log.debug("risposta per SPC per richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
								+ bodyrichiesta.getIdentificativoPSP() + "]: CausaleVersamento [" + datiPagamentoPA.getCausaleVersamento() + "]");
						log.debug("risposta per SPC per richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
								+ bodyrichiesta.getIdentificativoPSP() + "]: CredenzialiPagatore [" + datiPagamentoPA.getCredenzialiPagatore() + "]");
						log.debug("risposta per SPC per richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
								+ bodyrichiesta.getIdentificativoPSP() + "]: IbanAccredito [" + datiPagamentoPA.getIbanAccredito() + "]");
						log.debug("risposta per SPC per richiesta di attivazione per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
								+ bodyrichiesta.getIdentificativoPSP() + "]: ImportoSingoloVersamento [" + datiPagamentoPA.getImportoSingoloVersamento() + "]");

						esitoAttivaRPT.setDatiPagamentoPA(new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaTipoDatiPagamentoPA());
						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA = esitoAttivaRPT.getDatiPagamentoPA();

						paaTipoDatiPagamentoPA.setBicAccredito(datiPagamentoPA.getBicAccredito());
						paaTipoDatiPagamentoPA.setCausaleVersamento(datiPagamentoPA.getCausaleVersamento());
						paaTipoDatiPagamentoPA.setCredenzialiPagatore(datiPagamentoPA.getCredenzialiPagatore());
						paaTipoDatiPagamentoPA.setIbanAccredito(datiPagamentoPA.getIbanAccredito());
						paaTipoDatiPagamentoPA.setImportoSingoloVersamento(datiPagamentoPA.getImportoSingoloVersamento());
						// ENTE BENEFICIARIO DA FESP QUELLO DI PA E' INCOMPLETO
						paaTipoDatiPagamentoPA.setEnteBeneficiario(buildEnteBeneficiario(enteProp));


					} else {
						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
						faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
						faultBean.setFaultString("Errore generico PAA_ATTIVA_RPT: esito OK ma paaTipoDatiPagamentoPA null");
						faultBean.setDescription("Errore generico PAA_ATTIVA_RPT: esito OK ma paaTipoDatiPagamentoPA null");
						faultBean.setId(codAttivarptIdentificativoDominio);
						faultBean.setSerial(0);
						esitoAttivaRPT.setFault(faultBean);
					}
				} else {
					if (faultBeanPA != null) {
						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
						faultBean.setFaultCode(faultBeanPA.getFaultCode());
						faultBean.setFaultString(faultBeanPA.getFaultString());
						faultBean.setId(faultBeanPA.getId());
						faultBean.setDescription(faultBeanPA.getDescription());
						
						if(StringUtils.isNotBlank(faultBeanPA.getOriginalFaultCode()))
							faultBean.setOriginalFaultCode(faultBeanPA.getOriginalFaultCode());
						if(StringUtils.isNotBlank(faultBeanPA.getOriginalFaultString()))
							faultBean.setOriginalFaultString(faultBeanPA.getOriginalFaultString());
						if(StringUtils.isNotBlank(faultBeanPA.getOriginalDescription()))
							faultBean.setOriginalDescription(faultBeanPA.getOriginalDescription());
						
						faultBean.setSerial(faultBeanPA.getSerial());
						esitoAttivaRPT.setFault(faultBean);
					} else {
						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
						faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
						faultBean.setFaultString("Errore generico PAA_ATTIVA_RPT: esito KO ma faultBean null");
						faultBean.setDescription("Errore generico PAA_ATTIVA_RPT: esito KO ma faultBean null");
						faultBean.setId(codAttivarptIdentificativoDominio);
						faultBean.setSerial(0);
						esitoAttivaRPT.setFault(faultBean);
					}
				}
			}

		} catch (Exception ex) {

			log.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [" + ex.getMessage() + "]", ex);
			esitoAttivaRPT.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
			gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
			faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
			faultBean.setFaultString("Errore generico PAA_ATTIVA_RPT: " + ex.getCause());
			faultBean.setDescription("Errore generico PAA_ATTIVA_RPT: " + ex.getMessage());
			faultBean.setId(codAttivarptIdentificativoDominio);
			faultBean.setSerial(0);
			esitoAttivaRPT.setFault(faultBean);
		}

		/*
		 * LOG GIORNALE DEGLI EVENTI
		 */
		try {
			dataOraEvento = new Date();
			identificativoDominio = header.getIdentificativoDominio();
			identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
			codiceContestoPagamento = header.getCodiceContestoPagamento();
			identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
			tipoVersamento = Constants.PAY_PRESSO_PSP;
			componente = Constants.COMPONENTE.FESP.toString();
			categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaAttivaRPT.toString();
			sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
			identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

			identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
			identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
			canalePagamento = "";

			xmlString = "";
			try {
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory();
				context = JAXBContext.newInstance(PaaAttivaRPTRisposta.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createPaaAttivaRPTRisposta(paaAttivaRPTRisposta), sw);
				xmlString = sw.toString();
			} catch (JAXBException e) {
				log.error("paaAttivaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
			}

			parametriSpecificiInterfaccia = xmlString;

			esito = esitoAttivaRPT.getEsito();

			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
					identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
		} catch (Exception e) {
			log.warn("paaAttivaRPT RESPONSE impossibile inserire nel giornale degli eventi", e);
		}

		return paaAttivaRPTRisposta;
	}

	/**
	 * @param header
	 * @param bodyrichiesta
	 * @param paaSILAttivaRPRispostaDaPa
	 * @param enteProp
	 * @param mygovAttivaRptEId
	 */
	private void persistiResponseAttivazione(IntestazionePPT header, PaaAttivaRPT bodyrichiesta, PaaSILAttivaRPRisposta paaSILAttivaRPRispostaDaPa,
			MygovEnte enteProp, long mygovAttivaRptEId) {

		// Recuperati dall'header
		// String codAttivarptIdentificativoIntermediarioPa =
		// header.getIdentificativoIntermediarioPA();
		// String codAttivarptIdentificativoStazioneIntermediarioPa =
		// header.getIdentificativoStazioneIntermediarioPA();
		// String codAttivarptIdentificativoDominio = header.getIdentificativoDominio();
		// String codAttivarptIdentificativoUnivocoVersamento =
		// header.getIdentificativoUnivocoVersamento();
		// String codAttivarptCodiceContestoPagamento =
		// header.getCodiceContestoPagamento();

		// Recuperati dal body
		// String codAttivarptIdPsp = bodyrichiesta.getIdentificativoPSP();

		// Recuperati dai Dati Pagamento
		BigDecimal numEAttivarptImportoSingoloVersamento = null;
		String deEAttivarptIbanAccredito = null;
		String deEAttivarptBicAccredito = null;
		String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco = null;
		String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco = null;
		String deEAttivarptEnteBenefDenominazioneBeneficiario = null;
		String codEAttivarptEnteBenefCodiceUnitOperBeneficiario = null;
		String deEAttivarptEnteBenefDenomUnitOperBeneficiario = null;
		String deEAttivarptEnteBenefIndirizzoBeneficiario = null;
		String deEAttivarptEnteBenefCivicoBeneficiario = null;
		String codEAttivarptEnteBenefCapBeneficiario = null;
		String deEAttivarptEnteBenefLocalitaBeneficiario = null;
		String deEAttivarptEnteBenefProvinciaBeneficiario = null;
		String codEAttivarptEnteBenefNazioneBeneficiario = null;
		String deEAttivarptCredenzialiPagatore = null;
		String deEAttivarptCausaleVersamento = null;
		String deAttivarptEsito = null;
		String codAttivarptFaultCode = null;
		String deAttivarptFaultString = null;
		String codAttivarptId = null;
		String deAttivarptDescription = null;
		Integer codAttivarptSerial = null;
		String codAttivarptOriginalFaultCode = null; 
		String deAttivarptOriginalFaultString = null; 
		String deAttivarptOriginalFaultDescription = null;

		EsitoSILAttivaRP esitoSILAttivaRP = paaSILAttivaRPRispostaDaPa.getPaaSILAttivaRPRisposta();
		if (esitoSILAttivaRP != null) {
			PaaTipoDatiPagamentoPA datiPagamentoDaPa = esitoSILAttivaRP.getDatiPagamentoPA();

			if (datiPagamentoDaPa != null) {
				deEAttivarptCausaleVersamento = datiPagamentoDaPa.getCausaleVersamento();
				numEAttivarptImportoSingoloVersamento = datiPagamentoDaPa.getImportoSingoloVersamento();
				deEAttivarptIbanAccredito = datiPagamentoDaPa.getIbanAccredito();
				deEAttivarptBicAccredito = datiPagamentoDaPa.getBicAccredito();
				deEAttivarptCredenzialiPagatore = datiPagamentoDaPa.getCredenzialiPagatore();

				it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario enteBeneficiario = buildEnteBeneficiario(enteProp);
				if (enteBeneficiario != null) {
					codEAttivarptEnteBenefCapBeneficiario = enteBeneficiario.getCapBeneficiario();
					deEAttivarptEnteBenefCivicoBeneficiario = enteBeneficiario.getCivicoBeneficiario();
					codEAttivarptEnteBenefCodiceUnitOperBeneficiario = enteBeneficiario.getCodiceUnitOperBeneficiario();
					deEAttivarptEnteBenefDenominazioneBeneficiario = enteBeneficiario.getDenominazioneBeneficiario();
					deEAttivarptEnteBenefDenomUnitOperBeneficiario = enteBeneficiario.getDenomUnitOperBeneficiario();
					deEAttivarptEnteBenefIndirizzoBeneficiario = enteBeneficiario.getIndirizzoBeneficiario();
					deEAttivarptEnteBenefLocalitaBeneficiario = enteBeneficiario.getLocalitaBeneficiario();
					codEAttivarptEnteBenefNazioneBeneficiario = enteBeneficiario.getNazioneBeneficiario();
					deEAttivarptEnteBenefProvinciaBeneficiario = enteBeneficiario.getProvinciaBeneficiario();

					if (enteBeneficiario.getIdentificativoUnivocoBeneficiario() != null
							&& enteBeneficiario.getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco() != null) {
						codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco = enteBeneficiario.getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco()
								.value();
						codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco = enteBeneficiario.getIdentificativoUnivocoBeneficiario()
								.getCodiceIdentificativoUnivoco();
					}
				}
			}

			// Esito
			deAttivarptEsito = esitoSILAttivaRP.getEsito();
			// Codici errore
			it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.FaultBean faultBean = esitoSILAttivaRP.getFault();
			if (faultBean != null) {
				codAttivarptFaultCode = faultBean.getFaultCode();
				deAttivarptFaultString = faultBean.getFaultString();
				codAttivarptId = faultBean.getId();
				deAttivarptDescription = faultBean.getDescription();
				codAttivarptSerial = faultBean.getSerial();
				if(StringUtils.isNotBlank(faultBean.getOriginalFaultCode()))
					codAttivarptOriginalFaultCode = faultBean.getOriginalFaultCode();
				if(StringUtils.isNotBlank(faultBean.getOriginalFaultString()))
					deAttivarptOriginalFaultString = faultBean.getOriginalFaultString();
				if(StringUtils.isNotBlank(faultBean.getOriginalDescription()))
					deAttivarptOriginalFaultDescription = faultBean.getOriginalDescription();
			}

			// PERSISTE LA RISPOSTA ALLA RICHIESTA DI ATTIVAZIONE
			attivaRPTService.updateByKey(mygovAttivaRptEId, new Date(), numEAttivarptImportoSingoloVersamento, deEAttivarptIbanAccredito,
					deEAttivarptBicAccredito, codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco, codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco,
					deEAttivarptEnteBenefDenominazioneBeneficiario, codEAttivarptEnteBenefCodiceUnitOperBeneficiario,
					deEAttivarptEnteBenefDenomUnitOperBeneficiario, deEAttivarptEnteBenefIndirizzoBeneficiario, deEAttivarptEnteBenefCivicoBeneficiario,
					codEAttivarptEnteBenefCapBeneficiario, deEAttivarptEnteBenefLocalitaBeneficiario, deEAttivarptEnteBenefProvinciaBeneficiario,
					codEAttivarptEnteBenefNazioneBeneficiario, deEAttivarptCredenzialiPagatore, deEAttivarptCausaleVersamento, deAttivarptEsito,
					codAttivarptFaultCode, deAttivarptFaultString, codAttivarptId, deAttivarptDescription, codAttivarptSerial,
					codAttivarptOriginalFaultCode, deAttivarptOriginalFaultString, deAttivarptOriginalFaultDescription);
		}
	}

	/**
	 * @param header
	 * @param bodyrichiesta
	 * @return
	 */
	private MygovAttivaRptE persistiRequestAttivazione(IntestazionePPT header, PaaAttivaRPT bodyrichiesta) {

		String codAttivarptIdentificativoIntermediarioPa = header.getIdentificativoIntermediarioPA();
		String codAttivarptIdentificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
		String codAttivarptIdentificativoDominio = header.getIdentificativoDominio();
		String codAttivarptIdentificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
		String codAttivarptCodiceContestoPagamento = header.getCodiceContestoPagamento();

		String codAttivarptIdPsp = bodyrichiesta.getIdentificativoPSP();
		String codAttivarptIdIntermediarioPsp = bodyrichiesta.getIdentificativoIntermediarioPSP();
		String codAttivarptIdCanalePsp = bodyrichiesta.getIdentificativoCanalePSP();

		gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaTipoDatiPagamentoPSP datiPagamentoPSP = bodyrichiesta.getDatiPagamentoPSP();
		BigDecimal numAttivarptImportoSingoloVersamento = datiPagamentoPSP.getImportoSingoloVersamento();

		String deAttivarptIbanAddebito = datiPagamentoPSP.getIbanAddebito();
		String deAttivarptBicAddebito = datiPagamentoPSP.getBicAddebito();

		/* Optional */
		String deAttivarptIbanAppoggio = null;
		if (StringUtils.isNotBlank(datiPagamentoPSP.getIbanAppoggio())) {
			deAttivarptIbanAppoggio = datiPagamentoPSP.getIbanAppoggio();
		}

		/* Optional */
		String deAttivarptBicAppoggio = null;
		if (StringUtils.isNotBlank(datiPagamentoPSP.getBicAppoggio())) {
			deAttivarptBicAppoggio = datiPagamentoPSP.getBicAppoggio();
		}

		/* Optional */
		String codAttivarptSoggVersIdUnivVersTipoIdUnivoco = null;
		String codAttivarptSoggVersIdUnivVersCodiceIdUnivoco = null;
		String deAttivarptSoggVersAnagraficaVersante = null;
		String deAttivarptSoggVersIndirizzoVersante = null;
		String deAttivarptSoggVersCivicoVersante = null;
		String codAttivarptSoggVersCapVersante = null;
		String deAttivarptSoggVersLocalitaVersante = null;
		String deAttivarptSoggVersProvinciaVersante = null;
		String codAttivarptSoggVersNazioneVersante = null;
		String deAttivarptSoggVersEmailVersante = null;

		if (datiPagamentoPSP.getSoggettoVersante() != null) {
			if (datiPagamentoPSP.getSoggettoVersante().getIdentificativoUnivocoVersante() != null
					&& datiPagamentoPSP.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() != null) {
				codAttivarptSoggVersIdUnivVersTipoIdUnivoco = datiPagamentoPSP.getSoggettoVersante().getIdentificativoUnivocoVersante()
						.getTipoIdentificativoUnivoco().value();
				codAttivarptSoggVersIdUnivVersCodiceIdUnivoco = datiPagamentoPSP.getSoggettoVersante().getIdentificativoUnivocoVersante()
						.getCodiceIdentificativoUnivoco();
			}
			deAttivarptSoggVersAnagraficaVersante = datiPagamentoPSP.getSoggettoVersante().getAnagraficaVersante();
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getIndirizzoVersante())) {
				deAttivarptSoggVersIndirizzoVersante = datiPagamentoPSP.getSoggettoVersante().getIndirizzoVersante();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getCivicoVersante())) {
				deAttivarptSoggVersCivicoVersante = datiPagamentoPSP.getSoggettoVersante().getCivicoVersante();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getCapVersante())) {
				codAttivarptSoggVersCapVersante = datiPagamentoPSP.getSoggettoVersante().getCapVersante();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getLocalitaVersante())) {
				deAttivarptSoggVersLocalitaVersante = datiPagamentoPSP.getSoggettoVersante().getLocalitaVersante();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getProvinciaVersante())) {
				deAttivarptSoggVersProvinciaVersante = datiPagamentoPSP.getSoggettoVersante().getProvinciaVersante();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getNazioneVersante())) {
				codAttivarptSoggVersNazioneVersante = datiPagamentoPSP.getSoggettoVersante().getNazioneVersante();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoVersante().getEMailVersante())) {
				deAttivarptSoggVersEmailVersante = datiPagamentoPSP.getSoggettoVersante().getEMailVersante();
			}
		}

		String codAttivarptSoggPagIdUnivPagTipoIdUnivoco = null;
		String codAttivarptSoggPagIdUnivPagCodiceIdUnivoco = null;
		String deAttivarptSoggPagAnagraficaPagatore = null;
		String deAttivarptSoggPagIndirizzoPagatore = null;
		String deAttivarptSoggPagCivicoPagatore = null;
		String codAttivarptSoggPagCapPagatore = null;
		String deAttivarptSoggPagLocalitaPagatore = null;
		String deAttivarptSoggPagProvinciaPagatore = null;
		String codAttivarptSoggPagNazionePagatore = null;
		String deAttivarptSoggPagEmailPagatore = null;

		if (datiPagamentoPSP.getSoggettoPagatore() != null) {
			if (datiPagamentoPSP.getSoggettoPagatore().getIdentificativoUnivocoPagatore() != null
					&& datiPagamentoPSP.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco() != null) {
				codAttivarptSoggPagIdUnivPagTipoIdUnivoco = datiPagamentoPSP.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
						.getTipoIdentificativoUnivoco().value();
				codAttivarptSoggPagIdUnivPagCodiceIdUnivoco = datiPagamentoPSP.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
						.getCodiceIdentificativoUnivoco();
			}
			deAttivarptSoggPagAnagraficaPagatore = datiPagamentoPSP.getSoggettoPagatore().getAnagraficaPagatore();
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getIndirizzoPagatore())) {
				deAttivarptSoggPagIndirizzoPagatore = datiPagamentoPSP.getSoggettoPagatore().getIndirizzoPagatore();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getCivicoPagatore())) {
				deAttivarptSoggPagCivicoPagatore = datiPagamentoPSP.getSoggettoPagatore().getCivicoPagatore();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getCapPagatore())) {
				codAttivarptSoggPagCapPagatore = datiPagamentoPSP.getSoggettoPagatore().getCapPagatore();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getLocalitaPagatore())) {
				deAttivarptSoggPagLocalitaPagatore = datiPagamentoPSP.getSoggettoPagatore().getLocalitaPagatore();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getProvinciaPagatore())) {
				deAttivarptSoggPagProvinciaPagatore = datiPagamentoPSP.getSoggettoPagatore().getProvinciaPagatore();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getNazionePagatore())) {
				codAttivarptSoggPagNazionePagatore = datiPagamentoPSP.getSoggettoPagatore().getNazionePagatore();
			}
			/* Optional */
			if (StringUtils.isNotBlank(datiPagamentoPSP.getSoggettoPagatore().getEMailPagatore())) {
				deAttivarptSoggPagEmailPagatore = datiPagamentoPSP.getSoggettoPagatore().getEMailPagatore();
			}
		}

		// PERSISTE LA RICHIESTA DI ATTIVAZIONE
		return attivaRPTService.insert(codAttivarptIdPsp, codAttivarptIdIntermediarioPsp, codAttivarptIdCanalePsp, codAttivarptIdentificativoIntermediarioPa, codAttivarptIdentificativoStazioneIntermediarioPa,
				codAttivarptIdentificativoDominio, codAttivarptIdentificativoUnivocoVersamento, codAttivarptCodiceContestoPagamento, new Date(),
				numAttivarptImportoSingoloVersamento, deAttivarptIbanAppoggio, deAttivarptBicAppoggio, codAttivarptSoggVersIdUnivVersTipoIdUnivoco,
				codAttivarptSoggVersIdUnivVersCodiceIdUnivoco, deAttivarptSoggVersAnagraficaVersante, deAttivarptSoggVersIndirizzoVersante,
				deAttivarptSoggVersCivicoVersante, codAttivarptSoggVersCapVersante, deAttivarptSoggVersLocalitaVersante, deAttivarptSoggVersProvinciaVersante,
				codAttivarptSoggVersNazioneVersante, deAttivarptSoggVersEmailVersante, deAttivarptIbanAddebito, deAttivarptBicAddebito,
				codAttivarptSoggPagIdUnivPagTipoIdUnivoco, codAttivarptSoggPagIdUnivPagCodiceIdUnivoco, deAttivarptSoggPagAnagraficaPagatore,
				deAttivarptSoggPagIndirizzoPagatore, deAttivarptSoggPagCivicoPagatore, codAttivarptSoggPagCapPagatore, deAttivarptSoggPagLocalitaPagatore,
				deAttivarptSoggPagProvinciaPagatore, codAttivarptSoggPagNazionePagatore, deAttivarptSoggPagEmailPagatore);
	}

	/**
	 * @param enteProp
	 * @return
	 */
	private it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario buildEnteBeneficiario(MygovEnte enteProp) {

		// l'ente sull'rp nn c'e' (prendere da tabella ente)
		it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario enteBeneficiario = new it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario();

		it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaG idUnivocoPersonaG = new it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaG();
		idUnivocoPersonaG.setTipoIdentificativoUnivoco(it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersG.fromValue(enteProp
				.getCodRpEnteBenefIdUnivBenefTipoIdUnivoco()));
		idUnivocoPersonaG.setCodiceIdentificativoUnivoco(enteProp.getCodRpEnteBenefIdUnivBenefCodiceIdUnivoco());
		enteBeneficiario.setIdentificativoUnivocoBeneficiario(idUnivocoPersonaG);

		enteBeneficiario.setDenominazioneBeneficiario(enteProp.getDeRpEnteBenefDenominazioneBeneficiario());

		if (StringUtils.isNotBlank(enteProp.getDeRpEnteBenefDenomUnitOperBeneficiario())) {
			enteBeneficiario.setDenomUnitOperBeneficiario(enteProp.getDeRpEnteBenefDenomUnitOperBeneficiario());
		}
		if (StringUtils.isNotBlank(enteProp.getCodRpEnteBenefCodiceUnitOperBeneficiario())) {
			enteBeneficiario.setCodiceUnitOperBeneficiario(enteProp.getCodRpEnteBenefCodiceUnitOperBeneficiario());
		}
		if (StringUtils.isNotBlank(enteProp.getDeRpEnteBenefLocalitaBeneficiario())) {
			enteBeneficiario.setLocalitaBeneficiario(enteProp.getDeRpEnteBenefLocalitaBeneficiario());
		}
		if (StringUtils.isNotBlank(enteProp.getDeRpEnteBenefProvinciaBeneficiario())) {
			enteBeneficiario.setProvinciaBeneficiario(enteProp.getDeRpEnteBenefProvinciaBeneficiario());
		}
		if (StringUtils.isNotBlank(enteProp.getDeRpEnteBenefIndirizzoBeneficiario())) {
			enteBeneficiario.setIndirizzoBeneficiario(enteProp.getDeRpEnteBenefIndirizzoBeneficiario());
		}
		if (StringUtils.isNotBlank(enteProp.getDeRpEnteBenefCivicoBeneficiario())) {
			enteBeneficiario.setCivicoBeneficiario(enteProp.getDeRpEnteBenefCivicoBeneficiario());
		}
		if (StringUtils.isNotBlank(enteProp.getCodRpEnteBenefCapBeneficiario())) {
			enteBeneficiario.setCapBeneficiario(enteProp.getCodRpEnteBenefCapBeneficiario());
		}
		if (StringUtils.isNotBlank(enteProp.getCodRpEnteBenefNazioneBeneficiario())) {
			enteBeneficiario.setNazioneBeneficiario(enteProp.getCodRpEnteBenefNazioneBeneficiario());
		}
		return enteBeneficiario;
	}

	/**
	 * @param header
	 * @param headerAttivaRPTToCallPa
	 */
	private void buildHeaderRPTToCallPa(IntestazionePPT header, it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT headerAttivaRPTToCallPa) {
		headerAttivaRPTToCallPa.setCodiceContestoPagamento(header.getCodiceContestoPagamento());
		headerAttivaRPTToCallPa.setIdentificativoDominio(header.getIdentificativoDominio());
		headerAttivaRPTToCallPa.setIdentificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento());
		headerAttivaRPTToCallPa.setIdentificativoIntermediarioPA(header.getIdentificativoIntermediarioPA());
		headerAttivaRPTToCallPa.setIdentificativoStazioneIntermediarioPA(header.getIdentificativoStazioneIntermediarioPA());
	}

	/**
	 * @param bodyrichiesta
	 * @param bodyRichiestaAttivaRPTToCallPa
	 */
	private void buildBodyRichiestaAttivaRPTToCallPa(PaaAttivaRPT bodyrichiesta, PaaSILAttivaRP bodyRichiestaAttivaRPTToCallPa) {

		bodyRichiestaAttivaRPTToCallPa.setIdentificativoPSP(bodyrichiesta.getIdentificativoPSP());
		
		if (StringUtils.isNotBlank(bodyrichiesta.getIdentificativoIntermediarioPSP())) {
			bodyRichiestaAttivaRPTToCallPa.setIdentificativoIntermediarioPSP(bodyrichiesta.getIdentificativoIntermediarioPSP());
		}
		
		if (StringUtils.isNotBlank(bodyrichiesta.getIdentificativoCanalePSP())) {
			bodyRichiestaAttivaRPTToCallPa.setIdentificativoCanalePSP(bodyrichiesta.getIdentificativoCanalePSP());
		}

		PaaTipoDatiPagamentoPSP paaTipoDatiPagamentoToCallPa = new PaaTipoDatiPagamentoPSP();
		buildPaaTipoDatiPagamentoToCallPa(bodyrichiesta.getDatiPagamentoPSP(), paaTipoDatiPagamentoToCallPa);
		bodyRichiestaAttivaRPTToCallPa.setDatiPagamentoPSP(paaTipoDatiPagamentoToCallPa);
	}

	/**
	 * @param datiPagamentoPSP
	 * @param paaTipoDatiPagamentoToCallPa
	 */
	private void buildPaaTipoDatiPagamentoToCallPa(gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaTipoDatiPagamentoPSP datiPagamentoPSP,
			PaaTipoDatiPagamentoPSP paaTipoDatiPagamentoToCallPa) {

		if (StringUtils.isNotBlank(datiPagamentoPSP.getIbanAppoggio())) {
			paaTipoDatiPagamentoToCallPa.setIbanAppoggio(datiPagamentoPSP.getIbanAppoggio());
		}
		if (StringUtils.isNotBlank(datiPagamentoPSP.getBicAppoggio())) {
			paaTipoDatiPagamentoToCallPa.setBicAppoggio(datiPagamentoPSP.getBicAppoggio());
		}
		if (StringUtils.isNotBlank(datiPagamentoPSP.getIbanAddebito())) {
			paaTipoDatiPagamentoToCallPa.setIbanAddebito(datiPagamentoPSP.getIbanAddebito());
		}
		if (StringUtils.isNotBlank(datiPagamentoPSP.getBicAddebito())) {
			paaTipoDatiPagamentoToCallPa.setBicAddebito(datiPagamentoPSP.getBicAddebito());
		}
		if (datiPagamentoPSP.getImportoSingoloVersamento() != null) {
			paaTipoDatiPagamentoToCallPa.setImportoSingoloVersamento(datiPagamentoPSP.getImportoSingoloVersamento());
		}

		CtSoggettoPagatore soggettoPagatoreToCallPa = new CtSoggettoPagatore();
		buildSoggettoPagatoreToCallPa(datiPagamentoPSP.getSoggettoPagatore(), soggettoPagatoreToCallPa);
		paaTipoDatiPagamentoToCallPa.setSoggettoPagatore(soggettoPagatoreToCallPa);

		CtSoggettoVersante soggettoVersanteToCallPa = new CtSoggettoVersante();
		buildSoggettoVersanteToCallPa(datiPagamentoPSP.getSoggettoVersante(), soggettoVersanteToCallPa);
		paaTipoDatiPagamentoToCallPa.setSoggettoVersante(soggettoVersanteToCallPa);
	}

	/**
	 * @param soggettoVersante
	 * @param ctSoggettoVersanteToCallPa
	 */
	private void buildSoggettoVersanteToCallPa(it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoVersante soggettoVersante,
			CtSoggettoVersante ctSoggettoVersanteToCallPa) {

		it.veneto.regione.schemas._2012.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante = new it.veneto.regione.schemas._2012.pagamenti.CtIdentificativoUnivocoPersonaFG();
		if (soggettoVersante != null && soggettoVersante.getIdentificativoUnivocoVersante() != null
				&& soggettoVersante.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() != null) {

			it.veneto.regione.schemas._2012.pagamenti.StTipoIdentificativoUnivocoPersFG tipoIdentificativoUnivocoPersona = it.veneto.regione.schemas._2012.pagamenti.StTipoIdentificativoUnivocoPersFG
					.valueOf(soggettoVersante.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString());

			identificativoUnivocoVersante.setTipoIdentificativoUnivoco(tipoIdentificativoUnivocoPersona);
			identificativoUnivocoVersante.setCodiceIdentificativoUnivoco(soggettoVersante.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());
			
			/**
			 *  RIMOSSO IN DATA 01-12-2017
			 *  L'anagrafica pagatore viene valorizzata con quella dell'avviso o del dovuto, l'anagrafica versante è stata ignorata perchè possono arrivare dati scorretti
			 */
			//ctSoggettoVersanteToCallPa.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);

			ctSoggettoVersanteToCallPa.setAnagraficaVersante(soggettoVersante.getAnagraficaVersante());

			if (StringUtils.isNotBlank(soggettoVersante.getIndirizzoVersante())) {
				ctSoggettoVersanteToCallPa.setIndirizzoVersante(soggettoVersante.getIndirizzoVersante());
			}
			if (StringUtils.isNotBlank(soggettoVersante.getCivicoVersante())) {
				ctSoggettoVersanteToCallPa.setCivicoVersante(soggettoVersante.getCivicoVersante());
			}
			if (StringUtils.isNotBlank(soggettoVersante.getCapVersante())) {
				ctSoggettoVersanteToCallPa.setCapVersante(soggettoVersante.getCapVersante());
			}
			if (StringUtils.isNotBlank(soggettoVersante.getLocalitaVersante())) {
				ctSoggettoVersanteToCallPa.setLocalitaVersante(soggettoVersante.getLocalitaVersante());
			}
			if (StringUtils.isNotBlank(soggettoVersante.getProvinciaVersante())) {
				ctSoggettoVersanteToCallPa.setProvinciaVersante(soggettoVersante.getProvinciaVersante());
			}
			if (StringUtils.isNotBlank(soggettoVersante.getNazioneVersante())) {
				ctSoggettoVersanteToCallPa.setNazioneVersante(soggettoVersante.getNazioneVersante());
			}
			if (StringUtils.isNotBlank(soggettoVersante.getEMailVersante())) {
				ctSoggettoVersanteToCallPa.setEMailVersante(soggettoVersante.getEMailVersante());
			}
		}
	}

	/**
	 * @param soggettoPagatore
	 * @param soggettoPagatoreToCallPa
	 */
	private void buildSoggettoPagatoreToCallPa(it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoPagatore soggettoPagatore,
			CtSoggettoPagatore soggettoPagatoreToCallPa) {

		it.veneto.regione.schemas._2012.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore = new it.veneto.regione.schemas._2012.pagamenti.CtIdentificativoUnivocoPersonaFG();

		if (soggettoPagatore != null && soggettoPagatore.getIdentificativoUnivocoPagatore() != null
				&& soggettoPagatore.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco() != null) {

			it.veneto.regione.schemas._2012.pagamenti.StTipoIdentificativoUnivocoPersFG tipoIdentificativoUnivocoPersona = it.veneto.regione.schemas._2012.pagamenti.StTipoIdentificativoUnivocoPersFG
					.valueOf(soggettoPagatore.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString());

			identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(tipoIdentificativoUnivocoPersona);
			identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(soggettoPagatore.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());

			/**
			 *  RIMOSSO IN DATA 01-12-2017
			 *  L'anagrafica pagatore viene valorizzata con quella dell'avviso o del dovuto, l'anagrafica versante è stata ignorata perchè possono arrivare dati scorretti
			 */
			//soggettoPagatoreToCallPa.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);

			soggettoPagatoreToCallPa.setAnagraficaPagatore(soggettoPagatore.getAnagraficaPagatore());

			if (StringUtils.isNotBlank(soggettoPagatore.getIndirizzoPagatore())) {
				soggettoPagatoreToCallPa.setIndirizzoPagatore(soggettoPagatore.getIndirizzoPagatore());
			}
			if (StringUtils.isNotBlank(soggettoPagatore.getCivicoPagatore())) {
				soggettoPagatoreToCallPa.setCivicoPagatore(soggettoPagatore.getCivicoPagatore());
			}
			if (StringUtils.isNotBlank(soggettoPagatore.getCapPagatore())) {
				soggettoPagatoreToCallPa.setCapPagatore(soggettoPagatore.getCapPagatore());
			}
			if (StringUtils.isNotBlank(soggettoPagatore.getLocalitaPagatore())) {
				soggettoPagatoreToCallPa.setLocalitaPagatore(soggettoPagatore.getLocalitaPagatore());
			}
			if (StringUtils.isNotBlank(soggettoPagatore.getProvinciaPagatore())) {
				soggettoPagatoreToCallPa.setProvinciaPagatore(soggettoPagatore.getProvinciaPagatore());
			}
			if (StringUtils.isNotBlank(soggettoPagatore.getNazionePagatore())) {
				soggettoPagatoreToCallPa.setNazionePagatore(soggettoPagatore.getNazionePagatore());
			}
			if (StringUtils.isNotBlank(soggettoPagatore.getEMailPagatore())) {
				soggettoPagatoreToCallPa.setEMailPagatore(soggettoPagatore.getEMailPagatore());
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematiciccp.PagamentiTelematiciCCP#paaVerificaRPT(gov.telematici.pagamenti.ws.PaaVerificaRPT, gov.telematici.pagamenti.ws.ppthead.IntestazionePPT)
	 */
	public PaaVerificaRPTRisposta paaVerificaRPT(PaaVerificaRPT bodyrichiesta, IntestazionePPT header) {

		log.info("Executing operation paaVerificaRPT");

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
		String esito;
		try {
			dataOraEvento = new Date();
			identificativoDominio = header.getIdentificativoDominio();
			identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
			codiceContestoPagamento = header.getCodiceContestoPagamento();
			identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
			tipoVersamento = Constants.PAY_PRESSO_PSP;
			componente = Constants.COMPONENTE.FESP.toString();
			categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaVerificaRPT.toString();
			sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
			identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

			identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
			identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
			canalePagamento = "";

			xmlString = "";
			try {
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory();
				context = JAXBContext.newInstance(PaaVerificaRPT.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createPaaVerificaRPT(bodyrichiesta), sw);
				xmlString = sw.toString();
			} catch (JAXBException e) {
				log.error("paaVerificaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
			}

			parametriSpecificiInterfaccia = xmlString;

			esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
					identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
		} catch (Exception e1) {
			log.warn("paaVerificaRPT REQUEST impossibile inserire nel giornale degli eventi", e1);
		}

		PaaVerificaRPTRisposta paaVerificaRPTRisposta = new PaaVerificaRPTRisposta();
		paaVerificaRPTRisposta.setPaaVerificaRPTRisposta(new EsitoVerificaRPT());

		EsitoVerificaRPT esitoVerifica = paaVerificaRPTRisposta.getPaaVerificaRPTRisposta();
		// esitoVerifica.setFault(new FaultBean());
		//esitoVerifica.setDatiPagamentoPA(new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaTipoDatiPagamentoPA());

		// FaultBean faultBean = esitoVerifica.getFault();
//		gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA = esitoVerifica.getDatiPagamentoPA();

		String codAttivarptIdentificativoIntermediarioPa = null;
		String codAttivarptIdentificativoStazioneIntermediarioPa = null;
		String codAttivarptIdentificativoDominio = null;
		String codAttivarptIdPsp = null;

		try {
			// RECUPERO I CAMPI PASSATI
			// Recuperati dall'header
			codAttivarptIdentificativoIntermediarioPa = header.getIdentificativoIntermediarioPA();
			codAttivarptIdentificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
			codAttivarptIdentificativoDominio = header.getIdentificativoDominio();

			// Recuperati dal body
			codAttivarptIdPsp = bodyrichiesta.getIdentificativoPSP();

			// PREPARAZIONE DELLA RICHIESTA VERIFICA VERSO PA
			it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT headerVerificaRPTToCallPa = new it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT();
			buildHeaderRPTToCallPa(header, headerVerificaRPTToCallPa);
			PaaSILVerificaRP bodyVerificaRPTToCallPa = new PaaSILVerificaRP();
			bodyVerificaRPTToCallPa.setIdentificativoPSP(codAttivarptIdPsp);

			/*
			 * LOG GIORNALE DEGLI EVENTI
			 */
			try {
				dataOraEvento = new Date();
				identificativoDominio = header.getIdentificativoDominio();
				identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
				codiceContestoPagamento = header.getCodiceContestoPagamento();
				identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
				tipoVersamento = Constants.PAY_PRESSO_PSP;
				componente = Constants.COMPONENTE.FESP.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaSILVerificaRP.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
				identificativoFruitore = header.getIdentificativoStazioneIntermediarioPA();
				identificativoErogatore = header.getIdentificativoDominio();
				identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
				canalePagamento = "";

				xmlString = "";
				try {
					it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.ObjectFactory objectFactory = new it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.ObjectFactory();
					context = JAXBContext.newInstance(PaaSILVerificaRP.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createPaaSILVerificaRP(bodyVerificaRPTToCallPa), sw);
					xmlString = sw.toString();
				} catch (JAXBException e) {
					log.error("paaVerificaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
				}

				parametriSpecificiInterfaccia = xmlString;

				esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento,
						identificativoFruitore, identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esito);
			} catch (Exception e1) {
				log.warn("paaSILVerificaRP REQUEST impossibile inserire nel giornale degli eventi", e1);
			}
			PaaSILVerificaRPRisposta paaSILVerificaRPRisposta;
			MygovEnte ente = enteService.getByCodiceFiscale(header.getIdentificativoDominio());
			if (StringUtils.isNotBlank(ente.getDeUrlEsterniAttiva())) { //dipende da ente
				paaSILVerificaRPRisposta = new PaaSILVerificaRPRisposta();
				it.veneto.regione.pagamenti.nodoregionalefesp.esterni.esterniperpa.ccp.PaaSILVerificaEsterna bodyPaaSILVerificaEsterna = new it.veneto.regione.pagamenti.nodoregionalefesp.esterni.esterniperpa.ccp.PaaSILVerificaEsterna();
				bodyPaaSILVerificaEsterna.setIdentificativoPSP(codAttivarptIdPsp);
				
				//pagamentiTelematiciEsterniCCPServiceClient.setPagamentiTelematiciEsterniCCPPort
				
				PaaSILVerificaEsternaRisposta paaSILVerificaEsternaRisposta = pagamentiTelematiciEsterniCCPServiceClient.paaSILVerificaEsterna(bodyPaaSILVerificaEsterna, headerVerificaRPTToCallPa, ente.getDeUrlEsterniAttiva()); 
				EsitoSILVerificaRP esitoSILVerificaRP = new EsitoSILVerificaRP();				
				if (paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta() != null && paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getEsito() != null) {
					esitoSILVerificaRP.setEsito(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getEsito());
				}				
				PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA = new PaaTipoDatiPagamentoPA();
				if (paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta() != null && paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA() != null) {
					paaTipoDatiPagamentoPA.setBicAccredito(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getBicAccredito());
					paaTipoDatiPagamentoPA.setCausaleVersamento(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getCausaleVersamento());
					paaTipoDatiPagamentoPA.setCredenzialiPagatore(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getCredenzialiPagatore());
					paaTipoDatiPagamentoPA.setEnteBeneficiario(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getEnteBeneficiario()); 
					paaTipoDatiPagamentoPA.setIbanAccredito(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getIbanAccredito());
					paaTipoDatiPagamentoPA.setImportoSingoloVersamento(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getDatiPagamentoPA().getImportoSingoloVersamento());
				}				
				esitoSILVerificaRP.setDatiPagamentoPA(paaTipoDatiPagamentoPA);
				it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.FaultBean faultBean = new it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.FaultBean();
				if (paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta() != null && paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault() != null) {
					faultBean.setDescription(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getDescription());
					faultBean.setFaultCode(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getFaultCode());
					faultBean.setFaultString(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getFaultString());
					faultBean.setId(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getId());
					faultBean.setSerial(paaSILVerificaEsternaRisposta.getPaaSILVerificaEsternaRisposta().getFault().getSerial());
				}				
				esitoSILVerificaRP.setFault(faultBean);
				
				paaSILVerificaRPRisposta.setPaaSILVerificaRPRisposta(esitoSILVerificaRP);
				
			} else {
				paaSILVerificaRPRisposta = pagamentiTelematiciCCPServiceClient.paaSILVerificaRP(bodyVerificaRPTToCallPa,
						headerVerificaRPTToCallPa);
			}

			// INVIO DELLA RICHIESTA VERIFICA AL PA


			/*
			 * LOG GIORNALE DEGLI EVENTI RISPOSTA DI PA
			 */
			try {
				dataOraEvento = new Date();
				identificativoDominio = header.getIdentificativoDominio();
				identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
				codiceContestoPagamento = header.getCodiceContestoPagamento();
				identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
				tipoVersamento = Constants.PAY_PRESSO_PSP;
				componente = Constants.COMPONENTE.FESP.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaSILVerificaRP.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
				identificativoFruitore = header.getIdentificativoStazioneIntermediarioPA();
				identificativoErogatore = header.getIdentificativoDominio();
				identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
				canalePagamento = "";

				xmlString = "";
				try {
					it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.ObjectFactory objectFactory = new it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.ObjectFactory();
					context = JAXBContext.newInstance(PaaSILVerificaRPRisposta.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createPaaSILVerificaRPRisposta(paaSILVerificaRPRisposta), sw);
					xmlString = sw.toString();
				} catch (JAXBException e) {
					log.error("paaVerificaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
				}

				parametriSpecificiInterfaccia = xmlString;

				esito = paaSILVerificaRPRisposta.getPaaSILVerificaRPRisposta().getEsito();

				giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento,
						identificativoFruitore, identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esito);
			} catch (Exception e) {
				log.warn("paaSILVerificaRP RESPONSE impossibile inserire nel giornale degli eventi", e);
			}

			if (!nodoRegionaleFespIdentificativoIntermediarioPA.equalsIgnoreCase(codAttivarptIdentificativoIntermediarioPa)
					|| !nodoRegionaleFespIdentificativoStazioneIntermediarioPA.equalsIgnoreCase(codAttivarptIdentificativoStazioneIntermediarioPa)) {

				log.warn(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [Identificativo Intermediario PA diverso da regione veneto]");
				esitoVerifica.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
				faultBean.setFaultCode(FaultCodeConstants.PAA_VERIFICA_RPT_INTERMEDIARIO_SCONOSCIUTO);
				faultBean.setFaultString("Errore PAA_VERIFICA_RPT: Intermediario sconosciuto");
				faultBean.setDescription("Errore PAA_VERIFICA_RPT: Intermediario diverso da regione veneto");
				faultBean.setId(codAttivarptIdentificativoDominio);
				faultBean.setSerial(0);
				esitoVerifica.setFault(faultBean);
				
				
				/*
				 * LOG GIORNALE DEGLI EVENTI
				 */
				try {
					dataOraEvento = new Date();
					identificativoDominio = header.getIdentificativoDominio();
					identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
					codiceContestoPagamento = header.getCodiceContestoPagamento();
					identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
					tipoVersamento = Constants.PAY_PRESSO_PSP;
					componente = Constants.COMPONENTE.FESP.toString();
					categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
					tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaVerificaRPT.toString();
					sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
					identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

					identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
					identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
					canalePagamento = "";

					xmlString = "";
					try {
						gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory();
						context = JAXBContext.newInstance(PaaVerificaRPTRisposta.class);
						Marshaller marshaller = context.createMarshaller();
						StringWriter sw = new StringWriter();
						marshaller.marshal(objectFactory.createPaaVerificaRPTRisposta(paaVerificaRPTRisposta), sw);
						xmlString = sw.toString();
					} catch (JAXBException e) {
						log.error("paaVerificaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
					}

					parametriSpecificiInterfaccia = "";

					esito = esitoVerifica.getEsito();

					giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
							identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
							identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
				} catch (Exception e) {
					log.warn("paaVerificaRPT RESPONSE impossibile inserire nel giornale degli eventi", e);
				}
				
				
				return paaVerificaRPTRisposta;
			}

			// PERSISTENZA DELL'ESITO RISPOSTO DA PA PER LA VERIFICA SU DB

			// RITORNO AL NODO SPC DELL'ESITO DELLA VERIFICA

			MygovEnte enteProp = enteService.getByCodiceFiscale(codAttivarptIdentificativoDominio);

//			RIMOSSI CONTROLLI SU ESISTENZA PSP (COME PER PAASILATTIVARPT)
//			MygovCatalogopsp mygovCatalogopsp = catalogoPspService.getAssociazioneCatalogoPsp(codAttivarptIdPsp);
//
//			if (mygovCatalogopsp == null) {
//
//				log.warn(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [Identificativo PSP non presente]");
//				esitoVerifica.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
//				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
//				faultBean.setFaultCode(FaultCodeConstants.PAA_ATTIVA_RPT_PSP_SCONOSCIUTO);
//				faultBean.setFaultString("Errore PAA_ATTIVA_RPT: psp sconosciuto");
//				faultBean.setDescription("Errore PAA_ATTIVA_RPT: psp sconosciuto per l'ente");
//				faultBean.setId(codAttivarptIdentificativoDominio);
//				faultBean.setSerial(0);
//				esitoVerifica.setFault(faultBean);
//				return paaVerificaRPTRisposta;
//			}

			EsitoSILVerificaRP esitoSILVerificaRP = paaSILVerificaRPRisposta.getPaaSILVerificaRPRisposta();

			PaaTipoDatiPagamentoPA datiPagamentoPA = esitoSILVerificaRP.getDatiPagamentoPA();
			it.veneto.regione.pagamenti.pa.papernodoregionale.ccp.FaultBean faultBeanPA = esitoSILVerificaRP.getFault();

			// Imposto ESITO PA
			esitoVerifica.setEsito(esitoSILVerificaRP.getEsito());

			// Imposto DATI PAGAMENTO PA
			if (Constants.NODO_REGIONALE_FESP_ESITO_OK.equalsIgnoreCase(esitoSILVerificaRP.getEsito())) {

				log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
						+ bodyrichiesta.getIdentificativoPSP() + "]: ente [" + enteProp + "]");
				log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
						+ bodyrichiesta.getIdentificativoPSP() + "]: BicAccredito [" + datiPagamentoPA.getBicAccredito() + "]");
				log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
						+ bodyrichiesta.getIdentificativoPSP() + "]: CausaleVersamento [" + datiPagamentoPA.getCausaleVersamento() + "]");
				log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
						+ bodyrichiesta.getIdentificativoPSP() + "]: CredenzialiPagatore [" + datiPagamentoPA.getCredenzialiPagatore() + "]");
				log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
						+ bodyrichiesta.getIdentificativoPSP() + "]: IbanAccredito [" + datiPagamentoPA.getIbanAccredito() + "]");
				log.debug("risposta per SPC per verifica per IUV [" + header.getIdentificativoUnivocoVersamento() + "] e PSP ["
						+ bodyrichiesta.getIdentificativoPSP() + "]: ImportoSingoloVersamento [" + datiPagamentoPA.getImportoSingoloVersamento() + "]");

				esitoVerifica.setDatiPagamentoPA(new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaTipoDatiPagamentoPA());
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA = esitoVerifica.getDatiPagamentoPA();
				
				paaTipoDatiPagamentoPA.setBicAccredito(datiPagamentoPA.getBicAccredito());
				paaTipoDatiPagamentoPA.setCausaleVersamento(datiPagamentoPA.getCausaleVersamento().length() > 140 ? datiPagamentoPA.getCausaleVersamento().substring(0, 140) : datiPagamentoPA.getCausaleVersamento());
				paaTipoDatiPagamentoPA.setCredenzialiPagatore(datiPagamentoPA.getCredenzialiPagatore());
				paaTipoDatiPagamentoPA.setIbanAccredito(datiPagamentoPA.getIbanAccredito());
				paaTipoDatiPagamentoPA.setImportoSingoloVersamento(datiPagamentoPA.getImportoSingoloVersamento());
				paaTipoDatiPagamentoPA.setEnteBeneficiario(buildEnteBeneficiario(enteProp));
			} else if (faultBeanPA != null) {
				//Imposto FAULT PA
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
				faultBean.setFaultCode(faultBeanPA.getFaultCode());
				faultBean.setFaultString(faultBeanPA.getFaultString());
				faultBean.setDescription(faultBeanPA.getDescription());
				
				if(StringUtils.isNotBlank(faultBeanPA.getOriginalFaultCode()))
					faultBean.setOriginalFaultCode(faultBeanPA.getOriginalFaultCode());
				if(StringUtils.isNotBlank(faultBeanPA.getOriginalFaultString()))
					faultBean.setOriginalFaultString(faultBeanPA.getOriginalFaultString());
				if(StringUtils.isNotBlank(faultBeanPA.getOriginalDescription()))
					faultBean.setOriginalDescription(faultBeanPA.getOriginalDescription());
				
				faultBean.setId(faultBeanPA.getId());
				faultBean.setSerial(faultBeanPA.getSerial());
				esitoVerifica.setFault(faultBean);
			} else {
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
				faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
				faultBean.setFaultString("Errore generico PAA_ATTIVA_RPT: esito KO ma faultBean null");
				faultBean.setDescription("Errore generico PAA_ATTIVA_RPT: esito KO ma faultBean null");
				faultBean.setId(codAttivarptIdentificativoDominio);
				faultBean.setSerial(0);
				esitoVerifica.setFault(faultBean);
			}
		} catch (Exception ex) {
			log.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [" + ex.getMessage() + "]", ex);
			gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.FaultBean();
			faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
			faultBean.setFaultString("Errore generico PAA_VERIFICA_RPT: " + ex.getCause());
			faultBean.setDescription("Errore generico PAA_VERIFICA_RPT: " + ex.getMessage());
			faultBean.setId(codAttivarptIdentificativoDominio);
			faultBean.setSerial(0);
			esitoVerifica.setFault(faultBean);
			esitoVerifica.setEsito(Constants.NODO_REGIONALE_FESP_ESITO_KO);
		}

		/*
		 * LOG GIORNALE DEGLI EVENTI
		 */
		try {
			dataOraEvento = new Date();
			identificativoDominio = header.getIdentificativoDominio();
			identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
			codiceContestoPagamento = header.getCodiceContestoPagamento();
			identificativoPrestatoreServiziPagamento = bodyrichiesta.getIdentificativoPSP();
			tipoVersamento = Constants.PAY_PRESSO_PSP;
			componente = Constants.COMPONENTE.FESP.toString();
			categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaVerificaRPT.toString();
			sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
			identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

			identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
			identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
			canalePagamento = "";

			xmlString = "";
			try {
				gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.ccp.ObjectFactory();
				context = JAXBContext.newInstance(PaaVerificaRPTRisposta.class);
				Marshaller marshaller = context.createMarshaller();
				StringWriter sw = new StringWriter();
				marshaller.marshal(objectFactory.createPaaVerificaRPTRisposta(paaVerificaRPTRisposta), sw);
				xmlString = sw.toString();
			} catch (JAXBException e) {
				log.error("paaVerificaRPT REQUEST impossibile codificare parametriSpecificiInterfaccia", e);
			}

			parametriSpecificiInterfaccia = "";

			esito = esitoVerifica.getEsito();

			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
					identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
					identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);
		} catch (Exception e) {
			log.warn("paaVerificaRPT RESPONSE impossibile inserire nel giornale degli eventi", e);
		}

		return paaVerificaRPTRisposta;
	}

}
