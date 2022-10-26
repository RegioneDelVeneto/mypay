package it.regioneveneto.mygov.payment.nodoregionalefesp.server;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtEsitoChiediNumeroAvviso;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtNumeroAvviso;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.PaaChiediNumeroAvviso;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.PaaChiediNumeroAvvisoRisposta;
import gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.ppthead.IntestazionePPT;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersG;
import it.gov.spcoop.nodopagamentispc.servizi.richiestaavvisi.GenerazioneAvvisi;
import it.regioneveneto.mygov.payment.constants.Constants;
import it.regioneveneto.mygov.payment.constants.FaultCodeConstants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRichiestaAvvisi;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.EnteService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.GiornaleService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.RichiestaAvvisiService;
import it.regioneveneto.mygov.payment.pa.client.GenerazioneAvvisiServiceClient;
import it.regioneveneto.mygov.payment.utils.PropertiesUtil;
// oggetti per chiamata a paaSILChiediNumeroAvviso
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtDatiPagamentoPA;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtFaultBean;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtSILEsitoChiediNumeroAvviso;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.PaaSILChiediNumeroAvviso;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.PaaSILChiediNumeroAvvisoRisposta;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazioneAvviso;
import it.veneto.regione.schemas._2012.pagamenti.CtEnteBeneficiario;


@javax.jws.WebService(
        serviceName = "GenerazioneAvvisiservice",
        portName = "PPTPort",
        targetNamespace = "http://NodoPagamentiSPC.spcoop.gov.it/servizi/RichiestaAvvisi",
        wsdlLocation = "classpath:it/regioneveneto/mygov/payment/nodoregionalefesp/server/nodo-regionale-per-nodo-spc-richiesta-avvisi.wsdl",
        endpointInterface = "it.gov.spcoop.nodopagamentispc.servizi.richiestaavvisi.GenerazioneAvvisi")

public class GenerazioneAvvisiImpl implements GenerazioneAvvisi {

	public GenerazioneAvvisiImpl() {
		super();
	}
	
	@Autowired
	private GiornaleService giornaleService;
	
	@Autowired
	private RichiestaAvvisiService richiestaAvvisiService;
	
	@Autowired
	private EnteService enteService;
	
	@Autowired
	private PropertiesUtil propertiesUtil;
	
	@Autowired
	private GenerazioneAvvisiServiceClient generazioneAvvisiClient;

	private static final Log LOG = LogFactory.getLog(GenerazioneAvvisiImpl.class.getName());

