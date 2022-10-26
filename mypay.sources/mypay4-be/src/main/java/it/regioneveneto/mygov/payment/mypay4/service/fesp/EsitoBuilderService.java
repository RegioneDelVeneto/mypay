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

import it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloPagamentoRT;
import it.gov.digitpa.schemas._2011.pagamenti.RT;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.ws.util.EnumUtils;
import it.veneto.regione.pagamenti.pa.PaaSILInviaEsito;
import it.veneto.regione.pagamenti.pa.ppthead.IntestazionePPT;
import it.veneto.regione.schemas._2012.pagamenti.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EsitoBuilderService {

  @Autowired
  private JAXBTransformService jaxbTransformService;

  /**
   * @param rt
   * @return
   */
  public Esito buildEsito(RT rt) {

    Esito ctEsito = new Esito();
    ctEsito.setVersioneOggetto(rt.getVersioneOggetto());
    
    CtDominio dominio = new CtDominio();
    dominio.setIdentificativoDominio(rt.getDominio().getIdentificativoDominio());
    if (rt.getDominio().getIdentificativoStazioneRichiedente() != null) {
      dominio.setIdentificativoStazioneRichiedente(rt.getDominio().getIdentificativoStazioneRichiedente());
    }
    ctEsito.setDominio(dominio);
    ctEsito.setIdentificativoMessaggioRicevuta(rt.getIdentificativoMessaggioRicevuta());
    ctEsito.setDataOraMessaggioRicevuta(rt.getDataOraMessaggioRicevuta());
    ctEsito.setRiferimentoMessaggioRichiesta(rt.getRiferimentoMessaggioRichiesta());
    ctEsito.setRiferimentoDataRichiesta(rt.getRiferimentoDataRichiesta());

    CtIstitutoAttestante istitutoAttestante = new CtIstitutoAttestante();
    CtIdentificativoUnivoco identificativoUnivocoAttestante = new CtIdentificativoUnivoco();
    it.gov.digitpa.schemas._2011.pagamenti.CtIstitutoAttestante ctIstitutoAttestante = rt.getIstitutoAttestante();
    identificativoUnivocoAttestante.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivoco.fromValue(
            ctIstitutoAttestante.getIdentificativoUnivocoAttestante()
                .getTipoIdentificativoUnivoco().toString()));
    identificativoUnivocoAttestante.setCodiceIdentificativoUnivoco(
        ctIstitutoAttestante.getIdentificativoUnivocoAttestante().getCodiceIdentificativoUnivoco());
    istitutoAttestante.setIdentificativoUnivocoAttestante(identificativoUnivocoAttestante);

    Optional.ofNullable(ctIstitutoAttestante.getDenominazioneAttestante()).ifPresent(istitutoAttestante::setDenominazioneAttestante);
    Optional.ofNullable(ctIstitutoAttestante.getCodiceUnitOperAttestante()).ifPresent(istitutoAttestante::setCodiceUnitOperAttestante);
    Optional.ofNullable(ctIstitutoAttestante.getDenomUnitOperAttestante()).ifPresent(istitutoAttestante::setDenomUnitOperAttestante);
    Optional.ofNullable(ctIstitutoAttestante.getIndirizzoAttestante()).ifPresent(istitutoAttestante::setIndirizzoAttestante);
    Optional.ofNullable(ctIstitutoAttestante.getCivicoAttestante()).ifPresent(istitutoAttestante::setCivicoAttestante);
    Optional.ofNullable(ctIstitutoAttestante.getCapAttestante()).ifPresent(istitutoAttestante::setCapAttestante);
    Optional.ofNullable(ctIstitutoAttestante.getLocalitaAttestante()).ifPresent(istitutoAttestante::setLocalitaAttestante);
    Optional.ofNullable(ctIstitutoAttestante.getProvinciaAttestante()).ifPresent(istitutoAttestante::setProvinciaAttestante);
    Optional.ofNullable(ctIstitutoAttestante.getNazioneAttestante()).ifPresent(istitutoAttestante::setNazioneAttestante);
    ctEsito.setIstitutoAttestante(istitutoAttestante);

    CtEnteBeneficiario enteBeneficiario = new CtEnteBeneficiario();
    it.gov.digitpa.schemas._2011.pagamenti.CtEnteBeneficiario ctEnteBeneficiario = rt.getEnteBeneficiario();
    CtIdentificativoUnivocoPersonaG identificativoUnivocoBeneficiario = new CtIdentificativoUnivocoPersonaG();
    identificativoUnivocoBeneficiario.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.fromValue(
        ctEnteBeneficiario.getIdentificativoUnivocoBeneficiario()
                .getTipoIdentificativoUnivoco().toString()));
    identificativoUnivocoBeneficiario.setCodiceIdentificativoUnivoco(
        ctEnteBeneficiario.getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco());
    enteBeneficiario.setIdentificativoUnivocoBeneficiario(identificativoUnivocoBeneficiario);
    enteBeneficiario.setDenominazioneBeneficiario(ctEnteBeneficiario.getDenominazioneBeneficiario());

    Optional.ofNullable(ctEnteBeneficiario.getCodiceUnitOperBeneficiario()).ifPresent(enteBeneficiario::setCodiceUnitOperBeneficiario);
    Optional.ofNullable(ctEnteBeneficiario.getDenomUnitOperBeneficiario()).ifPresent(enteBeneficiario::setDenomUnitOperBeneficiario);
    Optional.ofNullable(ctEnteBeneficiario.getIndirizzoBeneficiario()).ifPresent(enteBeneficiario::setIndirizzoBeneficiario);
    Optional.ofNullable(ctEnteBeneficiario.getCivicoBeneficiario()).ifPresent(enteBeneficiario::setCivicoBeneficiario);
    Optional.ofNullable(ctEnteBeneficiario.getCapBeneficiario()).ifPresent(enteBeneficiario::setCapBeneficiario);
    Optional.ofNullable(ctEnteBeneficiario.getLocalitaBeneficiario()).ifPresent(enteBeneficiario::setLocalitaBeneficiario);
    Optional.ofNullable(ctEnteBeneficiario.getProvinciaBeneficiario()).ifPresent(enteBeneficiario::setProvinciaBeneficiario);
    Optional.ofNullable(ctEnteBeneficiario.getNazioneBeneficiario()).ifPresent(enteBeneficiario::setNazioneBeneficiario);
    ctEsito.setEnteBeneficiario(enteBeneficiario);

    if (rt.getSoggettoVersante() != null) {
      CtSoggettoVersante soggettoVersante = new CtSoggettoVersante();
      it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoVersante ctSoggettoVersante = rt.getSoggettoVersante();
      CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante = new CtIdentificativoUnivocoPersonaFG();
      identificativoUnivocoVersante.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.fromValue(
          ctSoggettoVersante.getIdentificativoUnivocoVersante()
                  .getTipoIdentificativoUnivoco().toString()));
      identificativoUnivocoVersante.setCodiceIdentificativoUnivoco(
          ctSoggettoVersante.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());
      soggettoVersante.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);
      soggettoVersante.setAnagraficaVersante(ctSoggettoVersante.getAnagraficaVersante());
      Optional.ofNullable(ctSoggettoVersante.getIndirizzoVersante()).ifPresent(soggettoVersante::setIndirizzoVersante);
      Optional.ofNullable(ctSoggettoVersante.getCivicoVersante()).ifPresent(soggettoVersante::setCivicoVersante);
      Optional.ofNullable(ctSoggettoVersante.getIndirizzoVersante()).ifPresent(soggettoVersante::setIndirizzoVersante);
      Optional.ofNullable(ctSoggettoVersante.getCapVersante()).ifPresent(soggettoVersante::setCapVersante);
      Optional.ofNullable(ctSoggettoVersante.getLocalitaVersante()).ifPresent(soggettoVersante::setLocalitaVersante);
      Optional.ofNullable(ctSoggettoVersante.getProvinciaVersante()).ifPresent(soggettoVersante::setProvinciaVersante);
      Optional.ofNullable(ctSoggettoVersante.getNazioneVersante()).ifPresent(soggettoVersante::setNazioneVersante);
      Optional.ofNullable(ctSoggettoVersante.getEMailVersante()).ifPresent(soggettoVersante::setEMailVersante);
      ctEsito.setSoggettoVersante(soggettoVersante);
    }

    CtSoggettoPagatore soggettoPagatore = new CtSoggettoPagatore();
    it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoPagatore ctSoggettoPagatore = rt.getSoggettoPagatore();
    CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore = new CtIdentificativoUnivocoPersonaFG();
    identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.fromValue(
        ctSoggettoPagatore.getIdentificativoUnivocoPagatore()
                .getTipoIdentificativoUnivoco().toString()));
    identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(
        ctSoggettoPagatore.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());
    soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
    soggettoPagatore.setAnagraficaPagatore(ctSoggettoPagatore.getAnagraficaPagatore());
    Optional.ofNullable(ctSoggettoPagatore.getIndirizzoPagatore()).ifPresent(soggettoPagatore::setIndirizzoPagatore);
    Optional.ofNullable(ctSoggettoPagatore.getCivicoPagatore()).ifPresent(soggettoPagatore::setCivicoPagatore);
    Optional.ofNullable(ctSoggettoPagatore.getCapPagatore()).ifPresent(soggettoPagatore::setCapPagatore);
    Optional.ofNullable(ctSoggettoPagatore.getLocalitaPagatore()).ifPresent(soggettoPagatore::setLocalitaPagatore);
    Optional.ofNullable(ctSoggettoPagatore.getProvinciaPagatore()).ifPresent(soggettoPagatore::setProvinciaPagatore);
    Optional.ofNullable(ctSoggettoPagatore.getNazionePagatore()).ifPresent(soggettoPagatore::setNazionePagatore);
    Optional.ofNullable(ctSoggettoPagatore.getEMailPagatore()).ifPresent(soggettoPagatore::setEMailPagatore);
    ctEsito.setSoggettoPagatore(soggettoPagatore);

    CtDatiVersamentoEsito datiPagamento = new CtDatiVersamentoEsito();
    datiPagamento.setCodiceEsitoPagamento(EnumUtils.StCodiceEsitoPagamento.forString(rt.getDatiPagamento().getCodiceEsitoPagamento()).toString());
    datiPagamento.setImportoTotalePagato(rt.getDatiPagamento().getImportoTotalePagato());
    datiPagamento.setIdentificativoUnivocoVersamento(rt.getDatiPagamento().getIdentificativoUnivocoVersamento());
    datiPagamento.setCodiceContestoPagamento(rt.getDatiPagamento().getCodiceContestoPagamento());

    if (!rt.getDatiPagamento().getDatiSingoloPagamentos().isEmpty()) {
      List<CtDatiSingoloPagamentoEsito> pagamentiSingoli = new ArrayList<>();
      for (CtDatiSingoloPagamentoRT singoloPagamentoRT : rt.getDatiPagamento().getDatiSingoloPagamentos()) {
        
        CtDatiSingoloPagamentoEsito singoloPagamento = new CtDatiSingoloPagamentoEsito();
        singoloPagamento.setSingoloImportoPagato(singoloPagamentoRT.getSingoloImportoPagato());
        singoloPagamento.setCausaleVersamento(singoloPagamentoRT.getCausaleVersamento());
        singoloPagamento.setDataEsitoSingoloPagamento(singoloPagamentoRT.getDataEsitoSingoloPagamento());
        singoloPagamento.setDatiSpecificiRiscossione(singoloPagamentoRT.getDatiSpecificiRiscossione());
        Optional.ofNullable(singoloPagamentoRT.getEsitoSingoloPagamento()).ifPresent(singoloPagamento::setEsitoSingoloPagamento);
        singoloPagamento.setIdentificativoUnivocoRiscossione(singoloPagamentoRT.getIdentificativoUnivocoRiscossione());
        Optional.ofNullable(singoloPagamentoRT.getCommissioniApplicatePSP()).ifPresent(singoloPagamento::setCommissioniApplicatePSP);
        if (singoloPagamentoRT.getAllegatoRicevuta() != null) {
          CtAllegatoRicevuta ctAllegatoRicevuta = new CtAllegatoRicevuta();
          ctAllegatoRicevuta.setTipoAllegatoRicevuta(StTipoAllegatoRicevuta.fromValue(singoloPagamentoRT.getAllegatoRicevuta().getTipoAllegatoRicevuta().toString()));
          ctAllegatoRicevuta.setTestoAllegato(singoloPagamentoRT.getAllegatoRicevuta().getTestoAllegato());
          singoloPagamento.setAllegatoRicevuta(ctAllegatoRicevuta);
        }
        pagamentiSingoli.add(singoloPagamento);
      }
      datiPagamento.getDatiSingoloPagamentos().addAll(pagamentiSingoli);
    }
    ctEsito.setDatiPagamento(datiPagamento);

    return ctEsito;
  }

  /**
   * @param header
   * @return
   */
  public IntestazionePPT buildHeaderEsito(gov.telematici.pagamenti.ws.ppthead.IntestazionePPT header) {
    IntestazionePPT result = new IntestazionePPT();
    result.setCodiceContestoPagamento(header.getCodiceContestoPagamento());
    result.setIdentificativoDominio(header.getIdentificativoDominio());
    result.setIdentificativoUnivocoVersamento(header.getIdentificativoUnivocoVersamento());

    return result;
  }

  /**
   * @param ctEsito
   * @return
   */
  public PaaSILInviaEsito buildBodyEsito(Esito ctEsito, String tipoFirma, byte[] rt) {
    String xmlString = jaxbTransformService.marshalling(ctEsito, Esito.class);
    byte[] byteEsito = xmlString.trim().getBytes(StandardCharsets.UTF_8);

    PaaSILInviaEsito result = new PaaSILInviaEsito();
    result.setEsito(byteEsito);
    result.setTipoFirma(tipoFirma);
    result.setRt(rt);

    return result;
  }
}
