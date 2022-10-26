package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author regione del veneto
 *
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface IuvGeneratorService {

	/**
	 * @param identificativoDominio
	 * @param tipoVersamento
	 * @param tipoGeneratore
	 * @param importo
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public String generateIuv(final String identificativoDominio, final String tipoVersamento,
			final String tipoGeneratore, final String importo, final String auxDigit);
}
