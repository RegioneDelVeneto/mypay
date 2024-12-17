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
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoElaboratoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.BadRequestException;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.service.AnagraficaStatoService;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoElaboratoService;
import it.regioneveneto.mygov.payment.mypay4.service.DovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteTipoDovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.JasperService;
import it.regioneveneto.mygov.payment.mypay4.service.RecaptchaService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.util.SumUtilis;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.exolab.castor.xml.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Avvisi", description = "Gestione avvisi di pagamento")
@RestController
@Slf4j
@ConditionalOnWebApplication
public class AvvisoController {

  private static final String AUTHENTICATED_PATH ="avvisi";
  private static final String ANONYMOUS_PATH= MyPay4AbstractSecurityConfig.PATH_PUBLIC+"/"+ AUTHENTICATED_PATH;
  private static final String OPERATORE_PATH= MyPay4AbstractSecurityConfig.PATH_OPERATORE+"/"+ AUTHENTICATED_PATH;

  @Autowired
  private DovutoService dovutoService;

  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private JasperService jasperService;

  @Autowired
  private RecaptchaService recaptchaService;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  private EnteService enteService;

  @Autowired
  MessageSource messageSource;

  @GetMapping(AUTHENTICATED_PATH +"/search")
  public ResponseEntity<?> searchAvviso(
      @AuthenticationPrincipal UserWithAdditionalInfo user,
      @RequestParam String numeroAvviso, @RequestParam Boolean owner,
      @RequestParam(required = false) String codIdUnivoco,
      @RequestParam(required = false) String anagrafica,
      @RequestParam(required = false) String codIpaEnte,
      @RequestParam(required = false) String codTipoDovuto) {
    return searchAvvisoImpl(user, owner, numeroAvviso, codIdUnivoco, anagrafica, codIpaEnte, codTipoDovuto);
  }

  @SecurityRequirements
  @GetMapping(ANONYMOUS_PATH+"/search")
  public ResponseEntity<?> searchAvvisoAnonymous(
      @RequestParam String numeroAvviso,
      @RequestParam(required = false) String codIdUnivoco,
      @RequestParam(required = false) String anagrafica,
      @RequestParam(required = false) String codIpaEnte,
      @RequestParam(required = false) String codTipoDovuto,
      @RequestParam String recaptcha) {

    boolean captchaVerified = recaptchaService.verify(recaptcha,"searchAvviso");
    if(!captchaVerified){
      log.info("error verifying recaptcha operation[searchAvviso] iuv[{}] ipa[{}] tipoDovuto[{}]", codIdUnivoco, codIpaEnte, codTipoDovuto);
      throw new BadRequestException("Errore verifica recaptcha");
    }
    return searchAvvisoImpl(null, false, numeroAvviso, codIdUnivoco, anagrafica, codIpaEnte, codTipoDovuto);
  }

