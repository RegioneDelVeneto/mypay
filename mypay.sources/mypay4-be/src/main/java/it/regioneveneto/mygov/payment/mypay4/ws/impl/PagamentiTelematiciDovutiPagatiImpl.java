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

import it.regioneveneto.mygov.payment.mypay4.bo.IdentificativoUnivocoEnte;
import it.regioneveneto.mygov.payment.mypay4.controller.FlussoController;
import it.regioneveneto.mygov.payment.mypay4.controller.MyBoxController;
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoMultibeneficiarioDao;
import it.regioneveneto.mygov.payment.mypay4.dao.FlussoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.GpdService;
import it.regioneveneto.mygov.payment.mypay4.storage.ContentStorage;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.PagamentiTelematiciDovutiPagati;
import it.regioneveneto.mygov.payment.mypay4.ws.util.ManageWsFault;
import it.veneto.regione.pagamenti.ente.*;
import it.veneto.regione.pagamenti.ente.ppthead.IntestazionePPT;
import it.veneto.regione.schemas._2012.pagamenti.ente.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static it.regioneveneto.mygov.payment.mypay4.ws.helper.PagamentiTelematiciDovutiPagatiHelper.*;
import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.*;
import static java.util.stream.Collectors.*;

@Service("PagamentiTelematiciDovutiPagatiImpl")
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciDovutiPagatiImpl implements PagamentiTelematiciDovutiPagati {

  @Value("${mypay.path.import.dovuti}")
  private String dovutiImportPath;

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;

  @Value("${pa.codIpaEntePredefinito}")
  private String adminEnteCodIpa;

  @Value("${pa.modelloUnico}")
  private boolean modelloUnico;

  @Value("${pa.gpd.enabled}")
  private boolean gpdEnabled;

  @Value("${pa.gpd.preload}")
  private boolean gpdPreload;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  private FlussoDao flussoDao;

  @Autowired
  private EnteService enteService;

  @Autowired
  private CarrelloService carrelloService;

  @Autowired
  private CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;

  @Autowired
  private DovutoService dovutoService;

  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;

  @Autowired
  private ImportDovutiService importDovutiService;

  @Autowired
  private ExportDovutiService exportDovutiService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private OperatoreEnteTipoDovutoService operatoreEnteTipoDovutoService;

  @Autowired
  private ChiediPagatiConRicevutaService chiediPagatiConRicevutaService;

  @Autowired
  private ChiediPosizioniAperteService chiediPosizioniAperteService;

  @Autowired
  private JAXBTransformService jaxbTransformService;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Autowired
  private IdentificativoUnivocoService identificativoUnivocoService;

  @Autowired
  private LocationService locationService;

  @Autowired
  private StorageService storageService;

  @Autowired
  private LandingService landingService;

  @Autowired
  private DatiMarcaBolloDigitaleService marcaBolloDigitaleService;

  @Autowired
  private AnagraficaSoggettoService anagraficaSoggettoService;

  @Autowired
  private PrenotazioneFlussoService prenotazioneFlussoService;

  @Autowired
  private FlussoExportScadutiService flussoExportScadutiService;

  @Autowired
  private DovutoMultibeneficiarioDao dovutoMultibeneficiarioDao;

  @Autowired
  private TassonomiaService tassonomiaService;

  @Autowired
  private EsitoService esitoService;

  @Autowired(required = false)
  private GpdService gpdService;

  @Override
  public PaaSILImportaDovutoRisposta paaSILImportaDovuto(PaaSILImportaDovuto body, IntestazionePPT header) {
    return dovutoService.importDovuto(
      Boolean.TRUE.equals(body.isFlagGeneraIuv()),
      header.getCodIpaEnte(),
      body.getPassword(),
      body.getDovuto(),
      body.getListaDovutiEntiSecondari());
  }

  @Override
  public PaaSILAutorizzaImportFlussoRisposta paaSILAutorizzaImportFlusso(PaaSILAutorizzaImportFlusso bodyrichiesta, IntestazionePPT header) {
    String codIpaEnte = header.getCodIpaEnte();
    String password = bodyrichiesta.getPassword();
    PaaSILAutorizzaImportFlussoRisposta paaSILAutorizzaImportFlussoRisposta = new PaaSILAutorizzaImportFlussoRisposta();
    var faultBeanEnte = enteService.verificaEnte(codIpaEnte, password, false);
    if (faultBeanEnte.isPresent()) {
      paaSILAutorizzaImportFlussoRisposta.setFault(faultBeanEnte.get());
      return paaSILAutorizzaImportFlussoRisposta;
    }

    String requestToken = UUID.randomUUID().toString();
    String authorizationToken = jwtTokenUtil.generateWsAuthorizationToken(codIpaEnte, null, requestToken, FlussoController.FILE_TYPE_FLUSSI_IMPORT);
    paaSILAutorizzaImportFlussoRisposta.setAuthorizationToken(authorizationToken);
    paaSILAutorizzaImportFlussoRisposta.setRequestToken(requestToken);
    paaSILAutorizzaImportFlussoRisposta.setImportPath(File.separator+dovutiImportPath);
    paaSILAutorizzaImportFlussoRisposta.setUploadUrl(appBeAbsolutePath + MyBoxController.UPLOAD_FLUSSO_PATH);

    return paaSILAutorizzaImportFlussoRisposta;
  }

  @Override
  public void paaSILChiediEsitoCarrelloDovuti(String codIpaEnte, String password, String idSessionCarrello, Holder<FaultBean> fault, Holder<ListaCarrelli> listaCarrelli) {
    log.info("Executing operation paaSILChiediEsitoCarrelloDovuti");

    Optional<FaultBean> faultValue = enteService.verificaEnte(codIpaEnte, password);
    if (faultValue.isPresent()) {
      fault.value = faultValue.get();
      return;
    }

    listaCarrelli.value = new ListaCarrelli();
    var carrelloMultiEnte = carrelloMultiBeneficiarioService.getByIdSession(idSessionCarrello);

    if (carrelloMultiEnte.isEmpty()) {
      //check on storage if carrello has been initialized, but not yet persisted
      if(storageService.getString(StorageService.WS_USER, idSessionCarrello).map(idToken -> storageService.getObject(StorageService.WS_USER, idToken, BasketTo.class).map(basketTo -> {
        log.info("idSessionCarrello [{}] is on storage and associated to idToken [{}] but not yet on DB", idSessionCarrello, idToken);
        basketTo.getItems().forEach(cartItem -> {
          RispostaCarrello rispostaCarrello = new RispostaCarrello();
          rispostaCarrello.setEsito(Constants.STATO_CARRELLO_PREDISPOSTO);
          rispostaCarrello.setCodIpaEnte(cartItem.getCodIpaEnte());
          listaCarrelli.value.getRispostaCarrellos().add(rispostaCarrello);
        });
        return true;
      })).isPresent()){
        return;
      } else if(storageService.isTokenWithTimestampExpired(idSessionCarrello)) {
        //check if carrello is not anymore on storage because storage expired
        RispostaCarrello rispostaCarrello = new RispostaCarrello();
        rispostaCarrello.setEsito(Constants.STATO_CARRELLO_SCADUTO);
        rispostaCarrello.setCodIpaEnte(codIpaEnte);
        listaCarrelli.value.getRispostaCarrellos().add(rispostaCarrello);
        return;
      }

      fault.value = VerificationUtils.getFaultBean("n/a",
        CODE_PAA_ID_SESSION_NON_VALIDO,
        "idSession [" + idSessionCarrello + "] non valido", null);
      return;
    }

    Optional<Carrello> carrello = carrelloService.getByMultiBeneficarioId(carrelloMultiEnte.get().getMygovCarrelloMultiBeneficiarioId());
    if (carrello.isEmpty()) {
      if(modelloUnico) {
        RispostaCarrello rispostaCarrello = new RispostaCarrello();
        rispostaCarrello.setEsito(carrelloMultiEnte.get().getMygovAnagraficaStatoId().getCodStato());
        rispostaCarrello.setCodIpaEnte(codIpaEnte);
        listaCarrelli.value.getRispostaCarrellos().add(rispostaCarrello);
      } else {
        fault.value = VerificationUtils.getFaultBean("n/a",
            CODE_PAA_ID_SESSION_NON_VALIDO,
            "idSession [" + idSessionCarrello + "] non valido", null);
      }
      return;
    }

    PagatiConRicevuta pagatiConRicevutaDocument;

    RispostaCarrello rispostaCarrello = new RispostaCarrello();
    String statoCarrello = carrello.get().getMygovAnagraficaStatoId().getCodStato();
    String idDominio = carrello.get().getCodRpSilinviarpIdDominio();
    Ente enteCarrello = enteService.getEnteByCodFiscale(idDominio);
    String codIpa = enteCarrello.getCodIpaEnte();

    rispostaCarrello.setCodIpaEnte(codIpa);
    rispostaCarrello.setEsito(carrello.get().getMygovAnagraficaStatoId().getDeStato());

    if (statoCarrello.equals(Constants.STATO_CARRELLO_PAGATO)
      || statoCarrello.equals(Constants.STATO_CARRELLO_NON_PAGATO)
      || statoCarrello.equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
      || statoCarrello.equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE)) {

      List<DovutoElaborato> listaDovuti = dovutoElaboratoService.getByCarrello(carrello.get());

      pagatiConRicevutaDocument = creaPagatiDocumentConRicevuta(carrello.get(), listaDovuti);

      String pagatiConRicevutaString = jaxbTransformService.marshalling(pagatiConRicevutaDocument, PagatiConRicevuta.class);

      log.debug("paaSILChiediEsitoCarrelloDovuti per dominio [{}] e IUV [{}]: {}", pagatiConRicevutaDocument.getDominio().getIdentificativoDominio(), pagatiConRicevutaDocument.getDatiPagamento()
        .getIdentificativoUnivocoVersamento(), pagatiConRicevutaString);

      // se usi soapUI
      // DataHandler pagatiValue = new DataHandler(new
      // ByteArrayDataSource(pagatiString.getBytes(),
      // "application/octet-stream"));

      byte[] encodedPagatiConRicevuta;
      encodedPagatiConRicevuta = Base64.encodeBase64(pagatiConRicevutaString.getBytes(StandardCharsets.UTF_8));
      DataHandler pagatiConRicevutaValue = new DataHandler(
        new ByteArrayDataSource(encodedPagatiConRicevuta, "application/octet-stream"));

      rispostaCarrello.setPagati(pagatiConRicevutaValue);

      if (!listaDovuti.isEmpty()) {
        DovutoElaborato pagato = listaDovuti.get(0);
        DataHandler rtValue = new DataHandler(
          new ByteArrayDataSource(pagato.getBlbRtPayload(), "application/octet-stream"));
        rispostaCarrello.setRt(rtValue);

      } else {
        log.error("paaSILChiediEsitoCarrelloDovuti error listaDovuti non contiene alcun dovuto elaborato per IUV:{}", pagatiConRicevutaDocument.getDatiPagamento()
          .getIdentificativoUnivocoVersamento());
      }
    }
    listaCarrelli.value.getRispostaCarrellos().add(rispostaCarrello);
  }

  @Override
  public void paaSILChiediPagati(String codIpaEnte, String password, String idSession, Holder<FaultBean> fault, Holder<DataHandler> pagati) {
    log.info("Executing operation paaSILChiediPagati");

    Optional<FaultBean> faultValue = enteService.verificaEnte(codIpaEnte, password);
    if (faultValue.isPresent()) {
      fault.value = faultValue.get();
      return;
    }
    Pagati pagatiDocument;
    Carrello carrello;
    var multiCart = carrelloMultiBeneficiarioService.getByIdSession(idSession);
    if (multiCart.isPresent()) {
      carrello = carrelloService.getByMultiBeneficarioId(multiCart.get().getMygovCarrelloMultiBeneficiarioId()).orElse(null);
    } else {
      carrello = carrelloService.getByIdSession(idSession);
    }
    if (carrello == null) {
      FaultBean faultBean;
      if(storageService.getString(StorageService.WS_USER, idSession).map(idToken ->
          storageService.getObject(StorageService.WS_USER, idToken, BasketTo.class)).stream()
        .peek(basketTo -> log.info("idSession [{}] is stored in cache and associated to the basket [{}] but no longer on DB", idSession, basketTo))
        .findFirst().isPresent()) {
        faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_NON_INIZIATO,
          String.format("Pagamento non iniziato per l'idSession specificato [%s]", idSession), null);
      } else if(storageService.isTokenWithTimestampExpired(idSession)) {
        //check if carrello is not anymore on storage because storage expired
        faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO,
          String.format("Pagamento scaduto per l'idSession specificato [%s]", idSession), null);
      } else {
        faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_ID_SESSION_NON_VALIDO,
          String.format("idSession [%s] non valido per l Ente specificato", idSession), null);
      }
      fault.value = faultBean;
      return;
    }

    String statoCarrello = carrello.getMygovAnagraficaStatoId().getCodStato();

    if (statoCarrello.equals(Constants.STATO_CARRELLO_NUOVO)
      || statoCarrello.equals(Constants.STATO_CARRELLO_PREDISPOSTO)) {
      fault.value = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_NON_INIZIATO,
        "Pagamento non iniziato per idSession specificato [" + idSession + "]", null);
      return;
    }

    if (statoCarrello.equals(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO)) {
      fault.value = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_IN_CORSO,
        "Pagamento in corso per idSession specificato [" + idSession + "]", null);
      return;
    }

    if (statoCarrello.equals(Constants.STATO_CARRELLO_ABORT)) {
      fault.value = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_ANNULLATO,
        "Pagamento annullato per idSession specificato [" + idSession + "]", null);
      return;
    }

    if (statoCarrello.equals(Constants.STATO_CARRELLO_SCADUTO)
      || statoCarrello.equals(Constants.STATO_CARRELLO_SCADUTO_ELABORATO)) {
      fault.value = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO,
        "Pagamento scaduto per idSession specificato [" + idSession + "]", null);
      return;
    }
    if (List.of(Constants.STATO_CARRELLO_PAGATO,
                Constants.STATO_CARRELLO_NON_PAGATO,
                Constants.STATO_CARRELLO_DECORRENZA_TERMINI,
                Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE).contains(statoCarrello)) {

      List<DovutoElaborato> listaDovuti = dovutoElaboratoService.getByCarrello(carrello);

      pagatiDocument = creaPagatiDocument(carrello, listaDovuti);

      String pagatiString = jaxbTransformService.marshalling(pagatiDocument, Pagati.class);

      log.debug("PAGATI per dominio [{}] e IUV [{}]: {}", pagatiDocument.getDominio().getIdentificativoDominio(),
        pagatiDocument.getDatiPagamento().getIdentificativoUnivocoVersamento(), pagatiString);

      byte[] encodedPagati;
      encodedPagati = Base64.encodeBase64(pagatiString.getBytes(StandardCharsets.UTF_8));

      pagati.value = new DataHandler(new ByteArrayDataSource(encodedPagati, "application/octet-stream"));
    } else {
      fault.value = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR,
        "Errore interno per idSession: " + idSession);
    }
  }

  @Override
  public void paaSILChiediPagatiConRicevuta(String codIpaEnte, String password, String idSession, String identificativoUnivocoVersamento, String identificativoUnivocoDovuto, Holder<FaultBean> fault, Holder<DataHandler> pagati, Holder<String> tipoFirma, Holder<DataHandler> rt) {
    chiediPagatiConRicevutaService.paaSILChiediPagatiConRicevuta(codIpaEnte, password, idSession, identificativoUnivocoVersamento, identificativoUnivocoDovuto, fault, pagati, tipoFirma, rt);
  }

  public PaaSILChiediPosizioniAperteRisposta paaSILChiediPosizioniAperte(PaaSILChiediPosizioniAperte bodyrechiesta) {
    return chiediPosizioniAperteService.paaSILChiediPosizioniAperte(bodyrechiesta);
  }

  @Override
  public PaaSILChiediStatoExportFlussoRisposta paaSILChiediStatoExportFlusso(PaaSILChiediStatoExportFlusso bodyrichiesta, IntestazionePPT header) {
    PaaSILChiediStatoExportFlussoRisposta paaSILChiediStatoExportFlussoRisposta = new PaaSILChiediStatoExportFlussoRisposta();

    ExportDovuti exportDovuti = exportDovutiService.getFlussoExport(bodyrichiesta.getRequestToken());
    String codIpaEnte = header.getCodIpaEnte();
    String password = bodyrichiesta.getPassword();

    var faultBean = enteService.verificaEnte(codIpaEnte, password, false);
    if (faultBean.isEmpty()) {
      faultBean = verificaExportDovuti(codIpaEnte, exportDovuti, bodyrichiesta.getRequestToken());
    }
    if (faultBean.isPresent()) {
      paaSILChiediStatoExportFlussoRisposta.setFault(faultBean.get());
      return paaSILChiediStatoExportFlussoRisposta;
    }

    paaSILChiediStatoExportFlussoRisposta.setStato(exportDovuti.getMygovAnagraficaStatoId().getCodStato());
    if (exportDovuti.getMygovAnagraficaStatoId().getCodStato().equals(Constants.STATO_EXPORT_ESEGUITO)) {
      if (StringUtils.isBlank(exportDovuti.getDeNomeFileGenerato())) {
        paaSILChiediStatoExportFlussoRisposta.setStato(Constants.STATO_EXPORT_ESEGUITO_NESSUN_DOVUTO_TROVATO);
        return paaSILChiediStatoExportFlussoRisposta;
      }
      String type = FlussoController.FILE_TYPE_FLUSSI_EXPORT;
      String filename = exportDovuti.getDeNomeFileGenerato();
      Long idEnte = exportDovuti.getMygovEnteId().getMygovEnteId();
      String securityToken = jwtTokenUtil.generateSecurityToken(null, type + "|" + idEnte + "|" + filename);
      //String url = landingService.getUrlDownloadFlussiExport(idEnte, securityToken, exportDovuti.getDeNomeFileGenerato());
      String urlDownload = UriComponentsBuilder
        .fromUriString(appBeAbsolutePath + MyBoxController.DOWNLOAD_FLUSSO_PATH + File.separator + idEnte)
        .queryParam("type", type)
        .queryParam("securityToken", URLEncoder.encode(securityToken, StandardCharsets.UTF_8))
        .queryParam("filename", filename)
        .encode()
        .toUriString();
      paaSILChiediStatoExportFlussoRisposta.setDownloadUrl(urlDownload);
    }
    return paaSILChiediStatoExportFlussoRisposta;
  }

  public PaaSILPrenotaExportFlussoScadutiRisposta paaSILPrenotaExportFlussoScaduti(
          PaaSILPrenotaExportFlussoScaduti bodyRichiesta, IntestazionePPT header) {
    PaaSILPrenotaExportFlussoScadutiRisposta paaSILPrenotaExportFlussoScadutiRisposta = new PaaSILPrenotaExportFlussoScadutiRisposta();

    log.info("paaSILPrenotaExportFlussoScaduti: start");
    String codIpaEnte = header.getCodIpaEnte();
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
    if (ente == null) {
      log.error("paaSILPrenotaExportFlussoScaduti: Ente non valido: {}", codIpaEnte);

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_ENTE_NON_VALIDO,
              "codice IPA Ente [" + codIpaEnte + "] non valido o password errata", null);

      paaSILPrenotaExportFlussoScadutiRisposta.setFault(faultBean);
      return paaSILPrenotaExportFlussoScadutiRisposta;
    }

    boolean passwordValidaPerEnte = enteService.verificaPassword(ente.getCodIpaEnte(), bodyRichiesta.getPassword());
    if (!passwordValidaPerEnte) {
      log.error("paaSILPrenotaExportFlussoScaduti: Password non valida per ente: {}", header.getCodIpaEnte());

      FaultBean faultBean = VerificationUtils.getFaultBean(header.getCodIpaEnte(), CODE_PAA_ENTE_NON_VALIDO,
              "Password non valida per ente [" + header.getCodIpaEnte() + "]", null);

      paaSILPrenotaExportFlussoScadutiRisposta.setFault(faultBean);
      return paaSILPrenotaExportFlussoScadutiRisposta;
    }

    String tipiDovuto = bodyRichiesta.getTipiDovuto();
    XMLGregorianCalendar calendarDataScadenza = bodyRichiesta.getDataScadenza();

    /*
     * *******  CONTROLLO SULLA CORRETTEZZA DELLA DATA SCADENZA  *******
     */

    if (calendarDataScadenza == null) {
      log.error("paaSILPrenotaExportFlussoScaduti: Data di scadenza non valida");

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_DATE_FROM_NON_VALIDO, "Data inizio non valida",
              null);

      paaSILPrenotaExportFlussoScadutiRisposta.setFault(faultBean);
      return paaSILPrenotaExportFlussoScadutiRisposta;
    }

    calendarDataScadenza.setTime(0,0,0);
    Date dataScadenza = Utilities.toDate(calendarDataScadenza);

    // //TIPO DOVUTO
    if (StringUtils.isNotBlank(tipiDovuto))
    {
      List<String> tipoDovutoList = new ArrayList<String>(Arrays.asList(tipiDovuto.split("\\s*;\\s*")));
      for (String tipoDovuto : tipoDovutoList)
      {
        EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getByCodTipo(tipoDovuto,codIpaEnte);

        if (enteTipoDovuto == null)
        {
          log.error("paaSILPrenotaExportFlussoScaduti: Identificativo tipo dovuto non valido: {}", tipoDovuto);

          FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO,
                  "identificativoTipoDovuto [" + tipoDovuto + "] non valido", null);
          paaSILPrenotaExportFlussoScadutiRisposta.setFault(faultBean);
          return paaSILPrenotaExportFlussoScadutiRisposta;
        }
      }
    }
    else
    {
      log.error("paaSILPrenotaExportFlussoScaduti: Nessun Identificativo tipo dovuto inserito");

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_INSERITO,
              "identificativoTipoDovuto non valido", null);
      paaSILPrenotaExportFlussoScadutiRisposta.setFault(faultBean);
      return paaSILPrenotaExportFlussoScadutiRisposta;
    }

    String requestToken = UUID.randomUUID().toString();
    String iuf = "EXP_SCADUTI_"+requestToken;

    flussoExportScadutiService.insertWithRefresh(iuf, ente, tipiDovuto, dataScadenza, Constants.STATO_EXPORT_LOAD, requestToken);

    paaSILPrenotaExportFlussoScadutiRisposta.setRequestToken(requestToken);
    log.info("paaSILPrenotaExportFlussoScaduti: end");
    return paaSILPrenotaExportFlussoScadutiRisposta;
  }

  public PaaSILChiediStatoExportFlussoScadutiRisposta paaSILChiediStatoExportFlussoScaduti(PaaSILChiediStatoExportFlussoScaduti bodyrichiesta, IntestazionePPT header) {
    PaaSILChiediStatoExportFlussoScadutiRisposta paaSILChiediStatoExportFlussoScadutiRisposta = new PaaSILChiediStatoExportFlussoScadutiRisposta();
    log.info("paaSILChiediStatoExportFlussoScaduti: start");
    String codIpaEnte = header.getCodIpaEnte();
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
    if (ente == null) {
      log.error("paaSILChiediStatoExportFlussoScaduti: Ente non valido: {}", codIpaEnte);

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_ENTE_NON_VALIDO,
              "codice IPA Ente [" + codIpaEnte + "] non valido o password errata", null);

      paaSILChiediStatoExportFlussoScadutiRisposta.setFault(faultBean);
      return paaSILChiediStatoExportFlussoScadutiRisposta;
    }

    boolean passwordValidaPerEnte = enteService.verificaPassword(ente.getCodIpaEnte(), bodyrichiesta.getPassword());
    if (!passwordValidaPerEnte) {
      log.error("paaSILChiediStatoExportFlussoScaduti: Password non valida per ente: {}", header.getCodIpaEnte());

      FaultBean faultBean = VerificationUtils.getFaultBean(header.getCodIpaEnte(), CODE_PAA_ENTE_NON_VALIDO,
              "Password non valida per ente [" + header.getCodIpaEnte() + "]", null);

      paaSILChiediStatoExportFlussoScadutiRisposta.setFault(faultBean);
      return paaSILChiediStatoExportFlussoScadutiRisposta;
    }

    List<FlussoExportScaduti> scaduti = flussoExportScadutiService.getByRequestToken(bodyrichiesta.getRequestToken());
    if (scaduti.size() > 1) {
      log.error("paaSILChiediStatoExportFlussoScaduti: flusso duplicato per Ente [{}] e requestToken [{}]", codIpaEnte, bodyrichiesta.getRequestToken());

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_REQUEST_TOKEN_NON_VALIDO,
              "requestToken [" + bodyrichiesta.getRequestToken() + "] duplicato per ente [" + codIpaEnte + "] ",
              null);

      paaSILChiediStatoExportFlussoScadutiRisposta.setFault(faultBean);
      return paaSILChiediStatoExportFlussoScadutiRisposta;
    }
    if (scaduti.isEmpty()) {
      log.error("paaSILChiediStatoExportFlussoScaduti: Ente [{}] non valido per requestToken [{}]", codIpaEnte, bodyrichiesta.getRequestToken());

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_REQUEST_TOKEN_NON_VALIDO,
              "requestToken [" + bodyrichiesta.getRequestToken() + "] non valido per ente [" + codIpaEnte + "] ",
              null);

      paaSILChiediStatoExportFlussoScadutiRisposta.setFault(faultBean);
      return paaSILChiediStatoExportFlussoScadutiRisposta;
    }
    FlussoExportScaduti scaduto =  scaduti.get(0);
    if (!codIpaEnte.equals(scaduto.getMygovEnteId().getCodIpaEnte())) {
      log.error("paaSILChiediStatoExportFlussoScaduti: Ente [{}] non autorizzato per requestToken [{}]", codIpaEnte, bodyrichiesta.getRequestToken());

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_REQUEST_TOKEN_NON_VALIDO,
              "requestToken [" + bodyrichiesta.getRequestToken() + "] non valido per ente [" + codIpaEnte + "] ",
              null);

      paaSILChiediStatoExportFlussoScadutiRisposta.setFault(faultBean);
      return paaSILChiediStatoExportFlussoScadutiRisposta;
    }

    AnagraficaStato stato = scaduto.getMygovAnagraficaStatoId();
    paaSILChiediStatoExportFlussoScadutiRisposta.setStato(stato.getCodStato());

    if (stato.getCodStato().equals(Constants.STATO_EXPORT_ESEGUITO))
    {
      if (StringUtils.isBlank(scaduto.getDeNomeFile()))
      {
        paaSILChiediStatoExportFlussoScadutiRisposta.setStato(Constants.STATO_EXPORT_ESEGUITO_NESSUN_DOVUTO_TROVATO);
        return paaSILChiediStatoExportFlussoScadutiRisposta;
      }
      else
      {
        String type = FlussoController.FILE_TYPE_FLUSSI_EXPORT_SCADUTI;
        String filename = scaduto.getDePercorsoFile();
        Long idEnte = scaduto.getMygovEnteId().getMygovEnteId();
        String securityToken = jwtTokenUtil.generateSecurityToken(null, type + "|" + idEnte + "|" + filename);
        String urlDownload = UriComponentsBuilder
                .fromUriString(appBeAbsolutePath + MyBoxController.DOWNLOAD_FLUSSO_PATH + File.separator + idEnte)
                .queryParam("type", type)
                .queryParam("securityToken", URLEncoder.encode(securityToken, StandardCharsets.UTF_8))
                .queryParam("filename", filename)
                .encode()
                .toUriString();
        paaSILChiediStatoExportFlussoScadutiRisposta.setNomeFile(scaduto.getDeNomeFile());
        paaSILChiediStatoExportFlussoScadutiRisposta.setPathFile(scaduto.getDePercorsoFile());
        paaSILChiediStatoExportFlussoScadutiRisposta.setDownloadUrl(urlDownload);
      }
    }
    log.info("paaSILChiediStatoExportFlussoScaduti: end");
    return paaSILChiediStatoExportFlussoScadutiRisposta;
  }

  @Override
  public PaaSILChiediStatoImportFlussoRisposta paaSILChiediStatoImportFlusso(PaaSILChiediStatoImportFlusso bodyrichiesta, IntestazionePPT header) {
    PaaSILChiediStatoImportFlussoRisposta paaSILChiediStatoImportFlussoRisposta = new PaaSILChiediStatoImportFlussoRisposta();

    String nomeFlusso = "";
    String codIpaEnte = header.getCodIpaEnte();
    String password = bodyrichiesta.getPassword();
    ImportDovuti importDovuti = importDovutiService.getFlussoImport(bodyrichiesta.getRequestToken());

    var faultBean = enteService.verificaEnte(codIpaEnte, password, false);
    if (faultBean.isEmpty()) {
      faultBean = verificaImportDovuti(codIpaEnte, importDovuti, bodyrichiesta.getRequestToken());
    }
    if (faultBean.isPresent()) {
      paaSILChiediStatoImportFlussoRisposta.setFault(faultBean.get());
      return paaSILChiediStatoImportFlussoRisposta;
    }

    if(StringUtils.isNotBlank(importDovuti.getCodErrore()))
      paaSILChiediStatoImportFlussoRisposta.setStato(importDovuti.getCodErrore());
    else
      paaSILChiediStatoImportFlussoRisposta.setStato(importDovuti.getMygovAnagraficaStatoId().getCodStato());
    String codRequestToken = importDovuti.getCodRequestToken();
    Flusso flusso = flussoDao.getByCodRequestToken(codRequestToken);
    if(flusso == null)
      log.info("paaSILChiediStatoImportFlusso flusso uguale a null. codRequestToken : {}",codRequestToken);

    if (Constants.STATO_IMPORT_ESEGUITO.equals(importDovuti.getMygovAnagraficaStatoId().getCodStato())) {
      Long idEnte = importDovuti.getMygovEnteId().getMygovEnteId();
      String type = FlussoController.FILE_TYPE_FLUSSI_IMPORT;
      String uriString = appBeAbsolutePath + MyBoxController.DOWNLOAD_FLUSSO_PATH + File.separator + idEnte;
      if (flusso != null) {
        //String url = landingService.getUrlDownloadFlussiExport(idEnte, securityToken, exportDovuti.getDeNomeFileGenerato());
        if (StringUtils.isNotBlank(flusso.getDeNomeFile()) && (flusso.getDeNomeFile().length() > 4)) {
          nomeFlusso += flusso.getDePercorsoFile() + File.separator + flusso.getDeNomeFile().substring(0, flusso.getDeNomeFile().length() - 4);
        }

        if (Boolean.TRUE.equals(bodyrichiesta.isFileIUV()) && flusso.getNumRigheImportateCorrettamente() > 0) {
          String filename = nomeFlusso + "_IUV.zip";
          String securityToken = jwtTokenUtil.generateSecurityToken(null, type + "|" + idEnte + "|" + filename);
          paaSILChiediStatoImportFlussoRisposta.setUrlFileIUV(
            UriComponentsBuilder.fromUriString(uriString)
              .queryParam("type", type)
              .queryParam("securityToken", URLEncoder.encode(securityToken, StandardCharsets.UTF_8))
              .queryParam("filename", filename)
              .encode().toUriString()
          );
        }
        if (Boolean.TRUE.equals(bodyrichiesta.isFileAvvisi()) && flusso.getPdfGenerati() != null && flusso.getPdfGenerati() > 0) {
          String filename = nomeFlusso + "_AVVISI_PDF.zip";
          String securityToken = jwtTokenUtil.generateSecurityToken(null, type + "|" + idEnte + "|" + filename);
          paaSILChiediStatoImportFlussoRisposta.setUrlFileAvvisi(
            UriComponentsBuilder.fromUriString(uriString)
              .queryParam("type", type)
              .queryParam("securityToken", URLEncoder.encode(securityToken, StandardCharsets.UTF_8))
              .queryParam("filename", filename)
              .encode().toUriString()
          );
        }
        if (Boolean.TRUE.equals(bodyrichiesta.isFileScarti()) && flusso.getNumRigheTotali() != null && flusso.getNumRigheImportateCorrettamente() != null && flusso.getNumRigheTotali() - flusso.getNumRigheImportateCorrettamente() > 0) {
          String filename = nomeFlusso + "_SCARTI.zip";
          String securityToken = jwtTokenUtil.generateSecurityToken(null, type + "|" + idEnte + "|" + filename);
          paaSILChiediStatoImportFlussoRisposta.setUrlFileScarti(
            UriComponentsBuilder.fromUriString(uriString)
              .queryParam("type", type)
              .queryParam("securityToken", URLEncoder.encode(securityToken, StandardCharsets.UTF_8))
              .queryParam("filename", filename)
              .encode().toUriString()
          );
        }
      } else if(flusso == null && Boolean.TRUE.equals(bodyrichiesta.isFileScarti()) && StringUtils.isNotBlank(importDovuti.getDeNomeFileScarti())) {
        String filename = importDovuti.getDeNomeFileScarti();
        String securityToken = jwtTokenUtil.generateSecurityToken(null, type + "|" + idEnte + "|" + filename);
        paaSILChiediStatoImportFlussoRisposta.setUrlFileScarti(
          UriComponentsBuilder.fromUriString(uriString)
            .queryParam("type", type)
            .queryParam("securityToken", URLEncoder.encode(securityToken, StandardCharsets.UTF_8))
            .queryParam("filename", filename)
            .encode().toUriString()
        );
      }
    }
    return paaSILChiediStatoImportFlussoRisposta;
  }

  public PaaSILChiediStoricoPagamentiRisposta paaSILChiediStoricoPagamenti(PaaSILChiediStoricoPagamenti bodyrichiesta) {
    log.debug("Executing operation paaSILChiediStoricoPagamenti");
    PaaSILChiediStoricoPagamentiRisposta response = new PaaSILChiediStoricoPagamentiRisposta();

    boolean passwordValidaPerEnte = enteService.verificaPassword(adminEnteCodIpa, bodyrichiesta.getPassword());
    if (!passwordValidaPerEnte) {
      log.error("paaSILChiediPosizioniAperte: Password non valida per ente: {} ", adminEnteCodIpa);
      String msg = String.format("Password non valida per ente [%s]", adminEnteCodIpa);
      response.setFault(VerificationUtils.getFaultBean(adminEnteCodIpa, CODE_PAA_ENTE_NON_VALIDO, msg, null));
      return response;
    }

    String codIpaEnte = bodyrichiesta.getCodIpaEnte();
    CtIdentificativoUnivocoPersonaFG identificativoUnivocoPersonaFG = bodyrichiesta.getIdentificativoUnivocoPersonaFG();

    if (StringUtils.isNotBlank(codIpaEnte)) {
      Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
      if (ente == null) {
        String msg = String.format("codice IPA Ente [%s] non valido", codIpaEnte);
        log.error("paaSILChiediStoricoPagamenti: {}", msg);
        response.setFault(VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_ENTE_NON_VALIDO, msg, null));
        return response;
      }
    }

    response.setFault(VerificationUtils.checkIdentificativoUnivocoPersonaFG(codIpaEnte, identificativoUnivocoPersonaFG));
    if (response.getFault() != null)
      return response;

    Date dtExtractionFrom = Optional.ofNullable(bodyrichiesta.getDataFrom())
      .map(Utilities::toDate)
      .map(Utilities::toMidnight)
      .orElse(Utilities.toMidnight(new Date()));

    Date dtExtractionTo = Optional.ofNullable(bodyrichiesta.getDataTo())
      .map(Utilities::toDate)
      .map(Utilities::toMidnight)
      .orElse(new Date());

    // controllo correttezza intervallo data
    if (dtExtractionTo.before(dtExtractionFrom)) {
      String faultString = String.format("Data di inizio [%s] successiva alla data di fine [%s]", dtExtractionFrom, dtExtractionTo);
      log.error("paaSILChiediStoricoPagamenti: {}", faultString);
      response.setFault(VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_INTERVALLO_DATE_NON_VALIDO, faultString, null));
      return response;
    }

    String tipoIdentificativoUnivoco = bodyrichiesta.getIdentificativoUnivocoPersonaFG().getTipoIdentificativoUnivoco().value();
    String codiceIdentificativoUnivoco = bodyrichiesta.getIdentificativoUnivocoPersonaFG().getCodiceIdentificativoUnivoco();
    try {
      Function<Map.Entry<Carrello, List<DovutoElaborato>>, PaaSILStoricoPagamenti> mapToResponseElement = entry -> {
        Carrello carrello = entry.getKey();
        List<DovutoElaborato> listaDovuti = entry.getValue();
        PaaSILStoricoPagamenti paaSILStoricoPagamentiElement = new PaaSILStoricoPagamenti();

        PagatiConRicevuta pagatiConRicevutaDocument = creaPagatiDocumentConRicevuta(carrello, listaDovuti);
        byte[] xml = jaxbTransformService.marshallingAsBytes(pagatiConRicevutaDocument, PagatiConRicevuta.class);
        DataSource pagatiConRicevutaDataSource = new ByteArrayDataSource(xml, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        DataHandler pagatiConRicevutaDataHandler = new DataHandler(pagatiConRicevutaDataSource);
        paaSILStoricoPagamentiElement.setCtPagatiConRicevuta(pagatiConRicevutaDataHandler);

        if (listaDovuti.isEmpty()) {
          String error = String.format("paaSILChiediStoricoPagamenti error carrello con id [%d]  non contiene alcun dovuto elaborato.", carrello.getMygovCarrelloId());
          log.error(error);
          throw new MyPayException(error);
        } else {
          DataSource rtDataSource = new ByteArrayDataSource(listaDovuti.get(0).getBlbRtPayload(), MediaType.APPLICATION_OCTET_STREAM_VALUE);
          DataHandler rtDataHandler = new DataHandler(rtDataSource);
          paaSILStoricoPagamentiElement.setRt(rtDataHandler);
        }
        paaSILStoricoPagamentiElement.setCodIpaEnte(listaDovuti.get(0).getNestedEnte().getCodIpaEnte());
        paaSILStoricoPagamentiElement.setDeNomeEnte(listaDovuti.get(0).getNestedEnte().getDeNomeEnte());
        paaSILStoricoPagamentiElement.setUrlDownloadRT(landingService.getUrlChiediStoricoPagamenti(carrello.getCodRpIdMessaggioRichiesta()));
        return paaSILStoricoPagamentiElement;
      };
      List<DovutoElaborato> pagatiList = dovutoElaboratoService.getByIdUnivocoPersonaEnteIntervalloData(tipoIdentificativoUnivoco,
        codiceIdentificativoUnivoco, codIpaEnte, dtExtractionFrom, dtExtractionTo);

      Map<Carrello, List<DovutoElaborato>> pagatiPerCarrelloMap = pagatiList.stream()
        .collect(groupingBy(DovutoElaborato::getMygovCarrelloId, toCollection(ArrayList::new)));

      List<PaaSILStoricoPagamenti> paaSILStoricoPagamentiList = pagatiPerCarrelloMap.entrySet().stream()
        .map(mapToResponseElement).collect(toUnmodifiableList());

      response.setDateTo(Utilities.toXMLGregorianCalendar(dtExtractionTo));
      response.getPaaSILStoricoPagamentis().addAll(paaSILStoricoPagamentiList);
      return response;
    } catch (Exception e) {
      String error = String.format("paaSILChiediPosizioniAperte error: [%s]", e.getMessage());
      throw new MyPayException(error, e);
    }
  }

  @Override
  public PaaSILInviaCarrelloDovutiRisposta paaSILInviaCarrelloDovuti(PaaSILInviaCarrelloDovuti bodyrichiesta, IntestazionePPT header) {

    ManageWsFault<PaaSILInviaCarrelloDovutiRisposta> manageFault = (codeFault, faultString, faultDescr, error) -> {
      log.error(faultString, error);
      PaaSILInviaCarrelloDovutiRisposta paaSILInviaCarrelloDovutiRisposta = new PaaSILInviaCarrelloDovutiRisposta();
      FaultBean faultBean = VerificationUtils.getFaultBean(header.getCodIpaEnte(),
        codeFault, faultString, faultDescr);
      paaSILInviaCarrelloDovutiRisposta.setEsito(Constants.STATO_ESITO_KO);
      paaSILInviaCarrelloDovutiRisposta.setFault(faultBean);
      return paaSILInviaCarrelloDovutiRisposta;
    };
    Ente enteHeader = enteService.getEnteByCodIpa(header.getCodIpaEnte());

    // Verifico persenza ente
    if (enteHeader == null)
      return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "codice IPA Ente [" + header.getCodIpaEnte() + "] non valido");

    // Verifico password per ente principale
    boolean passwordValidaPerEnte = enteService.verificaPassword(enteHeader.getCodIpaEnte(), bodyrichiesta.getPassword());
    if (!passwordValidaPerEnte)
      return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "Password non valida per ente [" + header.getCodIpaEnte() + "]");

    // Verifico per ogni carrello presenza ente ,relativa password e presenza codice IUC
    List<ElementoListaDovuti> listaCarrelloDovuti = bodyrichiesta.getListaDovuti().getElementoListaDovutis();
    for (ElementoListaDovuti elemento: listaCarrelloDovuti) {
      String enteListaCodIpa = elemento.getCodIpaEnte();
      Ente enteLista = enteService.getEnteByCodIpa(enteListaCodIpa);
      if (enteLista == null)
        return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "codice IPA Ente [" + elemento.getCodIpaEnte() + "] non valido");

      String passwordLista = elemento.getPassword();
      boolean passwordValidaPerEnteLista = passwordLista.equalsIgnoreCase(enteService.calculateInviaCarrelloDovutiHash(enteLista));

      if (!passwordValidaPerEnteLista)
        return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "Password non valida per ente [" + elemento.getCodIpaEnte() + "]");
    }

    List<CartItem> cartItemList = new ArrayList<>();
    List<IdentificativoUnivocoEnte> listaIdentificativoUnivocoEnte = new ArrayList<>();
    Dovuti dovutoEntePrimario = null;
    // Ciclo ancora tutti i dovuti andando a esaminare i dovuti base64
    for (ElementoListaDovuti elemento: listaCarrelloDovuti) {
      String enteListaCodIpa = elemento.getCodIpaEnte();
      Ente enteLista = enteService.getEnteByCodIpa(enteListaCodIpa);

      Dovuti dovuti;
      try {
        dovuti = jaxbTransformService.unmarshalling(elemento.getDovuti(), Dovuti.class, "/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
      } catch (MyPayException unmarshallingException) {
        String errorMessage = "XML ricevuto per paaSILInviaCarrelloDovuti non conforme all' XSD per ente [" + header.getCodIpaEnte() + "]  XML Error: \n" +
            jaxbTransformService.getDetailUnmarshalExceptionMessage(unmarshallingException, elemento.getDovuti());
        return manageFault.apply(PAA_XML_NON_VALIDO_CODE, "XML dei dovuti non valido", errorMessage, unmarshallingException);
      }

      // #45: Bonifica provincia pagatore case insensitive in import
      bonificaProvinciaPagatore(dovuti);
      dovutoEntePrimario = dovuti;

      String IUV = dovuti.getDatiVersamento().getIdentificativoUnivocoVersamento();

      String tipoVersamento = dovuti.getDatiVersamento().getTipoVersamento();

      List<CtDatiSingoloVersamentoDovuti> listaDovuti = dovuti.getDatiVersamento().getDatiSingoloVersamentos();

      if(listaDovuti.isEmpty()){
        return manageFault.apply(CODE_PAA_XML_NON_VALIDO, "dati singolo versamento mancanti");
      }

      if (StringUtils.isNotBlank(IUV)) {
        return manageFault.apply(CODE_PAA_IUV_NON_VALIDO,"l'inserimento dello IUV è deprecato");
      }

      if (!Utilities.validaTipoVersamento(tipoVersamento))
        return manageFault.apply(CODE_PAA_TIPO_VERSAMENTO_NON_VALIDO, "Tipo versamento non valido [" + tipoVersamento + "]");

      String tipoIdentificativoUnivoco = dovuti.getSoggettoPagatore()
        .getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString();
      String codiceIdentificativoUnivoco = dovuti.getSoggettoPagatore()
        .getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco();

      //MODIFICA LOGICA PER CODICE UTILIZZO FISCALE ANONIMO
      boolean isFlagCfAnonimo = listaDovuti.stream()
        .map(CtDatiSingoloVersamentoDovuti::getIdentificativoTipoDovuto)
        .distinct()
        .allMatch(idTipoDovuto ->
          enteTipoDovutoService.getOptionalByCodTipo(idTipoDovuto, enteLista.getCodIpaEnte(), true)
            .map(EnteTipoDovuto::isFlgCfAnonimo).orElse(false) );
      log.debug("flag CF " + Constants.CODICE_FISCALE_ANONIMO + "[{}]", isFlagCfAnonimo);
      if (!Utilities.isValidCodIdUnivocoConAnonimo(isFlagCfAnonimo, tipoIdentificativoUnivoco, codiceIdentificativoUnivoco)) {
        String faultForAnonimo = codiceIdentificativoUnivoco.equals(Constants.CODICE_FISCALE_ANONIMO)? "Funzionalita CF ANONIMO non valida" : null;
        if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO_F))
          return manageFault.apply(CODE_PAA_CODICE_FISCALE_NON_VALIDO, StringUtils.defaultString(faultForAnonimo, "Codice fiscale non valido [" + codiceIdentificativoUnivoco + "]"));
        else if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO_G))
          return manageFault.apply(CODE_PAA_P_IVA_NON_VALIDO, StringUtils.defaultString(faultForAnonimo, "P.IVA non valida [" + codiceIdentificativoUnivoco + "]"));
        else {
          throw new MyPayException("paaSILInviaCarrelloDovuti error tipoIdentificativoUnivoco non valido: ["+ tipoIdentificativoUnivoco + "]");
        }
      }
      for (CtDatiSingoloVersamentoDovuti ctDatiSingoloVersamentoDovuti : listaDovuti) {

        String IUD = ctDatiSingoloVersamentoDovuti.getIdentificativoUnivocoDovuto();
        if (!Utilities.validaIUD(IUD))
          return manageFault.apply(CODE_PAA_IUD_NON_VALIDO,"IdentificativoUnivocoDovuto non valido [" + IUD + "]");

        IdentificativoUnivocoEnte identificativoUnivocoEnte = new IdentificativoUnivocoEnte("IUD", enteLista.getCodIpaEnte(), IUD);
        if (listaIdentificativoUnivocoEnte.contains(identificativoUnivocoEnte))
          return manageFault.apply(CODE_PAA_IUD_DUPLICATO,"IUD [" + IUD + "] duplicato in input per ente");

        listaIdentificativoUnivocoEnte.add(identificativoUnivocoEnte);

        List<IdentificativoUnivoco> identificativoUnivocos = identificativoUnivocoService.getByEnteAndCodTipoIdAndId(enteLista.getMygovEnteId(), "IUD", IUD);

        if (!identificativoUnivocos.isEmpty())
          return manageFault.apply(CODE_PAA_IUD_DUPLICATO,"IUD [" + IUD + "] gia' inviato");

        String codTipo = ctDatiSingoloVersamentoDovuti.getIdentificativoTipoDovuto();
        Optional<EnteTipoDovuto> enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(codTipo, enteLista.getCodIpaEnte(), false);
        if (enteTipoDovutoOptional.isEmpty())
          return manageFault.apply(CODE_PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO, "identificativoTipoDovuto [" + codTipo + "] non valido");
        EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOptional.get();
        if (!enteTipoDovuto.isFlgAttivo())
          return manageFault.apply(CODE_PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_ABILITATO,"identificativoTipoDovuto [" + codTipo + "] non abilitato");

        if (codTipo.equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
          BigDecimal importo = ctDatiSingoloVersamentoDovuti.getImportoSingoloVersamento();
          if (importo.compareTo(enteTipoDovuto.getImporto())!=0)
            return manageFault.apply(CODE_PAA_IMPORTO_MARCA_BOLLO_DIGITALE_NON_VALIDA,
                "importoSingoloVersamento non valido per tipo dovuto " + Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE);
          if(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale()==null
              || StringUtils.isBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getTipoBollo())
              || StringUtils.isBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getHashDocumento())
              || StringUtils.isBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getProvinciaResidenza()) )
            return manageFault.apply(CODE_PAA_MARCA_BOLLO_DIGITALE_NON_VALIDA,
                "dati marca da bollo mancanti o non validi");
          //check hash documento length <= 72 due to a constraint of PagoPA
          if(StringUtils.length(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getHashDocumento())>72){
            return manageFault.apply(CODE_PAA_MARCA_BOLLO_DIGITALE_NON_VALIDA,
              "hashDocumento non può contenere più di 72 caratteri");
          }
        } else if(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale()!=null
            && (StringUtils.isNotBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getTipoBollo())
            || StringUtils.isNotBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getHashDocumento())
            || StringUtils.isNotBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getProvinciaResidenza())))
          return manageFault.apply(CODE_PAA_MARCA_BOLLO_DIGITALE_NON_VALIDA,
              "dati marca da bollo non supportati su tipo dovuto diverso da marca da bollo");

        String datiSpecificiRiscossione = ctDatiSingoloVersamentoDovuti.getDatiSpecificiRiscossione();
        if (!Utilities.validaDatiSpecificiRiscossione(datiSpecificiRiscossione))
          return manageFault.apply(CODE_PAA_DATI_SPECIFICI_RISCOSSIONE_NON_VALIDO,"DatiSpecificiRiscossione non valido [" + datiSpecificiRiscossione + "]");

        // CONTROLLO IMPORTO DOVUTO DIVERSO DA ZERO
        BigDecimal importoSingoloVersamento = ctDatiSingoloVersamentoDovuti.getImportoSingoloVersamento();
        if (importoSingoloVersamento.compareTo(BigDecimal.ZERO) == 0)
          return manageFault.apply(CODE_PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO,"ImportoSingoloVersamento non valido [" + importoSingoloVersamento + "]");

        // MODIFICHE ACCERTAMENTO PER CAPITOLI PREDETERMINATI
        Bilancio bilancio = ctDatiSingoloVersamentoDovuti.getBilancio();
        if(bilancio != null && !Utilities.verificaImportoBilancio(bilancio, importoSingoloVersamento))
          return manageFault.apply(CODE_PAA_IMPORTO_BILANCIO_NON_VALIDO,
            "Importo bilancio non congruente per dovuto con importo [" + importoSingoloVersamento + "]");

        if (StringUtils.isBlank(ctDatiSingoloVersamentoDovuti.getCausaleVersamento()))
          return manageFault.apply(PAA_CAUSALE_NON_PRESENTE_CODE,
            "Causale versamento obbligatoria mancante");
      }

      final AnagraficaPagatore anagraficaPagatore = anagraficaSoggettoService.fromSoggettoPagatore(dovuti.getSoggettoPagatore());
      String nomeFlusso = "_" + enteLista.getCodIpaEnte() + "_ESTERNO-ANONIMO_MULTIENTE";
      cartItemList.addAll(dovuti.getDatiVersamento().getDatiSingoloVersamentos().stream().map(dovuto ->
        CartItem.builder()
          .codIpaEnte(enteLista.getCodIpaEnte())
          .codTipoDovuto(dovuto.getIdentificativoTipoDovuto())
          .causale(dovuto.getCausaleVersamento())
          .importo(dovuto.getImportoSingoloVersamento())
          .datiSpecificiRiscossione(dovuto.getDatiSpecificiRiscossione())
          .iud(dovuto.getIdentificativoUnivocoDovuto())
          .intestatario(anagraficaPagatore)
          .identificativoUnivocoFlusso(nomeFlusso)
          .bolloDigitale(marcaBolloDigitaleService.mapCtDatiMarcaBolloDigitaleToModel(dovuto.getDatiMarcaBolloDigitale()))
          .bilancio(jaxbTransformService.marshallingNoNamespace(dovuto.getBilancio(), Bilancio.class))
          .build()).collect(toList()));
    }

    /*
     * Multi-beneficiary IUV management if defined (IUV_MULTI_09)
     * Controllo se il dovuto è un dovuto multibeneficiario, perchè è solo per questi che devo controllare
     * le informazioni degli Enti secondari, rappresentate dall'elemento "DovutiEntiSecondari".
     * NOTA: Un dovuto è multibeneficiario se listaDovutiEntiSecondari.getElementoListaDovutiEntiSecondaris() è diverso da null e size 1.
     */
    DovutiEntiSecondari dovutiEntiSecondari = null;
    if (null!=bodyrichiesta.getListaDovutiEntiSecondari()) {

      //String codIpaEntePrimario = listaCarrelloDovuti.get(0).getCodIpaEnte();
      //Ente entePrimario = enteService.getEnteByCodIpa(codIpaEntePrimario);
      //BigDecimal importoDovutoEntePrimario = dovutoEntePrimario.getDatiVersamento().getDatiSingoloVersamentos().get(0).getImportoSingoloVersamento();
      int numeroDatiSingoloVersamentoEntePrimario = dovutoEntePrimario.getDatiVersamento().getDatiSingoloVersamentos().size();

      if (listaCarrelloDovuti.size() > 1)
        return manageFault.apply(CODE_PAA_LIMITE_MASSIMO_DOVUTI_MULTIBENEFICIARI,"Numero dovuti Ente primario [" + listaCarrelloDovuti.size() + "] non valido");

      if (numeroDatiSingoloVersamentoEntePrimario > 1)
        return manageFault.apply(CODE_PAA_LIMITE_MASSIMO_DOVUTI_MULTIBENEFICIARI,"Numero dati singolo versamento Ente primario [" + numeroDatiSingoloVersamentoEntePrimario + "] non valido");

      List<ElementoListaDovutiEntiSecondari> listaDovutiEntiSecondari = bodyrichiesta.getListaDovutiEntiSecondari().getElementoListaDovutiEntiSecondaris();
      try {

        if (null != listaDovutiEntiSecondari &&
                listaDovutiEntiSecondari.size() == 1) {   /* Verifico che sia presente un solo dovuto per Ente secondario */

          for (ElementoListaDovutiEntiSecondari elementoListaDovutiEntiSecondari : listaDovutiEntiSecondari) {
            try {
              dovutiEntiSecondari = jaxbTransformService.unmarshalling(elementoListaDovutiEntiSecondari.getDovutiEntiSecondari(), DovutiEntiSecondari.class, "/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
            } catch (MyPayException unmarshallingException) {
              String errorMessage = "XML ricevuto per paaSILInviaCarrelloDovuti non conforme all' XSD per ente [" + header.getCodIpaEnte() + "]  XML Error: \n" +
                  jaxbTransformService.getDetailUnmarshalExceptionMessage(unmarshallingException, elementoListaDovutiEntiSecondari.getDovutiEntiSecondari());
              return manageFault.apply(PAA_XML_NON_VALIDO_CODE, "XML dei dovuti non valido", errorMessage, unmarshallingException);
            }

            CtDatiVersamentoDovutiEntiSecondari ctDatiVersamentoDovutiEntiSecondari = dovutiEntiSecondari.getDatiVersamentoEntiSecondari();

            String codiceFiscaleBeneficiario = ctDatiVersamentoDovutiEntiSecondari.getCodiceFiscaleBeneficiario();
            if (!Utilities.isValidCFOrPIVA(codiceFiscaleBeneficiario))
              return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"Codice Fiscale o Partita Iva Ente secondario non valido [" + codiceFiscaleBeneficiario + "]");

            String ibanAccreditoBeneficiario = ctDatiVersamentoDovutiEntiSecondari.getIbanAccreditoBeneficiario();
            if (!Utilities.isValidIban(ibanAccreditoBeneficiario))
              return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"IBAN accredito Ente secondario non valido [" + ibanAccreditoBeneficiario + "]");

            String indirizzoBeneficiario = ctDatiVersamentoDovutiEntiSecondari.getIndirizzoBeneficiario();
            if (StringUtils.isNotBlank(ctDatiVersamentoDovutiEntiSecondari.getIndirizzoBeneficiario())
                    && !Utilities.validaIndirizzoAnagrafica(indirizzoBeneficiario, false))
              return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"Indirizzo Ente secondario non valido [" + indirizzoBeneficiario + "]");

            String civicoBeneficiario = ctDatiVersamentoDovutiEntiSecondari.getCivicoBeneficiario();
            if (StringUtils.isNotBlank(civicoBeneficiario)
                    && !Utilities.validaCivicoAnagrafica(Utilities.bonificaCivicoAnagrafica(civicoBeneficiario), false))
              return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"Civico Ente secondario non valido [" + civicoBeneficiario + "]");
            ctDatiVersamentoDovutiEntiSecondari.setCivicoBeneficiario(civicoBeneficiario);

            String nazioneBeneficiario = ctDatiVersamentoDovutiEntiSecondari.getNazioneBeneficiario();
            NazioneTo nazione = null;
            if (StringUtils.isNotBlank(nazioneBeneficiario)) {
              nazione = locationService.getNazioneByCodIso(nazioneBeneficiario);
              if (nazione == null)
                return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"Nazione Ente secondario non valida [" + nazioneBeneficiario + "]");
            }

            String capBeneficiario = ctDatiVersamentoDovutiEntiSecondari.getCapBeneficiario();
            if (StringUtils.isNotBlank(capBeneficiario)) {
              if ((null != nazione && !Utilities.isValidCAP(capBeneficiario, nazione.getCodiceIsoAlpha2()))
                      || !Utilities.isValidCAP(capBeneficiario))
                return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"CAP Ente secondario non valido [" + capBeneficiario + "]");
            }

            String provinciaBeneficiario = ctDatiVersamentoDovutiEntiSecondari.getProvinciaBeneficiario();
            ProvinciaTo provincia = null;
            if (StringUtils.isNotBlank(provinciaBeneficiario)
                    && null != nazione && nazione.getCodiceIsoAlpha2().equalsIgnoreCase("IT")) {
              provincia = locationService.getProvinciaBySigla(provinciaBeneficiario);
              if (provincia == null)
                return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"Provincia Ente secondario non valida [" + provinciaBeneficiario + "]");
            }

            String localita = ctDatiVersamentoDovutiEntiSecondari.getLocalitaBeneficiario();
            if (StringUtils.isNotBlank(localita)
                    && null != nazione && nazione.getCodiceIsoAlpha2().equalsIgnoreCase("IT")
                    && null != provincia) {
              String comune = locationService.getComuneByNameAndSiglaProvincia(localita, provincia.getSigla())
                      .map(ComuneTo::getComune).orElse("");
              if (StringUtils.isBlank(comune))
                return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"Località Ente secondario non valida [" + localita + "]");
            }

            String datiSpecificiRiscossione = ctDatiVersamentoDovutiEntiSecondari.getDatiSpecificiRiscossione();
            if (StringUtils.isNotBlank(datiSpecificiRiscossione)) {
              String codiceTassonomicoEnteSec = StringUtils.substringBeforeLast(datiSpecificiRiscossione, "/" ) + "/";
              if (!tassonomiaService.ifExitsCodiceTassonomico(codiceTassonomicoEnteSec)) {
                return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"Codice tassonomico dei dati specifici riscossione dell'Ente Secondario non presente in archivio [" + datiSpecificiRiscossione + "]");
              }
            }

            BigDecimal importoSingoloVersamento = ctDatiVersamentoDovutiEntiSecondari.getImportoSingoloVersamento();
            if (importoSingoloVersamento.compareTo(BigDecimal.ZERO) == 0)
              return manageFault.apply(CODE_PAA_ENTE_SECONDARIO_NON_VALIDO,"ImportoSingoloVersamento Ente secondario non valido [" + importoSingoloVersamento + "]");
          }
        } else if (listaDovutiEntiSecondari.size() > 1) {
          return manageFault.apply(CODE_PAA_LIMITE_MASSIMO_DOVUTI_ENTI_SECONDARI,
                  "Numero di Dovuti Ente Secondario superiore ai consentiti da pagopa [" + listaDovutiEntiSecondari.size() + "]");
        }
      } catch (Exception e) {
        String buffer = "Errore durante la lettura dei dovuti Enti secondari per ente [" + header.getCodIpaEnte() + "]" +
          "Errore: \n" + e.getMessage();
        return manageFault.apply(CODE_PAA_ERRORE_RECUPERO_DOVUTI_ENTI_SECONDARI, "Errore durante la lettura dei dovuti Enti secondari", buffer, e);
      }
    }

    BasketTo basketTo = BasketTo.builder()
      .enteCaller(header.getCodIpaEnte())
      .idSession(Utilities.getRandomUUIDWithTimestamp())
        .showMyPay(StShowMyPay.SHORT.value())
      .versante(null)
      .tipoCarrello(Constants.TIPO_CARRELLO_ESTERNO_ANONIMO_MULTIENTE)
      .backUrlInviaEsito(bodyrichiesta.getEnteSILInviaRispostaPagamentoUrl())
      .items(cartItemList)
        .dovutiEntiSecondari(dovutiEntiSecondari)
      .build();

    PaaSILInviaCarrelloDovutiRisposta paaSILInviaCarrelloDovutiRisposta = new PaaSILInviaCarrelloDovutiRisposta();
    ContentStorage.StorageToken storageToken = storageService.putObject(StorageService.WS_USER, basketTo);
    //put in storage also idSessionCarrello, so that it will be possible to check if it exists in some WS before it is saved to DB
    storageService.putObject(StorageService.WS_USER, basketTo.getIdSession(), storageToken.getId());
    paaSILInviaCarrelloDovutiRisposta.setEsito(Constants.STATO_ESITO_OK);
    paaSILInviaCarrelloDovutiRisposta.setIdSessionCarrello(basketTo.getIdSession());
    paaSILInviaCarrelloDovutiRisposta.setRedirect(1);
    paaSILInviaCarrelloDovutiRisposta.setUrl(landingService.getUrlInviaDovuti(storageToken.getId()));

    return paaSILInviaCarrelloDovutiRisposta;
  }

  @Override
  public PaaSILInviaDovutiRisposta paaSILInviaDovuti(PaaSILInviaDovuti bodyrichiesta, IntestazionePPT header) {

    ManageWsFault<PaaSILInviaDovutiRisposta> manageFault = (codeFault, faultString, faultDescr, error) -> {
      log.error(faultString, error);
      PaaSILInviaDovutiRisposta paaSILInviaDovutiRisposta = new PaaSILInviaDovutiRisposta();
      FaultBean faultBean = VerificationUtils.getFaultBean(header.getCodIpaEnte(),
        codeFault, faultString, faultDescr);
      paaSILInviaDovutiRisposta.setEsito(Constants.STATO_ESITO_KO);
      paaSILInviaDovutiRisposta.setFault(faultBean);
      return paaSILInviaDovutiRisposta;
    };

    Ente ente = enteService.getEnteByCodIpa(header.getCodIpaEnte());

    if (ente == null)
      return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "codice IPA Ente [" + header.getCodIpaEnte() + "] non valido");

    boolean isStatoInserito = Utilities.checkIfStatoInserito(ente);
    if (isStatoInserito)
      return manageFault.apply(CODE_PAA_STATO_ENTE_NON_VALIDO, "Stato Ente Non Valido");

    boolean passwordValidaPerEnte = enteService.verificaPassword(ente.getCodIpaEnte(), bodyrichiesta.getPassword());
    if (!passwordValidaPerEnte)
      return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "Password non valida per ente [" + header.getCodIpaEnte() + "]");

    Dovuti dovuti;
    try {
      dovuti = jaxbTransformService.unmarshalling(bodyrichiesta.getDovuti(), Dovuti.class, "/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
    } catch (MyPayException unmarshallingException) {
      String errorMessage = "XML ricevuto per paaSILInviaDovuti non conforme all' XSD per ente [" + header.getCodIpaEnte() + "]  XML Error: \n" +
          jaxbTransformService.getDetailUnmarshalExceptionMessage(unmarshallingException, bodyrichiesta.getDovuti());
      return manageFault.apply(PAA_XML_NON_VALIDO_CODE, "XML dei dovuti non valido", errorMessage, unmarshallingException);
    }

    // #45: Bonifica provincia pagatore case insensitive in import
    bonificaProvinciaPagatore(dovuti);

    List<CtDatiSingoloVersamentoDovuti> listaDovuti = dovuti.getDatiVersamento().getDatiSingoloVersamentos();

    if(listaDovuti.isEmpty()){
      return manageFault.apply(CODE_PAA_XML_NON_VALIDO, "dati singolo versamento mancanti");
    }

    String IUV = dovuti.getDatiVersamento().getIdentificativoUnivocoVersamento();
    if (StringUtils.isNotBlank(IUV)) {
      return manageFault.apply(CODE_PAA_IUV_NON_VALIDO,"l'inserimento dello IUV è deprecato");
    }

    String tipoVersamento = dovuti.getDatiVersamento().getTipoVersamento();

    if (!Utilities.validaTipoVersamento(tipoVersamento))
      return manageFault.apply(CODE_PAA_TIPO_VERSAMENTO_NON_VALIDO,"Tipo versamento non valido [" + tipoVersamento + "]");

    String tipoIdentificativoUnivoco = dovuti.getSoggettoPagatore()
      .getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString();
    String codiceIdentificativoUnivoco = dovuti.getSoggettoPagatore()
      .getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco();
    //String anagraficaPagatore = dovuti.getSoggettoPagatore().getAnagraficaPagatore();

    //MODIFICA LOGICA PER CODICE UTILIZZO FISCALE ANONIMO
    boolean isFlagCfAnonimo = listaDovuti.stream()
      .map(CtDatiSingoloVersamentoDovuti::getIdentificativoTipoDovuto)
      .distinct()
      .allMatch(codTipoDovuto ->
        enteTipoDovutoService.getOptionalByCodTipo(codTipoDovuto, ente.getCodIpaEnte(), true)
          .map(EnteTipoDovuto::isFlgCfAnonimo).orElse(false) );
    log.debug("flag CF " + Constants.CODICE_FISCALE_ANONIMO + "[{}]", isFlagCfAnonimo);
    if (!Utilities.isValidCodIdUnivocoConAnonimo(isFlagCfAnonimo, tipoIdentificativoUnivoco, codiceIdentificativoUnivoco)) {
      String faultForAnonimo = codiceIdentificativoUnivoco.equals(Constants.CODICE_FISCALE_ANONIMO)? "Funzionalita CF ANONIMO non valida" : null;
      if (tipoIdentificativoUnivoco.equals("F"))
        return manageFault.apply(CODE_PAA_CODICE_FISCALE_NON_VALIDO, StringUtils.defaultString(faultForAnonimo, "Codice fiscale non valido [" + codiceIdentificativoUnivoco + "]"));
      else if (tipoIdentificativoUnivoco.equals("G"))
        return manageFault.apply(CODE_PAA_P_IVA_NON_VALIDO, StringUtils.defaultString(faultForAnonimo, "P.IVA non valida [" + codiceIdentificativoUnivoco + "]"));
      else {
        throw new MyPayException("paaSILInviaCarrelloDovuti error tipoIdentificativoUnivoco non valido: ["+ tipoIdentificativoUnivoco + "]");
      }
    }

    for (CtDatiSingoloVersamentoDovuti ctDatiSingoloVersamentoDovuti : listaDovuti) {

      String IUD = ctDatiSingoloVersamentoDovuti.getIdentificativoUnivocoDovuto();
      if (!Utilities.validaIUD(IUD))
        return manageFault.apply(CODE_PAA_IUD_NON_VALIDO,"IdentificativoUnivocoDovuto non valido [" + IUD + "]");

      List<IdentificativoUnivoco> identificativoUnivocos = identificativoUnivocoService
        .getByEnteAndCodTipoIdAndId(ente.getMygovEnteId(), "IUD", IUD);
      if (!identificativoUnivocos.isEmpty())
        return manageFault.apply(CODE_PAA_IUD_DUPLICATO,"IUD [" + IUD + "] già inviato");

      String codTipo = ctDatiSingoloVersamentoDovuti.getIdentificativoTipoDovuto();
      Optional<EnteTipoDovuto> enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(codTipo, ente.getCodIpaEnte(), false);
      if (enteTipoDovutoOptional.isEmpty())
        return manageFault.apply(CODE_PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO,"identificativoTipoDovuto [" + codTipo + "] non valido");
      EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOptional.get();
      if (!enteTipoDovuto.isFlgAttivo())
        return manageFault.apply(CODE_PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_ABILITATO, "identificativoTipoDovuto [" + codTipo + "] non abilitato");

      if (codTipo.equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
        BigDecimal importo = ctDatiSingoloVersamentoDovuti.getImportoSingoloVersamento();
        if (importo.compareTo(enteTipoDovuto.getImporto())!=0)
          return manageFault.apply(CODE_PAA_IMPORTO_MARCA_BOLLO_DIGITALE_NON_VALIDA,
              "importoSingoloVersamento non valido per tipo dovuto " + Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE);
        if(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale()==null
            || StringUtils.isBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getTipoBollo())
            || StringUtils.isBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getHashDocumento())
            || StringUtils.isBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getProvinciaResidenza()) )
          return manageFault.apply(CODE_PAA_MARCA_BOLLO_DIGITALE_NON_VALIDA,
              "dati marca da bollo mancanti o non validi");
        //check hash documento length <= 72 due to a constraint of PagoPA
        if(StringUtils.length(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getHashDocumento())>72){
          return manageFault.apply(CODE_PAA_MARCA_BOLLO_DIGITALE_NON_VALIDA,
            "hashDocumento non può contenere più di 72 caratteri");
        }
      } else if(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale()!=null
          && (StringUtils.isNotBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getTipoBollo())
          || StringUtils.isNotBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getHashDocumento())
          || StringUtils.isNotBlank(ctDatiSingoloVersamentoDovuti.getDatiMarcaBolloDigitale().getProvinciaResidenza())))
        return manageFault.apply(CODE_PAA_MARCA_BOLLO_DIGITALE_NON_VALIDA,
            "dati marca da bollo non supportati su tipo dovuto diverso da marca da bollo");

      String datiSpecificiRiscossione = ctDatiSingoloVersamentoDovuti.getDatiSpecificiRiscossione();
      if (!Utilities.validaDatiSpecificiRiscossione(datiSpecificiRiscossione))
        return manageFault.apply(CODE_PAA_DATI_SPECIFICI_RISCOSSIONE_NON_VALIDO,"DatiSpecificiRiscossione non valido [" + datiSpecificiRiscossione + "]");

      // CONTROLLO IMPORTO DOVUTO DIVERSO DA ZERO
      BigDecimal importoSingoloVersamento = ctDatiSingoloVersamentoDovuti.getImportoSingoloVersamento();
      if (importoSingoloVersamento.compareTo(BigDecimal.ZERO) == 0)
        return manageFault.apply(CODE_PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO,"ImportoSingoloVersamento non valido [" + importoSingoloVersamento + "]");

      // MODIFICHE ACCERTAMENTO PER CAPITOLI PREDETERMINATI
      Bilancio bilancio = ctDatiSingoloVersamentoDovuti.getBilancio();
      if (bilancio != null) {
        if (!Utilities.verificaImportoBilancio(bilancio, importoSingoloVersamento))
          return manageFault.apply(CODE_PAA_IMPORTO_BILANCIO_NON_VALIDO,"Importo bilancio non congruente per dovuto con importo [" + importoSingoloVersamento + "]");
      }

      if (StringUtils.isBlank(ctDatiSingoloVersamentoDovuti.getCausaleVersamento()))
        return manageFault.apply(PAA_CAUSALE_NON_PRESENTE_CODE, "Causale versamento obbligatoria mancante");
    }
    log.debug("Param showMyPay is: {} - if is null it get SHORT as default", bodyrichiesta.getShowMyPay());
    var showMyPay = Optional.ofNullable(bodyrichiesta.getShowMyPay()).map(StShowMyPay::value).orElse(StShowMyPay.SHORT.value());
    if (StShowMyPay.NONE.value().equalsIgnoreCase(showMyPay)) {

      // bonifiche da anagrafica con verifiche stringenti su dati facoltativi, se non validi esce su ws vecchio la bonifica viene fatta nel dao

      boolean anagraficaValida = true;
      String anagraficaError = null;
      // bonifica i dati prima di validarli
      if (dovuti.getSoggettoPagatore().getEMailPagatore() == null || !Utilities.isValidEmail(dovuti.getSoggettoPagatore().getEMailPagatore())) {
        anagraficaValida = false;
        anagraficaError = "email non valida";
      }

      if (StringUtils.isEmpty((dovuti.getSoggettoPagatore().getAnagraficaPagatore()))) {
        anagraficaValida = false;
        anagraficaError = "denominazione non valorizzata";
      }

      if (StringUtils.isNotBlank(dovuti.getSoggettoPagatore().getCapPagatore())) {
        NazioneTo nazione = locationService.getNazioneByCodIso(dovuti.getSoggettoPagatore().getNazionePagatore());
        if (nazione == null) {
          anagraficaValida = false;
          anagraficaError = "nazione non valorizzata";
        } else {
          if (!Utilities.isValidCAP(dovuti.getSoggettoPagatore().getCapPagatore(), String.valueOf(nazione.getCodiceIsoAlpha2()))) {
            anagraficaValida = false;
            anagraficaError = "cap non valido";
          }
        }
      }

      if (StringUtils.isNotBlank(dovuti.getSoggettoPagatore().getIndirizzoPagatore())) {
        if (!Utilities.validaIndirizzoAnagrafica(Utilities.bonificaIndirizzoAnagrafica(dovuti.getSoggettoPagatore().getIndirizzoPagatore()), false)) {
          anagraficaValida = false;
          anagraficaError = "indirizzo non valido";
        }
      }

      if (StringUtils.isNotBlank(dovuti.getSoggettoPagatore().getCivicoPagatore())) {
        if (!Utilities.validaCivicoAnagrafica(Utilities.bonificaCivicoAnagrafica(dovuti.getSoggettoPagatore().getCivicoPagatore()), false)) {
          anagraficaValida = false;
          anagraficaError = "civico non valido";
        }
      }

      if (!anagraficaValida)
        return manageFault.apply(CODE_PAA_ANAGRAFICA_NON_VALIDA,"paaSILInviaDovuti: Anagrafica Pagatore non valida" + anagraficaError + "]");
    }

    //final String tipoVersamento = dovuti.getDatiVersamento().getTipoVersamento();
    final AnagraficaPagatore anagraficaPagatore = anagraficaSoggettoService.fromSoggettoPagatore(dovuti.getSoggettoPagatore());
    String nomeFlusso = "_" + header.getCodIpaEnte() + "_ESTERNO-ANONIMO";
    BasketTo basketTo = BasketTo.builder()
        .enteCaller(header.getCodIpaEnte())
        .idSession(Utilities.getRandomUUIDWithTimestamp())
        .showMyPay(showMyPay)
        .versante(StShowMyPay.SHORT.value().equals(showMyPay)? null : anagraficaPagatore)
        .tipoCarrello(Constants.TIPO_CARRELLO_ESTERNO_ANONIMO)
        .backUrlInviaEsito(bodyrichiesta.getEnteSILInviaRispostaPagamentoUrl())
        .items(dovuti.getDatiVersamento().getDatiSingoloVersamentos().stream().map(dovuto ->
          CartItem.builder()
            .codIpaEnte(header.getCodIpaEnte())
            .codTipoDovuto(dovuto.getIdentificativoTipoDovuto())
            .causale(dovuto.getCausaleVersamento())
            .importo(dovuto.getImportoSingoloVersamento())
            .datiSpecificiRiscossione(dovuto.getDatiSpecificiRiscossione())
            .iud(dovuto.getIdentificativoUnivocoDovuto())
            .intestatario(anagraficaPagatore)
            .identificativoUnivocoFlusso(nomeFlusso)
            .bolloDigitale(marcaBolloDigitaleService.mapCtDatiMarcaBolloDigitaleToModel(dovuto.getDatiMarcaBolloDigitale()))
            .bilancio(jaxbTransformService.marshallingNoNamespace(dovuto.getBilancio(), Bilancio.class))
            .build()).collect(toList()))
        .build();

    PaaSILInviaDovutiRisposta paaSILInviaDovutiRisposta = new PaaSILInviaDovutiRisposta();
    ContentStorage.StorageToken storageToken = storageService.putObject(StorageService.WS_USER, basketTo);
    //put in storage also idSessionCarrello, so that it will be possible to check if it exists in some WS before it is saved to DB
    storageService.putObject(StorageService.WS_USER, basketTo.getIdSession(), storageToken.getId());
    paaSILInviaDovutiRisposta.setEsito(Constants.STATO_ESITO_OK);
    paaSILInviaDovutiRisposta.setIdSession(basketTo.getIdSession());
    paaSILInviaDovutiRisposta.setRedirect(1);
    paaSILInviaDovutiRisposta.setUrl(landingService.getUrlInviaDovuti(storageToken.getId()));

    return paaSILInviaDovutiRisposta;
  }

  @Override
  public PaaSILPrenotaExportFlussoRisposta paaSILPrenotaExportFlusso(PaaSILPrenotaExportFlusso body, IntestazionePPT header) {
    WsExportFlussoIncomeTo incomeTo = WsExportFlussoIncomeTo.builder()
      .codIpaEnte(header.getCodIpaEnte())
      .password(body.getPassword())
      .dateFrom(Utilities.toDate(body.getDateFrom()))
      .dateTo(Utilities.toDate(body.getDateTo()))
      .identificativoTipoDovuto(body.getIdentificativoTipoDovuto())
      .ricevuta(false)
      .incrementale(false)
      .versioneTracciato(body.getVersioneTracciato())
      .build();
    WsExportFlussoOutcomeTo outcomeTo = prenotazioneFlussoService.handlePrenotazioneFlussoExport(incomeTo);
    var response = new PaaSILPrenotaExportFlussoRisposta();
    if(outcomeTo.getFaultBean()!=null) {
      response.setFault(outcomeTo.getFaultBean());
    } else {
      response.setRequestToken(outcomeTo.getRequestToken());
    }
    return response;
  }

  @Override
  public PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta paaSILPrenotaExportFlussoIncrementaleConRicevuta(PaaSILPrenotaExportFlussoIncrementaleConRicevuta body, IntestazionePPT header) {
    Date dateTo = ObjectUtils.defaultIfNull(Utilities.toDate(body.getDateTo()), new Date());
    WsExportFlussoIncomeTo incomeTo = WsExportFlussoIncomeTo.builder()
      .codIpaEnte(header.getCodIpaEnte())
      .password(body.getPassword())
      .dateFrom(Utilities.toDate(body.getDateFrom()))
      .dateTo(dateTo)
      .identificativoTipoDovuto(body.getIdentificativoTipoDovuto())
      .ricevuta(body.isRicevuta())
      .incrementale(body.isIncrementale())
      .versioneTracciato(body.getVersioneTracciato())
      .passwordMypivot(body.getPasswordMypivot())
      .build();
    WsExportFlussoOutcomeTo outcomeTo = prenotazioneFlussoService.handlePrenotazioneFlussoExport(incomeTo);
    var response = new PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta();
    if(outcomeTo.getFaultBean()!=null) {
      response.setFault(outcomeTo.getFaultBean());
    } else {
      response.setDateTo(Utilities.toXMLGregorianCalendar(outcomeTo.getDateTo()));
      response.setRequestToken(outcomeTo.getRequestToken());
    }
    return response;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void paaSILRegistraPagamento(String codIpaEnte, String password, String identificativoUnivocoVersamento, String codiceContestoPagamento, BigDecimal singoloImportoPagato, XMLGregorianCalendar dataEsitoSingoloPagamento, Integer indiceDatiSingoloPagamento, String identificativoUnivocoRiscossione, String tipoIstitutoAttestante, String codiceIstitutiAttestante, String denominazioneAttestante, Holder<FaultBean> fault, Holder<String> esito) {

    if (gpdEnabled || gpdPreload) {
      String iupd = null;
      String cfEnte = null;
      List<Dovuto> listDovuti = dovutoService.getByIuvEnte(identificativoUnivocoVersamento, codIpaEnte);
      Dovuto d = null;
      if (!listDovuti.isEmpty()) {
        d = listDovuti.get(0);
        iupd = d.getGpdIupd();
        cfEnte = d.getNestedEnte().getCodiceFiscaleEnte();
      } else {
        List<DovutoElaborato> listDovutiElaborati = dovutoElaboratoService.getByIuvEnte(identificativoUnivocoVersamento, codIpaEnte);
        if (!listDovutiElaborati.isEmpty()) {
          DovutoElaborato de = listDovutiElaborati.get(0);
          iupd = de.getGpdIupd();
          cfEnte = de.getNestedEnte().getCodiceFiscaleEnte();
        }

      }
      if (gpdEnabled && StringUtils.isNotBlank(iupd)) {
        gpdService.deleteDebtPositionForCodice9(cfEnte, iupd, identificativoUnivocoVersamento);
      } else if (gpdPreload && d != null) {
        gpdService.managePreload('A', d, null);
      }
    }
    log.info("Executing operation paaSILRegistraPagamento: IUV: {} Ente: {}", identificativoUnivocoVersamento, codIpaEnte);
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
    DovutoElaborato dovutoElaborato;
    esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

    if (ente == null) {
      log.error("paaSILRegistraPagamento: Ente non valido: {}", codIpaEnte);
      esito.value = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
      fault.value = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_ENTE_NON_VALIDO,
        "codice IPA Ente [" + codIpaEnte + "] non valido", null);
      log.info("paaSILRegistraPagamento: esito = {}", esito.value);
      return;
    }

    boolean isStatoInserito = Utilities.checkIfStatoInserito(ente);

    if (isStatoInserito) {
      log.error("paaSILRegistraPagamento: Ente non inserito: {}", codIpaEnte);
      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_STATO_ENTE_NON_VALIDO, "Stato Ente Non Valido", null);
      faultBean.setSerial(0);
      esito.value = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
      fault.value = faultBean;
      log.info("paaSILRegistraPagamento: esito = {}", esito.value);
      return;
    }

    boolean passwordValidaPerEnte = enteService.verificaPassword(ente.getCodIpaEnte(), password);
    if (!passwordValidaPerEnte) {
      log.error("paaSILRegistraPagamento: Password non valida per ente: {}", codIpaEnte);
      esito.value = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
      fault.value = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_ENTE_NON_VALIDO,
        "Password non valida per ente [" + codIpaEnte + "]", null);
      log.info("paaSILRegistraPagamento: esito = {}", esito.value);
      return;
    }

    //creazione oggetto datiRendicontazioneCod9
    DatiRendicontazioneCod9 datiRendicontazioneCod9 = new DatiRendicontazioneCod9();
    datiRendicontazioneCod9.setIdentificativoUnivocoVersamento(identificativoUnivocoVersamento);
    datiRendicontazioneCod9.setCodiceContestoPagamento(codiceContestoPagamento);
    datiRendicontazioneCod9.setSingoloImportoPagato(singoloImportoPagato);
    datiRendicontazioneCod9.setDataEsitoSingoloPagamento(Utilities.toDate(dataEsitoSingoloPagamento));
    datiRendicontazioneCod9.setIndiceDatiSingoloPagamento(indiceDatiSingoloPagamento);
    datiRendicontazioneCod9.setIdentificativoUnivocoRiscossione(identificativoUnivocoRiscossione);
    IstitutoAttestante istAtt = new IstitutoAttestante();
    istAtt.setTipoIdentificativoUnivoco(tipoIstitutoAttestante);
    istAtt.setCodiceIdentificativoUnivoco(codiceIstitutiAttestante);
    istAtt.setDenominazioneAttestante(denominazioneAttestante);
    datiRendicontazioneCod9.setIstitutoAttestante(istAtt);

    List<Dovuto> listaDovuti = new ArrayList<>();
    //Differenzio la ricerca per modello
    //Cerco dovuto in mygov_dovuto dato IUV e codIpa
    if (Utilities.isAvviso(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento()))
      listaDovuti = dovutoService.getByIuvEnte(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte());
    else {
      Carrello carrelloDatoIuv = carrelloService.getByIdDominioIUV(ente.getCodiceFiscaleEnte(), datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
      if (null != carrelloDatoIuv)
        listaDovuti = dovutoService.getDovutiInCarrello(carrelloDatoIuv.getMygovCarrelloId());
    }

    if (!CollectionUtils.isEmpty(listaDovuti)) {
      log.info("paaSILRegistraPagamento: dovuto presente in mygovDovuto");
      Dovuto dovuto = listaDovuti.get(0);
      Carrello carrello = new Carrello();

      /*IUV_MULTI_15 Multi-beneficiary IUV management if defined
       * check if dovuto is dovuto multi-beneficiary
       */
      boolean checkExsistDovutoMultiben = dovutoMultibeneficiarioDao.checkExsistDovutoMultibeneficiarioByIdDovuto(dovuto.getMygovDovutoId()) > 0;

      List<DovutoElaborato> dovutoElaboratoListCompletato;
      //Se il dovuto ha il carrello, allora mi assicuro che anche l'eventuale dovuto_elaborato punti allo stesso carrello
      if (Utilities.carrelloIsPresent(dovuto)) {
        Long carrelloIdDovuto = dovuto.getMygovCarrelloId().getMygovCarrelloId();
        dovutoElaboratoListCompletato = dovutoElaboratoService.getByIuvEnteStato(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte(), Constants.STATO_DOVUTO_COMPLETATO, true, carrelloIdDovuto);
      } else {
        dovutoElaboratoListCompletato = dovutoElaboratoService.getByIuvEnteStato(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte(), Constants.STATO_DOVUTO_COMPLETATO, true, null);
      }
      //Se per il dovuto presente in mygov_dovuto è presente già dovuto in mygov_dovuto_elaborato in stato completato
      //Controllo se è pagato
      if (!CollectionUtils.isEmpty(dovutoElaboratoListCompletato)) {
        log.info("paaSILRegistraPagamento: dovuto presente in mygovDovuto e in mygovDovutoElaborato in stato completato");
        dovutoElaborato = dovutoElaboratoListCompletato.get(0);
        carrello = dovutoElaborato.getMygovCarrelloId();
        if (null != carrello) {
          log.info("paaSILRegistraPagamento: esiste carrello per dovuto");
          AnagraficaStato anagCarrello = dovutoElaborato.getMygovCarrelloId().getMygovAnagraficaStatoId();
          //Controllo lo stato del pagamento, se lo stato del dovuto è pagato non faccio nulla,
          //altrimenti setto lo stato a "PAGATO" ed aggiorno
          if (!(anagCarrello.getCodStato().equalsIgnoreCase(Constants.STATO_CARRELLO_PAGATO))) {
            //Diverso da PAGATO
            carrello = dovutoElaborato.getMygovCarrelloId();
            carrello = carrelloService.aggiornaCarrelloInPagato(datiRendicontazioneCod9, carrello, ente, dovuto);
            carrello = carrelloService.upsert(carrello);
            //Aggiorno i dati del dovutoElaborato per allinearli a quello di uno in stato COMPLETATo e PAGATO
            fillDovutoElaboratoCompletato(datiRendicontazioneCod9, dovuto, carrello, dovutoElaborato);
            dovutoElaboratoService.upsert(dovutoElaborato);
          }
        } else {
          log.info("paaSILRegistraPagamento: non esiste carrello per dovuto");
          //dovuto elaborato COMPLETATO senza carrello
          Carrello carrelloFittizio = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, dovuto, ente);
          carrello = carrelloService.upsert(carrelloFittizio);
          //Aggiorno i dati del dovutoElaborato per allinearli a quello di uno in stato COMPLETATo e PAGATO
          fillDovutoElaboratoCompletato(datiRendicontazioneCod9, dovuto, carrello, dovutoElaborato);
          dovutoElaboratoService.upsert(dovutoElaborato);
        }


      } else {
        List<DovutoElaborato> dovutoElaboratoListNoCompletato;
        if (Utilities.carrelloIsPresent(dovuto)) {
          Long carrelloIdDovuto = dovuto.getMygovCarrelloId().getMygovCarrelloId();
          dovutoElaboratoListNoCompletato = dovutoElaboratoService.getByIuvEnteStato(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte(), Constants.STATO_DOVUTO_COMPLETATO, false, carrelloIdDovuto);
        } else {
          dovutoElaboratoListNoCompletato = dovutoElaboratoService.getByIuvEnteStato(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte(), Constants.STATO_DOVUTO_COMPLETATO, false, null);
        }
        //Controllo se per il dovuto in mygov_dovuto è presente già dovuto in mygov_dovuto_elaborato in stato diverso da completato
        if (!CollectionUtils.isEmpty(dovutoElaboratoListNoCompletato)) {
          log.info("paaSILRegistraPagamento: dovuto presente in mygovDovuto e in mygovDovutoElaborato in stato diverso da completato");
          dovutoElaborato = dovutoElaboratoListNoCompletato.get(0);
          carrello = dovutoElaborato.getMygovCarrelloId();
          if (null == carrello) {
            log.info("paaSILRegistraPagamento: non esiste carrello per dovuto");
            //crea carrello fittizio
            Carrello carrelloFittizio = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, dovuto, ente);
            carrello = carrelloService.upsert(carrelloFittizio);
            dovutoElaborato.setMygovCarrelloId(carrello);
          } else {
            log.info("paaSILRegistraPagamento: esiste carrello per dovuto");
            carrello = carrelloService.aggiornaCarrelloInPagato(datiRendicontazioneCod9, carrello, ente, dovuto);
            carrello = carrelloService.upsert(carrello);
          }
          dovuto.setMygovCarrelloId(carrello);
          dovutoService.upsert(dovuto);
          dovutoElaboratoService.elaborateDovutoNoCompletato(dovuto.getMygovDovutoId(), dovutoElaborato, dovuto.getMygovAnagraficaStatoId().getCodStato(), Constants.STATO_DOVUTO_COMPLETATO, ente, datiRendicontazioneCod9);
        }else {
          //Il dovuto è presente solo il mygov_dovuto
          if (Utilities.carrelloIsPresent(dovuto)) {
            log.info("paaSILRegistraPagamento: esiste carrello per dovuto");
            carrello = dovuto.getMygovCarrelloId();
            carrello = carrelloService.aggiornaCarrelloInPagato(datiRendicontazioneCod9, carrello, ente, dovuto);
            carrello = carrelloService.upsert(carrello);
          } else {
            log.info("paaSILRegistraPagamento: non esiste carrello per dovuto");
            //crea carrello fittizio
            Carrello carrelloFittizio = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, dovuto, ente);
            carrello = carrelloService.upsert(carrelloFittizio);
          }
          dovuto.setMygovCarrelloId(carrello);
          dovutoService.upsert(dovuto);
          dovutoElaborato = dovutoElaboratoService.insert(dovuto.getMygovDovutoId(), dovuto.getMygovAnagraficaStatoId().getCodStato(), Constants.STATO_DOVUTO_COMPLETATO, ente, datiRendicontazioneCod9);

          // Multi-beneficiary IUV management if defined
          if(checkExsistDovutoMultiben) {
            DovutoMultibeneficiario dovutoMultibeneficiario = dovutoMultibeneficiarioDao.getByIdDovuto(dovuto.getMygovDovutoId());
            dovutoElaboratoService.insertDovutoMultibenefElaborato(dovuto, dovutoMultibeneficiario, dovutoElaborato.getMygovDovutoElaboratoId());
          }

        }
      }
      carrelloService.deleteDovutoCarrello(dovuto.getMygovDovutoId(), carrello.getMygovCarrelloId());

      if(checkExsistDovutoMultiben) //Elimino il record da DovutoMultibeneficiario
        dovutoService.deleteDovMultibenefByIdDovuto(dovuto.getMygovDovutoId());

      dovutoService.delete(dovuto);
      esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
      log.info("paaSILRegistraPagamento: esito = {}", esito.value);

    } else {
      //Non è presente in mygov_dovuto, cerco dovuto in mygov_dovuto_elaborato dato IUV e codIpa con stato COMPLETATO
      log.info("paaSILRegistraPagamento: dovuto non presente in mygovDovuto");
      List<DovutoElaborato> dovutoElaboratoListCompletato = dovutoElaboratoService.getByIuvEnteStato(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte(), Constants.STATO_DOVUTO_COMPLETATO, true, null);
      if (!CollectionUtils.isEmpty(dovutoElaboratoListCompletato)) {
        log.info("paaSILRegistraPagamento: dovuto presente in mygovDovuto in stato completato");
        dovutoElaborato = dovutoElaboratoListCompletato.get(0);
        Carrello carrello = dovutoElaborato.getMygovCarrelloId();
        if (null != carrello) {
          log.info("paaSILRegistraPagamento: esiste carrello per dovuto");
          AnagraficaStato anagCarrello = dovutoElaborato.getMygovCarrelloId().getMygovAnagraficaStatoId();
          //Controllo lo stato del pagamento, se lo stato del dovuto è pagato non faccio nulla,
          //altrimenti setto lo stato a "PAGATO" ed aggiorno
          if(!(anagCarrello.getCodStato().equalsIgnoreCase(Constants.STATO_CARRELLO_PAGATO))) {
            //Diverso da PAGATO
            AnagraficaStato anagStatoCarrelloPagato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PAGATO, Constants.STATO_TIPO_CARRELLO);
            carrello = dovutoElaborato.getMygovCarrelloId();
            carrello.setMygovAnagraficaStatoId(anagStatoCarrelloPagato);
            carrelloService.upsert(carrello);
            //Aggiorno i dati del dovutoElaborato per allinearli a quello di uno in stato COMPLETATo e PAGATO
            fillDovutoElaboratoWithNoDataEsito(datiRendicontazioneCod9, dovutoElaborato, carrello);
            dovutoElaboratoService.upsert(dovutoElaborato);
          }
        }else {
          log.info("paaSILRegistraPagamento: non esiste carrello per dovuto");
          //dovuto elaborato COMPLETATO senza carrello
          Carrello carrelloFittizio = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, null, ente);
          carrello = carrelloService.upsert(carrelloFittizio);
          //Aggiorno i dati del dovutoElaborato per allinearli a quello di uno in stato COMPLETATo e PAGATO
          fillDovutoElaboratoWithNoDataEsito(datiRendicontazioneCod9, dovutoElaborato, carrello);
          dovutoElaboratoService.upsert(dovutoElaborato);
        }
        esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
        log.info("paaSILRegistraPagamento: esito = {}", esito.value);
      }
      else {//Non è presente in mygov_dovuto e non è presente in mygov_dovuto_elaborato con stato COMPLETATO,
            //cerco dovuto in mygov_dovuto_elaborato dato IUV e codIpa con stato diverso da COMPLETATO

        List<DovutoElaborato> dovutoElaboratoListNoCompletato = dovutoElaboratoService.getByIuvEnteStato(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte(), Constants.STATO_DOVUTO_COMPLETATO, false, null);
        log.info("paaSILRegistraPagamento: dovuto presente in mygovDovuto in stato non completato");
        if (!CollectionUtils.isEmpty(dovutoElaboratoListNoCompletato)) {
          dovutoElaborato = dovutoElaboratoListNoCompletato.get(0);
          Carrello carrello = dovutoElaborato.getMygovCarrelloId();
          if (null == carrello) {
            log.info("paaSILRegistraPagamento: non esiste carrello per dovuto");
            //crea carrello fittizio
            Carrello carrelloFittizio = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, null, ente);
            carrello = carrelloService.upsert(carrelloFittizio);
            dovutoElaborato.setMygovCarrelloId(carrello);
          } else {
            log.info("paaSILRegistraPagamento: esiste carrello per dovuto");
            carrello = carrelloService.aggiornaCarrelloInPagato(datiRendicontazioneCod9, carrello, ente, null);
            carrelloService.upsert(carrello);
          }
          dovutoElaboratoService.elaborateDovutoNoCompletato(dovutoElaborato, Constants.STATO_DOVUTO_COMPLETATO, ente, datiRendicontazioneCod9);
          esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
          log.info("paaSILRegistraPagamento: esito = {}", esito.value);
        } else {//Non è presente in mygov_dovuto, non è presente in mygov_dovuto_elaborato con stato diverso da COMPLETATO,
          //non è presente in mygov_dovuto_elaborato con stato COMPLETATO, questo indica che il dovuto non esiste su MyPay
          //Dovuto non presente su MyPay
          log.info("paaSILRegistraPagamento: dovuto non presente su MyPay");
          //Creazione del dovuto DEFAULT per l'ente, se non è già presente
          try {
            if(enteTipoDovutoService.getOptionalByCodTipo(Constants.COD_TIPO_DOVUTO_DEFAULT, ente.getCodIpaEnte(), false).isEmpty()) { //prima volta del tipo dovuto DEFAULT
              EnteTipoDovuto enteTipoDovuto = EnteTipoDovutoService.getEnteTipoDovutoDefault();
              enteTipoDovuto.setMygovEnteId(ente);
              enteTipoDovutoService.insert(enteTipoDovuto);
              //Associo dovuto a tutti gli operatori dell'ente
              List<Operatore> operatoriList = operatoreEnteTipoDovutoService.getOperatoriByCodIpaEnte(ente.getCodIpaEnte());
              if (!operatoriList.isEmpty()) {
                for (Operatore operatore : operatoriList) {
                  operatoreEnteTipoDovutoService.insert(Constants.WS_USER_INFO, operatore.getCodFedUserId(), Constants.COD_TIPO_DOVUTO_DEFAULT, ente.getCodIpaEnte());
                }
              }
            }
          } catch (Exception e) {
            log.error("paaSILRegistraPagamento errore nel recupero del tipo dovuto: [{}]", e.getMessage());
            esito.value = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
            log.error("paaSILRegistraPagamento: esito = {}", esito.value);
            throw new MyPayException("paaSILRegistraPagamento errore nel recupero del tipo dovuto: [" + e.getMessage() + "]", e);
          }

          Carrello carrello = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, null, ente);
          carrello = carrelloService.upsert(carrello);
          dovutoElaborato = dovutoElaboratoService.getFittizio(carrello, datiRendicontazioneCod9, ente);
          dovutoElaboratoService.upsert(dovutoElaborato);
          esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
          log.info("paaSILRegistraPagamento: esito = {}", esito.value);
        }
      }
    }
    Optional.ofNullable(dovutoElaborato).ifPresent(esitoService::handlePushEsito);
  }

  @Override
  public PaaSILVerificaAvvisoRisposta paaSILVerificaAvviso(PaaSILVerificaAvviso bodyrichiesta, IntestazionePPT header) {
    PaaSILVerificaAvvisoRisposta paaSILVerificaAvvisoRisposta = new PaaSILVerificaAvvisoRisposta();

    var faultBean = enteService.verificaEnte(header.getCodIpaEnte(), bodyrichiesta.getPassword(), true);
    if (faultBean.isPresent()) {
      paaSILVerificaAvvisoRisposta.setFault(faultBean.get());
      paaSILVerificaAvvisoRisposta.setEsito("KO");
      return paaSILVerificaAvvisoRisposta;
    }

    String codIuv = bodyrichiesta.getIdentificativoUnivocoVersamento();
    String codIpaEnte = header.getCodIpaEnte();

    List<Dovuto> dovutiAvviso = dovutoService.getByIuvEnte(codIuv, codIpaEnte);
    String tipoDovutoDisabilitato = null;
    /*
    List<Dovuto> dovutiAttivi = new ArrayList<>();
    for (Dovuto dovuto : dovutiAvviso) {
      EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getByCodTipo(dovuto.getCodTipoDovuto(), codIpaEnte);
      if (enteTipoDovuto != null) {
        if (enteTipoDovuto.isFlgAttivo()) {
          dovutiAttivi.add(dovuto);
        } else {
          if (tipoDovutoDisabilitato.length() < 1) {
            tipoDovutoDisabilitato = " (il dovuto [" + dovuto.getCodTipoDovuto() + "] associato allo IUV risulta disabilitato)";
          }
        }
      }
    }*/
    String error = String.format("Error nessun dovuto trovato per IUV [%s] ed ente [%s]", codIuv, codIpaEnte);
    /*
   dovutiAvviso = dovutiAttivi;
   if (dovutiAvviso.isEmpty()) {
      log.error(error);
      paaSILVerificaAvvisoRisposta.setEsito("KO");
      paaSILVerificaAvvisoRisposta.setFault(VerificationUtils.getFaultBean(header.getCodIpaEnte(), CODE_PAA_IUV_NON_VALIDO, error, tipoDovutoDisabilitato));
      return paaSILVerificaAvvisoRisposta;
    }*/

    for (Dovuto dovuto : dovutiAvviso) {
      if(dovuto.isFlgIuvVolatile()){
        log.warn("Removing dovuto volatile per IUV [{}] ed ente [{}]", codIuv, codIpaEnte);
        dovutiAvviso.remove(dovuto);
        continue;
      }
      var optionalEtd = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), codIpaEnte, false);
      if (optionalEtd.map(e -> !e.isFlgAttivo()).orElse(optionalEtd.isEmpty())) {
        dovutiAvviso.remove(dovuto);
        tipoDovutoDisabilitato = StringUtils.defaultIfBlank(tipoDovutoDisabilitato,
          String.format("il dovuto [%s] associato allo IUV risulta disabilitato o non presente", dovuto.getCodTipoDovuto()));
        continue;
      }
      if (!dovuto.getMygovAnagraficaStatoId().getCodStato().equals(Constants.STATO_DOVUTO_DA_PAGARE)) {
        log.error(error);
        paaSILVerificaAvvisoRisposta.setEsito("KO");
        paaSILVerificaAvvisoRisposta.setFault(VerificationUtils.getFaultBean(header.getCodIpaEnte(), CODE_PAA_IUV_NON_VALIDO,
          String.format("non ci sono dovuti pagabili per il dato IUV [%s]", codIuv), null));
        return paaSILVerificaAvvisoRisposta;
      }
      if (optionalEtd.orElseThrow().isFlgScadenzaObbligatoria()) {
        LocalDate dataScadenza = new LocalDate(dovuto.getDtRpDatiVersDataEsecuzionePagamento());
        if (dataScadenza.isBefore(new LocalDate())) {
          log.error("Error dovuto scaduto per IUV [{}] ed ente [{}]", codIuv, codIpaEnte);
          paaSILVerificaAvvisoRisposta.setEsito("KO");
          paaSILVerificaAvvisoRisposta.setFault(VerificationUtils.getFaultBean(header.getCodIpaEnte(), CODE_PAA_IUV_SCADUTO,
            String.format("alcuni dovuti sono scaduti per il dato IUV [%s]", codIuv), null));
          return paaSILVerificaAvvisoRisposta;
        }
      }
    }
    if (dovutiAvviso.isEmpty()) {
      log.error(error);
      paaSILVerificaAvvisoRisposta.setEsito("KO");
      paaSILVerificaAvvisoRisposta.setFault(VerificationUtils.getFaultBean(header.getCodIpaEnte(), CODE_PAA_IUV_NON_VALIDO, error, tipoDovutoDisabilitato));
      return paaSILVerificaAvvisoRisposta;
    }

    BasketTo basketTo = BasketTo.builder()
      .enteCaller(header.getCodIpaEnte())
      .idSession(Utilities.getRandomUUIDWithTimestamp())
      .showMyPay(StShowMyPay.SHORT.value())
      .tipoCarrello(Constants.TIPO_CARRELLO_AVVISO_ANONIMO_ENTE)
      .backUrlInviaEsito(bodyrichiesta.getEnteSILInviaRispostaPagamentoUrl())
      .items(dovutiAvviso.stream()
        .map(dovuto -> CartItem.builder()
          .id(dovuto.getMygovDovutoId())
          .codIpaEnte(dovuto.getNestedEnte().getCodIpaEnte())
          .codTipoDovuto(dovuto.getCodTipoDovuto())
          .codStato(dovuto.getMygovAnagraficaStatoId().getCodStato())
          .causale(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento())
          .causaleVisualizzata(dovuto.getDeCausaleVisualizzata())
          .importo(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento())
          .datiSpecificiRiscossione(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione())
          .iud(dovuto.getCodIud())
          .codIuv(dovuto.getCodIuv())
          .intestatario(anagraficaSoggettoService.getAnagraficaPagatore(dovuto))
          .build())
        .collect(toList()))
      .build();

    paaSILVerificaAvvisoRisposta = new PaaSILVerificaAvvisoRisposta();
    ContentStorage.StorageToken storageToken = storageService.putObject(StorageService.WS_USER, basketTo);
    //put in storage also idSessionCarrello, so that it will be possible to check if it exists in some WS before it is saved to DB
    storageService.putObject(StorageService.WS_USER, basketTo.getIdSession(), storageToken.getId());
    paaSILVerificaAvvisoRisposta.setEsito(Constants.STATO_ESITO_OK);
    paaSILVerificaAvvisoRisposta.setIdSession(basketTo.getIdSession());
    paaSILVerificaAvvisoRisposta.setRedirect(1);
    paaSILVerificaAvvisoRisposta.setUrl(landingService.getUrlInviaDovuti(storageToken.getId()));

    return paaSILVerificaAvvisoRisposta;
  }

  private void bonificaProvinciaPagatore(Dovuti dovuti) {
    log.debug("bonificaProvinciaPagatore");
    if (dovuti != null && dovuti.getSoggettoPagatore() != null) {
      String provinciaPagatore = dovuti.getSoggettoPagatore().getProvinciaPagatore();
      log.debug("provincia pagatore: {}", provinciaPagatore);
      // provincia pagatore deve essere upperCase
      if (provinciaPagatore != null) {
        dovuti.getSoggettoPagatore().setProvinciaPagatore(provinciaPagatore.toUpperCase());
        log.debug("provincia pagatore: {}", dovuti.getSoggettoPagatore().getProvinciaPagatore());
      }
    }
  }

  private void fillDovutoElaboratoCompletato(DatiRendicontazioneCod9 datiRendicontazioneCod9, Dovuto dovuto, Carrello carrello, DovutoElaborato dovutoElaborato) {
    dovutoElaborato.setMygovCarrelloId(carrello);
    dovutoElaborato.setCodEDatiPagCodiceEsitoPagamento(Constants.CODICE_ESITO_PAGAMENTO_ESEGUITO.charAt(0));
    dovutoElaborato.setNumEDatiPagImportoTotalePagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaborato.setNumEDatiPagDatiSingPagSingoloImportoPagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaborato.setDeEDatiPagDatiSingPagEsitoSingoloPagamento(Constants.PAGATO_CON_RENDICONTAZIONE_9);
    dovutoElaborato.setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(datiRendicontazioneCod9.getDataEsitoSingoloPagamento());
    dovutoElaborato.setCodEDatiPagDatiSingPagIdUnivocoRiscoss(datiRendicontazioneCod9.getIdentificativoUnivocoRiscossione());
    String causale = Utilities.getDefaultString().andThen(Utilities.getTruncatedAt(Constants.MAX_LENGHT_CAUSALE)).apply(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(), Constants.CAUSALE_DOVUTO_PAGATO);
    dovutoElaborato.setDeEDatiPagDatiSingPagCausaleVersamento(causale);
    dovutoElaborato.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(StringUtils.defaultIfBlank(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(), Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW));
    dovutoElaborato.setDeRpDatiVersDatiSingVersCausaleVersamentoAgid(causale);
  }

  private void fillDovutoElaboratoWithNoDataEsito(DatiRendicontazioneCod9 datiRendicontazioneCod9, DovutoElaborato dovutoElaborato, Carrello carrello) {
    dovutoElaborato.setMygovCarrelloId(carrello);
    dovutoElaborato.setCodEDatiPagCodiceEsitoPagamento(Constants.CODICE_ESITO_PAGAMENTO_ESEGUITO.charAt(0));
    dovutoElaborato.setNumEDatiPagImportoTotalePagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaborato.setNumEDatiPagDatiSingPagSingoloImportoPagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaborato.setDeEDatiPagDatiSingPagEsitoSingoloPagamento(Constants.PAGATO_CON_RENDICONTAZIONE_9);
    dovutoElaborato.setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(datiRendicontazioneCod9.getDataEsitoSingoloPagamento());
    dovutoElaborato.setCodEDatiPagDatiSingPagIdUnivocoRiscoss(datiRendicontazioneCod9.getIdentificativoUnivocoRiscossione());
    dovutoElaborato.setDeEDatiPagDatiSingPagCausaleVersamento(Constants.CAUSALE_DOVUTO_PAGATO);
    dovutoElaborato.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW);
    dovutoElaborato.setDeRpDatiVersDatiSingVersCausaleVersamentoAgid(Constants.CAUSALE_DOVUTO_PAGATO);
  }
}
