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
import {
    MenuItem, MyPayBreadcrumbsService, UserService
} from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import {
    faBars, faEllipsisH, faSignInAlt, faSignOutAlt, faTimes
} from '@fortawesome/free-solid-svg-icons';

import { MenuService } from '../../services/menu.service';
import { SidenavService } from '../../services/sidenav.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {

  iconBars = faBars;
  itemSignOut = faSignOutAlt;
  iconHeader = faEllipsisH;
  iconSignIn = faSignInAlt;
  classHeader = '';

  onClickButtonHeader() {
    if(this.iconHeader == faEllipsisH){
      this.iconHeader = faTimes;
      this.classHeader = 'is-open';
    } else {
      this.iconHeader = faEllipsisH;
      this.classHeader = null;
    }
  }

  logged: boolean = false;
  secondLevelMenu:MenuItem[] = null;

  private loggedUserSub: Subscription;

  constructor(
    public userService: UserService,
    public menuService: MenuService,
    public myPayBreadcrumbsService: MyPayBreadcrumbsService,
    private sidenav: SidenavService,
    private router: Router) { }

  ngOnInit(): void {
    // manage the configuration and set the second level menu
    const routerSub = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(event => {
        const url = (<NavigationEnd>event).urlAfterRedirects || (<NavigationEnd>event).url;
        const firstLevelMenuItem = this.menuService.getFirstLevelMenuItemByUrl(url) || this.menuService.getDefaultFirstLevelMenuItem();
        if(firstLevelMenuItem){
          this.menuService.setSelectedMenuItemFirstLevel(firstLevelMenuItem);
          this.secondLevelMenu = this.menuService.getMenuSecondLevel(this.logged);
          routerSub.unsubscribe();
        }
      });
    this.logged = this.userService.isLogged();
    this.loggedUserSub = this.userService.getLoggedUserObs().subscribe(user => {
      this.logged = user!=null;
      this.secondLevelMenu = this.menuService.getMenuSecondLevel(this.logged);
    });
  }

  ngOnDestroy():void {
    this.loggedUserSub?.unsubscribe();
  }

  openLoginForm() {
    this.userService.goToLogin();
  }

  logout() {
    this.userService.logout();
  }

  toggleSidenav() {
    this.sidenav.toggle();
  }

  onClickFirstLevel(item: MenuItem) {
    this.menuService.onClickFirstLevel(item, this.menuService);
  }

}
