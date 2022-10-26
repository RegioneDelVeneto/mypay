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
import {
    ConfigurationService, genericRetryStrategy
} from 'projects/mypay4-fe-common/src/public-api';
import { BehaviorSubject, from, Observable, of, ReplaySubject, throwError } from 'rxjs';
import { catchError, first, flatMap, retryWhen } from 'rxjs/operators';

import { Injectable, Renderer2 } from '@angular/core';

declare var grecaptcha: any;

@Injectable({
  providedIn: 'root'
})
export class RecaptchaService {

  private ready = new ReplaySubject<any>(1);
  private active = new BehaviorSubject<boolean>(false);

  private enabled: boolean;
  private siteKey: string;

  constructor(
    private conf: ConfigurationService,
  ) {
  }

  public isActive():boolean {
    return this.active.value;
  }
  public isActiveObs():Observable<boolean> {
    return this.active.asObservable();
  }
  public deactivate() {
    //console.log('recaptcha: deactivating');
    if(this.enabled && this.active.getValue()){
      this.active.next(false);
      //console.log('recaptcha: deactivated');
    }
  }

  public init(renderer: Renderer2) {
    if(typeof grecaptcha === 'undefined'){
      console.log("init recaptcha");
      this.siteKey = this.conf.getBackendProperty('recaptchaSiteKey');
      this.enabled = this.siteKey ? true : false;
      console.log("recaptcha enabled: "+this.enabled);
      if(this.enabled){
        const script = renderer.createElement('script');
        script.src = `https://www.google.com/recaptcha/api.js?render=${this.siteKey}`;
        renderer.appendChild(document.head, script);
        this.ready.next(true);
        console.log("init recaptcha: done");
      }
    }
    //console.log('recaptcha: activating');
    if(this.enabled)
      this.active.next(true);
  }

  public submitToken(action: string){
    if(this.enabled)
      return this.ready.pipe(
        flatMap(() => from(grecaptcha.execute(this.siteKey, {action: action}))),
        retryWhen(genericRetryStrategy({scalingDuration: 100})),
        catchError(error => {
          console.log('error submitting captcha', error);
          return throwError(error || 'Errore durante la verifica del codice reCaptcha');
        }),
        first()
      );
    else
      return of('reCaptcha_disabled');
  }

}
