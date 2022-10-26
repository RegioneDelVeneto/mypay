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

export class Nazione {
  nazioneId: number;
  nomeNazione: string;
  codiceIsoAlpha2: string;
  constructor(nazioneId: number){
    this.nazioneId = nazioneId;
  }
}

export class Provincia {
  provinciaId: number;
  provincia: string;
  sigla: string;
  constructor(provinciaId: number){
    this.provinciaId = provinciaId;
  }
}

export class Comune {
  comuneId: number;
  comune: string;
  provinciaId: number;
  //siglaProvincia: string;
  //codBelfiore: string;
  //codiceIstat: string;
  //varCodBelfiore: string;
  //varProvincia: string;
  //varComune: string;
  constructor(comuneId: number){
    this.comuneId = comuneId;
  }
}
