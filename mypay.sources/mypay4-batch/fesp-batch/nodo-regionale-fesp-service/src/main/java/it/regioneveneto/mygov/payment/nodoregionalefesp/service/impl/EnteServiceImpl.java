package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.EnteDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.EnteDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.EnteService;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author regione del veneto
 *
 */
public class EnteServiceImpl implements EnteService {

	private static final Log log = LogFactory.getLog(EnteServiceImpl.class);

	private EnteDao enteDao;

	public EnteServiceImpl() {
		super();
	}

	/**
	 * @param enteDao the enteDao to set
	 */
	public void setEnteDao(EnteDao enteDao) {
		this.enteDao = enteDao;
	}

	@Override
	public MygovEnte getByCodiceFiscale(final String codiceFiscaleEnte) {

		log.debug("Invocato getByCodicefiscale con: cfEnte = [" + codiceFiscaleEnte + "] ");

		return enteDao.getByCodiceFiscale(codiceFiscaleEnte);
	}

	@Override
	public MygovEnte getByCodiceIpa(final String codiceIpa) {

		log.debug("Invocato getByCodiceIpa con: codiceIpa = [" + codiceIpa + "] ");

		return enteDao.getByCodiceIpa(codiceIpa);
	}

	public List<MygovEnte> findAll() {
		log.debug("Invocato metodo findAll()");
		return enteDao.findAll();
	}

	@Override
	public List<EnteDto> getAllEntiDto() {

		List<MygovEnte> enti = enteDao.findAll();
		List<EnteDto> enteDtos = mapEntitiesListToDtosList(enti);

		EnteDto allEnteDto = new EnteDto();
		allEnteDto.setCodIpa("tutti");
		allEnteDto.setCodFiscale("tutti");
		allEnteDto.setNomeEnte("tutti");
		enteDtos.add(0, allEnteDto);

		return enteDtos;
	}

	/**
	 * @param entities
	 * @return
	 */
	private List<EnteDto> mapEntitiesListToDtosList(List<MygovEnte> entities) {
		List<EnteDto> dtos = new ArrayList<EnteDto>();
		for (MygovEnte ente : entities) {
			EnteDto reportDto = mapEntityToDto(ente);
			if (reportDto != null)
				dtos.add(reportDto);
		}
		return dtos;
	}

	/**
	 * @param ente
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private EnteDto mapEntityToDto(MygovEnte ente) {
		EnteDto enteDto = new EnteDto();

		enteDto.setCodIpa(ente.getCodIpaEnte());
		enteDto.setCodFiscale(ente.getCodiceFiscaleEnte());
		enteDto.setNomeEnte(ente.getDeNomeEnte());

		return enteDto;
	}
}
