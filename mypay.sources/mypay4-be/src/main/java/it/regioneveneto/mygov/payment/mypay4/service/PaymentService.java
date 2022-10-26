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

import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaPagatore;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.PaymentOrderException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.pagamenti.pa.PaaTipoDatiPagamentoPA;
import it.veneto.regione.schemas._2012.pagamenti.StTipoVersamento;
import it.veneto.regione.schemas._2012.pagamenti.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.PAA_SYSTEM_ERROR;

@Service
@Slf4j
@Transactional(propagation = Propagation.REQUIRED)
public class PaymentService {

  @Autowired
  TassonomiaService tassonomiaService;
  @Autowired
  DatiMarcaBolloDigitaleService datiMarcaBolloDigitaleService;
  @Autowired
  JAXBTransformService jaxbTransformService;
  @Autowired
  GiornaleService giornaleService;
  @Autowired
  AnagraficaSoggettoService anagraficaSoggettoService;
  @Autowired
  CarrelloService carrelloService;
  @Autowired
  CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;
  @Autowired
  DovutoService dovutoService;
  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;
  @Autowired
  MessageSource messageSource;
  @Autowired
  PagamentiTelematiciRP pagamentiTelematiciRPClient;

  @Value("${pa.codIpaEntePredefinito}")
  private String codIpaEnteDefault;
  @Value("${pa.pspDefaultIdentificativoCanale}")
  private String identificativoCanale;
  @Value("${pa.pspDefaultIdentificativoPsp}")
  private String identificativoPsp;
  @Value("${pa.identificativoIntermediarioPA}")
  private String identificativoErogatore;
  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPa;

