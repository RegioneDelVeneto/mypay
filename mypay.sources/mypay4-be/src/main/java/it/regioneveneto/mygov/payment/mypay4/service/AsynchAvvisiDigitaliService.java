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

import gov.telematici.pagamenti.ws.CtIdentificativoUnivocoPersonaFG;
import gov.telematici.pagamenti.ws.CtSoggettoPagatore;
import gov.telematici.pagamenti.ws.StTipoIdentificativoUnivocoPersFG;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciAvvisiDigitali;
import it.veneto.regione.pagamenti.nodoregionalefesp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class AsynchAvvisiDigitaliService {

  @Value("${async.avvisiDigitali.corePoolSize:3}")
  String corePoolSize;
  @Value("${async.avvisiDigitali.maxPoolSize:10}")
  String maxPoolSize;
  @Value("${async.avvisiDigitali.queueCapacity:500}")
  String queueCapacity;

  @Autowired
  private PagamentiTelematiciAvvisiDigitali pagamentiTelematiciAvvisiDigitaliClient;

  @Autowired
  private AvvisoDigitaleService avvisoDigitaleService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private EnteFunzionalitaService enteFunzionalitaService;

  @Value("${pa.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  /**
   * La funzione restituisce l'oggetto da inviare nella request della chiamata al WS "nodoSILInviaAvvisoDigitale" per consentire l’invio al
   * Nodo dei Pagamenti-SPC delle informazioni relative ad una richiesta di inoltro di avviso di pagamento in formato digitale.
   *
   * @param {@link AvvisoDigitale} avvisoDigitale
   *
   * @return {@link CtAvvisoDigitale}
   * @author Marianna Memoli
   */
  private CtAvvisoDigitale prepareParameterAvvisoDigitaleWS(AvvisoDigitale avvisoDigitale) {

    /**
     * Aggregazione che riporta le informazioni concernenti l’identificazione fiscale del pagatore.
     */
    CtIdentificativoUnivocoPersonaFG ctIdentificativoUnivocoPersonaFG = new CtIdentificativoUnivocoPersonaFG();
    /*
     * Dato alfanumerico che indica la natura del pagatore, può assumere i seguenti valori:
     *  F = Persona fisica
     *  G = Persona Giuridica.
     */
    ctIdentificativoUnivocoPersonaFG.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.fromValue(avvisoDigitale.getCodAdSogPagIdUnivPagTipoIdUniv()));
    /*
     * Campo alfanumerico che può contenere il codice fiscale o, in alternativa, la partita IVA del pagatore.
     */
    ctIdentificativoUnivocoPersonaFG.setCodiceIdentificativoUnivoco(avvisoDigitale.getCodAdSogPagIdUnivPagCodIdUniv());

    /** ======================================================================================================================= */

    /**
     * Aggregazione che riporta le informazioni concernenti il soggetto pagatore
     */
    CtSoggettoPagatore ctSoggettoPagatore = new CtSoggettoPagatore();
    /* Indica il nominativo o la ragione sociale del pagatore */
    ctSoggettoPagatore.setAnagraficaPagatore(avvisoDigitale.getDeAdSogPagAnagPagatore());
    /* Aggregazione che riporta le informazioni concernenti l’identificazione fiscale del pagatore. */
    ctSoggettoPagatore.setIdentificativoUnivocoPagatore(ctIdentificativoUnivocoPersonaFG);

    /** ======================================================================================================================= */

    /**
     * L'oggetto da inviare nella request della chiamata al WS "nodoSILInviaAvvisoDigitale"
     */
    CtAvvisoDigitale ctAvvisoDigitale = new CtAvvisoDigitale();
    /* Il codice fiscale della struttura che invia l'avviso Digitale.*/
    ctAvvisoDigitale.setIdentificativoDominio(avvisoDigitale.getCodAdIdDominio());
    /* Denominazione dell’Ente Creditore che invia la richiesta di avviso digitale. */
    ctAvvisoDigitale.setAnagraficaBeneficiario(avvisoDigitale.getDeAdAnagBeneficiario());
    /*
     * Identificativo legato alla trasmissione dell'avviso digitale. Identifica lo specifico avviso e consente di riconoscere
     * la trasmissione duplicata
     */
    ctAvvisoDigitale.setIdentificativoMessaggioRichiesta(avvisoDigitale.getCodAdIdMessaggioRichiesta());
    /* Macro categoria di classificazione dell'avviso ad uso delle app e dell'Utilizzatore finale. */
    ctAvvisoDigitale.setTassonomiaAvviso(avvisoDigitale.getCodTassonomiaAvviso());
    /* Codice dell’avviso di pagamento */
    ctAvvisoDigitale.setCodiceAvviso(avvisoDigitale.getCodAdCodAvviso());
    /* Aggregazione che riporta le informazioni concernenti il soggetto pagatore */
    ctAvvisoDigitale.setSoggettoPagatore(ctSoggettoPagatore);
    /* Indica la data entro la quale si richiede che venga effettuato il pagamento secondo il formato ISO 8601 [YYYY]-[MM]-[DD] */
    ctAvvisoDigitale.setDataScadenzaPagamento(Utilities.toXMLGregorianCalendar(avvisoDigitale.getDtAdDataScadenzaPagamento()));
    /*
     * Indica la data, successiva alla data di scadenza, sino alla quale si ritiene valido l'avviso, secondo il formato ISO 8601
     * [YYYY]-[MM]-[DD]
     */
    ctAvvisoDigitale.setDataScadenzaAvviso(Utilities.toXMLGregorianCalendar(avvisoDigitale.getDtAdDataScadenzaAvviso()));
    /*
     * Campo numerico (due cifre per la parte decimale, il separatore dei centesimi è il punto '.'), indicante l’importo relativo
     * alla somma da versare.
     */
    ctAvvisoDigitale.setImportoAvviso(avvisoDigitale.getNumAdImportoAvviso());
    /* Testo libero a disposizione dell’Ente per descrivere le motivazioni del pagamento. */
    ctAvvisoDigitale.setDescrizionePagamento(avvisoDigitale.getDeAdDescPagamento());
    /*
     * URL di una pagina web messa a disposizione dall'Ente Creditore dove l'Utilizzatore finale può consultare l'avviso di pagamento.
     */
    ctAvvisoDigitale.setUrlAvviso(null);
    /* */
    ctAvvisoDigitale.setEMailSoggetto(null);
    /* */
    ctAvvisoDigitale.setCellulareSoggetto(null);

    /* Dati singolo versamento */
    CtDatiSingoloVersamento dtSngVrs = new CtDatiSingoloVersamento();
    dtSngVrs.setIbanAccredito(avvisoDigitale.getDatiSingVersIbanAccredito());
    dtSngVrs.setIbanAppoggio(avvisoDigitale.getDatiSingVersIbanAppoggio());
    ctAvvisoDigitale.getDatiSingoloVersamentos().add(dtSngVrs);

    /* Tipo pagamento */
    ctAvvisoDigitale.setTipoPagamento(Constants.TIPO_PAGAMENTO.forInt(avvisoDigitale.getTipoPagamento()).toString());

    /* Tipo operazione */
    ctAvvisoDigitale.setTipoOperazione(StTipoOperazione.fromValue(avvisoDigitale.getTipoOperazione()));

    /** ======================================================================================================================= */

    return ctAvvisoDigitale;
  }

  /**
   * La funzione restituisce l'oggetto da inviare nell'header della chiamata al WS "nodoSILInviaAvvisoDigitale" per consentire l’invio al
   * Nodo dei Pagamenti-SPC delle informazioni relative ad una richiesta di inoltro di avviso di pagamento in formato digitale.
   *
   * @param {@link String} identificativoDominio, codice fiscale della struttura che invia l'avviso Digitale.
   *
   * @return {@link IntestazionePPT}
   * @author Marianna Memoli
   */
  private IntestazionePPT prepareHeaderRequestWS(String identificativoDominio) {

    IntestazionePPT intestazionePPT = new IntestazionePPT();

    /**
     * Valore letto dal file di properties, che a sua volta è valorizzato dal TAG "__TAG_AGID_IDENTIFICATIVO_INTERMEDIARIO_PA__" del configure.
     */
    intestazionePPT.setIdentificativoIntermediarioPA(identificativoIntermediarioPA);
    /**
     * Valore letto dal file di properties, che a sua volta è valorizzato dal TAG "__TAG_AGID_IDENTIFICATIVO_STAZIONE_INTERMEDIARIO_PA__" del configure.
     */
    intestazionePPT.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPA);
    /** */
    intestazionePPT.setIdentificativoDominio(identificativoIntermediarioPA);

    return intestazionePPT;
  }

  /**
   * La funzione restituisce l'oggetto da inviare nella request della chiamata al WS "nodoSILInviaAvvisoDigitale" per consentire l’invio al
   * Nodo dei Pagamenti-SPC delle informazioni relative ad una richiesta di inoltro di avviso di pagamento in formato digitale.
   *
   * @param {@link AvvisoDigitale} avvisoDigitale
   *
   * @return {@link NodoSILInviaAvvisoDigitale}
   * @author Marianna Memoli
   */
  private NodoSILInviaAvvisoDigitale prepareBodyRequestWS(AvvisoDigitale avvisoDigitale) {

    NodoSILInviaAvvisoDigitale ctNodoSILInviaAvvisoDigitale = new NodoSILInviaAvvisoDigitale();

    ctNodoSILInviaAvvisoDigitale.setAvvisoDigitaleWS(prepareParameterAvvisoDigitaleWS(avvisoDigitale));

    //ctNodoSILInviaAvvisoDigitale.setPassword(null);

    return ctNodoSILInviaAvvisoDigitale;
  }

  /**
   * La funzione gestisce la notifica dell'avviso di pagamento in formato digitale.
   *
   * Dettaglio funzionalita' di "Avvisatura digitale in modalità push":
   * ----------------------------------------------------------------------------------------------------------------------------------------------
   * |
   * | MyPay invia al NodoSPC il singolo avviso digitale che intende far recapitare all'utente attraverso un apposito Web Services.
   * | Una volta superati i controlli specifici, l'avviso digitale ricevuto dal PSP viene recapitato in modalità sincrona al debitore.
   * | Il recapito al debitore avviene con le modalità da questi indicate in fase di iscrizione al servizio (e-mail, sms o notifica su dispositivo
   * | mobile), pertanto l'utilizzatore finale potrebbe ricevere lo stesso avviso attraverso più canali o più PSP.
   * | Il Web Services ritorna nella response l'esito dell'operazione e gli esiti degli invii dell'avviso per i diversi canali.
   * |
   * ----------------------------------------------------------------------------------------------------------------------------------------------
   *
   * Fasi del processo:
   * ----------------------------------------------------------------------------------------------------------------------------------------------
   *  1. Controllo se l'ente ha abilitato la funzionalità di "Avvisatura Digitale", se cosi non fosse interrompo la procedura.
   * 	2. Verifico che nella tabella "mygov_flusso_avviso_digitale" esista un flusso relativo la funzionalità di "AVVISATURA_DIGITALE"
   * 	   per l'ente considerato.
   * 	   Se mancante, inserisco l'anagrafica del flusso.
   * 	3. Verifico se i dati del dovuto si riferiscono ad un avviso di pagamento che è già stato preso in carico per la notifica.
   *  4. Chiamo la funzione che ha in carico la chiamata al WS e la gestione della response.
   *
   * @param {@link Ente} 		 ente
   * @param {@link String} 	 anagraficaPagatore, 			Indica il nominativo o la ragione sociale del pagatore.
   * @param {@link String} 	 emailSoggetto, 				Indirizzo di posta elettronica del soggetto al quale è indirizzato l'avviso.
   * @param {@link String} 	 cellulareSoggetto, 			Numero di cellulare del soggetto al quale è indirizzato l'avviso.
   * @param {@link String} 	 codIuvDovuto, 					IUV avviso
   * @param {@link String} 	 tipoIdUnivocoDovuto, 			Dato alfanumerico che indica la natura del pagatore (F o G).
   * @param {@link String} 	 codiceIdUnivocoDovuto, 		Il codice fiscale o, in alternativa, la partita IVA del pagatore.
   * @param {@link Date} 		 dataEsecPagamentoDovuto, 		Indica la data entro la quale si richiede che venga effettuato il pagamento.
   * @param {@link BigDecimal} importoSingoloVersDovuto,		L'importo relativo alla somma da versare.
   * @param {@link String} 	 causaleVersDovuto, 			Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento.
   * @param {@link Integer}	 tipoPagamento,					Tipo pagamento (0 = Contestuale, 1 = Non contestuale)
   * @param {@link String}	 tipoOperazione,				Tipo operazione (C = Update di un avviso esistente, U = Modifica di un avviso esistente, D = Cancellazione di un avviso esistente)
   *
   * @author Marianna Memoli
   * @author Stefano De Feo
   */
  @Async("AvvisiDigitaliTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void manageAvvisoDigitale(
      final Ente ente, final String anagraficaPagatore, final String emailSoggetto, final String cellulareSoggetto,
      final String codIuvDovuto, final String tipoIdUnivocoDovuto, final String codiceIdUnivocoDovuto, final Date dataEsecPagamentoDovuto,
      final BigDecimal importoSingoloVersDovuto, final String causaleVersDovuto, final String azione) {

    log.info("Call async service 'manageAvvisoDigitale' [CodiceIpa: " + ente.getCodIpaEnte() + ", anagraficaPagatore: " + anagraficaPagatore + ", IUV dovuto: " + codIuvDovuto + "]  ... START");

    try {
      /**
       * Controllo se l'ente ha abilitato la funzionalità di avvisatura digitale.
       */
      //Boolean avvisaturaAttiva = enteService.isEnableFunzionalitaByCodIpaEnte(ente.getCodIpaEnte(), Constants.FUNZIONALITA_AVVISATURA_DIGITALE);
      List<EnteFunzionalita> entiFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(ente.getCodIpaEnte(), true);
      boolean avvisaturaAttiva = entiFunzionalita.stream().filter(ef -> Constants.FUNZIONALITA_AVVISATURA_DIGITALE.equals(ef.getCodFunzionalita())).count() == 1;

      if (avvisaturaAttiva) {

        log.info("AVVIO PROCEDURA DI NOTIFICA AVVISO -->> Funzionalita' di '" + Constants.FUNZIONALITA_AVVISATURA_DIGITALE + "' ATTIVA per l'ente con CodIpaEnte '" + ente.getCodIpaEnte() + "'.");

        String idFlusso = "_" + ente.getCodIpaEnte() + Constants.FLUSSO_AVVISO_DIGITALE_WS_SUFFISSO_ID;

        /*
         * La funzione restituisce l'anagrafica del flusso relativo la funzionalità di "AVVISATURA_DIGITALE" per l'ente fornito come
         * parametro d'ingresso.
         * Se mancante, inserisco l'anagrafica del flusso nella tabella "mygov_flusso_avviso_digitale".
         */

        FlussoAvvisoDigitale flusso = avvisoDigitaleService.selectFlussoAvvisoForInsert(ente.getCodiceFiscaleEnte(), Constants.FLUSSO_AVVISO_DIGITALE_WS_TIPO_STATO,
            Constants.FLUSSO_AVVISO_DIGITALE_WS_STATO_DEFAULT, idFlusso, Constants.FLUSSO_AVVISO_DIGITALE_WS_TIPO);

        /* Build string codice avviso */
        String codiceAvviso = "";
        if (codIuvDovuto.length() == Constants.IUV_GENERATOR_15_LENGTH) codiceAvviso = Constants.OLD_IUV_AUX_DIGIT + ente.getApplicationCode() + codIuvDovuto;
        if (codIuvDovuto.length() == Constants.IUV_GENERATOR_17_LENGTH) codiceAvviso = Constants.SMALL_IUV_AUX_DIGIT + codIuvDovuto;

        /**
         * Valorizzato con il valore di default 1
         */
        Integer numAdTentativiInvio = 1;
        /**
         * Valorizzato con la stringa di default '00'
         */
        String codTassonomiaAvviso = "00";

        if(azione.equals("I")){ //Caso Inserimento
          log.info("manageAvvisoDigitale :: Inserimento Avviso Digitale [CodIpaEnte: " + ente.getCodIpaEnte() + " Azione: " + azione + "]");
          AvvisoDigitale avvisoDigitaleEsistente = avvisoDigitaleService.getByIdDominioECodiceAvviso(ente.getCodiceFiscaleEnte(), codiceAvviso);
          /**
           * Controllo se in tabella "mygov_avviso_digitale" non risulti già censito il dovuto come avviso,
           * se non è censito, inserisco un nuovo avviso digitale.
           */
          if(null == avvisoDigitaleEsistente){
            log.info("manageAvvisoDigitale :: Inserimento Avviso Digitale, creo un nuovo avviso digitale da inserire");
            /*
             * Censisco sul DB il record descrittivo dell'avviso digitale con stato "adWS/NUOVO" (Tabella mygov_avviso_digitale).
             */
            AvvisoDigitale avvisoDigitale = avvisoDigitaleService.addNewAvvisoDigitale(
                ente.getCodiceFiscaleEnte(),						/* Codice fiscale della struttura che invia l'avviso Digitale. */
                ente.getDeNomeEnte(),								/* Denominazione dell’Ente Creditore che invia la richiesta di avviso digitale. */
                codiceAvviso,										/* Codice dell’avviso di pagamento. */
                anagraficaPagatore,									/* Indica il nominativo o la ragione sociale del pagatore. */
                tipoIdUnivocoDovuto,								/* Dato alfanumerico che indica la natura del pagatore (F o G). */
                codiceIdUnivocoDovuto, 								/* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
                dataEsecPagamentoDovuto, 							/* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
                Utilities.addDays(dataEsecPagamentoDovuto, 1),		/* Indica la data, successiva alla data di scadenza, sino alla quale si ritiene valido l'avviso. */
                importoSingoloVersDovuto,							/* L'importo relativo alla somma da versare. */
                emailSoggetto, null,								/* Email e cellulare. */
                causaleVersDovuto,									/* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
                Constants.AVVISO_DIGITALE_WS_STATO_NUOVO, 			/* Codice dello stato da assegnare all'avviso. */
                Constants.AVVISO_DIGITALE_WS_TIPO_STATO	,			/* Categoria padre della tipologia di stato. */
                flusso.getCodFadIdFlusso(),							/* Identificativo flusso */
                ente.getCodRpDatiVersDatiSingVersIbanAccredito(), 	/* Iban accredito recuperato dall'ente */
                ente.getCodRpDatiVersDatiSingVersIbanAppoggio(),	/* Iban appoggio recuperato dall'ente */
                1, 													/* Tipo pagamento (0 = Contestuale, 1 = Non contestuale) */
                StTipoOperazione.C.value()							/* Tipo operazione (C = Creazione di un avviso esistente) */
            );

            /*
             * Chiamo la funzione che ha in carico la chiamata al WS e la gestione della response.
             */
            sendAvvisoDigitale(avvisoDigitale.getMygovAvvisoDigitaleId(), ente.getCodiceFiscaleEnte());
          }else{
            /**
             * Controllo se in tabella "mygov_avviso_digitale" risulti già censito il dovuto come avviso,
             * se censito, se anagrafica stato = ERRORE_INVIO ed tipo operazione = C, allora è stato già provata una
             * comunicazione al WS ma andato in errore
             *
             * NOTA: Potrebbe essere funzionalità inutilizzata, ma gestirebbe eventuale condizione
             */
            if(null != avvisoDigitaleEsistente && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO)
                && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.C.value())){
              log.info("manageAvvisoDigitale :: Inserimento Avviso Digitale, avviso digitale già censito, recuperato con stato: " +avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato()+
                  " e tipo operazione: "+avvisoDigitaleEsistente.getTipoOperazione()+ ", imposto anagrafica stato = NUOVO per permettere tentativo di invio aviso al WS");
              /*
               * Imposto anagrafica stato = NUOVO per permettere tentativo di invio aviso al WS
               */
              avvisoDigitaleService.updateAnagraficaStatoDiUnAvvisoDigitaleEsistente(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), Constants.AVVISO_DIGITALE_WS_STATO_NUOVO, Constants.AVVISO_DIGITALE_WS_TIPO_STATO);

              /*
               * Chiamo la funzione che ha in carico la chiamata al WS e la gestione della response.
               */
              sendAvvisoDigitale(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), ente.getCodiceFiscaleEnte());
            }
          }
        }//Fine Caso Inserimento
        else{
          if(azione.equals("A")){ //Caso azione annullamento
            log.info("manageAvvisoDigitale :: Annulla Avviso Digitale [CodIpaEnte: " + ente.getCodIpaEnte() + " Azione: " + azione + "]");
            /*
             * Recupero avviso digitale da annullare
             */
            AvvisoDigitale avvisoDigitaleEsistente = avvisoDigitaleService.getByIdDominioECodiceAvviso(ente.getCodiceFiscaleEnte(), codiceAvviso);

            if(null != avvisoDigitaleEsistente){
              /*
               * Trovato avviso da annullare
               */
              log.info("manageAvvisoDigitale :: L'avviso [CodIpaEnte: " + ente.getCodIpaEnte() + ", AvvisoID: " + avvisoDigitaleEsistente.getMygovAvvisoDigitaleId() + "] ESISTE in tabella, Azione: [" + azione + "]");

              /*
               * Se lo stato dell'avviso da annullare è ANNULLATO, allora l'avviso digitale è stato già annullato e quindi non effettuo nessuna operazione
               */
              if(!avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_ANNULLATO)
                  && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)){

                /*
                 * Condizioni in cui non bisogna richiamare il WS, ma solo modificare l'anagrafica stato dell'avviso
                 */
                if((avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_RICEVUTO_ESITO)
                    && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                    && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.D.value()))
                    ||
                    (avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO)
                        && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                        && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.C.value()))
                    ||
                    (avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_NUOVO)
                        && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                        && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.C.value()))
                ){

                  avvisoDigitaleService.updateAnagraficaStatoDiUnAvvisoDigitaleEsistente(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), Constants.AVVISO_DIGITALE_WS_STATO_ANNULLATO, Constants.AVVISO_DIGITALE_WS_TIPO_STATO);

                }else
                  /*
                   * Condizioni in cui bisogna richiamare il WS
                   */
                  if((avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_RICEVUTO_ESITO)
                      && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                      && !avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.D.value()))
                      ||
                      (avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO)
                          && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                          && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.D.value()))
                      ||
                      (avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO)
                          && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                          && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.U.value()))
                      ||
                      (avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_NUOVO)
                          && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                          && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.U.value()))
                  ){

                    avvisoDigitaleService.updateTipoOpeAnagStatoAvvDigitaleEsistente(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), Constants.AVVISO_DIGITALE_WS_STATO_NUOVO, Constants.AVVISO_DIGITALE_WS_TIPO_STATO,  StTipoOperazione.D.value());
                    /*
                     * Chiamo la funzione che ha in carico la chiamata al WS e la gestione della response.
                     */
                    sendAvvisoDigitale(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), ente.getCodiceFiscaleEnte());
                  }
              }else //Dato che lo stato dell'avviso è già ANNULLATO non prendo in considerazione la richiesta di annullamento
                log.info("manageAvvisoDigitale :: L'avviso [CodIpaEnte: " + ente.getCodIpaEnte() + ", AvvisoID: " + avvisoDigitaleEsistente.getMygovAvvisoDigitaleId() + "] risulta già ANNULLATO.");
            }else //L'avviso da annullare non esiste
              log.info("manageAvvisoDigitale :: L'avviso digitale da annullare non esiste, azione: "+ azione);
          }// Fine Caso azione annullamento
          else
          if(azione.equals("M")) {//Caso azione modifica

            log.info("manageAvvisoDigitale :: Modifica Avviso Digitale [CodIpaEnte: " + ente.getCodIpaEnte() + " Azione: " + azione + "]");
            /*
             * Recupero avviso digitale da annullare
             */
            AvvisoDigitale avvisoDigitaleEsistente = avvisoDigitaleService.getByIdDominioECodiceAvviso(ente.getCodiceFiscaleEnte(), codiceAvviso);

            if(null != avvisoDigitaleEsistente){
              /*
               * Trovato avviso da modificare
               */
              log.info("manageAvvisoDigitale :: L'avviso [CodIpaEnte: " + ente.getCodIpaEnte() + ", AvvisoID: " + avvisoDigitaleEsistente.getMygovAvvisoDigitaleId() + "] ESISTE in tabella, Azione: [" + azione + "]");
              /*
               * Se lo stato dell'avviso da annullare è ANNULLATO, allora l'avviso digitale è stato già annullato e quindi non effettuo nessuna operazione
               */
              if((avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_ANNULLATO)
                  && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO))
                  ||
                  (avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_NUOVO)
                      && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                      && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.D.value()))
              )
                //Dato che lo stato dell'avviso è ANNULLATO o si attende l'annullamento dell'ultimo, non prendo in considerazione la richiesta di modifica
                log.info("manageAvvisoDigitale :: L'avviso [CodIpaEnte: " + ente.getCodIpaEnte() + ", AvvisoID: " + avvisoDigitaleEsistente.getMygovAvvisoDigitaleId() + "] risulta ANNULLATO o in attesa di annullamento.");
              else
                /*
                 * Condizioni in cui bisogna apportare le modifiche all'avviso lasciando invariato anagrafica stato e tipo operazione, richiamare il WS
                 */
                if((avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_NUOVO)
                    && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                    && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.C.value()))
                    ||
                    (avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_NUOVO)
                        && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                        && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.U.value()))
                ){

                  avvisoDigitaleService.updateAvviso(
                      avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(),	/* Identificativo record */
                      ente.getCodiceFiscaleEnte(),  		/* Codice fiscale della struttura che invia la notifica. */
                      ente.getDeNomeEnte(),		  		/* Denominazione dell’Ente Creditore che invia la richiesta di notifica. */
                      avvisoDigitaleEsistente.getCodAdIdMessaggioRichiesta(),	  		/* Identificativo del messaggio di richiesta */
                      codiceAvviso,				  		/* Codice dell’avviso di pagamento. */
                      anagraficaPagatore,			  		/* Indica il nominativo o la ragione sociale del pagatore. */
                      tipoIdUnivocoDovuto,				/* Dato alfanumerico che indica la natura del pagatore (F o G). */
                      codiceIdUnivocoDovuto, 			  	/* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
                      dataEsecPagamentoDovuto, 			/* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
                      Utilities.addDays(dataEsecPagamentoDovuto, 1),				  		/* Indica la data sino alla quale si ritiene valido l'avviso. */
                      importoSingoloVersDovuto,			/* L'importo relativo alla somma da versare. */
                      emailSoggetto, null,		  		/* Email e cellulare. */
                      causaleVersDovuto,					/* Motivazioni del pagamento. */
                      flusso.getCodFadIdFlusso(),	  		/* Identificativo flusso */
                      numAdTentativiInvio,		  		/* */
                      codTassonomiaAvviso,		  		/* */
                      avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato(),	/* Categoria padre della tipologia di stato. */
                      avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato(),	/* Codice dello stato da assegnare all'avviso. */
                      ente.getCodRpDatiVersDatiSingVersIbanAccredito(), 	/* Iban accredito recuperato dall'ente */
                      ente.getCodRpDatiVersDatiSingVersIbanAppoggio(),	/* Iban appoggio recuperato dall'ente */
                      avvisoDigitaleEsistente.getTipoPagamento(), 		/* Tipo pagamento */
                      avvisoDigitaleEsistente.getTipoOperazione()			/* Tipo operazione */
                  );

                  /*
                   * Chiamo la funzione che ha in carico la chiamata al WS e la gestione della response.
                   */
                  sendAvvisoDigitale(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), ente.getCodiceFiscaleEnte());
                }else
                  /*
                   * Condizioni in cui bisogna apportare le modifiche all'avviso, impostare anagrafica stato = NUOVO e lasciando invariato tipo operazione, richiamare il WS
                   */
                  if((avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO)
                      && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                      && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.C.value()))
                      ||
                      (avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO)
                          && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                          && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.U.value()))
                  ){

                    avvisoDigitaleService.updateAvviso(
                        avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(),	/* Identificativo record */
                        ente.getCodiceFiscaleEnte(),  		/* Codice fiscale della struttura che invia la notifica. */
                        ente.getDeNomeEnte(),		  		/* Denominazione dell’Ente Creditore che invia la richiesta di notifica. */
                        avvisoDigitaleEsistente.getCodAdIdMessaggioRichiesta(),	  		/* Identificativo del messaggio di richiesta */
                        codiceAvviso,				  		/* Codice dell’avviso di pagamento. */
                        anagraficaPagatore,			  		/* Indica il nominativo o la ragione sociale del pagatore. */
                        tipoIdUnivocoDovuto,				/* Dato alfanumerico che indica la natura del pagatore (F o G). */
                        codiceIdUnivocoDovuto, 			  	/* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
                        dataEsecPagamentoDovuto, 			/* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
                        Utilities.addDays(dataEsecPagamentoDovuto, 1),				  		/* Indica la data sino alla quale si ritiene valido l'avviso. */
                        importoSingoloVersDovuto,			/* L'importo relativo alla somma da versare. */
                        emailSoggetto, null,		  		/* Email e cellulare. */
                        causaleVersDovuto,					/* Motivazioni del pagamento. */
                        flusso.getCodFadIdFlusso(),	  		/* Identificativo flusso */
                        numAdTentativiInvio,		  		/* */
                        codTassonomiaAvviso,		  		/* */
                        avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato(),	/* Categoria padre della tipologia di stato. */
                        Constants.AVVISO_DIGITALE_WS_STATO_NUOVO,			/* Codice dello stato da assegnare all'avviso. */
                        ente.getCodRpDatiVersDatiSingVersIbanAccredito(), 	/* Iban accredito recuperato dall'ente */
                        ente.getCodRpDatiVersDatiSingVersIbanAppoggio(),	/* Iban appoggio recuperato dall'ente */
                        avvisoDigitaleEsistente.getTipoPagamento(), 		/* Tipo pagamento */
                        avvisoDigitaleEsistente.getTipoOperazione()			/* Tipo operazione */
                    );

                    /*
                     * Chiamo la funzione che ha in carico la chiamata al WS e la gestione della response.
                     */
                    sendAvvisoDigitale(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), ente.getCodiceFiscaleEnte());

                  }else
                    /*
                     * Condizioni in cui non bisogna apportare le modifiche all'avviso, impostare anagrafica stato = NUOVO e lasciando invariato tipo operazione, richiamare il WS
                     */
                    if(avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO)
                        && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                        && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.D.value())){

                      avvisoDigitaleService.updateAnagraficaStatoDiUnAvvisoDigitaleEsistente(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), Constants.AVVISO_DIGITALE_WS_STATO_NUOVO, Constants.AVVISO_DIGITALE_WS_TIPO_STATO);

                      /*
                       * Chiamo la funzione che ha in carico la chiamata al WS e la gestione della response.
                       */
                      sendAvvisoDigitale(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), ente.getCodiceFiscaleEnte());

                    }else
                      /*
                       * Condizioni in cui bisogna apportare le modifiche all'avviso, impostare anagrafica stato = NUOVO, tipo operazione = U, richiamare il WS
                       */
                      if(avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_RICEVUTO_ESITO)
                          && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                          && !avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.D.value())){

                        avvisoDigitaleService.updateAvviso(
                            avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(),	/* Identificativo record */
                            ente.getCodiceFiscaleEnte(),  		/* Codice fiscale della struttura che invia la notifica. */
                            ente.getDeNomeEnte(),		  		/* Denominazione dell’Ente Creditore che invia la richiesta di notifica. */
                            avvisoDigitaleEsistente.getCodAdIdMessaggioRichiesta(),	  		/* Identificativo del messaggio di richiesta */
                            codiceAvviso,				  		/* Codice dell’avviso di pagamento. */
                            anagraficaPagatore,			  		/* Indica il nominativo o la ragione sociale del pagatore. */
                            tipoIdUnivocoDovuto,				/* Dato alfanumerico che indica la natura del pagatore (F o G). */
                            codiceIdUnivocoDovuto, 			  	/* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
                            dataEsecPagamentoDovuto, 			/* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
                            Utilities.addDays(dataEsecPagamentoDovuto, 1),				  		/* Indica la data sino alla quale si ritiene valido l'avviso. */
                            importoSingoloVersDovuto,			/* L'importo relativo alla somma da versare. */
                            emailSoggetto, null,		  		/* Email e cellulare. */
                            causaleVersDovuto,					/* Motivazioni del pagamento. */
                            flusso.getCodFadIdFlusso(),	  		/* Identificativo flusso */
                            numAdTentativiInvio,		  		/* */
                            codTassonomiaAvviso,		  		/* */
                            avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato(),	/* Categoria padre della tipologia di stato. */
                            Constants.AVVISO_DIGITALE_WS_STATO_NUOVO,			/* Codice dello stato da assegnare all'avviso. */
                            ente.getCodRpDatiVersDatiSingVersIbanAccredito(), 	/* Iban accredito recuperato dall'ente */
                            ente.getCodRpDatiVersDatiSingVersIbanAppoggio(),	/* Iban appoggio recuperato dall'ente */
                            avvisoDigitaleEsistente.getTipoPagamento(), 		/* Tipo pagamento */
                            StTipoOperazione.U.value()							/* Tipo operazione */
                        );

                        /*
                         * Chiamo la funzione che ha in carico la chiamata al WS e la gestione della response.
                         */
                        sendAvvisoDigitale(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), ente.getCodiceFiscaleEnte());

                      }else
                        /*
                         * Condizioni in cui non bisogna apportare le modifiche all'avviso, impostare anagrafica stato = ANNULLATO e lasciare invariato tipo operazione = U, senza richiamare il WS
                         */
                        if(avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_RICEVUTO_ESITO)
                            && avvisoDigitaleEsistente.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)
                            && avvisoDigitaleEsistente.getTipoOperazione().equalsIgnoreCase(StTipoOperazione.D.value())){

                          avvisoDigitaleService.updateAnagraficaStatoDiUnAvvisoDigitaleEsistente(avvisoDigitaleEsistente.getMygovAvvisoDigitaleId(), Constants.AVVISO_DIGITALE_WS_STATO_ANNULLATO, Constants.AVVISO_DIGITALE_WS_TIPO_STATO);

                        }
            }else //L'avviso da modificare non esiste
              log.info("manageAvvisoDigitale :: L'avviso digitale da modificare non esiste, azione: "+ azione);
          }// Fine Caso azione modifica
        }
      }// close if
      else{
        /*
         * Interrompo la procedura perchè la funzionalità di avviso digitale non è abilitata per l'ente
         */
        log.info("NON AVVIO PROCEDURA DI NOTIFICA AVVISO -->> Funzionalita' di '" + Constants.FUNZIONALITA_AVVISATURA_DIGITALE + "' NON ATTIVA per l'ente con CodIpaEnte '" + ente.getCodIpaEnte() + "'.");
      }

    }catch (Exception e) {
      log.error("Errore nella chiamata al metodo 'manageAvvisoDigitale' [CodiceIpa: " + ente.getCodIpaEnte() + ", anagraficaPagatore: " + anagraficaPagatore + ", IUV dovuto: " + codIuvDovuto + "]", e);
    }finally {
      log.info("Call async service 'manageAvvisoDigitale' [CodiceIpa: " + ente.getCodIpaEnte() + ", anagraficaPagatore: " + anagraficaPagatore + ", IUV dovuto: " + codIuvDovuto + "]  ... END");
    }
  }

  /**
   * La funzione gestisce l'inserimento, la modifica e l'annullamento dell'avviso di pagamento da notificare alla "Piattaforma IO".
   *
   * Fasi del processo:
   * ----------------------------------------------------------------------------------------------------------------------------------------------
   *  1. Verifico che sia l'ente che il tipo dovuto dell'avviso di pagamento siano stati abilitati alla notifica, se cosi non fosse interrompo
   *     la procedura.
   *
   * 	2. Verifico che nella tabella "mygov_flusso_avviso_digitale" esista un flusso "fttizio" relativo la funzionalità di "Notifica Avviso"
   * 	   per l'ente considerato. Se mancante, inserisco l'anagrafica del flusso.
   *
   * 	3. Nel caso d'inserimento aggiungo una riga per ogni avviso in tabella. Nel caso di modifica o annullamento, procedo solo se la riga relativa
   *     l'avviso è in stato "NUOVO".
   *
   * @param {@link Ente} 		 ente, 				  Anagrafica Ente
   * @param {@link String} 	 anagraficaPagatore,  Indica il nominativo o la ragione sociale del pagatore.
   * @param {@link String} 	 tipoIdUnivoco,  	  Dato alfanumerico che indica la natura del pagatore (F o G)
   * @param {@link String} 	 codiceIdUnivoco,	  Il codice fiscale o, in alternativa, la partita IVA del pagatore.
   * @param {@link Date} 		 dataScadPagamento,   Indica la data entro la quale si richiede che venga effettuato il pagamento.
   * @param {@link Date} 		 dataScadAvviso, 	  Indica la data sino alla quale si ritiene valido l'avviso.
   * @param {@link BigDecimal} importoVersamento,	  L'importo relativo alla somma da versare.
   * @param {@link String} 	 emailSoggetto, 	  Indirizzo di posta elettronica del soggetto al quale è indirizzato l'avviso.
   * @param {@link String} 	 causaleVersamento,   Motivazione del pagamento.
   * @param {@link String} 	 codIuv, 		  	  IUV avviso.
   * @param {@link String} 	 codTipoDovuto, 	  Tipo dovuto corrispondente l'avviso.
   * @param {@link String}	 azione, 			  Indica l'operazione da compiere sul dovuto. I valori considerati e le relative descrizioni sono:
   * 													'M' indica che il dovuto deve essere modificato;
   * 													'A' indica che il dovuto deve essere annullato.
   *
   * @author Marianna Memoli
   */
  @Async("AvvisiDigitaliTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void manageNotificaIO(
      final Ente ente, final String anagraficaPagatore, final String tipoIdUnivoco, final String codiceIdUnivoco,
      final Date dataScadPagamento, final Date dataScadAvviso, final BigDecimal importoVersamento, final String emailSoggetto,
      final String causaleVersamento, final String codIuv, final String codTipoDovuto, final String azione) {

    log.info("Call async service 'manageNotificaIO' [CodiceIpa: " + ente.getCodIpaEnte() + ", anagraficaPagatore: " + anagraficaPagatore + ", IUV dovuto: " + codIuv + "]  ... START");

    try {
      /*
       */
      //Boolean notificaAttiva    = enteService.isEnableFunzionalitaByCodIpaEnte(ente.getCodIpaEnte(), Constants.FUNZIONALITA_NOTIFICA_AVVISI_IO);
      List<EnteFunzionalita> entiFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(ente.getCodIpaEnte(), true);
      boolean notificaAttiva = entiFunzionalita.stream().filter(ef -> Constants.FUNZIONALITA_NOTIFICA_AVVISI_IO.equals(ef.getCodFunzionalita())).count() == 1;
      EnteTipoDovuto tipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(codTipoDovuto, ente.getCodIpaEnte(),true)
          .orElseThrow(()-> new NotFoundException("tipoDovuto not found or not active"));

      /*
       * Verifico che sia l'ente che il tipo dovuto dell'avviso di pagamento siano stati abilitati alla notifica, se cosi non fosse interrompo
       * la procedura.
       */
      if (notificaAttiva && tipoDovuto != null && tipoDovuto.isFlgNotificaIo()) {

        log.info("manageNotificaIO :: Funzionalita' di '" + Constants.FUNZIONALITA_NOTIFICA_AVVISI_IO + "' ATTIVA per l'ente con CodIpaEnte '" + ente.getCodIpaEnte() + "'.");

        /* ***************************************************************************************************************************** */

        /**
         * Valorizzato con null
         */
        String codAdIdMessaggioRichiesta = null;

        /**
         * Per i codici IUV di lunghezza 15 valorizzato come : "0_" + <Application Code dell'ente> + "_" + <Codice Iuv>
         * Per i codici IUV di lunghezza 17 valorizzato come : "3_" + <Codice Iuv>
         */
        String codAdCodAvviso = "";

        if (codIuv.length() == Constants.IUV_GENERATOR_15_LENGTH) codAdCodAvviso = "0_" + ente.getApplicationCode() + "_" + codIuv;
        if (codIuv.length() == Constants.IUV_GENERATOR_17_LENGTH) codAdCodAvviso = Constants.SMALL_IUV_AUX_DIGIT + "_" + codIuv;

        /**
         * Valorizzato con una stringa nel formato : _<codIpaEnte>_NOTIFICA-AVVISO-IO
         */
        String codIdFlussoAv = "_" + ente.getCodIpaEnte() + Constants.FLUSSO_NOTIFICA_AVVISO_IO_SUFFISSO_ID;

        /**
         * Valorizzato con il valore di default 1
         */
        Integer numAdTentativiInvio = 1;

        /**
         * Valorizzato con la stringa di default '00'
         */
        String codTassonomiaAvviso = "00";

        /**
         * La funzione restituisce l'anagrafica del flusso relativo la funzionalità di "Notifica alla Piattaforma IO" per l'ente fornito come
         * parametro d'ingresso.
         * Se mancante, inserisco l'anagrafica del flusso nella tabella "mygov_flusso_avviso_digitale".
         */
        FlussoAvvisoDigitale flusso = avvisoDigitaleService.selectFlussoAvvisoForInsert(
            ente.getCodiceFiscaleEnte(), Constants.FLUSSO_NOTIFICA_AVVISO_IO_TIPO_STATO, Constants.FLUSSO_NOTIFICA_AVVISO_IO_STATO_DEFAULT,
            codIdFlussoAv, Constants.FLUSSO_NOTIFICA_AVVISO_IO_TIPO);

        /* ****************************************************************************************************************************** */

        AvvisoDigitale avvisoDigitale = avvisoDigitaleService.getByIdDominioECodiceAvviso(ente.getCodiceFiscaleEnte(), codAdCodAvviso);

        /**
         * Controllo se in tabella "mygov_avviso_digitale" non risulti già censito il dovuto come avviso,
         * perchè vorrebbe dire che è stato già preso in carico per essere notificato.
         */
        if(avvisoDigitale == null){
          /*
           * Censisco sul DB il record descrittivo della notifica di avviso con stato "naIO/NUOVO" (Tabella mygov_avviso_digitale).
           */
          avvisoDigitaleService.addNewAvviso(
              ente.getCodiceFiscaleEnte(),  /* Codice fiscale della struttura che invia la notifica. */
              ente.getDeNomeEnte(),		  /* Denominazione dell’Ente Creditore che invia la richiesta di notifica. */
              codAdIdMessaggioRichiesta,	  /* Identificativo del messaggio di richiesta */
              codAdCodAvviso,				  /* Codice dell’avviso di pagamento. */
              anagraficaPagatore,			  /* Indica il nominativo o la ragione sociale del pagatore. */
              tipoIdUnivoco,				  /* Dato alfanumerico che indica la natura del pagatore (F o G). */
              codiceIdUnivoco, 			  /* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
              dataScadPagamento, 			  /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
              dataScadAvviso,				  /* Indica la data sino alla quale si ritiene valido l'avviso. */
              importoVersamento,			  /* L'importo relativo alla somma da versare. */
              emailSoggetto, null,		  /* Email e cellulare. */
              causaleVersamento,			  /* Motivazioni del pagamento. */
              flusso.getCodFadIdFlusso(),	  /* Identificativo del flusso */
              numAdTentativiInvio,		  /* */
              codTassonomiaAvviso,		  /* */
              Constants.NOTIFICA_AVVISO_IO_STATO_NUOVO,	/* Codice dello stato da assegnare all'avviso. */
              Constants.NOTIFICA_AVVISO_IO_TIPO_STATO,		/* Categoria padre della tipologia di stato. */
              ente.getCodRpDatiVersDatiSingVersIbanAccredito(),	/* Iban accredito recuperato da ente */
              ente.getCodRpDatiVersDatiSingVersIbanAppoggio(),	/* Iban appoggio recuperato da ente */
              1,													/* Tipo pagamento (0 = Contestuale, 1 = Non contestuale) */
              StTipoOperazione.C.value()							/* Tipo operazione (C = Creazione di un nuovo avviso) */
          );

          log.info("manageNotificaIO :: L'avviso [CodIpaEnte: " + ente.getCodIpaEnte() + ", CodAvviso: " + codAdCodAvviso + "] INSERITO con SUCCESSO.");
        }else{
          /**
           * Il dovuto che si sta considerando risulta già censito nella tabella "mygov_avviso_digitale",
           * quindi sono nel caso di modifica o annullamento, procedo solo se la riga relativa l'avviso è in stato "NUOVO".
           */
          log.info("manageNotificaIO :: L'avviso [CodIpaEnte: " + ente.getCodIpaEnte() + ", AvvisoID: " + avvisoDigitale.getMygovAvvisoDigitaleId() + "] ESISTE in tabella, Azione: [" + azione + "]");

          /*
           */
          if(avvisoDigitale.getMygovAnagraficaStatoId().getCodStato().equals(Constants.NOTIFICA_AVVISO_IO_TIPO_STATO)
              && avvisoDigitale.getMygovAnagraficaStatoId().getDeStato().equals(Constants.NOTIFICA_AVVISO_IO_STATO_NUOVO)){

            if(azione.equals("A")){
              /*
               * Dovuto annullato, cambio lo stato dell'avviso digitale
               */
              avvisoDigitaleService.updateTipoOpeAnagStatoAvvDigitaleEsistente(avvisoDigitale.getMygovAvvisoDigitaleId(), Constants.NOTIFICA_AVVISO_IO_STATO_ANNULLATO, Constants.NOTIFICA_AVVISO_IO_TIPO_STATO, StTipoOperazione.D.value());

              log.info("manageNotificaIO :: L'avviso [CodIpaEnte: " + ente.getCodIpaEnte() + ", AvvisoID: " + avvisoDigitale.getMygovAvvisoDigitaleId() + "] ANNULLATO con SUCCESSO.");
            }else
            if(azione.equals("M")) {
              /*
               * Dovuto modificato, aggiorno il record dell'avviso digitale
               */
              avvisoDigitaleService.updateAvviso(
                  avvisoDigitale.getMygovAvvisoDigitaleId(),		  /* Identificativo record */
                  ente.getCodiceFiscaleEnte(),  /* Codice fiscale della struttura che invia la notifica. */
                  ente.getDeNomeEnte(),		  /* Denominazione dell’Ente Creditore che invia la richiesta di notifica. */
                  codAdIdMessaggioRichiesta,	  /* Identificativo del messaggio di richiesta */
                  codAdCodAvviso,				  /* Codice dell’avviso di pagamento. */
                  anagraficaPagatore,			  /* Indica il nominativo o la ragione sociale del pagatore. */
                  tipoIdUnivoco,				  /* Dato alfanumerico che indica la natura del pagatore (F o G). */
                  codiceIdUnivoco, 			  /* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
                  dataScadPagamento, 			  /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
                  dataScadAvviso,				  /* Indica la data sino alla quale si ritiene valido l'avviso. */
                  importoVersamento,			  /* L'importo relativo alla somma da versare. */
                  emailSoggetto, null,		  /* Email e cellulare. */
                  causaleVersamento,			  /* Motivazioni del pagamento. */
                  flusso.getCodFadIdFlusso(),	  /* Identificativo flusso */
                  numAdTentativiInvio,		  /* */
                  codTassonomiaAvviso,		  /* */
                  Constants.NOTIFICA_AVVISO_IO_TIPO_STATO,	/* Categoria padre della tipologia di stato. */
                  Constants.NOTIFICA_AVVISO_IO_STATO_NUOVO,	/* Codice dello stato da assegnare all'avviso. */
                  ente.getCodRpDatiVersDatiSingVersIbanAccredito(), 	/* Iban accredito recuperato dall'ente */
                  ente.getCodRpDatiVersDatiSingVersIbanAppoggio(),	/* Iban appoggio recuperato dall'ente */
                  avvisoDigitale.getTipoPagamento(), 		/* Tipo pagamento */
                  StTipoOperazione.U.value()				/* Tipo operazione */
              );

              log.info("manageNotificaIO :: L'avviso [CodIpaEnte: " + ente.getCodIpaEnte() + ", AvvisoID: " + avvisoDigitale.getMygovAvvisoDigitaleId() + "] AGGIORNATO con SUCCESSO.");
            }
            else
              /*
               * Interrompo la procedura perchè dal valore di "azione" non si è determinato se si tratta di modifica o annullamento del dovuto.
               */
              log.warn("manageNotificaIO :: Nessuna operazione di eseguita per il dovuto con IUV[" + codIuv + "] e azione[" + azione + "].");

          }// close if stato != nuovo
        }// close else avvisoDigitale != null

      }// close if
      else
      if(!notificaAttiva){
        /**
         * Caso funzionalità di notifica non attiva per l'Ente
         */
          /*
           * Interrompo la procedura perchè la funzionalità di "Notifica alla Piattaforma IO" non è abilitata per l'ente.
           */
          log.info("manageNotificaIO :: Funzionalita' di '" + Constants.FUNZIONALITA_NOTIFICA_AVVISI_IO + "' NON ATTIVA per l'ente con CodIpaEnte '" + ente.getCodIpaEnte() + "'.");
      }else{
        /**
         * Caso funzionalità di notifica attiva per l'Ente, ma il tipo dovuto non è associato all'ente o non ha attiva la notifica.
         */
        log.info("manageNotificaIO :: Funzionalita' di '" + Constants.FUNZIONALITA_NOTIFICA_AVVISI_IO + "' ATTIVA per l'ente con CodIpaEnte '" + ente.getCodIpaEnte() + "'.");
        /*
         */
        if(tipoDovuto == null)
          /*
           * Interrompo la procedura perchè il tipo dovuto non risulta associato all'ente nel DB.
           */
          log.warn("manageNotificaIO :: Il dovuto con codice '" + codTipoDovuto + "' risulata NON ASSOCIATO all'ente con CodIpaEnte '" + ente.getCodIpaEnte() + "'.");
        else
          /*
           * Interrompo la procedura perchè per il tipo dovuto non è attiva la "Notifica alla Piattaforma IO".
           */
          log.info("manageNotificaIO :: Per il dovuto con codice '" + codTipoDovuto + "' e Ente con CodIpaEnte '" + ente.getCodIpaEnte() + "' NON È ATTIVA la Notifica alla Piattaforma IO.");
      }// close else

    }catch (Exception e) {
      log.error("manageNotificaIO :: Errore nella chiamata al metodo 'manageNotificaIO' [CodiceIpa: " + ente.getCodIpaEnte() + ", anagraficaPagatore: " + anagraficaPagatore + ", IUV dovuto: " + codIuv + "]", e);
    }finally {
      log.info("Call async service 'manageNotificaIO' [CodiceIpa: " + ente.getCodIpaEnte() + ", anagraficaPagatore: " + anagraficaPagatore + ", IUV dovuto: " + codIuv + "]  ... END");
    }
  }

  /**
   * Chiamo il WS dell'avvisatura esposto dal nodo-regionale-fesp
   *
   * @param {@link Long}   idAvviso, 	Identificativo interno dell'avviso
   * @param {@link String} idDominio	Identificativo del dominio
   *
   * @author Marianna Memoli
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public void sendAvvisoDigitale(final Long idAvviso, String idDominio) {
    log.info("Call async service 'sendAvvisoDigitale'  ... START");

    AvvisoDigitale avvisoDigitale = avvisoDigitaleService.getById(idAvviso);

    Assert.notNull(avvisoDigitale, "Object AvvisoDigitale è nullo.");

    /**
     * Controllo se l'avviso c'è ed è nello stato nuovo del ws
     */
    if(avvisoDigitale.getMygovAnagraficaStatoId().getCodStato().equals(Constants.AVVISO_DIGITALE_WS_STATO_NUOVO) &&
        avvisoDigitale.getMygovAnagraficaStatoId().getDeTipoStato().equals(Constants.AVVISO_DIGITALE_WS_TIPO_STATO)){
      try {
        /**
         * Chiamata al Web Services
         */
        NodoSILInviaAvvisoDigitaleRisposta risposta = pagamentiTelematiciAvvisiDigitaliClient.
            nodoSILInviaAvvisoDigitale(prepareBodyRequestWS(avvisoDigitale), prepareHeaderRequestWS(idDominio));

        if (risposta.getFault() != null) {
          String msg = "WebService 'nodoSILInviaAvvisoDigitale' return KO response | " +
              "FaultBean [id=" + risposta.getFault().getId() + ", faultString=" + risposta.getFault().getFaultString() +
              ", faultCode=" + risposta.getFault().getFaultCode() +  ", description=" + risposta.getFault().getDescription() +
              ", originalFaultString=" + risposta.getFault().getFaultString() +
              ", originalFaultCode=" + risposta.getFault().getFaultCode() +  ", originalFaultdescription=" + risposta.getFault().getDescription() +"]";

          log.error(msg);

          /*
           * Aggiorno lo stato dell'avviso digitale in "adWS/ERRORE_INVIO" (Tabella mygov_avviso_digitale).
           */
          avvisoDigitaleService.updateAnagraficaStatoDiUnAvvisoDigitaleEsistente(avvisoDigitale.getMygovAvvisoDigitaleId(), Constants.AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO, Constants.AVVISO_DIGITALE_WS_TIPO_STATO);
        } else {

          log.info("WebService 'nodoSILInviaAvvisoDigitale' return Esito: " + risposta.getEsitoOperazione());

          try{
            log.debug("Update dello stato del record dell'avviso digitale [Avviso ID: " + avvisoDigitale.getMygovAvvisoDigitaleId() + " | Stato da aggiornare: " + Constants.AVVISO_DIGITALE_WS_STATO_RICEVUTO_ESITO + "]");

            /*
             * Aggiorno lo stato dell'avviso digitale in "adWS/RICEVUTO_ESITO" (Tabella mygov_avviso_digitale).
             */
            avvisoDigitaleService.updateAnagraficaStatoDiUnAvvisoDigitaleEsistente(avvisoDigitale.getMygovAvvisoDigitaleId(), Constants.AVVISO_DIGITALE_WS_STATO_RICEVUTO_ESITO, Constants.AVVISO_DIGITALE_WS_TIPO_STATO);
          }catch(Exception e){
            log.error("Errore nell'aggiornamento dello stato del record dell'avviso digitale [Avviso ID: " + avvisoDigitale.getMygovAvvisoDigitaleId() + " | Stato da aggiornare: " + Constants.AVVISO_DIGITALE_WS_STATO_RICEVUTO_ESITO + "]");
          }

          /* Get dettaglio esito */
          CtEsitoAvvisoDigitale esitoWS = risposta.getEsitoAvvisoDigitaleWS();

          try{
            /**
             * Controloo se la response del WS ha restituito vuoto "CtEsitoAvvisoDigitale"
             */
            Assert.notNull(esitoWS, "Nella response del WS l'oggetto 'CtEsitoAvvisoDigitale' è nullo.");

            try{
              log.debug("Update colonne esito del record dell'avviso digitale [Avviso ID: " + avvisoDigitale.getMygovAvvisoDigitaleId());

              /*
               * Aggiorno le colonne predisposte per i dati di esito dell'avviso digitale in "adWS/RICEVUTO_ESITO"
               * (Tabella mygov_avviso_digitale).
               */
              avvisoDigitale = avvisoDigitaleService.updateWSEsito(avvisoDigitale.getMygovAvvisoDigitaleId(), esitoWS.getIdentificativoDominio(), esitoWS.getIdentificativoMessaggioRichiesta(), avvisoDigitale.getCodIdFlussoAv(), 1);
            }catch(Exception e){
              log.error("Errore nell'aggiornamento delle colonne di esito del record dell'avviso digitale [Avviso ID: " + avvisoDigitale.getMygovAvvisoDigitaleId() + "]");
            }

            /* Struttura che contiene gli esiti del singolo invio di Avviso Digitale per tipo di canale. */
            List<CtEsitoAvvisatura> listaEsitoCanale = esitoWS.getEsitoAvvisaturas();

            log.info(
                "CtEsitoAvvisoDigitale " +
                    "[identificativoDominio=" + esitoWS.getIdentificativoDominio() +
                    ", identificativoMessaggioRichiesta=" + esitoWS.getIdentificativoMessaggioRichiesta() +
                    ", esitoAvvisatura=" + (listaEsitoCanale != null ? listaEsitoCanale.size() : listaEsitoCanale ) + "]");

            /* Ciclo la lista di esiti e per canale scrivo i dati nella tabella "mygov_esito_avviso_digitale" */
            for (CtEsitoAvvisatura item : listaEsitoCanale) {
              try {
                log.info("CtEsitoAvvisatura [tipoCanaleEsito=" + item.getTipoCanaleEsito() +
                    ", identificativoCanale=" + item.getIdentificativoCanale() + ", dataEsito=" + item.getDataEsito() +
                    ", codiceEsito=" + item.getCodiceEsito() + ", descrizioneEsito=" + item + "]");

                /* Scrivo sul DB l'esito */
                avvisoDigitaleService.insertEsitoAvvisoDigitale(
                    avvisoDigitale, 							  /* Anagrafica avviso */
                    Integer.parseInt(item.getTipoCanaleEsito()),  /* Tipologia di canale usato per inviare l’avviso all'utente. */
                    item.getIdentificativoCanale(), 			  /* Identificativo del canale "mobile" a cui si riferisce l’esito dell’avvisatura. */
                    Utilities.toDate(item.getDataEsito()), 		  /* Data di produzione dell'esito da parte del NodoSPC o del canale di avvisatura utilizzato */
                    item.getCodiceEsito(), 						  /* Esito dell'invio riferito al singolo canale. */
                    item.getDescrizioneEsito()); 				  /* Testo libero che, in caso di esito negativo (codiceEsito<>0), descrive l’evento stesso. */
              }
              catch (Exception e) {
                log.error("Errore nella scrittura del record di esito dell'avviso digitale. ", e);
              }
            }// close for
          }catch(Exception e){
            log.error("Errore nella gestione della response del WS", e);
          }

        }// close else OK response

        log.info("Call async service 'sendAvvisoDigitale' ... END");
      }catch (Exception e) {
        log.error("Errore nella chiamata WebService nodoSILInviaAvvisoDigitale a FESP", e);

        /**
         * Aggiorno lo stato dell'avviso digitale in "adWS/ERRORE_INVIO" (Tabella mygov_avviso_digitale).
         */
        avvisoDigitaleService.updateAnagraficaStatoDiUnAvvisoDigitaleEsistente(avvisoDigitale.getMygovAvvisoDigitaleId(), Constants.AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO, Constants.AVVISO_DIGITALE_WS_TIPO_STATO);
      }
    }
//		else
//		  /**
//		   * Ignoro l'avviso che si sta considerando perchè nella tabella "mygov_avviso_digitale" risulta essere in un stato diverso da "adWS/NUOVO".
//		   *
//		   * NOTA: Gli stati potrebbero essere : 'NOTIFICATO', 'ERRORE'
//		   */
//		  log.warn("L'avviso [AvvisoID: " + idAvviso +
//				   ", Stato: " + avvisoDigitale.getMygovAnagraficaStatoId().getCodStato() + "/" + avvisoDigitale.getMygovAnagraficaStatoId().getDeTipoStato() +
//				   "] NON verrà notificato da WS perchè risulta essere in un stato diverso da 'adWS/NUOVO'.");
  }



  @Bean("AvvisiDigitaliTaskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Integer.parseInt(corePoolSize));
    executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
    executor.setQueueCapacity(Integer.parseInt(queueCapacity));
    executor.setThreadNamePrefix("MyPay4AvvisiDigitali-");
    executor.initialize();
    return executor;
  }

}