  ResponseEntity<Map<String, Object>> searchAvvisoImpl(UserWithAdditionalInfo user,Boolean owner, String numeroAvviso,
                                             String codIdUnivoco, String anagrafica, String codIpaEnte, String codTipoDovuto){
    Map<String, Object> responseMap = new HashMap<>();
    ResponseEntity.BodyBuilder responseBuilder;

    try {
      Map<String, String> map = this.inputValidator(user, numeroAvviso, owner, codIdUnivoco, anagrafica);

      Long mygovEnteId = null;
      codIpaEnte = StringUtils.stripToNull(codIpaEnte);
      if(codIpaEnte!=null)
        mygovEnteId = Optional.of(codIpaEnte).map(enteService::getEnteByCodIpa).map(Ente::getMygovEnteId)
          .orElseThrow(()-> new ValidatorException("Ente non valido"));

      List<DovutoTo> dovuti = dovutoService.searchDovutoOnTipoAttivo(mygovEnteId, codTipoDovuto, map.get("IUV"),
          map.get("codRpSoggIdUnivCodiceIdUnivoco"), map.get("deRpSoggPagAnagraficaPagatore"), false);

      List<DovutoTo> debiti = dovuti.stream().peek(dovuto -> {
        avvisoValidator(dovuto);
        if(!owner) {
          dovuto.setSecurityTokenAvviso(jwtTokenUtil.generateSecurityToken(user, ""+dovuto.getId()));
        }
      }).collect(Collectors.toList());

      //Retrieve the amount of importo dovuto multibeneficiario for 'debito' if it exists
      if(!debiti.isEmpty()) {
        BigDecimal importoMB = dovutoService.getImportoSingoloVerdamentoMBByIdDovuto(dovuti.get(0).getId());
        if(null != importoMB) {
          debiti.get(0).setImporto(SumUtilis.sumAmount(debiti.get(0).getImporto(), importoMB.toString()));
          debiti.get(0).setMultibeneficiario(true);
        }
      }

      responseMap.put("debiti", debiti);

      List<DovutoElaborato> pagati = dovutoElaboratoService.searchDovutoElaboratoByIuvIdPagatore(mygovEnteId, map.get("IUV"),
          map.get("codRpSoggIdUnivCodiceIdUnivoco"), map.get("deRpSoggPagAnagraficaPagatore"));
      List<DovutoElaboratoTo> debitiPagati = pagati.stream()
        .map(dovutoElaboratoService::mapDovutoElaboratoToDto)
        .peek(pagato -> {
          if(!owner){
            pagato.setSecurityTokenRt(jwtTokenUtil.generateSecurityToken(user, ""+pagato.getId()));
          }
        })
        .collect(Collectors.toList());

      //Retrieve the amount of importo dovuto multibeneficiario for 'pagati' if exsist
      if(!debitiPagati.isEmpty()) {
        debitiPagati.forEach(pagato -> {
          BigDecimal importoMB = dovutoElaboratoService.getImportoDovutoMultibeneficiarioElaboratoByIdDovuto(pagato.getId());
          if(null != importoMB) {
            pagato.setImporto(SumUtilis.sumAmountPagati(pagato.getImporto(), importoMB.toString()));
            pagato.setMultibeneficiario(true);
          }
        });
      }
      responseMap.put("pagati", debitiPagati);

      responseBuilder = ResponseEntity.status(HttpStatus.OK);
    } catch (Exception ex) {
      log.error("generic error when searching avviso", ex);
      responseMap.put("errorCode", ex.getMessage());
      responseBuilder = ResponseEntity.status(HttpStatus.BAD_REQUEST);
    }
    return responseBuilder.body(responseMap);
  }


  @GetMapping(AUTHENTICATED_PATH +"/download/{id}")
  public ResponseEntity<Resource> downloadAvviso(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long id, @RequestParam(required = false) String securityToken) {

    Dovuto dovuto = Optional.ofNullable(dovutoService.getById(id)).orElseThrow(NotFoundException::new);

    //retrieve "importo singolo versamento" for dovuto multibeneficiario
    BigDecimal importoDovutoMB = dovutoService.getImportoSingoloVerdamentoMBByIdDovuto(id);
    if(importoDovutoMB != null ) // if 'importoDovutoMB' is not null, i add in the amount of importo singolo versamento dovuto
      dovuto.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento().add(importoDovutoMB));

    //for avvisi not belonging to logged user (or when anonymous), verify security token
    // (to be sure that user found id in a legitimate way, i.e. searching with iuv and codice fiscale)
    if(user==null || !StringUtils.equalsIgnoreCase(user.getCodiceFiscale(),dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco())) {
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

    return downloadAvvisoImpl(dovuto);
  }

  @SecurityRequirements
  @GetMapping(ANONYMOUS_PATH+"/download/{id}")
  public ResponseEntity<Resource> downloadAvvisoAnonymous(
      @PathVariable Long id,
      @RequestParam String recaptcha,
      @RequestParam(required = false) String securityToken) {

    boolean captchaVerified = recaptchaService.verify(recaptcha,"downloadAvviso");
    if(!captchaVerified){
      throw new MyPayException("Errore verifica recaptcha");
    }

    return this.downloadAvviso(null, id, securityToken);
  }

  ResponseEntity<Resource> downloadAvvisoImpl(Dovuto dovuto) {
    try {
      ByteArrayOutputStream reportStream = jasperService.generateAvviso(dovuto);

      reportStream.flush();
      reportStream.close();

      String filename = Utilities.getFilenameAvviso(dovuto.getNestedEnte()!=null ? dovuto.getNestedEnte().getCodiceFiscaleEnte() : null, dovuto.getCodIuv());

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
      log.error("Si e verificato un errore nella generazione dell'avviso per il dovuto: {}", dovuto.toString(), e);
      throw new MyPayException("error downloading avviso", e);
    }
  }

