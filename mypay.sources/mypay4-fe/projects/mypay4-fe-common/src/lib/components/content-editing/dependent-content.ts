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
import { Component, OnDestroy } from '@angular/core';
import { FormGroup, AbstractControl, FormControl } from '@angular/forms';
//Adjusted//import 'rxjs/add/operator/debounceTime';
//Adjusted//import 'rxjs/add/operator/distinctUntilChanged';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

//Adjusted//import { Subscription } from 'rxjs/Subscription';
import { Subscription } from 'rxjs'
import { DynamicFunctionUtils } from './dynamic-function-utils'

@Component({
  template: ''
})
export abstract class DependentContent implements OnDestroy {

    private subscriptions:Subscription[] = [];

    ngOnDestroy() {
        // prevent memory leak when component is destroyed
        this.subscriptions.forEach(subscription => subscription.unsubscribe());
    }

    protected gestisciAggiornamentoDaFunzione(jsFunctionString:string, valueDependsOnUids:string[], form: FormGroup, currValParams:any, callback:Function){
        if (jsFunctionString) {
            // questa funzione viene invocata ogni volta che avviene una mdoifica 
            // ad uno dei campi che mi interessano per il calcolo del valore del campo.
            // e si occupa di reperire tutti gli altri parametri necessari al calcolo,
            // invocare la funzione per il calcolo del valore ed aggiornare il valore
            // sul form
            const updateCurrent = (uid:string, newVal:any) =>{
                if(currValParams[uid] === newVal){
                    //nessuna modifica da aggiornare
                    return;
                }
                currValParams[uid] = newVal
                const params:any[] = [];
                const paramsUid:string[] = [];
                // i parametri per la funzione sono esplicitati attraverso un path
                // relativo al campo di destinazione della funzione separando con '.' 
                // ogni livello.
                // Per convenzione utilizziamo come nome delle variabili utilizzati nelle
                // funzioni l'ultimo nome indicato dopo l'ultimo '.'
                valueDependsOnUids.forEach(
                    uidDep => {
                        if(currValParams[uidDep]!=undefined){
                            params.push(currValParams[uidDep]);
                            const paramNameIdx = uidDep.lastIndexOf('.');
                            paramsUid.push(uidDep.substring(paramNameIdx+1));
                        }else{
                            return false;
                        }
                    }
                );

                if(valueDependsOnUids.length === params.length){
                    // uso la promise per evitare problemi con calcoli onerosi
                    new Promise((resolve, reject) =>{
                        resolve( DynamicFunctionUtils.executeFunction(jsFunctionString, params, paramsUid));
                    })
                    .then((updatedValue) =>{
                        callback(updatedValue);
                    });
                }
            }

            // mi metto in ascolto di modifiche in uno dei campi per mantenere aggiornato il mio valore 
            valueDependsOnUids.forEach(uid=>{
                const control:FormControl = DynamicFunctionUtils.getControlFromForm(form, uid);
                if(control){
                    this.subscriptions.push(
                        //Adjusted//control.valueChanges
                        //Adjusted//// .debounceTime(20)
                        //Adjusted//.distinctUntilChanged()
                        control.valueChanges.pipe(distinctUntilChanged())
                        .subscribe(newVal=> updateCurrent(uid, newVal))
                    )
                    control.registerOnDisabledChange(
                        isDisabled =>  {
                           const newVal  = isDisabled ? '': control.value;
                           updateCurrent(uid, newVal)
                        }
                    )
                    control.updateValueAndValidity();
                }
            });
        }
    }
 }
