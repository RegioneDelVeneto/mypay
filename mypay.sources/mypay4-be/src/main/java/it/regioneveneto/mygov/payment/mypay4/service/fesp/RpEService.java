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

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RpEDao;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RpEDettaglioDao;
import it.regioneveneto.mygov.payment.mypay4.dto.common.Psp;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.RpEDettaglioDto;
import it.regioneveneto.mygov.payment.mypay4.exception.MandatoryFieldsException;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.CarrelloRp;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RpE;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RpEDettaglio;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.ElementoRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRPRisposta;
import it.veneto.regione.pagamenti.pa.EsitoPaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.FaultBean;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsitoRisposta;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
import it.veneto.regione.schemas._2012.pagamenti.Esito;
import it.veneto.regione.schemas._2012.pagamenti.RP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class RpEService {

  @Resource
  private RpEService self;
  @Autowired
  RpEDao rpEDao;
  @Autowired
  RpEDettaglioDao rpEDettaglioDao;

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RpE insertRp(ElementoRP elementoRP, Psp psp, RP rp, CarrelloRp carrelloRp){
    Date now = new Date();
    RpE rpE = RpE.builder()
        .dtCreazioneRp(now)
        .dtUltimaModificaRp(now)
        .mygovCarrelloRpId(carrelloRp)
        .codRpSilinviarpIdPsp(psp.getIdentificativoPSP())
        .codRpSilinviarpIdIntermediarioPsp(psp.getIdentificativoIntermediarioPSP())
        .codRpSilinviarpIdCanale(psp.getIdentificativoCanale())
        .codRpSilinviarpIdDominio(elementoRP.getIdentificativoDominio())
        .codRpSilinviarpIdUnivocoVersamento(elementoRP.getIdentificativoUnivocoVersamento())
        .codRpSilinviarpCodiceContestoPagamento(elementoRP.getCodiceContestoPagamento())
        .deRpVersioneOggetto(rp.getVersioneOggetto())
        .codRpDomIdDominio(rp.getDominio().getIdentificativoDominio())
        //.codRpDomIdStazioneRichiedente(rp.getDominio().getIdentificativoStazioneRichiedente())  TODO: IT'S NULL???
        .codRpIdMessaggioRichiesta(rp.getIdentificativoMessaggioRichiesta())
        .dtRpDataOraMessaggioRichiesta(rp.getDataOraMessaggioRichiesta().toGregorianCalendar().getTime())
        .codRpAutenticazioneSoggetto(rp.getAutenticazioneSoggetto().toString())
        .modelloPagamento(psp.getModelloPagamento())
        .build();

    var versante = rp.getSoggettoVersante();
    if (versante != null) rpE = rpE.toBuilder()
        .codRpSoggVersIdUnivVersTipoIdUnivoco(versante.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString())
        .codRpSoggVersIdUnivVersCodiceIdUnivoco(versante.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco())
        .deRpSoggVersAnagraficaVersante(versante.getAnagraficaVersante())
        .deRpSoggVersIndirizzoVersante(versante.getIndirizzoVersante())
        .deRpSoggVersCivicoVersante(versante.getCivicoVersante())
        .codRpSoggVersCapVersante(versante.getCapVersante())
        .deRpSoggVersLocalitaVersante(versante.getLocalitaVersante())
        .deRpSoggVersProvinciaVersante(versante.getProvinciaVersante())
        .codRpSoggVersNazioneVersante(versante.getNazioneVersante())
        .deRpSoggVersEmailVersante(versante.getEMailVersante())
        .build();
    var pagatore = rp.getSoggettoPagatore();
    if (pagatore != null) rpE = rpE.toBuilder()
        .codRpSoggPagIdUnivPagTipoIdUnivoco(pagatore.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString())
        .codRpSoggPagIdUnivPagCodiceIdUnivoco(pagatore.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco())
        .deRpSoggPagAnagraficaPagatore(pagatore.getAnagraficaPagatore())
        .deRpSoggPagIndirizzoPagatore(pagatore.getIndirizzoPagatore())
        .deRpSoggPagCivicoPagatore(pagatore.getCivicoPagatore())
        .codRpSoggPagCapPagatore(pagatore.getCapPagatore())
        .deRpSoggPagLocalitaPagatore(pagatore.getLocalitaPagatore())
        .deRpSoggPagProvinciaPagatore(pagatore.getProvinciaPagatore())
        .codRpSoggPagNazionePagatore(pagatore.getNazionePagatore())
        .deRpSoggPagEmailPagatore(pagatore.getEMailPagatore())
        .build();
    var dati = rp.getDatiVersamento();
    if (dati != null) rpE = rpE.toBuilder()
        .dtRpDatiVersDataEsecuzionePagamento(dati.getDataEsecuzionePagamento().toGregorianCalendar().getTime())
        .numRpDatiVersImportoTotaleDaVersare(dati.getImportoTotaleDaVersare())
        .codRpDatiVersTipoVersamento(dati.getTipoVersamento().toString())
        .codRpDatiVersIdUnivocoVersamento(dati.getIdentificativoUnivocoVersamento())
        .codRpDatiVersCodiceContestoPagamento(dati.getCodiceContestoPagamento())
        .deRpDatiVersIbanAddebito(dati.getIbanAddebito())
        .deRpDatiVersBicAddebito(dati.getBicAddebito())
        .build();
    long newId = rpEDao.insert(rpE);
    return rpE.toBuilder().mygovRpEId(newId).build();
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RpEDettaglio insertRpEDettaglio(RpEDettaglio dettaglio) {
    long mygovRpEDettaglioId = rpEDettaglioDao.insert(dettaglio);
    dettaglio.setMygovRpEDettaglioId(mygovRpEDettaglioId);
    return dettaglio;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RpEDettaglio insertRpEDettaglio(RpE rpE, RpEDettaglioDto dettDto) {
    RpEDettaglio dettaglio = RpEDettaglio.builder()
        .mygovRpEId(rpE)
        .numRpDatiVersDatiSingVersImportoSingoloVersamento(dettDto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento())
        .numRpDatiVersDatiSingVersCommissioneCaricoPa(dettDto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa())
        .codRpDatiVersDatiSingVersIbanAccredito(dettDto.getCodRpDatiVersDatiSingVersIbanAccredito())
        .codRpDatiVersDatiSingVersBicAccredito(dettDto.getCodRpDatiVersDatiSingVersBicAccredito())
        .codRpDatiVersDatiSingVersIbanAppoggio(dettDto.getCodRpDatiVersDatiSingVersIbanAppoggio())
        .codRpDatiVersDatiSingVersBicAppoggio(dettDto.getCodRpDatiVersDatiSingVersBicAppoggio())
        .codRpDatiVersDatiSingVersCredenzialiPagatore(dettDto.getCodRpDatiVersDatiSingVersCredenzialiPagatore())
        .deRpDatiVersDatiSingVersCausaleVersamento(dettDto.getDeRpDatiVersDatiSingVersCausaleVersamento())
        .deRpDatiVersDatiSingVersDatiSpecificiRiscossione(dettDto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione())
        .numEDatiPagDatiSingPagSingoloImportoPagato(dettDto.getNumEDatiPagDatiSingPagSingoloImportoPagato())
        .deEDatiPagDatiSingPagEsitoSingoloPagamento(dettDto.getDeEDatiPagDatiSingPagEsitoSingoloPagamento())
        .dtEDatiPagDatiSingPagDataEsitoSingoloPagamento(dettDto.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento())
        .codEDatiPagDatiSingPagIdUnivocoRiscoss(dettDto.getCodEDatiPagDatiSingPagIdUnivocoRiscoss())
        .deEDatiPagDatiSingPagCausaleVersamento(dettDto.getDeEDatiPagDatiSingPagCausaleVersamento())
        .deEDatiPagDatiSingPagDatiSpecificiRiscossione(dettDto.getDeEDatiPagDatiSingPagDatiSpecificiRiscossione())
        .numEDatiPagDatiSingPagCommissioniApplicatePsp(dettDto.getNumEDatiPagDatiSingPagCommissioniApplicatePsp())
        .codEDatiPagDatiSingPagAllegatoRicevutaTipo(dettDto.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo())
        .blbEDatiPagDatiSingPagAllegatoRicevutaTest(dettDto.getBlbEDatiPagDatiSingPagAllegatoRicevutaTest())
        .codRpDatiVersDatiSingVersDatiMbdTipoBollo(dettDto.getCodRpDatiVersDatiSingVersDatiMbdTipoBollo())
        .codRpDatiVersDatiSingVersDatiMbdHashDocumento(dettDto.getCodRpDatiVersDatiSingVersDatiMbdHashDocumento())
        .codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza(dettDto.getCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza()).build();

    return insertRpEDettaglio(dettaglio);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RpE insertRPWithRefresh(final String codRpSilinviarpIdPsp, final String codRpSilinviarpIdIntermediarioPsp, final String codRpSilinviarpIdCanale,
                                      final String codRpSilinviarpIdDominio, final String codRpSilinviarpIdUnivocoVersamento, final String codRpSilinviarpCodiceContestoPagamento,
                                      final String deRpVersioneOggetto, final String codRpDomIdDominio, final String codRpDomIdStazioneRichiedente,
                                      final String codRpIdMessaggioRichiesta, final Date dtRpDataOraMessaggioRichiesta, final String codRpAutenticazioneSoggetto,
                                      final String codRpSoggVersIdUnivVersTipoIdUnivoco, final String codRpSoggVersIdUnivVersCodiceIdUnivoco, final String deRpSoggVersAnagraficaVersante,
                                      final String deRpSoggVersIndirizzoVersante, final String deRpSoggVersCivicoVersante, final String codRpSoggVersCapVersante,
                                      final String deRpSoggVersLocalitaVersante, final String deRpSoggVersProvinciaVersante, final String codRpSoggVersNazioneVersante,
                                      final String deRpSoggVersEmailVersante, final String codRpSoggPagIdUnivPagTipoIdUnivoco, final String codRpSoggPagIdUnivPagCodiceIdUnivoco,
                                      final String deRpSoggPagAnagraficaPagatore, final String deRpSoggPagIndirizzoPagatore, final String deRpSoggPagCivicoPagatore,
                                      final String codRpSoggPagCapPagatore, final String deRpSoggPagLocalitaPagatore, final String deRpSoggPagProvinciaPagatore,
                                      final String codRpSoggPagNazionePagatore, final String deRpSoggPagEmailPagatore, final Date dtRpDatiVersDataEsecuzionePagamento,
                                      final BigDecimal numRpDatiVersImportoTotaleDaVersare, final String codRpDatiVersTipoVersamento, final String codRpDatiVersIdUnivocoVersamento,
                                      final String codRpDatiVersCodiceContestoPagamento, final String deRpDatiVersIbanAddebito, final String deRpDatiVersBicAddebito,
                                      final Integer modelloPagamento, final List<RpEDettaglioDto> rpEDettaglios) {

    log.debug("Invocato insertRP con: codRpSilinviarpIdPsp = [" + codRpSilinviarpIdPsp + "] ");

    RpE rpE = RpE.builder()
        .codRpSilinviarpIdPsp(codRpSilinviarpIdPsp)
        .codRpSilinviarpIdIntermediarioPsp(codRpSilinviarpIdIntermediarioPsp)
        .codRpSilinviarpIdCanale(codRpSilinviarpIdCanale)
        .codRpSilinviarpIdDominio(codRpSilinviarpIdDominio)
        .codRpSilinviarpIdUnivocoVersamento(codRpSilinviarpIdUnivocoVersamento)
        .codRpSilinviarpCodiceContestoPagamento(codRpSilinviarpCodiceContestoPagamento)
        .deRpVersioneOggetto(deRpVersioneOggetto)
        .codRpDomIdDominio(codRpDomIdDominio)
        .codRpDomIdStazioneRichiedente(codRpDomIdStazioneRichiedente)
        .codRpIdMessaggioRichiesta(codRpIdMessaggioRichiesta)
        .dtRpDataOraMessaggioRichiesta(dtRpDataOraMessaggioRichiesta)
        .codRpAutenticazioneSoggetto(codRpAutenticazioneSoggetto)
        .codRpSoggVersIdUnivVersTipoIdUnivoco(codRpSoggVersIdUnivVersTipoIdUnivoco)
        .codRpSoggVersIdUnivVersCodiceIdUnivoco(codRpSoggVersIdUnivVersCodiceIdUnivoco)
        .deRpSoggVersAnagraficaVersante(deRpSoggVersAnagraficaVersante)
        .deRpSoggVersIndirizzoVersante(deRpSoggVersIndirizzoVersante)
        .deRpSoggVersCivicoVersante(deRpSoggVersCivicoVersante)
        .codRpSoggVersCapVersante(codRpSoggVersCapVersante)
        .deRpSoggVersLocalitaVersante(deRpSoggVersLocalitaVersante)
        .deRpSoggVersProvinciaVersante(deRpSoggVersProvinciaVersante)
        .codRpSoggVersNazioneVersante(codRpSoggVersNazioneVersante)
        .deRpSoggVersEmailVersante(deRpSoggVersEmailVersante)
        .codRpSoggPagIdUnivPagTipoIdUnivoco(codRpSoggPagIdUnivPagTipoIdUnivoco)
        .codRpSoggPagIdUnivPagCodiceIdUnivoco(codRpSoggPagIdUnivPagCodiceIdUnivoco)
        .deRpSoggPagAnagraficaPagatore(deRpSoggPagAnagraficaPagatore)
        .deRpSoggPagIndirizzoPagatore(deRpSoggPagIndirizzoPagatore)
        .deRpSoggPagCivicoPagatore(deRpSoggPagCivicoPagatore)
        .codRpSoggPagCapPagatore(codRpSoggPagCapPagatore)
        .deRpSoggPagLocalitaPagatore(deRpSoggPagLocalitaPagatore)
        .deRpSoggPagProvinciaPagatore(deRpSoggPagProvinciaPagatore)
        .codRpSoggPagNazionePagatore(codRpSoggPagNazionePagatore)
        .deRpSoggPagEmailPagatore(deRpSoggPagEmailPagatore)
        .dtRpDatiVersDataEsecuzionePagamento(dtRpDatiVersDataEsecuzionePagamento)
        .numRpDatiVersImportoTotaleDaVersare(numRpDatiVersImportoTotaleDaVersare)
        .codRpDatiVersTipoVersamento(codRpDatiVersTipoVersamento)
        .codRpDatiVersIdUnivocoVersamento(codRpDatiVersIdUnivocoVersamento)
        .codRpDatiVersCodiceContestoPagamento(codRpDatiVersCodiceContestoPagamento)
        .deRpDatiVersIbanAddebito(deRpDatiVersIbanAddebito)
        .deRpDatiVersBicAddebito(deRpDatiVersBicAddebito)
        .modelloPagamento(modelloPagamento).build();

    Long mygovRpEId = rpEDao.insert(rpE);
    rpE.setMygovRpEId(mygovRpEId);

    log.debug("insertRP leggo e carico dettagli");

    for (RpEDettaglioDto dett : rpEDettaglios) {
      dett.setCodRpDatiVersDatiSingVersDatiMbdTipoBollo(
          StringUtils.isNotBlank(dett.getCodRpDatiVersDatiSingVersDatiMbdTipoBollo()) ? dett.getCodRpDatiVersDatiSingVersDatiMbdTipoBollo() : null);
      dett.setCodRpDatiVersDatiSingVersDatiMbdHashDocumento(
          StringUtils.isNotBlank(dett.getCodRpDatiVersDatiSingVersDatiMbdHashDocumento()) ? dett.getCodRpDatiVersDatiSingVersDatiMbdHashDocumento() : null);
      dett.setCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza(
          StringUtils.isNotBlank(dett.getCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza()) ? dett.getCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza() : null);
      insertRpEDettaglio(rpE, dett);
    }

    log.debug("fine insertRP");

    return rpE;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RpE updateRispostaRpById(final Long mygovRpEId, final String deRpSilinviarpEsito, final Integer codRpSilinviarpRedirect,
                                         final String codRpSilinviarpUrl, final String codRpSilinviarpFaultCode, final String deRpSilinviarpFaultString, final String codRpSilinviarpId,
                                         final String deRpSilinviarpDescription, final Integer codRpSilinviarpSerial, final String idSession, final String codRpSilinviarpOriginalFaultCode,
                                         final String deRpSilinviarpOriginalFaultString, final String deRpSilinviarpOriginalFaultDescription) throws DataAccessException {

    log.debug("Invocato metodo updateRispostaRpById PARAMETRI ::: " + "mygovRpEId = [" + mygovRpEId + "] ::: deRpSilinviarpEsito = [" + deRpSilinviarpEsito
        + "] ::: codRpSilinviarpRedirect = [" + codRpSilinviarpRedirect + "] ::: codRpSilinviarpUrl = [" + codRpSilinviarpUrl
        + "] ::: codRpSilinviarpFaultCode = [" + codRpSilinviarpFaultCode + "] ::: deRpSilinviarpFaultString = [" + deRpSilinviarpFaultString
        + "] ::: codRpSilinviarpId = [" + codRpSilinviarpId + "] ::: deRpSilinviarpDescription = [" + deRpSilinviarpDescription
        + "] ::: codRpSilinviarpSerial = [" + codRpSilinviarpSerial + "] ::: idSession = [" + idSession + "] ::: "
        + "codRpSilinviarpOriginalFaultCode = [" + codRpSilinviarpOriginalFaultCode + "] ::: "
        + "deRpSilinviarpOriginalFaultString = [" + deRpSilinviarpOriginalFaultString + "] ::: "
        + "deRpSilinviarpOriginalFaultDescription = [" + deRpSilinviarpOriginalFaultDescription + "] ::: ");

    RpE mygovRpE = getById(mygovRpEId).orElse(null);

    mygovRpE.setDtUltimaModificaRp(new Date());
    mygovRpE.setDeRpSilinviarpEsito(deRpSilinviarpEsito);
    mygovRpE.setCodRpSilinviarpRedirect(codRpSilinviarpRedirect);
    mygovRpE.setCodRpSilinviarpUrl(codRpSilinviarpUrl);
    mygovRpE.setCodRpSilinviarpFaultCode(codRpSilinviarpFaultCode);
    mygovRpE.setDeRpSilinviarpFaultString(deRpSilinviarpFaultString);

    mygovRpE.setCodRpSilinviarpId(codRpSilinviarpId);

    // Tronco deRpSilinviarpDescription se non è null e la lunghezza è maggiore di 1024
    if (StringUtils.isNotBlank(deRpSilinviarpDescription)) {
      if (deRpSilinviarpDescription.length() > 1024) {
        mygovRpE.setDeRpSilinviarpDescription(deRpSilinviarpDescription.substring(0, 1024));
      }
      else {
        mygovRpE.setDeRpSilinviarpDescription(deRpSilinviarpDescription);
      }
    }
    else {
      mygovRpE.setDeRpSilinviarpDescription(deRpSilinviarpDescription);
    }

    mygovRpE.setCodRpSilinviarpOriginalFaultCode(codRpSilinviarpOriginalFaultCode);
    mygovRpE.setDeRpSilinviarpOriginalFaultString(deRpSilinviarpOriginalFaultString);
    if (StringUtils.isNotBlank(deRpSilinviarpOriginalFaultDescription)) {
      if (deRpSilinviarpOriginalFaultDescription.length() > 1024)
        mygovRpE.setDeRpSilinviarpOriginalFaultDescription(deRpSilinviarpOriginalFaultDescription.substring(0, 1024));
      else
        mygovRpE.setDeRpSilinviarpOriginalFaultDescription(deRpSilinviarpOriginalFaultDescription);
    }

    mygovRpE.setCodRpSilinviarpSerial(codRpSilinviarpSerial);
    mygovRpE.setIdSession(idSession);

    log.debug("deRpSilinviarpEsito :" + deRpSilinviarpEsito);
    log.debug("codRpSilinviarpRedirect :" + codRpSilinviarpRedirect);
    log.debug("codRpSilinviarpUrl :" + codRpSilinviarpUrl);
    log.debug("codRpSilinviarpFaultCode :" + codRpSilinviarpFaultCode);
    log.debug("deRpSilinviarpFaultString :" + deRpSilinviarpFaultString);
    log.debug("codRpSilinviarpId :" + codRpSilinviarpId);
    log.debug("deRpSilinviarpDescription :" + deRpSilinviarpDescription);
    log.debug("codRpSilinviarpSerial :" + codRpSilinviarpSerial);
    log.debug("idSession :" + idSession);
    log.debug("codRpSilinviarpOriginalFaultCode :" + codRpSilinviarpOriginalFaultCode);
    log.debug("deRpSilinviarpOriginalFaultString :" + deRpSilinviarpOriginalFaultString);
    log.debug("deRpSilinviarpOriginalFaultDescription :" + deRpSilinviarpOriginalFaultDescription);

    rpEDao.update(mygovRpE);

    return mygovRpE;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateByKey(CarrelloRp carrelloRp, NodoSILInviaCarrelloRPRisposta response) {
    List<RpE> rpEList = self.getByCart(carrelloRp);
    for (RpE rpE: rpEList) {
      Date now = new Date();
      rpE.toBuilder()
          .dtUltimaModificaRp(now)
          .deRpSilinviarpEsito(response.getEsito())
          .codRpSilinviarpUrl(response.getUrl())
          .build();
      var fb = response.getFault();
      if (fb != null) rpE = rpE.toBuilder()
          .codRpSilinviarpFaultCode(fb.getFaultCode())
          .deRpSilinviarpFaultString(fb.getFaultString())
          .codRpSilinviarpId(fb.getId())
          .deRpSilinviarpDescription(Utilities.getTruncatedAt(1024).apply(fb.getDescription()))
          .codRpSilinviarpOriginalFaultCode(fb.getOriginalFaultCode())
          .deRpSilinviarpOriginalFaultString(fb.getOriginalFaultString())
          .deRpSilinviarpOriginalFaultDescription(Utilities.getTruncatedAt(1024).apply(fb.getOriginalDescription()))
          .codRpSilinviarpSerial(fb.getSerial())
          .build();

      int updated = rpEDao.update(rpE);
      if(updated!=1)
        throw new MyPayException(String.format("invalid number of rows updated: %d for mygovCarrelloRpId: %d", updated, carrelloRp.getMygovCarrelloRpId()));
    }
  }

  public Optional<RpE> getByIdSession(String idSession){
    return rpEDao.getByIdSession(idSession);
  }

  public Optional<RpE> getById(Long id){ return rpEDao.getById(id); }

  public List<RpE> getByCart(CarrelloRp CarrelloRp) { return rpEDao.getByCart(CarrelloRp.getMygovCarrelloRpId()); }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateCarrelloRef(RpE rpE, CarrelloRp carrelloRp) {
    rpE.toBuilder()
        .mygovCarrelloRpId(carrelloRp)
        .build();
    int updated = rpEDao.update(rpE);
    if(updated!=1)
      throw new MyPayException(String.format("invalid number of rows updated: %d for mygovCarrelloRpId: %d", updated, rpE.getMygovCarrelloRpId()));
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public RpE updateEById(Long mygovRpEId, Esito esito, IntestazionePPT header) {
    RpE rpE = self.getById(mygovRpEId).get()
        .toBuilder()
        .codESilinviaesitoIdDominio(header.getIdentificativoDominio())
        .codESilinviaesitoIdUnivocoVersamento(header.getIdentificativoUnivocoVersamento())
        .codESilinviaesitoCodiceContestoPagamento(header.getCodiceContestoPagamento())
        .deEVersioneOggetto(mandatoryChecker(esito.getVersioneOggetto(),"deEVersioneOggetto"))
        .codEDomIdDominio(mandatoryChecker(esito.getDominio().getIdentificativoDominio(),"codEDomIdDominio"))
        .codEDomIdStazioneRichiedente(esito.getDominio().getIdentificativoStazioneRichiedente())
        .codEIdMessaggioRicevuta(mandatoryChecker(esito.getIdentificativoMessaggioRicevuta(),"mandatoryChecker"))
        .dtEDataOraMessaggioRicevuta(mandatoryChecker(esito.getDataOraMessaggioRicevuta().toGregorianCalendar().getTime(),"dtEDataOraMessaggioRicevuta"))
        .codERiferimentoMessaggioRichiesta(mandatoryChecker(esito.getRiferimentoMessaggioRichiesta(),"codERiferimentoMessaggioRichiesta"))
        .dtERiferimentoDataRichiesta(mandatoryChecker(esito.getRiferimentoDataRichiesta().toGregorianCalendar().getTime(),"dtERiferimentoDataRichiesta"))
        .codEIstitAttesIdUnivAttesTipoIdUnivoco(mandatoryChecker(esito.getIstitutoAttestante().getIdentificativoUnivocoAttestante().getTipoIdentificativoUnivoco().toString(),"codEIstitAttesIdUnivAttesTipoIdUnivoco"))
        .codEIstitAttesIdUnivAttesCodiceIdUnivoco(mandatoryChecker(esito.getIstitutoAttestante().getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco(),"codEIstitAttesIdUnivAttesCodiceIdUnivoco"))
        .deEIstitAttesDenominazioneAttestante(mandatoryChecker(esito.getIstitutoAttestante().getDenominazioneAttestante(),"deEIstitAttesDenominazioneAttestante"))
        .codEIstitAttesCodiceUnitOperAttestante(esito.getIstitutoAttestante().getCodiceUnitOperAttestante())
        .deEIstitAttesDenomUnitOperAttestante(esito.getIstitutoAttestante().getDenomUnitOperAttestante())
        .deEIstitAttesIndirizzoAttestante(esito.getIstitutoAttestante().getIndirizzoAttestante())
        .deEIstitAttesCivicoAttestante(esito.getIstitutoAttestante().getCivicoAttestante())
        .codEIstitAttesCapAttestante(esito.getIstitutoAttestante().getCapAttestante())
        .deEIstitAttesLocalitaAttestante(esito.getIstitutoAttestante().getLocalitaAttestante())
        .deEIstitAttesProvinciaAttestante(esito.getIstitutoAttestante().getProvinciaAttestante())
        .codEIstitAttesNazioneAttestante(esito.getIstitutoAttestante().getNazioneAttestante())
        .codEEnteBenefIdUnivBenefTipoIdUnivoco(mandatoryChecker(esito.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco().toString(),"codEEnteBenefIdUnivBenefTipoIdUnivoco"))
        .codEEnteBenefIdUnivBenefCodiceIdUnivoco(mandatoryChecker(esito.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco(),"codEEnteBenefIdUnivBenefCodiceIdUnivoco"))
        .deEEnteBenefDenominazioneBeneficiario(mandatoryChecker(esito.getEnteBeneficiario().getDenominazioneBeneficiario(),"deEEnteBenefDenominazioneBeneficiario"))
        .codEEnteBenefCodiceUnitOperBeneficiario(esito.getEnteBeneficiario().getCodiceUnitOperBeneficiario())
        .deEEnteBenefDenomUnitOperBeneficiario(esito.getEnteBeneficiario().getDenomUnitOperBeneficiario())
        .deEEnteBenefIndirizzoBeneficiario(esito.getEnteBeneficiario().getIndirizzoBeneficiario())
        .deEEnteBenefCivicoBeneficiario(esito.getEnteBeneficiario().getCivicoBeneficiario())
        .codEEnteBenefCapBeneficiario(esito.getEnteBeneficiario().getCapBeneficiario())
        .deEEnteBenefLocalitaBeneficiario(esito.getEnteBeneficiario().getLocalitaBeneficiario())
        .deEEnteBenefProvinciaBeneficiario(esito.getEnteBeneficiario().getProvinciaBeneficiario())
        .codEEnteBenefNazioneBeneficiario(esito.getEnteBeneficiario().getNazioneBeneficiario())
        .codESoggPagIdUnivPagTipoIdUnivoco(mandatoryChecker(esito.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString(),"codESoggPagIdUnivPagTipoIdUnivoco"))
        .codESoggPagIdUnivPagCodiceIdUnivoco(mandatoryChecker(esito.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco(),"codESoggPagIdUnivPagCodiceIdUnivoco"))
        .deESoggPagAnagraficaPagatore(mandatoryChecker(esito.getSoggettoPagatore().getAnagraficaPagatore(),"deESoggPagAnagraficaPagatore"))
        .deESoggPagIndirizzoPagatore(esito.getSoggettoPagatore().getIndirizzoPagatore())
        .deESoggPagCivicoPagatore(esito.getSoggettoPagatore().getCivicoPagatore())
        .codESoggPagCapPagatore(esito.getSoggettoPagatore().getCapPagatore())
        .deESoggPagLocalitaPagatore(esito.getSoggettoPagatore().getLocalitaPagatore())
        .deESoggPagProvinciaPagatore(esito.getSoggettoPagatore().getProvinciaPagatore())
        .codESoggPagNazionePagatore(esito.getSoggettoPagatore().getNazionePagatore())
        .deESoggPagEmailPagatore(esito.getSoggettoPagatore().getEMailPagatore())
        .codEDatiPagCodiceEsitoPagamento(mandatoryChecker(esito.getDatiPagamento().getCodiceEsitoPagamento(),"codEDatiPagCodiceEsitoPagamento"))
        .numEDatiPagImportoTotalePagato(mandatoryChecker(esito.getDatiPagamento().getImportoTotalePagato(),"numEDatiPagImportoTotalePagato"))
        .codEDatiPagIdUnivocoVersamento(mandatoryChecker(esito.getDatiPagamento().getIdentificativoUnivocoVersamento(),"codEDatiPagIdUnivocoVersamento"))
        .codEDatiPagCodiceContestoPagamento(mandatoryChecker(esito.getDatiPagamento().getCodiceContestoPagamento(),"codEDatiPagCodiceContestoPagamento"))
        .build();

    int updated = rpEDao.update(rpE);
    if(updated!=1)
      throw new MyPayException(String.format("invalid number of rows updated: %d for mygovRpEId: %d", updated, rpE.getMygovRpEId()));
    return rpE;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateRispostaEById(PaaSILInviaEsitoRisposta paaSILInviaEsitoRisposta, Long mygovRpEId) {
    EsitoPaaSILInviaEsito esitoRisposta = paaSILInviaEsitoRisposta.getPaaSILInviaEsitoRisposta();
    Optional<FaultBean> optionalFault = Optional.ofNullable(esitoRisposta.getFault());
    String logMsg = String.format("Invocato metodo updateRispostaEById PARAMETRI ::: " + "mygovRpEId = [%d] ::: codAckE = [%s] ::: deESilinviaesitoEsito = [%s] ", mygovRpEId, Constants.ACK_STRING, esitoRisposta.getEsito());
    String logFault = "";

    RpE rpE = self.getById(mygovRpEId).get()
        .toBuilder()
        .dtUltimaModificaE(new Date())
        .codAckE(Constants.ACK_STRING)
        .deESilinviaesitoEsito(esitoRisposta.getEsito())
        .build();
    if(optionalFault.isPresent()) {
      FaultBean f = optionalFault.get();
      rpE = rpE.toBuilder()
          .codESilinviaesitoFaultCode(f.getFaultCode())
          .deESilinviaesitoFaultString(f.getFaultString())
          .codESilinviaesitoId(f.getId())
          .deESilinviaesitoDescription(Utilities.getTruncatedAt(1024).apply(f.getDescription()))
          .codESilinviaesitoOriginalFaultCode(f.getOriginalFaultCode())
          .deESilinviaesitoOriginalFaultString(f.getOriginalFaultString())
          .deESilinviaesitoOriginalFaultDescription(Utilities.getTruncatedAt(1024).apply(f.getOriginalDescription()))
          .codESilinviaesitoSerial(f.getSerial())
          .build();
      logFault = " ::: codESilinviaesitoFaultCode = [" + f.getFaultCode()
          + "] ::: deESilinviaesitoFaultString = [" + f.getFaultString() + "] ::: codESilinviaesitoId = [" + f.getId()
          + "] ::: deESilinviaesitoDescription = [" + f.getDescription() + "] ::: codESilinviaesitoSerial = [" + f.getSerial()
          + "] ::: codESilinviaesitoOriginalFaultCode = [" + f.getOriginalFaultCode() + "] ::: deESilinviaesitoOriginalFaultString = [" + f.getOriginalFaultString()
          + "] ::: deESilinviaesitoOriginalFaultDescription = [" + f.getOriginalDescription() + "]";
    };

    log.debug(logMsg + logFault);
    int updated = rpEDao.update(rpE);
    if(updated!=1)
      throw new MyPayException(String.format("invalid number of rows updated: %d for mygovRpEId: %d", updated, rpE.getMygovRpEId()));
  }

  private static <R> R mandatoryChecker(R in, String fieldName) {
    if (ObjectUtils.isEmpty(in)) {
      String error = String.format("Errore nell'inserimento esito: '%s' obbligatorio", fieldName);
      log.error(error);
      throw new MandatoryFieldsException(error);
    }
    return in;
  }
}
