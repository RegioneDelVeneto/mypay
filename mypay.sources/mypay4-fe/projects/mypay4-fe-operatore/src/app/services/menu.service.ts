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

import { Injectable, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { faClipboardCheck, faStar, faTachometerAlt } from '@fortawesome/free-solid-svg-icons';

import { routes } from '../app-routing/app-routing.module';
import { CardsComponent as AdminCardsComponent } from '../components/admin/cards/cards.component';
import { EnteListComponent } from '../components/admin/enti/ente-list/ente-list.component';
import { GiornaleComponent } from '../components/admin/giornale/giornale.component';
import { TaxonomyComponent } from '../components/admin/taxonomy/taxonomy.component';
import { TipoListComponent } from '../components/admin/tipi-dovuto/tipo-list/tipo-list.component';
import { UtentiComponent } from '../components/admin/utenti/utenti.component';
import { CardsComponent } from '../components/cards/cards.component';
import { DovutiComponent } from '../components/dovuti/dovuti.component';
import { FlussiCardsComponent } from '../components/flussi-cards/flussi-cards.component';
import { FlussiExportComponent } from '../components/flussi-export/flussi-export.component';
import { FlussiImportComponent } from '../components/flussi-import/flussi-import.component';
import { FlussiSpcComponent } from '../components/flussi-spc/flussi-spc.component';
import { EnteService } from './ente.service';
import { FlussiConservazioneComponent } from '../components/flussi-conservazione/flussi-conservazione.component';

@Injectable({
  providedIn: 'root'
})
export class MenuService implements OnDestroy {

  private menuItemsMap:{[key:number]:MenuItem} = {};
  private fullMenuItemsMap:{[key:number]:MenuItem} = {};
  private menuItemsExternal: MenuItem[];

  private menuItemsApp: MenuItem[];

  private userSub: Subscription;

  constructor(
    private router: Router,
    private userService: UserService,
    private myPayBreadcrumbsService: MyPayBreadcrumbsService,
    private enteService: EnteService,
  ) {
    this.menuItemsExternal = [
      new MenuItem(10, '#', 'Link esterno 1', {external:true, icon:faTachometerAlt}),
      new MenuItem(11, '#', 'Link esterno 2', {external:true, icon:faClipboardCheck}),
      new MenuItem(12, '#', 'Link esterno 3', {external:true, icon:faStar}),
    ];

    const subMenuFlussi = [
      new MenuItem(31,'/flussi-import', FlussiImportComponent.prototype, {}),
      new MenuItem(32,'/flussi-export', FlussiExportComponent.prototype, {}),
      new MenuItem(33,'/flussi-spc/rendicontazione', FlussiSpcComponent.prototype, {}),
      new MenuItem(34,'/flussi-spc/quadratura', 'Flussi di quadratura', {icon: FlussiSpcComponent.prototype.titleIcon}),
      new MenuItem(35,'/flussi-conservazione', FlussiConservazioneComponent.prototype, {}),
    ];

    const subMenuAdmin = [
      new MenuItem(41,'/admin/enti',EnteListComponent.prototype, {roles:[UserService.BACK_OFFICE_ADMIN_ROLE, UserService.BACK_OFFICE_ADMIN_ENTE_ROLE], needEnte:false}),
      new MenuItem(42,'/admin/utenti',UtentiComponent.prototype, {roles:[UserService.BACK_OFFICE_ADMIN_ROLE], needEnte:false}),
      new MenuItem(43,'/admin/tipiDovuto',TipoListComponent.prototype, {roles:[UserService.BACK_OFFICE_ADMIN_ROLE], needEnte:false}),
      //new MenuItem(44,'/admin/tassonomie',TassonomieComponent.prototype, {roles:[UserService.BACK_OFFICE_ADMIN_ROLE], needEnte:false}),
      new MenuItem(44,'/admin/taxonomy',TaxonomyComponent.prototype, {roles:[UserService.BACK_OFFICE_ADMIN_ROLE], needEnte:false}),
      //new MenuItem(45,'/admin/massive','Gestione massiva', {icon:faExpandArrowsAlt, roles:[UserService.BACK_OFFICE_ADMIN_ROLE], needEnte:false}),
      new MenuItem(46,'/admin/giornale/pa','Giornale degli eventi - PA', {icon: GiornaleComponent.prototype.titleIcon, roles:[UserService.BACK_OFFICE_ADMIN_ROLE], needEnte:false}),
      new MenuItem(47,'/admin/giornale/fesp','Giornale degli eventi - FESP', {icon: GiornaleComponent.prototype.titleIcon, roles:[UserService.BACK_OFFICE_ADMIN_ROLE], needEnte:false}),
    ]

    this.menuItemsApp = [
      new MenuItem(20,'/cards', CardsComponent.prototype, {needEnte:false}),
      new MenuItem(21,'/flussi', FlussiCardsComponent.prototype, {submenu:subMenuFlussi, needEnte:false}),
      new MenuItem(22,'/dovuti', DovutiComponent.prototype, {}),
      //new MenuItem(23,'/revoche', 'Gestione revoche', false, false, true, faArchive),
      new MenuItem(40,'/admin', AdminCardsComponent.prototype, {roles:[UserService.BACK_OFFICE_ADMIN_ROLE, UserService.BACK_OFFICE_ADMIN_ENTE_ROLE], submenu:subMenuAdmin, needEnte:false}),
    ];

    this.userSub = this.userService.getLoggedUserObs().subscribe(user => {
      if(!user)
        return;
      // if(userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE)) {
      //   this.menuItemsApp.push(new MenuItem(40,'/admin', AdminCardsComponent.prototype, {roles:[UserService.BACK_OFFICE_ADMIN_ROLE], submenu:subMenuAdmin, needEnte:false}));
      // } else if(userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ENTE_ROLE)) {
      //   this.menuItemsApp.push(new MenuItem(41,'/admin/enti',EnteListComponent.prototype, {roles:[UserService.BACK_OFFICE_ADMIN_ROLE, UserService.BACK_OFFICE_ADMIN_ENTE_ROLE], needEnte:false}));
      // }
      this.initFullMenuItemMap(this.menuItemsApp);
      this.setRoleAuth(this.menuItemsApp);
      this.myPayBreadcrumbsService.setMenuItemMap(this.menuItemsMap);

      //when defining the routes, just set the corresponding menu item id
      // here retrieve the full MenuItem object
      routes
        .filter(route => route.data?.menuItemId)
        .forEach(route => route.data.menuItem = this.getMenuItem(route.data.menuItemId) );
    })
  }

  private initFullMenuItemMap(menuItem: MenuItem[], parent?: MenuItem){
    menuItem.forEach(item => {
      item.parent = parent;
      item.auth = false;
      this.fullMenuItemsMap[item.id] = item;
      if(item.submenu)
        this.initFullMenuItemMap(item.submenu, item);
    });
  }

  private setRoleAuth(menuItem: MenuItem[], parent?: MenuItem){
    menuItem.forEach(item => {
      this.menuItemsMap[item.id] = item;
      item.auth = !item.roles || item.roles?.some(role => this.userService.isRoleAuthorized(role));
      //console.log('setting auth for item: '+item.labelHeader+' - '+item.auth);
      if(item.submenu)
        this.setRoleAuth(item.submenu, item);
    });
  }

  ngOnDestroy(){
    this.userSub?.unsubscribe();
  }

  getMenuItem(id: number | number[]): MenuItem[] {
    if(typeof id === 'number')
      return [this.fullMenuItemsMap[id]];
    else{
      return id.map(id => this.fullMenuItemsMap[id]);
    }
  }

  getMainMenu(){
    return this.menuItemsExternal;
  }

  getApplicationMenu(){
    return this.menuItemsApp;
  }

  onClickMenu(item: MenuItem, thisRef: MenuService) {
    if(!item.url)
      return;

    const url = (typeof item.url === 'string') ? item.url : item.url?.(thisRef.enteService.getCurrentEnte(), thisRef);
    if(url){
      if(item.external){
        window.location.href = url;
        //console.log('window.location.href = ', url);
      } else {
        thisRef.myPayBreadcrumbsService.resetBreadcrumbsIfPageChange(url);
        thisRef.router.navigateByUrl(url);
      }
    }
  }

}
