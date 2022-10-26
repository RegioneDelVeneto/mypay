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
    ApiInvokerService, Ente as CommonEnte, LocalCacheService, Mappers,
    TipoDovuto as CommonTipoDovuto
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable } from 'rxjs';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { BaseUrlService } from '../../../../mypay4-fe-common/src/lib/services/base-url.service';
import { AnagraficaStato } from '../model/anagrafica-stato';
import { AnagraficaTipoDovuto } from '../model/anagrafica-tipo-dovuto';
import { Ente } from '../model/ente';
import { EnteFunzionalita } from '../model/ente-funzionalita';
import { RegistroOperazione } from '../model/registro-operazione';
import { TassonomiaCodDesc } from '../model/tassonomia-cod-desc';
import { LingueCodDesc } from '../model/lingue-cod-desc';

@Injectable({
  providedIn: 'root'
})
export class AdminEntiService {

  constructor(
    private apiInvokerService: ApiInvokerService,
    private baseUrlService: BaseUrlService,
    private localCacheService: LocalCacheService,
  ) { }

  getLingueAggiuntive(): Observable<LingueCodDesc[]> {
    return this.apiInvokerService.get<LingueCodDesc[]>(this.baseUrlService.getOperatoreUrl() + 'admin/enti/lingueAggiuntive');
  }

  getTipiEnte(): Observable<TassonomiaCodDesc[]> {
    return this.apiInvokerService.get<TassonomiaCodDesc[]>(this.baseUrlService.getOperatoreUrl() + 'admin/enti/tipiEnte');
  }

  getCodTassonomico(tipoEnte: string, macroArea: string, tipoServizio: string, motivoRisc: string): Observable<TassonomiaCodDesc[]> {
    let params = new HttpParams()
        .append('tipoEnte', tipoEnte)
        .append('macroArea', macroArea)
        .append('tipoServizio', tipoServizio)
        .append('motivoRisc', motivoRisc);
    return this.apiInvokerService.get<TassonomiaCodDesc[]>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/codTassonomico', {params: params});
  }

  getAnagraficaStati(): Observable<AnagraficaStato[]> {
    return this.apiInvokerService.get<AnagraficaStato[]>(this.baseUrlService.getOperatoreUrl() + 'admin/enti/anagraficaStati');
  }

  searchEnti(codIpaEnte: string, deNome: string, codFiscale: string, idStato: number, dtAvvioFrom: string, dtAvvioTo: string): Observable<Ente[]> {
    let params = new HttpParams();
    if (codIpaEnte)
      params = params.append('codIpaEnte', codIpaEnte);
    if (deNome)
      params = params.append('deNome', deNome);
    if (codFiscale)
      params = params.append('codFiscale', codFiscale);
    if (idStato)
      params = params.append('idStato', idStato?.toString());
    if (dtAvvioFrom)
      params = params.append('dtAvvioFrom', dtAvvioFrom);
    if (dtAvvioTo)
      params = params.append('dtAvvioTo', dtAvvioTo);
    return this.localCacheService.manageThumbLogoEntiCache(
      this.apiInvokerService.get<Ente[]>(this.baseUrlService.getOperatoreUrl() + 'admin/enti', {params: params.append('logoMode','hash')}),
      (apiInvokerServiceRef: ApiInvokerService) => apiInvokerServiceRef.get<Ente[]>(this.baseUrlService.getOperatoreUrl() + 'admin/enti', {params: params.append('logoMode','thumb')})
    );
  }

  getEnteById(mygovEnteId: number): Observable<Ente> {
    return this.apiInvokerService.get<Ente>(this.baseUrlService.getOperatoreUrl() + 'admin/enti/' + mygovEnteId, null, new Mappers({mapperS2C: Ente}));
  }

  upsertEnte(mode: string, ente: Ente): Observable<Ente> {
    let mygovEnteId = ente.mygovEnteId;
    let targetUrl = mode === 'edit' ?
        this.baseUrlService.getOperatoreUrl()+'admin/enti/' + mygovEnteId + '/update' : this.baseUrlService.getOperatoreUrl()+'admin/enti/ente/insert';
    return this.apiInvokerService.post<Ente>(targetUrl, ente, null, new Mappers({mapper: Ente}));
  }

  updateLogo(mygovEnteId: number, formData: FormData) {
    return this.apiInvokerService.post<CommonEnte>(this.baseUrlService.getOperatoreUrl() + 'admin/enti/'+ mygovEnteId + '/saveLogo', formData);
  }

  searchEnteFunzionalita(mygovEnteId: number): Observable<EnteFunzionalita[]> {
    return this.apiInvokerService.get<EnteFunzionalita[]>(this.baseUrlService.getOperatoreUrl() + 'admin/enti/funzionalita/' + mygovEnteId, null, new Mappers({mapperS2C: EnteFunzionalita}));
  }

  getRegistroFunzionalita(mygovEnteId: number, funzionalita: string): Observable<RegistroOperazione[]> {
    return this.apiInvokerService.get<RegistroOperazione[]>(this.baseUrlService.getOperatoreUrl() + 'admin/enti/funzionalita/' + mygovEnteId + '/' + funzionalita, null, new Mappers({mapperS2C: RegistroOperazione}));
  }

  activateEnteFunzionalita(mygovEnteFunzionalitaId: number): Observable<void> {
    return this.apiInvokerService.get<void>(this.baseUrlService.getOperatoreUrl() + 'admin/enti/funzionalita/activate/' + mygovEnteFunzionalitaId);
  }

