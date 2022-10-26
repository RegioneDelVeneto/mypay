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

import it.regioneveneto.mygov.payment.mypay4.dao.AvvisoDigitaleDao;
import it.regioneveneto.mygov.payment.mypay4.dao.EsitoAvvisoDigitaleDao;
import it.regioneveneto.mygov.payment.mypay4.dao.FlussoAvvisoDigitaleDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class AvvisoDigitaleService {

  @Autowired
  private AvvisoDigitaleDao avvisoDigitaleDao;

  @Autowired
  private EsitoAvvisoDigitaleDao esitoAvvisoDigitaleDao;

  @Autowired
  private FlussoAvvisoDigitaleDao flussoAvvisoDigitaleDao;

  @Autowired
  private EnteService enteService;

  @Autowired
  private AnagraficaStatoService anagraficaStatoService;

  @Autowired
  private MessageSource messageSource;

  @Transactional(propagation = Propagation.REQUIRED)
  public int update(AvvisoDigitale avvisoDigitale) {
    return avvisoDigitaleDao.update(avvisoDigitale);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public AvvisoDigitale getById(Long mygovAvvisoDigitaleId) {
    return avvisoDigitaleDao.getById(mygovAvvisoDigitaleId);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public AvvisoDigitale getByIdDominioECodiceAvviso(String idDominio, String codiceAvviso) {
    List<AvvisoDigitale> avvisi = avvisoDigitaleDao.getByIdDominioECodiceAvviso(idDominio, codiceAvviso);
    if (avvisi.size() > 1)
      throw new DataIntegrityViolationException(
          messageSource.getMessage("pa.avvisoDigitale.datiAvvisoDigitaleDuplicati", null, Locale.ITALY));
    else if (avvisi.size() == 0)
      return null;
    else
      return avvisi.get(0);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int changeStateToAnnullato(Dovuto dovuto, Ente ente) {
    int updatedRec = 0;
    if (StringUtils.isNotBlank(dovuto.getCodIuv())) {
      AvvisoDigitale avvisoDigitale = this.getByIdDominioECodiceAvviso(ente.getCodiceFiscaleEnte(),
          "0" + ente.getApplicationCode() + dovuto.getCodIuv());
      if (avvisoDigitale != null) {
        AnagraficaStato anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_AVVISO_DIGITALE_NUOVO,
            Constants.STATO_AVVISO_DIGITALE_TIPO_STATO);
        if (anagraficaStato.getMygovAnagraficaStatoId().equals(avvisoDigitale.getMygovAnagraficaStatoId())) {
          AnagraficaStato statoAnnullato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_AVVISO_DIGITALE_ANNULLATO,
              Constants.STATO_AVVISO_DIGITALE_TIPO_STATO);
          avvisoDigitale.setMygovAnagraficaStatoId(statoAnnullato);
          avvisoDigitale.setDtUltimaModifica(new Date());
          updatedRec  = this.update(avvisoDigitale);
          if (updatedRec != 1)
            throw new MyPayException("Errore interno aggiornamento avvisoDigitale");

        } else {
          log.warn("Stato avviso digitale per codice avviso [ " + "0" + ente.getApplicationCode() + dovuto.getCodIuv()
              + " ] diverso da [ " + Constants.STATO_AVVISO_DIGITALE_NUOVO + " ]");
        }
      }
    }
    return updatedRec;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int changeStateToAnnullato(List<Dovuto> dovuti, Ente ente)  {
    int updatedRec = 0;
    for (Dovuto dovuto: dovuti)
      updatedRec += this.changeStateToAnnullato(dovuto, ente);
    log.info(updatedRec + " avviso_digitale aggiornati con successo.");
    return updatedRec;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AvvisoDigitale addNewAvviso(final String idDominio, final String deAnagBeneficiario, final String idMessaggioRichiesta, final String codAvviso, final String anagraficaPagatore,
      final String tipoIdentificativoUnivoco, final String codiceIdentificativoUnivoco, final Date dataScadenzaPagamento, final Date dataScadenzaAvviso, final BigDecimal importoAvviso,
      final String emailSoggetto, final String cellulareSoggetto, final String descrizionePagamento, final String codIdFlussoAv, final Integer numTentativiInvio,final String tassonomiaAvviso,
      final String codStato, final String deTipoStato, final String ibanAccredito, final String ibanAppoggio, final Integer tipoPagamento, final String tipoOperazione) {

    Assert.notNull(idDominio, 		     "Identificativo dominio nullo");
    Assert.notNull(deAnagBeneficiario,   "Anagrafica Beneficiario nulla");
    Assert.notNull(codAvviso, 		     "Codice Avviso nullo");
    Assert.notNull(importoAvviso, 	     "Importo Avviso nullo");
    Assert.notNull(descrizionePagamento, "Descrizione Pagamento nulla");
    Assert.notNull(codIdFlussoAv, 		 "Identificativo del flusso nullo");
    Assert.notNull(numTentativiInvio, 	 "Numero tentativi invio nullo");
    Assert.notNull(tassonomiaAvviso, 	 "Codice tassonomia nullo");
    Assert.notNull(codStato, 			 "Stato Avviso nullo");
    Assert.notNull(deTipoStato, 		 "Descrizione Tipo Stato nullo");
    Assert.notNull(tipoPagamento, 		 "Tipo pagamento nullo");
    Assert.notNull(tipoOperazione, 		 "Tipo operazione nulla");

    String deAnagBeneficiarioFinal = (deAnagBeneficiario.length() > 35) ? deAnagBeneficiario.substring(0, 35) : deAnagBeneficiario;
    String descrizionePagamentoFinal = (descrizionePagamento.length() > 10000) ? descrizionePagamento.substring(0, 10000) : descrizionePagamento;

    AvvisoDigitale avviso = getNewAvvisoDigitale(idDominio, deAnagBeneficiarioFinal, idMessaggioRichiesta, codAvviso, anagraficaPagatore,
        tipoIdentificativoUnivoco, codiceIdentificativoUnivoco, dataScadenzaPagamento, dataScadenzaAvviso, importoAvviso, emailSoggetto,
        cellulareSoggetto, descrizionePagamentoFinal, codStato, deTipoStato, tassonomiaAvviso, codIdFlussoAv, numTentativiInvio, ibanAccredito,
        ibanAppoggio, tipoPagamento, tipoOperazione);

    long mygovAvvisoDigitaleId = avvisoDigitaleDao.insert(avviso);
    avviso.setMygovAvvisoDigitaleId(mygovAvvisoDigitaleId);
    return avviso;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AvvisoDigitale addNewAvvisoDigitale(final String idDominio, final String anagraficaBeneficiario, final String codiceAvviso,
      final String anagraficaPagatore, final String tipoIdentificativoUnivoco, final String codiceIdentificativoUnivoco,
      final Date dataScadenzaPagamento, final Date dataScadenzaAvviso, final BigDecimal importoAvviso, final String emailSoggetto,
      final String cellulareSoggetto, final String descrizionePagamento, final String codStato, final String deTipoStato,
      final String codIdFlussoAv, final String ibanAccredito, final String ibanAppoggio, final Integer tipoPagamento, final String tipoOperazione) {

    Assert.notNull(idDominio, "Identificativo dominio nullo");
    Assert.notNull(anagraficaBeneficiario, "Anagrafica Beneficiario nulla");
    Assert.notNull(codiceAvviso, "Codice Avviso nullo");
    Assert.notNull(anagraficaPagatore, "Anagrafica Pagatore nulla");
    Assert.notNull(tipoIdentificativoUnivoco, "Tipo Identificativo Univoco nullo");
    Assert.notNull(codiceIdentificativoUnivoco, "Codice Identificativo Univoco nullo");
    Assert.notNull(importoAvviso, "Importo Avviso nullo");
    Assert.notNull(descrizionePagamento, "Descrizione Pagamento nulla");
    Assert.notNull(deTipoStato, "Descrizione Tipo Stato nulla");
    Assert.notNull(codStato, "Stato Avviso nullo");
    Assert.notNull(tipoPagamento, "Tipo pagamento nullo");
    Assert.notNull(tipoOperazione, "Tipo operazione nulla");

    String anagraficaBeneficiarioFinal = anagraficaBeneficiario.length() <= 35 ? anagraficaBeneficiario : anagraficaBeneficiario.substring(0, 35);
    Ente ente = enteService.getEnteByCodFiscale(idDominio);
    Date data = new Date();
    Long progressivo = enteService.callGetEnteTipoProgressivoFunction(ente.getCodIpaEnte(), Constants.TIPO_GENERATORE_AVVISO_DIGITALE, data);
    String idMessaggioRichiesta = DateFormatUtils.format(data, "yyyyMMdd") + "_" + progressivo;

    String newDescrizione = descrizionePagamento.length() <= 140 ? descrizionePagamento : descrizionePagamento.substring(0, 140);

    String codTassonomiaAvvisoTemp = "00";

    AvvisoDigitale avviso = getNewAvvisoDigitale(idDominio, anagraficaBeneficiarioFinal, idMessaggioRichiesta, codiceAvviso, anagraficaPagatore,
        tipoIdentificativoUnivoco, codiceIdentificativoUnivoco, dataScadenzaPagamento, dataScadenzaAvviso, importoAvviso, emailSoggetto,
        cellulareSoggetto, newDescrizione, codStato, deTipoStato, codTassonomiaAvvisoTemp, codIdFlussoAv, null, ibanAccredito, ibanAppoggio,
        tipoPagamento, tipoOperazione);
    long mygovAvvisoDigitaleId = avvisoDigitaleDao.insert(avviso);
    avviso.setMygovAvvisoDigitaleId(mygovAvvisoDigitaleId);
    return avviso;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AvvisoDigitale updateAvviso(final Long idAvvisoDigitale, final String idDominio, final String deAnagBeneficiario, final String idMessaggioRichiesta, final String codAvviso,
      final String anagraficaPagatore, final String tipoIdentificativoUnivoco, final String codiceIdentificativoUnivoco, final Date dataScadenzaPagamento,
      final Date dataScadenzaAvviso, final BigDecimal importoAvviso, final String emailSoggetto, final String cellulareSoggetto, final String descrizionePagamento,
      final String codIdFlussoAv, final Integer numTentativiInvio, final String tassonomiaAvviso, final String deTipoStato, final String codStato,
      final String ibanAccredito, final String ibanAppoggio, final Integer tipoPagamento, final String tipoOperazione) {

    Assert.notNull(idDominio, 		     "Identificativo dominio nullo");
    Assert.notNull(deAnagBeneficiario,   "Anagrafica Beneficiario nulla");
    Assert.notNull(codAvviso, 		     "Codice Avviso nullo");
    Assert.notNull(importoAvviso, 	     "Importo Avviso nullo");
    Assert.notNull(descrizionePagamento, "Descrizione Pagamento nulla");
    Assert.notNull(codIdFlussoAv, 		 "Identificativo del flusso nullo");
    Assert.notNull(numTentativiInvio, 	 "Numero tentativi invio nullo");
    Assert.notNull(tassonomiaAvviso, 	 "Codice tassonomia avviso nullo");
    Assert.notNull(codStato, 			 "Stato Avviso nullo");
    Assert.notNull(deTipoStato, 		 "Descrizione Tipo Stato nullo");
    Assert.notNull(tipoPagamento, 		 "Tipo Pagamento nullo");
    Assert.notNull(tipoOperazione, 		 "Tipo Operazione nulla");

    String deAnagBeneficiarioFinal = (deAnagBeneficiario.length() > 35) ? deAnagBeneficiario.substring(0, 35) : deAnagBeneficiario;
    String descrizionePagamentoFinal = (descrizionePagamento.length() > 10000) ? descrizionePagamento.substring(0, 10000) : descrizionePagamento;

    AvvisoDigitale avviso = getNewAvvisoDigitale(idDominio, deAnagBeneficiarioFinal, idMessaggioRichiesta, codAvviso, anagraficaPagatore,
        tipoIdentificativoUnivoco, codiceIdentificativoUnivoco, dataScadenzaPagamento, dataScadenzaAvviso, importoAvviso, emailSoggetto,
        cellulareSoggetto, descrizionePagamentoFinal, codStato, deTipoStato, tassonomiaAvviso, codIdFlussoAv, numTentativiInvio, ibanAccredito,
        ibanAppoggio, tipoPagamento, tipoOperazione);
    avviso.setMygovAvvisoDigitaleId(idAvvisoDigitale);
    avvisoDigitaleDao.update(avviso);
    return avviso;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AvvisoDigitale updateWSEsito(final Long idAvvisoDigitale, final String codEAdIdDominio, final String codEAdIdMessaggioRichiesta, final String codIdFlussoE, final Integer numAdTentativiInvio) throws DataAccessException {

    log.debug("Update record avviso digitale per mygov_avviso_digitale_id [ " + idAvvisoDigitale + " ] ... START ");

    Assert.notNull(idAvvisoDigitale, 		   "Identificativo interno dell'avviso nullo");
    Assert.notNull(codEAdIdDominio, 		   "Identificativo dominio nullo");
    Assert.notNull(codIdFlussoE, 			   "Identificativo del flusso di esito");
    Assert.notNull(codEAdIdMessaggioRichiesta, "Identificativo messaggio richiesta nullo");

    /* get record */
    AvvisoDigitale avvisoDigitalePersistente = avvisoDigitaleDao.getById(idAvvisoDigitale);

    Assert.notNull(avvisoDigitalePersistente, "Avviso digitale per id [ " + idAvvisoDigitale + " ] non presente");

    /* set value */
    avvisoDigitalePersistente.setCodEAdIdDominio(codEAdIdDominio);
    avvisoDigitalePersistente.setCodEAdIdMessaggioRichiesta(codEAdIdMessaggioRichiesta);
    avvisoDigitalePersistente.setCodIdFlussoE(codIdFlussoE);
    avvisoDigitalePersistente.setNumAdTentativiInvio(numAdTentativiInvio);
    avvisoDigitalePersistente.setDtUltimaModifica(new Date());
    avvisoDigitaleDao.update(avvisoDigitalePersistente);

    log.debug("Update record avviso digitale per mygov_avviso_digitale_id [ " + idAvvisoDigitale + " ] ... END ");

    return avvisoDigitalePersistente;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateAnagraficaStatoDiUnAvvisoDigitaleEsistente(final Long idAvvisoDigitale, final String anagraficaStato, final String deTipoStato){

    Assert.notNull(idAvvisoDigitale, "Identificativo Avviso Digitale nullo");
    Assert.notNull(anagraficaStato, "Anagrafica Stato nulla");
    Assert.notNull(deTipoStato, "Tipo Stato nullo");

    AnagraficaStato anagraficaStatoDomain = anagraficaStatoService.getByCodStatoAndTipoStato(anagraficaStato, deTipoStato);
    Assert.notNull(anagraficaStatoDomain, "Anagrafica stato per stato/tipo [ " + anagraficaStato + "/" + deTipoStato + "] non presente");
    avvisoDigitaleDao.updateState(idAvvisoDigitale, anagraficaStatoDomain.getMygovAnagraficaStatoId());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateTipoOpeAnagStatoAvvDigitaleEsistente(Long idAvvisoDigitale, String anagraficaStato, String deTipoStato, String tipoOperazione){
    Assert.notNull(idAvvisoDigitale, "Identificativo Avviso Digitale nullo");
    Assert.notNull(anagraficaStato, "Anagrafica Stato nulla");
    Assert.notNull(deTipoStato, "Tipo Stato nullo");
    Assert.notNull(tipoOperazione, "Tipo Operazione nulla");

    AnagraficaStato anagraficaStatoDomain = anagraficaStatoService.getByCodStatoAndTipoStato(anagraficaStato, deTipoStato);
    Assert.notNull(anagraficaStatoDomain, "Anagrafica stato per stato/tipo [ " + anagraficaStato + "/" + deTipoStato + "] non presente");
    avvisoDigitaleDao.updateStateOpType(idAvvisoDigitale, anagraficaStatoDomain.getMygovAnagraficaStatoId(), tipoOperazione);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public FlussoAvvisoDigitale selectFlussoAvvisoForInsert(String codFadIdDominio, String tipoStato, String anagraficaStato, String codFadIdFlusso, String codFadTipoFlusso) throws DataAccessException {
    log.debug("Verifica che il flusso con ID [ " + codFadIdFlusso + " ] esista ... IN CORSO ");

    List<FlussoAvvisoDigitale> flussi = flussoAvvisoDigitaleDao.getByCodFadIdFlusso(codFadIdFlusso);

    /*
     * Sono stati trovati più di un record per l'id flusso passato come parametro d'ingresso.
     */
    if (flussi.size() > 1){
      log.error("ERRORE: Trovati n° " + flussi.size() + " record con ID FLUSSO " + codFadIdFlusso);
      throw new DataIntegrityViolationException("ERRORE: Trovati n° " + flussi.size() + " record con ID FLUSSO " + codFadIdFlusso);
    }

    /*
     * Il flusso non esiste, lo inserisco.
     */
    if (CollectionUtils.isEmpty(flussi)) {
      FlussoAvvisoDigitale flusso = getNewFlussoAvvisoDigitale(tipoStato, anagraficaStato, codFadIdDominio, codFadIdFlusso, codFadTipoFlusso);
      long mygovFlussoAvvisoDigitaleId = flussoAvvisoDigitaleDao.insert(flusso);
      flusso.setMygovFlussoAvvisoDigitaleId(mygovFlussoAvvisoDigitaleId);
      return flusso;
    } else
      return flussi.get(0);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public EsitoAvvisoDigitale insertEsitoAvvisoDigitale(AvvisoDigitale avvisoDigitale, int numEAdEsitoAvTipoCanaleEsito, String codEAdEsitoAvIdCanaleEsito,
                                Date dtEAdEsitoAvDataEsito, int numEAdEsitoAvCodiceEsito, String deEAdEsitoAvDescEsito ) {
    log.debug("Insert record di esito per Avviso ID [ " + avvisoDigitale.getMygovAvvisoDigitaleId() + " ] ... START ");

    Assert.notNull(avvisoDigitale, 				 "Avviso Digitale nullo");
    Assert.notNull(numEAdEsitoAvTipoCanaleEsito, "Tipologia di canale usato nullo");
    Assert.notNull(dtEAdEsitoAvDataEsito, 		 "Data di produzione dell'esito nullo");
    Assert.notNull(numEAdEsitoAvCodiceEsito, 	 "Esito dell'invio nullo");

    EsitoAvvisoDigitale esitoAvvisoDigitale = new EsitoAvvisoDigitale();
    esitoAvvisoDigitale.setMygovAvvisoDigitaleId(avvisoDigitale);
    esitoAvvisoDigitale.setNumEAdEsitoAvCodiceEsito(numEAdEsitoAvCodiceEsito);
    esitoAvvisoDigitale.setCodEAdEsitoAvIdCanaleEsito(codEAdEsitoAvIdCanaleEsito);
    esitoAvvisoDigitale.setDtEAdEsitoAvDataEsito(dtEAdEsitoAvDataEsito);
    esitoAvvisoDigitale.setNumEAdEsitoAvCodiceEsito(numEAdEsitoAvCodiceEsito);
    esitoAvvisoDigitale.setDeEAdEsitoAvDescEsito(deEAdEsitoAvDescEsito);

    long mygovEsitoAvvisoDigitaleId = esitoAvvisoDigitaleDao.insert(esitoAvvisoDigitale);
    esitoAvvisoDigitale.setMygovEsitoAvvisoDigitaleId(mygovEsitoAvvisoDigitaleId);
    return esitoAvvisoDigitale;
  }

  private AvvisoDigitale getNewAvvisoDigitale(final String idDominio, final String anagraficaBeneficiario, final String idMessaggioRichiesta,
                                              final String codiceAvviso, final String anagraficaPagatore, final String tipoIdentificativoUnivoco, final String codiceIdentificativoUnivoco,
                                              final Date dataScadenzaPagamento, final Date dataScadenzaAvviso, final BigDecimal importoAvviso, final String emailSoggetto,
                                              final String cellulareSoggetto, final String descrizionePagamento, final String anagraficaStato, final String deTipoStato,
                                              final String codTassonomiaAvviso, final String codIdFlussoAv, final Integer numTentativiInvio, final String ibanAccredito,
                                              final String ibanAppoggio, final Integer tipoPagamento, final String tipoOperazione) {

    AvvisoDigitale avviso = new AvvisoDigitale();

    AnagraficaStato anagraficaStatoDomain = anagraficaStatoService.getByCodStatoAndTipoStato(anagraficaStato, deTipoStato);
    Assert.notNull(anagraficaStatoDomain, "Anagrafica stato per stato [ " + anagraficaStato + " ] non presente");

    avviso.setCodAdIdDominio(idDominio);
    avviso.setDeAdAnagBeneficiario(anagraficaBeneficiario);
    avviso.setCodAdIdMessaggioRichiesta(idMessaggioRichiesta);
    avviso.setCodAdCodAvviso(codiceAvviso);
    avviso.setDeAdSogPagAnagPagatore(anagraficaPagatore);
    avviso.setCodAdSogPagIdUnivPagTipoIdUniv(tipoIdentificativoUnivoco);
    avviso.setCodAdSogPagIdUnivPagCodIdUniv(codiceIdentificativoUnivoco);
    avviso.setDtAdDataScadenzaPagamento(dataScadenzaPagamento);
    avviso.setDtAdDataScadenzaAvviso(dataScadenzaAvviso);
    avviso.setNumAdImportoAvviso(importoAvviso);
    avviso.setDeAdEmailSoggetto(emailSoggetto);
    avviso.setDeAdCellulareSoggetto(cellulareSoggetto);
    avviso.setDeAdDescPagamento(descrizionePagamento);
    avviso.setMygovAnagraficaStatoId(anagraficaStatoDomain);
    avviso.setCodTassonomiaAvviso(codTassonomiaAvviso);
    avviso.setCodIdFlussoAv(codIdFlussoAv);
    avviso.setNumAdTentativiInvio(numTentativiInvio);
    avviso.setDatiSingVersIbanAccredito(ibanAccredito);
    avviso.setDatiSingVersIbanAppoggio(ibanAppoggio);
    avviso.setTipoOperazione(tipoOperazione);
    avviso.setTipoPagamento(tipoPagamento);

    Date data = new Date();
    avviso.setDtCreazione(data);
    avviso.setDtUltimaModifica(data);
    avviso.setVersion(0);
    return avviso;
  }

  private FlussoAvvisoDigitale getNewFlussoAvvisoDigitale(String tipoStato, String codStato, String codFadIdDominio, String codFadIdFlusso, String codFadTipoFlusso) throws DataAccessException {

    FlussoAvvisoDigitale flusso = new FlussoAvvisoDigitale();

    flusso.setVersion(0);
    AnagraficaStato anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(codStato, tipoStato);
    Assert.notNull(anagraficaStato, "Anagrafica stato per stato [ " + codStato + " ] non presente");
    flusso.setMygovAnagraficaStatoId(anagraficaStato);
    flusso.setCodFadIdDominio(codFadIdDominio);
    flusso.setCodFadIdFlusso(codFadIdFlusso);
    flusso.setCodFadTipoFlusso(codFadTipoFlusso);
    flusso.setDeFadFilePath("n.d.");
    flusso.setDeFadFilename("n.d.");

    Date data = new Date();
    flusso.setDtCreazione(data);
    flusso.setDtUltimaModifica(data);
    return flusso;
  }
}
