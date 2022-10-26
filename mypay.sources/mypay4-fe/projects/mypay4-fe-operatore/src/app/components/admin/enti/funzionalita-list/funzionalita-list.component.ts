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
import { DateTime } from 'luxon';
import { ToastrService } from 'ngx-toastr';
import {
    ConfirmDialogComponent
} from 'projects/mypay4-fe-common/src/lib/components/confirm-dialog/confirm-dialog.component';
import {
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import {
    manageError, OverlaySpinnerService, PageStateService, PaginatorData, TableAction, TableColumn, UserService
} from 'projects/mypay4-fe-common/src/public-api';
import { first } from 'rxjs/operators';

import { DatePipe } from '@angular/common';
import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { faBook, faSearch } from '@fortawesome/free-solid-svg-icons';

import { WithActions } from '../../../../../../../mypay4-fe-common/src/lib/table/with-actions';
import { EnteFunzionalita } from '../../../../model/ente-funzionalita';
import { AdminEntiService } from '../../../../services/admin-enti.service';
import { RegistroComponent } from '../../registro/registro.component';
import { Subscription } from 'rxjs/internal/Subscription';

@Component({
  selector: 'funzionalita-list',
  templateUrl: './funzionalita-list.component.html',
  styleUrls: ['./funzionalita-list.component.scss']
})
export class FunzionalitaListComponent implements OnInit {

  @Input() mygovEnteId: number;

  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<EnteFunzionalita>;
  tableColumns: TableColumn[] = [
    new TableColumn('codFunzionalita', 'Codice Funzionalità'),
    new TableColumn('statoAbilitazione', 'Stato'),
    new TableColumn('dtUltimaAbilitazione', 'Data ultima abilitazione',
      { sortable: (item: EnteFunzionalita) => item.dtUltimaAbilitazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] }),
    new TableColumn('dtUltimaDisabilitazione', 'Data ultima disabilitazione',
      { sortable: (item: EnteFunzionalita) => item.dtUltimaDisabilitazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] }),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(faSearch, this.activateEnteFunzionalita, this.activateEnteFunzionalitaEnabled, 'Abilita funzionalità'),
      new TableAction(faSearch, this.deactivateEnteFunzionalita, this.deactivateEnteFunzionalitaEnabled, 'Disabilita funzionalità'),
      new TableAction(faBook, this.getRegistroFunzionalita, this.getRegistroFunzionalitaEnebled, 'Registro cambio stato'),
      ] } ) ];
  tableData: EnteFunzionalita[];
  paginatorData: PaginatorData;

  hasSearched: boolean = false;
  blockingError: boolean = false;
  isUserAdmin: boolean = false;
  private userSub: Subscription;

  constructor(
    private pageStateService: PageStateService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private adminEntiService: AdminEntiService,
    private toastrService: ToastrService,
    private dialog: MatDialog,
    private userService:UserService
  ) {
    this.userSub = userService.getLoggedUserObs().subscribe(() => {
      this.isUserAdmin = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE);
    });
  }

  ngOnInit(): void {
    //retrieve page state data if navigating back
    if(this.pageStateService.isNavigatingBack()){
      const pageState = this.pageStateService.restoreState();
      if(pageState){
        setTimeout(()=>{
          this.tableData = pageState.tableData;
          this.paginatorData = pageState.paginatorData;
        });
      }
    }
    this.onSubmit(); //TODO Retreive all funzionalita by mygovEnteId.
  }

  ngOnDestroy():void {
  }

  onSubmit(){
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);

    this.adminEntiService.searchEnteFunzionalita(this.mygovEnteId).subscribe(data => {
      this.hasSearched = true;
      this.tableData = data;
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  getRegistroFunzionalita(elementRef: EnteFunzionalita, thisRef: FunzionalitaListComponent) {
    thisRef.adminEntiService.getRegistroFunzionalita(thisRef.mygovEnteId, elementRef.codFunzionalita).subscribe(data => {
      //open detail panel
      thisRef.dialog.open(RegistroComponent, {panelClass: 'mypay4-registro-panel', autoFocus:false, data: {
        tableData: data,
        title: `cambio stato abilitazione - Funzionalità: ${elementRef.codFunzionalita}`,
      } } );
    });
  }

  getRegistroFunzionalitaEnebled(elementRef: EnteFunzionalita, thisRef: FunzionalitaListComponent) { return thisRef.isUserAdmin; }

  activateEnteFunzionalitaEnabled(elementRef: EnteFunzionalita, thisRef: FunzionalitaListComponent) {
    return !elementRef.flgAttivo && thisRef.isUserAdmin;
  }

  activateEnteFunzionalita(elementRef: EnteFunzionalita, thisRef: FunzionalitaListComponent) {
    const msg = `Confermi di voler attivare funzionalità: ${elementRef.codFunzionalita}?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminEntiService.activateEnteFunzionalita(elementRef.mygovEnteFunzionalitaId).subscribe( () => {
          thisRef.toastrService.success('Ente funzionalità abilitato correttamente.');
          elementRef.flgAttivo = true;
          elementRef.statoAbilitazione = 'Abilitato';
          elementRef.dtUltimaAbilitazione = DateTime.now();
          WithActions.reset(elementRef);
          thisRef.overlaySpinnerService.detach(spinner);
        }, manageError('Errore effettuando l\'aggiornamento', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    });
  }

  deactivateEnteFunzionalitaEnabled(elementRef: EnteFunzionalita, thisRef: FunzionalitaListComponent) {
    return elementRef.flgAttivo && thisRef.isUserAdmin;
  }

  deactivateEnteFunzionalita(elementRef: EnteFunzionalita, thisRef: FunzionalitaListComponent) {
    const msg = `Confermi di voler disattivare funzionalità: ${elementRef.codFunzionalita}?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminEntiService.deactivateEnteFunzionalita(elementRef.mygovEnteFunzionalitaId).subscribe( () => {
          thisRef.toastrService.success('Ente funzionalità disabilitato correttamente.');
          elementRef.flgAttivo = false;
          elementRef.statoAbilitazione = 'Disabilitato';
          elementRef.dtUltimaDisabilitazione = DateTime.now();
          WithActions.reset(elementRef);
          thisRef.overlaySpinnerService.detach(spinner);
        }, manageError('Errore effettuando l\'aggiornamento', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    });
  }
}
