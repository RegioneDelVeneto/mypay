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
package it.regioneveneto.mygov.payment.mypay4.security;

import it.regioneveneto.mygov.payment.mypay4.dao.EnteDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotAuthorizedException;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class OperatoreAspect {

  @Value("${pa.api-operatore.enabled:true}")
  private String apiOperatoreEnabled;
  @Value("${pa.codIpaEntePredefinito}")
  private String adminEnteCodIpa;

  private Long adminEnteId;

  private static final Set<String> ADMIN_ROLE_SET = Set.of(Operatore.Role.ROLE_ADMIN.name());

  @Autowired
  private EnteDao enteDao;

  private static final ThreadLocal<Consumer<Long>> deferredCheckConsumer = new ThreadLocal<>();

  public static void deferredCheck(Long mygovEnteId){
    deferredCheckConsumer.get().accept(mygovEnteId);
  }

  private Long getAdminEnteId(){
    //check that user is application admin i.e. has ADMIN_ROLE on default ente set in configuration
    if(adminEnteId ==null)
      adminEnteId = Optional.ofNullable(enteDao.getEnteByCodIpa(adminEnteCodIpa)).map(Ente::getMygovEnteId).orElse(null);
    return adminEnteId;
  }

  @Around("@annotation(Operatore)")
  public Object checkOperatoreRights(ProceedingJoinPoint joinPoint) throws Throwable {
    log.warn("checking operatoreRights, operation: "+joinPoint.getSignature());

    if(!StringUtils.equalsIgnoreCase(this.apiOperatoreEnabled,"true"))
      throw new NotAuthorizedException("API operatore disabilitate");

    //remove thread-local consumer for deferred check
    deferredCheckConsumer.remove();
    //retrieve parameter containing codIpa
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Operatore operatoreAnnot = method.getAnnotation(Operatore.class);
    //in any case check if user is operatore
    UserWithAdditionalInfo user = (UserWithAdditionalInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if(user.getUsername()==null)
      throw new NotAuthorizedException("utente null");
    final Set<String> allowedRoles;
    if(!ArrayUtils.contains(operatoreAnnot.roles(), Operatore.Role.ANY))
      allowedRoles = Arrays.stream(operatoreAnnot.roles()).map(Enum::name).collect(Collectors.toUnmodifiableSet());
    else
      allowedRoles = null;

    if(operatoreAnnot.appAdmin()){
      if(allowedRoles!=null || !Operatore.NOT_PRESENT.equals(operatoreAnnot.value())){
        log.error("invalid Operatore annotation for method: {} - allowedRoles: {} - value: {}", method, allowedRoles, operatoreAnnot.value());
        throw new MyPayException("invalid Operatore annotation");
      }
      //check that user is application admin i.e. has ADMIN_ROLE on default ente set in configuration
      checkEnte(getAdminEnteId(), ADMIN_ROLE_SET, user, false);
    } else if(Operatore.NOT_PRESENT.equals(operatoreAnnot.value())) {
      //in this case just check that user is operatore for at least one ente
      log.warn("checking if user:"+user.getUsername()+" is operatore for any ente");
      if(user.getEntiRoles().isEmpty())
        throw new NotAuthorizedException("utente non autorizzato ad operare su nessun ente");
    } else if(Operatore.DEFERRED.equals(operatoreAnnot.value())) {
      //in this case the check is made inside the calling method, because
      // mygovEnteId is not available as method param
      deferredCheckConsumer.set(mygovEnteId -> checkEnte(mygovEnteId, allowedRoles, user, true));
    } else {
      //if "idEnteParam" is present, retrieve the codIpa for the call
      // and check if current user has the relevant role for that ente
      int mygovEnteIdParamIndex = ArrayUtils.indexOf(signature.getParameterNames(),operatoreAnnot.value());
      Long mygovEnteId = (Long) joinPoint.getArgs()[mygovEnteIdParamIndex];
      checkEnte(mygovEnteId, allowedRoles, user, false);
    }

    return joinPoint.proceed();
  }

  private void checkEnte(Long mygovEnteId, Set<String> allowedRoles, UserWithAdditionalInfo user, boolean deferred){
    if(mygovEnteId==null)
      throw new NotAuthorizedException("ente null");
    Ente ente = enteDao.getEnteById(mygovEnteId);
    if(ente==null)
      throw new NotAuthorizedException("ente non valido");
    log.warn("checking "+(deferred?"deferred":"")+" if user:"+user.getUsername()+" is operatore on ente "+ente.getCodIpaEnte());

    // special case in case an operation is allowed for role ROLE_ADMIN: APP_ADMIN (i.e. user with ROLE_ADMIN on hardcoded adminEnte)
    // is to be considered a superuser and can operate as admin on any ente
    boolean appAdminAccess = !Objects.equals(getAdminEnteId(), mygovEnteId)
        && ADMIN_ROLE_SET.equals(allowedRoles)
        && user.getEntiRoles().getOrDefault(adminEnteCodIpa, Set.of()).contains(Operatore.Role.ROLE_ADMIN.name())
        && !enteDao.isOperatoreForEnte(user.getUsername(), getAdminEnteId()).isEmpty();

    if(appAdminAccess) {
      log.info("appAdmin access for ente: {} - user: {}", ente.getCodIpaEnte(), user.getUsername());
    } else {
      //check if user is authorized on this ente
      if (!user.getEntiRoles().containsKey(ente.getCodIpaEnte()))
        throw new NotAuthorizedException("utente non autorizzato ad operare su ente: " + ente.getCodIpaEnte());
      //check if user has required role
      if (allowedRoles != null && user.getEntiRoles().get(ente.getCodIpaEnte()).stream().noneMatch(allowedRoles::contains))
        throw new NotAuthorizedException("utente non autorizzato su questa funzionalità");
      //check if user is authorized on this ente on DB

      if (enteDao.isOperatoreForEnte(user.getUsername(), mygovEnteId).isEmpty())
        throw new NotAuthorizedException("utente non autorizzato ad operare su ente: " + ente.getCodIpaEnte() + " [DB]");
    }
  }
}