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
package it.regioneveneto.mygov.payment.mypay4.service;

import it.regioneveneto.mygov.payment.mypay4.bo.CarrelloBo;
import it.regioneveneto.mygov.payment.mypay4.dao.CarrelloDao;
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoCarrelloDao;
import it.regioneveneto.mygov.payment.mypay4.dto.DatiRendicontazioneCod9;
import it.regioneveneto.mygov.payment.mypay4.dto.common.Psp;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRPRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRPRisposta;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsitoRisposta;
import it.veneto.regione.schemas._2012.pagamenti.Esito;
import it.veneto.regione.schemas._2012.pagamenti.RP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class CarrelloService {

  @Resource
  CarrelloService self;
  @Autowired
  AnagraficaStatoService anagraficaStatoService;
  @Autowired
  EnteTipoDovutoService enteTipoDovutoService;
  @Autowired
  CarrelloMultiBeneficiarioService carrelloMultiBeneficiarioService;
  @Autowired
  CarrelloDao carrelloDao;
  @Autowired
  DovutoCarrelloDao dovutoCarrelloDao;
  @Autowired
  MessageSource messageSource;

  @Value("${pa.deRpVersioneOggetto}")
  private String deRpVersioneOggetto;
  @Value("${pa.pspDefaultIdentificativoCanale}")
  private String identificativoCanale;
  @Value("${pa.pspDefaultIdentificativoIntermediarioPsp}")
  private String identificativoIntermediarioPsp;
  @Value("${pa.pspDefaultIdentificativoPsp}")
  private String identificativoPsp;
  @Value("${pa.pspDefaultModelloPagamento:1}")
  private int modelloPagamento;

  @Value("${task.invioEmailEsito.batchRowLimit:${task.common.batchRowLimit}}")
  private int invioEmailEsitoBatchRowLimit;
  @Value("${task.scadenzaCarrello.batchRowLimit:${task.common.batchRowLimit}}")
  private int scadenzaCarrelloBatchRowLimit;

  @Autowired
  ConfigurableEnvironment env;

  /**
   * This is the same method as CarrelloService.dovutoCarrelloElimina() in Mypay3.
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public int deleteDovutoCarrello(Long mygovDovutoId, Long mygovCarrelloId) {
    return dovutoCarrelloDao.delete(mygovDovutoId, mygovCarrelloId);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Carrello upsert(Carrello carrello) {
    if (carrello.getMygovCarrelloId() == null || self.getById(carrello.getMygovCarrelloId()) == null) {
      long mygovCarrelloId = carrelloDao.insert(carrello);
      carrello.setMygovCarrelloId(mygovCarrelloId);
    } else {
      carrelloDao.update(carrello);
    }
    return carrello;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public Carrello getById(Long id) {
    return carrelloDao.getById(id);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public Optional<Carrello> getByIdLockOrSkip(Long id) {
    return carrelloDao.getByIdLockOrSkip(id);
  }

  public Carrello getByIdMessaggioRichiesta(String idMessaggioRichiesta) {
    List<Carrello> items = carrelloDao.getByIdMessaggioRichiesta(idMessaggioRichiesta);
    if (CollectionUtils.isEmpty(items)) {
      return null;
    } else if (items.size() > 1) {
      throw new MyPayException(messageSource.getMessage("pa.carrello.carrelloDuplicato", null, Locale.ITALY));
    }
    return items.get(0);
  }

  public Carrello getByIdSession(String idSession) {
    List<Carrello> items = carrelloDao.getByIdSession(idSession);
    if (CollectionUtils.isEmpty(items)) {
      return null;
    } else if (items.size() > 1) {
      throw new MyPayException(messageSource.getMessage("pa.carrello.carrelloDuplicato", null, Locale.ITALY));
    }
    return items.get(0);
  }

  public List<Carrello> getByMultiBeneficarioId (Long mygovCarrelloMultiBeneficiarioId) {
    return carrelloDao.getByMultiBeneficarioId(mygovCarrelloMultiBeneficiarioId);
  }

  public Carrello getByIdDominioIUV(String idDominio, String iuv) {
    List<Carrello> items = carrelloDao.getByIdDominioIUV(idDominio, iuv);
    if (CollectionUtils.isEmpty(items)) {
      return null;
    } else if (items.size() > 1) {
      throw new MyPayException(messageSource.getMessage("pa.carrello.carrelloDuplicato", null, Locale.ITALY));
    }
    return items.get(0);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Carrello insertCarrello(Dovuto dovuto, CarrelloBo carrelloBo, Ente ente, CarrelloMultiBeneficiario carrelloMultiBeneficiario) {
    Carrello cart = new Carrello();

    AnagraficaStato anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PREDISPOSTO, Constants.STATO_TIPO_CARRELLO);
    cart.setMygovAnagraficaStatoId(anagraficaStato);

    Date now = new Date();
    cart.setDtCreazione(now);
    cart.setDeRpVersioneOggetto(deRpVersioneOggetto);
    Optional.ofNullable(dovuto.getCodIuv()).ifPresent(cart::setCodRpDatiVersIdUnivocoVersamento);
    cart.setCodRpSoggPagIdUnivPagTipoIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco());
    cart.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
    cart.setDeRpSoggPagAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore());
    cart.setDeRpSoggPagEmailPagatore(dovuto.getDeRpSoggPagEmailPagatore());

    // bonifica indirizzo pagatore
    //String indirizzoPagatore = dovuto.getSoggettoPagatore().getIndirizzoPagatore();
    //String indirizzoPagatoreBonificato;
    if (StringUtils.isNotBlank(dovuto.getDeRpSoggPagIndirizzoPagatore())) {
      //indirizzoPagatoreBonificato = Utilities.bonificaIndirizzoAnagrafica(indirizzoPagatore);
      cart.setDeRpSoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
    }

    // bonifica civico pagatore
    //String civicoPagatore = dovuto.getSoggettoPagatore().getCivicoPagatore();
    //String civicoPagatoreBonificato;
    if (StringUtils.isNotBlank(dovuto.getDeRpSoggPagCivicoPagatore())) {
      //civicoPagatoreBonificato = Utilities.bonificaCivicoAnagrafica(civicoPagatore);
      cart.setDeRpSoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
    }
    Optional.ofNullable(dovuto.getCodRpSoggPagCapPagatore()).ifPresent(cart::setCodRpSoggPagCapPagatore);
    Optional.ofNullable(dovuto.getDeRpSoggPagLocalitaPagatore()).ifPresent(cart::setDeRpSoggPagLocalitaPagatore);
    Optional.ofNullable(dovuto.getDeRpSoggPagProvinciaPagatore()).ifPresent(cart::setDeRpSoggPagProvinciaPagatore);
    Optional.ofNullable(dovuto.getCodRpSoggPagNazionePagatore()).ifPresent(cart::setCodRpSoggPagNazionePagatore);

    // CAMPI DA DATI VERSAMENTO
    cart.setDtRpDatiVersDataEsecuzionePagamento(now);
    cart.setCodRpDatiVersTipoVersamento(carrelloBo.getPsp().getTipoVersamento());
    cart.setCodRpSilinviarpIdPsp(carrelloBo.getPsp().getIdentificativoPSP());
    cart.setCodRpSilinviarpIdIntermediarioPsp(carrelloBo.getPsp().getIdentificativoIntermediarioPSP());
    cart.setCodRpSilinviarpIdCanale(carrelloBo.getPsp().getIdentificativoCanale());
    cart.setModelloPagamento(carrelloBo.getPsp().getModelloPagamento());
    cart.setCodRpSilinviarpIdDominio(ente.getCodiceFiscaleEnte());
    cart.setVersion(0);
    cart.setTipoCarrello(carrelloBo.getTipoCarrello());
    cart.setIdSession(carrelloBo.getIdSession());
    //Todo: sum every single amount
    cart.setNumRpDatiVersImportoTotaleDaVersare(carrelloBo.getTotalAmount());

    cart.setMygovCarrelloMultiBeneficiarioId(carrelloMultiBeneficiario);

    String codEnteTipoDovuto = dovuto.getCodTipoDovuto();
    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(codEnteTipoDovuto, ente.getCodIpaEnte(), true)
        .orElseThrow(()-> new NotFoundException("tipo dovuto non trovato: "+ente.getCodIpaEnte()+"/"+codEnteTipoDovuto));

    Optional.ofNullable(enteTipoDovuto.getCodiceContestoPagamento())
        .or(() -> Optional.ofNullable(carrelloBo.getCodiceContestoPagamento()))
        .ifPresent(cart::setCodRpDatiVersCodiceContestoPagamento);

    Long newId = carrelloDao.insert(cart);
    log.info("insert Carrello base, new Id: "+newId);
    log.debug("Carrello with ID [%s] has been created: {}", newId, ReflectionToStringBuilder.toString(cart));
    return cart.toBuilder().mygovCarrelloId(newId).build();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateCarrelloForRP(Carrello cart, RP ctRp, String status){

    cart.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(status, Constants.STATO_TIPO_CARRELLO));

    cart.setCodRpDomIdDominio(ctRp.getDominio().getIdentificativoDominio());
    cart.setCodRpDomIdStazioneRichiedente(ctRp.getDominio().getIdentificativoStazioneRichiedente());
    cart.setCodRpIdMessaggioRichiesta(ctRp.getIdentificativoMessaggioRichiesta());
    cart.setDtRpDataOraMessaggioRichiesta(ctRp.getDataOraMessaggioRichiesta().toGregorianCalendar().getTime());
    cart.setCodRpAutenticazioneSoggetto(ctRp.getAutenticazioneSoggetto().toString());

    // SOGGETTO PAGATORE
    cart.setCodRpSoggPagIdUnivPagTipoIdUnivoco(ctRp.getSoggettoPagatore()
        .getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString().charAt(0));
    cart.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(ctRp.getSoggettoPagatore()
        .getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());
    cart.setDeRpSoggPagAnagraficaPagatore(ctRp.getSoggettoPagatore().getAnagraficaPagatore());
    cart.setDeRpSoggPagIndirizzoPagatore(ctRp.getSoggettoPagatore().getIndirizzoPagatore());
    cart.setDeRpSoggPagCivicoPagatore(ctRp.getSoggettoPagatore().getCivicoPagatore());
    cart.setCodRpSoggPagCapPagatore(ctRp.getSoggettoPagatore().getCapPagatore());
    cart.setDeRpSoggPagLocalitaPagatore(ctRp.getSoggettoPagatore().getLocalitaPagatore());
    cart.setDeRpSoggPagProvinciaPagatore(ctRp.getSoggettoPagatore().getProvinciaPagatore());
    cart.setCodRpSoggPagNazionePagatore(ctRp.getSoggettoPagatore().getNazionePagatore());
    cart.setDeRpSoggPagEmailPagatore(ctRp.getSoggettoPagatore().getEMailPagatore());

    // SOGGETTO VERSANTE
    if (ctRp.getSoggettoVersante() != null) {
      cart.setCodRpSoggVersIdUnivVersTipoIdUnivoco(ctRp.getSoggettoVersante()
          .getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString().charAt(0));
      cart.setCodRpSoggVersIdUnivVersCodiceIdUnivoco(ctRp.getSoggettoVersante()
          .getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());
      cart.setCodRpSoggVersAnagraficaVersante(ctRp.getSoggettoVersante().getAnagraficaVersante());
      cart.setDeRpSoggVersIndirizzoVersante(ctRp.getSoggettoVersante().getIndirizzoVersante());
      cart.setDeRpSoggVersCivicoVersante(ctRp.getSoggettoVersante().getCivicoVersante());
      cart.setCodRpSoggVersCapVersante(ctRp.getSoggettoVersante().getCapVersante());
      cart.setDeRpSoggVersLocalitaVersante(ctRp.getSoggettoVersante().getLocalitaVersante());
      cart.setDeRpSoggVersProvinciaVersante(ctRp.getSoggettoVersante().getProvinciaVersante());
      cart.setCodRpSoggVersNazioneVersante(ctRp.getSoggettoVersante().getNazioneVersante());
      cart.setDeRpSoggVersEmailVersante(ctRp.getSoggettoVersante().getEMailVersante());
    }

    Date now = new Date();
    cart.setDtRpDatiVersDataEsecuzionePagamento(now);
    cart.setNumRpDatiVersImportoTotaleDaVersare(ctRp.getDatiVersamento().getImportoTotaleDaVersare());
    cart.setCodRpDatiVersTipoVersamento(ctRp.getDatiVersamento().getTipoVersamento().toString());
    cart.setCodRpDatiVersIdUnivocoVersamento(ctRp.getDatiVersamento().getIdentificativoUnivocoVersamento());
    cart.setCodRpDatiVersCodiceContestoPagamento(ctRp.getDatiVersamento().getCodiceContestoPagamento());
    cart.setDeRpDatiVersIbanAddebito(ctRp.getDatiVersamento().getIbanAddebito());
    cart.setDeRpDatiVersBicAddebito(ctRp.getDatiVersamento().getBicAddebito());
    cart.setDtUltimaModificaRp(now);

    int id = carrelloDao.update(cart);
    if (id != 1) {
      throw new MyPayException("Carrello update internal error");
    }
    log.info("Carrello ["+cart.getMygovCarrelloId()+"] is up to date");
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateCarrelloRpRisposta(final long idCarrello, final NodoSILInviaCarrelloRPRisposta ncr)
      throws DataAccessException, UnsupportedEncodingException, MalformedURLException {
    Carrello cart = getById(idCarrello);

    cart.setDeRpSilinviarpEsito(ncr.getEsito());
    if (!"OK".equals(ncr.getEsito()) && ncr.getFault() != null) {
      FaultBean fb = ncr.getFault();
      cart.setCodRpSilinviarpFaultCode(fb.getFaultCode());
      cart.setDeRpSilinviarpFaultString(fb.getFaultString());
      cart.setCodRpSilinviarpId(fb.getId());
      cart.setCodRpSilinviarpSerial(fb.getSerial());

      if (StringUtils.isNotBlank(fb.getDescription())) {
        String description = Utilities.getTruncatedAt(1024).apply(fb.getDescription());
        cart.setDeRpSilinviarpOriginalFaultDescription(description);
      }
      cart.setCodRpSilinviarpOriginalFaultCode(fb.getOriginalFaultCode());
      cart.setDeRpSilinviarpOriginalFaultString(fb.getOriginalFaultString());
      if (StringUtils.isNotBlank(fb.getOriginalDescription())) {
        String description = Utilities.getTruncatedAt(1024).apply(fb.getOriginalDescription());
        cart.setDeRpSilinviarpOriginalFaultDescription(description);
      }
      AnagraficaStato newStatus = anagraficaStatoService.getByCodStatoAndTipoStato(
          Constants.STATO_CARRELLO_IMPOSSIBILE_INVIARE_RP, Constants.STATO_TIPO_CARRELLO);

      if (!cart.getMygovAnagraficaStatoId().getCodStato()
          .equals(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO)) {
        throw new DataRetrievalFailureException("Tentativo di cambio stato Carrello ["
            + cart.getCodRpDatiVersIdUnivocoVersamento() + "] da ["
            + cart.getMygovAnagraficaStatoId().getCodStato() + "] in [" + newStatus.getCodStato()
            + "].");
      }
      cart.setMygovAnagraficaStatoId(newStatus);
    }
    cart.setCodRpSilinviarpUrl(ncr.getUrl());
    Date now = new Date();
    cart.setDtUltimaModificaRp(now);
    // estrarre idSession e persistere in idSessionFesp
    String idSessionFESP = null;
    if (StringUtils.isNotBlank(ncr.getUrl())) {
      Map<String, String> parametersMap = Utilities.splitQuery(new URL(ncr.getUrl()));
      idSessionFESP = parametersMap.get("idSession");
    }
    cart.setIdSessionFesp(idSessionFESP);

    int id = carrelloDao.update(cart);
    if (id != 1) {
      throw new MyPayException("Errore interno aggiornamento Carrello");
    }
    log.info("Carrello ["+cart.getMygovCarrelloId()+"] is up to date");
  }

  public Psp newPspDefaultAgidValue() {
    return Psp.builder()
        .identificativoCanale(identificativoCanale)
        .identificativoIntermediarioPSP(identificativoIntermediarioPsp)
        .identificativoPSP(identificativoPsp)
        .modelloPagamento(modelloPagamento)
        .tipoVersamento(Constants.PAY_BONIFICO_BANCARIO_TESORERIA)
        .build();
  }

  public String generateCausaleAgIDFormat(String codIuv, BigDecimal importo, String deRpDatiVersDatiSingVersCausaleVersamento) {
    StringJoiner joiner = new StringJoiner("/","/","");
    joiner.add(codIuv.startsWith("RF")? "RFS": "RFB")
        .add(codIuv)
        .add(importo.toString());
    if (StringUtils.isNotBlank(deRpDatiVersDatiSingVersCausaleVersamento)) {
      joiner.add("TXT").add(deRpDatiVersDatiSingVersCausaleVersamento);
    }
    String rebuilt = Utilities.rebuildValidFormat(joiner.toString(), Constants.CAUSALE_PATTERN);
    return String.format(Constants.CAUSALE_TRUNCATE_PATTERN, rebuilt);
  }

  public Carrello aggiornaCarrelloInPagato(DatiRendicontazioneCod9 datiRendicontazioneCod9, Carrello carrello,
                                           Ente ente, Dovuto dovuto) {

    AnagraficaStato anagStatoCarrelloPagato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PAGATO, Constants.STATO_TIPO_CARRELLO);
    carrello.setMygovAnagraficaStatoId(anagStatoCarrelloPagato);

    if (null != dovuto) {
      carrello.setDeRpSoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
      carrello.setDeRpSoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
      carrello.setCodRpSoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore());
      carrello.setDeRpSoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore());
      carrello.setDeRpSoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore());
      carrello.setCodRpDatiVersTipoVersamento(dovuto.getCodRpDatiVersTipoVersamento().isEmpty() ? Constants.TIPO_VERSAMENTO.TUTTI.getValue() : dovuto.getCodRpDatiVersTipoVersamento());
      carrello.setCodESoggPagIdUnivPagCodiceIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco().isEmpty() ? Constants.CODICE_FISCALE_ANONIMO : dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
      carrello.setCodESoggPagAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore().isEmpty() ? Constants.CODICE_FISCALE_ANONIMO : dovuto.getDeRpSoggPagAnagraficaPagatore());
      carrello.setDeESoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
      carrello.setDeESoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
      carrello.setCodESoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore());
      carrello.setDeESoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore());
      carrello.setDeESoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore());
      carrello.setCodRpSoggPagIdUnivPagTipoIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco() == ' ' ? Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0) : dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco());
      carrello.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco().isEmpty() ? Constants.CODICE_FISCALE_ANONIMO : dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
      carrello.setDeRpSoggPagAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore().isEmpty() ? Constants.CODICE_FISCALE_ANONIMO : dovuto.getDeRpSoggPagAnagraficaPagatore());

    }else {
      carrello.setCodRpDatiVersTipoVersamento(Constants.TIPO_VERSAMENTO.TUTTI.getValue());
      carrello.setCodESoggPagIdUnivPagCodiceIdUnivoco(Constants.CODICE_FISCALE_ANONIMO);
      carrello.setCodESoggPagAnagraficaPagatore(Constants.CODICE_FISCALE_ANONIMO);
      carrello.setCodRpSoggPagIdUnivPagTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));
      carrello.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(Constants.CODICE_FISCALE_ANONIMO);
      carrello.setDeRpSoggPagAnagraficaPagatore(Constants.CODICE_FISCALE_ANONIMO);
    }

    carrello.setDtUltimaModificaRp(new Date());
    carrello.setDtUltimaModificaE(new Date());
    carrello.setCodRpSilinviarpIdPsp(datiRendicontazioneCod9.getIstitutoAttestante().getCodiceIdentificativoUnivoco());
    carrello.setCodRpSilinviarpIdDominio(ente.getCodiceFiscaleEnte());
    carrello.setCodRpSilinviarpIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    carrello.setCodRpSilinviarpCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    carrello.setDeRpSilinviarpEsito(Constants.ESITO.OK.toString());
    carrello.setCodRpDomIdDominio(ente.getCodiceFiscaleEnte());
    carrello.setCodRpIdMessaggioRichiesta((null == carrello.getCodRpIdMessaggioRichiesta() || carrello.getCodRpIdMessaggioRichiesta().isEmpty()) ? Constants.COD_MARCATURA_REND_9 + UUID.randomUUID().toString().replace("-", "") : carrello.getCodRpIdMessaggioRichiesta());
    carrello.setDtRpDataOraMessaggioRichiesta(carrello.getDtRpDataOraMessaggioRichiesta() == null ? new Date() : carrello.getDtRpDataOraMessaggioRichiesta());
    carrello.setCodRpAutenticazioneSoggetto(Constants.CODICE_AUTENTICAZIONE_SOGGETTO_NA);
    carrello.setDtRpDatiVersDataEsecuzionePagamento(datiRendicontazioneCod9.getDataEsitoSingoloPagamento());
    carrello.setCodRpDatiVersIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    carrello.setCodRpDatiVersCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    carrello.setCodESilinviaesitoIdDominio(ente.getCodiceFiscaleEnte());
    carrello.setCodESilinviaesitoIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    carrello.setCodESilinviaesitoCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    carrello.setDeESilinviaesitoEsito(Constants.ESITO.OK.toString());
    carrello.setDeEVersioneOggetto(deRpVersioneOggetto);
    carrello.setCodEDomIdDominio(ente.getCodiceFiscaleEnte());
    carrello.setCodEIdMessaggioRicevuta((null == carrello.getCodEIdMessaggioRicevuta() || carrello.getCodEIdMessaggioRicevuta().isEmpty()) ? Constants.COD_MARCATURA_REND_9 + UUID.randomUUID().toString().replace("-", "") : carrello.getCodEIdMessaggioRicevuta());
    carrello.setCodEDataOraMessaggioRicevuta(carrello.getCodEDataOraMessaggioRicevuta() == null ? new Date() : carrello.getCodEDataOraMessaggioRicevuta());
    carrello.setCodERiferimentoMessaggioRichiesta(carrello.getCodRpIdMessaggioRichiesta());
    carrello.setCodERiferimentoDataRichiesta(carrello.getCodERiferimentoDataRichiesta() == null ? new Date() : carrello.getCodERiferimentoDataRichiesta());
    carrello.setCodEIstitAttIdUnivAttTipoIdUnivoco(datiRendicontazioneCod9.getIstitutoAttestante().getTipoIdentificativoUnivoco().charAt(0));
    carrello.setCodEIstitAttIdUnivAttCodiceIdUnivoco(datiRendicontazioneCod9.getIstitutoAttestante().getCodiceIdentificativoUnivoco());
    carrello.setCodEEnteBenefIdUnivBenefTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_G.charAt(0));
    carrello.setCodEEnteBenefIdUnivBenefCodiceIdUnivoco(ente.getCodiceFiscaleEnte());
    carrello.setDeEEnteBenefDenominazioneBeneficiario(ente.getDeRpEnteBenefDenominazioneBeneficiario());
    carrello.setDeEEnteBenefIndirizzoBeneficiario(ente.getDeRpEnteBenefIndirizzoBeneficiario());
    carrello.setDeEEnteBenefCivicoBeneficiario(ente.getDeRpEnteBenefCivicoBeneficiario());
    carrello.setCodEEnteBenefCapBeneficiario(ente.getCodRpEnteBenefCapBeneficiario());
    carrello.setDeEEnteBenefLocalitaBeneficiario(ente.getDeRpEnteBenefLocalitaBeneficiario());
    carrello.setDeEEnteBenefProvinciaBeneficiario(ente.getDeRpEnteBenefProvinciaBeneficiario());
    carrello.setCodEEnteBenefNazioneBeneficiario(ente.getCodRpEnteBenefNazioneBeneficiario());
    carrello.setCodESoggPagIdUnivPagTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));
    carrello.setCodEDatiPagCodiceEsitoPagamento(Constants.CODICE_ESITO_PAGAMENTO_ESEGUITO.charAt(0));
    carrello.setNumEDatiPagImportoTotalePagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    if (Utilities.isAvviso(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento()))
      carrello.setModelloPagamento(Integer.valueOf(Constants.MODELLO_PAGAMENTO_4));
    else {
      carrello.setModelloPagamento(Integer.valueOf(Constants.MODELLO_PAGAMENTO_1));
    }
    carrello.setFlgNotificaEsito(false);
    carrello.setDeRpVersioneOggetto(deRpVersioneOggetto);
    carrello.setNumRpDatiVersImportoTotaleDaVersare(datiRendicontazioneCod9.getSingoloImportoPagato());
    carrello.setDeEIstitAttDenominazioneAttestante(datiRendicontazioneCod9.getIstitutoAttestante().getDenominazioneAttestante());
    carrello.setCodEDatiPagIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    carrello.setCodEDatiPagCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    carrello.setTipoCarrello(Constants.TIPO_CARRELLO_DEFAULT);

    return carrello;
  }

  /**
   * This method creates the new Carrello object, and is the same method as getCarrelloFittizio() with dovuto = null,
   * getCarreloFittizioByDovuti() in CarrelloService of Mypay3.
   */
  public Carrello getCarrelloFittizio(DatiRendicontazioneCod9 datiRendicontazioneCod9, Dovuto dovuto, Ente ente) {

    Carrello carrelloFittizio = new Carrello();

    AnagraficaStato anagCarrelloPagato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PAGATO, Constants.STATO_TIPO_CARRELLO);

    carrelloFittizio.setVersion(0);
    carrelloFittizio.setDtCreazione(new Date());
    carrelloFittizio.setMygovAnagraficaStatoId(anagCarrelloPagato);
    carrelloFittizio.setDtUltimaModificaRp(new Date());
    carrelloFittizio.setDtUltimaModificaE(new Date());
    carrelloFittizio.setCodRpSilinviarpIdPsp(datiRendicontazioneCod9.getIstitutoAttestante().getCodiceIdentificativoUnivoco());
    carrelloFittizio.setCodRpSilinviarpIdDominio(ente.getCodiceFiscaleEnte());
    carrelloFittizio.setCodRpSilinviarpIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    carrelloFittizio.setCodRpSilinviarpCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    carrelloFittizio.setDeRpSilinviarpEsito(Constants.ESITO.OK.toString());
    carrelloFittizio.setCodRpDomIdDominio(ente.getCodiceFiscaleEnte());
    carrelloFittizio.setCodRpIdMessaggioRichiesta(Constants.COD_MARCATURA_REND_9 + UUID.randomUUID().toString().replace("-", ""));
    carrelloFittizio.setDtRpDataOraMessaggioRichiesta(new Date());
    carrelloFittizio.setCodRpAutenticazioneSoggetto(Constants.CODICE_AUTENTICAZIONE_SOGGETTO_NA);
    carrelloFittizio.setDtRpDatiVersDataEsecuzionePagamento(datiRendicontazioneCod9.getDataEsitoSingoloPagamento());
    carrelloFittizio.setCodRpDatiVersIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    carrelloFittizio.setCodRpDatiVersCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    carrelloFittizio.setCodESilinviaesitoIdDominio(ente.getCodiceFiscaleEnte());
    carrelloFittizio.setCodESilinviaesitoIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    carrelloFittizio.setCodESilinviaesitoCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    carrelloFittizio.setDeESilinviaesitoEsito(Constants.ESITO.OK.toString());
    carrelloFittizio.setDeEVersioneOggetto(deRpVersioneOggetto);
    carrelloFittizio.setCodEDomIdDominio(ente.getCodiceFiscaleEnte());
    carrelloFittizio.setCodEIdMessaggioRicevuta(Constants.COD_MARCATURA_REND_9 + UUID.randomUUID().toString().replace("-", ""));
    carrelloFittizio.setCodEDataOraMessaggioRicevuta(new Date());
    carrelloFittizio.setCodERiferimentoMessaggioRichiesta(carrelloFittizio.getCodRpIdMessaggioRichiesta());
    carrelloFittizio.setCodERiferimentoDataRichiesta(new Date());
    carrelloFittizio.setCodEIstitAttIdUnivAttTipoIdUnivoco(datiRendicontazioneCod9.getIstitutoAttestante().getTipoIdentificativoUnivoco().charAt(0));
    carrelloFittizio.setCodEIstitAttIdUnivAttCodiceIdUnivoco(datiRendicontazioneCod9.getIstitutoAttestante().getCodiceIdentificativoUnivoco());
    carrelloFittizio.setCodEEnteBenefIdUnivBenefTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_G.charAt(0));
    carrelloFittizio.setCodEEnteBenefIdUnivBenefCodiceIdUnivoco(ente.getCodiceFiscaleEnte());
    carrelloFittizio.setDeEEnteBenefDenominazioneBeneficiario(ente.getDeRpEnteBenefDenominazioneBeneficiario());
    carrelloFittizio.setDeEEnteBenefIndirizzoBeneficiario(ente.getDeRpEnteBenefIndirizzoBeneficiario());
    carrelloFittizio.setDeEEnteBenefCivicoBeneficiario(ente.getDeRpEnteBenefCivicoBeneficiario());
    carrelloFittizio.setCodEEnteBenefCapBeneficiario(ente.getCodRpEnteBenefCapBeneficiario());
    carrelloFittizio.setDeEEnteBenefLocalitaBeneficiario(ente.getDeRpEnteBenefLocalitaBeneficiario());
    carrelloFittizio.setDeEEnteBenefProvinciaBeneficiario(ente.getDeRpEnteBenefProvinciaBeneficiario());
    carrelloFittizio.setCodEEnteBenefNazioneBeneficiario(ente.getCodRpEnteBenefNazioneBeneficiario());
    carrelloFittizio.setCodESoggPagIdUnivPagTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));
    carrelloFittizio.setCodEDatiPagCodiceEsitoPagamento(Constants.CODICE_ESITO_PAGAMENTO_ESEGUITO.charAt(0));
    carrelloFittizio.setNumEDatiPagImportoTotalePagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    if (Utilities.isAvviso(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento()))
      carrelloFittizio.setModelloPagamento(Integer.valueOf(Constants.MODELLO_PAGAMENTO_4));
    else {
      carrelloFittizio.setModelloPagamento(Integer.valueOf(Constants.MODELLO_PAGAMENTO_1));
    }
    carrelloFittizio.setFlgNotificaEsito(false);
    carrelloFittizio.setDeRpVersioneOggetto(deRpVersioneOggetto);
    carrelloFittizio.setNumRpDatiVersImportoTotaleDaVersare(datiRendicontazioneCod9.getSingoloImportoPagato());
    carrelloFittizio.setDeEIstitAttDenominazioneAttestante(datiRendicontazioneCod9.getIstitutoAttestante().getDenominazioneAttestante());
    carrelloFittizio.setCodEDatiPagIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    carrelloFittizio.setCodEDatiPagCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    carrelloFittizio.setTipoCarrello(Constants.TIPO_CARRELLO_DEFAULT);

    if (dovuto != null) {
      carrelloFittizio.setDeRpSoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
      carrelloFittizio.setDeRpSoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
      carrelloFittizio.setCodRpSoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore());
      carrelloFittizio.setDeRpSoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore());
      carrelloFittizio.setDeRpSoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore());
      carrelloFittizio.setCodRpSoggPagNazionePagatore(dovuto.getCodRpSoggPagNazionePagatore());
      carrelloFittizio.setCodRpDatiVersTipoVersamento(dovuto.getCodRpDatiVersTipoVersamento().isEmpty() ? Constants.TIPO_VERSAMENTO.TUTTI.getValue() : dovuto.getCodRpDatiVersTipoVersamento());
      carrelloFittizio.setCodESoggPagIdUnivPagCodiceIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco().isEmpty() ? Constants.CODICE_FISCALE_ANONIMO : dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
      carrelloFittizio.setCodESoggPagAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore().isEmpty() ? Constants.CODICE_FISCALE_ANONIMO : dovuto.getDeRpSoggPagAnagraficaPagatore());
      carrelloFittizio.setDeESoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
      carrelloFittizio.setDeESoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
      carrelloFittizio.setCodESoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore());
      carrelloFittizio.setDeESoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore());
      carrelloFittizio.setDeESoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore());
      carrelloFittizio.setCodESoggPagNazionePagatore(dovuto.getCodRpSoggPagNazionePagatore());
      carrelloFittizio.setCodRpSoggPagIdUnivPagTipoIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco() == ' ' ? Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0) : dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco());
      carrelloFittizio.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco().isEmpty() ? Constants.CODICE_FISCALE_ANONIMO : dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
      carrelloFittizio.setDeRpSoggPagAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore().isEmpty() ? Constants.CODICE_FISCALE_ANONIMO : dovuto.getDeRpSoggPagAnagraficaPagatore());
    } else {
      carrelloFittizio.setCodRpDatiVersTipoVersamento(Constants.TIPO_VERSAMENTO.TUTTI.getValue());
      carrelloFittizio.setCodESoggPagIdUnivPagCodiceIdUnivoco(Constants.CODICE_FISCALE_ANONIMO);
      carrelloFittizio.setCodESoggPagAnagraficaPagatore(Constants.CODICE_FISCALE_ANONIMO);
      carrelloFittizio.setCodRpSoggPagIdUnivPagTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));
      carrelloFittizio.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(Constants.CODICE_FISCALE_ANONIMO);
      carrelloFittizio.setDeRpSoggPagAnagraficaPagatore(Constants.CODICE_FISCALE_ANONIMO);
    }

    return carrelloFittizio;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateResultCarrelloRp(Carrello cart, NodoSILInviaRPRisposta nsir) throws DataAccessException, UnsupportedEncodingException, MalformedURLException {

    cart.setDeRpSilinviarpEsito(nsir.getEsito());
    if (!"OK".equals(nsir.getEsito()) && nsir.getFault() != null) {
      FaultBean fb = nsir.getFault();
      cart.setCodRpSilinviarpFaultCode(fb.getFaultCode());
      cart.setDeRpSilinviarpFaultString(fb.getFaultString());
      cart.setCodRpSilinviarpId(fb.getId());
      cart.setCodRpSilinviarpSerial(fb.getSerial());

      if (StringUtils.isNotBlank(fb.getDescription())) {
        String description = Utilities.getTruncatedAt(1024).apply(fb.getDescription());
        cart.setDeRpSilinviarpOriginalFaultDescription(description);
      }
      cart.setCodRpSilinviarpOriginalFaultCode(fb.getOriginalFaultCode());
      cart.setDeRpSilinviarpOriginalFaultString(fb.getOriginalFaultString());
      if (StringUtils.isNotBlank(fb.getOriginalDescription())) {
        String description = Utilities.getTruncatedAt(1024).apply(fb.getOriginalDescription());
        cart.setDeRpSilinviarpOriginalFaultDescription(description);
      }
      AnagraficaStato newStatus = anagraficaStatoService.getByCodStatoAndTipoStato(
          Constants.STATO_CARRELLO_IMPOSSIBILE_INVIARE_RP, Constants.STATO_TIPO_MULTI_CARRELLO);

      if (!cart.getMygovAnagraficaStatoId().getCodStato()
          .equals(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO)) {
        throw new DataRetrievalFailureException("Attempt to change status Carrello ["
            + cart.getIdSession() + "] from ["
            + cart.getMygovAnagraficaStatoId().getCodStato() + "] to [" + newStatus.getCodStato() + "]");
      }
      cart.setMygovAnagraficaStatoId(newStatus);
    }
    cart.setCodRpSilinviarpUrl(nsir.getUrl());

    String idSessionFESP = null;
    if (StringUtils.isNotBlank(nsir.getUrl())) {
      Map<String, String> parametersMap = Utilities.splitQuery(new URL(nsir.getUrl()));
      idSessionFESP = parametersMap.get("idSession");
    }
    cart.setIdSessionFesp(idSessionFESP);

    int updatedRec = carrelloDao.update(cart);
    if (updatedRec != 1) {
      throw new MyPayException("Carrello update internal error");
    }
    log.info("Carrello with Id: "+cart.getMygovCarrelloId()+" is up to date");
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Carrello updateEsito(String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento, Esito ctEsito) {
    Carrello cart = self.getByIdMessaggioRichiesta(ctEsito.getRiferimentoMessaggioRichiesta());
    cart.setCodESilinviaesitoIdDominio(identificativoDominio);
    cart.setCodESilinviaesitoIdUnivocoVersamento(identificativoUnivocoVersamento);
    cart.setCodESilinviaesitoCodiceContestoPagamento(codiceContestoPagamento);
    cart.setDeEVersioneOggetto(ctEsito.getVersioneOggetto());
    cart.setCodEDomIdDominio(ctEsito.getDominio().getIdentificativoDominio());
    cart.setCodEDomIdStazioneRichiedente(ctEsito.getDominio().getIdentificativoStazioneRichiedente());
    cart.setCodEIdMessaggioRicevuta(ctEsito.getIdentificativoMessaggioRicevuta());
    cart.setCodEDataOraMessaggioRicevuta(ctEsito.getDataOraMessaggioRicevuta().toGregorianCalendar().getTime());
    cart.setCodERiferimentoMessaggioRichiesta(ctEsito.getRiferimentoMessaggioRichiesta());
    cart.setCodERiferimentoDataRichiesta(ctEsito.getRiferimentoDataRichiesta().toGregorianCalendar().getTime());
    cart.setCodEIstitAttIdUnivAttCodiceIdUnivoco(
        ctEsito.getIstitutoAttestante().getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco());
    cart.setCodEIstitAttIdUnivAttTipoIdUnivoco(ctEsito.getIstitutoAttestante()
        .getIdentificativoUnivocoAttestante().getTipoIdentificativoUnivoco().toString().charAt(0));
    cart.setDeEIstitAttDenominazioneAttestante(ctEsito.getIstitutoAttestante().getDenominazioneAttestante());
    cart.setCodEIstitAttCodiceUnitOperAttestante(ctEsito.getIstitutoAttestante().getCodiceUnitOperAttestante());
    cart.setDeEIstitAttDenomUnitOperAttestante(ctEsito.getIstitutoAttestante().getDenomUnitOperAttestante());
    cart.setDeEIstitAttIndirizzoAttestante(ctEsito.getIstitutoAttestante().getIndirizzoAttestante());
    cart.setDeEIstitAttCivicoAttestante(ctEsito.getIstitutoAttestante().getCivicoAttestante());
    cart.setCodEIstitAttCapAttestante(ctEsito.getIstitutoAttestante().getCapAttestante());
    cart.setDeEIstitAttLocalitaAttestante(ctEsito.getIstitutoAttestante().getLocalitaAttestante());
    cart.setDeEIstitAttProvinciaAttestante(ctEsito.getIstitutoAttestante().getProvinciaAttestante());
    cart.setCodEIstitAttNazioneAttestante(ctEsito.getIstitutoAttestante().getNazioneAttestante());

    if (ctEsito.getSoggettoVersante() != null) {
      cart.setCodESoggVersIdUnivVersTipoIdUnivoco(ctEsito.getSoggettoVersante()
          .getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString().charAt(0));
      cart.setCodESoggVersIdUnivVersCodiceIdUnivoco(
          ctEsito.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());
      cart.setCodESoggVersAnagraficaVersante(ctEsito.getSoggettoVersante().getAnagraficaVersante());
      cart.setDeESoggVersIndirizzoVersante(ctEsito.getSoggettoVersante().getIndirizzoVersante());
      cart.setDeESoggVersCivicoVersante(ctEsito.getSoggettoVersante().getCivicoVersante());
      cart.setCodESoggVersCapVersante(ctEsito.getSoggettoVersante().getCapVersante());
      cart.setDeESoggVersLocalitaVersante(ctEsito.getSoggettoVersante().getLocalitaVersante());
      cart.setDeESoggVersProvinciaVersante(ctEsito.getSoggettoVersante().getProvinciaVersante());
      cart.setCodESoggVersNazioneVersante(ctEsito.getSoggettoVersante().getNazioneVersante());
      cart.setDeESoggVersEmailVersante(ctEsito.getSoggettoVersante().getEMailVersante());
    }

    cart.setCodESoggPagIdUnivPagTipoIdUnivoco(ctEsito.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
        .getTipoIdentificativoUnivoco().toString().charAt(0));
    cart.setCodESoggPagIdUnivPagCodiceIdUnivoco(ctEsito.getSoggettoPagatore().getIdentificativoUnivocoPagatore()
        .getCodiceIdentificativoUnivoco());
    cart.setCodESoggPagAnagraficaPagatore(ctEsito.getSoggettoPagatore().getAnagraficaPagatore());
    cart.setDeESoggPagIndirizzoPagatore(ctEsito.getSoggettoPagatore().getIndirizzoPagatore());
    cart.setDeESoggPagCivicoPagatore(ctEsito.getSoggettoPagatore().getCivicoPagatore());
    cart.setCodESoggPagCapPagatore(ctEsito.getSoggettoPagatore().getCapPagatore());
    cart.setDeESoggPagLocalitaPagatore(ctEsito.getSoggettoPagatore().getLocalitaPagatore());
    cart.setDeESoggPagProvinciaPagatore(ctEsito.getSoggettoPagatore().getProvinciaPagatore());
    cart.setCodESoggPagNazionePagatore(ctEsito.getSoggettoPagatore().getNazionePagatore());
    cart.setDeESoggPagEmailPagatore(ctEsito.getSoggettoPagatore().getEMailPagatore());

    cart.setCodEDatiPagCodiceEsitoPagamento(ctEsito.getDatiPagamento().getCodiceEsitoPagamento().charAt(0));

    cart.setNumEDatiPagImportoTotalePagato(ctEsito.getDatiPagamento().getImportoTotalePagato());
    cart.setCodEDatiPagIdUnivocoVersamento(ctEsito.getDatiPagamento().getIdentificativoUnivocoVersamento());
    cart.setCodEDatiPagCodiceContestoPagamento(ctEsito.getDatiPagamento().getCodiceContestoPagamento());

    // Settate al CARRELLO le informazioni sul BENEFICIARIO prensenti
    // nell'esito
    cart.setDeEEnteBenefDenominazioneBeneficiario(ctEsito.getEnteBeneficiario().getDenominazioneBeneficiario());
    cart.setDeEEnteBenefCivicoBeneficiario(ctEsito.getEnteBeneficiario().getCivicoBeneficiario());
    cart.setDeEEnteBenefDenomUnitOperBeneficiario(ctEsito.getEnteBeneficiario().getDenomUnitOperBeneficiario());
    cart.setDeEEnteBenefIndirizzoBeneficiario(ctEsito.getEnteBeneficiario().getIndirizzoBeneficiario());
    cart.setDeEEnteBenefLocalitaBeneficiario(ctEsito.getEnteBeneficiario().getLocalitaBeneficiario());
    cart.setDeEEnteBenefProvinciaBeneficiario(ctEsito.getEnteBeneficiario().getProvinciaBeneficiario());
    cart.setCodEEnteBenefCapBeneficiario(ctEsito.getEnteBeneficiario().getCapBeneficiario());
    cart.setCodEEnteBenefCodiceUnitOperBeneficiario(
        ctEsito.getEnteBeneficiario().getCodiceUnitOperBeneficiario());
    cart.setCodEEnteBenefIdUnivBenefCodiceIdUnivoco(
        ctEsito.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco());
    Optional.ofNullable(ctEsito.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario()
        .getTipoIdentificativoUnivoco().toString().charAt(0)).ifPresent(cart::setCodEEnteBenefIdUnivBenefTipoIdUnivoco);
    cart.setCodEEnteBenefNazioneBeneficiario(ctEsito.getEnteBeneficiario().getNazioneBeneficiario());
    cart.setDtUltimaModificaE(new Date());

    Integer codEDatiPagCodiceEsitoPagamento = Integer.parseInt(cart.getCodEDatiPagCodiceEsitoPagamento().toString());
    AnagraficaStato nuovoStatoCarrello = anagraficaStatoService.getByCodiceEsitoPagamento(codEDatiPagCodiceEsitoPagamento, Constants.STATO_TIPO_CARRELLO);

    if (!cart.getMygovAnagraficaStatoId().getCodStato().equals(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO)) {
      throw new DataRetrievalFailureException("Tentativo di cambio stato Carrello ["
          + cart.getCodRpDatiVersIdUnivocoVersamento() + "] da ["
          + cart.getMygovAnagraficaStatoId().getCodStato() + "] in [" + nuovoStatoCarrello.getCodStato() + "].");
    }
    cart.setMygovAnagraficaStatoId(nuovoStatoCarrello);

    return self.upsert(cart);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void delete(Long mygovCarrelloId) {
    int deletedRec = carrelloDao.delete(mygovCarrelloId);
    if (deletedRec != 1) {
      throw new MyPayException("Carrello delete internal error");
    }
    log.info("Carrello with id: "+mygovCarrelloId+" is deleted");
  }

  public Carrello getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamento(String idDominio, String iuv, String codiceContestoPagamento) {
    return getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamento(idDominio, iuv, codiceContestoPagamento, false);
  }

  public Carrello getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamento(String idDominio, String iuv, String codiceContestoPagamento, boolean noJoin) {
    List<Carrello> items;
    if(noJoin)
      items = carrelloDao.getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamentoNoJoin(idDominio, iuv, codiceContestoPagamento);
    else
      items = carrelloDao.getByIdDominioIdUnivocoPagamentoAndCodiceContestoPagamento(idDominio, iuv, codiceContestoPagamento);
    if (CollectionUtils.isEmpty(items)) {
      return null;
    } else if (items.size() > 1) {
      throw new MyPayException(messageSource.getMessage("pa.carrello.carrelloDuplicato", null, Locale.ITALY));
    }
    return items.get(0);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updatePaaSILInviaEsitoRisposta(Long mygovCarrelloId, PaaSILInviaEsitoRisposta paaSILInviaEsitoRisposta) {
    Carrello cart = self.getById(mygovCarrelloId);
    if (cart == null) {
      return;
    }
    cart.setDeESilinviaesitoEsito(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getEsito());
    if (paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault() != null) {
      cart.setCodESilinviaesitoFaultCode(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getFaultCode());
      cart.setDeESilinviaesitoFaultString(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getFaultString());
      cart.setCodESilinviaesitoId(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getId());

      if (StringUtils.isNotBlank(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getDescription())) {
        String description = paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getDescription();
        cart.setDeESilinviaesitoDescription(Utilities.getTruncatedAt(1024).apply(description));
      }
      cart.setCodESilinviaesitoSerial(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getSerial());
      cart.setCodESilinviaesitoOriginalFaultCode(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getOriginalFaultCode());
      cart.setDeESilinviaesitoOriginalFaultString(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getOriginalFaultString());

      if (StringUtils.isNotBlank(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getOriginalDescription())) {
        String description = paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getFault().getOriginalDescription();
        cart.setDeESilinviaesitoOriginalFaultDescription(Utilities.getTruncatedAt(1024).apply(description));
      }
    }
    int updatedRec = carrelloDao.update(cart);
    if (updatedRec != 1) {
      throw new MyPayException("Carrello update internal error");
    }
    log.info("Carrello with id: "+cart.getMygovCarrelloId()+" is up to date");
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateFlgNotificaEsitoByIdMessaggioRichiesta(String idMessaggioRichiesta, Boolean flg) {
    Assert.notNull(idMessaggioRichiesta, "Id Messaggio Richiesta nullo");
    int updatedRec = carrelloDao.updateFlgNotificaEsitoByIdMessaggioRichiesta(idMessaggioRichiesta, flg);

    if (updatedRec != 1)
      throw new MyPayException("Carrello update internal error ["+updatedRec+"]");
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Carrello update(Carrello carrello) {
    int updatedRec = carrelloDao.update(carrello);
    if (updatedRec != 1) {
      throw new MyPayException("Carrello update internal error");
    }
    return carrello;
  }

  private Long statoCarrelloPagamentoInCorso = null;

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public List<Carrello> getByStatePagamentoInCorso(int modelloPagamento){
    Assert.isTrue(Carrello.VALID_MODELLOPAGAMENTO.contains(modelloPagamento), "invalid value for modelloPagamento");

    int deltaMinutes;
    try {
      String deltaMinutesString = env.getProperty("task.chiediCopiaEsito.minutiScadenzaControlloCarrello."+modelloPagamento);
      deltaMinutes = Integer.parseInt(deltaMinutesString);
      Assert.isTrue(deltaMinutes > 0, "invalid value for deltaMinutes");
    } catch (Exception e){
      throw new MyPayException("invalid value for deltaMinutes", e);
    }

    if(statoCarrelloPagamentoInCorso==null) {
      statoCarrelloPagamentoInCorso = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_CARRELLO_PAGAMENTO_IN_CORSO, Constants.STATO_TIPO_CARRELLO).getMygovAnagraficaStatoId();
    }

    return carrelloDao.getByStatePagamentoInCorso(modelloPagamento, deltaMinutes, statoCarrelloPagamentoInCorso);
  }

  @Cacheable(value = CacheService.CACHE_NAME_CHIEDI_COPIA_ESITO, key = "#mygovCarrelloId", unless="#result==null")
  public String getProcessingInfoChiediCopiaEsito(Long mygovCarrelloId){
    return null;
  }

  @CachePut(value = CacheService.CACHE_NAME_CHIEDI_COPIA_ESITO, key = "#mygovCarrelloId")
  public String updateProcessingInfoChiediCopiaEsito(Long mygovCarrelloId, int numChecks){
    return numChecks+"|"+System.currentTimeMillis();
  }

  @CacheEvict(value = CacheService.CACHE_NAME_CHIEDI_COPIA_ESITO, key = "#mygovCarrelloId")
  public void deleteProcessingInfoChiediCopiaEsito(Long mygovCarrelloId){}

  @Transactional(propagation = Propagation.MANDATORY)
  public List<Carrello> getWithEsitoAndNotMailSent(){
    return carrelloDao.getWithEsitoAndNotMailSent(invioEmailEsitoBatchRowLimit);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public List<Carrello> getOlderByTypeAndState(int minutes, String type, Long mygovAnagraficaStatoId){
    return carrelloDao.getOlderByTypeAndState(minutes, type, mygovAnagraficaStatoId, scadenzaCarrelloBatchRowLimit);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int updateStato(Long mygovCarrelloId, Long mygovAnagraficaStatoId){
    return carrelloDao.updateStato(mygovCarrelloId, mygovAnagraficaStatoId);
  }
}