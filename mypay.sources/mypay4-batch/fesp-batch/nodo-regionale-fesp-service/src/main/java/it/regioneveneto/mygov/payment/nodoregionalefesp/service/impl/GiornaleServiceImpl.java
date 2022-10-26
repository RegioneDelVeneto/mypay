/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import it.regioneveneto.mygov.payment.constants.Constants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.GiornaleDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovGiornale;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.GiornaleDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.GiornaleService;

/**
 * @author regione del veneto
 * 
 */
public class GiornaleServiceImpl implements GiornaleService {

	GiornaleDao giornaleDao;

	/**
	 * @param giornaleDao the giornaleDao to set
	 */
	public void setGiornaleDao(GiornaleDao giornaleDao) {
		this.giornaleDao = giornaleDao;
	}

	public GiornaleServiceImpl() {
		super();
	}

	@Override
	public Page<GiornaleDto> getGiornalePage(final String iuv, final String ente, final String te, final String ce, final String psp, final String esito,
			final Date from, final Date to, final int pageNumber, final int pageNumOfRecords, final String orderingField, final String sortingOrder) {

		Page<MygovGiornale> pageEntity = giornaleDao.getGiornalePage(iuv, ente, te, ce, psp, esito, from, to, pageNumber, pageNumOfRecords, orderingField,
				sortingOrder);

		Page<GiornaleDto> page = new Page<GiornaleDto>();
		page.setNextPage(pageEntity.isNextPage());
		page.setPage(pageEntity.getPage());
		page.setPageSize(pageEntity.getPageSize());
		page.setPreviousPage(pageEntity.isPreviousPage());
		page.setTotalPages(pageEntity.getTotalPages());
		page.setTotalRecords(pageEntity.getTotalRecords());
		page.setList(mapEntitiesListToDtosList(pageEntity.getList()));

		return page;
	}

	@Override
	public GiornaleDto getGiornaleDto(long idGiornale) {
		return mapEntityToDto(giornaleDao.getGiornale(idGiornale));
	}

	@Override
	public MygovGiornale getGiornale(String iuv) {
		return null;
	}

	/**
	 * @param entities
	 * @return
	 */
	private List<GiornaleDto> mapEntitiesListToDtosList(List<MygovGiornale> entities) {
		List<GiornaleDto> dtos = new ArrayList<GiornaleDto>();
		for (MygovGiornale giornale : entities) {
			GiornaleDto reportDto = mapEntityToDto(giornale);
			if (reportDto != null)
				dtos.add(reportDto);
		}
		return dtos;
	}

	/**
	 * @param giornale
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private GiornaleDto mapEntityToDto(MygovGiornale giornale) {
		GiornaleDto giornaleDto = new GiornaleDto();

		giornaleDto.setId(giornale.getMygovGiornaleId());
		giornaleDto.setIdentificativoDominio(giornale.getIdentificativoDominio());
		giornaleDto.setIdentificativoUnivocoVersamento(giornale.getIdentificativoUnivocoVersamento());

		giornaleDto.setTipoEvento(giornale.getTipoEvento());
		giornaleDto.setCategoriaEvento(giornale.getCategoriaEvento());

		giornaleDto.setIdentificativoPrestatoreServiziPagamento(giornale.getIdentificativoPrestatoreServiziPagamento());

		giornaleDto.setEsito(giornale.getEsito());

		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
		giornaleDto.setDataOraEvento(fmt.print(new DateTime(giornale.getDataOraEvento())));

		giornaleDto.setCodiceContestoPagamento(giornale.getCodiceContestoPagamento());
		giornaleDto.setTipoVersamento(giornale.getTipoVersamento());
		giornaleDto.setComponente(giornale.getComponente());
		giornaleDto.setSottoTipoEvento(giornale.getSottoTipoEvento());
		giornaleDto.setIdentificativoFruitore(giornale.getIdentificativoFruitore());
		giornaleDto.setIdentificativoErogatore(giornale.getIdentificativoErogatore());
		giornaleDto.setIdentificativoStazioneIntermediarioPa(giornale.getIdentificativoStazioneIntermediarioPa());
		giornaleDto.setCanalePagamento(giornale.getCanalePagamento());
		giornaleDto.setParametriSpecificiInterfaccia(giornale.getParametriSpecificiInterfaccia());

		return giornaleDto;
	}

	@Override
	public List<String> getAllPsp() {

		List<String> distinctPspList = giornaleDao.getAllPspDistinct();

		distinctPspList.removeAll(Arrays.asList("", null));

		distinctPspList.add(0, "tutti");

		return distinctPspList;
	}

	@Override
	public void registraEvento(Date dataOraEvento, String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento,
			String identificativoPrestatoreServiziPagamento, String tipoVersamento, String componente, String categoriaEvento, String tipoEvento,
			String sottoTipoEvento, String identificativoFruitore, String identificativoErogatore, String identificativoStazioneIntermediarioPa,
			String canalePagamento, String parametriSpecificiInterfaccia, String esito) {

		if (dataOraEvento == null) {
			dataOraEvento = new Date();
		}

		if (StringUtils.isBlank(identificativoDominio)) {
			identificativoDominio = "-";
		}

		if (StringUtils.isBlank(identificativoUnivocoVersamento)) {
			identificativoUnivocoVersamento = "-";
		}

		if (StringUtils.isBlank(codiceContestoPagamento)) {
			codiceContestoPagamento = "-";
		}

		if (StringUtils.isBlank(identificativoPrestatoreServiziPagamento)) {
			identificativoPrestatoreServiziPagamento = "-";
		}

		if (StringUtils.isBlank(componente)) {
			componente = "-";
		}

		if (StringUtils.isBlank(categoriaEvento)) {
			categoriaEvento = "-";
		}

		if (StringUtils.isBlank(tipoEvento)) {
			tipoEvento = "-";
		}

		if (StringUtils.isBlank(sottoTipoEvento)) {
			sottoTipoEvento = "-";
		}

		if (StringUtils.isBlank(identificativoFruitore)) {
			identificativoFruitore = "-";
		}

		if (StringUtils.isBlank(identificativoErogatore)) {
			identificativoErogatore = "-";
		}

		if (esito!= null && esito.contains("OK")) {
			esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
		}
		else {
			esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
		}

		giornaleDao.insertGiornale(dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
				identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
				identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esito);

	}

}
