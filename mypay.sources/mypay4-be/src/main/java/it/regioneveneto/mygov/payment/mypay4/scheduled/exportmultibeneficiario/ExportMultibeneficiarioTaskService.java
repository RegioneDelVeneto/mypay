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
package it.regioneveneto.mygov.payment.mypay4.scheduled.exportmultibeneficiario;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.model.Receipt;
import it.regioneveneto.mygov.payment.mypay4.service.ReceiptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue= ExportMultibeneficiarioTaskApplication.NAME)
public class ExportMultibeneficiarioTaskService {

  @Value("${task.exportMultibeneficiario.maxTry:10}")
  private int maxTry;

  @Value("${task.exportMultibeneficiario.initialIntervalMinutes:60}")
  private long initialIntervalMinutes;

  @Value("${task.exportMultibeneficiario.baseIncrementMinutes:1}")
  private long baseIncrementMinutes;

  @Value("${task.exportMultibeneficiario.multiplier:3.0}")
  private double multiplier;

  @Value("${task.exportMultibeneficiario.maxIntervalMinutes:43200}") // 43200 = 60*24*30 = 30 days
  private long maxIntervalMinutes;

  @Autowired
  private ReceiptService receiptService;

  private long counter = 0;

  @Transactional(propagation = Propagation.NEVER)
  public void exportMultibeneficiario() {
    log.info("exportMultibeneficiario start [{}]", ++counter);
    List<Receipt> receipts = receiptService.findByMultiBeneficiaryNotExported();
    if(!receipts.isEmpty()) {
      log.info("receipts to export: {}", receipts.size());
      receipts.forEach( receipt -> {
        boolean forceError = false;
        boolean toProcess = true;
        if(receipt.getStatusExport()==Receipt.STATUS_S) {
          if (receipt.getNumTryExport() > maxTry) {
            log.info("set receipt [{}}] as unrecoverable error due to maxTry[{}] reached", receipt.getMygovReceiptId(), receipt.getNumTryExport());
            forceError = true;
          } else if (System.currentTimeMillis() - receipt.getDtLastExport().getTime() <
            (initialIntervalMinutes + baseIncrementMinutes * Math.pow(multiplier, receipt.getNumTryExport())) * 1000 * 60 ) {
            toProcess = false;
          } else if (maxIntervalMinutes * 1000 * 60 < System.currentTimeMillis() - receipt.getDtLastExport().getTime()) {
            log.info("set receipt [{}}] as unrecoverable error due to maxIntervalMinutes[{}] reached", receipt.getMygovReceiptId(), maxIntervalMinutes);
            forceError = true;
          }
        }
        // set as unrecoverable error also if already tried all available retries and is next to trigger a new try
        if(forceError || toProcess && receipt.getNumTryExport() >= maxTry)
          receiptService.setExportStatusError(receipt.getMygovReceiptId());
        else if(toProcess)
          receiptService.exportReceipt(receipt.getMygovReceiptId());
      });
    }
    log.info("exportMultibeneficiario stop [{}]", counter);
  }

}