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
import { Observable, throwError } from 'rxjs';

import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { AlreadyManagedError } from '../model/already-managed-error';

@Injectable({
  providedIn: 'root'
})
export class ProcessHTTPMsgService {

  constructor() {
    //This is intentionally empty
  }

  public handleError(error: HttpErrorResponse) {

    //for this kind of errors we just relaunch it as it is (component will deal with it)
    if(error instanceof AlreadyManagedError)
      return throwError(error);

    let errMsg: string;

    if(error.status==500){
      errMsg = 'Errore interno di sistema: ';
    } else if(error.status==400){
      errMsg = 'Dati non validi: ';
    } else if(error.status==401){
      errMsg = 'Non autorizzato: ';
    } else if(error.status==404){
      errMsg = 'Risorsa non trovata: '
    } else if(error.status==422){  //managed exception
      errMsg = 'Impossibile eseguire l\'operazione: '
    } else if(error.status==418){  //I_AM_A_TEAPOT, used to bypass courtesy page with 404
      errMsg = 'Risorsa non trovata: '
    } else if(error.status==471){  //recaptcha fallback
      return throwError(error);
    } else {
      const status = error.status ? ('['+error.status+']') : '';
      errMsg = 'Errore di sistema '+status+': ';
    }

    if(error.error instanceof Blob){
      const reader: FileReader = new FileReader();
      const obs = new Observable((observer: any) => {
        reader.onloadend = (_event) => {
          let message;
          let errorUID;
          try{
            const jsonError = JSON.parse(reader.result?.toString());
            errorUID = jsonError.errorUID;
            message = jsonError.message || jsonError.errorCode;
          }catch(exc){
            message = reader.result?.toString() || 'Errore di sistema';
          }
          errMsg = ProcessHTTPMsgService.trimMessage(errMsg, message, errorUID);
          observer.error(errMsg);
          observer.complete();
        }
      });
      reader.readAsText(error.error);
      return obs;
    } else if(typeof error.error === 'string') {
      let message;
      let errorUID;
      try{
        const jsonError = JSON.parse(error.error?.toString());
        errorUID = jsonError.errorUID;
        message = jsonError.message || jsonError.errorCode;
      }catch(exc){
        message = error.error?.toString() || 'Errore di sistema';
      }
      errMsg = ProcessHTTPMsgService.trimMessage(errMsg, message, errorUID);
      return throwError(errMsg);
    } else {
      const errorUID = error.error?.errorUID;
      const message = error.error?.message || error.error?.errorCode || (errorUID ? null : error.message);
      errMsg = ProcessHTTPMsgService.trimMessage(errMsg, message, errorUID);
      return throwError(errMsg);
    }
  }

  public skipHandleError(error: HttpErrorResponse) {
    return throwError(error);
  }

  public static trimMessage(errMsg:string, message?:string, errorUID?:string){
    if(message)
      errMsg += message?.substring(0,150);
    //strip html
    errMsg = ProcessHTTPMsgService.stripHtml(errMsg);
    if(errMsg.endsWith(': '))
      errMsg = errMsg.substring(0, errMsg.length-2);
    if(message?.length>150)
      errMsg += "...";
    if(errorUID?.length > 0)
      errMsg += "</br></br><i><small>Se si contatta l'assistenza, menzionare il codice '"+errorUID+"'</small></i>";
    return errMsg;
  }

  private static stripHtml(html){
    try{
      const doc = new DOMParser().parseFromString(html || '', 'text/html');
      return doc.body.textContent || '';
    }catch(e){
      return html;
    }
  }
}
