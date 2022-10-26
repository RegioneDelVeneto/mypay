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
package it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciCCP25;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("PagamentiTelematiciCCP25FespImpl")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciCCP25Impl implements PagamentiTelematiciCCP25 {

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String nodoRegionaleFespIdentificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String nodoRegionaleFespIdentificativoStazioneIntermediarioPA;

  @Autowired
  GiornaleService giornaleFespService;


  @Override
  public PaVerifyPaymentNoticeRes paVerifyPaymentNotice(PaVerifyPaymentNoticeReq request) {
    return null;
  }

  @Override
  public PaGetPaymentRes paGetPayment(PaGetPaymentReq request) {
    return null;
  }

  @Override
  public PaSendRTRes paSendRT(PaSendRTReq request) {
    return null;
  }

}
