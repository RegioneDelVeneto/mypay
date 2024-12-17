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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.mygov.payment.mypay4.config.MyPay4AbstractSecurityConfig;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoElaboratoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoOperatoreTo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.AnagraficaStatoService;
import it.regioneveneto.mygov.payment.mypay4.service.CarrelloService;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoElaboratoService;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteTipoDovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.JasperService;
import it.regioneveneto.mygov.payment.mypay4.service.RecaptchaService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Pagamenti effettuati", description = "Gestione dei pagamenti effettuati")
@RestController("Gestione pagati")
@Slf4j
@ConditionalOnWebApplication
public class PagatoController {

  static final String AUTHENTICATED_PATH ="pagati";
  private static final String ANONYMOUS_PATH= MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;
  private static final String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;
  private static final String A2A_PATH= MyPay4AbstractSecurityConfig.PATH_A2A+"/"+ AUTHENTICATED_PATH;

  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;

  @Autowired
  private DovutoService dovutoService;

  @Autowired
  private CarrelloService carrelloService;

  @Autowired
  private EnteService enteService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  private JasperService jasperService;

  @Autowired
  private RecaptchaService recaptchaService;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Operation(summary = "Ricerca pagamenti elaborati",
      description = "Ricerca pagamenti effettuati, da parte dell'utente loggato, usando una serie di filtri opzionali",
      responses = { @ApiResponse(description = "La lista dei pagamenti corrispondenti ai filtri di ricerca impostati")})
  @GetMapping(AUTHENTICATED_PATH +"/search")
  public List<DovutoElaboratoTo> searchDovutoElaborato(
      @AuthenticationPrincipal UserWithAdditionalInfo user,
      @Parameter(description = "Data pagamento DA") @RequestParam LocalDate from,
      @Parameter(description = "Data pagamento A") @RequestParam LocalDate to,
      @Parameter(description = "Codice ente beneficiario del pagamento") @RequestParam(required = false) String codIpaEnte,
      @Parameter(description = "Causale del pagamento (filtro per sottostringa)") @RequestParam(required = false) String causale,
      @Parameter(description = "Codice tipo dovuto del pagamento") @RequestParam(required = false) String codTipoDovuto,
      @Parameter(description = "Codice stato del pagamento") @RequestParam(name = "state", required = false) String codStato
      ){
    return dovutoElaboratoService.searchDovutoElaborato(codIpaEnte, user.getCodiceFiscale(), from, to, causale, codTipoDovuto, codStato);
  }

  @Operation(summary = "Ultimi pagamenti effettuati",
      description = "Ritorna gli ultimi N pagamenti effettuati da parte dell'utente loggato",
      responses = { @ApiResponse(description = "La lista degli ultimi pagamenti effettuati")})
  @GetMapping(AUTHENTICATED_PATH +"/last")
  public List<DovutoElaboratoTo> searchLastDovutoElaborato(
      @AuthenticationPrincipal UserWithAdditionalInfo user,
      @Parameter(description = "numero degli ultimi pagamenti da ritornare",
          schema = @Schema(maximum = "10", minimum = "1") )
      @RequestParam Integer num){
    if(num <1 || num > 10){
      throw new BadRequestException("num must be between 1 and 10");
    }
    return dovutoElaboratoService.searchLastDovutoElaborato(user.getCodiceFiscale(), num);
  }

  @GetMapping(AUTHENTICATED_PATH +"/{id}/rt")
  public ResponseEntity<Resource> download(
      @AuthenticationPrincipal UserWithAdditionalInfo user,
      @PathVariable Long id,
      @RequestParam(required = false) String securityToken) throws MyPayException {
    return this.downloadImpl(id, user, securityToken);
  }

  @GetMapping(ANONYMOUS_PATH+"/{id}/rt")
  public ResponseEntity<Resource> downloadAnonymous(
      @PathVariable Long id,
      @RequestParam String recaptcha,
      @RequestParam String securityToken) {
    boolean captchaVerified = recaptchaService.verify(recaptcha,"downloadRt");
    if(!captchaVerified){
      throw new MyPayException("Errore verifica recaptcha");
    }
    return this.downloadImpl(id, null, securityToken);
  }

