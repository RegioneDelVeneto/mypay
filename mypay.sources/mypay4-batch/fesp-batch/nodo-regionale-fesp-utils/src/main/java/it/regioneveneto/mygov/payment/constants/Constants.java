package it.regioneveneto.mygov.payment.constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Constants {

	public static final String IUV_SISTEMA_INFORMATIVO_ID = "00";
	public static final String CREDITOR_REFERENCE_PREFIX = "RF";
	public static final String IMPORTO_PADDING_ZEROES = "%06d";
	public static final String CREDITOR_REFERENCE_BBT_SUFFIX = "99";
	public final static String ACK_STRING = "OK";
	public final static String ACK_STRING_KO = "KO";
	public final static String NODO_REGIONALE_FESP_ESITO_OK = "OK";
	public final static String NODO_REGIONALE_FESP_ESITO_KO = "KO";
	public final static String MYPROFILE_TENANT_REGIONE_VENETO = "_COD_IPA_ENTE_";

	// RUOLI
	public final static String MYPROFILE_ROLE_ADMIN = "ROLE_ADMIN";

	// STATI DELL'RPT
	public final static String STATO_DA_INOLTRARE = "DA_INOLTRARE";
	public final static String STATO_INOLTRATA = "INOLTRATA";
	public final static String STATO_NON_INOLTRATA = "NON_INOLTRATA";

	// Tipi Pagamenti
	public final static String ALL_PAGAMENTI = "ALL";
	public final static String PAY_BONIFICO_BANCARIO_TESORERIA = "BBT";
	public final static String PAY_BONIFICO_POSTALE = "BP";
	public final static String PAY_ADDEBITO_DIRETTO = "AD";
	public final static String PAY_CARTA_PAGAMENTO = "CP";
	public final static String PAY_PRESSO_PSP = "PO";
	public final static String PAY_MYBANK = "OBEP";
	
	//AUX DIGIT
	public static final String NEW_IUV_AUX_DIGIT = "3";
	public static final String OLD_IUV_AUX_DIGIT = "0";
	
	//Formati date
	public final static DateFormat DDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");

	public final static String IDENTIFICATIVO_PSP_POSTE = "BPPIITRRXXX";

	public enum GIORNALE_TIPO_EVENTO {
		ERRORE_NODO_SPC, 
		ERRORE_PA, 
		nodoSILInviaRP, 
		nodoInviaCarrelloRPT, 
		nodoSILInviaCarrelloRP, 
		nodoInviaRPT, 
		paaInviaRT, 
		paaSILInviaEsito, 
		paaVerificaRPT, 
		paaSILVerificaRP, 
		paaAttivaRPT, 
		paaSILAttivaRP, 
		nodoSILInviaRichiestaPagamento, 
		paaInviaRispostaPagamento, 
		nodoSILChiediSceltaWISP, 
		nodoChiediSceltaWISP, 
		paaChiediNumeroAvviso, 
		paaSILChiediNumeroAvviso,
		nodoSILInviaAvvisoDigitale,
		nodoInviaAvvisoDigitale,
		paaInviaRichiestaRevoca,
		nodoSILInviaRispostaRevoca, 
		nodoInviaRispostaRevoca
	}

	public enum GIORNALE_SOTTOTIPO_EVENTO {
		RESPONSE, REQUEST
	}

	public enum GIORNALE_CATEGORIA_EVENTO {
		INTERFACCIA, INTERNO
	}

	public enum GIORNALE_ESITO_EVENTO {
		OK, KO
	}

	public enum COMPONENTE {
		PA, FESP, NODO_SPC, WFESP
	}

	public static final String NODO_DEI_PAGAMENTI_SPC = "NodoDeiPagamentiSPC";
	
	public enum TIPOIDENTIFICATIVOUNIVOCO {
		G, A, B, F, ANONIMO
	}
	
	public final static String EMAIL_PATTERN = "^[A-Za-z0-9_]+([\\-\\+\\.'][A-Za-z0-9_]+)*@[A-Za-z0-9_]+([\\-\\.][A-Za-z0-9_]+)*\\.[A-Za-z0-9_]+([\\-\\.][A-Za-z0-9_]+)*$";
	
	public enum TIPOREVOCA {
		S_0("0"), S_1("1"), S_2("2");
		
		String value;

		private TIPOREVOCA(String value) {
			this.value = value;
		}
		
		public String getValue(){
			return this.value;
		}
		
	}
	
	public static final int GIORNALE_PARAMETRI_SPECIFICI_INTERFACCIA_MAX_LENGTH = 16384;
	
}
