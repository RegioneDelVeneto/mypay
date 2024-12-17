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
import it.regioneveneto.mygov.payment.mypay4.service.ExportDovutiService;
import it.regioneveneto.mygov.payment.mypay4.service.common.AppErrorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSConstants;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.*;

@Component
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue=ExportDovutiTaskApplication.NAME)
public class ExportDovutiTaskService {

  @Resource
  private ExportDovutiTaskService self;

  @Autowired
  private ExportDovutiService exportDovutiService;

  @Value("${spring.artemis.host}")
  private String mqHost;

  @Value("${spring.artemis.port}")
  private String mqPort;

  @Value("${spring.artemis.user}")
  private String mqUsername;
  @Value("${spring.artemis.password}")
  private String mqPassword;

  @Value("${queue.export-dovuti}")
  private String mqQueue;

  @Value("${task.exportDovuti.maxConnectionDurationMinues:15}")
  private long maxConnectionDurtionMinutes;

  @Value("${task.exportDovuti.maxWaitMessageSeconds:5}")
  private long maxWaitMessageSeconds;

  @Value("${task.exportDovuti.minHeartbeatLogSeconds:120}")
  private long minHeartbeatLogSeconds;

  @Autowired
  private AppErrorService appErrorService;

  private String instanceId;

  private ThreadLocal<Long> instanceCounter = ThreadLocal.withInitial(() -> new Long(0));

  @PostConstruct
  public void runAfterObjectCreated() {
    //this transformation will specially fit kubernetes environment: it will take the "random" part at end of pod name
    instanceId = "exportDovuti-"+appErrorService.getServerId().orElse(""+RandomUtils.nextLong(100000000, 1000000000));
  }

  @Transactional(propagation = Propagation.NEVER)
  public void exportDovuti(String taskId) {
    String taskName = instanceId+"-"+taskId;
    try{
      try(ActiveMQConnectionFactory connectionFactory = ActiveMQJMSClient.createConnectionFactory("tcp://" + mqHost + ":" + mqPort, taskName)){
        connectionFactory.setClientID(taskName);
        connectionFactory.setConsumerWindowSize(0);
        try(Connection connection = connectionFactory.createConnection(mqUsername, mqPassword)) {
          try (Session session = connection.createSession(false, ActiveMQJMSConstants.INDIVIDUAL_ACKNOWLEDGE)) {
            Destination destination = session.createQueue(mqQueue);
            try (MessageConsumer messageConsumer = session.createConsumer(destination)) {
              Long startConnectionMillis = System.currentTimeMillis();
              connection.start();
              instanceCounter.set(instanceCounter.get()+1);
              log.info("start jms connection taskName[{}] counter[{}], will be closed in ~ {} minutes", taskName, instanceCounter.get(), maxConnectionDurtionMinutes);
              int processedMessages = 0;
              long lastLogMessage = System.currentTimeMillis();
              while ((System.currentTimeMillis() - startConnectionMillis) < maxConnectionDurtionMinutes * 60 * 1000) {
                Message message = messageConsumer.receive(maxWaitMessageSeconds * 1000);
                if (message != null) {
                  processedMessages++;
                  Long mygovExportDovutoId = Long.parseLong(((TextMessage) message).getText());

                  boolean doAck = false;
                  try {
                    log.debug("processing message jmsId[{}] mygovExportDovutoId[{}]", message.getJMSMessageID(), mygovExportDovutoId);
                    exportDovutiService.exportDovuti(mygovExportDovutoId);
                    doAck = true;
                  } catch (Exception e) {
                    log.error("exception doing exportDovuti for id[{}]. Error message: [{}]", mygovExportDovutoId, e.getMessage());
                    doAck = true;
                  } finally {
                    if(doAck)
                      message.acknowledge();
                  }
                } else {
                  log.trace("no message found in {} seconds", maxWaitMessageSeconds);
                }
                if (System.currentTimeMillis() - lastLogMessage > minHeartbeatLogSeconds * 1000) {
                  lastLogMessage = System.currentTimeMillis();
                  log.debug("connection taskName[{}] counter[{}] will be closed in ~ {} seconds", taskName, instanceCounter.get(),
                      maxConnectionDurtionMinutes * 60 - (System.currentTimeMillis() - startConnectionMillis) / 1000);
                }
              }
              log.info("closing jms connection, processed messages: {}", processedMessages);
            }
          }
        }
      }
    } catch(Exception e){
      log.error("unexpected system error executing exportDovuti task", e);
    }

  }

}