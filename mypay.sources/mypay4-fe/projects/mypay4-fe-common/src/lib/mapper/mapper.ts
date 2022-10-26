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
import { DateTime } from 'luxon';

import { MapperDef, MapperType } from './mapper-def';

export function mapper<T>(object: T, mapperDef: MapperDef[]):T {
  if(!object)
    return object;

  mapperDef.forEach( def => {
    if(def.type === MapperType.DateTime){
      let format;
      switch(def.format){
        case 'local-date': format = 'yyyy/MM/dd'; break;
        case 'local-date-time': format = 'yyyy/MM/dd-HH:mm:ss'; break;
        default: format = def.format || 'yyyy/MM/dd-HH:mm:ss';
      }
      if(typeof object[def.property] === 'string'){
        if(format == 'unix')
          object[def.property] = DateTime.fromSeconds(parseInt(object[def.property]));
        else if(format == 'unix-ms')
          object[def.property] = DateTime.fromMillis(parseInt(object[def.property]));
        else{
          if((<string>object[def.property]).trim().length===0)
            object[def.property] = null;
          else {
            const dt = DateTime.fromFormat(object[def.property], format);
            if(dt.isValid)
              object[def.property] = dt;
            else
              throw new Error('invalid DateTime with format ['+format+']: '+object[def.property]);
          }
        }
      } else if(typeof object[def.property] === 'number'){
        if(format == 'unix')
          object[def.property] = DateTime.fromSeconds(object[def.property]);
        else if(format == 'unix-ms')
          object[def.property] = DateTime.fromMillis(object[def.property]);
      } else if(object[def.property] instanceof DateTime){
        object[def.property] = object[def.property].toFormat(format);
      } else if(Array.isArray(object[def.property])){
        object[def.property] = DateTime.fromFormat(_.join(_.range(0,7).map(i => object[def.property]?.[i] || 0),":"),"y:M:d:H:m:s:S");
      }
    } else if(def.type === MapperType.Currency){
      if(typeof object[def.property] === 'string')
        object[def.property] = Number(object[def.property]?.replace(',','.'));
    } else if(def.type === MapperType.Function){
      object[def.property] = def.format(object);
    } else if(def.type == MapperType.Rename){
      object[def.property] = object[def.format];
    } else if(def.type === MapperType.Boolean){
      if(object[def.property])
        object[def.property] = ["true","yes","on","1","si","vero"].includes(String(object[def.property]).toLowerCase());
    } else if(def.type === MapperType.Number){
      if(object[def.property])
      object[def.property] = Number(object[def.property]);
    }
  });
  return object;
}
