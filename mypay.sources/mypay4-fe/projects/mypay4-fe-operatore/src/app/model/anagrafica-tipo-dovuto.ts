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
export class AnagraficaTipoDovuto {
  mygovEnteTipoDovutoId: number;
  mygovEnteId: number;
  codIpaEnte: string;
  codTipo: string;
  deTipo: string;
  ibanAccreditoPi: string;
  ibanAccreditoPsp: string;
  codXsdCausale: string;
  importo: number;
  spontaneo: boolean;
  flgCfAnonimo: boolean;
  flgScadenzaObbligatoria: boolean;
  flgStampaDataScadenza: boolean;
  deUrlPagamentoDovuto: string;
  deBilancioDefault: string;
  deSettoreEnte: string;
  deIntestatarioCcPostale: string;
  flgNotificaIo: boolean;
  flgDisabilitaStampaAvviso: boolean;
  codiceContestoPagamento: string;
  flgNotificaEsitoPush: boolean;
  maxTentativiInoltroEsito: number;
  macroArea: string;
  tipoServizio: string;
  motivoRiscossione: string;
  codTassonomico: string;
  flgScadenzaObbligatoriaEnable: boolean;

  // Data in mygov_ente_sil
  nomeApplicativo: string;
  deUrlInoltroEsitoPagamentoPush: string;
  codServiceAccountJwtUscitaClientId: string;
  deServiceAccountJwtUscitaClientMail: string;
  codServiceAccountJwtUscitaSecretKeyId: string;
  codServiceAccountJwtUscitaSecretKey: string;
  flgJwtAttivo: boolean;
}
