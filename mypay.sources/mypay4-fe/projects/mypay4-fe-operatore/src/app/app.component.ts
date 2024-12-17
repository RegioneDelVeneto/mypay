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
    MenuItem, OverlaySpinnerRef, OverlaySpinnerService, UserService
} from 'projects/mypay4-fe-common/src/public-api';
import { combineLatest, Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { NavigationEnd, Router } from '@angular/router';
import {
    faBars, faChevronRight, faSignOutAlt, faTimes, faUser
} from '@fortawesome/free-solid-svg-icons';

import { EnteOverlayComponent } from './components/ente-overlay/ente-overlay.component';
import { EnteService } from './services/ente.service';
import { MenuService } from './services/menu.service';
import { SidenavService } from './services/sidenav.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements AfterViewInit, OnDestroy {
  title = 'mypay4-fe-operatore';
  @ViewChild('sidenav') public sidenav: MatSidenav;
  @ViewChild('mainContainerDiv', { read: ElementRef }) mainContainerDiv: ElementRef;

  iconUser = faUser;
  iconTimes = faTimes;
  iconSignOut = faSignOutAlt;
  iconBars = faBars;
  iconChevronRight = faChevronRight;

  overlayNeedEnte = false;

  private overlayMainContainer: OverlaySpinnerRef;
  private enteChangeSub: Subscription;

  constructor(
    private sidenavService: SidenavService,
    private overlaySpinnerService: OverlaySpinnerService,
    public menuService: MenuService,
    public userService: UserService,
    private enteService: EnteService,
    private router: Router,
  ) {

    router.events.pipe(
      filter( event => event instanceof NavigationEnd)
    ).subscribe( event => {
      window.scrollTo({top: 0, behavior: 'smooth'});
      //this.mainContentContainer.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'start' });
    });

  }

  ngAfterViewInit(): void {
    //subscribe to ente change events
    this.enteChangeSub = combineLatest([this.enteService.getCurrentEnteObs(),this.enteService.getNeedEnteObs()])
      .subscribe(([ente,needEnte]) => {
        const canWork = !this.userService.isLogged() || ente || !needEnte;
        if(canWork && this.overlayMainContainer) {
          this.overlaySpinnerService.detach(this.overlayMainContainer);
          this.overlayMainContainer = null;
          this.overlayNeedEnte = false;
        } else if(!canWork && !this.overlayMainContainer){
          this.overlayMainContainer = this.overlaySpinnerService.showProgress(this.mainContainerDiv, EnteOverlayComponent);
          this.overlayNeedEnte = true;
        }
      });

    this.sidenavService.setSidenav(this.sidenav);
    //sidenav open after logged
    // if(this.userService.isLogged())
    //   setTimeout(() => {
    //     this.sidenavService.open();
    //   }, 250);
  }

  ngOnDestroy(): void {
    this.enteChangeSub?.unsubscribe();
  }

  onClickMenu(item: MenuItem) {
    if(item.submenu?.length > 0){
      const newOpenedStatus = !item.opened;
      if(newOpenedStatus){
        this.menuService.getApplicationMenu()
          .filter(anItem => anItem.opened && anItem !== item)
          .forEach(anItem => setTimeout(()=> anItem.opened = false, 0) );
        this.menuService.onClickMenu(item, this.menuService);
      }
      setTimeout(()=> item.opened = newOpenedStatus, 0);
    }else{
      this.closeSidenav();
      this.menuService.onClickMenu(item, this.menuService);
    }
  }

  closeSidenav(){
    this.sidenavService.close();
  }

  openLoginForm() {
    this.sidenavService.close();
    this.userService.goToLogin();
  }

  logout() {
    this.sidenavService.close();
    this.userService.logout();
  }

  mainMenuOpen: boolean = false;
  toogleMainMenu() {
    this.mainMenuOpen = !this.mainMenuOpen;
  }

}
