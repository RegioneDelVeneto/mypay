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

import { Dovuto } from '../model/dovuto';
import { Flusso } from '../model/flusso';

@Injectable({
  providedIn: 'root'
})
export class DovutoService {

  constructor(
    private apiInvokerService: ApiInvokerService,
    private baseUrlService: BaseUrlService
  ) { }

  searchDebitiOperatore(ente:Ente, dateFrom: DateTime, dateTo: DateTime, tipoDovuto: TipoDovuto, flusso: Flusso,
    stato: string, causale: string, codFiscale: string, iud: string, iuv: string): Observable<Dovuto[]> {
    let params = new HttpParams();
    params = params
      .append('from', dateFrom.toFormat('yyyy/MM/dd'))
      .append('to', dateTo.toFormat('yyyy/MM/dd'));
    if(stato && stato !== 'tutti')
      params = params.append('codStato', stato);
    if(causale)
      params = params.append('causale', causale);
    if(codFiscale)
      params = params.append('codFiscale', codFiscale);
    if(iud)
      params = params.append('iud', iud);
    if(iuv)
      params = params.append('iuv', iuv);
    if(tipoDovuto)
      params = params.append('myGovEnteTipoDovutoId', tipoDovuto.mygovEnteTipoDovutoId.toString());
    if(flusso)
      params = params.append('nomeFlusso', flusso.nome);
    return this.apiInvokerService.get<Dovuto[]>
      (this.baseUrlService.getOperatoreUrl() + 'dovuti/' + ente.mygovEnteId.toString() + '/search', {params:params}, new Mappers({mapperS2C: Dovuto}));
  }

  searchPagatiOperatore(ente:Ente, dateFrom: DateTime, dateTo: DateTime, tipoDovuto: TipoDovuto, flusso: Flusso,
    stato: string, causale: string, codFiscale: string, iud: string, iuv: string): Observable<Dovuto[]> {
    let params = new HttpParams();
    params = params
      .append('from', dateFrom.toFormat('yyyy/MM/dd'))
      .append('to', dateTo.toFormat('yyyy/MM/dd'));
    if(stato)
      params = params.append('codStato', stato);
    if(causale)
      params = params.append('causale', causale);
    if(codFiscale)
      params = params.append('codFiscale', codFiscale);
    if(iud)
      params = params.append('iud', iud);
    if(iuv)
      params = params.append('iuv', iuv);
    if(tipoDovuto)
      params = params.append('myGovEnteTipoDovutoId', tipoDovuto.mygovEnteTipoDovutoId.toString());
    if(flusso)
      params = params.append('nomeFlusso', flusso.nome);

    return this.apiInvokerService.get<Dovuto[]>
      (this.baseUrlService.getOperatoreUrl() + 'pagati/' + ente.mygovEnteId.toString() + '/search', {params:params}, new Mappers({mapperS2C: Dovuto}));
  }

  getDetailDebitoOperatore(ente:Ente, mygovDovutoId: number): Observable<Dovuto> {
    return this.apiInvokerService.get<Dovuto>
      (this.baseUrlService.getOperatoreUrl() + 'dovuti/' + ente.mygovEnteId.toString() + '/' + mygovDovutoId, null, new Mappers({mapperS2C: Dovuto}));
  }

  // currently, the detail data is provided in the search API call for pagati
  // getDetailPagatoOperatore(ente:Ente, mygovPagatoId: number): Observable<Dovuto> {
  //   return this.apiInvokerService.get<Dovuto>
  //     (this.baseApiUrl + 'pagati/operatore/' + ente.mygovEnteId.toString() + '/' + mygovPagatoId, new Mappers({mapperS2C: Dovuto}));
  // }

  upsertDovuto(mode: string, ente:Ente, mygovDovutoId: number, dovuto: Dovuto): Observable<Dovuto> {
    let targetUrl = mode === 'edit' ?
    this.baseUrlService.getOperatoreUrl() + 'dovuti/update/' + ente.mygovEnteId.toString() + '/' + mygovDovutoId :
    this.baseUrlService.getOperatoreUrl() + 'dovuti/insert/' + ente.mygovEnteId.toString();
    return this.apiInvokerService.post<Dovuto>(targetUrl, dovuto, null, new Mappers({mapper: Dovuto}));
  }

  downloadAvviso(dovuto: Dovuto): Observable<any> {
    return this.apiInvokerService.get<any>(this.baseUrlService.getOperatoreUrl() + 'avvisi/' + dovuto.id + '/pn', {observe: 'response', responseType: 'blob'});
  }

  downloadRicevuta(dovuto: Dovuto): Observable<any> {
    return this.apiInvokerService.get<any>(this.baseUrlService.getOperatoreUrl() + 'pagati/' + dovuto.id + '/rt', {observe: 'response', responseType: 'blob'});
  }

  removeDovuto(ente: Ente, mygovDovutoId: number): Observable<any> {
    return this.apiInvokerService.post<any>(this.baseUrlService.getOperatoreUrl() + 'dovuti/remove/' + ente.mygovEnteId + '/' + mygovDovutoId, null);
  }

  askRicevuta(ente: Ente, mygovDovutoId: number): Observable<string> {
    return this.apiInvokerService.post<string>(this.baseUrlService.getOperatoreUrl() + 'dovuti/' + ente.mygovEnteId + '/' + mygovDovutoId + '/askRT', null, {responseType: 'text'});
  }

}
