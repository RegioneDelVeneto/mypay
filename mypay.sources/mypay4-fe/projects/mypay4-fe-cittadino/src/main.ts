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
import { ConfigurationFactory, environment } from 'projects/mypay4-fe-common/src/public-api';

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';

ConfigurationFactory.get().init()
.then(() => {
  if (environment.production)
    enableProdMode();
  return platformBrowserDynamic()
    .bootstrapModule(AppModule);
})
.catch(bootstrapManageError);

function bootstrapManageError(error:any){
    // error should be logged into console by defaultErrorLogger, so no extra logging is necessary here
    // console.log(err);
    // show error to user
    const errorMsgElement = document.querySelector('#errorMsgElement');
    let message = 'Errore di sistema avviando l\'applicazione. Si prega di riprovare in seguito.';
    if (error) {
        if (error.message) {
            message = message + ' Causa dell\'errore: ' + error.message;
        } else {
            message = message + ' Causa dell\'errore: ' + error;
        }
    }
    errorMsgElement.textContent = message;
}
