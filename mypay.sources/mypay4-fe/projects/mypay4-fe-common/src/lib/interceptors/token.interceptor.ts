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
import { ToastrService } from 'ngx-toastr';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import {
  HttpErrorResponse, HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Injectable } from '@angular/core';

import { AlreadyManagedError } from '../model/already-managed-error';
import { ConfigurationService } from '../services/configuration.service';
import { UserService } from '../services/user.service';
import { PATTERNS } from '../utils/string-utils';
import { generateRandomUUID } from '../utils/utils';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  private useAuthCookie: boolean;
  private useCors: boolean;

  constructor(
    private toastr: ToastrService,
    private conf: ConfigurationService,
    private userService: UserService) {
    this.useAuthCookie = this.conf.getBackendProperty('useAuthCookie');
    const beUrlString = this.conf.getProperty('baseApiUrl');
    var isBeUrlAbsolute = PATTERNS.absoluteUrl.test(beUrlString);
    if(!isBeUrlAbsolute){
      console.log('beUrl: relative - useCors: false');
      this.useCors = false;
    } else {
      const beUrl = new URL(beUrlString);
      const feUrl = new URL(window.location.href);
      this.useCors = beUrl.origin.toLowerCase() !== feUrl.origin.toLowerCase();
      console.log(`beOrigin [${beUrl.origin}] - feOrigin [${feUrl.origin}] - useCors: ${this.useCors}`);
    }
  }

  private setAuthHeader(headers: HttpHeaders) {
    if (headers?.has('Authorization')) {
      const authHeader = headers.get('Authorization');
      if (authHeader.startsWith('Bearer '))
        this.userService.setAccessToken(authHeader.substring(7));
    }
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const baseApiUrl = this.conf.getProperty('baseApiUrl');
    if (!request.url.startsWith(baseApiUrl + 'public/')) {
      const setHeadersValue : { [name: string]: string | string[] } = {
        ReqUid: generateRandomUUID()
      };
      if (!this.useAuthCookie) {
        const accessToken = this.userService.getAccessToken();
        if (!accessToken) {
          if(!request.url.startsWith(baseApiUrl+'doLogout'))
            this.toastr.error('Utente non autenticato.', 'Errore invocando \'' + request.url.substring(baseApiUrl.length - 1) + '\'.', { disableTimeOut: true });
          throw new AlreadyManagedError(new HttpErrorResponse({
            error: 'Utente non autenticato',
            status: 401,
            url: request.url
          }));
        }
        setHeadersValue.Authorization = `Bearer ${accessToken}`;
        request = request.clone({
          setHeaders: setHeadersValue
        });
      } else if(this.useCors){
        request = request.clone({
          setHeaders: setHeadersValue,
          withCredentials: true
        });
      }
    } else if (this.useAuthCookie && this.useCors && request.url.endsWith('/authpassword')) {
      request = request.clone({
        withCredentials: true
      });
    }
    return next.handle(request).pipe(
      map((event: HttpEvent<any>) => {
        if (!this.useAuthCookie && event instanceof HttpResponse)
          this.setAuthHeader(event.headers);
        return event;
      }),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && error?.error?.message === 'TOKEN_EXPIRED') {
          const fakeAuth = this.conf.getBackendProperty<boolean>('fakeAuth', false);
          //token expired: redirect to login
          const toastrMsg = 'È necessario effettuare nuovamente l\'autenticazione.';
          if (!this.toastr.findDuplicate(toastrMsg))
            this.toastr.warning(toastrMsg, 'Autenticazione scaduta', {
              disableTimeOut: !fakeAuth
            });
          if (fakeAuth) {
            // fake auth initiate login procedure
            this.userService.logout();
            this.userService.goToLogin();
          } else {
            // MyId integration: just logout and redirect to home
            this.userService.logout();
          }
          //mark the error as already managed, so that component may deal with it correctly (typically will not show an error message)
          return throwError(new AlreadyManagedError(error));
        } else {
          if (!this.useAuthCookie)
            this.setAuthHeader(error.headers);
          return throwError(error);
        }
      })
    );
  }
}
