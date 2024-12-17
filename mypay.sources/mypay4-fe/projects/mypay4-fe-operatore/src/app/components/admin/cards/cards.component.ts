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
import { Subscription } from 'rxjs/internal/Subscription';

import { Component, OnDestroy } from '@angular/core';
import {
  faAngleRight, faBook, faClone, faExpandArrowsAlt, faListAlt, faTools, faUniversity, faUsers
} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-cards',
  templateUrl: './cards.component.html',
  styleUrls: ['./cards.component.scss']
})
export class CardsComponent implements OnDestroy, WithTitle {

  get titleLabel(){ return "Back-office" }
  get titleIcon(){ return faTools }

  iconUniversity = faUniversity;
  iconUsers = faUsers;
  iconClone = faClone;
  iconListAlt = faListAlt;
  iconExpandArrowsAlt = faExpandArrowsAlt;
  iconAngleRight = faAngleRight;
  iconBook = faBook;

  private userSub: Subscription;
  isUserAdmin: boolean = false;
  isUserAdminEnte: boolean = false;

  constructor(
    userService:UserService
  ) {
    this.userSub = userService.getLoggedUserObs().subscribe(() => {
      this.isUserAdmin = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE);
      this.isUserAdminEnte = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ENTE_ROLE);
    });
  }

  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
  }
}
