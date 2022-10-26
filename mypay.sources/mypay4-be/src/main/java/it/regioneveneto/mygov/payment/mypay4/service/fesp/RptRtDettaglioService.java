/**
 *     MyPay - Payment portal of Regione Veneto.
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import it.gov.digitpa.schemas._2011.pagamenti.CtAllegatoRicevuta;
import it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloPagamentoRT;
import it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RptRtDettaglioDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRtDettaglio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class RptRtDettaglioService {

  @Resource
  RptRtDettaglioService self;
  @Autowired
  RptRtDettaglioDao rptRtDettaglioDao;

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void insertRptRtDettaglio(RptRt rptRt, List<CtDatiSingoloVersamentoRPT> ctDatiSingoloVersamentoRPTList) {
    for (CtDatiSingoloVersamentoRPT item: ctDatiSingoloVersamentoRPTList) {
      Date now = new Date();
      RptRtDettaglio dettaglio = RptRtDettaglio.builder()
          .mygovRptRtId(rptRt)
          .dtCreazione(now)
          .dtUltimaModifica(now)
          .numRptDatiVersDatiSingVersImportoSingoloVersamento(item.getImportoSingoloVersamento())
          .numRptDatiVersDatiSingVersCommissioneCaricoPa(item.getCommissioneCaricoPA())
          .deRptDatiVersDatiSingVersIbanAccredito(item.getIbanAccredito())
          .deRptDatiVersDatiSingVersBicAccredito(item.getBicAccredito())
          .deRptDatiVersDatiSingVersIbanAppoggio(item.getIbanAppoggio())
          .deRptDatiVersDatiSingVersBicAppoggio(item.getBicAppoggio())
          .codRptDatiVersDatiSingVersCredenzialiPagatore(item.getCredenzialiPagatore())
          .deRptDatiVersDatiSingVersCausaleVersamento(item.getCausaleVersamento())
          .deRptDatiVersDatiSingVersDatiSpecificiRiscossione(item.getDatiSpecificiRiscossione())
          .build();

      if (item.getDatiMarcaBolloDigitale() != null) {
        dettaglio.setCodRptDatiVersDatiSingVersDatiMbdTipoBollo(item.getDatiMarcaBolloDigitale().getTipoBollo());
        dettaglio.setCodRptDatiVersDatiSingVersDatiMbdHashDocumento(item.getDatiMarcaBolloDigitale().getHashDocumento());
        dettaglio.setCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza(item.getDatiMarcaBolloDigitale().getProvinciaResidenza());
      }
      long newId = rptRtDettaglioDao.insert(dettaglio);
      log.info("insert RpEDettaglio, new Id: %d", newId);
    }
  }

  public List<RptRtDettaglio> getByRptRtId(RptRt rptRt) {
    List<RptRtDettaglio> rptRtDettaglios = rptRtDettaglioDao.getByRptRtId(rptRt.getMygovRptRtId());
    return Collections.unmodifiableList(rptRtDettaglios);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateDateRt(RptRt rptRt, List<CtDatiSingoloPagamentoRT> ctDatiSingoloPagamentoRTList) {
    Iterator<CtDatiSingoloPagamentoRT> listDettagliDaAggiornare = ctDatiSingoloPagamentoRTList.iterator();
    List<RptRtDettaglio> dettaglioList = self.getByRptRtId(rptRt);
    for (RptRtDettaglio dettaglio : dettaglioList) {
      CtDatiSingoloPagamentoRT next = listDettagliDaAggiornare.next();
      Date now = new Date();
      dettaglio = dettaglio.toBuilder()
          .dtUltimaModifica(now)
          .numRtDatiPagDatiSingPagSingoloImportoPagato(next.getSingoloImportoPagato())
          .deRtDatiPagDatiSingPagEsitoSingoloPagamento(next.getEsitoSingoloPagamento())
          .dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento(next.getDataEsitoSingoloPagamento().toGregorianCalendar().getTime())
          .codRtDatiPagDatiSingPagIdUnivocoRiscossione(next.getIdentificativoUnivocoRiscossione())
          .deRtDatiPagDatiSingPagCausaleVersamento(next.getCausaleVersamento())
          .deRtDatiPagDatiSingPagDatiSpecificiRiscossione(next.getDatiSpecificiRiscossione())
          .numRtDatiPagDatiSingPagCommissioniApplicatePsp(next.getCommissioniApplicatePSP())
          .codRtDatiPagDatiSingPagAllegatoRicevutaTipo(
              Optional.ofNullable(next.getAllegatoRicevuta()).map(CtAllegatoRicevuta::getTipoAllegatoRicevuta).map(Enum::toString).orElse(null))
          .blbRtDatiPagDatiSingPagAllegatoRicevutaTest(
              Optional.ofNullable(next.getAllegatoRicevuta()).map(CtAllegatoRicevuta::getTestoAllegato).orElse(null))
          .build();
      int updated = rptRtDettaglioDao.updateDateRt(dettaglio);
      if(updated!=1)
        throw new MyPayException(String.format("invalid number of rows updated: %d for MygovRptRtDettaglioId: %d", updated, dettaglio.getMygovRptRtDettaglioId()));
    }
  }
}