  @GetMapping(OPERATORE_PATH +"/{id}/pn")
  @Operatore
  public ResponseEntity<Resource> downloadForOperatore(
      @AuthenticationPrincipal UserWithAdditionalInfo user, @PathVariable Long id) throws Exception {

    Dovuto dovuto = Optional.ofNullable(dovutoService.getById(id)).orElseThrow(NotFoundException::new);

    //retrieve "importo singolo versamento" for dovuto multibeneficiario
    BigDecimal importoDovutoMB = dovutoService.getImportoSingoloVerdamentoMBByIdDovuto(id);
    if(importoDovutoMB != null ) // if 'importoDovutoMB' is not null, i add in the amount of importo singolo versamento dovuto
      dovuto.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento().add(importoDovutoMB));
    List<EnteTipoDovuto> authorizedTipoDovuto = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(dovuto.getNestedEnte().getMygovEnteId(), user.getUsername());

    boolean error = authorizedTipoDovuto.stream().filter(etd -> etd.getCodTipo().equals(dovuto.getCodTipoDovuto())).count() != 1;
    //check if it in correct status
    if(!error)
      error = !AnagraficaStato.STATO_DOVUTO_DA_PAGARE.equals(dovuto.getMygovAnagraficaStatoId().getCodStato());
    if(error)
      throw new RuntimeException("L'avviso richiesto ["+id+"] non esiste o non si dispone dell'autorizzazione per visualizzarla");


    try {
      ByteArrayOutputStream reportStream = jasperService.generateAvviso(dovuto);

      reportStream.flush();
      reportStream.close();

      String filename = Utilities.getFilenameAvviso(dovuto.getNestedEnte()!=null ? dovuto.getNestedEnte().getCodiceFiscaleEnte() : null, dovuto.getCodIuv());

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

    } catch (ValidationException e) {
      log.error(e.getMessage());
      log.error("Si e verificato un errore nella generazione del report. " + dovuto, e);
      throw e;
    }
  }

  private Map<String, String> inputValidator(UserWithAdditionalInfo user, String numeroAvviso, Boolean owner, String codIdUnivoco, String anagrafica) {
    Map<String, String> rt = new HashMap<>();

    if(owner){
      rt.put("codRpSoggIdUnivCodiceIdUnivoco", user.getCodiceFiscale());
      rt.put("deRpSoggPagAnagraficaPagatore", null);
      rt.put("IUV", Utilities.numeroAvvisoToIuvValidator(numeroAvviso));
    } else {
      if( StringUtils.isBlank(codIdUnivoco) && StringUtils.isBlank(anagrafica) ||
          StringUtils.isNotBlank(codIdUnivoco) && StringUtils.isNotBlank(anagrafica)){
        throw new ValidatorException("Necessario inserire solo valore tra codice fiscale/partita iva e anagrafica intestatario");
      }
      rt.put("codRpSoggIdUnivCodiceIdUnivoco", StringUtils.isBlank(codIdUnivoco) ? Constants.CODICE_FISCALE_ANONIMO : StringUtils.strip(codIdUnivoco));
      rt.put("deRpSoggPagAnagraficaPagatore", StringUtils.isBlank(anagrafica) ? null : StringUtils.strip(anagrafica));
      rt.put("IUV", Utilities.numeroAvvisoToIuvValidator(numeroAvviso));
    }

    return rt;
  }

  private void avvisoValidator(DovutoTo dovuto) {
    enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), dovuto.getCodIpaEnte(), true)
      .ifPresentOrElse( enteTipoDovutoAttivo -> {
        boolean flagScadenzaObbligatoria = enteTipoDovutoAttivo.isFlgScadenzaObbligatoria();
        if(flagScadenzaObbligatoria && dovuto.getDataScadenza().isBefore(LocalDate.now())){
          dovuto.setCodStato(Constants.STATO_DOVUTO_SCADUTO);
          dovuto.setDeStato(anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_SCADUTO, Constants.STATO_TIPO_DOVUTO).getDeStato());
        }
        if(!Constants.STATO_DOVUTO_DA_PAGARE.equals(dovuto.getCodStato())){
          dovuto.setCodStato(Constants.STATO_DOVUTO_ERRORE);
          dovuto.setDeStato(messageSource.getMessage("pa.dovuto.nonpagabile", null, Locale.ITALY));
        }
      }, () -> {
        dovuto.setCodStato(Constants.STATO_DOVUTO_DISABILITATO);
        dovuto.setDeStato(messageSource.getMessage("pa.dovuto.nonabilitato", null, Locale.ITALY));
      });
  }

}
