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
package it.regioneveneto.mygov.payment.mypay4.scheduled.handlertdelivered;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Receipt;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.HandleRTDeliveredService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.ReceiptService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue= HandleRTDeliveredTaskApplication.NAME)
class HandleRTDeliveredTaskService {

  @Value("${task.handleRTDelivered.recovery.jobFrequency:60}")
  private int recoveryJobFrequency;

  @Value("${task.handleRTDelivered.recovery.baseMinutes:120}")
  private double recoveryBaseMinutes;
  @Value("${task.handleRTDelivered.recovery.multiplier:1.9}")
  private double recoveryMultiplier;
  @Value("${task.handleRTDelivered.recovery.maxTries:14}")
  private int maxTries;

  @Autowired
  ReceiptService receiptService;

  @Autowired
  HandleRTDeliveredService handleRTDeliveredService;

  private long counter = 0;
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.NEVER)
  public void handleRTDelivered(){
    log.info("handleRTDelivered start [{}]", ++counter);
    List<Long> receipts = receiptService.findIdByReadyOrForceStatus();
    if(!receipts.isEmpty()) {
      log.info("receipts to send: {}", receipts.size());
      receipts.forEach(handleRTDeliveredService::handleRTDelivered);
    }
    if(counter % recoveryJobFrequency == 0){
      log.info("start recovery task");
      List<Receipt> receiptsToRecovery = receiptService.findReceiptDataToRecovery();
      if(!receiptsToRecovery.isEmpty()) {
        long now = System.currentTimeMillis();
        long numReceiptsInError = receiptsToRecovery.size();
        AtomicInteger numReceiptsToRecovery = new AtomicInteger(0);
        AtomicInteger numReceiptsMarkedAsUnrecoverable = new AtomicInteger(0);
        receiptsToRecovery.forEach(r -> {
          long numTries = r.getNumTriesProcessing();
          if (numTries >= maxTries) {
            log.info("mark receipt[{}] as unrecoverable, max tries reached", r.getMygovReceiptId());
            numReceiptsMarkedAsUnrecoverable.incrementAndGet();
            receiptService.updateStatus(r.getMygovReceiptId(), Constants.RECEIPT_STATUS.UNRCVR.name(), false, false);
          } else {
            long deltaNextAttempt = 60 * 1000 * Double.valueOf(recoveryBaseMinutes * Math.pow(recoveryMultiplier, Math.max(0, numTries - 1))).longValue();
            if (r.getDtProcessing()==null || now > r.getDtProcessing().getTime() + deltaNextAttempt) {
              numReceiptsToRecovery.incrementAndGet();
              receiptService.updateStatus(r.getMygovReceiptId(), Constants.RECEIPT_STATUS.READY.name(), false, false);
            }
          }
        });
        log.info("receipt inErrorState / markedForNewTry / markedAsUnrecoverable: [{}/{}/{}]", numReceiptsInError, numReceiptsToRecovery.get(), numReceiptsMarkedAsUnrecoverable.get());
      }
    }
    log.info("handleRTDelivered stop [{}]", counter);
  }
}
