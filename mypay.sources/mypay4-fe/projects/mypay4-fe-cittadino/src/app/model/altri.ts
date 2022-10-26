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
import { IDefinizione, IDefinizioneDovuto, IDefinizioneDovutoResponse, IEnte, IFieldBean,
ISessionInformation, IUtente } from './altri-interface';

export class Message {
  constructor(message:string, severity:'INFO'|'WARN'|'ERROR'|'SUCCESS', messageCode?:string){
      this.messageCode = messageCode;
      this.message = message;
      this.severity = severity;
  }
message: string;
severity: string;
messageCode:string;
}


export class AnonymousUserInfo {
  constructor(email:string, captcha:string){
      this.email = email;
      this.captcha = captcha;
  }
email: string;
captcha: string;
}

export class Definizione implements IDefinizione {
  codIpa: string;
  codTipo: string;
  email: string;
  importo: number;
  captcha: string;
  data: Array<DefinizioneDovuto>;  
}

export class DefinizioneDovuto implements IDefinizioneDovuto{
  constructor(name:string, value:string){
      this.name = name;
      this.value = value;
  }
  name:string;
  value:string;
}

export class Item{
  htmlRender:string;
  htmlLabel:string;
  htmlClass:string;

  name:string;
  regex:string;
  defaultValue: string;

  isRequired:boolean;
  isInsertable:boolean;

  valueDependsOn:string;
  valueDependsOnUids:string;

  validDependsOn:string;
  validDependsOnUids:string;

  enabledDependsOn:string;
  enabledDependsOnUids:string;

  enumerationList: Array<string>;

  subFields: Array<Item>;
  isArray:boolean;
  insOrder:number;

  errorMessage:string;
  helpMessage:string;

  extraMap:any;
}