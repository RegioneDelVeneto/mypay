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
import { UserService, WithTitle } from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';

import { Component, OnDestroy, OnInit } from '@angular/core';
import {
    faAngleRight, faArchive, faClone, faHome, faRandom, faTools
} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-cards',
  templateUrl: './cards.component.html',
  styleUrls: ['./cards.component.scss']
})
export class CardsComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return "Home" }
  get titleIcon(){ return faHome }

  iconAngleRight = faAngleRight;
  iconRandom = faRandom;
  iconClone = faClone;
  iconArchive = faArchive;
  iconTools = faTools;
  isUserAdmin = false;
  private userSub: Subscription;

  constructor(
    userService:UserService,
  ) {
    this.userSub = userService.getLoggedUserObs().subscribe(() => {
      this.isUserAdmin = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE) ||
                         userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ENTE_ROLE);
    });
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
  }

}
