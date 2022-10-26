/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.FirmaNotValidException;

/**
 * @author regione del veneto
 * @author regione del veneto
 *
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface FirmaService {
	/**
	 * @param signed
	 * @param trackingInfo
	 * @return
	 * @throws FirmaNotValidException
	 */
	byte[] verify(byte[] signed, String trackingInfo) throws FirmaNotValidException;
}
