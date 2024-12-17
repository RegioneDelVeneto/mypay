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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.xml.transform.StringResult;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
public class XmlUtils {

  private XmlUtils() {
    throw new IllegalStateException("Utility class");
  }

  private static final String XSLT_IDENTITY_MINIFY =
          "<?xml version=\"1.0\"?>" +
          "<xsl:stylesheet version=\"1.0\"" +
          "  xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
          "  <xsl:output indent=\"no\" omit-xml-declaration=\"yes\" />" +
          "  <xsl:strip-space elements=\"*\"/>" +
          "  <xsl:template match=\"@*|node()\">" +
          "    <xsl:copy>" +
          "      <xsl:apply-templates select=\"@*|node()\"/>" +
          "    </xsl:copy>" +
          "  </xsl:template>" +
          "</xsl:stylesheet>";

  private static final ThreadLocal<Optional<Transformer>> xlstTransformer = ThreadLocal.withInitial(() -> {
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      Source xslt = new StreamSource(IOUtils.toInputStream(XSLT_IDENTITY_MINIFY, StandardCharsets.UTF_8));
      return Optional.of(factory.newTransformer(xslt));
    } catch(Exception e){
      log.warn("error initializing javax.xml.transform.Transformer", e);
      return Optional.empty();
    }
  });

  public static void clean(){
    XmlUtils.xlstTransformer.remove();
  }

  public static String minifyXml(String xmlIn){
    return XmlUtils.xlstTransformer.get().map(transformer -> {
      try {
        Source text = new StreamSource(IOUtils.toInputStream(xmlIn, StandardCharsets.UTF_8));
        StringResult result = new StringResult();
        transformer.transform(text, result);
        return result.toString();
      } catch(Exception e){
        log.trace("cannot minify xml", e);
        return xmlIn;
      } }).orElse(xmlIn);
  }

}
