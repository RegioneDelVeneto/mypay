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

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.pagamenti.nodoregionalefesp.StTipoOperazione;
import it.veneto.regione.pagamenti.pa.EsitoPaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsitoRisposta;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
import it.veneto.regione.schemas._2012.pagamenti.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EsitoService {

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;

  @Autowired
  EnteFunzionalitaService enteFunzionalitaService;
  @Autowired
  CarrelloService carrelloService;
  @Autowired
  DovutoService dovutoService;
  @Autowired
  DovutoElaboratoService dovutoElaboratoService;
  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;
  @Autowired
  DovutoCarrelloService dovutoCarrelloService;
  @Autowired
  PushEsitoSilService pushEsitoSilService;
  @Autowired
  DatiMarcaBolloDigitaleService datiMarcaBolloDigitaleService;
  @Autowired
  FlussoService flussoService;
  @Autowired
  EnteService enteService;
  @Autowired
  AvvisoDigitaleService avvisoDigitaleService;
  @Autowired
  AnagraficaStatoService anagraficaStatoService;
  @Autowired
  MessageSource messageSource;
  @Autowired
  MailService mailService;

  @Transactional(propagation = Propagation.REQUIRED)
  public void elaboraEsito(Esito ctEsito, String tipoFirma, byte[] rt, IntestazionePPT header) {

    String identificativoDominio = header.getIdentificativoDominio();
    String identificativoUnivocoVersamento = header.getIdentificativoUnivocoVersamento();
    String codiceContestoPagamento = header.getCodiceContestoPagamento();

    Carrello carrello = carrelloService.updateEsito(identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento, ctEsito);
    List<Dovuto> dovutiInCarrello = dovutoService.getDovutiInCarrello(carrello.getMygovCarrelloId());
    List<CtDatiSingoloPagamentoEsito> dovutiInEsito = ctEsito.getDatiPagamento().getDatiSingoloPagamentos();
    List<Long> idDovutiElaborati = new ArrayList<>();
    int codEDatiPagCodiceEsitoPagamento = Character.getNumericValue(carrello.getCodEDatiPagCodiceEsitoPagamento());
    String tipoCarrello = carrello.getTipoCarrello();

    int indiceDatiSingoloPagamento = 1;
    if (!dovutiInEsito.isEmpty()) {
      for (CtDatiSingoloPagamentoEsito ctDatiSingoloPagamentoEsito : dovutiInEsito) {
        //TODO check this code
        int indexDovutoInEsito = dovutiInEsito.indexOf(ctDatiSingoloPagamentoEsito);
        if(dovutiInCarrello.size() > indexDovutoInEsito) {
          Dovuto dovuto = dovutiInCarrello.get(indexDovutoInEsito);

          DovutoElaborato dovutoElaborato = dovutoElaboratoService.insertByEsito(dovuto, ctDatiSingoloPagamentoEsito, carrello, ctEsito, tipoFirma, rt,
            Constants.STATO_DOVUTO_COMPLETATO, Constants.STATO_TIPO_DOVUTO, indiceDatiSingoloPagamento);

          Long id = dovutoElaborato.getMygovDovutoElaboratoId();
          idDovutiElaborati.add(id);
          indiceDatiSingoloPagamento++;
          Flusso flusso = flussoService.getById(dovutoElaborato.getMygovFlussoId().getMygovFlussoId());
          String codIpaEnte = flusso.getMygovEnteId().getCodIpaEnte();

          try {
            boolean isPushAttiva = enteFunzionalitaService.isActiveByCodIpaEnte(Constants.FUNZIONALITA_INOLTRO_ESITO_PAGAMENTO_PUSH, codIpaEnte, true);
            log.debug("Inoltro esito push attivo per ente {}: {}", codIpaEnte, isPushAttiva);
            EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(dovutoElaborato.getCodTipoDovuto(), codIpaEnte, false).orElseThrow();
            log.debug("Esito push del dovuto {}: {}", dovutoElaborato.getCodTipoDovuto(), enteTipoDovuto.isFlgNotificaEsitoPush());
            if (isPushAttiva && enteTipoDovuto.isFlgNotificaEsitoPush()) {
              pushEsitoSilService.insertNewPushEsitoSil(id);
            }
          } catch (Exception e) {
            log.debug("Verifica esito push per ente " + codIpaEnte + " fallita");
          }
        }
      }
    } else {
      for (Dovuto dovuto : dovutiInCarrello) {
        CtDatiSingoloPagamentoEsito ctDatiSingoloPagamentoEsitoNegativo = new CtDatiSingoloPagamentoEsito();
        ctDatiSingoloPagamentoEsitoNegativo.setEsitoSingoloPagamento("NON PAGATO");
        ctDatiSingoloPagamentoEsitoNegativo.setDataEsitoSingoloPagamento(ctEsito.getDataOraMessaggioRicevuta());
        ctDatiSingoloPagamentoEsitoNegativo.setSingoloImportoPagato(BigDecimal.ZERO);
        ctDatiSingoloPagamentoEsitoNegativo.setIdentificativoUnivocoRiscossione("");
        dovutiInEsito.add(ctDatiSingoloPagamentoEsitoNegativo);

        DovutoElaborato dovutoElaborato = dovutoElaboratoService.insertByEsito(dovuto, ctDatiSingoloPagamentoEsitoNegativo, carrello, ctEsito, tipoFirma, rt,
            Constants.STATO_DOVUTO_COMPLETATO, Constants.STATO_TIPO_DOVUTO, indiceDatiSingoloPagamento);

        Long id = dovutoElaborato.getMygovDovutoElaboratoId();
        idDovutiElaborati.add(id);
        indiceDatiSingoloPagamento++;
      }
    }

    if(List.of(Constants.TIPO_CARRELLO_ESTERNO_ANONIMO, Constants.TIPO_CARRELLO_ESTERNO_ANONIMO_MULTIENTE).contains(tipoCarrello)) {
      for (CtDatiSingoloPagamentoEsito ctDatiSingoloPagamentoEsito : dovutiInEsito) {
        //TODO check this code
        int indexDovutoInEsito = dovutiInEsito.indexOf(ctDatiSingoloPagamentoEsito);
        if(dovutiInCarrello.size() > indexDovutoInEsito) {
          Dovuto dovuto = dovutiInCarrello.get(indexDovutoInEsito);
          dovutoCarrelloService.deleteDovutoCarrelloByIdDovuto(dovuto.getMygovDovutoId());
          dovutoService.removeDovuto(dovuto);
          if (dovuto.getCodTipoDovuto().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE) && dovuto.getMygovDatiMarcaBolloDigitaleId() != null)
            datiMarcaBolloDigitaleService.remove(dovuto.getMygovDatiMarcaBolloDigitaleId());
        }
      }
    } else {
      List<String> AvvisiKindList = List.of(Constants.TIPO_CARRELLO_AVVISO_ANONIMO, Constants.TIPO_CARRELLO_AVVISO_ANONIMO_ENTE);
      switch (codEDatiPagCodiceEsitoPagamento) {
        case 0: // TUTTO PAGATO
          for (CtDatiSingoloPagamentoEsito ctDatiSingoloPagamentoEsito : dovutiInEsito) {
            //TODO check this code
            int indexDovutoInEsito = dovutiInEsito.indexOf(ctDatiSingoloPagamentoEsito);
            if(dovutiInCarrello.size() > indexDovutoInEsito) {
              Dovuto dovuto = dovutiInCarrello.get(indexDovutoInEsito);
              dovutoCarrelloService.deleteDovutoCarrelloByIdDovuto(dovuto.getMygovDovutoId());
              dovutoService.removeDovuto(dovuto);
            }
          }
          break;
        case 1: // TUTTO NON PAGATO
          for (Dovuto dovuto : dovutiInCarrello) {
            elaboraDovutoNonPagato(dovuto.getMygovDovutoId(), carrello.getMygovCarrelloId());
          }
          break;
        case 2: // PARZIALMENTE PAGATO
        case 4: // DECORRENZA TERMINI PARZIALE
          for (CtDatiSingoloPagamentoEsito ctDatiSingoloPagamentoEsito : dovutiInEsito) {

            //TODO check this code
            int indexDovutoInEsito = dovutiInEsito.indexOf(ctDatiSingoloPagamentoEsito);
            if(dovutiInCarrello.size() > indexDovutoInEsito) {
              Dovuto dovuto = dovutiInCarrello.get(indexDovutoInEsito);
              if (!AvvisiKindList.contains(tipoCarrello) && ctDatiSingoloPagamentoEsito.getSingoloImportoPagato().compareTo(BigDecimal.ZERO) == 0) {
                elaboraDovutoNonPagato(dovuto.getMygovDovutoId(), carrello.getMygovCarrelloId());
              } else {
                dovutoCarrelloService.deleteDovutoCarrelloByIdDovuto(dovuto.getMygovDovutoId());
                dovutoService.removeDovuto(dovuto);
              }
            }
          }
          break;
        case 3: // TUTTO DECORRENZA TERMINI - lascio tutto come sta
          break;
        default:
          break;
      }
    }
    PaaSILInviaEsitoRisposta paaSILInviaEsitoRisposta = new PaaSILInviaEsitoRisposta();
    EsitoPaaSILInviaEsito esitoPaaSILInviaEsito = new EsitoPaaSILInviaEsito();
    esitoPaaSILInviaEsito.setEsito("OK");
    paaSILInviaEsitoRisposta.setPaaSILInviaEsitoRisposta(esitoPaaSILInviaEsito);

    carrelloService.updatePaaSILInviaEsitoRisposta(carrello.getMygovCarrelloId(), paaSILInviaEsitoRisposta);
    for (Long idDovutoElaborato : idDovutiElaborati) {
      dovutoElaboratoService.updatePaaSILInviaEsitoRisposta(idDovutoElaborato, paaSILInviaEsitoRisposta);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void elaboraDovutoNonPagato(Long idDovuto, Long idCarrello) {
    Dovuto dovuto = dovutoService.getById(idDovuto);
    if (StringUtils.isBlank(dovuto.getCodIuv()) && dovuto.getMygovFlussoId().getFlgSpontaneo()) {
      dovutoCarrelloService.deleteDovutoCarrelloByIdDovuto(idDovuto);
      dovutoService.removeDovuto(dovuto);
    } else {
      dovutoCarrelloService.deleteDovutoCarrelloByIdDovuto(idDovuto);
      dovutoService.restorePagabile(idDovuto); //Ripristino
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void elaboraDovutiNonPagatiERimuoviCarrello(final String idDominio, final String iuv, final String codiceContestoPagamento) {
    Carrello carrello = carrelloService.getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamento(idDominio, iuv, codiceContestoPagamento);
    if (carrello != null) {
      List<Dovuto> listaDovuti = dovutoService.getDovutiInCarrello(carrello.getMygovCarrelloId());
      for (Dovuto dovuto : listaDovuti) {
        elaboraDovutoNonPagato(dovuto.getMygovDovutoId(), carrello.getMygovCarrelloId());
      }
      carrelloService.delete(carrello.getMygovCarrelloId());
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void manageAvvisoDigitale(String identificativoDominio, Esito ctEsito, Carrello cart) {
    try {
      Ente ente = enteService.getEnteByCodFiscale(identificativoDominio);
      List<String> listaFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(ente.getCodIpaEnte(),true)
          .stream().map(EnteFunzionalita::getCodFunzionalita).collect(Collectors.toList());

      boolean avvisaturaAttiva = listaFunzionalita.contains(Constants.FUNZIONALITA_AVVISATURA_DIGITALE);
      boolean notificaPagVers = listaFunzionalita.contains(Constants.FUNZIONALITA_NOTIFICA_AVVISI_IO);
      List<DovutoElaborato> listaDovutiElaborati = dovutoElaboratoService.getByCarrello(cart);

      if (avvisaturaAttiva) {
        for (DovutoElaborato dovutoElaborato : listaDovutiElaborati) {
          if (Utilities.isAvviso(dovutoElaborato.getValidIuv()) && dovutoElaborato.getDeESilinviaesitoEsito().equals("OK")) {
            AvvisoDigitale avvisoDigitale = avvisoDigitaleService.getByIdDominioECodiceAvviso(ente.getCodiceFiscaleEnte(),
                "0" + ente.getApplicationCode() + dovutoElaborato.getValidIuv());
            AnagraficaStato anagStato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_AVVISO_DIGITALE_NUOVO,
                Constants.STATO_AVVISO_DIGITALE_TIPO_STATO);
            if (Objects.nonNull(avvisoDigitale) && avvisoDigitale.getMygovAnagraficaStatoId().equals(anagStato)) {
              avvisoDigitaleService.updateAnagraficaStatoDiUnAvvisoDigitaleEsistente(avvisoDigitale.getMygovAvvisoDigitaleId(),
                  Constants.STATO_AVVISO_DIGITALE_PAGATO, Constants.STATO_AVVISO_DIGITALE_TIPO_STATO);
            } else {
              log.warn("Stato avviso digitale per codice avviso [0 {} {}] diverso da [{}]", ente.getApplicationCode(), dovutoElaborato.getValidIuv(),Constants.STATO_AVVISO_DIGITALE_NUOVO);
            }
          }
        }
      }
      String codiceIdentificativoUnivocoPagatore = Optional.ofNullable(ctEsito.getSoggettoPagatore())
          .map(CtSoggettoPagatore::getIdentificativoUnivocoPagatore)
          .map(CtIdentificativoUnivocoPersonaFG::getCodiceIdentificativoUnivoco).orElse(StringUtils.EMPTY);
      String codiceIdentificativoUnivocoVersante = Optional.ofNullable(ctEsito.getSoggettoVersante())
          .map(CtSoggettoVersante::getIdentificativoUnivocoVersante)
          .map(CtIdentificativoUnivocoPersonaFG::getCodiceIdentificativoUnivoco).orElse(StringUtils.EMPTY);
      if (notificaPagVers && !codiceIdentificativoUnivocoPagatore.equalsIgnoreCase(codiceIdentificativoUnivocoVersante)) {
        for (DovutoElaborato dovutoElaborato : listaDovutiElaborati) {
          if (Utilities.isAvviso(dovutoElaborato.getValidIuv()) &&
              dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento()!= null &&
              dovutoElaborato.getCodEDatiPagCodiceEsitoPagamento().equals(Constants.CODICE_ESITO_PAGAMENTO_OK) &&
              dovutoElaborato.getNumEDatiPagDatiSingPagSingoloImportoPagato().compareTo(BigDecimal.ZERO) > 0) {

            String idFlusso = "_" + ente.getCodIpaEnte() + Constants.FLUSSO_NOTIFICA_AVVISO_IO_PAGVERS_SUFFISSO_ID;

            FlussoAvvisoDigitale flusso = avvisoDigitaleService.selectFlussoAvvisoForInsert(
                ente.getCodiceFiscaleEnte(), Constants.FLUSSO_NOTIFICA_AVVISO_IO_PAGVERS_TIPO_STATO, Constants.FLUSSO_NOTIFICA_AVVISO_IO_STATO_PAGVERS,
                idFlusso, Constants.FLUSSO_NOTIFICA_AVVISO_IO_PAGVERS_TIPO);
            //inserire record su tabella mygov_avviso_digitale
            avvisoDigitaleService.addNewAvvisoDigitale(
                ente.getCodiceFiscaleEnte(),											/* Codice fiscale della struttura che invia l'avviso Digitale. */
                ente.getDeNomeEnte(),													/* Denominazione dell’Ente Creditore che invia la richiesta di avviso digitale. */
                dovutoElaborato.getValidIuv(),											/* Codice dell’avviso di pagamento. METTO DIRETTAMENTE IUV*/
                ctEsito.getSoggettoPagatore().getAnagraficaPagatore(),/* Indica il nominativo o la ragione sociale del pagatore. */
                ctEsito.getSoggettoPagatore()
                    .getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().value(),	/* Dato alfanumerico che indica la natura del pagatore (F o G). */
                ctEsito.getSoggettoPagatore()
                    .getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco(), 	/* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
                dovutoElaborato.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(), 	/* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
                dovutoElaborato.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(),	/* Indica la data, successiva alla data di scadenza, sino alla quale si ritiene valido l'avviso. */
                dovutoElaborato.getNumEDatiPagDatiSingPagSingoloImportoPagato(),		/* L'importo relativo alla somma da versare. */
                ctEsito.getSoggettoPagatore().getEMailPagatore(), 		/* Email. */
                null,																	/* cellulare. */
                dovutoElaborato.getDeRpDatiVersDatiSingVersCausaleVersamento(),			/* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
                Constants.NOTIFICA_AVVISO_IO_STATO_NUOVO,								/* Codice dello stato da assegnare all'avviso. */
                Constants.NOTIFICA_AVVISO_IO_PAGVERS_TIPO_STATO	,						/* Categoria padre della tipologia di stato. */
                flusso.getCodFadIdFlusso(),												/* Identificativo flusso */
                ente.getCodRpDatiVersDatiSingVersIbanAccredito(), 						/* Iban accredito recuperato dall'ente */
                ente.getCodRpDatiVersDatiSingVersIbanAppoggio(),						/* Iban appoggio recuperato dall'ente */
                1, 																		/* Tipo pagamento (0 = Contestuale, 1 = Non contestuale) */
                StTipoOperazione.C.value()												/* Tipo operazione (C = Creazione di un avviso esistente) */
            );
          }
        }
      }
    }
    catch (Exception e) {
      log.warn("Errore cambio di stato nell'avvisatura digitale", e);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void sendEmailEsito(Carrello cart) {
    try {
      String idMessaggioRicevuta = cart.getCodEIdMessaggioRicevuta();
      if (cart.getCodEDatiPagCodiceEsitoPagamento()!=null &&
          cart.getCodEDatiPagCodiceEsitoPagamento().equals(Constants.CODICE_ESITO_PAGAMENTO_KO) &&
          idMessaggioRicevuta.startsWith("###")) {
        log.info("Ricevuta RT negativa generata dal FESP {}", idMessaggioRicevuta);
      } else {
        String codRpIdMessaggioRichiesta = cart.getCodRpIdMessaggioRichiesta();
        //retrieve email address from RT if present, otherwise get it from RPT
        String emailPagatore = StringUtils.firstNonBlank(cart.getDeESoggPagEmailPagatore(), cart.getDeRpSoggPagEmailPagatore());
        String emailVersante = StringUtils.firstNonBlank(cart.getDeESoggVersEmailVersante(), cart.getDeRpSoggVersEmailVersante());
        if(StringUtils.isBlank(emailPagatore) && StringUtils.isBlank(emailVersante)) {
          if(List.of(Constants.TIPO_CARRELLO_AVVISO_ANONIMO,
                  Constants.TIPO_CARRELLO_SPONTANEO_ANONIMO)
              .contains(cart.getTipoCarrello())) {
            carrelloService.updateFlgNotificaEsitoByIdMessaggioRichiesta(codRpIdMessaggioRichiesta, null);
            return;
          }
        }

        if(StringUtils.isBlank(emailPagatore) && StringUtils.isBlank(emailVersante)
            && List.of(Constants.TIPO_CARRELLO_PAGAMENTO_ATTIVATO_PRESSO_PSP,
            Constants.TIPO_CARRELLO_ESTERNO_ANONIMO,
            Constants.TIPO_CARRELLO_ESTERNO_ANONIMO_MULTIENTE,
            Constants.TIPO_CARRELLO_AVVISO_ANONIMO_ENTE
        ).contains(cart.getTipoCarrello())) {
          carrelloService.updateFlgNotificaEsitoByIdMessaggioRichiesta(codRpIdMessaggioRichiesta, null);
          return;
        }
        String content = messageSource.getMessage("pa.esito.mailContent", null, Locale.ITALY);
        Map<String, String> params = new HashMap<>();
        params.put("paUrl", appBeAbsolutePath);
        params.put("codRpIdMessaggioRichiesta", codRpIdMessaggioRichiesta);

        if(cart.getCodEDatiPagCodiceEsitoPagamento()==0){
          // se pagamento con successo e carrello ha 1 solo dovuto, cerco il messaggio custom legato al tipo dovuto e lo aggiungo al content
          List<DovutoElaborato> dovuti = dovutoElaboratoService.getByCarrello(cart);
          if(dovuti!=null && dovuti.size()==1){
            String enteTipoDovuto = dovuti.get(0).getNestedEnte().getCodIpaEnte()+"|"+dovuti.get(0).getCodTipoDovuto();
            Optional<String> tipoDovutoContent = Optional.ofNullable(messageSource.getMessage("pa.esito.tipodovuto."+enteTipoDovuto, null, null, Locale.ITALY));
            if(tipoDovutoContent.isPresent()){
              content = StringSubstitutor.replace(tipoDovutoContent.get(), params, "${", "}") + " <br> " + content;
            }
          }
        }

        params.put("content", content);
        params.put("paymentOutcome", messageSource.getMessage("pa.esito.esito", null, Locale.ITALY) + ": "
            + messageSource.getMessage("pa.esito."+ cart.getCodEDatiPagCodiceEsitoPagamento(), null, Locale.ITALY) );
        log.debug("send mail - emailVersante: {} - emailPagatore: {}", emailVersante, emailPagatore);

        boolean mailSent = false;
        try {
          if (StringUtils.isNotBlank(emailPagatore)) {
            mailService.sendMailEsitoPagamento(emailPagatore, null, params);
            mailSent = true;
          }
          if (StringUtils.isNotBlank(emailVersante) && !StringUtils.equalsIgnoreCase(emailVersante, emailPagatore)) {
            mailService.sendMailEsitoPagamento(emailVersante, null, params);
            mailSent = true;
          }
        } catch (Exception e) {
          log.warn("error sending mail for cart id:"+cart.getMygovCarrelloId(), e);
          throw new MyPayException(e);
        } finally {
          if(mailSent)
            carrelloService.updateFlgNotificaEsitoByIdMessaggioRichiesta(codRpIdMessaggioRichiesta, true);
        }

      }
    }
    catch (Exception e) {
      log.error("Errore durante la preparazione all'invio mail", e);
    }
  }

}
