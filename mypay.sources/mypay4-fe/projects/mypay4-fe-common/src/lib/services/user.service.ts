/*
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
import * as _ from 'lodash';
import { BehaviorSubject, Observable, of, Subject } from 'rxjs';
import { catchError, first, map } from 'rxjs/operators';

import { Location } from '@angular/common';
import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';

import { LoginComponent } from '../components/login/login.component';
import { versionInfo } from '../environments/version';
import { Mappers } from '../mapper/mappers';
import { AppInfo } from '../model/app-info';
import { EMAIL_SOURCE_TYPE, User } from '../model/user';
import { ApiInvokerService } from './api-invoker.service';
import { BaseUrlService } from './base-url.service';
import { ConfigurationService } from './configuration.service';
import { LocalStorageService } from './local-storage.service';
import { MyPayBreadcrumbsService } from './my-pay-breadcrumbs.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  public static readonly BACK_OFFICE_ADMIN_ROLE = Math.random().toString(36).substring(2, 15);
  public static readonly BACK_OFFICE_ADMIN_ENTE_ROLE = Math.random().toString(36).substring(2, 15);

  private loggedUser: User = null;
  private anyUserRoles: string[];
  private backofficeAdmin: boolean = false;
  private backofficeAdminEnte: boolean = false;
  private accessToken: string = null;
  private loggedUserObs: Subject<User> = new BehaviorSubject<User>(null);
  private dialogRef: MatDialogRef<LoginComponent>;

  private adminEnte: string;
  private fakeAuth: boolean;
  private useAuthCookie:boolean;
  private useLocalStorage: boolean;


  constructor(
    private apiInvokerService: ApiInvokerService,
    conf: ConfigurationService,
    private baseUrlService: BaseUrlService,
    private localStorageService: LocalStorageService,
    public dialog: MatDialog,
    private location: Location,
    private router: Router,
    private breadcrumbsService: MyPayBreadcrumbsService) {
      this.fakeAuth = conf.getBackendProperty<boolean>('fakeAuth', false);
      this.useAuthCookie = conf.getBackendProperty<boolean>('useAuthCookie', false);
      this.adminEnte = conf.getBackendProperty('adminEnte');
      this.useLocalStorage = this.localStorageService.isLocalStorageSupported && !this.useAuthCookie;
      //check local storage for stored user
      if(this.useLocalStorage){
        this.setUser(this.localStorageService.get('loggedUser', new Mappers({mapper:User})));
        this.setAccessToken(this.localStorageService.get('accessToken'));
        if(this.loggedUser)
          console.log('user and accessToken retrieved from local storage: ',this.loggedUser);
      }
      //check if an http-only cookie exists (this can only be made server-side) and user is therefore already authenticated
      // the check is made in the configuration service at app-loading phase
      if(this.useAuthCookie && conf.getUserFromCookie()){
        this.setUser(conf.getUserFromCookie(), false);
        console.log('user set from cookie: ',this.getLoggedUser());
      }
  }

  isRoleAuthorized(role: string){
    if(role === UserService.BACK_OFFICE_ADMIN_ROLE)
      return this.backofficeAdmin;
    else if(role === UserService.BACK_OFFICE_ADMIN_ENTE_ROLE)
      return this.backofficeAdminEnte;
    else
      return this.anyUserRoles?.includes(role);
  }

  goToLogin(){
    if(this.fakeAuth){
      if(!this.dialogRef){
        this.logout(false);
        this.dialogRef = this.dialog.open(LoginComponent, {width: '500px', height: '450px'});
        this.dialogRef.afterClosed().pipe(first()).subscribe( () => {
          this.dialogRef = null;
          if(!this.isLogged())
            this.router.navigate(['home']);
        });
      }
    } else {
      const loginUrl = this.baseUrlService.getBaseUrlApi() + 'saml/login?callbackUrl='+window.location.origin + this.location.prepareExternalUrl('logged');
      window.location.href = loginUrl;
    }
  }

  loginPassword(username: string, password: string, rememberMe: boolean = false): Observable<void> {
    return this.apiInvokerService.post<User>(this.baseUrlService.getPubCittadinoUrl() + 'authpassword',
          {username: username, password: password}, null, new Mappers({mapperS2C:User}))
      .pipe(map( user => {
        if(!user || !user.username)
          throw 'errore di autenticazione';
        this.setUser(user, rememberMe);
        return null;
      }));
  }

  loginToken(loginToken: string, rememberMe: boolean = false): Observable<User> {
    return this.apiInvokerService.post<User>(this.baseUrlService.getPubCittadinoUrl() + 'authtoken', loginToken, null, new Mappers({mapperS2C:User}))
    .pipe(map( user => {
      this.setUser(user, rememberMe);
      return user;
    }));
  }

  public retrieveAppInfo():Observable<AppInfo> {
    return this.apiInvokerService.post<AppInfo>(this.baseUrlService.getPubCittadinoUrl() + 'info/app',null, null, new Mappers({mapperS2C:AppInfo}));
  }

  public getAppInfoString(): Observable<string> {
    return this.retrieveAppInfo().pipe( map (appInfo => {
      const buildBeString = appInfo.commitDistance===0
        ? appInfo.lastTag
        : (appInfo.gitHash+"-"+appInfo.branchName?.substring(0,3)+"@"+appInfo.buildTime?.toISO());
      const buildFeString = _.isEmpty(versionInfo.tag)
        ? (versionInfo.gitHash+"-"+versionInfo.branchName?.substring(0,3)+"@"+versionInfo.buildTime)
        : versionInfo.tag;
      const appInfoString =
        //"Versione BE: "+appInfo.version+"</br>"+
        "Versione BE: "+buildBeString+"</br>"+
        "Start BE: "+appInfo.startTime?.toISO()+"</br>"+
        //"Versione FE: "+versionInfo.version+"</br>"+
        "Versione FE: "+buildFeString;
      return appInfoString;
    }) );
  }

  public updateUserData(newUserData: User): void {
    if(!this.isLogged() || !newUserData)
      return;

    const loggedUser = this.getLoggedUser();
    loggedUser.nazioneId = newUserData.nazioneId;
    loggedUser.provinciaId = newUserData.provinciaId;
    loggedUser.comuneId = newUserData.comuneId;
    loggedUser.cap = newUserData.cap;
    loggedUser.indirizzo = newUserData.indirizzo;
    loggedUser.civico = newUserData.civico;

    this.setUser(loggedUser, true);
  }

  private setUser(user: User, rememberMe: boolean = null) {
    const previousUser = this.loggedUser;

    //capitalize some fields
    if(user?.statoNascita)
      user.statoNascita = user?.statoNascita?.replace(/\w+/g, _.capitalize);
    if(user?.comuneNascita)
      user.comuneNascita = user?.comuneNascita?.replace(/\w+/g, _.capitalize);

    this.loggedUser = user;
    if(this.useLocalStorage){
      if(this.loggedUser && rememberMe != null)
        this.loggedUser.rememberMe = rememberMe;
      if(this?.loggedUser?.rememberMe){
        this.localStorageService.set('loggedUser', user, new Mappers({mapper:User}));
        //force save access token if this is present (rememberme could not be set when originally the token was set)
        if(this.accessToken)
          this.setAccessToken(this.accessToken);
      } else
        this.localStorageService.remove('loggedUser');
    }

    if(user){
      const setRoles = new Set<string>();
      Object.values(user.entiRoles || {}).forEach((roles:string[]) => roles?.forEach(role => setRoles.add(role)));
      this.anyUserRoles = Array.from(setRoles);
      this.backofficeAdmin = user.entiRoles?.[this.adminEnte]?.includes('ROLE_ADMIN');
      this.backofficeAdminEnte = Object.values(user.entiRoles).some(rolesForEnte => rolesForEnte.includes('ROLE_ADMIN'));
    } else {
      this.anyUserRoles = [];
      this.backofficeAdmin = false;
      this.backofficeAdminEnte = false;
    }

    if(previousUser?.username!==user?.username){
      this.loggedUserObs.next(this.loggedUser);
    }
  }

  setAccessToken(accessToken: string) {
    this.accessToken = accessToken;
    if(this.useLocalStorage){
      if(this?.loggedUser?.rememberMe && accessToken)
        this.localStorageService.set('accessToken', accessToken);
      else
        this.localStorageService.remove('accessToken');
    }
    //console.log('set access token to: ',accessToken);
  }

  logout(redirectHome:boolean = true): void {
    //remove logged user
    this.setUser(null);
    this.setAccessToken(null);
    //remove breadcrumbs
    this.breadcrumbsService.resetBreadcrumbs();
    //invoke server logout service (needed to remove cookie)
    let logoutFun = (!this.fakeAuth && this.useAuthCookie) ?
      this.apiInvokerService.get<void>(this.baseUrlService.getPubCittadinoUrl() + 'doLogout').pipe(catchError(() => of(1))) :
      of(1);
    logoutFun.pipe(first()).subscribe( () => {
      //redirect to home
      if(redirectHome){
        //console.log('redirecting to home');
        this.router.navigate(['home']);
      }
    });
  }

  changeEmailSourceFromBackofficeToValidated(): Observable<void> {
    return this.apiInvokerService.post<User>(this.baseUrlService.getCittadinoUrl() + 'email/confirmBackoffice', null)
      .pipe(map( () => {
        this.loggedUser.emailSourceType = EMAIL_SOURCE_TYPE.C;
        return null;
      }));
  }

  isLogged(): boolean {
    return this.loggedUser != null;
  }

  getLoggedUser(): User {
    return this.loggedUser;
  }

  getAnyUserRoles(): string[] {
    return this.anyUserRoles;
  }

  getAccessToken(): string {
    return this.accessToken;
  }

  getLoggedUserObs(): Subject<User> {
    return this.loggedUserObs;
  }

  getLoggedUserString(): string {
    return this.loggedUser != null ? this.loggedUser.nome+' '+this.loggedUser.cognome : null;
  }
}
