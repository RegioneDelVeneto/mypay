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
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { DateTime } from 'luxon';
import { ToastrService } from 'ngx-toastr';
import { DateValidators, manageError, OverlaySpinnerService, TipoDovuto, validateFormFun } from 'projects/mypay4-fe-common/src/public-api';
import { Observable, Subscription } from 'rxjs';
import { EnteService } from '../../services/ente.service';
import { FlussoService } from '../../services/flusso.service';

@Component({
  selector: 'app-flussi-conservazione-dialog',
  templateUrl: './flussi-conservazione-dialog.component.html',
  styleUrls: ['./flussi-conservazione-dialog.component.scss']
})
export class FlussiConservazioneDialogComponent implements OnDestroy {

  @ViewChild('iForm') insertFormDirective;

  versioniTracciato: string[] = ['RT', 'RPT'];

  tipoDovutoOptionsMap: Map<String, TipoDovuto[]>;
  tipoDovutoOptions: TipoDovuto[];
  tipoDovutoFilteredOptions: Observable<TipoDovuto[]>;

  blockingError: boolean = false;

  insertForm: FormGroup;
  insertFormErrors = {};

  private valueChangedSub: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private enteService: EnteService,
    private flussoService: FlussoService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private dialogRef: MatDialogRef<FlussiConservazioneDialogComponent>,
  ) {
    this.insertForm = this.formBuilder.group({
      dateFrom: [DateTime.now().startOf('day').minus({ month: 1 }), [Validators.required]],
      dateTo: [DateTime.now().startOf('day'), [Validators.required]],
      versioneTracciato: [this.versioniTracciato[this.versioniTracciato.length - 2], [Validators.required]]
    }, { validators: [DateValidators.dateRange('dateFrom', 'dateTo'), DateValidators.dateRangeMonth('dateFrom', 'dateTo')] });

    this.valueChangedSub = this.insertForm.valueChanges.subscribe(validateFormFun(this.insertForm, this.insertFormErrors));
  }

  ngOnDestroy(): void {
    this.valueChangedSub?.unsubscribe();
  }

  onInsert() {
    const i = this.insertForm.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.flussoService.insertFlussiExportConservazione(this.enteService.getCurrentEnte(), i.dateFrom, i.dateTo, i.versioneTracciato)
      .subscribe(data => {
        this.toastrService.success('Export prenotato correttamente.');
        this.overlaySpinnerService.detach(spinner);
        this.dialogRef.close();
      }, manageError("Errore effettuando l'inserimento", this.toastrService, () => { this.overlaySpinnerService.detach(spinner) }));
  }

  close() {
    this.dialogRef.close();
  }
}

