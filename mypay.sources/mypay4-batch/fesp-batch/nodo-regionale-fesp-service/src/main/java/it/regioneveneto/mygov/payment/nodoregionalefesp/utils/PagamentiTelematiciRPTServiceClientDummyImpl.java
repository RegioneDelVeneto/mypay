package it.regioneveneto.mygov.payment.nodoregionalefesp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.EsitoChiediStatoRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaCarrelloRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaCarrelloRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRispostaRevocaRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoIdQuadratura;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoIdRendicontazione;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoListaQuadrature;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoListaRPTPendenti;
import gov.telematici.pagamenti.ws.ppthead.IntestazioneCarrelloPPT;
import it.gov.digitpa.schemas.x2011.pagamenti.CtDatiVersamentoRT;
import it.gov.digitpa.schemas.x2011.pagamenti.CtDominio;
import it.gov.digitpa.schemas.x2011.pagamenti.CtEnteBeneficiario;
import it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivoco;
import it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaFG;
import it.gov.digitpa.schemas.x2011.pagamenti.CtIdentificativoUnivocoPersonaG;
import it.gov.digitpa.schemas.x2011.pagamenti.CtIstitutoAttestante;
import it.gov.digitpa.schemas.x2011.pagamenti.CtRicevutaTelematica;
import it.gov.digitpa.schemas.x2011.pagamenti.CtSoggettoPagatore;
import it.gov.digitpa.schemas.x2011.pagamenti.RTDocument;
import it.gov.digitpa.schemas.x2011.pagamenti.StCodiceEsitoPagamento;
import it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivoco;
import it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersFG;
import it.gov.digitpa.schemas.x2011.pagamenti.StTipoIdentificativoUnivocoPersG;
import it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematicirpt.PagamentiTelematiciRPT;
import it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematicirpt.PagamentiTelematiciRPTservice;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageRPTRTService;
import it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient;
import it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClientImpl;

/**
 * @author regione del veneto
 *
 */
public class PagamentiTelematiciRPTServiceClientDummyImpl implements PagamentiTelematiciRPTServiceClient {
	/**
	 *
	 */
	public final static String YYYYMMDDTHHMMSS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	
	/**
	 * 
	 */
	private String nodoChiediSceltaWISPUrl;
	
	/**
	 * @param nodoChiediSceltaWISPUrl the nodoChiediSceltaWISPUrl to set
	 */
	public void setNodoChiediSceltaWISPUrl(String nodoChiediSceltaWISPUrl) {
		this.nodoChiediSceltaWISPUrl = nodoChiediSceltaWISPUrl;
	}

	//2013-01-01T00:00:00
	/**
	 *
	 */
	public final static SimpleDateFormat YYYYMMDDTHHMMSS = new SimpleDateFormat(YYYYMMDDTHHMMSS_FORMAT);

	private ManageRPTRTService manageRPTRTService;
	
	private static final Log log = LogFactory.getLog(PagamentiTelematiciRPTServiceClientImpl.class);

