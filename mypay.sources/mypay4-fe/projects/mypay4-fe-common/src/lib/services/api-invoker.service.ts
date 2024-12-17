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
import { ToastrService } from 'ngx-toastr';
import { EMPTY, Observable } from 'rxjs';
import { catchError, filter, first, map } from 'rxjs/operators';

import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NavigationStart, Router } from '@angular/router';

import { mapper } from '../mapper/mapper';
import { Mappers } from '../mapper/mappers';
import { ContentDispositionUtil } from '../utils/content-disposition';
import { ProcessHTTPMsgService } from './process-httpmsg.service';

@Injectable({
  providedIn: 'root'
})
export class ApiInvokerService {

  private lastCountThresholdResponse: any;
  private lastCountThresholdCount: number;

  private lastNavigationTime: number = Date.now();

  constructor(
    private httpClient: HttpClient,
    private processHTTPMsgService: ProcessHTTPMsgService,
    private toastrService: ToastrService,
    private router: Router,
  ) {
    this.router.events.pipe(
      filter( event => event instanceof NavigationStart)
    ).subscribe( (event:NavigationStart) => {
      this.lastNavigationTime = Date.now();
      console.log('['+this.lastNavigationTime+'] navigate to url: ['+event.url+']');
    });
  }

  private errorHandler(skipHandleError: boolean) {
    const startTime = Date.now();
    return (error:HttpErrorResponse) => {
      if(skipHandleError){
        return this.processHTTPMsgService.skipHandleError(error);
      } else {
        const errorTime = Date.now();
        console.log('handling error with errorTime: '+errorTime+' - startTime: '+startTime+' - lastNavigationTime: '+this.lastNavigationTime+' - elapsed: '+(errorTime-startTime)+' - deltaLastNavig: '+(startTime - this.lastNavigationTime));
        const skipErrorByNavigation = (startTime - this.lastNavigationTime) < 0;
        if(skipErrorByNavigation){
          //just log the error
          console.error('http error, ignored because of skipErrorByNavigation', error);
          return EMPTY;
        } else {
          return this.processHTTPMsgService.handleError(error);
        }
      }
    }
  }

  get<T>(targetUrl: string, options?:Object, mappers?:Mappers):Observable<T> {
    const finalMapper = mappers?.responseMapper || (resp => resp);
    return this.httpClient.get(targetUrl, options || {} )
    .pipe(
      first(),
      map( resp => ApiInvokerService.mapperFunction(finalMapper(this.mapPartialResultList(resp)), mappers, 'S2C') ),
      catchError(this.errorHandler(options?.['skipHandleError']))
    );
  }

  post<T>(targetUrl: string, body: any, options?:Object, mappers?:Mappers):Observable<T> {
    const finalMapper = mappers?.responseMapper || (resp => resp);
    return this.httpClient.post(targetUrl, ApiInvokerService.mapperFunction(body, mappers, 'C2S'), options || {} )
    .pipe(
      first(),
      map( resp => ApiInvokerService.mapperFunction(finalMapper(this.mapPartialResultList(resp)), mappers, 'S2C') ),
      catchError(this.errorHandler(options?.['skipHandleError']))
    );
  }

  put<T>(targetUrl: string, body: any, options?:Object, mappers?:Mappers):Observable<T> {
    const finalMapper = mappers?.responseMapper || (resp => resp);
    return this.httpClient.put(targetUrl, ApiInvokerService.mapperFunction(body, mappers, 'C2S'), options || {} )
    .pipe(
      first(),
      map( resp => ApiInvokerService.mapperFunction(finalMapper(this.mapPartialResultList(resp)), mappers, 'S2C') ),
      catchError(this.errorHandler(options?.['skipHandleError']))
    );
  }

  delete<T>(targetUrl: string, options?:Object, mappers?:Mappers):Observable<T> {
    const finalMapper = mappers?.responseMapper || (resp => resp);
    return this.httpClient.delete(targetUrl, options || {} )
    .pipe(
      first(),
      map( resp => ApiInvokerService.mapperFunction(finalMapper(this.mapPartialResultList(resp)), mappers, 'S2C') ),
      catchError(this.errorHandler(options?.['skipHandleError']))
    );
  }

  getTotalCount(list: any): number {
    if(_.isEqual(this.lastCountThresholdResponse, list))
      return this.lastCountThresholdCount;
  }

  applyC2SMapper<T>(req: Object, mappers?:Mappers):T {
    return ApiInvokerService.mapperFunction(req, mappers, 'C2S');
  }

  applyS2CMapper<T>(resp: Object, mappers?:Mappers):T {
    const finalMapper = mappers?.responseMapper || (resp => resp);
    return ApiInvokerService.mapperFunction(finalMapper(this.mapPartialResultList(resp)), mappers, 'S2C');
  }

  static mapperFunction(objToMap: any, mappers: Mappers, type: string): any {
    const mapperDef = mappers?.['mapper'+type] || mappers?.mapper;
    const mapperList = mapperDef?.['MAPPER_'+type+'_DEF'] ?? mapperDef?.['MAPPER_DEF'];
    if(mapperList){
      if(Array.isArray(objToMap)){
        return objToMap.map( item => mapper(item, mapperList) );
      } else {
        return mapper(objToMap, mapperList);
      }
    } else {
      return objToMap;
    }
  }

  private mapPartialResultList(response: any): any {
    if(response && typeof response.count!=='undefined' && typeof response?.limit!=='undefined' && typeof response?.list!=='undefined'){
      if(response.count > response.limit){
        this.lastCountThresholdResponse = response.list;
        this.lastCountThresholdCount = response.count;
        this.toastrService.warning('Affinare la ricerca con filtri più selettivi.',
        `Sono mostrati ${response.limit} risultati su ${response.count} risultati trovati.`, {
          timeOut: 5000,
          closeButton:true});
      }
      return response.list;
    } else {
      return response;
    }
  }

  static extractFilenameFromContentDisposition(contentDispositionHeader: string): string {
    if(!contentDispositionHeader)
      return contentDispositionHeader;
    const contentDisposition = ContentDispositionUtil.parse(contentDispositionHeader);
    return contentDisposition?.parameters?.['filename'];
  }

}
