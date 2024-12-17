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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MailService {

  @Value("${pa.mail.to.maintainer.addresses}")
  private String[] maintainerToAddresses;

  @Value("${pa.mail.from.name.custom}")
  private String mailFromNameCustom;

  @Autowired
  private Environment templates;

  @Autowired
  private AsyncSendMailService sendMailService;



  public void sendMailAddressValidationMail(String to, String code, String nomeEnte){
    Map<String, String> params = Map.of(
        "nomeEnte", StringUtils.firstNonBlank(nomeEnte, "MyPay"),
        "to", to,
        "pinCode", code
    );
    String templateName = "mail-address-validation";
    String customEmailFromName = null;
    if(StringUtils.isNotBlank(nomeEnte)) {
      customEmailFromName = StringSubstitutor.replace(mailFromNameCustom, Map.of("nomeEnte", nomeEnte), "{", "}");
    }
    sendMailFromTemplate(new String[]{to}, null, params, templateName, customEmailFromName);
  }

  public void sendUserMailValidationMail(String to, String code, String nomeEnte){
    Map<String, String> params = Map.of(
        "nomeEnte", StringUtils.firstNonBlank(nomeEnte, "MyPay"),
        "to", to,
        "pinCode", code
    );
    String templateName = "user-mail-validation";
    String customEmailFromName = null;
    if(StringUtils.isNotBlank(nomeEnte)) {
      customEmailFromName = StringSubstitutor.replace(mailFromNameCustom, Map.of("nomeEnte", nomeEnte), "{", "}");
    }
    sendMailFromTemplate(new String[]{to}, null, params, templateName, customEmailFromName);
  }

  public void sendMailEsitoPagamento(String to, String[] cc, Map<String, String> params) {
    String templateName = "mail-esito-pagamento";
    sendMailFromTemplate(new String[]{to}, cc, params, templateName);
  }

  public void sendMailNotificaBackOffice(Map<String, String> params) {
    String templateName = "mail-notifica-backoffice";
    sendMailFromTemplate(maintainerToAddresses, null, params, templateName);
  }

  public void sendMailImportFlussoOk(String to[], String[] cc, Map<String, String> params) {
    String templateName = "mail-importFlusso-ok";
    sendMailFromTemplate(to, cc, params, templateName);
  }

  public void sendMailImportFlussoScarti(String to[], String[] cc, Map<String, String> params) {
    String templateName = "mail-importFlusso-scarti";
    sendMailFromTemplate(to, cc, params, templateName);
  }

  public void sendMailImportFlussoError(String to[], String[] cc, Map<String, String> params) {
    String templateName = "mail-importFlusso-error";
    sendMailFromTemplate(to, cc, params, templateName);
  }

  public void sendMailExportDovutiOk(String to[], String[] cc, Map<String, String> params) {
    String templateName = "mail-exportDovuti-ok";
    sendMailFromTemplate(to, cc, params, templateName);
  }

  public void sendMailExpiringTaxonomy(Map<String, String> params) {
    String templateName = "mail-expiring-taxonomy";
    sendMailFromTemplate(maintainerToAddresses, null, params, templateName);
  }

  public void sendMailTaxonomyChanges(Map<String, String> params) {
    String templateName = "mail-taxonomy-changes";
    sendMailFromTemplate(maintainerToAddresses, null, params, templateName);
  }

  private void sendMailFromTemplate(String[] to, String[] cc, Map<String, String> params, String templateName){
    sendMailFromTemplate(to, cc, params, templateName, null);
  }

  private void sendMailFromTemplate(String[] to, String[] cc, Map<String, String> params, String templateName, String customEmailFromName){
    Assert.isTrue(templates.containsProperty("template."+templateName+".subject"), "Invalid email template (missing subject) "+templateName);
    Assert.isTrue(templates.containsProperty("template."+templateName+".body"), "Invalid email template (missing body) "+templateName);
    String subject = StringSubstitutor.replace(templates.getRequiredProperty("template."+templateName+".subject"), params, "{", "}");
    String text = StringSubstitutor.replace(templates.getRequiredProperty("template."+templateName+".body"), params, "{", "}");
    sendMailService.sendMail(to, cc, subject, text, customEmailFromName);
  }

}

