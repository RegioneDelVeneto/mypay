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

import { WithActions } from './with-actions';

export interface OverlayText {
  text: string;
  style?: string;
  transform?: string;
  class?: string;
}
export class TableAction {

  public static readonly ACTION_ID_DETTAGLIO = "__dettaglio__";

  icon: IconDefinition;
  tooltip?: string;
  clickFun: (elementRef: WithActions, thisRef: any, eventRef: any) => any;
  clickEnabledFun: (elementRef: WithActions, thisRef: any) => boolean;
  overlayText?: OverlayText;
  id: string;

  constructor(icon: IconDefinition,
    clickFun: (elementRef: WithActions, thisRef: any, eventRef: any) => any,
    clickEnabledFun: (elementRef: WithActions, thisRef: any) => boolean,
    tooltip?: string,
    overlayText?: OverlayText,
    id?: string
    ) {
    this.icon = icon;
    this.clickFun = clickFun;
    this.clickEnabledFun = clickEnabledFun;
    this.tooltip = tooltip;
    this.overlayText = overlayText;
    this.id = id;
  }

  click(tableId, elementRef: WithActions, thisRef: any, eventRef: any): any {
    const ret = this.clickFun(elementRef, thisRef, eventRef);
    WithActions.toggleFloatingButtons(tableId, elementRef);
    return ret;
  }
}
