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
package it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.*;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Giornale;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciRPTClient;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service("PagamentiTelematiciRPTImpl")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciRPTImpl {

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String propIdStazioneIntermediarioPa;
  @Value("${pa.codIpaEntePredefinito}")
  private String codIpaEnteDefault;
  @Value("${pa.pspDefaultIdentificativoCanale}")
  private String identificativoCanale;
  @Value("${pa.pspDefaultIdentificativoPsp}")
  private String identificativoPsp;
  @Value("${pa.identificativoIntermediarioPA}")
  private String identificativoErogatore;

  @Autowired
  private GiornaleService giornaleFespService;

  @Autowired
  private JAXBTransformService jaxbTransformService;

  @Autowired
  private PagamentiTelematiciRPTClient pagamentiTelematiciRPTClient;

  public NodoInviaRPTRisposta nodoInviaRPT(Long mygovRptRtId, NodoInviaRPT body, IntestazionePPT header){

    Giornale dati = Giornale.builder()
        .identificativoDominio(header.getIdentificativoDominio())
        .identificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
        .codiceContestoPagamento(header.getCodiceContestoPagamento())
        .identificativoPrestatoreServiziPagamento(body.getIdentificativoPSP())
        .tipoVersamento(Constants.EMPTY)
        .componente(Constants.COMPONENTE_FESP)
        .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString())
        .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaRPT.toString())
        .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString().toString())
        .identificativoFruitore(propIdStazioneIntermediarioPa)
        .identificativoErogatore(Constants.NODO_DEI_PAGAMENTI_SPC)
        .identificativoStazioneIntermediarioPa(propIdStazioneIntermediarioPa)
        .canalePagamento(body.getIdentificativoCanale())
        .build();

    try {
      String xmlString = jaxbTransformService.marshalling(body, NodoInviaRPT.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoInviaRPT [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    NodoInviaRPTRisposta nodoInviaRPTRisposta = pagamentiTelematiciRPTClient.nodoInviaRPT(body, header);

    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    try {
      String xmlString = jaxbTransformService.marshalling(nodoInviaRPTRisposta, NodoInviaRPTRisposta.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoInviaRPT [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    return nodoInviaRPTRisposta;
  }

  public NodoInviaCarrelloRPTRisposta nodoInviaCarrelloRPT(IntestazioneCarrelloPPT header, NodoInviaCarrelloRPT body) {
    NodoInviaCarrelloRPTRisposta response = new NodoInviaCarrelloRPTRisposta();
    List<TipoElementoListaRPT> rptList = body.getListaRPT().getElementoListaRPTs();
    try {
      rptList.forEach(element -> {
        try {
          String xmlString = new String(element.getRpt(), StandardCharsets.UTF_8);
          giornaleFespService.registraEvento(new Date(), element.getIdentificativoDominio(),
              element.getIdentificativoUnivocoVersamento(), element.getCodiceContestoPagamento(),
              identificativoPsp, Constants.ALL_PAGAMENTI, Constants.COMPONENTE.FESP.toString(),
              Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaCarrelloRPT.toString(),
              Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
              codIpaEnteDefault, identificativoErogatore,
              propIdStazioneIntermediarioPa, identificativoCanale,
              xmlString, Constants.GIORNALE_ESITO_EVENTO.OK.toString());
        } catch (Exception e) {
          log.warn("body REQUEST impossible to insert in the event log", e);
        }
      });
      log.debug("PRIMA DI CHIAMATA AL WS ESTERNO");
      response = pagamentiTelematiciRPTClient.nodoInviaCarrelloRPT(body, header);

    } catch (Exception e) {
      log.error("Failed to initialize FESP client", e);
      response = new NodoInviaCarrelloRPTRisposta();
      response.setEsitoComplessivoOperazione(FaultCodeConstants.ESITO_KO);
      gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean faultRPT = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean();
      faultRPT.setFaultCode(Constants.STATO_EVTIPO_ERROR_NODO_FESP);
      faultRPT.setFaultString("Failed to initialize FESP client");
      ListaErroriRPT listaErroriRPT = new ListaErroriRPT();
      listaErroriRPT.getFaults().addAll(Collections.singletonList(faultRPT));
      response.setListaErroriRPT(listaErroriRPT);
    } finally {
      String xmlEsito = jaxbTransformService.marshalling(response, NodoInviaCarrelloRPTRisposta.class);
      NodoInviaCarrelloRPTRisposta finalResponse = response;
      rptList.forEach(element -> {
        try {
          giornaleFespService.registraEvento(new Date(), element.getIdentificativoDominio(),
              element.getIdentificativoUnivocoVersamento(), element.getCodiceContestoPagamento(),
              identificativoPsp, Constants.ALL_PAGAMENTI, Constants.COMPONENTE.FESP.toString(),
              Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaCarrelloRPT.toString(),
              Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
              codIpaEnteDefault, identificativoErogatore,
              propIdStazioneIntermediarioPa, identificativoCanale,
              xmlEsito, finalResponse.getEsitoComplessivoOperazione());
        } catch (Exception e) {
          log.warn("nodoSILInviaCarrelloRP [RESPONSE] impossible to insert in the event log", e);
        }
      });
    }
    return response;
  }

  public NodoChiediQuadraturaPARisposta nodoChiediQuadraturaPA(NodoChiediQuadraturaPA body, IntestazionePPT header) {
    Giornale dati = Giornale.builder()
        .identificativoDominio(body.getIdentificativoDominio())
        .identificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
        .codiceContestoPagamento(header.getCodiceContestoPagamento())
        .identificativoPrestatoreServiziPagamento(identificativoPsp)
        .tipoVersamento(Constants.EMPTY)
        .componente(Constants.COMPONENTE_FESP)
        .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString())
        .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediQuadraturaPA.toString())
        .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
        .identificativoFruitore(propIdStazioneIntermediarioPa)
        .identificativoErogatore(Constants.NODO_DEI_PAGAMENTI_SPC)
        .identificativoStazioneIntermediarioPa(body.getIdentificativoStazioneIntermediarioPA())
        .canalePagamento(identificativoCanale)
        .build();

    try {
      String xmlString = jaxbTransformService.marshalling(body, NodoChiediQuadraturaPA.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediQuadraturaPA [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    NodoChiediQuadraturaPARisposta nodoChiediQuadraturaPARisposta = pagamentiTelematiciRPTClient.nodoChiediQuadraturaPA(body);

    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    try {
      String xmlString = jaxbTransformService.marshalling(nodoChiediQuadraturaPARisposta, NodoChiediQuadraturaPARisposta.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediQuadraturaPA [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    return nodoChiediQuadraturaPARisposta;
  }

  public NodoChiediElencoFlussiRendicontazioneRisposta nodoChiediElencoFlussiRendicontazione(NodoChiediElencoFlussiRendicontazione body, IntestazionePPT header) {

    Giornale dati = Giornale.builder()
        .identificativoDominio(body.getIdentificativoDominio())
        .identificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
        .codiceContestoPagamento(header.getCodiceContestoPagamento())
        .identificativoPrestatoreServiziPagamento(body.getIdentificativoPSP())
        .tipoVersamento(Constants.EMPTY)
        .componente(Constants.COMPONENTE_FESP)
        .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString())
        .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediElencoFlussiRendicontazione.toString())
        .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
        .identificativoFruitore(propIdStazioneIntermediarioPa)
        .identificativoErogatore(Constants.NODO_DEI_PAGAMENTI_SPC)
        .identificativoStazioneIntermediarioPa(body.getIdentificativoStazioneIntermediarioPA())
        .canalePagamento(identificativoCanale)
        .build();

    try {
      String xmlString = jaxbTransformService.marshalling(body, NodoChiediElencoFlussiRendicontazione.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediElencoFlussiRendicontazione [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    NodoChiediElencoFlussiRendicontazioneRisposta nodoChiediElencoFlussiRendicontazioneRisposta = pagamentiTelematiciRPTClient.nodoChiediElencoFlussiRendicontazione(body);

    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    try {
      String xmlString = jaxbTransformService.marshalling(nodoChiediElencoFlussiRendicontazioneRisposta, NodoChiediElencoFlussiRendicontazioneRisposta.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediElencoFlussiRendicontazione [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }
    return nodoChiediElencoFlussiRendicontazioneRisposta;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.NOT_SUPPORTED)
  public NodoChiediCopiaRTRisposta nodoChiediCopiaRT(NodoChiediCopiaRT body) {

    Giornale dati = Giornale.builder()
        .identificativoDominio(body.getIdentificativoDominio())
        .identificativoUnivocoVersamento(body.getIdentificativoUnivocoVersamento())
        .codiceContestoPagamento(body.getCodiceContestoPagamento())
        .identificativoPrestatoreServiziPagamento(identificativoPsp)
        .tipoVersamento(Constants.EMPTY)
        .componente(Constants.COMPONENTE_FESP)
        .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString())
        .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediCopiaRT.toString())
        .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
        .identificativoFruitore(propIdStazioneIntermediarioPa)
        .identificativoErogatore(Constants.NODO_DEI_PAGAMENTI_SPC)
        .identificativoStazioneIntermediarioPa(body.getIdentificativoStazioneIntermediarioPA())
        .canalePagamento(identificativoCanale)
        .build();

    try {
      String xmlString = jaxbTransformService.marshalling(body, NodoChiediCopiaRT.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediCopiaRT [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    NodoChiediCopiaRTRisposta nodoChiediCopiaRTRisposta = pagamentiTelematiciRPTClient.nodoChiediCopiaRT(body);

    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    try {
      String xmlString = jaxbTransformService.marshalling(nodoChiediCopiaRTRisposta, NodoChiediCopiaRTRisposta.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediCopiaRT [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    return nodoChiediCopiaRTRisposta;
  }

  public NodoChiediListaPendentiRPTRisposta nodoChiediListaPendentiRPT(NodoChiediListaPendentiRPT body, IntestazionePPT header) {

    Giornale dati = Giornale.builder()
        .identificativoDominio(body.getIdentificativoDominio())
        .identificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
        .codiceContestoPagamento(header.getCodiceContestoPagamento())
        .identificativoPrestatoreServiziPagamento(identificativoPsp)
        .tipoVersamento(Constants.EMPTY)
        .componente(Constants.COMPONENTE_FESP)
        .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString())
        .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediListaPendentiRPT.toString())
        .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
        .identificativoFruitore(propIdStazioneIntermediarioPa)
        .identificativoErogatore(Constants.NODO_DEI_PAGAMENTI_SPC)
        .identificativoStazioneIntermediarioPa(body.getIdentificativoStazioneIntermediarioPA())
        .canalePagamento(identificativoCanale)
        .build();

    try {
      String xmlString = jaxbTransformService.marshalling(body, NodoChiediListaPendentiRPT.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediListaPendentiRPT [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    NodoChiediListaPendentiRPTRisposta nodoChiediListaPendentiRPTRisposta = pagamentiTelematiciRPTClient.nodoChiediListaPendentiRPT(body);

    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    try {
      String xmlString = jaxbTransformService.marshalling(nodoChiediListaPendentiRPTRisposta, NodoChiediListaPendentiRPTRisposta.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediListaPendentiRPT [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    return nodoChiediListaPendentiRPTRisposta;
  }

  public NodoChiediStatoRPTRisposta nodoChiediStatoRPT(NodoChiediStatoRPT body) {

    Giornale dati = Giornale.builder()
        .identificativoDominio(body.getIdentificativoDominio())
        .identificativoUnivocoVersamento(body.getIdentificativoUnivocoVersamento())
        .codiceContestoPagamento(body.getCodiceContestoPagamento())
        .identificativoPrestatoreServiziPagamento(identificativoPsp)
        .tipoVersamento(Constants.EMPTY)
        .componente(Constants.COMPONENTE_FESP)
        .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString())
        .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediStatoRPT.toString())
        .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
        .identificativoFruitore(propIdStazioneIntermediarioPa)
        .identificativoErogatore(Constants.NODO_DEI_PAGAMENTI_SPC)
        .identificativoStazioneIntermediarioPa(body.getIdentificativoStazioneIntermediarioPA())
        .canalePagamento(identificativoCanale)
        .build();

    try {
      String xmlString = jaxbTransformService.marshalling(body, NodoChiediStatoRPT.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediStatoRPT [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    NodoChiediStatoRPTRisposta nodoChiediStatoRPTRisposta = pagamentiTelematiciRPTClient.nodoChiediStatoRPT(body);

    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    try {
      String xmlString = jaxbTransformService.marshalling(nodoChiediStatoRPTRisposta, NodoChiediStatoRPTRisposta.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediStatoRPT [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    return nodoChiediStatoRPTRisposta;
  }

  public NodoChiediFlussoRendicontazioneRisposta nodoChiediFlussoRendicontazione(NodoChiediFlussoRendicontazione body, IntestazionePPT header) {

    Giornale dati = Giornale.builder()
        .identificativoDominio(body.getIdentificativoDominio())
        .identificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
        .codiceContestoPagamento(header.getCodiceContestoPagamento())
        .identificativoPrestatoreServiziPagamento(body.getIdentificativoPSP())
        .tipoVersamento(Constants.EMPTY)
        .componente(Constants.COMPONENTE_FESP)
        .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString())
        .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediFlussoRendicontazione.toString())
        .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
        .identificativoFruitore(propIdStazioneIntermediarioPa)
        .identificativoErogatore(Constants.NODO_DEI_PAGAMENTI_SPC)
        .identificativoStazioneIntermediarioPa(body.getIdentificativoStazioneIntermediarioPA())
        .canalePagamento(identificativoCanale)
        .build();

    try {
      String xmlString = jaxbTransformService.marshalling(body, NodoChiediFlussoRendicontazione.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediFlussoRendicontazione [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    NodoChiediFlussoRendicontazioneRisposta nodoChiediFlussoRendicontazioneRisposta = pagamentiTelematiciRPTClient.nodoChiediFlussoRendicontazione(body);

    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    try {
      String xmlString = jaxbTransformService.marshalling(nodoChiediFlussoRendicontazioneRisposta, NodoChiediFlussoRendicontazioneRisposta.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediFlussoRendicontazione [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    return nodoChiediFlussoRendicontazioneRisposta;
  }

  public NodoChiediElencoQuadraturePARisposta nodoChiediElencoQuadraturePA(NodoChiediElencoQuadraturePA body, IntestazionePPT header) {

    Giornale dati = Giornale.builder()
        .identificativoDominio(body.getIdentificativoDominio())
        .identificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
        .codiceContestoPagamento(header.getCodiceContestoPagamento())
        .identificativoPrestatoreServiziPagamento(identificativoPsp)
        .tipoVersamento(Constants.EMPTY)
        .componente(Constants.COMPONENTE_FESP)
        .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString())
        .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_FESP.nodoChiediElencoQuadraturePA.toString())
        .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
        .identificativoFruitore(propIdStazioneIntermediarioPa)
        .identificativoErogatore(Constants.NODO_DEI_PAGAMENTI_SPC)
        .identificativoStazioneIntermediarioPa(body.getIdentificativoStazioneIntermediarioPA())
        .canalePagamento(identificativoCanale)
        .build();

    try {
      String xmlString = jaxbTransformService.marshalling(body, NodoChiediElencoQuadraturePA.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediElencoQuadraturePA [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    NodoChiediElencoQuadraturePARisposta nodoChiediElencoQuadraturePARisposta = pagamentiTelematiciRPTClient.nodoChiediElencoQuadraturePA(body);

    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    try {
      String xmlString = jaxbTransformService.marshalling(nodoChiediElencoQuadraturePARisposta, NodoChiediElencoQuadraturePARisposta.class);
      giornaleFespService.registraEvento(
          null,
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
          xmlString,
          Constants.GIORNALE_ESITO_EVENTO.OK.toString()
      );
    } catch (Exception e) {
      log.warn("nodoChiediElencoQuadraturePA [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
    }

    return nodoChiediElencoQuadraturePARisposta;
  }
}
