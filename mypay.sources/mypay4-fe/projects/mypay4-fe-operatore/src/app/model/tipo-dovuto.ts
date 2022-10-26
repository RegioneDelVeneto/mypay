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
import { Ente } from './ente';

export class TipoDovuto {

  mygovEnteTipoDovutoId: number;
  mygovEnteId: Ente;
  codTipo: string;
  deTipo: string;
  ibanAccreditoPi: string;
  bicAccreditoPi: string;
  ibanAppoggioPi: string;
  bicAppoggioPi: string;
  ibanAccreditoPsp: string;
  bicAccreditoPsp: string;
  ibanAppoggioPsp: string;
  bicAppoggioPsp: string;
  codContoCorrentePostale: string;
  codXsdCausale: string;
  bicAccreditoPiSeller: boolean;
  bicAccreditoPspSeller: boolean;
  spontaneo: boolean;
  importo: number;
  deUrlPagamentoDovuto: string;
  deBilancioDefault: string;
  flgCfAnonimo: boolean;
  flgScadenzaObbligatoria: boolean;
  flgStampaDataScadenza: boolean;
  deIntestatarioCcPostale: string;
  deSettoreEnte: string;
  flgNotificaIo: boolean;
  flgNotificaEsitoPush: boolean;
  maxTentativiInoltroEsito: number;
  mygovEnteSilId: number;
  flgAttivo: boolean;
  codiceContestoPagamento: string;
  flgDisabilitaStampaAvviso: boolean;
  macroArea: string;
  tipoServizio: string;
  motivoRiscossione: string;
  codTassonomico: string;
}