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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SystemBlockService {

  @Value("${block.whitelist.payercf:}")
  private List<String> whitelistPayerCf;
  @Value("${block.blacklist.payercf:}")
  private List<String> blacklistPayerCf;

  @Value("${block.whitelist.operation:}")
  private List<String> whitelistOperation;

  @Value("${block.blacklist.operation:}")
  private List<String> blacklistOperation;

  @Value("${block.errorMessage:}")
  private String errorMessage;

  private boolean byOperationEnabled;
  private boolean byPayerCfEnabled;

  @PostConstruct
  public void runAfterObjectCreated() {
    errorMessage = StringUtils.firstNonBlank(errorMessage, "L'operazione richiesta non è al momento disponibile");

    whitelistPayerCf = whitelistPayerCf
      .stream()
      .map(String::toLowerCase)
      .collect(Collectors.toUnmodifiableList());
    blacklistPayerCf = blacklistPayerCf
      .stream()
      .map(String::toLowerCase)
      .collect(Collectors.toUnmodifiableList());
    byPayerCfEnabled = !whitelistPayerCf.isEmpty() || !blacklistPayerCf.isEmpty();
    if(byPayerCfEnabled)
      log.error("WARNING: blockByPayerCF active - blacklist[{}] whitelist[{}]",
        blacklistPayerCf.stream().collect(Collectors.joining(";")),
        whitelistPayerCf.stream().collect(Collectors.joining(";")));

    whitelistOperation = whitelistOperation
      .stream()
      .map(String::strip)
      .collect(Collectors.toUnmodifiableList());
    blacklistOperation = blacklistOperation
      .stream()
      .map(String::strip)
      .collect(Collectors.toUnmodifiableList());
    byOperationEnabled = !whitelistOperation.isEmpty() || !blacklistOperation.isEmpty();
    if(byOperationEnabled)
      log.error("WARNING: blockByOperation active - blacklist[{}] whitelist[{}]",
        blacklistOperation.stream().collect(Collectors.joining(";")),
        whitelistOperation.stream().collect(Collectors.joining(";")));
  }

  public String getErrorMessage(){
    return errorMessage;
  }

  public boolean checkBlockByOperationName(String operationName){
    return byOperationEnabled &&
      this.checkBlockByBlackAndWhitelist(operationName, "operationName", blacklistOperation, whitelistOperation);
  }

  public void blockByOperationName(String operationName){
    if(checkBlockByOperationName(operationName)){
      throw new UnsupportedOperationException(errorMessage);
    }
  }

  public boolean checkBlockByPayerCf(String payerCf){
    return byPayerCfEnabled &&
      this.checkBlockByBlackAndWhitelist(StringUtils.lowerCase(payerCf), "payerCf", blacklistPayerCf, whitelistPayerCf);
  }

  public void blockByPayerCf(String payerCf){
    if(checkBlockByPayerCf(payerCf)){
      throw new UnsupportedOperationException(errorMessage);
    }
  }

  private boolean checkBlockByBlackAndWhitelist(String val, String listName, List<String> blacklist, List<String> whitelist){
    if(StringUtils.isBlank(val))
      return false;
    if(blacklist.contains(val)){
      log.warn("blocked {}[{}] because in blacklist", listName, val);
      return true;
    } else if(!whitelist.isEmpty() && !whitelist.contains(val)){
      log.warn("blocked {}[{}] because not in whitelist", listName, val);
      return true;
    } else {
      return false;
    }
  }
}
