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

import { FlussoFile, FlussoImport, TipoFlusso } from '../model/flussi-gestione';
import { Flusso } from '../model/flusso';

@Injectable({
  providedIn: 'root'
})
export class FlussoService {

  constructor(
    private apiInvokerService: ApiInvokerService,
    private baseUrlService: BaseUrlService
  ) { }

  getFlussiByEnte(ente: Ente): Observable<Flusso[]> {
    return this.apiInvokerService.get<Flusso[]>(this.baseUrlService.getOperatoreUrl() + 'flussi/byEnteId/'+ente.mygovEnteId);
  }

  searchFlussiImport(ente: Ente, nomeFlusso: string, dateFrom: DateTime, dateTo: DateTime): Observable<FlussoImport[]> {
    let params = new HttpParams();
    if(nomeFlusso)
      params = params.append('nomeFlusso', nomeFlusso);
    params = params
      .append('from', dateFrom.toFormat('yyyy/MM/dd'))
      .append('to', dateTo.toFormat('yyyy/MM/dd'));
    return this.apiInvokerService.get<FlussoImport[]>
      (this.baseUrlService.getOperatoreUrl() + 'flussi/import/'+ente.mygovEnteId, {params:params}, new Mappers({mapperS2C: FlussoImport}) );
  }

  downloadFlussoLog(ente: Ente, fileName: string): any {
    let params = new HttpParams();
    params = params
      .append('fileName', fileName)
    return this.apiInvokerService.get<any>
      (this.baseUrlService.getOperatoreUrl() + 'flussi/import/'+ente.mygovEnteId+'/log', {params: params});
  }

  insertFlussiExport(ente: Ente, dateFrom: DateTime, dateTo: DateTime, tipoDovuto: TipoDovuto, versioneTracciato: string): Observable<number> {
    let params = new HttpParams();
    params = params
      .append('from', dateFrom.toFormat('yyyy/MM/dd'))
      .append('to', dateTo.toFormat('yyyy/MM/dd'))
      .append('versioneTracciato', versioneTracciato);
    if(tipoDovuto)
      params = params.append('tipoDovuto', tipoDovuto.codTipo);
    return this.apiInvokerService.get<number>(this.baseUrlService.getOperatoreUrl() + 'flussi/export/insert/'+ente.mygovEnteId, {params: params});
  }

  searchFlussiExport(ente: Ente, nomeFlusso: string, dateFrom: DateTime, dateTo: DateTime): Observable<FlussoFile[]> {
    let params = new HttpParams();
    if(nomeFlusso)
      params = params.append('nomeFlusso', nomeFlusso);
    params = params
      .append('from', dateFrom.toFormat('yyyy/MM/dd'))
      .append('to', dateTo.toFormat('yyyy/MM/dd'));
    return this.apiInvokerService.get<FlussoFile[]>
      (this.baseUrlService.getOperatoreUrl() + 'flussi/export/'+ente.mygovEnteId, {params: params}, new Mappers({mapperS2C: FlussoFile}));
  }

  uploadFlusso(ente: Ente, formData: FormData): Observable<any> {
    return this.apiInvokerService.post<any>(this.baseUrlService.getBaseUrlApi() + 'mybox/upload/'+ente.mygovEnteId, formData);
  }

  downloadFlusso(ente: Ente, type: string, filename: string, securityToken: string): any {
    let params = new HttpParams()
      .append('type', type)
      .append('filename', filename)
      .append('securityToken', securityToken);
    return this.apiInvokerService.get<any>(this.baseUrlService.getBaseUrlApi() + 'mybox/download/' + ente.mygovEnteId, {params: params,observe: 'response',  responseType: 'blob'});
  }

  searchFlussiSPC(ente: Ente, tipoFlusso: TipoFlusso, dateFrom: DateTime, dateTo: DateTime, flgProdOrDisp: string): Observable<FlussoFile[]> {
    let params = new HttpParams();
    params = params
      .append('from', dateFrom.toFormat('yyyy/MM/dd'))
      .append('to', dateTo.toFormat('yyyy/MM/dd'))
      .append('flgProdOrDisp', flgProdOrDisp);
    return this.apiInvokerService.get<FlussoFile[]>
      (this.baseUrlService.getOperatoreUrl() + 'flussi/spc/'+tipoFlusso+'/'+ente.mygovEnteId, {params: params}, new Mappers({mapperS2C: FlussoFile}));
  }

  removeFlusso(ente: Ente, mygovFlussoId: number): Observable<any> {
    return this.apiInvokerService.get<any>(this.baseUrlService.getOperatoreUrl() + 'flussi/remove/' + ente.mygovEnteId + '/' + mygovFlussoId);
  }

  insertFlussiExportConservazione(ente: Ente, dateFrom: DateTime, dateTo: DateTime, versioneTracciato: string): Observable<number> {
    let params = new HttpParams();
    params = params
      .append('from', dateFrom.toFormat('yyyy/MM/dd'))
      .append('to', dateTo.toFormat('yyyy/MM/dd'))
      .append('tipoTracciato', versioneTracciato);
    return this.apiInvokerService.get<number>(this.baseUrlService.getOperatoreUrl() + 'flussi/export/conservazione/insert/' + ente.mygovEnteId, { params: params });
  }

  searchFlussiExportConservazione(ente: Ente, nomeFlusso: string, dateFrom: DateTime, dateTo: DateTime): Observable<FlussoFile[]> {
    let params = new HttpParams();
    if (nomeFlusso)
      params = params.append('nomeFlusso', nomeFlusso);
    params = params
      .append('from', dateFrom.toFormat('yyyy/MM/dd'))
      .append('to', dateTo.toFormat('yyyy/MM/dd'));
    return this.apiInvokerService.get<FlussoFile[]>
      (this.baseUrlService.getOperatoreUrl() + 'flussi/export/conservazione/' + ente.mygovEnteId, { params: params }, new Mappers({ mapperS2C: FlussoFile }));
  }

  reloadFlusso(ente: Ente, flussoFile: FlussoFile): any {
    return this.apiInvokerService.get<FlussoFile[]>(this.baseUrlService.getOperatoreUrl() + 'flussi/conservazione/reload/' + ente.mygovEnteId + '/' + flussoFile.id);
  }

}
