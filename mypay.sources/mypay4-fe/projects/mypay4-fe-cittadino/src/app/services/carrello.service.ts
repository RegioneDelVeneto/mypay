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

import { Attualizzazione } from '../model/altri-interface';
import { Carrello } from '../model/carrello';
import { Esito } from '../model/esito';
import { ItemCarrello } from '../model/item-carrello';
import { Person } from '../model/person';
import { Spontaneo } from '../model/spontaneo';
import { EnteService } from './ente.service';

@Injectable({
  providedIn: 'root'
})
export class CarrelloService {

  private modelloUnico: boolean;
  private content: ItemCarrello[];
  private contentObs: Subject<ItemCarrello[]>;
  private _preloaded: {
    versante?:Person,
    idSession?:string,
    backUrl?:string,
    tipoCarrello?:string,
    dovutiEntiSecondari?:any
  } = {};

  constructor(
    private apiInvokerService: ApiInvokerService,
    private localStorageService: LocalStorageService,
    private conf: ConfigurationService,
    private baseUrlService: BaseUrlService,
    private enteService: EnteService,
  ) {
    if(this.conf.getProperty<boolean>('localPersistCarrello'))
      this.content = this.localStorageService.get('carrello');
    this.modelloUnico = conf.getBackendProperty<boolean>('modelloUnico', false);
    this.content = this.content || [];
    this.content.forEach(item => this.updateLogoEnte(item));
    this.content.forEach(element => WithActions.reset(element));
    this.contentObs = new BehaviorSubject<ItemCarrello[]>(this.content);
  }

  public get preloaded(){
    return this._preloaded;
  }

  public canAdd(itemCarrello: ItemCarrello):boolean {
    return !this.checkAddError(itemCarrello);
  }

  public contains(itemCarrello: ItemCarrello):boolean {
    return itemCarrello.id && this.content.findIndex(elem => elem.id === itemCarrello.id)>=0;
  }

  public containsMB(): boolean {
    return this.content.some(el => el.multibeneficiario === true);
  }

  public cantAddMB(itemCarrello: ItemCarrello):boolean {
    return this.content.length >= 1 && itemCarrello.multibeneficiario;
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

  public static isEsternoAnonimo(tipoCarrello: string): boolean {
    return ["ESTERNO_ANONIMO", "ESTERNO_ANONIMO_MULTIENTE"].includes(tipoCarrello);
  }


  private checkAddError(itemCarrello: ItemCarrello) {
    let error: string;
    if(CarrelloService.isEsternoAnonimo(this._preloaded?.tipoCarrello))
      error = 'Non è possibile aggiungere altri dovuti a questa tipologia di carrello, si prega di procedere al pagamento';
    else if(ItemCarrello.isDebito(itemCarrello) && (!itemCarrello?.id || itemCarrello['codStato'] !== 'INSERIMENTO_DOVUTO') )
      error = 'Elemento non valido';
    else if (this.content.length >= 5)
      error = 'Non è possibile aggiungere più di 5 elementi al carello.';
    else if (!this.modelloUnico && this.containsMB())
      error = 'Non è possibile aggiungere più di un elemento al carrello, è già presente un dovuto multibeneficiario.'
    else if (!this.modelloUnico && this.cantAddMB(itemCarrello))
      error = 'Non è possibile aggiungere il dovuto multibeneficiario al carrello.'
    else if (this.contains(itemCarrello))
      error = 'Elemento già presente nel carello.';
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
    this._preloaded = {};
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
  processSpontaneoExternalAppAnonymous(spontaneo: Spontaneo, extAppToken: string, overrideReplicaPaymentCheck: boolean = false, recaptchaResponse?: any): Observable<Esito> {
    const targetUrl = this.baseUrlService.getPubCittadinoUrl() +  'carrello/checkoutExtApp/' + extAppToken;
    let params = new HttpParams().append('recaptcha', recaptchaResponse);
    if(overrideReplicaPaymentCheck)
      params = params.append('overrideCheckReplicaPayments','ok');
    return this.apiInvokerService.post<Esito>(targetUrl, spontaneo, {params:params});
  }

  updateBasketPnd(codIpaEnte: string, codTipoDovuto: string, codIuv: string): Observable<Attualizzazione> {
    let params = new HttpParams();
    params = params.append('codIpaEnte', codIpaEnte);
    params = params.append('codTipoDovuto', codTipoDovuto);
    params = params.append('codIuv', codIuv);
    return this.apiInvokerService.get<Attualizzazione>(this.baseUrlService.getCittadinoUrl() + 'carrello/update', { params: params });
  }
  updateBasketPndAnonymous(codIpaEnte: string, codTipoDovuto: string, codIuv: string, recaptchaResponse?: any): Observable<Attualizzazione> {
    let params = new HttpParams();
    params = params.append('codIpaEnte', codIpaEnte);
    params = params.append('codTipoDovuto', codTipoDovuto);
    params = params.append('codIuv', codIuv);
    params = params.append('recaptcha', recaptchaResponse);
    return this.apiInvokerService.get<Attualizzazione>(this.baseUrlService.getPubCittadinoUrl() + 'carrello/update', { params: params });
  }
}
