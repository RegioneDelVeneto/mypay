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

import it.regioneveneto.mygov.payment.mypay4.dto.fesp.HandleRtEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.Executor;

@Service
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Slf4j
public class AsyncHandleRTService {

  @Value("${async.handleRT.corePoolSize:5}")
  String corePoolSize;
  @Value("${async.handleRT.maxPoolSize:10}")
  String maxPoolSize;
  @Value("${async.handleRT.queueCapacity:500}")
  String queueCapacity;

  @Autowired
  private HandleRTDeliveredService handleRTDeliveredService;

  @Async("HandleRTTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRT(HandleRtEvent handleRtEvent){
    log.info("TransactionalEventListener handleRT for mygovReceiptId: {} - fiscalCodeEnte: {} - noticeNumber: {} - receiptId: {}",
      handleRtEvent.getMygovReceiptId(), handleRtEvent.getFiscalCode(), handleRtEvent.getNoticeNumber(), handleRtEvent.getReceiptId());
    try {
      handleRTDeliveredService.handleRTDelivered(handleRtEvent.getMygovReceiptId());
    } catch(Exception e){
      log.warn("error in TransactionalEventListener handleRT", e);
      throw e;
    }
  }

  @Bean("HandleRTTaskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Integer.parseInt(corePoolSize));
    executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
    executor.setQueueCapacity(Integer.parseInt(queueCapacity));
    executor.setThreadNamePrefix("MyPay4HandleRT-");
    executor.initialize();
    return executor;
  }
}

