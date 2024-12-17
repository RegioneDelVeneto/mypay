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
import { from, Observable, of } from 'rxjs';
import { concatMap, map, mergeMap } from 'rxjs/operators';

import { Injectable } from '@angular/core';

import { Ente } from '../model/ente';
import { ApiInvokerService } from './api-invoker.service';
import { DexieService } from './dexie.service';

@Injectable({
  providedIn: 'root'
})
export class LocalCacheService {

  private localDbWorking = false;

  constructor(
    private dbService: DexieService,
    private apiInvokerService: ApiInvokerService,
  ) {
    dbService.table(DexieService.THUMB_LOGO_TABLE).get('test').then(_value => {
      this.localDbWorking = true;
      console.log('localDB working: '+this.localDbWorking);
    }).catch(error => {
      console.log('localDB working: '+this.localDbWorking+' ['+error+']');
    });
  }

  public manageThumbLogoEntiCache<T extends Ente>(entiWithHashObs: Observable<T[]>, entiWithThumbFun: (apiInvokerServiceRef: ApiInvokerService) => Observable<T[]>){
    return entiWithHashObs.pipe(
      mergeMap( enti => {
        const entiWithLogo = enti.filter(ente=>ente.hashThumbLogoEnte);
        if(!this.localDbWorking || entiWithLogo.length === 0){
          return of(enti);
        }
        return from(this.dbService.table(DexieService.THUMB_LOGO_TABLE).bulkGet(entiWithLogo.map(ente => ente.hashThumbLogoEnte)))
          .pipe(
            map( (result: any[]) => {
              const resultFound = result.filter(x => x !== undefined);
              if(resultFound.length === entiWithLogo.length){
                _.zipWith(entiWithLogo, resultFound, (ente: Ente, logo: string) => {
                    ente.thumbLogoEnte = logo;
                    return ente;
                });
                return enti;
              } else
                return null;
            })
          )
      }),
      concatMap( enti => {
          if(enti === null) {
            //console.log('loading allEnti with Thumb')
            return entiWithThumbFun(this.apiInvokerService)
              .pipe( map( newEnti => {
              const entiWithLogo = newEnti.filter(ente=>ente.hashThumbLogoEnte);
              if(this.localDbWorking && entiWithLogo.length > 0){
                from(this.dbService.table(DexieService.THUMB_LOGO_TABLE).bulkPut(
                  entiWithLogo.map(ente => ente.thumbLogoEnte),
                  entiWithLogo.map(ente => ente.hashThumbLogoEnte)))
                  .subscribe(() => console.log('added to logo cache: ',entiWithLogo.length),error => console.log('error bulkPut', error));
              }
              return newEnti;
            }));
          } else
            return of(enti);
      }),
    );
  }
}
