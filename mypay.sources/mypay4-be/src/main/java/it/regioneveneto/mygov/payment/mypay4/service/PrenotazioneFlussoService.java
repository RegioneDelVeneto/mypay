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

import it.regioneveneto.mygov.payment.mypay4.dto.WsExportFlussoIncomeTo;
import it.regioneveneto.mygov.payment.mypay4.dto.WsExportFlussoOutcomeTo;
import it.regioneveneto.mygov.payment.mypay4.model.ExportDovuti;
import it.regioneveneto.mygov.payment.mypay4.queue.QueueProducer;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.DEFAULT_VERSIONE_TRACCIATO;
import static it.regioneveneto.mygov.payment.mypay4.ws.helper.PagamentiTelematiciDovutiPagatiHelper.verificaDate;
import static it.regioneveneto.mygov.payment.mypay4.ws.helper.PagamentiTelematiciDovutiPagatiHelper.verificaTracciato;

@Service
@Slf4j
public class PrenotazioneFlussoService {

  @Autowired
  EnteService enteService;
  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;
  @Autowired
  UtenteService utenteService;
  @Autowired
  ExportDovutiService exportDovutiService;
  @Autowired
  private QueueProducer queueProducer;

  public WsExportFlussoOutcomeTo handlePrenotazioneFlussoExport(WsExportFlussoIncomeTo to) {

    //normalize incomeTo
    to.setCodIpaEnte(StringUtils.stripToNull(to.getCodIpaEnte()));
    to.setVersioneTracciato(StringUtils.defaultIfBlank(to.getVersioneTracciato(), DEFAULT_VERSIONE_TRACCIATO));
    to.setIdentificativoTipoDovuto(StringUtils.stripToNull(to.getIdentificativoTipoDovuto()));


    var faultBean = enteService.verificaEnte(to.getCodIpaEnte(), to.getPassword())
        .or(() -> verificaDate(to.getCodIpaEnte(), to.getDateFrom(), to.getDateTo()))
        .or(() -> enteTipoDovutoService.verificaTipoDovuto(to.getCodIpaEnte(), to.getIdentificativoTipoDovuto()))
        .or(() -> utenteService.verificaWsUser(to.getCodIpaEnte()))
        .or(() -> verificaTracciato(to.getCodIpaEnte(), to.getVersioneTracciato()));
    if (faultBean.isPresent()) {
      return WsExportFlussoOutcomeTo.builder()
          .faultBean(faultBean.get())
          .build();
    }
    if(!to.isIncrementale()) {
      to.setDateFrom(Utilities.toMidnight(to.getDateFrom()));
      to.setDateTo(Utilities.toMidnight(to.getDateTo()));
    }
    String requestToken = UUID.randomUUID().toString();
    ExportDovuti exportDovuti = exportDovutiService.insertFlussoExport(to, requestToken);

    //add message to EXPORT DOVUTI queue
    queueProducer.enqueueExportDovuti(exportDovuti.getMygovExportDovutiId());

    return WsExportFlussoOutcomeTo.builder()
        .dateTo(to.getDateTo())
        .requestToken(exportDovuti.getCodRequestToken())
        .build();
  }
}
