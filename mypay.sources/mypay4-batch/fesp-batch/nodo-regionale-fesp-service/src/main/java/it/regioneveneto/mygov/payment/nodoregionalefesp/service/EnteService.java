package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.EnteDto;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author regione del veneto
 * @author regione del veneto
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface EnteService {

	/**
	 * @param cfEnte
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovEnte getByCodiceFiscale(final String codiceFiscaleEnte);

	/**
	 * @param codiceIpa
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovEnte getByCodiceIpa(final String codiceIpa);

	/**
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	List<MygovEnte> findAll();
	
	
	List<EnteDto> getAllEntiDto();

}