  @GetMapping(A2A_PATH+"/info/{codFiscaleEnte}/{iuv}")
  public Map<String, String> getRtInfo(
      @PathVariable String codFiscaleEnte,
      @PathVariable String iuv) {

    //method previously worked with COD_IPA of ente: to preserve back-compatibility if the input param is a fiscal code
    // (i.e. 11 numbers) it is treated as fiscal code, otherwise as cod_ipa
    Ente ente = Optional.of(codFiscaleEnte).map(Constants.CF_PERSONA_GIURIDICA_REGEX.matcher(codFiscaleEnte).matches()
        ? enteService::getEnteByCodFiscale : enteService::getEnteByCodIpa)
      .orElseThrow(()->new NotFoundException("ente not found"));

    String iud=null, deStato=null, anagPagatore=null, cfPagatore=null;
    boolean found = false;

    //first search on dovuto elaborato (pick the first sorted by dt_ultima_modifica_rp DESC, mygov_dovuto_elaborato_id DESC )
    DovutoElaborato dovutoElaborato = dovutoElaboratoService.searchDovutoElaboratoByIuvEnte(iuv, ente.getCodIpaEnte())
      .stream()
      .filter(dovElab -> dovElab.getDtUltimaModificaRp() != null)
      .max(Comparator.comparing(DovutoElaborato::getDtUltimaModificaRp)
        .thenComparing(DovutoElaborato::getMygovDovutoElaboratoId))
      .orElse(null);

    if(dovutoElaborato!=null){
      found = true;
      iud = dovutoElaborato.getCodIud();
      if (!dovutoElaborato.getMygovAnagraficaStatoId().getCodStato().equals(AnagraficaStato.STATO_DOVUTO_COMPLETATO)) {
        deStato = dovutoElaborato.getMygovAnagraficaStatoId().getDeStato();
      } else {
        if (dovutoElaborato.getNumEDatiPagDatiSingPagSingoloImportoPagato().compareTo(BigDecimal.ZERO) == 0)
          deStato = anagraficaStatoService.getByCodStatoAndTipoStato(
              AnagraficaStato.STATO_CARRELLO_NON_PAGATO, AnagraficaStato.STATO_TIPO_CARRELLO).getDeStato();
        else
          deStato = anagraficaStatoService.getByCodStatoAndTipoStato(
              AnagraficaStato.STATO_CARRELLO_PAGATO, AnagraficaStato.STATO_TIPO_CARRELLO).getDeStato();
      }
      anagPagatore = dovutoElaborato.getDeRpSoggPagAnagraficaPagatore();
      cfPagatore = dovutoElaborato.getCodRpSoggPagIdUnivPagCodiceIdUnivoco();
    } else {
      List<Dovuto> listDovuto = dovutoService.searchDovutoByIuvEnte(iuv, ente.getCodIpaEnte());
      if(listDovuto.size()==1){
        found = true;
        Dovuto dovuto = listDovuto.get(0);
        iud = dovuto.getCodIud();
        deStato = dovuto.getMygovAnagraficaStatoId().getDeStato();
        anagPagatore = dovuto.getDeRpSoggPagAnagraficaPagatore();
        cfPagatore = dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco();
      }
    }

    Map<String, String> info = new HashMap<>();
    if(found) {
      info.put("iuv", iuv);
      info.put("iud", iud);
      info.put("deStato", deStato);
      info.put("anagPagatore", anagPagatore);
      info.put("cfPagatore", cfPagatore);
    }
    return info;
  }

  ResponseEntity<Resource> downloadImpl(Long id, UserWithAdditionalInfo user, String securityToken) throws MyPayException {

    DovutoElaborato dovutoElaborato = dovutoElaboratoService.getById(id);
    if(dovutoElaborato==null){
      log.warn("dovuto elaborato with id[{}] not found", id);
      throw new NotFoundException("dato non trovato");
    }

    //for avvisi not belonging to logged user (or when anonymous), verify security token
    // (to be sure that user found id in a legitimate way, i.e. searching with iuv and codice fiscale)
    boolean skipSecurityTokenCheck = user!=null
      && (StringUtils.equalsIgnoreCase(user.getCodiceFiscale(), dovutoElaborato.getCodRpSoggPagIdUnivPagCodiceIdUnivoco())
      || StringUtils.equalsIgnoreCase(user.getCodiceFiscale(), dovutoElaborato.getCodRpSoggVersIdUnivVersCodiceIdUnivoco()));

    if(!skipSecurityTokenCheck){
      try {
        String oid = jwtTokenUtil.parseSecurityToken(user, securityToken);
        if (!("" + id).equals(oid)) {
          throw new MyPayException("codice sicurezza non valido");
        }
      } catch(Exception e){
        log.warn("error parsing security code", e);
        throw new MyPayException("codice sicurezza non valido");
      }
    }

    //retrive 'importo singolo versamento' from dovuto multibeneficiario elaborato
    BigDecimal importoDovutoMB = dovutoElaboratoService.getImportoDovutoMultibeneficiarioElaboratoByIdDovuto(id);
    if(importoDovutoMB != null)
      dovutoElaborato.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(dovutoElaborato.getNumEDatiPagDatiSingPagSingoloImportoPagato().add(importoDovutoMB));

    //check if it in correct status
    if(dovutoElaborato==null || !AnagraficaStato.STATO_DOVUTO_COMPLETATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato()))
      throw new RuntimeException("La ricevuta telematica richiesta ["+id+"] non esiste o non si dispone dell'autorizzazione per visualizzarla");

