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

import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoOperatoreTo;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteTipoDovutoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Dovuti", description = "Gestione dei dovuti da pagare")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class DovutoController {

  private final static String AUTHENTICATED_PATH ="dovuti";
  private final static String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  @Autowired
  DovutoService dovutoService;
  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;


  @GetMapping(AUTHENTICATED_PATH+"/search")
  public List<DovutoTo> searchDovuto(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @RequestParam LocalDate from, @RequestParam LocalDate to,
      @RequestParam(required = false) String codIpaEnte, @RequestParam(required = false) String codTipoDovuto,
      @RequestParam(required = false) String causale){
    return dovutoService.searchDovuto(codIpaEnte, user.getCodiceFiscale(), codTipoDovuto, causale, from, to);
  }

  @GetMapping(AUTHENTICATED_PATH+"/last")
  public List<DovutoTo> searchLastDovuto(
      @AuthenticationPrincipal UserWithAdditionalInfo user,
      @RequestParam Integer num){
    if(num <1 || num > 10)
      throw new BadRequestException("num must be between 1 and 10");

    return dovutoService.searchLastDovuto(user.getCodiceFiscale(), num);
  }

  @GetMapping(OPERATORE_PATH+"/{mygovEnteId}/search")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public List<DovutoOperatoreTo> searchDovutoForOperatore(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
      @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam(required = false) String codStato,
      @RequestParam(required = false) Long myGovEnteTipoDovutoId, @RequestParam(required = false) String nomeFlusso,
      @RequestParam(required = false) String causale, @RequestParam(required = false) String codFiscale,
      @RequestParam(required = false) String iud, @RequestParam(required = false) String iuv){

    return dovutoService.searchDovutoForOperatore(user.getUsername(), mygovEnteId, from, to,
        codStato, myGovEnteTipoDovutoId, nomeFlusso, causale, codFiscale, iud, iuv);
  }

  @GetMapping(OPERATORE_PATH+"/{mygovEnteId}/{mygovDovutoId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public DovutoOperatoreTo detailDovutoForOperatore(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
      @PathVariable Long mygovDovutoId){

    return dovutoService.getDetailsForOperatore(user.getUsername(), mygovEnteId, mygovDovutoId);
  }

  @PostMapping(OPERATORE_PATH+"/{mygovEnteId}/{mygovDovutoId}/askRT")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public String askRt(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
      @PathVariable Long mygovDovutoId){

    return dovutoService.askRT(user.getUsername(), mygovEnteId, mygovDovutoId);
  }

  @PostMapping(OPERATORE_PATH+"/update/{mygovEnteId}/{mygovDovutoId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public DovutoOperatoreTo updateDovuto(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
      @PathVariable Long mygovDovutoId, @RequestBody DovutoOperatoreTo newDovuto) {
    DovutoOperatoreTo dovuto;
    try {
      dovuto = dovutoService.updateDovuto(user.getUsername(), mygovEnteId, mygovDovutoId, newDovuto);
    } catch(ValidatorException ex) {
      dovuto = DovutoOperatoreTo.builder().invalidDesc(ex.getMessage()).build();
    }
    return dovuto;
  }

  @PostMapping(OPERATORE_PATH+"/insert/{mygovEnteId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public DovutoOperatoreTo insertDovuto(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId, @RequestBody DovutoOperatoreTo newDovuto) {
    DovutoOperatoreTo dovuto;
    try {
      dovuto = dovutoService.insertDovuto(user.getUsername(), mygovEnteId, newDovuto);
    } catch(ValidatorException ex) {
      dovuto = DovutoOperatoreTo.builder().invalidDesc(ex.getMessage()).build();
    }
    return dovuto;
  }

  @GetMapping(OPERATORE_PATH+"/remove/{mygovEnteId}/{mygovDovutoId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public int removeDovuto(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId, @PathVariable Long mygovDovutoId) {
    try {
      return dovutoService.removeDovuto(user.getUsername(), mygovEnteId, mygovDovutoId);
    } catch(RuntimeException ex) {
      return 0;
    }
  }

}
