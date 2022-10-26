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
import it.regioneveneto.mygov.payment.mypay4.dao.FlussoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
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
import java.util.function.BiConsumer;
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

  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String propIdStazioneIntermediarioPa;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  private AvvisoService avvisoService;

  @Autowired
  private FlussoDao flussoDao;

  @Autowired
  private EnteService enteService;

  @Autowired
  private UtenteService utenteService;

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
  private GiornaleService giornalePaService;

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
  private FlussoService flussoService;

  @Autowired
  private PrenotazioneFlussoService prenotazioneFlussoService;

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
    CarrelloMultiBeneficiario carrelloMultiEnte = carrelloMultiBeneficiarioService.getByIdSession(idSessionCarrello);

    if (carrelloMultiEnte == null) {
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

      FaultBean faultBean = VerificationUtils.getFaultBean("n/a",
        CODE_PAA_ID_SESSION_NON_VALIDO,
        "idSession [" + idSessionCarrello + "] non valido", null);
      fault.value = faultBean;
      return;
    }

    List<Carrello> listaCarrelliDB = carrelloService.getByMultiBeneficarioId(carrelloMultiEnte.getMygovCarrelloMultiBeneficiarioId());

    for (Carrello carrello : listaCarrelliDB) {
      PagatiConRicevuta pagatiConRicevutaDocument = null;

      RispostaCarrello rispostaCarrello = new RispostaCarrello();
      String statoCarrello = carrello.getMygovAnagraficaStatoId().getCodStato();
      String idDominio = carrello.getCodRpSilinviarpIdDominio();
      Ente enteCarrello = enteService.getEnteByCodFiscale(idDominio);
      String codIpa = enteCarrello.getCodIpaEnte();

      rispostaCarrello.setCodIpaEnte(codIpa);
      rispostaCarrello.setEsito(carrello.getMygovAnagraficaStatoId().getDeStato());

      if (statoCarrello.equals(Constants.STATO_CARRELLO_PAGATO)
        || statoCarrello.equals(Constants.STATO_CARRELLO_NON_PAGATO)
        || statoCarrello.equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
        || statoCarrello.equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE)) {

        List<DovutoElaborato> listaDovuti = dovutoElaboratoService.getByCarrello(carrello);

        pagatiConRicevutaDocument = creaPagatiDocumentConRicevuta(carrello, listaDovuti);

        String pagatiConRicevutaString = jaxbTransformService.marshalling(pagatiConRicevutaDocument, PagatiConRicevuta.class);

        log.debug("paaSILChiediEsitoCarrelloDovuti per dominio ["
          + pagatiConRicevutaDocument.getDominio().getIdentificativoDominio()
          + "] e IUV [" + pagatiConRicevutaDocument.getDatiPagamento()
          .getIdentificativoUnivocoVersamento()
          + "]: " + pagatiConRicevutaString);

        // se usi soapUI
        // DataHandler pagatiValue = new DataHandler(new
        // ByteArrayDataSource(pagatiString.getBytes(),
        // "application/octet-stream"));

        byte[] encodedPagatiConRicevuta;
        encodedPagatiConRicevuta = Base64.encodeBase64(pagatiConRicevutaString.getBytes(StandardCharsets.UTF_8));
        DataHandler pagatiConRicevutaValue = new DataHandler(
          new ByteArrayDataSource(encodedPagatiConRicevuta, "application/octet-stream"));

        rispostaCarrello.setPagati(pagatiConRicevutaValue);

        if (listaDovuti.size() > 0) {
          DovutoElaborato pagato = listaDovuti.get(0);
          DataHandler rtValue = new DataHandler(
            new ByteArrayDataSource(pagato.getBlbRtPayload(), "application/octet-stream"));
          rispostaCarrello.setRt(rtValue);

        } else {
          log.error(
            "paaSILChiediEsitoCarrelloDovuti error listaDovuti non contiene alcun dovuto elaborato per IUV:" + pagatiConRicevutaDocument.getDatiPagamento()
              .getIdentificativoUnivocoVersamento());
        }
      }
      listaCarrelli.value.getRispostaCarrellos().add(rispostaCarrello);
    }
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
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
    Carrello carrello = carrelloService.getByIdSession(idSession);

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
      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_NON_INIZIATO,
        "Pagamento non iniziato per idSession specificato [" + idSession + "]", null);
      fault.value = faultBean;
      return;
    }

    if (statoCarrello.equals(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO)) {
      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_IN_CORSO,
        "Pagamento in corso per idSession specificato [" + idSession + "]", null);
      fault.value = faultBean;
      return;
    }

    if (statoCarrello.equals(Constants.STATO_CARRELLO_ABORT)) {
      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_ANNULLATO,
        "Pagamento annullato per idSession specificato [" + idSession + "]", null);
      fault.value = faultBean;

      // LOG NEL GIORNALE DEGLI EVENTI - chiedi pagati
      Date dataOraEvento = new Date();
      String identificativoDominio = ente.getCodiceFiscaleEnte();
      String identificativoUnivocoVersamento = carrello.getCodRpDatiVersIdUnivocoVersamento() != null
        ? carrello.getCodRpDatiVersIdUnivocoVersamento() : "";
      String codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
        ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
      String identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp() != null
        ? carrello.getCodRpSilinviarpIdPsp() : "";
      String tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
      String componente = Constants.COMPONENTE_PA;
      String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
      String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagati.toString();
      String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
      String identificativoFruitore = Constants.SIL;
      String identificativoErogatore = ente.getCodiceFiscaleEnte();
      String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      String canalePagamento = "";
      String parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_ANNULLATO
        + " Pagamento annullato per idSession specificato [" + idSession + "]";
      String esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

      giornalePaService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
        codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
        categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
        identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);

      return;
    }

    if (statoCarrello.equals(Constants.STATO_CARRELLO_SCADUTO)
      || statoCarrello.equals(Constants.STATO_CARRELLO_SCADUTO_ELABORATO)) {
      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO,
        "Pagamento scaduto per idSession specificato [" + idSession + "]", null);
      fault.value = faultBean;

      // LOG NEL GIORNALE DEGLI EVENTI - chiedi pagati
      Date dataOraEvento = new Date();
      String identificativoDominio = ente.getCodiceFiscaleEnte();
      String identificativoUnivocoVersamento = carrello.getCodRpDatiVersIdUnivocoVersamento() != null
        ? carrello.getCodRpDatiVersIdUnivocoVersamento() : "";
      String codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
        ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
      String identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp() != null
        ? carrello.getCodRpSilinviarpIdPsp() : "";
      String tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
      String componente = Constants.COMPONENTE_PA;
      String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
      String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagati.toString();
      String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
      String identificativoFruitore = Constants.SIL;
      String identificativoErogatore = ente.getCodiceFiscaleEnte();
      String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      String canalePagamento = "";
      String parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_SCADUTO
        + " Pagamento scaduto per idSession specificato [" + idSession + "]";
      String esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

      giornalePaService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
        codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
        categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
        identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);

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
      DataHandler pagatiValue = new DataHandler(new ByteArrayDataSource(encodedPagati, "application/octet-stream"));

      pagati.value = pagatiValue;

      // 1) LOG NEL GIORNALE DEGLI EVENTI - chiedi pagati
      Date dataOraEvento = new Date();
      String identificativoDominio = ente.getCodiceFiscaleEnte();
      String identificativoUnivocoVersamento = carrello.getCodRpDatiVersIdUnivocoVersamento();
      String codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
        ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
      String identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
      String tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
      String componente = Constants.COMPONENTE_PA;
      String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
      String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagati.toString();
      String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
      String identificativoFruitore = Constants.SIL;
      String identificativoErogatore = ente.getCodiceFiscaleEnte();
      String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      String canalePagamento = "";
      String parametriSpecificiInterfaccia = pagatiString;
      String esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

      giornalePaService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
        codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
        categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
        identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);

    } else {
      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR,
        "Errore interno per idSession: " + idSession);
      fault.value = faultBean;
    }
    return;
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
      faultBean = verificaExportDovuti(codIpaEnte, exportDovuti);
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

  @Override
  public PaaSILChiediStatoImportFlussoRisposta paaSILChiediStatoImportFlusso(PaaSILChiediStatoImportFlusso bodyrichiesta, IntestazionePPT header) {
    PaaSILChiediStatoImportFlussoRisposta paaSILChiediStatoImportFlussoRisposta = new PaaSILChiediStatoImportFlussoRisposta();

    String nomeFlusso = "";
    String codIpaEnte = header.getCodIpaEnte();
    String password = bodyrichiesta.getPassword();
    ImportDovuti importDovuti = importDovutiService.getFlussoImport(bodyrichiesta.getRequestToken());

    var faultBean = enteService.verificaEnte(codIpaEnte, password, false);
    if (faultBean.isEmpty()) {
      faultBean = verificaImportDovuti(codIpaEnte, importDovuti);
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
      log.error("paaSILChiediStatoImportFlusso error flusso uguale a null. codRequestToken : %s",codRequestToken);

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
    String codIpaEnte = bodyrichiesta.getCodIpaEnte();
    CtIdentificativoUnivocoPersonaFG identificativoUnivocoPersonaFG = bodyrichiesta.getIdentificativoUnivocoPersonaFG();

    if (StringUtils.isNotBlank(codIpaEnte)) {
      Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
      if (ente == null) {
        String msg = String.format("codice IPA Ente [%s] non valido", codIpaEnte);
        log.error("paaSILChiediStoricoPagamenti: %s", msg);
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
      log.error("paaSILChiediStoricoPagamenti: %s", faultString);
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
          throw new RuntimeException(error);
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
      String error = String.format("paaSILChiediPosizioniAperte error: [{}]", e.getMessage());
      log.error(error);
      throw new RuntimeException(error);
    }
  }

  @Override
  public PaaSILImportaDovutoRisposta paaSILImportaDovuto(PaaSILImportaDovuto bodyrichiesta, IntestazionePPT header) {
    return dovutoService.importDovuto(
      Constants.GIORNALE_TIPO_EVENTO_PA.paaSILImportaDovuto.toString(),
      bodyrichiesta.isFlagGeneraIuv()!= null && bodyrichiesta.isFlagGeneraIuv(),
      header.getCodIpaEnte(),
      bodyrichiesta.getPassword(),
      bodyrichiesta.getDovuto());
  }

  @Override
  public PaaSILInviaCarrelloDovutiRisposta paaSILInviaCarrelloDovuti(PaaSILInviaCarrelloDovuti bodyrichiesta, IntestazionePPT header) {

    Giornale dati = Giornale.builder()
      .identificativoDominio(Constants.EMPTY)
      .identificativoUnivocoVersamento(Constants.EMPTY)
      .codiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA)
      .identificativoPrestatoreServiziPagamento(Constants.EMPTY)
      .tipoVersamento(Constants.EMPTY)
      .componente(Constants.COMPONENTE_PA)
      .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString())
      .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_PA.paaSILInviaCarrelloDovuti.toString())
      .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
      .identificativoFruitore(Constants.SIL)
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
        log.warn("paaSILInviaCarrelloDovuti [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
      }
    };
    ManageWsFault<PaaSILInviaCarrelloDovutiRisposta> manageFault = (codeFault, faultString, faultDescr, error) -> {
      log.error(faultString, error);
      registraGiornale.accept(faultString + (StringUtils.isBlank(faultDescr)?"":(" - "+faultDescr)), Constants.GIORNALE_ESITO_EVENTO.KO.toString());
      PaaSILInviaCarrelloDovutiRisposta paaSILInviaCarrelloDovutiRisposta = new PaaSILInviaCarrelloDovutiRisposta();
      FaultBean faultBean = VerificationUtils.getFaultBean(header.getCodIpaEnte(),
        codeFault, faultString, faultDescr);
      paaSILInviaCarrelloDovutiRisposta.setEsito(Constants.STATO_ESITO_KO);
      paaSILInviaCarrelloDovutiRisposta.setFault(faultBean);
      return paaSILInviaCarrelloDovutiRisposta;
    };
    Ente enteHeader = enteService.getEnteByCodIpa(header.getCodIpaEnte());
    Optional.ofNullable(enteHeader).map(Ente::getCodiceFiscaleEnte).ifPresent(dati::setIdentificativoDominio);
    try {
      String xmlBodyString = jaxbTransformService.marshalling(bodyrichiesta, PaaSILInviaCarrelloDovuti.class);
      String xmlHeaderString = jaxbTransformService.marshalling(header, IntestazionePPT.class);
      registraGiornale.accept(xmlHeaderString + xmlBodyString, Constants.GIORNALE_ESITO_EVENTO.OK.toString());
    } catch (Exception e) {
      log.warn("paaSILInviaCarrelloDovuti [REQUEST] impossible to insert in the event log", e);
    }

    /// Verifico persenza ente
    if (enteHeader == null)
      return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "codice IPA Ente [" + header.getCodIpaEnte() + "] non valido");

    /// Verifico password per ente principale
    Boolean passwordValidaPerEnte = enteService.verificaPassword(enteHeader.getCodIpaEnte(), bodyrichiesta.getPassword());
    if (!passwordValidaPerEnte)
      return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "Password non valida per ente [" + header.getCodIpaEnte() + "]");

    // Verifico per ogni carrello presenza ente ,relativa password e presenza codice IUC
    List<ElementoListaDovuti> listaCarrelloDovuti =bodyrichiesta.getListaDovuti().getElementoListaDovutis();
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
    /// Ciclo ancora tutti i dovuti andando ad esaminare i dovuti base64
    for (ElementoListaDovuti elemento: listaCarrelloDovuti) {
      String enteListaCodIpa = elemento.getCodIpaEnte();
      Ente enteLista = enteService.getEnteByCodIpa(enteListaCodIpa);

      Dovuti dovuti = null;

      try {
        dovuti = jaxbTransformService.unmarshalling(elemento.getDovuti(), Dovuti.class, "/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
      } catch (MyPayException e) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("XML ricevuto per paaSILInviaCarrelloDovuti non conforme all' XSD per ente [" + header.getCodIpaEnte() + "]");
        buffer.append("XML Error: \n").append(e.getMessage());
        return manageFault.apply(PAA_XML_NON_VALIDO_CODE, "XML dei dovuti non valido", buffer.toString(), e);
      }

      // #45: Bonifica provincia pagatore case insensitive in import
      dovuti = bonificaProvinciaPagatore(dovuti);

      String IUV = dovuti.getDatiVersamento().getIdentificativoUnivocoVersamento();
      if (StringUtils.isNotBlank(IUV))
        dati.setIdentificativoUnivocoVersamento(IUV);

      dati.setTipoVersamento(dovuti.getDatiVersamento().getTipoVersamento());

      List<CtDatiSingoloVersamentoDovuti> listaDovuti = dovuti.getDatiVersamento().getDatiSingoloVersamentos();

      if (StringUtils.isNotBlank(IUV)) {
        IdentificativoUnivocoEnte iuvEnte = new IdentificativoUnivocoEnte("IUV", enteLista.getCodIpaEnte(), IUV);
        if (listaIdentificativoUnivocoEnte.contains(iuvEnte))
          return manageFault.apply(CODE_PAA_IUV_DUPLICATO, "IUV [" + IUV + "] duplicato in input per ente [" + enteLista.getCodIpaEnte() + "]");

        listaIdentificativoUnivocoEnte.add(iuvEnte);
        List<IdentificativoUnivoco> identificativoUnivocos = identificativoUnivocoService.getByEnteAndCodTipoIdAndId(enteLista.getMygovEnteId(), "IUV", IUV);
        if (identificativoUnivocos.size() > 0)
          return manageFault.apply(CODE_PAA_IUV_DUPLICATO,"IUV [" + IUV + "] gia'inviato per ente [" + enteLista.getCodIpaEnte() + "]");

        if (listaDovuti.size() > 1)
          return manageFault.apply(CODE_PAA_NUMERO_DOVUTI_PER_IUV_NON_VALIDO,"Numero dovuti [" + listaDovuti.size() + "] per lo stesso IUV [" + IUV + "] non valido");

        if (!Utilities.validaIUV(IUV, false, enteLista.getApplicationCode()))
          return manageFault.apply(CODE_PAA_IUV_NON_VALIDO,"IdentificativoUnivocoVersamento non valido [" + IUV + "]");

      }

      if (!Utilities.validaTipoVersamento(dati.getTipoVersamento()))
        return manageFault.apply(CODE_PAA_TIPO_VERSAMENTO_NON_VALIDO, "Tipo versamento non valido [" + dati.getTipoVersamento() + "]");

      // DEFAULT TIPO VERSAMENTO
      if (StringUtils.isBlank(dati.getTipoVersamento()))
        dati.setTipoVersamento(Constants.ALL_PAGAMENTI);

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
      log.debug("flag CF " + Constants.CODICE_FISCALE_ANONIMO + "[" + isFlagCfAnonimo + "]");
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

        if (identificativoUnivocos.size() > 0)
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
          if (!importo.equals(enteTipoDovuto.getImporto()))
            return manageFault.apply(CODE_PAA_IMPORTO_MARCA_BOLLO_DIGITALE_NON_VALIDA,
              "importoSingoloVersamento " + "non valido per tipo dovuto " + Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE);
        }

        String datiSpecificiRiscossione = ctDatiSingoloVersamentoDovuti.getDatiSpecificiRiscossione();
        if (!Utilities.validaDatiSpecificiRiscossione(datiSpecificiRiscossione))
          return manageFault.apply(CODE_PAA_DATI_SPECIFICI_RISCOSSIONE_NON_VALIDO,"DatiSpecificiRiscossione non valido [" + datiSpecificiRiscossione + "]");


        // CONTROLLO IMPORTO DOVUTO DIVERSO DA ZERO
        BigDecimal importoSingoloVersamento = ctDatiSingoloVersamentoDovuti.getImportoSingoloVersamento();
        if (importoSingoloVersamento.compareTo(BigDecimal.ZERO) == 0)
          return manageFault.apply(CODE_PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO,"ImportoSingoloVersamento non valido [" + importoSingoloVersamento + "]");



