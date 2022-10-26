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

import { Debito } from '../model/debito';

@Injectable({
  providedIn: 'root'
})
export class DebitoService {

  constructor(
    private baseUrlService: BaseUrlService,
    private apiInvokerService: ApiInvokerService
  ) { }

  searchDebiti(ente: Ente, dateFrom: DateTime, dateTo: DateTime, causale: string, tipoDovuto: TipoDovuto): Observable<Debito[]> {
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

    return this.apiInvokerService.get<Debito[]>
      (this.baseUrlService.getCittadinoUrl() + 'dovuti/search', {params:params}, new Mappers({mapperS2C: Debito}));
  }

  searchLastDebiti(num: number = 5): Observable<Debito[]> {
    let params = new HttpParams().append('num', ''+num);
    return this.apiInvokerService.get<Debito[]>
      (this.baseUrlService.getCittadinoUrl() + 'dovuti/last', {params:params}, new Mappers({mapperS2C: Debito}));
  }

}
