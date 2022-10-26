/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRtDettaglio;

/**
 * @author regione del veneto
 *
 */
public interface RptRtDettaglioDao {

	List<MygovRptRtDettaglio> getByRptRt(MygovRptRt mygovRptRt) throws DataAccessException;

	MygovRptRtDettaglio insert(MygovRptRt mygov_rpt_rt, BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento,
			BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa, String deRptDatiVersDatiSingVersIbanAccredito,
			String deRptDatiVersDatiSingVersBicAccredito, String deRptDatiVersDatiSingVersIbanAppoggio, String deRptDatiVersDatiSingVersBicAppoggio,
			String codRptDatiVersDatiSingVersCredenzialiPagatore, String deRptDatiVersDatiSingVersCausaleVersamento,
			String deRptDatiVersDatiSingVersDatiSpecificiRiscossione, BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato,
			String deRtDatiPagDatiSingPagEsitoSingoloPagamento, Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento,
			String codRtDatiPagDatiSingPagIdUnivocoRiscossione, String deRtDatiPagDatiSingPagCausaleVersamento,
			String deRtDatiPagDatiSingPagDatiSpecificiRiscossione, BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp,
			String codRtDatiPagDatiSingPagAllegatoRicevutaTipo, byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest) throws DataAccessException;

	MygovRptRtDettaglio insert(MygovRptRt mygov_rpt_rt, BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento,
			BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa, String deRptDatiVersDatiSingVersIbanAccredito,
			String deRptDatiVersDatiSingVersBicAccredito, String deRptDatiVersDatiSingVersIbanAppoggio, String deRptDatiVersDatiSingVersBicAppoggio,
			String codRptDatiVersDatiSingVersCredenzialiPagatore, String deRptDatiVersDatiSingVersCausaleVersamento,
			String deRptDatiVersDatiSingVersDatiSpecificiRiscossione, BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato,
			String deRtDatiPagDatiSingPagEsitoSingoloPagamento, Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento,
			String codRtDatiPagDatiSingPagIdUnivocoRiscossione, String deRtDatiPagDatiSingPagCausaleVersamento,
			String deRtDatiPagDatiSingPagDatiSpecificiRiscossione, BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp,
			String codRtDatiPagDatiSingPagAllegatoRicevutaTipo, byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest,
			final String codRptDatiVersDatiSingVersDatiMbdTipoBollo, final String codRptDatiVersDatiSingVersDatiMbdHashDocumento,
			final String codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza) throws DataAccessException;

	MygovRptRtDettaglio updateDateRt(Long mygovRptRtDettaglioId, int version, BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato,
			String deRtDatiPagDatiSingPagEsitoSingoloPagamento, Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento,
			String codRtDatiPagDatiSingPagIdUnivocoRiscossione, String deRtDatiPagDatiSingPagCausaleVersamento,
			String deRtDatiPagDatiSingPagDatiSpecificiRiscossione, BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp,
			String codRtDatiPagDatiSingPagAllegatoRicevutaTipo, byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest) throws DataAccessException;

}
