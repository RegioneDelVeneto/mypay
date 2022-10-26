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
package it.regioneveneto.mygov.payment.mypay4.util;

import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.exception.fesp.FespValidatorException;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Constants {


  public enum VERSIONE_TRACCIATO_EXPORT {
    VERSIONE_1_0("1.0"), VERSIONE_1_1("1.1"), VERSIONE_1_2("1.2");
    String value;

    public String getValue() {
      return this.value;
    }

    VERSIONE_TRACCIATO_EXPORT(String value) {
      this.value = value;
    }
  }

  public enum ESITO {
    OK("OK"), KO("KO");
    String value;
    ESITO(String value) {
      this.value = value;
    }
    public String getValue() {
      return this.value;
    }
  }

  public enum ESITO_PAGAMENTO {
    OK("OK"), KO("ERROR");
    String value;
    ESITO_PAGAMENTO(String value) {
      this.value = value;
    }
    public String getValue() {
      return this.value;
    }
  }

  public static final String DEFAULT_VERSIONE_TRACCIATO = "1.0";

  //LOGO ENTE
  public static final int MAX_WIDTH_LOGO_ENTE = 250;
  public static final int MAX_HEIGHT_LOGO_ENTE = 250;

  // DefinitionDovutoService
  public static final String BILANCIO_DEFAULT_TOTALE = "TOTALE";
  public static final String BILANCIO_DEFAULT_ESTRAI_IMPORTO = "estrai_importo";
  public static final String BILANCIO_DEFAULT_CALCOLA_IMPORTO = "calcola_importo";
  public static final String BILANCIO_DEFAULT_PLACEHOLDER_CAPITOLO = "PLACEHOLDER_CAPITOLO";

  public enum FLG_TIPO_FLUSSO {
    QUADRATURA("Q"), RENDICONTAZIONE("R");

    String value;

    FLG_TIPO_FLUSSO(String value) {
      this.value = value;
    }
    public static FLG_TIPO_FLUSSO fromString(String tipoFlusso) {
      List<FLG_TIPO_FLUSSO> tipi = Arrays.asList(values()).stream().filter(e -> e.value.equals(tipoFlusso)).collect(Collectors.toList());
      if (tipi != null && tipi.size() == 1)
        return tipi.get(0);
      throw new FespValidatorException("tipo flusso non supportato: " + tipoFlusso);
    }

    public String getValue() {
      return this.value;
    }
  }

  public enum TIPO_PAGAMENTO {
    INT_X_0(1), INT_X_1(2);

    Integer value;

    TIPO_PAGAMENTO(Integer value) {
      this.value = value;
    }

    public static TIPO_PAGAMENTO forInt(Integer value) {
      List<TIPO_PAGAMENTO> tipi = Arrays.asList(values()).stream().filter(e -> e.value.equals(value)).collect(Collectors.toList());
      if (tipi != null && tipi.size() == 1)
        return tipi.get(0);
      throw new ValidatorException("Invalido Tipo Pagamento: " + value);
    }
  }

  public final static String EMPTY = "";

  /*
    Spontaneo service.
   */
  public static final String XSD_TOTAL_INCLUDED = "total_included";
  public static final String XSD_NAME_VOCI_BILANCIO = "voci_bilancio";
  public static final String XSD_EXTRAATTR_CAPITOLI_MAP = "template_capitoli_map";

  /*
    Enum for DefinitionDovuto.
   */
  public enum TIPO_FUNZIONE {
    VALID, ENABLED, CAUSALE, VALUE, CAUSALE_VISUALIZZATA
  }

  //Nazione versante
  public final static String CODICE_NAZIONE_ITALIA = "IT";

  //STATI ENTE
  public final static String STATO_ENTE_INSERITO = "INSERITO";
  public final static String STATO_ENTE_PRE_ESERCIZIO = "PRE-ESERCIZIO";
  public final static String STATO_ENTE_ESERCIZIO = "ESERCIZIO";

  //Tipi Pagamenti
  public final static String ALL_PAGAMENTI = "ALL";
  public final static String PAY_BONIFICO_BANCARIO_TESORERIA = "BBT";
  public final static String PAY_BONIFICO_POSTALE = "BP";
  public final static String PAY_ADDEBITO_DIRETTO = "AD";
  public final static String PAY_CARTA_PAGAMENTO = "CP";
  public final static String PAY_PRESSO_PSP = "PO";
  public final static String PAY_MYBANK = "OBEP";

  //identificativoUnivocoVersamento
  public final static String CODICE_FISCALE_ANONIMO = "ANONIMO";
  public final static String TIPOIDENTIFICATIVOUNIVOCO_F = "F";
  public final static String TIPOIDENTIFICATIVOUNIVOCO_G = "G";
  public final static char[] TIPOIDENTIFICATIVOUNIVOCO_VALID_VALUES = new char[]{'F','G'};

  public final static String  DATI_SPECIFICI_RISCOSSIONE_UNKNOW = "9/---";

  public final static String COD_MARCATURA_REND_9 = "-9-";

  public final static String CODICE_CONTESTO_PAGAMENTO_NA = "n/a";
  public final static String PAGATO_CON_RENDICONTAZIONE_9 = "PAGATO CON RENDICONTAZIONE 9";
  public final static String CODICE_AUTENTICAZIONE_SOGGETTO_NA = "N/A";

  //TIPI CARRELLO
  public final static String TIPO_CARRELLO_DEFAULT = "DEFAULT";
  public final static String TIPO_CARRELLO_DEFAULT_CITTADINO = "DEFAULT_CITTADINO";
  public final static String TIPO_CARRELLO_CITTADINO_ANONYMOUS = "";
  public final static String TIPO_CARRELLO_AVVISO_ANONIMO = "AVVISO_ANONIMO";
  public final static String TIPO_CARRELLO_AVVISO_ANONIMO_ENTE = "AVVISO_ANONIMO_ENTE";
  public final static String TIPO_CARRELLO_ESTERNO_ANONIMO = "ESTERNO_ANONIMO";
  public final static String TIPO_CARRELLO_SPONTANEO_ANONIMO = "SPONTANEO_ANONIMO";
  public final static String TIPO_CARRELLO_PAGAMENTO_ATTIVATO_PRESSO_PSP = "ATTIVATO_PRESSO_PSP";
  public final static String TIPO_CARRELLO_ESTERNO_ANONIMO_MULTIENTE ="ESTERNO_ANONIMO_MULTIENTE";
  public final static String TIPO_CARRELLO_PRECARICATO_ANONIMO_ENTE ="PRECARICATO_ANONIMO_ENTE";

  //IUV
  public final static String IUV_GENERATOR_25 = "IUV25";
  public final static String IUV_GENERATOR_17 = "IUV17";
  public static final int IUV_GENERATOR_15_LENGTH = 15;
  public static final int IUV_GENERATOR_17_LENGTH = 17;

  //Tipo Anagrafica Stato
  public final static String STATO_TIPO_ENTE = "ente";
  public final static String STATO_TIPO_FLUSSO = "flusso";
  public final static String STATO_TIPO_DOVUTO = "dovuto";
  public final static String STATO_TIPO_CARRELLO = "carrel";
  public final static String STATO_TIPO_MULTI_CARRELLO  = "multic";
  public final static String STATO_TIPO_EXPORT = "export";
  public final static String STATO_TIPO_IMPORT = "import";
  //public final static String STATO_TIPO_EVTIPO = "evTipo";
  //public final static String STATO_TIPO_EVCATE = "evCate";
  //public final static String STATO_TIPO_EVESIT = "evEsit";
  public final static String STATO_TIPO_REVOCA = "revoca";

  //Anagrafica stato
  public final static String STATO_DOVUTO_DA_PAGARE = "INSERIMENTO_DOVUTO";
  public final static String STATO_DOVUTO_PAGAMENTO_INIZIATO = "PAGAMENTO_INIZIATO";
  public final static String STATO_DOVUTO_PREDISPOSTO = "PREDISPOSTO";
  public final static String STATO_DOVUTO_COMPLETATO = "COMPLETATO";
  public final static String STATO_DOVUTO_ANNULLATO = "ANNULLATO";
  public final static String STATO_DOVUTO_ERRORE = "ERROR_DOVUTO";
  public final static String STATO_DOVUTO_ABORT = "ABORT";
  public final static String STATO_DOVUTO_SCADUTO = "SCADUTO";
  public final static String STATO_DOVUTO_SCADUTO_ELABORATO = "SCADUTO_ELABORATO";

  public final static String STATO_DOVUTO_DISABILITATO = "DISABILITATO";

  public final static String STATO_DOVUTO_ELABORATO_REVOCATO = "REVOCATO";
  public final static String STATO_DOVUTO_ELABORATO_ANNULLATO_PSP = "ANNULLATO_PSP";


  public final static String STATO_FLUSSO_CARICATO = "CARICATO";
  public final static String STATO_FLUSSO_ANNULLATO = "ANNULLATO";
  public final static String STATO_FLUSSO_IN_CARICAMENTO = "LOAD_FLOW";
  public final static String STATO_FLUSSO_ATTESA_CARICAMENTO = "ATTESA_CARICAMENTO";
  public final static String STATO_FLUSSO_ERRORE_CARICAMENTO = "ERRORE_CARICAMENTO";

  public final static String STATO_CARRELLO_PREDISPOSTO = "PREDISPOSTO";
  public final static String STATO_CARRELLO_NUOVO = "NUOVO_CARRELLO";
  public final static String STATO_CARRELLO_PAGATO = "PAGATO";
  public final static String STATO_CARRELLO_NON_PAGATO = "NON_PAGATO";
  public final static String STATO_CARRELLO_PARZIALMENTE_PAGATO = "PARZ_PAGATO";
  public final static String STATO_CARRELLO_PAGAMENTO_IN_CORSO = "PAGAM_IN_CORSO";
  public final static String STATO_CARRELLO_DECORRENZA_TERMINI = "DECORR_TERM";
  public final static String STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE = "DECORR_TERM_PARZ";
  public final static String STATO_CARRELLO_ABORT = "ABORT";
  public final static String STATO_CARRELLO_IMPOSSIBILE_INVIARE_RP = "IMPOSSIBILE_INVIARE_CARRELLO_RP";
  public final static String STATO_CARRELLO_SCADUTO = "SCADUTO";
  public final static String STATO_CARRELLO_SCADUTO_ELABORATO = "SCADUTO_ELABORATO";

  public final static String STATO_EXPORT_LOAD = "LOAD_EXPORT";
  public final static String STATO_EXPORT_IN_ELAB = "EXPORT_IN_ELAB";
  public final static String STATO_EXPORT_ERROR = "ERROR_EXPORT";
  public final static String STATO_EXPORT_ESEGUITO = "EXPORT_ESEGUITO";
  public static final String STATO_EXPORT_ESEGUITO_NESSUN_DOVUTO_TROVATO = "EXPORT_ESEGUITO_NESSUN_DOVUTO_TROVATO";

  public final static String STATO_IMPORT_LOAD = "LOAD_IMPORT";
  public final static String STATO_IMPORT_ESEGUITO = "IMPORT_ESEGUITO";

  public final static String COMPONENTE_PA = "PA";
  public final static String COMPONENTE_FESP = "FESP";
  public static final String COMPONENTE_WFESP = "WFESP";

  public static final String NODO_DEI_PAGAMENTI_SPC = "NodoDeiPagamentiSPC";
  public static final String SIL = "SIL";
  public static final String NODO_REGIONALE_FESP = "NodoRegionaleFESP";

  public static final String STATO_AVVISO_DIGITALE_TIPO_STATO = "ad";
  public static final String STATO_AVVISO_DIGITALE_NUOVO = "NUOVO";
  public static final String STATO_AVVISO_DIGITALE_INSERITO_FLUSSO = "INSERITO_FLUSSO";
  public static final String STATO_AVVISO_DIGITALE_INVIATO = "INVIATO";
  public static final String STATO_AVVISO_DIGITALE_CONSEGNATO = "CONSEGNATO";
  public static final String STATO_AVVISO_DIGITALE_ERRORE_CONSEGNA = "ERRORE_CONSEGNA";
  public static final String STATO_AVVISO_DIGITALE_RICEVUTO_ESITO = "RICEVUTO_ESITO";
  public static final String STATO_AVVISO_DIGITALE_ANNULLATO = "ANNULLATO";
  public static final String STATO_AVVISO_DIGITALE_PAGATO = "PAGATO";

  public static final String TIPO_GENERATORE_AVVISO_DIGITALE = "cod_avviso_digitale";

  /**
   * FLUSSO - STATI per l'inoltro dell'avviso di pagamento in formato digitale.
   */
  /* Stringa di testo aggiunta come suffisso all'id del flusso nella tabella "mygov_flusso_avviso_digitale" colonna "cod_fad_id_flusso". */
  public static final String FLUSSO_AVVISO_DIGITALE_WS_SUFFISSO_ID = "_AVVISATURA-DIGITALE-WS";
  /* Stringa di testo che specializza il tipo del flusso della tabella "mygov_flusso_avviso_digitale" colonna "cod_fad_tipo_flusso". */
  public static final String FLUSSO_AVVISO_DIGITALE_WS_TIPO = "AD_WS";

  /* Codice che individua la tipologia di stato dello stato da assegnare al flusso nella tabella "mygov_flusso_avviso_digitale". */
  public static final String FLUSSO_AVVISO_DIGITALE_WS_TIPO_STATO = "fadWS";
  /* Codice dello stato assegnato al flusso dell'ente quando l'inserisce nel Database nella tabella "mygov_flusso_avviso_digitale". */
  public final static String FLUSSO_AVVISO_DIGITALE_WS_STATO_DEFAULT = "AVVISATURA_DIGITALE_WS";

  //* Codice che individua la tipologia di stato dello stato da assegnare all'avviso nella tabella "mygov_avviso_digitale". */
  public static final String AVVISO_DIGITALE_WS_TIPO_STATO = "adWS";
  /* Codice della stato assegnato all'avviso quando è preso incarico perchè venga inoltrato l'avviso di pagamento in formato digitale. */
  public static final String AVVISO_DIGITALE_WS_STATO_NUOVO = "NUOVO";
  /* Codice della stato assegnato all'avviso quando si verificano errori nella chiamata al WS o di esecuzione nel metodo service. */
  public static final String AVVISO_DIGITALE_WS_STATO_ERRORE_INVIO = "ERRORE_INVIO";
  /* Codice della stato assegnato all'avviso quando un ente notifica l'annullamento del dovuto. */
  public static final String AVVISO_DIGITALE_WS_STATO_ANNULLATO = "ANNULLATO";
  /* Codice della stato assegnato all'avviso quando la chiamata al WS per l'avvisatura digitale è andata a buon fine ricevendone l'esito. */
  public static final String AVVISO_DIGITALE_WS_STATO_RICEVUTO_ESITO = "RICEVUTO_ESITO";
  /** ============================================================================= **/

  public static final String STATO_REVOCA_TIPO_STATO = "revoca";
  public static final String STATO_REVOCA_NUOVO = "NUOVO";
  public static final String STATO_REVOCA_APPROVATA = "APPROVATA";
  public static final String STATO_REVOCA_RESPINTA = "RESPINTA";
  public static final String STATO_REVOCA_TUTTI = "tutti";

  // Codice Esito Pagamento
  public static final String CODICE_ESITO_PAGAMENTO_ESEGUITO = "0";
  public static final String CODICE_ESITO_PAGAMENTO_NON_ESEGUITO = "1";
  public static final String CODICE_ESITO_PAGAMENTO_PARZIALMENTE_ESEGUITO = "2";
  public static final String CODICE_ESITO_PAGAMENTO_DECORRENZA_TERMINI = "3";
  public static final String CODICE_ESITO_PAGAMENTO_DECORRENZA_TERMINI_PARZIALE = "4";

  public static final char CODICE_ESITO_PAGAMENTO_OK = '0';
  public static final char CODICE_ESITO_PAGAMENTO_KO = '1';

  /**
   * FLUSSO - STATI per la notifica dell'avviso verso piattaforma IO.
   */
  /* Stringa di testo aggiunta come suffisso all'id del flusso nella tabella "mygov_flusso_avviso_digitale" colonna "cod_fad_id_flusso". */
  public static final String FLUSSO_NOTIFICA_AVVISO_IO_SUFFISSO_ID = "_NOTIFICA-AVVISO-IO";
  public static final String FLUSSO_NOTIFICA_AVVISO_IO_PAGVERS_SUFFISSO_ID = "_NOTIFICA-AVVISO-IO-PAGVERS";
  /* Stringa di testo che specializza il tipo del flusso della tabella "mygov_flusso_avviso_digitale" colonna "cod_fad_tipo_flusso". */
  public static final String FLUSSO_NOTIFICA_AVVISO_IO_TIPO = "NA_IO";
  public static final String FLUSSO_NOTIFICA_AVVISO_IO_PAGVERS_TIPO = "NA_IO_PAGVERS";

  /* Codice che individua la tipologia di stato dello stato da assegnare al flusso fittizio nella tabella "mygov_flusso_avviso_digitale". */
  public static final String FLUSSO_NOTIFICA_AVVISO_IO_TIPO_STATO = "fnaIO";
  public static final String FLUSSO_NOTIFICA_AVVISO_IO_PAGVERS_TIPO_STATO = "fnaIOP";
  /* Codice dello stato assegnato al flusso fittizio dell'ente quando l'inserisce nel Database nella tabella "mygov_flusso_avviso_digitale". */
  public final static String FLUSSO_NOTIFICA_AVVISO_IO_STATO_DEFAULT = "NOTIFICA_AVVISO_IO";
  public final static String FLUSSO_NOTIFICA_AVVISO_IO_STATO_PAGVERS = "NOTIFICA_AVVISO_IO_PAGVERS";

  /* Codice che individua la tipologia di stato dello stato da assegnare alla notifica di avviso nella tabella "mygov_avviso_digitale". */
  public static final String NOTIFICA_AVVISO_IO_TIPO_STATO = "naIO";
  public static final String NOTIFICA_AVVISO_IO_PAGVERS_TIPO_STATO = "naIOPV";
  /* Codice dello stato assegnato all'avviso quando è preso in carico perchè venga notificato. */
  public static final String NOTIFICA_AVVISO_IO_STATO_NUOVO = "NUOVO";
  /* Codice dello stato assegnato all'avviso quando si verificano errori nella chiamata ad IO. */
  public static final String NOTIFICA_AVVISO_IO_STATO_ERRORE = "ERRORE";
  /* Codice dello stato assegnato all'avviso quando un ente notifica l'annullamento del dovuto. */
  public static final String NOTIFICA_AVVISO_IO_STATO_ANNULLATO = "ANNULLATO";
  /* Codice dello stato assegnato all'avviso quando la chiamata ad IO è andata a buon fine */
  public static final String NOTIFICA_AVVISO_IO_STATO_NOTIFICATO = "NOTIFICATO";
  /** ============================================================================= **/


  public static final String SMALL_IUV_AUX_DIGIT = "3";
  public static final String OLD_IUV_AUX_DIGIT = "0";

  /** ====================== For JaperUtils =================== **/
  public final static String AVVISO_PAGAMENTO_AUX_DIGIT = "0";
  public final static String AVVISO_PAGAMENTO_CODICE_IDENTIFICATIVO="PAGOPA";
  public final static String AVVISO_PAGAMENTO_VERSIONE="002";
  public final static String AVVISO_PAGAMENTO_SEPARATORE="|";
  public final static int NUMERO_MAX_AVVISI_PAGAMENTO_PSP = 1;
  public final static String TIPO_DOC_PRESSO_PSP = "896";
  public final static String LUNGHEZZA_CODICE_AVVISO = "18";
  public final static String LUNGHEZZA_CONTO = "12";
  public final static String LUNGHEZZA_IMPORTO = "10";
  public final static String LUNGHEZZA_TIPO_DOCUMENTO = "3";
  public final static String ID_DATAMATRIX = "1";
  public final static String FASE_PAGAMENTO = "P1";
  public final static String VALORE_FINALE_DATAMATRIX = "A";
  /** =========================================================== **/

  public final static String TAXONOMIC_CODE_PATTERN = "9\\/\\d{2}\\d{2}\\d{3}\\w{2}\\/\\S{0,129}";
  public final static String EMAIL_PATTERN = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"; //RFC 5322
  public final static String DATI_SPECIFICI_RISCOSSIONE_PATTERN = "[0129]{1}\\/\\S{3,138}";
  public final static String ANAGRAFICA_INDIRIZZO_PATTERN = "[a-z A-Z0-9.,()/'&]{1,70}";
  public final static String ANAGRAFICA_CIVICO_PATTERN = "[a-z A-Z0-9.,()/'&]{1,16}";
  public final static String NOT_ANAGRAFICA_INDIRIZZO_PATTERN = "[^a-z A-Z0-9.,()/'&]{1,70}";
  public final static String NOT_ANAGRAFICA_CIVICO_PATTERN = "[^a-z A-Z0-9.,()/'&]{1,16}";
  public final static String NOT_ANAGRAFICA_LOC_PROV_PATTERN = "[^a-z A-Z0-9.,()/'&]{1,35}";
  public static final String CAUSALE_PATTERN = "[^a-z A-Z0-9/\\-?:().,'+]{1,1024}";
  public static final String CAUSALE_TRUNCATE_PATTERN = "%1$.140s";
  public static final String REGEX_AUTORIZZAZIONE_POSTE = "^AUT.*DEL.*";
  public static final int LUNGHEZZA_IBAN = 27;
  public static final int LUNGHEZZA_MASSIMA_BIC = 11;

  public final static String FUNZIONALITA_PAGAMENTO_SPONTANEO = "PAGAMENTO_SPONTANEO";
  public static final String FUNZIONALITA_AVVISATURA_DIGITALE = "AVVISO_DIGITALE";
  public final static String FUNZIONALITA_NOTIFICA_AVVISI_IO = "NOTIFICA_AVVISI_IO";
  public final static String FUNZIONALITA_ENTE_PUBBLICO = "ENTE_PUBBLICO";
  public final static String FUNZIONALITA_INOLTRO_ESITO_PAGAMENTO_PUSH = "INOLTRO_ESITO_PAGAMENTO_PUSH";
  public final static String[] ENTI_ALL_FUNZIONALITA = new String[] {
      FUNZIONALITA_PAGAMENTO_SPONTANEO, FUNZIONALITA_AVVISATURA_DIGITALE, FUNZIONALITA_NOTIFICA_AVVISI_IO,
      FUNZIONALITA_ENTE_PUBBLICO, FUNZIONALITA_INOLTRO_ESITO_PAGAMENTO_PUSH };

  public final static BigDecimal MAX_AMOUNT = BigDecimal.valueOf(999999999.99);

  public static final String TIPO_DOVUTO_MARCA_BOLLO_DIGITALE = "MARCA_BOLLO_DIGITALE";

  public final static String IDENTIFICATIVO_PSP_POSTE = "BPPIITRRXXX";
  public final static int MAX_LENGHT_ANAGRAFICA_UTENTE_POSTE = 50;
  public final static int MAX_LENGHT_INDIRIZZO_PLUS_CIVICO_POSTE = 50;
  public final static int MAX_LENGHT_CAUSALE = 140;

  public static final String CAUSALE_DOVUTO_PAGATO = "PAGATO";
  public static final String MODELLO_PAGAMENTO_4 = "4";
  public static final String MODELLO_PAGAMENTO_1 = "1";

  public static final String COD_XSD_DEFAULT = "mypay_default";
  public static final String COD_TIPO_DOVUTO_DEFAULT = "DEFAULT";

  /** ====================== For FESP =================== **/
  public static final String IUV_SISTEMA_INFORMATIVO_ID = "00";
  public static final String CREDITOR_REFERENCE_PREFIX = "RF";
  public static final String IMPORTO_PADDING_ZEROES = "%06d";
  public static final String CREDITOR_REFERENCE_BBT_SUFFIX = "99";
  public final static String ACK_STRING = "OK";
  public final static String ACK_STRING_KO = "KO";
  public final static String NODO_REGIONALE_FESP_ESITO_OK = "OK";
  public final static String NODO_REGIONALE_FESP_ESITO_KO = "KO";
  public final static String MYPROFILE_TENANT_REGIONE_VENETO = "R_VENETO";
  public final static String FESP_ESITO_ERROR_NODO_SPC = "ERROR_NODO_SPC";
  // STATI DELL'RPT
  public final static String STATO_DA_INOLTRARE = "DA_INOLTRARE";
  public final static String STATO_INOLTRATA = "INOLTRATA";
  public final static String STATO_NON_INOLTRATA = "NON_INOLTRATA";
  //AUX DIGIT
  public static final String NEW_IUV_AUX_DIGIT = "3";
  //Formati date
  public final static DateFormat DDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");


  //Path relativi su FS
  public final static String PATH_RENDICONTAZIONE = "RENDICONTAZIONE";

  //Flussi rendicontazione/quadratura
  public final static String FILE_EXTENSION_FLUSSO = "xml";
  public final static String FILE_EXTENSION_TEMPORARY = "xml";

  // StatiEsecuzione in Mypay3
  public final static String STATO_ESECUZIONE_FLUSSO_CARICATO = "OK";
  public final static String STATO_ESECUZIONE_FLUSSO_ERRORE_CARICAMENTO = "KO";
  public final static String STATO_ESECUZIONE_FLUSSO_IN_CARICAMENTO = "IN_CARICAMENTO";

  public final static String STATO_ESITO_KO = "KO";
  public final static String STATO_ESITO_OK = "OK";

  public final static String STATO_EVTIPO_ERROR_NODO_FESP = "ERROR_NODO_FESP";
  public final static String STATO_EVTIPO_ERROR_NODO_SPC = "ERROR_NODO_SPC";

  public enum GIORNALE_TIPO_EVENTO_PA {
    nodoSILInviaCarrelloRP,
    nodoSILInviaRichiestaPagamento,
    nodoSILInviaRispostaRevoca,
    nodoSILInviaRP,
    paaSILAttivaRP,
    paaSILChiediNumeroAvviso,
    paaSILChiediPagati,
    paaSILChiediPagatiConRicevuta,
    paaSILImportaDovuto,
    paaSILInviaCarrelloDovuti,
    paaSILInviaDovuti,
    paaSILInviaEsito,
    paaSILInviaRichiestaRevoca,
    paaSILInviaRispostaPagamento,
    paaSILInviaRispostaPagamentoCarrel,
    paaSILRegistraPagamento,
    paaSILVerificaRP,
  }


  public enum GIORNALE_TIPO_EVENTO_FESP {
    nodoChiediCopiaRT,
    nodoChiediElencoFlussiRendicontazione,
    nodoChiediElencoQuadraturePA,
    nodoChiediFlussoRendicontazione,
    nodoChiediListaPendentiRPT,
    nodoChiediQuadraturaPA,
    nodoChiediSceltaWISP,
    nodoChiediStatoRPT,
    nodoInviaAvvisoDigitale,
    nodoInviaCarrelloRPT,
    nodoInviaRispostaRevoca,
    nodoInviaRPT,
    nodoSILChiediSceltaWISP,
    nodoSILInviaAvvisoDigitale,
    nodoSILInviaCarrelloRP,
    nodoSILRichiediRT,
    nodoSILInviaRichiestaPagamento,
    nodoSILInviaRispostaRevoca,
    nodoSILInviaRP,
    paaAttivaRPT,
    paaChiediNumeroAvviso,
    paaInviaRichiestaRevoca,
    paaInviaRispostaPagamento,
    paaInviaRT,
    paaSILAttivaRP,
    paaSILAttivaRPT,
    paaSILChiediNumeroAvviso,
    paaSILInviaEsito,
    paaSILVerificaRP,
    paaVerificaRPT,
  }

  public enum GIORNALE_SOTTOTIPO_EVENTO {RES, REQ}

  public enum GIORNALE_CATEGORIA_EVENTO { INTERFACCIA, INTERNO }

  public enum GIORNALE_ESITO_EVENTO { OK, KO }

  public enum COMPONENTE { PA, FESP, NODO_SPC, WFESP  }

  public enum TIPOIDENTIFICATIVOUNIVOCO { G, A, B, F, ANONIMO  }

  public enum TIPOREVOCA {
    S_0("0"), S_1("1"), S_2("2");

    String value;

    TIPOREVOCA(String value) {
      this.value = value;
    }

    public String getValue(){
      return this.value;
    }
  }
  public static final int GIORNALE_PARAMETRI_SPECIFICI_INTERFACCIA_MAX_LENGTH = 16384;
  /** =================================================== **/

  public static final String CODE_PAA_ENTE_NON_VALIDO = "PAA_ENTE_NON_VALIDO";
  public static final String CODE_PAA_DATE_FROM_NON_VALIDO = "PAA_DATE_FROM_NON_VALIDO";
  public static final String CODE_PAA_DATE_TO_NON_VALIDO = "PAA_DATE_TO_NON_VALIDO";
  public static final String CODE_PAA_INTERVALLO_DATE_NON_VALIDO = "PAA_INTERVALLO_DATE_NON_VALIDO";
  public static final String CODE_PAA_VERSIONE_TRACCIATO_NON_VALIDA = "PAA_VERSIONE_TRACCIATO_NON_VALIDA";
  public static final String CODE_PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO = "PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO";
  public static final String CODE_PAA_REQUEST_TOKEN_NON_VALIDO = "PAA_REQUEST_TOKEN_NON_VALIDO";

  public static final String CODE_PAA_ATTESA_ELABORAZIONE = "PAA_ATTESA_ELABORAZIONE";

  public static final String WS_USER = "WS_USER";
  public static final UserWithAdditionalInfo WS_USER_INFO = UserWithAdditionalInfo.builder().username(WS_USER).build();

  public static final String SYSTEM_USER = "SYSTEM_USER";
  public static final UserWithAdditionalInfo SYSTEM_USER_INFO = UserWithAdditionalInfo.builder().username(SYSTEM_USER).build();

  public enum TIPO_VERSAMENTO {
    BONIFICO_BANCARIO_TESORERIA("BBT"), BOLLETTINO_POSTALE("BP"), ADDEBITO_DIRETTO("AD"), CARTA_PAGAMENTO("CP"), PAGAMENTO_PRESSO_PSP(
        "PO"), ONLINE_BANKING_ELETTRONIC_PAYMENT("OBEP"), TUTTI("ALL");
    String value;

    TIPO_VERSAMENTO(String value) {
      this.value = value;
    }

    public String getValue() {
      return this.value;
    }
  }

  public enum TRIGGER_PAYMENT { ON_LINE, FROM_PSP }

}