	public void setManageRPTRTService(ManageRPTRTService manageRPTRTService) {
		this.manageRPTRTService = manageRPTRTService;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoInviaCarrelloRPT(gov.telematici.pagamenti.ws.NodoInviaCarrelloRPT, gov.telematici.pagamenti.ws.ppthead.IntestazioneCarrelloPPT)
	 */
	@Override
	public NodoInviaCarrelloRPTRisposta nodoInviaCarrelloRPT(NodoInviaCarrelloRPT bodyrichiesta, IntestazioneCarrelloPPT header) {
		log.debug("Dentro dummy new");
		NodoInviaCarrelloRPTRisposta risposta = new NodoInviaCarrelloRPTRisposta();
		
		boolean rispostaOK = true;
		
		if (rispostaOK){
			risposta.setEsitoComplessivoOperazione("OK");
			risposta.setUrl("http://_PAGO_PA_URL_?idSession="+UUID.randomUUID()+"&esito=OK");

		} else {
			risposta.setEsitoComplessivoOperazione("KO");
			
			FaultBean fb = new FaultBean();
			fb.setDescription("fault description");
			fb.setFaultString("fault string");
			fb.setFaultCode("PAA_FAULT_CODE");
			fb.setDescription("fault description original");
			fb.setFaultString("fault string original");
			fb.setFaultCode("PAA_FAULT_CODE");
			fb.setId("NodoDeiPagamentiSPC");
			risposta.setFault(fb);
		}
		
		return risposta;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoChiediQuadraturaPA(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void nodoChiediQuadraturaPA(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio, String identificativoFlusso, Holder<FaultBean> fault, Holder<DataHandler> xmlQuadratura) {
		StringBuffer xml = new StringBuffer();

		for (int i = 0; i < 100000; i++) {
			xml.append("Ciao ciao ciao ciao ciao ciao.");
		}

		DataSource ds = null;

		try {
			ds = new ByteArrayDataSource(xml.toString().getBytes("UTF-8"), "application/octet-stream");
		}
		catch (IOException e) {
			fault.value = new FaultBean();
			fault.value.setFaultCode("PAA_SYSTEM_ERROR");
			fault.value.setDescription(e.getMessage());
		}

		xmlQuadratura.value = new DataHandler(ds);
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoInviaRPT(gov.telematici.pagamenti.ws.ppthead.IntestazionePPT, gov.telematici.pagamenti.ws.NodoInviaRPT)
	 */
	@Override
	public NodoInviaRPTRisposta nodoInviaRPT(gov.telematici.pagamenti.ws.ppthead.IntestazionePPT header, NodoInviaRPT bodyrichiesta) {
		NodoInviaRPTRisposta paaSILInviaRPTRisposta = new NodoInviaRPTRisposta();
		paaSILInviaRPTRisposta.setEsito("KO");
//		paaSILInviaRPTRisposta.setEsito("OK");
//		paaSILInviaRPTRisposta.setRedirect(1);
//		paaSILInviaRPTRisposta.setUrl("http://it.wikipedia.org/wiki/Inviato?idSession=" + UUID.randomUUID().toString());

		FaultBean faultBean = new FaultBean();
		faultBean.setDescription("Description");
		//faultBean.setFaultCode("FaultCode");
		faultBean.setFaultCode("PPT_IBAN_NON_CENSITO");
		faultBean.setFaultString("FaultString");
		faultBean.setOriginalFaultCode("OriginalFaultCode");
		faultBean.setOriginalFaultString("Original string");
		faultBean.setOriginalDescription("Original description");
		faultBean.setId("Id");
		faultBean.setSerial(1);

		paaSILInviaRPTRisposta.setFault(faultBean);
		return paaSILInviaRPTRisposta;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoInviaRichiestaStorno(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, byte[], javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void nodoInviaRichiestaStorno(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento, byte[] rr, Holder<FaultBean> fault,
			Holder<String> esito) {
		// TODO Auto-generated method stub
		throw new RuntimeException("method not implemented");
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoChiediElencoQuadraturePA(java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void nodoChiediElencoQuadraturePA(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio, Holder<FaultBean> fault, Holder<TipoListaQuadrature> listaQuadrature) {
		listaQuadrature.value = new TipoListaQuadrature();

		GregorianCalendar gregorianCalendar = new GregorianCalendar(2014, 3, 28, 23, 24, 25);

		XMLGregorianCalendar dataOraFlusso = getDataOraFlusso(gregorianCalendar);

		TipoListaQuadrature tipoListaQuadrature = listaQuadrature.value;

		if (identificativoDominio.equals("00133880252")) {
			TipoIdQuadratura tiq = new TipoIdQuadratura();
			tiq.setIdentificativoFlusso("flussoQuadraturaFeltre_1");
			tiq.setDataOraFlusso(dataOraFlusso);
			tipoListaQuadrature.getIdQuadratura().add(tiq);

			tiq = new TipoIdQuadratura();
			tiq.setIdentificativoFlusso("flussoQuadraturaFeltre_2");
			tiq.setDataOraFlusso(dataOraFlusso);
			tipoListaQuadrature.getIdQuadratura().add(tiq);

			tiq = new TipoIdQuadratura();
			tiq.setIdentificativoFlusso("flussoQuadraturaFeltre_3");
			tiq.setDataOraFlusso(dataOraFlusso);
			tipoListaQuadrature.getIdQuadratura().add(tiq);
		}

		if (identificativoDominio.equals("00644060287")) {
			TipoIdQuadratura tiq = new TipoIdQuadratura();
			tiq.setIdentificativoFlusso("flussoQuadraturaPadova_1");
			tiq.setDataOraFlusso(dataOraFlusso);
			tipoListaQuadrature.getIdQuadratura().add(tiq);

			tiq = new TipoIdQuadratura();
			tiq.setIdentificativoFlusso("flussoQuadraturaPadova_2");
			tiq.setDataOraFlusso(dataOraFlusso);
			tipoListaQuadrature.getIdQuadratura().add(tiq);

			tiq = new TipoIdQuadratura();
			tiq.setIdentificativoFlusso("flussoQuadraturaPadova_3");
			tiq.setDataOraFlusso(dataOraFlusso);
			tipoListaQuadrature.getIdQuadratura().add(tiq);
		}
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoChiediListaPendentiRPT(java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.xml.datatype.XMLGregorianCalendar, javax.xml.datatype.XMLGregorianCalendar, java.math.BigInteger, javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void nodoChiediListaPendentiRPT(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio, XMLGregorianCalendar rangeDa, XMLGregorianCalendar rangeA, BigInteger dimensioneLista, Holder<FaultBean> fault,
			Holder<TipoListaRPTPendenti> listaRPTPendenti) {
		// TODO Auto-generated method stub
		throw new RuntimeException("method not implemented");
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoChiediCopiaRT(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.xml.ws.Holder, javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void nodoChiediCopiaRT(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento, Holder<FaultBean> fault,
			Holder<String> tipoFirma, Holder<DataHandler> rt) {
		tipoFirma.value = "0";

		MygovRptRt mygovRptRt = manageRPTRTService.getRptByIdDominioIdUnivocoVersamentoAndCodiceContestoPagamento(identificativoDominio,
				identificativoUnivocoVersamento, codiceContestoPagamento);

		CtRicevutaTelematica ricevutaTelematica = buildRt(mygovRptRt);

		RTDocument rtDocument = RTDocument.Factory.newInstance();
		rtDocument.setRT(ricevutaTelematica);

		DataSource ds = null;

		try {
			ds = new ByteArrayDataSource(rtDocument.toString().getBytes("UTF-8"), "application/octet-stream");
		}
		catch (IOException e) {
		}

		rt.value = new DataHandler(ds);
	}

	private CtRicevutaTelematica buildRt(MygovRptRt mygovRptRt) {
		CtRicevutaTelematica ctRicevutaTelematica = CtRicevutaTelematica.Factory.newInstance();

		ctRicevutaTelematica.setVersioneOggetto("6.0");

		/**
		 * CtDominio
		 */
		CtDominio dominio = CtDominio.Factory.newInstance();
		dominio.setIdentificativoDominio("000");
		ctRicevutaTelematica.setDominio(dominio);

		ctRicevutaTelematica.setIdentificativoMessaggioRicevuta(UUID.randomUUID().toString());
		ctRicevutaTelematica.setDataOraMessaggioRicevuta(Calendar.getInstance());
		ctRicevutaTelematica.setRiferimentoMessaggioRichiesta(mygovRptRt.getCodRptIdMessaggioRichiesta());
		ctRicevutaTelematica.setRiferimentoDataRichiesta(Calendar.getInstance());

		/**
		 * CtIstitutoAttestante
		 */
		CtIstitutoAttestante istitutoAttestante = CtIstitutoAttestante.Factory.newInstance();

		CtIdentificativoUnivoco identificativoUnivocoAttestante = CtIdentificativoUnivoco.Factory.newInstance();
		identificativoUnivocoAttestante.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivoco.G);
		identificativoUnivocoAttestante.setCodiceIdentificativoUnivoco("---");
		istitutoAttestante.setIdentificativoUnivocoAttestante(identificativoUnivocoAttestante);

		ctRicevutaTelematica.setIstitutoAttestante(istitutoAttestante);

		/**
		 * CtEnteBeneficiario
		 */
		CtEnteBeneficiario enteBeneficiario = CtEnteBeneficiario.Factory.newInstance();
		CtIdentificativoUnivocoPersonaG identificativoUnivocoBeneficiario = CtIdentificativoUnivocoPersonaG.Factory.newInstance();
		identificativoUnivocoBeneficiario.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.G);
		identificativoUnivocoBeneficiario.setCodiceIdentificativoUnivoco("---");
		enteBeneficiario.setIdentificativoUnivocoBeneficiario(identificativoUnivocoBeneficiario);
		enteBeneficiario.setDenominazioneBeneficiario("---");

		ctRicevutaTelematica.setEnteBeneficiario(enteBeneficiario);

		/**
		 * CtSoggettoVersante
		 */

		/**
		 * CtSoggettoPagatore
		 */
		CtSoggettoPagatore soggettoPagatore = CtSoggettoPagatore.Factory.newInstance();
		CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore = CtIdentificativoUnivocoPersonaFG.Factory.newInstance();
		identificativoUnivocoPagatore
				.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.Enum.forString(mygovRptRt.getCodRptSoggPagIdUnivPagTipoIdUnivoco()));
		identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco());
		soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
		soggettoPagatore.setAnagraficaPagatore(mygovRptRt.getDeRptSoggPagAnagraficaPagatore());

		ctRicevutaTelematica.setSoggettoPagatore(soggettoPagatore);

		/**
		 * CtDatiVersamentoEsito
		 */
		CtDatiVersamentoRT datiPagamento = CtDatiVersamentoRT.Factory.newInstance();
		datiPagamento.setCodiceEsitoPagamento(StCodiceEsitoPagamento.X_1);
		datiPagamento.setImportoTotalePagato(BigDecimal.ZERO);
		datiPagamento.setIdentificativoUnivocoVersamento(mygovRptRt.getCodRptDatiVersIdUnivocoVersamento());
		datiPagamento.setCodiceContestoPagamento(mygovRptRt.getCodRptDatiVersCodiceContestoPagamento());

		/**
		 * CtDatiSingoloPagamentoEsito
		 */

		//		if (rt.getDatiPagamento().getDatiSingoloPagamentoArray() != null) {
		//			CtDatiSingoloPagamentoRT[] pagamentiSingoli = new CtDatiSingoloPagamentoRT[rt.getDatiPagamento()
		//					.getDatiSingoloPagamentoArray().length];
		//			for (int i = 0; i < rt.getDatiPagamento().getDatiSingoloPagamentoArray().length; i++) {
		//				CtDatiSingoloPagamentoRT tmpPag = rt.getDatiPagamento().getDatiSingoloPagamentoArray(i);
		//				CtDatiSingoloPagamentoRT singoloPagamento = CtDatiSingoloPagamentoRT.Factory.newInstance();
		//				singoloPagamento.setSingoloImportoPagato(tmpPag.getSingoloImportoPagato());
		//				singoloPagamento.setCausaleVersamento(tmpPag.getCausaleVersamento());
		//				singoloPagamento.setDataEsitoSingoloPagamento(tmpPag.getDataEsitoSingoloPagamento());
		//				singoloPagamento.setDatiSpecificiRiscossione(tmpPag.getDatiSpecificiRiscossione());
		//				if (tmpPag.getEsitoSingoloPagamento() != null) {
		//					singoloPagamento.setEsitoSingoloPagamento(tmpPag.getEsitoSingoloPagamento());
		//				}
		//				singoloPagamento.setIdentificativoUnivocoRiscossione(tmpPag.getIdentificativoUnivocoRiscossione());
		//				pagamentiSingoli[i] = singoloPagamento;
		//			}
		//			datiPagamento.setDatiSingoloPagamentoArray(pagamentiSingoli);
		//		}
		ctRicevutaTelematica.setDatiPagamento(datiPagamento);

		return ctRicevutaTelematica;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoChiediElencoFlussiRendicontazione(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void nodoChiediElencoFlussiRendicontazione(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio, String identificativoPSP, Holder<FaultBean> fault,
			Holder<TipoElencoFlussiRendicontazione> elencoFlussiRendicontazione) {
		elencoFlussiRendicontazione.value = new TipoElencoFlussiRendicontazione();

		GregorianCalendar gregorianCalendar = new GregorianCalendar(2014, 3, 28, 23, 24, 25);

		XMLGregorianCalendar dataOraFlusso = getDataOraFlusso(gregorianCalendar);

		TipoElencoFlussiRendicontazione tipoElencoFlussiRendicontazione = elencoFlussiRendicontazione.value;

		if (identificativoDominio.equals("00133880252")) {
			TipoIdRendicontazione tir = new TipoIdRendicontazione();
			tir.setIdentificativoFlusso("flussoFeltre_1");
			tir.setDataOraFlusso(dataOraFlusso);
			tipoElencoFlussiRendicontazione.getIdRendicontazione().add(tir);

			tir = new TipoIdRendicontazione();
			tir.setIdentificativoFlusso("flussoFeltre_2");
			tir.setDataOraFlusso(dataOraFlusso);
			tipoElencoFlussiRendicontazione.getIdRendicontazione().add(tir);

			tir = new TipoIdRendicontazione();
			tir.setIdentificativoFlusso("flussoFeltre_3");
			tir.setDataOraFlusso(dataOraFlusso);
			tipoElencoFlussiRendicontazione.getIdRendicontazione().add(tir);
		}

		if (identificativoDominio.equals("00644060287")) {
			TipoIdRendicontazione tir = new TipoIdRendicontazione();
			tir.setIdentificativoFlusso("flussoPadova_1");
			tir.setDataOraFlusso(dataOraFlusso);
			tipoElencoFlussiRendicontazione.getIdRendicontazione().add(tir);

			tir = new TipoIdRendicontazione();
			tir.setIdentificativoFlusso("flussoPadova_2");
			tir.setDataOraFlusso(dataOraFlusso);
			tipoElencoFlussiRendicontazione.getIdRendicontazione().add(tir);

			tir = new TipoIdRendicontazione();
			tir.setIdentificativoFlusso("flussoPadova_3");
			tir.setDataOraFlusso(dataOraFlusso);
			tipoElencoFlussiRendicontazione.getIdRendicontazione().add(tir);
		}
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoChiediInformativaPSP(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void nodoChiediInformativaPSP(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio, String identificativoPSP, Holder<FaultBean> fault, Holder<DataHandler> xmlInformativa) {
		StringBuffer xml = new StringBuffer();
		
		log.debug("Inizio lettura informativa da file");
		File informativa = new File("/opt/ASJAVA/mypay/batch/nodoChiediInformativaPSP_29-01-2019.xml");
		try {
			BufferedReader br = new BufferedReader(new FileReader(informativa));
			String line = br.readLine();
			while (line != null){
				xml.append(line);
				line = br.readLine();
			}
			br.close();
			log.debug(xml.toString());
		} catch (Exception e1) {
			log.error("Errore in lettura informativa da file");
			log.error(e1.getMessage());
		}
		log.debug("Fine lettura informativa da file");
		
//		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>                                                            ");
//		xml.append("<listaInformativePSP>                                                                                 ");
//		xml.append("	<informativaPSP>                                                                                  ");
//		xml.append("<identificativoFlusso>FlussoXXX</identificativoFlusso>");
//		xml.append("		<identificativoPSP>BPPIITRRXXX</identificativoPSP>                                                        ");
//		xml.append("		<ragioneSociale>BPPIITRRXXX</ragioneSociale>                                                ");
//		xml.append("		<informativaMaster>                                                                       ");
//		xml.append("		<dataPubblicazione>" + YYYYMMDDTHHMMSS.format(new Date()) + "</dataPubblicazione>");
//
//		if ("BPPIITRRXXX".equals(identificativoPSP)) {
//			xml.append("		<dataInizioValidita>" + YYYYMMDDTHHMMSS.format(new Date()) + "</dataInizioValidita>");
//		}
//		else {
//			xml.append("		<dataInizioValidita>" + YYYYMMDDTHHMMSS.format(new Date()) + "</dataInizioValidita>");
//		}
//
//		xml.append("			<marcaBolloDigitale>1</marcaBolloDigitale>           ");
//		xml.append("		</informativaMaster>                                                                      ");
//		xml.append("		<listaInformativaDetail>                                                                  ");
//		xml.append("			<informativaDetail>                                                               ");
//		xml.append("				<identificativoIntermediario>10</identificativoIntermediario>             ");
//		xml.append("				<identificativoCanale>1</identificativoCanale>                            ");
//		xml.append("				<tipoVersamento>BP</tipoVersamento>                                       ");
//		xml.append("				<priorita>1</priorita>                                                    ");
//		xml.append("				<disponibilitaServizio>Dalle ore 13</disponibilitaServizio>                           ");
//		xml.append("				<descrizioneServizio>Descrizione</descrizioneServizio>                               ");
//		xml.append("				<condizioniEconomicheMassime>3.00</condizioniEconomicheMassime>               ");
//		xml.append("			</informativaDetail>                                                              ");
//		xml.append("			<informativaDetail>                                                               ");
//		xml.append("				<identificativoIntermediario>20</identificativoIntermediario>             ");
//		xml.append("				<identificativoCanale>2</identificativoCanale>                            ");
//		xml.append("				<tipoVersamento>BBT</tipoVersamento>                                      ");
//		xml.append("				<priorita>1</priorita>                                                    ");
//		xml.append("				<disponibilitaServizio>Dalle ore 13</disponibilitaServizio>                           ");
//		xml.append("				<descrizioneServizio>Descrizione</descrizioneServizio>                               ");
//		xml.append("				<condizioniEconomicheMassime>4.80</condizioniEconomicheMassime>               ");
//		xml.append("			</informativaDetail>                                                              ");
//		xml.append("			<informativaDetail>                                                               ");
//		xml.append("				<identificativoIntermediario>30</identificativoIntermediario>             ");
//		xml.append("				<identificativoCanale>3</identificativoCanale>                            ");
//		xml.append("				<tipoVersamento>BBT</tipoVersamento>                                      ");
//		xml.append("				<priorita>2</priorita>                                                    ");
//		xml.append("				<disponibilitaServizio>Dalle ore 13</disponibilitaServizio>                           ");
//		xml.append("				<descrizioneServizio>Descrizione</descrizioneServizio>                               ");
//		xml.append("				<condizioniEconomicheMassime>1.50</condizioniEconomicheMassime>               ");
//		xml.append("				<modelloPagamento>0</modelloPagamento>               ");
//		xml.append("			</informativaDetail>                                                              ");
//		xml.append("		</listaInformativaDetail>                                                                 ");
//		xml.append("	</informativaPSP>                                                                                 ");
//		xml.append("	<informativaPSP>                                                                                  ");
//		xml.append("<identificativoFlusso>FlussoYYY</identificativoFlusso>");
//		xml.append("		<identificativoPSP>BCITITMM</identificativoPSP>                                                        ");
//		xml.append("		<ragioneSociale>Poste Italiane</ragioneSociale>                                           ");
//		xml.append("		<informativaMaster>                                                                       ");
//		xml.append("		<dataPubblicazione>" + YYYYMMDDTHHMMSS.format(new Date()) + "</dataPubblicazione>");
//
//		if ("BPPIITRRXXX".equals(identificativoPSP)) {
//			xml.append("		<dataInizioValidita>" + YYYYMMDDTHHMMSS.format(new Date()) + "</dataInizioValidita>");
//		}
//		else {
//			xml.append("		<dataInizioValidita>" + YYYYMMDDTHHMMSS.format(new Date()) + "</dataInizioValidita>");
//		}
//
//		xml.append("		</informativaMaster>                                                                      ");
//		xml.append("		<listaInformativaDetail>                                                                  ");
//		xml.append("			<informativaDetail>                                                               ");
//		xml.append("				<identificativoIntermediario>10</identificativoIntermediario>             ");
//		xml.append("				<identificativoCanale>1</identificativoCanale>                            ");
//		xml.append("				<tipoVersamento>BP</tipoVersamento>                                       ");
//		xml.append("				<priorita>1</priorita>                                                    ");
//		xml.append("				<disponibilitaServizio>Dalle ore 13</disponibilitaServizio>                           ");
//		xml.append("				<descrizioneServizio>Descrizione</descrizioneServizio>                               ");
//		xml.append("				<condizioniEconomicheMassime>5.50</condizioniEconomicheMassime>               ");
//		xml.append("				<modelloPagamento>0</modelloPagamento>               ");
//		xml.append("			</informativaDetail>                                                              ");
//		xml.append("			<informativaDetail>                                                               ");
//		xml.append("				<identificativoIntermediario>20</identificativoIntermediario>             ");
//		xml.append("				<identificativoCanale>2</identificativoCanale>                            ");
//		xml.append("				<tipoVersamento>BBT</tipoVersamento>                                      ");
//		xml.append("				<priorita>1</priorita>                                                    ");
//		xml.append("				<disponibilitaServizio>Dalle ore 13</disponibilitaServizio>                           ");
//		xml.append("				<descrizioneServizio>Descrizione</descrizioneServizio>                               ");
//		xml.append("				<condizioniEconomicheMassime>3.00</condizioniEconomicheMassime>               ");
//		xml.append("				<modelloPagamento>0</modelloPagamento>               ");
//		xml.append("			</informativaDetail>                                                              ");
//		xml.append("			<informativaDetail>                                                               ");
//		xml.append("				<identificativoIntermediario>30</identificativoIntermediario>             ");
//		xml.append("				<identificativoCanale>3</identificativoCanale>                            ");
//		xml.append("				<tipoVersamento>BBT</tipoVersamento>                                      ");
//		xml.append("				<priorita>2</priorita>                                                    ");
//		xml.append("				<disponibilitaServizio>Dalle ore 13</disponibilitaServizio>                           ");
//		xml.append("				<descrizioneServizio>Descrizione</descrizioneServizio>                               ");
//		xml.append("				<condizioniEconomicheMassime>12.00</condizioniEconomicheMassime>               ");
//		xml.append("				<modelloPagamento>0</modelloPagamento>               ");
//		xml.append("			</informativaDetail>                                                              ");
//		xml.append("			<informativaDetail>                                                               ");
//		xml.append("				<identificativoIntermediario>40</identificativoIntermediario>             ");
//		xml.append("				<identificativoCanale>3</identificativoCanale>                            ");
//		xml.append("				<tipoVersamento>CP</tipoVersamento>                                      ");
//		xml.append("				<priorita>4</priorita>                                                    ");
//		xml.append("				<disponibilitaServizio>Dalle ore 13</disponibilitaServizio>                           ");
//		xml.append("				<descrizioneServizio>Descrizione</descrizioneServizio>                               ");
//		xml.append("				<condizioniEconomicheMassime>12.00</condizioniEconomicheMassime>               ");
//		xml.append("				<modelloPagamento>0</modelloPagamento>               ");
//		xml.append("			</informativaDetail>                                                              ");
//		xml.append("		</listaInformativaDetail>                                                                 ");
//		xml.append("	</informativaPSP>                                                                                 ");
//		xml.append("</listaInformativePSP>                                                                                ");

		DataSource ds = null;

		try {
			ds = new ByteArrayDataSource(xml.toString().getBytes("UTF-8"), "application/octet-stream");
		}
		catch (IOException e) {
			fault.value = new FaultBean();
			fault.value.setFaultCode("PAA_SYSTEM_ERROR");
			fault.value.setDescription(e.getMessage());
		}

		xmlInformativa.value = new DataHandler(ds);
	}

	//	/* (non-Javadoc)
	//	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoChiediInformativaPSP(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.xml.ws.Holder, javax.xml.ws.Holder)
	//	 */
	//	@Override
	//	public void nodoChiediInformativaPSP(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
	//			String identificativoDominio, String identificativoPSP, Holder<FaultBean> fault, Holder<DataHandler> xmlInformativa) {
	//
	//		StringBuffer xml = new StringBuffer();
	//		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>                                ");
	//		xml.append("<listaInformativePSP>");
	//		xml.append("<informativaPSP versione=\"0.1\">                                         ");
	//		xml.append("	<identificativo>" + identificativoPSP + "</identificativo>  ");
	//		xml.append("	<ragioneSociale>" + identificativoPSP + "</ragioneSociale>                       ");
	//		xml.append("	<informativaMaster>                                                   ");
	//		xml.append("		<dataPubblicazione>" + YYYYMMDDTHHMMSS.format(new Date()) + "</dataPubblicazione>");
	//		if ("UNICREDIT_1".equals(identificativoPSP)) {
	//			xml.append("		<dataInizioValidita>" + YYYYMMDDTHHMMSS.format(new Date()) + "</dataInizioValidita>");
	//		} else {
	//			xml.append("		<dataInizioValidita>" + YYYYMMDDTHHMMSS.format(new Date()) + "</dataInizioValidita>");
	//		}
	//		xml.append("	</informativaMaster>                                                  ");
	//		xml.append("	<listaInformativaDetail>                                              ");
	//		xml.append("		<informativaDetail>                                               ");
	//		xml.append("			<identificativoIntermediario>10</identificativoIntermediario> ");
	//		xml.append("			<identificativoCanale>1</identificativoCanale>                ");
	//		xml.append("			<tipoVersamento>BP</tipoVersamento>                           ");
	//		xml.append("			<priorita>1</priorita>                                        ");
	//		xml.append("			<disponibilitaServizio>Ogni lunedi</disponibilitaServizio>               ");
	//		xml.append("			<descrizioneServizio>Descizione 1</descrizioneServizio>                   ");
	//		xml.append("			<condizioniEconomicheMassime>1.00</condizioniEconomicheMassime>   ");
	//		xml.append("		</informativaDetail>                                              ");
	//		xml.append("		<informativaDetail>                                               ");
	//		xml.append("			<identificativoIntermediario>20</identificativoIntermediario> ");
	//		xml.append("			<identificativoCanale>2</identificativoCanale>                ");
	//		xml.append("			<tipoVersamento>BBT</tipoVersamento>                          ");
	//		xml.append("			<priorita>1</priorita>                                        ");
	//		xml.append("			<disponibilitaServizio>Ogni martedi</disponibilitaServizio>               ");
	//		xml.append("			<descrizioneServizio>Descizione 2</descrizioneServizio>                   ");
	//		xml.append("			<condizioniEconomicheMassime>1.50</condizioniEconomicheMassime>   ");
	//		xml.append("		</informativaDetail>                                              ");
	//		xml.append("		<informativaDetail>                                               ");
	//		xml.append("			<identificativoIntermediario>30</identificativoIntermediario> ");
	//		xml.append("			<identificativoCanale>3</identificativoCanale>                ");
	//		xml.append("			<tipoVersamento>BBT</tipoVersamento>                          ");
	//		xml.append("			<priorita>2</priorita>                                        ");
	//		xml.append("			<disponibilitaServizio>Ogni mercoledi</disponibilitaServizio>               ");
	//		xml.append("			<descrizioneServizio>Descizione 3</descrizioneServizio>                   ");
	//		xml.append("			<condizioniEconomicheMassime>2.00</condizioniEconomicheMassime>   ");
	//		xml.append("		</informativaDetail>                                              ");
	//		xml.append("	</listaInformativaDetail>                                             ");
	//		xml.append("</informativaPSP>                                                         ");
	//		xml.append("</listaInformativePSP>                                                       ");
	//		DataSource ds = null;
	//		try {
	//			ds = new ByteArrayDataSource(xml.toString().getBytes("UTF-8"), "application/octet-stream");
	//		} catch (IOException e) {
	//			fault.value = new FaultBean();
	//			fault.value.setFaultCode("PAA_SYSTEM_ERROR");
	//			fault.value.setDescription(e.getMessage());
	//		}
	//
	//		xmlInformativa.value = new DataHandler(ds);
	//	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoChiediStatoRPT(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void nodoChiediStatoRPT(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento, Holder<FaultBean> fault,
			Holder<EsitoChiediStatoRPT> esito) {
		EsitoChiediStatoRPT esitoChiediStatoRPT = new EsitoChiediStatoRPT();

		//esitoChiediStatoRPT.setStato(StatiRPT.RPT_RIFIUTATA_PSP.toString());
		FaultBean faultBean = new FaultBean();
		faultBean.setFaultCode(FaultCodeChiediStatoRPT.PPT_RPT_SCONOSCIUTA.toString());

		fault.value = faultBean;

		//		esito.value = esitoChiediStatoRPT;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodospcfesp.client.PagamentiTelematiciRPTServiceClient#nodoChiediFlussoRendicontazione(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void nodoChiediFlussoRendicontazione(String identificativoIntermediarioPA, String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio, String identificativoPSP, String identificativoFlusso, Holder<FaultBean> fault,
			Holder<DataHandler> xmlRendicontazione) {
		StringBuffer xml = new StringBuffer();

		for (int i = 0; i < 100000; i++) {
			xml.append("Ciao ciao ciao ciao ciao ciao.");
		}

		DataSource ds = null;

		try {
			ds = new ByteArrayDataSource(xml.toString().getBytes("UTF-8"), "application/octet-stream");
		}
		catch (IOException e) {
			fault.value = new FaultBean();
			fault.value.setFaultCode("PAA_SYSTEM_ERROR");
			fault.value.setDescription(e.getMessage());
		}

		xmlRendicontazione.value = new DataHandler(ds);
	}

	/**
	 * @param gregorianCalendar
	 * @return
	 */
	private XMLGregorianCalendar getDataOraFlusso(GregorianCalendar gregorianCalendar) {
		if (gregorianCalendar == null) {
			gregorianCalendar = new GregorianCalendar();
		}

		DatatypeFactory datatypeFactory = null;

		try {
			datatypeFactory = DatatypeFactory.newInstance();
		}
		catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

		return now;
	}

	@Override
	public void nodoChiediSceltaWISP(java.lang.String identificativoIntermediarioPA, java.lang.String identificativoStazioneIntermediarioPA,
			java.lang.String password, java.lang.String identificativoDominio, java.lang.String keyPA, java.lang.String keyWISP,
			javax.xml.ws.Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean> fault,
			javax.xml.ws.Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.StEffettuazioneScelta> effettuazioneScelta,
			javax.xml.ws.Holder<java.lang.String> identificativoPSP, javax.xml.ws.Holder<java.lang.String> identificativoIntermediarioPSP,
			javax.xml.ws.Holder<java.lang.String> identificativoCanale,
			javax.xml.ws.Holder<gov.telematici.pagamenti.ws.nodospcpernodoregionale.StTipoVersamento> tipoVersamento) {

		FaultBean faultBean = new FaultBean();
		faultBean.setId("NodoDeiPagamentiSPC");
		faultBean.setFaultCode("PPT_WISP_TIMEOUT_RECUPERO_SCELTA");
		faultBean.setFaultString("Timeout recupero scelta");
		faultBean.setDescription("Timeout recupero scelta");
		faultBean.setOriginalFaultCode("PPT_WISP_TIMEOUT_RECUPERO_SCELTA");
		faultBean.setOriginalFaultString("Timeout recupero scelta original");
		faultBean.setOriginalDescription("Timeout recupero scelta original");
		
		fault.value = faultBean;
		
//		effettuazioneScelta.value = StEffettuazioneScelta.SI;
//		identificativoCanale.value = "00348170101_01";
//		identificativoPSP.value = "UNCRITMM";
//		identificativoIntermediarioPSP.value = "00348170101";
//		tipoVersamento.value = StTipoVersamento.CP;
		
		PagamentiTelematiciRPTservice ss = null;

		try {
			ss = new PagamentiTelematiciRPTservice();
		}
		catch (Exception murle) {
			log.error("Failed to initialize FESP client", murle);

			throw new RuntimeException("Failed to initialize FESP client", murle);
		}

		PagamentiTelematiciRPT port = ss.getPagamentiTelematiciRPTPort();
		((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.nodoChiediSceltaWISPUrl);

		port.nodoChiediSceltaWISP(identificativoIntermediarioPA, identificativoStazioneIntermediarioPA, password, identificativoDominio, keyPA, keyWISP, fault,
				effettuazioneScelta, identificativoPSP, identificativoIntermediarioPSP, identificativoCanale, tipoVersamento);
	}

	@Override
	public NodoInviaRispostaRevocaRisposta nodoInviaRispostaRevoca(
			String identificativoIntermediarioPA,
			String identificativoStazioneIntermediarioPA, String password,
			String identificativoDominio,
			String identificativoUnivocoVersamento,
			String codiceContestoPagamento, byte[] er, Holder<FaultBean> fault,
			Holder<String> esito) {
		// TODO Auto-generated method stub
		
		NodoInviaRispostaRevocaRisposta ris = new NodoInviaRispostaRevocaRisposta();
		
		ris.setEsito("OK");
		ris.setFault(null);
		
//		ris.setEsito("KO");
//		FaultBean f = new FaultBean();
//		f.setFaultCode("faultCode");
//		f.setFaultString("faultString");
//		f.setDescription("description");
//		f.setOriginalFaultCode("OriginalFaultCode");
//		f.setOriginalFaultString("originalFaultString");
//		f.setOriginalDescription("OriginalDescription");
//		ris.setFault(f);
		
		return ris;
		
	}
}