    /* (non-Javadoc)
     * @see it.gov.spcoop.nodopagamentispc.servizi.richiestaavvisi.GenerazioneAvvisi#paaChiediNumeroAvviso(gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.PaaChiediNumeroAvviso  bodyrichiesta ,)gov.telematici.pagamenti.ws.ppthead_v2_1.IntestazionePPT  header )*
     */
    public PaaChiediNumeroAvvisoRisposta paaChiediNumeroAvviso(PaaChiediNumeroAvviso bodyrichiesta, IntestazionePPT header) { 
    	LOG.info("Executing operation paaChiediNumeroAvviso");

    	PaaSILChiediNumeroAvvisoRisposta _rspPaaSILChiediNumeroAvviso = new PaaSILChiediNumeroAvvisoRisposta();
    	PaaChiediNumeroAvvisoRisposta _rspPaaChiediNumeroAvviso  = new PaaChiediNumeroAvvisoRisposta();

    	IntestazioneAvviso _paaSILChiediNumeroAvviso_header = null;
    	MygovRichiestaAvvisi richiestaAvvisi = null;
    	
    	CtSILEsitoChiediNumeroAvviso ctSILEsitoChiediNumeroAvviso = null;
    	
    	try {

    		// LOG GIORNALE DEGLI EVENTI DATI RICEVUTI
    		try {

    			Date dataOraEvento = new Date();
    			String identificativoDominio = header.getIdentificativoDominio();
    			String identificativoUnivocoVersamento = "";
    			String codiceContestoPagamento = "";
    			String identificativoPrestatoreServiziPagamento = "";
    			String tipoVersamento = "";
    			String componente = Constants.COMPONENTE.FESP.toString();
    			String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
    			String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaChiediNumeroAvviso.toString();
    			String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
    			String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

    			String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
    			String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
    			String canalePagamento = "";

    			String xmlString = "";
    			try {
    				gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.ObjectFactory();
    				JAXBContext context = JAXBContext.newInstance(PaaChiediNumeroAvviso.class);
    				Marshaller marshaller = context.createMarshaller();
    				StringWriter sw = new StringWriter();
    				marshaller.marshal(objectFactory.createPaaChiediNumeroAvviso(bodyrichiesta), sw);
    				xmlString = sw.toString();
    			} catch (JAXBException jaxbex) {
    				LOG.error("Errore deserializzazione PaaChiediNumeroAvviso");
    			}

    			String parametriSpecificiInterfaccia = xmlString;

    			String esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

    			giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
    					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
    					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
    					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
    					esitoReq);

    		} catch(Exception ex) {
    			LOG.warn("paaChiediNumeroAvviso REQUEST impossibile inserire nel giornale degli eventi", ex);
    		}
    		
    		// CONTROLLO PARAMETRI HEADER
    		String faultCodeValidazioneHeader = isValidHeader(header);
    		if (StringUtils.isNotBlank(faultCodeValidazioneHeader)){
    			String idDominio = StringUtils.isNotBlank(header.getIdentificativoDominio()) 
    				? header.getIdentificativoDominio() : "-";
   				String idIntermediarioPA =  StringUtils.isNotBlank(header.getIdentificativoIntermediarioPA()) 
   					? header.getIdentificativoIntermediarioPA() : "-";
    			String idStazIntermediarioPA = StringUtils.isNotBlank(header.getIdentificativoStazioneIntermediarioPA())
    				? header.getIdentificativoStazioneIntermediarioPA() : "-";
    			String messaggioErrore;
    			switch (faultCodeValidazioneHeader) {
				case FaultCodeConstants.PAA_ID_DOMINIO_ERRATO:
					messaggioErrore = "idDominio " + idDominio + " errato";
					break;
				case FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO:
					messaggioErrore = "identificativoIntermediarioPA " + idIntermediarioPA + " errato";
					break;
				case FaultCodeConstants.PAA_STAZIONE_INT_ERRATA:
					messaggioErrore = "identificativoStazioneIntermediarioPA " + idStazIntermediarioPA + " errato";
					break;
				default:
					messaggioErrore = "errore interno";
					break;
				}
    			
    			_rspPaaChiediNumeroAvviso = buildRispostaNodoNegativa(idDominio, faultCodeValidazioneHeader, messaggioErrore, null);
    			registraRispostaNodoNegativa(idDominio, idStazIntermediarioPA, "", faultCodeValidazioneHeader, messaggioErrore);
    			
    			return _rspPaaChiediNumeroAvviso;
    		}
    		
    		// CONTROLLO CHE identificativoPSP sia presente in ALMENO un record del catalogo informativo
    		if (!isValidIdentificativoPSP(bodyrichiesta.getIdentificativoPSP())) {
    			String idPSP = StringUtils.isNotBlank(bodyrichiesta.getIdentificativoPSP()) ? bodyrichiesta.getIdentificativoPSP() : "";
    			String messaggioErrore = "nessun PSP trovato nel catalogo informativo con identificativoPSP " + idPSP; 
    			_rspPaaChiediNumeroAvviso = buildRispostaNodoNegativa(header.getIdentificativoDominio(), FaultCodeConstants.PAA_SYSTEM_ERROR, messaggioErrore, null);
    			registraRispostaNodoNegativa(header.getIdentificativoDominio(), header.getIdentificativoStazioneIntermediarioPA(), idPSP, FaultCodeConstants.PAA_SYSTEM_ERROR, messaggioErrore);
    			return _rspPaaChiediNumeroAvviso;
    		}
    		
    		
    		// PERSISTO RICHIESTA A PA IN RICHIESTA AVVISI
    		// il parametro null è idServizio, ad oggi 19/11/18 non previso nel wsdl
    		// di paaChiediNumeroAvviso ma indicato nelle specifiche 
    		String datiSpecificiServizio = new String(bodyrichiesta.getDatiSpecificiServizio(), "UTF-8");
    		richiestaAvvisi = richiestaAvvisiService.creaRichiestaAvvisi(header.getIdentificativoIntermediarioPA(),
    				header.getIdentificativoStazioneIntermediarioPA(),
    				header.getIdentificativoDominio(),
    				bodyrichiesta.getIdentificativoPSP(),
    				null, datiSpecificiServizio);
    		
    		
    		// COSTRUISCO RICHIESTA PER PA
    		_paaSILChiediNumeroAvviso_header = buildHeader(header);
    		PaaSILChiediNumeroAvviso _paaSILChiediNumeroAvviso_bodyrichiesta = buildBody(bodyrichiesta);

    		// LOGGO RICHIESTA PA SUL GIORNALE
    		try {
    			Date dataOraEvento = new Date();
    			String identificativoDominio = _paaSILChiediNumeroAvviso_header.getIdentificativoDominio();
    			String identificativoUnivocoVersamento = "";
    			String codiceContestoPagamento = "";
    			String identificativoPrestatoreServiziPagamento = "";
    			String tipoVersamento = "";
    			String componente = Constants.COMPONENTE.FESP.toString();
    			String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
    			String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaSILChiediNumeroAvviso.toString();
    			String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();
    			String identificativoFruitore = _paaSILChiediNumeroAvviso_header.getIdentificativoStazioneIntermediarioPA();

    			String identificativoErogatore = _paaSILChiediNumeroAvviso_header.getIdentificativoStazioneIntermediarioPA();
    			String identificativoStazioneIntermediarioPa = _paaSILChiediNumeroAvviso_header.getIdentificativoStazioneIntermediarioPA();
    			String canalePagamento = "";

    			String xmlString = "";
    			try {
    				it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.ObjectFactory objectFactory = new it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.ObjectFactory();
    				JAXBContext context = JAXBContext.newInstance(PaaSILChiediNumeroAvviso.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createPaaSILChiediNumeroAvviso(_paaSILChiediNumeroAvviso_bodyrichiesta), sw);
					xmlString = sw.toString();
    			} catch (JAXBException jaxbex) {
    				LOG.error("Errore deserializzazione REQUEST PaaSILChiediNumeroAvviso");
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
    			LOG.warn("paaSILChiediNumeroAvviso REQUEST impossibile inserire nel giornale degli eventi", ex);
    		}
    		
    		// CHIAMATA A PA
    		try {
    			_rspPaaSILChiediNumeroAvviso = this.generazioneAvvisiClient
    				.paaSILChiediNumeroAvviso(_paaSILChiediNumeroAvviso_bodyrichiesta, _paaSILChiediNumeroAvviso_header);
    		} catch (Exception e) {    			
    			_rspPaaChiediNumeroAvviso = buildRispostaNodoNegativa(header.getIdentificativoDominio(), FaultCodeConstants.PAA_SYSTEM_ERROR, e.getMessage(), null);
    			registraRispostaNodoNegativa(header.getIdentificativoDominio(), header.getIdentificativoStazioneIntermediarioPA(),
    					bodyrichiesta.getIdentificativoPSP(), FaultCodeConstants.PAA_SYSTEM_ERROR, "Eccezione sollevata durante chiamata a PA: " + e.getMessage());
    			return _rspPaaChiediNumeroAvviso;
    		}
    		
    	} catch (Exception e) {
    		LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR, e);
			_rspPaaChiediNumeroAvviso = buildRispostaNodoNegativa(header.getIdentificativoDominio(), FaultCodeConstants.PAA_SYSTEM_ERROR, e.getMessage(), null);
			registraRispostaNodoNegativa(header.getIdentificativoDominio(),	header.getIdentificativoStazioneIntermediarioPA(),
					bodyrichiesta.getIdentificativoPSP(), FaultCodeConstants.PAA_SYSTEM_ERROR, "Eccezione sollevata prima di chiamata a PA: " + e.getMessage());
			return _rspPaaChiediNumeroAvviso;
    	}
    		
