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
package it.regioneveneto.mygov.payment.mypay4.service.pagopa;

import it.regioneveneto.mygov.payment.mypay4.dao.DovutoPreloadDao;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd.*;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.service.DatiMarcaBolloDigitaleService;
import it.regioneveneto.mygov.payment.mypay4.service.EnteTipoDovutoService;
import it.regioneveneto.mygov.payment.mypay4.service.TassonomiaService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.EmailUtil;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.impl.PagamentiTelematiciCCPPaImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnExpression("${pa.gpd.enabled:true} or ${pa.gpd.preload:true}")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class GpdService {

  private final static BigDecimal EURO_TO_CENTS = new BigDecimal(100L);
  private static final ThreadLocal<SimpleDateFormat> date_fmt_UUID = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyMMddHHmmssSSS"));
  private static final String SEP = "-";

  @Autowired(required = false)
  private GpdClientService gpdClientService;
  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;
  @Autowired
  private DatiMarcaBolloDigitaleService datiMarcaBolloDigitaleService;
  @Autowired
  private TassonomiaService tassonomiaService;
  @Autowired
  private DovutoPreloadDao dovutoPreloadDao;

  public String generateRandomIupd(String paFiscalCode) {
    return String.format("%x", Long.valueOf(paFiscalCode)) + SEP +
        StringUtils.remove(StringUtils.substringBeforeLast(StringUtils.substringAfter(UUID.randomUUID().toString(), SEP), SEP), SEP) +
        SEP + String.format("%x", Long.valueOf(date_fmt_UUID.get().format(new Date())));
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public DebtPositionDetail getDebtPosition(String paFiscalCode, String iupd) {
    return gpdClientService.getDebtPosition(paFiscalCode, iupd);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void newDebtPosition(List<Dovuto> dovuti, Optional<DovutoMultibeneficiario> dovMulti) {
    Dovuto dovuto = dovuti.get(0);
    String iupd = dovuto.getGpdIupd();
    if (StringUtils.isBlank(iupd)) {
      Exception e = new RuntimeException("iupd missing");
      log.error("error creating DebtPosition - iupd missing", e);
      throw new MyPayException(e);
    }
    String organizationFiscalCode = dovuto.getNestedEnte().getCodiceFiscaleEnte();
    //map to DebtPosition
    DebtPosition debtPosition = mapDebtPosition(dovuti, dovMulti);

    gpdClientService.createDebtPosition(organizationFiscalCode, true, debtPosition);
  }


  @Transactional(propagation = Propagation.REQUIRED)
  public Dovuto updateDebtPosition(List<Dovuto> dovuti, Optional<DovutoMultibeneficiario> dovMulti) {
    Dovuto dovuto = dovuti.get(0);
    String iupd = dovuto.getGpdIupd();
    if (StringUtils.isBlank(iupd)) {
      Exception e = new RuntimeException("iupd missing");
      log.error("error updating DebtPosition - iupd missing", e);
      throw new MyPayException(e);
    }
    String organizationFiscalCode = dovuto.getNestedEnte().getCodiceFiscaleEnte();
    //map to DebtPosition
    DebtPosition debtPosition = mapDebtPosition(dovuti, dovMulti);
    gpdClientService.updateDebtPosition(organizationFiscalCode, iupd, true, debtPosition);
    return dovuto;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void newDebtPosition(String codiceFiscaleEnte, String gpdIupd, String codIuv, Double numRpDatiVersDatiSingVersImportoSingoloVersamento,
                              String codRpSoggPagIdUnivPagCodiceIdUnivoco,Character codRpSoggPagIdUnivPagTipoIdUnivoco, String deRpSoggPagAnagraficaPagatore,
                              String deRpSoggPagIndirizzoPagatore, String deRpSoggPagCivicoPagatore, String codRpSoggPagCapPagatore,
                              String deRpSoggPagLocalitaPagatore, String deRpSoggPagProvinciaPagatore, String codRpSoggPagNazionePagatore,
                              String deRpSoggPagEmailPagatore, String codIpaEnte, String deNomeEnte, Date dtCreazione,
                              String deRpDatiVersDatiSingVersCausaleVersamento,
                              Date dtRpDatiVersDataEsecuzionePagamento, String codRpDatiVersDatiSingVersIbanAccredito, String codTipoDovuto,
                              String datiSpecificiRiscossione, Optional<DovutoMultibeneficiario> dovMulti) {
    newDebtPosition(createDovutoFromFields(codiceFiscaleEnte, gpdIupd, codIuv, numRpDatiVersDatiSingVersImportoSingoloVersamento,
        codRpSoggPagIdUnivPagCodiceIdUnivoco, codRpSoggPagIdUnivPagTipoIdUnivoco,deRpSoggPagAnagraficaPagatore,
        deRpSoggPagIndirizzoPagatore, deRpSoggPagCivicoPagatore, codRpSoggPagCapPagatore,
        deRpSoggPagLocalitaPagatore, deRpSoggPagProvinciaPagatore, codRpSoggPagNazionePagatore,
        deRpSoggPagEmailPagatore, codIpaEnte, deNomeEnte, dtCreazione,
        deRpDatiVersDatiSingVersCausaleVersamento,
        dtRpDatiVersDataEsecuzionePagamento, codRpDatiVersDatiSingVersIbanAccredito, codTipoDovuto, datiSpecificiRiscossione, Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA), dovMulti);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Dovuto updateDebtPosition(String codiceFiscaleEnte, String gpdIupd, String codIuv, Double numRpDatiVersDatiSingVersImportoSingoloVersamento,
                                   String codRpSoggPagIdUnivPagCodiceIdUnivoco, Character codRpSoggPagIdUnivPagTipoIdUnivoco, String deRpSoggPagAnagraficaPagatore,
                                   String deRpSoggPagIndirizzoPagatore, String deRpSoggPagCivicoPagatore, String codRpSoggPagCapPagatore,
                                   String deRpSoggPagLocalitaPagatore, String deRpSoggPagProvinciaPagatore, String codRpSoggPagNazionePagatore,
                                   String deRpSoggPagEmailPagatore, String codIpaEnte, String deNomeEnte, Date dtCreazione,
                                   String deRpDatiVersDatiSingVersCausaleVersamento,
                                   Date dtRpDatiVersDataEsecuzionePagamento, String codRpDatiVersDatiSingVersIbanAccredito, String codTipoDovuto,
                                   String datiSpecificiRiscossione, Optional<DovutoMultibeneficiario> dovMulti) {

    return updateDebtPosition(createDovutoFromFields(codiceFiscaleEnte, gpdIupd, codIuv, numRpDatiVersDatiSingVersImportoSingoloVersamento,
        codRpSoggPagIdUnivPagCodiceIdUnivoco, codRpSoggPagIdUnivPagTipoIdUnivoco,deRpSoggPagAnagraficaPagatore,
        deRpSoggPagIndirizzoPagatore, deRpSoggPagCivicoPagatore, codRpSoggPagCapPagatore,
        deRpSoggPagLocalitaPagatore, deRpSoggPagProvinciaPagatore, codRpSoggPagNazionePagatore,
        deRpSoggPagEmailPagatore, codIpaEnte, deNomeEnte, dtCreazione,
        deRpDatiVersDatiSingVersCausaleVersamento,
        dtRpDatiVersDataEsecuzionePagamento, codRpDatiVersDatiSingVersIbanAccredito, codTipoDovuto, datiSpecificiRiscossione ,Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA), dovMulti);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteDebtPosition(String organizationFiscalCode, String iupd, String iuv) {
    try {
      Assert.isTrue(StringUtils.isNotBlank(organizationFiscalCode), "organizationFiscalCode is empty");
      Assert.isTrue(StringUtils.isNotBlank(iupd), "iupd is empty");
      //invoke GPD API
      gpdClientService.deleteDebtPosition(organizationFiscalCode, iupd, iuv);
    } catch (HttpClientErrorException.NotFound nfe) {
      int statusCode = nfe.getRawStatusCode();
      //if the debt position was not found (404)
      if (statusCode == 404) {
        log.info("error deleting debtPosition with code {} - ignoring the error: {}", statusCode, nfe.getMessage());
      } else {
        log.error("error deleting debtPosition", nfe);
        throw new MyPayException(nfe);
      }
    } catch (MyPayException mpe) {
      log.error("error deleting debtPosition", mpe);
      throw mpe;
    } catch (Exception e) {
      log.error("error deleting debtPosition", e);
      throw new MyPayException(e);
    }
  }


  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteDebtPositionForCodice9(String organizationFiscalCode, String iupd, String iuv) {
    try {
      Assert.isTrue(StringUtils.isNotBlank(organizationFiscalCode), "organizationFiscalCode is empty");
      Assert.isTrue(StringUtils.isNotBlank(iupd), "iupd is empty");
      //invoke GPD API
      gpdClientService.deleteDebtPosition(organizationFiscalCode, iupd, iuv);
    } catch (HttpClientErrorException.NotFound nfe) {
      int statusCode = nfe.getRawStatusCode();
      //if the debt position was not found (404) or exists in payment state (409)
      if (statusCode == 404 || statusCode == 409) {
        log.info("error deleting debtPosition with code {} - ignoring the error: {}", statusCode, nfe.getMessage());
      } else {
        log.error("error deleting debtPosition", nfe);
        throw new MyPayException(nfe);
      }
    } catch (MyPayException mpe) {
      log.error("error deleting debtPosition", mpe);
      throw mpe;
    } catch (Exception e) {
      log.error("error deleting debtPosition", e);
      throw new MyPayException(e);
    }
  }


  public DebtPosition mapDebtPosition(List<Dovuto> dovuti, Optional<DovutoMultibeneficiario> dmb) {

    Set<String> entiDistinct = dovuti.stream().map(dovuto -> dovuto.getNestedEnte().getCodiceFiscaleEnte()).collect(Collectors.toUnmodifiableSet());
    if(entiDistinct.size()>1){
      log.error("error creating debt position: dovuti on multiple ente: {}", entiDistinct);
      throw new MyPayException("error creating debt position: dovuti on multiple ente");
    }

    Dovuto dovutoPrimario = dovuti.get(0);
    String iupd = dovutoPrimario.getGpdIupd();

    Set<String> dueDateSet = new HashSet<>();
    List<Transfer> transferList = new ArrayList<>();
    AtomicInteger index = new AtomicInteger(1);
    AtomicLong amount = new AtomicLong(0);
    Map<String, Object> dovutoPrimarioData = new HashMap<>();
    dovuti.forEach(dovuto -> {

      String remittanceInformation = dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento();

      EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(dovuto.getCodTipoDovuto(), dovuto.getNestedEnte().getCodIpaEnte(), true)
          .orElseThrow(() -> new MyPayException("invalid tipo dovuto " + dovuto.getCodTipoDovuto() + " on ente " + dovuto.getNestedEnte().getCodIpaEnte()));
      if(index.get()==1)
        dovutoPrimarioData.put("enteTipoDovuto", enteTipoDovuto);
      if(enteTipoDovuto.isFlgScadenzaObbligatoria())
        dueDateSet.add(Utilities.convertDateToISOString(Utilities.toDateAtAtEndOfDay(dovutoPrimario.getDtRpDatiVersDataEsecuzionePagamento())));
      if(dueDateSet.size()>1){
        log.error("error creating debt position: dovuti having different dueDate: {}", dueDateSet);
        throw new MyPayException("error creating debt position: dovuti having different dueDate");
      }
      long amountTransfer = dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento().multiply(EURO_TO_CENTS).longValue();
      var transferBuilder = Transfer.builder()
          .idTransfer(Transfer.ID_TRANSFER.get(index.getAndIncrement()))
          .amount(amountTransfer)
          .organizationFiscalCode(dovuto.getNestedEnte().getCodiceFiscaleEnte())
          .remittanceInformation(StringUtils.isBlank(remittanceInformation) ? "Non disponibile" : StringUtils.substring(remittanceInformation,0,139))
          .category(tassonomiaService.getCleanTransferCategory(dovuto, enteTipoDovuto));
      if (enteTipoDovuto.getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
        DatiMarcaBolloDigitale d = Optional.ofNullable(dovuto.getMygovDatiMarcaBolloDigitaleId()).map(datiMarcaBolloDigitaleService::getById)
            .orElseThrow(() -> new MyPayException("dati marca bollo digitale not found for id " + dovuto.getMygovDovutoId()));
        transferBuilder.stamp(Stamp.builder()
            .hashDocument(d.getHashDocumento())
            .provincialResidence(d.getProvinciaResidenza())
            .stampType(d.getTipoBollo())
            .build());
      } else {
        transferBuilder
            .iban(PagamentiTelematiciCCPPaImpl.getIbanAccredito(dovuto.getNestedEnte(), enteTipoDovuto, null, Constants.PAY_PRESSO_PSP))
            .postalIban(enteTipoDovuto.getIbanAccreditoPi());
      }
      amount.addAndGet(amountTransfer);
      transferList.add(transferBuilder.build());
    });

    dmb.ifPresent(dovMulti -> {
      // mypay only support multibeneficiary payment if carrello has 1 IUV and 2 transfer
      if(transferList.size()>1){
        log.info("invalid multi-beneficiary payment: dovuto[{}] multiple transfer [{}]", dovutoPrimario.getMygovDovutoId(), transferList.size());
        throw new MyPayException("invalid multi-beneficiary payment: multiple transfer");
      }

      String transferCategory;
      if (StringUtils.isNotBlank(dovMulti.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione())) {
        Matcher matcher = Constants.DATI_SPECIFICI_RISCOSSIONE_REGEX.matcher(dovMulti.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione());
        if (matcher.find())
          transferCategory = (matcher.group(1));
        else
          throw new ValidatorException("invalid datiSpecificiRiscossione format [" + dovMulti.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione() + "]");
      } else {
        transferCategory = transferList.get(0).getCategory();
      }

      String remittanceInformation = StringUtils.firstNonBlank(dovMulti.getDeRpDatiVersDatiSingVersCausaleVersamento(),
              dovutoPrimario.getDeRpDatiVersDatiSingVersCausaleVersamento(), "Non disponibile");

      long transferAmount = dovMulti.getNumRpDatiVersDatiSingVersImportoSingoloVersamento().multiply(EURO_TO_CENTS).longValue();
      transferList.add( Transfer.builder()
          .idTransfer(Transfer.ID_TRANSFER.ID_2)
          .amount(transferAmount)
          .organizationFiscalCode(dovMulti.getCodiceFiscaleEnte())
          .remittanceInformation(StringUtils.substring(remittanceInformation,0,139))
          .category(transferCategory)
          .iban(dovMulti.getCodRpDatiVersDatiSingVersIbanAccredito())
          .postalIban(null)
          .build() );
      amount.addAndGet(transferAmount);
    });

    EnteTipoDovuto enteTipoDovutoPrimario = (EnteTipoDovuto)dovutoPrimarioData.get("enteTipoDovuto");

    PaymentOption paymentOption = PaymentOption.builder()
        .nav(Utilities.iuvToNumeroAvviso(dovutoPrimario.getCodIuv(), dovutoPrimario.getNestedEnte().getApplicationCode(), false))
        .iuv(dovutoPrimario.getCodIuv())
        .amount(amount.get())
        .description(PagamentiTelematiciCCPPaImpl.calcolaCausaleRispostaCCP(dovutoPrimario, enteTipoDovutoPrimario))
        .isPartialPayment(false)
        .dueDate(dueDateSet.stream().findFirst().orElse(Utilities.convertDateToISOString(PagamentiTelematiciCCPPaImpl.MAX_DATE)))
        .fee(0L)
        .transfer(transferList)
        .build();

    String fullName = dovutoPrimario.getDeRpSoggPagAnagraficaPagatore();


    return DebtPosition.builder()
        .iupd(iupd)
        .type(dovutoPrimario.getCodRpSoggPagIdUnivPagTipoIdUnivoco().toString().equals("F")?DebtPosition.PERSON_TYPE.F:DebtPosition.PERSON_TYPE.G)
        .fiscalCode(dovutoPrimario.getCodRpSoggPagIdUnivPagCodiceIdUnivoco())
        .fullName(StringUtils.isBlank(fullName) ? "Non disponibile" : fullName)
        .streetName(dovutoPrimario.getDeRpSoggPagIndirizzoPagatore())
        .civicNumber(dovutoPrimario.getDeRpSoggPagCivicoPagatore())
        .postalCode(dovutoPrimario.getCodRpSoggPagCapPagatore())
        .city(dovutoPrimario.getDeRpSoggPagLocalitaPagatore())
        .province(dovutoPrimario.getDeRpSoggPagProvinciaPagatore())
        .country(dovutoPrimario.getCodRpSoggPagNazionePagatore())
        .email(isValidEmail(dovutoPrimario.getDeRpSoggPagEmailPagatore(), dovutoPrimario.getDtCreazione())?dovutoPrimario.getDeRpSoggPagEmailPagatore():null)
        .switchToExpired(enteTipoDovutoPrimario.isFlgScadenzaObbligatoria())
        .companyName(dovutoPrimario.getNestedEnte().getDeNomeEnte())
        .paymentOption(List.of(paymentOption))
        .build();
  }


  public static List<Dovuto> createDovutoFromFields(String codiceFiscaleEnte, String gpdIupd, String codIuv, Double numRpDatiVersDatiSingVersImportoSingoloVersamento,
                                                    String codRpSoggPagIdUnivPagCodiceIdUnivoco,Character codRpSoggPagIdUnivPagTipoIdUnivoco, String deRpSoggPagAnagraficaPagatore,
                                                    String deRpSoggPagIndirizzoPagatore, String deRpSoggPagCivicoPagatore, String codRpSoggPagCapPagatore,
                                                    String deRpSoggPagLocalitaPagatore, String deRpSoggPagProvinciaPagatore, String codRpSoggPagNazionePagatore,
                                                    String deRpSoggPagEmailPagatore, String codIpaEnte, String deNomeEnte, Date dtCreazione,
                                                    String deRpDatiVersDatiSingVersCausaleVersamento,
                                                    Date dtRpDatiVersDataEsecuzionePagamento, String codRpDatiVersDatiSingVersIbanAccredito, String codTipoDovuto, String datiSpecificiRiscossione, char gpdStatus) {
    Dovuto dovuto = Dovuto.builder()
        .nestedEnte(Ente.builder()
            .codiceFiscaleEnte(codiceFiscaleEnte)
            .deNomeEnte(deNomeEnte)
            .codRpDatiVersDatiSingVersIbanAccredito(codRpDatiVersDatiSingVersIbanAccredito)
            .codIpaEnte(codIpaEnte)
            .build())
        .gpdIupd(gpdIupd)
        .codRpSoggPagIdUnivPagCodiceIdUnivoco(codRpSoggPagIdUnivPagCodiceIdUnivoco)
        .codRpSoggPagIdUnivPagTipoIdUnivoco(codRpSoggPagIdUnivPagTipoIdUnivoco)
        .deRpSoggPagAnagraficaPagatore(deRpSoggPagAnagraficaPagatore)
        .deRpSoggPagIndirizzoPagatore(deRpSoggPagIndirizzoPagatore)
        .deRpSoggPagCivicoPagatore(deRpSoggPagCivicoPagatore)
        .codRpSoggPagCapPagatore(codRpSoggPagCapPagatore)
        .deRpSoggPagLocalitaPagatore(deRpSoggPagLocalitaPagatore)
        .deRpSoggPagProvinciaPagatore(deRpSoggPagProvinciaPagatore)
        .codRpSoggPagNazionePagatore(codRpSoggPagNazionePagatore)
        .deRpSoggPagEmailPagatore(deRpSoggPagEmailPagatore)
        .dtCreazione(dtCreazione)
        .numRpDatiVersDatiSingVersImportoSingoloVersamento(BigDecimal.valueOf(numRpDatiVersDatiSingVersImportoSingoloVersamento))
        .deRpDatiVersDatiSingVersCausaleVersamento(deRpDatiVersDatiSingVersCausaleVersamento)
        .dtRpDatiVersDataEsecuzionePagamento(dtRpDatiVersDataEsecuzionePagamento)
        .codIuv(codIuv)
        .deRpDatiVersDatiSingVersDatiSpecificiRiscossione(datiSpecificiRiscossione)
        .codTipoDovuto(codTipoDovuto)
        .gpdStatus(gpdStatus)
        .build();

    return List.of(dovuto);
  }


  public void managePreload(char operation, Dovuto dovutoNew, Long idDovutoOld) {
    DovutoPreload dp = null;
    switch (operation) {
      case 'I':
        dp = DovutoPreload.builder()
                .mygovDovutoId(dovutoNew.getMygovDovutoId())
                .gpdIupd(dovutoNew.getGpdIupd())
                .nuovoGpdStatus(Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA)
                .dtUltimaModifica(new Date())
                .mygovEnteId(dovutoNew.getNestedEnte().getMygovEnteId())
                .build();

        dovutoPreloadDao.insert(dp);
        break;
      case 'M':
        dp = dovutoPreloadDao.getByIdDovuto(idDovutoOld);

        if (dp == null) {
          dp = DovutoPreload.builder()
                  .mygovDovutoId(dovutoNew.getMygovDovutoId())
                  .gpdIupd(dovutoNew.getGpdIupd())
                  .nuovoGpdStatus(Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA)
                  .dtUltimaModifica(new Date())
                  .mygovEnteId(dovutoNew.getNestedEnte().getMygovEnteId())
                  .build();

          dovutoPreloadDao.insert(dp);
        } else {
          if (dp.getGpdStatus() != null) {
            switch (dp.getGpdStatus()) {
              case Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_INVIATO_A_PAGOPA:
              case Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA:
              case Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA:
                dovutoPreloadDao.updateNuovoGpdStatus(List.of(dp.getMygovDovutoId()), Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA);
                break;
              case Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA:
                dovutoPreloadDao.updateNuovoGpdStatus(List.of(dp.getMygovDovutoId()), Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA);
                break;
            }
          }
          if (dp.getMygovDovutoId() != dovutoNew.getMygovDovutoId())
            dovutoPreloadDao.updateIdDovuto(idDovutoOld, dovutoNew.getMygovDovutoId());
          if (StringUtils.isBlank(dp.getGpdIupd()) && StringUtils.isNotBlank(dovutoNew.getGpdIupd()))
            dovutoPreloadDao.updateIupd(dp.getMygovDovutoPreloadId(),dovutoNew.getGpdIupd());

        }
        break;
      case 'A':
        dp = dovutoPreloadDao.getByIdDovuto(dovutoNew.getMygovDovutoId());
        if (dp != null) {
          if (dp.getGpdStatus() != null) {
            switch (dp.getGpdStatus()) {
              case Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA:
              case Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_INVIATO_A_PAGOPA:
              case Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA:
                dovutoPreloadDao.updateNuovoGpdStatus(List.of(dp.getMygovDovutoId()), Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA);
                break;
              case Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA:
                dovutoPreloadDao.updateNuovoGpdStatus(List.of(dp.getMygovDovutoId()), ' ');
                break;
            }
          } else {
            dovutoPreloadDao.updateNuovoGpdStatus(List.of(dp.getMygovDovutoId()), ' ');
          }
        }
        break;
    }

  }

  private boolean isValidEmail(String email, Date date) {

    if (email == null || date == null) {
      return false;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.YEAR, -5);
    Date fiveYearsAgo = calendar.getTime();

    if (date.before(fiveYearsAgo)) {
      return false;
    }

    return EmailUtil.validateEmail(email);
  }




}
