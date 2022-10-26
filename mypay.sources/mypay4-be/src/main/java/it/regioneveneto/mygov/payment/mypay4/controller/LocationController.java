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

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.ComuneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.NazioneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.ProvinciaTo;
import it.regioneveneto.mygov.payment.mypay4.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Località", description = "Gestione delle località (nazioni / province / comuni)")
@SecurityRequirements
@RestController
@RequestMapping(MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/location")
@Slf4j
@ConditionalOnWebApplication
public class LocationController {

  @Autowired
  LocationService locationService;

  private final NazioneTo EMPTY_NAZIONE = NazioneTo.builder().nazioneId(0L).nomeNazione(null).codiceIsoAlpha2(null).build();
  private final ProvinciaTo EMPTY_PROVINCIA = ProvinciaTo.builder().provinciaId(0L).provincia(null).sigla(null).build();
  private final ComuneTo EMPTY_COMUNE = ComuneTo.builder().comuneId(0L).comune(null).provinciaId(null).build();

  @GetMapping("nazioni")
  public List<NazioneTo> getNazioni(){
    List<NazioneTo>  nazioni = locationService.getNazioni();
    nazioni.add(0, EMPTY_NAZIONE);
    return nazioni;
  }

  @GetMapping("province")
  public List<ProvinciaTo> getProvince(){
    List<ProvinciaTo> province = locationService.getProvince();
    province.add(0, EMPTY_PROVINCIA);
    return province;
  }

  @GetMapping("comuni/{provinciaId}")
  public List<ComuneTo> getComuni(@PathVariable Long provinciaId){
    List<ComuneTo> comuni = locationService.getComuniByProvincia(provinciaId);
    comuni.add(0, EMPTY_COMUNE);
    return comuni;
  }
}
