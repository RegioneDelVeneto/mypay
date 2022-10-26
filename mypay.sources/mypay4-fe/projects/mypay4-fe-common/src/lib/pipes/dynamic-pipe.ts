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
import { Injector, Pipe, PipeTransform, Type } from '@angular/core';

@Pipe({
name: 'dynamicPipe'
})
export class DynamicPipe implements PipeTransform {

  public constructor(private injector: Injector) {
  }

  transform(value: any, pipeToken: any, pipeArgs: any[]): any {
    if (!pipeToken) {
      return value;
    } else {
      const pipe = this.injector.get<PipeTransform>(pipeToken as Type<PipeTransform>);
      try{
        return pipe.transform(value, ...(pipeArgs ?? []));
      } catch(error){
        console.warn(`Dynamic pipe [${pipeToken?.constructor?.name}] error on value [${value}]: ${error}`);
        return value;
      }
    }
  }
}
