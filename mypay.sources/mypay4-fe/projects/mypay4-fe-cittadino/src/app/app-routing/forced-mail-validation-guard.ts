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
import { Observable } from 'rxjs';

import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';

import { UserService } from '../../../../mypay4-fe-common/src/public-api';
import { MailValidationService } from '../services/mail-validation.service';

@Injectable()
export class ForcedMailValidationGuard implements CanActivate {

  constructor(
    private userService: UserService,
    private mailValidationService: MailValidationService,
    private router: Router){ }

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean>|Promise<boolean>|boolean {
    if(this.userService.isLogged() && this.userService.getLoggedUser().emailValidationNeeded){
      //force to user data page to complete email validation
      console.log('routing to utente path from: ', route.url);
      this.router.navigate(['utente']);
      this.mailValidationService.setForcedMailValidationWarning();
      return false;
    }

    return true;
  }
}
