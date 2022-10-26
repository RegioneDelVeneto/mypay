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

import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaPagatore;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.ReportUtilities;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class JasperService implements Serializable {

  @Autowired
  private EnteService enteService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private DovutoElaboratoService dovutoElaboratoService;

  @Autowired
  private ResourceLoader resourceLoader;

  private static final String REG_EX = "[^0-9A-Za-z &',-.:_]";

  @Value("${pa.reportsProdEnvironment:false}")
  private String reportsProdEnvironment;

  @Value("${pa.logoDefault:}")
  private String defaultLogo;

  public ByteArrayOutputStream generateAvviso(Dovuto dovuto) throws Exception {

    String iuv;
    String codTipoDovuto = dovuto.getCodTipoDovuto();
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    Ente ente = dovuto.getNestedEnte();
    BigDecimal importo = dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento();

    AnagraficaPagatore anagraficaPagatore = new AnagraficaPagatore();
    if (dovuto.getNestedAvviso()!=null) {
      anagraficaPagatore.setIndirizzo(dovuto.getNestedAvviso().getDeRpSoggPagIndirizzoPagatore());
      anagraficaPagatore.setCivico(dovuto.getNestedAvviso().getDeRpSoggPagCivicoPagatore());
      anagraficaPagatore.setCap(dovuto.getNestedAvviso().getCodRpSoggPagCapPagatore());
      anagraficaPagatore.setLocalita(dovuto.getNestedAvviso().getDeRpSoggPagLocalitaPagatore());
      anagraficaPagatore.setProvincia(dovuto.getNestedAvviso().getDeRpSoggPagProvinciaPagatore());
      anagraficaPagatore.setNazione(null);
      anagraficaPagatore.setAnagrafica(dovuto.getNestedAvviso().getDeRpSoggPagAnagraficaPagatore());
      anagraficaPagatore.setCodiceIdentificativoUnivoco(dovuto.getNestedAvviso().getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
    } else {
      anagraficaPagatore.setIndirizzo(dovuto.getDeRpSoggPagIndirizzoPagatore());
      anagraficaPagatore.setCivico(dovuto.getDeRpSoggPagCivicoPagatore());
      anagraficaPagatore.setCap(dovuto.getCodRpSoggPagCapPagatore());
      anagraficaPagatore.setLocalita(dovuto.getDeRpSoggPagLocalitaPagatore());
      anagraficaPagatore.setProvincia(dovuto.getDeRpSoggPagProvinciaPagatore());
      anagraficaPagatore.setNazione(null);
      anagraficaPagatore.setAnagrafica(dovuto.getDeRpSoggPagAnagraficaPagatore());
      anagraficaPagatore.setCodiceIdentificativoUnivoco(dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco());
    }

    String reportFilePathForEnte =  getResourcePath("/jasper/templates/" + ente.getCodIpaEnte() + "/avviso_pagamento.jasper");
    String reportFilePath;

    if (StringUtils.isNotBlank(reportFilePathForEnte)) {
      reportFilePath = reportFilePathForEnte;
    } else {
      reportFilePath = getResourcePath("/jasper/templates/avviso_pagamento.jasper");
    }

//		JasperReport jasperReport = JasperCompileManager.compileReport(reportFilePath);

    String oggettoDelPagamento = StringUtils.isNotBlank(dovuto.getDeCausaleVisualizzata()) ?
        dovuto.getDeCausaleVisualizzata() : dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento();
    String indirizzoDestinatario =
        (anagraficaPagatore.getIndirizzo() != null ? anagraficaPagatore.getIndirizzo()  + " " : "")
            + (anagraficaPagatore.getCivico() != null ? anagraficaPagatore.getCivico() + " " : "")
            + (anagraficaPagatore.getCap() != null ? anagraficaPagatore.getCap()  + "\n" : "")
            + (anagraficaPagatore.getLocalita() != null ? anagraficaPagatore.getLocalita() : "")
            + (anagraficaPagatore.getProvincia() != null ? (" (" + anagraficaPagatore.getProvincia() + ") - ") : "")
            + (anagraficaPagatore.getNazione() != null ? anagraficaPagatore.getNazione() : "");
    String infoEnte = ente.getDeRpEnteBenefIndirizzoBeneficiario() + " "
        + ente.getDeRpEnteBenefCivicoBeneficiario() + " "
        + ente.getCodRpEnteBenefCapBeneficiario() + " "
        + ente.getDeRpEnteBenefLocalitaBeneficiario() + " ("
        + ente.getDeRpEnteBenefProvinciaBeneficiario() + ")\n"
        + ente.getDeRpEnteBenefTelefonoBeneficiario() + "\n"
        //+ ente.getDeRpEnteBenefSitoWebBeneficiario() + " "
        + (ente.getDeRpEnteBenefEmailBeneficiario() != null ? ente.getDeRpEnteBenefEmailBeneficiario() : "");

    String qrCodeString = "";
    String codAvviso = "";
    iuv = dovuto.getCodIuv();
    if (iuv.length() == Constants.IUV_GENERATOR_15_LENGTH) {
      qrCodeString = Utilities.generateQRCodeString(ente.getCodiceFiscaleEnte(), Constants.AVVISO_PAGAMENTO_AUX_DIGIT, ente.getApplicationCode(), iuv, importo);
      codAvviso = Utilities.formatNumeroAvviso15digit(ente.getApplicationCode(), iuv);
    } else if (iuv.length() == Constants.IUV_GENERATOR_17_LENGTH) {
      qrCodeString = Utilities.generateQRCodeString(ente.getCodiceFiscaleEnte(), Constants.SMALL_IUV_AUX_DIGIT, "", iuv, importo);
      codAvviso = Utilities.formatNumeroAvviso17digit(Constants.SMALL_IUV_AUX_DIGIT, iuv);
    }
    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(codTipoDovuto, ente.getCodIpaEnte(),false).get();

    String dataScadenzaString = ( dovuto.getDtRpDatiVersDataEsecuzionePagamento() != null && enteTipoDovuto.isFlgStampaDataScadenza() )
        ? formatter.format(dovuto.getDtRpDatiVersDataEsecuzionePagamento()) : "";

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oggetto_del_pagamento", Utilities.shortenString(oggettoDelPagamento, 60));
    parameters.put("nome_cognome_destinatario", Utilities.shortenString(anagraficaPagatore.getAnagrafica(), 35));
    parameters.put("indirizzo_destinatario", Utilities.shortenString(indirizzoDestinatario, 40));
    parameters.put("cf_destinatario",  Utilities.shortenString(anagraficaPagatore.getCodiceIdentificativoUnivoco(), 16).toUpperCase());


    parameters.put("ente_creditore",  Utilities.shortenString(ente.getDeRpEnteBenefDenominazioneBeneficiario(), 50));
    parameters.put("cf_ente",  Utilities.shortenString(ente.getCodiceFiscaleEnte(), 16).toUpperCase());
    parameters.put("settore_ente",  Utilities.shortenString(enteTipoDovuto.getDeSettoreEnte(), 50));
    parameters.put("info_ente",  Utilities.shortenString(infoEnte, 100));
    if (dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento().compareTo(new BigDecimal(99999999.99)) == 1) {
      throw new Exception("Numero che eccede l'importo pagabile massimo possibile ");
    }
    String importoString = Utilities.getStringFromBigDecimalGroup(dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento());
    String importoStringNoPoint = importoString.replace(",", "");

    parameters.put("importo_euro", importoString + " Euro");
    parameters.put("importo", importoString);
    parameters.put("data", dataScadenzaString);
    parameters.put("qr_code_string", qrCodeString);
    parameters.put("cbill", StringUtils.isNotBlank(ente.getCodCodiceInterbancarioCbill()) ? ente.getCodCodiceInterbancarioCbill().toUpperCase() : "-");
    parameters.put("codice_avviso", codAvviso);
    parameters.put("lingua_aggiuntiva", ente.getLinguaAggiuntiva());
    // VERIFICARE SE ENTE POSSIEDE CC POSTE
    parameters.put("poste", "ko" );
    if (StringUtils.isNotBlank(enteTipoDovuto.getCodContoCorrentePostale()) && StringUtils.isNotBlank(ente.getDeAutorizzazione())){
      parameters.put("poste", "ok");
      parameters.put("numero_cc_postale", Utilities.shortenString(enteTipoDovuto.getCodContoCorrentePostale(), 12));  //su documento agid lunghezza 20 su documento poste lunghezza 12
      parameters.put("intestatario_conto_corrente_postale", Utilities.shortenString(enteTipoDovuto.getDeIntestatarioCcPostale(), 50));
      int index = ente.getDeAutorizzazione().indexOf("DEL");
      parameters.put("autorizzazione_0", Utilities.shortenString(ente.getDeAutorizzazione(), index-1));
      parameters.put("autorizzazione_1", ente.getDeAutorizzazione().substring(index));
      parameters.put("datamatrix_string",
          "codfase=".toLowerCase() +
              "NBPA".toUpperCase()  +
              ";".toUpperCase()  +
              (Constants.LUNGHEZZA_CODICE_AVVISO +  //Lunghezza Codice Avviso  --  Valore Fisso=18
                  codAvviso.replaceAll(" ", "") + // Codice Avviso    --   Coincide con il IV campo del bollettino ed e composto da 18 caratteri numerici
                  Constants.LUNGHEZZA_CONTO + //Lunghezza Conto  --  Valore Fisso=12
                  StringUtils.leftPad(Utilities.shortenString(enteTipoDovuto.getCodContoCorrentePostale().replaceAll(REG_EX, " "), 12), 12, '0') + //Conto  --  Si compone di 12 caratteri numerici, nel caso di numeri di conto corrente postale di lunghezza inferiore, bisognera aggiungere, a partire da sinistra, i necessari zeri iniziali fino a raggiungere la lunghezza di 12 caratteri
                  Constants.LUNGHEZZA_IMPORTO + //Lunghezza Importo  --  Valore Fisso=10
                  StringUtils.leftPad(importoStringNoPoint.replaceAll(REG_EX, " "), 10, '0') + //Importo  --  Si compone di 8 cifre intere e due decimali In caso di lunghezza inferiore, bisognera aggiungere, a partire da sinistra, i necessari zeri non significativi
                  Constants.LUNGHEZZA_TIPO_DOCUMENTO + //Lunghezza Tipo Documento  --  Valore Fisso=3
                  Constants.TIPO_DOC_PRESSO_PSP //Tipo Documento
              ).toUpperCase()  +  //Codeline
              Constants.ID_DATAMATRIX.toUpperCase()  +
              Constants.FASE_PAGAMENTO.toUpperCase()  +
              Utilities.shortenString(ente.getCodiceFiscaleEnte().replaceAll(REG_EX, " "), 11).toUpperCase()  + //codice fiscale ente
              StringUtils.rightPad(Utilities.shortenString(anagraficaPagatore.getCodiceIdentificativoUnivoco().replaceAll(REG_EX, " "), 16), 16) +        // Codice fiscale/partita iva --- Allineato a sx con eventuale filler blank a dx
              StringUtils.rightPad(Utilities.shortenString(anagraficaPagatore.getAnagrafica().replaceAll(REG_EX, " "), 40), 40).toUpperCase() + //Nome-cognome / ragione sociale / Destinatario  --- Allineato a sx con eventuale	filler blank a dx Saranno stampate sul Bollettino PA esclusivamente i primi 35 caratteri
              StringUtils.rightPad(Utilities.shortenString(oggettoDelPagamento.replaceAll(REG_EX, " "), 110), 110).toUpperCase()  + //Causale versamento / Oggetto del pagamento
              "            ".toUpperCase()  +   //Filler 12 spazi
              Constants.VALORE_FINALE_DATAMATRIX.toUpperCase() // Valore finale datamatrix
      );
    }

    //String logoDefault = this.propertiesUtil.getProperty("pa.logoDefault");
    String logoBase64Grayscale;

    String logoEnte = StringUtils.firstNonBlank(ente.getDeLogoEnte(), defaultLogo);

    if(StringUtils.isNotBlank(logoEnte)) {
      BufferedImage logoImage = Utilities.getImageFromBase64String(logoEnte);
      BufferedImage logoImageGrayscale = Utilities.covertToGrayscale(logoImage);
      logoBase64Grayscale = Utilities.getBase64StringFromImage(logoImageGrayscale);
      parameters.put("ente_img", logoBase64Grayscale);
    }

    parameters.put("pagolapa_img", getResourcePath("/jasper/images/logo-pagopa@3x.png"));
    parameters.put("avviso_pagamento", getResourcePath("/jasper/images/scritta-avviso-di-pagamento@3x.png"));
    parameters.put("paga_app_img", getResourcePath("/jasper/images/canali-digitali@3x.png"));
    parameters.put("paga_territorio_img", getResourcePath("/jasper/images/canali-fisici@3x.png"));
    parameters.put("banco_posta_img", getResourcePath("/jasper/images/logo-bancoposta@3x.png"));
    parameters.put("poste_italiane_img", getResourcePath("/jasper/images/logo-poste-italiane@3x.png"));
    parameters.put("bollettino_postale_img", getResourcePath("/jasper/images/logo-bollettino-postale@3x.png"));
    parameters.put("euro_img", getResourcePath("/jasper/images/logo-euro-bollettino@3x.png"));
    parameters.put("forbici_img", getResourcePath("/jasper/images/forbice.png"));
    parameters.put("font_path_RobotoMono-Bold", getResourcePath("/jasper/fonts/RobotoMono-Bold.ttf"));
    parameters.put("font_path_RobotoMono-Regular", getResourcePath("/jasper/fonts/RobotoMono-Regular.ttf"));
    parameters.put("font_path_TitilliumWeb-Black", getResourcePath("/jasper/fonts/TitilliumWeb-Black.ttf"));
    parameters.put("font_path_TitilliumWeb-Bold", getResourcePath("/jasper/fonts/TitilliumWeb-Bold.ttf"));
    parameters.put("font_path_TitilliumWeb-Regular", getResourcePath("/jasper/fonts/TitilliumWeb-Regular.ttf"));

    JasperReport jasperReport = (JasperReport) JRLoader.loadObject(new URL(getResourcePath(reportFilePath)));//JasperCompileManager.compileReport(reportSource);

    return ReportUtilities.generateAvviso(jasperReport, parameters);
  }

  public ByteArrayOutputStream generateRicevuta(Carrello carrello) throws Exception {

    Ente ente = Optional.ofNullable(enteService.getEnteByCodFiscale(StringUtils.trim(carrello.getCodRpDomIdDominio()))).orElseThrow(NotFoundException::new);

    //Path per il folder sul server dei Report templates
    String reportFilePathForEnte =  getResourcePath("/jasper/templates/" + ente.getCodIpaEnte() + "/exportRT.jasper");
    String reportFilePath;

    if (StringUtils.isNotBlank(reportFilePathForEnte)) {
      reportFilePath = reportFilePathForEnte;
    } else {
      reportFilePath = getResourcePath("/jasper/templates/exportRT.jasper");
    }

    //EnteLogo enteLogo = new EnteLogo();

    //String logoEnteBase64 = enteService.getLogoDaMostrareByCodIpa(ente.getCodIpaEnte());
    //enteLogo.setDeLogoEnte(logoEnteBase64);


    //Dati del carrello
    Map<String, Object> reportParameters = new HashMap<>();
    String logoEnte = StringUtils.firstNonBlank(ente.getDeLogoEnte(), defaultLogo);

    String logoPagoPaPath = getResourcePath("/jasper/images/pagolapa-blu.png");
    reportParameters.put("pagolapa_img", logoPagoPaPath);
    reportParameters.put("ente_img", logoEnte);

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    String dataRichiestaStampa = sdf.format(new Date());

    BigDecimal importoTotalePagato = carrello.getNumEDatiPagImportoTotalePagato();
    String idUnivocoVersamento = carrello.getCodEDatiPagIdUnivocoVersamento();
    String codiceContestoPagamento = carrello.getCodEDatiPagCodiceContestoPagamento();
    String dominio = carrello.getCodEDomIdDominio();
    String stazioneRichiedente = carrello.getCodEDomIdStazioneRichiedente();
    Character esito = carrello.getCodEDatiPagCodiceEsitoPagamento();
    String idMessaggioRicevuta = carrello.getCodEIdMessaggioRicevuta();
    String esitoDescrizione = this.getDescrizioneEsitoCarrello(esito);

    String strDataOraMessaggioRicevuta = Optional.ofNullable(carrello.getCodEDataOraMessaggioRicevuta())
      .map(sdf::format).orElse(null);

    String riferimentoMessaggioRichiesta = carrello.getCodERiferimentoMessaggioRichiesta();

    String strDataOraMessaggioRichiesta = Optional.ofNullable(carrello.getDtRpDataOraMessaggioRichiesta())
      .map(sdf::format).orElse(null);

    //ISTITUTO ATTESTANTE
    String istitAttDemoninazioneAttestante = carrello.getDeEIstitAttDenominazioneAttestante();
    Character istitAttIdUnivAttTipoIdUnivoco = carrello.getCodEIstitAttIdUnivAttTipoIdUnivoco();
    String istitAttIdUnivAttCodiceIdUnivoco = carrello.getCodEIstitAttIdUnivAttCodiceIdUnivoco();
    String istitAttNazioneAttestante = carrello.getCodEIstitAttNazioneAttestante();
    String istitAttProvinciaAttestante = carrello.getDeEIstitAttProvinciaAttestante();
    String istitAttLocalitaAttestante = carrello.getDeEIstitAttLocalitaAttestante();
    String istitAttIndirizzoAttestante = carrello.getDeEIstitAttIndirizzoAttestante();
    String istitAttCivicoAttestante = carrello.getDeEIstitAttCivicoAttestante();
    String istitAttCapAttestante = carrello.getCodEIstitAttCapAttestante();
    String istitAttDenomUnitOperAttestante = carrello.getDeEIstitAttDenomUnitOperAttestante();
    String istitAttCodiceUnitOperAttestante = carrello.getCodEIstitAttCodiceUnitOperAttestante();

    //ENTE BENEFICIARIO
    String enteBenefDenominazioneBeneficiario = carrello.getDeEEnteBenefDenominazioneBeneficiario();
    Character enteBenefIdUnivBenefTipoIdUnivoco = carrello.getCodEEnteBenefIdUnivBenefTipoIdUnivoco();
    String enteBenefIdUnivBenefCodiceIdUnivoco = carrello.getCodEEnteBenefIdUnivBenefCodiceIdUnivoco();
    String enteBenefNazioneBeneficiario = carrello.getCodEEnteBenefNazioneBeneficiario();
    String enteBenefProvinciaBeneficiario = carrello.getDeEEnteBenefProvinciaBeneficiario();
    String enteBenefLocalitaBeneficiario = carrello.getDeEEnteBenefLocalitaBeneficiario();
    String enteBenefIndirizzoBeneficiario = carrello.getDeEEnteBenefIndirizzoBeneficiario();
    String enteBenefCivicoBeneficiario = carrello.getDeEEnteBenefCivicoBeneficiario();
    String enteBenefCapBeneficiario = carrello.getCodEEnteBenefCapBeneficiario();
    String enteBenefDenomUnitOperBeneficiario = carrello.getDeEEnteBenefDenomUnitOperBeneficiario();
    String enteBenefCodiceUnitOperBeneficiario = carrello.getCodEEnteBenefCodiceUnitOperBeneficiario();

    //SOGGETTO VERSANTE
    String soggVersAnagraficaVersante = carrello.getCodESoggVersAnagraficaVersante();
    Character soggVersIdUnivVersTipoIdUnivoco = carrello.getCodESoggVersIdUnivVersTipoIdUnivoco();
    String soggVersIdUnivVersCodiceIdUnivoco = carrello.getCodESoggVersIdUnivVersCodiceIdUnivoco();
    String soggVersEmailVersante = carrello.getDeESoggVersEmailVersante();
    String soggVersNazioneVersante = carrello.getCodESoggVersNazioneVersante();
    String soggVersProvinciaVersante = carrello.getDeESoggVersProvinciaVersante();
    String soggVersLocalitaVersante = carrello.getDeESoggVersLocalitaVersante();
    String soggVersIndirizzoVersante = carrello.getDeESoggVersIndirizzoVersante();
    String soggVersCivicoVersante = carrello.getDeESoggVersCivicoVersante();
    String soggVersCapVersante = carrello.getCodESoggVersCapVersante();

    //SOGGETTO PAGATORE
    String soggPagAnagraficaPagatore = carrello.getCodESoggPagAnagraficaPagatore();
    Character soggPagIdUnivPagTipoIdUnivoco = carrello.getCodESoggPagIdUnivPagTipoIdUnivoco();
    String soggPagIdUnivPagCodiceIdUnivoco = carrello.getCodESoggPagIdUnivPagCodiceIdUnivoco();
    String soggPagEmailPagatore = carrello.getDeESoggPagEmailPagatore();
    String soggPagNazionePagatore = carrello.getCodESoggPagNazionePagatore();
    String soggPagProvinciaPagatore = carrello.getDeESoggPagProvinciaPagatore();
    String soggPagLocalitaPagatore = carrello.getDeESoggPagLocalitaPagatore();
    String soggPagIndirizzoPagatore = carrello.getDeESoggPagIndirizzoPagatore();
    String soggPagCivicoPagatore = carrello.getDeESoggPagCivicoPagatore();
    String soggPagCapPagatore = carrello.getCodESoggPagCapPagatore();

    reportParameters.put("dataRichiestaStampa", dataRichiestaStampa);
    reportParameters.put("importoTotalePagato", Utilities.parseImportoString(importoTotalePagato));
    reportParameters.put("idUnivocoVersamento", idUnivocoVersamento);
    reportParameters.put("codiceContestoPagamento", codiceContestoPagamento);
    reportParameters.put("dominio", dominio);
    reportParameters.put("stazioneRichiedente", stazioneRichiedente);
    reportParameters.put("esito", esitoDescrizione);
    reportParameters.put("idMessaggioRicevuta", idMessaggioRicevuta);
    reportParameters.put("dataOraMessaggioRicevuta", strDataOraMessaggioRicevuta);
    reportParameters.put("riferimentoMessaggioRichiesta", riferimentoMessaggioRichiesta);
    reportParameters.put("dataOraMessaggioRichiesta", strDataOraMessaggioRichiesta);

    //ISTITUTO ATTESTANTE
    reportParameters.put("istitAttDemoninazioneAttestante", istitAttDemoninazioneAttestante);
    reportParameters.put("istitAttIdUnivAttTipoIdUnivoco", istitAttIdUnivAttTipoIdUnivoco);
    reportParameters.put("istitAttIdUnivAttCodiceIdUnivoco", istitAttIdUnivAttCodiceIdUnivoco);
    reportParameters.put("istitAttNazioneAttestante", istitAttNazioneAttestante);
    reportParameters.put("istitAttProvinciaAttestante", istitAttProvinciaAttestante);
    reportParameters.put("istitAttLocalitaAttestante", istitAttLocalitaAttestante);
    reportParameters.put("istitAttIndirizzoAttestante", istitAttIndirizzoAttestante);
    reportParameters.put("istitAttCivicoAttestante", istitAttCivicoAttestante);
    reportParameters.put("istitAttCapAttestante", istitAttCapAttestante);
    reportParameters.put("istitAttDenomUnitOperAttestante", istitAttDenomUnitOperAttestante);
    reportParameters.put("istitAttCodiceUnitOperAttestante", istitAttCodiceUnitOperAttestante);

    //ENTE BENEFICIARIO
    reportParameters.put("enteBenefDenominazioneBeneficiario", enteBenefDenominazioneBeneficiario);
    reportParameters.put("enteBenefIdUnivBenefTipoIdUnivoco", enteBenefIdUnivBenefTipoIdUnivoco);
    reportParameters.put("enteBenefIdUnivBenefCodiceIdUnivoco", enteBenefIdUnivBenefCodiceIdUnivoco);
    reportParameters.put("enteBenefNazioneBeneficiario", enteBenefNazioneBeneficiario);
    reportParameters.put("enteBenefProvinciaBeneficiario", enteBenefProvinciaBeneficiario);
    reportParameters.put("enteBenefLocalitaBeneficiario", enteBenefLocalitaBeneficiario);
    reportParameters.put("enteBenefIndirizzoBeneficiario", enteBenefIndirizzoBeneficiario);
    reportParameters.put("enteBenefCivicoBeneficiario", enteBenefCivicoBeneficiario);
    reportParameters.put("enteBenefCapBeneficiario", enteBenefCapBeneficiario);
    reportParameters.put("enteBenefDenomUnitOperBeneficiario", enteBenefDenomUnitOperBeneficiario);
    reportParameters.put("enteBenefCodiceUnitOperBeneficiario", enteBenefCodiceUnitOperBeneficiario);

    //SOGGETTO VERSANTE
    reportParameters.put("soggVersAnagraficaVersante", soggVersAnagraficaVersante);
    reportParameters.put("soggVersIdUnivVersTipoIdUnivoco", soggVersIdUnivVersTipoIdUnivoco);
    reportParameters.put("soggVersIdUnivVersCodiceIdUnivoco", soggVersIdUnivVersCodiceIdUnivoco);
    reportParameters.put("soggVersEmailVersante", soggVersEmailVersante);
    reportParameters.put("soggVersNazioneVersante", soggVersNazioneVersante);
    reportParameters.put("soggVersProvinciaVersante", soggVersProvinciaVersante);
    reportParameters.put("soggVersLocalitaVersante", soggVersLocalitaVersante);
    reportParameters.put("soggVersIndirizzoVersante", soggVersIndirizzoVersante);
    reportParameters.put("soggVersCivicoVersante", soggVersCivicoVersante);
    reportParameters.put("soggVersCapVersante", soggVersCapVersante);

    //SOGGETTO PAGATORE
    reportParameters.put("soggPagAnagraficaPagatore", soggPagAnagraficaPagatore);
    reportParameters.put("soggPagIdUnivPagTipoIdUnivoco", soggPagIdUnivPagTipoIdUnivoco);
    reportParameters.put("soggPagIdUnivPagCodiceIdUnivoco", soggPagIdUnivPagCodiceIdUnivoco);
    reportParameters.put("soggPagEmailPagatore", soggPagEmailPagatore);
    reportParameters.put("soggPagNazionePagatore", soggPagNazionePagatore);
    reportParameters.put("soggPagProvinciaPagatore", soggPagProvinciaPagatore);
    reportParameters.put("soggPagLocalitaPagatore", soggPagLocalitaPagatore);
    reportParameters.put("soggPagIndirizzoPagatore", soggPagIndirizzoPagatore);
    reportParameters.put("soggPagCivicoPagatore", soggPagCivicoPagatore);
    reportParameters.put("soggPagCapPagatore", soggPagCapPagatore);
    /*
     * La variabile determina se la stampa della RT è in ambiente di collaudo o di esercizio.
     * Di modo da applicare o meno lo stile grafico che le distingue.
     */
    reportParameters.put("prodEnvironment", reportsProdEnvironment);
    reportParameters.put("collaudo_img", getResourcePath("/jasper/images/background-rt-collaudo.png"));

    //Lista di Mappe ciascuna rappresentante un singolo dovuto pagato
    List<DovutoElaborato> dovutoElaboratoList = dovutoElaboratoService.getByCarrello(carrello);
    Collection<Map<String, ?>> listDovutiPagatiMap = manageDovutiPagatiList(dovutoElaboratoList, ente.getCodIpaEnte());
    JRBeanCollectionDataSource dovutiPagatiMapCollectionDataSource = new JRBeanCollectionDataSource(listDovutiPagatiMap);

    //ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);

    //ReportUtilities.exportToPdf(reportFilePath, reportParameters, dovutiPagatiMapCollectionDataSource, reportStream);
    JasperReport jasperReport = (JasperReport) JRLoader.loadObject(new URL(getResourcePath(reportFilePath))); //JasperCompileManager.compileReport(reportFilePath);

    //if (dovutiPagatiMapCollectionDataSource == null) {
    //  dovutiPagatiMapCollectionDataSource  = new JREmptyDataSource();
    //}

    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParameters, dovutiPagatiMapCollectionDataSource  );
    //JasperExportManager.exportReportToPdfStream(jasperPrint, reportStream);

    JRPdfExporter exporter = new JRPdfExporter();
    SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
    exporter.setConfiguration(configuration);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    SimpleOutputStreamExporterOutput simpleOutputStreamExporterOutput = new SimpleOutputStreamExporterOutput(baos);
    exporter.setExporterOutput(simpleOutputStreamExporterOutput);

    SimpleExporterInput simpleExporterInput = new SimpleExporterInput(jasperPrint);
    exporter.setExporterInput(simpleExporterInput);

    exporter.exportReport();

    return baos;

  }

  private String getResourcePath(String resourcePath) {
    try {
      if(resourcePath.startsWith("/"))
        resourcePath = "classpath:"+resourcePath.substring(1);
      return resourceLoader.getResource(resourcePath).getURL().toExternalForm();
    } catch (Exception ex) {
      log.warn("Cannot open file: " + resourcePath+" due to error: "+ex.toString());
      return "";
    }
  }


  /**
   * @param esito
   * @return
   */
  private String getDescrizioneEsitoCarrello(Character esito) {
    String descrizioneEsito = null;
    if (esito != null) {

      String esitoStr = esito.toString();

      if ("0".equals(esitoStr)) {
        descrizioneEsito = "Pagamento eseguito";
      } else if ("1".equals(esitoStr)) {
        descrizioneEsito = "Pagamento non eseguito";
      } else if ("2".equals(esitoStr)) {
        descrizioneEsito = "Pagamento parzialmente eseguito";
      } else if ("3".equals(esitoStr)) {
        descrizioneEsito = "Decorrenza termini";
      } else if ("4".equals(esitoStr)) {
        descrizioneEsito = "Decorrenza termini parziale";
      }
    }
    return descrizioneEsito;
  }

  private Collection<Map<String, ?>> manageDovutiPagatiList(List<DovutoElaborato> dovutoPagatoList, String codIpaEnte) {

    Collection<Map<String, ?>> collectionOfMapDovutiPagati = new ArrayList<>();
    SimpleDateFormat DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy");

    dovutoPagatoList.forEach(dovutoPagato -> {
      Map<String, String> currentMap = new HashMap<>();
      currentMap.put("IUD", dovutoPagato.getCodIud());

      String causaleVersamento = Optional.ofNullable(dovutoPagato.getDeRpDatiVersDatiSingVersCausaleVersamentoAgid())
        .filter(Pattern.compile("^.*/TXT/.*$").asPredicate())
        .map(s -> Pattern.compile("(.*?\\/TXT/)").matcher(s).replaceAll(""))
        .orElse(dovutoPagato.getDeRpDatiVersDatiSingVersCausaleVersamento());
      if (causaleVersamento.length() > 50)
        causaleVersamento = Utilities.getTruncatedAt(50).apply(causaleVersamento).concat("...");

      currentMap.put("CAUSALE_VERSAMENTO", causaleVersamento);
      currentMap.put("IMPORTO_PAGATO", Utilities.parseImportoString(dovutoPagato.getNumEDatiPagDatiSingPagSingoloImportoPagato()));
      if (dovutoPagato.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento() != null) {
        currentMap.put("DATA_PAGAMENTO", DATEFORMAT.format(dovutoPagato.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento()));
      }
      currentMap.put("IDENTIFICATIVO_UNIV_RISCOSSIONE", dovutoPagato.getCodEDatiPagDatiSingPagIdUnivocoRiscoss());
      currentMap.put("IBAN_ACCREDITO", dovutoPagato.getCodRpDatiVersDatiSingVersIbanAccredito());
      currentMap.put("BIC_ACCREDITO", dovutoPagato.getCodRpDatiVersDatiSingVersBicAccredito());
      currentMap.put("DATI_SPECIFICI_RISCOSSIONE", dovutoPagato.getDeEDatiPagDatiSingPagDatiSpecificiRiscossione());
      currentMap.put("IBAN_APPOGGIO", dovutoPagato.getCodRpDatiVersDatiSingVersIbanAppoggio());
      currentMap.put("BIC_APPOGGIO", dovutoPagato.getCodRpDatiVersDatiSingVersBicAppoggio());

      var deTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(dovutoPagato.getCodTipoDovuto(), codIpaEnte, false)
          .map(EnteTipoDovuto::getDeTipo).orElse("");
      if (causaleVersamento.length() > 30)
        deTipoDovuto = Utilities.getTruncatedAt(30).apply(deTipoDovuto).concat("...");
      currentMap.put("TIPO_DOVUTO", deTipoDovuto);

      if (dovutoPagato.getNumEDatiPagDatiSingPagSingoloImportoPagato().doubleValue() > 0) {
        var commissioni = Optional.ofNullable(dovutoPagato.getNumEDatiPagDatiSingPagCommissioniApplicatePsp())
            .map(Utilities::parseImportoString).orElse("---");
        currentMap.put("COMMISSIONI_APPLICATE_PSP", commissioni);
      }

      collectionOfMapDovutiPagati.add(currentMap);
    });

    return collectionOfMapDovutiPagati;
  }

}
