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
package it.regioneveneto.mygov.payment.mypay4.controller;

import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.ReceiptService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController("Esecuzione script di manutenzione")
@Slf4j
@ConditionalOnWebApplication
public class ScriptController {

  private static final String AUTHENTICATED_PATH ="admin/script";
  private static final String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  @Value("${fesp.mode}")
  private String fespMode;

  @Autowired
  private ReceiptService receiptPaService;

  @Autowired(required=false)
  private it.regioneveneto.mygov.payment.mypay4.service.fesp.ReceiptService receiptFespService;


  @GetMapping(OPERATORE_PATH +"/receipt/forceGenerateBytes/{mygovReceiptId}")
  @Operatore(appAdmin = true)
  public String forceGenerateBytes(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable List<Long> mygovReceiptId){
    return mygovReceiptId.stream()
      .map(id -> id+":"+receiptPaService.forceGenerateBytes(id))
      .collect(Collectors.joining(","));
  }
  @GetMapping(OPERATORE_PATH +"/receipt/forceGenerateBytes")
  @Operatore(appAdmin = true)
  public String forceGenerateBytes(@AuthenticationPrincipal UserWithAdditionalInfo user){
    return receiptPaService.getWithoutReceiptBytes().stream()
      .map(id -> receiptPaService.forceGenerateBytes(id))
      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).toString();
  }

  @GetMapping(OPERATORE_PATH +"/receipt/forceExport/{mygovReceiptId}")
  @Operatore(appAdmin = true)
  public String forceExportPaReceipt(
    @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable List<Long> mygovReceiptId){
    return mygovReceiptId.stream()
      .map(id -> id+":"+receiptPaService.resetExportStatusNew(id))
      .collect(Collectors.joining(","));
  }


  @GetMapping(OPERATORE_PATH +"/receipt/forceHandleFesp/{newStatus}/{mygovReceiptId}")
  @Operatore(appAdmin = true)
  public String forceHandleFespReceipt(
    @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable String newStatus,
    @PathVariable List<Long> mygovReceiptId){
    if(receiptFespService==null || !StringUtils.equals(fespMode,"local"))
      throw new UnsupportedOperationException();
    String status;
    try{
      status = Constants.RECEIPT_STATUS.valueOf(newStatus).name();
      if(!StringUtils.equals(status, Constants.RECEIPT_STATUS_FORCE) &&
         !StringUtils.equals(status, Constants.RECEIPT_STATUS_READY) )
        throw new ValidatorException("invalid new status");
    }catch(Exception e) {
      log.error("error on status validation", e);
      throw new ValidatorException("invalid value for status");
    }
    return mygovReceiptId.stream()
      .map(id -> id+":"+receiptFespService.updateStatus(id, status, true, false))
      .collect(Collectors.joining(","));
  }
}
