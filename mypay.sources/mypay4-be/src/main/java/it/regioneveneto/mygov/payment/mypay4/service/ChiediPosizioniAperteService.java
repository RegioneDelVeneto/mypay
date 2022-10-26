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

import it.regioneveneto.mygov.payment.mypay4.model.DatiMarcaBolloDigitale;
import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.util.EnumUtils;
import it.veneto.regione.pagamenti.ente.PaaSILChiediPosizioniAperte;
import it.veneto.regione.pagamenti.ente.PaaSILChiediPosizioniAperteRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILPosizioniAperte;
import it.veneto.regione.schemas._2012.pagamenti.ente.Bilancio;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtDatiMarcaBolloDigitale;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtDatiSingoloVersamentoDovuti;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtDatiVersamentoDovuti;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtIdentificativoUnivocoPersonaFG;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtSoggettoPagatore;
import it.veneto.regione.schemas._2012.pagamenti.ente.Dovuti;
import it.veneto.regione.schemas._2012.pagamenti.ente.StTipoIdentificativoUnivocoPersFG;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.CODE_PAA_ENTE_NON_VALIDO;

@Service
@Slf4j
public class ChiediPosizioniAperteService {

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;
  @Value("${pa.deRpVersioneOggetto}")
  private String deRpVersioneOggetto;

  @Autowired
  EnteService enteService;
  @Autowired
  DovutoService dovutoService;
  @Autowired
  DatiMarcaBolloDigitaleService datiMarcaBolloDigitaleService;
  @Autowired
  JAXBTransformService jaxbTransformService;
  @Autowired
  LandingService landingService;

  public PaaSILChiediPosizioniAperteRisposta paaSILChiediPosizioniAperte(PaaSILChiediPosizioniAperte bodyRequest) {
    log.info("Executing operation paaSILChiediPosizioniAperte");
    PaaSILChiediPosizioniAperteRisposta response = new PaaSILChiediPosizioniAperteRisposta();
    String codIpaEnte = bodyRequest.getCodIpaEnte();
    CtIdentificativoUnivocoPersonaFG identificativoUnivocoPersonaFG = bodyRequest.getIdentificativoUnivocoPersonaFG();
    Ente ente;
    if (StringUtils.isNotBlank(codIpaEnte)) {
      ente = enteService.getEnteByCodIpa(codIpaEnte);
      if (ente == null) {
        String msg = String.format("codice IPA Ente [%s] non valido", codIpaEnte);
        log.error("paaSILChiediPosizioniAperte: %s", msg);
        response.setFault(VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_ENTE_NON_VALIDO, msg, null));
        return response;
      }
    }
    response.setFault(VerificationUtils.checkIdentificativoUnivocoPersonaFG(codIpaEnte, identificativoUnivocoPersonaFG));
    if (response.getFault() != null)
      return response;
    String kindPerson = identificativoUnivocoPersonaFG.getTipoIdentificativoUnivoco().value();
    String fiscalCode = identificativoUnivocoPersonaFG.getCodiceIdentificativoUnivoco();

    try {
      Function<Dovuto, PaaSILPosizioniAperte> mapDovutiToPosizioniAperte = dovuto -> {
        if(StringUtils.isBlank(dovuto.getIdSession())) {
          dovutoService.addIdSession(dovuto.getMygovDovutoId(), Utilities.getRandomicUUID());
        }
        PaaSILPosizioniAperte posizioniAperte = new PaaSILPosizioniAperte();
        posizioniAperte.setCodIpaEnte(dovuto.getNestedEnte().getCodIpaEnte().trim());
        posizioniAperte.setDeNomeEnte(dovuto.getNestedEnte().getDeNomeEnte().trim());
        posizioniAperte.setUrlPagamento(landingService.getUrlChiediPosizioniAperte(dovuto.getIdSession()));
        Dovuti dovutiDocument = mapDebitoToNewDovuti(dovuto);
        byte[] xml = jaxbTransformService.marshallingAsBytes(dovutiDocument, Dovuti.class);
        DataSource dataSource = new ByteArrayDataSource(xml, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        DataHandler dataHandler = new DataHandler(dataSource);
        posizioniAperte.setDovuti(dataHandler);
        return posizioniAperte;
      };
      List<PaaSILPosizioniAperte> posizioniAperte = dovutoService.getUnpaidByEnteIdUnivocoPersona(kindPerson, fiscalCode, codIpaEnte)
          .stream()
          .map(mapDovutiToPosizioniAperte)
          .collect(Collectors.toUnmodifiableList());
      response.getPaaSILPosizioniApertes().addAll(posizioniAperte);

      return response;
    } catch (Exception e) {
      log.error("paaSILChiediPosizioniAperte error: [{}]", e.getMessage());
      throw new RuntimeException(String.format("paaSILChiediPosizioniAperte error: [{}]", e.getMessage()));
    }
  }

  private Dovuti mapDebitoToNewDovuti(Dovuto dovuto) {
    Dovuti ctDovuti = new Dovuti();

    ctDovuti.setVersioneOggetto(deRpVersioneOggetto);

    CtIdentificativoUnivocoPersonaFG ctIdentificativoUnivocoPersonaFG = new CtIdentificativoUnivocoPersonaFG();
    ctIdentificativoUnivocoPersonaFG.setCodiceIdentificativoUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
    String codRpSoggPagIdUnivPagTipoIdUnivoco = String.valueOf(dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco());
    ctIdentificativoUnivocoPersonaFG.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.valueOf(codRpSoggPagIdUnivPagTipoIdUnivoco));

