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
    LocalStorageService
} from 'projects/mypay4-fe-common/src/lib/services/local-storage.service';
import {
    ApiInvokerService, BaseUrlService, ConfigurationService, WithActions
} from 'projects/mypay4-fe-common/src/public-api';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Carrello } from '../model/carrello';
import { Esito } from '../model/esito';
import { ItemCarrello } from '../model/item-carrello';
import { EnteService } from './ente.service';

@Injectable({
  providedIn: 'root'
})
export class CarrelloService {

  private content: ItemCarrello[];
  private contentObs: Subject<ItemCarrello[]>;

  constructor(
    private apiInvokerService: ApiInvokerService,
    private localStorageService: LocalStorageService,
    private conf: ConfigurationService,
    private baseUrlService: BaseUrlService,
    private enteService: EnteService,
  ) {
    if(this.conf.getProperty<boolean>('localPersistCarrello'))
      this.content = this.localStorageService.get('carrello');
    this.content = this.content || [];
    this.content.forEach(item => this.updateLogoEnte(item));
    this.content.forEach(element => WithActions.reset(element));
    this.contentObs = new BehaviorSubject<ItemCarrello[]>(this.content);
  }

  public canAdd(itemCarrello: ItemCarrello):boolean {
    return !this.checkAddError(itemCarrello);
  }

  public contains(itemCarrello: ItemCarrello):boolean {
    return itemCarrello.id && this.content.findIndex(elem => elem.id === itemCarrello.id)>=0;
  }

  public updateState(itemCarrello: ItemCarrello):void {
    if(ItemCarrello.isDebito(itemCarrello)){
      const debito = itemCarrello;
      if(this.contains(debito)) {
        debito.deStatoOriginaleCarrello = debito.deStato;
        debito.deStato = 'Inserito nel carrello';
      } else if(debito.deStatoOriginaleCarrello) {
        debito.deStato = debito.deStatoOriginaleCarrello;
        debito.deStatoOriginaleCarrello = null;
      }
    }
  }

  public updateLogoEnte(itemCarrello: ItemCarrello):void {
    if(itemCarrello && !itemCarrello.thumbEnte){
      this.enteService.getLogoEnte(itemCarrello.codIpaEnte).subscribe(logo => {
        itemCarrello.thumbEnte = logo;
        //console.log('logo of ente '+itemCarrello.codIpaEnte+': '+(logo||'').substring(0, 10));
      });
    }
  }

  public get(): Observable<ItemCarrello[]> {
    return this.contentObs;
  }

  public triggerRefresh(): void {
    this.contentObs.next(this.content);
  }


  private checkAddError(itemCarrello: ItemCarrello){
    let error:string;
    if(ItemCarrello.isDebito(itemCarrello) && (!itemCarrello?.id || itemCarrello['codStato'] !== 'INSERIMENTO_DOVUTO') )
      error = 'Elemento non valido';
    else if (this.content.length>=5)
      error = 'Non è possibile aggiungere più di 5 elementi al carello';
    else if(this.contains(itemCarrello))
      error ='Elemento già presente nel carello';

    return error;
  }

  public add(itemCarrello: ItemCarrello):string {
    return this.checkAddError(itemCarrello) || (()=>{
      itemCarrello.causaleVisualizzata = itemCarrello.causaleVisualizzata || itemCarrello.causale;
      this.updateLogoEnte(itemCarrello);
      this.content.push(itemCarrello);
      this.updateState(itemCarrello);
      this.contentObs.next(this.content);
      WithActions.reset(itemCarrello);
      if(this.conf.getProperty<boolean>('localPersistCarrello'))
        this.localStorageService.set('carrello', this.content);
      return null;
    })();
  }

  public canRemove(itemCarrello: ItemCarrello):boolean {
    return this.contains(itemCarrello);
  }

  public remove(itemCarrello: ItemCarrello):string {
    if(!itemCarrello)
      return 'Elemento non valido';
    const index = this.content.findIndex(elem => elem.id === itemCarrello.id);
    if(index < 0)
      return 'Elemento non presente nel carrello';

    this.content.splice(index, 1);
    this.updateState(itemCarrello);
    this.contentObs.next(this.content);
    WithActions.reset(itemCarrello);
    if(this.conf.getProperty<boolean>('localPersistCarrello'))
      this.localStorageService.set('carrello', this.content);
    return null;
  }

  public empty(){
    this.content.length = 0;
    this.contentObs.next(this.content);
    if(this.conf.getProperty<boolean>('localPersistCarrello'))
      this.localStorageService.set('carrello', this.content);
  }

  public size():number {
    return this.content.length;
  }

  processOrder(basket: Carrello, overrideReplicaPaymentCheck: boolean = false): Observable<Esito> {
    const targetUrl = this.baseUrlService.getCittadinoUrl() +  'carrello/checkout';
    let params = new HttpParams();
    if(overrideReplicaPaymentCheck)
      params = params.append('overrideCheckReplicaPayments','ok');
    return this.apiInvokerService.post<Esito>(targetUrl, basket, {params:params});
  }
  processOrderAnonymous(basket: Carrello, overrideReplicaPaymentCheck: boolean = false, recaptchaResponse?: any): Observable<Esito> {
    const targetUrl = this.baseUrlService.getPubCittadinoUrl() +  'carrello/checkout';
    let params = new HttpParams().append('recaptcha', recaptchaResponse);
    if(overrideReplicaPaymentCheck)
      params = params.append('overrideCheckReplicaPayments','ok');
    return this.apiInvokerService.post<Esito>(targetUrl, basket, {params:params});
  }
}
