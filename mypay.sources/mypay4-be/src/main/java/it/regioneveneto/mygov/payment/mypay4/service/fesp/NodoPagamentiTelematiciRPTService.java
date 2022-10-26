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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.*;
import it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT;
import it.gov.digitpa.schemas._2011.pagamenti.RPT;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.common.Psp;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciRPTImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ElementoRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRPRisposta;
import it.veneto.regione.schemas._2012.pagamenti.CtDatiSingoloVersamentoRP;
import it.veneto.regione.schemas._2012.pagamenti.RP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
public class NodoPagamentiTelematiciRPTService {

	@Autowired
	RpEService rpEService;
	@Autowired
	EnteService enteService;
	@Autowired
	RptRtService rptRtService;
	@Autowired
	CarrelloRpService carrelloRpService;
	@Autowired
	CarrelloRptService carrelloRptService;
	@Autowired
	RpEDettaglioService rpEDettaglioService;
	@Autowired
	JAXBTransformService jaxbTransformService;
	@Autowired
	RptRtDettaglioService rptRtDettaglioService;
	@Autowired
	InviaCarrelloBuilderService inviaCarrelloBuilderService;
	@Autowired
	PagamentiTelematiciRPTImpl pagamentiTelematiciRPTImpl;

	@Value("${pa.pspDefaultModelloPagamento}")
	private int modelloPagamento;
	@Value("${pa.pspDefaultIdentificativoCanale}")
	private String identificativoCanale;
	@Value("${pa.pspDefaultIdentificativoIntermediarioPsp}")
	private String identificativoIntermediarioPsp;
	@Value("${pa.pspDefaultIdentificativoPsp}")
	private String identificativoPsp;
	@Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
	private String identificativoIntermediarioPA;
	@Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
	private String identificativoStazioneIntermediarioPA;
	@Value("${nodoRegionaleFesp.password}")
	private String password;
	@Value("${app.be.absolute-path}")
	private String appBeAbsolutePath;

	private final static String PATH_RP_ESITO_XSD = "/wsdl/pa/PagInf_RP_Esito_6_2_0.xsd";
	private final static String FESP_LANDING_PATH = "/fesp/landing/richiestaPagamento?idSession=";
	private final static String ANONYMOUS_FESP_LANDING_PATH = MyPay4AbstractSecurityConfig.PATH_PUBLIC + FESP_LANDING_PATH;

