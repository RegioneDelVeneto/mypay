package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author regione del veneto
 *
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface ProgressiviVersamentoService {

	/**
	 * @param codiceIpaEnte
	 * @param tipoGeneratore
	 * @param tipoVersamento
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	long getNextProgressivoVersamento(final String codiceIpaEnte, final String tipoGeneratore,
			final String tipoVersamento);
}
