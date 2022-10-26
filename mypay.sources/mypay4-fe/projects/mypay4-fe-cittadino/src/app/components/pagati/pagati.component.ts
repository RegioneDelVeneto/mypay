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
    ApiInvokerService, DateValidators, Ente, manageError, OverlaySpinnerService, TableAction,
    TableColumn, TipoDovuto, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable, Subscription } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

import { CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { faCreditCard, faReceipt } from '@fortawesome/free-solid-svg-icons';

import { Pagato } from '../../model/pagato';
import { EnteService } from '../../services/ente.service';
import { PagatoService } from '../../services/pagato.service';

@Component({
  selector: 'app-pagati',
  templateUrl: './pagati.component.html',
  styleUrls: ['./pagati.component.scss']
})
export class PagatiComponent implements OnInit, AfterViewInit, OnDestroy, WithTitle {

  get titleLabel(){ return "Storico transazioni" }
  get titleIcon(){ return faCreditCard }

  iconReceipt = faReceipt;

  @ViewChild('sForm') searchFormDirective;

  enteOptions: Ente[];
  enteFilteredOptions: Observable<Ente[]>;

  tipoDovutoOptionsMap: Map<String, TipoDovuto[]>;
  tipoDovutoOptions: TipoDovuto[];
  tipoDovutoFilteredOptions: Observable<TipoDovuto[]>;
  previousEnte: Ente;

  hasSearched: boolean = false;
  blockingError: boolean = false;

  form: FormGroup;
  formErrors = {};
  private formChangesSub:Subscription;
  private enteChangesSub: Subscription;

  tableColumns: TableColumn[] = [
    new TableColumn('enteDeNome','Beneficiario'),
    new TableColumn('deTipoDovuto','Tipo dovuto'),
    new TableColumn('causale','Causale'),
    new TableColumn('importo','Importo', { pipe: CurrencyPipe, pipeArgs:['EUR', 'symbol'] } ),
    new TableColumn('dataPagamento','Data pagamento', { sortable: (item: Pagato) => item.dataPagamento?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy'] } ),
    new TableColumn('statoComplessivo','Stato', { pipe: TitleCasePipe } ),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
        new TableAction(this.iconReceipt, this.downloadRt, this.downloadRtEnabled, 'Scarica RT')
      ] } ) ];
  tableData: Pagato[];

  private enteValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteId != null ) ? null : {'invalid': true};
  };

  private tipoDovutoValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteTipoDovutoId != null ) ? null : {'invalid': true};
  };


  constructor(
    private formBuilder: FormBuilder,
    private pagatoService: PagatoService,
    private enteService: EnteService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private fileSaverService: FileSaverService) {
    this.form = this.formBuilder.group({
      ente: ['', [this.enteValidator]],
      tipoDovuto: ['', [this.tipoDovutoValidator]],
      dateFrom: [DateTime.now().startOf('day').minus({years: 2}), [Validators.required]],
      dateTo: [DateTime.now().startOf('day'), [Validators.required]],
      causale: ['']
    },{validators: DateValidators.dateRangeForRangePicker('dateFrom','dateTo')});
  }

  get placeholderTipoDovuto() {
    return this.form.get('ente').value?null:"Selezionare un ente";
  }
  placeholderEnte:string;

  ngOnInit(): void {
    //load enti from backend
    this.form.get('ente').disable();
    this.placeholderEnte = "Attendere - Caricamento elenco enti in corso..";
    this.enteService.getAllEnti().subscribe(enti => {
      this.form.get('ente').enable();
      this.placeholderEnte = null;
      this.enteOptions = enti;
      //init autocomplete feature of ente field
      this.enteFilteredOptions = this.form.get('ente').valueChanges
        .pipe(
          startWith(''),
          map(value => typeof value === 'string' || !value ? value : value.deNomeEnte),
          map(deNomeEnte => deNomeEnte ? this._enteFilter(deNomeEnte) : this.enteOptions.slice())
        );
    }, manageError('Errore caricando l\'elenco degli enti', this.toastrService, ()=>{this.blockingError=true}) );

    this.form.get('tipoDovuto').disable();
    this.tipoDovutoOptionsMap = new Map();
    this.enteChangesSub = this.form.get('ente').valueChanges.subscribe(value => {
      if(value && value.mygovEnteId){
        this.form.get('tipoDovuto').setValue(null);
        this.form.get('tipoDovuto').enable();
        if(!this.tipoDovutoOptionsMap.has(value.codIpaEnte)){
          this.enteService.getListTipoDovutoByEnte(value).subscribe(tipiDovuto => {
            this.tipoDovutoOptionsMap.set(value.codIpaEnte, tipiDovuto);
            this.tipoDovutoOptions = this.tipoDovutoOptionsMap.get(value.codIpaEnte);
            this.tipoDovutoFilteredOptions = this.form.get('tipoDovuto').valueChanges
            .pipe(
              startWith(''),
              map(value => typeof value === 'string' || !value ? value : value.deTipo),
              map(deTipoDovuto => deTipoDovuto ? this._tipoDovutoFilter(deTipoDovuto) : this.tipoDovutoOptions.slice())
            );
          }, manageError('Errore caricando l\'elenco dei tipi dovuto', this.toastrService, ()=>{this.blockingError=true}) );
        } else {
          this.tipoDovutoOptions = this.tipoDovutoOptionsMap.get(value.codIpaEnte);
          this.tipoDovutoFilteredOptions = this.form.get('tipoDovuto').valueChanges
          .pipe(
            startWith(''),
            map(value => typeof value === 'string' || !value ? value : value.deTipo),
            map(deTipoDovuto => deTipoDovuto ? this._tipoDovutoFilter(deTipoDovuto) : this.tipoDovutoOptions.slice())
          );
        }
      } else {
        this.tipoDovutoOptions = [];
        this.form.get('tipoDovuto').setValue(null);
        this.form.get('tipoDovuto').disable();
      }
    });
  }

  ngAfterViewInit(){
    this.formChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));

    //do search at viewLoad (with standard filters)
    this.onSubmit();
  }

  ngOnDestroy():void {
    this.formChangesSub?.unsubscribe();
    this.enteChangesSub?.unsubscribe();
  }

  enteDisplayFn(ente: Ente): string {
    return ente ? ente.deNomeEnte : '';
  }

  private _enteFilter(name: string): Ente[] {
    const filterValue = name.toLowerCase();
    return this.enteOptions.filter(option => option.deNomeEnte.toLowerCase().indexOf(filterValue) !== -1);
  }

  tipoDovutoDisplayFn(tipoDovuto: TipoDovuto): string {
    return tipoDovuto ? tipoDovuto.deTipo : '';
  }

  private _tipoDovutoFilter(name: string): TipoDovuto[] {
    const filterValue = name.toLowerCase();
    return this.tipoDovutoOptions.filter(option => option.deTipo.toLowerCase().indexOf(filterValue) !== -1);
  }

  onSubmit(){
    const i = this.form.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.pagatoService.searchPagati(i.ente, i.dateFrom, i.dateTo, i.causale, i.tipoDovuto)
      .subscribe(data => {
        this.hasSearched = true;
        data.forEach(element => {
          element.details = [
            {key:'Causale', value:element.causale},
            {key:'Data scadenza', value:element.dataScadenza?.toFormat('dd/MM/yyyy')},
            {key:'Data inizio transazione', value:element.dataInizioTransazione?.toFormat('dd/MM/yyyy HH:mm:ss')},
            {key:'Identificativo transazione', value:element.identificativoTransazione},
            {key:'Codice IUV', value:element.codIuv},
            {key:'Intestatario', value:element.intestatario},
            {key:'PSP scelto', value:element.pspScelto},
          ];
        })
        this.tableData = data;
        this.overlaySpinnerService.detach(spinner);
      }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onReset(){
    this.form.reset();
    this.hasSearched = false;
    this.tableData = null;
    //this.searchFormDirective.reset();
  }

  downloadRt(elementRef: Pagato, thisRef: PagatiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.pagatoService.downloadRt(elementRef).subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? 'mypay4_ricevuta_'+elementRef.id+'.pdf';
      const contentType = response.headers.get('content-type') ?? 'application/pdf; charset=utf-8';
      const blob:any = new Blob([response.body], { type: contentType });
      thisRef.fileSaverService.save(blob, fileName);
    }, manageError('Errore scaricando la ricevuta telematica', this.toastrService) );
  }

  downloadRtEnabled(elementRef: Pagato, thisRef: PagatiComponent) {
    return elementRef.stato === 'COMPLETATO';
  }

}
