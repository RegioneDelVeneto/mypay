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
    DateValidators, Ente, manageError, OverlaySpinnerService, TipoDovuto, validateFormFun
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable, Subscription } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

import { EnteService } from '../../services/ente.service';
import { FlussoService } from '../../services/flusso.service';

@Component({
  selector: 'app-flussi-export-dialog',
  templateUrl: './flussi-export-dialog.component.html',
  styleUrls: ['./flussi-export-dialog.component.scss']
})
export class FlussiExportDialogComponent implements OnInit, OnDestroy {

  @ViewChild('iForm') insertFormDirective;

  versioniTracciato: string[] = ['1.0', '1.1', '1.2'];

  tipoDovutoOptionsMap: Map<String, TipoDovuto[]>;
  tipoDovutoOptions: TipoDovuto[];
  tipoDovutoFilteredOptions: Observable<TipoDovuto[]>;

  blockingError: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private enteService: EnteService,
    private flussoService: FlussoService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private dialogRef: MatDialogRef<FlussiExportDialogComponent>,
  ) {
    this.insertForm = this.formBuilder.group({
      dateFrom: [DateTime.now().startOf('day').minus({days:7}), [Validators.required]],
      dateTo: [DateTime.now().startOf('day'), [Validators.required]],
      tipoDovuto: [null, [this.tipoDovutoValidator]],
      versioneTracciato: [this.versioniTracciato[this.versioniTracciato.length-1], [Validators.required]]
    }, { validators: DateValidators.dateRange('dateFrom','dateTo') });

    this.valueChangedSub = this.insertForm.valueChanges.subscribe(validateFormFun(this.insertForm, this.insertFormErrors));
  }

  ngOnInit(): void {
    this.tipoDovutoOptionsMap = new Map();
    this.enteService.getCurrentEnteObs().subscribe(value => this.onChangeEnte(this, value) );
    this.onChangeEnte(this, this.enteService.getCurrentEnte());
  }

  ngOnDestroy(): void {
    this.valueChangedSub?.unsubscribe();
  }

  private onChangeEnte(thisRef: FlussiExportDialogComponent, ente:Ente){
    if(ente && ente.mygovEnteId){
      //retrieve list of tipoDovuto and prepare autocomplete
      this.insertForm.controls['tipoDovuto'].setValue(null);
      if(!this.tipoDovutoOptionsMap.has(ente.codIpaEnte)){
        this.enteService.getListTipoDovutoByEnteAsOperatore(ente).subscribe(tipiDovuto => {
          this.tipoDovutoOptionsMap.set(ente.codIpaEnte, tipiDovuto);
          this.tipoDovutoOptions = this.tipoDovutoOptionsMap.get(ente.codIpaEnte);
          this.tipoDovutoFilteredOptions = this.insertForm.get('tipoDovuto').valueChanges
          .pipe(
            startWith(''),
            map(value => typeof value === 'string' || !value ? value : value.deTipo),
            map(deTipoDovuto => deTipoDovuto ? this._tipoDovutoFilter(deTipoDovuto) : this.tipoDovutoOptions.slice())
          );
        }, manageError('Errore caricando l\'elenco dei tipi dovuto', this.toastrService, ()=>{this.blockingError=true}) );
      } else {
        this.tipoDovutoOptions = this.tipoDovutoOptionsMap.get(ente.codIpaEnte);
        this.tipoDovutoFilteredOptions = this.insertForm.get('tipoDovuto').valueChanges
        .pipe(
          startWith(''),
          map(value => typeof value === 'string' || !value ? value : value.deTipo),
          map(deTipoDovuto => deTipoDovuto ? this._tipoDovutoFilter(deTipoDovuto) : this.tipoDovutoOptions.slice())
        );
      }
    } else {
      this.tipoDovutoOptions = [];
      this.insertForm.controls['tipoDovuto'].setValue(null);
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

  insertForm: FormGroup;
  insertFormErrors = {};
  private valueChangedSub: Subscription;

  onInsert() {
    const i = this.insertForm.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.flussoService.insertFlussiExport(this.enteService.getCurrentEnte(), i.dateFrom, i.dateTo, i.tipoDovuto, i.versioneTracciato)
      .subscribe(data => {
        this.toastrService.success('Export prenotato correttamente.');
        this.overlaySpinnerService.detach(spinner);
        this.dialogRef.close();
      }, manageError("Errore effettuando l'inserimento", this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  close() {
    this.dialogRef.close();
  }
}
