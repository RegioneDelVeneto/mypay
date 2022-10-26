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
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Breadcrumb } from '../../model/breadcrumb';
import { MyPayBreadcrumbsService } from '../../services/my-pay-breadcrumbs.service';

@Component({
  selector: 'my-pay-breadcrumbs',
  templateUrl: './my-pay-breadcrumbs.component.html',
  styleUrls: ['./my-pay-breadcrumbs.component.scss']
})
export class MyPayBreadcrumbsComponent implements OnInit {

  breadcrumbs: Breadcrumb[] = null;

  constructor(
    private myPayBreadcrumbsService: MyPayBreadcrumbsService,
    private router: Router,
  ) {
    myPayBreadcrumbsService.getBreadcrumbs().subscribe(breadcrumbs => this.breadcrumbs = breadcrumbs);
  }

  ngOnInit(): void {
  }

  onClickBreadcrumb(breadcrumb: Breadcrumb, goBackFor: number){
    if(goBackFor===0)
      return;
    if(breadcrumb.home){
      this.myPayBreadcrumbsService.resetBreadcrumbs();
      this.router.navigate(['cards']);
    } else {
      this.myPayBreadcrumbsService.goBack(goBackFor);
    }
  }
}
