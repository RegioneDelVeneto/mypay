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
import it.regioneveneto.mygov.payment.mypay4.exception.RollbackException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciRPTClient;
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
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
public class NodoPagamentiTelematiciRPTService {

	public static final String ERROR_INVIA_CARRELLO_RPT_PPT_PAGAMENTO_DUPLICATO = "PPT_PAGAMENTO_DUPLICATO";

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
	PagamentiTelematiciRPTClient pagamentiTelematiciRPTClient;
	@Autowired
	private RPTConservazioneService rptConservazioneService;
//  @Autowired
//  CarrelloService carrelloService;
//  @Autowired
//  DovutoService dovutoService;
//  @Autowired
//  private DovutoMultibeneficiarioDao dovutoMultibenDao;
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

	private static final String PATH_RP_ESITO_XSD = "/wsdl/pa/PagInf_RP_Esito_6_2_0.xsd";
	private static final String FESP_LANDING_PATH = "/fesp/landing/richiestaPagamento?idSession=";
	private static final String ANONYMOUS_FESP_LANDING_PATH = MyPay4AbstractSecurityConfig.PATH_PUBLIC + FESP_LANDING_PATH;

	@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
	public NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP request) {
		NodoSILInviaCarrelloRPRisposta responseCart = null;
		NodoInviaCarrelloRPTRisposta responseNodo = null;
		CarrelloRp carrelloRp = null;
		CarrelloRpt carrelloRpt = null;
		String idSession = Utilities.getRandomicUUID();
		Psp pspAgID = this.newPspAgidValue();
		NodoInviaCarrelloRPT nodoInviaCarrelloRPT = new NodoInviaCarrelloRPT();
		boolean forceRollbackTransaction = false;
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
			intestazioneCarrelloPPT.setIdentificativoIntermediarioPA(carrelloRpt.getCodRptInviacarrellorptIdIntermediarioPa());
			intestazioneCarrelloPPT.setIdentificativoStazioneIntermediarioPA(carrelloRpt.getCodRptInviacarrellorptIdStazioneIntermediarioPa());

      // Multi-beneficiary IUV management if defined (IUV_MULTI_08, IUV_MULTI_09)
      TipoElementoListaRPT elementoSecondarioRPT = null;
      boolean multibeneficiario = false;
      if (listRP.size()==1) {
        elementoSecondarioRPT = createSecondaryRPTElementIfMultiBeneficiaryIUVManaged(listRP.get(0));
        multibeneficiario = null != elementoSecondarioRPT;
      }

			for (ElementoRP elementoRP: listRP) {
				RP rp = jaxbTransformService.unmarshalling(elementoRP.getRp(), RP.class, PATH_RP_ESITO_XSD);
        TipoElementoListaRPT elementoRPT = new TipoElementoListaRPT();

        String ccp = null;
        if (multibeneficiario) {
          ccp = elementoSecondarioRPT.getCodiceContestoPagamento();
          rp.getDatiVersamento().setCodiceContestoPagamento(ccp);
          intestazioneCarrelloPPT.setIdentificativoCarrello(ccp);
          elementoRPT.setCodiceContestoPagamento(ccp);
        } else {
          intestazioneCarrelloPPT.setIdentificativoCarrello(carrelloRpt.getCodRptInviacarrellorptIdCarrello());
          elementoRPT.setCodiceContestoPagamento(elementoRP.getCodiceContestoPagamento());
        }

				RpE rpe = saveRp(elementoRP, pspAgID, rp, carrelloRp);
				Ente enteProp = enteService.getEnteByCodFiscale(rp.getDominio().getIdentificativoDominio());
				RPT ctRPT = inviaCarrelloBuilderService.buildRPT(rp, enteProp);

				elementoRPT.setIdentificativoDominio(elementoRP.getIdentificativoDominio());
				elementoRPT.setIdentificativoUnivocoVersamento(elementoRP.getIdentificativoUnivocoVersamento());
				elementoRPT.setTipoFirma(Constants.EMPTY);
				elementoRPT.setRpt(jaxbTransformService.marshallingAsBytes(ctRPT, RPT.class));
				RptRt mygovRptRt = saveRPT(elementoRPT, nodoInviaCarrelloRPT, intestazioneCarrelloPPT, ctRPT, rpe, carrelloRpt, ccp);

				try {
					RPT_Conservazione rtpCons = rptConservazioneService.insertRptConservazione(ctRPT,  mygovRptRt, enteProp );
					log.debug(rtpCons.toString());
				} catch (Exception e) {
					log.error("Errore nell'inserimento in RPT_Conservazione", e);
				}

        // Multi-beneficiary IUV management if defined (IUV_MULTI_08) - Setting order of RPT
        if (multibeneficiario) {
					//if importo(ente-primario)==0 -> do not send RPT
					if(ctRPT.getDatiVersamento().getImportoTotaleDaVersare().compareTo(BigDecimal.ZERO) > 0) {
						listaRPT.getElementoListaRPTs().add(elementoRPT);
						nodoInviaCarrelloRPT.setMultiBeneficiario(true);
					} else
						log.warn("nodoInviaCarrelloRPT: RPT with importo ente-primario ZERO ente[{}] iuv[{}] ccp[{}]",ctRPT.getDominio().getIdentificativoDominio(),
							ctRPT.getDatiVersamento().getIdentificativoUnivocoVersamento(), ctRPT.getDatiVersamento().getCodiceContestoPagamento());
          listaRPT.getElementoListaRPTs().add(elementoSecondarioRPT);
        } else
          listaRPT.getElementoListaRPTs().add(elementoRPT);

			}
			nodoInviaCarrelloRPT.setListaRPT(listaRPT);

			responseNodo = pagamentiTelematiciRPTClient.nodoInviaCarrelloRPT(nodoInviaCarrelloRPT, intestazioneCarrelloPPT);

			if(responseNodo!=null && responseNodo.getFault()!=null && StringUtils.equals(responseNodo.getFault().getFaultCode(),ERROR_INVIA_CARRELLO_RPT_PPT_PAGAMENTO_DUPLICATO)){
				log.error("forceRollbackTransction idCarrello[{}] cause[{}]", intestazioneCarrelloPPT.getIdentificativoCarrello(), ERROR_INVIA_CARRELLO_RPT_PPT_PAGAMENTO_DUPLICATO);
				throw new RollbackException(ERROR_INVIA_CARRELLO_RPT_PPT_PAGAMENTO_DUPLICATO);
			}

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

		} catch (RollbackException re){
			//just rethrow it: we want rollback to occur
			forceRollbackTransaction = true;
			throw re;
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
			if(!forceRollbackTransaction)
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

	private RpE saveRp(ElementoRP elementoRP, Psp psp, RP rp, CarrelloRp carrelloRp) {
		RpE rpE = rpEService.insertRp(elementoRP, psp, rp, carrelloRp);
		List<CtDatiSingoloVersamentoRP> ctDatiSingoloVersamentoRPList = rp.getDatiVersamento().getDatiSingoloVersamentos();
		if(!ctDatiSingoloVersamentoRPList.isEmpty()) {
			rpEDettaglioService.insertRpEDettaglio(rpE, ctDatiSingoloVersamentoRPList);
		}
		return rpE;
	}

  private RptRt saveRPT(TipoElementoListaRPT elementoRPT, NodoInviaCarrelloRPT nodoInviaCarrelloRPT, IntestazioneCarrelloPPT intestazioneCarrelloPPT, RPT rpt, RpE rpE, CarrelloRpt carrelloRpt,String CCP) {
		RptRt rptRt = rptRtService.insertRpt(elementoRPT, nodoInviaCarrelloRPT, intestazioneCarrelloPPT, rpt, rpE, carrelloRpt);
		List<CtDatiSingoloVersamentoRPT> ctDatiSingoloVersamentoRPTList = rpt.getDatiVersamento().getDatiSingoloVersamentos();
		if(!ctDatiSingoloVersamentoRPTList.isEmpty()) {
      rptRtDettaglioService.insertRptRtDettaglio(rptRt, ctDatiSingoloVersamentoRPTList, CCP);
		}
		return rptRt;
	}

	private void saveCartRpRisposta(CarrelloRp carrelloRp, NodoSILInviaCarrelloRPRisposta response, String idSession) {
		carrelloRpService.updateRispostaRpById(carrelloRp, response, idSession);
		rpEService.updateByKey(carrelloRp, response);
	}

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

  private TipoElementoListaRPT createSecondaryRPTElementIfMultiBeneficiaryIUVManaged(ElementoRP elementoRP) throws Exception {
    TipoElementoListaRPT elementoSecondarioRPT = null;
		RP rp = jaxbTransformService.unmarshalling(elementoRP.getRp(), RP.class, PATH_RP_ESITO_XSD);
		if(rp.getRpEnteSecondario()!=null){
			RP rpSecondario = rp.getRpEnteSecondario();
			Ente enteSecondario = jaxbTransformService.unmarshalling(rp.getDatiEnteSecondario(), Ente.class);
			elementoSecondarioRPT = new TipoElementoListaRPT();
			elementoSecondarioRPT.setCodiceContestoPagamento(elementoRP.getCodiceContestoPagamento());
			elementoSecondarioRPT.setIdentificativoDominio(enteSecondario.getCodiceFiscaleEnte());
			elementoSecondarioRPT.setIdentificativoUnivocoVersamento(elementoRP.getIdentificativoUnivocoVersamento());
			RPT CtSecondarioRPT = inviaCarrelloBuilderService.buildRPT(rpSecondario, enteSecondario);
			elementoSecondarioRPT.setTipoFirma(Constants.EMPTY);
			elementoSecondarioRPT.setRpt(jaxbTransformService.marshallingAsBytes(CtSecondarioRPT, RPT.class));
		}
    return elementoSecondarioRPT;
  }
}