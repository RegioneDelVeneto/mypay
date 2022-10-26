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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class BackOfficeService {

  @Autowired
  MailService mailService;

  @Autowired
  Jackson2ObjectMapperBuilder mapperBuilder;

  private static final String tableTemplate ="<tr><td>%s</td><td>%s</td><td>%s</td></tr>";

  public <T> String buildHtmlTableOnFieldsChanged(T oldOne, T newOne) {
    ObjectMapper mapper = mapperBuilder.build();
    Map<String, T> mapOld = mapper.convertValue(oldOne, Map.class);
    Map<String, T> mapNew = mapper.convertValue(newOne, Map.class);

    MapDifference<String, T> differences = Maps.difference(mapOld, mapNew);

    if(!differences.areEqual()) {
      StringBuilder htmlBuilder = new StringBuilder()
          .append("<table>")
          .append(String.format(tableTemplate, "Campo", "Vecchio valore", "nuovo valore"));
      differences.entriesDiffering().entrySet()
          .forEach(e -> htmlBuilder
              .append(String.format(tableTemplate, e.getKey(), e.getValue().leftValue(), e.getValue().rightValue())));
      return htmlBuilder.append("</table>").toString();
    }
    return Constants.EMPTY;
  }

  public <T> void sendReportToSystemMaintainer(UserWithAdditionalInfo user, String nomeEnte, String tipoGestione, T oldOne, T newOne) {
    if (!user.isSysAdmin()) {
      var changedElements = buildHtmlTableOnFieldsChanged(oldOne, newOne);
      if (!changedElements.isEmpty()) {
        var map = Map.of(
            "ente", nomeEnte,
            "nome", user.getFirstName() + " " + user.getFamilyName(),
            "codFiscale", user.getCodiceFiscale(),
            "kind", tipoGestione,
            "elements", changedElements
        );
        mailService.sendMailNotificaBackOffice(map);
      }
    }
  }
}
