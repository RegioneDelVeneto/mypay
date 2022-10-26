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
import { filter } from 'rxjs/operators';

import { Location } from '@angular/common';
import { Injectable } from '@angular/core';
import { NavigationStart, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class PageStateService {

  private currentNavigationState: NavigationStart;
  private mapObjState = new Map<number, any>();


  constructor(router: Router, public location: Location) {

    router.events.pipe(
      // The "events" stream contains all the navigation events. For this demo,
      // though, we only care about the NavigationStart event as it contains
      // information about what initiated the navigation sequence.
      filter( event => event instanceof NavigationStart)
    ).subscribe( (event:NavigationStart) => {
        this.currentNavigationState = event;

        console.group( "NavigationStart Event" );
        // Every navigation sequence is given a unique ID. Even "popstate"
        // navigations are really just "roll forward" navigations that get
        // a new, unique ID.
        console.log( "navigation id:", event.id );
        console.log( "route:", event.url );
        // The "navigationTrigger" will be one of:
        // --
        // - imperative (ie, user clicked a link).
        // - popstate (ie, browser controlled change such as Back button).
        // - hashchange
        // --
        // NOTE: I am not sure what triggers the "hashchange" type.
        console.log( "trigger:", event.navigationTrigger );

        console.log( "state:", this.mapObjState.get(event.id) );

        // This "restoredState" property is defined when the navigation
        // event is triggered by a "popstate" event (ex, back / forward
        // buttons). It will contain the ID of the earlier navigation event
        // to which the browser is returning.
        // --
        // CAUTION: This ID may not be part of the current page rendering.
        // This value is pulled out of the browser; and, may exist across
        // page refreshes.
        if ( event.restoredState ) {

          console.warn(
            "restoring navigation id:",
            event.restoredState.navigationId
          );

        }
        console.groupEnd();
      });

   }

  isNavigatingBack(): boolean {
    return this.currentNavigationState?.restoredState != null;
  }

  saveState(obj:any): number {
    const currentNavigationId = (<any>this.location.getState())?.navigationId;
    if(!currentNavigationId)
      return;
    this.mapObjState.set(currentNavigationId, obj);
    return currentNavigationId;
  }

  addToSavedState(navId: number, key: string, value:any): void {
    let obj = this.mapObjState.get(navId);
    if(!obj){
      obj = {};
      this.mapObjState.set(navId, obj);
    }
    obj[key] = value;
  }

  getSavedStateByKey(key: string, navId: number = null) {
    if(this.isNavigatingBack()){
      const previousNavigationId = this.currentNavigationState.restoredState.navigationId;
      const obj = this.mapObjState.get(previousNavigationId) || {};
      return obj[key];
    } else if (navId) {
      const obj = this.mapObjState.get(navId) || {};
      return obj[key];
    }
  }

  restoreState(navId: number = null): any {
    if(this.isNavigatingBack()){
      const previousNavigationId = this.currentNavigationState.restoredState.navigationId;
      const obj = this.mapObjState.get(previousNavigationId);
      this.mapObjState.delete(previousNavigationId);
      return obj;
    } else if (navId) {
      const obj = this.mapObjState.get(navId);
      this.mapObjState.delete(navId);
      return obj;
    }
  }
}
