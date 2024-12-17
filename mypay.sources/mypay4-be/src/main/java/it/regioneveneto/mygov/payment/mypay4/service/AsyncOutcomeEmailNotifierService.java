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

import it.regioneveneto.mygov.payment.mypay4.dto.OutcomeEmailNotifierEvent;
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
public class AsyncOutcomeEmailNotifierService {

	@Value("${async.outcomeEmailNotifier.corePoolSize:5}")
	String corePoolSize;
	@Value("${async.outcomeEmailNotifier.maxPoolSize:10}")
	String maxPoolSize;
	@Value("${async.outcomeEmailNotifier.queueCapacity:500}")
	String queueCapacity;

	@Autowired
	EsitoService esitoService;

	@Async("OutcomeEmailNotifierExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void notify(OutcomeEmailNotifierEvent event){
		log.info("TransactionalEventListener OutcomeEmailNotifier for MygovCarrelloId: {}", event.getMygovCarrelloId());
		try {
			esitoService.handleOutcomeEmailNotifier(event.getMygovCarrelloId());
		} catch(Exception e){
			log.warn("error in TransactionalEventListener OutcomeEmailNotifier", e);
			throw e;
		}
	}

	@Bean("OutcomeEmailNotifierExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(Integer.parseInt(corePoolSize));
		executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
		executor.setQueueCapacity(Integer.parseInt(queueCapacity));
		executor.setThreadNamePrefix("MyPay4OutcomeEmailNotifier-");
		executor.setRejectedExecutionHandler((r, exec1) -> log.warn("Task rejected, thread pool is full and queue is also full"));
		executor.initialize();
		return executor;
	}
}
