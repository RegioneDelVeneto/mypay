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


import { ItemCarrello } from './item-carrello';

export class Spontaneo extends ItemCarrello {

  //used to verify email for anonymous
  mailValidationToken: string;

  constructor(){
    super();
    //super.id = getRandomInt(1000000000, 10000000000);
  }

  static setDetails(element: Spontaneo):Spontaneo {
    if(element)
      element.details = [
        {key:'Oggetto del pagamento', value: element.causaleVisualizzata},
        {key:'Numero avviso', value: null},
        {key:'Intestatario avviso', value:
          [element.intestatario?.anagrafica, element.intestatario?.codiceIdentificativoUnivoco].filter(Boolean).join(' - ')},
      ];
    return element;
  }

}
