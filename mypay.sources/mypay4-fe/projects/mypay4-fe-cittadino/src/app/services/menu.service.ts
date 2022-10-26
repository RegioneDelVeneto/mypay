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
    MenuItem, MenuItemLabel, MyPayBreadcrumbsService
} from 'projects/mypay4-fe-common/src/public-api';

import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { faQuestionCircle } from '@fortawesome/free-solid-svg-icons';

import { AvvisiComponent } from '../components/avvisi/avvisi.component';
import { CardsComponent } from '../components/cards/cards.component';
import { CarrelloComponent } from '../components/carrello/carrello.component';
import { DatiUtenteComponent } from '../components/dati-utente/dati-utente.component';
import { DebitiComponent } from '../components/debiti/debiti.component';
import { HomeComponent } from '../components/home/home.component';
import { PagatiComponent } from '../components/pagati/pagati.component';
import { SpontaneoComponent } from '../components/spontaneo/spontaneo.component';
import { CarrelloService } from './carrello.service';

@Injectable({
  providedIn: 'root'
})
export class MenuService {

  private menuItemsMap:{[key:number]:MenuItem} = {};
  private selectedMenuItemFirstLevel:MenuItem;

  private menuItemsSecondLevelAnonymous:MenuItem[];
  private menuItemsSecondLevelLogged:MenuItem[];

  private menuItems: MenuItem[];

  constructor(
    private router: Router,
    carrelloService: CarrelloService,
    private myPayBreadcrumbsService: MyPayBreadcrumbsService,
    ) {
    const carrelloBadgeFun = ()=>carrelloService.size()>0?(""+carrelloService.size()):null;
    this.menuItems = [
      new MenuItem(10,'#', 'Link esterno 1', {external: true, anonymous: false}),
      new MenuItem(11,'#', 'Link esterno 2', {external: true, anonymous: false}),
      new MenuItem(13,'/home', 'Pagamenti', {active: true, submenu: [
        new MenuItem(20,'/home', HomeComponent.prototype, {logged: false}),
        new MenuItem(21,'/cards', CardsComponent.prototype, {anonymous: false}),
        new MenuItem(22,'/debiti', DebitiComponent.prototype, {anonymous: false}),
        new MenuItem(23,'/pagati', PagatiComponent.prototype, {anonymous: false}),
        new MenuItem(24,'/avvisi', AvvisiComponent.prototype, {}),
        new MenuItem(32,'/spontaneo', SpontaneoComponent.prototype, {}),
        new MenuItem(26,'/carrello', CarrelloComponent.prototype, {iconBadgeFun: carrelloBadgeFun}),
        new MenuItem(28,'/utente', DatiUtenteComponent.prototype, {anonymous: false}),
      ]}),
      new MenuItem(18,'#', new MenuItemLabel('Aiuto',null), {external: true, icon:faQuestionCircle, position:'right', dontHide: true})
    ];
    this._buildMenuItemsMap(this.menuItems);
    myPayBreadcrumbsService.setMenuItemMap(this.menuItemsMap);
  }

  private _buildMenuItemsMap(array: MenuItem[], parentItem: MenuItem = null){
    array.forEach(item => {
      item.parent = parentItem;
      this.menuItemsMap[item.id] = item;
      if(item.submenu)
        this._buildMenuItemsMap(item.submenu, item);
    });
  }

  getMenuItem(id: number): MenuItem {
    return this.menuItemsMap[id];
  }

  getFirstLevelMenuItemByUrl(url: string, parentId: number = null): MenuItem {
    const root = parentId ? this.getMenuItem(parentId)?.submenu : this.menuItems;
    const item = this._findMenuItemByUrl(root, url);
    return this._findRootItem(item);
  }

  getDefaultFirstLevelMenuItem(): MenuItem {
    return this.getMenuItem(13);
  }

  private _findRootItem(item: MenuItem): MenuItem {
    return item?.parent ? this._findRootItem(item.parent) : item;
  }

  private _findMenuItemByUrl(array: MenuItem[], url: string): MenuItem {
    for(const item of array){
      if(item.url === url)
        return item;
      if(item.submenu){
        const foundItem = this._findMenuItemByUrl(item.submenu, url);
        if(foundItem)
          return foundItem;
      }
    }
    return null;
  }

  private firstLevelCache: Map<string, MenuItem[]> = new Map();
  getMenuFirstLevel(forLogged: boolean, onlyPosition: string = null) {
    const key = forLogged+"|"+onlyPosition;
    if(!this.firstLevelCache.has(key))
      this.firstLevelCache.set(key, this.menuItems.filter(item => (forLogged ? item.logged : item.anonymous) && (!onlyPosition || item.position === onlyPosition)));
    return this.firstLevelCache.get(key);
  }

  getMenuSecondLevel(forLogged: boolean):MenuItem[] {
    return forLogged ? this.menuItemsSecondLevelLogged : this.menuItemsSecondLevelAnonymous;
  }

  setSelectedMenuItemFirstLevel(item: MenuItem){
    this.selectedMenuItemFirstLevel = item;
    this.menuItemsSecondLevelAnonymous = this.selectedMenuItemFirstLevel?.submenu?.filter(item => item.anonymous);
    this.menuItemsSecondLevelLogged = this.selectedMenuItemFirstLevel?.submenu?.filter(item => item.logged);
  }

  onClickFirstLevel(item: MenuItem, thisRef: MenuService) {
    if(!item.url)
      return;

    const url = (typeof item.url === 'string') ? item.url : item.url?.(null, thisRef);
    if(url){
      if(item.external)
        window.location.href = url;
      else {
        thisRef.setSelectedMenuItemFirstLevel(item);
        thisRef.myPayBreadcrumbsService.resetBreadcrumbsIfPageChange(url);
        thisRef.router.navigate([url]);
      }
    }
  }

}
