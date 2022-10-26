package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RptRtDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRtDettaglio;

/**
 * @author regione del veneto
 *
 */
public class HibernateRptRtDettaglioDao extends HibernateDaoSupport implements RptRtDettaglioDao {

	private static final Log log = LogFactory.getLog(HibernateRptRtDettaglioDao.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<MygovRptRtDettaglio> getByRptRt(MygovRptRt mygovRptRt) throws DataAccessException {

		List<MygovRptRtDettaglio> results = getHibernateTemplate().find(
				"from MygovRptRtDettaglio mygovRptRtDettaglio where mygovRptRtDettaglio.mygov_rpt_rt = ? order by mygovRptRtDettaglio.mygovRptRtDettaglioId",
				mygovRptRt);

		return results;
	}

	@Override
	public MygovRptRtDettaglio insert(final MygovRptRt mygov_rpt_rt, final BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento,
			final BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa, final String deRptDatiVersDatiSingVersIbanAccredito,
			final String deRptDatiVersDatiSingVersBicAccredito, final String deRptDatiVersDatiSingVersIbanAppoggio,
			final String deRptDatiVersDatiSingVersBicAppoggio, final String codRptDatiVersDatiSingVersCredenzialiPagatore,
			final String deRptDatiVersDatiSingVersCausaleVersamento, final String deRptDatiVersDatiSingVersDatiSpecificiRiscossione,
			final BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato, final String deRtDatiPagDatiSingPagEsitoSingoloPagamento,
			final Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento, final String codRtDatiPagDatiSingPagIdUnivocoRiscossione,
			final String deRtDatiPagDatiSingPagCausaleVersamento, final String deRtDatiPagDatiSingPagDatiSpecificiRiscossione,
			final BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp, final String codRtDatiPagDatiSingPagAllegatoRicevutaTipo,
			final byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest) throws DataAccessException {

		return doInsert(mygov_rpt_rt, numRptDatiVersDatiSingVersImportoSingoloVersamento, numRptDatiVersDatiSingVersCommissioneCaricoPa,
				deRptDatiVersDatiSingVersIbanAccredito, deRptDatiVersDatiSingVersBicAccredito, deRptDatiVersDatiSingVersIbanAppoggio,
				deRptDatiVersDatiSingVersBicAppoggio, codRptDatiVersDatiSingVersCredenzialiPagatore, deRptDatiVersDatiSingVersCausaleVersamento,
				deRptDatiVersDatiSingVersDatiSpecificiRiscossione, numRtDatiPagDatiSingPagSingoloImportoPagato, deRtDatiPagDatiSingPagEsitoSingoloPagamento,
				dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento, codRtDatiPagDatiSingPagIdUnivocoRiscossione, deRtDatiPagDatiSingPagCausaleVersamento,
				deRtDatiPagDatiSingPagDatiSpecificiRiscossione, numRtDatiPagDatiSingPagCommissioniApplicatePsp, codRtDatiPagDatiSingPagAllegatoRicevutaTipo,
				blbRtDatiPagDatiSingPagAllegatoRicevutaTest);

	}

	@Override
	public MygovRptRtDettaglio insert(final MygovRptRt mygov_rpt_rt, final BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento,
			final BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa, final String deRptDatiVersDatiSingVersIbanAccredito,
			final String deRptDatiVersDatiSingVersBicAccredito, final String deRptDatiVersDatiSingVersIbanAppoggio,
			final String deRptDatiVersDatiSingVersBicAppoggio, final String codRptDatiVersDatiSingVersCredenzialiPagatore,
			final String deRptDatiVersDatiSingVersCausaleVersamento, final String deRptDatiVersDatiSingVersDatiSpecificiRiscossione,
			final BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato, final String deRtDatiPagDatiSingPagEsitoSingoloPagamento,
			final Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento, final String codRtDatiPagDatiSingPagIdUnivocoRiscossione,
			final String deRtDatiPagDatiSingPagCausaleVersamento, final String deRtDatiPagDatiSingPagDatiSpecificiRiscossione,
			final BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp, final String codRtDatiPagDatiSingPagAllegatoRicevutaTipo,
			final byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest, final String codRptDatiVersDatiSingVersDatiMbdTipoBollo,
			final String codRptDatiVersDatiSingVersDatiMbdHashDocumento, final String codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza)
			throws DataAccessException {

		return doInsert(mygov_rpt_rt, numRptDatiVersDatiSingVersImportoSingoloVersamento, numRptDatiVersDatiSingVersCommissioneCaricoPa,
				deRptDatiVersDatiSingVersIbanAccredito, deRptDatiVersDatiSingVersBicAccredito, deRptDatiVersDatiSingVersIbanAppoggio,
				deRptDatiVersDatiSingVersBicAppoggio, codRptDatiVersDatiSingVersCredenzialiPagatore, deRptDatiVersDatiSingVersCausaleVersamento,
				deRptDatiVersDatiSingVersDatiSpecificiRiscossione, numRtDatiPagDatiSingPagSingoloImportoPagato, deRtDatiPagDatiSingPagEsitoSingoloPagamento,
				dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento, codRtDatiPagDatiSingPagIdUnivocoRiscossione, deRtDatiPagDatiSingPagCausaleVersamento,
				deRtDatiPagDatiSingPagDatiSpecificiRiscossione, numRtDatiPagDatiSingPagCommissioniApplicatePsp, codRtDatiPagDatiSingPagAllegatoRicevutaTipo,
				blbRtDatiPagDatiSingPagAllegatoRicevutaTest, codRptDatiVersDatiSingVersDatiMbdTipoBollo, codRptDatiVersDatiSingVersDatiMbdHashDocumento,
				codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza);

	}

	@Override
	public MygovRptRtDettaglio updateDateRt(final Long mygovRptRtDettaglioId, final int version, final BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato,
			final String deRtDatiPagDatiSingPagEsitoSingoloPagamento, final Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento,
			final String codRtDatiPagDatiSingPagIdUnivocoRiscossione, final String deRtDatiPagDatiSingPagCausaleVersamento,
			final String deRtDatiPagDatiSingPagDatiSpecificiRiscossione, final BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp,
			final String codRtDatiPagDatiSingPagAllegatoRicevutaTipo, final byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest) throws DataAccessException {

		MygovRptRtDettaglio mygovRptRtDettaglio = doGet(mygovRptRtDettaglioId);

		mygovRptRtDettaglio.setDtUltimaModifica(new Date());
		mygovRptRtDettaglio.setNumRtDatiPagDatiSingPagSingoloImportoPagato(numRtDatiPagDatiSingPagSingoloImportoPagato);
		mygovRptRtDettaglio.setDeRtDatiPagDatiSingPagEsitoSingoloPagamento(deRtDatiPagDatiSingPagEsitoSingoloPagamento);
		mygovRptRtDettaglio.setDtRtDatiPagDatiSingPagDataEsitoSingoloPagamento(dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento);
		mygovRptRtDettaglio.setCodRtDatiPagDatiSingPagIdUnivocoRiscossione(codRtDatiPagDatiSingPagIdUnivocoRiscossione);
		mygovRptRtDettaglio.setDeRtDatiPagDatiSingPagCausaleVersamento(deRtDatiPagDatiSingPagCausaleVersamento);
		mygovRptRtDettaglio.setDeRtDatiPagDatiSingPagDatiSpecificiRiscossione(deRtDatiPagDatiSingPagDatiSpecificiRiscossione);
		mygovRptRtDettaglio.setNumRtDatiPagDatiSingPagCommissioniApplicatePsp(numRtDatiPagDatiSingPagCommissioniApplicatePsp);
		mygovRptRtDettaglio.setCodRtDatiPagDatiSingPagAllegatoRicevutaTipo(codRtDatiPagDatiSingPagAllegatoRicevutaTipo);
		mygovRptRtDettaglio.setBlbRtDatiPagDatiSingPagAllegatoRicevutaTest(blbRtDatiPagDatiSingPagAllegatoRicevutaTest);

		if ((version != mygovRptRtDettaglio.getVersion()))
			throw new ObjectOptimisticLockingFailureException(MygovRptRtDettaglio.class, mygovRptRtDettaglio);

		getHibernateTemplate().update(mygovRptRtDettaglio);

		return mygovRptRtDettaglio;
	}

	protected MygovRptRtDettaglio doInsert(MygovRptRt mygov_rpt_rt, BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento,
			BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa, String deRptDatiVersDatiSingVersIbanAccredito,
			String deRptDatiVersDatiSingVersBicAccredito, String deRptDatiVersDatiSingVersIbanAppoggio, String deRptDatiVersDatiSingVersBicAppoggio,
			String codRptDatiVersDatiSingVersCredenzialiPagatore, String deRptDatiVersDatiSingVersCausaleVersamento,
			String deRptDatiVersDatiSingVersDatiSpecificiRiscossione, BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato,
			String deRtDatiPagDatiSingPagEsitoSingoloPagamento, Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento,
			String codRtDatiPagDatiSingPagIdUnivocoRiscossione, String deRtDatiPagDatiSingPagCausaleVersamento,
			String deRtDatiPagDatiSingPagDatiSpecificiRiscossione, BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp,
			String codRtDatiPagDatiSingPagAllegatoRicevutaTipo, byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest) {

		MygovRptRtDettaglio mygovRptRtDettaglio = new MygovRptRtDettaglio(mygov_rpt_rt, new Date(), new Date(),
				numRptDatiVersDatiSingVersImportoSingoloVersamento, numRptDatiVersDatiSingVersCommissioneCaricoPa, deRptDatiVersDatiSingVersIbanAccredito,
				deRptDatiVersDatiSingVersBicAccredito, deRptDatiVersDatiSingVersIbanAppoggio, deRptDatiVersDatiSingVersBicAppoggio,
				codRptDatiVersDatiSingVersCredenzialiPagatore, deRptDatiVersDatiSingVersCausaleVersamento, deRptDatiVersDatiSingVersDatiSpecificiRiscossione,
				numRtDatiPagDatiSingPagSingoloImportoPagato, deRtDatiPagDatiSingPagEsitoSingoloPagamento, dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento,
				codRtDatiPagDatiSingPagIdUnivocoRiscossione, deRtDatiPagDatiSingPagCausaleVersamento, deRtDatiPagDatiSingPagDatiSpecificiRiscossione,
				numRtDatiPagDatiSingPagCommissioniApplicatePsp, codRtDatiPagDatiSingPagAllegatoRicevutaTipo, blbRtDatiPagDatiSingPagAllegatoRicevutaTest, null,
				null, null);

		getHibernateTemplate().save(mygovRptRtDettaglio);
		return mygovRptRtDettaglio;
	}

	protected MygovRptRtDettaglio doInsert(MygovRptRt mygov_rpt_rt, BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento,
			BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa, String deRptDatiVersDatiSingVersIbanAccredito,
			String deRptDatiVersDatiSingVersBicAccredito, String deRptDatiVersDatiSingVersIbanAppoggio, String deRptDatiVersDatiSingVersBicAppoggio,
			String codRptDatiVersDatiSingVersCredenzialiPagatore, String deRptDatiVersDatiSingVersCausaleVersamento,
			String deRptDatiVersDatiSingVersDatiSpecificiRiscossione, BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato,
			String deRtDatiPagDatiSingPagEsitoSingoloPagamento, Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento,
			String codRtDatiPagDatiSingPagIdUnivocoRiscossione, String deRtDatiPagDatiSingPagCausaleVersamento,
			String deRtDatiPagDatiSingPagDatiSpecificiRiscossione, BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp,
			String codRtDatiPagDatiSingPagAllegatoRicevutaTipo, byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest,
			final String codRptDatiVersDatiSingVersDatiMbdTipoBollo, final String codRptDatiVersDatiSingVersDatiMbdHashDocumento,
			final String codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza) {

		MygovRptRtDettaglio mygovRptRtDettaglio = new MygovRptRtDettaglio(mygov_rpt_rt, new Date(), new Date(),
				numRptDatiVersDatiSingVersImportoSingoloVersamento, numRptDatiVersDatiSingVersCommissioneCaricoPa, deRptDatiVersDatiSingVersIbanAccredito,
				deRptDatiVersDatiSingVersBicAccredito, deRptDatiVersDatiSingVersIbanAppoggio, deRptDatiVersDatiSingVersBicAppoggio,
				codRptDatiVersDatiSingVersCredenzialiPagatore, deRptDatiVersDatiSingVersCausaleVersamento, deRptDatiVersDatiSingVersDatiSpecificiRiscossione,
				numRtDatiPagDatiSingPagSingoloImportoPagato, deRtDatiPagDatiSingPagEsitoSingoloPagamento, dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento,
				codRtDatiPagDatiSingPagIdUnivocoRiscossione, deRtDatiPagDatiSingPagCausaleVersamento, deRtDatiPagDatiSingPagDatiSpecificiRiscossione,
				numRtDatiPagDatiSingPagCommissioniApplicatePsp, codRtDatiPagDatiSingPagAllegatoRicevutaTipo, blbRtDatiPagDatiSingPagAllegatoRicevutaTest,
				codRptDatiVersDatiSingVersDatiMbdTipoBollo, codRptDatiVersDatiSingVersDatiMbdHashDocumento,
				codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza);

		getHibernateTemplate().save(mygovRptRtDettaglio);
		return mygovRptRtDettaglio;
	}

	protected MygovRptRtDettaglio doGet(final long mygovRptRtDettaglioId) throws DataAccessException {
		return getHibernateTemplate().get(MygovRptRtDettaglio.class, mygovRptRtDettaglioId);
	}
}
