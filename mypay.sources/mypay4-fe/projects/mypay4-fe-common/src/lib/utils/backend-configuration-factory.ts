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


import { environment } from '../environments/environment';
import { versionInfo } from '../environments/version';

export class ConfigurationFactory {

  private static instance: ConfigurationFactory;

  private initDone: boolean = false;
  private initPromise: Promise<void>;
  private externalizedConfiguration: any;
  private backendConfiguration: any;

  static get(){
    if(!ConfigurationFactory.instance)
      ConfigurationFactory.instance = new ConfigurationFactory();
    return ConfigurationFactory.instance;
  }

  private handleFetchErrors(response) {
    if (!response.ok) {
      throw Error(response.statusText);
    }
    return response;
  }

  init(): Promise<void>{
    if(!this.initPromise)
      this.initPromise = fetch('assets/conf/environment.json')
        .then(this.handleFetchErrors)
        .then(response => response.json())
        .then(json => this.externalizedConfiguration = json)
        .then(() => console.log('externalizedConfiguration', this.externalizedConfiguration))
        .catch(error => console.log('error reading environment.json', error))
        .then(() => fetch(this.getPropertyImpl('baseApiUrl')+'public/info/config', {method:'POST'}))
        .then(this.handleFetchErrors)
        .then(response => response.json())
        .then(json => this.backendConfiguration = json)
        .then(() => {
          const versionBE = this.backendConfiguration?.['gitHash']?.substring(0,8);
          const versionFE = versionInfo.gitHash?.substring(0,8);
          const urlSearchParams = new URLSearchParams(document.location.search);
          const versionUrl = urlSearchParams.get("mypayVersion");
          let skipReload = versionUrl==versionBE || versionBE==versionFE || versionFE==='?';
          console.log('versionFE['+versionFE+'] versionBE['+versionBE+'] versionUrl['+versionUrl+']');
          if(!skipReload){
            if(urlSearchParams.has('mypayVersion'))
              urlSearchParams.delete('mypayVersion');
            urlSearchParams.append('mypayVersion', versionBE);
            console.log('try reload FE becuse possibly outdated, new searchString['+urlSearchParams.toString()+']');
            document.location.search = urlSearchParams.toString();
          }
        })
        .then(() => console.log('backendConfiguration', this.backendConfiguration))
        .catch(error => {console.error(error); throw new Error('errore comunicando con il server')})
        .then(() => {this.initDone=true});
    return this.initPromise;
  }

  getProperty<T = string>(key: string, appEnvironment?:object, defaultValue?: T):T {
    if(!this.initDone)
      throw new Error('init phase not completed');

    return this.getPropertyImpl(key, appEnvironment, defaultValue);
  }

  private getPropertyImpl<T = string>(key: string, appEnvironment?:object, defaultValue?: T):T {
    if(this.externalizedConfiguration?.hasOwnProperty(key))
      return this.externalizedConfiguration[key];

    if(appEnvironment?.hasOwnProperty(key))
      return appEnvironment[key];

    if(environment.hasOwnProperty(key))
      return environment[key];

    return defaultValue;
  }

  getBackendProperty<T = string>(key: string, defaultValue?: T):T {
    if(!this.initDone)
      throw new Error('init phase not completed');

    if(this.backendConfiguration?.hasOwnProperty(key))
      return this.backendConfiguration[key];
    return defaultValue;
  }
}
