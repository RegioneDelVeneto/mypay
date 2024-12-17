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
import { Directive, Input } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

import { AbstractTextOperationDirective } from './abstact-text-operation.directive';

@Directive({
  selector: "input[removeSpace], textarea[removeSpace]",
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: RemoveSpaceDirective, multi: true }]
})
export class RemoveSpaceDirective extends AbstractTextOperationDirective {

  // Get a value of the removeSpace attribute if it was set.
  @Input() removeSpace: string;

  protected processValue(value: string): string{
    return value?.replace(/\s*/g, "");
  }

  protected eventValue(): string {
    return this.removeSpace;
  }

}
