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
import it.regioneveneto.mygov.payment.mypay4.dto.CodeDescriptionTo;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteExtedendTo;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteTipoDovutoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteTo;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.logging.LogExecution;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteTipoDovutoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Tag(name = "Enti", description = "Gestione degli enti beneficiari")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class EnteController {

  private static final String AUTHENTICATED_PATH ="enti";
  private static final String ANONYMOUS_PATH= MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;
  private static final String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  @Autowired
  EnteService enteService;

  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;

  @LogExecution(enabled = LogExecution.ParamMode.OFF) //added just as a configuration example
  @GetMapping(ANONYMOUS_PATH)
  public List<EnteTo> getAllEnti(@RequestParam(required = false) String logoMode){
    List<Ente> listEnti = enteService.getAllEnti();
    Function<Ente, EnteTo> mapper = "hash".equals(logoMode) ? enteService::mapEnteToDtoWithThumbnailHash : enteService::mapEnteToDtoWithThumbnail;
    return listEnti.stream().map(mapper).collect(Collectors.toList());
  }

  @GetMapping(ANONYMOUS_PATH +"/{id}")
  public EnteTo getEnteById(@PathVariable Long id){
    return enteService.mapEnteToDtoWithBigLogo(enteService.getEnteById(id));
  }

  @GetMapping(ANONYMOUS_PATH +"/byCodIpa/{codIpa}")
  public EnteTo getEnteByCodIpa(@PathVariable String codIpa){
    return enteService.mapEnteToDtoWithBigLogo(enteService.getEnteByCodIpa(codIpa));
  }

  @GetMapping(AUTHENTICATED_PATH +"/byCodFiscale/{codFiscale}")
  public EnteTo getEnteByCodFiscale(@PathVariable String codFiscale){
    return enteService.mapEnteToDtoWithoutLogo(enteService.getEnteByCodFiscale(codFiscale));
  }

  @GetMapping(ANONYMOUS_PATH+"/{id}/tipiDovuto")
  public List<EnteTipoDovutoTo> getTipiDovutoByEnteId(@PathVariable Long id){
    List<EnteTipoDovuto> listEnteTipiDovuto = enteTipoDovutoService.getAttiviByMygovEnteIdAndFlags(id, null, null);
    return listEnteTipiDovuto.stream().map(enteTipoDovutoService::mapEnteTipoDovutoToDto).collect(Collectors.toList());
  }

  @GetMapping(ANONYMOUS_PATH+"/{codIpa}/tipiDovutoByCodIpa")
  public List<CodeDescriptionTo> getTipiDovutoByCodIpa(@PathVariable String codIpa){
    Ente ente = Optional.ofNullable(enteService.getEnteByCodIpa(codIpa)).orElseThrow(NotFoundException::new);
    List<EnteTipoDovuto> listEnteTipiDovuto = enteTipoDovutoService.getAttiviByMygovEnteIdAndFlags(ente.getMygovEnteId(), null, null);
    return listEnteTipiDovuto.stream().map(enteTipoDovutoService::mapEnteTipoDovutoToCodeDescriptionDto).collect(Collectors.toList());
  }

  @GetMapping(OPERATORE_PATH +"/{id}/tipiDovutoOperatore")
  @Operatore(value = "id")
  public List<EnteTipoDovutoTo> getByMygovEnteIdAndOperatoreUsername(
          @AuthenticationPrincipal UserWithAdditionalInfo user,
          @PathVariable Long id,
          @RequestParam(required = false) String enteSecondario
      ){
    List<EnteTipoDovuto> listEnteTipiDovuto =
      enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(id,user.getUsername());
    if(StringUtils.equalsIgnoreCase(enteSecondario, "true"))
      listEnteTipiDovuto = enteTipoDovutoService.addTipoDovutoExportEnteSecondario(id, listEnteTipiDovuto);
    return listEnteTipiDovuto.stream().map(enteTipoDovutoService::mapEnteTipoDovutoToDto).collect(Collectors.toList());
  }

  @GetMapping(OPERATORE_PATH)
  @Operatore
  public List<EnteTo> getEntiByOperatore(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestParam(required = false) String logoMode){
    List<Ente> listEnti = enteService.getEntiByOperatoreUsername(user.getUsername());
    Function<Ente, EnteTo> mapper = "hash".equals(logoMode) ? enteService::mapEnteToDtoWithThumbnailHash : enteService::mapEnteToDtoWithThumbnail;
    return listEnti.stream().map(mapper).collect(Collectors.toList());
  }

  @GetMapping(MyPay4AbstractSecurityConfig.PATH_APP_ADMIN+"/checklogoenti")
  @Operatore(appAdmin = true)
  public String checkLogoEnti(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestParam(required = false) boolean delete){
    return enteService.checkInvalidLogo(delete);
  }

  @GetMapping(OPERATORE_PATH+"/extended")
  @Operatore
  public List<EnteExtedendTo> getEnteExtendedByOperatore(@AuthenticationPrincipal UserWithAdditionalInfo user) {
	  List<Ente> listEnti = enteService.getEntiByOperatoreUsername(user.getUsername());
	  return listEnti.stream().map(enteService:: mapEnteExtendeToByEnte).collect(Collectors.toList());
  }
}
