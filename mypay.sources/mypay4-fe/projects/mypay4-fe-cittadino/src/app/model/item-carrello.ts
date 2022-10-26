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
import * as _ from 'lodash';
import { WithActions } from 'projects/mypay4-fe-common/src/public-api';

import { Debito } from './debito';
import { Person } from './person';
import { Spontaneo } from './spontaneo';

export class ItemCarrello extends WithActions {
  id: number;
  causale: string;
  causaleVisualizzata: string;
  bilancio: string;
  importo: number;
  deEnte: string;
  thumbEnte: string;
  codIpaEnte: string;
  codTipoDovuto: string;
  deTipoDovuto: string;
  iud: string;
  datiSpecificiRiscossione: string;
  identificativoUnivocoFlusso: string;
  codIuv: string;
  // Message from validateForm() when error occurs;
  errorMsg: string;
  //detail of intestatario
  intestatario: Person;
  details: object[];

  // Used when mail intestatario is null and mail is compulsory
  versanteEmail: string;

  static isDebito(elem: ItemCarrello): elem is Debito {
    return !this.isSpontaneo(elem);
  }

  static isSpontaneo(elem: ItemCarrello): elem is Spontaneo {
    return _.isNil(elem?.['codIuv']);
  }
}
