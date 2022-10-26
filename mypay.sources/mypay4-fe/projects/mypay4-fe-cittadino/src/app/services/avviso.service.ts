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
    ApiInvokerService, BaseUrlService, mapper
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Debito } from '../model/debito';
import { Pagato } from '../model/pagato';

@Injectable({
  providedIn: 'root'
})
export class AvvisoService {

  constructor(
    private baseUrlService: BaseUrlService,
    private apiInvokerService: ApiInvokerService
  ) { }

  searchAvvisi(numeroAvviso: string, owner: boolean, codIdUnivoco: string, anagrafica: string): Observable<any> {
    return this.searchAvvisiImpl(false, numeroAvviso, owner, codIdUnivoco, anagrafica, null);
  }

  searchAvvisiAnonymous(numeroAvviso: string, codIdUnivoco: string, anagrafica: string, recaptchaResponse: any): Observable<any> {
    return this.searchAvvisiImpl(true, numeroAvviso, null, codIdUnivoco, anagrafica, recaptchaResponse);
  }

  private searchAvvisiImpl(anonymous:boolean, numeroAvviso: string, owner: boolean, codIdUnivoco: string, anagrafica: string, recaptchaResponse: any): Observable<any> {
    let params = new HttpParams();
    params = params.append('numeroAvviso', numeroAvviso);
    if(!owner) {
      if(codIdUnivoco)
        params = params.append('codIdUnivoco', codIdUnivoco);
      if(anagrafica)
        params = params.append('anagrafica', anagrafica);
    }
    if(anonymous){
      params = params.append('recaptcha', recaptchaResponse);
    } else {
      params = params.append('owner', String(owner));
    }

    return this.apiInvokerService.get<any>(this.baseUrlService.getCittadinoUrl(anonymous) + 'avvisi/search', {params:params})
      .pipe(map(response => {
        response.debiti = response.debiti?.map(item => mapper(item, Debito.MAPPER_S2C_DEF));
        response.pagati = response.pagati?.map(item => mapper(item, Pagato.MAPPER_S2C_DEF));
        return response;
      }));
  }

  downloadAvviso(debito: Debito): any {
    return this.downloadAvvisoImpl(false, debito);
  }

  downloadAvvisoAnonymous(debito: Debito, recaptchaResponse: any): any {
    return this.downloadAvvisoImpl(true, debito, recaptchaResponse);
  }

  private downloadAvvisoImpl(anonymous:boolean, debito: Debito, recaptchaResponse?: any): any {
    let params = new HttpParams();
    if(anonymous)
      params = params.append('recaptcha', recaptchaResponse);
    if(debito.securityTokenAvviso)
      params = params.append('securityToken', debito.securityTokenAvviso);
    return this.apiInvokerService.get<any>(this.baseUrlService.getCittadinoUrl(anonymous) + 'avvisi/download/' + debito.id, {params:params, observe: 'response', responseType: 'blob'});
  }

}
