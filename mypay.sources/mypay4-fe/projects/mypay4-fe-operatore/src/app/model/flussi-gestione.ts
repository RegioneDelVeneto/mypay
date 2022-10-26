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
import { MapperDef, MapperType, WithActions } from 'projects/mypay4-fe-common/src/public-api';

export class FlussiGestione {
  codRequestToken: string;
  flussi: FlussoImport[];
  files: FlussoFile[];
}

export class FlussoImport extends WithActions {

  public static readonly MAPPER_S2C_DEF  = [
    new MapperDef(MapperType.Function,'statoToShow',(flusso: FlussoImport) => {
      let stato = flusso.deStato;
      switch(flusso.codStato) {
        case "ERRORE_CARICAMENTO":
          stato += ' ['+flusso.codErrore+']';
          break;
        case "CARICATO":
          stato += ' - Dovuti caricati: '+flusso.numRigheImportateCorrettamente +
                            '- Scarti: '+(flusso.numRigheTotali-flusso.numRigheImportateCorrettamente);
          break;
      }
      return stato;
    }),
  ];
  id: number;
  nomeFlusso: string;
  dataCaricamento: DateTime;
  operatore: string;
  deStato: string;
  codStato: string;
  pdfGenerati: number;
  showDownload: boolean;
  codErrore: string;
  log: string;
  numRigheTotali: number;
  numRigheImportateCorrettamente: number;
  path: string;
  securityToken: string;

  statoToShow: string;
}

export class FlussoFile extends WithActions {
  public static readonly MAPPER_S2C_DEF = [
    new MapperDef(MapperType.DateTime,'dataCreazione','local-date-time'),
    new MapperDef(MapperType.DateTime,'dataProduzione','local-date-time'),
  ];
  identificativo: string;
  path: string;
  name: string;
  dataProduzione: DateTime;
  dataCreazione: DateTime;
  dimensione: number;
  securityToken: string;
}

export enum TipoFlusso {
  QUADRATURA = "Q",
  RENDICONTAZIONE = "R"
}
