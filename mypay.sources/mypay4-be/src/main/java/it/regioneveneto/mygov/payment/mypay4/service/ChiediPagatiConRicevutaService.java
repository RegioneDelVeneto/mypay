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

import it.regioneveneto.mygov.payment.mypay4.dto.BasketTo;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.DovutoUtils;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.helper.PagamentiTelematiciDovutiPagatiHelper;
import it.veneto.regione.pagamenti.ente.FaultBean;
import it.veneto.regione.schemas._2012.pagamenti.ente.PagatiConRicevuta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.Holder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.*;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class ChiediPagatiConRicevutaService {


  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String propIdStazioneIntermediarioPa;

  @Autowired
  private EnteService enteService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private DovutoService dovutoService;

  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;

  @Autowired
  private CarrelloService carrelloService;

  @Autowired
  private GiornaleService giornaleService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private JAXBTransformService jaxbTransformService;

  @Autowired
  private StorageService storageService;

  public void paaSILChiediPagatiConRicevuta(String codIpaEnte, String password, String idSession, String identificativoUnivocoVersamento, String identificativoUnivocoDovuto, Holder<FaultBean> fault, Holder<DataHandler> pagati, Holder<String> tipoFirma, Holder<DataHandler> rt) {
    log.info("Executing operation paaSILChiediPagatiConRicevuta");

    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
    if (ente == null) {
      log.error("paaSILChiediPagatiConRicevuta: Ente non valido: " + codIpaEnte);

      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte,
          CODE_PAA_ENTE_NON_VALIDO, "codice IPA Ente [" + codIpaEnte + "] non valido o password errata",
          null);

      fault.value = faultBean;
      return;
    }

    Boolean passwordValidaPerEnte = enteService.verificaPassword(ente.getCodIpaEnte(), password);
    if (!passwordValidaPerEnte) {
      log.error("paaSILChiediPagatiConRicevuta: Password non valida per ente: " + ente.getCodIpaEnte());

      FaultBean faultBean = VerificationUtils.getFaultBean(ente.getCodIpaEnte(),
          CODE_PAA_ENTE_NON_VALIDO, "Password non valida per ente [" + ente.getCodIpaEnte() + "]", null);

      fault.value = faultBean;
      return;
    }

    List<String> listaParametri = new ArrayList<>();

    if (StringUtils.isNotBlank(idSession)) {
      listaParametri.add(idSession);
    }
    if (StringUtils.isNotBlank(identificativoUnivocoVersamento)) {
      listaParametri.add(identificativoUnivocoVersamento);
    }
    if (StringUtils.isNotBlank(identificativoUnivocoDovuto)) {
      listaParametri.add(identificativoUnivocoDovuto);
    }

    if (listaParametri.size() > 1) {
      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR,
          "Errore, è stato specificato più di un paramero tra idSession, identificativoUnivocoVersamento e identificativoUnivocoDovuto.");
      fault.value = faultBean;
      return;
    }

    if (StringUtils.isNotBlank(idSession)) {

      PagatiConRicevuta pagatiConRicevutaDocument = null;

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
        String identificativoUnivocoVersamentoGiornale = carrello.getCodRpDatiVersIdUnivocoVersamento() != null
            ? carrello.getCodRpDatiVersIdUnivocoVersamento() : "";
        String codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
            ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
        String identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp() != null
            ? carrello.getCodRpSilinviarpIdPsp() : "";
        String tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
        String componente = Constants.COMPONENTE_FESP;
        String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
        String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
        String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
        String identificativoFruitore = Constants.SIL;
        String identificativoErogatore = ente.getCodiceFiscaleEnte();
        String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
        String canalePagamento = "";
        String parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_ANNULLATO
            + " Pagamento annullato per idSession specificato [" + idSession + "]";
        String esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

        giornaleService.registraEvento(dataOraEvento, identificativoDominio,
            identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
            identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
            tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
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
        String identificativoUnivocoVersamentoGiornale = carrello.getCodRpDatiVersIdUnivocoVersamento() != null
            ? carrello.getCodRpDatiVersIdUnivocoVersamento() : "";
        String codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
            ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
        String identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp() != null
            ? carrello.getCodRpSilinviarpIdPsp() : "";
        String tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
        String componente = Constants.COMPONENTE_FESP;
        String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
        String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
        String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
        String identificativoFruitore = Constants.SIL;
        String identificativoErogatore = ente.getCodiceFiscaleEnte();
        String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
        String canalePagamento = "";
        String parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_SCADUTO
            + " Pagamento scaduto per idSession specificato [" + idSession + "]";
        String esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

        giornaleService.registraEvento(dataOraEvento, identificativoDominio,
            identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
            identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
            tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
            identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);

        return;
      }

      if (statoCarrello.equals(Constants.STATO_CARRELLO_PAGATO)
          || statoCarrello.equals(Constants.STATO_CARRELLO_NON_PAGATO)
          || statoCarrello.equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
          || statoCarrello.equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE)) {

        List<DovutoElaborato> listaDovuti = dovutoElaboratoService.getByCarrello(carrello);

        pagatiConRicevutaDocument = PagamentiTelematiciDovutiPagatiHelper.creaPagatiDocumentConRicevuta(carrello, listaDovuti);

        String pagatiConRicevutaString = jaxbTransformService.marshalling(pagatiConRicevutaDocument, PagatiConRicevuta.class);

        log.debug("PAGATI CON RICEVUTA per dominio [" + pagatiConRicevutaDocument.getDominio().getIdentificativoDominio()
            + "] e IUV [" + pagatiConRicevutaDocument.getDatiPagamento() .getIdentificativoUnivocoVersamento() + "]: " + pagatiConRicevutaString);

        // se usi soapUI
        // DataHandler pagatiValue = new DataHandler(new
        // ByteArrayDataSource(pagatiString.getBytes(),
        // "application/octet-stream"));

        byte[] encodedPagatiConRicevuta;
        encodedPagatiConRicevuta = Base64.encodeBase64(pagatiConRicevutaString.getBytes(StandardCharsets.UTF_8));
        DataHandler pagatiConRicevutaValue = new DataHandler(
            new ByteArrayDataSource(encodedPagatiConRicevuta, "application/octet-stream"));

        pagati.value = pagatiConRicevutaValue;

        if (listaDovuti.size() > 0) {
          DovutoElaborato pagato = listaDovuti.get(0);

          tipoFirma.value = pagato.getDeRtInviartTipoFirma();

          DataHandler rtValue = new DataHandler(
              new ByteArrayDataSource(pagato.getBlbRtPayload(), "application/octet-stream"));
          rt.value = rtValue;

        } else {
          log.error(
              "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
          throw new RuntimeException(
              "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
        }

        // 1) LOG NEL GIORNALE DEGLI EVENTI - chiedi pagati
        Date dataOraEvento = new Date();
        String identificativoDominio = ente.getCodiceFiscaleEnte();
        String identificativoUnivocoVersamentoGiornale = carrello.getCodRpDatiVersIdUnivocoVersamento();
        String codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
            ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
        String identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
        String tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
        String componente = Constants.COMPONENTE_FESP;
        String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
        String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
        String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
        String identificativoFruitore = Constants.SIL;
        String identificativoErogatore = ente.getCodiceFiscaleEnte();
        String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
        String canalePagamento = "";
        String parametriSpecificiInterfaccia = pagatiConRicevutaString;
        String esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

        giornaleService.registraEvento(dataOraEvento, identificativoDominio,
            identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
            identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
            tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
            identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
            esito);

        return;
      } else {
        FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR,
            "Errore interno per idSession: " + idSession);
        fault.value = faultBean;
        return;
      }
    } else if (StringUtils.isNotBlank(identificativoUnivocoVersamento)) {
      // LOG giornale degli eventi
      Date dataOraEvento = new Date();
      String identificativoDominio = ente.getCodiceFiscaleEnte();
      String identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
      String codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
      String identificativoPrestatoreServiziPagamento = "";
      String tipoVersamento = "";
      String componente = Constants.COMPONENTE_FESP;
      String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
      String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
      String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString();
      String identificativoFruitore = Constants.SIL;
      String identificativoErogatore = ente.getCodiceFiscaleEnte();
      String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      String canalePagamento = "";
      String parametriSpecificiInterfaccia = "Parametri di input ente [ " + ente.getCodIpaEnte() + " ], IUV [ "
          + identificativoUnivocoVersamento + " ]";
      String esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

      giornaleService.registraEvento(dataOraEvento, identificativoDominio,
          identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
          identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento,
          sottoTipoEvento, identificativoFruitore, identificativoErogatore,
          identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);

      List<Dovuto> listaDovutiPagabili = dovutoService.getByIuvEnte(identificativoUnivocoVersamento, ente.getCodIpaEnte());
      if (CollectionUtils.isEmpty(listaDovutiPagabili)) {
        List<DovutoElaborato> listaDovutiElaborati = dovutoElaboratoService.getByIuvEnte(identificativoUnivocoVersamento, ente.getCodIpaEnte());
        if (!CollectionUtils.isEmpty(listaDovutiElaborati)) {
          // UNA RIGA
          if (listaDovutiElaborati.size() == 1) {
            DovutoElaborato dovutoElaborato = listaDovutiElaborati.get(0);
            AnagraficaStato anagraficaStato = dovutoElaborato.getMygovAnagraficaStatoId();
            // SCADUTO
            if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
                && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_SCADUTO_ELABORATO)) {
              FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO,
                  "Pagamento scaduto per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                      + identificativoUnivocoVersamento + "]",
                  null);
              fault.value = faultBean;

              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
              codiceContestoPagamento = dovutoElaborato.getCodRpSilinviarpCodiceContestoPagamento();
              identificativoPrestatoreServiziPagamento = dovutoElaborato.getCodRpSilinviarpIdPsp();
              tipoVersamento = dovutoElaborato.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_SCADUTO
                  + " Pagamento scaduto per per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                  + identificativoUnivocoVersamento + "]";
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
            // ABORTITO
            if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
                && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_ABORT)) {
              FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_ANNULLATO,
                  "Pagamento abortito per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                      + identificativoUnivocoVersamento + "]", null);
              fault.value = faultBean;

              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
              codiceContestoPagamento = dovutoElaborato.getCodRpSilinviarpCodiceContestoPagamento();
              identificativoPrestatoreServiziPagamento = dovutoElaborato.getCodRpSilinviarpIdPsp();
              tipoVersamento = dovutoElaborato.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_ANNULLATO
                  + " Pagamento abortito per per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                  + identificativoUnivocoVersamento + "]";
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
            // RT PRESENTE
            Carrello carrello = dovutoElaborato.getMygovCarrelloId();
            AnagraficaStato anagraficaStatoCarrello = carrello.getMygovAnagraficaStatoId();
            if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                && (anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_PAGATO)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_NON_PAGATO)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE))) {

              PagatiConRicevuta pagatiConRicevutaDocument =
                  PagamentiTelematiciDovutiPagatiHelper.creaPagatiDocumentConRicevuta(carrello, listaDovutiElaborati);

              String pagatiConRicevutaString = jaxbTransformService.marshalling(pagatiConRicevutaDocument, PagatiConRicevuta.class);

              log.debug("PAGATI CON RICEVUTA per dominio [" + pagatiConRicevutaDocument.getDominio().getIdentificativoDominio()
                  + "] e IUV [" + pagatiConRicevutaDocument.getDatiPagamento().getIdentificativoUnivocoVersamento()
                  + "]: " + pagatiConRicevutaString);

              // se usi soapUI
              // DataHandler pagatiValue = new DataHandler(new
              // ByteArrayDataSource(pagatiString.getBytes(),
              // "application/octet-stream"));

              byte[] encodedPagatiConRicevuta;
              encodedPagatiConRicevuta = Base64.encodeBase64(pagatiConRicevutaString.getBytes(StandardCharsets.UTF_8));
              DataHandler pagatiConRicevutaValue = new DataHandler(
                  new ByteArrayDataSource(encodedPagatiConRicevuta, "application/octet-stream"));

              pagati.value = pagatiConRicevutaValue;

              if (listaDovutiElaborati.size() > 0) {
                DovutoElaborato pagato = listaDovutiElaborati.get(0);

                tipoFirma.value = pagato.getDeRtInviartTipoFirma();

                DataHandler rtValue = new DataHandler(new ByteArrayDataSource(
                    pagato.getBlbRtPayload(), "application/octet-stream"));
                rt.value = rtValue;

              } else {
                log.error(
                    "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
                throw new RuntimeException(
                    "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
              }

              // 1) LOG NEL GIORNALE DEGLI EVENTI - chiedi
              // pagati
              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = carrello
                  .getCodRpDatiVersIdUnivocoVersamento();
              codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
                  ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
              identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
              tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = pagatiConRicevutaString;
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
          } else if (listaDovutiElaborati.size() > 1 && listaDovutiElaborati.size() <= 5
              && DovutoUtils.isAllDovutiElaboratiNelloStessoCarrello(listaDovutiElaborati)) {
            // DOVUTI > 1 E <= 5 TUTTI DELLO STESSO CARRELLO

            Carrello carrello = listaDovutiElaborati.get(0).getMygovCarrelloId();
            AnagraficaStato anagraficaStatoCarrello = carrello.getMygovAnagraficaStatoId();

            if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                && anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_DOVUTO_SCADUTO_ELABORATO)) {
              FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO,
                  "Carrello scaduto per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                      + identificativoUnivocoVersamento + "]",
                  null);
              fault.value = faultBean;

              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
              codiceContestoPagamento = carrello.getCodRpSilinviarpCodiceContestoPagamento();
              identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
              tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_SCADUTO
                  + " Carrello scaduto per per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                  + identificativoUnivocoVersamento + "]";
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
            // ABORTITO
            if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                && anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_ABORT)) {
              FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_ANNULLATO,
                  "Carrello abortito per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                      + identificativoUnivocoVersamento + "]",
                  null);
              fault.value = faultBean;

              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
              codiceContestoPagamento = carrello.getCodRpSilinviarpCodiceContestoPagamento();
              identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
              tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_ANNULLATO
                  + " Carrello abortito per per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                  + identificativoUnivocoVersamento + "]";
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
            // RT PRESENTE
            if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                && (anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_PAGATO)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_NON_PAGATO)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE))) {

              PagatiConRicevuta pagatiConRicevutaDocument =
                  PagamentiTelematiciDovutiPagatiHelper.creaPagatiDocumentConRicevuta(carrello, listaDovutiElaborati);

              String pagatiConRicevutaString = jaxbTransformService.marshalling(pagatiConRicevutaDocument, PagatiConRicevuta.class);

              log.debug("PAGATI CON RICEVUTA per dominio [" + pagatiConRicevutaDocument.getDominio().getIdentificativoDominio()
                  + "] e IUV [" + pagatiConRicevutaDocument.getDatiPagamento().getIdentificativoUnivocoVersamento()
                  + "]: " + pagatiConRicevutaString);

              // se usi soapUI
              // DataHandler pagatiValue = new DataHandler(new
              // ByteArrayDataSource(pagatiString.getBytes(),
              // "application/octet-stream"));

              byte[] encodedPagatiConRicevuta;
              encodedPagatiConRicevuta = Base64.encodeBase64(pagatiConRicevutaString.getBytes(StandardCharsets.UTF_8));
              DataHandler pagatiConRicevutaValue = new DataHandler(
                  new ByteArrayDataSource(encodedPagatiConRicevuta, "application/octet-stream"));

              pagati.value = pagatiConRicevutaValue;

              if (listaDovutiElaborati.size() > 0) {
                DovutoElaborato pagato = listaDovutiElaborati.get(0);

                tipoFirma.value = pagato.getDeRtInviartTipoFirma();

                DataHandler rtValue = new DataHandler(new ByteArrayDataSource(
                    pagato.getBlbRtPayload(), "application/octet-stream"));
                rt.value = rtValue;

              } else {
                log.error(
                    "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
                throw new RuntimeException(
                    "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
              }

              // 1) LOG NEL GIORNALE DEGLI EVENTI - chiedi
              // pagati
              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = carrello
                  .getCodRpDatiVersIdUnivocoVersamento();
              codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
                  ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
              identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
              tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = pagatiConRicevutaString;
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
          } else if (listaDovutiElaborati.size() > 1 && !DovutoUtils.isAllDovutiElaboratiNelloStessoCarrello(listaDovutiElaborati)) {
            // N DOVUTI NON TUTTI SULLO STESSO CARRELLO

            DovutoElaborato lastDovutoElaborato = DovutoUtils.getLastDovutoElaboratoOrPositivo(listaDovutiElaborati);
            Carrello carrello = lastDovutoElaborato.getMygovCarrelloId();
            List<DovutoElaborato> listaLastDovutoElaborato = new ArrayList<DovutoElaborato>();
            listaLastDovutoElaborato.add(lastDovutoElaborato);
            if (carrello != null) {
              AnagraficaStato anagraficaStatoCarrello = carrello.getMygovAnagraficaStatoId();

              if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                  && anagraficaStatoCarrello.getCodStato()
                  .equals(Constants.STATO_DOVUTO_SCADUTO_ELABORATO)) {
                FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO,
                    "Carrello scaduto per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                        + identificativoUnivocoVersamento + "]",
                    null);
                fault.value = faultBean;

                dataOraEvento = new Date();
                identificativoDominio = ente.getCodiceFiscaleEnte();
                identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
                codiceContestoPagamento = carrello.getCodRpSilinviarpCodiceContestoPagamento();
                identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
                tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
                componente = Constants.COMPONENTE_FESP;
                categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
                tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
                sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
                identificativoFruitore = Constants.SIL;
                identificativoErogatore = ente.getCodiceFiscaleEnte();
                identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
                canalePagamento = "";
                parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_SCADUTO
                    + " Carrello scaduto per per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                    + identificativoUnivocoVersamento + "]";
                esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

                giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                    identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                    identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                    categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                    identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                    parametriSpecificiInterfaccia, esito);

                return;
              }
              // ABORTITO
              if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                  && anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_ABORT)) {
                FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_ANNULLATO,
                    "Carrello abortito per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                        + identificativoUnivocoVersamento + "]",
                    null);
                fault.value = faultBean;

                dataOraEvento = new Date();
                identificativoDominio = ente.getCodiceFiscaleEnte();
                identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
                codiceContestoPagamento = carrello.getCodRpSilinviarpCodiceContestoPagamento();
                identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
                tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
                componente = Constants.COMPONENTE_FESP;
                categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
                tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
                sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
                identificativoFruitore = Constants.SIL;
                identificativoErogatore = ente.getCodiceFiscaleEnte();
                identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
                canalePagamento = "";
                parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_ANNULLATO
                    + " Carrello abortito per per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                    + identificativoUnivocoVersamento + "]";
                esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

                giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                    identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                    identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                    categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                    identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                    parametriSpecificiInterfaccia, esito);

                return;
              }
              // RT PRESENTE
              if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                  && (anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_PAGATO)
                  || anagraficaStatoCarrello.getCodStato()
                  .equals(Constants.STATO_CARRELLO_NON_PAGATO)
                  || anagraficaStatoCarrello.getCodStato()
                  .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
                  || anagraficaStatoCarrello.getCodStato()
                  .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE))) {

                PagatiConRicevuta pagatiConRicevutaDocument =
                    PagamentiTelematiciDovutiPagatiHelper.creaPagatiDocumentConRicevuta(carrello, listaLastDovutoElaborato);

                String pagatiConRicevutaString = jaxbTransformService.marshalling(pagatiConRicevutaDocument, PagatiConRicevuta.class);

                log.debug("PAGATI CON RICEVUTA per dominio [" + pagatiConRicevutaDocument.getDominio().getIdentificativoDominio()
                    + "] e IUV [" + pagatiConRicevutaDocument.getDatiPagamento().getIdentificativoUnivocoVersamento()
                    + "]: " + pagatiConRicevutaString);

                // se usi soapUI
                // DataHandler pagatiValue = new
                // DataHandler(new
                // ByteArrayDataSource(pagatiString.getBytes(),
                // "application/octet-stream"));

                byte[] encodedPagatiConRicevuta;
                encodedPagatiConRicevuta = Base64.encodeBase64(pagatiConRicevutaString.getBytes(StandardCharsets.UTF_8));
                DataHandler pagatiConRicevutaValue = new DataHandler(new ByteArrayDataSource(
                    encodedPagatiConRicevuta, "application/octet-stream"));

                pagati.value = pagatiConRicevutaValue;

                if (listaDovutiElaborati.size() > 0) {
                  DovutoElaborato pagato = lastDovutoElaborato;

                  tipoFirma.value = pagato.getDeRtInviartTipoFirma();

                  DataHandler rtValue = new DataHandler(new ByteArrayDataSource(
                      pagato.getBlbRtPayload(), "application/octet-stream"));
                  rt.value = rtValue;

                } else {
                  log.error(
                      "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
                  throw new RuntimeException(
                      "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
                }

                // 1) LOG NEL GIORNALE DEGLI EVENTI - chiedi
                // pagati
                dataOraEvento = new Date();
                identificativoDominio = ente.getCodiceFiscaleEnte();
                identificativoUnivocoVersamentoGiornale = carrello
                    .getCodRpDatiVersIdUnivocoVersamento();
                codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
                    ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
                identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
                tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
                componente = Constants.COMPONENTE_FESP;
                categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
                tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
                sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
                identificativoFruitore = Constants.SIL;
                identificativoErogatore = ente.getCodiceFiscaleEnte();
                identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
                canalePagamento = "";
                parametriSpecificiInterfaccia = pagatiConRicevutaString;
                esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

                giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                    identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                    identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                    categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                    identificativoErogatore, identificativoStazioneIntermediarioPa,
                    canalePagamento, parametriSpecificiInterfaccia, esito);

                return;
              }
            } else {
              // non esiste il carrello
              FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR,
                  "Carrello non trovato per ente [ " + ente.getCodIpaEnte() + " ], IUV [ "
                      + identificativoUnivocoVersamento + " ]");
              fault.value = faultBean;
              return;
            }
          }
        } else {
          // Lista dovuti elaborati vuota
          FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_IUV_NON_VALIDO,
              "IUV non valido per ente [ " + ente.getCodIpaEnte() + " ]", null);
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_IUV_NON_VALIDO + " IUV non valido per ente [ "
              + ente.getCodIpaEnte() + " ]";
          esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        }
      } else {
        // Dovuto pagabile o in corso di pagamento
        Dovuto dovuto = listaDovutiPagabili.get(0);
        AnagraficaStato anagraficaStato = dovuto.getMygovAnagraficaStatoId();

        if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
            && (anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_DA_PAGARE)
            || anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_PREDISPOSTO))) {

          // verifica flag (se true) e data scaduta torna scaduto
          EnteTipoDovuto etd = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), codIpaEnte, false).get();

          if (etd.isFlgScadenzaObbligatoria()) {
            LocalDate dataScadenza = new LocalDate(dovuto.getDtRpDatiVersDataEsecuzionePagamento());
            if (dataScadenza.isBefore(new LocalDate())) {
              // dovuto scaduto
              FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR,
                  "Dovuto scaduto per ente [ " + ente.getCodIpaEnte() + " ], IUV [ " + identificativoUnivocoVersamento + " ]");
              fault.value = faultBean;
              return;
            }
          }

          // DA PAGARE
          FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_NON_INIZIATO,
              "Pagamento non iniziato per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                  + identificativoUnivocoVersamento + "]",
              null);
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_NON_INIZIATO
              + " Pagamento non iniziato per per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
              + identificativoUnivocoVersamento + "]";
          esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        } else if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
            && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_SCADUTO)) {
          // SCADUTO
          FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO,
              "Pagamento scaduto per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                  + identificativoUnivocoVersamento + "]",
              null);
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_SCADUTO + " Pagamento scaduto per per ente [ "
              + ente.getCodIpaEnte() + " ], IUV [" + identificativoUnivocoVersamento + "]";
          esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        } else if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
            && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO)) {
          // PAGAMENTO IN CORSO
          FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_PAGAMENTO_IN_CORSO,
              "Pagamento in corso per ente [ " + ente.getCodIpaEnte() + " ], IUV ["
                  + identificativoUnivocoVersamento + "]",
              null);
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_IN_CORSO + " Pagamento in corso per per ente [ "
              + ente.getCodIpaEnte() + " ], IUV [" + identificativoUnivocoVersamento + "]";
          esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        } else {
          // altri stati con dovuto nella tabella mygov_dovuto
          FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR, "Errore interno per ente [ " + ente.getCodIpaEnte()
                  + " ], IUV [ " + identificativoUnivocoVersamento + " ]");
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = identificativoUnivocoVersamento;
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_SYSTEM_ERROR
              + " Errore interno per per ente [ " + ente.getCodIpaEnte() + " ], IUV [" + identificativoUnivocoVersamento + "]";
          esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        }
      }
    } else if (StringUtils.isNotBlank(identificativoUnivocoDovuto)) {
      // LOG giornale degli eventi
      Date dataOraEvento = new Date();
      String identificativoDominio = ente.getCodiceFiscaleEnte();
      String identificativoUnivocoVersamentoGiornale = "";
      String codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
      String identificativoPrestatoreServiziPagamento = "";
      String tipoVersamento = "";
      String componente = Constants.COMPONENTE_FESP;
      String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
      String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
      String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString();
      String identificativoFruitore = Constants.SIL;
      String identificativoErogatore = ente.getCodiceFiscaleEnte();
      String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      String canalePagamento = "";
      String parametriSpecificiInterfaccia = "Parametri di input ente [ " + ente.getCodIpaEnte() + " ], IUD [ "
          + identificativoUnivocoDovuto + " ]";
      String esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

      giornaleService.registraEvento(dataOraEvento, identificativoDominio,
          identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
          identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento,
          sottoTipoEvento, identificativoFruitore, identificativoErogatore,
          identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);

      Dovuto dovuto = dovutoService.getByIudEnte(identificativoUnivocoDovuto, ente.getCodIpaEnte());
      if (dovuto != null) {
        AnagraficaStato anagraficaStato = dovuto.getMygovAnagraficaStatoId();

        if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
            && (anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_DA_PAGARE)
            || anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_PREDISPOSTO))) {

          //verifica flag (se true) e data torna scaduto

          EnteTipoDovuto etd = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), codIpaEnte, false).get();

          if (etd.isFlgScadenzaObbligatoria()) {
            LocalDate dataScadenza = new LocalDate(dovuto.getDtRpDatiVersDataEsecuzionePagamento());
            if (dataScadenza.isBefore(new LocalDate())) {
              // dovuto scaduto
              FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR,
                  "Dovuto scaduto per ente [ " + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]");
              fault.value = faultBean;
              return;
            }
          }

          // DA PAGARE
          FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte,
                  CODE_PAA_PAGAMENTO_NON_INIZIATO, "Pagamento non iniziato per ente [ "
                      + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]", null);
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = "";
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_NON_INIZIATO
              + " Pagamento non iniziato per per ente [ " + ente.getCodIpaEnte() + " ], IUD [ "
              + identificativoUnivocoDovuto + " ]";
          esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        } else if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
            && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_SCADUTO)) {
          // SCADUTO
          FaultBean faultBean = VerificationUtils.getFaultBean(
                  codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO, "Pagamento scaduto per ente [ "
                      + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]", null);
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = "";
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_SCADUTO + " Pagamento scaduto per per ente [ "
              + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]";
          esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        } else if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
            && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO)) {
          // PAGAMENTO IN CORSO
          FaultBean faultBean = VerificationUtils.getFaultBean(
                  codIpaEnte, CODE_PAA_PAGAMENTO_IN_CORSO, "Pagamento in corso per ente [ "
                      + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]", null);
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = "";
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_IN_CORSO + " Pagamento in corso per per ente [ "
              + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]";
          esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        } else {
          // altri stati con dovuto nella tabella mygov_dovuto
          FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR, "Errore interno per ente [ " + ente.getCodIpaEnte()
                  + " ], IUD [ " + identificativoUnivocoDovuto + " ]");
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = "";
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_SYSTEM_ERROR
              + " Errore interno per per ente [ " + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]";
          esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        }
      } else {
        List<DovutoElaborato> listaDovutiElaborati = dovutoElaboratoService.getByIudEnte(identificativoUnivocoDovuto, ente.getCodIpaEnte());
        if (!CollectionUtils.isEmpty(listaDovutiElaborati)) {
          if (listaDovutiElaborati.size() == 1) {
            DovutoElaborato dovutoElaborato = listaDovutiElaborati.get(0);
            AnagraficaStato anagraficaStato = dovutoElaborato.getMygovAnagraficaStatoId();
            // SCADUTO
            if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
                && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_SCADUTO_ELABORATO)) {
              FaultBean faultBean = VerificationUtils.getFaultBean(
                  codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO, "Pagamento scaduto per ente [ "
                      + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]",
                  null);
              fault.value = faultBean;

              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = "";
              codiceContestoPagamento = dovutoElaborato.getCodRpSilinviarpCodiceContestoPagamento();
              identificativoPrestatoreServiziPagamento = dovutoElaborato.getCodRpSilinviarpIdPsp();
              tipoVersamento = dovutoElaborato.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_SCADUTO
                  + " Pagamento scaduto per per ente [ " + ente.getCodIpaEnte() + " ], IUD [ "
                  + identificativoUnivocoDovuto + " ]";
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
            // ABORTITO
            if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
                && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_ABORT)) {
              FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte,
                  CODE_PAA_PAGAMENTO_ANNULLATO, "Pagamento abortito per ente [ "
                      + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]",
                  null);
              fault.value = faultBean;

              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = "";
              codiceContestoPagamento = dovutoElaborato.getCodRpSilinviarpCodiceContestoPagamento();
              identificativoPrestatoreServiziPagamento = dovutoElaborato.getCodRpSilinviarpIdPsp();
              tipoVersamento = dovutoElaborato.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_ANNULLATO
                  + " Pagamento abortito per per ente [ " + ente.getCodIpaEnte() + " ], IUD [ "
                  + identificativoUnivocoDovuto + " ]";
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
            // RT PRESENTE
            Carrello carrello = dovutoElaborato.getMygovCarrelloId();
            AnagraficaStato anagraficaStatoCarrello = carrello.getMygovAnagraficaStatoId();
            if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                && (anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_PAGATO)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_NON_PAGATO)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE))) {

              PagatiConRicevuta pagatiConRicevutaDocument =
                  PagamentiTelematiciDovutiPagatiHelper.creaPagatiDocumentConRicevuta(carrello, listaDovutiElaborati);

              String pagatiConRicevutaString = jaxbTransformService.marshalling(pagatiConRicevutaDocument, PagatiConRicevuta.class);

              log.debug("PAGATI CON RICEVUTA per dominio [" + pagatiConRicevutaDocument.getDominio().getIdentificativoDominio()
                  + "] e IUV [" + pagatiConRicevutaDocument.getDatiPagamento().getIdentificativoUnivocoVersamento()
                  + "]: " + pagatiConRicevutaString);

              // se usi soapUI
              // DataHandler pagatiValue = new DataHandler(new
              // ByteArrayDataSource(pagatiString.getBytes(),
              // "application/octet-stream"));

              byte[] encodedPagatiConRicevuta;
              encodedPagatiConRicevuta = Base64.encodeBase64(pagatiConRicevutaString.getBytes(StandardCharsets.UTF_8));
              DataHandler pagatiConRicevutaValue = new DataHandler(
                  new ByteArrayDataSource(encodedPagatiConRicevuta, "application/octet-stream"));

              pagati.value = pagatiConRicevutaValue;

              if (listaDovutiElaborati.size() > 0) {
                DovutoElaborato pagato = listaDovutiElaborati.get(0);

                tipoFirma.value = pagato.getDeRtInviartTipoFirma();

                DataHandler rtValue = new DataHandler(new ByteArrayDataSource(
                    pagato.getBlbRtPayload(), "application/octet-stream"));
                rt.value = rtValue;

              } else {
                log.error(
                    "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
                throw new RuntimeException(
                    "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
              }

              // 1) LOG NEL GIORNALE DEGLI EVENTI - chiedi
              // pagati
              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = carrello
                  .getCodRpDatiVersIdUnivocoVersamento();
              codiceContestoPagamento = carrello.getCodRpDatiVersCodiceContestoPagamento() != null
                  ? carrello.getCodRpDatiVersCodiceContestoPagamento() : "";
              identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
              tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = pagatiConRicevutaString;
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
          } else {
            DovutoElaborato dovutoElaborato = DovutoUtils.getLastDovutoElaboratoOrPositivo(listaDovutiElaborati);
            AnagraficaStato anagraficaStato = dovutoElaborato.getMygovAnagraficaStatoId();
            List<DovutoElaborato> listaLastDovutoElaborato = new ArrayList<DovutoElaborato>();
            listaLastDovutoElaborato.add(dovutoElaborato);
            // SCADUTO
            if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
                && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_SCADUTO_ELABORATO)) {
              FaultBean faultBean = VerificationUtils.getFaultBean(
                  codIpaEnte, CODE_PAA_PAGAMENTO_SCADUTO, "Pagamento scaduto per ente [ "
                      + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]",
                  null);
              fault.value = faultBean;

              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = "";
              codiceContestoPagamento = dovutoElaborato.getCodRpSilinviarpCodiceContestoPagamento();
              identificativoPrestatoreServiziPagamento = dovutoElaborato.getCodRpSilinviarpIdPsp();
              tipoVersamento = dovutoElaborato.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_SCADUTO
                  + " Pagamento scaduto per per ente [ " + ente.getCodIpaEnte() + " ], IUD [ "
                  + identificativoUnivocoDovuto + " ]";
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
            // ABORTITO
            if (anagraficaStato.getDeTipoStato().equals(Constants.STATO_TIPO_DOVUTO)
                && anagraficaStato.getCodStato().equals(Constants.STATO_DOVUTO_ABORT)) {
              FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte,
                  CODE_PAA_PAGAMENTO_ANNULLATO, "Pagamento abortito per ente [ "
                      + ente.getCodIpaEnte() + " ], IUD [ " + identificativoUnivocoDovuto + " ]",
                  null);
              fault.value = faultBean;

              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = "";
              codiceContestoPagamento = dovutoElaborato.getCodRpSilinviarpCodiceContestoPagamento();
              identificativoPrestatoreServiziPagamento = dovutoElaborato.getCodRpSilinviarpIdPsp();
              tipoVersamento = dovutoElaborato.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = CODE_PAA_PAGAMENTO_ANNULLATO
                  + " Pagamento abortito per per ente [ " + ente.getCodIpaEnte() + " ], IUD [ "
                  + identificativoUnivocoDovuto + " ]";
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
            // RT PRESENTE
            Carrello carrello = dovutoElaborato.getMygovCarrelloId();
            AnagraficaStato anagraficaStatoCarrello = carrello.getMygovAnagraficaStatoId();
            if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                && (anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_PAGATO)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_NON_PAGATO)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
                || anagraficaStatoCarrello.getCodStato()
                .equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE))) {

              PagatiConRicevuta pagatiConRicevutaDocument =
                  PagamentiTelematiciDovutiPagatiHelper.creaPagatiDocumentConRicevuta(carrello, listaLastDovutoElaborato);

              String pagatiConRicevutaString = jaxbTransformService.marshalling(pagatiConRicevutaDocument, PagatiConRicevuta.class);

              log.debug("PAGATI CON RICEVUTA per dominio [" + pagatiConRicevutaDocument.getDominio().getIdentificativoDominio()
                  + "] e IUV [" + pagatiConRicevutaDocument.getDatiPagamento().getIdentificativoUnivocoVersamento()
                  + "]: " + pagatiConRicevutaString);

              // se usi soapUI
              // DataHandler pagatiValue = new DataHandler(new
              // ByteArrayDataSource(pagatiString.getBytes(),
              // "application/octet-stream"));

              byte[] encodedPagatiConRicevuta;
              encodedPagatiConRicevuta = Base64.encodeBase64(pagatiConRicevutaString.getBytes(StandardCharsets.UTF_8));
              DataHandler pagatiConRicevutaValue = new DataHandler(
                  new ByteArrayDataSource(encodedPagatiConRicevuta, "application/octet-stream"));

              pagati.value = pagatiConRicevutaValue;

              if (listaDovutiElaborati.size() > 0) {
                DovutoElaborato pagato = dovutoElaborato;

                tipoFirma.value = pagato.getDeRtInviartTipoFirma();

                DataHandler rtValue = new DataHandler(new ByteArrayDataSource(
                    pagato.getBlbRtPayload(), "application/octet-stream"));
                rt.value = rtValue;

              } else {
                log.error(
                    "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
                throw new RuntimeException(
                    "paaSILChiediPagatiConRicevuta error listaDovuti non contiene alcun dovuto elaborato.");
              }

              // 1) LOG NEL GIORNALE DEGLI EVENTI - chiedi
              // pagati
              dataOraEvento = new Date();
              identificativoDominio = ente.getCodiceFiscaleEnte();
              identificativoUnivocoVersamentoGiornale = carrello
                  .getCodRpDatiVersIdUnivocoVersamento();
              codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
              identificativoPrestatoreServiziPagamento = carrello.getCodRpSilinviarpIdPsp();
              tipoVersamento = carrello.getCodRpDatiVersTipoVersamento();
              componente = Constants.COMPONENTE_FESP;
              categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
              tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
              sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
              identificativoFruitore = Constants.SIL;
              identificativoErogatore = ente.getCodiceFiscaleEnte();
              identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
              canalePagamento = "";
              parametriSpecificiInterfaccia = pagatiConRicevutaString;
              esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

              giornaleService.registraEvento(dataOraEvento, identificativoDominio,
                  identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
                  identificativoPrestatoreServiziPagamento, tipoVersamento, componente,
                  categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
                  identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
                  parametriSpecificiInterfaccia, esito);

              return;
            }
          }
        } else {
          FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_IUD_NON_VALIDO,
              "IUD non valido per ente [ " + ente.getCodIpaEnte() + " ].", null);
          fault.value = faultBean;

          dataOraEvento = new Date();
          identificativoDominio = ente.getCodiceFiscaleEnte();
          identificativoUnivocoVersamentoGiornale = "";
          codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
          identificativoPrestatoreServiziPagamento = "";
          tipoVersamento = "";
          componente = Constants.COMPONENTE_FESP;
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
          identificativoFruitore = Constants.SIL;
          identificativoErogatore = ente.getCodiceFiscaleEnte();
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";
          parametriSpecificiInterfaccia = CODE_PAA_IUD_NON_VALIDO + " IUD non valido per ente [ "
              + ente.getCodIpaEnte() + " ].";
          esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

          giornaleService.registraEvento(dataOraEvento, identificativoDominio,
              identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
              identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento,
              tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
              identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
              esito);

          return;
        }
      }
    } else {
      FaultBean faultBean = VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_SYSTEM_ERROR, DESC_PAA_SYSTEM_ERROR,
          "Nessun campo di input (idSession, identificativoUnivocoVersamento, identificativoUnivocoDovuto) valorizzato per ente [ "
              + ente.getCodIpaEnte() + " ].");
      fault.value = faultBean;

      Date dataOraEvento = new Date();
      String identificativoDominio = ente.getCodiceFiscaleEnte();
      String identificativoUnivocoVersamentoGiornale = "";
      String codiceContestoPagamento = Constants.CODICE_CONTESTO_PAGAMENTO_NA;
      String identificativoPrestatoreServiziPagamento = "";
      String tipoVersamento = "";
      String componente = Constants.COMPONENTE_FESP;
      String categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERNO.toString();
      String tipoEvento = Constants.GIORNALE_TIPO_EVENTO_PA.paaSILChiediPagatiConRicevuta.toString();
      String sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();
      String identificativoFruitore = Constants.SIL;
      String identificativoErogatore = ente.getCodiceFiscaleEnte();
      String identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      String canalePagamento = "";
      String parametriSpecificiInterfaccia = CODE_PAA_SYSTEM_ERROR
          + " Nessun campo di input (idSession, identificativoUnivocoVersamento, identificativoUnivocoDovuto) valorizzato per ente [ "
          + ente.getCodIpaEnte() + " ].";
      String esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

      giornaleService.registraEvento(dataOraEvento, identificativoDominio,
          identificativoUnivocoVersamentoGiornale, codiceContestoPagamento,
          identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento,
          sottoTipoEvento, identificativoFruitore, identificativoErogatore,
          identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);

      return;
    }
  }
}