    	try {
    		// LOGGO RISPOSTA PA SUL GIORNALE 
    		try {

    			Date dataOraEvento = new Date();
    			String identificativoDominio = _paaSILChiediNumeroAvviso_header.getIdentificativoDominio();
    			String identificativoUnivocoVersamento = "";
    			String codiceContestoPagamento = "";
    			String identificativoPrestatoreServiziPagamento = "";
    			String tipoVersamento = "";
    			String componente = Constants.COMPONENTE.FESP.toString();
    			String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
    			String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaSILChiediNumeroAvviso.toString();
    			String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
    			String identificativoFruitore = _paaSILChiediNumeroAvviso_header.getIdentificativoStazioneIntermediarioPA();

    			String identificativoErogatore = _paaSILChiediNumeroAvviso_header.getIdentificativoStazioneIntermediarioPA();
    			String identificativoStazioneIntermediarioPa = _paaSILChiediNumeroAvviso_header.getIdentificativoStazioneIntermediarioPA();
    			String canalePagamento = "";

    			String xmlString = "";
    			try {
    				it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.ObjectFactory objectFactory = new it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.ObjectFactory();
    				JAXBContext context = JAXBContext.newInstance(PaaSILChiediNumeroAvvisoRisposta.class);
					Marshaller marshaller = context.createMarshaller();
					StringWriter sw = new StringWriter();
					marshaller.marshal(objectFactory.createPaaSILChiediNumeroAvvisoRisposta(_rspPaaSILChiediNumeroAvviso), sw);
					xmlString = sw.toString();
    			} catch (JAXBException jaxbex) {
    				LOG.error("Errore deserializzazione RESPONSE PaaSILChiediNumeroAvviso");
    			}
    			
    			String parametriSpecificiInterfaccia = xmlString;
				String esitoReq = _rspPaaSILChiediNumeroAvviso.getPaaSILChiediNumeroAvvisoRisposta().getEsito();
				
				if (!_rspPaaSILChiediNumeroAvviso.getPaaSILChiediNumeroAvvisoRisposta().getEsito().equals("OK"))
					LOG.debug("Ricevuto errore in risposta a PaaSILChiediNumeroAvviso:"
							+ _rspPaaSILChiediNumeroAvviso.getPaaSILChiediNumeroAvvisoRisposta().getFault().getDescription());

				giornaleService.registraEvento(dataOraEvento, identificativoDominio,
						identificativoUnivocoVersamento, codiceContestoPagamento,
						identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
						tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
				
    		} catch (Exception ex) {
    			LOG.warn("paaSILChiediNumeroAvviso REQUEST impossibile inserire nel giornale degli eventi", ex);
    		}
    		
    		ctSILEsitoChiediNumeroAvviso = _rspPaaSILChiediNumeroAvviso
    				.getPaaSILChiediNumeroAvvisoRisposta();

    		// PERSISTO RISPOSTA RITORNATA DA PA IN RICHIESTA AVVISI
    		if (ctSILEsitoChiediNumeroAvviso != null)
    			aggiornaRichiestaAvvisiConRispostaPA(richiestaAvvisi, ctSILEsitoChiediNumeroAvviso);
    		
    		//SE ESITO NON E' OK, TORNO RISPOSTA NEGATIVA
    		if (!ctSILEsitoChiediNumeroAvviso.getEsito().equals("OK")) {
    			_rspPaaChiediNumeroAvviso = buildRispostaNodoNegativa(null, null, null, ctSILEsitoChiediNumeroAvviso);
    		} else {
    			// COSTRUISCO RISPOSTA POSITIVA PER IL NODO
    			_rspPaaChiediNumeroAvviso = buildRispostaNodoPositiva(_rspPaaSILChiediNumeroAvviso);
    		}

    	} catch (Exception ex) {
    		LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
    		_rspPaaChiediNumeroAvviso = buildRispostaNodoNegativa(header.getIdentificativoDominio(), FaultCodeConstants.PAA_SYSTEM_ERROR, ex.getMessage(), ctSILEsitoChiediNumeroAvviso);
			registraRispostaNodoNegativa(header.getIdentificativoDominio(),	header.getIdentificativoStazioneIntermediarioPA(),
					bodyrichiesta.getIdentificativoPSP(), FaultCodeConstants.PAA_SYSTEM_ERROR, "Eccezione sollevata dopo chiamata a PA: " + ex.getMessage());
			return _rspPaaChiediNumeroAvviso;    		
    	}

    	// LOGGO RISPOSTA A NODO
    	try {
    		Date dataOraEvento = new Date();
    		String identificativoDominio = header.getIdentificativoDominio();
    		String identificativoUnivocoVersamento = "";
    		String codiceContestoPagamento = "";
    		String identificativoPrestatoreServiziPagamento = "";
    		String tipoVersamento = "";
    		String componente = Constants.COMPONENTE.FESP.toString();
    		String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
    		String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaChiediNumeroAvviso.toString();
    		String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
    		String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

    		String identificativoErogatore = header.getIdentificativoStazioneIntermediarioPA();
    		String identificativoStazioneIntermediarioPa = header.getIdentificativoStazioneIntermediarioPA();
    		String canalePagamento = "";

    		String xmlString = "";
    		try {
    			gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.ObjectFactory objectFactory = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.ObjectFactory();
    			JAXBContext context = JAXBContext.newInstance(PaaChiediNumeroAvviso.class);
    			Marshaller marshaller = context.createMarshaller();
    			StringWriter sw = new StringWriter();
    			marshaller.marshal(objectFactory.createPaaChiediNumeroAvvisoRisposta(_rspPaaChiediNumeroAvviso), sw);
    			xmlString = sw.toString();
    		} catch (JAXBException jaxbex) {
    			LOG.error("Errore deserializzazione PaaChiediNumeroAvviso");
    		}

    		String parametriSpecificiInterfaccia = xmlString;

    		String esitoReq = _rspPaaChiediNumeroAvviso.getPaaChiediNumeroAvvisoRisposta().getEsito();

    		giornaleService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
    				codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
    				categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
    				identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
    				esitoReq);

    	} catch(Exception ex) {
    		LOG.warn("paaChiediNumeroAvviso RESPONSE impossibile inserire nel giornale degli eventi", ex);
    	}

