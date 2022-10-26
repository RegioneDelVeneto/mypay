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
import { Subject } from 'rxjs';

import { Location } from '@angular/common';
import { Injectable } from '@angular/core';

import { Mappers } from '../mapper/mappers';
import { ApiInvokerService } from './api-invoker.service';

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {
  appPath: string;
  localStorage: Storage;

  changes$ = new Subject();

  constructor(
    private location: Location,
    private apiInvokerService: ApiInvokerService,
  ) {
    this.localStorage   = window.localStorage;
    this.appPath = this.location.prepareExternalUrl('');
    console.log('app path (base href): '+this.appPath);
  }

  get(key: string, mappers?:Mappers): any {
    if (this.isLocalStorageSupported) {
      const resp = JSON.parse(this.localStorage.getItem(this.appPath+'-'+key));
      return this.apiInvokerService.applyS2CMapper(resp, mappers);
    }

    return null;
  }

  set(key: string, value: any, mappers?:Mappers): boolean {
    if (this.isLocalStorageSupported) {
      const req = JSON.stringify(this.apiInvokerService.applyC2SMapper(_.cloneDeep(value), mappers));
      this.localStorage.setItem(this.appPath+'-'+key, req);
      this.changes$.next({
        type: 'set',
        key,
        value
      });
      return true;
    }

    return false;
  }

  remove(key: string): boolean {
    if (this.isLocalStorageSupported) {
      this.localStorage.removeItem(this.appPath+'-'+key);
      this.changes$.next({
        type: 'remove',
        key
      });
      return true;
    }

    return false;
  }

  get isLocalStorageSupported(): boolean {
    return !!this.localStorage
  }
}
