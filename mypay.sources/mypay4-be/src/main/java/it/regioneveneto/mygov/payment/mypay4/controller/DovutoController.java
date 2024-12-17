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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoOperatoreTo;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.DebtPositionDetail;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.GpdService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Tag(name = "Dovuti", description = "Gestione dei dovuti da pagare")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class DovutoController {

  static final String AUTHENTICATED_PATH ="dovuti";
  private static final String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  @Autowired
  private DovutoService dovutoService;
  @Autowired
  private EnteService enteService;
  @Autowired(required = false)
  private GpdService gpdService;

  private final Gson prettyPrintGson = new GsonBuilder().setPrettyPrinting().create();


  @GetMapping(AUTHENTICATED_PATH+"/search")
  public List<DovutoTo> searchDovuto(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @RequestParam LocalDate from, @RequestParam LocalDate to,
      @RequestParam(required = false) String codIpaEnte, @RequestParam(required = false) String codTipoDovuto,
      @RequestParam(required = false) String causale, @RequestParam(required = false) Boolean payableNoticeOnly){
    return dovutoService.searchDovuto(codIpaEnte, user.getCodiceFiscale(), codTipoDovuto, causale, from, to, BooleanUtils.isTrue(payableNoticeOnly), BooleanUtils.isTrue(payableNoticeOnly));
  }

  @GetMapping(AUTHENTICATED_PATH+"/last")
  public List<DovutoTo> searchLastDovuto(
      @AuthenticationPrincipal UserWithAdditionalInfo user,
      @RequestParam Integer num){
    if(num <1 || num > 10)
      throw new BadRequestException("num must be between 1 and 10");

    return dovutoService.searchLastDovuto(user.getCodiceFiscale(), num);
  }

  @PostMapping(AUTHENTICATED_PATH+"/remove/{mygovDovutoId}")
  public void removeDovutoForCittadino(
    @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovDovutoId) {
    dovutoService.removeDovutoForCittadino(user.getUsername(), mygovDovutoId);
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

  @PostMapping(OPERATORE_PATH+"/remove/{mygovEnteId}/{mygovDovutoId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public void removeDovutoForOperatore(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId, @PathVariable Long mygovDovutoId) {
    dovutoService.removeDovutoForOperatore(user.getUsername(), mygovEnteId, mygovDovutoId);
  }

  @GetMapping(OPERATORE_PATH+"/gpd/{paFiscalCode}/{iupd}")
  @Operatore(appAdmin = true)
  public String getIupdDetails(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable String paFiscalCode, @PathVariable String iupd) {
    if(gpdService!=null) {
      if(iupd.matches("\\d{18}")){
        try {
          //iupd is instead the notice number
          iupd = dovutoService.getByIuvEnte(Utilities.numeroAvvisoToIuvValidator(iupd), enteService.getEnteByCodFiscale(paFiscalCode).getCodIpaEnte()).stream().findFirst().map(Dovuto::getGpdIupd).orElseThrow();
          if(StringUtils.isBlank(iupd))
            return "IUPD is null for "+paFiscalCode+"/"+iupd;
        }catch(Exception e){
          log.warn("error retrieving IUPD for {}/{}", paFiscalCode, iupd, e);
          return "error retrieving IUPD for "+paFiscalCode+"/"+iupd;
        }
      }
      DebtPositionDetail debtPositionDetail = gpdService.getDebtPosition(paFiscalCode, iupd);
      return Optional.ofNullable(debtPositionDetail).map(prettyPrintGson::toJson)
          .orElse("no debt position found on GPD for "+paFiscalCode+"/"+iupd);
    } else {
      return "GPD not enabled";
    }
  }

  //example url: https://mypayHost:port/operatore/dovuti/gpd/iuvIupdHandling/<codFiscaleEnte>/<A|D>?dryRun=<true|false>&iuvIupd=01234567890123456:iupd1;01234567890123457:iupd2
  // use dryRun=true to just simulate action, without writing to DB
  @GetMapping(OPERATORE_PATH+"/gpd/iuvIupdHandling/{paFiscalCode}/{action}")
  @Operatore(appAdmin = true)
  public String handleIuvIupdAssociation(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                         @PathVariable String paFiscalCode, @PathVariable String action, @RequestParam String iuvIupd,
                                         @RequestParam(required = false) boolean dryRun){
    Set<String> iuvSet = new HashSet<>();
    if(gpdService!=null) {
      if(!action.equals("A") && !action.equals("D"))
        return "invalid action "+action;
      Ente ente = enteService.getEnteByCodFiscale(paFiscalCode);
      if(ente==null)
        return "Ente not found";
      StringBuilder resp = new StringBuilder("["+ DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now())+"] "+(dryRun?"DRY-RUN ":"")+"outcome:");
      for(String element:iuvIupd.split(";")){
        resp.append("\n<br>[").append(element).append("] ");
        String[] parts = element.split(":");
        if(parts.length<1 || parts.length>2) {
          resp.append("wrong element");
          continue;
        }
        String iuv = parts[0];
        String iupd = parts.length==1?null:parts[1];
        if(StringUtils.isBlank(iuv)){
          resp.append("missing IUV");
          continue;
        } else if(iuvSet.contains(iuv)){
          resp.append("already processed IUV: ").append(iuv);
          continue;
        }
        List<Dovuto> listDovuti = dovutoService.getByIuvEnte(iuv, ente.getCodIpaEnte());
        if(listDovuti.isEmpty()){
          resp.append("IUV not found: ").append(iuv);
          continue;
        } else if(listDovuti.size()>1){
          resp.append("IUV not unique: ").append(listDovuti.size());
          continue;
        }
        Dovuto dovuto = listDovuti.get(0);
        if(action.equals("A")){
          if(StringUtils.isBlank(iupd)){
            resp.append("IUPD to set is empty");
            continue;
          } else if(StringUtils.equals(dovuto.getGpdIupd(), iupd)){
            resp.append("IUPD already associated to IUV");
            continue;
          } else if(StringUtils.isNotBlank(dovuto.getGpdIupd())){
            resp.append("another IUPD already associated to IUV: ").append(dovuto.getGpdIupd());
            continue;
          }
        } else if(action.equals("D")){
          if(StringUtils.isNotBlank(iupd)){
            resp.append("IUPD to set is not empty");
            continue;
          } else if(StringUtils.isBlank(dovuto.getGpdIupd())){
            resp.append("IUPD already empty");
            continue;
          }
          iupd = null;
        }
        if(!dryRun)
          dovutoService.updateGpdIupd(dovuto.getMygovDovutoId(), iupd);
        iuvSet.add(iuv);
        resp.append("OK");
      }
      return resp.toString();
    } else {
      return "GPD not enabled";
    }
  }

}
