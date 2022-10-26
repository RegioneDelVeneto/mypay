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

import it.regioneveneto.mygov.payment.mypay4.dao.AvvisoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.AnagraficaPagatore;
import it.regioneveneto.mygov.payment.mypay4.model.Avviso;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class AvvisoService {

  @Autowired
  private AvvisoDao avvisoDao;

  @Transactional(propagation = Propagation.REQUIRED)
  public Avviso insert(Ente ente, String codIuv, AnagraficaPagatore anagraficaPagatore) {
    Avviso avviso = new Avviso();

    avviso.setCodIuv(codIuv);
    avviso.setMygovEnteId(ente);

    avviso.setCodRpSoggPagIdUnivPagTipoIdUnivoco(Character.toString(anagraficaPagatore.getTipoIdentificativoUnivoco()));
    avviso.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(anagraficaPagatore.getCodiceIdentificativoUnivoco());
    avviso.setDeRpSoggPagAnagraficaPagatore(anagraficaPagatore.getAnagrafica());
    avviso.setDeRpSoggPagEmailPagatore(anagraficaPagatore.getEmail());

    avviso.setDeRpSoggPagIndirizzoPagatore(anagraficaPagatore.getIndirizzo());
    avviso.setDeRpSoggPagCivicoPagatore(anagraficaPagatore.getCivico());
    avviso.setCodRpSoggPagCapPagatore(anagraficaPagatore.getCap());

    avviso.setDeRpSoggPagLocalitaPagatore(
        StringUtils.isBlank(anagraficaPagatore.getLocalita()) ? null : anagraficaPagatore.getLocalita());
    avviso.setDeRpSoggPagProvinciaPagatore(
        StringUtils.isBlank(anagraficaPagatore.getProvincia()) ? null : anagraficaPagatore.getProvincia());
    avviso.setCodRpSoggPagNazionePagatore(
        StringUtils.isBlank(anagraficaPagatore.getNazione()) ? null : anagraficaPagatore.getNazione());

    long mygovAvvisoId = avvisoDao.insert(avviso);
    avviso.setMygovAvvisoId(mygovAvvisoId);
    return avviso;
  }

  public Optional<Avviso> getByIuvEnte(final String codIuv, final String codIpaEnte) {
    return avvisoDao.getByIuvEnte(codIuv, codIpaEnte);
  }
}
