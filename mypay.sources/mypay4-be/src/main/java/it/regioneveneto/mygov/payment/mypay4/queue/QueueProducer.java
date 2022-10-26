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
package it.regioneveneto.mygov.payment.mypay4.queue;

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
@EnableJms
@Slf4j
public class QueueProducer {
  @Autowired
  JmsTemplate jmsTemplate;

  @Value("${queue.import-dovuti}")
  private String importDovutiQueue;
  @Value("${queue.export-dovuti}")
  private String exportDovutiQueue;

  @Value("${app.jms.verify-send:false}")
  private String verifySend;

  @Transactional
  @LogExecution(params = LogExecution.ParamMode.ON)
  public String enqueueImportDovuti(String path){
    return this.enqueueImpl(importDovutiQueue, path);
  }

  @Transactional
  @LogExecution(params = LogExecution.ParamMode.ON)
  public String enqueueExportDovuti(Long importDovutiId){
    return this.enqueueImpl(exportDovutiQueue, Long.toString(importDovutiId));
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public String enqueueTest(String msg){
    return this.enqueueImpl("TEST_QUEUE", UUID.randomUUID().toString()+" - msg: "+ msg);
  }

  private String enqueueImpl(String queueName, String msg){
    final AtomicReference<Message> message = new AtomicReference<>();
    String messageId;

    jmsTemplate.convertAndSend(queueName, msg, m -> {
      message.set(m);
      return m;
    });

    try {
      messageId = message.get().getJMSMessageID();
// commented out because it doesn't work in any case using transacted jms template
//      if(Boolean.parseBoolean(verifySend)) {
//        Boolean found = jmsTemplate.browseSelected(queueName, "JMSMessageID='" + messageId + "'",
//            (session, qb) -> qb.getEnumeration().hasMoreElements());
//        if (found == null || !found){
//          //throw new MyPayException("cannot send JMS message on queue " + queueName);
//        }
//        log.debug("found:" + messageId+" :"+found);
//        Integer size = jmsTemplate.browse(queueName,(session, qb) -> Collections.list(qb.getEnumeration()).size());
//        log.debug("size:"+size);
//      }
    } catch (JMSException e) {
      log.warn("cannot retrieve JSM message id", e);
      throw new MyPayException("cannot send JMS message on queue "+queueName, e);
    }
    log.info("JMS message sent, queue name:{} - msgId:{}", queueName, messageId);

    //TODO remove after tests
    if(msg.endsWith("hello"))
      throw new MyPayException("hello");
    return messageId;
  }
}