  public RP createCtRichiestaPagamento(Carrello cart, Ente ente, XMLGregorianCalendar currentTime, AnagraficaPagatore anagraficaPagatore, AnagraficaPagatore anagraficaVersante, PaaTipoDatiPagamentoPA paaTipoDatiPagamentoPA) {
    log.debug("rp for Ente [" + ente.getCodiceFiscaleEnte() + "] and IUV ["
        + cart.getCodRpDatiVersIdUnivocoVersamento() + "] process START");

    RP rp = new RP();
    rp.setVersioneOggetto(cart.getDeRpVersioneOggetto());
    CtDominio ctDominio = new CtDominio();
    Optional.ofNullable(ente.getCodiceFiscaleEnte()).ifPresent(ctDominio::setIdentificativoDominio);
    rp.setDominio(ctDominio);
    rp.setIdentificativoMessaggioRichiesta(Utilities.getRandomIdMessaggioRichiesta());
    rp.setDataOraMessaggioRichiesta(currentTime);

    if(cart.getTipoCarrello().equals(Constants.TIPO_CARRELLO_DEFAULT_CITTADINO)) {
      rp.setAutenticazioneSoggetto(StAutenticazioneSoggetto.USR);
    } else if(cart.getTipoCarrello().equals(Constants.TIPO_CARRELLO_PAGAMENTO_ATTIVATO_PRESSO_PSP)){
      rp.setAutenticazioneSoggetto(StAutenticazioneSoggetto.N_A);
    } else
      rp.setAutenticazioneSoggetto(StAutenticazioneSoggetto.OTH);

    anagraficaSoggettoService.mapAnagraficaSoggetto(anagraficaPagatore, CtSoggettoPagatore.class).ifPresent(rp::setSoggettoPagatore);
    anagraficaSoggettoService.mapAnagraficaSoggetto(anagraficaVersante, CtSoggettoVersante.class).ifPresent(rp::setSoggettoVersante);

    List<Dovuto> tuttiDovutiInCarrello = dovutoService.getDovutiInCarrello(cart.getMygovCarrelloId());
    List<CtDatiSingoloVersamentoRP> ctDatiSingoloVersamentoRPList = new ArrayList<>();

    BigDecimal total = BigDecimal.ZERO;
    for (Dovuto dovuto : tuttiDovutiInCarrello) {
      CtDatiSingoloVersamentoRP ctDatiSingoloVersamentoRP = new CtDatiSingoloVersamentoRP();

      BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento = dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento();
      total = total.add(numRpDatiVersDatiSingVersImportoSingoloVersamento);
      ctDatiSingoloVersamentoRP.setImportoSingoloVersamento(numRpDatiVersDatiSingVersImportoSingoloVersamento);

      if ((dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa() != null)
          && (dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa().compareTo(new BigDecimal(0)) != 0)) {
        ctDatiSingoloVersamentoRP.setCommissioneCaricoPA(dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa());
      } else if ((ente.getNumRpDatiVersDatiSingVersCommissioneCaricoPa() != null)
          && (ente.getNumRpDatiVersDatiSingVersCommissioneCaricoPa().compareTo(new BigDecimal(0)) != 0)) {
        ctDatiSingoloVersamentoRP.setCommissioneCaricoPA(ente.getNumRpDatiVersDatiSingVersCommissioneCaricoPa());
      }
      //IMPOSTA COORDINATE BANCARIE IN BASE A TIPO DOVUTO
      EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), ente.getCodIpaEnte(), true)
          .orElseThrow( () -> new PaymentOrderException("Tipo dovuto [" + dovuto.getCodTipoDovuto() + "]" + "non trovato per ente [" + ente.getCodIpaEnte() + "]"));
      // IMPOSTA MARCA DA BOLLO
      if (enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
        DatiMarcaBolloDigitale datiMarcaBolloDigitale = datiMarcaBolloDigitaleService.getById(dovuto.getMygovDatiMarcaBolloDigitaleId());
        CtDatiMarcaBolloDigitale ctDatiMarcaBolloDigitale = new CtDatiMarcaBolloDigitale();
        ctDatiMarcaBolloDigitale.setTipoBollo(datiMarcaBolloDigitale.getTipoBollo());
        ctDatiMarcaBolloDigitale.setHashDocumento(datiMarcaBolloDigitale.getHashDocumento());
        ctDatiMarcaBolloDigitale.setProvinciaResidenza(datiMarcaBolloDigitale.getProvinciaResidenza());
        ctDatiSingoloVersamentoRP.setDatiMarcaBolloDigitale(ctDatiMarcaBolloDigitale);
      } else if (paaTipoDatiPagamentoPA != null) {
        if (StringUtils.isNotBlank(paaTipoDatiPagamentoPA.getIbanAccredito())) {
          ctDatiSingoloVersamentoRP.setIbanAccredito(paaTipoDatiPagamentoPA.getIbanAccredito());
        } else if (StringUtils.isNotBlank(paaTipoDatiPagamentoPA.getBicAccredito())) {
          ctDatiSingoloVersamentoRP.setIbanAccredito(paaTipoDatiPagamentoPA.getBicAccredito());
        }
      } else {
        //ACCREDITO
        String ibanAccreditoPSPTipoDovuto = enteTipoDovuto.getIbanAccreditoPsp();
        if (StringUtils.isNotBlank(ibanAccreditoPSPTipoDovuto)) {
          ctDatiSingoloVersamentoRP.setIbanAccredito(ibanAccreditoPSPTipoDovuto);
        } else if (StringUtils.isNotBlank(ente.getCodRpDatiVersDatiSingVersIbanAccredito())) {
          ctDatiSingoloVersamentoRP.setIbanAccredito(ente.getCodRpDatiVersDatiSingVersIbanAccredito());
        }
        //APPOGGIO
        String ibanAccreditoPiTipoDovuto = enteTipoDovuto.getIbanAccreditoPi();
        if (StringUtils.isNotBlank(ibanAccreditoPiTipoDovuto)) {
          ctDatiSingoloVersamentoRP.setIbanAppoggio(ibanAccreditoPiTipoDovuto);
          ctDatiSingoloVersamentoRP.setBicAppoggio(Constants.IDENTIFICATIVO_PSP_POSTE);
        }
      }
      String causaleInseritaNellaRP = StringUtils.isNotBlank(dovuto.getDeCausaleVisualizzata())
          ? dovuto.getDeCausaleVisualizzata(): dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento();
      String causaleVersamento = carrelloService.generateCausaleAgIDFormat(cart.getCodRpDatiVersIdUnivocoVersamento(),
          ctDatiSingoloVersamentoRP.getImportoSingoloVersamento(), causaleInseritaNellaRP);
      ctDatiSingoloVersamentoRP.setCausaleVersamento(causaleVersamento);
      //TODO: tossonomia TEST IT
      String datiSpecificiRiscossioneTemp = Optional.ofNullable(dovuto.getCodIuv())
          .filter(StringUtils::isNotBlank).orElse(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione());
      final String datiSpecificiRiscossione = tassonomiaService.getRightTaxonomicCode(datiSpecificiRiscossioneTemp,
          Optional.ofNullable(enteTipoDovuto.getCodTassonomico())
              .orElseThrow(()->new NotFoundException("codiceTassonomico")));
      ctDatiSingoloVersamentoRP.setDatiSpecificiRiscossione(datiSpecificiRiscossione);
      ctDatiSingoloVersamentoRPList.add(ctDatiSingoloVersamentoRP);
    }
    CtDatiVersamentoRP ctDatiVersamentoRP = new CtDatiVersamentoRP();
    ctDatiVersamentoRP.setDataEsecuzionePagamento(currentTime);
    ctDatiVersamentoRP.setIdentificativoUnivocoVersamento(cart.getCodRpDatiVersIdUnivocoVersamento());
    ctDatiVersamentoRP.setTipoVersamento(StTipoVersamento.fromValue(
        cart.getCodRpDatiVersTipoVersamento().equals(Constants.ALL_PAGAMENTI)?
        Constants.PAY_BONIFICO_BANCARIO_TESORERIA : cart.getCodRpDatiVersTipoVersamento()));
    ctDatiVersamentoRP.setCodiceContestoPagamento(cart.getCodRpDatiVersCodiceContestoPagamento());
    ctDatiVersamentoRP.setImportoTotaleDaVersare(total);
    ctDatiVersamentoRP.getDatiSingoloVersamentos().addAll(ctDatiSingoloVersamentoRPList);
    rp.setDatiVersamento(ctDatiVersamentoRP);
    log.debug("rp for Ente [" + ente.getCodiceFiscaleEnte() + "] and IUV ["
        + cart.getCodRpDatiVersIdUnivocoVersamento() + "] process END");
    return rp;
  }

  public NodoSILInviaRPRisposta inviaRP(Long idCarrello, Map<String, Object> returnHashMap) {
    RP ctRichiestaPagamento = (RP) returnHashMap.get("ctRichiestaPagamento");
    NodoSILInviaRP nodoSILInviaRP = (NodoSILInviaRP) returnHashMap.get("nodoSILInviaRP");
    var intestazionePPT = (it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT) returnHashMap.get("intestazionePPT");
    NodoSILInviaRPRisposta nodoSILInviaRPRisposta = new NodoSILInviaRPRisposta();
    try {
      String xmlRequest = jaxbTransformService.marshalling(ctRichiestaPagamento, RP.class);
      giornaleService.registraEvento(new Date(), ctRichiestaPagamento.getDominio().getIdentificativoDominio(),
          ctRichiestaPagamento.getDatiVersamento().getIdentificativoUnivocoVersamento(),
          ctRichiestaPagamento.getDatiVersamento().getCodiceContestoPagamento(),
          nodoSILInviaRP.getIdentificativoPSP(),
          ctRichiestaPagamento.getDatiVersamento().getTipoVersamento().toString(),
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_PA.nodoSILInviaRP.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
          ctRichiestaPagamento.getDominio().getIdentificativoDominio(),
          identificativoErogatore,
          identificativoStazioneIntermediarioPa,
          nodoSILInviaRP.getIdentificativoCanale(),
          xmlRequest,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString());

      log.debug("Chiamata nodo per nodoSILInviaRP start");
      nodoSILInviaRPRisposta = pagamentiTelematiciRPClient.nodoSILInviaRP(nodoSILInviaRP, intestazionePPT);
      log.debug("Chiamata nodo per nodoSILInviaRP end");

      Carrello preparedCart = carrelloService.getById(idCarrello);
      carrelloService.updateResultCarrelloRp(preparedCart, nodoSILInviaRPRisposta);
      if (!"OK".equals(nodoSILInviaRPRisposta.getEsito()))
        log.error("Ricevuto esito risposta RP: {} - fault: {}", nodoSILInviaRPRisposta.getEsito(), Optional.ofNullable(nodoSILInviaRPRisposta.getFault()).map(FaultBean::getFaultCode).orElse(null));
    } catch (Exception e) {
      log.error("Error due processing nodoSILInviaRP", e);
      nodoSILInviaRPRisposta.setEsito(FaultCodeConstants.ESITO_KO);
      FaultBean faultBean = new FaultBean();
      faultBean.setId("NODO_INVIA_CARRELLO");
      faultBean.setFaultCode(PAA_SYSTEM_ERROR);
      faultBean.setDescription(e.getMessage());
      faultBean.setFaultString(messageSource.getMessage("pa.errore.invioRPT", null, Locale.ITALY));
      nodoSILInviaRPRisposta.setFault(faultBean);
    } finally {
      String xmlEsito = jaxbTransformService.marshalling(nodoSILInviaRPRisposta, NodoSILInviaRPRisposta.class);
      giornaleService.registraEvento(new Date(), ctRichiestaPagamento.getDominio().getIdentificativoDominio(),
          ctRichiestaPagamento.getDatiVersamento().getIdentificativoUnivocoVersamento(),
          ctRichiestaPagamento.getDatiVersamento().getCodiceContestoPagamento(),
          nodoSILInviaRP.getIdentificativoPSP(),
          ctRichiestaPagamento.getDatiVersamento().getTipoVersamento().toString(),
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_PA.nodoSILInviaRP.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
          ctRichiestaPagamento.getDominio().getIdentificativoDominio(),
          identificativoErogatore,
          identificativoStazioneIntermediarioPa,
          nodoSILInviaRP.getIdentificativoCanale(),
          xmlEsito,
          nodoSILInviaRPRisposta.getEsito());
    }
    return nodoSILInviaRPRisposta;
  }

  /*
   * Generate a IUV if not present.
   */
  protected String generateIUV(Carrello cart, Ente ente){
    Optional<String> optionalIUV = Optional.ofNullable(cart.getCodRpDatiVersIdUnivocoVersamento());
    if (optionalIUV.isEmpty()) {
      try {
        String total = Utilities.getStringFromBigDecimalGroup(cart.getNumRpDatiVersImportoTotaleDaVersare());
        String typePayment = cart.getCodRpDatiVersTipoVersamento().equals(Constants.ALL_PAGAMENTI)?
            Constants.PAY_BONIFICO_BANCARIO_TESORERIA : cart.getCodRpDatiVersTipoVersamento();
        return dovutoService.generateIUV(ente, total, Constants.IUV_GENERATOR_25, typePayment);
      } catch (Exception e) {
        log.error("processOrder - Error due processing IUV request", e);
        throw new ValidatorException(messageSource.getMessage("pa.carrello.richiestaIUVError", null, Locale.ITALY));
      }
    }
    return optionalIUV.get();
  }

  protected String generateCCP(Carrello cart) {
    return StringUtils.defaultIfBlank(cart.getCodRpDatiVersCodiceContestoPagamento(),
        cart.getCodRpDatiVersIdUnivocoVersamento().length() == 25 ? "n/a" : Utilities.getRandomicUUID());
  }

  public NodoSILInviaCarrelloRPRisposta inviaCarrelloRP(Long idCarrelloMultiBeneficiario, NodoSILInviaCarrelloRP nc) throws PaymentOrderException{
    NodoSILInviaCarrelloRPRisposta ncr = new NodoSILInviaCarrelloRPRisposta();
    List<ElementoRP> rpList = nc.getListaRP().getElementoRPs();
    try {
      for (ElementoRP elementoRP: rpList) {
        giornaleService.registraEvento(new Date(), elementoRP.getIdentificativoDominio(),
            elementoRP.getIdentificativoUnivocoVersamento(), elementoRP.getCodiceContestoPagamento(),
            identificativoPsp, Constants.ALL_PAGAMENTI, Constants.COMPONENTE.FESP.toString(),
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_PA.nodoSILInviaCarrelloRP.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
            codIpaEnteDefault, identificativoErogatore,
            identificativoStazioneIntermediarioPa, identificativoCanale,
            new String(elementoRP.getRp(), StandardCharsets.UTF_8), Constants.GIORNALE_ESITO_EVENTO.OK.toString());
      }

      log.debug("Chiamata nodo per inviaCarrelloRP start");
      ncr = pagamentiTelematiciRPClient.nodoSILInviaCarrelloRP(nc);
      log.debug("Chiamata nodo per inviaCarrelloRP end");

      CarrelloMultiBeneficiario carrelloMultiBeneficiario = carrelloMultiBeneficiarioService.getById(idCarrelloMultiBeneficiario);
      carrelloMultiBeneficiarioService.updateResultCarrelloRp(carrelloMultiBeneficiario, ncr);
      if (!"OK".equals(ncr.getEsito()))
        log.error("Ricevuto esito risposta RP: {} - fault: {}", ncr.getEsito(), Optional.ofNullable(ncr.getFault()).map(FaultBean::getFaultCode).orElse(null));
    } catch (Exception e) {
      log.error("Error due processing nodoSILInviaCarrelloRP", e);
      ncr.setEsito(FaultCodeConstants.ESITO_KO);
      FaultBean faultBean = new FaultBean();
      faultBean.setId("NODO_INVIA_CARRELLO");
      faultBean.setFaultCode(PAA_SYSTEM_ERROR);
      faultBean.setDescription(e.getMessage());
      faultBean.setFaultString(messageSource.getMessage("pa.errore.invioRPT", null, Locale.ITALY));
      ncr.setFault(faultBean);
    } finally {
      String xmlEsito = jaxbTransformService.marshalling(ncr, NodoSILInviaCarrelloRPRisposta.class);
      for (ElementoRP elementoRP: rpList) {
        giornaleService.registraEvento(new Date(), elementoRP.getIdentificativoDominio(),
            elementoRP.getIdentificativoUnivocoVersamento(), elementoRP.getCodiceContestoPagamento(),
            identificativoPsp, Constants.ALL_PAGAMENTI, Constants.COMPONENTE.PA.toString(),
            Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_PA.nodoSILInviaCarrelloRP.toString(),
            Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
            codIpaEnteDefault, identificativoErogatore,
            identificativoStazioneIntermediarioPa, identificativoCanale,
            xmlEsito, ncr.getEsito());
      }
    }
    return ncr;
  }
}
