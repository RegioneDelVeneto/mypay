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

import com.zaxxer.hikari.HikariDataSource;
import it.regioneveneto.mygov.payment.mypay4.config.dbreplica.ReplicaAwareTransactionManager;
import it.regioneveneto.mygov.payment.mypay4.config.dbreplica.TransactionRoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@Slf4j
public class DataSourceConfiguration {

  @Value("${spring.datasource.pa.minimumIdle:-1}")
  private int dataSourcePaReadWriteMinimumIdle;
  @Value("${spring.datasource.pa.maximumPoolSize:-1}")
  private int dataSourcePaReadWriteMaximumPoolSize;

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource.pa")
  public DataSourceProperties paDataSourceProperties() {
    return new DataSourceProperties();
  }
  @Bean(name="dsPaReadWrite")
  public DataSource paDataSource() {
    HikariDataSource ds = paDataSourceProperties().initializeDataSourceBuilder()
        .type(HikariDataSource.class).build();
    ds.setMinimumIdle(dataSourcePaReadWriteMinimumIdle);
    ds.setMaximumPoolSize(dataSourcePaReadWriteMaximumPoolSize);
    ds.setAutoCommit(false);
    log.info("creating data source [pa] with minimumIdle:"+ds.getMinimumIdle()+" maximumPoolSize:"+ds.getMaximumPoolSize());
    return ds;
  }

  @Value("${spring.datasource.pa.ro.enabled:false}")
  private boolean dataSourcePaReadOnlyEnabled;
  @Value("${spring.datasource.pa.ro.minimumIdle:-1}")
  private int dataSourcePaReadOnlyMinimumIdle;
  @Value("${spring.datasource.pa.ro.maximumPoolSize:-1}")
  private int dataSourcePaReadOnlyMaximumPoolSize;
  @Bean
  @ConfigurationProperties("spring.datasource.pa.ro")
  public DataSourceProperties paReadOnlyDataSourceProperties() {
    return new DataSourceProperties();
  }
  @Bean(name="dsPaReadOnly")
  @ConditionalOnProperty(prefix = "spring.datasource.pa.ro", name = "enabled", havingValue = "true")
  public DataSource paReadOnlyDataSource() {
    HikariDataSource ds = paReadOnlyDataSourceProperties().initializeDataSourceBuilder()
      .type(HikariDataSource.class).build();
    ds.setMinimumIdle(dataSourcePaReadOnlyMinimumIdle);
    ds.setMaximumPoolSize(dataSourcePaReadOnlyMaximumPoolSize);
    ds.setAutoCommit(false);
    log.info("creating data source [paReadOnly] with minimumIdle:"+ds.getMinimumIdle()+" maximumPoolSize:"+ds.getMaximumPoolSize());
    return ds;
  }

  @Bean(name="dsPa")
  @Primary
  public DataSource routingDataSource(@Qualifier("dsPaReadWrite") DataSource datasourceReadWrite,
                                      @Qualifier("dsPaReadOnly") Optional<DataSource> datasourceReadOnly) {
    if(dataSourcePaReadOnlyEnabled)
      return new TransactionRoutingDataSource("pa", datasourceReadWrite, datasourceReadOnly.get());
    else
      return datasourceReadWrite;
  }

  @Value("${spring.datasource.fesp.minimumIdle:-1}")
  private int dataSourceFespMinimumIdle;
  @Value("${spring.datasource.fesp.maximumPoolSize:-1}")
  private int dataSourceFespMaximumPoolSize;

  @Bean
  @ConfigurationProperties("spring.datasource.fesp")
  public DataSourceProperties fespDataSourceProperties() {
    return new DataSourceProperties();
  }
  @Bean(name="dsFesp")
  @ConfigurationProperties("spring.datasource.fesp.configuration")
  public DataSource fespDataSource() {
    HikariDataSource ds = fespDataSourceProperties().initializeDataSourceBuilder()
        .type(HikariDataSource.class).build();
    ds.setMinimumIdle(dataSourceFespMinimumIdle);
    ds.setMaximumPoolSize(dataSourceFespMaximumPoolSize);
    ds.setAutoCommit(false);
    log.info("creating data source [fesp] with minimumIdle:"+ds.getMinimumIdle()+" maximumPoolSize:"+ds.getMaximumPoolSize());
    return ds;
  }

  @Bean(name="tmPa")
  @Autowired
  @Primary
  PlatformTransactionManager paWrappedTransactionManager(@Qualifier("tmPaInternal") DataSourceTransactionManager wrapped) {
    if(dataSourcePaReadOnlyEnabled)
      return new ReplicaAwareTransactionManager(wrapped);
    else
      return wrapped;
  }

  @Bean(name="tmPaInternal")
  @Autowired
  DataSourceTransactionManager paTransactionManager(@Qualifier ("dsPa") DataSource datasource) {
    return new DataSourceTransactionManager(datasource);
  }

  @Bean(name="tmFesp")
  @Autowired
  DataSourceTransactionManager fespTransactionManager(@Qualifier ("dsFesp") DataSource datasource) {
    return new DataSourceTransactionManager(datasource);
  }
}
