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
import it.regioneveneto.mygov.payment.mypay4.dto.CartItem;
import it.regioneveneto.mygov.payment.mypay4.dto.SpontaneoFormTo;
import it.regioneveneto.mygov.payment.mypay4.dto.SpontaneoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.NotAuthorizedException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteFunzionalita;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.render.FieldBean;
import it.regioneveneto.mygov.payment.mypay4.util.render.HttpRenderClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class SpontaneoService {

  @Value("${app.fe.cittadino.absolute-path}")
  private String appFeCittadinoAbsolutePath;

  @Resource
  private SpontaneoService self;

  @Autowired
  private EnteService enteService;

  @Autowired
  private DovutoService dovutoService;


  @Autowired
  private EnteFunzionalitaService enteFunzionalitaService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private AsynchAvvisiDigitaliService asynchAvvisiDigitaliService;

  @Autowired
  private HttpRenderClientService httpRenderClientService;

  @Autowired
  private DefinitionDovutoService definitionDovutoService;

  public SpontaneoFormTo initializeForm(String codIpaEnte, String codTipo) {
    enteService.verifyEnteIsPublicAndActive(codIpaEnte);

    List<EnteFunzionalita> enteFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(codIpaEnte, true);
    boolean validDovutoSpontaneo = enteFunzionalita.stream().filter(ef -> Constants.FUNZIONALITA_PAGAMENTO_SPONTANEO.equals(ef.getCodFunzionalita())).count() == 1;
    if (!validDovutoSpontaneo) {
      throw new ValidatorException("La scelta Ente non e' attivato per il pagamento spontaneo.");
    }

    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(codTipo, codIpaEnte, true)
        .orElseThrow( () -> new ValidatorException(messageSource.getMessage("pa.messages.dovutoDisabilitato", null, Locale.ITALY)));

    SpontaneoFormTo spontaneoFormTo = new SpontaneoFormTo();

    List<FieldBean> fieldBeans = httpRenderClientService.getFieldBeansByCodXsd(enteTipoDovuto.getCodXsdCausale());
    Utilities.fillEnumerationListOfVociBilancioFromXSD(fieldBeans);
    String campoTotaleInXSD = Utilities.getTotaleInXSD(fieldBeans);

    ////if (StringUtils.isNotBlank(idSession)) {
    ////  SpontaneoAnonimo spontaneoAnonimo = spontaneoAnonimoService.getSpontaneoAnonimoByIdSession(idSession);

    ////  inserimentoSpontaneoCommand.setCodTipo(spontaneoAnonimo.getCodTipoDovuto());
    ////  inserimentoSpontaneoCommand.setImporto(Utilities
    ////      .parseImportoString(spontaneoAnonimo.getNumRpDatiVersDatiSingVersImportoSingoloVersamento()));
    ////  inserimentoSpontaneoCommand
    ////      .setCausaleCompleta(spontaneoAnonimo.getDeRpDatiVersDatiSingVersCausaleVersamento());
    ////  inserimentoSpontaneoCommand.setEmail(spontaneoAnonimo.getDeEmailAddress());
    ////  inserimentoSpontaneoCommand.setEnte(spontaneoAnonimo.getCodIpaEnte());
    ////}

    ////if (StringUtils.isNotBlank(error)) {
    ////  messagesDto.getErrorMessages().add(new DynamicMessageDto(error));
    ////  mav.addObject("messagesDto", messagesDto);
    ////}

    /* TODO reCapture if needed.
      if (Boolean.parseBoolean(propertiesUtil.getProperty("reCaptcha.enable"))) {
        spontaneoFormTo.setReCaptchaPublicKey(propertiesUtil.getProperty("reCaptcha.publicKey"));
      }
     */

    BigDecimal importoDB = enteTipoDovuto.getImporto();
    if (importoDB != null) {
      spontaneoFormTo.setImporto(importoDB.toString());
      spontaneoFormTo.setCampoTotaleInclusoInXSD(null);
    } else {
      //if (StringUtils.isNotBlank(importo)) {
      //  try {
      //    Utilities.parseImportoString(importo);
      //    inserimentoSpontaneoCommand.setImporto(importo);
      //    mav.addObject("campoTotaleInclusoInXSD", null);
      //  } catch (Exception e) {
      //    log.warn("Importo ricevuto non valido per tipo dovuto [ " + codTipo + " ], ente [ "
      //        + codIpaEnte + " ] e importo [ " + importo + " ]");
      //    mav.addObject("campoTotaleInclusoInXSD", campoTotaleInXSD);
      //  }
      //} else {
      //  mav.addObject("campoTotaleInclusoInXSD", campoTotaleInXSD);
      //}
      spontaneoFormTo.setCampoTotaleInclusoInXSD(campoTotaleInXSD);
    }

    ////if(StringUtils.isNotBlank(callbackUrl)){
    ////  request.getSession().setAttribute(Constants.SPONTANEO_CALLBACK_URL, callbackUrl);
    ////  request.getSession().setAttribute(Constants.SPONTANEO_ENTE_TIPO, ente.getCodIpaEnte() + enteTipoDovuto.getCodTipo());
    ////}

    spontaneoFormTo.setFieldBeans(fieldBeans);
    return spontaneoFormTo;
  }

  public SpontaneoTo validate(String codIpaEnte, String codTipo, SpontaneoFormTo spontaneoForm, boolean anonymousMode) throws Exception {
    enteService.verifyEnteIsPublicAndActive(codIpaEnte);
    List<EnteFunzionalita> enteFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(codIpaEnte, true);
    boolean validDovutoSpontaneo = enteFunzionalita.stream().filter(ef -> Constants.FUNZIONALITA_PAGAMENTO_SPONTANEO.equals(ef.getCodFunzionalita())).count() == 1;
    if (!validDovutoSpontaneo) {
      throw new ValidatorException("La scelta Ente non e' attivato per il pagamento spontaneo.");
    }

    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(codTipo, codIpaEnte, true)
        .orElseThrow( () -> new ValidatorException(messageSource.getMessage("pa.messages.dovutoDisabilitato", null, Locale.ITALY)));

    if (enteTipoDovuto.getImporto() != null) {
      spontaneoForm.setImporto(enteTipoDovuto.getImporto().toString());
    }

    if (StringUtils.isBlank(Utilities.getTotaleInXSD(spontaneoForm.getFieldBeans()))) {
      if (StringUtils.isBlank(spontaneoForm.getImporto())) {
        throw new ValidatorException("Modello di definizione della form non trovato");
      }
      if (new BigDecimal(spontaneoForm.getImporto()).compareTo(Constants.MAX_AMOUNT) > 0 ) {
        throw new ValidatorException("Importo superiore al massimo consentito");
      }
    }

    SpontaneoTo spontaneo = definitionDovutoService.validation(enteTipoDovuto, spontaneoForm);
    return spontaneo;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Dovuto prepareAvviso(UserWithAdditionalInfo user, CartItem spontaneo) {
    enteService.verifyEnteIsPublicAndActive(spontaneo.getCodIpaEnte());
    List<EnteFunzionalita> enteFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(spontaneo.getCodIpaEnte(), true);
    boolean validDovutoSpontaneo = enteFunzionalita.stream().filter(ef -> Constants.FUNZIONALITA_PAGAMENTO_SPONTANEO.equals(ef.getCodFunzionalita())).count() == 1;
    if (!validDovutoSpontaneo) {
      throw new ValidatorException("La scelta Ente non e' attivato per il pagamento spontaneo.");
    }
    if (spontaneo.getCodTipoDovuto().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE))
      throw new NotAuthorizedException("La stampa per questo tipo dovuto non è abilitata");
    //if pagatore email is null, then versante email is used (and is therefore compulsory)
    if(spontaneo.getIntestatario()!=null && StringUtils.isBlank(spontaneo.getIntestatario().getEmail())){
      if(StringUtils.isBlank(spontaneo.getVersanteEmail()))
        throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+AnagraficaPagatore.TIPO.Pagatore +".emailNonValida", null, Locale.ITALY));
      else
        spontaneo.getIntestatario().setEmail(spontaneo.getVersanteEmail());
    }

    self.validateCart(spontaneo);

    Ente ente = enteService.getEnteByCodIpa(spontaneo.getCodIpaEnte());
    var newId = dovutoService.insertDovutoFromSpontaneo(spontaneo, ente, true, null, false);

    // only if avviso: i.e. has IUV and length in [15 , 17])
    var dovuto = dovutoService.getById(newId);
    if (Utilities.isAvviso(dovuto.getCodIuv())) {
      // AVVISATURA DIGITALE WS
      asynchAvvisiDigitaliService.manageAvvisoDigitale(
          ente,                              /* Struttura che invia l'avviso Digitale. */
          spontaneo.getIntestatario().getAnagrafica(),        /* Indica il nominativo o la ragione sociale del pagatore. */
          spontaneo.getIntestatario().getEmail(),          /* Email */
          null,                                /* cellulare soggetto */
          dovuto.getCodIuv(),                        /* IUV dell’avviso di pagamento. */
          dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco() + "",        /* Dato alfanumerico che indica la natura del pagatore (F o G). */
          dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(),        /* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
          dovuto.getDtRpDatiVersDataEsecuzionePagamento(),        /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
          dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento(),  /* L'importo relativo alla somma da versare. */
          dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(),      /* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
          "I");                              /* Azione richiesta (I = Inserimento */

      // NOTIFICA PIATTAFORMA IO
      asynchAvvisiDigitaliService.manageNotificaIO(
          ente,                              /* Struttura che invia l'avviso Digitale. */
          spontaneo.getIntestatario().getAnagrafica(),        /* Indica il nominativo o la ragione sociale del pagatore. */
          dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco() + "",        /* Dato alfanumerico che indica la natura del pagatore (F o G). */
          dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(),        /* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
          dovuto.getDtRpDatiVersDataEsecuzionePagamento(),        /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
          dovuto.getDtRpDatiVersDataEsecuzionePagamento(),        /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
          dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento(),  /* L'importo relativo alla somma da versare. */
          spontaneo.getIntestatario().getEmail(),          /* Email soggetto */
          dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(),        /* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
          dovuto.getCodIuv(),                      /* IUV dell’avviso di pagamento. */
          dovuto.getCodTipoDovuto(),                    /* Tipo dovuto */
          "I");
    }

    return dovuto;
  }

  public void validateCart(CartItem cart) throws ValidatorException {

    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(cart.getCodTipoDovuto(), cart.getCodIpaEnte(), true)
        .orElseThrow(() -> new ValidatorException(String.format("Il tipo dovuto non e' attivo. [cod_ipa=%s, cod_tipo=%s]",
            cart.getCodIpaEnte(), cart.getCodTipoDovuto())));

    if (cart.getImporto() == null || cart.getImporto().compareTo(BigDecimal.ZERO) <= 0)
      throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.errore.importo", null, Locale.ITALY));

    dovutoService.validateAndNormalizeAnagraficaPagatore(AnagraficaPagatore.TIPO.Pagatore, cart.getIntestatario(), enteTipoDovuto.isFlgCfAnonimo());
  }

  public String getFrontendUrlForSpontaneo(Optional<String> codIpaEnte, Optional<String> codTipoDovuto){
    final StringBuilder s = new StringBuilder(this.appFeCittadinoAbsolutePath+"/landing/spontaneo");
    codIpaEnte.map(StringUtils::stripToNull).ifPresent(codIpaEnteValue -> {
      s.append("?codIpaEnte=").append(URLEncoder.encode(codIpaEnteValue, StandardCharsets.UTF_8));
      codTipoDovuto.map(StringUtils::stripToNull).ifPresent(codTipoDovutoValue ->
              s.append("&codTipo=").append(URLEncoder.encode(codTipoDovutoValue, StandardCharsets.UTF_8)));
    });
    return s.toString();
  }
}
