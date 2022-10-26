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
    ApiInvokerService, BaseUrlService, Mappers
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable } from 'rxjs';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Giornale } from '../model/giornale';

@Injectable({
  providedIn: 'root'
})
export class AdminGiornaleService {

  constructor(
    private apiInvokerService: ApiInvokerService,
    private baseUrlService: BaseUrlService,
  ) { }


  searchGiornale(type: GiornaleType, idDominio: string, iuv: string, tipoEvento: string, categoriaEvento: string, idPsp: string,
    esito: string, dataOraEventoFrom: DateTime, dataOraEventoTo: DateTime): Observable<Giornale[]> {
    let params = new HttpParams();
    if(idDominio)
      params = params.append('idDominio', idDominio);
    if(iuv)
      params = params.append('iuv', iuv);
    if(tipoEvento)
      params = params.append('tipoEvento', tipoEvento);
    if(categoriaEvento)
      params = params.append('categoriaEvento', categoriaEvento);
    if(idPsp)
      params = params.append('idPsp', idPsp);
    if(esito)
      params = params.append('esito', esito);
    if(dataOraEventoFrom)
      params = params.append('dataEventoFrom', dataOraEventoFrom.toFormat('yyyy/MM/dd'));
    if(dataOraEventoTo)
      params = params.append('dataEventoTo', dataOraEventoTo.toFormat('yyyy/MM/dd'));
    return this.apiInvokerService.get<Giornale[]>
      (this.baseUrlService.getOperatoreUrl() + 'admin/giornale/'+type+'/search', {params:params}, new Mappers({mapperS2C: Giornale}));
  }

  getDetailGiornale(type: GiornaleType, mygovGiornaleId: number): Observable<Giornale> {
    return this.apiInvokerService.get<Giornale>
      (this.baseUrlService.getOperatoreUrl() + 'admin/giornale/'+type+'/detail/'+mygovGiornaleId);
  }

  getAllPsp(type: GiornaleType): Observable<string[]> {
    return this.apiInvokerService.get<string[]>
      (this.baseUrlService.getOperatoreUrl() + 'admin/giornale/'+type+'/psp');
  }

  getAllOpzioni(type: GiornaleType): Observable<{[key:string]:string[]}> {
    return this.apiInvokerService.get<{[key:string]:string[]}>
      (this.baseUrlService.getOperatoreUrl() + 'admin/giornale/'+type+'/opzioni');
  }

}

export type GiornaleType = "pa" | "fesp";
