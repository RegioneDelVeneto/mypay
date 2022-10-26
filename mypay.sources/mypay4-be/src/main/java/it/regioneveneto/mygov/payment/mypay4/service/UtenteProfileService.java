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

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.security.Operatore;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.util.UtenteHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UtenteProfileService implements InitializingBean {

  @Value("${external-profile.enabled:false}")
  private String externalProfileEnabled;
  @Value("${pa.codIpaEntePredefinito}")
  private String adminEnteCodIpa;

  @Autowired
  private UtenteProfileProvider utenteProfileProvider;

  @Autowired
  private OperatoreService operatoreService;

  @Autowired
  private UtenteProfileService self;

  @Override
  public void afterPropertiesSet() {
    //check coherency between param externalProfileEnabled and concrete utenteProfileProvider class;
    if(Boolean.parseBoolean(externalProfileEnabled) ^ utenteProfileProvider instanceof OperatoreService)
      log.info("User Profile system: "+utenteProfileProvider.getClass().getName());
    else
      throw new MyPayException("invalid configuration of User Profile system: external-profile.enabled="
        +externalProfileEnabled+" - utenteProfileProvider class="+utenteProfileProvider.getClass().getName());
  }

  public void checkDisabledEditWhenExternalProfile(){
    //if external profile system is enabled, no edit action on user/ente/role is allowed
    if(Boolean.parseBoolean(externalProfileEnabled)){
      throw new UnsupportedOperationException("role editing disabled when external profile system is enabled");
    }
  }

  @Cacheable(value=CacheService.CACHE_NAME_UTENTE_PROFILE, key="{'username',#username}", unless="#result==null")
  public Map<String, Set<String>> getUserTenantsAndRoles(String username) {
    Map<String, Set<String>> roles = utenteProfileProvider.getUserTenantsAndRoles(username);
    log.trace("row roles for user {}: {}", username, roles);

    // clean up roles:
    // - make map "modifiable" (in case if it was unmodifiable)
    roles = roles.entrySet().stream()
        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), new HashSet<>(entry.getValue())))
        .collect(Collectors.toMap(
            AbstractMap.SimpleEntry::getKey,
            AbstractMap.SimpleEntry::getValue));
    // - remove all invalid roles
    roles.forEach((key, value) -> value.removeIf(role -> !UtenteHelper.isValidRole(role)));
    // - remove enti without any role
    roles.entrySet().removeIf( entry -> entry.getValue().isEmpty() );
    // - make immutable
    roles = roles.entrySet().stream()
        .map(entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), Collections.unmodifiableSet(entry.getValue())))
        .collect(Collectors.toUnmodifiableMap(
            AbstractMap.SimpleImmutableEntry::getKey,
            AbstractMap.SimpleImmutableEntry::getValue));

    log.debug("cleaned-up roles for user {}: {}", username, roles);
    return roles;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Map<String, Set<String>> getAndUpdateUserTenantsAndRoles(String username){
    self.forgetUserTenantsAndRoles(username);
    Map<String, Set<String>> roles = self.getUserTenantsAndRoles(username);
    //in case reading roles from external system, also sync mypay DB
    if(Boolean.parseBoolean(externalProfileEnabled)){
      operatoreService.updateUserTenantsAndRoles(username, roles);
    }
    return roles;
  }

  @CacheEvict(value=CacheService.CACHE_NAME_UTENTE_PROFILE,key="{'username',#username}")
  public void forgetUserTenantsAndRoles(String username){}

//  public void forgetUserTenantsAndRoles(String username){
//    utenteProfileProvider.clearUserTenantsAndRoles(username);
//  }

  public boolean isSystemAdministrator(Map<String, Set<String>> userTenantsAndRoles) {
    return userTenantsAndRoles.getOrDefault(adminEnteCodIpa, Set.of()).contains(Operatore.Role.ROLE_ADMIN.name());
  }
}
