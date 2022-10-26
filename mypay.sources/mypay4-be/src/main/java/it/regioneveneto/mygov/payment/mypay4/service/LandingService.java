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

import it.regioneveneto.mygov.payment.mypay4.controller.LandingController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class LandingService {

  @Value("${app.be.absolute-path}")
  private String appBeAbsolutePath;

  public String getUrlInviaDovuti(String id) { return appBeAbsolutePath + LandingController.ANONYMOUS_PATH + "/inviaDovuti?id=" + id; }

  public String getUrlDownloadAvviso(String dovutoId, String securityToken) {
    return appBeAbsolutePath + LandingController.ANONYMOUS_PATH + "/avviso?id=" + dovutoId + "&securityToken=" + URLEncoder.encode(securityToken, StandardCharsets.UTF_8);
  }

  public String getUrlChiediPosizioniAperte(String id) { return appBeAbsolutePath + LandingController.ANONYMOUS_PATH + "/precaricato?id=" + id; }

  public String getUrlChiediStoricoPagamenti(String id) {
    return appBeAbsolutePath + LandingController.ANONYMOUS_PATH + "/rt?id=" + id;
  }
}
