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

import it.regioneveneto.mygov.payment.mypay4.dto.LingueAggiuntiveAvvisoCodDescTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LingueAggiuntivaAvvisoService {

  @Value("#{${pa.avviso.languages}}")
  private Map<String, String> languages;

  public List<LingueAggiuntiveAvvisoCodDescTo> getLingueAggiuntive() {
    List<LingueAggiuntiveAvvisoCodDescTo> list = new ArrayList<>(List.of(new LingueAggiuntiveAvvisoCodDescTo(null, "No")));
    languages.forEach((k, v) -> list.add(new LingueAggiuntiveAvvisoCodDescTo(k, v)));
    return list;
  }
}