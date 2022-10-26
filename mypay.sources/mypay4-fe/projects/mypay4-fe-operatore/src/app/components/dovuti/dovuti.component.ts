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
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import {
    MypSearchChipsComponent
} from 'projects/mypay4-fe-common/src/lib/components/myp-search-chips/myp-search-chips.component';
import {
    ApiInvokerService, CodeLabel, DateValidators, Ente, manageError, OverlaySpinnerService,
    PageStateService, PaginatorData, PATTERNS, SearchFilterDef, TableAction, TableColumn,
    TipoDovuto, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable, of, Subscription } from 'rxjs';
import { first, map, startWith } from 'rxjs/operators';

import { animate, state, style, transition, trigger } from '@angular/animations';
import { CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { faClone, faEdit, faReceipt, faSearch, faTrash } from '@fortawesome/free-solid-svg-icons';

import { Dovuto } from '../../model/dovuto';
import { Flusso } from '../../model/flusso';
import { DovutoService } from '../../services/dovuto.service';
import { EnteService } from '../../services/ente.service';
import { FlussoService } from '../../services/flusso.service';

@Component({
  selector: 'app-dovuti',
  templateUrl: './dovuti.component.html',
  styleUrls: ['./dovuti.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class DovutiComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return "Gestione dovuti" }
  get titleIcon(){ return faClone }

  iconEdit = faEdit;
  iconReceipt = faReceipt;
  inconSearch = faSearch;
  iconTrash = faTrash;

  @ViewChild('sForm') searchFormDirective;
  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<Dovuto>;
  @ViewChild('mypSearchChips') mypSearchChips: MypSearchChipsComponent;

  tipoDovutoOptionsMap: Map<String, TipoDovuto[]>;
  tipoDovutoOptions: TipoDovuto[];
  tipoDovutoFilteredOptions: Observable<TipoDovuto[]>;

  flussoOptionsMap: Map<String, Flusso[]>;
  flussoOptions: Flusso[];
  flussoFilteredOptions: Observable<Flusso[]>;

  private allStatesMap = new Map<string, CodeLabel[]>();
  states: CodeLabel[];
  cfAnonimoChecked: boolean = false;

  hasSearched: boolean = false;
  blockingError: boolean = false;

  searchType: string = 'searchTypeDebiti';
  lastSearchType: string;

  allSearchTypes: CodeLabel[];

  tableDatailColumnsName = ['key','value'];
  expandedElement: Dovuto | null;

  private enteChangesSub: Subscription;
  private searchTypeChangesSub: Subscription;

  formDef: {[key:string]:SearchFilterDef};

  constructor(
    private formBuilder: FormBuilder,
    private enteService: EnteService,
    private flussoService: FlussoService,
    private dovutoService: DovutoService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private fileSaverService: FileSaverService,
    private router: Router,
    private pageStateService: PageStateService,
    private dialog: MatDialog,
  ) {

    this.allSearchTypes = [
      new CodeLabel('searchTypeDebiti', 'Online'),
      new CodeLabel('searchTypePagati', 'Nell\'archivio'),
    ];

    this.allStatesMap.set('searchTypeDebiti',[
      new CodeLabel('tutti', 'Tutti'),
      new CodeLabel('daPagare', 'Da pagare'),
      new CodeLabel('pagamentoIniziato', 'Pagamento iniziato'),
      new CodeLabel('scaduto', 'Scaduto')
    ]);
    this.allStatesMap.set('searchTypePagati',[
      new CodeLabel('tutti', 'Tutti'),
      new CodeLabel('annullato', 'Annullato'),
      new CodeLabel('pagato', 'Pagato'),
      new CodeLabel('nonPagato', 'Non pagato'),
      new CodeLabel('abortito', 'Transazione non completata'),
      new CodeLabel('scaduto', 'Scaduto')
    ]);

    this.formDef = [
      new SearchFilterDef('searchType', 'Tipo ricerca', 'searchTypeDebiti', [Validators.required], v => this.allSearchTypes.find(c => c.code===v).label),
      new SearchFilterDef('iuv', 'IUV', '', []),
      new SearchFilterDef('causale','Causale','', []),
      new SearchFilterDef('dateFrom', 'Data scadenza da', DateTime.now().startOf('day'), [Validators.required], v => v?.toFormat('dd/MM/yyyy')),
      new SearchFilterDef('dateTo', 'Data scadenza a', DateTime.now().startOf('day').plus({month:1}), [Validators.required], v => v?.toFormat('dd/MM/yyyy')),
      new SearchFilterDef('state', 'Stato pagamento', 'tutti', [Validators.required], (v,av) => this.allStatesMap.get(av.searchType).find(c => c.code===v).label, true),
      new SearchFilterDef('codFiscale', 'Codice fiscale / partita IVA', '', [Validators.pattern(PATTERNS.codiceFiscaleOPartitaIva)]),
      new SearchFilterDef('flusso', 'Nome flusso', '', [this.flussoValidator], v => v?.nome),
      new SearchFilterDef('tipoDovuto', 'Tipo dovuto', '', [this.tipoDovutoValidator], v => v?.deTipo),
      new SearchFilterDef('iud', 'IUD', '', []),

    ].reduce((formObj, elem) => {formObj[elem.field] = elem; return formObj}, {} );

    const formObj = _.mapValues(this.formDef, x => [x.value, x.validators]);
    //formDef.forEach(elem => formObj[elem.field] = [elem.value, elem.validators]);
    formObj['cfAnonimo']=[false];

    this.form = this.formBuilder.group(formObj, { validators: DateValidators.dateRange('dateFrom','dateTo') });

    this.formChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));
  }

  ngOnInit(): void {

    this.tipoDovutoOptionsMap = new Map();
    this.flussoOptionsMap = new Map();
    this.enteChangesSub = this.enteService.getCurrentEnteObs().subscribe(value => this.onChangeEnte(this, value) );

    //set correct state options based on searchType
    this.states = this.allStatesMap.get(this.form.get('searchType').value);
    this.searchTypeChangesSub = this.form.get('searchType').valueChanges.subscribe(searchType => {
      this.states = this.allStatesMap.get(searchType);
      this.searchType = searchType;
      //reset state when changing value of search type
      this.form.get('state').setValue('tutti');
    });

    //retrieve page state data if navigating back
    if(this.pageStateService.isNavigatingBack()){
      const pageState = this.pageStateService.restoreState();
      if(pageState){
        this.form.setValue(pageState.formData);
        setTimeout(()=>{
          if(pageState.reloadData){
            this.onSubmit();
          } else {
            this.tableData = pageState.tableData;
            this.paginatorData = pageState.paginatorData;
          }
        });
      }
    }
  }

  ngOnDestroy():void {
    this.formChangesSub?.unsubscribe();
    this.enteChangesSub?.unsubscribe();
    this.searchTypeChangesSub?.unsubscribe();
  }




  private onChangeEnte(thisRef: DovutiComponent, ente:Ente){
    if(ente && ente.mygovEnteId){
      //retrieve list of tipoDovuto and prepare autocomplete
      this.form.controls['tipoDovuto'].setValue(null);
      if(!this.tipoDovutoOptionsMap.has(ente.codIpaEnte)){
        this.enteService.getListTipoDovutoByEnteAsOperatore(ente).subscribe(tipiDovuto => {
          this.tipoDovutoOptionsMap.set(ente.codIpaEnte, tipiDovuto);
          this.tipoDovutoOptions = this.tipoDovutoOptionsMap.get(ente.codIpaEnte);
          this.tipoDovutoFilteredOptions = this.form.get('tipoDovuto').valueChanges
          .pipe(
            startWith(''),
            map(value => typeof value === 'string' || !value ? value : value.deTipo),
            map(deTipoDovuto => deTipoDovuto ? this._tipoDovutoFilter(deTipoDovuto) : this.tipoDovutoOptions.slice())
          );
        }, manageError('Errore caricando l\'elenco dei tipi dovuto', this.toastrService, ()=>{this.blockingError=true}) );
      } else {
        this.tipoDovutoOptions = this.tipoDovutoOptionsMap.get(ente.codIpaEnte);
        this.tipoDovutoFilteredOptions = this.form.get('tipoDovuto').valueChanges
        .pipe(
          startWith(''),
          map(value => typeof value === 'string' || !value ? value : value.deTipo),
          map(deTipoDovuto => deTipoDovuto ? this._tipoDovutoFilter(deTipoDovuto) : this.tipoDovutoOptions.slice())
        );
      }
      //retrieve list of flusso and prepare autocomplete
      this.form.controls['flusso'].setValue(null);
      if(!this.flussoOptionsMap.has(ente.codIpaEnte)){
        this.flussoService.getFlussiByEnte(ente).subscribe(flussi => {
          this.flussoOptionsMap.set(ente.codIpaEnte, flussi);
          this.flussoOptions = this.flussoOptionsMap.get(ente.codIpaEnte);
          this.flussoFilteredOptions = this.form.get('flusso').valueChanges
          .pipe(
            startWith(''),
            map(value => typeof value === 'string' || !value ? value : value.nome),
            map(nomeFlusso => nomeFlusso ? this._flussoFilter(nomeFlusso) : this.flussoOptions.slice())
          );
        }, manageError('Errore caricando l\'elenco dei flussi', this.toastrService, ()=>{this.blockingError=true}) );
      } else {
        this.flussoOptions = this.flussoOptionsMap.get(ente.codIpaEnte);
        this.flussoFilteredOptions = this.form.get('flusso').valueChanges
        .pipe(
          startWith(''),
          map(value => typeof value === 'string' || !value ? value : value.nome),
          map(nomeFlusso => nomeFlusso ? this._flussoFilter(nomeFlusso) : this.flussoOptions.slice())
        );
      }
    } else {
      this.tipoDovutoOptions = [];
      this.form.controls['tipoDovuto'].setValue(null);
      this.flussoOptions = [];
      this.form.controls['flusso'].setValue(null);
    }
  }

  tipoDovutoDisplayFn(tipoDovuto: TipoDovuto): string {
    return tipoDovuto ? tipoDovuto.deTipo : '';
  }

  private _tipoDovutoFilter(name: string): TipoDovuto[] {
    const filterValue = name.toLowerCase();
    return this.tipoDovutoOptions.filter(option => option.deTipo.toLowerCase().indexOf(filterValue) !== -1);
  }

  private tipoDovutoValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteTipoDovutoId != null ) ? null : {'invalid': true};
  };

  flussoDisplayFn(flusso: Flusso): string {
    return flusso ? flusso.nome : '';
  }

  private _flussoFilter(name: string): Flusso[] {
    const filterValue = name.toLowerCase();
    return this.flussoOptions.filter(option => option.nome.toLowerCase().indexOf(filterValue) !== -1);
  }

  private flussoValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.nome != null ) ? null : {'invalid': true};
  };

  form: FormGroup;
  formErrors = {};
  private formChangesSub:Subscription;

  tableColumns: TableColumn[] = [
    new TableColumn('codFiscale', 'Codice Fiscale/Partita IVA'),
    //new TableColumn('iud', 'IUD'),
    new TableColumn('iuv', 'IUV'),
    new TableColumn('causaleToShow', 'Causale'),
    new TableColumn('importo','Importo', { sortable: (item: Dovuto) => Number(item.importo?.replace(',','.')), pipe: CurrencyPipe, pipeArgs:['EUR', 'symbol'] } ),
    new TableColumn('dataScadenza', 'Data scadenza', { sortable: (item: Dovuto) => item.dataScadenza?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy'] } ),
    new TableColumn('stato', 'Stato', { pipe: TitleCasePipe} ),
    new TableColumn('dataStato', 'Data cambio stato', { sortable: (item: Dovuto) => item.dataStato?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] } ),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(this.inconSearch, this.gotoDetails, this.gotoDetailsEnabled, 'Visualizza dettaglio'),
      new TableAction(this.iconEdit, this.gotoUpdates, this.gotoUpdatesEnabled, 'Modifica dovuto'),
      new TableAction(this.iconReceipt, this.askRicevuta, this.askRicevutaEnabled, 'Richiedi RT a PagoPA'),
      new TableAction(this.iconReceipt, this.downloadAvviso, this.downloadAvvisoEnabled, 'Scarica avviso'),
      new TableAction(this.iconReceipt, this.downloadRicevuta, this.downloadRicevutaEnabled, 'Scarica ricevuta'),
      new TableAction(this.iconTrash, this.gotoRemove, this.gotoRemoveEnabled, 'Annulla dovuto'),
      ] } ) ];
  tableData: Dovuto[];
  paginatorData: PaginatorData;

  onSubmit(){
    const i = this.form.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);

    this.lastSearchType = this.searchType;
    let searchFun;
    if(this.searchType==='searchTypeDebiti')
      searchFun = this.dovutoService.searchDebitiOperatore(this.enteService.getCurrentEnte(), i.dateFrom, i.dateTo, i.tipoDovuto, i.flusso, i.state, i.causale, i.codFiscale, i.iud, i.iuv)
    else
      searchFun = this.dovutoService.searchPagatiOperatore(this.enteService.getCurrentEnte(), i.dateFrom, i.dateTo, i.tipoDovuto, i.flusso, i.state, i.causale, i.codFiscale, i.iud, i.iuv);
    searchFun.subscribe(data => {
      this.hasSearched = true;
      this.tableData = data;
      //close search panel if data found
      if(data?.length > 0)
        this.mypSearchChips.setSearchPanelState(false);
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onReset(){
    this.form.reset();
    _.forOwn(this.formDef, (value, key) => this.form.get(key).setValue(value.value));
    this.hasSearched = false;
    this.tableData = null;
  }

  onInsert() {
    const navId = this.pageStateService.saveState({
      formData: this.form.value,
      tableData: this.tableData,
      paginatorData: {
        pageSize: this.mypayTableComponent.paginator.pageSize,
        pageIndex: this.mypayTableComponent.paginator.pageIndex
      }
    });
    this.router.navigate(['dovutiDetails', 'insert', ''],{state:{backNavId:navId}});
  }

  onRemoveFilter(thisRef: DovutiComponent) {
    //update cfAnonimo checkbox
    if(!thisRef.form.get('codFiscale').value){
      thisRef.cfAnonimoOnChange(false);
      thisRef.form.get('cfAnonimo').setValue(false);
    }
    //redo the search
    if(thisRef.hasSearched)
      thisRef.onSubmit();
  }

  onClickRow(element:Dovuto, thisRef: DovutiComponent): Observable<void> {
    if(element.details)
      return of(null);
    if(thisRef.lastSearchType==='searchTypeDebiti'){
      return thisRef.dovutoService.getDetailDebitoOperatore(thisRef.enteService.getCurrentEnte(), element.id)
        .pipe(map(dovuto => {
          element.details = [
            {key:'Tipo dovuto', value:dovuto.tipoDovuto?.deTipo},
            {key:'Causale', value:dovuto.causaleToShow},
            {key:'IUD', value:dovuto.iud},
            {key:'Nome flusso', value:dovuto.iuf},
            {key:'Anagrafica', value:dovuto.anagrafica},
            {key:'Tipo soggetto', value:dovuto.tipoSoggetto},
            {key:'Email', value:dovuto.email},
            {key:'Indirizzo', value:dovuto.indirizzo},
            {key:'Civico', value:dovuto.numCiv},
            {key:'CAP', value:dovuto.cap},
            {key:'Nazione', value:dovuto.nazione?.nomeNazione},
            {key:'Provincia', value:dovuto.prov?.provincia},
            {key:'Località', value:dovuto.comune?.comune},
          ];
        }));
    } else {
      element.details = [
        {key:'Tipo dovuto', value:element.tipoDovuto?.deTipo},
        {key:'Causale', value:element.causale},
        {key:'Data scadenza', value:element.dataScadenza?.toFormat('dd/MM/yyyy')},
        {key:'Data inizio transazione', value:element.dataInizioTransazione?.toFormat('dd/MM/yyyy HH:mm:ss')},
        {key:'Identificativo transazione', value:element.identificativoTransazione},
        {key:'Codice IUV', value:element.iuv},
        {key:'Codice IUD', value:element.iud},
        {key:'Nome flusso', value:element.iuf},
        {key:'Intestatario', value:element.intestatario},
        {key:'PSP scelto', value:element.pspScelto},
      ];
      return of(null);
    }

  }

  gotoDetails(elementRef: Dovuto, thisRef: DovutiComponent, eventRef: any){
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.pageStateService.saveState({
      formData: thisRef.form.value,
      tableData: thisRef.tableData,
      paginatorData: {
        pageSize: thisRef.mypayTableComponent.paginator.pageSize,
        pageIndex: thisRef.mypayTableComponent.paginator.pageIndex
      }
    });
    thisRef.router.navigate(['dovutiDetails', 'view', elementRef.id]);
  }

  gotoDetailsEnabled(elementRef: Dovuto, thisRef: DovutiComponent) {
    return elementRef.dovutoType === 'debito';
  }

  gotoUpdates(elementRef: Dovuto, thisRef: DovutiComponent, eventRef: any) {
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
    thisRef.router.navigate(['dovutiDetails', 'edit', elementRef.id],{state:{backNavId:navId}});
  }

  gotoUpdatesEnabled(elementRef: Dovuto, thisRef: DovutiComponent) {
    return elementRef.dovutoType === 'debito' && (elementRef.codStato == 'SCADUTO' || elementRef.codStato == 'INSERIMENTO_DOVUTO');
  }

  downloadAvviso(elementRef: Dovuto, thisRef: DovutiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.dovutoService.downloadAvviso(elementRef).subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? 'mypay4_avviso_'+elementRef.id+'.pdf';
      const contentType = response.headers.get('content-type') ?? 'application/pdf; charset=utf-8';
      const blob:any = new Blob([response.body], { type: contentType });
      thisRef.fileSaverService.save(blob, fileName);
    }, manageError('Errore scaricando l\'avviso di pagamento', thisRef.toastrService) );
  }

  downloadAvvisoEnabled(elementRef: Dovuto, thisRef: DovutiComponent) {
    const iuv = elementRef.iuv || '';
    return elementRef.dovutoType === 'debito' && elementRef.codStato === 'INSERIMENTO_DOVUTO' && iuv !== '';
  }

  downloadRicevuta(elementRef: Dovuto, thisRef: DovutiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.dovutoService.downloadRicevuta(elementRef).subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? 'mypay4_ricevuta_'+elementRef.id+'.pdf';
      const contentType = response.headers.get('content-type') ?? 'application/pdf; charset=utf-8';
      const blob:any = new Blob([response.body], { type: contentType });
      thisRef.fileSaverService.save(blob, fileName);
    }, manageError('Errore scaricando la ricevuta telematica', thisRef.toastrService) );
  }

  downloadRicevutaEnabled(elementRef: Dovuto, thisRef: DovutiComponent) {
    return elementRef.dovutoType === 'pagato' && elementRef.hasRicevuta;
  }

  askRicevuta(elementRef: Dovuto, thisRef: DovutiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
    thisRef.dovutoService.askRicevuta(thisRef.enteService.getCurrentEnte(), elementRef.id).subscribe(response => {
      if(response==='OK')
        thisRef.toastrService.info('RT ricevuta correttamente da PagoPA');
      else
        thisRef.toastrService.warning('RT non disponibile su PagoPA' );
      thisRef.overlaySpinnerService.detach(spinner);
    }, manageError('Errore richiedendo la ricevuta telematica', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
  }

  askRicevutaEnabled(elementRef: Dovuto, thisRef: DovutiComponent) {
    return elementRef.dovutoType === 'debito' && elementRef.codStato === 'PAGAMENTO_INIZIATO';
  }

  gotoRemove(elementRef: Dovuto, thisRef: DovutiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();

    const msg = 'Confermi di voler annullare il dovuto?';
    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.dovutoService.removeDovuto(thisRef.enteService.getCurrentEnte(), elementRef.id).subscribe(response => {
          thisRef.overlaySpinnerService.detach(spinner);
          thisRef.toastrService.info('Dovuto annullato correttamente.' );
          thisRef.tableData = thisRef.tableData.filter(elem => elem.id !== elementRef.id);
        }, manageError('Errore annullando il dovuto', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    });
  }

  gotoRemoveEnabled(elementRef: Dovuto, thisRef: DovutiComponent) {
    return elementRef.dovutoType === 'debito' && (elementRef.codStato === 'INSERIMENTO_DOVUTO' || elementRef.codStato === 'SCADUTO');
  }

  cfAnonimoOnChange(checked: boolean) {
    const codFiscaleField = this.form.get('codFiscale');
    this.cfAnonimoChecked = checked;
    if(checked) {
      codFiscaleField.setValue(Dovuto.CF_ANONIMO);
      codFiscaleField.clearValidators();
    } else {
      codFiscaleField.setValue(null);
      codFiscaleField.setValidators(this.formDef['codFiscale'].validators);
    }
    codFiscaleField.updateValueAndValidity();
  }
}
