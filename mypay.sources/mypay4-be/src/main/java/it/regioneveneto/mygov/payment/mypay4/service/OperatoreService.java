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
import it.regioneveneto.mygov.payment.mypay4.dao.UtenteDao;
import it.regioneveneto.mygov.payment.mypay4.dto.EnteRolesTo;
import it.regioneveneto.mygov.payment.mypay4.dto.OperatoreTo;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore.Role;
import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Priority;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Priority(Integer.MIN_VALUE) //this is needed for correct configuration as UtenteProfileProvider
public class OperatoreService implements UtenteProfileProvider{

  public enum COUPLING_MODE { ENTE, DOVUTI, ENTE_DOVUTI }

  @Autowired
  private OperatoreDao operatoreDao;

  @Autowired
  private UtenteDao utenteDao;

  @Autowired
  private UtenteService utenteService;

  @Autowired
  private EnteService enteService;

  @Autowired
  private EnteTipoDovutoService enteTipoDovutoService;

  @Autowired
  private OperatoreEnteTipoDovutoService operatoreEnteTipoDovutoService;

  @Resource
  private OperatoreService self;

  @Transactional(propagation = Propagation.REQUIRED)
  public void updateUserTenantsAndRoles(String username, Map<String, Set<String>> newTenants){
    //find user on db
    Utente utente = utenteDao.getByCodFedUserId(username).orElseThrow(NotFoundException::new);
    Map<String, Set<String>> oldTenants = self.getUserTenantsAndRoles(username);
    //find tentants/roles to remove
    Set<String> allTenants = new HashSet<>(oldTenants.keySet());
    allTenants.addAll(newTenants.keySet());
    allTenants.forEach(tenant -> {
      if(!newTenants.containsKey(tenant)){
        //tenant to remove
        Ente ente = Optional.ofNullable(enteService.getEnteByCodIpa(tenant)).orElseThrow(NotFoundException::new);
        log.info("deleteAssociationOperatoreEnte user:{} - ente:{}({})",utente.getCodFedUserId(),ente.getCodIpaEnte(), ente.getDeNomeEnte());
        self.deleteAssociationOperatoreEnte(Constants.SYSTEM_USER_INFO, utente.getCodFedUserId(),ente.getMygovEnteId(), false);
      } else if(!oldTenants.containsKey(tenant)){
        //tenant to add
        Ente ente = enteService.getEnteByCodIpa(tenant);
        if(ente != null){
          log.info("addAssociationOperatoreEnte user:{} - ente:{}({})",utente.getCodFedUserId(),ente.getCodIpaEnte(), ente.getDeNomeEnte());
          self.addAssociationOperatoreEnte(Constants.SYSTEM_USER_INFO, utente.getCodFedUserId(), ente.getMygovEnteId(), COUPLING_MODE.ENTE, null);
          Set<String> newRoles = newTenants.get(tenant);
          //if any role except ROLE_OPER is present, then need to update roles too
          if(newRoles.size() > (newRoles.contains(Role.ROLE_OPER.name())?1:0) )
            self.setUserRoles(utente.getMygovUtenteId(), ente.getMygovEnteId(), newTenants.get(tenant));
        } else {
          log.warn("user: "+username+" - missing ente: "+tenant+" on db while this ente is present on external profile system");
        }
      } else if(!oldTenants.get(tenant).equals(newTenants.get(tenant))){
        Ente ente = Optional.ofNullable(enteService.getEnteByCodIpa(tenant)).orElseThrow(NotFoundException::new);
        log.info("setUserRoles user:{} - ente:{}({}) : {}",utente.getCodFedUserId(),ente.getCodIpaEnte(), ente.getDeNomeEnte(), newTenants.get(tenant));
        self.setUserRoles(utente.getMygovUtenteId(), ente.getMygovEnteId(), newTenants.get(tenant));
      }
    });

  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void setUserRoles(Long idOperatore, Long idEnte, Set<String> roles){
    if(roles.isEmpty())
      throw new MyPayException("roles cannot be empty, user="+idOperatore+", ente="+idEnte);
    //throw exception if invalid role
    roles.forEach(role -> {
      try{
        Role.valueOf(Role.ANY.name().equals(role)?"INVALID":role);
      } catch(Exception e){
        throw new MyPayException("invalid role "+role+", user="+idOperatore+", ente="+idEnte);
      }
    });
    //ROLE_OPER is necessary
    if(!roles.contains(Role.ROLE_OPER.name()))
      throw new MyPayException("roles must include ROLE_OPER, user="+idOperatore+", ente="+idEnte);

    //currently, the only possible operation is if ROLE_ADMIN is present or not
    log.info("changeRuolo user:{} - ente:{} - isAdmin:{}",idOperatore, idEnte, roles.contains(Role.ROLE_ADMIN.name()));
    self.changeRuolo(idOperatore, idEnte, roles.contains(Role.ROLE_ADMIN.name()));
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public Map<String, Set<String>> getUserTenantsAndRoles(String username){
    Map<String, Set<String>> roles = operatoreDao.getByCodFedUserId(username).stream()
        .collect(Collectors.groupingBy(Operatore::getCodIpaEnte,
            Collectors.mapping(
                //on DB table, no role means operator role
                operatore -> ObjectUtils.firstNonNull(operatore.getRuolo(),Role.ROLE_OPER.name()),
                Collectors.toSet()) ) );
    //on DB table, admin role implies operator role: add it if missing
    roles.values().stream().filter(set -> !set.contains(Role.ROLE_OPER.name())).forEach(set -> set.add(Role.ROLE_OPER.name()));
    return roles;
  }

  public Optional<OperatoreTo> getOperatoreDetails(Long utenteId){
    return utenteDao.getById(utenteId)
        .map(utente -> {
          List<Operatore> operatoreList = operatoreDao.getByCodFedUserId(utente.getCodFedUserId());
          return UtenteService.completeMapUtenteToDtoMainFields(utente,
              OperatoreTo.builder().fullEntiRoles( operatoreList.isEmpty() ? Collections.emptyList() : operatoreList.stream().collect(
                  Collectors.groupingBy(
                      operatore -> Pair.of(enteService.getEnteByCodIpa(operatore.getCodIpaEnte()), operatore),
                      Collectors.mapping(
                          //on DB table, no role means operator role
                          operatore -> ObjectUtils.firstNonNull(operatore.getRuolo(), it.regioneveneto.mygov.payment.mypay4.security.Operatore.Role.ROLE_OPER.name()),
                          Collectors.toSet()) ) ).entrySet().stream().map(entry -> EnteRolesTo.builder()
                  .mygovEnteId(entry.getKey().getLeft().getMygovEnteId())
                  .codIpaEnte(entry.getKey().getLeft().getCodIpaEnte())
                  .deNomeEnte(entry.getKey().getLeft().getDeNomeEnte())
                  .roles(entry.getValue())
                  .emailAddress(entry.getKey().getRight().getDeEmailAddress())
                  .build())
                  .sorted((e1,e2) -> e1.getDeNomeEnte().compareToIgnoreCase(e2.getDeNomeEnte()))
                  .collect(Collectors.toList()) )::build );
        });
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public EnteRolesTo addAssociationOperatoreEnte(UserWithAdditionalInfo user, String username, Long idEnte, COUPLING_MODE couplingMode, String email){
    boolean forceUserEnteAssociation = COUPLING_MODE.ENTE.equals(couplingMode) || COUPLING_MODE.ENTE_DOVUTI.equals(couplingMode);
    boolean coupleWithAllTipiDovuto = COUPLING_MODE.DOVUTI.equals(couplingMode) || COUPLING_MODE.ENTE_DOVUTI.equals(couplingMode);

    //find user on db
    Utente utente = utenteDao.getByCodFedUserId(username).orElseThrow(NotFoundException::new);
    //find ente on db
    Ente ente = Optional.ofNullable(enteService.getEnteById(idEnte)).orElseThrow(NotFoundException::new);

    Operatore operatore = operatoreDao.getByCodFedUserIdEnte(utente.getCodFedUserId(), ente.getCodIpaEnte());
    if (operatore == null) {
      operatore = Operatore.builder().codFedUserId(utente.getCodFedUserId()).codIpaEnte(ente.getCodIpaEnte()).ruolo(null).build();
      Long mygovOperatoreId = operatoreDao.insert(operatore.getCodFedUserId(), operatore.getCodIpaEnte(), operatore.getRuolo(), email);
      operatore.setMygovOperatoreId(mygovOperatoreId);
      log.info("associating user: {} with ente: {} coupleWithAllTipiDovuto: {} - id:{}", utente, ente.getCodIpaEnte(), coupleWithAllTipiDovuto, mygovOperatoreId);
    } else if(forceUserEnteAssociation){
      throw new MyPayException("User is already associated with ente");
    }

    if(coupleWithAllTipiDovuto){
      List<EnteTipoDovuto> enteTipoDovutos = enteTipoDovutoService.getAllByEnte(ente.getMygovEnteId());
      Operatore finalOperatore = operatore;
      enteTipoDovutos.forEach(etd -> {
        List<OperatoreEnteTipoDovuto> oetds = operatoreEnteTipoDovutoService.getByCodIpaCodTipoCodFed(ente.getCodIpaEnte(), etd.getCodTipo(), finalOperatore.getCodFedUserId());
        if (oetds.size() > 1) {
          throw new MyPayException("More than 1 OperatoreEnteTipoDovuto found!");
        } else if (CollectionUtils.isEmpty(oetds)) {
          operatoreEnteTipoDovutoService.insert( user,
              OperatoreEnteTipoDovuto.builder().mygovEnteTipoDovutoId(etd).mygovOperatoreId(finalOperatore).flgAttivo(true).build()
          );
        } else if (!oetds.get(0).isFlgAttivo()) {
          OperatoreEnteTipoDovuto oetd = oetds.get(0);
          oetd.setFlgAttivo(true);
          operatoreEnteTipoDovutoService.update(user, oetd);
        }
      });
    }

    //clear invalid cache
    utenteService.clearCacheByCodFedUserId(utente.getCodFedUserId());

    return EnteRolesTo.builder()
        .deNomeEnte(ente.getDeNomeEnte())
        .codIpaEnte(ente.getCodIpaEnte())
        .mygovEnteId(ente.getMygovEnteId())
        .roles(Collections.singleton(Role.ROLE_OPER.name()))
        .emailAddress(email)
        .build();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteAssociationOperatoreEnte(UserWithAdditionalInfo user, String username, Long idEnte, boolean decoupleOnlyTipiDovuto){
    //find user on db
    Utente utente = utenteDao.getByCodFedUserId(username).orElseThrow(NotFoundException::new);
    //find ente on db
    Ente ente = Optional.ofNullable(enteService.getEnteById(idEnte)).orElseThrow(NotFoundException::new);

    //first remove all operatore ente tipo dovuto associations
    operatoreEnteTipoDovutoService.deleteAllTipiDovutoByEnteForOperatore(user, utente.getCodFedUserId(), ente.getMygovEnteId());
    //..then remove from operatore (if requested)
    if(!decoupleOnlyTipiDovuto)
      operatoreDao.delete(utente.getCodFedUserId(),ente.getCodIpaEnte());
    log.info("removing association user: {} with ente: {} - decoupleOnlyTipiDovuto: {}", utente, ente.getCodIpaEnte(),decoupleOnlyTipiDovuto);

    //clear invalid cache
    utenteService.clearCacheByCodFedUserId(utente.getCodFedUserId());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public EnteRolesTo addAssociationOperatoreEnteTipoDovuto(UserWithAdditionalInfo user, Long idOperatore, Long idTipo){
    //find user on db
    Utente utente = utenteDao.getById(idOperatore).orElseThrow(NotFoundException::new);
    //find enteTipoDovuto on db
    EnteTipoDovuto etd = enteTipoDovutoService.getById(idTipo);

    List<OperatoreEnteTipoDovuto> oetds = operatoreEnteTipoDovutoService.getByCodIpaCodTipoCodFed(etd.getMygovEnteId().getCodIpaEnte(), etd.getCodTipo(), utente.getCodFedUserId());
    if (oetds.size() > 1)
      throw new MyPayException("More than 1 OperatoreEnteTipoDovuto found!");
    else if (CollectionUtils.isEmpty(oetds)) {
      Operatore operatore = operatoreDao.getByCodFedUserIdEnte(utente.getCodFedUserId(), etd.getMygovEnteId().getCodIpaEnte());
      operatoreEnteTipoDovutoService.insert(user,
          OperatoreEnteTipoDovuto.builder().mygovEnteTipoDovutoId(etd).mygovOperatoreId(operatore).flgAttivo(true).build()
      );
    } else if (!oetds.get(0).isFlgAttivo()) {
      OperatoreEnteTipoDovuto oetd = oetds.get(0);
      oetd.setFlgAttivo(true);
      operatoreEnteTipoDovutoService.update(user, oetd);
    }

    //clear invalid cache
    utenteService.clearCacheByCodFedUserId(utente.getCodFedUserId());

    return EnteRolesTo.builder()
        .deNomeEnte(etd.getMygovEnteId().getDeNomeEnte())
        .codIpaEnte(etd.getMygovEnteId().getCodIpaEnte())
        .mygovEnteId(etd.getMygovEnteId().getMygovEnteId())
        .roles(Collections.singleton(Role.ROLE_OPER.name()))
        .build();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteAssociationOperatoreEnteTipoDovuto(UserWithAdditionalInfo user,Long idOperatore, Long idTipo){
    //find user on db
    Utente utente = utenteDao.getById(idOperatore).orElseThrow(NotFoundException::new);
    //find enteTipoDovuto on db
    EnteTipoDovuto etd = enteTipoDovutoService.getById(idTipo);

    List<OperatoreEnteTipoDovuto> oetds = operatoreEnteTipoDovutoService.getByCodIpaCodTipoCodFed(etd.getMygovEnteId().getCodIpaEnte(), etd.getCodTipo(), utente.getCodFedUserId());
    if (oetds.size() > 1)
      throw new MyPayException("More than 1 OperatoreEnteTipoDovuto found!");
    else if (CollectionUtils.isEmpty(oetds)) {
      // do nothing: operatore is already not associated
      log.debug("operatore with id[] already not associated with tipoDovuto[]", idOperatore, idTipo);
    } else if (oetds.get(0).isFlgAttivo()) {
      OperatoreEnteTipoDovuto oetd = oetds.get(0);
      oetd.setFlgAttivo(false);
      operatoreEnteTipoDovutoService.update(user, oetd);
    }

    //clear invalid cache
    utenteService.clearCacheByCodFedUserId(utente.getCodFedUserId());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void changeRuolo(Long idOperatore, Long idEnte, boolean toAdmin){
    //find user on db
    Utente utente = utenteDao.getById(idOperatore).orElseThrow(NotFoundException::new);
    //find ente on db
    Ente ente = Optional.ofNullable(enteService.getEnteById(idEnte)).orElseThrow(NotFoundException::new);

    Operatore operatore = operatoreDao.getByCodFedUserIdEnte(utente.getCodFedUserId(), ente.getCodIpaEnte());
    operatore.setRuolo(toAdmin ? Role.ROLE_ADMIN.name() : null);
    operatoreDao.update(operatore);

    //clear invalid cache
    utenteService.clearCacheByCodFedUserId(utente.getCodFedUserId());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void changeEmailAddress(Long idOperatore, Long idEnte, String emailAddress){
    //find user on db
    Utente utente = utenteDao.getById(idOperatore).orElseThrow(NotFoundException::new);
    //find ente on db
    Ente ente = Optional.ofNullable(enteService.getEnteById(idEnte)).orElseThrow(NotFoundException::new);

    Operatore operatore = operatoreDao.getByCodFedUserIdEnte(utente.getCodFedUserId(), ente.getCodIpaEnte());
    operatore.setDeEmailAddress(emailAddress);
    operatoreDao.update(operatore);

    //clear invalid cache
    utenteService.clearCacheByCodFedUserId(utente.getCodFedUserId());
  }
}
