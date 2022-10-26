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

export class Person {
  public static CF_ANONIMO = 'ANONIMO';
  private __isPersonObject = true;

  codiceIdentificativoUnivoco: string
  tipoIdentificativoUnivoco?: 'F'|'G';
  anagrafica?: string
  email?: string
  indirizzo?: string
  civico?: string
  cap?: string
  nazione?: string
  provincia?: string
  localita?: string
  nazioneId?: number
  provinciaId?: number
  localitaId?: number

  public static ANONIMO():Person{
    const anonimo = new Person();
    anonimo.tipoIdentificativoUnivoco = 'F';
    anonimo.codiceIdentificativoUnivoco = Person.CF_ANONIMO;
    return anonimo;
  }

  public static normalizeAnonimoDetails(person: Person): Person {
    if(!person?.__isPersonObject)
      person = _.merge(new Person(), person);
    person.tipoIdentificativoUnivoco = 'F';
    person.codiceIdentificativoUnivoco = Person.CF_ANONIMO;
    return person;
  }

  public static isPersonAnonimo(person: Person):boolean {
    return !person || !person.codiceIdentificativoUnivoco || person.codiceIdentificativoUnivoco===Person.CF_ANONIMO;
  }

  public static idIntestatario(person: Person):string {
    if(!person)
      return;
    //return a simple hash (inspired by java hashcode) of "CF + name + email"
    //console.log(person.codiceIdentificativoUnivoco + "|" + person.anagrafica + "|" + person.email, Array.from(person.codiceIdentificativoUnivoco + "|" + person.anagrafica + "|" + person.email).reduce((hash, char) => 0 | (31 * hash + char.charCodeAt(0)), 0));
    return Array.from(person.codiceIdentificativoUnivoco + "|" + person.anagrafica + "|" + person.email)
        .map(s => s.toLowerCase())
        .reduce((hash, char) => 0 | (31 * hash + char.charCodeAt(0)), 0).toString();
  }
}
