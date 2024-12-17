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
import { WithActions } from 'projects/mypay4-fe-common/src/public-api';

import { MapperDef, MapperType } from '../../../../mypay4-fe-common/src/lib/mapper/mapper-def';

/** Common DTO for FlussoTassonomia and FlussoMassivo */
export class FlussoTasMas extends WithActions {
  public static readonly MAPPER_S2C_DEF  = [
    new MapperDef(MapperType.DateTime,'dtCreazione','local-date-time'),
    new MapperDef(MapperType.Function,'statoToShow',(flusso: FlussoTasMas) => {
      switch(flusso.codStato) {
        case "ERRORE_CARICAMENTO":
        case "ERRORE_ELABORAZIONE":
          return flusso.codErrore;
        case "CARICATO":
          return `Dovuti caricati: ${flusso.numRigheElaborateCorrettamente} - \
                  Scarti: ${(flusso.numRigheTotali-flusso.numRigheElaborateCorrettamente)}`;
      }
    }),
  ];
  public static readonly MAPPER_C2S_DEF  = [
    new MapperDef(MapperType.DateTime,'dtCreazione','local-date-time'),
  ];
  id: number;
  codStato: string;
  deStato: string;
  iuft: string;
  tipoMassivo: string; //Only for FlussoMassivo.
  numRigheTotali: number;
  numRigheElaborateCorrettamente: number;
  dtCreazione: DateTime;
  deNomeOperatore: string;
  path: string;
  codErrore: string;
  showDownload: boolean;
  securityToken: string;
  hash: string;
  statoToShow: string;
}
