package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RpEDettaglioDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpE;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRpEDettaglio;

/**
 * @author regione del veneto
 *
 */
public class HibernateRpEDettaglioDao extends HibernateDaoSupport implements RpEDettaglioDao {

	private static final Log log = LogFactory.getLog(HibernateRpEDettaglioDao.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<MygovRpEDettaglio> getByRpE(MygovRpE mygovRpE) throws DataAccessException {

		log.debug("Invocato metodo getByRpE per ID = [" + mygovRpE.getMygovRpEId() + "]");

		List<MygovRpEDettaglio> results = getHibernateTemplate().find(
				"from MygovRpEDettaglio mygovRpEDettaglio where mygovRpEDettaglio.mygov_rp_e = ? order by mygovRpEDettaglio.mygovRpEDettaglioId", mygovRpE);

		return results;
	}

	@Override
	public MygovRpEDettaglio insert(final MygovRpE mygovRpE, final BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento,
			final BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa, final String codRpDatiVersDatiSingVersIbanAccredito,
			final String codRpDatiVersDatiSingVersBicAccredito, final String codRpDatiVersDatiSingVersIbanAppoggio,
			final String codRpDatiVersDatiSingVersBicAppoggio, final String codRpDatiVersDatiSingVersCredenzialiPagatore,
			final String deRpDatiVersDatiSingVersCausaleVersamento, final String deRpDatiVersDatiSingVersDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato, final String deEDatiPagDatiSingPagEsitoSingoloPagamento,
			final Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento, final String codEDatiPagDatiSingPagIdUnivocoRiscoss,
			final String deEDatiPagDatiSingPagCausaleVersamento, final String deEDatiPagDatiSingPagDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp, final String codEDatiPagDatiSingPagAllegatoRicevutaTipo,
			final byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest) throws DataAccessException {

		return doInsert(mygovRpE, numRpDatiVersDatiSingVersImportoSingoloVersamento, numRpDatiVersDatiSingVersCommissioneCaricoPa,
				codRpDatiVersDatiSingVersIbanAccredito, codRpDatiVersDatiSingVersBicAccredito, codRpDatiVersDatiSingVersIbanAppoggio,
				codRpDatiVersDatiSingVersBicAppoggio, codRpDatiVersDatiSingVersCredenzialiPagatore, deRpDatiVersDatiSingVersCausaleVersamento,
				deRpDatiVersDatiSingVersDatiSpecificiRiscossione, numEDatiPagDatiSingPagSingoloImportoPagato, deEDatiPagDatiSingPagEsitoSingoloPagamento,
				dtEDatiPagDatiSingPagDataEsitoSingoloPagamento, codEDatiPagDatiSingPagIdUnivocoRiscoss, deEDatiPagDatiSingPagCausaleVersamento,
				deEDatiPagDatiSingPagDatiSpecificiRiscossione, numEDatiPagDatiSingPagCommissioniApplicatePsp, codEDatiPagDatiSingPagAllegatoRicevutaTipo,
				blbEDatiPagDatiSingPagAllegatoRicevutaTest);

	}

	@Override
	public MygovRpEDettaglio insert(final MygovRpE mygovRpE, final BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento,
			final BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa, final String codRpDatiVersDatiSingVersIbanAccredito,
			final String codRpDatiVersDatiSingVersBicAccredito, final String codRpDatiVersDatiSingVersIbanAppoggio,
			final String codRpDatiVersDatiSingVersBicAppoggio, final String codRpDatiVersDatiSingVersCredenzialiPagatore,
			final String deRpDatiVersDatiSingVersCausaleVersamento, final String deRpDatiVersDatiSingVersDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato, final String deEDatiPagDatiSingPagEsitoSingoloPagamento,
			final Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento, final String codEDatiPagDatiSingPagIdUnivocoRiscoss,
			final String deEDatiPagDatiSingPagCausaleVersamento, final String deEDatiPagDatiSingPagDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp, final String codEDatiPagDatiSingPagAllegatoRicevutaTipo,
			final byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest, final String codRpDatiVersDatiSingVersDatiMbdTipoBollo,
			final String codRpDatiVersDatiSingVersDatiMbdHashDocumento, final String codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza)
			throws DataAccessException {

		return doInsert(mygovRpE, numRpDatiVersDatiSingVersImportoSingoloVersamento, numRpDatiVersDatiSingVersCommissioneCaricoPa,
				codRpDatiVersDatiSingVersIbanAccredito, codRpDatiVersDatiSingVersBicAccredito, codRpDatiVersDatiSingVersIbanAppoggio,
				codRpDatiVersDatiSingVersBicAppoggio, codRpDatiVersDatiSingVersCredenzialiPagatore, deRpDatiVersDatiSingVersCausaleVersamento,
				deRpDatiVersDatiSingVersDatiSpecificiRiscossione, numEDatiPagDatiSingPagSingoloImportoPagato, deEDatiPagDatiSingPagEsitoSingoloPagamento,
				dtEDatiPagDatiSingPagDataEsitoSingoloPagamento, codEDatiPagDatiSingPagIdUnivocoRiscoss, deEDatiPagDatiSingPagCausaleVersamento,
				deEDatiPagDatiSingPagDatiSpecificiRiscossione, numEDatiPagDatiSingPagCommissioniApplicatePsp, codEDatiPagDatiSingPagAllegatoRicevutaTipo,
				blbEDatiPagDatiSingPagAllegatoRicevutaTest, codRpDatiVersDatiSingVersDatiMbdTipoBollo, codRpDatiVersDatiSingVersDatiMbdHashDocumento,
				codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza);

	}

	@Override
	public MygovRpEDettaglio updateDateE(final Long mygovRpEDettaglioId, final int version, final MygovRpE mygovRpE,
			final BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato, final String deEDatiPagDatiSingPagEsitoSingoloPagamento,
			final Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento, final String codEDatiPagDatiSingPagIdUnivocoRiscoss,
			final String deEDatiPagDatiSingPagCausaleVersamento, final String deEDatiPagDatiSingPagDatiSpecificiRiscossione,
			final BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp, final String codEDatiPagDatiSingPagAllegatoRicevutaTipo,
			final byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest) throws DataAccessException {

		MygovRpEDettaglio mygovRpEDettaglio = doGet(mygovRpEDettaglioId);
		if (mygovRpEDettaglio == null)
			throw new DataRetrievalFailureException("'mygovRpEDettaglioId' is not valid");

		mygovRpEDettaglio.setDtUltimaModifica(new Date());
		mygovRpEDettaglio.setNumEDatiPagDatiSingPagSingoloImportoPagato(numEDatiPagDatiSingPagSingoloImportoPagato);
		mygovRpEDettaglio.setDeEDatiPagDatiSingPagEsitoSingoloPagamento(deEDatiPagDatiSingPagEsitoSingoloPagamento);
		mygovRpEDettaglio.setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(dtEDatiPagDatiSingPagDataEsitoSingoloPagamento);
		mygovRpEDettaglio.setCodEDatiPagDatiSingPagIdUnivocoRiscoss(codEDatiPagDatiSingPagIdUnivocoRiscoss);
		mygovRpEDettaglio.setDeEDatiPagDatiSingPagCausaleVersamento(deEDatiPagDatiSingPagCausaleVersamento);
		mygovRpEDettaglio.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(deEDatiPagDatiSingPagDatiSpecificiRiscossione);

		mygovRpEDettaglio.setNumEDatiPagDatiSingPagCommissioniApplicatePsp(numEDatiPagDatiSingPagCommissioniApplicatePsp);
		mygovRpEDettaglio.setCodEDatiPagDatiSingPagAllegatoRicevutaTipo(codEDatiPagDatiSingPagAllegatoRicevutaTipo);
		mygovRpEDettaglio.setBlbEDatiPagDatiSingPagAllegatoRicevutaTest(blbEDatiPagDatiSingPagAllegatoRicevutaTest);
		if ((version != mygovRpEDettaglio.getVersion()))
			throw new ObjectOptimisticLockingFailureException(MygovRpEDettaglio.class, mygovRpEDettaglioId);

		getHibernateTemplate().update(mygovRpEDettaglio);

		return mygovRpEDettaglio;
	}

	protected MygovRpEDettaglio doGet(final long mygovRpEDettaglioId) throws DataAccessException {

		log.debug("Invocato metodo doGet PARAMETRI ::: " + "mygovRpEDettaglioId = [" + mygovRpEDettaglioId);

		return getHibernateTemplate().get(MygovRpEDettaglio.class, mygovRpEDettaglioId);
	}

	protected MygovRpEDettaglio doInsert(MygovRpE mygovRpE, BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento,
			BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa, String codRpDatiVersDatiSingVersIbanAccredito,
			String codRpDatiVersDatiSingVersBicAccredito, String codRpDatiVersDatiSingVersIbanAppoggio, String codRpDatiVersDatiSingVersBicAppoggio,
			String codRpDatiVersDatiSingVersCredenzialiPagatore, String deRpDatiVersDatiSingVersCausaleVersamento,
			String deRpDatiVersDatiSingVersDatiSpecificiRiscossione, BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato,
			String deEDatiPagDatiSingPagEsitoSingoloPagamento, Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento,
			String codEDatiPagDatiSingPagIdUnivocoRiscoss, String deEDatiPagDatiSingPagCausaleVersamento, String deEDatiPagDatiSingPagDatiSpecificiRiscossione,
			BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp, String codEDatiPagDatiSingPagAllegatoRicevutaTipo,
			byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest) {

		MygovRpEDettaglio mygovRpEDettaglio = new MygovRpEDettaglio(mygovRpE, new Date(), new Date(), numRpDatiVersDatiSingVersImportoSingoloVersamento,
				numRpDatiVersDatiSingVersCommissioneCaricoPa, codRpDatiVersDatiSingVersIbanAccredito, codRpDatiVersDatiSingVersBicAccredito,
				codRpDatiVersDatiSingVersIbanAppoggio, codRpDatiVersDatiSingVersBicAppoggio, codRpDatiVersDatiSingVersCredenzialiPagatore,
				deRpDatiVersDatiSingVersCausaleVersamento, deRpDatiVersDatiSingVersDatiSpecificiRiscossione, numEDatiPagDatiSingPagSingoloImportoPagato,
				deEDatiPagDatiSingPagEsitoSingoloPagamento, dtEDatiPagDatiSingPagDataEsitoSingoloPagamento, codEDatiPagDatiSingPagIdUnivocoRiscoss,
				deEDatiPagDatiSingPagCausaleVersamento, deEDatiPagDatiSingPagDatiSpecificiRiscossione, numEDatiPagDatiSingPagCommissioniApplicatePsp,
				codEDatiPagDatiSingPagAllegatoRicevutaTipo, blbEDatiPagDatiSingPagAllegatoRicevutaTest, null, null, null);

		getHibernateTemplate().save(mygovRpEDettaglio);
		return mygovRpEDettaglio;

	}

	protected MygovRpEDettaglio doInsert(MygovRpE mygovRpE, BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento,
			BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa, String codRpDatiVersDatiSingVersIbanAccredito,
			String codRpDatiVersDatiSingVersBicAccredito, String codRpDatiVersDatiSingVersIbanAppoggio, String codRpDatiVersDatiSingVersBicAppoggio,
			String codRpDatiVersDatiSingVersCredenzialiPagatore, String deRpDatiVersDatiSingVersCausaleVersamento,
			String deRpDatiVersDatiSingVersDatiSpecificiRiscossione, BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato,
			String deEDatiPagDatiSingPagEsitoSingoloPagamento, Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento,
			String codEDatiPagDatiSingPagIdUnivocoRiscoss, String deEDatiPagDatiSingPagCausaleVersamento, String deEDatiPagDatiSingPagDatiSpecificiRiscossione,
			BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp, String codEDatiPagDatiSingPagAllegatoRicevutaTipo,
			byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest, final String codRpDatiVersDatiSingVersDatiMbdTipoBollo,
			final String codRpDatiVersDatiSingVersDatiMbdHashDocumento, final String codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza) {

		MygovRpEDettaglio mygovRpEDettaglio = new MygovRpEDettaglio(mygovRpE, new Date(), new Date(), numRpDatiVersDatiSingVersImportoSingoloVersamento,
				numRpDatiVersDatiSingVersCommissioneCaricoPa, codRpDatiVersDatiSingVersIbanAccredito, codRpDatiVersDatiSingVersBicAccredito,
				codRpDatiVersDatiSingVersIbanAppoggio, codRpDatiVersDatiSingVersBicAppoggio, codRpDatiVersDatiSingVersCredenzialiPagatore,
				deRpDatiVersDatiSingVersCausaleVersamento, deRpDatiVersDatiSingVersDatiSpecificiRiscossione, numEDatiPagDatiSingPagSingoloImportoPagato,
				deEDatiPagDatiSingPagEsitoSingoloPagamento, dtEDatiPagDatiSingPagDataEsitoSingoloPagamento, codEDatiPagDatiSingPagIdUnivocoRiscoss,
				deEDatiPagDatiSingPagCausaleVersamento, deEDatiPagDatiSingPagDatiSpecificiRiscossione, numEDatiPagDatiSingPagCommissioniApplicatePsp,
				codEDatiPagDatiSingPagAllegatoRicevutaTipo, blbEDatiPagDatiSingPagAllegatoRicevutaTest, codRpDatiVersDatiSingVersDatiMbdTipoBollo,
				codRpDatiVersDatiSingVersDatiMbdHashDocumento, codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza);

		getHibernateTemplate().save(mygovRpEDettaglio);
		return mygovRpEDettaglio;

	}
}
