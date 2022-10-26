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
import { MapperDef, MapperType } from 'projects/mypay4-fe-common/src/public-api';

import { ItemCarrello } from './item-carrello';
import { Pagato } from './pagato';

export class Debito extends ItemCarrello {
  public static readonly MAPPER_S2C_DEF = [
    new MapperDef(MapperType.Currency,'importo'),
    new MapperDef(MapperType.DateTime,'dataScadenza','local-date'),
  ];

  codIud: string;
  causaleVisualizzata: string;
  valuta: string;
  codStato: string;
  dataScadenza: DateTime;
  deStato: string;
  modPagamento: string;
  codIuv: string;
  isMultiIntestatario: boolean;
  numeroAvviso: string;
  intestatarioAvviso: string;
  securityTokenAvviso: string;

  //calculated fields at runtime (this app)
  dataScadenzaDay: string;
  dataScadenzaMonth: string;
  dataScadenzaYear: string;
  dovutoElaborato: Pagato;
  deStatoOriginaleCarrello: string;

  static setDetails(element: Debito):Debito {
    if(element)
      element.details = [
        {id:'causaleVisualizzata', key:'Oggetto del pagamento', value:element.causaleVisualizzata},
        {id:'numeroAvviso', key:'Numero avviso', value:element.numeroAvviso},
        {id:'intestatarioAvviso', key:'Intestatario avviso', value:element.intestatarioAvviso},
        {id:'dataScadenza', key:'Data scadenza', value:element.dataScadenza?.toFormat('dd/MM/yyyy')} //this is used in carrello
        //{key:'Stato', value:element.deStato}
      ];
    return element;
  }

}
