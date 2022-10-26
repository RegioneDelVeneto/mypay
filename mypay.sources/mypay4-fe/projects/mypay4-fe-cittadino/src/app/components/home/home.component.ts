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

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
    faAngleRight, faFileInvoice, faHome, faInfoCircle, faTags
} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, WithTitle {

  get titleLabel(){ return "Bacheca pagamenti" }
  get titleIcon(){ return faHome }

  iconFileInvoice = faFileInvoice;
  iconTags = faTags;
  iconAngleRight = faAngleRight;
  iconInfo = faInfoCircle;

  constructor(
    private userService: UserService,
    private router: Router) { }

  ngOnInit(): void {
    if(this.userService.isLogged())
      this.router.navigate(['cards']);
  }

  openLoginForm() {
    this.userService.goToLogin();
  }
}

