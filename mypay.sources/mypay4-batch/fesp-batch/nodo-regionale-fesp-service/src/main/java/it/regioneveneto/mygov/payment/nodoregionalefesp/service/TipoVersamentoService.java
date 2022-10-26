package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovTipiversamento;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author regione del veneto
 *
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface TipoVersamentoService {

	/**
	 * @param id
	 * @return
	 */
	MygovTipiversamento getByKey(final long id);

	/**
	 * @param tipoVersamento
	 * @return
	 */
	MygovTipiversamento getByTipoVersamento(final String tipoVersamento);

	/**
	 * @return
	 */
	List<MygovTipiversamento> findAll();

	/**
	 * @param tipoVersamento
	 * @return
	 */
	String getIuvCodicePerTipoVersamento(final String tipoVersamento);

}
