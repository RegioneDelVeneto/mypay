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
import { FileSaverService } from 'ngx-filesaver';
import { ToastrService } from 'ngx-toastr';
import {
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import {
    ApiInvokerService, DateValidators, manageError, OverlaySpinnerService,
    PaginatorData, TableAction, TableColumn, validateFormFun
} from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';

import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { faDownload, faListAlt } from '@fortawesome/free-solid-svg-icons';

import { Flusso } from '../../../model/flusso';
import { FlussoTasMas } from '../../../model/flusso-tas-mas';
import { AdminFlussiService } from '../../../services/admin-flussi.service';

@Component({
  selector: 'app-taxonomy',
  templateUrl: './taxonomy.component.html',
  styleUrls: ['./taxonomy.component.scss']
})

export class TaxonomyComponent implements OnInit, OnDestroy {

  get titleLabel(){ return "Gestione tassonomia" }
  get titleIcon(){ return faListAlt }

  iconDownload = faDownload;

  @ViewChild('sForm') searchFormDirective;
  @ViewChild('fileForm', { read: ElementRef }) fileFormElement: ElementRef;
  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<FlussoTasMas>;
  @ViewChild('fileInput') fileInput: ElementRef;

  formData: FormData = null;
  hasSearched: boolean = false;
  blockingError: boolean = false;
  fileLabel: string;
  private valueChangedSub: Subscription;
  disablePayButton: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private adminFlussiService: AdminFlussiService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private fileSaverService: FileSaverService
  ) {

    this.form = this.formBuilder.group({
      nomeTassonomia: [''],
      dateFrom: [DateTime.now().startOf('day').minus({month: 1}), [Validators.required]],
      dateTo: [DateTime.now().startOf('day'), [Validators.required]],
    }, {validators: DateValidators.dateRange('dateFrom','dateTo')});

    this.valueChangedSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));
  }

  ngOnInit(): void {
    this.disablePayButton = false;
    this.onSubmit();
  }

  ngOnDestroy(): void {
    this.valueChangedSub?.unsubscribe();
  }

  flussoDisplayFn(flusso: Flusso): string {
    return flusso ? flusso.nome : '';
  }

  form: FormGroup;
  formErrors = {};

  tableColumns = [
    new TableColumn('id', 'ID Interno'),
    new TableColumn('iuf', 'Nome Tassonomia'),
    new TableColumn('dtCreazione', 'Data Caricamento', { sortable: (item: FlussoTasMas) => item.dtCreazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy'] } ),
    new TableColumn('deNomeOperatore', 'Operatore'),
    new TableColumn('statoToShow', 'Stato'),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(this.iconDownload, this.downloadTassonomia('originale'), this.downloadTassonomiaEnabled('originale'),
                      'Scarica originale',{text: 'O', class: 'badge-download', transform: 'right-7 up-7 shrink-4'}),
      new TableAction(this.iconDownload, this.downloadTassonomia('scartati'), this.downloadTassonomiaEnabled('scartati'),
                      'Scarica scartati',{text: 'S', class: 'badge-download', transform: 'right-7 up-7 shrink-4'}),
      ] } ) ];
  tableData: FlussoTasMas[];
  paginatorData: PaginatorData;

  onSubmit(){
    const i = this.form.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.adminFlussiService.searchTassonomie(i.nomeTassonomia, i.dateFrom, i.dateTo)
      .subscribe(data => {
        this.disablePayButton = false;
        this.hasSearched = true;
        this.tableData = data;
        this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, ()=>{this.overlaySpinnerService.detach(spinner)}) );
  }

  onReset(){
    this.form.reset();
    this.hasSearched = false;
    this.tableData = null;
  }

  downloadTassonomia(type: string){
    return function(element: FlussoTasMas, thisRef: TaxonomyComponent){
      switch(type){
        case 'originale': return thisRef.downloadFromMybox(element.path+'.zip', element.securityToken);
        case 'scartati': return thisRef.downloadFromMybox(element.path+'_SCARTI.csv_SCARTI.zip', element.securityToken);
        default: throw new Error("invalid download type: "+type);
      }
    }
  }

  downloadTassonomiaEnabled(type: string){
    return function(element: FlussoTasMas, thisRef: TaxonomyComponent){
      switch(type){
        case 'originale': return element.showDownload;
        case 'scartati': return element.showDownload && (element.numRigheTotali - element.numRigheElaborateCorrettamente) > 0;
        default: throw new Error("invalid download type: "+type);
      }
    }
  }

  downloadFromMybox(filename: string, securityToken: string) {
    this.adminFlussiService.downloadFlusso('TASSONOMIA_IMPORT', filename, securityToken).subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? filename.replace(/^.*[\\\/]/, '');
      const contentType = response.headers.get('content-type') ?? 'text/json; charset=utf-8';
      const blob:any = new Blob([response.body], { type: contentType });
      this.fileSaverService.save(blob, fileName);
    }, manageError('Errore scaricando il file dei flussi', this.toastrService) );
  }

  checkForUpdate() {
    this.disablePayButton = true;
    let lastUploadHash = null;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    if (this.tableData)
      lastUploadHash = this.tableData.reduce((prev, cur) => (prev?.id > cur.id ? prev : cur), null).hash;
    this.adminFlussiService.performUpdate(lastUploadHash)
      .subscribe(response => {
      if(response==='OK')
        this.toastrService.success('Import effettuato con successo');
      else
        this.toastrService.info('La tassonomia attuale risulta essere aggiornata' );
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, ()=>{this.overlaySpinnerService.detach(spinner)}) );
  }
}