    CtSoggettoPagatore soggPagatore = new CtSoggettoPagatore();
    soggPagatore.setIdentificativoUnivocoPagatore(ctIdentificativoUnivocoPersonaFG);
    soggPagatore.setAnagraficaPagatore(dovuto.getDeRpSoggPagAnagraficaPagatore());
    Optional.ofNullable(dovuto.getDeRpSoggPagIndirizzoPagatore()).ifPresent(soggPagatore::setIndirizzoPagatore);
    Optional.ofNullable(dovuto.getDeRpSoggPagCivicoPagatore()).ifPresent(soggPagatore::setCivicoPagatore);
    Optional.ofNullable(dovuto.getCodRpSoggPagCapPagatore()).ifPresent(soggPagatore::setCapPagatore);
    Optional.ofNullable(dovuto.getDeRpSoggPagLocalitaPagatore()).ifPresent(soggPagatore::setLocalitaPagatore);
    Optional.ofNullable(dovuto.getDeRpSoggPagProvinciaPagatore()).ifPresent(soggPagatore::setProvinciaPagatore);
    Optional.ofNullable(dovuto.getCodRpSoggPagNazionePagatore()).ifPresent(soggPagatore::setNazionePagatore);
    Optional.ofNullable(dovuto.getDeRpSoggPagEmailPagatore()).ifPresent(soggPagatore::setEMailPagatore);
    ctDovuti.setSoggettoPagatore(soggPagatore);

    CtDatiVersamentoDovuti datiVersamentoDovuti = new CtDatiVersamentoDovuti();
    datiVersamentoDovuti.setTipoVersamento(dovuto.getCodRpDatiVersTipoVersamento());
    Optional.ofNullable(dovuto.getCodIuv()).ifPresent(datiVersamentoDovuti::setIdentificativoUnivocoVersamento);
    addDatoSingoloVersamentoInCtDatiVersamentoDovuti(dovuto, datiVersamentoDovuti);

    ctDovuti.setDatiVersamento(datiVersamentoDovuti);
    return ctDovuti;
  }

  private void addDatoSingoloVersamentoInCtDatiVersamentoDovuti(Dovuto dovuto, CtDatiVersamentoDovuti datiVersamentoDovuti) {

    CtDatiSingoloVersamentoDovuti datiSingoloVersamento = new CtDatiSingoloVersamentoDovuti();
    datiVersamentoDovuti.getDatiSingoloVersamentos().add(datiSingoloVersamento);

    datiSingoloVersamento.setIdentificativoUnivocoDovuto(dovuto.getCodIud());
    datiSingoloVersamento.setImportoSingoloVersamento(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento());

    Optional.ofNullable(dovuto.getNumRpDatiVersDatiSingVersCommissioneCaricoPa()).ifPresent(datiSingoloVersamento::setCommissioneCaricoPA);

    datiSingoloVersamento.setIdentificativoTipoDovuto(dovuto.getCodTipoDovuto());
    datiSingoloVersamento.setCausaleVersamento(dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento());
    datiSingoloVersamento.setDatiSpecificiRiscossione(dovuto.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione());
    if(dovuto.getDtRpDatiVersDataEsecuzionePagamento()!= null){
      GregorianCalendar dataScadenzaCal= new GregorianCalendar();
      dataScadenzaCal.setTime(dovuto.getDtRpDatiVersDataEsecuzionePagamento());
      SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
      fmt.setCalendar(dataScadenzaCal);
      String dateFormatted = fmt.format(dataScadenzaCal.getTime());
      //datiSingoloVersamento.setDataScadenza(dateFormatted); //TODO Field "dataScadenza" not found.
    }

    if (dovuto.getMygovDatiMarcaBolloDigitaleId() != null) {
      var ctDatiMarcaBolloDigitale = new CtDatiMarcaBolloDigitale();
      DatiMarcaBolloDigitale datiMarcaBolloDigitale = datiMarcaBolloDigitaleService.getById(dovuto.getMygovDatiMarcaBolloDigitaleId());

      EnumUtils.StTipoBollo tipoBollo = EnumUtils.StTipoBollo.forString(datiMarcaBolloDigitale.getTipoBollo());
      ctDatiMarcaBolloDigitale.setTipoBollo(tipoBollo.toString());
      ctDatiMarcaBolloDigitale.setHashDocumento(datiMarcaBolloDigitale.getHashDocumento());
      ctDatiMarcaBolloDigitale.setProvinciaResidenza(datiMarcaBolloDigitale.getHashDocumento());

      datiSingoloVersamento.setDatiMarcaBolloDigitale(ctDatiMarcaBolloDigitale);
    }

    Optional.ofNullable(dovuto.getBilancio())
        .map(e -> jaxbTransformService.unmarshalling(e.getBytes(), Bilancio.class))
        .ifPresent(datiSingoloVersamento::setBilancio);
  }
}
