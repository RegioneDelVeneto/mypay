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
package it.regioneveneto.mygov.payment.mypay4.scheduled.exportdovuti;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.queue.QueueProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
@ConditionalOnProperty(name=AbstractApplication.NAME_KEY, havingValue=ExportDovutiTaskApplication.NAME)
public class ExportDovutiTask {

  @Value("${task.exportDovuti.concurrentTasksPerInstance:1}")
  private int concurrentTasksPerInstance;

  @Value("${task.exportDovuti.testEnqueue:false}")
  private boolean testEnqueue;

  @Value("${task.exportDovuti.fixedDelay}")
  private long fixedDelay;

  @Autowired
  private ThreadPoolTaskScheduler threadPoolTaskScheduler;

  @Autowired
  private ExportDovutiTaskService exportDovutiTaskService;

  @PostConstruct
  public void runAfterObjectCreated() {
    //check that concurrentTasksPerInstance is in interval 1:4
    if(concurrentTasksPerInstance < 1 || concurrentTasksPerInstance > 4){
      log.error("task.exportDovuti.concurrentTasksPerInstance must be in interval [1; 4], current value is {}",concurrentTasksPerInstance);
      throw new MyPayException("task.exportDovuti.concurrentTasksPerInstance must be in interval [1; 4], current value is "+concurrentTasksPerInstance);
    }

    //schedule jobs
    Instant startAt = Instant.now().plusMillis(RandomUtils.nextInt(3000,6000));
    for(int index=0; index < concurrentTasksPerInstance; index++) {
      startAt = startAt.plusMillis(RandomUtils.nextInt(2000,5000));
      final String indexString = "" + index;
      log.debug("starting ExportDovutiScheduled[{}] at time [{}]", indexString, startAt.toString());
      threadPoolTaskScheduler.scheduleWithFixedDelay(
          () -> exportDovutiTaskService.exportDovuti(indexString),
          startAt, Duration.ofMillis(fixedDelay));
    }

  }

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    int poolSize = this.concurrentTasksPerInstance + (this.testEnqueue ? 1 : 0);
    log.info("threadPoolTaskScheduler poolSize[{}]",poolSize);
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setThreadNamePrefix("MyPay4ExportDovuti");
    threadPoolTaskScheduler.setPoolSize(poolSize);
    return threadPoolTaskScheduler;
  }

  //only used for internal testing during development, to populate queue with random messages
  @Bean
  @ConditionalOnProperty(value = "task.exportDovuti.testEnqueue", havingValue = "true")
  public TestEnqueue testEnqueueScheduledJob() {
    return new TestEnqueue();
  }

}

@Slf4j
class TestEnqueue {

  @Autowired
  private QueueProducer queueProducer;

  public TestEnqueue() {
    log.warn("starting TestEnqueue - ENABLE IT ONLY ON DEV ENVIRONMENT");
  }

  @Scheduled(fixedDelayString = "1000", initialDelayString = "10000")
  public void testProduceQueue(){
    try{
      int toCreate = 0;
      File file = new File("/tmp/testExportDovuti.txt");
      if(file.exists() && file.isFile()){
        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        if(StringUtils.isNotBlank(content))
          try {
            toCreate = Integer.parseInt(content);
          }catch(Exception e){
            log.warn("cannot parse as number: {}", content, e);
            toCreate = 0;
          }
      }
      if(toCreate > 0){
        log.debug("[TEST] creating [{}] messages", toCreate);
        FileUtils.writeStringToFile(file, "0", StandardCharsets.UTF_8);
      }
      while(toCreate-->0) {
        Long mygovExportDovutoId = RandomUtils.nextLong(1000000, 10000000);
        String messageId = queueProducer.enqueueExportDovuti(mygovExportDovutoId);
        log.debug("[TEST] enqueued mygovExportDovutoId [{}] messageId[{}]", mygovExportDovutoId, messageId);
      }
    }catch(Exception e){
      log.warn("error on testProduceQueue", e);
    }
  }
}
