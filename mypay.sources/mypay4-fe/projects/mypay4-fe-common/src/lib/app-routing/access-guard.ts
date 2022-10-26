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
import { UserService } from 'projects/mypay4-fe-common/src/public-api';
import { Observable } from 'rxjs';

import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';

@Injectable()
export class AccessGuard implements CanActivate {

  constructor(
    private userService: UserService,
    private router: Router){ }

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean>|Promise<boolean>|boolean {
    const requiresLogin = route.data.requiresLogin || false;

    let auth = !requiresLogin || this.userService.isLogged();
    if(auth && _.isArray(route.data.menuItem))
      auth = !route.data.menuItem?.length || route.data.menuItem.some(item => !item?.roles || item.roles?.some(role => this.userService.isRoleAuthorized(role)));
    else if(auth)
      auth = !route.data.menuItem?.roles || route.data.menuItem.roles?.some(role => this.userService.isRoleAuthorized(role));

    //console.log('routing to: '+route+' - auth: '+auth);
    if (!auth) {
      this.router.navigate(['home']);
      return false;
    } else {
      return true;
    }
  }
}