    	return _rspPaaChiediNumeroAvviso;

    }

	private IntestazioneAvviso buildHeader(IntestazionePPT header) {
    	IntestazioneAvviso result = new IntestazioneAvviso();

    	result.setIdentificativoDominio(header.getIdentificativoDominio());
    	result.setIdentificativoIntermediarioPA(header.getIdentificativoIntermediarioPA());
    	result.setIdentificativoStazioneIntermediarioPA(header.getIdentificativoStazioneIntermediarioPA());
    	
		return result;
	}
    
    private PaaSILChiediNumeroAvviso buildBody(PaaChiediNumeroAvviso bodyrichiesta) {
    	PaaSILChiediNumeroAvviso result = new PaaSILChiediNumeroAvviso();
    	
    	result.setDatiSpecificiServizio(bodyrichiesta.getDatiSpecificiServizio());
    	result.setIdentificativoPSP(bodyrichiesta.getIdentificativoPSP());
    	
		return result;
	}
    
    private PaaChiediNumeroAvvisoRisposta buildRispostaNodoPositiva(PaaSILChiediNumeroAvvisoRisposta  paaSILChiediNumeroAvvisoRisposta) {
    	PaaChiediNumeroAvvisoRisposta risposta = new PaaChiediNumeroAvvisoRisposta();
    	CtEsitoChiediNumeroAvviso ctEsitoChiediNumeroAvviso = new CtEsitoChiediNumeroAvviso();
    	CtNumeroAvviso ctNumeroAvviso = new CtNumeroAvviso();

    	CtSILEsitoChiediNumeroAvviso ctSILEsitoChiediNumeroAvviso = paaSILChiediNumeroAvvisoRisposta.getPaaSILChiediNumeroAvvisoRisposta();
    	ctEsitoChiediNumeroAvviso.setEsito("OK");
    	
		ctNumeroAvviso.setApplicationCode(ctSILEsitoChiediNumeroAvviso.getNumeroAvviso().getApplicationCode());
		ctNumeroAvviso.setAuxDigit(ctSILEsitoChiediNumeroAvviso.getNumeroAvviso().getAuxDigit());
		ctNumeroAvviso.setIUV(ctSILEsitoChiediNumeroAvviso.getNumeroAvviso().getIUV());
		ctEsitoChiediNumeroAvviso.setNumeroAvviso(ctNumeroAvviso);
				
		gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtDatiPagamentoPA ctDatiPagamentoPA = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtDatiPagamentoPA();
		ctEsitoChiediNumeroAvviso.setDatiPagamentoPA(ctDatiPagamentoPA);
		
		risposta.setPaaChiediNumeroAvvisoRisposta(ctEsitoChiediNumeroAvviso);
		
		return risposta;
    }
    
    private PaaChiediNumeroAvvisoRisposta buildRispostaNodoNegativa(String faultId, String faultCode, String faultDescription, CtSILEsitoChiediNumeroAvviso ctSILEsitoChiediNumeroAvviso) {
    	PaaChiediNumeroAvvisoRisposta result = new PaaChiediNumeroAvvisoRisposta();
    	CtEsitoChiediNumeroAvviso ctEsitoChiediNumeroAvviso = new CtEsitoChiediNumeroAvviso();
    	gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtFaultBean faultBean
    		= new gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtFaultBean();
    	
    	boolean rispostaPaValorizzata = (ctSILEsitoChiediNumeroAvviso != null);
    	boolean rispostaPaValorizzataConFaultBean = rispostaPaValorizzata && (ctSILEsitoChiediNumeroAvviso.getFault() != null);
    	
    	ctEsitoChiediNumeroAvviso.setEsito("KO");
    	if (StringUtils.isNotBlank(faultId)) {
    		faultBean = getFaultBean(faultId,
    			StringUtils.isNotBlank(faultCode) ? faultCode : "",
    			"",
    			StringUtils.isNotBlank(faultDescription) ? faultDescription : "");
    		ctEsitoChiediNumeroAvviso.setFault(faultBean);
    	} else if (rispostaPaValorizzataConFaultBean) {
    		ctEsitoChiediNumeroAvviso.setFault(mapFaultBeanPerRispostaNodo(ctSILEsitoChiediNumeroAvviso.getFault()));
    	} else if (!rispostaPaValorizzataConFaultBean) {
    		faultBean = getFaultBean(FaultCodeConstants.PAA_SYSTEM_ERROR, "", "", "");
    		ctEsitoChiediNumeroAvviso.setFault(faultBean);
    	}
    	
    	if (rispostaPaValorizzata && ctSILEsitoChiediNumeroAvviso.getDatiPagamentoPA() != null)
    		ctEsitoChiediNumeroAvviso.setDatiPagamentoPA(mapDatiPagamentoPAPerRispostaNodo(ctSILEsitoChiediNumeroAvviso.getDatiPagamentoPA()));
    	if (rispostaPaValorizzata && ctSILEsitoChiediNumeroAvviso.getNumeroAvviso() != null)
    		ctEsitoChiediNumeroAvviso.setNumeroAvviso(mapNumeroAvvisoPerRispostaNodo(ctSILEsitoChiediNumeroAvviso.getNumeroAvviso()));
    	
    	result.setPaaChiediNumeroAvvisoRisposta(ctEsitoChiediNumeroAvviso);
    	
    	return result;
    }

	private void aggiornaRichiestaAvvisiConRispostaPA(MygovRichiestaAvvisi richiestaAvvisi, CtSILEsitoChiediNumeroAvviso ctSILEsitoChiediNumeroAvviso) {
		it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtNumeroAvviso numeroAvviso = ctSILEsitoChiediNumeroAvviso.getNumeroAvviso();
		CtDatiPagamentoPA datiPagamentoPA = ctSILEsitoChiediNumeroAvviso.getDatiPagamentoPA();
		CtFaultBean faultBean = ctSILEsitoChiediNumeroAvviso.getFault();
		
		richiestaAvvisiService.updateRispostaPA(richiestaAvvisi.getMygovRichiestaAvvisiId(),
				richiestaAvvisi.getCodEsito(), datiPagamentoPA, numeroAvviso, faultBean);
	}


	private gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtDatiPagamentoPA mapDatiPagamentoPAPerRispostaNodo(
			CtDatiPagamentoPA datiPagamentoPA) {
		gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtDatiPagamentoPA datiPagamentoPAPerNodo = 
			new gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtDatiPagamentoPA();
		
		datiPagamentoPAPerNodo.setImportoSingoloVersamento(datiPagamentoPA.getImportoSingoloVersamento());
		datiPagamentoPAPerNodo.setIbanAccredito(datiPagamentoPA.getIbanAccredito());
		if (StringUtils.isNotBlank(datiPagamentoPA.getBicAccredito()))
			datiPagamentoPAPerNodo.setBicAccredito(datiPagamentoPA.getBicAccredito());
		
		if (datiPagamentoPA.getEnteBeneficiario() != null) {
			CtEnteBeneficiario enteBeneficiario = datiPagamentoPA.getEnteBeneficiario();
			it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario enteBeneficiarioPerNodo = 
				new it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario();
			
			String tipoIdentificativo = enteBeneficiario.getIdentificativoUnivocoBeneficiario()
				.getTipoIdentificativoUnivoco().value();
			String codiceIdentificativo = enteBeneficiario.getIdentificativoUnivocoBeneficiario()
					.getCodiceIdentificativoUnivoco();
			enteBeneficiarioPerNodo.getIdentificativoUnivocoBeneficiario()
				.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.fromValue(tipoIdentificativo));
			enteBeneficiarioPerNodo.getIdentificativoUnivocoBeneficiario()
				.setCodiceIdentificativoUnivoco(codiceIdentificativo);

			enteBeneficiarioPerNodo.setDenominazioneBeneficiario(enteBeneficiario.getDenominazioneBeneficiario());
			
			enteBeneficiarioPerNodo.setCodiceUnitOperBeneficiario(enteBeneficiario.getCodiceUnitOperBeneficiario());
			enteBeneficiarioPerNodo.setDenomUnitOperBeneficiario(enteBeneficiario.getDenomUnitOperBeneficiario());
			enteBeneficiarioPerNodo.setIndirizzoBeneficiario(enteBeneficiario.getIndirizzoBeneficiario());
			enteBeneficiarioPerNodo.setCivicoBeneficiario(enteBeneficiario.getCivicoBeneficiario());
			enteBeneficiarioPerNodo.setCapBeneficiario(enteBeneficiario.getCapBeneficiario());
			enteBeneficiarioPerNodo.setLocalitaBeneficiario(enteBeneficiario.getLocalitaBeneficiario());
			enteBeneficiarioPerNodo.setProvinciaBeneficiario(enteBeneficiario.getProvinciaBeneficiario());
			enteBeneficiarioPerNodo.setNazioneBeneficiario(enteBeneficiario.getNazioneBeneficiario());
			
			datiPagamentoPAPerNodo.setEnteBeneficiario(enteBeneficiarioPerNodo);
			
		}
		
		datiPagamentoPAPerNodo.setCausaleVersamento(datiPagamentoPA.getCausaleVersamento());
		datiPagamentoPAPerNodo.setCredenzialiPagatore(datiPagamentoPA.getCredenzialiPagatore());
		
		return datiPagamentoPAPerNodo;
	}
	
	private CtNumeroAvviso mapNumeroAvvisoPerRispostaNodo(
			it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtNumeroAvviso numeroAvviso) {
		CtNumeroAvviso numeroAvvisoPerNodo = new CtNumeroAvviso();

		numeroAvvisoPerNodo.setAuxDigit(numeroAvviso.getAuxDigit());
		numeroAvvisoPerNodo.setApplicationCode(numeroAvviso.getApplicationCode());
		numeroAvvisoPerNodo.setIUV(numeroAvviso.getIUV());
		
		return numeroAvvisoPerNodo;
	}
	
	private gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtFaultBean mapFaultBeanPerRispostaNodo(
			CtFaultBean faultBean) {
		gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtFaultBean faultBeanPerNodo = new gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtFaultBean();
		
		if (faultBean.getId() != null)
		faultBeanPerNodo.setId(faultBean.getId());
		if (faultBean.getFaultCode() != null)
			faultBeanPerNodo.setFaultCode(faultBean.getFaultCode());
		if (faultBean.getFaultString() != null)
			faultBeanPerNodo.setFaultString(faultBean.getFaultString());
		if (faultBean.getDescription() != null)
			faultBeanPerNodo.setDescription(faultBean.getDescription());
		if (StringUtils.isNotBlank(faultBean.getOriginalFaultCode()))
			faultBeanPerNodo.setOriginalFaultCode(faultBean.getOriginalFaultCode());
		if (StringUtils.isNotBlank(faultBean.getOriginalFaultString()))
			faultBeanPerNodo.setOriginalFaultString(faultBean.getOriginalFaultString());
		if (StringUtils.isNotBlank(faultBean.getOriginalDescription()))
			faultBeanPerNodo.setOriginalDescription(faultBean.getOriginalDescription());
		if (faultBean.getSerial() != null)
			faultBeanPerNodo.setSerial(faultBean.getSerial());
		
		return faultBeanPerNodo;
	}
	
	private gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtFaultBean getFaultBean(String faultID, String faultCode,
			String faultString, String description) {
		LOG.error(faultCode + ": [" + description + "]");

		gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtFaultBean faultBean
			= new gov.telematici.pagamenti.ws.nodoregionalepernodospc.richiestaavvisi.CtFaultBean();
		
		faultBean.setId(faultID);
		faultBean.setFaultCode(faultCode);
		faultBean.setFaultString(faultString);
		faultBean.setDescription(description);

		return faultBean;
	}
	
	private String isValidHeader(IntestazionePPT header) {
		String idIntermediarioPAconf =
				propertiesUtil.getProperty("nodoRegionaleFesp.identificativoIntermediarioPA");
		String idStazIntermediarioPAconf =
				propertiesUtil.getProperty("nodoRegionaleFesp.identificativoStazioneIntermediarioPA");
		
		String idIntermediarioPAheader = header.getIdentificativoIntermediarioPA();
		String idStazIntermediarioPAheader = header.getIdentificativoStazioneIntermediarioPA();
		String idDominioheader = header.getIdentificativoDominio();
		
		if (StringUtils.isBlank(idIntermediarioPAheader)
				|| !idIntermediarioPAheader.equals(idIntermediarioPAconf))
			return FaultCodeConstants.PAA_ID_INTERMEDIARIO_ERRATO;
		if (StringUtils.isBlank(idStazIntermediarioPAheader)
				|| !idStazIntermediarioPAheader.equals(idStazIntermediarioPAconf))
			return FaultCodeConstants.PAA_STAZIONE_INT_ERRATA;
		try {
			enteService.getByCodiceFiscale(idDominioheader);
		} catch (IncorrectResultSizeDataAccessException e) {
			LOG.error("paaChiediNumeroAvviso: Nessun ente o più di un ente trovato per id dominio ["
					+ idDominioheader + "]");
			return FaultCodeConstants.PAA_ID_DOMINIO_ERRATO;
		}
		return null;
	}

    private boolean isValidIdentificativoPSP(String identificativoPSP) {
    	if (StringUtils.isBlank(identificativoPSP))
    		return false;
    	return true;
	}
    
    private void registraRispostaNodoNegativa(String idDominio, String idStazioneIntermediarioPA, String idPsp, String faultCodeValidazioneHeader, String messaggioErrore) {
		try {
			Date dataOraEvento = new Date();
			String componente = Constants.COMPONENTE.FESP.toString();
			String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			String tipoEvento = Constants.GIORNALE_TIPO_EVENTO.paaChiediNumeroAvviso.toString();
			String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();
			String identificativoFruitore = Constants.NODO_DEI_PAGAMENTI_SPC;

			String esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

			giornaleService.registraEvento(dataOraEvento, idDominio, "",
					"", idPsp, "", componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, idStazioneIntermediarioPA,
					idStazioneIntermediarioPA, "", messaggioErrore,
					esitoReq);

		} catch(Exception ex) {
			LOG.warn("paaChiediNumeroAvviso RESPONSE impossibile inserire nel giornale degli eventi", ex);
		}
	}

}
