package it.regioneveneto.mygov.payment.nodoregionalefesp.factory;

import it.regioneveneto.mygov.payment.nodoregionalefesp.iuvgenerators.IuvGenerator;

/**
 * @author regione del veneto
 *
 */
public interface IuvGeneratorFactory {

	/**
	 * @param tipoGeneratore
	 * @return
	 * @throws RuntimeException
	 */
	IuvGenerator getIuvGenerator(String tipoGeneratore) throws RuntimeException;
}
