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

import it.regioneveneto.mygov.payment.mypay4.dto.fesp.InviaRptEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Slf4j
public class AsyncInviaRPTService {

  @Value("${async.inviaRPT.corePoolSize:5}")
  String corePoolSize;
  @Value("${async.inviaRPT.maxPoolSize:10}")
  String maxPoolSize;
  @Value("${async.inviaRPT.queueCapacity:500}")
  String queueCapacity;

  @Autowired
  private NodoInviaRPTService nodoInviaRPTService;

  @Async("InviaRPTTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void inviaRPT(InviaRptEvent inviaRptEvent){
    log.info("TransactionalEventListener inviaRPT for ente: {} - iuv: {} - ccp: {}", inviaRptEvent.getCodAttivarptIdentificativoDominio(),
        inviaRptEvent.getCodAttivarptIdentificativoUnivocoVersamento(), inviaRptEvent.getCodAttivarptCodiceContestoPagamento());
    try {
      nodoInviaRPTService.inviaRPTForModello3(inviaRptEvent.getCodAttivarptIdentificativoDominio(),
          inviaRptEvent.getCodAttivarptIdentificativoUnivocoVersamento(), inviaRptEvent.getCodAttivarptCodiceContestoPagamento());
    } catch(Exception e){
      log.warn("error in TransactionalEventListener inviaRPT", e);
      throw e;
    }
  }


  @Async("InviaRPTTaskExecutor")
  @Retryable(value = Exception.class, maxAttemptsExpression = "${async.inviaRPT.retry.maxAttempts:3}",
      backoff = @Backoff(random = true, delayExpression = "${async.inviaRPT.retry.delay:1000}",
          maxDelayExpression = "${async.inviaRPT.retry.maxDelay:5000}", multiplierExpression = "${async.inviaRPT.retry.multiplier:2}"))
  public CompletableFuture<Void> scheduleInviaRPT(String codAttivarptIdentificativoDominio,
                               String codAttivarptIdentificativoUnivocoVersamento,
                               String codAttivarptCodiceContestoPagamento){
    log.info("async inviaRPT for ente: {} - iuv: {} - ccp: {}", codAttivarptIdentificativoDominio,
        codAttivarptIdentificativoUnivocoVersamento, codAttivarptCodiceContestoPagamento);
    try {
      nodoInviaRPTService.inviaRPTForModello3(codAttivarptIdentificativoDominio,
          codAttivarptIdentificativoUnivocoVersamento, codAttivarptCodiceContestoPagamento);
      return CompletableFuture.completedFuture(null);
    } catch(Exception e){
      log.warn("error in scheduleInviaRPT, using retry policy", e);
      throw e;
    }
  }

  @Recover
  private CompletableFuture<Void> recoverInviaRPT(Exception e, String codAttivarptIdentificativoDominio,
                               String codAttivarptIdentificativoUnivocoVersamento,
                               String codAttivarptCodiceContestoPagamento){
    String id = String.format("ente: %s - iuv: %s - ccp: %s", codAttivarptIdentificativoDominio,
        codAttivarptIdentificativoUnivocoVersamento, codAttivarptCodiceContestoPagamento);
    log.warn("cannot perform inviaRPT for "+ id+". Aborting", e);
    //TODO write fail to db or queue for retry, in case
    return CompletableFuture.completedFuture(null);
  }

  @Bean("InviaRPTTaskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Integer.parseInt(corePoolSize));
    executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
    executor.setQueueCapacity(Integer.parseInt(queueCapacity));
    executor.setThreadNamePrefix("MyPay4InviaRPT-");
    executor.initialize();
    return executor;
  }
}

