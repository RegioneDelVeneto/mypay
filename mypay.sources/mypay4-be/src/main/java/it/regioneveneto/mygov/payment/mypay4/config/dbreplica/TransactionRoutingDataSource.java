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

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TransactionRoutingDataSource extends AbstractRoutingDataSource {

  private String id;
  private static final ThreadLocal<Map<String,DataSourceType>> currentDataSourcesById = ThreadLocal.withInitial(() -> new HashMap<>());
  private static final ThreadLocal<String> currentDsId = new ThreadLocal<>();

  public TransactionRoutingDataSource(String id, DataSource master, DataSource slave) {
    this.id = id;
    Map<Object, Object> dataSources = new HashMap<>();
    dataSources.put(id+'|'+DataSourceType.READ_WRITE, master);
    dataSources.put(id+'|'+DataSourceType.READ_ONLY, slave);

    super.setTargetDataSources(dataSources);
    super.setDefaultTargetDataSource(master);
  }

  public String getId(){
    return id;
  }

  static void setReadonlyDataSource(String dsId, boolean isReadonly) {
    if(dsId==null) {
      currentDsId.remove();
    } else {
      currentDataSourcesById.get().put(dsId, isReadonly ? DataSourceType.READ_ONLY : DataSourceType.READ_WRITE);
      currentDsId.set(dsId);
    }
  }

  public static void unload(String dsId) {
    currentDataSourcesById.get().remove(dsId);
  }

  public static boolean isCurrentlyReadonly(String dsId){
    return dsId!=null && Objects.equals(currentDataSourcesById.get().get(dsId), DataSourceType.READ_ONLY );
  }

  public static boolean isCurrentlyReadonly(){
    return isCurrentlyReadonly(currentDsId.get());
  }

  @Override
  protected Object determineCurrentLookupKey() {
    return currentDataSourcesById.get().get(this.id);
  }

  private enum DataSourceType {
    READ_ONLY,
    READ_WRITE;
  }
}
