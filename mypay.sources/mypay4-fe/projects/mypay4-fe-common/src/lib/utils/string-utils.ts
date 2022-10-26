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

import { DecimalPipe } from '@angular/common';
import { AbstractControl } from '@angular/forms';

const LOCALE = 'it';

const __amountDecimalPipe = new DecimalPipe(LOCALE);

export function blank2Null(s: string): string {
  return s!=null && s.trim() !== '' ? s.trim() : null;
}

export function isBlankOrNull(s: string): boolean {
  return !blank2Null(s);
}

export function applyIfNotBlank<T>(s: string, funToApply: (string) => T): T {
  return isBlankOrNull(s) ? null : funToApply(s);
}

export function numberToFormattedAmount(n: number | string, removeThousandsSep: boolean = false):string {
  if(_.isNil(n) || n.toString().trim().length === 0)
    return '';
  if(removeThousandsSep){
    return new Intl.NumberFormat(LOCALE, {
      useGrouping: false,
      maximumFractionDigits: 2
    }).format(Number(n.toString()));
  }else
    return __amountDecimalPipe.transform(n, '1.2-2');
}

export function formettedAmountToNumberString(s: string):string {
  return formettedAmountToNumber(s)?.toString() ?? '';
}

export function formettedAmountToNumber(s: string):number {
  try{
    if(_.isNil(s) || s.trim().length === 0)
      return null;
    return new Number(s.replace(/[^\d,-]/g, '').replace(/,/g, '.')).valueOf();
  }catch(error){
    return null;
  }
}

export const PATTERNS = {
  codiceFiscale: /^(?:[A-Z][AEIOU][AEIOUX]|[B-DF-HJ-NP-TV-Z]{2}[A-Z]){2}(?:[\dLMNP-V]{2}(?:[A-EHLMPR-T](?:[04LQ][1-9MNP-V]|[15MR][\dLMNP-V]|[26NS][0-8LMNP-U])|[DHPS][37PT][0L]|[ACELMRT][37PT][01LM]|[AC-EHLMPR-T][26NS][9V])|(?:[02468LNQSU][048LQU]|[13579MPRTV][26NS])B[26NS][9V])(?:[A-MZ][1-9MNP-V][\dLMNP-V]{2}|[A-M][0L](?:[1-9MNP-V][\dLMNP-V]|[0L][1-9MNP-V]))[A-Z]$/i ,
  codiceFiscaleOPartitaIva: /^(?:[A-Z][AEIOU][AEIOUX]|[B-DF-HJ-NP-TV-Z]{2}[A-Z]){2}(?:[\dLMNP-V]{2}(?:[A-EHLMPR-T](?:[04LQ][1-9MNP-V]|[15MR][\dLMNP-V]|[26NS][0-8LMNP-U])|[DHPS][37PT][0L]|[ACELMRT][37PT][01LM]|[AC-EHLMPR-T][26NS][9V])|(?:[02468LNQSU][048LQU]|[13579MPRTV][26NS])B[26NS][9V])(?:[A-MZ][1-9MNP-V][\dLMNP-V]{2}|[A-M][0L](?:[1-9MNP-V][\dLMNP-V]|[0L][1-9MNP-V]))[A-Z]$|^[0-9]{11}$/i ,
  partitaIva: /^[0-9]{11}$/ ,
  cap: /^[0-9]{5}$/ ,
  importo: /^\d*(\,\d{0,2})?$/ ,
  importoNonZero: /^\s*(?=.*[1-9])\d*(?:\,\d{1,2})?\s*$/,
  tipoSoggetto: /^[FG]$/ ,
  urlWeb: /^(?:(?:(?:https?|ftp):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z0-9\u00a1-\uffff][a-z0-9\u00a1-\uffff_-]{0,62})?[a-z0-9\u00a1-\uffff]\.)+(?:[a-z\u00a1-\uffff]{2,}\.?))(?::\d{2,5})?(?:[/?#]\S*)?$/i ,
  url: /^(?:(?:(?:https?|ftp):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z0-9\u00a1-\uffff][a-z0-9\u00a1-\uffff_-]{0,62})?[a-z0-9\u00a1-\uffff]\.)+(?:[a-z\u00a1-\uffff]{2,}\.?)|localhost)(?::\d{2,5})?(?:[/?#]\S*)?$/i ,
  fixedDigit: (n:number) => {return new RegExp('^\\d{'+n+'}$'); }
}

export function controlToUppercase(formControl: AbstractControl){
  if(formControl)
    return formControl.valueChanges.subscribe(() => {
      formControl.patchValue(formControl.value?.toUpperCase(), {emitEvent: false});
    });
}

export function controlToLowercase(formControl: AbstractControl){
  if(formControl)
    return formControl.valueChanges.subscribe(() => {
    formControl.patchValue(formControl.value?.toLowerCase(), {emitEvent: false});
  });
}

/**
 * generate groups of 4 random characters
 * @example getUniqueId(1) : 607f
 * @example getUniqueId(2) : 95ca-361a-f8a1-1e73
 */
export function getUniqueId(parts: number = 1): string {
  const stringArr = [];
  for(let i = 0; i< parts; i++){
    // tslint:disable-next-line:no-bitwise
    const S4 = (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    stringArr.push(S4);
  }
  return stringArr.join('-');
}

/**
 * Returns a random integer between min (inclusive) and max (inclusive).
 * The value is no lower than min (or the next integer greater than min
 * if min isn't an integer) and no greater than max (or the next integer
 * lower than max if max isn't an integer).
 * Using Math.round() will give you a non-uniform distribution!
 */
export function getRandomInt(min: number, max:number):number {
  min = Math.ceil(min);
  max = Math.floor(max);
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function encodeRFC5987ValueChars(str) {
  return encodeURIComponent(str).
      // Notare che anche se per l'RFC3986 "!" è riservato, non lo è per
      // l' RFC5987, quindi non viene sostituito
      replace(/['()]/g, escape). // i.e., %27 %28 %29
      replace(/\*/g, '%2A').
          // Per l'RFC5987 questi caratteri non necessitano di essere codificati,
          // quindi possiamo consentire un po' più di leggibilità: |`^
          replace(/%(?:7C|60|5E)/g, unescape);
}
