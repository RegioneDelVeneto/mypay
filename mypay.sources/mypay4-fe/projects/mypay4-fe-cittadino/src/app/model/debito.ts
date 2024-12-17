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
import { TitleCasePipe } from '@angular/common';
import { DateTime } from 'luxon';
import { MapperDef, MapperType, numberToFormattedAmount } from 'projects/mypay4-fe-common/src/public-api';
import { DovutoMultibeneficiario } from 'projects/mypay4-fe-operatore/src/app/model/dovuto-multibeneficiario';


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
  annullabile: boolean;
  flgIuvVolatile: boolean;
  
  //calculated fields at runtime (this app)
  dataScadenzaDay: string;
  dataScadenzaMonth: string;
  dataScadenzaYear: string;
  dovutoElaborato: Pagato;
  deStatoOriginaleCarrello: string;

  //Dovuto Multibeneficiario
  dovutoMultibeneficiario: DovutoMultibeneficiario
  detailMultiBeneficiario: any;
  flgMultibeneficiario: boolean;

  //Ente primario
  entePrimarioDetail: any;
  detailEntePrimario: object[];

  static setDetails(element: Debito):Debito {
    if(element)
      element.details = [
        {id:'causaleVisualizzata', key:'Oggetto del pagamento', value:element.causaleVisualizzata},
        {key:'Stato', value:new TitleCasePipe().transform(element.deStato)},
        {id:'numeroAvviso', key:'Numero avviso', value:element.numeroAvviso},
        {id:'intestatarioAvviso', key:'Intestatario avviso', value:element.intestatarioAvviso},
        {id:'dataScadenza', key:'Data scadenza', value:element.dataScadenza?.toFormat('dd/MM/yyyy')}//this is used in carrello
      ];
    return element;
  }

  static setDetailMB(element: Debito): Debito {
    if(element)
      element.detailMultiBeneficiario = [
        {id:'denominazioneBeneficiario', key:'Denomincazione Ente', value:element.dovutoMultibeneficiario?.denominazioneBeneficiario},
        {id:'codiceIdentificativoUnivoco', key:'Codice Fiscale', value:element.dovutoMultibeneficiario?.codiceIdentificativoUnivoco},
        {id:'ibanAddebito', key:'IBAN addebito', value:element.dovutoMultibeneficiario?.ibanAddebito},
        {id:'indirizzoBeneficiario', key:'Indirizzo', value:element.dovutoMultibeneficiario?.indirizzoBeneficiario},
        {id:'civicoBeneficiario', key:'Civico', value:element.dovutoMultibeneficiario?.civicoBeneficiario},
        {id:'capBeneficiario', key:'CAP', value:element.dovutoMultibeneficiario?.capBeneficiario},
        {id:'nazioneBeneficiario', key:'Nazione', value:element.dovutoMultibeneficiario?.nazioneBeneficiario},
        {id:'provinciaBeneficiario', key:'Provincia', value:element.dovutoMultibeneficiario?.provinciaBeneficiario},
        {id:'localitaBeneficiario', key:'Località', value:element.dovutoMultibeneficiario?.localitaBeneficiario},
        {id:'importoSecondario', key:'Importo', value:numberToFormattedAmount(element.dovutoMultibeneficiario?.importoSecondario)+' €'}
      ];
      return element;
  }


  static setDetailEntePrimario(element: Debito): Debito {
    if(element) {
      element.detailEntePrimario = [
        {id:'denominazioneBeneficiario', key:'Denomincazione Ente', value:element.entePrimarioDetail?.denominazioneBeneficiario},
        {id:'codiceIdentificativoUnivoco', key:'Codice Fiscale', value:element.entePrimarioDetail?.codiceIdentificativoUnivoco},
        {id:'ibanAddebito', key:'IBAN addebito', value:element.entePrimarioDetail?.ibanAddebito},
        {id:'indirizzoBeneficiario', key:'Indirizzo', value:element.entePrimarioDetail?.indirizzoBeneficiario},
        {id:'civicoBeneficiario', key:'Civico', value:element.entePrimarioDetail?.civicoBeneficiario},
        {id:'capBeneficiario', key:'CAP', value:element.entePrimarioDetail?.capBeneficiario},
        {id:'nazioneBeneficiario', key:'Nazione', value:element.entePrimarioDetail?.nazioneBeneficiario},
        {id:'provinciaBeneficiario', key:'Provincia', value:element.entePrimarioDetail?.provinciaBeneficiario},
        {id:'localitaBeneficiario', key:'Località', value:element.entePrimarioDetail?.localitaBeneficiario},
        {id:'importoSecondario', key:'Importo', value:numberToFormattedAmount(element.entePrimarioDetail?.importo)+' €'}
      ];
      return element;
    }
  }


}
