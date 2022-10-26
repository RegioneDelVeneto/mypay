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
//The pipe class implements the PipeTransform interface's transform method that accepts an input value and an optional array of parameters and returns the transformed value.
import { Pipe, PipeTransform } from '@angular/core';

//We tell Angular that this is a pipe by applying the @Pipe decorator which we import from the core Angular library.
@Pipe({
  //The @Pipe decorator takes an object with a name property whose value is the pipe name that we'll use within a template expression. It must be a valid JavaScript identifier. Our pipe's name is orderby.
  name: "orderby"
})
export class OrderByPipe implements PipeTransform {
  transform(array:Array<any>, orderByValue: any):Array<any> {
    // Check if array exists, in this case array contains articles and args is an array that has 1 element : !id
    if(array) {
      // get the first element
      // let orderByValue = args[0];
      let byVal = 1;
      // check if exclamation point
      if(orderByValue.charAt(0) == "!") {
        // reverse the array
        byVal = -1;
        orderByValue = orderByValue.substring(1);
      }
      //console.log("byVal",byVal);
      //console.log("orderByValue",orderByValue);
      array.sort((a: any, b: any) => {
        if(a[orderByValue] < b[orderByValue]) {
          return -1*byVal;
        } else if (a[orderByValue] > b[orderByValue]) {
          return 1*byVal;
        } else {
          return 0;
        }
      });
      return array;
    }
    //
  }
}
