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
    ApiInvokerService, DateValidators, FileSizePipe, manageError, OverlaySpinnerService,
    PaginatorData, TableAction, TableColumn, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';

import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { faCloudDownloadAlt, faEdit, faReceipt, faSearch } from '@fortawesome/free-solid-svg-icons';

import { FlussoFile } from '../../model/flussi-gestione';
import { EnteService } from '../../services/ente.service';
import { FlussoService } from '../../services/flusso.service';
import {
    FlussiExportDialogComponent
} from '../flussi-export-dialog/flussi-export-dialog.component';

@Component({
  selector: 'app-flussi-export',
  templateUrl: './flussi-export.component.html',
  styleUrls: ['./flussi-export.component.scss']
})
export class FlussiExportComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return "Flussi RT" }
  get titleIcon(){ return faCloudDownloadAlt }

  iconEdit = faEdit;
  iconReceipt = faReceipt;
  inconSearch = faSearch;

  @ViewChild('sForm') searchFormDirective;
  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<FlussoFile>;

  hasSearched: boolean = false;
  blockingError: boolean = false;

  private valueChangedSub: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private enteService: EnteService,
    private flussoService: FlussoService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private fileSaverService: FileSaverService,
    private dialog: MatDialog,
  ) {

    this.searchForm = this.formBuilder.group({
      nomeFlusso: [''],
      dateFrom: [DateTime.now().startOf('day').minus({week:1}), [Validators.required]],
      dateTo: [DateTime.now().startOf('day'), [Validators.required]],
    }, { validators: DateValidators.dateRange('dateFrom','dateTo') });

    this.valueChangedSub = this.searchForm.valueChanges.subscribe(validateFormFun(this.searchForm, this.searchFormErrors));
  }

  ngOnDestroy(): void {
    this.valueChangedSub?.unsubscribe();
  }

  ngOnInit(): void {
    if(this.enteService.getCurrentEnte())
      this.onSearch();
  }

  searchForm: FormGroup;
  searchFormErrors = {};

  tableColumns = [
    new TableColumn('name', 'Nome File'),
    new TableColumn('dataCreazione', 'Data Prenotazione', { sortable: (item: FlussoFile) => item.dataCreazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] } ),
    new TableColumn('dimensione', 'Dimensione File', { sortable: (item: FlussoFile) => item.dimensione?.valueOf(), pipe: FileSizePipe} ),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(this.iconReceipt, this.downloadFromMybox, this.downloadFromMyboxEnabled, 'Scarica')
      ] } ) ];
  tableData: FlussoFile[];
  paginatorData: PaginatorData;

  onSearch(){
    const i = this.searchForm.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.flussoService.searchFlussiExport(this.enteService.getCurrentEnte(), i.nomeFlusso, i.dateFrom, i.dateTo)
      .subscribe(data => {
        this.hasSearched = true;
        this.tableData = data;
        this.overlaySpinnerService.detach(spinner);
      }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onReset(){
    this.searchForm.reset();
    this.searchForm.get('dateFrom').setValue(DateTime.now().startOf('day'));
    this.searchForm.get('dateTo').setValue(DateTime.now().startOf('day').plus({month:1}));
    this.hasSearched = false;
    this.tableData = null;
  }

  openDialog() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = false;
    dialogConfig.autoFocus = true;
    dialogConfig.width = "70%";
    this.dialog.open(FlussiExportDialogComponent, dialogConfig);
  }


  downloadFromMybox(elementRef: FlussoFile, thisRef: FlussiExportComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.flussoService.downloadFlusso(thisRef.enteService.getCurrentEnte(), 'FLUSSI_EXPORT', elementRef.path, elementRef.securityToken)
      .subscribe(response => {
        const contentDisposition = response.headers.get('content-disposition');
        const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? elementRef.path.replace(/^.*[\\\/]/, '');
        const contentType = response.headers.get('content-type') ?? 'application/octet-stream';
        const blob:any = new Blob([response.body], { type: contentType });
        thisRef.fileSaverService.save(blob, fileName);
      }, manageError('Errore scaricando il file del flusso', thisRef.toastrService) );
  }

  downloadFromMyboxEnabled(elementRef: FlussoFile, thisRef: FlussiExportComponent) {
    return elementRef.path ? true : false;
  }

}
