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
import it.regioneveneto.mygov.payment.mypay4.dto.GiornaleTo;
import it.regioneveneto.mygov.payment.mypay4.dto.OpzioniRicercaGiornaleTo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Tag(name = "Operatori", description = "Backoffice - giornale degli eventi")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class BackofficeGiornaleController {

  private static final String AUTHENTICATED_PATH ="admin/giornale";
  private static final String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  @Autowired
  private it.regioneveneto.mygov.payment.mypay4.service.fesp.GiornaleService giornaleServiceFesp;

  @Autowired
  private it.regioneveneto.mygov.payment.mypay4.service.GiornaleService giornaleServicePa;

  @GetMapping(OPERATORE_PATH+"/{type}/search")
  @Operatore(appAdmin = true)
  public List<GiornaleTo> searchGiornale(
      @PathVariable String type,
      @RequestParam(required = false) String idDominio, @RequestParam(required = false) String iuv,
      @RequestParam(required = false) String categoriaEvento, @RequestParam(required = false) String tipoEvento,
      @RequestParam(required = false) String idPsp, @RequestParam(required = false) String esito,
      @RequestParam LocalDate dataEventoFrom, @RequestParam LocalDate dataEventoTo){
    if(type.equals("pa"))
      return giornaleServicePa.searchGiornale(idDominio, iuv,
          categoriaEvento, tipoEvento, idPsp, esito, dataEventoFrom, dataEventoTo);
    else if(type.equals("fesp"))
      return giornaleServiceFesp.searchGiornale(idDominio, iuv,
              categoriaEvento, tipoEvento, idPsp, esito, dataEventoFrom, dataEventoTo);
    else
      throw new BadRequestException("invalid search type "+type);
  }

  @GetMapping(OPERATORE_PATH+"/{type}/detail/{id}")
  @Operatore(appAdmin = true)
  public GiornaleTo getDetailGiornale(
      @PathVariable String type,
      @PathVariable Long id){
    if(type.equals("pa"))
      return Optional.ofNullable(giornaleServicePa.getGiornaleById(id))
          .map(giornaleServicePa::mapToDetailDto).orElseThrow(NotFoundException::new);
    else if(type.equals("fesp"))
      return Optional.ofNullable(giornaleServiceFesp.getGiornaleById(id))
          .map(giornaleServiceFesp::mapToDetailDto).orElseThrow(NotFoundException::new);
    else
      throw new BadRequestException("invalid search type "+type);
  }

  @GetMapping(OPERATORE_PATH+"/{type}/psp")
  @Operatore(appAdmin = true)
  public List<String> getAllPsp(@PathVariable String type){
    if(type.equals("pa"))
      return giornaleServicePa.getAllPspPa();
    else if(type.equals("fesp"))
      return giornaleServiceFesp.getAllPspFesp();
    else
      throw new BadRequestException("invalid search type "+type);
  }

  @GetMapping(OPERATORE_PATH+"/{type}/opzioni")
  @Operatore(appAdmin = true)
  public OpzioniRicercaGiornaleTo getAllOptions(@PathVariable String type){
    if(type.equals("pa"))
      return OpzioniRicercaGiornaleTo.builder()
          .tipoGiornale(type)
          .tipiEvento(giornaleServicePa.getAllTipoEventoPa())
          .categorieEvento(giornaleServicePa.getAllCategoriaEventoPa())
          .esitiEvento((giornaleServicePa.getAllEsitoEventoPa()))
          .build();
    else if(type.equals("fesp"))
      return OpzioniRicercaGiornaleTo.builder()
          .tipoGiornale(type)
          .tipiEvento(giornaleServiceFesp.getAllTipoEventoFesp())
          .categorieEvento(giornaleServiceFesp.getAllCategoriaEventoFesp())
          .esitiEvento(giornaleServiceFesp.getAllEsitoEventoFesp())
          .build();
    else
      throw new BadRequestException("invalid search type "+type);
  }

}
