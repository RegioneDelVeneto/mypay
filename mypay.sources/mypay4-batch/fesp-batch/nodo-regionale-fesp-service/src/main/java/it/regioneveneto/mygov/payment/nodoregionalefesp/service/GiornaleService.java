package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovGiornale;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.GiornaleDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author regione del veneto
 *
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface GiornaleService {

	Page<GiornaleDto> getGiornalePage(final String iuv, final String ente, final String te, final String ce, final String psp, final String esito,
			final Date from, final Date to, final int pageNumber, final int pageNumOfRecords, final String orderingField, final String sortingOrder);

	GiornaleDto getGiornaleDto(final long idGiornale);

	MygovGiornale getGiornale(final String iuv);

	List<String> getAllPsp();

	@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = false)
	void registraEvento(final Date dataOraEvento, final String identificativoDominio, final String identificativoUnivocoVersamento,
			final String codiceContestoPagamento, final String identificativoPrestatoreServiziPagamento, final String tipoVersamento, final String componente,
			final String categoriaEvento, final String tipoEvento, final String sottoTipoEvento, final String identificativoFruitore,
			final String identificativoErogatore, final String identificativoStazioneIntermediarioPa, final String canalePagamento,
			final String parametriSpecificiInterfaccia, final String esito);

}