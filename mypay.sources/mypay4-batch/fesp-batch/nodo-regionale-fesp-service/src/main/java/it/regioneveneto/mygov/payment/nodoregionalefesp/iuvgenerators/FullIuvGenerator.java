/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.iuvgenerators;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.regioneveneto.mygov.payment.constants.Constants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.IuvGenerationException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ProgressiviVersamentoService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.TipoVersamentoService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.utils.IuvGeneratorUtils;

/**
 * @author regione del veneto
 *
 */
public class FullIuvGenerator implements IuvGenerator {

	private static final Log log = LogFactory.getLog(FullIuvGenerator.class);

	private static final String PROGRESSIVO_PADDING_ZEROES = "%013d";

	private TipoVersamentoService tipoVersamentoService;

	private ProgressiviVersamentoService progressiviVersamentoService;

	/**
	 * Costruttore senza argomenti
	 */
	public FullIuvGenerator() {
		super();
	}

	/**
	 * @param tipoVersamentoService the tipoVersamentoService to set
	 */
	public void setTipoVersamentoService(TipoVersamentoService tipoVersamentoService) {
		this.tipoVersamentoService = tipoVersamentoService;
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
	public String generateIuv(final String codiceIpaEnte, final String tipoVersamento, final String importo, final String codiceSegregazione, final String auxDigit)
			throws IuvGenerationException {

		log.debug("Generazione dello iuv per codiceIpaEnte [" + codiceIpaEnte + "], tipo versamento [" + tipoVersamento
				+ "], importo [" + importo + "], codice segregazione [" + codiceIpaEnte + "]");

		//Composizione Creditor Reference
		StringBuffer creditorReference = new StringBuffer();
		
		/*
		 * ##########################
		 * CODICE SEGREGAZIONE
		 * ##########################
		 */
		creditorReference.append(codiceSegregazione);
		
		/*
		 * ##########################
		 * TIPO VERSAMENTO
		 * ##########################
		 */
		//A partire dal tipo di versamento, viene recuperato il codice del tipo versamento censito per lo iuv

		String iuvCodiceTipoVersamento = this.tipoVersamentoService.getIuvCodicePerTipoVersamento(tipoVersamento);
		if (StringUtils.isBlank(iuvCodiceTipoVersamento)) {
			log.error("Nessun tipo versamento trovato per il tipo versamento [" + tipoVersamento + "]");
			throw new IuvGenerationException("Nessun tipo versamento trovato per il tipo versamento [" + tipoVersamento
					+ "]");
		}

		creditorReference.append(iuvCodiceTipoVersamento);

		/*
		 * ##########################
		 * PROGRESSIVO VERSAMENTO
		 * ##########################
		 */
		//Il progressivo viene generato appoggiandosi ad una tabella dei versamenti in base a Ente e TipoVersamento e sistema informativo
		long iuvCodiceProgressivoVersamento = this.progressiviVersamentoService.getNextProgressivoVersamento(
				codiceIpaEnte, IuvGenerator.FULL, tipoVersamento);
		if (iuvCodiceProgressivoVersamento == 0) {
			log.error("Errore nella generazione del nuovo progressivo");
			throw new IuvGenerationException("Errore nella generazione del nuovo progressivo per codiceIpaEnte ["
					+ codiceIpaEnte + "], tipo versamento [" + tipoVersamento + "]");
		}

		creditorReference.append(String.format(PROGRESSIVO_PADDING_ZEROES, iuvCodiceProgressivoVersamento));

		/*
		 * ##########################
		 * SISTEMA INFORMATIVO LOCALE
		 * ##########################
		 */
		//Il sistema informativo id viene preso dal file di properties, ovvero dipende dall'installazione
		creditorReference.append(Constants.IUV_SISTEMA_INFORMATIVO_ID);
		
		/*
		 * ##########################
		 * CIFRE FISSE
		 * ##########################
		 */
		creditorReference.append("00");
		
		/*
		 * ##########################
		 * Calcolo CheckDigits
		 * ##########################
		 */
		StringBuffer checkDigits = IuvGeneratorUtils.calculateCheckDigits(creditorReference);
		creditorReference.insert(0, checkDigits);
		creditorReference.insert(0, Constants.CREDITOR_REFERENCE_PREFIX);

		return creditorReference.toString();
	}
}
