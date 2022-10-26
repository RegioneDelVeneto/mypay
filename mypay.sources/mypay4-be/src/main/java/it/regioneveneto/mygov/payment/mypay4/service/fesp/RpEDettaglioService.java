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

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RpEDettaglioDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RpE;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RpEDettaglio;
import it.veneto.regione.schemas._2012.pagamenti.CtAllegatoRicevuta;
import it.veneto.regione.schemas._2012.pagamenti.CtDatiSingoloPagamentoEsito;
import it.veneto.regione.schemas._2012.pagamenti.CtDatiSingoloVersamentoRP;
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
public class RpEDettaglioService {

  @Resource
  private RpEDettaglioService self;
  @Autowired
  RpEDettaglioDao rpEDettaglioDao;

  public List<RpEDettaglio> getByRpE(RpE rpE) {
    List<RpEDettaglio> rptRtDettaglios = rpEDettaglioDao.getByRpE(rpE.getMygovRpEId());
    return Collections.unmodifiableList(rptRtDettaglios);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateDateE(RpE rpE, List<CtDatiSingoloPagamentoEsito> ctDatiSingoloPagamentoEsitoList) {
    Iterator<CtDatiSingoloPagamentoEsito> listDettagliDaAggiornare = ctDatiSingoloPagamentoEsitoList.iterator();
    List<RpEDettaglio> dettaglioList = self.getByRpE(rpE);
    for (RpEDettaglio dettaglio : dettaglioList) {
      CtDatiSingoloPagamentoEsito next = listDettagliDaAggiornare.next();
      Date now = new Date();
      dettaglio = dettaglio.toBuilder()
          .dtUltimaModifica(now)
          .numEDatiPagDatiSingPagSingoloImportoPagato(next.getSingoloImportoPagato())
          .deEDatiPagDatiSingPagEsitoSingoloPagamento(next.getEsitoSingoloPagamento())
          .dtEDatiPagDatiSingPagDataEsitoSingoloPagamento(next.getDataEsitoSingoloPagamento().toGregorianCalendar().getTime())
          .codEDatiPagDatiSingPagIdUnivocoRiscoss(next.getIdentificativoUnivocoRiscossione())
          .deEDatiPagDatiSingPagCausaleVersamento(next.getCausaleVersamento())
          .deEDatiPagDatiSingPagDatiSpecificiRiscossione(next.getDatiSpecificiRiscossione())
          .numEDatiPagDatiSingPagCommissioniApplicatePsp(next.getCommissioniApplicatePSP())
          .codEDatiPagDatiSingPagAllegatoRicevutaTipo(Optional.ofNullable(next.getAllegatoRicevuta())
              .map(CtAllegatoRicevuta::getTipoAllegatoRicevuta)
              .map(Enum::toString)
              .orElse(null))
          .blbEDatiPagDatiSingPagAllegatoRicevutaTest(Optional.ofNullable(next.getAllegatoRicevuta())
              .map(CtAllegatoRicevuta::getTestoAllegato)
              .orElse(null))
          .build();
      int updated = rpEDettaglioDao.updateDateE(dettaglio);
      if(updated!=1)
        throw new MyPayException(String.format("invalid number of rows updated: %d for MygovRpEDettaglioId: %d", updated, dettaglio.getMygovRpEDettaglioId()));
    }
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void insertRpEDettaglio(RpE rpE, List<CtDatiSingoloVersamentoRP> ctDatiSingoloVersamentoRPList) {

    for (CtDatiSingoloVersamentoRP dettaglioRP: ctDatiSingoloVersamentoRPList) {
      Date now = new Date();
      RpEDettaglio dettaglio = RpEDettaglio.builder()
          .mygovRpEId(rpE)
          .dtCreazione(now)
          .dtUltimaModifica(now)
          .numRpDatiVersDatiSingVersImportoSingoloVersamento(dettaglioRP.getImportoSingoloVersamento())
          .numRpDatiVersDatiSingVersCommissioneCaricoPa(dettaglioRP.getCommissioneCaricoPA())
          .codRpDatiVersDatiSingVersIbanAccredito(dettaglioRP.getIbanAccredito())
          .codRpDatiVersDatiSingVersBicAccredito(dettaglioRP.getBicAccredito())
          .codRpDatiVersDatiSingVersIbanAppoggio(dettaglioRP.getIbanAppoggio())
          .codRpDatiVersDatiSingVersBicAppoggio(dettaglioRP.getBicAppoggio())
          .codRpDatiVersDatiSingVersCredenzialiPagatore(dettaglioRP.getCredenzialiPagatore())
          .deRpDatiVersDatiSingVersCausaleVersamento(dettaglioRP.getCausaleVersamento())
          .deRpDatiVersDatiSingVersDatiSpecificiRiscossione(dettaglioRP.getDatiSpecificiRiscossione())
          .build();

      if (dettaglioRP.getDatiMarcaBolloDigitale() != null) {
        dettaglio.setCodRpDatiVersDatiSingVersDatiMbdTipoBollo(dettaglioRP.getDatiMarcaBolloDigitale().getTipoBollo());
        dettaglio.setCodRpDatiVersDatiSingVersDatiMbdHashDocumento(dettaglioRP.getDatiMarcaBolloDigitale().getHashDocumento());
        dettaglio.setCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza(dettaglioRP.getDatiMarcaBolloDigitale().getProvinciaResidenza());
      }
      long newId = rpEDettaglioDao.insert(dettaglio);
      log.info("insert RpEDettaglio, new Id: %d", newId);
    }
  }
}
