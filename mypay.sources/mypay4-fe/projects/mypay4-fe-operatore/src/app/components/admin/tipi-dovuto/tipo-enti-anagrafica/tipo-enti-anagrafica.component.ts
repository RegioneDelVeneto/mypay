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
import {
    ConfirmDialogComponent
} from 'projects/mypay4-fe-common/src/lib/components/confirm-dialog/confirm-dialog.component';
import {
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import {
    manageError, OverlaySpinnerService, PageStateService, PaginatorData, TableAction, TableColumn,
    validateFormFun, WithActions, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';
import { first } from 'rxjs/operators';

import { Location } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { faClone, faSearch } from '@fortawesome/free-solid-svg-icons';

import { TipoDovuto } from '../../../../../../../mypay4-fe-common/src/lib/model/tipo-dovuto';
import { AdminEntiService } from '../../../../services/admin-enti.service';
import { ManageMode } from '../../manage-mode';

@Component({
  selector: 'app-tipo-enti-anagrafica',
  templateUrl: './tipo-enti-anagrafica.component.html',
  styleUrls: ['./tipo-enti-anagrafica.component.scss']
})
export class TipoEntiAnagraficaComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return "Dettaglio tipo dovuto" }
  get titleIcon(){ return faClone }

  codTipo: string;
  deTipo: string;
  formAnag: FormGroup;
  formAnagErrors = {};
  private formAnagChangesSub:Subscription;

  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<TipoDovuto>;
  tableColumns: TableColumn[] = [
    new TableColumn('thumbLogoEnte', null, {type:'img64', ariaLabel:'Logo'}),
    new TableColumn('deNomeEnte', 'Nome'),
    new TableColumn('codIpaEnte', 'Codice IPA'),
    new TableColumn('statoAbilitazione', 'Stato'),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(faSearch, this.gotoDetailsEnte, null , 'Visualizza dettaglio Ente'),
      new TableAction(faSearch, this.gotoDetailsEnteTipoDovuto, null , 'Visualizza dettaglio Ente Tipo dovuto'),
      new TableAction(faSearch, this.activateTipoDovuto, this.activateTipoDovutoEnabled, 'Abilita tipo dovuto'),
      new TableAction(faSearch, this.deactivateTipoDovuto, this.deactivateTipoDovutoEnabled, 'Disabilita tipo dovuto'),
      ] } ) ];
  tableData: TipoDovuto[];
  paginatorData: PaginatorData;

  hasSearched: boolean = false;
  blockingError: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private pageStateService: PageStateService,
    private router: Router,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private adminEntiService: AdminEntiService,
    private toastrService: ToastrService,
    private route: ActivatedRoute,
    private location: Location,
    private dialog: MatDialog,
  ) {
    this.formAnag = this.formBuilder.group({
      codIpaEnte: [''],
      deNomeEnte: [''],
    });
    this.formAnagChangesSub = this.formAnag.valueChanges.subscribe(validateFormFun(this.formAnag, this.formAnagErrors));
  }

  ngOnInit(): void {
    const params = this.route.snapshot.params;
    this.codTipo = params['codTipo'];
    //retrieve page state data if navigating back
    if(this.pageStateService.isNavigatingBack()){
      const pageState = this.pageStateService.restoreState();
      if(pageState){
        this.formAnag.setValue(pageState.formData);
        setTimeout(()=>{
          this.tableData = pageState.tableData;
          this.paginatorData = pageState.paginatorData;
        });
      }
    }
    this.onSearch();
  }

  ngOnDestroy():void {
    this.formAnagChangesSub?.unsubscribe();
  }

  goBack(){
    this.location.back();
  }

  onSearch(){
    const i = this.formAnag.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.adminEntiService.searchTipiDovutoByTipo(this.codTipo, i.codIpaEnte, i.deNomeEnte, null).subscribe(data => {
      this.hasSearched = true;
      this.tableData = data;
      this.deTipo = data[0]?.deTipo;
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onReset(){
    this.formAnag.reset();
    this.hasSearched = false;
    this.tableData = null;
  }

  gotoDetailsEnte(elementRef: TipoDovuto, thisRef: TipoEntiAnagraficaComponent, eventRef: any){
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.pageStateService.saveState({
      formData: thisRef.formAnag.value,
      tableData: thisRef.tableData,
      paginatorData: {
        pageSize: thisRef.mypayTableComponent.paginator.pageSize,
        pageIndex: thisRef.mypayTableComponent.paginator.pageIndex
      }
    });
    thisRef.router.navigate(['admin/enti/details', elementRef.mygovEnteId]);
  }

  gotoDetailsEnteTipoDovuto(elementRef: TipoDovuto, thisRef: TipoEntiAnagraficaComponent, eventRef: any){
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.pageStateService.saveState({
      formData: thisRef.formAnag.value,
      tableData: thisRef.tableData,
      paginatorData: {
        pageSize: thisRef.mypayTableComponent.paginator.pageSize,
        pageIndex: thisRef.mypayTableComponent.paginator.pageIndex
      }
    });
    thisRef.router.navigate(['admin/enti/tipo/details', elementRef.mygovEnteId, elementRef.mygovEnteTipoDovutoId]);
  }

  activateTipoDovutoEnabled(elementRef: TipoDovuto, thisRef: TipoEntiAnagraficaComponent) {
    return ManageMode.Ente && !elementRef.flgAttivo;
  }

  activateTipoDovuto(elementRef: TipoDovuto, thisRef: TipoEntiAnagraficaComponent) {
    const msg = `Confermi di voler attivare il tipo dovuto "${elementRef.deTipo}" per l'ente "${elementRef.deNomeEnte}"?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminEntiService.activateTipoDovuto(elementRef.mygovEnteTipoDovutoId).subscribe( num => {
          thisRef.toastrService.success('Tipo dovuto abilitato correttamente.');
          elementRef.flgAttivo = true;
          elementRef.statoAbilitazione = 'Abilitato';
          WithActions.reset(elementRef);
          thisRef.overlaySpinnerService.detach(spinner);
        }, manageError('Errore effettuando l\'aggiornamento', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
      });
  }

  deactivateTipoDovutoEnabled(elementRef: TipoDovuto, thisRef: TipoEntiAnagraficaComponent) {
    return elementRef.flgAttivo;
  }

  deactivateTipoDovuto(elementRef: TipoDovuto, thisRef: TipoEntiAnagraficaComponent) {
    const msg = `Attenzione: prima di disattivare un dovuto è necessario verificare se esistono posizioni aperte, 
    in quanto tali posizioni non saranno più pagabili. Se il dovuto non ha posizioni aperte, allora è possibile disattivarlo. 
    Confermi di voler disattivare il tipo dovuto "${elementRef.deTipo}" per l'ente "${elementRef.deNomeEnte}"?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminEntiService.deactivateTipoDovuto(elementRef.mygovEnteTipoDovutoId).subscribe( num => {
          thisRef.toastrService.success('Tipo dovuto disabilitato correttamente.');
          elementRef.flgAttivo = false;
          elementRef.statoAbilitazione = 'Disabilitato';
          WithActions.reset(elementRef);
          thisRef.overlaySpinnerService.detach(spinner);
        }, manageError('Errore effettuando l\'aggiornamento', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
  });
  }

  gotoDelete(elementRef: TipoDovuto, thisRef: TipoEntiAnagraficaComponent) {
    const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
    thisRef.adminEntiService.deleteTipoDovuto(elementRef.mygovEnteTipoDovutoId).subscribe( num => {
      thisRef.toastrService.success('Tipo dovuto cancellato correttamente.');
      thisRef.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la cancellazione', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
  }

  onInsert() {
    let enteId = Number(this.route.snapshot.params['enteId']);
    this.router.navigate(['admin/enti/tipo/anagrafica', enteId]);
  }

}
