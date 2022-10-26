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
    ApiInvokerService, DateValidators, FileSizePipe, manageError, MyPayBreadcrumbsService,
    OverlaySpinnerService, PaginatorData, TableAction, TableColumn, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';
import { first } from 'rxjs/operators';

import { DatePipe } from '@angular/common';
import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { faEdit, faReceipt, faTrash } from '@fortawesome/free-solid-svg-icons';

import { FlussoFile, TipoFlusso } from '../../model/flussi-gestione';
import { EnteService } from '../../services/ente.service';
import { FlussoService } from '../../services/flusso.service';

@Component({
  selector: 'app-flussi-spc',
  templateUrl: './flussi-spc.component.html',
  styleUrls: ['./flussi-spc.component.scss']
})
export class FlussiSpcComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return this.pageTitle ?? "Flussi di rendicontazione" }
  get titleIcon(){ return faReceipt }


  @Input() tipoFlusso: TipoFlusso;
  pageTitle: string;

  iconEdit = faEdit;
  iconReceipt = faReceipt;
  iconTrash = faTrash;

  @ViewChild('sForm') searchFormDirective;
  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<FlussoFile>;

  hasSearched: boolean = false;
  blockingError: boolean = false;

  private valueChangedSub: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private enteService: EnteService,
    private flussoService: FlussoService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private fileSaverService: FileSaverService,
    private myPayBreadcrumbsService: MyPayBreadcrumbsService,
  ) {
    this.form = this.formBuilder.group({
      dateFrom: [DateTime.now().startOf('day').minus({month:1}), [Validators.required]],
      dateTo: [DateTime.now().startOf('day'), [Validators.required]],
      flgProdOrDisp: ['P']
    }, { validators: DateValidators.dateRange('dateFrom','dateTo') } );

    this.valueChangedSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));
  }

  ngOnDestroy(): void {
    this.valueChangedSub?.unsubscribe();
  }

  ngOnInit(): void {
    /** Set tipoFlusso for the case that this component is called by flussi-cards */
    this.route.data.pipe(first()).subscribe(params => {
      let tipoFlusso =  params['tipoFlusso'];
      this.tipoFlusso = tipoFlusso == 'quadratura' ? TipoFlusso.QUADRATURA : TipoFlusso.RENDICONTAZIONE;
      if( this.tipoFlusso === TipoFlusso.QUADRATURA ){
        this.pageTitle = 'Flussi di quadratura';
        this.myPayBreadcrumbsService.updateCurrentBreadcrumb(this.pageTitle);
      } else {
        this.pageTitle = null;
        this.myPayBreadcrumbsService.resetCurrentBreadcrumb();
      }
      if(this.enteService.getCurrentEnte())
        this.onSubmit();
    });
  }


  form: FormGroup;
  formErrors = {};

  tableColumns = [
    new TableColumn('identificativo', 'Identificativo'),
    new TableColumn('name', 'Nome File'),
    new TableColumn('dataProduzione', 'Data Produzione', { sortable: (item: FlussoFile) => item.dataProduzione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] } ),
    new TableColumn('dataCreazione', 'Data Disponibilità', { sortable: (item: FlussoFile) => item.dataCreazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy'] } ),
    new TableColumn('dimensione', 'Dimensione File', { sortable: (item: FlussoFile) => item.dimensione?.valueOf(), pipe: FileSizePipe} ),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(this.iconReceipt, this.downloadFromMybox, ()=>true, 'Scarica')
      ] } ) ];
  tableData: FlussoFile[];
  paginatorData: PaginatorData;

  onReset(){
    this.form.reset();
    this.form.get('dateFrom').setValue(DateTime.now().startOf('day').minus({month:1}));
    this.form.get('dateTo').setValue(DateTime.now().startOf('day'));
    this.hasSearched = false;
    this.tableData = null;
  }

  onSubmit(){
    const i = this.form.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.flussoService.searchFlussiSPC(this.enteService.getCurrentEnte(), this.tipoFlusso, i.dateFrom, i.dateTo, i.flgProdOrDisp)
      .subscribe(data => {
        this.hasSearched = true;
        this.tableData = data;
        this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, ()=>{this.overlaySpinnerService.detach(spinner);}) );
  }

  downloadFromMybox(elementRef: FlussoFile, thisRef: FlussiSpcComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    const tipoFlussoDownload = thisRef.tipoFlusso===TipoFlusso.RENDICONTAZIONE ? 'FLUSSI_RENDIC' : 'FLUSSI_QUADR';
    thisRef.flussoService.downloadFlusso(thisRef.enteService.getCurrentEnte(), tipoFlussoDownload, elementRef.path, elementRef.securityToken).subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? elementRef.path.replace(/^.*[\\\/]/, '');
      const contentType = response.headers.get('content-type') ?? 'application/octet-stream';
      const blob:any = new Blob([response.body], { type: contentType });
      thisRef.fileSaverService.save(blob, fileName);
    }, manageError('Errore scaricando il file', thisRef.toastrService) );
  }


}
