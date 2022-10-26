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
import * as _ from 'lodash';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

import { Injectable } from '@angular/core';
import { ActivationEnd, NavigationEnd, Route, Router, UrlSegment } from '@angular/router';
import { IconDefinition } from '@fortawesome/fontawesome-common-types';

import { Breadcrumb } from '../model/breadcrumb';
import { MenuItem } from '../model/menu-item';

@Injectable({
  providedIn: 'root'
})
export class MyPayBreadcrumbsService {

  private breadcrumbs: Breadcrumb[] = [];
  private breadcrumbsObs = new BehaviorSubject<Breadcrumb[]>(this.breadcrumbs);

  private menuItemMap:{[key:number]:MenuItem};

  private resetBreadcrumbsIfThisUrl: string;

  constructor(
    private router: Router,
  ) {
    let route: Route;
    let urlSegments: UrlSegment[];
    router.events.pipe(
      filter( event => event instanceof NavigationEnd || event instanceof ActivationEnd)
    ).subscribe( event => {
      if(event instanceof ActivationEnd) {
        route = event.snapshot.routeConfig;
        urlSegments = event.snapshot.url;
      } else if(event instanceof NavigationEnd){
        if(this.resetBreadcrumbsIfThisUrl){
          if(this.resetBreadcrumbsIfThisUrl==event.url){
            this.breadcrumbs = this.breadcrumbs.slice(0,this.breadcrumbs?.[0]?.home ? 1 : 0);
          }
          this.resetBreadcrumbsIfThisUrl = null;
        }
        if(router.getCurrentNavigation().trigger === 'popstate' || router.getCurrentNavigation().extras?.replaceUrl)
          this.breadcrumbs.pop();
        if(route.data?.breadcrumb && (this.breadcrumbs.length===0 || router.getCurrentNavigation().trigger !== 'popstate')){
          const breadcrumb = new Breadcrumb();
          if(route.data?.breadcrumb?.home){
            this.breadcrumbs = [];
            window.history.pushState(null, null, urlSegments.join('/'));
          }
          breadcrumb.label = route.data?.breadcrumb?.label || route.component?.prototype?.titleLabel || route.path;
          breadcrumb.home = route.data?.breadcrumb?.home || false;
          breadcrumb.originalLabel = breadcrumb.label;

          const lastIcon = _.findLast(this.breadcrumbs, b => b.icon!=null)?.icon;
          if(!breadcrumb.icon && (this.menuItemMap?.[route.data?.menuItemId]?.icon as IconDefinition)?.icon){
            const icon = this.menuItemMap?.[route.data?.menuItemId]?.icon as IconDefinition;
            if(lastIcon !== icon)
              breadcrumb.icon = icon;
          }
          if(!breadcrumb.icon)
            breadcrumb.icon = route.data?.breadcrumb?.icon || route.component?.prototype?.titleIcon;

          breadcrumb.originalIcon = breadcrumb.icon;
          if(breadcrumb.icon && breadcrumb.icon === lastIcon)
            breadcrumb.icon = null;

          route = null;
          urlSegments = null;
          this.breadcrumbs.push(breadcrumb);
        }
        this.breadcrumbsObs.next(this.breadcrumbs);
      }
    } );
  }

  public setMenuItemMap(menuItemMap:{[key:number]:MenuItem}){
    this.menuItemMap = menuItemMap;
  }

  public getBreadcrumbs(): Observable<Breadcrumb[]>{
    return this.breadcrumbsObs;
  }

  public resetBreadcrumbs(): void{
    this.breadcrumbs = this.breadcrumbs.slice(0,this.breadcrumbs?.[0]?.home ? 1 : 0);
    this.breadcrumbsObs.next(this.breadcrumbs);
  }

  public resetBreadcrumbsIfPageChange(url: string): void{
    this.resetBreadcrumbsIfThisUrl = url;
  }

  public updateCurrentBreadcrumb(label: string, icon?: IconDefinition): void{
    setTimeout(()=>{
      if(this.breadcrumbs.length<1)
        return;
      const currentBreadcumb = this.breadcrumbs[this.breadcrumbs.length-1];
      currentBreadcumb.label = label;
      if(icon)
        currentBreadcumb.icon = icon;
    }, 0);
  }

  public resetCurrentBreadcrumb(): void{
    setTimeout(()=>{
      if(this.breadcrumbs.length<1)
        return;
      const currentBreadcumb = this.breadcrumbs[this.breadcrumbs.length-1];
      currentBreadcumb.label = currentBreadcumb.originalLabel;
      currentBreadcumb.icon = currentBreadcumb.originalIcon;
    }, 0);
  }

  public goBack(times: number): void{
    if(times<=0)
      return;
    _.times(times-1, ()=>this.breadcrumbs.pop());
    window.history.go(-times);
    this.breadcrumbsObs.next(this.breadcrumbs);
  }

}
