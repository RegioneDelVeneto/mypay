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
package it.regioneveneto.mygov.payment.mypay4.service.common;

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.common.Giornale;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class GiornaleUUIDService {
  @EqualsAndHashCode
  public static class GiornaleUUID {

    private final Long uuid;
    public GiornaleUUID(){
      this.uuid = System.currentTimeMillis()*100000 + RandomUtils.nextInt(10000,99999);
    }

    @Override
    public String toString() {
      return uuid.toString();
    }
  }

  @Builder(toBuilder = true)
  @Data
  public static class GiornaleData{
    private GiornaleUUID giornaleUUID;
    private Giornale giornale;
    private String parametriSpecificiInterfaccia;
  }

  private static final ThreadLocal<GiornaleUUID> currentReqGiornaleUUID = new ThreadLocal<>();
  private static final ThreadLocal<GiornaleUUID> currentResGiornaleUUID = new ThreadLocal<>();
  private static final Map<GiornaleUUID, GiornaleData> mapGiornaleData = new HashMap<>();

  @PreDestroy
  public void clean(){
    currentReqGiornaleUUID.remove();
    currentResGiornaleUUID.remove();
  }

  public Optional<GiornaleUUID> getGiornaleUUID(Constants.GIORNALE_SOTTOTIPO_EVENTO type){
    GiornaleUUID uuid;
    if(type.equals(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ))
      uuid = currentReqGiornaleUUID.get();
    else if(type.equals(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES))
      uuid = currentResGiornaleUUID.get();
    else
      throw new MyPayException("invalid type");
    return Optional.ofNullable(uuid);
  }

  public void setGiornaleUUID(Constants.GIORNALE_SOTTOTIPO_EVENTO type, GiornaleUUID giornaleUUID){
    if(type.equals(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ))
      currentReqGiornaleUUID.set(giornaleUUID);
    else if(type.equals(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES))
      currentResGiornaleUUID.set(giornaleUUID);
    else
      throw new MyPayException("invalid type");
  }

  public GiornaleData getGiornaleData(GiornaleUUID giornaleUUID){
    GiornaleData giornaleData = mapGiornaleData.get(giornaleUUID);
    log.trace("getGiornaleData uuid[{}] giornaleData[{}]", giornaleUUID, giornaleData);
    return giornaleData;
  }
  public void setGiornaleData(GiornaleUUID giornaleUUID, GiornaleData giornaleData){
    log.trace("setGiornaleData uuid[{}] giornaleData[{}]", giornaleUUID, giornaleData);
    mapGiornaleData.put(giornaleUUID, giornaleData);
  }

  public void removeGiornaleDataAndUUID(GiornaleUUID giornaleUUID){
    mapGiornaleData.remove(giornaleUUID);
    String type=null;
    if(giornaleUUID.equals(currentReqGiornaleUUID.get())) {
      currentReqGiornaleUUID.remove();
      type=Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.name();
    }else if(giornaleUUID.equals(currentResGiornaleUUID.get())) {
      currentResGiornaleUUID.remove();
      type=Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.name();
    }
    log.trace("removeGiornaleDataAndUUID uuid[{}] type[{}]", giornaleUUID, type);
  }
}
