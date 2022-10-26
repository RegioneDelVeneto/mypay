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
import { IconDefinition } from '@fortawesome/fontawesome-common-types';

import { TableAction } from './table-action';
import { WithActions } from './with-actions';

export type ColumnType = 'text' | 'img64';

export class TableColumn {
  id: string;
  type: ColumnType;
  label: string | IconDefinition;
  sortable: boolean | ((item:any)=>string|number);
  pipe: any;
  pipeArgs: any[];
  tooltip: string;
  actions: TableAction[];
  checkbox: boolean = false;
  checkboxClick: (elementRef: WithActions, thisRef: any, eventRef: any) => any;
  ariaLabel: string;
  totalLabel: boolean = false;
  dispCondition : (thisRef:any, col?: TableColumn) => boolean;
  cellClick: (tableId: string, parentRef: any, element:any, tableColumn: TableColumn, onlyCheckEnabled?: boolean) => void | boolean;
  cellClickableCondition: (parentRef: any, element:any) => boolean;
  htmlId: string | (()=>string);

  //calculated fields
  hasIcon: boolean;
  isImg: boolean;

  constructor(id:string, label:string | IconDefinition, options: {
      htlmId?: string | (()=>string),
      type?: ColumnType,
      sortable?: boolean | ((item:any)=>string|number),
      pipe?: any,
      pipeArgs?: any[],
      tooltip?: string,
      actions?: TableAction[],
      checkbox?: boolean,
      checkboxClick?: (elementRef: WithActions, thisRef: any, eventRef: any) => any,
      ariaLabel?: string,
      totalLabel?: boolean,
      dispCondition?: (thisRef:any, col?: TableColumn)=>boolean,
      cellClick?: (tableId: string, parentRef: any, element:any, tableColumn: TableColumn, onlyCheckEnabled?: boolean) => void | boolean,
      cellClickableCondition?: (parentRef: any, element:any) => boolean} = null) {
    options = options || {};

    this.id = id;
    this.label = label;
    this.type = options.type || 'text';
    this.sortable = (options.type !== 'img64') && (options.sortable==null || options.sortable);
    this.pipe = options.pipe;
    this.pipeArgs = options.pipeArgs;
    this.tooltip = options.tooltip;
    this.actions = options.actions;
    this.checkbox = options.checkbox;
    this.checkboxClick = options.checkboxClick;
    this.ariaLabel = options.ariaLabel || label?.toString() || undefined;
    this.totalLabel = options.totalLabel;
    this.dispCondition = options.dispCondition;
    this.cellClick = options.cellClick;
    this.cellClickableCondition = options.cellClickableCondition;
    this.htmlId = options.htlmId;

    this.hasIcon = label && typeof label !== 'string';
    this.isImg = this.hasIcon || options.type === 'img64';
  }

  enabledActions(tableId: string, thisRef: any, elementRef: WithActions):TableAction[] {
    if(!(elementRef.enabledActions instanceof Map))
      elementRef.enabledActions = new Map();
    if(elementRef.enabledActions.has(tableId))
      return elementRef.enabledActions.get(tableId);

    const filtered = this.actions?.filter(action => {
      return !action.clickEnabledFun || action.clickEnabledFun(elementRef, thisRef);
    });
    elementRef.enabledActions.set(tableId, filtered);

    return filtered;
  }

  get calculatedHtmlId(){
    if(this.htmlId){
      if(typeof this.htmlId === 'string')
        return this.htmlId;
      else
        return this.htmlId() ?? this.id;
    }
    return this.id;
  }

  get additionalHtmlIdClasses(){
    const calcHtmlId = this.calculatedHtmlId;
    if(calcHtmlId === this.id)
      return null;
    else
      return [`cdk-column-${calcHtmlId}`, `mat-column-${calcHtmlId}`];
  }


}
