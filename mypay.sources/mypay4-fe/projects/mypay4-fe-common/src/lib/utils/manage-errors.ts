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
import { ToastrService } from 'ngx-toastr';

import { AlreadyManagedError } from '../model/already-managed-error';

export function manageError(msg: string, toastrService: ToastrService, doSomething: () => void = null): (error:any) => void {
  return function(error: any){
    if(doSomething)
      doSomething();
    if(!(error instanceof AlreadyManagedError && error.ignore))
      toastrService.error(error,msg,{
        disableTimeOut: true,
        tapToDismiss: false,
        enableHtml: true,
      });
  }
}
