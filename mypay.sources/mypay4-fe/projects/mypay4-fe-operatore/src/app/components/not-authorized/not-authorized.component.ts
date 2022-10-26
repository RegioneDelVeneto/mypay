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
import { UserService } from 'projects/mypay4-fe-common/src/public-api';

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-not-authorized',
  templateUrl: './not-authorized.component.html',
  styleUrls: ['./not-authorized.component.scss']
})
export class NotAuthorizedComponent implements OnInit {

  username: String;
  fullName: String;

  errorUID: String;
  detailMsg: String;
  emailValidationNeeded: boolean;

  constructor(
    private userService: UserService,
    private router: Router,
  ) {
    this.detailMsg = this.router.getCurrentNavigation()?.extras?.state?.msg;
    this.errorUID = this.router.getCurrentNavigation()?.extras?.state?.errorUID;
    console.log('errorUID: '+this.errorUID);

    this.emailValidationNeeded = this.router.getCurrentNavigation()?.extras?.state?.emailValidationNeeded || false;
  }

  ngOnInit(): void {
    this.username = this.userService.getLoggedUser()?.username;
    this.fullName = this.userService.getLoggedUser()?.nome + ' '+this.userService.getLoggedUser()?.cognome;

    setTimeout(()=>this.userService.logout(false), 1000);
  }

  openLoginForm() {
    this.userService.goToLogin();
  }

}