@Service
@Slf4j
class AsyncSendMailService {

  @Value("${async.sendMail.corePoolSize:2}")
  String corePoolSize;
  @Value("${async.sendMail.maxPoolSize:10}")
  String maxPoolSize;
  @Value("${async.sendMail.queueCapacity:500}")
  String queueCapacity;

  @Value("${pa.mail.from.name}")
  private String emailFromName;
  @Value("${pa.mail.from.address}")
  private String emailFromAddress;

  @Value("${pa.mail.whitelist:}")
  private List<String> emailWhitelistAddresses;

  @Autowired
  private JavaMailSender emailSender;

  @PostConstruct
  public void runAfterObjectCreated() {
    //put whitelist in lower case
    emailWhitelistAddresses = emailWhitelistAddresses
        .stream()
        .map(String::toLowerCase)
        .collect(Collectors.toUnmodifiableList());
  }

  private String[] filterRecipients(String[] addresses, String type){
    if(!emailWhitelistAddresses.isEmpty() && ArrayUtils.isNotEmpty(addresses)) {
      Map<Boolean, List<String>> groups = Arrays.stream(addresses)
          .map(String::toLowerCase)
          .collect(Collectors.partitioningBy(emailWhitelistAddresses::contains));
      addresses = groups.get(true).toArray(ArrayUtils.EMPTY_STRING_ARRAY);
      if (!groups.get(false).isEmpty())
        log.info("removed mail recipients ({}) not in whitelist: {}", type, StringUtils.joinWith("; ", groups.get(false)));
    }
    return addresses;
  }

  @Async("SendMailTaskExecutor")
  @Retryable(value = MailException.class, maxAttemptsExpression = "${async.sendMail.retry.maxAttempts}",
      //listeners = {"sendMailService"},
      backoff = @Backoff(random = true, delayExpression = "${async.sendMail.retry.delay}",
          maxDelayExpression = "${async.sendMail.retry.maxDelay}", multiplierExpression = "${async.sendMail.retry.multiplier}"))
  public void sendMail(String[] to, String[] cc, String subject, String htmlText, String customEmailFromName) {

    int retry = RetrySynchronizationManager.getContext().getRetryCount();
    if(retry>0)
      log.info("send mail, retry #{}", retry);

    to = filterRecipients(to, "TO");
    cc = filterRecipients(cc, "CC");

    if(ArrayUtils.isEmpty(to) && ArrayUtils.isEmpty(cc)){
      log.warn("not sending mail because all recipients are blacklisted, subject: {} - content: {}", subject, htmlText.replaceAll("[\\r\\n]+", " "));
      return;
    } else if(ArrayUtils.isEmpty(to) && ArrayUtils.isNotEmpty(cc)){
      log.warn("since all TO recipients are blacklisted, CC recipients moved to TO");
      to = cc;
      cc = null;
    }

    final String[] finalTo = to;
    final String[] finalCC = cc;

    emailSender.send( mimeMessage -> {
      MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      message.setFrom(emailFromAddress, StringUtils.firstNonBlank(customEmailFromName, this.emailFromName));
      message.setTo(finalTo);
      if(ArrayUtils.isNotEmpty(finalCC))
        message.setCc(finalCC);
      message.setSubject(subject);
      String plainText = Jsoup.clean(htmlText, "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));
      message.setText(plainText, htmlText);
      log.debug("sending mail message. TO: {} - CC: {} - Subject: {} - Text: \n{}", finalTo, finalCC, subject, plainText);
    } );
  }

  @Recover
  private void recover(MailException e, String[] to, String[] cc, String subject, String htmlText){
    log.warn("cannot send mail to:"+Arrays.toString(to)+" - subject:"+subject+". Aborting", e);
    //TODO write fail to db or queue for retry, in case
  }

  @Bean("SendMailTaskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Integer.parseInt(corePoolSize));
    executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
    executor.setQueueCapacity(Integer.parseInt(queueCapacity));
    executor.setThreadNamePrefix("MyPay4SendMail-");
    executor.initialize();
    return executor;
  }
}
