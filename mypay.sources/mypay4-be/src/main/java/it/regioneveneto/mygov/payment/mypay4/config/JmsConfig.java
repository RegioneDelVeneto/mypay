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
package it.regioneveneto.mygov.payment.mypay4.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

@Configuration
@Slf4j
@EnableJms
public class JmsConfig {
  @Bean
  public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory)
  {
    JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
    jmsTemplate.setSessionTransacted(true);
    //((CachingConnectionFactory)connectionFactory).setCacheProducers(false);
    log.info("Creating TRANSACTION ENABLED jms template ["+connectionFactory.getClass().getName()+"]");
    return jmsTemplate;
  }

//  @Bean
//  public ConnectionFactoryBeanProcessor connectionFactoryBeanProcessor(){
//    return new ConnectionFactoryBeanProcessor();
//  }

//  @Bean
//  public PlatformTransactionManager transactionManager(ConnectionFactory connectionFactory) {
//    return new JmsTransactionManager(connectionFactory);
//  }

}


//@Slf4j
//class ConnectionFactoryBeanProcessor implements BeanPostProcessor {
//  @Override
//  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//    if (bean instanceof ActiveMQConnectionFactory) {
//      log.info("processing bean name[{}] class[{}]", beanName, bean.getClass().getName());
//      ActiveMQConnectionFactory activeMQConnectionFactory = (ActiveMQConnectionFactory) bean;
//      if (activeMQConnectionFactory.getConsumerWindowSize() != 0) {
//        log.info("setting ConsumerWindowSize to 0 (see https://issues.apache.org/jira/browse/ARTEMIS-2417)");
//        try{
//          activeMQConnectionFactory.setConsumerWindowSize(0);
//        }catch(Exception e){
//          log.error("error setting ConsumerWindowSize to 0; ignoring it", e);
//        }
//        log.info("Connection factory [{}]", activeMQConnectionFactory);
//      }
//    }
//    return bean;
//  }
//}