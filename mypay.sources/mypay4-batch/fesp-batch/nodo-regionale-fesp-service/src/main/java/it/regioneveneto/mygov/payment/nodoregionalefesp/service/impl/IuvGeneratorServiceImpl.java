package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.EnteDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.IuvDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.IuvGenerationException;
import it.regioneveneto.mygov.payment.nodoregionalefesp.factory.IuvGeneratorFactory;
import it.regioneveneto.mygov.payment.nodoregionalefesp.iuvgenerators.IuvGenerator;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.IuvGeneratorService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

/**
 * @author regione del veneto
 *
 */
public class IuvGeneratorServiceImpl implements IuvGeneratorService {

	private static final Log log = LogFactory.getLog(IuvGeneratorServiceImpl.class);

	private IuvGeneratorFactory iuvGeneratorFactory;
	private IuvDao iuvDao;
	private EnteDao enteDao;

	/**
	 * @param enteDao the enteDao to set
	 */
	public void setEnteDao(EnteDao enteDao) {
		this.enteDao = enteDao;
	}

	/**
	 * Costruttore senza argomenti
	 */
	public IuvGeneratorServiceImpl() {
		super();
	}

	/**
	 * @param iuvGeneratorFactory the iuvGeneratorFactory to set
	 */
	public void setIuvGeneratorFactory(IuvGeneratorFactory iuvGeneratorFactory) {
		this.iuvGeneratorFactory = iuvGeneratorFactory;
	}

	/**
	 * @param iuvDao the iuvDao to set
	 */
	public void setIuvDao(IuvDao iuvDao) {
		this.iuvDao = iuvDao;
	}

	/* (non-Javadoc)
	 * @see it.regioneveneto.mygov.payment.nodoregionalefesp.service.IuvGeneratorService#generateIuv(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String generateIuv(final String identificativoDominio, final String tipoVersamento,
			final String tipoGeneratore, final String importo,  final String auxDigit) throws IuvGenerationException {

		log.debug("Invocato generateIuv con: identificativoDominio = [" + identificativoDominio
				+ "] - TipoVersamento = [" + tipoVersamento + "] - TipoGeneratore = [" + tipoGeneratore
				+ "] - Importo = [" + importo + "]");

		log.debug("Chiamata a getIuvGenerator con: tipoGeneratore = [" + tipoGeneratore + "]");
		
		if(tipoGeneratore.equals(IuvGenerator.SMALL) && StringUtils.isBlank(auxDigit)) {
			log.error("Errore, il campo auxDigit è nullo per il generatore di IUV a 17");
			throw new IuvGenerationException("Errore, il campo auxDigit è nullo per il generatore di IUV a 17");
		}
		
		IuvGenerator iuvGenerator = this.iuvGeneratorFactory.getIuvGenerator(tipoGeneratore);

		//Recupero l'oggetto intermediario
		MygovEnte mygovEnte = null;
		try {
			log.debug("Recupero dell'ente con codiceFiscale (identificativoDominio = [" + identificativoDominio + "]");
			mygovEnte = this.enteDao.getByCodiceFiscale(identificativoDominio);
		} catch (DataAccessException e) {
			log.error("Errore nel recupero dell'ente a db. " + e.getMessage(), e);
			throw new IuvGenerationException("Errore nel recupero dell'ente a db. " + e.getMessage(), e);
		}

		String codiceIpaEnte = null;
		String iuv = null;

		if (mygovEnte != null) {

			codiceIpaEnte = mygovEnte.getCodIpaEnte();
			log.debug("codiceIpaEnte recuperato = [" + codiceIpaEnte + "]");
			String codiceSegregazione = mygovEnte.getCodCodiceSegregazione();
			try {
				iuv = iuvGenerator.generateIuv(codiceIpaEnte, tipoVersamento, importo, codiceSegregazione, auxDigit);
			} catch (IuvGenerationException iuve) {
				log.error(iuve.getMessage(), iuve);
				throw new IuvGenerationException(iuve.getMessage(), iuve);
			}
		} else {
			log.error("Nessun ente trovato con identificativoDominio = [" + identificativoDominio + "]");
			throw new IuvGenerationException("Nessun ente trovato con identificativoDominio = ["
					+ identificativoDominio + "]");
		}

		log.debug("Iuv generato = [" + iuv + "]");

		//Inserimento dello iuv su DB
		try {
			log.debug("Inserimento dello iuv in tabella...");
			iuvDao.insert(codiceIpaEnte, tipoVersamento, iuv);
		} catch (DataAccessException e) {
			log.error("Errore nell' inserimento dello IUV a db", e);
			throw new IuvGenerationException("Errore nell' inserimento dello IUV a db", e);
		}

		return iuv;
	}
}
