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

import it.regioneveneto.mygov.payment.mypay4.dao.IdentificativoUnivocoDao;
import it.regioneveneto.mygov.payment.mypay4.model.IdentificativoUnivoco;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class IdentificativoUnivocoService {

  @Autowired
  private IdentificativoUnivocoDao identificativoUnivocoDao;

  public List<IdentificativoUnivoco> getByEnteAndCodTipoIdAndId(Long enteId, String codTipoIdentificativo, String identificativo){
    return identificativoUnivocoDao.getByEnteAndCodTipoIdAndId(enteId, codTipoIdentificativo, identificativo);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void insert(Long idFlusso, Long idEnte, String tipoIdentificativoUnivoco, String identificativo) {
    IdentificativoUnivoco identificativoUnivoco = IdentificativoUnivoco.builder()
        .codTipoIdentificativo(tipoIdentificativoUnivoco)
        .dtInserimento(new Date())
        .identificativo(identificativo)
        .mygovEnteId(idEnte)
        .mygovFlussoId(idFlusso)
        .version(0)
        .build();

    Long newId = identificativoUnivocoDao.insert(identificativoUnivoco);
    log.info("insert IdentificativoUnivoco, new Id: "+newId);
  }
}
