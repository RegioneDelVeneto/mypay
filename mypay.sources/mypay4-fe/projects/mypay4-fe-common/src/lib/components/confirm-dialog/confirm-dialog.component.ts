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
import { Component, Inject, OnInit, SecurityContext } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.scss']
})
export class ConfirmDialogComponent implements OnInit {

  htmlContent: string;
  invalid: boolean = false;
  confirmLabel: string;
  cancelLabel: string;
  titleLabel: string;
  checkboxLabel: string;

  checkboxEnabled: boolean = false;
  checkboxState: boolean = false;

  constructor(
    private dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private domSanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.htmlContent = this.domSanitizer.sanitize(SecurityContext.HTML, this.data?.message);
    this.invalid = this.data?.invalid ?? false;
    this.confirmLabel = this.data?.confirmLabel ?? 'Conferma';
    this.cancelLabel = this.data?.cancelLabel ?? 'Annulla';
    this.titleLabel = this.data?.titleLabel ?? 'Conferma operazione';
    if(this.data?.checkboxLabel){
      this.checkboxLabel = this.data?.checkboxLabel;
      this.checkboxEnabled = true;
    }
  }

  manageClose(result: boolean){
    this.dialogRef.close(result+(this.checkboxState?'_checked':''));
  }

}
