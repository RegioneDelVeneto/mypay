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
import {
    Comune, MapperDef, MapperType, Nazione, Provincia, TipoDovuto, WithActions
} from 'projects/mypay4-fe-common/src/public-api';

export class Dovuto extends WithActions {

  public static CF_ANONIMO = 'ANONIMO';

  public static readonly MAPPER_S2C_DEF = [
    new MapperDef(MapperType.DateTime,'dataScadenza','local-date'),
    new MapperDef(MapperType.DateTime,'dataStato','local-date-time'),
    new MapperDef(MapperType.DateTime,'dataInizioTransazione','local-date-time'),
    new MapperDef(MapperType.Function,'causaleToShow',(dovuto: Dovuto) => {
      return dovuto.causaleVisualizzata || dovuto.causale;
    })
  ];
  public static readonly MAPPER_C2S_DEF = [
    new MapperDef(MapperType.DateTime,'dataScadenza','local-date'),
    new MapperDef(MapperType.DateTime,'dataStato','local-date-time'),
    new MapperDef(MapperType.DateTime,'dataInizioTransazione','local-date-time'),
  ];

  id: number;
  codFiscale: string;
  iud: string;
  iuv: string;
  causale: string;
  causaleVisualizzata: string;
  importo: string;
  dataScadenza: DateTime;
  stato: string;
  codStato: string;
  dataStato: DateTime;
  hasAvviso: boolean;
  hasRicevuta: boolean;

  //details
  tipoDovuto: TipoDovuto;
  iuf: string;
  anagrafica: string;
  flgAnagraficaAnonima: boolean;
  tipoSoggetto: string;
  hasCodFiscale: boolean;
  email: string;
  indirizzo: string;
  numCiv: string;
  cap: string;
  nazione: Nazione;
  prov: Provincia;
  comune: Comune;
  flgGenerateIuv: boolean;

  //details dovutoElaborato (opeatore)
  dataInizioTransazione: DateTime;
  identificativoTransazione: string;
  intestatario: string;
  pspScelto: string;

  //calculated fields
  causaleToShow: string;

  dovutoType: string; //'debito' or 'pagato'

  invalidDesc: string;

  details: object[];
}
