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
import { BehaviorSubject } from 'rxjs';

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'detailfilter',
    pure: false
})
export class DetailFilterPipe implements PipeTransform {
    transform(items: any[] | BehaviorSubject<any[]>, include:string[], exclude:string[]): any {
        if (!items || !(include || exclude)) {
            return items;
        }
        // filter items array, items which match and return true will be
        // kept, false will be filtered out
        let newItems: any[];
        if(items instanceof BehaviorSubject)
          newItems = items.value;
        else
          newItems = items;
        if(include)
          newItems = newItems.filter(item => !item.id || include.includes(item.id));
        if(exclude)
          newItems = newItems.filter(item => !item.id || !exclude.includes(item.id));
        return newItems;
    }
}