//				MODIFICHE ACCERTAMENTO PER CAPITOLI PREDETERMINATI
        Bilancio bilancio = ctDatiSingoloVersamentoDovuti.getBilancio();
        if(bilancio != null && !Utilities.verificaImportoBilancio(bilancio, importoSingoloVersamento))
          return manageFault.apply(CODE_PAA_IMPORTO_BILANCIO_NON_VALIDO,
            "Importo bilancio non congruente per dovuto con importo [" + importoSingoloVersamento + "]");
      }

      try {
        String xmlString = jaxbTransformService.marshalling(dovuti, Dovuti.class);
        registraGiornale.accept(xmlString, Constants.GIORNALE_ESITO_EVENTO.OK.toString());
      } catch (Exception e) {
        log.warn("paaSILInviaCarrelloDovuti [REQUEST] impossible to insert in the event log", e);
      }

      final String tipoVersamento = dovuti.getDatiVersamento().getTipoVersamento();
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

    BasketTo basketTo = BasketTo.builder()
      .enteCaller(header.getCodIpaEnte())
      .idSession(Utilities.getRandomUUIDWithTimestamp())
      .showMyPay(StShowMyPay.NONE.value())
      .versante(null)
      .tipoCarrello(Constants.TIPO_CARRELLO_ESTERNO_ANONIMO_MULTIENTE)
      .backUrlInviaEsito(bodyrichiesta.getEnteSILInviaRispostaPagamentoUrl())
      .items(cartItemList)
      .build();

    PaaSILInviaCarrelloDovutiRisposta paaSILInviaCarrelloDovutiRisposta = new PaaSILInviaCarrelloDovutiRisposta();
    ContentStorage.StorageToken storageToken = storageService.putObject(StorageService.WS_USER, basketTo);
    //put in storage also idSessionCarrello, so that it will be possible to check if it exists in some WS before it is saved to DB
    storageService.putObject(StorageService.WS_USER, basketTo.getIdSession(), storageToken.getId());
    paaSILInviaCarrelloDovutiRisposta.setEsito(Constants.STATO_ESITO_OK);
    paaSILInviaCarrelloDovutiRisposta.setIdSessionCarrello(basketTo.getIdSession());
    paaSILInviaCarrelloDovutiRisposta.setRedirect(1);
    paaSILInviaCarrelloDovutiRisposta.setUrl(landingService.getUrlInviaDovuti(storageToken.getId()));
    registraGiornale.accept(paaSILInviaCarrelloDovutiRisposta.getUrl(), Constants.GIORNALE_ESITO_EVENTO.OK.toString());

    return paaSILInviaCarrelloDovutiRisposta;
  }

  @Override
  public PaaSILInviaDovutiRisposta paaSILInviaDovuti(PaaSILInviaDovuti bodyrichiesta, IntestazionePPT header) {

    Giornale dati = Giornale.builder()
      .identificativoDominio(Constants.EMPTY)
      .identificativoUnivocoVersamento(Constants.EMPTY)
      .codiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA)
      .identificativoPrestatoreServiziPagamento(Constants.EMPTY)
      .tipoVersamento(Constants.EMPTY)
      .componente(Constants.COMPONENTE_PA)
      .categoriaEvento(Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString())
      .tipoEvento(Constants.GIORNALE_TIPO_EVENTO_PA.paaSILInviaDovuti.toString())
      .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString())
      .identificativoFruitore(Constants.SIL)
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
        log.warn("paaSILInviaDovuti [" + dati.getSottoTipoEvento() + "] impossible to insert in the event log", e);
      }
    };

    ManageWsFault<PaaSILInviaDovutiRisposta> manageFault = (codeFault, faultString, faultDescr, error) -> {
      log.error(faultString, error);
      registraGiornale.accept(faultString + (StringUtils.isBlank(faultDescr)?"":(" - "+faultDescr)), Constants.GIORNALE_ESITO_EVENTO.KO.toString());
      PaaSILInviaDovutiRisposta paaSILInviaDovutiRisposta = new PaaSILInviaDovutiRisposta();
      FaultBean faultBean = VerificationUtils.getFaultBean(header.getCodIpaEnte(),
        codeFault, faultString, faultDescr);
      paaSILInviaDovutiRisposta.setEsito(Constants.STATO_ESITO_KO);
      paaSILInviaDovutiRisposta.setFault(faultBean);
      return paaSILInviaDovutiRisposta;
    };

    Ente ente = enteService.getEnteByCodIpa(header.getCodIpaEnte());
    Optional.ofNullable(ente).map(Ente::getCodiceFiscaleEnte).ifPresent(dati::setIdentificativoDominio);
    try {
      String xmlBodyString = jaxbTransformService.marshalling(bodyrichiesta, PaaSILInviaDovuti.class);
      String xmlHeaderString = jaxbTransformService.marshalling(header, IntestazionePPT.class);
      registraGiornale.accept(xmlHeaderString + xmlBodyString, Constants.GIORNALE_ESITO_EVENTO.OK.toString());
    } catch (Exception e) {
      log.warn("paaSILInviaDovuti [REQUEST] impossible to insert in the event log", e);
    }

    dati.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString());
    if (ente == null)
      return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "codice IPA Ente [" + header.getCodIpaEnte() + "] non valido");

    boolean isStatoInserito = Utilities.checkIfStatoInserito(ente);
    if (isStatoInserito)
      return manageFault.apply(CODE_PAA_STATO_ENTE_NON_VALIDO, "Stato Ente Non Valido");

    Boolean passwordValidaPerEnte = enteService.verificaPassword(ente.getCodIpaEnte(), bodyrichiesta.getPassword());
    if (!passwordValidaPerEnte)
      return manageFault.apply(CODE_PAA_ENTE_NON_VALIDO, "Password non valida per ente [" + header.getCodIpaEnte() + "]");

    Dovuti dovuti = null;
    try {
      dovuti = jaxbTransformService.unmarshalling(bodyrichiesta.getDovuti(), Dovuti.class, "/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
    } catch (MyPayException e) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("XML ricevuto per paaSILInviaDovuti non conforme all' XSD per ente [" + header.getCodIpaEnte() + "]");
      buffer.append("XML Error: \n").append(e.getMessage());
      return manageFault.apply(PAA_XML_NON_VALIDO_CODE, "XML dei dovuti non valido",buffer.toString(),e);
    }

    // #45: Bonifica provincia pagatore case insensitive in import
    dovuti = bonificaProvinciaPagatore(dovuti);

    if (StringUtils.isNotBlank(dovuti.getDatiVersamento().getIdentificativoUnivocoVersamento())) {
      dati.setIdentificativoUnivocoVersamento(dovuti.getDatiVersamento().getIdentificativoUnivocoVersamento());
    }
    dati.setTipoVersamento(dovuti.getDatiVersamento().getTipoVersamento());

    List<CtDatiSingoloVersamentoDovuti> listaDovuti = dovuti.getDatiVersamento().getDatiSingoloVersamentos();

    String IUV = dovuti.getDatiVersamento().getIdentificativoUnivocoVersamento();
    if (StringUtils.isNotBlank(IUV)) {
      List<IdentificativoUnivoco> identificativoUnivocos = identificativoUnivocoService
        .getByEnteAndCodTipoIdAndId(ente.getMygovEnteId(), "IUV", IUV);
      if (identificativoUnivocos.size() > 0)
        return manageFault.apply(CODE_PAA_IUV_DUPLICATO, "IUV [" + IUV + "] già inviato");

      if (listaDovuti.size() > 1)
        return manageFault.apply(CODE_PAA_NUMERO_DOVUTI_PER_IUV_NON_VALIDO,
          "Numero dovuti [" + listaDovuti.size() + "] per lo stesso IUV [" + IUV + "] non valido");

      if (!Utilities.validaIUV(IUV, false, ente.getApplicationCode()))
        return manageFault.apply(CODE_PAA_IUV_NON_VALIDO,
          "IdentificativoUnivocoVersamento non valido [" + IUV + "] per ente [" + header.getCodIpaEnte() + "]");
    }

    dati.setTipoVersamento(dovuti.getDatiVersamento().getTipoVersamento());

    if (!Utilities.validaTipoVersamento(dati.getTipoVersamento()))
      return manageFault.apply(CODE_PAA_TIPO_VERSAMENTO_NON_VALIDO,"Tipo versamento non valido [" + dati.getTipoVersamento() + "]");

    // DEFAULT TIPO VERSAMENTO
    if (StringUtils.isBlank(dati.getTipoVersamento())) {
      dati.setTipoVersamento(Constants.ALL_PAGAMENTI);
    }

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
    log.debug("flag CF " + Constants.CODICE_FISCALE_ANONIMO + "[" + isFlagCfAnonimo + "]");
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

    CtSoggettoPagatore ctSoggettoPagatore = dovuti.getSoggettoPagatore();

    for (CtDatiSingoloVersamentoDovuti ctDatiSingoloVersamentoDovuti : listaDovuti) {

      String IUD = ctDatiSingoloVersamentoDovuti.getIdentificativoUnivocoDovuto();
      if (!Utilities.validaIUD(IUD))
        return manageFault.apply(CODE_PAA_IUD_NON_VALIDO,"IdentificativoUnivocoDovuto non valido [" + IUD + "]");

      List<IdentificativoUnivoco> identificativoUnivocos = identificativoUnivocoService
        .getByEnteAndCodTipoIdAndId(ente.getMygovEnteId(), "IUD", IUD);
      if (identificativoUnivocos.size() > 0)
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
        if (!importo.equals(enteTipoDovuto.getImporto()))
          return manageFault.apply(CODE_PAA_IMPORTO_MARCA_BOLLO_DIGITALE_NON_VALIDA,"importoSingoloVersamento non valido per tipo dovuto " + Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE);
      }

      String datiSpecificiRiscossione = ctDatiSingoloVersamentoDovuti.getDatiSpecificiRiscossione();
      if (!Utilities.validaDatiSpecificiRiscossione(datiSpecificiRiscossione))
        return manageFault.apply(CODE_PAA_DATI_SPECIFICI_RISCOSSIONE_NON_VALIDO,"DatiSpecificiRiscossione non valido [" + datiSpecificiRiscossione + "]");

      // CONTROLLO IMPORTO DOVUTO DIVERSO DA ZERO
      BigDecimal importoSingoloVersamento = ctDatiSingoloVersamentoDovuti.getImportoSingoloVersamento();
      if (importoSingoloVersamento.compareTo(BigDecimal.ZERO) == 0)
        return manageFault.apply(CODE_PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO,"ImportoSingoloVersamento non valido [" + importoSingoloVersamento + "]");

