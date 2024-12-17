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
import * as _ from 'lodash';
import { DateTime } from 'luxon';
import { FileSaverService } from 'ngx-filesaver';
import { ToastrService } from 'ngx-toastr';
import {
    ConfirmDialogComponent
} from 'projects/mypay4-fe-common/src/lib/components/confirm-dialog/confirm-dialog.component';
import {
    ApiInvokerService, DateValidators, Ente, manageError, OverlaySpinnerService, TableAction,
    TableColumn, TipoDovuto, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable, Subscription } from 'rxjs';
import { first, map, startWith } from 'rxjs/operators';

import { animate, state, style, transition, trigger } from '@angular/animations';
import { CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import {
    faCartPlus, faCashRegister, faPrint, faReceipt, faTrash
} from '@fortawesome/free-solid-svg-icons';

import { Debito } from '../../model/debito';
import { AvvisoService } from '../../services/avviso.service';
import { CarrelloService } from '../../services/carrello.service';
import { DebitoService } from '../../services/debito.service';
import { EnteService } from '../../services/ente.service';

@Component({
  selector: 'app-debiti',
  templateUrl: './debiti.component.html',
  styleUrls: ['./debiti.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class DebitiComponent implements OnInit, AfterViewInit, OnDestroy, WithTitle {

  get titleLabel(){ return "Posizioni aperte" }
  get titleIcon(){ return faCashRegister }

  iconPrint = faPrint;
  iconAddCart = faCartPlus;
  iconRemoveCart = faTrash;
  iconReceipt = faReceipt;
  iconCashRegister = faCashRegister;
  iconTrash = faTrash;

  @ViewChild('sForm') searchFormDirective;

  enteOptions: Ente[];
  enteFilteredOptions: Observable<Ente[]>;

  tipoDovutoOptionsMap: Map<String, TipoDovuto[]>;
  tipoDovutoOptions: TipoDovuto[];
  tipoDovutoFilteredOptions: Observable<TipoDovuto[]>;
  previousEnte: Ente;

  hasSearched: boolean = false;
  blockingError: boolean = false;
  isWating: boolean = false;

  private formChangesSub: Subscription;
  private enteChangesSub: Subscription;

  form: FormGroup;
  formErrors = {};

  tableColumns: TableColumn[] = [
    new TableColumn('deEnte', 'Beneficiario'),
    new TableColumn('deTipoDovuto', 'Tipo dovuto'),
    new TableColumn('causale', 'Causale del versamento'),
    new TableColumn('importo','Importo', { pipe: CurrencyPipe, pipeArgs:['EUR', 'symbol'] } ),
    new TableColumn('dataScadenza', 'Data scadenza', { sortable: (item: Debito) => item.dataScadenza?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy'] } ),
    new TableColumn('deStato','Stato', { pipe: TitleCasePipe } ),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(this.iconReceipt, this.downloadAvviso, this.downloadAvvisoEnabled, 'Scarica avviso'),
      new TableAction(this.iconAddCart, this.addToCarrello, this.addToCarrelloEnabled, 'Aggiungi al carrello'),
      new TableAction(this.iconRemoveCart, this.removeFromCarrello, this.removeFromCarrelloEnabled, 'Rimuovi dal carrello'),
      new TableAction(this.iconTrash, this.gotoRemove, this.gotoRemoveEnabled, 'Annulla dovuto'),
      ] } ) ];
  tableData: Debito[];
  detailFilterExclude = ['dataScadenza'];
  //rowStyleFun = (elem:Debito) => (elem?.deStato === 'PAGAMENTO INIZIATO' ? 'background-color: lightyellow' : null);


  private enteValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteId != null ) ? null : {'invalid': true};
  };

  private tipoDovutoValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteTipoDovutoId != null ) ? null : {'invalid': true};
  };


  constructor(
    private formBuilder: FormBuilder,
    private debitoService: DebitoService,
    private avvisoService: AvvisoService,
    private carrelloService: CarrelloService,
    private enteService: EnteService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private fileSaverService: FileSaverService,
    private dialog: MatDialog,
  ) {
    this.form = this.formBuilder.group({
      ente: ['', [this.enteValidator]],
      tipoDovuto: ['', [this.tipoDovutoValidator]],
      dateFrom: [DateTime.now().startOf('day'), [Validators.required]],
      dateTo: [DateTime.now().startOf('day').plus({month:1}), [Validators.required]],
      causale: ['']
    },{validators: DateValidators.dateRange('dateFrom','dateTo')});

    this.formChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));
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
        this.form.get('tipoDovuto').enable();
        this.form.get('tipoDovuto').setValue(null);
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
        this.form.get('tipoDovuto').disable();
        this.form.get('tipoDovuto').setValue(null);
      }
    });
  }

  ngAfterViewInit():void {
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
    this.debitoService.searchDebiti(i.ente, i.dateFrom, i.dateTo, i.causale, i.tipoDovuto)
      .subscribe(data => {
        this.hasSearched = true;
        data.forEach(element => {
          Debito.setDetails(element);

          /*
          if(element.entePrimarioDetail != null)
            Debito.setDetailEntePrimario(element);

          if(element.dovutoMultibeneficiario != null) // if dovutoMultibeneficiario is not null, i set dovuto multubeneficiario detail
            Debito.setDetailMB(element);
            */

          this.carrelloService.updateState(element); //magage state "inserito nel carrello"
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

  downloadAvviso(elementRef: Debito, thisRef: DebitiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.avvisoService.downloadAvviso(elementRef).subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? 'mypay4_avviso_'+elementRef.id+'.pdf';
      const contentType = response.headers.get('content-type') ?? 'application/pdf; charset=utf-8';
      const blob:any = new Blob([response.body], { type: contentType });
      thisRef.fileSaverService.save(blob, fileName);
		}, manageError('Errore scaricando l\'avviso di pagamento', thisRef.toastrService) );
  }

  downloadAvvisoEnabled(elementRef: Debito, thisRef: DebitiComponent) {
    return elementRef.codStato === 'INSERIMENTO_DOVUTO' && !_.isNil(elementRef.codIuv);
  }

  addToCarrello(elementRef: Debito, thisRef: DebitiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();

    const addError = thisRef.carrelloService.add(elementRef);
    if(addError)
      thisRef.toastrService.error(addError,'Errore aggiungendo al carrello',{disableTimeOut: true});
    else {
      thisRef.toastrService.info('Elemento aggiunto al carrello');
      if (elementRef.multibeneficiario)
        thisRef.toastrService.info('Si fa presente che non è possibile aggiungere altri elementi al carrello.', null, {disableTimeOut: true});
    }
  }

  addToCarrelloEnabled(elementRef: Debito, thisRef: DebitiComponent){
    return thisRef.carrelloService.canAdd(elementRef);
  }

  removeFromCarrello(elementRef: Debito, thisRef: DebitiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    const removeError = thisRef.carrelloService.remove(elementRef);
    if(removeError)
      thisRef.toastrService.error(removeError,'Errore rimuovendo dal carrello',{disableTimeOut: true});
    else
      thisRef.toastrService.info('Elemento rimosso dal carrello');
  }

  removeFromCarrelloEnabled(elementRef: Debito, thisRef: DebitiComponent){
    return thisRef.carrelloService.canRemove(elementRef);
  }

  gotoRemove(elementRef: Debito, thisRef: DebitiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();

    const msg = 'Confermi di voler annullare il dovuto?';
    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.debitoService.removeDovuto(elementRef.id).subscribe(response => {
          thisRef.overlaySpinnerService.detach(spinner);
          thisRef.toastrService.info('Dovuto annullato correttamente.' );
          thisRef.tableData = thisRef.tableData.filter(elem => elem.id !== elementRef.id);
        }, manageError('Errore annullando il dovuto', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    });
  }

  gotoRemoveEnabled(elementRef: Debito, thisRef: DebitiComponent) {
    return elementRef.annullabile;
  }

}
