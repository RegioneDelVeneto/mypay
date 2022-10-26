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
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import {
    DateValidators, manageError, OverlaySpinnerService, PageStateService, PaginatorData,
    TableAction, TableColumn, UserService, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { AnagraficaStato } from 'projects/mypay4-fe-operatore/src/app/model/anagrafica-stato';
import { Ente } from 'projects/mypay4-fe-operatore/src/app/model/ente';
import { EnteService } from 'projects/mypay4-fe-operatore/src/app/services/ente.service';
import { Subscription } from 'rxjs';

import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { faSearch, faUniversity } from '@fortawesome/free-solid-svg-icons';

import { AdminEntiService } from '../../../../services/admin-enti.service';

@Component({
  selector: 'app-ente-list',
  templateUrl: './ente-list.component.html',
  styleUrls: ['./ente-list.component.scss']
})
export class EnteListComponent implements OnInit, OnDestroy, WithTitle {

  @ViewChild('sForm') searchFormDirective;

  get titleLabel(){ return "Gestione enti" }
  get titleIcon(){ return faUniversity }

  form: FormGroup;

  hasSearched: boolean = false;

  anagraficaStati: AnagraficaStato[];

  private formChangesSub: Subscription;
  formErrors = {};
  blockingError: boolean = false;

  private userSub: Subscription;
  isUserAdmin: boolean = false;
  isUserAdminEnte: boolean = false;

  constructor(
    private userService: UserService,
    private formBuilder: FormBuilder,
    private adminEntiService: AdminEntiService,
    private enteService: EnteService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private router: Router,
    private pageStateService: PageStateService,
  ) {
    this.form = this.formBuilder.group({
      codIpaEnte: [''],
      deNomeEnte: [''],
      codFiscale: [''],
      cdStato: [''],
      dtAvvioFrom: [null],
      dtAvvioTo: [null]
    }, { validators: DateValidators.dateRange('dtAvvioFrom','dtAvvioTo') });

    this.userSub = userService.getLoggedUserObs().subscribe(() => {
      this.isUserAdmin = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE);
    });
    this.formChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));
  }

  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<Ente>;
  tableColumns: TableColumn[] = [
    new TableColumn('thumbLogoEnte', null, {type:'img64', ariaLabel:'Logo'}),
    new TableColumn('deNomeEnte', 'Nome'),
    new TableColumn('codIpaEnte', 'Codice IPA'),
    new TableColumn('codiceFiscaleEnte', 'Codice fiscale'),
    new TableColumn('cdStatoEnte.deStato', 'Stato', {sortable: (item:Ente) => item.cdStatoEnte?.deStato}),
    new TableColumn('dtAvvio', 'Data Avvio', { sortable: (item: Ente) => item.dtAvvio?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy'] } ),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(faSearch, this.gotoDetails, null , 'Visualizza dettaglio'),
      ] } ) ];
  tableData: Ente[];
  paginatorData: PaginatorData;

  ngOnInit(): void {
    this.adminEntiService.getAnagraficaStati().subscribe( anagraficaStati => {
      this.anagraficaStati = anagraficaStati;
    }, manageError("Errore recuperando gli stati", this.toastrService, ()=>{this.blockingError=true}) );
    //retrieve page state data if navigating back
    if(this.pageStateService.isNavigatingBack()){
      const pageState = this.pageStateService.restoreState();
      if(pageState){
        this.form.setValue(pageState.formData);
        setTimeout(()=>{
          if(pageState.reloadData){
            this.onSearch();
          } else {
            this.tableData = pageState.tableData;
            this.paginatorData = pageState.paginatorData;
          }
        });
      }
    }
  }

  ngOnDestroy(): void {
    this.formChangesSub?.unsubscribe();
    this.userSub?.unsubscribe();
  }

  onSearch() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    const codIpaEnte = this.form.get('codIpaEnte')?.value;
    const deNome = this.form.get('deNomeEnte')?.value;
    const codFiscale = this.form.get('codFiscale')?.value;
    const idStato = (this.form.get('cdStato')?.value as AnagraficaStato).mygovAnagraficaStatoId;
    const dtAvvioFrom = (this.form.get('dtAvvioFrom')?.value as DateTime)?.toFormat('yyyy/MM/dd');
    const dtAvvioTo = (this.form.get('dtAvvioTo')?.value as DateTime)?.toFormat('yyyy/MM/dd');

    this.adminEntiService.searchEnti(codIpaEnte, deNome, codFiscale, idStato, dtAvvioFrom, dtAvvioTo).subscribe(enti => {
      this.hasSearched = true;
      this.tableData = enti;
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onSearchAll() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);

    this.adminEntiService.searchEnti(null, null, null, null, null, null).subscribe(enti => {
      this.hasSearched = true;
      this.tableData = enti;
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onInsert() {
    this.router.navigate(['admin/enti/anagrafica', 'insert', '']);
  }

  gotoDetails(element: Ente, thisRef: EnteListComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    const navId = thisRef.pageStateService.saveState({
      formData: thisRef.form.value,
      tableData: thisRef.tableData,
      paginatorData: {
        pageSize: thisRef.mypayTableComponent.paginator.pageSize,
        pageIndex: thisRef.mypayTableComponent.paginator.pageIndex
      }
    });
    thisRef.enteService.setCurrentEnte(element);
    thisRef.router.navigate(['admin/enti/details', element.mygovEnteId]);
  }
}
