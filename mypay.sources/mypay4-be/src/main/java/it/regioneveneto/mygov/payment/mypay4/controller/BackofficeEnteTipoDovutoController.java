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
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaTipoDovutoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteTipoDovutoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.RegistroOperazioneTo;
import it.regioneveneto.mygov.payment.mypay4.dto.TassonomiaCodDescTo;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteTipoDovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.RegistroOperazioneService;
import it.regioneveneto.mygov.payment.mypay4.service.TassonomiaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "TipoDovuto", description = "Backoffice - gestione dei Tipi dovuto")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class BackofficeEnteTipoDovutoController {

  private final static String AUTHENTICATED_PATH ="admin/tipiDovuto";
  private final static String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private TassonomiaService tassonomiaService;

  @Autowired
  private RegistroOperazioneService registroOperazioneService;

  @Autowired
  private DovutoService dovutoService;

  @GetMapping(OPERATORE_PATH+"/macroArea")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<TassonomiaCodDescTo> getMacroArea(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestParam String tipoEnte) {
    return tassonomiaService.getMacroAreaForSelect(tipoEnte);
  }

  @GetMapping(OPERATORE_PATH+"/tipoServizio")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<TassonomiaCodDescTo> getTipoServizio(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestParam String tipoEnte,
                                            @RequestParam String macroArea) {
    return tassonomiaService.getTipoServizioForSelect(tipoEnte, macroArea);
  }

  @GetMapping(OPERATORE_PATH+"/motivoRiscossione")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<TassonomiaCodDescTo> getMotivoRiscossione(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestParam String tipoEnte,
                                                  @RequestParam String macroArea, @RequestParam String tipoServizio) {
    return tassonomiaService.getMotivoRiscossioneforSelect(tipoEnte, macroArea, tipoServizio);
  }

  @GetMapping(OPERATORE_PATH+"/codTassonomico")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<TassonomiaCodDescTo> getCodTassonomico(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestParam String tipoEnte,
                                              @RequestParam String macroArea, @RequestParam String tipoServizio, @RequestParam String motivoRisc) {
    return tassonomiaService.getCodTassFromSelect(tipoEnte, macroArea, tipoServizio, motivoRisc);
  }

  @GetMapping(OPERATORE_PATH+"/search")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<EnteTipoDovutoTo> searchTipoDovuto(@AuthenticationPrincipal UserWithAdditionalInfo user,
                                                 @RequestParam(required = false) String codTipo, @RequestParam(required = false) String deTipo,
                                                 @RequestParam(required = false) Boolean flgAttivo){
    return enteTipoDovutoService.searchByTipoFlgAttivo(codTipo, deTipo, flgAttivo);
  }

  @GetMapping(OPERATORE_PATH+"/search/tipo/{codTipo}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<EnteTipoDovutoTo> searchTipoDovutoByTipo(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable String codTipo,
                                                       @RequestParam(required = false) String codIpaEnte,@RequestParam(required = false) String deNomeEnte,
                                                       @RequestParam(required = false) Boolean flgAttivo){
    return enteTipoDovutoService.searchByTipoEnteFlgAttivo(codTipo, codIpaEnte, deNomeEnte, flgAttivo);
  }

  @GetMapping(OPERATORE_PATH+"/search/{mygovEnteId}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN}, value="mygovEnteId")
  public List<EnteTipoDovutoTo> searchTipoDovuto(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                                 @RequestParam(required = false) String codTipo, @RequestParam(required = false) String deTipo,
                                                 @RequestParam(required = false) Boolean flgAttivo, @RequestParam(required = false) Boolean withActivationInfo){
    return enteTipoDovutoService.searchByEnteTipoFlgAttivo(mygovEnteId, codTipo, deTipo, flgAttivo, withActivationInfo);
  }

  @GetMapping(OPERATORE_PATH+"/search/{mygovEnteId}/cod/{codTipo}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN}, value="mygovEnteId")
  public EnteTipoDovutoTo getTipoDovutoByEnteAndCodTipo(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                                 @PathVariable String codTipo){
    return enteTipoDovutoService.getTipoDovutoByEnteAndCodTipo(mygovEnteId, codTipo);
  }

  @GetMapping(OPERATORE_PATH+"/{mygovEnteTipoDovutoId}/registro")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public List<RegistroOperazioneTo> registroTipoDovuto(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteTipoDovutoId){
    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getById(mygovEnteTipoDovutoId);
    List<RegistroOperazione> registro = registroOperazioneService.getByTipoAndOggetto(RegistroOperazione.TipoOperazione.ENTE_TIP_DOV,
        enteTipoDovuto.getMygovEnteId().getCodIpaEnte()+'|'+enteTipoDovuto.getCodTipo());
    return registro.stream().map(registroOperazioneService::mapRegistroOperazioneToDto).collect(Collectors.toList());
  }

  @GetMapping(OPERATORE_PATH+"/{mygovEnteTipoDovutoId}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public AnagraficaTipoDovutoTo getTipoDovutoById(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteTipoDovutoId){
    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getById(mygovEnteTipoDovutoId);
    return enteTipoDovutoService.getAnagrafica(enteTipoDovuto);
  }

  @GetMapping(OPERATORE_PATH+"/activate/{mygovEnteTipoDovutoId}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public void activate(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteTipoDovutoId){
    enteTipoDovutoService.switchActivation(user, mygovEnteTipoDovutoId, true);
  }

  @GetMapping(OPERATORE_PATH+"/deactivate/{mygovEnteTipoDovutoId}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public void deactivate(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteTipoDovutoId){
    enteTipoDovutoService.switchActivation(user, mygovEnteTipoDovutoId, false);
  }

  @DeleteMapping(OPERATORE_PATH+"/delete/{mygovEnteTipoDovutoId}")
  @Operatore(appAdmin = true)
  public int delete(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteTipoDovutoId){
    return enteTipoDovutoService.deleteTipoDovuto(mygovEnteTipoDovutoId);
  }

  @PostMapping(OPERATORE_PATH+"/insert")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public AnagraficaTipoDovutoTo insert(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestBody AnagraficaTipoDovutoTo anagraficaTipoDovutoTo){
    return enteTipoDovutoService.insertTipoDovuto(user, anagraficaTipoDovutoTo);
  }

  @PostMapping(OPERATORE_PATH+"/update")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN})
  public AnagraficaTipoDovutoTo update(@AuthenticationPrincipal UserWithAdditionalInfo user, @RequestBody AnagraficaTipoDovutoTo anagraficaTipoDovutoTo){
    return enteTipoDovutoService.updateTipoDovuto(user, anagraficaTipoDovutoTo);
  }

  @GetMapping(OPERATORE_PATH+"/hasDovutoNoScadenza/{mygovEnteId}/cod/{codTipo}")
  @Operatore(roles = {Operatore.Role.ROLE_ADMIN}, value="mygovEnteId")
  public boolean hasDovutoNoScadenza(@AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
                                     @PathVariable String codTipo){
    return dovutoService.hasDovutoNoScadenza(mygovEnteId, codTipo);
  }
}
