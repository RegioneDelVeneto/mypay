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

import it.regioneveneto.mygov.payment.mypay4.dao.OperatoreDao;
import it.regioneveneto.mygov.payment.mypay4.dao.OperatoreEnteTipoDovutoDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Operatore;
import it.regioneveneto.mygov.payment.mypay4.model.OperatoreEnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.RegistroOperazione;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class OperatoreEnteTipoDovutoService {

  @Autowired
  private OperatoreDao operatoreDao;

  @Autowired
  private OperatoreEnteTipoDovutoDao operatoreEnteTipoDovutoDao;

  @Autowired
  private EnteService enteService;

  @Autowired
  private UtenteService utenteService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private RegistroOperazioneService registroOperazioneService;

  @Resource
  private OperatoreEnteTipoDovutoDao self;

  private void insertRegistro(UserWithAdditionalInfo user, Long mygovOperatoreEnteTipoDovutoId, Optional<Boolean> forceState){
    OperatoreEnteTipoDovuto oetd = operatoreEnteTipoDovutoDao.getById(mygovOperatoreEnteTipoDovutoId).orElseThrow(()->new MyPayException("cannot get OperatoreEnteTipoDovuto with id"+mygovOperatoreEnteTipoDovutoId));
    String oggetto = oetd.getMygovOperatoreId().getCodFedUserId()+'|'+oetd.getMygovOperatoreId().getCodIpaEnte()+'|'+oetd.getMygovEnteTipoDovutoId().getCodTipo();
    registroOperazioneService.insert(user, RegistroOperazione.TipoOperazione.OPER_TIP_DOV, oggetto, forceState.orElse(oetd.isFlgAttivo()));
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public OperatoreEnteTipoDovuto insert(UserWithAdditionalInfo user, OperatoreEnteTipoDovuto oetd) {
    //check if already exists
    if(operatoreEnteTipoDovutoDao.existsByEnteTipoDovutoAndOperatore(oetd))
      throw new MyPayException(String.format(
          "OperatoreEnteTipoDovuto already exists for operatoreId[%d] and enteTipoDovutoId[%d]",
          oetd.getMygovOperatoreId().getMygovOperatoreId(), oetd.getMygovEnteTipoDovutoId().getMygovEnteTipoDovutoId()));
    long mygovOperatoreEnteTipoDovutoId = operatoreEnteTipoDovutoDao.insert(oetd);
    oetd.setMygovOperatoreEnteTipoDovutoId(mygovOperatoreEnteTipoDovutoId);
    enteTipoDovutoService.clearCache();
    insertRegistro(user, mygovOperatoreEnteTipoDovutoId, Optional.empty());
    return oetd;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public OperatoreEnteTipoDovuto update(UserWithAdditionalInfo user, OperatoreEnteTipoDovuto oetd) {
    operatoreEnteTipoDovutoDao.update(oetd);
    enteTipoDovutoService.clearCache();
    insertRegistro(user, oetd.getMygovOperatoreEnteTipoDovutoId(), Optional.empty());
    return oetd;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public OperatoreEnteTipoDovuto insert(UserWithAdditionalInfo user, final String codFedUserId, final String codTipoDovuto, final String codIpaEnte) {
    Utente utente = utenteService.getByCodFedUserId(codFedUserId).orElseThrow(()->new NotFoundException("Utente non trovato: "+codFedUserId));
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
    if (ente == null) {
      throw new MyPayException("Non esistono enti con codIpaEnte[" + codIpaEnte + "]");
    }
    Operatore operatore = this.getByCodFedUserIdEnte(codFedUserId, codIpaEnte);
    if (operatore == null) {
      throw new MyPayException("Non esiste un operatore per codIpaEnte[" + codIpaEnte + "] e codFedUserId [ "
          + codFedUserId + " ]");
    }
    EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(codTipoDovuto, codIpaEnte, false)
        .orElseThrow(() -> new MyPayException("Non esistono tipi dovuto per codIpaEnte[" + codIpaEnte
                            + "] e codTipoDovuto [ " + codTipoDovuto + " ]"));

    OperatoreEnteTipoDovuto operatoreEnteTipoDovuto = OperatoreEnteTipoDovuto.builder().mygovEnteTipoDovutoId(enteTipoDovuto)
        .mygovOperatoreId(operatore).flgAttivo(true).build();

    long mygovOperatoreEnteTipoDovutoId = operatoreEnteTipoDovutoDao.insert(operatoreEnteTipoDovuto);
    enteTipoDovutoService.clearCache();
    operatoreEnteTipoDovuto.setMygovOperatoreEnteTipoDovutoId(mygovOperatoreEnteTipoDovutoId);
    insertRegistro(user, mygovOperatoreEnteTipoDovutoId, Optional.empty());
    return operatoreEnteTipoDovuto;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public int deleteAllTipiDovutoByEnteForOperatore(UserWithAdditionalInfo user, String codFedUserId, Long mygovEnteId){
    Ente ente = enteService.getEnteById(mygovEnteId);
    self.getByCodIpaCodTipoCodFed(ente.getCodIpaEnte(), null, codFedUserId)
        .stream().filter(oetd -> oetd.isFlgAttivo())
        .forEach(oetd -> insertRegistro(user, oetd.getMygovOperatoreEnteTipoDovutoId(), Optional.of(false)));
    enteTipoDovutoService.clearCache();
    return operatoreEnteTipoDovutoDao.deleteAllTipiDovutoByEnteForOperatore(codFedUserId, mygovEnteId);
  }

  public List<OperatoreEnteTipoDovuto> getByCodIpaCodTipoCodFed(String codIpaEnte, String codTipo, String codFedUserId) {
    return operatoreEnteTipoDovutoDao.getByCodIpaCodTipoCodFed(codIpaEnte, codTipo, codFedUserId);
  }

  public List<Operatore> getOperatoriByCodIpaEnte(String codIpaEnte) {
    return operatoreDao.getAllOperatoriByCodIpaEnte(codIpaEnte);
  }

  public Operatore getByCodFedUserIdEnte(String codFedUserId, String codIpaEnte) {
    return operatoreDao.getByCodFedUserIdEnte(codFedUserId, codIpaEnte);
  }

  public List<OperatoreEnteTipoDovuto> getByEnteTipoDovuto(Long mygovEnteTipoDovuto) {
    return operatoreEnteTipoDovutoDao.getByEnteTipoDovuto(mygovEnteTipoDovuto);
  }
}
