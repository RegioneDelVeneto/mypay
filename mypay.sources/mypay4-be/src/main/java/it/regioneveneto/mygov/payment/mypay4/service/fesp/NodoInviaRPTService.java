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

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.IntestazionePPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPTRisposta;
import it.gov.digitpa.schemas._2011.pagamenti.RPT;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RptRtDettaglioDao;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.NodoSILInviaRPRispostaException;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.RpEDettaglioDto;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.RptRtDettaglioDto;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciRPTClient;
import it.regioneveneto.mygov.payment.mypay4.ws.util.EnumUtils;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.schemas._2012.pagamenti.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.GDateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.*;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.*;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class NodoInviaRPTService {

  @Autowired
  private EnteService enteFespService;
  @Autowired
  private RptRtService rptRtService;
  @Autowired
  private RpEService rpEService;
  @Autowired
  private RptRtDettaglioDao rptRtDettaglioDao;
  @Autowired
  private PagamentiTelematiciRPTClient pagamentiTelematiciRPTClient;
  @Autowired
  private JAXBTransformService jaxbTransformService;
  @Autowired
  private RPTConservazioneService rptConservazioneService;

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;

  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String propIdStazioneIntermediarioPa;

  @Value("${pa.identificativoIntermediarioPA}")
  private String propIdIntermediarioPa;

  @Value("${pa.identificativoIntermediarioPAPassword}")
  private String propIdIntermediarioPaPassword;


  public NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(NodoSILChiediCopiaEsito nodoSilChiediCopiaEsito) {
    NodoSILChiediCopiaEsitoRisposta risposta = new NodoSILChiediCopiaEsitoRisposta();
    String identificativoDominio = nodoSilChiediCopiaEsito.getIdentificativoDominio();
    String identificativoUnivocoVersamento = nodoSilChiediCopiaEsito.getIdentificativoUnivocoVersamento();
    String codiceContestoPagamento = nodoSilChiediCopiaEsito.getCodiceContestoPagamento();
    //controllo le celle relative a RPT
    RptRt rptRt = rptRtService.getRPTByIdDominioIuvCdContestoPagamento(identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);

    if (rptRt == null) {
      String msg = String.format("Nessuna RPT per idDominio[%s] IUV[%s] CCP[%s]",
          identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);
      log.debug(msg);
      FaultBean fb = VerificationUtils.getFespFaultBean(PAA_RPT_NON_PRESENTE, PAA_RPT_NON_PRESENTE, msg, msg, 1);
      risposta.setFault(fb);
      return risposta;
    } else {
      //controllo le celle relative a RT
      rptRt = rptRtService.getRTByIdDominioIuvCdContestoPagamento(identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);
      if (rptRt == null) {
        String msg = String.format("Nessuna RT per idDominio[%s] IUV[%s] CCP[%s]",
            identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);
        log.debug(msg);
        FaultBean fb = VerificationUtils.getFespFaultBean(PAA_RT_NON_PRESENTE, PAA_RT_NON_PRESENTE, msg, msg, 1);
        risposta.setFault(fb);
        return risposta;
      }

      if (StringUtils.isNotBlank(rptRt.getCodRtDatiPagCodiceEsitoPagamento())) {
        risposta.setTipoFirma(rptRt.getDeRptInviarptTipoFirma());

        risposta.setRt(rptRt.getBlbRtPayload());

        Esito esito = buildEsito(rptRt);
        risposta.setEsito(jaxbTransformService.marshallingAsBytes(esito, Esito.class));
      } else {
        String msg = String.format("Codice esito pagamento non valorizzato RT con identificativo dominio [ %s ], IUV [ %s ] e codice contesto pagamento [ %s ]",
            identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento);
        log.error(msg);
        FaultBean fb = VerificationUtils.getFespFaultBean(PAA_SYSTEM_ERROR, PAA_SYSTEM_ERROR, msg, msg, 1);
        risposta.setFault(fb);
      }
      return risposta;
    }
  }

  private Esito buildEsito(RptRt rptRt) {

    /*
     * CtEsito
     */

    Esito ctEsito = new Esito();
    ctEsito.setVersioneOggetto(rptRt.getDeRtVersioneOggetto());

    /*
     * CtDominio
     */

    CtDominio dominio = new CtDominio();
    dominio.setIdentificativoDominio(rptRt.getCodRtDomIdDominio());
    dominio.setIdentificativoStazioneRichiedente(StringUtils.stripToNull(rptRt.getCodRtDomIdStazioneRichiedente()));
    ctEsito.setDominio(dominio);

    ctEsito.setIdentificativoMessaggioRicevuta(rptRt.getCodRtIdMessaggioRicevuta());
    ctEsito.setDataOraMessaggioRicevuta(Utilities.toXMLGregorianCalendar(rptRt.getDtRtDataOraMessaggioRicevuta()));
    ctEsito.setRiferimentoMessaggioRichiesta(rptRt.getCodRptIdMessaggioRichiesta());
    ctEsito.setRiferimentoDataRichiesta(Utilities.toXMLGregorianCalendar(rptRt.getDtRptDataOraMessaggioRichiesta()));

    /*
     * CtIstitutoAttestante
     */

    CtIstitutoAttestante istitutoAttestante = new CtIstitutoAttestante();
    CtIdentificativoUnivoco identificativoUnivocoAttestante = new CtIdentificativoUnivoco();
    identificativoUnivocoAttestante.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivoco.fromValue(rptRt.getCodRtIstitAttesIdUnivAttesTipoIdUnivoco()));
    identificativoUnivocoAttestante.setCodiceIdentificativoUnivoco(rptRt.getCodRtIstitAttesIdUnivAttesCodiceIdUnivoco());
    istitutoAttestante.setIdentificativoUnivocoAttestante(identificativoUnivocoAttestante);
    istitutoAttestante.setDenominazioneAttestante(StringUtils.stripToNull(rptRt.getDeRtIstitAttesDenominazioneAttestante()));
    istitutoAttestante.setCodiceUnitOperAttestante(StringUtils.stripToNull(rptRt.getCodRtIstitAttesCodiceUnitOperAttestante()));
    istitutoAttestante.setDenomUnitOperAttestante(StringUtils.stripToNull(rptRt.getDeRtIstitAttesDenomUnitOperAttestante()));
    istitutoAttestante.setIndirizzoAttestante(StringUtils.stripToNull(rptRt.getDeRtIstitAttesIndirizzoAttestante()));
    istitutoAttestante.setCivicoAttestante(StringUtils.stripToNull(rptRt.getDeRtIstitAttesCivicoAttestante()));
    istitutoAttestante.setCapAttestante(StringUtils.stripToNull(rptRt.getCodRtIstitAttesCapAttestante()));
    istitutoAttestante.setLocalitaAttestante(StringUtils.stripToNull(rptRt.getDeRtIstitAttesLocalitaAttestante()));
    istitutoAttestante.setProvinciaAttestante(StringUtils.stripToNull(rptRt.getDeRtIstitAttesProvinciaAttestante()));
    istitutoAttestante.setNazioneAttestante(StringUtils.stripToNull(rptRt.getCodRtIstitAttesNazioneAttestante()));
    ctEsito.setIstitutoAttestante(istitutoAttestante);

    /*
     * CtEnteBeneficiario
     */

    CtEnteBeneficiario enteBeneficiario = new CtEnteBeneficiario();
    CtIdentificativoUnivocoPersonaG identificativoUnivocoBeneficiario = new CtIdentificativoUnivocoPersonaG();
    identificativoUnivocoBeneficiario.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.fromValue(rptRt.getCodRtEnteBenefIdUnivBenefTipoIdUnivoco()));
    identificativoUnivocoBeneficiario.setCodiceIdentificativoUnivoco(rptRt.getCodRtEnteBenefIdUnivBenefCodiceIdUnivoco());
    enteBeneficiario.setIdentificativoUnivocoBeneficiario(identificativoUnivocoBeneficiario);
    enteBeneficiario.setDenominazioneBeneficiario(rptRt.getDeRtEnteBenefDenominazioneBeneficiario());
    enteBeneficiario.setCodiceUnitOperBeneficiario(StringUtils.stripToNull(rptRt.getCodRtEnteBenefCodiceUnitOperBeneficiario()));
    enteBeneficiario.setDenomUnitOperBeneficiario(StringUtils.stripToNull(rptRt.getDeRtEnteBenefDenomUnitOperBeneficiario()));
    enteBeneficiario.setIndirizzoBeneficiario(StringUtils.stripToNull(rptRt.getDeRtEnteBenefIndirizzoBeneficiario()));
    enteBeneficiario.setCivicoBeneficiario(StringUtils.stripToNull(rptRt.getDeRtEnteBenefCivicoBeneficiario()));
    enteBeneficiario.setCapBeneficiario(StringUtils.stripToNull(rptRt.getCodRtEnteBenefCapBeneficiario()));
    enteBeneficiario.setLocalitaBeneficiario(StringUtils.stripToNull(rptRt.getDeRtEnteBenefLocalitaBeneficiario()));
    enteBeneficiario.setProvinciaBeneficiario(StringUtils.stripToNull(rptRt.getDeRtEnteBenefProvinciaBeneficiario()));
    enteBeneficiario.setNazioneBeneficiario(StringUtils.stripToNull(rptRt.getCodRtEnteBenefNazioneBeneficiario()));
    ctEsito.setEnteBeneficiario(enteBeneficiario);

    /*
     * CtSoggettoVersante
     */

    if (StringUtils.isNotBlank(rptRt.getDeRtSoggVersAnagraficaVersante())) {
      CtSoggettoVersante soggettoVersante = new CtSoggettoVersante();
      CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante = new CtIdentificativoUnivocoPersonaFG();
      identificativoUnivocoVersante.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.fromValue(rptRt.getCodRtSoggVersIdUnivVersTipoIdUnivoco()));
      identificativoUnivocoVersante.setCodiceIdentificativoUnivoco(rptRt.getCodRtSoggVersIdUnivVersCodiceIdUnivoco());
      soggettoVersante.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);
      soggettoVersante.setAnagraficaVersante(rptRt.getDeRtSoggVersAnagraficaVersante());
      soggettoVersante.setIndirizzoVersante(StringUtils.stripToNull(rptRt.getDeRtSoggVersIndirizzoVersante()));
      soggettoVersante.setCivicoVersante(StringUtils.stripToNull(rptRt.getDeRtSoggVersCivicoVersante()));
      soggettoVersante.setCapVersante(StringUtils.stripToNull(rptRt.getCodRtSoggVersCapVersante()));
      soggettoVersante.setLocalitaVersante(StringUtils.stripToNull(rptRt.getDeRtSoggVersLocalitaVersante()));
      soggettoVersante.setProvinciaVersante(StringUtils.stripToNull(rptRt.getDeRtSoggVersProvinciaVersante()));
      soggettoVersante.setNazioneVersante(StringUtils.stripToNull(rptRt.getCodRtSoggVersNazioneVersante()));
      soggettoVersante.setEMailVersante(StringUtils.stripToNull(rptRt.getDeRtSoggVersEmailVersante()));
      ctEsito.setSoggettoVersante(soggettoVersante);
    }

    /*
     * CtSoggettoPagatore
     */

    CtSoggettoPagatore soggettoPagatore = new CtSoggettoPagatore();
    CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore = new CtIdentificativoUnivocoPersonaFG();
    identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.fromValue(rptRt.getCodRtSoggPagIdUnivPagTipoIdUnivoco()));
    identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(rptRt.getCodRtSoggPagIdUnivPagCodiceIdUnivoco());
    soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
    soggettoPagatore.setAnagraficaPagatore(rptRt.getDeRtSoggPagAnagraficaPagatore());
    soggettoPagatore.setIndirizzoPagatore(StringUtils.stripToNull(rptRt.getDeRtSoggPagIndirizzoPagatore()));
    soggettoPagatore.setCivicoPagatore(StringUtils.stripToNull(rptRt.getDeRtSoggPagCivicoPagatore()));
    soggettoPagatore.setCapPagatore(StringUtils.stripToNull(rptRt.getCodRtSoggPagCapPagatore()));
    soggettoPagatore.setLocalitaPagatore(StringUtils.stripToNull(rptRt.getDeRtSoggPagLocalitaPagatore()));
    soggettoPagatore.setProvinciaPagatore(StringUtils.stripToNull(rptRt.getDeRtSoggPagProvinciaPagatore()));
    soggettoPagatore.setNazionePagatore(StringUtils.stripToNull(rptRt.getCodRtSoggPagNazionePagatore()));
    soggettoPagatore.setEMailPagatore(StringUtils.stripToNull(rptRt.getDeRtSoggPagEmailPagatore()));
    ctEsito.setSoggettoPagatore(soggettoPagatore);

    /*
     * CtDatiVersamentoEsito
     */

    CtDatiVersamentoEsito datiPagamento = new CtDatiVersamentoEsito();
    datiPagamento.setCodiceEsitoPagamento(EnumUtils.StCodiceEsitoPagamento.forString(rptRt.getCodRtDatiPagCodiceEsitoPagamento()).toString());
    datiPagamento.setImportoTotalePagato(rptRt.getNumRtDatiPagImportoTotalePagato());
    datiPagamento.setIdentificativoUnivocoVersamento(rptRt.getCodRtDatiPagIdUnivocoVersamento());
    datiPagamento.setCodiceContestoPagamento(rptRt.getCodRtDatiPagCodiceContestoPagamento());

    /*
     * CtDatiSingoloPagamentoEsito
     */

    List<RptRtDettaglio> listaPagamenti = rptRtDettaglioDao.getByRptRtId(rptRt.getMygovRptRtId());
    if (listaPagamenti != null) {
      CtDatiSingoloPagamentoEsito[] pagamentiSingoli = new CtDatiSingoloPagamentoEsito[listaPagamenti.size()];
      int count = 0;
      for (int i = 0; i < listaPagamenti.size(); i++) {
        RptRtDettaglio dettaglioPagamento = listaPagamenti.get(i);
        if (dettaglioPagamento.getNumRtDatiPagDatiSingPagSingoloImportoPagato() == null) {
          continue;
        }
        count++;
        CtDatiSingoloPagamentoEsito singoloPagamento = new CtDatiSingoloPagamentoEsito();
        singoloPagamento.setSingoloImportoPagato(dettaglioPagamento.getNumRtDatiPagDatiSingPagSingoloImportoPagato());
        singoloPagamento.setCausaleVersamento(dettaglioPagamento.getDeRtDatiPagDatiSingPagCausaleVersamento());
        singoloPagamento.setDataEsitoSingoloPagamento(Utilities.toXMLGregorianCalendar(dettaglioPagamento.getDtRtDatiPagDatiSingPagDataEsitoSingoloPagamento()));
        singoloPagamento.setDatiSpecificiRiscossione(dettaglioPagamento.getDeRtDatiPagDatiSingPagDatiSpecificiRiscossione());
        singoloPagamento.setEsitoSingoloPagamento(StringUtils.stripToNull(dettaglioPagamento.getDeRtDatiPagDatiSingPagEsitoSingoloPagamento()));
        singoloPagamento.setIdentificativoUnivocoRiscossione(dettaglioPagamento.getCodRtDatiPagDatiSingPagIdUnivocoRiscossione());
        if (dettaglioPagamento.getNumRtDatiPagDatiSingPagCommissioniApplicatePsp()!=null)
          singoloPagamento.setCommissioniApplicatePSP(dettaglioPagamento.getNumRtDatiPagDatiSingPagCommissioniApplicatePsp());
        if (dettaglioPagamento.getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest() != null) {
          CtAllegatoRicevuta ctAllegatoRicevuta = new CtAllegatoRicevuta();
          ctAllegatoRicevuta.setTipoAllegatoRicevuta(StTipoAllegatoRicevuta.fromValue(dettaglioPagamento.getCodRtDatiPagDatiSingPagAllegatoRicevutaTipo()));
          ctAllegatoRicevuta.setTestoAllegato(dettaglioPagamento.getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest());
          singoloPagamento.setAllegatoRicevuta(ctAllegatoRicevuta);
        }
        pagamentiSingoli[i] = singoloPagamento;
      }

      if (count == listaPagamenti.size())
        datiPagamento.getDatiSingoloPagamentos().addAll(Arrays.asList(pagamentiSingoli));
    }

    ctEsito.setDatiPagamento(datiPagamento);

    return ctEsito;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public NodoSILInviaRPRisposta nodoSILInviaRP(NodoSILInviaRP bodyrichiesta,
                                               it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT header)
      throws NodoSILInviaRPRispostaException {

    NodoInviaRPTRisposta rispostaRPT = null;
    NodoSILInviaRPRisposta rispostaRP = null;

    //generare l'informazione idSession come UUID
    UUID idSession = UUID.randomUUID();

    RptRt mygovRptRt = null;
    RpE mygovRpE = null;
    try {
      //			SALVA DB RP
      RP rp = jaxbTransformService.unmarshalling(bodyrichiesta.getRp(), RP.class);

      mygovRpE = saveRp(header, bodyrichiesta, rp);

      //        	COSTRUISCI RPT
      //Prendo dati mancanti da tabella ente
      String idDominio = rp.getDominio().getIdentificativoDominio();
      Ente enteProp = enteFespService.getEnteByCodFiscale(idDominio);

      RPT ctRPT = this.buildRPT(rp, enteProp);
      IntestazionePPT paaSILInviaRPTHeader = this.buildHeaderRPT(header);
      NodoInviaRPT paaSILInviaRPTBody = this.buildBodyRPT(ctRPT, bodyrichiesta);

      //per poste questo campo e' obbligatorio e vuoto
      paaSILInviaRPTBody.setTipoFirma(Constants.EMPTY);

      //        	SALVA RPT
      mygovRptRt = saveRPT(paaSILInviaRPTHeader, paaSILInviaRPTBody, ctRPT, mygovRpE.getMygovRpEId(),
          bodyrichiesta.getModelloPagamento());

      try {
        RPT_Conservazione rtpCons = rptConservazioneService.insertRptConservazione(ctRPT,  mygovRptRt, enteProp);
        log.debug(rtpCons.toString());
      } catch (Exception e) {
        log.error("Errore nell'inserimento in RPT_Conservazione", e);
      }

      // se modello 2 o 3 prendo in carico l'invio RPT e torno subito OK
      if (bodyrichiesta.getModelloPagamento() == 4
          || StringUtils.isNotBlank(mygovRpE.getDeRpDatiVersIbanAddebito())) {

        log.debug("workaraund attiva per idSession: [" + idSession + "]");

        rispostaRP = new NodoSILInviaRPRisposta();
        rispostaRP.setEsito("OK");
        rispostaRP.setRedirect(1);

        FaultBean err = new FaultBean();
        rispostaRP.setFault(err);

      } else {
        //altrimenti invio adesso RPT
        rispostaRPT = pagamentiTelematiciRPTClient.nodoInviaRPT(
            paaSILInviaRPTBody,
            paaSILInviaRPTHeader);

        //        	COSTRUISCI RISPOSTA RP
        rispostaRP = builtRispostaRP(rispostaRPT, idSession);
      }

    } catch (Exception ex) {
      log.error(PPT_ESITO_SCONOSCIUTO + ": [" + ex.getMessage() + "]", ex);

      //non e' modello  2 ne modello 3 (in quei casi non c'e' risposta da salvare e non la costruisco)
      if (mygovRpE != null && bodyrichiesta.getModelloPagamento() != 4
          && StringUtils.isBlank(mygovRpE.getDeRpDatiVersIbanAddebito())) {
        //RISPOSTA RPT
        rispostaRPT = new NodoInviaRPTRisposta();
        rispostaRPT.setEsito("KO");

        gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean faultRPT = new gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean();
        faultRPT.setFaultCode(PPT_ESITO_SCONOSCIUTO);
        faultRPT.setDescription(ex.getMessage());
        rispostaRPT.setFault(faultRPT);
      }

      //RISPOSTA RP
      rispostaRP = new NodoSILInviaRPRisposta();
      rispostaRP.setEsito("KO");

      FaultBean faultRP = new FaultBean();
      faultRP.setFaultCode(PPT_ESITO_SCONOSCIUTO);
      faultRP.setDescription(ex.getMessage());
      rispostaRP.setFault(faultRP);
    }

    try {
      if (mygovRpE != null) {

        //	        	SALVA DB RISPOSTA RPT
        //non e' modello  2 ne modello 3 (in quei casi non c'e' risposta da salvare)
        if (bodyrichiesta.getModelloPagamento() != 4 && StringUtils.isBlank(mygovRpE.getDeRpDatiVersIbanAddebito())
            && mygovRptRt != null) {
          saveRPTRisposta(rispostaRPT, mygovRptRt.getMygovRptRtId());
        }

        //        	SALVA DB RISPOSTA RP
        saveRpRisposta(mygovRpE.getMygovRpEId(), rispostaRP, idSession);
      }
    } catch (Exception e) {
      log.error("Error saving RP risposta: [" + e.getMessage() + "]", e);
    }

    //se errore nel salvataggio (invio preso in carico) RP o RPT e (modello 2 o 3) ROLLBACK per permettere risottomissione
    if (rispostaRP.getEsito().equals("KO") && (bodyrichiesta.getModelloPagamento() == 4
        || StringUtils.isNotBlank(mygovRpE.getDeRpDatiVersIbanAddebito()))) {
      // rollback
      log.error("Rollback invia RPT con IUV [" + header.getIdentificativoUnivocoVersamento()
          + "] esito KO e CCP [" + header.getCodiceContestoPagamento() + "]");
      NodoSILInviaRPRispostaException nodoSILInviaRPRispostaException = new NodoSILInviaRPRispostaException(
          "Rollback invia RPT con IUV [" + header.getIdentificativoUnivocoVersamento()
              + "] esito KO e CCP [" + header.getCodiceContestoPagamento() + "]");

      nodoSILInviaRPRispostaException.setNodoSILInviaRPRisposta(rispostaRP);
      throw nodoSILInviaRPRispostaException;
    }



    // commit
    return rispostaRP;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void saveRPTRisposta(NodoInviaRPTRisposta rispostaRPT, Long mygovRptRtId)
      throws UnsupportedEncodingException, MalformedURLException {
    rptRtService.updateRispostaRptById(mygovRptRtId, rispostaRPT.getEsito(), rispostaRPT.getRedirect(),
        rispostaRPT.getUrl(),
        ((rispostaRPT.getFault() != null) ? rispostaRPT.getFault().getFaultCode() : null),
        ((rispostaRPT.getFault() != null) ? rispostaRPT.getFault().getFaultString() : null),
        ((rispostaRPT.getFault() != null) ? rispostaRPT.getFault().getId() : null),
        ((rispostaRPT.getFault() != null) ? rispostaRPT.getFault().getDescription() : null),
        ((rispostaRPT.getFault() != null) ? rispostaRPT.getFault().getSerial() : null),
        null, // OriginalFaultCode which not exists in Mypay4.
        null, // OriginalFaultString which not exists in Mypay4.
        null  // OriginalDescription which not exists in Mypay4.
    );
  }

  public IntestazionePPT buildHeaderRPT(it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT header) {
    IntestazionePPT result = new IntestazionePPT();

    result.setCodiceContestoPagamento(header.getCodiceContestoPagamento());
    result.setIdentificativoDominio(header.getIdentificativoDominio());
    //Questi dati sono stati tolti da RP.. prendere da tabella ente
    result.setIdentificativoIntermediarioPA(propIdIntermediarioPa);
    result.setIdentificativoStazioneIntermediarioPA(propIdStazioneIntermediarioPa);
    result.setIdentificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento());

    return result;
  }

  public IntestazionePPT buildHeaderRPT(RptRt mygovRptRt) {
    IntestazionePPT result = new IntestazionePPT();

    result.setCodiceContestoPagamento(mygovRptRt.getCodRptInviarptCodiceContestoPagamento());
    result.setIdentificativoDominio(mygovRptRt.getCodRptInviarptIdDominio());
    result.setIdentificativoIntermediarioPA(mygovRptRt.getCodRptInviarptIdIntermediarioPa());
    result.setIdentificativoStazioneIntermediarioPA(mygovRptRt.getCodRptInviarptIdStazioneIntermediarioPa());
    result.setIdentificativoUnivocoVersamento(mygovRptRt.getCodRptInviarptIdUnivocoVersamento());

    return result;
  }

  public RPT buildRPT(RP rp, Ente enteProp) {
    RPT ctRPT = new RPT();
    ctRPT.setVersioneOggetto(rp.getVersioneOggetto());

    it.gov.digitpa.schemas._2011.pagamenti.CtDominio dominio = new it.gov.digitpa.schemas._2011.pagamenti.CtDominio();
    dominio.setIdentificativoDominio(rp.getDominio().getIdentificativoDominio());
    dominio.setIdentificativoStazioneRichiedente(StringUtils.stripToNull(rp.getDominio().getIdentificativoStazioneRichiedente()));
    ctRPT.setDominio(dominio);

    ctRPT.setIdentificativoMessaggioRichiesta(rp.getIdentificativoMessaggioRichiesta());
    ctRPT.setDataOraMessaggioRichiesta(rp.getDataOraMessaggioRichiesta());
    ctRPT.setAutenticazioneSoggetto(
        it.gov.digitpa.schemas._2011.pagamenti.StAutenticazioneSoggetto.fromValue(rp.getAutenticazioneSoggetto().value()));

    it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoVersante soggettoVersante = new it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoVersante();
    it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante =
        new it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaFG();

    if (rp.getSoggettoVersante() != null) {
      identificativoUnivocoVersante.setTipoIdentificativoUnivoco(
          it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersFG.fromValue(
              rp.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().value()));
      identificativoUnivocoVersante.setCodiceIdentificativoUnivoco(
          rp.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());
      soggettoVersante.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);
      soggettoVersante.setAnagraficaVersante(rp.getSoggettoVersante().getAnagraficaVersante());
      soggettoVersante.setIndirizzoVersante(StringUtils.stripToNull(rp.getSoggettoVersante().getIndirizzoVersante()));
      soggettoVersante.setCivicoVersante(StringUtils.stripToNull(rp.getSoggettoVersante().getCivicoVersante()));
      soggettoVersante.setCapVersante(StringUtils.stripToNull(rp.getSoggettoVersante().getCapVersante()));
      soggettoVersante.setLocalitaVersante(StringUtils.stripToNull(rp.getSoggettoVersante().getLocalitaVersante()));
      soggettoVersante.setProvinciaVersante(StringUtils.stripToNull(rp.getSoggettoVersante().getProvinciaVersante()));
      soggettoVersante.setNazioneVersante(StringUtils.stripToNull(rp.getSoggettoVersante().getNazioneVersante()));
      soggettoVersante.setEMailVersante(StringUtils.stripToNull(rp.getSoggettoVersante().getEMailVersante()));
      ctRPT.setSoggettoVersante(soggettoVersante);
    }

    it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoPagatore soggettoPagatore = new it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoPagatore();
    it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore =
        new it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaFG();
    identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(
        it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersFG.fromValue(
            rp.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().value()));
    identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(
        rp.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());
    soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
    soggettoPagatore.setAnagraficaPagatore(rp.getSoggettoPagatore().getAnagraficaPagatore());
    soggettoPagatore.setIndirizzoPagatore(StringUtils.stripToNull(rp.getSoggettoPagatore().getIndirizzoPagatore()));
    soggettoPagatore.setCivicoPagatore(StringUtils.stripToNull(rp.getSoggettoPagatore().getCivicoPagatore()));
    soggettoPagatore.setCapPagatore(StringUtils.stripToNull(rp.getSoggettoPagatore().getCapPagatore()));
    soggettoPagatore.setLocalitaPagatore(StringUtils.stripToNull(rp.getSoggettoPagatore().getLocalitaPagatore()));
    soggettoPagatore.setProvinciaPagatore(StringUtils.stripToNull(rp.getSoggettoPagatore().getProvinciaPagatore()));
    soggettoPagatore.setNazionePagatore(StringUtils.stripToNull(rp.getSoggettoPagatore().getNazionePagatore()));
    soggettoPagatore.setEMailPagatore(StringUtils.stripToNull(rp.getSoggettoPagatore().getEMailPagatore()));
    ctRPT.setSoggettoPagatore(soggettoPagatore);

    //l'ente sull'rp nn c'e' (prendere da tabella ente)
    it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario enteBeneficiario = new it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario();
    it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaG idUnivocoPersonaG =
        new it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaG();
    idUnivocoPersonaG.setTipoIdentificativoUnivoco(
        it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersG.fromValue(enteProp.getCodRpEnteBenefIdUnivBenefTipoIdUnivoco()));
    idUnivocoPersonaG.setCodiceIdentificativoUnivoco(enteProp.getCodRpEnteBenefIdUnivBenefCodiceIdUnivoco());
    enteBeneficiario.setIdentificativoUnivocoBeneficiario(idUnivocoPersonaG);
    enteBeneficiario.setDenominazioneBeneficiario(enteProp.getDeRpEnteBenefDenominazioneBeneficiario());
    enteBeneficiario.setDenomUnitOperBeneficiario(StringUtils.stripToNull(enteProp.getDeRpEnteBenefDenomUnitOperBeneficiario()));
    enteBeneficiario.setCodiceUnitOperBeneficiario(StringUtils.stripToNull(enteProp.getCodRpEnteBenefCodiceUnitOperBeneficiario()));
    enteBeneficiario.setLocalitaBeneficiario(StringUtils.stripToNull(enteProp.getDeRpEnteBenefLocalitaBeneficiario()));
    enteBeneficiario.setProvinciaBeneficiario(StringUtils.stripToNull(enteProp.getDeRpEnteBenefProvinciaBeneficiario()));
    enteBeneficiario.setIndirizzoBeneficiario(StringUtils.stripToNull(enteProp.getDeRpEnteBenefIndirizzoBeneficiario()));
    enteBeneficiario.setCivicoBeneficiario(StringUtils.stripToNull(enteProp.getDeRpEnteBenefCivicoBeneficiario()));
    enteBeneficiario.setCapBeneficiario(StringUtils.stripToNull(enteProp.getCodRpEnteBenefCapBeneficiario()));
    enteBeneficiario.setNazioneBeneficiario(StringUtils.stripToNull(enteProp.getCodRpEnteBenefNazioneBeneficiario()));

    ctRPT.setEnteBeneficiario(enteBeneficiario);

    it.gov.digitpa.schemas._2011.pagamenti.CtDatiVersamentoRPT datiVersamento = new it.gov.digitpa.schemas._2011.pagamenti.CtDatiVersamentoRPT();
    datiVersamento.setDataEsecuzionePagamento(rp.getDatiVersamento().getDataEsecuzionePagamento());
    datiVersamento.setImportoTotaleDaVersare(rp.getDatiVersamento().getImportoTotaleDaVersare());
    datiVersamento.setTipoVersamento(
        it.gov.digitpa.schemas._2011.pagamenti.StTipoVersamento.fromValue(rp.getDatiVersamento().getTipoVersamento().value()));
    datiVersamento.setIdentificativoUnivocoVersamento(rp.getDatiVersamento().getIdentificativoUnivocoVersamento());
    datiVersamento.setCodiceContestoPagamento(rp.getDatiVersamento().getCodiceContestoPagamento());
    datiVersamento.setIbanAddebito(StringUtils.stripToNull(rp.getDatiVersamento().getIbanAddebito()));
    datiVersamento.setBicAddebito(StringUtils.stripToNull(rp.getDatiVersamento().getBicAddebito()));

    //Tipo Firma richiesta per la RT (preso dalla tabella ente)
    datiVersamento.setFirmaRicevuta(EnumUtils.StFirmaRicevuta.forString(enteProp.getCodRpDatiVersFirmaRicevuta()).toString());

    //SINGOLI VERSAMENTI
    List<it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT> datiSingoloVersamentoArray = new ArrayList<>();

    for (it.veneto.regione.schemas._2012.pagamenti.CtDatiSingoloVersamentoRP tmpVer: rp.getDatiVersamento().getDatiSingoloVersamentos()) {
      it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT datiSingoloVersamento = new it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT();
      datiSingoloVersamento.setImportoSingoloVersamento(tmpVer.getImportoSingoloVersamento());
      datiSingoloVersamento.setCommissioneCaricoPA(tmpVer.getCommissioneCaricoPA());
      datiSingoloVersamento.setIbanAccredito(StringUtils.stripToNull(tmpVer.getIbanAccredito()));
      datiSingoloVersamento.setBicAccredito(StringUtils.stripToNull(tmpVer.getBicAccredito()));
      datiSingoloVersamento.setIbanAppoggio(StringUtils.stripToNull(tmpVer.getIbanAppoggio()));
      datiSingoloVersamento.setBicAppoggio(StringUtils.stripToNull(tmpVer.getBicAppoggio()));
      datiSingoloVersamento.setCredenzialiPagatore(StringUtils.stripToNull(tmpVer.getCredenzialiPagatore()));

      if (tmpVer.getDatiMarcaBolloDigitale() != null) {
        it.gov.digitpa.schemas._2011.pagamenti.CtDatiMarcaBolloDigitale newMarcaBolloDigitale = new it.gov.digitpa.schemas._2011.pagamenti.CtDatiMarcaBolloDigitale();
        newMarcaBolloDigitale.setTipoBollo(EnumUtils.StTipoBollo.forString(tmpVer.getDatiMarcaBolloDigitale().getTipoBollo()).toString());
        newMarcaBolloDigitale.setHashDocumento(tmpVer.getDatiMarcaBolloDigitale().getHashDocumento());
        newMarcaBolloDigitale.setProvinciaResidenza(tmpVer.getDatiMarcaBolloDigitale().getProvinciaResidenza());
        datiSingoloVersamento.setDatiMarcaBolloDigitale(newMarcaBolloDigitale);
      }

      datiSingoloVersamento.setCausaleVersamento(tmpVer.getCausaleVersamento());
      datiSingoloVersamento.setDatiSpecificiRiscossione(tmpVer.getDatiSpecificiRiscossione());
      datiSingoloVersamentoArray.add(datiSingoloVersamento);
    }

    datiVersamento.getDatiSingoloVersamentos().addAll(datiSingoloVersamentoArray);
    ctRPT.setDatiVersamento(datiVersamento);

    return ctRPT;
  }

  public RPT buildRPT(RptRt mygovRptRt) {
    List<RptRtDettaglio> mygovRptRtDettaglios = rptRtDettaglioDao.getByRptRtId(mygovRptRt.getMygovRptRtId());

    RPT ctRPT = new RPT();

    ctRPT.setVersioneOggetto(mygovRptRt.getDeRptVersioneOggetto());

    it.gov.digitpa.schemas._2011.pagamenti.CtDominio dominio = new it.gov.digitpa.schemas._2011.pagamenti.CtDominio();
    dominio.setIdentificativoDominio(mygovRptRt.getCodRptDomIdDominio());
    dominio.setIdentificativoStazioneRichiedente(StringUtils.stripToNull(mygovRptRt.getCodRptDomIdStazioneRichiedente()));
    ctRPT.setDominio(dominio);

    ctRPT.setIdentificativoMessaggioRichiesta(mygovRptRt.getCodRptIdMessaggioRichiesta());

    GDateBuilder builder = new GDateBuilder(mygovRptRt.getDtRptDataOraMessaggioRichiesta());
    builder.clearTimeZone();
    Calendar dtRptDataOraMessaggioRichiesta = builder.getCalendar();

    ctRPT.setDataOraMessaggioRichiesta(Utilities.toXMLGregorianCalendar(dtRptDataOraMessaggioRichiesta));

    ctRPT.setAutenticazioneSoggetto(
        it.gov.digitpa.schemas._2011.pagamenti.StAutenticazioneSoggetto.fromValue(mygovRptRt.getCodRptAutenticazioneSoggetto()));

    it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoVersante soggettoVersante = new it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoVersante();
    it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante =
        new it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaFG();

    if (StringUtils.isNotBlank(mygovRptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco())) {
      identificativoUnivocoVersante.setTipoIdentificativoUnivoco(
          it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersFG.fromValue(mygovRptRt.getCodRptSoggVersIdUnivVersTipoIdUnivoco()));
      identificativoUnivocoVersante
          .setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco());
      soggettoVersante.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);
      soggettoVersante.setAnagraficaVersante(mygovRptRt.getDeRptSoggVersAnagraficaVersante());
      soggettoVersante.setIndirizzoVersante(StringUtils.stripToNull(mygovRptRt.getDeRptSoggVersIndirizzoVersante()));
      soggettoVersante.setCivicoVersante(StringUtils.stripToNull(mygovRptRt.getDeRptSoggVersCivicoVersante()));
      soggettoVersante.setCapVersante(StringUtils.stripToNull(mygovRptRt.getCodRptSoggVersCapVersante()));
      soggettoVersante.setLocalitaVersante(StringUtils.stripToNull(mygovRptRt.getDeRptSoggVersLocalitaVersante()));
      soggettoVersante.setProvinciaVersante(StringUtils.stripToNull(mygovRptRt.getDeRptSoggVersProvinciaVersante()));
      soggettoVersante.setNazioneVersante(StringUtils.stripToNull(mygovRptRt.getCodRptSoggVersNazioneVersante()));
      soggettoVersante.setEMailVersante(StringUtils.stripToNull(mygovRptRt.getDeRptSoggVersEmailVersante()));
      ctRPT.setSoggettoVersante(soggettoVersante);
    }

    it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoPagatore soggettoPagatore = new it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoPagatore();
    it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore =
        new it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaFG();

    identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(
        it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersFG.fromValue(mygovRptRt.getCodRptSoggPagIdUnivPagTipoIdUnivoco()));
    identificativoUnivocoPagatore
        .setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco());
    soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
    soggettoPagatore.setAnagraficaPagatore(mygovRptRt.getDeRptSoggPagAnagraficaPagatore());
    soggettoPagatore.setIndirizzoPagatore(StringUtils.stripToNull(mygovRptRt.getDeRptSoggPagIndirizzoPagatore()));
    soggettoPagatore.setCivicoPagatore(StringUtils.stripToNull(mygovRptRt.getDeRptSoggPagCivicoPagatore()));
    soggettoPagatore.setCapPagatore(StringUtils.stripToNull(mygovRptRt.getCodRptSoggPagCapPagatore()));
    soggettoPagatore.setLocalitaPagatore(StringUtils.stripToNull(mygovRptRt.getDeRptSoggPagLocalitaPagatore()));
    soggettoPagatore.setProvinciaPagatore(StringUtils.stripToNull(mygovRptRt.getDeRptSoggPagProvinciaPagatore()));
    soggettoPagatore.setNazionePagatore(StringUtils.stripToNull(mygovRptRt.getCodRptSoggPagNazionePagatore()));
    soggettoPagatore.setEMailPagatore(StringUtils.stripToNull(mygovRptRt.getDeRptSoggPagEmailPagatore()));

    ctRPT.setSoggettoPagatore(soggettoPagatore);

    it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario enteBeneficiario = new it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario();

    it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaG idUnivocoPersonaG =
        new it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaG();

    idUnivocoPersonaG.setTipoIdentificativoUnivoco(
        it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersG.fromValue(mygovRptRt.getCodRptEnteBenefIdUnivBenefTipoIdUnivoco()));
    idUnivocoPersonaG.setCodiceIdentificativoUnivoco(mygovRptRt.getCodRptEnteBenefIdUnivBenefCodiceIdUnivoco());
    enteBeneficiario.setIdentificativoUnivocoBeneficiario(idUnivocoPersonaG);
    enteBeneficiario.setDenominazioneBeneficiario(mygovRptRt.getDeRptEnteBenefDenominazioneBeneficiario());
    enteBeneficiario.setDenomUnitOperBeneficiario(StringUtils.stripToNull(mygovRptRt.getDeRptEnteBenefDenomUnitOperBeneficiario()));
    enteBeneficiario.setCodiceUnitOperBeneficiario(StringUtils.stripToNull(mygovRptRt.getCodRptEnteBenefCodiceUnitOperBeneficiario()));
    enteBeneficiario.setLocalitaBeneficiario(StringUtils.stripToNull(mygovRptRt.getDeRptEnteBenefLocalitaBeneficiario()));
    enteBeneficiario.setProvinciaBeneficiario(StringUtils.stripToNull(mygovRptRt.getDeRptEnteBenefProvinciaBeneficiario()));
    enteBeneficiario.setIndirizzoBeneficiario(StringUtils.stripToNull(mygovRptRt.getDeRptEnteBenefIndirizzoBeneficiario()));
    enteBeneficiario.setCivicoBeneficiario(StringUtils.stripToNull(mygovRptRt.getDeRptEnteBenefCivicoBeneficiario()));
    enteBeneficiario.setCapBeneficiario(StringUtils.stripToNull(mygovRptRt.getCodRptEnteBenefCapBeneficiario()));
    enteBeneficiario.setNazioneBeneficiario(StringUtils.stripToNull(mygovRptRt.getCodRptEnteBenefNazioneBeneficiario()));

    ctRPT.setEnteBeneficiario(enteBeneficiario);

    it.gov.digitpa.schemas._2011.pagamenti.CtDatiVersamentoRPT datiVersamento = new it.gov.digitpa.schemas._2011.pagamenti.CtDatiVersamentoRPT();

    builder = new GDateBuilder(mygovRptRt.getDtRptDatiVersDataEsecuzionePagamento());
    builder.clearTimeZone();
    Calendar dtRptDatiVersDataEsecuzionePagamento = builder.getCalendar();

    datiVersamento.setDataEsecuzionePagamento(Utilities.toXMLGregorianCalendar(dtRptDatiVersDataEsecuzionePagamento));

    datiVersamento.setImportoTotaleDaVersare(mygovRptRt.getNumRptDatiVersImportoTotaleDaVersare());
    datiVersamento.setTipoVersamento(
        it.gov.digitpa.schemas._2011.pagamenti.StTipoVersamento.fromValue(mygovRptRt.getCodRptDatiVersTipoVersamento()));
    datiVersamento.setIdentificativoUnivocoVersamento(mygovRptRt.getCodRptDatiVersIdUnivocoVersamento());
    datiVersamento.setCodiceContestoPagamento(mygovRptRt.getCodRptDatiVersCodiceContestoPagamento());
    datiVersamento.setIbanAddebito(StringUtils.stripToNull(mygovRptRt.getDeRptDatiVersIbanAddebito()));
    datiVersamento.setBicAddebito(StringUtils.stripToNull(mygovRptRt.getDeRptDatiVersBicAddebito()));

    //Tipo Firma richiesta per la RT (preso dalla tabella ente)
    datiVersamento.setFirmaRicevuta(EnumUtils.StFirmaRicevuta.forString(mygovRptRt.getCodRptDatiVersFirmaRicevuta()).toString());

    //SINGOLI VERSAMENTI
    List<it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT> datiSingoloVersamentoArray = new ArrayList<>();

    for (RptRtDettaglio mygovRptRtDettaglio : mygovRptRtDettaglios) {

      it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT datiSingoloVersamento =
          new it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT();
      datiSingoloVersamento.setImportoSingoloVersamento(
          mygovRptRtDettaglio.getNumRptDatiVersDatiSingVersImportoSingoloVersamento());
      if (mygovRptRtDettaglio.getNumRptDatiVersDatiSingVersCommissioneCaricoPa() != null)
        datiSingoloVersamento.setCommissioneCaricoPA(mygovRptRtDettaglio.getNumRptDatiVersDatiSingVersCommissioneCaricoPa());
      datiSingoloVersamento.setIbanAccredito(StringUtils.stripToNull(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersIbanAccredito()));
      datiSingoloVersamento.setBicAccredito(StringUtils.stripToNull(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersBicAccredito()));
      datiSingoloVersamento.setIbanAppoggio(StringUtils.stripToNull(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersIbanAppoggio()));
      datiSingoloVersamento.setBicAppoggio(StringUtils.stripToNull(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersBicAppoggio()));
      datiSingoloVersamento.setCredenzialiPagatore(StringUtils.stripToNull(mygovRptRtDettaglio.getCodRptDatiVersDatiSingVersCredenzialiPagatore()));
      datiSingoloVersamento.setCausaleVersamento(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersCausaleVersamento());
      datiSingoloVersamento.setDatiSpecificiRiscossione(mygovRptRtDettaglio.getDeRptDatiVersDatiSingVersDatiSpecificiRiscossione());
      datiSingoloVersamentoArray.add(datiSingoloVersamento);
    }

    datiVersamento.getDatiSingoloVersamentos().addAll(datiSingoloVersamentoArray);
    ctRPT.setDatiVersamento(datiVersamento);

    return ctRPT;
  }

  public NodoInviaRPT buildBodyRPT(it.gov.digitpa.schemas._2011.pagamenti.RPT rpt,
                                   NodoSILInviaRP bodyrichiesta) {
    byte[] byteRPT = jaxbTransformService.marshallingAsBytes(rpt, RPT.class);

    NodoInviaRPT result = new NodoInviaRPT();
    result.setIdentificativoCanale(StringUtils.stripToNull(bodyrichiesta.getIdentificativoCanale()));
    result.setIdentificativoIntermediarioPSP(StringUtils.stripToNull(bodyrichiesta.getIdentificativoIntermediarioPSP()));
    result.setIdentificativoPSP(bodyrichiesta.getIdentificativoPSP());
    result.setPassword(propIdIntermediarioPaPassword);
    result.setRpt(byteRPT);

    return result;
  }

  public NodoInviaRPT buildBodyRPT(it.gov.digitpa.schemas._2011.pagamenti.RPT rpt,
                                   RptRt mygovRptRt) {
    byte[] byteRPT = jaxbTransformService.marshallingAsBytes(rpt, RPT.class);

    NodoInviaRPT result = new NodoInviaRPT();
    result.setIdentificativoCanale(StringUtils.stripToNull(mygovRptRt.getCodRptInviarptIdCanale()));
    result.setIdentificativoIntermediarioPSP(StringUtils.stripToNull(mygovRptRt.getCodRptInviarptIdIntermediarioPsp()));
    result.setIdentificativoPSP(mygovRptRt.getCodRptInviarptIdPsp());
    result.setPassword(propIdIntermediarioPaPassword);
    result.setRpt(byteRPT);

    return result;
  }

  /**
   * @param mygovRpEId
   * @param rispostaRP
   * @param idSession
   */
  private void saveRpRisposta(Long mygovRpEId, NodoSILInviaRPRisposta rispostaRP, UUID idSession) {

    FaultBean esitoFault = rispostaRP.getFault();

    rpEService.updateRispostaRpById(mygovRpEId, rispostaRP.getEsito(), rispostaRP.getRedirect(),
        rispostaRP.getUrl(), esitoFault.getFaultCode(), esitoFault.getFaultString(), esitoFault.getId(),
        esitoFault.getDescription(), esitoFault.getSerial(), idSession.toString(), esitoFault.getOriginalFaultCode(),
        esitoFault.getOriginalFaultString(), esitoFault.getOriginalDescription());
  }

  private RpE saveRp(it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT header,
                          NodoSILInviaRP bodyrichiesta, it.veneto.regione.schemas._2012.pagamenti.RP rp) {
    List<RpEDettaglioDto> rpVersamenti = new ArrayList<>();

    List<it.veneto.regione.schemas._2012.pagamenti.CtDatiSingoloVersamentoRP> versamenti = rp.getDatiVersamento().getDatiSingoloVersamentos();

    for (it.veneto.regione.schemas._2012.pagamenti.CtDatiSingoloVersamentoRP singoloVersamento: versamenti) {
      RpEDettaglioDto rpVersamento = new RpEDettaglioDto();

      rpVersamento.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(
          singoloVersamento.getImportoSingoloVersamento());
      rpVersamento.setNumRpDatiVersDatiSingVersCommissioneCaricoPa(singoloVersamento.getCommissioneCaricoPA());
      rpVersamento.setCodRpDatiVersDatiSingVersIbanAccredito(singoloVersamento.getIbanAccredito());
      rpVersamento.setCodRpDatiVersDatiSingVersBicAccredito(singoloVersamento.getBicAccredito());
      rpVersamento.setCodRpDatiVersDatiSingVersIbanAppoggio(singoloVersamento.getIbanAppoggio());
      rpVersamento.setCodRpDatiVersDatiSingVersBicAppoggio(singoloVersamento.getBicAppoggio());
      rpVersamento.setCodRpDatiVersDatiSingVersCredenzialiPagatore(singoloVersamento.getCredenzialiPagatore());
      rpVersamento.setDeRpDatiVersDatiSingVersCausaleVersamento(singoloVersamento.getCausaleVersamento());
      rpVersamento.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(
          singoloVersamento.getDatiSpecificiRiscossione());

      rpVersamento.setNumEDatiPagDatiSingPagCommissioniApplicatePsp(null);
      rpVersamento.setCodEDatiPagDatiSingPagAllegatoRicevutaTipo(null);
      rpVersamento.setBlbEDatiPagDatiSingPagAllegatoRicevutaTest(null);

      if (singoloVersamento.getDatiMarcaBolloDigitale() != null) {
        rpVersamento.setCodRpDatiVersDatiSingVersDatiMbdTipoBollo(
            singoloVersamento.getDatiMarcaBolloDigitale().getTipoBollo());
        rpVersamento.setCodRpDatiVersDatiSingVersDatiMbdHashDocumento(
            singoloVersamento.getDatiMarcaBolloDigitale().getHashDocumento());
        rpVersamento.setCodRpDatiVersDatiSingVersDatiMbdProvinciaResidenza(
            singoloVersamento.getDatiMarcaBolloDigitale().getProvinciaResidenza());
      }

      rpVersamenti.add(rpVersamento);
    }

    return rpEService.insertRPWithRefresh(bodyrichiesta.getIdentificativoPSP(),
        bodyrichiesta.getIdentificativoIntermediarioPSP(), bodyrichiesta.getIdentificativoCanale(),
        header.getIdentificativoDominio(), header.getIdentificativoUnivocoVersamento(),
        header.getCodiceContestoPagamento(), rp.getVersioneOggetto(),
        rp.getDominio().getIdentificativoDominio(), rp.getDominio().getIdentificativoStazioneRichiedente(),
        rp.getIdentificativoMessaggioRichiesta(), Utilities.toDate(rp.getDataOraMessaggioRichiesta()),
        rp.getAutenticazioneSoggetto().toString(),
        Utilities.ifNotNull(rp.getSoggettoVersante(), ct -> ct.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString()),
        Utilities.ifNotNull(rp.getSoggettoVersante(), ct -> ct.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco()),
        Utilities.ifNotNull(rp.getSoggettoVersante(), CtSoggettoVersante::getAnagraficaVersante),
        Utilities.ifNotNull(rp.getSoggettoVersante(), CtSoggettoVersante::getIndirizzoVersante),
        Utilities.ifNotNull(rp.getSoggettoVersante(), CtSoggettoVersante::getCivicoVersante),
        Utilities.ifNotNull(rp.getSoggettoVersante(), CtSoggettoVersante::getCapVersante),
        Utilities.ifNotNull(rp.getSoggettoVersante(), CtSoggettoVersante::getLocalitaVersante),
        Utilities.ifNotNull(rp.getSoggettoVersante(), CtSoggettoVersante::getProvinciaVersante),
        Utilities.ifNotNull(rp.getSoggettoVersante(), CtSoggettoVersante::getNazioneVersante),
        Utilities.ifNotNull(rp.getSoggettoVersante(), CtSoggettoVersante::getEMailVersante),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), ct -> ct.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString()),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), ct -> ct.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco()),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), CtSoggettoPagatore::getAnagraficaPagatore),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), CtSoggettoPagatore::getIndirizzoPagatore),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), CtSoggettoPagatore::getCivicoPagatore),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), CtSoggettoPagatore::getCapPagatore),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), CtSoggettoPagatore::getLocalitaPagatore),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), CtSoggettoPagatore::getProvinciaPagatore),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), CtSoggettoPagatore::getNazionePagatore),
        Utilities.ifNotNull(rp.getSoggettoPagatore(), CtSoggettoPagatore::getEMailPagatore),
        Utilities.ifNotNull(rp.getDatiVersamento(), ct -> Utilities.toDate(ct.getDataEsecuzionePagamento())),
        Utilities.ifNotNull(rp.getDatiVersamento(), CtDatiVersamentoRP::getImportoTotaleDaVersare),
        Utilities.ifNotNull(rp.getDatiVersamento(), ct -> ct.getTipoVersamento().toString()),
        Utilities.ifNotNull(rp.getDatiVersamento(), CtDatiVersamentoRP::getIdentificativoUnivocoVersamento),
        Utilities.ifNotNull(rp.getDatiVersamento(), CtDatiVersamentoRP::getCodiceContestoPagamento),
        Utilities.ifNotNull(rp.getDatiVersamento(), CtDatiVersamentoRP::getIbanAddebito),
        Utilities.ifNotNull(rp.getDatiVersamento(), CtDatiVersamentoRP::getBicAddebito),
        bodyrichiesta.getModelloPagamento(), rpVersamenti);
  }

  private RptRt saveRPT(gov.telematici.pagamenti.ws.nodospcpernodoregionale.IntestazionePPT header,
                             NodoInviaRPT paaSILInviaRPTBody,
                             RPT rpt, Long mygovRpEId,
                             Integer modelloPagamento) {
    List<RptRtDettaglioDto> richiestaVersamenti = new ArrayList<>();

    List<it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT> versamenti = rpt.getDatiVersamento()
        .getDatiSingoloVersamentos();

    for (it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloVersamentoRPT tmpVer: versamenti) {
      RptRtDettaglioDto tmpRPTVer = new RptRtDettaglioDto();

      tmpRPTVer.setNumRptDatiVersDatiSingVersImportoSingoloVersamento(tmpVer.getImportoSingoloVersamento());
      tmpRPTVer.setNumRptDatiVersDatiSingVersCommissioneCaricoPa(tmpVer.getCommissioneCaricoPA());
      tmpRPTVer.setDeRptDatiVersDatiSingVersIbanAccredito(tmpVer.getIbanAccredito());
      tmpRPTVer.setDeRptDatiVersDatiSingVersBicAccredito(tmpVer.getBicAccredito());
      tmpRPTVer.setDeRptDatiVersDatiSingVersIbanAppoggio(tmpVer.getIbanAppoggio());
      tmpRPTVer.setDeRptDatiVersDatiSingVersBicAppoggio(tmpVer.getBicAppoggio());
      tmpRPTVer.setCodRptDatiVersDatiSingVersCredenzialiPagatore(tmpVer.getCredenzialiPagatore());
      tmpRPTVer.setDeRptDatiVersDatiSingVersCausaleVersamento(tmpVer.getCausaleVersamento());
      tmpRPTVer.setDeRptDatiVersDatiSingVersDatiSpecificiRiscossione(tmpVer.getDatiSpecificiRiscossione());

      tmpRPTVer.setNumRtDatiPagDatiSingPagCommissioniApplicatePsp(null);
      tmpRPTVer.setCodRtDatiPagDatiSingPagAllegatoRicevutaTipo(null);
      tmpRPTVer.setBlbRtDatiPagDatiSingPagAllegatoRicevutaTest(null);

      if (tmpVer.getDatiMarcaBolloDigitale() != null) {
        tmpRPTVer.setCodRptDatiVersDatiSingVersDatiMbdTipoBollo(
            tmpVer.getDatiMarcaBolloDigitale().getTipoBollo());
        tmpRPTVer.setCodRptDatiVersDatiSingVersDatiMbdHashDocumento(
            tmpVer.getDatiMarcaBolloDigitale().getHashDocumento());
        tmpRPTVer.setCodRptDatiVersDatiSingVersDatiMbdProvinciaResidenza(
            tmpVer.getDatiMarcaBolloDigitale().getProvinciaResidenza());
      }

      richiestaVersamenti.add(tmpRPTVer);
    }

    String soggVersanteTipoIdentificativoUnivocoVersante = null;
    String soggVersanteCodiceIdentificativoUnivoco = null;

    if ((rpt.getSoggettoVersante() != null)
        && (rpt.getSoggettoVersante().getIdentificativoUnivocoVersante() != null)) {
      if (rpt.getSoggettoVersante().getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco() != null) {
        soggVersanteTipoIdentificativoUnivocoVersante = rpt.getSoggettoVersante()
            .getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString();
      }

      soggVersanteCodiceIdentificativoUnivoco = rpt.getSoggettoVersante().getIdentificativoUnivocoVersante()
          .getCodiceIdentificativoUnivoco();
    }

    return rptRtService.insertRpt(paaSILInviaRPTBody.getPassword(),
        paaSILInviaRPTBody.getIdentificativoPSP(), paaSILInviaRPTBody.getIdentificativoIntermediarioPSP(),
        paaSILInviaRPTBody.getIdentificativoCanale(), paaSILInviaRPTBody.getTipoFirma(),
        header.getIdentificativoIntermediarioPA(), header.getIdentificativoStazioneIntermediarioPA(),
        header.getIdentificativoDominio(), header.getIdentificativoUnivocoVersamento(),
        header.getCodiceContestoPagamento(), rpt.getVersioneOggetto(),
        rpt.getDominio().getIdentificativoDominio(), rpt.getDominio().getIdentificativoStazioneRichiedente(),
        rpt.getIdentificativoMessaggioRichiesta(), Utilities.toDate(rpt.getDataOraMessaggioRichiesta()),
        rpt.getAutenticazioneSoggetto().value(), soggVersanteTipoIdentificativoUnivocoVersante,
        soggVersanteCodiceIdentificativoUnivoco,
        ((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getAnagraficaVersante() : null),
        ((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getIndirizzoVersante() : null),
        ((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getCivicoVersante() : null),
        ((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getCapVersante() : null),
        ((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getLocalitaVersante() : null),
        ((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getProvinciaVersante() : null),
        ((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getNazioneVersante() : null),
        ((rpt.getSoggettoVersante() != null) ? rpt.getSoggettoVersante().getEMailVersante() : null),
        rpt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString(),
        rpt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco(),
        rpt.getSoggettoPagatore().getAnagraficaPagatore(), rpt.getSoggettoPagatore().getIndirizzoPagatore(),
        rpt.getSoggettoPagatore().getCivicoPagatore(), rpt.getSoggettoPagatore().getCapPagatore(),
        rpt.getSoggettoPagatore().getLocalitaPagatore(), rpt.getSoggettoPagatore().getProvinciaPagatore(),
        rpt.getSoggettoPagatore().getNazionePagatore(), rpt.getSoggettoPagatore().getEMailPagatore(),
        rpt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco().toString(),
        rpt.getEnteBeneficiario().getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco(),
        rpt.getEnteBeneficiario().getDenominazioneBeneficiario(),
        rpt.getEnteBeneficiario().getCodiceUnitOperBeneficiario(),
        rpt.getEnteBeneficiario().getDenomUnitOperBeneficiario(),
        rpt.getEnteBeneficiario().getIndirizzoBeneficiario(), rpt.getEnteBeneficiario().getCivicoBeneficiario(),
        rpt.getEnteBeneficiario().getCapBeneficiario(), rpt.getEnteBeneficiario().getLocalitaBeneficiario(),
        rpt.getEnteBeneficiario().getProvinciaBeneficiario(),
        rpt.getEnteBeneficiario().getNazioneBeneficiario(),
        Utilities.toDate(rpt.getDatiVersamento().getDataEsecuzionePagamento()),
        rpt.getDatiVersamento().getImportoTotaleDaVersare(),
        rpt.getDatiVersamento().getTipoVersamento().toString(),
        rpt.getDatiVersamento().getIdentificativoUnivocoVersamento(),
        rpt.getDatiVersamento().getCodiceContestoPagamento(), rpt.getDatiVersamento().getIbanAddebito(),
        rpt.getDatiVersamento().getBicAddebito(), rpt.getDatiVersamento().getFirmaRicevuta(),
        richiestaVersamenti, modelloPagamento, mygovRpEId);

  }

  private NodoSILInviaRPRisposta builtRispostaRP(NodoInviaRPTRisposta rispostaRPT, UUID idSession) {
    NodoSILInviaRPRisposta rispostaRP = new NodoSILInviaRPRisposta();

    rispostaRP.setEsito(rispostaRPT.getEsito());
    rispostaRP.setRedirect(rispostaRPT.getRedirect());

    if ((rispostaRPT.getRedirect() != null) && (rispostaRPT.getRedirect() == 1)) {
      //prendi da properties :: NODO REGIONALE FESP BASE URL
      rispostaRP.setUrl(appBeAbsolutePath + "/nodoSILInviaRichiestaPagamento.html?idSession="
          + idSession.toString());
    } else {
      rispostaRP.setUrl(rispostaRPT.getUrl());
    }

    FaultBean err = new FaultBean();

    if (rispostaRPT.getFault() != null) {
      err.setFaultCode(rispostaRPT.getFault().getFaultCode());
      err.setFaultString(rispostaRPT.getFault().getFaultString());
      err.setId(rispostaRPT.getFault().getId());
      err.setDescription(rispostaRPT.getFault().getDescription());
      err.setOriginalFaultCode(null);   // OriginalFaultCode which not exists in Mypay4.
      err.setOriginalFaultString(null); // OriginalFaultString which not exists in Mypay4.
      err.setOriginalDescription(null); // OriginalDescription which not exists in Mypay4.
      err.setSerial(rispostaRPT.getFault().getSerial());
    }

    rispostaRP.setFault(err);

    return rispostaRP;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
  public void inviaRPTForModello3(String codAttivarptIdentificativoDominio,
                                    String codAttivarptIdentificativoUnivocoVersamento,
                                    String codAttivarptCodiceContestoPagamento){
    rptRtService.getRPTByIdDominioIuvCdContestoPagamentoForInviaRPT(
        codAttivarptIdentificativoDominio,codAttivarptIdentificativoUnivocoVersamento, codAttivarptCodiceContestoPagamento
    ).ifPresentOrElse( this::inviaRPTForModello3Impl,
        () -> log.info("RptRT not found, or already locked for ente: {} - iuv: {} - ccp: {}", codAttivarptIdentificativoDominio,
        codAttivarptIdentificativoUnivocoVersamento, codAttivarptCodiceContestoPagamento));
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.NESTED)
  public void inviaRPTForModello3ForTask(RptRt mygovRptRt){
    this.inviaRPTForModello3Impl(mygovRptRt);
  }

  private void inviaRPTForModello3Impl(RptRt mygovRptRt){
    //call WS client inviaRpt
    IntestazionePPT header = buildHeaderRPT(mygovRptRt);
    RPT rptObj = buildRPT(mygovRptRt);
    NodoInviaRPT body = buildBodyRPT(rptObj, mygovRptRt);
    try {
      NodoInviaRPTRisposta response = pagamentiTelematiciRPTClient.nodoInviaRPT(
          body,
          header);

      String msg = response.getFault()!=null?
          StringUtils.joinWith(" - ", response.getEsito(), response.getFault().getFaultCode(), response.getFault().getFaultString()) :
          response.getEsito();
      log.info("RESPONSE inviaRPT for mygovRptRtId: {} - esito/fault: {}", mygovRptRt.getMygovRptRtId(), msg);
      //save response
      saveRPTRisposta(response, mygovRptRt.getMygovRptRtId());
    } catch (UnsupportedEncodingException | MalformedURLException e) {
      throw new MyPayException("internal error", e);
    }
  }

}
