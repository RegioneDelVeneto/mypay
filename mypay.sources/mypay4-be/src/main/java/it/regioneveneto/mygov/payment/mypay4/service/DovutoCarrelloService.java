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
package it.regioneveneto.mygov.payment.mypay4.service;

import it.regioneveneto.mygov.payment.mypay4.dao.DovutoCarrelloDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoCarrello;
import it.veneto.regione.schemas._2012.pagamenti.RP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.IntStream;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class DovutoCarrelloService {

  @Autowired
  DovutoCarrelloDao dovutoCarrelloDao;

  /**
   * This is the same method as CarrelloService.dovutoCarrelloElimina() in Mypay3.
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public void insertRP(Carrello carrello, List<Dovuto> dovuti, RP rp) {
    IntStream.range(0, dovuti.size()).forEach(index -> {
      DovutoCarrello dovutoCarrello = DovutoCarrello.builder()
        .mygovCarrelloId(carrello)
        .mygovDovutoId(dovuti.get(index))
        .numRpDatiVersDatiSingVersCommissioneCaricoPa(
          rp.getDatiVersamento().getDatiSingoloVersamentos().get(index).getCommissioneCaricoPA())
        .codRpDatiVersDatiSingVersIbanAccredito(
          rp.getDatiVersamento().getDatiSingoloVersamentos().get(index).getIbanAccredito())
        .codRpDatiVersDatiSingVersBicAccredito(
          rp.getDatiVersamento().getDatiSingoloVersamentos().get(index).getBicAccredito())
        .codRpDatiVersDatiSingVersIbanAppoggio(
          rp.getDatiVersamento().getDatiSingoloVersamentos().get(index).getIbanAppoggio())
        .codRpDatiVersDatiSingVersBicAppoggio(
          rp.getDatiVersamento().getDatiSingoloVersamentos().get(index).getBicAppoggio())
        .codRpDatiVersDatiSingVersCredenzialiPagatore(
          rp.getDatiVersamento().getDatiSingoloVersamentos().get(index).getCredenzialiPagatore())
        .deRpDatiVersDatiSingVersCausaleVersamentoAgid(
          rp.getDatiVersamento().getDatiSingoloVersamentos().get(index).getCausaleVersamento())
      .build();
      long newId = dovutoCarrelloDao.insert(dovutoCarrello);
      log.info("insert DovutoCarrello, new Id: "+newId);
    });
  }

  /**
   * This is the same method as CarrelloService.dovutoCarrelloElimina() in Mypay3.
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public int deleteDovutoCarrello(Long mygovDovutoId, Long mygovCarrelloId) {
    return dovutoCarrelloDao.delete(mygovDovutoId, mygovCarrelloId);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int deleteDovutoCarrelloByIdDovuto(Long mygovDovutoId) {
    return dovutoCarrelloDao.deleteByIdDovuto(mygovDovutoId);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public DovutoCarrello getByDovutoECarrello(Long mygovDovutoId, Long mygovCarrelloId) {
    List<DovutoCarrello> list = dovutoCarrelloDao.getByDovutoECarrello(mygovDovutoId, mygovCarrelloId);
    if (list != null && list.size() > 1)
      throw new MyPayException("pa.dovutoCarrello.dovutoCarrelloDuplicato");
    return CollectionUtils.isEmpty(list) ? null : list.get(0);
  }
}
