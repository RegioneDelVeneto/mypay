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
  export class FieldBean {
    name: string;
    required: boolean;
    regex: string;
    htmlRender: string;
    htmlClass: string;
    htmlLabel: string;
    htmlPlaceholder: string;
    bindCms: string;
    defaultValue: string;
    insertableOrder: number;
    indexable: boolean;
    renderableOrder: number;
    serchableOrder: number;
    listableOrder: number;
    insertable: boolean;
    renderable: boolean;
    searchable: boolean;
    isListable: boolean;
    isAssociation: boolean;
    isDetailLink: boolean;
    associationField: string;
    minOccurences: number;
    maxOccurences: number;
    groupBy: string;
    extraAttr: any;
    enumerationList: Array<string>;
    subfields: FieldBean[];

    validDependsOn: string;
    validDependsOnUids: string;

    valueDependsOn: string;
    valueDependsOnUids: string;

    hiddenDependsOn: string;
    hiddenDependsOnUids: string;

    mandatoryDependsOn: string;
    mandatoryDependsOnUids: string;

    enabledDependsOn: string;
    enabledDependsOnUids: string;

    errorMessage: string;
    helpMessage: string;
  }