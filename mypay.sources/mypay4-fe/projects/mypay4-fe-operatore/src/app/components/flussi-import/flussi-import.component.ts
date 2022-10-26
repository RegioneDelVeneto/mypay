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
    ConfirmDialogComponent
} from 'projects/mypay4-fe-common/src/lib/components/confirm-dialog/confirm-dialog.component';
import {
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import {
    ApiInvokerService, DateValidators, Ente, FileSizePipe, manageError, OverlaySpinnerService,
    PaginatorData, TableAction, TableColumn, validateFormFun, WithActions, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';
import { first } from 'rxjs/operators';

import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import {
    faCloudDownloadAlt, faDownload, faEdit, faEllipsisH, faReceipt, faTrash
} from '@fortawesome/free-solid-svg-icons';

import { FlussoImport } from '../../model/flussi-gestione';
import { Flusso } from '../../model/flusso';
import { EnteService } from '../../services/ente.service';
import { FlussoService } from '../../services/flusso.service';

@Component({
  selector: 'app-flussi-import',
  templateUrl: './flussi-import.component.html',
  styleUrls: ['./flussi-import.component.scss'],
})
export class FlussiImportComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return "Importazione flussi" }
  get titleIcon(){ return faCloudDownloadAlt }

  iconEllipsisH = faEllipsisH;
  iconEdit = faEdit;
  iconReceipt = faReceipt;
  iconTrash = faTrash;
  iconDownload = faDownload;

  @ViewChild('sForm') searchFormDirective;
  @ViewChild('fileForm', { read: ElementRef }) fileFormElement: ElementRef;
  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<FlussoImport>;
  @ViewChild('fileInput') fileInput: ElementRef;

  formData: FormData = null;
  hasSearched: boolean = false;
  blockingError: boolean = false;
  fileLabel: string;
  private enteSub: Subscription;
  private valueChangedSub: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private enteService: EnteService,
    private flussoService: FlussoService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private fileSaverService: FileSaverService,
    private fileSizePipe: FileSizePipe,
    private dialog: MatDialog
  ) {

    this.form = this.formBuilder.group({
      nomeFlusso: [''],
      dateFrom: [DateTime.now().startOf('day').minus({month:1}), [Validators.required]],
      dateTo: [DateTime.now().startOf('day'), [Validators.required]],
    }, {validators: DateValidators.dateRange('dateFrom','dateTo')});

    this.valueChangedSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));
  }

  ngOnInit(): void {
    this.enteSub = this.enteService.getCurrentEnteObs().subscribe(value => this.onChangeEnte(this, value) );
    this.onChangeEnte(this, this.enteService.getCurrentEnte());
    if(this.enteService.getCurrentEnte())
      this.onSubmit();
  }

  ngOnDestroy(): void {
    this.enteSub?.unsubscribe();
    this.valueChangedSub?.unsubscribe();
  }

  private onChangeEnte(thisRef: FlussiImportComponent, ente:Ente){
    this.form.controls['nomeFlusso'].setValue(null);
  }

  flussoDisplayFn(flusso: Flusso): string {
    return flusso ? flusso.nome : '';
  }

  form: FormGroup;
  formErrors = {};

  tableColumns = [
    new TableColumn('id', 'ID Interno'),
    new TableColumn('nomeFlusso', 'Nome Flusso'),
    new TableColumn('dataCaricamento', 'Data Caricamento', { sortable: (item: FlussoImport) => item.dataCaricamento?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy'] } ),
    new TableColumn('operatore', 'Operatore'),
    new TableColumn('statoToShow', 'Stato'),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(this.iconDownload, this.downloadFlusso('originale'), this.downloadFlussoEnabled('originale'),
                      'Scarica originale',{text: 'O', class: 'badge-download', transform: 'right-7 up-7 shrink-4'}),
      new TableAction(this.iconDownload, this.downloadFlusso('caricati'), this.downloadFlussoEnabled('caricati'),
                      'Scarica caricati',{text: 'C', class: 'badge-download', transform: 'right-7 up-7 shrink-4'}),
      new TableAction(this.iconDownload, this.downloadFlusso('scartati'), this.downloadFlussoEnabled('scartati'),
                      'Scarica scartati',{text: 'S', class: 'badge-download', transform: 'right-7 up-7 shrink-4'}),
      new TableAction(this.iconDownload, this.downloadFlusso('generati'), this.downloadFlussoEnabled('generati'),
                      'Scarica generati',{text: 'G', class: 'badge-download', transform: 'right-7 up-7 shrink-4'}),
      new TableAction(this.iconReceipt, this.gotoDownloadLog, this.gotoDownloadLogEnabled, 'Scarica log'),
      new TableAction(this.iconTrash, this.gotoRemove, this.gotoRemoveEnabled, 'Annulla'),
      ] } ) ];
  tableData: FlussoImport[];
  paginatorData: PaginatorData;

  onSubmit(){
    const i = this.form.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.flussoService.searchFlussiImport(this.enteService.getCurrentEnte(), i.nomeFlusso, i.dateFrom, i.dateTo)
      .subscribe(data => {
        this.hasSearched = true;
        this.tableData = data;
        this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, ()=>{this.overlaySpinnerService.detach(spinner)}) );
  }

  onReset(){
    this.form.reset();
    this.form.get('dateFrom').setValue(DateTime.now().startOf('day').minus({month:1}));
    this.form.get('dateTo').setValue(DateTime.now().startOf('day'));
    this.hasSearched = false;
    this.tableData = null;
  }

  selectFileOnChange(files: FileList) {
    if (files?.length > 0) {
      this.formData = new FormData();
      this.formData.append("file", files[0]);
      this.formData.append("type", "FLUSSI_IMPORT");
      this.fileLabel = files[0].name + " ["+this.fileSizePipe.transform(files[0].size)+"]";
    } else {
      this.formData = null;
      this.fileLabel = null;
      this.fileFormElement.nativeElement.reset();
    }
  }

  downloadFlusso(type: string){
    return function(element: FlussoImport, thisRef: FlussiImportComponent){
      switch(type){
        case 'originale': return thisRef.downloadFromMybox(element.path+'.zip', element.securityToken);
        case 'caricati': return thisRef.downloadFromMybox(element.path+'_IUV.zip', element.securityToken);
        case 'scartati': return thisRef.downloadFromMybox(element.path+'_SCARTI.zip', element.securityToken);
        case 'generati': return thisRef.downloadFromMybox(element.path+'_AVVISI_PDF.zip', element.securityToken);
        default: throw new Error("invalid download type: "+type);
      }
    }
  }

  downloadFlussoEnabled(type: string){
    return function(element: FlussoImport, thisRef: FlussiImportComponent){
      switch(type){
        case 'originale': return element.showDownload;
        case 'caricati': return element.showDownload && element.numRigheImportateCorrettamente > 0;
        case 'scartati': return element.showDownload && (element.numRigheTotali - element.numRigheImportateCorrettamente) > 0;
        case 'generati': return element.showDownload && element.pdfGenerati > 0;
        default: throw new Error("invalid download type: "+type);
      }
    }
  }

  uploadFlusso() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.flussoService.uploadFlusso(this.enteService.getCurrentEnte(), this.formData)
      .subscribe(() => {
        this.toastrService.success('File caricato correttamente');
        this.selectFileOnChange(null);
        this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando il caricamento del file', this.toastrService, ()=>{this.overlaySpinnerService.detach(spinner)}) );
  }

  downloadFromMybox(filename: string, securityToken: string) {
    this.flussoService.downloadFlusso(this.enteService.getCurrentEnte(), 'FLUSSI_IMPORT', filename, securityToken).subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? filename.replace(/^.*[\\\/]/, '');
      const contentType = response.headers.get('content-type') ?? 'application/octet-stream';
      const blob:any = new Blob([response.body], { type: contentType });
      this.fileSaverService.save(blob, fileName);
    }, manageError('Errore scaricando il file del flusso', this.toastrService) );
  }

  gotoDownloadLog(elementRef: FlussoImport, thisRef: FlussiImportComponent, eventRef: any){
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.flussoService.downloadFlussoLog(thisRef.enteService.getCurrentEnte(), elementRef.log).subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? elementRef.log.replace(/^.*[\\\/]/, '');
      const contentType = response.headers.get('content-type') ?? 'application/octet-stream';
      const blob:any = new Blob([response.body], { type: contentType });
      thisRef.fileSaverService.save(blob, fileName);
    }, manageError('Errore scaricando la ricevuta telematica', thisRef.toastrService) );
  }

  gotoDownloadLogEnabled(elementRef: FlussoImport, thisRef: FlussiImportComponent) {
    return elementRef.log && elementRef.log.length > 0;
  }

  gotoRemove(elementRef: FlussoImport, thisRef: FlussiImportComponent, eventRef: any){
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: 'Confermi di voler annullare il flusso?'}})
    .afterClosed().pipe(first()).subscribe(result => {
      if(result==="true")
        thisRef.flussoService.removeFlusso(thisRef.enteService.getCurrentEnte(), elementRef.id).subscribe(response => {
          thisRef.toastrService.info('Flusso annullato correttamente.');
          elementRef.codStato = 'ANNULLATO';
          elementRef.statoToShow = 'ANNULLATO';
          WithActions.reset(elementRef);
        }, manageError('Errore annullando il flusso', thisRef.toastrService) );
    });
  }

  gotoRemoveEnabled(elementRef: FlussoImport, thisRef: FlussiImportComponent) {
    let stato = elementRef.codStato?.toLowerCase().trim();
    return stato === 'caricato';
  }

}
