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
package it.regioneveneto.mygov.payment.mypay4.service;

import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivoco;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.schemas._2012.pagamenti.CtReceipt;
import it.veneto.regione.schemas._2012.pagamenti.CtTransferPAReceipt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.*;

@Service
@Slf4j
public class SendRtOutcomeService {

	@Autowired
	EnteService enteService;

	@Autowired
	DovutoService dovutoService;

	@Autowired
	DovutoElaboratoService dovutoElaboratoService;
	
	@Autowired
	AnagraficaStatoService anagraficaStatoService;

	@Autowired
	CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;

	@Autowired
	private DatiMarcaBolloDigitaleService datiMarcaBolloDigitaleService;

	@Value("${pa.deRpVersioneOggetto}")
	private String deRpVersioneOggetto;

	@Transactional(propagation = Propagation.REQUIRED)
	public Optional<DovutoElaborato> processOutComeForSendRT(CtReceipt ctReceipt, byte[] nodeReceiptBytes, Dovuto dovuto, Carrello cart, int index) {
		var dovutoElaborato = dovutoElaboratoService.upsert(buildDovutoElaborato(ctReceipt, nodeReceiptBytes, dovuto, cart, index));
		Optional<DovutoMultibeneficiario> dovutoMultibeneficiario = Optional.ofNullable(dovutoService.getDovMultibenefByIdDovuto(dovuto.getMygovDovutoId()));
		if (dovutoMultibeneficiario.isPresent()) {
			ctReceipt.getTransferList().getTransfers()
				.stream()
				.skip(1).findFirst()
				.ifPresent(ctTransferPA ->
					dovutoElaboratoService.insertDovutoMultibenefElaborato(dovuto, buildDovutoMultibeneficiario(ctTransferPA),
						dovutoElaborato.getMygovDovutoElaboratoId())
				);
			Optional.ofNullable(dovutoService.getDovMultibenefByIdDovuto(dovuto.getMygovDovutoId()))
				.ifPresent(item -> dovutoService.deleteDovMultibenef(item));
		}
		dovutoService.delete(dovuto);
		if (dovuto.getCodTipoDovuto().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE) && dovuto.getMygovDatiMarcaBolloDigitaleId() != null)
			datiMarcaBolloDigitaleService.remove(dovuto.getMygovDatiMarcaBolloDigitaleId());
		return Optional.of(dovutoElaborato);
	}

	public Carrello buildFakeCart(CtReceipt ctReceipt, Dovuto dovuto, Optional<CarrelloMultiBeneficiario> multiCart) {
		Carrello cart = new Carrello();
		Date now = new Date();
		//manage applicationDate, paymentDateTime, transferDate when null
		//`paymentDateTime` : payment execution date by the user
		//`applicationDate` : application date, payment date on the PSP side
		//`transferDate` : transfer date
		multiCart.ifPresent(cart::setMygovCarrelloMultiBeneficiarioId);
		multiCart.ifPresent(item -> cart.setIdSession(item.getIdSessionCarrello()));

		Date paymentDateTime = Optional.ofNullable(ctReceipt.getPaymentDateTime()).map(d -> d.toGregorianCalendar().getTime()).orElse(now);
		Date applicationDate = Optional.ofNullable(ctReceipt.getApplicationDate()).map(d -> d.toGregorianCalendar().getTime()).orElse(paymentDateTime);
		Date transferDate = Optional.ofNullable(ctReceipt.getTransferDate()).map(d -> d.toGregorianCalendar().getTime()).orElse(null);

		cart.setVersion(0);
		cart.setDtCreazione(now);
		cart.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(STATO_CARRELLO_PAGATO, STATO_TIPO_CARRELLO));
		cart.setDtUltimaModificaRp(now);
		cart.setDtUltimaModificaE(now);
		cart.setCodRpSilinviarpIdPsp(ctReceipt.getIdPSP());
		cart.setCodRpSilinviarpIdDominio(dovuto.getNestedEnte().getCodiceFiscaleEnte());
		cart.setCodRpSilinviarpIdUnivocoVersamento(ctReceipt.getCreditorReferenceId());
		cart.setCodRpSilinviarpCodiceContestoPagamento(ctReceipt.getReceiptId());
		cart.setDeRpSilinviarpEsito(ESITO.OK.toString());
		cart.setCodRpDomIdDominio(dovuto.getNestedEnte().getCodiceFiscaleEnte());
		cart.setCodRpIdMessaggioRichiesta(ctReceipt.getReceiptId());
		cart.setDtRpDataOraMessaggioRichiesta(paymentDateTime);
		cart.setCodRpAutenticazioneSoggetto(CODICE_AUTENTICAZIONE_SOGGETTO_NA);
		cart.setDtRpDatiVersDataEsecuzionePagamento(transferDate);
		cart.setCodRpDatiVersIdUnivocoVersamento(dovuto.getCodIuv());
		cart.setCodRpDatiVersCodiceContestoPagamento(ctReceipt.getReceiptId());
		cart.setCodESilinviaesitoIdDominio(dovuto.getNestedEnte().getCodiceFiscaleEnte());
		cart.setCodESilinviaesitoIdUnivocoVersamento(ctReceipt.getCreditorReferenceId());
		cart.setCodESilinviaesitoCodiceContestoPagamento(ctReceipt.getReceiptId());
		cart.setDeESilinviaesitoEsito(ESITO.OK.toString());
		cart.setDeEVersioneOggetto(deRpVersioneOggetto);
		cart.setCodEDomIdDominio(dovuto.getNestedEnte().getCodiceFiscaleEnte());
		cart.setCodEIdMessaggioRicevuta(ctReceipt.getReceiptId());
		cart.setCodEDataOraMessaggioRicevuta(paymentDateTime);
		cart.setCodERiferimentoMessaggioRichiesta(cart.getCodRpIdMessaggioRichiesta());
		cart.setCodERiferimentoDataRichiesta(applicationDate);
		cart.setCodEIstitAttIdUnivAttTipoIdUnivoco(StTipoIdentificativoUnivoco.B.value().charAt(0));
		cart.setCodEIstitAttIdUnivAttCodiceIdUnivoco(ctReceipt.getIdPSP());
		cart.setCodEEnteBenefIdUnivBenefTipoIdUnivoco(TIPOIDENTIFICATIVOUNIVOCO_G.charAt(0));
		cart.setCodEEnteBenefIdUnivBenefCodiceIdUnivoco(dovuto.getNestedEnte().getCodiceFiscaleEnte());
		cart.setDeEEnteBenefDenominazioneBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefDenominazioneBeneficiario());
		cart.setDeEEnteBenefIndirizzoBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefIndirizzoBeneficiario());
		cart.setDeEEnteBenefCivicoBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefCivicoBeneficiario());
		cart.setCodEEnteBenefCapBeneficiario(dovuto.getNestedEnte().getCodRpEnteBenefCapBeneficiario());
		cart.setDeEEnteBenefLocalitaBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefLocalitaBeneficiario());
		cart.setDeEEnteBenefProvinciaBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefProvinciaBeneficiario());
		cart.setCodEEnteBenefNazioneBeneficiario(dovuto.getNestedEnte().getCodRpEnteBenefNazioneBeneficiario());
		cart.setCodEDatiPagCodiceEsitoPagamento(CODICE_ESITO_PAGAMENTO_ESEGUITO.charAt(0));
		cart.setNumEDatiPagImportoTotalePagato(ctReceipt.getPaymentAmount());
		var modelloPagamento = StringUtils.isNotBlank(ctReceipt.getPaymentNote())? MODELLO_PAGAMENTO_1 : MODELLO_PAGAMENTO_4;
		cart.setModelloPagamento(Integer.valueOf(modelloPagamento));

		cart.setFlgNotificaEsito(false);
		cart.setDeRpVersioneOggetto(deRpVersioneOggetto);
		cart.setNumRpDatiVersImportoTotaleDaVersare(ctReceipt.getPaymentAmount());
		cart.setDeEIstitAttDenominazioneAttestante(ctReceipt.getPSPCompanyName());
		cart.setCodEDatiPagIdUnivocoVersamento(ctReceipt.getCreditorReferenceId());
		cart.setCodEDatiPagCodiceContestoPagamento(ctReceipt.getReceiptId());
		cart.setTipoCarrello(TIPO_CARRELLO_PAGAMENTO_ATTIVATO_PRESSO_PSP);

		cart.setDeRpSoggPagEmailPagatore(dovuto.getDeRpSoggPagEmailPagatore());
		cart.setDeRpSoggVersEmailVersante(dovuto.getDeRpSoggPagEmailPagatore());
		cart.setDeRpSoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
		cart.setDeRpSoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
		cart.setCodRpSoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore());
		cart.setDeRpSoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore());
		cart.setDeRpSoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore());
		cart.setCodRpSoggPagNazionePagatore(dovuto.getCodRpSoggPagNazionePagatore());
		cart.setCodRpDatiVersTipoVersamento(dovuto.getCodRpDatiVersTipoVersamento());
		cart.setCodRpSoggPagIdUnivPagTipoIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco());
		cart.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
		cart.setDeRpSoggPagAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore());
		if(ctReceipt.getDebtor()!=null) {
			cart.setCodESoggPagIdUnivPagCodiceIdUnivoco(ctReceipt.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue());
			cart.setCodESoggPagIdUnivPagTipoIdUnivoco(ctReceipt.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierType().value().charAt(0));
			cart.setCodESoggPagAnagraficaPagatore(ctReceipt.getDebtor().getFullName());
			cart.setDeESoggPagIndirizzoPagatore(ctReceipt.getDebtor().getStreetName());
			cart.setDeESoggPagCivicoPagatore(ctReceipt.getDebtor().getCivicNumber());
			cart.setCodESoggPagCapPagatore(ctReceipt.getDebtor().getPostalCode());
			cart.setDeESoggPagLocalitaPagatore(ctReceipt.getDebtor().getCity());
			cart.setDeESoggPagProvinciaPagatore(ctReceipt.getDebtor().getStateProvinceRegion());
			cart.setDeESoggPagEmailPagatore(ctReceipt.getDebtor().getEMail());
		}
		if (ctReceipt.getPayer()!=null) {
			cart.setCodESoggVersIdUnivVersCodiceIdUnivoco(ctReceipt.getPayer().getUniqueIdentifier().getEntityUniqueIdentifierValue());
			cart.setCodESoggVersIdUnivVersTipoIdUnivoco(ctReceipt.getPayer().getUniqueIdentifier().getEntityUniqueIdentifierType().value().charAt(0));
			cart.setCodESoggVersAnagraficaVersante(ctReceipt.getPayer().getFullName());
			cart.setDeESoggVersIndirizzoVersante(ctReceipt.getPayer().getStreetName());
			cart.setDeESoggVersCivicoVersante(ctReceipt.getPayer().getCivicNumber());
			cart.setCodESoggVersCapVersante(ctReceipt.getPayer().getPostalCode());
			cart.setDeESoggVersLocalitaVersante(ctReceipt.getPayer().getCity());
			cart.setDeESoggVersProvinciaVersante(ctReceipt.getPayer().getStateProvinceRegion());
			cart.setDeESoggVersEmailVersante(ctReceipt.getPayer().getEMail());
		}
		return cart;
	}

	private DovutoElaborato buildDovutoElaborato(CtReceipt ctReceipt, byte[] nodeReceiptBytes, Dovuto dovuto, Carrello carrello, int index) {
		var transfer = ctReceipt.getTransferList().getTransfers().stream().filter(item -> Objects.equals(item.getIdTransfer(), index)).findFirst().get();
		var now = new Date();
		var builder = DovutoElaborato.builder()
			.mygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(STATO_DOVUTO_COMPLETATO, STATO_TIPO_DOVUTO))
			.flgDovutoAttuale(dovuto.isFlgDovutoAttuale())
			.mygovFlussoId(dovuto.getMygovFlussoId())
			.numRigaFlusso(dovuto.getNumRigaFlusso())
			.mygovCarrelloId(carrello)
			.codIud(dovuto.getCodIud())
			.codIuv(dovuto.getCodIuv())
			.dtCreazione(now)
			.dtCreazione(now)
			.codIuv(ctReceipt.getCreditorReferenceId())
			.dtUltimaModificaRp(now)
			.dtUltimaModificaE(now)
			.codRpSoggPagIdUnivPagTipoIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco())
			.codRpSoggPagIdUnivPagCodiceIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco())
			.deRpSoggPagAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore())
			.deRpSoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore())
			.deRpSoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore())
			.codRpSoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore())
			.deRpSoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore())
			.deRpSoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore())
			.codRpSoggPagNazionePagatore(dovuto.getCodRpSoggPagNazionePagatore())
			.deRpSoggPagEmailPagatore(dovuto.getDeRpSoggPagEmailPagatore())
			.dtRpDatiVersDataEsecuzionePagamento(dovuto.getDtRpDatiVersDataEsecuzionePagamento())
			.numRpDatiVersDatiSingVersImportoSingoloVersamento(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento())
			.numRpDatiVersDatiSingVersCommissioneCaricoPa(dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa())
			.codTipoDovuto(dovuto.getCodTipoDovuto())
			.deRpDatiVersDatiSingVersCausaleVersamento(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento())
			.deRpDatiVersDatiSingVersDatiSpecificiRiscossione(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione())
			.codTipoDovuto(dovuto.getCodTipoDovuto())
			.codAckRp(carrello.getCodAckRp())
			.dtUltimaModificaRp(carrello.getDtUltimaModificaRp())
			.dtUltimaModificaE(carrello.getDtUltimaModificaE())
			.codRpSilinviarpIdPsp(carrello.getCodRpSilinviarpIdPsp())
			.codRpSilinviarpIdIntermediarioPsp(carrello.getCodRpSilinviarpIdIntermediarioPsp())
			.codRpSilinviarpIdCanale(carrello.getCodRpSilinviarpIdCanale())
			.codRpSilinviarpIdDominio(carrello.getCodRpSilinviarpIdDominio())
			.codRpSilinviarpIdUnivocoVersamento(carrello.getCodRpSilinviarpIdUnivocoVersamento())
			.codRpSilinviarpCodiceContestoPagamento(carrello.getCodRpSilinviarpCodiceContestoPagamento())
			.deRpSilinviarpEsito(carrello.getDeRpSilinviarpEsito())
			.codRpSilinviarpRedirect(carrello.getCodRpSilinviarpRedirect())
			.codRpSilinviarpUrl(carrello.getCodRpSilinviarpUrl())
			.codRpSilinviarpFaultCode(carrello.getCodRpSilinviarpFaultCode())
			.deRpSilinviarpFaultString(carrello.getDeRpSilinviarpFaultString())
			.codRpSilinviarpId(carrello.getCodRpSilinviarpId())
			.codRpSilinviarpOriginalFaultCode(carrello.getCodRpSilinviarpOriginalFaultCode())
			.deRpSilinviarpOriginalFaultString(carrello.getDeRpSilinviarpOriginalFaultString())
			.codRpSilinviarpSerial(carrello.getCodRpSilinviarpSerial())
			.deRpVersioneOggetto(carrello.getDeRpVersioneOggetto())
			.codRpDomIdDominio(carrello.getCodRpDomIdDominio())
			.codRpDomIdStazioneRichiedente(carrello.getCodRpDomIdStazioneRichiedente())
			.codRpIdMessaggioRichiesta(carrello.getCodRpIdMessaggioRichiesta())
			.dtRpDataOraMessaggioRichiesta(carrello.getDtRpDataOraMessaggioRichiesta())
			.codRpAutenticazioneSoggetto(carrello.getCodRpAutenticazioneSoggetto())
			.codRpSoggVersIdUnivVersTipoIdUnivoco(carrello.getCodRpSoggVersIdUnivVersTipoIdUnivoco())
			.codRpSoggVersIdUnivVersCodiceIdUnivoco(carrello.getCodRpSoggVersIdUnivVersCodiceIdUnivoco())
			.codRpSoggVersAnagraficaVersante(carrello.getCodRpSoggVersAnagraficaVersante())
			.deRpSoggVersIndirizzoVersante(carrello.getDeRpSoggVersIndirizzoVersante())
			.deRpSoggVersCivicoVersante(carrello.getDeRpSoggVersCivicoVersante())
			.codRpSoggVersCapVersante(carrello.getCodRpSoggVersCapVersante())
			.deRpSoggVersLocalitaVersante(carrello.getDeRpSoggVersLocalitaVersante())
			.deRpSoggVersProvinciaVersante(carrello.getDeRpSoggVersProvinciaVersante())
			.codRpSoggVersNazioneVersante(carrello.getCodRpSoggVersNazioneVersante())
			.deRpSoggVersEmailVersante(carrello.getDeRpSoggVersEmailVersante())
			.codRpSoggPagIdUnivPagTipoIdUnivoco(carrello.getCodRpSoggPagIdUnivPagTipoIdUnivoco())
			.codRpSoggPagIdUnivPagCodiceIdUnivoco(carrello.getCodRpSoggPagIdUnivPagCodiceIdUnivoco())
			.deRpSoggPagAnagraficaPagatore(carrello.getDeRpSoggPagAnagraficaPagatore())
			.deRpSoggPagIndirizzoPagatore(carrello.getDeRpSoggPagIndirizzoPagatore())
			.deRpSoggPagCivicoPagatore(carrello.getDeRpSoggPagCivicoPagatore())
			.codRpSoggPagCapPagatore(carrello.getCodRpSoggPagCapPagatore())
			.deRpSoggPagLocalitaPagatore(carrello.getDeRpSoggPagLocalitaPagatore())
			.deRpSoggPagProvinciaPagatore(carrello.getDeRpSoggPagProvinciaPagatore())
			.codRpSoggPagNazionePagatore(carrello.getCodRpSoggPagNazionePagatore())
			.deRpSoggPagEmailPagatore(carrello.getDeRpSoggPagEmailPagatore())
			.numRpDatiVersImportoTotaleDaVersare(carrello.getNumRpDatiVersImportoTotaleDaVersare())
			.codRpDatiVersTipoVersamento(carrello.getCodRpDatiVersTipoVersamento())
			.codRpDatiVersIdUnivocoVersamento(carrello.getCodRpDatiVersIdUnivocoVersamento())
			.codRpDatiVersCodiceContestoPagamento(carrello.getCodRpDatiVersCodiceContestoPagamento())
			.deRpDatiVersIbanAddebito(carrello.getDeRpDatiVersIbanAddebito())
			.deRpDatiVersBicAddebito(carrello.getDeRpDatiVersBicAddebito())
			.modelloPagamento(carrello.getModelloPagamento())
			.enteSilInviaRispostaPagamentoUrl(carrello.getEnteSilInviaRispostaPagamentoUrl())
			.codESilinviaesitoIdDominio(carrello.getCodESilinviaesitoIdDominio())
			.codESilinviaesitoIdUnivocoVersamento(carrello.getCodESilinviaesitoIdUnivocoVersamento())
			.codESilinviaesitoCodiceContestoPagamento(carrello.getCodESilinviaesitoCodiceContestoPagamento())
			.deEVersioneOggetto(carrello.getDeEVersioneOggetto())
			.codEDomIdDominio(carrello.getCodEDomIdDominio())
			.codEDomIdStazioneRichiedente(carrello.getCodEDomIdStazioneRichiedente())
			.codEIdMessaggioRicevuta(carrello.getCodEIdMessaggioRicevuta())
			.codEDataOraMessaggioRicevuta(carrello.getCodEDataOraMessaggioRicevuta())
			.codERiferimentoMessaggioRichiesta(carrello.getCodERiferimentoMessaggioRichiesta())
			.codERiferimentoDataRichiesta(carrello.getCodERiferimentoDataRichiesta())
			.codEIstitAttIdUnivAttTipoIdUnivoco(carrello.getCodEIstitAttIdUnivAttTipoIdUnivoco())
			.codEIstitAttIdUnivAttCodiceIdUnivoco(carrello.getCodEIstitAttIdUnivAttCodiceIdUnivoco())
			.deEIstitAttDenominazioneAttestante(carrello.getDeEIstitAttDenominazioneAttestante())
			.codEIstitAttCodiceUnitOperAttestante(carrello.getCodEIstitAttCodiceUnitOperAttestante())
			.deEIstitAttDenomUnitOperAttestante(carrello.getDeEIstitAttDenomUnitOperAttestante())
			.deEIstitAttIndirizzoAttestante(carrello.getDeEIstitAttIndirizzoAttestante())
			.deEIstitAttCivicoAttestante(carrello.getDeEIstitAttCivicoAttestante())
			.codEIstitAttCapAttestante(carrello.getCodEIstitAttCapAttestante())
			.deEIstitAttLocalitaAttestante(carrello.getDeEIstitAttLocalitaAttestante())
			.deEIstitAttProvinciaAttestante(carrello.getDeEIstitAttProvinciaAttestante())
			.codEIstitAttNazioneAttestante(carrello.getCodEIstitAttNazioneAttestante())
			.codEEnteBenefIdUnivBenefTipoIdUnivoco(TIPOIDENTIFICATIVOUNIVOCO_G.charAt(0))
			.codEEnteBenefIdUnivBenefCodiceIdUnivoco(dovuto.getNestedEnte().getCodiceFiscaleEnte())
			.deEEnteBenefDenominazioneBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefDenominazioneBeneficiario())
			.deEEnteBenefIndirizzoBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefIndirizzoBeneficiario())
			.deEEnteBenefCivicoBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefCivicoBeneficiario())
			.codEEnteBenefCapBeneficiario(dovuto.getNestedEnte().getCodRpEnteBenefCapBeneficiario())
			.deEEnteBenefLocalitaBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefLocalitaBeneficiario())
			.deEEnteBenefProvinciaBeneficiario(dovuto.getNestedEnte().getDeRpEnteBenefProvinciaBeneficiario())
			.codEEnteBenefNazioneBeneficiario(dovuto.getNestedEnte().getCodRpEnteBenefNazioneBeneficiario())
			.codESoggVersIdUnivVersTipoIdUnivoco(carrello.getCodESoggVersIdUnivVersTipoIdUnivoco())
			.codESoggVersIdUnivVersCodiceIdUnivoco(carrello.getCodESoggVersIdUnivVersCodiceIdUnivoco())
			.codESoggVersAnagraficaVersante(carrello.getCodESoggVersAnagraficaVersante())
			.deESoggVersIndirizzoVersante(carrello.getDeESoggVersIndirizzoVersante())
			.deESoggVersCivicoVersante(carrello.getDeESoggVersCivicoVersante())
			.codESoggVersCapVersante(carrello.getCodESoggVersCapVersante())
			.deESoggVersLocalitaVersante(carrello.getDeESoggVersLocalitaVersante())
			.deESoggVersProvinciaVersante(carrello.getDeESoggVersProvinciaVersante())
			.codESoggVersNazioneVersante(carrello.getCodESoggVersNazioneVersante())
			.deESoggVersEmailVersante(carrello.getDeESoggVersEmailVersante())
			.codESoggPagIdUnivPagTipoIdUnivoco(carrello.getCodESoggPagIdUnivPagTipoIdUnivoco())
			.codESoggPagIdUnivPagCodiceIdUnivoco(carrello.getCodESoggPagIdUnivPagCodiceIdUnivoco())
			.codESoggPagAnagraficaPagatore(carrello.getCodESoggPagAnagraficaPagatore())
			.deESoggPagIndirizzoPagatore(carrello.getDeESoggPagIndirizzoPagatore())
			.deESoggPagCivicoPagatore(carrello.getDeESoggPagCivicoPagatore())
			.codESoggPagCapPagatore(carrello.getCodESoggPagCapPagatore())
			.deESoggPagLocalitaPagatore(carrello.getDeESoggPagLocalitaPagatore())
			.deESoggPagProvinciaPagatore(carrello.getDeESoggPagProvinciaPagatore())
			.codESoggPagNazionePagatore(carrello.getCodESoggPagNazionePagatore())
			.deESoggPagEmailPagatore(carrello.getDeESoggPagEmailPagatore())
			.codEDatiPagCodiceEsitoPagamento(carrello.getCodEDatiPagCodiceEsitoPagamento())
			.numEDatiPagImportoTotalePagato(carrello.getNumEDatiPagImportoTotalePagato())
			.codEDatiPagIdUnivocoVersamento(carrello.getCodEDatiPagIdUnivocoVersamento())
			.codEDatiPagCodiceContestoPagamento(carrello.getCodEDatiPagCodiceContestoPagamento())
			.indiceDatiSingoloPagamento(transfer.getIdTransfer())
			.numEDatiPagDatiSingPagSingoloImportoPagato(transfer.getTransferAmount())
			.deEDatiPagDatiSingPagEsitoSingoloPagamento(CODICE_ESITO_PAGAMENTO_ESEGUITO)
			.dtEDatiPagDatiSingPagDataEsitoSingoloPagamento(ctReceipt.getPaymentDateTime().toGregorianCalendar().getTime())
			.codEDatiPagDatiSingPagIdUnivocoRiscoss(ctReceipt.getReceiptId())
			.deEDatiPagDatiSingPagCausaleVersamento(transfer.getRemittanceInformation())
			.deEDatiPagDatiSingPagDatiSpecificiRiscossione(Optional.ofNullable(ctReceipt.getPaymentNote())
				.map(s -> dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione())
				.orElse("9/"+ transfer.getTransferCategory() +"/"))
			.numEDatiPagDatiSingPagCommissioniApplicatePsp(ctReceipt.getFee())
			.dtUltimoCambioStato(now)
			.blbRtPayload(nodeReceiptBytes)
			.gpdIupd(dovuto.getGpdIupd())
			.gpdStatus(dovuto.getGpdStatus());
		var description = Utilities.getTruncatedAt(1024).apply(carrello.getDeRpSilinviarpDescription());
		var originalDescription = Utilities.getTruncatedAt(1024).apply(carrello.getDeRpSilinviarpOriginalFaultDescription());
		Optional.ofNullable(description).ifPresent(builder::deRpSilinviarpDescription);
		Optional.ofNullable(originalDescription).ifPresent(builder::deRpSilinviarpOriginalFaultDescription);
		Optional.ofNullable(dovuto.getBilancio()).ifPresent(builder::bilancio);

		if (Objects.nonNull(transfer.getMBDAttachment())) {
			DatiMarcaBolloDigitale bolloDigitale = datiMarcaBolloDigitaleService.getById(dovuto.getMygovDatiMarcaBolloDigitaleId());
			Optional.ofNullable(bolloDigitale.getTipoBollo()).ifPresent(builder::codRpDatiVersDatiSingVersDatiMbdTipoBollo);
			Optional.ofNullable(bolloDigitale.getHashDocumento()).ifPresent(builder::codRpDatiVersDatiSingVersDatiMbdHashDocumento);
			Optional.ofNullable(bolloDigitale.getProvinciaResidenza()).ifPresent(builder::codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza);
			builder.codEDatiPagDatiSingPagAllegatoRicevutaTipo("BD").blbEDatiPagDatiSingPagAllegatoRicevutaTest(transfer.getMBDAttachment());
		}
		Optional.ofNullable(transfer.getIBAN()).ifPresent(builder::codRpDatiVersDatiSingVersIbanAccredito);

		return builder.build();
	}

	private DovutoMultibeneficiario buildDovutoMultibeneficiario(CtTransferPAReceipt ctTransferPA) {
		Optional<Ente> ente = Optional.ofNullable(enteService.getEnteByCodFiscale(ctTransferPA.getFiscalCodePA()));
		return DovutoMultibeneficiario.builder()
			.numRpDatiVersDatiSingVersImportoSingoloVersamento(ctTransferPA.getTransferAmount())
			.codiceFiscaleEnte(ctTransferPA.getFiscalCodePA())
			.codRpDatiVersDatiSingVersIbanAccredito(ctTransferPA.getIBAN())
			.deRpEnteBenefDenominazioneBeneficiario(ente.map(Ente::getDeRpEnteBenefDenominazioneBeneficiario).orElse(null))
// these data cannot be reliably retrieved from Receipt
//			.deRpEnteBenefCivicoBeneficiario(ente.getDeRpEnteBenefCivicoBeneficiario())
//			.deRpEnteBenefIndirizzoBeneficiario(ente.getDeRpEnteBenefIndirizzoBeneficiario())
//			.deRpEnteBenefLocalitaBeneficiario(ente.getDeRpEnteBenefLocalitaBeneficiario())
//			.deRpEnteBenefProvinciaBeneficiario(ente.getDeRpEnteBenefProvinciaBeneficiario())
//			.codRpEnteBenefNazioneBeneficiario(ente.getCodRpEnteBenefNazioneBeneficiario())
//			.codRpEnteBenefCapBeneficiario(ente.getCodRpEnteBenefCapBeneficiario())
			.deRpDatiVersDatiSingVersDatiSpecificiRiscossione(ctTransferPA.getTransferCategory())
			.deRpDatiVersDatiSingVersCausaleVersamento(ctTransferPA.getRemittanceInformation())
			.build();
	}
}
