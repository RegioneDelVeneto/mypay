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
package it.regioneveneto.mygov.payment.mypay4.ws.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.regioneveneto.mygov.payment.mypay4.model.Carrello;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.ExportDovuti;
import it.regioneveneto.mygov.payment.mypay4.model.ImportDovuti;
import it.regioneveneto.mygov.payment.mypay4.util.TriFunction;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.util.EnumUtils;
import it.veneto.regione.pagamenti.ente.FaultBean;
import it.veneto.regione.schemas._2012.pagamenti.ente.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.*;

@Slf4j
public class PagamentiTelematiciDovutiPagatiHelper {

  private static TriFunction<String, String, String, Optional<FaultBean>> manageFault = (codIpaEnte, codeFault, faultString) -> {
    log.error(faultString);
    var faultBean = VerificationUtils.getFaultBean(codIpaEnte, codeFault, faultString, null);
    return Optional.of(faultBean);
  };

  public static Optional<FaultBean> verificaExportDovuti(String codIpaEnte, ExportDovuti dovuti, String requestToken) {
    if (dovuti == null)
      return manageFault.apply(codIpaEnte, CODE_PAA_REQUEST_TOKEN_NON_VALIDO, String.format("nessun record trovato per Ente [%s] e requestToken [%s]", codIpaEnte, requestToken));
    if (!codIpaEnte.equals(dovuti.getMygovEnteId().getCodIpaEnte()))
      return manageFault.apply(codIpaEnte, CODE_PAA_REQUEST_TOKEN_NON_VALIDO, String.format("Ente [%s] non autorizzato per requestToken [%s]", codIpaEnte, requestToken));
    return Optional.empty();
  }

  public static Optional<FaultBean> verificaPasswordMyPivot(String passwordMypivot, String appBePasswordMyPivot) {
    if ( !StringUtils.isEmpty(passwordMypivot) && !StringUtils.equals(passwordMypivot, appBePasswordMyPivot) ) {
      return manageFault.apply(passwordMypivot, CODE_PAA_PASSWORD_ALLINEAMENTO_MYPIVOT_NON_VALIDA, "Password allineamento MyPivot non valida");
    }
    return Optional.empty();
  }

  public static Optional<FaultBean> verificaDate(String codIpaEnte, Date dtExtractionFrom, Date dtExtractionTo) {
    if (dtExtractionFrom == null) {
      return manageFault.apply(codIpaEnte, CODE_PAA_DATE_FROM_NON_VALIDO, "Data inizio non valida");
    }
    if (dtExtractionTo == null) {
      return manageFault.apply(codIpaEnte, CODE_PAA_DATE_TO_NON_VALIDO, "Data fine non valida");
    }
    if (dtExtractionTo.before(dtExtractionFrom)) {
      return manageFault.apply(codIpaEnte, CODE_PAA_INTERVALLO_DATE_NON_VALIDO, "L'intervallo data inizio e data fine non è valido");
    }
    return Optional.empty();
  }

  public static Optional<FaultBean> verificaTracciato(String codIpaEnte, String versioneTracciato) {
    if (!Utilities.validaVersioneTracciatoExport(versioneTracciato)) {
      return manageFault.apply(codIpaEnte, CODE_PAA_VERSIONE_TRACCIATO_NON_VALIDA,  String.format("Versione tracciato non valida per ente [%s]", codIpaEnte));
    }
    return Optional.empty();
  }

  public static Optional<FaultBean> verificaImportDovuti(String codIpaEnte, ImportDovuti dovuti, String requestToken) {
    if (dovuti == null)
      return manageFault.apply(codIpaEnte, CODE_PAA_REQUEST_TOKEN_NON_VALIDO, String.format("nessun record trovato per Ente [%s] e requestToken [%s]", codIpaEnte, requestToken));
    if (!codIpaEnte.equals(dovuti.getMygovEnteId().getCodIpaEnte()))
      return manageFault.apply(codIpaEnte, CODE_PAA_REQUEST_TOKEN_NON_VALIDO, String.format("Ente [%s] non autorizzato per requestToken [%s]", codIpaEnte, requestToken));
    if (dovuti.getMygovAnagraficaStatoId() == null)
      return manageFault.apply(codIpaEnte, CODE_PAA_REQUEST_TOKEN_NON_VALIDO, String.format("Ente [%s] - token in attesa elaborazione [%s]", codIpaEnte, requestToken));
    return Optional.empty();
  }

  public static Pagati creaPagatiDocument(final Carrello carrello, final List<DovutoElaborato> listaDovuti) {
    PagatiConRicevuta pagatiConRicevuta = creaPagatiDocument(carrello, listaDovuti, false);
    return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .convertValue(pagatiConRicevuta, new TypeReference<>() {});
  }

