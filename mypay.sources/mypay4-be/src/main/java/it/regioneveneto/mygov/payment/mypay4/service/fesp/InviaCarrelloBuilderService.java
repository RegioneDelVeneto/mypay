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

import it.gov.digitpa.schemas._2011.pagamenti.*;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.veneto.regione.schemas._2012.pagamenti.CtDatiSingoloVersamentoRP;
import it.veneto.regione.schemas._2012.pagamenti.RP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InviaCarrelloBuilderService {

  public RPT buildRPT(RP rp, Ente enteProp) {
    RPT ctRPT = new RPT();
    ctRPT.setVersioneOggetto(rp.getVersioneOggetto());

    CtDominio dominio = new CtDominio();
    dominio.setIdentificativoDominio(rp.getDominio().getIdentificativoDominio());
    Optional.ofNullable(rp.getDominio().getIdentificativoStazioneRichiedente()).ifPresent(dominio::setIdentificativoStazioneRichiedente);
    ctRPT.setDominio(dominio);

    ctRPT.setIdentificativoMessaggioRichiesta(rp.getIdentificativoMessaggioRichiesta());
    ctRPT.setDataOraMessaggioRichiesta(rp.getDataOraMessaggioRichiesta());
    ctRPT.setAutenticazioneSoggetto(StAutenticazioneSoggetto.fromValue(rp.getAutenticazioneSoggetto().toString()));

    CtSoggettoVersante soggettoVersante = new CtSoggettoVersante();
    CtIdentificativoUnivocoPersonaFG identificativoUnivocoVersante = new CtIdentificativoUnivocoPersonaFG();
    if (rp.getSoggettoVersante() != null) {
      it.veneto.regione.schemas._2012.pagamenti.CtSoggettoVersante soggettoVersantePA = rp.getSoggettoVersante();
      identificativoUnivocoVersante.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.fromValue(
          soggettoVersantePA.getIdentificativoUnivocoVersante().getTipoIdentificativoUnivoco().toString()));
      identificativoUnivocoVersante.setCodiceIdentificativoUnivoco(
          soggettoVersantePA.getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());
      soggettoVersante.setIdentificativoUnivocoVersante(identificativoUnivocoVersante);
      soggettoVersante.setAnagraficaVersante(soggettoVersantePA.getAnagraficaVersante());
      Optional.ofNullable(soggettoVersantePA.getIndirizzoVersante()).ifPresent(soggettoVersante::setIndirizzoVersante);
      Optional.ofNullable(soggettoVersantePA.getCivicoVersante()).ifPresent(soggettoVersante::setCivicoVersante);
      Optional.ofNullable(soggettoVersantePA.getCapVersante()).ifPresent(soggettoVersante::setCapVersante);
      Optional.ofNullable(soggettoVersantePA.getLocalitaVersante()).ifPresent(soggettoVersante::setLocalitaVersante);
      Optional.ofNullable(soggettoVersantePA.getProvinciaVersante()).ifPresent(soggettoVersante::setProvinciaVersante);
      Optional.ofNullable(soggettoVersantePA.getNazioneVersante()).ifPresent(soggettoVersante::setNazioneVersante);
      Optional.ofNullable(soggettoVersantePA.getEMailVersante()).ifPresent(soggettoVersante::setEMailVersante);
      ctRPT.setSoggettoVersante(soggettoVersante);
    }

    CtSoggettoPagatore soggettoPagatore = new CtSoggettoPagatore();
    it.veneto.regione.schemas._2012.pagamenti.CtSoggettoPagatore soggettoPagatorePA = rp.getSoggettoPagatore();
    CtIdentificativoUnivocoPersonaFG identificativoUnivocoPagatore = new CtIdentificativoUnivocoPersonaFG();
    identificativoUnivocoPagatore.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.fromValue(
        soggettoPagatorePA.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString()));
    identificativoUnivocoPagatore.setCodiceIdentificativoUnivoco(
        soggettoPagatorePA.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());
    soggettoPagatore.setIdentificativoUnivocoPagatore(identificativoUnivocoPagatore);
    soggettoPagatore.setAnagraficaPagatore(soggettoPagatorePA.getAnagraficaPagatore());
    Optional.ofNullable(soggettoPagatorePA.getIndirizzoPagatore()).ifPresent(soggettoPagatore::setIndirizzoPagatore);
    Optional.ofNullable(soggettoPagatorePA.getCivicoPagatore()).ifPresent(soggettoPagatore::setCivicoPagatore);
    Optional.ofNullable(soggettoPagatorePA.getCapPagatore()).ifPresent(soggettoPagatore::setCapPagatore);
    Optional.ofNullable(soggettoPagatorePA.getLocalitaPagatore()).ifPresent(soggettoPagatore::setLocalitaPagatore);
    Optional.ofNullable(soggettoPagatorePA.getProvinciaPagatore()).ifPresent(soggettoPagatore::setProvinciaPagatore);
    Optional.ofNullable(soggettoPagatorePA.getNazionePagatore()).ifPresent(soggettoPagatore::setNazionePagatore);
    Optional.ofNullable(soggettoPagatorePA.getEMailPagatore()).ifPresent(soggettoPagatore::setEMailPagatore);
    ctRPT.setSoggettoPagatore(soggettoPagatore);

    //l'ente sull'rp nn c'e' (prendere da tabella ente)
    CtEnteBeneficiario enteBeneficiario = new CtEnteBeneficiario();
    CtIdentificativoUnivocoPersonaG idUnivocoPersonaG = new CtIdentificativoUnivocoPersonaG();
    idUnivocoPersonaG.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.fromValue(enteProp.getCodRpEnteBenefIdUnivBenefTipoIdUnivoco()));
    idUnivocoPersonaG.setCodiceIdentificativoUnivoco(enteProp.getCodRpEnteBenefIdUnivBenefCodiceIdUnivoco());
    enteBeneficiario.setIdentificativoUnivocoBeneficiario(idUnivocoPersonaG);
    enteBeneficiario.setDenominazioneBeneficiario(enteProp.getDeRpEnteBenefDenominazioneBeneficiario());
    Optional.ofNullable(enteProp.getDeRpEnteBenefDenomUnitOperBeneficiario()).ifPresent(enteBeneficiario::setDenomUnitOperBeneficiario);
    Optional.ofNullable(enteProp.getCodRpEnteBenefCodiceUnitOperBeneficiario()).ifPresent(enteBeneficiario::setCodiceUnitOperBeneficiario);
    Optional.ofNullable(enteProp.getDeRpEnteBenefLocalitaBeneficiario()).ifPresent(enteBeneficiario::setLocalitaBeneficiario);
    Optional.ofNullable(enteProp.getDeRpEnteBenefProvinciaBeneficiario()).ifPresent(enteBeneficiario::setProvinciaBeneficiario);
    Optional.ofNullable(enteProp.getDeRpEnteBenefIndirizzoBeneficiario()).ifPresent(enteBeneficiario::setIndirizzoBeneficiario);
    Optional.ofNullable(enteProp.getDeRpEnteBenefCivicoBeneficiario()).ifPresent(enteBeneficiario::setCivicoBeneficiario);
    Optional.ofNullable(enteProp.getCodRpEnteBenefCapBeneficiario()).ifPresent(enteBeneficiario::setCapBeneficiario);
    Optional.ofNullable(enteProp.getCodRpEnteBenefNazioneBeneficiario()).ifPresent(enteBeneficiario::setNazioneBeneficiario);
    ctRPT.setEnteBeneficiario(enteBeneficiario);

    CtDatiVersamentoRPT datiVersamento = new CtDatiVersamentoRPT();
    datiVersamento.setDataEsecuzionePagamento(rp.getDatiVersamento().getDataEsecuzionePagamento());
    datiVersamento.setImportoTotaleDaVersare(rp.getDatiVersamento().getImportoTotaleDaVersare());
    datiVersamento.setTipoVersamento(StTipoVersamento.fromValue(rp.getDatiVersamento().getTipoVersamento().toString()));
    datiVersamento.setIdentificativoUnivocoVersamento(rp.getDatiVersamento().getIdentificativoUnivocoVersamento());
    datiVersamento.setCodiceContestoPagamento(rp.getDatiVersamento().getCodiceContestoPagamento());
    Optional.ofNullable(rp.getDatiVersamento().getIbanAddebito()).ifPresent(datiVersamento::setIbanAddebito);
    Optional.ofNullable(rp.getDatiVersamento().getBicAddebito()).ifPresent(datiVersamento::setBicAddebito);

    //Tipo Firma richiesta per la RT (preso dalla tabella ente)
    datiVersamento.setFirmaRicevuta(enteProp.getCodRpDatiVersFirmaRicevuta());

    //SINGOLI VERSAMENTI
    List<CtDatiSingoloVersamentoRPT> ctDatiSingoloVersamentoRPTList = new ArrayList<>();
    for (CtDatiSingoloVersamentoRP item : rp.getDatiVersamento().getDatiSingoloVersamentos()) {
      CtDatiSingoloVersamentoRPT ctDatiSingoloVersamentoRPT = new CtDatiSingoloVersamentoRPT();
      ctDatiSingoloVersamentoRPT.setImportoSingoloVersamento(item.getImportoSingoloVersamento());

      Optional.ofNullable(item.getCommissioneCaricoPA()).ifPresent(ctDatiSingoloVersamentoRPT::setCommissioneCaricoPA);
      Optional.ofNullable(item.getIbanAccredito()).ifPresent(ctDatiSingoloVersamentoRPT::setIbanAccredito);
      Optional.ofNullable(item.getBicAccredito()).ifPresent(ctDatiSingoloVersamentoRPT::setBicAccredito);
      Optional.ofNullable(item.getIbanAppoggio()).ifPresent(ctDatiSingoloVersamentoRPT::setIbanAppoggio);
      Optional.ofNullable(item.getBicAppoggio()).ifPresent(ctDatiSingoloVersamentoRPT::setBicAppoggio);
      Optional.ofNullable(item.getCredenzialiPagatore()).ifPresent(ctDatiSingoloVersamentoRPT::setCredenzialiPagatore);

      if (item.getDatiMarcaBolloDigitale() != null) {
        CtDatiMarcaBolloDigitale newMarcaBolloDigitale = new CtDatiMarcaBolloDigitale();
        newMarcaBolloDigitale.setTipoBollo(item.getDatiMarcaBolloDigitale().getTipoBollo());
        newMarcaBolloDigitale.setHashDocumento(item.getDatiMarcaBolloDigitale().getHashDocumento());
        newMarcaBolloDigitale.setProvinciaResidenza(item.getDatiMarcaBolloDigitale().getProvinciaResidenza());
        ctDatiSingoloVersamentoRPT.setDatiMarcaBolloDigitale(newMarcaBolloDigitale);
      }
      ctDatiSingoloVersamentoRPT.setCausaleVersamento(item.getCausaleVersamento());
      ctDatiSingoloVersamentoRPT.setDatiSpecificiRiscossione(item.getDatiSpecificiRiscossione());
      ctDatiSingoloVersamentoRPTList.add(ctDatiSingoloVersamentoRPT);
    }
    datiVersamento.getDatiSingoloVersamentos().addAll(ctDatiSingoloVersamentoRPTList);
    ctRPT.setDatiVersamento(datiVersamento);
    return ctRPT;
  }
}
