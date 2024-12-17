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
import { BehaviorSubject, Observable } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { Injectable, NgZone } from '@angular/core';

import { CookieConsent } from '../model/cookie-consent';

// from https://cookie-bar.eu/
declare function setupCookieBar(): any;

@Injectable({
  providedIn: 'root'
})
export class CookieService {

  private missingNeededConsent = new BehaviorSubject<boolean>(false);

  private consentState: CookieConsent = new CookieConsent();
  private consentStateObs = new BehaviorSubject<CookieConsent>(this.consentState);

  constructor(
    private zone: NgZone
  ) {
    this.getCookieObservable().subscribe(cookie => {
      //console.log("modified cookies: ", cookie);
      const cookieValue = cookie.match(/(;)?cookiebar=([^;]*);?/)?.[2];
      const newConsentState = new CookieConsent();
      newConsentState.cookieConsentChoice = cookieValue != null;
      newConsentState.cookieAll = cookieValue === 'CookieAllowed';
      newConsentState.cookieTracking = cookieValue === 'CookieCustomized' && cookie.match(/(;)?cookiebar-tracking=([^;]*);?/)?.[2] === 'true';
      newConsentState.cookieThirdParty = cookieValue === 'CookieCustomized' && cookie.match(/(;)?cookiebar-third-party=([^;]*);?/)?.[2] === 'true';

      if(_.isEqual(this.consentState, newConsentState))
        return;
      this.consentState = newConsentState;
      console.log("new consent state: ",JSON.stringify(this.consentState));
      this.consentStateObs.next(this.consentState);
    });
  }

  public getConsentStateObs() {
    return this.consentStateObs.pipe(debounceTime(100));
  }

  public getConsentState() {
    return this.consentState;
  }

  private getBaseURL(): string{
    try{
      return new URL(document.getElementsByTagName("base")[0].href).pathname.replace(/\/$/, "") || '/';
    }catch(error){
      console.log(error);
      return '/';
    }
  }

  resetCookieConsentBar(){
    document.cookie="cookiebar=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/";
    document.cookie="cookiebar-tracking=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/";
    document.cookie="cookiebar-third-party=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/";

    //remove margin in case cookie-bar is already displayed
    const elem = document.getElementById('cookie-bar');
    const height = elem?.clientHeight;
    if (elem?.getBoundingClientRect().top === 0) {
      const currentTop = parseInt(document.getElementsByTagName('body')[0].style.marginTop);
      document.getElementsByTagName('body')[0].style.marginTop = currentTop - height + 'px';
    } else if(elem) {
      const currentBottom = parseInt(document.getElementsByTagName('body')[0].style.marginBottom);
      document.getElementsByTagName('body')[0].style.marginBottom = currentBottom -height + 'px';
    }
    //reset cookie-bar
    setupCookieBar();
  }

  private getCookieObservable(): Observable<string> {
    const key = 'cookie';
    const target = Object.getPrototypeOf(Object.getPrototypeOf(document));
    const setter = Object.getOwnPropertyDescriptor(target, key).set;
    const subject = new BehaviorSubject<string>(document.cookie);
    Object.defineProperty(target, key, {
        set: value => {
            this.zone.run(() => {
              setter.call(document, value);
              subject.next(document.cookie);
            });
        }
    });
    return subject.asObservable();
  }

  public getMissingNeededConsentObs() {
    return this.missingNeededConsent.asObservable();
  }

  public setMissingNeededConsent():void {
    if(this.missingNeededConsent.getValue()!=true){
      this.missingNeededConsent.next(true);
    }
  }

  public unsetMissingNeededConsent():void {
    if(this.missingNeededConsent.getValue()!=false){
      this.missingNeededConsent.next(false);
    }
  }

}
