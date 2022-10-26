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

  /**
   * DefinizioneDovuto
   *
   * @export
   * @interface IDefinizioneDovutoResponse
   */
  export interface IDefinizioneDovutoResponse {
    status: 'OK' | 'KO';
    code: string;
    message: string;
    data: string;
  }

  /**
   * SessionInformation
   *
   * @export
   * @interface ISessionInformation
   */
  export interface ISessionInformation {
    deTipoDovuto: string;
    utenteLoggato: boolean;
    enableCaptcha: boolean;
    ente: IEnte;
    publicCaptchaKey: string;
    importo: number;
  }

  /**
   * Utente
   *
   * @export
   * @interface IUtente
   */
  export interface IUtente {
      deEmailAddress: string;
      deFirstname: string;
      deLastname: string;
  }

  /**
   * Ente
   *
   * @export
   * @interface IEnte
   */
  export interface IEnte {
    codIpaEnte: string;
    codiceFiscaleEnte: string;
    deNomeEnte: string;
  }

  export interface IFieldBean {
    name: string;
    required: boolean;
    regex: string;
    html_render: string;
    html_class: string;
    html_label: string;
    html_placeholder: string;
    bind_cms: string;
    default_value: string;
    ins_order: number;
    isIndexable: boolean;
    renderable_order: number;
    ser_order: number;
    lis_order: number;
    isInsertable: boolean;
    isRenderable: boolean;
    isSearchable: boolean;
    isListable: boolean;
    isAssociation: boolean;
    isDetailLink: boolean;
    associationField: string;
    min_occurences: number;
    max_occurences: number;
    group_by: string;
    extra_map: any;
    enumeration_list: Array<string>;
    subfields: Array<IFieldBean>;

    valid_depends_on: string;
    valid_depends_on_uids: string;

    value_depends_on: string;
    value_depends_on_uids: string;

    hidden_depends_on: string;
    hidden_depends_on_uids: string;

    mandatory_depends_on: string;
    mandatory_depends_on_uids: string;

    enabled_depends_on: string;
    enabled_depends_on_uids: string;

    error_message: string;
    help_message: string;
  }

 export enum RenderType {
    TEXT,
    INTEGER,
    DOUBLE,
    BOOLEAN,
    DATE,
    DATETIME,
    TEXTAREA,
    HTMLAREA,
    IMAGE,
    SELECT,
    MULTILINK,
    FILE,
    NONE,
    SINGLESELECT,
    MULTISELECT,
    MULTIFIELD,
    ASSOCIATION,
    LINKGENERATOR,
    TAGS,
    TAB,
    LABEL
  }

  export interface IDefinizione {
      codIpa: string;
      codTipo: string;
      email: string;
      importo: number;
      captcha: string;
      data: Array<IDefinizioneDovuto>;
  }

  export interface IDefinizioneDovuto {
      name: string;
      value: any;
  }