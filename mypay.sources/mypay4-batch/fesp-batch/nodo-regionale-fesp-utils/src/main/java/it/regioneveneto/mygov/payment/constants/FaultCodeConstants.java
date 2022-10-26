package it.regioneveneto.mygov.payment.constants;

/**
 * @author regione del veneto
 *
 */
public interface FaultCodeConstants {

	/**
	 * ERRORE GENERICO
	 */
	public static final String PAA_SYSTEM_ERROR = "PAA_SYSTEM_ERROR";

	public static final String PAA_NOT_INVOKED = "PAA_NOT_INVOKED";

	public static final String PPT_ESITO_SCONOSCIUTO = "PPT_ESITO_SCONOSCIUTO";

	/**
	 * Errore generico per PAA_INVIA_RT
	 */
	public static final String PAA_RPT_SCONOSCIUTA = "PAA_RPT_SCONOSCIUTA";

	public static final String PAA_RT_DUPLICATA = "PAA_RT_DUPLICATA";

	public static final String PAA_RT_NON_VALIDA = "PAA_RT_NON_VALIDA";
	
	public static final String PAA_RT_SCONOSCIUTA = "PAA_RT_SCONOSCIUTA";

	/**
	 * Errore generico per PAA_ATTIVA_RPT
	 */
	public static final String PAA_ATTIVA_RPT_INTERMEDIARIO_SCONOSCIUTO = "INTERMEDIARIO_SCONOSCIUTO";

//	public static final String PAA_ATTIVA_RPT_PSP_SCONOSCIUTO = "PSP_SCONOSCIUTO";

	public static final String PAA_ATTIVA_RPT_DUPLICATA = "ATTIVA_RPT_DUPLICATA";

	/**
	 * Errore generico per PAA_VERIFICA_RPT
	 */
	public static final String PAA_VERIFICA_RPT_INTERMEDIARIO_SCONOSCIUTO = "INTERMEDIARIO_SCONOSCIUTO";

//	public static final String PAA_VERIFICA_RPT_PSP_SCONOSCIUTO = "PSP_SCONOSCIUTO";

	/**
	 * ERRORI PER VERIFICA FIRMA
	 */
	public static final String PAA_TIPOFIRMA_SCONOSCIUTO = "PAA_TIPOFIRMA_SCONOSCIUTO";

	public static final String PAA_ERRORE_FORMATO_BUSTA_FIRMATA = "PAA_ERRORE_FORMATO_BUSTA_FIRMATA";

	public static final String PAA_FIRMA_ERRATA = "PAA_FIRMA_ERRATA";

	/**
	 *  ERRORI WISP
	 */
	public static final String PAA_CHIEDI_SCELTA_WISP = "PAA_CHIEDI_SCELTA_WISP";
	public static final String PAA_PSP_NON_TROVATO = "PAA_PSP_NON_TROVATO";

	/**
	 *  Errori chiedi copia esito
	 */
	public static final String PAA_CHIEDI_COPIA_ESITO_ENTE_NON_PRESENTE = "PAA_CHIEDI_COPIA_ESITO_ENTE_NON_PRESENTE";

	public static final String PAA_CHIEDI_COPIA_ESITO_ERRORE = "PAA_CHIEDI_COPIA_ESITO_ERRORE";

	public static final String PAA_RPT_NON_PRESENTE = "PAA_RPT_NON_PRESENTE";
	
	public static final String PAA_RT_NON_PRESENTE = "PAA_RT_NON_PRESENTE";
	
	/**
	 *  ERRORI PER AVVISI DIGITALI
	 */
	public static final String PAA_NODO_INVIA_AVVISO_DIGITALE = "PAA_NODO_INVIA_AVVISO_DIGITALE";
	public static final String PAA_NODO_SIL_INVIA_AVVISO_DIGITALE = "PAA_NODO_SIL_INVIA_AVVISO_DIGITALE";

	
	/**
	 * ERRORI paaChiediNumeroAvviso
	 */
	public static final String PAA_ID_INTERMEDIARIO_ERRATO = "PAA_ID_INTERMEDIARIO_ERRATO";
	public static final String PAA_STAZIONE_INT_ERRATA = "PAA_STAZIONE_INT_ERRATA";
	public static final String PAA_ID_DOMINIO_ERRATO = "PAA_ID_DOMINIO_ERRATO";
	
	/**
	 * ERRORI XML
	 */
	public static final String PAA_SINTASSI_EXTRAXSD = "PAA_SINTASSI_EXTRAXSD";
	public static final String PAA_SINTASSI_XSD = "PAA_SINTASSI_XSD";
	public static final String PAA_SEMANTICA = "PAA_SEMANTICA";
	
	/**
	 * ERRORI paaInviaRichiestaRevoca
	 */
	public static final String PAA_ER_DUPLICATA = "PAA_ER_DUPLICATA";
}
