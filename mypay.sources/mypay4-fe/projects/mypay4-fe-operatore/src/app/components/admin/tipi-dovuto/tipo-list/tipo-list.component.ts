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
    manageError, OverlaySpinnerService, PageStateService, PaginatorData, TableAction, TableColumn,
    UserService, validateFormFun, WithActions, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';
import { first } from 'rxjs/operators';

import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { faBook, faClone, faSearch } from '@fortawesome/free-solid-svg-icons';

import { TipoDovuto } from '../../../../../../../mypay4-fe-common/src/lib/model/tipo-dovuto';
import { AdminEntiService } from '../../../../services/admin-enti.service';
import { ManageMode } from '../../manage-mode';
import { RegistroComponent } from '../../registro/registro.component';

@Component({
  selector: 'app-tipo-list',
  templateUrl: './tipo-list.component.html',
  styleUrls: ['./tipo-list.component.scss']
})
export class TipoListComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return "Gestione tipi dovuto" }
  get titleIcon(){ return faClone }

  manageMode: ManageMode;

  form: FormGroup;
  formErrors = {};
  private formChangesSub:Subscription;

  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<TipoDovuto>;
  tableColumns: TableColumn[] = [
    new TableColumn('codTipo', 'Codice', { htlmId: () => this.manageMode === ManageMode.TipoDovuto ? 'codTipoLarge' : null}),
    new TableColumn('deTipo', 'Descrizione', { htlmId: () => this.manageMode === ManageMode.TipoDovuto ? 'deTipoLarge' : null}),
    new TableColumn('statoAbilitazione', 'Stato', { dispCondition: () => this.manageMode === ManageMode.Ente }),
    new TableColumn('dtUltimaAbilitazione', 'Data ultima abilitazione', { dispCondition: ()=>this.manageMode === ManageMode.Ente,
      sortable: (item: TipoDovuto) => item.dtUltimaAbilitazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] }),
    new TableColumn('dtUltimaDisabilitazione', 'Data ultima disabilitazione', { dispCondition: ()=>this.manageMode === ManageMode.Ente,
      sortable: (item: TipoDovuto) => item.dtUltimaDisabilitazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] }),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(faSearch, this.gotoDetails, null , 'Dettaglio tipo dovuto'),
      new TableAction(faSearch, this.activateTipoDovuto, this.activateTipoDovutoEnabled, 'Abilita tipo dovuto'),
      new TableAction(faSearch, this.deactivateTipoDovuto, this.deactivateTipoDovutoEnabled, 'Disabilita tipo dovuto'),
      new TableAction(faSearch, this.gotoDelete, this.isEnteModeForSysAdmin, 'Cancella tipo dovuto'),
      new TableAction(faBook, this.getRegistroTipoDovuto, this.isEnteMode, 'Registro cambio stato'),
      ] } ) ];
  tableData: TipoDovuto[];
  paginatorData: PaginatorData;

  hasSearched: boolean = false;
  blockingError: boolean = false;
  private userSub: Subscription;
  isUserAdmin: boolean = false;
  isUserAdminEnte: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private pageStateService: PageStateService,
    private router: Router,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private adminEntiService: AdminEntiService,
    private toastrService: ToastrService,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private userService: UserService
  ) {
    this.form = this.formBuilder.group({
      codTipo: [''],
      deTipo: [''],
      flgAttivo: ['']
    });
    this.formChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));
    this.userSub = userService.getLoggedUserObs().subscribe(() => {
      this.isUserAdmin = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE);
      this.isUserAdminEnte = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ENTE_ROLE);
    });
  }

  ngOnInit(): void {
    const params = this.route.snapshot.params;
    if (params['enteId'])
      this.manageMode = ManageMode.Ente;
    else
      this.manageMode = ManageMode.TipoDovuto;

    if (this.manageMode === ManageMode.Ente) {
      //retrieve page state data if navigating back
      if(this.pageStateService.isNavigatingBack()){
        if (this.pageStateService.getSavedStateByKey('tabIndex') === 1) {
          const pageState = this.pageStateService.restoreState();
          if(pageState){
            this.form.setValue(pageState.formData);
            setTimeout(()=>{
              this.tableData = pageState.tableData;
              this.paginatorData = pageState.paginatorData;
            });
          }
        }
      }
      this.onSubmit(); //TODO Retreive all tipo dovuto by mygovEnteId.
    }
  }

  ngOnDestroy():void {
    this.formChangesSub?.unsubscribe();
  }

  onSubmit(){
    const i = this.form.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    let searchTipiDovutoFun;
    if (this.manageMode === ManageMode.Ente) {
      let enteId = Number(this.route.snapshot.params['enteId']);
      searchTipiDovutoFun = this.adminEntiService.searchTipiDovutoByEnte.bind(this.adminEntiService, enteId, i.codTipo, i.deTipo, i.flgAttivo);
    } else if (this.manageMode === ManageMode.TipoDovuto) {
      searchTipiDovutoFun = this.adminEntiService.searchTipiDovuto.bind(this.adminEntiService, i.codTipo, i.deTipo, i.flgAttivo);
    }
    searchTipiDovutoFun().subscribe(data => {
      this.hasSearched = true;
      this.tableData = data;
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onReset(){
    this.form.reset();
    this.hasSearched = false;
    this.tableData = null;
  }

  gotoDetails(elementRef: TipoDovuto, thisRef: TipoListComponent, eventRef: any){
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.pageStateService.saveState({
      tabIndex: thisRef.manageMode === ManageMode.Ente ? 1 : undefined,
      formData: thisRef.form.value,
      tableData: thisRef.tableData,
      paginatorData: {
        pageSize: thisRef.mypayTableComponent.paginator.pageSize,
        pageIndex: thisRef.mypayTableComponent.paginator.pageIndex
      }
    });
    if (thisRef.manageMode === ManageMode.Ente) {
      let enteId = Number(thisRef.route.snapshot.params['enteId']);
      thisRef.router.navigate(['admin/enti/tipo/details', enteId, elementRef.mygovEnteTipoDovutoId]);
    } else if (thisRef.manageMode === ManageMode.TipoDovuto) {
      thisRef.router.navigate(['admin/tipiDovuto/tipo', elementRef.codTipo]);
    }
  }

  activateTipoDovutoEnabled(elementRef: TipoDovuto, thisRef: TipoListComponent) {
    return thisRef.manageMode === ManageMode.Ente && !elementRef.flgAttivo;
  }

  activateTipoDovuto(elementRef: TipoDovuto, thisRef: TipoListComponent) {
    const msg = `Confermi di voler attivare il tipo dovuto "${elementRef.deTipo}" per l'ente "${elementRef.deNomeEnte}"?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminEntiService.activateTipoDovuto(elementRef.mygovEnteTipoDovutoId).subscribe( num => {
          thisRef.toastrService.success('Tipo dovuto abilitato correttamente.');
          elementRef.flgAttivo = true;
          elementRef.statoAbilitazione = 'Abilitato';
          elementRef.dtUltimaAbilitazione = DateTime.now();
          WithActions.reset(elementRef);
          thisRef.overlaySpinnerService.detach(spinner);
        }, manageError('Errore effettuando l\'abilitazione', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
      });
  }

  deactivateTipoDovutoEnabled(elementRef: TipoDovuto, thisRef: TipoListComponent) {
    return thisRef.manageMode === ManageMode.Ente && elementRef.flgAttivo;
  }

  deactivateTipoDovuto(elementRef: TipoDovuto, thisRef: TipoListComponent) {
    const msg = `Attenzione: prima di disattivare un dovuto è necessario verificare se esistono posizioni aperte, 
    in quanto tali posizioni non saranno più pagabili. Se il dovuto non ha posizioni aperte, allora è possibile disattivarlo. 
    Confermi di voler disattivare il tipo dovuto "${elementRef.deTipo}" per l'ente "${elementRef.deNomeEnte}"?`;

    thisRef.dialog.open(ConfirmDialogComponent, { autoFocus: false, data: { message: msg } })
      .afterClosed().pipe(first()).subscribe(result => {
        if (result === "false") return;
          const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
          thisRef.adminEntiService.deactivateTipoDovuto(elementRef.mygovEnteTipoDovutoId).subscribe(num => {
            thisRef.toastrService.success('Tipo dovuto disabilitato correttamente.');
            elementRef.flgAttivo = false;
            elementRef.statoAbilitazione = 'Disabilitato';
            elementRef.dtUltimaDisabilitazione = DateTime.now();
            WithActions.reset(elementRef);
            thisRef.overlaySpinnerService.detach(spinner);
          }, manageError('Errore effettuando la disabilitazione', thisRef.toastrService, () => { thisRef.overlaySpinnerService.detach(spinner) }));
      });
  }

  isEnteMode(elementRef: TipoDovuto, thisRef: TipoListComponent) {
    return thisRef.manageMode === ManageMode.Ente;
  }

  isEnteModeForSysAdmin(elementRef: TipoDovuto, thisRef: TipoListComponent) {
    return thisRef.isEnteMode(elementRef, thisRef) && thisRef.isUserAdmin;
  }

  gotoDelete(elementRef: TipoDovuto, thisRef: TipoListComponent) {
    const msg = `Attenzione: prima di cancellare un dovuto è necessario se esistono posizioni aperte.
    Se il dovuto non ha posizioni aperte, allora è possibile cancellarlo.
    Confermi di voler cancellare il tipo dovuto "${elementRef.deTipo}" per l'ente "${elementRef.deNomeEnte}"?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminEntiService.deleteTipoDovuto(elementRef.mygovEnteTipoDovutoId).subscribe( num => {
          thisRef.onSubmit();
          thisRef.toastrService.success('Tipo dovuto cancellato correttamente.');
          thisRef.overlaySpinnerService.detach(spinner);
        }, manageError('Errore effettuando la cancellazione', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
      });
  }

  onInsert() {
    this.pageStateService.saveState({
      tabIndex: this.manageMode === ManageMode.Ente ? 1 : undefined,
      formData: this.form.value,
      tableData: this.tableData,
      paginatorData: {
        pageSize: this.mypayTableComponent.paginator.pageSize,
        pageIndex: this.mypayTableComponent.paginator.pageIndex
      }
    });
    let enteId = Number(this.route.snapshot.params['enteId']);
    this.router.navigate(['admin/enti/tipo/anagrafica', enteId]);
  }

  getRegistroTipoDovuto(elementRef: TipoDovuto, thisRef: TipoListComponent) {
    thisRef.adminEntiService.getRegistroTipoDovuto(elementRef.mygovEnteTipoDovutoId).subscribe(data => {
      //open detail panel
      thisRef.dialog.open(RegistroComponent, {panelClass: 'mypay4-registro-panel', autoFocus:false, data: {
        tableData: data,
        title: `cambio stato abilitazione - Tipo dovuto: ${elementRef.deTipo}`,
      } } );
    });
  }

}
