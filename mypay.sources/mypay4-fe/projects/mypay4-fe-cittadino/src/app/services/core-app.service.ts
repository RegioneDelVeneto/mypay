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
import { BehaviorSubject, Observable } from 'rxjs';

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CoreAppService {

  private DEFAULT_STATE = new CoreAppElementsActivation();

  private activationState = new BehaviorSubject<CoreAppElementsActivation>(this.DEFAULT_STATE);

  constructor() { 
    //This is intentionally empty
  }

  public setState(newState: CoreAppElementsActivation){
    const currentState = this.activationState.getValue();
    if(!CoreAppElementsActivation.isEqual(currentState, newState))
      this.activationState.next(new CoreAppElementsActivation(newState));
  }

  public resetState(){
    this.setState(this.DEFAULT_STATE);
  }

  public getStateObs():Observable<CoreAppElementsActivation> {
    return this.activationState.asObservable();
  }
}

export class CoreAppElementsActivation {
  header: boolean = true;
  sidenav: boolean = true;
  footer: boolean = true;

  constructor(elem?: CoreAppElementsActivation){
    if(elem){
      this.header = elem.header;
      this.sidenav = elem.sidenav;
      this.footer = elem.footer;
    }
  }

  static isEqual(first: CoreAppElementsActivation, second: CoreAppElementsActivation){
    return !first && !second ||
      first?.header === second?.header &&
      first?.sidenav === second?.sidenav &&
      first?.footer === second?.footer;
  }
}