	@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
	public NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP request) {
		NodoSILInviaCarrelloRPRisposta responseCart = null;
		NodoInviaCarrelloRPTRisposta responseNodo = null;
		CarrelloRp carrelloRp = null;
		CarrelloRpt carrelloRpt = null;
		String idSession = Utilities.getRandomicUUID();
		Psp pspAgID = this.newPspAgidValue();
		NodoInviaCarrelloRPT nodoInviaCarrelloRPT = new NodoInviaCarrelloRPT();
		try {
			carrelloRp = carrelloRpService.insert(idSession, request.getIdentificativoDominioEnteChiamante());
			carrelloRpt = carrelloRptService.insert(carrelloRp, pspAgID);
			TipoListaRPT listaRPT = new TipoListaRPT();
			List<ElementoRP> listRP = request.getListaRP().getElementoRPs();
			nodoInviaCarrelloRPT.setPassword(pspAgID.getPassword());
			nodoInviaCarrelloRPT.setIdentificativoCanale(pspAgID.getIdentificativoCanale());
			nodoInviaCarrelloRPT.setIdentificativoIntermediarioPSP(pspAgID.getIdentificativoIntermediarioPSP());
			nodoInviaCarrelloRPT.setIdentificativoPSP(pspAgID.getIdentificativoPSP());
			IntestazioneCarrelloPPT intestazioneCarrelloPPT = new IntestazioneCarrelloPPT();
			intestazioneCarrelloPPT.setIdentificativoCarrello(carrelloRpt.getCodRptInviacarrellorptIdCarrello());
			intestazioneCarrelloPPT.setIdentificativoIntermediarioPA(carrelloRpt.getCodRptInviacarrellorptIdIntermediarioPa());
			intestazioneCarrelloPPT.setIdentificativoStazioneIntermediarioPA(carrelloRpt.getCodRptInviacarrellorptIdStazioneIntermediarioPa());
			for (ElementoRP elementoRP: listRP) {
				RP rp = jaxbTransformService.unmarshalling(elementoRP.getRp(), RP.class, PATH_RP_ESITO_XSD);
				RpE rpe = saveRp(elementoRP, pspAgID, rp, carrelloRp);
				Ente enteProp = enteService.getEnteByCodFiscale(rp.getDominio().getIdentificativoDominio());
				RPT CtRPT = inviaCarrelloBuilderService.buildRPT(rp, enteProp);

				TipoElementoListaRPT elementoRPT = new TipoElementoListaRPT();
				elementoRPT.setCodiceContestoPagamento(elementoRP.getCodiceContestoPagamento());
				elementoRPT.setIdentificativoDominio(elementoRP.getIdentificativoDominio());
				elementoRPT.setIdentificativoUnivocoVersamento(elementoRP.getIdentificativoUnivocoVersamento());
				elementoRPT.setTipoFirma(Constants.EMPTY);
				elementoRPT.setRpt(jaxbTransformService.marshallingAsBytes(CtRPT, RPT.class));
				listaRPT.getElementoListaRPTs().add(elementoRPT);
				saveRPT(elementoRPT, nodoInviaCarrelloRPT, intestazioneCarrelloPPT, CtRPT, rpe, carrelloRpt);
			}
			nodoInviaCarrelloRPT.setListaRPT(listaRPT);

			responseNodo = pagamentiTelematiciRPTImpl.nodoInviaCarrelloRPT(intestazioneCarrelloPPT, nodoInviaCarrelloRPT);

			responseCart = new NodoSILInviaCarrelloRPRisposta();
			if(responseNodo!=null && responseNodo.getFault()!=null){
				FaultBean fault = new FaultBean();
				fault.setFaultCode(responseNodo.getFault().getFaultCode());
				fault.setFaultString(responseNodo.getFault().getFaultString());
				fault.setId(responseNodo.getFault().getId());
				fault.setDescription(responseNodo.getFault().getDescription());
				fault.setOriginalFaultCode(responseNodo.getFault().getOriginalFaultCode());
				fault.setOriginalFaultString(responseNodo.getFault().getOriginalFaultString());
				fault.setOriginalDescription(responseNodo.getFault().getOriginalDescription());
				fault.setSerial(responseNodo.getFault().getSerial());
				responseCart.setFault(fault);
			}
			responseCart.setEsito(responseNodo.getEsitoComplessivoOperazione());
			if(StringUtils.equalsIgnoreCase(responseNodo.getEsitoComplessivoOperazione(), "OK"))
				responseCart.setUrl(appBeAbsolutePath + ANONYMOUS_FESP_LANDING_PATH + idSession);

		} catch (Exception e) {
			log.error(String.format("{}: [{}]", FaultCodeConstants.PPT_ESITO_SCONOSCIUTO, e.getMessage()), e);

			responseNodo = new NodoInviaCarrelloRPTRisposta();
			responseNodo.setEsitoComplessivoOperazione(FaultCodeConstants.ESITO_KO);
			gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean faultRPT = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean();
			faultRPT.setFaultCode(FaultCodeConstants.PPT_ESITO_SCONOSCIUTO);
			faultRPT.setDescription(e.getMessage());
			ListaErroriRPT listaErroriRPT = new ListaErroriRPT();
			listaErroriRPT.getFaults().addAll(Collections.singletonList(faultRPT));
			responseNodo.setListaErroriRPT(listaErroriRPT);

			//RISPOSTA RP
			responseCart = new NodoSILInviaCarrelloRPRisposta();
			responseCart.setEsito(responseNodo.getEsitoComplessivoOperazione());
			FaultBean faultRP = new FaultBean();
			faultRP.setFaultCode(faultRPT.getFaultCode());
			faultRP.setDescription(faultRPT.getDescription());
			responseCart.setFault(faultRP);
		} finally {
			try {
				if (carrelloRp!=null && responseCart!=null) {
					saveCartRpRisposta(carrelloRp, responseCart, idSession);
					if  (carrelloRpt!=null && responseNodo!=null) {
						saveCarrelloRPTRisposta(responseNodo, carrelloRpt);
					}
				}
			} catch (Exception e) {
				log.error(String.format("Error saving RP risposta: [{}]", e.getMessage()), e);
			}
		}
		return responseCart;
	}

	@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
	private RpE saveRp(ElementoRP elementoRP, Psp psp, RP rp, CarrelloRp carrelloRp) {
		RpE rpE = rpEService.insertRp(elementoRP, psp, rp, carrelloRp);
		List<CtDatiSingoloVersamentoRP> ctDatiSingoloVersamentoRPList = rp.getDatiVersamento().getDatiSingoloVersamentos();
		if(!ctDatiSingoloVersamentoRPList.isEmpty()) {
			rpEDettaglioService.insertRpEDettaglio(rpE, ctDatiSingoloVersamentoRPList);
		}
		return rpE;
	}

	@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
	private RptRt saveRPT(TipoElementoListaRPT elementoRPT, NodoInviaCarrelloRPT nodoInviaCarrelloRPT, IntestazioneCarrelloPPT intestazioneCarrelloPPT, RPT rpt, RpE rpE, CarrelloRpt carrelloRpt) {
		RptRt rptRt = rptRtService.insertRpt(elementoRPT, nodoInviaCarrelloRPT, intestazioneCarrelloPPT, rpt, rpE, carrelloRpt);
		List<CtDatiSingoloVersamentoRPT> ctDatiSingoloVersamentoRPTList = rpt.getDatiVersamento().getDatiSingoloVersamentos();
		if(!ctDatiSingoloVersamentoRPTList.isEmpty()) {
			rptRtDettaglioService.insertRptRtDettaglio(rptRt, ctDatiSingoloVersamentoRPTList);
		}
		return rptRt;
	}

	@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
	private void saveCartRpRisposta(CarrelloRp carrelloRp, NodoSILInviaCarrelloRPRisposta response, String idSession) {
		carrelloRpService.updateRispostaRpById(carrelloRp, response, idSession);
		rpEService.updateByKey(carrelloRp, response);
	}

	@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
	private void saveCarrelloRPTRisposta(NodoInviaCarrelloRPTRisposta respondeNodo, CarrelloRpt carrelloRpt) throws UnsupportedEncodingException, MalformedURLException {
		carrelloRptService.updateByKey(respondeNodo, carrelloRpt);
		rptRtService.updateRispostaRptByCart(respondeNodo, carrelloRpt);
	}

	private Psp newPspAgidValue() {
		return Psp.builder()
			.identificativoCanale(identificativoCanale)
			.identificativoIntermediarioPSP(identificativoIntermediarioPsp)
			.identificativoPSP(identificativoPsp)
			.password(password)
			.identificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPA)
			.identificativoIntermediarioPA(identificativoIntermediarioPA)
			.modelloPagamento(modelloPagamento)
			.build();
	}
}
