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

import it.regioneveneto.mygov.payment.mypay4.dao.StandardTipoDovutoDao;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.StandardTipoDovuto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

@Service
@Slf4j
public class StandardTipoDovutoService {

  @Resource
  StandardTipoDovutoService self;

  @Autowired
  StandardTipoDovutoDao standardTipoDovutoDao;

  public Set<StandardTipoDovuto> getAll() {
    return standardTipoDovutoDao.getAll();
  }

  public Set<EnteTipoDovuto> getAllStandardDefault(Ente ente) {
    Predicate<StandardTipoDovuto> filter = s -> s.getCodTassonomico().startsWith("9/" + ente.getCodTipoEnte());
    Function<StandardTipoDovuto, EnteTipoDovuto> mapper = in -> EnteTipoDovuto.builder()
        .mygovEnteId(ente)
        .codTipo(in.getCodTipo())
        .deTipo(in.getDeTipo())
        .codXsdCausale(in.getCodXsdCausale())
        .macroArea(in.getMacroArea())
        .tipoServizio(in.getTipoServizio())
        .motivoRiscossione(in.getMotivoRiscossione())
        .codTassonomico(in.getCodTassonomico())
        .bicAccreditoPiSeller(true)
        .bicAccreditoPspSeller(false)
        .flgAttivo(false)
        .spontaneo(false)
        .build();
    return self.getAll()
        .stream()
        .filter(filter)
        .map(mapper)
        .collect(toSet());
  }
}
