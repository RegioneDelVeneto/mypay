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

import it.regioneveneto.mygov.payment.mypay4.dto.ExportReceiptEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.Executor;

@Service
@Slf4j
public class AsyncExportReceiptService {

  @Value("${async.exportReceipt.corePoolSize:5}")
  String corePoolSize;
  @Value("${async.exportReceipt.maxPoolSize:10}")
  String maxPoolSize;
  @Value("${async.exportReceipt.queueCapacity:500}")
  String queueCapacity;

  @Autowired
  private ReceiptService receiptService;

  @Async("ExportReceiptTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void exportRT(ExportReceiptEvent exportReceiptEvent){
    log.info("TransactionalEventListener exportReceipt for mygovReceiptId: {}", exportReceiptEvent.getMygovReceiptId());
    try {
      receiptService.exportReceipt(exportReceiptEvent.getMygovReceiptId());
    } catch(Exception e){
      log.warn("error in TransactionalEventListener exportReceipt", e);
      throw e;
    }
  }

  @Bean("ExportReceiptTaskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Integer.parseInt(corePoolSize));
    executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
    executor.setQueueCapacity(Integer.parseInt(queueCapacity));
    executor.setThreadNamePrefix("MyPay4ExportReceipt-");
    executor.initialize();
    return executor;
  }
}

