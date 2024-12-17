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
import { Subscription } from 'rxjs';

import { AbstractControl, FormControl, FormGroup, ValidatorFn } from '@angular/forms';

const __formValidationMessages = {
  'required': 'Campo obbligatorio',
  'pattern': 'Valore non corretto',
  'invalid': 'Valore non valido',
  'minlength': 'Valore troppo corto',
  'maxlength': 'Valore troppo lungo',
  'email': 'Email non valida',
  'matDatepickerParse': 'Data non valida',
  'dateRange': '"Data a" precede "Data da"',
  'dateRangeMonth': 'Intervallo massimo di estrazione pari ad 1 mese',
  'matDatepickerParseRangeFrom': '"Data da" non valida',
  'matDatepickerParseRangeTo': '"Data a" non valida',
  'requiredRangeFrom': '"Data da" obbligatoria',
  'requiredRangeTo': '"Data a" obbligatoria',
  'dateMinFrom': '"Data da" precedente alla data minima ammessa',
  'dateMaxFrom': '"Data da" successiva alla data massima ammessa',
  'dateMinTo': '"Data a" precedente alla data minima ammessa',
  'dateMaxTo': '"Data a" successiva alla data massima ammessa',
  'fieldMismatch': 'Campo conferma diverso dal campo di riferimento',
}

function validateForm(formGroup:FormGroup, data:Object, formErrors?:{}, formValidationMessages?:{}):{} {
  if(!formErrors)
    formErrors={};
  if(formGroup && data)
    Object.keys(data).forEach(field => {
      const control = formGroup.get(field);
      formErrors[field] = '';
      if(control && !control.valid){
        for(const key in control.errors) {
          if(control.errors.hasOwnProperty(key)){
            formErrors[field] += (formValidationMessages?.[field]?.[key] ?? formValidationMessages?.[key]
                                  ?? __formValidationMessages[key] ?? key) + ' ';
            //console.log('field:',field,' - key:',key,' - formErrors[field]:',formErrors[field]);
            control.markAsDirty();
          }
        }
      }
    });
  return formErrors;
}

export function subscribeValidateForm(formGroup:FormGroup, formErrors?:{}, formValidationMessages?:{}, callbackFun?:(data:Object)=>void): Subscription{
  patchFormGroupMarkAsTouched(formGroup);
  return formGroup.valueChanges.subscribe( (data:Object) => {
    callbackFun?.(data);
    return validateForm(formGroup, data, formErrors, formValidationMessages);
  } )
}

export function subscribeValidateFormAsync(formGroup:FormGroup, formErrors?:{}, formValidationMessages?:{}, callbackFun?:(data:Object)=>void): Subscription{
  patchFormGroupMarkAsTouched(formGroup);
  return formGroup.valueChanges.subscribe( (status:Object) => {
    const data = formGroup.value;
    callbackFun?.(data);
    return validateForm(formGroup, data, formErrors, formValidationMessages);
  } )
}

export function validateFormFun(formGroup:FormGroup, formErrors?:{}, formValidationMessages?:{}, callbackFun?:(data:Object)=>void):(any)=>{} {
  patchFormGroupMarkAsTouched(formGroup);
  return (data:Object) => {
    callbackFun?.(data);
    return validateForm(formGroup, data, formErrors, formValidationMessages);
  }
}

export function validateFormFunAsync(formGroup:FormGroup, formErrors?:{}, formValidationMessages?:{}, callbackFun?:(data:Object)=>void):(any)=>{} {
  patchFormGroupMarkAsTouched(formGroup);
  return (status:Object) => {
    const data = formGroup.value;
    callbackFun?.(data);
    return validateForm(formGroup, data, formErrors, formValidationMessages);
  }
}

function patchFormGroupMarkAsTouched(formGroup: FormGroup){
  // workaround to be able to listen to 'touched' controls (angular doesn't provide this feature)
  // see this for details: https://github.com/angular/angular/issues/10887
  const origMarkAsTouched = formGroup.markAsTouched;
  if(!formGroup?.['hasCustomMarkAsTouched']){
    //console.log('patching FormGroup.markAsTouched()...')
    formGroup.markAsTouched = (opts?: {onlySelf?: boolean;}) => {
      if(formGroup.untouched){
        //console.log("trigger touched");
        origMarkAsTouched.apply(formGroup, opts);
        formGroup.updateValueAndValidity();
      }
    };
    formGroup['hasCustomMarkAsTouched']=true;
  }
}

export function conditionalValidator(
  predicate: () => boolean,
  validator: ValidatorFn,
  errorNamespace?: string
): ValidatorFn {
  return formControl => {
    if (!formControl.parent) {
      return null;
    }
    let error = null;
    if (predicate()) {
      error = validator(formControl);
    }
    if (errorNamespace && error) {
      const customError = {};
      customError[errorNamespace] = error;
      error = customError;
    }
    return error;
  };
}

export function fieldsMatchValidator(otherField: AbstractControl) {
  return (control: FormControl) => {
    //console.log("fieldsMatchValidator", control, control.value , otherField.value, control.value !== otherField.value)
    if (control.value !== otherField.value) {
      return { 'fieldMismatch': true }
    } else {
      return null;
    }
  }
}
