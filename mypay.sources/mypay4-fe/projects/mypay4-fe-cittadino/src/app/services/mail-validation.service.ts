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
  ApiInvokerService, BaseUrlService, Mappers
} from 'projects/mypay4-fe-common/src/public-api';
import { Subject, throwError } from 'rxjs';
import { map } from 'rxjs/operators';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { MailValidationRequest, MailValidationResponse } from '../model/mail-validation';
import { UserMailValidation } from '../model/user-mail-validation';

@Injectable({
  providedIn: 'root'
})
export class MailValidationService {

  private forcedMailValidationWarningSubject = new Subject<void>();

  constructor(
    private baseUrlService: BaseUrlService,
    private apiInvokerService: ApiInvokerService
  ) { }

  //
  // validate email address when submitting carrello as anonymous
  //

  requestMailValidation(emailAddress: string, recaptchaResponse: any) {
    const params = new HttpParams()
      .append('emailAddress', emailAddress)
      .append('recaptcha', recaptchaResponse);

    return this.apiInvokerService.post<MailValidationRequest>(this.baseUrlService.getPubCittadinoUrl() + 'mailvalidation/request', null, {params:params})
      .pipe(map(r => {if(r.error) throwError(r.error); return r;}));
  }

  verifyMailValidation(mailValidationRequest: MailValidationRequest, recaptchaResponse: any) {
    const params = new HttpParams()
      .append('recaptcha', recaptchaResponse);

    return this.apiInvokerService.post<MailValidationResponse>(
      this.baseUrlService.getPubCittadinoUrl() + 'mailvalidation/verify', mailValidationRequest, {params:params}, new Mappers({mapperS2C:MailValidationResponse}));
  }


  //
  // manual validation when logged user change email address
  //

  userMailCheckValidationStatus() {
    return this.apiInvokerService.get<UserMailValidation>(this.baseUrlService.getCittadinoUrl() + 'email', null, new Mappers({mapperS2C:UserMailValidation}));
  }

  userMailSendNewEmail(emailAddress: string) {
    const params = new HttpParams()
      .append('emailAddress', emailAddress);

    return this.apiInvokerService.post<UserMailValidation>(this.baseUrlService.getCittadinoUrl() + 'email/init', null, {params:params}, new Mappers({mapperS2C:UserMailValidation}));
  }

  userMailResetNewEmail() {
    return this.apiInvokerService.post<void>(this.baseUrlService.getCittadinoUrl() + 'email/reset', null);
  }

  userMailSendCode(code: string) {
    const params = new HttpParams()
      .append('code', code);

    return this.apiInvokerService.post<UserMailValidation>(this.baseUrlService.getCittadinoUrl() + 'email/validate', null, {params:params}, new Mappers({mapperS2C:UserMailValidation}));
  }

  setForcedMailValidationWarning(){
    this.forcedMailValidationWarningSubject.next();
  }

  getForcedMailValidationWarningObservable(){
    return this.forcedMailValidationWarningSubject.asObservable();
  }
}
