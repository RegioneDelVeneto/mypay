package it.regioneveneto.mygov.payment.nodoregionalefesp.factory;

import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.IuvGenerationException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.IuvGeneratorNotFound;
import it.regioneveneto.mygov.payment.nodoregionalefesp.iuvgenerators.IuvGenerator;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author regione del veneto
 *
 */
public class IuvGeneratorFactoryImpl implements IuvGeneratorFactory {

	private static final Log log = LogFactory.getLog(IuvGeneratorFactoryImpl.class);

	/**
	 * Mappa tipo versamento/generatore di iuv 
	 */
	private Map<String, IuvGenerator> generatorsMap;

	/**
	 * Costruttore senza argomenti
	 */
	public IuvGeneratorFactoryImpl() {
		super();
	}

	/**
	 * @param generatorsMap the generatorsMap to set
	 */
	public void setGeneratorsMap(Map<String, IuvGenerator> generatorsMap) {
		this.generatorsMap = generatorsMap;
	}

	@Override
	public IuvGenerator getIuvGenerator(final String tipoGeneratore) throws IuvGeneratorNotFound {

		IuvGenerator iuvGenerator = this.generatorsMap.get(tipoGeneratore);
		if (iuvGenerator == null) {
			log.error("Nessuna istanza trovata per il generatore di iuv per il tipo Generatore [" + tipoGeneratore
					+ "]");
			throw new IuvGenerationException(
					"Nessuna istanza trovata per il generatore di iuv per il tipo Generatore [" + tipoGeneratore + "]");
		}

		return iuvGenerator;
	}
}
