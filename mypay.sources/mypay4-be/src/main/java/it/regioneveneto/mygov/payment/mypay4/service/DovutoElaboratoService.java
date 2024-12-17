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

import it.regioneveneto.mygov.payment.mypay4.dao.DovutoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoElaboratoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoMultibeneficiarioDao;
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoMultibeneficiarioElaboratoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.DatiRendicontazioneCod9;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoElaboratoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoOperatoreTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.DatiMarcaBolloDigitale;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoCarrello;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoFunctionOut;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoMultibeneficiario;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoMultibeneficiarioElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Flusso;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.GpdService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.ListWithCount;
import it.regioneveneto.mygov.payment.mypay4.util.MaxResultsHelper;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.util.SumUtilis;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsitoRisposta;
import it.veneto.regione.schemas._2012.pagamenti.CtDatiSingoloPagamentoEsito;
import it.veneto.regione.schemas._2012.pagamenti.Esito;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.statement.OutParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DovutoElaboratoService {

  @Autowired
  private DovutoElaboratoDao dovutoElaboratoDao;
  @Autowired
  private DatiMarcaBolloDigitaleService datiMarcaBolloDigitaleService;
  @Autowired
  private DovutoDao dovutoDao;

  @Autowired
  private DovutoMultibeneficiarioDao dovutoMultibeneficiarioDao;
  @Autowired
  private DovutoMultibeneficiarioElaboratoDao dovutoMultibeneficiarioElaboratoDao;
  @Autowired
  private DovutoCarrelloService dovutoCarrelloService;
  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  private FlussoService flussoService;

  @Autowired
  private CarrelloService carrelloService;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private MaxResultsHelper maxResultsHelper;

  @Autowired(required = false)
  private GpdService gpdService;

  @Value("${pa.deRpVersioneOggetto:6.2.0}")
  private String deRpVersioneOggetto;

  @Value("${pa.gpd.enabled}")
  private boolean gpdEnabled;

  @Transactional(propagation = Propagation.REQUIRED)
  public DovutoElaborato upsert(DovutoElaborato dovutoElaborato) {
    if (dovutoElaborato.getMygovDovutoElaboratoId() == null || this.getById(dovutoElaborato.getMygovDovutoElaboratoId()) == null) {
      long mygovDovutoElaboratoId = dovutoElaboratoDao.insert(dovutoElaborato);
      dovutoElaborato.setMygovDovutoElaboratoId(mygovDovutoElaboratoId);
    } else {
      dovutoElaboratoDao.update(dovutoElaborato);
    }
    return dovutoElaborato;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public DovutoElaborato getById(Long id) {
    return dovutoElaboratoDao.getById(id);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public long count(String codIpaEnte, String codTipoDovuto) {
    return dovutoElaboratoDao.count(codIpaEnte, codTipoDovuto);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoElaborato> getByCarrello(Carrello carrello) {
    List<DovutoElaborato> dovuti = dovutoElaboratoDao.getByCarrelloId( carrello.getMygovCarrelloId() );
    dovuti.forEach(d ->
      d.setMygovCarrelloId( ObjectUtils.firstNonNull(ObjectUtils.cloneIfPossible(carrello), carrello) ) );
    return dovuti;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoElaborato> getByCarrelloId(Long carrelloId) {
    List<DovutoElaborato> dovuti = dovutoElaboratoDao.getByCarrelloId(carrelloId);
    dovuti.forEach(d ->
      d.setMygovCarrelloId( Optional.ofNullable(d.getMygovCarrelloId()).map(c -> carrelloService.getById(c.getMygovCarrelloId())).orElse(null) ) );
    return dovuti;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoElaborato> getByIuvEnte(String iuv, String codIpaEnte) {
    Long idStatoDovutoAnnullato = anagraficaStatoService.getIdByTipoAndCode(Constants.STATO_TIPO_DOVUTO,"ANNULLATO");
    List<DovutoElaborato> dovuti = dovutoElaboratoDao.getByIuvEnte(iuv, codIpaEnte, idStatoDovutoAnnullato);
    dovuti.forEach(d ->
      d.setMygovCarrelloId( Optional.ofNullable(d.getMygovCarrelloId()).map(c -> carrelloService.getById(c.getMygovCarrelloId())).orElse(null) ) );
    return dovuti;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoElaborato> getByIudEnte(String iud, String codIpaEnte) {
    List<DovutoElaborato> dovuti = dovutoElaboratoDao.getByIudEnte(iud, codIpaEnte);
    dovuti.forEach(d ->
      d.setMygovCarrelloId( Optional.ofNullable(d.getMygovCarrelloId()).map(c -> carrelloService.getById(c.getMygovCarrelloId())).orElse(null) ) );
    return dovuti;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoElaborato> getByIuvEnteStato(String iuv, String codIpaEnte, String codStato, boolean inStato, Long carrelloId) {
    List<DovutoElaborato> dovuti = dovutoElaboratoDao.getByIuvEnteStato(iuv, codIpaEnte, codStato, inStato, carrelloId);
    dovuti.forEach(d ->
      d.setMygovCarrelloId( Optional.ofNullable(d.getMygovCarrelloId()).map(c -> carrelloService.getById(c.getMygovCarrelloId())).orElse(null) ) );
    return dovuti;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoElaborato> getByIdUnivocoPersonaEnteIntervalloData(String tipoIdUnivoco, String idUnivoco, String codIpaEnte, Date from, Date to) {
    List<DovutoElaborato> dovuti = dovutoElaboratoDao.getByIdUnivocoPersonaEnteIntervalloData(tipoIdUnivoco, idUnivoco, codIpaEnte, from, to);
    //dovuti.forEach(d ->
    //  d.setMygovCarrelloId( Optional.ofNullable(d.getMygovCarrelloId()).map(c -> carrelloService.getById(c.getMygovCarrelloId())).orElse(null) ) );
    return dovuti;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public DovutoElaborato getByIdForOperator(Long mygovEnteId, String username, Long id) {
    List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
    List<String> listCodTipoDovuto = enteTipoDovutoOfOperator.stream().map(EnteTipoDovuto::getCodTipo).collect(Collectors.toList());
    DovutoElaborato dovuto = dovutoElaboratoDao.getById(id);
    return listCodTipoDovuto.contains(dovuto.getCodTipoDovuto()) ? dovuto : null;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public ListWithCount<DovutoElaboratoTo> searchDovutoElaborato(String codIpaEnte,
                                                                String idUnivocoPagatoreVersante, LocalDate from, LocalDate to,
                                                                String causale, String codTipoDovuto, String codStato) {

    final String codStatoPagamento;
    final String importoCod;
    if (codStato != null)
      switch (codStato) {
        case "annullato":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_ANNULLATO;
          importoCod = null;
          break;
        case "pagato":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_COMPLETATO;
          importoCod = "gtZero";
          break;
        case "nonPagato":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_COMPLETATO;
          importoCod = "eqZero";
          break;
        case "abortito":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_ABORT;
          importoCod = null;
          break;
        case "scaduto":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_SCADUTO_ELABORATO;
          importoCod = null;
          break;
        default:
          codStatoPagamento = null;
          importoCod = null;
      }
    else {
      codStatoPagamento = null;
      importoCod = null;
    }

    ListWithCount<DovutoElaboratoTo> payload = maxResultsHelper.manageMaxResults(
            maxResults -> dovutoElaboratoDao.searchDovutoElaborato(codIpaEnte,
                    idUnivocoPagatoreVersante, from, to, causale, codTipoDovuto,
                    codStatoPagamento, importoCod, maxResults),
            this::mapDovutoElaboratoToDto,
            () -> dovutoElaboratoDao.searchDovutoElaboratoCount(codIpaEnte,
                    idUnivocoPagatoreVersante, from, to, causale, codTipoDovuto,
                    codStatoPagamento, importoCod) );

    // Retrieve information from Dovuto Ente primario and Dovuto multibeneficiario
    if(!payload.isEmpty())
      dovutoMultibeneficiarioElaboratoDao.getListDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(
        payload.stream().map(DovutoElaboratoTo::getId).collect(Collectors.toList()))
        .forEach(dme ->
          payload.stream().filter(de -> dme.getIdDovutoElaborato().equals(de.getId())).forEach( de ->
            de.setImporto(SumUtilis.sumAmountPagati(de.getImporto(), dme.getImportoSecondario())) ) );

    return payload;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public Optional<Boolean> hasReplicaDovutoElaborato(String codIpaEnte, char tipoIdUnivocoPagatoreVersante,
                                                     String idUnivocoPagatoreVersante, String causale, String codTipoDovuto) {
    if(StringUtils.isBlank(codIpaEnte) || StringUtils.isBlank(idUnivocoPagatoreVersante)
            || StringUtils.isBlank(causale) || StringUtils.isBlank(codTipoDovuto))
      return Optional.empty();
    else
      return dovutoElaboratoDao.hasReplicaDovutoElaborato(codIpaEnte, tipoIdUnivocoPagatoreVersante, idUnivocoPagatoreVersante, causale, codTipoDovuto);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoElaborato> searchDovutoElaboratoByIuvIdPagatore(Long mygovEnteId, String iuv, String idUnivocoPagatore, String anagraficaPagatore) {
    return dovutoElaboratoDao.searchDovutoElaboratoByIuvIdPagatore(mygovEnteId, StringUtils.upperCase(iuv), idUnivocoPagatore, anagraficaPagatore);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoElaborato> searchDovutoElaboratoByIuvEnte(String iuv, String codIpaEnte) {
    return dovutoElaboratoDao.searchDovutoElaboratoByIuvEnte(StringUtils.upperCase(iuv), codIpaEnte);
  }


  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoOperatoreTo> searchDovutoElaboratoForOperatore(String username, Long mygovEnteId, String codStato, Long myGovEnteTipoDovutoId,
                                                                   String nomeFlusso, LocalDate from, LocalDate to, String codiceFiscale,
                                                                   String causale, String codIud, String codIuv) {

    List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
    List<EnteTipoDovuto> enteTipoDovutoFiltered;
    if (myGovEnteTipoDovutoId != null) {
      //check if operatore is authorized for tipoDovuto
      enteTipoDovutoFiltered = enteTipoDovutoOfOperator.stream().filter(etd -> etd.getMygovEnteTipoDovutoId().equals(myGovEnteTipoDovutoId)).collect(Collectors.toList());
      if (enteTipoDovutoFiltered.isEmpty())
        return null;
    } else {
      enteTipoDovutoFiltered = enteTipoDovutoOfOperator;
    }

    List<String> listCodTipoDovuto = enteTipoDovutoFiltered.stream().map(EnteTipoDovuto::getCodTipo).collect(Collectors.toList());

    final String codStatoPagamento;
    final String importoCod;
    if (codStato != null)
      switch (codStato) {
        case "annullato":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_ANNULLATO;
          importoCod = null;
          break;
        case "pagato":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_COMPLETATO;
          importoCod = "gtZero";
          break;
        case "nonPagato":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_COMPLETATO;
          importoCod = "eqZero";
          break;
        case "abortito":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_ABORT;
          importoCod = null;
          break;
        case "scaduto":
          codStatoPagamento = AnagraficaStato.STATO_DOVUTO_SCADUTO_ELABORATO;
          importoCod = null;
          break;
        default:
          codStatoPagamento = null;
          importoCod = null;
      }
    else {
      codStatoPagamento = null;
      importoCod = null;
    }
    final LocalDate adjustedTo = to.plusDays(1);

    List<DovutoOperatoreTo> payload = maxResultsHelper.manageMaxResults(
            maxResults -> dovutoElaboratoDao.searchDovutoElaboratoNellArchivio(mygovEnteId, codStatoPagamento, importoCod,
                    listCodTipoDovuto, nomeFlusso, from, adjustedTo, codiceFiscale, causale, codIud, StringUtils.upperCase(codIuv), maxResults),
            () -> dovutoElaboratoDao.searchDovutoElaboratoNellArchivioCount(mygovEnteId, codStatoPagamento, importoCod,
                    listCodTipoDovuto, nomeFlusso, from, adjustedTo, codiceFiscale, causale, codIud, StringUtils.upperCase(codIuv)) );

    if(!payload.isEmpty()) {
      List<Long> ids = payload.stream().map(DovutoOperatoreTo::getId).collect(Collectors.toList());
      // retrieve list of enti primari by ids dovuti
      dovutoElaboratoDao.getListInfoEntePrimarioByIdDovuto(ids).forEach(deep ->
        payload.stream().filter(d -> deep.getIdDovutoElaborato().equals(d.getId())).forEach(d -> d.setEntePrimarioElaboratoDetail(deep))
      );
      dovutoMultibeneficiarioElaboratoDao.getListDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(ids).forEach(dme ->
        payload.stream().filter(d -> dme.getIdDovutoElaborato().equals(d.getId())).forEach(d -> {
          d.setImporto(SumUtilis.sumAmount(d.getImporto(), dme.getImportoSecondario()));
          d.setDovutoMultibeneficiarioElaborato(dme);
          d.setFlgMultibeneficiario(true);
        }) );
    }

    return payload;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public DovutoOperatoreTo getDetailsForOperatore(String username, Long mygovEnteId, Long mygovPagatoId) {
    throw new UnsupportedOperationException("TO IMPLEMENT");
    //List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
    //List<String> listCodTipoDovuto = enteTipoDovutoOfOperator.stream().map(enteTipoDovuto -> enteTipoDovuto.getCodTipo()).collect(Collectors.toList());
    //return dovutoDao.getDoutoDetailsOperatore(listCodTipoDovuto, mygovDovutoId, "true");
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<DovutoElaboratoTo> searchLastDovutoElaborato(String idUnivocoPagatoreVersante, Integer num) {
    List<DovutoElaboratoTo> result = dovutoElaboratoDao.searchLastDovutoElaborato(idUnivocoPagatoreVersante, num)
            .stream().map(this::mapDovutoElaboratoToDto).collect(Collectors.toList());

    //retrieve fields for dovuto multi-beneficiario
    if(!result.isEmpty())
      dovutoMultibeneficiarioElaboratoDao.getListDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(
        result.stream().map(DovutoElaboratoTo::getId).collect(Collectors.toList())).forEach(mb ->
          result.stream().filter(d -> d.getId()==mb.getIdDovutoElaborato()).forEach(d -> {
            d.setMultibeneficiario(true);
            d.setImporto(SumUtilis.sumAmountPagati(d.getImporto(), mb.getImportoSecondario()));
          }) );

    return result;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public DovutoElaborato getByIdAndUser(Long id, char tipoIdUnivocoPagatoreVersante, String idUnivocoPagatoreVersante){
    return this.dovutoElaboratoDao.getByIdAndUser(id, tipoIdUnivocoPagatoreVersante, idUnivocoPagatoreVersante);
  }

  public DovutoElaboratoTo mapDovutoElaboratoToDto(DovutoElaborato pagato) {
    DovutoElaboratoTo pagatoDto = new DovutoElaboratoTo();

    pagatoDto.setId(pagato.getMygovDovutoElaboratoId());
    if (StringUtils.isNotBlank(pagato.getDeRpDatiVersDatiSingVersCausaleVersamentoAgid()) &&
            pagato.getDeRpDatiVersDatiSingVersCausaleVersamentoAgid().contains("/TXT/")) {
      String causaleAgid = pagato.getDeRpDatiVersDatiSingVersCausaleVersamentoAgid();
      pagatoDto.setCausale(causaleAgid.substring(causaleAgid.indexOf("/TXT/")+5));
    }
    else {
      pagatoDto.setCausale(pagato.getDeRpDatiVersDatiSingVersCausaleVersamento());
    }
    pagatoDto.setValuta("EUR");

    pagatoDto.setDataScadenza( Utilities.toLocalDate(pagato.getDtRpDatiVersDataEsecuzionePagamento()) );
    pagatoDto.setDataPagamento( Utilities.toLocalDate(pagato.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento()) );

    pagatoDto.setCodStato(pagato.getMygovAnagraficaStatoId().getCodStato());
    pagatoDto.setStato(pagato.getMygovAnagraficaStatoId().getDeStato());

    String statoComplessivo;
    if (!pagato.getMygovAnagraficaStatoId().getCodStato().equals(AnagraficaStato.STATO_DOVUTO_COMPLETATO)) {
      statoComplessivo = pagato.getMygovAnagraficaStatoId().getDeStato();
    }
    else {
      if (pagato.getNumEDatiPagDatiSingPagSingoloImportoPagato().compareTo(BigDecimal.ZERO) == 0) {
        statoComplessivo = anagraficaStatoService.getByCodStatoAndTipoStato(AnagraficaStato.STATO_CARRELLO_NON_PAGATO, AnagraficaStato.STATO_TIPO_CARRELLO)
                .getDeStato();
      }
      else {
        statoComplessivo = anagraficaStatoService.getByCodStatoAndTipoStato(AnagraficaStato.STATO_CARRELLO_PAGATO, AnagraficaStato.STATO_TIPO_CARRELLO).getDeStato();

        if (pagato.getNumEDatiPagDatiSingPagCommissioniApplicatePsp() != null) {
          pagatoDto.setCommissioniApplicatePsp(Utilities.parseImportoString(pagato.getNumEDatiPagDatiSingPagCommissioniApplicatePsp()));
        }
        if (StringUtils.isNotBlank(pagato.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo())) {
          pagatoDto.setAllegatoRicevutaCodiceTipo(pagato.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo());
          pagatoDto.setAllegatoRicevutaTipo(messageSource.getMessage("pa.debitoDettaglio.allegatoRicevutaTipo", new Object[]{pagato.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo()}, Locale.ITALY));
          if (pagato.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo().equals("ES")) {
            pagatoDto.setAllegatoRicevutaTest(new String(pagato.getBlbEDatiPagDatiSingPagAllegatoRicevutaTest(), StandardCharsets.UTF_8));
          }
        }
      }

      //0000106: Aggiunte informazioni transazioni chiuse nel dettaglio pagato
      pagatoDto.setDataInizioTransazione( Utilities.toLocalDateTime(pagato.getDtUltimaModificaRp()) );

      pagatoDto.setIdentificativoTransazione(pagato.getCodRpSilinviarpIdUnivocoVersamento() + " - " + pagato.getCodRpSilinviarpCodiceContestoPagamento());
      pagatoDto.setIntestatario(pagato.getDeRpSoggPagAnagraficaPagatore() + " - " + pagato.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
      pagatoDto.setEmail(pagatoDto.getEmail());

      if (StringUtils.isNotBlank(pagato.getDeEIstitAttDenominazioneAttestante())){
        if (StringUtils.isNotBlank(pagato.getCodEIstitAttIdUnivAttCodiceIdUnivoco())){
          pagatoDto.setPspScelto(pagato.getDeEIstitAttDenominazioneAttestante()
                  + " (" + pagato.getCodEIstitAttIdUnivAttCodiceIdUnivoco() + ")");
        }
        else {
          pagatoDto.setPspScelto(pagato.getDeEIstitAttDenominazioneAttestante());
        }
      }
      else if (StringUtils.isNotBlank(pagato.getCodEIstitAttIdUnivAttCodiceIdUnivoco())){
        pagatoDto.setPspScelto(pagato.getCodEIstitAttIdUnivAttCodiceIdUnivoco());
      } else {
        pagatoDto.setPspScelto("");
      }
    }

    pagatoDto.setStatoComplessivo(statoComplessivo);

    if (statoComplessivo.equals(AnagraficaStato.STATO_CARRELLO_PAGATO)) {
      pagatoDto.setImporto(Utilities.parseImportoString(pagato.getNumEDatiPagDatiSingPagSingoloImportoPagato()));
      pagatoDto.setImportoAsCent(Utilities.amountAsEuroCents(pagato.getNumEDatiPagDatiSingPagSingoloImportoPagato()));
    }
    else {
      pagatoDto.setImporto(Utilities.parseImportoString(pagato.getNumRpDatiVersDatiSingVersImportoSingoloVersamento()));
      pagatoDto.setImportoAsCent(Utilities.amountAsEuroCents(pagato.getNumRpDatiVersDatiSingVersImportoSingoloVersamento()));
    }

    pagatoDto.setModPagamento(pagato.getCodRpDatiVersTipoVersamento());

    pagatoDto.setShowStampaRicevutaButton(!StringUtils.startsWith(pagato.getCodEIdMessaggioRicevuta(), ("###")));

    if(pagato.getNestedEnte()!=null) {
      pagatoDto.setEnteId(pagato.getNestedEnte().getMygovEnteId());
      pagatoDto.setEnteDeNome(pagato.getNestedEnte().getDeNomeEnte());
      pagatoDto.setCodIpaEnte(pagato.getNestedEnte().getCodIpaEnte());
      pagatoDto.setCodFiscaleEnte(pagato.getNestedEnte().getCodiceFiscaleEnte());
    }

    pagatoDto.setCodTipoDovuto(pagato.getCodTipoDovuto());
    pagatoDto.setDeTipoDovuto(
            enteTipoDovutoService.getOptionalByCodTipo(pagato.getCodTipoDovuto(), pagato.getNestedEnte().getCodIpaEnte(), false)
                    .map(EnteTipoDovuto::getDeTipo).orElse(pagato.getCodTipoDovuto()));

    pagatoDto.setCodIuv(pagato.getValidIuv());
    if(StringUtils.isNotBlank(pagatoDto.getCodIuv()) && pagato.getNestedEnte()!=null)
      pagatoDto.setNumeroAvviso(Utilities.iuvToNumeroAvviso(pagatoDto.getCodIuv(), pagato.getNestedEnte().getApplicationCode(), false));

    return pagatoDto;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long elaborateDovuto(Dovuto dovuto, String vecchioStato, String nuovoStato) {

    if(!Optional.ofNullable(dovuto)
            .map(Dovuto::getMygovAnagraficaStatoId)
            .map(AnagraficaStato::getCodStato)
            .filter(Predicate.isEqual(vecchioStato))
            .isPresent())
      throw new MyPayException(new MyPayException(messageSource.getMessage("pa.dovuto.optimisticLockErrorAnnullamento", null, Locale.ITALY)));

    // Clone dovutoElaborato fron dovuto;
    DovutoElaborato dovutoElaborato = new DovutoElaborato();
    dovutoElaborato.setMygovAnagraficaStatoId(
            anagraficaStatoService.getByCodStatoAndTipoStato(nuovoStato, Constants.STATO_TIPO_DOVUTO));

    dovutoElaborato.setFlgDovutoAttuale(dovuto.isFlgDovutoAttuale());
    dovutoElaborato.setMygovFlussoId(dovuto.getMygovFlussoId());
    dovutoElaborato.setNumRigaFlusso(dovuto.getNumRigaFlusso());
    dovutoElaborato.setMygovCarrelloId(dovuto.getMygovCarrelloId());
    dovutoElaborato.setCodIud(dovuto.getCodIud());
    dovutoElaborato.setCodIuv(dovuto.getCodIuv());
    dovutoElaborato.setDtCreazione(new Date());

    // copio campi da dovuto, se non e' annullamento poi questi campi
    // vengono sovrascritti dagli stessi campi nel carrello
    dovutoElaborato.setCodRpSoggPagIdUnivPagTipoIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco());
    dovutoElaborato.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
    dovutoElaborato.setDeRpSoggPagAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore());
    dovutoElaborato.setDeRpSoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
    dovutoElaborato.setDeRpSoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
    dovutoElaborato.setCodRpSoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore());
    dovutoElaborato.setDeRpSoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore());
    dovutoElaborato.setDeRpSoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore());
    dovutoElaborato.setCodRpSoggPagNazionePagatore(dovuto.getCodRpSoggPagNazionePagatore());
    dovutoElaborato.setDeRpSoggPagEmailPagatore(dovuto.getDeRpSoggPagEmailPagatore());

    dovutoElaborato.setDtRpDatiVersDataEsecuzionePagamento(dovuto.getDtRpDatiVersDataEsecuzionePagamento());
    dovutoElaborato.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(
            dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento());
    dovutoElaborato.setNumRpDatiVersDatiSingVersCommissioneCaricoPa(
            dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa());

    dovutoElaborato.setCodTipoDovuto(dovuto.getCodTipoDovuto());

    // CREDNZIALI PER DOVUTO
    dovutoElaborato.setCodRpDatiVersDatiSingVersCredenzialiPagatore(null);

    dovutoElaborato
            .setDeRpDatiVersDatiSingVersCausaleVersamento(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento());
    dovutoElaborato.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(
            dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione());

    dovutoElaborato.setCodTipoDovuto(dovuto.getCodTipoDovuto());
    Optional.ofNullable(dovuto.getBilancio()).ifPresent(dovutoElaborato::setBilancio);
    dovutoElaborato.setDtUltimoCambioStato(new Date());
    dovutoElaborato.setDeRpVersioneOggetto(deRpVersioneOggetto);
    dovutoElaborato.setGpdStatus(dovuto.getGpdStatus());
    dovutoElaborato.setGpdIupd(dovuto.getGpdIupd());

    long mygovDovutoElaboratoId = dovutoElaboratoDao.insert(dovutoElaborato);
    /**Multi-beneficiary IUV management if defined**/
    DovutoMultibeneficiario dovMultibenef = dovutoMultibeneficiarioDao.getByIdDovuto(dovuto.getMygovDovutoId());
    int deletedRecMulti = (dovMultibenef!=null) ? dovutoMultibeneficiarioDao.delete(dovMultibenef) : 0;
    int deletedRec = dovutoDao.delete(dovuto);

    //todo nell'annullare il dovuto associare al mygov_dovuto_elaborato (annullato) anche un record della mygov_dovuto_multibeneficiario_elaborato

    if (mygovDovutoElaboratoId < 1 || deletedRec != 1 || (dovMultibenef!=null && deletedRecMulti != 1))
      throw new MyPayException("Errore interno aggiornamento  dovuto e/o dovutoElaborato");
    return mygovDovutoElaboratoId;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void elaborateDovutoNoCompletato(long idDovuto, DovutoElaborato dovutoElaborato, String vecchioStato, String nuovoStato, Ente ente, DatiRendicontazioneCod9 datiRendicontazioneCod9)
          throws DataAccessException {

    Dovuto dovuto = dovutoDao.getById(idDovuto);
    if(!Optional.ofNullable(dovuto)
            .map(Dovuto::getMygovAnagraficaStatoId)
            .map(AnagraficaStato::getCodStato)
            .filter(Predicate.isEqual(vecchioStato))
            .isPresent())
      throw new MyPayException(new MyPayException(messageSource.getMessage("pa.dovuto.optimisticLockErrorAnnullamento", null, Locale.ITALY)));

    dovutoElaborato.setMygovDovutoElaboratoId(null);
    dovutoElaborato.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(nuovoStato, Constants.STATO_TIPO_DOVUTO));

    copiaCampiDovutoRend9(dovuto, dovutoElaborato, datiRendicontazioneCod9, ente);
    dovutoElaborato.setGpdStatus(dovuto.getGpdStatus());
    dovutoElaborato.setGpdIupd(dovuto.getGpdIupd());
    dovutoElaborato.setDtUltimoCambioStato(new Date());

    this.upsert(dovutoElaborato);
    dovutoDao.delete(dovuto);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void elaborateDovutoNoCompletato(DovutoElaborato dovutoElaborato, String statoDovutoCompletato, Ente ente,
                                          DatiRendicontazioneCod9 datiRendicontazioneCod9) {

    dovutoElaborato.setMygovDovutoElaboratoId(null);
    dovutoElaborato.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(statoDovutoCompletato, Constants.STATO_TIPO_DOVUTO));

    copiaCampiDovutoRend9(null, dovutoElaborato, datiRendicontazioneCod9, ente);
    dovutoElaborato.setDtUltimoCambioStato(new Date());

    this.upsert(dovutoElaborato);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public DovutoElaborato insert(final long idDovuto, final String vecchioStato, final String nuovoStato, final Ente ente, final DatiRendicontazioneCod9 datiRendicontazioneCod9)
          throws DataAccessException {

    Dovuto dovuto = dovutoDao.getById(idDovuto);
    if(!Optional.ofNullable(dovuto)
            .map(Dovuto::getMygovAnagraficaStatoId)
            .map(AnagraficaStato::getCodStato)
            .filter(Predicate.isEqual(vecchioStato))
            .isPresent())
      throw new MyPayException(new MyPayException(messageSource.getMessage("pa.dovuto.optimisticLockErrorAnnullamento", null, Locale.ITALY)));

    DovutoElaborato dovutoElaborato = new DovutoElaborato();
    dovutoElaborato.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(nuovoStato, Constants.STATO_TIPO_DOVUTO));

    copiaCampiDovutoRend9(dovuto, dovutoElaborato, datiRendicontazioneCod9, ente);

    dovutoElaborato.setDtUltimoCambioStato(new Date());

    dovutoElaborato.setDeRpVersioneOggetto(deRpVersioneOggetto);

    dovutoElaborato.setFlgDovutoAttuale(dovuto.isFlgDovutoAttuale());
    dovutoElaborato.setMygovFlussoId(dovuto.getMygovFlussoId());
    dovutoElaborato.setNumRigaFlusso(dovuto.getNumRigaFlusso());
    dovutoElaborato.setMygovCarrelloId(dovuto.getMygovCarrelloId());
    dovutoElaborato.setCodIud(dovuto.getCodIud());
    dovutoElaborato.setGpdStatus(dovuto.getGpdStatus());
    dovutoElaborato.setGpdIupd(dovuto.getGpdIupd());

    long mygovDovutoElaboratoId = dovutoElaboratoDao.insert(dovutoElaborato);
    dovutoElaborato.setMygovDovutoElaboratoId(mygovDovutoElaboratoId);
    //TODO Gestire l'aggiunta del dovuto multibeneficiario elaborato nella tabella mygov_dovuto_multibeneficiario_elaborato
    //dovutoDao.delete(dovuto);
    return dovutoElaborato;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public DovutoFunctionOut callAnnullaFunction(Long n_mygov_ente_id, Long n_mygov_flusso_id, Integer n_num_riga_flusso, Long n_mygov_anagrafica_stato_id,
                                               Long n_mygov_carrello_id, String n_cod_iud, String n_cod_iuv, Date n_dt_creazione, String n_de_rp_versione_oggetto,
                                               String n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, String n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
                                               String n_de_rp_sogg_pag_anagrafica_pagatore, String n_de_rp_sogg_pag_indirizzo_pagatore, String n_de_rp_sogg_pag_civico_pagatore,
                                               String n_cod_rp_sogg_pag_cap_pagatore, String n_de_rp_sogg_pag_localita_pagatore, String n_de_rp_sogg_pag_provincia_pagatore,
                                               String n_cod_rp_sogg_pag_nazione_pagatore, String n_de_rp_sogg_pag_email_pagatore,
                                               Date n_dt_rp_dati_vers_data_esecuzione_pagamento, String n_cod_rp_dati_vers_tipo_versamento,
                                               Double n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, Double n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa,
                                               String n_cod_tipo_dovuto, String n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
                                               String n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, Long n_mygov_utente_id, boolean insert_avv_dig,
                                               String n_cod_iupd, Character n_gpd_status) {

    OutParameters out = dovutoElaboratoDao.callAnnullaFunction(n_mygov_ente_id, n_mygov_flusso_id, n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, n_cod_iud,
            n_cod_iuv, n_dt_creazione, n_de_rp_versione_oggetto, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
            n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, n_de_rp_sogg_pag_indirizzo_pagatore,
            n_de_rp_sogg_pag_civico_pagatore, n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, n_de_rp_sogg_pag_provincia_pagatore,
            n_cod_rp_sogg_pag_nazione_pagatore, n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento,
            n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
            n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto, n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
            n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_mygov_utente_id, insert_avv_dig, n_cod_iupd, n_gpd_status);

    return out==null?null:DovutoFunctionOut.builder()
            .result(out.getString("result"))
            .resultDesc(out.getString("result_desc"))
            .build();
  }

  /**
   * This is the same method as PagatoService.getDovutoElaboratoFittizio() in Mypay3.
   * @param carrello
   * @param ctDtRendCod9
   * @param ente
   * @return new DovutoElaborato Object.
   */
  @Transactional(propagation =  Propagation.REQUIRED)
  public DovutoElaborato getFittizio(Carrello carrello, DatiRendicontazioneCod9 ctDtRendCod9, Ente ente) {
    //TODO Stefano, creazione di un DovutoElaborato fittizio
    DovutoElaborato dovutoElaboratoFittizio = new DovutoElaborato();

    AnagraficaStato anagDovutoCompletato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_COMPLETATO, Constants.STATO_TIPO_DOVUTO);

    dovutoElaboratoFittizio.setVersion(0);
    dovutoElaboratoFittizio.setFlgDovutoAttuale(true);
    Flusso flusso = flussoService.getRend9Flusso(ente);
    dovutoElaboratoFittizio.setMygovFlussoId(flusso);
    dovutoElaboratoFittizio.setNumRigaFlusso(flusso.getNumRigheTotali());
    dovutoElaboratoFittizio.setMygovAnagraficaStatoId(anagDovutoCompletato);
    dovutoElaboratoFittizio.setMygovCarrelloId(carrello);
    dovutoElaboratoFittizio.setCodIud(Utilities.getRandomIUD());
    dovutoElaboratoFittizio.setDtCreazione(new Date());
    dovutoElaboratoFittizio.setCodIuv(ctDtRendCod9.getIdentificativoUnivocoVersamento());
    dovutoElaboratoFittizio.setDtUltimaModificaRp(new Date());
    dovutoElaboratoFittizio.setDtUltimaModificaE(new Date());
    dovutoElaboratoFittizio.setCodRpSilinviarpIdPsp(ctDtRendCod9.getIstitutoAttestante().getCodiceIdentificativoUnivoco());
    dovutoElaboratoFittizio.setCodRpSilinviarpIdDominio(ente.getCodiceFiscaleEnte());
    dovutoElaboratoFittizio.setCodRpSilinviarpIdUnivocoVersamento(ctDtRendCod9.getIdentificativoUnivocoVersamento());
    dovutoElaboratoFittizio.setCodRpSilinviarpCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    dovutoElaboratoFittizio.setDeRpSilinviarpEsito(Constants.ESITO.OK.toString());
    dovutoElaboratoFittizio.setDeRpVersioneOggetto(deRpVersioneOggetto);
    dovutoElaboratoFittizio.setCodRpDomIdDominio(ente.getCodiceFiscaleEnte());
    dovutoElaboratoFittizio.setCodRpIdMessaggioRichiesta(carrello.getCodRpIdMessaggioRichiesta());
    dovutoElaboratoFittizio.setDtRpDataOraMessaggioRichiesta(carrello.getDtRpDataOraMessaggioRichiesta());
    dovutoElaboratoFittizio.setCodRpAutenticazioneSoggetto(Constants.CODICE_AUTENTICAZIONE_SOGGETTO_NA);
    dovutoElaboratoFittizio.setNumRpDatiVersImportoTotaleDaVersare(ctDtRendCod9.getSingoloImportoPagato());
    dovutoElaboratoFittizio.setCodRpDatiVersIdUnivocoVersamento(ctDtRendCod9.getIdentificativoUnivocoVersamento());
    dovutoElaboratoFittizio.setCodRpDatiVersCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    dovutoElaboratoFittizio.setCodESilinviaesitoIdDominio(ente.getCodiceFiscaleEnte());
    dovutoElaboratoFittizio.setCodESilinviaesitoIdUnivocoVersamento(ctDtRendCod9.getIdentificativoUnivocoVersamento());
    dovutoElaboratoFittizio.setCodESilinviaesitoCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    dovutoElaboratoFittizio.setDeESilinviaesitoEsito(Constants.ESITO.OK.toString());
    dovutoElaboratoFittizio.setDeEVersioneOggetto(deRpVersioneOggetto);
    dovutoElaboratoFittizio.setCodEDomIdDominio(ente.getCodiceFiscaleEnte());
    dovutoElaboratoFittizio.setCodEIdMessaggioRicevuta(carrello.getCodEIdMessaggioRicevuta());
    dovutoElaboratoFittizio.setCodEDataOraMessaggioRicevuta(carrello.getCodEDataOraMessaggioRicevuta());
    dovutoElaboratoFittizio.setCodERiferimentoMessaggioRichiesta(dovutoElaboratoFittizio.getCodEIdMessaggioRicevuta());
    dovutoElaboratoFittizio.setCodERiferimentoDataRichiesta(new Date());
    dovutoElaboratoFittizio.setCodEIstitAttIdUnivAttTipoIdUnivoco(ctDtRendCod9.getIstitutoAttestante().getTipoIdentificativoUnivoco().charAt(0));
    dovutoElaboratoFittizio.setCodEIstitAttIdUnivAttCodiceIdUnivoco(ctDtRendCod9.getIstitutoAttestante().getCodiceIdentificativoUnivoco());
    dovutoElaboratoFittizio.setDeEIstitAttDenominazioneAttestante(ctDtRendCod9.getIstitutoAttestante().getDenominazioneAttestante());
    dovutoElaboratoFittizio.setCodEEnteBenefIdUnivBenefTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_G.charAt(0));
    dovutoElaboratoFittizio.setCodEEnteBenefIdUnivBenefCodiceIdUnivoco(ente.getCodiceFiscaleEnte());
    dovutoElaboratoFittizio.setDeEEnteBenefDenominazioneBeneficiario(ente.getDeRpEnteBenefDenominazioneBeneficiario());
    dovutoElaboratoFittizio.setDeEEnteBenefIndirizzoBeneficiario(ente.getDeRpEnteBenefIndirizzoBeneficiario());
    dovutoElaboratoFittizio.setDeEEnteBenefCivicoBeneficiario(ente.getDeRpEnteBenefCivicoBeneficiario());
    dovutoElaboratoFittizio.setCodEEnteBenefCapBeneficiario(ente.getCodRpEnteBenefCapBeneficiario());
    dovutoElaboratoFittizio.setDeEEnteBenefLocalitaBeneficiario(ente.getDeRpEnteBenefLocalitaBeneficiario());
    dovutoElaboratoFittizio.setDeEEnteBenefProvinciaBeneficiario(ente.getDeRpEnteBenefProvinciaBeneficiario());
    dovutoElaboratoFittizio.setCodEEnteBenefNazioneBeneficiario(ente.getCodRpEnteBenefNazioneBeneficiario());
    dovutoElaboratoFittizio.setCodESoggPagIdUnivPagTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));
    dovutoElaboratoFittizio.setCodEDatiPagCodiceEsitoPagamento(Constants.CODICE_ESITO_PAGAMENTO_ESEGUITO.charAt(0));
    dovutoElaboratoFittizio.setNumEDatiPagImportoTotalePagato(ctDtRendCod9.getSingoloImportoPagato());
    dovutoElaboratoFittizio.setCodEDatiPagIdUnivocoVersamento(ctDtRendCod9.getIdentificativoUnivocoVersamento());
    dovutoElaboratoFittizio.setCodEDatiPagCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    dovutoElaboratoFittizio.setNumEDatiPagDatiSingPagSingoloImportoPagato(ctDtRendCod9.getSingoloImportoPagato());
    dovutoElaboratoFittizio.setDeEDatiPagDatiSingPagEsitoSingoloPagamento(Constants.PAGATO_CON_RENDICONTAZIONE_9);
    dovutoElaboratoFittizio.setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(ctDtRendCod9.getDataEsitoSingoloPagamento());
    dovutoElaboratoFittizio.setCodEDatiPagDatiSingPagIdUnivocoRiscoss(ctDtRendCod9.getIdentificativoUnivocoRiscossione());
    if (Utilities.isAvviso(ctDtRendCod9.getIdentificativoUnivocoVersamento()))
      dovutoElaboratoFittizio.setModelloPagamento(Integer.valueOf(Constants.MODELLO_PAGAMENTO_4));
    else {
      dovutoElaboratoFittizio.setModelloPagamento(Integer.valueOf(Constants.MODELLO_PAGAMENTO_1));
    }
    dovutoElaboratoFittizio.setIndiceDatiSingoloPagamento(ctDtRendCod9.getIndiceDatiSingoloPagamento());
    dovutoElaboratoFittizio.setCodRpDatiVersTipoVersamento(Constants.TIPO_VERSAMENTO.TUTTI.getValue());
    dovutoElaboratoFittizio.setCodESoggPagIdUnivPagCodiceIdUnivoco(Constants.CODICE_FISCALE_ANONIMO);
    dovutoElaboratoFittizio.setCodESoggPagAnagraficaPagatore(Constants.CODICE_FISCALE_ANONIMO);
    dovutoElaboratoFittizio.setDeEDatiPagDatiSingPagCausaleVersamento(Constants.CAUSALE_DOVUTO_PAGATO);
    dovutoElaboratoFittizio.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW);
    dovutoElaboratoFittizio.setDeRpDatiVersDatiSingVersCausaleVersamentoAgid(Constants.CAUSALE_DOVUTO_PAGATO);
    dovutoElaboratoFittizio.setCodTipoDovuto(Constants.COD_TIPO_DOVUTO_DEFAULT);
    dovutoElaboratoFittizio.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(ctDtRendCod9.getSingoloImportoPagato());
    dovutoElaboratoFittizio.setDtUltimoCambioStato(new Date());
    dovutoElaboratoFittizio.setCodRpSoggPagIdUnivPagTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));
    dovutoElaboratoFittizio.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(Constants.CODICE_FISCALE_ANONIMO);
    dovutoElaboratoFittizio.setDeRpSoggPagAnagraficaPagatore(Constants.CODICE_FISCALE_ANONIMO);
    dovutoElaboratoFittizio.setDeRpDatiVersDatiSingVersCausaleVersamento(Constants.CAUSALE_DOVUTO_PAGATO);
    dovutoElaboratoFittizio.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW);
    dovutoElaboratoFittizio.setCodTipoDovuto(Constants.COD_TIPO_DOVUTO_DEFAULT);
    dovutoElaboratoFittizio.setDtRpDatiVersDataEsecuzionePagamento(ctDtRendCod9.getDataEsitoSingoloPagamento());

    return dovutoElaboratoFittizio;
  }

  private void copiaCampiDovutoRend9(Dovuto dovuto, DovutoElaborato dovutoElaborato,
                                     DatiRendicontazioneCod9 datiRendicontazioneCod9, Ente ente) {

    if (null != dovuto) {
      dovutoElaborato.setFlgDovutoAttuale(dovuto.isFlgDovutoAttuale());
      dovutoElaborato.setMygovFlussoId(dovuto.getMygovFlussoId());
      dovutoElaborato.setNumRigaFlusso(dovuto.getNumRigaFlusso());
      //dovutoElaborato.setCarrello(dovuto.getCarrello());
      dovutoElaborato.setCodIud(dovuto.getCodIud());
      dovutoElaborato.setDeRpSoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
      dovutoElaborato.setDeRpSoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
      dovutoElaborato.setCodRpSoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore());
      dovutoElaborato.setDeRpSoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore());
      dovutoElaborato.setDeRpSoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore());
      dovutoElaborato.setCodRpDatiVersTipoVersamento(Utilities.getDefaultString().apply(dovuto.getCodRpDatiVersTipoVersamento(), Constants.TIPO_VERSAMENTO.TUTTI.getValue()));
      dovutoElaborato.setCodESoggPagIdUnivPagCodiceIdUnivoco(Utilities.getDefaultString().apply(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(), Constants.CODICE_FISCALE_ANONIMO));
      dovutoElaborato.setCodESoggPagAnagraficaPagatore(Utilities.getDefaultString().apply(dovuto.getDeRpSoggPagAnagraficaPagatore(), Constants.CODICE_FISCALE_ANONIMO));
      dovutoElaborato.setDeESoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
      dovutoElaborato.setDeESoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
      dovutoElaborato.setCodESoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore());
      dovutoElaborato.setDeESoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore());
      dovutoElaborato.setDeESoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore());
      String causale = Utilities.getDefaultString()
              .andThen(Utilities.getTruncatedAt(Constants.MAX_LENGHT_CAUSALE))
              .apply(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(), Constants.CAUSALE_DOVUTO_PAGATO);
      dovutoElaborato.setDeEDatiPagDatiSingPagCausaleVersamento(causale);
      dovutoElaborato.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(Utilities.getDefaultString().apply(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(), Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW));
      dovutoElaborato.setDeRpDatiVersDatiSingVersCausaleVersamentoAgid(causale);
      dovutoElaborato.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(Utilities.getDefaultString().apply(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(), Constants.CODICE_FISCALE_ANONIMO));
      dovutoElaborato.setCodRpSoggPagIdUnivPagTipoIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco() == ' ' ? Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0) : dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco());
      dovutoElaborato.setDeRpSoggPagAnagraficaPagatore(Utilities.getDefaultString().apply(dovuto.getDeRpSoggPagAnagraficaPagatore(), Constants.CODICE_FISCALE_ANONIMO));
      dovutoElaborato.setDeRpDatiVersDatiSingVersCausaleVersamento(Utilities.getDefaultString().apply(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(), Constants.CAUSALE_DOVUTO_PAGATO));
      dovutoElaborato.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(Utilities.getDefaultString().apply(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(), Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW));
      dovutoElaborato.setCodTipoDovuto(dovuto.getCodTipoDovuto());
      dovutoElaborato.setDtRpDatiVersDataEsecuzionePagamento(datiRendicontazioneCod9.getDataEsitoSingoloPagamento());
      if(null != dovuto.getMygovCarrelloId()) {
        dovutoElaborato.setCodRpIdMessaggioRichiesta(dovuto.getMygovCarrelloId().getCodRpIdMessaggioRichiesta());
        dovutoElaborato.setCodEIdMessaggioRicevuta(dovuto.getMygovCarrelloId().getCodEIdMessaggioRicevuta());
        dovutoElaborato.setCodEDataOraMessaggioRicevuta(dovuto.getMygovCarrelloId().getCodEDataOraMessaggioRicevuta() == null ? new Date() : dovuto.getMygovCarrelloId().getCodEDataOraMessaggioRicevuta());
        dovutoElaborato.setCodERiferimentoDataRichiesta(dovuto.getMygovCarrelloId().getCodERiferimentoDataRichiesta() == null ? new Date() : dovuto.getMygovCarrelloId().getCodERiferimentoDataRichiesta());
      }else {
        dovutoElaborato.setCodEDataOraMessaggioRicevuta(new Date());
        dovutoElaborato.setCodERiferimentoDataRichiesta(new Date());
      }
    }else {
      dovutoElaborato.setCodRpDatiVersTipoVersamento(Constants.TIPO_VERSAMENTO.TUTTI.getValue());
      dovutoElaborato.setCodESoggPagIdUnivPagCodiceIdUnivoco(Constants.CODICE_FISCALE_ANONIMO);
      dovutoElaborato.setCodESoggPagAnagraficaPagatore(Constants.CODICE_FISCALE_ANONIMO);
      dovutoElaborato.setDeEDatiPagDatiSingPagCausaleVersamento(Constants.CAUSALE_DOVUTO_PAGATO);
      dovutoElaborato.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW);
      dovutoElaborato.setDeRpDatiVersDatiSingVersCausaleVersamentoAgid(Constants.CAUSALE_DOVUTO_PAGATO);
      dovutoElaborato.setCodRpIdMessaggioRichiesta(Constants.COD_MARCATURA_REND_9 + Utilities.getRandomicUUID());
      dovutoElaborato.setCodEIdMessaggioRicevuta(Constants.COD_MARCATURA_REND_9 + Utilities.getRandomicUUID());
      dovutoElaborato.setCodEDataOraMessaggioRicevuta(dovutoElaborato.getMygovCarrelloId().getCodEDataOraMessaggioRicevuta());
      dovutoElaborato.setCodERiferimentoDataRichiesta(dovutoElaborato.getMygovCarrelloId().getCodERiferimentoDataRichiesta());
      dovutoElaborato.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(Constants.CODICE_FISCALE_ANONIMO);
      dovutoElaborato.setCodRpSoggPagIdUnivPagTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));
      dovutoElaborato.setDeRpSoggPagAnagraficaPagatore(Constants.CODICE_FISCALE_ANONIMO);
      dovutoElaborato.setDeRpDatiVersDatiSingVersCausaleVersamento(Constants.CAUSALE_DOVUTO_PAGATO);
      dovutoElaborato.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(Constants.DATI_SPECIFICI_RISCOSSIONE_UNKNOW);
      dovutoElaborato.setCodEDataOraMessaggioRicevuta(new Date());
      dovutoElaborato.setCodERiferimentoDataRichiesta(new Date());
      dovutoElaborato.setCodTipoDovuto(Constants.COD_TIPO_DOVUTO_DEFAULT);
      dovutoElaborato.setDtRpDatiVersDataEsecuzionePagamento(datiRendicontazioneCod9.getDataEsitoSingoloPagamento());
    }

    dovutoElaborato.setDtCreazione(new Date());
    dovutoElaborato.setCodIuv(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    dovutoElaborato.setDtUltimaModificaRp(new Date());
    dovutoElaborato.setDtUltimaModificaE(new Date());
    dovutoElaborato.setCodRpSilinviarpIdPsp(datiRendicontazioneCod9.getIstitutoAttestante().getCodiceIdentificativoUnivoco());
    dovutoElaborato.setCodRpSilinviarpIdDominio(ente.getCodiceFiscaleEnte());
    dovutoElaborato.setCodRpSilinviarpIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    dovutoElaborato.setCodRpSilinviarpCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    dovutoElaborato.setDeRpSilinviarpEsito(Constants.ESITO.OK.toString());
    dovutoElaborato.setDeRpVersioneOggetto(deRpVersioneOggetto);
    dovutoElaborato.setCodRpDomIdDominio(ente.getCodiceFiscaleEnte());
    dovutoElaborato.setDtRpDataOraMessaggioRichiesta(new Date());
    dovutoElaborato.setCodRpAutenticazioneSoggetto(Constants.CODICE_AUTENTICAZIONE_SOGGETTO_NA);
    dovutoElaborato.setNumRpDatiVersImportoTotaleDaVersare(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaborato.setCodRpDatiVersIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    dovutoElaborato.setCodRpDatiVersCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    dovutoElaborato.setCodESilinviaesitoIdDominio(ente.getCodiceFiscaleEnte());
    dovutoElaborato.setCodESilinviaesitoIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    dovutoElaborato.setCodESilinviaesitoCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    dovutoElaborato.setDeESilinviaesitoEsito(Constants.ESITO.OK.toString());
    dovutoElaborato.setDeEVersioneOggetto(deRpVersioneOggetto);
    dovutoElaborato.setCodEDomIdDominio(ente.getCodiceFiscaleEnte());
    dovutoElaborato.setCodERiferimentoMessaggioRichiesta(dovutoElaborato.getCodEIdMessaggioRicevuta());
    dovutoElaborato.setCodEIstitAttIdUnivAttTipoIdUnivoco(datiRendicontazioneCod9.getIstitutoAttestante().getTipoIdentificativoUnivoco().charAt(0));
    dovutoElaborato.setCodEIstitAttIdUnivAttCodiceIdUnivoco(datiRendicontazioneCod9.getIstitutoAttestante().getCodiceIdentificativoUnivoco());
    dovutoElaborato.setDeEIstitAttDenominazioneAttestante(datiRendicontazioneCod9.getIstitutoAttestante().getDenominazioneAttestante());
    dovutoElaborato.setCodEEnteBenefIdUnivBenefTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_G.charAt(0));
    dovutoElaborato.setCodEEnteBenefIdUnivBenefCodiceIdUnivoco(ente.getCodiceFiscaleEnte());
    dovutoElaborato.setDeEEnteBenefDenominazioneBeneficiario(ente.getDeRpEnteBenefDenominazioneBeneficiario());
    dovutoElaborato.setDeEEnteBenefIndirizzoBeneficiario(ente.getDeRpEnteBenefIndirizzoBeneficiario());
    dovutoElaborato.setDeEEnteBenefCivicoBeneficiario(ente.getDeRpEnteBenefCivicoBeneficiario());
    dovutoElaborato.setCodEEnteBenefCapBeneficiario(ente.getCodRpEnteBenefCapBeneficiario());
    dovutoElaborato.setDeEEnteBenefLocalitaBeneficiario(ente.getDeRpEnteBenefLocalitaBeneficiario());
    dovutoElaborato.setDeEEnteBenefProvinciaBeneficiario(ente.getDeRpEnteBenefProvinciaBeneficiario());
    dovutoElaborato.setCodEEnteBenefNazioneBeneficiario(ente.getCodRpEnteBenefNazioneBeneficiario());
    dovutoElaborato.setCodESoggPagIdUnivPagTipoIdUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));
    dovutoElaborato.setCodEDatiPagCodiceEsitoPagamento(Constants.CODICE_ESITO_PAGAMENTO_ESEGUITO.charAt(0));
    dovutoElaborato.setNumEDatiPagImportoTotalePagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaborato.setCodEDatiPagIdUnivocoVersamento(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento());
    dovutoElaborato.setCodEDatiPagCodiceContestoPagamento(Constants.CODICE_CONTESTO_PAGAMENTO_NA);
    dovutoElaborato.setNumEDatiPagDatiSingPagSingoloImportoPagato(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaborato.setDeEDatiPagDatiSingPagEsitoSingoloPagamento(Constants.PAGATO_CON_RENDICONTAZIONE_9);
    dovutoElaborato.setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(datiRendicontazioneCod9.getDataEsitoSingoloPagamento());
    dovutoElaborato.setCodEDatiPagDatiSingPagIdUnivocoRiscoss(datiRendicontazioneCod9.getIdentificativoUnivocoRiscossione());
    if (Utilities.isAvviso(datiRendicontazioneCod9.getIdentificativoUnivocoVersamento()))
      dovutoElaborato.setModelloPagamento(Integer.valueOf(Constants.MODELLO_PAGAMENTO_4));
    else {
      dovutoElaborato.setModelloPagamento(Integer.valueOf(Constants.MODELLO_PAGAMENTO_1));
    }
    dovutoElaborato.setIndiceDatiSingoloPagamento(datiRendicontazioneCod9.getIndiceDatiSingoloPagamento());
    dovutoElaborato.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(datiRendicontazioneCod9.getSingoloImportoPagato());
    dovutoElaborato.setDtUltimoCambioStato(new Date());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public DovutoElaborato insertByEsito(Dovuto dovuto, CtDatiSingoloPagamentoEsito singoloPagamentoEsito,
                                       Carrello carrello, Esito ctEsito, final String tipoFirma, final byte[] rt, String nuovoStato,
                                       String tipoNuovoStato, int indiceDatiSingoloPagamento) {

    DovutoElaborato elaborato = new DovutoElaborato();
    AnagraficaStato anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(nuovoStato, tipoNuovoStato);
    elaborato.setMygovAnagraficaStatoId(anagraficaStato);
    elaborato.setDeRtInviartTipoFirma(tipoFirma);
    elaborato.setBlbRtPayload(rt);
    Date now = new Date();

    elaborato.setFlgDovutoAttuale(dovuto.isFlgDovutoAttuale());
    elaborato.setMygovFlussoId(dovuto.getMygovFlussoId());
    elaborato.setNumRigaFlusso(dovuto.getNumRigaFlusso());
    elaborato.setMygovCarrelloId(dovuto.getMygovCarrelloId());
    elaborato.setCodIud(dovuto.getCodIud());
    elaborato.setCodIuv(dovuto.getCodIuv());
    elaborato.setDtCreazione(now);
    elaborato.setCodRpSoggPagIdUnivPagTipoIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco());
    elaborato.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
    elaborato.setDeRpSoggPagAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore());
    elaborato.setDeRpSoggPagIndirizzoPagatore(dovuto.getDeRpSoggPagIndirizzoPagatore());
    elaborato.setDeRpSoggPagCivicoPagatore(dovuto.getDeRpSoggPagCivicoPagatore());
    elaborato.setCodRpSoggPagCapPagatore(dovuto.getCodRpSoggPagCapPagatore());
    elaborato.setDeRpSoggPagLocalitaPagatore(dovuto.getDeRpSoggPagLocalitaPagatore());
    elaborato.setDeRpSoggPagProvinciaPagatore(dovuto.getDeRpSoggPagProvinciaPagatore());
    elaborato.setCodRpSoggPagNazionePagatore(dovuto.getCodRpSoggPagNazionePagatore());
    elaborato.setDeRpSoggPagEmailPagatore(dovuto.getDeRpSoggPagEmailPagatore());
    elaborato.setDtRpDatiVersDataEsecuzionePagamento(dovuto.getDtRpDatiVersDataEsecuzionePagamento());
    elaborato.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento());
    elaborato.setNumRpDatiVersDatiSingVersCommissioneCaricoPa(dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa());
    elaborato.setCodTipoDovuto(dovuto.getCodTipoDovuto());
    elaborato.setCodRpDatiVersDatiSingVersCredenzialiPagatore(null);
    elaborato.setDeRpDatiVersDatiSingVersCausaleVersamento(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento());
    String datiSpecificiRiscossione = dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione();
    elaborato.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(datiSpecificiRiscossione);
    elaborato.setCodTipoDovuto(dovuto.getCodTipoDovuto());
    Optional.ofNullable(dovuto.getBilancio()).ifPresent(elaborato::setBilancio);

    elaborato.setCodAckRp(carrello.getCodAckRp());
    elaborato.setDtUltimaModificaRp(carrello.getDtUltimaModificaRp());
    elaborato.setDtUltimaModificaE(carrello.getDtUltimaModificaE());
    elaborato.setCodRpSilinviarpIdPsp(carrello.getCodRpSilinviarpIdPsp());
    elaborato.setCodRpSilinviarpIdIntermediarioPsp(carrello.getCodRpSilinviarpIdIntermediarioPsp());
    elaborato.setCodRpSilinviarpIdCanale(carrello.getCodRpSilinviarpIdCanale());
    elaborato.setCodRpSilinviarpIdDominio(carrello.getCodRpSilinviarpIdDominio());
    elaborato.setCodRpSilinviarpIdUnivocoVersamento(carrello.getCodRpSilinviarpIdUnivocoVersamento());
    elaborato.setCodRpSilinviarpCodiceContestoPagamento(carrello.getCodRpSilinviarpCodiceContestoPagamento());
    elaborato.setDeRpSilinviarpEsito(carrello.getDeRpSilinviarpEsito());
    elaborato.setCodRpSilinviarpRedirect(carrello.getCodRpSilinviarpRedirect());
    elaborato.setCodRpSilinviarpUrl(carrello.getCodRpSilinviarpUrl());
    elaborato.setCodRpSilinviarpFaultCode(carrello.getCodRpSilinviarpFaultCode());
    elaborato.setDeRpSilinviarpFaultString(carrello.getDeRpSilinviarpFaultString());
    elaborato.setCodRpSilinviarpId(carrello.getCodRpSilinviarpId());

    String description = Utilities.getTruncatedAt(1024).apply(carrello.getDeRpSilinviarpDescription());
    Optional.ofNullable(description).ifPresent(elaborato::setDeRpSilinviarpDescription);
    elaborato.setCodRpSilinviarpOriginalFaultCode(carrello.getCodRpSilinviarpOriginalFaultCode());
    elaborato.setDeRpSilinviarpOriginalFaultString(carrello.getDeRpSilinviarpOriginalFaultString());
    String originalDescription = Utilities.getTruncatedAt(1024).apply(carrello.getDeRpSilinviarpOriginalFaultDescription());
    Optional.ofNullable(originalDescription).ifPresent(elaborato::setDeRpSilinviarpOriginalFaultDescription);

    elaborato.setCodRpSilinviarpSerial(carrello.getCodRpSilinviarpSerial());
    elaborato.setDeRpVersioneOggetto(carrello.getDeRpVersioneOggetto());
    elaborato.setCodRpDomIdDominio(carrello.getCodRpDomIdDominio());
    elaborato.setCodRpDomIdStazioneRichiedente(carrello.getCodRpDomIdStazioneRichiedente());
    elaborato.setCodRpIdMessaggioRichiesta(carrello.getCodRpIdMessaggioRichiesta());
    elaborato.setDtRpDataOraMessaggioRichiesta(carrello.getDtRpDataOraMessaggioRichiesta());
    elaborato.setCodRpAutenticazioneSoggetto(carrello.getCodRpAutenticazioneSoggetto());
    elaborato.setCodRpSoggVersIdUnivVersTipoIdUnivoco(carrello.getCodRpSoggVersIdUnivVersTipoIdUnivoco());
    elaborato.setCodRpSoggVersIdUnivVersCodiceIdUnivoco(carrello.getCodRpSoggVersIdUnivVersCodiceIdUnivoco());
    elaborato.setCodRpSoggVersAnagraficaVersante(carrello.getCodRpSoggVersAnagraficaVersante());
    elaborato.setDeRpSoggVersIndirizzoVersante(carrello.getDeRpSoggVersIndirizzoVersante());
    elaborato.setDeRpSoggVersCivicoVersante(carrello.getDeRpSoggVersCivicoVersante());
    elaborato.setCodRpSoggVersCapVersante(carrello.getCodRpSoggVersCapVersante());
    elaborato.setDeRpSoggVersLocalitaVersante(carrello.getDeRpSoggVersLocalitaVersante());
    elaborato.setDeRpSoggVersProvinciaVersante(carrello.getDeRpSoggVersProvinciaVersante());
    elaborato.setCodRpSoggVersNazioneVersante(carrello.getCodRpSoggVersNazioneVersante());
    elaborato.setDeRpSoggVersEmailVersante(carrello.getDeRpSoggVersEmailVersante());

    elaborato.setCodRpSoggPagIdUnivPagTipoIdUnivoco(carrello.getCodRpSoggPagIdUnivPagTipoIdUnivoco());
    elaborato.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(carrello.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
    elaborato.setDeRpSoggPagAnagraficaPagatore(carrello.getDeRpSoggPagAnagraficaPagatore());
    elaborato.setDeRpSoggPagIndirizzoPagatore(carrello.getDeRpSoggPagIndirizzoPagatore());
    elaborato.setDeRpSoggPagCivicoPagatore(carrello.getDeRpSoggPagCivicoPagatore());
    elaborato.setCodRpSoggPagCapPagatore(carrello.getCodRpSoggPagCapPagatore());
    elaborato.setDeRpSoggPagLocalitaPagatore(carrello.getDeRpSoggPagLocalitaPagatore());
    elaborato.setDeRpSoggPagProvinciaPagatore(carrello.getDeRpSoggPagProvinciaPagatore());
    elaborato.setCodRpSoggPagNazionePagatore(carrello.getCodRpSoggPagNazionePagatore());
    elaborato.setDeRpSoggPagEmailPagatore(carrello.getDeRpSoggPagEmailPagatore());

    elaborato.setNumRpDatiVersImportoTotaleDaVersare(carrello.getNumRpDatiVersImportoTotaleDaVersare());
    elaborato.setCodRpDatiVersTipoVersamento(carrello.getCodRpDatiVersTipoVersamento());
    elaborato.setCodRpDatiVersIdUnivocoVersamento(carrello.getCodRpDatiVersIdUnivocoVersamento());
    elaborato.setCodRpDatiVersCodiceContestoPagamento(carrello.getCodRpDatiVersCodiceContestoPagamento());
    elaborato.setDeRpDatiVersIbanAddebito(carrello.getDeRpDatiVersIbanAddebito());
    elaborato.setDeRpDatiVersBicAddebito(carrello.getDeRpDatiVersBicAddebito());

    elaborato.setModelloPagamento(carrello.getModelloPagamento());
    elaborato.setEnteSilInviaRispostaPagamentoUrl(carrello.getEnteSilInviaRispostaPagamentoUrl());

    DovutoCarrello dovutoCarrello = dovutoCarrelloService.getByDovutoECarrello(dovuto.getMygovDovutoId(), carrello.getMygovCarrelloId());

    if (Objects.nonNull(dovutoCarrello)) {
      elaborato = elaborato.toBuilder()
              .numRpDatiVersDatiSingVersCommissioneCaricoPa(dovutoCarrello.getNumRpDatiVersDatiSingVersCommissioneCaricoPa())
              .codRpDatiVersDatiSingVersIbanAccredito(dovutoCarrello.getCodRpDatiVersDatiSingVersIbanAccredito())
              .codRpDatiVersDatiSingVersBicAccredito(dovutoCarrello.getCodRpDatiVersDatiSingVersBicAccredito())
              .codRpDatiVersDatiSingVersIbanAppoggio(dovutoCarrello.getCodRpDatiVersDatiSingVersIbanAppoggio())
              .codRpDatiVersDatiSingVersBicAppoggio(dovutoCarrello.getCodRpDatiVersDatiSingVersBicAppoggio())
              .deRpDatiVersDatiSingVersCausaleVersamentoAgid(dovutoCarrello.getDeRpDatiVersDatiSingVersCausaleVersamentoAgid())
              .build();
    }

    if (!List.of(Constants.STATO_DOVUTO_ABORT, Constants.STATO_DOVUTO_SCADUTO_ELABORATO).contains(nuovoStato)) {
      elaborato.setCodESilinviaesitoIdDominio(carrello.getCodESilinviaesitoIdDominio());
      elaborato.setCodESilinviaesitoIdUnivocoVersamento(carrello.getCodESilinviaesitoIdUnivocoVersamento());
      elaborato.setCodESilinviaesitoCodiceContestoPagamento(carrello.getCodESilinviaesitoCodiceContestoPagamento());
      elaborato.setDeEVersioneOggetto(carrello.getDeEVersioneOggetto());
      elaborato.setCodEDomIdDominio(carrello.getCodEDomIdDominio());
      elaborato.setCodEDomIdStazioneRichiedente(carrello.getCodEDomIdStazioneRichiedente());
      elaborato.setCodEIdMessaggioRicevuta(carrello.getCodEIdMessaggioRicevuta());
      elaborato.setCodEDataOraMessaggioRicevuta(carrello.getCodEDataOraMessaggioRicevuta());
      elaborato.setCodERiferimentoMessaggioRichiesta(carrello.getCodERiferimentoMessaggioRichiesta());
      elaborato.setCodERiferimentoDataRichiesta(carrello.getCodERiferimentoDataRichiesta());
      elaborato.setCodEIstitAttIdUnivAttTipoIdUnivoco(carrello.getCodEIstitAttIdUnivAttTipoIdUnivoco());
      elaborato.setCodEIstitAttIdUnivAttCodiceIdUnivoco(carrello.getCodEIstitAttIdUnivAttCodiceIdUnivoco());
      elaborato.setDeEIstitAttDenominazioneAttestante(carrello.getDeEIstitAttDenominazioneAttestante());
      elaborato.setCodEIstitAttCodiceUnitOperAttestante(carrello.getCodEIstitAttCodiceUnitOperAttestante());
      elaborato.setDeEIstitAttDenomUnitOperAttestante(carrello.getDeEIstitAttDenomUnitOperAttestante());
      elaborato.setDeEIstitAttIndirizzoAttestante(carrello.getDeEIstitAttIndirizzoAttestante());
      elaborato.setDeEIstitAttCivicoAttestante(carrello.getDeEIstitAttCivicoAttestante());
      elaborato.setCodEIstitAttCapAttestante(carrello.getCodEIstitAttCapAttestante());
      elaborato.setDeEIstitAttLocalitaAttestante(carrello.getDeEIstitAttLocalitaAttestante());
      elaborato.setDeEIstitAttProvinciaAttestante(carrello.getDeEIstitAttProvinciaAttestante());
      elaborato.setCodEIstitAttNazioneAttestante(carrello.getCodEIstitAttNazioneAttestante());

      elaborato.setCodEEnteBenefIdUnivBenefTipoIdUnivoco(ctEsito.getEnteBeneficiario()
              .getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco().toString().charAt(0));
      elaborato.setCodEEnteBenefIdUnivBenefCodiceIdUnivoco(ctEsito.getEnteBeneficiario()
              .getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco());
      elaborato.setDeEEnteBenefDenominazioneBeneficiario(ctEsito.getEnteBeneficiario().getDenominazioneBeneficiario());
      elaborato.setCodEEnteBenefCodiceUnitOperBeneficiario(ctEsito.getEnteBeneficiario().getCodiceUnitOperBeneficiario());
      elaborato.setDeEEnteBenefDenomUnitOperBeneficiario(ctEsito.getEnteBeneficiario().getDenomUnitOperBeneficiario());
      elaborato.setDeEEnteBenefIndirizzoBeneficiario(ctEsito.getEnteBeneficiario().getIndirizzoBeneficiario());
      elaborato.setDeEEnteBenefCivicoBeneficiario(ctEsito.getEnteBeneficiario().getCivicoBeneficiario());
      elaborato.setCodEEnteBenefCapBeneficiario(ctEsito.getEnteBeneficiario().getCapBeneficiario());
      elaborato.setDeEEnteBenefLocalitaBeneficiario(ctEsito.getEnteBeneficiario().getLocalitaBeneficiario());
      elaborato.setDeEEnteBenefProvinciaBeneficiario(ctEsito.getEnteBeneficiario().getProvinciaBeneficiario());
      elaborato.setCodEEnteBenefNazioneBeneficiario(ctEsito.getEnteBeneficiario().getNazioneBeneficiario());
      elaborato.setCodESoggVersIdUnivVersTipoIdUnivoco(carrello.getCodESoggVersIdUnivVersTipoIdUnivoco());
      elaborato.setCodESoggVersIdUnivVersCodiceIdUnivoco(carrello.getCodESoggVersIdUnivVersCodiceIdUnivoco());
      elaborato.setCodESoggVersAnagraficaVersante(carrello.getCodESoggVersAnagraficaVersante());
      elaborato.setDeESoggVersIndirizzoVersante(carrello.getDeESoggVersIndirizzoVersante());
      elaborato.setDeESoggVersCivicoVersante(carrello.getDeESoggVersCivicoVersante());
      elaborato.setCodESoggVersCapVersante(carrello.getCodESoggVersCapVersante());
      elaborato.setDeESoggVersLocalitaVersante(carrello.getDeESoggVersLocalitaVersante());
      elaborato.setDeESoggVersProvinciaVersante(carrello.getDeESoggVersProvinciaVersante());
      elaborato.setCodESoggVersNazioneVersante(carrello.getCodESoggVersNazioneVersante());
      elaborato.setDeESoggVersEmailVersante(carrello.getDeESoggVersEmailVersante());

      elaborato.setCodESoggPagIdUnivPagTipoIdUnivoco(carrello.getCodESoggPagIdUnivPagTipoIdUnivoco());
      elaborato.setCodESoggPagIdUnivPagCodiceIdUnivoco(carrello.getCodESoggPagIdUnivPagCodiceIdUnivoco());
      elaborato.setCodESoggPagAnagraficaPagatore(carrello.getCodESoggPagAnagraficaPagatore());
      elaborato.setDeESoggPagIndirizzoPagatore(carrello.getDeESoggPagIndirizzoPagatore());
      elaborato.setDeESoggPagCivicoPagatore(carrello.getDeESoggPagCivicoPagatore());
      elaborato.setCodESoggPagCapPagatore(carrello.getCodESoggPagCapPagatore());
      elaborato.setDeESoggPagLocalitaPagatore(carrello.getDeESoggPagLocalitaPagatore());
      elaborato.setDeESoggPagProvinciaPagatore(carrello.getDeESoggPagProvinciaPagatore());
      elaborato.setCodESoggPagNazionePagatore(carrello.getCodESoggPagNazionePagatore());
      elaborato.setDeESoggPagEmailPagatore(carrello.getDeESoggPagEmailPagatore());

      elaborato.setCodEDatiPagCodiceEsitoPagamento(carrello.getCodEDatiPagCodiceEsitoPagamento());
      elaborato.setNumEDatiPagImportoTotalePagato(carrello.getNumEDatiPagImportoTotalePagato());
      elaborato.setCodEDatiPagIdUnivocoVersamento(carrello.getCodEDatiPagIdUnivocoVersamento());
      elaborato.setCodEDatiPagCodiceContestoPagamento(carrello.getCodEDatiPagCodiceContestoPagamento());

      elaborato.setNumEDatiPagDatiSingPagSingoloImportoPagato(singoloPagamentoEsito.getSingoloImportoPagato());
      elaborato.setDeEDatiPagDatiSingPagEsitoSingoloPagamento(singoloPagamentoEsito.getEsitoSingoloPagamento());
      elaborato.setDtEDatiPagDatiSingPagDataEsitoSingoloPagamento(singoloPagamentoEsito.getDataEsitoSingoloPagamento().toGregorianCalendar().getTime());
      elaborato.setCodEDatiPagDatiSingPagIdUnivocoRiscoss( singoloPagamentoEsito.getIdentificativoUnivocoRiscossione());
      elaborato.setDeEDatiPagDatiSingPagCausaleVersamento(singoloPagamentoEsito.getCausaleVersamento());
      elaborato.setDeEDatiPagDatiSingPagDatiSpecificiRiscossione(
              Utilities.getDefaultString().apply(datiSpecificiRiscossione, singoloPagamentoEsito.getDatiSpecificiRiscossione())
      );

      elaborato.setDtUltimoCambioStato(elaborato.getCodEDataOraMessaggioRicevuta());

      elaborato.setNumEDatiPagDatiSingPagCommissioniApplicatePsp(singoloPagamentoEsito.getCommissioniApplicatePSP());

      if (singoloPagamentoEsito.getAllegatoRicevuta() != null) {
        elaborato.setCodEDatiPagDatiSingPagAllegatoRicevutaTipo(singoloPagamentoEsito.getAllegatoRicevuta().getTipoAllegatoRicevuta().toString());
        elaborato.setBlbEDatiPagDatiSingPagAllegatoRicevutaTest(singoloPagamentoEsito.getAllegatoRicevuta().getTestoAllegato());
      }
    }
    elaborato.setIndiceDatiSingoloPagamento(indiceDatiSingoloPagamento);
    elaborato.setGpdStatus(dovuto.getGpdStatus());
    elaborato.setGpdIupd(dovuto.getGpdIupd());
    if (elaborato.getDtUltimoCambioStato()==null) elaborato.setDtUltimoCambioStato(now);

    if (dovuto.getCodTipoDovuto().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE) && dovuto.getMygovDatiMarcaBolloDigitaleId() != null) {
      DatiMarcaBolloDigitale bolloDigitale = datiMarcaBolloDigitaleService.getById(dovuto.getMygovDatiMarcaBolloDigitaleId());
      Optional.ofNullable(bolloDigitale.getTipoBollo()).ifPresent(elaborato::setCodRpDatiVersDatiSingVersDatiMbdTipoBollo);
      Optional.ofNullable(bolloDigitale.getHashDocumento()).ifPresent(elaborato::setCodRpDatiVersDatiSingVersDatiMbdHashDocumento);
      Optional.ofNullable(bolloDigitale.getProvinciaResidenza()).ifPresent(elaborato::setCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza);
    }

    long newId = dovutoElaboratoDao.insert(elaborato);
    return elaborato.toBuilder().mygovDovutoElaboratoId(newId).build();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updatePaaSILInviaEsitoRisposta(Long idDovutoElaborato, PaaSILInviaEsitoRisposta paaSILInviaEsitoRisposta) {
    DovutoElaborato dovutoElaborato = this.getById(idDovutoElaborato);
    if (dovutoElaborato == null) {
      return;
    }
    dovutoElaborato.setDeESilinviaesitoEsito(paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta().getEsito());
    this.upsert(dovutoElaborato);
  }

  public Optional<DovutoElaborato> getByCodRpIdMessaggioRichiesta(String idMessaggioRichiesta) {
    return dovutoElaboratoDao.getByCodRpIdMessaggioRichiesta(idMessaggioRichiesta);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public DovutoMultibeneficiarioElaborato insertDovutoMultibenefElaborato(Dovuto dovuto, DovutoMultibeneficiario dovMultibenef,
                                                                          Long mygovDovutoElaboratoId) {

    DovutoMultibeneficiarioElaborato dovMultibenefElaborato = new DovutoMultibeneficiarioElaborato();

    dovMultibenefElaborato.setDeRpEnteBenefDenominazioneBeneficiario(dovMultibenef.getDeRpEnteBenefDenominazioneBeneficiario());
    dovMultibenefElaborato.setCodiceFiscaleEnte(dovMultibenef.getCodiceFiscaleEnte());
    dovMultibenefElaborato.setCodIuv(dovuto.getCodIuv());
    dovMultibenefElaborato.setCodIud(dovuto.getCodIud());

    dovMultibenefElaborato.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(dovMultibenef.getNumRpDatiVersDatiSingVersImportoSingoloVersamento());

    dovMultibenefElaborato.setDeRpEnteBenefCivicoBeneficiario(dovMultibenef.getDeRpEnteBenefCivicoBeneficiario());
    dovMultibenefElaborato.setDeRpEnteBenefIndirizzoBeneficiario(dovMultibenef.getDeRpEnteBenefIndirizzoBeneficiario());
    dovMultibenefElaborato.setDeRpEnteBenefLocalitaBeneficiario(dovMultibenef.getDeRpEnteBenefLocalitaBeneficiario());
    dovMultibenefElaborato.setDeRpEnteBenefProvinciaBeneficiario(dovMultibenef.getDeRpEnteBenefProvinciaBeneficiario());
    dovMultibenefElaborato.setCodRpEnteBenefNazioneBeneficiario(dovMultibenef.getCodRpEnteBenefNazioneBeneficiario());
    dovMultibenefElaborato.setCodRpEnteBenefCapBeneficiario(dovMultibenef.getCodRpEnteBenefCapBeneficiario());
    dovMultibenefElaborato.setCodRpDatiVersDatiSingVersIbanAccredito(dovMultibenef.getCodRpDatiVersDatiSingVersIbanAccredito());

    if (null!=mygovDovutoElaboratoId)
      dovMultibenefElaborato.setMygovDovutoElaboratoId(DovutoElaborato.builder().mygovDovutoElaboratoId(mygovDovutoElaboratoId).build());

    dovMultibenefElaborato.setDeRpDatiVersDatiSingVersCausaleVersamento(dovMultibenef.getDeRpDatiVersDatiSingVersCausaleVersamento());
    dovMultibenefElaborato.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(dovMultibenef.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione());

    Date now = new Date();
    dovMultibenefElaborato.setDtCreazione(now);
    dovMultibenefElaborato.setDtUltimaModifica(now);

    long newId = dovutoMultibeneficiarioElaboratoDao.insert(dovMultibenefElaborato);
    return dovMultibenefElaborato.toBuilder().mygovDovutoMultibeneficiarioElaboratoId(newId).build();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public BigDecimal getImportoDovutoMultibeneficiarioElaboratoByIdDovuto(Long idDovuto) {
    return dovutoMultibeneficiarioElaboratoDao.getImportoDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(idDovuto);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public Optional<DovutoMultibeneficiarioElaborato> getDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(long idDovutoElaborato) {
    return dovutoMultibeneficiarioElaboratoDao.getDovutoMultibeneficiarioElaboratoByIdDovutoElaborato(idDovutoElaborato);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public DovutoMultibeneficiarioElaborato getDovutoMultibeneficiarioElaboratoByCCP(String CCP) {
    DovutoMultibeneficiarioElaborato dovMultibeneficiarioElaborato = dovutoMultibeneficiarioElaboratoDao.getDovutoMultibeneficiarioElaboratoByCCP(CCP);
    return dovMultibeneficiarioElaborato;
  }


  @Transactional(propagation = Propagation.REQUIRED)
  public int updateFromReceipt(Long mygovDovutoMultibeneficiarioElaboratoId,
                               String deRpDatiVersDatiSingVersCausaleVersamento, String deRpDatiVersDatiSingVersDatiSpecificiRiscossione) {
    return dovutoMultibeneficiarioElaboratoDao.updateFromReceipt(mygovDovutoMultibeneficiarioElaboratoId,
      deRpDatiVersDatiSingVersCausaleVersamento, deRpDatiVersDatiSingVersDatiSpecificiRiscossione);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int updateDovElaboratoId(DovutoMultibeneficiarioElaborato dovMultiElab) {
    return dovutoMultibeneficiarioElaboratoDao.updateDovElaboratoId(dovMultiElab);
  }

}
