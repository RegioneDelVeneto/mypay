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

import { FlussoTasMas } from '../model/flusso-tas-mas';

@Injectable({
  providedIn: 'root'
})
export class AdminFlussiService {

  constructor(
    private apiInvokerService: ApiInvokerService,
    private baseUrlService: BaseUrlService,
  ) { }

  searchTassonomie(nomeTassonomia: string, dateFrom: DateTime, dateTo: DateTime) {
    let params = new HttpParams()
      .append('dateFrom', dateFrom.toFormat('yyyy/LL/dd'))
      .append('dateTo', dateTo.toFormat('yyyy/LL/dd'));
    if (nomeTassonomia)
      params = params.append('nomeTassonomia', nomeTassonomia)
    return this.apiInvokerService.get<FlussoTasMas[]>(this.baseUrlService.getOperatoreUrl() + 'admin/flussi/tassonomie/search', {params: params}, new Mappers({mapperS2C: FlussoTasMas}));
  }

  uploadFlusso(formData: FormData): Observable<any> {
    return this.apiInvokerService.post<any>(this.baseUrlService.getBaseUrlApi() + 'mybox/upload/0', formData);
  }

  downloadFlusso(type: string, filename: string, securityToken: string): any {
    let params = new HttpParams()
      .append('type', type)
      .append('filename', filename)
      .append('securityToken', securityToken);
    return this.apiInvokerService.get<any>(this.baseUrlService.getBaseUrlApi() + 'mybox/download/0', {params: params, observe: 'response', responseType: 'blob'});
  }
}
