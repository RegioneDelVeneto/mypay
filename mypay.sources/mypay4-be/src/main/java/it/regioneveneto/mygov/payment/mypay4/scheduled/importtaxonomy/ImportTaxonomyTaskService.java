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
package it.regioneveneto.mygov.payment.mypay4.scheduled.importtaxonomy;

import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.model.Tassonomia;
import it.regioneveneto.mygov.payment.mypay4.service.ImportTaxonomyHandlerService;
import it.regioneveneto.mygov.payment.mypay4.service.MailService;
import it.regioneveneto.mygov.payment.mypay4.service.TassonomiaService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue= ImportTaxonomyTaskApplication.NAME)
public class ImportTaxonomyTaskService {

	@Autowired
	ImportTaxonomyHandlerService importTaxonomyHandlerService;

	@Autowired
	TassonomiaService tassonomiaService;

	@Autowired
	MailService mailService;

	private long counter = 0;
	@Value("${task.importTaxonomy.mail.range.notification}")
	private int notificationRangeDays;
	private final Predicate<Long> evaluateRange = n -> n % notificationRangeDays == 0;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private static final String BATCH_USER = "BATCH_USER";

	@Transactional(propagation = Propagation.NEVER)
	public void importTaxonomy() {
		StopWatch timing = new StopWatch("ImportTaxonomyTask");
		timing.start();
		log.info("ImportTaxonomy start [{}]", ++counter);
		var apiData = importTaxonomyHandlerService.checkForNewData(null);
		if (apiData.isPresent())
			importTaxonomyHandlerService.performUpdate(BATCH_USER, apiData.get());

		Map<String, String> notificationMap = tassonomiaService.getAllExpiring()
				.stream()
				.filter(item -> evaluateRange.test(ChronoUnit.DAYS.between(LocalDate.now(), Utilities.toLocalDateTime(item.getDtFineValidita()))))
				.collect(Collectors.toMap(Tassonomia::getCodiceTassonomico, t -> dateFormat.format(t.getDtFineValidita())));
		if (!notificationMap.isEmpty()) {
			var params = buildParamContent(notificationMap);
			log.debug("sending mail to admin due to taxonomy expiring date");
			mailService.sendMailExpiringTaxonomy(params);
		}
		log.info("ImportTaxonomy stop [{}]", counter);
		timing.stop();
		log.debug("ImportTaxonomyTaskService :: importTaxonomy :: End in {} seconds", timing.getTotalTimeSeconds());
	}

	private Map<String, String> buildParamContent(Map<String, String> map) {
		final String tableTemplate ="<tr><td>%s</td><td>%s</td></tr>";
		StringBuilder htmlBuilder = new StringBuilder("<table border=\"1\">")
				.append(String.format(tableTemplate, "codice tassonomico", "data scadenza"));
		map.forEach((key, value) -> htmlBuilder.append(String.format(tableTemplate, key, value)));
		return Map.of("elements", htmlBuilder.append("</table>").toString());
	}
}
