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
    ApiInvokerService, BaseUrlService, Ente, LocalCacheService, TipoDovuto, UserService
} from 'projects/mypay4-fe-common/src/public-api';
import { BehaviorSubject, Observable, of, Subject, Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';

import { Injectable, OnDestroy } from '@angular/core';
import { Router, RoutesRecognized } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class EnteService implements OnDestroy{

  private currentEnte: Ente;
  private currentEnteSubject: Subject<Ente> = new BehaviorSubject(null);
  private userChangeSub: Subscription;
  private needEnte: boolean;
  private needEnteSubject: Subject<boolean> = new BehaviorSubject(false);
  private routeChangeSub: Subscription;

  private entiForLogo = new Map<number, Ente>();

  constructor(
    private apiInvokerService: ApiInvokerService,
    private userService: UserService,
    private baseUrlService: BaseUrlService,
    private router: Router,
    private localCacheService: LocalCacheService,
  ) {
    //reset current ente if user changes
    this.userChangeSub = this.userService.getLoggedUserObs().subscribe(user => {
      this.setCurrentEnte(null);
    });
    this.routeChangeSub = this.router.events
      .pipe(filter(event => event instanceof RoutesRecognized))
      .subscribe( (event:RoutesRecognized) => {
        const data = event.state.root.firstChild.data;
        let needEnte = true;
        if(data.needEnte != null)
          needEnte = data.needEnte;
        else if(data.menuItem?.length>0)
          needEnte = data.menuItem[0].needEnte; //if more menuItem, just chek the first (they are supposed to be all the same value)
        this.setNeedEnte(needEnte);
      });
  }

  ngOnDestroy():void {
    this.userChangeSub?.unsubscribe();
    this.routeChangeSub?.unsubscribe();
  }

  getCurrentEnte():Ente {
    return this.currentEnte;
  }

  setCurrentEnte(ente:Ente) {
    const changed = this.currentEnte !== ente;
    this.currentEnte = ente;
    if(changed || this.currentEnte === null){
      this.currentEnteSubject.next(this.currentEnte);
    }
  }

  getCurrentEnteObs():Observable<Ente> {
    return this.currentEnteSubject;
  }

  isNeedEnte(): boolean {
    return this.needEnte;
  }

  setNeedEnte(needEnte: boolean) {
    const changed = this.needEnte !== needEnte;
    this.needEnte = needEnte;
    if(changed){
      this.needEnteSubject.next(this.needEnte);
    }
  }

  getNeedEnteObs():Observable<boolean> {
    return this.needEnteSubject;
  }

  getAllEnti(): Observable<Ente[]> {
    return this.localCacheService.manageThumbLogoEntiCache(
      this.apiInvokerService.get<Ente[]>(this.baseUrlService.getPubOperatoreUrl() + 'enti?logoMode=hash'),
      (apiInvokerServiceRef: ApiInvokerService) => apiInvokerServiceRef.get<Ente[]>(this.baseUrlService.getPubOperatoreUrl() + 'enti?logoMode=thumb')
    );
  };

  getEntiByOperatoreUserId(): Observable<Ente[]> {
    return this.localCacheService.manageThumbLogoEntiCache(
      this.apiInvokerService.get<Ente[]>(this.baseUrlService.getOperatoreUrl() + 'enti?logoMode=hash', {skipHandleError: true}),
      (apiInvokerServiceRef: ApiInvokerService) => apiInvokerServiceRef.get<Ente[]>(this.baseUrlService.getOperatoreUrl() + 'enti?logoMode=thumb', {skipHandleError: true})
    );
  }


  getListTipoDovutoByEnte(ente: Ente): Observable<TipoDovuto[]> {
    return this.apiInvokerService.get<TipoDovuto[]>(this.baseUrlService.getBaseUrlApi() + 'enti/' + ente.mygovEnteId + '/tipiDovuto');
  }

  getListTipoDovutoByEnteAsOperatore(ente: Ente): Observable<TipoDovuto[]> {
    return this.apiInvokerService.get<TipoDovuto[]>(this.baseUrlService.getOperatoreUrl() + 'enti/' + ente.mygovEnteId + '/tipiDovutoOperatore');
  }

  getEnte(idEnte: number, thumbnail:boolean = true): Observable<Ente> {
    return this.entiForLogo.has(idEnte) ?
      of(this.entiForLogo.get(idEnte)) :
      this.apiInvokerService.get<Ente>(this.baseUrlService.getPubCittadinoUrl() + 'enti/' + idEnte)
        .pipe(
          map(ente => {
            this.entiForLogo.set(idEnte, ente);
            return ente;
          })
        );
  }

}
