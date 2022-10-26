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
    ApiInvokerService, ConfigurationService, environment
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';

import { Injectable } from '@angular/core';

import { Comune, Nazione, Provincia } from '../model/location';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  private baseApiUrl: string;

  constructor(
    private apiInvokerService: ApiInvokerService,
    conf: ConfigurationService
  ) {
    this.baseApiUrl = conf.getProperty('baseApiUrl', environment);
  }

  //cache values
  private nazioni: Nazione[];
  private province: Provincia[];
  private comuni: Comune[][]=[];

  getNazioni(): Observable<Nazione[]> {
    return this.nazioni ? of(this.nazioni) :
      this.apiInvokerService.get<Nazione[]>(this.baseApiUrl + 'public/location/nazioni')
        .pipe(tap(nazioni => this.nazioni=nazioni ));
  }

  getProvince(): Observable<Provincia[]> {
    return this.province ? of(this.province) :
      this.apiInvokerService.get<Provincia[]>(this.baseApiUrl + 'public/location/province')
        .pipe(tap(province => this.province=province ));
  }

  getComuni(provincia: Provincia): Observable<Comune[]> {
    return this.comuni[provincia.provinciaId] ? of(this.comuni[provincia.provinciaId]) :
      this.apiInvokerService.get<Comune[]>(this.baseApiUrl + 'public/location/comuni/' + provincia.provinciaId)
        .pipe(tap(comuni => this.comuni[provincia.provinciaId]=comuni ));
  }
}
