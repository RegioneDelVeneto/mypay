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
import { DateTime } from 'luxon';

import { MapperDef, MapperType } from '../mapper/mapper-def';
import { WithActions } from '../table/with-actions';

export enum EMAIL_SOURCE_TYPE {
   A = 'A',
   B = 'B',
   C = 'C',
   V = 'V',
}

export class User extends WithActions{

  public static readonly EMAIL_SOURCE_TYPE_DESCR = {
    'A': 'Email validata da sistema di autenticazione',
    'B': 'Email validata da operatore',
    'C': 'Email validata da operatore e confermata da utente',
    'V': 'Email validata da utente',
    //description only
    'M': 'Email mancante o non validata',
    'O': 'Validazione email in corso',
  }

  public static readonly MAPPER_DEF = [
    new MapperDef(MapperType.DateTime,'lastLogin','local-date-time'),
  ];

  userId: number;
  username: string;
  nome: string;
  cognome: string;
  codiceFiscale: string;
  email: string;
  emailNew: string;
  emailSourceType: string;
  emailValidationNeeded: boolean;
  telefono: string;
  indirizzo: string;
  civico: string;
  cap: string;
  comuneId: number;
  provinciaId: number;
  nazioneId: number;
  comune: string;
  provincia: string;
  nazione: string;
  entiRoles: { [ente: string]: string[] };
  lastLogin: DateTime;
  loginType: string;
  statoNascita: string;
  provinciaNascita: string;
  comuneNascita: string;
  dataNascita: string;


  rememberMe: boolean;
}