  deactivateEnteFunzionalita(mygovEnteFunzionalitaId: number): Observable<void> {
    return this.apiInvokerService.get<void>(this.baseUrlService.getOperatoreUrl() + 'admin/enti/funzionalita/deactivate/' + mygovEnteFunzionalitaId);
  }

  getMacroArea(tipoEnte: string): Observable<TassonomiaCodDesc[]> {
    let params = new HttpParams()
        .append('tipoEnte', tipoEnte);
    return this.apiInvokerService.get<TassonomiaCodDesc[]>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/macroArea', {params: params});
  }

  getTipoServizio(tipoEnte: string, macroArea: string): Observable<TassonomiaCodDesc[]> {
    let params = new HttpParams()
        .append('tipoEnte', tipoEnte)
        .append('macroArea', macroArea);
    return this.apiInvokerService.get<TassonomiaCodDesc[]>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/tipoServizio', {params: params});
  }

  getMotivoRiscossione(tipoEnte: string, macroArea: string, tipoServizio: string): Observable<TassonomiaCodDesc[]> {
    let params = new HttpParams()
        .append('tipoEnte', tipoEnte)
        .append('macroArea', macroArea)
        .append('tipoServizio', tipoServizio);
    return this.apiInvokerService.get<TassonomiaCodDesc[]>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/motivoRiscossione', {params: params});
  }

  searchTipiDovuto(codTipo: string, deTipo: string, flgAttivo: string): Observable<CommonTipoDovuto[]> {
    let params = new HttpParams();
    if (codTipo)
      params = params.append('codTipo', codTipo);
    if (deTipo)
      params = params.append('deTipo', deTipo);
    if (flgAttivo)
      params = params.append('flgAttivo', flgAttivo);
    return this.apiInvokerService.get<CommonTipoDovuto[]>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/search', { params: params }, new Mappers({mapperS2C: CommonTipoDovuto}));
  }

  searchTipiDovutoByTipo(codTipo: string, codIpaEnte: string, deNomeEnte: string, flgAttivo): Observable<CommonTipoDovuto[]> {
    let params = new HttpParams().append('codTipo', codTipo);
    if (codIpaEnte)
      params = params.append('codIpaEnte', codIpaEnte);
    if (deNomeEnte)
      params = params.append('deNomeEnte', deNomeEnte);
    if (flgAttivo)
      params = params.append('flgAttivo', flgAttivo);
    return this.apiInvokerService.get<CommonTipoDovuto[]>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/search/tipo/' + codTipo, { params: params }, new Mappers({mapperS2C: CommonTipoDovuto}));
  }

  searchTipiDovutoByEnte(mygovEnteId: number, codTipo: string, deTipo: string, flgAttivo: string): Observable<CommonTipoDovuto[]> {
    let params = new HttpParams();
    params = params.append('withActivationInfo', 'true');
    if (codTipo)
      params = params.append('codTipo', codTipo);
    if (deTipo)
      params = params.append('deTipo', deTipo);
    if (flgAttivo)
      params = params.append('flgAttivo', flgAttivo);
    return this.apiInvokerService.get<CommonTipoDovuto[]>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/search/' + mygovEnteId, { params: params }, new Mappers({mapperS2C: CommonTipoDovuto}));
  }

  getTipoDovutoByEnteAndCod(mygovEnteId: number, codTipo: string): Observable<CommonTipoDovuto> {
    return this.apiInvokerService.get<CommonTipoDovuto>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/search/' + mygovEnteId + '/cod/' + codTipo, null, new Mappers({mapperS2C: CommonTipoDovuto}));
  }

  getRegistroTipoDovuto(mygovEnteTipoDovutoId: number): Observable<RegistroOperazione[]> {
    return this.apiInvokerService.get<RegistroOperazione[]>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/' + mygovEnteTipoDovutoId + '/registro', null, new Mappers({mapperS2C: RegistroOperazione}));
  }

  getTipoDovutoById(mygovEnteTipoDovutoId: number): Observable<AnagraficaTipoDovuto> {
    return this.apiInvokerService.get<AnagraficaTipoDovuto>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/' + mygovEnteTipoDovutoId);
  }

  activateTipoDovuto(mygovEnteTipoDovutoId: number): Observable<void> {
    return this.apiInvokerService.get<void>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/activate/' + mygovEnteTipoDovutoId);
  }

  deactivateTipoDovuto(mygovEnteTipoDovutoId: number): Observable<void> {
    return this.apiInvokerService.get<void>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/deactivate/' + mygovEnteTipoDovutoId);
  }

  deleteTipoDovuto(mygovEnteTipoDovutoId: number): Observable<void> {
    return this.apiInvokerService.delete<void>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/delete/' + mygovEnteTipoDovutoId);
  }

  upsertTipoDovuto(mode: string, anagTipoDovuto: AnagraficaTipoDovuto): Observable<AnagraficaTipoDovuto> {
    let targetUrl = mode === 'edit' ?
        this.baseUrlService.getOperatoreUrl()+'admin/tipiDovuto/update' : this.baseUrlService.getOperatoreUrl()+'admin/tipiDovuto/insert';
    return this.apiInvokerService.post<AnagraficaTipoDovuto>(targetUrl, anagTipoDovuto);
  }

  hasDovutoNoScadenza(mygovEnteId: number, codTipo: string){
    return this.apiInvokerService.get<boolean>(this.baseUrlService.getOperatoreUrl() + 'admin/tipiDovuto/hasDovutoNoScadenza/' + mygovEnteId + '/cod/' + codTipo);
  }
}
