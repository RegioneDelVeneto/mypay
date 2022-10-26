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
import { first } from 'rxjs/operators';

import { Directive, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { ConfirmDialogComponent } from '../components/confirm-dialog/confirm-dialog.component';

@Directive({
  selector: '[app-confirm]'
})
export class AppConfirmDirective {
  @Output("clickConfirmed") clickConfirmed: EventEmitter<any> = new EventEmitter();

  message: string;
  invalid: boolean = false;
  confirmLabel: string;
  cancelLabel: string;

  @Input('app-confirm') set setMessage(message: string | {message: string, invalid: boolean, confirmLabel?: string, cancelLabel?: string}){
    if(typeof message == 'string'){
      this.message = message;
      this.invalid = false;
    }else{
      this.message = message.message;
      this.invalid = message.invalid;
      this.confirmLabel = message.confirmLabel;
      this.cancelLabel = message.cancelLabel;
    }
  }

  constructor(private dialog: MatDialog) { }

  @HostListener('click', ['$event'])
  onClick(e: Event) {
    e.preventDefault();
    e.stopPropagation();
    this.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: this.message,
      invalid: this.invalid, confirmLabel: this.confirmLabel, cancelLabel: this.cancelLabel}})
    .afterClosed().pipe(first()).subscribe(result => {
      if(result==="true")
        this.clickConfirmed.next(e);
    });
  }
}
