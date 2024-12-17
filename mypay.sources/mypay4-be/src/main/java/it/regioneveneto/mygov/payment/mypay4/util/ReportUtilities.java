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
package it.regioneveneto.mygov.payment.mypay4.util;

import it.regioneveneto.mygov.payment.mypay4.service.JasperService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ReportUtilities {

  private static final Logger log = LoggerFactory.getLogger(ReportUtilities.class);

  public static ByteArrayOutputStream generateAvviso(JasperReport jasperReport, Map<String, Object> parameters) throws JRException {
    JasperPrint jasperPrint = generateLocaledReport("IT", jasperReport, parameters);
    if (parameters.get("lingua_aggiuntiva") != null) {
      if (parameters.get("lingua_aggiuntiva").equals("EN")) {
        JasperPrint jasperPrintEN = generateLocaledReport("EN", jasperReport, parameters);
        jasperPrint.addPage(jasperPrintEN.getPages().get(0));
      } else if (parameters.get("lingua_aggiuntiva").equals("DE")) {
        JasperPrint jasperPrintDE = generateLocaledReport("DE", jasperReport, parameters);
        jasperPrint.addPage(jasperPrintDE.getPages().get(0));
      }
    }

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

  private static JasperPrint generateLocaledReport(String locale, JasperReport jasperReport, Map<String, Object> parameters) throws JRException {
    Map<String, Object> clonedParams = new HashMap<String, Object>(parameters);
    if (locale.equals("IT")) {
      setLabels("IT", clonedParams);
    } else if (locale.equals("EN")) {
      setLabels("EN", clonedParams);
    } else if (locale.equals("DE")) {
      setLabels("DE", clonedParams);
    }
    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, clonedParams);
    for (JRStyle style : jasperPrint.getStyles()) {
      style.setPdfEmbedded(true);
      if ("RobotoMono_Bold".equalsIgnoreCase(style.getName())) {
        style.setPdfFontName((String)parameters.get("font_path_RobotoMono-Bold"));
      }
      if ("RobotoMono_Regular".equalsIgnoreCase(style.getName())) {
        style.setPdfFontName((String)parameters.get("font_path_RobotoMono-Regular"));
      }
      if ("Titillium_Black".equalsIgnoreCase(style.getName())) {
        style.setPdfFontName((String)parameters.get("font_path_TitilliumWeb-Black"));
      }
      if ("Titillium_Bold".equalsIgnoreCase(style.getName())) {
        style.setPdfFontName((String)parameters.get("font_path_TitilliumWeb-Bold"));
      }
      if ("Titillium_Regular".equalsIgnoreCase(style.getName())) {
        style.setPdfFontName((String)parameters.get("font_path_TitilliumWeb-Regular"));
      }
    }
    return jasperPrint;
  }

  private static void setLabels(String locale, Map<String, Object> parameters) {
    try {
      String propFile = String.format("/jasper/locales/jasper_messages_%s.properties", locale);
      log.info("Read from the properties file. File Name: " + propFile);
      InputStream is = JasperService.class.getResourceAsStream(propFile);
      Properties prop = new Properties();
      prop.load(is);
      is.close();
      parameters.put("L_BANCHE_E_ALTRI_CANALI", prop.getProperty("L_BANCHE_E_ALTRI_CANALI"));
      parameters.put("L_BANCHE_E_ALTRI_CANALI_DESC", prop.getProperty("L_BANCHE_E_ALTRI_CANALI_DESC"));
      parameters.put("L_BOLLETTINO_POSTALE_PAGABILE", prop.getProperty("L_BOLLETTINO_POSTALE_PAGABILE"));
      parameters.put("L_BOLLETTINO_POSTALE_PA", prop.getProperty("L_BOLLETTINO_POSTALE_PA"));
      parameters.put("L_COD_FISCALE_ENTE_CREDITORE", prop.getProperty("L_COD_FISCALE_ENTE_CREDITORE"));
      parameters.put("L_COD_FISCALE", prop.getProperty("L_COD_FISCALE"));
      parameters.put("L_CODICE_AVVISO", prop.getProperty("L_CODICE_AVVISO"));
      parameters.put("L_CODICE_CBILL", prop.getProperty("L_CODICE_CBILL"));
      parameters.put("L_DESTINATARIO", prop.getProperty("L_DESTINATARIO"));
      parameters.put("L_DESTINATARIO_AVVISO", prop.getProperty("L_DESTINATARIO_AVVISO"));
      parameters.put("L_DOVE_PAGARE", prop.getProperty("L_DOVE_PAGARE"));
      parameters.put("L_ENTE_CREDITORE", prop.getProperty("L_ENTE_CREDITORE"));
      parameters.put("L_ENTE_CREDITORE_MIN", prop.getProperty("L_ENTE_CREDITORE_MIN"));
      parameters.put("L_ENTRO_IL", prop.getProperty("L_ENTRO_IL"));
      parameters.put("L_EURO", prop.getProperty("L_EURO"));
      parameters.put("L_INTESTATO_A", prop.getProperty("L_INTESTATO_A"));
      parameters.put("L_IMPORTO_E_AGGIORNATO_AUTOMATICAMENTE", prop.getProperty("L_IMPORTO_E_AGGIORNATO_AUTOMATICAMENTE"));
      parameters.put("L_LISTA_DEI_CANALI_DI_PAGAMENTO", prop.getProperty("L_LISTA_DEI_CANALI_DI_PAGAMENTO"));
      parameters.put("L_OGGETTO_DEL_PAGAMENTO", prop.getProperty("L_OGGETTO_DEL_PAGAMENTO"));
      parameters.put("L_PAGA_SUL_SITO", prop.getProperty("L_PAGA_SUL_SITO"));
      String L_PAGA_SUL_SITO_DESC = ((String) parameters.get("poste")).equalsIgnoreCase("ok") ?
          prop.getProperty("L_PAGA_SUL_SITO_DESC_POSTOK") : prop.getProperty("L_PAGA_SUL_SITO_DESC_POSTKO");
      parameters.put("L_PAGA_SUL_SITO_DESC", L_PAGA_SUL_SITO_DESC);
      parameters.put("L_PAGA_SUL_TERRITORIO", prop.getProperty("L_PAGA_SUL_TERRITORIO"));
      String L_PAGA_SUL_TERRITORIO_DESC = ((String) parameters.get("poste")).equalsIgnoreCase("ok") ?
          prop.getProperty("L_PAGA_SUL_TERRITORIO_DESC_POSTOK") : prop.getProperty("L_PAGA_SUL_TERRITORIO_DESC_POSTKO");
      parameters.put("L_PAGA_SUL_TERRITORIO_DESC", L_PAGA_SUL_TERRITORIO_DESC);
      parameters.put("L_PUOI_PAGARE", prop.getProperty("L_PUOI_PAGARE"));
      parameters.put("L_QUANTO_E_QUANDO_PAGARE", prop.getProperty("L_QUANTO_E_QUANDO_PAGARE"));
      parameters.put("L_RATA_UNICA", prop.getProperty("L_RATA_UNICA"));
      parameters.put("L_STATIC_TEXT", prop.getProperty("L_STATIC_TEXT"));
      parameters.put("L_SUL_CC_N", prop.getProperty("L_SUL_CC_N"));
      parameters.put("L_TIPO", prop.getProperty("L_TIPO"));
      parameters.put("L_UTILIZZA_LA_PORZIONE_DI_AVVISO", prop.getProperty("L_UTILIZZA_LA_PORZIONE_DI_AVVISO"));
    } catch (IOException ex) {
      log.error(ex.getMessage());
      ex.printStackTrace();
    }

  }
}
