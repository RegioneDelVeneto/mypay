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
    ApiInvokerService, BaseUrlService, Ente, LocalCacheService, Mappers, TipoDovuto
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable } from 'rxjs';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Debito } from '../model/debito';
import { Spontaneo } from '../model/spontaneo';
import { SpontaneoForm } from '../model/spontaneo-form';

@Injectable({
  providedIn: 'root'
})
export class SpontaneoService {

  constructor(
    private baseUrlService: BaseUrlService,
    private apiInvokerService: ApiInvokerService,
    private localCacheService: LocalCacheService,
  ) { }

  getAllEntiSpontanei(): Observable<Ente[]> {
    return this.localCacheService.manageThumbLogoEntiCache(
      this.apiInvokerService.get<Ente[]>(this.baseUrlService.getPubCittadinoUrl() + 'spontaneo?logoMode=hash'),
      (apiInvokerServiceRef: ApiInvokerService) => apiInvokerServiceRef.get<Ente[]>(this.baseUrlService.getPubCittadinoUrl() + 'spontaneo?logoMode=thumb')
    );
  }

  getEnteSpontaneoByCodIpa(codIpaEnte: string): Observable<Ente[]> {
    return this.localCacheService.manageThumbLogoEntiCache(
      this.apiInvokerService.get<Ente[]>(this.baseUrlService.getPubCittadinoUrl() + `spontaneo/ente/${codIpaEnte}?logoMode=hash`),
      (apiInvokerServiceRef: ApiInvokerService) => apiInvokerServiceRef.get<Ente[]>(this.baseUrlService.getPubCittadinoUrl() + `spontaneo/ente/${codIpaEnte}?logoMode=hash`)
    );
  }

  getListTipoDovutoByEnte(ente: Ente): Observable<TipoDovuto[]> {
    return this.apiInvokerService.get<TipoDovuto[]>(this.baseUrlService.getPubCittadinoUrl() + 'spontaneo/' + ente.mygovEnteId + '/tipiDovuto');
  }

  initializeForm(ente: Ente, tipoDovuto: TipoDovuto): Observable<SpontaneoForm>{
    return this.initializeFormImpl(false, ente, tipoDovuto);
  }

  initializeFormAnonymous(ente: Ente, tipoDovuto: TipoDovuto, recaptchaResponse: any): Observable<SpontaneoForm>{
    return this.initializeFormImpl(true, ente, tipoDovuto, recaptchaResponse);
  }

  private initializeFormImpl(anonymous: boolean, ente: Ente, tipoDovuto: TipoDovuto, recaptchaResponse?: any): Observable<SpontaneoForm>{
    let params = null;
    if(anonymous)
      params = new HttpParams().append('recaptcha', recaptchaResponse);
    return this.apiInvokerService.get<SpontaneoForm>(
      this.baseUrlService.getCittadinoUrl(anonymous) + 'spontaneo/initialize/' + ente.codIpaEnte + '/' + tipoDovuto.codTipo, {params:params});
  }

  validateForm(ente: Ente, tipoDovuto: TipoDovuto, spontaneoForm: SpontaneoForm): Observable<Spontaneo>{
    return this.validateFormImpl(false, ente, tipoDovuto, spontaneoForm);
  }

  validateFormAnonymous(ente: Ente, tipoDovuto: TipoDovuto, spontaneoForm: SpontaneoForm, recaptchaResponse: any): Observable<Spontaneo>{
    return this.validateFormImpl(true, ente, tipoDovuto, spontaneoForm, recaptchaResponse);
  }

  private validateFormImpl(anonymous: boolean, ente: Ente, tipoDovuto: TipoDovuto, spontaneoForm: SpontaneoForm, recaptchaResponse?: any): Observable<Spontaneo>{
    let params = null;
    if(anonymous)
      params = new HttpParams().append('recaptcha', recaptchaResponse);
    return this.apiInvokerService.post<Spontaneo>(
      this.baseUrlService.getCittadinoUrl(anonymous) + 'spontaneo/validate/' + ente.codIpaEnte + '/' + tipoDovuto.codTipo, spontaneoForm, {params:params});
  }

  prepareAvviso(spontaneo: Spontaneo): Observable<Debito> {
    return this.apiInvokerService.post<Debito>(this.baseUrlService.getCittadinoUrl() + 'spontaneo/prepareAvviso', spontaneo, {},new Mappers({mapperS2C: Debito}));
  }

  prepareAvvisoAnonymous(spontaneo: Spontaneo, recaptchaResponse: any): Observable<Debito> {
    const params = new HttpParams().append('recaptcha', recaptchaResponse);
    return this.apiInvokerService.post<Debito>(this.baseUrlService.getPubCittadinoUrl() + 'spontaneo/prepareAvviso', spontaneo, {params:params}, new Mappers({mapperS2C: Debito}));
  }

}
