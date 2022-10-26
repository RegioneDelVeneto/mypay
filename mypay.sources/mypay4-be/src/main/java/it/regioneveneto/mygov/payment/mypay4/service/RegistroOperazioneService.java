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

import it.regioneveneto.mygov.payment.mypay4.dao.RegistroOperazioneDao;
import it.regioneveneto.mygov.payment.mypay4.dto.RegistroOperazioneTo;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RegistroOperazioneService {

  @Autowired
  private RegistroOperazioneDao registroOperazioneDao;

  @Autowired
  private UtenteService utenteService;

  @Transactional(propagation = Propagation.SUPPORTS)
  public List<RegistroOperazione> getByTipoAndOggetto(RegistroOperazione.TipoOperazione tipoOperazione, String oggettoOperazione){
    return registroOperazioneDao.getByTipoAndOggetto(tipoOperazione, oggettoOperazione);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void insert(RegistroOperazione r){
    registroOperazioneDao.insert(r);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void insert(UserWithAdditionalInfo user,
                     RegistroOperazione.TipoOperazione tipoOperazione,
                     String oggettoOperazione,
                     boolean value){
    RegistroOperazione r = RegistroOperazione.builder()
        .codFedUserIdOperatore(user.getUsername())
        .codTipoOperazione(tipoOperazione)
        .deOggettoOperazione(oggettoOperazione)
        .codStatoBool(value)
        .build();
    registroOperazioneDao.insert(r);
  }

  public RegistroOperazioneTo mapRegistroOperazioneToDto(RegistroOperazione r) {
    return RegistroOperazioneTo.builder()
        .username(r.getCodFedUserIdOperatore())
        .fullName(utenteService.getByCodFedUserId(r.getCodFedUserIdOperatore())
            .map(u -> StringUtils.trim(u.getDeFirstname()+" "+u.getDeLastname()))
            .orElse("-"))
        .dtOperazione(Utilities.toLocalDateTime(r.getDtOperazione()))
        .statoDa(r.getCodStatoBool()!=null ? (!r.getCodStatoBool()) : null)
        .statoA(r.getCodStatoBool()!=null ? r.getCodStatoBool() : null)
        .build();
  }
}
