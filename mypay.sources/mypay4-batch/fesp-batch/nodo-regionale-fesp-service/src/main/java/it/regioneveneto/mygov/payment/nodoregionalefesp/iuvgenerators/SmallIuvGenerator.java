/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.iuvgenerators;

import it.regioneveneto.mygov.payment.constants.Constants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.IuvGenerationException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ProgressiviVersamentoService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author regione del veneto
 *
 */
public class SmallIuvGenerator implements IuvGenerator {

	private static final Log log = LogFactory.getLog(SmallIuvGenerator.class);

	private static final String PROGRESSIVO_PADDING_ZEROES = "%011d";

	private ProgressiviVersamentoService progressiviVersamentoService;

	/**
	 * Costruttore senza argomenti
	 */
	public SmallIuvGenerator() {
		super();
	}

	/**
	 * @param progressiviVersamentoService the progressiviVersamentoService to set
	 */
	public void setProgressiviVersamentoService(ProgressiviVersamentoService progressiviVersamentoService) {
		this.progressiviVersamentoService = progressiviVersamentoService;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.iuvgenerators.IuvGenerator#generateIuv()
	 */
	@Override
	public String generateIuv(final String codiceIpaEnte, final String tipoVersamento, final String importo, final String codiceSegregazione, final String auxDigit) {

		log.debug("Generazione dello iuv per codiceIpaEnte [" + codiceIpaEnte + "], tipo versamento [" + tipoVersamento + "], importo [" + importo + "]");

		//Composizione Creditor Reference
		StringBuffer creditorReference = new StringBuffer();

		/*
		 * #######################
		 * SPAZIO AD USO FUTURO
		 * #######################
		 */
		//		creditorReference.append("000");
		
		/*
		 * ##########################
		 * CODICE SEGREGAZIONE
		 * ##########################
		 */
		creditorReference.append(codiceSegregazione);
		
		/*
		 * ##########################
		 * SISTEMA INFORMATIVO LOCALE
		 * ##########################
		 */
		//Il sistema informativo id viene preso dal file di properties, ovvero dipende dall'installazione
		creditorReference.append(Constants.IUV_SISTEMA_INFORMATIVO_ID);

		//		/*
		//		 * ##########################
		//		 * TIPO VERSAMENTO
		//		 * ##########################
		//		 */
		//		//A partire dal tipo di versamento, viene recuperato il codice del tipo versamento censito per lo iuv
		//
		//		String iuvCodiceTipoVersamento = this.tipoVersamentoService.getIuvCodicePerTipoVersamento(tipoVersamento);
		//		if (StringUtils.isBlank(iuvCodiceTipoVersamento)) {
		//			log.error("Nessun tipo versamento trovato per il tipo versamento [" + tipoVersamento + "]");
		//			throw new IuvGenerationException("Nessun tipo versamento trovato per il tipo versamento [" + tipoVersamento
		//					+ "]");
		//		}
		//
		//		creditorReference.append(iuvCodiceTipoVersamento);

		/*
		 * ##########################
		 * PROGRESSIVO VERSAMENTO METTO SEMPRE FISSO TIPO A 'PO' PER EVITARE DUPLICATI VISTO CHE NON HO iuv_codice_tipo_versamento_id SU GEN_IUV_BREVE
		 * ##########################
		 */
		//Il progressivo viene generato appoggiandosi ad una tabella dei versamenti in base a Ente e TipoVersamento e sistema informativo
		long iuvCodiceProgressivoVersamento = this.progressiviVersamentoService.getNextProgressivoVersamento(codiceIpaEnte, IuvGenerator.SMALL,
				Constants.PAY_PRESSO_PSP);
		if (iuvCodiceProgressivoVersamento == 0) {
			log.error("Errore nella generazione del nuovo progressivo");
			throw new IuvGenerationException("Errore nella generazione del nuovo progressivo per codiceIpaEnte [" + codiceIpaEnte + "], tipo versamento ["
					+ tipoVersamento + "]");
		}

		creditorReference.append(String.format(PROGRESSIVO_PADDING_ZEROES, iuvCodiceProgressivoVersamento));

		/*
		 * Summary	0001380: Aggiornamento generatore IUV secondo specifiche Poste Italiane
		   Description	Aggiornare il generatore degli IUV in formato 15 cifre 
		   ("http://_HOST_ENTE_/dokuwiki/doku.php?id=mygov:documentazione_tecnica:formati_iuv") [^] 
		   da 
		   
		   "<00><Progressivo Versamento><00>" 
		   
		   a 
		   
		   "<00><Progressivo Versamento><CC>" 
		   
		   sostituendo le ultime due cifre fisse "00" con il resto "CC" della divisione intera per "93" del numero di 16 cifre 
		   composto dalla concatenazione di "001" con "<00><Progressivo Versamento>".
		 * 
		 */

		String digitString = auxDigit + creditorReference.toString();
		Long digit = Long.parseLong(digitString);
		Long divisore = new Long(93);
		Long resto = digit % divisore;
		String restoString = String.valueOf(resto);

		String CC = "";
		if (resto == 0) {
			CC = "00";
		} else if (resto < 10) {
			CC = "0" + restoString;
		} else {
			CC = restoString;
		}

		creditorReference.append(CC);

		/*
		 * ##########################
		 * SPAZIO AD USO FUTURO
		 * ##########################
		 */
		//creditorReference.append("00");

		return creditorReference.toString();
	}

}
