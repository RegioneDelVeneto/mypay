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
import it.regioneveneto.mygov.payment.mypay4.dto.UtenteTo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.LocationService;
import it.regioneveneto.mygov.payment.mypay4.service.UtenteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@Tag(name = "Utente", description = "Gestione dati personali utente")
@SecurityRequirements
@RestController
@Slf4j
@ConditionalOnWebApplication
public class UtenteController {

  private static final String AUTHENTICATED_PATH ="utente";

  @Autowired
  private UtenteService utenteService;

  @Autowired
  private LocationService locationService;

  private Long nazioneIdItalia;

  @PostMapping(AUTHENTICATED_PATH)
  public void update(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestBody UtenteTo utenteTo){
    //check data coherence: if address is present, must be complete
    if(nazioneIdItalia==null)
      nazioneIdItalia = Optional.ofNullable(locationService.getNazioneByCodIso("IT")).orElseThrow(NotFoundException::new).getNazioneId();
    boolean addressCompletelyPresent = utenteTo.getNazioneId()!=null &&
        (  utenteTo.getNazioneId().equals(nazioneIdItalia) && utenteTo.getProvinciaId()!=null && utenteTo.getComuneId()!=null ||
          !utenteTo.getNazioneId().equals(nazioneIdItalia) && utenteTo.getProvinciaId()==null && utenteTo.getComuneId()==null ) &&
        StringUtils.isNotBlank(utenteTo.getCap()) && StringUtils.isNotBlank(utenteTo.getIndirizzo()) &&
        StringUtils.isNotBlank(utenteTo.getCivico());
    boolean addressCompletelyMissing = utenteTo.getNazioneId()==null && utenteTo.getProvinciaId()==null &&
        utenteTo.getComuneId()==null && StringUtils.isBlank(utenteTo.getCap()) &&
        StringUtils.isBlank(utenteTo.getIndirizzo()) && StringUtils.isBlank(utenteTo.getCivico());
    if(addressCompletelyMissing){
      log.info("setting address to null for user: {}", user.getUsername());
      utenteTo.setCap(null);
      utenteTo.setIndirizzo(null);
      utenteTo.setCivico(null);
    } else if(addressCompletelyPresent) {
      if(utenteTo.getNazioneId().equals(nazioneIdItalia)) {
        if(locationService.getProvincia(utenteTo.getProvinciaId()) == null ||
                locationService.getComune(utenteTo.getComuneId()) == null )
          throw new BadRequestException();
      } else {
        if(locationService.getNazione(utenteTo.getNazioneId()) == null )
          throw new BadRequestException();
      }
    } else {
      throw new BadRequestException("address fields not completely set");
    }

    //get existing user data
    Utente utente = utenteService.getByCodFedUserId(user.getUsername()).orElseThrow();
    utenteTo.setUserId(utente.getMygovUtenteId());

    //check if at least some data has been changed
    if( Objects.equals(utente.getNazioneId(), utenteTo.getNazioneId()) &&
        Objects.equals(utente.getProvinciaId(), utenteTo.getProvinciaId()) &&
        Objects.equals(utente.getComuneId(), utenteTo.getComuneId()) &&
        StringUtils.equals(utente.getCap(), utenteTo.getCap()) &&
        StringUtils.equals(utente.getIndirizzo(), utenteTo.getIndirizzo()) &&
        StringUtils.equals(utente.getCivico(), utenteTo.getCivico()) ) {
      //do nothing, unchanged data
     log.info("update utente [{}] address data: no changed data; request ignored", user.getUsername());
    } else {
      utenteService.updateIndirizzo(utenteTo);
    }
  }

}
