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
  ConfigurationService, UserService, genericRetryStrategy
} from 'projects/mypay4-fe-common/src/public-api';
import { BehaviorSubject, Observable, ReplaySubject, Subject, from, of, throwError } from 'rxjs';
import { catchError, first, flatMap, retryWhen } from 'rxjs/operators';

import { Component, Injectable, NgZone, Renderer2 } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

declare let grecaptcha: any;

@Injectable({
  providedIn: 'root'
})
export class RecaptchaService {

  private ready = new ReplaySubject<any>(1);
  private active = new BehaviorSubject<boolean>(false);

  private enabled: boolean;
  private siteKey: string;
  private siteKeyV2: string;
  private recaptchaV3Prefix: string = '';

  constructor(
    private conf: ConfigurationService,
    private userService: UserService,
    private dialog: MatDialog,
    private ngZone: NgZone,
  ) {
    this.siteKey = this.conf.getBackendProperty('recaptchaSiteKey');
    this.siteKeyV2 = this.conf.getBackendProperty('recaptchaV2SiteKey');
    this.enabled = this.siteKey ? true : false;
    console.log("recaptcha enabled: "+this.enabled);
  }

  public isEnabled():boolean {
    return this.enabled;
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
    if(this.enabled && typeof grecaptcha === 'undefined'){
      console.log("init recaptcha");
      if(this.conf.getFlag("recaptchaForceV2", 'false')==='true')
        this.recaptchaV3Prefix = "V2Force|";
      const script = renderer.createElement('script');
      script.src = `https://www.google.com/recaptcha/api.js?render=${this.siteKey}`;
      renderer.appendChild(document.head, script);
      this.ready.next(true);
      console.log("init recaptcha: done");
    }
    //console.log('recaptcha: activating');
    if(this.enabled)
      this.active.next(true);
  }

  public initFallback(callback: (recaptchaResponse: string)=>void){
    grecaptcha.ready(() => {
      const recaptchaV2DialogRef = this.dialog.open(RecaptchaV2Dialog);
      const recaptchaV2WidgetId = grecaptcha.render('recaptcha_fallback', {
          'sitekey' : this.siteKeyV2,
          'callback' : (response) => {
            setTimeout(()=>{
              grecaptcha.reset(recaptchaV2WidgetId);
              this.ngZone.run(() => {
                recaptchaV2DialogRef.close();
                callback(response);
              });
            });
          }
      });
    });
  }

  private submitToken(action: string):Observable<string>{
    if(this.enabled)
      return this.ready.pipe(
        flatMap(() => from(grecaptcha.execute(this.siteKey, {action: action}))),
        retryWhen(genericRetryStrategy({scalingDuration: 100})),
        catchError(error => {
          console.log('error submitting captcha', error);
          return throwError(error || 'Errore durante la verifica del codice reCaptcha');
        }),
        first()
      ) as Observable<string>;
    else
      return of('reCaptcha_disabled');
  }

  

  public submitWithRecaptchaHandling<T>(action: string,
    payloadFunAnon: (recaptchaResponse: string)=>Observable<T>,
    payloadFunLogged?: ()=>Observable<T>) {

    if(this.userService.isLogged())
      return payloadFunLogged?.() ?? of(null);
    else
      return this.submitToken(action).pipe(
        flatMap(token => payloadFunAnon(this.recaptchaV3Prefix + token)),
        catchError(error => {
          if(error?.status===471 && error?.error==='recaptcha_low_score' && this.siteKeyV2){
            console.log('falling back to recaptcha V2');
            const v2Callback = new Subject<string>();
            this.ngZone.runOutsideAngular(()=>{
              this.initFallback(recaptchaResponseV2 => {
                v2Callback.next(recaptchaResponseV2);
                v2Callback.complete();
              });
            });
            return v2Callback.asObservable().pipe(
              flatMap(token => payloadFunAnon("V2|"+token)),
            );
          } else {
            return throwError(error);
          }
        })
      );

  }

}


@Component({
  selector: 'recaptchaV2-dialog',
  templateUrl: 'recaptchaV2-dialog.html',
})
export class RecaptchaV2Dialog {
  constructor() {}
}