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
package it.regioneveneto.mygov.payment.mypay4.ws.impl;

import it.regioneveneto.mygov.payment.mypay4.dto.AttualizzazioneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.ExportReceiptEvent;
import it.regioneveneto.mygov.payment.mypay4.dto.OutcomeEmailNotifierEvent;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.SystemBlockService;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.GpdService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.client.PagamentiTelematiciEsterniCCPClient;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.pa.*;
import it.veneto.regione.schemas._2012.pagamenti.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service("PagamentiTelematiciCCPPaImpl")
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciCCPPaImpl {

	public static final Date MAX_DATE = Utilities.toDate(LocalDate.of(2099,12,31));

	@Value("${pa.gpd.preload}")
	private boolean gpdPreload;
	@Autowired
	private EnteService enteService;
	@Autowired
	private DovutoService dovutoService;
	@Autowired
	private DovutoElaboratoService dovutoElaboratoService;
	@Autowired
	private EnteTipoDovutoService enteTipoDovutoService;
	@Autowired
	private PagamentiTelematiciEsterniCCPClient pagamentiTelematiciEsterniCCPClient;
	@Autowired
	private AnagraficaSoggettoService anagraficaSoggettoService;
	@Autowired
	private SendRtOutcomeService sendRtOutcomeService;
	@Autowired
	private ReceiptService receiptService;
  @Autowired
	private SystemBlockService systemBlockService;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private EsitoService esitoService;
	@Autowired
	private CarrelloService carrelloService;
	@Autowired
	private TassonomiaService tassonomiaService;
	@Autowired
	private AttualizzazioneImportoService attualizzazioneImportoService;
	@Autowired
	CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;
	@Autowired
	DatiMarcaBolloDigitaleService marcaBolloDigitaleService;
	@Autowired(required = false)
	private GpdService gpdService;

	private static final String PAYMENT_NOTE_PREFIX = Constants.ID_CART_PREFIX + Constants.DASH_SEPARATOR;

	@Value("${pa.modelloUnico}")
	private boolean modelloUnico;
	@Value("${pa.gpd.enabled}")
	private boolean gpdEnabled;

	@LogExecution()
	@Transactional(propagation = Propagation.REQUIRED)
	public PaSendRTRisposta paSendRT(PaSendRT request) {
		var receipt = request.getReceipt();
		BiFunction<StOutcome, Pair<String, String>, PaSendRTRisposta> result = (outcome, error) -> {
			PaSendRTRisposta response = new PaSendRTRisposta();
			response.setOutcome(outcome);
			if (Objects.nonNull(error)) {
				var fault = new FaultBean();
				fault.setFaultCode(error.getFirst());
				fault.setDescription(error.getSecond());
				fault.setFaultString(error.getSecond());
				fault.setId(receipt.getFiscalCode());
				fault.setSerial(0);
				response.setFault(fault);
			}
			return response;
		};

		var ente = enteService.getEnteByCodFiscale(receipt.getFiscalCode());
		List<Dovuto> dovuti;
		if (ente == null) {
			dovuti = Collections.emptyList();
			log.info("receipt id[{}] noticeNumber[{}] ente[{}] - payment not managed by MyPay", receipt.getReceiptId(), receipt.getNoticeNumber(), receipt.getFiscalCode());
		} else {
			dovuti =	Optional.of(receipt.getCreditorReferenceId())
				.filter(Utilities::isAvviso)
				.map(iuv -> dovutoService.searchDovutoByIuvEnte(iuv, ente.getCodIpaEnte()))
				.orElse(new ArrayList<>());
		}
		Optional<CarrelloMultiBeneficiario> multiCart = Optional.empty();
		if(isModelloUnicoWithIdCart(receipt.getPaymentNote()) && !dovuti.isEmpty()) {
			String idSession = receipt.getPaymentNote() .substring(PAYMENT_NOTE_PREFIX.length());
			multiCart = carrelloMultiBeneficiarioService.getByIdSession(idSession);
			carrelloMultiBeneficiarioService.updateCarrelloMultiBeneficiarioStatus(
				multiCart.orElseThrow(() -> new NotFoundException("carrello not found for idSession "+idSession)).getMygovCarrelloMultiBeneficiarioId(),
				Constants.STATO_CARRELLO_PAGATO, true);
		}
		Carrello cart = null;
		if(!dovuti.isEmpty())
			cart = carrelloService.upsert(sendRtOutcomeService.buildFakeCart(receipt, dovuti.get(0), multiCart));

		Long mygovReceiptId;
		if (modelloUnico) {
			List<DovutoElaborato> elaborati = new ArrayList<>();
			AtomicInteger indexTransfer = new AtomicInteger(1);
			for (Dovuto dovuto : dovuti) {
				sendRtOutcomeService.processOutComeForSendRT(receipt, request.getReceiptBytes(), dovuto, cart, indexTransfer.getAndIncrement())
					.ifPresent(elaborati::add);
				if (gpdPreload) {
					gpdService.managePreload('A', dovuto, null);
				}
			}
			mygovReceiptId = receiptService.insertNewReceipt(receipt, request.getReceiptBytes(), elaborati.stream().findFirst());
			elaborati.forEach(esitoService::handlePushEsito);
		} else {
			if (dovuti.size() > 1)
				return result.apply(StOutcome.KO, Pair.of(FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore pagamento multiplo"));

			Optional<DovutoElaborato> dovutoElaborato = Optional.empty();
			if (!dovuti.isEmpty()) {
				var dovuto = dovuti.get(0);
				//if dovuto in state "da pagare" -> modello 3 -> receipt should be processed
				//if dovuto in state "pagamento in corso" -> modello 1 and RT yet not processed -> postpone receipt processing
				if (request.isForce() || dovuto.getMygovAnagraficaStatoId().getCodStato().equalsIgnoreCase(Constants.STATO_DOVUTO_DA_PAGARE))
					dovutoElaborato = sendRtOutcomeService.processOutComeForSendRT(receipt, request.getReceiptBytes(), dovuto, cart, dovuti.size());
				else if (dovuto.getMygovAnagraficaStatoId().getCodStato().equalsIgnoreCase(Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO))
					return result.apply(StOutcome.KO, Pair.of(FaultCodeConstants.PAA_WAIT, "postpone processing of receipt"));
			} else if (ente != null) {
				var dovutoElaboratoList = dovutoElaboratoService
					.getByIuvEnteStato(receipt.getCreditorReferenceId(), ente.getCodIpaEnte(), Constants.STATO_DOVUTO_COMPLETATO, true, null);

				if (CollectionUtils.isNotEmpty(dovutoElaboratoList)) {
					var dovutoElaboratoOkList = dovutoElaboratoList.stream().filter(de -> Objects.equals(de.getCodEDatiPagCodiceEsitoPagamento(), Constants.CODICE_ESITO_PAGAMENTO_OK_OBJ)).collect(Collectors.toUnmodifiableList());
					if (dovutoElaboratoOkList.isEmpty()) {
						//anomaly: there are at least 1 D.E. status KO, but 0 D.E. status OK
						log.error("received receipt but for ente[{}] iuv[{}] only exist dovutoElaborato KO", ente.getCodIpaEnte(), receipt.getCreditorReferenceId());
						throw new MyPayException("invalid dovutoElaborato state for ente/iuv " + ente.getCodIpaEnte() + "/" + receipt.getCreditorReferenceId());
					} else {
						dovutoElaborato = dovutoElaboratoOkList.stream().max(Comparator.comparing(DovutoElaborato::getDtUltimoCambioStato));
					}
				}
			}
			mygovReceiptId = receiptService.insertNewReceipt(receipt, request.getReceiptBytes(), dovutoElaborato);

			//only for modello 1, add data from receipt to dovuto_elaborato_multibeneficiario (that has been already inserted when RT arrived at primary ente)
			dovutoElaborato.filter(d -> ObjectUtils.notEqual(d.getModelloPagamento(), Constants.MODELLO_PAG.TRE.getValue())).ifPresent(d ->
				dovutoElaboratoService.getDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(d.getMygovDovutoElaboratoId()).ifPresent(dme ->
					receipt.getTransferList().getTransfers()
						.stream()
						.skip(1).findFirst()
						.ifPresent(ctTransferPA ->
							dovutoElaboratoService.updateFromReceipt(dme.getMygovDovutoMultibeneficiarioElaboratoId(),
								ctTransferPA.getRemittanceInformation(), ctTransferPA.getTransferCategory()))));

			dovutoElaborato.ifPresent(esitoService::handlePushEsito);
		}
		if (cart!=null && cart.getMygovCarrelloId()!=null)
			applicationEventPublisher.publishEvent(OutcomeEmailNotifierEvent.builder()
				.mygovCarrelloId(cart.getMygovCarrelloId())
				.build());
		//in case of receipt for multi-beneficiary payment, export to MyPivot
		if(receipt.getTransferList().getTransfers()
			.stream()
			.map(CtTransferPAReceipt::getFiscalCodePA)
			.anyMatch(Predicate.not(receipt.getFiscalCode()::equals))) {
			applicationEventPublisher.publishEvent(ExportReceiptEvent.builder()
				.mygovReceiptId(mygovReceiptId)
				.build());
		}
		return result.apply(StOutcome.OK,null);
	}

	@LogExecution()
	public PaVerifyPaymentNoticeRisposta paVerifyPaymentNotice(PaVerifyPaymentNotice request) {

		BiFunction<String, String, PaVerifyPaymentNoticeRisposta> returnFault = (String code, String descr) -> {
			PaVerifyPaymentNoticeRisposta response = new PaVerifyPaymentNoticeRisposta();
			response.setFault(new FaultBean());
			response.getFault().setFaultCode(code);
			response.getFault().setDescription(descr);
			response.getFault().setFaultString(descr);
			response.getFault().setId(request.getQrCodeFiscalCode());
			response.getFault().setSerial(0);
			return response;
		};

		try{
			Ente ente = enteService.getEnteByCodFiscale(request.getQrCodeFiscalCode());
			if (ente == null || Utilities.checkIfStatoInserito(ente))
				return returnFault.apply(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_CODE, FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);

			if(StringUtils.isNotBlank(ente.getDeUrlEsterniAttiva())){
				PaExternalVerifyPaymentNoticeReq externalReq = new PaExternalVerifyPaymentNoticeReq();
				externalReq.setIdBrokerPA(request.getIdBrokerPA());
				externalReq.setIdStation(request.getIdStation());
				externalReq.setIdPA(request.getIdPA());
				externalReq.setQrCodeFiscalCode(request.getQrCodeFiscalCode());
				externalReq.setNoticeNumber(request.getNoticeNumber());

				PaExternalVerifyPaymentNoticeRes externalRes = pagamentiTelematiciEsterniCCPClient.paExternalVerifyPaymentNotice(externalReq, ente.getDeUrlEsterniAttiva());
				PaVerifyPaymentNoticeRisposta response = new PaVerifyPaymentNoticeRisposta();
				if(externalRes.getFault()!=null){
					response.setFault(new FaultBean());
					response.getFault().setFaultCode(externalRes.getFault().getFaultCode());
					response.getFault().setFaultString(externalRes.getFault().getFaultString());
					response.getFault().setDescription(externalRes.getFault().getDescription());
				} else {
					response.setFiscalCodePA(externalRes.getFiscalCodePA());
					response.setCompanyName(externalRes.getCompanyName());
					response.setOfficeName(externalRes.getOfficeName());
					response.setPaymentDescription(externalRes.getPaymentDescription());
					response.setAmount(externalRes.getAmount());
					response.setDueDate(externalRes.getDueDate());
					response.setDetailDescription(response.getDetailDescription());
					response.setAllCCP(response.isAllCCP());
				}
				return response;
			}

			String iuv;
			try{
				iuv = Utilities.numeroAvvisoToIuvValidator(request.getNoticeNumber());
			} catch(ValidatorException ve){
				log.warn("invalid IUV for notice number {}/{}", request.getQrCodeFiscalCode(), request.getNoticeNumber());
				return returnFault.apply(FaultCodeConstants.PAA_IUV_NON_VALIDO_CODE, FaultCodeConstants.PAA_IUV_NON_VALIDO_STRING);
			}

			List<Dovuto> dovuti = dovutoService.searchDovutoByIuvEnte(iuv, ente.getCodIpaEnte());
			if (dovuti == null || dovuti.size() == 0) {

				List<DovutoElaborato> dovutiElaborati = dovutoElaboratoService.searchDovutoElaboratoByIuvEnte(iuv, ente.getCodIpaEnte());

				if ((dovutiElaborati == null) || (dovutiElaborati.size() == 0))
					return returnFault.apply(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_CODE, FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);

				for (DovutoElaborato dovutoElaborato : dovutiElaborati) {
					if (Constants.STATO_DOVUTO_ANNULLATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato()))
						return returnFault.apply(FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_CODE, FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_STRING);

					if (Constants.STATO_DOVUTO_COMPLETATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato())
						&& dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento() != null
						&& !dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento().equals(Constants.CODICE_ESITO_PAGAMENTO_KO))
						return returnFault.apply(FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_CODE, FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_STRING);
				}

				return returnFault.apply(FaultCodeConstants.PAA_SYSTEM_ERROR, "Il pagamento richiesto ha stato non atteso");
			}

			// Se esiste piu di un dovuto nella tabella DOVUTO
			if (dovuti.size() > 1)
				return returnFault.apply(FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore pagamento multiplo");

			Dovuto dovuto = dovuti.get(0);

			//check blacklist/whitelist codice fiscale
			systemBlockService.blockByPayerCf(Optional.ofNullable(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco()).orElse(Constants.CODICE_FISCALE_ANONIMO));

			Optional<DovutoMultibeneficiario> dovutoMultibeneficiario = Optional.ofNullable(dovutoService.getDovMultibenefByIdDovuto(dovuto.getMygovDovutoId()));

			Optional<EnteTipoDovuto> enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), ente.getCodIpaEnte(), false);

			if (enteTipoDovutoOptional.isEmpty())
				return returnFault.apply(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_VALIDO_PER_ENTE, "Tipo dovuto non trovato per ente");

			EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOptional.get();
			if (!enteTipoDovuto.isFlgAttivo())
				return returnFault.apply(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_ABILITATO_PER_ENTE, "Tipo dovuto non abilitato per ente");

			if (enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE))
				return returnFault.apply(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_VALIDO_PER_ENTE, "Pagamento marca da bollo digitale non supportato per modello 3 per ente");

			FaultBean faultBean = new FaultBean();
			if (!checkDovutoPagabile(dovuto, enteTipoDovuto.isFlgScadenzaObbligatoria(), faultBean, request.getQrCodeFiscalCode()))
				return returnFault.apply(faultBean.getFaultCode(), faultBean.getFaultString());

			if ((!dovuto.getCodRpDatiVersTipoVersamento().equals(Constants.ALL_PAGAMENTI))
				&& (!dovuto.getCodRpDatiVersTipoVersamento().contains(Constants.PAY_PRESSO_PSP)))
				return returnFault.apply(FaultCodeConstants.PAA_TIPO_VERSAMENTO_ERRATO, "Tipo versamento non consentito");

			//implementazione PND Start
			BigDecimal importoPosizione = BigDecimal.ZERO;
			String bilancioStr = null;
			if(!gpdEnabled) {
				try {
					AttualizzazioneTo attualizzazione = attualizzazioneImportoService.attualizzaImporto(iuv, enteTipoDovuto, ente);
					importoPosizione = attualizzazione.getImportoPosizione();
					bilancioStr = attualizzazione.getBilancio();
				} catch (AttualizzaImportoException ae) {
					if (ae.isBlocking())
						return returnFault.apply(FaultCodeConstants.PAA_DOVUTO_NON_PAGABILE, "[" + ae.getCode() + "] " + ae.getMessage());
					else
						log.warn("Eccezione gestita nell'attualizzazione dell'importo, ignoring it", ae);
				} catch (Exception e) {
					log.warn("Eccezione nell'attualizzazione dell'importo, ignoring it", e);
				}
			}
			if (importoPosizione == null || importoPosizione.compareTo(BigDecimal.ZERO) <= 0) {
				importoPosizione = dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento() ;
			}
			else {  // aggiornare importo su DB
				dovuto.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(importoPosizione);
				if (bilancioStr != null) {
					dovuto.setBilancio(bilancioStr);
				}
				dovutoService.upsert(dovuto);
			}

			PaVerifyPaymentNoticeRisposta response = new PaVerifyPaymentNoticeRisposta();
			response.setFiscalCodePA(request.getQrCodeFiscalCode());
			response.setCompanyName(ente.getDeNomeEnte());
			response.setOfficeName(null);
			response.setAmount(
					importoPosizione.add(
					dovutoMultibeneficiario.map(DovutoMultibeneficiario::getNumRpDatiVersDatiSingVersImportoSingoloVersamento)
						.orElse(BigDecimal.ZERO).setScale(2)));
			if(dovuto.getDtRpDatiVersDataEsecuzionePagamento()!=null)
				response.setDueDate(Utilities.toXMLGregorianCalendar(dovuto.getDtRpDatiVersDataEsecuzionePagamento()));
			response.setPaymentDescription(calcolaCausaleRispostaCCP(dovuto, enteTipoDovuto));
			response.setDetailDescription(null);
			response.setAllCCP(StringUtils.isNotBlank(enteTipoDovuto.getIbanAccreditoPi()));

			return response;

		} catch (Exception ex) {
			log.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
			return returnFault.apply(FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore generico: "+ex.getMessage());
		}

	}

	@LogExecution()
	public PaGetPaymentRisposta paGetPayment(PaGetPayment request) {

		BiFunction<String, String, PaGetPaymentRisposta> returnFault = (String code, String descr) -> {
			PaGetPaymentRisposta response = new PaGetPaymentRisposta();
			response.setFault(new FaultBean());
			response.getFault().setFaultCode(code);
			response.getFault().setDescription(descr);
			response.getFault().setFaultString(descr);
			response.getFault().setId(request.getQrCodeFiscalCode());
			response.getFault().setSerial(0);
			return response;
		};

		try{
			Ente ente = enteService.getEnteByCodFiscale(request.getQrCodeFiscalCode());
			if (ente == null || Utilities.checkIfStatoInserito(ente))
				return returnFault.apply(FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_CODE, FaultCodeConstants.PAA_ID_DOMINIO_ERRATO_STRING);

			if(StringUtils.isNotBlank(ente.getDeUrlEsterniAttiva())){
				PaExternalGetPaymentReq externalReq = new PaExternalGetPaymentReq();
				externalReq.setIdPA(request.getIdPA());
				externalReq.setIdBrokerPA(request.getIdBrokerPA());
				externalReq.setIdStation(request.getIdStation());
				externalReq.setQrCodeFiscalCode(request.getQrCodeFiscalCode());
				externalReq.setNoticeNumber(request.getNoticeNumber());
				externalReq.setAmount(request.getAmount());
				externalReq.setPaymentNote(request.getPaymentNote());
				externalReq.setTransferType(request.getTransferType());
				externalReq.setDueDate(request.getDueDate());
				PaExternalGetPaymentRes externalRes = pagamentiTelematiciEsterniCCPClient.paExternalGetPayment(externalReq, ente.getDeUrlEsterniAttiva());
				PaGetPaymentRisposta response = new PaGetPaymentRisposta();
				if(externalRes.getFault()!=null){
					response.setFault(new FaultBean());
					response.getFault().setFaultCode(externalRes.getFault().getFaultCode());
					response.getFault().setFaultString(externalRes.getFault().getFaultString());
					response.getFault().setDescription(externalRes.getFault().getDescription());
				} else {
					response.setCreditorReferenceId(externalRes.getCreditorReferenceId());
					response.setPaymentAmount(externalRes.getPaymentAmount());
					response.setDueDate(externalRes.getDueDate());
					response.setRetentionDate(externalRes.getRetentionDate());
					response.setLastPayment(externalRes.isLastPayment());
					response.setDescription(externalRes.getDescription());
					response.setCompanyName(externalRes.getCompanyName());
					response.setOfficeName(externalRes.getOfficeName());
					response.setDebtor(externalRes.getDebtor());
					response.getTransferLists().addAll(externalRes.getTransferLists());
				}
				return response;
			}

			String iuv;
			try{
				iuv = Utilities.numeroAvvisoToIuvValidator(request.getNoticeNumber());
			} catch(ValidatorException ve){
				log.warn("invalid IUV for notice number {}/{}", request.getQrCodeFiscalCode(), request.getNoticeNumber());
				return returnFault.apply(FaultCodeConstants.PAA_IUV_NON_VALIDO_CODE, FaultCodeConstants.PAA_IUV_NON_VALIDO_STRING);
			}

			List<Dovuto> dovuti = dovutoService.searchDovutoByIuvEnte(iuv, ente.getCodIpaEnte());
			if (dovuti == null || dovuti.size() == 0) {
				List<DovutoElaborato> dovutiElaborati = dovutoElaboratoService.searchDovutoElaboratoByIuvEnte(iuv, ente.getCodIpaEnte());

				if ((dovutiElaborati == null) || (dovutiElaborati.size() == 0))
					return returnFault.apply(FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_CODE, FaultCodeConstants.PAA_PAGAMENTO_SCONOSCIUTO_STRING);

				for (DovutoElaborato dovutoElaborato : dovutiElaborati) {
					if (Constants.STATO_DOVUTO_ANNULLATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato()))
						return returnFault.apply(FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_CODE, FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_STRING);

					if (Constants.STATO_DOVUTO_COMPLETATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato())
						&& dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento() != null
						&& !dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento().equals(Constants.CODICE_ESITO_PAGAMENTO_KO))
						return returnFault.apply(FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_CODE, FaultCodeConstants.PAA_PAGAMENTO_DUPLICATO_STRING);
				}

				return returnFault.apply(FaultCodeConstants.PAA_SYSTEM_ERROR, "Il pagamento richiesto ha stato non atteso");
			}

			if (!isModelloUnicoWithIdCart(request.getPaymentNote())) {
				// Se esiste piu di un dovuto nella tabella DOVUTO
				if (dovuti.size() > 1)
					return returnFault.apply(FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore pagamento multiplo");
			}

			Dovuto dovuto = dovuti.get(0);

			//check blacklist/whitelist codice fiscale
			systemBlockService.blockByPayerCf(Optional.ofNullable(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco()).orElse(Constants.CODICE_FISCALE_ANONIMO));

			Optional<DovutoMultibeneficiario> dovutoMultibeneficiario = Optional.ofNullable(dovutoService.getDovMultibenefByIdDovuto(dovuto.getMygovDovutoId()));

			Optional<EnteTipoDovuto> enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), ente.getCodIpaEnte(), false);

			if (enteTipoDovutoOptional.isEmpty())
				return returnFault.apply(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_VALIDO_PER_ENTE, "Tipo dovuto non trovato per ente");

			EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOptional.get();
			if (!enteTipoDovuto.isFlgAttivo())
				return returnFault.apply(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_ABILITATO_PER_ENTE, "Tipo dovuto non abilitato per ente");

			if (!isModelloUnicoWithIdCart(request.getPaymentNote()) && enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE))
				return returnFault.apply(FaultCodeConstants.PAA_TIPO_DOVUTO_NON_VALIDO_PER_ENTE, "Pagamento marca da bollo digitale non supportato per modello 3 per ente");

			FaultBean faultBean = new FaultBean();
			if (!checkDovutoPagabile(dovuto, enteTipoDovuto.isFlgScadenzaObbligatoria(), faultBean, request.getQrCodeFiscalCode()))
				return returnFault.apply(faultBean.getFaultCode(), faultBean.getFaultString());

			if ((!dovuto.getCodRpDatiVersTipoVersamento().equals(Constants.ALL_PAGAMENTI))
				&& (!dovuto.getCodRpDatiVersTipoVersamento().contains(Constants.PAY_PRESSO_PSP)))
				return returnFault.apply(FaultCodeConstants.PAA_TIPO_VERSAMENTO_ERRATO, "Tipo versamento non consentito");


			//retrieve dueDate
			XMLGregorianCalendar dueDate = Utilities.toXMLGregorianCalendar(enteTipoDovuto.isFlgScadenzaObbligatoria() ?
				dovuto.getDtRpDatiVersDataEsecuzionePagamento() : MAX_DATE, true);

			//retrieve iban
			String iban = getIbanAccredito(ente, enteTipoDovuto,
				Objects.equals(request.getTransferType(),StTransferType.POSTAL) ? Constants.IDENTIFICATIVO_PSP_POSTE : null,
				Constants.PAY_PRESSO_PSP);

			//retrieve transfer category
			String transferCategory = tassonomiaService.getCleanTransferCategory(dovuto, enteTipoDovuto);

			//retrieve causale
			String causale = calcolaCausaleRispostaCCP(dovuto, enteTipoDovuto);

			BigDecimal importoPosizione = BigDecimal.ZERO;
			String bilancioStr = null;

			if(!gpdEnabled) {
				try {
					AttualizzazioneTo attualizzazione = attualizzazioneImportoService.attualizzaImporto(iuv, enteTipoDovuto, ente);
					importoPosizione = attualizzazione.getImportoPosizione();
					bilancioStr = attualizzazione.getBilancio();
				} catch (AttualizzaImportoException ae) {
					if (ae.isBlocking())
						return returnFault.apply(FaultCodeConstants.PAA_DOVUTO_NON_PAGABILE, "[" + ae.getCode() + "] " + ae.getMessage());
					else
						log.warn("Eccezione gestita nell'attualizzazione dell'importo, ignoring it", ae);
				} catch (Exception e) {
					log.warn("Eccezione nell'attualizzazione dell'importo, ignoring it", e);
				}
			}
			if (importoPosizione == null || importoPosizione.compareTo(BigDecimal.ZERO) <= 0) {
				importoPosizione = dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento() ;
			}
			else {  // aggiornare importo su DB
				dovuto.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(importoPosizione);
				if (bilancioStr != null) {
					dovuto.setBilancio(bilancioStr);
				}
				dovutoService.upsert(dovuto);
			}


			PaGetPaymentRisposta response = new PaGetPaymentRisposta();
			response.setCreditorReferenceId(dovuto.getCodIuv());
			response.setDueDate(dueDate);
			response.setRetentionDate(Utilities.toXMLGregorianCalendar(DateTime.now().plusHours(1).toDate(), true)); //the data validity of this response: set to 1 hour
			//response.setLastPayment(false);  //currently MyPay and pagoPa do not support installment payments
			response.setDescription(causale);
			response.setCompanyName(ente.getDeRpEnteBenefDenominazioneBeneficiario());
			response.setOfficeName(null); //not supported by MyPay

			var anagraficaPagatore = anagraficaSoggettoService.getAnagraficaPagatore(dovuto);
			anagraficaSoggettoService.mapAnagraficaSoggetto(anagraficaPagatore, CtSoggettoPagatore.class).ifPresent(response::setDebtor);

			AtomicInteger indexTransfer = new AtomicInteger(1);
			if(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento().compareTo(BigDecimal.ZERO)>0) {
				CtTransferPA transferPA = new CtTransferPA();
				transferPA.setIdTransfer(indexTransfer.getAndIncrement());
				transferPA.setFiscalCodePA(ente.getCodiceFiscaleEnte());
				if (enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
					var bolloDigitale = marcaBolloDigitaleService.getById(dovuto.getMygovDatiMarcaBolloDigitaleId());
					transferPA.setRichiestaMarcaDaBollo(new CtRichiestaMarcaDaBollo());
					transferPA.getRichiestaMarcaDaBollo().setTipoBollo(bolloDigitale.getTipoBollo());
					transferPA.getRichiestaMarcaDaBollo().setHashDocumento(Base64.decodeBase64(bolloDigitale.getHashDocumento())); //hashDocumento is base64-encoded
					transferPA.getRichiestaMarcaDaBollo().setProvinciaResidenza(bolloDigitale.getProvinciaResidenza());
				} else {
					transferPA.setIBAN(iban);
				}
				transferPA.setTransferAmount(importoPosizione);
				String remittanceInformation = carrelloService.generateCausaleAgIDFormat(dovuto.getCodIuv(), dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento(),
					StringUtils.firstNonBlank(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(), causale));
				transferPA.setRemittanceInformation(StringUtils.left(remittanceInformation, 140));
				transferPA.setTransferCategory(transferCategory);
				CtMetadata metadata = new CtMetadata();
				CtMapEntry entry = new CtMapEntry();
				entry.setKey("datiSpecificiRiscossione");
				entry.setValue(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione());
				metadata.getMapEntries().add(entry);
				transferPA.setMetadata(metadata);
				response.getTransferLists().add(transferPA);
			}
			if(dovutoMultibeneficiario.isPresent()){
				DovutoMultibeneficiario dm = dovutoMultibeneficiario.get();
				CtTransferPA transferPA2 = new CtTransferPA();
				transferPA2.setIdTransfer(indexTransfer.get());
				transferPA2.setFiscalCodePA(dm.getCodiceFiscaleEnte());
				transferPA2.setIBAN(dm.getCodRpDatiVersDatiSingVersIbanAccredito());
				transferPA2.setTransferAmount(dm.getNumRpDatiVersDatiSingVersImportoSingoloVersamento());
				String remittanceInformation2 = carrelloService.generateCausaleAgIDFormat(dovuto.getCodIuv(), dm.getNumRpDatiVersDatiSingVersImportoSingoloVersamento(),
					StringUtils.firstNonBlank(dm.getDeRpDatiVersDatiSingVersCausaleVersamento(), dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(), calcolaCausaleRispostaCCP(dovuto, enteTipoDovuto)));
				transferPA2.setRemittanceInformation(StringUtils.left(remittanceInformation2, 140));
				if(StringUtils.isNotBlank(dm.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione())){
					Matcher matcher = Constants.DATI_SPECIFICI_RISCOSSIONE_REGEX.matcher(dm.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione());
					if(matcher.find())
						transferPA2.setTransferCategory(matcher.group(1));
					else
						throw new ValidatorException("invalid datiSpecificiRiscossione format ["+dm.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione()+"]");
				} else {
					transferPA2.setTransferCategory(transferCategory);
				}
				response.getTransferLists().add(transferPA2);
			}
			if (isModelloUnicoWithIdCart(request.getPaymentNote()) && dovutoMultibeneficiario.isEmpty()) {
				dovuti.stream().skip(1).forEach(item -> {
					var enteTipoDovutoNth = enteTipoDovutoService.getByCodTipo(item.getCodTipoDovuto(), ente.getCodIpaEnte());
					var transferPAnth = new CtTransferPA();
					transferPAnth.setIdTransfer(indexTransfer.getAndIncrement());
					transferPAnth.setFiscalCodePA(ente.getCodiceFiscaleEnte());
					if (enteTipoDovutoNth.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
						var bolloDigitale = marcaBolloDigitaleService.getById(item.getMygovDatiMarcaBolloDigitaleId());
						transferPAnth.setRichiestaMarcaDaBollo(new CtRichiestaMarcaDaBollo());
						transferPAnth.getRichiestaMarcaDaBollo().setTipoBollo(bolloDigitale.getTipoBollo());
						transferPAnth.getRichiestaMarcaDaBollo().setHashDocumento(Base64.decodeBase64(bolloDigitale.getHashDocumento())); //hashDocumento is base64-encoded
						transferPAnth.getRichiestaMarcaDaBollo().setProvinciaResidenza(bolloDigitale.getProvinciaResidenza());
					} else {
						String ibanNth = getIbanAccredito(ente, enteTipoDovutoNth,
							Objects.equals(request.getTransferType(),StTransferType.POSTAL) ? Constants.IDENTIFICATIVO_PSP_POSTE : null,
							Constants.PAY_PRESSO_PSP);
						transferPAnth.setIBAN(ibanNth);
					}
					transferPAnth.setTransferAmount(item.getNumRpDatiVersDatiSingVersImportoSingoloVersamento());
					String remittanceInformation = carrelloService.generateCausaleAgIDFormat(item.getCodIuv(), item.getNumRpDatiVersDatiSingVersImportoSingoloVersamento(),
						StringUtils.firstNonBlank(item.getDeRpDatiVersDatiSingVersCausaleVersamento(), calcolaCausaleRispostaCCP(item, enteTipoDovutoNth)));
					transferPAnth.setRemittanceInformation(StringUtils.left(remittanceInformation, 140));
					transferPAnth.setTransferCategory(tassonomiaService.getCleanTransferCategory(item, enteTipoDovutoNth));
					CtMetadata metadata = new CtMetadata();
					CtMapEntry entry = new CtMapEntry();
					entry.setKey("datiSpecificiRiscossione");
					entry.setValue(item.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione());
					metadata.getMapEntries().add(entry);
					transferPAnth.setMetadata(metadata);
					response.getTransferLists().add(transferPAnth);
				});
			}
			response.setPaymentAmount(response.getTransferLists().stream().map(CtTransferPA::getTransferAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

			return response;

		} catch (Exception ex) {
			log.error(FaultCodeConstants.PAA_SYSTEM_ERROR, ex);
			return returnFault.apply(FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore generico: "+ex.getMessage());
		}

	}

	public static String calcolaCausaleRispostaCCP(Dovuto dovuto, EnteTipoDovuto enteTipoDovuto) {
		return StringUtils.left(StringUtils.firstNonBlank(
			dovuto.getDeCausaleVisualizzata(), dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(), enteTipoDovuto.getDeTipo()).trim(), 140);
	}

	public boolean checkDovutoPagabile(Dovuto dovuto, boolean flagScadenzaObbligatoria,
	                                   FaultBean faultBean, String identificativoDominio) {

		// CONTROLLO DOVUTO GIA' INIZIATO
		if (Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO.equals(dovuto.getMygovAnagraficaStatoId().getCodStato())) {
			faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_IN_CORSO_CODE);
			faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_IN_CORSO_STRING);
			faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_IN_CORSO_STRING);
			faultBean.setId(identificativoDominio);
			faultBean.setSerial(0);
			return false;
		}

		// CONTROLLO DOVUTO SCADUTO
		boolean isDovutoScaduto = dovutoService.isDovutoScaduto(dovuto, flagScadenzaObbligatoria);
		if (isDovutoScaduto) {
			faultBean.setFaultCode(FaultCodeConstants.PAA_PAGAMENTO_SCADUTO_CODE);
			faultBean.setDescription(FaultCodeConstants.PAA_PAGAMENTO_SCADUTO_STRING);
			faultBean.setFaultString(FaultCodeConstants.PAA_PAGAMENTO_SCADUTO_STRING);
			faultBean.setId(identificativoDominio);
			faultBean.setSerial(0);
			return false;
		}

		// STATO DIVERSO DA PAGABILE
		if (!Constants.STATO_DOVUTO_DA_PAGARE.equals(dovuto.getMygovAnagraficaStatoId().getCodStato())) {
			faultBean.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
			faultBean.setDescription("Il pagamento richiesto risulta in uno stato non pagabile");
			faultBean.setFaultString("Il pagamento richiesto risulta in uno stato non pagabile");
			faultBean.setId(identificativoDominio);
			faultBean.setSerial(0);
			return false;
		}

		return true;
	}

	public static String getIbanAccredito(Ente ente, EnteTipoDovuto enteTipoDovuto, String identificativoPSP,
	                               String tipoVersamento) {
		if (!enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
			if (Constants.IDENTIFICATIVO_PSP_POSTE.equals(identificativoPSP)) {
				String ibanAccreditoTipoDovuto = enteTipoDovuto.getIbanAccreditoPi();

				// ACCREDITO
				if (StringUtils.isNotBlank(ibanAccreditoTipoDovuto)) {
					return ibanAccreditoTipoDovuto;
				}
				throw new RuntimeException(
					"Nessun IBAN postale associato al tipo dovuto [ " + enteTipoDovuto.getDeTipo() + " ]");
			} else if (Constants.PAY_MYBANK.equals(tipoVersamento)) {
				String ibanAccreditoPSPTipoDovuto = enteTipoDovuto.getIbanAccreditoPsp();
				String bicAccreditoPSPTipoDovuto = enteTipoDovuto.getBicAccreditoPsp();

				String ibanAccreditoPiTipoDovuto = enteTipoDovuto.getIbanAccreditoPi();
				String bicAccreditoPiTipoDovuto = enteTipoDovuto.getBicAccreditoPi();

				// ACCREDITO
				if (enteTipoDovuto.isBicAccreditoPspSeller() && (StringUtils.isNotBlank(ibanAccreditoPSPTipoDovuto)
					|| StringUtils.isNotBlank(bicAccreditoPSPTipoDovuto))) {
					if (StringUtils.isNotBlank(ibanAccreditoPSPTipoDovuto)) {
						return ibanAccreditoPSPTipoDovuto;
					}
				} else if (enteTipoDovuto.isBicAccreditoPiSeller() && (StringUtils.isNotBlank(ibanAccreditoPiTipoDovuto)
					|| StringUtils.isNotBlank(bicAccreditoPiTipoDovuto))) {
					if (StringUtils.isNotBlank(ibanAccreditoPiTipoDovuto)) {
						return ibanAccreditoPiTipoDovuto;
					}
				} else {
					if (ente.getCodRpDatiVersDatiSingVersBicAccreditoSeller()) {
						// Recupera dall'ente
						if (StringUtils.isNotBlank(ente.getCodRpDatiVersDatiSingVersIbanAccredito())) {
							return ente.getCodRpDatiVersDatiSingVersIbanAccredito();
						}
					} else {
						throw new RuntimeException("Errore nel recupero del iban di accredito per PSP MyBank");
					}
				}
			} else {
				String ibanAccreditoTipoDovuto = enteTipoDovuto.getIbanAccreditoPsp();

				// ACCREDITO

				if (StringUtils.isNotBlank(ibanAccreditoTipoDovuto)) {
					return ibanAccreditoTipoDovuto;

				} else {
					// Recupera dall'ente
					if (StringUtils.isNotBlank(ente.getCodRpDatiVersDatiSingVersIbanAccredito())) {
						return ente.getCodRpDatiVersDatiSingVersIbanAccredito();
					}
				}
			}
		}
		return null;
	}

	private boolean isModelloUnicoWithIdCart(String paymentNote) {
		return modelloUnico && StringUtils.isNotBlank(paymentNote) && Pattern.compile("^(" + PAYMENT_NOTE_PREFIX + ").*$").matcher(paymentNote).matches();
	}
}
