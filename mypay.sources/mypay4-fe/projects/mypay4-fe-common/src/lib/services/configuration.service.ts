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
import { HttpBackend, HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { ApiInvokerService } from '../../public-api';
import { Mappers } from '../mapper/mappers';
import { User } from '../model/user';
import { ConfigurationFactory } from '../utils/backend-configuration-factory';

declare var myPayAppModuleName: any;

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {

  private backendConfigurationFactory: ConfigurationFactory;
  private userFromCookie: User;

  constructor(
    private httpBackend: HttpBackend
  ) {
    this.backendConfigurationFactory = ConfigurationFactory.get();
  }

  bootstrapConfig(): Promise<void> {
    const useAuthCookie = this.getBackendProperty<boolean>('useAuthCookie', false);
    if(useAuthCookie){
      return new HttpClient(this.httpBackend)
      .post<User>(this.getProperty('baseApiUrl')+'checkLoginCookie', null)
      .toPromise().then(user => {
        if(user && typeof user === 'object')
          this.userFromCookie = ApiInvokerService.mapperFunction(user, new Mappers({mapperS2C:User}), 'S2C');
        return;
      }, error => {
        console.log('error checkLoginCookie, ignoring it!', error);
      });
    } else
      return Promise.resolve();
  }

  getBackendProperty<T = string>(key: string, defaultValue?: T):T {
    return this.backendConfigurationFactory.getBackendProperty(key, defaultValue);
  }

  getProperty<T = string>(key: string, appEnvironment?:object, defaultValue?: T):T {
    return this.backendConfigurationFactory.getProperty(key, appEnvironment, defaultValue);
  }

  getUserFromCookie():User {
    return this.userFromCookie;
  }

  getMyPayAppModuleName(): string {
    return myPayAppModuleName;
  }

}
