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
import { Directive } from '@angular/core';
import { MAT_DATE_FORMATS } from '@angular/material/core';

@Directive({
  selector: '[datepickerFormatYyyy]',
  providers: [
    { provide: MAT_DATE_FORMATS, useValue: {
      parse: {
        dateInput: 'yyyy',
      },
      display: {
        dateInput: 'yyyy',
        monthYearLabel: 'MMM/yyyy',
        dateA11yLabel: 'dd/MM/yyyy',
        monthYearA11yLabel: 'MMM/yyyy',
      },
    } },
  ],
})
export class DatepickerFormatYyyyDirective {

  constructor() { 
    //This is intentionally empty
  }

}

@Directive({
  selector: '[datepickerFormatMmYyyy]',
  providers: [
    { provide: MAT_DATE_FORMATS, useValue: {
      parse: {
        dateInput: 'MM/yyyy',
      },
      display: {
        dateInput: 'MM/yyyy',
        monthYearLabel: 'MMM/yyyy',
        dateA11yLabel: 'dd/MM/yyyy',
        monthYearA11yLabel: 'MMM/yyyy',
      },
    } },
  ],
})
export class DatepickerFormatMmYyyyDirective {

  constructor() { 
    //This is intentionally empty
  }

}