  public static PagatiConRicevuta creaPagatiDocumentConRicevuta(final Carrello carrello, final List<DovutoElaborato> listaDovuti) {
    return creaPagatiDocument(carrello, listaDovuti, true);
  }

  private static PagatiConRicevuta creaPagatiDocument(final Carrello carrello, final List<DovutoElaborato> listaDovuti, boolean conRicevuta) {
    PagatiConRicevuta ctPagatiConRicevuta  = new PagatiConRicevuta();

    //CtDatiVersamentoPagatiConRicevuta ctPagatiConRicevuta = new CtDatiVersamentoPagatiConRicevuta();

    ctPagatiConRicevuta.setVersioneOggetto(carrello.getDeRpVersioneOggetto());
    ctPagatiConRicevuta.setIdentificativoMessaggioRicevuta(carrello.getCodEIdMessaggioRicevuta());
    Optional.ofNullable(carrello.getCodEDataOraMessaggioRicevuta()).map(Utilities::toXMLGregorianCalendar)
        .ifPresent(ctPagatiConRicevuta::setDataOraMessaggioRicevuta);
    ctPagatiConRicevuta.setRiferimentoMessaggioRichiesta(carrello.getCodERiferimentoMessaggioRichiesta());
    Optional.ofNullable(carrello.getCodERiferimentoDataRichiesta()).map(d -> Utilities.toXMLGregorianCalendar(d, true))
        .ifPresent(ctPagatiConRicevuta::setRiferimentoDataRichiesta);

    CtDominio ctDominio = new CtDominio();
    ctDominio.setIdentificativoDominio(carrello.getCodEDomIdDominio());

    Optional.ofNullable(carrello.getCodEDomIdStazioneRichiedente()).ifPresent(ctDominio::setIdentificativoStazioneRichiedente);

    ctPagatiConRicevuta.setDominio(ctDominio);

    CtIdentificativoUnivoco ctIdentificativoUnivoco = new CtIdentificativoUnivoco();
    ctIdentificativoUnivoco.setCodiceIdentificativoUnivoco(carrello.getCodEIstitAttIdUnivAttCodiceIdUnivoco());
    Optional.ofNullable(carrello.getCodEIstitAttIdUnivAttTipoIdUnivoco()).map(String::valueOf)
        .map(StTipoIdentificativoUnivoco::fromValue).ifPresent(ctIdentificativoUnivoco::setTipoIdentificativoUnivoco);

    CtIstitutoAttestante ctIstitutoAttestante = new CtIstitutoAttestante();
    ctIstitutoAttestante.setIdentificativoUnivocoAttestante(ctIdentificativoUnivoco);
    ctIstitutoAttestante.setDenominazioneAttestante(carrello.getDeEIstitAttDenominazioneAttestante());
    Optional.ofNullable(carrello.getCodEIstitAttCodiceUnitOperAttestante()).ifPresent(ctIstitutoAttestante::setCodiceUnitOperAttestante);
    Optional.ofNullable(carrello.getDeEIstitAttDenomUnitOperAttestante()).ifPresent(ctIstitutoAttestante::setDenomUnitOperAttestante);
    Optional.ofNullable(carrello.getDeEIstitAttIndirizzoAttestante()).ifPresent(ctIstitutoAttestante::setIndirizzoAttestante);
    Optional.ofNullable(carrello.getDeEIstitAttCivicoAttestante()).ifPresent(ctIstitutoAttestante::setCivicoAttestante);
    Optional.ofNullable(carrello.getCodEIstitAttCapAttestante()).ifPresent(ctIstitutoAttestante::setCapAttestante);
    Optional.ofNullable(carrello.getDeEIstitAttLocalitaAttestante()).ifPresent(ctIstitutoAttestante::setLocalitaAttestante);
    Optional.ofNullable(carrello.getDeEIstitAttProvinciaAttestante()).ifPresent(ctIstitutoAttestante::setProvinciaAttestante);
    Optional.ofNullable(carrello.getCodEIstitAttNazioneAttestante()).ifPresent(ctIstitutoAttestante::setNazioneAttestante);

    ctPagatiConRicevuta.setIstitutoAttestante(ctIstitutoAttestante);

    CtEnteBeneficiario ctEnteBeneficiario = new CtEnteBeneficiario();

    CtIdentificativoUnivocoPersonaG ctIdentificativoUnivocoPersonaG = new CtIdentificativoUnivocoPersonaG();
    ctIdentificativoUnivocoPersonaG.setCodiceIdentificativoUnivoco(carrello.getCodEEnteBenefIdUnivBenefCodiceIdUnivoco());
    Optional.ofNullable(carrello.getCodEEnteBenefIdUnivBenefTipoIdUnivoco()).map(String::valueOf)
        .map(StTipoIdentificativoUnivocoPersG::fromValue).ifPresent(ctIdentificativoUnivocoPersonaG::setTipoIdentificativoUnivoco);

    ctEnteBeneficiario.setIdentificativoUnivocoBeneficiario(ctIdentificativoUnivocoPersonaG);
    ctEnteBeneficiario.setDenominazioneBeneficiario(carrello.getDeEEnteBenefDenominazioneBeneficiario());
    Optional.ofNullable(carrello.getCodEEnteBenefCodiceUnitOperBeneficiario()).ifPresent(ctEnteBeneficiario::setCodiceUnitOperBeneficiario);
    Optional.ofNullable(carrello.getDeEEnteBenefDenomUnitOperBeneficiario()).ifPresent(ctEnteBeneficiario::setDenomUnitOperBeneficiario);
    Optional.ofNullable(carrello.getDeEEnteBenefIndirizzoBeneficiario()).ifPresent(ctEnteBeneficiario::setIndirizzoBeneficiario);
    Optional.ofNullable(carrello.getDeEEnteBenefCivicoBeneficiario()).ifPresent(ctEnteBeneficiario::setCivicoBeneficiario);
    Optional.ofNullable(carrello.getCodEEnteBenefCapBeneficiario()).ifPresent(ctEnteBeneficiario::setCapBeneficiario);
    Optional.ofNullable(carrello.getDeEEnteBenefLocalitaBeneficiario()).ifPresent(ctEnteBeneficiario::setLocalitaBeneficiario);
    Optional.ofNullable(carrello.getDeEEnteBenefProvinciaBeneficiario()).ifPresent(ctEnteBeneficiario::setProvinciaBeneficiario);
    Optional.ofNullable(carrello.getCodEEnteBenefNazioneBeneficiario()).ifPresent(ctEnteBeneficiario::setNazioneBeneficiario);

    ctPagatiConRicevuta.setEnteBeneficiario(ctEnteBeneficiario);

    CtIdentificativoUnivocoPersonaFG ctIdentificativoUnivocoPersonaFG = new CtIdentificativoUnivocoPersonaFG();
    ctIdentificativoUnivocoPersonaFG.setCodiceIdentificativoUnivoco(carrello.getCodESoggPagIdUnivPagCodiceIdUnivoco());
    Optional.ofNullable(carrello.getCodESoggPagIdUnivPagTipoIdUnivoco()).map(String::valueOf)
        .map(StTipoIdentificativoUnivocoPersFG::fromValue).ifPresent(ctIdentificativoUnivocoPersonaFG::setTipoIdentificativoUnivoco);

    CtSoggettoPagatore ctSoggettoPagatore = new CtSoggettoPagatore();
    ctSoggettoPagatore.setIdentificativoUnivocoPagatore(ctIdentificativoUnivocoPersonaFG);
    ctSoggettoPagatore.setAnagraficaPagatore(carrello.getCodESoggPagAnagraficaPagatore());
    Optional.ofNullable(carrello.getDeESoggPagIndirizzoPagatore()).ifPresent(ctSoggettoPagatore::setIndirizzoPagatore);
    Optional.ofNullable(carrello.getDeESoggPagCivicoPagatore()).ifPresent(ctSoggettoPagatore::setCivicoPagatore);
    Optional.ofNullable(carrello.getCodESoggPagCapPagatore()).ifPresent(ctSoggettoPagatore::setCapPagatore);
    Optional.ofNullable(carrello.getDeESoggPagLocalitaPagatore()).ifPresent(ctSoggettoPagatore::setLocalitaPagatore);
    Optional.ofNullable(carrello.getDeESoggPagProvinciaPagatore()).map(e -> e.substring(0, 2)).ifPresent(ctSoggettoPagatore::setProvinciaPagatore);
    Optional.ofNullable(carrello.getCodESoggPagNazionePagatore()).ifPresent(ctSoggettoPagatore::setNazionePagatore);
    Optional.ofNullable(carrello.getDeESoggPagEmailPagatore()).ifPresent(ctSoggettoPagatore::setEMailPagatore);

    ctPagatiConRicevuta.setSoggettoPagatore(ctSoggettoPagatore);
    // No difference between creaPagatiDocumento() and creaPagatiDocumentoConRicevuta() from the beginning to here.

    CtDatiVersamentoPagatiConRicevuta ctDatiVersamentoPagatiConRicevuta = new CtDatiVersamentoPagatiConRicevuta();
    Optional.ofNullable(carrello.getCodEDatiPagCodiceEsitoPagamento()).map(String::valueOf)
        .map(EnumUtils.StCodiceEsitoPagamento::forString).map(EnumUtils.StCodiceEsitoPagamento::toString)
        .ifPresent(ctDatiVersamentoPagatiConRicevuta::setCodiceEsitoPagamento);
    ctDatiVersamentoPagatiConRicevuta.setImportoTotalePagato(carrello.getNumEDatiPagImportoTotalePagato());
    ctDatiVersamentoPagatiConRicevuta.setIdentificativoUnivocoVersamento(carrello.getCodEDatiPagIdUnivocoVersamento());
    ctDatiVersamentoPagatiConRicevuta.setCodiceContestoPagamento(carrello.getCodEDatiPagCodiceContestoPagamento());

    for (DovutoElaborato dovutoElaborato : listaDovuti) {
      if (StringUtils.isBlank(dovutoElaborato.getCodEDatiPagDatiSingPagIdUnivocoRiscoss())) {
        continue;
      }

      CtDatiSingoloPagamentoPagatiConRicevuta ctDatiSingoloPagamentoPagatiConRicevuta = new CtDatiSingoloPagamentoPagatiConRicevuta();
      ctDatiVersamentoPagatiConRicevuta.getDatiSingoloPagamentos().add(ctDatiSingoloPagamentoPagatiConRicevuta);

      ctDatiSingoloPagamentoPagatiConRicevuta.setSingoloImportoPagato(dovutoElaborato.getNumEDatiPagDatiSingPagSingoloImportoPagato());

      Optional.ofNullable(dovutoElaborato.getDeEDatiPagDatiSingPagEsitoSingoloPagamento()).ifPresent(ctDatiSingoloPagamentoPagatiConRicevuta::setEsitoSingoloPagamento);
      Optional.ofNullable(dovutoElaborato.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento()).map(d -> Utilities.toXMLGregorianCalendar(d, true))
          .ifPresent(ctDatiSingoloPagamentoPagatiConRicevuta::setDataEsitoSingoloPagamento);
      ctDatiSingoloPagamentoPagatiConRicevuta.setIdentificativoUnivocoRiscossione(dovutoElaborato.getCodEDatiPagDatiSingPagIdUnivocoRiscoss());
      ctDatiSingoloPagamentoPagatiConRicevuta.setCausaleVersamento(dovutoElaborato.getDeEDatiPagDatiSingPagCausaleVersamento());
      ctDatiSingoloPagamentoPagatiConRicevuta.setDatiSpecificiRiscossione(dovutoElaborato.getDeEDatiPagDatiSingPagDatiSpecificiRiscossione());

      ctDatiSingoloPagamentoPagatiConRicevuta.setIdentificativoUnivocoDovuto(dovutoElaborato.getCodIud());

      if (conRicevuta) { // Only the ricevuta has the fields below.
        Optional.ofNullable(dovutoElaborato.getIndiceDatiSingoloPagamento()).ifPresent(ctDatiSingoloPagamentoPagatiConRicevuta::setIndiceDatiSingoloPagamento);
        Optional.ofNullable(dovutoElaborato.getNumEDatiPagDatiSingPagCommissioniApplicatePsp()).ifPresent(ctDatiSingoloPagamentoPagatiConRicevuta::setCommissioniApplicatePSP);

        if (dovutoElaborato.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo() != null
            && dovutoElaborato.getBlbEDatiPagDatiSingPagAllegatoRicevutaTest() != null) {
          CtAllegatoRicevuta ctAllegatoRicevuta = new CtAllegatoRicevuta();
          ctAllegatoRicevuta
              .setTipoAllegatoRicevuta(StTipoAllegatoRicevuta.fromValue(dovutoElaborato.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo()));
          ctAllegatoRicevuta.setTestoAllegato(dovutoElaborato.getBlbEDatiPagDatiSingPagAllegatoRicevutaTest());

          ctDatiSingoloPagamentoPagatiConRicevuta.setAllegatoRicevuta(ctAllegatoRicevuta);
        }
      }
    }

    ctPagatiConRicevuta.setDatiPagamento(ctDatiVersamentoPagatiConRicevuta);

    //pagatiConRicevutaDocument.setPagatiConRicevuta(ctPagatiConRicevuta);

    //return pagatiConRicevutaDocument;
    return ctPagatiConRicevuta;
  }
}
