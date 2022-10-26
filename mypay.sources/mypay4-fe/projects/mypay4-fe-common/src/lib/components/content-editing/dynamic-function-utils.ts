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
import { FormGroup, AbstractControl, FormControl } from '@angular/forms';

export class DynamicFunctionUtils {

    static executeFunction(jsFunctionString:string, params:any[], paramsUid:string[]): any  {
        const args:string[] = paramsUid.concat(jsFunctionString);
        // creo la nuova funzione ... e poi la eseguo
        try{
            const res:any = Function.apply(null, args).apply(null, params);
            return  res;
        } catch (err){
            console.log(err);
            console.log('function:'+jsFunctionString);
            console.log('args:'+JSON.stringify(args));
            console.log('params:'+JSON.stringify(params));
            alert("Si e' verificato un errore nell'esecuzione della funzione");
        }
        return null;
    }
     
    static getControlFromForm(form:FormGroup, uid:string):FormControl{
        if(uid){ 
            const uidPath:string[] = uid.split('.');
            return DynamicFunctionUtils.getControlFromFormByUidPath(form, uidPath);
        }
        else{
            return null;
        }
    }

    // recupero in modo ricorsivo il valore del FormControl indicato navigando i formGroup
    // utilizzando "parent" come parola chiave per risalire al formGroup padre
    private  static  getControlFromFormByUidPath(form:FormGroup, uidPath:string[]):FormControl{
        if(uidPath.length>0){
            const path = uidPath.shift();
            const subForm:AbstractControl = (path==='parent') ? form.parent : form.controls[path];;
            if(subForm){
                if (subForm instanceof FormGroup && uidPath.length>0){
                    return DynamicFunctionUtils.getControlFromFormByUidPath(subForm, uidPath);
                }else if (subForm instanceof FormControl && uidPath.length===0){
                    return subForm;
                }else{
                    throw new TypeError('Elemento trovato non corretto');
                }
            }
            else {
                throw new Error('Path del form non corretto. Elemento non trovato');
            }
        }
        else{
            throw new Error('Path del form non corretto. Elemento non trovato');
        }
    }
}
