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

import it.regioneveneto.mygov.payment.mypay4.dao.EnteDao;
import it.regioneveneto.mygov.payment.mypay4.dao.EnteFunzionalitaDao;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteFunzionalita;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class EnteFunzionalitaService {

  @Autowired
  private EnteFunzionalitaDao enteFunzionalitaDao;

  @Autowired
  private EnteDao enteDao;

  @Autowired
  private EnteService enteService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private RegistroOperazioneService registroOperazioneService;

  public List<EnteFunzionalita> getAllByCodIpaEnte(String codIpaEnte) {
    return enteFunzionalitaDao.getAllByCodIpaEnte(codIpaEnte, null);
  }

  public List<EnteFunzionalita> getAllByCodIpaEnte(String codIpaEnte, Boolean flgAttivo) {
    return enteFunzionalitaDao.getAllByCodIpaEnte(codIpaEnte, flgAttivo);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void switchActivation(Long mygovEnteFunzionalita, boolean flgAttivo, UserWithAdditionalInfo operatore) {
    EnteFunzionalita enteFunzionalita = enteFunzionalitaDao.getById(mygovEnteFunzionalita)
        .orElseThrow(() -> new NotFoundException("EnteFunzionalita not found. [id:" + mygovEnteFunzionalita + "]"));
    if(enteFunzionalita.isFlgAttivo() != flgAttivo) {
      enteFunzionalita.setFlgAttivo(flgAttivo);
      enteFunzionalitaDao.update(enteFunzionalita);
      String oggetto = enteFunzionalita.getCodIpaEnte()+'|'+enteFunzionalita.getCodFunzionalita();
      registroOperazioneService.insert(operatore, RegistroOperazione.TipoOperazione.ENTE_FUNZ, oggetto, flgAttivo);

      if (!flgAttivo && enteFunzionalita.getCodFunzionalita().equals(Constants.FUNZIONALITA_INOLTRO_ESITO_PAGAMENTO_PUSH)) {
        Ente ente = enteDao.getEnteByCodIpa(enteFunzionalita.getCodIpaEnte());
        enteTipoDovutoService.getAllByEnte(ente.getMygovEnteId()).stream().filter(EnteTipoDovuto::isFlgNotificaEsitoPush)
            .forEach(enteTipoDovuto -> {
                enteTipoDovuto.setFlgNotificaEsitoPush(false);
                enteTipoDovutoService.update(enteTipoDovuto);
            });
      }
      enteService.clearCacheAllEnti();
    }
  }

  public boolean isActiveByCodIpaEnte(String code, String codIpaEnte, boolean flgAttivo) {
    return enteFunzionalitaDao.getAllByCodIpaEnte(codIpaEnte, flgAttivo)
      .stream()
      .anyMatch(ef -> code.equals(ef.getCodFunzionalita()));
  }

  public boolean isActiveByFunzionalitaAndCodIpaEnte(String codIpaEnte, String codFunzionalita){
    return enteFunzionalitaDao.isActiveByFunzionalitaAndCodIpaEnte(codIpaEnte, codFunzionalita);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Optional<Long> insert(String funzionalita, String codIpaEnte, boolean stato) {
    EnteFunzionalita enteFunzionalita = EnteFunzionalita.builder()
        .codFunzionalita(funzionalita)
        .codIpaEnte(codIpaEnte)
        .flgAttivo(stato)
        .build();
    long newId = enteFunzionalitaDao.insert(enteFunzionalita);
    return Optional.of(newId);
  }
}
