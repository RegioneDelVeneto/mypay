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
import {
    ApiInvokerService, BaseUrlService, Mappers
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable } from 'rxjs';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { EnteRoles } from '../model/ente-roles';
import { Operatore } from '../model/operatore';
import { RegistroOperazione } from '../model/registro-operazione';

@Injectable({
  providedIn: 'root'
})
export class AdminUtenteService {

  constructor(
    private apiInvokerService: ApiInvokerService,
    private baseUrlService: BaseUrlService,
  ) { }

  searchOperatori(username: string, cognome: string, nome: string, onlyOper: boolean): Observable<Operatore[]> {
    let params = new HttpParams()
      .append('onlyOper', ''+onlyOper);
    if(username)
      params = params.append('username', username);
    if(cognome)
      params = params.append('cognome', cognome);
    if(nome)
      params = params.append('nome', nome);

    return this.apiInvokerService.get<Operatore[]>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/search', {params:params}, new Mappers({mapperS2C: Operatore}));
  }

  searchOperatoriForEnte(mygovEnteId: number, username: string, cognome: string, nome: string): Observable<Operatore[]> {
    let params = new HttpParams();
    if(username)
      params = params.append('username', username);
    if(cognome)
      params = params.append('cognome', cognome);
    if(nome)
      params = params.append('nome', nome);

    return this.apiInvokerService.get<Operatore[]>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/search/ente/' + mygovEnteId, {params:params}, new Mappers({mapperS2C: Operatore}));
  }

  searchOperatoriForEnteTipoDovuto(enteId: number, tipoId: number, username: string, cognome: string, nome: string, flgAssociato: string): Observable<Operatore[]> {
    let params = new HttpParams();
    if(username)
      params = params.append('username', username);
    if(cognome)
      params = params.append('cognome', cognome);
    if(nome)
      params = params.append('nome', nome);
    if(flgAssociato)
      params = params.append('flgAssociato', flgAssociato);

    return this.apiInvokerService.get<Operatore[]>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/search/tipoDovuto/' + enteId + '/' + tipoId, {params:params}, new Mappers({mapperS2C: Operatore}));
  }

  detailOperatore(mygovUtenteId: number): Observable<Operatore> {
    return this.apiInvokerService.get<Operatore>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/'+mygovUtenteId, null, new Mappers({mapperS2C: Operatore}));
  }

  updateAnagraficaOperatore(operatore: Operatore) {
    const newOperatore = _.cloneDeep(operatore);
    return this.apiInvokerService.post<void>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/'+operatore.userId+'/anagrafica',newOperatore, null, new Mappers({mapperC2S: Operatore}));
  }

  insertAnagraficaOperatore(operatore: Operatore) {
    const newOperatore = _.cloneDeep(operatore);
    return this.apiInvokerService.post<number>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/anagrafica',newOperatore, null, new Mappers({mapperC2S: Operatore}));
  }

  checkUsernameExists(username: string) {
    return this.apiInvokerService.get<boolean>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/username/'+encodeURIComponent(username));
  }

  coupleEnte(operatore: Operatore, enteId: number, couplingMode:'ENTE_DOVUTI'|'ENTE'|'DOVUTI'){
    let params = new HttpParams()
      .append('couplingMode', couplingMode)
      .append('emailAddress', operatore.email);

    return this.apiInvokerService.post<EnteRoles>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/'+operatore.username+'/coupleEnte/'+enteId,null,{params:params});
  }

  decoupleEnte(operatore: Operatore, enteId: number, decoupleOnlyTipiDovuto: boolean){
    let params = new HttpParams()
      .append('decoupleOnlyTipiDovuto', ''+decoupleOnlyTipiDovuto);

    return this.apiInvokerService.delete<void>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/'+operatore.username+'/coupleEnte/'+enteId,{params:params});
  }

  coupleTipo(operatore: Operatore, tipoId: number){
    return this.apiInvokerService.post<EnteRoles>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/'+operatore.userId+'/coupleTipo/'+tipoId, null);
  }

  decoupleTipo(operatore: Operatore, tipoId: number){
    return this.apiInvokerService.delete<void>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/'+operatore.userId+'/coupleTipo/'+tipoId);
  }

  changeRuolo(operatore: Operatore, enteId: number, toAdmin: boolean) {
    const params = new HttpParams().append('toAdmin', ''+toAdmin);

    return this.apiInvokerService.post<void>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/'+operatore.userId+'/changeRuolo/'+enteId,null,{params:params});
  }

  changeEmailAddress(operatore: Operatore, enteId: number, emailAddress: string) {
    const params = new HttpParams().append('emailAddress', emailAddress);

    return this.apiInvokerService.post<void>
      (this.baseUrlService.getOperatoreUrl() + 'admin/utenti/'+operatore.userId+'/changeEmail/'+enteId,null,{params:params});
  }

  getRegistroTipoDovutoOperatore(operatore: Operatore, tipoId: number): Observable<RegistroOperazione[]> {
    return this.apiInvokerService.get<RegistroOperazione[]>(this.baseUrlService.getOperatoreUrl() + 'admin/utenti/' + operatore.userId + '/registro/' + tipoId, null, new Mappers({mapperS2C: RegistroOperazione}));
  }
}
