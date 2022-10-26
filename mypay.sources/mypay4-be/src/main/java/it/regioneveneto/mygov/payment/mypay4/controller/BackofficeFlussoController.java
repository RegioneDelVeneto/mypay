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
import it.regioneveneto.mygov.payment.mypay4.service.TassonomiaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "BackofficeFlusso", description = "Backoffice Flusso")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class BackofficeFlussoController {

  private final static String AUTHENTICATED_PATH ="admin/flussi";
  private final static String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  public final static String FILE_TYPE_TASSONOMIA_IMPORT = "TASSONOMIA_IMPORT";
  public final static String FILE_TYPE_MASSIVA_IMPORT = "MASSIVA_IMPORT";

  @Autowired
  private TassonomiaService tassonomiaService;

  @Autowired
  private MyBoxController myBoxController;


  @GetMapping(OPERATORE_PATH+"/tassonomie/search")
  @Operatore(appAdmin = true)
  public List<FlussoTasMasTo> searchTassonomie(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                               @RequestParam(required = false) String nomeTassonomia,
                                               @RequestParam LocalDate dateFrom, @RequestParam LocalDate dateTo){
    List<FlussoTasMasTo> tassonomie = tassonomiaService.searchTassonomie(nomeTassonomia, dateFrom, dateTo);
    tassonomie.stream().forEach(tassonomia -> {
      tassonomia.setSecurityToken(myBoxController.generateSecurityToken(FILE_TYPE_TASSONOMIA_IMPORT, tassonomia.getPath(), user, 0L));
    });
    return tassonomie;
  }

}