//			MODIFICHE ACCERTAMENTO PER CAPITOLI PREDETERMINATI
      Bilancio bilancio = ctDatiSingoloVersamentoDovuti.getBilancio();
      if (bilancio != null) {
        if (!Utilities.verificaImportoBilancio(bilancio, importoSingoloVersamento))
          return manageFault.apply(CODE_PAA_IMPORTO_BILANCIO_NON_VALIDO,"Importo bilancio non congruente per dovuto con importo [" + importoSingoloVersamento + "]");
      }
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
    registraGiornale.accept(paaSILInviaDovutiRisposta.getUrl(), Constants.GIORNALE_ESITO_EVENTO.OK.toString());

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
    response.setRequestToken(outcomeTo.getRequestToken());
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
      .build();
    WsExportFlussoOutcomeTo outcomeTo = prenotazioneFlussoService.handlePrenotazioneFlussoExport(incomeTo);
    var response = new PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta();
    response.setDateTo(Utilities.toXMLGregorianCalendar(outcomeTo.getDateTo()));
    response.setRequestToken(outcomeTo.getRequestToken());
    return response;
  }

  @Override
  public void paaSILRegistraPagamento(String codIpaEnte, String password, String identificativoUnivocoVersamento, String codiceContestoPagamento, BigDecimal singoloImportoPagato, XMLGregorianCalendar dataEsitoSingoloPagamento, Integer indiceDatiSingoloPagamento, String identificativoUnivocoRiscossione, String tipoIstitutoAttestante, String codiceIstitutiAttestante, String denominazioneAttestante, Holder<FaultBean> fault, Holder<String> esito) {

    log.info("Executing operation paaSILRegistraPagamento: IUV: " + identificativoUnivocoVersamento + " Ente: " + codIpaEnte);
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);

    // 1) LOG NEL GIORNALE DEGLI EVENTI
    Date dataOraEvento = new Date();
    String identificativoDominio = Optional.ofNullable(ente).map(Ente::getCodiceFiscaleEnte).orElse("");
    String identificativoPrestatoreServiziPagamento = "";
    String tipoVersamento = "";
    String componente = Constants.COMPONENTE_PA;
    String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
    String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILRegistraPagamento.toString();
    String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString();
    String identificativoFruitore = "NodoDeiPagamentiRVE";
    String identificativoErogatore = propIdStazioneIntermediarioPa;
    String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
    String canalePagamento = "";
    String parametriSpecificiInterfaccia = "";
    if (null == codiceContestoPagamento || codiceContestoPagamento.isEmpty()) {
      codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
    }

    String body = "";

    if (StringUtils.isNotBlank(codIpaEnte)) {
      body = body + " codIpaEnte: " + codIpaEnte
        + " identificativoUnivocoVersamento: " + identificativoUnivocoVersamento
        + " codiceContestoPagamento: " + codiceContestoPagamento
        + " singoloImportoPagato: " + singoloImportoPagato
        + " dataEsitoSingoloPagamento: " + dataEsitoSingoloPagamento
        + " indiceDatiSingoloPagamento: " + indiceDatiSingoloPagamento
        + " identificativoUnivocoRiscossione: " + identificativoUnivocoRiscossione
        + " tipoIstitutoAttestante: " + tipoIstitutoAttestante
        + " codiceIstitutiAttestante: " + codiceIstitutiAttestante
        + " denominazioneAttestante: " + denominazioneAttestante;
    }

    parametriSpecificiInterfaccia = body;
    log.debug("Body richiesta: " + body);
    esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

    giornalePaService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
      codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
      categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
      identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito.toString());

    sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();

    if (ente == null) {
      log.error("paaSILRegistraPagamento: Ente non valido: " + codIpaEnte);

      parametriSpecificiInterfaccia = "paaSILRegistraPagamento: Ente non valido: " + codIpaEnte;
      esito.value = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
      giornalePaService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
        codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
        categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
        identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito.toString());

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_ENTE_NON_VALIDO,
        "codice IPA Ente [" + codIpaEnte + "] non valido", null);
      fault.value = faultBean;
      log.info("paaSILRegistraPagamento: esito = " + esito.value);
      return;
    }

    boolean isStatoInserito = Utilities.checkIfStatoInserito(ente);

    if (isStatoInserito) {
      log.error("paaSILRegistraPagamento: Ente non inserito: " + codIpaEnte);

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_STATO_ENTE_NON_VALIDO, "Stato Ente Non Valido", null);
      faultBean.setSerial(0);

      parametriSpecificiInterfaccia = faultBean.getDescription();

      esito.value = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
      fault.value = faultBean;

      giornalePaService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
        codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
        categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
        identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito.toString());
      log.info("paaSILRegistraPagamento: esito = " + esito.value);
      return;

    }

    Boolean passwordValidaPerEnte = enteService.verificaPassword(ente.getCodIpaEnte(), password);
    if (!passwordValidaPerEnte) {
      log.error("paaSILRegistraPagamento: Password non valida per ente: " + codIpaEnte);

      parametriSpecificiInterfaccia = "paaSILRegistraPagamento: Password non valida per ente: "
        + codIpaEnte;
      esito.value = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
      giornalePaService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
        codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
        categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
        identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito.toString());

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_ENTE_NON_VALIDO,
        "Password non valida per ente [" + codIpaEnte + "]", null);

      fault.value = faultBean;
      log.info("paaSILRegistraPagamento: esito = " + esito.value);
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

    List<Dovuto> listaDovuti = new ArrayList<Dovuto>();
    //Differenzio la ricerca per modello
    //Cerco dovuto in mygov_dovuto dato IUV e codIpa
    if (Utilities.isAvviso(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento()))
      listaDovuti = dovutoService.getByIuvEnte(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte());
    else {
      Carrello carrelloDatoIuv = new Carrello();
      carrelloDatoIuv = carrelloService.getByIdDominioIUV(ente.getCodiceFiscaleEnte(), datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
      if (null != carrelloDatoIuv)
        listaDovuti = dovutoService.getDovutiInCarrello(carrelloDatoIuv.getMygovCarrelloId());
    }

    if (!CollectionUtils.isEmpty(listaDovuti)) {
      log.info("paaSILRegistraPagamento: dovuto presente in mygovDovuto");
      Dovuto dovuto = listaDovuti.get(0);
      Carrello carrello = new Carrello();
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
        DovutoElaborato dovutoElaboratoCompletato = dovutoElaboratoListCompletato.get(0);
        carrello = dovutoElaboratoCompletato.getMygovCarrelloId();
        if (null != carrello) {
          log.info("paaSILRegistraPagamento: esiste carrello per dovuto");
          AnagraficaStato anagCarrello = dovutoElaboratoCompletato.getMygovCarrelloId().getMygovAnagraficaStatoId();
          //Controllo lo stato del pagamento, se lo stato del dovuto è pagato non faccio nulla,
          //altrimenti setto lo stato a "PAGATO" ed aggiorno
          if (!(anagCarrello.getCodStato().equalsIgnoreCase(Constants.STATO_CARRELLO_PAGATO))) {
            //Diverso da PAGATO
            carrello = dovutoElaboratoCompletato.getMygovCarrelloId();
            carrello = carrelloService.aggiornaCarrelloInPagato(datiRendicontazioneCod9, carrello, ente, dovuto);
            carrello = carrelloService.upsert(carrello);
            //Aggiorno i dati del dovutoElaborato per allinearli a quello di uno in stato COMPLETATo e PAGATO
            fillDovutoElaboratoCompletato(datiRendicontazioneCod9, dovuto, carrello, dovutoElaboratoCompletato);
            dovutoElaboratoService.upsert(dovutoElaboratoCompletato);
          }
        } else {
          log.info("paaSILRegistraPagamento: non esiste carrello per dovuto");
          //dovuto elaborato COMPLETATO senza carrello
          Carrello carrelloFittizio = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, dovuto, ente);
          carrello = carrelloService.upsert(carrelloFittizio);
          //Aggiorno i dati del dovutoElaborato per allinearli a quello di uno in stato COMPLETATo e PAGATO
          fillDovutoElaboratoCompletato(datiRendicontazioneCod9, dovuto, carrello, dovutoElaboratoCompletato);
          dovutoElaboratoService.upsert(dovutoElaboratoCompletato);
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
          DovutoElaborato dovutoElaboratoNoCompletato = dovutoElaboratoListNoCompletato.get(0);
          carrello = dovutoElaboratoNoCompletato.getMygovCarrelloId();
          if (null == carrello) {
            log.info("paaSILRegistraPagamento: non esiste carrello per dovuto");
            //crea carrello fittizio
            Carrello carrelloFittizio = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, dovuto, ente);
            carrello = carrelloService.upsert(carrelloFittizio);
            dovutoElaboratoNoCompletato.setMygovCarrelloId(carrello);
          } else {
            log.info("paaSILRegistraPagamento: esiste carrello per dovuto");
            carrello = carrelloService.aggiornaCarrelloInPagato(datiRendicontazioneCod9, carrello, ente, dovuto);
            carrello = carrelloService.upsert(carrello);
          }
          dovuto.setMygovCarrelloId(carrello);
          dovutoService.upsert(dovuto);
          dovutoElaboratoService.elaborateDovutoNoCompletato(dovuto.getMygovDovutoId(), dovutoElaboratoNoCompletato, dovuto.getMygovAnagraficaStatoId().getCodStato(), Constants.STATO_DOVUTO_COMPLETATO, ente, datiRendicontazioneCod9);

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
          dovutoElaboratoService.insert(dovuto.getMygovDovutoId(), dovuto.getMygovAnagraficaStatoId().getCodStato(), Constants.STATO_DOVUTO_COMPLETATO, ente, datiRendicontazioneCod9);
        }
      }
      carrelloService.deleteDovutoCarrello(dovuto.getMygovDovutoId(), carrello.getMygovCarrelloId());
      dovutoService.delete(dovuto);
      esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
      log.info("paaSILRegistraPagamento: esito = "+ esito.value);
      return;
    }
    else {//Non è presente in mygov_dovuto, cerco dovuto in mygov_dovuto_elaborato dato IUV e codIpa con stato COMPLETATO
      log.info("paaSILRegistraPagamento: dovuto non presente in mygovDovuto");
      List<DovutoElaborato> dovutoElaboratoListCompletato = dovutoElaboratoService.getByIuvEnteStato(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte(), Constants.STATO_DOVUTO_COMPLETATO, true, null);
      if (!CollectionUtils.isEmpty(dovutoElaboratoListCompletato)) {
        log.info("paaSILRegistraPagamento: dovuto presente in mygovDovuto in stato completato");
        DovutoElaborato dovutoElaboratoCompletato = dovutoElaboratoListCompletato.get(0);
        Carrello carrello = dovutoElaboratoCompletato.getMygovCarrelloId();
        if (null != carrello) {
          log.info("paaSILRegistraPagamento: esiste carrello per dovuto");
          AnagraficaStato anagCarrello = dovutoElaboratoCompletato.getMygovCarrelloId().getMygovAnagraficaStatoId();
          //Controllo lo stato del pagamento, se lo stato del dovuto è pagato non faccio nulla,
          //altrimenti setto lo stato a "PAGATO" ed aggiorno
          if(!(anagCarrello.getCodStato().equalsIgnoreCase(Constants.STATO_CARRELLO_PAGATO))) {
            //Diverso da PAGATO
            AnagraficaStato anagStatoCarrelloPagato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PAGATO, Constants.STATO_TIPO_CARRELLO);
            carrello = dovutoElaboratoCompletato.getMygovCarrelloId();
            carrello.setMygovAnagraficaStatoId(anagStatoCarrelloPagato);
            carrelloService.upsert(carrello);
            //Aggiorno i dati del dovutoElaborato per allinearli a quello di uno in stato COMPLETATo e PAGATO
            fillDovutoElaboratoWithNoDataEsito(datiRendicontazioneCod9, dovutoElaboratoCompletato, carrello);
            dovutoElaboratoService.upsert(dovutoElaboratoCompletato);
          }
        }else {
          log.info("paaSILRegistraPagamento: non esiste carrello per dovuto");
          //dovuto elaborato COMPLETATO senza carrello
          Carrello carrelloFittizio = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, null, ente);
          carrello = carrelloService.upsert(carrelloFittizio);
          //Aggiorno i dati del dovutoElaborato per allinearli a quello di uno in stato COMPLETATo e PAGATO
          fillDovutoElaboratoWithNoDataEsito(datiRendicontazioneCod9, dovutoElaboratoCompletato, carrello);
          dovutoElaboratoService.upsert(dovutoElaboratoCompletato);
        }
        esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
        log.info("paaSILRegistraPagamento: esito = "+ esito.value);
        return;
      }
      else {//Non è presente in mygov_dovuto e non è presente in mygov_dovuto_elaborato con stato COMPLETATO,
        //cerco dovuto in mygov_dovuto_elaborato dato IUV e codIpa con stato diverso da COMPLETATO

        List<DovutoElaborato> dovutoElaboratoListNoCompletato = dovutoElaboratoService.getByIuvEnteStato(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento(), ente.getCodIpaEnte(), Constants.STATO_DOVUTO_COMPLETATO, false, null);
        log.info("paaSILRegistraPagamento: dovuto presente in mygovDovuto in stato non completato");
        if (!CollectionUtils.isEmpty(dovutoElaboratoListNoCompletato)) {
          DovutoElaborato dovutoElaboratoNoCompletato = dovutoElaboratoListNoCompletato.get(0);
          Carrello carrello = dovutoElaboratoNoCompletato.getMygovCarrelloId();
          if (null == carrello) {
            log.info("paaSILRegistraPagamento: non esiste carrello per dovuto");
            //crea carrello fittizio
            Carrello carrelloFittizio = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, null, ente);
            carrello = carrelloService.upsert(carrelloFittizio);
            dovutoElaboratoNoCompletato.setMygovCarrelloId(carrello);
          } else {
            log.info("paaSILRegistraPagamento: esiste carrello per dovuto");
            carrello = carrelloService.aggiornaCarrelloInPagato(datiRendicontazioneCod9, carrello, ente, null);
            carrello = carrelloService.upsert(carrello);
          }
          dovutoElaboratoService.elaborateDovutoNoCompletato(dovutoElaboratoNoCompletato, Constants.STATO_DOVUTO_COMPLETATO, ente, datiRendicontazioneCod9);
          esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
          log.info("paaSILRegistraPagamento: esito = " + esito.value);
          return;

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
            log.error("paaSILRegistraPagamento errore nel recupero del tipo dovuto: [" + e.getMessage() + "]");

            parametriSpecificiInterfaccia = "paaSILRegistraPagamento errore nel recupero del tipo dovuto: [" + e.getMessage() + "]";
            esito.value = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
            giornalePaService.registraEvento(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento,
              codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
              categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito.toString());
            log.error("paaSILRegistraPagamento: esito = " + esito.value);
            throw new RuntimeException("paaSILRegistraPagamento errore nel recupero del tipo dovuto: [" + e.getMessage() + "]");
          }

          Carrello carrello = carrelloService.getCarrelloFittizio(datiRendicontazioneCod9, null, ente);
          carrello = carrelloService.upsert(carrello);
          DovutoElaborato dovutoElaborato = dovutoElaboratoService.getFittizio(carrello, datiRendicontazioneCod9, ente);
          dovutoElaborato = dovutoElaboratoService.upsert(dovutoElaborato);
          esito.value = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
          log.info("paaSILRegistraPagamento: esito = " + esito.value);
          return;
        }
      }
    }
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
      var optionalEtd = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), codIpaEnte, false);
      if (optionalEtd.map(e -> !e.isFlgAttivo()).orElse(optionalEtd.isEmpty())) {
        dovutiAvviso.remove(dovuto);
        //dovutiAttivi.add(dovuto);
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
      if (optionalEtd.get().isFlgScadenzaObbligatoria()) {
        LocalDate dataScadenza = new LocalDate(dovuto.getDtRpDatiVersDataEsecuzionePagamento());
        if (dataScadenza.isBefore(new LocalDate())) {
          log.error("Error dovuto scaduto per IUV [%s] ed ente [%s]", codIuv, codIpaEnte);
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

  private Dovuti bonificaProvinciaPagatore(Dovuti dovuti) {
    log.debug("bonificaProvinciaPagatore");
    if (dovuti != null && dovuti.getSoggettoPagatore() != null) {
      String provinciaPagatore = dovuti.getSoggettoPagatore().getProvinciaPagatore();
      log.debug("provincia pagatore: %s", provinciaPagatore);
      // provincia pagatore deve essere upperCase
      if (provinciaPagatore != null) {
        dovuti.getSoggettoPagatore().setProvinciaPagatore(provinciaPagatore.toUpperCase());
        log.debug("provincia pagatore: %s", dovuti.getSoggettoPagatore().getProvinciaPagatore());
      }
    }
    return dovuti;
  }

  private void fillDovutoElaboratoCompletato(DatiRendicontazioneCod9 datiRendicontazioneCod9, Dovuto dovuto, Carrello carrello, DovutoElaborato dovutoElaboratoCompletato) {
    dovutoElaboratoCompletato.setMygovCarrelloId(carrello);
    dovutoElaboratoCompletato.setCodEDatiPagCodiceEsitoPagamento(Constants.CODICE_ESITO_PAGAMENTO_ESEGUITO.charAt(0));
    dovutoElaboratoCompletato.setNumEDatiPagImportoTotalePagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaboratoCompletato.setNumEDatiPagDatiSingPagSingoloImportoPagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaboratoCompletato.setDeEDatiPagDatiSingPagEsitoSingoloPagamento(Constants.PAGATO_CON_RENDICONTAZIONE_9);
    dovutoElaboratoCompletato.setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(datiRendicontazioneCod9.getDataEsitoSingoloPagamento());
    dovutoElaboratoCompletato.setCodEDatiPagDatiSingPagIdUnivocoRiscoss(datiRendicontazioneCod9.getIdentificativoUnivocoRiscossione());
    String causale = Utilities.getDefaultString().andThen(Utilities.getTruncatedAt(Constants.MAX_LENGHT_CAUSALE)).apply(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(), Constants.CAUSALE_DOVUTO_PAGATO);
    dovutoElaboratoCompletato.setDeEDatiPagDatiSingPagCausaleVersamento(causale);
    dovutoElaboratoCompletato.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(StringUtils.defaultIfBlank(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(), Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW));
    dovutoElaboratoCompletato.setDeRpDatiVersDatiSingVersCausaleVersamentoAgid(causale);
  }

  private void fillDovutoElaboratoWithNoDataEsito(DatiRendicontazioneCod9 datiRendicontazioneCod9, DovutoElaborato dovutoElaboratoCompletato, Carrello carrello) {
    dovutoElaboratoCompletato.setMygovCarrelloId(carrello);
    dovutoElaboratoCompletato.setCodEDatiPagCodiceEsitoPagamento(Constants.CODICE_ESITO_PAGAMENTO_ESEGUITO.charAt(0));
    dovutoElaboratoCompletato.setNumEDatiPagImportoTotalePagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaboratoCompletato.setNumEDatiPagDatiSingPagSingoloImportoPagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaboratoCompletato.setDeEDatiPagDatiSingPagEsitoSingoloPagamento(Constants.PAGATO_CON_RENDICONTAZIONE_9);
    dovutoElaboratoCompletato.setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(datiRendicontazioneCod9.getDataEsitoSingoloPagamento());
    dovutoElaboratoCompletato.setCodEDatiPagDatiSingPagIdUnivocoRiscoss(datiRendicontazioneCod9.getIdentificativoUnivocoRiscossione());
    dovutoElaboratoCompletato.setDeEDatiPagDatiSingPagCausaleVersamento(Constants.CAUSALE_DOVUTO_PAGATO);
    dovutoElaboratoCompletato.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW);
    dovutoElaboratoCompletato.setDeRpDatiVersDatiSingVersCausaleVersamentoAgid(Constants.CAUSALE_DOVUTO_PAGATO);
  }
}
