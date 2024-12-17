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
import { TableAction } from './table-action';

export class WithActions {
  showFloatingButtons: Map<string,boolean>;
  enabledActions: Map<string,TableAction[]>;

  public static reset(element: WithActions){
    element.enabledActions = null;
  }

  public static toggleFloatingButtons(_tableId: string, _element: WithActions){
    //do nothing since we are no more using floating buttons for actions, but vertical menu
    return;
  }

}
