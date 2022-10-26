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
    ApiInvokerService, BaseUrlService, Ente, LocalCacheService, TipoDovuto
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnteService {

  private entiForLogo = new Map<string, Ente>();

  constructor(
    private baseUrlService: BaseUrlService,
    private apiInvokerService: ApiInvokerService,
    private localCacheService: LocalCacheService,
  ) { }



  getAllEnti(): Observable<Ente[]> {
    return this.localCacheService.manageThumbLogoEntiCache(
      this.apiInvokerService.get<Ente[]>(this.baseUrlService.getPubCittadinoUrl() + 'enti?logoMode=hash'),
      (apiInvokerServiceRef: ApiInvokerService) => apiInvokerServiceRef.get<Ente[]>(this.baseUrlService.getPubCittadinoUrl() + 'enti?logoMode=thumb')
    );
  };

  getEnte(codIpaEnte: string): Observable<Ente> {
    return this.entiForLogo.has(codIpaEnte) ?
      of(this.entiForLogo.get(codIpaEnte)) :
      this.apiInvokerService.get<Ente>(this.baseUrlService.getPubCittadinoUrl() + 'enti/byCodIpa/' + codIpaEnte)
        .pipe(
          map(ente => {
            this.entiForLogo.set(codIpaEnte, ente);
            return ente;
          })
        );
  }

  getLogoEnte(codIpaEnte: string): Observable<string> {
    return this.getEnte(codIpaEnte).pipe(map(ente => Ente.logo(ente)));
  }

  getListTipoDovutoByEnte(ente: Ente): Observable<TipoDovuto[]> {
    return this.apiInvokerService.get<TipoDovuto[]>(this.baseUrlService.getPubCittadinoUrl() + 'enti/' + ente.mygovEnteId + '/tipiDovuto');
  }

}
