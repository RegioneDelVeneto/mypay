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
import { Injectable } from '@angular/core';

import { ConfigurationService } from './configuration.service';

@Injectable({
  providedIn: 'root'
})
export class BaseUrlService {

  private baseApiUrl: string;
  private cittadinoUrl: string;
  private pubCittadinoUrl: string;
  private operatoreUrl: string;
  private pubOperatoreUrl: string;

  constructor(
    conf: ConfigurationService,
  ) {
    this.baseApiUrl = conf.getProperty('baseApiUrl');
    this.cittadinoUrl = this.baseApiUrl;
    this.pubCittadinoUrl = this.cittadinoUrl + 'public/';
    this.operatoreUrl = this.baseApiUrl +'operatore/';
    this.pubOperatoreUrl = this.baseApiUrl + 'public/';
  }

  public getBaseUrlApi(): string {
    return this.baseApiUrl;
  }

  public getPubCittadinoUrl(): string {
    return this.pubCittadinoUrl;
  }

  public getCittadinoUrl(anonymous: boolean = false): string {
    return anonymous ? this.pubCittadinoUrl : this.cittadinoUrl;
  }

  public getOperatoreUrl(): string {
    return this.operatoreUrl;
  }

  public getPubOperatoreUrl(): string {
    return this.pubOperatoreUrl;
  }
}
