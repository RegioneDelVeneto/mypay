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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.*;
import gov.telematici.pagamenti.ws.ppthead.IntestazionePPT;
import it.gov.digitpa.schemas._2011.pagamenti.RPT;
import it.gov.digitpa.schemas._2011.pagamenti.RT;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RptRtDao;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RptRtDettaglioDao;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.RptRtDettaglioDto;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.fesp.FespException;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.CarrelloRpt;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RpE;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRtDettaglio;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class RptRtService {

  @Resource
  private RptRtService self;
  @Autowired
  private RptRtDao rptRtDao;
  @Autowired
  private RptRtDettaglioDao rptRtDettaglioDao;

  @Value("${pa.pspDefaultModelloPagamento}")
  private int modelloPagamento;

  @Value("${task.inviaRPTAttivate.batchRowLimit:${task.common.batchRowLimit}}")
  private int inviaRPTAttivateBatchRowLimit;


  @Autowired
  ConfigurableEnvironment env;

  public RptRt getById(Long mygovRptRtId) { return rptRtDao.getById(mygovRptRtId);  }

  public Optional<RptRt> getByIdLockOrSkip(Long mygovRptRtId) { return rptRtDao.getByIdLockOrSkip(mygovRptRtId);  }

  public List<RptRt> getByCart(Long mygovCarrelloRptId) { return rptRtDao.getByCart(mygovCarrelloRptId);  }

  public RptRt getRPTByIdDominioIuvCdContestoPagamento(String idDominio, String idUnivocoVersamento, String codiceContestoPagamento) {
    List<RptRt> rptRts = rptRtDao.getRPTByIdDominioIuvCdContestoPagamento(idDominio, idUnivocoVersamento, codiceContestoPagamento);
    if (rptRts != null && rptRts.size() > 1)
      throw new FespException("'mygovRptRt' is not unique: " + rptRts.size() + " records found.");
    return CollectionUtils.isEmpty(rptRts) ? null : rptRts.get(0);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.MANDATORY)
  public List<RptRt> getRPTAttivateForInviaRPT() {
    List<RptRt> rptRts = rptRtDao.getRPTAttivateForInviaRPT(inviaRPTAttivateBatchRowLimit);
    if(log.isTraceEnabled()) {
      List<RptRt> rptRtsNoLock = rptRtDao.getRPTAttivateForInviaRPTNoLock(inviaRPTAttivateBatchRowLimit);
      log.trace("rptRts: {} - rptRtsNoLock: {}",rptRts.size(), rptRtsNoLock.size());
    }
    return rptRts;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.NOT_SUPPORTED)
  public List<RptRt> getRPTPendentiForChiediStatoRPT(int modelloPagamento) {
    Assert.isTrue(Carrello.VALID_MODELLOPAGAMENTO.contains(modelloPagamento), "invalid value for modelloPagamento");

    int deltaMinutes;
    try {
      String deltaMinutesString = env.getProperty("task.chiediStatoRPT.deltaMinutes."+modelloPagamento);
      deltaMinutes = Integer.parseInt(deltaMinutesString);
      Assert.isTrue(deltaMinutes > 0, "invalid value for deltaMinutes");
    } catch (Exception e){
      throw new MyPayException("invalid value for deltaMinutes", e);
    }
    // optimization: only retrieve used fields on RptRt
    List<RptRt> rptRts = rptRtDao.getRPTPendentiForChiediStatoRPT(modelloPagamento,deltaMinutes);
    return rptRts;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public Optional<RptRt> getRPTByIdDominioIuvCdContestoPagamentoForInviaRPT(String idDominio, String idUnivocoVersamento, String codiceContestoPagamento) {
    List<RptRt> rptRts = rptRtDao.getRPTByIdDominioIuvCdContestoPagamentoForInviaRPT(idDominio, idUnivocoVersamento, codiceContestoPagamento);
    if(rptRts.isEmpty()){
      List<RptRt> rptRts2 = rptRtDao.getRPTByIdDominioIuvCdContestoPagamentoForInviaRPTNoLock(idDominio, idUnivocoVersamento, codiceContestoPagamento);
      log.debug("DEBUG rtpRtNoLock {}", rptRts2.isEmpty()?"empty":rptRts2.get(0));
    }
    if (rptRts != null && rptRts.size() > 1)
      throw new FespException("'mygovRptRt' is not unique: " + rptRts.size() + " records found.");
    return CollectionUtils.isEmpty(rptRts) ? Optional.empty() : Optional.of(rptRts.get(0));
  }

  public RptRt getRTByIdDominioIuvCdContestoPagamento(String idDominio, String idUnivocoVersamento, String codiceContestoPagamento) {
    List<RptRt> rptRts = rptRtDao.getRTByIdDominioIuvCdContestoPagamento(idDominio, idUnivocoVersamento, codiceContestoPagamento);
    if (rptRts != null && rptRts.size() > 1)
      throw new FespException("'mygovRptRt' is not unique: " + rptRts.size() + " records found.");
    return CollectionUtils.isEmpty(rptRts) ? null : rptRts.get(0);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RptRtDettaglio insertRptRtDettaglio(RptRt rptRt, RptRtDettaglioDto dettDto) {
    RptRtDettaglio dettaglio = RptRtDettaglio.builder()
        .mygovRptRtId(rptRt)
        .numRptDatiVersDatiSingVersImportoSingoloVersamento(dettDto.getNumRptDatiVersDatiSingVersImportoSingoloVersamento())
        .numRptDatiVersDatiSingVersCommissioneCaricoPa(dettDto.getNumRptDatiVersDatiSingVersCommissioneCaricoPa())
        .deRptDatiVersDatiSingVersIbanAccredito(dettDto.getDeRptDatiVersDatiSingVersIbanAccredito())
        .deRptDatiVersDatiSingVersBicAccredito(dettDto.getDeRptDatiVersDatiSingVersBicAccredito())
        .deRptDatiVersDatiSingVersIbanAppoggio(dettDto.getDeRptDatiVersDatiSingVersIbanAppoggio())
        .deRptDatiVersDatiSingVersBicAppoggio(dettDto.getDeRptDatiVersDatiSingVersBicAppoggio())
        .codRptDatiVersDatiSingVersCredenzialiPagatore(dettDto.getCodRptDatiVersDatiSingVersCredenzialiPagatore())
        .deRptDatiVersDatiSingVersCausaleVersamento(dettDto.getDeRptDatiVersDatiSingVersCausaleVersamento())
        .deRptDatiVersDatiSingVersDatiSpecificiRiscossione(dettDto.getDeRptDatiVersDatiSingVersDatiSpecificiRiscossione())
        .numRtDatiPagDatiSingPagSingoloImportoPagato(dettDto.getNumRtDatiPagDatiSingPagSingoloImportoPagato())
        .deRtDatiPagDatiSingPagEsitoSingoloPagamento(dettDto.getDeRtDatiPagDatiSingPagEsitoSingoloPagamento())
        .dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento(dettDto.getDtRtDatiPagDatiSingPagDataEsitoSingoloPagamento())
        .codRtDatiPagDatiSingPagIdUnivocoRiscossione(dettDto.getCodRtDatiPagDatiSingPagIdUnivocoRiscossione())
        .deRtDatiPagDatiSingPagCausaleVersamento(dettDto.getDeRtDatiPagDatiSingPagCausaleVersamento())
        .deRtDatiPagDatiSingPagDatiSpecificiRiscossione(dettDto.getDeRtDatiPagDatiSingPagDatiSpecificiRiscossione())
        .numRtDatiPagDatiSingPagCommissioniApplicatePsp(dettDto.getNumRtDatiPagDatiSingPagCommissioniApplicatePsp())
        .codRtDatiPagDatiSingPagAllegatoRicevutaTipo(dettDto.getCodRtDatiPagDatiSingPagAllegatoRicevutaTipo())
        .blbRtDatiPagDatiSingPagAllegatoRicevutaTest(dettDto.getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest())
        .codRptDatiVersDatiSingVersDatiMbdTipoBollo(dettDto.getCodRptDatiVersDatiSingVersDatiMbdTipoBollo())
        .codRptDatiVersDatiSingVersDatiMbdHashDocumento(dettDto.getCodRptDatiVersDatiSingVersDatiMbdHashDocumento())
        .codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza(dettDto.getCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza())
        .build();

    long mygovRptRtDettaglioId = rptRtDettaglioDao.insert(dettaglio);
    dettaglio.setMygovRptRtDettaglioId(mygovRptRtDettaglioId);
    return dettaglio;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RptRt updateRispostaRptById(Long mygovRptRtId, String deRptInviarptEsito, Integer numRptInviarptRedirect, String codRptInviarptUrl,
                                     String codRptInviarptFaultCode, String codRptInviarptFaultString, String codRptInviarptId, String deRptInviarptDescription,
                                     Integer numRptInviarptSerial, String codRptInviarptOriginaltFaultCode, String deRptInviarptOriginaltFaultString,
                                     String deRptInviarptOriginaltFaultDescription) throws UnsupportedEncodingException, MalformedURLException {

    RptRt mygovRptRt = getById(mygovRptRtId);

    mygovRptRt.setDtUltimaModificaRpt(new Date());
    mygovRptRt.setDeRptInviarptEsito(deRptInviarptEsito);
    mygovRptRt.setNumRptInviarptRedirect(numRptInviarptRedirect);
    mygovRptRt.setCodRptInviarptUrl(codRptInviarptUrl);
    mygovRptRt.setCodRptInviarptFaultCode(codRptInviarptFaultCode);
    mygovRptRt.setCodRptInviarptFaultString(codRptInviarptFaultString);
    mygovRptRt.setCodRptInviarptId(codRptInviarptId);

    // Tronco deRptInviarptDescription se non è null e la lunghezza è maggiore di 1024
    if (StringUtils.isNotBlank(deRptInviarptDescription)) {
      if (deRptInviarptDescription.length() > 1024) {
        mygovRptRt.setDeRptInviarptDescription(deRptInviarptDescription.substring(0, 1024));
      } else {
        mygovRptRt.setDeRptInviarptDescription(deRptInviarptDescription);
      }
    } else {
      mygovRptRt.setDeRptInviarptDescription(deRptInviarptDescription);
    }

    mygovRptRt.setCodRptInviarptOriginalFaultCode(codRptInviarptOriginaltFaultCode);
    mygovRptRt.setDeRptInviarptOriginalFaultString(deRptInviarptOriginaltFaultString);
    if (StringUtils.isNotBlank(deRptInviarptOriginaltFaultDescription)) {
      if (deRptInviarptOriginaltFaultDescription.length() > 1024)
        mygovRptRt.setDeRptInviarptOriginalFaultDescription(deRptInviarptOriginaltFaultDescription.substring(0, 1024));
      else
        mygovRptRt.setDeRptInviarptOriginalFaultDescription(deRptInviarptOriginaltFaultDescription);
    }

    mygovRptRt.setNumRptInviarptSerial(numRptInviarptSerial);

    String idSessionSPC = null;
    if (StringUtils.isNotBlank(codRptInviarptUrl)) {
      //estrarre idSessioSPC
      Map<String, String> parametersMap = Utilities.splitQuery(new URL(codRptInviarptUrl));
      idSessionSPC = parametersMap.get("idSession");
    }
    mygovRptRt.setIdSession(idSessionSPC);

    log.debug("deRptInviarptEsito :" + deRptInviarptEsito);
    log.debug("numRptInviarptRedirect :" + numRptInviarptRedirect);
    log.debug("codRptInviarptUrl :" + codRptInviarptUrl);
    log.debug("codRptInviarptFaultCode :" + codRptInviarptFaultCode);
    log.debug("codRptInviarptFaultString :" + codRptInviarptFaultString);
    log.debug("codRptInviarptId :" + codRptInviarptId);
    log.debug("deRptInviarptDescription :" + deRptInviarptDescription);
    log.debug("numRptInviarptSerial :" + numRptInviarptSerial);
    log.debug("idSessionSPC :" + idSessionSPC);
    log.debug("codRptInviarptOriginaltFaultCode :" + codRptInviarptOriginaltFaultCode);
    log.debug("deRptInviarptOriginaltFaultString :" + deRptInviarptOriginaltFaultString);
    log.debug("deRptInviarptOriginaltFaultDescription :" + deRptInviarptOriginaltFaultDescription);
    rptRtDao.update(mygovRptRt);

    return mygovRptRt;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RptRt insertRpt(String deRptInviarptPassword, String codRptInviarptIdPsp, String codRptInviarptIdIntermediarioPsp,
                         String codRptInviarptIdCanale, String deRptInviarptTipoFirma, String codRptInviarptIdIntermediarioPa,
                         String codRptInviarptIdStazioneIntermediarioPa, String codRptInviarptIdDominio, String codRptInviarptIdUnivocoVersamento,
                         String codRptInviarptCodiceContestoPagamento, String deRptVersioneOggetto, String codRptDomIdDominio, String codRptDomIdStazioneRichiedente,
                         String codRptIdMessaggioRichiesta, Date dtRptDataOraMessaggioRichiesta, String codRptAutenticazioneSoggetto,
                         String codRptSoggVersIdUnivVersTipoIdUnivoco, String codRptSoggVersIdUnivVersCodiceIdUnivoco, String deRptSoggVersAnagraficaVersante,
                         String deRptSoggVersIndirizzoVersante, String deRptSoggVersCivicoVersante, String codRptSoggVersCapVersante, String deRptSoggVersLocalitaVersante,
                         String deRptSoggVersProvinciaVersante, String codRptSoggVersNazioneVersante, String deRptSoggVersEmailVersante,
                         String codRptSoggPagIdUnivPagTipoIdUnivoco, String codRptSoggPagIdUnivPagCodiceIdUnivoco, String deRptSoggPagAnagraficaPagatore,
                         String deRptSoggPagIndirizzoPagatore, String deRptSoggPagCivicoPagatore, String codRptSoggPagCapPagatore, String deRptSoggPagLocalitaPagatore,
                         String deRptSoggPagProvinciaPagatore, String codRptSoggPagNazionePagatore, String deRptSoggPagEmailPagatore,
                         String codRptEnteBenefIdUnivBenefTipoIdUnivoco, String codRptEnteBenefIdUnivBenefCodiceIdUnivoco, String deRptEnteBenefDenominazioneBeneficiario,
                         String codRptEnteBenefCodiceUnitOperBeneficiario, String deRptEnteBenefDenomUnitOperBeneficiario, String deRptEnteBenefIndirizzoBeneficiario,
                         String deRptEnteBenefCivicoBeneficiario, String codRptEnteBenefCapBeneficiario, String deRptEnteBenefLocalitaBeneficiario,
                         String deRptEnteBenefProvinciaBeneficiario, String codRptEnteBenefNazioneBeneficiario, Date dtRptDatiVersDataEsecuzionePagamento,
                         BigDecimal numRptDatiVersImportoTotaleDaVersare, String codRptDatiVersTipoVersamento, String codRptDatiVersIdUnivocoVersamento,
                         String codRptDatiVersCodiceContestoPagamento, String deRptDatiVersIbanAddebito, String deRptDatiVersBicAddebito, String codRptDatiVersFirmaRicevuta,
                         List<RptRtDettaglioDto> versamenti, final Integer modelloPagamento, Long mygovRpEId) {

    log.debug("Invocato insertRpt con: codRptDatiVersIdUnivocoVersamento = [" + codRptDatiVersIdUnivocoVersamento + "] ");
    RptRt rptRt = RptRt.builder()
        .deRptInviarptPassword(deRptInviarptPassword)
        .codRptInviarptIdPsp(codRptInviarptIdPsp)
        .codRptInviarptIdIntermediarioPsp(codRptInviarptIdIntermediarioPsp)
        .codRptInviarptIdCanale(codRptInviarptIdCanale)
        .deRptInviarptTipoFirma(deRptInviarptTipoFirma)
        .codRptInviarptIdIntermediarioPa(codRptInviarptIdIntermediarioPa)
        .codRptInviarptIdStazioneIntermediarioPa(codRptInviarptIdStazioneIntermediarioPa)
        .codRptInviarptIdDominio(codRptInviarptIdDominio)
        .codRptInviarptIdUnivocoVersamento(codRptInviarptIdUnivocoVersamento)
        .codRptInviarptCodiceContestoPagamento(codRptInviarptCodiceContestoPagamento)
        .deRptVersioneOggetto(deRptVersioneOggetto)
        .codRptDomIdDominio(codRptDomIdDominio)
        .codRptDomIdStazioneRichiedente(codRptDomIdStazioneRichiedente)
        .codRptIdMessaggioRichiesta(codRptIdMessaggioRichiesta)
        .dtRptDataOraMessaggioRichiesta(dtRptDataOraMessaggioRichiesta)
        .codRptAutenticazioneSoggetto(codRptAutenticazioneSoggetto)
        .codRptSoggVersIdUnivVersTipoIdUnivoco(codRptSoggVersIdUnivVersTipoIdUnivoco)
        .codRptSoggVersIdUnivVersCodiceIdUnivoco(codRptSoggVersIdUnivVersCodiceIdUnivoco)
        .deRptSoggVersAnagraficaVersante(deRptSoggVersAnagraficaVersante)
        .deRptSoggVersIndirizzoVersante(deRptSoggVersIndirizzoVersante)
        .deRptSoggVersCivicoVersante(deRptSoggVersCivicoVersante)
        .codRptSoggVersCapVersante(codRptSoggVersCapVersante)
        .deRptSoggVersLocalitaVersante(deRptSoggVersLocalitaVersante)
        .deRptSoggVersProvinciaVersante(deRptSoggVersProvinciaVersante)
        .codRptSoggVersNazioneVersante(codRptSoggVersNazioneVersante)
        .deRptSoggVersEmailVersante(deRptSoggVersEmailVersante)
        .codRptSoggPagIdUnivPagTipoIdUnivoco(codRptSoggPagIdUnivPagTipoIdUnivoco)
        .codRptSoggPagIdUnivPagCodiceIdUnivoco(codRptSoggPagIdUnivPagCodiceIdUnivoco)
        .deRptSoggPagAnagraficaPagatore(deRptSoggPagAnagraficaPagatore)
        .deRptSoggPagIndirizzoPagatore(deRptSoggPagIndirizzoPagatore)
        .deRptSoggPagCivicoPagatore(deRptSoggPagCivicoPagatore)
        .codRptSoggPagCapPagatore(codRptSoggPagCapPagatore)
        .deRptSoggPagLocalitaPagatore(deRptSoggPagLocalitaPagatore)
        .deRptSoggPagProvinciaPagatore(deRptSoggPagProvinciaPagatore)
        .codRptSoggPagNazionePagatore(codRptSoggPagNazionePagatore)
        .deRptSoggPagEmailPagatore(deRptSoggPagEmailPagatore)
        .codRptEnteBenefIdUnivBenefTipoIdUnivoco(codRptEnteBenefIdUnivBenefTipoIdUnivoco)
        .codRptEnteBenefIdUnivBenefCodiceIdUnivoco(codRptEnteBenefIdUnivBenefCodiceIdUnivoco)
        .deRptEnteBenefDenominazioneBeneficiario(deRptEnteBenefDenominazioneBeneficiario)
        .codRptEnteBenefCodiceUnitOperBeneficiario(codRptEnteBenefCodiceUnitOperBeneficiario)
        .deRptEnteBenefDenomUnitOperBeneficiario(deRptEnteBenefDenomUnitOperBeneficiario)
        .deRptEnteBenefIndirizzoBeneficiario(deRptEnteBenefIndirizzoBeneficiario)
        .deRptEnteBenefCivicoBeneficiario(deRptEnteBenefCivicoBeneficiario)
        .codRptEnteBenefCapBeneficiario(codRptEnteBenefCapBeneficiario)
        .deRptEnteBenefLocalitaBeneficiario(deRptEnteBenefLocalitaBeneficiario)
        .deRptEnteBenefProvinciaBeneficiario(deRptEnteBenefProvinciaBeneficiario)
        .codRptEnteBenefNazioneBeneficiario(codRptEnteBenefNazioneBeneficiario)
        .dtRptDatiVersDataEsecuzionePagamento(dtRptDatiVersDataEsecuzionePagamento)
        .numRptDatiVersImportoTotaleDaVersare(numRptDatiVersImportoTotaleDaVersare)
        .codRptDatiVersTipoVersamento(codRptDatiVersTipoVersamento)
        .codRptDatiVersIdUnivocoVersamento(codRptDatiVersIdUnivocoVersamento)
        .codRptDatiVersCodiceContestoPagamento(codRptDatiVersCodiceContestoPagamento)
        .deRptDatiVersIbanAddebito(deRptDatiVersIbanAddebito)
        .deRptDatiVersBicAddebito(deRptDatiVersBicAddebito)
        .codRptDatiVersFirmaRicevuta(codRptDatiVersFirmaRicevuta)
        .modelloPagamento(modelloPagamento)
        .mygovRpEId(mygovRpEId).build();

    Long mygovRptRtId = rptRtDao.insert(rptRt);
    rptRt.setMygovRptRtId(mygovRptRtId);

    log.debug("insertRpt lettura caricamento singoli pagamenti");

    for (RptRtDettaglioDto dett : versamenti) {
      dett.setCodRptDatiVersDatiSingVersDatiMbdTipoBollo(
          StringUtils.isNotBlank(dett.getCodRptDatiVersDatiSingVersDatiMbdTipoBollo()) ? dett.getCodRptDatiVersDatiSingVersDatiMbdTipoBollo() : null);
      dett.setCodRptDatiVersDatiSingVersDatiMbdHashDocumento(
          StringUtils.isNotBlank(dett.getCodRptDatiVersDatiSingVersDatiMbdHashDocumento()) ? dett.getCodRptDatiVersDatiSingVersDatiMbdHashDocumento() : null);
      dett.setCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza(
          StringUtils.isNotBlank(dett.getCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza()) ? dett.getCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza() : null);
      insertRptRtDettaglio(rptRt, dett);
    }

    log.debug("fine insertRpt ");

    return rptRt;
  }

  public RptRt getRPTByCodRptIdMessaggioRichiesta(String riferimentoMessaggioRichiesta) {
    List<RptRt> rptRts = rptRtDao.getRPTByCodRptIdMessaggioRichiesta(riferimentoMessaggioRichiesta);
    if (rptRts != null && rptRts.size() > 1)
      throw new FespException("'mygovRptRt' is not unique: " + rptRts.size() + " records found.");
    return CollectionUtils.isEmpty(rptRts) ? null : rptRts.get(0);
  }

  public boolean validaUguaglianzaCampiRptRt(RptRt rptRt, RT rt) {
    if (!rptRt.getCodRptDomIdDominio().equals(rt.getDominio().getIdentificativoDominio())) {
      log.debug("validaUguaglianzaCampiRptRt: campo [IdentificativoDominio] non coerente");
      return false;
    }

    if (!((rptRt.getCodRptDomIdStazioneRichiedente() == null && rt.getDominio().getIdentificativoStazioneRichiedente() == null)
        || (rptRt.getCodRptDomIdStazioneRichiedente() != null
        && rptRt.getCodRptDomIdStazioneRichiedente().equals(rt.getDominio().getIdentificativoStazioneRichiedente())))) {
      log.debug("validaUguaglianzaCampiRptRt: campo [IdentificativoStazioneRichiedente] non coerente");
      return false;
    }

    if (!rptRt.getCodRptDomIdDominio().equals(rt.getDominio().getIdentificativoDominio())) {
      log.debug("validaUguaglianzaCampiRptRt: campo [IdentificativoDominio] non coerente");
      return false;
    }
    if (rt.getSoggettoVersante() != null && rptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco() != null) {

      if (!((rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco() == null
          && rptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco() == null)
          || (rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco() != null
          && rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco()
          .equalsIgnoreCase(rptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco())))) {
        log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.IdentificativoUnivocoVersante] non coerente");
        return false;
      }

      if (!((rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() == null
          && rptRt.getCodRptSoggVersIdUnivVersTipoIdUnivoco() == null)
          || (rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() != null
          && rt.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString()
          .equalsIgnoreCase(rptRt.getCodRptSoggVersIdUnivVersTipoIdUnivoco())))) {
        log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoVersante.TipoIdentificativoUnivoco] non coerente");
        return false;
      }
    }
    if ((rt.getSoggettoPagatore() == null && rptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco() != null)
        || (rt.getSoggettoPagatore() != null && rptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco() == null)) {
      log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore] non coerente");
      return false;
    }

    if (!((rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco() == null
        && rptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco() == null)
        || (rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco() != null
        && rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco()
        .equalsIgnoreCase(rptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco())))) {
      log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.CodiceIdentificativoUnivoco] non coerente");
      return false;
    }

    if (!((rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco() == null
        && rptRt.getCodRptSoggPagIdUnivPagTipoIdUnivoco() == null)
        || (rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco() != null
        && rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString()
        .equalsIgnoreCase(rptRt.getCodRptSoggPagIdUnivPagTipoIdUnivoco())))) {
      log.debug("validaUguaglianzaCampiRptRt: campo [SoggettoPagatore.TipoIdentificativoUnivoco] non coerente");
      return false;
    }
    return true;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateRispostaRtById(PaaInviaRTRisposta response, Long mygovRptRtId) {
    RptRt rptRt = self.getById(mygovRptRtId)
        .toBuilder()
        .codAckRt(Utilities.ifEqualsOrElse(Constants.ACK_STRING, Constants.ACK_STRING_KO).apply(response.getPaaInviaRTRisposta().getEsito()))
        .codRtInviartEsito(StringUtils.abbreviate(response.getPaaInviaRTRisposta().getEsito(),256))
        .build();
    if(response.getPaaInviaRTRisposta().getFault() != null) {
      var fb = response.getPaaInviaRTRisposta().getFault();
      rptRt = rptRt.toBuilder()
          .codRtInviartFaultCode(StringUtils.abbreviate(fb.getFaultCode(),256))
          .codRtInviartFaultString(StringUtils.abbreviate(fb.getFaultString(),256))
          .codRtInviartId(fb.getId())
          .deRtInviartDescription(StringUtils.abbreviate(fb.getDescription(),1024))
          .codRtInviartOriginalFaultCode(StringUtils.abbreviate(fb.getOriginalFaultCode(),256))
          .deRtInviartOriginalFaultString(StringUtils.abbreviate(fb.getOriginalFaultCode(),256))
          .deRtInviartOriginalFaultDescription(StringUtils.abbreviate(fb.getOriginalDescription(),1024))
          .numRtInviartSerial(fb.getSerial())
          .build();
    }
    Optional.ofNullable(rptRt.getDtCreazioneRt()).map(d -> new Date()).ifPresent(rptRt::setDtUltimaModificaRt);
    int updated = rptRtDao.update(rptRt);
    if(updated!=1)
      throw new MyPayException(String.format("invalid number of rows updated: %d for mygovRptRtId: %d", updated, mygovRptRtId));
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.MANDATORY)
  public void setInviarptEsitoOkById(Long mygovRptRtId) {
    RptRt rptRt = self.getById(mygovRptRtId)
        .toBuilder()
        .dtUltimaModificaRt(new Date())
        .deRptInviarptEsito(Constants.STATO_ESITO_OK)
        .codRptInviarptFaultCode(null)
        .codRptInviarptFaultString(null)
        .codRptInviarptId(null)
        .deRptInviarptDescription(null)
        .build();
    int updated = rptRtDao.update(rptRt);
    if(updated!=1)
      throw new MyPayException(String.format("invalid number of rows updated: %d for mygovRptRtId: %d", updated, mygovRptRtId));
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.MANDATORY)
  public void clearInviarptEsitoById(Long mygovRptRtId) {
    RptRt rptRt = self.getById(mygovRptRtId)
      .toBuilder()
      .dtUltimaModificaRt(new Date())
      .deRptInviarptEsito(null)
      .codRptInviarptFaultCode(null)
      .codRptInviarptFaultString(null)
      .codRptInviarptId(null)
      .deRptInviarptDescription(null)
      .build();
    int updated = rptRtDao.update(rptRt);
    if(updated!=1)
      throw new MyPayException(String.format("invalid number of rows updated: %d for mygovRptRtId: %d", updated, mygovRptRtId));
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RptRt updateRtById(Long mygovRptRtId, IntestazionePPT header, PaaInviaRT bodyrichiesta, RT rt,
                            byte[] rtPayload) {
    RptRt rptRt = self.getById(mygovRptRtId)
        .toBuilder()
        .deRtInviartTipoFirma(bodyrichiesta.getTipoFirma())
        .codRtInviartIdIntermediarioPa(header.getIdentificativoIntermediarioPA())
        .codRtInviartIdStazioneIntermediarioPa(header.getIdentificativoStazioneIntermediarioPA())
        .codRtInviartIdDominio(header.getIdentificativoDominio())
        .codRtInviartIdUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
        .codRtInviartCodiceContestoPagamento(header.getCodiceContestoPagamento())
        .deRtVersioneOggetto(rt.getVersioneOggetto())
        .codRtDomIdDominio(rt.getDominio().getIdentificativoDominio())
        .codRtDomIdStazioneRichiedente(rt.getDominio().getIdentificativoStazioneRichiedente())
        .codRtIdMessaggioRicevuta(rt.getIdentificativoMessaggioRicevuta())
        .dtRtDataOraMessaggioRicevuta(rt.getDataOraMessaggioRicevuta().toGregorianCalendar().getTime())
        .codRtRiferimentoMessaggioRichiesta(rt.getRiferimentoMessaggioRichiesta())
        .dtRtRiferimentoDataRichiesta(rt.getRiferimentoDataRichiesta().toGregorianCalendar().getTime())
        .codRtIstitAttesIdUnivAttesTipoIdUnivoco(rt.getIstitutoAttestante().getIdentificativoUnivocoAttestante().getTipoIdentificativoUnivoco().toString())
        .codRtIstitAttesIdUnivAttesCodiceIdUnivoco(rt.getIstitutoAttestante().getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco())
        .deRtIstitAttesDenominazioneAttestante(rt.getIstitutoAttestante().getDenominazioneAttestante())
        .codRtIstitAttesCodiceUnitOperAttestante(rt.getIstitutoAttestante().getCodiceUnitOperAttestante())
        .deRtIstitAttesDenomUnitOperAttestante(rt.getIstitutoAttestante().getDenomUnitOperAttestante())
        .deRtIstitAttesIndirizzoAttestante(rt.getIstitutoAttestante().getIndirizzoAttestante())
        .deRtIstitAttesCivicoAttestante(rt.getIstitutoAttestante().getCivicoAttestante())
        .codRtIstitAttesCapAttestante(rt.getIstitutoAttestante().getCapAttestante())
        .deRtIstitAttesLocalitaAttestante(rt.getIstitutoAttestante().getLocalitaAttestante())
        .deRtIstitAttesProvinciaAttestante(rt.getIstitutoAttestante().getProvinciaAttestante())
        .codRtIstitAttesNazioneAttestante(rt.getIstitutoAttestante().getNazioneAttestante())
        .codRtEnteBenefIdUnivBenefTipoIdUnivoco(rt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco().toString())
        .codRtEnteBenefIdUnivBenefCodiceIdUnivoco(rt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco())
        .deRtEnteBenefDenominazioneBeneficiario(rt.getEnteBeneficiario().getDenominazioneBeneficiario())
        .codRtEnteBenefCodiceUnitOperBeneficiario(rt.getEnteBeneficiario().getCodiceUnitOperBeneficiario())
        .deRtEnteBenefDenomUnitOperBeneficiario(rt.getEnteBeneficiario().getDenomUnitOperBeneficiario())
        .deRtEnteBenefIndirizzoBeneficiario(rt.getEnteBeneficiario().getIndirizzoBeneficiario())
        .deRtEnteBenefCivicoBeneficiario(rt.getEnteBeneficiario().getCivicoBeneficiario())
        .codRtEnteBenefCapBeneficiario(rt.getEnteBeneficiario().getCapBeneficiario())
        .deRtEnteBenefLocalitaBeneficiario(rt.getEnteBeneficiario().getLocalitaBeneficiario())
        .deRtEnteBenefProvinciaBeneficiario(rt.getEnteBeneficiario().getProvinciaBeneficiario())
        .codRtEnteBenefNazioneBeneficiario(rt.getEnteBeneficiario().getNazioneBeneficiario())
        .codRtSoggPagIdUnivPagTipoIdUnivoco(rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString())
        .codRtSoggPagIdUnivPagCodiceIdUnivoco(rt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco())
        .deRtSoggPagAnagraficaPagatore(rt.getSoggettoPagatore().getAnagraficaPagatore())
        .deRtSoggPagIndirizzoPagatore(rt.getSoggettoPagatore().getIndirizzoPagatore())
        .deRtSoggPagCivicoPagatore(rt.getSoggettoPagatore().getCivicoPagatore())
        .codRtSoggPagCapPagatore(rt.getSoggettoPagatore().getCapPagatore())
        .deRtSoggPagLocalitaPagatore(rt.getSoggettoPagatore().getLocalitaPagatore())
        .deRtSoggPagProvinciaPagatore(rt.getSoggettoPagatore().getProvinciaPagatore())
        .codRtSoggPagNazionePagatore(rt.getSoggettoPagatore().getNazionePagatore())
        .deRtSoggPagEmailPagatore(rt.getSoggettoPagatore().getEMailPagatore())
        .codRtDatiPagCodiceEsitoPagamento(rt.getDatiPagamento().getCodiceEsitoPagamento())
        .numRtDatiPagImportoTotalePagato(rt.getDatiPagamento().getImportoTotalePagato())
        .codRtDatiPagIdUnivocoVersamento(rt.getDatiPagamento().getIdentificativoUnivocoVersamento())
        .codRtDatiPagCodiceContestoPagamento(rt.getDatiPagamento().getCodiceContestoPagamento())
        .blbRtPayload(rtPayload)
        .build();

    var sv = rt.getSoggettoVersante();
    if (sv != null) rptRt = rptRt.toBuilder()
        .codRtSoggVersIdUnivVersTipoIdUnivoco(sv.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString())
        .codRtSoggVersIdUnivVersCodiceIdUnivoco(sv.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco())
        .deRtSoggVersAnagraficaVersante(sv.getAnagraficaVersante())
        .deRtSoggVersIndirizzoVersante(sv.getIndirizzoVersante())
        .deRtSoggVersCivicoVersante(sv.getCivicoVersante())
        .codRtSoggVersCapVersante(sv.getCapVersante())
        .deRtSoggVersLocalitaVersante(sv.getLocalitaVersante())
        .deRtSoggVersProvinciaVersante(sv.getProvinciaVersante())
        .codRtSoggVersNazioneVersante(sv.getNazioneVersante())
        .deRtSoggVersEmailVersante(sv.getEMailVersante())
        .build();
    Date now = new Date();
    rptRt.setDtCreazioneRt(now);
    rptRt.setDtUltimaModificaRt(now);
    int updated = rptRtDao.update(rptRt);
    if(updated!=1)
      throw new MyPayException(String.format("invalid number of rows updated: %d for mygovRptRtId: %d", updated, rptRt.getMygovRptRtId()));
    return rptRt;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RptRt insertRpt(TipoElementoListaRPT elementoRPT, NodoInviaCarrelloRPT nodoInviaCarrelloRPT, IntestazioneCarrelloPPT intestazioneCarrelloPPT, RPT rpt, RpE rpE, CarrelloRpt carrelloRpt) {
    Date now = new Date();
    RptRt rptRt = RptRt.builder()
        .dtCreazioneRpt(now)
        .dtUltimaModificaRpt(now)
        .mygovCarrelloRptId(carrelloRpt)
        .deRptInviarptPassword(nodoInviaCarrelloRPT.getPassword())
        .codRptInviarptIdPsp(nodoInviaCarrelloRPT.getIdentificativoPSP())
        .codRptInviarptIdIntermediarioPsp(nodoInviaCarrelloRPT.getIdentificativoIntermediarioPSP())
        .codRptInviarptIdCanale(nodoInviaCarrelloRPT.getIdentificativoCanale())
        .deRptInviarptTipoFirma(elementoRPT.getTipoFirma())
        .codRptInviarptIdIntermediarioPa(intestazioneCarrelloPPT.getIdentificativoIntermediarioPA())
        .codRptInviarptIdStazioneIntermediarioPa(intestazioneCarrelloPPT.getIdentificativoStazioneIntermediarioPA())
        .codRptInviarptIdDominio(elementoRPT.getIdentificativoDominio())
        .codRptInviarptIdUnivocoVersamento(elementoRPT.getIdentificativoUnivocoVersamento())
        .codRptInviarptCodiceContestoPagamento(elementoRPT.getCodiceContestoPagamento())
        .deRptVersioneOggetto(rpt.getVersioneOggetto())
        .codRptDomIdDominio(rpt.getDominio().getIdentificativoDominio())
        .codRptDomIdStazioneRichiedente(rpt.getDominio().getIdentificativoStazioneRichiedente())
        .codRptIdMessaggioRichiesta(rpt.getIdentificativoMessaggioRichiesta())
        .dtRptDataOraMessaggioRichiesta(rpt.getDataOraMessaggioRichiesta().toGregorianCalendar().getTime())
        .codRptAutenticazioneSoggetto(rpt.getAutenticazioneSoggetto().value())
        .codRptSoggPagIdUnivPagTipoIdUnivoco(rpt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString())
        .codRptSoggPagIdUnivPagCodiceIdUnivoco(rpt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco())
        .deRptSoggPagAnagraficaPagatore(rpt.getSoggettoPagatore().getAnagraficaPagatore())
        .deRptSoggPagIndirizzoPagatore(rpt.getSoggettoPagatore().getIndirizzoPagatore())
        .deRptSoggPagCivicoPagatore(rpt.getSoggettoPagatore().getCivicoPagatore())
        .codRptSoggPagCapPagatore(rpt.getSoggettoPagatore().getCapPagatore())
        .deRptSoggPagLocalitaPagatore(rpt.getSoggettoPagatore().getLocalitaPagatore())
        .deRptSoggPagProvinciaPagatore(rpt.getSoggettoPagatore().getProvinciaPagatore())
        .codRptSoggPagNazionePagatore(rpt.getSoggettoPagatore().getNazionePagatore())
        .deRptSoggPagEmailPagatore(rpt.getSoggettoPagatore().getEMailPagatore())
        .codRptEnteBenefIdUnivBenefTipoIdUnivoco(rpt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco().toString())
        .codRptEnteBenefIdUnivBenefCodiceIdUnivoco(rpt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco())
        .deRptEnteBenefDenominazioneBeneficiario(rpt.getEnteBeneficiario().getDenominazioneBeneficiario())
        .codRptEnteBenefCodiceUnitOperBeneficiario(rpt.getEnteBeneficiario().getCodiceUnitOperBeneficiario())
        .deRptEnteBenefDenomUnitOperBeneficiario(rpt.getEnteBeneficiario().getDenomUnitOperBeneficiario())
        .deRptEnteBenefIndirizzoBeneficiario(rpt.getEnteBeneficiario().getIndirizzoBeneficiario())
        .deRptEnteBenefCivicoBeneficiario(rpt.getEnteBeneficiario().getCivicoBeneficiario())
        .codRptEnteBenefCapBeneficiario(rpt.getEnteBeneficiario().getCapBeneficiario())
        .deRptEnteBenefLocalitaBeneficiario(rpt.getEnteBeneficiario().getLocalitaBeneficiario())
        .deRptEnteBenefProvinciaBeneficiario(rpt.getEnteBeneficiario().getProvinciaBeneficiario())
        .codRptEnteBenefNazioneBeneficiario(rpt.getEnteBeneficiario().getNazioneBeneficiario())
        .dtRptDatiVersDataEsecuzionePagamento(rpt.getDatiVersamento().getDataEsecuzionePagamento().toGregorianCalendar().getTime())
        .numRptDatiVersImportoTotaleDaVersare(rpt.getDatiVersamento().getImportoTotaleDaVersare())
        .codRptDatiVersTipoVersamento(rpt.getDatiVersamento().getTipoVersamento().toString())
        .codRptDatiVersIdUnivocoVersamento(rpt.getDatiVersamento().getIdentificativoUnivocoVersamento())
        .codRptDatiVersCodiceContestoPagamento(rpt.getDatiVersamento().getCodiceContestoPagamento())
        .deRptDatiVersIbanAddebito(rpt.getDatiVersamento().getIbanAddebito())
        .deRptDatiVersBicAddebito(rpt.getDatiVersamento().getBicAddebito())
        .codRptDatiVersFirmaRicevuta(rpt.getDatiVersamento().getFirmaRicevuta())
        .modelloPagamento(modelloPagamento)
        .mygovRpEId(rpE.getMygovRpEId())
        .build();
    var sv = rpt.getSoggettoVersante();
    if (sv != null) rptRt = rptRt.toBuilder()
        .codRptSoggVersIdUnivVersTipoIdUnivoco(sv.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString())
        .codRptSoggVersIdUnivVersCodiceIdUnivoco(sv.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco())
        .deRptSoggVersAnagraficaVersante(sv.getAnagraficaVersante())
        .deRptSoggVersIndirizzoVersante(sv.getIndirizzoVersante())
        .deRptSoggVersCivicoVersante(sv.getCivicoVersante())
        .codRptSoggVersCapVersante(sv.getCapVersante())
        .deRptSoggVersLocalitaVersante(sv.getLocalitaVersante())
        .deRptSoggVersProvinciaVersante(sv.getProvinciaVersante())
        .codRptSoggVersNazioneVersante(sv.getNazioneVersante())
        .deRptSoggVersEmailVersante(sv.getEMailVersante())
        .build();
    long newId = rptRtDao.insert(rptRt);
    return rptRt.toBuilder().mygovRptRtId(newId).build();
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateRispostaRptByCart(NodoInviaCarrelloRPTRisposta responseNodo, CarrelloRpt carrelloRpt) {
    List<RptRt> rptRtList = self.getByCart(carrelloRpt.getMygovCarrelloRptId());
    for (RptRt rptRt: rptRtList) {
      rptRt.setDtUltimaModificaRpt(new Date());
      rptRt.setDeRptInviarptEsito(responseNodo.getEsitoComplessivoOperazione());
      rptRt.setCodRptInviarptUrl(responseNodo.getUrl());
      ListaErroriRPT listaErroriRPT = responseNodo.getListaErroriRPT();
      if (listaErroriRPT != null && listaErroriRPT.getFaults() != null) {
        var f = listaErroriRPT.getFaults().get(0);
        rptRt = rptRt.toBuilder()
            .codRptInviarptFaultCode(f.getFaultCode())
            .codRptInviarptFaultString(f.getFaultString())
            .codRptInviarptId(f.getId())
            .deRptInviarptDescription(Utilities.getTruncatedAt(1024).apply(f.getDescription()))
            .deRptInviarptOriginalFaultDescription(Utilities.getTruncatedAt(1024).apply(f.getOriginalDescription()))
            .codRptInviarptOriginalFaultCode(f.getOriginalFaultCode())
            .deRptInviarptOriginalFaultString(f.getOriginalFaultString())
            .numRptInviarptSerial(f.getSerial())
            .build();
      }
      int updated = rptRtDao.update(rptRt);
      if(updated!=1)
        throw new MyPayException(String.format("invalid number of rows updated: %d for mygovRptRtId: %d", updated, rptRt.getMygovRptRtId()));
    }
  }

  public RptRt getByIdSession(String idSession) {
    List<RptRt> list = rptRtDao.getByIdSession(idSession);
    if (list != null && list.size() > 1)
      throw new FespException("'mygovRptRt' is not unique: " + list.size() + " records found.");
    return CollectionUtils.isEmpty(list) ? null : list.get(0);
  }

  @Cacheable(value = CacheService.CACHE_NAME_CHIEDI_STATO_RPT, key = "#mygovRptRtId", unless="#result==null")
  public String getProcessingInfoChiediStatoRpt(Long mygovRptRtId){
    return null;
  }

  @CachePut(value = CacheService.CACHE_NAME_CHIEDI_STATO_RPT, key = "#mygovRptRtId")
  public String updateProcessingInfoChiediStatoRpt(Long mygovRptRtId, int numChecks){
    return numChecks+"|"+System.currentTimeMillis();
  }

  @CacheEvict(value = CacheService.CACHE_NAME_CHIEDI_STATO_RPT, key = "#mygovRptRtId")
  public void deleteProcessingInfoChiediStatoRpt(Long mygovRptRtId){}
}
