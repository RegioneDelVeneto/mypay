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
package it.regioneveneto.mygov.payment.mypay4.controller.fesp;

import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.*;

@Controller("landing FESP")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@ConditionalOnWebApplication
public class LandingController {

  @Autowired
  private CarrelloRpService carrelloRpService;
  @Autowired
  private CarrelloRptService carrelloRptService;
  @Autowired
  private EnteService enteService;
  @Autowired
  private RpEService rPEService;
  @Autowired
  private RptRtService rptRtService;
  @Autowired
  private it.regioneveneto.mygov.payment.mypay4.controller.LandingController landingControllerModulePa;
  @Autowired
  private GiornaleService giornaleService;

  private static final String MODULE_PATH = "fesp";
  private static final String ANONYMOUS_PATH = MyPay4AbstractSecurityConfig.PATH_PUBLIC + "/" + MODULE_PATH + "/landing";
  public static final String PAYMENT_OUTCOME_URL = ANONYMOUS_PATH + "/outcome";
  private static final String LANG_PARAM_ITA = "lang=ita";

  //@Value("${app.be.absolute-path}")
  //private String appBeAbsolutePath;
  //@Value("${app.fe.cittadino.absolute-path}")
  //private String appFeCittadinoAbsolutePath;
  @Value("${fesp.mode}")
  private String fespMode;

  //public String getUrlSendPaymentRequest(String idSession) {
  //  return appBeAbsolutePath + ANONYMOUS_PATH + "/richiestaPagamento?idSession=" + idSession;
  //}

  @GetMapping(ANONYMOUS_PATH + "/richiestaPagamento")
  public ResponseEntity<Object> landingSendPaymentRequest(@RequestParam String idSession) {
    try {
      if (StringUtils.isBlank(idSession)) {
        log.error("idSession parameter is mandatory");
        throw new BadRequestException("idSession parameter is mandatory");
      }
      CarrelloRp carrelloRpOpt = carrelloRpService.getByIdSession(idSession).orElseThrow(NotFoundException::new);

      CarrelloRpt carrelloRptOpt = carrelloRptService.getByCarrelloRpId(carrelloRpOpt.getMygovCarrelloRpId())
          .orElseThrow(NotFoundException::new);

      if(!StringUtils.equalsIgnoreCase(carrelloRptOpt.getDeRptInviacarrellorptEsitoComplessivoOperazione(),"OK")){
        log.error("error landingSendPaymentRequest, mygov_carrello_rpt_id[{}], cod_rpt_inviacarrellorpt_fault_code[{}], cod_rpt_inviacarrellorpt_fault_string[{}]",
          carrelloRptOpt.getMygovCarrelloRptId(), carrelloRptOpt.getCodRptInviacarrellorptFaultCode(), carrelloRptOpt.getCodRptInviacarrellorptFaultString());
        throw new MyPayException("Errore interno");
      }

      String redirectUrl = UriComponentsBuilder
          .fromUriString(carrelloRptOpt.getCodRptInviacarrellorptUrl())
          .query(LANG_PARAM_ITA)
          .encode()
          .toUriString();

      return redirectTo(redirectUrl);
    } catch (Exception e) {
      log.error("landingSendPaymentRequest", e);
      throw new MyPayException("system error", e);
    }
  }

