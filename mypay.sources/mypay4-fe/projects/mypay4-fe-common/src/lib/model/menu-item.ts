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
import { IconDefinition } from '@fortawesome/fontawesome-common-types';

import { WithTitle } from '../components/with-title';
import { Ente } from './ente';

export class MenuItem {
  id: number;
  url: string | ((ente: Ente, menuServiceRef: any) => string);
  labelHeader: string;
  labelSidebar: string;
  active: boolean;
  external: boolean;
  position: string;
  icon: string | IconDefinition;
  submenu: MenuItem[];
  parent: MenuItem;
  roles: string[];
  needEnte: boolean;


  logged: boolean;
  anonymous: boolean;
  iconBadgeFun: () => string;
  dontHide: boolean;
  ariaLabel: string;

  auth: boolean;
  opened: boolean = false;

  constructor(id:number, url: string | ((ente: Ente, menuServiceRef: any) => string), label: string | MenuItemLabel | WithTitle, options: {
              active?: boolean,
              external?: boolean,
              logged?: boolean,
              anonymous?: boolean,
              icon?: string | IconDefinition,
              iconBadgeFun?: () => string,
              submenu?: MenuItem[],
              position?: string,
              dontHide?: boolean,
              ariaLabel?: string,
              roles?: string[],
              needEnte?: boolean,
              controller?: any } ){
    this.id = id;
    this.url = url;

    if(label instanceof MenuItemLabel){
      this.labelHeader = label.header;
      this.labelSidebar = label.sidebar;
    }else if(this.instanceOfWithTitle(label)){
      this.labelHeader = label.titleLabel;
      this.labelSidebar = label.titleLabel;
    }else{
      this.labelHeader = label;
      this.labelSidebar = label;
    }

    if(this.instanceOfWithTitle(label)){
      this.icon = label.titleIcon;
    }else{
      this.icon = options?.icon
    }

    this.active = options?.active ?? false;
    this.external = options?.external ?? false;
    this.logged = options?.logged ?? true;
    this.anonymous = options?.anonymous ?? true;
    this.iconBadgeFun = options?.iconBadgeFun;
    this.submenu = options?.submenu;
    this.position = options?.position ?? 'center';
    this.dontHide = options?.dontHide ?? false;
    this.ariaLabel = options?.ariaLabel || label?.toString();
    this.roles = options?.roles;
    this.needEnte = options?.needEnte ?? true;
  }

  hasImgIcon(){
    return this.icon && typeof this.icon === 'string';
  }

  hasFAIcon(){
    return this.icon && typeof this.icon !== 'string';
  }

  hasFABadge(){
    return this.iconBadgeFun?.()!=null;
  }

  instanceOfWithTitle(object: any): object is WithTitle {
    return object.titleLabel && object.titleIcon;
  }

  getSidebarLabel(){
    return this.labelSidebar;
  }

  getHeaderLabel(){
    return this.labelHeader;
  }

  getAriaLabel(){
    return this.ariaLabel || this.labelHeader;
  }

  getId(){
    return this.id;
  }
}

export class MenuItemLabel{
  sidebar: string;
  header: string;

  constructor(sidebar:string, header:string){
    this.sidebar = sidebar;
    this.header = header;
  }

  toString() {
    return this.sidebar || this.header;
  }
}
