/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpEDettaglio;

/**
 * @author regione del veneto
 *
 */
public interface RpEDettaglioDao {

	List<MygovRpEDettaglio> getByRpE(final MygovRpE mygovRpE) throws DataAccessException;

	MygovRpEDettaglio insert(final MygovRpE mygovRpE,
			final BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento,
			final BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa,
			final String codRpDatiVersDatiSingVersIbanAccredito, final String codRpDatiVersDatiSingVersBicAccredito,
			final String codRpDatiVersDatiSingVersIbanAppoggio, final String codRpDatiVersDatiSingVersBicAppoggio,
			final String codRpDatiVersDatiSingVersCredenzialiPagatore,
			final String deRpDatiVersDatiSingVersCausaleVersamento,
			final String deRpDatiVersDatiSingVersDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato,
			final String deEDatiPagDatiSingPagEsitoSingoloPagamento,
			final Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento,
			final String codEDatiPagDatiSingPagIdUnivocoRiscoss, final String deEDatiPagDatiSingPagCausaleVersamento,
			final String deEDatiPagDatiSingPagDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp, final String codEDatiPagDatiSingPagAllegatoRicevutaTipo,
			final byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest) throws DataAccessException;
	
	MygovRpEDettaglio insert(final MygovRpE mygovRpE,
			final BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento,
			final BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa,
			final String codRpDatiVersDatiSingVersIbanAccredito, final String codRpDatiVersDatiSingVersBicAccredito,
			final String codRpDatiVersDatiSingVersIbanAppoggio, final String codRpDatiVersDatiSingVersBicAppoggio,
			final String codRpDatiVersDatiSingVersCredenzialiPagatore,
			final String deRpDatiVersDatiSingVersCausaleVersamento,
			final String deRpDatiVersDatiSingVersDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato,
			final String deEDatiPagDatiSingPagEsitoSingoloPagamento,
			final Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento,
			final String codEDatiPagDatiSingPagIdUnivocoRiscoss, final String deEDatiPagDatiSingPagCausaleVersamento,
			final String deEDatiPagDatiSingPagDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp, final String codEDatiPagDatiSingPagAllegatoRicevutaTipo,
			final byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest,
			final String codRpDatiVersDatiSingVersDatiMbdTipoBollo,
			final String codRpDatiVersDatiSingVersDatiMbdHashDocumento,
			final String codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza) throws DataAccessException;

	MygovRpEDettaglio updateDateE(final Long mygovRpEDettaglioId, final int version, final MygovRpE mygovRpE,
			final BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato,
			final String deEDatiPagDatiSingPagEsitoSingoloPagamento,
			final Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento,
			final String codEDatiPagDatiSingPagIdUnivocoRiscoss, final String deEDatiPagDatiSingPagCausaleVersamento,
			final String deEDatiPagDatiSingPagDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp,
			final String codEDatiPagDatiSingPagAllegatoRicevutaTipo,
			final byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest) throws DataAccessException;

}
