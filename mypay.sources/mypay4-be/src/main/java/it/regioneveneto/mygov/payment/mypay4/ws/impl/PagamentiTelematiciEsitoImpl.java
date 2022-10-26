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

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.Giornale;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.PagamentiTelematiciEsito;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp.PagamentiTelematiciRTImpl;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.regioneveneto.mygov.payment.mypay4.ws.util.ManageWsFault;
import it.veneto.regione.pagamenti.pa.EsitoPaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.FaultBean;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsitoRisposta;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
import it.veneto.regione.schemas._2012.pagamenti.Esito;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.function.BiConsumer;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.PAA_XML_NON_VALIDO_CODE;


@Service("PagamentiTelematiciEsitoImpl")
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciEsitoImpl implements PagamentiTelematiciEsito {

  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String propIdStazioneIntermediarioPa;

  @Autowired
  AvvisoDigitaleService avvisoDigitaleService;
  @Autowired
  EnteService enteService;
  @Autowired
  EnteFunzionalitaService enteFunzionalitaService;
  @Autowired
  GiornaleService giornalePaService;
  @Autowired
  CarrelloService carrelloService;
  @Autowired
  JAXBTransformService jaxbTransformService;
  @Autowired
  DovutoElaboratoService dovutoElaboratoService;
  @Autowired
  AnagraficaStatoService anagraficaStatoService;
  @Autowired
  AsynchAvvisiDigitaliService flussoAvvisoDigitaleService;
  @Autowired
  EsitoService esitoService;

  @Override
  public PaaSILInviaEsitoRisposta paaSILInviaEsito(PaaSILInviaEsito bodyrichiesta, IntestazionePPT header) {
    log.info("Executing operation paaSILInviaEsito");
    String identificativoDominio = header.getIdentificativoDominio();
    String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
    String codiceContestoPagamento = header.getCodiceContestoPagamento();
    Giornale dati = Giornale.builder()
        .identificativoDominio(identificativoDominio)
        .identificativoUnivocoVersamento(identificativoUnivocoVersamento)
        .codiceContestoPagamento(codiceContestoPagamento)
        .identificativoPrestatoreServiziPagamento(Constants.EMPTY)
        .tipoVersamento(Constants.EMPTY)
        .componente(Constants.COMPONENTE_FESP)
        .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString())
        .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_PA.paaSILInviaEsito.toString())
        .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
        .identificativoFruitore(identificativoDominio)
        .identificativoErogatore(Constants.EMPTY)
        .identificativoStazioneIntermediarioPa(propIdStazioneIntermediarioPa)
        .canalePagamento(Constants.EMPTY)
        .build();
    BiConsumer<String, String> registraGiornale = (parametriSpecificiInterfaccia, esito) -> {
      try {
        giornalePaService.registraEvento(
            new Date(),
            dati.getIdentificativoDominio(),
            dati.getIdentificativoUnivocoVersamento(),
            dati.getCodiceContestoPagamento(),
            dati.getIdentificativoPrestatoreServiziPagamento(),
            dati.getTipoVersamento(),
            dati.getComponente(),
            dati.getCategoriaEvento(),
            dati.getTipoEvento(),
            dati.getSottoTipoEvento(),
            dati.getIdentificativoFruitore(),
            dati.getIdentificativoErogatore(),
            dati.getIdentificativoStazioneIntermediarioPa(),
            dati.getCanalePagamento(),
            parametriSpecificiInterfaccia,
            esito
        );
      } catch (Exception e) {
        log.warn("paaSILInviaEsito [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
      }
    };
    ManageWsFault<PaaSILInviaEsitoRisposta> manageFault = (codeFault, faultString, faultDescr, error) -> {
      log.error(faultString, error);
      registraGiornale.accept(faultString + (StringUtils.isBlank(faultDescr)?"":(" - "+faultDescr)), Constants.GIORNALE_ESITO_EVENTO.KO.toString());
      PaaSILInviaEsitoRisposta paaSILInviaEsitoRisposta = new PaaSILInviaEsitoRisposta();
      EsitoPaaSILInviaEsito esitoPaaSILInviaEsito = new EsitoPaaSILInviaEsito();
      FaultBean faultBean = VerificationUtils.getPaFaultBean(header.getIdentificativoDominio(),
          codeFault, faultString, faultDescr,1);
      esitoPaaSILInviaEsito.setEsito(Constants.GIORNALE_ESITO_EVENTO.KO.toString());
      esitoPaaSILInviaEsito.setFault(faultBean);
      paaSILInviaEsitoRisposta.setPaaSILInviaEsitoRisposta(esitoPaaSILInviaEsito);
      return paaSILInviaEsitoRisposta;
    };

    try {
      String xmlString = jaxbTransformService.marshalling(bodyrichiesta, PaaSILInviaEsito.class);
      registraGiornale.accept(xmlString, Constants.GIORNALE_ESITO_EVENTO.OK.toString());
    } catch (Exception ex) {
      return manageFault.apply(identificativoDominio, FaultCodeConstants.CODE_PAA_SYSTEM_ERROR,
          "Error executing operation paaSILInviaEsito", ex);
    }
    Esito ctEsito;
    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());

    try {
      ctEsito = jaxbTransformService.unmarshalling(bodyrichiesta.getEsito(), Esito.class, "/wsdl/pa/PagInf_RP_Esito_6_2_0.xsd");
    } catch (MyPayException e) {
      //ugly workaround: in case we are processing a fake RT ("forza RT negativa"), ignore the unmarshalling error
      // because it may be the same error causing RPT to be rejected, and try use the fake esito when it is generated
      if(PagamentiTelematiciRTImpl.getFakeCtEsito()!=null){
        log.info("error unmarshalling ctEsito but using fakeCtEsito instead", e);
        ctEsito = PagamentiTelematiciRTImpl.getFakeCtEsito();
      } else {
        String buffer = "XML ricevuto per PaaSILInviaEsito non conforme all' XSD per ente [" + identificativoDominio + "]" +
          "XML Error: \n" + e.getMessage();
        return manageFault.apply(PAA_XML_NON_VALIDO_CODE, "XML Esito non valido", buffer,e);
      }
    }

    Carrello cart = carrelloService.getByIdMessaggioRichiesta(ctEsito.getRiferimentoMessaggioRichiesta());
    String codAnagraficaStato = cart.getMygovAnagraficaStatoId().getCodStato();
    dati.setIdentificativoPrestatoreServiziPagamento(cart.getCodRpSilinviarpIdPsp());
    dati.setCanalePagamento(cart.getCodRpSilinviarpIdCanale());
    //TODO: revoca/annullo tecnico eliminato! vedi mypay3 v5.9 da riga 270
    if (!codAnagraficaStato.equals(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO)) {
      String message = "ESITO RIFIUTATO: Carrello per IUV" + ctEsito.getDatiPagamento().getIdentificativoUnivocoVersamento() + " gia in stato +"
          + cart.getMygovAnagraficaStatoId().getDeStato();
      return manageFault.apply(FaultCodeConstants.CODE_PAA_SYSTEM_ERROR, message);
    }

    try {
      log.trace("CARRELLO PRIMA: {}",cart);
      esitoService.elaboraEsito(ctEsito, bodyrichiesta.getTipoFirma(), bodyrichiesta.getRt(), header);
      cart = carrelloService.getById(cart.getMygovCarrelloId());
      log.trace("CARRELLO DOPO: {}",cart);
      esitoService.manageAvvisoDigitale(identificativoDominio, ctEsito, cart);
      esitoService.sendEmailEsito(cart);
    }
    catch (Exception ex) {
      manageFault.apply(FaultCodeConstants.CODE_PAA_SYSTEM_ERROR, FaultCodeConstants.DESC_PAA_SYSTEM_ERROR,"Error elaborazione esito",ex);
    }
    PaaSILInviaEsitoRisposta response = new PaaSILInviaEsitoRisposta();
    EsitoPaaSILInviaEsito esitoPaaSILInviaEsito = new EsitoPaaSILInviaEsito();
    esitoPaaSILInviaEsito.setEsito(Constants.STATO_ESITO_OK);
    response.setPaaSILInviaEsitoRisposta(esitoPaaSILInviaEsito);

    registraGiornale.accept(esitoPaaSILInviaEsito.getEsito(), esitoPaaSILInviaEsito.getEsito());
    return response;
  }



}

