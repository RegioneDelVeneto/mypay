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
import { ToastrService } from 'ngx-toastr';
import {
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import {
    MypSearchChipsComponent
} from 'projects/mypay4-fe-common/src/lib/components/myp-search-chips/myp-search-chips.component';
import {
    DateValidators, Ente, manageError, OverlaySpinnerService, PaginatorData, SearchFilterDef,
    TableColumn, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable, Subscription } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { faBook } from '@fortawesome/free-solid-svg-icons';

import { Giornale } from '../../../model/giornale';
import { AdminGiornaleService, GiornaleType } from '../../../services/admin-giornale.service';
import { EnteService } from '../../../services/ente.service';

@Component({
  selector: 'app-giornale',
  templateUrl: './giornale.component.html',
  styleUrls: ['./giornale.component.scss']
})
export class GiornaleComponent implements OnInit, OnDestroy, WithTitle {

  @ViewChild('sForm') searchFormDirective;
  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<Giornale>;
  @ViewChild('mypSearchChips') mypSearchChips: MypSearchChipsComponent;

  get titleLabel(){ return "Giornale degli eventi" }
  get titleIcon(){ return faBook }

  type: GiornaleType;

  hasSearched: boolean = false;
  blockingError: boolean = false;
  formDef: {[key:string]:SearchFilterDef};

  form: FormGroup;
  formErrors = {};
  private formChangesSub:Subscription;

  enteOptions: Ente[];
  enteFilteredOptions: Observable<Ente[]>;

  allPsp:string[] = [];
  allEsito:string[] = [];
  allTipoEvento:string[] = [];
  allCategoriaEvento:string[] = [];

  tableColumns: TableColumn[] = [
    new TableColumn('dataOraEvento', 'Data evento', { sortable: (item: Giornale) => item.dataOraEvento?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] } ),
    new TableColumn('idDominio', 'Ente'),
    new TableColumn('iuv', 'IUV'),
    new TableColumn('evento', 'Evento'),
    new TableColumn('idPsp', 'PSP'),
  ];
  tableData: Giornale[];
  paginatorData: PaginatorData;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private enteService: EnteService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private giornaleService: AdminGiornaleService,
  ) {
    const typeString = this.route.snapshot.url.pop()?.path;
    if(typeString !=="pa" && typeString !=="fesp"){
      this.toastrService.error('Tipo giornale errato: '+this.type,'Visualizza giornale',{disableTimeOut: true});
      return;
    } else {
      this.type = typeString;
    }

    this.formDef = [
      new SearchFilterDef('ente', 'Ente', '', [this.enteValidator], v => v?.deNomeEnte),
      new SearchFilterDef('iuv', 'IUV', '', []),
      new SearchFilterDef('tipoEvento','Evento','', []),
      new SearchFilterDef('categoriaEvento','Categoria','', []),
      new SearchFilterDef('idPsp','PSP','', []),
      new SearchFilterDef('esito','Esito','', []),
      new SearchFilterDef('dateFrom', 'Data da', DateTime.now().startOf('day').minus({days:7}), [Validators.required], v => v?.toFormat('dd/MM/yyyy')),
      new SearchFilterDef('dateTo', 'Data a', DateTime.now().startOf('day'), [Validators.required], v => v?.toFormat('dd/MM/yyyy')),
    ].reduce((formObj, elem) => {formObj[elem.field] = elem; return formObj}, {} );

    const formObj = _.mapValues(this.formDef, x => [x.value, x.validators]);
    //formDef.forEach(elem => formObj[elem.field] = [elem.value, elem.validators]);

    this.form = this.formBuilder.group(formObj, { validators: DateValidators.dateRange('dateFrom','dateTo') });

    this.formChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));
   }

  ngOnInit(): void {
    this.enteService.getAllEnti().subscribe( allEnti => {
      this.enteOptions = allEnti;
      this.enteFilteredOptions = this.form.get('ente').valueChanges
      .pipe(
        startWith(''),
        map(value => typeof value === 'string' || !value ? value : value.deNomeEnte),
        map(deNomeEnte => deNomeEnte ? this._enteFilter(deNomeEnte) : this.enteOptions.slice())
      );
    });

    this.giornaleService.getAllPsp(this.type).subscribe( allPsp => this.allPsp = allPsp);

    this.giornaleService.getAllOpzioni(this.type).subscribe( allOpzioni => {
      this.allCategoriaEvento = allOpzioni.categorieEvento;
      this.allTipoEvento = allOpzioni.tipiEvento;
      this.allEsito = allOpzioni.esitiEvento;
    });
  }

  ngOnDestroy():void {
    this.formChangesSub?.unsubscribe();
  }

  enteDisplayFn(ente: Ente): string {
    return ente ? ente.deNomeEnte : '';
  }

  private _enteFilter(name: string): Ente[] {
    const filterValue = name.toLowerCase();
    return this.enteOptions.filter(option => option.deNomeEnte.toLowerCase().indexOf(filterValue) !== -1);
  }

  private enteValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteId != null ) ? null : {'invalid': true};
  };

  onSubmit(){
    const i = this.form.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);

    this.giornaleService.searchGiornale(
      this.type,
      i.ente?.codiceFiscaleEnte,
      i.iuv,
      i.tipoEvento,
      i.categoriaEvento,
      i.idPsp,
      i.esito,
      i.dateFrom,
      i.dateTo)
    .subscribe(data => {
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

  onRemoveFilter(thisRef: GiornaleComponent) {
    //redo the search
    if(thisRef.hasSearched)
      thisRef.onSubmit();
  }

  onClickRow(element:Giornale, thisRef: GiornaleComponent): Observable<void> {
    return thisRef.giornaleService.getDetailGiornale(thisRef.type, element.mygovGiornaleId)
      .pipe( map(giornale => {
        element.details = [
          {key:'Tipo evento', value:giornale.tipo},
          {key:'Sottotipo evento', value:giornale.sottotipo},
          {key:'Categoria', value:giornale.categoria},
          {key:'Esito', value:giornale.esito},
          {key:'Contesto pagamento', value:giornale.contestoPagamento},
          {key:'Tipo versamento', value:giornale.tipoVersamento},
          {key:'Componente', value:giornale.componente},
          {key:'Identificativo fruitore', value:giornale.idFruitore},
          {key:'Identificativo erogatore', value:giornale.idErogatore},
          {key:'Stazione intermediario PA', value:giornale.idStazione},
          {key:'Canale pagamento', value:giornale.canalePagamento},
          {key:'Parametri specifici interfaccia', value: giornale.parametriInterfaccia?.trim(), options:{entireRow:true, preformatted: true}},
        ];
      }) );
  }

}
