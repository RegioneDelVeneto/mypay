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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AppErrorService {
  private final static String SEP = "-";
  private final static ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmss"));

  @Value("${HOSTNAME}")
  private Optional<String> hostname;

  private Optional<String> serverId;

  @PostConstruct
  public void runAfterObjectCreated() {
    //this transformation will specially fit kubernetes environment: it will take the "random" part at end of pod name
    serverId = hostname.map(StringUtils::trim).filter(StringUtils::isNotBlank)
      .map(hn -> StringUtils.substring(hn, StringUtils.lastOrdinalIndexOf(hn, SEP, 2) + 1))
      .map(StringUtils::lowerCase);
  }

  public Pair<String, String> generateNowStringAndErrorUid(){
    String now = sdf.get().format(new Date());
    String[] uuidParts = UUID.randomUUID().toString().split("-");
    String errorUid = new StringBuilder(uuidParts[0]).append(SEP).append(uuidParts[1])
      //this part of UUID is last part of Kubernetes pod name, if available
      .append(SEP).append(serverId.orElse(uuidParts[2] + SEP + uuidParts[3]))
      //last part of UUID is current timestamp (epoch) as hex-number
      .append(SEP).append(String.format("%x", Long.valueOf(now)))
      .toString();
    return Pair.of(now,errorUid);
  }
}
