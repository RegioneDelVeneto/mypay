package it.regioneveneto.mygov.payment.nodoregionalefesp.iuvgenerators;

import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.IuvGenerationException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author regione del veneto
 *
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface IuvGenerator {

	/**
	 * 
	 */
	public static final String FULL = "IUV25";

	/**
	 * 
	 */
	public static final String SMALL = "IUV17";

	/**
	 * @param codiceIpaEnte
	 * @param tipoVersamento
	 * @param importo
	 * @return
	 * @throws IuvGenerationException
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	String generateIuv(String codiceIpaEnte, String tipoVersamento, String importo, String codiceSegregazione,String auxDigit) throws IuvGenerationException;
}
