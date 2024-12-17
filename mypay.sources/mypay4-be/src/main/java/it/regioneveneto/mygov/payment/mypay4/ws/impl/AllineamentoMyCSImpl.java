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
package it.regioneveneto.mygov.payment.mypay4.ws.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.regioneveneto.mygov.payment.mypay4.bo.IdentificativoUnivocoEnte;
import it.regioneveneto.mygov.payment.mypay4.controller.FlussoController;
import it.regioneveneto.mygov.payment.mypay4.controller.MyBoxController;
import it.regioneveneto.mygov.payment.mypay4.dao.FlussoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.service.*;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.storage.ContentStorage;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.AllineamentoMyCS;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.AllineamentoMyCS;
import it.regioneveneto.mygov.payment.mypay4.ws.util.ManageWsFault;
import it.veneto.regione.pagamenti.ente.*;
import it.veneto.regione.pagamenti.ente.ppthead.IntestazionePPT;
import it.veneto.regione.schemas._2012.pagamenti.ente.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.util.UriComponentsBuilder;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.*;
import static java.util.stream.Collectors.*;

@Service("AllineamentoMyCSImpl")
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class AllineamentoMyCSImpl implements AllineamentoMyCS {

  @Autowired
  private EnteService enteService;

  @Autowired
  private EnteFunzionalitaService enteFunzionalitaService;

  @Autowired
  private UtenteService utenteService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private OperatoreService operatoreService;
  @Autowired
  private OperatoreEnteTipoDovutoService operatoreEnteTipoDovutoService;

  @Value("${ws.paaMCSAllineamento.password}")
  private String wsPaaMCSAllineamentoPassword;

  @Override
  public void paaMCSAllineamento(String passwordAllineamento, Date dateFromAllineamento, Holder<FaultBean> fault, Holder<String> jsonAllineamento) {

    log.info("START paaMCSAllineamento");

    StopWatch swInvia = new StopWatch("WS paaMCSAllineamento :: paaMCSAllineamento");
    CtAllineamento ctAllineamento = new CtAllineamento();
    List<CtEnte> enti = new ArrayList<CtEnte>();
    List<CtEnteFunzionalita> entiFunzionalita = new ArrayList<CtEnteFunzionalita>();
    List<CtEnteTipoDovuto> enteTipiDovuto = new ArrayList<CtEnteTipoDovuto>();
    List<CtOperatore> operatori = new ArrayList<CtOperatore>();
    List<CtOperatoreEnteTipoDovuto> operatoriEnteTipiDovuto = new ArrayList<CtOperatoreEnteTipoDovuto>();
    List<CtUtente> utenti = new ArrayList<CtUtente>();

    // VERIFICA PASSWORD passwordAllineamento
    if (passwordAllineamento == null || !wsPaaMCSAllineamentoPassword.equals(passwordAllineamento)) {
      log.error("paaMCSAllineamento: Password per l'allineamento non valida");
      return;
    }

    // VERIFICA DATA DI ESTRAZIONE dateFromAllineamento
    /*
    Date dateFrom = null;
    if (dateFromAllineamento != null)
      dateFrom = dateFromAllineamento != null ? new java.sql.Date((dateFromAllineamento.toGregorianCalendar().getTime()).getTime()) : null;
    */

    //Recupero Enti
    swInvia.start();
    List<Ente> entiL = enteService.getAllEnti();
    ctAllineamento.getEntis().addAll(mappingEnti(entiL));
    log.info("Oggi sono presenti " + (ctAllineamento.getEntis()).size() + " enti...");
    swInvia.stop();
    log.info("Tempo gestione Ente: " + swInvia.getTotalTimeSeconds());

    //Recupero Funzionalita
    //N.B. non può essere gestita la data di estrazione
    swInvia.start();
    List<EnteFunzionalita> enteFunzionalitas = enteFunzionalitaService.getAll();
    ctAllineamento.getEntiFunzionalitas().addAll(mappingFunzionalita(enteFunzionalitas));
    log.info("Oggi sono presenti " + (ctAllineamento.getEntiFunzionalitas()).size() + " funzionalità");
    swInvia.stop();
    log.info("Tempo gestione EnteFunzionalita: " + swInvia.getTotalTimeSeconds());

    //Recupero EnteTipoDovuto
    //N.B. non può essere gestita la data di estrazione
    swInvia.start();
    List<EnteTipoDovuto> enteTipoDovutos = enteTipoDovutoService.getAll();
    ctAllineamento.getEnteTipiDovutos().addAll(mappingEnteTipoDovuto(enteTipoDovutos));
    log.info("Oggi sono presenti " + (ctAllineamento.getEnteTipiDovutos()).size() + " ente tipo dovuto");
    swInvia.stop();
    log.info("Tempo gestione EnteTipoDovuto: " + swInvia.getTotalTimeSeconds());

    //Recupero Operatori
    //N.B. non può essere gestita la data di estrazione
    swInvia.start();
    List<CtOperatore> ctOperatoreList = new ArrayList<CtOperatore>();
    try {
      List<Operatore> operatoriEnteL = operatoreService.getAll();
      ctOperatoreList.addAll(mappingOperatori(operatoriEnteL));
    } catch (Exception e) {
      log.error("ERRORE RECUPERO OPERATORI - Errore: " + e);
    }
    ctAllineamento.getOperatoris().addAll(ctOperatoreList);
    log.info("Oggi sono presenti " + (ctAllineamento.getOperatoris()).size() + " operatori...");
    swInvia.stop();
    log.info("Tempo gestione Operatori: " + swInvia.getTotalTimeSeconds());

    //Recupero Operatori Ente TipoDovuto
    //N.B. non può essere gestita la data di estrazione
    swInvia.start();
    List<CtOperatoreEnteTipoDovuto> ctOperatoreEnteTipoDovutoList = new ArrayList<CtOperatoreEnteTipoDovuto>();
    try {
      List<OperatoreEnteTipoDovuto> oetdL = operatoreEnteTipoDovutoService.getAll();
      ctOperatoreEnteTipoDovutoList.addAll(mappingOperatoriEnteTipoDovuto(oetdL));
    } catch (Exception e) {
      log.error("ERRORE RECUPERO OPERATORI TIPO DOVUTO - Errore: " + e);
    }
    ctAllineamento.getOperatoriEnteTipiDovutos().addAll(ctOperatoreEnteTipoDovutoList);
    log.info("Oggi sono presenti " + (ctAllineamento.getOperatoriEnteTipiDovutos()).size() + " operatoriEnteTipiDovuto...");
    swInvia.stop();
    log.info("Tempo gestione Operatori Ente TipoDovuto: " + swInvia.getTotalTimeSeconds());

    //Recupero Utenti
    //N.B. non può essere gestita la data di estrazione
    swInvia.start();
    List<CtUtente> ctUtenteList = new ArrayList<CtUtente>();
    List<Utente> utentiL = utenteService.getAll();
    ctUtenteList.addAll(mappingUtenti(utentiL));
    ctAllineamento.getUtentis().addAll(ctUtenteList);
    log.info("Oggi sono presenti " + (ctAllineamento.getUtentis()).size() + " utenti...");
    swInvia.stop();
    log.info("Tempo gestione Utenti: " + swInvia.getTotalTimeSeconds());


    String json = null;
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    try {

      json = ow.writeValueAsString(ctAllineamento);
      log.debug("JsonUtenti:" + json);
    } catch (Exception e) {
      log.error("ERRORE CREAZIONE JSON ALLINEAMENTO MYCALCOLOSANZIONI - Errore: " + e);
    }
    jsonAllineamento.value = json;

    log.info("STOP paaMCSAllineamento");
  }

  protected List<CtEnte> mappingEnti(List<Ente> enteList){
    List<CtEnte> entiRes = new ArrayList<CtEnte>();
    for(Ente ente : enteList) {
      CtEnte enteRes = new CtEnte();
      enteRes.setCodIpaEnte(ente.getCodIpaEnte());
      enteRes.setCodiceFiscaleEnte(ente.getCodiceFiscaleEnte());
      enteRes.setDeNomeEnte(ente.getDeNomeEnte());
      enteRes.setEmailAmministratore(ente.getEmailAmministratore());
//			enteRes.setDtCreazione(ente.getDtCreazione().toString());
//			enteRes.setDtUltimaModifica(null);
      enteRes.setCodRpDatiVersTipoVersamento(ente.getCodRpDatiVersTipoVersamento());
      BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa = ente.getNumRpDatiVersDatiSingVersCommissioneCaricoPa();
      enteRes.setNumRpDatiVersDatiSingVersCommissioneCaricoPa(numRpDatiVersDatiSingVersCommissioneCaricoPa!=null?
              numRpDatiVersDatiSingVersCommissioneCaricoPa.longValue():new Long(0));
      enteRes.setCodRpDatiVersDatiSingVersIbanAccredito(ente.getCodRpDatiVersDatiSingVersIbanAccredito());
      enteRes.setCodRpDatiVersDatiSingVersBicAccredito(ente.getCodRpDatiVersDatiSingVersBicAccredito());
      enteRes.setCodRpDatiVersDatiSingVersIbanAppoggio(ente.getCodRpDatiVersDatiSingVersIbanAppoggio());
      enteRes.setCodRpDatiVersDatiSingVersBicAppoggio(ente.getCodRpDatiVersDatiSingVersBicAppoggio());
      enteRes.setMyboxClientKey(ente.getMyboxClientKey());
      enteRes.setMyboxClientSecret(ente.getMyboxClientSecret());
      enteRes.setEnteSilInviaRispostaPagamentoUrl(ente.getEnteSilInviaRispostaPagamentoUrl());
      enteRes.setCodGlobalLocationNumber(ente.getCodGlobalLocationNumber());
      enteRes.setDePassword(ente.getDePassword());
      enteRes.setCodRpDatiVersDatiSingVersBicAccreditoSeller(ente.getCodRpDatiVersDatiSingVersBicAccreditoSeller());
      enteRes.setDeRpEnteBenefDenominazioneBeneficiario(ente.getDeRpEnteBenefDenominazioneBeneficiario());
      enteRes.setDeRpEnteBenefIndirizzoBeneficiario(ente.getDeRpEnteBenefIndirizzoBeneficiario());
      enteRes.setDeRpEnteBenefCivicoBeneficiario(ente.getDeRpEnteBenefCivicoBeneficiario());
      enteRes.setCodRpEnteBenefCapBeneficiario(ente.getCodRpEnteBenefCapBeneficiario());
      enteRes.setDeRpEnteBenefLocalitaBeneficiario(ente.getDeRpEnteBenefLocalitaBeneficiario());
      enteRes.setDeRpEnteBenefProvinciaBeneficiario(ente.getDeRpEnteBenefProvinciaBeneficiario());
      enteRes.setCodRpEnteBenefNazioneBeneficiario(ente.getCodRpEnteBenefNazioneBeneficiario());
      enteRes.setDeRpEnteBenefTelefonoBeneficiario(ente.getDeRpEnteBenefTelefonoBeneficiario());
      enteRes.setDeRpEnteBenefSitoWebBeneficiario(ente.getDeRpEnteBenefSitoWebBeneficiario());
      enteRes.setDeRpEnteBenefEmailBeneficiario(ente.getDeRpEnteBenefEmailBeneficiario());
      enteRes.setApplicationCode(ente.getApplicationCode());
      enteRes.setCodCodiceInterbancarioCbill(ente.getCodCodiceInterbancarioCbill());
      enteRes.setDeInformazioniEnte(ente.getDeInformazioniEnte());
      enteRes.setDeAutorizzazione(ente.getDeAutorizzazione());
      enteRes.setCodStatoEnte(ente.getCdStatoEnte().getCodStato());
      enteRes.setDeTipoStato(ente.getCdStatoEnte().getDeTipoStato());
      enteRes.setDeUrlEsterniAttiva(ente.getDeUrlEsterniAttiva());
      enteRes.setLinguaAggiuntiva(ente.getLinguaAggiuntiva());
      enteRes.setDeLogoEnte(ente.getDeLogoEnte());
      entiRes.add(enteRes);
    }
    return entiRes;
  }

  protected List<CtEnteFunzionalita> mappingFunzionalita(List<EnteFunzionalita> enteFunzionalitaList){
    List<CtEnteFunzionalita> ctEnteFunzionalitaRes = new ArrayList<CtEnteFunzionalita>();
    for(EnteFunzionalita enteFunzionalita : enteFunzionalitaList) {
      CtEnteFunzionalita ctEnteFunzionalita = new CtEnteFunzionalita();
      ctEnteFunzionalita.setCodIpaEnte(enteFunzionalita.getCodIpaEnte());
      ctEnteFunzionalita.setCodFunzionalita(enteFunzionalita.getCodFunzionalita());
      ctEnteFunzionalita.setFlgAttivo(enteFunzionalita.isFlgAttivo());
      ctEnteFunzionalitaRes.add(ctEnteFunzionalita);
    }
    return ctEnteFunzionalitaRes;
  }

  protected List<CtEnteTipoDovuto> mappingEnteTipoDovuto(List<EnteTipoDovuto> enteTipoDovutoList){
    List<CtEnteTipoDovuto> ctEnteTipoDovutoRes = new ArrayList<CtEnteTipoDovuto>();
    for(EnteTipoDovuto enteTipoDovuto : enteTipoDovutoList) {
      CtEnteTipoDovuto ctEnteTipoDovuto = new CtEnteTipoDovuto();
      ctEnteTipoDovuto.setCodIpaEnte(enteTipoDovuto.getMygovEnteId().getCodIpaEnte());
      ctEnteTipoDovuto.setCodTipo(enteTipoDovuto.getCodTipo());
      ctEnteTipoDovuto.setDeTipo(enteTipoDovuto.getDeTipo());
      ctEnteTipoDovuto.setIbanAccreditoPi(enteTipoDovuto.getIbanAccreditoPi());
      ctEnteTipoDovuto.setBicAccreditoPi(enteTipoDovuto.getBicAccreditoPi());
      ctEnteTipoDovuto.setIbanAppoggioPi(enteTipoDovuto.getIbanAppoggioPi());
      ctEnteTipoDovuto.setBicAppoggioPi(enteTipoDovuto.getBicAppoggioPi());
      ctEnteTipoDovuto.setIbanAccreditoPsp(enteTipoDovuto.getIbanAccreditoPsp());
      ctEnteTipoDovuto.setBicAccreditoPsp(enteTipoDovuto.getBicAccreditoPsp());
      ctEnteTipoDovuto.setIbanAppoggioPsp(enteTipoDovuto.getIbanAppoggioPsp());
      ctEnteTipoDovuto.setBicAppoggioPsp(enteTipoDovuto.getBicAppoggioPsp());
      ctEnteTipoDovuto.setCodContoCorrentePostale(enteTipoDovuto.getCodContoCorrentePostale());
      ctEnteTipoDovuto.setCodXsdCausale(enteTipoDovuto.getCodXsdCausale());
      ctEnteTipoDovuto.setBicAccreditoPiSeller(enteTipoDovuto.isBicAccreditoPiSeller());
      ctEnteTipoDovuto.setBicAccreditoPspSeller(enteTipoDovuto.isBicAccreditoPspSeller());
      ctEnteTipoDovuto.setSpontaneo(enteTipoDovuto.isSpontaneo());
      BigDecimal importo = enteTipoDovuto.getImporto();
      ctEnteTipoDovuto.setImporto(importo!=null?importo.longValue(): null);
      ctEnteTipoDovuto.setDeUrlPagamentoDovuto(enteTipoDovuto.getDeUrlPagamentoDovuto());
      ctEnteTipoDovuto.setDeBilancioDefault(enteTipoDovuto.getDeBilancioDefault());
      ctEnteTipoDovuto.setFlgCfAnonimo(enteTipoDovuto.isFlgCfAnonimo());
      ctEnteTipoDovuto.setFlgScadenzaObbligatoria(enteTipoDovuto.isFlgScadenzaObbligatoria());
      ctEnteTipoDovuto.setFlgStampaDataScadenza(enteTipoDovuto.isFlgStampaDataScadenza());
      ctEnteTipoDovuto.setDeIntestatarioCcPostale(enteTipoDovuto.getDeIntestatarioCcPostale());
      ctEnteTipoDovuto.setDeSettoreEnte(enteTipoDovuto.getDeSettoreEnte());
      ctEnteTipoDovuto.setFlgNotificaIo(enteTipoDovuto.isFlgNotificaIo());
      ctEnteTipoDovuto.setFlgNotificaEsitoPush(enteTipoDovuto.isFlgNotificaEsitoPush());
      ctEnteTipoDovuto.setMaxTentativiInoltroEsito(enteTipoDovuto.getMaxTentativiInoltroEsito());
      ctEnteTipoDovuto.setCodiceContestoPagamento(enteTipoDovuto.getCodiceContestoPagamento());
      ctEnteTipoDovuto.setFlgDisabilitaStampaAvviso(enteTipoDovuto.isFlgDisabilitaStampaAvviso());
      ctEnteTipoDovuto.setFlgAttivo(enteTipoDovuto.isFlgAttivo());
      ctEnteTipoDovuto.setMacroarea(enteTipoDovuto.getMacroArea());
      ctEnteTipoDovuto.setTipoServizio(enteTipoDovuto.getTipoServizio());
      ctEnteTipoDovuto.setMotivoRisc(enteTipoDovuto.getMotivoRiscossione());
      ctEnteTipoDovuto.setCodTassonomico(enteTipoDovuto.getCodTassonomico());
      ctEnteTipoDovuto.setTokenServiceGio("");
      ctEnteTipoDovuto.setFlagServiceGio(false);
      ctEnteTipoDovuto.setGiorniNotificaScadenza(null);
      ctEnteTipoDovuto.setFlagSensibile(false);
      ctEnteTipoDovuto.setFlagOmettiInvioCreazione(false);
      ctEnteTipoDovutoRes.add(ctEnteTipoDovuto);
    }
    return ctEnteTipoDovutoRes;
  }

  protected List<CtOperatore> mappingOperatori(List<Operatore> operatoreList){
    List<CtOperatore> operatoriRes = new ArrayList<CtOperatore>();
    for(Operatore operatore : operatoreList) {
      CtOperatore operatoreRes = new CtOperatore();
      operatoreRes.setCodIpaEnte(operatore.getCodIpaEnte());
      operatoreRes.setRuolo(operatore.getRuolo());
      operatoreRes.setCodFedUserId(operatore.getCodFedUserId());
      operatoriRes.add(operatoreRes);
    }
    return operatoriRes;
  }

  protected List<CtOperatoreEnteTipoDovuto> mappingOperatoriEnteTipoDovuto(List<OperatoreEnteTipoDovuto> oetds){

    List<CtOperatoreEnteTipoDovuto> oetdsRes = new ArrayList<CtOperatoreEnteTipoDovuto>();
    try {
      for(OperatoreEnteTipoDovuto oetd : oetds) {

        CtOperatoreEnteTipoDovuto oetdRes = new CtOperatoreEnteTipoDovuto();

        if (oetd.getMygovEnteTipoDovutoId() != null && oetd.getMygovOperatoreId().getCodIpaEnte() != null
        ) {
          oetdRes.setCodTipoDovuto(oetd.getMygovEnteTipoDovutoId().getCodTipo());
          oetdRes.setCodIpaEnte(oetd.getMygovOperatoreId().getCodIpaEnte());
          oetdRes.setCodFedUserId(oetd.getMygovOperatoreId().getCodFedUserId());
          oetdRes.setFlgAttivo(oetd.getMygovEnteTipoDovutoId().isFlgAttivo());
        }
        oetdsRes.add(oetdRes);
      }
    }catch(Exception e) {
      log.error("ERRORE RECUPERO OPERATORI/OPERATORI TIPO DOVUTO - Errore: "+e);
    }
    return oetdsRes;
  }

  protected List<CtUtente> mappingUtenti(List<Utente> utenteList){
    List<CtUtente> ctUtenteRes = new ArrayList<CtUtente>();
    for(Utente utente : utenteList) {
      CtUtente ctUtente = new CtUtente();
      ctUtente.setVersion(utente.getVersion());
      ctUtente.setCodFedUserId(utente.getCodFedUserId());
      ctUtente.setCodCodiceFiscaleUtente(utente.getCodCodiceFiscaleUtente());
      ctUtente.setFlgFedAuthorized(utente.isFlgFedAuthorized());
      ctUtente.setDeEmailAddress(utente.getDeEmailAddress());
      ctUtente.setDeFirstname(utente.getDeFirstname());
      ctUtente.setDeLastname(utente.getDeLastname());
      ctUtente.setDeFedLegalEntity( utente.getDeFedLegalEntity() );
      ctUtente.setIndirizzo(utente.getIndirizzo());
      ctUtente.setCivico(utente.getCivico());
      ctUtente.setCap(utente.getCap());
      ctUtenteRes.add(ctUtente);
    }
    return ctUtenteRes;
  }
}
