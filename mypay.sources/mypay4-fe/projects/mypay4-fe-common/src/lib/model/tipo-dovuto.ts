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

export class TipoDovuto extends WithActions {
  public static readonly MAPPER_S2C_DEF = [
    new MapperDef(MapperType.Function,'statoAbilitazione',(tipoDovuto: TipoDovuto) => {
      return tipoDovuto.flgAttivo ? 'Abilitato' : 'Disabilitato';
    }),
    new MapperDef(MapperType.DateTime,'dtUltimaAbilitazione','local-date-time'),
    new MapperDef(MapperType.DateTime,'dtUltimaDisabilitazione','local-date-time'),
  ]
  mygovEnteTipoDovutoId: number;
  codIpaEnte: string;
  codTipo: string;
  deTipo: string;
  deUrlPagamentoDovuto: string;

  //for operatore
  mygovEnteId: number;
  deNomeEnte: string;
  thumbLogoEnte: string;
  flgCfAnonimo: boolean;
  flgScadenzaObbligatoria: boolean;
  flgAttivo: boolean;
  importo: string;

  statoAbilitazione: string;
  dtUltimaAbilitazione: DateTime;
  dtUltimaDisabilitazione: DateTime;
}
