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
import { ToastrService } from 'ngx-toastr';
import { Ente, manageError, User, UserService } from 'projects/mypay4-fe-common/src/public-api';
import { Observable, Subscription } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup } from '@angular/forms';
import { MatAutocomplete } from '@angular/material/autocomplete';
import { Router } from '@angular/router';
import { faBars, faSignOutAlt, faUser } from '@fortawesome/free-solid-svg-icons';

import { EnteService } from '../../services/ente.service';
import { SidenavService } from '../../services/sidenav.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {

  @ViewChild(MatAutocomplete) autocompleteEnte: MatAutocomplete;

  iconBars = faBars;
  iconUser = faUser;
  itemSignOut = faSignOutAlt;

  singleEnte: boolean = false;
  enteOptions: Ente[];
  enteFilteredOptions: Observable<Ente[]>;

  blockingError: boolean = false;
  form: FormGroup;
  private userChangeSub:Subscription;
  private enteChangeSub:Subscription;

  constructor(
    private formBuilder: FormBuilder,
    public enteService: EnteService,
    public userService: UserService,
    private toastrService: ToastrService,
    private sidenav: SidenavService,
    private router: Router) {
      this.form = this.formBuilder.group({
        ente: ['', [this.enteValidator]]
      });
    }

  ngOnInit(): void {
    //subscribe to login/logout events
    this.userChangeSub = this.userService.getLoggedUserObs().subscribe(user => this.onUserChanged(this, user));
    this.enteChangeSub = this.enteService.getCurrentEnteObs().subscribe(ente => {
      if(ente && this.enteOptions.find(e => e.codIpaEnte === ente.codIpaEnte))
        this.form.get('ente').setValue(ente);
    });
  }

  ngOnDestroy(): void {
    this.userChangeSub?.unsubscribe();
    this.enteChangeSub?.unsubscribe();
  }

  private onUserChanged(thisRef:HeaderComponent, user:User){
    if(user == null) {
      thisRef.enteOptions = [];
      thisRef.singleEnte = false;
    } else {
      thisRef.form.get('ente').setValue(null);
      //load enti from backend
      if(!user.emailValidationNeeded)
        thisRef.enteService.getEntiByOperatoreUserId().subscribe(enti => {
          thisRef.enteOptions = enti;
          thisRef.singleEnte = thisRef.enteOptions?.length === 1;
          if(thisRef.singleEnte){
            console.log('forcing ente to the single ente of operatore: ', thisRef.enteOptions[0].codIpaEnte);
            thisRef.enteService.setCurrentEnte(thisRef.enteOptions[0]);
            thisRef.form.get('ente').setValue(thisRef.enteOptions[0]);
          }
          //init autocomplete feature of ente field
          thisRef.enteFilteredOptions = thisRef.form.get('ente').valueChanges
            .pipe(
              startWith(''),
              map(value => typeof value === 'string' || !value ? value : value.deNomeEnte),
              map(deNomeEnte => deNomeEnte ? thisRef._enteFilter(deNomeEnte) : thisRef.enteOptions.slice())
            );
        }, error => {
          if(error?.status === 401){
            const msg = error.error?.message;
            const errorUID = error?.error?.errorUID ?? error?.errorUID;
            //if not authorized user is not an operatore -> go to not-authorized page
            this.router.navigate(['not-authorized'],{state:{msg:msg, errorUID: errorUID}});
          } else
            manageError('Errore caricando l\'elenco degli enti', this.toastrService, ()=>{this.blockingError=true})(error);
        } );
    }
  }

  logout() {
    this.userService.logout();
    this.sidenav.close();
  }

  toggleSidenav() {
    this.sidenav.toggle();
  }

  private enteValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteId != null ) ? null : {'invalid': true};
  };

  enteDisplayFn(ente: Ente): string {
    return ente ? ente.deNomeEnte : '';
  }

  private _enteFilter(name: string): Ente[] {
    const filterValue = name.toLowerCase();
    return this.enteOptions.filter(option => option.deNomeEnte.toLowerCase().indexOf(filterValue) !== -1);
  }

  private verifyEnte(){
    const currentEnte = this.enteService.getCurrentEnte();
    const currentValue = this.form.get('ente').value;
    if(!currentValue || typeof currentValue==='string' && (<string>currentValue).trim().length===0){
      this.enteService.setCurrentEnte(null);
    } else if(currentValue.mygovEnteId != null) {
      this.enteService.setCurrentEnte(currentValue);
    }
    //reset input field to previous valid value when new value is invalid
    this.form.get('ente').setValue(this.enteService.getCurrentEnte());

    const newEnte = this.enteService.getCurrentEnte();
    if( currentEnte && newEnte?.mygovEnteId !== currentEnte?.mygovEnteId ){
      //changed ente: go to home
      this.router.navigate(['cards']);
    }
  }

  enteFocusout($ev){
    if(!this.autocompleteEnte.isOpen)
      this.verifyEnte();
  }

  enteSelected(ente: Ente){
    this.verifyEnte();
  }

  enteClosed(){
    this.verifyEnte();
  }

  gotoLogoLocation(){
    //define here the action when logo on header is clicked
  }

}
