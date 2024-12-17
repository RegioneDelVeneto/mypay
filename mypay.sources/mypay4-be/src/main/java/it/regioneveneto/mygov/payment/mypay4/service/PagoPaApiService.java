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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.regioneveneto.mygov.payment.mypay4.dto.PagoPaApiTaxonomyTo;
import it.regioneveneto.mygov.payment.mypay4.dto.TassonomiaTo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PagoPaApiService {

	@Value("${api.platform.pagopa.baseUrl}")
	private String baseUrlGithub;

	@Value("${api.platform.pagopa.taxonomy.service.context}")
	private String taxonomyPathContext;

	@Value("${api.platform.pagopa.taxonomy.service.csv.param}")
	private String csvParam;

	private final ObjectMapper objectMapper;

	public PagoPaApiService(Jackson2ObjectMapperBuilder mapperBuilder) {
		this.objectMapper = mapperBuilder.build();
	}

	@SneakyThrows
	public HttpResponse<String> apiPagoPaHandler(String url) {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(url))
				.GET()
				.build();
		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	}


	public PagoPaApiTaxonomyTo getJsonTaxonomy() {
		var response = apiPagoPaHandler(baseUrlGithub + taxonomyPathContext);
		return PagoPaApiTaxonomyTo.builder()
				.uuid(response.headers().map().get("uuid").get(0))
				.version(response.headers().map().get("version").get(0))
				.created(LocalDateTime.parse(response.headers().map().get("created").get(0), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))
				.body(response.body())
				.hash(DigestUtils.md5Hex(response.body()))
				.build();
	}

	public String getCsvFile(){
		var response = apiPagoPaHandler(baseUrlGithub + taxonomyPathContext + csvParam);
		return response.body();
	}

	@SneakyThrows
	public Map<String, TassonomiaTo> transformJson(String json) {
		var tassonomiaToList = objectMapper.readValue(json, new TypeReference<List<TassonomiaTo>>(){});
		return tassonomiaToList.stream().collect(Collectors.toMap(TassonomiaTo::getDatiSpecificiIncasso, Function.identity()));
	}
}
