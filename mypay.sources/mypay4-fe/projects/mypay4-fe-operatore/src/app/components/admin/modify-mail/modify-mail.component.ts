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
import { ToastContainerDirective, ToastrService } from 'ngx-toastr';
import {
    manageError, OverlaySpinnerService, subscribeValidateForm
} from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';

import { Component, ElementRef, Inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { faTimes } from '@fortawesome/free-solid-svg-icons';

import { Operatore } from '../../../model/operatore';
import { AdminUtenteService } from '../../../services/admin-utente.service';

@Component({
  selector: 'app-modify-mail',
  templateUrl: './modify-mail.component.html',
  styleUrls: ['./modify-mail.component.scss']
})
export class ModifyMailComponent implements OnInit, OnDestroy {

  operatore: Operatore;
  mygovEnteId: number;
  emailAddress: string;
  deNomeEnte: string;
  form: FormGroup;
  formErrors = {};
  private valueChangesSub: Subscription;
  iconTimes = faTimes;

  @ViewChild(ToastContainerDirective, { static: true })
  toastContainer: ToastContainerDirective;

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: any,
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<ModifyMailComponent>,
    private adminUtenteService: AdminUtenteService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
  ) {
    this.operatore = data.operatore;
    this.emailAddress = data.emailAddress;
    this.mygovEnteId = data.mygovEnteId;
    this.deNomeEnte = data.deNomeEnte;
  }

  ngOnInit(): void {
    this.toastrService.overlayContainer = this.toastContainer;

    this.form = this.formBuilder.group({
      email: [this.emailAddress, [Validators.required, Validators.email]],
    });

    this.valueChangesSub = subscribeValidateForm(this.form, this.formErrors);
  }

  ngOnDestroy(): void {
    this.valueChangesSub?.unsubscribe();
  }

  onSave(): void {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    const newEmail = this.form.get('email').value;
    this.adminUtenteService
      .changeEmailAddress(this.operatore, this.mygovEnteId, newEmail)
      .subscribe( () => {
        this.overlaySpinnerService.detach(spinner);
        this.toastrService.overlayContainer = null;
        this.dialogRef.close({emailAddress: newEmail});
      }, manageError('Errore modificando la mail', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );

  }

}
