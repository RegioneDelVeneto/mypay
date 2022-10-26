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

import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/*
 * Utility class used to trim all strings during marshalling/unmarshalling of SOAP messages.
 * It is referenced in the .xjb files of single WSDL.
 *
 * Warning: due to internal working of JAXB Gradle plugin, it is necessary that during compilation phase
 * a class with this signature exists. Therefore a jar called mypay4-be-XmlAdapter.jar has been put into
 * folder 'libs'.
 * Anyway, at runtime, that jar is not used and the compiled class in the 'main' MyPay4 jar is used. So
 * there is no need to update the mypay4-be-XmlAdapter.jar in case a modification is made on this class,
 * for it to be seen at runtime.
 *
 */
@Slf4j
public class TrimStringXmlAdapter extends XmlAdapter<String, String> {

  @Override
  public String marshal(String text) {
    return this.trim("marshal", text);
  }

  @Override
  public String unmarshal(String v) {
    return this.trim("unmarshal", v);
  }

  private String trim(String oper, String text){
    if(text==null)
      return null;
    String trimmed = text.trim();
    if(log.isInfoEnabled() && trimmed.length()!=text.length())
      log.info("{}: trim [{}] -> [{}]", oper, text, trimmed);
    return trimmed;
  }
}