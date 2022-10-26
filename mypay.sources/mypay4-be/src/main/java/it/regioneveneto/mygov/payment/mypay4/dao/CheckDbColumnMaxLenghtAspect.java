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
package it.regioneveneto.mygov.payment.mypay4.dao;

import it.regioneveneto.mygov.payment.mypay4.dao.catalog.CatalogDao;
import it.regioneveneto.mygov.payment.mypay4.dto.catalog.ColumnInfo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotAuthorizedException;
import it.regioneveneto.mygov.payment.mypay4.model.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
@Slf4j
@ConditionalOnProperty(prefix = "mypay", name = "checkDbMaxLength.enabled", havingValue = "true")
public class CheckDbColumnMaxLenghtAspect {

  @Value("${pa.api-operatore.enabled:true}")
  private String apiOperatoreEnabled;

  @Autowired
  @Qualifier("paCatalogDao")
  private CatalogDao paCatalogDao;

  @Autowired
  @Qualifier("fespCatalogDao")
  private CatalogDao fespCatalogDao;

  private Map<String, Integer> columnData;

  @EventListener
  private void initMaxLengthData(ApplicationReadyEvent event){
    log.info("loading DB catalog info");
    this.columnData = Stream.concat(
          paCatalogDao.loadMaxLengthData().stream().peek(columnInfo -> columnInfo.setDb("pa")),
          fespCatalogDao.loadMaxLengthData().stream().peek(columnInfo -> columnInfo.setDb("fesp")))
        .collect(Collectors.toUnmodifiableMap(
            columnInfo -> (columnInfo.getDb()+"|"+columnInfo.getTableName()+"|"+columnInfo.getColumnName()).toLowerCase(),
            ColumnInfo::getCharacterMaximumLength
        ));
    log.debug("columnData: {}",Arrays.asList(columnData));
  }

  @Around("@annotation(org.jdbi.v3.sqlobject.statement.SqlUpdate)")
  public Object checkDbColumnMaxLenght(ProceedingJoinPoint joinPoint) throws Throwable {
    log.debug("checking maxDbLength on sql update, operation: "+joinPoint.getSignature());

    if(!StringUtils.equalsIgnoreCase(this.apiOperatoreEnabled,"true"))
      throw new NotAuthorizedException("API operatore disabilitate");

    //retrieve parameter containing codIpa
    Arrays.stream(joinPoint.getArgs())
        .filter(obj -> obj!=null
            && BaseEntity.class.isAssignableFrom(obj.getClass()))
        .forEach(obj -> {
          try {
            String fields = (String) obj.getClass().getDeclaredField("FIELDS").get(null);
            String table = (String) obj.getClass().getDeclaredField("TABLE").get(null);
            String module = obj.getClass().getPackageName().indexOf(".fesp.")!=-1 ? "fesp" : "pa";
            if(table.indexOf(".")!=-1){
              module = table.substring(0, table.indexOf("."));
              table = table.substring(table.indexOf(".")+1);
            }
            String finalTable = table;
            String finalModule = module;
            if(StringUtils.isNotBlank(table) && StringUtils.isNotBlank(fields))
              Arrays.stream(fields.split(","))
                .map(aField -> aField.substring(aField.indexOf('.')+1, aField.indexOf(' ')))
                .forEach(aField -> {
                  try {
                    String key = StringUtils.joinWith("|",finalModule, finalTable, aField);
                    Integer maxLength = columnData.get(key);
                    if(maxLength!=null) {
                      log.trace("key:{} - maxhLength: {}", key, maxLength);
                      Object value = BeanUtils.getProperty(obj, CaseUtils.toCamelCase(aField, false, '_'));
                      if (value instanceof CharSequence && StringUtils.length((CharSequence) value) > maxLength)
                        throw new BadRequestException("invalid value for: " + key + " - too many chars");
                    }
                  } catch (BadRequestException bre) {
                    throw bre;
                  } catch (Exception e) {
                    log.warn("ignored exception", e);
                  }
                });
          } catch (BadRequestException bre) {
            throw bre;
          } catch(Exception e){
            log.warn("ignored exception", e);
          }
        });

    return joinPoint.proceed();
  }


}
