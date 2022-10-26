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
import { DateTime } from 'luxon';
import {
    ApiInvokerService, BaseUrlService, Ente, Mappers, TipoDovuto
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable } from 'rxjs';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Pagato } from '../model/pagato';

@Injectable({
  providedIn: 'root'
})
export class PagatoService {

  constructor(
    private baseUrlService: BaseUrlService,
    private apiInvokerService: ApiInvokerService
  ) { }

  searchPagati(ente: Ente, dateFrom: DateTime, dateTo: DateTime, causale: string, tipoDovuto: TipoDovuto): Observable<Pagato[]> {
    let params = new HttpParams();
    params = params
      .append('from', dateFrom.toFormat('yyyy/MM/dd'))
      .append('to', dateTo.toFormat('yyyy/MM/dd'));
    if(ente)
      params = params.append('codIpaEnte', ente.codIpaEnte);
    if(causale)
      params = params.append('causale', causale);
    if(tipoDovuto)
      params = params.append('codTipoDovuto', tipoDovuto.codTipo);

    return this.apiInvokerService.get<Pagato[]>
      (this.baseUrlService.getCittadinoUrl() + 'pagati/search', {params:params}, new Mappers({mapperS2C: Pagato}));
  }

  searchLastPagati(num: number = 5): Observable<Pagato[]> {
    let params = new HttpParams().append('num', ''+num);
    return this.apiInvokerService.get<Pagato[]>
      (this.baseUrlService.getCittadinoUrl() + 'pagati/last', {params:params}, new Mappers({mapperS2C: Pagato}));
  }

  downloadRt(pagato: Pagato): any {
    let params = new HttpParams();
    if(pagato.securityTokenRt)
      params = params.append('securityToken', pagato.securityTokenRt);
    return this.apiInvokerService.get<any>(this.baseUrlService.getCittadinoUrl() + 'pagati/' + pagato.id + '/rt', {params:params, observe: 'response', responseType: 'blob'});
  }

  downloadRtAnonymous(pagato: Pagato, recaptchaResponse: any): any {
    const params = new HttpParams()
      .append('recaptcha', recaptchaResponse)
      .append('securityToken', pagato.securityTokenRt);
    return this.apiInvokerService.get<any>(this.baseUrlService.getPubCittadinoUrl() + 'pagati/' + pagato.id + '/rt', {params:params, observe: 'response', responseType: 'blob'});
  }

}
