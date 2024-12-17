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
package it.regioneveneto.mygov.payment.mypay4.service.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AppErrorService {
  private static final String SEP = "-";
  private static final ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmss"));

  //hostname on most linux env, computername on windows
  @Value("${HOSTNAME:${COMPUTERNAME:}}")
  private String hostnameEnvVariable;

  private Optional<String> hostname;
  @Getter
  private Optional<String> serverId;


  @PostConstruct
  public void runAfterObjectCreated() {
    String hostname = null;
    //get hostname various ways
    if (StringUtils.isNotBlank(hostnameEnvVariable)) {
      hostname = hostnameEnvVariable;
      log.info("set hostname from env variable to: {}", hostname);
    }
    if (StringUtils.isBlank(hostname)) {
      try {
        hostname = InetAddress.getLocalHost().getHostName();
        log.info("set hostname from InetAddress to: {}", hostname);
      } catch (Exception e) {
        log.warn("cannot retrieve hostname from InetAddress", e);
      }
    }
    if (StringUtils.isBlank(hostname)) {
      try {
        Process p = new ProcessBuilder("hostname").start();
        if (p.exitValue() == 0)
          hostname = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
        log.info("set hostname from o.s. command hostname to: {}", hostname);
      } catch (Exception e) {
        log.warn("cannot retrieve hostname from o.s. command", e);
      }
    }

    if (StringUtils.isBlank(hostname)) {
      log.error("cannot set hostname");
      this.hostname = Optional.empty();
    } else {
      this.hostname = Optional.of(hostname);
    }

    //this transformation will specially fit kubernetes environment: it will take the "random" part at end of pod name
    serverId = this.hostname.map(StringUtils::trim).filter(StringUtils::isNotBlank)
        .map(hn -> StringUtils.substring(hn, StringUtils.lastOrdinalIndexOf(hn, SEP, 2) + 1))
        .map(StringUtils::lowerCase);
  }

  @PreDestroy
  public void preDestroy() {
    sdf.remove();
  }

  public Pair<String, String> generateNowStringAndErrorUid() {
    String now = sdf.get().format(new Date());
    String[] uuidParts = UUID.randomUUID().toString().split("-");
    String errorUid = uuidParts[0] + SEP + uuidParts[1] +
        //this part of UUID is last part of Kubernetes pod name, if available
        SEP + serverId.orElse(uuidParts[2] + SEP + uuidParts[3]) +
        //last part of UUID is current timestamp (epoch) as hex-number
        SEP + String.format("%x", Long.valueOf(now));
    return Pair.of(now, errorUid);
  }

}
