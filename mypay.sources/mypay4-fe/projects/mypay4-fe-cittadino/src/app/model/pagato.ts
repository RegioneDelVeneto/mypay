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
import { MapperDef, MapperType, WithActions} from 'projects/mypay4-fe-common/src/public-api';
import { DovutoMultibeneficiario } from 'projects/mypay4-fe-operatore/src/app/model/dovuto-multibeneficiario';

export class Pagato extends WithActions {
  public static readonly MAPPER_S2C_DEF = [
    new MapperDef(MapperType.Currency,'importo'),
    new MapperDef(MapperType.DateTime,'dataScadenza','local-date'),
    new MapperDef(MapperType.DateTime,'dataPagamento','local-date'),
    new MapperDef(MapperType.DateTime,'dataInizioTransazione','local-date-time'),
  ];

  id: number;
  causale: string;
  importo: number;
  valuta: string;
  dataScadenza: DateTime;
  dataPagamento: DateTime;

  codStato: string;
  stato: string;
  statoComplessivo: string;

  modPagamento: string;

  //Campi valorizzati solo in caso di dovuto in stato TRANSAZIONE_CONCLUSA
  dataInizioTransazione: DateTime;
  identificativoTransazione: string;
  intestatario: string;
  pspScelto: string;

  //Campi valorizzati solo in caso di dovuto in stato PAGATO
  commissioniApplicatePsp: string;
  allegatoRicevutaCodiceTipo: string;
  allegatoRicevutaTipo: string;
  allegatoRicevutaTest: string;

  showStampaRicevutaButton: boolean;

  enteId: number;
  enteDeNome: string;
  codTipoDovuto: string;
  deTipoDovuto: string;
  codIuv: string;
  securityTokenRt: string;

  //calculated fields at runtime (this app)
  dataPagamentoDay: string;
  dataPagamentoMonth: string;
  dataPagamentoYear: string;

  //Ente primario
  entePrimarioElaboratoDetail: any;
  detailEntePrimario: object[];

  //dovuto multibeneficiario
  dovutoMultibeneficiario: DovutoMultibeneficiario;
  detailMultiBeneficiario: object[];
  isMultibeneficiario: boolean;

  details: object[];

}
