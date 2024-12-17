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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.ProgressiviVersamentoDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.ProgressiviVersamento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ProgressiviVersamentoService {

  @Autowired
  private ProgressiviVersamentoDao progressiviVersamentoDao;

  long getNextProgressivoVersamento(final String codIpaEnte, final String tipoGeneratore,
                                    final String tipoVersamento) {
    long nextProgressivo;

    ProgressiviVersamento currentEntity = progressiviVersamentoDao.getByKey(codIpaEnte, tipoGeneratore, tipoVersamento, true);
    if(currentEntity==null){
      //insert
      nextProgressivo = 1;
      ProgressiviVersamento newEntity = ProgressiviVersamento.builder()
          .version(0)
          .codIpaEnte(codIpaEnte)
          .tipoGeneratore(tipoGeneratore)
          .tipoVersamento(tipoVersamento)
          .progressivoVersamento(nextProgressivo)
          .build();
      progressiviVersamentoDao.insert(newEntity);
    } else {
      //update
      nextProgressivo = currentEntity.getProgressivoVersamento()+1;
      progressiviVersamentoDao.updateProgressivoVersamento(currentEntity.getId(), nextProgressivo);
    }

    log.debug("nextProgressivoPagamento ["+codIpaEnte+"]["+tipoGeneratore+"]["+tipoVersamento+"]: "+nextProgressivo);

    return nextProgressivo;

  }
}
