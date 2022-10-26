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
import { AbstractControl, FormGroup, ValidatorFn } from '@angular/forms';

export class DateValidators {

  //Validation should be put on dateTo field
  private static notBefore(dateFrom: AbstractControl): ValidatorFn {
    return (dateTo: AbstractControl): { [key: string]: any } | null =>
      (!dateFrom.value || ! dateTo.value || !dateFrom.value > dateTo.value) ? null : {dateRange: true}
  }
  //Validation should be put on dateFrom field
  private static notAfter(dateTo: AbstractControl): ValidatorFn {
    return (dateFrom: AbstractControl): { [key: string]: any } | null =>
      (!dateFrom.value || ! dateTo.value || !dateFrom.value > dateTo.value) ? null : {dateRange: true}
  }


  //Validation that works on Luxon (DateTime) date fields
  // error is shown on both dateFrom and dateTo FormControl (if touched) (error code is 'dateRange')
  // this validation should be added at FormGroup level
  static dateRange(dateFromField: string, dateToField: string): ValidatorFn {
    return (fg: FormGroup): { [key: string]: any } | null => {
      const dateFrom = fg.get(dateFromField);
      const dateTo = fg.get(dateToField);
      const error = dateFrom.value && dateTo.value && dateFrom.value > dateTo.value;
      [dateFrom, dateTo].forEach(control => {
        if(!error && control.hasError('dateRange')){
          control.setErrors({dateRange: null});
          control.updateValueAndValidity({onlySelf: true, emitEvent: false});
        } else if(error && !control.hasError('dateRange')){
          control.setErrors({dateRange: true});
        }
      });
      return error?{dateRange: true}:null;
    }
  }

  //Validation that works for range-date-picker on Luxon (DateTime) date fields
  // error is shown only on dateFrom FormControl (if touched) (error code is 'dateRange')
  // this validation should be added at FormGroup level
  static dateRangeForRangePicker(dateFromField: string, dateToField: string): ValidatorFn {
    const errorValidFun = (field: AbstractControl, error:boolean, errorName: string) : boolean => {
      const errorObj = {};
      if(!error && field.hasError(errorName)){
        errorObj[errorName] = null;
        field.setErrors(errorObj);
        return true;
      } else if(error && !field.hasError(errorName)){
        errorObj[errorName] = true;
        field.setErrors(errorObj);
      }
      return false;
    };

    return (fg: FormGroup): { [key: string]: any } | null => {
      const dateFrom = fg.get(dateFromField);
      const dateTo = fg.get(dateToField);

      if(dateFrom.disabled){
        dateFrom.setErrors(null);
        dateTo.setErrors(null);
        dateFrom.updateValueAndValidity({onlySelf: true, emitEvent: false});
        return null;
      }

      var update = false;
      update = errorValidFun(dateFrom, dateFrom.getError('matDatepickerParse'), 'matDatepickerParseRangeFrom') || update;
      if(!dateFrom.getError('matDatepickerParse'))
        update = errorValidFun(dateFrom, dateFrom.getError('required'), 'requiredRangeFrom') || update;
      update = errorValidFun(dateFrom, dateTo.getError('matDatepickerParse'), 'matDatepickerParseRangeTo') || update;
      if(!dateTo.getError('matDatepickerParse'))
        update = errorValidFun(dateFrom, dateTo.getError('required'), 'requiredRangeTo') || update;
      update = errorValidFun(dateFrom, dateFrom.value && dateTo.value && dateFrom.value > dateTo.value, 'dateRange') || update;
      update = errorValidFun(dateFrom, dateFrom.getError('matDatepickerMin'), 'dateMinFrom') || update;
      update = errorValidFun(dateFrom, dateFrom.getError('matDatepickerMax'), 'dateMaxFrom') || update;
      update = errorValidFun(dateFrom, dateTo.getError('matDatepickerMin'), 'dateMinTo') || update;
      update = errorValidFun(dateFrom, dateTo.getError('matDatepickerMax'), 'dateMaxTo') || update;
      if(update)
        dateFrom.updateValueAndValidity({onlySelf: true, emitEvent: false});

      return dateFrom.errors;
    }
  }

  private static hasRequiredField = (abstractControl: AbstractControl): boolean => {
    if (abstractControl.validator) {
      const validator = abstractControl.validator({}as AbstractControl);
      if (validator && validator.required) {
        return true;
      }
    }
    return false;
  }

}