    try {
      Carrello carrello = carrelloService.getById(dovutoElaborato.getMygovCarrelloId().getMygovCarrelloId());
      ByteArrayOutputStream reportStream = jasperService.generateRicevuta(carrello);

      String filename = Utilities.getFilenameRt(carrello.getCodRpDomIdDominio(), carrello.getValidIuv());

      reportStream.flush();
      reportStream.close();

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
      headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");

      InputStream isReport = new ByteArrayInputStream(reportStream.toByteArray());
      reportStream.close();

      return ResponseEntity.ok()
          .headers(headers)
          //.contentLength()
          .contentType(MediaType.APPLICATION_PDF)
          .body(new InputStreamResource(isReport));

    } catch (Exception e) {
      log.error("Si e verificato un errore nella generazione ricevuta telematica per il dovuto elaborato: {}", dovutoElaborato.getMygovDovutoElaboratoId(), e);
      throw new MyPayException("error downloading avviso", e);
    }
  }

  @GetMapping(OPERATORE_PATH +"/{id}/rt")
  @Operatore
  public ResponseEntity<Resource> downloadForOperatore(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long id) throws Exception {

    //check if given DovutoElaborato exists
    DovutoElaborato dovutoElaborato = dovutoElaboratoService.getById(id);
    List<EnteTipoDovuto> authorizedTipoDovuto = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(dovutoElaborato.getNestedEnte().getMygovEnteId(), user.getUsername());

    //retrive importo dovuto multibeneficiario elaborato
    BigDecimal importoMB = dovutoElaboratoService.getImportoDovutoMultibeneficiarioElaboratoByIdDovuto(id);
    if(importoMB != null)
      dovutoElaborato.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(dovutoElaborato.getNumEDatiPagDatiSingPagSingoloImportoPagato().add(importoMB));


    boolean error = authorizedTipoDovuto.stream().filter(etd -> etd.getCodTipo().equals(dovutoElaborato.getCodTipoDovuto())).count() != 1;
    //check if it in correct status
    if(!error)
      error = !AnagraficaStato.STATO_DOVUTO_COMPLETATO.equals(dovutoElaborato.getMygovAnagraficaStatoId().getCodStato());
    if(error)
      throw new RuntimeException("La ricevuta telematica richiesta ["+id+"] non esiste o non si dispone dell'autorizzazione per visualizzarla");

    try {
      Carrello carrello = carrelloService.getById(dovutoElaborato.getMygovCarrelloId().getMygovCarrelloId());
      ByteArrayOutputStream reportStream = jasperService.generateRicevuta(carrello);

      reportStream.flush();
      reportStream.close();

      String filename = Utilities.getFilenameRt(carrello.getCodRpDomIdDominio(), carrello.getValidIuv());

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
      headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");

      InputStream isReport = new ByteArrayInputStream(reportStream.toByteArray());
      reportStream.close();

      return ResponseEntity.ok()
          .headers(headers)
          //.contentLength()
          .contentType(MediaType.APPLICATION_PDF)
          .body(new InputStreamResource(isReport));

    } catch (Exception e) {
      log.error(e.getMessage());
      log.error("Si e verificato un errore nella generazione del report.", e);
      throw e;
    }
  }

  @GetMapping(OPERATORE_PATH +"/{mygovEnteId}/search")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public List<DovutoOperatoreTo> searchPagatoForOperatore(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
      @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam(required = false) String codStato,
      @RequestParam(required = false) Long myGovEnteTipoDovutoId, @RequestParam(required = false) String nomeFlusso,
      @RequestParam(required = false) String causale, @RequestParam(required = false) String codFiscale,
      @RequestParam(required = false) String iud, @RequestParam(required = false) String iuv){

    return dovutoElaboratoService.searchDovutoElaboratoForOperatore(user.getUsername(),
        mygovEnteId, codStato, myGovEnteTipoDovutoId, nomeFlusso, from, to, codFiscale, causale, iud, iuv);
  }

  @GetMapping(OPERATORE_PATH +"/{mygovEnteId}/{mygovPagatoId}")
  @Operatore(value = "mygovEnteId", roles = {Operatore.Role.ROLE_OPER})
  public DovutoOperatoreTo detailPagatoForOperatore(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long mygovEnteId,
      @PathVariable Long mygovPagatoId){

    return dovutoElaboratoService.getDetailsForOperatore(user.getUsername(), mygovEnteId, mygovPagatoId);
  }

}
