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
package it.regioneveneto.mygov.payment.mypay4.config.dbreplica;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

@Slf4j
public class ReplicaAwareTransactionManager implements PlatformTransactionManager {

  private final DataSourceTransactionManager wrapped;
  private final String dsId;

  public ReplicaAwareTransactionManager(DataSourceTransactionManager wrappedTransactionManager) {
    wrapped = wrappedTransactionManager;
    DataSource ds = wrapped.getDataSource();
    if(ds instanceof TransactionRoutingDataSource)
      dsId = ((TransactionRoutingDataSource) ds).getId();
    else
      dsId = null;
  }

  @Override
  public @NotNull TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
    TransactionRoutingDataSource.setReadonlyDataSource(dsId,definition != null && definition.isReadOnly());
    TransactionStatus transactionStatus = wrapped.getTransaction(definition);

    if(log.isDebugEnabled()) {
      String currTransRO = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "RO" : "RW";
      String definitionRO = definition != null && definition.isReadOnly() ? "RO" : "RW";
      final String definitionProp;
      switch (definition.getPropagationBehavior()) {
        case TransactionDefinition.PROPAGATION_REQUIRED:
          definitionProp = "REQUIRED";
          break;
        case TransactionDefinition.PROPAGATION_SUPPORTS:
          definitionProp = "SUPPORTS";
          break;
        case TransactionDefinition.PROPAGATION_MANDATORY:
          definitionProp = "MANDATORY";
          break;
        case TransactionDefinition.PROPAGATION_REQUIRES_NEW:
          definitionProp = "REQUIRES_NEW";
          break;
        case TransactionDefinition.PROPAGATION_NOT_SUPPORTED:
          definitionProp = "NOT_SUPPORTED";
          break;
        case TransactionDefinition.PROPAGATION_NEVER:
          definitionProp = "NEVER";
          break;
        case TransactionDefinition.PROPAGATION_NESTED:
          definitionProp = "NESTED";
          break;
        default:
          definitionProp = "unknown-" + definition.getPropagationBehavior();
      }
      if (transactionStatus.isNewTransaction())
        log.debug("New ReplicaAwareTransaction for ds[{}] RO(curr/def)[{}/{}] propag[{}]", dsId, currTransRO, definitionRO, definitionProp, new Exception("Created at"));
      else
        log.debug("Reuse ReplicaAwareTransaction for ds[{}] RO(curr/def)[{}/{}] propag[{}]", dsId, currTransRO, definitionRO, definitionProp);
    }

    return transactionStatus;
  }

  @Override
  public void commit(@NotNull TransactionStatus status) throws TransactionException {
    wrapped.commit(status);
  }

  @Override
  public void rollback(@NotNull TransactionStatus status) throws TransactionException {
    wrapped.rollback(status);
  }
}
