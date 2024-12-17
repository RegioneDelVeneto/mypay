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
import it.regioneveneto.mygov.payment.mypay4.dto.FlussoTasMasTo;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.FlussoTassonomiaService;
import it.regioneveneto.mygov.payment.mypay4.service.ImportTaxonomyHandlerService;
import it.regioneveneto.mygov.payment.mypay4.service.StorageService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "BackofficeTassonomie", description = "Backoffice Tassonomie")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class BackofficeTassonomieController {

  private static final String AUTHENTICATED_PATH ="admin/taxonomy";
  private static final String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;
  public static final String FILE_TYPE_TASSONOMIA_IMPORT = "TASSONOMIA_IMPORT";
  
  @Autowired
  FlussoTassonomiaService flussoTassonomiaService;

  @Autowired
  MyBoxController myBoxController;

  @Autowired
  ImportTaxonomyHandlerService importTaxonomyHandlerService;

  @Autowired
  StorageService storageService;

  @GetMapping(OPERATORE_PATH+"/search")
  @Operatore(appAdmin = true)
  public List<FlussoTasMasTo> searchTassonomie(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                               @RequestParam(required = false) String nomeTassonomia,
                                               @RequestParam LocalDate dateFrom, @RequestParam LocalDate dateTo){
    return flussoTassonomiaService.searchTassonomie(nomeTassonomia, dateFrom, dateTo)
      .stream()
      .peek(item -> item.setSecurityToken(myBoxController.generateSecurityToken(FILE_TYPE_TASSONOMIA_IMPORT, item.getPath(), user, 0L)))
      .collect(Collectors.toList());
  }

  @SneakyThrows
  @PostMapping(OPERATORE_PATH+"/update")
  @Operatore(appAdmin = true)
  public String updateTaxonomy(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestParam(required = false) String hash){
    var apiData = importTaxonomyHandlerService.checkForNewData(hash);
    if (apiData.isPresent()) {
      importTaxonomyHandlerService.performUpdate(user.getCodiceFiscale(), apiData.get());
      return Constants.STATO_ESITO_OK;
    }
    return Constants.STATO_ESITO_KO;
  }
}