  @GetMapping(ANONYMOUS_PATH + "/esitoPagamento")
  public ResponseEntity<?> landingPaymentResponse(@RequestParam(name="idSession") String nodoSPCFespIdSession, @RequestParam String esito) {
    if (StringUtils.isBlank(nodoSPCFespIdSession)) {
      log.error("idSession parameter is mandatory");
      throw new BadRequestException("idSession parameter is mandatory");
    }
    String dominio = null;
    RptRt rptRt = null;
    try {
      String idSession;
      rptRt = rptRtService.getByIdSession(nodoSPCFespIdSession);
      if (rptRt == null) {
        log.info("Nessuna RPT presente per idSessione: {}", nodoSPCFespIdSession);
        CarrelloRpt carrelloRpt = carrelloRptService.getByIdSession(nodoSPCFespIdSession).orElseThrow(NotFoundException::new);
        CarrelloRp carrelloRp = Optional.ofNullable(carrelloRpt.getMygovCarrelloRpId()).orElseThrow(NotFoundException::new);
        dominio = carrelloRp.getCodiceFiscaleEnte();
        idSession = carrelloRp.getIdSessionCarrello();
      } else {
        RpE rpE = rPEService.getById(rptRt.getMygovRpEId()).orElseThrow(NotFoundException::new);
        dominio = rpE.getCodRpSilinviarpIdDominio();
        idSession = rpE.getIdSession();
      }
      Ente ente = Optional.ofNullable(enteService.getEnteByCodFiscale(dominio)).orElseThrow(NotFoundException::new);

      String esitoParam = ESITO_PAGAMENTO.KO.getValue().equals(esito)? FESP_ESITO_ERROR_NODO_SPC : NODO_REGIONALE_FESP_ESITO_OK;

      try {
        giornaleService.registraEvento(new Date(), dominio,
          Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptIdUnivocoVersamento), Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptCodiceContestoPagamento),
                Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptIdPsp), Utilities.ifNotNull(rptRt, RptRt::getCodRptDatiVersTipoVersamento), Constants.COMPONENTE_WFESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(), Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaRispostaPagamentoCarrel.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
          Constants.NODO_REGIONALE_FESP, dominio,
                Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptIdStazioneIntermediarioPa), Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptIdCanale),
          nodoSPCFespIdSession+" - "+esito,
          StringUtils.equalsIgnoreCase(esito,"OK") ? Constants.GIORNALE_ESITO_EVENTO.OK.toString() : Constants.GIORNALE_ESITO_EVENTO.KO.toString());
      } catch (Exception e) {
        log.warn("nodoInviaRispostaPagamentoCarrel [RESPONSE] impossible to insert in the event log", e);
      }

      log.debug("fesp mode redirect {}", fespMode);
      if (fespMode.equalsIgnoreCase("local")) {
        return landingControllerModulePa.landingPaymentResponse(idSession, esitoParam);
      } else {
        return redirectTo(UriComponentsBuilder
            .fromUriString(ente.getPaaSilInviaRispostaPagamentoUrl())
            .queryParam("idSession", idSession)
            .queryParam("esito", esitoParam)
            .encode()
            .toUriString());
      }
    } catch (Exception e) {
      log.error("landingPaymentResponse", e);
      try {
        giornaleService.registraEvento(new Date(), dominio,
          Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptIdUnivocoVersamento),
          Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptCodiceContestoPagamento),
          Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptIdPsp),
          Utilities.ifNotNull(rptRt, RptRt::getCodRptDatiVersTipoVersamento),
          Constants.COMPONENTE_WFESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaRispostaPagamentoCarrel.toString(),
          Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
          Constants.NODO_REGIONALE_FESP, dominio,
          Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptIdStazioneIntermediarioPa),
          Utilities.ifNotNull(rptRt, RptRt::getCodRptInviarptIdCanale),
          nodoSPCFespIdSession + " - " + esito + " - " + e,
          Constants.GIORNALE_ESITO_EVENTO.KO.toString());
      } catch (Exception e2) {
        log.warn("nodoInviaRispostaPagamentoCarrel [RESPONSE] impossible to insert in the event log", e2);
      }
      throw new MyPayException("system error", e);
    }
  }

  @GetMapping(PAYMENT_OUTCOME_URL+"/{outcome}")
  public ResponseEntity<?> landingPaymentResponseFromCheckout(@PathVariable String outcome, @RequestParam String id) {
    if (fespMode.equalsIgnoreCase("local")) {
      return landingControllerModulePa.landingPaymentResponseFromCheckout(outcome, id);
    } else {
      throw new UnsupportedOperationException("landingPaymentResponseFromCheckout");
    }
  }

  private ResponseEntity<Object> redirectTo(String url) throws URISyntaxException {
    URI uri = new URI(url);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setLocation(uri);
    log.debug("redirecting to :" + url);
    return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(httpHeaders).build();
  }
}