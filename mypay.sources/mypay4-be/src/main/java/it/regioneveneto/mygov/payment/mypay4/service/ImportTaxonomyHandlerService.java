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
import it.regioneveneto.mygov.payment.mypay4.dto.PagoPaApiTaxonomyTo;
import it.regioneveneto.mygov.payment.mypay4.dto.TassonomiaTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
public class ImportTaxonomyHandlerService {

  @Autowired
  FlussoTassonomiaService flussoTassonomiaService;

  @Autowired
  PagoPaApiService pagoPaApiService;

  @Autowired
  TassonomiaService tassonomiaService;

  @Autowired
  Jackson2ObjectMapperBuilder mapperBuilder;

  @Autowired
  MailService mailService;

  @Autowired
  MyBoxService myBoxService;

  @Value("${mypay.path.import.tassonomia}")
  private String pathTassonomia;

  public Optional<PagoPaApiTaxonomyTo> checkForNewData(String hash) {
    try {
      var apiData = pagoPaApiService.getJsonTaxonomy();
      log.debug("PagoPaApiTaxonomyTo: {}", apiData);
      if (StringUtils.isBlank(hash)) {
        log.debug("select last");
        var lastUpload = flussoTassonomiaService.getLast();
        hash = lastUpload.getHash();
      }
      if (apiData.getHash().equals(hash)) {
        log.info("current taxonomy is up to date");
        return Optional.empty();
      }
      log.info("current taxonomy is out of date");
      return Optional.of(apiData);
    } catch (Exception e) {
      log.error("error due to retrieve JSON from api [{}]", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void performUpdate(String user, PagoPaApiTaxonomyTo pagoPaApiTaxonomyTo) {
    log.info("Perfoming update");
    try {
      Map<String, TassonomiaTo> currentTaxonomyMap = tassonomiaService.getAllMapped();
      Map<String, TassonomiaTo> jsonMap = pagoPaApiService.transformJson(pagoPaApiTaxonomyTo.getBody());

      StringBuilder htmlRowsTableFormat = new StringBuilder();
      jsonMap.forEach((key, value) -> {
        var optionalCurrentItemMatch = Optional.ofNullable(currentTaxonomyMap.get(key));
        htmlRowsTableFormat.append(buildHtmlRowsTableFormat(key, optionalCurrentItemMatch.orElse(new TassonomiaTo()), value));
        TassonomiaTo taxonomyToStore = optionalCurrentItemMatch.map(obj -> value.toBuilder().id(obj.getId()).build()).orElse(value);
        tassonomiaService.upsert(taxonomyToStore);
      });

      var csvStringFormat = pagoPaApiService.getCsvFile();
      String filename = String.format("%s_%s_%s", "tassonomia", pagoPaApiTaxonomyTo.getVersion(), pagoPaApiTaxonomyTo.getCreated().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
      var aFileInArchive = myBoxService.saveFile("tmp_taxonomy", String.format("%s.%s", filename, "csv"), csvStringFormat);
      String zipFileName = String.format("%s.%s", filename, "zip");
      var zippedFile = myBoxService.archiveFileToZip(aFileInArchive, pathTassonomia + File.separator + LocalDate.now().getYear(), zipFileName);
      flussoTassonomiaService.onUploadFile(user, zippedFile, jsonMap.size(), pagoPaApiTaxonomyTo);

      var html = buildHtmlTableFormat(htmlRowsTableFormat.toString());
      var params = Map.of("elements", html);
      log.info("sending mail to admin due to notify taxonomy changes");
      mailService.sendMailTaxonomyChanges(params);

      if (aFileInArchive.delete())
        log.debug("deleted temp file [{}]", aFileInArchive.getName());
    } catch (Exception e) {
      log.error("error due to perfom update [{}]", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private String buildHtmlRowsTableFormat(String key, TassonomiaTo oldOne, TassonomiaTo newOne) {
    ObjectMapper mapper = mapperBuilder.build();
    Map<String, String> mapOld = mapper.convertValue(oldOne, Map.class);
    Map<String, String> mapNew = mapper.convertValue(newOne, Map.class);
    Function<String, String> rowTemplateFunction = s -> String.format("<td>%s</td>", s);
    StringBuilder htmlBuilder = new StringBuilder(String.format("<tr><th>%s</th></tr>", key))
        .append("<tr><th>precedente</th>");
    mapOld.values()
        .stream()
        .map(StringUtils::defaultString)
        .map(rowTemplateFunction)
        .forEach(htmlBuilder::append);
    htmlBuilder.append("</tr>")
        .append("<tr><th>attuale</th>");
    mapNew.forEach((k, v) -> {
      var result = mapOld.containsKey(k) && mapOld.get(k).equals(v) ? "" : v;
      htmlBuilder.append(rowTemplateFunction.apply(result));
    });
    return htmlBuilder.append("</tr>").toString();
  }

  private String buildHtmlTableFormat(String rows) {
    Function<String, String> headerRowTemplateFunction = s -> String.format("<td><b>%s</b></td>", StringUtils.defaultString(s, ""));
    StringBuilder htmlBuilder = new StringBuilder("<table border=\"1\">")
        .append("<tr><td></td>");
    Arrays.stream(TassonomiaTo.Fields.values())
        .map(TassonomiaTo.Fields::fieldName)
        .map(headerRowTemplateFunction)
        .forEach(htmlBuilder::append);
    return htmlBuilder.append("</tr>")
        .append(rows)
        .append("</table>")
        .toString();
  }

}
