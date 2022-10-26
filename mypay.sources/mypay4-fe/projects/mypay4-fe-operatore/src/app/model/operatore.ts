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
import { MapperDef, MapperType, User } from 'projects/mypay4-fe-common/src/public-api';

import { EnteRoles } from './ente-roles';

export class Operatore extends User {
  public static readonly MAPPER_S2C_DEF = [
    new MapperDef(MapperType.Function,'amministratore',(operatore: Operatore) => {
      return operatore.ruolo === 'ROLE_ADMIN';
    }),
    new MapperDef(MapperType.Function,'statoAssociazione',(operatore: Operatore) => {
      return operatore.flgAssociato ? 'Abilitato' : 'Disabilitato';
    }),
    new MapperDef(MapperType.DateTime,'lastLogin','local-date-time'),
    new MapperDef(MapperType.DateTime,'dtUltimaAbilitazione','local-date-time'),
    new MapperDef(MapperType.DateTime,'dtUltimaDisabilitazione','local-date-time'),
  ];
  public static readonly MAPPER_C2S_DEF = [
    new MapperDef(MapperType.DateTime,'lastLogin','local-date-time'),
  ];

  fullEntiRoles: EnteRoles[];

  ruolo: string;
  flgAssociato: boolean;
  amministratore: boolean;
  statoAssociazione: string;

  dtUltimaAbilitazione: DateTime;
  dtUltimaDisabilitazione: DateTime;
}
